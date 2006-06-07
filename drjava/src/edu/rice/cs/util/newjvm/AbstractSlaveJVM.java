/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
//import edu.rice.cs.util.PreventExitSecurityManager;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

//import edu.rice.cs.util.PreventExitSecurityManager;

/** A partial implementation of a {@link SlaveRemote} that provides the quit functionality and that also periodically 
 *  checks if the master is still alive and automatically quits if not.
 *  @version $Id$
 */
public abstract class AbstractSlaveJVM implements SlaveRemote, Serializable {
  public static final int CHECK_MAIN_VM_ALIVE_SECONDS = 1;
  
  protected static final Log _log  = new Log("MasterSlave.txt", true);
  
//  /** remote reference to the Master JVM; after initialization it is immutable until quit is executed. */
//  public volatile MasterRemote _master;

  /** Name of the thread to quit the slave. */
  protected volatile String _quitSlaveThreadName = "Quit SlaveJVM Thread";

  /** Name of the thread to periodically poll the master. */
  protected volatile String _pollMasterThreadName = "Poll MasterJVM Thread";
  
  private volatile Thread _checkMaster = null;

  private final Object _slaveJVMLock = new Object();
  
  private volatile boolean _slaveExited = false;
  
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
        catch (Throwable th) { 
          _log.log(this + ".quit() failed!");
          quitFailed(th); 
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
   *  automatically quit if it's dead.  Unsynchronized because 
   *  (i)   this method can only be called once (without throwing an error) and _master is immutable once assigned here
   *        until quit() 
   *  (ii)  this method does not depend on any mutable state in this (which constrains {@link #handleStart}); and
   *  (iii) this method (and perhaps {@link #handleStart}) perform remote calls on master.
   *  This method delegates starting actions other than polling master to {@link #handleStart}.
   */
  public final void start(final MasterRemote master) throws RemoteException {
    
    if (_checkMaster != null) throw new UnexpectedException(this + ".start(...) called a second time");

    _checkMaster = new Thread(_pollMasterThreadName) {
      public void run() { // Note: this method is NOT synchronized; it runs in a different thread.
//        PreventExitSecurityManager.activate();
        while (true) {
          try { Thread.sleep(CHECK_MAIN_VM_ALIVE_SECONDS*1000); }
          catch (InterruptedException ie) { }
//          _log.log(this + " polling " + master + " to confirm Master JVM is still alive");
          try { master.checkStillAlive(); }
          catch (RemoteException re) { quit(); }  // Master JVM service is defunct. Quit! */
        }
      }
    };

    _checkMaster.setDaemon(true);
    _checkMaster.start();

    handleStart(master);  // master is passed as parameter because in some refactorings, _master is eliminated
  }

  /** Called when the slave JVM has started running.  Subclasses must implement this method. */
  protected abstract void handleStart(MasterRemote master);
  
//  public void finalize() { _log.log(this + " has been FINALIZED"); }
}
