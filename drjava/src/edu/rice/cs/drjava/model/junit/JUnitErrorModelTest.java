/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.junit;


import junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.util.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.GlobalModelJUnitTest.JUnitTestListener;

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

  private static final String NONPUBLIC_TEXT =
    "import junit.framework.*; " +
    "public class NonPublic extends TestCase { " +
    "  public NonPublic(String name) { super(name); } " +
    "  void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";

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

  /**
   * Tests that the errors array contains all encountered failures and errors
   * in the right order.
   */
  public void testErrorsArrayInOrder() throws Exception {
    _m = new JUnitErrorModel(new JUnitError[0], _model, false);
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.java");
    doc.saveFile(new FileSelector(file));

    JUnitTestListener listener = new JUnitTestListener();
    _model.addListener(listener);
    doc.startCompile();
    listener.checkCompileOccurred();
    synchronized(listener) {
      doc.startJUnit();
      listener.assertJUnitStartCount(1);
      listener.wait();
    }
    // Clear document so we can make sure it's written to after startJUnit
    _model.getJUnitModel().getJUnitDocument().remove
      (0, _model.getJUnitModel().getJUnitDocument().getLength() - 1);
    //final TestResult testResults = doc.startJUnit();

    //_m = new JUnitErrorModel(doc.getDocument(), "MonkeyTestFail", testResults);
    _m = _model.getJUnitModel().getJUnitErrorModel();

    //JUnitError[] errorsWithPositions = _m.getErrorsWithPositions();
    //JUnitError[] errorsWithoutPositions = _m.getErrorsWithoutPositions();
    //assertTrue("testResults should not be null", testResults != null);

    assertEquals("the test results should have one error and one failure "+_m.getNumErrors(),
                 2,
                  _m.getNumErrors());

    assertEquals("test case has one error reported" + _m.getError(0).message(),
                 _m.getError(0).isWarning(),
                 false
                 );

    assertEquals("test case has one failure reported" + _m.getError(1).message(),
                 _m.getError(1).isWarning(),
                 true
                 );
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
    JUnitTestListener listener = new JUnitTestListener(false);
//      System.out.println("compiling all");
    _model.getCompilerModel().compileAll();

    OpenDefinitionsDocument doc3 = setupDocument(ABC_CLASS_TWO);
    final File file3 = new File(_tempDir, "ABC2.java");
    doc3.saveFile(new FileSelector(file3));

    listener = new JUnitTestListener();
    // Compile the incorrect ABC
//      System.out.println("compiling doc3");
    doc3.startCompile();
    _model.addListener(listener);
    // Run the test: a VerifyError will be thrown.
    JUnitTestListener listener2 = new JUnitTestListener();
    _model.addListener(listener2);
    synchronized(listener2) {
//      System.out.println("starting junit");
      doc2.startJUnit();
      listener2.wait();
    }

    assertEquals("test case has one error reported", 1,
                 _model.getJUnitModel().getJUnitErrorModel().getNumErrors());
    _model.removeListener(listener2);
  }
}

