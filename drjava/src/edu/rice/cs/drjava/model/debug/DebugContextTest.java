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
import javax.swing.text.BadLocationException;
import junit.extensions.*;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import java.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.CodeStatus;

/**
 * More tests over the JPDA debugger.
 * @version $Id$
 */
public final class DebugContextTest extends DebugTestCase {
  /**
   * Tests that the sourcepath config option properly adds files to the
   * search directories.
   */
  public void testDebugSourcepath() throws Exception {
    if (printMessages) {
      System.out.println("----testDebugSourcePath----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);
    Vector<File> path = new Vector<File>();
    path.addElement(_tempDir);  // directory where doc's file is saved

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    // Source is highlighted because document is stored in breakpoint
    debugListener.assertThreadLocationUpdatedCount(1);  // fires

    // Step into bar() method
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_INTO);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    // Source is highlighted because file is in source root set
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // fires


    // Close file so it won't be in source root set
    _model.closeFile(doc);
    debugListener.assertBreakpointRemovedCount(1);

    // Step to next line
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(1);  // suspended
      _notifierLock.wait();
    }
    // Source is not highlighted
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // doesn't fire

    synchronized(_debugger) {
      // Add _tempDir to our sourcepath
      DrJava.getConfig().setSetting(OptionConstants.DEBUG_SOURCEPATH, path);
    }

    // Step to next line
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    // Source is highlighted because file is now on sourcepath
    debugListener.assertStepRequestedCount(3);  // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that breakpoints behave correctly in non-public classes.
   */
  public synchronized void testBreakpointsAndStepsInNonPublicClasses()
    throws Exception
  {
    if (printMessages) System.out.println("----testBreakpointsAndStepsInNonPublicClasses----");
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("Baz Line 1"),14);
    debugListener.assertBreakpointSetCount(1);

    // Run the baz() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass2().baz()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }

    if (printMessages) System.out.println("----After breakpoint:\n" + getInteractionsText());

    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("Baz Line 1");

    if (printMessages) System.out.println("adding another breakpoint");

    // Set another breakpoint (after is class loaded)
    _debugger.toggleBreakpoint(doc,
       DEBUG_CLASS.indexOf("System.out.println(\"Bar Line 2\")"), 9);
    debugListener.assertBreakpointSetCount(2);

    // Step to next line
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }

    if (printMessages) System.out.println("****"+getInteractionsText());
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
      if (printMessages) System.out.println("resuming");
      _asyncResume();
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    if (printMessages) System.out.println("----After one resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(3);  //fires
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Bar Line 1");
    assertInteractionsDoesNotContain("Bar Line 2");

    if( printMessages ) System.err.println("-------- Adding interpret listener --------");
    // Resume until finished, waiting for call to interpret to end
    InterpretListener interpretListener = new InterpretListener();
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      if( printMessages ) System.err.println("-------- resuming --------");
      _asyncResume();
      _waitForNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
                            // here, we get a currThreadDied since it's the last thread
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    if (printMessages) System.out.println("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);
    assertInteractionsContains("Bar Line 2");

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that stepping into a breakpoint works.
   */
  public synchronized void testStepIntoOverBreakpoint() throws Exception {
    if (printMessages) {
      System.out.println("----testStepIntoOverBreakpoint----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugClass.java",
                                                   DEBUG_CLASS);

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("Foo Line 1"), 3);
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();\n"), 4);
    debugListener.assertBreakpointSetCount(2);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
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
    assertInteractionsDoesNotContain("Foo Line 1");

    // Step over once
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }

    if (printMessages) {
      System.out.println("****"+getInteractionsText());
    }
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
      _asyncResume();
      _waitForNotifies(3);  // interactionEnded, interpreterChanged, currThreadDied
                            // here, we get a currThreadDied since it's the last thread
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    if (printMessages) {
      System.out.println("----After resume:\n" + getInteractionsText());
    }
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);


    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    debugListener.assertBreakpointRemovedCount(2);  //fires (no waiting)

    // Shutdown the debugger
    if (printMessages) {
      System.out.println("Shutting down...");
    }
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }

    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) {
      System.out.println("Shut down.");
    }
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that static fields are consistent across different interpreter contexts.
   */
  public void testStaticFieldsConsistent() throws Exception {
    if (printMessages) {
      System.out.println("----testStaticFieldsConsistent----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaDebugStaticField.java",
                                                   CLASS_WITH_STATIC_FIELD);

    // Set a breakpoint
    _debugger.toggleBreakpoint(doc,CLASS_WITH_STATIC_FIELD.indexOf("System.out.println"), 4);
    debugListener.assertBreakpointSetCount(1);

    // Run the main method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("java DrJavaDebugStaticField");
      _waitForNotifies(6);  // (suspended, updated, breakpointReached) *2
      _notifierLock.wait();
    }

    // TODO: Why is this call being made?
    DebugThreadData threadA = new DebugThreadData(_debugger.getCurrentThread());
    DebugThreadData threadB = new DebugThreadData(_debugger.getThreadAt(1));

     if (printMessages) {
      System.out.println("----After breakpoint:\n" + getInteractionsText());
    }

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
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    if (printMessages) {
      System.out.println("****"+getInteractionsText());
    }
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(4);  // fires
    debugListener.assertCurrThreadSuspendedCount(4);  // fires
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertCurrThreadDiedCount(0);
    assertEquals("x has correct value after increment", "6", interpret("DrJavaDebugStaticField.x"));
    assertEquals("this has correct value for x after increment", "6", interpret("this.x"));

    synchronized(_notifierLock){
      _asyncDoSetCurrentThread(threadB);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    interpret("");
    assertInteractionsContains("The current thread has changed.");
    assertEquals("x has correct value in other thread", "6", interpret("DrJavaDebugStaticField.x"));
    assertEquals("this has correct value for x in other thread", "6", interpret("this.x"));

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that watches can correctly see the values of local
   * variables, fields and fields of outer classes. Also tests
   * that we can watch objects initialized to null (bug #771040).
   * Also tests that we can watch final local variables of outer
   * classes (bug #769174).
   */
  public void testNonStaticWatches() throws Exception {
    if (printMessages) {
      System.out.println("----testNonStaticWatches----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("Monkey.java",
                                                   MONKEY_WITH_INNER_CLASS);

    // Set a breakpoint
    _debugger.toggleBreakpoint(doc,MONKEY_WITH_INNER_CLASS.indexOf("innerMethodFoo = 12;"), 10);
    _debugger.toggleBreakpoint(doc,MONKEY_WITH_INNER_CLASS.indexOf("System.out.println(\"localVar = \" + localVar);"), 32);
    debugListener.assertBreakpointSetCount(2);

    // Run an inner method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new Monkey().bar()");//new MonkeyInner().new MonkeyInnerInner().innerMethod()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    _debugger.addWatch("foo");
    _debugger.addWatch("innerFoo");
    _debugger.addWatch("innerInnerFoo");
    _debugger.addWatch("innerMethodFoo");
    _debugger.addWatch("asdf");
    _debugger.addWatch("nullString");
    _debugger.addWatch("localVar");

    if (printMessages) {
      System.out.println("first step");
    }
    // Step to line 11
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(1);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  // fires
    debugListener.assertCurrThreadSuspendedCount(2);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

    Vector<DebugWatchData> watches = _debugger.getWatches();
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

    if (printMessages) {
      System.out.println("second step");
    }
    // Step to line 12
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(2);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadSuspendedCount(3);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

    if (printMessages) {
      System.out.println("third step");
    }
    // Step to line 13
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(3);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(3); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(4);  // fires
    debugListener.assertCurrThreadSuspendedCount(4);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

    if (printMessages) {
      System.out.println("fourth step");
    }
    // Step to line 14
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    debugListener.assertStepRequestedCount(4);  // fires (don't wait)
    debugListener.assertCurrThreadResumedCount(4); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(5);  // fires
    debugListener.assertCurrThreadSuspendedCount(5);  // fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);

    if (printMessages) {
      System.out.println("fifth step");
    }
    // Step to line 15
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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

    if (printMessages) {
      System.out.println("sixth step");
    }
    // Step into static method
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_INTO);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _asyncResume();
      _waitForNotifies(3);  // breakpointReached, suspended, updated
      _notifierLock.wait();
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
    debugListener.assertBreakpointRemovedCount(2);  //fires (no waiting)

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that watches can correctly see the values of local
   * variables, fields and fields of outer classes.
   */
  public void testStaticWatches() throws Exception {
    if (printMessages) {
      System.out.println("----testStaticWatches----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("MonkeyStaticStuff.java",
                                                   MONKEY_STATIC_STUFF);

    // Set a breakpoint
    int index = MONKEY_STATIC_STUFF.indexOf("System.out.println(MonkeyInner.MonkeyTwoDeep.twoDeepFoo);");
    _debugger.toggleBreakpoint(doc,
                               index,
                               14);
    debugListener.assertBreakpointSetCount(1);

    // Run an inner method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.MonkeyThreeDeep.threeDeepMethod();");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    _debugger.addWatch("foo");
    _debugger.addWatch("innerFoo");
    _debugger.addWatch("twoDeepFoo");
    _debugger.addWatch("threeDeepFoo");
    _debugger.addWatch("asdf");

    Vector<DebugWatchData> watches = _debugger.getWatches();
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
  }

  /**
   * Tests that watches can correctly see the values of final local
   * variables and method parameters from enclosing classes.
   *
   * Note:  Some final local variables are inlined by the compiler
   * (even in debug mode), so they are unavailable to the debugger.
   */
  public void testWatchLocalVarsFromInnerClass() throws Exception {
    if (printMessages) {
      System.out.println("----testWatchLocalVarsFromInnerClass----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("InnerClassWithLocalVariables.java",
                                                   INNER_CLASS_WITH_LOCAL_VARS);

    // Set a breakpoint
    int index = INNER_CLASS_WITH_LOCAL_VARS.indexOf("numArgs:");
    _debugger.toggleBreakpoint(doc, index, 7);
    debugListener.assertBreakpointSetCount(1);

    // Run the main method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("java InnerClassWithLocalVariables arg");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    _debugger.addWatch("numArgs");
    _debugger.addWatch("args");
    _debugger.addWatch("inlined");

    // Check watch values
    Vector<DebugWatchData> watches = _debugger.getWatches();
    assertEquals("numArgs watch value incorrect",
                 "1", watches.get(0).getValue());
    String argsWatch = watches.get(1).getValue();
    assertTrue("args watch value incorrect",
               argsWatch.indexOf("java.lang.String") != -1);

    // unfortunately, inlined variable can't be seen
    assertEquals("watch value incorrect", DebugWatchData.NO_VALUE, watches.get(2).getValue());

    // Shut down
    _shutdownAndWaitForInteractionEnded();
    _debugger.removeListener(debugListener);
  }

  /**
   * Tests that watches can correctly see the values of final local
   * variables and method parameters from enclosing classes.
   *
   * Note:  Some final local variables are inlined by the compiler
   * (even in debug mode), so they are unavailable to the debugger.
   */
  public void testThreadShouldDie() throws Exception {
    if (printMessages) {
      System.out.println("----testThreadShouldDie----");
    }
    StepTestListener debugListener = new StepTestListener();
    _debugger.addListener(debugListener);

    // Start up
    OpenDefinitionsDocument doc = _startupDebugger("DrJavaThreadDeathTest.java",
                                                   THREAD_DEATH_CLASS);

    // Before bugs 697825 and 779111 were fixed, this line would just
    //  hang, since dead threads remained suspended indefinitely.
    interpret("Jones.threadShouldDie()");

    // Shut down
    _shutdownWithoutSuspendedInteraction();
    _debugger.removeListener(debugListener);
  }
}
