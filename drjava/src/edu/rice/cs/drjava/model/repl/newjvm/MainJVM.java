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

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.newjvm.*;

/**
 * Manages a remote JVM.
 *
 * @version $Id$
 */
public class MainJVM extends AbstractMasterJVM implements MainJVMRemoteI {
  /** The global model. */
  private GlobalModel _model;

  public MainJVM(final GlobalModel model) throws RemoteException {
    super(InterpreterJVM.class.getName());

    _model = model;
    restartInterpreterJVM();
  }

  private InterpreterJVMRemoteI _interpreterJVM() {
    return (InterpreterJVMRemoteI) getSlave();
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
      _interpreterJVM().interpret(s);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void addClassPath(String path) {
    _ensureInterpreterConnected();

    try {
      //System.err.println("addclasspath to " + _interpreterJVM + ": " + path);
      _interpreterJVM().addClassPath(path);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void setPackageScope(String packageName) {
    _ensureInterpreterConnected();

    try {
      _interpreterJVM().setPackageScope(packageName);
    }
    catch (RemoteException re) {
      _threwException(re);
    }
  }

  public void reset() {
    _ensureInterpreterConnected();

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
    try {
      quitSlave();
    }
    catch (RemoteException re) {
      _threwException(re);
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
  public void restartInterpreterJVM() {
    if (isStartupInProgress()) {
      return;
    }

    try {
      invokeSlave();
    }
    catch (RemoteException re) {
      _threwException(re);
    }
    catch (IOException ioe) {
      _threwException(ioe);
    }
  }

  protected void handleSlaveQuit(int status) {
    restartInterpreterJVM();
    _model.replCalledSystemExit(status);
  }

  protected void handleSlaveConnected() {
    synchronized(this) {
      // notify so that if we were waiting (in ensureConnected)
      // this will wake em up
      notify();
    }
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
        while (_interpreterJVM() == null) {
          wait();
        }
      }
    }
    catch (InterruptedException ie) {
      throw new edu.rice.cs.util.UnexpectedException(ie);
    }
  }
}
