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
import edu.rice.cs.drjava.model.repl.DummyInteractionsListener;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.swing.AsyncTask;
import edu.rice.cs.util.FileOpenSelector;

/** A dummy GlobalModelListener that does nothing. Useful for listening to only a small number of events.
  * Not currently used.
  * @version $Id: DummyGlobalModelListener.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public class DummyGlobalModelListener extends DummyInteractionsListener implements GlobalModelListener {
  
  /** Called when an asynchronous task must be run in the model */
  public <P,R> void executeAsyncTask(AsyncTask<P,R> task, P param, boolean showProgress, boolean lockUI) {  }
  
  public void handleAlreadyOpenDocument(OpenDefinitionsDocument doc) { }
  
  /** Called when trying to open one or more files that do not exist. */
  public void filesNotFound(File... f) {  }

  /** Called when trying to write one or more files that are read-only.
    * @param f files that are read-only
    * @return the files that should be attempted to be rewritten */
  public File[] filesReadOnly(File... f) { return f; }
  
  /** Called after a new document is created. */
  public void newFileCreated(OpenDefinitionsDocument doc) { }
  
  /** Called after the current document is saved. */
  public void fileSaved(OpenDefinitionsDocument doc) { }
  
  /** Called after a file is opened and read into the current document. */
  public void fileOpened(OpenDefinitionsDocument doc) { }
  
  /** Called after a document is closed. */
  public void fileClosed(OpenDefinitionsDocument doc) { }
  
  /** Called after a document is reverted. */
  public void fileReverted(OpenDefinitionsDocument doc) { }
  
  /** Called when an undoable edit occurs. */
  public void undoableEditHappened() { }
  
  /** Called after a compile is started by the GlobalModel. */
  public void compileStarted() { }
  
  /** Called when a compile has finished running. */
  public void compileEnded(File workDir, List<? extends File> excludedFiles) { }
  
  /** Called if a compile is aborted. */
  public void compileAborted(Exception e) { }

  /** Called after the active compiler has been changed. */
  public void activeCompilerChanged() { }

  /** Called when a file's main method is about to be run. */
  public void prepareForRun(OpenDefinitionsDocument doc) { }
  
  /** Called when saving a file whose path contains a '#' symbol. */
  public void filePathContainsPound() { }
  
  /** Called to demand that all class file must be in sync before running unit tests. It is up to the caller of this 
    * method to check if the documents are out of sync, using OpenDefinitionsDocument.checkIfClassFileInSync().
    */
  public void compileBeforeJUnit(final CompilerListener l, List<OpenDefinitionsDocument> outOfSync) { }
  
  /** Called after JUnit is started by the GlobalModel. */
  public void junitStarted() { }
  
  /** Called when testing all files. */
  public void junitClassesStarted() {  }
  
  /** Called to indicate that a suite of tests has started running.
    * @param numTests The number of tests in the suite to be run.
    */
  public void junitSuiteStarted(int numTests) { }
  
  /** Called when a particular test is started.
    * @param name The name of the test being started.
    */
  public void junitTestStarted(String name) { }
  
  /** Called when a particular test has ended.
    * @param name The name of the test that has ended.
    * @param wasSuccessful Whether the test passed or not.
    * @param causedError If not successful, whether the test caused an error or simply failed.
    */
  public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) { }
  
  /** Called after JUnit is finished running tests. */
  public void junitEnded() { }
  
  /** Called when the console window is reset. */
  public void consoleReset() { }
  
  /** Called to demand that all files be saved before compiling. It is up to the caller of this method to check
    * if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
    */
  public void saveBeforeCompile() { }
  
  public void saveUntitled() { }
  
  /** Called to demand that all files be saved before generating Javadoc. It is up to the caller of this method to 
    * check if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
    */
  public void saveBeforeJavadoc() { }

  /** Called to demand that all files be compiled before generating Javadoc.
    */
  public void compileBeforeJavadoc(final CompilerListener afterCompile) { }
  
  /** Called to demand that all files be saved before starting the debugger. It is up to the caller of this method 
    * to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
    *
    * Not currently used. */
//  public void saveBeforeDebug() { }
  
  /** Called when the navigator selection changes the current directory without changing the active document. */
  public void currentDirectoryChanged(File dir) { }
  
  /** Called when trying to test a non-TestCase class.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) { }
  
  /** Called when trying to test an illegal class file.
    * @param e the ClassFileError thrown when DrScala attempted to load the offending class.
    */
  public void classFileError(ClassFileError e) { }
  
  /** Called to ask the listener if it is OK to abandon the current document. */
  public boolean canAbandonFile(OpenDefinitionsDocument doc) { return true; }
  
  /** Called to ask the listener if the document should be saved before quitting.
    * @return true if quitting should continue, false if the user cancelled */
  public boolean quitFile(OpenDefinitionsDocument doc) { return true; }
  
  /** Called to ask the listener if it is OK to replace the current document by a newer version on disk. */
  public boolean shouldRevertFile(OpenDefinitionsDocument doc) { return true; }
  
  /** Called after Javadoc is started by the GlobalModel. */
  public void javadocStarted() { }
  
  /** Called after Javadoc is finished.
    * @param success whether the Javadoc operation generated proper output
    * @param destDir if (success == true) the location where the output was placed, otherwise undefined
    * @param allDocs Whether we are running over all open documents
    */
  public void javadocEnded(boolean success, File destDir, boolean allDocs) { }
  
  public void activeDocumentChanged(OpenDefinitionsDocument active) { }
  
  public void activeDocumentRefreshed(OpenDefinitionsDocument active) { }
  
  public void focusOnLastFocusOwner() { }
  
  public void focusOnDefinitionsPane() { }
  
  public void documentNotFound(OpenDefinitionsDocument d, File f) { }
  
  /** Called when the project's build directory has changed. */
  public void projectBuildDirChanged() {  }
  
  /** Called when the project's build directory has changed. */
  public void projectWorkDirChanged() {  }
  
  /** Called when the project is being opened and the model needs the gui to do some stuff for it. */
  public void openProject(File pfile, FileOpenSelector files) {  }
  
  /** Called when the project is being closed. */
  public void projectClosed() {  }
  
  /** Called when all open files are being closed. */
  public void allFilesClosed() {  }
  
  /** Called when the projects modified state has changed. */
  public void projectModified() {  }
  
  /** Called when a project's main class has been set/unset. */
  public void projectRunnableChanged() {  }
  
  /** Called when the a region is added to the browswing history. */
  public void browserChanged() { }
  
  /** Called when the current location in the document needs to be synchronized to the actual location displayed in the view. */
  public void updateCurrentLocationInDoc() { }
}
