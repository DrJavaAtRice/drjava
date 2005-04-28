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

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.config.OptionListener;

/** Toolkit-independent document that provides console-like interaction with a Java interpreter.
 *  @version $Id$
 */
public class InteractionsDocument extends ConsoleDocument {

  /** Default banner. */
  public static final String DEFAULT_BANNER = "Welcome to DrJava.\n";

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

  /** String to print when the document is reset. Defaults to "Welcome to DrJava." */
  private String _banner;


  /** Reset the document on startup.  Uses a history with configurable size.
   *  @param document DocumentAdapter to use for the model
   */
  public InteractionsDocument(DocumentAdapter document) { this(document, new History()); }

  /** Reset the document on startup.  Uses a history with the given
   *  maximum size.  This history will not use the config framework.
   *  @param document DocumentAdapter to use for the model
   *  @param maxHistorySize Number of commands to remember in the history
   */
  public InteractionsDocument(DocumentAdapter document, int maxHistorySize) {
    this(document, new History(maxHistorySize));
  }

  /** Reset the document on startup.  Uses the given history.
   *  @param document DocumentAdapter to use for the model
   *  @param history History of commands
   */
  public InteractionsDocument(DocumentAdapter document, History history) {
    super(document);
    _history = history;
    _hasPrompt = true;
    _banner = DEFAULT_BANNER;
    _prompt = DEFAULT_PROMPT;

    reset();
  }

  /** Accessor for the banner, which is printed when the document resets. */
  public String getBanner() {  return _banner; }

  /** Sets the string to use for the banner when the document resets.
   *  @param banner String to be printed when the document resets.
   */
  public void setBanner(String banner) { _banner = banner; }

  /** Lets this document know whether an interaction is in progress.
   *  @param inProgress whether an interaction is in progress
   */
  public void setInProgress(boolean inProgress) { _hasPrompt = !inProgress; }

  /** Returns whether an interaction is currently in progress. */
  public boolean inProgress() { return !_hasPrompt; }

  /** Resets the document to a clean state.  Does not reset the history. */
  public synchronized void reset() {
    try {
      forceRemoveText(0, _document.getDocLength());
      forceInsertText(0, _banner, OBJECT_RETURN_STYLE);
      insertPrompt();
      _history.moveEnd();
      setInProgress(false);
    }
    catch (DocumentAdapterException e) { throw new UnexpectedException(e); }
  }

  /** Replaces any text entered past the prompt with the current item in the history. */
  private void _replaceCurrentLineFromHistory() {
    try {
      _clearCurrentInputText();
      insertText(getDocLength(), _history.getCurrent(), DEFAULT_STYLE);
    }
    catch (DocumentAdapterException ble) { throw new UnexpectedException(ble); }
  }

  /** Accessor method for the history of commands. */
  public OptionListener<Integer> getHistoryOptionListener() { return _history.getHistoryOptionListener(); }

  /** Adds the given text to the history of commands. */
  public synchronized void addToHistory(String text) { _history.add(text); }

  /** Saves the unedited version of the current history to a file
   *  @param selector File to save to
   */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _history.writeToFile(selector);
  }

  /**
   * Saves the edited version of the current history to a file
   * @param selector File to save to
   * @param editedVersion Edited verison of the history which will be
   * saved to file instead of the lines saved in the history. The saved
   * file will still include any tags needed to recognize it as a saved
   * interactions file.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
    _history.writeToFile(selector, editedVersion);
  }

  /** Returns the entire history as a single string.  Commands should be separated by semicolons. If an entire
   *  command does not end in a semicolon, one is added.
   */
  public String getHistoryAsStringWithSemicolons() {
      return _history.getHistoryAsStringWithSemicolons();
  }

  /** Returns the entire history as a single string.  Commands should be separated by semicolons. */
  public String getHistoryAsString() { return _history.getHistoryAsString(); }

  /** Clears the history */
  public void clearHistory() { _history.clear();  }
  
  public String lastEntry() { return _history.lastEntry(); }  // may throw a RuntimeException if no such entry

  /** Puts the previous line from the history on the current line and moves the history back one line.
   *  @param entry the current entry (perhaps edited from what is in history)
   */
  public synchronized void moveHistoryPrevious(String entry) {
    _history.movePrevious(entry);
    _replaceCurrentLineFromHistory();
  }

  /** Puts the next line from the history on the current line and moves the history forward one line.
   *  @param entry the current entry (perhaps edited from what is in history)
   */
  public synchronized void moveHistoryNext(String entry) {
    _history.moveNext(entry);
    _replaceCurrentLineFromHistory();
  }

  /** Returns whether there is a previous command in the history. */
  public boolean hasHistoryPrevious() { return  _history.hasPrevious(); }

  /** Returns whether there is a next command in the history. */
  public boolean hasHistoryNext() { return _history.hasNext(); }

  /** Reverse searches the history for the given string.
   *  @param searchString the string to search for
   */
  public synchronized void reverseHistorySearch(String searchString) {
    _history.reverseSearch(searchString);
    _replaceCurrentLineFromHistory();
  }

  /** Forward searches the history for the given string.
   *  @param searchString the string to search for
   */
  public synchronized void forwardHistorySearch(String searchString) {
    _history.forwardSearch(searchString);
    _replaceCurrentLineFromHistory();
  }

  /** Gets the previous interaction in the history and replaces whatever is on the current interactions input
   *  line with this interaction.
   */
  public synchronized boolean recallPreviousInteractionInHistory() {
    if (hasHistoryPrevious()) {
      moveHistoryPrevious(getCurrentInteraction());
      return true;
    }
    _beep.run();
    return false;
  }

  /** Gets the next interaction in the history and replaces whatever is on the current interactions input line 
   *  with this interaction.
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
  public synchronized void reverseSearchInteractionsInHistory() {
    if (hasHistoryPrevious()) reverseHistorySearch(getCurrentInteraction());
    else _beep.run();
  }

  /** Forward searches the history for interactions that started with the current interaction. */
  public void forwardSearchInteractionsInHistory() {
    if (hasHistoryNext()) forwardHistorySearch(getCurrentInteraction());
    else _beep.run();
  }

  /** Inserts the given exception data into the document with the given style.
   *  @param exceptionClass Name of the exception that was thrown
   *  @param message Message contained in the exception
   *  @param stackTrace String representation of the stack trace
   *  @param styleName name of the style for formatting the exception
   */
  public synchronized void appendExceptionResult(String exceptionClass, String message, String stackTrace,
                                                 String styleName) {
    try {
      if (null == message || "null".equals(message)) message = "";
      
      // Simplify the common error messages
      if ("koala.dynamicjava.interpreter.error.ExecutionError".equals(exceptionClass) ||
          "edu.rice.cs.drjava.model.repl.InteractionsException".equals(exceptionClass)) {
        exceptionClass = "Error";
      }

      String c = exceptionClass;
      if (c.indexOf('.') != -1) c = c.substring(c.lastIndexOf('.') + 1, c.length());      
      insertText(getDocLength(), c + ": " + message + "\n", styleName);

      // An example stack trace:
      //
      // java.lang.IllegalMonitorStateException:
      // at java.lang.Object.wait(Native Method)
      // at java.lang.Object.wait(Object.java:425)
      if (! stackTrace.trim().equals("")) {
        BufferedReader reader=new BufferedReader(new StringReader(stackTrace));

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

          insertText(getDocLength(), line, styleName);

          // OK, now if fileName != null we did parse out fileName
          // and lineNumber.
          // Here's where we'd add the button, etc.
          /*
          if (fileName != null) {
            JButton button = new JButton("go");
            button.addActionListener(new ExceptionButtonListener(fileName, lineNumber));
            SimpleAttributeSet buttonSet = new SimpleAttributeSet(set);
            StyleConstants.setComponent(buttonSet, button);
            insertString(getDocLength(), "  ", null);
            insertString(getDocLength() - 1, " ", buttonSet);
            JOptionPane.showMessageDialog(null, "button in");
            insertString(getDocLength(), " ", null);
            JOptionPane.showMessageDialog(null, "extra space");
          }*/

          //JOptionPane.showMessageDialog(null, "\\n");
          insertText(getDocLength(), "\n", styleName);

        } // end the while
      }
    }
    catch (IOException ioe) {
      // won't happen; we're readLine'ing from a String!
      throw new UnexpectedException(ioe);
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
  }

  public void appendSyntaxErrorResult(String message, String interaction, int startRow, int startCol,
                                      int endRow, int endCol, String styleName) {
    try {
      if (null == message || "null".equals(message))  message = "";
      
      if (message.indexOf("Lexical error") != -1) {
        int i = message.lastIndexOf(':');
        if (i != -1) message = "Syntax Error:" + message.substring(i+2,message.length());                                
      }
      
      if (message.indexOf("Error") == -1) message = "Error: " + message;
      
      insertText(getDocLength(), message + "\n" , styleName );
    }
    catch (DocumentAdapterException ble) { throw new UnexpectedException(ble); }
  }

  /** Clears the current input text and then moves to the end of the command history. */
  public synchronized void clearCurrentInteraction() {
    super.clearCurrentInput();
    _history.moveEnd();
  }

  /** Returns the string that the user has entered at the current prompt. Forwards to getCurrentInput(). */
  public synchronized String getCurrentInteraction() {
    return super.getCurrentInput();
  }
  
  /* Only used for testing. */
  protected History getHistory() { return _history; }
}
