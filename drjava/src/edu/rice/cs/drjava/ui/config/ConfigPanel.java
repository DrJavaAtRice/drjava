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
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import gj.util.Vector;
import gj.util.Hashtable;

/**
 * The panel on which each set of configuration options (e.g. Fonts, Colors) displays
 *  its configurable items as read from the OptionConstants.
 * @version $Id$
 */
public class ConfigPanel extends JPanel {
  
  protected JLabel _title;
  protected Vector<OptionComponent> _components;
  
  /**
   * Constructor for this ConfigPanel
   * @param title the title for this panel
   */
  public ConfigPanel(String title) {
    _title = new JLabel(title);
    _components = new Vector<OptionComponent>();
    
  }
  
  public String getTitle() {
    return _title.getText();
  }
  
  /**
   * The method for adding new OptionComponents to this ConfigPanel
   * @param oc the OptionComponent to be added
   */ 
  public void addComponent( OptionComponent oc) {
    _components.addElement(oc);
  }
  
  public void displayComponents() {
    this.setLayout(new BorderLayout());
    this.add(_title, BorderLayout.NORTH);

    JPanel panel = new JPanel();
    //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setLayout(new BorderLayout());
    JPanel panel2 = new JPanel();
    //panel2.setLayout(new GridLayout(0, 1));
    panel.add(panel2, BorderLayout.NORTH);
    JScrollPane scroll = new JScrollPane(panel,
                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    GridBagLayout gridbag = new GridBagLayout(); 
    GridBagConstraints c = new GridBagConstraints();
    panel2.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(0, 10, 0, 10);
    Insets compInsets  = new Insets(0, 0, 0, 0);
    for (int i=0; i<_components.size(); i++) {
      OptionComponent comp = _components.elementAt(i);
      
      c.weightx = 0.0;
      c.gridwidth = 1; 
      c.insets = labelInsets;
      
      JLabel label= comp.getLabel();
      gridbag.setConstraints(label, c);
      panel2.add(label);
      
      c.weightx = 1.0;      
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.insets = compInsets;
      
      JComponent otherC = comp.getComponent();
      gridbag.setConstraints(otherC, c);
      panel2.add(otherC);      
    }
    /*
     for (int i=0; i<_components.size(); i++) {
     panel2.add(_components.elementAt(i));
     }*/
    
    this.add(scroll, BorderLayout.CENTER);
  }
  
  /**
   * Tells each component in the vector to update() itself
   * @return whether update() of all the components succeeded 
   */ 
  public boolean update() {
    
    for (int i= 0; i<_components.size();i++) {
      boolean isValidUpdate = _components.elementAt(i).update();
      if (!isValidUpdate) return false;
    }  
    
    return true;
  }
  
  /**
   * Tells each component to reset its field to the stored value.
   */
  public void reset() {
    for (int i= 0; i<_components.size();i++) {
      _components.elementAt(i).reset();
    }
  }
}