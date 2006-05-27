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

package edu.rice.cs.util.swing;

import edu.rice.cs.drjava.ui.MainFrame;
import javax.swing.*;

/**
 * Simple class wrapping JOptionPane to have a checkbox underneath the message.
 * @version $Id$
 */
public class ConfirmCheckBoxDialog {
  private JDialog _dialog;
  private JOptionPane _optionPane;
  private JCheckBox _checkBox;

  /**
   * Instantiates a new confirm dialog with default checkbox text.
   * @param parent the parent frame
   * @param title the title of the dialog
   * @param message the stuff to display in the body of the dialog.
   *                For a simple message, should be a String,
   *                or can be an Object[] including Strings and Components to
   *                display in the body of the dialog.
   */
  public ConfirmCheckBoxDialog(JFrame parent, String title, Object message) {
    this(parent, title, message, "Do not show this message again");
  }

  /**
   * Instantiates a new confirm dialog with Yes/No as the options.
   * @param parent the parent frame
   * @param title the title of the dialog
   * @param checkBoxText the text to display with the checkbox
   * @param message the stuff to display in the body of the dialog.
   *                For a simple message, should be a String,
   *                or can be an Object[] including Strings and Components to
   *                display in the body of the dialog.
   */
  public ConfirmCheckBoxDialog(JFrame parent, String title,
                               Object message, String checkBoxText) {
    this(parent, title, message, checkBoxText, JOptionPane.QUESTION_MESSAGE,
         JOptionPane.YES_NO_OPTION);
  }

  /**
   * Instantiates a new confirm dialog.
   * @param parent the parent frame
   * @param title the title of the dialog
   * @param message the stuff to display in the body of the dialog.
   *                For a simple message, should be a String,
   *                or can be an Object[] including Strings and Components to
   *                display in the body of the dialog.
   * @param checkBoxText the text to display with the checkbox
   * @param messageType the JOptionPane message type
   * @param optionType the JOptionPane option type
   */
  public ConfirmCheckBoxDialog(JFrame parent, String title, Object message,
                               String checkBoxText, int messageType, int optionType) {
    _optionPane = new JOptionPane(message, messageType, optionType);
    JPanel checkBoxPane = new JPanel();
    checkBoxPane.add(_initCheckBox(checkBoxText));
    _optionPane.add(checkBoxPane, 1);
    _dialog = _optionPane.createDialog(parent, title);
  }

  /** Initializes the JCheckBox to have the given text. */
  private JCheckBox _initCheckBox(String text) {
    _checkBox = new JCheckBox(text);
    return _checkBox;
  }

  /** Shows the dialog.
   *  @return the JOptionPane result of showing the dialog.
   */
  public int show() {
    MainFrame.setPopupLoc(_dialog, _dialog.getOwner());
    _dialog.setVisible(true);

    Object val = _optionPane.getValue();
    if (val == null || !(val instanceof Integer)) {
      return JOptionPane.CLOSED_OPTION;
    }
    return ((Integer)val).intValue();
  }

  /**
   * Gets the selected value of the check box.  Should not be called until
   * the dialog is shown.
   * @return the value of the checkbox
   */
  public boolean getCheckBoxValue() {
    return _checkBox.isSelected();
  }
}