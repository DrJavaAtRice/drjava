/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.compiler.LanguageLevelStackTraceMapper;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.ConsoleDocumentInterface;


/* 2017-11-09 vuphan314 merged maladat/drjava/master
  into DrJavaAtRice/drjava/master */
// <<<<<<< HEAD
// /** Interactions model which can notify GlobalModelListeners on events.
//   * TODO: remove invokeLater wrappers here and enforce the policy that all of the listener methods must use them
//   * @version $Id$
//   */
// public class DefaultInteractionsModel extends RMIInteractionsModel {
//   /** Message to signal that input is required from the    console. */
// //  public static final String INPUT_REQUIRED_MESSAGE =
// //    "Please enter input in the Console tab." + _newLine;
//
//   public static final Log _log = new Log("GlobalModel.txt", false);
//
// =======
import java.io.File;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import javarepl.Evaluator;
import javarepl.ExpressionReader;
import javarepl.client.EvaluationLog;
import javarepl.client.EvaluationResult;
import javarepl.internal.totallylazy.Mapper;
import javarepl.internal.totallylazy.Option;
import javarepl.internal.totallylazy.Sequence;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

import static javarepl.internal.totallylazy.Option.none;

/**
 * Created by maladat on 1/21/16.
 */
public class DefaultInteractionsModel extends InteractionsModel {
  public static final Log _log = new Log("InteractionsModel.txt", false);

// >>>>>>> f3d81242dd8e22323379022fe3496dcc5d2b01d4
  /** The global model that includes this interactions model.  Provides access to interpreter and console. */
  protected final DefaultGlobalModel _model;

  public DrJavaRepl _drJavaRepl;

  public DrJavaExpressionReader _expressionReader;

  /** Creates a new InteractionsModel.
   * @param model DefaultGlobalModel to do the interpretation
   * @param cDoc document
   * @param wd  the working directory for interactions i/o
   */
  public DefaultInteractionsModel(DefaultGlobalModel model, ConsoleDocumentInterface cDoc, File wd) {
    super(cDoc, wd, DrJava.getConfig().getSetting(OptionConstants.HISTORY_MAX_SIZE).intValue(),
            WRITE_DELAY);
    _model = model;

    try {
      _drJavaRepl = new DrJavaRepl(wd.getAbsolutePath());
      _expressionReader = new DrJavaExpressionReader();
    } catch (Exception e) {
      //TODO : Better exception handling.
      e.printStackTrace();
    }

    // Set whether to allow "assert" statements to be run in the remote JVM.
    Boolean allow = DrJava.getConfig().getSetting(OptionConstants.RUN_WITH_ASSERT);
    // TODO: Tell interpreter whether to allow assertions.

    // Add option listeners  // WHEN ARE THESE EVER REMOVED?
    DrJava.getConfig().addOptionListener(OptionConstants.HISTORY_MAX_SIZE, _document.getHistoryOptionListener());
    DrJava.getConfig().addOptionListener(OptionConstants.RUN_WITH_ASSERT,
            new OptionListener<Boolean>() {
              public void optionChanged(OptionEvent<Boolean> oce) {
                // TODO: Tell interpreter whether to allow assertions when option changes.
              }
            });
  }

  /** Called when the repl prints to System.out.  This method can safely be called from outside the event thread.
   * @param s String to print
   */
  public void replSystemOutPrint(String s) {
    super.replSystemOutPrint(s); // Print s to interactions pane
    _model.systemOutPrint(s);    // Print s to console
  }

  /** Called when the repl prints to System.err.  This method can safely be called from outside the event thread.
   * @param s String to print
   */
  public void replSystemErrPrint(String s) {
    super.replSystemErrPrint(s);
    _model.systemErrPrint(s);
    _log.log("Printing string '" + s + "'");
  }

  /** Returns a line of text entered by the user at the equivalent of System.in.  This method may be safely called
   * from outside the event thread. */
  public String getConsoleInput() {
    String s = super.getConsoleInput();
    _log.log("Returning '" + s + "' as console input");
    _model.systemInEcho(s);
    return s;
  }

  /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
   * @param t The Throwable thrown by System.exit
   */
  protected void _interpreterResetFailed(final Throwable t) {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _log.log("Reset Failed! See the console tab for details.");
        _document.insertBeforeLastPrompt("Reset Failed! See the console tab for details." + StringOps.NEWLINE,
                InteractionsDocument.ERROR_STYLE);
        // Print the exception to the console
        _model.systemErrPrint(StringOps.getStackTrace(t));  // redundantly moves code to event thread
      }
    });
  }

  /** Any extra action to perform (beyond notifying listeners) when the interpreter won't start.
   * @param e The Exception indicating the interpreter won't start
   */
  protected void _interpreterWontStart(final Exception e) {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        String output = "JVM failed to start.  Make sure a firewall is not blocking " +
                StringOps.NEWLINE + "inter-process communication.  See the console tab for details." + StringOps.NEWLINE;
        _log.log("inserted '" + output + "' before prompt");
        _document.insertBeforeLastPrompt(output, InteractionsDocument.ERROR_STYLE);
        // Print the exception to the console
        _model.systemErrPrint(StringOps.getStackTrace(e));  // redundantly moves code to event thread
      }
    });
  }

  /** Called when the Java interpreter is ready to use.  This method body adds actions that involve the global model.
   * This method may run outside the event thread.
   */
  public void interpreterReady(File wd) {
    _log.log("****In DefaultInteractionsModel, interpreterReady(" + wd + ") called, resetting interactionsClassPath");
    _model.resetInteractionsClassPath();  // Done here rather than in the superclass because _model is available here.
    super.interpreterReady(wd);
  }

  /** In the event thread, notifies listeners that an interaction has started. */
  public void _notifyInteractionStarted() {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interactionStarted();
      _log.log("****Event: an interaction has started");
    } });
  }

  /** In the event thread, notifies listeners that an interaction has ended. */
  protected void _notifyInteractionEnded() {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interactionEnded();
      _log.log("****Event: the current interaction has ended");
    } });
  }

  /** In the event thread, notifies listeners that an error was present in the interaction. */
  protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interactionErrorOccurred(offset,length);
      _log.log("****Event: a syntax error occurred in the interactions model");
    } });
  }

  /** In the event thread, notifies listeners that the interpreter has changed.
   * @param inProgress Whether the new interpreter is currently in progress.
   */
  protected void _notifyInterpreterChanged(final boolean inProgress) {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interpreterChanged(inProgress);
      _log.log("****Event: the interpreter was changed; inProgess = " + inProgress);
    } });
  }

  /** In the event thread, notifies listeners that the interpreter is resetting. */
  protected void _notifyInterpreterResetting() {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interpreterResetting();
      _log.log("****Event: the interpreter is resetting");
    } });
  }

  /** In the event thread, notifies listeners that the interpreter is ready. Sometimes called from outside the event
   * thread. */
  public void _notifyInterpreterReady(final File wd) {
//    System.out.println("Asynchronously notifying interpreterReady event listeners");  // DEBUG
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interpreterReady(wd);
      _log.log("****Event: the interpreter is ready with wd " + wd);
    } });
  }

  /** In the event thread, notifies listeners that the interpreter has exited unexpectedly.
   * @param status Status code of the dead process
   */
  protected void _notifyInterpreterExited(final int status) {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interpreterExited(status);
      _log.log("****Event: the interpreter exited unexpectedly with status = " + status);
    } });
  }

  /** In the event thread, notifies listeners that the interpreter reset failed.
   * @param t Throwable causing the failure
   */
  protected void _notifyInterpreterResetFailed(final Throwable t) {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interpreterResetFailed(t);
      _log.log("****Event: interpreter reset failed");
    } });
  }

  /** In the event thread, notifies the view that the current interaction is incomplete. */
  protected void _notifyInteractionIncomplete() {
    Utilities.invokeLater(new Runnable() { public void run() {
      _notifier.interactionIncomplete();
      _log.log("****Event: the current interaction is incomplete");
    } });
  }

  public ConsoleDocument getConsoleDocument() { return _model.getConsoleDocument(); }

  /** Overides method in InteractionModel.java and changes stackTrace for athrowable if LL files are present
    * @param stackTrace the stack trace to change files name and line number in
    * @return stack trace with replaced file name and line number (if throwable occured in a .dj* file)
    */
  public StackTraceElement[] replaceLLException(StackTraceElement[] stackTrace) {
    // use LLSTM from compiler model.
    LanguageLevelStackTraceMapper LLSTM = _model.getCompilerModel().getLLSTM();
    final List<File> files = new ArrayList<File>();
    for(OpenDefinitionsDocument odd: _model.getLLOpenDefinitionsDocuments()) { files.add(odd.getRawFile()); }

    return (LLSTM.replaceStackTrace(stackTrace,files));
  }

  /** A compiler can instruct DrJava to include additional elements for the boot
   * class path of the Interactions JVM. */
  public List<File> getCompilerBootClassPath() {
    return _model.getCompilerModel().getActiveCompiler().additionalBootClassPathForInteractions();
  }

  /** Transform the command line to be interpreted into something the Interactions JVM can use.
   * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
   * "import MyClass" is not handled here.
   * @param interactionsString unprocessed command line
   * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
    return _model.getCompilerModel().getActiveCompiler().transformCommands(interactionsString);
  }

  /** Interprets the given command.
   * @param toEval command to be evaluated
   */
  protected void _interpret(String toEval) {
    debug.logStart("Interpret " + toEval);

    Option<String> expression = none();
    Option<EvaluationResult> result = none();
    expression = this._expressionReader.readExpression(toEval);
    if (!expression.isEmpty()) {
      try {
        result = this._drJavaRepl.client.execute(expression.get());
      } catch (Exception e) {
        //TODO: Handle exception more gracefully.
        e.printStackTrace();
      }
      if (!result.isEmpty()) {
        for (EvaluationLog log : result.get().logs()) {
          this.append(log.message() + StringOps.NEWLINE, InteractionsDocument.DEFAULT_STYLE);
        }
        this.replReturnedVoid();
      } else {
        this.replReturnedVoid();
      }
    } else {
      this.replReturnedVoid();
    }

    debug.logEnd();
  }

  /** Adds the given path to the interpreter's class path.
   * @param f  the path to add
   */
  public void addProjectClassPath(File f) {
    try {
      this._drJavaRepl.client.execute(":cp " + f.getAbsolutePath());
    } catch (Exception e) {
      //TODO: Handle exception more gracefully.
      e.printStackTrace();
    }
  }

  /** These add the given path to the build directory class paths used in the interpreter.
   * @param f  the path to add
   */
  public void addBuildDirectoryClassPath(File f) {
    try {
      this._drJavaRepl.client.execute(":cp " + f.getAbsolutePath());
    } catch (Exception e) {
      //TODO: Handle exception more gracefully.
      e.printStackTrace();
    }
  }

  /** These add the given path to the project files class paths used in the interpreter.
   * @param f  the path to add
   */
  public void addProjectFilesClassPath(File f) {
    try {
      this._drJavaRepl.client.execute(":cp " + f.getAbsolutePath());
    } catch (Exception e) {
      //TODO: Handle exception more gracefully.
      e.printStackTrace();
    }
  }

  /** These add the given path to the external files class paths used in the interpreter.
   * @param f  the path to add
   */
  public void addExternalFilesClassPath(File f) {
    try {
      this._drJavaRepl.client.execute(":cp " + f.getAbsolutePath());
    } catch (Exception e) {
      //TODO: Handle exception more gracefully.
      e.printStackTrace();
    }
  }

  /** These add the given path to the extra class paths used in the interpreter.
   * @param f  the path to add
   */
  public void addExtraClassPath(File f) {
    try {
      this._drJavaRepl.client.execute(":cp " + f.getAbsolutePath());
    } catch (Exception e) {
      //TODO: Handle exception more gracefully.
      e.printStackTrace();
    }
  }

  /** Resets the Java interpreter. */
  protected void _resetInterpreter(File wd, boolean force) {
    try {
      this._drJavaRepl.client.execute(":quit");
      this._drJavaRepl = new DrJavaRepl(wd.getAbsolutePath());
    } catch (Exception e) {
      //TODO: Handle exception more gracefully.
      e.printStackTrace();
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

  /** Gets the string representation of the value of a variable in the current interpreter.
   * @param var the name of the variable
   * @return A string representation of the value, or {@code null} if the variable is not defined.
   */
  public Pair<String,String> getVariableToString(String var) {
    //TODO: Implement variable value finder. Check old DefaultInteractionsModel implementation.
    return new Pair<String,String>(var, "unimplemented");
  }

  public void dispose() {
    _drJavaRepl.dispose();
  }
}
