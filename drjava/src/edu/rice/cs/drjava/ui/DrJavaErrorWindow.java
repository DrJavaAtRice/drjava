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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.BorderlessScrollPane;

/** Displays uncaught exceptions and logged conditions.
 *  This window is not automatically updated when new errors occur. In the case of errors, we want to
 *  minimize the effects on the GUI. If we want to see an updated dialog, we can click on the "DrJava Errors"
 *  button again.
 *  @version $Id$
 */
public class DrJavaErrorWindow extends JDialog {

  /** information about the error */
  private JComponent _errorInfo;
  /** contains the stack trace */
  private JTextArea _stackTrace;
  /** label with index */
  private JLabel _indexLabel;
  /** scroll pane for _stackTrace */
  private JScrollPane _stackTraceScroll;
  /** compresses the buttonPanel into the east */
  private JPanel _bottomPanel;
  /** contains the butons */
  private JPanel _buttonPanel;
  /** the button that copies the stack trace to the clipboard */
  private JButton _copyButton;
  /** the button that closes this window */
  private JButton _closeButton;
  /** the button that moves to the next error */
  private JButton _nextButton;
  /** the button that moves to the previous error */
  private JButton _prevButton;
  /** the button that clears all errors and closes the window */
  private JButton _dismissButton;
  /** the errors that were passed to this window */
  private java.util.List<Throwable> _errors;
  /** the currently selected error */
  private Throwable _error;
  /** the currently selected error index */
  private int _errorIndex;
  /** the parent frame */
  private static JFrame _parentFrame = new JFrame();
  /** true if parent changed since last singleton() call */
  private static boolean _parentChanged = true;
  
  /** Sets the parent frame. */
  public static void setFrame(JFrame f) { _parentFrame = f; _parentChanged = true; }
  
  /** The singleton instance of this dialog. */
  private static DrJavaErrorWindow _singletonInstance;
  
  /** Returns the singleton instance. Recreates it if necessary. */
  public static synchronized DrJavaErrorWindow singleton() {
    if (_parentChanged) {
      _singletonInstance = new DrJavaErrorWindow();
      _parentChanged = false;
    }
    return _singletonInstance;
  }
  
  /** Creates a window to graphically display the errors that have occurred in the code of DrJava. */
  private DrJavaErrorWindow() {
    super(_parentFrame, "DrJava Errors");

    this.setSize(600,400);
    setLocationRelativeTo(_parentFrame);

    // If we set this pane to be of type text/rtf, it wraps based on words
    // as opposed to based on characters.
    _stackTrace = new JTextArea();
    _stackTrace.setEditable(false);

    _prevButton = new JButton(_prevAction);
    _nextButton = new JButton(_nextAction);
    _copyButton = new JButton(_copyAction);
    _dismissButton = new JButton(_dismissAction);
    _closeButton = new JButton(_closeAction);

    _bottomPanel = new JPanel(new BorderLayout());
    _buttonPanel = new JPanel();
    _buttonPanel.add(_prevButton);
    _buttonPanel.add(_nextButton);
    _buttonPanel.add(_copyButton);
    _buttonPanel.add(_dismissButton);
    _buttonPanel.add(_closeButton);
    _indexLabel = new JLabel();
    _bottomPanel.add(_indexLabel, BorderLayout.CENTER);
    _bottomPanel.add(_buttonPanel, BorderLayout.EAST);

    _stackTraceScroll = new BorderlessScrollPane(_stackTrace,
                                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    _errorInfo = new JOptionPane("No errors occurred!",JOptionPane.INFORMATION_MESSAGE,
                                 JOptionPane.DEFAULT_OPTION,null,
                                 new Object[0]);

    JPanel cp = new JPanel(new BorderLayout(5,5));
    cp.setBorder(new EmptyBorder(5,5,5,5));
    setContentPane(cp);
    cp.add(_errorInfo, BorderLayout.NORTH);
    cp.add(_stackTraceScroll, BorderLayout.CENTER);
    cp.add(_bottomPanel, BorderLayout.SOUTH);    
    init();
  }
  
  /** Initialize the dialog when setting to visible. */
  public void setVisible(boolean b) {
    if (b) {
      init();
    }
    super.setVisible(b);
  }
  
  /** Initialize the dialog. */
  private void init() {
    _errors = DrJavaErrorHandler.getErrors();
    if (_errors.size()>0) {
      _error = _errors.get(0);
      _errorIndex = 0;
      System.out.println("Errors:");
      for(Throwable t: _errors) System.out.println("\t"+t);
    }
    else {
      _error = null;
      _errorIndex = -1;
    }
    _prevAction.setEnabled(false);
    _nextAction.setEnabled(_errors.size()>1);
    _dismissAction.setEnabled(_errors.size()>0);
    _copyAction.setEnabled(_errors.size()>0);
    updateErrorInfo();
  }

  /** Update the buttons and text area after next or previous. */
  private void updateErrorInfo() {
    getContentPane().remove(_errorInfo);
    if (_error!=null) {
      String trace;
      if (_error instanceof DrJavaErrorHandler.LoggedCondition) {
        StringBuilder b = new StringBuilder();
        b.append("Logged condition: ");
        b.append(_error.getMessage());
        b.append('\n');
        boolean first = true;
        for(StackTraceElement ste: _error.getStackTrace()) {
          if (first) { first = false; continue; /* skip first frame, that's the log method itself */ }
          b.append("\tat ");
          b.append(ste);
          b.append('\n');
        }
        trace = b.toString();
      }
      else {
        trace = StringOps.getStackTrace(_error);
        if (_error instanceof UnexpectedException) {
          Throwable t = ((UnexpectedException)_error).getCause();
          trace = trace + "\nCaused by:\n" + StringOps.getStackTrace(t);
        }
      }
      _stackTrace.setText(trace);
      _stackTrace.setCaretPosition(0);
      msg[0] = String.valueOf(_errors.size())+" error"+((_errors.size()>1)?"s":"")+" occured!";
      _errorInfo = new JOptionPane(msg,JOptionPane.ERROR_MESSAGE,
                                       JOptionPane.DEFAULT_OPTION,null,
                                       new Object[0]);      
      _indexLabel.setText("Error "+(_errorIndex+1)+" of "+(_errors.size()));
    }
    else {
      _errorInfo = new JOptionPane("No errors occurred!",JOptionPane.INFORMATION_MESSAGE,
                                       JOptionPane.DEFAULT_OPTION,null,
                                       new Object[0]);
      _stackTrace.setText("");
      _indexLabel.setText("");
    }
    getContentPane().add(_errorInfo, BorderLayout.NORTH);
    validate();
  }
  
  /* Close the window. */
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) {
      DrJavaErrorWindow.this.dispose();
    }
  };
  
  /* Go to the previous error. */
  private Action _prevAction = new AbstractAction("Previous") {
    public void actionPerformed(ActionEvent e) {
      if (_errorIndex>0) {
        --_errorIndex;
        _error = _errors.get(_errorIndex);
        if (_errorIndex==0) { setEnabled(false); }
        if (_errors.size()>1) { _nextAction.setEnabled(true); }
        updateErrorInfo();
      }
    }
  };
  
  /** Go to the next error. */
  private Action _nextAction = new AbstractAction("Next") {
    public void actionPerformed(ActionEvent e) {
      if (_errorIndex<_errors.size()-1) {
        ++_errorIndex;
        _error = _errors.get(_errorIndex);
        if (_errorIndex==_errors.size()-1) { setEnabled(false); }
        if (_errors.size()>1) { _prevAction.setEnabled(true); }
        updateErrorInfo();
      }
    }
  };
  
  /** Dismiss all errors and close the window. */
  private Action _dismissAction = new AbstractAction("Dismiss") {
    public void actionPerformed(ActionEvent e) {
      DrJavaErrorHandler.clearErrors();
      _errors.clear();
      _error = null;
      _errorIndex = -1;
      setEnabled(false);
      _prevAction.setEnabled(false);
      _nextAction.setEnabled(false);
      _copyAction.setEnabled(false);
      updateErrorInfo();
      JButton errorsButton = DrJavaErrorHandler.getButton();
      if (errorsButton!=null) { errorsButton.setVisible(false); }
      _closeAction.actionPerformed(e);
    }
  };

  /** Copy currently selected error to clip board. */
  private Action _copyAction = new AbstractAction("Copy This Error") {
    public void actionPerformed(ActionEvent e) {
      _stackTrace.grabFocus();
      _stackTrace.getActionMap().get(DefaultEditorKit.selectAllAction).actionPerformed(e);
      _stackTrace.getActionMap().get(DefaultEditorKit.copyAction).actionPerformed(e);
    }
  };

  /**
   * Returns the canned message for the user
   */
  private final String[] msg = {
    "",
    "Please submit a bug report containing the system information in the Help>About ",
    "window and an account of the actions that caused the bug (if known) to",
    "http://sourceforge.net/projects/drjava.",
    "You may wish to save all your work and restart DrJava.",
    "Thanks for your help in making DrJava better!"};

}