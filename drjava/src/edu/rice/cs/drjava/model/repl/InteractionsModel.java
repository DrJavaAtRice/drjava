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
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import java.util.Vector;
import java.util.ArrayList;

import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.model.FileOpenSelector;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;

import edu.rice.cs.javaast.*;
import edu.rice.cs.javaast.tree.*;
import edu.rice.cs.javaast.parser.*;

/**
 * A model which can serve as the glue between an InteractionsDocument and
 * any JavaInterpreter.  This abstract class provides common functionality
 * for all such models.
 * @version $Id$
 */
public abstract class InteractionsModel implements InteractionsModelCallback {

  /**
   * Keeps track of any listeners to the model.
   */
  protected final InteractionsEventNotifier _notifier =
    new InteractionsEventNotifier();
  
  protected static final String _newLine = System.getProperty("line.separator");
  
  /**
   * InteractionsDocument containing the commands and history.
   */
  protected final InteractionsDocument _document;
  
  /**
   * Whether we are waiting for the interpreter to register for the first time.
   */
  protected boolean _waitingForFirstInterpreter;
  
  /**
   * Whether the interpreter has been used since its last reset.
   */
  protected boolean _interpreterUsed;
  
  /**
   * A lock object to prevent multiple threads from interpreting at once.
   */
  private final Object _interpreterLock;
  
  /**
   * A lock object to prevent print calls to System.out or System.err
   * from flooding the JVM, ensuring the UI remains responsive.
   */
  private final Object _writerLock;
  
  /**
   * Number of milliseconds to wait after each println, to prevent
   * the JVM from being flooded with print calls.
   */
  private int _writeDelay;
  
  /**
   * Port used by the debugger to connect to the Interactions JVM.
   * Uniquely created in getDebugPort().
   */
  private int _debugPort;
  
  /**
   * Whether the debug port has been set already or not.
   * If not, calling getDebugPort will generate an available port.
   */
  private boolean _debugPortSet;
  
  /** Interactions processor, currently a pre-processor **/
  private InteractionsProcessorI _interactionsProcessor;

  /**
   * Constructs an InteractionsModel.
   * @param adapter DocumentAdapter to use in the InteractionsDocument
   * @param historySize Number of lines to store in the history
   * @param writeDelay Number of milliseconds to wait after each println
   */
  public InteractionsModel(DocumentAdapter adapter, int historySize,
                           int writeDelay) {
    _writeDelay = writeDelay;
    _document = new InteractionsDocument(adapter, historySize);
    _waitingForFirstInterpreter = true;
    _interpreterUsed = false;
    _interpreterLock = new Object();
    _writerLock = new Object();
    _debugPort = -1;
    _debugPortSet = false;
    _interactionsProcessor = new InteractionsProcessor();
  }
  
  /**
   * Add a JavadocListener to the model.
   * @param listener a listener that reacts to Interactions events
   */
  public void addListener(InteractionsListener listener) {
    _notifier.addListener(listener);
  }

  /**
   * Remove an InteractionsListener from the model.  If the listener is not
   * currently listening to this model, this method has no effect.
   * @param listener a listener that reacts to Interactions events
   */
  public void removeListener(InteractionsListener listener) {
    _notifier.removeListener(listener);
  }

  /**
   * Removes all InteractionsListeners from this model.
   */
  public void removeAllInteractionListeners() {
    _notifier.removeAllListeners();
  }
  
  /**
   * Returns the InteractionsDocument stored by this model.
   */
  public InteractionsDocument getDocument() {
    return _document;
  }
  
  /**
   * Sets this model's notion of whether it is waiting for the first
   * interpreter to connect.  The interactionsReady event is not fired
   * for the first interpreter.
   */
  public void setWaitingForFirstInterpreter(boolean waiting) {
    _waitingForFirstInterpreter = waiting;
  }
  
  /**
   * Interprets the current given text at the prompt in the interactions doc.
   */
  public void interpretCurrentInteraction() {
    synchronized(_interpreterLock) {
      // Don't start a new interaction while one is in progress
      if (_document.inProgress()) {
        return;
      }

      String text = _document.getCurrentInteraction();
      String toEval = text.trim();
      if (toEval.startsWith("java ")) {
        toEval = _testClassCall(toEval);
      }

      try {
//        _checkInteraction(text);
        toEval = _interactionsProcessor.preProcess(toEval);

        _prepareToInterpret(text);
        interpret(toEval);
      }
      catch (ParseException pe) {
        // A ParseException indicates a syntax error in the input window
        String errMsg = pe.getInteractionsMessage();
//        javax.swing.JOptionPane.showMessageDialog(null, "ParseException:\n" + errMsg);
        if (errMsg.endsWith("<EOF>\"")) {
          _notifier.interactionIncomplete();
        }
        else {
          _prepareToInterpret(text);
          replReturnedSyntaxError(errMsg, text, pe.getBeginLine(),
                                  pe.getBeginColumn(), pe.getEndLine(), pe.getEndColumn());
        }
      }
      catch (TokenMgrError tme) {
        // A TokenMgrError indicates some lexical difficulty with input.
//        javax.swing.JOptionPane.showMessageDialog(null, "TokenMgrError:\n" + tme.getMessage());
        _prepareToInterpret(text);
        int row = tme.getErrorRow();
        int col = tme.getErrorColumn() - 1;
        replReturnedSyntaxError(tme.getMessage(), text, row, col, row, col);
      }
    }
  }

  /**
   * Performs pre-interpretation preparation of the interactions document and
   * notifies the view.
   */
  private void _prepareToInterpret(String text) {
    _docAppend(_newLine, InteractionsDocument.DEFAULT_STYLE);
    _notifyInteractionStarted();
    _document.setInProgress(true);
    _document.addToHistory(text);
  }

  /**
   * Interprets the given command.
   * @param toEval command to be evaluated
   */
  public final void interpret(String toEval) {
    _interpreterUsed = true;
    _interpret(toEval);
  }
  
  /**
   * Interprets the given command.  This should only be called from
   * interpret, never directly.
   * @param toEval command to be evaluated
   */
  protected abstract void _interpret(String toEval);

  /**
   * Verifies that the current interaction is "complete"; i.e. the user has
   * finished typing.
   * @param toCheck the String to check
   * @return true iff the interaction is complete
   */
//  protected abstract boolean _checkInteraction(String toCheck);
  /**
   * Verifies that the current interaction is "complete"; i.e. the user has
   * finished typing.
   * @param toCheck the String to check
   * @return true iff the interaction is complete
   */
  protected boolean _checkInteraction(String toCheck) {
    String result;
    try {
      result = _interactionsProcessor.preProcess(toCheck);
    }
    catch (ParseException pe) {
      // A ParseException indicates a syntax error in the input window
      if (pe.getInteractionsMessage().endsWith("<EOF>\"")) {
        _notifier.interactionIncomplete();
        return false;
      }
    }
    catch (TokenMgrError tme) {
      // A TokenMgrError indicates some lexical difficulty with input.
    }
    return true;
  }

  /**
   * Notifies listeners that an interaction has started.
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInteractionStarted();
  
  /**
   * Gets the string representation of the value of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public abstract String getVariableToString(String var);
  
  /**
   * Gets the class name of a variable in the current interpreter.
   * @param var the name of the variable
   */
  public abstract String getVariableClassName(String var);
  
  /**
   * Resets the Java interpreter, resetting the flag to indicate whether
   * the interpreter has been used since the last reset.
   */
  public final void resetInterpreter() {
    _interpreterUsed = false;
    _resetInterpreter();
  }
  
  /**
   * Resets the Java interpreter.  This should only be called from
   * resetInterpreter, never directly.
   */
  protected abstract void _resetInterpreter();
  
  /**
   * Returns whether the interpreter has been used since the last reset
   * operation.  (Set to true in interpret and false in resetInterpreter.)
   */
  public boolean interpreterUsed() {
    return _interpreterUsed;
  }
  
  /**
   * Adds the given path to the interpreter's classpath.
   * @param path Path to add
   */
  public abstract void addToClassPath(String path);
  
  /** 
   * Handles a syntax error being returned from an interaction
   * @param offset the first character of the error in the InteractionsDocument 
   * @param length the length of the error.
   */
  protected abstract void _notifySyntaxErrorOccurred(int offset, int length);

  /**
   * Opens the files chosen in the given file selector, and returns an ArrayList
   * with one history string for each selected file.
   * @param selector A file selector supporting multiple file selection
   * @return a list of histories (one for each selected file)
   */
  protected ArrayList<String> _getHistoryText(FileOpenSelector selector)
    throws IOException, OperationCanceledException
  {
    File[] files = null;
    files = selector.getFiles();
    ArrayList<String> histories = new ArrayList<String>();
    ArrayList<String> strings = new ArrayList<String>();
    if (files == null) {
      throw new IOException("No Files returned from FileSelector");
    }
    
    for (int i=0; i < files.length; i++) {
      if (files[i] == null) {
        throw new IOException("File name returned from FileSelector is null");
      }
      File c = files[i];
      if (c != null) {
        try {
          FileInputStream fis = new FileInputStream(c);
          InputStreamReader isr = new InputStreamReader(fis);
          BufferedReader br = new BufferedReader(isr);
          String currLine;
          while ((currLine = br.readLine()) != null) {
            strings.add(currLine);
          }
          br.close(); // win32 needs readers closed explicitly!
        }
        catch (IOException ioe) {
          throw new IOException("File name returned from FileSelector is null");
          //_showIOError(ioe);
        }
      }
      
      // Create a single string with all formatted lines from this history
      String text = "";
      String currString;
      boolean firstLine = true;
      int formatVersion = 1;
      for (int j = 0; j < strings.size(); j++) {
        currString = strings.get(j);
        if (currString.length() > 0) {
          // check for file format version string.
          // NOTE: the original file format did not have a version string
          if (firstLine && (currString.trim().equals(History.HISTORY_FORMAT_VERSION_2.trim()))) {
            formatVersion = 2;
          }
          switch (formatVersion) {
            case (1):
              // When reading this format, we need to make sure each line ends in a semicolon.
              // This behavior can be buggy; that's why the format was changed.
              if (currString.charAt(currString.length() - 1) == ';') {
                text += currString + _newLine;
              }
              else {
                text += currString + ";" + _newLine;
              }
              break;
            case (2):
              if (!firstLine) { // don't include format version string in output
                text += currString + _newLine;
              }
              break;
          }
          firstLine = false;
        }
      }

      // Add the entire formatted text to the list of histories
      histories.add(text);
    }
    return histories;
  }

  /**
   * Removes the interaction-separator comments from a history, so that they
   * will not appear when executing the history.
   * @param text The full, formatted text of an interactions history (obtained
   * from _getHistoryText)
   * @return A list of strings representing each interaction in the history.
   * If no separators are present, the entire history is treated as one
   * interaction.
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
    if (!"".equals(last)) {
      interactions.add(last);
    }
    return interactions;
  }

  /**
   * Interprets the files selected in the FileOpenSelector. Assumes all strings
   * have no trailing whitespace.  Interprets the array all at once so if there
   * are any errors, none of the statements after the first erroneous one are
   * processed.
   */
  public void loadHistory(FileOpenSelector selector) throws IOException {
    ArrayList<String> histories;
    try {
      histories = _getHistoryText(selector);
    }
    catch (OperationCanceledException oce) {
      return;
    }
    _document.clearCurrentInteraction();

    // Insert into the document and interpret
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < histories.size(); i++) {
      ArrayList<String> interactions = _removeSeparators(histories.get(i));
      for (int j = 0; j < interactions.size(); j++) {
        String curr = interactions.get(j);
        int len = curr.length();
        buf.append(curr);
        if (len > 0 && curr.charAt(len - 1) != ';') {
          buf.append(';');
        }
        buf.append(_newLine);
      }
    }
    _docAppend(buf.toString().trim(), InteractionsDocument.DEFAULT_STYLE);
    interpretCurrentInteraction();
  }

  public InteractionsScriptModel loadHistoryAsScript(FileOpenSelector selector)
    throws IOException, OperationCanceledException
  {
    ArrayList<String> histories = _getHistoryText(selector);
    ArrayList<String> interactions = new ArrayList<String>();
    for (int i = 0; i < histories.size(); i++) {
      interactions.addAll(_removeSeparators(histories.get(i)));
    }
    return new InteractionsScriptModel(this, interactions);
  }
  
  /**
   * Returns the port number to use for debugging the interactions JVM.
   * Generates an available port if one has not been set manually.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException {
    if (!_debugPortSet) {
      _createNewDebugPort();
    }
    return _debugPort;
  }
  
  /**
   * Generates an available port for use with the debugger.
   * @throws IOException if unable to get a valid port number.
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
  
  /**
   * Sets the port number to use for debugging the interactions JVM.
   * @param port Port to use to debug the interactions JVM
   */
  public void setDebugPort(int port) {
    _debugPort = port;
    _debugPortSet = true;
  }
  
  /**
   * Called when the repl prints to System.out.
   * @param s String to print
   */
  public void replSystemOutPrint(String s) {
    _document.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_OUT_STYLE);
  }

  /**
   * Called when the repl prints to System.err.
   * @param s String to print
   */
  public void replSystemErrPrint(String s) {
    _document.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_ERR_STYLE);
  }
  
  /**
   * Returns a line of text entered by the user at the equivalent
   * of System.in.
   */
  public abstract String getConsoleInput();

  /**
   * Any common behavior when an interaction ends.
   * Subclasses might want to additionally notify listeners here.
   * (Do this after calling super())
   */
  protected void _interactionIsOver() {
    _document.setInProgress(false);
    _document.insertPrompt();
    _notifyInteractionEnded();
  }
  
  /**
   * Notifies listeners that an interaction has ended.
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInteractionEnded();
  
  /**
   * Appends a string to the given document using a named style.
   * Also waits for a small amount of time (_writeDelay) to prevent any one
   * writer from flooding the model with print calls to the point that the
   * user interface could become unresponsive.
   * @param s String to append to the end of the document
   * @param styleName Name of the style to use for s
   */
  protected void _docAppend(String s, String styleName) {
    synchronized(_document) {
      synchronized(_writerLock) {
        try {
          _document.insertText(_document.getDocLength(), s, styleName);
          
          // Wait to prevent being flooded with println's
          _writerLock.wait(_writeDelay);
        }
        catch (DocumentAdapterException e) {
          throw new UnexpectedException(e);
        }
        catch (InterruptedException e) {
          // It's ok, we'll go ahead and resume
        }
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
  public void replReturnedResult(String result) {
    _docAppend(result + _newLine, InteractionsDocument.DEFAULT_STYLE);
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
  public void replThrewException(String exceptionClass,
                                 String message,
                                 String stackTrace) {
    _document.appendExceptionResult(exceptionClass,
                                    message,
                                    stackTrace,
                                    InteractionsDocument.ERROR_STYLE);
    _interactionIsOver();
  }
  
  /**
   * Signifies that the most recent interpretation was preempted
   * by a syntax error.  The integer parameters support future
   * error highlighting.
   *
   * @param errorMessage The syntax error message
   * @param startRow The starting row of the error
   * @param startCol The starting column of the error
   * @param startRow The end row of the error
   * @param startCol The end column of the error
   */
  public void replReturnedSyntaxError(String errorMessage,
                                      String interaction,
                                      int startRow,
                                      int startCol,
                                      int endRow,
                                      int endCol ) {
    edu.rice.cs.util.Pair<Integer,Integer> oAndL = 
      StringOps.getOffsetAndLength(interaction, startRow, startCol, endRow, endCol);
    
    _notifySyntaxErrorOccurred(_document.getPromptPos() + oAndL.getFirst().intValue(),
                                oAndL.getSecond().intValue());
    
    _document.appendSyntaxErrorResult(errorMessage,
                                      startRow,
                                      startCol,
                                      endRow,
                                      endCol,
                                      InteractionsDocument.ERROR_STYLE);

    _interactionIsOver();
  }
  
  /**
   * Signifies that the most recent interpretation contained a call to
   * System.exit.
   *
   * @param status The exit status that will be returned.
   */
  public void replCalledSystemExit(int status) {
    _notifyInterpreterExited(status);
  }
  
  /**
   * Notifies listeners that the interpreter has exited unexpectedly.
   * @param status Status code of the dead process
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInterpreterExited(int status);
  
  /**
   * Called when the interpreter starts to reset.
   */
  public void interpreterResetting() {
    if (!_waitingForFirstInterpreter) {
      _document.insertBeforeLastPrompt("Resetting Interactions..." + _newLine,
                                       InteractionsDocument.ERROR_STYLE);
      _document.setInProgress(true);
      
      // Change to a new debug port to avoid conflicts
      try {
        _createNewDebugPort();
      }
      catch (IOException ioe) {
        // Oh well, leave it at the previous port
      }
      
      _notifyInterpreterResetting();
    }
  }
  
  /**
   * Notifies listeners that the interpreter is resetting.
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInterpreterResetting();
  
  /**
   * This method is called by the Main JVM if the Interpreter JVM cannot
   * be exited (likely because of its having a security manager)
   * @param t The Throwable thrown by System.exit
   */
  public void interpreterResetFailed(Throwable t) {
    _interpreterResetFailed(t);
    _document.setInProgress(false);
    _notifyInterpreterResetFailed(t);
  }
  
  /**
   * Any extra action to perform (beyond notifying listeners) when
   * the interpreter fails to reset.
   * @param t The Throwable thrown by System.exit
   */
  protected abstract void _interpreterResetFailed(Throwable t);
  
  /**
   * Notifies listeners that the interpreter reset failed.
   * @param t Throwable explaining why the reset failed.
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInterpreterResetFailed(Throwable t);
  
  /**
   * Called when a new Java interpreter has registered and is ready for use.
   */
  public void interpreterReady() {
    if (!_waitingForFirstInterpreter) {
      _document.reset();
      _document.setInProgress(false);
      _notifyInterpreterReady();
    }
    _waitingForFirstInterpreter = false;
  }
  
  /**
   * Notifies listeners that the interpreter is ready.
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInterpreterReady();
  
  
  /**
   * Assumes a trimmed String. Returns a string of the main call that the
   * interpretor can use.
   */
  protected static String _testClassCall(String s) {
    if (s.endsWith(";")) {
      s = _deleteSemiColon(s);
    }
    List<String> args = ArgumentTokenizer.tokenize(s, true);
    boolean seenArg = false;
    String className = args.get(1);
    StringBuffer mainCall = new StringBuffer();
    mainCall.append(className.substring(1, className.length() - 1));
    mainCall.append(".main(new String[]{");
    for (int i = 2; i < args.size(); i++) {
      if (seenArg) {
        mainCall.append(",");
      }
      else {
        seenArg = true;
      }
      mainCall.append(args.get(i));
    }
    mainCall.append("});");
    return mainCall.toString();
  }

  /**
   * Deletes the last character of a string.  Assumes semicolon at the
   * end, but does not check.  Helper for _testClassCall(String).
   * @param s
   * @return
   */
  protected static String _deleteSemiColon(String s) {
    return  s.substring(0, s.length() - 1);
  }
}
