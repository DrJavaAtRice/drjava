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

package edu.rice.cs.drjava.model;

import junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;

/**
 * A test on the GlobalModel for JUnit testing.
 *
 * @version $Id$
 */
public class GlobalModelJUnitTest extends GlobalModelTestCase {
  private static final String MONKEYTEST_PASS_TEXT =
    "import junit.framework.*; " + 
    "public class MonkeyTestPass extends TestCase { " +
    "  public MonkeyTestPass(String name) { super(name); } " +
    "  public void testShouldPass() { " +
    "    assertEquals(\"monkey\", \"monkey\"); " +
    "  } " +
    "}";
  
  private static final String MONKEYTEST_FAIL_TEXT =
    "import junit.framework.*; " + 
    "public class MonkeyTestFail extends TestCase { " +
    "  public MonkeyTestFail(String name) { super(name); } " +
    "  public void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";
  
  private static final String MONKEYTEST_COMPILEERROR_TEXT =
    "import junit.framework.*; " + 
    "public class MonkeyTestCompileError extends TestCase { " +
    "  Object MonkeyTestFail(String name) { super(name); } " +
    "  public void testShouldFail() { " +
    "    assertEquals(\"monkey\", \"baboon\"); " +
    "  } " +
    "}";
  
  private static final String NON_TESTCASE_TEXT =
    "public class NonTestCase {}";
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelJUnitTest(String name) {
    super(name);
  }
  
  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelJUnitTest.class);
  }
  
  /**
   * Tests that startJUnit() does not execute if there are compile 
   * errors in the file
   */
  public void testDoNotRunJUnitWhenCompileErrorsOccur() throws Exception {
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_COMPILEERROR_TEXT);
    final File file = new File(_tempDir, "MonkeyTestCompileError.java");
    doc.saveFile(new FileSelector(file));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    
    _model.addListener(listener);
    doc.startJUnit();
    listener.checkCompileOccurred();
    assertCompileErrorsPresent("JUnit", true);
    listener.assertJUnitStartCount(0);
    listener.assertJUnitEndCount(0);
  }
  
  /**
   * Tests that startJUnit() does not execute if the user runs it
   * with an unsaved file and then chooses not to save the file.
   */
  public void testDoNotRunJUnitUnsavedAndDoNotSaveWhenAsked() throws Exception {
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void junitStarted() { junitStartCount++; }
      public void junitEnded() { junitEndCount++; }
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertEquals("should be JUNIT reason to save", JUNIT_REASON, reason);
        assertModified(true, doc);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        
        try {
          doc.saveFile(new CancelingSelector());
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }
        
        saveBeforeProceedingCount++;
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveBeforeProceedingCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        
        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          throw new UnexpectedException(ise);
        }
        assertEquals("JUNIT file saved", file, f);
        saveCount++;
      }
    };
    
    _model.addListener(listener);
    doc.startJUnit();
    
    // Check events fired
    listener.assertSaveBeforeProceedingCount(1);
    listener.assertSaveCount(0);
    listener.assertJUnitStartCount(0);
    listener.assertJUnitEndCount(0);
  }
  
  
  
  
  /**
   * Tests that a JUnit file with no errors is reported to have no errors.
   */
  public void testNoJUnitErrors() throws Exception {
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    // Clear document so we can make sure it's written to after startJUnit
    _model.getJUnitDocument().remove(0, 
                                     _model.getJUnitDocument().getLength() - 1);
    final TestResult testResults = doc.startJUnit();
    
    assertEquals("test case should have no errors reported",
                 0,
                 testResults.failureCount());
    
    assertTrue("junit document should have been written to " +
               "so it's length should be greater than zero",
               _model.getJUnitDocument().getLength() > 0);
  }
  
  /**
   * Tests that a JUnit file with an error is reported to have an error.
   */
  public void testOneJUnitError() throws Exception {
    OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_FAIL_TEXT);
    final File file = new File(_tempDir, "MonkeyTestFail.java");
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    // Clear document so we can make sure it's written to after startJUnit
    _model.getJUnitDocument().remove(0, _model.getJUnitDocument().getLength() - 1);
    final TestResult testResults = doc.startJUnit();
    
    assertEquals("test case has one error reported",
                 1,
                 testResults.failureCount());
    
    assertTrue("junit document should have been written to " +
               "so it's length should be greater than zero",
               _model.getJUnitDocument().getLength() > 0);
  }
  
  /**
   * Tests that startJUnit() executes happily if the user runs
   * it with an unsaved file and then chooses to save the file.
   */
  public void testRunJUnitUnsavedButSaveWhenAsked()
    throws Exception {
    final OpenDefinitionsDocument doc = setupDocument(MONKEYTEST_PASS_TEXT);
    final File file = new File(_tempDir, "MonkeyTestPass.java");
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void junitStarted() { junitStartCount++; }
      public void junitEnded() { junitEndCount++; }
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertEquals("should be JUNIT reason to save", JUNIT_REASON, reason);
        assertModified(true, doc);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        
        try {
          doc.saveFile(new FileSelector(file));
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }
        
        saveBeforeProceedingCount++;
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveBeforeProceedingCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        
        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          throw new UnexpectedException(ise);
        }
        assertEquals("JUNIT file saved", file, f);
        saveCount++;
      }
    };
    
    _model.addListener(listener);
    doc.startJUnit();
    
    // Check events fired
    listener.assertSaveBeforeProceedingCount(1);
    listener.assertSaveCount(1);
    assertCompileErrorsPresent("JUNIT", false);
    listener.checkCompileOccurred();
    listener.assertJUnitStartCount(1);
    listener.assertJUnitEndCount(1);
    
    // Make sure .class exists
    File compiled = classForJava(file, "MonkeyTestPass");
    assertTrue("JUNIT: Class file doesn't exist after compile", compiled.exists());    
  }
 
  /**
   * Tests that the ui is notified to put up an error dialog if JUnit
   * is run on a non-TestCase.
   */
  public void testNonTestCaseError() throws Exception {
    final OpenDefinitionsDocument doc = setupDocument(NON_TESTCASE_TEXT);
    final File file = new File(_tempDir, "NonTestCase.java");
    doc.saveFile(new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void junitStarted() { junitStartCount++; }
      public void junitEnded() { junitEndCount++; }
      public void nonTestCase() { nonTestCaseCount++; }
    };
    
    _model.addListener(listener);
    doc.startJUnit();
    
    // Check events fired
    listener.assertJUnitStartCount(1);
    listener.assertJUnitEndCount(1);
    listener.assertNonTestCaseCount(1);
  }
}
