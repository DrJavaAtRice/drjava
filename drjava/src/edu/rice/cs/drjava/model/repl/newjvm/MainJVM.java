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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.*;
import java.io.*;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

// NOTE: Do NOT import/use the config framework in this class!
//  (It seems to crash Eclipse...)
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.debug.DebugModelCallback;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.newjvm.*;

/**
 * Manages a remote JVM.
 *
 * @version $Id$
 */
public class MainJVM extends AbstractMasterJVM implements MainJVMRemoteI {
  /** Name of the class to use in the remote JVM. */
  private static final String SLAVE_CLASS_NAME =
    "edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM";

  public static final String DEFAULT_INTERPRETER_NAME = "DEFAULT";

  private Log _log = new Log("MainJVMLog", false);

  /** Listens to interactions-related events. */
  private InteractionsModelCallback _interactionsModel;

  /** Listens to JUnit-related events. */
  private JUnitModelCallback _junitModel;

  /** Listens to debug-related events */
  private DebugModelCallback _debugModel;

  /**
   * This flag is set to false to inhibit the automatic restart of the JVM.
   */
  private boolean _enabled = true;

  /**
   * This flag is set to remember that the JVM is cleanly restarting, so
   * that the replCalledSystemExit method does not need to be called.
   */
  private boolean _cleanlyRestarting = false;

  /** Instance of inner class to handle interpret result. */
  private final ResultHandler _handler = new ResultHandler();

  /** Whether to allow "assert" statements to run in the remote JVM. */
  private boolean _allowAssertions = false;

  /** Classpath to use for starting the interpreter JVM */
  private String _startupClasspath;

  /**
   * Starting classpath reorganized into a vector.
   */
  private Vector<String> _startupClasspathVector;

  /**
   * The name of the current interpreter.
   */
  private String _currentInterpreterName = DEFAULT_INTERPRETER_NAME;

  /**
   * Creates a new MainJVM to interface to another JVM, but does not
   * automatically start the Interpreter JVM.  Callers should set the
   * InteractionsModel and JUnitModel, and then call startInterpreterJVM().
   */
  public MainJVM() {
    super(SLAVE_CLASS_NAME);
    _waitForQuitThreadName = "Wait for Interactions to Exit Thread";
    _exportMasterThreadName = "Export DrJava to RMI Thread";

    _interactionsModel = new DummyInteractionsModel();
    _junitModel = new DummyJUnitModel();
    _debugModel = new DummyDebugModel();
    _startupClasspath = System.getProperty("java.class.path");
    _parseStartupClasspath();
    //startInterpreterJVM();
  }

  private void _parseStartupClasspath() {
    String separator = System.getProperty("path.separator");
    int index = _startupClasspath.indexOf(separator);
    int lastIndex = 0;
    _startupClasspathVector = new Vector<String>();
    while (index != -1) {
      _startupClasspathVector.add(_startupClasspath.substring(lastIndex, index));
      lastIndex = index + separator.length();
      index = _startupClasspath.indexOf(separator, lastIndex);
    }
    // Get the last entry
    index = _startupClasspath.length();
    _startupClasspathVector.add(_startupClasspath.substring(lastIndex, index));
  }

  public boolean isInterpreterRunning() {
    return _interpreterJVM() != null;
  }

  /**
   * Provides an object to listen to interactions-related events.
   */
  public void setInteractionsModel(InteractionsModelCallback model) {
    _interactionsModel = model;
  }

  /**
   * Provides an object to listen to test-related events.
   */
  public void setJUnitModel(JUnitModelCallback model) {
    _junitModel = model;
  }

  /**
   * Provides an object to listen to debug-related events.
   * @param model the debug model
   */
  public void setDebugModel(DebugModelCallback model) {
    _debugModel = model;
  }

  /**
   * Sets whether the remote JVM will run "assert" statements after
   * the next restart.
   */
  public void setAllowAssertions(boolean allow) {
    _allowAssertions = allow;
  }

  public void interpret(final String s) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    // Spawn thread on InterpreterJVM side
    //  (will receive result in the interpretResult(...) method)
    try {
      _log.logTime("main.interp: " + s);
      _interpreterJVM().interpret(s);
    }
    catch (java.rmi.UnmarshalException ume) {
      // Could not receive result from interpret; system probably exited.
      // We will silently fail and let the interpreter restart.
      _log.logTime("main.interp: UnmarshalException, so interpreter is dead:\n"
                 + ume);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * Gets the string representation of the value of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableToString(String var) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return null;

    ensureInterpreterConnected();

    try {
      return _interpreterJVM().getVariableToString(var);
    }
    catch (RemoteException re) {
      _threwException(re);
      return null;
    }
  }

  /**
   * Gets the class name of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableClassName(String var) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return null;

    ensureInterpreterConnected();

    try {
      return _interpreterJVM().getVariableClassName(var);
    }
    catch (RemoteException re) {
      _threwException(re);
      return null;
    }
  }

  /**
   * Called when a call to interpret has completed.
   * @param result The result of the interpretation
   */
  public void interpretResult(InterpretResult result) throws RemoteException {
//     try {
      _log.logTime("main.interp result: " + result);
      result.apply(getResultHandler());
//      }
//      catch (Throwable t) {
//        _log.logTime("EXCEPTION in interpretResult: " + t.toString());
//      }
  }


  /**
   * Adds a single path to the Interpreter's class path.
   * This method <b>cannot</b> take multiple paths separated by
   * a path separator; it must be called separately for each path.
   * @param path Path to be added to classpath
   */
  public void addClassPath(String path) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    try {
      //System.err.println("addclasspath to " + _interpreterJVM + ": " + path);
      _interpreterJVM().addClassPath(path);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * Returns the current classpath of the interpreter as a list of
   * unique entries.  The list is empty if a remote exception occurs.
   */
  public Vector<String> getClasspath() {
    // silently fail if disabled. see killInterpreter docs for details.
    if (_enabled) {

      ensureInterpreterConnected();

      try {
        Vector<String> classpath = new Vector<String>(_startupClasspathVector);
        classpath.addAll(_interpreterJVM().getAugmentedClasspath());
//        for(int i = 0; i < _startupClasspathVector.size(); i++) {
//          classpath.addElement(_startupClasspathVector.elementAt(i));
//        }
//        Vector<String> augmentedClasspath = _interpreterJVM().getAugmentedClasspath();
//        for(int i = 0; i < augmentedClasspath.size(); i++) {
//          classpath.addElement(augmentedClasspath.elementAt(i));
//        }
        return classpath;
      }
      catch (RemoteException re) {
        _threwException(re);
      }
    }
    return new Vector<String>();
  }

  /**
   * Gets the augmented classpath of the interpreter jvm as a string.
   */
  public String getClasspathString() {
    // silently fail if disabled. see killInterpreter docs for details.
    if (!_enabled) {
      return null;
    }

    ensureInterpreterConnected();
    try {
      return _interpreterJVM().getClasspathString();
    }
    catch (RemoteException re) {
      _threwException(re);
      return "";
    }
  }

  /**
   * Sets the Interpreter to be in the given package.
   * @param packageName Name of the package to enter.
   */
  public void setPackageScope(String packageName) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    try {
      _interpreterJVM().setPackageScope(packageName);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * "Soft" resets the interpreter, without killing the JVM.  This method
   * is not really used anymore, since this doesn't reset any threads that
   * have been spawned.
   */
  public void reset() {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    try {
      _interpreterJVM().reset();
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * @param show Whether to show a message if a reset operation fails.
   */
  public void setShowMessageOnResetFailure(boolean show) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    try {
      _interpreterJVM().setShowMessageOnResetFailure(show);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * Forwards a call to System.err from InterpreterJVM to the
   * local InteractionsModel.
   * @param s String that was printed in the other JVM
   */
  public void systemErrPrint(String s) throws RemoteException {
    _interactionsModel.replSystemErrPrint(s);
  }

  /**
   * Forwards a call to System.out from InterpreterJVM to the
   * local InteractionsModel.
   * @param s String that was printed in the other JVM
   */
  public void systemOutPrint(String s) throws RemoteException {
    _interactionsModel.replSystemOutPrint(s);
  }

  /**
   * Runs a JUnit Test class in the Interpreter JVM.
   * @param classNames the class names to run in a test
   * @param files the associated filenames
   * @param isTestAll if we're testing all open files or not
   */
  public List<String> runTestSuite(List<String> classNames, List<File> files,
                                    boolean isTestAll) {
    List<String> classes = new ArrayList<String>();
    // silently fail if disabled. see killInterpreter docs for details.
    if (_enabled) {
      ensureInterpreterConnected();

      try {
        classes = _interpreterJVM().runTestSuite(classNames, files, isTestAll);
      }
      catch (RemoteException re) {
        _threwException(re);
      }
    }
    return classes;
  }

  /**
   * Called if JUnit is invoked on a non TestCase class.  Forwards from
   * the other JVM to the local JUnit model.
   * @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) throws RemoteException {
    _junitModel.nonTestCase(isTestAll);
  }

  /**
   * Called to indicate that a suite of tests has started running.
   * Forwards from the other JVM to the local JUnit model.
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) throws RemoteException {
    _junitModel.testSuiteStarted(numTests);
  }

  /**
   * Called when a particular test is started.
   * Forwards from the other JVM to the local JUnit model.
   * @param testName The name of the test being started.
   */
  public void testStarted(String testName) throws RemoteException {
    _junitModel.testStarted(testName);
  }

  /**
   * Called when a particular test has ended.
   * Forwards from the other JVM to the local JUnit model.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError)
    throws RemoteException {
    _junitModel.testEnded(testName, wasSuccessful, causedError);
  }

  /**
   * Called when a full suite of tests has finished running.
   * Forwards from the other JVM to the local JUnit model.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) throws RemoteException {
    _junitModel.testSuiteEnded(errors);
  }

  /**
   * Called when the JUnitTestManager wants to open a file that is not currently open.
   * @param className the name of the class for which we want to find the file
   * @return the file associated with the given class
   */
  public File getFileForClassName(String className) throws RemoteException {
    return _junitModel.getFileForClassName(className);
  }

  /**
   * Notifies the main jvm that an assignment has been made in the given debug
   * interpreter.
   * Does not notify on declarations.
   *
   * This method is not currently necessary, since we don't copy back
   * values in a debug interpreter until the thread has resumed.
   *
   * @param name the name of the debug interpreter
   *
  public void notifyDebugInterpreterAssignment(String name) {
  }*/

  /**
   * Accessor for the remote interface to the Interpreter JVM.
   */
  private InterpreterJVMRemoteI _interpreterJVM() {
    return (InterpreterJVMRemoteI) getSlave();
  }

  /**
   * Adds a named DynamicJavaAdapter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addJavaInterpreter(String name) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    try {
      _interpreterJVM().addJavaInterpreter(name);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * Adds a named JavaDebugInterpreter to the list of interpreters.
   * @param name the unique name for the interpreter
   * @param className the fully qualified class name of the class
   * the debug interpreter is in
   * @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name, String className) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) {
      return;
    }

    ensureInterpreterConnected();

    try {
      _interpreterJVM().addDebugInterpreter(name, className);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * Removes the interpreter with the given name, if it exists.
   * @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (!_enabled) {
      return;
    }

    ensureInterpreterConnected();

    try {
      _interpreterJVM().removeInterpreter(name);
      if (name.equals(_currentInterpreterName)) {
        _currentInterpreterName = null;
      }
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * sets the current interpreter to the one specified by name
   * @param name the unique name of the interpreter to set active
   * @return Whether the new interpreter is currently in progress
   * with an interaction (ie. whether an interactionEnded event will be fired)
   */
  public boolean setActiveInterpreter(String name) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (!_enabled) {
      return false;
    }

    ensureInterpreterConnected();

    try {
      boolean result = _interpreterJVM().setActiveInterpreter(name);
      _currentInterpreterName = name;
      return result;
    }
    catch (RemoteException re) {
      _threwException(re);
      return false;
    }
  }

  /**
   * Sets the default interpreter to be the current one.
   * @return Whether the new interpreter is currently in progress
   * with an interaction (ie. whether an interactionEnded event will be fired)
   */
  public boolean setToDefaultInterpreter() {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return false;

    ensureInterpreterConnected();

    try {
      boolean result = _interpreterJVM().setToDefaultInterpreter();
      _currentInterpreterName = DEFAULT_INTERPRETER_NAME;
      return result;
    }
    catch (ConnectIOException ce) {
      _log.logTime("Could not connect to the interpreterJVM after killing it", ce);
      return false;
    }
    catch (RemoteException re) {
      _threwException(re);
      return false;
    }
  }

  /**
   * Accesses the cached current interpreter name.
   */
  public String getCurrentInterpreterName() {
    return _currentInterpreterName;
  }

  /**
   * Kills the running interpreter JVM, and optionally restarts it
   *
   * @param shouldRestart if true, the interpreter will be restarted
   * automatically.
   * Note: If the interpreter is not restarted, all of the methods that
   * delgate to the interpreter will silently fail!
   * Therefore, killing without restarting should be used with extreme care
   * and only in carefully controlled test cases or when DrJava is quitting
   * anyway.
   */
  public synchronized void killInterpreter(boolean shouldRestart) {
    try {
      _enabled = shouldRestart;
      _cleanlyRestarting = true;
      if (shouldRestart) {
        _interactionsModel.interpreterResetting();
      }
      quitSlave();
    }
    catch (ConnectException ce) {
      _log.logTime("Could not connect to the interpreterJVM while trying to kill it", ce);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * Sets the classpath to use for starting the interpreter JVM.
   * Must include the classes for the interpreter.
   * @param classpath Classpath for the interpreter JVM
   */
  public void setStartupClasspath(String classpath) {
    _startupClasspath = classpath;
    _parseStartupClasspath();
  }

  /**
   * Starts the interpreter if it's not running already.
   */
  public void startInterpreterJVM() {
    if (isStartupInProgress() || isInterpreterRunning()) {
      return;
    }

    // Pass assertion and debug port information as JVM arguments
    ArrayList<String> jvmArgs = new ArrayList<String>();
    if (allowAssertions()) {
      jvmArgs.add("-ea");
    }
    // set the "user.dir" property to the user's working directory so that relative files will resolve correctly.
//    File workDir = DrJava.getConfig().getSetting(OptionConstants.WORKING_DIRECTORY);
//    if (workDir != FileOption.NULL_FILE) {
//      jvmArgs.add("-Duser.dir=" + workDir.getAbsolutePath());
//    }
    int debugPort = getDebugPort();
    _log.logTime("starting with debug port: " + debugPort);
    if (debugPort > -1) {
      jvmArgs.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" +
                         debugPort);
      jvmArgs.add("-Xdebug");
      jvmArgs.add("-Xnoagent");
      jvmArgs.add("-Djava.compiler=NONE");
    }
    if (DrJava.usingJSR14v20()) {
      // System.out.println("using jsr14 v2.0");
      File jsr14 = DrJava.getConfig().getSetting(OptionConstants.JSR14_LOCATION);
      jvmArgs.add("-Xbootclasspath/p:" + jsr14.getAbsolutePath());
    }
    String optionArgString = DrJava.getConfig().getSetting(OptionConstants.JVM_ARGS);
    List<String> optionArgs = ArgumentTokenizer.tokenize(optionArgString);
    jvmArgs.addAll(optionArgs);
    String[] jvmArgsArray = new String[jvmArgs.size()];
    for (int i=0; i < jvmArgs.size(); i++) {
      jvmArgsArray[i] = jvmArgs.get(i);
    }

    // Invoke the Interpreter JVM
    try {
      invokeSlave(jvmArgsArray, _startupClasspath);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
    catch (IOException ioe) {
      _threwException(ioe);
    }
  }

  /**
   * React if the slave JVM quits.  Restarts the JVM unless _enabled is false,
   * and notifies the InteractionsModel if the quit was unexpected.
   * @param status Status returned by the dead process.
   */
  protected void handleSlaveQuit(int status) {
    // Only restart the slave if _enabled is true
    if (_enabled) {
      // We have already fired this event if we are cleanly restarting
      if (!_cleanlyRestarting) {
        _interactionsModel.interpreterResetting();
      }
      startInterpreterJVM();
    }

    if (!_cleanlyRestarting) {
      _interactionsModel.replCalledSystemExit(status);
    }
    _cleanlyRestarting = false;
  }

  /**
   * Action to take if the slave JVM quits before registering.
   * @param status Status code of the JVM
   */
  protected void slaveQuitDuringStartup(int status) {
    // The slave JVM is not enabled after this.
    _enabled = false;

    String msg = "Interpreter JVM exited before registering, status: "
      + status;
    IllegalStateException e = new IllegalStateException(msg);
    _interactionsModel.interpreterResetFailed(e);
    _cleanlyRestarting = false;
    throw e;
  }

  /**
   * Called if the slave JVM dies before it is able to register.
   * @param cause The Throwable which caused the slave to die.
   */
  public void errorStartingSlave(Throwable cause) throws RemoteException {
    new edu.rice.cs.drjava.ui.AWTExceptionHandler().handle(cause);
  }

  /**
   * This method is called by the interpreter JVM if it cannot
   * be exited (likely because of its having a
   * security manager)
   * @param th The Throwable thrown by System.exit
   */
  public void quitFailed(Throwable th) throws RemoteException {
    _interactionsModel.interpreterResetFailed(th);
    _cleanlyRestarting = false;
  }


  /**
   * Returns whether a JVM is currently starting.
   */
  public boolean isStartupInProgress() {
    return super.isStartupInProgress();
  }

  /**
   * Called when the Interpreter JVM connects to us after being started.
   */
  protected void handleSlaveConnected() {
    // we reset the enabled flag since, unless told otherwise via
    // killInterpreter(false), we want to automatically respawn
    _enabled = true;
    _cleanlyRestarting = false;

    Boolean allowAccess = DrJava.getConfig().getSetting(OptionConstants.ALLOW_PRIVATE_ACCESS);
    setPrivateAccessible(allowAccess.booleanValue());

    _interactionsModel.interpreterReady();
    _junitModel.junitJVMReady();

    _log.logTime("thread in connected: " + Thread.currentThread());

    synchronized(this) {
      // notify so that if we were waiting (in ensureConnected)
      // this will wake em up
      notify();
    }
  }

  /**
   * Returns the visitor to handle an InterpretResult.
   */
  protected InterpretResultVisitor<Object> getResultHandler() {
    return _handler;
  }

  /**
   * Returns the debug port to use, as specified by the model.
   * Returns -1 if no usable port could be found.
   */
  protected int getDebugPort() {
    int port = -1;
    try {
      port = _interactionsModel.getDebugPort();
    }
    catch (IOException ioe) {
      // Can't find port; don't use debugger
    }
    return port;
  }


  /**
   * Return whether to allow assertions in the InterpreterJVM.
   */
  protected boolean allowAssertions() {
    String version = System.getProperty("java.version");
    return (_allowAssertions && (version != null) &&
        ("1.4.0".compareTo(version) <= 0));
  }

  /**
   * Lets the model know if any exceptions occur while communicating with
   * the Interpreter JVM.
   */
  private void _threwException(Throwable t) {
    _interactionsModel.replThrewException(t.getClass().getName(),
                                          t.getMessage(),
                                          StringOps.getStackTrace(t));
  }

  /**
   * Sets the interpreter to allow access to private members.
   */
  public void setPrivateAccessible(boolean allow) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (!_enabled) {
      return;
    }

    ensureInterpreterConnected();
    try {
      _interpreterJVM().setPrivateAccessible(allow);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  /**
   * If an interpreter has not registered itself, this method will
   * block until one does.
   */
  public void ensureInterpreterConnected() {
    try {
      synchronized(this) {
        // Now we silently fail if interpreter is disabled instead of
        // throwing an exception. This situation occurs only in test cases
        // and when DrJava is about to quit.
        //if (! _enabled) {
          //throw new IllegalStateException("Interpreter is disabled");
        //}

        while (_interpreterJVM() == null) {
          //_log.logTime("interpreter is null, waiting for it to register");
          wait();
        }
        //_log.logTime("interpreter registered; moving on");
      }
    }
    catch (InterruptedException ie) {
      throw new edu.rice.cs.util.UnexpectedException(ie);
    }
  }

  /**
   * Asks the main jvm for input from the console.
   * @return the console input
   */
  public String getConsoleInput() {
    return _interactionsModel.getConsoleInput();
  }

  /**
   * Peforms the appropriate action to return any type of result
   * from a call to interpret back to the GlobalModel.
   */
  private class ResultHandler implements InterpretResultVisitor<Object> {
    /**
     * Lets the model know that void was returned.
     * @return null
     */
    public Object forVoidResult(VoidResult that) {
      _interactionsModel.replReturnedVoid();
      return null;
    }

    /**
     * Returns a value result (as a String) back to the model.
     * @return null
     */
    public Object forValueResult(ValueResult that) {
      String result = that.getValueStr();
      _interactionsModel.replReturnedResult(result);
      return null;
    }

    /**
     * Returns an exception back to the model.
     * @return null
     */
    public Object forExceptionResult(ExceptionResult that) {
      _interactionsModel.replThrewException(that.getExceptionClass(),
                                that.getExceptionMessage(),
                                that.getStackTrace());
      return null;
    }

    /**
     * Indicates there was a syntax error to the model.
     * @return null
     */
    public Object forSyntaxErrorResult(SyntaxErrorResult that) {
      _interactionsModel.replReturnedSyntaxError(that.getErrorMessage(),
                                                 that.getInteraction(),
                                                 that.getStartRow(),
                                                 that.getStartCol(),
                                                 that.getEndRow(),
                                                 that.getEndCol() );
      return null;
    }
  }

  /**
   * InteractionsModel which does not react to events.
   */
  public static class DummyInteractionsModel implements InteractionsModelCallback {
    public int getDebugPort() throws IOException { return -1; }
    public void replSystemOutPrint(String s) {}
    public void replSystemErrPrint(String s) {}
    public String getConsoleInput() {
      throw new IllegalStateException("Cannot request input from dummy interactions model!");
    }
    public void setInputListener(InputListener il) {
      throw new IllegalStateException("Cannot set the input listener of dummy interactions model!");
    }
    public void changeInputListener(InputListener from, InputListener to) {
      throw new IllegalStateException("Cannot change the input listener of dummy interactions model!");
    }
    public void replReturnedVoid() {}
    public void replReturnedResult(String result) {}
    public void replThrewException(String exceptionClass,
                                   String message,
                                   String stackTrace) {}
    public void replReturnedSyntaxError(String errorMessage,
                                        String interaction,
                                        int startRow,
                                        int startCol,
                                        int endRow,
                                        int endCol ) {}
    public void replCalledSystemExit(int status) {}
    public void interpreterResetting() {}
    public void interpreterResetFailed(Throwable th) {}
    public void interpreterReady() {}
  }

  /**
   * JUnitModel which does not react to events.
   */
  public static class DummyJUnitModel implements JUnitModelCallback {
    public void nonTestCase(boolean isTestAll) {}
    public void testSuiteStarted(int numTests) {}
    public void testStarted(String testName) {}
    public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {}
    public void testSuiteEnded(JUnitError[] errors) {}
    public File getFileForClassName(String className) { return null; }
    public String getClasspathString() { return ""; }
    public void junitJVMReady() {}
  }

  /**
   * DebugModelCallback which does not react to events.
   */
  public static class DummyDebugModel implements DebugModelCallback {
    public void notifyDebugInterpreterAssignment(String name) {
    }
  }
}
