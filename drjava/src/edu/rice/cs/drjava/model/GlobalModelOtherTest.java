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
 * A test on the GlobalModel that does deals with everything outside of
 * simple file operations, e.g., compile, quit.
 *
 * TODO: Have setUp create a temp directory and tearDown delete it recursively.
 *       This would get rid of junk dealing with creating/deleting directories
 *       from each test.
 *
 * @version $Id$
 */
public class GlobalModelOtherTest extends GlobalModelTestCase {
  private static final String FOO_MISSING_CLOSE_TEXT = 
    "class Foo {";

  private static final String FOO_PACKAGE_AFTER_IMPORT = 
    "import java.util.*;\npackage a;\n" + FOO_TEXT;

  private static final String FOO_PACKAGE_INSIDE_CLASS = 
    "class Foo { package a; }";

  private static final String FOO_PACKAGE_AS_FIELD = 
    "class Foo { int package; }";

  private static final String FOO_PACKAGE_AS_FIELD_2 = 
    "class Foo { int package = 5; }";

  private static final String FOO_PACKAGE_AS_PART_OF_FIELD = 
    "class Foo { int cur_package = 5; }";

  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelOtherTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelOtherTest.class);
  }

  /**
   * Tests a normal compile that should work.
   */
  public void testCompileNormal() throws BadLocationException, IOException {
    setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listener for save -- assume it works
    _model.saveFile(new FileSelector(file));
    TestListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    _model.startCompile();
    assertCompileErrorsPresent(false);
    listener.assertCompileStartCount(1);
    listener.assertCompileEndCount(1);
    listener.assertInteractionsResetCount(1);
    listener.assertConsoleResetCount(1);

    // Make sure .class exists
    File compiled = classForJava(file, "Foo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());

    file.delete();
    compiled.delete();
  }

  /**
   * Tests a compile that should work that uses a field that contains
   * the text "package" as a component of the name.
   */
  public void testCompileWithPackageAsPartOfFieldName()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_PACKAGE_AS_PART_OF_FIELD);
    final File file = tempFile();

    // No listener for save -- assume it works
    _model.saveFile(new FileSelector(file));
    TestListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    _model.startCompile();
    assertCompileErrorsPresent(false);
    listener.assertCompileStartCount(1);
    listener.assertCompileEndCount(1);
    listener.assertInteractionsResetCount(1);
    listener.assertConsoleResetCount(1);

    // Make sure .class exists
    File compiled = classForJava(file, "Foo");
    assertTrue("Class file doesn't exist after compile", compiled.exists());

    file.delete();
    compiled.delete();
  }

  /**
   * Creates a source file with "package" as a field name and ensures
   * that compile starts but fails due to the invalid field name.
   */
  public void testCompilePackageAsField()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_PACKAGE_AS_FIELD);
    final File file = tempFile();
    _model.saveFile(new FileSelector(file));

    CompileShouldFailListener listener = new CompileShouldFailListener();

    _model.addListener(listener);
    _model.startCompile();
    listener.assertCompileStartCount(1);
    listener.assertCompileEndCount(1);

    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(true);

    File compiled = classForJava(file, "Foo");
    assertEquals("Class file exists after failing compile",
                 false,
                 compiled.exists());

    file.delete();
  }
  
  /**
   * Creates a source file with "package" as a field name and ensures
   * that compile starts but fails due to the invalid field name.
   * This is different from {@link #testCompilePackageAsField} as it
   * initializes the field.
   */
  public void testCompilePackageAsField2()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_PACKAGE_AS_FIELD_2);
    final File file = tempFile();
    _model.saveFile(new FileSelector(file));

    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    _model.startCompile();
    listener.assertCompileStartCount(1);
    listener.assertCompileEndCount(1);

    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(true);

    File compiled = classForJava(file, "Foo");
    assertEquals("Class file exists after failing compile",
                 false,
                 compiled.exists());

    file.delete();
  }
  
  /**
   * Tests compiling an invalid file and checks to make sure the class
   * file was not created.
   */
  public void testCompileMissingCloseSquiggly()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_MISSING_CLOSE_TEXT);
    final File file = tempFile();
    _model.saveFile(new FileSelector(file));
    TestListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    _model.startCompile();
    assertCompileErrorsPresent(true);
    listener.assertCompileStartCount(1);
    listener.assertCompileEndCount(1);
    File compiled = classForJava(file, "Foo");
    assertTrue("Class file exists after compile?!", !compiled.exists());

    file.delete();
  }

  /**
   * Puts an otherwise valid package statement inside a class declaration.
   * This better not work!
   */
  public void testCompileWithPackageStatementInsideClass()
    throws BadLocationException, IOException
  {
    // Create temp file
    File baseTempDir = tempFile();
    File subdir = new File(baseTempDir, "a");
    File fooFile = new File(subdir, "Foo.java");
    File compiled = classForJava(fooFile, "Foo");

    try {
      // Delete the file and make a directory of the same name
      baseTempDir.delete();
      baseTempDir.mkdir();

      // Now make subdirectory a
      subdir.mkdir();

      // Save the footext to Foo.java in the subdirectory
      setupDocument(FOO_PACKAGE_INSIDE_CLASS);
      _model.saveFileAs(new FileSelector(fooFile));

      // do compile -- should fail since package decl is not valid!
      CompileShouldFailListener listener = new CompileShouldFailListener();
      _model.addListener(listener);
      _model.startCompile();

      listener.assertCompileStartCount(1);
      listener.assertCompileEndCount(1);
      assertCompileErrorsPresent(true);
      assertTrue("Class file exists after failed compile", !compiled.exists());
    }
    finally {
      // Delete files and then directories
      compiled.delete(); // shouldn't be there, but just in case
      fooFile.delete();
      subdir.delete();
      baseTempDir.delete();
    }
  }
  

 
  /**
   * If we try to compile an unsaved file but we do save it from within
   * saveBeforeProceeding, the compile should occur happily.
   */
  public void testCompileUnsavedButSaveWhenAsked()
    throws BadLocationException, IOException
  {
    setupDocument(FOO_TEXT);
    final File file = tempFile();

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertEquals("save reason", COMPILE_REASON, reason);
        assertModified(true);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInteractionsResetCount(0);
        assertConsoleResetCount(0);

        try {
          _model.saveFile(new FileSelector(file));
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }

        saveBeforeProceedingCount++;
      }

      public void fileSaved(File f) {
        assertModified(false);
        assertSaveBeforeProceedingCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInteractionsResetCount(0);
        assertConsoleResetCount(0);

        assertEquals("file saved", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);
    _model.startCompile();

    // Check events fired
    listener.assertSaveBeforeProceedingCount(1);
    listener.assertSaveCount(1);
    assertCompileErrorsPresent(false);
    listener.assertCompileStartCount(1);
    listener.assertCompileEndCount(1);
    listener.assertInteractionsResetCount(1);
    listener.assertConsoleResetCount(1);

    // Make sure .class exists
    File compiled = classForJava(file, "Foo");
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
    setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertModified(true);
        assertEquals("save reason", COMPILE_REASON, reason);
        saveBeforeProceedingCount++;
        // since we don't actually save the compile should abort
      }
    };

    _model.addListener(listener);
    _model.startCompile();
    listener.assertSaveBeforeProceedingCount(1);
    assertModified(true);
    assertContents(FOO_TEXT);
  }

  /**
   * Exits the program without having written anything to the current document.
   */
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

  /**
   * Exits the program without saving any changes made to the current document.
   * Loses the changes.
   */
  public void testQuitUnsavedDocumentAllowAbandon() {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        canAbandonCount++;
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

  /**
   * Attempts to exit with unsaved changes, but doesn't allow the quit.
   */
  public void testQuitUnsavedDocumentDisallowAbandon()
    throws BadLocationException
  {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    setupDocument(FOO_TEXT);

    // Ensure canAbandonChanges is called
    TestListener listener = new TestListener() {
      public boolean canAbandonFile(File file) {
        canAbandonCount++;
        return false; // no, don't quit on me!
      }
    };

    _model.addListener(listener);

    try {
      _model.quit();
      listener.assertAbandonCount(1);
      assertEquals("number of attempts to quit", 0, manager.getAttempts());
    }
    catch (SecurityException e) {
      fail("Quit succeeded despite canAbandon returning no!");
    }

    System.setSecurityManager(null);
  }

  public void testGetSourceRootDefaultPackage()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp file
    File baseTempDir = tempFile();
    // Delete the file and make a directory of the same name
    baseTempDir.delete();
    baseTempDir.mkdir();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    setupDocument(FOO_TEXT);
    _model.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    assertEquals("source root",
                 subdir,
                 _model.getSourceRoot());

    fooFile.delete();

    // walk back and delete all dirs to the base
    while (! subdir.equals(baseTempDir)) {
      subdir.delete();
      subdir = subdir.getParentFile();
    }

    baseTempDir.delete();
  }

  public void testGetSourceRootPackageThreeDeepValid()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp file
    File baseTempDir = tempFile();
    // Delete the file and make a directory of the same name
    baseTempDir.delete();
    baseTempDir.mkdir();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    setupDocument("package a.b.c;\n" + FOO_TEXT);
    _model.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    assertEquals("source root",
                 baseTempDir,
                 _model.getSourceRoot());

    fooFile.delete();

    // walk back and delete all dirs to the base
    while (! subdir.equals(baseTempDir)) {
      subdir.delete();
      subdir = subdir.getParentFile();
    }

    baseTempDir.delete();
  }

  public void testGetSourceRootPackageThreeDeepInvalid()
    throws BadLocationException, IOException
  {
    // Create temp file
    File baseTempDir = tempFile();
    // Delete the file and make a directory of the same name
    baseTempDir.delete();
    baseTempDir.mkdir();

    // Now make subdirectory a/b/d
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "d");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    setupDocument("package a.b.c;\n" + FOO_TEXT);
    _model.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // The package name is wrong so this should fail.
    try {
      File root = _model.getSourceRoot();
      fail("getSourceRoot() did not fail on invalid package. It returned: " +
           root);
    }
    catch (InvalidPackageException e) {
      // good.
    }

    fooFile.delete();

    // walk back and delete all dirs to the base
    while (! subdir.equals(baseTempDir)) {
      subdir.delete();
      subdir = subdir.getParentFile();
    }

    baseTempDir.delete();
  }

  public void testGetSourceRootPackageOneDeepValid()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp file
    File baseTempDir = tempFile();
    // Delete the file and make a directory of the same name
    baseTempDir.delete();
    baseTempDir.mkdir();

    // Now make subdirectory a
    File subdir = new File(baseTempDir, "a");
    subdir.mkdir();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    setupDocument("package a;\n" + FOO_TEXT);
    _model.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    assertEquals("source root",
                 baseTempDir,
                 _model.getSourceRoot());

    fooFile.delete();
    subdir.delete();
    baseTempDir.delete();
  }

 }
