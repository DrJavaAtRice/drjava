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

import  junit.framework.*;

import java.io.*;

import java.util.LinkedList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Position;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * A test on the GlobalModel for compilation.
 *
 * @version $Id$
 */
public final class GlobalModelCompileTest extends GlobalModelTestCase {
  private static final String FOO_MISSING_CLOSE_TEXT =
    "class DrJavaTestFoo {";
  
  private static final String BAR_MISSING_SEMI_TEXT =
    "class DrJavaTestBar { int x }";
  
  private static final String FOO_PACKAGE_AFTER_IMPORT =
    "import java.util.*;\npackage a;\n" + FOO_TEXT;

  private static final String FOO_PACKAGE_INSIDE_CLASS =
    "class DrJavaTestFoo { package a; }";

  private static final String FOO_PACKAGE_AS_FIELD =
    "class DrJavaTestFoo { int package; }";

  private static final String FOO_PACKAGE_AS_FIELD_2 =
    "class DrJavaTestFoo { int package = 5; }";

  private static final String FOO_PACKAGE_AS_PART_OF_FIELD =
    "class DrJavaTestFoo { int cur_package = 5; }";

  private static final String FOO2_EXTENDS_FOO_TEXT =
    "class DrJavaTestFoo2 extends DrJavaTestFoo {}";
  
  private static final String FOO_NON_PUBLIC_CLASS_TEXT = 
    "class DrJavaTestFoo {} class Foo{}";
  
  private static final String FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT =
    "class DrJavaTestFoo2 extends Foo{}";
  
  private static final String FOO_WITH_ASSERT =
    "class DrJavaTestFoo { void foo() { assert true; } }";

  private static final String BAR_MISSING_SEMI_TEXT_MULTIPLE_LINES =
    "class DrJavaTestFoo {\n  int a = 5;\n  int x\n }";
  
  private static final String FOO_WITH_GENERICS =
    "class DrJavaTestFooGenerics<T> {}";

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
   * test case for each compiler, without resetting the interactions JVM.
   * This method is called once per test method, and it magically
   * invokes the method.
   */
  public void runBare() throws Throwable {
    CompilerInterface[] compilers = CompilerRegistry.ONLY.getAvailableCompilers();
    for (int i = 0; i < compilers.length; i++) {
      System.out.println("Run " + i + ": " + compilers[i]);
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
   * Tests calling compileAll with no source files works.
   * Doesn't reset interactions because Interactions Pane isn't used.
   */
  public void testCompileAllWithNoFiles()
    throws BadLocationException, IOException, InterruptedException
  {
    // Open one empty doc
    _model.newFile();
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    _model.compileAll();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();
  }

  
  /**
   * Tests that the interactions pane is reset after a successful
   * compile if it has been used.
   */
  public void testCompileResetsInteractions()
    throws BadLocationException, IOException, InterruptedException,
    DocumentAdapterException
  {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = new File(_tempDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));

    // Interpret something to force a reset
    interpret("Object o = new Object();");
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(true);
    _model.addListener(listener);
    int numErrors = _model.getNumErrors();
    synchronized(listener) {
      _model.compileAll();
      numErrors = _model.getNumErrors();
      if (_model.getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      listener.wait();
    }
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();
  }

  /**
   * Tests calling compileAll with different source roots works.
   */
  public void testCompileAllDifferentSourceRoots()
    throws BadLocationException, IOException, InterruptedException
  {
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = new File(aDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    doc2.saveFile(new FileSelector(file2));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    int numErrors = _model.getNumErrors();
    _model.compileAll();
    numErrors = _model.getNumErrors();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists for both files
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Foo Class file doesn't exist after compile", 
               compiled.exists());
    File compiled2 = classForJava(file2, "DrJavaTestBar");
    assertTrue(_name() + "Bar Class file doesn't exist after compile", 
               compiled2.exists());
  }
  
  /**
   * Tests calling compileAll with different source roots works
   * if the files have errors in them.  (Each file has 1 error.)
   */
  public void testCompileAllFailsDifferentSourceRoots()
    throws BadLocationException, IOException
  {
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setupDocument(FOO_MISSING_CLOSE_TEXT);
    final File file = new File(aDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_MISSING_SEMI_TEXT);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    doc2.saveFile(new FileSelector(file2));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    _model.compileAll();
    assertCompileErrorsPresent(_name(), true);
    assertEquals("Should have 2 compiler errors", 2, _model.getNumErrors());
    listener.checkCompileOccurred();

    // Make sure .class does not exist for both files
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertEquals(_name() + "Class file exists after failing compile (1)",
                 false,
                 compiled.exists());
    File compiled2 = classForJava(file2, "DrJavaTestBar");
    assertEquals(_name() + "Class file exists after failing compile (2)",
                 false,
                 compiled2.exists());
  }

  /**
   * Test that one compiled file can depend on the other and that when a keyword
   * is part of a field name, the file will compile.
   * We compile DrJavaTestFoo and then DrJavaTestFoo2 (which extends 
   * DrJavaTestFoo). This shows that the compiler successfully found 
   * DrJavaTestFoo2 when compiling DrJavaTestFoo.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileClasspathOKDefaultPackage()
    throws BadLocationException, IOException, InterruptedException
  {
    // Create/compile foo, assuming it works
    OpenDefinitionsDocument doc1 = setupDocument(FOO_PACKAGE_AS_PART_OF_FIELD);
    final File fooFile = new File(_tempDir, "DrJavaTestFoo.java");
    
    doc1.saveFile(new FileSelector(fooFile));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener); 
    doc1.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);

    OpenDefinitionsDocument doc2 = setupDocument(FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(_tempDir, "DrJavaTestFoo2.java");
    doc2.saveFile(new FileSelector(foo2File));

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(foo2File, "DrJavaTestFoo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
  }

  /**
   * Test that one compiled file can depend on the other.
   * We compile a.DrJavaTestFoo and then b.DrJavaTestFoo2 (which extends 
   * DrJavaTestFoo). This shows that the compiler successfully found 
   * DrJavaTestFoo2 when compiling DrJavaTestFoo.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileClasspathOKDifferentPackages()
    throws BadLocationException, IOException, InterruptedException
  {
    File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();

    // Create/compile foo, assuming it works
    // foo must be public and in DrJavaTestFoo.java!
    OpenDefinitionsDocument doc1 =
      setupDocument("package a;\n" + "public " + FOO_TEXT);
    final File fooFile = new File(aDir, "DrJavaTestFoo.java");
    doc1.saveFile(new FileSelector(fooFile));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    doc1.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    
    OpenDefinitionsDocument doc2 =
      setupDocument("package b;\nimport a.DrJavaTestFoo;\n" + FOO2_EXTENDS_FOO_TEXT);
    final File foo2File = new File(bDir, "DrJavaTestFoo2.java");
    doc2.saveFile(new FileSelector(foo2File));

    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();

    // Make sure .class exists
    File compiled = classForJava(foo2File, "DrJavaTestFoo2");
    assertTrue(_name() + "Class file doesn't exist after compile",
               compiled.exists());
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

    File compiled = classForJava(file, "DrJavaTestFoo");
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

    File compiled = classForJava(file, "DrJavaTestFoo");
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

    File compiled = classForJava(file, "DrJavaTestFoo");
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
    File fooFile = new File(subdir, "DrJavaTestFoo.java");
    File compiled = classForJava(fooFile, "DrJavaTestFoo");

    // Now make subdirectory a
    subdir.mkdir();

    // Save the footext to DrJavaTestFoo.java in the subdirectory
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
   * If we try to compile an unsaved file, and if we don't save when
   * asked to saveAllBeforeProceeding, it should not do the compile
   * or any other actions.
   */
  public void testCompileAbortsIfUnsaved()
    throws BadLocationException, IOException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);

    TestListener listener = new TestListener() {
      public void saveAllBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertModified(true, doc);
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
        saveAllBeforeProceedingCount++;
        // since we don't actually save the compile should abort
      }
    };

    _model.addListener(listener);
    doc.startCompile();
    listener.assertSaveAllBeforeProceedingCount(1);
    assertModified(true, doc);
    assertContents(FOO_TEXT, doc);
  }

  /**
   * If we try to compile while any files are unsaved, and if we don't 
   * save when asked to saveAllBeforeProceeding, it should not do the compile
   * or any other actions.
   */
  public void testCompileAbortsIfAnyUnsaved()
    throws BadLocationException, IOException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    
    TestListener listener = new TestListener() {
      public void saveAllBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertModified(true, doc);
        assertModified(true, doc2);
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
        saveAllBeforeProceedingCount++;
        // since we don't actually save the compile should abort
      }
    };
    
    _model.addListener(listener);
    doc.startCompile();
    listener.assertSaveAllBeforeProceedingCount(1);
    assertModified(true, doc);
    assertModified(true, doc2);
    assertContents(FOO_TEXT, doc);
    assertContents(BAR_TEXT, doc2);
  }

  /**
   * If we try to compile while any files (including the active file) are 
   * unsaved but we do save it from within saveAllBeforeProceeding, the 
   * compile should occur happily.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileAnyUnsavedButSaveWhenAsked()
    throws BadLocationException, IOException, InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(2);
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false) {
      public void saveAllBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
        assertModified(true, doc);
        assertModified(true, doc2);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInteractionsResetCount(0);
        assertConsoleResetCount(0);
        
        try {
          doc.saveFile(new FileSelector(file));
          doc2.saveFile(new FileSelector(file2));
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }
        
        saveAllBeforeProceedingCount++;
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveAllBeforeProceedingCount(0);
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
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        //assertEquals(_name() + "file saved", file, f);
        saveCount++;
      }
    };
    
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }

    // Check events fired
    listener.assertSaveAllBeforeProceedingCount(1);
    listener.assertSaveCount(2);
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());
  }

  /**
   * If we try to compile while any files (but not the active file) are unsaved
   * but we do save it from within saveAllBeforeProceeding, the compile should
   * occur happily.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileActiveSavedAnyUnsavedButSaveWhenAsked()
    throws BadLocationException, IOException, InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(1);
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false) {
      public void saveAllBeforeProceeding(GlobalModelListener.SaveReason reason) {
        assertEquals(_name() + "save reason", COMPILE_REASON, reason);
        assertModified(false, doc);
        assertModified(true, doc2);
        assertSaveCount(0);
        assertCompileStartCount(0);
        assertCompileEndCount(0);
        assertInteractionsResetCount(0);
        assertConsoleResetCount(0);
        
        try {
          doc2.saveFile(new FileSelector(file2));
        }
        catch (IOException ioe) {
          fail("Save produced exception: " + ioe);
        }
        
        saveAllBeforeProceedingCount++;
        assertModified(false, doc);
        assertModified(false, doc2);
        assertTrue(!_model.areAnyModifiedSinceSave());
      }
      
      public void fileSaved(OpenDefinitionsDocument doc) {
        assertModified(false, doc);
        assertSaveAllBeforeProceedingCount(0);
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
        catch (FileMovedException fme) {
          // We know file should exist
          fail("file does not exist");
        }
        assertEquals(_name() + "file saved", file2, f);
        saveCount++;
      }
    };
    
    assertModified(true, doc);
    doc.saveFile(new FileSelector(file));
    assertModified(false, doc);
    assertModified(true, doc2);
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    assertTrue(!_model.areAnyModifiedSinceSave());
    
    // Check events fired
    listener.assertCompileStartCount(1);
    listener.assertSaveAllBeforeProceedingCount(1);
    listener.assertSaveCount(1);
    assertCompileErrorsPresent(_name(), false);
    listener.checkCompileOccurred();
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Class file doesn't exist after compile", compiled.exists());
  }

  /**
   * After creating a new file, saving, and compiling it, this test checks
   * that the new document is in sync after compiling and is out of sync 
   * after modifying and even saving it.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testClassFileSynchronization()
    throws BadLocationException, IOException, InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    
    doc.saveFile(new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    assertTrue(_name() + "Class file should not exist before compile", 
               doc.getDocument().getCachedClassFile() == null);
    assertTrue("should not be in sync before compile", 
               !doc.checkIfClassFileInSync());
    doc.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    listener.checkCompileOccurred();
    assertTrue("should be in sync after compile",
               doc.checkIfClassFileInSync());
    doc.getDocument().insertString(0, "hi", null);
    assertTrue("should not be in sync after modification",
               !doc.checkIfClassFileInSync());

    // Have to wait 2 seconds so file will have a different timestamp
    Thread.sleep(2000);

    doc.saveFile(new FileSelector(file));
    assertTrue("should not be in sync after save", 
               !doc.checkIfClassFileInSync());
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + " Class file should exist after compile", compiled.exists());
  }
  
  /**
   * Ensure that renaming a file makes it out of sync with its class file.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testClassFileSynchronizationAfterRename()
    throws BadLocationException, IOException, IllegalStateException,
    InterruptedException
  {
    final OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(2);
    
    doc.saveFile(new FileSelector(file));
    
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    assertTrue(_name() + "Class file should not exist before compile", 
               doc.getDocument().getCachedClassFile() == null);
    assertTrue("should not be in sync before compile", 
               !doc.checkIfClassFileInSync());
    doc.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    listener.checkCompileOccurred();
    assertTrue("should be in sync after compile", 
               doc.checkIfClassFileInSync());
    
    // Have to wait 1 second so file will have a different timestamp
    Thread.sleep(2000);
    
    // Rename to a different file
    doc.saveFileAs(new FileSelector(file2));
    assertTrue("should not be in sync after renaming",
               !doc.checkIfClassFileInSync());
  }
  
   /**
   * Tests a compile after a file has unexpectedly been moved or deleted.
   */
  public void testCompileAfterFileMoved() throws BadLocationException, IOException {
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));    
    TestListener listener = new TestListener();
    _model.addListener(listener);
    file.delete();
    try {
      doc.startCompile();
      fail("Compile should not have begun.");
    }
    catch (FileMovedException fme) {
      //compile should never have begun because the file was not where it was expected
      // to be on disk.
    }
    
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Class file shouldn't exist after compile", !compiled.exists());
  }
  
  /**
   * Tests a compile on a file that references a non-public class defined in 
   * another class with a name different than the non-public class.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileReferenceToNonPublicClass() throws BadLocationException, IOException, InterruptedException {
    OpenDefinitionsDocument doc = setupDocument(FOO_NON_PUBLIC_CLASS_TEXT);
    OpenDefinitionsDocument doc2 = setupDocument(FOO2_REFERENCES_NON_PUBLIC_CLASS_TEXT);
    final File file = tempFile();
    final File file2 = tempFile(1);
    doc.saveFile(new FileSelector(file));
    doc2.saveFile(new FileSelector(file2));
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
    _model.addListener(listener);
    doc.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    _model.removeListener(listener);
    CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
    _model.addListener(listener2);
    doc2.startCompile();
    if (_model.getNumErrors() > 0) {
      fail("compile failed: " + getCompilerErrorString());
    }
    
    listener.checkCompileOccurred();
    listener2.checkCompileOccurred();
    assertCompileErrorsPresent(_name(), false);
    
    // Make sure .class exists
    File compiled = classForJava(file, "DrJavaTestFoo");
    File compiled2 = classForJava(file, "DrJavaTestFoo2");
    assertTrue(_name() + "Class file should exist after compile", compiled.exists());
    assertTrue(_name() + "Class file should exist after compile", compiled2.exists());
  }
  
  /**
   * Test support for assert keyword if enabled.
   * Note that this test only runs in Java 1.4 or higher.
   * Doesn't reset interactions because no interpretations are performed.
   */
  public void testCompileWithJavaAssert()
    throws BadLocationException, IOException, InterruptedException
  {
    // No assert support by default (or in 1.3)
    OpenDefinitionsDocument doc = setupDocument(FOO_WITH_ASSERT);
    final File file = tempFile();
    doc.saveFile(new FileSelector(file));
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    
    doc.startCompile();

    assertCompileErrorsPresent(_name(), true);
    listener.checkCompileOccurred();
    File compiled = classForJava(file, "DrJavaTestFoo");
    assertTrue(_name() + "Class file exists after compile?!", !compiled.exists());
    _model.removeListener(listener);
    
    
    // Only run assertions test in 1.4
    String version = System.getProperty("java.version");
    if ((version != null) && ("1.4.0".compareTo(version) <= 0)) {
      // Turn on assert support
      DrJava.getConfig().setSetting(OptionConstants.JAVAC_ALLOW_ASSERT,
                                    new Boolean(true));
      
      CompileShouldSucceedListener listener2 = new CompileShouldSucceedListener(false);
      _model.addListener(listener2); 
      doc.startCompile();
      if (_model.getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      _model.removeListener(listener2);
      assertCompileErrorsPresent(_name(), false);
      listener2.checkCompileOccurred();
      
      // Make sure .class exists
      compiled = classForJava(file, "DrJavaTestFoo");
      assertTrue(_name() + "Class file doesn't exist after compile",
                 compiled.exists());
    }
  }


  public void testCompileFailsCorrectLineNumbers() throws BadLocationException, IOException {
        File aDir = new File(_tempDir, "a");
    File bDir = new File(_tempDir, "b");
    aDir.mkdir();
    bDir.mkdir();
    OpenDefinitionsDocument doc = setupDocument(FOO_PACKAGE_AFTER_IMPORT);
    final File file = new File(aDir, "DrJavaTestFoo.java");
    doc.saveFile(new FileSelector(file));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_MISSING_SEMI_TEXT_MULTIPLE_LINES);
    final File file2 = new File(bDir, "DrJavaTestBar.java");
    doc2.saveFile(new FileSelector(file2));
    
    CompileShouldFailListener listener = new CompileShouldFailListener();
    _model.addListener(listener);
    _model.compileAll();
    assertCompileErrorsPresent(_name(), true);
    //    assertEquals("Should have 2 compiler errors", 2, _model.getNumErrors());
    listener.checkCompileOccurred();

    //TODO: rewrite these lines for the new interface
    //Position[] positions = doc.getCompilerErrorModel().getPositions();
    //Position[] positions2 = doc2.getCompilerErrorModel().getPositions();

    //assertTrue("first doc should have errors", positions.length > 0);
    //assertTrue("second doc should have errors", positions2.length > 0);
    //assertTrue("location of first error should be between 20 and 29 inclusive (line 2)",
    //    positions[0].getOffset() <= 20 && positions[0].getOffset() <= 29);
    //assertTrue("location of error should be after 34 (line 3 or 4)", positions2[0].getOffset() >= 34);

  }
  
  /**
   * Tests compiling a file with generics works with generic compilers.
   * (NOTE: this currently tests the GJ compiler, but not JSR-14...
   *  JSR-14 is only available if the config option is set, and we clear
   *  the config before running the tests.  We have a guess where the jar
   *  is -- the lib directory -- but how can we get a URL for that?)
   */
  public void testCompileWithGenerics()
    throws BadLocationException, IOException, InterruptedException
  {
    // Only run this test if using a compiler with generics
    if (_isGenericCompiler()) {
      
      OpenDefinitionsDocument doc = setupDocument(FOO_WITH_GENERICS);
      final File file = new File(_tempDir, "DrJavaTestFooGenerics.java");
      doc.saveFile(new FileSelector(file));
      
      CompileShouldSucceedListener listener = new CompileShouldSucceedListener(false);
      _model.addListener(listener);
      int numErrors = _model.getNumErrors();
      _model.compileAll();
      numErrors = _model.getNumErrors();
      if (_model.getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      assertCompileErrorsPresent(_name(), false);
      listener.checkCompileOccurred();
      
      // Make sure .class exists
      File compiled = classForJava(file, "DrJavaTestFooGenerics");
      assertTrue(_name() + "FooGenerics Class file doesn't exist after compile", compiled.exists());
    }
  }
  
  /**
   * Returns whether the currently active compiler supports generics.
   */
  protected boolean _isGenericCompiler() {
    String name = _model.getActiveCompiler().getClass().getName();
    for (int i=0; i < CompilerRegistry.GENERIC_JAVA_COMPILERS.length; i++) {
      if (name.equals(CompilerRegistry.GENERIC_JAVA_COMPILERS[i])) {
        //System.out.println(name + " supports generics");
        return true;
      }
    }
    //System.out.println(name + " doesn't support generics");
    return false;
  }
  
}
