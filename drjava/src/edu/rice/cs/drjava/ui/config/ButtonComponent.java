/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.config;

import java.awt.event.*;
import javax.swing.*;

import edu.rice.cs.util.swing.SwingFrame;

/**
 * Button component to perform some action. Nothing gets saved.
 */
public class ButtonComponent extends OptionComponent<Object,JButton> {
  protected JButton _jb;

  /** Constructs a new ButtonComponent.
   * @param l the listener that gets executed when the button is pressed
   * @param text the text to display with the option
   * @param parent the parent frame
   */
  public ButtonComponent(ActionListener l, String text, SwingFrame parent) {
    super(null, "", parent);
    _jb = new JButton(text);
    _jb.addActionListener(l);
    _jb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { notifyChangeListeners(); }
    });
    setComponent(_jb);
  }

  /** Constructs a new ButtonComponent with a tooltip description.
   * @param l the listener that gets executed when the button is pressed
   * @param text the text to display with the option
   * @param parent the parent frame
   * @param description text to show in a tooltip over 
   */
  public ButtonComponent(ActionListener l, String text, SwingFrame parent, String description) {
    this(l, text, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _jb.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() { return true; } 
  
  /** Displays the given value. */
  public void setValue(Object value) { /* nothing to do */ }
}
