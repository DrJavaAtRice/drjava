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

import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.LinkedList;

// TODO: Check synchronization.
import edu.rice.cs.util.Pair;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.RightClickMouseAdapter;
import edu.rice.cs.util.text.SwingDocument;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.CompoundUndoManager;
import edu.rice.cs.drjava.model.definitions.DefinitionsEditorKit;
import edu.rice.cs.drjava.model.definitions.NoSuchDocumentException;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelState;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.model.debug.Breakpoint;

/** The pane in which work on a given OpenDefinitionsDocument occurs. A DefinitionsPane is tied to a single document,
 *  which cannot be changed.
 *  @version $Id$
 */
public class DefinitionsPane extends AbstractDJPane implements Finalizable<DefinitionsPane> {

  /** This field NEEDS to be set by setEditorKit() BEFORE any DefinitonsPanes are created. */
  private static DefinitionsEditorKit EDITOR_KIT;
  
  /* Minimum number of characters to trigger indent warning prompt */
  private static int INDENT_WARNING_THRESHOLD = 20000;
    
  /** Our parent window. */
  private final MainFrame _mainFrame;
  /** Our corresponding ODD */
  private final OpenDefinitionsDocument _doc;
  
  private volatile UndoAction _undoAction;
  private volatile RedoAction _redoAction;
  private volatile boolean testVariable;   //For Tests ONLY
//  private Document _defdoc;
  
  /** Flag used to determine if the user has already been warned about debugging when the document within 
   *  this defpane has been modified since its last save.
   */
  private volatile boolean _hasWarnedAboutModified = false;

//  /** Used by the centering source mechanism to ensure paints */
//  private boolean _updatePending = false;

  /** Whether to draw text as antialiased. */
  private volatile boolean _antiAliasText = false;

  /** Our current compiler error matching highlight. */
  private volatile HighlightManager.HighlightInfo _errorHighlightTag = null;

  /** Highlight painter for bookmarks. */
  static volatile ReverseHighlighter.DefaultUnderlineHighlightPainter BOOKMARK_PAINTER =
    new ReverseHighlighter.DefaultUnderlineHighlightPainter(DrJava.getConfig().getSetting(BOOKMARK_COLOR), 3);

  /** Highlight painter for find results. */
  static volatile ReverseHighlighter.DefaultUnderlineHighlightPainter[] FIND_RESULTS_PAINTERS;
  
  static {
    FIND_RESULTS_PAINTERS = new ReverseHighlighter.DefaultUnderlineHighlightPainter[FIND_RESULTS_COLORS.length+1];
    for(int i = 0; i < FIND_RESULTS_COLORS.length; ++i) {
      FIND_RESULTS_PAINTERS[i] =
        new ReverseHighlighter.DefaultUnderlineHighlightPainter(DrJava.getConfig().getSetting(FIND_RESULTS_COLORS[i]), 3);
    }
    FIND_RESULTS_PAINTERS[FIND_RESULTS_COLORS.length] =
        new ReverseHighlighter.DefaultUnderlineHighlightPainter(Color.WHITE, 0);
  }
  
  /** How many find result panels are using the highlight painters. */
  static volatile int[] FIND_RESULTS_PAINTERS_USAGE = new int[FIND_RESULTS_COLORS.length];

  /** Highlight painter for breakpoints. */
  static ReverseHighlighter.DefaultHighlightPainter BREAKPOINT_PAINTER =
    new ReverseHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));

  /** Highlight painter for disabled breakpoints. */
  static volatile ReverseHighlighter.DefaultHighlightPainter DISABLED_BREAKPOINT_PAINTER =
    new ReverseHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_DISABLED_COLOR));

  /** Highlight painter for thread's current location. */
  static volatile ReverseHighlighter.DefaultHighlightPainter THREAD_PAINTER =
    new ReverseHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(DEBUG_THREAD_COLOR));

  /** The name of the keymap added to the super class (saved so it can be removed). */
  public static final String INDENT_KEYMAP_NAME = "INDENT_KEYMAP";

  /** Updates the highlight if there is any. Not necessarily executed in event thread. */
  protected void _updateMatchHighlight() {
    int to = getCaretPosition();
    int from = _doc.balanceBackward(); //_doc()._reduced.balanceBackward();
    if (from > -1) {
      // Found a matching open brace to this close brace
      from = to - from;
      _addHighlight(from, to);
      //      Highlighter.Highlight[] _lites = getHighlighter().getHighlights();
      
      String matchText = _matchText(from);
      
      if (matchText != null) _mainFrame.updateFileTitle("Matches: " + matchText);
      else _mainFrame.updateFileTitle();
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
      _mainFrame.updateFileTitle();         
    }
  }
  
  /* Returns the text of the line where a matching open brace exists whenever the cursor is at a closing brace */
  private String _matchText(int braceIndex) {
    DJDocument doc = _doc;
    String docText;
    docText = doc.getText();
   
    if (docText.charAt(braceIndex) == '{') {//match everything before if we found a curly brace
      Character charBefore = null;
      int charBeforeIndex = braceIndex-1;
      boolean previousLine = false;
      
      if (charBeforeIndex != -1) charBefore = docText.charAt(charBeforeIndex);
      
      charBeforeIndex--;
      
      while (charBeforeIndex >= 0 && (charBefore == '\n' || charBefore == ' ')) {
        charBefore = docText.charAt(charBeforeIndex);
        if (!previousLine &&  charBefore != '\n' && charBefore != ' ') charBeforeIndex = braceIndex-1;
        if (charBefore == '\n')  previousLine = true;
        charBeforeIndex--;
      }
      
      final StringBuilder returnText = new StringBuilder(docText.substring(0, charBeforeIndex+2));          
      if (previousLine) returnText.append("...");
      returnText.append("{");
      
      int lastNewLineIndex = returnText.lastIndexOf("\n");
      return returnText.substring(lastNewLineIndex+1);
    }
    else //not a curly brace
      return null;     
  }  
    
  /** The OptionListener for DEFINITIONS_MATCH_COLOR. */
  private class MatchColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      MATCH_PAINTER = new ReverseHighlighter.DefaultHighlightPainter(oce.value);
      if (_matchHighlight != null) {
        int start = _matchHighlight.getStartOffset();
        int end = _matchHighlight.getEndOffset();
        _matchHighlight.remove();
        _addHighlight(start, end);
      }
    }
  }

  /** The OptionListener for COMPILER_ERROR_COLOR. */
  private class ErrorColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      ERROR_PAINTER = new ReverseHighlighter.DefaultHighlightPainter(oce.value);
      if (_errorHighlightTag != null) {
        int start = _errorHighlightTag.getStartOffset();
        int end = _errorHighlightTag.getEndOffset();
        _errorHighlightTag.remove();
        addErrorHighlight(start, end);
      }
    }
  }

  /** The OptionListener for BOOKMARK_COLOR. */
  private class BookmarkColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      BOOKMARK_PAINTER = new ReverseHighlighter.DefaultUnderlineHighlightPainter(oce.value, BOOKMARK_PAINTER.getThickness());
      _mainFrame.refreshBookmarkHighlightPainter();
    }
  }

  /** The OptionListener for FIND_RESULTS_COLOR. */
  private class FindResultsColorOptionListener implements OptionListener<Color> {
    private int _index;
    public FindResultsColorOptionListener(int i) { _index = i; }
    public void optionChanged(OptionEvent<Color> oce) {
      FIND_RESULTS_PAINTERS[_index] =
        new ReverseHighlighter.DefaultUnderlineHighlightPainter(oce.value, FIND_RESULTS_PAINTERS[_index].getThickness());
    }
  }

  /** The OptionListener for DEBUG_BREAKPOINT_COLOR. */
  private class BreakpointColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      BREAKPOINT_PAINTER = new ReverseHighlighter.DefaultHighlightPainter(oce.value);
      _mainFrame.refreshBreakpointHighlightPainter();
    }
  }

  /** The OptionListener for DEBUG_BREAKPOINT_DISABLED_COLOR. */
  private class DisabledBreakpointColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      DISABLED_BREAKPOINT_PAINTER =  new ReverseHighlighter.DefaultHighlightPainter(oce.value);
      _mainFrame.refreshBreakpointHighlightPainter();
    }
  }

  /** The OptionListener for DEBUG_THREAD_COLOR. */
  private static class ThreadColorOptionListener implements OptionListener<Color> {
    public void optionChanged(OptionEvent<Color> oce) {
      THREAD_PAINTER = new ReverseHighlighter.DefaultHighlightPainter(oce.value);
    }
  }

  /** The OptionListener for TEXT_ANTIALIAS. */
  private class AntiAliasOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _antiAliasText = oce.value.booleanValue();
      DefinitionsPane.this.repaint();
    }
  }

  /** Listens to any undoable events in the document, and adds them
   *  to the undo manager.  Must be done in the view because the edits are
   *  stored along with the caret position at the time of the edit.
   */
  private final UndoableEditListener _undoListener = new UndoableEditListener() {
    
    /** The function to handle what happens when an UndoableEditEvent occurs.
     *  @param e
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      UndoWithPosition undo = new UndoWithPosition(e.getEdit(), getCaretPosition());
      if (!_inCompoundEdit) {
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

  /** The menu item for the "Toggle Breakpoint" option. Stored in field so that it may be enabled and
   *  disabled depending on Debug Mode.
   */
  private volatile JMenuItem _toggleBreakpointMenuItem;

//  /** The menu item for the "Add Watch" option. Stored in field so that it may be enabled and
//   *  disabled depending on Debug Mode.
//   */
//  private JMenuItem _addWatchMenuItem;

  /** The contextual popup menu for the Definitions Pane. */
  private volatile JPopupMenu _popMenu;

  /** The mouse adapter for handling a popup menu. */
  private volatile PopupMenuMouseAdapter _popupMenuMA;

  /** Listens to caret to highlight errors as appropriate. */
  private volatile ErrorCaretListener _errorListener;

  private volatile ActionListener _setSizeListener = null;

  /** An action to handle indentation spawned by pressing the tab key. */
  private class IndentKeyActionTab extends AbstractAction {
    
    /** Handle the key typed event from the text field. */
    public void actionPerformed(ActionEvent e) {
      // The following commented out code was moved into the indent() method
      //int pos = getCaretPosition();
      //_doc().setCurrentLocation(pos);
      indent();
    }
  }

  /** Used for indent action spawned by pressing the enter key, '{', or '}'. */
  private class IndentKeyAction extends AbstractAction {
    
    /** The key string ("\n"|"{"|"}") for the key pressed that invokes this
     *  instance. Not used currently, but there for readability and possible
     *  future use, e.g., debugging add-ons or the rewrite of the indention code.
     */
    private final String _key;

    /** The default action to take when the specified key is pressed. */
    private final Action _defaultAction;

    /** Whether to perform the indent if the caret is in a String or comment. */
    private final boolean _indentNonCode;

    /** Creates an IndentKeyAction which only invokes indent if the caret is in code, and not Strings or 
     *  comments.
     */
    IndentKeyAction(String key, Action defaultAction) {
      this(key, defaultAction, false);
    }

    /** Creates a new IndentKeyAction with the specified parameters.
     *  @param key name of the key, for debugging purposes
     *  @param defaultAction action to perform in addition to indenting
     *  @param indentNonCode whether to indent Strings and comments
     */
    IndentKeyAction(String key, Action defaultAction, boolean indentNonCode) {
      _key = key;
      _defaultAction = defaultAction;
      _indentNonCode = indentNonCode;
    }

    /** This method tells what the reason should be for spawning this indent event
     *  Defaults to Indenter.OTHER
     */
    protected int getIndentReason() { return Indenter.OTHER; }

    /** Handle the "key typed" event from the text field. Calls the default action to make sure the right things
     *  happen, then makes a call to indentLine().
     */
    public void actionPerformed(ActionEvent e) {
      _defaultAction.actionPerformed(e);
      
      // Only indent if in code
      
      _doc.setCurrentLocation(getCaretPosition());
      ReducedModelState state = _doc.getStateAtCurrent();
      if (state.equals(ReducedModelState.FREE) || _indentNonCode) indent(getIndentReason());
    }
  }

  /** Special action to take care of case when tab key is pressed. */
  private volatile Action _indentKeyActionTab = new IndentKeyActionTab();

  /** Because the "default" action for the enter key is special, it must be
   *  grabbed from the Keymap using getAction(KeyStroke), which returns the
   *  "default" action for all keys which have behavior extending beyond
   *  regular text keys.
   */
  private final Action _indentKeyActionLine =
    new IndentKeyAction("\n", (Action) getActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)),
                        true /* indent non-code, too */ ) {
    /* overriding this method is important so that pressing the enter key causes
     * different indentation than pressing other keys, for bug 681203
     */
    protected int getIndentReason() { return Indenter.ENTER_KEY_PRESS; }
  };

  /** Likewise, regular text keys like '{', '}', and ':' do not have special actions that are returned by 
   *  getAction(KeyStroke). To make sure these behave right, we use getDefaultAction() instead.
   */
  private final Action _indentKeyActionSquiggly = new IndentKeyAction("}", getKeymap().getDefaultAction());
  private final Action _indentKeyActionOpenSquiggly = new IndentKeyAction("{", getKeymap().getDefaultAction());
  private final Action _indentKeyActionColon = new IndentKeyAction(":", getKeymap().getDefaultAction());

  /** Tells us whether we currently are in the middle of a CompoundEdit for regular keystrokes.
   *  Helps us with granular undo.
   */
  private volatile boolean _inCompoundEdit = false;
  private volatile int _compoundEditKey;

  /** Our keymap containing key bindings.  Takes precedence over the default map. */
  final Keymap ourMap;
  
  /** Standard Constructor.  Sets up all the defaults.
   *  @param mf the parent window
   */
  public DefinitionsPane(MainFrame mf, final OpenDefinitionsDocument doc) {
    super(new SwingDocument());
    
    _mainFrame = mf;
    
    addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {  
        _mainFrame.getModel().getDocumentNavigator().requestSelectionUpdate(doc);
      }
      public void focusLost(FocusEvent e) {  }
    });
    
    _doc = doc;  // NOTE: _doc is final
    
    // read the initial selection/scrolling values from the document
    // to be set when the pane is first notified active
    _selStart = _doc.getInitialSelectionStart();
    _selEnd = _doc.getInitialSelectionEnd();
    _savedVScroll = _doc.getInitialVerticalScroll();
    _savedHScroll = _doc.getInitialHorizontalScroll();
    
    //super.setDocument(NULL_DOCUMENT);
    _resetUndo();
    
    Font mainFont = DrJava.getConfig().getSetting(FONT_MAIN);
    setFont(mainFont);
    
    setEditable(true);
    
    // add actions for indent key
    ourMap = addKeymap(INDENT_KEYMAP_NAME, getKeymap());
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), _indentKeyActionLine);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), _indentKeyActionTab);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('}'), _indentKeyActionSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke('{'), _indentKeyActionOpenSquiggly);
    ourMap.addActionForKeyStroke(KeyStroke.getKeyStroke(':'), _indentKeyActionColon);
    setKeymap(ourMap);

//    Keymap map = ourMap;
//    KeyStroke[] ks;
//     ks = ourMap.getBoundKeyStrokes();
//     for (KeyStroke k:ks) {
//       System.out.println(k);
//     }
//     ourMap = ourMap.getResolveParent();
//     ks = ourMap.getBoundKeyStrokes();
//     for (KeyStroke k:ks) {
//       System.out.println(k);
//     }
  
//    this.setEditorKit(new StyledEditorKit());

    if (CodeStatus.DEVELOPMENT) _antiAliasText = DrJava.getConfig().getSetting(TEXT_ANTIALIAS).booleanValue();

    OptionListener<Color> temp;
    Pair<Option<Color>, OptionListener<Color>> pair;
      
    // Setup the color listeners. NOTE: the Foreground/Background listeners add themselves to DrJava.getConfig() 
    // in their own constructors. Rather than refactor it, we decided to work with that design decision.
    temp = new ForegroundColorListener(this);
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEFINITIONS_NORMAL_COLOR, temp);
    _colorOptionListeners.add(pair);
    
    temp = new BackgroundColorListener(this);
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEFINITIONS_BACKGROUND_COLOR, temp);
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

    temp = new BookmarkColorOptionListener();
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.BOOKMARK_COLOR, temp);
    _colorOptionListeners.add(pair);
    DrJava.getConfig().addOptionListener( OptionConstants.BOOKMARK_COLOR, temp);

    for(int i=0; i<FIND_RESULTS_COLORS.length; ++i) {
      temp = new FindResultsColorOptionListener(i);
      pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.FIND_RESULTS_COLORS[i], temp);
      _colorOptionListeners.add(pair);
      DrJava.getConfig().addOptionListener( OptionConstants.FIND_RESULTS_COLORS[i], temp);
    }
    
    temp = new BreakpointColorOptionListener();
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEBUG_BREAKPOINT_COLOR, temp);
    _colorOptionListeners.add(pair);
    DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_BREAKPOINT_COLOR, temp);
    
    temp = new DisabledBreakpointColorOptionListener();
    pair = new Pair<Option<Color>, OptionListener<Color>>(OptionConstants.DEBUG_BREAKPOINT_DISABLED_COLOR, temp);
    _colorOptionListeners.add(pair);
    DrJava.getConfig().addOptionListener( OptionConstants.DEBUG_BREAKPOINT_DISABLED_COLOR, temp);
    
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
    this.addMouseListener(_popupMenuMA);
    this.setHighlighter(new ReverseHighlighter());
    _highlightManager = new HighlightManager(this);

    int rate = this.getCaret().getBlinkRate();
    // Change the caret to one that doesn't remove selection highlighting when focus is lost.
    // Fixes bug #788295 "No highlight when find/replace switches docs".
    this.setCaret(new DefaultCaret() {
      public void focusLost(FocusEvent e) { setVisible(false); }
    });
    this.getCaret().setBlinkRate(rate);
//    Utilities.showDebug("DP constructor finished");    
  }
  
  /** Ends a compound edit.*/
  public void endCompoundEdit() {
    if (_inCompoundEdit) {
      CompoundUndoManager undoMan = _doc.getUndoManager();
      _inCompoundEdit = false;
      undoMan.endCompoundEdit(_compoundEditKey);
    }
  }

  /** Takes in any keyboard input, checks to see if it is in the keyToActionMap in KeybindingManager, if so 
   *  executes the action, otherwise checks if it contains the current platform's menu shortcut modifier and 
   *  if so, ignores that command (this disallows the execution of the UI's default actions such as 
   *  cut/copy/paste/select all), otherwise does whatever normally would be done.
   */
  public void processKeyEvent(KeyEvent e) {
    if (_mainFrame.getAllowKeyEvents()) {
      KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
      Action a = KeyBindingManager.Singleton.get(ks);
      // Don't perform the action if the keystroke is NULL_KEYSTROKE (generated by some Windows keys)
      if ((ks != KeyStrokeOption.NULL_KEYSTROKE) && (a != null)) {
//        System.out.println("Keystroke was null");
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
          if (e.getKeyCode() == KeyEvent.VK_ENTER) endCompoundEdit();
          
          CompoundUndoManager undoMan = _doc.getUndoManager();
//          int key = undoMan.startCompoundEdit();
//          System.out.println("supering 1 " + isAltF4);
          
          super.processKeyEvent(e);
          // We call endCompoundEdit() here because one will automatically start when processKeyEvent finishes 
          // (see the definition of _undoListener).
          endCompoundEdit();
//          undoMan.endCompoundEdit(key); //commented out because of frenchkeyboard fix
//          e.consume();
        }
        else {
          
          // The following conditional fixes bug #676586 by ignoring typed events when the meta key is down and fixes
          // bug #905405 "Undo Alt+Anything Causes Exception" by ignoring typed events when the alt key is down.
          // NOTE: no longer need to check for alt since we now only start a new compound edit if an undoable edit 
          // actually happened.
          if ((e.getModifiers() & InputEvent.META_MASK) != 0 
                // || ((e.getModifiers() & InputEvent.ALT_MASK) != 0)) // omitted for frenchkeyboard support
                && e.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            
//            System.out.println("not supering 1 " + isAltF4);
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
          
          /* If the KeyEvent is not a pressed event, process it before we do granular undo or _inCompoundEdit may 
           * get set incorrectly. This code breaks Alt-F4, and may break other system keybindings since the event 
           * is consumed by us. */
          if (e.getID() != KeyEvent.KEY_TYPED) {
            super.processKeyEvent(e);
            return;
          }
        }
        // This if statement is for tests only
        if ((e.getModifiers() & InputEvent.ALT_MASK) != 0) testVariable = true; // ALT_MASK actually pressed
        else testVariable = false;
        
        super.processKeyEvent(e);
      }
    }
  }

  /** Sets the editor kit that will be used by all DefinitionsPanes.
   *  @param editorKit The editor kit to use for new DefinitionsPanes.
   */
  public static void setEditorKit(DefinitionsEditorKit editorKit) { EDITOR_KIT = editorKit; }

  /** Enable anti-aliased text by overriding paintComponent. */
  protected void paintComponent(Graphics g) {
    if (CodeStatus.DEVELOPMENT) {
      if (_antiAliasText && g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      }
    }
    super.paintComponent(g);
  }

  /** Creates the popup menu for the DefinitionsPane. */
  private void createPopupMenu() {
    // Create the popup menu.
    _popMenu = new JPopupMenu();

    _popMenu.add(_mainFrame.cutAction);
    _popMenu.add(_mainFrame.copyAction);
    _popMenu.add(_mainFrame.pasteAction);
    _popMenu.addSeparator();

    JMenuItem indentItem = new JMenuItem("Indent Line(s)");
    indentItem.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent ae) { indent(); }
    });
    _popMenu.add(indentItem);

    JMenuItem commentLinesItem = new JMenuItem("Comment Line(s)");
    commentLinesItem.addActionListener(new AbstractAction() {
      public void actionPerformed( ActionEvent ae) {
        _mainFrame.hourglassOn();
        try{
          _doc.setCurrentLocation(getCaretPosition());
          _commentLines();
        }
        finally{ _mainFrame.hourglassOff(); }
      }
    });
    _popMenu.add(commentLinesItem);

    JMenuItem uncommentLinesItem = new JMenuItem("Uncomment Line(s)");
    uncommentLinesItem.addActionListener ( new AbstractAction() {
      public void actionPerformed( ActionEvent ae) {
        _doc.setCurrentLocation(getCaretPosition());
        _uncommentLines();
      }
    });
    _popMenu.add(uncommentLinesItem);

    /* Go to this file... */
    _popMenu.addSeparator();
    JMenuItem gotoFileUnderCursorItem = new JMenuItem("Go to File Under Cursor");
    gotoFileUnderCursorItem.addActionListener ( new AbstractAction() {
      public void actionPerformed( ActionEvent ae) {
        _doc.setCurrentLocation(getCaretPosition());
        _mainFrame._gotoFileUnderCursor();
      }
    });
    _popMenu.add(gotoFileUnderCursorItem);

    /* Toggle bookmark */
    JMenuItem toggleBookmarkItem = new JMenuItem("Toggle Bookmark");
    toggleBookmarkItem.addActionListener ( new AbstractAction() {
      public void actionPerformed( ActionEvent ae) {
        if (getSelectionStart()==getSelectionEnd()) { // nothing selected
          // Make sure that the breakpoint is set on the *clicked* line, if within a selection block.
          setCaretPosition(viewToModel(_popupMenuMA.getLastMouseClick().getPoint()));
        }
        _mainFrame.toggleBookmark();
      }
    });
    _popMenu.add(toggleBookmarkItem);
      
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
    }
  }

  /* The private MouseAdapter for responding to various clicks concerning the popup menu */
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
    }

    protected void _popupAction(MouseEvent e) {
      requestFocusInWindow();
      _popMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    public MouseEvent getLastMouseClick() { return _lastMouseClick; }
  }

  /** Comments out the lines contained within the given selection. */
  private void _commentLines() {
      _mainFrame.commentLines();
//    _doc.commentLinesInDefinitions(getSelectionStart(), getSelectionEnd());
  }

  /** Uncomments the lines contained within the given selection. */
  private void _uncommentLines() {
    _mainFrame.uncommentLines();
//    _doc.uncommentLinesInDefinitions(getSelectionStart(), getSelectionEnd()); 
  }

  /** @return the undo action. */
  public UndoAction getUndoAction() { return  _undoAction; }

  /** @return the redo action. */
  public RedoAction getRedoAction() { return  _redoAction; }

  /** Get the OpenDefinitionsDocument contained in this DefinitionsPane. */
  public OpenDefinitionsDocument getOpenDefDocument() { return _doc; }
  
  /** Get the DJDocument (OpenDefinitionsDocument) contained in this pane.
   *  Required by the super class AbstractDJPane.
   */
  public DJDocument getDJDocument() { return _doc; }

  /** Access to the pane's HighlightManager */
  public HighlightManager getHighlightManager() { return _highlightManager; }

  /** Set the caret position and also scroll to make sure the location is visible.
   *  @param pos Location to scroll to.
   */
  public void setPositionAndScroll(int pos) {
    try {
      setCaretPosition(pos);
      scrollRectToVisible(modelToView(pos));
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }

  /** Override JEditorPane's setDocument to make sure only the Document in our final OpenDefinitionsDocument 
   *  can be used.
   */
  public void setDocument(Document d) {
    if (_doc != null) {  // When can _doc be null?
      if ((d == null) || (!d.equals(_doc))) {
        throw new IllegalStateException("Cannot set the document of a DefinitionsPane to a different document.");
      }
    }
    super.setDocument(d);  // If _doc is null should we do this?
  }

  public boolean checkAltKey() { // For tests only
    return testVariable;
  }
  
  /** Add a ErrorCaretListener to this pane, keeping it accessible so its error model can be updated later. */
  public void addErrorCaretListener(ErrorCaretListener listener) {
    _errorListener = listener;
    addCaretListener(listener);
  }

  /** Gets the ErrorCaretListener for this pane. */
  public ErrorCaretListener getErrorCaretListener() { return _errorListener; }

  /** Switches the location of the error highlight in the document if there was one. Otherwise adds the 
   *  highlight. The invariant is that there are zero or one error highlights at any time.
   */
  public void addErrorHighlight(int from, int to)  {
    removeErrorHighlight();
    _errorHighlightTag = _highlightManager.addHighlight(from, to, ERROR_PAINTER);
  }

  /** Removes the previous compiler error highlight from the document after the cursor has moved. */
  public void removeErrorHighlight() {
    if (_errorHighlightTag != null) {
      _errorHighlightTag.remove();
      _errorHighlightTag = null;
    }
  }

  public boolean hasWarnedAboutModified() { return _hasWarnedAboutModified; }

  public void hasWarnedAboutModified( boolean hasWarned) {
    _hasWarnedAboutModified = hasWarned;
  }

  public void addBreakpointHighlight( Breakpoint bp ) { }

  public void removeBreakpointHighlight( Breakpoint bp) { }

  /** This instance of the scroll pane is here in order to allow for the definitions pane to save the
   *  horizontal and vertical scroll
   */
  private volatile JScrollPane _scrollPane;
  
  public void setScrollPane(JScrollPane s) { _scrollPane = s; }
  
  /** Used to save the caret position, selection, and scroll when setting the definitions pane to be inactive */
  private volatile int _savedVScroll;
  private volatile int _savedHScroll;
  private volatile int _position;
  private volatile int _selStart;
  private volatile int _selEnd;
  
  /** This function is called when the active document is changed. this function is called on the pane that is 
   * replaced by the new active pane. It allows the pane to "shutdown" when not in use.  Currently, this procedure 
   *  replaces the Definitions Document with a blank dummy document to help conserve memory (so that the pane will 
   *  not be holding onto the last reference of a definitions document not allowing it to be garbage collected)
   */
  public void notifyInactive() {
    // we catch a NoSuchDocumentException here because during a close/closeAll
    // the model closes the definitions document before the MainFrame switches
    // out the panes.  If this is the case, then the following code does not
    // need to be run.
    try {
      // Sync caret with location before switching
      getOpenDefDocument().setCurrentLocation(getCaretPosition());
      
      // Remove any error highlighting in the old def pane
      removeErrorHighlight();
      
      _position = _doc.getCurrentLocation();
      _selStart = getSelectionStart();
      _selEnd = getSelectionEnd();

      _savedVScroll = _scrollPane.getVerticalScrollBar().getValue();
      _savedHScroll = _scrollPane.getHorizontalScrollBar().getValue();

      super.setDocument(NULL_DOCUMENT);
    }
    catch(NoSuchDocumentException e) {
      // This exception was just thrown because the document was just 
      // closed and so this pane will soon be garbage collected.  
      // We don't need to do any more cleanup.
    }
  }
    
  /** This function is called when switching a pane to be the active document pane.  It allows the pane to do whatever 
   *  "startUp" is required.  Since setInactive swapped out the document for a dummy document, we need to reload the 
   *  actual document and reset its caret position to the saved location.  Only runs in event thread.
   */
  public void notifyActive() {
    assert ! _mainFrame.isVisible() || EventQueue.isDispatchThread();
    super.setDocument(_doc);
    if (_doc.getUndoableEditListeners().length == 0) _resetUndo();
    
    _doc.acquireWriteLock();
    int len = _doc.getLength();
    if (len < _position || len < _selEnd) {
      // the document changed since we're set inactive
      //so set selection to be none
      _position = len;
      _selStart = len;
      _selEnd = len;
    }
    try {
      if (_position == _selStart) {
        setCaretPosition(_selEnd);
        moveCaretPosition(_selStart);
        _doc.setCurrentLocation(_selStart);
      }
      else {
        setCaretPosition(_selStart);
        moveCaretPosition(_selEnd);
        _doc.setCurrentLocation(_selEnd);
      }
    }
    finally { _doc.releaseWriteLock(); }
    _scrollPane.getVerticalScrollBar().setValue(_savedVScroll);
    _scrollPane.getHorizontalScrollBar().setValue(_savedHScroll);
    // Explicitly set scrollbar policies fixing bug #1445898 
    _scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    _scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }
  
  public int getVerticalScroll() {
    if (getDocument() == NULL_DOCUMENT) return _savedVScroll;
    else return _scrollPane.getVerticalScrollBar().getValue();    
  }
  
  public int getHorizontalScroll() {
    if (getDocument() == NULL_DOCUMENT) return _savedHScroll;
    else return _scrollPane.getHorizontalScrollBar().getValue();
  }
  
  public int getCurrentLine() {
    try {
      int pos = getCaretPosition();
      FontMetrics metrics = getFontMetrics(getFont());
      Rectangle startRect = modelToView(pos);
      if (startRect == null) return 1;
      //top left position is (3,3), so font size<=6 will be off
      return (new Double (startRect.getY() / metrics.getHeight()).intValue() + 1);
    } catch (BadLocationException e) {
      // This shouldnt happen b/c we retrieve the caret pos before calling modelToView
      throw new UnexpectedException(e);
    }
  }

  /** Determines current line using logic in DefinitionsDocument.  Does it differ from getCurrentLine()? */
  public int getCurrentLinefromDoc() { return _doc.getCurrentLine(); }  
  
  public int getCurrentCol() { return _doc.getCurrentCol(); }
  
  public void setSize(int width, int height) {
    super.setSize(width, height);
    if (_setSizeListener != null) _setSizeListener.actionPerformed(null);
  }

  public void addSetSizeListener(ActionListener listener) { _setSizeListener = listener; }
  public void removeSetSizeListener() { _setSizeListener = null; }

  public void centerViewOnOffset(int offset) {
    assert EventQueue.isDispatchThread();
    try {
      FontMetrics metrics = getFontMetrics(getFont());
      JViewport defViewPort = _mainFrame.getDefViewport();
      double viewWidth = defViewPort.getWidth();
      double viewHeight = defViewPort.getHeight();
      // Scroll to make sure this item is visible
      // Centers the selection in the viewport
      Rectangle startRect;
      startRect = this.modelToView(offset);

      if (startRect != null) {
        int startRectX = (int) startRect.getX();
        int startRectY = (int) startRect.getY();
        startRect.setLocation(startRectX - (int)(viewWidth/2), startRectY - (int)(viewHeight/2));
        Point endPoint = new Point(startRectX+(int)(viewWidth/2),
                                   startRectY+(int)(viewHeight/2 + metrics.getHeight()/2));

        // Add the end rect onto the start rect to make a rectangle
        // that encompasses the entire selection
        startRect.add(endPoint);

        this.scrollRectToVisible(startRect);
      }
      removeSetSizeListener();

      setCaretPosition(offset);
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }

  public void centerViewOnLine(int lineNumber) {
    FontMetrics metrics = getFontMetrics(getFont());
    Point p = new Point(0, metrics.getHeight() * (lineNumber));
    int offset = this.viewToModel(p);
    this.centerViewOnOffset(offset);
  }

  /** This method overrides a broken version in JTextComponent.  It allows
   *  selection to proceed backwards as well as forwards.  If selection is backwards,
   *  then the caret will end up at the start of the selection rather than the end.
   */
  public void select(int selectionStart, int selectionEnd) {
    if (selectionStart < 0) selectionStart = 0;
    if (selectionEnd < 0) selectionEnd = 0;
    setCaretPosition(selectionStart);
    moveCaretPosition(selectionEnd);  // What about the caret position in the reduced model?  It is now inconsistent!
  }

  /** Reset the document Undo list. */
  public void resetUndo() {
    _doc.getUndoManager().discardAllEdits();

    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }

  /** Reset the document Undo list. */
  private void _resetUndo() {
    if (_undoAction == null) _undoAction = new UndoAction();
    if (_redoAction == null) _redoAction = new RedoAction();

    _doc.resetUndoManager();
    
    getDocument().addUndoableEditListener(_undoListener);
    _undoAction.updateUndoState();
    _redoAction.updateRedoState();
  }


  /** Overriding this method ensures that all new documents created in this editor pane use our editor 
   *  kit (and thus our model).
   */
  protected EditorKit createDefaultEditorKit() {
    //return _editorKit;
    return EDITOR_KIT;
  }
  
  /** Prompt the user whether or not they wish to indent, if the selection size is very large.
   *  Return true if the indent is to be completed
   *  @param selStart - the selection start
   *  @param selEnd - the selection end
   */
  protected boolean shouldIndent(int selStart, int selEnd) {
    if (selEnd > (selStart + INDENT_WARNING_THRESHOLD)) {
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
          return false;
        default:
          return true;
      }
    }
    return true;
  }
  
  
  /** Indent the given selection, for the given reason, in the current document.
   *  @param selStart - the selection start
   *  @param selEnd - the selection end
   *  @param reason - the reason for the indent
   *  @param pm - the ProgressMonitor used by the indenter
   */
  protected void indentLines(int selStart, int selEnd, int reason, ProgressMonitor pm) {
    //_mainFrame.hourglassOn();
    // final int key = _doc.getUndoManager().startCompoundEdit(); //Commented out in regards to French KeyBoard Fix
    try {
      _doc.indentLines(selStart, selEnd, reason, pm);
      endCompoundEdit();
    }
    catch (OperationCanceledException oce) {
      // if canceled, undo the indent; but first, end compound edit
      endCompoundEdit();
      _doc.getUndoManager().undo();
      // pm = null, so cancel can't be pressed
      throw new UnexpectedException(oce);
    }
    catch (RuntimeException e) {
      /* Catches the exception to turn off the the hourglass and close the compound edit before throwing out to the
       * main frame. */
      endCompoundEdit();
      throw e;
    }
    
    //_doc.setCurrentLocation(caretPos);
    setCaretPosition(_doc.getCurrentLocation());
  }
    
  /** Saved option listeners kept in this field so they can be removed for garbage collection  */
  private List<Pair<Option<Color>, OptionListener<Color>>> _colorOptionListeners = 
    new LinkedList<Pair<Option<Color>, OptionListener<Color>>>();
    
  private List<Pair<Option<Boolean>, OptionListener<Boolean>>> _booleanOptionListeners = 
    new LinkedList<Pair<Option<Boolean>, OptionListener<Boolean>>>();
  
  /** Called when the definitions pane is released from duty.  This frees up any option listeners that are holding 
   *  references to this object so this can be garbage collected.
   */
  public void close() {
    for (Pair<Option<Color>, OptionListener<Color>> p: _colorOptionListeners) {
      DrJava.getConfig().removeOptionListener(p.getFirst(), p.getSecond());
    }
    for (Pair<Option<Boolean>, OptionListener<Boolean>> p: _booleanOptionListeners) {
      DrJava.getConfig().removeOptionListener(p.getFirst(), p.getSecond());
    }
    _colorOptionListeners.clear();
    _booleanOptionListeners.clear();
    
    ourMap.removeBindings();
    removeKeymap(ourMap.getName());
    
    _popMenu.removeAll();
  }

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
        _doc.updateModifiedSinceSave();
        _mainFrame.updateFileTitle();
      }
      catch (CannotUndoException ex) {
        throw new UnexpectedException(ex);
      }
      updateUndoState();
      _redoAction.updateRedoState();
    }

    /** Updates the undo list, i.e., where we are as regards undo and redo. */
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
        _doc.updateModifiedSinceSave();
        _mainFrame.updateFileTitle();
      } catch (CannotRedoException ex) {
        throw new UnexpectedException(ex);
      }
      updateRedoState();
      _undoAction.updateUndoState();
    }

    /** Updates the redo state, i.e., where we are as regards undo and redo. */
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

  /** Wrapper for UndoableEdit that pairs UndoableEdits with their caret positions */
  private class UndoWithPosition implements UndoableEdit {
    private final UndoableEdit _undo;
    private final int _pos;

    public UndoWithPosition(UndoableEdit undo, int pos) {
      _undo = undo;
      _pos = pos;
    }

    public int getPosition() { return _pos;  }
    public boolean addEdit(UndoableEdit ue) { return _undo.addEdit(ue); }
    public boolean canRedo() { return _undo.canRedo(); }
    public boolean canUndo() { return _undo.canUndo(); }
    public void die() { _undo.die(); }
    public String getPresentationName() { return _undo.getPresentationName(); }
    public String getUndoPresentationName() { return _undo.getUndoPresentationName(); }
    public String getRedoPresentationName() { return _undo.getRedoPresentationName(); }
    public boolean isSignificant() { return _undo.isSignificant(); }

    public void redo() {
      _undo.redo();
      if (_pos > -1) setCaretPosition(_pos);
    }

    public boolean replaceEdit(UndoableEdit ue) { return _undo.replaceEdit(ue); }

    public void undo() {
      if (_pos > -1) setCaretPosition(_pos);
      _undo.undo();
    }
  }
  
  /** This list of listeners to notify when we are finalized */
  private List<FinalizationListener<DefinitionsPane>> _finalizationListeners = 
    new LinkedList<FinalizationListener<DefinitionsPane>>();
  
  /** Registers a finalization listener with the specific instance of the ddoc. NOTE: this should only be used by test 
   *  cases.  This policy ensures that we don't spring memory leaks by allowing our unit tests to keep track of 
   *  whether objects are being finalized (garbage collected).
   *  @param fl the listener to register
   */
  public void addFinalizationListener(FinalizationListener<DefinitionsPane> fl) { _finalizationListeners.add(fl); }

  public List<FinalizationListener<DefinitionsPane>> getFinalizationListeners() { return _finalizationListeners; }

  /** This method is called when this object becomes unreachable.  Since this class implements
   *  edu.rice.cs.drjava.model.Finalizable, it must notify its listeners.
   */
  protected void finalize() {
    FinalizationEvent<DefinitionsPane> fe = new FinalizationEvent<DefinitionsPane>(this);
    for (FinalizationListener<DefinitionsPane> fl: _finalizationListeners) fl.finalized(fe);
  }
}
