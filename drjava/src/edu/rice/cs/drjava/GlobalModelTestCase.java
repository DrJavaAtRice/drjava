package  edu.rice.cs.drjava;

import  junit.framework.*;

import java.io.*;

import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

/**
 * Tests the interface to the GlobalModel.  The test here substitutes as a sort
 * of false UI that runs all the methods in GlobalModel, simulating
 * user input.
 * @version $Id$
 */
public abstract class GlobalModelTestCase extends TestCase {
  protected GlobalModel _model;

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
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelTestCase.class);
  }

  /**
   */
  public void setUp() {
    _model = new GlobalModel();
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

  /**
   * Read the entire contents of a file and return them.
   */
  protected String readFile(File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      char c = (char) reader.read();
      buf.append(c);
    }

    return buf.toString();
  }

  /** Create a new temporary file. */
  protected File tempFile() throws IOException {
    return File.createTempFile("DrJava-test", ".java");
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
    File file = tempFile();
    FileWriter writer = new FileWriter(file);
    writer.write(text);
    writer.close();
    return file;
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
    assertEquals("compile errors > 0?",
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
  class TestListener implements GlobalModelListener {
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

  protected class CompileShouldSucceedListener extends TestListener {
    public void compileStarted() {
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      assertInteractionsResetCount(0);
      assertConsoleResetCount(0);
      compileStartCount++;
    }

    public void compileEnded() {
      assertCompileEndCount(0);
      assertCompileStartCount(1);
      assertInteractionsResetCount(0);
      assertConsoleResetCount(0);
      compileEndCount++;
    }

    public void interactionsReset() {
      assertInteractionsResetCount(0);
      assertCompileStartCount(1);
      assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      interactionsResetCount++;
    }

    public void consoleReset() {
      assertConsoleResetCount(0);
      assertCompileStartCount(1);
      assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      consoleResetCount++;
    }
  }

  /**
   * A model listener for situations expecting a compilation to fail.
   */
  protected class CompileShouldFailListener extends TestListener {
    public void compileStarted() {
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      compileStartCount++;
    }

    public void compileEnded() {
      assertCompileEndCount(0);
      assertCompileStartCount(1);
      compileEndCount++;
    }
  }
}
