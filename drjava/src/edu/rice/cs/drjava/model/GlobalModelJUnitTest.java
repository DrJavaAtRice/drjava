/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import junit.framework.*;

import java.awt.EventQueue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

/** A test of Junit testing support in the GlobalModel.
  * @version $Id: GlobalModelJUnitTest.java 5751 2013-02-06 10:32:04Z rcartwright $
  */
public final class GlobalModelJUnitTest extends GlobalModelTestCase {
  
  private static final Log _log = new Log("GlobalModel.txt", false);
  
  /** Whether or not to print debugging output. */
  static final boolean printMessages = false;
  
  /** Delay embedded in testInfiniteLoop_NOJOIN */
  static final int INFINITE_LOOP_DELAY = 30000;

  private static final String ELSPETH_ERROR_TEXT =
    "import java.io._ \n" +
    "import junit.framework._ \n" +
    "import org.junit.Assert._ \n" +
    "class Elspeth extends TestCase { \n" +
    "  def testMe() { \n" +
    "    val s = \"elspeth\" \n" +
    "    assertEquals(\"they match\", s, \"elspeth4\") \n" +
    "  } \n" +
    "}\n";

  private static final String MONKEYTEST_PASS_TEXT =
    "import java.io._ \n" +
    "import junit.framework._ \n" +
    "import org.junit.Assert._ \n" +
    "class MonkeyTestPass extends TestCase { \n" +
    "  def testShouldPass() { \n" +
    "    assertEquals(\"monkey\", \"monkey\") \n" +
    "  } \n" +
    "}\n";
  
  private static final String MONKEYTEST_PASS_ALT_TEXT =
    "import java.io._ \n" +
    "import junit.framework._ \n" +
    "import org.junit.Assert._ \n" +
    "class MonkeyTestPass extends TestCase { \n" +
    "  def testShouldPass() { \n" +
    "    assertEquals(\"monkeys\", \"monkeys\") \n" +
    "  } \n" +
    "}\n";
  
  private static final String MONKEYTEST_FAIL_TEXT =
    "import junit.framework._ \n" +
    "import org.junit.Assert._ \n" +
    "class MonkeyTestFail extends TestCase { \n" +
    "  def testShouldFail() { \n" +
    "    assertEquals(\"monkey\", \"baboon\") " +
    "  } \n" +
    "}\n";

  private static final String MONKEYTEST_ERROR_TEXT =
    "import junit.framework._; \n" +
    "class MonkeyTestError extends TestCase { \n" +
    "  def testThrowsError() { \n" +
    "    throw new Error(\"This is an error.\") \n" +
    "  } \n" +
    "}\n";

  private static final String NON_TESTCASE_TEXT =
    "class NonTestCase {}";


  private static final String MONKEYTEST_INFINITE_TEXT =
    "import junit.framework._ \n" +
    "class MonkeyTestInfinite extends TestCase { \n" +
    "  def testInfinite() { \n" +
    "    while(true) {}\n" +
    "  } \n" +
    "}\n";

  private static final String HAS_MULTIPLE_TESTS_PASS_TEXT =
    "import junit.framework._ \n" +
    "import org.junit.Assert._ \n" +
    "class HasMultipleTestsPass extends TestCase { \n" +
    "  def testShouldPass() { \n" +
    "    assertEquals(\"monkey\", \"monkey\") \n" +
    "  } \n" +
    "  def testShouldAlsoPass() { \n" +
    "    assertTrue(true) \n" +
    "  } \n" +
    "}\n";

  private static final String STATIC_INNER_TEST_TEXT = 
    "import junit.framework._ \n" +
    " class StaticInnerTestCase{ \n" +
    "   object Sadf extends TestCase { \n" +
    "     def testX() { \n" +
    "       assertTrue(\"this is true\", true) \n" +
    "     } \n" +
    "     def testY() { \n" +
    "       assertFalse(\"this is false\", false) \n" +
    "     } \n" +
    "   } \n" +
    "} \n";
  
  private static final String MULTI_CLASSES_IN_FILE_TEXT = 
    "import junit.framework._ \n" + /* last char index: 25 */
    "import org.junit.Assert._ \n" + /* last char index: 58 */
    " class A { } \n" + /* last char index: 72 */
    " class B /* with syntax error */ { def foo(x: Int) { } } \n" + /* 'def' starts at 108 */
    " class DJTest extends TestCase { \n" + 
    "   def testAB() { assertTrue(\"this is true\", true) } \n" +
    " }\n";
  
  /** Tests that a JUnit file with no errors is reported to have no errors. */
  public void testNoJUnitErrors_NOJOIN() throws Exception {
    _log.log("+++Starting testNoJUnitErrors_NOJOIN");
    
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.scala");
    saveFile(doc, new FileSelector(file));
    final JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    _log.log("Invoking compiler on " + doc);
    listener.compile(doc); // synchronously compiles doc
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    assertTrue("class file for file1 exists", new File(_tempDir, "MonkeyTestPass.class").exists());
    
    if (_model.getCompilerModel().getNumErrors() > 0) fail("compile failed: " + getCompilerErrorString());
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertJUnitStartCount(0);
    
    _log.log("NoJUnitErrors_NOJOIN errors: " + _model.getJUnitModel().getJUnitErrorModel());

    /* NOTE: in DrScala the junit testing protocol is slightly different than it is in DrJava.  TODO: determine why
     * the following (commented out) test fails. */
//    listener.assertNonTestCaseCount(0);
    assertEquals("test case should have no errors reported",  0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
    _log.log("+++Completing testNoJUnitErrors_NOJOIN");
  }
  
  /** Tests that a JUnit file with an error is reported to have an error. */
  public void xtestOneJUnitError_NOJOIN() throws Exception {
    _log.log("+++Starting testOneJUnitError_NOJOIN");
    
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.scala");
    _log.log(doc + " set up");
    saveFile(doc, new FileSelector(file));
    _log.log(doc + " saved");
    final JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    _log.log("JUnitTestListener installed");
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.runJUnit(_model.getJUnitModel());
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    assertEquals("test case has one error reported", 1, _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener);
    
    _log.log("+++Completing testOneJUnitError");
  }
  
  /** Tests that a JUnit file with an error is reported to have an error. */
  public void xtestElspethOneJUnitError_NOJOIN() throws Exception {
    _log.log("+++Starting testElspethOneJunitError_NOJOIN");
    
    final OpenDefinitionsDocument doc = setUpDocument(ELSPETH_ERROR_TEXT);
    final File file = new File(_tempDir, "Elspeth.scala");
    saveFile(doc, new FileSelector(file));
    final JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.runJUnit(doc);
    
    final JUnitErrorModel jem = _model.getJUnitModel().getJUnitErrorModel();
    assertEquals("test case has one error reported", 1, jem.getNumErrors());
//    assertTrue("first error should be an error not a warning", !jem.getError(0).isWarning());
    _model.removeListener(listener);
    
    _log.log("+++Completing testElspethOneJUnitError");
  }
  
  /** Tests that a test class which throws a *real* Error (not an Exception) is handled correctly. */
  public void xtestRealError_NOJOIN() throws Exception {
    _log.log("+++Startinging testRealError_NOJOIN");
    
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_ERROR_TEXT);
    final File file = new File(_tempDir, "MonkeyTestError.scala");
    saveFile(doc, new FileSelector(file));
    final JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    assertEquals("test case has one error reported", 1, _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    listener.assertJUnitEndCount(1);
    _model.removeListener(listener);
    
    _log.log("+++Completing testRealError");
  }
  
  /** Tests that the ui is notified to put up an error dialog if JUnit is run on a non-TestCase. */
  public void xtestNonTestCaseError_NOJOIN() throws Exception {
    _log.log("+++Starting testNonTestCaseError_NOJOIN");
    
    final OpenDefinitionsDocument doc = setUpDocument(NON_TESTCASE_TEXT);
    final File file = new File(_tempDir, "NonTestCase.scala");
    saveFile(doc, new FileSelector(file));
    
    final JUnitTestListener listener = new JUnitNonTestListener();
    
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    _log.log("After test");
    
    // Check events fired
    listener.assertJUnitStartCount(0);  // JUnit is never started
    listener.assertJUnitEndCount(1); // JUnit never started but junitEnded() is called when interpreter is replaced
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    _model.removeListener(listener);
    
    _log.log("+++Completing testNonTestCaseError");
  }
  
  /** Tests a document that has no corresponding class file. */
  public void xtestNoClassFile() throws Exception {
    _log.log("+++Starting testNoClassFile");
    
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.scala");
    saveFile(doc, new FileSelector(file));
    _log.log(doc + " saved");
    
    final JUnitTestListener listener = new JUnitCompileBeforeTestListener(); 
    _model.addListener(listener);
    _log.log("Listener " + listener + " added");
    
    _log.log(file + " NOT compiled");
//    Utilities.show("calling _runJunit in testNoClassFile");
    
    listener.runJUnit(doc);
//    Utilities.showDebug("Junit run completed");
    
    _log.log("after test");
    listener.assertCompileBeforeJUnitCount(1);
    listener.assertNonTestCaseCount(0);
    /* Note: the protocol for junit testing is slightly different in DrScala than DrJava.  TODO: determine why the
     * following (commented out) count is 1 rather than 0. */
//    listener.assertJUnitStartCount(0);
    listener.assertJUnitEndCount(1);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(1);
    listener.assertJUnitTestEndedCount(1);
    _model.removeListener(listener);
    _log.log("+++Completing testNoClassFile");
  }
  
  // Commented out because MultiThreadedTestCase objects to the RemoteException thrown by auxiliary unit testing thread
  // after ResetInterpreter kills the slave JVM.
  /** Tests that an infinite loop in a test case can be aborted by clicking the Reset button. */
  public void testInfiniteLoop_NOJOIN() throws Exception {
    _log.log("+++Starting testInfiniteLoop_NOJOIN");
    
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_INFINITE_TEXT);
    _log.log(doc + " set up");
    final File file = new File(_tempDir, "MonkeyTestInfinite.scala");
    _log.log(doc + " saved");
    saveFile(doc, new FileSelector(file));
    
    final JUnitTestListener listener = new JUnitTestListener(false) {
      public void junitSuiteStarted(int numTests) {
        assertEquals("should run 1 test", 1, numTests);
        synchronized(this) { junitSuiteStartedCount++; }
        // kill the infinite test once testSuiteProcessing starts
        _model.resetInterpreter(new File(System.getProperty("user.dir")));
      }
    };
    
    _model.addListener(listener);
    _log.log("Compiling " + doc);
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    if (_model.getCompilerModel().getNumErrors() > 0) fail("compile failed: " + getCompilerErrorString());
    
    listener.logJUnitStart();
    try {  // This loop cannot run in the event thread because it contains an explicit sleep operation
      assert ! EventQueue.isDispatchThread();
      _log.log("Starting JUnit");
      doc.startJUnit();
//      Thread.sleep(INFINITE_LOOP_DELAY);
      _log.log("Waiting for time out or for Junit to terminate");
      listener.waitJUnitDone();
      // this waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
      // auxiliary thread silently swallows the exception and terminates.
    }
    catch (Exception e) { fail("Aborting unit testing runs recovery code in testing thread; no exception is thrown"); }
    
    _log.log("Waiting for reset to occur");
    listener.waitResetDone();  // reset should occur when test suite is started
    
    _log.log("ResetDone");
    
    _log.log("after test");
    listener.assertJUnitStartCount(1);
    _model.removeListener(listener);
    listener.assertJUnitEndCount(1);  // Testing was aborted after junitStarted(); junitEnded called in recovery code
    _log.log("+++Completing testInfiniteLoop");
  }
  
  /** Tests that when a JUnit file with no errors, after being saved and compiled,
    * has it's contents replaced by a test that should fail, will pass all tests.
    */
  public void xtestUnsavedAndUnCompiledChanges() throws Exception {
    _log.log("+++Starting testUnsavedAndUnCompiledChanges");
    
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.scala");
    saveFile(doc, new FileSelector(file));
    
    final List<OpenDefinitionsDocument> docs = _model.getSortedOpenDefinitionsDocuments();
    
    final OpenDefinitionsDocument untitled = docs.get(0);
    
    _log.log("Untitled file is named: " + untitled.getName());
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        untitled.quitFile();
        _model.closeFileWithoutPrompt(untitled);
      }
    });
    
    // set up test listener for compile command; automatically checks that compilation is performed
    final JUnitTestListener listener = new JUnitCompileBeforeTestListener();
    _model.addListener(listener);
    
    testStartCompile(doc);
    listener.waitCompileDone();
    
     _log.log(doc + " compiled");
    
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
    
    final JUnitTestListener listener1 = new JUnitTestListener();
    _model.addListener(listener1);
    
    
    assertEquals("test case should have no errors reported after saving", 0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener1);
    
    _log.log("+++Completing testUnsavedAndUnCompiledChanges");
  }
  
  /** Verifies that we get a nonTestCase event and that opening a single test file enables testing. */
  public void safeJUnitAllWithNoValidTests() throws Exception {
    
    _log.log("+++Starting testJUnitAllWithNoValidTests");
    
    final JUnitNonTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(1);
    listener.assertJUnitSuiteStartedCount(0);
    listener.assertJUnitTestStartedCount(0);
    listener.assertJUnitTestEndedCount(0);
    _model.removeListener(listener);
    
    final JUnitCompileBeforeTestListener listener2 = new JUnitCompileBeforeTestListener();
    _model.addListener(listener2);
    final OpenDefinitionsDocument doc = setUpDocument(NON_TESTCASE_TEXT);
    final File file = new File(_tempDir, "NonTestCase.scala");
    _log.log(file + "created;  canWrite() = " + file.canWrite() + "; exists() = " + file.exists());
    saveFile(doc, new FileSelector(file));
    
    listener2.compile(doc);
    listener2.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener2.resetCompileCounts();
    
    // Opending Test
    final File file2 = new File(_tempDir, "MonkeyTestPass.scala");
    final OpenDefinitionsDocument doc2 = setUpDocument(MONKEYTEST_PASS_TEXT);
    saveFile(doc2, new FileSelector(file2));
    listener2.runJUnit(_model.getJUnitModel());
    
    listener2.assertNonTestCaseCount(0);
    listener2.assertJUnitSuiteStartedCount(1);
    listener2.assertJUnitTestStartedCount(1);
    listener2.assertJUnitTestEndedCount(1);
    _model.removeListener(listener2);
    
    _log.log("+++Completing testJUnitAllWithNoValidTests");
  }
  
  /** Tests that junit all works with one or two test cases that should pass. */
  public void safeJUnitAllWithNoErrors() throws Exception {
    _log.log("+++Starting testJUnitAllWithNoErrors");
        
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.scala");
    saveFile(doc, new FileSelector(file));
    final JUnitTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(1);
    listener.assertJUnitTestEndedCount(1);
    _model.removeListener(listener);
    
    final OpenDefinitionsDocument doc2 = setUpDocument(HAS_MULTIPLE_TESTS_PASS_TEXT);
    final File file2 = new File(_tempDir, "HasMultipleTestsPass.scala");
    saveFile(doc2, new FileSelector(file2));
    
    final JUnitNonTestListener listener2 = new JUnitNonTestListener(true);
    _model.addListener(listener2);
    
    listener2.compile(doc);
    _log.log(file2 + " compiled");
    
    listener2.runJUnit(_model.getJUnitModel());
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener2.assertNonTestCaseCount(0);
    listener2.assertJUnitSuiteStartedCount(1);
    listener2.assertJUnitTestStartedCount(3);
    listener2.assertJUnitTestEndedCount(3);
    _model.removeListener(listener2);
    
    _log.log("+++Completing testJUnitAllWithNoErrors");
  }
  
  /** Tests that junit all works with test cases that do not pass. */
  public void safeJUnitAllWithErrors() throws Exception {
    
    _log.log("+++Starting testJUnitAllWithErrors");
    
    final OpenDefinitionsDocument doc = setUpDocument(MONKEYTEST_ERROR_TEXT);
    final OpenDefinitionsDocument doc2 = setUpDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestError.scala");
    final File file2 = new File(_tempDir, "MonkeyTestFail.scala");
    saveFile(doc, new FileSelector(file));
    saveFile(doc2, new FileSelector(file2));
    final JUnitNonTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.resetCompileCounts();
    listener.compile(doc2);
    listener.checkCompileOccurred();
    _log.log(file2 + " compiled");
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(2);
    listener.assertJUnitTestEndedCount(2);
    _model.removeListener(listener);
    
    final JUnitErrorModel jem = _model.getJUnitModel().getJUnitErrorModel();
    assertEquals("test case has one error reported", 2, jem.getNumErrors());
    
    assertTrue("first error should be an error", jem.getError(0).isWarning());
    assertFalse("second error should be a failure", jem.getError(1).isWarning());
    
    _log.log("+++Completing testJUnitAllWithErrors");
  } 
  
  /** Tests that junit all works with one or two test cases that should pass. */
  public void safeJUnitStaticInnerClass() throws Exception {
    _log.log("+++Starting testJUnitAllWithStaticInnerClass");
    
    final OpenDefinitionsDocument doc = setUpDocument(NON_TESTCASE_TEXT);
    final OpenDefinitionsDocument doc2 = setUpDocument(STATIC_INNER_TEST_TEXT);
    final File file = new File(_tempDir, "NonTestCase.scala");
    final File file2 = new File(_tempDir, "StaticInnerTestCase.scala");
    saveFile(doc, new FileSelector(file));
    saveFile(doc2, new FileSelector(file2));
    
    final JUnitNonTestListener listener = new JUnitNonTestListener(true);
    _model.addListener(listener);
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.resetCompileCounts();
    listener.compile(doc2);
    listener.checkCompileOccurred();
    _log.log(file2 + " compiled");
    
    listener.runJUnit(_model.getJUnitModel());
    
    listener.assertNonTestCaseCount(0);
    listener.assertJUnitSuiteStartedCount(1);
    listener.assertJUnitTestStartedCount(2);
    listener.assertJUnitTestEndedCount(2);
    _model.removeListener(listener);
    
    _log.log("+++Completing testJUnitStaticInnerClass");
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
      /** Assumes that DrScala is in flat file mode! */
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
    */
  public void xtestCorrectFilesAfterIncorrectChanges_NOJOIN() throws Exception {
    _log.log("+++Starting testCorrectFilesAfterIncorrectChanges");
    
    final OpenDefinitionsDocument doc1 = setUpDocument(MULTI_CLASSES_IN_FILE_TEXT);
    _log.log(doc1 + " set up");
    final File file = new File(_tempDir, "DJTest.scala");     
    saveFile(doc1, new FileSelector(file));
    _log.log(doc1 + " saved");
    _log.log("In testCorrectFilesAfterIncorrectChanges_NOJOIN(), DJTest.java = \n" + doc1.getText());
    
    final JUnitNonTestListener listener1 = new JUnitNonTestListener(true);
    _model.addListener(listener1);
    listener1.compile(doc1);
    listener1.checkCompileOccurred();
    assertCompileErrorsPresent(false);

    listener1.runJUnit(_model.getJUnitModel());
    listener1.assertJUnitSuiteStartedCount(0);
    listener1.assertJUnitTestStartedCount(1);
    listener1.assertJUnitTestEndedCount(1);
    listener1.assertNonTestCaseCount(0);
    _model.removeListener(listener1);
    // doc1.remove(87,4);
    doc1.remove(109,6);
    
    final JUnitTestListener listener2 = new JUnitCompileBeforeTestListener();
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
    _log.log("+++Completing testCorrectFilesAfterIncorrectChanges");
  }
  
  /* Tests if a JUnit4 style unit test works. */
  public void xtestJUnit4StyleTestWorks_NOJOIN() throws Exception {
    
    _log.log("+++Starting testJUnit4StyleTestWorks");
    
    final File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4StyleTest.scala");
    assertTrue("testJUnitStyleTest:file0 created", file0.exists());
    _log.log("Test file " + file0 + " exists");
    final OpenDefinitionsDocument doc = setUpDocument((_model._createOpenDefinitionsDocument(file0)).getText());    
    
    final File file = new File(_tempDir, "JUnit4StyleTest.scala");
    saveFile(doc, new FileSelector(file));
    assertTrue("testJUnitStyleTest:file exists", file.exists());
    _log.log("The file " + file + " exists");
    final JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc); // synchronously compiles doc
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertJUnitStartCount(0);  // Why 0?
    
    _log.log("errors: " + _model.getJUnitModel().getJUnitErrorModel());
    
    listener.assertNonTestCaseCount(0);
    assertEquals("test case should have no errors reported",  0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
    _log.log("+++Completing testJUnit4StyleTestWorks");
  }
  
  /** Tests to see if a JUnit4 style test with multiple test cases passes. */
  public void testJUnit4MultiTest_NOJOIN() throws Exception {
    
    _log.log("+++Starting testJUnit4MultiTest");
    
    final File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4MultiTest.scala");
    final OpenDefinitionsDocument doc = setUpDocument((_model._createOpenDefinitionsDocument(file0)).getText());    
    
    final File file = new File(_tempDir, "JUnit4MultiTest.scala");
    saveFile(doc, new FileSelector(file));
    final JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc); // synchronously compiles doc
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertJUnitStartCount(1);
    
    _log.log("errors: " + _model.getJUnitModel().getJUnitErrorModel());
    
    listener.assertNonTestCaseCount(0);
    assertEquals("test case should have no errors reported",  0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
    _log.log("+++Completing testJUnit4SMultiTest");
  }
  
  
  /** Tests to see if a JUnit4 style test with no test cases will not run. */
  public void xtestJUnit4NoTest_NOJOIN() throws Exception {
    _log.log("+++Starting testJUnit4NoTest");
    
    final File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4NoTest.scala");
    final OpenDefinitionsDocument doc = setUpDocument((_model._createOpenDefinitionsDocument(file0)).getText());
    final File file = new File(_tempDir, "JUnit4NoTest.scala");
    saveFile(doc, new FileSelector(file));
    
    final JUnitTestListener listener = new JUnitNonTestListener();
    
    _model.addListener(listener);
    
    listener.compile(doc);
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
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
    
    _log.log("+++Completing testJUnit4NoTest");
  }
  
  /** Tests to see if a JUnit4 style test with a test method and multiple nonTest methods will run. */
  public void testJUnit4TwoMethod1Test_NOJOIN() throws Exception {
    
    _log.log("+++Starting testJUnit4TwoMethod1Test");
    
    final File file0 = new File("testFiles/GlobalModelJUnitTestFiles/JUnit4TwoMethod1Test.scala");
    final OpenDefinitionsDocument doc = setUpDocument((_model._createOpenDefinitionsDocument(file0)).getText());    
    
    final File file = new File(_tempDir, "JUnit4TwoMethod1Test.scala");
    saveFile(doc, new FileSelector(file));
    final JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    
    listener.compile(doc); // synchronously compiles doc
    listener.checkCompileOccurred();
    _log.log(file + " compiled");
    
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertJUnitStartCount(1);
    
    _log.log("errors: " + _model.getJUnitModel().getJUnitErrorModel());
    
    listener.assertNonTestCaseCount(0);
    assertEquals("test case should have no errors reported",  0,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
    _log.log("+++Completing testJUnit4TwoMethod1Test");
  }
}

