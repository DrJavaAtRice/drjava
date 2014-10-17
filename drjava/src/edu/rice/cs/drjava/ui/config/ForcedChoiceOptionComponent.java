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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.SwingFrame;

import java.awt.event.*;
import java.util.Iterator;

/** This component displays all legal choices for a ForcedChoiceOption as a list
  * of radio buttons.  The radio buttons are placed within a framed panel titled
  * with the OptionComponent's label.
  * @version $Id: ForcedChoiceOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ForcedChoiceOptionComponent extends OptionComponent<String,JComboBox<String>> {
  private volatile JComboBox<String> _comboBox;

  /** Main constructor builds a panel containing a set of radio buttons for the
    * legal values of the ForcedChoiceOption.
    */
  public ForcedChoiceOptionComponent(ForcedChoiceOption option, String labelText, SwingFrame parent) {
    super(option, labelText, parent);

    // Build the combo box from the Iterator of legal values
    Iterator<String> values = option.getLegalValues();
    _comboBox = new JComboBox<String>();
    
    _comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { notifyChangeListeners(); }
    });

    while(values.hasNext()) {
      String currValue = values.next();
      _comboBox.addItem(currValue);
    }

    resetToCurrent(DrJava.getConfig().getSetting(_option));
    setComponent(_comboBox);
  }

  /** Constructor that allows for a tooltip description. */
  public ForcedChoiceOptionComponent(ForcedChoiceOption option, String labelText, SwingFrame parent, String description) {
    this(option, labelText, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
    * @param description the tooltip text
    */
  public void setDescription(String description) {
    _comboBox.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Selects the radio button corresponding to the current config options. */
  public void resetToCurrent(String current) {
//    _comboBox.setEnabled(DrJava.getConfig().isOptionEditable(_option));
    _comboBox.setSelectedItem(current);
    
//    String current = DrJava.getConfig().getSetting(_option);
//    Iterator values = ((ForcedChoiceOption)_option).getLegalValues();
//    int i = 0;
//
//    while(values.hasNext()) {
//      if (current.equals(values.next())) {
//
//        return;
//      }
//      i++;
//    }
  }

  /** Updates the config object with the new setting.  Should run in event thread.
    * @return true if the new value is set successfully
    */
  public boolean updateConfig() {
    String oldValue = DrJava.getConfig().getSetting(_option);
    String newValue = _comboBox.getSelectedItem().toString();  // An ugly workaround; toString() should be unnecessary.

    if (! newValue.equals(oldValue)) { DrJava.getConfig().setSetting(_option, newValue); }

    return true;
  }
  
  /** @return the value currently displayed in the combo box. */
  public String getCurrentComboBoxValue() {
    return _comboBox.getSelectedItem().toString();  // An ugly workaround; toString() should be unnecessary.
  }

  /** Displays the given value. */
  public void setValue(String value) { resetToCurrent(value); }
}