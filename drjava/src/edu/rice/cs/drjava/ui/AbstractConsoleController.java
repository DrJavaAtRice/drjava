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
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;

import java.io.Serializable;

import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.platform.PlatformFactory;

/** Abstract class to handle hooking up a console document with its pane.
 *  @version $Id$
 */
public abstract class AbstractConsoleController implements Serializable {
  /** Adapter for the Swing document used by the model.*/
  protected InteractionsDJDocument _adapter;

  /** Pane from the view. */
  protected InteractionsPane _pane;

  /** Style to use for default text. */
  protected SimpleAttributeSet _defaultStyle;

  /** Style to use for System.out. */
  protected final SimpleAttributeSet _systemOutStyle;

  /** Style to use for System.err. */
  protected final SimpleAttributeSet _systemErrStyle;

  // package private for testing purposes (although I haven't written tests yet)

  /** Action to change focus to previous pane. */
  Action switchToPrevPaneAction;

  /** Action to change focus to next pane. */
  Action switchToNextPaneAction;

  /** Initializes the document adapter and interactions pane. Subclasses *must* call _init() at the end 
   *  of their constructors.
   */
  protected AbstractConsoleController(InteractionsDJDocument adapter, InteractionsPane pane) {
    _adapter = adapter;
    _pane = pane;
    _defaultStyle = new SimpleAttributeSet();
    _systemOutStyle = new SimpleAttributeSet();
    _systemErrStyle = new SimpleAttributeSet();
  }

  /** Gets the console document for this console.*/
  public abstract ConsoleDocument getConsoleDoc();

  /** Initialization method.  *Must* be called in constructor by all subclasses. */
  protected void _init() {
    _addDocumentStyles();
    _setupModel();
    _setupView();
  }

  /** Adds AttributeSets as named styles to the document adapter. */
  protected void _addDocumentStyles() {
    // Default
    _adapter.setDocStyle(ConsoleDocument.DEFAULT_STYLE, _defaultStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_NORMAL_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        setDefaultFont(oe.value);
      }
    });

    // System.out
    _systemOutStyle.addAttributes(_defaultStyle);
    _systemOutStyle.addAttribute(StyleConstants.Foreground,
                                 DrJava.getConfig().getSetting(OptionConstants.SYSTEM_OUT_COLOR));
    _adapter.setDocStyle(ConsoleDocument.SYSTEM_OUT_STYLE, _systemOutStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_OUT_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _systemOutStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });

    // System.err
    _systemErrStyle.addAttributes(_defaultStyle);
    _systemErrStyle.addAttribute(StyleConstants.Foreground,
                                 DrJava.getConfig().getSetting(OptionConstants.SYSTEM_ERR_COLOR));
    _adapter.setDocStyle(ConsoleDocument.SYSTEM_ERR_STYLE, _systemErrStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_ERR_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _systemErrStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
  }

  /**
   * Sets the font for the document, updating all existing text.
   * This behavior is only necessary in Mac OS X, since
   * setFont() works fine on JTextPane on all other tested platforms.
   * @param f New font to use.
   */
  public void setDefaultFont(Font f) {
    Color c = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR);
    setDefaultFont(f, c);
  }

  /**
   * Sets the color for the document, updating all existing text.
   * This behavior is only necessary in Mac OS X, since
   * changing the main font works on all other tested platforms.
   * @param c New color to use.
   */
  public void setDefaultFont(Color c) {
    Font f = DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN);
    setDefaultFont(f, c);
  }

  /**
   * Sets the font and color for the document, updating all existing text.
   * This behavior is only necessary in Mac OS X, since setFont() and
   * changing the main font works on all other tested platforms.
   * @param f New font to use.
   * @param c New color to use.
   */
  public void setDefaultFont(Font f, Color c) {
    if (PlatformFactory.ONLY.isMacPlatform()) {
      SimpleAttributeSet fontSet = new SimpleAttributeSet();
      StyleConstants.setFontFamily(fontSet, f.getFamily());
      StyleConstants.setFontSize(fontSet, f.getSize());
      StyleConstants.setBold(fontSet, f.isBold());
      StyleConstants.setItalic(fontSet, f.isItalic());
      if (c != null) {
        StyleConstants.setForeground(fontSet, c);
      }
      _adapter.setCharacterAttributes(0, _adapter.getLength()+1, fontSet, false);
      _pane.setCharacterAttributes(fontSet, false);
      _updateStyles(fontSet);
    }
  }

  /**
   * Updates all document styles with the attributes contained in newSet.
   * @param newSet Style containing new attributes to use.
   */
  protected void _updateStyles(AttributeSet newSet) {
    _defaultStyle.addAttributes(newSet);
    _systemOutStyle.addAttributes(newSet);
    _systemErrStyle.addAttributes(newSet);
  }

  /** Sets up the model.*/
  protected abstract void _setupModel();

  /** Ensures that the caret always stays on or after the prompt, so that output is always scrolled to the bottom.
   * (The prompt is always at the bottom.)
   */
  class CaretUpdateListener implements DocumentListener {
    public void insertUpdate(final DocumentEvent e) {
      Utilities.invokeLater(new Runnable() {
        public void run() {
          ConsoleDocument doc = getConsoleDoc();
          int caretPos = _pane.getCaretPosition();
          int promptPos = doc.getPromptPos();
          int length = doc.getLength();
          
          // Figure out where the prompt was before the update
          int prevPromptPos = promptPos;
          if (e.getOffset() < promptPos) {
            // Insert happened before prompt,
            //  so previous position was further back
            prevPromptPos = promptPos - e.getLength();
          }
          
          if (!doc.hasPrompt()) {
            // Scroll to the end of the document, since output has been
            // inserted after the prompt.
            moveToEnd();
          }
          // (Be careful not to move caret during a reset, when the
          //  prompt pos is temporarily far greater than the length.)
          else if (promptPos <= length) {
            if (caretPos < prevPromptPos) {
              // Caret has fallen behind prompt, so make it catch up so
              //  the new input is visible.
              moveToPrompt();
            }
            else {
              // Caret was on or after prompt, so move it right by the size
              //  of the insert.
              int size = promptPos - prevPromptPos;
              if (size > 0)  moveTo(caretPos + size);
            }
          }
        }
      });
    }

    public void removeUpdate(DocumentEvent e) { _ensureLegalCaretPos(); }
    public void changedUpdate(DocumentEvent e) { _ensureLegalCaretPos(); }
    
    protected void _ensureLegalCaretPos() {
      Utilities.invokeLater(new Runnable() {
        public void run() { 
          int length = getConsoleDoc().getLength();
          if (_pane.getCaretPosition() > length) _pane.setCaretPosition(length);
        }
      });
    }
  }


  /** Sets up the view. */
  protected void _setupView() {
    KeyStroke beginLineKey = DrJava.getConfig().getSetting(OptionConstants.KEY_BEGIN_LINE);
    _pane.addActionForKeyStroke(beginLineKey, gotoPromptPosAction);
    _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(beginLineKey),
                                selectToPromptPosAction);
    KeyStroke endLineKey = DrJava.getConfig().getSetting(OptionConstants.KEY_END_LINE);
    _pane.addActionForKeyStroke(endLineKey, gotoEndAction);
    _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(endLineKey),
                                selectToEndAction);

    DrJava.getConfig().addOptionListener(OptionConstants.KEY_BEGIN_LINE,
                                         new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(oe.value, gotoPromptPosAction);
        _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(oe.value),
                                    selectToPromptPosAction);
     }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_END_LINE,
                                         new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(oe.value, gotoEndAction);
        _pane.addActionForKeyStroke(KeyBindingManager.Singleton.addShiftModifier(oe.value),
                                    selectToEndAction);
     }
    });
  }

  /** Accessor method for the InteractionsDJDocument. */
  public InteractionsDJDocument getDocumentAdapter() { return _adapter; }

  /** Accessor method for the InteractionsPane. */
  public InteractionsPane getPane() { return _pane; }

  /** Determines if the associated console pane is currently computing.
   *  @return true iff the console is busy
   */
  protected boolean _busy() { return ! getConsoleDoc().hasPrompt(); }

  /** Inserts a new line at the caret position. */
  AbstractAction newLineAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { getConsoleDoc().insertNewLine(_pane.getCaretPosition()); }
  };

  /** Removes all text after the prompt. */
  AbstractAction clearCurrentAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { getConsoleDoc().clearCurrentInput(); }
  };

  /** Goes to the end of the current input line. */
  AbstractAction gotoEndAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { moveToEnd(); }
  };

  /** Selects to the end of the current input line. */
  AbstractAction selectToEndAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { _pane.moveCaretPosition(getConsoleDoc().getLength()); }
  };

  /** Moves the caret to the prompt. */
  AbstractAction gotoPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { moveToPrompt(); }
  };

  /** Selects to the current prompt. */
  AbstractAction selectToPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      // Selects the text between the old pos and the prompt
      _pane.moveCaretPosition(getConsoleDoc().getPromptPos());
    }
  };

  /** Moves the pane's caret to the end of the document. Only affects reduced_model not the document model. */
  void moveToEnd() { moveTo(getConsoleDoc().getLength()); }
  
  /** Moves the pane's caret to the document's prompt. Only affects reduced_model not the document model. */
  void moveToPrompt() { moveTo(getConsoleDoc().getPromptPos()); }
  
  /** Moves the pane's caret to the given position, as long as it's legal. */
  void moveTo(int pos) {
    // Sanity check
    if (pos < 0) pos = 0;
    else {
      int maxLen = getConsoleDoc().getLength(); 
      if (pos > maxLen) pos = maxLen;
    }
    _pane.setCaretPosition(pos);
  }

  public void setPrevPaneAction(Action a) {
    switchToPrevPaneAction = a;

    // We do this here since switchToPrevPaneAction is set after the constructor is called.
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_PANE),
                                switchToPrevPaneAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_PREVIOUS_PANE, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_PANE),
                                    switchToPrevPaneAction);
      }
    });
  }

  public void setNextPaneAction(Action a) {
    switchToNextPaneAction = a;

    // We do this here since switchToNextPaneAction is set after the
    // constructor is called.
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_PANE),
                                switchToNextPaneAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_NEXT_PANE, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_PANE),
                                    switchToNextPaneAction);
      }
    });
  }
}
