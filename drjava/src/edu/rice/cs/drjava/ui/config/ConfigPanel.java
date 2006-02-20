/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;

// TODO: Check synchronization.
import java.util.Vector;

/**
 * The panel on which each set of configuration options (e.g. Fonts, Colors)
 * displays its configurable items as read from the OptionConstants.
 * @version $Id$
 */
public class ConfigPanel extends JPanel {

  //protected JLabel _title;
  protected String _title;
  protected Vector<OptionComponent> _components;

  /**
   * Constructor for this ConfigPanel
   * @param title the title for this panel
   */
  public ConfigPanel(String title) {
    //_title = new JLabel(title);
    _title = title;
    _components = new Vector<OptionComponent>();

  }

  public String getTitle() {
    //return _title.getText();
    return _title;
  }

  /**
   * The method for adding new OptionComponents to this ConfigPanel
   * @param oc the OptionComponent to be added
   */
  public void addComponent( OptionComponent oc) {
    _components.add(oc);
  }

  public void displayComponents() {
    this.setLayout(new BorderLayout());
    //this.add(_title, BorderLayout.NORTH);

    JPanel panel = new JPanel();  // sits in scrollpane and compresses layout
    panel.setLayout(new BorderLayout());
    JPanel panel2 = new JPanel();  // contains OptionComponents
    panel.add(panel2, BorderLayout.NORTH);
    JScrollPane scroll = new JScrollPane(panel,
                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                               _title));
    // Fix increment on scrollbar
    JScrollBar bar = scroll.getVerticalScrollBar();
    bar.setUnitIncrement(25);
    bar.setBlockIncrement(400);

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel2.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(0, 10, 0, 10);
    Insets compInsets  = new Insets(0, 0, 0, 0);
    for (int i=0; i<_components.size(); i++) {
      OptionComponent comp = _components.get(i);

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
     panel2.add(_components.get(i));
     }*/

    // Reset Button
    JButton _resetToDefaultButton = new JButton("Reset to Defaults");
    _resetToDefaultButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetToDefault();
      }
    });
    JPanel resetPanel = new JPanel();
    resetPanel.setLayout(new FlowLayout());
    resetPanel.setBorder(new EmptyBorder(5,5,5,5));
    resetPanel.add(_resetToDefaultButton);
    panel.add(resetPanel, BorderLayout.SOUTH);

    this.add(scroll, BorderLayout.CENTER);
  }

  /**
   * Tells each component in the vector to update Config with its value
   * @return whether update() of all the components succeeded
   */
  public boolean update() {

    for (int i= 0; i<_components.size();i++) {
      boolean isValidUpdate = _components.get(i).updateConfig();
      if (!isValidUpdate) return false;
    }

    return true;
  }

  /** Tells each component to reset its display field to the current value. */
  public void resetToCurrent() {
    for (int i=0; i < _components.size(); i++) {
      _components.get(i).resetToCurrent();
    }
  }

  /** Tells each component to reset its value to the component's default. */
  public void resetToDefault() {
    for (int i=0; i < _components.size(); i++) {
      _components.get(i).resetToDefault();
    }
  }
}
