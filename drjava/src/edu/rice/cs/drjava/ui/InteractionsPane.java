/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Toolkit;

import edu.rice.cs.util.swing.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.model.repl.*;

/**
 * The view component for repl interaction.
 *
 * @version $Id$
 */
public class InteractionsPane extends JTextPane implements OptionConstants {

  /** The custom keymap for the interactions pane. */
  protected Keymap _keymap;
  // InteractionsPane must be constructed completely, so
  // this can't be placed in the constructor.
  protected HighlightManager _highlightManager = null;

  /**
   *  Highlight painter for syntax errors, currently borrowing breakpoint color.
   */
  public static DefaultHighlighter.DefaultHighlightPainter ERROR_PAINTER =
    new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));


  /** A runnable object that causes the editor to beep. */
  protected Runnable _beep = new Runnable() {
    public void run() {
      Toolkit.getDefaultToolkit().beep();
    }
  };

  /**
   * Returns a runnable object that beeps to the user.
   */
  public Runnable getBeep() {
    return _beep;
  }

  /**
   * Creates an InteractionsPane with the given document.
   * Uses default keymap name ("INTERACTIONS_KEYMAP")
   * @param doc StyledDocument containing the interactions history.
   */
  public InteractionsPane(InteractionsDocumentAdapter doc) {
    this("INTERACTIONS_KEYMAP", doc);
  }

  /**
   * Creates an InteractionsPane with the given document.
   * @param keymapName the name of the keymap for this pane
   * @param doc StyledDocument containing the interactions history.
   */
  public InteractionsPane(String keymapName, InteractionsDocumentAdapter doc) {
    super(doc);

    //add actions for enter key, etc.
    _keymap = addKeymap(keymapName, getKeymap());

    setCaretPosition(doc.getLength());

    // Setup color listeners.
    new ForegroundColorListener(this);
    new BackgroundColorListener(this);
  }

  /**
   * Assigns the given keystroke to the given action in this pane.
   * @param stroke keystroke that triggers the action
   * @param action Action to perform
   */
  public void addActionForKeyStroke(KeyStroke stroke, Action action) {
    // we don't want multiple keys bound to the same action
    KeyStroke[] keys = _keymap.getKeyStrokesForAction(action);
    if (keys != null) {
      for (int i = 0; i < keys.length; i++) {
        _keymap.removeKeyStrokeBinding(keys[i]);
      }
    }
    _keymap.addActionForKeyStroke(stroke, action);
    setKeymap(_keymap);
  }

  /**
   * Sets this pane's beep to be a different runnable object.
   * (Defaults to Toolkit.getDefaultToolkit().beep().)
   * @param beep Runnable command to notify the user
   */
  public void setBeep(Runnable beep) {
    _beep = beep;
  }

  /**
   * Initializes the highlight manager.
   */
  private void _initializeHighlightManager() {
    _highlightManager = new HighlightManager(this);
  }

  /**
   * Highlights the given text with error highlight.
   * @param offset the offset in the text
   * @param length the length of the error to highlight
   */
  public void highlightError(int offset, int length) {
    if(_highlightManager == null) {
      _initializeHighlightManager();
    }
    _highlightManager.addHighlight(offset, offset+length, ERROR_PAINTER);
  }
  
//  public void requestFocus() {
//    super.requestFocus();
//    System.out.println("InteractionsPane.setFocus() was called");
//  }
}