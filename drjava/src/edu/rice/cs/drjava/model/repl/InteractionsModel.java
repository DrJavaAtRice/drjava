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

import java.io.*;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import gj.util.Vector;

import edu.rice.cs.drjava.model.FileOpenSelector;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.repl.newjvm.MainJVM;
import edu.rice.cs.util.*;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * A model which can serve as the glue between an InteractionsDocument and
 * any JavaInterpreter.  This abstract class provides common functionality
 * for all such models.
 * @version $Id$
 */
public abstract class InteractionsModel implements InteractionsModelCallback {
  
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
  
  /**
   * Constructs an InteractionsModel.
   * @param adapter DocumentAdapter to use in the InteractionsDocument
   * @param historySize Number of lines to store in the history
   * @param writeDelay Number of milliseconds to wait after each println
   */
  public InteractionsModel(DocumentAdapter adapter, int historySize, int writeDelay) {
    _writeDelay = writeDelay;
    _document = new InteractionsDocument(adapter, historySize);
    _waitingForFirstInterpreter = true;
    _interpreterUsed = false;
    _interpreterLock = new Object();
    _writerLock = new Object();
    _debugPort = -1;
    _debugPortSet = false;
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
      
      _notifyInteractionStarted();
      
      String text = _document.getCurrentInteraction();
      _document.setInProgress(true);
      _document.addToHistory(text);
      
      // there is no return at the end of the last line
      // better to put it on now and not later.
      _docAppend("\n", InteractionsDocument.DEFAULT_STYLE);
      
      String toEval = text.trim();
      if (toEval.startsWith("java ")) {
        toEval = _testClassCall(toEval);
      }
      interpret(toEval);
    }
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
   * Notifies listeners that an interaction has started.
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInteractionStarted();
  
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
   * Interprets the files selected in the FileOpenSelector. Assumes all strings
   * have no trailing whitespace.  Interprets the array all at once so if there
   * are any errors, none of the statements after the first erroneous one are
   * processed.
   */
  public void loadHistory(FileOpenSelector selector) throws IOException {
    
    File[] files = null;
    try {
      files = selector.getFiles();
    }
    catch (OperationCanceledException oce) {
      return;
      // don't need to do anything
    }
    Vector<String> strings = new Vector<String>();
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
            strings.addElement(currLine);
          }
          br.close(); // win32 needs readers closed explicitly!
        }
        catch (IOException ioe) {
          throw new IOException("File name returned from FileSelector is null");
          //_showIOError(ioe);
        }
        
      }
      
      String text = "";
      String currString;
      boolean firstLine = true;
      int formatVersion = 1;
      for (int j = 0; j < strings.size(); j++) {
        currString = strings.elementAt(j);
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
                text += currString + "\n";
              }
              else {
                text += currString + ";\n";
              }
              break;
            case(2):
              if (!firstLine) { // don't include format version string in output
                text += currString + "\n";
              }
              break;
          }
          firstLine = false;
        }
      }
      _document.clearCurrentInteraction();

      // Crop off the last newline
      text.trim();

      // Insert into the document and interpret
      _docAppend(text, InteractionsDocument.DEFAULT_STYLE);
      interpretCurrentInteraction();
    }
  }
  
  
  /**
   * Returns the port number to use for debugging the interactions JVM.
   * Generates an available port if one has not been set manually.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException {
    if (!_debugPortSet) {
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
    }
    return _debugPort;
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
    _docAppend(result + "\n", InteractionsDocument.DEFAULT_STYLE);
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
      StringOps.getOffsetAndLength( interaction, startRow, startCol, endRow, endCol );
    
    _notifySyntaxErrorOccurred( _document.getPromptPos() + oAndL.getFirst().intValue(),
                                oAndL.getSecond().intValue() );
    
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
   * This method is called by the Main JVM if the Interpreter JVM cannot
   * be exited (likely because of its having a security manager)
   * @param th The Throwable thrown by System.exit
   */
  public void interpreterResetFailed(Throwable th) {    
    _document.insertBeforeLastPrompt("Reset Failed! See the console tab for details.\n",
                                     InteractionsDocument.ERROR_STYLE);
    _document.setInProgress(false);
    _notifyInterpreterResetFailed(th);
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
      _document.insertBeforeLastPrompt("Resetting Interactions...\n",
                                       InteractionsDocument.ERROR_STYLE);
      _document.setInProgress(true);
      _notifyInterpreterResetting();
    }
  }
  
  /**
   * Notifies listeners that the interpreter is resetting.
   * (Subclasses must maintain listeners.)
   */
  protected abstract void _notifyInterpreterResetting();
  
  /**
   * Notifies listeners that the interpreter reset failed.
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
    LinkedList ll = new LinkedList();
    if (s.endsWith(";"))
      s = _deleteSemiColon(s);
    StreamTokenizer st = new StreamTokenizer(new StringReader(s));
    st.ordinaryChar('\'');
    st.ordinaryChar('\\');
    st.ordinaryChars('0','9');
    st.ordinaryChars('-', '.');
    st.wordChars('\'', '\'');
    st.wordChars('\\', '\\');
    st.wordChars('0', '9');
    st.wordChars('-', '.');
    
    try {
      st.nextToken();             //don't want to get back java
      st.nextToken();             //move to second token
      String className = st.sval;
      StringBuffer mainCall = new StringBuffer(className);
      mainCall.append(".main(new String[]{");
      
      // Add each argument
      boolean seenArg = false;
      while (st.nextToken() != StreamTokenizer.TT_EOF) {
        if ((st.ttype == StreamTokenizer.TT_WORD) ||
            (st.ttype == '"'))
        {
          if (seenArg) {
            mainCall.append(",");
          }
          else {
            seenArg = true;
          }
          mainCall.append("\"");
          mainCall.append(_escapeQuotesAndBackslashes(st.sval));
          mainCall.append("\"");
        }
        else {
          throw new IllegalArgumentException("Unknown token type: " + st.ttype);
        }
      }
      mainCall.append("});");
      return mainCall.toString();
    }
    catch (IOException ioe) {
      // Can't happen with a StringReader.
      throw new UnexpectedException(ioe);
    }
    
    
    
    
    /*
    LinkedList ll = new LinkedList();
    if (s.endsWith(";"))
      s = _deleteSemiColon(s);
    StringTokenizer st = new StringTokenizer(s);
    st.nextToken();             //don't want to get back java
    String argument = st.nextToken();           // must have a second Token
    while (st.hasMoreTokens())
      ll.add(st.nextToken());
    argument = argument + ".main(new String[]{";
    ListIterator li = ll.listIterator(0);
    while (li.hasNext()) {
      String currArg = (String)li.next();
      currArg = _escapeQuotesAndBackslashes(currArg);
      
      argument = argument + "\"" + currArg + "\"";
      if (li.hasNext())
        argument = argument + ",";
    }
    argument = argument + "});";
    return  argument;
    */
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
  
  /**
   * Inserts backslashes before any occurrences of a backslash or
   * quote in the given string.  Also converts any special characters
   * appropriately.
   */
  protected static String _escapeQuotesAndBackslashes(String s) {
    StringBuffer buf = new StringBuffer(s);
    int lastIndex = 0;
    
    // Walk backwards, looking for quotes or backslashes.
    //  If we see any, insert an extra backslash into the buffer at
    //  the same index.  (By walking backwards, the index into the buffer
    //  will remain correct as we change the buffer.)
    for (int i = s.length()-1; i >= 0; i--) {
      char c = s.charAt(i);
      if ((c == '\\') || (c == '"')) {
        buf.insert(i, '\\');
      }
      // Replace any special characters with escaped versions
      else if (c == '\n') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\n");
      }
      else if (c == '\t') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\t");
      }
      else if (c == '\r') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\r");
      }
      else if (c == '\b') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\b");
      }
      else if (c == '\f') {
        buf.deleteCharAt(i);
        buf.insert(i, "\\f");
      }
    }
    return buf.toString();
  }
}
