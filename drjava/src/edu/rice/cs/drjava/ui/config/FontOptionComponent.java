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
import edu.rice.cs.util.swing.FontChooser;

import java.awt.*;
import java.awt.event.*;

/**
 * The Graphical form of a FontOption.
 * @version $Id$
 */ 
public class FontOptionComponent extends OptionComponent<Font> {
  
  private JButton _button;
  private JTextField _fontField;
  private JPanel _panel;
  private Font _font;
  
  public FontOptionComponent(FontOption opt, String text, Frame parent) {
    super(opt, text, parent);
    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseFont();
      }
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
  }
  
  /**
   * Constructor that allows for a tooltip description.
   */
  public FontOptionComponent(FontOption opt, String text,
                             Frame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /**
   * Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _panel.setToolTipText(description);
    _fontField.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /**
   * Updates the font field to display the given font.
   */
  private void _updateField(Font f) {
    _fontField.setFont(f);
    _fontField.setText(_option.format(f));
  }
    
  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() {
    return _panel;
  }
  
  /**
   * Shows a custom font chooser dialog to pick a new font.
   */
  public void chooseFont() {
    Font f = FontChooser.showDialog(_parent, 
                                    "Choose '" + getLabelText() + "'",
                                    _font);
    if (f != null) {
      _font = f;
      _updateField(_font);
    }
  }
  
  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    if (!_font.equals(DrJava.getConfig().getSetting(_option))) {
      DrJava.getConfig().setSetting(_option, _font);
    }
    return true;
  }
  
   /**
   * Displays the given value.
   */
  public void setValue(Font value) {
    _font = value;
    _updateField(value);
  }
}