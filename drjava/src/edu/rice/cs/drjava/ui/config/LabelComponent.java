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

import javax.swing.*;

import edu.rice.cs.util.swing.SwingFrame;

/** Displays a label in the form of an option component, to be displayed in a config panel. */
public class LabelComponent extends OptionComponent<Object,JTextArea> {
  private JTextArea _text;

  public LabelComponent(String text, SwingFrame parent, boolean left) {
    super(left?text:"", parent);
    _text = new JTextArea(left?"":text);
    _text.setEditable(false);
    _text.setBackground(parent.getBackground());
//    _text.setBackground(new Color(204,204,204));
  }

  public LabelComponent(String text, SwingFrame parent, String description, boolean left) {
    this(text, parent, left);
    setDescription(description);
  }

  public LabelComponent(String text, SwingFrame parent) { this(text,parent,false); }

  public LabelComponent(String text, SwingFrame parent, String description) {
    this(text, parent, description, false);
  }

  public void setDescription(String description) {
    _text.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.  (Does nothing.)
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() { return true; }

  /** Displays the given value.  (Never changes.) */
  public void setValue(Object value) { }

  /** Return's this OptionComponent's configurable component. */
  public JTextArea getComponent() { return _text; }
}