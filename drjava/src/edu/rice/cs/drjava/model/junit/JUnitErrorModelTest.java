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

package edu.rice.cs.drjava.model.junit;

import edu.rice.cs.util.*;
import edu.rice.cs.drjava.model.*;

import junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.*;

/**
 * A test on the GlobalModel for JUnit testing.
 *
 * @version $Id$
 */
public class JUnitErrorModelTest extends GlobalModelJUnitTest {

  private JUnitErrorModel _m;
  private String _testString;
  
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
    
    
  /**
   * Constructor.
   * @param  String name
   */
  public JUnitErrorModelTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(JUnitErrorModelTest.class);
  }
  
  public void testSubstring() {
   
    _m = new JUnitErrorModel();
    String expected = _m._substring("a simple test", 0, 2);
    
    assertEquals("_substring returned an invalid String",
                 "a ",
                 expected);
    
  }
  
  /**
   * Tests that the errors array contains all encountered failures and errors in the right order. 
   */
  public void testErrorsArrayInOrder() throws Exception { 
    
    _m = new JUnitErrorModel();
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.java");
    doc.saveFile(new FileSelector(file));
    
    TestShouldSucceedListener listener = new TestShouldSucceedListener();
    _model.addListener(listener);
    synchronized(listener) {
      doc.startCompile();
      listener.wait();
    }
    listener.checkCompileOccurred();
    synchronized(listener) {
      doc.startJUnit();
      listener.assertJUnitStartCount(1);
      listener.wait();
    }
    // Clear document so we can make sure it's written to after startJUnit
    _model.getJUnitDocument().remove(0, _model.getJUnitDocument().getLength() - 1);
    //final TestResult testResults = doc.startJUnit();
    
    //_m = new JUnitErrorModel(doc.getDocument(), "MonkeyTestFail", testResults);
    _m = doc.getJUnitErrorModel();
    
    //JUnitError[] errorsWithPositions = _m.getErrorsWithPositions();
    //JUnitError[] errorsWithoutPositions = _m.getErrorsWithoutPositions();
    //assertTrue("testResults should not be null", testResults != null);
    
    assertEquals("the test results should have one error and one failure "+_m.getErrorsWithPositions().length+" "+_m.getErrorsWithoutPositions().length,
                 2,
                  _m.getErrorsWithPositions().length);
    
    assertEquals("test case has one error reported" + _m.getErrorsWithPositions()[0].message(),
                 _m.getErrorsWithPositions()[0].isWarning(),
                 false
                 );

    assertEquals("test case has one failure reported" + _m.getErrorsWithPositions()[1].message(),
                 _m.getErrorsWithPositions()[1].isWarning(),
                 true
                 );
    //_model.setResetAfterCompile(true);
    
  }
  
  /**
   * Tests that the JUnitErrorModel contained within any other open documents is cleared
   * when JUnit is run on a given document.
   */
  public void testClearOthersWhenJUnitRun() throws Exception {

    // Temporarily disabled?  Why?
    
    
    OpenDefinitionsDocument doc1 = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file1 = new File(_tempDir, "MonkeyTestFail.java");
    doc1.saveFile(new FileSelector(file1));
    
    TestShouldSucceedListener listener = new TestShouldSucceedListener();
    _model.addListener(listener);
    synchronized(listener) {
      doc1.startCompile();
      listener.wait();
    }
    listener.checkCompileOccurred();
    synchronized(listener) {
      doc1.startJUnit();
      listener.assertJUnitStartCount(1);
      listener.wait();
    }
    //final TestResult tr1 = doc1.startJUnit();
    JUnitErrorModel m1before = doc1.getJUnitErrorModel();
    _model.removeListener(listener);
    OpenDefinitionsDocument doc2 = setupDocument(NONPUBLIC_TEXT);
    final File file2 = new File(_tempDir, "NonPublic.java");
    doc2.saveFile(new FileSelector(file2));
    //final TestResult tr2 = doc2.startJUnit();
    TestShouldSucceedListener listener2 = new TestShouldSucceedListener();
    _model.addListener(listener2);
    
    synchronized(listener2) {
      doc2.startCompile();
      listener2.wait();
    }
    
    listener2.checkCompileOccurred();
    synchronized(listener2) {
      doc2.startJUnit();
      listener2.assertJUnitStartCount(1);
      listener2.wait();
    }
    JUnitErrorModel m1after = doc1.getJUnitErrorModel();
    JUnitErrorModel m2 = doc2.getJUnitErrorModel();
    
    assertEquals("test case has errors",
                 2,
                 m1before.getNumErrors()
                 );

    assertEquals("test case has errors",
                 1,
                 m2.getNumErrors()
                 );
    
    assertEquals("test case has errors",
                 0,
                 m1after.getNumErrors()
                 );
    _model.removeListener(listener2);
    
  }
  
}

