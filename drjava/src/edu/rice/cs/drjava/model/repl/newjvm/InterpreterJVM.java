/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;
import java.io.*;

import java.rmi.*;
import java.net.URL;
import java.net.MalformedURLException;


// NOTE: Do NOT import/use the config framework in this class!
//  (This class runs in a different JVM, and will not share the config object)


import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.InputStreamRedirector;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.newjvm.*;

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.junit.JUnitTestManager;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.ClassPathEntry;

// For Windows focus fix
import javax.swing.JDialog;

import koala.dynamicjava.parser.wrapper.*;
import koala.dynamicjava.parser.*;

/** This is the main class for the interpreter JVM.  All public methods except those involving remote calls (callbacks) 
 *  synchronized (unless synchronization has no effect).  This class is loaded in the Interpreter JVM, not the Main JVM. 
 *  (Do not use DrJava's config framework here.)
 *  <p>
 *  Note that this class is specific to DynamicJava. It must be refactored to accommodate other interpreters.
 *  @version $Id$
 */
public class InterpreterJVM extends AbstractSlaveJVM implements InterpreterJVMRemoteI, JUnitModelCallback {

  /** Singleton instance of this class. */
  public static final InterpreterJVM ONLY = new InterpreterJVM();
  
  private static final Log _log = new Log("MasterSlave.txt", false);
  private static final boolean printMessages = true;
  
  /** String to append to error messages when no stack trace is available. */
  public static final String EMPTY_TRACE_TEXT = "";
    
  /** Metadata encapsulating the default interpreter. */
  private final InterpreterData _defaultInterpreter;
  
  /** Maps names to interpreters with metadata. */
  private final Hashtable<String,InterpreterData> _interpreters;
 
  /** The currently accumulated classpath for all Java interpreters.  List contains unqiue entries. */
  private final ClassPathVector _classPath;
  
  /** Responsible for running JUnit tests in this JVM. */
  private final JUnitTestManager _junitTestManager;
  
  /** manages the classpath for all of DrJava */
  private final ClassPathManager _classPathManager;
  
  /** Remote reference to the MainJVM class in DrJava's primary JVM.  Assigned ONLY once. */
  private volatile MainJVMRemoteI _mainJVM;

  /** The current interpreter. */
  private volatile InterpreterData _activeInterpreter;
  
//  /** Busy flag.  Used to prevent multiple interpretations from running simultaneously. */
//  private volatile boolean interpretationInProgress = false;
  
  /** Interactions processor, currently a pre-processor **/
  //  private InteractionsProcessorI _interactionsProcessor;
  
  /** Whether to display an error message if a reset fails. */
  private volatile boolean _messageOnResetFailure;
  
  /** Private constructor; use the singleton ONLY instance. */
  private InterpreterJVM() {

    _classPath = new ClassPathVector();
    _classPathManager = new ClassPathManager();
    _defaultInterpreter = new InterpreterData(new DynamicJavaAdapter(_classPathManager));
    _interpreters = new Hashtable<String,InterpreterData>();
    _junitTestManager = new JUnitTestManager(this);
    _messageOnResetFailure = true;
    
    //    _interactionsProcessor = new InteractionsProcessor();
    
    _quitSlaveThreadName = "Reset Interactions Thread";
    _pollMasterThreadName = "Poll DrJava Thread";
    _activeInterpreter = _defaultInterpreter;
    
    try { _activeInterpreter.getInterpreter().interpret("0"); }
    catch (ExceptionReturnedException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }

  private static void _dialog(String s) {
    //javax.swing.JOptionPane.showMessageDialog(null, s);
    _log.log(s);
  }
  
  /** Actions to perform when this JVM is started (through its superclass, AbstractSlaveJVM).  Contract from superclass
   *  mandates that this code does not synchronized on this across a remote call.  This method has no synchronization
   *  because it can only be called once (part of the superclass contract) and _mainJVM is only assigned (once!) here. */
  protected void handleStart(MasterRemote mainJVM) {
    //_dialog("handleStart");
    _mainJVM = (MainJVMRemoteI) mainJVM;
    
    // redirect stdin
    System.setIn(new InputStreamRedirector() {
      protected String _getInput() {  // NOT synchronized on InterpreterJVM.this.  _mainJVM is immutable.
        try { return _mainJVM.getConsoleInput(); }
        catch(RemoteException re) {
          // blow up if no MainJVM found
          _log.log("System.in: " + re.toString());
          throw new IllegalStateException("Main JVM can't be reached for input.\n" + re);
        }
      }
    });
    
    // redirect stdout
    System.setOut(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) { // NOT synchronized on InterpreterJVM.this.  _mainJVM is immutable.
        try {
          //_log.logTime("out.print: " + s);
          _mainJVM.systemOutPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.log("System.out: " + re.toString());
        }
      }
    }));
    
    // redirect stderr
    System.setErr(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) { // NOT synchronized on InterpreterJVM.this.  _mainJVM is immutable.
        try {
          //_log.logTime("err.print: " + s);
          _mainJVM.systemErrPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
          _log.log("System.err: " + re.toString());
        }
      }
    }));
    
    /* On Windows, any frame or dialog opened from Interactions pane will appear *behind* DrJava's frame, unless a 
     * previous frame or dialog is shown here.  Not sure what the difference is, but this hack seems to work.  (I'd
     * be happy to find a better solution, though.)  Only necessary on Windows, since frames and dialogs on other 
     * platforms appear correctly in front of DrJava. */
    if (PlatformFactory.ONLY.isWindowsPlatform()) {
      JDialog d = new JDialog();
      d.setSize(0,0);
      d.setVisible(true);
      d.setVisible(false);
    }
    //_dialog("interpreter JVM started");
  }

  /** Interprets the given string of source code in the active interpreter. The result is returned to MainJVM via 
   *  the interpretResult method.
   *  @param s Source code to interpret.
   */
  public void interpret(String s) { interpret(s, _activeInterpreter); }
  
  /** Interprets the given string of source code with the given interpreter. The result is returned to MainJVM via 
   *  the interpretResult method.
   *  @param s Source code to interpret.
   *  @param interpreterName Name of the interpreter to use
   *  @throws IllegalArgumentException if the named interpreter does not exist
   */
  public void interpret(String s, String interpreterName) { interpret(s, getInterpreter(interpreterName));  }
  
  /** Interprets the given string of source code with the given interpreter.  The result is returned to MainJVM via
   *  the interpretResult method.  Not synchronized!
   *  @param input Source code to interpret.
   *  @param interpreter The interpreter (plus metadata) to use
   */
  public void interpret(final String input, final InterpreterData interpreter) {
    _log.log(this + ".interpret(" + input + ") called");
    try {
      synchronized(interpreter) { 
        if (interpreter.inProgress()) {
            _mainJVM.interpretResult(new InterpreterBusy());
          return;
        }
//      interpretationInProgress = true; 
      interpreter.setInProgress(true);  // records that a given interpreter is in progress (used by debugger?)
      }
      // The following code is NOT synchronized on this. Mutual exclusion is guaranteed by preceding synchronized block.
//        Utilities.showDebug("InterpreterJVM.interpret(" + input + ", ...) called");
      Thread thread = new Thread("interpret thread: " + input) {
        public void run() {
          String s = input;
          try {  // Delimiting a catch for RemoteExceptions that might be thrown in catch clauses of enclosed try
            try {
              _log.log("Interpreter thread for " + input + " has started");
//              _dialog("to interp: " + s);
              
//            Utilities.showDebug("Preparing to invoke interpret method on " + s);
              Object result = interpreter.getInterpreter().interpret(s);
              String resultString = String.valueOf(result);
//            Utilities.showDebug("Result string is: " + resultString);
              
              if (result == Interpreter.NO_RESULT) {
                //return new VoidResult();
                //_dialog("void interp ret: " + resultString);
                _mainJVM.interpretResult(new VoidResult());
              }
              else {
                // we use String.valueOf because it deals with result = null!
                //_dialog("about to tell main result was " + resultString);
                //return new ValueResult(resultString);
                String style = InteractionsDocument.OBJECT_RETURN_STYLE;
                if (result instanceof String) {
                  style = InteractionsDocument.STRING_RETURN_STYLE;
                  //Single quotes have already been added to chars by now, so they are read as strings
                  String possibleChar = (String)result;
                  
                  if (possibleChar.startsWith("\'") && possibleChar.endsWith("\'") && possibleChar.length()==3)
                    style = InteractionsDocument.CHARACTER_RETURN_STYLE;                
                }
                if (result instanceof Number) style = InteractionsDocument.NUMBER_RETURN_STYLE;
                _mainJVM.interpretResult(new ValueResult(resultString, style));
              }
            }
            catch (ExceptionReturnedException e) {
              Throwable t = e.getContainedException();
//            Utilities.showStackTrace(t);
              _dialog("interp exception: " + t);
              // TODO: replace the following if ladder by dynamic dispatch.  Create a visitor for DynamicJava errors?
              if (t instanceof ParseException)
                _mainJVM.interpretResult(new SyntaxErrorResult((ParseException) t, input));
              else if (t instanceof TokenMgrError)
                _mainJVM.interpretResult(new SyntaxErrorResult((TokenMgrError) t, input));
              else if (t instanceof ParseError)
                _mainJVM.interpretResult(new SyntaxErrorResult((ParseError) t, input));
              else {
                //Other exceptions are non lexical/parse related exceptions. These include arithmetic exceptions, 
                //wrong version exceptions, etc.
                
                _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(), t.getMessage(),
                                                             InterpreterJVM.getStackTrace(t), null));
              }                                                                                                                                        
            }
            catch (Throwable t) {
              // A user's toString method might throw anything, so we need to be careful
              _dialog("irregular interp exception: " + t);
//            Utilities.showStackTrace(t);
              String shortMsg = null;
              if ((t instanceof ParseError) &&  ((ParseError) t).getParseException() != null) 
                shortMsg = ((ParseError) t).getMessage(); // in this case, getMessage is equivalent to getShortMessage
              _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(), t.getMessage(),
                                                           InterpreterJVM.getStackTrace(t), shortMsg));
            }
          }
          catch(RemoteException re) { /* MainJVM no longer accessible.  Cannot recover. */  
            _log.log("MainJVM.interpret threw " + re.toString());
          }
        }
      }; // end of Thread definition
      
      thread.setDaemon(true);
      thread.start();
    } // end of interpretation block including synchronized prelude 
    catch(RemoteException re) { /* MainJVM not accessible.  Cannot recover. */  
      _log.log("MainJVM.interpret threw" + re.toString());
    }
    finally { // fields are volatile so no synchronization is necessary
//      interpretationInProgress = false;
      interpreter.setInProgress(false); 
    }
  }
        
  private static String _processReturnValue(Object o) {
    if (o instanceof String) return "\"" + o + "\"";
    if (o instanceof Character) return "'" + o + "'";
    return o.toString();
  }
  
  /** Gets the string representation of the value of a variable in the current interpreter.
   *  @param var the name of the variable
   *  @return null if the variable is not defined, "null" if the value is null, or else its string representation
   */
  public synchronized String getVariableToString(String var) throws RemoteException {
    // Add to the default interpreter, if it is a JavaInterpreter
    Interpreter i = _activeInterpreter.getInterpreter();
    if (i instanceof JavaInterpreter) {
      try {
        Object value = ((JavaInterpreter)i).getVariable(var);
        if (value == null)  return "null";
        if (value instanceof koala.dynamicjava.interpreter.UninitializedObject) return null;
        return _processReturnValue(value);
      }
      catch (IllegalStateException e) { return null; }  // variable was not defined
    }
    return null;
  }
  
  /** Gets the class name of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public synchronized String getVariableClassName(String var) throws RemoteException {
    // Add to the default interpreter, if it is a JavaInterpreter
    Interpreter i = _activeInterpreter.getInterpreter();
    if (i instanceof JavaInterpreter) {
      try {
        Class c = ((JavaInterpreter)i).getVariableClass(var);
        if (c == null) return "null";
        else return c.getName();
      }
      catch (IllegalStateException e) {
        // variable was not defined
        return null;
      }
    }
    else return null;
  }
  
  /** Adds a named DynamicJavaAdapter to list of interpreters. Presets it to contain the current accumulated classpath.
   *  @param name the unique name for the interpreter
   *  @throws IllegalArgumentException if the name is not unique
   */
  public synchronized void addJavaInterpreter(String name) {
    JavaInterpreter interpreter = new DynamicJavaAdapter(_classPathManager);
    // Add each entry on the accumulated classpath
    _updateInterpreterClassPath(interpreter);
    addInterpreter(name, interpreter);
  }
  
  /** Adds a named JavaDebugInterpreter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @param className the fully qualified class name of the class the debug interpreter is in
   *  @throws IllegalArgumentException if the name is not unique
   */
  public synchronized void addDebugInterpreter(String name, String className) {
    JavaDebugInterpreter interpreter = new JavaDebugInterpreter(name, className);
    interpreter.setPrivateAccessible(true);
    // Add each entry on the accumulated classpath
    _updateInterpreterClassPath(interpreter);
    addInterpreter(name, interpreter);
  }
  
  /** Adds a named interpreter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @param interpreter the interpreter to add
   *  @throws IllegalArgumentException if the name is not unique
   */
  public synchronized void addInterpreter(String name, Interpreter interpreter) {
    if (_interpreters.containsKey(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a unique interpreter name");
    }
    _interpreters.put(name, new InterpreterData(interpreter));
  }
  
  /** Removes the interpreter with the given name, if it exists.  Unsynchronized because _interpreters is immutable
   *  and its methods are thread-safe.
   *  @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) { _interpreters.remove(name); }
  
  /** Returns the interpreter (with metadata) with the given name
   *  @param name the unique name of the desired interpreter
   *  @throws IllegalArgumentException if no such named interpreter exists
   */
  InterpreterData getInterpreter(String name) {
    InterpreterData interpreter = _interpreters.get(name);
    if (interpreter != null) return interpreter;
    else throw new IllegalArgumentException("Interpreter '" + name + "' does not exist.");
  }
  
  /** Returns the Java interpreter with the given name
   *  @param name the unique name of the desired interpreter
   *  @throws IllegalArgumentException if no such named interpreter exists, or if the named interpreter is not a Java
   *          interpreter
   */
  public synchronized JavaInterpreter getJavaInterpreter(String name) {
    if (printMessages) System.out.println("Getting interpreter data");
    InterpreterData interpreterData = getInterpreter(name);
    if (printMessages) System.out.println("Getting interpreter instance");
    Interpreter interpreter = interpreterData.getInterpreter();
    if (printMessages) System.out.println("returning");
    
    if (interpreter instanceof JavaInterpreter) return (JavaInterpreter) interpreter;
    else {
      throw new IllegalArgumentException("Interpreter '" + name + "' is not a JavaInterpreter.");
    }
  }
  
  
  /** Sets the current interpreter to be the one specified by the given name
   *  @param name the unique name of the interpreter to set active
   *  @return Whether the new interpreter is currently in progress with an interaction
   */
  public synchronized boolean setActiveInterpreter(String name) {
    _activeInterpreter = getInterpreter(name);
    return _activeInterpreter.inProgress();
  }
  
  /** Sets the default interpreter to be active.
   *  @return Whether the new interpreter is currently in progress with an interaction
   */
  public synchronized boolean setToDefaultInterpreter() {
    _activeInterpreter = _defaultInterpreter;
    return _activeInterpreter.inProgress();
  }
  
  /** Gets the hashtable containing the named interpreters.  Package private for testing purposes.
   *  @return said hashtable
   */
  Hashtable<String,InterpreterData> getInterpreters() { return _interpreters; }
  
  /** Returns the current active interpreter.  Package private; for tests only. */
  Interpreter getActiveInterpreter() { return _activeInterpreter.getInterpreter(); }
  
  /** Gets the stack trace from the given exception, stripping off the bottom parts of the trace that are internal 
   *  to the interpreter.  This would be much easier to do in JDK 1.4, since you can get the stack trace frames 
   *  directly, instead of having to parse this!  TODO: revise this code to use the JDK 1.4+ API.
   */
  public static String getStackTrace(Throwable t) {
    //_dialog("before creating reader");
    BufferedReader reader = new BufferedReader(new StringReader(StringOps.getStackTrace(t)));
    
    //_dialog("after creating reader");
    LinkedList<String> traceItems = new LinkedList<String>();
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
    
    // OK, now we crop off everything after the first "koala.dynamicjava." or "edu.rice.cs.drjava.", if there is one.
    
    //  First, find the index of an occurrence.
    int index = -1;
    for (int i = 0; i < traceItems.size(); i++) {
      String item = traceItems.get(i);
      item = item.trim();
      if (item.startsWith("at edu.rice.cs.drjava.") || item.startsWith("at koala.dynamicjava.")) {
        index = i;
        break;
      }
    }
    
    // Now crop off the rest
    if (index > -1) {
      while (traceItems.size() > index) traceItems.removeLast();
    }
    
    // Last check: See if there are no items left. If there are none, put one in to say it happened at top-level.
    if (traceItems.isEmpty()) traceItems.add(EMPTY_TRACE_TEXT);
    
    // OK, now rebuild string
    StringBuffer buf = new StringBuffer();
    ListIterator itor = traceItems.listIterator();
    String newLine = System.getProperty("line.separator");
    boolean first = true;
    while (itor.hasNext()) {
      if (first) first = false; else buf.append(newLine);

      buf.append("  " + ((String) itor.next()).trim());
    }
    
    return buf.toString();
  }
  
  // ---------- Java-specific methods ----------
  
  /** Sets the package scope for the current active interpreter, if it is a JavaInterpreter. */
  public void setPackageScope(String s) {
    Interpreter active = _activeInterpreter.getInterpreter();
    if (active instanceof JavaInterpreter) {
      ((JavaInterpreter)active).setPackageScope(s);
    }
  }
  
  /** @param show Whether to show a message if a reset operation fails. */
  public void setShowMessageOnResetFailure(boolean show) { _messageOnResetFailure = show; }
  
  /** This method is called if the interpreterJVM cannot be exited (likely because of a modified security manager. */
  protected void quitFailed(Throwable th) {  // NOT synchronized
    if (_messageOnResetFailure) {
      String msg = "The interactions pane could not be reset:\n" + th;
      javax.swing.JOptionPane.showMessageDialog(null, msg);
    }
    
    try { _mainJVM.quitFailed(th); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("quitFailed: " + re.toString());
    }
  }
  
  /** Sets the interpreter to allow access to private members. */
  public synchronized void setPrivateAccessible(boolean allow) {
    Interpreter active = _activeInterpreter.getInterpreter();
    if (active instanceof JavaInterpreter) {
      ((JavaInterpreter)active).setPrivateAccessible(allow);
    }
  } 
  
  // ---------- JUnit methods ----------
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCases classes (by 
   *  loading them).  Unsynchronized because it contains a remote call and does not involve mutable local state.
   *  @param classNames the class names to run in a test
   *  @param files the associated file
   *  @return the class names that are actually test cases
   */
  public List<String> findTestClasses(List<String> classNames, List<File> files) throws RemoteException {
    // new ScrollableDialog(null, "InterpterJVM.findTestClasses invoked", "", "").show();
    return _junitTestManager.findTestClasses(classNames, files);
  }
  
  /** Runs JUnit test suite already cached in the Interpreter JVM.  Unsynchronized because it contains a remote call
   *  and does not involve mutable local state.
   *  @return false if no test suite is cached; true otherwise
   */
  public boolean runTestSuite() throws RemoteException {
    // new ScrollableDialog(null, "InterpreterJVM.runTestSuite() called!", "", "").show();
    return _junitTestManager.runTestSuite();
  }
  
  /** Notifies Main JVM that JUnit has been invoked on a non TestCase class.  Unsynchronized because it contains a 
   *  remote call and does not involve mutable local state.
   *  @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) {
    try { _mainJVM.nonTestCase(isTestAll); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("nonTestCase: " + re.toString());
    }
  }
  
  /** Notifies the main JVM that JUnitTestManager has encountered an illegal class file.  Unsynchronized because it 
   *  contains a remote call and does not involve mutable local state.
   *  @param e the ClassFileError object describing the error on loading the file
   */
  public void classFileError(ClassFileError e) {
    try { _mainJVM.classFileError(e); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("classFileError: " + re.toString());
    }
  }
  
  /** Notifies that a suite of tests has started running.  Unsynchronized because it contains a remote call and does
   *  not involve mutable local state.
   *  @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) {
    try { _mainJVM.testSuiteStarted(numTests); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testSuiteStarted: " + re.toString());
    }
  }
  
  /** Notifies that a particular test has started.  Unsynchronized because it contains a remote call and does not
   *  involve mutable local state.
   *  @param testName The name of the test being started.
   */
  public void testStarted(String testName) {
    try { _mainJVM.testStarted(testName); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testStarted" + re.toString());
    }
  }
  
  /** Notifies that a particular test has ended.  Unsynchronized because it contains a remote call.
   *  @param testName The name of the test that has ended.
   *  @param wasSuccessful Whether the test passed or not.
   *  @param causedError If not successful, whether the test caused an error or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {
    try { _mainJVM.testEnded(testName, wasSuccessful, causedError); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testEnded: " + re.toString());
    }
  }
  
  /** Notifies that a full suite of tests has finished running.  Unsynchronized because it contains a remote call
   *  and does not involve mutable local state.
   *  @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
    try { _mainJVM.testSuiteEnded(errors); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("testSuiteFinished: " + re.toString());
    }
  }
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.  Unsynchronized because it 
   *  contains a remote call and does not involve mutable local state.
   *  @param className the name of the class for which we want to find the file
   *  @return the file associated with the given class
   */
  public File getFileForClassName(String className) {
    try { return _mainJVM.getFileForClassName(className); }
    catch (RemoteException re) {
      // nothing to do
      _log.log("getFileForClassName: " + re.toString());
      return null;
    }
  }
  
  public void junitJVMReady() { }
  
  //////////////////////////////////////////////////////////////
  // ALL functions regarding classpath
  //////////////////////////////////////////////////////////////
  
  /** Adds a classpath to the given interpreter.  assumes that lock on this is held.
   *  @param interpreter the interpreter
   */
  protected /* synchronized */ void _updateInterpreterClassPath(JavaInterpreter interpreter) {
    
    for (ClassPathEntry e: _classPathManager.getProjectCP())
      interpreter.addProjectClassPath(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getBuildDirectoryCP())
      interpreter.addBuildDirectoryClassPath(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getProjectFilesCP())
      interpreter.addProjectFilesClassPath(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getExternalFilesCP())
      interpreter.addExternalFilesClassPath(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getExtraCP())
      interpreter.addExtraClassPath(e.getEntry());
  }
  
  /** Adds the given path to the classpath shared by ALL Java interpreters. This method <b>cannot</b> take multiple
   *  paths separated by a path separator; it must be called separately for each path.  Only unique paths are added.
   *  @param s Entry to add to the accumulated classpath
   */
  public synchronized void addExtraClassPath(URL s) {
    //_dialog("add classpath: " + s);
    if (_classPath.contains(s)) return;    // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addExtraClassPath(s);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addExtraClassPath(s);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(s);
  }
 
  /** Adds the given path to the classpath shared by ALL Java interpreters. This method <b>cannot</b> take multiple 
   *  paths separated by a path separator; it must be called separately for each path.  Only unique paths are added.
   *  @param s Entry to add to the accumulated classpath
   */
  public synchronized void addProjectClassPath(URL s) {
    //_dialog("add classpath: " + s);
    if (_classPath.contains(s)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addProjectClassPath(s);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addProjectClassPath(s);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(s);
  }
 
  /** Adds the given path to the classpath shared by ALL Java interpreters. This method <b>cannot</b> take multiple 
   *  paths separated by a path separator; it must be called separately for each path.  Only unique paths are added.
   *  @param s Entry to add to the accumulated classpath
   */
  public synchronized void addBuildDirectoryClassPath(URL s) {
    //_dialog("add classpath: " + s);
    if (_classPath.contains(s)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addBuildDirectoryClassPath(s);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addBuildDirectoryClassPath(s);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(s);
  }
  
 
  /** Adds the given path to the classpath shared by ALL Java interpreters. This method <b>cannot</b> take multiple 
   *  paths separated by a path separator; it must be called separately for each path.  Only unique paths are added.
   *  @param s Entry to add to the accumulated classpath
   */
  public synchronized void addProjectFilesClassPath(URL s) {
    //_dialog("add classpath: " + s);
    if (_classPath.contains(s)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addProjectFilesClassPath(s);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addProjectFilesClassPath(s);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(s);
  }
 
  /** Adds the given path to the classpath shared by ALL Java interpreters. This method <b>cannot</b> take multiple
   *  paths separated by a path separator; it must be called separately for each path. Only unique paths are added.
   * @param s Entry to add to the accumulated classpath
   */
  public synchronized void addExternalFilesClassPath(URL s) {
    //_dialog("add classpath: " + s);
    if (_classPath.contains(s)) return;  // Don't add it again
    
    // Add to the default interpreter, if it is a JavaInterpreter
    if (_defaultInterpreter.getInterpreter() instanceof JavaInterpreter) {
      ((JavaInterpreter)_defaultInterpreter.getInterpreter()).addExternalFilesClassPath(s);
    }
    
    // Add to any named JavaInterpreters to be consistent
    Enumeration<InterpreterData> interpreters = _interpreters.elements();
    while (interpreters.hasMoreElements()) {
      Interpreter interpreter = interpreters.nextElement().getInterpreter();
      if (interpreter instanceof JavaInterpreter) {
        ((JavaInterpreter)interpreter).addExternalFilesClassPath(s);
      }
    }
    
    // Keep this entry on the accumulated classpath
    _classPath.add(s);
  }
  
  /** Returns a copy of the list of unique entries on the classpath.
   *  @return a vector of strings so that RMI doesn't have to serialize the URL object. Serializing URL objects fails
   *  when using jsr14.
   */
  public synchronized Vector<String> getAugmentedClassPath() {
    Vector<String> ret = new Vector<String>();

    for (ClassPathEntry e: _classPathManager.getProjectCP())  ret.add(e.getEntry().toString());

    for (ClassPathEntry e: _classPathManager.getBuildDirectoryCP()) 
      ret.add(e.getEntry().toString());
    
    for (ClassPathEntry e: _classPathManager.getProjectFilesCP())
      ret.add(e.getEntry().toString());

    for (ClassPathEntry e: _classPathManager.getExternalFilesCP())
      ret.add(e.getEntry().toString());

    for (ClassPathEntry e: _classPathManager.getExtraCP())
      ret.add(e.getEntry().toString());

    return ret;
  }
  
  //// The following methods convert strings received
  //// from RMI to URL objects since URL objects cannot
  //// be successfully serialized when using JSR14.
  
  public void addExtraClassPath(String s) {
    try { addExtraClassPath(new URL(s)); } 
    catch(MalformedURLException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }
  
  public void addProjectClassPath(String s) {
    try { addProjectClassPath(new URL(s)); } 
    catch(MalformedURLException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }
  
  public void addBuildDirectoryClassPath(String s) {
    try { addBuildDirectoryClassPath(new URL(s)); } 
    catch(MalformedURLException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }
  
  public void addProjectFilesClassPath(String s) {
    try { addProjectFilesClassPath(new URL(s)); } 
    catch(MalformedURLException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }
  
  public void addExternalFilesClassPath(String s) { 
    try { addExternalFilesClassPath(new URL(s)); } 
    catch(MalformedURLException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }
  
  /** Returns the vector of URL objects as a ClasspathVector which has an intelligent toString().  The toString()
   *  method of ClasspathVector is usable as the classpath command line argument for java, javac javadoc, and junit.
   *  @return a vector of URLs with an intelligent toString();
   */
  public synchronized ClassPathVector getClassPath() {
    ClassPathVector ret = new ClassPathVector();
    
    for (ClassPathEntry e: _classPathManager.getProjectCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getBuildDirectoryCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getProjectFilesCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getExternalFilesCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: _classPathManager.getExtraCP()) ret.add(e.getEntry());
    
    return ret;
  } 
}

/** Bookkeeping class to maintain information about each interpreter, such as whether it is currently in progress. */
class InterpreterData {
  protected final Interpreter _interpreter;
  protected volatile boolean _inProgress;
  
  InterpreterData(Interpreter interpreter) {
    _interpreter = interpreter;
    _inProgress = false;
  }
  
  // The following methods do not need to be synchronized because they access or set volatile fields.
  
  /** Gets the interpreter. */
  public Interpreter getInterpreter() { return _interpreter; }
  
  /** Returns whether this interpreter is currently in progress with an interaction. */
  public boolean inProgress() { return _inProgress; }
  
  /** Sets whether this interpreter is currently in progress. */
  public void setInProgress(boolean inProgress) { _inProgress = inProgress; }
}
