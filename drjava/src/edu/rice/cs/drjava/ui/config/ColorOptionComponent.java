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

/**
 * Graphical form of a ColorOption.
 * @version $Id$
 */
public class ColorOptionComponent extends OptionComponent<Color> {
  private JButton _button;
  private JTextField _colorField;
  private JPanel _panel;
  private Color _color;
  private boolean _isBackgroundColor;
  
  /**
   * Main constructor for ColorOptionComponent.
   * @param opt The ColorOption to display
   * @param text The text to display in the label of the component
   * @param parent The Frame displaying this component
   */
  public ColorOptionComponent (ColorOption opt, String text, Frame parent) {
    this(opt, text, parent, false);
  }
  
  /**
   * An alternate constructor, allowing the caller to specify whether
   * this color is a background color.  If so, the button will display
   * the color as its background.
   */
  public ColorOptionComponent(ColorOption opt, String text, Frame parent,
                              boolean isBackgroundColor)
  {
    super(opt, text, parent);
    _isBackgroundColor = isBackgroundColor;
    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseColor();
      }
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
      _colorField.setForeground(Color.black);
    }
    else {
      _colorField.setBackground(Color.white);
      // Would be nice to use background color here (need a listener in configframe?)
      //_colorField.setBackground(DrJava.getConfig().getSetting(OptionConstants.DEFINITIONS_BACKGROUND_COLOR)););
    }
    _color = DrJava.getConfig().getSetting(_option);
    _updateField(_color);
  }
  
  /**
   * Constructor that allows for a tooltip description.
   */
  public ColorOptionComponent (ColorOption opt, String text,
                               Frame parent, String description) {
    this(opt, text, parent, description, false);
  }

  /**
   * Constructor that allows for a tooltip description as well as whether
   * or not this is a background color.
   */
  public ColorOptionComponent(ColorOption opt, String text, Frame parent,
                              String description, boolean isBackgroundColor)
  {
    this(opt, text, parent, isBackgroundColor);
    setDescription(description);
  }

  /**
   * Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _panel.setToolTipText(description);
    _button.setToolTipText(description);
    _colorField.setToolTipText(description);
    _label.setToolTipText(description);
  }
    
  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    if (!_color.equals(DrJava.getConfig().getSetting(_option))) {
      DrJava.getConfig().setSetting(_option, _color);
    }

    return true;
  }
  
   
  /**
   * Displays the given value.
   */
  public void setValue(Color value) {
    _color = value;
    _updateField(value);
  }
  
  /**
   * Updates the component's field to display the given color.
   */
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
  
  /**
   * Shows a color chooser dialog for picking a new color.
   */
  public void chooseColor() {
    Color c = JColorChooser.showDialog(_parent,
                                       "Choose '" + getLabelText() + "'",
                                       _color);
    if (c != null) {
      _color = c;
      _updateField(_color);
    }    
  }
  
}