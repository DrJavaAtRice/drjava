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
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

// TODO: Check synchronization.
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.OperationCanceledException;
import edu.rice.cs.drjava.model.Finalizable;
import edu.rice.cs.drjava.model.FinalizationListener;
import edu.rice.cs.drjava.model.FinalizationEvent;
import edu.rice.cs.drjava.model.definitions.CompoundUndoManager;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.definitions.NoSuchDocumentException;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelState;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.model.debug.Breakpoint;
/**
 * The pane in which work on a given OpenDefinitionsDocument occurs.
 * A DefinitionsPane is tied to a single document, which cannot be
 * changed.
 * @version $Id$
 */
public class DefinitionsPane extends JTextPane implements OptionConstants, Finalizable<DefinitionsPane> {

  /**
   * This field NEEDS to be set by setEditorKit() BEFORE any DefinitonsPanes
   * are created.
   */
  private static DefinitionsEditorKit EDITOR_KIT;
    
  /**
   * Our parent window.
   */
  private MainFrame _mainFrame;
  private final OpenDefinitionsDocument _doc;
  private UndoAction _undoAction;
  private RedoAction _redoAction;
  private HighlightManager _highlightManager;
  
  
  private final StyledDocument NULL_DOCUMENT = new DefaultStyledDocument();
//  private Document _defdoc;
  
  /**
   * Flag used to determine if the user has already been warned about debugging
   * when the document within this defpane has been modified since its last save.
   */
  private boolean _hasWarnedAboutModified = false;

  /**
   * Our current paren/brace/bracket matching highlight.
   */
  private HighlightManager.HighlightInfo _matchHighlight = null;

  /**
   * Used by the centering source mechanism to ensure paints
   * Not used.
  private boolean _updatePending = false;*/

  /**
   * Whether to draw text as antialiased.
   */
  private boolean _antiAliasText = false;

  /**
   * Paren/brace/bracket matching highlight color.
   */
  public static DefaultHighlighter.DefaultHighlightPainter
    MATCH_PAINTER;

  static {
    Color highColor = DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR);

    MATCH_PAINTER =
      new DefaultHighlighter.DefaultHighlightPainter(highColor);
  }

  /**
   * Our current compiler error matching highlight.
   */
  private HighlightManager.HighlightInfo _errorHighlightTag = null;

  /**
   * Highlight painter for selected errors in the defs doc.
   */
  public static DefaultHighlighter.DefaultHighlightPainter
    ERROR_PAINTER =
    new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));

  /**
   *  Highlight painter for breakpoints
   */
  public static DefaultHighlighter.DefaultHighlightPainter
    BREAKPOINT_PAINTER =
    new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));

  /**
   * Highlight painter for thread's current location
   */
  public static DefaultHighlighter.DefaultHighlightPainter
    THREAD_PAINTER =
    new DefaultHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(DEBUG_THREAD_COLOR));

  /**
   * The name of the keymap added to the super class (saved so it can be removed)
   */
  public static String INDENT_KEYMAP_NAME = "INDENT_KEYMAP";
  
  /**
   * The OptionListener for DEFINITIONS_MATCH_COLOR
   */
  private class MatchColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      MATCH_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
      if (_matchHighlight != null) {
        int start = _matchHighlight.getStartOffset();
        int end = _matchHighlight.getEndOffset();
        _matchHighlight.remove();
        _addHighlight(start, end);
      }
    }
  }

  /**
   * The OptionListener for COMPILER_ERROR_COLOR
   */
  private class ErrorColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      ERROR_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
      if (_errorHighlightTag != null) {
        int start = _errorHighlightTag.getStartOffset();
        int end = _errorHighlightTag.getEndOffset();
        _errorHighlightTag.remove();
        addErrorHighlight(start, end);
      }
    }
  }

  /**
   * The OptionListener for DEBUG_BREAKPOINT_COLOR
   */
  private class BreakpointColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      BREAKPOINT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
    }
  }

  /**
   * The OptionListener for DEBUG_THREAD_COLOR
   */
  private class ThreadColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      THREAD_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(oce.value);
    }
  }

  /**
   * The OptionListener for TEXT_ANTIALIAS
   */
  private class AntiAliasOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _antiAliasText = oce.value.booleanValue();
    }
  }

  /**
   * Listens to any undoable events in the document, and adds them
   * to the undo manager.  Must be done in the view because the edits are
   * stored along with the caret position at the time of the edit.
   */
  private UndoableEditListener _undoListener = new UndoableEditListener() {
    /**
     * The function to handle what happens when an UndoableEditEvent occurs.
     * @param e
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      UndoWithPosition undo = new UndoWithPosition(e.getEdit(), getCaretPosition());
      _doc.getUndoManager().addEdit(undo);
      getRedoAction().setEnabled(false);
    }
  };

  /**
   * The menu item for the "Toggle Breakpoint" option. Stored in field so that it may be enabled and
   * disabled depending on Debug Mode
   */
  private JMenuItem _toggleBreakpointMenuItem;

  /**
   * The menu item for the "Add Watch" option. Stored in field so that it may be enabled and
   * disabled depending on Debug Mode
   */
  //private JMenuItem _addWatchMenuItem;

  /**
   * The contextual popup menu for the Definitions Pane.
   */
  private JPopupMenu _popMenu;

  /**
   * The mouse adapter for handling a popup menu
   */
  private PopupMenuMouseAdapter _popupMenuMA;

  /**
   * Listens to caret to highlight errors as appropriate.
   */
  private ErrorCaretListener _errorListener;

  private ActionListener _setSizeListener = null;

  /**
   * Looks for changes in the caret position to see if a paren/brace/bracket
   * highlight is needed.
   */
  private CaretListener _matchListener = new CaretListener() {
    /**
     * Checks caret position to see if it needs to set or remove a highlight
     * from the document.
     * When the cursor is immediately right of ')', '}', or ']', it highlights
     * up to the matching open paren/brace/bracket.
     * @param e the event fired by the caret position change
     */
    public void caretUpdate(CaretEvent e) {
      //_doc().setCurrentLocation(getCaretPosition());
      _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
      _removePreviousHighlight();
      _updateMatchHighlight();
    }
  };

  /**
   * Updates the highlight if there is any.
   */
  private void _updateMatchHighlight() {
    int to = getCaretPosition();
    int from = _doc.balanceBackward(); //_doc()._reduced.balanceBackward();
    if (from > -1) {
      // Found a matching open brace to this close brace
      from = to - from;
      _addHighlight(from, to);
      //      Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
    }
    // if this wasn't a close brace, check for an open brace
    else {
      // (getCaretPosition will be the start of the highlight)
      from = to;

      to = _doc.balanceForward();
      if (to > -1) {
        to = to + from;
        _addHighlight(from - 1, to);
//        Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
      }
    }
  }

  /**
   * Adds a highlight to the document.  Called by _updateMatchHighlight().
   * @param from start of highlight
   * @param to end of highlight
   */
  private void _addHighlight(int from, int to) {
    _matchHighlight = _highlightManager.addHighlight(from, to, MATCH_PAINTER);
  }

  /**
   * Removes the previous highlight so document is cleared when caret position changes.
   */
  private void _removePreviousHighlight() {
    if (_matchHighlight != null) {
      _matchHighlight.remove();
      //_highlightManager.removeHighlight((HighlightManager.HighlightInfo)_matchHighlight);
      _matchHighlight = null;
    }
  }

  /**
   * An action to handle indentation spawned by pressing the tab key.
   */
  private class IndentKeyActionTab extends AbstractAction {
    /**
     * Handle the key typed event from the text field.
     */
    public void actionPerformed(ActionEvent e) {
      //int pos = getCaretPosition();
      //_doc().setCurrentLocation(pos);
      indent();
    }
  }

  /**
   * Used for indent action spawned by pressing the enter key, '{', or '}'.
   */
  private class IndentKeyAction extends AbstractAction {
    /**
     * The key string ("\n"|"{"|"}") for the key pressed that invokes this
     * instance. Not used currently, but there for readability and possible
     * future use, e.g., debugging add-ons or the rewrite of the indention code.
     */
    private final String _key;

    /**
     * The default action to take when the specified key is pressed.
     */
    private final Action _defaultAction;

    /**
     * Whether to perform the indent if the caret is in a String or comment.
     */
    private final boolean _indentNonCode;

    /**
     * Creates an IndentKeyAction which only invokes indent if the caret
     * is in code, and not Strings or comments.
     */
    IndentKeyAction(String key, Action defaultAction) {
      this(key, defaultAction, false);
    }

    /**
     * Creates a new IndentKeyAction with the specified parameters.
     * @param key name of the key, for debugging purposes
     * @param defaultAction action to perform in addition to indenting
     * @param indentNonCode whether to indent Strings and comments
     */
    IndentKeyAction(String key, Action defaultAction, boolean indentNonCode) {
      _key = key;
      _defaultAction = defaultAction;
      _indentNonCode = indentNonCode;
    }

    /**
     * This method tells what the reason should be for spawning this indent event
     * Defaults to Indenter.OTHER
     */
    protected int getIndentReason(){
      return Indenter.OTHER;
    }

    /**
     * Handle the "key typed" event from the text field.
     * Calls the default action to make sure the right things happen, then makes
     * a call to indentLine().
     */
    public void actionPerformed(ActionEvent e) {
      _defaultAction.actionPerformed(e);

      // Only indent if in code
      _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
      ReducedModelState state = _doc.getStateAtCurrent();
      if (state.equals(ReducedModelState.FREE) || _indentNonCode) {
        indent(getIndentReason());
      }
    }
  }

  /**
   * Special action to take care of case when tab key is pressed.
   */
  private Action _indentKeyActionTab = new IndentKeyActionTab();

  /**
   * Because the "default" action for the enter key is special, it must be
   * grabbed from the Keymap using getAction(KeyStroke), which returns the
   * "default" action for all keys which have behavior extending beyond
   * regular text keys.
   */
  private Action _indentKeyActionLine =
    new IndentKeyAction("\n",
                        (Action) this.getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)),
                        true /* indent non-code, too */ ) {
    /* overriding this method is important so that pressing the enter key causes
     * different indentation than pressing other keys, for bug 681203
     */
    protected int getIndentReason(){
      return Indenter.ENTER_KEY_PRESS;
    }
  };

  /**
   * Likewise, regular text keys like '{', '}', and ':' do not have special actions
   * that are returned by getAction(KeyStroke). To make sure these behave right,
   * we use getDefaultAction() instead.
   */
  private Action _indentKeyActionSquiggly = new IndentKeyAction("}", getKeymap().getDefaultAction());
  private Action _indentKeyActionOpenSquiggly = new IndentKeyAction("{", getKeymap().getDefaultAction());
  private Action _indentKeyActionColon = new IndentKeyAction(":", getKeymap().getDefaultAction());

  /**
   * Tells us whether we currently are in the middle of a CompoundEdit for regualr keystrokes.
   * Helps us with granular undo.
   */
  private boolean _inCompoundEdit = false;
  private int _compoundEditKey;

  /**
   * Ends a compound edit.
   */
  public void endCompoundEdit() {
    if (_inCompoundEdit) {
      CompoundUndoManager undoMan = _doc.getUndoManager();
      _inCompoundEdit = false;
      undoMan.endCompoundEdit(_compoundEditKey);
    }
  }
  /**
   * Takes in any keyboard input, checks to see if it is in the keyToActionMap
   * in KeybindingManager, if so executes the action, otherwise checks if it
   * contains the current platform's menu shortcut modifier and if so, ignores
   * that command (this disallows the execution of the UI's default
   * actions such as cut/copy/paste/select all), otherwise does whatever
   * normally would be done
   */
  public void processKeyEvent(KeyEvent e) {
    KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
    Action a = KeyBindingManager.Singleton.get(ks);
    // Don't perform the action if the keystroke is NULL_KEYSTROKE,
    //  which can get generated by some Windows keys
    if ((ks != KeyStrokeOption.NULL_KEYSTROKE) && (a != null)) {
//      System.out.println("Keystroke was null");
      endCompoundEdit();
      // Performs the action a
      SwingUtilities.notifyAction(a, ks, e, e.getSource(), e.getModifiers());

      // Make sure we don't consume it again
      e.consume();
    }
    else {
      // Allows one step undoing of the keystrokes defined on the keymap (e.g. enter, tab, '{', '}', ':').
      Keymap km = getKeymap();

      if (km.isLocallyDefined(ks) || km.isLocallyDefined(KeyStroke.getKeyStroke(ks.getKeyChar()))) {
        // We're breaking up compound edits at the granularity of "enter"'s.
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          endCompoundEdit();
        }

        CompoundUndoManager undoMan = _doc.getUndoManager();
        int key = undoMan.startCompoundEdit();
//        System.out.println("supering 1 " + isAltF4);
        super.processKeyEvent(e);
        undoMan.endCompoundEdit(key);
//        e.consume();
      }
      else {
        // The following conditional fixes bug #676586 by ignoring typed events when the meta key is down
        // and fixes bug #905405 "Undo Alt+Anything Causes Exception" by ignoring typed events when
        // the alt key is down.
        if ((((e.getModifiers() & InputEvent.META_MASK) != 0) || ((e.getModifiers() & InputEvent.ALT_MASK) != 0))
              && e.getKeyCode() == KeyEvent.VK_UNDEFINED) {
//          System.out.println("not supering 1 " + isAltF4);
          return;
        }

        // The following conditional fixes ease of use issue 693253 by checking if a typed event is
        // shift-delete or shift-backspace and then performing a delete or backspace operation,
        // respectively
        if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
          int newModifiers = e.getModifiers() & ~(InputEvent.SHIFT_MASK);

          KeyStroke newKs = KeyStroke.getKeyStroke(ks.getKeyCode(), newModifiers, ks.isOnKeyRelease());
          String name = KeyBindingManager.Singleton.getName(newKs);

          if (name != null && (name.equals("Delete Previous") || name.equals("Delete Next"))) {
            endCompoundEdit();
            // We are unsure about the third and fourth arguments (e and e.getSource()); we simply
            // reuse the original values
            SwingUtilities.notifyAction(KeyBindingManager.Singleton.get(newKs), newKs, e, e.getSource(), newModifiers);
            e.consume();
//            System.out.println("not supering 2 " + isAltF4);
            return;
          }
        }

        // If the KeyEvent is not a pressed event, process it before we do
        // granular undo or _inCompoundEdit may get set incorrectly.
        // This code breaks Alt-F4, and may break other system keybindings
        // since the event is consumed by us.  For now, just check Alt-F4.
        if (e.getID() != KeyEvent.KEY_TYPED) {
//          System.out.println("supering 2 " + isAltF4);
//          boolean isAltF4 = e.getKeyCode() == KeyEvent.VK_F4 && (KeyEvent.ALT_MASK & e.getModifiers()) != 0;
//          if (!isAltF4) {
          super.processKeyEvent(e);
//            e.consume();
//          }
          return;
        }

        // backspace deletes twice without this check, overrides other keystrokes
        // that use the mask modifier
        if (((ks.getModifiers() & mask) == 0) && ks.getKeyChar() != '\b') {
          int _keyval = (int) e.getKeyChar();
          if (_keyval >= 32 && _keyval <= 126) {
            CompoundUndoManager undoMan = _doc.getUndoManager();
            if (!_inCompoundEdit) {
              _inCompoundEdit = true;
              _compoundEditKey = undoMan.startCompoundEdit();
              getUndoAction().updateUndoState();
              getRedoAction().updateRedoState();
              //super.processKeyEvent(e);
            }
//            else {
//              UndoableEdit lastEdit = undoMan.getNextUndo();
//              lastEdit.die();
//              _compoundEditKey = undoMan.startCompoundEdit();
//              super.processKeyEvent(e);
//              undoMan.addEdit(lastEdit);
//              undoMan.endCompoundEdit(_compoundEditKey);
//            }
          }
//          System.out.println("supering 3 " + isAltF4);
          super.processKeyEvent(e);
        }
//        else {
//          e.consume();
//          _inCompoundEdit = false;
//        }
      }
    }
  }

  /**
   * Sets the editor kit that will be used by all DefinitionsPanes.
   * @param editorKit The editor kit to use for new DefinitionsPanes.
   */
  public static void setEditorKit(DefinitionsEditorKit editorKit) {
    EDITOR_KIT = editorKit;
  }

  
  Keymap ourMap;
  /**
   * Constructor.  Sets up all the defaults.
   * @param mf the parent window
   */
  public DefinitionsPane(MainFrame mf,
                         OpenDefinitionsDocument doc)
  {
    super(new DefaultStyledDocument());
    _mainFrame = mf;
    
    // Start the pane out with the NULL_DOCUMENT so that
    // it doesn't start out with a reference to the defdoc
    _doc = doc;
    //super.setDocument(NULL_DOCUMENT);
    _resetUndo();
    
    setContentType("text/java");
    //setFont(new Font("Courier", 0, 12));
    Font mainFont = DrJava.getConfig().getSetting(FONT_MAIN);
    setFont(mainFont);

    //setSize(new Dimension(1024, 1000));
    setEditable(true);

    // add actions for indent key
    ourMap = addKeymap(INDENT_KEYMAP_NAME, getKeymap());
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                 _indentKeyActionLine);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
                                 _indentKeyActionTab);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('}'),
                                 _indentKeyActionSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('{'),
                                 _indentKeyActionOpenSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(':'),
                                 _indentKeyActionColon);
    setKeymap(ourMap);

    //this.setEditorKit(new StyledEditorKit());

    // Add listener that checks if position in the document has changed.
    // If it has changed, check and see if we should be highlighting matching braces.
    this.addCaretListener(_matchListener);

    if (CodeStatus.DEVELOPMENT) {
      _antiAliasText = DrJava.getConfig().getSetting(TEXT_ANTIALIAS).booleanValue();
    }

    
    OptionListener<Color> temp;
    Pair<Option<Color>, OptionListener<Color>> pair;
    
        
    // Setup the color listeners.
    // NOTE: the Foreground/Background listeners add themselves to 
    //   DrJava.getConfig() in their own constructors.
    //   Rather than refactor it, we decided to work with that 
    //   design decision.
    temp = new ForegroundColorListener(this);
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEFINITIONS_NORMAL_COLOR,temp);
    _colorOptionListeners.add(pair);
    
    temp = new BackgroundColorListener(this);
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEFINITIONS_BACKGROUND_COLOR,temp);
    _colorOptionListeners.add(pair);

    // These listeners do not register themselves in their own constructors.  We do.
    temp = new MatchColorOptionListener();
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEFINITIONS_MATCH_COLOR, temp);
    _colorOptionListeners.add(pair);
    DrJava.getConfig().addOptionListener( OptionConstants.DEFINITIONS_MATCH_COLOR, temp);
    
    temp = new ErrorColorOptionListener();
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.COMPILER_ERROR_COLOR, temp);
    _colorOptionListeners.add(pair);
    DrJava.getConfig().addOptionListener( OptionConstants.COMPILER_ERROR_COLOR, temp);
    
    temp = new BreakpointColorOptionListener();
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEBUG_BREAKPOINT_COLOR, temp);
    _colorOptionListeners.add(pair);
    DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_BREAKPOINT_COLOR, temp);
    
    temp = new ThreadColorOptionListener();
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEBUG_THREAD_COLOR, temp);
    _colorOptionListeners.add(pair);
    DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_THREAD_COLOR, temp);

    if (CodeStatus.DEVELOPMENT) {
      OptionListener<Boolean> aaTemp = new AntiAliasOptionListener();
      Pair<Option<Boolean>, OptionListener<Boolean>> aaPair = new Pair<Option<Boolean>, OptionListener<Boolean>>(OptionConstants.TEXT_ANTIALIAS, aaTemp);
      _booleanOptionListeners.add(aaPair);
      DrJava.getConfig().addOptionListener( OptionConstants.TEXT_ANTIALIAS, aaTemp);
    }

    createPopupMenu();

    //Add listener to components that can bring up popup menus.
    _popupMenuMA = new PopupMenuMouseAdapter();
    this.addMouseListener( _popupMenuMA );

    _highlightManager = new HighlightManager(this);

    int rate = this.getCaret().getBlinkRate();
    // Change the caret to one that doesn't remove selection highlighting when focus is lost.
    // Fixes bug #788295 "No highlight when find/replace switches docs".
    this.setCaret(new DefaultCaret() {
      public void focusLost(FocusEvent e) {
        setVisible(false);
      }
    });
    this.getCaret().setBlinkRate(rate);

  }

  /**
   * Enable anti-aliased text by overriding paintComponent.
   */
  protected void paintComponent(Graphics g) {
    if (CodeStatus.DEVELOPMENT) {
      if (_antiAliasText && g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      }
    }
    super.paintComponent(g);
  }

  /**
   * Be sure to update the document (and thus the reduced model) any time
   * the caret position changes.
   * @param pos
   */
  public void setCaretPosition(int pos) {
    super.setCaretPosition(pos);
    _doc.syncCurrentLocationWithDefinitions(pos);
//    _doc.setCurrentLocation(pos);
  }

  /**
   *  Creates the popup menu for the DefinitionsPane
   */
  private void createPopupMenu() {
    // Create the popup menu.
    _popMenu = new JPopupMenu();

    _popMenu.add(_mainFrame.cutAction);
    _popMenu.add(_mainFrame.copyAction);
    _popMenu.add(_mainFrame.pasteAction);
    _popMenu.addSeparator();

    JMenuItem indentItem = new JMenuItem("Indent Line(s)");
    indentItem.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) {
        indent();
      }
    });
    _popMenu.add(indentItem);

    JMenuItem commentLinesItem = new JMenuItem("Comment Line(s)");
    commentLinesItem.addActionListener ( new AbstractAction() {
      public void actionPerformed( ActionEvent ae) {
        _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
        _commentLines();
      }
    });
    _popMenu.add(commentLinesItem);

    JMenuItem uncommentLinesItem = new JMenuItem("Uncomment Line(s)");
    uncommentLinesItem.addActionListener ( new AbstractAction() {
      public void actionPerformed( ActionEvent ae) {
        _doc.syncCurrentLocationWithDefinitions(getCaretPosition());
        _uncommentLines();
      }
    });
    _popMenu.add(uncommentLinesItem);

    if (_mainFrame.getModel().getDebugger().isAvailable()) {
      _popMenu.addSeparator();

      // Breakpoint
      JMenuItem breakpointItem = new JMenuItem("Toggle Breakpoint");
      breakpointItem.addActionListener( new AbstractAction() {
        public void actionPerformed( ActionEvent ae ) {
          // Make sure that the breakpoint is set on the *clicked* line, if within a selection block.
          setCaretPosition(viewToModel(_popupMenuMA.getLastMouseClick().getPoint()));
          _mainFrame.debuggerToggleBreakpoint();
        }
      });
      _toggleBreakpointMenuItem = _popMenu.add(breakpointItem);
      _toggleBreakpointMenuItem.setEnabled(false);
    }


  }

  /*
   * The private MouseAdapter for responding to various clicks concerning the popup menu
   */
  private class PopupMenuMouseAdapter extends RightClickMouseAdapter {

    private MouseEvent _lastMouseClick = null;

    public void mousePressed(MouseEvent e) {
      super.mousePressed(e);

      _lastMouseClick = e;

      endCompoundEdit();

      // if not in the selected area,
      if ((viewToModel(e.getPoint()) < getSelectionStart()) ||
          (viewToModel(e.getPoint()) > getSelectionEnd()) ) {
        //move caret to clicked position, deselecting previous selection
        setCaretPosition(viewToModel(e.getPoint()));
      }

      //Don't show the "Toggle Breakpoint" option in the contextual menu, if the JMenuItem is null.
      if (_toggleBreakpointMenuItem != null) {
        _toggleBreakpointMenuItem.setEnabled(_mainFrame.inDebugMode());
      }
      //Don't show the "Add Watch" option in the contextual menu, if the JMenuItem is null.
      //if (_addWatchMenuItem != null) {
      //  _addWatchMenuItem.setEnabled(_mainFrame.inDebugMode());
      //}
    }

    protected void _popupAction(MouseEvent e) {
      requestFocus();
      _popMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    public MouseEvent getLastMouseClick() {
      return _lastMouseClick;
    }
  }

  /**
   *  Comments out the lines contained within the given selection.
   */
  private void _commentLines() {
    _doc.commentLinesInDefinitions(getSelectionStart(), getSelectionEnd());
  }

  /**
   *  Uncomments the lines contained within the given selection.
   */
  private void _uncommentLines() {
    _doc.uncommentLinesInDefinitions(getSelectionStart(), getSelectionEnd());
  }


  /**
   * @return the undo action
   */
  public UndoAction getUndoAction() {
    return  _undoAction;
  }

  /**
   * @return the redo action
   */
  public RedoAction getRedoAction() {
    return  _redoAction;
  }

  /**
   * Get the OpenDefinitionsDocument contained in this DefinitionsPane.
   */
  public OpenDefinitionsDocument getOpenDocument() {
    return _doc;
  }

  /**
   * Access to the pane's HighlightManager
   */
  public HighlightManager getHighlightManager() {
    return _highlightManager;
  }

  /**
   * Set the caret position and also scroll to make sure the location is
   * visible.
   * @param pos Location to scroll to.
   */
  public void setPositionAndScroll(int pos) {
    try {
      setCaretPosition(pos);
      scrollRectToVisible(modelToView(pos));
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }


  /**
   * Override JEditorPane's setDocument to make sure only
   * the Document in our final OpenDefinitionsDocument can
   * be used.
   */
  public void setDocument(Document doc){
    if (_doc != null) {
      if ((doc == null) || (!doc.equals(_doc))) {
        throw new IllegalStateException("Cannot set the document of " +
                                        "a DefinitionsPane to a " +
                                        "different document.");
      }
    }
    super.setDocument(doc);
  }

  /**
   * Add a ErrorCaretListener to this pane, keeping it
   * accessible so its error model can be updated later.
   */
  public void addErrorCaretListener(ErrorCaretListener listener) {
    _errorListener = listener;
    addCaretListener(listener);
  }

  /**
   * Gets the ErrorCaretListener for this pane.
   */
  public ErrorCaretListener getErrorCaretListener() {
    return _errorListener;
  }

  /**
   * Switches the location of the error highlight in the document if there
   * was one. Otherwise adds the highlight. The invariant is that there are
   * zero or one error highlights at any time.
   */
  public void addErrorHighlight(int from, int to)  {
    removeErrorHighlight();
    _errorHighlightTag = _highlightManager.addHighlight(from, to, ERROR_PAINTER);
  }

  /**
   * Removes the previous compiler error highlight from the document after
   * the cursor has moved.
   */
  public void removeErrorHighlight() {
    if (_errorHighlightTag != null) {
      _errorHighlightTag.remove();
      _errorHighlightTag = null;
    }
  }

  public boolean hasWarnedAboutModified() {
    return _hasWarnedAboutModified;
  }

  public void hasWarnedAboutModified( boolean hasWarned) {
    _hasWarnedAboutModified = hasWarned;
  }

  public void addBreakpointHighlight( Breakpoint bp ) {
    /*
     int lineStart = getStartPosFromLineNumber(bp.getLineNumber());
     int lineEnd = _doc.getLineEndPos(lineStart);

     _highlightManager.addHighlight(lineStart, lineEnd, _breakpointHighlighter);
     */
  }

  public void removeBreakpointHighlight( Breakpoint bp) {

  }
  
  /**
   * Reset undo machinery on setDocument.
   */
  private void setDocument(OpenDefinitionsDocument doc) {
    super.setDocument(doc);
    _resetUndo();
  }

  
  /**
   * This instance of the scroll pane is here in order
   * to allow for the definitions pane to save the
   * horizontal and vertical scroll
   */
  private JScrollPane _scrollPane;
  
  public void setScrollPane(JScrollPane s){
    _scrollPane = s;
  }
  
  
  /**
   * used to save the caret position, selection, and scroll
   * when setting the definitions pane to be inactive
   */
  
  private int _saved_vert_pos;
  private int _saved_horz_pos;
  private int _position;
  private int _selStart;
  private int _selEnd;
  
  /**
   * This function is called when the active document is changed. this function
   * is called on the pane that is replaced by the new active pane. it allows
   * the pane to "shutdown" when not in use.
   * currently, this procedure replaces the Definitions Document with a blank dummy
   * document to help conserve memory (so that the pane will not be holding onto the last
   * reference of a definitions document not allowing it to be garbage collected)
   */
  public void notifyInactive(){
    // we catch a NoSuchDocumentException here because during a close/closeAll
    // the model closes the definitions document before the MainFrame switches
    // out the panes.  If this is the case, then the following code does not
    // need to be run.
    try {
      // Sync caret with location before switching
      getOpenDocument().syncCurrentLocationWithDefinitions(getCaretPosition());
      
      
      // Remove any error highlighting in the old def pane
      removeErrorHighlight();
      
      _position = _doc.getCurrentDefinitionsLocation();
      _selStart = getSelectionStart();
      _selEnd = getSelectionEnd();

      _saved_vert_pos = _scrollPane.getVerticalScrollBar().getValue();
      _saved_horz_pos = _scrollPane.getHorizontalScrollBar().getValue();

      super.setDocument(NULL_DOCUMENT);
    }
    catch(NoSuchDocumentException e) {
      // This exception was just thrown because the document was just 
      // closed and so this pane will soon be garbage collected.  
      // We don't need to do any more cleanup.
    }
  }
    
  /**
   * this function is called when switching a pane to be the active document pane.
   * it allows the pane to do any "startup" it needs to. since setInactive swapped
   * out the document for a dummy document, we need to reload the actual document
   * and reset its caret position to the saved location.
   */
  public void notifyActive() {
    super.setDocument(_doc);
    if(_doc.getUndoableEditListeners().length == 0){
      _resetUndo();
    }
    setCaretPosition(_position);
    if(_position == _selStart){
      setCaretPosition(_selEnd);
      moveCaretPosition(_selStart);
    }else{
      setCaretPosition(_selStart);
      moveCaretPosition(_selEnd);
    }
    _doc.syncCurrentLocationWithDefinitions(_position);
    _scrollPane.getVerticalScrollBar().setValue(_saved_vert_pos);
    _scrollPane.getHorizontalScrollBar().setValue(_saved_horz_pos);
  }
  
  
  public int getCurrentLine() {
    try {
      int pos = getCaretPosition();
      FontMetrics metrics = getFontMetrics(getFont());
      Rectangle startRect = modelToView(pos);
      if (startRect == null) {
        return 1;
      }
      //top left position is (3,3), so font size<=6 will be off
      return (new Double (startRect.getY() / metrics.getHeight()).intValue() + 1);
    } catch (BadLocationException e) {
      // This shouldnt happen b/c we retrieve the caret pos before calling
      // modelToView
      throw new UnexpectedException(e);
    }
  }

  public int getCurrentCol() {
    return _doc.getCurrentCol();
  }
  public void setSize(int width, int height) {
    super.setSize(width, height);
    if (_setSizeListener != null) {
      _setSizeListener.actionPerformed(null);
    }
  }

  public void addSetSizeListener(ActionListener listener) {
    _setSizeListener = listener;
  }
  public void removeSetSizeListener() {
    _setSizeListener = null;
  }

  public void centerViewOnOffset(int offset) {
    try {
      FontMetrics metrics = getFontMetrics(getFont());
      double viewWidth = _mainFrame.getDefViewport().getWidth();
      double viewHeight = _mainFrame.getDefViewport().getHeight();
      // Scroll to make sure this item is visible
      // Centers the selection in the viewport
      Rectangle startRect;
      startRect = this.modelToView(offset);

      if (startRect != null) {
        int startRectX = (int)startRect.getX();
        int startRectY = (int)startRect.getY();
        startRect.setLocation(startRectX-(int)(viewWidth/2),
                              startRectY-(int)(viewHeight/2));
        Point endPoint = new Point(startRectX+(int)(viewWidth/2),
                                   startRectY+(int)(viewHeight/2 +
                                                    metrics.getHeight()/2));

        // Add the end rect onto the start rect to make a rectangle
        // that encompasses the entire selection
        startRect.add(endPoint);

        this.scrollRectToVisible(startRect);
      }
      removeSetSizeListener();

      setCaretPosition(offset);
    }

    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  public void centerViewOnLine(int lineNumber) {
    FontMetrics metrics = getFontMetrics(getFont());
    Point p = new Point(0, metrics.getHeight() * (lineNumber));
    int offset = this.viewToModel(p);
    this.centerViewOnOffset(offset);
  }

  /**
   * This method overrides a broken version in JTextComponent.  It allows
   * selection to proceed backwards as well as forwards.  If selection is backwards,
   * then the caret will end up at the start of the selection rather than the end.
   */
  public void select(int selectionStart, int selectionEnd){
    if (selectionStart < 0){
      selectionStart = 0;
    }
    if (selectionEnd < 0) {
      selectionEnd = 0;
    }
    setCaretPosition(selectionStart);
    moveCaretPosition(selectionEnd);
  }

  /**
   * Reset the document Undo list.
   */
  public void resetUndo() {
    _doc.getUndoManager().discardAllEdits();

    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }

  /**
   * Reset the document Undo list.
   */
  private void _resetUndo() {
    if (_undoAction == null) {
      _undoAction = new UndoAction();
    }
    if (_redoAction == null) {
      _redoAction = new RedoAction();
    }

    _doc.resetUndoManager();
    
    getDocument().addUndoableEditListener(_undoListener);
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }


  /**
   * Overriding this method ensures that all new documents created in this
   * editor pane use our editor kit (and thus our model).
   */
  protected EditorKit createDefaultEditorKit() {
    //return _editorKit;
    return EDITOR_KIT;
  }

  /**
   * Runs indent(int) with a default value of Indenter.OTHER
   */
  public void indent(){
    indent(Indenter.OTHER);
  }

  /**
   * Perform an indent either on the current line or on the given
   * selected box of text.  Calls are sent to GlobalModel which are then
   * forwarded on to DefinitionsDocument.  Hopefully the indent code
   * will be fixed and corrected so this doesn't look so ugly.
   * The purpose is to divorce the pane from the document so we can just
   * pass a document to DefinitionsPane and that's all it cares about.
   * @param reason the action that spawned this indent action.  Enter presses
   * are special, so that stars are inserted when lines in a multiline comment
   * are broken up.
   */
  public void indent(final int reason) {

    /**
     * Because indent() is a function called directly by the Keymap,
     * it does not go through the regular insertString channels and thus
     * it may not be in sync with the document's position.  For that
     * reason, we must sync the document with the pane before we go
     * ahead and indent.
     * old: _doc().setCurrentLocation(getCaretPosition());
     * new:
     */
    _doc.syncCurrentLocationWithDefinitions(getCaretPosition());

    final int selStart = getSelectionStart();
    final int selEnd = getSelectionEnd();

    //    final SwingWorker worker = new SwingWorker() {
    //      public Object construct() {

    //        // Use a progress monitor to show a progress dialog only if necessary.
    ProgressMonitor pm = null;
    //= new ProgressMonitor(_mainFrame, "Indenting...",
    //                    null, 0, selEnd - selStart);

    //pm.setProgress(0);
    // 3 seconds before displaying the progress bar.
    //pm.setMillisToDecideToPopup(3000);

    // XXX: Temporary hack because of slow indent...
    //  Prompt if more than 10000 characters to be indented
    boolean doIndent = true;
    if (selEnd > (selStart + 10000)) {
      Object[] options = {"Yes", "No"};
      int n = JOptionPane.showOptionDialog
        (_mainFrame,
         "Re-indenting this block may take a very long time.  Are you sure?",
         "Confirm Re-indent",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         options,
         options[1]);
      switch (n) {
        case JOptionPane.CANCEL_OPTION:
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.NO_OPTION:
          doIndent = false;
          break;
        default:
          doIndent = true;
          break;
      }
    }

    // Do the indent
    if (doIndent) {
      _mainFrame.hourglassOn();
      final int key = _doc.getUndoManager().startCompoundEdit();
      try {
        _doc.indentLinesInDefinitions(selStart, selEnd, reason, pm);
        //      _indentLines(reason, pm);
        _doc.getUndoManager().endCompoundEdit(key);
      }
      catch (OperationCanceledException oce) {
        // if canceled, undo the indent; but first, end compound edit
        _doc.getUndoManager().endCompoundEdit(key);
        _doc.getUndoManager().undo(key);
        // pm = null, so cancel can't be pressed
        throw new UnexpectedException(oce);
      }
      catch (RuntimeException e) {
        //catches the exception to turn off the the hourglass
        //and close the compound edit before throwing out to
        //the main frame.
        _mainFrame.hourglassOff();
        //pm.close();
        _doc.getUndoManager().endCompoundEdit(key);
        throw e;
      }

      //_doc.syncCurrentLocationWithDefinitions(caretPos);
      setCaretPosition(_doc.getCurrentDefinitionsLocation());
      _mainFrame.hourglassOff();
      //pm.close();

      //        return null;
      //      }
      //    };
      //    worker.start();
    }
  }

//  protected void finalize() throws Throwable {
//    System.err.println("finalizing DefinitionsPane: " + _doc);
//  }
    
  /**
   * Updates the UI to a new look and feel.
   * Need to update the contained popup menu as well.
   *
   * Currently, we don't support changing the look and feel
   * on the fly, so this is disabled.
   *
   public void updateUI() {
   super.updateUI();
   if (_popMenu != null) {
   SwingUtilities.updateComponentTreeUI(_popMenu);
   }
   }*/

  /**
   * Saved option listeners kept in this field so they can
   * be removed for garbage collection 
   */
  private List<Pair<Option<Color>, OptionListener<Color>>> _colorOptionListeners = 
    new LinkedList<Pair<Option<Color>, OptionListener<Color>>>();
    
  private List<Pair<Option<Boolean>, OptionListener<Boolean>>> _booleanOptionListeners = 
    new LinkedList<Pair<Option<Boolean>, OptionListener<Boolean>>>();
  
  /**
   * Called when the definitions pane is released from duty.  This
   * frees up any option listeners that are holding references to
   * this object so this can be garbage collected.
   */
  public void close(){
    for(Pair<Option<Color>, OptionListener<Color>> p: _colorOptionListeners){
      DrJava.getConfig().removeOptionListener(p.getFirst(), p.getSecond());
    }
    for(Pair<Option<Boolean>, OptionListener<Boolean>> p: _booleanOptionListeners){
      DrJava.getConfig().removeOptionListener(p.getFirst(), p.getSecond());
    }
    _colorOptionListeners.clear();
    _booleanOptionListeners.clear();
    
    ourMap.removeBindings();
    removeKeymap(ourMap.getName());
    
    _popMenu.removeAll();
  }
  
  
  
  
  
  /**
   * The undo action.
   */
  private class UndoAction extends AbstractAction {
    /**
     * Constructor.
     */
    private UndoAction() {
      super("Undo");
      setEnabled(false);
    }

    /**
     * What to do when user chooses to undo.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        //        UndoableEdit edit = _doc.getNextUndo();
        //         int pos = -1;
        //         if (edit != null && edit instanceof UndoWithPosition) {
        //           pos = ((UndoWithPosition)edit).getPosition();
        //         }
        //
        //         if (pos > -1) {
        //           //centerViewOnOffset(pos);
        //           setCaretPosition(pos);
        //         }
        _doc.getUndoManager().undo();
        _doc.setModifiedSinceSave();
        _mainFrame.updateFileTitle();
      }
      catch (CannotUndoException ex) {
        throw new UnexpectedException(ex);
      }
      updateUndoState();
      _redoAction.updateRedoState();
    }


    /**
     * Updates the undo list, i.e., where we are as regards undo and redo.
     */
    protected void updateUndoState() {
      if (_doc.undoManagerCanUndo()) {
        setEnabled(true);
        putValue(Action.NAME, _doc.getUndoManager().getUndoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Undo");
      }
    }
  }

  /**
   * Redo action.
   */
  private class RedoAction extends AbstractAction {

    /**
     * Constructor.
     */
    private RedoAction() {
      super("Redo");
      setEnabled(false);
    }

    /**
     * In the event that the user chooses to redo something, this is what's called.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      try {
        //        UndoableEdit edit = _doc.getNextRedo();
        //         int pos = -1;
        //         if (edit instanceof UndoWithPosition) {
        //           pos = ((UndoWithPosition)edit).getPosition();
        //         }
        _doc.getUndoManager().redo();

        //         if (pos > -1) {
        //           //centerViewOnOffset(pos);
        //           setCaretPosition(pos);
        //         }
        _doc.setModifiedSinceSave();
        _mainFrame.updateFileTitle();
      } catch (CannotRedoException ex) {
        throw new UnexpectedException(ex);
      }
      updateRedoState();
      _undoAction.updateUndoState();
    }

    /**
     * Updates the redo state, i.e., where we are as regards undo and redo.
     */
    protected void updateRedoState() {
      if (_doc.undoManagerCanRedo()) {
        setEnabled(true);
        putValue(Action.NAME, _doc.getUndoManager().getRedoPresentationName());
      }
      else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }

  /**
   * Wrapper for UndoableEdit that pairs UndoableEdits with their
   * caret positions
   */
  private class UndoWithPosition implements UndoableEdit {
    private UndoableEdit _undo;
    private int _pos;

    public UndoWithPosition(UndoableEdit undo, int pos) {
      _undo = undo;
      _pos = pos;
    }

    public int getPosition() {
      return _pos;
    }

    public boolean addEdit(UndoableEdit ue) {
      return _undo.addEdit(ue);
    }

    public boolean canRedo() {
      return _undo.canRedo();
    }

    public boolean canUndo() {
      return _undo.canUndo();
    }

    public void die() {
      _undo.die();
    }

    public String getPresentationName() {
      return _undo.getPresentationName();
    }

    public String getUndoPresentationName() {
      return _undo.getUndoPresentationName();
    }

    public String getRedoPresentationName() {
      return _undo.getRedoPresentationName();
    }

    public boolean isSignificant() {
      return _undo.isSignificant();
    }

    public void redo() {
      _undo.redo();
      if(_pos > -1) {
        setCaretPosition(_pos);
      }
    }

    public boolean replaceEdit(UndoableEdit ue) {
      return _undo.replaceEdit(ue);
    }

    public void undo() {
      if(_pos > -1) {
        setCaretPosition(_pos);
      }
      _undo.undo();
    }
  }
  
  
  
  /**
   * This list of listeners to notify when we are finalized
   */
  private List<FinalizationListener<DefinitionsPane>> _finalizationListeners = 
    new LinkedList<FinalizationListener<DefinitionsPane>>();
  
  /**
   * Registers a finalization listener with the specific instance of the ddoc
   * <p><b>NOTE:</b><i>This should only be used by test cases.  This is to ensure that
   * we don't spring memory leaks by allowing our unit tests to keep track of 
   * whether objects are being finalized (garbage collected)</i></p>
   * @param fl the listener to register
   */
  public void addFinalizationListener(FinalizationListener<DefinitionsPane> fl) {
    _finalizationListeners.add(fl);
  }

  public List<FinalizationListener<DefinitionsPane>> getFinalizationListeners(){
    return _finalizationListeners;
  }

  /**
   * This is called when this method is GC'd.  Since this class implements
   * edu.rice.cs.drjava.model.Finalizable, it must notify its listeners
   */
  protected void finalize() {
    FinalizationEvent<DefinitionsPane> fe = new FinalizationEvent<DefinitionsPane>(this);
    for(FinalizationListener<DefinitionsPane> fl: _finalizationListeners) {
      fl.finalized(fe);
    }
  }
}
