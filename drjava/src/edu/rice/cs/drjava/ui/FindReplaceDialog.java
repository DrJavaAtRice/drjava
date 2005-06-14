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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.EmptyBorder;

import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.DefaultSingleDisplayModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.swing.FindReplaceMachine;
import edu.rice.cs.util.swing.FindResult;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.UnexpectedException;


/** The tabbed panel that handles requests for finding and replacing text.
 *  (Used to be a dialog box, hence the name. We should fix this.)
 *  @version $Id$
 */
class FindReplaceDialog extends TabbedPanel implements OptionConstants {
  private JButton _findNextButton;
  private JButton _replaceButton;
  private JButton _replaceFindButton;
  private JButton _replaceAllButton;
  private JTextPane _findField = new JTextPane(new DefaultStyledDocument());
  private BorderlessScrollPane _findPane = new BorderlessScrollPane(_findField);
  private JTextPane _replaceField = new JTextPane(new DefaultStyledDocument());
  private BorderlessScrollPane _replacePane = new BorderlessScrollPane(_replaceField);
  private JLabel _message; // JL
  private JPanel _labelPanel;
  private JCheckBox _ignoreCommentsAndStrings;
  private JCheckBox _matchCase;
  private JCheckBox _searchBackwards;
  private JCheckBox _searchAllDocuments;
//  private ButtonGroup _radioButtonGroup;
  private JPanel _lowerCheckPanel;
  private JCheckBox _matchWholeWord; // private JRadioButton _matchWholeWord; // JL
//  private JRadioButton _findAnyOccurrence; // JL
  private JPanel _matchCaseAndClosePanel;
  private JPanel _commentsAndStringsPanel;
  private JPanel _rightPanel;
  private FindReplaceMachine _machine;
  private SingleDisplayModel _model;
  private DefinitionsPane _defPane = null;
  private boolean _caretChanged;
  
  /** Listens for changes to the cursor position in order to reset the start position */
  private CaretListener _caretListener = new CaretListener() {
    public void caretUpdate(CaretEvent e) {
      _replaceAction.setEnabled(false);
      _replaceFindAction.setEnabled(false);
      _machine.positionChanged();
      _caretChanged = true;
    }
  };
  
  /**Action to replace the newLine default Action when pressing the Enter key. Instead, does a find*/
  Action _findEnterAction = new TextAction("Find on Pressing Enter") {
    public void actionPerformed(ActionEvent ae) {
      _doFind();
      _findField.requestFocusInWindow();
    }};
  
  
//  /** Listens for the pressing of the Enter key (only activated after the Ctrl key is not pressed). Finds the next
//   *  occurrence of the findWord. Uses logic to "undo" the default behavior of the Enter key. If someone can find a way
//   *  to remove the default Enter key behavior (i.e. go to the next line) that would be a better solution (solved above)
//   */
//  private final KeyListener _findEnterListener = new KeyListener() {
//      public void keyPressed(KeyEvent e) {}
//      
//      public void keyReleased(KeyEvent e) {
//        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//          Utilities.invokeAndWait( new Runnable() {
//            public void run() {   
//              int caretPosition = _findField.getCaretPosition();
//              String text = _findField.getText();
//              String textBeforeCaret = text.substring(0, caretPosition);
//              String textAfterCaret = "";
//              if (caretPosition < text.length()) textAfterCaret = text.substring(caretPosition);
//              
//              int previousNewLineOffset = textBeforeCaret.lastIndexOf("\n");
//              if (previousNewLineOffset == textBeforeCaret.length()-1)
//                text = textBeforeCaret.substring(0, previousNewLineOffset).concat(textAfterCaret);
//              
//              _findField.setText(text);
//              //put the caret where it was before
//              _findField.setCaretPosition(textBeforeCaret.length()-1);
//              _doFind();
//              _findField.requestFocusInWindow();
//            }
//          });
//        }
//      }
//      
//      public void keyTyped(KeyEvent e){}
//    };
  
  /** Listens for the pressing of the Enter key (only activated after the Ctrl key has been pressed). Inserts a new line
   *  in the findField
   */
  private final KeyListener _newLineEnterListener = new KeyListener() {
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER)  {
        String text = _findField.getText();
        _findField.setText(text + "\n"); 
      }        
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
  };
  
            
  
  /** Standard Constructor.
   *  @param frame the overall enclosing window
   *  @param model the model containing the documents to search
   */
  public FindReplaceDialog(MainFrame frame, SingleDisplayModel model) {
    super(frame, "Find/Replace");
    _model = model;
    _mainframe = frame;

    int i = WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
    //i = this.WHEN_FOCUSED;
    //i = this.WHEN_IN_FOCUSED_WINDOW;
    //InputMap im = _mainPanel.getInputMap(i);
    InputMap im = _findField.getInputMap(i);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Close");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "Find Next");
    //ActionMap am = _mainPanel.getActionMap();
    ActionMap am = _findField.getActionMap();
    am.put("Find Next", _findNextAction);
    am.put("Close", new AbstractAction("Close") {
      public void actionPerformed(ActionEvent ae) {
        _frame.getCurrentDefPane().requestFocusInWindow();
        _close();
      }
    });

    // Setup color listeners.
    new ForegroundColorListener(_findField);
    new BackgroundColorListener(_findField);
    new ForegroundColorListener(_replaceField);
    new BackgroundColorListener(_replaceField);
    /********* Lower Button Panel Initialization ********/
    _findNextButton = new JButton(_findNextAction);
    _replaceButton = new JButton(_replaceAction);
    _replaceFindButton = new JButton(_replaceFindAction);
    _replaceAllButton = new JButton(_replaceAllAction);
    //_closeButton = new JButton(_closeAction);
    _message = new JLabel(""); // JL

    _replaceAction.setEnabled(false);
    _replaceFindAction.setEnabled(false);

    // set up the layout
    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(1,0,5,0));
    buttons.add(_findNextButton);
    buttons.add(_replaceButton);
    buttons.add(_replaceFindButton);
    buttons.add(_replaceAllButton);
    //buttons.add(_closeButton);

    /******** Text Field Initializations ********/
    // Sets font for the "Find" field
    Font font = DrJava.getConfig().getSetting(FONT_MAIN);
    setFieldFont(font);

    // Create the Labels
    JLabel findLabel = new JLabel("Find", SwingConstants.LEFT);
    //findLabel.setLabelFor(_findField);
    findLabel.setHorizontalAlignment(SwingConstants.LEFT);

    JLabel replaceLabel = new JLabel("Replace", SwingConstants.LEFT);
    // replaceLabel.setLabelFor(_replaceField);
    replaceLabel.setHorizontalAlignment(SwingConstants.LEFT);

    //JLabel matchLabel = new JLabel("Match", SwingConstants.LEFT); // JL
    //matchLabel.setHorizontalAlignment(SwingConstants.LEFT);  // JL

    // need separate label and field panels so that the find and
    // replace textfields line up

    _labelPanel = new JPanel(new GridLayout(2,1));
    // _labelPanel.setLayout(new BoxLayout(_labelPanel, BoxLayout.Y_AXIS));

    //_labelPanel.add(Box.createGlue());
    _labelPanel.add(findLabel);
    _labelPanel.add(replaceLabel);
    // _labelPanel.add(matchLabel); // JL
    _labelPanel.setBorder(new EmptyBorder(0,5,0,5)); // 5 pix on sides

    _machine = new FindReplaceMachine(_model.getDocumentIterator());
    
    _updateMachine();
    /******** Listeners for the right-hand check boxes ********/
    
    MatchCaseListener mcl = new MatchCaseListener();
    _matchCase = new JCheckBox("Match Case", DrJava.getConfig().getSetting(OptionConstants.FIND_MATCH_CASE));
    _machine.setMatchCase(DrJava.getConfig().getSetting(OptionConstants.FIND_MATCH_CASE));
    _matchCase.addItemListener(mcl);

    SearchBackwardsListener bsl = new SearchBackwardsListener();
    _searchBackwards = new JCheckBox("Search Backwards", DrJava.getConfig().getSetting(OptionConstants.FIND_SEARCH_BACKWARDS));
    _machine.setSearchBackwards(DrJava.getConfig().getSetting(OptionConstants.FIND_SEARCH_BACKWARDS));
    _searchBackwards.addItemListener(bsl);
//    _searchBackwards.setMargin(new Insets(0,4,0,3));

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
    
    
//    haven't completed this functionality yet    
    IgnoreCommentsAndStringsListener icasl = new IgnoreCommentsAndStringsListener();
    _ignoreCommentsAndStrings = new JCheckBox("Ignore Comments and Strings", DrJava.getConfig().getSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS));
    _machine.setIgnoreCommentsAndStrings(DrJava.getConfig().getSetting(OptionConstants.FIND_NO_COMMENTS_STRINGS));
    _ignoreCommentsAndStrings.addItemListener(icasl);
    
    //FindAnyOccurrenceListener faol = new FindAnyOccurrenceListener(); // JL
    //_findAnyOccurrence = new JRadioButton("Any Occurrence"); // JL
    //_findAnyOccurrence.addActionListener(faol); // JL

    this.removeAll(); // actually, override the behavior of TabbedPanel


    /******** Initialize the panels containing the checkboxes ********/
    // remake closePanel
    _closePanel = new JPanel(new BorderLayout());
    _closePanel.add(_closeButton, BorderLayout.NORTH);

    _commentsAndStringsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _commentsAndStringsPanel.add(_ignoreCommentsAndStrings);
        
    _lowerCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _lowerCheckPanel.add(_matchWholeWord);
    _lowerCheckPanel.add(_searchAllDocuments); // JL (added)
    //_lowerCheckPanel.add(_findAnyOccurrence); // JL
    // add radiobuttons to the buttongroup
    //_radioButtonGroup = new ButtonGroup(); // JL
    //_radioButtonGroup.add(_matchWholeWord); // JL
    //_radioButtonGroup.add(_findAnyOccurrence); // JL

    _matchCaseAndClosePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    _matchCaseAndClosePanel.add(_matchCase); // JL
    _matchCaseAndClosePanel.add(_searchBackwards); // JL
    //_matchCaseAndClosePanel.add(_searchAllDocuments); // JL - moved to _radioButtonGroup
    //_matchCaseAndClosePanel.add(_matchWholeWord);
    //_matchCaseAndClosePanel.add(_findAnyOccurrence);
    _matchCaseAndClosePanel.add(_closePanel);
    //_findAnyOccurrence.setSelected(true); // JL
    
    


    /******** Set up the Panel containing the Text Fields ********/
//    _rightPanel = new JPanel(new GridLayout(1,2,5,0));
    JPanel midPanel = new JPanel(new GridLayout(2,1));
    JPanel farRightPanel = new JPanel(new GridLayout(3,1));
    midPanel.add(_findPane);
    midPanel.add(_replacePane);
//    midPanel.add(wrap(_message)); // JL
    // midPanel.add(wrap(_lowerCheckPanel)); // JL

    /******** Set up the Panel containing all 3 rows of checkboxes ********/
    farRightPanel.add(_matchCaseAndClosePanel);
    farRightPanel.add(_lowerCheckPanel); // JL
    farRightPanel.add(_commentsAndStringsPanel);
    //farRightPanel.add(_message); // JL


    /******** Set upt the Panel containing the three above main panels ********/
     
    _rightPanel = new JPanel(new BorderLayout(5, 0));
    _rightPanel.add(midPanel, BorderLayout.CENTER);
    _rightPanel.add(farRightPanel, BorderLayout.EAST);
    //_rightPanel.add(_lowerCheckPanel, BorderLayout.SOUTH);


    /******* Put all the main panels onto the Find/Replace tab ********/
    hookComponents(this, _rightPanel, _labelPanel,buttons);
    

    /******** Set the Tab order ********/
    _findField.setNextFocusableComponent(_replaceField);
    _replaceField.setNextFocusableComponent(_matchCase);
    _matchCase.setNextFocusableComponent(_searchBackwards);
    _searchBackwards.setNextFocusableComponent(_matchWholeWord); // JL (edited)
    _matchWholeWord.setNextFocusableComponent(_searchAllDocuments); // JL (edited)
    _searchAllDocuments.setNextFocusableComponent(_ignoreCommentsAndStrings); // JL (edited)
    _ignoreCommentsAndStrings.setNextFocusableComponent(_findNextButton);
    //_findAnyOccurrence.setNextFocusableComponent(_findNextButton); // JL
    _replaceAllButton.setNextFocusableComponent(_closeButton);
    _closeButton.setNextFocusableComponent(_findField);

    // DocumentListener that keeps track of changes in the find field.
    _findField.getDocument().addDocumentListener(new DocumentListener() {
      
      /**If attributes in the find field have changed, gray out "Replace" & "Replace and Find Next" buttons.
       * @param e the event caught by this listener
       */
      public void changedUpdate(DocumentEvent e) { updateHelper(); }

      /** If text has been changed in the find field, gray out "Replace" & "Replace and Find Next" buttons.
       *  @param e the event caught by this listener
       */
      public void insertUpdate(DocumentEvent e) { updateHelper(); }
      
      /** If text has been changed in the find field, gray out "Replace" & "Replace and Find Next" buttons.
       *  @param e the event caught by this listener
       */
      public void removeUpdate(DocumentEvent e) { updateHelper(); }
      
      private void updateHelper() {
        _machine.makeCurrentOffsetStart();
        _replaceAction.setEnabled(false);
        _replaceFindAction.setEnabled(false);
        _machine.positionChanged();
        if (_findField.getText().equals("")) _replaceAllAction.setEnabled(false);
        else                                 _replaceAllAction.setEnabled(true);
      }
    });
    
       
    // Overrides the default behavior for the Enter key. Replaces it with behavior that executes _doFind  
    KeyStroke returnKS = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    _findField.getKeymap().addActionForKeyStroke(returnKS, _findEnterAction);
    
//    _findField.addKeyListener(_findEnterListener);
    
    /** Listens for the Ctrl key being pressed and adds a listener for the Enter key. When Ctrl is released, the listener is removed. */
    _findField.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
//          _findField.removeKeyListener(_findEnterListener);
//          if (_findField.getKeyListeners().length <= 1 )
            _findField.addKeyListener(_newLineEnterListener); 
        }
      }
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
          _findField.removeKeyListener(_newLineEnterListener);
//          if (_findField.getKeyListeners().length <= 1)
//            _findField.addKeyListener(_findEnterListener);
          }
      }
      public void keyTyped(KeyEvent e) {}
    });
    
    
  }

  public boolean requestFocusInWindow() {
    super.requestFocusInWindow();
    _findField.selectAll();
    return _findField.requestFocusInWindow();
  }

  JTextPane getFindField() { return _findField; }

  /** Called when the user presses the key assigned to find next. */
  public void findNext() { if (_findField.getText().length() > 0) _doFind(); }

  /** Called from MainFrame upon opening this Dialog or changes in the active document. */
  public void beginListeningTo(DefinitionsPane defPane) {
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
        _replaceFindAction.setEnabled(false);
      }
      else {
        _replaceAction.setEnabled(true);
        _replaceFindAction.setEnabled(true);
        _machine.setLastFindWord();
      }

      if (_findField.getText().equals("")) _replaceAllAction.setEnabled(false);
      else                                 _replaceAllAction.setEnabled(true);

      _mainframe.clearStatusMessage(); // _message.setText(""); // JL
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
      _mainframe.clearStatusMessage(); // _message.setText(""); // JL
    } 
  }

  /** The action performed when hitting the Enter key in the find field. */
  private Action _findNextAction = new AbstractAction("Find Next") {
    public void actionPerformed(ActionEvent e) {
      _doFind();
      _findField.requestFocusInWindow();
    }
  };

  /** Abstracted out since this is called from find and replace/find. */
  private void _doFind() {
    _updateMachine();
    _machine.setFindWord(_findField.getText());
    _machine.setReplaceWord(_replaceField.getText());
    _mainframe.clearStatusMessage(); // _message.setText(""); // JL

    // FindResult contains the document that the result was found in, offset to the next occurrence of 
    // the string, and a flag indicating whether the end of the document was wrapped around while searching
    // for the string.
    FindResult fr = _machine.findNext();
    AbstractDocumentInterface doc = fr.getDocument();
    OpenDefinitionsDocument matchDoc = ((DefaultSingleDisplayModel) _model).getODDForDocument(doc);
    OpenDefinitionsDocument openDoc = _defPane.getOpenDefDocument();

    final int pos = fr.getFoundOffset();
    
    // If there actually *is* a match, then switch active documents. otherwise don't
    if (pos != -1) { // found a match
      Caret c = _defPane.getCaret();
      c.setDot(c.getDot());
      
      if (! matchDoc.equals(openDoc)) 
        _model.setActiveDocument(matchDoc);  // set active doc if matchDoc != openDoc
   
      _defPane.setCaretPosition(pos);
      _caretChanged = true;
      _updateMachine();
    }

    if (fr.getWrapped()) {
      Toolkit.getDefaultToolkit().beep();
      if (!_machine.getSearchBackwards()) {
        _mainframe.setStatusMessage("Search wrapped to beginning."); // JL
        //_message.setText("Search wrapped to beginning."); // JL
      } 
      else
        _mainframe.setStatusMessage("Search wrapped to end."); // JL
//        _message.setText("Search wrapped to end"); // JL
    }
    if (pos >= 0) {
      _selectFoundItem();

      _replaceAction.setEnabled(true);
      _replaceFindAction.setEnabled(true);
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
      _replaceFindAction.setEnabled(false);
      _replaceButton.requestFocusInWindow();
    }
  };

  private Action _replaceFindAction = new AbstractAction("Replace/Find Next") {
    public void actionPerformed(ActionEvent e) {
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
        _doFind();
        _replaceFindButton.requestFocusInWindow();
      }
      else {
        _replaceAction.setEnabled(false);
        _replaceFindAction.setEnabled(false);
        Toolkit.getDefaultToolkit().beep();
        _mainframe.setStatusMessage("Replace failed.");
//        _message.setText("Replace failed."); // JL
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
      _replaceFindAction.setEnabled(false);
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
      _machine.setDocument(doc);
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
