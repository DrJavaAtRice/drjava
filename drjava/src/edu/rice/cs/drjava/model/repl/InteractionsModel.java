/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.io.*;
import java.net.ServerSocket;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import edu.rice.cs.drjava.ui.DrScalaErrorHandler;
import edu.rice.cs.drjava.ui.InteractionsPane;
import edu.rice.cs.util.FileOpenSelector;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocumentInterface;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.tuple.Pair;

/** A Swing specific model for the DrScala InteractionsPane.  It glues together an InteractionsDocument, an 
  * InteractionsPane and a JavaInterpreter.  This abstract class provides common functionality for all such models.
  * The methods in this class generally can be executed only in the event thread once the model has been constructed.
  * @version $Id: InteractionsModel.java 5727 2012-09-30 03:58:32Z rcartwright $
  */
public abstract class InteractionsModel implements InteractionsModelCallback {
  
  /** methods inherited from InteractionsModelCallBack that are left abstract:
 
    * void_notifyInterpreterResetting()
    * 
    * void interpreterReady()
    * void notifyInterpreterReady()
    * 
    * List<File> getCompilerBootClassPath();
    * String transformCommands(String interactionsString)
    */
  
  /** Banner prefix. */
  public static final String BANNER_PREFIX = "Welcome to DrScala.";

  /** Number of milliseconds to wait after each println, to prevent the JVM from being flooded
    * with print calls. */
  public static final int WRITE_DELAY = 50;
  
  public static final Log _log = new Log("GlobalModel.txt", true);
 
//  public static final String _newLine = "\n"; // was StringOps.EOL; but Swing uses '\n' for newLine
  
  /** Keeps track of any listeners to the model. */
  protected final InteractionsEventNotifier _notifier = new InteractionsEventNotifier();
  
  /** InteractionsDocument containing the commands and history.  This field is volatile rather than final because its
    * initialization is deferred until after the interactions pane is created.  The InteractionsDocument constructor
    * indirectly accesses the pane generating a NullPointerException if _pane is uninitialized.
    */
  protected volatile InteractionsDocument _document;
  
  /** The working directory for the current interpreter. */
  protected volatile File _workingDir = IOUtil.WORKING_DIRECTORY;
  
  /* Flag set only while resetInterpreter is in progress. Only modified by code in the class DefaultInteractionsModel*/ 
  protected volatile boolean _restartInProgress = false;
  
  /** The input listener to listen for requests to System.in. */
  protected volatile InputListener _inputListener;
  
  /** The embedded interactions document (a SwingDocument in native DrJava) */
  protected final ConsoleDocumentInterface _cDoc;
  
  /** A lock object to prevent print calls to System.out or System.err from flooding the JVM, ensuring the UI remains
    * responsive.  Only public for testing purposes. */
  private final Object _writerLock;
  
  /** Number of milliseconds to wait after each println, to prevent the JVM from being flooded with print calls. */
  private final int _writeDelay;
  
  /** The String added to history when the interaction is complete or an error is thrown */
  private volatile String _toAddToHistory = "";
  
  /** Last error, or null if successful. */
  protected volatile String _lastError = null;
  protected volatile String _secondToLastError = null;
  
  /** Constructs part of an InteractionsModel, namely the interactions document.  Classes derived from this one
    * create the interactions pane (including printing a banner with the working directory which is unknown
    * in this class) and set up an interpreter.  This constructs the interactions document and installs a degnerate
    * input listener.
    * @param cDoc document to use in the InteractionsDocument
    * @param historySize Number of lines to store in the history
    * @param writeDelay Number of milliseconds to wait after each println
    */
  public InteractionsModel(ConsoleDocumentInterface cDoc, int historySize, int writeDelay) {
    _document = new InteractionsDocument(cDoc, historySize);
    _cDoc = cDoc;
    _writeDelay = writeDelay;
    _writerLock = new Object(); 
    _inputListener = NoInputListener.ONLY;
  }
  
  /** Sets _pane and initializes the caret position in the pane.  Called in the InteractionsController. */
  public abstract void setUpPane(InteractionsPane pane);
     
  /** Gets _pane for this interactions model */
  public abstract InteractionsPane getPane();

  protected abstract void scrollToCaret();
  
  /** Adds an InteractionsListener to the model.
    * @param listener a listener that reacts to Interactions events. 
    */
  public void addListener(InteractionsListener listener) { _notifier.addListener(listener); }
  
  /** Remove an InteractionsListener from the model.  If the listener is not currently listening to this model, this 
    * method has no effect.
    * @param listener a listener that reacts to Interactions events
    */
  public void removeListener(InteractionsListener listener) { _notifier.removeListener(listener); }
  
  /** Removes all InteractionsListeners from this model. */
  public void removeAllInteractionsTestListeners() { _notifier.removeAllListeners(); }
  
  /** Returns the InteractionsDocument stored by this model. */
  public InteractionsDocument getDocument() { return _document; }
  
  public void interactionContinues() {
    _document.setInteractionInProgress(false);
    _notifyInteractionEnded();
    _notifyInteractionIncomplete();
  }
 
  /** Interprets the current given text at the prompt in the interactions doc. May run outside the event thread. */
  public void interpretCurrentInteraction() {

    Utilities.invokeLater(new Runnable() {
      public void run() {
        
        if (_document.interactionInProgress() ) return;  // Don't start a new interaction while one is in progress
        
        String text = _document.getCurrentInteraction();
        String toEval = text.trim();
        _prepareToInterpret(toEval);  // Writes a newLine!
//        if (toEval.startsWith("java ")) toEval = _transformJavaCommand(toEval);
//        else if (toEval.startsWith("applet ")) toEval = _transformAppletCommand(toEval);
        toEval = transformCommands(toEval);
        
        final String evalText = toEval;

        new Thread(new Runnable() { 
          public void run() { 
            try { interpretCommand(evalText); } 
            catch(Throwable t) { DrScalaErrorHandler.record(t); }
          } 
        }).start(); 
      }
    });
  }
  
  /** Performs pre-interpretation preparation of the interactions document and notifies the view.  Must run in the
    * event thread for newline to be inserted at proper time.  Assumes that Write Lock is already held. */
  private void _prepareToInterpret(String text) {
    _addNewline();
    _notifyInteractionStarted();
    _document.setInteractionInProgress(true);
    _toAddToHistory = text; // _document.addToHistory(text);
    //Do not add to history immediately in case the user is not finished typing when they press return
  }
  
  /** Appends a newLine to _document assuming that the Write Lock is already held.  Must run in the event thread. */
  public void _addNewline() { append(StringOps.NEWLINE, InteractionsDocument.DEFAULT_STYLE); }
  
  /** Interprets the given command.
    * @param toEval command to be evaluated. */
  public final void interpretCommand(String toEval) { _interpretCommand(toEval); }
  
  /** Interprets the given command.  This should only be called from interpret, never directly.
    * @param toEval command to be evaluated
    */
  protected abstract void _interpretCommand(String toEval);
  
  /** Notifies the view that the current interaction is incomplete. */
  protected abstract void _notifyInteractionIncomplete();
  
  /** Notifies listeners that an interaction has started. (Subclasses must maintain listeners.) */
  public abstract void _notifyInteractionStarted();
  
//  /** Gets the string representation of the value of a variable in the current interpreter.
//    * @param var the name of the variable
//    * @return A string representation of the value, or {@code null} if the variable is not defined.
//    */
//  public abstract Pair<String,String> getVariableToString(String var);
  
  public abstract void documentReset();
  
  /** Resets the Java interpreter with working directory wd. All interpeter resets invoke this method! */
  public abstract void resetInterpreter(File wd);
  
  /** Returns the working directory for the current interpreter. */
  public File getWorkingDirectory() { return _workingDir; }
  
  /** Resets the interpreter by replacing the slave JVM. */
  public abstract void hardResetInterpreter(File wd);

  /** These add the given path to the project files classpaths used in the interpreter.
    * @param f  the path to add
    */
  public abstract void addInteractionsClassPath(File f);
  
  /** Handles a syntax error being returned from an interaction
    * @param offset the first character of the error in the InteractionsDocument
    * @param length the length of the error.
    */
  protected abstract void _notifySyntaxErrorOccurred(int offset, int length);
 
  /** Interprets the files selected in the FileOpenSelector. Assumes all strings have no trailing whitespace.
    * Interprets the array all at once so if there are any errors, none of the statements after the first 
    * erroneous one are processed.  Only runs in the event thread.
    */
  public void loadHistory(final FileOpenSelector selector) throws IOException {
    ArrayList<String> histories;
    try { histories = _getHistoryText(selector); }
    catch (OperationCanceledException oce) { return; }
    final ArrayList<String> _histories = histories;
    
    _document.clearCurrentInteraction();
    
    _log.log("In InteractionsModel.loadHistory(...), interactions document cleared");
    
    // Insert into the document and interpret
    final StringBuilder buf = new StringBuilder();
    for (String hist: _histories) {
      ArrayList<String> interactions = _removeSeparators(hist);
      _log.log("interactions array = " + interactions);
      for (String curr: interactions) {
        int len = curr.length();
        buf.append(curr);
        if (len > 0 && curr.charAt(len - 1) != ';')  buf.append(';');
        buf.append(StringOps.EOL);
      }
    }
    String text = buf.toString().trim();
    _log.log("Loaded history is: '" + text + "'");
    append(text, InteractionsDocument.DEFAULT_STYLE);
    _log.log("Interpreting current interaction");
    interpretCurrentInteraction();     
  }
  
   /** Opens the files chosen in the given file selector, and returns an ArrayList with one history string 
    * for each selected file.
    * @param selector A file selector supporting multiple file selection
    * @return a list of histories (one for each selected file)
    */
  protected static ArrayList<String> _getHistoryText(FileOpenSelector selector)
    throws IOException, OperationCanceledException {
    File[] files = selector.getFiles();
    if (files == null) throw new IOException("No Files returned from FileSelector");
    
    ArrayList<String> histories = new ArrayList<String>();
    ArrayList<String> strings = new ArrayList<String>();
    
    for (File f: files) {
      if (f == null) throw new IOException("File name returned from FileSelector is null");
      try {
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        while (true) {
          String line = br.readLine();
          if (line == null) break;
          strings.add(line);
        }
        br.close(); // win32 needs readers closed explicitly!
      }
      catch (IOException ioe) { throw new IOException("File name returned from FileSelector is null"); }
      
      // Create a single string with all formatted lines from this history
      final StringBuilder text = new StringBuilder();
      boolean firstLine = true;
      int formatVersion = 1;
      for (String s: strings) {
        int sl = s.length();
        if (sl > 0) {
          
          // check for format version string. NOTE: the original file format did not have a version string
          if (firstLine && (s.trim().equals(History.HISTORY_FORMAT_VERSION_2.trim()))) formatVersion = 2;
          
          switch (formatVersion) {
            case (1):
              // When reading this format, we need to make sure each line ends in a semicolon.
              // This behavior can be buggy; that's why the format was changed.
              text.append(s);
              if (s.charAt(sl - 1) != ';') text.append(';');
              text.append(StringOps.EOL);
              break;
            case (2):
              if (!firstLine) text.append(s).append(StringOps.EOL); // omit version string from output
              break;
          }
          firstLine = false;
        }
      }
      
      // Add the entire formatted text to the list of histories
      histories.add(text.toString());
    }
    return histories;
  }
  
  /* Loads the contents of the specified file(s) into the histories buffer. This method is dynamic because it refers
   * to this. */
  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException {
    ArrayList<String> histories = _getHistoryText(selector);
    ArrayList<String> interactions = new ArrayList<String>();
    for (String hist: histories) interactions.addAll(_removeSeparators(hist));
    return new InteractionsScriptModel(this, interactions);
  }
  
  /** Removes the interaction-separator comments from a history, so that they will not appear when executing
    * the history.
    * @param text The full, formatted text of an interactions history (obtained from _getHistoryText)
    * @return A list of strings representing each interaction in the history. If no separators are present, 
    * the entire history is treated as one interaction.
    */
  protected static ArrayList<String> _removeSeparators(String text) {
    String sep = History.INTERACTION_SEPARATOR;
    int len = sep.length();
    ArrayList<String> interactions = new ArrayList<String>();
    
    // Loop while there are still separators, adding the text between separators
    //  as separate elements to the interactions list
    int index = text.indexOf(sep);
    int lastIndex = 0;
    while (index != -1) {
      interactions.add(text.substring(lastIndex, index).trim());
      lastIndex = index + len;
      index = text.indexOf(sep, lastIndex);
    }
    
    // get last interaction
    String last = text.substring(lastIndex, text.length()).trim();
    if (!"".equals(last)) interactions.add(last);
    return interactions;
  }
    
  private static final int DELAY_INTERVAL = 10;
  private volatile int delayCount = DELAY_INTERVAL;
  
  /** Called when the repl prints to System.out.  Includes a delay to prevent flooding the interactions document.  This
    * method can safely be called from outside the event thread.
    * @param s String to print
    */
  public void replSystemOutPrint(final String s) {
    Utilities.invokeLater(new Runnable() {
      public void run() { _document.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_OUT_STYLE); }
    });
    if (delayCount == 0) {
      scrollToCaret();
//      System.err.println(s + " printed; caretPostion = " + _pane.getCaretPosition());
      _writerDelay();
      delayCount = DELAY_INTERVAL;
    }
    else delayCount--;   
  }
  
  /** Called when the repl prints to System.err.  Includes a delay to prevent flooding the interactions document.  This
    * method can safely be called from outside the event thread.
    * @param s String to print 
    */
  public void replSystemErrPrint(final String s) {
      Utilities.invokeLater(new Runnable() {
        public void run() { _document.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_ERR_STYLE); } 
      });
      if (delayCount == 0) {
        scrollToCaret();
//      System.err.println(s + " printed; caretPostion = " + _pane.getCaretPosition());
        _writerDelay();
        delayCount = DELAY_INTERVAL;
      }
      else delayCount--;
  }
  
  /** Returns a line of text entered by the user at the equivalent of System.in.  Only executes in the event thread. */
  public String getConsoleInput() { return _inputListener.getConsoleInput(); }
  
  /** Sets the listener for any type of single-source input event. The listener can only be changed with the 
    * changeInputListener method.except in testing code which needs to install a different listener after the
    * interactiona controller has been created (method testConsoleInput in GlobalModelIOTest).
    * @param listener a listener that reacts to input requests
    * @throws IllegalStateException if the input listener is locked
    */
  public void setInputListener(InputListener listener) {
    if (_inputListener == NoInputListener.ONLY || Utilities.TEST_MODE) { _inputListener = listener; }
    else throw new IllegalStateException("Cannot change the input listener until it is released.");
  }
  
  /** Changes the input listener. Takes in the old listener to ensure that the owner
    * of the original listener is aware that it is being changed. It is therefore
    * important NOT to include a public accessor to the input listener on the model.
    * Only used in a single thread in unit tests, so synchronization is unnecessary.
    * @param oldListener the listener that was installed
    * @param newListener the listener to be installed
    */
  public void changeInputListener(InputListener oldListener, InputListener newListener) {
    if (_inputListener == oldListener) _inputListener = newListener;
    else
      throw new IllegalArgumentException("The given old listener is not installed!");      
  }
  
  /** Performs the common behavior when an interaction ends. Subclasses might want to additionally notify listeners 
    * here. (Do this after calling super()).  Access is public for testing purposes.
    */
  public void _interactionIsOver() {
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _document.addToHistory(_toAddToHistory);
        _document.setInteractionInProgress(false);
        _log.log("_interactionIsOver method inserting prompt in interactions pane");
        _document.insertPrompt();
        _notifyInteractionEnded();
      }
    });
    scrollToCaret();
  }

  public void _intermediateInteractionIsOver() {
    _log.log("InteractionsModel._intermediateInteractionsIsOver() called");
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _document.addToHistory(_toAddToHistory);
        _document.setInteractionInProgress(false);
        _document.insertContinuationString();
        _notifyInteractionEnded();
      }
    });
    scrollToCaret();
  }

  /** Notifies listeners that an interaction has ended. (Subclasses must maintain listeners.) */
  protected abstract void _notifyInteractionEnded();
  
  /** Appends a string to the given document using a named style.
    * @param s  String to append to the end of the document
    * @param styleName  Name of the style to use for s
    */
  public void append(final String s, final String styleName) {
    Utilities.invokeLater(new Runnable() { public void run() { _document.append(s, styleName); } });
    scrollToCaret();
  }
  
  /** Waits for a small amount of time on a shared writer lock. */
  public void _writerDelay() {
    synchronized(_writerLock) {
      try {
        // Wait to prevent being flooded with println's
        _writerLock.wait(_writeDelay);
      }
      catch (EditDocumentException e) { throw new UnexpectedException(e); }
      catch (InterruptedException e) { /* Not a problem. continue */}
    }
  }
  
  /** Signifies that the most recent interpretation completed successfully, returning no value. */
  public void replReturnedVoid() {
    _secondToLastError = _lastError;
    _lastError = null;
    _interactionIsOver();
  }
  
  /** Appends the returned result to the interactions document, inserts a prompt in the interactions document, and 
    * advances the caret in the interactions pane.
    * @param result The String returned by the interpretation which in Scala begins "<varname> :".  We always use
    *        String representations because they are serializable (and easy to debug).
    *        String form because returning the Object directly would require the data type to be serializable.
    */
  public void replReturnedResult(String result, String style) {
    _log.log("InteractionsModel.replReturned(...) given '" + result + "'");
    _secondToLastError = _lastError;
    _lastError = null;
    if (result.equals("     | ")) {  // I don't understand the encoding here; a funny terminator -- Corky
      append("", style);
      _intermediateInteractionIsOver();
    }
    else {
      append(result + "", style);
      _interactionIsOver();
    }
  }
  
  /* Due to delays and blocking by running code in the event handling thread, does not have any effect. */
  
//  /** Displays the fact that repl is being reset requiring a restart of the Scala interpreter. The Scala interpreter
//    * cannot be reset because it is busy. The code that performs this restart is located in MainJVM. ONLY runs in the
//    * Event Handling Thread! 
//    */
//  public void replIsResetting() {
//    _document.insertBeforeLastPrompt(" Restarting the Scala interpreter, which is a SLOW process ...\n", 
//                                     InteractionsDocument.ERROR_STYLE);
//  }

  /** Signifies that the most recent interpretation was ended due to an exception being thrown. */
  public void replThrewException(String message, StackTraceElement[] stackTrace) {
//    stackTrace = replaceLLException(stackTrace);
    StringBuilder sb = new StringBuilder(message);
    for(StackTraceElement ste: stackTrace) {
      sb.append("\n\tat ");
      sb.append(ste);
    }
    replThrewException(sb.toString().trim());
  }
  
  /** Signifies that the most recent interpretation was ended due to an exception being thrown. */
  public void replThrewException(final String message) {
    if (message.endsWith("<EOF>\"")) {
      interactionContinues();
    }
    else {
      Utilities.invokeLater(new Runnable() {
        public void run() { _document.appendExceptionResult(message, InteractionsDocument.ERROR_STYLE); }
      });
      _secondToLastError = _lastError;
      _lastError = message;
      _interactionIsOver();
    }
  }
  
//  /** Signifies that the most recent interpretation was preempted by a syntax error.  The integer parameters
//    * support future error highlighting.
//    * @param errorMessage The syntax error message
//    * @param startRow The starting row of the error
//    * @param startCol The starting column of the error
//    * @param endRow The end row of the error
//    * param endCol The end column of the error
//    */
//  public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol,
//                                      int endRow, int endCol ) {
//    // Note: this method is currently never called.  The highlighting functionality needs 
//    // to be restored.
//    _secondToLastError = _lastError;
//    _lastError = errorMessage;
//    if (errorMessage != null) {
//      if (errorMessage.endsWith("<EOF>\"")) {
//        interactionContinues();
//        return;
//      }
//    }
//    
//    Pair<Integer,Integer> oAndL =
//      StringOps.getOffsetAndLength(interaction, startRow, startCol, endRow, endCol);
//    
//    _notifySyntaxErrorOccurred(_document.getPromptPos() + oAndL.first().intValue(),oAndL.second().intValue());
//    
//    _document.appendSyntaxErrorResult(errorMessage, interaction, startRow, startCol, endRow, endCol,
//                                      InteractionsDocument.ERROR_STYLE);
//    
//    _interactionIsOver();
//  }
  
  /** Signifies that the most recent interpretation contained a call to System.exit.
    * @param status The exit status that will be returned.
    */
  public void replCalledSystemExit(int status) {
//    Utilities.showDebug("InteractionsModel: replCalledSystemExit(" + status + ") called");
    _log.log("InteractionsModel.replCalledSystemExit(" + status + ") called");
    _notifyInterpreterExited(status); 
  }
  
  /** Notifies listeners that the interpreter has exited unexpectedly. (Subclasses must maintain listeners.)
    * @param status Status code of the dead process
    */
  protected abstract void _notifyInterpreterExited(int status);
  
  /** Called when the interpreter starts to reset. */
  public void interpreterResetting() {
    _log.log("InteractionsModel.interpreterResetting called");
    Utilities.invokeLater(new Runnable() {
      public void run() {
        _document.insertBeforeLastPrompt(" Resetting interactions by killing and restarting the interactions JVM.  Be patient ...\n", InteractionsDocument.ERROR_STYLE);
        _document.setInteractionInProgress(true);
      }
    });
  }
  
  /** This method is called by the Main JVM if the Interpreter JVM cannot be exited
    * @param t The Throwable thrown by System.exit
    */
  public void interpreterResetFailed(Throwable t) {
    _interpreterResetFailed(t);
    _document.setInteractionInProgress(false);
    _notifyInterpreterResetFailed(t);
  }
  
  public void interpreterWontStart(Exception e) {
    _interpreterWontStart(e);
    _document.setInteractionInProgress(true); // prevent editing -- is there a better way to do this?
  }
  
  /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
    * @param t The Throwable thrown by System.exit
    */
  protected abstract void _interpreterResetFailed(Throwable t);
  
  /** Notifies listeners that the interpreter reset failed. (Subclasses must maintain listeners.)
    * @param t Throwable explaining why the reset failed.
    */
  protected abstract void _notifyInterpreterResetFailed(Throwable t);
  
  /** Action to perform when the interpreter won't start. */
  protected abstract void _interpreterWontStart(Exception e);

  public abstract String getStartUpBanner();
  
  /** Singleton InputListener which should never be asked for input. */
  private static class NoInputListener implements InputListener {
    public static final NoInputListener ONLY = new NoInputListener();
    private NoInputListener() { }
    
    public String getConsoleInput() { throw new IllegalStateException("No input listener installed!"); }
  }
  
  /** Gets the console tab document for this interactions model */
  public abstract ConsoleDocument getConsoleDocument();
  
  /** Return the last error, or null if successful. */
  public String getLastError() {
    return _lastError;
  }
  
  /** Return the second to last error, or null if successful. */
  public String getSecondToLastError() {
    return _secondToLastError;
  }
  
  abstract public void resetLastErrors();
  
  /** Returns the last history item and then removes it, or returns null if the history is empty. */
  public String removeLastFromHistory() {
    return _document.removeLastFromHistory();
  }
}
