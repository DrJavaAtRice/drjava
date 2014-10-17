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

import java.awt.Toolkit;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.FocusListener;

import java.io.File;

import java.util.Vector;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.event.UndoableEditListener;
import javax.swing.event.UndoableEditEvent;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.repl.InputListener;
import edu.rice.cs.drjava.model.repl.InteractionsDocument;
import edu.rice.cs.drjava.model.repl.InteractionsDJDocument;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.drjava.model.repl.InteractionsModel;

import edu.rice.cs.drjava.config.OptionConstants;

import edu.rice.cs.util.swing.DelegatingAction;

import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.util.text.ConsoleDocument;
import edu.rice.cs.util.UnexpectedException;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
/* TODO: clean up mixed references to _adapter and _doc which point to almost the same thing (an 
 * InteractionsDJDocument versus an InteractionsDocument. */

/** This class installs listeners and actions between an InteractionsDocument (the model) and an InteractionsPane 
  * (the view).  We may want to refactor this class into a different package. <p>
  * (The PopupConsole was introduced in version 1.29 of this file and subsequently removed.)
  *
  * @version $Id: InteractionsController.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class InteractionsController extends AbstractConsoleController {
  
  /* InteractionsDocument _adapter is inherited from AbstractConsoleController. */
  /* InteractionsPane _pane is inherited from AbstractConsoleController. */
  
  private static final String INPUT_ENTERED_NAME = "Input Entered";
  private static final String INSERT_NEWLINE_NAME = "Insert Newline";
  private static final String INSERT_END_OF_STREAM = "Insert End of Stream";
  private static final String UNDO_NAME = "Undo";
  private static final String REDO_NAME = "Redo";
  
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
  
  /** Runnable command that disables the "Close System.in" menu command. */
  private final Runnable _disableCloseSystemInMenuItemCommand;
  
  /** Default implementation of the insert text in input command */
  private static final Lambda<String, String> _defaultInsertTextCommand = 
    new Lambda<String,String>() {
    public String value(String input) {
      throw new UnsupportedOperationException("Cannot insert text. There is no console input in progress");
    }
  };
  
  /** Default implementation of the input completion command */
  private static final Runnable _defaultInputCompletionCommand = 
    new Runnable() { public void run() { /* Do nothing */ } };
  
  /** A temporary variable used to hold a box allocated inside getConsoleInput below. */
  private volatile InputBox _box;
  /** A temporary variable used to hold the result fetched from _box in getConsoleInput below. */
  private volatile String _result;
  /** A variable indicating whether the input stream has been closed. */
  private volatile boolean _endOfStream = false;
  
  /** Listens for input requests from System.in, displaying an input box as needed. */
  protected volatile InputListener _inputListener = new InputListener() {
    public String getConsoleInput() {
      if (_endOfStream) return ""; // input stream has been closed, don't ask for more input
      final CompletionMonitor completionMonitor = new CompletionMonitor();
      _box = new InputBox(_endOfStream);
      // add all focus listeners to the Input Box
      for(FocusListener fl: _undoRedoInteractionFocusListeners) {
        _box.addFocusListener(fl);
      }
      
      // Embed the input box into the interactions pane. This operation must be performed in the UI thread
      EventQueue.invokeLater(new Runnable() {  // why EventQueue.invokeLater?
        public void run() { 
          
          // These commands only run in the event thread
          final Lambda<String,String> insertTextCommand = _box.makeInsertTextCommand();  // command for testing
          
          final Runnable inputCompletionCommand = new Runnable() {  // command for terminating each input interaction
            public void run() {
              assert EventQueue.isDispatchThread();
              // Reset the commands to their default inactive state
              _setConsoleInputCommands(_defaultInputCompletionCommand, _defaultInsertTextCommand);
              
              _box.disableInputs();
              _result = _box.getText();
              if (_box.wasClosedWithEnter()) {
                _result += "\n";
              }
              setEndOfStream(_box.isEndOfStream());
              
              /* Move the cursor back to the end of the interactions pane while preventing _doc from changing in the 
               * interim. */
              _pane.setEditable(true);
              _pane.setCaretPosition(_doc.getLength()); 
              _pane.requestFocusInWindow();
              
              // use undo/redo for the Interactions Pane again
              _undoAction.setDelegatee(_pane.getUndoAction());
              _redoAction.setDelegatee(_pane.getRedoAction());
              
              completionMonitor.signal();
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
          _doc.insertBeforeLastPrompt(" ", InteractionsDocument.DEFAULT_STYLE);
          
          // bind INPUT_BOX_STYLE to inputAttributes in the associated InteractionsDJDocument 
          _interactionsDJDocument.setDocStyle(INPUT_BOX_STYLE, inputAttributes);
          
          // and insert the symbol for the input box with the correct style (identifying it as our InputBox)
          _doc.insertBeforeLastPrompt(INPUT_BOX_SYMBOL, INPUT_BOX_STYLE);
          
          _doc.insertBeforeLastPrompt("\n", InteractionsDocument.DEFAULT_STYLE);
          
          _box.setVisible(true);
          EventQueue.invokeLater(new Runnable() { public void run() { _box.requestFocusInWindow(); } });
          
          _undoAction.setDelegatee(_box.getUndoAction());
          _redoAction.setDelegatee(_box.getRedoAction());
          _pane.setEditable(false);
        }
      });
      fireConsoleInputStarted();
      
      // Wait for the inputCompletionCommand to be invoked
      completionMonitor.attemptEnsureSignaled();
      
      fireConsoleInputCompleted(_result);
      
      return _result;
    }
  };
  
  private ArrayList<ConsoleStateListener> _consoleStateListeners;
  
  private InteractionsListener _viewListener = new InteractionsListener() {
    public void interactionStarted() { }
    public void interactionEnded() { _pane.requestFocusInWindow(); }    
    public void interactionErrorOccurred(int offset, int length) { }    
    
    public void interpreterResetting() {
      assert EventQueue.isDispatchThread(); 
      _interactionsDJDocument.clearColoring();
      _endOfStream = false;
    }
    
    public void interpreterReady(File wd) { }
    public void interpreterResetFailed(Throwable t) { }
    public void interpreterExited(int status) { }
    public void interpreterChanged(boolean inProgress) { }
    public void interactionIncomplete() { }
  };
  
  /** Glue together the given model and a new view.
    * @param model An InteractionsModel
    * @param adapter InteractionsDJDocument being used by the model's doc
    */
  public InteractionsController(final InteractionsModel model,
                                InteractionsDJDocument adapter,
                                Runnable disableCloseSystemInMenuItemCommand) {
    this(model, adapter, new InteractionsPane(adapter) {  // creates InteractionsPane
      public int getPromptPos() { return model.getDocument().getPromptPos(); }
    }, disableCloseSystemInMenuItemCommand);
    _undoAction.setDelegatee(_pane.getUndoAction());
    _redoAction.setDelegatee(_pane.getRedoAction());
  }
  
  /** Glue together the given model and view.
    * @param model An InteractionsModel
    * @param adapter InteractionsDJDocument being used by the model's doc
    * @param pane An InteractionsPane
    */
  public InteractionsController(InteractionsModel model,
                                InteractionsDJDocument adapter,
                                InteractionsPane pane,
                                Runnable disableCloseSystemInMenuItemCommand) {
    super(adapter, pane);
    _disableCloseSystemInMenuItemCommand = disableCloseSystemInMenuItemCommand;
    DefaultEditorKit d = InteractionsPane.EDITOR_KIT;
    
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
    _consoleStateListeners = new ArrayList<ConsoleStateListener>();
//    _pane.addCaretListener(new CaretListener() {  // Update the cachedCaretPosition 
//      public void caretUpdate(CaretEvent e) { 
//        _log.log("Caret Event: " + e + " from source " + e.getSource());
////        setCachedCaretPos(e.getDot()); 
//      }
//    });
    
    // Add key binding option listener for Input Box.
    // Done here, not in InputBox's constructor, so we only create one. Otherwise we might
    // create one per InputBox, and it would be difficult to remove them again.
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_UNDO, _keyBindingOptionListener);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_REDO, _keyBindingOptionListener);
    
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
  
  /** Sets the end of stream flag. */
  public void setEndOfStream(boolean tf) {
    _endOfStream = tf;
    if (_box != null) { _box.setEndOfStream(tf); }
    if (tf) { _disableCloseSystemInMenuItemCommand.run(); }
  }
  
  
  /** Gets the input listener for console input requests.  ONLY used in unit tests.
    * @return the input listener for console input requests.
    */
  public InputListener getInputListener() { return _inputListener; }
  
  /** Forces console input to complete without the user hitting <Enter>.  Called by MainFrame when reset is called so 
    * that this lock is released.  This method is thread safe.
    * @throws UnsupportedOperationException If the interactions pane is not receiving console input
    */
  public void interruptConsoleInput() { EventQueue.invokeLater(_inputCompletionCommand); }
  
  /** Inserts text into the console.  Can only be called from the event thread.  ONLY used in unit tests.
    * @param input The text to insert into the console input box
    * @throws UnsupportedOperationException If the the interactions pane is not receiving console input
    */
  public void insertConsoleText(String input) { _insertTextCommand.value(input); }
  
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
    _interactionsDJDocument.setDocStyle(InteractionsDocument.ERROR_STYLE, _errStyle);
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
    _interactionsDJDocument.setDocStyle(InteractionsDocument.DEBUGGER_STYLE, _debugStyle);
    DrJava.getConfig().addOptionListener(OptionConstants.DEBUG_MESSAGE_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oe) {
        _debugStyle.addAttribute(StyleConstants.Foreground, oe.value);
      }
    });
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
    
//    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), indentKeyActionTab);
//    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, java.awt.Event.SHIFT_MASK), indentKeyActionLine);

    // Potential additions: actions must be copied from DefinitionsPane
//    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke('}'), indentKeyActionCurly);
//    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke('{'), indentKeyActionOpenCurly);
//    _pane.addActionForKeyStroke(KeyStroke.getKeyStroke(':'), indentKeyActionColon);
    
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
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_PREVIOUS_WORD, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
        _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PREVIOUS_WORD), prevWordAction);
      }
    });
    
    _pane.addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_NEXT_WORD), nextWordAction);
    DrJava.getConfig().addOptionListener(OptionConstants.KEY_NEXT_WORD, new OptionListener<Vector<KeyStroke>>() {
      public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
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
    public void actionPerformed(ActionEvent e) { _model.interpretCurrentInteraction(); }
  };
  
//  /** Evaluates the current text following the prompt in the interactions document.*/
//  private void _evalCurrentInteraction() {
//    
//    if (! _interactionsDJDocument._insideBlockComment()) {
//       
//    String toEval;
//    if (_doc.inProgress()) return;  // Don't start a new interaction while one is in progress
//      
//      String text = _doc.getCurrentInteraction();
//      toEval = text.trim();
//      if (toEval.startsWith("java ")) toEval = _testClassCall(toEval);
////          System.err.println("Preparing to interpret '" + text  + "'");
//      _prepareToInterpret(text);  // Writes a newLine!
//    }
//    try { _model.interpret(toEval); }
//    catch (Throwable t) { DrJavaErrorHandler.record(t); }
//  };
//  
//  /** Performs pre-interpretation preparation of the interactions document and notifies the view.  Must run in the
//    * event thread for newline to be inserted at proper time.  Assumes that Write Lock is already held. */
//  private void _prepareToInterpret(String text) {
//    _addNewline();
//    _notifyInteractionStarted();
//    _doc.setInProgress(true);
//    _model.setAddToHistory(text); // _document.addToHistory(text);
//    //Do not add to history immediately in case the user is not finished typing when they press return
//  }
//  
//  /** Appends a newLine to _document assuming that the Write Lock is already held. Must run in the event thread. */
//  private void _addNewline() { append(StringOps.NEWLINE, InteractionsDocument.DEFAULT_STYLE); }
         
  /** Recalls the previous command from the history. */
  AbstractAction historyPrevAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        if (_doc.recallPreviousInteractionInHistory()) moveToEnd();
        if (!_isCursorAfterPrompt()) moveToPrompt();
      }
    }
  };
  
  /** Recalls the next command from the history. */
  AbstractAction historyNextAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy() && (_doc.recallNextInteractionInHistory() || !_isCursorAfterPrompt())) moveToPrompt(); 
    }
  };
  
  /** Added feature for up. If the cursor is on the first line of the current interaction, it goes into the history.
    * Otherwise, stays within the current interaction
    */
  AbstractAction moveUpAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        if (_shouldGoIntoHistory(_doc.getPromptPos(), _pane.getCaretPosition())) 
          historyPrevAction.actionPerformed(e);
        else {
          defaultUpAction.actionPerformed(e);
          if (! _isCursorAfterPrompt()) moveToPrompt();
        }
      }
    }
  };
  
  /** Added feature for down. If the cursor is on the last line of the current interaction, it goes into the history.
    * Otherwise, stays within the current interaction
    */
  AbstractAction moveDownAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        if (_shouldGoIntoHistory(_pane.getCaretPosition(), _interactionsDJDocument.getLength())) {
          historyNextAction.actionPerformed(e);
        } else { defaultDownAction.actionPerformed(e); }
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
      try { text = _interactionsDJDocument.getText(start, end - start); }
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
        _doc.reverseSearchInteractionsInHistory();
        moveToEnd();
      }
    }
  };
  
  /** Forward searches in the history. */
  AbstractAction historyForwardSearchAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        _doc.forwardSearchInteractionsInHistory();
        moveToEnd();
      }
    }
  };
  
  /** Moves the caret left or wraps around. */
  AbstractAction moveLeftAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (! _busy()) {
        int promptPos = _doc.getPromptPos();
        int pos = _pane.getCaretPosition();
        if (pos < promptPos) moveToPrompt();
        else if (pos == promptPos) moveToEnd(); // Wrap around to the end
        else _pane.setCaretPosition(pos - 1); // pos > promptPos
      }
    }
  };
  
  /** Moves the caret right or wraps around. */
  AbstractAction moveRightAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int pos = _pane.getCaretPosition();
      if (pos < _doc.getPromptPos()) moveToEnd();
      else if (pos >= _doc.getLength()) moveToPrompt(); // Wrap around to the star
      else {
        _pane.setCaretPosition(pos + 1); // position between prompt and end
//          setCachedCaretPos(pos + 1);
      }
    }
  };
  
  /** Skips back one word.  Doesn't move past the prompt. */
  AbstractAction prevWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      int promptPos = _doc.getPromptPos();
      if (position < promptPos) moveToPrompt();
      else if (position == promptPos) moveToEnd(); // Wrap around to the end
      else _pane.getActionMap().get(DefaultEditorKit.previousWordAction).actionPerformed(e);
    }
  };
  
  /** Skips forward one word.  Doesn't move past the prompt. */
  AbstractAction nextWordAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      int position = _pane.getCaretPosition();
      int promptPos = _doc.getPromptPos();
      if (position < promptPos) moveToEnd();
      else if (position >= _doc.getLength()) moveToPrompt(); // Wrap around to the start
      else _pane.getActionMap().get(DefaultEditorKit.nextWordAction).actionPerformed(e);
    }
  };
  
  /** Indents the selected text. */
  AbstractAction indentKeyActionTab = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { _pane.indent(); }
  };
   
  /** Indents in preparation for typing next line */
  AbstractAction indentKeyActionLine = new AbstractAction() {
    public void actionPerformed(ActionEvent e) { 
      _doc.append("\n", null);  // null style
      _pane.indent(Indenter.IndentReason.ENTER_KEY_PRESS); }
  };
  
  private final DelegatingAction _undoAction = new DelegatingAction();
  private final DelegatingAction _redoAction = new DelegatingAction();
  private final ArrayList<FocusListener> _undoRedoInteractionFocusListeners = new ArrayList<FocusListener>();
  
  /** Add a focus listener to the Interactions Pane and the Input Box. */
  public void addFocusListener(FocusListener listener) {
    _pane.addFocusListener(listener);
    // we need to store the focus listeners, because they need to be added to future
    // Input Boxes too.
    _undoRedoInteractionFocusListeners.add(listener);
    if (_box != null) {
      for(FocusListener fl: _undoRedoInteractionFocusListeners) {
        _box.addFocusListener(fl);
      }
    }
  }
  
  /** @return the undo action. */
  public Action getUndoAction() { return _undoAction; }
  
  /** @return the redo action. */
  public Action getRedoAction() { return _redoAction; }
 
  /** OptionListener responding to changes for the undo/redo key bindings. */
  private final OptionListener<Vector<KeyStroke>> _keyBindingOptionListener = new OptionListener<Vector<KeyStroke>>() {
    public void optionChanged(OptionEvent<Vector<KeyStroke>> oce) {
      if (_box != null) { _box.updateKeyBindings(); }
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
    private volatile boolean _endOfStream = false;
    private volatile boolean _closedWithEnter = false;
    private final InputMap _oldInputMap = new InputMap();
    
    public InputBox(boolean endOfStream) {
      _endOfStream = endOfStream;
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
      
      final InputMap im = getInputMap(WHEN_FOCUSED);
      final ActionMap am = getActionMap();
      
      // Add the input listener for <Shift+Enter> and <Cntl+Enter>
      final Action newLineAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) { insert("\n", getCaretPosition()); }
      };      
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,java.awt.Event.SHIFT_MASK), INSERT_NEWLINE_NAME);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,java.awt.Event.CTRL_MASK), INSERT_NEWLINE_NAME);      
      am.put(INSERT_NEWLINE_NAME, newLineAction);
      
      // Link undo/redo to this InputBox
      // First clone the InputMap so we can change the keystroke mappings
      if (im.keys()!=null) { // im.keys() may be null!
        for(KeyStroke ks: im.keys()) { _oldInputMap.put(ks, im.get(ks)); }
      }
      
      final UndoManager undo = new UndoManager();
      final Document doc = getDocument(); 
      
      final Action undoAction = new AbstractAction("Undo") {
        public void actionPerformed(ActionEvent e) {
          try {
            if (undo.canUndo()) { undo.undo(); }           
          }
          catch (CannotUndoException cue) { } 
          setEnabled(undo.canUndo() && isEditable());
          am.get(REDO_NAME).setEnabled(undo.canRedo() && isEditable());
        }
      };
      am.put(UNDO_NAME, undoAction);
      final Action redoAction = new AbstractAction("Redo") {
        public void actionPerformed(ActionEvent e) {
          try {
            if (undo.canRedo()) { undo.redo(); }
          }
          catch (CannotRedoException cue) { }
          undoAction.setEnabled(undo.canUndo() && isEditable());
          setEnabled(undo.canRedo() && isEditable());
        }
      };
      am.put(REDO_NAME, redoAction);
      
      updateKeyBindings();
      
      // Listen for undo and redo events
      doc.addUndoableEditListener(new UndoableEditListener() {
        public void undoableEditHappened(UndoableEditEvent evt) {
          undo.addEdit(evt.getEdit());
          undoAction.setEnabled(undo.canUndo() && isEditable());
          redoAction.setEnabled(undo.canRedo() && isEditable());
        }
      });
      undoAction.setEnabled(undo.canUndo() && isEditable());
      redoAction.setEnabled(undo.canRedo() && isEditable());
    }
    
    /** Update the key bindings for undo and redo. */
    public void updateKeyBindings() {
      // first restore old InputMap.
      final InputMap im = getInputMap(WHEN_FOCUSED);
      if (im.keys()!=null) { // im.keys() may be null!
        for(KeyStroke ks: im.keys()) { im.remove(ks); }
      }
      if (_oldInputMap.keys()!=null) { // keys() may return null!
        for(KeyStroke ks: _oldInputMap.keys()) { im.put(ks, _oldInputMap.get(ks)); }
      }
      
      for(KeyStroke ks: DrJava.getConfig().getSetting(OptionConstants.KEY_UNDO)) { im.put(ks, UNDO_NAME); }
      for(KeyStroke ks: DrJava.getConfig().getSetting(OptionConstants.KEY_REDO)) { im.put(ks, REDO_NAME); }
    }
    
    /** Returns true if this stream has been closed. */
    public boolean isEndOfStream() { return _endOfStream; }

    /** Setter for end of stream flag. */
    public void setEndOfStream(boolean tf) { _endOfStream = tf; }
    
    /** Was Enter pressed? */
    public boolean wasClosedWithEnter() { return _closedWithEnter; }
    
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
    
    /** Specifies what to do when the <Enter> or <Ctrl+D> keys are hit. */
    void setInputCompletionCommand(final Runnable command) {
      final InputMap im = getInputMap(WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), INPUT_ENTERED_NAME);
      for(KeyStroke k: DrJava.getConfig().getSetting(OptionConstants.KEY_CLOSE_SYSTEM_IN)) im.put(k, INSERT_END_OF_STREAM);
      
      final ActionMap am = getActionMap();
      am.put(INPUT_ENTERED_NAME, new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          _closedWithEnter = true; // add newline later
          command.run();
        }
      });

      // Add the input listener for <Ctrl+D>
      am.put(INSERT_END_OF_STREAM, new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          _endOfStream = true;
          command.run();
        }
      });
    }
    
    /** Generates a lambda that can be used to insert text into this input box.  Only runs in event thread.
      * @return A lambda that inserts the given text into the textbox when applied
      */
    Lambda<String,String> makeInsertTextCommand() {
      return new Lambda<String, String>() {
        public String value(String input) {
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
    
    /** @return the undo action. */
    public Action getUndoAction() { return getActionMap().get(UNDO_NAME); }
    
    /** @return the redo action. */
    public Action getRedoAction() { return getActionMap().get(REDO_NAME); }
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
