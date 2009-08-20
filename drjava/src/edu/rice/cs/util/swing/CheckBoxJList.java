package edu.rice.cs.util.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/** A JList with checkboxes for each item. */
public class CheckBoxJList extends JList implements ListSelectionListener {
  static Color listForeground;
  static Color listBackground;
  static Color listSelectionForeground;
  static Color listSelectionBackground;
  
  static {
    UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
    listForeground =  uid.getColor("List.foreground");
    listBackground =  uid.getColor("List.background");
    listSelectionForeground =  uid.getColor("List.selectionForeground");
    listSelectionBackground =  uid.getColor("List.selectionBackground");
  }
  
  HashSet<Integer> selectionCache = new HashSet<Integer>();
  
  protected void init(Vector<?> listData, Vector<?> selData) {
    setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    CheckBoxListCellRenderer r = new CheckBoxListCellRenderer();
    if (selData != null) {
      int i = 0;
      for(Object t: listData) {
        boolean sel = false;
        if (selData.contains(t)) {
          selectionCache.add(i);
          getSelectionModel().addSelectionInterval(i, i);
          sel = true;
        }
        r.getListCellRendererComponent(this,
                                       listData.get(i),
                                       i,
                                       sel,
                                       (i == 0));
        ++i;
      }
    }    
    setCellRenderer(r);
    addListSelectionListener(this);
  }

  public CheckBoxJList(Vector<?> listData) {
    super(listData);
    init(listData, null);
  }

  public CheckBoxJList(Vector<?> listData, Vector<?> selData) {
    super(listData);
    init(listData, selData);
  }
  
  @SuppressWarnings("unchecked")
  public CheckBoxJList(ListModel lm) {
    super(lm);
    Vector listData = new Vector();
    for(int i = 0; i < lm.getSize(); ++i) listData.add(lm.getElementAt(i));
    init(listData, new Vector());
  }
  
  // ListSelectionListener implementation
  public void valueChanged(ListSelectionEvent lse) {
    if (!lse.getValueIsAdjusting()) {
      removeListSelectionListener(this);
      
      // remember everything selected as a result of this action
      HashSet<Integer> newSelections = new HashSet<Integer>();
      int size = getModel().getSize();
      for (int i = 0; i < size; i++) {
        if (getSelectionModel().isSelectedIndex(i)) {
          newSelections.add(i);
        }
      }
      
      // turn on everything that was previously selected
      Iterator<Integer> it = selectionCache.iterator();
      while (it.hasNext()) {
        int index = it.next();
        getSelectionModel().addSelectionInterval(index, index);
      }
      
      // add or remove the delta
      it = newSelections.iterator();
      while (it.hasNext()) {
        Integer nextInt = it.next();
        if (selectionCache.contains(nextInt)) {
          getSelectionModel().removeSelectionInterval(nextInt, nextInt);
        }
        else {
          getSelectionModel().addSelectionInterval(nextInt, nextInt);
        }
      }
      
      // save selections for next time
      selectionCache.clear();
      for (int i = 0; i < size; i++) {
        if (getSelectionModel().isSelectedIndex(i)) {
          selectionCache.add(i);
        }
      }      
      addListSelectionListener(this);
    }
  }
    
  private static class CheckBoxListCellRenderer extends JComponent implements ListCellRenderer {
    DefaultListCellRenderer defaultComp;
    JCheckBox checkbox;
    public CheckBoxListCellRenderer() {
      setLayout(new BorderLayout());
      defaultComp = new DefaultListCellRenderer();
      checkbox = new JCheckBox();
      add(checkbox, BorderLayout.WEST);
      add(defaultComp, BorderLayout.CENTER);
    }
    
    public Component getListCellRendererComponent(JList list,
                                                  Object  value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus){
      defaultComp.getListCellRendererComponent (list, value, index,
                                                isSelected, cellHasFocus);
      checkbox.setSelected(isSelected);
      Component[] comps = getComponents();
      for (int i = 0; i < comps.length; ++i) {
        comps[i].setForeground(listForeground);
        comps[i].setBackground(listBackground);
      }
      return this;
    }
  }
}
