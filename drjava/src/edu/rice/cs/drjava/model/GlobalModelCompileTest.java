package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;

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

  private static final String FOO2_EXTENDS_FOO_TEXT =
    "class Foo2 extends Foo {}";

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
   */
  public void runBare() throws Throwable {
    CompilerInterface[] compilers = CompilerRegistry.ONLY.getAvailableCompilers();
    for (int i = 0; i < compilers.length; i++) {
      setUp();
      _model.setActiveCompiler(compilers[i]);

      try {
        runTest();
      }
      finally {
        tearDown();
      }
    }
  }

  private String _name() {
    return "compiler=" + _model.getActiveCompiler().getName() + ": ";
  }

  /**
   * Tests a normal compile that should work.
   */
  public void testCompileNormal() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    // No listener for save -- assume it works
    doc.saveFile(new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    doc.startCompile();
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(file, "Foo");
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());
  }

  /**
   * Test that one compiled file can depend on the other.
   * We compile Foo and then Foo2 (which extends Foo). This shows
   * that the compiler successfully found Foo2 when compiling Foo.
   */
  public void testCompileClasspathOKDefaultPackage()
    throws BadLocationException, IOException
  {
    // Create/compile foo, assuming it works
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    final File fooFile = new File(_tempDir, "Foo.java");
    doc1.saveFile(new FileSelector(fooFile));
    doc1.startCompile();

    OpenDefinitionsDocument doc2 = setupDocument(FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(_tempDir, "Foo2.java");
    doc2.saveFile(new FileSelector(foo2File));

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    doc2.startCompile();
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(foo2File, "Foo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
  }

  /**
   * Test that one compiled file can depend on the other.
   * We compile a.Foo and then b.Foo2 (which extends Foo). This shows
   * that the compiler successfully found Foo2 when compiling Foo.
   */
  public void testCompileClasspathOKDifferentPackages()
    throws BadLocationException, IOException
  {
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();

    // Create/compile foo, assuming it works
    // foo must be public and in Foo.java!
    OpenDefinitionsDocument doc1 =
      setupDocument("package a;\n" + "public " + FOO_TEXT);
    final File fooFile = new File(aDir, "Foo.java");
    doc1.saveFile(new FileSelector(fooFile));
    doc1.startCompile();

    OpenDefinitionsDocument doc2 =
      setupDocument("package b;\nimport a.Foo;\n" + FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(bDir, "Foo2.java");
    doc2.saveFile(new FileSelector(foo2File));

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    doc2.startCompile();
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(foo2File, "Foo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
  }

  /**
   * Tests a compile that should work that uses a field that contains
   * the text "package" as a component of the name.
   */
  public void testCompileWithPackageAsPartOfFieldName()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AS_PART_OF_FIELD);
    final File file = tempFile();

    // No listener for save -- assume it works
    doc.saveFile(new FileSelector(file));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    doc.startCompile();
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(file, "Foo");
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());
  }

  /**
   * Creates a source file with "package" as a field name and ensures
   * that compile starts but fails due to the invalid field name.
   */
  public void testCompilePackageAsField()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AS_FIELD);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));

    CompileShouldFailListener listener = new CompileShouldFailListener();

    _model.addListener(listener);
    doc.startCompile();
    listener.checkCompileOccurred();

    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(_name(), true);

    File compiled = classForJava(file, "Foo");
    assertEquals(_name() + "Class file exists after failing compile",
                 false,
                 compiled.exists());
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
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AS_FIELD_2);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));

    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    doc.startCompile();
    listener.checkCompileOccurred();

    // There better be an error since "package" can not be an identifier!
    assertCompileErrorsPresent(_name(), true);

    File compiled = classForJava(file, "Foo");
    assertEquals(_name() + "Class file exists after failing compile",
                 false,
                 compiled.exists());
  }

  /**
   * Tests compiling an invalid file and checks to make sure the class
   * file was not created.
   */
  public void testCompileMissingCloseSquiggly()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_MISSING_CLOSE_TEXT);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    doc.startCompile();
    assertCompileErrorsPresent(_name(), true);
    listener.checkCompileOccurred();

    File compiled = classForJava(file, "Foo");
    assertTrue(_name() + "Class file exists after compile?!", !compiled.exists());
  }

  /**
   * Puts an otherwise valid package statement inside a class declaration.
   * This better not work!
   */
  public void testCompileWithPackageStatementInsideClass()
    throws BadLocationException, IOException
  {
    // Create temp file
    File baseTempDir = tempDirectory();
    File subdir = new File(baseTempDir, "a");
    File fooFile = new File(subdir, "Foo.java");
    File compiled = classForJava(fooFile, "Foo");

    // Now make subdirectory a
    subdir.mkdir();

    // Save the footext to Foo.java in the subdirectory
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_INSIDE_CLASS);
    doc.saveFileAs(new FileSelector(fooFile));

    // do compile -- should fail since package decl is not valid!
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    doc.startCompile();

    listener.checkCompileOccurred();
    assertCompileErrorsPresent(_name(), true);
    assertTrue(_name() + "Class file exists after failed compile",
               !compiled.exists());
  }

  /**
   * If we try to compile an unsaved file but we do save it from within
   * saveBeforeProceeding, the compile should occur happily.
   */
  public void testCompileUnsavedButSaveWhenAsked()
    throws BadLocationException, IOException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener() {
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
        assertModified(true, doc);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInteractionsResetCount(0);
        assertConsoleResetCount(0);

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
        assertInteractionsResetCount(0);
        assertConsoleResetCount(0);

        File f = null;
        try {
          f = doc.getFile();
        }
        catch (IllegalStateException ise) {
          // We know file should exist
          throw new UnexpectedException(ise);
        }
        assertEquals(_name() + "file saved", file, f);
        saveCount++;
      }
    };

    _model.addListener(listener);
    doc.startCompile();

    // Check events fired
    listener.assertSaveBeforeProceedingCount(1);
    listener.assertSaveCount(1);
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(file, "Foo");
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());
  }

  /**
   * If we try to compile an unsaved file, and if we don't save when
   * asked to saveBeforeProceeding, it should not do the compile
   * or any other actions.
   */
  public void testCompileAbortsIfUnsaved() throws BadLocationException {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertModified(true, doc);
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
        saveBeforeProceedingCount++;
        // since we don't actually save the compile should abort
      }
    };

    _model.addListener(listener);
    doc.startCompile();
    listener.assertSaveBeforeProceedingCount(1);
    assertModified(true, doc);
    assertContents(FOO_TEXT, doc);
  }

}
