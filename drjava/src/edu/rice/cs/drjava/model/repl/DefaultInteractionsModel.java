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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.EventNotifier;
import edu.rice.cs.drjava.model.GlobalModelListener;
import edu.rice.cs.drjava.model.repl.History.HistorySizeOptionListener;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.text.*;

/**
 * Interactions model which can notify GlobalModelListeners on events.
 * @version $Id$
 */
public class DefaultInteractionsModel extends RMIInteractionsModel {
  /**
   * Message to signal that input is required from the console.
   */
  public static final String INPUT_REQUIRED_MESSAGE =
    "Please enter input in the Console tab." + _newLine;
  
  /**
   * Model that contains the interpreter to use.
   * (If possible, we'd like to eliminate the need for this field...)
   */
  protected final DefaultGlobalModel _model;
  
  
  /**
   * Keeps track of any listeners to the model.
   */
  protected final EventNotifier _notifier;
    
  
  /**
   * Creates a new InteractionsModel.
   * @param model DefaultGlobalModel to do the interpretation
   * @param control RMI interface to the Interpreter JVM
   * @param adapter SwingDocumentAdapter to use for the document
   */
  public DefaultInteractionsModel(DefaultGlobalModel model,
                                  MainJVM control,
                                  SwingDocumentAdapter adapter)
  {
    super(control, 
          adapter,
          DrJava.getConfig().getSetting(OptionConstants.HISTORY_MAX_SIZE).intValue(),
          DefaultGlobalModel.WRITE_DELAY);
    _model = model;
    _notifier = model.getNotifier();
    
    // Set whether to allow "assert" statements to be run in the remote JVM.
    Boolean allow = 
      DrJava.getConfig().getSetting(OptionConstants.JAVAC_ALLOW_ASSERT);
    _interpreterControl.setAllowAssertions(allow.booleanValue());
    
    // Add option listeners
    DrJava.getConfig().addOptionListener(OptionConstants.HISTORY_MAX_SIZE,
                                         _document.getHistory().
                                           new HistorySizeOptionListener());
    DrJava.getConfig().addOptionListener(OptionConstants.JAVAC_ALLOW_ASSERT,
                                         new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _interpreterControl.setAllowAssertions(oce.value.booleanValue());
      }
    });
  }
  
    
  /** 
   * Called when the repl prints to System.out.
   * @param s String to print
   */
  public void replSystemOutPrint(String s) {
    super.replSystemOutPrint(s);
    // TO DO: How can we print to the console without having a model field?
    _model.systemOutPrint(s);
  }

  /** 
   * Called when the repl prints to System.err.
   * @param s String to print
   */
  public void replSystemErrPrint(String s) {
    super.replSystemErrPrint(s);
    // TO DO: How can we print to the console without having a model field?
    _model.systemErrPrint(s);
  }
  
  /**
   * Any extra action to perform (beyond notifying listeners) when
   * the interpreter fails to reset.
   * @param t The Throwable thrown by System.exit
   */
  protected void _interpreterResetFailed(Throwable t) {
    _document.insertBeforeLastPrompt("Reset Failed! See the console tab for details." + _newLine,
                                     InteractionsDocument.ERROR_STYLE);
    // Print the exception to the console
    _model.systemErrPrint(StringOps.getStackTrace(t));
  }
  
  /**
   * Called when input is requested from System.in.
   * @return the input
   */
  public String getConsoleInput() {
    if (_document.inProgress()) {
      _docAppend(INPUT_REQUIRED_MESSAGE, InteractionsDocument.DEBUGGER_STYLE);
    }
    else {
      _document.insertBeforeLastPrompt(INPUT_REQUIRED_MESSAGE,
                                       InteractionsDocument.DEBUGGER_STYLE);
    }
    return _model.getConsoleInput();
  }

  /**
   * Called when the Java interpreter is ready to use.
   * Adds any open documents to the classpath.
   */
  public void interpreterReady() {
    // TO DO: How can we reset the classpath without having a model field?
    _model.resetInteractionsClasspath();
    super.interpreterReady();
  }
  
  /**
   * Notifies listeners that an interaction has started.
   */
  protected void _notifyInteractionStarted() {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interactionStarted();
      }
    });
  }
  
  /**
   * Notifies listeners that an interaction has ended.
   */
  protected void _notifyInteractionEnded() {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interactionEnded();
      }
    });
  }

  /**
   * Notifies listeners that an error was present in the interaction.
   */
  protected void _notifySyntaxErrorOccurred(final int offset, final int length) {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interactionErrorOccurred(offset,length);
      }
    });
  }
  /**
   * Notifies listeners that the interpreter has changed.
   * @param inProgress Whether the new interpreter is currently in progress.
   */
  protected void _notifyInterpreterChanged(final boolean inProgress) {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interpreterChanged(inProgress);
      }
    });
  }
  
  /**
   * Notifies listeners that the interpreter is resetting.
   */
  protected void _notifyInterpreterResetting() {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interpreterResetting();
      }
    });
  }
  
  /**
   * Notifies listeners that the interpreter is ready.
   */
  protected void _notifyInterpreterReady() {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interpreterReady();
      }
    });
  }
  
  /**
   * Notifies listeners that the interpreter has exited unexpectedly.
   * @param status Status code of the dead process
   */
  protected void _notifyInterpreterExited(final int status) {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interpreterExited(status);
      }
    });
  }
  
  /**
   * Notifies listeners that the interpreter reset failed.
   * @param t Throwable causing the failure
   */
  protected void _notifyInterpreterResetFailed(final Throwable t) {
    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.interpreterResetFailed(t);
      }
    });    
  }
}
