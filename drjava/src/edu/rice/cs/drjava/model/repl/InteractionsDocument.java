/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.io.*;
import java.awt.print.*;

import edu.rice.cs.drjava.model.print.DrJavaBook;

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.EditDocumentInterface;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.drjava.config.OptionListener;

/** Toolkit-independent document that provides console-like interaction with a Java interpreter.
 *  @version $Id$
 */
public class InteractionsDocument extends ConsoleDocument {

  /** Default prompt. */
  public static final String DEFAULT_PROMPT = "> ";

  /** Style for error messages */
  public static final String ERROR_STYLE = "error";

  /** Style for debugger messages */
  public static final String DEBUGGER_STYLE = "debugger";

  public static final String OBJECT_RETURN_STYLE = "object.return.style";
  
  public static final String STRING_RETURN_STYLE = "string.return.style";
  
  public static final String CHARACTER_RETURN_STYLE = "character.return.style";
  
  public static final String NUMBER_RETURN_STYLE = "number.return.style";
  
   /** Command-line history. It's not reset when the interpreter is reset. */
  private final History _history;

  /* Constructors */

  /** Reset the document on startUp.  Uses a history with configurable size.
   *  @param document the edit document to use for the model
   */
  public InteractionsDocument(EditDocumentInterface document, String banner) { this(document, new History(), banner); }

  /** Reset the document on startUp.  Uses a history with the given
   *  maximum size.  This history will not use the config framework.
   *  @param document EditDocumentInterface to use for the model
   *  @param maxHistorySize Number of commands to remember in the history
   */
  public InteractionsDocument(EditDocumentInterface document, int maxHistorySize, String banner) {
    this(document, new History(maxHistorySize), banner);
  }
  
  /** Creates and resets the interactions document on DrJava startUp.  Uses the given history.  
   *  @param document EditDocumentInterface to use for the model
   *  @param history History of commands
   */
  public InteractionsDocument(EditDocumentInterface document, History history, String banner) {
    super(document);
    _history = history;
    _hasPrompt = true;
    _prompt = DEFAULT_PROMPT;
    reset(banner);
  }

  /** Lets this document know whether an interaction is in progress.
   *  @param inProgress whether an interaction is in progress
   */
  public void setInProgress(boolean inProgress) { 
    acquireWriteLock();
    _hasPrompt = ! inProgress;
    releaseWriteLock();
  }

  /** Returns whether an interaction is currently in progress. */
  public boolean inProgress() { return ! _hasPrompt; }

  /** Resets the document to a clean state.  Does not reset the history. */
  public void reset(String banner) {
    acquireWriteLock();
    try {
      forceRemoveText(0, _document.getLength());
      forceInsertText(0, banner, OBJECT_RETURN_STYLE);
      insertPrompt();
      _history.moveEnd();
      setInProgress(false);
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
    finally { releaseWriteLock(); }
  }

  /** Replaces any text entered past the prompt with the current item in the history. */
  private void _replaceCurrentLineFromHistory() {
    try {
      _clearCurrentInputText();
      append(_history.getCurrent(), DEFAULT_STYLE);
    }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
  }

  /** Accessor method for the history of commands. */
  public OptionListener<Integer> getHistoryOptionListener() { return _history.getHistoryOptionListener(); }

  /** Adds the given text to the history of commands. */
  public void addToHistory(String text) { 
    acquireWriteLock();
    try { _history.add(text); } 
    finally { releaseWriteLock(); }
  }

  /** Saves the unedited version of the current history to a file
   *  @param selector File to save to
   */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    acquireReadLock();  // does not modify state of document including history
    try { _history.writeToFile(selector); }
    finally { releaseReadLock(); }
  }

  /** Saves the edited version of the current history to a file
   *  @param selector File to save to
   *  @param editedVersion Edited verison of the history which will be
   *  saved to file instead of the lines saved in the history. The saved
   *  file will still include any tags needed to recognize it as a saved
   *  interactions file.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
      acquireReadLock();  // does not modify state of document including history
      try { _history.writeToFile(selector, editedVersion); }
      finally { releaseReadLock(); }
  }

  /** Returns the entire history as a single string.  Commands should be separated by semicolons. If an entire
   *  command does not end in a semicolon, one is added.
   */
  public String getHistoryAsStringWithSemicolons() {
    acquireReadLock();
    try { return _history.getHistoryAsStringWithSemicolons(); }
    finally { releaseReadLock(); }
  }

  /** Returns the entire history as a single string.  Commands should be separated by semicolons. */
  public String getHistoryAsString() { 
    acquireReadLock();
    try { return _history.getHistoryAsString(); }
    finally { releaseReadLock(); }
  }

  /** Clears the history */
  public void clearHistory() { 
    acquireWriteLock();
    try { _history.clear(); }
    finally { releaseWriteLock(); }
  }
  
  public String lastEntry() { 
    acquireReadLock();
    try { return _history.lastEntry(); }  // may throw a RuntimeException if no such entry
    finally { releaseReadLock(); }
  }
  /** Puts the previous line from the history on the current line and moves the history back one line.
   *  @param entry the current entry (perhaps edited from what is in history)
   */
  public void moveHistoryPrevious(String entry) {
    acquireWriteLock();
    try { 
      _history.movePrevious(entry);
      _replaceCurrentLineFromHistory();
    }
    finally { releaseWriteLock(); }
  }
  
  /** Puts the next line from the history on the current line and moves the history forward one line.
   *  @param entry the current entry (perhaps edited from what is in history)
   */
  public void moveHistoryNext(String entry) {
    acquireWriteLock();
    try {
      _history.moveNext(entry);
      _replaceCurrentLineFromHistory();
    }
   finally { releaseWriteLock(); }
  }
  
  /** Returns whether there is a previous command in the history. */
  public boolean hasHistoryPrevious() { 
    acquireReadLock();
    try { return _history.hasPrevious(); }
    finally { releaseReadLock(); }
  }

  /** Returns whether there is a next command in the history. */
  public boolean hasHistoryNext() { 
    acquireReadLock();
    try { return _history.hasNext(); }
    finally { releaseReadLock(); }
  }
  
  /** Reverse searches the history for the given string.
   *  @param searchString the string to search for
   */
  public void reverseHistorySearch(String searchString) {
    acquireWriteLock();
    try {
      _history.reverseSearch(searchString);
      _replaceCurrentLineFromHistory();
    }
    finally { releaseWriteLock(); }
  }
  
  /** Forward searches the history for the given string.
   *  @param searchString the string to search for
   */
  public void forwardHistorySearch(String searchString) {
    acquireWriteLock();
    try {   
      _history.forwardSearch(searchString);
      _replaceCurrentLineFromHistory();
    }
    finally { releaseWriteLock(); }
  }
  
  /** Gets the previous interaction in the history and replaces whatever is on the current interactions input
   *  line with this interaction.
   */
  public boolean recallPreviousInteractionInHistory() {
    acquireWriteLock();
    try {    
      if (hasHistoryPrevious()) {
        moveHistoryPrevious(getCurrentInteraction());
        return true;
      }
      _beep.run();
      return false;
    }
    finally { releaseWriteLock(); }
  }
  
  /** Gets the next interaction in the history and replaces whatever is on the current interactions input line 
   *  with this interaction.
   */
  public boolean recallNextInteractionInHistory() {
    acquireWriteLock();
    try {    
      if (hasHistoryNext()) {
        moveHistoryNext(getCurrentInteraction());
        return true;
      }
      _beep.run();
      return false;
    }
    finally { releaseWriteLock(); }
  }
  

  /** Reverse searches the history for interactions that started with the current interaction. */
  public void reverseSearchInteractionsInHistory() {
    acquireWriteLock();
    try {   
      if (hasHistoryPrevious()) reverseHistorySearch(getCurrentInteraction());
      else _beep.run();
    }
    finally { releaseWriteLock(); }
  }
  
  /** Forward searches the history for interactions that started with the current interaction. */
  public void forwardSearchInteractionsInHistory() {
    acquireWriteLock();
    try {   
      if (hasHistoryNext()) forwardHistorySearch(getCurrentInteraction());
      else _beep.run();
    }
    finally { releaseWriteLock(); }
  }
  
  /** Inserts the given exception data into the document with the given style.
   *  @param exceptionClass Name of the exception that was thrown
   *  @param message Message contained in the exception
   *  @param stackTrace String representation of the stack trace
   *  @param styleName name of the style for formatting the exception
   */
  public void appendExceptionResult(String exceptionClass, String message, String stackTrace, String styleName) {
    // TODO: should probably log this error, or figure out what causes it
    // it does not seem to affect the program negatively, though
    if (message != null && (message.equals("Connection refused to host: 127.0.0.1; nested exception is: \n" +
                                           "\tjava.net.ConnectException: Connection refused: connect"))) return;

    if (null == message || "null".equals(message)) message = "";
    
    // Simplify the common error messages
    if ("koala.dynamicjava.interpreter.error.ExecutionError".equals(exceptionClass) ||
        "edu.rice.cs.drjava.model.repl.InteractionsException".equals(exceptionClass)) {
      exceptionClass = "Error";
    }
    
    // The following is an ugly hack that should be fixed ASAP.  The read/writelock methods need to be added to
    // the EditDocumentInterface interface.  This cast and a similar one in ConsoleDocument must be removed because they
    // defeat the purpose of the EditDocumentInterface interface.
    
    String c = exceptionClass;
    if (c.indexOf('.') != -1) c = c.substring(c.lastIndexOf('.') + 1, c.length());
    
    acquireWriteLock();
    try {
      append(c + ": " + message + "\n", styleName);
      
      // An example stack trace:
      //
      // java.lang.IllegalMonitorStateException:
      // at java.lang.Object.wait(Native Method)
      // at java.lang.Object.wait(Object.java:425)
      if (! stackTrace.trim().equals("")) {
        BufferedReader reader = new BufferedReader(new StringReader(stackTrace));
        
        String line;
        // a line is parsable if it has ( then : then ), with some
        // text between each of those
        while ((line = reader.readLine()) != null) {
          String fileName;
          int lineNumber;
          
          // TODO:  Why is this stuff here??
          int openLoc = line.indexOf('(');
          if (openLoc != -1) {
            int closeLoc = line.indexOf(')', openLoc + 1);
            
            if (closeLoc != -1) {
              int colonLoc = line.indexOf(':', openLoc + 1);
              if ((colonLoc > openLoc) && (colonLoc < closeLoc)) {
                // ok this line is parsable!
                String lineNumStr = line.substring(colonLoc + 1, closeLoc);
                try {
                  lineNumber = Integer.parseInt(lineNumStr);
                  fileName = line.substring(openLoc + 1, colonLoc);
                }
                catch (NumberFormatException nfe) {
                  // do nothing; we failed at parsing
                }
              }
            }
          }
          
          append(line, styleName);
          
          //JOptionPane.showMessageDialog(null, "\\n");
          append("\n", styleName);
          
        } // end the while
      }
    }
    catch (IOException ioe) { throw new UnexpectedException(ioe); }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
    finally { releaseWriteLock(); }
  }  

  public void appendSyntaxErrorResult(String message, String interaction, int startRow, int startCol,
                                      int endRow, int endCol, String styleName) {
    try {
      if (null == message || "null".equals(message))  message = "";
      
      if (message.indexOf("Lexical error") != -1) {
        int i = message.lastIndexOf(':');
        if (i != -1) message = "Syntax Error:" + message.substring(i+2, message.length());                                
      }
      
      if (message.indexOf("Error") == -1) message = "Error: " + message;
      
      append(message + "\n" , styleName);
    }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
  }

  /** Clears the current input text and then moves to the end of the command history. */
  public void clearCurrentInteraction() {
    acquireWriteLock();
    try {
      super.clearCurrentInput();
      _history.moveEnd();
    }
    finally { releaseWriteLock(); }
  }  

  /** Returns the string that the user has entered at the current prompt. Forwards to getCurrentInput(). */
  public String getCurrentInteraction() { return getCurrentInput(); }
  
  public String getDefaultStyle() { return InteractionsDocument.DEFAULT_STYLE; }
  
  /** This method tells the document to prepare all the DrJavaBook and PagePrinter objects. */
  public void preparePrintJob() {
    _book = new DrJavaBook(getDocText(0, getLength()), "Interactions", new PageFormat());
  }
  
  /* Only used for testing. */
  protected History getHistory() { return _history; }
}
