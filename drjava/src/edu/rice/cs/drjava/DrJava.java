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

package edu.rice.cs.drjava;

import java.io.PrintStream;
import edu.rice.cs.drjava.ui.MainFrame;
import edu.rice.cs.util.PreventExitSecurityManager;

/** 
 * Main class for DrJava. 
 * @version $Id$
 */
public class DrJava {
  private static final PrintStream _consoleOut = System.out;
  private static final PrintStream _consoleErr = System.err;
  private static PreventExitSecurityManager _manager;

  public static void main(String[] args) {

    /*
    Thread dbg = new Thread() {
      public void run() {
        while (true) {
          System.gc();
          int free = (int) (Runtime.getRuntime().freeMemory() / 1000);
          int total = (int) (Runtime.getRuntime().totalMemory() / 1000);
          int used = total - free;
          System.err.println(used + "k / "  + total + "k");

          try {
            Thread.currentThread().sleep(10000);
          } 
          catch (InterruptedException ie) {
            break;
          }
        }
      }
    };
    dbg.setDaemon(true);
    dbg.start();
    */
      
    try {
      System.setProperty("com.apple.macos.useScreenMenuBar","true");

      MainFrame mf = new MainFrame();

      // This enabling of the security manager must happen *after* the mainframe
      // is constructed. See bug #518509.
      enableSecurityManager();

      mf.show();
    } catch (Exception ex) {
      _consoleErr.println(ex.getClass().getName() + ": " + ex.getMessage());
      ex.printStackTrace(_consoleErr);
    }
  }

  public static PreventExitSecurityManager getSecurityManager() {
    return _manager;
  }

  public static void enableSecurityManager() {
    if (_manager == null) {
      _manager = PreventExitSecurityManager.activate();
    }

    if (System.getSecurityManager() != _manager) {
      System.setSecurityManager(_manager);
    }
  }

  public static void disableSecurityManager() {
    _manager.deactivate();
  }

  /**
   * Get the actual System.err stream.
   * @return System.err
   */
  public static PrintStream consoleErr() {
    return  _consoleErr;
  }

  /**
   * Get the actual System.out stream.
   * @return System.out
   */
  public static PrintStream consoleOut() {
    return  _consoleOut;
  }
}
