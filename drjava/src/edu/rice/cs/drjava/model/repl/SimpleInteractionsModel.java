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

package edu.rice.cs.drjava.model.repl;

import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.*;

// TODO: Is synchronization used properly here?
import java.util.Vector;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;

/**
 * A simple implementation of an InteractionsModel, which uses a
 * DynamicJavaAdatper directly (in the same JVM) to interpret code.
 * This can be used in a standalone interface, such as
 * edu.rice.cs.drjava.ui.SimpleInteractionsWindow.
 * @version $Id$
 */
public class SimpleInteractionsModel extends InteractionsModel {
  
  /** Milliseconds to wait after each println */
  protected static final int WRITE_DELAY = 50;

  /**
   * An interpreter to evaluate interactions.
   */
  protected JavaInterpreter _interpreter;

  /**
   * List of listeners to this document.
   */
  protected final Vector<InteractionsListener> _listeners;

  /**
   * Creates a new InteractionsModel using a SwingDocumentAdapter.
   */
  public SimpleInteractionsModel() {
    this(new SwingDocumentAdapter());
  }
  
  /**
   * Creates a new InteractionsModel with the given document adapter.
   * @param document Toolkit-independent document adapter
   */
  public SimpleInteractionsModel(DocumentAdapter document) {
    super(document, 1000, WRITE_DELAY);
    _interpreter = new DynamicJavaAdapter();
    _listeners = new Vector<InteractionsListener>();
    
    _interpreter.defineVariable("INTERPRETER", _interpreter);
  }
  
  /**
   * Interprets the given command.
   * @param toEval command to be evaluated
   */
  protected void _interpret(String toEval) {
    try {
      Object result = _interpreter.interpret(toEval);
      if (result != Interpreter.NO_RESULT) {
        _docAppend(String.valueOf(result) + System.getProperty("line.separator"), 
                   InteractionsDocument.DEFAULT_STYLE);
      }
    }
    catch (ExceptionReturnedException e) {
      Throwable t = e.getContainedException();
      // getStackTrace should be a utility method somewhere...
      _document.appendExceptionResult(t.getClass().getName(),
                                      t.getMessage(),
                                      InterpreterJVM.getStackTrace(t),
                                      InteractionsDocument.DEFAULT_STYLE);
    }
    finally {
      _interactionIsOver();
    }
  }
  
  /**
   * Gets the string representation of the value of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableToString(String var) {
    Object value = _interpreter.getVariable(var);
    return value.toString();
  }
  
  /**
   * Gets the class name of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableClassName(String var) {
    Class c = _interpreter.getVariableClass(var);
    return c.getName();
  }
  
  /**
   * Adds the given path to the interpreter's classpath.
   * @param path Path to add
   */
  public void addToClassPath(String path) {
    _interpreter.addClassPath(path);
  }
  
  /**
   * Defines a variable in the interpreter to the given value.
   */
  public void defineVariable(String name, Object value) {
    _interpreter.defineVariable(name, value);
  }
  
  /**
   * Defines a final variable in the interpreter to the given value.
   */
  public void defineConstant(String name, Object value) {
    _interpreter.defineConstant(name, value);
  }
  
  /**
   * Sets whether protected and private variables and methods can be accessed 
   * from within the interpreter.
   */
  public void setInterpreterPrivateAccessible(boolean accessible) {
    _interpreter.setPrivateAccessible(accessible);
  }
  
  /**
   * Does not allow input from System.in.
   * @return empty string
   */
  public String getConsoleInput() {
    _document.insertBeforeLastPrompt("Cannot read from System.in." + _newLine,
                                     InteractionsDocument.ERROR_STYLE);
    return "";
  }
  
  /**
   * Any extra action to perform (beyond notifying listeners) when
   * the interpreter fails to reset.
   * @param t The Throwable thrown by System.exit
   */
  protected void _interpreterResetFailed(Throwable t) {
    _document.insertBeforeLastPrompt("Reset Failed!" + _newLine,
                                     InteractionsDocument.ERROR_STYLE);
  }
  
  /**
   * Resets the Java interpreter.
   */
  protected void _resetInterpreter() {
    interpreterResetting();
    _interpreter = new DynamicJavaAdapter();
    interpreterReady();
  }

  /**
   * Adds a listener to this model.
   */
  public void addInteractionsListener(InteractionsListener l) {
    _listeners.add(l);
  }

  /**
   * Removes the given listener from this model.
   */
  public void removeInteractionsListener(InteractionsListener l) {
    _listeners.remove(l);
  }

  /**
   * Removes all listeners from this model.
   */
  public void removeAllInteractionListeners() {
    _listeners.clear();
  }

  
  /**
   * Notifies listeners that an interaction has started.
   */
  protected void _notifyInteractionStarted() {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.get(i).interactionStarted();
    }
  }
  
  /**
   * Notifies listeners that an interaction has ended.
   */
  protected void _notifyInteractionEnded() {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.get(i).interactionEnded();
    }
  }
  
  /**
   * Notifies listeners that an interaction contained
   * a syntax error.
   */
  protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.get(i).interactionErrorOccurred(offset, length);
    }
  }
  
  /**
   * Notifies listeners that the interpreter is resetting.
   */
  protected void _notifyInterpreterResetting() {
    // Ok, we don't need to do anything special
  }
  
  /**
   * Notifies listeners that the interpreter is ready.
   */
  protected void _notifyInterpreterReady() {
    //  Ok, we don't need to do anything special
  }
  
  /**
   * Notifies listeners that the interpreter has exited unexpectedly.
   * @param status Status code of the dead process
   */
  protected void _notifyInterpreterExited(final int status) {
    // Won't happen in a single JVM
  }
  
  /**
   * Notifies listeners that the interpreter reset failed.
   */
  protected void _notifyInterpreterResetFailed(Throwable t) {
    // Won't happen in a single JVM
  }

}
