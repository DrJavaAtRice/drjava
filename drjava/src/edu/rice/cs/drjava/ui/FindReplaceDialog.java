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

import java.util.Hashtable;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.EmptyBorder;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FindReplaceMachine;
import edu.rice.cs.drjava.model.FindResult;

import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.text.SwingDocument;
import edu.rice.cs.util.UnexpectedException;


/** The tabbed panel that handles requests for finding and replacing text.
 *  (Used to be a dialog box, hence the name. We should fix this.)
 *  @version $Id$
 */
class FindReplaceDialog extends TabbedPanel implements OptionConstants {
  private JButton _findNextButton;
  private JButton _findPreviousButton;
  private JButton _replaceButton;
  private JButton _replaceFindNextButton;
  private JButton _replaceFindPreviousButton;
  private JButton _replaceAllButton;
  private JLabel _findLabelTop;
  private JLabel _findLabelBot;
  private JLabel _replaceLabelTop;
  private JLabel _replaceLabelBot;
  private JTextPane _findField = new JTextPane(new DefaultStyledDocument());
  private BorderlessScrollPane _findPane = new BorderlessScrollPane(_findField);
  private JTextPane _replaceField = new JTextPane(new SwingDocument());
  private BorderlessScrollPane _replacePane = new BorderlessScrollPane(_replaceField);
  private JLabel _message; // JL
  private JPanel _labelPanel;
  private JCheckBox _ignoreCommentsAndStrings;
  private JCheckBox _matchCase;
  private JCheckBox _searchAllDocuments;
//  private ButtonGroup _radioButtonGroup;
  private JPanel _lowerCheckPanel;
  private JCheckBox _matchWholeWord; // private JRadioButton _matchWholeWord; // JL
//  private JRadioButton _findAnyOccurrence; // JL
  private JPanel _matchCaseAndAllDocsPanel;
  private JPanel _rightPanel;
  private FindReplaceMachine _machine;
  private SingleDisplayModel _model;
  private DefinitionsPane _defPane = null;
  private boolean _caretChanged;
  
  /** Listens for changes to the cursor position in order to reset the start position */
  private CaretListener _caretListener = new CaretListener() {
    public void caretUpdate(CaretEvent e) {
//      Utilities.invokeLater(new Runnable() {
//        public void run() {
          _replaceAction.setEnabled(false);
          _replaceFindNextAction.setEnabled(false);
          _replaceFindPreviousAction.setEnabled(false);
          _machine.positionChanged();
          _caretChanged = true;
//        }
//      });
    }
  };
  
  //Action to replace the newLine defaultAction when pressing the Enter key inside the _findField.
  private Action _findEnterAction = new TextAction ("Find on Pressing Enter") {    
    public void actionPerformed(ActionEvent ae) { _doFind(); }
  };
  
  
  //Action to move to switch focus when pressing the Tab key inside the _findField.
  private Action _findFieldSwitchFocusForwardAction = new TextAction ("Switch Focus from Find Field") {    
    public void actionPerformed(ActionEvent ae) {
      _findField.getNextFocusableComponent().requestFocusInWindow();
    } //Added findPrevious button which replaces the SearchBackwards CheckBox by actually e
  };
  
  //Action to move to switch focus when pressing the Tab key inside the _replaceField.
  private Action _replaceFieldSwitchFocusForwardAction = new TextAction ("Switch Focus from Replace Field") {    
    public void actionPerformed(ActionEvent ae) {
      _replaceField.getNextFocusableComponent().requestFocusInWindow();
    }
  };
  
  
  //Action to move to switch focus when pressing Shift-Tab inside the _findField.
  private Action _findFieldSwitchFocusBackAction = new TextAction ("Switch Focus from Find Field") {    
    public void actionPerformed(ActionEvent ae) {
      _closeButton.requestFocusInWindow();
    }
  };
  
   //Action to move to switch focus when pressing Shift-Tab inside the _replaceField.
  private Action _replaceFieldSwitchFocusBackAction = new TextAction ("Switch Focus from Replace Field") {    
    public void actionPerformed(ActionEvent ae) {
      _findField.requestFocusInWindow();
    }
  };
  
            
  /** Standard Constructor.
   *  @param frame the overall enclosing window
   *  @param model the model containing the documents to search
   */
  public FindReplaceDialog(MainFrame frame, SingleDisplayModel model) {
    super(frame, "Find/Replace");
    _model = model;
    _mainframe = frame;
    _machine = new FindReplaceMachine(_model, _model.getDocumentIterator());
    _updateMachine();

    int i = WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
    InputMap fim = _findField.getInputMap(i);
    fim.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Close");
    fim.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "Find Next");
    fim.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "Switch Focus Forward");
    fim.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK), "Switch Focus Back");
    
    ActionMap fam = _findField.getActionMap();
    fam.put("Find Next", _findNextAction);
    fam.put("Close", new AbstractAction("Close") {
      public void actionPerformed(ActionEvent ae) {
        _frame.getCurrentDefPane().requestFocusInWindow();
        _close();
      }
    });
    fam.put("Switch Focus Forward", new AbstractAction("Switch Focus Forward") {
      public void actionPerformed(ActionEvent ae) { _findField.getNextFocusableComponent().requestFocusInWindow(); }
    });
    fam.put("Switch Focus Back", new AbstractAction("Switch Focus Back") {
      public void actionPerformed(ActionEvent ae) { _closeButton.requestFocusInWindow(); }
    });
    
    
    InputMap rim = _replaceField.getInputMap(i);
    rim.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "Switch Focus Forward");
    rim.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK), "Switch Focus Back");
    
    ActionMap ram = _replaceField.getActionMap();
    ram.put("Switch Focus", new AbstractAction("Switch Focus") {
      public void actionPerformed(ActionEvent ae) { _replaceField.getNextFocusableComponent().requestFocusInWindow(); }
    });
    ram.put("Switch Focus Back", new AbstractAction("Switch Focus Back") {
      public void actionPerformed(ActionEvent ae) { _findField.requestFocusInWindow(); }
    });
    
    
    // Setup color listeners.
    new ForegroundColorListener(_findField);
    new BackgroundColorListener(_findField);
    new ForegroundColorListener(_replaceField);
    new BackgroundColorListener(_replaceField);
    
    /********* Lower Button Panel Initialization ********/
    _findNextButton = new JButton(_findNextAction);
    _findPreviousButton = new JButton(_findPreviousAction);
    _replaceButton = new JButton(_replaceAction);
    _replaceFindNextButton = new JButton(_replaceFindNextAction);
    _replaceFindPreviousButton = new JButton(_replaceFindPreviousAction);
    _replaceAllButton = new JButton(_replaceAllAction);
    _message = new JLabel(""); // JL

    _replaceAction.setEnabled(false);
    _replaceFindNextAction.setEnabled(false);
    _replaceFindPreviousAction.setEnabled(false);

    // set up the layout
    
    /******** Text Field Initializations ********/
    // Sets font for the "Find" field
    Font font = DrJava.getConfig().getSetting(FONT_MAIN);
    setFieldFont(font);

    // Create the Structure for the replace label
    _replaceLabelTop = new JLabel("Replace", SwingConstants.RIGHT);
    _replaceLabelBot = new JLabel("With", SwingConstants.RIGHT);
    
    JPanel replaceLabelPanelTop = new JPanel(new BorderLayout(5,5));
    JPanel replaceLabelPanelBot = new JPanel(new BorderLayout(5,5));
    JPanel replaceLabelPanel = new JPanel(new GridLayout(2,1));
    
    replaceLabelPanelTop.add(_replaceLabelTop, BorderLayout.SOUTH);
    replaceLabelPanelBot.add(_replaceLabelBot, BorderLayout.NORTH);
    
    replaceLabelPanel.add(replaceLabelPanelTop);
    replaceLabelPanel.add(replaceLabelPanelBot);
    
    
    // Create the stucture for the find label
    _findLabelTop = new JLabel("Find", SwingConstants.RIGHT);
    _findLabelBot = new JLabel("Next", SwingConstants.RIGHT);
    
    JPanel findLabelPanelTop = new JPanel(new BorderLayout(5,5));
    JPanel findLabelPanelBot = new JPanel(new BorderLayout(5,5));
    JPanel findLabelPanel = new JPanel(new GridLayout(2,1));
    
    findLabelPanelTop.add(_findLabelTop, BorderLayout.SOUTH);
    findLabelPanelBot.add(_findLabelBot, BorderLayout.NORTH);
    
    findLabelPanel.add(findLabelPanelTop);
    findLabelPanel.add(findLabelPanelBot);                     

    
//    // need separate label and field panels so that the find and
//    // replace textfields line up
//    _labelPanel = new JPanel(new GridLayout(2,1));
//    _labelPanel.add(_findLabel);
//    _labelPanel.add(_replaceLabel);
//    _labelPanel.setBorder(new EmptyBorder(0,5,0,5)); // 5 pix on sides
//    _labelPanel.setFocusable(false);
    
    /******** Button Panel ********/
    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(1,0,5,0));
    buttons.add(_findNextButton);
    buttons.add(_findPreviousButton);
    buttons.add(_replaceFindNextButton);
    buttons.add(_replaceFindPreviousButton);
    buttons.add(_replaceButton);
    buttons.add(_replaceAllButton);
   
    
    /******** Listeners for the right-hand check boxes ********/
    MatchCaseListener mcl = new MatchCaseListener();
    _matchCase = new JCheckBox("Match Case", DrJava.getConfig().getSetting(OptionConstants.FIND_MATCH_CASE));
    _machine.setMatchCase(DrJava.getConfig().getSetting(OptionConstants.FIND_MATCH_CASE));
    _matchCase.addItemListener(mcl);

    _machine.setSearchBackwards(DrJava.getConfig().getSetting(OptionConstants.FIND_SEARCH_BACKWARDS));
    
    SearchAllDocumentsListener sadl= new SearchAllDocumentsListener();
    _searchAllDocuments = new JCheckBox("Search All Documents", DrJava.getConfig().getSetting(OptionConstants.FIND_ALL_DOCUMENTS));
    _machine.setSearchAllDocuments(DrJava.getConfig().getSetting(OptionConstants.FIND_ALL_DOCUMENTS));
    _searchAllDocuments.addItemListener(sadl);

    MatchWholeWordListener mwwl = new MatchWholeWordListener();
    _matchWholeWord = new JCheckBox("Whole Word", DrJava.getConfig().getSetting(OptionConstants.FIND_WHOLE_WORD));// new JRadioButton("Whole Word"); // JL
    if (DrJava.getConfig().getSetting(OptionConstants.FIND_WHOLE_WORD)) _machine.setMatchWholeWord();
    else  _machine.setFindAnyOccurrence();
    _matchWholeWord.addItemListener(mwwl);
    _matchCase.setPreferredSize(_matchWholeWord.getPreferredSize());
     
    IgnoreCommentsAndStringsListener icasl = new IgnoreCommentsAndStringsListener();
    _ignoreCommentsAndStrings = new JCheckBox("No Comments/Strings", DrJava.getConfig().getSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS));
    _machine.setIgnoreCommentsAndStrings(DrJava.getConfig().getSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS));
    _ignoreCommentsAndStrings.addItemListener(icasl);

    this.removeAll(); // actually, override the behavior of TabbedPanel


    /******** Initialize the panels containing the checkboxes ********/
    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);

    _lowerCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _lowerCheckPanel.add(_matchWholeWord); 
    _lowerCheckPanel.add(_ignoreCommentsAndStrings);
    _lowerCheckPanel.setMaximumSize(new Dimension(1000, 40));

    _matchCaseAndAllDocsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _matchCaseAndAllDocsPanel.add(_matchCase);
    _matchCaseAndAllDocsPanel.add(_searchAllDocuments);
    _matchCaseAndAllDocsPanel.setMaximumSize(new Dimension(1000, 40));
    _searchAllDocuments.setSelected(false);


    _findPane.setHorizontalScrollBarPolicy(BorderlessScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _replacePane.setHorizontalScrollBarPolicy(BorderlessScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    JPanel findPanel = new JPanel(new BorderLayout(5,5));
    findPanel.add(findLabelPanel, BorderLayout.WEST);
    findPanel.add(_findPane, BorderLayout.CENTER);
//    findPanel.add(Box.createVerticalGlue());
    
    JPanel replacePanel = new JPanel(new BorderLayout(5,5));
    replacePanel.add(replaceLabelPanel, BorderLayout.WEST);
    replacePanel.add(_replacePane, BorderLayout.CENTER);
//    replacePanel.add(Box.createVerticalGlue());
        
    /******** Set up the Panel containing the Text Fields ********/
    JPanel leftPanel = new JPanel(new GridLayout(1,2,5,5));
    leftPanel.add(findPanel);
    leftPanel.add(replacePanel);

    /******** Set up the Panel containing both rows of checkboxes ********/
    Box optionsPanel = new Box(BoxLayout.Y_AXIS);
    optionsPanel.add(_matchCaseAndAllDocsPanel);
    optionsPanel.add(_lowerCheckPanel);
    optionsPanel.add(Box.createGlue());


    /******** Set upt the Panel containing the two above main panels ********/
    JPanel midPanel = new JPanel(new BorderLayout(5,5));
    midPanel.add(leftPanel, BorderLayout.CENTER);
    midPanel.add(optionsPanel, BorderLayout.EAST);
    
    
    /******** Set upt the Panel containing the midPanel and the closePanel ********/
    _rightPanel = new JPanel(new BorderLayout(5, 5));
    _rightPanel.add(midPanel, BorderLayout.CENTER);
    _rightPanel.add(_closePanel, BorderLayout.EAST); 
    
    JPanel newPanel = new JPanel();
    newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
    newPanel.add(_rightPanel);
    newPanel.add(Box.createVerticalStrut(5));
    newPanel.add(buttons);
    newPanel.add(Box.createVerticalStrut(5));
    
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(Box.createHorizontalStrut(5));
    this.add(newPanel);


    /******* Put all the main panels onto the Find/Replace tab ********/
//    hookComponents(this, _rightPanel, new JPanel(), buttons);
    

    /******** Set the Tab order ********/
    _findField.setNextFocusableComponent(_replaceField);
    _replaceField.setNextFocusableComponent(_matchCase);
    _matchCase.setNextFocusableComponent(_searchAllDocuments);
    _searchAllDocuments.setNextFocusableComponent(_matchWholeWord); // JL (edited)
    _matchWholeWord.setNextFocusableComponent(_ignoreCommentsAndStrings); // JL (edited)
    _ignoreCommentsAndStrings.setNextFocusableComponent(_findNextButton);
    _findNextButton.setNextFocusableComponent(_findPreviousButton);
    _findPreviousButton.setNextFocusableComponent(_replaceFindNextButton);
    _replaceFindNextButton.setNextFocusableComponent(_replaceFindPreviousButton);
    _replaceFindPreviousButton.setNextFocusableComponent(_replaceButton);
    _replaceButton.setNextFocusableComponent(_replaceAllButton);
    _replaceAllButton.setNextFocusableComponent(_closeButton);
    _closeButton.setNextFocusableComponent(_findField);
    
    
    /******** Document, Focus and Key Listeners ********/
    
    // DocumentListener that keeps track of changes in the find field.
    _findField.getDocument().addDocumentListener(new DocumentListener() {
      
      /**If attributes in the find field have changed, gray out "Replace" & "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void changedUpdate(DocumentEvent e) { _updateHelper(); }

      /** If text has been changed in the find field, gray out "Replace" & "Replace and Find Next" buttons.
       *  @param e the event caught by this listener
       */
      public void insertUpdate(DocumentEvent e) { _updateHelper(); }
      
      /** If text has been changed in the find field, gray out "Replace" & "Replace and Find Next" buttons.
       *  @param e the event caught by this listener
       */
      public void removeUpdate(DocumentEvent e) { _updateHelper(); }
      
      private void _updateHelper() {
        Utilities.invokeLater(new Runnable() {
          public void run() {
            _machine.makeCurrentOffsetStart();
            updateFirstDocInSearch();
            _replaceAction.setEnabled(false);
            _replaceFindNextAction.setEnabled(false);
            _replaceFindPreviousAction.setEnabled(false);
            _machine.positionChanged();
            if (_findField.getText().equals("")) _replaceAllAction.setEnabled(false);
            else                                 _replaceAllAction.setEnabled(true);
            updateUI();
          }
        });
      }
    });  
    
    
    /************** Change behavior of findField ****************/
     
    Keymap km = _findField.addKeymap("Find Field Bindings", _findField.getKeymap());
      
    KeyStroke findKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    km.addActionForKeyStroke(findKey, _findEnterAction); 
    
    KeyStroke switchFocusForwardKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    km.addActionForKeyStroke(switchFocusForwardKey, _findFieldSwitchFocusForwardAction); 
    
    KeyStroke switchFocusBackKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK);
    km.addActionForKeyStroke(switchFocusBackKey, _findFieldSwitchFocusBackAction); 
    
    
    Action findNewLineAction = new TextAction("NewLine Action") {
      public void actionPerformed(ActionEvent e) {
        String text = _findField.getText();
        int caretPos = _findField.getCaretPosition();
        String textBeforeCaret = text.substring(0, caretPos);
        String textAfterCaret = text.substring(caretPos);
        _findField.setText(textBeforeCaret.concat("\n").concat(textAfterCaret));
        _findField.setCaretPosition(caretPos+1);
      }
    };    
//    Action newLineAction = new DefaultEditorKit.InsertBreakAction();
    
    KeyStroke newLineKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK);
    km.addActionForKeyStroke(newLineKey, findNewLineAction); 
    
    Action tabAction = new DefaultEditorKit.InsertTabAction();
    KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.CTRL_MASK);
    km.addActionForKeyStroke(tabKey, tabAction); 
    _findField.setKeymap(km);

    
    /************** Change behavior of replaceField ****************/
  
    Keymap rkm = _replaceField.addKeymap("Replace Field Bindings", _replaceField.getKeymap());
    
    Action replaceNewLineAction = new TextAction("NewLine Action") {
      public void actionPerformed(ActionEvent e) {
        String text = _replaceField.getText();
        int caretPos = _replaceField.getCaretPosition();
        String textBeforeCaret = text.substring(0, caretPos);
        String textAfterCaret = text.substring(caretPos);
        _replaceField.setText(textBeforeCaret.concat("\n").concat(textAfterCaret));
        _replaceField.setCaretPosition(caretPos+1);
      }
    };    
    
    rkm.addActionForKeyStroke(newLineKey, replaceNewLineAction);
    rkm.addActionForKeyStroke(switchFocusForwardKey, _replaceFieldSwitchFocusForwardAction); 
    rkm.addActionForKeyStroke(switchFocusBackKey, _replaceFieldSwitchFocusBackAction);
    rkm.addActionForKeyStroke(tabKey, tabAction); 
    _replaceField.setKeymap(rkm);
  }
    
    

  /** Focuses the find/replace dialog in the window, placing the focus on the _findField, and selecting all the text.*/
  public boolean requestFocusInWindow() {
    super.requestFocusInWindow();
    _findField.selectAll();
    return _findField.requestFocusInWindow();
  }

  /** Getter method for the _findField component */
  JTextPane getFindField() { return _findField; }

  /** Called when user the activates "find next" command.  Package visibility to accommodate calls from MainFrame. */
  void findNext() { 
    _machine.setSearchBackwards(false);
    _findLabelTop.setText("Find");
    _findLabelBot.setText("Next");
    _doFind();
  }
  
  /** Called when user the activates "find previous" command.  Package visibility to accommodate calls from MainFrame. */
  void findPrevious() {
    _machine.setSearchBackwards(true);
    _findLabelBot.setText("Prev");
    _doFind();
  }
  
  /** Called from MainFrame in response to opening this or changes in the active document. */
  void beginListeningTo(DefinitionsPane defPane) {
    if (_defPane==null) {
      // removed so it doesn't give the pane focus when switching documents
//      requestFocusInWindow(); 
      _displayed = true;
      _defPane = defPane;
      _defPane.addCaretListener(_caretListener);
      _caretChanged = true;
      
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _mainframe.clearStatusMessage(); // _message.setText(""); // JL
      if (!_machine.isOnMatch() || _findField.getText().equals("")) {
        _replaceAction.setEnabled(false);
        _replaceFindNextAction.setEnabled(false);
        _replaceFindPreviousAction.setEnabled(false);
      }
      else {
        _replaceAction.setEnabled(true);
        _replaceFindNextAction.setEnabled(true);
        _replaceFindPreviousAction.setEnabled(true);
        _machine.setLastFindWord();
      }

      if (_findField.getText().equals("")) _replaceAllAction.setEnabled(false);
      else                                 _replaceAllAction.setEnabled(true);

      _mainframe.clearStatusMessage();
    }
    else
      throw new UnexpectedException(new RuntimeException("FindReplaceDialog should not be listening to anything"));
  }

  /** Called from MainFrame upon closing this Dialog or changes in the active document */
  public void stopListening() {
    if (_defPane != null) {
      _defPane.removeCaretListener(_caretListener);
      _defPane = null;
      _displayed = false;
      _mainframe.clearStatusMessage();
    } 
  }

  /** The action performed when searching forwards */
  private Action _findNextAction = new AbstractAction("Find Next") {
    public void actionPerformed(ActionEvent e) { findNext(); }
  };
  
  private Action _findPreviousAction =  new AbstractAction("Find Previous") {
    public void actionPerformed(ActionEvent e) { findPrevious(); }
  };
                                                            
  /** Abstracted out since this is called from find and replace/find. */
  private void _doFind() {
    if (_findField.getText().length() > 0) {
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _mainframe.clearStatusMessage(); // _message.setText(""); // JL
      
      // FindResult contains the document that the result was found in, offset to the next occurrence of 
      // the string, and a flag indicating whether the end of the document was wrapped around while searching
      // for the string.
      FindResult fr = _machine.findNext();
      AbstractDocumentInterface doc = fr.getDocument();
      OpenDefinitionsDocument matchDoc = _model.getODDForDocument(doc);
      OpenDefinitionsDocument openDoc = _defPane.getOpenDefDocument();
      
      final int pos = fr.getFoundOffset();
      
      // If there actually *is* a match, then switch active documents. otherwise don't
      if (pos != -1) { // found a match
        Caret c = _defPane.getCaret();
        c.setDot(c.getDot());
        
        if (! matchDoc.equals(openDoc)) _model.setActiveDocument(matchDoc);  // set active doc if matchDoc != openDoc
        else _model.refreshActiveDocument();  // re-establish openDoc (which is the _activeDocument) as active
        
        _defPane.setCaretPosition(pos);
        _caretChanged = true;
        _updateMachine();
      }
      
      if (fr.getWrapped() && !_machine.getSearchAllDocuments()) {
        Toolkit.getDefaultToolkit().beep();
        if (!_machine.getSearchBackwards()) _mainframe.setStatusMessage("Search wrapped to beginning.");
        else  _mainframe.setStatusMessage("Search wrapped to end.");
      }
      
      if (fr.getAllDocsWrapped() && _machine.getSearchAllDocuments()) {
        Toolkit.getDefaultToolkit().beep();
        _mainframe.setStatusMessage("Search wrapped around all documents.");
      }
      
      if (pos >= 0) {
        _selectFoundItem();
        
        _replaceAction.setEnabled(true);
        _replaceFindNextAction.setEnabled(true);
        _replaceFindPreviousAction.setEnabled(true);
        _machine.setLastFindWord();
      }
      // else the entire document was searched and no instance of the string
      // was found. display at most 50 characters of the non-found string
      else {
        Toolkit.getDefaultToolkit().beep();
        StringBuffer statusMessage = new StringBuffer("Search text \"");
        if (_machine.getFindWord().length() <= 50) statusMessage.append(_machine.getFindWord());
        else statusMessage.append(_machine.getFindWord().substring(0, 49) + "...");
        statusMessage.append("\" not found.");
        _mainframe.setStatusMessage(statusMessage.toString());
      }
    }
    _findField.requestFocusInWindow();
  }

  
  private Action _replaceAction = new AbstractAction("Replace") {
    public void actionPerformed(ActionEvent e) {
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      String replaceWord = _replaceField.getText();
      _machine.setReplaceWord(replaceWord);
      _mainframe.clearStatusMessage();

      // replaces the occurrence at the current position
      boolean replaced = _machine.replaceCurrent();
      if (replaced) {
        _selectReplacedItem(replaceWord.length());
      }
      _replaceAction.setEnabled(false);
      _replaceFindNextAction.setEnabled(false);
      _replaceFindPreviousAction.setEnabled(false);
      _replaceButton.requestFocusInWindow();
    }
  };

  private Action _replaceFindNextAction = new AbstractAction("Replace/Find Next") {
    public void actionPerformed(ActionEvent e) {
      if (getSearchBackwards() == true) {
        _machine.positionChanged();
        findNext();
      }
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      String replaceWord = _replaceField.getText();
      _machine.setReplaceWord(replaceWord);
      _mainframe.clearStatusMessage(); // _message.setText(""); // JL
      
      // replaces the occurrence at the current position
      boolean replaced = _machine.replaceCurrent();
      // and finds the next word
      if (replaced) {
        _selectReplacedItem(replaceWord.length());
        findNext();
        _replaceFindNextButton.requestFocusInWindow();
      }
      else {
        _replaceAction.setEnabled(false);
        _replaceFindNextAction.setEnabled(false);
        _replaceFindPreviousAction.setEnabled(false);
        Toolkit.getDefaultToolkit().beep();
        _mainframe.setStatusMessage("Replace failed.");
      }
    }
  };
  
  private Action _replaceFindPreviousAction = new AbstractAction("Replace/Find Previous") {
    public void actionPerformed(ActionEvent e) {
      if (getSearchBackwards() == false) {
        _machine.positionChanged();
        findPrevious();
      }
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      String replaceWord = _replaceField.getText();
      _machine.setReplaceWord(replaceWord);
      _mainframe.clearStatusMessage(); 
      
      // replaces the occurrence at the current position
      boolean replaced = _machine.replaceCurrent();
      // and finds the previous word
      if (replaced) {
        _selectReplacedItem(replaceWord.length());
        findPrevious();
        _replaceFindPreviousButton.requestFocusInWindow();
      }
      else {
        _replaceAction.setEnabled(false);
        _replaceFindNextAction.setEnabled(false);
        _replaceFindPreviousAction.setEnabled(false);
        Toolkit.getDefaultToolkit().beep();
        _mainframe.setStatusMessage("Replace failed.");
      }
    }
  };

  // Replaces all occurences of the findfield text with that
  // of the replacefield text both before and after the cursor
  // without prompting for wrapping around the end of the
  // document
  private Action _replaceAllAction = new AbstractAction("Replace All") {
    public void actionPerformed(ActionEvent e) {
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
//      _mainframe.clearStatusMessage();
      _mainframe.clearStatusMessage(); // _message.setText(""); // JL
      int count = _machine.replaceAll();
      Toolkit.getDefaultToolkit().beep();
      _mainframe.setStatusMessage("Replaced " + count + " occurrence" + ((count == 1) ? "" :
                                                                           "s") + ".");
//      _message.setText("Replaced " + count + " occurrence" + ((count == 1) ? "" :
//                                                                "s") + ".");
      _replaceAction.setEnabled(false);
      _replaceFindNextAction.setEnabled(false);
      _replaceFindPreviousAction.setEnabled(false);
    }
  };


  /*private Action _closeAction = new AbstractAction("X") {
   public void actionPerformed(ActionEvent e) {
   // removeTab automatically calls show()
   _close();
   }
   };*/

  protected void _close() {
    _defPane.requestFocusInWindow();
    if (_displayed) stopListening();
    super._close();
    //_frame.uninstallFindReplaceDialog(this);
  }

  private MainFrame _mainframe;

  public void setSearchBackwards(boolean b) { _machine.setSearchBackwards(b); }
  public boolean getSearchBackwards() { return _machine.getSearchBackwards(); }

  /** Sets the font of the find and replace fields to f. */
  public void setFieldFont(Font f) {
    _findField.setFont(f);
    _replaceField.setFont(f);
  }
  
  /** Updates the first document where the current all-document search began (called in two places: either when the 
   * _findField is updated, or when the user changes documents.
   */
  public void updateFirstDocInSearch() {
    _machine.setFirstDoc(_model.getActiveDocument());
  }

  private static Container wrap(JComponent comp) {
    Container stretcher = Box.createHorizontalBox();
    stretcher.add(comp);
    stretcher.add(Box.createHorizontalGlue());
    return stretcher;
  }

  /** Consider a parent container.  Change its layout to GridBagLayout
   * with 2 columns, 2 rows.  Consider them quadrants in a coordinate plain.
   * put the arguments in their corresponding quadrants, ignoring q3.
   */
  private static void hookComponents(Container parent, JComponent q1,
                                     JComponent q2, JComponent q4) {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    parent.setLayout(gbl);
    c.fill = c.BOTH;
    addComp(parent, q2, c, gbl, 0, 0, 0f, 0f, 1, 0);
    addComp(parent, q1, c, gbl, 0, 1, 1f, 0f, 1, 0);
    addComp(parent, new JPanel(), c, gbl, 1, 0, 1f, 1f, 2, 0);
    addComp(parent, new JPanel(), c, gbl, 2, 0, 0f, 0f, 1, 0);
    addComp(parent, q4, c, gbl, 2, 1, 1f, 0f, 1, 0);
  }

  private static void addComp(Container p, JComponent child,
                              GridBagConstraints c, GridBagLayout gbl,
                              int row, int col,
                              float weightx, float weighty, int gridw,
                              int ipady) {
    c.gridx = col; c.gridy = row;
    c.weightx = weightx; c.weighty = weighty;
    c.gridwidth = gridw;
    c.ipady = ipady;
    gbl.setConstraints(child,c);
    p.add(child);
  }

  /** Sets appropriate variables in the FindReplaceMachine if the caret has been changed. */
  private void _updateMachine() {
    if (_caretChanged) {
      OpenDefinitionsDocument doc = _model.getActiveDocument();
      OpenDefinitionsDocument currentDoc = _defPane.getOpenDefDocument();
      _machine.setDocument(doc);
      if (_machine.getFirstDoc() == null) _machine.setFirstDoc(doc);
      _machine.setStart(_defPane.getCaretPosition());
      _machine.setPosition(_defPane.getCaretPosition());
      _caretChanged = false;
    }
  }

//  /** Shows the dialog and sets the focus appropriately. */
//  public void show() {
//   //super.show();
//   System.err.println("*** Called show ***");
////   if (!isVisible())
//     _frame.installFindReplaceDialog(this);
//     _updateMachine();
//     _findField.requestFocusInWindow();
//     _findField.selectAll();
//   }

  /** This method is used to select the item that has been inserted in a replacement. */
  private void _selectReplacedItem(int length) {
    int from, to;
    to = _machine.getCurrentOffset();
    if (_machine.getSearchBackwards()) from = to + length;
    else                               from = to - length;
    _selectFoundItem(from, to);
  }


  /** Calls _selectFoundItem(from, to) with reasonable defaults. */
  private void _selectFoundItem() {
    int position = _machine.getCurrentOffset();
    int to, from;
    to = position;
    if (!_machine.getSearchBackwards()) from = position - _machine.getFindWord().length();
    else from = position + _machine.getFindWord().length();
    _selectFoundItem(from, to);
  }

  /** Will select the searched-for text.  Originally highlighted the text, but we ran into problems
   *  with the document remove method changing the view to where the cursor was located, resulting in 
   *  replace constantly jumping from the replaced text back to the cursor.  There was a 
   *  removePreviousHighlight method which was removed since selections are removed automatically upon
   *  a caret change.
   */
  private void _selectFoundItem(int from, int to) {
    _defPane.centerViewOnOffset(from);
    _defPane.select(from, to);

    // Found this little statement that will show the selected text
    // in _defPane without giving _defPane focus, allowing the
    // user to hit enter repeatedly and change the document while finding
    // next.
    _defPane.getCaret().setSelectionVisible(true);
  }

  /*private void _close() {
   hide();
   }*/
  
  /*public void hide() {
   System.err.println("*** Called hide ***");
   if (_open)
   _frame.uninstallFindReplaceDialog(this);
   //super.hide();
   }*/
  
  /*private ContinueCommand CONFIRM_CONTINUE = new ContinueCommand() {
   public boolean shouldContinue() {
   String text = "The search has reached the end of the document.\n" +
   "Continue searching from the start?";
   int rc = JOptionPane.showConfirmDialog(FindReplaceDialog.this,
   text,
   "Continue search?",
   JOptionPane.YES_NO_OPTION);
   
   switch (rc) {
   case JOptionPane.YES_OPTION:
   return true;
   case JOptionPane.NO_OPTION:
   return false;
   default:
   throw new RuntimeException("Invalid rc: " + rc);
   }

   }
   };*/
  
  
  /***************** METHODS FOR TESTING PURPOSES ONLY  ***********************/
  public DefinitionsPane getDefPane() { return _defPane; }
  public JButton getFindNextButton() {return _findNextButton; }
  

  class MatchCaseListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.DESELECTED) {
        _machine.setMatchCase(false);
        DrJava.getConfig().setSetting(OptionConstants.FIND_MATCH_CASE, Boolean.valueOf(false));

      }
      else if (e.getStateChange() == ItemEvent.SELECTED) {
        _machine.setMatchCase(true);
        DrJava.getConfig().setSetting(OptionConstants.FIND_MATCH_CASE, Boolean.valueOf(true));
      }
      _findField.requestFocusInWindow();
    }
  }

  class SearchBackwardsListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.DESELECTED) {
        _machine.setSearchBackwards(false);
        DrJava.getConfig().setSetting(OptionConstants.FIND_SEARCH_BACKWARDS, Boolean.valueOf(false));
      }
      else if (e.getStateChange() == ItemEvent.SELECTED) {
        _machine.setSearchBackwards(true);
        DrJava.getConfig().setSetting(OptionConstants.FIND_SEARCH_BACKWARDS, Boolean.valueOf(true));
      }
      _findField.requestFocusInWindow();
    }
  }

  class SearchAllDocumentsListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.DESELECTED) {
        _machine.setSearchAllDocuments(false);
        DrJava.getConfig().setSetting(OptionConstants.FIND_ALL_DOCUMENTS, Boolean.valueOf(false));
      }
      else if (e.getStateChange() == ItemEvent.SELECTED) {
        _machine.setSearchAllDocuments(true);
        DrJava.getConfig().setSetting(OptionConstants.FIND_ALL_DOCUMENTS, Boolean.valueOf(true));
      }
      _findField.requestFocusInWindow();
    }
  }
  
  class MatchWholeWordListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.DESELECTED) {
        _machine.setFindAnyOccurrence();
        DrJava.getConfig().setSetting(OptionConstants.FIND_WHOLE_WORD, Boolean.valueOf(false));
      }
      else if (e.getStateChange() == ItemEvent.SELECTED) {
        _machine.setMatchWholeWord();
        DrJava.getConfig().setSetting(OptionConstants.FIND_WHOLE_WORD, Boolean.valueOf(true));
      }
      _findField.requestFocusInWindow();
    }
  }
  
  class IgnoreCommentsAndStringsListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.DESELECTED) {
        _machine.setIgnoreCommentsAndStrings(false);
        DrJava.getConfig().setSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS, Boolean.valueOf(false));
      }
      else if (e.getStateChange() == ItemEvent.SELECTED) {
        _machine.setIgnoreCommentsAndStrings(true);
        DrJava.getConfig().setSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS, Boolean.valueOf(true));
      }
      _findField.requestFocusInWindow();
    }
  }
}
