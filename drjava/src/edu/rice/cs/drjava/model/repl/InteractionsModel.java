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

import java.io.*;
import java.net.ServerSocket;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.model.FileOpenSelector;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;

/** A model which can serve as the glue between an InteractionsDocument and any JavaInterpreter.  This 
 *  abstract class provides common functionality for all such models.
 *  @version $Id$
 */
public abstract class InteractionsModel implements InteractionsModelCallback {

  /** Keeps track of any listeners to the model. */
  protected final InteractionsEventNotifier _notifier = new InteractionsEventNotifier();

  /** System-dependent newline string. */
  protected static final String _newLine = System.getProperty("line.separator");

  /** InteractionsDocument containing the commands and history. */
  protected final InteractionsDocument _document;

  /** Whether we are waiting for the interpreter to register for the first time. */
  protected boolean _waitingForFirstInterpreter;

  /** Whether the interpreter has been used since its last reset. */
  protected boolean _interpreterUsed;

  /** A lock object to prevent multiple threads from interpreting at once. */
  private final Object _interpreterLock;

  /** A lock object to prevent print calls to System.out or System.err from flooding 
   *  the JVM, ensuring the UI remains responsive. */
  private final Object _writerLock;

  /** Number of milliseconds to wait after each println, to prevent
   *  the JVM from being flooded with print calls. */
  private int _writeDelay;

  /** Port used by the debugger to connect to the Interactions JVM. Uniquely created in getDebugPort(). */
  private int _debugPort;

  /** Whether the debug port has been set already or not.
   *  If not, calling getDebugPort will generate an available port. */
  private boolean _debugPortSet;
  
  /** The String added to history when the interaction is complete or an error is thrown */
  private String _toAddToHistory = "";

  /** The input listener to listen for requests to System.in. */
  protected InputListener _inputListener;

  protected DocumentAdapter _adapter;
  
  /** Constructs an InteractionsModel.
   *  @param adapter DocumentAdapter to use in the InteractionsDocument
   *  @param historySize Number of lines to store in the history
   *  @param writeDelay Number of milliseconds to wait after each println
   */
  public InteractionsModel(DocumentAdapter adapter, int historySize, int writeDelay) {
    _writeDelay = writeDelay;
    _document = new InteractionsDocument(adapter, historySize);
    _adapter = adapter;
    _waitingForFirstInterpreter = true;
    _interpreterUsed = false;
    _interpreterLock = new Object();
    _writerLock = new Object();
    _debugPort = -1;
    _debugPortSet = false;
    _inputListener = NoInputListener.ONLY;
  }

  /** Add a JavadocListener to the model.
   * @param listener a listener that reacts to Interactions events */
  public void addListener(InteractionsListener listener) { _notifier.addListener(listener); }

  /** Remove an InteractionsListener from the model.  If the listener is not
   *  currently listening to this model, this method has no effect.
   *  @param listener a listener that reacts to Interactions events
   */
  public void removeListener(InteractionsListener listener) {
    _notifier.removeListener(listener);
  }

  /** Removes all InteractionsListeners from this model. */
  public void removeAllInteractionListeners() { _notifier.removeAllListeners(); }

  /** Returns the InteractionsDocument stored by this model. */
  public InteractionsDocument getDocument() { return _document; }

  public void interactionContinues() {
    _document.setInProgress(false);
    _notifyInteractionEnded();
    _notifyInteractionIncomplete();
  }
  
  /** Sets this model's notion of whether it is waiting for the first
   *  interpreter to connect.  The interactionsReady event is not fired
   *  for the first interpreter.
   */
  public void setWaitingForFirstInterpreter(boolean waiting) {
    _waitingForFirstInterpreter = waiting;
  }

  /** Interprets the current given text at the prompt in the interactions doc. */
  public void interpretCurrentInteraction() {
    synchronized(_interpreterLock) {
      // Don't start a new interaction while one is in progress
      if (_document.inProgress()) return;

      String text = _document.getCurrentInteraction();
      String toEval = text.trim();
      if (toEval.startsWith("java ")) toEval = _testClassCall(toEval);

      _prepareToInterpret(text);
      interpret(toEval);
    }
  }

  /** Performs pre-interpretation preparation of the interactions document and
   *  notifies the view. */
  private void _prepareToInterpret(String text) {
    addNewLine();
    _notifyInteractionStarted();
    _document.setInProgress(true);
    _toAddToHistory = text; // _document.addToHistory(text);
    //Do not add to history immediately in case the user is not finished typing when they press return
  }
  
  public void addNewLine() { _docAppend(_newLine, InteractionsDocument.DEFAULT_STYLE); }

  /** Interprets the given command.
   *  @param toEval command to be evaluated. */
  public final void interpret(String toEval) {
    synchronized (_interpreterLock) {
      _interpreterUsed = true;
      _interpret(toEval);
    }
  }

  /** Interprets the given command.  This should only be called from interpret, never directly.
   *  @param toEval command to be evaluated
   */
  protected abstract void _interpret(String toEval);

  /** Notifies the view that the current interaction is incomplete. */
  protected abstract void _notifyInteractionIncomplete();

  /** Notifies listeners that an interaction has started. (Subclasses must maintain listeners.) */
  protected abstract void _notifyInteractionStarted();

  /** Gets the string representation of the value of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public abstract String getVariableToString(String var);

  /** Gets the class name of a variable in the current interpreter.
   *  @param var the name of the variable
   */
  public abstract String getVariableClassName(String var);

  /** Resets the Java interpreter, resetting the flag to indicate whether the interpreter has been used 
   *  since the last reset.
   */
  public final void resetInterpreter() {
    _interpreterUsed = false;
    _resetInterpreter();
  }

  /** Resets the Java interpreter.  This should only be called from resetInterpreter, never directly. */
  protected abstract void _resetInterpreter();

  /** Returns whether the interpreter has been used since the last reset
   *  operation.  (Set to true in interpret and false in resetInterpreter.)
   */
  public boolean interpreterUsed() { return _interpreterUsed; }

  /** These add the given path to the classpaths used in the interpreter.
   *  @param path Path to add
   */
  public abstract void addProjectClassPath(URL path);
  public abstract void addBuildDirectoryClassPath(URL path);
  public abstract void addProjectFilesClassPath(URL path);
  public abstract void addExternalFilesClassPath(URL path);
  public abstract void addExtraClassPath(URL path);
 
  /** Handles a syntax error being returned from an interaction
   *  @param offset the first character of the error in the InteractionsDocument
   *  @param length the length of the error.
   */
  protected abstract void _notifySyntaxErrorOccurred(int offset, int length);

  /** Opens the files chosen in the given file selector, and returns an ArrayList with one history string 
   *  for each selected file.
   *  @param selector A file selector supporting multiple file selection
   *  @return a list of histories (one for each selected file)
   */
  protected ArrayList<String> _getHistoryText(FileOpenSelector selector)
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
      StringBuffer text = new StringBuffer();
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
              text.append(_newLine);
              break;
            case (2):
              if (!firstLine) text.append(s).append(_newLine); // omit version string from output
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

  /** Removes the interaction-separator comments from a history, so that they will not appear when executing
   *  the history.
   *  @param text The full, formatted text of an interactions history (obtained from _getHistoryText)
   *  @return A list of strings representing each interaction in the history. If no separators are present, 
   *  the entire history is treated as one interaction.
   */
  protected ArrayList<String> _removeSeparators(String text) {
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

  /** Interprets the files selected in the FileOpenSelector. Assumes all strings have no trailing whitespace.
   *  Interprets the array all at once so if there are any errors, none of the statements after the first 
   *  erroneous one are processed.
   */
  public void loadHistory(FileOpenSelector selector) throws IOException {
    ArrayList<String> histories;
    try { histories = _getHistoryText(selector); }
    catch (OperationCanceledException oce) { return; }
    _document.clearCurrentInteraction();

    // Insert into the document and interpret
    StringBuffer buf = new StringBuffer();
    for (String hist: histories) {
      ArrayList<String> interactions = _removeSeparators(hist);
      for (String curr: interactions) {
        int len = curr.length();
        buf.append(curr);
        if (len > 0 && curr.charAt(len - 1) != ';')  buf.append(';');
        buf.append(_newLine);
      }
    }
    _docAppend(buf.toString().trim(), InteractionsDocument.DEFAULT_STYLE);
    interpretCurrentInteraction();
  }

  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException {
    ArrayList<String> histories = _getHistoryText(selector);
    ArrayList<String> interactions = new ArrayList<String>();
    for (String hist: histories) interactions.addAll(_removeSeparators(hist));
    return new InteractionsScriptModel(this, interactions);
  }

  /** Returns the port number to use for debugging the interactions JVM.
   *  Generates an available port if one has not been set manually.
   *  @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException {
    if (!_debugPortSet) _createNewDebugPort();
    return _debugPort;
  }

  /** Generates an available port for use with the debugger.
   *  @throws IOException if unable to get a valid port number.
   */
  protected void _createNewDebugPort() throws IOException {
    try {
      ServerSocket socket = new ServerSocket(0);
      _debugPort = socket.getLocalPort();
      socket.close();
    }
    catch (java.net.SocketException se) {
      // something wrong with sockets, can't use for debugger
      _debugPort = -1;
    }
    _debugPortSet = true;
    if (CodeStatus.DEVELOPMENT) {
      System.setProperty("drjava.debug.port", String.valueOf(_debugPort));
    }
  }

  /** Sets the port number to use for debugging the interactions JVM.
   *  @param port Port to use to debug the interactions JVM
   */
  public void setDebugPort(int port) {
    _debugPort = port;
    _debugPortSet = true;
  }

  /** Called when the repl prints to System.out.
   *  @param s String to print
   */
  public void replSystemOutPrint(String s) {
    _document.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_OUT_STYLE);
  }

  /** Called when the repl prints to System.err.
   *  @param s String to print
   */
  public void replSystemErrPrint(String s) {
    _document.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_ERR_STYLE);
  }

  /** Returns a line of text entered by the user at the equivalent of System.in. */
  public String getConsoleInput() { return _inputListener.getConsoleInput(); }

  /** Sets the listener for any type of single-source input event.
   *  The listener can only be changed with the changeInputListener method.
   *  @param listener a listener that reacts to input requests
   *  @throws IllegalStateException if the input listener is locked
   */
  public void setInputListener(InputListener listener) {
    if (_inputListener == NoInputListener.ONLY) _inputListener = listener;
    else  throw new IllegalStateException("Cannot change the input listener until it is released.");
  }

  /** Changes the input listener. Takes in the old listener to ensure that the owner
   *  of the original listener is aware that it is being changed. It is therefore
   *  important NOT to include a public accessor to the input listener on the model.
   *  @param oldListener the listener that was installed
   *  @param newListener the listener to be installed
   */
  public void changeInputListener(InputListener oldListener, InputListener newListener) {
    // syncrhonize to prevent concurrent modifications to the listener
    synchronized (NoInputListener.ONLY) {
      if (_inputListener == oldListener) _inputListener = newListener;
      else
        throw new IllegalArgumentException("The given old listener is not installed!");      
    }
  }

  /** Any common behavior when an interaction ends. Subclasses might want to additionally notify listeners 
   *  here. (Do this after calling super())
   */
  protected void _interactionIsOver() {
    _document.addToHistory(_toAddToHistory);
    _document.setInProgress(false);
    _document.insertPrompt();
    _notifyInteractionEnded();
  }

  /** Notifies listeners that an interaction has ended. (Subclasses must maintain listeners.) */
  protected abstract void _notifyInteractionEnded();

  /** Appends a string to the given document using a named style. Also waits for a small amount of time 
   *  (_writeDelay) to prevent any one writer from flooding the model with print calls to the point that 
   *  the user interface could become unresponsive.
   *  @param s String to append to the end of the document
   *  @param styleName Name of the style to use for s
   */
  protected void _docAppend(String s, String styleName) {
    synchronized(_writerLock) {
      try {
        _document.insertText(_document.getDocLength(), s, styleName);
        
        // Wait to prevent being flooded with println's
        _writerLock.wait(_writeDelay);
      }
      catch (DocumentAdapterException e) { throw new UnexpectedException(e); }
      catch (InterruptedException e) {
        // It's ok, we'll go ahead and resume
      }
    }
  }

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning no value.
   */
  public void replReturnedVoid() {
    _interactionIsOver();
  }

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning a value.
   *
   * @param result The .toString-ed version of the value that was returned
   *               by the interpretation. We must return the String form
   *               because returning the Object directly would require the
   *               data type to be serializable.
   */
  public void replReturnedResult(String result, String style) {
    _docAppend(result + _newLine, style);
    _interactionIsOver();
  }

  /**
   * Signifies that the most recent interpretation was ended
   * due to an exception being thrown.
   *
   * @param exceptionClass The name of the class of the thrown exception
   * @param message The exception's message
   * @param stackTrace The stack trace of the exception
   */
  public void replThrewException(String exceptionClass, String message, String stackTrace, 
                                 String shortMessage) {
    if (shortMessage!=null) {
      if (shortMessage.endsWith("<EOF>\"")) {
        interactionContinues();
        return;
      }
    }
    _document.appendExceptionResult(exceptionClass, message, stackTrace,
                                    InteractionsDocument.ERROR_STYLE);
    _interactionIsOver();
  }

  /** Signifies that the most recent interpretation was preempted by a syntax error.  The integer parameters
   *  support future error highlighting.
   *  @param errorMessage The syntax error message
   *  @param startRow The starting row of the error
   *  @param startCol The starting column of the error
   *  @param endRow The end row of the error
   *  param endCol The end column of the error
   */
  public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol,
                                      int endRow, int endCol ) {
    if (errorMessage!=null) {
      if (errorMessage.endsWith("<EOF>\"")) {
        interactionContinues();
        return;
      }
    }
    
    edu.rice.cs.util.Pair<Integer,Integer> oAndL =
      StringOps.getOffsetAndLength(interaction, startRow, startCol, endRow, endCol);

    _notifySyntaxErrorOccurred(_document.getPromptPos() + oAndL.getFirst().intValue(),
                                oAndL.getSecond().intValue());

    _document.appendSyntaxErrorResult(errorMessage, interaction, startRow, startCol, endRow, endCol,
                                      InteractionsDocument.ERROR_STYLE);

    _interactionIsOver();
  }

  /** Signifies that the most recent interpretation contained a call to System.exit.
   *  @param status The exit status that will be returned.
   */
  public void replCalledSystemExit(int status) { _notifyInterpreterExited(status); }

  /** Notifies listeners that the interpreter has exited unexpectedly. (Subclasses must maintain listeners.)
   *  @param status Status code of the dead process
   */
  protected abstract void _notifyInterpreterExited(int status);

  /** Called when the interpreter starts to reset. */
  public void interpreterResetting() {
    if (!_waitingForFirstInterpreter) {
      _document.insertBeforeLastPrompt("Resetting Interactions..." + _newLine,
                                       InteractionsDocument.ERROR_STYLE);
      _document.setInProgress(true);

      // Change to a new debug port to avoid conflicts
      try { _createNewDebugPort(); }
      catch (IOException ioe) {
        // Oh well, leave it at the previous port
      }
      _notifyInterpreterResetting();
    }
  }

  /** Notifies listeners that the interpreter is resetting. (Subclasses must maintain listeners.) */
  protected abstract void _notifyInterpreterResetting();

  /** This method is called by the Main JVM if the Interpreter JVM cannot
   *  be exited (likely because of its having a security manager)
   *  @param t The Throwable thrown by System.exit
   */
  public void interpreterResetFailed(Throwable t) {
    _interpreterResetFailed(t);
    _document.setInProgress(false);
    _notifyInterpreterResetFailed(t);
  }

  /** Any extra action to perform (beyond notifying listeners) when the interpreter fails to reset.
   *  @param t The Throwable thrown by System.exit
   */
  protected abstract void _interpreterResetFailed(Throwable t);

  /** Notifies listeners that the interpreter reset failed. (Subclasses must maintain listeners.)
   *  @param t Throwable explaining why the reset failed.
   */
  protected abstract void _notifyInterpreterResetFailed(Throwable t);

  /** Called when a new Java interpreter has registered and is ready for use. */
  public void interpreterReady() {
    if (!_waitingForFirstInterpreter) {
      _document.reset();
      _document.setInProgress(false);
      _notifyInterpreterReady();
    }
    _waitingForFirstInterpreter = false;
  }

  /** Notifies listeners that the interpreter is ready. (Subclasses must maintain listeners.) */
  protected abstract void _notifyInterpreterReady();

  /** Assumes a trimmed String. Returns a string of the main call that the interpretor can use. */
  protected static String _testClassCall(String s) {
    if (s.endsWith(";"))  s = _deleteSemiColon(s);
    List<String> args = ArgumentTokenizer.tokenize(s, true);
    boolean seenArg = false;
    String className = args.get(1);
    StringBuffer mainCall = new StringBuffer();
    mainCall.append(className.substring(1, className.length() - 1));
    mainCall.append(".main(new String[]{");
    for (int i = 2; i < args.size(); i++) {
      if (seenArg) mainCall.append(",");
      else seenArg = true;
      mainCall.append(args.get(i));
    }
    mainCall.append("});");
    return mainCall.toString();
  }

  /** Deletes the last character of a string.  Assumes semicolon at the end, but does not check.  Helper 
   *  for _testClassCall(String).
   *  @param s the String containing the semicolon
   *  @return a substring of s with one less character
   */
  protected static String _deleteSemiColon(String s) { return  s.substring(0, s.length() - 1); }

  /** Singleton InputListener which should never be asked for input. */
  private static class NoInputListener implements InputListener {
    public static final NoInputListener ONLY = new NoInputListener();
    private NoInputListener() { }

    public String getConsoleInput() { throw new IllegalStateException("No input listener installed!"); }
  }
}
