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

/** A test of Junit testing support in the GlobalModel.
  * @version $Id$
  */
public final class GlobalModelJUnitTest extends GlobalModelTestCase {
  
  private static Log _log = new Log("GlobalModel.txt", false);
  
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
    _log.log("----testNoJUnitErrors-----");
//    Utilities.show("Running testNoJUnitErrors");
    
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    saveFile(doc, new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc); // synchronously compiles doc
    listener.checkCompileOccurred();
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertJUnitStartCount(1);
    
    _log.log("errors: " + _model.getJUnitModel().getJUnitErrorModel());
    
    listener.assertNonTestCaseCount(0);
    assertEquals("test case should have no errors reported",  0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
    _log.log("testNoJUnitErrors completed");
  }
  
  /** Tests that a JUnit file with an error is reported to have an error. 
   * @throws Exception if something goes wrong 
   */
  public void testOneJUnitError_NOJOIN() throws Exception {
    _log.log("----testOneJUnitError-----");
//    Utilities.show("Running testOneJUnitError");
    
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.java");
    saveFile(doc, new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    
    listener.runJUnit(_model.getJUnitModel());
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    assertEquals("test case has one error reported", 1, _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
    
    _log.log("testOneJUnitError completed");
  }
  
  /** Tests that a JUnit file with an error is reported to have an error. 
   * @throws Exception if something goes wrong 
   */
  public void testElspethOneJUnitError_NOJOIN() throws Exception {
    _log.log("----testElspethOneJUnitError-----");
//    Utilities.show("Running testElspethOneJunitError");
    
    OpenDefinitionsDocument doc = setupDocument(ELSPETH_ERROR_TEXT);
    final File file = new File(_tempDir, "Elspeth.java");
    saveFile(doc, new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    
    listener.runJUnit(doc);
    
    JUnitErrorModel junitErrorModel = _model.getJUnitModel().getJUnitErrorModel();
    assertEquals("test case has one error reported", 1, junitErrorModel.getNumErrors());
    assertTrue("first error should be an error not a warning", !junitErrorModel.getError(0).isWarning());
    _model.removeListener(listener);
    
    _log.log("testElspethOneJUnitError completed");
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
    _log.log("+++Starting testNonTestCaseError");
//    Utilities.show("Running testNonTestCaseError");
    
    final OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    final File file = new File(_tempDir, "NonTestCase.java");
    saveFile(doc, new FileSelector(file));
    
    JUnitTestListener listener = new JUnitNonTestListener();
    
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    _log.log("after test");
    
    // Check events fired
    listener.assertJUnitStartCount(0);  // JUnit is never started
    listener.assertJUnitEndCount(0);    // JUnit never started and hence never ended
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    _model.removeListener(listener);
    
    _log.log("testNonTestCaseError completed");
  }
  
  /** Tests that the UI is notified to put up an error dialog if JUnit is run on a non-public TestCase. 
    * @throws Exception if something goes wrong 
    */
  public void testResultOfNonPublicTestCase_NOJOIN() throws Exception {
    _log.log("----testResultOfNonPublicTestCase-----");
//    Utilities.show("Running testResultOfNonPublicTestCase");
    
    final OpenDefinitionsDocument doc = setupDocument(NONPUBLIC_TEXT);
    final File file = new File(_tempDir, "NonPublic.java");
    saveFile(doc, new FileSelector(file));
    
    JUnitTestListener listener = new JUnitTestListener();
    
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    _log.log("After test");
    
    // Check events fired
    listener.assertJUnitStartCount(1);
    listener.assertJUnitEndCount(1);
    
    assertEquals("test case has one error reported", 1, _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
    
    _log.log("testResultOfNonPublicTestCase completed");
  }
  
  /* This test has become inconsistent with DrJava behavior.  If a document's file no longer exists and no class file
   * exists, DrJava will detect that there is no valid class file for the document and ask the user to compile the
   * file
   */
//  public void testDoNotRunJUnitIfFileHasBeenMoved() throws Exception {
//    if (printMessages) System.err.println("----testDoNotRunJUnitIfFileHasBeenMoved-----");
////    Utilities.show("Running testDoNotRunJUnitIfFileHasBeenMoved");
//    
//    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
//    final File file = new File(_tempDir, "MonkeyTestPass.java");
//    doc.saveFile(new FileSelector(file));
//
//    JUnitTestListener listener = new JUnitTestListener();
//
//    _model.addListener(listener);
//    file.delete();
//
//    listener.runJUnit(doc);
//
//    listener.assertJUnitStartCount(0);
//    listener.assertJUnitTestStartedCount(0);
//
//    _model.removeListener(listener);
//    _log.log("testDoNotRunJUnitIfFileHasBeenMoved completed");
//  }
  
  /** Tests a document that has no corresponding class file. 
    * @throws Exception if something goes wrong 
    */
  public void testNoClassFile() throws Exception {
    _log.log("----testNoClassFile-----");
//    Utilities.show("Running testNoClassFile");
    
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    saveFile(doc, new FileSelector(file));
    
    JUnitTestListener listener = new JUnitCompileBeforeTestListener();
    
    _model.addListener(listener);
    
//    Utilities.show("calling _runJunit in testNoClassFile");
    
    listener.runJUnit(doc);
//    Utilities.showDebug("Junit run completed");
    
    _log.log("after test");
    listener.assertCompileBeforeJUnitCount(1);
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitStartCount(1);
    listener.assertJUnitEndCount(1);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(1);
    listener.assertJUnitTestEndedCount(1);
    _model.removeListener(listener);
    _log.log("testNoClassFile completed");
  }
  
  // Commented out because MultiThreadedTestCase objects to the RemoteException thrown by auxiliary unit testing thread
  // after resetInteractions kills the slave JVM.
  /** Tests that an infinite loop in a test case can be aborted by clicking the Reset button. 
   * @throws Exception if something goes wrong 
   */
  public void testInfiniteLoop_NOJOIN() throws Exception {
    _log.log("----testInfiniteLoop-----");
//    Utilities.show("Running testInfiniteLoop");
    
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_INFINITE_TEXT);
    final File file = new File(_tempDir, "MonkeyTestInfinite.java");
    saveFile(doc, new FileSelector(file));
    
    JUnitTestListener listener = new JUnitTestListener(false) {
      public void junitSuiteStarted(int numTests) {
        assertEquals("should run 1 test", 1, numTests);
        synchronized(this) { junitSuiteStartedCount++; }
        // kill the infinite test once testSuiteProcessing starts
        _model.resetInteractions(new File(System.getProperty("user.dir")));
      }
    };
    
    _model.addListener(listener);
    listener.compile(doc);
    
    _log.log("Compilation of infinite loop completed");
    
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    
    _log.log("CheckCompile completed");
//    _model.removeListener(listener);
//    
//    _model.addListener(listener2);
    
    listener.logJUnitStart();
    try {
      _log.log("Starting JUnit");
      doc.startJUnit();
      listener.waitJUnitDone();
      // this waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
      // auxiliary thread silently swallows the exception and terminates.
    }
    catch (Exception e) { fail("Aborting unit testing runs recovery code in testing thread; no exception is thrown"); }
    
    listener.waitResetDone();  // reset should occur when test suite is started
    
    _log.log("ResetDone");
    
    _log.log("after test");
    listener.assertJUnitStartCount(1);
    _model.removeListener(listener);
    listener.assertJUnitEndCount(1);  // Testing was aborted after junitStarted(); junitEnded called in recovery code
    _log.log("Reached Test End");
    _log.log("testInfiniteLoop completed");
  }
  
  /** Tests that when a JUnit file with no errors, after being saved and compiled,
   * has it's contents replaced by a test that should fail, will pass all tests.
   * @throws Exception if something goes wrong 
   */
  public void testUnsavedAndUnCompiledChanges() throws Exception {
    _log.log("-----testUnsavedAndUnCompiledChanges-----");
    
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    saveFile(doc, new FileSelector(file));
    
    List<OpenDefinitionsDocument> docs = _model.getSortedOpenDefinitionsDocuments();
    
    final OpenDefinitionsDocument untitled = docs.get(0);
    
    _log.log("Untitled file is named: " + untitled.getName());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        untitled.quitFile();
        _model.closeFileWithoutPrompt(untitled);
      }
    });
    
    // set up test listener for compile command; automatically checks that compilation is performed
    JUnitTestListener listener = new JUnitCompileBeforeTestListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    
    _log.log("Ordinary compile completed");
    listener.waitCompileDone();
    
    listener.resetCompileCounts();
    
    changeDocumentText(MONKEYTEST_PASS_ALT_TEXT, doc);
    _log.log("document changed; modifiedSinceSave = " + doc.isModifiedSinceSave());
    
    listener.runJUnit(doc);    
    _log.log("JUnit completed");
    
    /* Unsaved document forces both saveBeforeCompile and compileBeforeTest */
    
    listener.assertSaveBeforeCompileCount(1);
    listener.assertCompileBeforeJUnitCount(1);
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitStartCount(1);
    listener.assertJUnitEndCount(1);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(1);
    listener.assertJUnitTestEndedCount(1);
    
    _log.log("after test");
    _model.removeListener(listener);
    
    assertEquals("test case should have no errors reported after modifying", 0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    saveFile(doc, new FileSelector(file));
    
    listener = new JUnitTestListener();
    _model.addListener(listener);
    
    
    assertEquals("test case should have no errors reported after saving", 0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
    
    _log.log("testUnsavedAndUnCompiledChanges completed");
  }
  
  /** Verifies that we get a nonTestCase event and that opening a single test file enables testing. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitAllWithNoValidTests() throws Exception {
    
    _log.log("-----testJUnitAllWithNoValidTests-----");
    
    JUnitNonTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    _model.removeListener(listener);
    
    JUnitCompileBeforeTestListener listener2 = new JUnitCompileBeforeTestListener();
    _model.addListener(listener2);
    OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    File file = new File(_tempDir, "NonTestCase.java");
    _log.log("-----> file = " + file + " -- canWrite() = " + file.canWrite() + " -- exists() = " + file.exists());
    saveFile(doc, new FileSelector(file));
    
    listener2.compile(doc);
    listener2.checkCompileOccurred();
    
    listener2.resetCompileCounts();
    
    // Opending Test
    File file2 = new File(_tempDir, "MonkeyTestPass.java");
    OpenDefinitionsDocument doc2 = setupDocument(MONKEYTEST_PASS_TEXT);
    saveFile(doc2, new FileSelector(file2));
    listener2.runJUnit(_model.getJUnitModel());
    
    listener2.assertNonTestCaseCount(0);
    listener2.assertJUnitSuiteStartedCount(1);
    listener2.assertJUnitTestStartedCount(1);
    listener2.assertJUnitTestEndedCount(1);
    _model.removeListener(listener2);
    
    _log.log("testJUnitAllWithNoValidTests completed");
  }
  
  /** Tests that junit all works with one or two test cases that should pass. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitAllWithNoErrors() throws Exception {
//    _log.log("Starting testJUnitAllWithNoErrors");
    
//    final OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
//    final File file = new File(_tempDir, "NonTestCase.java");
//    saveFile(doc, new FileSelector(file));
//    
//    JUnitTestListener listener = new JUnitNonTestListener(true);
//    
//    _model.addListener(listener);
//    
//    listener.compile(doc);
//    listener.checkCompileOccurred();
//    
//    _log.log("Compiled first doc");
//    
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    File file = new File(_tempDir, "MonkeyTestPass.java");
    saveFile(doc, new FileSelector(file));
    JUnitTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    listener.compile(doc);
    listener.checkCompileOccurred();
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(1);
    listener.assertJUnitTestEndedCount(1);
    _model.removeListener(listener);
    
    doc = setupDocument(HAS_MULTIPLE_TESTS_PASS_TEXT);
    file = new File(_tempDir, "HasMultipleTestsPass.java");
    saveFile(doc, new FileSelector(file));
    
    listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    
    listener.compile(doc);
    
    listener.runJUnit(_model.getJUnitModel());
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(3);
    listener.assertJUnitTestEndedCount(3);
    _model.removeListener(listener);
    
    _log.log("testJUnitAllWithNoErrors completed");
  }
  
  /** Tests that junit all works with test cases that do not pass. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitAllWithErrors() throws Exception {
    
    _log.log("-----testJUnitAllWithErrors-----");
    
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_ERROR_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(MONKEYTEST_FAIL_TEXT);
    File file = new File(_tempDir, "MonkeyTestError.java");
    File file2 = new File(_tempDir, "MonkeyTestFail.java");
    saveFile(doc, new FileSelector(file));
    saveFile(doc2, new FileSelector(file2));
    JUnitNonTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    listener.compile(doc);
    listener.checkCompileOccurred();
    listener.resetCompileCounts();
    listener.compile(doc2);
    listener.checkCompileOccurred();
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(2);
    listener.assertJUnitTestEndedCount(2);
    _model.removeListener(listener);
    
    JUnitErrorModel junitErrorModel = _model.getJUnitModel().getJUnitErrorModel();
    assertEquals("test case has one error reported", 2, junitErrorModel.getNumErrors());
    
    assertTrue("first error should be an error", junitErrorModel.getError(0).isWarning());
    assertFalse("second error should be a failure", junitErrorModel.getError(1).isWarning());
    
    _log.log("testJUnitAllWithErrors completed");
  } 
  
  /** Tests that junit all works with one or two test cases that should pass. 
   * @throws Exception if something goes wrong 
   */
  public void safeJUnitStaticInnerClass() throws Exception {
    _log.log("-----testJUnitAllWithStaticInnerClass-----");
    
    OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(STATIC_INNER_TEST_TEXT);
    File file = new File(_tempDir, "NonTestCase.java");
    File file2 = new File(_tempDir, "StaticInnerTestCase.java");
    saveFile(doc, new FileSelector(file));
    saveFile(doc2, new FileSelector(file2));
    
    JUnitNonTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    listener.compile(doc);
    listener.checkCompileOccurred();
    listener.resetCompileCounts();
    listener.compile(doc2);
    listener.checkCompileOccurred();
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(2);
    listener.assertJUnitTestEndedCount(2);
    _model.removeListener(listener);
    _log.log("----testJUnitAllWithNoErrors-----"); 
    
    _log.log("testJUnitStaticInnerClass completed");
  }  
  
  /** Tests that testing an uncompiled but correct group of files will first compile and then run test. */
  public class JUnitCompileBeforeTestListener extends JUnitTestListener {
    
    /* Method copied by _mainListener in MainFrame. */
    public void compileBeforeJUnit(final CompilerListener testAfterCompile, List<OpenDefinitionsDocument> outOfSync) {
      _log.log("compileBeforeJUnit called in listener " + this);
      synchronized(this) { compileBeforeJUnitCount++; }
      // Compile all open source files
      _model.getCompilerModel().addListener(testAfterCompile);  // listener removes itself
      _log.log("Calling _compileAll()");
      try { _model.getCompilerModel().compileAll();  /* instead of invoking MainFrame._compileAll() */ }
      catch(IOException e) { fail("Compile step generated IOException"); }
      
      _log.log("Compilation finished");
    }
    
    public void saveBeforeCompile() {
      _log.log("saveBeforeCompile called in " + this);
      synchronized(this) { saveBeforeCompileCount++; }
      /** Assumes that DrJava is in flat file mode! */
      saveAllFiles(_model, new FileSaveSelector() {
        public File getFile() { throw new UnexpectedException ("Test should not ask for save file name"); }
        public boolean warnFileOpen(File f) { return false; }
        public boolean verifyOverwrite(File f) { return true; }
        public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return false; }
        public boolean shouldUpdateDocumentState() { return true; }
      });
    }
    public void fileSaved(OpenDefinitionsDocument doc) { }
  }
  
  /** Tests that when a JUnit file with no errors is compiled and then modified to contain
   * an error does not pass unit testing (by running correct class files).
   * @throws Exception if something goes wrong 
   */
  public void testCorrectFilesAfterIncorrectChanges_NOJOIN() throws Exception {
    _log.log("----testCorrectFilesAfterIncorrectChanges-----");
    
//    OpenDefinitionsDocument doc0 = setupDocument(NON_TESTCASE_TEXT);
//    JUnitNonTestListener listener0 = new JUnitNonTestListener(true);
//    File file = new File(_tempDir, "NonTestCase.java");
//    saveFile(doc0, new FileSelector(file));
//    _model.addListener(listener0);
//    
//    listener0.compile(doc0);
//    listener0.checkCompileOccurred();
//    _model.removeListener(listener0);
//// What is the preceding code segment supposed to test; it has already been done!
        
    final OpenDefinitionsDocument doc1 = setupDocument(MULTI_CLASSES_IN_FILE_TEXT);
    final File file = new File(_tempDir, "DJTest.java");
    saveFile(doc1, new FileSelector(file));
    _log.log("In testCorrectFilesAfterIncorrectChanges, DJTest.java = \n" + doc1.getText());
    
    final JUnitNonTestListener listener1 = new JUnitNonTestListener(true);
    _model.addListener(listener1);
    listener1.compile(doc1);
    listener1.checkCompileOccurred();
    assertCompileErrorsPresent(false);

    listener1.runJUnit(_model.getJUnitModel());
    listener1.assertJUnitSuiteStartedCount(1);
    listener1.assertJUnitTestStartedCount(1);
    listener1.assertJUnitTestEndedCount(1);
    listener1.assertNonTestCaseCount(0);
    _model.removeListener(listener1);
    doc1.remove(87,4);
    
    JUnitTestListener listener2 = new JUnitCompileBeforeTestListener();
    _model.addListener(listener2);
    listener2.runJUnit(doc1);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    _log.log("after test");
    listener2.assertCompileBeforeJUnitCount(1);
    listener2.assertNonTestCaseCount(1);
    listener2.assertJUnitStartCount(0);
    listener2.assertJUnitEndCount(0);
    listener2.assertJUnitSuiteStartedCount(0);
    listener2.assertJUnitTestStartedCount(0);
    listener2.assertJUnitTestEndedCount(0);
    _model.removeListener(listener2);
    _log.log("testCorrectFilesAfterIncorrectChanges completed");
  }
  
  
  /** Tests if a JUnit4 style unit test works. 
    * @throws Exception if something goes wrong 
    */
  public void testJUnit4StyleTestWorks_NOJOIN() throws Exception {
    
    _log.log("----testJUnit4StyleTestWorks-----");
    
    File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4StyleTest.java");
    final OpenDefinitionsDocument doc = setupDocument((_model._createOpenDefinitionsDocument(file0)).getText());    
    
    final File file = new File(_tempDir, "JUnit4StyleTest.java");
    saveFile(doc, new FileSelector(file));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc); // synchronously compiles doc
    listener.checkCompileOccurred();
    
    listener.runJUnit(doc);
    _log.log("errors: " + Arrays.toString(_model.getJUnitModel().getJUnitErrorModel().getErrors()));
    
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertJUnitStartCount(1);
    

    listener.assertNonTestCaseCount(0);
    assertEquals("test case should have no errors reported",  0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
    _log.log("----testJUnit4StyleTestWorks completed");
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
  
  
  /** Tests to see if a JUnit4 style test with no test cases will not run. 
   * @throws Exception if something goes wrong 
   */
  public void testJUnit4NoTest_NOJOIN() throws Exception {
    _log.log("----testJUnit4NoTest-----");
    
    File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4NoTest.java");
    final OpenDefinitionsDocument doc = setupDocument((_model._createOpenDefinitionsDocument(file0)).getText());
    final File file = new File(_tempDir, "JUnit4NoTest.java");
    saveFile(doc, new FileSelector(file));
    
    JUnitTestListener listener = new JUnitNonTestListener();
    
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    _log.log("after test");
    
    // Check events fired
    listener.assertJUnitStartCount(0);  // JUnit is never started
    listener.assertJUnitEndCount(0); // JUnit never started and hence never ended
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    _model.removeListener(listener);
    
    _log.log("testJUnit4NoTest completed");
  }
  
  /** Tests to see if a JUnit4 style test with a test method and multiple nonTest methods will run. 
   * @throws Exception if something goes wrong 
   */
  public void testJUnit4TwoMethod1Test_NOJOIN() throws Exception {
    
    _log.log("----testJUnit4TwoMethod1Test-----");
    
    File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4TwoMethod1Test.java");
    final OpenDefinitionsDocument doc = setupDocument((_model._createOpenDefinitionsDocument(file0)).getText());    
    
    final File file = new File(_tempDir, "JUnit4TwoMethod1Test.java");
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
    _log.log("testJUnit4TwoMethod1Test completed");
  }
}

