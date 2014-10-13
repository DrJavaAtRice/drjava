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
import java.util.ArrayList;
import java.util.Vector;


import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** More tests over the JPDA debugger.
 *  @version $Id: DebugContextTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class DebugContextTest extends JPDADebugTestCase {
  
  public void testStub() { /* here just to prevent a "no tests found" error */ }
  
  
//  inherits _log from GlobalModelTestCase 
//  public static Log _log = new Log("Debug.txt", false);
  
  /** Tests that the sourcepath config option properly adds files to the search directories. */
  public void XXXtestDebugSourcepath() throws Exception {
    debug.logStart();
    _log.log("----testDebugSourcePath----");
    final StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    final OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java", DEBUG_CLASS);
    final Vector<File> path = new Vector<File>();
    path.add(_tempDir);  // directory where doc's file is saved

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),true);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    // Source is highlighted because document is stored in breakpoint
    debugListener.assertThreadLocationUpdatedCount(1);  // fires

    // Step into bar() method
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_INTO);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    // Source is highlighted because file is in source root set
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // fires

    // Close file so it won't be in source root set
    _model.closeFile(doc);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionRemovedCount(1);

    // Step to next line
    synchronized(_notifierLock) {
      _setPendingNotifies(1);  // suspended
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    // Source is not highlighted
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // doesn't fire

//    synchronized(_debugger) {
      // Add _tempDir to our sourcepath
      Utilities.invokeAndWait(new Runnable() { 
        public void run() { 
          DrJava.getConfig().setSetting(OptionConstants.DEBUG_SOURCEPATH, path);
        }
      });
//    }

    // Step to next line
    synchronized(_notifierLock) {
      _asyncStep(Debugger.StepType.STEP_OVER);
      _setPendingNotifies(2);  // suspended, updated
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    // Source is highlighted because file is now on sourcepath
    debugListener.assertStepRequestedCount(3);  // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    
    _log.log("Shutting down testDebugSourcePath");

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }

  /** Tests that breakpoints behave correctly in non-public classes. */
  public synchronized void XXXtestBreakpointsAndStepsInNonPublicClasses() throws Exception {
    debug.logStart();
    _log.log("----testBreakpointsAndStepsInNonPublicClasses----");
    final StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java", DEBUG_CLASS);

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("Baz Line 1"),true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);

    // Run the baz() method, hitting breakpoint
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      interpretIgnoreResult("new DrJavaDebugClass2().baz()");
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

//    _log.log("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("Baz Line 1");

//    _log.log("adding another breakpoint");

    // Set another breakpoint (after is class loaded)
    _debugger.toggleBreakpoint(doc, DEBUG_CLASS.indexOf("System.out.println(\"Bar Line 2\")"), true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(2);

    // Step to next line
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

//    _log.log("****" + getInteractionsText());
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2); // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(2);  //fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("Baz Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    // Resume until next breakpoint
    synchronized(_notifierLock) {
//      _log.log("resuming");
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      _asyncResume();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
//    _log.log("----After one resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(3);  //fires
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Bar Line 1");
    assertInteractionsDoesNotContain("Bar Line 2");

//    _log.log("-------- Adding interpret listener --------");
    // Resume until finished, waiting for call to interpret to end
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
//      _log.log("-------- resuming --------");
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied (since it's the last thread)
      _asyncResume();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    _log.log("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);
    assertInteractionsContains("Bar Line 2");

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }

  /** Tests that stepping into a breakpoint works. */
  public synchronized void XXXtestStepIntoOverBreakpoint() throws Exception {
    debug.logStart();
    _log.log("----testStepIntoOverBreakpoint----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java", DEBUG_CLASS);

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("Foo Line 1"),true);
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();\n"),true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(2);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

//    _log.log("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("Foo Line 1");

    // Step over once
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // fires
    debugListener.assertCurrThreadSuspendedCount(2);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Foo Line 1");

    // Step over again
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

//    _log.log("****" + getInteractionsText());
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(3);  // fires
    debugListener.assertBreakpointReachedCount(1);

    // Resume until finished, waiting for interpret call to finish
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied (since it's the last thread)
      _asyncResume();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

//    _log.log("----After resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);

    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionRemovedCount(2);  //fires (no waiting)

    // Shutdown the debugger
//    _log.log("Shutting down ...");

    synchronized(_notifierLock) {
      _setPendingNotifies(1);  // shutdown
      _debugger.shutdown();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    debugListener.assertDebuggerShutdownCount(1);  //fires
    _log.log("Completed testStepIntoOverBreakpoint");
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }

  /** Tests that static fields are consistent across different interpreter contexts. */
  public void XXXtestStaticFieldsConsistent() throws Exception {
    debug.logStart();
    _log.log("----testStaticFieldsConsistent----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugStaticField.java", CLASS_WITH_STATIC_FIELD);

    // Set a breakpoint
    _debugger.toggleBreakpoint(doc,CLASS_WITH_STATIC_FIELD.indexOf("System.out.println"),true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);

    // Run the main method, hitting breakpoint
    synchronized(_notifierLock) {
      _setPendingNotifies(6);  // (suspended, updated, breakpointReached) *2
      interpretIgnoreResult("java DrJavaDebugStaticField");
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    // TODO: Why is this call being made?
    @SuppressWarnings("unused") DebugThreadData threadA = new JPDAThreadData(_debugger.getCurrentThread());
    DebugThreadData threadB = new JPDAThreadData(_debugger.getThreadAt(1));
//    _log.log("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(2);  //fires
    debugListener.assertCurrThreadSuspendedCount(2);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertEquals("x has correct value at start", "0", interpret("DrJavaDebugStaticField.x"));
    assertEquals("assigning x succeeds", "5", interpret("DrJavaDebugStaticField.x = 5"));
    assertEquals("assignment reflected in this", "5", interpret("this.x"));

    // Step over once
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadSuspendedCount(3);  // fires
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("x == 5");
    assertEquals("x retains correct value after step", "5", interpret("DrJavaDebugStaticField.x"));
    assertEquals("this has correct value for x after step", "5", interpret("this.x"));

    // Step over again
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
//    _log.log("****" + getInteractionsText());
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(4);  // fires
    debugListener.assertCurrThreadSuspendedCount(4);  // fires
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertCurrThreadDiedCount(0);
    assertEquals("x has correct value after increment", "6", interpret("DrJavaDebugStaticField.x"));
    assertEquals("this has correct value for x after increment", "6", interpret("this.x"));

    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncDoSetCurrentThread(threadB);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpret("");
    assertInteractionsContains("The current thread has changed.");
    assertEquals("x has correct value in other thread", "6", interpret("DrJavaDebugStaticField.x"));
    assertEquals("this has correct value for x in other thread", "6", interpret("this.x"));

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }

  /** Tests that watches can correctly see the values of local variables, fields and fields of outer classes. Also tests
    * that we can watch objects initialized to null (bug #771040) and that we can watch final local variables of outer
    * classes (bug #769174).
    */
  public void XXXtestNonStaticWatches() throws Exception {
    debug.logStart();
    _log.log("----testNonStaticWatches----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);
    
    final String monkey = MONKEY_WITH_INNER_CLASS;

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("Monkey.java", monkey);

    // Set a breakpoint
    _debugger.toggleBreakpoint(doc, monkey.indexOf("innerMethodFoo = 12;"), true);
    _debugger.toggleBreakpoint(doc, monkey.indexOf("System.out.println(\"localVar = \" + localVar);"), true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(2);

    // Run an inner method, hitting breakpoint
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      interpretIgnoreResult("new Monkey().bar()");//new MonkeyInner().new MonkeyInnerInner().innerMethod()");
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    _debugger.addWatch("foo");
    _debugger.addWatch("innerFoo");
    _debugger.addWatch("innerInnerFoo");
    _debugger.addWatch("innerMethodFoo");
    _debugger.addWatch("asdf");
    _debugger.addWatch("nullString");
    _debugger.addWatch("localVar");
    
    Utilities.clearEventQueue();
    debugListener.assertWatchSetCount(7);

//    _log.log("first step");
    
    // Step to line 11
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // fires
    debugListener.assertCurrThreadSuspendedCount(2);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

    ArrayList<DebugWatchData> watches = _debugger.getWatches();
    assertEquals("watch name incorrect", "foo", watches.get(0).getName());
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch name incorrect", "innerInnerFoo", watches.get(2).getName());
    assertEquals("watch name incorrect", "innerMethodFoo", watches.get(3).getName());
    assertEquals("watch name incorrect", "asdf", watches.get(4).getName());
    assertEquals("watch name incorrect", "nullString", watches.get(5).getName());
    assertEquals("watch value incorrect", "6", watches.get(0).getValue());
    assertEquals("watch value incorrect", "8", watches.get(1).getValue());
    assertEquals("watch value incorrect", "10", watches.get(2).getValue());
    assertEquals("watch value incorrect", "12", watches.get(3).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(4).getValue());
    assertEquals("watch value incorrect", "null", watches.get(5).getValue());
    assertEquals("watch type incorrect", "java.lang.String", watches.get(5).getType());

    interpret("innerFoo = 0");
    watches = _debugger.getWatches();
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch value incorrect", "0", watches.get(1).getValue());

    interpret("innerFoo = 8");
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch value incorrect", "8", watches.get(1).getValue());

//    _log.log("second step in " + this);
    
    // Step to line 12
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadSuspendedCount(3);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

//      _log.log("third step in " + this);
      
    // Step to line 13
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(3);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(3); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(4);  // fires
    debugListener.assertCurrThreadSuspendedCount(4);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

//    _log.log("fourth step in " + this);

    // Step to line 14
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(4);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(4); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(5);  // fires
    debugListener.assertCurrThreadSuspendedCount(5);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

//    _log.log("fifth step in " + this);

    // Step to line 15
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_OVER);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(5);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(5); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(6);  // fires
    debugListener.assertCurrThreadSuspendedCount(6);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

    watches = _debugger.getWatches();
    assertEquals("watch name incorrect", "foo", watches.get(0).getName());
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch name incorrect", "innerInnerFoo", watches.get(2).getName());
    assertEquals("watch name incorrect", "innerMethodFoo", watches.get(3).getName());
    assertEquals("watch name incorrect", "asdf", watches.get(4).getName());
    assertEquals("watch name incorrect", "nullString", watches.get(5).getName());
    assertEquals("watch value incorrect", "7", watches.get(0).getValue());
    assertEquals("watch value incorrect", "9", watches.get(1).getValue());
    assertEquals("watch value incorrect", "11", watches.get(2).getValue());
    assertEquals("watch value incorrect", "13", watches.get(3).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(4).getValue());
    assertEquals("watch value incorrect", "null", watches.get(5).getValue());
    assertEquals("watch type incorrect", "java.lang.String", watches.get(5).getType());

//      _log.log("sixth step in " + this);

    // Step into static method
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // suspended, updated
      _asyncStep(Debugger.StepType.STEP_INTO);
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(6);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(6); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(7);  // fires
    debugListener.assertCurrThreadSuspendedCount(7);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

    // Test watches in a static context.
    watches = _debugger.getWatches();
    assertEquals("watch name incorrect", "foo", watches.get(0).getName());
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch name incorrect", "innerInnerFoo", watches.get(2).getName());
    assertEquals("watch name incorrect", "innerMethodFoo", watches.get(3).getName());
    assertEquals("watch name incorrect", "asdf", watches.get(4).getName());
    assertEquals("watch name incorrect", "nullString", watches.get(5).getName());
    assertEquals("watch value incorrect", "7", watches.get(0).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(1).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(2).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(3).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(4).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(5).getValue());
    assertEquals("watch type incorrect", DebugWatchData.NO_TYPE, watches.get(5).getType());

    // Resumes one thread, finishing it and switching to the next break point
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // breakpointReached, suspended, updated
      _asyncResume();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(6);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(7); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(8);  // fires
    debugListener.assertCurrThreadSuspendedCount(8);  // fires
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertCurrThreadDiedCount(0);

    // Test watching a final local variable of an outer class
    watches = _debugger.getWatches();
    assertEquals("watch name incorrect", "localVar", watches.get(6).getName());
    assertEquals("watch value incorrect", "11", watches.get(6).getValue());

    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionRemovedCount(2);  //fires (no waiting)

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }

  /** Tests that watches can correctly see the values of local
   * variables, fields and fields of outer classes.
   */
  public void XXXtestStaticWatches() throws Exception {
    debug.logStart();
    _log.log("----testStaticWatches----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("MonkeyStaticStuff.java", MONKEY_STATIC_STUFF);

    // Set a breakpoint
    int index = MONKEY_STATIC_STUFF.indexOf("System.out.println(MonkeyInner.MonkeyTwoDeep.twoDeepFoo);");
    _debugger.toggleBreakpoint(doc, index, true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);

    // Run an inner method, hitting breakpoint
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      interpretIgnoreResult("MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.MonkeyThreeDeep.threeDeepMethod();");
      while (_pendingNotifies > 0) _notifierLock.wait();
    }

    _debugger.addWatch("foo");
    _debugger.addWatch("innerFoo");
    _debugger.addWatch("twoDeepFoo");
    _debugger.addWatch("threeDeepFoo");
    _debugger.addWatch("asdf");
    
    Utilities.clearEventQueue();
    debugListener.assertWatchSetCount(5);
    
    ArrayList<DebugWatchData> watches = _debugger.getWatches();
    assertEquals("watch name incorrect", "foo", watches.get(0).getName());
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch name incorrect", "twoDeepFoo", watches.get(2).getName());
    assertEquals("watch name incorrect", "threeDeepFoo", watches.get(3).getName());
    assertEquals("watch name incorrect", "asdf", watches.get(4).getName());
    assertEquals("watch value incorrect", "6", watches.get(0).getValue());
    assertEquals("watch value incorrect", "8", watches.get(1).getValue());
    assertEquals("watch value incorrect", "13", watches.get(2).getValue());
    assertEquals("watch value incorrect", "18", watches.get(3).getValue());
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(4).getValue());

    interpret("innerFoo = 0");
    watches = _debugger.getWatches();
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch value incorrect", "0", watches.get(1).getValue());

    interpret("innerFoo = 8");
    assertEquals("watch name incorrect", "innerFoo", watches.get(1).getName());
    assertEquals("watch value incorrect", "8", watches.get(1).getValue());

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }

  /** Tests that watches can correctly see the values of final local variables and method parameters from enclosing
    * classes.   Note:  Some final local variables are inlined by the compiler (even in debug mode), so they are 
    * unavailable to the debugger.
    */
  public void XXXtestWatchLocalVarsFromInnerClass() throws Exception {
    debug.logStart();
    _log.log("----testWatchLocalVarsFromInnerClass----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("InnerClassWithLocalVariables.java", INNER_CLASS_WITH_LOCAL_VARS);

    // Set a breakpoint
    int index = INNER_CLASS_WITH_LOCAL_VARS.indexOf("numArgs:");
    _debugger.toggleBreakpoint(doc, index, true);
    
    Utilities.clearEventQueue();
    debugListener.assertRegionAddedCount(1);

    // Run the main method, hitting breakpoint
    synchronized(_notifierLock) {
      _setPendingNotifies(3);  // suspended, updated, breakpointReached
      interpretIgnoreResult("java InnerClassWithLocalVariables arg");
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    _debugger.addWatch("numArgs");
    _debugger.addWatch("args");
    _debugger.addWatch("inlined");
    Utilities.clearEventQueue();
    debugListener.assertWatchSetCount(3);

    // Check watch values
    ArrayList<DebugWatchData> watches = _debugger.getWatches();
    assertEquals("numArgs watch value incorrect", "1", watches.get(0).getValue());
    String argsWatch = watches.get(1).getValue();
    assertTrue("args watch value incorrect", argsWatch.indexOf("java.lang.String") != -1);

    // unfortunately, inlined variable can't be seen
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(2).getValue());

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }

  /** Tests that watches can correctly see the values of final local variables and method parameters from enclosing
    * classes.  Note:  Some final local variables are inlined by the compiler (even in debug mode), so they are 
    * unavailable to the debugger.
    */
  public void XXXtestThreadShouldDie() throws Exception {
    debug.logStart();
    _log.log("----testThreadShouldDie----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    _startupDebugger("DrJavaThreadDeathTest.java", THREAD_DEATH_CLASS);

    // Before bugs 697825 and 779111 were fixed, this line would just
    //  hang, since dead threads remained suspended indefinitely.
    interpret("Jones.threadShouldDie()");

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
    debug.logEnd();
  }
}
