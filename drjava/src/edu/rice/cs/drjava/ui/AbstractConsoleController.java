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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;

import javax.swing.*;
import javax.swing.text.*;

import java.util.Vector;

import edu.rice.cs.util.text.ConsoleDocument;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.platform.PlatformFactory;
import edu.rice.cs.drjava.model.ClipboardHistoryModel;

/** Abstract class that hooks a Swing console/interactions document with its Swing pane.
  * TODO: move interactions specific functionality to InteractionsController by creating ConsoleDJDocument class
  * @version $Id: AbstractConsoleController.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class AbstractConsoleController /* implements Serializable */ {
  
  /** Adapter for the Swing document used by the model.*/
  protected final InteractionsDJDocument _interactionsDJDocument;

  /** Pane from the view. */
  protected final InteractionsPane _pane;

  /** Style to use for default text. */
  protected final SimpleAttributeSet _defaultStyle;

  /** Style to use for System.out. */
  protected final SimpleAttributeSet _systemOutStyle;

  /** Style to use for System.err. */
  protected final SimpleAttributeSet _systemErrStyle;

  /** Action to change focus to previous pane.  Package private for testing purposes. */
  volatile Action switchToPrevPaneAction;

  /** Action to change focus to next pane. */
  volatile Action switchToNextPaneAction;

  /** Initializes the Swing console document and Swing interactions pane. Subclasses *must* call _init() at the end 
   *  of their constructors.
   */
  protected AbstractConsoleController(InteractionsDJDocument doc, InteractionsPane pane) {
    _interactionsDJDocument = doc;
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

  /** Adds AttributeSets as named styles to the Swing console document. */
  protected void _addDocumentStyles() {
    // Default
    _interactionsDJDocument.setDocStyle(ConsoleDocument.DEFAULT_STYLE, _defaultStyle);
    
    // System.out
    _systemOutStyle.addAttributes(_defaultStyle);
    _systemOutStyle.addAttribute(StyleConstants.Foreground,
                                 DrJava.getConfig().getSetting(OptionConstants.SYSTEM_OUT_COLOR));
    _interactionsDJDocument.setDocStyle(ConsoleDocument.SYSTEM_OUT_STYLE, _systemOutStyle);
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
    _interactionsDJDocument.setDocStyle(ConsoleDocument.SYSTEM_ERR_STYLE, _systemErrStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_ERR_COLOR,
                                         new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _systemErrStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
  }

  /** Sets up the model.*/
  protected abstract void _setupModel();
  
  /** Sets up the view. */
  protected void _setupView() {
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_BEGIN_LINE), gotoPromptPosAction);
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_BEGIN_LINE_SELECT), selectToPromptPosAction);
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_END_LINE), gotoEndAction);
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_END_LINE_SELECT), selectToEndAction);

    DrJava.getConfig().addOptionListener(OptionConstants.KEY_BEGIN_LINE, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(oe.value, gotoPromptPosAction);
      }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_BEGIN_LINE_SELECT, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(oe.value, selectToPromptPosAction);
     }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_END_LINE, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(oe.value, gotoEndAction);
     }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_END_LINE_SELECT, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(oe.value, selectToEndAction);
     }
    });
    
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_CUT), cutAction);
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_COPY), copyAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_CUT, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_CUT), cutAction);
     }
    });
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_COPY, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_COPY), copyAction);
     }
    });
  }
  
  /** Clears and resets the view (other than features derived from the model. */
  public void resetView() {
//    _pane.resetPrompts();  // NOT USED
//    System.err.println("Prompts.reset" + "Prompts for pane " + _pane.hashCode() + " is " + _pane.getPromptList());
  }
  
  /** Default cut action. */
  Action cutAction = new DefaultEditorKit.CutAction() {
    public void actionPerformed(ActionEvent e) {
      
      if (_pane.getSelectedText() != null) {
        super.actionPerformed(e);
        String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(_pane);
        if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
      }
    }
  };
  
  /** Default copy action. */
  Action copyAction = new DefaultEditorKit.CopyAction() {
    public void actionPerformed(ActionEvent e) {
      if (_pane.getSelectedText() != null) {
        super.actionPerformed(e);
        String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(_pane);
        if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
      }
    }
  };

  /** Accessor method for the InteractionsDJDocument. */
  public InteractionsDJDocument getDocumentAdapter() { return _interactionsDJDocument; }

  /** Accessor method for the InteractionsPane. */
  public InteractionsPane getPane() { return _pane; }

  /** Determines if the associated console pane is currently computing.
   *  @return true iff the console is busy
   */
  protected boolean _busy() { return ! getConsoleDoc().hasPrompt(); }

  /** Inserts a new line at the caret position. */
  AbstractAction newLineAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { 
      ConsoleDocument doc = getConsoleDoc();
      doc.insertNewline(_pane.getCaretPosition()); 
    }
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
    public void actionPerformed(ActionEvent e) { 
      ConsoleDocument doc = getConsoleDoc();
      _pane.moveCaretPosition(doc.getLength()); 
    }
  };

  /** Moves the caret to the prompt. */
  AbstractAction gotoPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { moveToPrompt(); }
  };

  /** Selects to the current prompt. */
  AbstractAction selectToPromptPosAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      assert EventQueue.isDispatchThread();
      ConsoleDocument doc = getConsoleDoc();
      // Selects the text between the old pos and the prompt
      _pane.moveCaretPosition(doc.getPromptPos());
    }
  };

  /** Moves the pane's caret to the end of the document. Only affects reduced_model not the document model.  */
  void moveToEnd() { 
    assert EventQueue.isDispatchThread();
    int len = getConsoleDoc().getLength();
    _pane.setCaretPosition(len);
//    setCachedCaretPos(len);
  }
  
  /** Moves the pane's caret to the document's prompt. Only affects reduced_model not the document model. */
  void moveToPrompt() { 
    assert EventQueue.isDispatchThread();
    int pos = getConsoleDoc().getPromptPos();
    _pane.setCaretPosition(pos);
  }

  public void setPrevPaneAction(Action a) {
    switchToPrevPaneAction = a;

    // We do this here since switchToPrevPaneAction is set after the constructor is called.
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_PANE),
                                switchToPrevPaneAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_PREVIOUS_PANE, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_PANE), switchToPrevPaneAction);
      }
    });
  }

  public void setNextPaneAction(Action a) {
    switchToNextPaneAction = a;

    // We do this here since switchToNextPaneAction is set after the
    // constructor is called.
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_PANE),
                                switchToNextPaneAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_NEXT_PANE, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_PANE), switchToNextPaneAction);
      }
    });
  }
}
