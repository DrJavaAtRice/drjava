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
 * The Graphical form of a FontOption
 */ 
public class FontOptionComponent extends OptionComponent<FontOption> {
  
  JButton _button;
  Font _currentFont;
  Font _newFont;
  
  public FontOptionComponent(FontOption opt, String text, Frame parent) {
    super(opt, text, parent);
    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseFont();
      }
    });
    _button.setBackground(Color.white);

    _currentFont = DrJava.CONFIG.getSetting(_option);
    _newFont = _currentFont;
    _updateButton(_currentFont);
    
  }
  
  private void _updateButton (Font f) {
    _button.setFont(f);
    _button.setText(_option.format(f));
  }
    
  public JComponent getComponent() {
    return _button;
  }
  
  public void chooseFont() {
    Font f = FontChooser.showDialog(_parent, _newFont);
    
    if (f != null) {
      _newFont = f;
      _updateButton(_newFont);
    }
  }
  
  public boolean update() {
    if (!_newFont.equals(_currentFont)) {
      DrJava.CONFIG.setSetting(_option, _newFont);
      _currentFont = _newFont;
    }
    return true;
  }
  
  public void reset() {
    _currentFont = DrJava.CONFIG.getSetting(_option);
    _newFont = _currentFont;
    _updateButton(_currentFont);
  }
}