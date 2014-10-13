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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

import java.io.*;

/** This class contains the basic fields and methods that are necessary for any test file that needs to use the
  * JPDADebugger.
  * @version $Id: DebugTestCase.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class DebugTestCase extends GlobalModelTestCase {
  
//  protected final boolean printEvents = true;
//  protected final boolean printMessages = true;
//  protected PrintStream printStream = System.err; 
  
  // _log inherited from GlabalModelTestCase
  
  protected volatile int _pendingNotifies = 0;
  protected final Object _notifierLock = new Object();
  
  protected volatile Debugger _debugger;
  
  protected static final String DEBUG_CLASS =
    /*  1 */ "class DrJavaDebugClass {\n" +
    /*  2 */ "  public void foo() {\n" +
    /*  3 */ "    System.out.println(\"Foo Line 1\");\n" +
    /*  4 */ "    bar();\n" +
    /*  5 */ "    System.out.println(\"Foo Line 3\");\n" +
    /*  6 */ "  }\n" +
    /*  7 */ "  public void bar() {\n" +
    /*  8 */ "    System.out.println(\"Bar Line 1\");\n" +
    /*  9 */ "    System.out.println(\"Bar Line 2\");\n" +
    /* 10 */ "  }\n" +
    /* 11 */ "}\n" +
    /* 12 */ "class DrJavaDebugClass2 {\n" +
    /* 13 */ "  public void baz() {\n" +
    /* 14 */ "    System.out.println(\"Baz Line 1\");\n" +
    /* 15 */ "    new DrJavaDebugClass().bar();\n" +
    /* 16 */ "  }\n" +
    /* 17 */ "}";
  
  protected static final String DEBUG_CLASS_WITH_PACKAGE =
    /*  1 */ "package a;\n" +
    /*  2 */ "public class DrJavaDebugClassWithPackage {\n" +
    /*  3 */ "  public void foo() {\n" +
    /*  4 */ "    System.out.println(\"foo line 1\");\n" +
    /*  5 */ "    System.out.println(\"foo line 2\");\n" +
    /*  6 */ "  }\n" +
    /*  7 */ "}";
  
  protected static final String SUSPEND_CLASS =
    "class Suspender {\n" +
    "  public static void main(String[] args) {\n" +
    "    Thread t1 = new Thread(){\n" +
    "      public void run(){\n" +
    "        int a = 1;\n" +
    "        while(true);\n" +
    "      }\n" +
    "    };\n" +
    "    t1.start();\n" +
    "  }\n" +
    "}";
  
  protected static final String MONKEY_CLASS =
    /* 1 */    "class Monkey {\n" +
    /* 2 */    "  public static void main(String[] args) {\n" +
    /* 3 */    "\n" +
    /* 4 */    "    Thread t = new Thread(){\n" +
    /* 5 */    "      public void run(){\n" +
    /* 6 */    "       try{\n" +
    /* 7 */    "         Thread.sleep(1000);\n" +
    /* 8 */    "       }\n" +
    /* 9 */    "       catch(InterruptedException e){\n" +
    /* 10 */    "      }\n" +
    /* 11 */    "      System.out.println(\"I\'m a thread! Yeah!\");\n" +
    /* 12 */    "      }\n" +
    /* 13 */    "    };\n" +
    /* 14 */    "    try{\n" +
    /* 15 */    "      t.start();\n" +
    /* 16 */    "      System.out.println(\"I just woke up.  I\'m a big boy now.\");\n" +
    /* 17 */    "      System.out.println(\"James likes bananas!\");\n" +
    /* 18 */    "      System.out.println(\"Yes they do.\");\n" +
    /* 19 */    "    }catch(Exception e){\n" +
    /* 20 */    "      e.printStackTrace();\n" +
    /* 21 */    "    }\n" +
    /* 22 */    "  }\n" +
    /* 23 */    "}\n";
  
  protected static final String MONKEY_WITH_INNER_CLASS =
    /* 1 */    "class Monkey {\n" +
    /* 2 */    "  static int foo = 6; \n" +
    /* 3 */    "  class MonkeyInner { \n" +
    /* 4 */    "    int innerFoo = 8;\n" +
    /* 5 */    "    class MonkeyInnerInner { \n" +
    /* 6 */    "      int innerInnerFoo = 10;\n" +
    /* 7 */    "      public void innerMethod() { \n" +
    /* 8 */    "        int innerMethodFoo;\n" +
    /* 9 */    "        String nullString = null;\n" +
    /* 10 */   "        innerMethodFoo = 12;\n" +
    /* 11 */   "        foo++;\n" +
    /* 12 */   "        innerFoo++;\n" +
    /* 13 */   "        innerInnerFoo++;\n" +
    /* 14 */   "        innerMethodFoo++;\n" +
    /* 15 */   "        staticMethod();\n" +
    /* 16 */   "        System.out.println(\"innerMethodFoo: \" + innerMethodFoo);\n" +
    /* 17 */   "      }\n" +
    /* 18 */   "    }\n" +
    /* 19 */   "  }\n" +
    /* 20 */   "  public void bar() {\n" +
    /* 21 */   "    final MonkeyInner.MonkeyInnerInner mi = \n" +
    /* 22 */   "      new MonkeyInner().new MonkeyInnerInner();\n" +
    /* 23 */   "    mi.innerMethod();\n" +
    /* 24 */   "    final int localVar = 99;\n" +
    /* 25 */   "    new Thread() {\n" +
    /* 26 */   "      public void run() {\n" +
    /* 27 */   "        final int localVar = mi.innerInnerFoo;\n" +
    /* 28 */   "        new Thread() {\n" +
    /* 29 */   "          public void run() {\n" +
    /* 30 */   "            new Thread() {\n" +
    /* 31 */   "              public void run() {\n" +
    /* 32 */   "                System.out.println(\"localVar = \" + localVar);\n" +
    /* 33 */   "              }\n" +
    /* 34 */   "            }.run();\n" +
    /* 35 */   "          }\n" +
    /* 36 */   "        }.run();\n" +
    /* 37 */   "      }\n" +
    /* 38 */   "    }.run();\n" +
    /* 39 */   "  }\n" +
    /* 40 */   "  public static void staticMethod() {\n" +
    /* 41 */   "    int z = 3;\n" +
    /* 42 */   "  }\n" +
    /* 43 */   "}\n";
  
  protected static final String INNER_CLASS_WITH_LOCAL_VARS =
    /*  1 */ "class InnerClassWithLocalVariables {\n" +
    /*  2 */ "  public static void main(final String[] args) {\n" +
    /*  3 */ "    final int numArgs = args.length;\n" +
    /*  4 */ "    final int inlined = 0;\n" +
    /*  5 */ "    new Runnable() {\n" +
    /*  6 */ "      public void run() {\n" +
    /*  7 */ "        System.out.println(\"numArgs: \" + numArgs);\n" +
    /*  8 */ "        System.out.println(\"inlined: \" + inlined);\n" +
    /*  9 */ "        System.out.println(\"args.length: \" + args.length);\n" +
    /* 10 */ "      }\n" +
    /* 11 */ "    }.run();\n" +
    /* 12 */ "  }\n" +
    /* 13 */ "}\n";
  
  protected static final String CLASS_WITH_STATIC_FIELD =
    /*  1 */    "public class DrJavaDebugStaticField {\n" +
    /*  2 */    "  public static int x = 0;\n" +
    /*  3 */    "  public void bar() {\n" +
    /*  4 */    "    System.out.println(\"x == \" + x);\n" +
    /*  5 */    "    x++;\n" +
    /*  6 */    "  }\n" +
    /*  7 */    "  public static void main(String[] nu) {\n" +
    /*  8 */    "    new Thread(\"stuff\") {\n" +
    /*  9 */    "      public void run() {\n" +
    /* 10 */    "        new DrJavaDebugStaticField().bar();\n" +
    /* 11 */    "      }\n" +
    /* 12 */    "    }.start();\n" +
    /* 13 */    "    new DrJavaDebugStaticField().bar();\n" +
    /* 14 */    "  }\n" +
    /* 15 */    "}";
  
  protected static final String MONKEY_STATIC_STUFF =
    /*1*/ "class MonkeyStaticStuff {\n" +
    /*2*/ "  static int foo = 6;\n" +
    /*3*/ "  static class MonkeyInner {\n" +
    /*4*/ "    static int innerFoo = 8;\n" +
    /*5*/ "    static public class MonkeyTwoDeep {\n" +
    /*6*/ "      static int twoDeepFoo = 13;\n" +
    /*7*/ "      static class MonkeyThreeDeep {\n" +
    /*8*/ "        public static int threeDeepFoo = 18;\n" +
    /*9*/ "        public static void threeDeepMethod() {\n" +
    /*10*/"          System.out.println(MonkeyStaticStuff.MonkeyInner.MonkeyTwoDeep.MonkeyThreeDeep.threeDeepFoo);\n" +
    /*11*/"          System.out.println(MonkeyTwoDeep.twoDeepFoo);\n" +
    /*12*/"          System.out.println(MonkeyStaticStuff.foo);\n" +
    /*13*/"          System.out.println(MonkeyStaticStuff.MonkeyInner.innerFoo);\n" +
    /*14*/"          System.out.println(MonkeyInner.MonkeyTwoDeep.twoDeepFoo);\n" +
    /*15*/"          System.out.println(innerFoo);\n" +
    /*16*/"        }\n" +
    /*17*/"      }\n" +
    /*18*/"      static int getNegativeTwo() { return -2; }\n" +
    /*19*/"    }\n" +
    /*20*/"  }\n" +
    /*21*/"}";
  
  protected static final String THREAD_DEATH_CLASS =
    /*  1 */ "class Jones {\n" +
    /*  2 */ "  public static void threadShouldDie() {\n" +
    /*  3 */ "    Thread cooper = new Thread() {\n" +
    /*  4 */ "      public void run() {\n" +
    /*  5 */ "        System.out.println(\"This thread should die.\");\n" +
    /*  6 */ "      }\n" +
    /*  7 */ "    };\n" +
    /*  8 */ "    cooper.start();\n" +
    /*  9 */ "    while(cooper.isAlive()) {}\n" +
    /* 10 */ "    System.out.println(\"Thread died.\");\n" +
    /* 11 */ "  }\n" +
    /* 12 */ "}";
  
  /** Sets up the debugger for each test. */
  public void setUp() throws Exception {
    super.setUp();
    _log.log("Setting up (DebugTestCase)" + this);
    _debugger = _model.getDebugger();
    assertNotNull("Debug Manager should not be null", _debugger);
  }
  
  /** Cleans up the debugger after each test. */
  public void tearDown() throws Exception {
    _log.log("Tearing down (DebugTestCase)" + this);
    _debugger = null;
    super.tearDown();
  }
  
  /** Ensures that the given object will wait for n notifications. Callers must call o.wait() AFTER this is 
    * called.  Use _notifyLock instead of o.notify() when using this method. Only one object (o) can use this 
    * synchronization protocol at a time, since it uses a field to store the number of pending notifications.
    * @param n The number of times to be "notified" through _notifyLock
    */
  protected void _setPendingNotifies(int n) throws InterruptedException {
    synchronized(_notifierLock) {
      _log.log("Waiting for " + n + " notifications ...");
      _pendingNotifies = n;
    }
  }
  
  /** Notifies _notifierLock if the after the notify count has expired. See _setPendingNotifies. */
  protected void _notifyLock() {
    synchronized(_notifierLock) {
      _pendingNotifies--;
      _log.log("notified; count = " + _pendingNotifies);     
      if (_pendingNotifies == 0) {
        _log.log("Notify count reached 0 -- notifying!");
        _notifierLock.notifyAll();  // can accommodate multiple threads waiting on this event (NOT USED?)
      }
      if (_pendingNotifies < 0) fail("Notified too many times");
    }
  }
  
  /** Cleanly starts the debugger with a newly compiled file saved in a temporary directory.  Assumes that the file will
    * compile successfully.
    * @param fileName Name of the file to save in a temp directory
    * @param classText String containing the code for the class to compile
    * @return OpenDefinitionsDocument containing the compiled source file
    */
  protected OpenDefinitionsDocument _startupDebugger(String fileName, String classText) throws Exception {
    // Create a file in the temporary directory
    File file = IOUtil.attemptCanonicalFile(new File(_tempDir, fileName));
    return _startupDebugger(file, classText);
  }
  
  /** Cleanly starts the debugger with a newly compiled file saved in a temporary directory.  Assumes that the 
    * file will compile successfully.
    * @param file File to save the class in
    * @param classText String containing the code for the class to compile
    * @return OpenDefinitionsDocument containing the compiled source file
    */
  protected OpenDefinitionsDocument _startupDebugger(File file, String classText) throws Exception {
    // Compile the file
    _log.log("Compiling " + file);
    OpenDefinitionsDocument doc = doCompile(classText, file);
    _log.log("Staring debugger in " + this);
    // Start debugger
    synchronized(_notifierLock) {
      _setPendingNotifies(1);  // startUp
      _debugger.startUp();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    _log.log("Finished starting debugger in " + this);
    return doc;
  }
  
  /** Cleanly shuts down the debugger, without having to wait for a suspended interaction to complete. */
  protected void _shutdownWithoutSuspendedInteraction() throws Exception {
    _log.log("Shutting down debugger in " + this + " without waiting");
    _model.getBreakpointManager().clearRegions();
    
    // Shutdown the debugger
    _log.log("Shutting down...");
    synchronized(_notifierLock) {
      _setPendingNotifies(1);  // shutdown
      _debugger.shutdown();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    _log.log("Shut down.");
    _log.log("Completed debugger shutdown for " + this);
  }
  
  /** Cleanly shuts down the debugger, waiting for a suspended interaction to complete. */
  protected void _shutdownAndWaitForInteractionEnded() throws Exception {
    _log.log("Shutting down debugger in " + this + " with waiting");
    _model.getBreakpointManager().clearRegions();
    
    // Shutdown the debugger
    _log.log("Shutting down...");
    InterpretListener interpretListener = new InterpretListener() {
      public void interpreterChanged(boolean inProgress) {
        // Don't notify: happens in the same thread
        interpreterChangedCount++;
      }
    };
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      _setPendingNotifies(2);  // interactionEnded, shutdown
      _debugger.shutdown();
      while (_pendingNotifies > 0) _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    interpretListener.assertInterpreterChangedCount(1);  // fires (don't wait)
    _model.removeListener(interpretListener);
    
    _log.log("Shut down.");
    _log.log("Completed debugger shutdown for " + this);
  }
  
  /** Sets the current debugger thread to the specified thread t.*/
  protected void _doSetCurrentThread(final DebugThreadData t) throws DebugException {
    Utilities.invokeLater(new Runnable() { 
      public void run() { 
        try {_debugger.setCurrentThread(t); }
        catch(DebugException e) { throw new UnexpectedException(e); }
      } 
    });
  }
  
  /** Resumes the debugger asynchronously so as to avoid getting notified before we start waiting for notifies. */
  protected void _asyncStep(final Debugger.StepType type) {
    new Thread("asyncStep Thread") {
      public void run() {
        try { _debugger.step(type); }
        catch(DebugException dbe) {
          dbe.printStackTrace();
          listenerFail("Debugger couldn't be resumed!\n" + dbe);
        }
      }
    }.start();
  }
  
  /** Resumes the debugger asynchronously so as to aovid
   * getting notified before we start waiting for notifies
   */
  protected void _asyncResume() {
    new Thread("asyncResume Thread") {
      public void run() {
        try { _debugger.resume(); }
        catch(DebugException dbe) {
          dbe.printStackTrace();
          listenerFail("Debugger couldn't be resumed!\n" + dbe);
        }
      }
    }.start();
  }
  
  /** Sets the current thread in a new thread to avoid being notified of events before we start waiting for them. */
  protected void _asyncDoSetCurrentThread(final DebugThreadData th) {
    new Thread("asyncDoSetCurrentThread Thread") {
      public void run() {
        try { _doSetCurrentThread(th); }
        catch (DebugException dbe) {
          dbe.printStackTrace();
          listenerFail("Couldn't set current thread in _asyncDoSetCurrentThread\n" + dbe);
        }
      }
    }.start();
  }
  
  /** Listens to events from the debugger to ensure that they happen at the correct times. */
  protected class DebugTestListener implements DebugListener {
    protected volatile int debuggerStartedCount = 0;
    protected volatile int debuggerShutdownCount = 0;
    protected volatile int threadLocationUpdatedCount = 0;
    protected volatile int breakpointReachedCount = 0;
    protected volatile int regionAddedCount = 0;
    protected volatile int regionChangedCount = 0;
    protected volatile int regionRemovedCount = 0;
    protected volatile int watchSetCount = 0;
    protected volatile int watchRemovedCount = 0;
    protected volatile int stepRequestedCount = 0;
    protected volatile int currThreadSuspendedCount = 0;
    protected volatile int currThreadResumedCount = 0;
    protected volatile int threadStartedCount = 0;
    protected volatile int currThreadDiedCount = 0;
    protected volatile int currThreadSetCount = 0;
    protected volatile int nonCurrThreadDiedCount = 0;
    
    public DebugTestListener() { }
    
    public void assertDebuggerStartedCount(int i) {
      assertEquals("number of times debuggerStarted fired", i, debuggerStartedCount);
    }
    
    public void assertDebuggerShutdownCount(int i) {
      assertEquals("number of times debuggerShutdown fired", i, debuggerShutdownCount);
    }
    
    public void assertThreadLocationUpdatedCount(int i) {
      assertEquals("number of times threadLocationUpdated fired", i, threadLocationUpdatedCount);
    }
    
    public void assertBreakpointReachedCount(int i) {
      assertEquals("number of times breakpointReached fired", i, breakpointReachedCount);
    }
    
    public void assertRegionAddedCount(int i) {
      assertEquals("number of times regionAdded fired", i, regionAddedCount);
    }
    
    public void assertRegionChangedCount(int i) {
      assertEquals("number of times regionChanged fired", i, regionChangedCount);
    }
    
    public void assertRegionRemovedCount(int i) {
      assertEquals("number of times regionRemoved fired", i, regionRemovedCount);
    }
    
    public void assertWatchSetCount(int i) {
      assertEquals("number of times watchSet fired", i, watchSetCount);
    }
    
    public void assertWatchRemovedCount(int i) {
      assertEquals("number of times watchRemoved fired", i, watchRemovedCount);
    }
    
    public void assertStepRequestedCount(int i) {
      assertEquals("number of times stepRequested fired", i, stepRequestedCount);
    }
    
    public void assertStepFinishedCount(int i) {
      assertEquals("number of times stepRequested fired", i, stepRequestedCount);
    }
    
    public void assertCurrThreadSuspendedCount(int i) {
      assertEquals("number of times currThreadSuspended fired", i, currThreadSuspendedCount);
    }
    
    public void assertCurrThreadResumedCount(int i) {
      assertEquals("number of times currThreadResumed fired", i, currThreadResumedCount);
    }
    
    public void assertCurrThreadSetCount(int i) {
      assertEquals("number of times currThreadSet fired", i, currThreadSetCount);
    }
    
    public void assertThreadStartedCount(int i) {
      assertEquals("number of times threadStarted fired", i,threadStartedCount);
    }
    
    public void assertCurrThreadDiedCount(int i) {
      assertEquals("number of times currThreadDied fired", i, currThreadDiedCount);
    }
    
    public void assertNonCurrThreadDiedCount(int i) {
      assertEquals("number of times nonCurrThreadDied fired", i, nonCurrThreadDiedCount);
    }
    
    
    public void debuggerStarted() { fail("debuggerStarted fired unexpectedly"); }
    
    public void debuggerShutdown() { fail("debuggerShutdown fired unexpectedly"); }
    
    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) {
      fail("threadLocationUpdated fired unexpectedly");
    }
    
    public void breakpointReached(Breakpoint bp) { fail("breakpointReached fired unexpectedly"); }
    
    public void regionAdded(Breakpoint bp) { fail("regionAdded fired unexpectedly"); }
    
    public void regionChanged(Breakpoint bp) { fail("regionChanged fired unexpectedly"); }
    
    public void regionRemoved(Breakpoint bp) { fail("regionRemoved fired unexpectedly"); }
    
    public void watchSet(DebugWatchData w) { fail("watchSet fired unexpectedly"); }
    
    public void watchRemoved(DebugWatchData w) { fail("watchRemoved fired unexpectedly"); }
    
    public void stepRequested() { fail("stepRequested fired unexpectedly"); }
    
    public void currThreadSuspended() { fail("currThreadSuspended fired unexpectedly"); }
    
    public void currThreadResumed() { fail("currThreadResumed fired unexpectedly"); }
    
    public void currThreadSet(DebugThreadData dtd) { fail("currThreadSet fired unexpectedly"); }
    
    /** This won't fail because threads could be starting at any time. We have to expect this to be fired. */
    public void threadStarted() { threadStartedCount++; }
    
    public void currThreadDied() { fail("currThreadDied fired unexpectedly"); }
    
    /** This won't fail because threads could be dying at any time. We have to expect this to be fired. */
    public void nonCurrThreadDied() { nonCurrThreadDiedCount++; }
  }
  
  /** DebugTestListener for all tests starting the debugger. */
  protected class DebugStartAndStopListener extends DebugTestListener {
    
    public DebugStartAndStopListener() { }
    
    public void debuggerStarted() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        debuggerStartedCount++;
        _log.log("debuggerStarted " + debuggerStartedCount);
        _notifyLock();
      }
    }
    public void debuggerShutdown() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        debuggerShutdownCount++;
        _log.log("debuggerShutdown " + debuggerShutdownCount);
        _notifyLock();
      }
    }
  }
  
  /** DebugTestListener for all tests setting breakpoints. */
  protected class BreakpointTestListener extends DebugStartAndStopListener {
    
    public BreakpointTestListener() { }
    
    public void breakpointReached(Breakpoint bp) {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        breakpointReachedCount++;
        _log.log("breakpointReached " + breakpointReachedCount);
        _notifyLock();
      }
    }
    public void regionAdded(Breakpoint bp) {
      // Manager's thread: test shouldn't wait
      regionAddedCount++;
    }
    public void regionRemoved(Breakpoint bp) {
      // Manager's thread: test shouldn't wait
      regionRemovedCount++;
      _log.log("regionRemoved " + regionRemovedCount);
    }
    
    public void currThreadSuspended() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        currThreadSuspendedCount++;
        _log.log("threadSuspended " + currThreadSuspendedCount);
        _notifyLock();
      }
    }
    public void currThreadResumed() {
      // Manager's thread: test shouldn't wait
      currThreadResumedCount++;
      _log.log("threadResumed " + currThreadResumedCount);
    }
    public void currThreadSet(DebugThreadData dtd) {
      // Manager's thread: test shouldn't wait
      currThreadSetCount++;
      _log.log("threadSet " + currThreadSetCount);
    }
    public void currThreadDied() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        currThreadDiedCount++;
        _log.log("currThreadDied " + currThreadDiedCount);
        _notifyLock();
      }
    }
    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        threadLocationUpdatedCount++;
        _log.log("threadUpdated " + threadLocationUpdatedCount);
        _notifyLock();
      }
    }
    public void watchSet(DebugWatchData w) {
      // Manager's thread: test shouldn't wait
      watchSetCount++;
      _log.log("watchSet " + watchSetCount);
    }
    public void watchRemoved(DebugWatchData w) {
      // Manager's thread: test shouldn't wait
      watchRemovedCount++;
      _log.log("watchRemoved " + watchRemovedCount);
    }
  }
  
  /** DebugTestListener for all tests using the stepper. */
  protected class StepTestListener extends BreakpointTestListener {
    
    public StepTestListener() { }
    
    public void stepRequested() {
      // Manager's thread: test shouldn't wait
      stepRequestedCount++;
      _log.log("stepRequested " + stepRequestedCount);
    }
  }
  
  /** TestListener that listens for an interpretation to end, and then notifies anyone waiting on it.  
    * (Necessary to prevent tests from overlapping.) */
  protected class InterpretListener extends TestListener {
    
    public InterpretListener() { }
    
    public void interactionStarted() {
      synchronized(_notifierLock) {
        interactionStartCount++;
        _log.log("interactionStarted " + interactionStartCount);
        _notifyLock();
      }
    }
    public void interactionEnded() {
      synchronized(_notifierLock) {
        interactionEndCount++;
        _log.log("interactionEnded " + interactionEndCount);
        _notifyLock();
      }
    }
    
    public void interpreterChanged(boolean inProgress) {
      synchronized(_notifierLock) {
        interpreterChangedCount++;
        _log.log("interpreterChanged " + interpreterChangedCount);
        _notifyLock();
      }
    }
  }
}