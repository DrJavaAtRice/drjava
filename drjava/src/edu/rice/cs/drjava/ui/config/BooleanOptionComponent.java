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

import java.awt.event.*;
import javax.swing.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.SwingFrame;

/** Graphical form of a BooleanOption.
  * @version $Id: BooleanOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class BooleanOptionComponent extends OptionComponent<Boolean,JCheckBox> {
  protected JCheckBox _jcb;

  /** Constructs a new BooleanOptionComponent.
   * @param opt the BooleanOption this component represents
   * @param text the text to display with the option
   * @param parent the parent frame
   * @param left whether the descriptive text should be on the left
   */
  public BooleanOptionComponent(BooleanOption opt, String text, SwingFrame parent, boolean left) {
    super(opt, left?text:"", parent);
    _jcb = new JCheckBox();
    _jcb.setText(left?"":text);
    _jcb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { notifyChangeListeners(); }
    });
    
    _jcb.setSelected(DrJava.getConfig().getSetting(_option).booleanValue());
    setComponent(_jcb);
  }

  /** Constructs a new BooleanOptionComponent with a tooltip description.
   *  @param opt the BooleanOption this component represents
   *  @param text the text to display with the option
   *  @param parent the parent frame
   *  @param description text to show in a tooltip over 
   *  @param left whether the descriptive text should be on the left
   */
  public BooleanOptionComponent(BooleanOption opt, String text, SwingFrame parent, String description, boolean left) {
    this(opt, text, parent, left);
    setDescription(description);
  }
  
    /** Constructs a new BooleanOptionComponent.
   * @param opt the BooleanOption this component represents
   * @param text the text to display with the option
   * @param parent the parent frame
   */
  public BooleanOptionComponent(BooleanOption opt, String text, SwingFrame parent) {
    this(opt, text, parent, true);
  }

  /** Constructs a new BooleanOptionComponent with a tooltip description.
   *  @param opt the BooleanOption this component represents
   *  @param text the text to display with the option
   *  @param parent the parent frame
   *  @param description text to show in a tooltip over 
   */
  public BooleanOptionComponent(BooleanOption opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent, true);
  }

  /** Sets the tooltip description text for this option.
    * @param description the tooltip text
    */
  public void setDescription(String description) {
    _jcb.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.
    * @return true if the new value is set successfully
    */
  public boolean updateConfig() {
    Boolean oldValue = DrJava.getConfig().getSetting(_option);
    Boolean newValue = Boolean.valueOf(_jcb.isSelected());
    
    if (! oldValue.equals(newValue)) DrJava.getConfig().setSetting(_option, newValue);      

    return true;
  } 
  
  /** Displays the given value. */
  public void setValue(Boolean value) { _jcb.setSelected(value.booleanValue()); }
  
//  /** Return's this OptionComponent's configurable component. */
//  public JCheckBox getComponent() { return _jcb; }
  
  /** Whether the component should occupy the entire column. */
  public BooleanOptionComponent setEntireColumn(boolean entireColumn) {
    _entireColumn = entireColumn;
    return this;
  }

}
