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

import java.awt.Toolkit;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.File;

import java.util.EventListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.DefaultStyledDocument;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.repl.InputListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsModel;
import edu.rice.cs.drjava.ui.DrJavaErrorHandler;

import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.CompletionMonitor;
import edu.rice.cs.util.Lambda;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
/* TODO: clean up mixed references to _adapter and _doc which point to almost the same thing (an 
 * InteractionsDJDocument versus an InteractionsDocument. */

/** This class installs listeners and actions between an InteractionsDocument (the model) and an InteractionsPane 
  * (the view).  We may want to refactor this class into a different package. <p>
  * (The PopupConsole was introduced in version 1.29 of this file and subsequently removed.)
  *
  * @version $Id$
  */
public class InteractionsController extends AbstractConsoleController {
  
  /* InteractionsDocument _adapter is inherited from AbstractConsoleController. */
  /* InteractionsPane _pane is inherited from AbstractConsoleController. */
  
  private static final Log _log = new Log("ConsoleController.txt", false);
  
  private static final String INPUT_ENTERED_NAME = "Input Entered";
  private static final String INSERT_NEWLINE_NAME = "Insert Newline";
  
  /** Style for System.in box */
  public static final String INPUT_BOX_STYLE = "input.box.style";
  
  /** The symbol used in the document for the input box. */
  public static final String INPUT_BOX_SYMBOL = "[DrJava Input Box]";
  
  /** InteractionsModel to handle interpretation. */
  private volatile InteractionsModel _model;
  
  /** GUI-agnostic interactions document from the model. */
  private volatile InteractionsDocument _doc;
  
  /** Style to use for error messages. */
  private volatile SimpleAttributeSet _errStyle;
  
  /** Style to use for debug messages. */
  private final SimpleAttributeSet _debugStyle;
  
  /** Lambda used to input text into the embedded System.in input box. */
  private volatile Lambda<String, String> _insertTextCommand;
  
  /** Runnable command used to force the System.in input to complete <p>
    * <b>NOTE:</b> This command must be executed on swing's event handling thread.
    */
  private volatile Runnable _inputCompletionCommand;
  
  /** Default implementation of the insert text in input command */
  private static final Lambda<String, String> _defaultInsertTextCommand = 
    new Lambda<String,String>() {
    public String apply(String input) {
      throw new UnsupportedOperationException("Cannot insert text. There is no console input in progress");
    }
  };
  
  /** Default implementation of the input completion command */
  private static final Runnable _defaultInputCompletionCommand = 
    new Runnable() { public void run() { /* Do nothing */ } };
  
  /** A temporary variable used to hold a box allocated inside getConsoleInput below. */
  private volatile InputBox _box;
  /** A temporary variable used to hold the text fetched from _box in getConsoleInput below. */
  private volatile String _text;
  
  /** Listens for input requests from System.in, displaying an input box as needed. */
  protected volatile InputListener _inputListener = new InputListener() {
    public String getConsoleInput() {
      final CompletionMonitor completionMonitor = new CompletionMonitor();
      _box = new InputBox();
      
      // Embed the input box into the interactions pane. This operation must be performed in the UI thread
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { 
          
          // These commands only run in the event thread
          final Lambda<String,String> insertTextCommand = _box.makeInsertTextCommand();  // command for testing
          
          final Runnable inputCompletionCommand = new Runnable() {  // command for terminating each input interaction
            public void run() {
              assert EventQueue.isDispatchThread();
              // Reset the commands to their default inactive state
              _setConsoleInputCommands(_defaultInputCompletionCommand, _defaultInsertTextCommand);
              
              _box.disableInputs();
              _text = _box.getText() + "\n";
              
              /* Move the cursor back to the end of the interactions pane while preventing _doc from changing in the 
               * interim. */
              _doc.acquireReadLock();
              try { 
                _pane.setEditable(true);
                _pane.setCaretPosition(_doc.getLength()); }
              finally { _doc.releaseReadLock(); }
              _pane.requestFocusInWindow();
              
              completionMonitor.set();
            }
          };
          
          _box.setInputCompletionCommand(inputCompletionCommand);
          _setConsoleInputCommands(inputCompletionCommand, insertTextCommand);
          _pane.setEditable(true);
          
          // create an empty MutableAttributeSet for _box
          MutableAttributeSet inputAttributes = new SimpleAttributeSet();
          
          // initialize MutableAttributeSet to the attributes of the _box component
          StyleConstants.setComponent(inputAttributes, _box);
          
          /* Insert box in document. */
          _doc.acquireWriteLock();
          try {
            _doc.insertBeforeLastPrompt(" ", _doc.DEFAULT_STYLE);
            
            // bind INPUT_BOX_STYLE to inputAttributes in the associated InteractionsDJDocument 
            _swingConsoleDocument.setDocStyle(INPUT_BOX_STYLE, inputAttributes);
            
            // and insert the symbol for the input box with the correct style (identifying it as our InputBox)
            _doc.insertBeforeLastPrompt(INPUT_BOX_SYMBOL, INPUT_BOX_STYLE);
            
            _doc.insertBeforeLastPrompt("\n", _doc.DEFAULT_STYLE);
          }
          finally { _doc.releaseWriteLock(); }
          
          _box.setVisible(true);
          SwingUtilities.invokeLater(new Runnable() { public void run() { _box.requestFocusInWindow(); } });
          
          _pane.setEditable(false);
        }
      });
      fireConsoleInputStarted();
      
      // Wait for the inputCompletionCommand to be invoked
      completionMonitor.waitOne();
      
      fireConsoleInputCompleted(_text);
      
      return _text;
    }
  };
  
  private Vector<ConsoleStateListener> _consoleStateListeners;
  
  private InteractionsListener _viewListener = new InteractionsListener() {
    public void interactionStarted() { }
    public void interactionEnded() { _pane.requestFocusInWindow(); }    
    public void interactionErrorOccurred(int offset, int length) { }    
    
    public void interpreterResetting() {
      Utilities.invokeLater(new Runnable() { 
        public void run() { 
          _swingConsoleDocument.clearColoring();
//          _pane.resetPrompts();  // NOT USED
        }
      });
    }
    
    public void interpreterReady(File wd) { }
    public void interpreterResetFailed(Throwable t) { }
    public void interpreterExited(int status) { }
    public void interpreterChanged(boolean inProgress) { }
    public void interactionIncomplete() { }
    public void slaveJVMUsed() { }
  };
  
  /** Glue together the given model and a new view.
    * @param model An InteractionsModel
    * @param adapter InteractionsDJDocument being used by the model's doc
    */
  public InteractionsController(final InteractionsModel model, InteractionsDJDocument adapter) {
    this(model, adapter, new InteractionsPane(adapter) {  // creates InteractionsPane
      public int getPromptPos() { return model.getDocument().getPromptPos(); }
    });
  }
  
  /** Glue together the given model and view.
    * @param model An InteractionsModel
    * @param adapter InteractionsDJDocument being used by the model's doc
    * @param pane An InteractionsPane
    */
  public InteractionsController(InteractionsModel model, InteractionsDJDocument adapter, InteractionsPane pane) {
    super(adapter, pane);
    DefaultEditorKit d = pane.EDITOR_KIT;
    
    for (Action a : d.getActions()) {
      if (a.getValue(Action.NAME).equals(DefaultEditorKit.upAction))  defaultUpAction = a;
      if (a.getValue(Action.NAME).equals(DefaultEditorKit.downAction)) defaultDownAction = a;
    }
    
    _model = model;
    _doc = model.getDocument();
    _errStyle = new SimpleAttributeSet();
    _debugStyle = new SimpleAttributeSet();
    
    _model.setInputListener(_inputListener);
    _model.addListener(_viewListener);
    _model.setUpPane(pane);    // sets the interactions pane within the model and initializes the caret
    
    _inputCompletionCommand = _defaultInputCompletionCommand;
    _insertTextCommand = _defaultInsertTextCommand;
    _consoleStateListeners = new Vector<ConsoleStateListener>();
//    _pane.addCaretListener(new CaretListener() {  // Update the cachedCaretPosition 
//      public void caretUpdate(CaretEvent e) { 
//        _log.log("Caret Event: " + e + " from source " + e.getSource());
////        setCachedCaretPos(e.getDot()); 
//      }
//    }); 
    
    _init();  // residual superclass initialization
  }
  
  public void addConsoleStateListener(ConsoleStateListener listener) {
    _consoleStateListeners.add(listener);
  }
  
  public void removeConsoleStateListener(ConsoleStateListener listener) {
    _consoleStateListeners.remove(listener);
  }
  
  private void fireConsoleInputStarted() {
    for(ConsoleStateListener listener : _consoleStateListeners) {
      listener.consoleInputStarted(this);
    }
  }
  
  private void fireConsoleInputCompleted(String text) {
    for(ConsoleStateListener listener : _consoleStateListeners) { listener.consoleInputCompleted(text, this); }
  }
  
  /** Gets the input listener for console input requests.  ONLY used in unit tests.
    * @return the input listener for console input requests.
    */
  public InputListener getInputListener() { return _inputListener; }
  
  /** Forces console input to complete without the user hitting <Enter>.  Called by MainFrame when reset is called so 
    * that this lock is released.  This method is thread safe.
    * @throws UnsupportedOperationException If the interactions pane is not receiving console input
    */
  public void interruptConsoleInput() { SwingUtilities.invokeLater(_inputCompletionCommand); }
  
  /** Inserts text into the console.  Can only be called from the event thread.  ONLY used in unit tests.
    * @param input The text to insert into the console input box
    * @throws UnsupportedOperationException If the the interactions pane is not receiving console input
    */
  public void insertConsoleText(String input) { _insertTextCommand.apply(input); }
  
  /** Accessor method for the InteractionsModel.
    * @return the interactions model
    */
  public InteractionsModel getInteractionsModel() {  return _model; }
  
  /** Allows the abstract superclass to use the document.
    * @return the InteractionsDocument
    */
  public ConsoleDocument getConsoleDoc() { return _doc; }
  
  /** Accessor method for the InteractionsDocument. */
  public InteractionsDocument getDocument() { return _doc; }
  
  /** Adds AttributeSets as named styles to the document adapter. */
  protected void _addDocumentStyles() {
    // Add AbstractConsoleController styles
    super._addDocumentStyles();
    
    // Error
    _errStyle.addAttributes(_defaultStyle);
    _errStyle.addAttribute(StyleConstants.Foreground, 
                           DrJava.getConfig().getSetting(OptionConstants.INTERACTIONS_ERROR_COLOR));
    _errStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    _swingConsoleDocument.setDocStyle(InteractionsDocument.ERROR_STYLE, _errStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.INTERACTIONS_ERROR_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _errStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
    
    // Debug
    _debugStyle.addAttributes(_defaultStyle);
    _debugStyle.addAttribute(StyleConstants.Foreground, 
                             DrJava.getConfig().getSetting(OptionConstants.DEBUG_MESSAGE_COLOR));
    _debugStyle.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    _swingConsoleDocument.setDocStyle(InteractionsDocument.DEBUGGER_STYLE, _debugStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.DEBUG_MESSAGE_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _debugStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
  }
  
  /** Updates all document styles with the attributes contained in newSet.  This behavior is only used in Mac OS X, 
    * JDK 1.4.1, since setFont() works fine on JTextPane on all other tested platforms.
    * @param newSet Style containing new attributes to use.
    */
  protected void _updateStyles(AttributeSet newSet) {
    super._updateStyles(newSet);
    _errStyle.addAttributes(newSet);
    StyleConstants.setBold(_errStyle, true);  // ensure err is always bold
    _debugStyle.addAttributes(newSet);
    StyleConstants.setBold(_debugStyle, true);  // ensure debug is always bold
  }
  
  /** Adds listeners to the model. */
  protected void _setupModel() { _doc.setBeep(_pane.getBeep()); }
  
  /** Adds actions to the view. */
  protected void _setupView() {
    super._setupView();
    
    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    // Add actions with keystrokes
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), evalAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.Event.SHIFT_MASK), newLineAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_B, mask), clearCurrentAction);
    
    // Up and down need to be bound both for keypad and not
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), moveUpAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), moveUpAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_UP, mask), historyPrevAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), moveDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), moveDownAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, mask), historyNextAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), historyReverseSearchAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, java.awt.Event.SHIFT_MASK),
                                historyForwardSearchAction);
    
    // Left needs to be prevented from rolling cursor back before the prompt.
    // Both left and right should lock when caret is before the prompt.
    // Caret is allowed before the prompt for the purposes of mouse-based copy-
    // and-paste.
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), moveLeftAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), moveLeftAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), moveRightAction);
    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), moveRightAction);
    
    // Prevent previous word action from going past the prompt
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_WORD), prevWordAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_PREVIOUS_WORD, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_WORD), prevWordAction);
      }
    });
    
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_WORD), nextWordAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_NEXT_WORD, new OptionListener<KeyStroke>() {
      public void optionChanged(OptionEvent<KeyStroke> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_WORD), nextWordAction);
      }
    });
  }
  
  /** Sets the commands used to manipulate the console input process.  Only runs in the event thread. */
  private void _setConsoleInputCommands(Runnable inputCompletionCommand, Lambda<String,String> insertTextCommand) {
    _insertTextCommand = insertTextCommand;
    _inputCompletionCommand = inputCompletionCommand;
  }
  
  // The fields below were made package private for testing purposes.
  
  /** Evaluates the interaction on the current line. */
  AbstractAction evalAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.acquireWriteLock();
      try {
        if (! _swingConsoleDocument.inBlockComment()) {
          Thread command = new Thread("Evaluating Interaction") { 
            public void run() {  // not under a Write Lock!
              try { _model.interpretCurrentInteraction(); }
              catch (Throwable t) { DrJavaErrorHandler.record(t); }
            }
          };
          command.start();
        }
        else {
          _model._addNewline();
          _model.interactionContinues();
        }
      }
      finally { _doc.releaseWriteLock(); }
    }
  };
  
  /** Recalls the previous command from the history. */
  AbstractAction historyPrevAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        _doc.acquireWriteLock();  // recall... below acquires WriteLock!
        try {
          if (_doc.recallPreviousInteractionInHistory()) moveToEnd();
          if (!_isCursorAfterPrompt()) moveToPrompt();
        }
        finally { _doc.releaseWriteLock(); }
      }
    }
  };
  
  /** Recalls the next command from the history. */
  AbstractAction historyNextAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        _doc.acquireWriteLock();
        try { if (_doc.recallNextInteractionInHistory() || !_isCursorAfterPrompt()) moveToPrompt(); }
        finally { _doc.releaseWriteLock(); }
      }
    }
  };
  
  /** Added feature for up. If the cursor is on the first line of the current interaction, it goes into the history.
    * Otherwise, stays within the current interaction
    */
  AbstractAction moveUpAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        _doc.acquireWriteLock();
        try {
          if (_shouldGoIntoHistory(_doc.getPromptPos(), _pane.getCaretPosition())) 
            historyPrevAction.actionPerformed(e);
          else {
            defaultUpAction.actionPerformed(e);
            if (! _isCursorAfterPrompt()) moveToPrompt();
          }
        }
        finally { _doc.releaseWriteLock(); }
      }
    }
  };
  
  /** Added feature for down. If the cursor is on the last line of the current interaction, it goes into the history.
    * Otherwise, stays within the current interaction
    */
  AbstractAction moveDownAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        _doc.acquireWriteLock();
        try {
          if (_shouldGoIntoHistory(_pane.getCaretPosition(), _swingConsoleDocument.getLength())) {
            historyNextAction.actionPerformed(e);
          } else { defaultDownAction.actionPerformed(e); }
        }
        finally { _doc.releaseWriteLock(); }
      }
    }
  };
  
  /** Tests whether or not to move into the history.  Should be executed in the event thread to ensure
    * that caret and prompt positions are in consistent states.
    * @return true iff there are no "\n" characters between the start and the end
    */  
  private boolean _shouldGoIntoHistory(int start, int end) {
    if (_isCursorAfterPrompt() && end >= start) {
      String text = "";
      try { text = _swingConsoleDocument.getText(start, end - start); }
      catch(BadLocationException ble) {
        throw new UnexpectedException(ble); //The conditional should prevent this from ever happening
      }
      if (text.indexOf("\n") != -1) return false;
    }
    return true;
  }
  
  private boolean _isCursorAfterPrompt() { return _pane.getCaretPosition() >= _doc.getPromptPos(); }
  
  Action defaultUpAction;
  Action defaultDownAction;
  
  /** Reverse searches in the history. */
  AbstractAction historyReverseSearchAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (!_busy()) {
//        _doc.acquireReadLock();  // may be overkill
//        try {
        _doc.reverseSearchInteractionsInHistory();
        moveToEnd();
//        }
//        finally { _doc.releaseReadLock(); }
      }
    }
  };
  
  /** Forward searches in the history. */
  AbstractAction historyForwardSearchAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
//        _doc.acquireReadLock();  // may be overkill
//        try {
        _doc.forwardSearchInteractionsInHistory();
        moveToEnd();
//        }
//        finally { _doc.releaseReadLock(); }
      }
    }
  };
  
  /** Moves the caret left or wraps around. */
  AbstractAction moveLeftAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        _doc.acquireReadLock(); 
        try {
          int promptPos = _doc.getPromptPos();
          int pos = _pane.getCaretPosition();
          if (pos < promptPos) moveToPrompt();
          else if (pos == promptPos) moveToEnd(); // Wrap around to the end
          else {
            _pane.setCaretPosition(pos - 1); // pos > promptPos
//            setCachedCaretPos(pos - 1);
          }
        }
        finally { _doc.releaseReadLock(); }
      }
    }
  };
  
  /** Moves the caret right or wraps around. */
  AbstractAction moveRightAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.acquireReadLock();
      try {
        int pos = _pane.getCaretPosition();
        if (pos < _doc.getPromptPos()) moveToEnd();
        else if (pos >= _doc.getLength()) moveToPrompt(); // Wrap around to the star
        else {
          _pane.setCaretPosition(pos + 1); // position between prompt and end
//          setCachedCaretPos(pos + 1);
        }
      }
      finally { _doc.releaseReadLock(); }
    }
  };
  
  /** Skips back one word.  Doesn't move past the prompt. */
  AbstractAction prevWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.acquireReadLock();
      try {
        int position = _pane.getCaretPosition();
        int promptPos = _doc.getPromptPos();
        if (position < promptPos) moveToPrompt();
        else if (position == promptPos) moveToEnd(); // Wrap around to the end
        else _pane.getActionMap().get(DefaultEditorKit.previousWordAction).actionPerformed(e);
      }
      finally { _doc.releaseReadLock(); }
    }
  };
  
  /** Skips forward one word.  Doesn't move past the prompt. */
  AbstractAction nextWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      _doc.acquireReadLock();
      try {
        int position = _pane.getCaretPosition();
        int promptPos = _doc.getPromptPos();
        if (position < promptPos) moveToEnd();
        else if (position >= _doc.getLength()) moveToPrompt(); // Wrap around to the start
        else _pane.getActionMap().get(DefaultEditorKit.nextWordAction).actionPerformed(e);
      }
      finally { _doc.releaseReadLock(); }
    }
  };
  
  /** A box that can be inserted into the interactions pane for separate input.  Do not confuse with 
    * edu.rice.cs.util.swing.InputBox. */
  private static class InputBox extends JTextArea {
    private static final int BORDER_WIDTH = 1;
    private static final int INNER_BUFFER_WIDTH = 3;
    private static final int OUTER_BUFFER_WIDTH = 2;
    private volatile Color _bgColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_BACKGROUND_COLOR);
    private volatile Color _fgColor = DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR);
    private volatile Color _sysInColor = DrJava.getConfig().getSetting(OptionConstants.SYSTEM_IN_COLOR);
    private volatile boolean _antiAliasText = DrJava.getConfig().getSetting(OptionConstants.TEXT_ANTIALIAS);
    
    public InputBox() {
      setForeground(_sysInColor);
      setBackground(_bgColor);
      setCaretColor(_fgColor);
      setBorder(_createBorder());
      setLineWrap(true);
      
      DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_NORMAL_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _fgColor = oe.value;
          setBorder(_createBorder());
          setCaretColor(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_BACKGROUND_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _bgColor = oe.value;
          setBorder(_createBorder());
          setBackground(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.SYSTEM_IN_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _sysInColor = oe.value;
          setForeground(oe.value);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.TEXT_ANTIALIAS,
                                           new OptionListener<Boolean>() {
        public void optionChanged(OptionEvent<Boolean> oce) {
          _antiAliasText = oce.value.booleanValue();
          InputBox.this.repaint();
        }
      });
      
      // Add the input listener for <Shift+Enter>
      final Action newLineAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) { insert("\n", getCaretPosition()); }
      };
      
      final InputMap im = getInputMap(WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,java.awt.Event.SHIFT_MASK), INSERT_NEWLINE_NAME);
      
      final ActionMap am = getActionMap();
      am.put(INSERT_NEWLINE_NAME, newLineAction);
      
    }
    
    private Border _createBorder() {
      Border outerouter = BorderFactory.createLineBorder(_bgColor, OUTER_BUFFER_WIDTH);
      Border outer = BorderFactory.createLineBorder(_fgColor, BORDER_WIDTH);
      Border inner = BorderFactory.createLineBorder(_bgColor, INNER_BUFFER_WIDTH);
      Border temp = BorderFactory.createCompoundBorder(outer, inner);
      return BorderFactory.createCompoundBorder(outerouter, temp);
    }
    /** Enable anti-aliased text by overriding paintComponent. */
    protected void paintComponent(Graphics g) {
      if (_antiAliasText && g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      }
      super.paintComponent(g);
    }
    
    /** Specifies what to do when the <Enter> key is hit. */
    void setInputCompletionCommand(final Runnable command) {
      final InputMap im = getInputMap(WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), INPUT_ENTERED_NAME);
      
      final ActionMap am = getActionMap();
      am.put(INPUT_ENTERED_NAME, new AbstractAction() {
        public void actionPerformed(ActionEvent e) { command.run(); }
      });
    }
    
    /** Generates a lambda that can be used to insert text into this input box.  Only runs in event thread.
      * @return A lambda that inserts the given text into the textbox when applied
      */
    Lambda<String,String> makeInsertTextCommand() {
      return new Lambda<String, String>() {
        public String apply(String input) {
          insert(input, getCaretPosition());
          return input;
        }
      };
    }
    
    /** Behaves somewhat like setEnable(false) in that it disables all
      * input to the text box, but it does not change the appearance of the text.
      */
    void disableInputs() {
      setEditable(false);
      
      ActionMap am = getActionMap();
      Action action;
      
      action = am.get(INPUT_ENTERED_NAME);
      if (action != null) action.setEnabled(false);
      
      action = am.get(INSERT_NEWLINE_NAME);
      if (action != null) action.setEnabled(false);
      
      getCaret().setVisible(false);
    }
  }
  
  /** A listener interface that allows for others outside the interactions controller to be notified when the input
    * console is enabled in the interactions pane.
    */
  public interface ConsoleStateListener extends EventListener {
    
    /** Called when the input console is started in the interactions pane. <p>
      * This method is called from the thread that initiated the console input,
      */
    public void consoleInputStarted(InteractionsController c);
    
    /** Called when the console input is complete. <p>
      * This method is called from the thread that initiated the console input.
      * @param result The text that was inputted to the console
      */
    public void consoleInputCompleted(String result, InteractionsController c);
    
  }
}
