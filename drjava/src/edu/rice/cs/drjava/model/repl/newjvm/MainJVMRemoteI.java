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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.Remote;
import java.rmi.RemoteException;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.drjava.model.junit.JUnitError;

/**
 * This interface specifies the methods that the Main JVM exposes
 * for the InterpreterJVM to call.
 *
 * @version $Id$
 */
public interface MainJVMRemoteI extends MasterRemote {
  public void systemErrPrint(String s) throws RemoteException;
  public void systemOutPrint(String s) throws RemoteException;
  
  /**
   * Called when a call to interpret has completed.
   * @param result The result of the interpretation
   */
  public void interpretResult(InterpretResult result) throws RemoteException;
  
  /**
   * This method is called by the interpreter JVM if it cannot
   * be exited (likely because of its having a
   * security manager)
   * @param th The Throwable thrown by System.exit
   */
  public void quitFailed(Throwable th) throws RemoteException;
  
  /**
   * Called if JUnit is invoked on a non TestCase class.
   */
  public void nonTestCase() throws RemoteException;
  
  /**
   * Called to indicate that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) throws RemoteException;
  
  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  public void testStarted(String testName) throws RemoteException;
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError)
    throws RemoteException;
  
  /**
   * Called when a full suite of tests has finished running.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) throws RemoteException;
  
  /**
   * Notifies the main jvm that an assignment has been made in the given debug 
   * interpreter.
   * Does not notify on declarations.
   * 
   * This method is not currently necessary, since we don't copy back
   * values in a debug interpreter until the thread has resumed.
   * 
   * @param name the name of the debug interpreter
   *
  public void notifyDebugInterpreterAssignment(String name) throws RemoteException;
  */

  /**
   * Asks the main jvm for input from the console.
   * @return the console input
   */
  public String getConsoleInput() throws RemoteException;
}
