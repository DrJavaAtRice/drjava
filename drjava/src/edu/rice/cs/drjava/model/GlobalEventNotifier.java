/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the names of its contributors may 
 *      be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.awt.EventQueue;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.rice.cs.drjava.model.compiler.CompilerListener;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.swing.AsyncTask;
import edu.rice.cs.util.swing.Utilities;

/** Overview: communication between asynchronous tasks is normally conducted by generating an event which is recorded
  * in volatile global state of the tasks that have registered to observe the event. In addition, some tasks (not 
  * running in the event dispatch thread!) may explicitly wait for an event to occur by registering for the event and 
  * waiting for notification.  The library class edu.rice.cs.plt.concurrent.CompletionMonitor makes this protocol easy
  * to implement. -- Corky 8-31-2017
  * <p>
  * This class maintains a list of all listeners to events in the global model, and notifies them when any event in this
  * API occurs (is called by some task). Each method of the corresponding listener interface is a potential event for 
  * which tasks may register by creating an appropriate listener object containing code blocks for each possible event
  * to communicate to the task (most importantly it's local volatile storage) AND registering that listener by adding it 
  * the listener list for the corresponding model. The methods (listener code blocks) within registered listener objects
  * are often called <i>listeners</i>, creating confusion because the entire object containing all of the listeners to 
  * model events are also called <i>listeners<i>.  This class supports the communcation of events at the level of the
  * global model.  Other classes with similar names of the XXXNotifier use similar code to perform the same function for
  * the event interfaces for other models, e.g., InteractionsEventNotifier and ScaladocEventNotifier for
  * the interactions model and the scaladoc model. These classes implement the corresponding event notifier interfaces 
  * so that tasks within the corresponding model can transparently communicate using events local to the model.
  * <p>
  * Tasks which might otherwise communicate using their own event framework should use the listener and event notifier
  * interfaces of the model to which they belong since these interfaces already provide much of the requisite functionality.
  * Listeners should therefore be considered a private implementation detail of a particular task or group of 
  * communicating tasks within model, and should not be directly invoked by tasks outside of that model.
  * <p>
  * TODO: remove readers/writers locking and confirm that all code in this class executed in the event dispatch thread.
  * TODO: remove direct references to GlobalEventNotifier outside of DefaultGlobalModel
  * TODO: remove public modifier from this class when above has happened
  * <p>
  * All methods in this class have been performing synchronization using methods provided by ReaderWriterLock, which is
  * unncecessary if all event handling is serialized in the event dispatch thread.  
  * <p>
  * <i>No</i> methods on this class should be synchronized using traditional Java synchronization!
  * <p>
  * @version $Id: GlobalEventNotifier.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
// QUESTION: why are we still using _lock operations?  All notifiers should run in the event thread.

public class GlobalEventNotifier extends EventNotifier<GlobalModelListener> implements GlobalModelListener 
  /*, Serializable */ {
  
  public <P,R> void executeAsyncTask(AsyncTask<P,R> task, P param, boolean showProgress, boolean lockUI) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.executeAsyncTask(task, param, showProgress, lockUI); }
  }
  
  public void filesNotFound(File... f) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.filesNotFound(f); }
  }
  
  /** @return the intersection of all the return values from the listeners. */
  public File[] filesReadOnly(File... f) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    LinkedList<File> files = new LinkedList<File>();
    for(File fi: f) { files.add(fi); }
    for (GlobalModelListener l : _listeners) {
      List<File> retry = Arrays.asList(l.filesReadOnly(f));
      files.retainAll(retry);
    }
    
    return files.toArray(new File[files.size()]);
  }
  
  /** Performs any UI related steps to handle the case in which a file is being opened that
   * is already open and modified. The two choices are to revert to the copy on disk, or to
   * keep the current changes.
   * @param doc  {@code true} if the user wishes to revert the document, {@code false} to ignore
   */
  public void handleAlreadyOpenDocument(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for(GlobalModelListener l : _listeners) { l.handleAlreadyOpenDocument(doc); }
  }
  
  /* -------------- project state ------------------*/
  public void openProject(File pfile, FileOpenSelector files) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.openProject(pfile, files); }
  }
  
  public void projectClosed() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.projectClosed(); }
  }
  
  public void allFilesClosed() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.allFilesClosed();}
  }
   
  public void projectModified() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.projectModified(); }
  }
  
  public void projectBuildDirChanged() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.projectBuildDirChanged(); }
  }
  
  public void projectWorkDirChanged() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.projectWorkDirChanged(); }
  }
  
  public void projectRunnableChanged() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.projectRunnableChanged(); }
  }
  
  
  /* ---------- Deprecated Methods ---------- */
  
  /** Lets the listeners know some event has taken place.
    * @param n tells the listener what happened.
    */
  public void notifyListeners(Notifier n) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { n.notifyListener(l); }
  }
  
  /** Allows the GlobalModel to ask its listeners a yes/no question and receive a response.
    * @param p the question being asked of the listeners
    * @return the listeners' responses ANDed together, true if they all
    * agree, false if some disagree
    * @deprecated Use listener methods directly instead.
    */
  @Deprecated
  public boolean pollListeners(Poller p) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l: _listeners) { if (! p.poll(l)) return false; }
    return true; 
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
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.prepareForRun(doc); }
  }
  
  /** Called after a new document is created. */
  public void newFileCreated(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.newFileCreated(doc); }
  }
  
  /** Called when the console window is reset. */
  public void consoleReset() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.consoleReset(); }
  }
  
  /** Called after the current document is saved. */
  public void fileSaved(OpenDefinitionsDocument doc) {
//    ScrollableDialog sd = new ScrollableDialog(null, "fileSaved(" + doc + ") called in GlobalEventNotifier.java", "", "");
//    sd.show();
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.fileSaved(doc); }
  }
  
  /** Called after a file is opened and read into the current document. */
  public void fileOpened(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.fileOpened(doc); }
  }
  
  /** Called after a document is closed. */
  public void fileClosed(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.fileClosed(doc); }
  }
  
  /** Called after a document is reverted. */
  public void fileReverted(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.fileReverted(doc); }
  }
  
  /** Called when an undoable edit occurs. */
  public void undoableEditHappened() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.undoableEditHappened(); }
  }
  
  /** Called to ask the listeners if it is OK to abandon the current document. */
  public boolean canAbandonFile(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l: _listeners) { if (! l.canAbandonFile(doc)) return false; }
    return true;
  }
  
  /** Called to ask the listeners save the file before quitting at the user's option.
    * @return true if quitting should continue, false if the user cancelled */
  public boolean quitFile(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    // if one of the listeners returns false (=user cancelled), abort
    for (GlobalModelListener l: _listeners) { if (!l.quitFile(doc)) return false; }
    return true;
  }
  
  /** Called to ask the listeners if it is OK to revert the current document to the version saved on disk. */
  public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l: _listeners) { if (! l.shouldRevertFile(doc)) return false; }
    return true;
  }
  
  /** Called when the selection in the navigator changes the current directory without changing the active document. */
  public void currentDirectoryChanged(File dir) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.currentDirectoryChanged(dir); }
  }
  
  /** Called when the selection in the navigator changes the active document. */
  public void activeDocumentChanged(OpenDefinitionsDocument active) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.activeDocumentChanged(active); } 
  }
  
  /** Called when the active document is refreshed.  */
  public void activeDocumentRefreshed(OpenDefinitionsDocument active) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.activeDocumentRefreshed(active); }
  }
  
  /** Called to shift the focus to the Definitions Pane. */
  public void focusOnDefinitionsPane() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.focusOnDefinitionsPane(); }
    
  }
  
  /** Called to shift the focus to the last focus owner among the main frame panes. */
  public void focusOnLastFocusOwner() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.focusOnLastFocusOwner(); }
  }
//  /** Called to demand that all files be saved before running the main method of a document. It is up to the caller
//    * of this method to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments(). This is
//    * nor used currently, but it is commented out in case it is needed later. 
//    */
//  public void saveBeforeRun() {
//    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    for (GlobalModelListener l : _listeners) { l.saveBeforeRun(); }
//  }
  
  //------------------------------ Interactions ------------------------------//
  
  /** Called after an interaction is started by the GlobalModel. */
  public void interactionStarted() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interactionStarted(); }
  }
  
  /** Called when an interaction has finished running. */
  public void interactionEnded() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interactionEnded(); }
  }
  
  /** Called when the interactions window generates a syntax error.
    * @param offset the error's offset into the InteractionsDocument.
    * @param length the length of the error.
    */
  public void interactionErrorOccurred(int offset, int length) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interactionErrorOccurred(offset, length); }
  }
  
  /** Called when the interactionsJVM has begun resetting. */
  public void interpreterResetting() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interpreterResetting(); }
  }
  
  /** Called when the interactions window is softly reset. */
  public void interpreterReady() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interpreterReady(); }
  }
  
  /** Called if the interpreter reset failed.
    * @param t Throwable explaining why the reset failed.
    * (Subclasses must maintain listeners.)
    */
  public void interpreterResetFailed(final Throwable t) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interpreterResetFailed(t); }
  }
  
  /** Called when the interactions JVM was closed by System.exit or by being aborted. Immediately after this the
    * interactions will be reset.
    * @param status the exit code
    */
  public void interpreterExited(int status) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interpreterExited(status); }
  }
  
//  /** Called when the active interpreter is changed.
//    * @param interactionInProgress() Whether the new interpreter is processing an interaction (i.e,. whether an interactionEnded
//    *        event will be fired)
//    */
//  public void interpreterReplaced() {
//    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    for (GlobalModelListener l : _listeners) { l.interpreterReplaced(); }  
//  }
  
  //-------------------------------- Compiler --------------------------------//
  
  /** Called after a compile is started by the GlobalModel. */
  public void compileStarted() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.compileStarted(); }
  }
  
  /** Called when a compile has finished running. */
  public void compileEnded(File workDir, List<? extends File> excludedFiles) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.compileEnded(workDir, excludedFiles); }
  }
  
   /** Called if a compile is aborted. */
  public void compileAborted(Exception e) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.compileAborted(e); }
  }
  /** Called to demand that all files be saved before compiling. It is up to the caller of this method to check
    * if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
    */
  public void saveBeforeCompile() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.saveBeforeCompile(); }
  }
  
  /** Called to demand that the active document, which is untitled, is saved before compiling.  */
  public void saveUntitled() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.saveUntitled(); }
  }
  
  //---------------------------------- JUnit ---------------------------------//
  
  /** Called when trying to test a non-TestCase class.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.nonTestCase(isTestAll, didCompileFail); }
  }
  
  /** Called when trying to test an illegal class file.
    * @param e the ClassFileError thrown when DrScala attempted to load the offending file
    */
  public void classFileError(ClassFileError e) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.classFileError(e); }
  }
  
  /** Called before attempting unit testing if tested class files are out of sync, to give the user a chance to save. Do
    * not continue with JUnit if the user doesn't recompile!
    */
  public void compileBeforeJUnit(final CompilerListener cl, List<OpenDefinitionsDocument> outOfSync) {
    _log.log("compileBeforeJUnit invoked with argument " + cl + " in GlobalEventNotifier " + this);
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.compileBeforeJUnit(cl, outOfSync); }
    
  }
  
  /** Called after JUnit is started by the GlobalModel. */
  public void junitStarted() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.junitStarted(); }
  }
  
  /** Called when testing specific list of classes. */
  public void junitClassesStarted() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.junitClassesStarted(); }  
  }
  
  /** Called to indicate that a suite of tests has started running.
    * @param numTests The number of tests in the suite to be run.
    */
  public void junitSuiteStarted(int numTests) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.junitSuiteStarted(numTests); }
  }
  
  /** Called when a particular test is started.
    * @param name The name of the test being started.
    */
  public void junitTestStarted(String name) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.junitTestStarted(name); }
  }
  
  /** Called when a particular test has ended.
    * @param name the name of the test that has ended
    * @param wasSuccessful whether the test passed or not
    * @param causedError if not successful, whether the test caused an error or simply failed
    */
  public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.junitTestEnded(name, wasSuccessful, causedError); }
  }
  
  /** Called after JUnit is finished running tests. */
  public void junitEnded() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.junitEnded(); }
  }
  
//  /** Called to demand that all files be saved before running JUnit tests. It is up to the caller of this 
//    * method to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments(). This is 
//    * never called currently, but it is commented out in case it is needed later. */
//  public void saveBeforeJUnit() {
//    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    for (GlobalModelListener l : _listeners) { l.saveBeforeJUnit(); }
//  }
  
  //--------------------------------- Scaladoc --------------------------------//
  
  /** Called after Scaladoc is started by the GlobalModel. */
  public void scaladocStarted() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.scaladocStarted(); }
  }
  
  /** Called after Scaladoc is finished.
    * @param success whether the Scaladoc operation generated proper output
    * @param destDir if (success) the location where the output was generated, otherwise undefined (possibly null)
    * @param allDocs Whether Scaladoc was run for all open documents
    */
  public void scaladocEnded(boolean success, File destDir, boolean allDocs) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.scaladocEnded(success, destDir, allDocs); }
  }
  
  
  /** Called before attempting Scaladoc, to give the user a chance to save. Do not continue with Scaladoc if the user 
    * doesn't save!
    */
  public void saveBeforeScaladoc() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.saveBeforeScaladoc(); }
  }

  /** Called before attempting Scaladoc, to give the user a chance to compile. Do not continue with Scaladoc if the
    * user doesn't comoile!
    */
  public void compileBeforeScaladoc(final CompilerListener afterCompile) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.compileBeforeScaladoc(afterCompile); }
  }
  
//  /** Called to demand that all files be saved before starting the debugger. It is up to the caller of this method
//    * to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments(). This is not used 
//    * currently, but it is commented out in case it is needed later. */
//  public void saveBeforeDebug() {
//    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    for (GlobalModelListener l : _listeners) { l.saveBeforeDebug(); }
//  }
  
  /** Notifies the view that the current interaction is incomplete. */
  public void interactionIncomplete() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.interactionIncomplete(); }
  }
  
  /** Notifies the view that the current file path contains a #. */
  public void filePathContainsPound() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.filePathContainsPound(); }
  }
  
  // ----- Cache -----
  public void documentNotFound(OpenDefinitionsDocument d, File f) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.documentNotFound(d,f); }
  }
  
  // ----- BrowserHistory -----
  public void browserChanged() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.browserChanged(); }
  }

  public void updateCurrentLocationInDoc() {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    for (GlobalModelListener l : _listeners) { l.updateCurrentLocationInDoc(); }
  }
}
