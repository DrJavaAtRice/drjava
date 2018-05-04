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

import edu.rice.cs.drjava.model.GlobalModelTestCase.FileSelector;
import edu.rice.cs.drjava.model.GlobalModelTestCase.JUnitTestListener;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

/** A test of Junit testing support in the GlobalModel.
  * @version $Id$
  */
public final class GlobalModelJUnitTest extends GlobalModelJunitTestCase {
  
  private static Log _log = new Log("GlobalModelJUnitTest.txt", false);
  
  /** Whether or not to print debugging output. */
  static final boolean printMessages = true;
  
  private static final String ELSPETH_ERROR_TEXT = 
    "import junit.framework.TestCase;" +
    "public class Elspeth extends TestCase {" +
    "    public void testMe() {" +
    "        String s = \"elspeth\";" +
    "        assertEquals(\"they match\", s, \"elspeth4\");" +
    "    }" +
    "  public Elspeth() {" +
    "    super();" +
    "  }" +
    "  public java.lang.String toString() {" +
    "    return \"Elspeth(\" + \")\";" +
    "  }" +
    "  public boolean equals(java.lang.Object o) {" +
    "    if ((o == null) || getClass() != o.getClass()) return false;" +
    "    return true;" +
    "  }" +
    "  public int hashCode() {" +
    "    return getClass().hashCode();" +
    "  }" +
    "}";
  
  private static final String MONKEYTEST_PASS_TEXT =
    "import junit.framework.*; \n" +
    "import java.io.*; \n" +
    "public class MonkeyTestPass extends TestCase { \n" +
    "  public MonkeyTestPass(String name) { super(name); } \n" +
    "  public void testShouldPass() { \n" +
    "    assertEquals(\"monkey\", \"monkey\"); \n" +
    "  } \n" +
    "}\n";
  
  private static final String MONKEYTEST_PASS_ALT_TEXT =
    "import junit.framework.*; \n" +
    "import java.io.*; \n" +
    "public class MonkeyTestPass extends TestCase { \n" +
    "  public MonkeyTestPass(String name) { super(name); } \n" +
    "  public void testShouldPass() { \n" +
    "    assertEquals(\"monkeys\", \"monkeys\"); \n" +
    "  } \n" +
    "}\n";
  
  private static final String MONKEYTEST_FAIL_TEXT =
    "import junit.framework.*; " +
    "public class MonkeyTestFail extends TestCase { " +
    "  public MonkeyTestFail(String name) { super(name); } " +
    "  public void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";
  
  private static final String MONKEYTEST_ERROR_TEXT =
    "import junit.framework.*; " +
    "public class MonkeyTestError extends TestCase { " +
    "  public MonkeyTestError(String name) { super(name); } " +
    "  public void testThrowsError() { " +
    "    throw new Error(\"This is an error.\"); " +
    "  } " +
    "}";
  
//  private static final String MONKEYTEST_COMPILEERROR_TEXT =
//    "import junit.framework.*; " +
//    "public class MonkeyTestCompileError extends TestCase { " +
//    "  Object MonkeyTestFail(String name) { super(name); } " +
//    "  public void testShouldFail() { " +
//    "    assertEquals(\"monkey\", \"baboon\"); " +
//    "  } " +
//    "}";
  
  private static final String NONPUBLIC_TEXT =
    "import junit.framework.*; " +
    "class NonPublic extends TestCase { " +
    "  NonPublic(String name) { super(name); } " +
    "  void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";
  
  private static final String NON_TESTCASE_TEXT =
    "public class NonTestCase {}";
  
  private static final String MONKEYTEST_INFINITE_TEXT =
    "import junit.framework.*; " +
    "public class MonkeyTestInfinite extends TestCase { " +
    "  public MonkeyTestInfinite(String name) { super(name); } " +
    "  public void testInfinite() { " +
    "    while(true) {}" +
    "  } " +
    "}";
  
  private static final String HAS_MULTIPLE_TESTS_PASS_TEXT =
    "import junit.framework.*; " +
    "public class HasMultipleTestsPass extends TestCase { " +
    "  public HasMultipleTestsPass(String name) { super(name); } " +
    "  public void testShouldPass() { " +
    "    assertEquals(\"monkey\", \"monkey\"); " +
    "  } " +
    "  public void testShouldAlsoPass() { " +
    "    assertTrue(true); " +
    "  } " +
    "}";
  
  private static final String STATIC_INNER_TEST_TEXT = 
    "import junit.framework.TestCase;" +
    " public class StaticInnerTestCase{" +
    "   public static class Sadf extends TestCase {" +
    "     public Sadf() {" +
    "       super();" +
    "     }" +
    "     public Sadf(String name) {" +
    "       super(name);" +
    "     }" +
    "     public void testX() {" +
    "       assertTrue(\"this is true\", true);" +
    "     }" +
    "     public void testY() {" +
    "       assertFalse(\"this is false\", false);" +
    "     }" +
    "   }" +
    "}";
  
  private static final String MULTI_CLASSES_IN_FILE_TEXT = 
    "import junit.framework.TestCase;" +
    " class A { } " +
    " class B /* with syntax error */ { public void foo(int x) { } } " +
    " public class DJTest extends TestCase { " + 
    "   public void testAB() { assertTrue(\"this is true\", true); } " +
    " }";
  
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
  /** Tests that a test class which throws a *real* Error (not an Exception) is handled correctly. 
   * @throws Exception if something goes wrong 
   */
  public void testRealError_NOJOIN() throws Exception {
    _log.log("----testRealError-----");
//    Utilities.show("Running testRealError");
    
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_ERROR_TEXT);
    final File file = new File(_tempDir, "MonkeyTestError.java");
    saveFile(doc, new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    _model.getJUnitModel().setRunTestParallel(false);
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    assertEquals("test case has one error reported", 1, _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    listener.assertJUnitEndCount(1);
    _model.removeListener(listener);
    
    _log.log("+++Completing testRealError completed");
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
  

  
	/**
	 * Tests a document that has no corresponding class file.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void testNoClassFile() throws Exception {
		testNoClassFile(true);
	}

	// Commented out because MultiThreadedTestCase objects to the RemoteException
	// thrown by auxiliary unit testing thread
	// after resetInteractions kills the slave JVM.
	/**
	 * Tests that an infinite loop in a test case can be aborted by clicking the
	 * Reset button.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void testInfiniteLoop_NOJOIN() throws Exception {
		testInfiniteLoop_NOJOIN(true);
	}

	/**
	 * Tests that when a JUnit file with no errors, after being saved and compiled,
	 * has it's contents replaced by a test that should fail, will pass all tests.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void testUnsavedAndUnCompiledChanges() throws Exception {
		testUnsavedAndUnCompiledChanges(true);
	}

	/**
	 * Verifies that we get a nonTestCase event and that opening a single test file
	 * enables testing.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void safeJUnitAllWithNoValidTests() throws Exception {
		safeJUnitAllWithNoValidTests(true);
	}

	/**
	 * Tests that junit all works with one or two test cases that should pass.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void safeJUnitAllWithNoErrors() throws Exception {
		safeJUnitAllWithNoErrors(true);
	}

	/**
	 * Tests that junit all works with test cases that do not pass.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void safeJUnitAllWithErrors() throws Exception {

		safeJUnitAllWithErrors(true);
	}

	/**
	 * Tests that junit all works with one or two test cases that should pass.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void safeJUnitStaticInnerClass() throws Exception {
		safeJUnitStaticInnerClass(true);
	}

	/**
	 * Tests that when a JUnit file with no errors is compiled and then modified to
	 * contain an error does not pass unit testing (by running correct class files).
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void testCorrectFilesAfterIncorrectChanges_NOJOIN() throws Exception {
		testCorrectFilesAfterIncorrectChanges_NOJOIN(true);
	}

	/**
	 * Tests if a JUnit4 style unit test works.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void testJUnit4StyleTestWorks_NOJOIN() throws Exception {
		testJUnit4StyleTestWorks_NOJOIN(true);
	}

	  
	  /** Tests to see if a JUnit4 style test with multiple test cases passes. 
	   * @throws Exception if something goes wrong 
	   */
	  public void testJUnit4MultiTest_NOJOIN() throws Exception {
	    
	    _log.log("----testJUnit4MultiTest-----");
	    
	    File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4MultiTest.java");
	    final OpenDefinitionsDocument doc = setupDocument((_model._createOpenDefinitionsDocument(file0)).getText());    
	    
	    final File file = new File(_tempDir, "JUnit4MultiTest.java");
	    saveFile(doc, new FileSelector(file));
	    JUnitTestListener listener = new JUnitTestListener();
	    _model.addListener(listener);
	    
	    listener.compile(doc); // synchronously compiles doc
	    listener.checkCompileOccurred();
	    
	    listener.runJUnit(doc);
	    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
	    
	    _log.log("errors: " + Arrays.toString(_model.getJUnitModel().getJUnitErrorModel().getErrors()));
	    
	    listener.assertJUnitStartCount(1);
	    
	    listener.assertNonTestCaseCount(0);
	    assertEquals("test case should have no errors reported",  0,
	                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
	    
	    _model.removeListener(listener);
	    _log.log("testJUnit4SMultiTest completed");
	  }

	/**
	 * Tests to see if a JUnit4 style test with no test cases will not run.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void testJUnit4NoTest_NOJOIN() throws Exception {
		testJUnit4NoTest_NOJOIN(true);
	}

	/**
	 * Tests to see if a JUnit4 style test with a test method and multiple nonTest
	 * methods will run.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void testJUnit4TwoMethod1Test_NOJOIN() throws Exception {
		testJUnit4TwoMethod1Test_NOJOIN(true);
	}
}
