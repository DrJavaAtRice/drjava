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

import java.util.Vector;
import java.io.File;

import edu.rice.cs.util.ReaderWriterLock;

/**
 * Keeps track of all listeners to the model, and has the ability
 * to notify them of some event.
 * 
 * All methods in this class must use the synchronization methods
 * provided by ReaderWriterLock.  This ensures that multiple notifications
 * (reads) can occur simultaneously, but only one thread can be adding
 * or removing listeners (writing) at a time, and no reads can occur
 * during a write.
 * 
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 * 
 * @version $Id$
 */
public class EventNotifier implements GlobalModelListener {
  /**
   * All GlobalModelListeners that are listening to the model.
   * Accesses to this collection are protected by the ReaderWriterLock.
   * The collection must be synchronized, since multiple readers could
   * access it at once.
   */
  private final Vector<GlobalModelListener> _listeners;
  
  /**
   * Provides synchronization primitives for solving the readers/writers
   * problem.  In EventNotifier, adding and removing listeners are considered
   * write operations, and all notifications are considered read operations.
   * 
   * Multiple reads are allowed simultaneously, but only one write can occur
   * at a time, and no reads can occur during a write.
   */
  private final ReaderWriterLock _lock;
  
  /**
   * Creates a new EventNotifier with an empty list of listeners.
   */
  public EventNotifier() {
    _listeners = new Vector<GlobalModelListener>();
    _lock = new ReaderWriterLock();
  }
  
  // -------------------- WRITER METHODS --------------------
  
  /**
   * Add a listener to the model.
   * @param listener a listener that reacts on events
   */
  public void addListener(GlobalModelListener listener) {
    _lock.startWrite();
    try {
      _listeners.addElement(listener);
    }
    finally {
      _lock.endWrite();
    }
  }
  
  /**
   * Remove a listener from the model.
   * @param listener a listener that reacts on events
   */
  public void removeListener(GlobalModelListener listener) {
    _lock.startWrite();
    try {
      _listeners.removeElement(listener);
    }
    finally {
      _lock.endWrite();
    }
  }
  
  /**
   * Removes all listeners from this notifier.
   */
  public void removeAllListeners() {
    _lock.startWrite();
    try {
      _listeners.removeAllElements();
    }
    finally {
      _lock.endWrite();
    }
  }
  
  
  // -------------------- READER METHODS --------------------
  
  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
  public void notifyListeners(Notifier n) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        n.notifyListener(_listeners.get(i));
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Allows the GlobalModel to ask its listeners a yes/no question and
   * receive a response.
   * @param EventPoller p the question being asked of the listeners
   * @return the listeners' responses ANDed together, true if they all
   * agree, false if some disagree
   */
  public boolean pollListeners(Poller p) {
    _lock.startRead();
    try {
      boolean poll = true;
      
      int size = _listeners.size();
      for(int i = 0; (poll && (i < size)); i++) {
        poll = poll && p.poll(_listeners.get(i));
      }
      return poll;
    }
    finally {
      _lock.endRead();
    }
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
   * Called when a file's main method is about to be run.
   */
  public void runStarted(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).runStarted(doc);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after JUnit is started by the GlobalModel.
   */
  public void junitStarted(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitStarted(doc);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when the active interpreter is changed.
   * @param inProgress Whether the new interpreter is currently in progress
   * with an interaction (ie. whether an interactionEnded event will be fired)
   */
  public void interpreterChanged(boolean inProgress) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interpreterChanged(inProgress);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called to indicate that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void junitSuiteStarted(int numTests) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitSuiteStarted(numTests);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when the interactions window generates a syntax error.
   *
   * @param offset the error's offset into the InteractionsDocument
   * @param length the length of the error
   */
  public void interactionErrorOccurred(int offset, int length) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interactionErrorOccurred(offset, length);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after a new document is created.
   */
  public void newFileCreated(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).newFileCreated(doc);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when an interaction has finished running.
   */
  public void interactionEnded() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interactionEnded();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when the console window is reset.
   */
  public void consoleReset() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).consoleReset();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after the current document is saved.
   */
  public void fileSaved(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).fileSaved(doc);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  public void junitTestStarted(String name) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitTestStarted(name);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after a file is opened and read into the current document.
   */
  public void fileOpened(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).fileOpened(doc);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when the interactionsJVM has begun resetting.
   */
  public void interpreterResetting() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interpreterResetting();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after Javadoc is started by the GlobalModel.
   */
  public void javadocStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).javadocStarted();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after a document is closed.
   */
  public void fileClosed(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).fileClosed(doc);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void junitTestEnded(OpenDefinitionsDocument doc, String name, boolean wasSuccesful, boolean causedError) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitTestEnded(doc, name, wasSuccesful, causedError);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after a document is reverted.
   */
  public void fileReverted(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).fileReverted(doc);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after Javadoc is finished.
   * @param success whether the Javadoc operation generated proper output
   * @param destDir if (success == true) the location where the output was
   *                generated, otherwise undefined (possibly null)
   */
  public void javadocEnded(boolean success, File destDir) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).javadocEnded(success, destDir);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when the interactions window is reset.
   */
  public void interpreterReady() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interpreterReady();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called if the interpreter reset failed.
   * (Subclasses must maintain listeners.)
   */
  public void interpreterResetFailed() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interpreterResetFailed();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when trying to test a non-TestCase class.
   */
  public void nonTestCase() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).nonTestCase();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when an undoable edit occurs.
   */
  public void undoableEditHappened() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).undoableEditHappened();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when the interactions JVM was closed by System.exit
   * or by being aborted. Immediately after this the interactions
   * will be reset.
   * @param status the exit code
   */
  public void interpreterExited(int status) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interpreterExited(status);
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called to ask the listener if it is OK to abandon the current
   * document.
   */
  public boolean canAbandonFile(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      boolean poll = true;
      int size = _listeners.size();
      for(int i = 0; (poll && (i < size)); i++) {
        poll = poll && _listeners.get(i).canAbandonFile(doc);
      }
      return poll;
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after JUnit is finished running tests.
   */
  public void junitEnded() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitEnded();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after a compile is started by the GlobalModel.
   */
  public void compileStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).compileStarted();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called to ask the listener if it is OK to revert the current
   * document to a newer version saved on file.
   */
  public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
    _lock.startRead();
    try {
      boolean poll = true;
      int size = _listeners.size();
      for(int i = 0; (poll && (i < size)); i++) {
        poll = poll && _listeners.get(i).shouldRevertFile(doc);
      }
      return poll;
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called after an interaction is started by the GlobalModel.
   */
  public void interactionStarted() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).interactionStarted();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called when a compile has finished running.
   */
  public void compileEnded() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).compileEnded();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called to demand that all files be saved before compiling.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeCompile() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).saveBeforeCompile();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called to demand that all files be saved before running the main method of
   * a document. It is up to the caller of this method to check if the documents
   * have been saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeRun() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).saveBeforeRun();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called to demand that all files be saved before running JUnit tests.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeJUnit() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).saveBeforeJUnit();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called before attempting Javadoc, to give users a chance to save.
   * Do not continue with Javadoc if the user doesn't save!
   */
  public void saveBeforeJavadoc() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).saveBeforeJavadoc();
      }
    }
    finally {
      _lock.endRead();
    }
  }
  
  /**
   * Called to demand that all files be saved before starting the debugger.
   * It is up to the caller of this method to check if the documents have been
   * saved, using IGetDocuments.hasModifiedDocuments().
   */
  public void saveBeforeDebug() {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).saveBeforeDebug();
      }
    }
    finally {
      _lock.endRead();
    }
  }
}

