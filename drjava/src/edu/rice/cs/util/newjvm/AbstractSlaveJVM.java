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

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;

import java.rmi.*;

import static edu.rice.cs.plt.debug.DebugUtil.error;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A partial implementation of a {@link SlaveRemote} that provides the quit functionality and that also periodically 
  * checks if the master is still alive and automatically quits if not.
  * @version $Id$
  */
public abstract class AbstractSlaveJVM implements SlaveRemote {
  public static final int CHECK_MAIN_VM_ALIVE_SECONDS = 1;
  
  protected static final Log _log  = new Log("MasterSlave.txt", false);
  
//  /** remote reference to the Master JVM; after initialization it is immutable until quit is executed. */
//  public volatile MasterRemote _master;
  
  /** Name of the thread to quit the slave. */
  protected volatile String _quitSlaveThreadName = "Quit SlaveJVM Thread";
  
  /** Name of the thread to periodically poll the master. */
  protected volatile String _pollMasterThreadName = "Poll MasterJVM Thread";
  
  private volatile Thread _checkMaster = null;
  
  private final Object _slaveJVMLock = new Object();
  
  private volatile boolean _slaveExited = false;
  
  public AbstractSlaveJVM() throws RemoteException { }
  
  private void shutdown() {
//    try { 
//      boolean exported = UnicastRemoteObject.unexportObject(this, true); 
//      if (! exported) _log.log("ERROR: " + this + " was not unexported before shutdown");
//    }
//    catch(NoSuchObjectException e) { throw new UnexpectedException(e); }  // should never happen
    _log.log(AbstractSlaveJVM.this + ".shutdown() calling System.exit(0)");
    System.exit(0);
  }
  
  /** Quits the slave JVM, calling {@link #beforeQuit} before it does. */
  public final synchronized void quit() {
//    _log.log(this + ".quit() called");
//    _master = null;
    
    beforeQuit();
    
    _slaveExited = false;
//    Utilities.showDebug("quit() called");
    
    // put exit into another thread to allow this RMI call to return normally.
    Thread t = new Thread(_quitSlaveThreadName) {
      public void run() {
        try {
          // wait for parent RMI calling thread to exit 
          synchronized(_slaveJVMLock) { 
            while (! _slaveExited) {
//              _log.log("Waiting for " + AbstractSlaveJVM.this + ".quit() to exit");
              _slaveJVMLock.wait(); 
            }
          }
          shutdown();
        }
        catch(Throwable t) { 
          _log.log(this + ".quit() failed!");
          quitFailed(t); 
        }
      }
    };
    
    t.start();
//    _log.log(this + ".quit() RMI call exited");
    synchronized(_slaveJVMLock) { 
      _slaveExited = true; 
      _slaveJVMLock.notify();  // There does not appear to be any constraint forcing this thread to exit before shutdown
    }
  }
  
  /** This method is called just before the JVM is quit.  It can be overridden to provide cleanup code, etc. */
  protected void beforeQuit() { }
  
  /** This method is called if the interpreterJVM cannot be exited (likely because of a unexpected security manager.) */
  protected void quitFailed(Throwable th) { }
  
  /** Initializes the Slave JVM including starting background thread to periodically poll the master JVM and 
    * automatically quit if it's dead.  Unsynchronized because 
    * (i)   this method can only be called once (without throwing an error) and _master is immutable once assigned here
    *       until quit() 
    * (ii)  this method does not depend on any mutable state in this (which constrains {@link #handleStart}); and
    * (iii) this method (and perhaps {@link #handleStart}) perform remote calls on master.
    * This method delegates starting actions other than polling master to {@link #handleStart}.
    */
  public final void start(final MasterRemote master) throws RemoteException {
    
    if (_checkMaster != null) throw new UnexpectedException(this + ".start(...) called a second time");
    
    _checkMaster = new Thread(_pollMasterThreadName) {
      public void run() { // Note: this method is NOT synchronized; it runs in a different thread.
        while (true) {
          try { Thread.sleep(CHECK_MAIN_VM_ALIVE_SECONDS*1000); }
          catch (InterruptedException ie) { }
//          _log.log(this + " polling " + master + " to confirm Master JVM is still alive");
          try { master.checkStillAlive(); }
          catch (RemoteException re) {
            // TODO: This should always be an exceptional situation, but for now
            // many tests abandon the slave without quitting cleanly.
            // error.log(re);
            quit(); // Master JVM service is defunct. Quit! */
          }
        }
      }
    };
    
    
    
    _checkMaster.setDaemon(true);
    _checkMaster.start();
    _log.log(_checkMaster + " created and STARTed by " + this);
    
    handleStart(master);  // master is passed as parameter because in some refactorings, _master is eliminated
    
  }
  
  /** Called when the slave JVM has started running.  Subclasses must implement this method. */
  protected abstract void handleStart(MasterRemote master);
  
//  public void finalize() { _log.log(this + " has been FINALIZED"); }
}
