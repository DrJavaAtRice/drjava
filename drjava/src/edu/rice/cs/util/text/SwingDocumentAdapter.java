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

package edu.rice.cs.util.text;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import java.util.Hashtable;

/**
 * Provides a toolkit-independent way to interact with a
 * Swing StyledDocument.
 * @version $Id$
 */
public class SwingDocumentAdapter extends DefaultStyledDocument
  implements DocumentAdapter
{
  /** Maps names to attribute sets */
  protected Hashtable<String, AttributeSet> _styles;
  
  /** Determines which edits are legal on this document. */
  protected DocumentEditCondition _condition;
  
  /**
   * Creates a new document adapter for a Swing StyledDocument.
   */
  public SwingDocumentAdapter() {
    _styles = new Hashtable<String, AttributeSet>();
    _condition = new DocumentEditCondition();
  }
  
  /**
   * Adds the given AttributeSet as a style with the given name.
   * It can then be used in insertString.
   * @param name Name of the style, to be passed to insertString
   * @param s AttributeSet to use for the style
   */
  public void addDocStyle(String name, AttributeSet s) {
    _styles.put(name, s);
  }
  
  /**
   * Gets the object which can determine whether an insert
   * or remove edit should be applied, based on the inputs.
   * @param condition Object to determine legality of inputs
   */
  public DocumentEditCondition getEditCondition() {
    return _condition;
  }
  
  /**
   * Provides an object which can determine whether an insert
   * or remove edit should be applied, based on the inputs.
   * @param condition Object to determine legality of inputs
   */
  public void setEditCondition(DocumentEditCondition condition) {
    _condition = condition;
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
    if (_condition.canInsertText(offs, str, style)) {
      forceInsertText(offs, str, style);
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
    AttributeSet s = null;
    if (style != null) {
      s = _styles.get(style);
    }
    try {
      super.insertString(offs, str, s);
    }
    catch (BadLocationException e) {
      throw new DocumentAdapterException(e);
    }
  }
  
  /**
   * Overrides superclass's insertString to impose the edit condition.
   * The AttributeSet is ignored in the condition, which sees a null
   * style name.
   */
  public void insertString(int offs, String str, AttributeSet set) 
    throws BadLocationException
  {
    if (_condition.canInsertText(offs, str, null)) {
      super.insertString(offs, str, set);
    }
  }
  
  /**
   * Removes a portion of the document, if the edit condition allows it.
   * @param offs Offset to start deleting from
   * @param len Number of characters to remove
   * @throws DocumentAdapterException if the offset or length are illegal
   */
  public void removeText(int offs, int len) throws DocumentAdapterException {
    if (_condition.canRemoveText(offs, len)) {
      forceRemoveText(offs, len);
    }
  }
  
  /**
   * Removes a portion of the document, regardless of the edit condition.
   * @param offs Offset to start deleting from
   * @param len Number of characters to remove
   * @throws DocumentAdapterException if the offset or length are illegal
   */
  public void forceRemoveText(int offs, int len) throws DocumentAdapterException {
    try {
      super.remove(offs, len);
    }
    catch (BadLocationException e) {
      throw new DocumentAdapterException(e);
    }
  }
  
  /**
   * Overrides superclass's remove to impose the edit condition.
   */
  public void remove(int offs, int len) throws BadLocationException {
    if (_condition.canRemoveText(offs, len)) {
      super.remove(offs, len);
    }
  }
  
  /**
   * Returns the length of the document.
   */
  public int getDocLength() {
    return getLength();
  }
  
  /**
   * Returns a portion of the document.
   * @param offs First offset of the desired text
   * @param len Number of characters to return
   * @throws DocumentAdapterException if the offset or length are illegal
   */
  public String getDocText(int offs, int len) throws DocumentAdapterException {
    try {
      return getText(offs, len);
    }
    catch (BadLocationException e) {
      throw new DocumentAdapterException(e);
    }
  }
}
