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

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import java.io.*;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;

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
    show();
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