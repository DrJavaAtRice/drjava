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

import java.util.ArrayList;
import java.io.File;

/**
 * Keeps track of all listeners to the model, and has the ability
 * to notify them of some event.  All methods on this class should be
 * synchronized to prevent events from occuring while updating listeners.
 * @version $Id$
 */
public class EventNotifier implements GlobalModelListener {
  /**
   * All GlobalModelListeners that are listening to the model.
   * An unsynchronized collection is okay as long as all methods on this class
   * are synchronized (against this class itself).  Syncing on this is safer
   * because it prevents unsynchronized iteration over listeners.
   */
  private final ArrayList<GlobalModelListener> _listeners;
  
  /**
   * Creates a new EventNotifier with an empty list of listeners.
   */
  public EventNotifier() {
    _listeners = new ArrayList<GlobalModelListener>();
  }
  
  /**
   * Add a listener to the model.
   * @param listener a listener that reacts on events
   */
  synchronized public void addListener(GlobalModelListener listener) {
    _listeners.add(listener);
  }

  /**
   * Remove a listener from the model.
   * @param listener a listener that reacts on events
   */
  synchronized public void removeListener(GlobalModelListener listener) {
    _listeners.remove(listener);
  }

  /**
   * Removes all listeners from this notifier.
   */
  synchronized public void removeAllListeners() {
    _listeners.clear();
  }
  
  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
  synchronized public void notifyListeners(Notifier n) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      n.notifyListener(_listeners.get(i));
    }
  }
  
  /**
   * Allows the GlobalModel to ask its listeners a yes/no question and
   * receive a response.
   * @param EventPoller p the question being asked of the listeners
   * @return the listeners' responses ANDed together, true if they all
   * agree, false if some disagree
   */
  synchronized public boolean pollListeners(Poller p) {
    boolean poll = true;
    
    int size = _listeners.size();
    for(int i = 0; (poll && (i < size)); i++) {
      poll = poll && p.poll(_listeners.get(i));
    }
    return poll;
  }

  /**
   * Class model for notifying listeners of an event.
   */
  public abstract static class Notifier {
    public abstract void notifyListener(GlobalModelListener l);
  }

  /**
   * Class model for asking listeners a yes/no question.
   */
  public abstract static class Poller {
    public abstract boolean poll(GlobalModelListener l);
  }
  
  /**
   * Called after JUnit is started by the GlobalModel.
   */
  synchronized public void junitStarted(OpenDefinitionsDocument doc) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).junitStarted(doc);
    }
  }
  
  /**
   * Called when the active interpreter is changed.
   * @param inProgress Whether the new interpreter is currently in progress
   * with an interaction (ie. whether an interactionEnded event will be fired)
   */
  synchronized public void interpreterChanged(boolean inProgress) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interpreterChanged(inProgress);
    }
  }
  
  /**
   * Called to indicate that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  synchronized public void junitSuiteStarted(int numTests) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).junitSuiteStarted(numTests);
    }
  }
  
  /**
   * Called when the interactions window generates a syntax error.
   *
   * @param offset the error's offset into the InteractionsDocument
   * @param length the length of the error
   */
  synchronized public void interactionErrorOccurred(int offset, int length) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interactionErrorOccurred(offset, length);
    }
  }
  
  /**
   * Called after a new document is created.
   */
  synchronized public void newFileCreated(OpenDefinitionsDocument doc) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).newFileCreated(doc);
    }
  }
  
  /**
   * Called when an interaction has finished running.
   */
  synchronized public void interactionEnded() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interactionEnded();
    }
  }
  
  /**
   * Called when the console window is reset.
   */
  synchronized public void consoleReset() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).consoleReset();
    }
  }
  
  /**
   * Called after the current document is saved.
   */
  synchronized public void fileSaved(OpenDefinitionsDocument doc) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).fileSaved(doc);
    }
  }
  
  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  synchronized public void junitTestStarted(String name) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).junitTestStarted(name);
    }
  }
  
  /**
   * Called after a file is opened and read into the current document.
   */
  synchronized public void fileOpened(OpenDefinitionsDocument doc) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).fileOpened(doc);
    }
  }
  
  /**
   * Called when the interactionsJVM has begun resetting.
   */
  synchronized public void interpreterResetting() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interpreterResetting();
    }
  }
  
  /**
   * Called after Javadoc is started by the GlobalModel.
   */
  synchronized public void javadocStarted() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).javadocStarted();
    }
  }
  
  /**
   * Called after a document is closed.
   */
  synchronized public void fileClosed(OpenDefinitionsDocument doc) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).fileClosed(doc);
    }
  }
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  synchronized public void junitTestEnded(OpenDefinitionsDocument doc, String name, boolean wasSuccesful, boolean causedError) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).junitTestEnded(doc, name, wasSuccesful, causedError);
    }
  }
  
  /**
   * Called after a document is reverted.
   */
  synchronized public void fileReverted(OpenDefinitionsDocument doc) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).fileReverted(doc);
    }
  }
  
  /**
   * Called after Javadoc is finished.
   * @param success whether the Javadoc operation generated proper output
   * @param destDir if (success == true) the location where the output was
   *                generated, otherwise undefined (possibly null)
   */
  synchronized public void javadocEnded(boolean success, File destDir) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).javadocEnded(success, destDir);
    }
  }
  
  /**
   * Called when the interactions window is reset.
   */
  synchronized public void interpreterReady() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interpreterReady();
    }
  }
  
  /**
   * Called if the interpreter reset failed.
   * (Subclasses must maintain listeners.)
   */
  synchronized public void interpreterResetFailed() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interpreterResetFailed();
    }
  }
  
  /**
   * Called when trying to test a non-TestCase class.
   */
  synchronized public void nonTestCase() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).nonTestCase();
    }
  }
  
  /**
   * Called when an undoable edit occurs.
   */
  synchronized public void undoableEditHappened() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).undoableEditHappened();
    }
  }
  
  /**
   * Called when the interactions JVM was closed by System.exit
   * or by being aborted. Immediately after this the interactions
   * will be reset.
   * @param status the exit code
   */
  synchronized public void interpreterExited(int status) {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interpreterExited(status);
    }
  }
  
  /**
   * Called to ask the listener if it is OK to abandon the current
   * document.
   */
  synchronized public boolean canAbandonFile(OpenDefinitionsDocument doc) {
    boolean poll = true;
    int size = _listeners.size();
    for(int i = 0; (poll && (i < size)); i++) {
      poll = poll && _listeners.get(i).canAbandonFile(doc);
    }
    return poll;
  }
  
  /**
   * Called after JUnit is finished running tests.
   */
  synchronized public void junitEnded() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).junitEnded();
    }
  }
  
  /**
   * Called after a compile is started by the GlobalModel.
   */
  synchronized public void compileStarted() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).compileStarted();
    }
  }
  
  /**
   * Called to ask the listener if it is OK to revert the current
   * document to a newer version saved on file.
   */
  synchronized public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
    boolean poll = true;
    int size = _listeners.size();
    for(int i = 0; (poll && (i < size)); i++) {
      poll = poll && _listeners.get(i).shouldRevertFile(doc);
    }
    return poll;
  }
  
  /**
   * Called after an interaction is started by the GlobalModel.
   */
  synchronized public void interactionStarted() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).interactionStarted();
    }
  }
  
  /**
   * Called when a compile has finished running.
   */
  synchronized public void compileEnded() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).compileEnded();
    }
  }
  
  /**
   * Called before attempting Javadoc, to give users a chance to save.
   * Do not continue with Javadoc if the user doesn't save!
   */
  synchronized public void saveBeforeJavadoc() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).saveBeforeJavadoc();
    }
  }
 
  /**
   * Called to demand that all files be saved before starting the debugger.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  synchronized public void saveBeforeDebug() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).saveBeforeDebug();
    }
  }
  
  /**
   * Called to demand that all files be saved before compiling.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  synchronized public void saveBeforeCompile() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).saveBeforeCompile();
    }
  }
  
  /**
   * Called to demand that all files be saved before running JUnit tests.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  synchronized public void saveBeforeJUnit() {
    int size = _listeners.size();
    for(int i = 0; i < size; i++) {
      _listeners.get(i).saveBeforeJUnit();
    }
  }
}

