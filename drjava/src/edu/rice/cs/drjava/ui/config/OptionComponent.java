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
import java.awt.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.DrJava;

/**
 * The graphical form of an Option. Provides a way to see the values of Option
 * while running DrJava and perform live updating of Options.
 * @version $Id$
 */
public abstract class OptionComponent<T> {
  protected Option<T> _option;
  protected JLabel _label;
  protected Frame _parent;
    
  public OptionComponent (Option<T> option, String labelText, Frame parent) {
    _option = option;
    _label = new JLabel(labelText);
    _label.setHorizontalAlignment(JLabel.RIGHT);
    _parent = parent;
  }
  
  /**
   * Special constructor for degenerate option components does not take
   * an option.
   * @param labelText Text for descriptive label of this option.
   * @param parent The parent frame.
   */
  public OptionComponent (String labelText, Frame parent) {
    this(null, labelText, parent);
  }
  
  public Option<T> getOption() {
    return _option;
  }
  
  public String getLabelText() {
    return _label.getText();
  } 
  
  public JLabel getLabel() { return _label; } 
  
  public abstract JComponent getComponent();
  
  /**
   * Updates the appropriate configuration option with the new value 
   * if different from the old value and legal. Any changes should be 
   * done immediately such that current and future references to the Option 
   * should reflect the changes.
   * @return false, if value is invalid; otherwise true.
   */ 
  public abstract boolean updateConfig();

  /**
   * Resets the entry field to reflect the actual stored value for the option.
   */
  public void resetToCurrent() {
    if (_option != null) {
      setValue(DrJava.CONFIG.getSetting(_option));
    }
  }
  
  /**
   * Resets the actual value of the component to the original default.
   */
  public void resetToDefault() {
    if (_option != null) {
      setValue(_option.getDefault());
    }
  }
  
  /**
   * Sets the value that is currently displayed by this component.
   */
  public abstract void setValue(T value);
  
  public void showErrorMessage(String title, OptionParseException e) {
    showErrorMessage(title, e.value, e.message);
  }
  
  public void showErrorMessage(String title, String value, String message) {
    JOptionPane.showMessageDialog(_parent,
                                  "There was an error in one of the options that you entered.\n" +
                                  "Option: '" + getLabelText() + "'\n" +
                                  "Your value: '" + value + "'\n" +
                                  "Error: "+ message,
                                  title,
                                  JOptionPane.WARNING_MESSAGE);
  }
  
}
                                      
  
