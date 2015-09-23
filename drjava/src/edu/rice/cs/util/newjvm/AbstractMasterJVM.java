/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.util.Map;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.concurrent.ConcurrentUtil;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.plt.concurrent.StateMonitor;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.reflect.ReflectException;
import edu.rice.cs.plt.reflect.ReflectUtil;

import edu.rice.cs.util.Log;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** An abstract class implementing the logic to invoke and control, via RMI, a second Java virtual 
  * machine. This class is used by subclassing it. (See package documentation for more details.)
  * The state-changing methods of this class consistently block until a precondition for the state
  * change is satisfied &mdash; for example, {@link #quitSlave} cannot complete until a slave is
  * running.  Only one thread may change the state at a time.  Thus, clients should be careful
  * to only invoke state-changing methods when they are guaranteed to succeed (only invoking
  * {@code quitSlave()}, for example, when it is known to have been matched by a successful
  * {@code invokeSlave} invocation).
  *  
  * @version $Id: AbstractMasterJVM.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class AbstractMasterJVM implements MasterRemote {
  
  /** Synchronization strategy: compare-and-swap guarantees that only one thread enters a STARTING, or
    * QUITTING, or DISPOSED state.  After that, the only state transitions out of STARTING/QUITTING occur 
    * in the same thread (or a single designated worker thread); all other threads must wait until the
    * transition to FRESH or RUNNING.
    */
  private enum State { FRESH, STARTING, RUNNING, QUITTING, DISPOSED };
  
  private static Log _log = new Log("MasterJVM.txt", true);
  
  /** Loads an instance of the given AbstractSlaveJVM class.  Invoked in the slave JVM. */
  private static class SlaveFactory implements Thunk<AbstractSlaveJVM>, Serializable {
    private final String _className;
    public SlaveFactory(String className) { _className = className; }
    public AbstractSlaveJVM value() {
      try { return (AbstractSlaveJVM) ReflectUtil.getStaticField(_className, "ONLY"); }
      catch (ReflectException e) {
        try { return (AbstractSlaveJVM) ReflectUtil.loadObject(_className); }
        catch (ReflectException e2) { throw new WrappedException(e2); }
     }
    }
  }
  
  private final StateMonitor<State> _monitor;
  private final SlaveFactory _slaveFactory;
  private final LazyThunk<MasterRemote> _masterStub;
  /** The slave JVM remote stub (non-null when the state is RUNNING). */
  private volatile SlaveRemote _slave;
  
  /**
   * Set up the master JVM object.  Does not start a slave JVM.
   * @param slaveClassName The fully-qualified class name of the class to start up in the second JVM.  Must be a
   *                       subclass of {@link AbstractSlaveJVM}.
   */
  protected AbstractMasterJVM(String slaveClassName) {
    _monitor = new StateMonitor<State>(State.FRESH);
    _slaveFactory = new SlaveFactory(slaveClassName);
    _masterStub = new LazyThunk<MasterRemote>(new Thunk<MasterRemote>() {
      public MasterRemote value() {
        try { return (MasterRemote) UnicastRemoteObject.exportObject(AbstractMasterJVM.this, 0); }
        catch (RemoteException re) {
          error.log(re);
          throw new UnexpectedException(re);
        }
      }
    });
    _slave = null;
    // Make sure RMI doesn't use an IP address that might change
    System.setProperty("java.rmi.server.hostname", "127.0.0.1");
  }
  
  /** Callback for when the slave JVM has connected, and the bidirectional communications link has been 
    * established.  Provides access to the newly-created slave JVM.
    */
  protected abstract void handleSlaveConnected(SlaveRemote newSlave);
  
  /** Callback for when the slave JVM has quit.
    * @param status The exit code returned by the slave JVM.
    */
  protected abstract void handleSlaveQuit(int status);
  
  /**
   * Callback for when the slave JVM fails to either run or respond to {@link SlaveRemote#start}.
   * @param e  Exception that occurred during startup.
   */
  protected abstract void handleSlaveWontStart(Exception e);
  
  /** Creates and starts the slave JVM.  If the the slave is currently running, waits until it completes.
    * Also waits until the new process has started up and calls one of {@link #handleSlaveConnected}
    * or {@link #handleSlaveWontStart} before returning.
    * @param jvmBuilder  JVMBuilder to use in starting the remote process.
    * @throws IllegalStateException  If this object has been disposed.
    */
  protected final void invokeSlave(JVMBuilder jvmBuilder) {
    transition(State.FRESH, State.STARTING);

    // update jvmBuilder with any special properties
    Map<String, String> props = ConcurrentUtil.getPropertiesAsMap("plt.", "drscala.", "edu.rice.cs.");
    if (!props.containsKey("plt.log.working.dir") && // Set plt.log.working.dir, in case the working dir changes
        (props.containsKey("plt.debug.log") || props.containsKey("plt.error.log") || 
            props.containsKey("plt.log.factory"))) {
      props.put("plt.log.working.dir", System.getProperty("user.dir", ""));
    }
    // include props, but shadow them with any definitions in jvmBuilder
    final JVMBuilder tweakedJVMBuilder = jvmBuilder.properties(CollectUtil.union(props, jvmBuilder.properties()));

    SlaveRemote newSlave = null;
    try {
      _log.log("invoking remote JVM process");
      newSlave =
        (SlaveRemote) ConcurrentUtil.exportInProcess(_slaveFactory, tweakedJVMBuilder, new Runnable1<Process>() {
          public void run(Process p) {
            _log.log("Remote JVM quit");
            _monitor.set(State.FRESH);
            _log.log("Entered state " + State.FRESH + "; Invoking handleSlaveQuit(" + p.exitValue() + ")");
            handleSlaveQuit(p.exitValue());
          }
        });
    }
    catch (Exception e) {
      _log.log("invoking remote JVM process failed with exception " + e);
      _monitor.set(State.FRESH);
      _log.log("Entered state " + State.FRESH + "\nInvoking handleSlaveWontStart(" + e + ")");              
      handleSlaveWontStart(e);
    }

    if (newSlave != null) {
      _log.log("newSlave is not null");
      try {
        _log.log("Starting newSlave with arg = '" + _masterStub.value() + "'");
        newSlave.start(_masterStub.value());   // advances state to RUNNING
      }
      catch (RemoteException e) {
        _log.log("Threw Exception " + e + "\nAttempting to quit with argument '" + newSlave + "'");
        attemptQuit(newSlave);
        _log.log("Entered state " + State.FRESH + "\nCalling handleSlaveWontStart(" + e + ")");
        _monitor.set(State.FRESH);
        handleSlaveWontStart(e);
        return;
      }
      
      _log.log("Calling handleSlaveConnected(" + newSlave + ")");
      handleSlaveConnected(newSlave);  // calls  _stateMonitor.value().started
      _slave = newSlave;
//      _log.log("Entered new state " + State.RUNNING);
//      _monitor.set(State.RUNNING);  // already accomplished by newSlave.start() above
    }
  }
  
  /** Quits slave JVM.  If a slave is not currently started and running, blocks until that state is reached.
    * @throws IllegalStateException  If this object has been disposed.
    */
  protected final void quitSlave() {
    _log.log("quitSlave called; state is " + _monitor.value());
    transition(State.RUNNING, State.QUITTING);
    attemptQuit(_slave);
    _slave = null;
    _log.log("Entered state " + State.FRESH);
    _monitor.set(State.FRESH);
  }
    
  /** Make a best attempt to invoke {@code slave.quit()}.  Log an error if it fails. */
  private static void attemptQuit(SlaveRemote slave) {
    try { 
      slave.quit();
      _log.log("Called slave.quit()");
    }
    catch (RemoteException e) { 
      _log.log("Unable to complete slave.quit(); rhrew " + e);
      error.log("Unable to complete slave.quit()", e); 
    }
  }
  
  /**
   * Free the resources required for this object to respond to RMI invocations (useful for applications -- such as
   * testing -- that produce a large number of MasterJVMs as a program runs).  Requires the slave to have
   * quit; blocks until that occurs.  After an object has been disposed, it is no longer useful.
   */
  protected void dispose() {
    transition(State.FRESH, State.DISPOSED);
    if (_masterStub.isResolved()) { 
      try { UnicastRemoteObject.unexportObject(this, true); }
      catch (NoSuchObjectException e) { error.log(e); }
    }
  }
  
  /** Transition from State.STARTING to State.RUNNING; used in MainJVM from which State is hidden. */
  protected void _enterRunningState() { transition(State.STARTING, State.RUNNING); }
  
  /** Make a thread-safe state transition.  Blocks until the {@code from} state is reached and this
    * thread is successful in performing the transition (only one thread can do so at a time).  Throws
    * an IllegalStateException if the DISPOSED state is reached first, since there is never a transition
    * out of the disposed state (the alternative is to block permanently). 
    */
  private void transition(State from, State to) {
    _log.log("AbstractMasterJVM.transition(" + from + ", " + to + ") called");
    State s = _monitor.value();
    // watch all state transitions until from->to is successful or the DISPOSED state is reached
    while (!(s.equals(from) && _monitor.compareAndSet(from, to))) {
      if (s.equals(State.DISPOSED)) { throw new IllegalStateException("In disposed state"); }
      _log.log("Waiting for transition from " + s + " to " + from);
      try { s = _monitor.ensureNotState(s); }
      catch (InterruptedException e) { throw new UnexpectedException(e); }
    }
   _log.log("Entered state " + to);
  }
  
  protected boolean isDisposed() { return _monitor.value().equals(State.DISPOSED); }
  
  /** No-op to prove that the master is still alive. */
  public void checkStillAlive() { }
  
}

