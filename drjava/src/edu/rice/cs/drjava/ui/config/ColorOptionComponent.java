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
 * Graphical form of a ColorOption
 * @version $Id$
 */
public class ColorOptionComponent extends OptionComponent<ColorOption> {
  private JButton _button;
  private Color _currentColor;
  private Color _newColor;
  
  public ColorOptionComponent (ColorOption opt, String text, Frame parent) {
    super(opt, text, parent);
    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseColor();
      }
    });
    _button.setBackground(Color.white);
    _currentColor = DrJava.CONFIG.getSetting(_option);
    _newColor = _currentColor;
    _updateButton(_currentColor);
  }
  
  public boolean update() {
    if (_newColor != _currentColor) {
      DrJava.CONFIG.setSetting(_option, _newColor);
    }
    return true;
  } 
  
  public void reset() {
    _currentColor = DrJava.CONFIG.getSetting(_option);
    _newColor = _currentColor;
    _updateButton(_currentColor);
  }
  
  private void _updateButton(Color c) {
    _button.setForeground(c);
    _button.setText(getLabelText() + " ("+_option.format(c)+")");
  }
  
  public JComponent getComponent() { return _button; }
  
  public void chooseColor() {

    Color c = JColorChooser.showDialog(
                                       _parent,
                                       "Choose '" + getLabelText() + "'",
                                       _newColor);
    if (c != null) {
      _newColor = c;
      _updateButton(_newColor);
    }
    
  }
    
  /**
  private class ColorDialog extend JDialog {
    
    private JLabel _title;
    private JColorChooser _chooser;
    
    public ColorDialog() {
      super(_parent, "Choose Color", true);
      this.setLayout(new BorderLayout());
      
      _title = new JLabel("Choose Color");
      this.add(_title, BorderLayout.NORTH);
      
      _chooser = new JColorChooser();
      this.add(_chooser, BorderLayout.CENTER);
    }
    
  }
  */
  
}