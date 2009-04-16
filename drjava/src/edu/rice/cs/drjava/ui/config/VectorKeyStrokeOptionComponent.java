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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.*;

import java.util.*;

import edu.rice.cs.drjava.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.CheckBoxJList;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

/** Graphical form of a VectorOption for the KeyStroke options.
 *  @version $Id$
 */
public class VectorKeyStrokeOptionComponent extends VectorOptionComponent<KeyStroke> implements OptionConstants, Comparable<VectorKeyStrokeOptionComponent> {
  private static final int DIALOG_HEIGHT = 185;
  public static final HashMap<KeyStroke, VectorKeyStrokeOptionComponent> _keyToKSOC =
    new HashMap<KeyStroke, VectorKeyStrokeOptionComponent>();

  protected boolean _moveButtonEnabled = true;
  
  public VectorKeyStrokeOptionComponent (VectorOption<KeyStroke> opt, String text, SwingFrame parent) {
    this(opt, text, parent, false);
  }
  
  /** Constructor that allows for a tooltip description. */
  public VectorKeyStrokeOptionComponent (VectorOption<KeyStroke> opt, String text, SwingFrame parent, String description) {
    this(opt, text, parent, description, false);
  }

  /** Constructor with flag for move buttons. */
  public VectorKeyStrokeOptionComponent (VectorOption<KeyStroke> opt, String text, SwingFrame parent, boolean moveButtonEnabled) {
    super(opt, text, parent, true);  // creates three buttons, no remove
    _moveButtonEnabled = moveButtonEnabled;
    for(KeyStroke k: getKeyStrokes()) _keyToKSOC.put(k, this);
  }
  
  /** Constructor that allows for a tooltip description. */
  public VectorKeyStrokeOptionComponent (VectorOption<KeyStroke> opt, String text, SwingFrame parent, String description,
                                          boolean moveButtonEnabled) {
    this(opt, text, parent, moveButtonEnabled);
    setDescription(description);
  }
  
  /** Returns the table model. Can be overridden by subclasses. */
  protected AbstractTableModel _makeTableModel() {
    return new AbstractTableModel() {
      public int getRowCount() { return _ksData.size()+1; }
      public int getColumnCount() {return 2;}
      public boolean isCellEditable(int row, int col){
        return true;
      }
      public Object getValueAt(int row, int col) {
        switch(col) {
          case 0:
            if (row < _ksData.size()) return KeyStrokeOption.formatKeyStroke((KeyStroke)(_ksData.get(row)[0]));
            else return new JButton("Add");
          case 1: 
            if (row < _ksData.size()) return (JButton)(_ksData.get(row)[1]);
            else return new JLabel();
        }
        throw new IllegalArgumentException("Illegal column");
      }
      public Class getColumnClass(int col) {
        switch(col) {
          case 0: return JLabel.class;
          case 1: return JButton.class;
        }
        throw new IllegalArgumentException("Illegal column");
      }
    };
  }
  
  /** Compare two components to have them sorted alphabetically. */
  public int compareTo(VectorKeyStrokeOptionComponent o) {
    return this.getLabelText().compareTo(o.getLabelText());
  }

  Vector<KeyStroke> getKeyStrokes() {
    Vector<KeyStroke> ret = new Vector<KeyStroke>();
    for (int i = 0; i < _ksData.size(); i++){
      ret.add((KeyStroke)(_ksData.get(i)[0]));
    }
    return ret;
  }
  
  /** Adds buttons to _buttonPanel */
  protected void _addButtons() {
    //_buttonPanel.add(_addButton);
    if (_moveButtonEnabled) {
      _buttonPanel.add(_moveUpButton);
      _buttonPanel.add(_moveDownButton);
    }
  }
  
  /** Shows a dialog for adding a keystroke to the element. */
  public void chooseKeyStroke() {    
    _table.getSelectionModel().clearSelection();
    GetKeyDialog getKeyDialog = new GetKeyDialog(_parent, "Specify Shortcut", true);
    getKeyDialog.promptKey(KeyStrokeOption.NULL_KEYSTROKE);
  }
  
  /** Also have to remove the keystroke from the map, in addition to what the superclass already does. */
  protected void _removeIndex(int i) {
    _keyToKSOC.remove(_ksData.get(i)[0]);
    super._removeIndex(i);
  }
  
  protected Action _getAddAction() {
    return new AbstractAction("Add") {
      public void actionPerformed(ActionEvent ae) {
        chooseKeyStroke();
        _tableModel.fireTableDataChanged();
      }
    };
  }
  
  protected void _doAction(){
    chooseKeyStroke();
    _tableModel.fireTableDataChanged();
  }
  
  /** Accessor to the current contents of the table.
    * @return The contents of the list in this component in the form of a Vector.
    */
  public Vector<KeyStroke> getValue() {
    Vector<KeyStroke> ret = new Vector<KeyStroke>();
    for (int i = 0; i < _ksData.size(); i++){
      ret.add((KeyStroke)(_ksData.get(i)[0]));
    }
    return ret;
  }

  /** Displays the given value. */
  public void setValue(Vector<KeyStroke> value) {
    _ksData = new Vector<Object[]>();
    for (int i = 0; i < value.size(); i++){
      Object[] temp = {value.get(i), newRemoveButton()};
      _ksData.add(temp);
    }
    _tableModel.fireTableDataChanged();
  }

  /** Displays the given value. */
  public void setValue(ArrayList<KeyStroke> value) {
    _ksData = new Vector<Object[]>();
    for (int i = 0; i < value.size(); i++){
      Object[] temp = {value.get(i), newRemoveButton()};
      _ksData.add(temp);
    }
    _tableModel.fireTableDataChanged();
  }
  
  protected void _addValue(KeyStroke value) {
    Object[] temp = {value, newRemoveButton()};
    _ksData.add(temp);
    _tableModel.fireTableRowsInserted(_ksData.size()-1, _ksData.size()-1);
    _table.getSelectionModel().setSelectionInterval(_ksData.size()-1,_ksData.size()-1);
    _tableModel.fireTableDataChanged();
    notifyChangeListeners();
    notifyChangeListeners();
  }
  
  /** A dialog that allows the user to type in a keystroke to be bound
   * to the action that was clicked. If the user types a keystroke that
   * is bound to another action, the dialog will display that information.
   */
  public class GetKeyDialog extends JDialog {
    private InputField _inputField;
    private JButton _cancelButton;
    private JButton _okButton;
    private JLabel _instructionLabel;
    private JLabel _currentLabel;
    private JLabel _actionLabel;
    private JPanel _inputAndClearPanel;
    private JPanel _cancelAndOKPanel;
    private KeyStroke _currentKeyStroke;

    public GetKeyDialog(SwingFrame f, String title, boolean modal) {
      super(f, title, modal);
      // Should all of the following code be run in event thread?
      _inputField = new InputField();
      _cancelButton = new JButton("Cancel");
      _cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          _inputField.requestFocusInWindow();
          GetKeyDialog.this.dispose();
        }
      });
      _okButton = new JButton("OK");
      _okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          if ((_currentKeyStroke!=KeyStrokeOption.NULL_KEYSTROKE) &&
              (!getKeyStrokes().contains(_currentKeyStroke))) {
            VectorKeyStrokeOptionComponent conflict = _keyToKSOC.get(_currentKeyStroke);
            if (conflict != null) {
              // key used by another VectorKeyStrokeOptionComponent already
              // remove key from that component
              Vector<KeyStroke> v = conflict.getKeyStrokes();
              v.removeElement(_currentKeyStroke);
              conflict.setValue(v);
              conflict.resizeTable();
            }
            
            _keyToKSOC.put(_currentKeyStroke, VectorKeyStrokeOptionComponent.this);
            _addValue(_currentKeyStroke);
            resizeTable();
          }
          _inputField.requestFocusInWindow();
          GetKeyDialog.this.dispose();
        }
      });
      _instructionLabel = new JLabel("Type in the keystroke you want to use " +
                                     "and click \"OK\"");
      _currentLabel = new JLabel("Current action bound to the keystroke:");
      _actionLabel = new JLabel("<none>");

      _inputAndClearPanel = new JPanel(new BorderLayout());
      _inputAndClearPanel.add(_inputField, BorderLayout.CENTER);

      _cancelAndOKPanel = new JPanel(new GridLayout(1,0));
      _cancelAndOKPanel.add(_okButton);
      _cancelAndOKPanel.add(_cancelButton);

      JPanel panel = (JPanel)this.getContentPane();

      panel.setLayout(new GridLayout(0, 1));
      panel.add(_instructionLabel);
      panel.add(_inputAndClearPanel);
      panel.add(_currentLabel);
      panel.add(_actionLabel);
      panel.add(_cancelAndOKPanel);
      setSize((int)_instructionLabel.getPreferredSize().getWidth() + 30, DIALOG_HEIGHT);
      EventQueue.invokeLater(new Runnable() { public void run() { GetKeyDialog.this.pack(); } });
    }

    public void promptKey(KeyStroke initial) {
      _instructionLabel.setText("Type in the keystroke you want to use for \"" +
                                getLabelText() +
                                "\" and click \"OK\"");
      _currentKeyStroke = initial;
      _actionLabel.setText(getLabelText());
      _inputField.setText(KeyStrokeOption.formatKeyStroke(_currentKeyStroke));
      this.setSize((int)_instructionLabel.getPreferredSize().getWidth() + 30, DIALOG_HEIGHT);
      Utilities.setPopupLoc(this, getOwner());
      this.setVisible(true);
    }

    /** A textfield that takes in one keystroke at a time and displays its formatted String version. It updates the
      * label that displays what action the currently displayed keystroke is bound to.
      */
    private class InputField extends JTextField {
      public void processKeyEvent(KeyEvent e) {
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
        if (e.getID() == KeyEvent.KEY_PRESSED) {
          this.setText(KeyStrokeOption.formatKeyStroke(ks));
          VectorKeyStrokeOptionComponent configKs = _keyToKSOC.get(ks);
          if (configKs == null)
            _actionLabel.setText("<none>");
          else {
            String name = configKs.getLabelText();//KeyBindingManager.Singleton.getName(configKs.getConfigKeyStroke());
            _actionLabel.setText(name);
          }
          _currentKeyStroke = ks;
        }
      }
    }
  }
}