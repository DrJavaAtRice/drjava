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

/**
 * Displayed whenever an uncaught exception is thrown and propagates back to an 
 * action. Displays information about the exception, asks the user to submit a 
 * bug report, and prints the stack trace.
 * @version $Id$
 */

// wanted to make this a JWindow, but it looked funny with no border
public class UncaughtExceptionWindow extends JFrame {
  
  // information about the exception
  private JTextArea _exceptionInfo;
  // contains the stack trace
  private JTextArea _stackTrace;
  // scroll pane for _stackTrace
  private JScrollPane _stackTraceScroll;
  // contains the exception info and the ok panel
  private JPanel _topPanel;
  // contains the ok button in the north 
  private JPanel _okPanel;
  // the button that closes this window
  private JButton _okButton;
  // the exception that was passed to this window
  private Throwable _exception;
  
  /**
   * Creates a window to graphically display an exception which
   * has occurred in the code of DrJava.
   */
  public UncaughtExceptionWindow(Throwable exception) {
    _exception = exception;
    
    this.setSize(600,400);
    this.setLocation(200,200);
    
    Insets ins = new Insets(20,20,20,20);
    
      // If we set this pane to be of type text/rtf, it wraps based on words
      // as opposed to based on characters.
    _stackTrace = new JTextArea(_getStackTraceString());
    _stackTrace.setBackground(new Color(204,204,204));
    _stackTrace.setMargin(ins);
    _exceptionInfo = new JTextArea(_getExceptionString());
    _exceptionInfo.setBackground(new Color(204,204,204));
    _exceptionInfo.setMargin(ins);
    _exceptionInfo.setEditable(false);
    
    _stackTrace.setEditable(false);
    
    _okButton = new JButton(_okAction);
    
    _okPanel = new JPanel(new BorderLayout());
    _okPanel.add(_okButton, BorderLayout.SOUTH);
    _okPanel.setBackground(Color.gray.brighter());
    
    _topPanel = new JPanel(new BorderLayout());
    _topPanel.add(_exceptionInfo, BorderLayout.CENTER);
    _topPanel.add(_okPanel, BorderLayout.EAST);
    
    _stackTraceScroll = new JScrollPane(_stackTrace, 
                                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(_topPanel, BorderLayout.NORTH);
    this.getContentPane().add(_stackTraceScroll, BorderLayout.CENTER);
    this.setTitle("Uncaught Exception");
    
    this.setVisible(true);
    //Toolkit.getDefaultToolkit().beep();
  }
  
  private Action _okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {        
        UncaughtExceptionWindow.this.dispose();
      }
    };
  
  /**
   * Returns the canned message for the user
   */
  private String _getExceptionString() {
    return new String("A runtime exception occured!\n"+
                      _exception+"\n\n"+
                      "Please submit a bug report containing the "+
                      "system information in the "+
                      "Help>About \nwindow and an " +
                      "account of the actions "+ 
                      "that caused the bug (if known) to\n"+
                      "http://sourceforge.net/projects/drjava.\n\n"+
                      "You may wish to save all your work and "+
                      "restart DrJava.\n" +
                      "Thanks for your help in making DrJava "+
                      "better!");
  }
  
  /**
   * Returns the stack trace in String form
   */
  private String _getStackTraceString() {
    StringWriter swFail = new StringWriter();
    PrintWriter pwFail  = new PrintWriter(swFail);
    
    _exception.printStackTrace(pwFail);
    return new String("Stack Trace: \n\n"+
                      swFail.toString());
  }   
}