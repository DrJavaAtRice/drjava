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

package edu.rice.cs.drjava.model.junit;

import java.util.List;
import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * Keeps track of all listeners to a JUnitModel, and has the ability
 * to notify them of some event.
 * <p>
 *
 * This class has a specific role of managing JUnitListeners.  Other
 * classes with similar names use similar code to perform the same function for
 * other interfaces, e.g. InteractionsEventNotifier and GlobalEventNotifier.
 * These classes implement the appropriate interface definition so that they
 * can be used transparently as composite packaging for a particular listener
 * interface.
 * <p>
 *
 * Components which might otherwise manage their own list of listeners use
 * EventNotifiers instead to simplify their internal implementation.  Notifiers
 * should therefore be considered a private implementation detail of the
 * components, and should not be used directly outside of the "host" component.
 * <p>
 *
 * All methods in this class must use the synchronization methods
 * provided by ReaderWriterLock.  This ensures that multiple notifications
 * (reads) can occur simultaneously, but only one thread can be adding
 * or removing listeners (writing) at a time, and no reads can occur
 * during a write.
 * <p>
 *
 * <i>No</i> methods on this class should be synchronized using traditional
 * Java synchronization!
 * <p>
 *
 * @version $Id$
 */
class JUnitEventNotifier extends EventNotifier<JUnitListener>
    implements JUnitListener {

  /**
   * Called when trying to test a non-TestCase class.
   * @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).nonTestCase(isTestAll);
      }
    }
    finally {
      _lock.endRead();
    }
  }

  /**
   * Called after JUnit is started by the GlobalModel.
   */
  public void junitStarted(List<OpenDefinitionsDocument> docs) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitStarted(docs);
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
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void junitTestEnded(String name, boolean wasSuccesful, boolean causedError) {
    _lock.startRead();
    try {
      int size = _listeners.size();
      for(int i = 0; i < size; i++) {
        _listeners.get(i).junitTestEnded(name, wasSuccesful, causedError);
      }
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
}

