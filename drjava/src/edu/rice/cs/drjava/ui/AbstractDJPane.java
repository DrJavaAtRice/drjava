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
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;

import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.SwingDocument;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/** This class is misnamed!!!  The associated document is NOT necessarily a DJDocument!  It
 *  can be a ConsoleDocument. */
public abstract class AbstractDJPane extends JTextPane implements OptionConstants {
  
  // ------------ FIELDS -----------
  
  /* The amount of the visible pane to scroll on a single click (Swing's default is .1) */
  private static final double SCROLL_UNIT = .05;
  
  /** Paren/brace/bracket matching highlight color. */
  static ReverseHighlighter.DefaultHighlightPainter MATCH_PAINTER;

  static {
    Color highColor = DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR);
    MATCH_PAINTER = new ReverseHighlighter.DefaultHighlightPainter(highColor);
  }
  
  /** Highlight painter for selected errors in the defs doc. */
  static ReverseHighlighter.DefaultHighlightPainter ERROR_PAINTER =
    new ReverseHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));
  
  protected volatile HighlightManager _highlightManager;
  
  /** Looks for changes in the caret position to see if a paren/brace/bracket highlight is needed. */
  protected final CaretListener _matchListener = new CaretListener() {
    
    /** Checks caret position to see if it needs to set or remove a highlight from the document. When the cursor
      * is immediately right of ')', '}', or ']', it highlights up to the matching open paren/brace/bracket.
      * This method must execute as part of the document update. If deferred using invokeLater, it does not work.
      * Only modifies the document--not any GUI classes.
      * @param e the event fired by the caret position change
      */
    public void caretUpdate(CaretEvent e) { 
      getDJDocument().setCurrentLocation(getCaretPosition());  
      _removePreviousHighlight();
      _updateMatchHighlight();
    }
  };
  
  /** Our current paren/brace/bracket matching highlight. */
  protected volatile HighlightManager.HighlightInfo _matchHighlight = null;
  
  protected final SwingDocument NULL_DOCUMENT = new SwingDocument();
  
  //--------- CONSTRUCTOR ----------
  
  AbstractDJPane(SwingDocument doc) {
    super(doc);
    setContentType("text/java");
    
    // Add listener that checks if highlighting matching braces must be updated
    addCaretListener(_matchListener);
  }
  
  //--------- METHODS -----------
 
  /** Adds a highlight to the document.  Called by _updateMatchHighlight().
   *  @param from start of highlight
   *  @param to end of highlight
   */
  protected void _addHighlight(int from, int to) {
    _matchHighlight = _highlightManager.addHighlight(from, to, MATCH_PAINTER);
  }
  
  protected abstract void _updateMatchHighlight();

  /** Removes the previous highlight so document is cleared when caret position changes.  Can be executed from outside
    * the event thread. */
  protected void _removePreviousHighlight() {
    if (_matchHighlight != null) {
      _matchHighlight.remove();
      //_highlightManager.removeHighlight((HighlightManager.HighlightInfo)_matchHighlight);
      _matchHighlight = null;
    }
  }
  
  /** Be sure to update the document (and thus the reduced model) any time the caret position changes.
   *  @param pos
   */
  public void setCaretPosition(int pos) {
    super.setCaretPosition(pos);
    getDJDocument().setCurrentLocation(pos);
//    _doc.setCurrentLocation(pos);
  }
  
  public int getScrollableUnitIncrement(Rectangle visibleRectangle, int orientation, int direction) {
    return (int) (visibleRectangle.getHeight() * SCROLL_UNIT);
  }
  
  /** What is this for?  The override does not nothing! */ 
  public void moveCaretPosition(int pos) { super.moveCaretPosition(pos); }
  
  /** Runs indent(int) with a default value of Indenter.OTHER */
  public void indent() { indent(Indenter.OTHER); }

  /** Perform an indent either on the current line or on the given selected box of text.  Calls are sent to GlobalModel
   *  which are then forwarded on to the document.  Hopefully the indent code will be fixed and corrected so this 
   *  doesn't look so ugly. The purpose is to divorce the pane from the document so we can just pass a document to 
   *  DefinitionsPane and that's all it cares about.
   *  @param reason the action that spawned this indent action.  Enter presses are special, so that stars are inserted 
   *  when lines in a multiline comment are broken up.
   */
  public void indent(final int reason) {

    /** Because indent() is a function called directly by the Keymap, it does not go through the regular insertString
     *  channels and thus it may not be in sync with the document's position.  For that reason, we must grab the
     *  caretPostion and set the current location to that value before calling the insertLine operation.  The logic
     *  for a single line insert is very dependent on the current location.
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

  /** Indent the given selection, for the given reason, in the current document.
   *  @param selStart - the selection start
   *  @param selEnd - the selection end
   *  @param reason - the reason for the indent
   *  @param pm - the ProgressMonitor used by the indenter
   */
  protected abstract void indentLines(int selStart, int selEnd, int reason, ProgressMonitor pm);
     
  /**
   * Returns true if the indent is to be performed.
   * @param selStart - the selection start
   * @param selEnd - the selection end
   */
  protected abstract boolean shouldIndent(int selStart, int selEnd);
  
  /** Returns the DJDocument held by the pane. */
  public abstract DJDocument getDJDocument();
}