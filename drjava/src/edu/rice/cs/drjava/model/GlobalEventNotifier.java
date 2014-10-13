/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.File;
import java.util.List;

import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.swing.AsyncTask;

/** Keeps track of all listeners to the model, and has the ability to notify them of some event.
  * <p>
  * This class has a specific role of managing GlobalModelListeners.  Other classes with similar names use similar
  * code to perform the same function for other interfaces, e.g. InteractionsEventNotifier and JavadocEventNotifier.
  * These classes implement the appropriate interface definition so that they can be used transparently as composite 
  * packaging for a particular listener interface.
  * <p>
  * Components which might otherwise manage their own list of listeners use EventNotifiers instead to simplify their 
  * internal implementation.  Notifiers should therefore be considered a private implementation detail of the 
  * components, and should not be used directly outside of the "host" component.
  * <p>
  * TODO: remove direct references to GlobalEventNotifier outside of DefaultGlobalModel
  * TODO: remove public modifier from this class when above has happened
  *
  * All methods in this class must use the synchronization methods provided by ReaderWriterLock.  This ensures that 
  * multiple notifications (reads) can occur simultaneously, but only one thread can be adding or removing listeners 
  * (writing) at a time, and no reads can occur during a write.
  * <p>
  * <i>No</i> methods on this class should be synchronized using traditional Java synchronization!
  * <p>
  * @version $Id: GlobalEventNotifier.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
// QUESTION: why are we still using _lock operations?  All notifiers should run in the event thread.

public class GlobalEventNotifier extends EventNotifier<GlobalModelListener> implements GlobalModelListener 
  /*, Serializable */ {
  
  public <P,R> void executeAsyncTask(AsyncTask<P,R> task, P param, boolean showProgress, boolean lockUI) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.executeAsyncTask(task, param, showProgress, lockUI); } }
    finally { _lock.endRead(); }
  }
  
  public void filesNotFound(File... f) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.filesNotFound(f); } }
    finally { _lock.endRead(); }
  }
  
  /** @return the intersection of all the return values from the listeners. */
  public File[] filesReadOnly(File... f) {
    _lock.startRead();
    java.util.LinkedList<File> files = new java.util.LinkedList<File>();
    for(File fi: f) { files.add(fi); }
    try {
      for (GlobalModelListener l : _listeners) {
        java.util.List<File> retry = java.util.Arrays.asList(l.filesReadOnly(f));
        files.retainAll(retry);
      }
    }
    finally { _lock.endRead(); }
    return files.toArray(new File[files.size()]);
  }
  
  /** Performs any UI related steps to handle the case in which a file is being opened that
   * is already open and modified. The two choices are to revert to the copy on disk, or to
   * keep the current changes.
   * @param doc  {@code true} if the user wishes to revert the document, {@code false} to ignore
   */
  public void handleAlreadyOpenDocument(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try { for(GlobalModelListener l : _listeners) { l.handleAlreadyOpenDocument(doc); } }
    finally { _lock.endRead(); }
  }
  
  /* -------------- project state ------------------*/
  public void openProject(File pfile, FileOpenSelector files) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.openProject(pfile, files); } }
    finally { _lock.endRead(); }
  }
  
  public void projectClosed() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.projectClosed();} }
    finally { _lock.endRead(); }
  }
  
  public void allFilesClosed() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.allFilesClosed();} }
    finally { _lock.endRead(); }
  }
   
  public void projectModified() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.projectModified(); } }
    finally { _lock.endRead(); }
  }
  
  public void projectBuildDirChanged() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.projectBuildDirChanged(); } }
    finally { _lock.endRead(); }
  }
  
  public void projectWorkDirChanged() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.projectWorkDirChanged(); } }
    finally { _lock.endRead(); }
  }
  
  public void projectRunnableChanged() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.projectRunnableChanged(); } }
    finally { _lock.endRead(); }
  }
  
  
  /* ---------- Deprecated Methods ---------- */
  
  /** Lets the listeners know some event has taken place.
    * @param n tells the listener what happened.
    */
  public void notifyListeners(Notifier n) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { n.notifyListener(l); } }
    finally { _lock.endRead(); }
  }
  
  /** Allows the GlobalModel to ask its listeners a yes/no question and receive a response.
    * @param p the question being asked of the listeners
    * @return the listeners' responses ANDed together, true if they all
    * agree, false if some disagree
    * @deprecated Use listener methods directly instead.
    */
  @Deprecated
  public boolean pollListeners(Poller p) {
    _lock.startRead();
    try {
      for (GlobalModelListener l: _listeners) { if (! p.poll(l)) return false; }
      return true;
    }
    finally { _lock.endRead(); }
  }
  
  /** Class model for notifying listeners of an event.
   * @deprecated Use listener methods directly instead.
   */
  @Deprecated
  public abstract static class Notifier {
    public abstract void notifyListener(GlobalModelListener l);
  }
  
  /** Class model for asking listeners a yes/no question.
   * @deprecated Use listener methods directly instead.
   */
  @Deprecated
  public abstract static class Poller {
    public abstract boolean poll(GlobalModelListener l);
  }
  
  // ---------- End of Deprecated Methods ----------
  
  
  
  //------------------------------ GlobalModel -------------------------------//
  
  /** Called when a file's main method is about to be run. */
  public void prepareForRun(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.prepareForRun(doc); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after a new document is created. */
  public void newFileCreated(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.newFileCreated(doc); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the console window is reset. */
  public void consoleReset() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.consoleReset(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after the current document is saved. */
  public void fileSaved(OpenDefinitionsDocument doc) {
//    ScrollableDialog sd = new ScrollableDialog(null, "fileSaved(" + doc + ") called in GlobalEventNotifier.java", "", "");
//    sd.show();
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.fileSaved(doc); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after a file is opened and read into the current document. */
  public void fileOpened(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.fileOpened(doc); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after a document is closed. */
  public void fileClosed(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.fileClosed(doc); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after a document is reverted. */
  public void fileReverted(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.fileReverted(doc); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when an undoable edit occurs. */
  public void undoableEditHappened() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.undoableEditHappened(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called to ask the listeners if it is OK to abandon the current document. */
  public boolean canAbandonFile(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      for (GlobalModelListener l: _listeners) { if (! l.canAbandonFile(doc)) return false; }
      return true;
    }
    finally { _lock.endRead(); }
  }
  
  /** Called to ask the listeners save the file before quitting at the user's option.
    * @return true if quitting should continue, false if the user cancelled */
  public boolean quitFile(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      // if one of the listeners returns false (=user cancelled), abort
      for (GlobalModelListener l: _listeners) { if (!l.quitFile(doc)) return false; }
    }
    finally { _lock.endRead(); }
    return true;
  }
  
  /** Called to ask the listeners if it is OK to revert the current document to the version saved on disk. */
  public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try { 
      for (GlobalModelListener l: _listeners) { if (! l.shouldRevertFile(doc)) return false; }
      return true;
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when the selection in the navigator changes the current directory without changing the active document. */
  public void currentDirectoryChanged(File dir) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.currentDirectoryChanged(dir); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the selection in the navigator changes the active document. */
  public void activeDocumentChanged(OpenDefinitionsDocument active) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.activeDocumentChanged(active); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the active document is refreshed.  */
  public void activeDocumentRefreshed(OpenDefinitionsDocument active) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.activeDocumentRefreshed(active); } }
    finally { _lock.endRead(); }
  }
  
  /** Called to shift the focus to the Definitions Pane. */
  public void focusOnDefinitionsPane() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.focusOnDefinitionsPane(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called to shift the focus to the last focus owner among the main frame panes. */
  public void focusOnLastFocusOwner() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.focusOnLastFocusOwner(); } }
    finally { _lock.endRead(); }
  }
//  /** Called to demand that all files be saved before running the main method of a document. It is up to the caller
//    * of this method to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments(). This is
//    * nor used currently, but it is commented out in case it is needed later. 
//    */
//  public void saveBeforeRun() {
//    _lock.startRead();
//    try { for (GlobalModelListener l : _listeners) { l.saveBeforeRun(); } }
//    finally { _lock.endRead(); }
//  }
  
  //------------------------------ Interactions ------------------------------//
  
  /** Called after an interaction is started by the GlobalModel. */
  public void interactionStarted() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interactionStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when an interaction has finished running. */
  public void interactionEnded() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interactionEnded(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactions window generates a syntax error.
    * @param offset the error's offset into the InteractionsDocument.
    * @param length the length of the error.
    */
  public void interactionErrorOccurred(int offset, int length) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interactionErrorOccurred(offset, length); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactionsJVM has begun resetting. */
  public void interpreterResetting() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interpreterResetting(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactions window is reset. */
  public void interpreterReady(File wd) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interpreterReady(wd); } }
    finally { _lock.endRead(); }
  }
  
  /** Called if the interpreter reset failed.
    * @param t Throwable explaining why the reset failed.
    * (Subclasses must maintain listeners.)
    */
  public void interpreterResetFailed(final Throwable t) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interpreterResetFailed(t); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the interactions JVM was closed by System.exit or by being aborted. Immediately after this the
    * interactions will be reset.
    * @param status the exit code
    */
  public void interpreterExited(int status) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interpreterExited(status); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when the active interpreter is changed.
    * @param inProgress Whether the new interpreter is processing an interaction (i.e,. whether an interactionEnded
    *        event will be fired)
    */
  public void interpreterChanged(boolean inProgress) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interpreterChanged(inProgress); } }
    finally { _lock.endRead(); }
  }
  
  //-------------------------------- Compiler --------------------------------//
  
  /** Called after a compile is started by the GlobalModel. */
  public void compileStarted() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.compileStarted(); }
    }
    finally { _lock.endRead(); }
  }
  
  /** Called when a compile has finished running. */
  public void compileEnded(File workDir, List<? extends File> excludedFiles) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.compileEnded(workDir, excludedFiles); } }
    finally { _lock.endRead(); }
  }
  
   /** Called if a compile is aborted. */
  public void compileAborted(Exception e) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.compileAborted(e); } }
    finally { _lock.endRead(); }
  }
  /** Called to demand that all files be saved before compiling. It is up to the caller of this method to check
    * if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
    */
  public void saveBeforeCompile() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.saveBeforeCompile(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called to demand that the active document, which is untitled, is saved before compiling.  */
  public void saveUntitled() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.saveUntitled(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after the active compiler has been changed. */
  public void activeCompilerChanged() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.activeCompilerChanged(); } }
    finally { _lock.endRead(); }
  }
  
  //---------------------------------- JUnit ---------------------------------//
  
  /** Called when trying to test a non-TestCase class.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.nonTestCase(isTestAll, didCompileFail); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when trying to test an illegal class file.
    * @param e the ClassFileError thrown when DrScala attempted to load the offending file
    */
  public void classFileError(ClassFileError e) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.classFileError(e); } }
    finally { _lock.endRead(); }
  }
  
  /** Called before attempting unit testing if tested class files are out of sync, to give the user a chance to save. Do
    * not continue with JUnit if the user doesn't recompile!
    */
  public void compileBeforeJUnit(final CompilerListener cl, List<OpenDefinitionsDocument> outOfSync) {
//    Utilities.show("compileBeforeJUnit invoked with argument " + cl + " in GlobalEventNotifier " + this);
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.compileBeforeJUnit(cl, outOfSync); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after JUnit is started by the GlobalModel. */
  public void junitStarted() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.junitStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when testing specific list of classes. */
  public void junitClassesStarted() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.junitClassesStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called to indicate that a suite of tests has started running.
    * @param numTests The number of tests in the suite to be run.
    */
  public void junitSuiteStarted(int numTests) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.junitSuiteStarted(numTests); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when a particular test is started.
    * @param name The name of the test being started.
    */
  public void junitTestStarted(String name) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.junitTestStarted(name); } }
    finally { _lock.endRead(); }
  }
  
  /** Called when a particular test has ended.
    * @param name the name of the test that has ended
    * @param wasSuccessful whether the test passed or not
    * @param causedError if not successful, whether the test caused an error or simply failed
    */
  public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
    _lock.startRead();
    try { 
      for (GlobalModelListener l : _listeners) { l.junitTestEnded(name, wasSuccessful, causedError); }
    }
    finally { _lock.endRead(); }
  }
  
  /** Called after JUnit is finished running tests. */
  public void junitEnded() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.junitEnded(); } }
    finally { _lock.endRead(); }
  }
  
//  /** Called to demand that all files be saved before running JUnit tests. It is up to the caller of this 
//    * method to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments(). This is 
//    * never called currently, but it is commented out in case it is needed later. */
//  public void saveBeforeJUnit() {
//    _lock.startRead();
//    try {
//      for (GlobalModelListener l : _listeners) {
//        l.saveBeforeJUnit();
//      }
//    }
//    finally {
//      _lock.endRead();
//    }
//  }
  
  //--------------------------------- Javadoc --------------------------------//
  
  /** Called after Javadoc is started by the GlobalModel. */
  public void javadocStarted() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.javadocStarted(); } }
    finally { _lock.endRead(); }
  }
  
  /** Called after Javadoc is finished.
    * @param success whether the Javadoc operation generated proper output
    * @param destDir if (success) the location where the output was generated, otherwise undefined (possibly null)
    * @param allDocs Whether Javadoc was run for all open documents
    */
  public void javadocEnded(boolean success, File destDir, boolean allDocs) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.javadocEnded(success, destDir, allDocs); } }
    finally { _lock.endRead(); }
  }
  
  
  /** Called before attempting Javadoc, to give the user a chance to save. Do not continue with Javadoc if the user 
    * doesn't save!
    */
  public void saveBeforeJavadoc() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.saveBeforeJavadoc(); } }
    finally { _lock.endRead(); }
  }

  /** Called before attempting Javadoc, to give the user a chance to compile. Do not continue with Javadoc if the
    * user doesn't comoile!
    */
  public void compileBeforeJavadoc(final CompilerListener afterCompile) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.compileBeforeJavadoc(afterCompile); } }
    finally { _lock.endRead(); }
  }
  
//  /** Called to demand that all files be saved before starting the debugger. It is up to the caller of this method
//    * to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments(). This is not used 
//    * currently, but it is commented out in case it is needed later. */
//  public void saveBeforeDebug() {
//    _lock.startRead();
//    try { for (GlobalModelListener l : _listeners) { l.saveBeforeDebug(); } }
//    finally { _lock.endRead(); }
//  }
  
  /** Notifies the view that the current interaction is incomplete. */
  public void interactionIncomplete() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.interactionIncomplete(); } }
    finally { _lock.endRead(); }
  }
  
  /** Notifies the view that the current file path contains a #. */
  public void filePathContainsPound() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.filePathContainsPound(); } }
    finally { _lock.endRead(); }
  }
  
  // ----- Cache -----
  public void documentNotFound(OpenDefinitionsDocument d, File f) {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.documentNotFound(d,f); } }
    finally { _lock.endRead(); } 
  }
  
  // ----- BrowserHistory -----
  public void browserChanged() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.browserChanged(); } }
    finally { _lock.endRead(); } 
  }

  public void updateCurrentLocationInDoc() {
    _lock.startRead();
    try { for (GlobalModelListener l : _listeners) { l.updateCurrentLocationInDoc(); } }
    finally { _lock.endRead(); } 
  }
}
