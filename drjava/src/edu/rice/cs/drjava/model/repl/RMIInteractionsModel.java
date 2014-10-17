/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.text.ConsoleDocumentInterface;

import java.io.File;
import java.awt.EventQueue;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** A Swing specific InteractionsModel which can serve as the glue between a local InteractionsDocument and a remote 
  * JavaInterpreter in another JVM.
  * @version $Id: RMIInteractionsModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class RMIInteractionsModel extends InteractionsModel {
  
  /** RMI interface to the remote Java interpreter.*/
  protected final MainJVM _jvm;
  
  /** Constructs an InteractionsModel which can communicate with another JVM.
    * @param jvm RMI interface to the slave JVM
    * @param cDoc document to use in the InteractionsDocument
    * @param historySize Number of lines to store in the history
    * @param writeDelay Number of milliseconds to wait after each println
    */
  public RMIInteractionsModel(MainJVM jvm, ConsoleDocumentInterface cDoc, File wd, int historySize, int writeDelay) {
    super(cDoc, wd, historySize, writeDelay);
    _jvm = jvm;
  }
  
  /** Interprets the given command.
    * @param toEval command to be evaluated
    */
  protected void _interpret(String toEval) {
    debug.logStart("Interpret " + toEval);
    _jvm.interpret(toEval);
    debug.logEnd();
  }
  
  /** Gets the string representation of the value of a variable in the current interpreter.
    * @param var the name of the variable
    */
  public Pair<String,String> getVariableToString(String var) {
    System.out.println("getVariableToString: "+var);
    Option<Pair<String,String>> result = _jvm.getVariableToString(var);
    System.out.println("\tresult.isNone? " + result.isNone());
    Pair<String,String> retval = result.unwrap(new Pair<String,String>("",""));
    System.out.println("\tretval: " + retval);
    return retval;
  }
  
  /** Adds the given path to the interpreter's class path.
    * @param f  the path to add
    */
  public void addProjectClassPath(File f) { _jvm.addProjectClassPath(f); }
  
  /** These add the given path to the build directory class paths used in the interpreter.
    * @param f  the path to add
    */
  public void addBuildDirectoryClassPath(File f) { _jvm.addBuildDirectoryClassPath(f); }
  
  /** These add the given path to the project files class paths used in the interpreter.
    * @param f  the path to add
    */
  public void addProjectFilesClassPath(File f) { _jvm.addProjectFilesClassPath(f); }
  
  /** These add the given path to the external files class paths used in the interpreter.
    * @param f  the path to add
    */
  public void addExternalFilesClassPath(File f) { _jvm.addExternalFilesClassPath(f); }
  
  /** These add the given path to the extra class paths used in the interpreter.
    * @param f  the path to add
    */
  public void addExtraClassPath(File f) { _jvm.addExtraClassPath(f); }
  
  /** Resets the Java interpreter. */
  protected void _resetInterpreter(File wd, boolean force) {
    setToDefaultInterpreter();
    _jvm.setWorkingDirectory(wd);
    _jvm.restartInterpreterJVM(force);
  }
  
  /** Adds a named interpreter to the list.
    * @param name the unique name for the interpreter
    * @throws IllegalArgumentException if the name is not unique
    */
  public void addInterpreter(String name) { _jvm.addInterpreter(name); }
  
  /** Removes the interpreter with the given name, if it exists.
    * @param name Name of the interpreter to remove
    */
  public void removeInterpreter(String name) { _jvm.removeInterpreter(name); }
  
  /** Sets the active interpreter.
    * @param name the (unique) name of the interpreter.
    * @param prompt the prompt the interpreter should have.
    */
  public void setActiveInterpreter(String name, String prompt) {
    Option<Pair<Boolean, Boolean>> result = _jvm.setActiveInterpreter(name);
    debug.logValue("result", result);
    if (result.isSome() && result.unwrap().first()) { // interpreter changed
      boolean inProgress = result.unwrap().second();
      _updateDocument(prompt, inProgress);
      _notifyInterpreterChanged(inProgress);
    }
  }
  
  /** Sets the default interpreter to be the current one. */
  public void setToDefaultInterpreter() {
    Option<Pair<Boolean, Boolean>> result = _jvm.setToDefaultInterpreter();
    if (result.isSome() && result.unwrap().first()) { // interpreter changed
      boolean inProgress = result.unwrap().second();
      _updateDocument(InteractionsDocument.DEFAULT_PROMPT, inProgress);
      _notifyInterpreterChanged(inProgress);
    }
  }
  
  /** Updates the prompt and status of the document after an interpreter change.
    * Must run in event thread. (TODO: is it okay that related RMI calls occur in the event thread?)
    * @param prompt New prompt to display
    * @param inProgress whether the interpreter is currently in progress
    */
  private void _updateDocument(String prompt, boolean inProgress) {
    assert EventQueue.isDispatchThread();
    _document.setPrompt(prompt);
    _document.insertNewline(_document.getLength());
    _document.insertPrompt();
//            int len = _document.getPromptLength();  
//            advanceCaret(len);
    _document.setInProgress(inProgress);
    scrollToCaret();
  }
  
  /** Notifies listeners that the interpreter has changed. (Subclasses must maintain listeners.)
    * @param inProgress Whether the new interpreter is currently in progress with an interaction, i.e., whether 
    *        an interactionEnded event will be fired)
    */
  protected abstract void _notifyInterpreterChanged(boolean inProgress);

  /** Sets whether or not the interpreter should enforce access to all members. */
  public void setEnforceAllAccess(boolean enforce) { _jvm.setEnforceAllAccess(enforce); }
  
  /** Sets whether or not the interpreter should enforce access to private members. */
  public void setEnforcePrivateAccess(boolean enforce) { _jvm.setEnforcePrivateAccess(enforce); }

  /** Require a semicolon at the end of statements. */
  public void setRequireSemicolon(boolean require) { _jvm.setRequireSemicolon(require); }
  
  /** Require variable declarations to include an explicit type. */
  public void setRequireVariableType(boolean require) { _jvm.setRequireVariableType(require); }
  
  /** Gets the interpreter class path from the interpreter jvm.
    * @return a list of class path elements
    */
  public Iterable<File> getClassPath() { 
    Option<Iterable<File>> result = _jvm.getClassPath();
    return result.unwrap(IterUtil.<File>empty());
  }
  
}
