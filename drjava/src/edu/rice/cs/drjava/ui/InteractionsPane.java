/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.*;

import java.util.List;
//import java.util.LinkedList;
import java.util.Vector;

import edu.rice.cs.util.swing.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.model.DJDocument;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.drjava.model.repl.*;

/** The view component for repl interaction.
  * @version $Id$
  */
public abstract class InteractionsPane extends AbstractDJPane implements OptionConstants, ClipboardOwner {
  
  /** The custom keymap for the interactions pane. */
  protected Keymap _keymap;
  
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
  
  /** Returns a runnable object that beeps to the user. */
  public Runnable getBeep() { return _beep; }
  
  private InteractionsDJDocument _doc;
  
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
  }
  
  /** We lost ownership of what we put in the clipboard. */
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // ignore
  }
  
  /** Widens the visibilitly of the processKeyEvent method; it is protected in the superclass. */
  public void processKeyEvent(KeyEvent e) { super.processKeyEvent(e); }
  
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
  
  /** Updates the current location and highlight (if one exists). Adds prompt position to prompt list.  Assumes read
    * lock and reduced locks on _doc are already held. */
  protected void matchUpdate(int offset) {
    if (! _doc.hasPrompt()) return;
    _doc._setCurrentLocation(offset); 
    _removePreviousHighlight();
    
//    addToPromptList(getPromptPos()); // NOT USED
    int to = getCaretPosition();
    int from = _doc._balanceBackward(); //_doc()._reduced.balanceBackward();
    if (from > -1) {
      // Found a matching open brace to this close brace
      from = to - from;
      /* if (_notCrossesPrompt(to,from)) */ _addHighlight(from, to);  // _listOfPrompt NOT USED
      //      Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
    }
    // if this wasn't a close brace, check for an open brace
    else {
      // (getCaretPosition will be the start of the highlight)
      from = to;
      to = _doc._balanceForward();
      
      if (to > -1) {
        to = to + from;
        /* if (_notCrossesPrompt(to,from)) */ _addHighlight(from - 1, to);  // _listOfPrompt NOT USED
//        Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
      }
    }
  }
  
//  /** Returns the list of prompts. Used for tests. */
//  List<Integer> getPromptList() {  return _listOfPrompt; }  // NOT USED
  
//  /** Resets the list of prompts. Called when the interactions pane is reset. */  // NOT USED
//  public void resetPrompts() {
////    System.err.println("Clearing prompt list");
//    _listOfPrompt.clear(); 
//  }
  
  // NOT USED
//  /** Adds the position to the list of prompt positions. package private for tests. Does not necessarily run in
//    * event thread. _listOfPrompt is a Vector which is thread safe. */
//  void addToPromptList(int pos) {
//    if (! _listOfPrompt.contains(new Integer(pos))) _listOfPrompt.add(new Integer(pos));
//  }
  
//  /** Returns true if the two locations do not have a prompt between them. */  // NOT USED
//  private boolean _notCrossesPrompt(int to, int from) {
////    DrJava.consoleErr().println("To: " + to + " , From: " + from);
//    boolean toReturn = true;
//    for (Integer prompt : _listOfPrompt) {
//      toReturn &= ((to >= prompt && from >= prompt) || (to <= prompt && from <= prompt));      
//    }
//    return toReturn;
//  }
  
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
      setCaretPos(_doc.getCurrentLocation());    // FIX: not atomic; needs to be done as part of indentLines
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
}