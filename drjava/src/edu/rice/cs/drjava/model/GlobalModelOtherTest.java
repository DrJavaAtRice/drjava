/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.*;
import javax.swing.text.BadLocationException;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.plt.iter.IterUtil;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A test on the GlobalModel that does deals with everything outside of simple file operations, e.g., compile, quit.
  * @version $Id$
  */
public final class GlobalModelOtherTest extends GlobalModelTestCase implements OptionConstants {
  
  //  _log can be inherited from GlobalModelTestCase
  Log _log = new Log("GlobalModelOtherTest.txt", false);
  
  private static final String FOO_CLASS =
    "package bar;\n" +
    "public class Foo {\n" +
    "  public static void main(String[] args) {\n" +
    "    System.out.println(\"Foo\");\n" +
    "  }\n" +
    "}\n";
  
  /** Get the canonical name of a file.  If the operation fails, the test will fail. 
   * @param f the file for which to find the canonical path
   * @return the canonical file path
   */
  private File makeCanonical(File f) {
    try { return f.getCanonicalFile(); }
    catch (IOException e) { fail("Can't get a canonical path for file " + f); return null; }
  }
  
  /** Tests that the undoableEditHappened event is fired if the undo manager is in use. */
  public void testUndoEventsOccur() /* throws BadLocationException */ {
    debug.logStart();
    
    final TestListener listener = new TestListener() { 
      public void undoableEditHappened() { 
        undoableEditCount++; 
        System.err.println("undoableEditHappened call propagated to listener");
      } 
    };
    
    final OpenDefinitionsDocument doc = _model.newFile();

    Utilities.clearEventQueue();
    
    _model.addListener(listener);
        // Have to add an undoable edit listener for Undo to work
    doc.addUndoableEditListener(new UndoableEditListener() {
      public void undoableEditHappened(UndoableEditEvent e) { 
            System.err.println("undoableEditHappened(" + e + ") called");
        doc.getUndoManager().addEdit(e.getEdit()); 
      }
    });
    
    changeDocumentText("test", doc);
        
    Utilities.clearEventQueue();  // undoableEditHappened propagated using invokeLater
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        listener.assertUndoableEditCount(1);
        
        _model.removeListener(listener);
      }
    });
//    Utilities.clearEventQueue();
    _log.log("testUndoEventsOccur() completed");
    debug.logEnd();
  }
  
  
  /** Checks that System.exit is handled appropriately from interactions pane. 
   * @throws EditDocumentException if an error occurs during editing
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testExitInteractions() throws EditDocumentException, InterruptedException {
    debug.logStart();
    final InteractionListener listener = new InteractionListener(); /*{
      
      public void consoleReset() { consoleResetCount++; }
    }; */
    _model.addListener(listener);
    
    listener.logInteractionStart();
    interpretIgnoreResult("System.exit(23);"); 
    listener.waitInteractionDone();
    listener.waitResetDone();
    Utilities.clearEventQueue();
    
    _model.removeListener(listener);
    
//    listener.assertConsoleResetCount(0);
    listener.assertInteractionStartCount(1);
    listener.assertInterpreterResettingCount(1);
    listener.assertInterpreterReadyCount(1);
    listener.assertInterpreterExitedCount(1);
    assertEquals("exit status", 23, listener.getLastExitStatus());
    
    _log.log("testExitInteractions() completed");
    debug.logEnd();
  }
  
  /** Creates a new class, compiles it and then checks that the REPL can see it.  
   * Then checks that a compiled class file in another directory can be both 
   * accessed and extended if it is on the "extra.classpath" config option.
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws EditDocumentException if an error occurs during editing
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testInteractionsCanSeeCompiledClasses() throws BadLocationException, EditDocumentException,
    IOException, InterruptedException {
    debug.logStart();
    
    // Compile Foo
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    File dir1 = makeCanonical(new File(_tempDir, "dir1"));
    dir1.mkdir();
    File file1 = makeCanonical(new File(dir1, "TestFile1.java"));
    doCompile(doc1, file1);
    
    assertEquals("interactions result", "\"DrJavaTestFoo\"", interpret("new DrJavaTestFoo().getClass().getName()"));
    
    // Add directory 1 to extra classpath and close doc1
    Vector<File> cp = new Vector<File>();
    cp.add(dir1);
    DrJava.getConfig().setSetting(EXTRA_CLASSPATH, cp);
    
    Utilities.clearEventQueue();
    _model.closeFile(doc1);
    
    // Compile Baz which extends Foo in another directory.
    OpenDefinitionsDocument doc2 = setupDocument(BAZ_TEXT);
    File dir2 = makeCanonical(new File(_tempDir, "dir2"));
    dir2.mkdir();
    File file2 = makeCanonical(new File(dir2, "TestFile1.java"));
    doCompile(doc2, file2);
    
    // Ensure that Baz can use the Foo class from extra classpath
    assertEquals("interactions result", "\"DrJavaTestBaz\"", interpret("new DrJavaTestBaz().getClass().getName()"));
    
    // Ensure that static fields can be seen
    assertEquals("result of static field", "3", interpret("DrJavaTestBaz.x"));
    
    // Also ensure that Foo can be used directly
    assertEquals("interactions result", "\"DrJavaTestFoo\"", interpret("new DrJavaTestFoo().getClass().getName()"));
    
    _log.log("testInteractionsCanSeeCompletedClasses() completed");
    debug.logEnd();
  }
  
  /** Compiles a new class in the default package with a mixed case name, and 
   * ensures that it can be instantiated on a variable with an identical name 
   * (but a lowercase first letter).  Catches SF bug #689026 ("DynamicJava 
   * can't handle certain variable names")
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws EditDocumentException if an error occurs during editing
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
    */
  public void testInteractionsVariableWithLowercaseClassName() throws BadLocationException, EditDocumentException,
    IOException, InterruptedException {
    debug.logStart();
    
    // Compile a test file
    OpenDefinitionsDocument doc1 = setupDocument("public class DrJavaTestClass {}");
    File file1 = makeCanonical(new File(_tempDir, "DrJavaTestClass.java"));
    doCompile(doc1, file1);
    
    // This shouldn't cause an error (no output should be displayed)
    assertEquals("interactions result", "", interpret("Object drJavaTestClass = new DrJavaTestClass();"));
    _log.log("testInteractionsVariableWithLowercaseClassName() completed");
    debug.logEnd();
  }
  
  /** Checks that updating a class and recompiling it is visible from the REPL. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws EditDocumentException if an error occurs during editing
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testInteractionsCanSeeChangedClass() throws BadLocationException, EditDocumentException,
    IOException, InterruptedException {
    debug.logStart();
    
    final String text_before = "class DrJavaTestFoo { public int m() { return ";
    final String text_after = "; } }";
    final int num_iterations = 3;
    File file;
    OpenDefinitionsDocument doc;
    
    for (int i = 0; i < num_iterations; i++) {
      doc = setupDocument(text_before + i + text_after);
      file = tempFile(i);
      doCompile(doc, file);
      
      assertEquals("interactions result, i=" + i, String.valueOf(i), interpret("new DrJavaTestFoo().m()"));
    }
    _log.log("testInteractionsCanSeeChangedClass() completed");
    debug.logEnd();
  }
  
  /** Checks that an anonymous inner class can be defined in the repl! 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws EditDocumentException if an error occurs during editing
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testInteractionsDefineAnonymousInnerClass() throws BadLocationException, EditDocumentException,
    IOException, InterruptedException {
    debug.logStart();
    
    final String interface_text = "public interface I { int getValue(); }";
    final File file = createFile("I.java");
    
    OpenDefinitionsDocument doc;
    
    doc = setupDocument(interface_text);
    doCompile(doc, file);
    
    for (int i = 0; i < 3; i++) {
      String s = "new I() { public int getValue() { return " + i + "; } }.getValue()";
      
      assertEquals("interactions result, i=" + i, String.valueOf(i), interpret(s));
    }
    _log.log("testInteractionsDefineAnonymousInnerClass() completed");
    debug.logEnd();
  }
  
  public void testGetSourceRootDefaultPackage() throws BadLocationException, IOException {
    debug.logStart();
    
    // Get source root (current directory only)
    Iterable<File> roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 0, IterUtil.sizeOf(roots));
    
    // Create temp directory
    File baseTempDir = tempDirectory();
    
    // Now make subdirectory a/b/c
    File subdir = makeCanonical(new File(baseTempDir, "a"));
    subdir = makeCanonical(new File(subdir, "b"));
    subdir = makeCanonical(new File(subdir, "c"));
    subdir.mkdirs();
    
    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = makeCanonical(new File(subdir, "DrJavaTestFoo.java"));
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    saveFileAs(doc, new FileSelector(fooFile));
    
    // No events should fire
    _model.addListener(new TestListener());
    
    // Get source roots
    roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, IterUtil.sizeOf(roots));
    // Get the source root for the new file in directory subdir
    assertEquals("source root", subdir, IterUtil.first(roots));
    
    _log.log("testGetSourceRootDefaultPackage() completed");
    debug.logEnd();
  }
  
  public void testGetSourceRootPackageThreeDeepValid() throws BadLocationException, IOException {
    debug.logStart();
    
    // Create temp directory
    File baseTempDir = tempDirectory();
    
    // Now make subdirectory a/b/c
    File subdir = makeCanonical(new File(baseTempDir, "a"));
    subdir = makeCanonical(new File(subdir, "b").getCanonicalFile());
    subdir = makeCanonical(new File(subdir, "c").getCanonicalFile());
    subdir.mkdirs();
    
    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = makeCanonical(new File(subdir, "DrJavaTestFoo.java"));
    OpenDefinitionsDocument doc = setupDocument("package a.b.c;\n" + FOO_TEXT);
    saveFileAs(doc, new FileSelector(fooFile));
//    System.err.println("Package name is: " + _model.getPackageName());
    
    // No events should fire
    _model.addListener(new TestListener());
    
    // Since we had the package statement the source root should be base dir
    Iterable<File> roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, IterUtil.sizeOf(roots));
    assertEquals("source root", baseTempDir.getCanonicalFile(), IterUtil.first(roots).getCanonicalFile());
    
    _log.log("testGetSourceRootPackageThreeDeepValid() completed");
    debug.logEnd();
  }
  
  /** Tests that getSourceRoot works with a relative path when a package name is present. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws IOException if an IO operation fails
   */
  public void testGetSourceRootPackageThreeDeepValidRelative() throws BadLocationException, IOException {
    debug.logStart();
    
    // Create temp directory
    File baseTempDir = tempDirectory();
    File subdir = makeCanonical(new File(baseTempDir, "a"));
    subdir = makeCanonical(new File(subdir, "b"));
    subdir = makeCanonical(new File(subdir, "c"));
    subdir.mkdirs();
    
    // Save the footext to DrJavaTestFoo.java in a relative directory
    //   temp/./a/b/../b/c == temp/a/b/c
    File relDir = makeCanonical(new File(baseTempDir, "./a/b/../b/c"));
    File fooFile = makeCanonical(new File(relDir, "DrJavaTestFoo.java"));
    OpenDefinitionsDocument doc =
      setupDocument("package a.b.c;\n" + FOO_TEXT);
    saveFileAs(doc, new FileSelector(fooFile));
    
    // No events should fire
    _model.addListener(new TestListener());
    
    // Since we had the package statement the source root should be base dir
    Iterable<File> roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, IterUtil.sizeOf(roots));
    assertEquals("source root", baseTempDir.getCanonicalFile(), IterUtil.first(roots).getCanonicalFile());
    
    _log.log("testGetSourceRootPackageThreeDeepValidRelative() completed");
    debug.logEnd();
  }
  
  public void testGetSourceRootPackageThreeDeepInvalid() throws BadLocationException, IOException {
    debug.logStart();
    
    // Create temp directory
    File baseTempDir = tempDirectory();
    
    // Now make subdirectory a/b/d
    File subdir = makeCanonical(new File(baseTempDir, "a"));
    subdir = makeCanonical(new File(subdir, "b"));
    subdir = makeCanonical(new File(subdir, "d"));
    subdir.mkdirs();
    
    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = makeCanonical(new File(subdir, "DrJavaTestFoo.java"));
    OpenDefinitionsDocument doc = setupDocument("package a.b.c;\n" + FOO_TEXT);
    saveFileAs(doc, new FileSelector(fooFile));
    
    // No events should fire
    _model.addListener(new TestListener());
    
    // The package name is wrong so this should return only currDir
    Iterable<File> roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 0, IterUtil.sizeOf(roots));
    
    _log.log("testGetSourceRootPackageThreeDeepInvalid() completed");
    debug.logEnd();
  }
  
  public void testGetSourceRootPackageOneDeepValid() throws BadLocationException, IOException {
    debug.logStart();
    
    // Create temp directory
    File baseTempDir = tempDirectory();
    
    // Now make subdirectory a
    File subdir = makeCanonical(new File(baseTempDir, "a"));
    subdir.mkdir();
    
    // Save the footext to DrJavaTestFoo.java in the subdirectory
    File fooFile = makeCanonical(new File(subdir, "DrJavaTestFoo.java"));
    OpenDefinitionsDocument doc = setupDocument("package a;\n" + FOO_TEXT);
    saveFileAs(doc, new FileSelector(fooFile));
    
    // No events should fire
    _model.addListener(new TestListener());
    
    // Since we had the package statement the source root should be base dir
    Iterable<File> roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 1, IterUtil.sizeOf(roots));
    assertEquals("source root", baseTempDir.getCanonicalFile(), IterUtil.first(roots).getCanonicalFile());
    
    _log.log("testGetSourceRootPackageOneDeepValid() completed");
    debug.logEnd();
  }
  
  
  public void testGetMultipleSourceRootsDefaultPackage() throws BadLocationException, IOException {
    debug.logStart();
    
    // Create temp directory
    File baseTempDir = tempDirectory();
    
    // Now make subdirectories a, b
    File subdir1 = makeCanonical(new File(baseTempDir, "a"));
    subdir1.mkdir();
    File subdir2 = makeCanonical(new File(baseTempDir, "b"));
    subdir2.mkdir();
    
    // Save the footext to DrJavaTestFoo.java in subdirectory 1
    File file1 = makeCanonical(new File(subdir1, "DrJavaTestFoo.java"));
    OpenDefinitionsDocument doc1 = setupDocument(FOO_TEXT);
    saveFileAs(doc1, new FileSelector(file1));
    
    // Save the bartext to Bar.java in subdirectory 1
    File file2 = makeCanonical(new File(subdir1, "Bar.java"));
    OpenDefinitionsDocument doc2 = setupDocument(BAR_TEXT);
    saveFileAs(doc2, new FileSelector(file2));
    
    // Save the bartext to Bar.java in subdirectory 2
    File file3 = makeCanonical(new File(subdir2, "Bar.java"));
    OpenDefinitionsDocument doc3 = setupDocument(BAR_TEXT);
    saveFileAs(doc3, new FileSelector(file3));
    
    Utilities.clearEventQueue();
    
    // No events should fire
    _model.addListener(new TestListener());
    
    // Get source roots (should be 2: no duplicates)
    Iterable<File> roots = _model.getSourceRootSet();
    assertEquals("number of source roots", 2, IterUtil.sizeOf(roots));
    Iterator<File> i = roots.iterator();
    File root1 = i.next();
    File root2 = i.next();
    
    // Make sure both source roots are in set
    // But we don't care about the order
    if (!( (root1.equals(subdir1) && root2.equals(subdir2)) || (root1.equals(subdir2) && root2.equals(subdir1)) )) {
      fail("source roots did not match");
    }
    
    _log.log("testGetMultipleSourceRootsDefaultPackage() completed");
    debug.logEnd();
  }
  
  /** Creates a new class, compiles it and then checks that the REPL can see it. 
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws EditDocumentException if an error occurs during editing
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  public void testInteractionsLiveUpdateClassPath() throws BadLocationException, EditDocumentException,
    IOException, InterruptedException {
    debug.logStart();
    
    OpenDefinitionsDocument doc = setupDocument(FOO_TEXT);
    Utilities.clearEventQueue();
    
    File f = tempFile();
    
    doCompile(doc, f);
    
    // Rename the directory so it's not on the classpath anymore
    String tempPath = f.getParent();
    File tempDir = makeCanonical(new File(tempPath));
    tempDir.renameTo(makeCanonical(new File(tempPath + "a")));
    
    String result = interpret("new DrJavaTestFoo().getClass().getName()");
    
    // Should cause a NoClassDefFound, but we shouldn't check exact syntax.
    //  Instead, make sure it isn't "DrJavaTestFoo", as if the class was found.
    assertFalse("interactions should have an error, not the correct answer", "\"DrJavaTestFoo\"".equals(result));
//    System.err.println("Result1 is: " + result);
    
    // Add new directory to classpath through Config
    Vector<File> cp = new Vector<File>();
    cp.add(makeCanonical(new File(tempPath + "a")));
    DrJava.getConfig().setSetting(EXTRA_CLASSPATH, cp);
    
    Utilities.clearEventQueue();
    _model.resetInteractionsClassPath();
    
    result = interpret("new DrJavaTestFoo().getClass().getName()");
    
    // Now it should be on the classpath
    assertEquals("interactions result", "\"DrJavaTestFoo\"", result);
    
    // Rename directory back to clean up
    tempDir = makeCanonical(new File(tempPath + "a"));
    tempDir.renameTo(makeCanonical(new File(tempPath)));
    
    _log.log("testInteractionsLiveUpdateClasspath() completed");
    debug.logEnd();
  }
  
  /** Tests that the appropriate event is fired when the model's interpreter changes.*/
  public void testSwitchInterpreters() {
    debug.logStart();
    TestListener listener = new TestListener() {
      public void interpreterChanged(boolean inProgress) {
        assertTrue("should not be in progress", !inProgress);
        interpreterChangedCount++;
      }
    };
    _model.addListener(listener);
    
    final DefaultInteractionsModel dim = _model.getInteractionsModel();
    
    // Create a new Java interpreter, and set it to be active
    
    /*Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        dim.addInterpreter("testInterpreter");
        dim.setActiveInterpreter("testInterpreter", "myPrompt>"); 
      }
    });*/
    
//    Utilities.clearEventQueue();
    listener.assertInterpreterChangedCount(1);
    _model.removeListener(listener);
    
    _log.log("testSwitchInterpreters() completed");
    debug.logEnd();
  }
  
  public void testRunMainMethod() throws Exception {
    debug.logStart();
    
    File dir = makeCanonical(new File(_tempDir, "bar"));
    dir.mkdir();
    File file = makeCanonical(new File(dir, "Foo.java"));
    final OpenDefinitionsDocument doc = doCompile(FOO_CLASS, file);
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try { doc.runMain(null); }
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
    
//    Utilities.clearEventQueue();
    assertInteractionsContains(InteractionsModel.BANNER_PREFIX);
    doc.insertString(doc.getLength(), " ", null);
    
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try { doc.runMain(null); }
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
    
//    Utilities.clearEventQueue();
    assertInteractionsContains(AbstractGlobalModel.DOCUMENT_OUT_OF_SYNC_MSG);
    Utilities.clearEventQueue();  
    // Killing time here; Slave JVM may not have released Foo.class so that the file can be deleted on Windows.
    
    _log.log("testRunMainMethod() completed");
    debug.logEnd();
  }
  
  public void testBookmark() throws Exception {
    debug.logStart();
    
    File dir = makeCanonical(new File(_tempDir, "bar"));
    dir.mkdir();
    final File file = makeCanonical(new File(dir, "Foo.java"));
    java.io.FileWriter fw = new java.io.FileWriter(file);
    fw.write(FOO_CLASS);
    fw.close();
    _model.openFile(new edu.rice.cs.util.FileOpenSelector() {
      public File[] getFiles() throws edu.rice.cs.util.OperationCanceledException {
        return new File[] { file };
      }
    });
    assertEquals("Should be 0 bookmarks", 0, _model.getBookmarkManager().getRegions().size());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(3,3); } });
    ArrayList<MovingDocumentRegion> bms = _model.getBookmarkManager().getRegions();
    assertEquals("Should be 1 bookmarks", 1, bms.size());
    assertEquals("Start offset should be 0", 0, bms.get(0).getStartOffset());
    assertEquals("End offset should be " + FOO_CLASS.indexOf('\n'), FOO_CLASS.indexOf('\n'), bms.get(0).getEndOffset());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(3,3); } });
    bms = _model.getBookmarkManager().getRegions();
    assertEquals("Should be 0 bookmarks", 0, bms.size());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(3,6); } });
    bms = _model.getBookmarkManager().getRegions();
    assertEquals("Should be 1 bookmarks", 1, bms.size());
    assertEquals("Start offset should be 3", 3, bms.get(0).getStartOffset());
    assertEquals("End offset should be 6", 6, bms.get(0).getEndOffset());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(12,8); } });
    bms = _model.getBookmarkManager().getRegions();
    // Note: bms is sorted by increasing (startOffset, endOffset)
    assertEquals("Should be 2 bookmarks", 2, bms.size());  // the bookmarks are disjoint
    assertEquals("Start offset should be 3", 3, bms.get(0).getStartOffset());
    assertEquals("End offset should be 6", 6, bms.get(0).getEndOffset());
    assertEquals("Start offset should be 8", 8, bms.get(1).getStartOffset());
    assertEquals("End offset should be 12", 12, bms.get(1).getEndOffset());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(5,10); } });
    bms = _model.getBookmarkManager().getRegions();
    assertEquals("Should be 0 bookmarks", 0, bms.size());  // the preceding two bookmarks overlapped and were deleted
//    assertEquals("Start offset should be 5", 5, bms.get(0).getStartOffset());
//    assertEquals("End offset should be 10", 10, bms.get(0).getEndOffset());
//    assertEquals("Start offset should be 5", 5, bms.get(1).getStartOffset());
//    assertEquals("End offset should be 10", 10, bms.get(1).getEndOffset());
//    assertEquals("Start offset should be 8", 8, bms.get(2).getStartOffset());
//    assertEquals("End offset should be 12", 12, bms.get(2).getEndOffset());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(8,12); } });
    bms = _model.getBookmarkManager().getRegions();
    assertEquals("Should be 1 bookmarks", 1, bms.size());  // no preceding bookmark
    assertEquals("Start offset should be 8", 8, bms.get(0).getStartOffset());
    assertEquals("End offset should be 12", 12, bms.get(0).getEndOffset());
//    assertEquals("Start offset should be 5", 5, bms.get(1).getStartOffset());
//    assertEquals("End offset should be 10", 10, bms.get(1).getEndOffset());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(3,6); } });
    bms = _model.getBookmarkManager().getRegions();
    assertEquals("Should be 2 bookmarks", 2, bms.size());  // no overlap
    assertEquals("Start offset should be 3", 3, bms.get(0).getStartOffset());
    assertEquals("End offset should be 6", 6, bms.get(0).getEndOffset());
    assertEquals("Start offset should be 8", 8, bms.get(1).getStartOffset());
    assertEquals("End offset should be 12", 12, bms.get(1).getEndOffset());
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.toggleBookmark(10,5); } });
    bms = _model.getBookmarkManager().getRegions();
    assertEquals("Should be 0 bookmarks", 0, bms.size());
    
    debug.logEnd();
  }
  
}
