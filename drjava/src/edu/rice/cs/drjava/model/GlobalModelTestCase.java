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

import junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.*;
import javax.swing.*;
import java.rmi.registry.Registry;

import edu.rice.cs.util.*;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.DrJava;
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
public abstract class GlobalModelTestCase extends MultiThreadedTestCase {
  /**
   * the prototype global model to hold the interpreter. This prevents
   * us from having to re-invoke the interpreter every time!
   */
  protected static final DefaultGlobalModel _originalModel = 
    new DefaultGlobalModel();

  protected DefaultGlobalModel _model;
  protected File _tempDir;
  
  protected static final String FOO_TEXT = "class DrJavaTestFoo {}";
  protected static final String BAR_TEXT = "class DrJavaTestBar {}";
  protected static final String BAZ_TEXT = 
    "class DrJavaTestBaz extends DrJavaTestFoo {}";
  protected static final String FOO_MISSING_CLOSE_TEXT =
    "class DrJavaTestFoo {";

  protected static final String FOO_PACKAGE_AFTER_IMPORT =
    "import java.util.*;\npackage a;\n" + FOO_TEXT;

  protected static final String FOO_PACKAGE_INSIDE_CLASS =
    "class DrJavaTestFoo { package a; }";

  protected static final String FOO_PACKAGE_AS_FIELD =
    "class DrJavaTestFoo { int package; }";

  protected static final String FOO_PACKAGE_AS_FIELD_2 =
    "class DrJavaTestFoo { int package = 5; }";

  protected static final String FOO_PACKAGE_AS_PART_OF_FIELD =
    "class DrJavaTestFoo { int cur_package = 5; }";


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
  public void setUp() throws IOException {
    DrJava.getConfig().resetToDefaults();
    createModel();
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
    super.setUp();
  }

  /**
   * Teardown for each test case, which recursively deletes the
   * temporary directory created in setUp.
   */
  public void tearDown() throws IOException {
    boolean ret = FileOps.deleteDirectory(_tempDir);
    assertTrue("delete temp directory " + _tempDir, ret);
    super.tearDown();
    //_model.dispose();
    
    _tempDir = null;
    _model = null;
    System.gc();
  }

  /**
   * Instantiates the GlobalModel to be used in the test cases.
   */
  protected void createModel() {
    _model = new DefaultGlobalModel(_originalModel);
    //_model = new DefaultGlobalModel();
  }
  
  /**
   * Clear all old text and insert the given text.
   */
  protected void changeDocumentText(String s, OpenDefinitionsDocument doc)
    throws BadLocationException
  {
    Document document = doc.getDocument();
    document.remove(0, document.getLength());
    assertLength(0, doc);
    document.insertString(0, s, null);
    assertModified(true, doc);
    assertContents(s, doc);
  }

  /** Create a new temporary file in _tempDir. */
  protected File tempFile() throws IOException {
    return File.createTempFile("DrJava-test", ".java", _tempDir);
  }

  /** 
   * Create a new temporary file in _tempDir.  Calls with the same
   * int will return the same filename, while calls with different
   * ints will return different filenames.
   */
  protected File tempFile(int i) throws IOException {
    return File.createTempFile("DrJava-test" + i, ".java", _tempDir);
  }

  /** Create a new temporary directory in _tempDir. */
  protected File tempDirectory() throws IOException {
    return FileOps.createTempDirectory("DrJava-test", _tempDir);
  }

  protected File createFile(String name) {
    return new File(_tempDir, name);
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
    FileOps.writeStringToFile(temp, text);
    return temp;
  }


  /**
   * Creates and returns a new document, makes sure newFile is fired, and
   * then adds some text.  When this method is done newCount is reset to 0.
   * @return the new modified document
   */
  protected OpenDefinitionsDocument setupDocument(String text)
    throws BadLocationException
  {
    TestListener listener = new TestListener() {
      public void newFileCreated(OpenDefinitionsDocument doc) {
        newCount++;
      }
    };

    _model.addListener(listener);

    // Open a new document
    int numOpen = _model.getDefinitionsDocuments().getSize();
    OpenDefinitionsDocument doc = _model.newFile();
    assertNumOpenDocs(numOpen + 1);

    listener.assertNewCount(1);
    assertLength(0, doc);
    assertModified(false, doc);

    changeDocumentText(text, doc);
    _model.removeListener(listener);

    return doc;
  }
  
  /**
   * Compiles a new file with the given text.
   * The compile is expected to succeed and it is checked to make sure it worked
   * reasonably.  This method does not return until the Interactions JVM
   * has reset and is ready to use.
   * @param text Code for the class to be compiled
   * @param file File to save the class in
   * @return Document after it has been saved and compiled
   */
  protected synchronized OpenDefinitionsDocument doCompile(String text, File file)
    throws IOException, BadLocationException, InterruptedException
  {
    OpenDefinitionsDocument doc = setupDocument(text);
    doCompile(doc, file);
    return doc;
  }
  
  /**
   * Saves to the given file, and then compiles the given document.
   * The compile is expected to succeed and it is checked to make sure it worked
   * reasonably.  This method does not return until the Interactions JVM
   * has reset and is ready to use.
   * @param doc Document containing the code to be compiled
   * @param file File to save the class in
   */
  protected void doCompile(OpenDefinitionsDocument doc, File file)
    throws IOException, InterruptedException
  {
    doc.saveFile(new FileSelector(file));

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(true);
    _model.addListener(listener);
    synchronized(listener) {
      doc.startCompile();
      if (_model.getNumErrors() > 0) {
        fail("compile failed: " + doc.getCompilerErrorModel());
      }
      listener.wait();
    }
    listener.checkCompileOccurred();
    assertCompileErrorsPresent(false);
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
  protected String interpret(String input) throws DocumentAdapterException {
    DocumentAdapter interactionsDoc = _model.getInteractionsDocument();
    interactionsDoc.insertText(interactionsDoc.getDocLength(), input, 
                               InteractionsDocument.DEFAULT_STYLE);

    // skip 1 for newline
    final int resultsStartLocation = interactionsDoc.getDocLength() + 1;

    TestListener listener = new TestListener() {
      public void interactionStarted() {
        interactionStartCount++;
      }

      public void interactionEnded() {
        assertInteractionStartCount(1);

        synchronized(this) {
          interactionEndCount++;
          this.notify();
        }
      }
    };

    _model.addListener(listener);
    _model.interpretCurrentInteraction();

    // wait for interpret over
    while (listener.interactionEndCount == 0) {
      synchronized(listener) {
        try {
          listener.wait();
        }
        catch (InterruptedException ie) {
          throw new UnexpectedException(ie);
        }
      }
    }

    _model.removeListener(listener);
    listener.assertInteractionStartCount(1);
    listener.assertInteractionEndCount(1);

    // skip 1 for newline
    final int resultsEndLocation = interactionsDoc.getDocLength() - 1 -
                                   InteractionsDocument.PROMPT.length();

    final int resultsLen = resultsEndLocation - resultsStartLocation;
    //System.out.println("resultsStartLoc = " + resultsStartLocation + " resultsEndLocation = " + resultsEndLocation);
    // There was no output from this interaction
    if (resultsLen <= 0)
      return "";    
    return interactionsDoc.getDocText(resultsStartLocation, resultsLen);
  }

  protected void interpretIgnoreResult(String input) throws DocumentAdapterException
  {
    DocumentAdapter interactionsDoc = _model.getInteractionsDocument();
    interactionsDoc.insertText(interactionsDoc.getDocLength(), input, 
                               InteractionsDocument.DEFAULT_STYLE);

    _model.interpretCurrentInteraction();
  }
  
  /**
   * Asserts that the given string exists in the Interactions Document.
   */
  protected void assertInteractionsContains(String text) throws DocumentAdapterException{
    _assertInteractionContainsHelper(text, true);
  }
  
  /**
   * Asserts that the given string does not exist in the Interactions Document.
   */
  protected void assertInteractionsDoesNotContain(String text)
    throws DocumentAdapterException {
    _assertInteractionContainsHelper(text, false);
  }
  
  private void _assertInteractionContainsHelper(String text, boolean shouldContain)
    throws DocumentAdapterException {
    
    String interactText = getInteractionsText();
    int contains = interactText.lastIndexOf(text);
    assertTrue("Interactions document should " +
               (shouldContain ? "" : "not ")
                 + "contain: "
                 +text,
               (contains != -1) == shouldContain);    
  }
  
  /**
   * Returns the current contents of the interactions document
   */
  protected String getInteractionsText() throws DocumentAdapterException {
    DocumentAdapter doc = _model.getInteractionsDocument();
    return doc.getDocText(0, doc.getDocLength());
  }
  
  

  protected void assertNumOpenDocs(int num) {
    assertEquals("number of open documents",
                 num,
                 _model.getDefinitionsDocuments().getSize());
  }

  protected void assertModified(boolean b, OpenDefinitionsDocument doc) {
    assertEquals("document isModifiedSinceSave",
                 b,
                 doc.isModifiedSinceSave());
  }


  protected void assertLength(int len, OpenDefinitionsDocument doc)
    throws BadLocationException
  {
    assertEquals("document length",
                 len,
                 doc.getDocument().getLength());
  }

  protected void assertContents(String s, OpenDefinitionsDocument doc)
    throws BadLocationException
  {
    int len = doc.getDocument().getLength();

    assertEquals("document contents",
                 s,
                 doc.getDocument().getText(0, len));
  }

  protected void assertCompileErrorsPresent(boolean b) {
    assertCompileErrorsPresent("", b);
  }

  protected void assertCompileErrorsPresent(String name, boolean b) {
    //CompilerError[] errors = _model.getCompileErrors();
    int numErrors = _model.getNumErrors();

    if (name.length() > 0) {
      name += ": ";
    }

    //StringBuffer buf = new StringBuffer();
    //for (int i = 0; i < errors.length; i++) {
    //  buf.append("\nerror #" + i + ": " + errors[i]);
    //}

    assertEquals(name + "compile errors > 0? numErrors=" + numErrors,
                 b,
                 numErrors > 0);
  }
  
    // These exceptions are specially used only in this test case.
    // They are used to verify that the code blocks 
  public class OverwriteException extends RuntimeException{}
  public class OpenWarningException extends RuntimeException{}
  public class FileMovedWarningException extends RuntimeException{}
  
  public class WarningFileSelector implements FileOpenSelector, FileSaveSelector {
    private File _file;
    public WarningFileSelector(File f) {
      _file = f;
    }

    public File getFile() throws OperationCanceledException {
      return _file;
    }
    public File[] getFiles() throws OperationCanceledException {
      return new File[] {_file};
    }
    public void warnFileOpen(){
      throw new OpenWarningException();
    }
    public boolean verifyOverwrite(){
      throw new OverwriteException();
    }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, 
                                            File oldFile) {
      throw new FileMovedWarningException();
    }
  }

  /** 
   * this class is used by several test cases in Compile Tests that expect
   * incorrect behavior concerning the saving of files.  This special
   * FileSelector is included to ensure compliance with these test cases,
   * for which the intricacies of saving files are unimportant.
   *
   * The only FileSelector that honest-to-supreme-deity matters is
   * is DefaultGlobalModel.DefinitionsDocumentHandler, which is much
   * more like WarningFileSelector
   */
  
    public class FileSelector implements FileOpenSelector, FileSaveSelector {
    private File _file, _file2;
    public FileSelector(File f) {
      _file = f;
    }
    public FileSelector(File f1, File f2) {
      _file = f1;
      _file2 = f2;
    }

    public File getFile() throws OperationCanceledException {
      return _file;
    }
    public File[] getFiles() throws OperationCanceledException {
      if (_file2 != null) {
        return new File[] {_file, _file2};
      } else {
        return new File[] {_file};
      }
    }
    public void warnFileOpen(){
    }
    public boolean verifyOverwrite(){
      return true;
    }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc,
                                            File oldFile) {
      return true;
    }
  }
  
  public class CancelingSelector implements FileOpenSelector, FileSaveSelector
  {
    public File getFile() throws OperationCanceledException {
      throw new OperationCanceledException();
    }
    public File[] getFiles() throws OperationCanceledException {
      throw new OperationCanceledException();
    }
    public void warnFileOpen(){
    }
    public boolean verifyOverwrite(){
      return true;
    }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc,
                                            File oldFile) {
      return true;
    }
  }
  
  /**
   * A GlobalModelListener for testing.
   * By default it expects no events to be fired. To customize,
   * subclass and override one or more methods.
   */
  public static class TestListener implements GlobalModelListener {
    protected int newCount;
    protected int openCount;
    protected int closeCount;
    protected int saveCount;
    protected int canAbandonCount;
    protected int compileStartCount;
    protected int compileEndCount;
    protected int junitStartCount;
    protected int junitSuiteStartedCount;
    protected int junitTestStartedCount;
    protected int junitTestEndedCount;
    protected int junitEndCount;
    protected int interactionStartCount;
    protected int interactionEndCount;
    //protected int interactionCaretPositionChangedCount;
    protected int consoleResetCount;
    protected int interactionsResettingCount;
    protected int interactionsResetCount;
    protected int interactionsExitedCount;
    protected int saveAllBeforeProceedingCount;
    protected int nonTestCaseCount;
    protected int lastExitStatus;
    protected int fileRevertedCount;
    protected int shouldRevertFileCount;
    protected int undoableEditCount;

    public TestListener() {
      resetCounts();
    }

    public void resetCounts() {
      newCount = 0;
      openCount = 0;
      closeCount = 0;
      saveCount = 0;
      canAbandonCount = 0;
      compileStartCount = 0;
      compileEndCount = 0;
      junitStartCount = 0;
      junitSuiteStartedCount = 0;
      junitTestStartedCount = 0;
      junitTestEndedCount = 0;
      junitEndCount = 0;
      interactionStartCount = 0;
      interactionEndCount = 0;
      //interactionCaretPositionChangedCount = 0;
      consoleResetCount = 0;
      interactionsResettingCount = 0;
      interactionsResetCount = 0;
      saveAllBeforeProceedingCount = 0;
      nonTestCaseCount = 0;
      lastExitStatus = 0;
      fileRevertedCount = 0;
      shouldRevertFileCount = 0;
      undoableEditCount = 0;
    }
    
    /**
     * This method prints the failure message to System.err and
     * kills the JVM.  Just calling fail() doesn't always cause the
     * test to fail, because the listener is often called from another
     * thread.
     */
    protected void listenerFail(String s) {
      System.err.println("TEST FAILED: " + s);
      new AssertionFailedError(s).printStackTrace(System.err);
      _testFailed = true;
      fail(s);
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

    public void assertCloseCount(int i) {
      assertEquals("number of times closeFile fired", i, closeCount);
    }

    public void assertSaveCount(int i) {
      assertEquals("number of times saveFile fired", i, saveCount);
    }
    
    public void assertJUnitStartCount(int i) {
      assertEquals("number of times junitStarted fired", i, junitStartCount);
    }
    
    public void assertJUnitSuiteStartedCount(int i) {
      assertEquals("number of times junitSuiteStarted fired", i, junitSuiteStartedCount);
    }
    
    public void assertJUnitTestStartedCount(int i) {
      assertEquals("number of times junitTestStarted fired", i, junitTestStartedCount);
    }
    
    public void assertJUnitTestEndedCount(int i) {
      assertEquals("number of times junitTestEnded fired", i, junitTestEndedCount);
    }

    public void assertJUnitEndCount(int i) {
      assertEquals("number of times junitEnded fired", i, junitEndCount);
    }

    public void assertInteractionStartCount(int i) {
      assertEquals("number of times interactionStarted fired", i, interactionStartCount);
    }

    public void assertInteractionEndCount(int i) {
      assertEquals("number of times interactionEnded fired", i, interactionEndCount);
    }
    /*
    public void assertInteractionCaretPositionChangedCount(int i) {
      assertEquals("number of times interactionCaretPositionChanged fired", i, interactionCaretPositionChangedCount);
    }
    */
    public void assertCompileStartCount(int i) {
      assertEquals("number of times compileStarted fired", i, compileStartCount);
    }

    public void assertCompileEndCount(int i) {
      assertEquals("number of times compileEnded fired", i, compileEndCount);
    }

    public void assertInteractionsResettingCount(int i) {
      assertEquals("number of times interactionsResetting fired",
                   i,
                   interactionsResettingCount);
    }
    
    public void assertInteractionsResetCount(int i) {
      assertEquals("number of times interactionsReset fired",
                   i,
                   interactionsResetCount);
    }

    public void assertInteractionsExitedCount(int i) {
      assertEquals("number of times interactionsExited fired",
                   i,
                   interactionsExitedCount);
    }

    public void assertConsoleResetCount(int i) {
      assertEquals("number of times consoleReset fired",
                   i,
                   consoleResetCount);
    }

    public void assertSaveAllBeforeProceedingCount(int i) {
      assertEquals("number of times saveAllBeforeProceeding fired",
                   i,
                   saveAllBeforeProceedingCount);
    }
    
    public void assertNonTestCaseCount(int i) {
      assertEquals("number of times nonTestCase fired",
                   i,
                   nonTestCaseCount);
    }

    public void assertFileRevertedCount(int i) {
      assertEquals("number of times fileReverted fired",
                   i,
                   fileRevertedCount);
    }

    public void assertUndoableEditCount(int i) {
      assertEquals("number of times undoableEditHappened fired",
                   i,
                   undoableEditCount);
    }
    
    public void assertShouldRevertFileCount(int i) {
      assertEquals("number of times shouldRevertFile fired",
                   i,
                   shouldRevertFileCount);
    }

    public void newFileCreated(OpenDefinitionsDocument doc) {
      listenerFail("newFileCreated fired unexpectedly");
    }

    public void fileOpened(OpenDefinitionsDocument doc) {
      listenerFail("fileOpened fired unexpectedly");
    }

    public void fileClosed(OpenDefinitionsDocument doc) {
      listenerFail("fileClosed fired unexpectedly");
    }

    public void fileSaved(OpenDefinitionsDocument doc) {
      listenerFail("fileSaved fired unexpectedly");
    }

    public void fileReverted(OpenDefinitionsDocument doc) {
      listenerFail("fileReverted fired unexpectedly");
    }
    
    public void undoableEditHappened() {
      listenerFail("undoableEditHappened fired unexpectedly");
    }

    public void junitStarted(OpenDefinitionsDocument doc) {
      listenerFail("junitStarted fired unexpectedly");
    }

    public void junitSuiteStarted(int numTests) {
      listenerFail("junitSuiteStarted fired unexpectedly");
    }
  
    public void junitTestStarted(OpenDefinitionsDocument doc, String name) {
      listenerFail("junitTestStarted fired unexpectedly");
    }
  
    public void junitTestEnded(OpenDefinitionsDocument doc, String name,
                               boolean wasSuccessful, boolean causedError) {
      listenerFail("junitTestEnded fired unexpectedly");
    }
  
    public void junitEnded() {
      listenerFail("junitEnded fired unexpectedly");
    }

    public void interactionStarted() {
      listenerFail("interactionStarted fired unexpectedly");
    }

    public void interactionEnded() {
      listenerFail("interactionEnded fired unexpectedly");
    }
    /*
    public void interactionCaretPositionChanged(int pos) {
      listenerFail("interactionCaretPosition fired unexpectedly");
    }*/

    public void compileStarted() {
      listenerFail("compileStarted fired unexpectedly");
    }

    public void compileEnded() {
      listenerFail("compileEnded fired unexpectedly");
    }

    public void interactionsResetting() {
      listenerFail("interactionsResetting fired unexpectedly");
    }
    
    public void interactionsReset() {
      listenerFail("interactionsReset fired unexpectedly");
    }

    public void interactionsExited(int status) {
      listenerFail("interactionsExited(" + status + ") fired unexpectedly");
    }

    public void consoleReset() {
      listenerFail("consoleReset fired unexpectedly");
    }

    public void saveAllBeforeProceeding(GlobalModelListener.SaveReason reason) {
      listenerFail("saveAllBeforeProceeding fired unexpectedly");
    }

    public void nonTestCase() {
      listenerFail("nonTestCase fired unexpectedly");
    }

    public boolean canAbandonFile(OpenDefinitionsDocument doc) {
      listenerFail("canAbandonFile fired unexpectedly");

      // this is actually unreachable but the compiler won't believe me. sigh.
      throw new RuntimeException();
    }
    public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
      listenerFail("shouldRevertfile fired unexpectedly");

      // this is actually unreachable but the compiler won't believe me. sigh.
      throw new RuntimeException();
    }
  }

  
  /**
   * If users expect the Interactions to be reset after a compilation, they
   * must synchronize on this listener when compiling, then wait() on it.
   * The interactionsReset() method will notify().
   */
  public static class CompileShouldSucceedListener extends TestListener {
    private boolean _expectReset;
    
    /**
     * @param expectReset Whether to listen for interactions being
     * reset after a compiliation
     */
    public CompileShouldSucceedListener(boolean expectReset) {
      _expectReset = expectReset;
    }
    
    public void compileStarted() {
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      assertInteractionsResettingCount(0);
      assertInteractionsResetCount(0);
      assertConsoleResetCount(0);
      compileStartCount++;
    }

    public void compileEnded() {
      assertCompileEndCount(0);
      assertCompileStartCount(1);
      assertInteractionsResettingCount(0);
      assertInteractionsResetCount(0);
      assertConsoleResetCount(0);
      compileEndCount++;
    }

    public void interactionsResetting() {
      assertInteractionsResettingCount(0);
      assertInteractionsResetCount(0);
      assertCompileStartCount(1);
      assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      interactionsResettingCount++;
    }
    
    public void interactionsReset() {
      synchronized(this) {
        assertInteractionsResettingCount(1);
        assertInteractionsResetCount(0);
        assertCompileStartCount(1);
        assertCompileEndCount(1);
        // don't care whether interactions or console are reset first
        interactionsResetCount++;
        notify();
      }
    }

    public void consoleReset() {
      assertConsoleResetCount(0);
      assertCompileStartCount(1);
      assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      consoleResetCount++;
    }

    public void checkCompileOccurred() {
      assertCompileEndCount(1);
      assertCompileStartCount(1);
      if (_expectReset) {
        assertInteractionsResettingCount(1);
        assertInteractionsResetCount(1);
      }
      assertConsoleResetCount(1);
    }
  }
    
  /**
   * A model listener for situations expecting a compilation to fail.
   */
  public static class CompileShouldFailListener extends TestListener {
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

    public void checkCompileOccurred() {
      assertCompileEndCount(1);
      assertCompileStartCount(1);
    }
  }
  
}
