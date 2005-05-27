/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import java.rmi.registry.Registry;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.text.ConsoleInterface;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;

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

  protected DefaultGlobalModel _model;
  protected File _tempDir;
  
  protected boolean _junitDone;
  protected final Object _junitLock = new Object();
  
  protected void _logJUnitStart() { _junitDone = false; }
  
  protected void _runJUnit(OpenDefinitionsDocument doc) throws IOException, ClassNotFoundException, 
    InterruptedException {
    //    new ScrollableDialog(null, "Starting JUnit", "", "").show();
    _logJUnitStart();
    doc.startJUnit();
//    new ScrollableDialog(null, "JUnit Started", "", "").show();
    _waitJUnitDone();
  }
  
  protected void _runJUnit() throws IOException, ClassNotFoundException, InterruptedException {  
    _logJUnitStart();
    _model.getJUnitModel().junitAll();
    _waitJUnitDone();
  }
   
  protected void _waitJUnitDone() throws InterruptedException {
    synchronized(_junitLock) {
      while (!_junitDone) _junitLock.wait();
    }
  }
  
  protected static final String FOO_TEXT = "class DrJavaTestFoo {}";
  protected static final String BAR_TEXT = "class DrJavaTestBar {}";
  protected static final String BAZ_TEXT = "class DrJavaTestBaz extends DrJavaTestFoo { public static int x = 3; }";
  protected static final String FOO_MISSING_CLOSE_TEXT = "class DrJavaTestFoo {";
  protected static final String FOO_PACKAGE_AFTER_IMPORT = "import java.util.*;\npackage a;\n" + FOO_TEXT;
  protected static final String FOO_PACKAGE_INSIDE_CLASS = "class DrJavaTestFoo { package a; }";
  protected static final String FOO_PACKAGE_AS_FIELD = "class DrJavaTestFoo { int package; }";
  protected static final String FOO_PACKAGE_AS_FIELD_2 = "class DrJavaTestFoo { int package = 5; }";
  protected static final String FOO_PACKAGE_AS_PART_OF_FIELD = "class DrJavaTestFoo { int cur_package = 5; }";

  /** Setup for each test case, which does the following.
   *  <OL>
   *  <LI>
   *  Creates a new GlobalModel in {@link #_model} for each test case run.
   *  </LI>
   *  <LI>
   *  Creates a new temporary directory in {@link #_tempDir}.
   *  </LI>
   *  </OL>
   */
  public void setUp() throws IOException {
    DrJava.getConfig().resetToDefaults();
    createModel();
    _model.setResetAfterCompile(false);
    String user = System.getProperty("user.name");
    _tempDir = FileOps.createTempDirectory("DrJava-test-" + user);
//    FileOps.deleteDirectoryOnExit(_tempDir);
    super.setUp();
  }

  /** Teardown for each test case, which recursively deletes the temporary directory created in setUp.
   */
  public void tearDown() throws IOException {
    boolean ret = FileOps.deleteDirectory(_tempDir);
    assertTrue("delete temp directory " + _tempDir, ret);

    _model.dispose();
    _tempDir = null;
    _model = null;
    
    super.tearDown();
  }

  /** Instantiates the GlobalModel to be used in the test cases. */
  protected void createModel() {
    //_model = new DefaultSingleDisplayModel(_originalModel);
    _model = new DefaultSingleDisplayModel();

    // Wait until it has connected
    _model._interpreterControl.ensureInterpreterConnected();
    // Wait until all pending events have finished
  }

  /** Clear all old text and insert the given text. */
  protected void changeDocumentText(String s, OpenDefinitionsDocument doc) throws BadLocationException {
    doc.clear();
    assertLength(0, doc);
    doc.insertString(0, s, null);
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

  /** Given a .java file and a class file name, returns the corresponding .class file. */
  protected File classForJava(File sourceFile, String className) {
    assertTrue(sourceFile.getName().endsWith(".java"));
    String cname = className + ".class";
    return new File(sourceFile.getParent(), cname);
  }

  /** Creates a new temporary file and writes the given text to it.
   *  The File object for the new file is returned.
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
  protected OpenDefinitionsDocument setupDocument(String text) throws BadLocationException {
    TestListener listener = new TestListener() {
      public void newFileCreated(OpenDefinitionsDocument doc) { newCount++; }
    };

    _model.addListener(listener);

    // Open a new document
    int numOpen = _model.getOpenDefinitionsDocuments().size();
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
  protected void doCompile(OpenDefinitionsDocument doc, File file) throws IOException, 
    InterruptedException {
    doc.saveFile(new FileSelector(file));

    // Perform a mindless interpretation to force interactions to reset.
    //  (only to simplify this method)
    try { interpret("2+2"); }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }

    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(true);
    _model.setResetAfterCompile(true);
    _model.addListener(listener);
    synchronized(listener) {
      doc.startCompile();
      if (_model.getCompilerModel().getNumErrors() > 0) {
        fail("compile failed: " + getCompilerErrorString());
      }
      listener.wait();
    }
    listener.checkCompileOccurred();
    assertCompileErrorsPresent(false);
    _model.removeListener(listener);
  }

  /** Returns a string with all compiler errors. */
  protected String getCompilerErrorString() {
    StringBuffer buf = new StringBuffer();
    buf.append(" compiler error(s):\n");
    buf.append(_model.getCompilerModel().getCompilerErrorModel().toString());
    return buf.toString();
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
    InteractionsDocument interactionsDoc = _model.getInteractionsDocument();
    interactionsDoc.insertText(interactionsDoc.getDocLength(), input,
                               InteractionsDocument.DEFAULT_STYLE);

    // skip the right length for the newline
    int newLineLen = System.getProperty("line.separator").length();
    final int resultsStartLocation = interactionsDoc.getDocLength() + newLineLen;

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
    try {
      synchronized(listener) {
        _model.interpretCurrentInteraction();
        listener.wait();
 /**///In previous versions of 1.5.0-beta compiler, several tests hang right here, because 
 /////in DebugContextTest and JavaDebugInterpreterTest, the files that were being tested, 
 /////for example, MonkeyStuff.java, was being compiled without the -g flag, so debugging was 
 /////impossible. This happened because of a bug in the 1.5 compiler, which is now fixed in the new
 /////version.
      }
    }
    catch (InterruptedException ie) {
      throw new UnexpectedException(ie);
    }
    _model.removeListener(listener);
    listener.assertInteractionStartCount(1);
    listener.assertInteractionEndCount(1);

    // skip the right length for the newline
    final int resultsEndLocation = interactionsDoc.getDocLength() - newLineLen -
                                   interactionsDoc.getPrompt().length();

    final int resultsLen = resultsEndLocation - resultsStartLocation;
    //System.out.println("resultsStartLoc = " + resultsStartLocation + " resultsEndLocation = " + resultsEndLocation);
    // There was no output from this interaction
    if (resultsLen <= 0)
      return "";
    return interactionsDoc.getDocText(resultsStartLocation, resultsLen);
  }

  protected void interpretIgnoreResult(String input) throws DocumentAdapterException {
    ConsoleInterface interactionsDoc = _model.getInteractionsDocument();
    interactionsDoc.insertText(interactionsDoc.getDocLength(), input, InteractionsDocument.DEFAULT_STYLE);

    _model.interpretCurrentInteraction();
  }

  /**
   * Asserts that the given string exists in the Interactions Document.
   */
  protected void assertInteractionsContains(String text) throws DocumentAdapterException {
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
                 + "contain:\n"
                 + text
                 + "\nActual contents of Interactions document:\n"
                 + interactText,
               (contains != -1) == shouldContain);
  }

  /** Returns the current contents of the interactions document */
  protected String getInteractionsText() throws DocumentAdapterException {
    ConsoleInterface doc = _model.getInteractionsDocument();
    return doc.getDocText(0, doc.getDocLength());
  }



  protected void assertNumOpenDocs(int num) {
    assertEquals("number of open documents",
                 num,
                 _model.getOpenDefinitionsDocuments().size());
  }

  protected void assertModified(boolean b, OpenDefinitionsDocument doc) {
    assertEquals("document isModifiedSinceSave", b, doc.isModifiedSinceSave());
  }


  protected void assertLength(int len, OpenDefinitionsDocument doc) {
    assertEquals("document length", len, doc.getLength());
  }

  protected void assertContents(String s, OpenDefinitionsDocument doc) throws BadLocationException {
    assertEquals("document contents", s, doc.getText());
  }

  protected void assertCompileErrorsPresent(boolean b) { assertCompileErrorsPresent("", b); }

  protected void assertCompileErrorsPresent(String name, boolean b) {
    //CompilerError[] errors = _model.getCompileErrors();
    int numErrors = _model.getCompilerModel().getNumErrors();

    if (name.length() > 0)  name += ": ";

    //StringBuffer buf = new StringBuffer();
    //for (int i = 0; i < errors.length; i++) {
    //  buf.append("\nerror #" + i + ": " + errors[i]);
    //}

    assertEquals(name + "compile errors > 0? numErrors=" + numErrors, b, numErrors > 0);
  }

    // These exceptions are specially used only in this test case.
    // They are used to verify that the code blocks
  public static class OverwriteException extends RuntimeException{ }
  public static class OpenWarningException extends RuntimeException{ }
  public static class FileMovedWarningException extends RuntimeException{ }

  public static class WarningFileSelector implements FileOpenSelector, FileSaveSelector {
    private File _file;
    public WarningFileSelector(File f) { _file = f; }
    public File getFile() throws OperationCanceledException { return _file; }
    public File[] getFiles() throws OperationCanceledException { return new File[] {_file}; }
    public boolean warnFileOpen(File f) { throw new OpenWarningException(); }
    public boolean verifyOverwrite() { throw new OverwriteException(); }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {
      throw new FileMovedWarningException();
    }
  }

  /** This class is used by several test cases in Compile Tests that expect incorrect behavior concerning the saving 
   *  of files.  This special FileSelector is included to ensure compliance with these test cases, for which the 
   *  intricacies of saving files are unimportant.  The only FileSelector that honest-to-supreme-deity matters is
   *  is DefaultGlobalModel.ConcreteOpenDefDoc, which is much more like WarningFileSelector
   */

  public static class FileSelector implements FileOpenSelector, FileSaveSelector {
    private File _file, _file2;
    public FileSelector(File f) { _file = f; }
    public FileSelector(File f1, File f2) {
      _file = f1;
      _file2 = f2;
    }

    public File getFile() throws OperationCanceledException { return _file; }
    
    public File[] getFiles() throws OperationCanceledException {
      if (_file2 != null) return new File[] {_file, _file2};
      else return new File[] {_file};
    }
    public boolean warnFileOpen(File f) { return true; }
    public boolean verifyOverwrite() { return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {
      return true;
    }
  }

  public static class CancelingSelector implements FileOpenSelector, FileSaveSelector {
    public File getFile() throws OperationCanceledException {
      throw new OperationCanceledException();
    }
    public File[] getFiles() throws OperationCanceledException {
      throw new OperationCanceledException();
    }
    public boolean warnFileOpen(File f) { return true; }
    public boolean verifyOverwrite() {return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {  return true; }
  }

  /** A GlobalModelListener for testing. By default it expects no events to be fired. To customize,
   *  subclass and override one or more methods.
   */
  public static class TestListener implements GlobalModelListener {
    /** Remembers when this listener was created. */
    protected Exception _startupTrace;
    protected int fileNotFoundCount;
    protected int newCount;
    protected int openCount;
    protected int closeCount;
    protected int saveCount;
    protected int canAbandonCount;
    protected int quitFileCount;
    protected int classFileErrorCount;
    protected int compileStartCount;
    protected int compileEndCount;
    protected int runStartCount;
    protected int junitStartCount;
    protected int junitSuiteStartedCount;
    protected int junitTestStartedCount;
    protected int junitTestEndedCount;
    protected int junitEndCount;
    protected int interactionStartCount;
    protected int interactionEndCount;
    protected int interactionErrorCount;
    protected int interpreterResettingCount;
    protected int interpreterReadyCount;
    protected int interpreterExitedCount;
    protected int interpreterResetFailedCount;
    protected int interpreterChangedCount;
    //protected int interactionCaretPositionChangedCount;
    protected int consoleResetCount;
    protected int saveBeforeCompileCount;
    //protected int saveBeforeRunCount;
    //protected int saveBeforeJUnitCount;
    protected int saveBeforeJavadocCount;
    //protected int saveBeforeDebugCount;
    protected int nonTestCaseCount;
    protected int lastExitStatus;
    protected int fileRevertedCount;
    protected int shouldRevertFileCount;
    protected int undoableEditCount;
    protected int interactionIncompleteCount;
    protected int filePathContainsPoundCount;

    public TestListener() {
      _startupTrace = new Exception();
      resetCounts();
    }

    public void resetCounts() {
      fileNotFoundCount = 0;
      newCount = 0;
      openCount = 0;
      closeCount = 0;
      saveCount = 0;
      canAbandonCount = 0;
      quitFileCount = 0;
      classFileErrorCount = 0;
      compileStartCount = 0;
      compileEndCount = 0;
      runStartCount = 0;
      junitStartCount = 0;
      junitSuiteStartedCount = 0;
      junitTestStartedCount = 0;
      junitTestEndedCount = 0;
      junitEndCount = 0;
      interactionStartCount = 0;
      interactionEndCount = 0;
      interactionErrorCount = 0;
      interpreterChangedCount = 0;
      //interactionCaretPositionChangedCount = 0;
      consoleResetCount = 0;
      interpreterResettingCount = 0;
      interpreterReadyCount = 0;
      interpreterExitedCount = 0;
      interpreterResetFailedCount = 0;
      saveBeforeCompileCount = 0;
      //saveBeforeRunCount = 0;
      //saveBeforeJUnitCount = 0;
      saveBeforeJavadocCount = 0;
      //saveBeforeDebugCount = 0;
      nonTestCaseCount = 0;
      lastExitStatus = 0;
      fileRevertedCount = 0;
      shouldRevertFileCount = 0;
      undoableEditCount = 0;
      interactionIncompleteCount = 0;
      filePathContainsPoundCount = 0;
    }

    
    public void projectModified() { }
    public void projectOpened(File pfile, FileOpenSelector files) { }
    public void projectClosed() { }
    public void projectBuildDirChanged() { }
    public void projectRunnableChanged() { }
    
    public void currentDirectoryChanged(File dir) { }
    
    /** Appends the stack trace from the listener's creation to the end of the given failure message. */
    public void listenerFail(String message) {
      String header = "\nTestListener creation stack trace:\n" +
        StringOps.getStackTrace(_startupTrace);
      MultiThreadedTestCase.listenerFail(message + header);
    }

    public void assertFileNotFoundCount(int i) {
      assertEquals("number of times fileNotFound fired", i, fileNotFoundCount);
    }

    public void assertAbandonCount(int i) {
      assertEquals("number of times canAbandon fired", i, canAbandonCount);
    }

     public void assertQuitFileCount(int i) {
      assertEquals("number of times quitFile fired", i, quitFileCount);
    }
     public void assertClassFileErrorCount(int i) {
      assertEquals("number of times classFileError fired", i, classFileErrorCount);
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

    public void assertInteractionErrorCount(int i) {
      assertEquals("number of times interactionError fired", i, interactionErrorCount );
    }

    public void assertInterpreterChangedCount(int i) {
      assertEquals("number of times interpreterChanged fired", i, interpreterChangedCount);
    }

//    /** Not used */
//    public void assertInteractionCaretPositionChangedCount(int i) {
//      assertEquals("number of times interactionCaretPositionChanged fired", i, interactionCaretPositionChangedCount);
//    }

    public void assertCompileStartCount(int i) {
      assertEquals("number of times compileStarted fired", i, compileStartCount);
    }

    public void assertCompileEndCount(int i) {
      assertEquals("number of times compileEnded fired", i, compileEndCount);
    }

    public void assertRunStartCount(int i) {
      assertEquals("number of times runStarted fired", i, runStartCount);
    }

    public void assertInterpreterResettingCount(int i) {
      assertEquals("number of times interactionsResetting fired", i, interpreterResettingCount);
    }

    public void assertInterpreterReadyCount(int i) {
      assertEquals("number of times interactionsReset fired", i, interpreterReadyCount);
    }

    public void assertInterpreterResetFailedCount(int i) {
      assertEquals("number of times interactionsResetFailed fired", i, interpreterResetFailedCount);
    }

    public void assertInterpreterExitedCount(int i) {
      assertEquals("number of times interpreterExited fired", i, interpreterExitedCount);
    }

    public void assertInteractionsErrorCount(int i) {
      assertEquals("number of times interactionsError fired", i, interactionErrorCount);
    }

    public void assertConsoleResetCount(int i) {
      assertEquals("number of times consoleReset fired", i, consoleResetCount);
    }

    public void assertSaveBeforeCompileCount(int i) {
      assertEquals("number of times saveBeforeCompile fired", i, saveBeforeCompileCount);
    }

//    /** Not used.*/
//    public void assertSaveBeforeRunCount(int i) {
//      assertEquals("number of times saveBeforeRun fired", i, saveBeforeRunCount);
//    }
//
//    /** Not used. */
//    public void assertSaveBeforeJUnitCount(int i) {
//      assertEquals("number of times saveBeforeJUnit fired", i, saveBeforeJUnitCount);
//    }

    public void assertSaveBeforeJavadocCount(int i) {
      assertEquals("number of times saveBeforeJavadoc fired", i, saveBeforeJavadocCount);
    }

//    /** Not used. */
//    public void assertSaveBeforeDebugCount(int i) {
//      assertEquals("number of times saveBeforeDebug fired",
//                   i,
//                   saveBeforeDebugCount);
//    }

    public void assertNonTestCaseCount(int i) {
      assertEquals("number of times nonTestCase fired", i,  nonTestCaseCount);
    }

    public void assertFileRevertedCount(int i) {
      assertEquals("number of times fileReverted fired", i, fileRevertedCount);
    }

    public void assertUndoableEditCount(int i) {
      assertEquals("number of times undoableEditHappened fired", i, undoableEditCount);
    }

    public void assertShouldRevertFileCount(int i) {
      assertEquals("number of times shouldRevertFile fired", i, shouldRevertFileCount);
    }

    public void assertInteractionIncompleteCount(int i) {
      assertEquals("number of times interactionIncomplete fired", i, interactionIncompleteCount);
    }

    public void newFileCreated(OpenDefinitionsDocument doc) {
      listenerFail("newFileCreated fired unexpectedly");
    }
    
    public void fileNotFound(File f) {
      listenerFail("fileNotFound fired unexpectedly");
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

    public void junitStarted(List<OpenDefinitionsDocument> doc) {
      listenerFail("junitStarted fired unexpectedly");
    }

    public void junitAllStarted() {
      listenerFail("junitAllStarted fired unexpectedly");
    }

    public void junitSuiteStarted(int numTests) {
      listenerFail("junitSuiteStarted fired unexpectedly");
    }

    public void junitTestStarted(String name) {
      listenerFail("junitTestStarted fired unexpectedly");
    }

    public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
      listenerFail("junitTestEnded fired unexpectedly");
    }

    public void junitEnded() {
      listenerFail("junitEnded fired unexpectedly");
    }

    public void javadocStarted() {
      listenerFail("javadocStarted fired unexpectedly");
    }

    public void javadocEnded(boolean success, File destDir, boolean allDocs) {
      listenerFail("javadocEnded fired unexpectedly");
    }

    public void interactionStarted() {
      listenerFail("interactionStarted fired unexpectedly");
    }

    public void interactionEnded() {
      listenerFail("interactionEnded fired unexpectedly");
    }

    public void interactionErrorOccurred(int offset, int length) {
      listenerFail("interpreterErrorOccurred fired unexpectedly");
    }

    public void interpreterChanged(boolean inProgress) {
      listenerFail("interpreterChanged fired unexpectedly");
    }

//    /**Not used */
//    public void interactionCaretPositionChanged(int pos) {
//      listenerFail("interactionCaretPosition fired unexpectedly");
//    }

    public void compileStarted() {
      listenerFail("compileStarted fired unexpectedly");
    }

    public void compileEnded() {
      listenerFail("compileEnded fired unexpectedly");
    }

    public void runStarted(OpenDefinitionsDocument doc) {
      listenerFail("runStarted fired unexpectedly");
    }

    public void interpreterResetting() {
      listenerFail("interactionsResetting fired unexpectedly");
    }

    public void interpreterReady() {
      listenerFail("interactionsReset fired unexpectedly");
    }

    public void interpreterExited(int status) {
      listenerFail("interpreterExited(" + status + ") fired unexpectedly");
    }

    public void interpreterResetFailed(Throwable t) {
      listenerFail("interpreterResetFailed fired unexpectedly");
    }

    public void consoleReset() {
      listenerFail("consoleReset fired unexpectedly");
    }

    public void saveBeforeCompile() {
      listenerFail("saveBeforeCompile fired unexpectedly");
    }

//    /** Not used. */
//    public void saveBeforeRun() {
//      listenerFail("saveBeforeRun fired unexpectedly");
//    }

//    /** Not used. */
//    public void saveBeforeJUnit() {
//      listenerFail("saveBeforeJUnit fired unexpectedly");
//    }

    public void saveBeforeJavadoc() {
      listenerFail("saveBeforeJavadoc fired unexpectedly");
    }
    
//    /** Not used. */
//    public void saveBeforeDebug() {
//      listenerFail("saveBeforeDebug fired unexpectedly");
//    }

    public void nonTestCase(boolean isTestAll) {
      listenerFail("nonTestCase fired unexpectedly");
    }

    public boolean canAbandonFile(OpenDefinitionsDocument doc) {
      listenerFail("canAbandonFile fired unexpectedly");
      throw new UnexpectedException();
    }
    
    public void quitFile(OpenDefinitionsDocument doc) {
      listenerFail("quitFile fired unexpectedly");
      throw new UnexpectedException();
    }
    
    public void classFileError(ClassFileError e) {
      listenerFail("classFileError fired unexpectedly");
    }
    
    public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
      listenerFail("shouldRevertfile fired unexpectedly");
      throw new UnexpectedException();
    }

    public void interactionIncomplete() {
      listenerFail("interactionIncomplete fired unexpectedly");
    }

    public void filePathContainsPound() {
      listenerFail("filePathContainsPound fired unexpectedly");
    }
    
    public void documentNotFound(OpenDefinitionsDocument d, File f) {
      listenerFail("documentNotFound fired unexpectedly");
    }
    
    public void activeDocumentChanged(OpenDefinitionsDocument active) {
      // listenerFail("activeDocumentChanged fired unexpectedly"); // this event is not tested !!
    }
  }


  /** If users expect the Interactions to be reset after a compilation, they must synchronize on this listener 
   *  when compiling, then wait() on it. The interactionsReset() method will notify().
   */
  public static class CompileShouldSucceedListener extends TestListener {
    private boolean _expectReset;

    /** Reset after a compilation.
     *  @param expectReset Whether to listen for interactions being
     */
    public CompileShouldSucceedListener(boolean expectReset) { _expectReset = expectReset; }

    public void compileStarted() {
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      compileStartCount++;
    }

    public void compileEnded() {
      assertCompileEndCount(0);
      assertCompileStartCount(1);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      compileEndCount++;
    }

    public void interpreterResetting() {
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertCompileStartCount(1);
      assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      interpreterResettingCount++;
    }

    public void interpreterReady() {
      synchronized(this) {
        assertInterpreterResettingCount(1);
        assertInterpreterReadyCount(0);
        assertCompileStartCount(1);
        assertCompileEndCount(1);
        // don't care whether interactions or console are reset first
        interpreterReadyCount++;
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
        assertInterpreterResettingCount(1);
        assertInterpreterReadyCount(1);
      }
      else {
        assertInterpreterResettingCount(0);
        assertInterpreterReadyCount(0);
      }

      // Note: console is no longer reset after a compile
      //assertConsoleResetCount(1);
    }
  }

  /** A model listener for situations expecting a compilation to fail. */
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
  
  public class JUnitNonTestListener extends JUnitTestListener {
    private boolean _shouldBeTestAll;
    public JUnitNonTestListener() {
      _shouldBeTestAll = false;
    }
    public JUnitNonTestListener(boolean shouldBeTestAll) {
      _shouldBeTestAll = shouldBeTestAll;
    }
    public void nonTestCase(boolean isTestAll) {
      nonTestCaseCount++;
      assertEquals("Non test case heard the wrong value for test current/test all",
                   _shouldBeTestAll, isTestAll);
      synchronized(_junitLock) {
        _junitDone = true;
        _junitLock.notify();
      }
    }
  }

  public class JUnitTestListener extends CompileShouldSucceedListener {
    // handle System.out's separately but default to outter class's printMessage value
    protected boolean printMessages = GlobalModelJUnitTest.printMessages; 
    public JUnitTestListener() {
      this(false,false);  // don't reset interactions after compile by default
    }
    public JUnitTestListener(boolean shouldResetAfterCompile) { 
      this(shouldResetAfterCompile,false); 
    }
    public JUnitTestListener(boolean shouldResetAfterCompile, boolean printListenerMessages) {
      super(shouldResetAfterCompile);
      this.printMessages = printListenerMessages;
    }
    public void junitStarted(List<OpenDefinitionsDocument> odds) {
      if (printMessages) System.out.println("listener.junitStarted");
      junitStartCount++;
    }
    public void junitSuiteStarted(int numTests) {
      if (printMessages) System.out.println("listener.junitSuiteStarted, numTests="+numTests);
      assertJUnitStartCount(1);
      junitSuiteStartedCount++;
    }
    public void junitTestStarted(String name) {
      if (printMessages) System.out.println("  listener.junitTestStarted, "+name);
      junitTestStartedCount++;
    }
    public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
      if (printMessages) System.out.println("  listener.junitTestEnded, name="+name+" succ="+wasSuccessful+" err="+causedError);
      junitTestEndedCount++;
      assertEquals("junitTestEndedCount should be same as junitTestStartedCount",
                   junitTestEndedCount, junitTestStartedCount);
    }
    public void nonTestCase(boolean isTestAll) {
      if (printMessages) System.out.println("listener.nonTestCase, isTestAll="+isTestAll);
      nonTestCaseCount++;
      synchronized(_junitLock) {
        _junitDone = true;
        _junitLock.notify();
      }
    }
    public void classFileError(ClassFileError e) {
      if (printMessages) System.out.println("listener.classFileError, e="+e);
      classFileErrorCount++;
      synchronized(_junitLock) {
        _junitDone = true;
        _junitLock.notify();
      }
    }
    public synchronized void junitEnded() {
      //assertJUnitSuiteStartedCount(1);
      if (printMessages) System.out.println("junitEnded event!");
      junitEndCount++;
      synchronized(_junitLock) {
        _junitDone = true;
        _junitLock.notify();
      }
    }
  }
}
