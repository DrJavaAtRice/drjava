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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.util.newjvm.*;

/**
 * Manages a remote JVM.
 *
 * @version $Id$
 */
public class MainJVM extends AbstractMasterJVM implements MainJVMRemoteI {
  private Log _log = new Log("MainJVM");

  /** The global model. */
  private GlobalModel _model;

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

  public MainJVM(final GlobalModel model) throws RemoteException {
    super(InterpreterJVM.class.getName());

    _model = model;
    startInterpreterJVM();
  }

  public boolean isInterpreterRunning() {
    return _interpreterJVM() != null;
  }

  /**
   * For test cases, we reuse the same MainJVM for efficiency.
   * This method is used to retarget the model pointer.
   *
   * Note: This feature should only be used for test cases! Otherwise
   * we're all better off making a new MainJVM when we need it.
   */
  public void setModel(GlobalModel model) {
    _model = model;
  }

  public void interpret(final String s) {
    // silently fail if diabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    // Spawn thread on InterpreterJVM side
    //  (will receive result in the interpretResult(...) method)
    try {
      //_log.log("main.interp: " + s);
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
    
    // Spawn thread on this side (receive result "immediately")
    /*
    Thread thread = new Thread("interpret thread: " + s) {
      public void run() {
        try {
          //System.err.println("interpret to " + _interpreterJVM() + ": " + s);
          InterpretResult result = _interpreterJVM().interpret(s);
          _log.log("main.interp: " + s + " --> " + result);
          result.apply(getResultHandler());
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
    };

    thread.setDaemon(true);
    thread.start();
    */
  }
  
  /**
   * Called when a call to interpret has completed.
   * @param result The result of the interpretation
   */
  public void interpretResult(InterpretResult result) throws RemoteException {
    //_log.log("main.interp result: " + s);
    result.apply(getResultHandler());
  }

  public void addClassPath(String path) {
    // silently fail if diabled. see killInterpreter docs for details.
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

  public void setPackageScope(String packageName) {
    // silently fail if diabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    try {
      _interpreterJVM().setPackageScope(packageName);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void reset() {
    // silently fail if diabled. see killInterpreter docs for details.
    if (! _enabled) return;

    ensureInterpreterConnected();

    try {
      _interpreterJVM().reset();
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void systemErrPrint(String s) throws RemoteException {
    _model.replSystemErrPrint(s);
  }

  public void systemOutPrint(String s) throws RemoteException {
    _model.replSystemOutPrint(s);
  }
  
  /**
   * Runs a JUnit Test class in the Interpreter JVM.
   * @param className Name of the TestCase class
   * @param fileName Name of the file for the TestCase class
   */
  public void runTestSuite(String className, String fileName) {
    // silently fail if diabled. see killInterpreter docs for details.
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
   * Called if JUnit is invoked on a non TestCase class.
   */
  public void nonTestCase() throws RemoteException {
    _model.nonTestCase();
  }
  
  /**
   * Called to indicate that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(int numTests) throws RemoteException {
    _model.testSuiteStarted(numTests);
  }
  
  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  public void testStarted(String testName) throws RemoteException {
    _model.testStarted(testName);
  }
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(String testName, boolean wasSuccessful, boolean causedError)
    throws RemoteException {
    _model.testEnded(testName, wasSuccessful, causedError);
  }
  
  /**
   * Called when a full suite of tests has finished running.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) throws RemoteException {
    _model.testSuiteEnded(errors);
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
      if (shouldRestart) {
        _cleanlyRestarting = true;
      }
      quitSlave();
    }
    catch (RemoteException re) {
      _threwException(re);
    }
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
      invokeSlave(jvmArgsArray);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
    catch (IOException ioe) {
      _threwException(ioe);
    }
  }

  protected void handleSlaveQuit(int status) {
    // Only restart the slave if _enabled is true
    if (_enabled) {
      // _model may be null if we're running a test on this
      if (_model != null) {
        _model.interactionsResetting();
      }

      startInterpreterJVM();
    }

    if (!_cleanlyRestarting && (_model != null)) {
      _model.replCalledSystemExit(status);
    }
    _cleanlyRestarting = false;
  }

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
    
    // _model may be null if we're running a test on this
    if (_model != null) {
      _model.interactionsReady();
    }

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
      if (_model != null) {
        port = _model.getDebugPort();
      }
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
    Boolean allow = DrJava.getConfig().getSetting(OptionConstants.JAVAC_ALLOW_ASSERT);
    String version = System.getProperty("java.version");
    return ((allow.booleanValue()) && (version != null) &&
        ("1.4.0".compareTo(version) <= 0));
  }

  /**
   * Lets the model know if any exceptions occur while communicating with
   * the Interpreter JVM.
   */
  private void _threwException(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));

    // model may be null if we're running a test
    if (_model != null) {
      _model.replThrewException(t.getClass().getName(),
                                t.getMessage(),
                                writer.toString());
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
          wait();
        }
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
      _model.replReturnedVoid();
      return null;
    }

    /**
     * Returns a value result (as a String) back to the model.
     * @returns null
     */
    public Object forValueResult(ValueResult that) {
      String result = that.getValueStr();
      _log.log("return called: " + result);
      _model.replReturnedResult(result);
      _log.log("return call over: " + result);
      return null;
    }

    /**
     * Returns an exception back to the model.
     * @returns null
     */
    public Object forExceptionResult(ExceptionResult that) {
      _model.replThrewException(that.getExceptionClass(),
                                that.getExceptionMessage(),
                                that.getStackTrace());
      return null;
    }
  }

}
