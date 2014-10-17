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
import java.util.List;
import java.util.ArrayList;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
//import edu.rice.cs.drjava.model.compiler.LanguageLevelStackTraceMapper;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.ConsoleDocumentInterface;
import edu.rice.cs.util.swing.Utilities;

/** Interactions model which can notify GlobalModelListeners on events.
  * TODO: remove invokeLater wrappers here and enforce the policy that all of the listener methods must use them
  * @version $Id: DefaultInteractionsModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class DefaultInteractionsModel extends RMIInteractionsModel {
  /** Message to signal that input is required from the console. */
//  public static final String INPUT_REQUIRED_MESSAGE =
//    "Please enter input in the Console tab." + _newLine;
  
  /** Model that contains the interpreter to use. */
  protected final DefaultGlobalModel _model;
  
  /** Creates a new InteractionsModel.
    * @param model DefaultGlobalModel to do the interpretation
    * @param jvm  the RMI interface used by the Main JVM to access the Interpreter JVM
    * @param cDoc document
    * @param wd  the working directory for interactions i/o
    */
  public DefaultInteractionsModel(DefaultGlobalModel model, MainJVM jvm, ConsoleDocumentInterface cDoc, File wd) {
    super(jvm, cDoc, wd, DrJava.getConfig().getSetting(OptionConstants.HISTORY_MAX_SIZE).intValue(),
          WRITE_DELAY);
    _model = model;
    // Set whether to allow "assert" statements to be run in the remote JVM.
    Boolean allow = DrJava.getConfig().getSetting(OptionConstants.RUN_WITH_ASSERT);
    _jvm.setAllowAssertions(allow.booleanValue());
    
    // Add option listeners  // WHEN ARE THESE EVER REMOVED?
    DrJava.getConfig().addOptionListener(OptionConstants.HISTORY_MAX_SIZE, _document.getHistoryOptionListener());
    DrJava.getConfig().addOptionListener(OptionConstants.RUN_WITH_ASSERT,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _jvm.setAllowAssertions(oce.value.booleanValue());
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
  }
  
  /** Returns a line of text entered by the user at the equivalent of System.in.  This method may be safely called
    * from outside the event thread. */
  public String getConsoleInput() { 
    String s = super.getConsoleInput();
//    System.err.println("Returning '" + s + "' as console input");
    _model.systemInEcho(s);
    return s; 
  }
  
  /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
    * @param t The Throwable thrown by System.exit
    */
  protected void _interpreterResetFailed(final Throwable t) {
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        _document.insertBeforeLastPrompt("Reset Failed! See the console tab for details." + StringOps.NEWLINE,
                                         InteractionsDocument.ERROR_STYLE);
         // Print the exception to the console
        _model.systemErrPrint(StringOps.getStackTrace(t));  // redundantly moves code to event thread
      }
    });
  }   
  
  protected void _interpreterWontStart(final Exception e) {
    Utilities.invokeLater(new Runnable() { 
      public void run() {
        _document.insertBeforeLastPrompt("JVM failed to start.  Make sure a firewall is not blocking " +
                                         StringOps.NEWLINE +
                                         "inter-process communication.  See the console tab for details." +
                                         StringOps.NEWLINE,
                                         InteractionsDocument.ERROR_STYLE);
         // Print the exception to the console
        _model.systemErrPrint(StringOps.getStackTrace(e));  // redundantly moves code to event thread
      }
    });
  }
  
  /** Called when the Java interpreter is ready to use.  This method body adds actions that involve the global model. 
    * This method may run outside the event thread. 
    */
  public void interpreterReady(File wd) {
    _model.resetInteractionsClassPath();  // Done here rather than in the superclass because _model is available here.
    super.interpreterReady(wd);
  }
  
  /** In the event thread, notifies listeners that an interaction has started. */
  public void _notifyInteractionStarted() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionStarted(); } });
  }
  
  /** In the event thread, notifies listeners that an interaction has ended. */
  protected void _notifyInteractionEnded() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionEnded(); } });
  }
  
  /** In the event thread, notifies listeners that an error was present in the interaction. */
  protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionErrorOccurred(offset,length); } });
  }
  
  /** In the event thread, notifies listeners that the interpreter has changed.
    * @param inProgress Whether the new interpreter is currently in progress.
    */
  protected void _notifyInterpreterChanged(final boolean inProgress) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterChanged(inProgress); } });
  }
  
  /** In the event thread, notifies listeners that the interpreter is resetting. */
  protected void _notifyInterpreterResetting() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterResetting(); } });
  }
  
  /** In the event thread, notifies listeners that the interpreter is ready. Sometimes called from outside the event
    * thread. */
  public void _notifyInterpreterReady(final File wd) {  
//    System.out.println("Asynchronously notifying interpreterReady event listeners");  // DEBUG
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterReady(wd); } });
  }
  
  /** In the event thread, notifies listeners that the interpreter has exited unexpectedly.
    * @param status Status code of the dead process
    */
  protected void _notifyInterpreterExited(final int status) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterExited(status); } });
  }
  
  /** In the event thread, notifies listeners that the interpreter reset failed.
    * @param t Throwable causing the failure
    */
  protected void _notifyInterpreterResetFailed(final Throwable t) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterResetFailed(t); } });
  }
  
  /** In the event thread, notifies the view that the current interaction is incomplete. */
  protected void _notifyInteractionIncomplete() {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionIncomplete(); } });
  }
  
  public ConsoleDocument getConsoleDocument() { return _model.getConsoleDocument(); }
  
//  /** overides method in InteractionModel.java and changes stackTrace for a
//    * throwable if LL files are present
//    * @param stackTrace the stack trace to change files name and line number in
//    * @return stack trace with replaced file name and line number (if throwable occured in a .dj* file)
//    */
//  public StackTraceElement[] replaceLLException(StackTraceElement[] stackTrace) {
//    // use LLSTM from compiler model.
//    LanguageLevelStackTraceMapper LLSTM = _model.getCompilerModel().getLLSTM();
//    final List<File> files = new ArrayList<File>();
//    for(OpenDefinitionsDocument odd: _model.getLLOpenDefinitionsDocuments()) { files.add(odd.getRawFile()); }
//    
//   return (LLSTM.replaceStackTrace(stackTrace,files));
//  }  
  
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
}
