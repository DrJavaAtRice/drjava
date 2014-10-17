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

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import edu.rice.cs.drjava.model.repl.newjvm.ClassPathManager;
import edu.rice.cs.drjava.model.AbstractGlobalModel;

import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.OptionVisitor;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.text.TextUtil;

import edu.rice.cs.dynamicjava.Options;
import edu.rice.cs.dynamicjava.interpreter.Interpreter;
import edu.rice.cs.dynamicjava.interpreter.InterpreterException;
import edu.rice.cs.dynamicjava.interpreter.EvaluatorException;

/** A simple implementation of InteractionsModel, which uses a DynamicJavaAdapter directly (in the same JVM) to 
  * interpret code.  It can be used in a standalone interface, such as edu.rice.cs.drjava.ui.SimpleInteractionsWindow.
  * @version $Id: SimpleInteractionsModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class SimpleInteractionsModel extends InteractionsModel {  
  protected ClassPathManager _classPathManager;
  protected Interpreter _interpreter;
  protected final InteractionsPaneOptions _interpreterOptions;
  
  /** Creates a new InteractionsModel using a InteractionsDJDocument. */
  public SimpleInteractionsModel() { this(new InteractionsDJDocument()); }
  
  /** Creates a new InteractionsModel with the given document adapter.
    * @param document Toolkit-independent document adapter
    */
  public SimpleInteractionsModel(InteractionsDJDocument document) {
    super(document, new File(System.getProperty("user.dir")), 1000, WRITE_DELAY);
    _classPathManager = new ClassPathManager(ReflectUtil.SYSTEM_CLASS_PATH);
    _interpreterOptions = new InteractionsPaneOptions();
    _interpreter = new Interpreter(_interpreterOptions, _classPathManager.makeClassLoader(null));
    //_interpreter.defineVariable("INTERPRETER", _interpreter);
  }
  
  /** Interprets the given command.  Must run in event thread to properly sequence updating the interactions pane.
   * @param toEval command to be evaluated
   */
  protected void _interpret(String toEval) {
    try {
      Option<Object> result = _interpreter.interpret(toEval);
      if (result.isSome()) {
        String objString = null;
        try { objString = TextUtil.toString(result.unwrap()); }
        catch (Throwable t) { throw new EvaluatorException(t); }
        append(objString + "\n", InteractionsDocument.OBJECT_RETURN_STYLE);
      }
    }
    catch (InterpreterException e) {
      StringWriter msg = new StringWriter();
      e.printUserMessage(new PrintWriter(msg));
      _document.appendExceptionResult(msg.toString(), InteractionsDocument.DEFAULT_STYLE);
    }
    finally { _interactionIsOver(); }
  }
  
  /** Gets the string representation of the value of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public Pair<String,String> getVariableToString(String var) {
    try {
      Option<Object> value = _interpreter.interpret(var);
      try {
          return value.apply(new OptionVisitor<Object,Pair<String,String>>() {
              public Pair<String,String> forNone() {
                  return new Pair<String,String>("","");
              }
              public Pair<String,String> forSome(Object value) {
                  return new Pair<String,String>(TextUtil.toString(value),value.getClass().getName());
              }
          });
      }
      catch (Throwable t) { throw new EvaluatorException(t); }
    }
    catch (InterpreterException e) { return new Pair<String,String>("",""); }
  }
  
  /** Adds the given path to the interpreter's classpath.
    * @param path Path to add
    */
  public void addProjectClassPath(File path) { _classPathManager.addProjectCP(path); }
  
  /** Adds the given path to the interpreter's classpath.
    * @param path Path to add
    */
  public void addBuildDirectoryClassPath(File path) { _classPathManager.addBuildDirectoryCP(path); }
  
  /** Adds the given path to the interpreter's classpath.
    * @param path Path to add
    */
  public void addProjectFilesClassPath(File path) { _classPathManager.addProjectFilesCP(path); }
  
  /** Adds the given path to the interpreter's classpath.
    * @param path Path to add
    */
  public void addExternalFilesClassPath(File path) { _classPathManager.addExternalFilesCP(path); }
  
  /** Adds the given path to the interpreter's classpath.
    * @param path Path to add
    */
  public void addExtraClassPath(File path) { _classPathManager.addExtraCP(path); }
  
  /** Sets whether or not the interpreter should enforce access to all members. */
  public void setEnforceAllAccess(boolean enforce) { _interpreterOptions.setEnforceAllAccess(enforce); }
  
  /** Sets whether or not the interpreter should enforce access to private members. */
  public void setEnforcePrivateAccess(boolean enforce) { _interpreterOptions.setEnforcePrivateAccess(enforce); }

  /** Require a semicolon at the end of statements. */
  public void setRequireSemicolon(boolean require) { _interpreterOptions.setRequireSemicolon(require); }
  
  /** Require variable declarations to include an explicit type. */
  public void setRequireVariableType(boolean require) { _interpreterOptions.setRequireVariableType(require); }
  
  /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
    * @param t The Throwable thrown by System.exit
    */
  protected void _interpreterResetFailed(Throwable t) {
    _document.insertBeforeLastPrompt("Reset Failed!" + StringOps.NEWLINE, InteractionsDocument.ERROR_STYLE);
  }
  
  protected void _interpreterWontStart(Exception e) {
    _document.insertBeforeLastPrompt("JVM failed to start." + StringOps.NEWLINE, InteractionsDocument.ERROR_STYLE);
  }
  
  /** Resets the Java interpreter. */
  protected void _resetInterpreter(File wd, boolean force) {
    interpreterResetting();
    _classPathManager = new ClassPathManager(ReflectUtil.SYSTEM_CLASS_PATH);
    _interpreter = new Interpreter(Options.DEFAULT, _classPathManager.makeClassLoader(null));
    interpreterReady(wd);
  }
  
  /** Notifies listeners that an interaction has started. */
  public void _notifyInteractionStarted() { 
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
    * @param status Status code of the dead process
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
  
  /** Returns null because console tab document is not supported in this model */
  public ConsoleDocument getConsoleDocument() { return null; }
  
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. */
  public List<File> getCompilerBootClassPath() {
    // not supported
    // TODO: figure out what to do here
    return new ArrayList<File>();
  }
  
  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
    // TODO: figure out what to do here
    return interactionsString;
  }
}
