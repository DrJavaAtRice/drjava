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
 * @version $Id$
 */
public class ConsoleDocument implements DocumentAdapter {
  /**
   * Default text style.
   */
  public static final String DEFAULT_STYLE = "default";

  /**
   * Style for System.out
   */
  public static final String SYSTEM_OUT_STYLE = "System.out";

  /**
   * Style for System.err
   */
  public static final String SYSTEM_ERR_STYLE = "System.err";

  /**
   * The default prompt to use in the console.
   */
  public static final String DEFAULT_CONSOLE_PROMPT = "";

  /**
   * The document storing the text for this console model.
   */
  protected DocumentAdapter _document;
  
  /**
   * A runnable command to use for a notification beep.
   */
  protected Runnable _beep;
  
  /** 
   * Index in the document of the first place that is editable.
   */
  protected int _promptPos;
  
  /**
   * String to use for the prompt.
   */
  protected String _prompt;

  /**
   * Whether the document currently has a prompt and is ready to accept input.
   */
  protected boolean _hasPrompt;
  
  /**
   * Creates a new ConsoleDocument with the given DocumentAdapter.
   * @param adapter the DocumentAdapter to use
   */
  public ConsoleDocument(DocumentAdapter adapter) {
    _document = adapter;

    _beep = new Runnable() {
      public void run() {}
    };
    _promptPos = 0;
    _prompt = DEFAULT_CONSOLE_PROMPT;
    _hasPrompt = false;

    // Prevent any edits before the prompt!
    _document.setEditCondition(new InteractionsEditCondition());
  }

  /**
   * @return true iff this document has a prompt and is ready to accept input.
   */
  public boolean hasPrompt() {
    return _hasPrompt;
  }

  /**
   * Accessor for the string used for the prompt.
   */
  public String getPrompt() {
    return _prompt;
  }
  
  /**
   * Sets the string to use for the prompt.
   * @param prompt String to use for the prompt.
   */
  public void setPrompt(String prompt) {
    _prompt = prompt;
  }
  
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
   * Sets a runnable action to use as a beep.
   * @param beep Runnable beep command
   */
  public void setBeep(Runnable beep) {
    _beep = beep;
  }
  
  /** 
   * Resets the document to a clean state.
   */
  public void reset() {
    try {
      forceRemoveText(0, _document.getDocLength());
      _promptPos = 0;
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Prints a prompt for a new input.
   */
  public void insertPrompt() {
    try {
      _promptPos = _document.getDocLength() + _prompt.length();
      _hasPrompt = true;
      forceInsertText(_document.getDocLength(), _prompt, DEFAULT_STYLE);
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Disables the prompt in this document.
   */
  public void disablePrompt() {
    _hasPrompt = false;
    _promptPos = _document.getDocLength();
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
      String newLine = System.getProperty("line.separator");
      insertText(pos, newLine, DEFAULT_STYLE);
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
      if (_hasPrompt) {
        pos = _promptPos - _prompt.length();
      }
      else {
        pos = getDocLength();
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
  public String getCurrentInput() {
    try {
      return getDocText(_promptPos, getDocLength() - _promptPos);
    }
    catch (DocumentAdapterException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Clears the current input text.
   */
  public void clearCurrentInput() {
    _clearCurrentInputText();
  }

  /**
   * Removes the text from the current prompt to the end of the document.
   */
  protected void _clearCurrentInputText() {
    try {
      // Delete old value of current line
      removeText(_promptPos, getDocLength() - _promptPos);
    }
    catch (DocumentAdapterException ble) {
      throw new UnexpectedException(ble);
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