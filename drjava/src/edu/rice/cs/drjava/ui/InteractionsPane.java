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

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.undo.*;  
import javax.swing.event.*;
import java.awt.event.*;

import edu.rice.cs.drjava.model.definitions.*;

import java.awt.event.KeyEvent;
import java.awt.datatransfer.*;
import java.util.Vector;

import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.model.DJDocument;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.repl.*;

/** The view component for repl interaction.
  * @version $Id: InteractionsPane.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class InteractionsPane extends AbstractDJPane implements OptionConstants, ClipboardOwner {
  
  public static Log LOG = new Log("InteractionsPane.txt", false);
  
  /** fields for use in undo/redo functionality */
  private volatile UndoAction _undoAction;
  private volatile RedoAction _redoAction;
  public volatile boolean _inCompoundEdit = false;
  private volatile int _compoundEditKey;
  private volatile boolean deleteCEBool = true;
    
  /** The custom keymap for the interactions pane. */
  protected final Keymap _keymap;
  
  /** Whether to draw text as antialiased. */
  private boolean _antiAliasText = false;
  
  static StyledEditorKit EDITOR_KIT;
  
  static { EDITOR_KIT = new InteractionsEditorKit();  }
  
  /** A runnable object that causes the editor to beep. */
  protected Runnable _beep = new Runnable() {
    public void run() { Toolkit.getDefaultToolkit().beep(); }
  };
  
  /** The OptionListener for TEXT_ANTIALIAS. */
  private class AntiAliasOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _antiAliasText = oce.value.booleanValue();
      InteractionsPane.this.repaint();
    }
  }
  
  /** listens for a left click by mouse and ends a compound edit for the undo/redo funationality
    * means that when someone clicks to change position in text, starts typing at the new location,
    * the undo will only get rid of the added text
    */
  private class leftUndoBreak extends MouseAdapter {
    public void mouseClicked(MouseEvent e){
     endCompoundEdit(); 
    }
  }
  
  /** Returns a runnable object that beeps to the user. */
  public Runnable getBeep() { return _beep; }
  
  private final InteractionsDJDocument _doc;
  
//  private List<Integer> _listOfPrompt = new Vector<Integer>(); // Vector used because it is synchronized. // NOT USED
  
  /** Creates an InteractionsPane with the given document.
    * Uses default keymap name ("INTERACTIONS_KEYMAP")
    * @param doc StyledDocument containing the interactions history.
    */
  public InteractionsPane(InteractionsDJDocument doc) { this("INTERACTIONS_KEYMAP", doc); }
  
  /** Creates an InteractionsPane with the given document.
    * @param keymapName the name of the keymap for this pane
    * @param doc StyledDocument containing the interactions history.
    */
  public InteractionsPane(String keymapName, InteractionsDJDocument doc) {
    super(doc);
    _doc = doc;
    //add actions for enter key, etc.
    _keymap = addKeymap(keymapName, getKeymap());
    
    setCaretPosition(doc.getLength());
    
    setHighlighter(new ReverseHighlighter());
    _highlightManager = new HighlightManager(this);
    
    _antiAliasText = DrJava.getConfig().getSetting(TEXT_ANTIALIAS).booleanValue();
    
    // The superclass AbstractDJPane installs a matchListener for this class
    
    // Setup color listeners.
    
    new ForegroundColorListener(this);
    new BackgroundColorListener(this);
    
    OptionListener<Boolean> aaTemp = new AntiAliasOptionListener();
    DrJava.getConfig().addOptionListener(OptionConstants.TEXT_ANTIALIAS, aaTemp);
    
    _resetUndo(); //gets undoManager ready to go
    addMouseListener(new leftUndoBreak());
  }
  
  /** We lost ownership of what we put in the clipboard. */
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // ignore
  }
  
  /** Widens the visibilitly of the processKeyEvent method; it is protected in the superclass. */
  public void processKeyEvent(KeyEvent e) { 
    
    //Fixes bug ID:2898576 - Backspace undo/redo issues
    if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && deleteCEBool){
      endCompoundEdit(); 
      deleteCEBool=false;
    }
    else if(e.getID()==KeyEvent.KEY_PRESSED && e.getKeyCode() != KeyEvent.VK_BACK_SPACE){
      deleteCEBool = true;
    }
    
    KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
    Action a = KeyBindingManager.ONLY.get(ks);
    // Don't perform the action if the keystroke is NULL_KEYSTROKE (generated by some Windows keys)
    if ((ks != KeyStrokeOption.NULL_KEYSTROKE) && (a != null)) {
      endCompoundEdit();
    }
    
    if ((e.getModifiers() & InputEvent.SHIFT_MASK)!=0 && e.getKeyCode()==KeyEvent.VK_ENTER) endCompoundEdit();  //ends compound edit on line change
    
    super.processKeyEvent(e);
  }
  
  /** Assigns the given keystroke to the given action in this pane.
    * @param stroke keystroke that triggers the action
    * @param action Action to perform
    */
  public void addActionForKeyStroke(KeyStroke stroke, Action action) {
    // we don't want multiple keys bound to the same action; Why NOT?
    KeyStroke[] keys = _keymap.getKeyStrokesForAction(action);
    if (keys != null) {
      for (int i = 0; i < keys.length; i++) _keymap.removeKeyStrokeBinding(keys[i]);
    }
    _keymap.addActionForKeyStroke(stroke, action);
    setKeymap(_keymap);
  }

  /** Assigns the given keystroke to the given action in this pane.
    * @param stroke keystroke that triggers the action
    * @param action Action to perform
    */
  public void addActionForKeyStroke(Vector<KeyStroke> stroke, Action action) {
    // remove previous bindings
    KeyStroke[] keys = _keymap.getKeyStrokesForAction(action);
    if (keys != null) {
      for (int i = 0; i < keys.length; i++) _keymap.removeKeyStrokeBinding(keys[i]);
    }
    for (KeyStroke ks: stroke) {
      _keymap.addActionForKeyStroke(ks, action);
    }
    setKeymap(_keymap);
  }
  
  /** Sets this pane's beep to be a different runnable object. Defaults to Toolkit.getDefaultToolkit().beep().
    * @param beep Runnable command to notify the user
    */
  public void setBeep(Runnable beep) { _beep = beep; }
  
  /** Highlights the given text with error highlight.
    * @param offset the offset in the text
    * @param length the length of the error to highlight
    */
  public void highlightError(int offset, int length) {
    _highlightManager.addHighlight(offset, offset+length, ERROR_PAINTER);
  }
  
  /** Overriding this method ensures that all new documents created in this editor pane use our editor kit 
    * (and thus our model).
    */
  protected EditorKit createDefaultEditorKit() { return EDITOR_KIT; }
  
  /** Enable anti-aliased text by overriding paintComponent. */
  protected void paintComponent(Graphics g) {
    if (g == null) return;  // Addresses bug 1651914
    if (_antiAliasText && g instanceof Graphics2D) {
      Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    super.paintComponent(g);
  }
  
  /** Returns the DJDocument held by the pane. */
  public DJDocument getDJDocument() { return _doc; }
  
  /** Updates match highlights.  Only runs in the event thread. 
    * @param offset   caret position immediately following some form of brace; hence offset > 0. 
    * @param opening  true if the the preceding brace is "opening" 
    */
  protected void matchUpdate(int offset, boolean opening) {
    if (! _doc.hasPrompt()) return;
    _doc.setCurrentLocation(offset); 
    _removePreviousHighlight();
    
    int caretPos = getCaretPosition();
    
    if (opening) {
      // getCaretPosition() will be the start of the highlight
      
      int to = _doc.balanceForward();  // relative distance to matching bracket
      
      if (to > -1) {  // matching closing bracket was found
        int end = caretPos + to;
        _addHighlight(caretPos - 1, end);  
      }
    }
    else {
      int from = _doc.balanceBackward();
      if (from > -1) {  // matching open bracket was found
        int start = caretPos - from;
        _addHighlight(start, caretPos);
      }
    }
  }
  
  /** Updates status fields in the main frame (title bar, selected file name) when document is modified. */
  protected void updateStatusField() { /* do nothing; this is an interactions pane. */ }
  
  /** Indent the given selection, for the given reason, in the current document.  Should only run in the event queuel
    * @param selStart - the selection start
    * @param selEnd - the selection end
    * @param reason - the reason for the indent
    * @param pm - the ProgressMonitor used by the indenter
    */
  protected void indentLines(int selStart, int selEnd, Indenter.IndentReason reason, ProgressMonitor pm) {
    assert EventQueue.isDispatchThread();
    try {
      _doc.indentLines(selStart, selEnd, reason, pm);
      setCaretPos(_doc.getCurrentLocation());    
    }
    catch (OperationCanceledException oce) { throw new UnexpectedException(oce); }
  }
  
  /** Returns true if the indent is to be performed. The code in the definitions pane prompts the user, but this 
    * requires a copy of mainframe, and a reason to do so. The user does not need to be prompted here. The cutoff 
    * in the definitions pane for the prompt is 10000 characters, which is unlikely to occur in the interactions 
    * pane very often if at all.
    * @param selStart - the selection start
    * @param selEnd - the selection end
    */
  protected boolean shouldIndent(int selStart, int selEnd) { return true; }
  
  /** Gets the current prompt position */
  public abstract int getPromptPos();
   
  /** Listens to any undoable events in the document, and adds them to the undo manager.  Must be done in the view 
    * because the edits are stored along with the caret position at the time of the edit.
    */
  private final UndoableEditListener _undoListener = new UndoableEditListener() {
    
    /** The function to handle what happens when an UndoableEditEvent occurs.
     *  @param e
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      assert EventQueue.isDispatchThread() || Utilities.TEST_MODE;
      UndoableEdit undo = e.getEdit();
      LOG.log("In undoableEditHappened - _inCompoundEdit is "+ _inCompoundEdit);
      if (! _inCompoundEdit) {
        CompoundUndoManager undoMan = _doc.getUndoManager();
        _inCompoundEdit = true;
        _compoundEditKey = undoMan.startCompoundEdit();
        getUndoAction().updateUndoState();
        getRedoAction().updateRedoState();
      }
      _doc.getUndoManager().addEdit(undo);
      getRedoAction().setEnabled(false);
    }
  };

  
  /** Ends a compound edit.*/
  public void endCompoundEdit() {
    if (_inCompoundEdit) {
      CompoundUndoManager undoMan = _doc.getUndoManager();
      _inCompoundEdit = false;
      undoMan.endCompoundEdit(_compoundEditKey);
    }
  }

  /** @return the undo action. */
  public UndoAction getUndoAction() { return  _undoAction; }

  /** @return the redo action. */
  public RedoAction getRedoAction() { return  _redoAction; }
  
  /** The undo action. */
  public class UndoAction extends AbstractAction {
    
    /** Constructor. */
    private UndoAction() {
      super("Undo");
      setEnabled(false);
    }

    /** What to do when user chooses to undo.
     *  @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        // LOG.log("UndoAction.actionPerformed. _doc = "+_doc+", event = "+e);
        _doc.getUndoManager().undo();
        _doc.updateModifiedSinceSave();
      }
      catch (CannotUndoException ex) {
        throw new UnexpectedException(ex);
      }
      updateUndoState();
      _redoAction.updateRedoState();
    }

    /** Updates the undo list, i.e., where we are as regards undo and redo. */
    protected void updateUndoState() {
      if (_doc.undoManagerCanUndo() && isEditable()) {
        setEnabled(true);
        putValue(Action.NAME, _doc.getUndoManager().getUndoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }
  }
  
  
  /** Redo action. */
  public class RedoAction extends AbstractAction {

    /** Constructor. */
    private RedoAction() {
      super("Redo");
      setEnabled(false);
    }

    /** In the event that the user chooses to redo something, this is what's called.
     *  @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        _doc.getUndoManager().redo();
        _doc.updateModifiedSinceSave();
      } catch (CannotRedoException ex) {
        throw new UnexpectedException(ex);
      }
      updateRedoState();
      _undoAction.updateUndoState();
    }

    /** Updates the redo state, i.e., where we are as regards undo and redo. */
    protected void updateRedoState() {
      if (_doc.undoManagerCanRedo() && isEditable()) {
        setEnabled(true);
        putValue(Action.NAME, _doc.getUndoManager().getRedoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }
  
  /** resets undo manager **/
  private void resetUndo() {
    _doc.getUndoManager().discardAllEdits();
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }
  
  /** discards edits and resets undoManager **/
  public void discardUndoEdits() {
    endCompoundEdit();
    resetUndo();
  }
  
  /** Reset the document Undo list. */
  public void _resetUndo() {
    if (_undoAction == null) _undoAction = new UndoAction();
    if (_redoAction == null) _redoAction = new RedoAction();
    
    _doc.resetUndoManager();
    
    _doc.addUndoableEditListener(_undoListener);
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }  
}