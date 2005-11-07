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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.text.*;
import edu.rice.cs.util.swing.Utilities;

/** Interactions model which can notify GlobalModelListeners on events.
 *  @version $Id$
 */
public class DefaultInteractionsModel extends RMIInteractionsModel {
  /** Message to signal that input is required from the console. */
//  public static final String INPUT_REQUIRED_MESSAGE =
//    "Please enter input in the Console tab." + _newLine;

  /** Model that contains the interpreter to use. (Can this be eliminated?) */
  protected final DefaultGlobalModel _model;

  /** Creates a new InteractionsModel.
   *  @param model DefaultGlobalModel to do the interpretation
   *  @param control RMI interface to the Interpreter JVM
   *  @param adapter InteractionsDJDocument to use for the document
   */
  public DefaultInteractionsModel(DefaultGlobalModel model, MainJVM control, EditDocumentInterface adapter) {
    super(control, adapter,
          DrJava.getConfig().getSetting(OptionConstants.HISTORY_MAX_SIZE).intValue(),
          DefaultGlobalModel.WRITE_DELAY);
    _model = model;
    // Set whether to allow "assert" statements to be run in the remote JVM.
    Boolean allow = DrJava.getConfig().getSetting(OptionConstants.JAVAC_ALLOW_ASSERT);
    _interpreterControl.setAllowAssertions(allow.booleanValue());
    
    // Add option listeners
    DrJava.getConfig().addOptionListener(OptionConstants.HISTORY_MAX_SIZE,
                                         _document.getHistoryOptionListener());
    DrJava.getConfig().addOptionListener(OptionConstants.JAVAC_ALLOW_ASSERT,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _interpreterControl.setAllowAssertions(oce.value.booleanValue());
      }
    });
  }


  /** Called when the repl prints to System.out.
   *  @param s String to print
   */
  public void replSystemOutPrint(String s) {
    super.replSystemOutPrint(s);
    _model.systemOutPrint(s);
  }

  /** Called when the repl prints to System.err.
   *  @param s String to print
   */
  public void replSystemErrPrint(String s) {
    super.replSystemErrPrint(s);
    _model.systemErrPrint(s);
  }

  /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
   *  @param t The Throwable thrown by System.exit
   */
  protected void _interpreterResetFailed(Throwable t) {
    _document.insertBeforeLastPrompt("Reset Failed! See the console tab for details." + _newLine,
                                     InteractionsDocument.ERROR_STYLE);
    // Print the exception to the console
    _model.systemErrPrint(StringOps.getStackTrace(t));
  }

  /** Called when the Java interpreter is ready to use. Adds any open documents to the classpath. */
  public void interpreterReady() {
    _model.resetInteractionsClasspath();
    super.interpreterReady();
  }

  /** Notifies listeners that an interaction has started. */
  protected void _notifyInteractionStarted() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionStarted(); } });
  }

  /** Notifies listeners that an interaction has ended. */
  protected void _notifyInteractionEnded() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionEnded(); } });
  }

  /** Notifies listeners that an error was present in the interaction. */
  protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionErrorOccurred(offset,length); } });
  }

  /** Notifies listeners that the interpreter has changed.
   *  @param inProgress Whether the new interpreter is currently in progress.
   */
  protected void _notifyInterpreterChanged(final boolean inProgress) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterChanged(inProgress); } });
  }

  /** Notifies listeners that the interpreter is resetting. */
  protected void _notifyInterpreterResetting() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterResetting(); } });
  }

  /** Notifies listeners that the interpreter is ready. */
  protected void _notifyInterpreterReady() { 
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterReady(); } });
  }

  /** Notifies listeners that the interpreter has exited unexpectedly.
   *  @param status Status code of the dead process
   */
  protected void _notifyInterpreterExited(final int status) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterExited(status); } });
  }

  /** Notifies listeners that the interpreter reset failed.
   *  @param t Throwable causing the failure
   */
  protected void _notifyInterpreterResetFailed(final Throwable t) {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interpreterResetFailed(t); } });
  }

  /** Notifies the view that the current interaction is incomplete. */
  protected void _notifyInteractionIncomplete() {
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.interactionIncomplete(); } });
  }
  
  public ConsoleDocument getConsoleDocument() { return _model.getConsoleDocument(); }
}
