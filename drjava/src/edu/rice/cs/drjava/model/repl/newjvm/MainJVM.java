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

import java.rmi.registry.*;
import java.rmi.server.*;
import java.rmi.*;
import java.io.*;

import gj.util.Vector;

// NOTE: Do NOT import/use the config framework in this class!
//  (It seems to crash Eclipse...)
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.junit.JUnitModelCallback;
import edu.rice.cs.util.newjvm.*;

/**
 * Manages a remote JVM.
 *
 * @version $Id$
 */
public class MainJVM extends AbstractMasterJVM implements MainJVMRemoteI {
  private Log _log = new Log("MainJVM");
  
  /** Listens to interactions-related events. */
  private InteractionsModelCallback _interactionsModel;
  
  /** Listens to JUnit-related events. */
  private JUnitModelCallback _junitModel;

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
   * Creates a new MainJVM to interface to another JVM, but does not
   * automatically start the Interpreter JVM.  Callers should set the
   * InteractionsModel and JUnitModel, and then call startInterpreterJVM().
   */
  public MainJVM() {
    super(InterpreterJVM.class.getName());

    _interactionsModel = new DummyInteractionsModel();
    _junitModel = new DummyJUnitModel();
    _startupClasspath = System.getProperty("java.class.path");
    //startInterpreterJVM();
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
      _log.log("main.interp: " + s);
      _interpreterJVM().interpret(s);
    }
    catch (java.rmi.UnmarshalException ume) {
      // Could not receive result from interpret; system probably exited.
      // We will silently fail and let the interpreter restart.
      _log.log("main.interp: UnmarshalException, so interpreter is dead:\n"
                 + ume);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }
  
  /**
   * Called when a call to interpret has completed.
   * @param result The result of the interpretation
   */
  public void interpretResult(InterpretResult result) throws RemoteException {
//     try {
      _log.log("main.interp result: " + result);
      result.apply(getResultHandler());
//      }
//      catch (Throwable t) {
//        _log.log("EXCEPTION in interpretResult: " + t.toString());
//      }
  }
  

  /**
   * Adds the given path to the Interpreter's class path.
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
   * @param className Name of the TestCase class
   * @param fileName Name of the file for the TestCase class
   */
  public void runTestSuite(String className, String fileName) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();
    
    try {
      _interpreterJVM().runTestSuite(className, fileName);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }
  
  /**
   * Called if JUnit is invoked on a non TestCase class.  Forwards from
   * the other JVM to the local JUnit model.
   */
  public void nonTestCase() throws RemoteException {
    _junitModel.nonTestCase();
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
   * Removes the interpreter with the given name, if it exists.
   * @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) {
    // silently fail if disabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();
    
    try {
      _interpreterJVM().removeInterpreter(name);
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
    if (! _enabled) return false;

    ensureInterpreterConnected();
    
    try {
      return _interpreterJVM().setActiveInterpreter(name);
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
      return _interpreterJVM().setToDefaultInterpreter();
    }
    catch (RemoteException re) {
      _threwException(re);
      return false;
    }
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
  public void killInterpreter(boolean shouldRestart) {
    try {
      _enabled = shouldRestart;
      _cleanlyRestarting = true;
      quitSlave();
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
  }

  /**
   * Starts the interpreter if it's not running already.
   */
  public void startInterpreterJVM() {
    if (isStartupInProgress() || isInterpreterRunning()) {
      return;
    }

    // Pass assertion and debug port information as JVM arguments
    Vector<String> jvmArgs = new Vector<String>();
    if (allowAssertions()) {
      jvmArgs.addElement("-ea");
    }
    int debugPort = getDebugPort();
    _log.log("starting with debug port: " + debugPort);
    if (debugPort > -1) {
      jvmArgs.addElement("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + 
                         debugPort);
      jvmArgs.addElement("-Xdebug");
      jvmArgs.addElement("-Xnoagent");
      jvmArgs.addElement("-Djava.compiler=NONE");
    }
    String[] jvmArgsArray = new String[jvmArgs.size()];
    for (int i=0; i < jvmArgs.size(); i++) {
      jvmArgsArray[i] = jvmArgs.elementAt(i);
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
      _interactionsModel.interpreterResetting();

      startInterpreterJVM();
    }

    if (!_cleanlyRestarting) {
      _interactionsModel.replCalledSystemExit(status);
    }
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
    
    _interactionsModel.interpreterReady();
    _junitModel.junitJVMReady();

    _log.log("thread in connected: " + Thread.currentThread());

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
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));

    _interactionsModel.replThrewException(t.getClass().getName(),
                                          t.getMessage(),
                                          writer.toString());
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
          //_log.log("interpreter is null, waiting for it to register");
          wait();
        }
        //_log.log("interpreter registered; moving on");
      }
    }
    catch (InterruptedException ie) {
      throw new edu.rice.cs.util.UnexpectedException(ie);
    }
  }

  /**
   * Accessor for the remote interface to the Interpreter JVM.
   */
  private InterpreterJVMRemoteI _interpreterJVM() {
    return (InterpreterJVMRemoteI) getSlave();
  }

  /**
   * Peforms the appropriate action to return any type of result
   * from a call to interpret back to the GlobalModel.
   */
  private class ResultHandler implements InterpretResultVisitor<Object> {
    /**
     * Lets the model know that void was returned.
     * @returns null
     */
    public Object forVoidResult(VoidResult that) {
      _interactionsModel.replReturnedVoid();
      return null;
    }

    /**
     * Returns a value result (as a String) back to the model.
     * @returns null
     */
    public Object forValueResult(ValueResult that) {
      String result = that.getValueStr();
      _interactionsModel.replReturnedResult(result);
      return null;
    }

    /**
     * Returns an exception back to the model.
     * @returns null
     */
    public Object forExceptionResult(ExceptionResult that) {
      _interactionsModel.replThrewException(that.getExceptionClass(),
                                that.getExceptionMessage(),
                                that.getStackTrace());
      return null;
    }
  }

  /**
   * InteractionsModel which does not react to events.
   */
  public static class DummyInteractionsModel 
    implements InteractionsModelCallback
  {
    public int getDebugPort() throws IOException { return -1; }
    public void replSystemOutPrint(String s) {}
    public void replSystemErrPrint(String s) {}
    public void replReturnedVoid() {}
    public void replReturnedResult(String result) {}
    public void replThrewException(String exceptionClass,
                                   String message,
                                   String stackTrace) {}
    public void replCalledSystemExit(int status) {}
    public void interpreterResetting() {}
    public void interpreterReady() {}
  }
  
  /**
   * JUnitModel which does not react to events.
   */
  public static class DummyJUnitModel implements JUnitModelCallback {
    public void nonTestCase() {}
    public void testSuiteStarted(int numTests) {}
    public void testStarted(String testName) {}
    public void testEnded(String testName, boolean wasSuccessful,
                          boolean causedError) {}
    public void testSuiteEnded(JUnitError[] errors) {}
    public void junitJVMReady() {}
  }
}
