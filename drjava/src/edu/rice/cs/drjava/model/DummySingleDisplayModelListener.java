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

import java.io.File;

/**
 * A dummy, do-nothing GlobalModelListener.
 * Useful for listening to only a small number of events.
 * @version $Id$
 */
public class DummySingleDisplayModelListener implements SingleDisplayModelListener {
  
  /**
   * Called after a new document is created.
   */
  public void newFileCreated(OpenDefinitionsDocument doc) { }

  /**
   * Called after the current document is saved.
   */
  public void fileSaved(OpenDefinitionsDocument doc) { }

  /**
   * Called after a file is opened and read into the current document.
   */
  public void fileOpened(OpenDefinitionsDocument doc) { }

  /**
   * Called after a document is closed.
   */
  public void fileClosed(OpenDefinitionsDocument doc) { }

  /**
   * Called after a document is reverted.
   */
  public void fileReverted(OpenDefinitionsDocument doc) { }
  
  /**
   * Called when an undoable edit occurs.
   */
  public void undoableEditHappened() { }

  /**
   * Called after a compile is started by the GlobalModel.
   */
  public void compileStarted() { }

  /**
   * Called when a compile has finished running.
   */
  public void compileEnded() { }
  
  /**
   * Called when a file's main method is about to be run.
   */
  public void runStarted(OpenDefinitionsDocument doc) { }

  /**
   * Called after JUnit is started by the GlobalModel.
   */
  public void junitStarted(OpenDefinitionsDocument doc) { }
  
  /**
   * Called to indicate that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void junitSuiteStarted(int numTests) { }
  
  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  public void junitTestStarted(String name) { }
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void junitTestEnded(OpenDefinitionsDocument doc, String name,
                             boolean wasSuccesful, boolean causedError) { }
  
  /**
   * Called after JUnit is finished running tests.
   */
  public void junitEnded() { }

  //---------------------- InteractionsListener Methods ----------------------//
  
  /**
   * Called after an interaction is started by the GlobalModel.
   */
  public void interactionStarted() { }

  /**
   * Called when an interaction has finished running.
   */
  public void interactionEnded() { }
  
  /**
   * Called when the interactions window generates a syntax error.
   *
   * @param offset the error's offset into the InteractionsDocument
   * @param length the length of the error
   */
  public void interactionErrorOccurred(int offset, int length) { }

  /**
   * Called when the interactionsJVM has begun resetting.
   */
  public void interpreterResetting() { }
  
  /**
   * Called when the interactions window is reset.
   */
  public void interpreterReady() { }

  /**
   * Called when the interactions JVM was closed by System.exit
   * or by being aborted. Immediately after this the interactions
   * will be reset.
   * @param status the exit code
   */
  public void interpreterExited(int status) { }
  
  /**
   * Called if the interpreter reset failed.
   * (Subclasses must maintain listeners.)
   */
  public void interpreterResetFailed() { }
  
  /**
   * Called when the active interpreter is changed.
   * @param inProgress Whether the new interpreter is currently in progress
   * with an interaction (ie. whether an interactionEnded event will be fired)
   */
  public void interpreterChanged(boolean inProgress) { }

  //-------------------- End InteractionsListener Methods --------------------//
  
  /**
   * Called when the caret position in the interactions pane is changed
   */
  //public void interactionCaretPositionChanged(int pos) { }

  /**
   * Called when the console window is reset.
   */
  public void consoleReset() { }
  
  /**
   * Called to demand that all files be saved before compiling.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeCompile() { }

  /**
   * Called to demand that all files be saved before running the main method of
   * a document. It is up to the caller of this method to check if the documents
   * have been saved, using IGetDocuments.hasModifiedDocuments().
   *
   * Not currently used.
  public void saveBeforeRun() { }
   */
  
  /**
   * Called to demand that all files be saved before running JUnit tests.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   *
   * Not currently used.
  public void saveBeforeJUnit() { }
   */
  
  /**
   * Called to demand that all files be saved before generating Javadoc.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeJavadoc() { }
  
  /**
   * Called to demand that all files be saved before starting the debugger.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   *
   * Not currently used.
  public void saveBeforeDebug() { }
   */
  
  /**
   * Called when trying to test a non-TestCase class.
   */
  public void nonTestCase() { }

  /**
   * Called to ask the listener if it is OK to abandon the current
   * document.
   */
  public boolean canAbandonFile(OpenDefinitionsDocument doc) { return true; }

  /**
   * Called to ask the listener if it is OK to revert the current
   * document to a newer version saved on file.
   */
  public boolean shouldRevertFile(OpenDefinitionsDocument doc) { return true; }

  /**
   * Called after Javadoc is started by the GlobalModel.
   */
  public void javadocStarted() { }
  
  /**
   * Called after Javadoc is finished.
   * @param success whether the Javadoc operation generated proper output
   * @param destDir if (success == true) the location where the output was
   *                generated, otherwise undefined (possibly null)
   */
  public void javadocEnded(boolean success, File destDir) { }
  
  public void activeDocumentChanged(OpenDefinitionsDocument active) { }
}
