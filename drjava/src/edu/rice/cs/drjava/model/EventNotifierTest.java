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

import junit.framework.TestCase;

import edu.rice.cs.drjava.model.GlobalModelTestCase.TestListener;

/**
 * Tests the functionality of the class that notifies listeners
 * of a global model.
 * @version $Id$
 */
public final class EventNotifierTest extends TestCase {
  
  protected EventNotifier _notifier;
  
  public void setUp() {
    _notifier = new EventNotifier();
  }
  
  public void tearDown() {
    _notifier = null;
  }
  
  /**
   * Checks that the notifier adds and removes listeners correctly,
   * notifying the correct ones on a particular event.
   */
  public void testAddAndRemoveListeners() {
    TestListener listener1 = new TestListener() {
      public void junitSuiteStarted(int numTests) {
        junitSuiteStartedCount++;
      }
      public void interpreterExited(int status) {
        interpreterExitedCount++;
      }
    };
    TestListener listener2 = new TestListener() {
      public void junitSuiteStarted(int numTests) {
        junitSuiteStartedCount++;
      }
    };
    
    _notifier.addListener(listener1);
    _notifier.addListener(listener2);
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.junitSuiteStarted(1);
      }
    });
    listener1.assertJUnitSuiteStartedCount(1);
    listener2.assertJUnitSuiteStartedCount(1);

    //remove one listener and fire another event
    _notifier.removeListener(listener2);
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interpreterExited(1);
      }
    });
    listener1.assertInterpreterExitedCount(1);
  }
  
  /**
   * Checks that the notifier can poll multiple listeners.
   */
  public void testPollListeners() {
    TestListener trueListener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        return true;
      }
    };
    TestListener falseListener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) {
        return false;
      }
    };

    // Make sure trueListener says yes
    _notifier.addListener(trueListener);
    boolean result = _notifier.pollListeners(new EventNotifier.Poller() {
      public boolean poll(GlobalModelListener l) {
        return l.canAbandonFile(null);
      }
    });
    assertTrue("should be able to abandon file", result);
    
    // Make sure falseListener says no
    _notifier.addListener(falseListener);
    result = _notifier.pollListeners(new EventNotifier.Poller() {
      public boolean poll(GlobalModelListener l) {
        return l.canAbandonFile(null);
      }
    });
    assertTrue("should not be able to abandon file", !result);
  }
}