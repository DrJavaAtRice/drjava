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

package edu.rice.cs.util.text;

import java.io.*;
import java.awt.EventQueue;
import java.awt.print.*;
import java.awt.EventQueue;

import edu.rice.cs.drjava.model.print.DrJavaBook;

import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocumentInterface;
import edu.rice.cs.util.text.DocumentEditCondition;
import edu.rice.cs.util.text.EditDocumentException;
import edu.rice.cs.util.FileOps;

/** A GUI-toolkit agnostic interface to a console document.  This class assumes that the embedded document supports 
  * readers/writers locking and uses that locking protocol to ensure the integrity of the data added in this class
  * WHY is prompt considered part of a console document rather than an interactions document?
  * @version $Id: ConsoleDocument.java 5594 2012-06-21 11:23:40Z rcartwright $ */
public class ConsoleDocument implements ConsoleDocumentInterface {
  
  /** The default prompt to use in the console. */
  public static final String DEFAULT_CONSOLE_PROMPT = "";
  
  public static final String DEFAULT_CONTINUATION_STRING = "";
  
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
  
  /** String to use in multiline expressions */
  protected volatile String _continuationString;
  
  /** The book object used for printing that represents several pages */
  protected volatile DrJavaBook _book;
  
  /** Creates a new ConsoleDocument with the given embedded ConsoleDocumentInterface (a SwingDocument in native DrJava).
    * @param doc the embedded ConsoleDocumentInterface object
    */
  public ConsoleDocument(ConsoleDocumentInterface doc) {
    _document = doc;
    
    _beep = new Runnable() { public void run() { } };
    _prompt = DEFAULT_CONSOLE_PROMPT;
    
    _continuationString = DEFAULT_CONTINUATION_STRING;
    
    _promptPos = DEFAULT_CONSOLE_PROMPT.length();
    _document.setHasPrompt(false);
    _document.setEditCondition(new ConsoleEditCondition()); // Prevent any edits before the prompt!
  }
  
  /** @return true iff this document has a prompt and is ready to accept input. */
  public boolean hasPrompt() { return _document.hasPrompt(); }
  
  public void setHasPrompt(boolean val) { _document.setHasPrompt(val); }
  
  /** Accessor for the string used for the prompt. */
  public String getPrompt() { return _prompt; }
  
  /** Sets the string to use for the prompt.
    * @param prompt String to use for the prompt.
    */
  public void setPrompt(String prompt) { _prompt = prompt; }
  
  /** Returns the length of the prompt string. */
  public int getPromptLength() { return _prompt.length(); }
  
  /** Gets the object which determines whether an insert/remove edit should be applied based on the inputs.
    * @return the DocumentEditCondition to determine legality of inputs
    */
  public DocumentEditCondition getEditCondition() { return _document.getEditCondition(); }
  
  /** Provides an object which can determine whether an insert or remove edit should be applied, based on the inputs.
    * @param condition Object to determine legality of inputs
    */
  public void setEditCondition(DocumentEditCondition condition) { _document.setEditCondition(condition); }
  
  /** Returns the first location in the document where editing is allowed. */
  public int getPromptPos() { return _promptPos; }
  
  /** Sets the prompt position. Only used in tests.
    * @param newPos the new position.
    */
  public void setPromptPos(int newPos) { 
    _promptPos = newPos; 
  }
  
  /** Sets a runnable action to use as a beep.
    * @param beep Runnable beep command
    */
  public void setBeep(Runnable beep) { _beep = beep; }
  
  /** Resets the document to a clean state. Only runs in the event thread. */
  public void reset(String banner) {
    assert EventQueue.isDispatchThread();
    try {
      forceRemoveText(0, _document.getLength());
      forceInsertText(0, banner, DEFAULT_STYLE);
      _promptPos = banner.length();
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
  }
  
  /** Prints a prompt for a new input. */
  public void insertPrompt() {
    try {
      int len = _document.getLength();
      // Update _promptPos before updating _document because insertText runs insertUpdate to adjust caret
      _promptPos = len + _prompt.length();
      forceInsertText(len, _prompt, DEFAULT_STYLE); // need forceAppend!
      _document.setHasPrompt(true);
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e);  }
  }
  
  /** Prints a continuation string for new input in a multiline expression. */
  public void insertContinuationString() {
    try {
      int len = _document.getLength();
      // Update _promptPos before updating _document because insertText runs insertUpdate to adjust caret
      _promptPos = len + _continuationString.length();
      forceInsertText(len, _continuationString, DEFAULT_STYLE); // need forceAppend!
      _document.setHasPrompt(true);
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e);  }
  }  
  
  /** Disables the prompt in this document. */
  public void disablePrompt() {
    _document.setHasPrompt(false);
    _promptPos = _document.getLength();
  }
  
  /** Inserts a new line at the given position.
    * @param pos Position to insert the new line
    */
  public void insertNewline(int pos) {
    // Correct the position if necessary
    try {
      int len = _document.getLength();
      if (pos > len)  pos = len;
      else if (pos < 0) pos = 0;
      
      String newLine = "\n";  // Was StringOps.EOL; but Swing uses '\n' for newLine
      insertText(pos, newLine, DEFAULT_STYLE);
    }
    catch (EditDocumentException e) { throw new UnexpectedException(e); }
  }
  
  /** Gets the position immediately before the prompt, or the doc length if there is no prompt.  Only runs in the event
    * thread. */
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
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    try {
      int pos = _getPositionBeforePrompt();
//      System.err.println("_promptPos before update = " + _promptPos);
      _promptPos = _promptPos + text.length();
      forceInsertText(pos, text, style);
    }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
  }
  
  /** Inserts a string into the document at the given offset and named style, if the edit condition allows it.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void insertText(int offs, String str, String style) throws EditDocumentException {
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
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    int offs = _document.getLength();
    _addToStyleLists(offs, str, style);
    _document.insertText(offs, str, style);
  }
  
  /** Inserts a string into the document at the given offset and  style, regardless of the edit condition.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void forceInsertText(int offs, String str, String style) throws EditDocumentException {      
    _addToStyleLists(offs, str, style);
//    System.err.println("Inserting text '" + str + "' at position " + offs);
    _document.forceInsertText(offs, str, style);
  }
  
  /** Adds style specifier to _stylelists. Only runs in event thread. */
  private void _addToStyleLists(int offs, String str, String style) {
    if (_document instanceof SwingDocument)
      ((SwingDocument)_document).addColoring(offs, offs + str.length(), style);
  }
  
  /** Removes a portion of the document, if the edit condition (including promptPos) allows it.
    * @param offs Offset to start deleting from
    * @param len Number of characters to remove
    * @throws EditDocumentException if the offset or length are illegal
    */
  public void removeText(int offs, int len) throws EditDocumentException {
    if (offs < _promptPos) _beep.run();
    else _document.removeText(offs, len);
  }
  
  /** Removes a portion of the document, regardless of the edit condition.
    * @param offs Offset to start deleting from
    * @param len Number of characters to remove
    * @throws EditDocumentException if the offset or length are illegal
    */
  public void forceRemoveText(int offs, int len) throws EditDocumentException {
    _document.forceRemoveText(offs, len);
  }
  
  /** Returns the length of the document. */
  public int getLength() { return _document.getLength(); }
  
  /** Returns a portion of the document.
    * @param offs First offset of the desired text
    * @param len Number of characters to return
    * @throws EditDocumentException if the offset or length are illegal
    */
  public String getDocText(int offs, int len) throws EditDocumentException {
    return _document.getDocText(offs, len);
  }
  
  /** Returns the entire text of the document.  Identical to getText() in AbstractDocumentInterface.
    * @throws EditDocumentException if the offset or length are illegal
    */
  public String getText() { return _document.getDocText(0, getLength()); }
  
  /** Returns the string that the user has entered at the current prompt. May contain newline characters. */
  public String getCurrentInput() {
      try { return getDocText(_promptPos, _document.getLength() - _promptPos); }
      catch (EditDocumentException e) { throw new UnexpectedException(e); }
  }
  
  /** Clears the current input text. */
  public void clearCurrentInput() {  _clearCurrentInputText(); }
  
  /** Removes the text from the current prompt to the end of the document. */
  protected void _clearCurrentInputText() {
    try {
      // Delete old value of current line
      removeText(_promptPos, _document.getLength() - _promptPos);
    }
    catch (EditDocumentException ble) { throw new UnexpectedException(ble); }
  }
  
  /* Returns the default style for a "console" document. */
  public String getDefaultStyle() { return ConsoleDocument.DEFAULT_STYLE; }
  
  /** Returns the Pageable object for printing.
    * @return A Pageable representing this document.
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
  
  /** Saves the contents of the document to a file.
    * @param selector File to save to
    */
  public void saveCopy(FileSaveSelector selector) throws IOException {
    assert EventQueue.isDispatchThread();
    try {
      final File file = selector.getFile().getCanonicalFile();
      if (! file.exists() || selector.verifyOverwrite(file)) {  // confirm that existing file can be overwritten        
        FileOps.saveFile(new FileOps.DefaultFileSaver(file) {
          /** Only runs in event thread so no read lock is necessary. */
          public void saveTo(OutputStream os) throws IOException {
            final String text = getDocText(0, getLength());
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(text,0,text.length());
            osw.flush();
          }
        });
      }
    }
    catch (OperationCanceledException oce) {
      // Thrown by selector.getFile() if the user cancels.
      // We don't do anything if this happens.
      return;
    }
  }
}
