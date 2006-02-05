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

/** Graphical form of a ColorOption.
 *  @version $Id$
 */
public class ColorOptionComponent extends OptionComponent<Color> {
  private JButton _button;
  private JTextField _colorField;
  private JPanel _panel;
  private Color _color;
  private boolean _isBackgroundColor;
  private boolean _isBoldText;
  
  /** Main constructor for ColorOptionComponent.
   *  @param opt The ColorOption to display
   *  @param text The text to display in the label of the component
   *  @param parent The Frame displaying this component
   */
  public ColorOptionComponent (ColorOption opt, String text, Frame parent) {
    this(opt, text, parent, false);
  }
  
  /** An alternate constructor, allowing the caller to specify whether this color is a background color.  If so, 
   *  the button will display the color as its background.
   */
  public ColorOptionComponent(ColorOption opt, String text, Frame parent, boolean isBackgroundColor) {
    this(opt, text, parent, isBackgroundColor, false);
  }
  
  public ColorOptionComponent(ColorOption opt, String text, Frame parent, boolean isBackgroundColor, boolean isBoldText)
  {
    super(opt, text, parent);
    _isBackgroundColor = isBackgroundColor;
    _isBoldText = isBoldText;
    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { chooseColor(); }
    });
    _button.setText("...");
    _button.setMaximumSize(new Dimension(10,10));
    _button.setMinimumSize(new Dimension(10,10));
    
    _colorField = new JTextField();
    _colorField.setEditable(false);
    _colorField.setHorizontalAlignment(JTextField.CENTER);
    _panel = new JPanel(new BorderLayout());
    _panel.add(_colorField, BorderLayout.CENTER);
    _panel.add(_button, BorderLayout.EAST);
    if (_isBackgroundColor) {
//      _colorField.setForeground(Color.black);
      _colorField.setForeground(DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_NORMAL_COLOR));
      DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_NORMAL_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _colorField.setForeground(oe.value);
        }
      });
    }
    else {
//      _colorField.setBackground(Color.white);
      _colorField.setBackground(DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_BACKGROUND_COLOR));
       DrJava.getConfig().addOptionListener(OptionConstants.DEFINITIONS_BACKGROUND_COLOR,
                                           new OptionListener<Color>() {
        public void optionChanged(OptionEvent<Color> oe) {
          _colorField.setBackground(oe.value);
        }
      });
   }
    if (_isBoldText) {
      _colorField.setFont(_colorField.getFont().deriveFont(Font.BOLD));
    }
    _color = DrJava.getConfig().getSetting(_option);
    _updateField(_color);
  }
  
  /** Constructor that allows for a tooltip description. */
  public ColorOptionComponent(ColorOption opt, String text,
                              Frame parent, String description) {
    this(opt, text, parent, description, false);
  }

  /** Constructor that allows for a tooltip description as well as whether or not this is a background color. */
  public ColorOptionComponent(ColorOption opt, String text, Frame parent, String description, boolean isBackgroundColor)
  {
    this(opt, text, parent, isBackgroundColor);
    setDescription(description);
  }

  /** Constructor that allows for a tooltip description as well as whether or not this is a background color.*/
  public ColorOptionComponent(ColorOption opt, String text, Frame parent, String description, boolean isBackgroundColor, 
                              boolean isBoldText) {
    this(opt, text, parent, isBackgroundColor, isBoldText);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
   *  @param description the tooltip text
   */
  public void setDescription(String description) {
    _panel.setToolTipText(description);
    _button.setToolTipText(description);
    _colorField.setToolTipText(description);
    _label.setToolTipText(description);
  }
    
  /** Updates the config object with the new setting.
   *  @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    if (!_color.equals(DrJava.getConfig().getSetting(_option))) {
      DrJava.getConfig().setSetting(_option, _color);
    }

    return true;
  }
  
   
  /** Displays the given value. */
  public void setValue(Color value) {
    _color = value;
    _updateField(value);
  }
  
  /** Updates the component's field to display the given color. */
  private void _updateField(Color c) {
    if (_isBackgroundColor) {
      _colorField.setBackground(c);
    }
    else {
      _colorField.setForeground(c);
    }
    _colorField.setText(getLabelText() + " ("+_option.format(c)+")");
  }
  
  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() { return _panel; }
  
  /** Shows a color chooser dialog for picking a new color. */
  public void chooseColor() {
    Color c = JColorChooser.showDialog(_parent, "Choose '" + getLabelText() + "'", _color);
    if (c != null) {
      _color = c;
      _updateField(_color);
    }    
  }
  
}