/*BEGIN_COPYRIGHT_BLOCK*

DrJava Eclipse Plug-in BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of DrJava, the JavaPLT group, Rice University, nor the names of software 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.eclipse.util.text;

import java.awt.print.Pageable;
import java.awt.print.PrinterException;
import java.util.Hashtable;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;

import edu.rice.cs.util.text.*;

/**
 * Provides a toolkit-independent way to interact with an
 * SWT StyledText widget (from Eclipse).
 *
 * A StyledText serves as both model and view, so this class
 * must interface to the model parts of the widget.
 *
 * @version $Id$
 */
public class SWTDocumentAdapter implements EditDocumentInterface, ConsoleDocumentInterface {

  // TO DO:
  //  - Test multithreaded support

  /**
  * 
  */
 private static final long serialVersionUID = 5467877329916880674L;

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
  public void insertText(int offs, String str, String style) throws EditDocumentException {
    _insertText(offs, str, style);
  }
  public void _insertText(int offs, String str, String style) throws EditDocumentException {
    if (_condition.canInsertText(offs)) forceInsertText(offs, str, style);
  }

  /** Inserts a string into the document at the given offset and the given named style, regardless of the edit condition.
    * @param offs Offset into the document
    * @param str String to be inserted
    * @param style Name of the style to use.  Must have been added using addStyle.
    * @throws EditDocumentException if the offset is illegal
    */
  
  // WHY IS THIS METHOD SYNCHRONIZED? IT MAKES NO SENSE!
  public synchronized void forceInsertText(final int offs, final String str, final String style)
    throws EditDocumentException {
    _forceInsertText(offs, str, style);
  }
  
  public void _forceInsertText(final int offs, final String str, final String style) {
    SWTStyle s = null;
    if (style != null) s = _styles.get(style);
    
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

  /** Removes a portion of the document, if the edit condition allows it.
    * @param offs Offset to start deleting from
    * @param len Number of characters to remove
    * @throws EditDocumentException if the offset or length are illegal
    */
  public void removeText(int offs, int len) throws EditDocumentException {
    _removeText(offs, len);
  }
  /** Same as above except it assumes that the document readLock is already held, which is 
    * irrelevant in SWT.
    */
  public void _removeText(int offs, int len) throws EditDocumentException {
    if (_condition.canRemoveText(offs)) { //, len)) {
      forceRemoveText(offs, len);
    }
  }

  /** Removes a portion of the document, regardless of the edit condition.
    * @param offs Offset to start deleting from
    * @param len Number of characters to remove
    * @throws EditDocumentException if the offset or length are illegal
    */
  public synchronized void forceRemoveText(final int offs, final int len) throws EditDocumentException {
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

  /** Returns the length of the document. */
  public int getLength() { return _text.getCharCount(); }

  /** Returns a portion of the document.
    * @param offs First offset of the desired text
    * @param len Number of characters to return
    * @throws EditDocumentException if the offset or length are illegal
    */
  public String getDocText(int offs, int len) throws EditDocumentException {
    try { return _text.getTextRange(offs, len); }
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
   public void print() throws PrinterException { }
   
   private volatile boolean _hasPrompt = true;
   
   /** @return true iff this document has a prompt and is ready to accept input. */
   public boolean hasPrompt() { return _hasPrompt; }
   
   /** Setter for the _hasPrompt property. */
   public void setHasPrompt(boolean val) { _hasPrompt = val; }
}
