/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import koala.dynamicjava.gui.resource.*;
import koala.dynamicjava.gui.resource.ActionMap;

/**
 * This component is used to manipulate a list of strings
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/11/14
 */

public class StringList extends JPanel implements ActionMap {
  
  /** The resource file name */
  protected final static String RESOURCE = "koala.dynamicjava.gui.resources.stringlist";
  
  /** The resource bundle */
  protected static ResourceBundle bundle;
  
  /** The string list */
  protected JList list;
  
  /** The list model */
  protected DefaultListModel listModel = new DefaultListModel();
  
  /** The remove button */
  protected JButton removeButton;
  
  /** The up button */
  protected JButton upButton;
  
  /** The down button */
  protected JButton downButton;
  
  static {
    bundle = ResourceBundle.getBundle(RESOURCE, Locale.getDefault());
  }
  
  /**
   * Creates a new list
   * @param addAction the action associated with the add button
   */
  public StringList(Action addAction) {
    super(new BorderLayout());
    
    listeners.put("AddButtonAction",    addAction);
    listeners.put("RemoveButtonAction", new RemoveButtonAction());
    listeners.put("UpButtonAction",     new UpButtonAction());
    listeners.put("DownButtonAction",   new DownButtonAction());
    
    list = new JList(listModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(new ListSelectionAdapter());
    
    JScrollPane sp = new JScrollPane();
    sp.getViewport().add(list);
    add(sp);
    
    JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER));
    add("South", bp);
    
    ButtonFactory bf = new ButtonFactory(bundle, this);
    
    bp.add(bf.createJButton("AddButton"));
    bp.add(removeButton = bf.createJButton("RemoveButton"));
    bp.add(upButton     = bf.createJButton("UpButton"));
    bp.add(downButton   = bf.createJButton("DownButton"));
    
    removeButton.setEnabled(false);
    upButton.setEnabled(false);
    downButton.setEnabled(false);
  }
  
  /**
   * Returns the strings contained in the list
   */
  public String[] getStrings() {
    Object[] t1 = listModel.toArray();
    String[] t2 = new String[t1.length];
    for (int i = 0; i < t1.length; i++) {
      t2[i] = (String)t1[i];
    }
    return t2;
  }
  
  /**
   * Sets the strings
   */
  public void setStrings(String[] strings) {
    listModel.clear();
    for (int i = 0; i < strings.length; i++) {
      listModel.addElement(strings[i]);
    }
  }
  
  /**
   * Adds a string
   */
  public void add(String s) {
    listModel.addElement(s);
    updateButtons();
  }
  
  /**
   * Updates the state of the buttons
   */
  protected void updateButtons() {
    int size = listModel.size();
    int i    = list.getSelectedIndex();
    
    boolean empty        = size == 0;
    boolean selected     = i != -1;
    boolean zeroSelected = i == 0;
    boolean lastSelected = i == size - 1;
    
    removeButton.setEnabled(!empty && selected);
    upButton.setEnabled(!empty && selected && !zeroSelected);
    downButton.setEnabled(!empty && selected && !lastSelected);
  }
  
  /**
   * The action associated with the 'remove' button
   */
  protected class RemoveButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int i = list.getSelectedIndex();
      listModel.removeElementAt(i);
      updateButtons();
    }
  }
  
  /** The action associated with the 'up' button */
  protected class UpButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int    i = list.getSelectedIndex();
      Object o = listModel.getElementAt(i);
      listModel.removeElementAt(i);
      listModel.insertElementAt(o, i - 1);
      list.setSelectedIndex(i - 1);
    }
  }
  
  /** The action associated with the 'down' button */
  protected class DownButtonAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      int    i = list.getSelectedIndex();
      Object o = listModel.getElementAt(i);
      listModel.removeElementAt(i);
      listModel.insertElementAt(o, i + 1);
      list.setSelectedIndex(i + 1);
    }
  }
  
  /** To manage selection modifications */
  protected class ListSelectionAdapter implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      removeButton.setEnabled(true);
      int i = list.getSelectedIndex();
      upButton.setEnabled(i != 0);
      downButton.setEnabled(i != listModel.size() - 1);
    } 
  }
  
  // ActionMap implementation
  
  /** The map that contains the listeners */
  protected Map<String,Action> listeners = new HashMap<String,Action>();
  
  /**
   * Returns the action associated with the given string
   * or null on error
   * @param key the key mapped with the action to get
   * @throws MissingListenerException if the action is not found
   */
  public Action getAction(String key) throws MissingListenerException {
    return listeners.get(key);
  }
}
