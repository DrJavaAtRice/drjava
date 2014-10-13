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

package edu.rice.cs.drjava.model.debug.jpda;

import java.io.*;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.debug.*;

import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.swing.Utilities;

/** Tests the JPDA-based debugger.
  * TODO: Why are these tests commented out?!
  * @version $Id: DebugTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class DebugTest extends JPDADebugTestCase implements OptionConstants {
  
  public void testStub() { /* here just to prevent a "no tests found" error */ }
  
  /** Tests startUp and shutdown, ensuring that all appropriate fields are initialized.  Ensures multiple startups
    * and shutdowns work, even after a reset, which changes the debug port.
    */
  public void XXXtestStartupAndShutdown() throws DebugException, InterruptedException {
    _log.log("----testStartupAndShutdown----");
    DebugTestListener debugListener = new DebugStartAndStopListener();
    _debugger.addListener(debugListener);
    
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startUp();
      _setPendingNotifies(1);  // startUp
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(1);  //fires
    debugListener.assertDebuggerShutdownCount(0);
    
    // Check fields and status
    assertTrue("Debug Manager should be ready", _debugger.isReady());
    assertNotNull("EventRequestManager should not be null after startUp", _debugger.getEventRequestManager());
    assertNotNull("PendingRequestManager should not be null after startUp", _debugger.getPendingRequestManager());
    
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
      _debugger.startUp();
      _setPendingNotifies(1);  // startUp
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(2);  //fires
    debugListener.assertDebuggerShutdownCount(1);
    
    // Define listener that will count notification events
    InterpretListener resetListener = new InterpretListener() {
      public void interactionStarted() {
        // Don't notify: happens in the same thread
        _log.log("interactionStarted called in resetListener");
        interactionStartCount++;
      }
      public void interactionEnded() {
        // Don't notify: happens in the same thread
        _log.log("interactionEnded called in resetListener");
        interactionEndCount++;
      }
      public void interpreterChanged(boolean inProgress) {
        // Don't notify: happens in the same thread
        _log.log("interpreterChanged called in resetListener");
        interpreterChangedCount++;
      }
      public void interpreterResetting() {
        // Don't notify: happens in the same thread
        _log.log("interpreterResetting called in resetListener");
        interpreterResettingCount++;
      }
      public void interpreterReady(File wd) {
        synchronized(_notifierLock) {
          interpreterReadyCount++;
          _log.log("interpreterReady " + interpreterReadyCount);
          _notifyLock();
        }
      }
      public void consoleReset() { consoleResetCount++; }
    };
    
    // Install listener
    _model.addListener(resetListener); // shutdown, interpreterReady
    _setPendingNotifies(2);
    
    // Use the interpeter so that resetInteractions restarts the slave JVM
//    interpret("2+2");
    
    _model.resetInteractions(FileOps.NULL_FILE);
    
    synchronized(_notifierLock) { while (_pendingNotifies > 0) _notifierLock.wait(); }
    
    _model.removeListener(resetListener);
    
    resetListener.assertInterpreterResettingCount(1);  //fires (no waiting)
    resetListener.assertInterpreterReadyCount(1);  //fires
    debugListener.assertDebuggerStartedCount(2);
    debugListener.assertDebuggerShutdownCount(2);  //fires
    
    
    // Start debugger again after reset
    synchronized(_notifierLock) {
      _debugger.startUp();
      _setPendingNotifies(1);  // startUp
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
  
  
  /** Test that when two threads are suspended setCurrentThread can be used
   * to switch between them in the debugger
   */
  public synchronized void XXXtestMultiThreadedSetCurrentThread() throws Exception {
    _log.log("----testMultiThreadedSetCurrentThread----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    
    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("Monkey.java",
                                                   MONKEY_CLASS);
    
    // Set two breakpoints
    int index = MONKEY_CLASS.indexOf("System.out.println(\"I\'m a thread! Yeah!\");");
    _debugger.toggleBreakpoint(doc,index, true);
    index = MONKEY_CLASS.indexOf("System.out.println(\"James likes bananas!\");");
    _debugger.toggleBreakpoint(doc,index, true);
    
    // Run the main() method, hitting both breakpoints in different threads
    synchronized(_notifierLock) {
      interpretIgnoreResult("java Monkey");
      _setPendingNotifies(6); // (suspended, updated, breakpointReached) * 2
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    DebugThreadData threadA = new JPDAThreadData(_debugger.getCurrentThread());
    DebugThreadData threadB = new JPDAThreadData(_debugger.getThreadAt(1));
    synchronized(_notifierLock) {
      _asyncDoSetCurrentThread(threadB);
      _setPendingNotifies(2);  // updated, suspended
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    DebugThreadData thread1 = new JPDAThreadData(_debugger.getThreadAt(1));
    DebugThreadData thread2 = new JPDAThreadData(_debugger.getCurrentThread());
    
    // make sure threads have switched places
    assertTrue(thread1.getUniqueID() == threadA.getUniqueID());
    assertTrue(thread2.getUniqueID() == threadB.getUniqueID());
    
    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
  }
  
  /** Tests that setCurrentThread works for multiple threads
   *
   * This test has been commented out because we do not support setting the
   * current thread to be an unsuspended thread right now
   *
   public synchronized void testMultiThreadedSetCurrentThread() throws Exception {
   _log.log("----testMultiThreadedSetCurrentThread----");
   BreakpointTestListener debugListener = new BreakpointTestListener();
   _debugger.addListener(debugListener);
   
   // Start up
   OpenDefinitionsDocument doc = _startupDebugger("Suspender.java",
   SUSPEND_CLASS);
   
   int index = SUSPEND_CLASS.indexOf("int a = 1;");
   _debugger.toggleBreakpoint(doc,index,true);
   
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
  
  /** Tests that breakpoints behave correctly for multiple threads. */
  public synchronized void XXXtestMultiThreadedBreakpointsAndStep() throws Exception {
    _log.log("----testMultiThreadedBreakpointsAndStep----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    
    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("Monkey.java", MONKEY_CLASS);
    
    // Set breakpoints
    int index = MONKEY_CLASS.indexOf("System.out.println(\"I\'m a thread! Yeah!\");");
    _debugger.toggleBreakpoint(doc,index,true);
    index = MONKEY_CLASS.indexOf("System.out.println(\"I just woke up.  I\'m a big boy now.\");");
    _debugger.toggleBreakpoint(doc,index,true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(2);
    
    // Run the main method, hitting breakpoints
    synchronized(_notifierLock) {
      interpretIgnoreResult("java Monkey");
      _setPendingNotifies(6);  // (suspended, updated, breakpointReached) x 2
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    DebugThreadData thread = new JPDAThreadData(_debugger.getCurrentThread());
    // Resumes one thread, finishing it and switching to the next break point
    synchronized(_notifierLock) {
      _asyncResume();
      _setPendingNotifies(2);  // suspended, updated
      // no longer get a currThreadDied since we immediately
      // switch to the next thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    DebugThreadData thread2 = new JPDAThreadData(_debugger.getCurrentThread());
    assertTrue("testMultiThreadedBreakPoint thread references should not be equal",
               !thread.getName().equals(thread2.getName()));
    
    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(3);  //fires
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertCurrThreadResumedCount(1);
    _debugger.removeListener(debugListener);
    
    _log.log("Testing stepping...");
    
    // Step
    StepTestListener stepTestListener = new StepTestListener();
    _debugger.addListener(stepTestListener);
    synchronized(_notifierLock) {
      _asyncStep(Debugger.StepType.STEP_INTO);
      _setPendingNotifies(2); // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    stepTestListener.assertStepRequestedCount(1);
    _debugger.removeListener(stepTestListener);
    
    DebugThreadData thread3 = new JPDAThreadData(_debugger.getCurrentThread());
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
  
  
  
  /** Tests that breakpoints behave correctly.
   */
  public synchronized void XXXtestBreakpoints() throws Exception {
    _log.log("----testBreakpoints----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    
    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);
    
    // Add breakpoint before class is loaded
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("----After breakpoint:\n" + getInteractionsText());
    
    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");
    
    _log.log("adding another breakpoint");
    
    // Set another breakpoint (after is class loaded)
    _debugger.toggleBreakpoint(doc, DEBUG_CLASS.indexOf("System.out.println(\"Bar Line 2\")"), true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(2);
    
    // Resume until next breakpoint
    synchronized(_notifierLock) {
      _log.log("resuming");
      _asyncResume();
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    _log.log("----After one resume:\n" + getInteractionsText());
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
      _log.log("-------- Resuming --------");
      _asyncResume();
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
      // here, we get a currThreadDied since it's the last thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);
    
    _log.log("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertThreadLocationUpdatedCount(2);
    debugListener.assertCurrThreadSuspendedCount(2);
    assertInteractionsContains("Foo Line 3");
    
    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }
  
  /** Tests that the debugger will stop at a breakpoint in one class
   * when the invoking method resides in a class with the same
   * prefix in its name.  (bug #769764)
   * (ie. Class DrJavaDebugTest2 has a method which calls something
   * in class DrJavaDebugTest, which has a breakpoint.)
   */
  public synchronized void XXXtestBreakpointsWithSameNamePrefix() throws Exception {
    _log.log("----testBreakpointsWithSameNamePrefix----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    _debugger.addListener(debugListener);
    
    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);
    
    // Add breakpoint in DrJavaDebugClass before class is loaded
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("Bar Line 1"),true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);
    
    // Run the baz() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass2().baz()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("----After breakpoint:\n" + getInteractionsText());
    
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
      _log.log("-------- Resuming --------");
      _asyncResume();
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
      // here, we get a currThreadDied since it's the last thread
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);
    
    _log.log("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(1);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertThreadLocationUpdatedCount(1);
    debugListener.assertCurrThreadSuspendedCount(1);
    assertInteractionsContains("Bar Line 2");
    
    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }
  
  /** Tests that breakpoints and steps behave correctly.
   */
  public void XXXtestStepInto() throws Exception {
    _log.log("----testStepInto----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);
    
    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);
    
    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("----After breakpoint:\n" + getInteractionsText());
    
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
      _asyncStep(Debugger.StepType.STEP_INTO);
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
      _asyncStep(Debugger.StepType.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("****" + getInteractionsText());
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
      _asyncStep(Debugger.StepType.STEP_OVER);
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
      _asyncStep(Debugger.StepType.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    synchronized(_notifierLock) {
      _asyncStep(Debugger.StepType.STEP_OVER);
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
      _asyncStep(Debugger.StepType.STEP_OVER);
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
  
  /** Tests that stepping out of a method behaves correctly. */
  public synchronized void XXXtestStepOut() throws Exception {
    _log.log("----testStepOut----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);
    
    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);
    
    // Set breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("----After breakpoint:\n" + getInteractionsText());
    
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
      _asyncStep(Debugger.StepType.STEP_INTO);
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
      _asyncStep(Debugger.StepType.STEP_OUT);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("****" + getInteractionsText());
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
  
  /** Tests that stepping works in a public class with a package
   */
  public synchronized void XXXtestStepOverWithPackage() throws Exception {
    _log.log("----testStepOverWithPackage----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);
    
    // Create the file in an "a" sub-directory
    File aDir = new File(_tempDir, "a");
    aDir.mkdir();
    File file = new File(aDir, "DrJavaDebugClassWithPackage.java");
    
    // Start up
    OpenDefinitionsDocument doc = _startupDebugger(file, DEBUG_CLASS_WITH_PACKAGE);
    
    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS_WITH_PACKAGE.indexOf("foo line 1"), true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new a.DrJavaDebugClassWithPackage().foo()");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("----After breakpoint:\n" + getInteractionsText());
    
    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("foo line 1");
    
    // Step over once
    synchronized(_notifierLock) {
      _asyncStep(Debugger.StepType.STEP_OVER);
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
      _asyncStep(Debugger.StepType.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    
    _log.log("****" + getInteractionsText());
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
    
    _log.log("----After resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);
    
    
    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }
  
  /** Tests the utility function to get a relative directory for a package.
   */
  public void XXXtestGetPackageDir() {
    String class1 = "edu.rice.cs.drjava.model.MyTest";
    String class2 = "MyTest";
    String sep = System.getProperty("file.separator");
    
    assertEquals("package dir with package",
                 "edu" + sep + "rice" + sep + "cs" + sep +
                 "drjava" + sep + "model" + sep,
                 DrJavaFileUtils.getPackageDir(class1));
    assertEquals("package dir without package",
                 "",
                 DrJavaFileUtils.getPackageDir(class2));
  }
}

