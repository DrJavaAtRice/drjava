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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.text;

import edu.rice.cs.util.UnexpectedException;
import static edu.rice.cs.util.text.AbstractDocumentInterface.*;

import java.awt.print.Pageable;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.BadLocationException;

import java.util.Hashtable;

/** A swing implementation of the toolkit-independent EditDocumentInterface.  This document must use the readers/writers
  * locking protocol established in its superclasses.
 *  TODO: create a separate DummySwingDocument class for testing and make SwingDocument abstract.
 *  @version $Id$
 */
public class SwingDocument extends DefaultStyledDocument implements EditDocumentInterface, AbstractDocumentInterface {
  
//  /** The lock state.  See ReadersWritersLocking interface for documentation. */
//  private volatile int _lockState = UNLOCKED;
  
  /** Maps names to attribute sets */
  final protected Hashtable<String, AttributeSet> _styles;

  /** Determines which edits are legal on this document. */
  protected DocumentEditCondition _condition;
  
  /** Lock used to protect _wrappedPosListLock in DefinitionsDocument.  Placed here to ensure that it initialized before
    * use! */
  protected static final Object _wrappedPosListLock = new Object();

  /** Creates a new document adapter for a Swing StyledDocument. TODO: convert _styles and _condition to lazily 
    * initialized volatiles as soon as support for Java 1.4 is dropped and the double-check idiom is safe. */
  public SwingDocument() { 
    _styles = new Hashtable<String, AttributeSet>();
    _condition = new DocumentEditCondition();
  }

  /** Adds the given AttributeSet as a style with the given name. It can then be used in insertString.
   *  @param name Name of the style, to be passed to insertString
   *  @param s AttributeSet to use for the style
   */
  public void setDocStyle(String name, AttributeSet s) {
    _styles.put(name, s);  // no locking necessary: _styles is final and Hashtable is thread-safe
  }

  /** Returns the style with the given name, or null if no such named style exists. */
  public AttributeSet getDocStyle(String name) {
    return _styles.get(name);  // no locking necessary: _styles is final and Hashtable is thread-safe
  }
    
  /** Adds the given coloring style to the styles list.  Not supported in SwingDocument. */
  public void addColoring(int start, int end, String style) { }

  /** Gets the object which can determine whether an insert or remove edit should be applied, based on the inputs.
   *  @return an Object to determine legality of inputs
   */
  public DocumentEditCondition getEditCondition() { return _condition; }

  /** Provides an object which can determine whether an insert or remove edit should be applied, based on the inputs.
   *  @param condition Object to determine legality of inputs
   */
  public void setEditCondition(DocumentEditCondition condition) {
    acquireWriteLock();
    try { _condition = condition; }
    finally { releaseWriteLock(); }
  }
  
  /* Clears the document. */
  public void clear() {
    acquireWriteLock();
    try { remove(0, getLength()); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
    finally { releaseWriteLock(); }
  }
  

  /** Inserts a string into the document at the given offset and style, if the edit condition allows it.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void insertText(int offs, String str, String style) {
    acquireWriteLock();
    try { if (_condition.canInsertText(offs)) _forceInsertText(offs, str, style); }
    finally { releaseWriteLock(); }
  }
  
  /** Behaves exactly like forceInsertText except for assuming that WriteLock is already held. */
  public void _forceInsertText(int offs, String str, String style) {
    int len = getLength();
    if ((offs < 0) || (offs > len)) {
      String msg = "Offset " + offs + " passed to SwingDocument.forceInsertText is out of bounds [0, " + len + "]";
      throw new EditDocumentException(null, msg);
    }
    AttributeSet s = null;
    if (style != null) s = getDocStyle(style);
    try { super.insertString(offs, str, s); }
    catch (BadLocationException e) { throw new EditDocumentException(e); }
  }

  /** Inserts a string into the document at the given offset and style, regardless of the edit condition.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  public void forceInsertText(int offs, String str, String style) {
    acquireWriteLock();
    try { _forceInsertText(offs, str, style); }
    finally { releaseWriteLock(); }
  }
     
  /** Overrides superclass's insertString to impose the edit condition. The AttributeSet is ignored in the condition, 
   *  which sees a null style name.
   */
  public void insertString(int offs, String str, AttributeSet set) throws BadLocationException {
    acquireWriteLock();  // locking is used to make the test and modification atomic
    try { if (_condition.canInsertText(offs)) super.insertString(offs, str, set); }
    finally { releaseWriteLock(); }
  }

  /** Removes a portion of the document, if the edit condition allows it.
   *  @param offs Offset to start deleting from
   *  @param len Number of characters to remove
   *  @throws EditDocumentException if the offset or length are illegal
   */
  public void removeText(int offs, int len) {
    acquireWriteLock();  // locking is used to make the test and modification atomic
    try { if (_condition.canRemoveText(offs)) forceRemoveText(offs, len); }
    finally { releaseWriteLock(); }
  }

  /** Removes a portion of the document, regardless of the edit condition.
   *  @param offs Offset to start deleting from
   *  @param len Number of characters to remove
   *  @throws EditDocumentException if the offset or length are illegal
   */
  public void forceRemoveText(int offs, int len) {
    /* Using a writeLock is unnecessary because remove is already thread-safe */
    try { super.remove(offs, len); }
    catch (BadLocationException e) { throw new EditDocumentException(e); }
  }

  /** Overrides superclass's remove to impose the edit condition. */
  public void remove(int offs, int len) throws BadLocationException {
    acquireWriteLock(); // locking is used to make the test and modification atomic
    try { if (_condition.canRemoveText(offs))  super.remove(offs, len); }
    finally { releaseWriteLock(); }
  }

//  /** Returns the length of the document. */
//  public int getDocLength() { return getLength(); } // locking is unnecessary because getLength is already thread-safe

  /** Returns a portion of the document.
   *  @param offs First offset of the desired text
   *  @param len Number of characters to return
   *  @throws EditDocumentException if the offset or length are illegal
   */
  public String getDocText(int offs, int len) {
    try { return getText(offs, len); }  // locking is unnecessary because getText is already thread-safe
    catch (BadLocationException e) { throw new EditDocumentException(e); }
  }
  
  /** Returns entire text of this document. */
  public String getText() {
    acquireReadLock();
    try { return getText(0, getLength()); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }  // impossible
    finally { releaseReadLock(); }
  }
  
  /** Appends given string with specified attributes to end of this document. */
  public void append(String str, AttributeSet set) {
    acquireWriteLock();
    try { insertString(getLength(), str, set); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }  // impossible
    finally { releaseWriteLock(); }
  }
  
  /** Appends given string with specified named style to end of this document. */
  public void append(String str, String style) { append(str, style == null ? null : getDocStyle(style)); }
  
  /** A SwingDocument instance does not have a default style */
  public String getDefaultStyle() { return null; }
  
  public void print() {
    throw new UnsupportedOperationException("Printing not supported");
  }
  
  public Pageable getPageable() {
    throw new UnsupportedOperationException("Printing not supported");
  }
  
  /* Locking operations */
  
  /* Swing-style readLock(). Must be renamed because inherited writeLock is final. */
  public /* synchronized */ void acquireReadLock() {
//    _lockState++;
    readLock();
  }
   
  /* Swing-style readUnlock(). Must be renamed because inherited writeLock is final. */
  public /* synchronized */ void releaseReadLock() {
    readUnlock();
//    _lockState--;
  }

  /** Swing-style writeLock().  Must be renamed because inherited writeLock is final. */
  public /* synchronized */ void acquireWriteLock() { 
//    _lockState = MODIFYLOCKED;
    writeLock(); 
  }
  
   /** Swing-style writeUnlock().  Must be renamed because inherited writeUnlock is final.*/
  public /* synchronized*/ void releaseWriteLock() { 
    writeUnlock();
//   _lockState = UNLOCKED;
  }
  
  /** Performs the default behavior for createPosition in DefaultStyledDocument. */
  public Position createUnwrappedPosition(int offs) throws BadLocationException { return super.createPosition(offs); }
  
//  public int getLockState() { return _lockState; }
}

