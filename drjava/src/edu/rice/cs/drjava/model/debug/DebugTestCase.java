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

package edu.rice.cs.drjava.model.debug;

import edu.rice.cs.drjava.model.*;

import java.io.*;

import junit.framework.*;

/**
 * This class contains the basic fields and methods that are necessary
 * for any test file that needs to use the JPDADebugger.
 * 
 * @version $Id$
 */
public abstract class DebugTestCase extends GlobalModelTestCase {

  protected final boolean printEvents = false;
  protected final boolean printMessages = false;
  
  protected int _pendingNotifies = 0;
  protected Object _notifierLock = new Object();

  protected JPDADebugger _debugger;
  
  /**
   * Constructor.
   * @param  String name
   */
  public DebugTestCase(String name) {
    super(name);
  }

  /**
   * Sets up the debugger for each test.
   */
  public void setUp() throws IOException {
    super.setUp();
    _debugger = (JPDADebugger) _model.getDebugger();
    assertNotNull("Debug Manager should not be null", _debugger);
  }

  /**
   * Cleans up the debugger after each test.
   */
  public void tearDown() throws IOException {
    _debugger = null;
    super.tearDown();
  }

  
  /**
   * Ensures that the given object will wait for n notifications.
   * Callers must call o.wait() AFTER this is called.  (We can't call it
   * here, because then the synchronized _notifyObject method can never
   * be entered.)  Use _notifyObject instead of o.notify() when using
   * this method.
   * Only one object (o) can use this at a time, since it uses a field
   * to store the number of pending notifications.
   *
   * @param n The number of times to be "notified" through _notifyObject
   */
  protected void _waitForNotifies(int n) throws InterruptedException {
    synchronized(_notifierLock) {
      if (printMessages) {
        System.out.println("waiting for " + n + " notifications...");
      }
      _pendingNotifies = n;
    }
  }

  /**
   * Notifies _notifierLock if the after the notify count has expired.
   * See _waitForNotifies
   */
  protected void _notifyLock() {
    synchronized(_notifierLock) {
      if (printMessages) {
        System.out.println("notified");
      }
      _pendingNotifies--;
      if (_pendingNotifies == 0) {
        if (printMessages) {
          System.out.println("Notify count reached 0 -- notifying!");
        }
        _notifierLock.notify();
      }
      if (_pendingNotifies < 0) {
        fail("Notified too many times");
      }
    }
  }
  
  
  /**
   * Cleanly starts the debugger with a newly compiled file saved in a
   * temporary directory.  Assumes that the file will compile successfully.
   * @param fileName Name of the file to save in a temp directory
   * @param classText String containing the code for the class to compile
   * @return OpenDefinitionsDocument containing the compiled source file
   */
  protected OpenDefinitionsDocument _startupDebugger(String fileName, String classText)
    throws Exception
  {
    // Create a file in the temporary directory
    File file = new File(_tempDir, fileName);
    return _startupDebugger(file, classText);
  }
  
  /**
   * Cleanly starts the debugger with a newly compiled file saved in a
   * temporary directory.  Assumes that the file will compile successfully.
   * @param file File to save the class in
   * @param classText String containing the code for the class to compile
   * @return OpenDefinitionsDocument containing the compiled source file
   */
  protected OpenDefinitionsDocument _startupDebugger(File file, String classText)
    throws Exception
  {
    // Compile the file
    OpenDefinitionsDocument doc = doCompile(classText, file);
    
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    return doc;
  }

  /**
   * Cleanly shuts down the debugger, without having to wait for
   * a suspended interaction to complete.
   */
  protected void _shutdownWithoutSuspendedInteraction() throws Exception {
    _debugger.removeAllBreakpoints();
      
    // Shutdown the debugger
    if (printMessages) {
      System.out.println("Shutting down...");
    }
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    if (printMessages) {
      System.out.println("Shut down.");
    }
  }
  
  /**
   * Cleanly shuts down the debugger, waiting for a suspended
   * interaction to complete.
   */
  protected void _shutdownAndWaitForInteractionEnded() throws Exception {
    _debugger.removeAllBreakpoints();
    
    // Shutdown the debugger
    if (printMessages) {
      System.out.println("Shutting down...");
    }
    InterpretListener interpretListener = new InterpretListener() {
       public void interpreterChanged(boolean inProgress) {
         // Don't notify: happens in the same thread
        interpreterChangedCount++;
       }
     };
    _model.addListener(interpretListener);
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(2);  // interactionEnded, shutdown
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    interpretListener.assertInterpreterChangedCount(1);  // fires (don't wait)
    _model.removeListener(interpretListener);
    
    if (printMessages) {
      System.out.println("Shut down.");
    }
  }

  /**
   * Sets the current debugger thread to th
   */
  protected void _doSetCurrentThread(final DebugThreadData th) throws DebugException {
    _debugger.setCurrentThread(th);
  }

  /**
   * Resumes the debugger asynchronously so as to aovid
   * getting notified before we start waiting for notifies
   */
  protected void _asyncStep(final int whatKind){
    new Thread("asyncStep Thread") {
      public void run(){
        try{
          _debugger.step(whatKind);
        }
        catch(DebugException dbe) {
          dbe.printStackTrace();
          listenerFail("Debugger couldn't be resumed!\n" + dbe);
        }
      }
    }.start();
  }
  
  /**
   * Resumes the debugger asynchronously so as to aovid
   * getting notified before we start waiting for notifies
   */
  protected void _asyncResume(){
    new Thread("asyncResume Thread") {
      public void run(){
        try{
          _debugger.resume();
        }
        catch(DebugException dbe) {
          dbe.printStackTrace();
          listenerFail("Debugger couldn't be resumed!\n" + dbe);
        }
      }
    }.start();
  }
  
  /**
   * Sets the current thread in a new thread to avoid
   * being notified of events before we start waiting for them
   */
  protected void _asyncDoSetCurrentThread(final DebugThreadData th) {
    new Thread("asyncDoSetCurrentThread Thread") {
      public void run(){
        try {
          _doSetCurrentThread(th);
        }
        catch (DebugException dbe) {
          dbe.printStackTrace();
          listenerFail("Couldn't set current thread in _asyncDoSetCurrentThread\n" +
                       dbe);
        }
      }
    }.start();
  }
    
    
  /**
   * Listens to events from the debugger to ensure that they happen at the
   * correct times.
   */
  protected class DebugTestListener implements DebugListener {
    protected int debuggerStartedCount = 0;
    protected int debuggerShutdownCount = 0;
    protected int threadLocationUpdatedCount = 0;
    protected int breakpointSetCount = 0;
    protected int breakpointReachedCount = 0;
    protected int breakpointRemovedCount = 0;
    protected int stepRequestedCount = 0;
    protected int currThreadSuspendedCount = 0;
    protected int currThreadResumedCount = 0;
    protected int threadStartedCount = 0;
    protected int currThreadDiedCount = 0;
    protected int currThreadSetCount = 0;
    protected int nonCurrThreadDiedCount = 0;
    
    public void assertDebuggerStartedCount(int i) {
      assertEquals("number of times debuggerStarted fired", i, debuggerStartedCount);
    }
    
    public void assertDebuggerShutdownCount(int i) {
      assertEquals("number of times debuggerShutdown fired", i, debuggerShutdownCount);
    }
    
    public void assertThreadLocationUpdatedCount(int i) {
      assertEquals("number of times threadLocationUpdated fired", i,
                   threadLocationUpdatedCount);
    }
    
    public void assertBreakpointSetCount(int i) {
      assertEquals("number of times breakpointSet fired", i, breakpointSetCount);
    }
    
    public void assertBreakpointReachedCount(int i) {
      assertEquals("number of times breakpointReached fired", i, breakpointReachedCount);
    }
    
    public void assertBreakpointRemovedCount(int i) {
      assertEquals("number of times breakpointRemoved fired", i, breakpointRemovedCount);
    }
    
    public void assertStepRequestedCount(int i) {
      assertEquals("number of times stepRequested fired", i,
                   stepRequestedCount);
    }
    
    public void assertStepFinishedCount(int i) {
      assertEquals("number of times stepRequested fired", i,
                   stepRequestedCount);
    }
    
    public void assertCurrThreadSuspendedCount(int i) {
      assertEquals("number of times currThreadSuspended fired", i,
                   currThreadSuspendedCount);
    }
    
    public void assertCurrThreadResumedCount(int i) {
      assertEquals("number of times currThreadResumed fired", i,
                   currThreadResumedCount);
    }

    public void assertCurrThreadSetCount(int i) {
      assertEquals("number of times currThreadSet fired", i,
                   currThreadSetCount);
    }
    
    public void assertThreadStartedCount(int i) {
      assertEquals("number of times threadStarted fired", i,
                   threadStartedCount);
    }
    
    public void assertCurrThreadDiedCount(int i) {
      assertEquals("number of times currThreadDied fired", i,
                   currThreadDiedCount);
    }
    
    public void assertNonCurrThreadDiedCount(int i) {
      assertEquals("number of times nonCurrThreadDied fired", i,
                   nonCurrThreadDiedCount);
    }


    public void debuggerStarted() {
      fail("debuggerStarted fired unexpectedly");
    }
    
    public void debuggerShutdown() {
      fail("debuggerShutdown fired unexpectedly");
    }

    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber, boolean shouldHighlight) {
      fail("threadLocationUpdated fired unexpectedly");
    }
  
    public void breakpointSet(Breakpoint bp) {
      fail("breakpointSet fired unexpectedly");
    }
    
    public void breakpointReached(Breakpoint bp) {
      fail("breakpointReached fired unexpectedly");
    }
    
    public void breakpointRemoved(Breakpoint bp) {
      fail("breakpointRemoved fired unexpectedly");
    }
    
    public void stepRequested() {
      fail("stepRequested fired unexpectedly");
    }
    
    public void currThreadSuspended() {
      fail("currThreadSuspended fired unexpectedly");
    }
    
    public void currThreadResumed() {
      fail("currThreadResumed fired unexpectedly");
    }
    
    public void currThreadSet(DebugThreadData dtd) {
      fail("currThreadSet fired unexpectedly");
    }
    
    /**
     * This won't fail because threads could be starting at any time.
     * We have to expect this to be fired.
     */
    public void threadStarted() {
      threadStartedCount++;
    }
    
    public void currThreadDied() {
      fail("currThreadDied fired unexpectedly");
    }
    
    /**
     * This won't fail because threads could be dying at any time.
     * We have to expect this to be fired.
     */
    public void nonCurrThreadDied() {
      nonCurrThreadDiedCount++;
    }
  }

  /**
   * DebugTestListener for all tests starting the debugger.
   */
  protected class DebugStartAndStopListener extends DebugTestListener {
    public void debuggerStarted() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        debuggerStartedCount++;
        if (printEvents) System.out.println("debuggerStarted " + debuggerStartedCount);
        _notifyLock();
      }
    }
    public void debuggerShutdown() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        debuggerShutdownCount++;
        if (printEvents) System.out.println("debuggerShutdown " + debuggerShutdownCount);
        _notifyLock();
      }
    }
  }

  /**
   * DebugTestListener for all tests setting breakpoints.
   */
  protected class BreakpointTestListener extends DebugStartAndStopListener {
    public BreakpointTestListener() {}
    public void breakpointSet(Breakpoint bp) {
      // Manager's thread: test shouldn't wait
      breakpointSetCount++;
    }
    public void breakpointReached(Breakpoint bp) {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        breakpointReachedCount++;
        if (printEvents) System.out.println("breakpointReached " + breakpointReachedCount);
        _notifyLock();
      }
    }
    public void breakpointRemoved(Breakpoint bp) {
      // Manager's thread: test shouldn't wait
      breakpointRemovedCount++;
      if (printEvents) System.out.println("breakpointRemoved " + breakpointRemovedCount);
    }
    
    public void currThreadSuspended() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        currThreadSuspendedCount++;
        if (printEvents) System.out.println("threadSuspended " + currThreadSuspendedCount);
        _notifyLock();
      }
    }
    public void currThreadResumed() {
      // Manager's thread: test shouldn't wait
      currThreadResumedCount++;
      if (printEvents) System.out.println("threadResumed " + currThreadResumedCount);
    }
    public void currThreadSet(DebugThreadData dtd) {
      // Manager's thread: test shouldn't wait
      currThreadSetCount++;
      if (printEvents) System.out.println("threadSet " + currThreadSetCount);
    }
    public void currThreadDied() {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        currThreadDiedCount++;
        if (printEvents) System.out.println("currThreadDied " + currThreadDiedCount);
        _notifyLock();
      }
    }
    public void threadLocationUpdated(OpenDefinitionsDocument doc,
                                      int lineNumber, boolean shouldHighlight) {
      // EventHandler's thread: test should wait
      synchronized(_notifierLock) {
        threadLocationUpdatedCount++;
        if (printEvents) System.out.println("threadUpdated " + threadLocationUpdatedCount);
        _notifyLock();
      }
    }
  }
  
  /**
   * DebugTestListener for all tests using the stepper.
   */
  protected class StepTestListener extends BreakpointTestListener {
    public void stepRequested() {
      // Manager's thread: test shouldn't wait
      stepRequestedCount++;
      if (printEvents) System.out.println("stepRequested " + stepRequestedCount);
    }
  }
  
  /**
   * TestListener that listens for an interpretation to end, and
   * then notifies anyone waiting on it.  (Necessary to prevent tests
   * from overlapping.)
   */
  protected class InterpretListener extends TestListener {
    public void interactionStarted() {
      synchronized(_notifierLock) {
        interactionStartCount++;
        if (printEvents) System.out.println("interactionStarted " + interactionStartCount);
        _notifyLock();
      }
    }
    public void interactionEnded() {
      synchronized(_notifierLock) {
        interactionEndCount++;
        if (printEvents) System.out.println("interactionEnded " + interactionEndCount);
        _notifyLock();
      }
    }
    
    public void interpreterChanged(boolean inProgress){
      synchronized(_notifierLock) {
        interpreterChangedCount++;
        if (printEvents) System.out.println("interpreterChanged " + interpreterChangedCount);
        _notifyLock();
      }
    }
  }
}