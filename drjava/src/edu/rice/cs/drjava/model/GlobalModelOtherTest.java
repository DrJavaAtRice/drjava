package edu.rice.cs.drjava.model;

import  junit.framework.*;

import java.io.*;

import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;

/**
 * A test on the GlobalModel that does deals with everything outside of
 * simple file operations, e.g., compile, quit.
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
   * Creates a new class, compiles it and then checks that the REPL
   * can see it.
   */
  public void testInteractionsCanSeeCompile()
    throws BadLocationException, IOException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    doc.startCompile();
    listener.checkCompileOccurred();
    assertCompileErrorsPresent(false);
    _model.removeListener(listener);

    String result = interpret("new Foo().getClass().getName()");

    assertEquals("interactions result",
                 "Foo",
                 result);
  }

  /**
   * Checks that updating a class and recompiling it is visible from
   * the REPL.
   */
  public void testInteractionsCanSeeChangedClass()
    throws BadLocationException, IOException
  {
    final String text_before = "class Foo { public int m() { return ";
    final String text_after = "; } }";
    final File file = tempFile();
    final int num_iterations = 5;
    OpenDefinitionsDocument doc;


    for (int i = 0; i < num_iterations; i++) {
      doc = setupDocument(text_before + i + text_after);
      doc.saveFile(new FileSelector(file));

      CompileShouldSucceedListener listener =new CompileShouldSucceedListener();
      _model.addListener(listener);

      doc.startCompile();
      listener.checkCompileOccurred();
      assertCompileErrorsPresent(false);
      _model.removeListener(listener);

      assertEquals("interactions result, i=" + i,
          String.valueOf(i),
          interpret("new Foo().m()"));
    }
  }

  /**
   * Exits the program without opening any documents.
   */
  public void testQuitNoDocuments() {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    assertNumOpenDocs(0);

    // Ensure no events get fired
    _model.addListener(new TestListener());

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!)");
    }
    catch (SecurityException e) {
      // Good, the security manager saved us from exiting.
      assertEquals("number of attempts to quit", 1, manager.getAttempts());
    }

    System.setSecurityManager(null);
  }

  /**
   * Exits the program without having written anything to the open documents.
   */
  public void testQuitEmptyDocuments() {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    _model.newFile();
    _model.newFile();

    assertNumOpenDocs(2);

    // Check for proper events
    TestListener listener = new TestListener() {
      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
    };
    _model.addListener(listener);

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!)");
    }
    catch (SecurityException e) {
      // Good, the security manager saved us from exiting.
      assertEquals("number of attempts to quit", 1, manager.getAttempts());
      listener.assertCloseCount(2);
    }

    System.setSecurityManager(null);
  }


  /**
   * Exits the program without saving any changes made to the current document.
   * Loses the changes.
   */
  public void testQuitUnsavedDocumentsAllowAbandon()
    throws BadLocationException
  {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    TestListener listener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return true; // yes allow the abandon
      }

      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
    };

    _model.newFile();
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);

    assertNumOpenDocs(3);

    _model.addListener(listener);

    try {
      _model.quit();
      fail("Got past quit without security exception (and without quitting!)");
    }
    catch (SecurityException e) {
      // Good, the security manager saved us from exiting.
      assertEquals("number of attempts to quit", 1, manager.getAttempts());

      // Only the changed files should prompt an event
      listener.assertAbandonCount(2);
      listener.assertCloseCount(3);
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

    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    assertNumOpenDocs(1);

    // Ensure canAbandonChanges is called
    TestListener listener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
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

  /**
   * Attempts to exit with unsaved changes, but doesn't allow the quit.
   */
  public void testQuitMultipleDocumentsDisallowAbandon()
    throws BadLocationException
  {
    PreventExitSecurityManager manager = new PreventExitSecurityManager();
    System.setSecurityManager(manager);

    _model.newFile();
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    _model.newFile();
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);

    assertNumOpenDocs(4);

    // Ensure canAbandonChanges is called
    TestListener listener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        canAbandonCount++;
        return false; // no, don't quit on me!
      }

      public void fileClosed(OpenDefinitionsDocument doc) {
        closeCount++;
      }
    };

    _model.addListener(listener);

    try {
      _model.quit();

      // Should close first new file, stop trying after doc1
      listener.assertCloseCount(1);
      listener.assertAbandonCount(1);
      assertNumOpenDocs(3);

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
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Get source root
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root", subdir, roots[0]);
  }

  public void testGetSourceRootPackageThreeDeepValid()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/c
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "c");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc =
      setupDocument("package a.b.c;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root", baseTempDir, roots[0]);

  }

  public void testGetSourceRootPackageThreeDeepInvalid()
    throws BadLocationException, IOException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a/b/d
    File subdir = new File(baseTempDir, "a");
    subdir = new File(subdir, "b");
    subdir = new File(subdir, "d");
    subdir.mkdirs();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc =
      setupDocument("package a.b.c;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // The package name is wrong so this should fail.
    try {
      File[] root = _model.getSourceRootSet();
      fail("getSourceRoot() did not fail on invalid package. It returned: " +
           root);
    }
    catch (InvalidPackageException e) {
      // good.
    }
  }

  public void testGetSourceRootPackageOneDeepValid()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectory a
    File subdir = new File(baseTempDir, "a");
    subdir.mkdir();

    // Save the footext to Foo.java in the subdirectory
    File fooFile = new File(subdir, "Foo.java");
    OpenDefinitionsDocument doc = setupDocument("package a;\n" + FOO_TEXT);
    doc.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, roots.length);
    assertEquals("source root", baseTempDir, roots[0]);

  }


  public void testGetMultipleSourceRootsDefaultPackage()
    throws BadLocationException, IOException, InvalidPackageException
  {
    // Create temp directory
    File baseTempDir = tempDirectory();

    // Now make subdirectories a, b
    File subdir1 = new File(baseTempDir, "a");
    subdir1.mkdir();
    File subdir2 = new File(baseTempDir, "b");
    subdir2.mkdir();

    // Save the footext to Foo.java in subdirectory 1
    File file1 = new File(subdir1, "Foo.java");
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    doc1.saveFileAs(new FileSelector(file1));

    // Save the bartext to Bar.java in subdirectory 1
    File file2 = new File(subdir1, "Bar.java");
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    doc2.saveFileAs(new FileSelector(file2));

    // Save the bartext to Bar.java in subdirectory 2
    File file3 = new File(subdir2, "Bar.java");
    OpenDefinitionsDocument doc3 = setupDocument(BAR_TEXT);
    doc3.saveFileAs(new FileSelector(file3));

    // No events should fire
    _model.addListener(new TestListener());

    // Get source roots (should be 2: no duplicates)
    File[] roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 2, roots.length);
    File root1 = roots[0];
    File root2 = roots[1];

    // Make sure both source roots are in set
    if (!( (root1.equals(subdir1) && root2.equals(subdir2)) ||
           (root1.equals(subdir2) && root2.equals(subdir1)) ))
    {
      fail("source roots did not match");
    }
  }

}
