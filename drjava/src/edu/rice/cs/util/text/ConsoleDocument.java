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
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.text;

import java.awt.print.*;

import edu.rice.cs.drjava.model.print.DrJavaBook;

import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.ConsoleDocumentInterface;
import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.EditDocumentException;

/** A GUI-toolkit agnostic interface to a console document.  This class assumes that the embedded document supports 
  * readers/writers locking and uses that locking protocol to ensure the integrity of the data added in this class
  * @version $Id$ */
public class ConsoleDocument implements ConsoleDocumentInterface {
  
  /** The default prompt to use in the console. */
  public static final String DEFAULT_CONSOLE_PROMPT = "";
  
  /** Default text style. */
  public static final String DEFAULT_STYLE = "default";

  /** Style for System.out */
  public static final String SYSTEM_OUT_STYLE = "System.out";

  /** Style for System.err */
  public static final String SYSTEM_ERR_STYLE = "System.err";

  /** Style for System.in */
  public static final String SYSTEM_IN_STYLE = "System.in";

  /** The embedded document storing the text and _hasPrompt property for this console model. */
  protected final ConsoleDocumentInterface _document;

  /** A runnable command to use for a notification beep. */
  protected volatile Runnable _beep;

  /** Index in the document of the first place that is editable. */
  private volatile int _promptPos;

  /** String to use for the prompt. */
  protected volatile String _prompt;

  /** The book object used for printing that represents several pages */
  protected volatile DrJavaBook _book;

  /** Creates a new ConsoleDocument with the given embedded ConsoleDocumentInterface (a SwingDocument in native DrJava).
   *  @param doc the embedded ConsoleDocumentInterface object
   */
  public ConsoleDocument(ConsoleDocumentInterface doc) {
    _document = doc;
    
    _beep = new Runnable() { public void run() { } };
    _prompt = DEFAULT_CONSOLE_PROMPT;
    _promptPos = DEFAULT_CONSOLE_PROMPT.length();
    _document.setHasPrompt(false);
   
    // Prevent any edits before the prompt!
    _document.setEditCondition(new ConsoleEditCondition());
  }

  /** @return true iff this document has a prompt and is ready to accept input. */
  public boolean hasPrompt() { return _document.hasPrompt(); }
  
  public void setHasPrompt(boolean val) {
    acquireWriteLock();    // Is this overkill?
    try { _document.setHasPrompt(val); }
    finally { releaseWriteLock(); }
  }

  /** Accessor for the string used for the prompt. */
  public String getPrompt() { return _prompt; }

  /** Sets the string to use for the prompt.
   *  @param prompt String to use for the prompt.
   */
  public void setPrompt(String prompt) { 
    acquireWriteLock();  
    _prompt = prompt;
    releaseWriteLock();
  }

  /** Gets the object which determines whether an insert/remove edit should be applied based on the inputs.
   *  @return the DocumentEditCondition to determine legality of inputs
   */
  public DocumentEditCondition getEditCondition() { return _document.getEditCondition(); }

  /** Provides an object which can determine whether an insert or remove edit should be applied, based on 
   *  the inputs.
   *  @param condition Object to determine legality of inputs
   */
  public void setEditCondition(DocumentEditCondition condition) { _document.setEditCondition(condition); }

  /** Returns the first location in the document where editing is allowed. */
  public int getPromptPos() { return _promptPos; }

  /** Sets the prompt position. Only used in tests.
   *  @param newPos the new position.
   */
  public void setPromptPos(int newPos) { 
    acquireWriteLock();
    _promptPos = newPos; 
    releaseWriteLock();
  }

  /** Sets a runnable action to use as a beep.
   *  @param beep Runnable beep command
   */
  public void setBeep(Runnable beep) { 
    acquireWriteLock();
    _beep = beep; 
    releaseWriteLock();
  }

  /** Resets the document to a clean state. */
  public void reset(String banner) {
    acquireWriteLock();
    try {
      forceRemoveText(0, _document.getLength());
      _forceInsertText(0, banner, DEFAULT_STYLE);
      _promptPos = banner.length();
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
    finally { releaseWriteLock(); }
  }

  /** Prints a prompt for a new input. */
  public void insertPrompt() {
    acquireWriteLock();
    try {
      int len = _document.getLength();
      // Update _promptPos before updating _document because insertText runs insertUpdate to adjust caret
      _promptPos = len + _prompt.length();
      _forceInsertText(len, _prompt, DEFAULT_STYLE); // need forceAppend!
      _document.setHasPrompt(true);
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e);  }
    finally { releaseWriteLock(); }
  }

  /** Disables the prompt in this document. */
  public void disablePrompt() {
    acquireWriteLock();
    try {
    _document.setHasPrompt(false);
    _promptPos = _document.getLength();
    }
    finally { releaseWriteLock(); }
  }
  
  /** Inserts a new line at the given position.
    * @param pos Position to insert the new line
    */
  public void insertNewLine(int pos) {
    // Correct the position if necessary
    acquireWriteLock();
    try {
      int len = _document.getLength();
      if (pos > len)  pos = len;
      else if (pos < 0) pos = 0;
      
      String newLine = "\n";  // Was StringOps.EOL; but Swing uses '\n' for newLine
      insertText(pos, newLine, DEFAULT_STYLE);
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
    finally { releaseWriteLock(); }
  }

  /** Gets the position immediately before the prompt, or the doc length if there is no prompt. Assumes that ReadLock or
    * WriteLock is already held.*/
  private int _getPositionBeforePrompt() {
    int len = _document.getLength();
    if (_document.hasPrompt()) {
      int promptStart = _promptPos - _prompt.length();
      return (promptStart < len && promptStart >= 0) ? promptStart : len;  // ensure position is within document 
    }
    return len;
  }

  /** Inserts the given string with the given attributes just before the most recent prompt.
    * @param text String to insert
    * @param style name of style to format the string
    */
  public void insertBeforeLastPrompt(String text, String style) {
    acquireWriteLock();
    try {
      int pos = _getPositionBeforePrompt();
      _promptPos = _promptPos + text.length();
      _forceInsertText(pos, text, style);
    }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
    finally { releaseWriteLock(); }
  }

  /** Inserts a string into the document at the given offset and named style, if the edit condition allows it.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void insertText(int offs, String str, String style) throws EditDocumentException {
    acquireWriteLock();
    try { _insertText(offs, str, style); }
    finally { releaseWriteLock(); }
  }
  
  /** Inserts a string into the document at the given offset and named style, if the edit condition allows it., as
    * above.  Assumes WriteLock is held. */
  public void _insertText(int offs, String str, String style) throws EditDocumentException {
    if (offs < _promptPos) _beep.run();
    else {
      _addToStyleLists(offs, str, style);
      _document.insertText(offs, str, style);
    }
  }   
  
  /** Appends a string to this in the given named style, if the edit condition allows it.
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void append(String str, String style) throws EditDocumentException {
    acquireWriteLock();
    try {
      int offs = _document.getLength();
      _addToStyleLists(offs, str, style);
      _document._insertText(offs, str, style);
    }
    finally { releaseWriteLock(); }
  }
  
  /** Inserts a string into the document at the given offset and  style, regardless of the edit condition.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void forceInsertText(int offs, String str, String style) throws EditDocumentException {
    acquireWriteLock();
    try { _forceInsertText(offs, str, style); }
    finally { releaseWriteLock(); }
  }
  
  /** Inserts a string into the document exactly like forceInsertText above except that it assumes the WriteLock
    * is already held.
    */
  public void _forceInsertText(int offs, String str, String style) throws EditDocumentException {      
    _addToStyleLists(offs, str, style);
    _document._forceInsertText(offs, str, style);
  }
  
  private void _addToStyleLists(int offs, String str, String style) {
    if (_document instanceof SwingDocument) 
      ((SwingDocument)_document).addColoring(offs, offs + str.length(), style);
  }
  
  /** Removes a portion of the document, if the edit condition (including promptPos) allows it.
   *  @param offs Offset to start deleting from
   *  @param len Number of characters to remove
   *  @throws EditDocumentException if the offset or length are illegal
   */
  public void removeText(int offs, int len) throws EditDocumentException {
    acquireWriteLock();
    try { _removeText(offs, len); }
    finally { releaseWriteLock(); }
  }

  /** Removes a portion of the document, if the edit condition allows it, as above.  Assumes that WriteLock is held. */
  public void _removeText(int offs, int len) throws EditDocumentException {
    if (offs < _promptPos) _beep.run();
    else _document._removeText(offs, len);
  }
  
  /** Removes a portion of the document, regardless of the edit condition.
   *  @param offs Offset to start deleting from
   *  @param len Number of characters to remove
   *  @throws EditDocumentException if the offset or length are illegal
   */
  public void forceRemoveText(int offs, int len) throws EditDocumentException {
    _document.forceRemoveText(offs, len);
  }

  /** Returns the length of the document. */
  public int getLength() { return _document.getLength(); }

  /** Returns a portion of the document.
   *  @param offs First offset of the desired text
   *  @param len Number of characters to return
   *  @throws EditDocumentException if the offset or length are illegal
   */
  public String getDocText(int offs, int len) throws EditDocumentException {
    return _document.getDocText(offs, len);
  }

  /** Returns the entire text of the document.  Identical to getText() in AbstractDocumentInterface.
   *  @throws EditDocumentException if the offset or length are illegal
   */
  public String getText() {
    acquireWriteLock();
    try { return _document.getDocText(0, getLength()); }
    finally { releaseWriteLock(); }
  }
  
  /** Returns the string that the user has entered at the current prompt.
   *  May contain newline characters.
   */
  public String getCurrentInput() {
    acquireReadLock();
    try {
      try { return getDocText(_promptPos, _document.getLength() - _promptPos); }
      catch (EditDocumentException e) { throw new UnexpectedException(e); }
    }
    finally { releaseReadLock(); }
  }

  /** Clears the current input text. */
  public void clearCurrentInput() {  _clearCurrentInputText(); }

  /** Removes the text from the current prompt to the end of the document. */
  protected void _clearCurrentInputText() {
    acquireWriteLock();
    try {
      // Delete old value of current line
      removeText(_promptPos, _document.getLength() - _promptPos);
    }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
    finally { releaseWriteLock(); }
  }
  
  /* Returns the default style for a "console" document. */
  public String getDefaultStyle() { return ConsoleDocument.DEFAULT_STYLE; }

  /** Returns the Pageable object for printing.
   *  @return A Pageable representing this document.
   */
  public Pageable getPageable() throws IllegalStateException { return _book; }
  
  /** This method tells the document to prepare all the DrJavaBook and PagePrinter objects. */
  public void preparePrintJob() {
    _book = new DrJavaBook(getDocText(0, getLength()), "Console", new PageFormat());
  }
  
  /** Prints the given document by bringing up a "Print" window. */
  public void print() throws PrinterException {
    preparePrintJob();
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPageable(_book);
    if (printJob.printDialog()) printJob.print();
    cleanUpPrintJob();
  }
  
  /** Clears the pageable object used to hold the print job. */
  public void cleanUpPrintJob() { _book = null; }
  
  /** Class ensuring that attempts to edit document lines above the prompt are rejected. */
  class ConsoleEditCondition extends DocumentEditCondition {
    public boolean canInsertText(int offs) { return canRemoveText(offs); }
    
    public boolean canRemoveText(int offs) {
      if (offs < _promptPos) {
        _beep.run();
        return false;
      }
      return true;
    }
  }
  
  /* Locking operations */
  
  /** Swing-style acquireReadLock(). */
  public void acquireReadLock() { _document.acquireReadLock(); }
  
  /** Swing-style releaseReadLock(). */
  public void releaseReadLock() { _document.releaseReadLock(); }
  
  /** Swing-style writeLock(). */
  public void acquireWriteLock() { _document.acquireWriteLock(); }
  
  /** Swing-style writeUnlock(). */
  public void releaseWriteLock() { _document.releaseWriteLock(); }
  
//  public int getLockState() { return _document.getLockState(); }
}
