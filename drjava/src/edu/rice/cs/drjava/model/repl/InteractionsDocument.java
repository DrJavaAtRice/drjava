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

import java.io.*;
import java.awt.print.*;

import edu.rice.cs.drjava.model.print.DrJavaBook;

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.text.ConsoleDocumentInterface;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.drjava.config.OptionListener;

/** A GUI toolkit-agnostic document that supports console-like interaction with a Java interpreter.
  * This class assumes that the embedded document supports readers/writers locking and uses that locking
  * protocol to ensure the integrity of the data added in this class 
  * @version $Id: InteractionsDocument.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class InteractionsDocument extends ConsoleDocument {
  
  /** Default prompt. */
  public static final String DEFAULT_PROMPT = "> ";

  /** continuation string for multiline expressions */
  public static final String CONTINUATION_STRING = "     | ";

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
    * @param document the edit document to use for the model
    */
  public InteractionsDocument(ConsoleDocumentInterface document) { 
    this(document, new History()); 
  }
  
  /** Reset the document on startUp.  Uses a history with the given maximum size.  This history will not use the config
    * framework.
    * @param document EditDocumentInterface to use for the model
    * @param maxHistorySize Number of commands to remember in the history
    */
  public InteractionsDocument(ConsoleDocumentInterface document, int maxHistorySize) {
    this(document, new History(maxHistorySize));
  }
  
  /** Creates and resets the interactions document on DrJava startUp.  Uses the given history.  
    * @param document EditDocumentInterface to use for the model
    * @param history History of commands
    */
  public InteractionsDocument(ConsoleDocumentInterface document, History history) {
    super(document);  // initializes _document = document;
    _history = history;
    _document.setHasPrompt(true);
    _prompt = DEFAULT_PROMPT;
    _continuationString = CONTINUATION_STRING;
  }
  
  /** Lets this document know whether an interaction is in progress.
    * @param inProgress whether an interaction is in progress
    */
  public void setInProgress(boolean inProgress) { _document.setHasPrompt(! inProgress); }
  
  /** Returns whether an interaction is currently in progress. */
  public boolean inProgress() { return ! _document.hasPrompt(); }
  
  /** Sets the banner in an empty docuemnt. */
  public void setBanner(String banner) {
    try {
      setPromptPos(0);
      insertText(0, banner, OBJECT_RETURN_STYLE);
      insertPrompt();
      _history.moveEnd();
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
  }
    
  /** Resets the document to a clean state.  Does not reset the history. */
  public void reset(String banner) {
    try {
//      System.err.println("Resetting the interactions document with banner '" + banner + "'");
      // Clear interactions document
      setHasPrompt(false);
      setPromptPos(0);
      removeText(0, _document.getLength());
      insertText(0, banner, OBJECT_RETURN_STYLE);
//      System.err.println("Inserting prompt in cleared interactions pane");
      insertPrompt();
      _history.moveEnd();
      setInProgress(false);  // redundant? also done in InteractionsDocument.interpreterReady(...)
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
  }
  
  /** Replaces any text entered past the prompt with the current item in the history. Only runs in event thread. */
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
  public void addToHistory(String text) { _history.add(text); }
  
  /** Returns the last history item and then removes it, or returns null if the history is empty. */
  public String removeLastFromHistory() { return _history.removeLast(); }
  
  /** Saves the unedited version of the current history to a file
    * @param selector File to save to
    */
  public void saveHistory(FileSaveSelector selector) throws IOException { _history.writeToFile(selector); }
  
  /** Saves the edited version of the current history to a file
    * @param selector File to save to
    * @param editedVersion Edited version of the history which will be
    * saved to file instead of the lines saved in the history. The saved
    * file will still include any tags needed to recognize it as a saved
    * interactions file.
    */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
    History.writeToFile(selector, editedVersion); 
  }
  
  /** Returns the entire history as a single string.  Commands should be separated by semicolons. If an entire
    * command does not end in a semicolon, one is added.
    */
  public String getHistoryAsStringWithSemicolons() {
    return _history.getHistoryAsStringWithSemicolons(); 
  }
  
  /** Returns the entire history as a single string.  Commands should be separated by semicolons. */
  public String getHistoryAsString() { 
    return _history.getHistoryAsString(); 
  }
  
  /** Clears the history */
  public void clearHistory() { _history.clear(); }
  
  public String lastEntry() { return _history.lastEntry(); }
  /** Puts the previous line from the history on the current line and moves the history back one line.
    * @param entry the current entry (perhaps edited from what is in history)
    */
  public void moveHistoryPrevious(String entry) {
    _history.movePrevious(entry);
    _replaceCurrentLineFromHistory();
  }
  
  /** Puts the next line from the history on the current line and moves the history forward one line.
    * @param entry the current entry (perhaps edited from what is in history)
    */
  public void moveHistoryNext(String entry) {
    _history.moveNext(entry);
    _replaceCurrentLineFromHistory();
  }
  
  /** Returns whether there is a previous command in the history.  Only runs in event thread. */
  private boolean hasHistoryPrevious() { return _history.hasPrevious(); }
  
  /** Returns whether there is a next command in the history.  Only runs in event thread. */
  public boolean hasHistoryNext() { return _history.hasNext(); }
  
  /** Reverse searches the history for the given string.
    * @param searchString the string to search for
    */
  public void reverseHistorySearch(String searchString) {
    _history.reverseSearch(searchString);
    _replaceCurrentLineFromHistory();
  }
  
  /** Forward searches the history for the given string.
    * @param searchString the string to search for
    */
  public void forwardHistorySearch(String searchString) {
    _history.forwardSearch(searchString);
    _replaceCurrentLineFromHistory();
  }
  
  /** Gets the previous interaction in the history and replaces whatever is on the current interactions input
    * line with this interaction.  Only runs in event thread.
    */
  public boolean recallPreviousInteractionInHistory() {   
    if (hasHistoryPrevious()) {
      moveHistoryPrevious(getCurrentInteraction());
      return true;
    }
    _beep.run();
    return false;
  }
  
  /** Gets the next interaction in the history and replaces whatever is on the current interactions input line 
    * with this interaction.
    */
  public boolean recallNextInteractionInHistory() {
    if (hasHistoryNext()) {
      moveHistoryNext(getCurrentInteraction());
      return true;
    }
    _beep.run();
    return false;
  }
  
  
  /** Reverse searches the history for interactions that started with the current interaction. */
  public void reverseSearchInteractionsInHistory() {
    if (hasHistoryPrevious()) reverseHistorySearch(getCurrentInteraction());
    else _beep.run();
  }
  
  /** Forward searches the history for interactions that started with the current interaction. */
  public void forwardSearchInteractionsInHistory() {
    if (hasHistoryNext()) forwardHistorySearch(getCurrentInteraction());
    else _beep.run();
  }
  
  /** Inserts the given exception data into the document with the given style.
    * @param message  Message contained in the exception
    * @param styleName  name of the style for formatting the exception
    */
  public void appendExceptionResult(String message, String styleName) {
    // Note that there is similar code in InteractionsDJDocument.  Something should be refactored.
    
    // TODO: should probably log this error, or figure out what causes it (mgricken)
    // it does not seem to affect the program negatively, though
    // I'm commenting out, just to see when it appears
//    if (message != null && (message.equals("Connection refused to host: 127.0.0.1; nested exception is: \n" +
//                                           "\tjava.net.ConnectException: Connection refused: connect"))) return;
    try { append(message + "\n", styleName); }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
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
    super.clearCurrentInput();
    _history.moveEnd();
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
