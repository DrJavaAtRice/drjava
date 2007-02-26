/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.io.File;


import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;
import edu.rice.cs.drjava.model.repl.newjvm.ClassPathManager;

import edu.rice.cs.util.swing.Utilities;

import edu.rice.cs.util.text.ConsoleDocument;

/** A simple implementation of InteractionsModel, which uses a DynamicJavaAdapter directly (in the same JVM) to 
 *  interpret code.  It can be used in a standalone interface, such as edu.rice.cs.drjava.ui.SimpleInteractionsWindow.
 *  @version $Id$
 */
public class SimpleInteractionsModel extends InteractionsModel {

  /** Milliseconds to wait after each println */
  protected static final int WRITE_DELAY = 5;

  /** An interpreter to evaluate interactions. */
  protected JavaInterpreter _interpreter;

  /** Creates a new InteractionsModel using a InteractionsDJDocument. */
  public SimpleInteractionsModel() { this(new InteractionsDJDocument()); }

  /** Creates a new InteractionsModel with the given document adapter.
   *  @param document Toolkit-independent document adapter
   */
  public SimpleInteractionsModel(InteractionsDJDocument document) {
    super(document, new File(System.getProperty("user.dir")), 1000, WRITE_DELAY);
    _interpreter = new DynamicJavaAdapter(new ClassPathManager());

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
        append(String.valueOf(result) + System.getProperty("line.separator"),
                   InteractionsDocument.OBJECT_RETURN_STYLE);
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
    finally { _interactionIsOver(); }
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

  /** Adds the given path to the interpreter's classpath.
   *  @param path Path to add
   */
  public void addProjectClassPath(File path) { _interpreter.addProjectClassPath(path); }

  /** Adds the given path to the interpreter's classpath.
   *  @param path Path to add
   */
  public void addBuildDirectoryClassPath(File path) { _interpreter.addBuildDirectoryClassPath(path); }

  /** Adds the given path to the interpreter's classpath.
   *  @param path Path to add
   */
  public void addProjectFilesClassPath(File path) { _interpreter.addProjectFilesClassPath(path); }

  /** Adds the given path to the interpreter's classpath.
   *  @param path Path to add
   */
  public void addExternalFilesClassPath(File path) { _interpreter.addExternalFilesClassPath(path); }

  /** Adds the given path to the interpreter's classpath.
   *  @param path Path to add
   */
  public void addExtraClassPath(File path) { _interpreter.addExtraClassPath(path); }


  /** Defines a variable in the interpreter to the given value. */
  public void defineVariable(String name, Object value) { _interpreter.defineVariable(name, value); }

  /** Defines a final variable in the interpreter to the given value. */
  public void defineConstant(String name, Object value) { _interpreter.defineConstant(name, value); }

  /** Sets whether protected and private variables and methods can be accessed from within the interpreter. */
  public void setInterpreterPrivateAccessible(boolean accessible) { _interpreter.setPrivateAccessible(accessible); }

  /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
   *  @param t The Throwable thrown by System.exit
   */
  protected void _interpreterResetFailed(Throwable t) {
    _document.insertBeforeLastPrompt("Reset Failed!" + _newLine, InteractionsDocument.ERROR_STYLE);
  }

  /** Resets the Java interpreter. */
  protected void _resetInterpreter(File wd) {
    interpreterResetting();
    _interpreter = new DynamicJavaAdapter(new ClassPathManager());
    interpreterReady(wd);
  }

  /** Notifies listeners that an interaction has started. */
  protected void _notifyInteractionStarted() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionStarted(); } });
  }

  /** Notifies listeners that an interaction has ended. */
  protected void _notifyInteractionEnded() {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionEnded(); } });
  }

  /** Notifies listeners that an interaction contained a syntax error. */
  protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionErrorOccurred(offset, length); } });
  }

  /** Notifies listeners that the interpreter is resetting. */
  protected void _notifyInterpreterResetting() {  /* do nothing */  }

  /** Notifies listeners that the interpreter is ready.  */
  public void _notifyInterpreterReady(File wd) {
    //  Ok, we don't need to do anything special
  }

  /** Notifies listeners that the interpreter has exited unexpectedly.
   *  @param status Status code of the dead process
   */
  protected void _notifyInterpreterExited(final int status) {
    // Won't happen in a single JVM
  }

  /** Notifies listeners that the interpreter reset failed. */
  protected void _notifyInterpreterResetFailed(Throwable t) {
    // Won't happen in a single JVM
  }

  /** Notifies listeners that the interperaction was incomplete. */
  protected void _notifyInteractionIncomplete() {
    // Oh well.  Nothing to do.
  }
  
  /** Notifies listeners that the slave JVM has been used. */
  protected void _notifySlaveJVMUsed() { /* do nothing; no slave JVM */ }
   
  /** Returns null because console tab document is not supported in this model */
  public ConsoleDocument getConsoleDocument() { return null; }
}
