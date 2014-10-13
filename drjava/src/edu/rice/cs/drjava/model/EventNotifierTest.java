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

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.GlobalModelTestCase.TestListener;

/**
 * Tests the functionality of the class that notifies listeners
 * of a global model.
 * @version $Id: EventNotifierTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class EventNotifierTest extends DrJavaTestCase {
  
  protected GlobalEventNotifier _notifier;
  
  public void setUp() throws Exception {
    super.setUp();
    _notifier = new GlobalEventNotifier();
  }
  
  public void tearDown() throws Exception {
    _notifier = null;
    super.tearDown();
  }
  
  /** Checks that the notifier adds and removes listeners correctly,
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
    _notifier.junitSuiteStarted(1);
    
    listener1.assertJUnitSuiteStartedCount(1);
    listener2.assertJUnitSuiteStartedCount(1);

    //remove one listener and fire another event
    _notifier.removeListener(listener2);
    _notifier.interpreterExited(1);

    listener1.assertInterpreterExitedCount(1);
    listener2.assertInterpreterExitedCount(0);
  }
  
  /** Checks that the notifier can poll multiple listeners.
   */
  public void testPollListeners() {
    TestListener trueListener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) { return true; }
    };
    TestListener falseListener = new TestListener() {
      public boolean canAbandonFile(OpenDefinitionsDocument doc) { return false; }
    };

    // Make sure trueListener says yes
    _notifier.addListener(trueListener);
    boolean result = _notifier.canAbandonFile(null);
    assertTrue("should be able to abandon file", result);
    
    // Make sure falseListener says no
    _notifier.addListener(falseListener);
    result = _notifier.canAbandonFile(null);
    assertTrue("should not be able to abandon file", !result);
  }
}
