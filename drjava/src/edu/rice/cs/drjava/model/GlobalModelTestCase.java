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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.junit.JUnitModel;
import edu.rice.cs.drjava.ui.InteractionsController;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.swing.AsyncTask;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentException;

import javax.swing.text.BadLocationException;

import java.io.File;
import java.io.IOException;
import java.rmi.UnmarshalException;
import java.util.regex.*;
import java.util.List;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Base class for tests over the {@link GlobalModel}.
 *
 *  This class provides a number of convenience methods for testing the GlobalModel. It also contains a model instance 
 *  (reset in {@link #setUp} and a temporary directory that's created per test invocation (and subsequently cleaned in
 *  {@link #tearDown}. This reduces the burden for such file management stuff in the test cases themselves.
 *
 *  @version $Id$
 */
public abstract class GlobalModelTestCase extends MultiThreadedTestCase {
  
  public static final Log _log = new Log("GlobalModel.txt", false);

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
    super.setUp();  // declared to throw Exception
    debug.logStart();
    _log.log("Setting up " + this);
    _model = new TestGlobalModel();
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        // ensure that the JVM is ready to run; the GlobalModelJUnitTest test cases sometimes received a
        // late _junitModel.junitJVMReady() notification after the unit tests had already been started, and
        // that was interpreted as trying to start JUnit tests while tests were already running.
        _model.ensureJVMStarterFinished();
        // create an interactions pane which is essential to the function of the interactions model; 
        _interactionsController =  // InteractionsController constructor creates an interactions pane
          new InteractionsController(_model.getInteractionsModel(),
                                     _model.getSwingInteractionsDocument(),
                                     new Runnable() { public void run() { } });
        _log.log("Global model created for " + this);
        DrJava.getConfig().resetToDefaults();
        String user = System.getProperty("user.name");
        try { _tempDir = FileOps.createTempDirectory("DrJava-test-" + user /*, ""*/); }
        
        catch(IOException e) {
          fail("IOException thrown with traceback: \n" + e);
        }
      }
    });
//    Utilities.clearEventQueue(); // Let some pending event queue operations complete
    _model.setResetAfterCompile(false);
    
    _log.log("Completed (GlobalModelTestCase) set up of " + this);
    debug.logEnd();
  }
  

  /** Teardown for each test case, which recursively deletes the temporary directory created in setUp. */
  public void tearDown() throws Exception {
    debug.logStart();
    _log.log("Tearing down " + this);
//    System.err.println("Tearing down " + this);
    _model.dispose();
    // We have disposed of the model, remove all interaction listeners to ensure
    // we do not get any late notifications from the interpreter JVM.
    // This fixes the "MainJVM is disposed" errors.
    _model.getInteractionsModel().removeAllInteractionListeners();

    /*boolean ret =*/ IOUtil.deleteOnExitRecursively(_tempDir);
    //assertTrue("delete temp directory " + _tempDir, ret);

    _tempDir = null;
    _model = null;

    super.tearDown();
    _log.log("Completed tear down of " + this);
    debug.logEnd();
//    System.err.println("Completed tear down of " + this);
  }

  /** Clear all old text and insert the given text. 
   * @param s the new text to be set
   * @param doc the document in which to change the text
   */
  protected void changeDocumentText(final String s, final OpenDefinitionsDocument doc) /*throws BadLocationException */{
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          doc.clear();
          assertLength(0, doc);
          doc.append(s, null);
          assertModified(true, doc);
          assertContents(s, doc);
        }
        catch(BadLocationException e) { throw new UnexpectedException(e); }
      }
    });
  }

  /** Create a new temporary file in _tempDir.
   * @return new temporary file in _tempDir. 
   * @throws IOException if an IO operation fails
   */
  protected File tempFile() throws IOException {
    File f = File.createTempFile("DrJava-test", ".java", _tempDir).getCanonicalFile();
//    System.err.println("temp file created with name " + f);
    return f;
  }

  /** Create a new temporary file in _tempDir.  Calls with the same int will 
   * return the same filename, while calls with different ints will return 
   * different filenames.
   * @param i index to be appended to the filename
   * @return new temporary file in _tempDir. 
   * @throws IOException if an IO operation fails
   */
  protected File tempFile(int i) throws IOException {
    return File.createTempFile("DrJava-test" + i, ".java", _tempDir).getCanonicalFile();
  }

  /** Create a new temporary directory in _tempDir. 
   * @return new temporary file in _tempDir. 
   * @throws IOException if an IO operation fails
   */
  protected File tempDirectory() throws IOException {
    return IOUtil.createAndMarkTempDirectory("DrJava-test", "", _tempDir);
  }

  protected File createFile(String name) { return new File(_tempDir, name); }

  /** Given a .java file and a class file name, returns the corresponding 
   * .class file. 
   * @param sourceFile .java file name
   * @param className class file name
   * @return the class file
   */
  protected File classForJava(File sourceFile, String className) {
    assertTrue(sourceFile.getName().endsWith(".java"));
    String cname = className + ".class";
    return new File(sourceFile.getParent(), cname);
  }

  /** Creates a new temporary file and writes the given text to it.
   * The File object for the new file is returned.
   * @param text the text to be written to the file
   * @return the file object for the new file
   * @throws IOException if an IO operation fails 
   */
  protected File writeToNewTempFile(String text) throws IOException {
    File temp = tempFile();
    IOUtil.writeStringToFile(temp, text);
    return temp;
  }

  /** Creates and returns a new document, makes sure newFile is fired, and then 
   * adds some text.  When this method is done newCount is reset to 0.
   * @param text the text to be written to the file
   * @return the new modified document
   * @throws BadLocationException if attempts to reference an invalid location
   */
  protected OpenDefinitionsDocument setupDocument(final String text) throws BadLocationException {
    TestListener listener = new TestListener() {
      public void newFileCreated(OpenDefinitionsDocument doc) { newCount++; }
    };

    _model.addListener(listener);

    // Open a new document
    int numOpen = _model.getOpenDefinitionsDocuments().size();
    
    // newFile() accesses and modifies Swing objects
    Utilities.invokeAndWait(new Runnable() { public void run () { _doc = _model.newFile(); } });
    
    assertNumOpenDocs(numOpen + 1);

    listener.assertNewCount(1);
    assertLength(0, _doc);
    assertModified(false, _doc);

    Utilities.invokeAndWait(new Runnable() { public void run() { changeDocumentText(text, _doc); } });
    
    assertModified(true, _doc);
    _model.removeListener(listener); 

    return _doc;
  }
  
  protected void safeLoadHistory(final FileSelector fs) {
    Utilities.invokeAndWait(new Runnable() { public void run() { _model.loadHistory(fs); } });
  }
  
  protected void safeSaveHistory(final FileSelector fs) {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try {_model.saveHistory(fs); } 
        catch(IOException e) { throw new UnexpectedException(e); }
      }
    });
  }
                                
  /** Invokes startCompile on the given document in the event thread. 
   * @param doc the document to be compiled
   */
  protected static void testStartCompile(final OpenDefinitionsDocument doc) {
    Utilities.invokeLater(new Runnable() { 
      public void run() { 
        try { doc.startCompile(); }
        catch(IOException e) { throw new UnexpectedException(); }
      } 
    });
  }
  /** Compiles a new file with the given text. The compile is expected to 
   * succeed and it is checked to make sure it worked reasonably.  This method 
   * does not return until the Interactions JVM has reset and is ready to use.
   * @param text Code for the class to be compiled
   * @param file File to save the class in
   * @return Document after it has been saved and compiled
   * @throws IOException if an IO operation fails
   * @throws BadLocationException if attempts to reference an invalid location
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  protected synchronized OpenDefinitionsDocument doCompile(String text, File file) throws IOException, 
    BadLocationException, InterruptedException {
    
    OpenDefinitionsDocument doc = setupDocument(text);
    doCompile(doc, file);
    return doc;
  }

  /** Saves to the given file, and then compiles the given document. The 
   * compile is expected to succeed and it is checked to make sure it worked 
   * reasonably.  This method does not return until the Interactions JVM has 
   * reset and is ready to use.
   * @param doc Document containing the code to be compiled
   * @param file File to save the class in
   * @throws IOException if an IO operation fails
   * @throws InterruptedException if execution is interrupted unexpectedly
   */
  protected void doCompile(final OpenDefinitionsDocument doc, File file) throws IOException,  InterruptedException {
    saveFile(doc, new FileSelector(file));

    // Perform a mindless interpretation to force interactions to reset (only to simplify this method)
    try { interpret("0"); }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
    Utilities.clearEventQueue();
    
    _model.setResetAfterCompile(true);
    CompileShouldSucceedListener listener = new CompileShouldSucceedListener();
    _model.addListener(listener);
    
    listener.logCompileStart();

    testStartCompile(doc);
//    Utilities.clearEventQueue();
    
    if (_model.getCompilerModel().getNumErrors() > 0)  fail("compile failed: " + getCompilerErrorString());

    listener.waitCompileDone();

    listener.checkCompileOccurred();
    assertCompileErrorsPresent(false);
    
    listener.waitResetDone();
    Utilities.clearEventQueue();
    _model.removeListener(listener);
  }

  /** @return a string with all compiler errors. */
  protected String getCompilerErrorString() {
    final StringBuilder buf = new StringBuilder();
    buf.append(" compiler error(s):\n");
    buf.append(_model.getCompilerModel().getCompilerErrorModel().toString());
    return buf.toString();
  }

  /** Puts the given input into the interactions document and then interprets 
   * it, returning the result that was put into the interactions document. 
   * This assumes the interactions document is in a state with no text after 
   * the prompt. To be sure this is the case, you can reset interactions 
   * first. This method provides its own listener to synchronized with the 
   * completion of the interaction.
   * @param input text to interpret
   * @return The output from this interpretation, in String form, as it was printed to the interactions document.
   * @throws EditDocumentException if an error occurs while editing the document
   */
  protected String interpret(final String input) throws EditDocumentException {
    
    final InteractionsDocument interactionsDoc = _model.getInteractionsDocument();

    InteractionListener listener = new InteractionListener();
    _model.addListener(listener);
    
    // Set up the interaction
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        interactionsDoc.setInProgress(false);  // for some reason, the inProgress state can be true when interpret is invoked
        interactionsDoc.append(input, ConsoleDocument.DEFAULT_STYLE);
      }
    });
    
    // Record information about pending interaction
    
    final int newLineLen = 1; // Was StringOps.EOL.length(); but Swing uses '\n' for newLine
    final int resultsStartLocation = interactionsDoc.getLength() + newLineLen;
//    Utilities.clearEventQueue();   
    listener.logInteractionStart();
    
    // Execute the interaction
    Utilities.invokeLater(new Runnable() { public void run() { _model.interpretCurrentInteraction(); } });
    
    listener.waitInteractionDone();

    Utilities.clearEventQueue();
    _model.removeListener(listener);
    
    listener.assertInteractionStartCount(1);
    listener.assertInteractionEndCount(1);

    // skip the right length for the newline
    final int resultsEndLocation = interactionsDoc.getLength() - newLineLen - interactionsDoc.getPrompt().length();
    
    final int resultsLen = resultsEndLocation - resultsStartLocation;
    _log.log("resultsStartLoc = " + resultsStartLocation + " resultsEndLocation = " + resultsEndLocation);
    _log.log("Contents = '" + interactionsDoc.getDocText(0, resultsEndLocation+1) + "'");
    if (resultsLen <= 0) return "";
    return interactionsDoc.getDocText(resultsStartLocation, resultsLen);
  }

  /** Appends the input string to the interactions pane and interprets it. 
   * @param input string to be appended
   * @throws EditDocumentException if an error occurs while editing the document
   */
  protected void interpretIgnoreResult(String input) throws EditDocumentException {
    InteractionsDocument interactionsDoc = _model.getInteractionsDocument();
    interactionsDoc.append(input, ConsoleDocument.DEFAULT_STYLE);
    try { _model.interpretCurrentInteraction(); }
    catch(RuntimeException re) { // On Windows, UnmarshalExceptions are sometime thrown
      Throwable cause = re.getCause();
      if (! (cause instanceof UnmarshalException)) throw re; // otherwise ignore
    }
  }

  /** Asserts that the given string exists in the Interactions Document. 
   * @param text text to search for within the interactions document
   * @throws EditDocumentException if an error occurs while editing the document
   */
  protected void assertInteractionsContains(String text) throws EditDocumentException {
    _assertInteractionContainsHelper(text, true);
  }

  /** Asserts that the given string does not exist in the Interactions Document. 
   * @param text text to search for within the interactions document
   * @throws EditDocumentException if an error occurs while editing the document
   */
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

  /** Asserts that the text in the Interactions Document matches the given regex. 
   * @param regex regex to search for a match against
   * @throws EditDocumentException if an error occurs while editing the document
   */
  protected void assertInteractionsMatches(String regex) throws EditDocumentException {
    _assertInteractionMatchesHelper(regex, true);
  }

  /** Asserts that the text in the Interactions Document does NOT match the given regex. 
   * @param regex regex to search for a match against
   * @throws EditDocumentException if an error occurs while editing the document
   */
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

  /** @return the current contents of the interactions document */
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
  
  /** Invokes doc.saveFile from within the event thread. 
   * @param doc document to be saved
   * @param fss a FileSaveSelector
   */
  protected void saveFile(final OpenDefinitionsDocument doc, final FileSaveSelector fss) {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try { doc.saveFile(fss); }
        catch(Exception e) { throw new UnexpectedException(e); }
      } });
  }
  
  /** Invokes doc.saveFileAs from within the event thread. 
   * @param doc document to be saved
   * @param fss a FileSaveSelector
   */
  protected void saveFileAs(final OpenDefinitionsDocument doc, final FileSaveSelector fss) {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try { doc.saveFileAs(fss); }
        catch(Exception e) { throw new UnexpectedException(e); }
      } });
  }
  
  /** Invokes doc.saveFileCopy from within the event thread. 
   * @param doc document to be saved
   * @param fss a FileSaveSelector
   */
  protected void saveFileCopy(final OpenDefinitionsDocument doc, final FileSaveSelector fss) {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() { 
        try { doc.saveFileAs(fss); }
        catch(Exception e) { throw new UnexpectedException(e); }
      } });
  }
  
  protected void saveAllFiles(final GlobalModel model, final FileSaveSelector fs) {
    Utilities.invokeAndWait(new Runnable() {
      public void run() { 
        try { model.saveAllFiles(fs); } // this should save the files as file1,file2,file3 respectively
        catch(Exception e) { throw new UnexpectedException(e); }
      }
    });
  }

  protected void assertCompileErrorsPresent(boolean b) { assertCompileErrorsPresent("", b); }

  protected void assertCompileErrorsPresent(String name, boolean b) {
    int numErrors = _model.getCompilerModel().getNumErrors();

    if (name.length() > 0)  name += ": ";
    
    assertEquals(name + " compile errors > 0 ? numErrors = " + numErrors, b, numErrors > 0);
  }

  // These exceptions are specially used only in this test case. They are used to verify that the code blocks
  public static class OverwriteException extends RuntimeException{ }
  public static class OpenWarningException extends RuntimeException{ }
  public static class FileMovedWarningException extends RuntimeException{ }

  public static class WarningFileSelector implements FileOpenSelector, FileSaveSelector {
    private volatile File _file;
    public WarningFileSelector(File f) { _file = f; }
    public File getFile() throws OperationCanceledException { return _file; }
    public File[] getFiles() throws OperationCanceledException { return new File[] {_file}; }
    public boolean warnFileOpen(File f) { throw new OpenWarningException(); }
    public boolean verifyOverwrite(File f) { throw new OverwriteException(); }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {
      throw new FileMovedWarningException();
    }
    public boolean shouldUpdateDocumentState() { return true; }
  }

  /** This class is used by several test cases in Compile Tests that expect incorrect behavior concerning the saving 
    * of files.  This special FileSelector is included to ensure compliance with these test cases, for which the 
    * intricacies of saving files are unimportant.  The only FileSelector that honest-to-supreme-deity matters is
    * is DefaultGlobalModel.ConcreteOpenDefDoc, which is much more like WarningFileSelector
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
    public boolean verifyOverwrite(File f) { return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) { return true; }
    public boolean shouldUpdateDocumentState() { return true; }
  }

  public static class SaveCopyFileSelector extends FileSelector {
    public SaveCopyFileSelector(File f) { super(f); }
    public SaveCopyFileSelector(File f1, File f2) { super(f1, f2); }
    public boolean shouldUpdateDocumentState() { return false; }
  }

  public static class CancelingSelector implements FileOpenSelector, FileSaveSelector {
    public File getFile() throws OperationCanceledException { throw new OperationCanceledException(); }
    public File[] getFiles() throws OperationCanceledException { throw new OperationCanceledException(); }
    public boolean warnFileOpen(File f) { return true; }
    public boolean verifyOverwrite(File f) {return true; }
    public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc, File oldFile) {  return true; }
    public boolean shouldUpdateDocumentState() { return true; }
  }

  /** A GlobalModelListener for testing. By default it expects no events to be fired. To customize,
   *  subclass and override one or more methods.
   */
  public static class TestListener implements GlobalModelListener {
    /** Remembers when this listener was created. */
    protected volatile Exception _startupTrace;
    
    protected volatile boolean hasClearedEventQueue;
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
    protected volatile int activeCompilerChangedCount;
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
    protected volatile int compileBeforeJavadocCount;
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
      _log.log("Restting all counts to 0 and boolean flags to false");
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
      activeCompilerChangedCount = 0;
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
      compileBeforeJavadocCount = 0;
      //saveBeforeDebugCount = 0;
      nonTestCaseCount = 0;
      lastExitStatus = 0;
      fileRevertedCount = 0;
      shouldRevertFileCount = 0;
      undoableEditCount = 0;
      interactionIncompleteCount = 0;
      filePathContainsPoundCount = 0;
      hasClearedEventQueue = false;
    }

    public void projectModified() { }
    public void openProject(File pfile, FileOpenSelector files) { }
    public void projectClosed() { }
    public void allFilesClosed() { }
    public void projectBuildDirChanged() { }
    public void projectWorkDirChanged() { }
    public void projectRunnableChanged() { }
    
    public void currentDirectoryChanged(File dir) { }
    
    /** Appends the stack trace from the listener's creation to the end of the given failure message. 
     * @param message the failure message
     */
    public void listenerFail(String message) {
      String header = "\nTestListener creation stack trace:\n" + StringOps.getStackTrace(_startupTrace);
      MultiThreadedTestCase.listenerFail(message + header);
    }
    
// clearEventQueue must not be called from the event queue (?), and assertEquals shouldn't have unexpected side effects    
//    public void assertEquals(String s, int expected, int actual) {
//      if (!hasClearedEventQueue) {
//        Utilities.clearEventQueue();
//        hasClearedEventQueue = true;
//      }
//      Assert.assertEquals(s, expected, actual);
//    }
//    
//    public void assertEquals(String s, boolean expected, boolean actual) {
//      if (!hasClearedEventQueue) {
//        Utilities.clearEventQueue();
//        hasClearedEventQueue = true;
//      }
//      Assert.assertEquals(s, expected, actual);
//    }
//
//    public void assertEquals(String s, Object expected, Object actual) {
//      if (!hasClearedEventQueue) {
//        Utilities.clearEventQueue();
//        hasClearedEventQueue = true;
//      }
//      Assert.assertEquals(s, expected, actual);
//    }

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

///** Not used */
//    public void assertInteractionCaretPositionChangedCount(int i) {
//      assertEquals("number of times interactionCaretPositionChanged fired", i, interactionCaretPositionChangedCount);
//    }

    public void assertCompileStartCount(int i) {
      assertEquals("number of times compileStarted fired", i, compileStartCount);
    }

    public void assertCompileEndCount(int i) {
      assertEquals("number of times compileEnded fired", i, compileEndCount);
    }

    public void assertActiveCompilerChangedCount(int i) {
      assertEquals("number of times activeCompilerChanged fired", i, activeCompilerChangedCount);
    }

    public void assertRunStartCount(int i) {
      assertEquals("number of times prepareForRun fired", i, runStartCount);
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

///** Not used.*/
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

    public void assertCompileBeforeJavadocCount(int i) {
      assertEquals("number of times compileBeforeJavadoc fired", i, compileBeforeJavadocCount);
    }

///** Not used. */
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
    public File[] filesReadOnly(File... f) { listenerFail("filesReadOnly fired unexpectedly"); return f; }
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

///**Not used */
//    public void interactionCaretPositionChanged(int pos) {
//      listenerFail("interactionCaretPosition fired unexpectedly");
//    }

    public void compileStarted() { listenerFail("compileStarted fired unexpectedly"); }
    public void compileEnded(File workDir, List<? extends File> excludedFiles) { 
      listenerFail("compileEnded fired unexpectedly"); 
    }
    public void compileAborted(Exception e) { listenerFail("compileAborted fired unexpectedly"); }
    public void activeCompilerChanged() { listenerFail("activeCompilerChanged fired unexpectedly"); }

    public void prepareForRun(OpenDefinitionsDocument doc) { listenerFail("prepareForRun fired unexpectedly"); }
    
    public void interpreterResetting() { listenerFail("interpreterResetting fired unexpectedly"); }

    public void interpreterReady(File wd) { listenerFail("interpreterReady fired unexpectedly");  }
    public void interpreterExited(int status) {
      listenerFail("interpreterExited(" + status + ") fired unexpectedly");
    }
    public void interpreterResetFailed(Throwable t) { listenerFail("interpreterResetFailed fired unexpectedly"); }
    public void consoleReset() { listenerFail("consoleReset fired unexpectedly"); }
    public void saveUntitled() { listenerFail("saveUntitled fired unexpectedly"); }
    
    public void compileBeforeJUnit(CompilerListener cl, List<OpenDefinitionsDocument> outOfSync) { compileBeforeJUnitCount++; }

    public void saveBeforeJavadoc() { listenerFail("saveBeforeJavadoc fired unexpectedly"); }
    public void compileBeforeJavadoc(final CompilerListener afterCompile) {
      listenerFail("compileBeforeJavadoc fired unexpectedly");
    }
    public void nonTestCase(boolean isTestAll, boolean didCompileFail) { listenerFail("nonTestCase fired unexpectedly"); }

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
    public void focusOnDefinitionsPane() {  /* this event is not directly tested */ }
    public void focusOnLastFocusOwner() {  /* this event is not directly tested */ }
    public void browserChanged() { /* this event is not directly tested */ }
    public void updateCurrentLocationInDoc() { /* this event is not directly tested */ }
  }
  
  public static class InteractionListener extends TestListener {
    public static Log _log = new Log("GlobalModel.txt", false);
    private static final int WAIT_TIMEOUT = 20000; // time to wait for _interactionDone or _resetDone 
    private volatile CompletionMonitor _interactionDone;
    private volatile CompletionMonitor _resetDone;
    
    private volatile int _lastExitStatus = -1;
    
    public InteractionListener() {
      _interactionDone = new CompletionMonitor();
      _resetDone = new CompletionMonitor();
    }
    
    public synchronized void interactionStarted() { interactionStartCount++; }
    
    public void interactionEnded() {
//        assertInteractionStartCount(1);
      
      synchronized(this) { interactionEndCount++; }
      _interactionDone.signal();
    }
    
    public void interpreterExited(int status) {
//        Utilities.showDebug("GlobalModelOtherTest: interpreterExited");
//        assertInteractionStartCount(1);
//        assertInterpreterResettingCount(0);
      synchronized(this) { 
        interpreterExitedCount++;
        _lastExitStatus = status;
      }
      _interactionDone.signal();
    }
    
    public void interpreterResetting() {
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      synchronized(this) { interpreterResettingCount++; }
    }
    
    public void interpreterReady(File wd) {
//        Utilities.showDebug("GlobalModelOtherTest: interpreterReady");
      synchronized(this) { interpreterReadyCount++; }
      _resetDone.signal();
    }
    
    public void consoleReset() {
      assertConsoleResetCount(0);
//        assertCompileStartCount(1);
//        assertCompileEndCount(1);
      // don't care whether interactions or console are reset first
      synchronized(this) { consoleResetCount++; }
    }
    
    public void resetConsoleResetCount() { consoleResetCount = 0; }
    
    public synchronized void logInteractionStart() {
      _log.log("Resetting _interactionDone and _resetDone");
      _interactionDone.reset();
      _resetDone.reset();
    }
    
    public void waitInteractionDone() {
      assertTrue("Interaction did not complete before timeout",
                 _interactionDone.attemptEnsureSignaled(WAIT_TIMEOUT));
    }
    
    public void waitResetDone() throws InterruptedException {
      assertTrue("Reset did not complete before timeout",
                 _resetDone.attemptEnsureSignaled(WAIT_TIMEOUT));
    }
    
    public int getLastExitStatus() { return _lastExitStatus; }
  };
  
  
  /** A model listener for situations expecting a compilation to succeed. */
  public static class CompileShouldSucceedListener extends InteractionListener {
    
    private volatile boolean _compileDone = false;        // records when compilaton is done
    private final Object _compileLock = new Object();     // lock for _compileDone
    
    public void logCompileStart() { 
//      logInteractionStart();
      synchronized (_compileLock) {
        _log.log("In CSSL, logCompileStart called");
        _compileDone = false; 
      }
    }
    
    public void compile(OpenDefinitionsDocument doc) throws IOException, InterruptedException {
      logCompileStart();
      testStartCompile(doc);
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
        _compileDone = true;  // modify flag first so that notified threads will see that compilation is done
        _compileLock.notifyAll();
      }
    }
    
    @Override public void newFileCreated(OpenDefinitionsDocument doc) { /* ingore this operation */ }
    
    @Override public void compileStarted() {
//      Utilities.showDebug("compileStarted called in CSSListener");
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      assertActiveCompilerChangedCount(0);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      synchronized(this) { compileStartCount++; }
    }
    
    @Override public void compileEnded(File workDir, List<? extends File> excludedFiles) {
//      Utilities.showDebug("compileEnded called in CSSListener");
      assertCompileEndCount(0);
      assertActiveCompilerChangedCount(0);
      assertCompileStartCount(1);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      synchronized(this) { compileEndCount++; }
      _notifyCompileDone();
    }
    
    @Override public void compileAborted(Exception e) {
      _notifyCompileDone();
    }

    @Override public void activeCompilerChanged() {
//      Utilities.showDebug("compileEnded called in CSSListener");
      synchronized(this) { activeCompilerChangedCount++; }
      _notifyCompileDone();
    }
    
    public void checkCompileOccurred() {
      assertCompileEndCount(1);
      assertActiveCompilerChangedCount(0);
      assertCompileStartCount(1);
    }
  }
    
  /** A model listener for situations expecting a compilation to fail. */
  public static class CompileShouldFailListener extends TestListener {
    
    private volatile boolean _compileDone = false;        // records when compilaton is done
    private final Object _compileLock = new Object();     // lock for _compileDone
    
    public void logCompileStart() {
      synchronized (_compileLock) { 
        _log.log("In CSFL, logCompileStart called");
        _compileDone = false; 
      }
    }
    
    public void waitCompileDone() throws InterruptedException {
      synchronized (_compileLock) {
        while (! _compileDone) {
//        System.err.println("Waiting for JUnit to complete");
          _compileLock.wait();
        }
      }
    }
    
    public void compile(OpenDefinitionsDocument doc) throws IOException, InterruptedException {
      logCompileStart();
      testStartCompile(doc);
      waitCompileDone();
    }
    
    private void _notifyCompileDone() {
      synchronized(_compileLock) {
        _log.log("In CSFL, compilation done notification");
        _compileDone = true;
        _compileLock.notify();
      }
    }
    
    @Override public void compileStarted() {
      assertCompileStartCount(0);
      assertCompileEndCount(0);
      assertActiveCompilerChangedCount(0);
      synchronized(this) { compileStartCount++; }
    }
    
    @Override public void compileEnded(File workDir, List<? extends File> excludedFiles) {
      assertCompileEndCount(0);
      assertActiveCompilerChangedCount(0);
      assertCompileStartCount(1);
      assertInterpreterResettingCount(0);
      assertInterpreterReadyCount(0);
      assertConsoleResetCount(0);
      synchronized(this) { compileEndCount++; }
      _notifyCompileDone();
    }
    
    @Override public void compileAborted(Exception e) {
      _notifyCompileDone();
    }

    
    public void checkCompileOccurred() {
      assertCompileEndCount(1);
      assertCompileStartCount(1);
      assertActiveCompilerChangedCount(0);
    }
    
  }
  
  public static class JUnitTestListener extends CompileShouldSucceedListener {
    
    protected volatile boolean _junitDone = false;
    protected final Object _junitLock = new Object();
    
    // handle System.out's separately but default to outer class's printMessage value
    protected volatile boolean printMessages = GlobalModelJUnitTest.printMessages;
    
    /** Construct JUnitTestListener without resetting interactions */
    public JUnitTestListener() { this(false);  }
    public JUnitTestListener(boolean printListenerMessages) {
      this.printMessages = printListenerMessages;
    }
    
    public synchronized void logJUnitStart() { 
      _log.log("JUnitStart called");
//      logCompileStart();
      _junitDone = false; 
    }
    
    /** Runs JUnit on doc to completion. 
      * @param doc the document on which to run JUnit
      * @throws IOException if an IO operation fails
      * @throws ClassNotFoundException if a class is not found
      * @throws InterruptedException if execution is interrupted unexpectedly
      */
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
//    _log.log("Starting JUnit");
      jm.junitAll();
      waitJUnitDone();
    }
    
    public void waitJUnitDone() throws InterruptedException {
      synchronized(_junitLock) { while (! _junitDone) { _junitLock.wait(); } }
    }
    
    protected void _notifyJUnitDone() {  // access in subclass JUnitNonTestListener
      synchronized(_junitLock) {
        _junitDone = true;
        _junitLock.notifyAll();
      }
    }
    
    public void resetCompileCounts() { 
      compileStartCount = 0; 
      compileEndCount = 0;
      activeCompilerChangedCount = 0;
    }
    
    public synchronized void resetJUnitCounts() {
      junitStartCount = 0;
      junitSuiteStartedCount = 0;
      junitTestStartedCount = 0;
      junitTestEndedCount = 0;
      junitEndCount = 0;
    }
     
    @Override public void junitStarted() {
      _log.log("listener.junitStarted, incrementing count");
      synchronized(this) { junitStartCount++; }
    }
    @Override public void junitSuiteStarted(int numTests) {
      _log.log("listener.junitSuiteStarted, incrementing count, numTests = " + numTests);
      assertJUnitStartCount(1);
      synchronized(this) { junitSuiteStartedCount++; }
    }
    @Override public void junitTestStarted(String name) {
      _log.log("  listener.junitTestStarted, incrementing count " + name);
      synchronized(this) { junitTestStartedCount++; }
    }
    @Override public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
      _log.log("  listener.junitTestEnded, incrementing count, name = " + name + " succ = " + wasSuccessful + 
                                            " err = " + causedError);
      synchronized(this) { junitTestEndedCount++; }
      assertEquals("junitTestEndedCount should be same as junitTestStartedCount", junitTestEndedCount, 
                   junitTestStartedCount);
    }
    @Override public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
      _log.log("listener.nonTestCase, isTestAll=" + isTestAll);
      synchronized(this) { nonTestCaseCount++; }
      _log.log("nonTestCase() called, incrementing count, notifying JUnitDone");
      _notifyJUnitDone();
    }
    @Override public void classFileError(ClassFileError e) {
      _log.log("listener.classFileError, e=" + e);
      synchronized(this) { classFileErrorCount++; }
      _log.log("classFileError() called; notifying JUnitDone");
      _notifyJUnitDone();
    }
    @Override public void junitEnded() {
      // assertJUnitSuiteStartedCount(1);
      _log.log("junitEnded event!");
      synchronized(this) { junitEndCount++; }
      _log.log("junitEnded() called; notifying JUnitDone");
      _notifyJUnitDone();
    }
  }
  
  /** Listener class for failing JUnit invocation. */
  public static class JUnitNonTestListener extends JUnitTestListener {
    private volatile boolean _shouldBeTestAll;
    public JUnitNonTestListener() { this(false); }
    public JUnitNonTestListener(boolean shouldBeTestAll) { _shouldBeTestAll = shouldBeTestAll; }
    public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
      synchronized(this) { nonTestCaseCount++; }
      assertEquals("Non test case heard the wrong value for test current/test all", _shouldBeTestAll, isTestAll);
//      Utilities.show("synchronizing on _junitLock");
      _notifyJUnitDone();
    }
  }
  
  /* A variant of DefaultGlobalModel used only for testing purposes.  This variant
   * does not change the working directory when resetting interactions.  This test class and its
   * descendants were written before the distinction between getWorkingDirectory and getMasterDirectory.
   * This method override restores the old semantics for getWorkingDirectory.  The new definition breaks
   * some unit tests because the slave JVM keeps its working directory open until it shuts down. 
   */
   public static class TestGlobalModel extends DefaultGlobalModel {
    public File getWorkingDirectory() { return getMasterWorkingDirectory(); }
  } 
}
