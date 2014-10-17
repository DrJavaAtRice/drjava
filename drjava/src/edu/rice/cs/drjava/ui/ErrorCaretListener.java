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

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.util.swing.Utilities;

import java.awt.EventQueue;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Position;

/** Listens to the caret in the associated DefinitionsPane and highlights the text containing CompilerErrors.
 * @version $Id: ErrorCaretListener.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ErrorCaretListener implements CaretListener {
  private final OpenDefinitionsDocument _openDoc;
  private final DefinitionsPane _definitionsPane;
  protected final MainFrame _frame;

  /** Constructs a new caret listener to highlight errors. */
  public ErrorCaretListener(OpenDefinitionsDocument doc, DefinitionsPane defPane, MainFrame frame) {
    _openDoc = doc;
    _definitionsPane = defPane;
    _frame = frame;
  }

  /** Gets the OpenDefinitionsDocument corresponding to this listener. */
  public OpenDefinitionsDocument getOpenDefDoc() { return _openDoc; }

  /** After each update to the caret, determine if changes in highlighting need to be made.  Highlights the line 
   *  if the compiler output tab is showing.  Only runs in the event thread.
   */
  public void caretUpdate(final CaretEvent evt) {
    assert EventQueue.isDispatchThread();
    if (_frame.getSelectedErrorPanel() == null) return;
    updateHighlight(evt.getDot());
  }
  
  /** Update the highlight appropriately. */
  public void updateHighlight(final int curPos) {
    assert EventQueue.isDispatchThread();
    ErrorPanel panel = _frame.getSelectedErrorPanel();
    if (panel == null) return;  // no error panel is currently selected; redundant!
    
    CompilerErrorModel model =  panel.getErrorModel();
    
    if (! model.hasErrorsWithPositions(_openDoc)) return;
    
//    Utilities.showDebug("ErrorCaretListener.updateHighlight invoked");
    
    DJError error = model.getErrorAtOffset(_openDoc, curPos);
    
    ErrorPanel.ErrorListPane errorListPane = panel.getErrorListPane();
    // if no error is on this line, select the (none) item
    if (error == null) errorListPane.selectNothing();
    else {      
      if (errorListPane.shouldShowHighlightsInSource()) {
        // No need to move the caret since it's already here!
        _highlightErrorInSource(model.getPosition(error));
      }
      // Select item wants the DJError
      errorListPane.selectItem(error);
    }
  } 
  
  /** Hides the error highlight in the document. */
  public void removeHighlight() { 
    assert EventQueue.isDispatchThread();
    _definitionsPane.removeErrorHighlight(); 
  }

  /** Highlights the given error in the source.  Only runs in event thread.
   *  @param pos the position of the error
   */
  private void _highlightErrorInSource(Position pos) {
    assert EventQueue.isDispatchThread();
    if (pos == null) return;
    int errPos = pos.getOffset();
    
    String text = _openDoc.getText();
    
    // Look for the previous newline BEFORE this character. Thus start looking
    // on the character one before this character. If this is not the case,
    // if the error is at a newline character, both prev and next newlines
    // will be set to that place, resulting in nothing being highlighted.
    int prevNewline = text.lastIndexOf('\n', errPos - 1);
    if (prevNewline == -1) prevNewline = 0;
    
    int nextNewline = text.indexOf('\n', errPos);
    if (nextNewline == -1) nextNewline = text.length();
    
    removeHighlight();
    
    //Add 1 if not the first line of the file, so that the highlight range
    // will match the range chosen for the highlight manager.
    if (prevNewline > 0) prevNewline++;      
    
    if (prevNewline <= nextNewline) {
      _definitionsPane.addErrorHighlight(prevNewline, nextNewline);
    }
  }
}

