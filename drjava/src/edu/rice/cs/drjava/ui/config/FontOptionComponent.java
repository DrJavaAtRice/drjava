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
import edu.rice.cs.util.swing.FontChooser;
import edu.rice.cs.util.swing.SwingFrame;

import java.awt.*;
import java.awt.event.*;

/** The Graphical form of a FontOption.
  * @version $Id: FontOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
  */ 
public class FontOptionComponent extends OptionComponent<Font,JPanel> {
  
  private final JButton _button;
  private final JTextField _fontField;
  private final JPanel _panel;
  private volatile Font _font;
  
  public FontOptionComponent(FontOption opt, String text, SwingFrame parent) {
    super(opt, text, parent);
    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { chooseFont(); }
    });
    _button.setText("...");
    _button.setMaximumSize(new Dimension(10,10));
    _button.setMinimumSize(new Dimension(10,10));
    
    _fontField = new JTextField();
    _fontField.setEditable(false);
    _fontField.setBackground(Color.white);
    _fontField.setHorizontalAlignment(JTextField.CENTER);
    _panel = new JPanel(new BorderLayout());
    _panel.add(_fontField, BorderLayout.CENTER);
    _panel.add(_button, BorderLayout.EAST);

    _font = DrJava.getConfig().getSetting(_option);
    _updateField(_font);
    setComponent(_panel);
  }
  
  /** Constructor that allows for a tooltip description. */
  public FontOptionComponent(FontOption opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
    * @param description the tooltip text
    */
  public void setDescription(String description) {
    _panel.setToolTipText(description);
    _fontField.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the font field to display the given font. */
  private void _updateField(Font f) {
    _fontField.setFont(f);
    _fontField.setText(_option.format(f));
  }
  
  /** Shows a custom font chooser dialog to pick a new font. */
  public void chooseFont() {
    String oldText = _fontField.getText();
    Font f = FontChooser.showDialog(_parent, "Choose '" + getLabelText() + "'", _font);
    if (f != null) {
      _font = f;
      _updateField(_font);
      if (!oldText.equals(_fontField.getText())) { notifyChangeListeners(); }
    }
  }
  
  /** Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    if (! _font.equals(DrJava.getConfig().getSetting(_option))) DrJava.getConfig().setSetting(_option, _font);
    return true;
  }
  
   /** Displays the given value. */
  public void setValue(Font value) {
    _font = value;
    _updateField(value);
  }
}