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

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import edu.rice.cs.util.newjvm.ExecJVM;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.junit.JUnitError;

/**
 * Manages a remote JVM.
 *
 * @version $Id$
 */
public class MainJVM extends UnicastRemoteObject implements MainJVMRemoteI {
  /** The name of the RMI object for the present JVM. */
  private final String _identifier;
  
  private final static int RESET_TIME_OUT = 60;

  /**
   * The global model.
   * This field might be null if the MainJVM is running in a test,
   * but will never be null in actual use.
   */
  private GlobalModel _model;

  /**
   * Is there a JVM in the process of starting up?
   * This variable is protected by synchronized(this).
   */
  private boolean _startupInProgress = false;
  
  private RestartThread _restartThread = null;
  private RestartThread _timerThread = null;
  private Timer _restartTimer = null;
  
  /**
   * Keeps track of whether we are currently in the process of cleanly
   * resetting the interactions JVM, as opposed to having it killed by
   * a thread in that JVM.  Used to determine whether or not to display
   * an information message when the JVM restarts.
   * This variable is protected by synchronized(this).
   * @see #setIsResetting
   */
  //private boolean _resetInProgress = false;

  /**
   * This is the pointer to the interpreter JVM remote object, used to call
   * back to it. It has the value null when the interpeter JVM is not running
   * or is not connected yet. It gets set to a value by
   * {@link #registerInterpreterJVM}, which is called by the interpreter
   * JVM itself over RMI.
   *
   * This can only be changed while holding the object lock.
   */
  private InterpreterJVMRemoteI _interpreterJVM = null;

  /**
   * Process object for the running interpreter, or null if none.
   * This can only be changed while holding the object lock.
   */
  private Process _interpreterProcess = null;

  public MainJVM(final GlobalModel model) throws RemoteException {
    super();

    _model = model;
    _startNameServiceIfNeeded();
    _identifier = _createIdentifier();

    try {
      Naming.rebind(_identifier, this);
    }
    catch (Exception e) {
      throw new edu.rice.cs.util.UnexpectedException(e);
    }

    restartInterpreterJVM();
  }
  
  /**
   * Creates a MainJVM without a model.
   */
  public MainJVM() throws RemoteException {
    this(null);
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

  public void interpret(String s) {
    _ensureInterpreterConnected();

    try {
      //System.err.println("interpret to " + _interpreterJVM + ": " + s);
      _interpreterJVM.interpret(s);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void addClassPath(String path) {
    _ensureInterpreterConnected();

    try {
      //DrJava.consoleOut().println("addclasspath to " + _interpreterJVM + ": " + path);
      //DrJava.consoleOut().println("ADD CLASSPATH: " + path);
      _interpreterJVM.addClassPath(path);
      //DrJava.consoleOut().println("DONE ADDING.");
    }
    catch (RemoteException re) {
      //DrJava.consoleOut().println("EXCEPTION in addClassPath:\n" + re);
      //re.printStackTrace();
      _threwException(re);
      //throw new edu.rice.cs.util.UnexpectedException(re);
    }
  }

  public void setPackageScope(String packageName) {
    _ensureInterpreterConnected();

    try {
      _interpreterJVM.setPackageScope(packageName);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void reset() {
    _ensureInterpreterConnected();

    try {
      _interpreterJVM.reset();
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public String getIdentifier() {
    return _identifier;
  }

  public void systemErrPrint(String s) throws RemoteException {
    _model.replSystemErrPrint(s);
  }

  public void systemOutPrint(String s) throws RemoteException {
    _model.replSystemOutPrint(s);
  }

  /**
   * Registers the interpreter JVM for later callbacks.
   *
   * @param remote The interpreter JVM controller.
   */
  public synchronized void registerInterpreterJVM(InterpreterJVMRemoteI remote)
    throws RemoteException
  {
    if (_interpreterJVM != null) { 
      try {
        //DrJava.consoleOut().println("killing jvm! " + i);
        remote.exitJVM();
      }
      catch (RemoteException re) {
      }
      return;
    }
    _timerThread.disable();
    _interpreterJVM = remote;
    // _model may be null if we're running a test on this
    if (_model != null) {
      _model.interactionsReady();
    }
    _startupInProgress = false;
    // wake up anyone waiting for an interpreter!
    notify();
  }

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning no value.
   */
  public void returnedVoid() throws RemoteException {
    _model.replReturnedVoid();
  }

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning a value.
   *
   * @param result The .toString-ed version of the value that was returned
   *               by the interpretation. We must return the String form
   *               because returning the Object directly would require the
   *               data type to be serializable.
   */
  public void returnedResult(String result) throws RemoteException {
    _model.replReturnedResult(result);
  }

  /**
   * Signifies that the most recent interpretation was ended 
   * due to an exception being thrown.
   *
   * @param exceptionClass The name of the class of the thrown exception
   * @param message The exception's message
   * @param stackTrace The stack trace of the exception
   */
  public void threwException(String exceptionClass,
                             String message,
                             String stackTrace) throws RemoteException
  {
    _model.replThrewException(exceptionClass, message, stackTrace);
  }

  public synchronized void killInterpreter() {
    if ((_interpreterProcess != null) && (_interpreterJVM != null)) {
      try {
        //DrJava.consoleOut().println("killing jvm! " + i);
        _interpreterJVM.exitJVM();
      }
      catch (RemoteException re) {
        // couldn't ask it to quit nicely. be mean and kill
        _interpreterProcess.destroy();
      }
      
      _interpreterProcess = null;
      _interpreterJVM = null;
    }
  }

  /**
   * Kills current interpreter JVM if any, then starts a new one.
   * It turns out that before I added the {@link #_startupInProgress} guard,
   * we were starting up two jvms in quick succession sometimes. This caused
   * nasty problems (sometimes, it was a timing thing!) if the addClasspath
   * issued after an abort went to the first JVM but then future
   * interpretations went to the second JVM! So, we can make
   * restartInterpreterJVM safe for duplicate calls by just not starting
   * another if the previous one is in the process of starting up.
   */
  public synchronized void restartInterpreterJVM() {
    if (_startupInProgress) {
      return;
    }
    _startupInProgress = true;   
    
    // _model may be null if we're running a test on this
    if (_model != null)
      _model.interactionsResetting();
    
    if (_restartThread != null) {
      //DrJava.consoleOut().println("Disabling _restartThread");
      _restartThread.disable();
    }
    
    killInterpreter();
    
    int debugPort = getDebugPort();
    
    String className = InterpreterJVM.class.getName();
    String[] args = new String[] { getIdentifier() };
    Vector<String> jvmArgs = new Vector<String>();
    
    if (allowAssertions()) {
      jvmArgs.addElement("-ea");
    }
    
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
    
    //System.out.println("starting interpreter... " + jvmargs[1]);
    /*String classpath = _model.getClasspath() +
     System.getProperty("path.separator") +
     System.getProperty("java.class.path");
     */
    try {
      //DrJava.consoleOut().println("In MainJVM: starting interpreter jvm");
      _interpreterProcess = ExecJVM.
        runJVMPropogateClassPath(className, args, jvmArgsArray);
      
      // Start a thread to wait for the interpreter to die and to fire
      // off a new one (and notify model) when it happens
      _restartThread = new RestartThread() {
        public void run() {
          try {
            int status = _interpreterProcess.waitFor();
            synchronized(MainJVM.this) {
              if (_shouldReset) {
                boolean currentlyStarting = _startupInProgress;
                restartInterpreterJVM();
                if (!currentlyStarting) {
                  replCalledSystemExit(status);
                }
              }
            }
          }
          catch (InterruptedException ie) {
            throw new edu.rice.cs.util.UnexpectedException(ie);
          }
        }
      };
      // If RESET_TIME_OUT seconds pass before the new InterpreterJVM registers, try restarting the JVM
      _timerThread = new RestartThread() {
        public void run() {
          _restartTimer = new Timer(RESET_TIME_OUT*1000, new ActionListener() {
            int count = 0;
            public void actionPerformed(ActionEvent e) {
              synchronized(MainJVM.this) {
                if (!_shouldReset) {
                  _stopTimerThread();
                  return;
                }
                if (count < 2) {
                  _model.printDebugMessage("Could not connect to InterpreterJVM, trying again...");
                  restartInterpreterJVM();
                  count++;
                  return;
                }
                _model.printDebugMessage("Connection failed, the interactions window will not work");
                _stopTimerThread();
              }
            }
          });               
          _restartTimer.setRepeats(true);
          _restartTimer.start();   
        }
      };
      _restartThread.start();
      _timerThread.start();
    }
    catch (IOException ioe) {
      _threwException(ioe);
    }
  }
  
  private void _stopTimerThread() {
    _restartTimer.stop();
  }
  /**
   * Returns the debug port to use, as specified by the model.
   * Returns -1 if no usable port could be found.
   */
  protected int getDebugPort() {
    int port = -1;
    try {
      port = _model.getDebugPort();
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
   * Action to take if the Repl tries to exit.
   * @param status Exit code from the interpreter JVM
   */
  protected void replCalledSystemExit(int status) {
    _model.replCalledSystemExit(status);
  }

  
  /**
   * Lets us know whether or not we are in the process of a clean reset.
   * If we are, the message explaining that the JVM was exited should not
   * be displayed.  For some reason, this needs to be set from outside this
   * class, rather than simply setting the field in restartInteractionsJVM.
   * (Timing issues, maybe?)
   * @param b whether we are in the process of resetting
   *
  public synchronized void setIsResetting(boolean b) { 
    //DrJava.consoleOut().println("setIsResestting: resetInProgress: " + b);
    _resetInProgress = b;
  }*/
  
  /**
   * Returns whether we are in the process of cleanly resetting the
   * interactions JVM.
   * @see #setIsResetting
   *
  private synchronized boolean _isResetting() {
    //DrJava.consoleOut().println("isResetting: resetInProgress: " + _resetInProgress);
    return _resetInProgress;
  }*/
  
  public void checkStillAlive() throws RemoteException {
    // do nothing.
  }

  private void _threwException(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));

    try {
      threwException(t.getClass().getName(), t.getMessage(), writer.toString());
    }
    catch (RemoteException re) {
      // impossible, we're not calling it remotely!
      throw new edu.rice.cs.util.UnexpectedException(re);
    }
  }

  private void _ensureInterpreterConnected() {
    try {
      synchronized(this) {
        while (_interpreterJVM == null) {
          wait();
        }
      }
    }
    catch (InterruptedException ie) {
      throw new edu.rice.cs.util.UnexpectedException(ie);
    }
  }

  private void _startNameServiceIfNeeded() {
    try {
      Naming.list("");
    }
    catch (Exception e) {
      try {
        LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
      }
      catch (RemoteException re) {
        throw new edu.rice.cs.util.UnexpectedException(re);
      }
    }
  }

  private String _createIdentifier() {
    try {
      File file = File.createTempFile("drjava", "");
      file.deleteOnExit();
      return file.getName();
    }
    catch (IOException ioe) { 
      throw new edu.rice.cs.util.UnexpectedException(ioe);
    }
  }
  
  public void runTest(String className, String fileName) {
    _ensureInterpreterConnected();
    
    try {
      _interpreterJVM.runTest(className, fileName);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }
  
  public void nonTestCase() throws RemoteException {
    _model.nonTestCase();
  }
  
  public void testFinished(JUnitError[] errors) throws RemoteException {
    _model.testFinished(errors);
  }
  
  private class RestartThread extends Thread {
    protected boolean _shouldReset = true;
    
    public void disable() {
      _shouldReset = false;
    }
  }
}
