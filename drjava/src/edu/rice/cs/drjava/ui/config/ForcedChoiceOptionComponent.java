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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import java.awt.*;
import java.util.Iterator;

/**
 * This component displays all legal choices for a ForcedChoiceOption as a list
 * of radio buttons.  The radio buttons are placed within a framed panel titled
 * with the OptionComponent's label.
 * @version $Id$
 */
public class ForcedChoiceOptionComponent extends OptionComponent<String> {
  private JComboBox _comboBox;

  /**
   * Main constructor builds a panel containing a set of radio buttons for the
   * legal values of the ForcedChoiceOption.
   */
  public ForcedChoiceOptionComponent(ForcedChoiceOption option, String labelText, Frame parent) {
    super(option, labelText, parent);

    // Build the combo box from the Iterator of legal values
    Iterator<String> values = option.getLegalValues();
    _comboBox = new JComboBox();

    while(values.hasNext()) {
      String currValue = values.next();
      _comboBox.addItem(currValue);
    }

    resetToCurrent(DrJava.getConfig().getSetting(_option));
  }

  /**
   * Constructor that allows for a tooltip description.
   */
  public ForcedChoiceOptionComponent(ForcedChoiceOption option, String labelText,
                                     Frame parent, String description) {
    this(option, labelText, parent);
    setDescription(description);
  }

  /**
   * Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _comboBox.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /**
   * Selects the radio button corresponding to the current config options.
   */
  public void resetToCurrent(String current) {
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

  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() {
    return _comboBox;
  }

  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    String oldValue = DrJava.getConfig().getSetting(_option);
    String newValue = _comboBox.getSelectedItem().toString();

    if (!newValue.equals(oldValue)) {
      DrJava.getConfig().setSetting(_option, newValue);
    }

    return true;
  }

  /**
   * Displays the given value.
   */
  public void setValue(String value) {
    resetToCurrent(value);
  }
}