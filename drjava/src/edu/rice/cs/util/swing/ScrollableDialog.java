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

package edu.rice.cs.util.swing;

import edu.rice.cs.util.swing.Utilities;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
//import java.io.Serializable;

/** Manages a JDialog with a scrollable text area and a button panel.
  * @version $Id: ScrollableDialog.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ScrollableDialog /* implements Serializable */ {
  /** Default width for all ScrollableDialogs. */
  public static final int DEFAULT_WIDTH = 500;
  /** Default height for all ScrollableDialogs. */
  public static final int DEFAULT_HEIGHT = 400;
  /** JDialog managed by this component. */
  protected JDialog _dialog;
  /** JTextArea contained in a scroll pane in this dialog. */
  protected JTextArea _textArea;
  /** Panel of buttons at the bottom of this dialog. */
  protected JPanel _buttonPanel;
  /** ScrollPane that contains the text area. */
  protected JScrollPane _textScroll;

  /** Creates a new ScrollableDialog with the default width and height.
   * @param parent Parent frame for this dialog
   * @param title Title for this dialog
   * @param header Message to display at the top of this dialog
   * @param text Text to insert into the scrollable JTextArea
   */
  public ScrollableDialog(JFrame parent, String title, String header, String text) {
    this(parent, title, header, text, DEFAULT_WIDTH, DEFAULT_HEIGHT, false);
  }
  
  /** Creates a new ScrollableDialog.
   * @param parent Parent frame for this dialog
   * @param title Title for this dialog
   * @param header Message to display at the top of this dialog
   * @param text Text to insert into the scrollable JTextArea
   * @param width Width for this dialog
   * @param height Height for this dialog
   */
  public ScrollableDialog(JFrame parent, String title, String header, String text, int width, int height) {
    this(parent, title, header, text, width, height, false);
  }
  
  /** Creates a new ScrollableDialog with the default width and height.
   * @param parent Parent frame for this dialog
   * @param title Title for this dialog
   * @param header Message to display at the top of this dialog
   * @param text Text to insert into the scrollable JTextArea
   * @param wrap whether to wrap long lines
   */
  public ScrollableDialog(JFrame parent, String title, String header, String text, boolean wrap) {
    this(parent, title, header, text, DEFAULT_WIDTH, DEFAULT_HEIGHT, wrap);
  }
  
  /** Creates a new ScrollableDialog.
   * @param parent Parent frame for this dialog
   * @param title Title for this dialog
   * @param header Message to display at the top of this dialog
   * @param text Text to insert into the scrollable JTextArea
   * @param width Width for this dialog
   * @param height Height for this dialog
   * @param wrap whether to wrap long lines
   */
  public ScrollableDialog(JFrame parent, String title, String header, String text, int width, int height, boolean wrap) {
    _dialog = new JDialog(parent, title, true);    
    Container content = _dialog.getContentPane();

    content.setLayout(new BorderLayout());

    // Create the text area
    _textArea = new JTextArea();
    _textArea.setEditable(false);
    _textArea.setText(text);
    _textArea.setLineWrap(wrap);
    _textArea.setWrapStyleWord(true);
    
    // Arrange the dialog
    _dialog.setSize(width, height);
    
    // Add components
    _textScroll = new BorderlessScrollPane(_textArea,
                                           JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                           wrap?JScrollPane.HORIZONTAL_SCROLLBAR_NEVER:JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JPanel scrollWrapper = new JPanel(new BorderLayout(0,5));
    scrollWrapper.setBorder(new EmptyBorder(5,5,0,5));
    scrollWrapper.add(new JLabel(header),BorderLayout.NORTH);
    scrollWrapper.add(_textScroll,BorderLayout.CENTER);
    JPanel bottomPanel = new JPanel(new BorderLayout());
    _buttonPanel = new JPanel(new GridLayout(1,0,5,5));
    bottomPanel.add(_buttonPanel,BorderLayout.EAST);
    bottomPanel.setBorder(new EmptyBorder(5,5,5,5));
    _addButtons();
    
    content.add(scrollWrapper, BorderLayout.CENTER);
    content.add(bottomPanel, BorderLayout.SOUTH);
    
    // This method is deprecated.  There are alternatives, but it is
    // probably best to let defer to the standard focus-management
    // policy rather than trying to customize it.
    //_textArea.requestDefaultFocus();
  }

  /** Adds buttons to this dialog's button panel.
    * Subclasses can override this to add different buttons.
    */
  protected void _addButtons() {
    _buttonPanel.add(new JButton(_okAction));
  }

  /** A default "OK" action which disposes this dialog when invoked. */
  private Action _okAction = new AbstractAction("OK") {
    public void actionPerformed(ActionEvent e) {
      _dialog.dispose();
    }
  };

  /** Sets the font for the text area in this dialog.
    * @param f New font for the text
    */
  public void setTextFont(Font f) {
    _textArea.setFont(f);
  }
  
  /** Shows this dialog. */
  public void show() {
    Utilities.setPopupLoc(_dialog, _dialog.getOwner());
    _textArea.setCaretPosition(0);
    _textScroll.getHorizontalScrollBar().setValue(_textScroll.getHorizontalScrollBar().getMinimum());
    _textScroll.getVerticalScrollBar().setValue(_textScroll.getVerticalScrollBar().getMinimum());
    _dialog.setVisible(true);
  }
}
