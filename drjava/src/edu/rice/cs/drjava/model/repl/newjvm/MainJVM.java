package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.registry.*;
import java.rmi.server.*;
import java.rmi.*;
import java.io.*;

import edu.rice.cs.util.ExecJVM;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.*;

/**
 * Manages a remote JVM.
 *
 * @version $Id$
 */
public class MainJVM extends UnicastRemoteObject implements MainJVMRemoteI {
  /** The name of the RMI object for the present JVM. */
  private final String _identifier;

  /** The global model. */
  private GlobalModel _model;

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
      _interpreterJVM.interpret(s);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void addClassPath(String path) {
    _ensureInterpreterConnected();

    try {
      _interpreterJVM.addClassPath(path);
    }
    catch (RemoteException re) {
      _threwException(re);
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
  public void registerInterpreterJVM(InterpreterJVMRemoteI remote)
    throws RemoteException
  {
    synchronized(this) {
      _interpreterJVM = remote;
      // wake up anyone waiting for an interpreter!
      notify();
    }
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

  public void killInterpreter() {
    synchronized(this) {
      if ((_interpreterProcess != null) && (_interpreterJVM != null)) {
        try {
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
  }

  /**
   * Kills current interpreter JVM if any, then starts a new one.
   */
  public void restartInterpreterJVM() {
    synchronized(this) {
      killInterpreter();

      String className = InterpreterJVM.class.getName();
      String[] args = new String[] { getIdentifier() };
      try {
        _interpreterProcess = ExecJVM.runJVMPropogateClassPath(className, args);
        
        // Start a thread to wait for the interpreter to die and to fire
        // off a new one (and notify model) when it happens
        Thread thread = new Thread() {
          public void run() {
            try {
              int status = _interpreterProcess.waitFor();
              restartInterpreterJVM();
              _model.replCalledSystemExit(status);
            }
            catch (InterruptedException ie) {
              throw new edu.rice.cs.util.UnexpectedException(ie);
            }
          }
        };

        thread.start();
      }
      catch (IOException ioe) {
        _threwException(ioe);
      }
    }
  }

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
}
