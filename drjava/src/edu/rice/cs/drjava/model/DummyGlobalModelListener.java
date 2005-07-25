/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.File;
import java.util.List;
import edu.rice.cs.util.classloader.ClassFileError;

/** A dummy, do-nothing GlobalModelListener. Useful for listening to only a small number of events.
 *  @version $Id$
 */
public class DummyGlobalModelListener implements GlobalModelListener {
  
  /** Called when trying to open a file that does not exist. */
  public void fileNotFound(File f) {   }
  
  /** Called when the project's build directory has changed. */
  public void projectBuildDirChanged() {  }
  
  /** Called when the project is being opened and the model needs the gui to do some stuff for it. */
  public void projectOpened(File pfile, FileOpenSelector files) {  }
  
  /** Called when the project is being closed. */
  public void projectClosed() {  }
  
  /** Called when the projects modified state has changed. */
  public void projectModified() {  }

  /** Called when a project's main class has been set/unset. */
  public void projectRunnableChanged() {  }
  
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
  public void compileEnded() { }
  
  /** Called when a file's main method is about to be run. */
  public void runStarted(OpenDefinitionsDocument doc) { }
  
  /** Called when saving a file whose path contains a '#' symbol. */
  public void filePathContainsPound() { }

  /** Called after JUnit is started by the GlobalModel. */
  public void junitStarted(List<OpenDefinitionsDocument> docs) { }
  
  /** Called when testing all files. */
  public void junitClassesStarted() {  }

  /** Called to indicate that a suite of tests has started running.
   *  @param numTests The number of tests in the suite to be run.
   */
  public void junitSuiteStarted(int numTests) { }
  
  /** Called when a particular test is started.
   *  @param name The name of the test being started.
   */
  public void junitTestStarted(String name) { }
  
  /** Called when a particular test has ended.
   *  @param name The name of the test that has ended.
   *  @param wasSuccessful Whether the test passed or not.
   *  @param causedError If not successful, whether the test caused an error or simply failed.
   */
  public void junitTestEnded(String name, boolean wasSuccessful, boolean causedError) { }
  
  /** Called after JUnit is finished running tests. */
  public void junitEnded() { }

  //---------------------- InteractionsListener Methods ----------------------//
  
  /** Called after an interaction is started by the GlobalModel.  */
  public void interactionStarted() { }

  /** Called when an interaction has finished running. */
  public void interactionEnded() { }
  
  /** Called when the interactions window generates a syntax error.
   *  @param offset the error's offset into the InteractionsDocument
   *  @param length the length of the error
   */
  public void interactionErrorOccurred(int offset, int length) { }

  /** Called when the interactionsJVM has begun resetting. */
  public void interpreterResetting() { }
  
  /** Called when the interactions window is reset. */
  public void interpreterReady() { }

  /** Called when the interactions JVM was closed by System.exit
   *  or by being aborted. Immediately after this the interactions
   *  will be reset.
   * @param status the exit code
   */
  public void interpreterExited(int status) { }
  
  /** Called if the interpreter reset failed. (Subclasses must maintain listeners.) */
  public void interpreterResetFailed(Throwable t) { }
  
  /** Called when the active interpreter is changed.
   *  @param inProgress Whether the new interpreter is currently processing an interaction (i.e. whether an 
   *  interactionEnded event will be fired)
   */
  public void interpreterChanged(boolean inProgress) { }

  /** Called when enter was typed in the interactions pane but the interaction was incomplete. */
  public void interactionIncomplete() { }

  //-------------------- End InteractionsListener Methods --------------------//
  
  /** Called when the console window is reset. */
  public void consoleReset() { }
  
  /** Called to demand that all files be saved before compiling. It is up to the caller of this method to check
   *  if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeCompile() { }

  /** Called to demand that all files be saved before running the main method of a document. It is up to the 
   *  caller of this method to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
   *
   *  Not currently used.*/
//  public void saveBeforeRun() { }

  /** Called to demand that all files be saved before running JUnit tests. It is up to the caller of this method 
   *  to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
   *
   *  Not currently used. */
//  public void saveBeforeJUnit() { }

  
  /** Called to demand that all files be saved before generating Javadoc. It is up to the caller of this method to 
   *  check if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeJavadoc() { }
  
  /** Called to demand that all files be saved before starting the debugger. It is up to the caller of this method 
   *  to check if the documents have been saved, using IGetDocuments.hasModifiedDocuments().
   *
   *  Not currently used. */
//  public void saveBeforeDebug() { }
  
  /** Called when the navigator selection changes the current directory without changing the active document. */
  public void currentDirectoryChanged(File dir) { }
  
  /** Called when trying to test a non-TestCase class.
   *  @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) { }
  
  /** Called when trying to test an illegal class file.
   *  @param e the ClassFileError thrown when DrJava attempted to load the offending class.
   */
  public void classFileError(ClassFileError e) { }

  /** Called to ask the listener if it is OK to abandon the current document. */
  public boolean canAbandonFile(OpenDefinitionsDocument doc) { return true; }
  
  /** Called to ask the listener if the document should be saved before quitting. */
  public void quitFile(OpenDefinitionsDocument doc) { }
  
  /** Called to ask the listener if it is OK to replace the current document by a newer version on disk. */
  public boolean shouldRevertFile(OpenDefinitionsDocument doc) { return true; }

  /** Called after Javadoc is started by the GlobalModel. */
  public void javadocStarted() { }
  
  /** Called after Javadoc is finished.
   *  @param success whether the Javadoc operation generated proper output
   *  @param destDir if (success == true) the location where the output was placed, otherwise undefined
   *  @param allDocs Whether we are running over all open documents
   */
  public void javadocEnded(boolean success, File destDir, boolean allDocs) { }
  
  public void activeDocumentChanged(OpenDefinitionsDocument active) { }
  
  public void documentNotFound(OpenDefinitionsDocument d, File f) { }
}
