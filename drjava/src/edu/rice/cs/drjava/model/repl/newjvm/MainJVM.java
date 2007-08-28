/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.*;
import java.io.*;
import java.net.MalformedURLException;

import java.util.List;
import java.util.ArrayList;

// NOTE: Do NOT import/use the config framework in this class!
//  (It seems to crash Eclipse...)
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.debug.DebugModelCallback;

import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.swing.ScrollableDialog;
import koala.dynamicjava.parser.wrapper.*;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Manages a remote JVM.
 *  @version $Id$
 */
public class MainJVM extends AbstractMasterJVM implements MainJVMRemoteI {
  /** Name of the class to use in the remote JVM. */
  private static final String SLAVE_CLASS_NAME = "edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM";
  
  public static final String DEFAULT_INTERPRETER_NAME = "DEFAULT";
  
  // _log is inherited from AbstractMasterJVM
  
  /** Working directory for slave JVM */
  private volatile File _workDir;
  
  /** Listens to interactions-related events. */
  private volatile InteractionsModelCallback _interactionsModel;
  
  /** Listens to JUnit-related events. */
  private volatile JUnitModelCallback _junitModel;
  
  /** Listens to debug-related events */
  private volatile DebugModelCallback _debugModel;
  
  /** Used to protect interpreterJVM setting */
  private final Object _interpreterLock = new Object();
  
  /** Records state of slaveJVM (interpreterJVM); used to suppress restartInteractions on a fresh JVM */
  private volatile boolean _slaveJVMUsed = false;
  
  /** This flag is set to false to inhibit the automatic restart of the JVM. */
  private volatile boolean _restart = true;
  
  /** This flag is set to remember that the JVM is cleanly restarting, so that the replCalledSystemExit method
   *  does not need to be called.
   */
  private volatile boolean _cleanlyRestarting = false;
  
  /** Number of previous attempts to start slave JVM in this startup. */
  private volatile int _numAttempts = 0;
  
  /** Number of slave startup failures allowed before aborting the startup process. */
  private static final int MAX_COUNT = 3;
  
  /** Instance of inner class to handle interpret result. */
  private final ResultHandler _handler = new ResultHandler();
  
  /** Whether to allow "assert" statements to run in the remote JVM. */
  private volatile boolean _allowAssertions = false;
  
  /** Classpath to use for starting the interpreter JVM */
  private volatile Iterable<File> _startupClassPath;

  /** A list of user-defined arguments to pass to the interpreter. */
  private volatile List<String> _optionArgs;
  
  /** The name of the current interpreter. */
  private volatile String _currentInterpreterName = DEFAULT_INTERPRETER_NAME;
  
  /** Creates a new MainJVM to interface to another JVM;  the MainJVM has a link to the partially initialized 
   *  global model.  The MainJVM but does not automatically start the Interpreter JVM.  Callers must set the
   *  InteractionsModel and JUnitModel and then call startInterpreterJVM().
   */
  public MainJVM(File wd) throws RemoteException {
    super(SLAVE_CLASS_NAME);
//    Utilities.show("Starting the slave JVM");
    _workDir = wd;
    _waitForQuitThreadName = "Wait for Interactions to Exit Thread";
//    _exportMasterThreadName = "Export DrJava to RMI Thread";
    
    _interactionsModel = new DummyInteractionsModel();
    _junitModel = new DummyJUnitModel();
    _debugModel = new DummyDebugModel();
    _startupClassPath = GlobalModel.RUNTIME_CLASS_PATH;
    _optionArgs = new ArrayList<String>();
  }
  
  public boolean isInterpreterRunning() { return _interpreterJVM() != null; }
  
  public boolean slaveJVMUsed() { return _slaveJVMUsed; }
  
  /** Provides an object to listen to interactions-related events. */
  public void setInteractionsModel(InteractionsModelCallback model) { _interactionsModel = model; }
  
  /** Provides an object to listen to test-related events.*/
  public void setJUnitModel(JUnitModelCallback model) { _junitModel = model; }
  
  /** Provides an object to listen to debug-related events.
   *  @param model the debug model
   */
  public void setDebugModel(DebugModelCallback model) { _debugModel = model; }
  
  /** Sets whether the remote JVM will run "assert" statements after the next restart. */
  public void setAllowAssertions(boolean allow) { _allowAssertions = allow; }
  
  /** Sets the extra (optional) arguments to be passed to the interpreter.
   *  @param argString the arguments as they would be typed at the command-line
   */
  public void setOptionArgs(String argString) { _optionArgs = ArgumentTokenizer.tokenize(argString); }
  
  /** Interprets string s in slave JVM.  No masterJVMLock synchronization because reading _restart is the only
   *  access.to master JVM state. */
  public void interpret(final String s) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    // Spawn thread on InterpreterJVM side
    //  (will receive result in the interpretResult(...) method)
    try {
      _log.log(this + ".interpret(" + s + ")");
      _slaveJVMUsed = true;
      _interactionsModel.slaveJVMUsed();
      slave.interpret(s);
    }
    catch (java.rmi.UnmarshalException ume) {
      // Could not receive result from interpret; system probably exited.
      // We will silently fail and let the interpreter restart.
      _log.log(this + ".interpret threw UnmarshalException, so interpreter is dead:\n" + ume);
    }
    catch (RemoteException re) { _threwException(re); }
  }
  
  /** Gets the string representation of the value of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public String getVariableToString(String var) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return null;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { return slave.getVariableToString(var); }
    catch (RemoteException re) {
      _threwException(re);
      return null;
    }
  }
  
  /** Gets the class name of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public String getVariableClassName(String var) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return null;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
      
    try { return slave.getVariableClassName(var); }
    catch (RemoteException re) {
      _threwException(re);
      return null;
    }
  }
  
  /** Called when a call to interpret has completed.
   *  @param result The result of the interpretation
   */
  public void interpretResult(InterpretResult result) throws RemoteException {
    try {
      _log.log(this + ".interpretResult(" + result + ")");
      result.apply(getResultHandler());
    }
    catch (Throwable t) {
      _log.log(this + "interpretResult threw " + t.toString());
    }
  }
  
//  /**
//   * Adds a single path to the Interpreter's class path.
//   * This method <b>cannot</b> take multiple paths separated by
//   * a path separator; it must be called separately for each path.
//   * @param path Path to be added to classpath
//   */
//  public void addClassPath(String path) {
//    // silently fail if disabled. see killInterpreter docs for details.
//    if (! _restart) return;
//    
//    ensureInterpreterConnected();
//    
//    try {
//      //      System.err.println("addclasspath to " + _interpreterJVM() + ": " + path);
//      //      System.err.println("full classpath: " + getClasspath());
//      _interpreterJVM().addClassPath(path);
//    }
//    catch (RemoteException re) {
//      _threwException(re);
//    }
//  }
  
  public void addProjectClassPath(File f) {
    if (! _restart) return;
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.addProjectClassPath(f); }
    catch(RemoteException re) { _threwException(re); }
  }
  
  public void addBuildDirectoryClassPath(File f) {
    if (! _restart) return;
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.addBuildDirectoryClassPath(f); }
    catch(RemoteException re) { _threwException(re); }
  }
  
  public void addProjectFilesClassPath(File f) {
    if (! _restart) return;
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.addProjectFilesClassPath(f); }
    catch(RemoteException re) { _threwException(re); }
  }
  
  public void addExternalFilesClassPath(File f) {
    if (! _restart) return;
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.addExternalFilesClassPath(f); }
    catch(RemoteException re) { _threwException(re); }
  }
  
  public void addExtraClassPath(File f) {
    if (! _restart) return;
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.addExtraClassPath(f); }
    catch(RemoteException re) { _threwException(re); }
  }
  
  /** Returns the current classpath of the interpreter as a list of
   *  unique entries.  The list is empty if a remote exception occurs.
   */
  public Iterable<File> getClassPath() {
    // silently fail if disabled. see killInterpreter docs for details.
    if (_restart) {
      
      InterpreterJVMRemoteI slave = ensureInterpreterConnected();
      
      try { return IterUtil.compose(slave.getAugmentedClassPath(), _startupClassPath); }
      catch (RemoteException re) { _threwException(re); return IterUtil.empty(); }
    }
    else { return IterUtil.empty(); }
  }
  
  
  /** Sets the Interpreter to be in the given package.
   *  @param packageName Name of the package to enter.
   */
  public void setPackageScope(String packageName) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.setPackageScope(packageName); }
    catch (RemoteException re) { _threwException(re); }
  }
  
  /** @param show Whether to show a message if a reset operation fails. */
  public void setShowMessageOnResetFailure(boolean show) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();

    try { slave.setShowMessageOnResetFailure(show); }
    catch (RemoteException re) { _threwException(re); }
  }
  
  /** Forwards a call to System.err from InterpreterJVM to the local InteractionsModel.
   *  @param s String that was printed in the other JVM
   */
  public void systemErrPrint(String s) throws RemoteException {
    _interactionsModel.replSystemErrPrint(s);
  }
  
  /** Forwards a call to System.out from InterpreterJVM to the local InteractionsModel.
   *  @param s String that was printed in the other JVM
   */
  public void systemOutPrint(String s) throws RemoteException {
    _interactionsModel.replSystemOutPrint(s);
  }
  
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCases
   *  classes (by loading them)
   *  @param classNames the class names to run in a test
   *  @param files the associated file
   *  @return the class names that are actually test cases
   */
  public List<String> findTestClasses(List<String> classNames, List<File> files) throws RemoteException {
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    return slave.findTestClasses(classNames, files);
  }
  
  /** Runs the JUnit test suite already cached in the Interpreter JVM.
   *  @return false if no test suite is cached; true otherwise
   */
  public boolean runTestSuite() throws RemoteException {
    return _interpreterJVM().runTestSuite();
  }
  
  /** Called if JUnit is invoked on a non TestCase class.  Forwards from the other JVM to the local JUnit model.
   *  @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) throws RemoteException {
    _junitModel.nonTestCase(isTestAll);
  }
  
  /** Called if the slave JVM encounters an illegal class file in testing.  Forwards from
   *  the other JVM to the local JUnit model.
   *  @param e the ClassFileError describing the error when loading the class file
   */
  public void classFileError(ClassFileError e) throws RemoteException {
//    Utilities.showDebug("classFileError(" + e + ") called in MainJVM");
    _junitModel.classFileError(e);
  }
  /** Called to indicate that a suite of tests has started running.
   *  Forwards from the other JVM to the local JUnit model.
   *  @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) throws RemoteException {
    _slaveJVMUsed = true;
//    Utilities.show("MainJVM.testSuiteStarted(" + numTests + ") called");
    _interactionsModel.slaveJVMUsed();
    _junitModel.testSuiteStarted(numTests);
  }
  
  /** Called when a particular test is started.  Forwards from the slave JVM to the local JUnit model.
   *  @param testName The name of the test being started.
   */
  public void testStarted(String testName) throws RemoteException {
//    Utilities.show("MainJVM.testStarted(" + testName + ") called");
    _slaveJVMUsed = true;
//     Utilities.show("MainJVM.testStarted(" + testName + ") called");
    _junitModel.testStarted(testName);
  }
  
  /** Called when a particular test has ended. Forwards from the other JVM to the local JUnit model.
   *  @param testName The name of the test that has ended.
   *  @param wasSuccessful Whether the test passed or not.
   *  @param causedError If not successful, whether the test caused an error or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) throws RemoteException {
    _junitModel.testEnded(testName, wasSuccessful, causedError);
  }
  
  /** Called when a full suite of tests has finished running. Forwards from the other JVM to the local JUnit model.
   *  @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) throws RemoteException {
//    Utilities.showDebug("MainJVM.testSuiteEnded() called");
    _junitModel.testSuiteEnded(errors);
  }
  
  /** Called when the JUnitTestManager wants to open a file that is not currently open.
   *  @param className the name of the class for which we want to find the file
   *  @return the file associated with the given class
   */
  public File getFileForClassName(String className) throws RemoteException {
    return _junitModel.getFileForClassName(className);
  }
  
  /** Notifies the main jvm that an assignment has been made in the given debug interpreter.
   *  Does not notify on declarations.
   *
   *  This method is not currently necessary, since we don't copy back values in a debug interpreter until the thread
   *  has resumed.
   *
   * @param name the name of the debug interpreter
   *
   public void notifyDebugInterpreterAssignment(String name) {
   }*/
  
  /**Accessor for the remote interface to the Interpreter JVM; _slave is protected field of the supserclass. */
  private InterpreterJVMRemoteI _interpreterJVM() { return (InterpreterJVMRemoteI) _slave; }
  
//  /** Updates the security manager in slave JVM */
//  public void enableSecurityManager() throws RemoteException {
//    _interpreterJVM().enableSecurityManager();
//  }
//  
//  /** Updates the security manager in slave JVM */
//  public void disableSecurityManager() throws RemoteException{
//    _interpreterJVM().disableSecurityManager();
//  }
  
  
  /** Adds a named DynamicJavaAdapter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @throws IllegalArgumentException if the name is not unique
   */
  public void addJavaInterpreter(String name) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.addJavaInterpreter(name);  }
    catch (RemoteException re) { _threwException(re);  }
  }
  
  /** Adds a named JavaDebugInterpreter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @param className the fully qualified class name of the class the debug interpreter is in
   *  @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name, String className) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try { slave.addDebugInterpreter(name, className); }
    catch (RemoteException re) { _threwException(re); }
  }
  
  /** Removes the interpreter with the given name, if it exists.
   *  @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (!_restart)  return;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try {
      slave.removeInterpreter(name);
      if (name.equals(_currentInterpreterName))  _currentInterpreterName = null;
    }
    catch (RemoteException re) { _threwException(re); }
  }
  
  /** Sets the current interpreter to the one specified by name
   *  @param name the unique name of the interpreter to set active
   *  @return Whether the new interpreter is currently processing an interaction (i.e., whether an interactionEnded
   *          event will be fired)
   */
  public boolean setActiveInterpreter(String name) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (!_restart) return false;
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try {
      boolean result = slave.setActiveInterpreter(name);
      _currentInterpreterName = name;
      return result;
    }
    catch (RemoteException re) {
      _threwException(re);
      return false;
    }
  }
  
  /** Sets the default interpreter to be the current one.
   *  @return Whether the new interpreter is currently in progress with an interaction (ie. whether an 
   *          interactionEnded event will be fired)
   */
  public boolean setToDefaultInterpreter() {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _restart) return false;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    
    try {
      boolean result = slave.setToDefaultInterpreter();
      _currentInterpreterName = DEFAULT_INTERPRETER_NAME;
      return result;
    }
    catch (ConnectIOException ce) {
      _log.log(this + "could not connect to the interpreterJVM after killing it.  Threw " + ce);
      return false;
    }
    catch (RemoteException re) {
      _threwException(re);
      return false;
    }
  }

  /** Accesses the cached current interpreter name. */
  public String getCurrentInterpreterName() { return _currentInterpreterName; }
  
  /** Kills the running interpreter JVM, and restarts with working directory wd if wd != null.  If wd == null, the
   * interpreter is not restarted.  Note: If the interpreter is not restarted, all of the methods that delegate to the
   * interpreter will silently fail!  Therefore, killing without restarting should be used with extreme care and only in 
   * carefully controlled test cases or when DrJava is quitting anyway.
   */

  public void killInterpreter(File wd) {
    boolean restart;
    synchronized(_masterJVMLock) {
      _workDir = wd;
      _restart = (wd != null);
      _cleanlyRestarting = true;
      restart = _restart;
    }
      
    /* Dropping lock before performing operations on the interactions document/pane and making remote call. */
    try { 
      if (restart) _interactionsModel.interpreterResetting();
      quitSlave(); 
    } // new slave JVM is restarted by call on startInterpreterJVM on death of current slave
    catch (RemoteException e) {
      _log.log(this + "could not connect to the interpreterJVM while trying to kill it.  Threw " + e);
    }
  }
  
  /** Sets the classpath to use for starting the interpreter JVM. Must include the classes for the interpreter.
   *  @param classPath Classpath for the interpreter JVM
   */
  public void setStartupClassPath(String classPath) {
    _startupClassPath = IOUtil.attemptCanonicalFiles(IOUtil.parsePath(classPath));
  }
  
  /** Starts the interpreter if it's not running already. */
  public void startInterpreterJVM() {
    _log.log(this + ".startInterpreterJVM() called");
//    synchronized(_masterJVMLock) {  // synch is unnecessary
    if (isStartupInProgress() || isInterpreterRunning())  return;  // These predicates simply check volatile boolean flags
//    }
    // Pass assertion and debug port information as JVM arguments
    ArrayList<String> jvmArgs = new ArrayList<String>();
    if (allowAssertions())  jvmArgs.add("-ea");
    int debugPort = getDebugPort();
    _log.log("Main JVM starting with debug port: " + debugPort);
    if (debugPort > -1) {
      jvmArgs.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + debugPort);
      jvmArgs.add("-Xdebug");
      jvmArgs.add("-Xnoagent");
      jvmArgs.add("-Djava.compiler=NONE");
    }
    // Cannot do the following line because it causes an error on Macs in the Eclipse plug-in.
    // By instantiating the config, somehow the Apple JVM tries to start up AWT, which seems
    // to be prohibited by Eclipse.  Badness ensues.
    //    String optionArgString = DrJava.getConfig().getSetting(OptionConstants.JVM_ARGS);
    //    List<String> optionArgs = ArgumentTokenizer.tokenize(optionArgString);
    jvmArgs.addAll(_optionArgs);
    String[] jvmArgsArray = new String[jvmArgs.size()];
    for (int i = 0; i < jvmArgs.size(); i++) { jvmArgsArray[i] = jvmArgs.get(i); }
    
    // Create and invoke the Interpreter JVM
    _numAttempts = 0;
    try {
     // _startupClasspath is sent in as the interactions classpath
//      Utilities.show("Calling invokeSlave(" + jvmArgs + ", " + _startupClassPath + ", " +  _workDir +")");
      invokeSlave(jvmArgsArray, IOUtil.pathToString(_startupClassPath), _workDir);
      _slaveJVMUsed = false;
    }
    catch (RemoteException re) { _threwException(re); }
    catch (IOException ioe) { _threwException(ioe); }
  }
  
  /** React if the slave JVM quits.  Restarts the JVM unless _restart is false, and notifies the InteractionsModel
   *  if the quit was unexpected.  Called from a thread within AbstractMasterJVM waiting for the death of the process
   *  that starts and runs the slave JVM.
   *  @param status Status returned by the dead process.
   */
  protected void handleSlaveQuit(int status) {
    // Only restart the slave if _restart is true
//    Utilities.showDebug("MainJVM: slaveJVM has quit with status " + status + " _workDir = " + _workDir + 
//      " _cleanlyRestarting = " + _cleanlyRestarting);
    if (_restart) {
      // We have already fired this event if we are cleanly restarting
      if (! _cleanlyRestarting) _interactionsModel.interpreterResetting();
//      Utilities.showDebug("MainJVM: calling startInterpreterJVM()");
      startInterpreterJVM();
    }
    
    if (!_cleanlyRestarting) _interactionsModel.replCalledSystemExit(status);
    _cleanlyRestarting = false;
  }
  
  
  
  /** Action to take if the slave JVM quits before registering.  Assumes _masterJVMLock is held.
   *  @param status Status code of the JVM
   *  TODO: revise the unit tests that kill the slave prematurely (by making them wait until the
   *  slave registers) and remove the TEST_MODE escape.
   */
  protected void slaveQuitDuringStartup(int status) {
    super.slaveQuitDuringStartup(status);
    _numAttempts++;  // no synchronization since this is the only place that _numAttempts is modified
    if (Utilities.TEST_MODE || _numAttempts < MAX_COUNT) return;  // Some tests kill the slave immediately after it starts.

    // The slave JVM is not enabled after this to prevent an infinite loop of attempted startups
    _restart = false;
    
    // Signal that an internal error occurred
    String msg = "Interpreter JVM exited before registering, status: " + status;
    IllegalStateException e = new IllegalStateException(msg);
    new edu.rice.cs.drjava.ui.DrJavaErrorHandler().handle(e);
  }
  
  /** Called if the slave JVM dies before it is able to register.
   *  @param cause The Throwable which caused the slave to die.
   */
  public void errorStartingSlave(Throwable cause) throws RemoteException {
    new edu.rice.cs.drjava.ui.DrJavaErrorHandler().handle(cause);
  }
  
  /** This method is called by the interpreter JVM if it cannot be exited.
   *  @param th The Throwable thrown by System.exit
   */
  public void quitFailed(Throwable th) throws RemoteException {
    _interactionsModel.interpreterResetFailed(th);
    _cleanlyRestarting = false;
  }
  
  /** Returns whether a JVM is currently starting.  This override widens the visibility of the method. */
  public boolean isStartupInProgress() { return super.isStartupInProgress(); }
  
  /** Called when Interpreter JVM connects to us after being started. Assumes that _masterJVMLock is already held. */
  protected void handleSlaveConnected() {
    // we reset the enabled flag since, unless told otherwise via
    // killInterpreter(false), we want to automatically respawn
//    System.out.println("handleSlaveConnected() called in MainJVM");  // DEBUG
    _restart = true;
    _cleanlyRestarting = false;
    
    Boolean allowAccess = DrJava.getConfig().getSetting(OptionConstants.ALLOW_PRIVATE_ACCESS);
    setPrivateAccessible(allowAccess.booleanValue());
    
//    System.out.println("Calling interpreterReady(" + _workDir + ") called in MainJVM");  // DEBUG
    _interactionsModel.interpreterReady(_workDir);
    _junitModel.junitJVMReady();
    
    _log.log("Main JVM Thread for slave connection is: " + Thread.currentThread());
    
    // notify a thread that is waiting in ensureInterpreterConnected
    synchronized(_interpreterLock) { _interpreterLock.notifyAll(); } 
  }

  /** ReEnables restarting the slave if it has been turned off by repeated startup failures. */
  public void enableRestart() { _restart = true; }
  
  /** Returns the visitor to handle an InterpretResult. */
  protected InterpretResultVisitor<Object> getResultHandler() { return _handler; }
  
  /** Returns the debug port to use, as specified by the model. Returns -1 if no usable port could be found. */
  protected int getDebugPort() {
    int port = -1;
    try {  port = _interactionsModel.getDebugPort(); }
    catch (IOException ioe) {
      /* Can't find port; don't use debugger */
    }
    return port;
  }
  
  /** Return whether to allow assertions in the InterpreterJVM. */
  protected boolean allowAssertions() {
    String version = System.getProperty("java.version");
    return (_allowAssertions && (version != null) && ("1.4.0".compareTo(version) <= 0));
  }
  
  /** Lets the model know if any exceptions occur while communicating with the Interpreter JVM. */
  private void _threwException(Throwable t) {
    String shortMsg = null;
    if ((t instanceof ParseError) && ((ParseError) t).getParseException() != null) 
      shortMsg = ((ParseError) t).getMessage();  // in this case, getMessage is equivalent to getShortMessage
    _interactionsModel.replThrewException(t.getClass().getName(), t.getMessage(), StringOps.getStackTrace(t), shortMsg);                                    ;
  } 
  
  /** Sets the interpreter to allow access to private members. TODO: synchronize? */
  public void setPrivateAccessible(boolean allow) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (!_restart) return;
    
    InterpreterJVMRemoteI slave = ensureInterpreterConnected();
    try { slave.setPrivateAccessible(allow); }
    catch (RemoteException re) { _threwException(re); }
  }
  
  /** If an interpreter has not registered itself, this method will block until one does.*/
  public InterpreterJVMRemoteI ensureInterpreterConnected() {
//    _log.log("ensureInterpreterConnected called by Main JVM");
    try {
      synchronized(_interpreterLock) {
        /* Now we silently fail if interpreter is disabled instead of throwing an exception. This situation
         * occurs only in test cases and when DrJava is about to quit. 
         */
        if (! _restart) { throw new IllegalStateException("Interpreter is disabled"); }
        InterpreterJVMRemoteI slave = _interpreterJVM();
        while (slave == null) {
//          _log.log("interpreter is null in Main JVM, waiting for it to register");
          _interpreterLock.wait();
          slave = _interpreterJVM();
        }
        
//        _log.log("interpreter " + interp + " registered in Main JVM");
        return slave;
      }
    }
    catch (InterruptedException ie) { throw new UnexpectedException(ie); }
  }
  
  /** Asks the main jvm for input from the console.
    * @return the console input
    */
  public String getConsoleInput() { 
    String s = _interactionsModel.getConsoleInput(); 
//    System.err.println("MainJVM.getConsoleInput() returns '" + s + "'");
    return s; 
  }
  
  /**
   * Peforms the appropriate action to return any type of result
   * from a call to interpret back to the GlobalModel.
   */
  private class ResultHandler implements InterpretResultVisitor<Object> {
    /** Lets the model know that void was returned.
      * @return null
      */
    public Object forVoidResult(VoidResult that) {
      _interactionsModel.replReturnedVoid();
      return null;
    }
    
    /** Returns a value result (as a String) back to the model.
     *  @return null
     */
    public Object forValueResult(ValueResult that) {
      String result = that.getValueStr();
      String style = that.getStyle();
      _interactionsModel.replReturnedResult(result, style);
      return null;
    }
    
    /** Returns an exception back to the model.
     *  @return null
     */
    public Object forExceptionResult(ExceptionResult that) { /**/
      _interactionsModel.replThrewException(that.getExceptionClass(), that.getExceptionMessage(), that.getStackTrace(),
                                            that.getSpecialMessage());
      return null;
    }
    
    /** Indicates there was a syntax error to the model.
     *  @return null
     */
    public Object forSyntaxErrorResult(SyntaxErrorResult that) {
      _interactionsModel.replReturnedSyntaxError(that.getErrorMessage(), that.getInteraction(), that.getStartRow(),
                                                 that.getStartCol(), that.getEndRow(), that.getEndCol() );
      return null;
    }
    
    public Object forInterpreterBusy(InterpreterBusy that) {
      throw new UnexpectedException("MainJVM.interpret() called when InterpreterJVM was busy!");
    }
  }
  
  /** InteractionsModel which does not react to events. */
  public static class DummyInteractionsModel implements InteractionsModelCallback {
    public int getDebugPort() throws IOException { return -1; }
    public void replSystemOutPrint(String s) { }
    public void replSystemErrPrint(String s) { }
    public String getConsoleInput() {
      throw new IllegalStateException("Cannot request input from dummy interactions model!");
    }
    public void setInputListener(InputListener il) {
      throw new IllegalStateException("Cannot set the input listener of dummy interactions model!");
    }
    public void changeInputListener(InputListener from, InputListener to) {
      throw new IllegalStateException("Cannot change the input listener of dummy interactions model!");
    }
    public void replReturnedVoid() { }
    public void replReturnedResult(String result, String style) { }
    public void replThrewException(String exceptionClass, String message, String stackTrace, String specialMessage) { }
    public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol, int endRow,
                                        int endCol) { }
    public void replCalledSystemExit(int status) { }
    public void interpreterResetting() { }
    public void interpreterResetFailed(Throwable th) { }
    public void interpreterReady(File wd) { }
    public void slaveJVMUsed() { }
  }
  
  /** JUnitModel which does not react to events. */
  public static class DummyJUnitModel implements JUnitModelCallback {
    public void nonTestCase(boolean isTestAll) { }
    public void classFileError(ClassFileError e) { }
    public void testSuiteStarted(int numTests) { }
    public void testStarted(String testName) { }
    public void testEnded(String testName, boolean wasSuccessful, boolean causedError) { }
    public void testSuiteEnded(JUnitError[] errors) { }
    public File getFileForClassName(String className) { return null; }
    public Iterable<File> getClassPath() { return IterUtil.empty(); }
    public void junitJVMReady() { }
  }
  
  /** DebugModelCallback which does not react to events. */
  public static class DummyDebugModel implements DebugModelCallback {
    public void notifyDebugInterpreterAssignment(String name) {
    }
  }
}
