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
 * The special option component for the toolbar text and toolbar icon options.
 * Not a true OptionComponent, in that it actually represents and governs the 
 * configuration of two BooleanOptions (i.e. those corresponding to TOOLBAR_TEXT_ENABLED 
 * and TOOLBAR_ICONS_ENABLED) bypassing the the normal graphical representation
 * with JRadioButtons, in order to comply with the special circumstances regarding 
 * their setting.
 */
public class ToolbarOptionComponent extends OptionComponent<BooleanOption> {
  
  private JRadioButton _textButton;
  private JRadioButton _iconsButton;
  private JRadioButton _textAndIconsButton;
  private ButtonGroup _group;
  private JPanel _buttonPanel;

  //String constants used to identify which one of the three choices is selected.
  public static final String TEXT_ONLY = "text only";
  public static final String ICONS_ONLY= "icons only";
  public static final String TEXT_AND_ICONS = "text and icons";
  
  /**
   * The constructor does not take an option since we have specific knowledge of the
   * two options we'll need for this component. We simpy access them as needed, and use
   * OptionComponent's degenerate constructor.
   * @param title the title for this panel
   * @param the parent frame
   */
  public ToolbarOptionComponent(String title, Frame parent) {
    super( title, parent);
    
    _textButton = new JRadioButton(TEXT_ONLY);
    _textButton.setActionCommand(TEXT_ONLY);
    
    _iconsButton = new JRadioButton(ICONS_ONLY);
    _iconsButton.setActionCommand(ICONS_ONLY);
    
    _textAndIconsButton = new JRadioButton(TEXT_AND_ICONS);
    _textAndIconsButton.setActionCommand(TEXT_AND_ICONS);

    _setSelected();
    
    _group = new ButtonGroup();
    _group.add(_textButton);
    _group.add(_iconsButton);
    _group.add(_textAndIconsButton);
   
    _buttonPanel = new JPanel();
    _buttonPanel.setLayout(new GridLayout(0,1));
    _buttonPanel.add(_textButton);
    _buttonPanel.add(_iconsButton);
    _buttonPanel.add(_textAndIconsButton);
  }
  
  /**
   * Selects the radio button that corresponds to the previously chosen Toolbar text/icon preference
   */ 
  private void _setSelected() {
    boolean textWasEnabled = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    boolean iconsWereEnabled = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();
    
    if (textWasEnabled && iconsWereEnabled) {
      _textAndIconsButton.setSelected(true);
    }
    else {
      if (textWasEnabled) _textButton.setSelected(true);
      else if (iconsWereEnabled) _iconsButton.setSelected(true);
    }
  }
  
  public JComponent getComponent() {
    return _buttonPanel;
  }
  
  public boolean update() {
    String btnIdent = _group.getSelection().getActionCommand();
    boolean textWasEnabled = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    boolean iconsWereEnabled = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();
    
    if (btnIdent == TEXT_ONLY) {
      if (!textWasEnabled) DrJava.CONFIG.setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, new Boolean(true));
      if (iconsWereEnabled) DrJava.CONFIG.setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, new Boolean(false));
    }
           
    if (btnIdent == ICONS_ONLY) {
      if (!iconsWereEnabled) DrJava.CONFIG.setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, new Boolean(true));
      if (textWasEnabled) DrJava.CONFIG.setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, new Boolean(false));
    }
       
    if (btnIdent == TEXT_AND_ICONS) {
      if (!textWasEnabled) DrJava.CONFIG.setSetting(OptionConstants.TOOLBAR_TEXT_ENABLED, new Boolean(true));
      if (!iconsWereEnabled) DrJava.CONFIG.setSetting(OptionConstants.TOOLBAR_ICONS_ENABLED, new Boolean(true));
    }
    
    return true;
  }
  
  
  public void reset() {
    _setSelected();
  }
  
}