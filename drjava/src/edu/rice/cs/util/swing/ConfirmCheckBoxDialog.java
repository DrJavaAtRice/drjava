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

package edu.rice.cs.util.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

  /**
   * initializes the JCheckBox to have the given text.
   */
  private JCheckBox _initCheckBox(String text) {
    _checkBox = new JCheckBox(text);
    return _checkBox;
  }

  /**
   * Shows the dialog.
   * @return the JOptionPane result of showing the dialog.
   */
  public int show() {
    _dialog.show();
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