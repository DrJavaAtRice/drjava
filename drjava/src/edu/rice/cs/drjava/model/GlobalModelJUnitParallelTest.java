/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the names of its contributors may be used 
 *      to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software. Open Source Initative Approved is a trademark
 * of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/ or 
 * http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

/** This is the parallel version of GlobalModelJUnitTest, we add     _model.getJUnitModel().setRunTestParallel(true); 
 * before each call to Junit  to make sure that test case can be tested in parallel. 
 * testJUnit4MultiTest_NOJOIN's assertNonTestCaseCount() and listener.assertJUnitStartCount(1) are comment out because multi test are run
 * in multiple thread and can't run wait in Global Junit model
 * testRealError_NOJOIN is discarded because JunitCore doesn't separate error and failure
  * @version $Id$
  */
public final class GlobalModelJUnitParallelTest extends GlobalModelJunitTestCase {
  
  private static Log _log = new Log("GlobalModelJUnitParallelTest.txt", true);
  

  
  /** Tests that a JUnit file with no errors is reported to have no errors. 
    * @throws Exception if something goes wrong 
    */
  public void testNoJUnitErrors_NOJOIN() throws Exception {
	  testNoJUnitErrors_NOJOIN(true);
  }
  
  /** Tests that a JUnit file with an error is reported to have an error. 
   * @throws Exception if something goes wrong 
   */
  public void testOneJUnitError_NOJOIN() throws Exception {
	  testOneJUnitError_NOJOIN(true);
  }
  
  /** Tests that a JUnit file with an error is reported to have an error. 
   * @throws Exception if something goes wrong 
   */
  public void testElspethOneJUnitError_NOJOIN() throws Exception {
	  testElspethOneJUnitError_NOJOIN(true);
  }
  

  /** Tests that the ui is notified to put up an error dialog if JUnit is run on a non-TestCase. 
   * @throws Exception if something goes wrong 
   */
  public void testNonTestCaseError_NOJOIN() throws Exception {
	  testNonTestCaseError_NOJOIN(true);
  }
  
  /** Tests that the UI is notified to put up an error dialog if JUnit is run on a non-public TestCase. 
    * @throws Exception if something goes wrong 
    */
  public void testResultOfNonPublicTestCase_NOJOIN() throws Exception {
	  testResultOfNonPublicTestCase_NOJOIN(true);
  }
  

  
  /** Tests a document that has no corresponding class file. 
    * @throws Exception if something goes wrong 
    */
  public void testNoClassFile() throws Exception {
	  testNoClassFile(true);
  }
  
  // Commented out because MultiThreadedTestCase objects to the RemoteException thrown by auxiliary unit testing thread
  // after resetInteractions kills the slave JVM.
  /** Tests that an infinite loop in a test case can be aborted by clicking the Reset button. 
   * @throws Exception if something goes wrong 
   */
  public void testInfiniteLoop_NOJOIN() throws Exception {
	  testInfiniteLoop_NOJOIN(true);
  }
  
  /** Tests that when a JUnit file with no errors, after being saved and compiled,
   * has it's contents replaced by a test that should fail, will pass all tests.
   * @throws Exception if something goes wrong 
   */
  public void testUnsavedAndUnCompiledChanges() throws Exception {
	  testUnsavedAndUnCompiledChanges(true);
  }
  
  /** Verifies that we get a nonTestCase event and that opening a single test file enables testing. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitAllWithNoValidTests() throws Exception { 
	  safeJUnitAllWithNoValidTests(true);
  }
  
  /** Tests that junit all works with one or two test cases that should pass. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitAllWithNoErrors() throws Exception {
	  safeJUnitAllWithNoErrors(true);
  }
  
  /** Tests that junit all works with test cases that do not pass. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitAllWithErrors() throws Exception {
    
	  safeJUnitAllWithErrors(true);
  } 
  
  /** Tests that junit all works with one or two test cases that should pass. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitStaticInnerClass() throws Exception {
	  safeJUnitStaticInnerClass(true);
  }  
  

  
  /** Tests that when a JUnit file with no errors is compiled and then modified to contain
   * an error does not pass unit testing (by running correct class files).
   * @throws Exception if something goes wrong 
   */
  public void testCorrectFilesAfterIncorrectChanges_NOJOIN() throws Exception {
	  testCorrectFilesAfterIncorrectChanges_NOJOIN(true);
  }
  
  
  /** Tests if a JUnit4 style unit test works. 
    * @throws Exception if something goes wrong 
    */
  public void testJUnit4StyleTestWorks_NOJOIN() throws Exception {
	  testJUnit4StyleTestWorks_NOJOIN(true);
  }
  
  /** Tests to see if a JUnit4 style test with multiple test cases passes. 
   * @throws Exception if something goes wrong 
   */
  public void testJUnit4MultiTest_NOJOIN() throws Exception {
	  testJUnit4MultiTest_NOJOIN(true);
  }
  
  
  /** Tests to see if a JUnit4 style test with no test cases will not run. 
   * @throws Exception if something goes wrong 
   */
  public void testJUnit4NoTest_NOJOIN() throws Exception {
	  testJUnit4NoTest_NOJOIN(true);
  }
  
  /** Tests to see if a JUnit4 style test with a test method and multiple nonTest methods will run. 
   * @throws Exception if something goes wrong 
   */
  public void testJUnit4TwoMethod1Test_NOJOIN() throws Exception {
	  testJUnit4TwoMethod1Test_NOJOIN(true);
  }
}
