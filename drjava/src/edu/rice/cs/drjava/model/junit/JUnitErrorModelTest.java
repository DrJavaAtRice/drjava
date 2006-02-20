/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.junit;

import edu.rice.cs.drjava.model.GlobalModelTestCase;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.util.swing.Utilities;

import java.io.File;

/**
 * A test on the GlobalModel for JUnit testing.
 *
 * @version $Id$
 */
public final class JUnitErrorModelTest extends GlobalModelTestCase {

  private JUnitErrorModel _m;
  
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
    "class MyTest extends junit.framework.TestCase {\n"+
    "  void testMyMethod() {\n"+
    "    assertEquals(\"OneString\", \"TwoStrings\");\n"+
    "  }\n"+
    "}\n";

  /** Tests that the errors array contains all encountered failures and error in the right order. */
  public void testErrorsArrayInOrder() throws Exception {
    _m = new JUnitErrorModel(new JUnitError[0], _model, false);
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.java");
    doc.saveFile(new FileSelector(file));

    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) fail("compile failed: " + getCompilerErrorString());
    listener.checkCompileOccurred();
    
    _runJUnit(doc);
    
    listener.assertJUnitStartCount(1);
    // Clear document so we can make sure it's written to after startJUnit
    _model.getJUnitModel().getJUnitDocument().remove
      (0, _model.getJUnitModel().getJUnitDocument().getLength() - 1);
    //final TestResult testResults = doc.startJUnit();

    //_m = new JUnitErrorModel(doc.getDocument(), "MonkeyTestFail", testResults);
    _m = _model.getJUnitModel().getJUnitErrorModel();

    //JUnitError[] errorsWithPositions = _m.getErrorsWithPositions();
    //JUnitError[] errorsWithoutPositions = _m.getErrorsWithoutPositions();
    //assertTrue("testResults should not be null", testResults != null);

    assertEquals("the test results should have one error and one failure "+_m.getNumErrors(), 2, _m.getNumErrors());

    assertEquals("test case has one error reported" + _m.getError(0).message(), _m.getError(0).isWarning(), false);

    assertEquals("test case has one failure reported" + _m.getError(1).message(), _m.getError(1).isWarning(), true);
    //_model.setResetAfterCompile(true);
  }

  /**
   * Tests that a VerifyError is reported as an error, rather than
   * simply causing JUnit to blow up.  Note that this test will hang if
   * the error is not reported correctly, because the JUnitTestManager will
   * blow up in the other JVM and never notify us that it's finished.
   */
  public void testVerifyErrorHandledCorrectly() throws Exception {
    OpenDefinitionsDocument doc = setupDocument(ABC_CLASS_ONE);
    final File file = new File(_tempDir, "ABC1.java");
    doc.saveFile(new FileSelector(file));

    OpenDefinitionsDocument doc2 = setupDocument(ABC_TEST);
    final File file2 = new File(_tempDir, "ABCTest.java");
    doc2.saveFile(new FileSelector(file2));

    // Compile the correct ABC and the test
//    JUnitTestListener listener = new JUnitTestListener(false);
//      System.out.println("compiling all");
    _model.getCompilerModel().compileAll();

    OpenDefinitionsDocument doc3 = setupDocument(ABC_CLASS_TWO);
    final File file3 = new File(_tempDir, "ABC2.java");
    doc3.saveFile(new FileSelector(file3));

    JUnitTestListener listener = new JUnitNonTestListener();
    // Compile the incorrect ABC
//      System.out.println("compiling doc3");
    doc3.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.addListener(listener);
    // Run the test: a VerifyError will be thrown in Java 1.4
//    JUnitTestListener listener2 = new JUnitTestListener();
//    _model.addListener(listener2);

    listener.assertClassFileErrorCount(0);
    _runJUnit(doc2);
    double version = Double.valueOf(System.getProperty("java.specification.version"));
    if (version < 1.5) listener.assertClassFileErrorCount(1);
    else 
      assertEquals("Should report one error", 1, _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    
    _model.removeListener(listener);
  }


  /** Tests that an elementary level file has the previous line of the actual error reported as the line of its error.
   *  Necessitated by the added code in the .java file associated with the .dj0 file (the import statement added by the
   *  language level compiler)
   */

  public void testLanguageLevelJUnitErrorLine() throws Exception {
    
    _m = new JUnitErrorModel(new JUnitError[0], _model, false);
    OpenDefinitionsDocument doc = setupDocument(LANGUAGE_LEVEL_TEST);
    final File file = new File(_tempDir, "MyTest.dj0");
    doc.saveFile(new FileSelector(file));

    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    Utilities.clearEventQueue();
    doc.startCompile();
    if (_model.getCompilerModel().getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    listener.checkCompileOccurred();
    
    _runJUnit(doc);
    
    listener.assertJUnitStartCount(1);

    // Clear document so we can make sure it's written to after startJUnit
    _model.getJUnitModel().getJUnitDocument().remove(0, _model.getJUnitModel().getJUnitDocument().getLength() - 1);

    _m = _model.getJUnitModel().getJUnitErrorModel();

    assertEquals("the test results should have one failure "+_m.getNumErrors(), 1, _m.getNumErrors());

    assertEquals("the error line should be line number 2", 2, _m.getError(0).lineNumber());
    
  }


  /** Test errors that occur in superclass. */
  public void testErrorInSuperClass() throws Exception {
    OpenDefinitionsDocument doc1 = setupDocument(TEST_ONE);
    OpenDefinitionsDocument doc2 = setupDocument(TEST_TWO);
    final File file1 = new File(_tempDir, "TestOne.java");
    final File file2 = new File(_tempDir, "TestTwo.java");
    doc1.saveFile(new FileSelector(file1));
    doc2.saveFile(new FileSelector(file2));
    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    _model.getCompilerModel().compileAll();
//        doc1.startCompile();
//        doc2.startCompile();
    
    
    _runJUnit(doc1);
    
    listener.assertJUnitStartCount(1);
    
    _m = _model.getJUnitModel().getJUnitErrorModel();
    
    assertEquals("test case has one error reported", 3, _m.getNumErrors());
    assertTrue("first error should be an error not a warning", !_m.getError(0).isWarning());

    assertTrue("it's a junit error", _m.getError(0) instanceof JUnitError);

    assertEquals("The first error is on line 5", 3, _m.getError(0).lineNumber());
    assertEquals("The first error is on line 5", 19, _m.getError(1).lineNumber());
    assertEquals("The first error is on line 5", 22, _m.getError(2).lineNumber());
    
    _runJUnit(doc2);
    
    listener.assertJUnitStartCount(2);
    
    assertEquals("test case has one error reported", 3, _m.getNumErrors());
    assertTrue("first error should be an error not a warning", !_m.getError(0).isWarning());
    assertEquals("The first error is on line 5", 3, _m.getError(0).lineNumber());
    assertEquals("The first error is on line 5", 19, _m.getError(1).lineNumber());
    assertEquals("The first error is on line 5", 22, _m.getError(2).lineNumber());

    _model.removeListener(listener);
    
  }
}

