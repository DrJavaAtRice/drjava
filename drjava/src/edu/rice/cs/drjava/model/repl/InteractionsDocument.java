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
 * Toolkit-independent document that provides console-like interaction
 * with a Java interpreter.
 * @version $Id$
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
  
  /**
   * String to print when the document is reset.
   * Defaults to "Welcome to DrJava."
   */
  protected String _banner;
  
  /**
   * Command-line history. It's not reset when the interpreter is reset.
   */
  protected History _history;

  /**
   * Reset the document on startup.  Uses a history with configurable size.
   * @param document DocumentAdapter to use for the model
   */
  public InteractionsDocument(DocumentAdapter document) {
    this(document, new History());
  }
  
  /**
   * Reset the document on startup.  Uses a history with the given
   * maximum size.  This history will not use the config framework.
   * @param document DocumentAdapter to use for the model
   * @param maxHistorySize Number of commands to remember in the history
   */
  public InteractionsDocument(DocumentAdapter document, int maxHistorySize) {
    this(document, new History(maxHistorySize));
  }

  /**
   * Reset the document on startup.  Uses the given history.
   * @param document DocumentAdapter to use for the model
   * @param history History of commands
   */
  public InteractionsDocument(DocumentAdapter document, History history) {
    super(document);
    _history = history;
    
    _hasPrompt = true;
    _banner = DEFAULT_BANNER;
    _prompt = DEFAULT_PROMPT;
    
    reset();
  }
  
  
  /**
   * Accessor for the banner, which is printed when the document resets.
   */
  public String getBanner() {
    return _banner;
  }
  
  /**
   * Sets the string to use for the banner when the document resets.
   * @param banner String to be printed when the document resets.
   */
  public void setBanner(String banner) {
    _banner = banner;
  }
  
  /**
   * Lets this document know whether an interaction is in progress.
   * @param inProgress whether an interaction is in progress
   */
  public void setInProgress(boolean inProgress) {
    _hasPrompt = !inProgress;
  }

  /**
   * Returns whether an interaction is currently in progress.
   */
  public boolean inProgress() {
    return !_hasPrompt;
  }
  
  /** 
   * Resets the document to a clean state.  Does not reset the history.
   */
  public void reset() {
    try {
      forceRemoveText(0, _document.getDocLength());
      forceInsertText(0, _banner, DEFAULT_STYLE);
      insertPrompt();
      _history.moveEnd();
      setInProgress(false);
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Replaces any text entered past the prompt with the current
   * item in the history.
   */
  protected void _replaceCurrentLineFromHistory() {
    try {
      _clearCurrentInputText();
      insertText(getDocLength(), _history.getCurrent(), DEFAULT_STYLE);
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
  }
  
  /**
   * Accessor method for the history of commands.
   */
  public History getHistory() {
    return _history;
  }
  
  /**
   * Adds the given text to the history of commands.
   */
  public void addToHistory(String text) {
    _history.add(text);
  }
  
  /**
   * Saves the unedited version of the current history to a file
   * @param selector File to save to
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
  public void saveHistory(FileSaveSelector selector, String editedVersion)
    throws IOException
  {
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
  public void recallPreviousInteractionInHistory() {
    if (hasHistoryPrevious()) {
      moveHistoryPrevious();
    }
    else {
      _beep.run();
    }
  }

  /**
   * Gets the next interaction in the history and
   * replaces whatever is on the current
   * interactions input line with this interaction.
   */
  public void recallNextInteractionInHistory() {
    if (hasHistoryNext()) {
      moveHistoryNext();
    }
    else {
      _beep.run();
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

      // Simplify the common error messages
      if ("koala.dynamicjava.interpreter.error.ExecutionError".equals(exceptionClass) ||
          "edu.rice.cs.drjava.model.repl.InteractionsException".equals(exceptionClass)) {
        exceptionClass = "Error";
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
  
  public void appendSyntaxErrorResult(String message,
                                      int startRow,
                                      int startCol,
                                      int endRow,
                                      int endCol, 
                                      String styleName )
  {
    //writeLock();
    try {

      if (null == message || "null".equals(message)) {
        message = "";
      }
      
      
      
     insertText( getDocLength(), message + "\n" , styleName );
    }

    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
    }
    finally {
      //writeUnlock();
    }
  }

  /**
   * Clears the current input text and then moves
   * to the end of the command history.
   */
  public void clearCurrentInteraction() {
    super.clearCurrentInput();
    _history.moveEnd();
  }

  /**
   * Returns the string that the user has entered at the current prompt.
   * Forwards to getCurrentInput()
   */
  public String getCurrentInteraction() {
    return super.getCurrentInput();
  }
}
