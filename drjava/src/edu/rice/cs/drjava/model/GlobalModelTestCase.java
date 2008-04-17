/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.drjava.DrJava;

import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.ui.InteractionsController;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.swing.AsyncTask;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.text.EditDocumentInterface;

import javax.swing.text.BadLocationException;
import java.io.File;
import java.io.IOException;
import java.util.regex.*;
import java.util.List;

/** Base class for tests over the {@link GlobalModel}.
 *
 *  This class provides a number of convenience methods for testing the GlobalModel. It also contains a model instance 
 *  (reset in {@link #setUp} and a temporary directory that's created per test invocation (and subsequently cleaned in
 *  {@link #tearDown}. This reduces the burden for such file management stuff in the test cases themselves.
 *
 *  @version $Id$
 */
public abstract class GlobalModelTestCase extends MultiThreadedTestCase {
  
  protected static final Log _log  = new Log("GlobalModel.txt", false);

  protected volatile DefaultGlobalModel _model;
  protected volatile InteractionsController _interactionsController;
  protected volatile File _tempDir;
  protected volatile OpenDefinitionsDocument _doc;  // the working document in some shared set up routines

  protected static final String FOO_TEXT = "class DrJavaTestFoo {}";
  protected static final String BAR_TEXT = "class DrJavaTestBar {}";
  protected static final String BAZ_TEXT = "class DrJavaTestBaz extends DrJavaTestFoo { public static int x = 3; }";
  protected static final String FOO_MISSING_CLOSE_TEXT = "class DrJavaTestFoo {";
  protected static final String FOO_PACKAGE_AFTER_IMPORT = "import java.util.*;\npackage a;\n" + FOO_TEXT;
  protected static final String FOO_PACKAGE_INSIDE_CLASS = "class DrJavaTestFoo { package a; }";
  protected static final String FOO_PACKAGE_AS_FIELD = "class DrJavaTestFoo { int package; }";
  protected static final String FOO_PACKAGE_AS_FIELD_2 = "class DrJavaTestFoo { int package = 5; }";
  protected static final String FOO_PACKAGE_AS_PART_OF_FIELD = "class DrJavaTestFoo { int cur_package = 5; }";
  
  public GlobalModelTestCase() { _log.log("Constructing a " + this); }

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
  public void setUp() throws Exception {
    _log.log("Setting up " + this);
    super.setUp();
    _model = new TestGlobalModel();
    // create an interactions pane which is essential to the function of the interactions model; 
    _interactionsController =  // InteractionsController constructor creates an interactions pane
      new InteractionsController(_model.getInteractionsModel(), _model.getSwingInteractionsDocument());
    _log.log("Global model created for " + this);
    DrJava.getConfig().resetToDefaults();
    String user = System.getProperty("user.name");
    
    _tempDir = /* IOUtil.createAndMarkTempDirectory */ FileOps.createTempDirectory("DrJava-test-" + user /*, ""*/);
//    System.err.println("Temp Directory is " + _tempDir.getAbsolutePath());
    
    // Wait until model has connected to slave JVM
    _log.log("Ensuring that interpreter is connected in " + this);
    _model._jvm.ensureInterpreterConnected();
    _log.log("Ensured that intepreter is connected in " + this);
    _model.setResetAfterCompile(false);
    _log.log("Completed (GlobalModelTestCase) set up of " + this);

//    _model.getOpenDefinitionsDocuments().get(0).saveFile(new FileSelector(new File(_tempDir, "blank document")));
//    super.setUp();
  }

  /** Teardown for each test case, which recursively deletes the temporary directory created in setUp. */
  public void tearDown() throws Exception {
    _log.log("Tearing down " + this);
    _model.dispose();

    /*boolean ret =*/ IOUtil.deleteOnExitRecursively(_tempDir);
    //assertTrue("delete temp directory " + _tempDir, ret);

    _tempDir = null;
    _model = null;

    super.tearDown();
    _log.log("Completed tear down of " + this);
  }

  /** Clear all old text and insert the given text. */
  protected void changeDocumentText(String s, OpenDefinitionsDocument doc) throws BadLocationException {
    doc.clear();
    assertLength(0, doc);
    doc.append(s, null);
    assertModified(true, doc);
    assertContents(s, doc);
  }

  /** Create a new temporary file in _tempDir. */
  protected File tempFile() throws IOException {
    File f = File.createTempFile("DrJava-test", ".java", _tempDir).getCanonicalFile();
//    System.err.println("temp file created with name " + f);
    return f;
  }

  /** Create a new temporary file in _tempDir.  Calls with the same int will return the same filename, while calls
   *  with different ints will return different filenames.
   */
  protected File tempFile(int i) throws IOException {
    return File.createTempFile("DrJava-test" + i, ".java", _tempDir).getCanonicalFile();
  }

  /** Create a new temporary directory in _tempDir. */
  protected File tempDirectory() throws IOException {
    return IOUtil.createAndMarkTempDirectory("DrJava-test", "", _tempDir);
  }

  protected File createFile(String name) { return new File(_tempDir, name); }

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
    IOUtil.writeStringToFile(temp, text);
    return temp;
  }

  /** Creates and returns a new document, makes sure newFile is fired, and then adds some text.  When this method is
   *  done newCount is reset to 0.
   *  @return the new modified document
   */
  protected OpenDefinitionsDocument setupDocument(String text) throws BadLocationException {
    TestListener listener = new TestListener() {
      public synchronized void newFileCreated(OpenDefinitionsDocument doc) { newCount++; }
    };

    _model.addListener(listener);

    // Open a new document
    int numOpen = _model.getOpenDefinitionsDocuments().size();
    Utilities.invokeAndWait(new Runnable() { public void run () { _doc = _model.newFile(); } });
    
    assertNumOpenDocs(numOpen + 1);

    listener.assertNewCount(1);
    assertLength(0, _doc);
    assertModified(false, _doc);

    changeDocumentText(text, _doc);
    assertModified(true, _doc);
    _model.removeListener(listener); 

    return _doc;
  }

  /** Compiles a new file with the given text. The compile is expected to succeed and it is checked to make sure it
   *  worked reasonably.  This method does not return until the Interactions JVM has reset and is ready to use.
   *  @param text Code for the class to be compiled
   *  @param file File to save the class in
   *  @return Document after it has been saved and compiled
   */
  protected synchronized OpenDefinitionsDocument doCompile(String text, File file) throws IOException, 
    BadLocationException, InterruptedException {
    
    OpenDefinitionsDocument doc = setupDocument(text);
    doCompile(doc, file);
    return doc;
  }

  /** Saves to the given file, and then compiles the given document. The compile is expected to succeed and it is 
   *  checked to make sure it worked reasonably.  This method does not return until the Interactions JVM has reset
   *  and is ready to use.
   *  @param doc Document containing the code to be compiled
   *  @param file File to save the class in
   */
  protected void doCompile(OpenDefinitionsDocument doc, File file) throws IOException,  InterruptedException {
    doc.saveFile(new FileSelector(file));

    // Perform a mindless interpretation to force interactions to reset (only to simplify this method)
    try { interpret("0"); }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
    Utilities.clearEventQueue();
    
    _model.setResetAfterCompile(true);
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener(true);
    _model.addListener(listener);
    
    listener.logCompileStart();

    doc.startCompile();
    Utilities.clearEventQueue();
    
    if (_model.getCompilerModel().getNumErrors() > 0)  fail("compile failed: " + getCompilerErrorString());

    listener.waitCompileDone();

    listener.checkCompileOccurred();
    assertCompileErrorsPresent(false);
    
    listener.waitResetDone();
    Utilities.clearEventQueue();
    _model.removeListener(listener);
  }

  /** Returns a string with all compiler errors. */
  protected String getCompilerErrorString() {
    final StringBuilder buf = new StringBuilder();
    buf.append(" compiler error(s):\n");
    buf.append(_model.getCompilerModel().getCompilerErrorModel().toString());
    return buf.toString();
  }

  /** Puts the given input into the interactions document and then interprets it, returning the result that was put
    * into the interactions document. This assumes the interactions document is in a state with no text after the 
    * prompt. To be sure this is the case, you can reset interactions first.  This method provides its own listener
    * to synchronized with the completion of the interaction.
    *
    * @param input text to interpret
    * @return The output from this interpretation, in String form, as it was printed to the interactions document.
    */
  protected String interpret(String input) throws EditDocumentException {
    
    InteractionsDocument interactionsDoc = _model.getInteractionsDocument();
    Utilities.clearEventQueue();
    
    interactionsDoc.setInProgress(false);  // for some reason, the inProgress state can be true when interpret is invoked
    interactionsDoc.append(input, InteractionsDocument.DEFAULT_STYLE);
    
    Utilities.clearEventQueue();

    // skip the right length for the newline
    final int newLineLen = 1; // Was StringOps.EOL.length(); but Swing uses '\n' for newLine
    final int resultsStartLocation = interactionsDoc.getLength() + newLineLen;

    InteractionListener listener = new InteractionListener();

    _model.addListener(listener);
    listener.logInteractionStart();
    try {
      _model.interpretCurrentInteraction();
      listener.waitInteractionDone();
    }
    catch (InterruptedException ie) { throw new UnexpectedException(ie); }
    Utilities.clearEventQueue();
    _model.removeListener(listener);
    
    listener.assertInteractionStartCount(1);
    listener.assertInteractionEndCount(1);

    // skip the right length for the newline
    interactionsDoc.acquireReadLock();
    try {
      final int resultsEndLocation = interactionsDoc.getLength() - newLineLen - interactionsDoc.getPrompt().length();
      
      final int resultsLen = resultsEndLocation - resultsStartLocation;
      _log.log("resultsStartLoc = " + resultsStartLocation + " resultsEndLocation = " + resultsEndLocation);
      _log.log("Contents = '" + interactionsDoc.getDocText(0, resultsEndLocation+1) + "'");
      if (resultsLen <= 0) return "";
      return interactionsDoc.getDocText(resultsStartLocation, resultsLen);
    }
    finally { interactionsDoc.releaseReadLock(); }
  }

  /** Appends the input string to the interactions pane and interprets it. */
  protected void interpretIgnoreResult(String input) throws EditDocumentException {
    InteractionsDocument interactionsDoc = _model.getInteractionsDocument();
    interactionsDoc.append(input, InteractionsDocument.DEFAULT_STYLE);
    _model.interpretCurrentInteraction();
  }

  /** Asserts that the given string exists in the Interactions Document. */
  protected void assertInteractionsContains(String text) throws EditDocumentException {
    _assertInteractionContainsHelper(text, true);
  }

  /** Asserts that the given string does not exist in the Interactions Document. */
  protected void assertInteractionsDoesNotContain(String text) throws EditDocumentException {
    _assertInteractionContainsHelper(text, false);
  }

  private void _assertInteractionContainsHelper(String text, boolean shouldContain) throws EditDocumentException {

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

  /** Asserts that the text in the Interactions Document matches the given regex. */
  protected void assertInteractionsMatches(String regex) throws EditDocumentException {
    _assertInteractionMatchesHelper(regex, true);
  }

  /** Asserts that the text in the Interactions Document does NOT match the given regex. */
  protected void assertInteractionsDoesNotMatch(String regex)
    throws EditDocumentException {
    _assertInteractionMatchesHelper(regex, false);
  }
  
  private void _assertInteractionMatchesHelper(String regex, boolean shouldMatch) throws EditDocumentException {

    String interactText = getInteractionsText();
    boolean matches = Pattern.compile(regex, Pattern.MULTILINE|Pattern.DOTALL).matcher(interactText).matches();
    assertTrue("Interactions document should " +
               (shouldMatch ? "" : "not ")
                 + "match:\n"
                 + regex
                 + "\nActual contents of Interactions document:\n"
                 + interactText,
               matches == shouldMatch);
  }

  /** Returns the current contents of the interactions document */
  protected String getInteractionsText() throws EditDocumentException {
    InteractionsDocument doc = _model.getInteractionsDocument();
    return doc.getText();
  }

  protected void assertNumOpenDocs(int num) {
    assertEquals("number of open documents", num, _model.getOpenDefinitionsDocuments().size());
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

    assertEquals(name + "compile errors > 0? numErrors =" + numErrors, b, numErrors > 0);
  }

    // These exceptions are specially used only in this test case.
    // They are used to verify that the code blocks
  public static class OverwriteException extends RuntimeException{ }
  public static class OpenWarningException extends RuntimeException{ }
  public static class FileMovedWarningException extends RuntimeException{ }

  public static class WarningFileSelector implements FileOpenSelector, FileSaveSelector {
    private volatile File _file;
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
    private volatile File _file1, _file2;
    public FileSelector(File f) { _file1 = f; }
    public FileSelector(File f1, File f2) {
      _file1 = f1;
      _file2 = f2;
    }

    public File getFile() throws OperationCanceledException { return _file1; }
    
    public File[] getFiles() throws OperationCanceledException {
      if (_file2 != null) return new File[] {_file1, _file2};
      else return new File[] {_file1};
    }
    public boolean warnFileOpen(File f) { return true; }
    public boolean verifyOverwrite() { return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
  }

  public static class CancelingSelector implements FileOpenSelector, FileSaveSelector {
    public File getFile() throws OperationCanceledException { throw new OperationCanceledException(); }
    public File[] getFiles() throws OperationCanceledException { throw new OperationCanceledException(); }
    public boolean warnFileOpen(File f) { return true; }
    public boolean verifyOverwrite() {return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {  return true; }
  }

  /** A GlobalModelListener for testing. By default it expects no events to be fired. To customize,
   *  subclass and override one or more methods.
   */
  public static class TestListener implements GlobalModelListener {
    /** Remembers when this listener was created. */
    protected volatile Exception _startupTrace;
    
    protected volatile int fileNotFoundCount;
    protected volatile int newCount;
    protected volatile int openCount;
    protected volatile int closeCount;
    protected volatile int saveCount;
    protected volatile int canAbandonCount;
    protected volatile int quitFileCount;
    protected volatile int classFileErrorCount;
    protected volatile int compileStartCount;
    protected volatile int compileEndCount;
    protected volatile int runStartCount;
    protected volatile int junitStartCount;
    protected volatile int junitSuiteStartedCount;
    protected volatile int junitTestStartedCount;
    protected volatile int junitTestEndedCount;
    protected volatile int junitEndCount;
    protected volatile int interactionStartCount;
    protected volatile int interactionEndCount;
    protected volatile int interactionErrorCount;
    protected volatile int interpreterResettingCount;
    protected volatile int interpreterReadyCount;
    protected volatile int interpreterExitedCount;
    protected volatile int interpreterResetFailedCount;
    protected volatile int interpreterChangedCount;
    //protected int interactionCaretPositionChangedCount;
    protected volatile int consoleResetCount;
    protected volatile int saveBeforeCompileCount;
    //protected int saveBeforeRunCount;
    protected volatile int compileBeforeJUnitCount;
    protected volatile int saveBeforeJavadocCount;
    //protected int saveBeforeDebugCount;
    protected volatile int nonTestCaseCount;
    protected volatile int lastExitStatus;
    protected volatile int fileRevertedCount;
    protected volatile int shouldRevertFileCount;
    protected volatile int undoableEditCount;
    protected volatile int interactionIncompleteCount;
    protected volatile int filePathContainsPoundCount;

    public TestListener() {
      _startupTrace = new Exception();
      resetCounts();
    }

    public synchronized void resetCounts() {
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
      compileBeforeJUnitCount = 0;
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
    public void projectWorkDirChanged() { }
    public void projectRunnableChanged() { }
    
    public void currentDirectoryChanged(File dir) { }
    
    /** Appends the stack trace from the listener's creation to the end of the given failure message. */
    public void listenerFail(String message) {
      String header = "\nTestListener creation stack trace:\n" + StringOps.getStackTrace(_startupTrace);
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
      assertEquals("number of times interpreterResetting fired", i, interpreterResettingCount);
    }

    public void assertInterpreterReadyCount(int i) {
      assertEquals("number of times interpreterReady fired", i, interpreterReadyCount);
    }

    public void assertInterpreterResetFailedCount(int i) {
      assertEquals("number of times interpreterResetFailed fired", i, interpreterResetFailedCount);
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
    public void assertCompileBeforeJUnitCount(int i) {
      assertEquals("number of times compileBeforeJUnit fired", i, compileBeforeJUnitCount);
    }

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

    public <P,R> void executeAsyncTask(AsyncTask<P,R> task, P param, boolean showProgress, boolean lockUI) {  
      listenerFail("executeAswyncTask fired unexpectedly");
    }
       
    public void handleAlreadyOpenDocument(OpenDefinitionsDocument doc) {
      listenerFail("handleAlreadyOpenDocument fired unexpectedly");
    }
      
    public void newFileCreated(OpenDefinitionsDocument doc) { listenerFail("newFileCreated fired unexpectedly"); } 
    public void filesNotFound(File... f) { listenerFail("fileNotFound fired unexpectedly"); }
    // Recent revision defers opening files until the document for a file is requested forcing the following comment out
    public void fileOpened(OpenDefinitionsDocument doc) { /* listenerFail("fileOpened fired unexpectedly"); */ }
    public void fileClosed(OpenDefinitionsDocument doc) { listenerFail("fileClosed fired unexpectedly"); }
    public void fileSaved(OpenDefinitionsDocument doc) { listenerFail("fileSaved fired unexpectedly"); }
    public void fileReverted(OpenDefinitionsDocument doc) { listenerFail("fileReverted fired unexpectedly"); }
    public void undoableEditHappened() { listenerFail("undoableEditHappened fired unexpectedly"); }
    public void saveBeforeCompile() { listenerFail("saveBeforeCompile fired unexpectedly"); }
    
    public void junitStarted() { listenerFail("junitStarted fired unexpectedly"); }
    public void junitClassesStarted() { listenerFail("junitAllStarted fired unexpectedly"); }
    public void junitSuiteStarted(int numTests) { listenerFail("junitSuiteStarted fired unexpectedly"); }
    public void junitTestStarted(String name) { listenerFail("junitTestStarted fired unexpectedly"); }
    public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
      listenerFail("junitTestEnded fired unexpectedly");
    }
    public void junitEnded() { listenerFail("junitEnded fired unexpectedly"); }
    
    public void javadocStarted() { listenerFail("javadocStarted fired unexpectedly"); }
    public void javadocEnded(boolean success, File destDir, boolean allDocs) {
      listenerFail("javadocEnded fired unexpectedly");
    }

    public void interactionStarted() { listenerFail("interactionStarted fired unexpectedly"); }
    public void interactionEnded() { listenerFail("interactionEnded fired unexpectedly"); }
    public void interactionErrorOccurred(int offset, int length) {
      listenerFail("interpreterErrorOccurred fired unexpectedly");
    }

    public void interpreterChanged(boolean inProgress) { listenerFail("interpreterChanged fired unexpectedly"); }

//    /**Not used */
//    public void interactionCaretPositionChanged(int pos) {
//      listenerFail("interactionCaretPosition fired unexpectedly");
//    }

    public void compileStarted() { listenerFail("compileStarted fired unexpectedly"); }
    public void compileEnded(File workDir, List<? extends File> excludedFiles) { listenerFail("compileEnded fired unexpectedly"); }
    
    public void runStarted(OpenDefinitionsDocument doc) { listenerFail("runStarted fired unexpectedly"); }
    
    public void interpreterResetting() { listenerFail("interpreterResetting fired unexpectedly"); }

    public void interpreterReady(File wd) { listenerFail("interpreterReady fired unexpectedly");  }
    public void interpreterExited(int status) {
      listenerFail("interpreterExited(" + status + ") fired unexpectedly");
    }
    public void interpreterResetFailed(Throwable t) { listenerFail("interpreterResetFailed fired unexpectedly"); }
    public void slaveJVMUsed() { /* not directly tested; ignore it */ }
    public void consoleReset() { listenerFail("consoleReset fired unexpectedly"); }
    public void saveUntitled() { listenerFail("saveUntitled fired unexpectedly"); }
    
    public void compileBeforeJUnit(CompilerListener cl) { compileBeforeJUnitCount++; }

    public void saveBeforeJavadoc() { listenerFail("saveBeforeJavadoc fired unexpectedly"); }
    public void nonTestCase(boolean isTestAll) { listenerFail("nonTestCase fired unexpectedly"); }

    public boolean canAbandonFile(OpenDefinitionsDocument doc) {
      listenerFail("canAbandonFile fired unexpectedly");
      throw new UnexpectedException();
    }
    
    public boolean quitFile(OpenDefinitionsDocument doc) {
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

    public void interactionIncomplete() { listenerFail("interactionIncomplete fired unexpectedly"); }
    public void filePathContainsPound() { listenerFail("filePathContainsPound fired unexpectedly"); }
    
    public void documentNotFound(OpenDefinitionsDocument d, File f) {
      listenerFail("documentNotFound fired unexpectedly");
    }
    
    public void activeDocumentChanged(OpenDefinitionsDocument active) { /* this event is not directly tested */ }
    
    public void activeDocumentRefreshed(OpenDefinitionsDocument active) { /* this event is not directly tested */ }
        
    public void focusOnDefinitionsPane() {  /* this event is not dircectly tested */ }
    
    public void focusOnLastFocusOwner() {  /* this event is not dircectly tested */ }
  }
  
  public static class InteractionListener extends TestListener {
    private volatile boolean _interactionDone = false;       // records when the interaction is done
    private final Object _interactionLock = new Object();    // lock for _interactionDone
    
    private volatile boolean _resetDone = false;             // records when the interaction is done
    private final Object _resetLock = new Object();          // lock for _interactionDone
    
    private volatile int _lastExitStatus = -1;
    
    /** Relying on the default constructor. */
    
    public synchronized void interactionStarted() { interactionStartCount++; }
    
    public void interactionEnded() {
//        assertInteractionStartCount(1);
      
      synchronized(this) { interactionEndCount++; }
      synchronized(_interactionLock) { 
        _interactionDone = true;
        _interactionLock.notify(); 
      }
    }
    
    public void interpreterExited(int status) {
//        Utilities.showDebug("GlobalModelOtherTest: interpreterExited");
//        assertInteractionStartCount(1);
//        assertInterpreterResettingCount(0);
      synchronized(this) { 
        interpreterExitedCount++;
        _lastExitStatus = status;
      }
      synchronized(_interactionLock) { 
        _interactionDone = true;
        _interactionLock.notify(); 
      }
    }
    
    public void interpreterResetting() {
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      synchronized(this) { interpreterResettingCount++; }
    }
    
    public void interpreterReady(File wd) {
//        Utilities.showDebug("GlobalModelOtherTest: interpreterReady");
      synchronized(this) { interpreterReadyCount++; }
      synchronized(_resetLock) {
//          assertInteractionStartCount(1);
//          assertInterpreterExitedCount(1);
//          assertInterpreterResettingCount(1);
//          Utilities.showDebug("GlobalModelOtherTest: notifying resetDone");
        _resetDone = true;
        _resetLock.notify();
      }
    }
    
    public void consoleReset() {
      assertConsoleResetCount(0);
//        assertCompileStartCount(1);
//        assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      synchronized(this) { consoleResetCount++; }
    }
    
    public void resetConsoleResetCount() { consoleResetCount = 0; }
    
    public void logInteractionStart() {
      _interactionDone = false;
      _resetDone = false;
    }
    
    public void waitInteractionDone() throws InterruptedException {
      synchronized(_interactionLock) { while (! _interactionDone) _interactionLock.wait(); }
    }
    
    public void waitResetDone() throws InterruptedException {
      synchronized(_resetLock) { while (! _resetDone)  _resetLock.wait(); }
    }
    
    public int getLastExitStatus() { return _lastExitStatus; }
  };
  
  
  /** A model listener for situations expecting a compilation to fail.  The _expectReset flag determines if interactions
   *  are reset after a compilation. The interactionsReset() method notifies when reset has occurred.
   */
  public static class CompileShouldSucceedListener extends InteractionListener {
    private volatile boolean _expectReset;
    
    private volatile boolean _compileDone = false;        // records when compilaton is done
    private final Object _compileLock = new Object();     // lock for _compileDone
    
    public void logCompileStart() { 
      logInteractionStart();
      _compileDone = false; 
    }
    
    public void compile(OpenDefinitionsDocument doc) throws IOException, InterruptedException {
      logCompileStart();
      doc.startCompile();
      waitCompileDone();
    }
    
    public void waitCompileDone() throws InterruptedException {
      synchronized(_compileLock) {
        while (! _compileDone) {
//        System.err.println("Waiting for JUnit to complete");
          _compileLock.wait();
        }
      }
    }
  
    private void _notifyCompileDone() {
      synchronized(_compileLock) {
        _compileDone = true;
        _compileLock.notify();
      }
    }
    
    /** Standard constructor.
     *  @param expectReset Whether to listen for interactions being
     */
    public CompileShouldSucceedListener(boolean expectReset) { _expectReset = expectReset; }
    
    public CompileShouldSucceedListener() { this(false); }
    
//    public boolean notDone() { return ! _interactionsReset; }
    
    @Override public void newFileCreated(OpenDefinitionsDocument doc) { /* ingore this operation */ }
    
    @Override public void compileStarted() {
//      Utilities.showDebug("compileStarted called in CSSListener");
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      synchronized(this) { compileStartCount++; }
    }
    
    @Override public void compileEnded(File workDir, List<? extends File> excludedFiles) {
//      Utilities.showDebug("compileEnded called in CSSListener");
      assertCompileEndCount(0);
      assertCompileStartCount(1);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      synchronized(this) { compileEndCount++; }
      _notifyCompileDone();
    }
    
    public void checkCompileOccurred() {
      assertCompileEndCount(1);
      assertCompileStartCount(1);
//      if (_expectReset) {
//        assertInterpreterResettingCount(1);
//        assertInterpreterReadyCount(1);
//      }
//      else {
//        assertInterpreterResettingCount(0);
//        assertInterpreterReadyCount(0);
//      }
      
      // Note: console is no longer reset after a compile
      //assertConsoleResetCount(1);
    }
  }
    
  /** A model listener for situations expecting a compilation to fail. */
  public static class CompileShouldFailListener extends TestListener {
    
    private volatile boolean _compileDone = false;        // records when compilaton is done
    private final Object _compileLock = new Object();     // lock for _compileDone
    
    public void logCompileStart() {  _compileDone = false; }
    
    public void waitCompileDone() throws InterruptedException {
      synchronized(_compileLock) {
        while (! _compileDone) {
//        System.err.println("Waiting for JUnit to complete");
          _compileLock.wait();
        }
      }
    }
    
    public void compile(OpenDefinitionsDocument doc) throws IOException, InterruptedException {
      logCompileStart();
      doc.startCompile();
      waitCompileDone();
    }
    
    private void _notifyCompileDone() {
      synchronized(_compileLock) {
        _compileDone = true;
        _compileLock.notify();
      }
    }
    
    @Override public void compileStarted() {
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      synchronized(this) { compileStartCount++; }
    }
    
    @Override public void compileEnded(File workDir, List<? extends File> excludedFiles) {
//      Utilities.showDebug("compileEnded called in CSSListener");
      assertCompileEndCount(0);
      assertCompileStartCount(1);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      synchronized(this) { compileEndCount++; }
      _notifyCompileDone();
    }
    
    public void checkCompileOccurred() {
      assertCompileEndCount(1);
      assertCompileStartCount(1);
    }
    
  }
  
  public static class JUnitTestListener extends CompileShouldSucceedListener {
    
    protected volatile boolean _junitDone = false;
    protected final Object _junitLock = new Object();
    
    // handle System.out's separately but default to outer class's printMessage value
    protected volatile boolean printMessages = GlobalModelJUnitTest.printMessages;
    
    public void logJUnitStart() { 
      logCompileStart();
      _junitDone = false; 
    }
    
    /** Runs JUnit on doc to completion. */
    public void runJUnit(OpenDefinitionsDocument doc) throws IOException, ClassNotFoundException, 
      InterruptedException {
      logJUnitStart();
//    System.err.println("Starting JUnit on " + doc);
      doc.startJUnit();
//    System.err.println("JUnit Started on " + doc);
      waitJUnitDone();
    }
    
    public void runJUnit(JUnitModel jm) throws IOException, ClassNotFoundException, InterruptedException {  
      logJUnitStart();
//    System.err.println("Starting JUnit");
      jm.junitAll();
      waitJUnitDone();
    }
    
    public void waitJUnitDone() throws InterruptedException {
      synchronized(_junitLock) { while (! _junitDone) { _junitLock.wait(); } }
    }
    
    private void _notifyJUnitDone() {
      synchronized(_junitLock) {
        _junitDone = true;
        _junitLock.notify();
      }
    }
    
    /** Construct JUnitTestListener without resetting interactions */
    public JUnitTestListener() { this(false, false);  }
    public JUnitTestListener(boolean shouldResetAfterCompile) {  this(shouldResetAfterCompile, false); }
    public JUnitTestListener(boolean shouldResetAfterCompile, boolean printListenerMessages) {
      super(shouldResetAfterCompile);
      this.printMessages = printListenerMessages;
    }
    public void resetCompileCounts() { 
      compileStartCount = 0; 
      compileEndCount = 0;
    }
    @Override public void junitStarted() {
      if (printMessages) System.out.println("listener.junitStarted");
      synchronized(this) { junitStartCount++; }
    }
    @Override public void junitSuiteStarted(int numTests) {
      if (printMessages) System.out.println("listener.junitSuiteStarted, numTests = "+numTests);
      assertJUnitStartCount(1);
      synchronized(this) { junitSuiteStartedCount++; }
    }
    @Override public void junitTestStarted(String name) {
      if (printMessages) System.out.println("  listener.junitTestStarted, " + name);
      synchronized(this) { junitTestStartedCount++; }
    }
    @Override public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
      if (printMessages) System.out.println("  listener.junitTestEnded, name = " + name + " succ = " + wasSuccessful + 
                                            " err = " + causedError);
      synchronized(this) { junitTestEndedCount++; }
      assertEquals("junitTestEndedCount should be same as junitTestStartedCount", junitTestEndedCount, 
                   junitTestStartedCount);
    }
    @Override public void nonTestCase(boolean isTestAll) {
      if (printMessages) System.out.println("listener.nonTestCase, isTestAll=" + isTestAll);
      synchronized(this) { nonTestCaseCount++; }
      _notifyJUnitDone();
    }
    @Override public void classFileError(ClassFileError e) {
      if (printMessages) System.out.println("listener.classFileError, e="+e);
      synchronized(this) { classFileErrorCount++; }
      _notifyJUnitDone();
    }
    @Override public void junitEnded() {
      //assertJUnitSuiteStartedCount(1);
      if (printMessages) System.out.println("junitEnded event!");
      synchronized(this) { junitEndCount++; }
      _notifyJUnitDone();
    }
  }
  
  /** Listener class for failing JUnit invocation. */
  public static class JUnitNonTestListener extends JUnitTestListener {
    private volatile boolean _shouldBeTestAll;
    public JUnitNonTestListener() {  this(false); }
    public JUnitNonTestListener(boolean shouldBeTestAll) { _shouldBeTestAll = shouldBeTestAll; }
    public void nonTestCase(boolean isTestAll) {
      synchronized(this) { nonTestCaseCount++; }
      assertEquals("Non test case heard the wrong value for test current/test all", _shouldBeTestAll, isTestAll);
//      Utilities.show("synchronizing on _junitLock");
      synchronized(_junitLock) {
//        System.err.println("JUnit aborted as nonTestCase");
        _junitDone = true;
        _junitLock.notify();
      }
    }
  }
  
  /* A variant of DefaultGlobalModel used only for testing purposes.  This variant
   * does not change the working directory when resetting interactions.  This test class and its
   * descendants were written before the distinction between getWorkingDirectory and getMasterDirectory.
   * This method override restores the old semantics for getWorkingDirectory.  The new definition breaks
   * some unit tests because the slave JVM keeps its working directory open until it shuts down. 
   */
   public class TestGlobalModel extends DefaultGlobalModel {
    public File getWorkingDirectory() { return getMasterWorkingDirectory(); }
  } 
}
