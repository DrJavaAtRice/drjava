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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
//import edu.rice.cs.drjava.model.DefaultDJDocument;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.ClipboardHistoryModel;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.text.SwingDocument;

// TODO: Check synchronization.
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.border.EmptyBorder;
import java.awt.datatransfer.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

/** This class contains common code and interfaces from CompilerErrorPanel, JUnitPanel, and JavadocErrorPanel.
 *  TODO: parameterize the types of CompilerErrors used here
 *  @version $Id$
 */
public abstract class ErrorPanel extends TabbedPanel implements OptionConstants {
  
  protected static final SimpleAttributeSet NORMAL_ATTRIBUTES = _getNormalAttributes();
  protected static final SimpleAttributeSet BOLD_ATTRIBUTES = _getBoldAttributes();
  
  /** The total number of errors in the list */
  protected int _numErrors;
  protected JCheckBox _showHighlightsCheckBox;
  
  // TODO: is this necessary, or can we get by with installing a domain-specific
  //       model in the constructor - e.g. JavadocModel
  protected SingleDisplayModel _model;
  
  private JScrollPane _scroller;
  
  /** This contains the _scroller and the _errorNavPanel. */
  private JPanel _leftPanel;
  
  /** This contains the label, showHighlightsCheckBox, and the customPanel. */
  private JPanel _rightPanel;
  
  private JPanel _errorNavPanel;
  
  private JPanel _errorNavButtonsPanel;
  
  /** This JPanel contains each child panel's specific UI components. **/
  protected JPanel customPanel;
  
  private JButton _nextErrorButton;
  private JButton _prevErrorButton;
  
  /** Highlight painter for selected list items. */
  static ReverseHighlighter.DefaultHighlightPainter _listHighlightPainter =
    new ReverseHighlighter.DefaultHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));
  
  protected static final SimpleAttributeSet _getBoldAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    StyleConstants.setBold(s, true);
    return s;
  }
  
  protected static final SimpleAttributeSet _getNormalAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    return s;
  }
  
  public ErrorPanel(SingleDisplayModel model, MainFrame frame, String tabString, String labelString) {
    super(frame, tabString);
    _model = model;
    
    _mainPanel.setLayout(new BorderLayout());
    
    _leftPanel = new JPanel(new BorderLayout());
    
    _errorNavPanel = new JPanel(new GridBagLayout());
    
    
    /******** Initialize the error navigation buttons ********/
    _errorNavButtonsPanel = new JPanel(new BorderLayout());
    
    _nextErrorButton = new JButton(MainFrame.getIcon("Down16.gif"));//new JButton("Next Error");
    _prevErrorButton = new JButton(MainFrame.getIcon("Up16.gif"));//new JButton("Prev Error");
    
    _nextErrorButton.setMargin(new Insets(0,0,0,0));
    _nextErrorButton.setToolTipText("Go to the next error");
    _prevErrorButton.setMargin(new Insets(0,0,0,0));
    _prevErrorButton.setToolTipText("Go to the previous error");
    
    
    //    _errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
    //    _errorPanel.setPreferredSize(new Dimension(27,35));
    //    _errorPanel.add(_prevErrorButton);
    //    _errorPanel.add(_nextErrorButton);
    //    _uiBox.add(_errorPanel, BorderLayout.WEST);
    _errorNavButtonsPanel.add(_prevErrorButton, BorderLayout.NORTH);
    _errorNavButtonsPanel.add(_nextErrorButton, BorderLayout.SOUTH);
    _errorNavButtonsPanel.setBorder(new EmptyBorder(18,5,18,5)); // 5 pix padding on sides
    
    //    JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    //    middlePanel.add(_errorNavButtonsPanel);
    
    _errorNavPanel.add(_errorNavButtonsPanel);//, BorderLayout.CENTER);
    _showHighlightsCheckBox = new JCheckBox( "Highlight source", true);
    
    //    _mainPanel.setMinimumSize(new Dimension(225,60));
    // We make the vertical scrollbar always there.
    // If we don't, when it pops up it cuts away the right edge of the
    // text. Very bad.
    _scroller = new BorderlessScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
    _leftPanel.add(_scroller, BorderLayout.CENTER);
    _leftPanel.add(_errorNavPanel, BorderLayout.EAST);
    
    customPanel = new JPanel(new BorderLayout());
    _rightPanel = new JPanel(new BorderLayout());
    _rightPanel.setBorder(new EmptyBorder(0,5,0,5)); // 5 pix padding on sides
    //    uiBox.setBorder(new EmptyBorder(5,0,0,0)); // 5 pix padding on top
    _rightPanel.add(new JLabel(labelString, SwingConstants.LEFT), BorderLayout.NORTH);
    _rightPanel.add(customPanel, BorderLayout.CENTER);
    _rightPanel.add(_showHighlightsCheckBox, BorderLayout.SOUTH);
    
    _mainPanel.add(_leftPanel, BorderLayout.CENTER);
    _mainPanel.add(_rightPanel, BorderLayout.EAST);
  }
  
  protected void setErrorListPane(final ErrorListPane elp) {
    _scroller.setViewportView(elp);
    _nextErrorButton.setEnabled(false);
    _nextErrorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        elp.nextError();
        //      _prevErrorButton.setEnabled(_errorListPane.hasPrevError());
        //      _nextErrorButton.setEnabled(_errorListPane.hasNextError());
      }
    });
    _prevErrorButton.setEnabled(false);
    _prevErrorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        elp.prevError();
        //      _prevErrorButton.setEnabled(_errorListPane.hasPrevError());
        //      _nextErrorButton.setEnabled(_errorListPane.hasNextError());
      }
    });
  }
  
  /** Changes the font of the error list. */
  public void setListFont(Font f) {
    SimpleAttributeSet set = new SimpleAttributeSet();
    StyleConstants.setFontFamily(set, f.getFamily());
    StyleConstants.setFontSize(set, f.getSize());
    StyleConstants.setBold(set, f.isBold());
    StyleConstants.setItalic(set, f.isItalic());
    
    _updateStyles(set);
    
    getErrorListPane().setFont(f);
    
    SwingDocument doc = getErrorListPane().getSwingDocument();
    doc.acquireWriteLock();
    try { doc.setCharacterAttributes(0, doc.getLength() + 1, set, false); }
    finally { doc.releaseWriteLock(); }
  }

  
  /** Updates all document styles with the attributes contained in newSet.
   *  @param newSet Style containing new attributes to use.
   */
  protected void _updateStyles(AttributeSet newSet) {
    NORMAL_ATTRIBUTES.addAttributes(newSet);
    BOLD_ATTRIBUTES.addAttributes(newSet);
    StyleConstants.setBold(BOLD_ATTRIBUTES, true);  // bold should always be bold
  }
  
  abstract protected ErrorListPane getErrorListPane();
  
  protected SingleDisplayModel getModel() {
    return _model;
  }
  
  /**
   * This function returns the correct error model
   */
  abstract protected CompilerErrorModel getErrorModel();
  
  /** Pane to show compiler errors. Similar to a listbox (clicking selects an item) but items can each wrap, etc. */
  public abstract class ErrorListPane extends JEditorPane implements ClipboardOwner {
    /** The custom keymap for the error list pane. */
    protected Keymap _keymap;
    
    /** Index into _errorListPositions of the currently selected error. */
    private int _selectedIndex;
    
    /**
     * The start position of each error in the list. This position is the place
     * where the error starts in the error list, as opposed to the place where
     * the error exists in the source.
     */
    protected Position[] _errorListPositions;
    
    /** Table mapping Positions in the error list to CompilerErrors. */
    protected final Hashtable<Position, CompilerError> _errorTable = new Hashtable<Position, CompilerError>();
    
    // when we create a highlight we get back a tag we can use to remove it
    private HighlightManager.HighlightInfo _listHighlightTag = null;
    
    private HighlightManager _highlightManager = new HighlightManager(this);
    
    protected MouseAdapter defaultMouseListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) { selectNothing(); }
      public void mouseReleased(MouseEvent e) {
        CompilerError error = _errorAtPoint(e.getPoint());
        
        if (_isEmptySelection() && error != null) getErrorListPane().switchToError(error);
        else  selectNothing();
      }
    };
    
//    private Hashtable<Position, CompilerError> _setUpErrorTable() {
//      return new Hashtable<Position, CompilerError>();
//    }
    
    /** Constructs the CompilerErrorListPane.*/
    public ErrorListPane() {
//      // If we set this pane to be of type text/rtf, it wraps based on words
//      // as opposed to based on characters.
      
      setContentType("text/rtf");
      setDocument(new SwingDocument());
      setHighlighter(new ReverseHighlighter());
      
      addMouseListener(defaultMouseListener);
      
      _selectedIndex = 0;
      _errorListPositions = new Position[0];
        
      this.setFont(new Font("Courier", 0, 20));
      
      // We set the editor pane disabled so it won't get keyboard focus,
      // which makes it uneditable, and so you can't select text inside it.
      //setEnabled(false);
      
      // Set the editor pane to be uneditable, but allow selecting text.
      setEditable(false);
      
      DrJava.getConfig().addOptionListener(COMPILER_ERROR_COLOR,
                                           new CompilerErrorColorOptionListener());
      
      // Set the colors.
      StyleConstants.setForeground(NORMAL_ATTRIBUTES,
                                   DrJava.getConfig().getSetting
                                     (DEFINITIONS_NORMAL_COLOR));
      StyleConstants.setForeground(BOLD_ATTRIBUTES,
                                   DrJava.getConfig().getSetting
                                     (DEFINITIONS_NORMAL_COLOR));
      setBackground(DrJava.getConfig().getSetting(DEFINITIONS_BACKGROUND_COLOR));
      
      // Add OptionListeners for the colors.
      DrJava.getConfig().addOptionListener(DEFINITIONS_NORMAL_COLOR,
                                           new ForegroundColorListener());
      DrJava.getConfig().addOptionListener(DEFINITIONS_BACKGROUND_COLOR,
                                           new BackgroundColorListener());
      
      /* Item listener instead of change listener so that this code won't be called (twice) every time the mouse moves
       * over the _showHighlightsCheckBox (5/26/05)
       */
      _showHighlightsCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          DefinitionsPane lastDefPane = _frame.getCurrentDefPane();
          
          if (e.getStateChange() == ItemEvent.DESELECTED) {
            lastDefPane.removeErrorHighlight();
          }
          
          else if (e.getStateChange() == ItemEvent.SELECTED) {   
            getErrorListPane().switchToError(getSelectedIndex());
// Commented out because they are redudant; done in switchToError(...)            
//          DefinitionsPane curDefPane = _frame.getCurrentDefPane(); 
//            lastDefPane.requestFocusInWindow();
//            lastDefPane.getCaret().setVisible(true);
          }
        }
      });
      
      _keymap = addKeymap("ERRORLIST_KEYMAP", getKeymap());
      
      addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_CUT), cutAction);
      addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_COPY), copyAction);
      addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PASTE_FROM_HISTORY), pasteAction);
      DrJava.getConfig().addOptionListener(OptionConstants.KEY_CUT, new OptionListener<KeyStroke>() {
        public void optionChanged(OptionEvent<KeyStroke> oe) {
          addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_CUT), cutAction);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.KEY_COPY, new OptionListener<KeyStroke>() {
        public void optionChanged(OptionEvent<KeyStroke> oe) {
          addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_COPY), copyAction);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.KEY_PASTE_FROM_HISTORY, new OptionListener<KeyStroke>() {
        public void optionChanged(OptionEvent<KeyStroke> oe) {
          addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PASTE_FROM_HISTORY), pasteAction);
        }
      });
    }
    
    /** Gets the SwingDocument associated with this ErrorListPane.  The inherited getDocument method must be preserved
     *  because the ErrorListPane constructor uses it fetch a Document that is NOT a SwingDocument.  ErrorListPane 
     *  immediately sets the Document corresponding to this JEditorPane to a SwingDocument and strictly maintains it as 
     *  a SwingDocument, but the JEditorPane constructor binds its document to a PlainDocument and uses getDocument 
     *  before ErrorListPane can set this field to a SwingDocument.
     */
    public SwingDocument getSwingDocument() { return (SwingDocument) getDocument(); }
    
    /** Assigns the given keystroke to the given action in this pane.
     *  @param stroke keystroke that triggers the action
     *  @param action Action to perform
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
    
    /** We lost ownership of what we put in the clipboard. */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
      // ignore
    }

    /** Default cut action. */
    Action cutAction = new DefaultEditorKit.CutAction() {
      public void actionPerformed(ActionEvent e) {
        if (getSelectedText()!=null) {
          super.actionPerformed(e);
          String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(ErrorListPane.this);
          if ((s!=null) && (s.length()!=0)) { ClipboardHistoryModel.singleton().put(s); }
        }
      }
    };
    
    /** Default copy action. */
    Action copyAction = new DefaultEditorKit.CopyAction() {
      public void actionPerformed(ActionEvent e) {
        if (getSelectedText()!=null) {
          super.actionPerformed(e);
          String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(ErrorListPane.this);
          if ((s!=null) && (s.length()!=0)){ ClipboardHistoryModel.singleton().put(s); }
        }
      }
    };
    
    /** No-op paste action. */
    Action pasteAction = new DefaultEditorKit.PasteAction() {
      public void actionPerformed(ActionEvent e) { }
    };
    
    /** Returns true if the errors should be highlighted in the source
     *  @return the status of the JCheckBox _showHighlightsCheckBox
     */
    public boolean shouldShowHighlightsInSource() { return _showHighlightsCheckBox.isSelected(); }
    
    /** Get the index of the current error in the error array.  */
    public int getSelectedIndex() { return _selectedIndex; }
    
    /** Returns CompilerError associated with the given visual coordinates. Returns null if none. */
    protected CompilerError _errorAtPoint(Point p) {
      int modelPos = viewToModel(p);
      
      if (modelPos == -1) return null;
      
      // Find the first error whose position preceeds this model position
      int errorNum = -1;
      for (int i = 0; i < _errorListPositions.length; i++) {
        if (_errorListPositions[i].getOffset() <= modelPos)  errorNum = i;
        else break; // we've gone past the correct error; the last value was right
      }
      
      if (errorNum >= 0) return _errorTable.get(_errorListPositions[errorNum]);
      return null;
    }
    
    /** Returns the index into _errorListPositions corresponding to the given CompilerError. */
    private int _getIndexForError(CompilerError error) {
      
      if (error == null) throw new IllegalArgumentException("Couldn't find index for null error");
      
      for (int i = 0; i < _errorListPositions.length; i++) {
        CompilerError e= _errorTable.get(_errorListPositions[i]);
        if (error.equals(e))  return i;
      }
      
      throw new IllegalArgumentException("Couldn't find index for error " + error);
    }
    
    /** Returns true if the text selection interval is empty. */
    protected boolean _isEmptySelection() { return getSelectionStart() == getSelectionEnd(); }
    
    /** Update the pane which holds the list of errors for the viewer. */
    protected void updateListPane(boolean done) {
      try {
        _errorListPositions = new Position[_numErrors];
        _errorTable.clear();
        
        if (_numErrors == 0) _updateNoErrors(done);
        else _updateWithErrors();
      }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
      
      // Force UI to redraw
//      revalidate();
    }
    
    abstract protected void _updateNoErrors(boolean done) throws BadLocationException;
    
    abstract protected void _updateWithErrors() throws BadLocationException;
    
    /** Gets the message indicating the number of errors and warnings.*/
    protected String _getNumErrorsMessage(String failureName, String failureMeaning) {
      StringBuffer numErrMsg;
      
      /** Used for display purposes only */
      int numCompErrs = getErrorModel().getNumCompErrors();
      int numWarnings = getErrorModel().getNumWarnings();     
      
      if (!getErrorModel().hasOnlyWarnings()) {
        numErrMsg = new StringBuffer(numCompErrs + " " + failureName);   //failureName = error or test (for compilation and JUnit testing respectively)
        if (numCompErrs > 1) numErrMsg.append("s");
        if (numWarnings > 0) numErrMsg.append(" and " + numWarnings + " warning");          
      }
      
      else  numErrMsg = new StringBuffer(numWarnings + " warning"); 
      
      if (numWarnings > 1) numErrMsg.append("s");
     
      numErrMsg.append(" " + failureMeaning + ":\n");
      return numErrMsg.toString();
    }
    
    /**
     * Gets the message to title the block containing only errors.
     */
    protected String _getErrorTitle() {
      CompilerErrorModel cem = getErrorModel();
      if (cem.getNumCompErrors() > 1)
        return "--------------\n*** Errors ***\n--------------\n";
      if (cem.getNumCompErrors() > 0)
        return "-------------\n*** Error ***\n-------------\n";
      return "";
    }
      
    /**
     * Gets the message to title the block containing only warnings.
     */
    protected String _getWarningTitle() {
      CompilerErrorModel cem = getErrorModel();
      if (cem.getNumWarnings() > 1)
        return "--------------\n** Warnings **\n--------------\n";
      if (cem.getNumWarnings() > 0)
        return "-------------\n** Warning **\n-------------\n";
      return "";
    }
        
    /** Used to show that the last compile was unsuccessful.*/
    protected void _updateWithErrors(String failureName, String failureMeaning, SwingDocument doc)
      throws BadLocationException {
      // Print how many errors
      String numErrsMsg = _getNumErrorsMessage(failureName, failureMeaning);
      doc.append(numErrsMsg, BOLD_ATTRIBUTES);
      
      _insertErrors(doc);
      setDocument(doc);
      
      // Select the first error if there are some errors (i.e. does not select if there are only warnings)
      if (!getErrorModel().hasOnlyWarnings())
        getErrorListPane().switchToError(0);
    }
    
    /** Returns true if there is an error after the selected error. */
    public boolean hasNextError() { return this.getSelectedIndex() + 1 < _numErrors; }
    
    /** Returns true if there is an error before the selected error. */
    public boolean hasPrevError() { return this.getSelectedIndex() > 0; }
    
    /** Switches to the next error. */
    public void nextError() {
      // Select the error
      if (hasNextError()) {
        this._selectedIndex += 1;
//        Utilities.showDebug("selected index in nextError is " + _selectedIndex + " _numErrors is " + _numErrors);
        getErrorListPane().switchToError(this.getSelectedIndex());
      }
    }
    
    /** Switches to the previous error. */
    public void prevError() {
      // Select the error
      if (hasPrevError()) {
        this._selectedIndex -= 1;
        getErrorListPane().switchToError(this.getSelectedIndex());
      }
    }
    
    /** Inserts all of the errors into the given document.
     *  @param doc the document into which to insert the errors
     */
    protected void _insertErrors(SwingDocument doc) throws BadLocationException {
      CompilerErrorModel cem = getErrorModel();
      int numErrors = cem.getNumErrors();
      
      //Added this counter in order to add errors and warnings in correct order and select them correctly
      //Previous version used errorNum as a counter, but this doesn't work anymore because we are not doing
      //errors and variables at the same time.
      int errorPositionInListOfErrors = 0;
      // Show errors first and warnings second
      
      String errorTitle = _getErrorTitle();
      if (cem.getNumWarnings() > 0)   
        doc.append(errorTitle, BOLD_ATTRIBUTES);
      
      for (int errorNum = 0; errorNum < numErrors; errorNum++) {
        int startPos = doc.getLength();
        CompilerError err = cem.getError(errorNum);
        
        if (!err.isWarning()){
          _insertErrorText(err, doc);
          Position pos = doc.createPosition(startPos);
          _errorListPositions[errorPositionInListOfErrors] = pos;
          _errorTable.put(pos, err);
          errorPositionInListOfErrors++;
        }
      }
      
      String warningTitle = _getWarningTitle();
      if (cem.getNumCompErrors() > 0)   
        doc.append(warningTitle, BOLD_ATTRIBUTES);
      
      for (int errorNum = 0; errorNum < numErrors; errorNum++) {
        int startPos = doc.getLength();
        CompilerError err = cem.getError(errorNum);
        
        if (err.isWarning()){
          _insertErrorText(err, doc);
          Position pos = doc.createPosition(startPos);
          _errorListPositions[errorPositionInListOfErrors] = pos;
          _errorTable.put(pos, err);
          errorPositionInListOfErrors++;
        }
      }      
    }
    
    /** Prints a message for the given error
     *  @param error the error to print
     *  @param doc the document in the error pane
     */
    protected void _insertErrorText(CompilerError error, SwingDocument doc) throws BadLocationException {
      // Show file and line number
      doc.append("File: ", BOLD_ATTRIBUTES);
      String fileAndLineNumber = error.getFileMessage() + "  [line: " + error.getLineMessage() + "]";
      doc.append(fileAndLineNumber + "\n", NORMAL_ATTRIBUTES);
      
      if (error.isWarning()) doc.append(_getWarningText(), BOLD_ATTRIBUTES);
      else doc.append(_getErrorText(), BOLD_ATTRIBUTES);
      
      doc.append(error.message(), NORMAL_ATTRIBUTES);
      doc.append("\n", NORMAL_ATTRIBUTES);
    }
    
    /** Returns the string to identify a warning. */
    protected String _getWarningText() { return "Warning: "; }
    
    /** Returns the string to identify an error. */
    protected String _getErrorText() { return "Error: "; }
    
    /** When the selection of the current error changes, remove the highlight in the error pane. */
    protected void _removeListHighlight() {
      if (_listHighlightTag != null) {
        _listHighlightTag.remove();
        _listHighlightTag = null;
      }
      //      _prevErrorButton.setEnabled(false);
      //      _nextErrorButton.setEnabled(false);
    }
    
    /** Don't select any errors in the error pane. */
    public void selectNothing() {
      //      _selectedIndex = -1;
      _removeListHighlight();
      
      // Remove highlight from the defPane that has it
      _frame.getCurrentDefPane().removeErrorHighlight();
    }
    
    /** Selects the given error inside the error list pane. */
    public void selectItem(CompilerError error) {
//      Utilities.showDebug("selectItem(" + error + ") called");
      try {
        // Find corresponding index
        int i = _getIndexForError(error);
        
        _selectedIndex = i;
//        Utilities.showDebug("selected index = " + i);
        _removeListHighlight();
        
        int startPos = _errorListPositions[i].getOffset();
//        Utilities.showDebug("startPos = " + startPos);
        
        // end pos is either the end of the document (if this is the last error)
        // or the end of the error if the last error (i.e. before the warnings title)
        // or the char where the next error starts
        int endPos;
        if (i + 1 >= (_numErrors)) endPos = getDocument().getLength();   
        else { 
          endPos = _errorListPositions[i + 1].getOffset();
//          Utilities.showDebug("endPos(before) = " + endPos);
          CompilerError nextError = _errorTable.get(_errorListPositions[i+1]);
//          Utilities.showDebug("nextError = " + nextError);
          if (!error.isWarning() && nextError.isWarning()) endPos = endPos - _getWarningTitle().length();
//          Utilities.showDebug("endPos(after) = " + endPos);
        }            
        
//        Utilities.showDebug("startpos = " + startPos + " endpos = " + endPos);
        
        try {
          _listHighlightTag = _highlightManager.addHighlight(startPos, endPos, _listHighlightPainter);
          
          // If first error, show number of errors and warnings preferentially to showing the error
          // Otherwise, scroll to make sure this item is visible
          Rectangle startRect;
          if (i == 0)  startRect = modelToView(0);
          
          else startRect = modelToView(startPos);
          
          Rectangle endRect = modelToView(endPos - 1);
          
          if (startRect != null && endRect != null) {
            // Add the end rect onto the start rect to make a rectangle
            // that encompasses the entire error
            startRect.add(endRect);
            
            //System.err.println("scrll vis: " + startRect);
            
            scrollRectToVisible(startRect);
            _updateScrollButtons();
          }
          else {
//            Utilities.showDebug("Either startRect or endRect is null!");
            // Couldn't draw the box to highlight, so don't highlight anything
            _removeListHighlight();
          }
        }
        catch (BadLocationException badBadLocation) { }
        
      }
      catch (IllegalArgumentException iae) {
        // This shouldn't be happening, but it was reported in bug 704006.
        // (_getIndexForError throws it.)
        // We'll at least fail a little more gracefully.
        _removeListHighlight();
      }
    }
    
    protected void _updateScrollButtons() {
      if (hasNextError()) {
        _nextErrorButton.setEnabled(true);
      }
      else {
        _nextErrorButton.setEnabled(false);
      }
      if (hasPrevError()) {
        _prevErrorButton.setEnabled(true);
      }
      else {
        _prevErrorButton.setEnabled(false);
      }
    }
    
    /** Change all state to select a new error, including moving the caret to the error, if a corresponding position
     *  exists.
     *  @param error The error to switch to
     */
    void switchToError(CompilerError error) {
//      Utilities.showDebug("ErrorPanel.switchToError called");
      if (error == null) return;
      
      SingleDisplayModel model = getModel();
      
      DefinitionsPane prevPane = _frame.getCurrentDefPane();
      prevPane.removeErrorHighlight();  // hide previous error highlight
      OpenDefinitionsDocument prevDoc = prevPane.getOpenDefDocument();
      
      if (error.file() != null) {
        try {
          OpenDefinitionsDocument doc = model.getDocumentForFile(error.file());
          CompilerErrorModel errorModel = getErrorModel();
          
          Position pos = errorModel.getPosition(error); // null if error has no Position
//          Utilities.showDebug("The position of the error is: " + pos);
          // switch to correct def pane and move caret to error position
//          Utilities.showDebug("active document being set to " + doc + " in ErrorPanel.switchToError");
          
          if (! prevDoc.equals(doc)) model.setActiveDocument(doc);
          else model.refreshActiveDocument();
          
//          Utilities.showDebug("setting active document has completed");
          
          DefinitionsPane defPane = _frame.getCurrentDefPane();
          
          if (pos != null) {
            int errPos = pos.getOffset();
            if (errPos >= 0 && errPos <= doc.getLength()) {
              defPane.centerViewOnOffset(errPos);
              
              /* The folowing fixes a bug where, if two consecutive errors are in the same position, the previous error
               * is unhighlighted and the new error is not highlighted because the CaretListener does not act because there
               * is no change in caret position. (This is the only place where updateHighlight was called from before) */
              defPane.getErrorCaretListener().updateHighlight(errPos);
            }
            
          }
          // The following line is a brute force hack that fixed a bug plaguing the DefinitionsPane immediately after a compilation
          // with errors.  In some cases (which were consistently reproducible), the DefinitionsPane editing functions would break
          // whereby the keystrokes had their usual meaning but incorrect updates were performed in the DefintionsPane.  For example,
          // the display behaved as if the editor were in "overwrite" mode.
//          _frame._switchDefScrollPane(); // resets an out-of-kilter DefinitionsPane on the first error after a compilation
          defPane.requestFocusInWindow();
          defPane.getCaret().setVisible(true);
        }
        catch (IOException ioe) {
          // Don't highlight the source if file can't be opened
        }
      }
//      Utilities.showDebug("Calling selectItem(...) from switchToError");
      /* setActiveDocument(doc) selects the first error corresponding to the current position (caret location) but this may not
       * be the correct error if there are multiple errors for this this position.  The following selects the correct error.*/
      getErrorListPane().selectItem(error); 
    }
    
    
    /** Another interface to switchToError.
     *  @param index Index into the array of positions in the CompilerErrorListPane
     */
    void switchToError(int index) {
      if ((index >= 0) && (index < _errorListPositions.length)) {
        Position pos = _errorListPositions[index];
        CompilerError error= _errorTable.get(pos);
        switchToError(error);
      }
    }
    
    /** The OptionListener for compiler COMPILER_ERROR_COLOR */
    private class CompilerErrorColorOptionListener implements OptionListener<Color> {
      
      public void optionChanged(OptionEvent<Color> oce) {
        _listHighlightPainter = new ReverseHighlighter.DefaultHighlightPainter(oce.value);
        if (_listHighlightTag != null) {
          _listHighlightTag.refresh(_listHighlightPainter);
        }
      }
    }
    
    /** The OptionListener for compiler DEFINITIONS_NORMAL_COLOR */
    private class ForegroundColorListener implements OptionListener<Color> {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(NORMAL_ATTRIBUTES, oce.value);
        StyleConstants.setForeground(BOLD_ATTRIBUTES, oce.value);
        
        // Re-attribute the existing text with the new color.
        SwingDocument doc = getErrorListPane().getSwingDocument();
        SimpleAttributeSet set = new SimpleAttributeSet();
        set.addAttribute(StyleConstants.Foreground, oce.value);
        doc.acquireWriteLock();
        try { doc.setCharacterAttributes(0, doc.getLength(), set, false); }
        finally { doc.releaseWriteLock(); }
        //        ErrorListPane.this.repaint();
      }
    }
    
    /** The OptionListener for compiler DEFINITIONS_BACKGROUND_COLOR. */
    private class BackgroundColorListener implements OptionListener<Color> {
      public void optionChanged(OptionEvent<Color> oce) {
        setBackground(oce.value);
        ErrorListPane.this.repaint();
      }
    }
  }
}