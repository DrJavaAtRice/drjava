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
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.BorderlessScrollPane;

/**
 * Displayed whenever an uncaught exception is thrown and propagates back to an
 * action. Displays information about the exception, asks the user to submit a
 * bug report, and prints the stack trace.
 * @version $Id$
 */
public class UncaughtExceptionWindow extends JDialog {

  /** information about the exception */
  private JComponent _exceptionInfo;
  /** contains the stack trace */
  private JTextArea _stackTrace;
  /** scroll pane for _stackTrace */
  private JScrollPane _stackTraceScroll;
  /** compresses the buttonPanel into the east */
  private JPanel _okPanel;
  /** contains the copy and ok butons */
  private JPanel _buttonPanel;
  /** the button that copies the stack trace to the clipboard */
  private JButton _copyButton;
  /** the button that closes this window */
  private JButton _okButton;
  /** the exception that was passed to this window */
  private Throwable _exception;

  /**
   * Creates a window to graphically display an exception which
   * has occurred in the code of DrJava.
   */
  public UncaughtExceptionWindow(JFrame frame, Throwable exception) {
    super(frame,"Unexpected Error");
    _exception = exception;

    this.setSize(600,400);
    setLocationRelativeTo(frame);

    String trace = StringOps.getStackTrace(_exception);
    if (_exception instanceof UnexpectedException) {
      Throwable t = ((UnexpectedException)_exception).getContainedThrowable();
      trace = trace + "\nCaused by:\n" + StringOps.getStackTrace(t);
    }

    // If we set this pane to be of type text/rtf, it wraps based on words
    // as opposed to based on characters.
    _stackTrace = new JTextArea(trace);
    msg[1] = exception.toString();
    _exceptionInfo = new JOptionPane(msg,JOptionPane.ERROR_MESSAGE,
                                     JOptionPane.DEFAULT_OPTION,null,
                                     new Object[0]);

    _stackTrace.setEditable(false);

    _copyButton = new JButton(_copyAction);
    _okButton = new JButton(_okAction);

    _okPanel = new JPanel(new BorderLayout());
    _buttonPanel = new JPanel();
    _buttonPanel.add(_copyButton);
    _buttonPanel.add(_okButton);
    _okPanel.add(_buttonPanel, BorderLayout.EAST);

    _stackTraceScroll = new
      BorderlessScrollPane(_stackTrace,
                           JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                           JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    JPanel cp = new JPanel(new BorderLayout(5,5));
    cp.setBorder(new EmptyBorder(5,5,5,5));
    setContentPane(cp);
    cp.add(_exceptionInfo, BorderLayout.NORTH);
    cp.add(_stackTraceScroll, BorderLayout.CENTER);
    cp.add(_okPanel, BorderLayout.SOUTH);
    setVisible(true);
  }

  private Action _okAction = new AbstractAction("OK") {
    public void actionPerformed(ActionEvent e) {
      UncaughtExceptionWindow.this.dispose();
    }
  };

  private Action _copyAction = new AbstractAction("Copy Stack Trace") {
    public void actionPerformed(ActionEvent e) {
      _stackTrace.grabFocus();
      _stackTrace.getActionMap().get(DefaultEditorKit.selectAllAction).
        actionPerformed(e);
      _stackTrace.getActionMap().get(DefaultEditorKit.copyAction).
        actionPerformed(e);
    }
  };

  /**
   * Returns the canned message for the user
   */
  private final String[] msg = {
    "A runtime exception occured!",
    "",
    "Please submit a bug report containing the system information in the Help>About ",
    "window and an account of the actions that caused the bug (if known) to",
    "http://sourceforge.net/projects/drjava.",
    "You may wish to save all your work and restart DrJava.",
    "Thanks for your help in making DrJava better!"};

}