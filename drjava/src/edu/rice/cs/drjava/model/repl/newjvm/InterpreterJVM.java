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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.util.LinkedList;
import java.util.ListIterator;
import gj.util.Vector;
import gj.util.Hashtable;
import gj.util.Enumeration;
import java.io.*;
import java.rmi.*;

// NOTE: Do NOT import/use the config framework in this class!
//  (This class runs in a different JVM, and will not share the config object)

import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.InputStreamRedirector;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.junit.JUnitTestManager;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.repl.*;

import edu.rice.cs.javaast.parser.*;
import edu.rice.cs.javaast.tree.*;
import edu.rice.cs.javaast.*;

// For Windows focus fix
import javax.swing.JDialog;

/**
 * This is the main class for the interpreter JVM.
 * Note that this class is specific to using DynamicJava. It would need
 * to be subclassed to use with another interpreter. (Really, there would
 * need to be an abstract base class, but since we don't need it yet I'm
 * not making one.)
 *
 * @version $Id$
 */
public class InterpreterJVM extends AbstractSlaveJVM
                            implements InterpreterJVMRemoteI
{
  private static final boolean printMessages = false;
  /** Singleton instance of this class. */
  public static final InterpreterJVM ONLY = new InterpreterJVM();
  
  /** String to append to error messages when no stack trace is available. */
  public static final String EMPTY_TRACE_TEXT = "";
  //public static final String EMPTY_TRACE_TEXT = "  at (the interactions window)";
  

  /** Remote reference to the MainJVM class in DrJava's primary JVM. */
  private MainJVMRemoteI _mainJVM;
  
  /** Metadata encapsulating the default interpreter. */
  private InterpreterData _defaultInterpreter;
  
  /** Maps names to interpreters with metadata. */
  private Hashtable<String,InterpreterData> _interpreters;
  
  private InterpreterData _activeInterpreter;

  /** 
   * The currently accumulated classpath for all Java interpreters.
   * List contains unqiue entries.
   */
  private Vector<String> _classpath;
  
  /** Responsible for running JUnit tests in this JVM. */
  private JUnitTestManager _junitTestManager;
  
  /** Interactions processor, currently a pre-processor **/
  private InteractionsProcessorI _interactionsProcessor;
  
  /**
   * Whether to display an error message if a reset fails.
   */
  private boolean _messageOnResetFailure;
  
  /**
   * Private constructor; use the singleton ONLY instance.
   */
  private InterpreterJVM() {
    reset();
    _interactionsProcessor = new InteractionsProcessor();
    _messageOnResetFailure = true;
  }
  
  /**
   * Resets this InterpreterJVM to its default state.
   */
  public void reset() {
    _defaultInterpreter = new InterpreterData(new DynamicJavaAdapter());
    _activeInterpreter = _defaultInterpreter;
    _interpreters = new Hashtable<String,InterpreterData>();
    _junitTestManager = new JUnitTestManager(this);
    _classpath = new Vector<String>();
    
    // do an interpretation to get the interpreter loaded fully
    try {
      _activeInterpreter.getInterpreter().interpret("0");
    }
    catch (ExceptionReturnedException e) {
      throw new edu.rice.cs.util.UnexpectedException(e);
    }
  }

  private static final Log _log = new Log("IntJVMLog", false);
  private static void _dialog(String s) {
    //javax.swing.JOptionPane.showMessageDialog(null, s);
    _log.logTime(s);
  }
  
  /**
   * Actions to perform when this JVM is started (through its superclass,
   * AbstractSlaveJVM).
   */
  protected void handleStart(MasterRemote mainJVM) {
    //_dialog("handleStart");
    _mainJVM = (MainJVMRemoteI) mainJVM;

    // redirect stdin
    try {
      System.setIn(new InputStreamRedirector() {
        protected String _getInput() {
          try {
            return _mainJVM.getConsoleInput();
          }
          catch (RemoteException re) {
            // blow up if no MainJVM found
            _log.logTime("System.in: " + re.toString());
            throw new IllegalStateException("Main JVM can't be reached for input.\n" + re);
          }
        }
      });
    }
    catch (IOException ioe) {
      // leaves System.in alone
      _log.logTime("Creating InputStreamRedirector: " + ioe.toString());
    }

    // redirect stdout
    System.setOut(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try {
          //_log.logTime("out.print: " + s);
          _mainJVM.systemOutPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.logTime("System.out: " + re.toString());
        }
      }
    }));

    // redirect stderr
    System.setErr(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try {
          //_log.logTime("err.print: " + s);
          _mainJVM.systemErrPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.logTime("System.err: " + re.toString());
        }
      }
    }));
    
    // On Windows, any frame or dialog opened from Interactions pane will
    // appear *behind* DrJava's frame, unless a previous frame or dialog
    // is shown here.  Not sure what the difference is, but this hack
    // seems to work.  (I'd be happy to find a better solution, though.)
    // Only necessary on Windows, since frames and dialogs on other
    // platforms appear correctly in front of DrJava.
    if (PlatformFactory.ONLY.isWindowsPlatform()) {
      JDialog d = new JDialog();
      d.setSize(0,0);
      d.show();
      d.hide();
    }
    //_dialog("interpreter JVM started");
  }


  /**
   * Interprets the given string of source code in the active interpreter.
   * The result is returned to MainJVM via the interpretResult method.
   * @param s Source code to interpret.
   */
  public void interpret(String s) {
    interpret(s, _activeInterpreter);
  }
  
  /**
   * Interprets the given string of source code in the interpreter with the
   * given name.
   * The result is returned to MainJVM via the interpretResult method.
   * @param s Source code to interpret.
   * @param interpreterName Name of the interpreter to use
   * @throws IllegalArgumentException if the named interpreter does not exist
   */
  public void interpret(String s, String interpreterName) {
    interpret(s, getInterpreter(interpreterName));
  }
  
  /**
   * Interprets the given string of source code with the given interpreter.
   * The result is returned to MainJVM via the interpretResult method.
   * @param s Source code to interpret.
   * @param interpreter The interpreter (plus metadata) to use
   */
  public synchronized void interpret(final String s, final InterpreterData interpreter) {
    Thread thread = new Thread("interpret thread: " + s) {
      public void run() {
        try {
          interpreter.setInProgress(true);
          try {
            _dialog("to interp: " + s);
            
            // Processor disabled until bug 750605 fixed
            //String s1 = _interactionsProcessor.preProcess(s);
            Object result = interpreter.getInterpreter().interpret(s);
            //String s2 = _interactionsProcessor.postProcess(s1, result);
            
            if (result == Interpreter.NO_RESULT) {
              //return new VoidResult();
              _dialog("void interp ret: " + result);
              _mainJVM.interpretResult(new VoidResult());
            }
            else {
              // we use String.valueOf because it deals with result = null!
              _dialog("about to tell main result was " + result);
              //return new ValueResult(String.valueOf(result));
              _mainJVM.interpretResult(new ValueResult(String.valueOf(result)));

            }
          }
          catch (ExceptionReturnedException e) {
            Throwable t = e.getContainedException();
            _dialog("interp exception: " + t);
            
            //_dialog("before call to threwException");
            //return new ExceptionResult(t.getClass().getName(),
            //                           t.getMessage(),
            //                           getStackTrace(t));
            _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(),
                                                         t.getMessage(),
                                                         getStackTrace(t)));
          }
//           catch (ParseException pe) {
//             // A ParseException indicates a syntax error in the input window
//             _mainJVM.interpretResult( new SyntaxErrorResult( pe, s ) );
//           }
//           catch (TokenMgrError tme) {
//             // A TokenMgrError indicates some lexical difficulty with input.
//             _mainJVM.interpretResult( new SyntaxErrorResult( tme, s ) );
//           }
          catch (Throwable t) {
            // A user's toString method might throw anything, so we need to be careful
            //_dialog("thrown by toString: " + t);
            //return new ExceptionResult(t.getClass().getName(),
            //                           t.getMessage(),
            //                           getStackTrace(t));
            _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(),
                                                         t.getMessage(),
                                                         getStackTrace(t)));
          }
        }
        catch (RemoteException re) {
          // Can't communicate with MainJVM?  Nothing to do...
          _log.logTime("interpret: " + re.toString());
        }
        finally {
          interpreter.setInProgress(false);
        }
      }
    };

    thread.setDaemon(true);
    thread.start();
  }
  
  /**
   * Notifies that an assignment has been made in the given interpreter.
   * Does not notify on declarations.
   * 
   * This method is not currently necessary, since we don't copy back
   * values in a debug interpreter until the thread has resumed.
   * 
   * @param name the name of the interpreter
   *
  public void notifyInterpreterAssignment(String name) {
    try {
      _mainJVM.notifyDebugInterpreterAssignment(name);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("notifyAssignment: " + re.toString());
    }
  }*/
  
  /**
   * Adds a classpath to the given interpreter.
   * @param interpreter the interpreter
   */
  protected void _addClasspath(JavaInterpreter interpreter) {
    for (int i=0; i < _classpath.size(); i++) {
      interpreter.addClassPath(_classpath.elementAt(i));
    }
  }
  
  /**
   * Adds a named DynamicJavaAdapter to the list of interpreters.
   * Presets it to contain the current accumulated classpath.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addJavaInterpreter(String name) {
    JavaInterpreter interpreter = new DynamicJavaAdapter();
    // Add each entry on the accumulated classpath
    _addClasspath(interpreter);
    addInterpreter(name, interpreter);
  }
  
  /**
   * Adds a named JavaDebugInterpreter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name) {
    JavaDebugInterpreter interpreter = new JavaDebugInterpreter(name);
    interpreter.setPrivateAccessible(true);
    // Add each entry on the accumulated classpath
    _addClasspath(interpreter);
    addInterpreter(name, interpreter);
  }
  
  /**
   * Adds a named interpreter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @param interpreter the interpreter to add
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addInterpreter(String name, Interpreter interpreter) {
    if (_interpreters.containsKey(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a unique interpreter name");
    }
    _interpreters.put(name, new InterpreterData(interpreter));
  }
  
  /**
   * Removes the interpreter with the given name, if it exists.
   * @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) {
    _interpreters.remove(name);
  }
  
  /**
   * Returns the interpreter (with metadata) with the given name
   * @param name the unique name of the desired interpreter
   * @throws IllegalArgumentException if no such named interpreter exists
   */
   InterpreterData getInterpreter(String name) {
     InterpreterData interpreter = _interpreters.get(name);
     if (interpreter != null) {
       return interpreter;
     }
     else {
       throw new IllegalArgumentException("Interpreter '" + name + "' does not exist.");
     }
   }
   
   /**
    * Returns the Java interpreter with the given name
    * @param name the unique name of the desired interpreter
    * @throws IllegalArgumentException if no such named interpreter exists, or if
    * the named interpreter is not a Java interpreter
    */
   public JavaInterpreter getJavaInterpreter(String name) {
     if( printMessages ) System.err.println("Getting interpreter data");
     InterpreterData interpreterData = getInterpreter(name);
     if( printMessages ) System.out.println("Getting interpreter instance");
     Interpreter interpreter = interpreterData.getInterpreter();
     if( printMessages ) System.out.println("returning");

     if (interpreter instanceof JavaInterpreter) {
       return (JavaInterpreter) interpreter;
     }
     else {
       throw new IllegalArgumentException("Interpreter '" + name + "' is not a JavaInterpreter.");
     }
   }

  /**
   * Sets the current interpreter to be the one specified by the given name
   * @param name the unique name of the interpreter to set active
   * @return Whether the new interpreter is currently in progress
   * with an interaction
   */
   public boolean setActiveInterpreter(String name) {
     _activeInterpreter = getInterpreter(name);
     return _activeInterpreter.isInProgress();
   }
  
  /**
   * Sets the default interpreter to be active.
   * @return Whether the new interpreter is currently in progress
   * with an interaction
   */
  public boolean setToDefaultInterpreter() {
    _activeInterpreter = _defaultInterpreter;
    return _activeInterpreter.isInProgress();
  }
  
  /**
   * Gets the hashtable containing the named interpreters.  Package private
   * for testing purposes.
   * @return said hashtable
   */
  Hashtable<String,InterpreterData> getInterpreters() {
    return _interpreters;
  }
  
  /**
   * Returns the current active interpreter.  Package private; for tests only.
   */
  Interpreter getActiveInterpreter() {
    return _activeInterpreter.getInterpreter();
  }


  /**
   * Gets the stack trace from the given exception, stripping off
   * the bottom parts of the trace that are internal to the interpreter.
   * This would be much easier to do in JDK 1.4, since you can get the
   * stack trace frames directly, instead of having to parse this!
   */
  public static String getStackTrace(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));

    //_dialog("before creating reader");
    BufferedReader reader
      = new BufferedReader(new StringReader(writer.toString()));

    //_dialog("after creating reader");
    LinkedList traceItems = new LinkedList();
    try {
      // we will generate list of trace items
      // skip the first one since it's just the message
      //_dialog("before first readLine");
      reader.readLine();
      //_dialog("after first readLine");

      String s;
      while ((s = reader.readLine()) != null) {
        //_dialog("read: " + s);
        traceItems.add(s);
      }
    }
    catch (IOException ioe) {
      return "Unable to get stack trace";
    }

    // OK, now we crop off everything after the first "koala.dynamicjava." or
    //  "edu.rice.cs.drjava.", if there is one.
    
    //  First, find the index of an occurrence.
    int index = -1;
    for (int i=0; i < traceItems.size(); i++) {
      String item = (String) traceItems.get(i);
      item = item.trim();
      if (item.startsWith("at edu.rice.cs.drjava.") ||
          item.startsWith("at koala.dynamicjava."))
      {
        index = i;
        break;
      }
    }
    
    // Now crop off the rest
    if (index > -1) {
      while (traceItems.size() > index) {
        traceItems.removeLast();
      }
    }

    // Last check: See if there are no items left. If there are none,
    // put one in to say it happened at top-level.
    if (traceItems.isEmpty()) {
      traceItems.add(EMPTY_TRACE_TEXT);
    }
    

    // OK, now rebuild string
    StringBuffer buf = new StringBuffer();
    ListIterator itor = traceItems.listIterator();
    boolean first = true;
    while (itor.hasNext()) {
      if (first) {
        first = false;
      }
      else {
        buf.append("\n");
      }

      buf.append("  " + ((String) itor.next()).trim());
    }

    return buf.toString();
  }
  
  // ---------- Java-specific methods ----------

  /**
   * Adds the given path to the classpath shared by ALL Java interpreters.
   * This method <b>cannot</b> take multiple paths separated by
   * a path separator; it must be called separately for each path.
   * Only unique paths are added.
   * @param s Entry to add to the accumulated classpath
   */
  public void addClassPath(String s) {
    if (_classpath.contains(s)) {
      return;
    }
    
    //_dialog("add classpath: " + s);
    if (_classpath.contains(s)) {
      // Don't add it again
      return;
    }
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addClassPath(s);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addClassPath(s);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classpath.addElement(s);
  }
  
  /**
   * Returns a copy of the list of unique entries on the classpath.
   */
  public Vector<String> getAugmentedClasspath() {
    return _classpath;
  }
  
  /**
   * Returns the accumulated classpath in use by all Java interpreters,
   * in the form of a path-separator delimited string.
   */
  public String getClasspathString() {
    StringBuffer cp = new StringBuffer();
    for (int i=0; i < _classpath.size(); i++) {
      cp.append(_classpath.elementAt(i));
      cp.append(System.getProperty("path.separator"));
    }
    return cp.toString();
  }

  /**
   * Sets the package scope for the current active interpreter,
   * if it is a JavaInterpreter.
   */
  public void setPackageScope(String s) {
    Interpreter active = _activeInterpreter.getInterpreter();
    if (active instanceof JavaInterpreter) {
      ((JavaInterpreter)active).setPackageScope(s);
    }
  }
  
  /**
   * @param show Whether to show a message if a reset operation fails.
   */
  public void setShowMessageOnResetFailure(boolean show) {
    _messageOnResetFailure = show;
  }
  
  /**
   * This method is called if the interpreterJVM cannot
   * be exited (likely because of its having a
   * security manager)
   */
  protected void quitFailed(Throwable th) {
    if (_messageOnResetFailure) {
      String msg = "The interactions pane could not be reset:\n" + th;
      javax.swing.JOptionPane.showMessageDialog(null, msg);
    }
    
    try {
      _mainJVM.quitFailed(th);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("quitFailed: " + re.toString());
    }
  }
  
  
  // ---------- JUnit methods ----------
  
  /**
   * Runs a JUnit Test class in the Interpreter JVM.
   * @param className Name of the TestCase class
   * @param fileName Name of the file for the TestCase class
   */
  public void runTestSuite(String className, String fileName) throws RemoteException {
    _junitTestManager.runTest(className, fileName);
  }
  
  /**
   * Notifies the main JVM if JUnit is invoked on a non TestCase class.
   */
  public void nonTestCase() {
    try {
      _mainJVM.nonTestCase();
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("nonTestCase: " + re.toString());
    }
  }
  
  /**
   * Notifies that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) {
    try {
      _mainJVM.testSuiteStarted(numTests);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("testSuiteStarted: " + re.toString());
    }
  }
  
  /**
   * Notifies that a particular test has started.
   * @param testName The name of the test being started.
   */
  public void testStarted(String testName) {
    try {
      _mainJVM.testStarted(testName);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("testStarted" + re.toString());
    }
  }
  
  /**
   * Notifies that a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {
    try {
      _mainJVM.testEnded(testName, wasSuccessful, causedError);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("testEnded: " + re.toString());
    }
  }
  
  /**
   * Notifies that a full suite of tests has finished running.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteFinished(JUnitError[] errors) {
    try {
      _mainJVM.testSuiteEnded(errors);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("testSuiteFinished: " + re.toString());
    }
  }

}


/**
 * Bookkeeping class to maintain meta information about each interpreter,
 * such as whether it is currently in progress.
 */
class InterpreterData {
  protected final Interpreter _interpreter;
  protected boolean _inProgress;
  
  InterpreterData(Interpreter interpreter) {
    _interpreter = interpreter;
    _inProgress = false;
  }
  
  /** Gets the interpreter. */
  public Interpreter getInterpreter() {
    return _interpreter;
  }
  
  /** Returns whether this interpreter is currently in progress with an interaction. */
  public boolean isInProgress() {
    return _inProgress;
  }
  
  /** Sets whether this interpreter is currently in progress. */
  public void setInProgress(boolean inProgress) {
    _inProgress = inProgress;
  }
}