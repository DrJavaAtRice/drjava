/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui.config;

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.SwingFrame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Vector;


/** Graphical form of a VectorOption for the Extra Classpath option. Uses a file chooser for each String element.
 *  TODO: define a static make method that adds buttons so that moveUp and moveDown button definitions can be moved
 *  to subclass
 *  @version $Id$
 */
public abstract class VectorOptionComponent<T> extends OptionComponent<Vector<T>> implements OptionConstants {
  protected JScrollPane _listScrollPane;
  protected JPanel _panel;
  protected JList _list;
  protected JPanel _buttonPanel;
  protected JButton _addButton;
  protected JButton _removeButton;
  protected JButton _moveUpButton;   /* Only used in VectorFileOptionComponent subclass. */
  protected JButton _moveDownButton; /* Only used in VectorFileOptionComponent subclass. */
  protected DefaultListModel _listModel;
  protected static final int NUM_ROWS = 5;
  protected static final int PIXELS_PER_ROW = 18;

  /** Builds a new VectorOptionComponent.
    * @param opt the option
    * @param text the label to display
    * @param parent the parent frame
    */
  public VectorOptionComponent(VectorOption<T> opt, String text, SwingFrame parent) {
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
            notifyChangeListeners();
          }
          else {
            _list.setSelectedIndex(index);
            notifyChangeListeners();
          }
        }
      }
    });
    
    /* Only used in VectorFileOptionComponent subclass */
    _moveUpButton = new JButton(new AbstractAction("Move Up") {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          if (index > 0) {
            Object o = _listModel.getElementAt(index);
            _listModel.remove(index);
            _listModel.insertElementAt(o, index - 1);
            _list.setSelectedIndex(index - 1);
            notifyChangeListeners();
          }
        }
      }
    });

    /* Only used in VectorFileOptionComponent subclass */
    _moveDownButton = new JButton(new AbstractAction("Move Down") {
      public void actionPerformed(ActionEvent ae) {
        if (!_list.isSelectionEmpty()) {
          int index = _list.getSelectedIndex();
          if (index < _listModel.getSize() - 1) {
            Object o = _listModel.getElementAt(index);
            _listModel.remove(index);
            _listModel.insertElementAt(o, index + 1);
            _list.setSelectedIndex(index + 1);
            notifyChangeListeners();
          }
        }
      }
    });
    
    
    _buttonPanel = new JPanel();
    _buttonPanel.setBorder(new EmptyBorder(5,5,5,5));
    _buttonPanel.setLayout(new BoxLayout(_buttonPanel, BoxLayout.X_AXIS));
    
    _buttonPanel.add(Box.createHorizontalGlue());
    _addButtons(); // all buttons needs to be added consecutively as a group for glue to work properly               
    _buttonPanel.add(Box.createHorizontalGlue());

    _listScrollPane = new JScrollPane(_list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    _panel = new JPanel(new BorderLayout());
    _panel.add(_listScrollPane, BorderLayout.CENTER);
    _panel.add(_buttonPanel, BorderLayout.SOUTH);

    _listScrollPane.setPreferredSize(new Dimension(0, NUM_ROWS * PIXELS_PER_ROW));
  }

  /** Adds buttons to _buttonPanel */
  protected void _addButtons() {
    _buttonPanel.add(_addButton);
    _buttonPanel.add(_removeButton);
  }
  
  /** Constructor that allows for a tooltip description.
   */
  public VectorOptionComponent(VectorOption<T> opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _listScrollPane.setToolTipText(description);
    _list.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Updates the config object with the new setting.
    * @return true if the new value is set successfully
    */
  public boolean updateConfig() {
    Vector<T> current = getValue();
    DrJava.getConfig().setSetting(_option, current);
    resetToCurrent();
    return true;
  }
  
  /** Accessor to the current contents of the list.
    * @return The contents of the list in this component in the form of a Vector.
    */
  public Vector<T> getValue() {
    Vector<T> current = new Vector<T>();
    for (int i = 0; i < _listModel.getSize(); i++) {
      /* javax.swing.DefaultListModel should be generified! */
      @SuppressWarnings("unchecked") 
      T element = (T) _listModel.getElementAt(i);
      current.add(element);
    }
    return current;
  }

  /** Displays the given value. */
  public void setValue(Vector<T> value) {
    _listModel.clear();
    for (int i = 0; i < value.size(); i++) {
      _listModel.addElement(value.elementAt(i));
    }
  }

  /** Return's this OptionComponent's configurable component. */
  public JComponent getComponent() { return _panel; }

  /** Gets an action that adds a component to the set of options. */
  protected abstract Action _getAddAction();
}
