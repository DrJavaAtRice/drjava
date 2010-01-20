/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;

import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.SwingDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import java.awt.dnd.*;
import edu.rice.cs.drjava.DrJavaRoot;

/** This pane class for a SwingDocument. */
public abstract class AbstractDJPane extends JTextPane
  implements OptionConstants, DropTargetListener {
  
  // ------------ FIELDS -----------
  
  /* The amount of the visible pane to scroll on a single click (Swing's default is .1) */
  private static final double SCROLL_UNIT = .05;

  
  /** Paren/brace/bracket matching highlight color. */
  static ReverseHighlighter.DrJavaHighlightPainter MATCH_PAINTER;

  static {
    Color highColor = DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR);
    MATCH_PAINTER = new ReverseHighlighter.DrJavaHighlightPainter(highColor);
  }
  
  /** Highlight painter for selected errors in the defs doc. */
  static ReverseHighlighter.DrJavaHighlightPainter ERROR_PAINTER =
    new ReverseHighlighter.DrJavaHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));
  
  private static final int ALT_CTRL_META_MASK = Event.ALT_MASK | Event.CTRL_MASK | Event.META_MASK;
  
  protected volatile HighlightManager _highlightManager;
  
  /** Looks for changes in the caret position to see if a paren/brace/bracket highlight is needed. */
  protected final CaretListener _matchListener = new CaretListener() {
    
    /** Checks caret position to see if it needs to set or remove a highlight from the document. Only modifies the 
      * document--not any GUI classes.
      * @param ce the event fired by the caret position change
      */
    public void caretUpdate(final CaretEvent ce) { 
      
      assert EventQueue.isDispatchThread();
      _removePreviousHighlight();
      
      int offset = ce.getDot();
      if (offset < 1) return;
      DJDocument doc = getDJDocument();
      try { 
        char prevChar = doc.getText(offset - 1, 1).charAt(0);
        if (prevChar == '{' || prevChar == '(' ) matchUpdate(offset, true);       // forward match
        else if (prevChar == '}' || prevChar == ')') matchUpdate(offset, false);  // backward match
        else updateStatusField();  // update main frame status fields; a no-op for InteractionsPanes
        
      }
      catch(BadLocationException e) { DrJavaErrorHandler.record(e); }
    }
  };
  
  /** Our current paren/brace/bracket matching highlight. */
  protected volatile HighlightManager.HighlightInfo _matchHighlight = null;
  
  protected static final SwingDocument NULL_DOCUMENT = new SwingDocument() {
    public void addDocumentListener(DocumentListener listener) {
      // do nothing, NULL_DOCUMENT is a dummy
    }
    public void addUndoableEditListener(UndoableEditListener listener) {
      // do nothing, NULL_DOCUMENT is a dummy
    }
  };
  
  //--------- CONSTRUCTOR ----------
  
  AbstractDJPane(SwingDocument doc) {
    super(doc);
    setContentType("text/java");
    
    // Add listener that checks if highlighting matching braces must be updated
    addCaretListener(_matchListener);
    disableAltCntlMetaChars(this);
  }
  
  //--------- METHODS -----------
  
  /** Create a null default action for Cntl/Alt/Meta chars in the keymap for p. */
  public static void disableAltCntlMetaChars(JTextComponent p) {
    Keymap km = p.getKeymap();
    final Action defaultAction = km.getDefaultAction();
    km.setDefaultAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if ((e.getModifiers() & ALT_CTRL_META_MASK) != 0) return;
        defaultAction.actionPerformed(e);
      }
    });
  }
 
  /** Adds a highlight to the document.  Called by _updateMatchHighlight().
   *  @param from start of highlight
   *  @param to end of highlight
   */
  protected void _addHighlight(int from, int to) {
    _matchHighlight = _highlightManager.addHighlight(from, to, MATCH_PAINTER);
  }
  
  /** Updates the document location when the cursor is immediately to the right of a bracket and highlights the
    * corresponding bracketed area.  The opening param indicates whether the preceding char is an opening bracket
    * ['(', '{'] or a closing bracket [')', '}']  This method must execute directly as part of the document update.
    * If cannot be deferred using invokeLater.  Only modifies fields added to DefaultStyledDocument)---not any Swing
    * library classes.  Only executes in the event thread.
    * @param offset   the new offset of the caret
    * @param opening  true if the preceding character is an opening bracket ['(', '{']
    */
  protected abstract void matchUpdate(int offset, boolean opening);
  
  /** Updates status fields in the main frame (title bar, selected file name) when document is modified. */
  protected abstract void updateStatusField();

  /** Removes the previous highlight so document is cleared when caret position changes.  Only runs in event thread. */
  protected void _removePreviousHighlight() {
    assert EventQueue.isDispatchThread();
    if (_matchHighlight != null) {
      _matchHighlight.remove();
      //_highlightManager.removeHighlight((HighlightManager.HighlightInfo)_matchHighlight);
      _matchHighlight = null;
    }
  }
  
  /** A length checked version of setCaretPosition(int pos) that ensures pos is within the DJDocument. */
  public void setCaretPos(int pos) {
//    System.err.println("setCaretPos(" + pos + ") called");
    DJDocument doc = getDJDocument();
    int len = doc.getLength();
    if (pos > len) {
      setCaretPosition(len);
      return;
    }
    setCaretPosition(pos);
  }

// This block of code is used solely for debugging
//  public void setCaretPosition(int pos) {
//    super.setCaretPosition(pos);
//    System.err.println("setCaretPosition(" + pos + ") called");
//  }

  public int getScrollableUnitIncrement(Rectangle visibleRectangle, int orientation, int direction) {
    return (int) (visibleRectangle.getHeight() * SCROLL_UNIT);
  }
  
  /** Runs indent(int) with a default value of Indenter.IndentReason.OTHER */
  public void indent() { indent(Indenter.IndentReason.OTHER); }

  /** Perform an indent either on the current line or on the given selected box of text.  Calls are sent to GlobalModel
   *  which are then forwarded on to the document.  Hopefully the indent code will be fixed and corrected so this 
   *  doesn't look so ugly. The purpose is to divorce the pane from the document so we can just pass a document to 
   *  DefinitionsPane and that's all it cares about.
   *  @param reason the action that spawned this indent action.  Enter presses are special, so that stars are inserted 
   *  when lines in a multiline comment are broken up.
   */
  public void indent(final Indenter.IndentReason reason) {

    /** Because indent() is a function called directly by the Keymap, it does not go through the regular insertString
      * channels.  Thus it may not be in sync with the document's internal position.  For that reason, we grab the
      * caretPosition and set the current location to that value before calling the insertLine operation.  The logic
      * for a single line insert is very dependent on the current location.
      */
    
    // Is this action still necessary?  Answer: yes!  Without this line, the caret often moves when the user hits "tab"
    getDJDocument().setCurrentLocation(getCaretPosition());
    
    // The _reduced lock within DefinitionsDocument should be probably be set as well
    
    final int selStart = getSelectionStart();
    final int selEnd = getSelectionEnd();
    
    ProgressMonitor pm = null;
    //= new ProgressMonitor(_mainFrame, "Indenting...",
    //                    null, 0, selEnd - selStart);
    
    //pm.setProgress(0);
    // 3 seconds before displaying the progress bar.
    //pm.setMillisToDecideToPopup(3000);
    
    // XXX: Temporary hack because of slow indent...
    //  Prompt if more than 10000 characters to be indented, then do the indent
    if (shouldIndent(selStart,selEnd)) { indentLines(selStart, selEnd, reason, pm); }

  }

  /** Indents the given selection, for the given reason, in the current document.
    * @param selStart - the selection start
    * @param selEnd - the selection end
    * @param reason - the reason for the indent
    * @param pm - the ProgressMonitor used by the indenter
    */
  protected abstract void indentLines(int selStart, int selEnd, Indenter.IndentReason reason, ProgressMonitor pm);
     
  /** Returns true if the indent is to be performed.
    * @param selStart - the selection start
    * @param selEnd - the selection end
    */
  protected abstract boolean shouldIndent(int selStart, int selEnd);
  
  /** Returns the DJDocument held by the pane. */
  public abstract DJDocument getDJDocument();
  
  /** Drag and drop target. */
  DropTarget dropTarget = new DropTarget(this, this);  

  /** User dragged something into the component. */
  public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
    DrJavaRoot.dragEnter(dropTargetDragEvent);
  }
  
  public void dragExit(DropTargetEvent dropTargetEvent) { }
  public void dragOver(DropTargetDragEvent dropTargetDragEvent) { }
  public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent){ }
  
  /** User dropped something on the component. */
  public synchronized void drop(DropTargetDropEvent dropTargetDropEvent) {
    DrJavaRoot.drop(dropTargetDropEvent);
  }
}