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
import javax.swing.event.*;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.SwingFrame;

/** Graphical form of an IntegerOption.
  * @version $Id: IntegerOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class IntegerOptionComponent extends OptionComponent<Integer,JTextField> {
  private volatile JTextField _jtf;
  
  public IntegerOptionComponent (IntegerOption opt, String text, SwingFrame parent) {
    super(opt, text, parent);
    _jtf = new JTextField();
    _jtf.setText(_option.format(DrJava.getConfig().getSetting(_option)));
    _jtf.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { notifyChangeListeners(); }
      public void removeUpdate(DocumentEvent e) { notifyChangeListeners(); }
      public void changedUpdate(DocumentEvent e) { notifyChangeListeners(); }
    });
    setComponent(_jtf);
  }
  
  /** Constructor that allows for a tooltip description. */
  public IntegerOptionComponent (IntegerOption opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
    * @param description the tooltip text
    */
  public void setDescription(String description) {
    _jtf.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.  Should run in event thread.
    * @return true if the new value is set successfully
    */
  public boolean updateConfig() {
  
    Integer currentValue = DrJava.getConfig().getSetting(_option);
    String enteredString = _jtf.getText().trim();
    //If the current value is the same as the enterd value, there is nothing to do.
    if (currentValue.toString().equals(enteredString)) return true;
    
    Integer enteredValue;
    try { enteredValue = _option.parse(enteredString); }
    catch (OptionParseException ope) {
      showErrorMessage("Invalid Integer!", ope);
      return false;
    }
    
    DrJava.getConfig().setSetting(_option, enteredValue);
    return true;
  } 
  
  /** Displays the given value. */
  public void setValue(Integer value) {
    _jtf.setText(_option.format(value));
  }    
}