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
 * @version $Id$
 */
public class GlobalModelTest extends TestCase {
  private GlobalModel _model;

  private int _newCount;
  private int _openCount;
  private int _saveCount;
  private int _canAbandonCount;
  private int _compileStartCount;
  private int _compileEndCount;
  private int _consoleResetCount;
  private int _interactionsResetCount;
  private int _saveBeforeProceedingCount;

  private static final String FOO_TEXT = "class Foo {}";
  private static final String BAR_TEXT = "class Bar {}";
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelTest(String name) {
    super(name);
  }

  /**
   */
  public void setUp() {
    _model = new GlobalModel();
    _newCount = 0;
    _openCount = 0;
    _saveCount = 0;
    _canAbandonCount = 0;
    _compileStartCount = 0;
    _compileEndCount = 0;
    _consoleResetCount = 0;
    _interactionsResetCount = 0;
    _saveBeforeProceedingCount = 0;
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelTest.class);
  }

  public void testNewFileAllowAbandon() throws BadLocationException {
    _setupDocument(FOO_TEXT);

    // Now try to "new" again and check for proper events
    TestListener listener = new TestListener() {
      public void newFileCreated() {
        _assertAbandonCount(1);
        _newCount++;
      }

      public boolean canAbandonFile(File file) {
        _canAbandonCount++;
        return true; // yes allow the abandon
      }
    };

    _model.addListener(listener);
    _model.newFile();
    _assertNewCount(1);
    _assertModified(false);
    _assertLength(0);
  }

  public void testNewFileDisallowAbandon() throws BadLocationException {
    _setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        _canAbandonCount++;
        return false; // no, don't abandon our document!!!
      }
    };

    _model.addListener(listener);
    _model.newFile();
    _assertAbandonCount(1);
    _assertModified(true);
    _assertContents(FOO_TEXT);
  }

  public void testOpenRealFileAllowAbandon()
    throws BadLocationException, IOException
  {
    final File tempFile = _writeToNewTempFile(BAR_TEXT);

    TestListener listener = new TestListener() {
      public void fileOpened(File file) {
        assertEquals("file to open", tempFile, file);
        _openCount++;
      }
    };

    _model.addListener(listener);
    _model.openFile(new FileSelector(tempFile));
    _assertOpenCount(1);
    _assertModified(false);
    _assertContents(BAR_TEXT);
    tempFile.delete();
  }

  public void testCancelOpenFileAllowAbandon()
    throws BadLocationException, IOException
  {

    _setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        _canAbandonCount++;
        return true; // yes allow the abandon
      }
    };

    _model.addListener(listener);
    _model.openFile(new CancelingSelector());
    _assertModified(true);
    _assertContents(FOO_TEXT);
  }

  public void testOpenNonexistentFile()
    throws BadLocationException, IOException
  {
    _model.addListener(new TestListener());

    try {
      _model.openFile(new FileSelector(new File("fake-file")));
      fail("IO exception was not thrown!");
    }
    catch (FileNotFoundException fnf) {
      // As we hoped, the file was not found
    }

    _assertLength(0);
    _assertModified(false);
  }

  public void testOpenFileDisallowAbandon()
    throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        _canAbandonCount++;
        return false; // no, don't abandon our document!!!
      }
    };

    _model.addListener(listener);
    _model.openFile(new FileSelector(new File("junk-doesnt-exist")));
    _assertAbandonCount(1);
    _assertModified(true);
    _assertContents(FOO_TEXT);
  }

  public void testCancelFirstSave() throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);

    // No need to override methods since no events should be fired
    _model.addListener(new TestListener());

    _model.saveFile(new CancelingSelector());
    _assertModified(true);
    _assertContents(FOO_TEXT);
  }

  public void testRealSaveFirstSave() throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);
    final File file = _tempFile();

    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file, f);
        _saveCount++;
      }
    };

    _model.addListener(listener);

    _model.saveFile(new FileSelector(file));
    _assertSaveCount(1);
    _assertModified(false);
    _assertContents(FOO_TEXT);

    assertEquals("contents of saved file",
                 FOO_TEXT,
                 _readFile(file));

    file.delete();
  }

  public void testSaveAlreadySaved() throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);
    final File file = _tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file));
    _assertModified(false);
    _assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 _readFile(file));

    // Listener to use on future save
    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file, f);
        _saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    _changeDocumentText(BAR_TEXT);

    // Save over top of the previous file
    _model.saveFile(new FileSelector(file));
    _assertSaveCount(1);

    assertEquals("contents of saved file",
                 BAR_TEXT,
                 _readFile(file));

    file.delete();
  }

  /**
   * First we save the document with FOO_TEXT.
   * Then we tell it to save over the old text, but pass in a CancelingSelector
   * to cancel if we are asked for a new file name. This should not happen
   * since the file is already saved.
   */
  public void testCancelSaveAlreadySaved()
    throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);
    final File file = _tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file));
    _assertModified(false);
    _assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 _readFile(file));

    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file, f);
        _saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    _changeDocumentText(BAR_TEXT);

    _model.saveFile(new CancelingSelector());

    // The file should have saved on top of the old text anyhow.
    // The canceling selector should never have been called.
    _assertSaveCount(1);
    _assertModified(false);
    _assertContents(BAR_TEXT);

    assertEquals("contents of saved file",
                 BAR_TEXT,
                 _readFile(file));

    file.delete();
  }

  /**
   * Make sure that saveAs doesn't save if we cancel!
   */
  public void testCancelSaveAsAlreadySaved()
    throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);
    final File file = _tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file));
    _assertModified(false);
    _assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 _readFile(file));

    // No events better be fired!
    _model.addListener(new TestListener());

    // Muck up the document
    _changeDocumentText(BAR_TEXT);

    _model.saveFileAs(new CancelingSelector());

    assertEquals("contents of saved file",
                 FOO_TEXT,
                 _readFile(file));

    file.delete();
  }

  /**
   * Make sure that saveAs saves to a different file.
   */
  public void testSaveAsAlreadySaved()
    throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);
    final File file1 = _tempFile();
    final File file2 = _tempFile();

    // No listeners here -- other tests ensure the first save works
    _model.saveFile(new FileSelector(file1));
    _assertModified(false);
    _assertContents(FOO_TEXT);
    assertEquals("contents of saved file",
                 FOO_TEXT,
                 _readFile(file1));

    // Make sure we save now to the new file name
    TestListener listener = new TestListener() {
      public void fileSaved(File f) {
        assertEquals("saved file name", file2, f);
        _saveCount++;
      }
    };

    _model.addListener(listener);

    // Muck up the document
    _changeDocumentText(BAR_TEXT);

    _model.saveFileAs(new FileSelector(file2));

    assertEquals("contents of saved file1",
                 FOO_TEXT,
                 _readFile(file1));

    assertEquals("contents of saved file2",
                 BAR_TEXT,
                 _readFile(file2));

    file1.delete();
    file2.delete();
  }

  public void testCompileNormal() throws BadLocationException, IOException {
    _setupDocument(FOO_TEXT);
    final File file = _tempFile();

    // No listener for save -- assume it works
    _model.saveFile(new FileSelector(file));

    _model.addListener(new NormalCompileListener());
    _model.startCompile();
    _assertCompileStartCount(1);
    _assertCompileEndCount(1);
    _assertInteractionsResetCount(1);
    _assertConsoleResetCount(1);

    // Make sure .class exists
    File compiled = _classForJava(file, "Foo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());

    file.delete();
    compiled.delete();
  }

  /**
   * If we try to compile an unsaved file but we do save it from within
   * saveBeforeProceeding, the compile should occur happily.
   */
  public void testCompileUnsavedButSaveWhenAsked()
    throws BadLocationException, IOException
  {
    _setupDocument(FOO_TEXT);
    final File file = _tempFile();

    NormalCompileListener listener = new NormalCompileListener() {
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertEquals("save reason", COMPILE_REASON, reason);
        _assertModified(true);
        _assertSaveCount(0);
        _assertCompileStartCount(0);
        _assertCompileEndCount(0);
        _assertInteractionsResetCount(0);
        _assertConsoleResetCount(0);

        try {
          _model.saveFile(new FileSelector(file));
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }

        _saveBeforeProceedingCount++;
      }

      public void fileSaved(File f) {
        _assertModified(false);
        _assertSaveBeforeProceedingCount(0);
        _assertCompileStartCount(0);
        _assertCompileEndCount(0);
        _assertInteractionsResetCount(0);
        _assertConsoleResetCount(0);

        assertEquals("file saved", file, f);
        _saveCount++;
      }
    };

    _model.addListener(listener);
    _model.startCompile();

    // Check events fired
    _assertSaveBeforeProceedingCount(1);
    _assertSaveCount(1);
    _assertCompileStartCount(1);
    _assertCompileEndCount(1);
    _assertInteractionsResetCount(1);
    _assertConsoleResetCount(1);

    // Make sure .class exists
    File compiled = _classForJava(file, "Foo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());

    file.delete();
    compiled.delete();
  }

  /**
   * If we try to compile an unsaved file, and if we don't save when
   * asked to saveBeforeProceeding, it should not do the compile
   * or any other actions.
   */
  public void testCompileAbortsIfUnsaved() throws BadLocationException {
    _setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        _assertModified(true);
        assertEquals("save reason", COMPILE_REASON, reason);
        _saveBeforeProceedingCount++;
        // since we don't actually save the compile should abort
      }
    };

    _model.addListener(listener);
    _model.startCompile();
    _assertSaveBeforeProceedingCount(1);
    _assertModified(true);
    _assertContents(FOO_TEXT);
  }

  public void testQuitEmptyDocument() {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    // Ensure no events get fired
    _model.addListener(new TestListener());

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!");
    }
    catch (SecurityException e) {
      // Good, the security manager saved us from exiting.
      assertEquals("number of attempts to quit", 1, manager.getAttempts());
    }

    System.setSecurityManager(null);
  }

  public void testQuitUnsavedDocumentAllowAbandon() {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        _canAbandonCount++;
        return true; // yes allow the abandon
      }
    };

    _model.addListener(listener);

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!");
    }
    catch (SecurityException e) {
      // Good, the security manager saved us from exiting.
      assertEquals("number of attempts to quit", 1, manager.getAttempts());
    }

    System.setSecurityManager(null);
  }

  public void testQuitUnsavedDocumentDisallowAbandon()
    throws BadLocationException
  {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    _setupDocument(FOO_TEXT);

    // Ensure canAbandonChanges is called
    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        _canAbandonCount++;
        return false; // no, don't quit on me!
      }
    };

    _model.addListener(listener);

    try {
      _model.quit();
      _assertAbandonCount(1);
      assertEquals("number of attempts to quit", 0, manager.getAttempts());
    }
    catch (SecurityException e) {
      fail("Quit succeeded despite canAbandon returning no!");
    }

    System.setSecurityManager(null);
  }

  /**
   * Clear all old text and insert the given text.
   */
  private void _changeDocumentText(String s) throws BadLocationException {
    Document doc = _model.getDefinitionsDocument();
    doc.remove(0, doc.getLength());
    _assertLength(0);
    doc.insertString(0, s, null);
    _assertModified(true);
    _assertContents(s);
  }

  /**
   * Read the entire contents of a file and return them.
   */
  private String _readFile(File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      char c = (char) reader.read();
      buf.append(c);
    }

    return buf.toString();
  }

  /** Create a new temporary file. */
  private File _tempFile() throws IOException {
    return File.createTempFile("DrJava-test", ".java");
  }

  /**
   * Given a .java file and a class file name,
   * returns the corresponding .class file.
   */
  private File _classForJava(File sourceFile, String className) {
    assertTrue(sourceFile.getName().endsWith(".java"));

    String cname = className + ".class";

    return new File(sourceFile.getParent(), cname);
  }

  /**
   * Creates a new temporary file and writes the given text to it.
   * The File object for the new file is returned.
   */
  private File _writeToNewTempFile(String text) throws IOException {
    File file = _tempFile();
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
  private void _setupDocument(String text) throws BadLocationException {
    _newCount = 0;

    TestListener listener = new TestListener() {
      public void newFileCreated() {
        _newCount++;
      }
    };

    _model.addListener(listener);
    _model.newFile();
    _assertNewCount(1);
    _assertLength(0);
    _assertModified(false);

    _changeDocumentText(text);
    _model.removeListener(listener);
    _newCount = 0;
  }

  private void _assertAbandonCount(int i) {
    assertEquals("number of times canAbandon fired", i, _canAbandonCount);
  }

  private void _assertNewCount(int i) {
    assertEquals("number of times newFile fired", i, _newCount);
  }

  private void _assertOpenCount(int i) {
    assertEquals("number of times openFile fired", i, _openCount);
  }

  private void _assertSaveCount(int i) {
    assertEquals("number of times openFile fired", i, _saveCount);
  }

  private void _assertCompileStartCount(int i) {
    assertEquals("number of times compileStarted fired", i, _compileStartCount);
  }

  private void _assertCompileEndCount(int i) {
    assertEquals("number of times compileEnded fired", i, _compileEndCount);
  }

  private void _assertInteractionsResetCount(int i) {
    assertEquals("number of times interactionsReset fired",
                 i,
                 _interactionsResetCount);
  }

  private void _assertConsoleResetCount(int i) {
    assertEquals("number of times consoleReset fired",
                 i,
                 _consoleResetCount);
  }

  private void _assertSaveBeforeProceedingCount(int i) {
    assertEquals("number of times saveBeforeProceeding fired",
                 i,
                 _saveBeforeProceedingCount);
  }

  private void _assertModified(boolean b) {
    assertEquals("definitionsDocument.isModifiedSinceSave",
                 b,
                 _model.isModifiedSinceSave());
  }


  private void _assertLength(int len) throws BadLocationException {
    assertEquals("document length",
                 len,
                 _model.getDefinitionsDocument().getLength());
  }

  private void _assertContents(String s) throws BadLocationException {
    int len = _model.getDefinitionsDocument().getLength();

    assertEquals("document contents",
                 s,
                 _model.getDefinitionsDocument().getText(0, len));
  }

  /**
   * A GlobalModelListener for testing.
   * By default it expects no events to be fired. To customize,
   * subclass and override one or more methods.
   */
  private class TestListener implements GlobalModelListener {
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

  private class NormalCompileListener extends TestListener {
    public void compileStarted() {
      _assertCompileEndCount(0);
      _assertInteractionsResetCount(0);
      _assertConsoleResetCount(0);
      _compileStartCount++;
    }

    public void compileEnded() {
      _assertCompileStartCount(1);
      _assertInteractionsResetCount(0);
      _assertConsoleResetCount(0);
      _compileEndCount++;
    }

    public void interactionsReset() {
      _assertCompileStartCount(1);
      _assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      _interactionsResetCount++;
    }

    public void consoleReset() {
      _assertCompileStartCount(1);
      _assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      _consoleResetCount++;
    }
  }

  private class FileSelector implements FileOpenSelector, FileSaveSelector {
    private File _file;

    public FileSelector(File f) {
      _file = f;
    }

    public File getFile() throws OperationCanceledException {
      return _file;
    }
  }

  private class CancelingSelector implements FileOpenSelector, FileSaveSelector
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
  private class PreventExitSecurityManager extends SecurityManager {
    private int _attempts = 0;

    public int getAttempts() { return _attempts; }

    public void checkPermission(java.security.Permission perm) {
    }

    public void checkExit(int status) {
      _attempts++;
      throw new SecurityException("Can not exit!");
    }
  }
}
