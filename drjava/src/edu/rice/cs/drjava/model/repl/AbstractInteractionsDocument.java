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

import java.util.*;
import java.io.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.model.FileSaveSelector;

/**
 * Implementation of most of the InteractionsDocument interface,
 * supporting a full history of commands.  Concrete subclasses must
 * implement interpretCurrentInteraction() with an appropriate
 * interpreter.
 * @version $Id$
 */
public abstract class AbstractInteractionsDocument
  implements InteractionsDocument
{
  /**
   * The document storing the text for this interactions model.
   */
  protected DocumentAdapter _document;
  
  /**
   * Whether the interpreter is currently interpreting an interaction
   */
  protected boolean _inProgress = false;
  
  /**
   * A runnable command to use for a notification beep.
   */
  protected Runnable _beep = new Runnable() {
    public void run() {}
  };

  /** 
   * Index in the document of the first place that is editable.
   */
  protected int _promptPos = 0;

  /**
   * Command-line history. It's not reset when the interpreter is reset.
   */
  protected History _history;

  /**
   * Reset the document on startup.  Uses a history with configurable size.
   * @param document DocumentAdapter to use for the model
   */
  public AbstractInteractionsDocument(DocumentAdapter document) {
    this(document, new History());
  }
  
  /**
   * Reset the document on startup.  Uses a history with the given
   * maximum size.  This history will not use the config framework.
   * @param document DocumentAdapter to use for the model
   * @param maxHistorySize Number of commands to remember in the history
   */
  public AbstractInteractionsDocument(DocumentAdapter document,
                                      int maxHistorySize) {
    this(document, new History(maxHistorySize));
  }
  
  
  
  /**
   * Reset the document on startup.  Uses the given history.
   * @param document DocumentAdapter to use for the model
   * @param history History of commands
   */
  public AbstractInteractionsDocument(DocumentAdapter document,
                                      History history) {
    _document = document;
    _history = history;
    
    // Prevent any edits before the prompt!
    _document.setEditCondition(new InteractionsEditCondition());
    
    reset();
  }
  
  
  
  /**
   * Interprets the current command at the prompt.
   */
  public abstract void interpretCurrentInteraction();
  
  /**
   * Gets the object which can determine whether an insert
   * or remove edit should be applied, based on the inputs.
   * @param condition Object to determine legality of inputs
   */
  public DocumentEditCondition getEditCondition() {
    return _document.getEditCondition();
  }
  
  /**
   * Provides an object which can determine whether an insert
   * or remove edit should be applied, based on the inputs.
   * @param condition Object to determine legality of inputs
   */
  public void setEditCondition(DocumentEditCondition condition) {
    _document.setEditCondition(condition);
  }

  /**
   * Returns the first location in the document where editing is allowed.
   */
  public int getPromptPos() {
    return _promptPos;
  }

  /**
   * Lets this document know whether an interaction is in progress.
   * @param inProgress whether an interaction is in progress
   */
  public void setInProgress(boolean inProgress) {
    _inProgress = inProgress;
  }

  /**
   * Returns whether an interaction is currently in progress.
   */
  public boolean inProgress() {
    return _inProgress;
  }
  
  /**
   * Sets a runnable action to use as a beep.
   * @param beep Runnable beep command
   */
  public void setBeep(Runnable beep) {
    _beep = beep;
  }
  
  /** 
   * Resets the document to a clean state.  Does not reset the history.
   */
  public void reset() {
    try {
      forceRemoveText(0, _document.getDocLength());
      forceInsertText(0, BANNER, DEFAULT_STYLE);
      insertPrompt();
      _history.moveEnd();
      setInProgress(false);
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Prints a prompt for a new interaction.
   */
  public void insertPrompt() {
    try {
      forceInsertText(_document.getDocLength(), PROMPT, DEFAULT_STYLE);
      _promptPos = _document.getDocLength();
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Inserts a new line at the given position.
   * @param pos Position to insert the new line
   */
  public void insertNewLine(int pos) {
    // Correct the position if necessary
    if (pos > getDocLength()) {
      pos = getDocLength();
    }
    else if (pos < 0) {
      pos = 0;
    }
    
    try {
      insertText(pos, "\n", DEFAULT_STYLE);
    }
    catch (DocumentAdapterException e) {
      // Shouldn't happen after we've corrected it
      throw new UnexpectedException(e);
    }
  }

  /**
   * Inserts the given string with the given attributes just before the
   * most recent prompt.
   * @param text String to insert
   * @param style name of style to format the string
   */
  public void insertBeforeLastPrompt(String text, String style) {
    try {
      int pos;
      if (_inProgress) {
        pos = getDocLength();
      }
      else {
        pos = _promptPos - PROMPT.length();
      }

      _promptPos += text.length();
      _document.forceInsertText(pos, text, style);
      
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
  }
  
  /**
   * Inserts a string into the document at the given offset
   * and the given named style, if the edit condition allows it.
   * @param offs Offset into the document
   * @param str String to be inserted
   * @param style Name of the style to use.  Must have been
   * added using addStyle.
   * @throws DocumentAdapterException if the offset is illegal
   */
  public void insertText(int offs, String str, String style)
    throws DocumentAdapterException
  {
    if (offs < _promptPos) {
      _beep.run();
    }
    else {
      _document.insertText(offs, str, style);
    }
  }
  
  /**
   * Inserts a string into the document at the given offset
   * and the given named style, regardless of the edit condition.
   * @param offs Offset into the document
   * @param str String to be inserted
   * @param style Name of the style to use.  Must have been
   * added using addStyle.
   * @throws DocumentAdapterException if the offset is illegal
   */
  public void forceInsertText(int offs, String str, String style)
    throws DocumentAdapterException
  {
    _document.forceInsertText(offs, str, style);
  }

  /**
   * Removes a portion of the document, if the edit condition allows it.
   * @param offs Offset to start deleting from
   * @param len Number of characters to remove
   * @throws DocumentAdapterException if the offset or length are illegal
   */
  public void removeText(int offs, int len) throws DocumentAdapterException {
    if (offs < _promptPos) {
      _beep.run();
    } 
    else {
      _document.removeText(offs, len);
    }
  }
  
  /**
   * Removes a portion of the document, regardless of the edit condition.
   * @param offs Offset to start deleting from
   * @param len Number of characters to remove
   * @throws DocumentAdapterException if the offset or length are illegal
   */
  public void forceRemoveText(int offs, int len) throws DocumentAdapterException {
    _document.forceRemoveText(offs, len);
  }
  
  /**
   * Returns the length of the document.
   */
  public int getDocLength() {
    return _document.getDocLength();
  }
  
  /**
   * Returns a portion of the document.
   * @param offs First offset of the desired text
   * @param len Number of characters to return
   * @throws DocumentAdapterException if the offset or length are illegal
   */
  public String getDocText(int offs, int len) throws DocumentAdapterException {
    return _document.getDocText(offs, len);
  }
  
  /**
   * Returns the string that the user has entered at the current prompt.
   * May contain newline characters.
   */
  public String getCurrentInteraction() {
    try {
      return getDocText(_promptPos, getDocLength() - _promptPos);
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Clears the current interaction text and then moves
   * to the end of the command history.
   */
  public void clearCurrentInteraction() {
    _clearCurrentInteractionText();
    _history.moveEnd();
  }

  /**
   * Removes the text from the current prompt to the end of the document.
   */
  protected void _clearCurrentInteractionText() {
    try {
      // Delete old value of current line
      removeText(_promptPos, getDocLength() - _promptPos);
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
  }
  
  /**
   * Replaces any text entered past the prompt with the current
   * item in the history.
   */
  protected void _replaceCurrentLineFromHistory() {
    try {
      _clearCurrentInteractionText();
      insertText(getDocLength(), _history.getCurrent(), DEFAULT_STYLE);
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
  }
  
  
  /**
   * Adds the given text to the history of commands.
   */
  public void addToHistory(String text) {
    _history.add(text);
  }
  
  /**
   * Saves the interactions history (or an edited history) with the given
   * file selector.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion) throws IOException {
    _history.writeToFile(selector, editedVersion);
  }

  /**
   * Returns the entire history as a single string.  Commands should
   * be separated by semicolons. If an entire command does not end in a
   * semicolon, one is added.
   */
  public String getHistoryAsStringWithSemicolons() {
      return _history.getHistoryAsStringWithSemicolons();
  }

  /**
   * Returns the entire history as a single string.  Commands should
   * be separated by semicolons.
   */
  public String getHistoryAsString() {
    return _history.getHistoryAsString();
  }

  /**
   * Clears the history
   */
  public void clearHistory() {
    _history.clear();
  }

  /**
   * Puts the previous line from the history on the current line
   * and moves the history back one line.
   */
  public void moveHistoryPrevious() {
    _history.movePrevious();
    _replaceCurrentLineFromHistory();
  }

  /**
   * Puts the next line from the history on the current line
   * and moves the history forward one line.
   */
  public void moveHistoryNext() {
    _history.moveNext();
    _replaceCurrentLineFromHistory();
  }

  /**
   * Returns whether there is a previous command in the history.
   */
  public boolean hasHistoryPrevious() {
    return  _history.hasPrevious();
  }

  /**
   * Returns whether there is a next command in the history.
   */
  public boolean hasHistoryNext() {
    return _history.hasNext();
  }
  
  /**
   * Gets the previous interaction in the history and
   * replaces whatever is on the current
   * interactions input line with this interaction.
   */
  public void recallPreviousInteractionInHistory(Runnable failed) {
    if (hasHistoryPrevious()) {
      moveHistoryPrevious();
    }
    else {
      failed.run();
    }
  }

  /**
   * Gets the next interaction in the history and
   * replaces whatever is on the current
   * interactions input line with this interaction.
   */
  public void recallNextInteractionInHistory(Runnable failed) {
    if (hasHistoryNext()) {
      moveHistoryNext();
    }
    else {
      failed.run();
    }
  }

  /**
   * Inserts the given exception data into the document with the given style.
   * @param exceptionClass Name of the exception that was thrown
   * @param message Message contained in the exception
   * @param stackTrace String representation of the stack trace
   * @param styleName name of the style for formatting the exception
   */
  public void appendExceptionResult(String exceptionClass,
                                    String message,
                                    String stackTrace,
                                    String styleName)
  {
    //writeLock();
    try {

      if (null == message || "null".equals(message)) {
        message = "";
      }

      insertText(getDocLength(), 
                 exceptionClass + ": " + message + "\n", styleName);

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
          String fileName = null;
          int lineNumber = -1;

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
          if (fileName != null) {
            /*
            JButton button = new JButton("go");
            button.addActionListener(new ExceptionButtonListener(fileName,
                                                                 lineNumber));

            SimpleAttributeSet buttonSet = new SimpleAttributeSet(set);
            StyleConstants.setComponent(buttonSet, button);
            insertString(getDocLength(), "  ", null);
            insertString(getDocLength() - 1, " ", buttonSet);
            */
            //JOptionPane.showMessageDialog(null, "button in");
            //insertString(getDocLength(), " ", null);
            //JOptionPane.showMessageDialog(null, "extra space");
          }

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
    finally {
      //writeUnlock();
    }
  }
  
  /**
   * Class to ensure that any attempt to edit the document
   * above the prompt is rejected.
   */
  class InteractionsEditCondition extends DocumentEditCondition {
    public boolean canInsertText(int offs, String str, String style) {
      if (offs < getPromptPos()) {
        _beep.run();
        return false;
      }
      else {
        return true;
      }
    }
    public boolean canRemoveText(int offs, int len) {
      if (offs < getPromptPos()) {
        _beep.run();
        return false;
      }
      else {
        return true;
      }
    }
  }
}
