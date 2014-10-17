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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.File;
import java.rmi.RemoteException;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.util.classloader.ClassFileError;

/**
 * This interface specifies the methods that the Main JVM exposes
 * for the InterpreterJVM to call.
 *
 * @version $Id: MainJVMRemoteI.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public interface MainJVMRemoteI extends MasterRemote {
  
  /** Forwards a call to System.err from InterpreterJVM to the MainJVM for output to the user.
    * @param s String that was printed in the other JVM
    */
  public void systemErrPrint(String s) throws RemoteException;
  
  /** Forwards a call to System.out from InterpreterJVM to the MainJVM for output to the user.
    * @param s String that was printed in the other JVM
    */
  public void systemOutPrint(String s) throws RemoteException;
  
  /** Asks the main jvm for input from the console.
   * @return the console input
   */
  public String getConsoleInput() throws RemoteException;
  
  /** Called if JUnit is invoked on a non TestCase class.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) throws RemoteException;
  
  /** Called if the slave JVM encounters an illegal class file during testing.
    * @param e the ClassFileError object describing the error when loading the class file.
    */
  public void classFileError(ClassFileError e) throws RemoteException;
  
  /** Called to indicate that a suite of tests has started running.
    * @param numTests The number of tests in the suite to be run.
    */
  public void testSuiteStarted(int numTests) throws RemoteException;
  
  /** Called when a particular test is started.
    * @param testName The name of the test being started.
    */
  public void testStarted(String testName) throws RemoteException;
  
  /** Called when a particular test has ended.
    * @param testName The name of the test that has ended.
    * @param wasSuccessful Whether the test passed or not.
    * @param causedError If not successful, whether the test caused an error
    * or simply failed.
    */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError)
    throws RemoteException;
  
  /** Called when a full suite of tests has finished running.
    * @param errors The array of errors from all failed tests in the suite.
    */
  public void testSuiteEnded(JUnitError[] errors) throws RemoteException;
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.
    * @param className the name of the class for which we want to find the file
    * @return the file associated with the given class
    */
  public File getFileForClassName(String className) throws RemoteException;
  
  /** Notifies the main jvm that an assignment has been made in the given debug
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
  
}
