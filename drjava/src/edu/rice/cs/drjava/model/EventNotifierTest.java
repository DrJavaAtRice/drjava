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

import junit.framework.TestCase;

import edu.rice.cs.drjava.model.GlobalModelTestCase.TestListener;

/**
 * Tests the functionality of the class that notifies listeners
 * of a global model.
 * @version $Id$
 */
public final class EventNotifierTest extends TestCase {
  
  protected GlobalEventNotifier _notifier;
  
  public void setUp() { _notifier = new GlobalEventNotifier(); }
  
  public void tearDown() { _notifier = null; }
  
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
    _notifier.notifyListeners(new GlobalEventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.junitSuiteStarted(1);
      }
    });
    listener1.assertJUnitSuiteStartedCount(1);
    listener2.assertJUnitSuiteStartedCount(1);

    //remove one listener and fire another event
    _notifier.removeListener(listener2);
    _notifier.notifyListeners(new GlobalEventNotifier.Notifier() {
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
    boolean result = _notifier.pollListeners(new GlobalEventNotifier.Poller() {
      public boolean poll(GlobalModelListener l) {
        return l.canAbandonFile(null);
      }
    });
    assertTrue("should be able to abandon file", result);
    
    // Make sure falseListener says no
    _notifier.addListener(falseListener);
    result = _notifier.pollListeners(new GlobalEventNotifier.Poller() {
      public boolean poll(GlobalModelListener l) {
        return l.canAbandonFile(null);
      }
    });
    assertTrue("should not be able to abandon file", !result);
  }
}
