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
    // Create temp directory
    File baseTempDir = tempDirectory();

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
    setupDocument("package a.b.c;\n" + FOO_TEXT);
    _model.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    assertEquals("source root",
                 baseTempDir,
                 _model.getSourceRoot());

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
    setupDocument("package a;\n" + FOO_TEXT);
    _model.saveFileAs(new FileSelector(fooFile));

    // No events should fire
    _model.addListener(new TestListener());

    // Since we had the package statement the source root should be base dir
    assertEquals("source root",
                 baseTempDir,
                 _model.getSourceRoot());

  }

 }
