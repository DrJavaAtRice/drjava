/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu).  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the 
 * following conditions are met:
 *    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 *      disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *      following disclaimer in the documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the names of its contributors may 
 *      be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import edu.rice.cs.drjava.DrScala;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.drjava.model.repl.newjvm.InterpreterException;
import edu.rice.cs.drjava.ui.InteractionsPane;
import edu.rice.cs.drjava.ui.avail.DefaultGUIAvailabilityNotifier;
import edu.rice.cs.drjava.ui.avail.GUIAvailabilityListener;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocumentInterface;

import java.io.File;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.text.BadLocationException;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.drjava.config.OptionConstants.*;

/** A Swing specific InteractionsModel which can serve as the glue between a local InteractionsDocument and a remote 
  * JavaInterpreter in another JVM.
  * @version $Id: RMIInteractionsModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class RMIInteractionsModel extends InteractionsModel {
  
  /* static final Log _log inherited from InteractionsModel */
  
  /* instance fields inherited from Interactions Model:
   *   protected final InteractionsEventNotifier _notifier = new InteractionsEventNotifier();
   *   protected volatile InteractionsDocument _document;
   *   protected volatile File _workingDir;
   *   protected volatile boolean _restartInProgress;
   *   protected volatile InputListener _inputListener;
   *   protected final ConsoleDocumentInterface _cDoc;
   *   protected volatile String _lastError = null;
   *   protected volatile String _secondToLastError = null;
   */

  private static final int WAIT_TIMEOUT = 30000; // time to wait for the slave JVM to restart in milliseconds
 
  /** RMI interface to the remote Java interpreter.*/
  protected final MainJVM _jvm;
  
//  /* A completion monitor that is satisfied by default. The raise() operation changes it to false. */ 
//  protected final CompletionMonitor _newSlaveMonitor = new CompletionMonitor(true);
  
  /** The interactions pane attached to this document.  In contrast to a standard MVC decomposition, where the model
    * and the view are independent components, an interactions model inherently includes a prompt and a cursor marking
    * where the next input expression (in progress) begins and where the cursor is within that expression.  In Swing, the
    * view contains the cursor.  Our InteractionsDocument (a form of ConsoleDocument) contains the prompt.  Public only for
    * testing purposes; otherwise protected.
    * 
    * TODO: The Eclipse plug-in doesn't use Swing, and so has no InteractionsPane.  In that case, _pane is always null.
    * This should be redesigned to eliminate the strong coupling with Swing.
    */
  public volatile InteractionsPane _pane;  // initially null
    
  /** Banner displayed at top of the interactions document */
  private volatile String _banner;
     
  /** GUI component availability notifier.  An identical copy is heavily used in MainFrame. */
  private final DefaultGUIAvailabilityNotifier _guiNotifier = DefaultGUIAvailabilityNotifier.ONLY;
  
  /** Constructs an InteractionsModel which can communicate with another JVM.
    * @param jvm RMI interface to the slave JVM
    * @param cDoc document to use in the InteractionsDocument
    * @param historySize Number of lines to store in the history
    * @param writeDelay Number of milliseconds to wait after each println
    */
  public RMIInteractionsModel(MainJVM jvm, ConsoleDocumentInterface cDoc, File wd, int historySize, int writeDelay) {
    super(cDoc, historySize, writeDelay);  // binds _document to cDoc
    _jvm = jvm;
    _jvm.setWorkingDirectory(wd); // Is wd set up in slave yet?  (No slave yet exists?)
    /* The posting of a banner at the top of InteractionsDocument must be deferred
     * until after the InteracationsPane has been set up. */
    EventQueue.invokeLater(new Runnable() {
      public void run() { _document.setBanner(generateBanner(wd));}
    });
  }
  
  /** Sets the _pane field and initializes the caret position in the pane.  Called in the InteractionsController. */
  public void setUpPane(InteractionsPane pane) { 
    _pane = pane;
    _pane.setCaretPosition(_document.getLength());
  }
  
  /** Gets the _pane field */
  public InteractionsPane getPane() { return _pane; }
  
  protected void scrollToCaret() {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        final InteractionsPane pane = _pane; 
        if (pane == null) return;  // Can be called in tests when component has not been realized
        int pos = pane.getCaretPosition();
        try { pane.scrollRectToVisible(pane.modelToView(pos)); }
        catch(BadLocationException e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  public String getStartUpBanner() { return generateBanner(_workingDir); }
  
  /* The method repackages generates the banner corresponding to wd, binds _banner to this value, and returns it. 
   * It is package private so that RMIInteractionsModel inherits it. */
  String generateBanner(File wd) { 
    _banner = BANNER_PREFIX + "  Working directory is " + wd + '\n';
    return _banner;
  }
  
  /** Resets the displayed document */
  public void documentReset() { 
    assert EventQueue.isDispatchThread();
    _log.log("invoking documentReset()");
    _document.reset(generateBanner(_workingDir));
    _log.log("documentReset() returned");
    _document.clearColoring();
    _guiNotifier.available(GUIAvailabilityListener.ComponentType.INTERACTIONS);
  }
  
  /** Interprets the given command.
    * @param toEval command to be evaluated
    */
  protected void _interpretCommand(String toEval) {
    _log.log("_interpretCommand (in RMIInteractionsModel) " + toEval);
    _jvm.interpret(toEval);
  }
  
//  /** Gets the string representation of the value of a variable in the current interpreter.
//    * @param var the name of the variable
//    */
//  public Pair<String,String> getVariableToString(String var) {
//    System.out.println("getVariableToString: " + var);
//    Option<Pair<String,String>> result = _jvm.getVariableToString(var);
//    System.out.println("\tresult.isNone? " + result.isNone());
//    Pair<String,String> retval = result.unwrap(new Pair<String,String>("",""));
//    System.out.println("\tretval: " + retval);
//    return retval;
//  }
  
  /** Called when a new Java interpreter has registered and is ready for use. */
  public void interpreterReady() {
    _log.log("*****interpreterReady() called in InteractionsModel");
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _log.log("In RMIInteractionsMode.Runnable(at line 189).run() started");
        _document.setInteractionInProgress(false);
        /* The following is already done in restartInterpreter and setUpNewInterpreter. */
//        _log.log("resetting interactions document in RMIInteractionsModel.interpreterReady()");
        documentReset();  // resets the interactions document
        _log.log("In RMIInteractionsModel.Runnable(at line 189).run(), interactions document has been reset");
        if (_pane != null) _pane.setCaretPosition(_document.getLength());  
        performDefaultImports();
        _log.log("In RMIInteractionsModel.Runnable(at line 189).run(), calling notifyInterpreterReady()");
        _notifyInterpreterReady();
        _log.log("In RMIInteractionsModel.Runnable(at line 189).run(), completed _notifyInterpreterReady()");
      }
    });
  }

  /** Resets the interactions window with specified working directory. Also clears the console if the option is indicated 
    * (on by default).  If {@code wd} is {@code null}, the former working directory is used. Only runs in the event
    * dispatch thread.  This method does NOT wait for the operation to complete.  The caller needs to call 
    * _interactionsListener.waitResetDone() after this method returns to ensure tha the interactions pane has finished 
    * resetting.
    * <p>
    * This method is universally used to reset the interations pane except for direct calls to hardResetInteractions
    * in DefaultGlobalModel; this method may trigger a hard reset.
    */
  public void resetInterpreter(File wd) {
    assert _pane != null;
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();

    _log.log("DefaultGlobalModel.ResetInterpreter(" + wd + ") called.");    
    _notifyInterpreterResetting(); // performed asynchronously in event thread
  
    /* Determine working directory of the current slave JVM which is cached in the MainJVM object. */
    final File currentWorkDir = _jvm.getWorkingDirectory();
    
    boolean trySoftReset = _jvm.classPathUnchanged();
    _log.log("In RMIInteractionsModel, trySoftReset = " + trySoftReset);

    if ((wd.equals(currentWorkDir)) && trySoftReset) {
      _log.log("In RMIInteractionsModel, executing reset interpreter in ResetInterpreter"); 
      
      // Try to reset the interpreter internally without killing and restarting the slave JVM
      try {
        boolean success = _jvm.resetInterpreter();
        
        _log.log("_jvm.resetInterpreter() returned " + success);;
        if (success && ! _jvm.isDisposed()) {  // In some tests, the Main JVM is already disposed ?
          /* trigger the interpreterResetting event
          _notifier.interpreterResetting();
          /* inform InteractionsModel that interpreter is ready */
          _notifyInterpreterReady();  // operation runs the event thread
        }
      }
      catch(InterpreterBusyException e) {
        _log.log("resetInterpreter threw InterpreterBusy exception forcing hard reset.");
        hardResetInterpreter(wd);
      }
    }
    else {
      _log.log("simple reset interpreter failed, forcing a hard reset; wd = " + wd + 
               " currentWorkDir = " + currentWorkDir);
      hardResetInterpreter(wd);
    }
    _notifyInterpreterReady();  // _notifyInterpreterReady invokes the event thread
  }
  
  /** This method has exactly the same contract as restInterpreter except that it always replaces the slave JVM. */
  public void hardResetInterpreter(File wd) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();    
    
//    assert _pane != null;  // failed in a test; evidenty the _pane is set up asynchronously
    
    _log.log("DefaultGlobalModel.hardResetInterpreter(" + wd + ") called.");
    
    // update the setting
    DrScala.getConfig().setSetting(LAST_INTERACTIONS_DIRECTORY, wd);
    
    setUpNewInterpreter();
    _log.log("hardResetInterpreter(" + wd + ") has finished, but reset happens asynchronously");
  }
  
  /** Adds the given path to the interactions class path used in the interpreter.
    * @param f  the path to add
    */
  public void addInteractionsClassPath(File f) { _jvm.addInteractionsClassPath(f); }
  
  /** Adds the given path(s) to the interactions class path used in the interpreter.
    * @param cp the path to add
    */
  public void addInteractionsClassPath(List<File> cp) { _jvm.addInteractionsClassPath(cp); }
  
//  /** Attempts to reset the Scala interpreter in the slave JVM.  */
//  protected boolean _resetInterpreter(final File wd) {
////    _jvm.setWorkingDirectory(wd);  // already done
//    /* Try to reset the interpreter using the internal scala interpreter reset command.  If this fails restart the
//     * slave JVM. */
//    _log.log("_resetInterpreter in RMIInteractions model has been called");
//    boolean success = _jvm.resetInterpreter();  // attempt an interpreter reset
//    _log.log("_resetInterpreter returned " + success);
//    if (success) EventQueue.invokeLater(new Runnable() { public void run() { documentReset(); }});
//    // logic for calling hardReset should be here!  the contract for this method should NOT say "Attempts to"
//    return success;
//  }
  
  /** Sets the new interpreter to be the current one. */
  public void setUpNewInterpreter() {
//    _newSlaveMonitor.raise();  // raise flag indicating that slave JVM is being replaced
    _log.log("In RMIInteractionsModel, setUpNewInterpreter has been called");
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _log.log("EventQueue thunk in RMIInteractionsModel.; restarting interpreter");
        if (!_jvm.isDisposed()) {  // in some unit tests, _jvm is already disposed and setUpNewInterpreter 
          _jvm.restartInterpreterJVM();  // only place other than tests where this method is called
          _log.log("Interpreter JVM being replaced");
          _notifier.interpreterResetting();
        }
        /* when _jvm.isDisposed, there setUpNewInterpreter() becomes a no-op */
      }
    });
  }
  
  /** Updates the prompt and status of the document after an interpreter change. Must run in event thread. 
    * (TODO: is it okay that related RMI calls occur in the event thread?)
    * @param prompt New prompt to display
    */
  private void _updateDocument(String prompt) {
    assert EventQueue.isDispatchThread();
    _document.setPrompt(prompt);
    _document.insertNewline(_document.getLength());
    _document.insertPrompt();
    scrollToCaret();
  }
 
  /** Perform the default imports of the classes and packages listed in the INTERACTIOONS_AUTO_IMPORT_CLASSES. */
  public void performDefaultImports() {
    java.util.ArrayList<String> classes = DrScala.getConfig().getSetting(OptionConstants.INTERACTIONS_AUTO_IMPORT_CLASSES);
    final StringBuilder sb = new StringBuilder();
    
    for(String s: classes) {
      String name = s.trim();
      if (s.length() > 0) {
        sb.append("import ");
        sb.append(s.trim());
        sb.append("; ");
      }
    }
    if (sb.length() > 0) {
      interpretCommand(sb.toString());
      _document.insertBeforeLastPrompt("Default imports: " + sb.toString() + "\n", InteractionsDocument.DEBUGGER_STYLE);
    }
  }
  
  /** Reset the information about the last and second to last error. */
  public void resetLastErrors() {
    _lastError = _secondToLastError = null;
  }
}
