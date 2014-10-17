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
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;

// TODO: Check synchronization.
import java.util.Vector;

/** The panel that set of configuration options (e.g. Fonts, Colors) uses to display its configurable items as read
 *  from OptionConstants.
 *  @version $Id: ConfigPanel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ConfigPanel extends JPanel {

  protected final String _title;
  protected final Vector<OptionComponent<?,?>> _components;

  /** Constructor for this ConfigPanel
   *  @param title the title for this panel
   */
  public ConfigPanel(String title) {
    //_title = new JLabel(title);
    _title = title;
    _components = new Vector<OptionComponent<?,?>>();
  }

  public String getTitle() { return _title; }

  /** The method for adding new OptionComponents to this ConfigPanel
   *  @param oc the OptionComponent to be added
   */
  public void addComponent(OptionComponent<?,?> oc) { _components.add(oc); }

  public void displayComponents() {
    this.setLayout(new BorderLayout());

    JPanel panel = new JPanel();  // sits in scrollpane and compresses layout
    panel.setLayout(new BorderLayout());
    JPanel panel2 = new JPanel();  // contains OptionComponents
    panel.add(panel2, BorderLayout.NORTH);
    
    JScrollPane scroll = 
      new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), _title));
    
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
    for (int i = 0; i < _components.size(); i++) {
      OptionComponent<?,?> comp = _components.get(i);

      if (!comp.useEntireColumn()) {
        c.anchor = GridBagConstraints.CENTER;
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
      else {
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = compInsets;

        JComponent otherC = comp.getComponent();
        gridbag.setConstraints(otherC, c);
        panel2.add(otherC);
      }
    }

    // Reset Button
    JButton _resetToDefaultButton = new JButton("Reset to Defaults");
    _resetToDefaultButton.addActionListener(new ActionListener() { 
      public void actionPerformed(ActionEvent e) { resetToDefault(); }
    });
    JPanel resetPanel = new JPanel();
    resetPanel.setLayout(new FlowLayout());
    resetPanel.setBorder(new EmptyBorder(5,5,5,5));
    resetPanel.add(_resetToDefaultButton);
    panel.add(resetPanel, BorderLayout.SOUTH);

    this.add(scroll, BorderLayout.CENTER);
  }

  /** Tells each component in the vector to update Config with its value.  Should run in event thread.
   *  @return whether update() of all the components succeeded
   */
  public boolean update() {

    for (int i = 0; i < _components.size(); i++) {
      boolean isValidUpdate = _components.get(i).updateConfig();
      if (! isValidUpdate) return false;
    }
    return true;
  }

  /** Tells each component to reset its display field to the current value. */
  public void resetToCurrent() {
    for (int i = 0; i < _components.size(); i++) {
      _components.get(i).resetToCurrent();
      if (_components.get(i) instanceof VectorOptionComponent<?>)
        ((VectorOptionComponent<?>)_components.get(i)).resizeTable();
    }
  }

  /** Tells each component to reset its value to its default. Each component creates an event thread task. */
  public void resetToDefault() {
    for (int i = 0; i < _components.size(); i++) {
      _components.get(i).resetToDefault();
      if (_components.get(i) instanceof VectorOptionComponent<?>)
        ((VectorOptionComponent<?>)_components.get(i)).resizeTable();
    }
    // must reset the "current keystroke map" when resetting
    VectorKeyStrokeOptionComponent.resetCurrentKeyStrokeMap();
  }
}
