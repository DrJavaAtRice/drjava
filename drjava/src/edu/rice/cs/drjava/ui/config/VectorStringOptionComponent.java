/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.SwingFrame;

/** Graphical form of a VectorOption for the string options. Uses a JOptionPane for each String element.
 */
public class VectorStringOptionComponent extends VectorOptionComponent<String> implements OptionConstants {
  public VectorStringOptionComponent(VectorOption<String> opt, String text, SwingFrame parent) {
    this(opt, text, parent, null);
  }
  
  /** Constructor that allows for a tooltip description. 
   * @param opt the option
   * @param text the label to display
   * @param parent the parent frame
   * @param description tooltip text
   */
  public VectorStringOptionComponent(VectorOption<String> opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent, description, false);
  }

  /** Constructor with flag for move buttons. 
   * @param opt the option
   * @param text the label to display
   * @param parent the parent frame
   * @param description tooltip text
   * @param moveButtonEnabled true if the move buttons should be enabled
   */
  public VectorStringOptionComponent(VectorOption<String> opt, String text, SwingFrame parent,
                                     String description, boolean moveButtonEnabled) {
    super(opt, text, parent, new String[] { }, description, moveButtonEnabled);  // creates all four buttons
  }

  /** Shows a JOptionPane for adding a string to the element. */
  public void chooseString() {
    String s = (String)JOptionPane.showInputDialog(_parent,
                                                   "Enter the value to add:",
                                                   "Add",
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   null,
                                                   null,
                                                   "");    
    //If a string was returned, add it.
    if ((s != null) && (s.length() > 0)) {
      if (verify(s)) {
        _addValue(s);
      }
    }
  }
  
  /** Verify the input before we add it.
    * @param s input to be added
    * @return true if the input should be added. */
  protected boolean verify(String s) {
    return true;
  }
  
  protected Action _getAddAction() {
    return new AbstractAction("Add") {
      public void actionPerformed(ActionEvent ae) {
        chooseString();
      }
    };
  }
}
