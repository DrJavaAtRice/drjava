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

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;


/**
 * Graphical form of a VectorOption for the Extra Classpath option.
 * Uses a file chooser for each String element.
 * @version $Id$
 */
public abstract class VectorOptionComponent<T> extends OptionComponent<Vector<T>>
  implements OptionConstants
{
  protected JScrollPane _listScrollPane;
  protected JPanel _panel;
  protected JList _list;
  protected JPanel _buttonPanel;
  protected JButton _addButton;
  protected JButton _removeButton;
  protected DefaultListModel _listModel;
  protected static final int NUM_ROWS = 5;
  protected static final int PIXELS_PER_ROW = 18;

  /**
   * Builds a new VectorOptionComponent.
   * @param opt the option
   * @param text the label to display
   * @param parent the parent frame
   */
  public VectorOptionComponent(VectorOption<T> opt, String text, Frame parent) {
    super(opt, text, parent);

    //set up list
    _listModel = new DefaultListModel();
    _list = new JList(_listModel);
    _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    resetToCurrent();

    /*
    Vector v = DrJava.getConfig().getSetting(_option);
    String[] array = new String[v.size()];
    v.copyInto(array);
    //_list.setListData(array);
    for (int i = 0; i < array.length; i++) {
      _listModel.add(array[i]);
    }
    */

    _addButton = new JButton(_getAddAction());
    _removeButton = new JButton(new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          _listModel.remove(index);
          if (index == _listModel.getSize()) { // we removed the last element
            if (index > 0) // and there's more than one element in the list
            _list.setSelectedIndex(index - 1);
          }
          else {
            _list.setSelectedIndex(index);
          }
        }
      }
    });

    _buttonPanel = new JPanel();
    _buttonPanel.add(_addButton);
    _buttonPanel.add(_removeButton);
    //buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    //buttons.add(Box.createGlue());
    //buttons.add(Box.createGlue());
    //buttons.add(Box.createGlue());

    _listScrollPane = new JScrollPane(_list,
                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    _panel = new JPanel(new BorderLayout());
    _panel.add(_listScrollPane, BorderLayout.CENTER);
    _panel.add(_buttonPanel, BorderLayout.SOUTH);

    _listScrollPane.setPreferredSize(new Dimension(0, NUM_ROWS * PIXELS_PER_ROW));
  }

  /**
   * Constructor that allows for a tooltip description.
   */
  public VectorOptionComponent(VectorOption<T> opt, String text,
                               Frame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /**
   * Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _listScrollPane.setToolTipText(description);
    _list.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    Vector<T> current = new Vector<T>();
    for (int i = 0; i < _listModel.getSize(); i++) {
      current.add((T) _listModel.getElementAt(i));  /* javax.swing.DefaultListModel should be generified! */
    }
    DrJava.getConfig().setSetting(_option, current);
    resetToCurrent();

    return true;
  }

  /**
   * Displays the given value.
   */
  public void setValue(Vector<T> value) {
    _listModel.clear();
    for (int i = 0; i < value.size(); i++) {
      _listModel.addElement(value.elementAt(i));
    }
  }

  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() {
    return _panel;
  }

  /**
   * Gets an action that adds a component to the set of options.
   */
  protected abstract Action _getAddAction();
}
