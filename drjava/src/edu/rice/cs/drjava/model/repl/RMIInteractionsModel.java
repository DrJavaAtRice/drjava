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

import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.ui.InteractionsController;
import edu.rice.cs.util.text.ConsoleDocumentInterface;

import java.io.File;

/** An InteractionsModel which can serve as the glue between a local InteractionsDocument and a remote JavaInterpreter
  * in another JVM.
  * @version $Id$
  */
public abstract class RMIInteractionsModel extends InteractionsModel {

  /** RMI interface to the remote Java interpreter.*/
  protected final MainJVM _jvm;

  /** Constructs an InteractionsModel which can communicate with another JVM.
   *  @param jvm RMI interface to the slave JVM
   *  @param adapter InteractionsDJDocument to use in the InteractionsDocument
   *  @param historySize Number of lines to store in the history
   *  @param writeDelay Number of milliseconds to wait after each println
   */
  public RMIInteractionsModel(MainJVM jvm, ConsoleDocumentInterface adapter, File wd, int historySize, int writeDelay) {
    super(adapter, wd, historySize, writeDelay);
    _jvm = jvm;
  }

  /** Interprets the given command.
    * @param toEval command to be evaluated
    */
  protected void _interpret(String toEval) { _jvm.interpret(toEval); }

  /** Gets the string representation of the value of a variable in the current interpreter.
    * @param var the name of the variable
    */
  public String getVariableToString(String var) { return _jvm.getVariableToString(var); }

  /**
   * Gets the class name of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableClassName(String var) {
    return _jvm.getVariableClassName(var);
  }

  /**
   * Adds the given path to the interpreter's classpath.
   * @param path Path to add
   */
//  public void addToClassPath(String path) {
//    _interpreterControl.addClassPath(path);
//  }
//  
  public void addProjectClassPath(File f) { _jvm.addProjectClassPath(f); }

  public void addBuildDirectoryClassPath(File f) { _jvm.addBuildDirectoryClassPath(f); }
  
  public void addProjectFilesClassPath(File f) { 
//    System.err.println("Adding " + path + " to projectFilesClassPath in the slave JVM");
    _jvm.addProjectFilesClassPath(f); 
  }
  
  public void addExternalFilesClassPath(File f) { _jvm.addExternalFilesClassPath(f); }
  
  public void addExtraClassPath(File f) { _jvm.addExtraClassPath(f); }
  
  /** Resets the Java interpreter. */
  protected void _resetInterpreter(File wd) { _jvm.killInterpreter(wd); }

  /** Adds a named DynamicJavaAdapter to the list of interpreters.
   *  @param name the unique name for the interpreter
   *  @throws IllegalArgumentException if the name is not unique
   */
  public void addJavaInterpreter(String name) { _jvm.addJavaInterpreter(name); }

  /** Adds a named JavaDebugInterpreter to the list of interpreters.
   *  @param name the unique name for the debug interpreter
   *  @param className the fully qualified class name of the class the debug interpreter is in
   *  @throws IllegalArgumentException if the name is not unique
   */
  public void addDebugInterpreter(String name, String className) {
    _jvm.addDebugInterpreter(name, className);
  }

  /** Removes the interpreter with the given name, if it exists.
   *  @param name Name of the interpreter to remove
   */
  public void removeInterpreter(String name) {
    _jvm.removeInterpreter(name);
  }

  /** Sets the active interpreter.
   *  @param name the (unique) name of the interpreter.
   *  @param prompt the prompt the interpreter should have.
   */
  public void setActiveInterpreter(String name, String prompt) {
    String currName = _jvm.getCurrentInterpreterName();
    boolean inProgress = _jvm.setActiveInterpreter(name);
    _updateDocument(prompt, inProgress, !currName.equals(name));
    _notifyInterpreterChanged(inProgress);
  }

  /** Sets the default interpreter to be the current one. */
  public void setToDefaultInterpreter() {
    // Only print prompt if we're not already the default
    String currName = _jvm.getCurrentInterpreterName();
    boolean printPrompt = !MainJVM.DEFAULT_INTERPRETER_NAME.equals(currName);

    boolean inProgress = _jvm.setToDefaultInterpreter();

    _updateDocument(InteractionsDocument.DEFAULT_PROMPT, inProgress, printPrompt);
    _notifyInterpreterChanged(inProgress);
  }

  /** Updates the prompt and status of the document after an interpreter change.  Assumes write lock is already held.
   *  @param prompt New prompt to display
   *  @param inProgress whether the interpreter is currently in progress
   *  @param updatePrompt whether or not the interpreter has changed
   */
  private void _updateDocument(String prompt, boolean inProgress, boolean updatePrompt) {
    if (updatePrompt) {
      _document.acquireWriteLock();
      try {
        _document.setPrompt(prompt);
        _document.insertNewLine(_document.getLength());
        _document.insertPrompt();
      }
      finally { _document.releaseWriteLock(); }      
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
    _jvm.setPrivateAccessible(allow);
  }

  /** Gets the interpreter classpath from the interpreter jvm.
   * @return a vector of classpath elements
   */
  public Iterable<File> getClassPath() { return _jvm.getClassPath(); }
}
