package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.model.compiler.*;

/**
 * A test on the GlobalModel for compilation.
 *
 * @version $Id$
 */
public class GlobalModelCompileTest extends GlobalModelTestCase {
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
  public GlobalModelCompileTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelCompileTest.class);
  }

  /**
   * Overrides {@link TestCase#runBare} to interatively run this
   * test case for each compiler.
   * This method is called once per test method, and it magically
   * invokes the method.
   *
   * This is like {@link edu.rice.cs.drjava.util.MultipleStateTestCase},
   * but I can't use it because I must subclass GlobalModelTestCase.
   */
  public void runBare() throws Throwable {
    CompilerInterface[] compilers = CompilerRegistry.ONLY.getAvailableCompilers();
    try {
      for (int i = 0; i < compilers.length; i++) {
        setUp();
        _model.setActiveCompiler(compilers[i]);
        runTest();
      }
    }
    finally {
      tearDown();
    }
  }

  private String _name() {
    return "compiler=" + _model.getActiveCompiler().getName() + ": ";
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
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());

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
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());

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
    assertEquals(_name() + "Class file exists after failing compile",
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
    assertEquals(_name() + "Class file exists after failing compile",
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
    assertTrue(_name() + "Class file exists after compile?!", !compiled.exists());

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
      assertTrue(_name() + "Class file exists after failed compile", !compiled.exists());
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
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
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

        assertEquals(_name() + "file saved", file, f);
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
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());

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
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
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
  
  protected static class CompileShouldSucceedListener extends TestListener {
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
  protected static class CompileShouldFailListener extends TestListener {
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
