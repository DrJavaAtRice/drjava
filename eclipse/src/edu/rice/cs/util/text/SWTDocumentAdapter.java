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

import java.util.Hashtable;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

import org.eclipse.swt.events.*;
import java.awt.print.Pageable;
import java.awt.print.PrinterException;

/**
 * Provides a toolkit-independent way to interact with an
 * SWT StyledText widget (from Eclipse).
 *
 * A StyledText serves as both model and view, so this class
 * must interface to the model parts of the widget.
 *
 * @version $Id$
 */
public class SWTDocumentAdapter implements EditDocumentInterface {

  // TO DO:
  //  - Test multithreaded support

  /** StyledText widget containing the view. */
  protected StyledText _pane;

  /** Underlying document used by the view. */
  protected StyledTextContent _text;

  /** Maps names to attribute sets */
  protected Hashtable<String, SWTStyle> _styles;

  /** Determines which edits are legal on this document. */
  protected DocumentEditCondition _condition;

  /** Whether the adapter is in a state of forcing an insertion. */
  protected boolean _forceInsert;

  /** Whether the adapter is in a state of forcing a removal. */
  protected boolean _forceRemove;

  /** Any exception that occurred in the most recent (asynchronous) edit. */
  protected EditDocumentException _editException;

  /**
   * Creates a new document adapter for an SWT StyledText.
   */
  public SWTDocumentAdapter(StyledText pane) {
    _pane = pane;
    _text = pane.getContent();
    _styles = new Hashtable<String, SWTStyle>();
    _condition = new DocumentEditCondition();
    _forceInsert = false;
    _forceRemove = false;
    _editException = null;

    // Add a listener that enforces the condition
    addVerifyListener(new ConditionListener());
  }

  /**
   * Adds a VerifyListener to the internal SWTStyledText.
   * @param l VerifyListener to add to the pane
   */
  public void addVerifyListener(VerifyListener l) {
    _pane.addVerifyListener(l);
  }

  /**
   * Removes a VerifyListener from the internal SWTStyledText.
   * @param l VerifyListener to remove from the pane
   */
  public void removeVerifyListener(VerifyListener l) {
    _pane.removeVerifyListener(l);
  }

  /**
   * Adds a ModifyListener to the internal SWTStyledText.
   * @param l ModifyListener to add to the pane
   */
  public void addModifyListener(ModifyListener l) {
    _pane.addModifyListener(l);
  }

  /**
   * Removes a ModifyListener from the internal SWTStyledText.
   * @param l ModifyListener to remove from the pane
   */
  public void removeModifyListener(ModifyListener l) {
    _pane.removeModifyListener(l);
  }

  /**
   * Adds the given SWTStyle as a style with the given name.
   * It can then be used in insertText.
   * @param name Name of the style, to be passed to insertText
   * @param s SWTStyle to use for the style
   */
  public void addDocStyle(String name, SWTStyle s) {
    _styles.put(name, s);
  }

  /**
   * Gets the object which can determine whether an insert
   * or remove edit should be applied, based on the inputs.
   * @return an Object to determine legality of inputs
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
   * @throws EditDocumentException if the offset is illegal
   */
  public void insertText(int offs, String str, String style)
    throws EditDocumentException
  {
    if (_condition.canInsertText(offs)) { //, str, style)) {
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
   * @throws EditDocumentException if the offset is illegal
   */
  public synchronized void forceInsertText(final int offs, final String str,
                                           final String style)
    throws EditDocumentException
  {
    SWTStyle s = null;
    if (style != null) {
      s = _styles.get(style);
    }
    final SWTStyle chosenStyle = s;

    _editException = null;
    _forceInsert = true;

    // Do the insert
    _pane.getDisplay().syncExec(new Runnable() {
      public void run() {
        try {
          _pane.replaceTextRange(offs, 0, str);

          // Add the style
          if (chosenStyle != null) {
            StyleRange range = new StyleRange();
            range.start = offs;
            range.length = str.length();
            range.fontStyle = chosenStyle.getFontStyle();
            range.foreground = chosenStyle.getColor();
            _pane.setStyleRange(range);
          }
        }
        catch (IllegalArgumentException e) {
          _editException = new EditDocumentException(e);
        }
      }
    });
    _forceInsert = false;
    if (_editException != null) {
      throw _editException;
    }
  }

  /**
   * Removes a portion of the document, if the edit condition allows it.
   * @param offs Offset to start deleting from
   * @param len Number of characters to remove
   * @throws EditDocumentException if the offset or length are illegal
   */
  public void removeText(int offs, int len) throws EditDocumentException {
      if (_condition.canRemoveText(offs)) { //, len)) {
      forceRemoveText(offs, len);
    }
  }

  /**
   * Removes a portion of the document, regardless of the edit condition.
   * @param offs Offset to start deleting from
   * @param len Number of characters to remove
   * @throws EditDocumentException if the offset or length are illegal
   */
  public synchronized void forceRemoveText(final int offs, final int len)
    throws EditDocumentException
  {
    _editException = null;
    _forceRemove = true;

    // Do the remove
    _pane.getDisplay().syncExec(new Runnable() {
      public void run() {
        try {
          _pane.replaceTextRange(offs, len, "");
        }
        catch (IllegalArgumentException e) {
          _editException = new EditDocumentException(e);
        }
      }
    });
    _forceRemove = false;
    if (_editException != null) {
      throw _editException;
    }
  }

  /**
   * Returns the length of the document.
   */
  public int getLength() {
    return _text.getCharCount();
  }

  /**
   * Returns a portion of the document.
   * @param offs First offset of the desired text
   * @param len Number of characters to return
   * @throws EditDocumentException if the offset or length are illegal
   */
  public String getDocText(int offs, int len) throws EditDocumentException {
    try {
      return _text.getTextRange(offs, len);
    }
    catch (IllegalArgumentException e) {
      throw new EditDocumentException(e);
    }
  }
  /** Appends a string to this in the given named style, if the edit condition allows it.
   *  @param str String to be inserted
   *  @param style Name of the style to use.  Must have been added using addStyle.
   *  @throws EditDocumentException if the offset is illegal
   */
  public void append(String str, String style) {
	  	
	  	int offs = getLength();
	    forceInsertText(offs, str, style);
  }
                                                                                                                        

  
  /**
   * Highlights the given range in the given color.
   * @param offset Offset of first character to highlight
   * @param length Number of characters to highlight
   * @param color Color to use for the highlight
   */
  public void highlightRange(int offset, int length, Color color) {
    StyleRange range = new StyleRange();
    range.start = offset;
    range.length = length;
    range.background = color;
    _pane.setStyleRange(range);
  }

  /**
   * A VerifyListener that enforces the current edit condition.
   */
  protected class ConditionListener implements VerifyListener {
    public void verifyText(VerifyEvent e) {
      if (e.text.length() == 0) {
        // Remove event
        e.doit = _canRemove(e);
      }
      else if (e.start == e.end) {
        // Insert event
        e.doit = _canInsert(e);
      }
      else {
        // Replace event
        e.doit = _canRemove(e) && _canInsert(e);
      }
    }
    /** Returns whether the event should be allowed to insert. */
    protected boolean _canInsert(VerifyEvent e) {
      return _forceInsert ||
	  _condition.canInsertText(e.start); //, e.text, null);
    }
    /** Returns whether the event should be allowed to remove. */
    protected boolean _canRemove(VerifyEvent e) {
      return _forceRemove ||
	  _condition.canRemoveText(e.start); //, e.end - e.start);
    }
  }

  /**
   * Bookkeeping for a particular style in an SWTDocumentAdapter.
   */
  public static class SWTStyle {
    /** Color for this style. */
    protected Color _color;
    protected int _fontStyle;

    /**
     * Creates a new style to be used in an SWTDocumentAdapter.
     * @param color Color of the style
     * @param fontStyle Font style constant (eg. SWT.BOLD)
     */
    public SWTStyle(Color color, int fontStyle) {
      _color = color;
      _fontStyle = fontStyle;
    }

    public Color getColor() { return _color; }
    public int getFontStyle() { return _fontStyle; }
  }

    /* Locking operations */

   /** Swing-style readLock(). */
   public void acquireReadLock() { }

    /** Swing-style readUnlock(). */
   public void releaseReadLock() { }

     /** Swing-style writeLock(). */
   public void acquireWriteLock() { }

    /** Swing-style writeUnlock(). */
   public void releaseWriteLock(){ }

   /** Gets the String identifying the default style for this document if one exists; null otherwise. */
   public String getDefaultStyle() {
	   	return "NONE";
   }
    

   /** Returns the Pageable object for printing.
    *  @return A Pageable representing this document.
    */
   public Pageable getPageable() throws IllegalStateException {
	   return null;
   }
    
   
   /** Prints the given console document */
   public void print() throws PrinterException {
	   
   }

}
