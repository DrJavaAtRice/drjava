/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.util.List;
import java.util.ArrayList;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.debug.DebugModelCallback;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;

import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.plt.concurrent.StateMonitor;

import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.classloader.ClassFileError;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * <p>Manages a remote JVM.  Includes methods for communication in both directions: MainJVMRemoteI
 * provides callbacks allowing the remote JVM to access the model; and a variety of delegating
 * methods wrap calls to the InterpreterJVMRemoteI methods, taking care of any RMI-related errors.
 * In the case of errors, these interpreter-delegating methods communicate the failure via the
 * return value.  (Note that it is impossible to guarantee success of these methods -- the remote
 * process may exit arbitrarily at any time -- and clients should behave gracefully when failures
 * occur.)</p>
 * 
 * <p>The current design is flawed: strictly speaking, two sequential interpreter-delegating calls to
 * this object may communicate with <em>different</em> JVMs if the remote JVM happens to reset in
 * the interim.  A better design would return a separate object for interfacing with each unique remote
 * JVM.  In this way, clients would know that all calls to a certain object would be forwarded to
 * the same remote JVM.</p>
 * 
 * @version $Id$
 */
public class MainJVM extends AbstractMasterJVM implements MainJVMRemoteI {
  
  /** Number of slave startup failures allowed before aborting the startup process. */
  private static final int MAX_STARTUP_FAILURES = 3;
  
  /** Number of milliseconds to block while waiting for an InterpreterJVM stub. */
  private static final int STARTUP_TIMEOUT = 10000;  
  
  /** Contains the current InterpreterJVM stub, or {@code null} if it is not running. */
  private final StateMonitor<InterpreterJVMRemoteI> _interpreterJVM;
  
  /** Records state of slaveJVM (interpreterJVM); used to suppress restartInteractions on a fresh JVM */
  private volatile boolean _slaveJVMUsed = false;
  
  /** Instance of inner class to handle interpret result. */
  private final ResultHandler _handler = new ResultHandler();
  
  /** Listens to interactions-related events. */
  private volatile InteractionsModelCallback _interactionsModel;
  
  /** Listens to JUnit-related events. */
  private volatile JUnitModelCallback _junitModel;
  
  /** Listens to debug-related events */
  private volatile DebugModelCallback _debugModel;
  
  /**
   * Controls access to all startup-related options and state, for code that writes to these fields or that reads
   * multiple fields (and requires them to be consistent).
   */
  private final Object _startupLock = new Object();
  
  /* startup state */
  
  /** This flag is set to false to inhibit the automatic restart of the JVM. */
  private volatile boolean _restart = true;
  
  /** This flag is set to remember that the JVM is cleanly restarting, so that the replCalledSystemExit method
    * does not need to be called.
    */
  private volatile boolean _cleanlyRestarting = false;
  
  /** Number of startup attempts that have been initiated but not completed.  0 means not starting up. */
  private volatile int _startupAttempts = 0;
  
  /** Signaled whenever no startup is in progress. */
  private final CompletionMonitor _startupComplete = new CompletionMonitor(true);
  
  /* JVM execution options */
  
  /** Whether to allow "assert" statements to run in the remote JVM. */
  private volatile boolean _allowAssertions = false;
  
  /** Class path to use for starting the interpreter JVM */
  private volatile Iterable<File> _startupClassPath;
  
  /** Working directory for slave JVM */
  private volatile File _workingDir;
  
  /** Creates a new MainJVM to interface to another JVM;  the MainJVM has a link to the partially initialized 
    * global model.  The MainJVM but does not automatically start the Interpreter JVM.  Callers must set the
    * InteractionsModel and JUnitModel and then call startInterpreterJVM().
    */
  public MainJVM(File wd) {
    super(InterpreterJVM.class.getName());
    _workingDir = wd;
    _interactionsModel = new DummyInteractionsModel();
    _junitModel = new DummyJUnitModel();
    _debugModel = new DummyDebugModel();
    _interpreterJVM = new StateMonitor<InterpreterJVMRemoteI>(null);
    _startupClassPath = ReflectUtil.SYSTEM_CLASS_PATH;
  }
  
  
  /*
   * === Startup and shutdown methods ===
   */
  
  /** Starts the interpreter if it's not running already. */
  public void startInterpreterJVM() {
    debug.logStart();

    boolean alreadyStarted;
    synchronized (_startupLock) {
      alreadyStarted = (_interpreterJVM.value() != null || !_startupComplete.isSignaled());
      if (!alreadyStarted) {
        _startupComplete.reset();
        _startupAttempts = 1;
      }
    }
    if (alreadyStarted) { debug.log("Already started"); }
    else { _doStartup(); }
    debug.logEnd();
  }
    
  /**
   * Kills the running interpreter JVM, and restarts with working directory {@code wd} if {@code wd != null}.
   * If {@code wd == null}, the interpreter is not restarted.  (Note that killing without restarting will
   * cause all methods that delegate to the interpreter JVM to fail, returning "false" or "none".)
   */
  public void killInterpreterJVM(File wd) {
    debug.logStart();
    _startupComplete.attemptEnsureSignaled();
    boolean restart = (wd != null);
    InterpreterJVMRemoteI current;
    synchronized (_startupLock) {
      _workingDir = wd;
      _restart = restart;
      _cleanlyRestarting = true;
      current = _interpreterJVM.getAndSet(null);
    }
    if (current != null) {
      if (restart) _interactionsModel.interpreterResetting();
      quitSlave();
      // new slave JVM is started by in handleSlaveQuit()
    }
    debug.logEnd();
  }
  
  /**
   * Kill the interpreter JVM, do not restart it, and terminate the RMI server associated with this object.
   * May be useful when a number of different MainJVM objects are created (such as when running tests).
   */
  public void dispose() {
    debug.logStart();
    killInterpreterJVM(null);
    if (!isDisposed()) { super.dispose(); }
    debug.logEnd();
  }
  
  
  /*
   * === AbstractMasterJVM methods ===
   */

  /**
   * Callback for when the slave JVM has connected, and the bidirectional communications link has been 
   * established.  Provides access to the newly-created slave JVM.
   */
  protected void handleSlaveConnected(SlaveRemote newSlave) {
    InterpreterJVMRemoteI slaveCast = (InterpreterJVMRemoteI) newSlave;
    Boolean allowAccess = DrJava.getConfig().getSetting(OptionConstants.ALLOW_PRIVATE_ACCESS);
    try { slaveCast.setPrivateAccessible(allowAccess); }
    catch (RemoteException re) { _handleRemoteException(re); }

    synchronized (_startupLock) {
      _restart = true;
      _cleanlyRestarting = false;
      _startupAttempts = 0;
      _slaveJVMUsed = false;
      _interpreterJVM.set(slaveCast); // initialized after all other state is set
      _startupComplete.signal();
    }
    _interactionsModel.interpreterReady(_workingDir);
    _junitModel.junitJVMReady();
  }
  
  /**
   * Callback for when the slave JVM has quit.
   * @param status The exit code returned by the slave JVM.
   */
  protected void handleSlaveQuit(int status) {
    debug.logValue("Slave quit", "status", status);
    boolean wasRestarting;
    boolean doRestart;
    synchronized (_startupLock) {
      wasRestarting = _cleanlyRestarting;
      _cleanlyRestarting = false;
      doRestart = _restart;
      _interpreterJVM.set(null);
    }
    if (!wasRestarting) { _interactionsModel.replCalledSystemExit(status); }
    if (doRestart) {
      // We have already fired this event if we are cleanly restarting
      if (!wasRestarting) { _interactionsModel.interpreterResetting(); }
      startInterpreterJVM();
    }
  }
  
  
  /**
   * Callback for when the slave JVM fails to either run or respond to {@link SlaveRemote#start}.
   * @param e  Exception that occurred during startup.
   */
  protected void handleSlaveWontStart(Exception e) {
    boolean giveUp;
    synchronized (_startupLock) {
      _startupAttempts++;
      debug.logValue("startupFailures", _startupAttempts);
      giveUp = _startupAttempts > MAX_STARTUP_FAILURES;
      if (giveUp) {
        _restart = false;
        _startupAttempts = 0;
        _startupComplete.signal();
      }
    }
    if (giveUp) {
      debug.log("Giving up on restart");
      _interactionsModel.interpreterWontStart(e);
    }
    else {
      debug.logStart("trying to start interpreter again");
      _doStartup();
      debug.logEnd("trying to start interpreter again");
    }
  }
  

  /*
   * === MainJVMRemoteI methods ===
   */
  
  // TODO: export other objects, such as the interactionsModel, thus avoiding the need to delegate here?
  
  /** Forwards a call to System.err from InterpreterJVM to the local InteractionsModel.
    * @param s String that was printed in the other JVM
    */
  public void systemErrPrint(String s) {
    debug.logStart();
    _interactionsModel.replSystemErrPrint(s);
//    Utilities.clearEventQueue();               // wait for event queue task to complete
    debug.logEnd();
  }
  
  /** Forwards a call to System.out from InterpreterJVM to the local InteractionsModel.
    * @param s String that was printed in the other JVM
    */
  public void systemOutPrint(String s) {
    debug.logStart();
    _interactionsModel.replSystemOutPrint(s); 
//    Utilities.clearEventQueue();                // wait for event queue task to complete
    debug.logEnd();
  }
  
  /** Asks the main jvm for input from the console.
   * @return the console input
   */
  public String getConsoleInput() { 
    String s = _interactionsModel.getConsoleInput(); 
    // System.err.println("MainJVM.getConsoleInput() returns '" + s + "'");
    return s; 
  }
 
  /** This method is called by the interpreter JVM if it cannot be exited.
   * @param th The Throwable thrown by System.exit
   */
  public void quitFailed(Throwable th) {
    _interactionsModel.interpreterResetFailed(th);
    _cleanlyRestarting = false;
  }
 
  /** Called if JUnit is invoked on a non TestCase class.  Forwards from the other JVM to the local JUnit model.
   * @param isTestAll whether or not it was a use of the test all button
   */
  public void nonTestCase(boolean isTestAll) {
    _junitModel.nonTestCase(isTestAll);
  }
 
  /** Called if the slave JVM encounters an illegal class file in testing.  Forwards from
   * the other JVM to the local JUnit model.
   * @param e the ClassFileError describing the error when loading the class file
   */
  public void classFileError(ClassFileError e) {
    _junitModel.classFileError(e);
  }
  
  /** Called to indicate that a suite of tests has started running.
   * Forwards from the other JVM to the local JUnit model.
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) {
    _slaveJVMUsed = true;
    _junitModel.testSuiteStarted(numTests);
  }

  /** Called when a particular test is started.  Forwards from the slave JVM to the local JUnit model.
   * @param testName The name of the test being started.
   */
  public void testStarted(String testName) {
    _slaveJVMUsed = true;
    _junitModel.testStarted(testName);
  }
 
  /** Called when a particular test has ended. Forwards from the other JVM to the local JUnit model.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError) {
    _junitModel.testEnded(testName, wasSuccessful, causedError);
  }
 
  /** Called when a full suite of tests has finished running. Forwards from the other JVM to the local JUnit model.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
    _junitModel.testSuiteEnded(errors);
  }
 
  /** Called when the JUnitTestManager wants to open a file that is not currently open.
   * @param className the name of the class for which we want to find the file
   * @return the file associated with the given class
   */
  public File getFileForClassName(String className) {
    return _junitModel.getFileForClassName(className);
  }
 
//  /** Notifies the main jvm that an assignment has been made in the given debug interpreter.
//   * Does not notify on declarations.
//   *
//   * This method is not currently necessary, since we don't copy back values in a debug interpreter until the thread
//   * has resumed.
//   *
//   * @param name the name of the debug interpreter
//   */
//   public void notifyDebugInterpreterAssignment(String name) {
//   }
 
    
  /*
   * === Local getters and setters ===
   */
  
  public boolean slaveJVMUsed() { return _slaveJVMUsed; }
  
  /** Provides an object to listen to interactions-related events. */
  public void setInteractionsModel(InteractionsModelCallback model) { _interactionsModel = model; }
  
  /** Provides an object to listen to test-related events.*/
  public void setJUnitModel(JUnitModelCallback model) { _junitModel = model; }
  
  /** Provides an object to listen to debug-related events.
    * @param model the debug model
    */
  public void setDebugModel(DebugModelCallback model) { _debugModel = model; }
  
  /** Sets whether the remote JVM will run "assert" statements after the next restart. */
  public void setAllowAssertions(boolean allow) { _allowAssertions = allow; }
  
  /**
   * Sets the class path to use for starting the interpreter JVM. Must include the classes for the interpreter.
   * @param classPath Class path for the interpreter JVM
   */
  public void setStartupClassPath(String classPath) {
    _startupClassPath = IOUtil.parsePath(classPath);
  }
  
  /** Re-enables restarting the slave if it has been turned off by repeated startup failures. */
  public void enableRestart() {
    synchronized (_startupLock) { _restart = true; }
  }
  
  /** Declared as a getter in order to allow subclasses to override the standard behavior. */
  protected InterpretResult.Visitor<Void> resultHandler() { return _handler; }
  
  
  /* === Wrappers for InterpreterJVMRemoteI methods === */

  /** Interprets string s in the remote JVM.  Blocks until the interpreter is connected and evaluation completes.
    * @return  {@code true} if successful; {@code false} if the subprocess is unavailable, the subprocess dies
    *          during the call, or an unexpected exception occurs.
    */
  public boolean interpret(final String s) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try {
      debug.logStart("Interpreting " + s);
      _slaveJVMUsed = true;
      InterpretResult result = remote.interpret(s);
      result.apply(resultHandler());
      debug.logEnd("result", result);
      return true;
    }
    catch (RemoteException e) { debug.logEnd(); _handleRemoteException(e); return false; }
  }
  
  /**
   * Gets the string representation of the value of a variable in the current interpreter, or "none"
   * if the remote JVM is unavailable or an error occurs.  Blocks until the interpreter is connected.
   * @param var the name of the variable
   */
  public Option<String> getVariableToString(String var) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return Option.none(); }
    try { return Option.some(remote.getVariableToString(var)); }
    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
  }
  
  /**
   * Gets the class name of a variable's type in the current interpreter, or "none"
   * if the remote JVM is unavailable or an error occurs.  Blocks until the interpreter is connected.
   * @param var the name of the variable
   */
  public Option<String> getVariableType(String var) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return Option.none(); }
    try { return Option.some(remote.getVariableType(var)); }
    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
  }
  
  /**
   * Blocks until the interpreter is connected.  Returns {@code true} if the change was successfully passed to
   * the remote JVM.
   */
  public boolean addProjectClassPath(File f) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.addProjectClassPath(f); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /**
   * Blocks until the interpreter is connected.  Returns {@code true} if the change was successfully passed to
   * the remote JVM.
   */
  public boolean addBuildDirectoryClassPath(File f) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.addBuildDirectoryClassPath(f); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /**
   * Blocks until the interpreter is connected.  Returns {@code true} if the change was successfully passed to
   * the remote JVM.
   */
  public boolean addProjectFilesClassPath(File f) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.addProjectFilesClassPath(f); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /**
   * Blocks until the interpreter is connected.  Returns {@code true} if the change was successfully passed to
   * the remote JVM.
   */
  public boolean addExternalFilesClassPath(File f) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.addExternalFilesClassPath(f); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /**
   * Blocks until the interpreter is connected.  Returns {@code true} if the change was successfully passed to
   * the remote JVM.
   */
  public boolean addExtraClassPath(File f) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.addExtraClassPath(f); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /** Returns the current class path of the interpreter as a list of unique entries.  The result is "none"
   * if the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
    */
  public Option<Iterable<File>> getClassPath() {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return Option.none(); }
    try { return Option.some(remote.getClassPath()); }
    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
  }
  
  /** Sets the Interpreter to be in the given package.  Blocks until the interpreter is connected.
    * @param packageName Name of the package to enter.
    */
  public boolean setPackageScope(String packageName) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.interpret("package " + packageName + ";"); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCase
    * classes (by loading them).  Blocks until the interpreter is connected and the operation completes.
    * @param classNames the class names to run in a test
    * @param files the associated file
    * @return the class names that are actually test cases
    */
  public Option<List<String>> findTestClasses(List<String> classNames, List<File> files) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return Option.none(); }
    try { return Option.some(remote.findTestClasses(classNames, files)); }
    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
  }
  
  /**
   * Runs the JUnit test suite already cached in the Interpreter JVM.  Blocks until the remote JVM is available.
   * Returns {@code false} if no test suite is cached, the remote JVM is unavailable, or an error occurs.
   */
  public boolean runTestSuite() { 
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { return remote.runTestSuite(); }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
//  /** Updates the security manager in slave JVM */
//  public void enableSecurityManager() throws RemoteException {
//    _interpreterJVM().enableSecurityManager();
//  }
//  
//  /** Updates the security manager in slave JVM */
//  public void disableSecurityManager() throws RemoteException{
//    _interpreterJVM().disableSecurityManager();
//  }
  
  
  /**
   * Adds a named interpreter to the list.  The result is {@code false} if the remote JVM is unavailable or
   * if an exception occurs.  Blocks until the interpreter is connected.
   * @param name the unique name for the interpreter
   * @throws IllegalArgumentException if the name is not unique
   */
  public boolean addInterpreter(String name) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.addInterpreter(name); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /** Removes the interpreter with the given name, if it exists.  The result is {@code false} if
   * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
    * @param name Name of the interpreter to remove
    */
  public boolean removeInterpreter(String name) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.removeInterpreter(name); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
  
  /** Sets the current interpreter to the one specified by name.  The result is "none" if
   * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
    * @param name the unique name of the interpreter to set active
    * @return Status flags: whether the current interpreter changed, and whether it is busy; or "none" on an error
    */
  public Option<Pair<Boolean, Boolean>> setActiveInterpreter(String name) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return Option.none(); }
    try { return Option.some(remote.setActiveInterpreter(name)); }
    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
  }
  
  /** Sets the default interpreter to be the current one.  The result is "none" if
   * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
    * @return Status flags: whether the current interpreter changed, and whether it is busy; or "none" on an error
    */
  public Option<Pair<Boolean, Boolean>> setToDefaultInterpreter() {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return Option.none(); }
    try { return Option.some(remote.setToDefaultInterpreter()); }
    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
  }
  
  /** Sets the interpreter to allow access to private members.  The result is {@code false} if
   * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
   */
  public boolean setPrivateAccessible(boolean allow) {
    InterpreterJVMRemoteI remote = _accessInterpreterJVM();
    if (remote == null) { return false; }
    try { remote.setPrivateAccessible(allow); return true; }
    catch (RemoteException e) { _handleRemoteException(e); return false; }
  }
   
  
  /*
   * === Helper methods ===
   */
  
  /** Call invokeSlave with the appropriate JVMBuilder.  Defined here to allow for multiple attempts. */
  private void _doStartup() {
    Iterable<File> classPath;
    List<String> jvmArgs = new ArrayList<String>();
    File dir;
    synchronized (_startupLock) {
      if (_allowAssertions) { jvmArgs.add("-ea"); }
      classPath = _startupClassPath;
      dir = _workingDir;
    }
    // TODO: Eliminate NULL_FILE.  It is a bad idea!  The correct behavior when it is used always depends on
    // context, so it can never be treated transparently.  In this case, the process won't start.
    if (dir == FileOps.NULL_FILE) { dir = IOUtil.WORKING_DIRECTORY; }
    int debugPort = _getDebugPort();
    if (debugPort > -1) {
      jvmArgs.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + debugPort);
      jvmArgs.add("-Xdebug");
      jvmArgs.add("-Xnoagent");
      jvmArgs.add("-Djava.compiler=NONE");
    }
    String slaveMemory = DrJava.getConfig().getSetting(OptionConstants.SLAVE_JVM_XMX);
    if (!"".equals(slaveMemory) && !OptionConstants.heapSizeChoices.get(0).equals(slaveMemory)) {
      jvmArgs.add("-Xmx" + slaveMemory + "M");
    }
    String slaveArgs = DrJava.getConfig().getSetting(OptionConstants.SLAVE_JVM_ARGS);
    if (PlatformFactory.ONLY.isMacPlatform()) {
      jvmArgs.add("-Xdock:name=Interactions");
    }
    jvmArgs.addAll(ArgumentTokenizer.tokenize(slaveArgs));
    
    invokeSlave(new JVMBuilder(classPath).directory(dir).jvmArguments(jvmArgs));
  }
  
  /** Returns the debug port to use, as specified by the model. Returns -1 if no usable port could be found. */
  private int _getDebugPort() {
    int port = -1;
    try {  port = _interactionsModel.getDebugPort(); }
    catch (IOException ioe) {
      /* Can't find port; don't use debugger */
    }
    return port;
  }
  
  /** If an interpreter has not registered itself, this method will block until one does.*/
  private InterpreterJVMRemoteI _accessInterpreterJVM() {
    if (!_restart) { return _interpreterJVM.value(); }
    else { return _interpreterJVM.attemptEnsureNotState(null, STARTUP_TIMEOUT); }
  }

  /** Lets the model know if any exceptions occur while communicating with the Interpreter JVM. */
  private void _handleRemoteException(RemoteException e) {
    if (e instanceof UnmarshalException && e.getCause() instanceof EOFException) {
      /* Interpreter JVM has disappeared (perhaps reset); just ignore the error. */
    }
    else { DrJavaErrorHandler.record(e); }
  }
  
  
  /*
   * Helper classes
   */

  /** Performs the appropriate action to return any type of result from a call to interpret back to the GlobalModel. */
  private class ResultHandler implements InterpretResult.Visitor<Void> {
    /** Lets the model know that void was returned. */
    public Void forNoValue() {
      _interactionsModel.replReturnedVoid();
      return null;
    }
    
    /** Calls replReturnedResult() */
    public Void forObjectValue(String objString) {
      _interactionsModel.replReturnedResult(objString, InteractionsDocument.OBJECT_RETURN_STYLE);
      return null;
    }
    
    /** Calls replReturnedResult() */
    public Void forStringValue(String s) {
      _interactionsModel.replReturnedResult('"' + s + '"', InteractionsDocument.STRING_RETURN_STYLE);
      return null;
    }
    
    /** Calls replReturnedResult() */
    public Void forCharValue(Character c) {
      _interactionsModel.replReturnedResult("'" + c + "'", InteractionsDocument.CHARACTER_RETURN_STYLE);
      return null;
    }
    
    /** Calls replReturnedResult() */
    public Void forNumberValue(Number n) {
      _interactionsModel.replReturnedResult(n.toString(), InteractionsDocument.NUMBER_RETURN_STYLE);
      return null;
    }
    
    /** Calls replReturnedResult() */
    public Void forBooleanValue(Boolean b) {
      _interactionsModel.replReturnedResult(b.toString(), InteractionsDocument.OBJECT_RETURN_STYLE);
      return null;
    }
    
    /** Calls replThrewException() */
    public Void forException(String msg) {
      // TODO: restore location/syntax highlighting functionality
      _interactionsModel.replThrewException(msg);
      return null;
    }
    
    public Void forUnexpectedException(Throwable t) {
      _interactionsModel.replReturnedVoid();
      throw new UnexpectedException(t);
    }
    
    public Void forBusy() {
      _interactionsModel.replReturnedVoid();
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
    public void replThrewException(String message) { }
    public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol, int endRow,
                                        int endCol) { }
    public void replCalledSystemExit(int status) { }
    public void interpreterResetting() { }
    public void interpreterResetFailed(Throwable th) { }
    public void interpreterWontStart(Exception e) { }
    public void interpreterReady(File wd) { }
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
