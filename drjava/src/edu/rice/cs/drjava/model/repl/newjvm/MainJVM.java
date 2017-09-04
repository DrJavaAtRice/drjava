/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.awt.EventQueue;

import java.rmi.*;
import java.io.*;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import edu.rice.cs.drjava.DrScala;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.newjvm.Interpreter;

import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.ui.DrScalaErrorHandler;

import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.classloader.ClassFileError;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;

import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.plt.concurrent.StateMonitor;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.drjava.config.OptionConstants.*;

/** <p>The JVM communication protocol developed for DrJava is complex; DrJava supports multiple simultaneous
  * interpreters "running" in the JVM [I don't know if more than one can actually be executing in the slave at a time]. 
  * DrScala has exactly one interpreter so we can use a much simpler protocol.  In particular, we need to maintain
  * a volatile flag variable (_busy) in this class that designates whether or not the interpreter is busy performing
  * a computation.  Some DrScala actions, notably "Reset Interactions", can take place while the interpreter is busy.
  * When this situation occurs, the slave JVM must be deconnected (it will co-operatively stop) and a new slaveJVM 
  * running a new fresh interpreter must be created.  To perform this process, we will rely on the old proven DrJava 
  * infrastructure (which in principle is more baroque than we need).  We have slightly revised that infrastructure by
  * eliminating the notion of a "used" interpreter and recognizing that a slave has only one interpreter.  Since
  * more than one slave can exist at a time (during the process of terminating the current slave and creating a new
  * one), we retain the notion of "getting" the current interpreter.
  * 
  * <p>The old protocol is described in the following comment text.
  * 
  * <p>This class manages a remote JVM.  Includes methods for communication in both directions: MainJVMRemoteI
  * provides callbacks allowing the remote JVM to access the model, and a variety of delegating
  * methods wrap calls to the InterpreterJVMRemoteI methods, taking care of any RMI-related errors.
  * In the case of errors, these interpreter-delegating methods communicate the failure via the
  * return value.  (Note that it is impossible to guarantee success of these methods -- the remote
  * process may exit arbitrarily at any time -- and clients should behave gracefully when failures
  * occur.)</p>
  * 
  * <p> Note that the Option type used in this class is edu.rice.cs.plt.tuple.Option, not edu.rice.cs.drjava.config. 
  * 
  * @version $Id: MainJVM.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public class MainJVM extends AbstractMasterJVM implements MainJVMRemoteI {
  
//  NOTE: The superclass AbstractMasterJVM consists primarily of a state machine implemented as a StateMonitor<State>
//  where State is a private Enum type.  The field name for the state machine is _monitor.  This class implements a
//  different state machine called _stateMonitor of type StateMonitor<Phase> where Phase (formerly State) is a private
//  class.  Although the two state machines are similar, no explicit relation is defined between the two.  These two
//  state machines should obviously be consolidated.

  // Inheriting static final Log _log from AbstractMasterJVM 
  
  /* Note that the MainJVM is created only once
  
  /* TODO: add additional phase to Phase called BusyPhase instead of using a _busy flag */
  private volatile boolean _busy = false;  // flag that records whether the interpreter is busy
  public volatile String _toEval = "";     // the String being evaluated when the interpreter is busy
  
  /** static flag giving existence status of interpreter (slave) JVM.  Since it is static, it can be queried before
    * the MainJVM is created. */
  public static volatile boolean _interpreterJVMStarted = false;
  
  /** Number of slave startup failures allowed before aborting the startup process. */
  private static final int MAX_STARTUP_FAILURES = 3;
  
  /** Number of milliseconds to block while waiting for an InterpreterJVM stub. */
  private static final int STARTUP_TIMEOUT = 20000;  
  
  /** Contains the current InterpreterJVM stub, or {@code null} if it is not running. 
    * NOTE: Phase is NOT the private Enum class inherited from AbstractMasterJVM. */
  private final StateMonitor<Phase> _stateMonitor = new StateMonitor<Phase>(new FreshPhase());
  
  /** Instance of inner class to handle interpret result. We cannot make ResultHandler a singleton class because
    * it depends on the enclosing instance this.  Different copies can have different enclosing instances. */
  private final ResultHandler _handler = new ResultHandler();
  
  /** Listens to interactions-related events. */
  private volatile InteractionsModelCallback _interactionsModel;
  
  /** Listens to JUnit-related events. */
  private volatile JUnitModelCallback _junitModel;
  
  /* JVM execution options */
  
  /** Whether to allow "assert" statements to run in the remote JVM. */
  private volatile boolean _allowAssertions = false;
  
  /* _classPath is initialized to ReflectUtil.SYSTEM_CLASS_PATH and updated only when the interpreter is restarted.
   * in the slave JVM.  In a restart operation, the intended value of the interactions class path is fetched from 
   * the slave JVM and the class path for new slave JVM is stored in _newClassPath. */
  
  /** Caches the current working directory and classPath of the slave JVM; matches the current working directory and
    * classPath in the slave JVM except during startUp and when a reset is taking place. _classpath should only be 
    * updated when the current slave JVM is killed and new slave JVM is started. _classPath and _newClassPath should 
    * never be mutated. */
  private volatile File _workingDir;
  private volatile List<File> _classPath = ReflectUtil.SYSTEM_CLASS_PATH;
  private volatile List<File> _newClassPath = ReflectUtil.SYSTEM_CLASS_PATH;
  
  private volatile GlobalModel _model;
  
  /** Creates a new MainJVM to interface to another JVM; the MainJVM has a link to the partially initialized 
    * global model.  The MainJVM but does not automatically start the Interpreter JVM.  Callers must set the
    * InteractionsModel and JUnitModel and then call startInterpreterJVM().
    */
  public MainJVM(GlobalModel model) {
    super(InterpreterJVM.class.getName());
    /* the intended _workingDir is maintained by the interactions model; it is null in the DefaultInteractionsModel */
     _log.log("MainJVM object created with null workingDir");
    _workingDir = null;
    _model = model;
    _interactionsModel = new DefaultInteractionsModel();
    _junitModel = new DummyJUnitModel();
  }
  
  /* === Boolean methods === */
  public boolean isDisposed() { return _stateMonitor.value() instanceof DisposedPhase; }
  
  /* === Startup and shutdown methods === */
  
  /** Starts the interpreter for the first time. */
  public void startInterpreterJVM() { 
//    Utilities.show("Trying to start SlaveJVM");
    _log.log("In MainJVM, startInterpreterJVM() called; invoking start() on " + _stateMonitor.value());
    _stateMonitor.value().start();  
  }
  
  /** Stop the interpreter if it's currently running.  (Note that, until {@link #startInterpreterJVM} is called
    * again, all methods that delegate to the interpreter JVM will fail, returning "false" or "none".)
    */
  public void stopInterpreterJVM() { 
    _log.log("In MainJVM, stopInterpreterJVM() called; invoking stop() on " + _stateMonitor.value());
    _stateMonitor.value().stop(); 
  }
  
  /** Create a new interpreter JVM.  Has the same effect as {@link #startInterpreterJVM} if no interpreter
    * exists.  If the interpreter exists, it is stopped and a new slave JVM with a new interpreter is started.
    * Only runs in the event thread.
    */
  public void restartInterpreterJVM() {
    assert EventQueue.isDispatchThread();
    _log.log("restartInterpreter called; _interpreterJVMStarted = " + _interpreterJVMStarted);

    _newClassPath = getInteractionsClassPath();
    _log.log("***** _newClassPath = " + _newClassPath);
    
    _log.log("In MainJVM, restartInterpreterJVM() called; invoking restart() on " + _stateMonitor.value());
    _stateMonitor.value().restart();
    /* The following are inconsistent with startInterpreter which is supposed to be equivalent if no interpreter exists */
//    _interactionsModel.documentReset();
//    _interactionsModel._notifyInterpreterReady();
  }
  
  /** Stop the interpreter JVM, do not restart it, and terminate the RMI server associated with this object.
    * May be useful when a number of different MainJVM objects are created (such as when running tests).
    */
  public void dispose() { _stateMonitor.value().dispose(); }
  
  
  /* === AbstractMasterJVM methods === */
  
  /** Callback for when the slave JVM has connected, and the bidirectional communications link has been 
    * established.  Provides access to the newly-created slave JVM.
    */
  protected void handleSlaveConnected(SlaveRemote newSlave) {
    InterpreterJVMRemoteI slaveCast = (InterpreterJVMRemoteI) newSlave;
    _log.log("In MainJVM, handleSlaveConnected(" + newSlave + ") called; invoking started(" + slaveCast + ") on " +
             _stateMonitor.value());
    _stateMonitor.value().started(slaveCast);
  }
  
  /** Callback for when the slave JVM has quit.
    * @param status The exit code returned by the slave JVM.
    */
  protected void handleSlaveQuit(int status) {
//    debug.logValue("Slave quit", "status", status);
    _log.log("In MainJVM, handleSlaveQuit() called; invoking stopped(" + status + ") on " + _stateMonitor.value());
    _stateMonitor.value().stopped(status);  // This event is very important
  }
  
  /** Callback for when the slave JVM fails to either run or respond to {@link SlaveRemote#start}.
    * @param e  Exception that occurred during startup.
    */
  protected void handleSlaveWontStart(Exception e) {
    debug.log("In MainJVM, handleSlaveWontStart called; invoking startFailed(" + e + ") on " + _stateMonitor.value());
    _stateMonitor.value().startFailed(e);
  }
  
  /* === MainJVMRemoteI methods === */
  
  // TODO: export other objects, such as the interactionsModel, thus avoiding the need to delegate here?
  
  /** Forwards a call to System.err from InterpreterJVM to the local InteractionsModel.
    * @param s String that was printed in the other JVM
    */
  public void systemErrPrint(String s) {
//    debug.logStart();
    _interactionsModel.replSystemErrPrint(s);
//    Utilities.clearEventQueue();               // wait for event queue task to complete
//    debug.logEnd();
  }
  
  /** Forwards a call to System.out from InterpreterJVM to the local InteractionsModel.
    * @param s String that was printed in the other JVM
    */
  public void systemOutPrint(String s) {
//    debug.logStart();
    _interactionsModel.replSystemOutPrint(s); 
//    Utilities.clearEventQueue();                // wait for event queue task to complete
//    debug.logEnd();
  }
  
  /** Asks the main jvm for input from the console.
    * @return the console input
    */
  public String getConsoleInput() { 
    String s = _interactionsModel.getConsoleInput(); 
    // System.err.println("MainJVM.getConsoleInput() returns '" + s + "'");
    return s; 
  }
  
  /** Called if JUnit is invoked on a non TestCase class.  Forwards from the other JVM to the local JUnit model.
    * @param isTestAll whether or not it was a use of the test all button
    * @param didCompileFail whether or not a compile before this JUnit attempt failed
    */
  public void nonTestCase(boolean isTestAll, boolean didCompileFail) {
    _junitModel.nonTestCase(isTestAll, didCompileFail);
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
    _junitModel.testSuiteStarted(numTests);
  }
  
  /** Called when a particular test is started.  Forwards from the slave JVM to the local JUnit model.
    * @param testName The name of the test being started.
    */
  public void testStarted(String testName) {
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
  
  /* === Local getters and setters === */
  
  /** Provides an object to listen to interactions-related events. */
  public void setInteractionsModel(InteractionsModel model) { _interactionsModel = model; }
  
  /** Provides an object to listen to test-related events.*/
  public void setJUnitModel(JUnitModelCallback model) { _junitModel = model; } 
  
  /** Sets whether the remote JVM will run "assert" statements after the next restart. */
  public void setAllowAssertions(boolean allow) { _allowAssertions = allow; }
  
//  /** Sets the class path to match that of the that in the starting slave JVM. Must include the classes for the 
//    * interpreter.
//    * @param classPath Class path for the interpreter JVM
//    */
//  public void setClassPath(String classPath) {
//    _classPath = CollectUtil.makeList(IOUtil.parsePath(classPath));
//  }
  
  /** Gets the cached value of the working directory for the slave JVM; should be accurate except when hardReset is
    * in progress. */
  public File getWorkingDirectory() { return _workingDir; }
  
  /** Sets the working directory for the slave JVM; should only be changed as the working directory is changed in the
    * slave JVM; the assumption is that this field is updated ahead of resetting interactions in the slave. */
  public void setWorkingDirectory(File dir) { _workingDir = dir; }
  
  /** Declared as a getter in order to allow subclasses to override the standard behavior. */
  protected InterpretResult.Visitor<String> resultHandler() { return _handler; }
  
  /* === Wrappers for InterpreterJVMRemoteI methods === */
  
  /** Interprets string s in the remote JVM.  Blocks until the interpreter is connected and evaluation completes.
    * The result of interpretation is processed by resultHandler(), a visitor on type InterpreterResult.  If
    * result processing is performed by a single thread, this processing is done before the method returns.v
    * @return  {@code true} if successful; {@code false} if the subprocess is unavailable (perhaps because it is
    *          busy performing a computation, the subprocess dies during the call, or an unexpected exception occurs.
    * NOTE: this method thread-safe because the _busy flag is double-checked.
    */
  public boolean interpret(final String s) {
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();  
    _log.log("In MainJVM, interpret(" + s + ") called; state = " + _stateMonitor.value() + "; _busy = " + _busy + "; remote = " + remote);
    if (remote == null || _busy) { 
      return false; 
    }
    // Double-check value of busy
    _log.log("In MainJVM, remote = " + remote);
    synchronized(this) {
      if (_busy) return false;
      _busy = true;
      _toEval = s;
    }
    try {
//      debug.logStart("Interpreting " + s);
      _log.log("In MainJVM, interpreting [" + s + "] remote = " + remote + "; _busy = " + _busy);
      InterpretResult result = remote.interpret(s);  // blocks until a result is returned
      _busy = false;
      _log.log("In MainJVM, after interpreting [" + s + "] result = " + result + "; _busy = " + _busy);
      String printedResult = result.apply(resultHandler());
      _log.log("In MainJVM, embedded result actually returned to MainJVM is " + printedResult);
      return true;
    }
    catch (RemoteException e) { 
//      debug.logEnd(); 
      _handleRemoteException(e); return false; }
  }
  
  /** Resets the interpreter and returns a boolean value (or exception!) indicating what happened.  
    * If the call returns true, the interpreter was available and was reset.
    * If the call returns false, the interpreter was not yet available.
    * If the call throws an Exception, then the interpreter was busy performing a computation.
    * NOTE: this method can be called in any thread.
    */
  public boolean resetInterpreter() {
    _log.log("In MainJVM, resetInterpreter in MainJVM called; _busy = " + _busy);
    if (_busy)
      throw new InterpreterBusyException("Attempt to resetInterpreter failed because the interpreter was busy");
    
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
    
    if (remote == null) {
      _log.log("In MainJVM, no interpreter is available; _stateMonitor is in state " + _stateMonitor.value());
      return false;
    }
    else { /* remote is the available interpreter */
      try { 
        _log.log("In MainJVM, calling internal reset on remote from MainJVM");
        remote.reset(); 
      }
      catch(RemoteException e) { 
        _log.log("In MainJVM, invoking reset on the remote interpreter threw exception " + e);
        throw new UnexpectedException(e); 
      }
      _log.log("In MainJVM, returning true from MainJVM.resetInterpreter; _busy = " + _busy);
      return true;
    }
  }
  
//  /** Gets the string representation of the value of a variable in the current interpreter, or "none"
//    * if the remote JVM is unavailable or an error occurs.  Blocks until the interpreter is connected.
//    * @param var the name of the variable
//    */
//  public Option<Pair<String,String>> getVariableToString(String var) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return Option.none(); }
//    try { return Option.some(remote.getVariableToString(var)); }
//    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
//  }
  
  /** Returns the class path stored in the ClassPathManager as a list of unique entries.  Delegates to the remote
    * (slave) JVM. The result is "none" if the remote JVM is unavailable or if an exception occurs.  Blocks until the 
    * interpreter is connected.
    * @return the class path
    */
  public List<File> getInteractionsClassPath() {
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
    if (! _interpreterJVMStarted || remote == null || _busy) return _newClassPath;
    _log.log("In getInteractionsClassPath, _interpreterJVMStarted = " + _interpreterJVMStarted + "; remote = " + remote + "; _busy = " + _busy);
    try { return remote.getInteractionsClassPath(); }
    catch (RemoteException e) { _handleRemoteException(e); return _newClassPath ; }
  }
  
  static final List<File> EMPTY_FILE_LIST = Arrays.asList(new File[]{});
  
  /* Current class path in slave JVM now stored in _classPath. */
  
  /** Blocks until the interpreter is connected.  Returns {@code true} if the change was successfully passed to
    * the remote JVM.  Assumes the same addition has already been made in the interactions model.  If we obtain a Scala
    * interpreter where we can incrementally extend the class path, then this code must be modified so that the 
    * cached value here reflects it. */
  public void addInteractionsClassPath(File f) {
//    _log.log("In MainJVM, adding '" + f + "' to interactions class path; _classPath = " + _classPath);
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
    if (remote != null && ! _busy) {
      try { 
        _log.log("In MainJVM, calling remote.addInteractionClassPath(" + f + ")");
        /* if the interpreter is changed to support additions to classPath, then f must be added to _classPath here */
        remote.addInteractionsClassPath(f);
        /* _classPath is NOT updated; it caches immutable value of the class path embedded in the slave JVM.  It only 
         * changes when the slave JVM is replaced. */
      }
      catch (RemoteException e) { _handleRemoteException(e); }
    }
  }

  /** Blocks until the interpreter is connected.  This method handles the addition of multiple entries to class path.
    * Returns {@code true} if all files are successfully passed to the remote JVM. */
  public void addInteractionsClassPath(List<File> cp) {
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
    if (remote != null && ! _busy) {
      _log.log("In MainJVM, adding '" + cp + "' to interactions class path");
      for (File f:cp)  addInteractionsClassPath(f);
    }
  }
  
  /** Returns true iff the current classPath stored in the ClassPathManager (running in the interpreter [slave] JVM
    * matches the class path for the current interpreter [slave] JVM.  */
  public boolean classPathUnchanged() {
    _log.log("classPathUnchanged called");
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
    if (remote == null) return false;
    final HashSet<File> interactionsClassPath = new HashSet<File>(_newClassPath);
    final HashSet<File> classPath = new HashSet<File>(_classPath);                          
    _log.log("In classPathUnchanged: interactions classPath = " + interactionsClassPath + "; current classPath = " + classPath);
    return interactionsClassPath.equals(classPath); 
  }
  
  /** Sets up a JUnit test suite in the Interpreter JVM and finds which classes are really TestCase
    * classes (by loading them).  Blocks until the interpreter is connected and the operation completes.
    * @param classNames the class names to run in a test
    * @param files the associated file
    * @return the class names that are actually test cases
    */
  public Option<List<String>> findTestClasses(List<String> classNames, List<File> files) {
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
    _log.log("In MainJVM.findTestClasses, remote = " + remote);
    if (remote == null || _busy) { return Option.none(); }
    try { 
      _log.log("In MainJVM.findTestClasses, forwarding method call to remote");
      return Option.some(remote.findTestClasses(classNames, files)); 
    }
    catch (RemoteException e) {
      _log.log("In MainJVM.findTestClasses, RemoteException thrown: " + e);
      _handleRemoteException(e); 
      return Option.none(); }
  }
  
  /**
   * Runs the JUnit test suite already cached in the Interpreter JVM.  Blocks until the remote JVM is available.
   * Returns {@code false} if no test suite is cached, the remote JVM is unavailable, or an error occurs.
   */
  public boolean runTestSuite() { 
    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
    if (remote == null || _busy) { return false; }
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
  
  /* In DrScala, there is only one interpreter. */
  
//  /** Adds a named interpreter to the list.  The result is {@code false} if the remote JVM is unavailable or
//    * if an exception occurs.  Blocks until the interpreter is connected.
//    * @param name the unique name for the interpreter
//    * @throws IllegalArgumentException if the name is not unique
//    */
//  public boolean addInterpreter(String name) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return false; }
//    try { remote.addInterpreter(name); return true; }
//    catch (RemoteException e) { _handleRemoteException(e); return false; }
//  }
  
//  /** Removes the interpreter with the given name, if it exists.  The result is {@code false} if
//    * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
//    * @param name Name of the interpreter to remove
//    */
//  public boolean removeInterpreter(String name) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return false; }
//    try { remote.removeInterpreter(name); return true; }
//    catch (RemoteException e) { _handleRemoteException(e); return false; }
//  }
  
//  /** Sets the current interpreter to the one specified by name.  The result is "none" if
//   * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
//    * @param name the unique name of the interpreter to set active
//    * @return Status flags: whether the current interpreter changed, and whether it is busy; or "none" on an error
//    */
//  public Option<Pair<Boolean, Boolean>> setActiveInterpreter(String name) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return Option.none(); }
//    try { return Option.some(remote.setActiveInterpreter(name)); }
//    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
//  }
//  
//  /** Sets the default interpreter to be the current one.  The result is "none" if
//   * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
//   * @return Status flags: whether the current interpreter changed, and whether it is busy; or "none" on an error
//   */
//  public Option<Pair<Boolean, Boolean>> setToDefaultInterpreter() {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return Option.none(); }
//    try { return Option.some(remote.setToDefaultInterpreter()); }
//    catch (RemoteException e) { _handleRemoteException(e); return Option.none(); }
//  }
  
  /** Sets the interpreter to enforce access to all members.  The result is {@code false} if
    * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
    */
//  public boolean setEnforceAllAccess(boolean enforce) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return false; }
//    try { remote.setEnforceAllAccess(enforce); return true; }
//    catch (RemoteException e) { _handleRemoteException(e); return false; }
//  }
  
//  /** Sets the interpreter to enforce access to private members.  The result is {@code false} if
//    * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
//    */
//  public boolean setEnforcePrivateAccess(boolean enforce) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return false; }
//    try { remote.setEnforcePrivateAccess(enforce); return true; }
//    catch (RemoteException e) { _handleRemoteException(e); return false; }
//  }
  
//  /** Require a semicolon at the end of statements. The result is {@code false} if
//    * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
//    */
//  public boolean setRequireSemicolon(boolean require) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return false; }
//    try { remote.setRequireSemicolon(require); return true; }
//    catch (RemoteException e) { _handleRemoteException(e); return false; }
//  }
//  
//  /** Require variable declarations to include an explicit type. The result is {@code false} if
//    * the remote JVM is unavailable or if an exception occurs.  Blocks until the interpreter is connected.
//    */
//  public boolean setRequireVariableType(boolean require) {
//    InterpreterJVMRemoteI remote = _stateMonitor.value().interpreter();
//    if (remote == null || _busy) { return false; }
//    try { remote.setRequireVariableType(require); return true; }
//    catch (RemoteException e) { _handleRemoteException(e); return false; }
//  }
  
  /* === Helper methods === */
  
  /** Call invokeSlave with the appropriate JVMBuilder. */
  private void _doStartup() {
//    Utilities.show("Executing _doStartup()");
    _log.log("In MainJVM, executing _doStartup()");
    /* Set the _workingDir to the desired working directory embedded in the interactions model */
    File _workingDir = _interactionsModel.getWorkingDirectory();  // could use the setter setWorkingDirectory
    // TODO: Eliminate this use of NULL_FILE.  It is a bad idea!  The correct behavior when it is used always depends on
    //       context, so it can never be treated transparently.  In this case, the process won't start.
    // COMMENT: I emphatically disagree.  null should never be used as a legal data value.  So if no _workingDir is 
    //          specified, NULL_FILE is the only good option, independent of "transparency".  I added the null check
    //          below because I have no assurance that NULL_FILE is used everywhere in the DrJava code base. -- Corky
    
    if (_workingDir == null || _workingDir == FileOps.NULL_FILE) { _workingDir = IOUtil.WORKING_DIRECTORY; }
    _log.log("Working directory is " + _workingDir);
    
    _log.log("In MainJVM, setting up jvmArgs");
    List<String> jvmArgs = new ArrayList<String>();

    if (_allowAssertions) { jvmArgs.add("-ea"); }
    
    String slaveMemory = DrScala.getConfig().getSetting(SLAVE_JVM_XMX);
    if (! "".equals(slaveMemory) && ! heapSizeChoices.get(0).equals(slaveMemory)) {
      jvmArgs.add("-Xmx" + slaveMemory + "M");
    }
    String slaveArgs = DrScala.getConfig().getSetting(SLAVE_JVM_ARGS);
    if (PlatformFactory.ONLY.isMacPlatform()) {
      jvmArgs.add("-Xdock:name=Interactions");
    }
    
    /* The streamlined code that follows assumes the class path for the new JVM is constructed from the system path
     * and the new path entries already stored in the interactions model. */
    _log.log("In MainJVM, adding boot class path to jvmArgs");
    // add additional boot class path items specified by the selected compiler
    for (File f: /* getInteractionsClassPath() */ _classPath) {
      try {
        // NOTE: this is a work-around; it seems like it's impossible to pass long file names here on Windows
        // so we are using a clumsy method that determines the short file name
        File shortF = FileOps.getShortFile(f);
        jvmArgs.add("-Xbootclasspath/a:" + shortF.getAbsolutePath().replace(File.separatorChar, '/'));
      }
      catch(IOException ioe) {
        // TODO: figure out what to do here. error? warn?
      }
    }
    
    jvmArgs.addAll(ArgumentTokenizer.tokenize(slaveArgs));
    _log.log("Completed execution of ArgumentTokenizer; slaveArgs = " + slaveArgs);
    
    /* class path for new slave JVM is _newClassPath*/
    final JVMBuilder jvmb = new JVMBuilder(_newClassPath).directory(_workingDir).jvmArguments(jvmArgs);
    _log.log("In MainJVM, _newClassPath = " + _newClassPath + "; jvmb = " + jvmb);
    
//    Utilities.show("Calling invokeSlave(" + jvmb + ")");
    _log.log("In MainJVM, calling invokeSlave(" + jvmb + ")");
    invokeSlave(jvmb);
    _interpreterJVMStarted = true;
  }
  
  /** Lets the model know if any exceptions occur while communicating with the Interpreter JVM. */
  private void _handleRemoteException(RemoteException e) {
    if (e instanceof UnmarshalException) {
      /* Interpreter JVM has disappeared (perhaps reset); just ignore the error. */
      if (e.getCause() instanceof EOFException) return;
      /* Deals with bug 2688586: Reset during debugging throws UnmarshalException
       * We may want to extend this to all kinds of SocketExceptions. */
      if ((e.getCause() instanceof SocketException) &&
          (e.getCause().getMessage().equals("Connection reset"))) return;
    }
    DrScalaErrorHandler.record(e);
  }
  
  /* Helper classes */
  
  /** Phase-based implementation of the starting/stopping functionality. Each of the methods retests the phase
    * (state) of _stateMonitor for MainJVM to determine if a phase change has occurred between the method dispatch
    * and subsequent execution.  Why?  In the general case (which may or may not be possible in DrScala, two 
    * threads could try to advance the phase concurrently.  The phase is advanced strictly by compareAndSet
    * operations, so only one such thread can successfully advance the state.
    */
  private abstract class Phase {
    /** Get the current interpreter -- null if unavailable.  Blocks in the Starting and Restarting states until
      * either a timeout or a state change (normally to RunningPhase) occurs. */
    public abstract InterpreterJVMRemoteI interpreter();
    
    /** Ensure that the interpreter is starting or running.  Block if necessary. */
    public abstract void start();
    
    /** Ensure that the interpreter is stopping or not running. Block if necessary. */
    public abstract void stop();
    
    /** Stop the slave JVM and create a new one. */
    public abstract void restart();
    
    public abstract void dispose();
    
    /** React to a completed startup. */
    public void started(InterpreterJVMRemoteI i) { throw new IllegalStateException("Unexpected started() call"); }
    
    /** React to a failed startup. */
    public void startFailed(Exception e) { throw new IllegalStateException("Unexpected startFailed() call"); }
    
    /** React to a completed shutdown (requested or spontaneous). */
    public void stopped(int status) { throw new IllegalStateException("Unexpected stopped() call"); }
  }
  
  /** Fresh, hasn't yet been started. */
  private class FreshPhase extends Phase {
    public InterpreterJVMRemoteI interpreter() { return null; } 
    public void start() {
      _log.log("In MainJVM, FreshPhase.start() invoked");
      /* The following operation compares the state of _stateMonitor (which is initially a new FreshPhase()) with this,
       * which is an intance of FreshPhase (and formerly the value of the state of _stateMonitor).  It will take an
       * unusual race condition for this comparison to fail; state must have been assigned after this was fetched for
       * this dispatch.
       */
      if (_stateMonitor.compareAndSet(this, new StartingPhase())) { // Advance to StartingPhase
        _log.log("In MainJVM, doing Startup");
        _doStartup(); 
      }
      else { 
        _stateMonitor.value().start(); /* The state of _stateMonitor has changed from a FreshPhase to another state. */
      }
    }
    public void stop() { }
    public void restart() { 
      _log.log("In MainJVM, FreshPhase.restart() invoked");
      start(); 
    }  // restart is equivalent to start in FreshPhase
    public void dispose() {
      if (_stateMonitor.compareAndSet(this, new DisposedPhase())) { MainJVM.super.dispose(); }
      else { _stateMonitor.value().dispose(); }
    }
  }
  
  /** Has been started, waiting for startup to complete. */
  private class StartingPhase extends Phase {
    private final int _failures;
    public StartingPhase() { _failures = 0; }
    private StartingPhase(int failures) { _failures = failures; }
    
    public InterpreterJVMRemoteI interpreter() {
      try { return _stateMonitor.ensureNotState(this, STARTUP_TIMEOUT).interpreter(); }
      catch (TimeoutException e) { return null; }
      catch (InterruptedException e) { throw new UnexpectedException(e); }
    }
    
    public void start() { }
    
    public void restart() {
      _log.log("In MainJVM, startingPhase.restart() invoked");  // what prevents infinite recursion here ?
      try { _stateMonitor.ensureNotState(this, STARTUP_TIMEOUT).restart(); }
      catch (Exception e) {
        UnexpectedException ue = new UnexpectedException(e); 
        _log.log("Throwing " + e);
        throw ue;
      }
    }
    
    public void stop() {
      try { _stateMonitor.ensureNotState(this, STARTUP_TIMEOUT).stop(); }
      catch (Exception e) { throw new UnexpectedException(e); }
    }
    
    public void dispose() { stop(); _stateMonitor.value().dispose(); }
    
    @Override public void started(InterpreterJVMRemoteI i) {
      _log.log("In MainJVM, startingPhase.started(" + i + ") called");
      if (_stateMonitor.compareAndSet(this, new RunningPhase(i))) {  // advances from StartingPhase to RunningPhase
        
        _enterRunningState();
        // Note that _workingDir isn't guaranteed to be the dir at the time startup began.  Is that a problem?
        // (Is the user ever going to see a working dir message that doesn't match the actual setting?)
        _interactionsModel.interpreterReady();
        _classPath = _newClassPath; // The intended class path has become the actual class path
        _junitModel.junitJVMReady();        
      }
      else { _stateMonitor.value().started(i); }
    }
    
    @Override public void startFailed(Exception e) {
      int count = _failures + 1;
      if (count < MAX_STARTUP_FAILURES) {
        if (_stateMonitor.compareAndSet(this, new StartingPhase(count))) { _doStartup(); }
        else { _stateMonitor.value().startFailed(e); }
      }
      else {
        if (_stateMonitor.compareAndSet(this, new FreshPhase())) { _interactionsModel.interpreterWontStart(e); }
        else { _stateMonitor.value().startFailed(e); }
      }
    }
  }
  
  /** Has an active interpreter available. */
  private class RunningPhase extends Phase {
    protected final InterpreterJVMRemoteI _interpreter;
    public RunningPhase(InterpreterJVMRemoteI interpreter) { _interpreter = interpreter; }
    public InterpreterJVMRemoteI interpreter() { return _interpreter; }
    public void start() { }
    
    public void stop() {  // terminates execution of slave JVMs
      _log.log("In MainJVM, runningPhase.stop() executed");
      if (_stateMonitor.compareAndSet(this, new StoppingPhase())) { // Advance to StoppingPhase
        quitSlave(); 
      }
      else { _stateMonitor.value().stop(); }
    }
    
    public void restart() {
      _log.log("In MainJVM, runningPhase.restart() executed");
      if (_stateMonitor.compareAndSet(this, new RestartingPhase())) {
        _log.log("In MainJVM, advancing phase to RestartingPhase");
//        _interactionsModel.interpreterResetting();  // now done in setUpNewInterpreter
        _log.log("In MainJVM, setting _busy to false and calling quitSlave()");
        _busy = false; // restarting may involve terminating a busy interpreter in current slaveJVM
        quitSlave();
      }
      else { 
        _log.log("In MainJVM, runningPhase.restart called but state already advanced to RestartingPhase");
        _stateMonitor.value().restart();  // Does NOTHING in RestartingPhase
      }
    }
    
    public void dispose() { stop(); _stateMonitor.value().dispose(); }
    
    @Override public void stopped(int status) {
      _log.log("In MainJVM, stopped(" + status + ") called in RunningPhase");
      if (_stateMonitor.compareAndSet(this, new RestartingPhase())) {  // Advance to RestartingPhase
        _interactionsModel.replCalledSystemExit(status);
//        _interactionsModel.interpreterResetting();  // performed by the restart() method in this RunningPhase!
      }
      // Note: state has advanced to RestartingPhase
      _stateMonitor.value().stopped(status); // delegate whether state changed here or in another thread
    }
  }
  
  /** Waiting for stop, should automatically start when that happens. */
  private class RestartingPhase extends Phase {
    
    public InterpreterJVMRemoteI interpreter() {
      try { return _stateMonitor.ensureNotState(this, STARTUP_TIMEOUT).interpreter(); }
      catch (TimeoutException e) { return null; }
      catch (InterruptedException e) { throw new UnexpectedException(e); }
    }
    
    public void start() { }
    
    public void stop() {
      if (!_stateMonitor.compareAndSet(this, new StoppingPhase())) { 
        _stateMonitor.value().stop(); // Only invoked when state has already advanced to StoppingPhase, but does NOTHING
      }
    }
    
    public void restart() { }
    
    public void dispose() {
      if (_stateMonitor.compareAndSet(this, new DisposedPhase())) { MainJVM.super.dispose(); }
      else { _stateMonitor.value().dispose(); }
    }
    
    @Override public void stopped(int status) {
      _log.log("In MainJVM, restartingPhase.stopped(" + status + ") called");
      if (_stateMonitor.compareAndSet(this, new StartingPhase())) { // Advance to StartingPhase
        _log.log("In MainJVM, advanced to StartingPhase and starting new slave JVM");
        _doStartup();  
      }  // 
      else { _stateMonitor.value().stopped(status); }
    }
  }
  
  /** DrScala is exiting; waiting for stop with no restart. There is no interpreter because the slave JVM is already
    * dead or is dying. */
  private class StoppingPhase extends Phase {
    public InterpreterJVMRemoteI interpreter() { return null; }
    
    public void start() {
      try { _stateMonitor.ensureNotState(this, STARTUP_TIMEOUT).start(); }
      catch (Exception e) { throw new UnexpectedException(e); }
    }
    
    public void stop() { }
    
    /** Resurrects the slave JVM by starting a new one.  Can this ever happen? */
    public void restart() { 
      if (!_stateMonitor.compareAndSet(this, new RestartingPhase())) { _stateMonitor.value().restart(); }
    }
    
    public void dispose() {
      if (_stateMonitor.compareAndSet(this, new DisposedPhase())) { MainJVM.super.dispose(); }
      else { _stateMonitor.value().dispose(); }
    }
    
    @Override public void stopped(int status) {
      if (!_stateMonitor.compareAndSet(this, new FreshPhase())) { _stateMonitor.value().stopped(status); } 
    }
  }
  
  /* NOTE: TEST_MODE hack was added because the Scala interpreter reset process broke in later versions of DrScala 
   * (later than 2.9?) , requiring interpreter replacement instead of interpreter resetting (including soft resets).  
   * The old unit test infrastructure (inheritied from DrJava) did not handle this issue well. */
  private class DisposedPhase extends Phase {
    public InterpreterJVMRemoteI interpreter() { throw new IllegalStateException("MainJVM is disposed"); }
    public void start() { if (! Utilities.TEST_MODE) throw new IllegalStateException("MainJVM is disposed"); }
    public void stop() { if (! Utilities.TEST_MODE) throw new IllegalStateException("MainJVM is disposed"); }
    public void restart() { if (! Utilities.TEST_MODE) throw new IllegalStateException("MainJVM is disposed"); }
    public void dispose() { }
    public void stopped() { /* may occur if transitioned here from Restarting or Stopping */ }
  }
  
  /** Performs the appropriate action to return any type of result from a call to interpret back to the GlobalModel. */
  private class ResultHandler implements InterpretResult.Visitor<String> {
    
    /** Lets the model know that void was returned. */
    public String forNoValue() {
      _interactionsModel.replReturnedVoid();
      return null;
    }
    
    /** Calls replReturnedResult() */
    public String forObjectValue(String objString, String objTypeString) {
      _interactionsModel.replReturnedResult(objString, InteractionsDocument.OBJECT_RETURN_STYLE);
      return objString;
    }
    
    /** Calls replReturnedResult() */
    public String forStringValue(String s) {
      _interactionsModel.replReturnedResult(s, InteractionsDocument.STRING_RETURN_STYLE);
      return s;
    }
    
    /** Calls replReturnedResult() */
    public String forCharValue(Character c) {
      final String s = c.toString();
      _interactionsModel.replReturnedResult(s, InteractionsDocument.CHARACTER_RETURN_STYLE);
      return s;
    }
    
    /** Calls replReturnedResult() */
    public String forNumberValue(Number n) {
      final String s = n.toString();
      _interactionsModel.replReturnedResult(s, InteractionsDocument.NUMBER_RETURN_STYLE);
      return s;
    }
    
    /** Calls replReturnedResult() */
    public String forBooleanValue(Boolean b) {
      final String s = b.toString();
      _interactionsModel.replReturnedResult(s, InteractionsDocument.OBJECT_RETURN_STYLE);
      return s;
    }
    
    /** Calls replThrewException() */
    public String forEvalException(String message, StackTraceElement[] stackTrace) {
      // TODO: restore location/syntax highlighting functionality
      _interactionsModel.replThrewException(message, stackTrace);
      return null;
    }
    
    /** Calls replThrewException() */
    public String forException(String message) {
      // TODO: restore location/syntax highlighting functionality
      _interactionsModel.replThrewException(message);
      return "Exception: " + message;
    }
    
    public String forUnexpectedException(Throwable t) {
      _interactionsModel.replReturnedVoid();
      throw new UnexpectedException(t);
    }
    
//    public String forBusy() {
//            
//      Utilities.show("forBusy() invoked by resultHandler.");
//      /* Force the interpreter to restart in a new slave JVM. */
//
//      /* Terminate the slave JVM; the slave includes code to terminate once it is disconnected. */
//      stopInterpreterJVM();
//      
//      /* Restart the interpreter, displaying a pending message in interactions pane that the interpreter is being 
//       * restarted. After the interpreter is restarted, this display is not included int the new InteractionsDocument. */
//
//      restartInterpreterJVM();
//      return "*** JVM was busy and restarted ***";
//    }
  }
  
  /** InteractionsModel which does not react to events. */
  public static class DefaultInteractionsModel implements InteractionsModelCallback {
    
    /* Degenerate method definitions */
    
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
//    public ConsoleDocument getConsoleDocument() {
//      throw new IllegalStateException("Cannot get console document of dummy interactions model!");
//    }
//    
//    public void _notifyInterpreterReady(File f) {
//      throw new IllegalStateException("Cannot notifyInterpreterReady(File f) from dummy interactions model!");
//    }
    public void _notifyInterpreterReady() {
      throw new IllegalStateException("Cannot notifyInterpreterReady from dummy interactions model!");
    }
    
    public void replReturnedVoid() { }
    public void replReturnedResult(String result, String style) { }
//    public void replIsResetting() { }
    public void replThrewException(String message, StackTraceElement[] stackTrace) { }
    public void replThrewException(String message) { }
//    public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol, int endRow,
//                                        int endCol) { }
    public void replCalledSystemExit(int status) { }
    public void interpreterResetting() { }
    public void documentReset() { }
    public void interpreterResetFailed(Throwable th) { }
    public void interpreterWontStart(Exception e) { }
    public void interpreterReady() { }
    public List<File> getCompilerBootClassPath() { return new ArrayList<File>(); }
    public File getWorkingDirectory() { return null; }
    public String transformCommands(String interactionsString) { return interactionsString; }
  }
  
  /** JUnitModel which does not react to events. */
  public static class DummyJUnitModel implements JUnitModelCallback {
    public void nonTestCase(boolean isTestAll, boolean didCompileFail) { }
    public void classFileError(ClassFileError e) { }
    public void testSuiteStarted(int numTests) { }
    public void testStarted(String testName) { }
    public void testEnded(String testName, boolean wasSuccessful, boolean causedError) { }
    public void testSuiteEnded(JUnitError[] errors) { }
    public File getFileForClassName(String className) { return null; }
//    public Iterable<File> getClassPath() { return IterUtil.empty(); }
    public void junitJVMReady() { }
  }
}
  
  


