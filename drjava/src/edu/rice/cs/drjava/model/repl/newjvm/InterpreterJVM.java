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

import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OutputStreamRedirector;
import edu.rice.cs.util.InputStreamRedirector;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.classloader.ClassFileError;
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

/** This is the main class for the interpreter JVM. Note that this class is specific to using DynamicJava. It would need
 *  to be subclassed to use with another interpreter. (Really, there would need to be an abstract base class, but since 
 *  we don't need it yet I'm not making one.)
 *  This class is loaded in the Interpreter JVM, not the Main JVM. (Do not use DrJava's config framework here.)
 *  @version $Id$
 */
public class InterpreterJVM extends AbstractSlaveJVM implements InterpreterJVMRemoteI, JUnitModelCallback {
  
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
  
  /** The current interpreter. */
  private InterpreterData _activeInterpreter;
  
  /** The currently accumulated classpath for all Java interpreters.  List contains unqiue entries. */
  private ClassPathVector _classPath;
  
  /** Responsible for running JUnit tests in this JVM. */
  private JUnitTestManager _junitTestManager;
  
  /** manages the classpath for all of DrJava */
  ClassPathManager classPathManager;
  
  /** Interactions processor, currently a pre-processor **/
  //  private InteractionsProcessorI _interactionsProcessor;
  
  /** Whether to display an error message if a reset fails. */
  private boolean _messageOnResetFailure;
  
  /** Private constructor; use the singleton ONLY instance. */
  private InterpreterJVM() {
    _quitSlaveThreadName = "Reset Interactions Thread";
    _pollMasterThreadName = "Poll DrJava Thread";
    reset();
    //    _interactionsProcessor = new InteractionsProcessor();
    _messageOnResetFailure = true;
  }
  
  /** Resets this InterpreterJVM to its default state. */
  private void reset() {
    classPathManager = new ClassPathManager();
    _defaultInterpreter = new InterpreterData(new DynamicJavaAdapter(classPathManager));
    _activeInterpreter = _defaultInterpreter;
    _interpreters = new Hashtable<String,InterpreterData>();
    _classPath = new ClassPathVector();
    _junitTestManager = new JUnitTestManager(this);
    
    // do an interpretation to get the interpreter loaded fully
    try { _activeInterpreter.getInterpreter().interpret("0"); }
    catch (ExceptionReturnedException e) { throw new edu.rice.cs.util.UnexpectedException(e); }
  }
  
//  /** Updates the security manager in the slave JVM. */
//  public void enableSecurityManager() throws RemoteException {
//    edu.rice.cs.drjava.DrJava.enableSecurityManager();
//  }
//  
//  /** Updates the security manager in the slave JVM. */
//  public void disableSecurityManager() throws RemoteException {
//    edu.rice.cs.drjava.DrJava.disableSecurityManager();
//  }
  
  private static final Log _log = new Log("IntJVMLog", false);
  private static void _dialog(String s) {
    //javax.swing.JOptionPane.showMessageDialog(null, s);
    _log.logTime(s);
  }
  
  /** Actions to perform when this JVM is started (through its superclass, AbstractSlaveJVM). */
  protected void handleStart(MasterRemote mainJVM) {
    //_dialog("handleStart");
    _mainJVM = (MainJVMRemoteI) mainJVM;
    
//    // install special SecurityManager that blocks interpreted code from doing System.exit(...)
//    try { enableSecurityManager(); }
//    catch(RemoteException re) {
//       _log.logTime("PreventExitSecurityManager: " + re.toString());
//       throw new IllegalStateException("Slave JVM cannot install special SecurityManager.\n" + re);
//    }
    
    // redirect stdin
    System.setIn(new InputStreamRedirector() {
      protected String _getInput() {
        try { return _mainJVM.getConsoleInput(); }
        catch(RemoteException re) {
          // blow up if no MainJVM found
          _log.logTime("System.in: " + re.toString());
          throw new IllegalStateException("Main JVM can't be reached for input.\n" + re);
        }
      }
    });
    
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
  public void interpret(String s, String interpreterName) {
    interpret(s, getInterpreter(interpreterName));
  }
  
  /** Interprets the given string of source code with the given interpreter.  The result is returned to MainJVM via
   *  the interpretResult method.
   *  @param input Source code to interpret.
   *  @param interpreter The interpreter (plus metadata) to use
   */
  public void interpret(final String input, final InterpreterData interpreter) {
//    Utilities.showDebug("InterpreterJVM.interpret(" + input + ", ...) called");
    Thread thread = new Thread("interpret thread: " + input) {
      public void run() {
        String s = input;
        try {
          interpreter.setInProgress(true);
          try {
            _dialog("to interp: " + s);
            
            String s1 = s;  //_interactionsProcessor.preProcess(s);
//            Utilities.showDebug("Preparing to invoke interpret method on " + s1);
            Object result = interpreter.getInterpreter().interpret(s1);
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
              
              _mainJVM.interpretResult(new ExceptionResult(t.getClass().getName(),
                                                           t.getMessage(),
                                                           InterpreterJVM.getStackTrace(t),
                                                           null));
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
  
  private String _processReturnValue(Object o) {
    if (o instanceof String) return "\"" + o + "\"";
    if (o instanceof Character) return "'" + o + "'";
    return o.toString();
  }
  
  /** Gets the string representation of the value of a variable in the current interpreter.
   *  @param var the name of the variable
   *  @return null if the variable is not defined, "null" if the value is null, or else its string representation
   */
  public String getVariableToString(String var) throws RemoteException {
    // Add to the default interpreter, if it is a JavaInterpreter
    Interpreter i = _activeInterpreter.getInterpreter();
    if (i instanceof JavaInterpreter) {
      try {
        Object value = ((JavaInterpreter)i).getVariable(var);
        if (value == null)  return "null";
        if (value instanceof koala.dynamicjava.interpreter.UninitializedObject) return null;
        return _processReturnValue(value);
      }
      catch (IllegalStateException e) {
        // variable was not defined
        return null;
      }
    }
    return null;
  }
  
  /** Gets the class name of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public String getVariableClassName(String var) throws RemoteException {
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
  public void addJavaInterpreter(String name) {
    JavaInterpreter interpreter = new DynamicJavaAdapter(classPathManager);
    // Add each entry on the accumulated classpath
    _updateInterpreterClassPath(interpreter);
    addInterpreter(name, interpreter);
  }
  
  /** Adds a named JavaDebugInterpreter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @param className the fully qualified class name of the class the debug interpreter is in
   *  @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name, String className) {
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
  public void addInterpreter(String name, Interpreter interpreter) {
    if (_interpreters.containsKey(name)) {
      throw new IllegalArgumentException("'" + name + "' is not a unique interpreter name");
    }
    _interpreters.put(name, new InterpreterData(interpreter));
  }
  
  /** Removes the interpreter with the given name, if it exists.
   *  @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) {
    _interpreters.remove(name);
  }
  
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
  public JavaInterpreter getJavaInterpreter(String name) {
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
  public boolean setActiveInterpreter(String name) {
    _activeInterpreter = getInterpreter(name);
    return _activeInterpreter.inProgress();
  }
  
  /** Sets the default interpreter to be active.
   *  @return Whether the new interpreter is currently in progress with an interaction
   */
  public boolean setToDefaultInterpreter() {
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
    for (int i=0; i < traceItems.size(); i++) {
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
  public void setShowMessageOnResetFailure(boolean show) {
    _messageOnResetFailure = show;
  }
  
  /** This method is called if the interpreterJVM cannot be exited (likely because of a modified security manager*/
  protected void quitFailed(Throwable th) {
    if (_messageOnResetFailure) {
      String msg = "The interactions pane could not be reset:\n" + th;
      javax.swing.JOptionPane.showMessageDialog(null, msg);
    }
    
    try { _mainJVM.quitFailed(th); }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("quitFailed: " + re.toString());
    }
  }
  
  /** Sets the interpreter to allow access to private members. */
  public void setPrivateAccessible(boolean allow) {
    Interpreter active = _activeInterpreter.getInterpreter();
    if (active instanceof JavaInterpreter) {
      ((JavaInterpreter)active).setPrivateAccessible(allow);
    }
  } 
  
  // ---------- JUnit methods ----------
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCases classes (by 
   *  loading them)
   *  @param classNames the class names to run in a test
   *  @param files the associated file
   *  @return the class names that are actually test cases
   */
  public List<String> findTestClasses(List<String> classNames, List<File> files)
    throws RemoteException {
    // new ScrollableDialog(null, "InterpterJVM.findTestClasses invoked", "", "").show();
    return _junitTestManager.findTestClasses(classNames, files);
  }
  
  /** Runs the JUnit test suite already cached in the Interpreter JVM.
   *  @return false if no test suite is cached; true otherwise
   */
  public boolean runTestSuite() throws RemoteException {
    // new ScrollableDialog(null, "InterpreterJVM.runTestSuite() called!", "", "").show();
    return _junitTestManager.runTestSuite();
  }
  
  /** Notifies the main JVM that JUnit has been invoked on a non TestCase class.
   *  @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) {
    try { _mainJVM.nonTestCase(isTestAll); }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("nonTestCase: " + re.toString());
    }
  }
  
  /** Notifies the main JVM that JUnitTestManager has encountered an illegal class file.
   *  @param e the ClassFileError object describing the error on loading the file
   */
  public void classFileError(ClassFileError e) {
    try { _mainJVM.classFileError(e); }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("classFileError: " + re.toString());
    }
  }
  
  /** Notifies that a suite of tests has started running.
   *  @param numTests The number of tests in the suite to be run.
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
  
  /** Notifies that a particular test has started.
   *  @param testName The name of the test being started.
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
  
  /** Notifies that a particular test has ended.
   *  @param testName The name of the test that has ended.
   *  @param wasSuccessful Whether the test passed or not.
   *  @param causedError If not successful, whether the test caused an error or simply failed.
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
  
  /** Notifies that a full suite of tests has finished running.
   *  @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
    try {
      _mainJVM.testSuiteEnded(errors);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("testSuiteFinished: " + re.toString());
    }
  }
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.
   *  @param className the name of the class for which we want to find the file
   *  @return the file associated with the given class
   */
  public File getFileForClassName(String className) {
    try {
      return _mainJVM.getFileForClassName(className);
    }
    catch (RemoteException re) {
      // nothing to do
      _log.logTime("getFileForClassName: " + re.toString());
      return null;
    }
  }
  
  public void junitJVMReady() { }
  
  
  //////////////////////////////////////////////////////////////
  // ALL functions regarding classpath
  //////////////////////////////////////////////////////////////
  
  /** Adds a classpath to the given interpreter.
   *  @param interpreter the interpreter
   */
  protected void _updateInterpreterClassPath(JavaInterpreter interpreter) {
    
    for (ClassPathEntry e: classPathManager.getProjectCP())
      interpreter.addProjectClassPath(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getBuildDirectoryCP())
      interpreter.addBuildDirectoryClassPath(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getProjectFilesCP())
      interpreter.addProjectFilesClassPath(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getExternalFilesCP())
      interpreter.addExternalFilesClassPath(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getExtraCP())
      interpreter.addExtraClassPath(e.getEntry());
  }
  
  /** Adds the given path to the classpath shared by ALL Java interpreters. This method <b>cannot</b> take multiple
   *  paths separated by a path separator; it must be called separately for each path.  Only unique paths are added.
   *  @param s Entry to add to the accumulated classpath
   */
  public void addExtraClassPath(URL s) {
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
  public void addProjectClassPath(URL s) {
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
  public void addBuildDirectoryClassPath(URL s) {
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
  public void addProjectFilesClassPath(URL s) {
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
  public void addExternalFilesClassPath(URL s) {
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
  public Vector<String> getAugmentedClassPath() {
    Vector<String> ret = new Vector<String>();

    for (ClassPathEntry e: classPathManager.getProjectCP())  ret.add(e.getEntry().toString());

    for (ClassPathEntry e: classPathManager.getBuildDirectoryCP()) 
      ret.add(e.getEntry().toString());
    
    for (ClassPathEntry e: classPathManager.getProjectFilesCP())
      ret.add(e.getEntry().toString());

    for (ClassPathEntry e: classPathManager.getExternalFilesCP())
      ret.add(e.getEntry().toString());

    for (ClassPathEntry e: classPathManager.getExtraCP())
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
  public ClassPathVector getClassPath() {
    ClassPathVector ret = new ClassPathVector();
    
    for (ClassPathEntry e: classPathManager.getProjectCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getBuildDirectoryCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getProjectFilesCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getExternalFilesCP()) ret.add(e.getEntry());
    
    for (ClassPathEntry e: classPathManager.getExtraCP()) ret.add(e.getEntry());
    
    return ret;
  } 
}

/** Bookkeeping class to maintain information about each interpreter, such as whether it is currently in progress. */
class InterpreterData {
  protected final Interpreter _interpreter;
  protected boolean _inProgress;
  
  InterpreterData(Interpreter interpreter) {
    _interpreter = interpreter;
    _inProgress = false;
  }
  
  /** Gets the interpreter. */
  public Interpreter getInterpreter() { return _interpreter; }
  
  /** Returns whether this interpreter is currently in progress with an interaction. */
  public boolean inProgress() { return _inProgress; }
  
  /** Sets whether this interpreter is currently in progress. */
  public void setInProgress(boolean inProgress) { _inProgress = inProgress; }
}
