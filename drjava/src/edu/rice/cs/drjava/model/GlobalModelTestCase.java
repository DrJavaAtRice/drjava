package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.util.FileOps;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;

/**
 * Base class for tests over the {@link GlobalModel}.
 *
 * This class provides a number of convenience methods for testing the
 * GlobalModel. It also contains a model instance (reset in {@link #setUp}
 * and a temporary directory that's created per test invocation (and
 * subsequently cleaned in {@link #tearDown}. This reduces the burden
 * for such file management stuff in the test cases themselves.
 *
 * @version $Id$
 */
public abstract class GlobalModelTestCase extends TestCase {
  protected GlobalModel _model;
  protected File _tempDir;

  protected static final String FOO_TEXT = "class Foo {}";
  protected static final String BAR_TEXT = "class Bar {}";
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelTestCase(String name) {
    super(name);
  }

  /**
   * Setup for each test case, which does the following.
   * <OL>
   * <LI>
   *  Creates a new GlobalModel in {@link #_model} for each test case run.
   * </LI>
   * <LI>
   *  Creates a new temporary directory in {@link #_tempDir}.
   * </LI>
   * </OL>
   */
  protected void setUp() throws IOException {
    _model = new GlobalModel();
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
  }

  /**
   * Teardown for each test case, which recursively deletes the
   * temporary directory created in setUp.
   */
  protected void tearDown() throws IOException {
    boolean ret = FileOps.deleteDirectory(_tempDir);
    assertTrue("delete temp directory " + _tempDir, ret);
  }

  /**
   * Clear all old text and insert the given text.
   */
  protected void changeDocumentText(String s) throws BadLocationException {
    Document doc = _model.getDefinitionsDocument();
    doc.remove(0, doc.getLength());
    assertLength(0);
    doc.insertString(0, s, null);
    assertModified(true);
    assertContents(s);
  }

  /** Create a new temporary file in _tempDir. */
  protected File tempFile() throws IOException {
    return File.createTempFile("DrJava-test", ".java", _tempDir);
  }

  /** Create a new temporary directory in _tempDir. */
  protected File tempDirectory() throws IOException {
    return FileOps.createTempDirectory("DrJava-test", _tempDir);
  }

  /**
   * Given a .java file and a class file name,
   * returns the corresponding .class file.
   */
  protected File classForJava(File sourceFile, String className) {
    assertTrue(sourceFile.getName().endsWith(".java"));

    String cname = className + ".class";

    return new File(sourceFile.getParent(), cname);
  }

  /**
   * Creates a new temporary file and writes the given text to it.
   * The File object for the new file is returned.
   */
  protected File writeToNewTempFile(String text) throws IOException {
    File temp = tempFile();
    FileOps.writeToFile(temp, text);
    return temp;
  }


  /**
   * Create a new document (the previous one is assumed to not be modified),
   * makes sure newFile is fired, and then adds some text.
   * When this method is done newCount is reset to 0.
   */
  protected void setupDocument(String text) throws BadLocationException {
    TestListener listener = new TestListener() {
      public void newFileCreated() {
        newCount++;
      }
    };

    _model.addListener(listener);
    _model.newFile();
    listener.assertNewCount(1);
    assertLength(0);
    assertModified(false);

    changeDocumentText(text);
    _model.removeListener(listener);
  }

  /**
   * Puts the given input into the interactions document and then interprets
   * it, returning the result that was put into the interactions document.
   * This assumes the interactions document is in a state with no text
   * after the prompt. To be sure this is the case, you can reset interactions
   * first.
   *
   * @param input text to interpret
   * @return The output from this interpretation, in String form, as it was
   *         printed to the interactions document.
   */
  protected String interpret(String input) throws BadLocationException {
    Document interactionsDoc = _model.getInteractionsDocument();
    interactionsDoc.insertString(interactionsDoc.getLength(), input, null);

    // skip 1 for newline
    final int resultsStartLocation = interactionsDoc.getLength() + 1;
    _model.interpretCurrentInteraction();
    final int resultsEndLocation = interactionsDoc.getLength() -
                                   InteractionsDocument.PROMPT.length();
    final int resultsLen = resultsEndLocation - resultsStartLocation;

    return interactionsDoc.getText(resultsStartLocation, resultsLen);
  }

  protected void assertModified(boolean b) {
    assertEquals("definitionsDocument.isModifiedSinceSave",
                 b,
                 _model.isModifiedSinceSave());
  }


  protected void assertLength(int len) throws BadLocationException {
    assertEquals("document length",
                 len,
                 _model.getDefinitionsDocument().getLength());
  }

  protected void assertContents(String s) throws BadLocationException {
    int len = _model.getDefinitionsDocument().getLength();

    assertEquals("document contents",
                 s,
                 _model.getDefinitionsDocument().getText(0, len));
  }

  protected void assertCompileErrorsPresent(boolean b) {
    CompilerError[] errors = _model.getCompileErrors();

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < errors.length; i++) {
      buf.append("\nerror #" + i + ": " + errors[i]);
    }
    
    assertEquals("compile errors > 0? errors=" + buf,
                 b,
                 errors.length > 0);
  }

  protected class FileSelector implements FileOpenSelector, FileSaveSelector {
    private File _file;

    public FileSelector(File f) {
      _file = f;
    }

    public File getFile() throws OperationCanceledException {
      return _file;
    }
  }

  protected class CancelingSelector implements FileOpenSelector, FileSaveSelector
  {
    public File getFile() throws OperationCanceledException {
      throw new OperationCanceledException();
    }
  }

  /**
   * A security manager to prevent exiting the VM.
   * This allows us to test whether quit() correctly tries to exit without
   * letting the exit actually occur.
   */
  protected class PreventExitSecurityManager extends SecurityManager {
    private int _attempts = 0;

    public int getAttempts() { return _attempts; }

    public void checkPermission(java.security.Permission perm) {
    }

    public void checkExit(int status) {
      _attempts++;
      throw new SecurityException("Can not exit!");
    }
  }
  
  /**
   * A GlobalModelListener for testing.
   * By default it expects no events to be fired. To customize,
   * subclass and override one or more methods.
   */
  protected static class TestListener implements GlobalModelListener {
    protected int newCount;
    protected int openCount;
    protected int saveCount;
    protected int canAbandonCount;
    protected int compileStartCount;
    protected int compileEndCount;
    protected int consoleResetCount;
    protected int interactionsResetCount;
    protected int saveBeforeProceedingCount;
   
    public TestListener() {
      resetCounts();
    }
    
    public void resetCounts() {
      newCount = 0;
      openCount = 0;
      saveCount = 0;
      canAbandonCount = 0;
      compileStartCount = 0;
      compileEndCount = 0;
      consoleResetCount = 0;
      interactionsResetCount = 0;
      saveBeforeProceedingCount = 0;
    }
    
    public void assertAbandonCount(int i) {
      assertEquals("number of times canAbandon fired", i, canAbandonCount);
    }
    
    public void assertNewCount(int i) {
      assertEquals("number of times newFile fired", i, newCount);
    }
    
    public void assertOpenCount(int i) {
      assertEquals("number of times openFile fired", i, openCount);
    }
    
    public void assertSaveCount(int i) {
      assertEquals("number of times openFile fired", i, saveCount);
    }
    
    public void assertCompileStartCount(int i) {
      assertEquals("number of times compileStarted fired", i, compileStartCount);
    }
    
    public void assertCompileEndCount(int i) {
      assertEquals("number of times compileEnded fired", i, compileEndCount);
    }
    
    public void assertInteractionsResetCount(int i) {
      assertEquals("number of times interactionsReset fired",
                   i,
                   interactionsResetCount);
    }
    
    public void assertConsoleResetCount(int i) {
      assertEquals("number of times consoleReset fired",
                   i,
                   consoleResetCount);
    }
    
    public void assertSaveBeforeProceedingCount(int i) {
      assertEquals("number of times saveBeforeProceeding fired",
                   i,
                   saveBeforeProceedingCount);
    }
    public void newFileCreated() {
      fail("newFileCreated fired unexpectedly");
    }
    
    public void fileSaved(File file) {
      fail("fileSaved fired unexpectedly");
    }
    
    public void fileOpened(File file) {
      fail("fileOpened fired unexpectedly");
    }
    
    public void compileStarted() {
      fail("compileStarted fired unexpectedly");
    }
    
    public void compileEnded() {
      fail("compileEnded fired unexpectedly");
    }
    
    public void interactionsReset() {
      fail("interactionsReset fired unexpectedly");
    }
    
    public void consoleReset() {
      fail("consoleReset fired unexpectedly");
    }
    
    public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
      fail("saveBeforeProceeding fired unexpectedly");
    }
    
    public boolean canAbandonFile(File file) {
      fail("canAbandonFile fired unexpectedly");
      
      // this is actually unreachable but the compiler won't believe me. sigh.
      throw new RuntimeException();
    }
  }
}
