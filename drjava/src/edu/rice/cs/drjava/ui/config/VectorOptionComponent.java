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
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.filechooser.FileFilter;

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
      current.add((T)_listModel.getElementAt(i));
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
