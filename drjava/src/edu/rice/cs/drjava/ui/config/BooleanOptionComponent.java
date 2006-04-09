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
import java.awt.event.*;


/**
 * Graphical form of a BooleanOption.
 * @version $Id$
 */
public class BooleanOptionComponent extends OptionComponent<Boolean> {
  protected JCheckBox _jcb;

  /**
   * Constructs a new BooleanOptionComponent.
   * @param opt the BooleanOption this component represents
   * @param text the text to display with the option
   * @param parent the parent frame
   */
  public BooleanOptionComponent(BooleanOption opt, String text, Frame parent) {
    super(opt, "", parent);
    _jcb = new JCheckBox();
    _jcb.setText(text);
    _jcb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        notifyChangeListeners();
      }
    });
    
    _jcb.setSelected(DrJava.getConfig().getSetting(_option).booleanValue());
  }

  /** Constructs a new BooleanOptionComponent with a tooltip description.
   *  @param opt the BooleanOption this component represents
   *  @param text the text to display with the option
   *  @param parent the parent frame
   *  @param description text to show in a tooltip over 
   */
  public BooleanOptionComponent(BooleanOption opt, String text,
                                Frame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
   *  @param description the tooltip text
   */
  public void setDescription(String description) {
    _jcb.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.
   *  @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    Boolean oldValue = DrJava.getConfig().getSetting(_option);
    Boolean newValue = Boolean.valueOf(_jcb.isSelected());
    
    if (!oldValue.equals(newValue)) DrJava.getConfig().setSetting(_option, newValue);      

    return true;
  } 
  
  /** Displays the given value. */
  public void setValue(Boolean value) {
    _jcb.setSelected(value.booleanValue());
  }
  
  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() { return _jcb; }
}
