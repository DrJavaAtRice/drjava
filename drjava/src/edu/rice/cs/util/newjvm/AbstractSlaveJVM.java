/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import java.rmi.*;

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
          Thread.currentThread().sleep(100);
          System.exit(0);
        }
        catch (Throwable th) {
          quitFailed(th);
        }
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
            Thread.currentThread().sleep(CHECK_MAIN_VM_ALIVE_MINUTES*60*1000);
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
