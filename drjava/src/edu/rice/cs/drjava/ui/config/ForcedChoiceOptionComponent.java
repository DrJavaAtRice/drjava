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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import java.awt.*;
import java.awt.event.*;
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
    Iterator values = ((ForcedChoiceOption)_option).getLegalValues();
    int numValues = ((ForcedChoiceOption)_option).getNumValues();
    _comboBox = new JComboBox();
    int i = 0;
    
    while(values.hasNext()) {
      String currValue = (String)values.next();
      _comboBox.addItem(currValue);
      i++;
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

  public void setDescription(String description) {
    _comboBox.setToolTipText(description);
  }

  /**
   * Selects the radio button corresponding to the current config options.
   */ 
  public void resetToCurrent(String current) {
    _comboBox.setSelectedItem(current);
    //String current = DrJava.getConfig().getSetting(_option);
//    Iterator values = ((ForcedChoiceOption)_option).getLegalValues();
//    int i = 0;
//    
//    while(values.hasNext()) {
//      if(current.equals(values.next())) {     
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