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

import java.util.*;
import java.io.*;
import java.rmi.server.*;
import java.rmi.*;
import java.net.MalformedURLException;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.OutputStreamRedirector;

/**
 * This is the main class for the interpreter JVM.
 * Note that this class is specific to using DynamicJava. It would need
 * to be subclassed to use with another interpreter. (Really, there would
 * need to be an abstract base class, but since we don't need it yet I'm
 * not making one.)
 *
 * @version $Id$
 */
public class InterpreterJVM extends UnicastRemoteObject
                            implements InterpreterJVMRemoteI
{
  public static final int WAIT_UNTIL_QUIT_MS = 100;
  public static final int CHECK_MAIN_VM_ALIVE_MINUTES = 1;
  public static final String EMPTY_TRACE_TEXT = "";
  //public static final String EMPTY_TRACE_TEXT = "  at (the interactions window)";

  private final MainJVMRemoteI _mainJVM;
  private JavaInterpreter _interpreter;

  public InterpreterJVM(String url) throws RemoteException,
                                           NotBoundException,
                                           MalformedURLException
  {
    super();

    reset();

    _mainJVM = (MainJVMRemoteI) Naming.lookup(url);
    _mainJVM.registerInterpreterJVM(this);

    // redirect stdout
    System.setOut(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try {
          _mainJVM.systemOutPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
        }
      }
    }));

    // redirect stderr
    System.setErr(new PrintStream(new OutputStreamRedirector() {
      public void print(String s) {
        try {
          _mainJVM.systemErrPrint(s);
        }
        catch (RemoteException re) {
          // nothing to do
        }
      }
    }));

    // Start a thread to periodically ping the main VM and quit when
    // it's dead
    // This is just in case the main vm dies without killing us
    Thread thread = new Thread() {
      public void run() {
        while (true) {
          try {
            Thread.currentThread().sleep(CHECK_MAIN_VM_ALIVE_MINUTES*60*1000);
          }
          catch (InterruptedException ie) {
          }

          try {
            _mainJVM.checkStillAlive();
          }
          catch (RemoteException re) {
            // not there anymore. quit!
            System.exit(0);
          }
        }
      }
    };

    thread.start();

  }

  public void interpret(final String s) throws RemoteException {
    // fire off thread to interpret to keep from blocking the caller
    // it's all asynchronous anyhow ...
    Thread thread = new Thread() {
      public void run() {
        try {
          //_dialog("to interp: " + s);
          Object result = _interpreter.interpret(s);
          //_dialog("interp ret: " + result);
          if (result == JavaInterpreter.NO_RESULT) {
            _mainJVM.returnedVoid();
          }
          else {
            // we use String.valueOf because it deals with result = null!
            _mainJVM.returnedResult(String.valueOf(result));
          }
        }
        catch (RemoteException re) {
          // what do do? nothing I guess. main jvm is dead!
        }
        catch (ExceptionReturnedException e) {
          Throwable t = e.getContainedException();

          try {
            //_dialog("before call to threwException");
            _mainJVM.threwException(t.getClass().getName(),
                                    t.getMessage(),
                                    getStackTrace(t));
          }
          catch (RemoteException re) {
            // what do do? nothing I guess. main jvm is dead!
          }
        }
      }
    };

    thread.start();
  }

  private static void _dialog(String s) {
    javax.swing.JOptionPane.showMessageDialog(null, s);
  }

  /**
   * Gets the stack trace from the given exception, stripping off
   * the bottom parts of the trace that are internal to the interpreter.
   * This would be much easier to do in JDK 1.4, since you can get the
   * stack trace frames directly, instead of having to parse this!
   */
  protected static String getStackTrace(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));

    //_dialog("before creating reader");
    BufferedReader reader
      = new BufferedReader(new StringReader(writer.toString()));

    //_dialog("after creating reader");
    LinkedList traceItems = new LinkedList();
    try {
      // we will generate list of trace items
      // skip the first one since it's just the message
      //_dialog("before first readLine");
      reader.readLine();
      //_dialog("after first readLine");

      String s;
      while ((s = reader.readLine()) != null) {
        //_dialog("read: " + s);
        traceItems.add(s);
      }
    }
    catch (IOException ioe) {
      return "Unable to get stack trace";
    }

    // OK, now we search back to front looking for the first thing that's not
    // prefixed by edu.rice.cs.drjava or koala.dynamicjava.
    // We remove the extraneous items.
    while (! traceItems.isEmpty()) {
      String last = (String) traceItems.getLast();
      last = last.trim();

      if (last.startsWith("at edu.rice.cs.drjava.") ||
          last.startsWith("at koala.dynamicjava."))
      {
        traceItems.removeLast();
      }
      else {
        break;
      }
    }

    // Now, check if the last item here is a java.lang.reflect. If so,
    // get rid of it.
    if (! traceItems.isEmpty()) {
      String last = (String) traceItems.getLast();
      last = last.trim();

      if (last.startsWith("at java.lang.reflect.")) {
        traceItems.removeLast();
      }
    }

    // sigh. on jdk 1.4 above the java.lang.reflect there
    // are some sun.reflect entries. get rid of em
    while (! traceItems.isEmpty()) {
      String last = (String) traceItems.getLast();
      last = last.trim();

      if (last.startsWith("at sun.reflect."))
      {
        traceItems.removeLast();
      }
      else {
        break;
      }
    }

    // Last check: See if there are no items left. If there are none,
    // put one in to say it happened at top-level.
    if (traceItems.isEmpty()) {
      traceItems.add(EMPTY_TRACE_TEXT);
    }

    // OK, now rebuild string
    StringBuffer buf = new StringBuffer();
    ListIterator itor = traceItems.listIterator();
    boolean first = true;
    while (itor.hasNext()) {
      if (first) {
        first = false;
      }
      else {
        buf.append("\n");
      }

      buf.append("  " + ((String) itor.next()).trim());
    }

    return buf.toString();
  }

  public void exitJVM() throws RemoteException {
    // can't just exit in the middle of the rmi call -- it confuses the other
    // side. so make a thread start and wait a moment to exit.
    
    Thread thread = new Thread() {
      public void run() {
        try {
          Thread.currentThread().sleep(WAIT_UNTIL_QUIT_MS);
        }
        catch (InterruptedException ie) {
          // who cares? time to exit anyway!
        }

        System.exit(-1);
      }
    };

    thread.start();
  }

  public void addClassPath(String s) throws RemoteException {
    //_dialog("add classpath: " + s);
    _interpreter.addClassPath(s);
  }

  public void setPackageScope(String s) throws RemoteException {
    _interpreter.setPackageScope(s);
  }

  public void reset() throws RemoteException {
    _interpreter = new DynamicJavaAdapter();
    
    // do an interpretation to get the interpreter loaded fully
    try {
      _interpreter.interpret("0");
    }
    catch (ExceptionReturnedException e) {
      throw new edu.rice.cs.util.UnexpectedException(e);
    }
  }


  /**
   * Main entry point for interpreter JVM.
   *
   * @param args Command-line arguments. #1 must be the URL to find the 
   *             MainJVMRemoteI via RMI.
   */
  public static void main(String[] args) {
    //javax.swing.JFrame frame = new javax.swing.JFrame("interpreter up");
    //frame.show();

    try {
      InterpreterJVM jvm = new InterpreterJVM(args[0]);
    }
    catch (Throwable t) {
      //javax.swing.JOptionPane.showMessageDialog(null, "Interpreter JVM error: " + t);
      t.printStackTrace();
      System.exit(-1);
    }
  }
}
