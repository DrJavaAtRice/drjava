package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface specifies the methods that the Main JVM exposes
 * for the InterpreterJVM to call.
 *
 * @version $Id$
 */
public interface MainJVMRemoteI extends Remote {
  public void systemErrPrint(String s) throws RemoteException;
  public void systemOutPrint(String s) throws RemoteException;

  /**
   * Registers the interpreter JVM for later callbacks.
   *
   * @param remote The interpreter JVM controller.
   */
  public void registerInterpreterJVM(InterpreterJVMRemoteI remote)
    throws RemoteException;

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning no value.
   */
  public void returnedVoid() throws RemoteException;

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning a value.
   *
   * @param result The .toString-ed version of the value that was returned
   *               by the interpretation. We must return the String form
   *               because returning the Object directly would require the
   *               data type to be serializable.
   */
  public void returnedResult(String result) throws RemoteException;

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
                             String stackTrace) throws RemoteException;

  /** 
   * The interpreter JVM calls this method periodically to ensure the main
   * VM is still alive. If it's not, the interpreter just quits.
   */
  public void checkStillAlive() throws RemoteException;
}
