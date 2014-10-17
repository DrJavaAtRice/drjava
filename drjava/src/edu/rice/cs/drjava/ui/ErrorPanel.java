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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.ClipboardHistoryModel;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.HighlightManager;
import edu.rice.cs.util.swing.BorderlessScrollPane;
import edu.rice.cs.util.text.SwingDocument;
import edu.rice.cs.drjava.model.print.DrJavaBook;
  
import edu.rice.cs.util.swing.RightClickMouseAdapter;

import java.util.HashMap;
import java.util.Vector;

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
import java.awt.print.*;
import java.io.IOException;

/** This class contains common code and interfaces from CompilerErrorPanel, JUnitPanel, and JavadocErrorPanel.
 *  TODO: parameterize the types of CompilerErrors (which should be called DJErrors) used here
 *  @version $Id: ErrorPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public abstract class ErrorPanel extends TabbedPanel implements OptionConstants {
  
  protected static final SimpleAttributeSet NORMAL_ATTRIBUTES = _getNormalAttributes();
  protected static final SimpleAttributeSet BOLD_ATTRIBUTES = _getBoldAttributes();
  
  /** The total number of errors in the list */
  protected volatile int _numErrors;
  protected volatile JCheckBox _showHighlightsCheckBox;
  
  protected volatile SingleDisplayModel _model;
  
  private volatile JScrollPane _scroller;
  
  /** This contains the _scroller and the _errorNavPanel. */
  private volatile JPanel _leftPanel;
  
  /** This contains the label, showHighlightsCheckBox, and the customPanel. */
  private volatile JPanel _rightPanel;
  
  private volatile JPanel _errorNavPanel;
  
  private volatile JPanel _errorNavButtonsPanel;
  
  /** This JPanel contains each child panel's specific UI components. **/
  protected volatile JPanel customPanel;
  
  private volatile JButton _nextErrorButton;
  private volatile JButton _prevErrorButton;
  
  /** _popupMenu and _popupMenuListener are either both null or both non-null. */
  protected volatile JPopupMenu _popupMenu = null;
  protected volatile RightClickMouseAdapter _popupMenuListener = null;
  
  /** Highlight painter for selected list items. */
  static volatile ReverseHighlighter.DrJavaHighlightPainter _listHighlightPainter =
    new ReverseHighlighter.DrJavaHighlightPainter(DrJava.getConfig().getSetting(COMPILER_ERROR_COLOR));
  
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
    
    _nextErrorButton.setMargin(new Insets(0, 0, 0, 0));
    _nextErrorButton.setToolTipText("Go to the next error");
    _prevErrorButton.setMargin(new Insets(0, 0, 0, 0));
    _prevErrorButton.setToolTipText("Go to the previous error");
    
    
    //    _errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
    //    _errorPanel.setPreferredSize(new Dimension(27,35));
    //    _errorPanel.add(_prevErrorButton);
    //    _errorPanel.add(_nextErrorButton);
    //    _uiBox.add(_errorPanel, BorderLayout.WEST);
    _errorNavButtonsPanel.add(_prevErrorButton, BorderLayout.NORTH);
    _errorNavButtonsPanel.add(_nextErrorButton, BorderLayout.SOUTH);
    _errorNavButtonsPanel.setBorder(new EmptyBorder(18, 5, 18, 5)); // 5 pix padding on sides
    
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
    
    /** Default copy action.  Returns focus to the correct pane. */
    final Action copyAction = new AbstractAction("Copy Contents to Clipboard", MainFrame.getIcon("Copy16.gif")) {
      public void actionPerformed(ActionEvent e) {
        getErrorListPane().selectAll();
        String t = getErrorListPane().getSelectedText();
        if (t != null) {
          if (t.length() != 0) {
            StringSelection stringSelection = new StringSelection(t);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, getErrorListPane());
            ClipboardHistoryModel.singleton().put(t);
          }
        }
      }
    };
    addPopupMenu(copyAction);
    getPopupMenu().add(new AbstractAction("Save Copy of Contents...", MainFrame.getIcon("Save16.gif")) {
      public void actionPerformed(ActionEvent e) {
        _frame._saveDocumentCopy(getErrorListPane().getErrorDocument());
      }
    });
    getPopupMenu().addSeparator();
    getPopupMenu().add(new AbstractAction("Print...", MainFrame.getIcon("Print16.gif")) {
      public void actionPerformed(ActionEvent e) {
        getErrorListPane().getErrorDocument().print();
      }
    });
    getPopupMenu().add(new AbstractAction("Print Preview...", MainFrame.getIcon("PrintPreview16.gif")) {
      public void actionPerformed(ActionEvent e) {
        getErrorListPane().getErrorDocument().preparePrintJob();
        new PreviewErrorFrame();
      }
    });
  }
  
  protected void setErrorListPane(final ErrorListPane elp) {
    if (_popupMenuListener!=null) {
      if ((_scroller!=null) &&
          (_scroller.getViewport()!=null) &&
          (_scroller.getViewport().getView()!=null)) {
        _scroller.getViewport().getView().removeMouseListener(_popupMenuListener);
      }
    }
    
    _scroller.setViewportView(elp);
    
    if (_popupMenuListener!=null) {
      _scroller.getViewport().getView().addMouseListener(_popupMenuListener);
    }
    
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
    
    ErrorDocument doc = getErrorListPane().getErrorDocument();
    doc.setCharacterAttributes(0, doc.getLength() + 1, set, false); 
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
  
  protected SingleDisplayModel getModel() { return _model; }
  
  /** This function returns the correct error model  */
  abstract protected CompilerErrorModel getErrorModel();
  
  /** Pane to show compiler errors. Similar to a listbox (clicking selects an item) but items can each wrap, etc. */
  public abstract class ErrorListPane extends JEditorPane implements ClipboardOwner {
    /** The custom keymap for the error list pane. */
    protected volatile Keymap _keymap;
    
    /** Index into _errorListPositions of the currently selected error. */
    private volatile int _selectedIndex;
    
    /**
     * The start position of each error in the list. This position is the place
     * where the error starts in the error list, as opposed to the place where
     * the error exists in the source.
     */
    protected volatile Position[] _errorListPositions;
    
    /** Table mapping Positions in the error list to CompilerErrors. */
    protected final HashMap<Position, DJError> _errorTable = new HashMap<Position, DJError>();
    
    // when we create a highlight we get back a tag we can use to remove it
    private volatile HighlightManager.HighlightInfo _listHighlightTag = null;
    
    private volatile HighlightManager _highlightManager = new HighlightManager(this);
    
    /** Default cut action. */
    volatile Action cutAction = new DefaultEditorKit.CutAction() {
      public void actionPerformed(ActionEvent e) {
        if (getSelectedText() != null) {
          super.actionPerformed(e);
          String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(ErrorListPane.this);
          if ((s != null) && (s.length() != 0)) { ClipboardHistoryModel.singleton().put(s); }
        }
      }
    };
    
    /** Default copy action. */
    volatile Action copyAction = new DefaultEditorKit.CopyAction() {
      public void actionPerformed(ActionEvent e) {
        if (getSelectedText() != null) {
          super.actionPerformed(e);
          String s = edu.rice.cs.util.swing.Utilities.getClipboardSelection(ErrorListPane.this);
          if (s != null && s.length() != 0) { ClipboardHistoryModel.singleton().put(s); }
        }
      }
    };
    
    /** No-op paste action. */
    volatile Action pasteAction = new DefaultEditorKit.PasteAction() {
      public void actionPerformed(ActionEvent e) { }
    };
     
    protected MouseAdapter defaultMouseListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) { selectNothing(); }
      public void mouseReleased(MouseEvent e) {
        DJError error = _errorAtPoint(e.getPoint());
        
        if (_isEmptySelection() && error != null) getErrorListPane().switchToError(error);
        else  selectNothing();
      }
    };
    
//    private Hashtable<Position, DJError> _setUpErrorTable() {
//      return new Hashtable<Position, DJError>();
//    }
    
    /** Constructs the CompilerErrorListPane.*/
    public ErrorListPane() {
//      // If we set this pane to be of type text/rtf, it wraps based on words
//      // as opposed to based on characters.
      
      setContentType("text/rtf");
      setDocument(new ErrorDocument(getErrorDocumentTitle()));
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
      
      DrJava.getConfig().addOptionListener(COMPILER_ERROR_COLOR, new CompilerErrorColorOptionListener());
      
      // Set the colors.
      StyleConstants.setForeground(NORMAL_ATTRIBUTES, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
      StyleConstants.setForeground(BOLD_ATTRIBUTES, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
      setBackground(DrJava.getConfig().getSetting(DEFINITIONS_BACKGROUND_COLOR));
      
      // Add OptionListeners for the colors.
      DrJava.getConfig().addOptionListener(DEFINITIONS_NORMAL_COLOR, new ForegroundColorListener());
      DrJava.getConfig().addOptionListener(DEFINITIONS_BACKGROUND_COLOR, new BackgroundColorListener());
      
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
      DrJava.getConfig().addOptionListener(OptionConstants.KEY_CUT, new OptionListener<Vector<KeyStroke>>() {
        public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
          addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_CUT), cutAction);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.KEY_COPY, new OptionListener<Vector<KeyStroke>>() {
        public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
          addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_COPY), copyAction);
        }
      });
      DrJava.getConfig().addOptionListener(OptionConstants.KEY_PASTE_FROM_HISTORY, new OptionListener<Vector<KeyStroke>>() {
        public void optionChanged(OptionEvent<Vector<KeyStroke>> oe) {
          addActionForKeyStroke(DrJava.getConfig().getSetting(OptionConstants.KEY_PASTE_FROM_HISTORY), pasteAction);
        }
      });
    }
    
    /** Gets the ErrorDocument associated with this ErrorListPane.  The inherited getDocument method must be preserved
      * because the ErrorListPane constructor uses it fetch a Document that is NOT an ErrorDocument.  ErrorListPane 
      * immediately sets the Document corresponding to this JEditorPane to an ErrorDocument and strictly maintains it as 
      * an ErrorDocument, but the JEditorPane constructor binds its document to a PlainDocument and uses getDocument 
      * before ErrorListPane can set this field to an ErrorDocument.
      */
    public ErrorDocument getErrorDocument() { return (ErrorDocument) getDocument(); }
    
    /** Assigns the given keystroke to the given action in this pane.
     *  @param stroke keystroke that triggers the action
     *  @param action Action to perform
     */
    public void addActionForKeyStroke(Vector<KeyStroke> stroke, Action action) {
      // remove previous bindings
      KeyStroke[] keys = _keymap.getKeyStrokesForAction(action);
      if (keys != null) {
        for (int i = 0; i < keys.length; i++) {
          _keymap.removeKeyStrokeBinding(keys[i]);
        }
      }
      for (KeyStroke ks: stroke) {
        _keymap.addActionForKeyStroke(ks, action);
      }
      setKeymap(_keymap);
    }
    
    /** We lost ownership of what we put in the clipboard. */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
      // ignore
    }

  
    /** Returns true if the errors should be highlighted in the source
     *  @return the status of the JCheckBox _showHighlightsCheckBox
     */
    public boolean shouldShowHighlightsInSource() { return _showHighlightsCheckBox.isSelected(); }
    
    /** Get the index of the current error in the error array.  */
    public int getSelectedIndex() { return _selectedIndex; }
    
    /** Returns DJError associated with the given visual coordinates. Returns null if none. */
    protected DJError _errorAtPoint(Point p) {
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
    
    /** Returns the index into _errorListPositions corresponding to the given DJError. */
    private int _getIndexForError(DJError error) {
      
      if (error == null) throw new IllegalArgumentException("Couldn't find index for null error");
      
      for (int i = 0; i < _errorListPositions.length; i++) {
        DJError e= _errorTable.get(_errorListPositions[i]);
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
      repaint();
    }
    
    abstract protected void _updateNoErrors(boolean done) throws BadLocationException;
    
    abstract protected void _updateWithErrors() throws BadLocationException;
    
    /** Gets the message indicating the number of errors and warnings.*/
    protected String _getNumErrorsMessage(String failureName, String failureMeaning) {
      StringBuilder numErrMsg;
      
      /** Used for display purposes only */
      int numCompErrs = getErrorModel().getNumCompilerErrors();
      int numWarnings = getErrorModel().getNumWarnings();     
      
      if (!getErrorModel().hasOnlyWarnings()) {
        //failureName = error or test (for compilation and JUnit testing respectively)
        numErrMsg = new StringBuilder(numCompErrs + " " + failureName);   
        if (numCompErrs > 1) numErrMsg.append("s");
        if (numWarnings > 0) numErrMsg.append(" and " + numWarnings + " warning");          
      }
      
      else numErrMsg = new StringBuilder(numWarnings + " warning"); 
      
      if (numWarnings > 1) numErrMsg.append("s");
     
      numErrMsg.append(" " + failureMeaning + ":\n");
      return numErrMsg.toString();
    }
    
    /** Gets the message to title the block containing only errors. */
    protected String _getErrorTitle() {
      CompilerErrorModel cem = getErrorModel();
      if (cem.getNumCompilerErrors() > 1) return "--------------\n*** Errors ***\n--------------\n";
      if (cem.getNumCompilerErrors() > 0) return "-------------\n*** Error ***\n-------------\n";
      return "";
    }
      
    /** Gets the message to title the block containing only warnings. */
    protected String _getWarningTitle() {
      CompilerErrorModel cem = getErrorModel();
      if (cem.getNumWarnings() > 1) return "--------------\n** Warnings **\n--------------\n";
      if (cem.getNumWarnings() > 0) return "-------------\n** Warning **\n-------------\n";
      return "";
    }
        
    /** Used to show that the last compile was unsuccessful.*/
    protected void _updateWithErrors(String failureName, String failureMeaning, ErrorDocument doc)
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
    protected void _insertErrors(ErrorDocument doc) throws BadLocationException {
      CompilerErrorModel cem = getErrorModel();
      int numErrors = cem.getNumErrors();
      
      //Added this counter in order to add errors and warnings in correct order and select them correctly
      //Previous version used errorNum as a counter, but this doesn't work anymore because we are not doing
      //errors and variables at the same time.
      int errorPositionInListOfErrors = 0;
      // Show errors first and warnings second
      
      String errorTitle = _getErrorTitle();
      if (cem.getNumWarnings() > 0) doc.append(errorTitle, BOLD_ATTRIBUTES);
      
      for (int errorNum = 0; errorNum < numErrors; errorNum++) {
        int startPos = doc.getLength();
        DJError err = cem.getError(errorNum);
        
        if (!err.isWarning()) {
          _insertErrorText(err, doc);
          Position pos = doc.createPosition(startPos);
          _errorListPositions[errorPositionInListOfErrors] = pos;
          _errorTable.put(pos, err);
          errorPositionInListOfErrors++;
        }
      }
      
      String warningTitle = _getWarningTitle();
      if (cem.getNumCompilerErrors() > 0) doc.append(warningTitle, BOLD_ATTRIBUTES);
      
      for (int errorNum = 0; errorNum < numErrors; errorNum++) {
        int startPos = doc.getLength();
        DJError err = cem.getError(errorNum);
        
        if (err.isWarning()) {
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
    protected void _insertErrorText(DJError error, ErrorDocument doc) throws BadLocationException {
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
    public void selectItem(DJError error) {
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
          DJError nextError = _errorTable.get(_errorListPositions[i+1]);
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
    void switchToError(DJError error) {
//      Utilities.showDebug("ErrorPanel.switchToError called");
      if (error == null) return;
      
      final SingleDisplayModel model = getModel();
      
      DefinitionsPane prevPane = _frame.getCurrentDefPane();
      prevPane.removeErrorHighlight();  // hide previous error highlight
      OpenDefinitionsDocument prevDoc = prevPane.getOpenDefDocument();
      
      if (error.file() != null) {
        try {          
          boolean open = false;
          for(OpenDefinitionsDocument doc : model.getOpenDefinitionsDocuments()) {
            if((doc.getFile() != null) && (doc.getFile().equals(error.file()))) {
              open = true;
              break;
            }
          }
          
          if(open) {
            OpenDefinitionsDocument doc = model.getDocumentForFile(error.file());
            CompilerErrorModel errorModel = getErrorModel();
            
            Position pos = errorModel.getPosition(error); // null if error has no Position
//          Utilities.showDebug("The position of the error is: " + pos);
            // switch to correct def pane and move caret to error position
//          Utilities.showDebug("active document being set to " + doc + " in ErrorPanel.switchToError");
            
            if (! prevDoc.equals(doc)) {
              model.setActiveDocument(doc);
              EventQueue.invokeLater(new Runnable() { 
                public void run() { 
                  model.addToBrowserHistory(); 
                } });
            }
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
            // whereby the keystrokes had their usual meaning but incorrect updates were performed in the DefinitionsPane.  For example,
            // the display behaved as if the editor were in "overwrite" mode.
//          _frame._switchDefScrollPane(); // resets an out-of-kilter DefinitionsPane on the first error after a compilation
            defPane.requestFocusInWindow();
            defPane.getCaret().setVisible(true);
          }
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
        DJError error= _errorTable.get(pos);
        switchToError(error);
      }
    }
    
    /** The OptionListener for compiler COMPILER_ERROR_COLOR */
    private class CompilerErrorColorOptionListener implements OptionListener<Color> {
      
      public void optionChanged(OptionEvent<Color> oce) {
        _listHighlightPainter = new ReverseHighlighter.DrJavaHighlightPainter(oce.value);
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
        ErrorDocument doc = getErrorListPane().getErrorDocument();
        SimpleAttributeSet set = new SimpleAttributeSet();
        set.addAttribute(StyleConstants.Foreground, oce.value);
        doc.setCharacterAttributes(0, doc.getLength(), set, false); 
      }
    }
    
    /** The OptionListener for compiler DEFINITIONS_BACKGROUND_COLOR. */
    private class BackgroundColorListener implements OptionListener<Color> {
      public void optionChanged(OptionEvent<Color> oce) {
        setBackground(oce.value);
        ErrorListPane.this.repaint();
      }
    }
    public String getErrorDocumentTitle() { return "Errors"; }
  }
  
  public class ErrorDocument extends SwingDocument {
    protected volatile DrJavaBook _book;
    protected final String _title;
    public ErrorDocument(String t) { _title = t; }
    public Pageable getPageable() throws IllegalStateException { return _book; }
    public void preparePrintJob() {
      _book = new DrJavaBook(getDocText(0, getLength()), _title, new PageFormat());
    }
    public void print() {
      preparePrintJob();
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPageable(_book);
      try {
        if (printJob.printDialog()) printJob.print();
      }
      catch(PrinterException e) {
        MainFrameStatics.showError(_frame, e, "Print Error", "An error occured while printing.");
      }
      cleanUpPrintJob();
    }
    public void cleanUpPrintJob() { _book = null; }
  }
  
  public class PreviewErrorFrame extends PreviewFrame {
    public PreviewErrorFrame() throws IllegalStateException {
      super(ErrorPanel.this._model, ErrorPanel.this._frame, false);
    }
    protected Pageable setUpDocument(SingleDisplayModel model, boolean interactions) {
      return getErrorListPane().getErrorDocument().getPageable();
    }    
    protected void _print() {
      getErrorListPane().getErrorDocument().print();
    }
  }
  
  public JPopupMenu getPopupMenu() { return _popupMenu; }
  
  public void addPopupMenu(Action... actions) {
    if (_popupMenu==null) {
      _popupMenu = new JPopupMenu();
    }
    else {
      _popupMenu.removeAll();
      removeMouseListener(_popupMenuListener);
      _scroller.removeMouseListener(_popupMenuListener);
      _scroller.getViewport().getView().removeMouseListener(_popupMenuListener);      
    }
    for(Action a: actions) { _popupMenu.add(a); }
    _popupMenuListener = new RightClickMouseAdapter() {
      protected void _popupAction(MouseEvent e) {
        requestFocusInWindow();
        _popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    };
    addMouseListener(_popupMenuListener);
    if (_scroller!=null) {
      _scroller.addMouseListener(_popupMenuListener);
      if (_scroller.getViewport().getView()!=null) {
        _scroller.getViewport().getView().addMouseListener(_popupMenuListener);
      }
    }
  }
}