package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.io.File;
import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;


/**
 * Tests the enabled/disabled status of "Save" and "Compile" buttons.
 * Unfortunately, the tests take forever since they are creating an
 * actual MainFrame.
 * @version $Id$
 */
public class ButtonTest extends TestCase {
  MainFrame _m;

  /**
   * Constructor.
   * @param   String name
   */
  public ButtonTest(String name) {
    super(name);
  }

  /**
   * Sets up a window for testing.
   */
  public void setUp() {
    _m = new MainFrame();
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(ButtonTest.class);
  }

  /**
   * Test if the save button is initially disabled.
   */
  public void testSaveButtonInitiallyDisabled() {
    assertTrue(!_m._saveButton.isEnabled());
  }

  /**
   * Test if the compile button is initially disabled.
   */
  public void testCompileButtonInitiallyDisabled() {
    assertTrue(!_m._compileButton.isEnabled());
  }

  /**
   * Test if the save button becomes usable after modification
   * to the document.
   * @exception BadLocationException
   */
  public void testSaveEnabledAfterModification() throws BadLocationException {
    DefinitionsDocument d = _m.getDefPane()._doc();
    d.insertString(0, "this is a test", null);
    assertTrue(_m._saveButton.isEnabled());
  }

  /**
   * Test to make sure the compile button is disabled
   * after modification to the document.
   * @exception BadLocationException
   */
  public void testCompileDisabledAfterModification() throws BadLocationException {
    DefinitionsDocument d = _m.getDefPane()._doc();
    d.insertString(0, "this is a test", null);
    assertTrue(!_m._compileButton.isEnabled());
  }

  /**
   * Test to make sure the compile button is enabled
   * after the document is saved.
   * @exception BadLocationException
   */
  public void testCompileEnabledAfterSave() throws BadLocationException {
    DefinitionsPane v = _m.getDefPane();
    DefinitionsDocument d = v._doc();
    d.insertString(0, "this is a test", null);
    assertTrue(_m.saveToFile("button-test-file"));
    assertTrue(_m._compileButton.isEnabled());
    assertTrue(new File("button-test-file").delete());
  }

  /**
   * Test to make sure the save button is disabled
   * immediately after a document save.  It should only be enabled when
   * there have been unsaved modifications made to the document.
   * @exception BadLocationException
   */
  public void testSaveDisabledAfterSave() throws BadLocationException {
    DefinitionsPane v = _m.getDefPane();
    DefinitionsDocument d = v._doc();
    d.insertString(0, "this is a test", null);
    assertTrue(_m.saveToFile("button-test-file"));
    assertTrue(!_m._saveButton.isEnabled());
    assertTrue(new File("button-test-file").delete());
  }

  /**
   * Test to make sure the compile button is disabled after the compiler is run
   * on the current source code.  It will be re-enabled after the document has
   * been modified and saved.
   * @exception BadLocationException
   */
  public void testCompileDisabledAfterCompile() throws BadLocationException {
    DefinitionsPane v = _m.getDefPane();
    DefinitionsDocument d = v._doc();
    d.insertString(0, "public class C{}", null);
    assertTrue(_m.saveToFile("C.java"));
    try {
      _m.compile();
    } 
    catch (NullPointerException ex) {
    // A compilation will cause messages to be written to
    // the compile-errors window, which doesn't exist in a
    // barebones MainFrame.
    }
    assertTrue(!_m._compileButton.isEnabled());
    new File("C.java").delete();
    new File("C.class").delete();
  }
}