/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.SingleDisplayModel;
import edu.rice.cs.drjava.model.junit.JUnitError;
import edu.rice.cs.drjava.model.junit.JUnitErrorModel;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * The panel which displays all the testing errors.
 * In the future, it may also contain a progress bar.
 *
 * @version $Id$
 */
public class JUnitPanel extends ErrorPanel{

  private static final SimpleAttributeSet OUT_OF_SYNC_ATTRIBUTES = _getOutOfSyncAttributes();
  
  private static final SimpleAttributeSet _getOutOfSyncAttributes() {
    SimpleAttributeSet s = new SimpleAttributeSet();
    s.addAttribute(StyleConstants.Foreground, Color.red.darker());
    s.addAttribute(StyleConstants.Bold, new Boolean(true));
    return s;
  }

  private static final String TEST_OUT_OF_SYNC = "The document being tested has been modified " +
    "and should be recompiled!\n";

  protected JUnitErrorListPane _errorListPane;
  private int _testCount;
  private boolean _testsSuccessful;
  private OpenDefinitionsDocument _odd = null;

  private JUnitProgressBar _progressBar;

  /**
   * Constructor.
   * @param model SingleDisplayModel in which we are running
   * @param frame MainFrame in which we are displayed
   */
  public JUnitPanel(SingleDisplayModel model, MainFrame frame) {
    super(model, frame, "Test Output");
    _testCount = 0;
    _testsSuccessful = true;
    _errorListPane = new JUnitErrorListPane();

    _mainPanel.setLayout(new BorderLayout());

    // We make the vertical scrollbar always there.
    // If we don't, when it pops up it cuts away the right edge of the
    // text. Very bad.
    JScrollPane scroller =
      new BorderlessScrollPane(_errorListPane,
                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


    _mainPanel.add(scroller, BorderLayout.CENTER);
    
    JPanel sidePanel = new JPanel(new BorderLayout());
    sidePanel.setBorder(new EmptyBorder(0,5,0,5)); // 5 pix padding on sides
    JPanel innerPanel = new JPanel(new BorderLayout());  // bar and checkbox
    innerPanel.setBorder(new EmptyBorder(5,0,0,0)); // 5 pix padding on top
    sidePanel.add(new JLabel("Test Progress", SwingConstants.LEFT),
                      BorderLayout.NORTH);
       
    sidePanel.add(innerPanel,BorderLayout.CENTER);
    
    _progressBar = new JUnitProgressBar();
    innerPanel.add(_progressBar, BorderLayout.NORTH);
    innerPanel.add(new JPanel(),BorderLayout.CENTER);
        
    _showHighlightsCheckBox = new JCheckBox( "Highlight source", true);
    _showHighlightsCheckBox.addChangeListener( new ChangeListener() {
      public void stateChanged (ChangeEvent ce) {
        DefinitionsPane lastDefPane = getErrorListPane().getLastDefPane();
        
        if (_showHighlightsCheckBox.isSelected()) {
          //lastDefPane.setCaretPosition( lastDefPane.getCaretPosition());
          _errorListPane.switchToError(getErrorListPane().getSelectedIndex());
          lastDefPane.requestFocus();
          lastDefPane.getCaret().setVisible(true);
        }
        else {
          lastDefPane.removeTestErrorHighlight();
        }
      }
    });
    
    innerPanel.add(_showHighlightsCheckBox, BorderLayout.SOUTH);
    _mainPanel.add(sidePanel, BorderLayout.EAST);

  }
  
  /**
   * Returns the JUnitErrorListPane that this panel manages.
   */
  public JUnitErrorListPane getErrorListPane() {
    return _errorListPane;
  }

  /** Called when compilation begins. */
  public void setJUnitInProgress(OpenDefinitionsDocument odd) {
    _odd = odd;
  }

  protected JUnitErrorModel getErrorModel(){
    return getModel().getJUnitErrorModel();
  }

  /**
   * Clean up when the tab is closed.
   */
  protected void _close() {
    super._close();
    //formerly, this would also reset the JUnitErrorModel, but that doesn't seem to be necessary
    reset();
  }

  /**
   * Reset the errors to the current error information.
   */
  public void reset() {
    JUnitErrorModel juem = _model.getJUnitErrorModel();
    boolean testsHaveRun = false;
    if (juem != null) {
      _numErrors = juem.getErrors().length;
      testsHaveRun = juem.haveTestsRun();
    } else {
      _numErrors = 0;
    }
    _errorListPane.updateListPane(testsHaveRun);
  }

  /**
   * Resets the progress bar to start counting the given number of tests.
   */
  public void progressReset(int numTests) {
    _progressBar.reset();
    _progressBar.start(numTests);
    _testsSuccessful = true;
    _testCount = 0;
  }
  
  /**
   * Steps the progress bar forward by one test.
   * @param successful Whether the last test was successful or not.
   */
  public void progressStep(boolean successful) {
    _testCount++;
    _testsSuccessful &= successful;
    _progressBar.step(_testCount, _testsSuccessful);
  }

  /**
   * A pane to show JUnit errors. It acts a bit like a listbox (clicking
   * selects an item) but items can each wrap, etc.
   */
  public class JUnitErrorListPane extends ErrorPanel.ErrorListPane {
    private JPopupMenu _popMenu;
    private Window _stackFrame = null;
    private JTextArea _stackTextArea;
    private final JLabel _errorLabel = new JLabel(),
    _testLabel = new JLabel(), _fileLabel = new JLabel();
    protected PopupAdapter _popupAdapter = new PopupAdapter();

    /**
     * Constructs the JUnitErrorListPane.
     */
    public JUnitErrorListPane() {
      super();
      this.removeMouseListener(defaultMouseListener);
      this.addMouseListener(_popupAdapter);
    }

    /**
     * Provides the ability to display the name of the test being run.
     * Not currently used, since it appears and disappears to quickly
     * to be useful in the current setup.
     */
    public void testStarted(String name) {
//      Document doc = getDocument();
//      try {
//        doc.insertString(doc.getLength(),
//                         "  " + name,
//                         NORMAL_ATTRIBUTES);
//      }
//      catch (BadLocationException ble) {
//        // Inserting at end, shouldn't happen
//        throw new UnexpectedException(ble);
//      }
    }

    /**
     * Provides the ability to display the results of a test that has finished.
     * Not currently used, since it appears and disappears to quickly
     * to be useful in the current setup.
     */
    public void testEnded(String name, boolean wasSuccessful, boolean causedError) {
//      Document doc = getDocument();
//      String status = "ok";
//      if (!wasSuccessful) {
//        status = (causedError) ? "error" : "failed";
//      }
//      try {
//        doc.insertString(doc.getLength(),
//                         "  [" + status + "]\n",
//                         NORMAL_ATTRIBUTES);
//      }
//      catch (BadLocationException ble) {
//        // Inserting at end, shouldn't happen
//        throw new UnexpectedException(ble);
//      }
    }

    /** Puts the error pane into "compilation in progress" state. */
    public void setJUnitInProgress() {
      _errorListPositions = new Position[0];
      progressReset(0);

      DefaultStyledDocument doc = new DefaultStyledDocument();
      _checkSync(doc);

      try {
        doc.insertString(doc.getLength(),
                         "Testing in progress, please wait...\n",
                         NORMAL_ATTRIBUTES);
      }
      catch (BadLocationException ble) {
        throw new UnexpectedException(ble);
      }
      setDocument(doc);

      selectNothing();
    }

    /**
     * Used to show that the last compile was unsuccessful.
     */
    protected void _updateWithErrors() throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      _checkSync(doc);
      _updateWithErrors("test", "failed", doc);
    }

    /**
     * Used to show that the last compile was successful.
     */
    protected void _updateNoErrors(boolean haveTestsRun) throws BadLocationException {
      DefaultStyledDocument doc = new DefaultStyledDocument();
      _checkSync(doc);
      String msg = (haveTestsRun) ? "All tests completed successfully." : "";

      doc.insertString(doc.getLength(),
                       msg,
                       NORMAL_ATTRIBUTES);
      setDocument(doc);

      selectNothing();
    }

    /**
     * Checks the document being tested to see if it's in sync. If not,
     * displays a message in the document in the test output pane.
     */
    private void _checkSync(Document doc) {
      if (_odd == null) {
        return;
      }
      if (!_odd.checkIfClassFileInSync()) {
        try {
          doc.insertString(doc.getLength(), TEST_OUT_OF_SYNC, OUT_OF_SYNC_ATTRIBUTES);
        }
        catch (BadLocationException ble) {
          throw new UnexpectedException(ble);
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
          _stackFrame.hide();
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
      _stackFrame.show();
    }

    private class PopupAdapter extends MouseAdapter {

      private JUnitError _error = null;

      public PopupAdapter (){
        _popMenu = new JPopupMenu();
        JMenuItem stackTraceItem = new JMenuItem("Show Stack Trace");
        stackTraceItem.addActionListener ( new AbstractAction() {
          public void actionPerformed( ActionEvent ae) {
            JUnitError error = getError();
            if (error != null) {
              if (_stackFrame == null) {
                _setupStackTraceFrame();
              }
              _displayStackTrace(error);
            }
          }
        });
        _popMenu.add(stackTraceItem);
      }

      public void mousePressed(MouseEvent e) {
        selectNothing();

        maybeShowPopup(e);
      }

      public void mouseReleased(MouseEvent e) {
        //TODO: get rid of cast in the next line, if possible
        _error = (JUnitError)_errorAtPoint(e.getPoint());

        if (_isEmptySelection() && _error != null) {
          _errorListPane.switchToError(_error);
        }
        else {
          selectNothing();
        }
        maybeShowPopup(e);
      }

      private void maybeShowPopup(MouseEvent e) {
        //Ask if the popuptrigger was pressed, rather than if the
        //right mouse button was pressed
        if (e.isPopupTrigger()) {
          _popMenu.show(e.getComponent(),
                        e.getX(), e.getY());
        }
      }

      public JUnitError getError() {
        return _error;
      }

    }

  }

}

/**
 * A progress bar showing the status of JUnit tests.
 * Green until a test fails, then red.
 * Adapted from JUnit code.
 */
class JUnitProgressBar extends JProgressBar {
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
