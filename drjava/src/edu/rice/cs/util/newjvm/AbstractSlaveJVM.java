/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import java.rmi.*;
import edu.rice.cs.util.FileOps;
import java.io.*;
/**
 * A partial implementation of a {@link SlaveRemote} that provides
 * the quit functionality and that also periodically checks if the master is
 * still alive and automatically quits if not.
 *
 * @version $Id$
 */
public abstract class AbstractSlaveJVM implements SlaveRemote {
  public static final int CHECK_MAIN_VM_ALIVE_MINUTES = 1;

  /**
   * Name of the thread to quit the slave.
   */
  protected String _quitSlaveThreadName = "Quit SlaveJVM Thread";

  /**
   * Name of the thread to periodically poll the master.
   */
  protected String _pollMasterThreadName = "Poll MasterJVM Thread";

  
  /**
   * Quits the slave JVM, calling {@link #beforeQuit} before it does.
   */
  public final void quit() {
    beforeQuit();

    // put exit into another thread to allow this RMI call to return normally.
    Thread t = new Thread(_quitSlaveThreadName) {
      public void run() {
        try {
          Thread.sleep(100);
          System.exit(0);
        }
        catch (Throwable th) { quitFailed(th); }
      }
    };

    t.start();
  }

  /**
   * This method is called just before the JVM is quit.
   * It can be overridden to provide cleanup code, etc.
   */
  protected void beforeQuit() {}

  /**
   * This method is called if the interpreterJVM cannot
   * be exited (likely because of its having a
   * security manager)
   */
  protected void quitFailed(Throwable th) {}

  /**
   * Starts background thread to periodically poll the master JVM and
   * automatically quit if it's dead.
   * It delegates the actual start to {@link #handleStart}.
   */
  public final void start(final MasterRemote master) throws RemoteException {
    Thread thread = new Thread(_pollMasterThreadName) {
      public void run() {
        while (true) {
          try {
            Thread.sleep(CHECK_MAIN_VM_ALIVE_MINUTES*60*1000);
          }
          catch (InterruptedException ie) {
          }

          try {
            master.checkStillAlive();
          }
          catch (RemoteException re) {
            // not there anymore. quit!
            quit();
          }
        }
      }
    };

    thread.setDaemon(true);
    thread.start();

    handleStart(master);
  }

  /**
   * Called when the slave JVM has started running.  Subclasses must implement
   * this method.
   */
  protected abstract void handleStart(MasterRemote master);
}
