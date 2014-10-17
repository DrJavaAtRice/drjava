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
import javax.swing.*;

/**
 * Simple class wrapping JOptionPane to have a checkbox underneath the message.
 * @version $Id: ConfirmCheckBoxDialog.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ConfirmCheckBoxDialog {
  private JDialog _dialog;
  private JOptionPane _optionPane;
  private JCheckBox _checkBox;
  
  /** Instantiates a new confirm dialog with default checkbox text.
    * @param parent the parent frame
    * @param title the title of the dialog
    * @param message the stuff to display in the body of the dialog. For a simple message, it should be a String; it can
    *        also be an Object[] including Strings and Components to display in the body of the dialog.
    */
  public ConfirmCheckBoxDialog(JFrame parent, String title, Object message) {
    this(parent, title, message, "Do not show this message again");
  }
  
  /** Instantiates a new confirm dialog with Yes/No as the options.
    * @param parent the parent frame
    * @param title the title of the dialog
    * @param message the stuff to display in the body of the dialog. For a simple message, it should be a String; it can
    *        also be an Object[] including Strings and Components to display in the body of the dialog.
    * @param checkBoxText the text to display with the checkbox
    */
  public ConfirmCheckBoxDialog(JFrame parent, String title, Object message, String checkBoxText) {
    this(parent, title, message, checkBoxText, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION); }
  
  /** Instantiates a new confirm dialog with Yes/No as the options.
    * @param parent  The parent frame
    * @param title  The title of the dialog
    * @param message  The stuff to display in the body of the dialog. For a simple message, it should be a String; it can
    *                 also be an Object[] including Strings and Components to display in the body of the dialog.
    * @param checkBoxText  The text to display with the checkbox
    * @param messageType  The JOptionPane message type
    * @param optionType  The JOptionPane option type
    */
  public ConfirmCheckBoxDialog(JFrame parent, String title, Object message, String checkBoxText, int messageType, 
                               int optionType) {
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
    * @return the JOptionPane result of showing the dialog.
    */
  public int show() {
    Utilities.setPopupLoc(_dialog, _dialog.getOwner());
    _dialog.setVisible(true);
    
    Object val = _optionPane.getValue();
    if (val == null || !(val instanceof Integer)) {
      return JOptionPane.CLOSED_OPTION;
    }
    return ((Integer)val).intValue();
  }
  
  /** Gets the selected value of the check box.  Should not be called until the dialog is shown.
    * @return the value of the checkbox
    */
  public boolean getCheckBoxValue() { return _checkBox.isSelected(); }
}