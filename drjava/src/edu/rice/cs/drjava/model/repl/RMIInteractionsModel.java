/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.util.text.EditDocumentInterface;

import java.net.URL;
import java.util.Vector;
import java.io.File;

/**
 * An InteractionsModel which can serve as the glue between a local
 * InteractionsDocument and a remote JavaInterpreter in another JVM.
 * @version $Id$
 */
public abstract class RMIInteractionsModel extends InteractionsModel {

  /** RMI interface to the remote Java interpreter.*/
  protected final MainJVM _interpreterControl;

  /** Constructs an InteractionsModel which can communicate with another JVM.
   *  @param control RMI interface to the Java interpreter
   *  @param adapter InteractionsDJDocument to use in the InteractionsDocument
   *  @param historySize Number of lines to store in the history
   *  @param writeDelay Number of milliseconds to wait after each println
   */
  public RMIInteractionsModel(MainJVM control, EditDocumentInterface adapter, int historySize, int writeDelay) {
    super(adapter, historySize, writeDelay);
    _interpreterControl = control;
  }

  /** Interprets the given command.
   *  @param toEval command to be evaluated
   */
  protected void _interpret(String toEval) { _interpreterControl.interpret(toEval); }

  /** Gets the string representation of the value of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public String getVariableToString(String var) {
    return _interpreterControl.getVariableToString(var);
  }

  /**
   * Gets the class name of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableClassName(String var) {
    return _interpreterControl.getVariableClassName(var);
  }

  /**
   * Adds the given path to the interpreter's classpath.
   * @param path Path to add
   */
//  public void addToClassPath(String path) {
//    _interpreterControl.addClassPath(path);
//  }
//  
  public void addProjectClassPath(URL path) { _interpreterControl.addProjectClassPath(path); }

  public void addBuildDirectoryClassPath(URL path) { _interpreterControl.addBuildDirectoryClassPath(path); }
  
  public void addProjectFilesClassPath(URL path) { _interpreterControl.addProjectFilesClassPath(path); }
  
  public void addExternalFilesClassPath(URL path) { _interpreterControl.addExternalFilesClassPath(path); }
  
  public void addExtraClassPath(URL path) { _interpreterControl.addExtraClassPath(path); }
  
  /** Resets the Java interpreter. */
  protected void _resetInterpreter(File wd) { _interpreterControl.killInterpreter(wd); }

  /** Adds a named DynamicJavaAdapter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @throws IllegalArgumentException if the name is not unique
   */
  public void addJavaInterpreter(String name) { _interpreterControl.addJavaInterpreter(name); }

  /** Adds a named JavaDebugInterpreter to the list of interpreters.
   *  @param name the unique name for the debug interpreter
   *  @param className the fully qualified class name of the class the debug interpreter is in
   *  @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name, String className) {
    _interpreterControl.addDebugInterpreter(name, className);
  }

  /** Removes the interpreter with the given name, if it exists.
   *  @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) {
    _interpreterControl.removeInterpreter(name);
  }

  /** Sets the active interpreter.
   *  @param name the (unique) name of the interpreter.
   *  @param prompt the prompt the interpreter should have.
   */
  public void setActiveInterpreter(String name, String prompt) {
    String currName = _interpreterControl.getCurrentInterpreterName();
    boolean inProgress = _interpreterControl.setActiveInterpreter(name);
    _updateDocument(prompt, inProgress, !currName.equals(name));
    _notifyInterpreterChanged(inProgress);
  }

  /** Sets the default interpreter to be the current one. */
  public void setToDefaultInterpreter() {
    // Only print prompt if we're not already the default
    String currName = _interpreterControl.getCurrentInterpreterName();
    boolean printPrompt = !MainJVM.DEFAULT_INTERPRETER_NAME.equals(currName);

    boolean inProgress = _interpreterControl.setToDefaultInterpreter();

    _updateDocument(InteractionsDocument.DEFAULT_PROMPT, inProgress, printPrompt);
    _notifyInterpreterChanged(inProgress);
  }

  /** Updates the prompt and status of the document after an interpreter change.
   *  @param prompt New prompt to display
   *  @param inProgress whether the interpreter is currently in progress
   *  @param updatePrompt whether or not the interpreter has changed
   */
  protected void _updateDocument(String prompt, boolean inProgress, boolean updatePrompt) {
    if (updatePrompt) {
      _document.setPrompt(prompt);
      _document.insertNewLine(_document.getLength());
      _document.insertPrompt();
    }
    _document.setInProgress(inProgress);
  }

  /** Notifies listeners that the interpreter has changed. (Subclasses must maintain listeners.)
   *  @param inProgress Whether the new interpreter is currently in progress with an interaction, i.e., whether 
   *         an interactionEnded event will be fired)
   */
  protected abstract void _notifyInterpreterChanged(boolean inProgress);

  /** Sets whether or not the interpreter should allow access to private members. */
  public void setPrivateAccessible(boolean allow) {
    _interpreterControl.setPrivateAccessible(allow);
  }

  /** Gets the interpreter classpath from the interpreter jvm.
   * @return a vector of classpath elements
   */
  public Vector<URL> getClassPath() { return _interpreterControl.getClassPath(); }
}