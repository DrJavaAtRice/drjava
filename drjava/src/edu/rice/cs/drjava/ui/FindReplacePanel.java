/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.ref.WeakReference;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FindReplaceMachine;
import edu.rice.cs.drjava.model.FindResult;
import edu.rice.cs.drjava.model.ClipboardHistoryModel;
import edu.rice.cs.drjava.model.MovingDocumentRegion;
import edu.rice.cs.drjava.model.RegionManager;
import edu.rice.cs.plt.lambda.Runnable1;  // variant on Runnable with unary run method
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.text.SwingDocument;

/** The tabbed panel that handles requests for finding and replacing text.
  * @version $Id$
  */
class FindReplacePanel extends TabbedPanel implements ClipboardOwner {

  /* Other bracketing options:
   *   solid circle u25CF or u26AB (fails)
   *   half circles u25D6 (fails), u25D7
   *   diamond u2666
   *   block arrows  u25BA, u25C4
   *   big block arrows u25B6, u25C0
   *   enclosing wedges u25E4, u25E5
   *   small solid square u25FE
   *   fisheye u25C9
   */
  public static final char LEFT = '\u25FE'; 
  public static final char RIGHT = '\u25FE'; 
  
  // Fields of FindReplacePanel
  // Note: these fields are closed over in listeners (Runnables) so concurrent access is possible!
  private final JButton _findNextButton;
  private final JButton _findPreviousButton;
  private final JButton _findAllButton;
  private final JButton _replaceButton;
  private final JButton _replaceFindNextButton;
  private final JButton _replaceFindPreviousButton;
  private final JButton _replaceAllButton;
  
  private volatile JTextPane _findField;
  private volatile JTextPane _replaceField;
  
  private volatile JLabel _findLabelBot; // Dynamically updated
  
  /* The choice of fields names here is really obnoxious because this class is closely linked with FindReplaceMachine
   * and the SAME field (property) names have completely different meanings in that class! */
  private volatile JCheckBox _ignoreCommentsAndStrings;
  private volatile JCheckBox _matchCase;
  private volatile JCheckBox _searchAllDocuments;
  private volatile JCheckBox _matchWholeWord;
  private volatile JCheckBox _ignoreTestCases;
  private volatile JCheckBox _searchSelectionOnly;
  
  /* MainFrame _frame is inherited from TabbedPanel */
  
  private final FindReplaceMachine _machine;
  private final SingleDisplayModel _model;
  private volatile DefinitionsPane _defPane = null;
  private volatile boolean _caretChanged;
  
  private volatile boolean _isFindReplaceActive = false;
  
  public boolean isFindReplaceActive() { return _isFindReplaceActive; }
  
  /** Listens for changes to the cursor position in order to reset the start position */
  private CaretListener _caretListener = new CaretListener() {
    public void caretUpdate(CaretEvent e) {
      
      assert EventQueue.isDispatchThread();
      
      _replaceAction.setEnabled(false);
      _replaceFindNextAction.setEnabled(false);
      _replaceFindPreviousAction.setEnabled(false);
      _machine.positionChanged();
      _caretChanged = true; 
    }
  };
  
  /** The action performed when searching forwards */
  private Action _findNextAction = new AbstractAction("Find Next") {
    public void actionPerformed(ActionEvent e) { findNext(); }
  };
  
  public Action getFindNextAction() { return _findNextAction; }
  
  private Action _findPreviousAction =  new AbstractAction("Find Previous") {
    public void actionPerformed(ActionEvent e) { findPrevious(); }
  };
  
  public Action getFindPreviousAction() { return _findPreviousAction; }
  
  private Action _findAllAction =  new AbstractAction("Find All") {
    public void actionPerformed(final ActionEvent e) { _isFindReplaceActive = true; _findAll(); _isFindReplaceActive = false;}
  };
  
  private Action _doFindAction = new AbstractAction("Do Find") {
    public void actionPerformed(ActionEvent e) { _doFind(); }
  };
  
  private Action _replaceAction = new AbstractAction("Replace") {
    public void actionPerformed(ActionEvent e) { _replace(); }
  };
  
  private Action _replaceFindNextAction = new AbstractAction("Replace/Find Next") {
    public void actionPerformed(ActionEvent e) { _replaceFindNext(); }
  };
  
  private Action _replaceFindPreviousAction = new AbstractAction("Replace/Find Previous") {
    public void actionPerformed(ActionEvent e) { _replaceFindPrevious(); };
  };
  
  /** Replaces all occurences of the findfield text with that of the replacefield text both before and after the cursor
    * without prompting for wrapping around the end of the document.
    */
  private Action _replaceAllAction = new AbstractAction("Replace All") {
    public void actionPerformed(ActionEvent e) { _replaceAll(); }
  };
  
  // Inserts '\n' into a text field.  (The default binding for "enter" is to insert
  // the system-specific newline string (I think), which causes trouble when finding
  // in files with different newline strings.)
  // TODO: Standardize on \n in a post-processing step, rather than mucking around
  // in the workings of a text editor field.  (Notice, for example, that this
  // doesn't correctly handle an 'enter' pressed while some text is selected.)
  Action _standardNewlineAction = new TextAction("Newline Action") {
    public void actionPerformed(ActionEvent e) {
      JTextComponent c = getTextComponent(e);
      String text = c.getText();
      int caretPos = c.getCaretPosition();
      String textBeforeCaret = text.substring(0, caretPos);
      String textAfterCaret = text.substring(caretPos);
      c.setText(textBeforeCaret.concat("\n").concat(textAfterCaret));
      c.setCaretPosition(caretPos+1);
    }
  };    
  
  /*private Action _closeAction = new AbstractAction("X") {
   public void actionPerformed(ActionEvent e) {
   // removeTab automatically calls show()
   _close();
   }
   };*/
  
  /** Standard Constructor.
    * @param frame the overall enclosing window
    * @param model the model containing the documents to search
    */
  public FindReplacePanel(MainFrame frame, SingleDisplayModel model) {
    super(frame, "Find/Replace");
    _model = model;
    _machine = new FindReplaceMachine(_model, _model.getDocumentIterator(), frame);
    _updateMachine();
    
    
    /********* Button Initialization ********/
    _findNextButton = new JButton(_findNextAction);
    _findPreviousButton = new JButton(_findPreviousAction);
    _findAllButton = new JButton(_findAllAction);
    _replaceButton = new JButton(_replaceAction);
    _replaceFindNextButton = new JButton(_replaceFindNextAction);
    _replaceFindPreviousButton = new JButton(_replaceFindPreviousAction);
    _replaceAllButton = new JButton(_replaceAllAction);
    
    _replaceAction.setEnabled(false);
    _replaceFindNextAction.setEnabled(false);
    _replaceFindPreviousAction.setEnabled(false);
    
    
    /********* Find/Replace Field Initialization **********/
    _findField = new JTextPane(new DefaultStyledDocument());
    _replaceField = new JTextPane(new SwingDocument());
    
    // Make a null action the default for Cntl/Alt/Meta chars entered in Find/Replace fields
    // caused bug 3280955: Reoccurrence of French keyboard problem
    // commented out
    // AbstractDJPane.disableAltCntlMetaChars(_findField);
    // AbstractDJPane.disableAltCntlMetaChars(_replaceField);
    
    //Install document traversal listeners in Find/Replace fields
    _findField.addKeyListener(frame._historyListener);
    _findField.addFocusListener(frame._focusListenerForRecentDocs);
    _replaceField.addKeyListener(frame._historyListener);
    _findField.addFocusListener(frame._focusListenerForRecentDocs);
    
    // Ignore special treatment of 'tab' in text panes
    int tabForward = KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS;
    int tabBackward = KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS;
    _findField.setFocusTraversalKeys(tabForward, null);
    _replaceField.setFocusTraversalKeys(tabForward, null);
    _findField.setFocusTraversalKeys(tabBackward, null);
    _replaceField.setFocusTraversalKeys(tabBackward, null);
    
    // Define custom key bindings for 'enter' and 'tab'
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Event.CTRL_MASK);
    KeyStroke ctrlTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.CTRL_MASK);
    InputMap findIM = _findField.getInputMap();
    InputMap replaceIM = _replaceField.getInputMap();
    findIM.put(enter, "Do Find");
    findIM.put(ctrlEnter, "Insert Newline");
    findIM.put(ctrlTab, "Insert Tab");
    for(KeyStroke k: DrJava.getConfig().getSetting(OptionConstants.KEY_CUT)) findIM.put(k, "Cut");
    for(KeyStroke k: DrJava.getConfig().getSetting(OptionConstants.KEY_COPY)) findIM.put(k, "Copy");
    replaceIM.put(enter, "Insert Newline");
    replaceIM.put(ctrlEnter, "Insert Newline");
    replaceIM.put(ctrlTab, "Insert Tab");
    for(KeyStroke k: DrJava.getConfig().getSetting(OptionConstants.KEY_CUT)) replaceIM.put(k, "Cut");
    for(KeyStroke k: DrJava.getConfig().getSetting(OptionConstants.KEY_COPY)) replaceIM.put(k, "Copy");
    
    Action insertTabAction = new DefaultEditorKit.InsertTabAction();
    ActionMap findAM = _findField.getActionMap();
    ActionMap replaceAM = _replaceField.getActionMap();
    findAM.put("Do Find", _doFindAction);
    findAM.put("Insert Newline", _standardNewlineAction);
    findAM.put("Insert Tab", insertTabAction);
    findAM.put("Cut", cutAction);
    findAM.put("Copy", copyAction);
    replaceAM.put("Insert Newline", _standardNewlineAction);
    replaceAM.put("Insert Tab", insertTabAction);
    replaceAM.put("Cut", cutAction);
    replaceAM.put("Copy", copyAction);
    
    // Setup color listeners.
    new ForegroundColorListener(_findField);
    new BackgroundColorListener(_findField);
    new ForegroundColorListener(_replaceField);
    new BackgroundColorListener(_replaceField);
    Font font = DrJava.getConfig().getSetting(OptionConstants.FONT_MAIN);
    setFieldFont(font);
    
    
    /******** Label Initializations ********/
    // Create the Structure for the replace label
    final JLabel _replaceLabelTop = new JLabel("Replace", SwingConstants.RIGHT);
    final JLabel _replaceLabelBot = new JLabel("With", SwingConstants.RIGHT);
    
    final JPanel replaceLabelPanelTop = new JPanel(new BorderLayout(5,5));
    final JPanel replaceLabelPanelBot = new JPanel(new BorderLayout(5,5));
    final JPanel replaceLabelPanel = new JPanel(new GridLayout(2,1));
    
    replaceLabelPanelTop.add(_replaceLabelTop, BorderLayout.SOUTH);
    replaceLabelPanelBot.add(_replaceLabelBot, BorderLayout.NORTH);
    
    replaceLabelPanel.add(replaceLabelPanelTop);
    replaceLabelPanel.add(replaceLabelPanelBot);
    
    
    // Create the stucture for the find label
    JLabel _findLabelTop = new JLabel("Find", SwingConstants.RIGHT);
    _findLabelBot = new JLabel("Next", SwingConstants.RIGHT);
    
    JPanel findLabelPanelTop = new JPanel(new BorderLayout(5,5));
    JPanel findLabelPanelBot = new JPanel(new BorderLayout(5,5));
    JPanel findLabelPanel = new JPanel(new GridLayout(2,1));
    
    findLabelPanelTop.add(_findLabelTop, BorderLayout.SOUTH);
    findLabelPanelBot.add(_findLabelBot, BorderLayout.NORTH);
    
    findLabelPanel.add(findLabelPanelTop);
    findLabelPanel.add(findLabelPanelBot);
    
    
    /******** Button Panel ********/
    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(1,0,5,0));
    buttons.add(_findNextButton);
    buttons.add(_findPreviousButton);
    buttons.add(_findAllButton);
    buttons.add(_replaceFindNextButton);
    buttons.add(_replaceFindPreviousButton);
    buttons.add(_replaceButton);
    buttons.add(_replaceAllButton);
    
    
    /******** Listeners for the right-hand check boxes ********/
    boolean matchCaseSelected = DrJava.getConfig().getSetting(OptionConstants.FIND_MATCH_CASE);
    _matchCase = new JCheckBox("Match Case", matchCaseSelected);
    _machine.setMatchCase(matchCaseSelected);
    _matchCase.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
        _machine.setMatchCase(isSelected);
        DrJava.getConfig().setSetting(OptionConstants.FIND_MATCH_CASE, isSelected);
        _findField.requestFocusInWindow();
      }
    });
    
    boolean searchAllSelected = DrJava.getConfig().getSetting(OptionConstants.FIND_ALL_DOCUMENTS);
    _searchAllDocuments = new JCheckBox("Search All Documents", searchAllSelected);
    _machine.setSearchAllDocuments(searchAllSelected);
    _searchAllDocuments.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
        if (isSelected)
          _searchSelectionOnly.setSelected(false);
        _machine.setSearchAllDocuments(isSelected);
        DrJava.getConfig().setSetting(OptionConstants.FIND_ALL_DOCUMENTS, isSelected);
        _findField.requestFocusInWindow();
      }
    });
    
    boolean searchSelection = DrJava.getConfig().getSetting(OptionConstants.FIND_ONLY_SELECTION);
    _searchSelectionOnly = new JCheckBox("Search Selection Only", searchSelection);
    _machine.setSearchSelectionOnly(searchSelection);
    _searchSelectionOnly.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
        _machine.setSearchSelectionOnly(isSelected);
        if (isSelected) {
          _ignoreTestCases.setSelected(false);
          _searchAllDocuments.setSelected(false);
          _findNextAction.setEnabled(false);
          _findPreviousAction.setEnabled(false);
          _replaceFindNextAction.setEnabled(false);
          _replaceAction.setEnabled(false);
          _replaceFindPreviousAction.setEnabled(false);
        }
        else {
          _findNextAction.setEnabled(true);
          _findPreviousAction.setEnabled(true);
          _replaceFindNextAction.setEnabled(true);
          _replaceAction.setEnabled(true);
          _replaceFindPreviousAction.setEnabled(true);
        }
        DrJava.getConfig().setSetting(OptionConstants.FIND_ONLY_SELECTION, isSelected);
        _findField.requestFocusInWindow();        
      }      
    });
    
    boolean matchWordSelected = DrJava.getConfig().getSetting(OptionConstants.FIND_WHOLE_WORD);
    _matchWholeWord = new JCheckBox("Whole Word", matchWordSelected);
    if (matchWordSelected) { _machine.setMatchWholeWord(); }
    else { _machine.setFindAnyOccurrence(); }
    _matchWholeWord.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
        if (isSelected) { _machine.setMatchWholeWord(); }
        else { _machine.setFindAnyOccurrence(); }
        DrJava.getConfig().setSetting(OptionConstants.FIND_WHOLE_WORD, isSelected);
        _findField.requestFocusInWindow();
      }
    });
    
    boolean ignoreCommentsSelected = DrJava.getConfig().getSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS);
    _ignoreCommentsAndStrings = new JCheckBox("No Comments/Strings", ignoreCommentsSelected);
    _machine.setIgnoreCommentsAndStrings(ignoreCommentsSelected);
    _ignoreCommentsAndStrings.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
        _machine.setIgnoreCommentsAndStrings(isSelected);
        DrJava.getConfig().setSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS, isSelected);
        _findField.requestFocusInWindow();
      }
    });
    
    boolean ignoreTestCasesSelected = DrJava.getConfig().getSetting(OptionConstants.FIND_NO_TEST_CASES);
    _ignoreTestCases = new JCheckBox("No Test Cases", ignoreTestCasesSelected);
    _machine.setIgnoreTestCases(ignoreTestCasesSelected);
    _ignoreTestCases.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
        if (isSelected) 
          _searchSelectionOnly.setSelected(false);
        _machine.setIgnoreTestCases(isSelected);
        DrJava.getConfig().setSetting(OptionConstants.FIND_NO_TEST_CASES, isSelected);
        _findField.requestFocusInWindow();
      }
    });

    // We choose not to preserve backwards searching between sessions
    //_machine.setSearchBackwards(DrJava.getConfig().getSetting(OptionConstants.FIND_SEARCH_BACKWARDS));
    
    
    /******** Initialize the panels containing the checkboxes ********/
    this.removeAll(); // actually, override the behavior of TabbedPanel
    
    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);
    
    JPanel _lowerCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _lowerCheckPanel.add(_matchWholeWord); 
    _lowerCheckPanel.add(_ignoreCommentsAndStrings);
    _lowerCheckPanel.setMaximumSize(new Dimension(200, 40));
    
    JPanel _matchCaseAndAllDocsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _matchCase.setPreferredSize(_matchWholeWord.getPreferredSize());
    _matchCaseAndAllDocsPanel.add(_matchCase);
    _matchCaseAndAllDocsPanel.add(_searchAllDocuments);
    _matchCaseAndAllDocsPanel.setMaximumSize(new Dimension(200, 40));

    JPanel _ignoreTestCasesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _ignoreTestCasesPanel.add(_ignoreTestCases);
    _ignoreTestCasesPanel.add(_searchSelectionOnly);
    _ignoreTestCasesPanel.setMaximumSize(new Dimension(200, 40));
    
    BorderlessScrollPane _findPane = new BorderlessScrollPane(_findField);
    BorderlessScrollPane _replacePane = new BorderlessScrollPane(_replaceField);
    _findPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _replacePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    
    JPanel findPanel = new JPanel(new BorderLayout(5,5));
    findPanel.add(findLabelPanel, BorderLayout.WEST);
    findPanel.add(_findPane, BorderLayout.CENTER);
    
    JPanel replacePanel = new JPanel(new BorderLayout(5,5));
    replacePanel.add(replaceLabelPanel, BorderLayout.WEST);
    replacePanel.add(_replacePane, BorderLayout.CENTER);
    
    /******** Set up the Panel containing the Text Fields ********/
    JPanel leftPanel = new JPanel(new GridLayout(1,2,5,5));
    leftPanel.add(findPanel);
    leftPanel.add(replacePanel);
    
    /******** Set up the Panel containing both rows of checkboxes ********/
    GridBagLayout gbLayout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    JPanel emptyPanel = new JPanel();
    JPanel optionsPanel = new JPanel(gbLayout);
    optionsPanel.setLayout(gbLayout);
    optionsPanel.add(_matchCaseAndAllDocsPanel);
    optionsPanel.add(_lowerCheckPanel);
    optionsPanel.add(_ignoreTestCasesPanel);
    optionsPanel.add(emptyPanel);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.weightx = 1.0;
    gbLayout.setConstraints(_matchCaseAndAllDocsPanel, c);
    gbLayout.setConstraints(_lowerCheckPanel, c);
    gbLayout.setConstraints(_ignoreTestCasesPanel, c);
    
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.SOUTH;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 1.0;
    
    gbLayout.setConstraints(emptyPanel, c);
    
    /******** Set up the Panel containing the two above main panels ********/
    JPanel midPanel = new JPanel(new BorderLayout(5,5));
    midPanel.add(leftPanel, BorderLayout.CENTER);
    midPanel.add(optionsPanel, BorderLayout.EAST);
    
    
    /******** Set up the Panel containing the midPanel and the closePanel ********/
    JPanel _rightPanel = new JPanel(new BorderLayout(5, 5));
    _rightPanel.add(midPanel, BorderLayout.CENTER);
    _rightPanel.add(_closePanel, BorderLayout.EAST); 
    
    JPanel newPanel = new JPanel();
    newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
    newPanel.add(_rightPanel);
    newPanel.add(Box.createVerticalStrut(5));
    newPanel.add(buttons);
    newPanel.add(Box.createVerticalStrut(5));
    
    this.add(newPanel);
    
    /******** Document, Focus and Key Listeners ********/
    
    // DocumentListener that keeps track of changes in the find field.
    _findField.getDocument().addDocumentListener(new DocumentListener() {
      
      /** If attributes in the find field have changed, gray out "Replace" & "Replace and Find Next" buttons.
        * Assumes all updates are performed in the event thread.
        * @param e the event caught by this listener
        */
      public void changedUpdate(DocumentEvent e) { _updateHelper(); }
      
      /** If text has been changed in the find field, gray out "Replace" & "Replace and Find Next" buttons.
        * @param e the event caught by this listener
        */
      public void insertUpdate(DocumentEvent e) { _updateHelper(); }
      
      /** If text has been changed in the find field, gray out "Replace" & "Replace and Find Next" buttons.
        * @param e the event caught by this listener
        */
      public void removeUpdate(DocumentEvent e) { _updateHelper(); }
      
      private void _updateHelper() {
        assert EventQueue.isDispatchThread();
//            _machine.makeCurrentOffsetStart();
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
  
  /** Focuses the find/replace dialog in the window, placing the focus on the _findField, and selecting all the text.*/
  public boolean requestFocusInWindow() {
    super.requestFocusInWindow();
    _findField.selectAll();
    return _findField.requestFocusInWindow();
  }
  
  /** @return the _findField component */
  JTextPane getFindField() { return _findField; }

  /** Performs "find all" command. */
  private void _findAll() {
    
    _findLabelBot.setText("Next");
    final String searchStr = _findField.getText();
    final String title = searchStr;
    final OpenDefinitionsDocument startDoc = _defPane.getOpenDefDocument();
    final boolean searchAll = _machine.getSearchAllDocuments();
    final boolean searchSelectionOnly = _machine.getSearchSelectionOnly();

    final String tabLabel = (title.length() <= 20) ? title : title.substring(0,20);
    final RegionManager<MovingDocumentRegion> rm = _model.createFindResultsManager();

    final MovingDocumentRegion region = 
      new MovingDocumentRegion(startDoc, _defPane.getSelectionStart(), _defPane.getSelectionEnd(), 
                               startDoc._getLineStartPos(_defPane.getSelectionStart()),
                               startDoc._getLineEndPos(_defPane.getSelectionEnd()));

    final FindResultsPanel panel = 
      _frame.createFindResultsPanel(rm, region, tabLabel, searchStr, searchAll, searchSelectionOnly, _machine.getMatchCase(),
                                    _machine.getMatchWholeWord(), _machine.getIgnoreCommentsAndStrings(),
                                    _ignoreTestCases.isSelected(), new WeakReference<OpenDefinitionsDocument>(startDoc), this);

    findAll(searchStr, searchAll, searchSelectionOnly, _machine.getMatchCase(),  _machine.getMatchWholeWord(), 
            _machine.getIgnoreCommentsAndStrings(), _ignoreTestCases.isSelected(), startDoc, rm, region, panel);

    _log.log("Refreshing active document after 'find all'");

    EventQueue.invokeLater(new Runnable() {  // execute these actions after all pending Runnables in the EventQueue
      public void run() {
        _model.refreshActiveDocument();  // Rationale: a giant findAll left the definitions pane is a strange state
        if (panel.isEmpty()) { requestFocusInWindow(); }// selects _findField as focus }
        else {
          panel.requestFocusInWindow();
          panel.getRegTree().scrollRowToVisible(0);
        }
      }});
  }
  
  /** Performs "find all" with the specified options. 
    * @param searchStr string to search for
    * @param searchAll true if we should search all documents
    * @param searchSelectionOnly true if we should search only the current selection
    * @param matchCase true if search should be case-sensitive
    * @param wholeWord true if we want to match the whole word
    * @param noComments true if we want to ignore comments
    * @param noTestCases true if we want to ignore test cases
    * @param startDoc first document to search within
    * @param rm a RegionManager
    * @param region a MovingDocumentRegion
    * @param panel panel in which to display search results
    */
  public void findAll(final String searchStr, final boolean searchAll, final boolean searchSelectionOnly, final boolean matchCase,
                      final boolean wholeWord, final boolean noComments, final boolean noTestCases, final OpenDefinitionsDocument startDoc, 
                      final RegionManager<MovingDocumentRegion> rm, final MovingDocumentRegion region, final FindResultsPanel panel) {
    
    _machine.setSearchBackwards(false);

    final int searchLen = searchStr.length();
    if (searchLen == 0) return;
    
    final OpenDefinitionsDocument oldDoc = _machine.getDocument();
    final OpenDefinitionsDocument oldFirstDoc = _machine.getFirstDoc();
//    final String oldFindWord = _machine.getFindWord();
    final boolean oldSearchAll = _machine.getSearchAllDocuments();
    final boolean oldSearchSelectionOnly = _machine.getSearchSelectionOnly();
    final boolean oldMatchCase = _machine.getMatchCase();
    final boolean oldWholeWord = _machine.getMatchWholeWord();
    final boolean oldNoComments = _machine.getIgnoreCommentsAndStrings();
    final boolean oldNoTestCases = _machine.getIgnoreTestCases();
    final int oldPosition = _machine.getCurrentOffset();
    
//    _updateMachine();  // in this method call, _defPane returned null in actual usage
    _machine.setDocument(startDoc);
    if (_machine.getFirstDoc() == null) _machine.setFirstDoc(startDoc);
    _machine.setSearchAllDocuments(searchAll);
    _machine.setSearchSelectionOnly(searchSelectionOnly);
    _machine.setMatchCase(matchCase);
    if (wholeWord) { _machine.setMatchWholeWord(); }
    else { _machine.setFindAnyOccurrence(); }
    _machine.setIgnoreCommentsAndStrings(noComments);
    _machine.setPosition(startDoc.getCurrentLocation());
    _machine.setIgnoreTestCases(noTestCases);

    _machine.setFindWord(searchStr);
    final String replaceStr = _replaceField.getText();
    _machine.setReplaceWord(replaceStr);
    final List<FindResult> results = new ArrayList<FindResult>();
    
    _frame.setStatusMessage("Finding All");
    repaint();
        
    _frame.hourglassOn();
    int count = 0;
    try {
      /* Accumulate all occurrences of searchStr in results. */
      count = _machine.processAll(new Runnable1<FindResult>() { public void run(FindResult fr) { results.add(fr); }}, region);
    }
    finally { 
      _frame.hourglassOff(); 
      _model.refreshActiveDocument();
    } 
    /* Restore state of FindReplaceMachine except for _findWord and _replaceWord. */
    _log.log("Restoring FindReplaceMachine");
    _machine.setDocument(oldDoc);
    _machine.setFirstDoc(oldFirstDoc);
//    _machine.setFindWord(oldFindWord);
    _machine.setSearchAllDocuments(oldSearchAll);
    _machine.setSearchSelectionOnly(oldSearchSelectionOnly);
    _machine.setMatchCase(oldMatchCase);
    if (oldWholeWord) { _machine.setMatchWholeWord(); }
    else { _machine.setFindAnyOccurrence(); }
    _machine.setIgnoreCommentsAndStrings(oldNoComments);
    _machine.setIgnoreTestCases(oldNoTestCases);
    _machine.setPosition(oldPosition);
    
//    System.out.println("Adding found regions to corresponding documents");
    for (FindResult fr: results) {
      
      final OpenDefinitionsDocument doc = fr.getDocument();
      
      final int end = fr.getFoundOffset();
      final int start = end - searchLen;
      final int lineStart = doc._getLineStartPos(start);
      final int lineEnd = doc._getLineEndPos(end);
      
      rm.addRegion(new MovingDocumentRegion(doc, start, end, lineStart, lineEnd));                       
    }
      
    // show panel in tabbed pane unless it is empty
    if (count > 0) _frame.showFindResultsPanel(panel);
    else panel.freeResources();
    
//    _model.refreshActiveDocument();  // force tabbed pane to show count for find command
    
    if (searchSelectionOnly) 
      EventQueue.invokeLater(new Runnable() { public void run() { 
      if (_defPane != null) {
        _defPane.requestFocusInWindow();
        _defPane.setSelectionStart(region.getStartOffset());
        _defPane.setSelectionEnd(region.getEndOffset());
      }
    } }); 
    _log.log("Completing findAll call");
    Toolkit.getDefaultToolkit().beep();
    _log.log("Updating status message to report number of matching occurrences");
    _frame.setStatusMessage("Found " + count + " occurrence" + ((count == 1) ? "" : "s") + ".");
  }
  
  /** Performs the "replace all" command. */
  
  private void _replaceAll() {
    
    _findLabelBot.setText("Next");
    
    final String searchStr = _findField.getText();
    final String replaceStr = _replaceField.getText();
    final String title = searchStr;
    final OpenDefinitionsDocument startDoc = _defPane.getOpenDefDocument();
    final boolean searchAll = _machine.getSearchAllDocuments();
    final boolean searchSelectionOnly = _machine.getSearchSelectionOnly();
    final boolean matchCase = _machine.getMatchCase();
    final boolean wholeWord = _machine.getMatchWholeWord();
    final boolean noComments = _machine.getIgnoreCommentsAndStrings();
    final boolean noTestCases = _machine.getIgnoreTestCases();
    
    final MovingDocumentRegion region = 
      new MovingDocumentRegion(startDoc, _defPane.getSelectionStart(), _defPane.getSelectionEnd(), 
                               startDoc._getLineStartPos(_defPane.getSelectionStart()),
                               startDoc._getLineEndPos(_defPane.getSelectionEnd()));
    
    replaceAll(searchStr, replaceStr, searchAll, searchSelectionOnly, _machine.getMatchCase(),  _machine.getMatchWholeWord(), 
            _machine.getIgnoreCommentsAndStrings(), _ignoreTestCases.isSelected(), startDoc, region);
  }
  
  /** Performs the "replace all" with the specified options. 
    * @param searchStr string to search for
    * @param replaceStr string to be used as replacement
    * @param searchAll true if we should search all documents
    * @param searchSelectionOnly true if we should search only the current selection
    * @param matchCase true if search should be case-sensitive
    * @param wholeWord true if we want to match the whole word
    * @param noComments true if we want to ignore comments
    * @param noTestCases true if we want to ignore test cases
    * @param startDoc first document to search within
    * @param region a MovingDocumentRegion
    */
  public void replaceAll(final String searchStr, final String replaceStr, final boolean searchAll, final boolean searchSelectionOnly, 
                         final boolean matchCase, final boolean wholeWord, final boolean noComments, final boolean noTestCases, 
                         final OpenDefinitionsDocument startDoc, final MovingDocumentRegion region) {

    
    _machine.setSearchBackwards(false);

    final int searchLen = searchStr.length();
    if (searchLen == 0) return;
    
    final OpenDefinitionsDocument oldDoc = _machine.getDocument();
    final OpenDefinitionsDocument oldFirstDoc = _machine.getFirstDoc();
//    final String oldFindWord = _machine.getFindWord();
    final boolean oldSearchAll = _machine.getSearchAllDocuments();
    final boolean oldSearchSelectionOnly = _machine.getSearchSelectionOnly();
    final boolean oldMatchCase = _machine.getMatchCase();
    final boolean oldWholeWord = _machine.getMatchWholeWord();
    final boolean oldNoComments = _machine.getIgnoreCommentsAndStrings();
    final boolean oldNoTestCases = _machine.getIgnoreTestCases();
    final int oldPosition = _machine.getCurrentOffset();
    
    _machine.setDocument(startDoc);
    if (_machine.getFirstDoc() == null) _machine.setFirstDoc(startDoc);
    _machine.setSearchAllDocuments(searchAll);
    _machine.setSearchSelectionOnly(searchSelectionOnly);
    _machine.setMatchCase(matchCase);
    if (wholeWord) { _machine.setMatchWholeWord(); }
    else { _machine.setFindAnyOccurrence(); }
    _machine.setIgnoreCommentsAndStrings(noComments);
    _machine.setPosition(startDoc.getCurrentLocation());
    _machine.setIgnoreTestCases(noTestCases);

    _machine.setFindWord(searchStr);
    _machine.setReplaceWord(replaceStr);
    _log.log("In FindReplacePanel, setting _findWord (in FRM) to '" + searchStr + "' and _replaceWord to '" + replaceStr + "'");

    _frame.setStatusMessage("Replacing All");
    repaint();
    
    _frame.hourglassOn();
    int count = 0;
    /* Code snippet to replace matched String (findWord) with replaceWord loaded into FindReplaceMachine.
     * fr is ignored because the machine state implicitly contains this information. 
     * Aborts if an attempt is made to replace a string that does not match _findWord (searchStr).
     */
    Runnable1<FindResult> replaceMatchingString = new Runnable1<FindResult>() { 
      public void run(FindResult fr) { 
        boolean success =_machine.replaceCurrent();
        if (! success) throw new UnexpectedException("Replace All command aborted because current does not match search string");
      }
    };
    try {
      /* Replace all matching strings in region (which may be entire project). */
      count = _machine.processAll(replaceMatchingString, region);
    }
    finally { 
      _frame.hourglassOff(); 
      _model.refreshActiveDocument();
    }
    
    /* Restore state of FindReplaceMachine, except for _findWord and _replaceWord */
    _log.log("Restoring FindReplaceMachine");
    _machine.setDocument(oldDoc);
    _machine.setFirstDoc(oldFirstDoc);
//    _machine.setFindWord(oldFindWord);
    _machine.setSearchAllDocuments(oldSearchAll);
    _machine.setSearchSelectionOnly(oldSearchSelectionOnly);
    _machine.setMatchCase(oldMatchCase);
    if (oldWholeWord) { _machine.setMatchWholeWord(); }
    else { _machine.setFindAnyOccurrence(); }
    _machine.setIgnoreCommentsAndStrings(oldNoComments);
    _machine.setIgnoreTestCases(oldNoTestCases);
    _machine.setPosition(oldPosition);
    
    Toolkit.getDefaultToolkit().beep();
    _frame.setStatusMessage("Replaced " + count + " occurrence" + ((count == 1) ? "" : "s") + ".");
    _replaceAction.setEnabled(false);
    _replaceFindNextAction.setEnabled(false);
    _replaceFindPreviousAction.setEnabled(false);
    
    _log.log("Refreshing active document after 'replace all'");
    _model.refreshActiveDocument();  // Rationale: a giant findAll left the definitions pane is a strange state
    _findField.requestFocusInWindow();
//    EventQueue.invokeLater(new Runnable() { public void run() { panel.getRegTree().scrollRowToVisible(0); } });
  }
  
  private void _replaceFindNext() {
    _frame.updateStatusField("Replacing and Finding Next");
    if (isSearchBackwards() == true) {
      _machine.positionChanged();
      findNext();
    }
    _updateMachine();
    _machine.setFindWord(_findField.getText());
    final String replaceWord = _replaceField.getText();
    _machine.setReplaceWord(replaceWord);
    _frame.clearStatusMessage(); // _message.setText(""); // JL
    
    // replaces the occurrence at the current position
    final boolean replaced = _machine.replaceCurrent();
    // and finds the next word
    if (replaced) {
      _selectFoundOrReplacedItem(replaceWord.length());
      findNext();
      _replaceFindNextButton.requestFocusInWindow();
    }
    else {
      _replaceAction.setEnabled(false);
      _replaceFindNextAction.setEnabled(false);
      _replaceFindPreviousAction.setEnabled(false);
      Toolkit.getDefaultToolkit().beep();
      _frame.setStatusMessage("Replace failed.");
    }
  }
  
  private void _replaceFindPrevious() {
    _frame.updateStatusField("Replacing and Finding Previous");
    if (isSearchBackwards() == false) {
      _machine.positionChanged();
      findPrevious();
    }
    _updateMachine();
    _machine.setFindWord(_findField.getText());
    final String replaceWord = _replaceField.getText();
    _machine.setReplaceWord(replaceWord);
    _frame.clearStatusMessage(); 
    
    // replaces the occurrence at the current position
    final boolean replaced = _machine.replaceCurrent();
    // and finds the previous word
    if (replaced) {
      _selectFoundOrReplacedItem(replaceWord.length());
      findPrevious();
      _replaceFindPreviousButton.requestFocusInWindow();
    }
    else {
      _replaceAction.setEnabled(false);
      _replaceFindNextAction.setEnabled(false);
      _replaceFindPreviousAction.setEnabled(false);
      Toolkit.getDefaultToolkit().beep();
      _frame.setStatusMessage("Replace failed.");
    }
  }
  
  /** Performs the "find next" command.  Package visibility to accommodate calls from MainFrame. */
  void findNext() {
    _frame.updateStatusField("Finding Next");
    _machine.setSearchBackwards(false);
    _findLabelBot.setText("Next");
    _doFind();  // updates position stored in machine before starting
    if (DrJava.getConfig().getSetting(OptionConstants.FIND_REPLACE_FOCUS_IN_DEFPANE).booleanValue()) {
      _defPane.requestFocusInWindow();  // moves focus to DefinitionsPane
    }
  }
  
  /** Called when user the activates "find previous" command.  Package visibility to accommodate calls from MainFrame. */
  void findPrevious() {
    _frame.updateStatusField("Finding Previous");
    _machine.setSearchBackwards(true);
    _findLabelBot.setText("Prev");
    _doFind();
    if (DrJava.getConfig().getSetting(OptionConstants.FIND_REPLACE_FOCUS_IN_DEFPANE).booleanValue()) {
      _defPane.requestFocusInWindow();  // moves focus to DefinitionsPane
    }
  }
  
  private void _replace() {
    _frame.updateStatusField("Replacing");
    _updateMachine();
    _machine.setFindWord(_findField.getText());
    final String replaceWord = _replaceField.getText();
    _machine.setReplaceWord(replaceWord);
//    _frame.clearStatusMessage();
    
    // replaces the occurrence at the current position
    final boolean replaced = _machine.replaceCurrent();
    if (replaced) _selectFoundOrReplacedItem(replaceWord.length());
    _replaceAction.setEnabled(false);
    _replaceFindNextAction.setEnabled(false);
    _replaceFindPreviousAction.setEnabled(false);
    _replaceButton.requestFocusInWindow();
  }
  
  /** Called from MainFrame in response to opening this or changes in the active document. 
   * @param defPane a DefinitionsPane
   */
  void beginListeningTo(DefinitionsPane defPane) {
//    System.out.println("beginListeningTo called!");
    if (_defPane == null) {
      // removed so it doesn't give the pane focus when switching documents
//      requestFocusInWindow(); 
      _displayed = true;
      _defPane = defPane;
      _defPane.addCaretListener(_caretListener);
      _caretChanged = true;
      
      _updateMachine();
      _machine.setFindWord(_findField.getText());
      _machine.setReplaceWord(_replaceField.getText());
      _frame.clearStatusMessage(); // _message.setText(""); // JL
      if (! _machine.onFindWordMatch() || _findField.getText().equals("")) {
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
      
      _frame.clearStatusMessage();
    }
    else
      throw new UnexpectedException(new RuntimeException("FindReplacePanel should not be listening to anything"));
  }
  
  /** Called from MainFrame upon closing this Dialog or changes in the active document. */
  public void stopListening() {
//    System.out.println("stopListening() called");
    if (_defPane != null) {
      _defPane.removeCaretListener(_caretListener);
      _defPane = null;
      _displayed = false;
      _frame.clearStatusMessage();
    } 
  }
  
  /** Abstracted out since this is called from findNext and findPrevious. */
  private void _doFind() {
    
    if (_findField.getText().length() > 0) {

      _updateMachine();
      final String findWord = _findField.getText();
      _machine.setFindWord(findWord);
      _machine.setReplaceWord(_replaceField.getText());
      _frame.clearStatusMessage();
      final boolean searchAll = _machine.getSearchAllDocuments();
      
      // FindResult contains the document that the result was found in, offset to the next occurrence of 
      // the string, and a flag indicating whether the end of the document was wrapped around while searching
      // for the string.
      _frame.hourglassOn();
      try {
        final FindResult fr = _machine.findNext();
        OpenDefinitionsDocument matchDoc = fr.getDocument();
//      OpenDefinitionsDocument matchDoc = _model.getODDForDocument(doc);
        OpenDefinitionsDocument openDoc = _defPane.getOpenDefDocument();
        final boolean docChanged = matchDoc != openDoc;
        
        final int pos = fr.getFoundOffset();
        
        if (pos >= 0) _model.addToBrowserHistory();  // pos >= 0  <=> search succeeded
        
        if (searchAll) {  // if search was global, reset the active document
          if (docChanged) _model.setActiveDocument(matchDoc);  // set active doc if matchDoc != openDoc
          else _model.refreshActiveDocument();  // the unmodified active document may have been kicked out of the cache!
        } 
        
        if (fr.isWrapped() && ! searchAll) {
          Toolkit.getDefaultToolkit().beep();
          if (! _machine.isSearchBackwards()) _frame.setStatusMessage("Search wrapped to beginning.");
          else _frame.setStatusMessage("Search wrapped to end.");
        }
        
        if (fr.getAllWrapped() && searchAll) {
          Toolkit.getDefaultToolkit().beep();
          _frame.setStatusMessage("Search wrapped around all documents.");
        }
        
        if (pos >= 0) { // found a match
//        Caret c = _defPane.getCaret();
//        c.setDot(c.getDot());
          _defPane.setCaretPosition(pos);
          _caretChanged = true;
          _updateMachine();
          
          final Runnable command = new Runnable() {
            public void run() {
              _selectFoundOrReplacedItem(findWord.length());
              _replaceAction.setEnabled(true);
              _replaceFindNextAction.setEnabled(true);
              _replaceFindPreviousAction.setEnabled(true);
              _machine.setLastFindWord();
              _model.addToBrowserHistory();
              if (DrJava.getConfig().getSetting(OptionConstants.FIND_REPLACE_FOCUS_IN_DEFPANE).booleanValue()) {
                // moves focus to DefinitionsPane
                _frame.toFront();
                EventQueue.invokeLater(new Runnable() { public void run() { 
                  if (_defPane != null) {
                    _defPane.requestFocusInWindow();
                  }
                } });
              }
            } };
          
          if (docChanged)
            // defer executing this code until after active document switch is complete
            EventQueue.invokeLater(command);
          else command.run();
        }
        // else the entire document was searched and no instance of the string
        // was found. display at most 50 characters of the non-found string
        else {
          Toolkit.getDefaultToolkit().beep();
          final StringBuilder statusMessage = new StringBuilder("Search text \"");
          if (findWord.length() <= 50) statusMessage.append(findWord);
          else statusMessage.append(findWord.substring(0, 49) + "...");
          statusMessage.append("\" not found.");
          _frame.setStatusMessage(statusMessage.toString());
        }
      }
      finally { _frame.hourglassOff(); }
    }
    
    if (! DrJava.getConfig().getSetting(OptionConstants.FIND_REPLACE_FOCUS_IN_DEFPANE).booleanValue()) {
      _findField.requestFocusInWindow();
    }
  }
  
  @Override
  protected void _close() {
    _defPane.requestFocusInWindow();
    if (_displayed) stopListening();
    super._close();
    //_frame.uninstallFindReplaceDialog(this);
  }
  
  public void setSearchBackwards(boolean b) { _machine.setSearchBackwards(b); }
  public boolean isSearchBackwards() { return _machine.isSearchBackwards(); }
  
  /** Sets the font of the find and replace fields to f. 
   * @param f font to be set
   */
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
  
//  private static Container wrap(JComponent comp) {
//    Container stretcher = Box.createHorizontalBox();
//    stretcher.add(comp);
//    stretcher.add(Box.createHorizontalGlue());
//    return stretcher;
//  }
//
//  /** Consider a parent container.  Change its layout to GridBagLayout with 2 columns, 2 rows.  Consider them 
//    * quadrants in a coordinate plain.  Put the arguments in their corresponding quadrants, ignoring q3.
//    */
//  private static void hookComponents(Container parent, JComponent q1, JComponent q2, JComponent q4) {
//    GridBagLayout gbl = new GridBagLayout();
//    GridBagConstraints c = new GridBagConstraints();
//    parent.setLayout(gbl);
//    c.fill = c.BOTH;
//    addComp(parent, q2, c, gbl, 0, 0, 0f, 0f, 1, 0);
//    addComp(parent, q1, c, gbl, 0, 1, 1f, 0f, 1, 0);
//    addComp(parent, new JPanel(), c, gbl, 1, 0, 1f, 1f, 2, 0);
//    addComp(parent, new JPanel(), c, gbl, 2, 0, 0f, 0f, 1, 0);
//    addComp(parent, q4, c, gbl, 2, 1, 1f, 0f, 1, 0);
//  }
  
//  private static void addComp(Container p, JComponent child,
//                              GridBagConstraints c, GridBagLayout gbl,
//                              int row, int col,
//                              float weightx, float weighty, int gridw,
//                              int ipady) {
//    c.gridx = col; c.gridy = row;
//    c.weightx = weightx; c.weighty = weighty;
//    c.gridwidth = gridw;
//    c.ipady = ipady;
//    gbl.setConstraints(child,c);
//    p.add(child);
//  }
  
  /** Sets appropriate variables in the FindReplaceMachine if the caret has been changed. */
  private void _updateMachine() {
    if (_caretChanged) {
      final OpenDefinitionsDocument doc = _model.getActiveDocument();
      _machine.setDocument(doc);
      if (_machine.getFirstDoc() == null) _machine.setFirstDoc(doc);
      if (_defPane != null) {
        _machine.setPosition(_defPane.getCaretPosition());
        _caretChanged = false;
      }
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
  
  /** This method is used to select the item that has been inserted in a 
   * replacement.  Assumes the current offset identifies the found or 
   * replaced item.  In a forward search, this offset is the RIGHT edge of 
   * the found/replaced item; in a backwards search it is the LEFT edge. 
   * @param length length of the found or replaced item
   */
  private void _selectFoundOrReplacedItem(int length) {
    final int offset = _machine.getCurrentOffset();
    final int from, to;
    
    if (_machine.isSearchBackwards()) {
      from = offset + length;
      // "to" is the offset where the caret will be positioned
      // when searching backwards, "to" has to be the smaller offset
      to = offset;
    }
    else {
      from = offset - length;
      to = offset;
    }
    _selectFoundOrReplacedItem(from, to);
  }
  
  
//  /** Calls _selectFoundItem(from, to) with reasonable defaults. */
//  private void _selectFoundItem() {
//    int position = _machine.getCurrentOffset();
//    int to, from;
//    to = from = position;
//    if (! _machine.getSearchBackwards()) from = position - _machine.getFindWord().length();
//    else to = position + _machine.getFindWord().length();
//    _selectFoundItem(from, to);
//  }
  
  /** Will select the identified text (from, to).  Note that positions are 
   * technically between characters, so there is no distinction between open 
   * and closed intervals.  Originally highlighted the text, but we ran into problems
   * with the document remove method changing the view to where the cursor was located, resulting in 
   * replace constantly jumping from the replaced text back to the cursor.  There was a 
   * removePreviousHighlight method which was removed since selections are removed automatically upon
   * a caret change.
   * @param from left bound on selection
   * @param to right bound on selection
   */
  private void _selectFoundOrReplacedItem(int from, int to) {
    _defPane.centerViewOnOffset(from);
    _defPane.select(from, to);
    
    // Found this little statement that will show the selected text in _defPane without giving _defPane 
    // focus, allowing the user to hit enter repeatedly and change the document while finding next.
    EventQueue.invokeLater(new Runnable() { 
      public void run() { _defPane.getCaret().setSelectionVisible(true); } 
    });
//    _defPane.centerViewOnOffset(from);
  }
  
//  public void hide() {
//   System.err.println("*** Called hide ***");
//   if (_open)
//   _frame.uninstallFindReplaceDialog(this);
//   //super.hide();
//   }
  
//  private ContinueCommand CONFIRM_CONTINUE = new ContinueCommand() {
//    public boolean shouldContinue() {
//      String text = "The search has reached the end of the document.\n" +
//        "Continue searching from the start?";
//      int rc = JOptionPane.showConfirmDialog(FindReplacePanel.this,
//                                             text,
//                                             "Continue search?",
//                                             JOptionPane.YES_NO_OPTION);
//      
//      switch (rc) {
//        case JOptionPane.YES_OPTION:
//          return true;
//        case JOptionPane.NO_OPTION:
//          return false;
//        default:
//          throw new RuntimeException("Invalid rc: " + rc);
//      }
//      
//    }
//  };
  
  /** We lost ownership of what we put in the clipboard. */
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    // ignore
  }
  
  /** Default cut action. */
  Action cutAction = new DefaultEditorKit.CutAction() {
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() instanceof JTextComponent) {
        final JTextComponent tc = (JTextComponent)e.getSource();
        if (tc.getSelectedText() != null) {
          super.actionPerformed(e);
          final String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(FindReplacePanel.this);
          if (s != null && s.length() != 0){ ClipboardHistoryModel.singleton().put(s); }
        }
      }
    }
  };
  
  /** Default copy action. */
  Action copyAction = new DefaultEditorKit.CopyAction() {
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() instanceof JTextComponent) {
        final JTextComponent tc = (JTextComponent)e.getSource();
        if (tc.getSelectedText() != null) {
          super.actionPerformed(e);
          final String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(FindReplacePanel.this);
          if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
        }
      }
    }
  };  

  /** Uses the FindReplaceMachine from the most recently run search to check if r is (still) a match for searchString.
    * @param r the region to check
    * @param searchString the string to check the region against
    * @return whether or not the text in r matches searchString
    */
  public boolean isSearchStringMatch(MovingDocumentRegion r, String searchString) {
    final OpenDefinitionsDocument doc = r.getDocument();
    final int startPos = r.getStartOffset();
    final int endPos = r.getEndOffset();

    if ((endPos - startPos == searchString.length()) && (_machine != null)) {  /* preserves the state of the FRM! */
      /* save state of FRM */
      final String oldFindWord = _machine.getFindWord();
      final OpenDefinitionsDocument oldDoc = _machine.getDocument();
      final int oldPos = _machine.getCurrentOffset();
      
      try {
        _machine.setFindWord(searchString);  
        _machine.setDocument(doc);
        _machine.setPosition(endPos); 
        return _machine.onFindWordMatch();
      } 
      finally { /* restore state of FRM */
        _machine.setFindWord(oldFindWord);
        _machine.setDocument(oldDoc);
        _machine.setPosition(oldPos);
      }
    }
    return false;
  }

  /*--------------------- METHODS FOR TESTING PURPOSES ONLY ---------------------*/
  public DefinitionsPane getDefPane() { return _defPane; }
  public JButton getFindNextButton() { return _findNextButton; }
}
