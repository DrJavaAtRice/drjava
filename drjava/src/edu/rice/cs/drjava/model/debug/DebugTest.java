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

import junit.framework.*;

import java.io.*;

import javax.swing.text.BadLocationException;
import junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
import gj.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.*;
import edu.rice.cs.drjava.CodeStatus;

/**
 * Tests the JPDA-based debugger.
 *
 * @version $Id$
 */
public class DebugTest extends GlobalModelTestCase implements OptionConstants {
  
  final boolean printEvents = false;
  final boolean printMessages = false;
  
  private int _pendingNotifies = 0;
  private Object _notifierLock = new Object();
  private Vector<File> _userSourcePath;
  private Boolean _userStepJava;
  private Boolean _userStepInterpreter;
  private Boolean _userStepDrJava;
  protected static final String DEBUG_CLASS = 
    /*  1 */ "class DrJavaDebugClass {\n" + //19
    /*  2 */ "  public void foo() {\n" +//43
    /*  3 */ "    System.out.println(\"Foo Line 1\");\n" +
    /*  4 */ "    bar();\n" +
    /*  5 */ "    System.out.println(\"Foo Line 3\");\n" +
    /*  6 */ "  }\n" +
    /*  7 */ "  public void bar() {\n" +
    /*  8 */ "    System.out.println(\"Bar Line 1\");\n" +
    /*  9 */ "    System.out.println(\"Bar Line 2\");\n" + 
    /* 10 */ "  }\n" +
    /* 11 */ "}";
  
  protected DebugManager _debugManager;
  
  /**
   * Constructor.
   * @param  String name
   */
  public DebugTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(DebugTest.class);
  }

  public void setUp() throws IOException {
    super.setUp();
    _debugManager = _model.getDebugManager();
    assertTrue("Debug Manager should not be null", _debugManager != null);
    
    // Remember all previous settings and clear them
    _userSourcePath = ConfigurationTool.CONFIG.getSetting(OptionConstants.DEBUG_SOURCEPATH);
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_SOURCEPATH,new Vector<File>());
    _userStepInterpreter = ConfigurationTool.CONFIG.getSetting(OptionConstants.DEBUG_STEP_INTERPRETER);
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_STEP_INTERPRETER,new Boolean(false));
    _userStepJava = ConfigurationTool.CONFIG.getSetting(OptionConstants.DEBUG_STEP_JAVA);
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_STEP_JAVA,new Boolean(false));
    _userStepDrJava = ConfigurationTool.CONFIG.getSetting(OptionConstants.DEBUG_STEP_DRJAVA);
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_STEP_DRJAVA,new Boolean(false));
    
  }
  
  public void tearDown() throws IOException{
    super.tearDown();
    assertTrue("Debug Manager should not be null", _debugManager != null);
    
    // Reset all previous settings
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_SOURCEPATH,_userSourcePath);
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_STEP_INTERPRETER,_userStepInterpreter);
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_STEP_JAVA, _userStepJava);
    ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_STEP_DRJAVA,_userStepDrJava);
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
  private void _waitForNotifies(int n)
    throws InterruptedException
  {
    synchronized(_notifierLock) {
      if (printMessages) System.out.println("waiting for " + n + " notifications...");
      _pendingNotifies = n;
    }
  }
  
  /**
   * Notifies the given object if the notify count has expired.
   * See _waitForNotifies
   */
  private void _notifyObject(Object o) {
    synchronized(_notifierLock) {
      if (printMessages) System.out.println("notified");
      _pendingNotifies--;
      if(_pendingNotifies == 0){
        if (printMessages) System.out.println("Notify count reached 0-- notifying!");
        o.notify();
      }
      if(_pendingNotifies < 0){
        if (printMessages) System.out.println("Notified too many times!");
        fail("Notified too many times");
      }
    }
  }
  
  /**
   * tests debug classpath config option
   */
  public void testDebugSourcepath() 
    throws DebugException, BadLocationException, IOException, InterruptedException
  {
    if (printMessages)  System.out.println("----testDebugSourcePath----");
    DebugTestListener debugListener = new DebugTestListener() {
      public void breakpointSet(Breakpoint bp) {
        breakpointSetCount++;
      }
      public void breakpointReached(Breakpoint bp) {
        synchronized(_notifierLock) {
          breakpointReachedCount++;
          if (printEvents) System.out.println("breakpointReached " + breakpointReachedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void breakpointRemoved(Breakpoint bp) {
        //synchronized(_notifierLock) {
        breakpointRemovedCount++;
        if (printEvents) System.out.println("breakpointRemoved " + breakpointRemovedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadSuspended() {
        synchronized(_notifierLock) {
          currThreadSuspendedCount++;
          if (printEvents) System.out.println("threadSuspended " + currThreadSuspendedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void currThreadResumed() {
        //synchronized(_notifierLock) {
        currThreadResumedCount++;
        if (printEvents) System.out.println("threadResumed " + currThreadResumedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadDied() {
        synchronized(_notifierLock) {
          currThreadDiedCount++;
          if (printEvents) System.out.println("threadDied " + currThreadDiedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber){
        synchronized(_notifierLock) {
          threadLocationUpdatedCount++;
          if (printEvents) System.out.println("threadUpdated " + threadLocationUpdatedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void debuggerShutdown() {
        synchronized(_notifierLock) {
          debuggerShutdownCount++;
          if (printEvents) System.out.println("debuggerShutdown " + debuggerShutdownCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void debuggerStarted() {
        synchronized(_notifierLock) {
          debuggerStartedCount++;
          if (printEvents) System.out.println("debuggerStarted " + debuggerStartedCount);
          _notifyObject(_notifierLock);
        }
      }
    };
    
    // Compile the class
    File file2 = new File(_tempDir, "DrJavaDebugClass.java");
    OpenDefinitionsDocument doc = _doCompile(DEBUG_CLASS, file2);
    Vector<File> path = new Vector<File>();
    path.addElement(_tempDir);
    
    _debugManager.addListener(debugListener); 
  
    // Start debugger and add breakpoint
    synchronized(_notifierLock) {
      _debugManager.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    _debugManager.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
   
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    // Source is highlighted because doc is on breakpoint object
    debugListener.assertThreadLocationUpdatedCount(1);  // fires
    
    // Step into bar() method
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_INTO);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    // Source is highlighted because file is in source root set
    debugListener.assertThreadLocationUpdatedCount(2);  // fires
    
    
    // Close file so it won't be in source root set
    _model.closeFile(doc);
    
    // Step to next line
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_OVER);
      _waitForNotifies(1);  // suspended
      _notifierLock.wait();
    }
    // Source is not highlighted
   
    debugListener.assertThreadLocationUpdatedCount(2);  // doesn't fire
   
    synchronized(_debugManager){
      // Add _tempDir to our sourcepath
      ConfigurationTool.CONFIG.setSetting(OptionConstants.DEBUG_SOURCEPATH, path);
    }
    
    // Step to next line
    synchronized(_notifierLock){
     
     
      _debugManager.step(DebugManager.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    // Source is highlighted because file is on sourcepath
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    
    
    //Remove listener at end
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugManager.shutdown();
      _waitForNotifies(2);  // threadDied, shutdown
      _notifierLock.wait();
    }
    debugListener.assertCurrThreadDiedCount(1);  // fires
    debugListener.assertBreakpointRemovedCount(1);  // fires (don't wait)
    debugListener.assertDebuggerShutdownCount(1);  // fires
    if (printMessages) System.out.println("Shut down.");
    _debugManager.removeListener(debugListener);
  }
  
  
  /**
   * Tests that stepping out of a method behaves correctly.
   */
  public void testStepOut() 
    throws DebugException, BadLocationException, IOException, InterruptedException
  {
    if (printMessages)  System.out.println("----testStepOut----");
    DebugTestListener debugListener = new DebugTestListener() {
      public void breakpointSet(Breakpoint bp) {
        breakpointSetCount++;
      }
      public void breakpointReached(Breakpoint bp) {
        synchronized(_notifierLock) {
          breakpointReachedCount++;
          if (printEvents) System.out.println("breakpointReached " + breakpointReachedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void breakpointRemoved(Breakpoint bp) {
        //synchronized(_notifierLock) {
          breakpointRemovedCount++;
          if (printEvents) System.out.println("breakpointRemoved " + breakpointRemovedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadSuspended() {
        synchronized(_notifierLock) {
          currThreadSuspendedCount++;
          if (printEvents) System.out.println("threadSuspended " + currThreadSuspendedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void currThreadResumed() {
        //synchronized(_notifierLock) {
          currThreadResumedCount++;
          if (printEvents) System.out.println("threadResumed " + currThreadResumedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadDied() {
        synchronized(_notifierLock) {
          currThreadDiedCount++;
          if (printEvents) System.out.println("threadDied " + currThreadDiedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber){
        synchronized(_notifierLock) {
          threadLocationUpdatedCount++;
          if (printEvents) System.out.println("threadUpdated " + threadLocationUpdatedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void debuggerShutdown() {
        synchronized(_notifierLock) {
          debuggerShutdownCount++;
          if (printEvents) System.out.println("debuggerShutdown " + debuggerShutdownCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void debuggerStarted() {
        synchronized(_notifierLock) {
          debuggerStartedCount++;
          if (printEvents) System.out.println("debuggerStarted " + debuggerStartedCount);
          _notifyObject(_notifierLock);
        }
      }
    };
    
    // Compile the class
    File file2 = new File(_tempDir, "DrJavaDebugClass.java");
    OpenDefinitionsDocument doc = _doCompile(DEBUG_CLASS, file2);
    _debugManager.addListener(debugListener); 
    // Start debugger and add breakpoint
    synchronized(_notifierLock) {
      _debugManager.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    
    debugListener.assertDebuggerStartedCount(1);
    
    _debugManager.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    
    if (printMessages) System.out.println("----After breakpoint:\n" + _getInteractionsText());
      
    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  // fires
    debugListener.assertThreadLocationUpdatedCount(1);  // fires
    debugListener.assertCurrThreadSuspendedCount(1);  // fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    // Step into bar() method
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_INTO);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(2);  //fires
    debugListener.assertCurrThreadSuspendedCount(2);  //fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("Bar Line 1");
    
    // Step out of method
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_OUT);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    
    if (printMessages) System.out.println("****"+_getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(3);  // fires
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("Bar Line 2");
    assertInteractionsDoesNotContain("Foo Line 3");
    
    //Remove listener at end
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugManager.shutdown();
      _waitForNotifies(2);  // threadDied, shutdown
      _notifierLock.wait();
    }
    debugListener.assertCurrThreadDiedCount(1);  // fires
    debugListener.assertBreakpointRemovedCount(1);  // fires (don't wait)
    debugListener.assertDebuggerShutdownCount(1);  // fires
    if (printMessages) System.out.println("Shut down.");
    _debugManager.removeListener(debugListener);
  }

  
  /**
   * Tests that breakpoints and steps behave correctly.
   */
  public void testStepInto() 
    throws DebugException, BadLocationException, IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testStepInto----");
    DebugTestListener debugListener = new DebugTestListener() {
      public void breakpointSet(Breakpoint bp) {
        breakpointSetCount++;
      }
      public void breakpointReached(Breakpoint bp) {
        synchronized(_notifierLock) {
          breakpointReachedCount++;
          if (printEvents) System.out.println("breakpointReached " + breakpointReachedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void breakpointRemoved(Breakpoint bp) {
        //synchronized(_notifierLock) {
          breakpointRemovedCount++;
          if (printEvents) System.out.println("breakpointRemoved " + breakpointRemovedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadSuspended() {
        synchronized(_notifierLock) {
          currThreadSuspendedCount++;
          if (printEvents) System.out.println("threadSuspended " + currThreadSuspendedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void currThreadResumed() {
        //synchronized(_notifierLock) {
          currThreadResumedCount++;
          if (printEvents) System.out.println("threadResumed " + currThreadResumedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadDied() {
        synchronized(_notifierLock) {
          currThreadDiedCount++;
          if (printEvents) System.out.println("threadDied " + currThreadDiedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber){
        synchronized(_notifierLock) {
          threadLocationUpdatedCount++;
          if (printEvents) System.out.println("threadUpdated " + threadLocationUpdatedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void debuggerShutdown() {
        synchronized(_notifierLock) {
          debuggerShutdownCount++;
          if (printEvents) System.out.println("debuggerShutdown " + debuggerShutdownCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void debuggerStarted() {
        synchronized(_notifierLock) {
          debuggerStartedCount++;
          if (printEvents) System.out.println("debuggerStarted " + debuggerStartedCount);
          _notifyObject(_notifierLock);
        }
      }
    };
    
    // Compile the class
    OpenDefinitionsDocument doc = _doCompile(DEBUG_CLASS, tempFile());
   
    _debugManager.addListener(debugListener); 
    // Start debugger
    synchronized(_notifierLock) {
      _debugManager.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(1);
    
    // Add a breakpoint
    _debugManager.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    
    if (printMessages) System.out.println("----After breakpoint:\n" + _getInteractionsText());
      
    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(1);  //fires
    debugListener.assertThreadLocationUpdatedCount(1);  //fires
    debugListener.assertCurrThreadSuspendedCount(1);  //fires
    debugListener.assertCurrThreadResumedCount(0);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    // Step into bar() method
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_INTO);
      _waitForNotifies(1);  // suspended
      _notifierLock.wait();
    }
    debugListener.assertCurrThreadResumedCount(1); // fires (don't wait)
    //NOTE: LocationUpdatedCount is still 1 because the manager could not find the
    //file on the sourcepath so the count was not updated.
    debugListener.assertThreadLocationUpdatedCount(1);
    debugListener.assertCurrThreadSuspendedCount(2);  //fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsDoesNotContain("Bar Line 1");
    
    // Step to next line
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_OVER);
      _waitForNotifies(1);  // suspended
      _notifierLock.wait();
    }
    
    if (printMessages) System.out.println("****"+_getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(1);  
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertBreakpointReachedCount(1);
    assertInteractionsContains("Bar Line 1");
    assertInteractionsDoesNotContain("Bar Line 2");
    

    // Step to next line
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_OVER);
      _waitForNotifies(1);  // suspended
      _notifierLock.wait();
    }
    
    debugListener.assertCurrThreadResumedCount(3); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(4);  //fires
    debugListener.assertBreakpointReachedCount(1);        
    assertInteractionsContains("Bar Line 2");
    assertInteractionsDoesNotContain("Foo Line 3");
    
    // Step twice to print last line in Foo
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_OVER);
      _waitForNotifies(1);  // suspended
      _notifierLock.wait();
    }

    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_OVER);
      _waitForNotifies(1);  // suspended
      _notifierLock.wait();
    }

    debugListener.assertCurrThreadResumedCount(5); // fires (don't wait)
    debugListener.assertThreadLocationUpdatedCount(1);
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(6);  //fires
    debugListener.assertBreakpointReachedCount(1);      
    assertInteractionsContains("Foo Line 3");
    
    
    // Step again to finish
    synchronized(_notifierLock){
      _debugManager.step(DebugManager.STEP_OVER);
      _waitForNotifies(1);  // threadDied
      _notifierLock.wait();
    }
    debugListener.assertCurrThreadDiedCount(1);

      // Remove listener at end
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugManager.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    debugListener.assertBreakpointRemovedCount(1);  //fires once (no waiting)
    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
    _debugManager.removeListener(debugListener);
  }

  
  
  /**
   * Tests that breakpoints behave correctly.
   */
  public synchronized void testBreakpoints() 
    throws DebugException, BadLocationException, IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testBreakpoints----");
    DebugTestListener debugListener = new DebugTestListener() {
      public void breakpointSet(Breakpoint bp) {
        breakpointSetCount++;
      }
      public void breakpointReached(Breakpoint bp) {
        synchronized(_notifierLock) {
          breakpointReachedCount++;
          if (printEvents) System.out.println("breakpointReached " + breakpointReachedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void breakpointRemoved(Breakpoint bp) {
        //synchronized(_notifierLock) {
          breakpointRemovedCount++;
          if (printEvents) System.out.println("breakpointRemoved " + breakpointRemovedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadSuspended() {
        synchronized(_notifierLock) {
          currThreadSuspendedCount++;
          if (printEvents) System.out.println("threadSuspended " + currThreadSuspendedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void currThreadResumed() {
        //synchronized(_notifierLock) {
          currThreadResumedCount++;
          if (printEvents) System.out.println("threadResumed " + currThreadResumedCount);
        //  _notifyObject(_notifierLock);
        //}
      }
      
      public void currThreadDied() {
        synchronized(_notifierLock) {
          currThreadDiedCount++;
          if (printEvents) System.out.println("threadDied " + currThreadDiedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber){
        synchronized(_notifierLock) {
          threadLocationUpdatedCount++;
          if (printEvents) System.out.println("threadUpdated " + threadLocationUpdatedCount);
          _notifyObject(_notifierLock);
        }
      }
      
      public void debuggerShutdown() {
        synchronized(_notifierLock) {
          debuggerShutdownCount++;
          if (printEvents) System.out.println("debuggerShutdown " + debuggerShutdownCount);
          _notifyObject(_notifierLock);
        }
      }
       
      public void debuggerStarted() {
        synchronized(_notifierLock) {
          debuggerStartedCount++;
          if (printEvents) System.out.println("debuggerStarted " + debuggerStartedCount);
          _notifyObject(_notifierLock);
        }
      }
    };
    
    
    
    // Compile the class
    OpenDefinitionsDocument doc = _doCompile(DEBUG_CLASS, tempFile());
    _debugManager.addListener(debugListener);
    // Start debugger and add breakpoint (before class is loaded)
    synchronized(_notifierLock) {
      _debugManager.startup();
      _waitForNotifies(1);
      _notifierLock.wait();
    }
   
    _debugManager.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    
    if (printMessages) System.out.println("----After breakpoint:\n" + _getInteractionsText());
      
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
    _debugManager.toggleBreakpoint(doc,
       DEBUG_CLASS.indexOf("System.out.println(\"Bar Line 2\")"), 9);
    debugListener.assertBreakpointSetCount(2);
    
    
    // Resume until next breakpoint
    synchronized(_notifierLock) {
      if (printMessages) System.out.println("resuming");
      _debugManager.resume();
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
    }
    if (printMessages) System.out.println("----After one resume:\n" + _getInteractionsText());
    debugListener.assertCurrThreadResumedCount(1);  //fires (no waiting)
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(2);  //fires
    debugListener.assertCurrThreadSuspendedCount(2);  //fires
    debugListener.assertCurrThreadDiedCount(0);
    assertInteractionsContains("Bar Line 1");
    assertInteractionsDoesNotContain("Bar Line 2");
    
    // Resume until finished
    synchronized(_notifierLock) {
      _debugManager.resume();
      _waitForNotifies(1);  // threadDied
      _notifierLock.wait();
    }
    if (printMessages) System.out.println("----After second resume:\n" + _getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2);  //fires (no waiting)
    debugListener.assertCurrThreadDiedCount(1);  //fires
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertThreadLocationUpdatedCount(2);
    debugListener.assertCurrThreadSuspendedCount(2);
    assertInteractionsContains("Foo Line 3");

      
    // Remove listener at end
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugManager.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    debugListener.assertBreakpointRemovedCount(2);  //fires twice (no waiting)
    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
    _debugManager.removeListener(debugListener);
  }
  
  
  /**
   * Tests that starting up and shutting down the debugger triggers the
   * correct states and events.
   */
  public synchronized void testStartupAndShutdown() throws DebugException, 
    InterruptedException {
    if (printMessages) System.out.println("----testStartupAndShutdown----");
    DebugTestListener listener = new DebugTestListener() {
      public void debuggerStarted() {
        synchronized(_notifierLock) {
          debuggerStartedCount++;
          if (printEvents) System.out.println("debuggerStarted " + debuggerStartedCount);
          _notifyObject(_notifierLock);
        }
      }
      public void debuggerShutdown() {
        synchronized(_notifierLock) {
          debuggerShutdownCount++;
          if (printEvents) System.out.println("debuggerShutdown " + debuggerShutdownCount);
          _notifyObject(_notifierLock);
        }
      }
    };

    _debugManager.addListener(listener);
     synchronized(_notifierLock) {
      _debugManager.startup();
      _waitForNotifies(1);
      _notifierLock.wait();
    }
    
    listener.assertDebuggerStartedCount(1);
    listener.assertDebuggerShutdownCount(0);
    assertTrue("Debug Manager should be ready", _debugManager.isReady());
    
    synchronized(_notifierLock) {
      _debugManager.shutdown();
      _waitForNotifies(1);
      _notifierLock.wait();
    }
    listener.assertDebuggerStartedCount(1);
    listener.assertDebuggerShutdownCount(1);
    assertTrue("Debug Manager should not be ready", !_debugManager.isReady());

    _debugManager.removeListener(listener);
  }
  
  
  /**
   * Compiles a new file with the given text.
   */
  private OpenDefinitionsDocument _doCompile(String text, File file)
    throws IOException, BadLocationException
  {
    OpenDefinitionsDocument doc = setupDocument(text);
    doc.saveFile(new FileSelector(file));
    doc.startCompile();
    return doc;
  }
  
  /**
   * Asserts that the given string exists in the Interactions Document.
   */
  private void assertInteractionsContains(String text) throws BadLocationException{
    assertInteractionHelper(text, true);
  }
  
  private void assertInteractionsDoesNotContain(String text)
    throws BadLocationException{
    assertInteractionHelper(text,false);
  }
  
  private void assertInteractionHelper(String text, boolean shouldContain)
    throws BadLocationException {
    
    String interactText = _getInteractionsText();
    int contains = interactText.lastIndexOf(text);
    assertTrue("Interactions document should " +
               (shouldContain ? "" : "not ")
                 + "contain: "
                 +text,
               (contains != -1) == shouldContain);    
  }
  
  /**
   * Returns the current contents of the interactions document
   */
  private String _getInteractionsText() throws BadLocationException {
    Document doc = _model.getInteractionsDocument();
    return doc.getText(0, doc.getLength());
  }
  
  
  /**
   * Listens to events from the debugger to ensure that they happen at the
   * correct times.
   */
  class DebugTestListener implements DebugListener {
    protected int debuggerStartedCount = 0;
    protected int debuggerShutdownCount = 0;
    protected int threadLocationUpdatedCount = 0;
    protected int breakpointSetCount = 0;
    protected int breakpointReachedCount = 0;
    protected int breakpointRemovedCount = 0;
    protected int currThreadSuspendedCount = 0;
    protected int currThreadResumedCount = 0;
    protected int currThreadDiedCount = 0;
    
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
    
    public void assertCurrThreadSuspendedCount(int i) {
      assertEquals("number of times currThreadSuspended fired", i,
                   currThreadSuspendedCount);
    }
    
    public void assertCurrThreadResumedCount(int i) {
      assertEquals("number of times currThreadResumed fired", i,
                   currThreadResumedCount);
    }
    
    public void assertCurrThreadDiedCount(int i) {
      assertEquals("number of times currThreadDied fired", i,
                   currThreadDiedCount);
    }


    public void debuggerStarted() {
      fail("debuggerStarted fired unexpectedly");
    }
    
    public void debuggerShutdown() {
      fail("debuggerShutdown fired unexpectedly");
    }

    public void threadLocationUpdated(OpenDefinitionsDocument doc, int lineNumber) {
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
    
    public void currThreadSuspended() {
      fail("currThreadSuspended fired unexpectedly");
    }
    
    public void currThreadResumed() {
      fail("currThreadResumed fired unexpectedly");
    }
    
    public void currThreadDied() {
      fail(" fired unexpectedly");
    }
  }
}
