package edu.rice.cs.drjava.model.repl.newjvm;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface specifies the methods that the interpreter JVM exposes
 * for the MainJVM to call.
 *
 * @version $Id$
 */
public interface InterpreterJVMRemoteI extends Remote {
  public void interpret(String s) throws RemoteException;
  public void addClassPath(String s) throws RemoteException;
  public void setPackageScope(String s) throws RemoteException;
  public void reset() throws RemoteException;
  public void exitJVM() throws RemoteException;
}
