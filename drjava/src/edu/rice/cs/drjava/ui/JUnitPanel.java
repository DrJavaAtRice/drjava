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

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.junit.JUnitErrorModel;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.BorderlessScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The panel which displays all the testing errors.
 * In the future, it may also contain a progress bar.
 *
 * @version $Id$
 */
public class JUnitPanel extends ErrorPanel {
  private static final String START_JUNIT_MSG = "Testing in progress.  Please wait...\n";
  private static final String JUNIT_FINISHED_MSG = "All tests completed successfully.\n";
  private static final String NO_TESTS_MSG = "";

  private static final SimpleAttributeSet OUT_OF_SYNC_ATTRIBUTES = _getOutOfSyncAttributes();
  private static final SimpleAttributeSet _getOutOfSyncAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    s.addAttribute(StyleConstants.Foreground, Color.red.darker());
    s.addAttribute(StyleConstants.Bold, Boolean.TRUE);
    return s;
  }

  private static final SimpleAttributeSet TEST_PASS_ATTRIBUTES = _getTestPassAttributes();
  private static final SimpleAttributeSet _getTestPassAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    s.addAttribute(StyleConstants.Foreground, Color.green.darker());
    return s;
  }

  private static final SimpleAttributeSet TEST_FAIL_ATTRIBUTES = _getTestFailAttributes();
  private static final SimpleAttributeSet _getTestFailAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    s.addAttribute(StyleConstants.Foreground, Color.red);
    return s;
  }

  private static final String TEST_OUT_OF_SYNC =
    "The document(s) being tested have been modified and should be recompiled!\n";

  protected JUnitErrorListPane _errorListPane;
  private int _testCount;
  private boolean _testsSuccessful;

  private JUnitProgressBar _progressBar;
  private List<OpenDefinitionsDocument> _odds = new ArrayList<OpenDefinitionsDocument>();

  private Action _showStackTraceAction = new AbstractAction("Show Stack Trace") {
        public void actionPerformed(ActionEvent ae) {
          if (_error != null) {
            _displayStackTrace(_error);
          }
        }
      };

  private JButton _showStackTraceButton;

  /** The currently selected error. */
  private JUnitError _error = null;
  private Window _stackFrame = null;
  private JTextArea _stackTextArea;
  private final JLabel _errorLabel = new JLabel();
  private final JLabel _testLabel = new JLabel();
  private final JLabel _fileLabel = new JLabel();

  /**
   * Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public JUnitPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Test Output", "Test Progress");
    _testCount = 0;
    _testsSuccessful = true;

    _progressBar = new JUnitProgressBar();
    _progressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI());
    _showStackTraceButton = new JButton(_showStackTraceAction);
    customPanel.add(_progressBar, BorderLayout.NORTH);
    customPanel.add(_showStackTraceButton, BorderLayout.SOUTH);

    _errorListPane = new JUnitErrorListPane();
    setErrorListPane(_errorListPane);

  }

  /** Returns the JUnitErrorListPane that this panel manages. */
  public JUnitErrorListPane getErrorListPane() { return _errorListPane; }

  protected JUnitErrorModel getErrorModel(){ 
    return getModel().getJUnitModel().getJUnitErrorModel();
  }

  /**
   * Updates all document styles with the attributes contained in newSet.
   * @param newSet Style containing new attributes to use.
   */
  protected void _updateStyles(AttributeSet newSet) {
    super._updateStyles(newSet);
    OUT_OF_SYNC_ATTRIBUTES.addAttributes(newSet);
    StyleConstants.setBold(OUT_OF_SYNC_ATTRIBUTES, true);  // should always be bold
    TEST_PASS_ATTRIBUTES.addAttributes(newSet);
    TEST_FAIL_ATTRIBUTES.addAttributes(newSet);
  }

  /** Called when work begins. */
  public void setJUnitInProgress(List<OpenDefinitionsDocument> odds) {
    _odds = odds;  // _odds is updated atomically; no interference with _checkSync
    setJUnitInProgress();
  }
  
  /** called when work begins */
  public void setJUnitInProgress() {
    _errorListPane.setJUnitInProgress();
  }

  /**
   * Clean up when the tab is closed.
   */
  protected void _close() {
    super._close();
    getModel().getJUnitModel().resetJUnitErrors();
    reset();
  }

  /**
   * Reset the errors to the current error information.
   */
  public void reset() {
    JUnitErrorModel juem = getModel().getJUnitModel().getJUnitErrorModel();
    boolean testsHaveRun = false;
    if (juem != null) {
      _numErrors = juem.getNumErrors();
      testsHaveRun = juem.haveTestsRun();
    } else {
      _numErrors = 0;
    }
    _errorListPane.updateListPane(testsHaveRun);
  }

  /**
   * Resets the progress bar to start counting the given number of tests.
   */
  public synchronized void progressReset(int numTests) {
    _progressBar.reset();
    _progressBar.start(numTests);
    _testsSuccessful = true;
    _testCount = 0;
  }

  /**
   * Steps the progress bar forward by one test.
   * @param successful Whether the last test was successful or not.
   */
  public synchronized void progressStep(boolean successful) {
    _testCount++;
    _testsSuccessful &= successful;
    _progressBar.step(_testCount, _testsSuccessful);
  }

  public synchronized void testStarted(String className, String testName) {
  }

  private void _displayStackTrace (JUnitError e) {
    _errorLabel.setText((e.isWarning() ? "Error: " : "Failure: ") +
                        e.message());
    _fileLabel.setText("File: "+(new File(e.fileName())).getName());
    if (!e.testName().equals("")) {
      _testLabel.setText("Test: "+e.testName());
    }
    else {
      _testLabel.setText("");
    }
    _stackTextArea.setText(e.stackTrace());
    _stackTextArea.setCaretPosition(0);
    _stackFrame.setVisible(true);
  }

  /**
   * A pane to show JUnit errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  public class JUnitErrorListPane extends ErrorPanel.ErrorListPane {
    private JPopupMenu _popMenu;
    private String _runningTestName;
    private boolean _warnedOutOfSync;
    private static final String JUNIT_WARNING = "junit.framework.TestSuite$1.warning";


    /**
     * Maps any test names in the currently running suite to the position
     * that they appear in the list pane.
     */
    private final HashMap<String, Position> _runningTestNamePositions;

    /**
     * Constructs the JUnitErrorListPane.
     */
    public JUnitErrorListPane() {
      removeMouseListener(defaultMouseListener);
      _popMenu = new JPopupMenu();
      _popMenu.add(_showStackTraceAction);
      _error = null;
      _setupStackTraceFrame();
      addMouseListener(new PopupAdapter());
      _runningTestName = null;
      _runningTestNamePositions = new HashMap<String, Position>();
      _showStackTraceButton.setEnabled(false);
    }

    private String _getTestFromName(String name) {
      int paren = name.indexOf('(');
      if ((paren > -1) && (paren < name.length())) {
        return name.substring(0, paren);
      }
      else {
        throw new IllegalArgumentException("Name does not contain any parens: " + name);
      }
    }

    private String _getClassFromName(String name) {
      int paren = name.indexOf('(');
      if ((paren > -1) && (paren < name.length())) {
        return name.substring(paren + 1, name.length() - 1);
      }
      else {
        throw new IllegalArgumentException("Name does not contain any parens: " + name);
      }
    }

    /**
     * Provides the ability to display the name of the test being run.
     */
    public synchronized void testStarted(String name) {
      String testName = _getTestFromName(name);
      String className = _getClassFromName(name);
      String fullName = className + "." + testName;
      if (fullName.equals(JUNIT_WARNING)) return;
      Document doc = getDocument();
      int index = doc.getLength();

      try {
        // Insert the classname if it has changed
        if (!className.equals(_runningTestName)) {
          _runningTestName = className;
          doc.insertString(index, "  " + className + "\n", NORMAL_ATTRIBUTES);
          index = doc.getLength();
        }

        // Insert the test name, remembering its position
        doc.insertString(index, "    ", NORMAL_ATTRIBUTES);
        index = doc.getLength();
        doc.insertString(index, testName + "\n", NORMAL_ATTRIBUTES);
        Position pos = doc.createPosition(index);
        _runningTestNamePositions.put(fullName, pos);
        setCaretPosition(index);
      }
      catch (BadLocationException ble) {
        // Inserting at end, shouldn't happen
        throw new UnexpectedException(ble);
      }
    }

    /**
     * Provides the ability to display the results of a test that has finished.
     */
    public synchronized void testEnded(String name, boolean wasSuccessful, boolean causedError) {
      String testName = _getTestFromName(name);
      String fullName = _getClassFromName(name) + "." + testName;
      if (fullName.equals(JUNIT_WARNING)) {
        return;
      }
      Document doc = getDocument();
      Position namePos = _runningTestNamePositions.get(fullName);
      AttributeSet set;
      if (!wasSuccessful || causedError) set = TEST_FAIL_ATTRIBUTES;
      else set = TEST_PASS_ATTRIBUTES;
      if (namePos != null) {
        int index = namePos.getOffset();
        int length = testName.length();
        if (doc instanceof StyledDocument) {
          ((StyledDocument)doc).setCharacterAttributes(index, length, set, false);
        }
      }
    }

    /** Puts the error pane into "junit in progress" state. */
    public synchronized void setJUnitInProgress() {
      _errorListPositions = new Position[0];
      progressReset(0);
      _runningTestNamePositions.clear();
      _runningTestName = null;
      _warnedOutOfSync = false;

      DefaultStyledDocument doc = new DefaultStyledDocument();
      _checkSync(doc);

      try { doc.insertString(doc.getLength(), START_JUNIT_MSG, BOLD_ATTRIBUTES); }
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      setDocument(doc);
      selectNothing();
    }

    /** Used to show that testing was unsuccessful. */
    protected synchronized void _updateWithErrors() throws BadLocationException {
      //DefaultStyledDocument doc = new DefaultStyledDocument();
      DefaultStyledDocument doc = (DefaultStyledDocument) getDocument();
      _checkSync(doc);
      _updateWithErrors("test", "failed", doc);
    }

    protected void _updateWithErrors(String failureName, String failureMeaning,
                                     DefaultStyledDocument doc)
      throws BadLocationException {
      // Print how many errors
      _replaceInProgressText(_getNumErrorsMessage(failureName, failureMeaning));

      _insertErrors(doc);

      // Select the first error
      switchToError(0);
    }

    /**
     * Prints a message for the given error
     * @param error the error to print
     * @param doc the document in the error pane
     *
     * This code inserts the error text underneath the test name (which should be red) in the
     * pane.  However, the highlighter is dependent on the errors being contiguous and at the
     * end of the document, so this is disabled pending an overhaul of the highlight manager.
     *
    protected void _insertErrorText(CompilerError error, Document doc) throws BadLocationException {
      // Show file and line number
      JUnitError err = (JUnitError)error;
      String test = err.testName();
      String name = err.className() + "." + test;
      int index;
      Position pos = _runningTestNamePositions.get(name);
      if (pos != null) {
        index = pos.getOffset() + test.length() + 1;
      }
      else {
        index = doc.getLength();
      }
      String toInsert = "File: ";
      doc.insertString(index, toInsert, BOLD_ATTRIBUTES);
      index += toInsert.length();
      toInsert = error.getFileMessage() + "  [line: " + error.getLineMessage() + "]\n";
      doc.insertString(index, toInsert, NORMAL_ATTRIBUTES);
      index += toInsert.length();

      if (error.isWarning()) {
        toInsert = _getWarningText();
      }
      else {
        toInsert = _getErrorText();
      }
      doc.insertString(index, toInsert, BOLD_ATTRIBUTES);
      index += toInsert.length();

      toInsert = error.message() + "\n";
      doc.insertString(index, toInsert, NORMAL_ATTRIBUTES);
    }*/

    /**
     * Replaces the "Testing in progress..." text with the given message.
     * @param msg the text to insert
     */
    private void _replaceInProgressText(String msg) throws BadLocationException {
      int start = 0;
      if (_warnedOutOfSync) { start = TEST_OUT_OF_SYNC.length(); }
      int len = START_JUNIT_MSG.length();
      Document doc = getDocument();
      if (doc.getLength() >= len + start) {
        doc.remove(start, len);
        doc.insertString(start, msg, BOLD_ATTRIBUTES);
      }
    }

    /**
     * Returns the string to identify a warning.
     * In JUnit, warnings (the odd case) indicate errors/exceptions.
     */
    protected String _getWarningText() {  return "Error: "; }

    /**
     * Returns the string to identify an error.
     * In JUnit, errors (the normal case) indicate TestFailures.
     */
    protected String _getErrorText() { return "Failure: "; }

    /** Updates the list pane with no errors. */
    protected synchronized void _updateNoErrors(boolean haveTestsRun) throws BadLocationException {
      //DefaultStyledDocument doc = new DefaultStyledDocument();
      _checkSync(getDocument());
      _replaceInProgressText(haveTestsRun ? JUNIT_FINISHED_MSG : NO_TESTS_MSG);

      selectNothing();
      setCaretPosition(0);
    }

    /**
     * Checks the document being tested to see if it's in sync. If not,
     * displays a message in the document in the test output pane.
     */
    private void _checkSync(Document doc) {
      if (_warnedOutOfSync) return;
      List<OpenDefinitionsDocument> odds = _odds;  // grab current _odds
      for (OpenDefinitionsDocument odoc: odds) {
        if (! odoc.checkIfClassFileInSync()) {
          try {
            doc.insertString(0, TEST_OUT_OF_SYNC, OUT_OF_SYNC_ATTRIBUTES);
            _warnedOutOfSync = true;
            return;
          }
          catch (BadLocationException ble) {
            throw new UnexpectedException(ble);
          }
        }
      }
    }

    private void _setupStackTraceFrame() {
      //DrJava.consoleOut().println("Stack Trace for Error: \n"+ e.stackTrace());
      JDialog _dialog = new JDialog(_frame,"JUnit Error Stack Trace",false);
      _stackFrame = _dialog;
      _stackTextArea = new JTextArea();
      _stackTextArea.setEditable(false);
      _stackTextArea.setLineWrap(false);
      JScrollPane scroll = new
        BorderlessScrollPane(_stackTextArea,
                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      ActionListener closeListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          _stackFrame.setVisible(false);
        }
      };
      JButton closeButton = new JButton("Close");
      closeButton.addActionListener(closeListener);
      JPanel closePanel = new JPanel(new BorderLayout());
      closePanel.setBorder(new EmptyBorder(5,5,0,0));
      closePanel.add(closeButton, BorderLayout.EAST);
      JPanel cp = new JPanel(new BorderLayout());
      _dialog.setContentPane(cp);
      cp.setBorder(new EmptyBorder(5,5,5,5));
      cp.add(scroll, BorderLayout.CENTER);
      cp.add(closePanel, BorderLayout.SOUTH);
      JPanel topPanel = new JPanel(new GridLayout(0,1,0,5));
      topPanel.setBorder(new EmptyBorder(0,0,5,0));
      topPanel.add(_fileLabel);
      topPanel.add(_testLabel);
      topPanel.add(_errorLabel);
      cp.add(topPanel, BorderLayout.NORTH);
      _dialog.setSize(600, 500);
      // initial location is relative to parent (MainFrame)
      _dialog.setLocationRelativeTo(_frame);
    }

    /**
     * Overrides selectItem in ErrorListPane to update the current _error selected
     * and enabling the _showStackTraceButton.
     */
    public void selectItem(CompilerError error) {
      super.selectItem(error);
      _error = (JUnitError)error;
      _showStackTraceButton.setEnabled(true);
    }


    /**
     * Overrides _removeListHighlight in ErrorListPane to disable the _showStackTraceButton.
     */
    protected void _removeListHighlight() {
      super._removeListHighlight();
      _showStackTraceButton.setEnabled(false);
    }

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

    private class PopupAdapter extends RightClickMouseAdapter {
      /**
       * Show popup if the click is on an error.
       * @param e the MouseEvent correponding to this click
       */
      public void mousePressed(MouseEvent e) {
        if (_selectError(e)) {
          super.mousePressed(e);
        }
      }

      /**
       * Show popup if the click is on an error.
       * @param e the MouseEvent correponding to this click
       */
      public void mouseReleased(MouseEvent e) {
        if (_selectError(e)) {
          super.mouseReleased(e);
        }
      }

      /**
       * Select the error at the given mouse event.
       * @param e the MouseEvent correponding to this click
       * @return true iff the mouse click is over an error
       */
      private boolean _selectError(MouseEvent e) {
        //TODO: get rid of cast in the next line, if possible
        _error = (JUnitError)_errorAtPoint(e.getPoint());

        if (_isEmptySelection() && _error != null) {
          _errorListPane.switchToError(_error);
          return true;
        }
        else {
          selectNothing();
          return false;
        }
      }

      /**
       * Shows the popup menu for this mouse adapter.
       * @param e the MouseEvent correponding to this click
       */
      protected void _popupAction(MouseEvent e) {
        _popMenu.show(e.getComponent(), e.getX(), e.getY());
      }

//      public JUnitError getError() {
//        return _error;
//      }
    }
  }
  
  
  /**
   * A progress bar showing the status of JUnit tests.
   * Green until a test fails, then red.
   * Adapted from JUnit code.
   */
  static class JUnitProgressBar extends JProgressBar {
    private boolean _hasError = false;

    public JUnitProgressBar() {
      super();
      setForeground(getStatusColor());
    }

    private Color getStatusColor() {
      if (_hasError) {
        return Color.red;
      }
      else {
        return Color.green;
      }
    }

    public void reset() {
      _hasError = false;
      setForeground(getStatusColor());
      setValue(0);
    }

    public void start(int total) {
      setMaximum(total);
      reset();
    }

    public void step(int value, boolean successful) {
      setValue(value);
      if (!_hasError && !successful) {
        _hasError= true;
        setForeground(getStatusColor());
      }
    }
  }
}
