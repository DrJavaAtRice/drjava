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

import java.awt.EventQueue;
import java.awt.print.Pageable;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

import java.util.HashMap;

/** A swing implementation of the toolkit-independent EditDocumentInterface.  This document should use the 
  * readers/writers locking protocol established in its superclasses but it does not.  Under unfavorable
  * schedules, methods like append will generate run-time errors because the document can change between the
  * determination of its length and the insertion of the appended text.  An operation in the document is safe 
  * only if the proper lock is held before it is called OR all update and operations involving multiple reads
  * are performed in the event thread.
  * 
  * TODO: create a separate DummySwingDocument class for testing and make SwingDocument abstract.
  * @version $Id: SwingDocument.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class SwingDocument extends DefaultStyledDocument implements EditDocumentInterface, AbstractDocumentInterface {
  
  /** The modified state. */
  protected volatile boolean _isModifiedSinceSave = false;
  
  /** Maps names to attribute sets */
  final protected HashMap<String, AttributeSet> _styles;
  
  /** Determines which edits are legal on this document. */
  protected DocumentEditCondition _condition;
  
  /** Creates a new document adapter for a Swing StyledDocument. TODO: convert _styles and _condition to lazily 
    * initialized volatiles as soon as support for Java 1.4 is dropped and the double-check idiom is safe. */
  public SwingDocument() { 
    _styles = new HashMap<String, AttributeSet>();
    _condition = new DocumentEditCondition();
  }
  
  /** Adds the given AttributeSet as a style with the given name. It can then be used in insertString.
    * @param name Name of the style, to be passed to insertString
    * @param s AttributeSet to use for the style
    */
  public void setDocStyle(String name, AttributeSet s) {
    _styles.put(name, s);  // no locking necessary: _styles is final and Hashtable is thread-safe
  }
  
  /** Returns the style with the given name, or null if no such named style exists. */
  public AttributeSet getDocStyle(String name) {
    return _styles.get(name);  // no locking necessary: _styles is final and Hashtable is thread-safe
  }
  
  /** Adds the given coloring style to the styles list.  Not supported in SwingDocument.  ONly runs in event thread. */
  public void addColoring(int start, int end, String style) { }
  
  /** Gets the object which can determine whether an insert or remove edit should be applied, based on the inputs.
    * @return an Object to determine legality of inputs
    */
  public DocumentEditCondition getEditCondition() { return _condition; }
  
  /** Provides an object which can determine whether an insert or remove edit should be applied, based on the inputs.
    * @param condition Object to determine legality of inputs
    */
  public void setEditCondition(DocumentEditCondition condition) { _condition = condition;  }
  
  /* Clears the document. */
  public void clear() {
    try { remove(0, getLength()); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Inserts a string into the document at the given offset and style, if the edit condition allows it.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void insertText(int offs, String str, String style) {
    if (_condition.canInsertText(offs)) forceInsertText(offs, str, style); 
  }
  
  /** Inserts a string into the document at the given offset and style, regardless of the edit condition.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void forceInsertText(int offs, String str, String style) {
    int len = getLength();
    if ((offs < 0) || (offs > len)) {
      String msg = "Offset " + offs + " passed to SwingDocument.forceInsertText is out of bounds [0, " + len + "]";
      throw new EditDocumentException(null, msg);
    }
    AttributeSet s = null;
    if (style != null) s = getDocStyle(style);
    try { super.insertString(offs, str, s); }
    catch (BadLocationException e) { throw new EditDocumentException(e); }  // should never happen
  }
  
 /** Overrides superclass's insertString to impose the edit condition. The AttributeSet is ignored in the condition, 
    * which sees a null style name.
    */
  public void insertString(int offs, String str, AttributeSet set) throws BadLocationException {
    // Assertion commented out because it does not hold at startup.  See DrJava bug 2321815.
    /* assert Utilities.TEST_MODE || EventQueue.isDispatchThread(); */
    if (_condition.canInsertText(offs)) super.insertString(offs, str, set);
  }
  
  /** Removes a portion of the document, if the edit condition allows it.
    * @param offs Offset to start deleting from
    * @param len Number of characters to remove
    * @throws EditDocumentException if the offset or length are illegal
    */
  public void removeText(int offs, int len) {
    if (_condition.canRemoveText(offs)) forceRemoveText(offs, len); 
  }
  
  /** Removes a portion of the document, regardless of the edit condition.
    * @param offs Offset to start deleting from
    * @param len Number of characters to remove
    * @throws EditDocumentException if the offset or length are illegal
    */
  public void forceRemoveText(int offs, int len) {
    try { super.remove(offs, len); }
    catch (BadLocationException e) { throw new EditDocumentException(e); }
  }
  
  /** Overrides superclass's remove to impose the edit condition. */
  public void remove(int offs, int len) throws BadLocationException {
    if (_condition.canRemoveText(offs))  super.remove(offs, len); 
  }
  
//  /** Returns the length of the document. */
//  public int getDocLength() { return getLength(); } // locking is unnecessary because getLength is already thread-safe
  
  /** Returns a portion of the document.
    * @param offs First offset of the desired text
    * @param len Number of characters to return
    * @throws EditDocumentException if the offset or length are illegal
    */
  public String getDocText(int offs, int len) {
    try { return getText(offs, len); }  // locking is unnecessary because getText is already thread-safe
    catch (BadLocationException e) { throw new EditDocumentException(e); }
  }
  
  /** Gets the document text; this method should only be executed in event thread. */
  public String getText() { 
    try { return getText(0, getLength()); }  // calls method defined in DefaultStyledDocument
    catch (BadLocationException e) { throw new UnexpectedException(e); }  // should never happen in event thread
  }
  
  /** Sanitized version of getText(int, int) that converts BadLocationException to UnexpectedException. */
  public String _getText(int pos, int len) { 
    try { return getText(pos, len); }  // calls method defined in DefaultStyledDocument
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Appends given string with specified attributes to end of this document. */
  public void append(String str, AttributeSet set) {
    try { insertString(getLength(), str, set); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }  // impossible
  }
  
  /** Appends given string with specified named style to end of this document. */
  public void append(String str, String style) { append(str, style == null ? null : getDocStyle(style)); }
  
  /** Appends given string with default style to end of this document. */
  public void append(String str) { append(str, (AttributeSet) null); }
  
  /** A SwingDocument instance does not have a default style */
  public String getDefaultStyle() { return null; }
  
  public void print() { throw new UnsupportedOperationException("Printing not supported"); }
  
  public Pageable getPageable() { throw new UnsupportedOperationException("Printing not supported"); }

  /** Performs the default behavior for createPosition in DefaultStyledDocument. */
  public Position createUnwrappedPosition(int offs) throws BadLocationException { return super.createPosition(offs); }
}

