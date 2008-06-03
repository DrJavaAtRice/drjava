/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.ui.InteractionsController;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocumentInterface;

import java.io.File;
import java.awt.EventQueue;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A Swing specific InteractionsModel which can serve as the glue between a local InteractionsDocument and a remote 
  * JavaInterpreter in another JVM.
  * @version $Id$
  */
public abstract class RMIInteractionsModel extends InteractionsModel {
  
  /** RMI interface to the remote Java interpreter.*/
  protected final MainJVM _jvm;
  
  /** Constructs an InteractionsModel which can communicate with another JVM.
    * @param jvm RMI interface to the slave JVM
    * @param adapter InteractionsDJDocument to use in the InteractionsDocument
    * @param historySize Number of lines to store in the history
    * @param writeDelay Number of milliseconds to wait after each println
    */
  public RMIInteractionsModel(MainJVM jvm, ConsoleDocumentInterface adapter, File wd, int historySize, int writeDelay) {
    super(adapter, wd, historySize, writeDelay);
    _jvm = jvm;
  }
  
  /** Interprets the given command.
    * @param toEval command to be evaluated
    */
  protected 
    void _interpret(String toEval) {
    debug.logStart("Interpret " + toEval);
    _jvm.interpret(toEval);
    debug.logEnd();
  }
  
  /** Gets the string representation of the value of a variable in the current interpreter.
    * @param var the name of the variable
    */
  public String getVariableToString(String var) { return _jvm.getVariableToString(var); }
  
  /** Gets the class name of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public String getVariableType(String var) {
    return _jvm.getVariableType(var);
  }
  
  /** Adds the given path to the interpreter's classpath.
    * @param file  the path to add
    */
//  public void addToClassPath(String path) {
//    _interpreterControl.addClassPath(path);
//  }
//  
  /** Adds the given path to the interpreter's classpath.
    * @param f  the path to add
    */
  public void addProjectClassPath(File f) { _jvm.addProjectClassPath(f); }
  
  /** These add the given path to the build directory classpaths used in the interpreter.
    * @param f  the path to add
    */
  public void addBuildDirectoryClassPath(File f) { _jvm.addBuildDirectoryClassPath(f); }
  
  /** These add the given path to the project files classpaths used in the interpreter.
    * @param f  the path to add
    */
  public void addProjectFilesClassPath(File f) { 
//    System.err.println("Adding " + path + " to projectFilesClassPath in the slave JVM");
    _jvm.addProjectFilesClassPath(f); 
  }
  
  /** These add the given path to the external files classpaths used in the interpreter.
    * @param f  the path to add
    */
  public void addExternalFilesClassPath(File f) { _jvm.addExternalFilesClassPath(f); }
  
  /** These add the given path to the extra classpaths used in the interpreter.
    * @param f  the path to add
    */
  public void addExtraClassPath(File f) { _jvm.addExtraClassPath(f); }
  
  /** Resets the Java interpreter. */
  protected void _resetInterpreter(File wd) { _jvm.killInterpreter(wd); }
  
  /** Adds a named interpreter to the list.
    * @param name the unique name for the interpreter
    * @throws IllegalArgumentException if the name is not unique
    */
  public void addInterpreter(String name) { _jvm.addInterpreter(name); }
  
  /** Removes the interpreter with the given name, if it exists.
    * @param name Name of the interpreter to remove
    */
  public void removeInterpreter(String name) {
    _jvm.removeInterpreter(name);
  }
  
  /** Sets the active interpreter.
    * @param name the (unique) name of the interpreter.
    * @param prompt the prompt the interpreter should have.
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
  
  /** Updates the prompt and status of the document after an interpreter change.
    * Must run in event thread.
    * @param prompt New prompt to display
    * @param inProgress whether the interpreter is currently in progress
    * @param updatePrompt whether or not the interpreter has changed
    */
  private void _updateDocument(final String prompt, final boolean inProgress, boolean updatePrompt) {
    if (updatePrompt) {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          _document.acquireWriteLock();
          try {
            _document.setPrompt(prompt);
            _document.insertNewline(_document.getLength());
            _document.insertPrompt();
//            int len = _document.getPromptLength();  
//            advanceCaret(len);
            _document.setInProgress(inProgress);
          }
          finally { _document.releaseWriteLock(); }
        }
      });
      scrollToCaret();
    }   
  }
  
  /** Notifies listeners that the interpreter has changed. (Subclasses must maintain listeners.)
    * @param inProgress Whether the new interpreter is currently in progress with an interaction, i.e., whether 
    *        an interactionEnded event will be fired)
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
  
  /** Enables restarting of slave JVM. */
  public void enableRestart() { _jvm.enableRestart(); }
}
