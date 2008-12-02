/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.concurrent.ConcurrentUtil;

import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Map;

/** An abstract class implementing the logic to invoke and control, via RMI, a second Java virtual 
  * machine. This class is used by subclassing it. (See package documentation for more details.)
  * This class runs in both the master and the slave JVMs.
  * @version $Id$
  */
public abstract class AbstractMasterJVM/*<SlaveType extends SlaveRemote>*/ implements MasterRemote/*<SlaveType>*/ {
  
  public static final Log _log  = new Log("MasterSlave.txt", false);
  
  /** Name for the thread that waits for the slave to exit. */
  protected volatile String _waitForQuitThreadName = "Wait for SlaveJVM Exit Thread";
  
//  /** Name for the thread that exports the MasterJVM to RMI. */
//  protected volatile String _exportMasterThreadName = "Export MasterJVM Thread";
  
  /** Lock for accessing the critical state of this AbstractMasterJVM including _monitorThread.  */
  protected final Object _masterJVMLock = new Object();
  
  private static final String RUNNER = SlaveJVMRunner.class.getName();
  
  /** The slave JVM remote stub if it's connected; null if not connected. */
  protected volatile SlaveRemote _slave;
  
  /** Is slave JVM in the process of starting up?  INVARIANT: _startupInProgess => _slave == null. */
  private volatile boolean _startupInProgress = false;
  
  /** This flag is set when a quit request is issued before the slave has finished starting up. 
    * In that case, immediately after starting up, we quit it. INVARIANT: _quitOnStartUp => _startupInProgress 
    */
  private volatile boolean _quitOnStartup = false;
  
//  /** Lock used in exporting this object to a file and loading it in the slaveJVM; protects stub variables. */
//  final static Object Lock = new Object();
  
  /** The current remote stub for this main JVM object. This field is null except between the time the slave
    * JVM is first invoked and the time the slave registers itself.
    */
  private volatile MasterRemote _masterStub = null;
  
  /** The file containing the serialized remote stub. This field is null except between the time the slave
    * JVM is first invoked and the time the slave registers itself.
    */
  private volatile File _masterStubFile;
  
  /** The fully-qualified name of the slave JVM class. */
  private final String _slaveClassName;
  
  /** The thread monitoring the Slave JVM, waiting for it to terminate.  This feature inhibits the creation
    * of more than one Slave JVM corresponding to "this" 
    */
  private volatile Thread _monitorThread;
  
//  /** The lock used to protect _monitorThread. */
//  private final Object _monitorLock = new Object();
  
  /** Sets up the master JVM object, but does not actually invoke the slave JVM.
    * @param slaveClassName The fully-qualified class name of the class to start up in the second JVM. This class must
    * implement the interface specified by this class's type parameter, which must be a subclass of {@link SlaveRemote}.
    */
  protected AbstractMasterJVM(String slaveClassName) throws RemoteException {
    super();
    _slaveClassName = slaveClassName;
    _slave = null;
    _monitorThread = null;
    
    _log.log(this + " CREATED");
    
    // Make sure RMI doesn't use an IP address that might change
    System.setProperty("java.rmi.server.hostname", "127.0.0.1");
  }
  
  /** Callback for when the slave JVM has connected, and the bidirectional communications link has been 
    * established.  During this call, {@link #getSlave} is guaranteed to not return null.
    */
  protected abstract void handleSlaveConnected();
  
  /** Callback for when the slave JVM has quit. During this call, {@link #getSlave} is guaranteed to return null.
    * @param status The exit code returned by the slave JVM.
    */
  protected abstract void handleSlaveQuit(int status);
  
  /** Invokes slave JVM without any JVM arguments.
    * @throws IllegalStateException if slave JVM already connected or startUp is in progress.
    */
  protected final void invokeSlave() throws IOException, RemoteException {
    invokeSlave(new String[0], FileOps.NULL_FILE);
  }
  
  /** Invokes slave JVM, using the system classpath.
    * @param jvmArgs Array of arguments to pass to the JVM on startUp
    * @throws IllegalStateException if slave JVM already connected or startUp is in progress.
    */
  protected final void invokeSlave(String[] jvmArgs, File workDir) throws IOException, RemoteException {
    invokeSlave(jvmArgs, System.getProperty("java.class.path"), workDir);
  }
  
  /** Creates and invokes slave JVM.
    * @param jvmArgs Array of arguments to pass to the JVM on startUp
    * @param cp Classpath to use when starting the JVM
    * @throws IllegalStateException if slave JVM already connected or startUp is in progress.
    */
  protected final void invokeSlave(final String[] jvmArgs, final String cp, final File workDir) throws IOException, 
    RemoteException {
    
    synchronized(_masterJVMLock) { // synchronization prelude only lets one thread at a time execute the sequel
      
      try { while (_startupInProgress || _monitorThread != null) _masterJVMLock.wait(); }
      catch(InterruptedException e) { throw new UnexpectedException(e); }
      _startupInProgress = true;
    }
    
    _log.log(this + ".invokeSlave(...) called");
    
    /******************************************************************************************************
      * First, we we export ourselves to a file, if it has not already been done on a previous invocation. *
      *****************************************************************************************************/
    
    if (_masterStub == null) {
      try { _masterStub = (MasterRemote) UnicastRemoteObject.exportObject(this, 0); }
      catch (RemoteException re) {
        javax.swing.JOptionPane.showMessageDialog(null, edu.rice.cs.util.StringOps.getStackTrace(re));
        _log.log(this + " threw " + re);
        throw new UnexpectedException(re);  // should never happen
      }
      _log.log(this + " EXPORTed Master JVM");
      
      _masterStubFile = File.createTempFile("DrJava-remote-stub", ".tmp");
      _masterStubFile.deleteOnExit();
      
      // serialize stub to _masterStubFile
      FileOutputStream fstream = new FileOutputStream(_masterStubFile);
      ObjectOutputStream ostream = new ObjectOutputStream(fstream);
      ostream.writeObject(_masterStub);
      ostream.flush();
      fstream.close();
      ostream.close();
    }
    
    final String[] args = new String[] { _masterStubFile.getAbsolutePath(), _slaveClassName };
    
    LinkedList<String> fullJVMArgs = new LinkedList<String>(Arrays.asList(jvmArgs));
    Properties propagate = ConcurrentUtil.getProperties("plt.", "drjava.", "edu.rice.cs.");
    if (propagate.containsKey("plt.debug.log") || propagate.containsKey("plt.error.log") || 
        propagate.containsKey("plt.log.factory")) {
      propagate.put("plt.log.working.dir", System.getProperty("user.dir", ""));
    }
    for (Map.Entry<Object, Object> entry : propagate.entrySet()) {
      fullJVMArgs.addFirst("-D" + entry.getKey() + "=" + entry.getValue());
    }
    final String[] jvmArgsArray = fullJVMArgs.toArray(new String[0]);
    
    // Start a thread to create the slave JVM and wait for it to die.  When it dies, delegate what to do (restart?) 
    // to subclass
    _monitorThread = new Thread(_waitForQuitThreadName) {
      public void run() {
        try { /* Create the slave JVM. */ 
          
          _log.log(AbstractMasterJVM.this + " is STARTING a Slave JVM with args " + Arrays.asList(args));
          
          final Process process = ExecJVM.runJVM(RUNNER, args, cp, jvmArgsArray, workDir);
          _log.log(AbstractMasterJVM.this + " CREATED Slave JVM process " + process + " with " + asString());
          
          int status = process.waitFor();
//          System.err.println(process + " DIED under control of " + asString() + " with status " + status);
          synchronized(_masterJVMLock) {
            if (_startupInProgress) {
//              System.err.println("Process " + process + " died while starting up");
              /* If we get here, the process died without registering.  One possible cause is the intermittent funky 3 minute
               * pause in readObject in RUNNER.  Other possible causes are errors in the classpath or the absence of a 
               * debug port.  Proper behavior in this case is unclear, so we'll let our subclasses decide. */
              slaveQuitDuringStartup(status);
            }
            if (_slave != null) { // Slave JVM quit spontaneously
              _slave = null; 
            }
            _monitorThread = null;
            _masterJVMLock.notifyAll();  // signal that Slave JVM died to any thread waiting for _monitorThread == null
          }
          
//          _log.log(asString() + " calling handleSlaveQuit(" + status + ")");
          handleSlaveQuit(status);
        }
        catch(NoSuchObjectException e) { throw new UnexpectedException(e); }
        catch(InterruptedException e) { throw new UnexpectedException(e); }
        catch(IOException e) { throw new UnexpectedException(e); }
      }
      private String asString() { return "MonitorThread@" + Integer.toHexString(hashCode()); }
    };
//    _log.log(this + " is starting a slave monitor thread to detect when the Slave JVM dies");
    _monitorThread.start();
  }
  
  /** Waits until no slave JVM is running under control of "this" */
  public void waitSlaveDone() {
    try { synchronized(_masterJVMLock) { while (_monitorThread != null) _masterJVMLock.wait(); }}
    catch(InterruptedException e) { throw new UnexpectedException(e); } 
  }
  
  /** Action to take if the slave JVM quits before registering.  Assumes _masterJVMLock is held.
    * @param status Status code of the JVM
    */
  protected void slaveQuitDuringStartup(int status) {
    // Reset Master JVM state (in case invokeSlave is called again on this object)
    _startupInProgress = false;
    _quitOnStartup = false;
    _monitorThread = null;
  }
  
  /** Called if the slave JVM dies before it is able to register.
    * @param cause The Throwable which caused the slave to die.
    */
  public abstract void errorStartingSlave(Throwable cause) throws RemoteException;
  
  /** No-op to prove that the master is still alive. */
  public void checkStillAlive() { }
  
  /* Records the identity and status of the Slave JVM in the Master JVM */
  public void registerSlave(SlaveRemote slave) throws RemoteException {
    _log.log(this + " registering Slave " + slave);
    
    boolean quitSlavePending;  // flag used to move quitSlave() call out of synchronized block
    
    synchronized(_masterJVMLock) {
      _slave = slave;
      _startupInProgress = false;
      
      _log.log(this + " calling handleSlaveConnected()");
      
      handleSlaveConnected();
      
      quitSlavePending = _quitOnStartup;
      if (_quitOnStartup) {
        // quitSlave was called before the slave registered, so we now act on the deferred quit request.
        _quitOnStartup = false;
      }
    }
    if (quitSlavePending) {
      _log.log(this + " Executing deferred quitSlave() that was called during startUp");
      quitSlave();  // not synchronized; _slave may be null when this code executes
    }
  }
  
  /** Withdraws RMI exports for this. */
  public void dispose() throws RemoteException {
    _log.log(this + ".dispose() called; slaveRemote is " + _slave);
    if (_startupInProgress) _log.log(this + ".dispose() is KILLing startUp in process; dying slave reference does not yet exist");
    SlaveRemote dyingSlave;
    synchronized(_masterJVMLock) {
      _masterStub = null;
      if (_monitorThread != null) _monitorThread = null;
      dyingSlave = _slave;  // save value of _slave in case it is not null
      _slave = null;
      
      // Withdraw RMI exports
      // Slave in process of starting will die because master is inaccessible.
      _log.log(this + ".dispose() UNEXPORTing " + this);
      UnicastRemoteObject.unexportObject(this, true);
    }
    if (dyingSlave != null) { 
      _log.log(this + ".dispose() QUITing " + dyingSlave);
      dyingSlave.quit();  // unsynchronized; may hasten the death of dyingSlave
    }
  }
  
  /** Quits slave JVM.  On exit, _slave == null.  _quitOnStartup may be true
    * @throws IllegalStateException if no slave JVM is connected
    */
  protected final void quitSlave() throws RemoteException {
    SlaveRemote dyingSlave;
    synchronized(_masterJVMLock) {
      if (isStartupInProgress()) {
        /* There is a slave to be quit, but _slave == null, so we cannot contact it yet. Instead we set _quitOnStartup
         * and tell the slave to quit when it registers in registerSlave. */
        _quitOnStartup = true;
        return;
      }
      else if (_slave == null)  {
        _log.log(this + " called quitSlave() when no slave was running");
        return;
      }
      else {
        dyingSlave = _slave;
        _slave = null;
      }
    }
    dyingSlave.quit();  // remote operation is not synchronized!
  }
  
  /** Returns slave remote instance, or null if not connected. */
  protected final SlaveRemote getSlave() {  return _slave; }
  
  /** Returns true if the slave is in the process of starting. */
  protected boolean isStartupInProgress() { return _startupInProgress; }
}

