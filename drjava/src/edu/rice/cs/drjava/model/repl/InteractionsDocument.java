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

import java.io.IOException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.drjava.model.FileSaveSelector;

/**
 * Interface for a document that handles input to the repl and the 
 * interpretation of this input.
 * @version $Id$
 */
public interface InteractionsDocument extends DocumentAdapter {
  public static final String BANNER = "Welcome to DrJava.\n";
  public static final String PROMPT = "> ";
  
  /** Default text style. */
  public static final String DEFAULT_STYLE = "default";
  
  /** Style for System.out */
  public static final String SYSTEM_OUT_STYLE = "System.out";
  
  /** Style for System.err */
  public static final String SYSTEM_ERR_STYLE = "System.err";
  
  /** Style for error messages */
  public static final String ERROR_STYLE = "error";
  
  /** Style for debugger messages */
  public static final String DEBUGGER_STYLE = "debugger";

  /**
   * Interprets the current command at the prompt.
   */
  public void interpretCurrentInteraction();
  
  /**
   * Returns the first location in the document where editing is allowed.
   */
  public int getPromptPos();

  /**
   * Lets this document know whether an interaction is in progress.
   * @param inProgress Whether an interaction is in progress
   */
  public void setInProgress(boolean inProgress);

  /**
   * Returns whether an interaction is currently in progress.
   */
  public boolean inProgress();
  
  /**
   * Sets a runnable action to use as a beep.
   * @param beep Runnable beep command
   */
  public void setBeep(Runnable beep);
  
  /** 
   * Resets the document to a clean state.  Does not reset the history.
   */
  public void reset();

  /**
   * Prints a prompt for a new interaction.
   */
  public void insertPrompt();
  
  /**
   * Inserts a new line at the given position.
   * @param pos Position to insert the new line
   */
  public void insertNewLine(int pos);

  /**
   * Inserts the given string with the given attributes just before the
   * most recent prompt.
   * @param text String to insert
   * @param style name of style to format the string
   */
  public void insertBeforeLastPrompt(String text, String style);
  
  
  /**
   * Returns the string that the user has entered at the current prompt.
   * May contain newline characters.
   */
  public String getCurrentInteraction();

  /**
   * Clears the current interaction text and then moves
   * to the end of the command history.
   */
  public void clearCurrentInteraction();
  
  /**
   * Adds the given text to the history of commands.
   */
  public void addToHistory(String text);
  
  /**
   * Saves the interactions history (or an edited history) with the given
   * file selector.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion) 
    throws IOException;

  /**
   * Returns the entire history as a single string.  Commands should
   * be separated by semicolons. If an entire command does not end in a
   * semicolon, one is added.
   */
  public String getHistoryAsStringWithSemicolons();

  /**
   * Returns the entire history as a single string.  Commands should
   * be separated by semicolons.
   */
  public String getHistoryAsString();
  
  /**
   * Clears the history
   */
  public void clearHistory();

  /**
   * Puts the previous line from the history on the current line
   * and moves the history back one line.
   */
  public void moveHistoryPrevious();

  /**
   * Puts the next line from the history on the current line
   * and moves the history forward one line.
   */
  public void moveHistoryNext();

  /**
   * Returns whether there is a previous command in the history.
   */
  public boolean hasHistoryPrevious();

  /**
   * Returns whether there is a next command in the history.
   */
  public boolean hasHistoryNext();
  
  /**
   * Gets the previous interaction in the history and
   * replaces whatever is on the current
   * interactions input line with this interaction.
   * @param failed Something to run if there is no previous command
   */
  public void recallPreviousInteractionInHistory(Runnable failed);

  /**
   * Gets the next interaction in the history and
   * replaces whatever is on the current
   * interactions input line with this interaction.
   * @param failed Something to run if there is no next command
   */
  public void recallNextInteractionInHistory(Runnable failed);

  

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
                                    String styleName);
}
