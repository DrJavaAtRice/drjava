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
  
  /**
   * Tests that the errors array contains all encountered failures and errors in the right order. 
   */
  public void testErrorsArrayInOrder() throws Exception { 
    
    _m = new JUnitErrorModel(new JUnitError[0], _model, false);
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
    _m = _model.getJUnitErrorModel();
    
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
}

