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

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapter;
import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.DocumentAdapterException;

/**
 * @version $Id$
 */
public class ConsoleDocument implements DocumentAdapter {
  
  /** Default text style.*/
  public static final String DEFAULT_STYLE = "default";

  /** Style for System.out */
  public static final String SYSTEM_OUT_STYLE = "System.out";

  /** Style for System.err */
  public static final String SYSTEM_ERR_STYLE = "System.err";

  /** Style for System.in */
  public static final String SYSTEM_IN_STYLE = "System.in";

  /** The default prompt to use in the console. */
  public static final String DEFAULT_CONSOLE_PROMPT = "";

  /** The document storing the text for this console model. */
  protected DocumentAdapter _document;

  /** A runnable command to use for a notification beep. */
  protected Runnable _beep;

  /** Index in the document of the first place that is editable. */
  protected int _promptPos;

  /** String to use for the prompt. */
  protected String _prompt;

  /** Whether the document currently has a prompt and is ready to accept input. */
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
   * @return the DocumentEditCondition to determine legality of inputs
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
   * Sets the prompt position.
   * @param newPos the new position.
   */
  public void setPromptPos(int newPos) {
    _promptPos = newPos;
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
   * Gets the position immediately before the prompt, or the doc length if
   * there is no prompt.
   */
  public int getPositionBeforePrompt() {
    if (_hasPrompt) {
      return _promptPos - _prompt.length();
    }
    else {
      return getDocLength();
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
      int pos = getPositionBeforePrompt();
      _promptPos += text.length();
      _addToStyleLists(pos,text,style);
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
      _addToStyleLists(offs,str,style);
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
    _addToStyleLists(offs,str,style);
    _document.forceInsertText(offs, str, style);
  }
  
  private void _addToStyleLists(int offs, String str, String style) {
    if(_document instanceof InteractionsDocumentAdapter) 
      ((InteractionsDocumentAdapter)_document).addColoring(offs,offs + str.length(),style);
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