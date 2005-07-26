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

package edu.rice.cs.drjava.model.debug;

import java.io.*;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;

import edu.rice.cs.util.swing.Utilities;

/**
 * Tests the JPDA-based debugger.
 *
 * @version $Id$
 */
public final class DebugTest extends DebugTestCase implements OptionConstants {
  /**
   * Tests startup and shutdown, ensuring that all appropriate fields are
   * initialized.  Ensures multiple startups and shutdowns work, even
   * after a reset, which changes the debug port.
   */
  public void testStartupAndShutdown() throws DebugException, InterruptedException {
    if (printMessages) System.out.println("----testStartupAndShutdown----");
    DebugTestListener debugListener = new DebugStartAndStopListener();
    _debugger.addListener(debugListener);

    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _setPendingNotifies(1);  // startup
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(1);  //fires
    debugListener.assertDebuggerShutdownCount(0);

    // Check fields and status
    assertTrue("Debug Manager should be ready", _debugger.isReady());
    assertNotNull("EventRequestManager should not be null after startup",
                  _debugger.getEventRequestManager());
    assertNotNull("PendingRequestManager should not be null after startup",
                  _debugger.getPendingRequestManager());

    // Shutdown the debugger
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _setPendingNotifies(1);  // shutdown
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(1);
    debugListener.assertDebuggerShutdownCount(1);  //fires

    // Start debugger again without resetting
    synchronized(_notifierLock) {
      _debugger.startup();
      _setPendingNotifies(1);  // startup
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(2);  //fires
    debugListener.assertDebuggerShutdownCount(1);

    // Reset interactions (which shuts down debugger)
    InterpretListener resetListener = new InterpretListener() {
      public void interpreterChanged(boolean inProgress) {
        // Don't notify: happens in the same thread
        interpreterChangedCount++;
      }
      public void interpreterResetting() {
        // Don't notify: happens in the same thread
        interpreterResettingCount++;
      }
      public void interpreterReady() {
        synchronized(_notifierLock) {
          interpreterReadyCount++;
          if (printEvents) System.out.println("interpreterReady " + interpreterReadyCount);
          _notifyLock();
        }
      }

      public void consoleReset() {
        consoleResetCount++;
      }
    };
    _model.addListener(resetListener);
    synchronized(_notifierLock) {
      _model.resetInteractions();
      _setPendingNotifies(2);  // shutdown, interpreterReady
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    _model.removeListener(resetListener);
    resetListener.assertInterpreterResettingCount(1);  //fires (no waiting)
    resetListener.assertInterpreterReadyCount(1);  //fires
    debugListener.assertDebuggerStartedCount(2);
    debugListener.assertDebuggerShutdownCount(2);  //fires


    // Start debugger again after reset
    synchronized(_notifierLock) {
      _debugger.startup();
      _setPendingNotifies(1);  // startup
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(3);  //fires
    debugListener.assertDebuggerShutdownCount(2);

    // Shutdown the debugger
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _setPendingNotifies(1);  // shutdown
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(3);
    debugListener.assertDebuggerShutdownCount(3);  //fires

    _debugger.removeListener(debugListener);
  }


  /**
   * Test that when two threads are suspended setCurrentThread can be used
   * to switch between them in the debugger
   */
  public synchronized void testMultiThreadedSetCurrentThread() throws Exception {
    if (printMessages) System.out.println("----testMultiThreadedSetCurrentThread----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("Monkey.java",
                                                   MONKEY_CLASS);

    // Set two breakpoints
    int index = MONKEY_CLASS.indexOf("System.out.println(\"I\'m a thread! Yeah!\");");
    _debugger.toggleBreakpoint(doc,index,11);
    index = MONKEY_CLASS.indexOf("System.out.println(\"James likes bananas!\");");
    _debugger.toggleBreakpoint(doc,index,17);

    // Run the main() method, hitting both breakpoints in different threads
    synchronized(_notifierLock) {
      interpretIgnoreResult("java Monkey");
      _setPendingNotifies(6); // (suspended, updated, breakpointReached) * 2
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    DebugThreadData threadA = new DebugThreadData(_debugger.getCurrentThread());
    DebugThreadData threadB = new DebugThreadData(_debugger.getThreadAt(1));
    synchronized(_notifierLock) {
      _asyncDoSetCurrentThread(threadB);
      _setPendingNotifies(2);  // updated, suspended
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    DebugThreadData thread1 = new DebugThreadData(_debugger.getThreadAt(1));
    DebugThreadData thread2 = new DebugThreadData(_debugger.getCurrentThread());

    // make sure threads have switched places
    assertTrue(thread1.getUniqueID() == threadA.getUniqueID());
    assertTrue(thread2.getUniqueID() == threadB.getUniqueID());

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that setCurrentThread works for multiple threads
   *
   * This test has been commented out because we do not support setting the
   * current thread to be an unsuspended thread right now
   *
  public synchronized void testMultiThreadedSetCurrentThread() throws Exception {
    if (printMessages) System.out.println("----testMultiThreadedSetCurrentThread----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("Suspender.java",
                                                   SUSPEND_CLASS);

    int index = SUSPEND_CLASS.indexOf("int a = 1;");
    _debugger.toggleBreakpoint(doc,index,5);

    // Run the main() method, hitting breakpoints
    synchronized(_notifierLock) {
      interpretIgnoreResult("java Suspender");
      _setPendingNotifies(3); // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    final DebugThreadData thread = new DebugThreadData(_debugger.getCurrentThread());
    synchronized(_notifierLock) {
       // _debugger.setCurrentThread(...);
       // must be executed in another thread because otherwise the notifies
       // will be received before the _notifierLock is released
      new Thread() {
        public void run() {
          try {
            _debugger.resume();
            _doSetCurrentThread(thread);
          }
          catch (DebugException excep) {
            excep.printStackTrace();
            fail("_doSetCurrentThread failed in testMultiThreadedSetCurrentThread");
          }
        }
      }.start();
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    // Ensure thread suspended
    debugListener.assertCurrThreadSuspendedCount(2);  //fires

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }*/

  /**
   * Tests that breakpoints behave correctly for multiple threads
   */
  public synchronized void testMultiThreadedBreakpointsAndStep() throws Exception {
    if (printMessages) System.out.println("----testMultiThreadedBreakpointsAndStep----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("Monkey.java",
                                                   MONKEY_CLASS);

    // Set breakpoints
    int index = MONKEY_CLASS.indexOf("System.out.println(\"I\'m a thread! Yeah!\");");
    _debugger.toggleBreakpoint(doc,index,11);
    index = MONKEY_CLASS.indexOf("System.out.println(\"I just woke up.  I\'m a big boy now.\");");
    _debugger.toggleBreakpoint(doc,index,16);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointSetCount(2);

    // Run the main method, hitting breakpoints
    synchronized(_notifierLock) {
      interpretIgnoreResult("java Monkey");
      _setPendingNotifies(6);  // (suspended, updated, breakpointReached) x 2
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    DebugThreadData thread = new DebugThreadData(_debugger.getCurrentThread());
    // Resumes one thread, finishing it and switching to the next break point
    synchronized(_notifierLock) {
      _asyncResume();
      _setPendingNotifies(2);  // suspended, updated
                            // no longer get a currThreadDied since we immediately
                            // switch to the next thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    DebugThreadData thread2 = new DebugThreadData(_debugger.getCurrentThread());
    assertTrue("testMultiThreadedBreakPoint thread references should not be equal",
               !thread.getName().equals(thread2.getName()));

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(3);  //fires
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertCurrThreadResumedCount(1);
    _debugger.removeListener(debugListener);

    if (printMessages) {
      System.out.println("Testing stepping...");
    }
    // Step
    StepTestListener stepTestListener = new StepTestListener();
    _debugger.addListener(stepTestListener);
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_INTO);
      _setPendingNotifies(2); // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    stepTestListener.assertStepRequestedCount(1);
    _debugger.removeListener(stepTestListener);

    DebugThreadData thread3 = new DebugThreadData(_debugger.getCurrentThread());
    assertEquals("testMultiThreadedBreakPoint thread references should be equal",
                 thread2.getName(), thread3.getName());

    // Resume until finished, waiting for interpret call to end
    _debugger.addListener(debugListener);
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      _asyncResume();
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
                            // we get a currThreadDied here since it's the last thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }



  /**
   * Tests that breakpoints behave correctly.
   */
  public synchronized void testBreakpoints() throws Exception {
    if (printMessages) System.out.println("----testBreakpoints----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);

   // Add breakpoint before class is loaded
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointSetCount(1);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) System.out.println("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    if (printMessages) System.out.println("adding another breakpoint");

    // Set another breakpoint (after is class loaded)
    _debugger.toggleBreakpoint(doc, DEBUG_CLASS.indexOf("System.out.println(\"Bar Line 2\")"), 9);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointSetCount(2);

    // Resume until next breakpoint
    synchronized(_notifierLock) {
      if (printMessages) System.out.println("resuming");
      _asyncResume();
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    if (printMessages) System.out.println("----After one resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(1);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(2);  //fires
    debugListener.assertCurrThreadSuspendedCount(2);  //fires
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Bar Line 1");
    assertInteractionsDoesNotContain("Bar Line 2");

    // Resume until finished, waiting for interpret call to end
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      if ( printMessages ) System.err.println("-------- Resuming --------");
      _asyncResume();
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
                            // here, we get a currThreadDied since it's the last thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    if (printMessages) System.out.println("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertThreadLocationUpdatedCount(2);
    debugListener.assertCurrThreadSuspendedCount(2);
    assertInteractionsContains("Foo Line 3");

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that the debugger will stop at a breakpoint in one class
   * when the invoking method resides in a class with the same
   * prefix in its name.  (bug #769764)
   * (ie. Class DrJavaDebugTest2 has a method which calls something
   * in class DrJavaDebugTest, which has a breakpoint.)
   */
  public synchronized void testBreakpointsWithSameNamePrefix() throws Exception {
    if (printMessages) System.out.println("----testBreakpointsWithSameNamePrefix----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);

   // Add breakpoint in DrJavaDebugClass before class is loaded
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("Bar Line 1"),8);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointSetCount(1);

    // Run the baz() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass2().baz()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) System.out.println("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Baz Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    // Resume until finished, waiting for interpret call to end
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      if ( printMessages ) System.err.println("-------- Resuming --------");
      _asyncResume();
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
                            // here, we get a currThreadDied since it's the last thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    if (printMessages) System.out.println("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(1);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertThreadLocationUpdatedCount(1);
    debugListener.assertCurrThreadSuspendedCount(1);
    assertInteractionsContains("Bar Line 2");

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that breakpoints and steps behave correctly.
   */
  public void testStepInto() throws Exception {
    if (printMessages) System.out.println("----testStepInto----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointSetCount(1);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) {
      System.out.println("----After breakpoint:\n" + getInteractionsText());
    }

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    // Step into bar() method
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_INTO);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // fires
    debugListener.assertCurrThreadSuspendedCount(2);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("Bar Line 1");

    // Step to next line
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) System.out.println("****"+getInteractionsText());
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(3);  // fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("Bar Line 1");
    assertInteractionsDoesNotContain("Bar Line 2");

    // Step to next line
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(3);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(3); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(4);  // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(4);  // fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("Bar Line 2");
    assertInteractionsDoesNotContain("Foo Line 3");

    // Step twice to print last line in Foo
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(5);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(5); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(6);  // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(6);  //fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("Foo Line 3");


    // Step again to finish, waiting for interpret call to end
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
                            // here, we get a currThreadDied since it's the last thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    debugListener.assertStepRequestedCount(6);  // fires (don't wait)
    debugListener.assertCurrThreadDiedCount(1);

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that stepping out of a method behaves correctly.
   */
  public synchronized void testStepOut() throws Exception {
    if (printMessages) {
      System.out.println("----testStepOut----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);

    // Set breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointSetCount(1);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) System.out.println("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  // fires
    debugListener.assertThreadLocationUpdatedCount(1);  // fires
    debugListener.assertCurrThreadSuspendedCount(1);  // fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    // Step into bar() method
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_INTO);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  //fires
    debugListener.assertCurrThreadSuspendedCount(2);  //fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("Bar Line 1");

    // Step out of method
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OUT);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) System.out.println("****"+getInteractionsText());
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("Bar Line 2");
    assertInteractionsDoesNotContain("Foo Line 3");

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that stepping works in a public class with a package
   */
  public synchronized void testStepOverWithPackage() throws Exception {
    if (printMessages) System.out.println("----testStepOverWithPackage----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Create the file in an "a" sub-directory
    File aDir = new File(_tempDir, "a");
    aDir.mkdir();
    File file = new File(aDir, "DrJavaDebugClassWithPackage.java");

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger(file, DEBUG_CLASS_WITH_PACKAGE);

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS_WITH_PACKAGE.indexOf("foo line 1"), 4);
    
    Utilities.clearEventQueue();
    debugListener.assertBreakpointSetCount(1);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new a.DrJavaDebugClassWithPackage().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) System.out.println("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("foo line 1");

    // Step over once
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // fires
    debugListener.assertCurrThreadSuspendedCount(2);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("foo line 1");
    assertInteractionsDoesNotContain("foo line 2");

    // Step over again
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    if (printMessages) System.out.println("****"+getInteractionsText());
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(3);  // fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("foo line 2");

    // Resume until finished, waiting for interpret call to finish
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      _asyncResume();
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
                            // here, we get a currThreadDied since it's the last thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    if (printMessages) System.out.println("----After resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);


    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests the utility function to get a relative directory for a package.
   */
  public void testGetPackageDir() {
    String class1 = "edu.rice.cs.drjava.model.MyTest";
    String class2 = "MyTest";
    String sep = System.getProperty("file.separator");

    assertEquals("package dir with package",
                 "edu" + sep + "rice" + sep + "cs" + sep +
                 "drjava" + sep + "model" + sep,
                 _debugger.getPackageDir(class1));
    assertEquals("package dir without package",
                 "",
                 _debugger.getPackageDir(class2));
  }
}
  
