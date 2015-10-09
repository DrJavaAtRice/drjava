/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.junit;

import edu.rice.cs.drjava.model.GlobalModelTestCase;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.Log;

import java.io.File;
import javax.swing.text.BadLocationException;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A test on the GlobalModel for JUnit testing.
  * @version $Id$
  */
public final class JUnitErrorModelTest extends GlobalModelTestCase {
  
  private volatile JUnitErrorModel _m;
  protected static final Log _log = new Log("JUnitError.txt", false);
  
  private static final String MONKEYTEST_FAIL_TEXT =
    "import junit.framework.*; \n" +
    "import java.io.*; \n" +
    "public class MonkeyTestFail extends TestCase { \n" +
    "  public MonkeyTestFail(String name) { super(name); } \n" +
    "  public void testShouldFail() { \n" +
    "    assertEquals(\"monkey\", \"baboon\"); \n" +
    "  } \n" +
    "  public void testShouldErr() throws Exception { \n" +
    "    throw new IOException(\"Error\"); \n" +
    "  } \n" +
    "}";
  
  private static final String TEST_ONE =
    "import junit.framework.TestCase;\n" +
    "public class TestOne extends TestCase {\n" +
    "  public void testMyMethod() {\n" +
    "    assertTrue(false);\n" +
    "  }\n" +
    "  public TestOne() {\n" +
    "    super();\n" +
    "  }\n" +
    "  public java.lang.String toString() {\n" +
    "    return \"TestOne(\" + \")\";\n" +
    "  }\n" +
    "  public boolean equals(java.lang.Object o) {\n" +
    "    if ((o == null) || getClass() != o.getClass()) return false;\n" +
    "    return true;\n" +
    "  }\n" +
    "  public int hashCode() {\n" +
    "    return getClass().hashCode();\n" +
    "  }\n" +
    "  public void testThrowing() throws Exception{\n" +
    "    throw new Exception(\"here\");\n" +
    "  }\n" +
    "  public void testFail(){\n" +
    "    fail(\"i just failed the test\");\n" +
    "  }\n" +
    "}";
  
  private static final String TEST_TWO =
    "import junit.framework.TestCase;\n" +
    "public class TestTwo extends TestOne {\n" +
    "  public void testTwo() {\n" +
    "    assertTrue(true);\n" +
    "  }\n" +
    "  public TestTwo() {\n" +
    "    super();\n" +
    "  }\n" +
    "  public java.lang.String toString() {\n" +
    "    return \"TestTwo(\" + \")\";\n" +
    "  }\n" +
    "  public boolean equals(java.lang.Object o) {\n" +
    "    if ((o == null) || getClass() != o.getClass()) return false;\n" +
    "    return true;\n" +
    "  }\n" +
    "  public int hashCode() {\n" +
    "    return getClass().hashCode();\n" +
    "  }\n" +
    "}";
  
//  private static final String NONPUBLIC_TEXT =
//    "import junit.framework.*; " +
//    "public class NonPublic extends TestCase { " +
//    "  public NonPublic(String name) { super(name); } " +
//    "  void testShouldFail() { " +
//    "    assertEquals(\"monkey\", \"baboon\"); " +
//    "  } " +
//    "}";
  
  private static final String ABC_CLASS_ONE =
    "class ABC extends java.util.Vector {}\n";
  
  private static final String ABC_CLASS_TWO =
    "class ABC extends java.util.ArrayList {}\n";
  
  private static final String ABC_TEST =
    "public class ABCTest extends junit.framework.TestCase {\n" +
    "  public void testABC() {\n" +
    "    new ABC().get(0);\n" +
    "  }\n" +
    "}";
  
  private static final String LANGUAGE_LEVEL_TEST =
    "class MyTest extends junit.framework.TestCase {\n" + 
    "  void testMyMethod() {\n" + 
    "    assertEquals(\"OneString\", \"TwoStrings\");\n" + 
    "  }\n" + 
    "}\n";
  
//  private void testSaveFile(final OpenDefinitionsDocument doc, final FileSelector fs) {
//    Utilities.invokeAndWait(new Runnable() { 
//      public void run() {
//        try { doc.saveFile(fs); }
//        catch(IOException e) { throw new UnexpectedException(e); }
//      } 
//    });
//  }
    
  /** Tests that the errors array contains all encountered failures and error in the right order. */
  public void testErrorsArrayInOrder_NOJOIN() throws Exception {
    debug.logStart();
    _log.log("testErrorArrayInOrder_NOJOIN started");
    final JUnitTestListener listener = new JUnitTestListener();
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    _log.log("doc setUp");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try {
          _m = new JUnitErrorModel(new JUnitError[0], _model, false);

          final File file = new File(_tempDir, "MonkeyTestFail.java");
          saveFile(doc, new FileSelector(file));
          
          _model.addListener(listener);
          
          testStartCompile(doc);
          _log.log("Compile started");
          
          listener.waitCompileDone();
          _log.log("Compile done");
          
          if (_model.getCompilerModel().getNumErrors() > 0) fail("compile failed: " + getCompilerErrorString());
          listener.checkCompileOccurred();
          _log.log("Done with first block");
        }
        catch(Exception e) { fail("The following exception was thrown in the first block of testErrorsArrayInOrder: /n" + e); }
      }
    });
         
    listener.runJUnit(doc);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
    listener.assertJUnitStartCount(1);
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try {     
          // Clear document so we can make sure it's written to after startJUnit; 
          // ?? When does the clear operation happen?  How is the timing of this clear operation controlled?
          // Performing the clear operation atomically in the event thread.
          
          _model.getJUnitModel().getJUnitDocument().remove(0, _model.getJUnitModel().getJUnitDocument().getLength() - 1);
          assertEquals("Confirm document is empty", 0, _model.getJUnitModel().getJUnitDocument().getLength());
          _log.log("JUnitDocument is empty");
        }
        catch(BadLocationException e) { fail("BadLocationException in clearing JUnitDocument"); }
        catch(Exception e) { fail("The following exception was thrown in testErrorsArrayInOrder: /n" + e); }
      }
    });
    
    // Wait until events triggered by running unit tests have cleared ? (should be done by code above)
//    Utilities.clearEventQueue();
    _log.log("Event queue cleared");
    _m = _model.getJUnitModel().getJUnitErrorModel();
    
    //JUnitError[] errorsWithPositions = _m.getErrorsWithPositions();
    //JUnitError[] errorsWithoutPositions = _m.getErrorsWithoutPositions();
    //assertTrue("testResults should not be null", testResults != null);
    
    assertEquals("the test results should have one error and one failure " + _m.getNumErrors(), 2, _m.getNumErrors());
    
    assertEquals("test case has one error reported" + _m.getError(0).message(), _m.getError(0).isWarning(), false);
    
    assertEquals("test case has one failure reported" + _m.getError(1).message(), _m.getError(1).isWarning(), true);
    //_model.setResetAfterCompile(true);
    
    //final TestResult testResults = doc.startJUnit();
    
    _log.log("testErrorArrayInOrder complete");
    //_m = new JUnitErrorModel(doc.getDocument(), "MonkeyTestFail", testResults);
    debug.logEnd();
  }
  
  /** Tests that a VerifyError is reported as an error, rather than
   * simply causing JUnit to blow up.  Note that this test will hang if
   * the error is not reported correctly, because the JUnitTestManager will
   * blow up in the other JVM and never notify us that it's finished.
   */
  public void testVerifyErrorHandledCorrectly_NOJOIN() throws Exception {
    _log.log("testVerifyErrorHandledCorrectly_NOJOIN started");
    
    final OpenDefinitionsDocument doc = setupDocument(ABC_CLASS_ONE);
    final File file = new File(_tempDir, "ABC1.java");
    final OpenDefinitionsDocument doc2 = setupDocument(ABC_TEST);
    final File file2 = new File(_tempDir, "ABCTest.java");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try {     
          saveFile(doc, new FileSelector(file));
          saveFile(doc2, new FileSelector(file2));
          
          // Compile the correct ABC and the test
//          JUnitTestListener listener = new JUnitTestListener(false);
//          System.out.println("compiling all");
          _model.getCompilerModel().compileAll();
        }
        catch(Exception e) { fail("The following exception was thrown in testVerifyErrorHandledCorrectly_NOJOIN location 1: /n" + e); }
      } 
    });
    
   _log.log("First compile in  testVerifyErrorHandledCorrectly_NOJOIN comlete");
   
    final OpenDefinitionsDocument doc3 = setupDocument(ABC_CLASS_TWO);
    final File file3 = new File(_tempDir, "ABC2.java");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { saveFile(doc3, new FileSelector(file3)); }
    });
    
    final JUnitTestListener listener = new JUnitNonTestListener();
    // Compile the incorrect ABC
//      System.out.println("compiling doc3");
    
    _model.addListener(listener);
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try { listener.compile(doc3); }
        catch(Exception e) { fail("The following exception was thrown in testVerifyErrorHandledCorrectly_NOJOIN location 2: /n" + e); }
      }
    });
    
    _log.log("Second compile complete");
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.resetCounts();
    // Run the test: a VerifyError will be thrown in Java 1.4
//    JUnitTestListener listener2 = new JUnitTestListener();
//    _model.addListener(listener2);
    
    listener.assertClassFileErrorCount(0);
    listener.runJUnit(doc2);
    listener.waitJUnitDone();
    
    _log.log("JUnit execution complete");
    
    double version = Double.valueOf(System.getProperty("java.specification.version"));
    if (version < 1.5) listener.assertClassFileErrorCount(1);
    else 
      assertEquals("Should report one error", 1, _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
    _log.log("testVerifyErrorHandledCorrectly_NOJOIN comlete");
  }
  

// TODO: THIS TEST NEEDS TO BE RE-WRITTEN TO USE THE LINE NUMBER MAP.
//  /** Tests that an elementary level file has the previous line of the actual error reported as the line of its error.
//    * Necessitated by the added code in the .java file associated with the .dj0 file (the import statement added by the
//    * language level compiler)
//    */
//  
//  public void testLanguageLevelJUnitErrorLine() throws Exception {
//    debug.logStart();
//    _m = new JUnitErrorModel(new JUnitError[0], _model, false);
//    final OpenDefinitionsDocument doc = setupDocument(LANGUAGE_LEVEL_TEST);
//    final File file = new File(_tempDir, "MyTest.dj0");
//    saveFile(doc, new FileSelector(file));
//    
//    JUnitTestListener listener = new JUnitTestListener();
//    _model.addListener(listener);
//    
//    
//    testStartCompile(doc);
//    
//    listener.waitCompileDone();
//    
//    if (_model.getCompilerModel().getNumErrors() > 0) fail("compile failed: " + getCompilerErrorString());
//    listener.checkCompileOccurred();
//    
//    listener.runJUnit(doc);
//    
//    listener.assertJUnitStartCount(1);
//    
//// Clear document so we can make sure it's written to after startJUnit
//    _model.getJUnitModel().getJUnitDocument().remove(0, _model.getJUnitModel().getJUnitDocument().getLength() - 1);
//    
//    _m = _model.getJUnitModel().getJUnitErrorModel();
//    
//    assertEquals("the test results should have one failure " + _m.getNumErrors(), 1, _m.getNumErrors());
//    
//    assertEquals("the error line should be line number 2", 2, _m.getError(0).lineNumber());
//    debug.logEnd();
//  }
  
  /** Test errors that occur in superclass. */
  public void testErrorInSuperClass_NOJOIN() throws Exception {
    _log.log("testErrorInSuperClass_NOJOIN started");
    debug.logStart();
    final OpenDefinitionsDocument doc1 = setupDocument(TEST_ONE);
    final OpenDefinitionsDocument doc2 = setupDocument(TEST_TWO);
    final File file1 = new File(_tempDir, "TestOne.java");
    final File file2 = new File(_tempDir, "TestTwo.java");
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          saveFile(doc1, new FileSelector(file1));
          saveFile(doc2, new FileSelector(file2));
        }
        catch(Exception e) { fail("The following exception was thrown in testErrorInSuperClass_NOJOIN location 1: /n" + e); }
      }
    });    
    JUnitTestListener listener = new JUnitTestListener(true);
    _model.addListener(listener);
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try { _model.getCompilerModel().compileAll(); }
        catch(Exception e) { fail("The following exception was thrown in testErrorInSuperClass_NOJOIN location 12: /n" + e); }
      }
    });
//        doc1.startCompile();
//        doc2.startCompile();
    
    listener.waitCompileDone();
    
//    Utilities.clearEventQueue();
    
    _log.log("Testing the first document");
    
    listener.runJUnit(doc1); //  waits until JUnit is done
    
//    Utilities.clearEventQueue();
    
    _log.log("First document test should be complete");
    listener.assertJUnitStartCount(1);  
    
    _m = _model.getJUnitModel().getJUnitErrorModel();
    
    assertEquals("test case has one error reported", 3, _m.getNumErrors());
    assertTrue("first error should be an error not a warning", !_m.getError(0).isWarning());
    
    assertTrue("it's a junit error", _m.getError(0) instanceof JUnitError);
    
    assertEquals("The first error is on line 5", 3, _m.getError(0).lineNumber());
    assertEquals("The first error is on line 5", 19, _m.getError(1).lineNumber());
    assertEquals("The first error is on line 5", 22, _m.getError(2).lineNumber());
    
//    Utilities.clearEventQueue();
//    Utilities.clearEventQueue();
    
    _log.log("Testing the second document");
    listener.resetJUnitCounts();

    listener.runJUnit(doc2);
    // runJUnit waits until the thread started in DefaultJUnitModel._rawJUnitOpenDefDocs has called notify
    
//    Utilities.clearEventQueue();
    _log.log("Second document testing should be complete");
    
    listener.assertJUnitStartCount(1);
    
    assertEquals("test case has one error reported", 3, _m.getNumErrors());
    assertTrue("first error should be an error not a warning", !_m.getError(0).isWarning());
    assertEquals("The first error is on line 5", 3, _m.getError(0).lineNumber());
    assertEquals("The first error is on line 5", 19, _m.getError(1).lineNumber());
    assertEquals("The first error is on line 5", 22, _m.getError(2).lineNumber());
    
    _model.removeListener(listener);
    debug.logEnd();
     _log.log("testErrorInSuperClass_NOJOIN complete");
  }
}

