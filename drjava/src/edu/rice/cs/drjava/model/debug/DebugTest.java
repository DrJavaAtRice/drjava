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
import javax.swing.SwingUtilities;
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
import edu.rice.cs.util.text.DocumentAdapterException;
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

  protected JPDADebugger _debugger;
  
  /**
   * Constructor.
   * @param  String name
   */
  public DebugTest(String name) {
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
   * Sets the current debugger thread to th 
   */
  private void doSetCurrentThread(final DebugThreadData th) throws DebugException {
    _debugger.setCurrentThread(th);
  }

  /**
   * Resumes the debugger asynchronously so as to aovid 
   * getting notified before we start waiting for notifies
   */
  private void _asyncStep(final int whatKind){
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
  private void _asyncResume(){
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
  private void _asyncDoSetCurrentThread(final DebugThreadData th) {
    new Thread("asyncDoSetCurrentThread Thread") {
      public void run(){
        try {
          doSetCurrentThread(th);
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
   * Tests startup and shutdown, ensuring that all appropriate fields are
   * initialized.
   */
  public void testStartupAndShutdown()
    throws DebugException, InterruptedException
  {
    if (printMessages) System.out.println("----testStartupAndShutdown----");
    DebugTestListener debugListener = new DebugStartAndStopListener();
    _debugger.addListener(debugListener);
    
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(1);
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
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    debugListener.assertDebuggerShutdownCount(1);
    _debugger.removeListener(debugListener);
  }
  
  /** 
   * Test that when two threads are suspended setCurrentThread can be used 
   * to switch between them in the debugger
   */
  public synchronized void testMultiThreadedSetCurrentThread()
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testMultiThreadedSetCurrentThread----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    
     // Compile the class
     OpenDefinitionsDocument doc = doCompile(MONKEY_CLASS, tempFile());
     _debugger.addListener(debugListener);
     // Start debugger
     synchronized(_notifierLock) {
       _debugger.startup();
       _waitForNotifies(1);  // startup
       _notifierLock.wait();
     }
     
     // Set two breakpoints
     int index = MONKEY_CLASS.indexOf("System.out.println(\"I\'m a thread! Yeah!\");");
     _debugger.toggleBreakpoint(doc,index,11);
     index = MONKEY_CLASS.indexOf("System.out.println(\"James likes bananas!\");");
     _debugger.toggleBreakpoint(doc,index,17);
     
      // Run the main() method, hitting both breakpoints in different threads
     synchronized(_notifierLock) {
       interpretIgnoreResult("java Monkey");
       _waitForNotifies(6); // (suspended, updated, breakpointReached) * 2
       _notifierLock.wait();
     }
     DebugThreadData threadA = new DebugThreadData(_debugger.getCurrentThread());
     DebugThreadData threadB = new DebugThreadData(_debugger.getThreadAt(1));
     synchronized(_notifierLock) {
       _asyncDoSetCurrentThread(threadB);
       _waitForNotifies(2);  // updated, suspended
       _notifierLock.wait();
     }
     
     DebugThreadData thread1 = new DebugThreadData(_debugger.getThreadAt(1));
     DebugThreadData thread2 = new DebugThreadData(_debugger.getCurrentThread());
     
     // make sure threads have switched places
     assertTrue(thread1.getUniqueID() == threadA.getUniqueID());
     assertTrue(thread2.getUniqueID() == threadB.getUniqueID());
     
       // Close doc and make sure breakpoints are removed
     _model.closeFile(doc);
     
     // Shutdown the debugger
     if (printMessages) System.out.println("Shutting down...");
     InterpretListener interpretListener = new InterpretListener() {
       public void interpreterChanged(boolean inProgress){
         // Don't notify: happens in the same thread
        interpreterChangedCount++;
       }
     };
     _model.addListener(interpretListener);
     synchronized(_notifierLock) {
       //_asyncResume();
       //_asyncResume();
       _debugger.shutdown();
       _waitForNotifies(2);  // shutdown, interactionEnded
       _notifierLock.wait();
     }
     interpretListener.assertInterpreterChangedCount(1);
     debugListener.assertDebuggerShutdownCount(1);  //fires
     if (printMessages) System.out.println("Shut down.");
     _model.removeListener(interpretListener);
     _debugger.removeListener(debugListener);
  }

  /** 
   * This test has been commented out because we do not support setting the 
   * current thread to be an unsuspended thread right now
   */
//  /**
//   * Tests that setCurrentThread works for multiple threads 
//   */
//   public synchronized void testMultiThreadedSetCurrentThread()
//     throws DebugException, BadLocationException, DocumentAdapterException,
//     IOException, InterruptedException
//   {
//     if (printMessages) System.out.println("----testMultiThreadedSetCurrentThread----");
//     BreakpointTestListener debugListener = new BreakpointTestListener();
//    
//     // Compile the class
//     OpenDefinitionsDocument doc = doCompile(SUSPEND_CLASS, tempFile());
//     _debugger.addListener(debugListener);
//     // Start debugger
//     synchronized(_notifierLock) {
//       _debugger.startup();
//       _waitForNotifies(1);
//       _notifierLock.wait();
//     }
//     debugListener.assertDebuggerStartedCount(1);
//     debugListener.assertDebuggerShutdownCount(0);
//     assertTrue("Debug Manager should be ready", _debugger.isReady());
//     
//     int index = SUSPEND_CLASS.indexOf("int a = 1;");
//     _debugger.toggleBreakpoint(doc,index,5);
//     
//      // Run the main() method, hitting breakpoints
//     synchronized(_notifierLock) {
//       interpretIgnoreResult("java Suspender");
//       _waitForNotifies(3); // suspended, updated, breakpointReached
//       _notifierLock.wait();
//     }    
//     final DebugThreadData thread = new DebugThreadData(_debugger.getCurrentThread());
//     synchronized(_notifierLock){
//       /** _debugger.setCurrentThread(...); 
//        * must be executed in another thread because otherwise the notifies
//        * will be received before the _notifierLock is released
//        */
//       new Thread() {
//         public void run(){
//           try{
//             _debugger.resume();
//             doSetCurrentThread(thread);
//           }catch(DebugException excep){
//             excep.printStackTrace();
//             fail("doSetCurrentThread failed in testMultiThreadedSetCurrentThread");
//           }
//         }
//       }.start();
//       _waitForNotifies(2);  // suspended, updated
//       _notifierLock.wait();
//     }
//     // Ensure thread suspended
//     debugListener.assertCurrThreadSuspendedCount(2);  //fires
//     
//       // Close doc and make sure breakpoints are removed
//     _model.closeFile(doc);
//     
//     // Shutdown the debugger
//     if (printMessages) System.out.println("Shutting down...");
//     synchronized(_notifierLock) {
//       _debugger.shutdown();
//       _waitForNotifies(1);  // shutdown
//       _notifierLock.wait();
//     }
//     
//     debugListener.assertDebuggerShutdownCount(1);  //fires
//     if (printMessages) System.out.println("Shut down.");
//     _debugger.removeListener(debugListener);
//   }
  
  
  /**
   * Tests that breakpoints behave correctly for multiple threads 
   */
  public synchronized void testMultiThreadedBreakpointsAndStep()
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testMultiThreadedBreakpointsAndStep----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    
    // Compile the class
    OpenDefinitionsDocument doc = doCompile(MONKEY_CLASS, tempFile());   
    
    _debugger.addListener(debugListener);
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    
    // Set breakpoints
    int index = MONKEY_CLASS.indexOf("System.out.println(\"I\'m a thread! Yeah!\");");
    _debugger.toggleBreakpoint(doc,index,11);
    index = MONKEY_CLASS.indexOf("System.out.println(\"I just woke up.  I\'m a big boy now.\");");
    _debugger.toggleBreakpoint(doc,index,16);
    debugListener.assertBreakpointSetCount(2);
    
    // Run the main() method, hitting breakpoints
    synchronized(_notifierLock) {
      interpretIgnoreResult("java Monkey");
      _waitForNotifies(6);  // (suspended, updated, breakpointReached) x 2
      _notifierLock.wait();
    }
    
    DebugThreadData thread = new DebugThreadData(_debugger.getCurrentThread());    
    // Resumes one thread, finishing it and switching to the next break point
    synchronized(_notifierLock) {
      _asyncResume();
      _waitForNotifies(3);  // currThreadDied, suspended, updated
      _notifierLock.wait();
    }
    
    DebugThreadData thread2 = new DebugThreadData(_debugger.getCurrentThread());
    assertTrue("testMultiThreadedBreakPoint thread references should not be equal",
               !thread.getName().equals(thread2.getName()));
    
    // Ensure breakpoint is hit
    debugListener.assertBreakpointReachedCount(2);  //fires
    debugListener.assertThreadLocationUpdatedCount(3);  //fires
    debugListener.assertCurrThreadSuspendedCount(3);  //fires
    debugListener.assertCurrThreadResumedCount(1);
    debugListener.assertCurrThreadDiedCount(1);
    _debugger.removeListener(debugListener);
    
    if( printMessages ) System.out.println("Testing stepping...");
    // Step
    StepTestListener stepTestListener = new StepTestListener();
    _debugger.addListener(stepTestListener);
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_INTO);
      _waitForNotifies(2); // suspended, updated
      _notifierLock.wait();
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
      _waitForNotifies(3);  // interactionEnded, currThreadDied, interpreterChanged
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);
    
    // Close doc
    _model.closeFile(doc);
    
    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    
    if (printMessages) System.out.println("Shut down.");
  }
  
  
  
  /**
   * Tests that breakpoints behave correctly.
   */
  public synchronized void testBreakpoints() 
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testBreakpoints----");
    BreakpointTestListener debugListener = new BreakpointTestListener();
    
    // Compile the class
    OpenDefinitionsDocument doc = doCompile(DEBUG_CLASS, tempFile());
    _debugger.addListener(debugListener);
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);
      _notifierLock.wait();
    }
    
   // Add breakpoint before class is loaded
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
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
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");
    
    if (printMessages) System.out.println("adding another breakpoint");
    
    // Set another breakpoint (after is class loaded)
    _debugger.toggleBreakpoint(doc,
       DEBUG_CLASS.indexOf("System.out.println(\"Bar Line 2\")"), 9);
    debugListener.assertBreakpointSetCount(2);
    
    
    // Resume until next breakpoint
    synchronized(_notifierLock) {
      if (printMessages) System.out.println("resuming");
      _asyncResume();
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
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
      if( printMessages ) System.err.println("-------- Resuming --------");
      _asyncResume();
      _waitForNotifies(3);  // interactionEnded, currThreadDied, interpreterChanged
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);
    
    if (printMessages) System.out.println("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(2);  //fires (no waiting)
    debugListener.assertCurrThreadDiedCount(1);  //fires
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertThreadLocationUpdatedCount(2);
    debugListener.assertCurrThreadSuspendedCount(2);
    assertInteractionsContains("Foo Line 3");
    
    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    debugListener.assertBreakpointRemovedCount(2);  //fires twice (no waiting)
      
    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    
    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
    _model.removeListener(interpretListener);
    _debugger.removeListener(debugListener);
  }
  
  /**
   * Tests that breakpoints and steps behave correctly.
   */
  public void testStepInto() 
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testStepInto----");
    StepTestListener debugListener = new StepTestListener();
    
    // Compile the class
    OpenDefinitionsDocument doc = doCompile(DEBUG_CLASS, tempFile());
   
    _debugger.addListener(debugListener); 
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    
    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
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
    assertInteractionsContains("Foo Line 1");
    assertInteractionsDoesNotContain("Bar Line 1");

    // Step into bar() method
    synchronized(_notifierLock){
      _asyncStep(Debugger.STEP_INTO);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
    }
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _waitForNotifies(3);  // interactionEnded, currThreadDied, interpreterChanged
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);
    
    debugListener.assertStepRequestedCount(6);  // fires (don't wait)
    debugListener.assertCurrThreadDiedCount(1);

    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
  
    debugListener.assertBreakpointRemovedCount(1);  //fires once (no waiting)
    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
    _debugger.removeListener(debugListener);
  }
  
  /**
   * Tests that stepping out of a method behaves correctly.
   */
  public synchronized void testStepOut() 
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages)  System.out.println("----testStepOut----");
    StepTestListener debugListener = new StepTestListener();
    
    // Compile the class
    File file2 = new File(_tempDir, "DrJavaDebugClass.java");
    OpenDefinitionsDocument doc = doCompile(DEBUG_CLASS, file2);
    _debugger.addListener(debugListener); 
    // Start debugger and add breakpoint
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    
    // Set breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("bar();"),4);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new DrJavaDebugClass().foo()");
      _waitForNotifies(3);  // suspended, updated, breakpointReached
      _notifierLock.wait();
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
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
    
    // Shutdown the debugger and listen for the interpret call to end
    if (printMessages) System.out.println("Shutting down...");
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
    _model.removeListener(interpretListener);

    debugListener.assertBreakpointRemovedCount(1);  // fires (don't wait)
    debugListener.assertDebuggerShutdownCount(1);  // fires
    if (printMessages) System.out.println("Shut down.");
    _debugger.removeListener(debugListener);
  }
  
  /**
   * Tests that stepping works in a public class with a package
   */
  public synchronized void testStepOverWithPackage() 
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testStepOverWithPackage----");
    StepTestListener debugListener = new StepTestListener();
    
    // Compile the class
    File aDir = new File(_tempDir, "a");
    aDir.mkdir();
    File file = new File(aDir, "DrJavaDebugClassWithPackage.java");
    OpenDefinitionsDocument doc = doCompile(DEBUG_CLASS_WITH_PACKAGE, file);
    
    _debugger.addListener(debugListener); 
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    
    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS_WITH_PACKAGE.indexOf("foo line 1"), 4);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("new a.DrJavaDebugClassWithPackage().foo()");
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
    assertInteractionsDoesNotContain("foo line 1");

    // Step over once
    synchronized(_notifierLock) {
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
    assertInteractionsContains("foo line 1");
    assertInteractionsDoesNotContain("foo line 2");
    
    // Step over again
    synchronized(_notifierLock) {
      _asyncStep(Debugger.STEP_OVER);
      _waitForNotifies(2);  // suspended, updated
      _notifierLock.wait();
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
      _waitForNotifies(3);  // interactionEnded, currThreadDied, interpreterChanged
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    if (printMessages) System.out.println("----After resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertCurrThreadDiedCount(1);  //fires
    debugListener.assertBreakpointReachedCount(1);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);

   
    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    debugListener.assertBreakpointRemovedCount(1);  //fires (no waiting)
    
    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    
    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
    _debugger.removeListener(debugListener);
  }
  
  /**
   * Tests that the sourcepath config option properly adds files to the
   * search directories.
   */
  public void testDebugSourcepath() 
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages)  System.out.println("----testDebugSourcePath----");
    StepTestListener debugListener = new StepTestListener();
    
    // Compile the class
    File file2 = new File(_tempDir, "DrJavaDebugClass.java");
    OpenDefinitionsDocument doc = doCompile(DEBUG_CLASS, file2);
    Vector<File> path = new Vector<File>();
    path.addElement(_tempDir);
    
    _debugger.addListener(debugListener); 
  
    // Start debugger and add breakpoint
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
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
    
    // Shutdown the debugger and listen for the interpret call to end
    if (printMessages) System.out.println("Shutting down...");
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
    interpretListener.assertInteractionEndCount(1);  // fires
    interpretListener.assertInterpreterChangedCount(1);  // fires (don't wait)
    _model.removeListener(interpretListener);
    
    debugListener.assertDebuggerShutdownCount(1);  // fires
    if (printMessages) System.out.println("Shut down.");
    _debugger.removeListener(debugListener);
  }
  
  /**
   * Tests that breakpoints behave correctly in non-public classes.
   */
  public synchronized void testBreakpointsAndStepsInNonPublicClasses() 
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) System.out.println("----testBreakpointsAndStepsInNonPublicClasses----");
    StepTestListener debugListener = new StepTestListener();
    
    // Compile the class
    OpenDefinitionsDocument doc = doCompile(DEBUG_CLASS, tempFile());
    _debugger.addListener(debugListener);
    // Start debugger and add breakpoint (before class is loaded)
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);
      _notifierLock.wait();
    }

    // Add a breakpoint
    _debugger.toggleBreakpoint(doc,DEBUG_CLASS.indexOf("Baz Line 1"),14);
    debugListener.assertBreakpointSetCount(1);
    
    // Run the foo() method, hitting breakpoint
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
      _waitForNotifies(3);  // currThreadDied, interactionEnded, interpreterChanged
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);
    
    if (printMessages) System.out.println("----After second resume:\n" + getInteractionsText());
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertCurrThreadDiedCount(1);  //fires
    debugListener.assertBreakpointReachedCount(2);
    debugListener.assertThreadLocationUpdatedCount(3);
    debugListener.assertCurrThreadSuspendedCount(3);
    assertInteractionsContains("Bar Line 2");
    
    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    debugListener.assertBreakpointRemovedCount(2);  //fires twice (no waiting)
      
    // Shutdown the debugger
    if (printMessages) System.out.println("Shutting down...");
    synchronized(_notifierLock) {
      _debugger.shutdown();
      _waitForNotifies(1);  // shutdown
      _notifierLock.wait();
    }
    debugListener.assertDebuggerShutdownCount(1);  //fires
    if (printMessages) System.out.println("Shut down.");
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
  
  /**
   * Tests that stepping into a breakpoint works.
   */
  public synchronized void testStepIntoOverBreakpoint() 
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) {
      System.out.println("----testStepIntoOverBreakpoint----");
    }
    StepTestListener debugListener = new StepTestListener();
    
    // Compile the class
    File file = new File(_tempDir, "DrJavaDebugClass.java");
    OpenDefinitionsDocument doc = doCompile(DEBUG_CLASS, file);
    
    _debugger.addListener(debugListener); 
    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(1);
    
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
      _waitForNotifies(3);  // interactionEnded, currThreadDied, interpreterChanged
      _notifierLock.wait();
    }
    interpretListener.assertInteractionEndCount(1);
    _model.removeListener(interpretListener);

    if (printMessages) {
      System.out.println("----After resume:\n" + getInteractionsText());
    }
    debugListener.assertCurrThreadResumedCount(3);  //fires (no waiting)
    debugListener.assertCurrThreadDiedCount(1);  //fires
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
  public void testStaticFieldsConsistent()
    throws DebugException, BadLocationException, DocumentAdapterException,
    IOException, InterruptedException
  {
    if (printMessages) {
      System.out.println("----testStaticFieldsConsistent----");
    }
    StepTestListener debugListener = new StepTestListener();
    
    // Compile the class
    File file = new File(_tempDir, "DrJavaDebugStaticField.java");
    OpenDefinitionsDocument doc = doCompile(CLASS_WITH_STATIC_FIELD, file);
    
    _debugger.addListener(debugListener);

    // Start debugger
    synchronized(_notifierLock) {
      _debugger.startup();
      _waitForNotifies(1);  // startup
      _notifierLock.wait();
    }
    debugListener.assertDebuggerStartedCount(1);
    
    _debugger.toggleBreakpoint(doc,CLASS_WITH_STATIC_FIELD.indexOf("System.out.println"), 4);
    debugListener.assertBreakpointSetCount(1);

    // Run the foo() method, hitting breakpoint
    synchronized(_notifierLock) {
      interpretIgnoreResult("java DrJavaDebugStaticField");
      _waitForNotifies(6);  // (suspended, updated, breakpointReached) *2
      _notifierLock.wait();
    }

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
    debugListener.assertCurrThreadDiedCount(0);
    debugListener.assertCurrThreadSuspendedCount(4);  // fires
    debugListener.assertBreakpointReachedCount(2);
    assertEquals("x has correct value after increment", "6", interpret("DrJavaDebugStaticField.x"));
    assertEquals("this has correct value for x after increment", "6", interpret("this.x"));

    synchronized(_notifierLock){
      _asyncDoSetCurrentThread(threadB);
      _waitForNotifies(2);  // updated, suspended
      _notifierLock.wait();
    }
    assertEquals("x has correct value in other thread", "6", interpret("DrJavaDebugStaticField.x"));
    assertEquals("this has correct value for x in other thread", "6", interpret("this.x"));

    // Close doc and make sure breakpoints are removed
    _model.closeFile(doc);
    debugListener.assertBreakpointRemovedCount(1);  //fires (no waiting)
    
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
  class DebugStartAndStopListener extends DebugTestListener {
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
  class BreakpointTestListener extends DebugStartAndStopListener {
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
                                      int lineNumber) {
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
  class StepTestListener extends BreakpointTestListener {
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
  public class InterpretListener extends TestListener {
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
