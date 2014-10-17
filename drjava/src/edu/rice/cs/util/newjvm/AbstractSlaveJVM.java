/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.plt.concurrent.ConcurrentUtil;

import java.rmi.*;

import static edu.rice.cs.plt.debug.DebugUtil.error;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A partial implementation of a {@link SlaveRemote} that provides the quit functionality and that also periodically 
  * checks if the master is still alive and automatically quits if not.
  * @version $Id: AbstractSlaveJVM.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class AbstractSlaveJVM implements SlaveRemote {
  public static final int CHECK_MAIN_VM_ALIVE_SECONDS = 1;
  
  /** Name of the thread to quit the slave. */
  private final String _quitSlaveThreadName;
  /** Name of the thread to periodically poll the master. */
  private final String _pollMasterThreadName;
  private boolean _started;
  
  public AbstractSlaveJVM() {
    this("Quit SlaveJVM Thread", "Poll MasterJVM Thread");
  }
  
  public AbstractSlaveJVM(String quitSlaveThreadName, String pollMasterThreadName) {
    _quitSlaveThreadName = quitSlaveThreadName;
    _pollMasterThreadName = pollMasterThreadName;
    _started = false;
  }
  
  /** Quits the slave JVM, calling {@link #beforeQuit} before it does. */
  public final synchronized void quit() {
    beforeQuit();
    // put exit into another thread to allow this RMI call to return normally.
    new Thread(_quitSlaveThreadName) {
      public void run() {
        // ensure (as best we can) that the quit() RMI call has returned cleanly
        synchronized(AbstractSlaveJVM.this) {
          try { System.exit(0); }
          catch (RuntimeException e) { error.log("Can't invoke System.exit", e); }
        }
      }
    }.start();
  }
  
  /** Initializes the Slave JVM including starting background thread to periodically poll the master JVM and 
    * automatically quit if it's dead.  Synchronized to prevent other method invocations from proceeding before
    * startup is complete.
    */
  public final synchronized void start(final MasterRemote master) throws RemoteException {
    if (_started) { throw new IllegalArgumentException("start() has already been invoked"); }
    master.checkStillAlive(); // verify that two-way communication works; may throw RemoteException

    Thread checkMaster = new Thread(_pollMasterThreadName) {
      public void run() {
        while (true) {
          ConcurrentUtil.sleep(CHECK_MAIN_VM_ALIVE_SECONDS*1000);
          try { master.checkStillAlive(); }
          catch (RemoteException e) {
            // TODO: This should always be an exceptional situation, but for now
            // many tests abandon the slave without quitting cleanly.
            // error.log("Master is no longer available", e);
            quit();
          }
        }
      }
    };
    checkMaster.setDaemon(true);
    checkMaster.start();
    handleStart(master);
  }
  
  /** This method is called just before the JVM is quit.  It can be overridden to provide cleanup code, etc. */
  protected void beforeQuit() { }
  
  /** Called when the slave JVM has started running.  Subclasses must implement this method. */
  protected abstract void handleStart(MasterRemote master);
  
}
