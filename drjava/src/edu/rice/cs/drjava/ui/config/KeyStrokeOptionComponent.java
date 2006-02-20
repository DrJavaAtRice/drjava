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
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

/**
 * Graphical form of a KeyStrokeOption.
 * @version $Id$
 */
public class KeyStrokeOptionComponent extends OptionComponent<KeyStroke> implements Comparable {
  private static final int DIALOG_HEIGHT = 185;
  /**
   * TODO: should this be synchronized?
   */
  public static final Hashtable<KeyStroke, KeyStrokeOptionComponent> _keyToKSOC =
    new Hashtable<KeyStroke, KeyStrokeOptionComponent>();
  private JButton _button;
  private JTextField _keyField;
  private JPanel _panel;
  private static GetKeyDialog _getKeyDialog =  null;

  private KeyStroke _key;

  public KeyStrokeOptionComponent(KeyStrokeOption opt,
                                  String text,
                                  final Frame parent) {
    super(opt, text, parent);

    _key = DrJava.getConfig().getSetting(opt);

    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {

        if (_getKeyDialog == null) {
          _getKeyDialog =
            new GetKeyDialog(parent,
                             "Specify Shortcut",
                             true);
        }

        String oldText = _keyField.getText();
        _getKeyDialog.promptKey(KeyStrokeOptionComponent.this);
        if (!_keyField.getText().equals(oldText)) {
          notifyChangeListeners();
        }
      }
    });
    _button.setText("...");
    _button.setMaximumSize(new Dimension(10,10));
    _button.setMinimumSize(new Dimension(10,10));

    _keyField = new JTextField();
    _keyField.setEditable(false);
    _keyField.setBackground(Color.white);
    _keyField.setHorizontalAlignment(JTextField.CENTER);
    _keyField.setText(_option.format(_key));
    _panel = new JPanel(new BorderLayout());
    _panel.add(_keyField, BorderLayout.CENTER);
    _panel.add(_button, BorderLayout.EAST);

    GridLayout gl = new GridLayout(1,0);
    gl.setHgap(15);
    _keyToKSOC.put(_key, this);
  }

  /**
   * Constructor that allows for a tooltip description.
   */
  public KeyStrokeOptionComponent(KeyStrokeOption opt, String text,
                                  Frame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /**
   * Sets the tooltip description text for this option.
   * @param description the tooltip text
   */
  public void setDescription(String description) {
    _panel.setToolTipText(description);
    _button.setToolTipText(description);
    _keyField.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /**
   * Returns a custom string representation of this option component.
   */
  public String toString() {
    return "<KSOC>label:" + getLabelText() + "ks: " +
      getKeyStroke() + "jb: " + _button.getText() + "</KSOC>\n";
  }

  /**
   * Updates the config object with the new setting.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    if (!_key.equals(getConfigKeyStroke())) {
      DrJava.getConfig().setSetting(_option, _key);
      setValue(_key);
    }
    return true;
  }

  /**
   * Displays the given value.
   */
  public void setValue(KeyStroke value) {
    _key = value;
    _keyField.setText(_option.format(value));
  }

  /**
   * Compares two KeyStrokeOptionComponents based on the text of their labels.
   * @return Comparison based on labels, or 1 if o is not a KeyStrokeOptionComponent
   */
  public int compareTo(Object o) {
    if (o instanceof KeyStrokeOptionComponent) {
      KeyStrokeOptionComponent other = (KeyStrokeOptionComponent)o;
      return this.getLabelText().compareTo(other.getLabelText());
    }
    else return 1;
  }

  /**
   * Returns the currently selected KeyStroke.
   */
  public KeyStroke getKeyStroke() {
    return _key;
  }

  /**
   * Returns the KeyStroke current set in the Config settings.
   */
  public KeyStroke getConfigKeyStroke() {
    return DrJava.getConfig().getSetting(_option);
  }

  /**
   * Return's this OptionComponent's configurable component.
   */
  public JComponent getComponent() { return _panel; }

  /**
   * A dialog that allows the user to type in a keystroke to be bound
   * to the action that was clicked. If the user types a keystroke that
   * is bound to another action, the dialog will display that information.
   */
  private class GetKeyDialog extends JDialog {
    private InputField _inputField;
    private JButton _clearButton;
    private JButton _cancelButton;
    private JButton _okButton;
    private JLabel _instructionLabel;
    private JLabel _currentLabel;
    private JLabel _actionLabel;
    private JPanel _inputAndClearPanel;
//    private JPanel _labelsPanel;
    private JPanel _cancelAndOKPanel;
    private KeyStroke _currentKeyStroke;
    private KeyStrokeOptionComponent _ksoc;
//    private Frame frame;

    public GetKeyDialog(Frame f, String title, boolean modal) {
      super(f, title, modal);
//      frame = f;

      _inputField = new InputField();
      _clearButton = new JButton("Clear");
      _clearButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          _inputField.setText("");
          _actionLabel.setText("<none>");
          _currentKeyStroke = KeyStrokeOption.NULL_KEYSTROKE;
          _inputField.requestFocusInWindow();
        }
      });
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
          if (!_ksoc.getKeyStroke().equals(_currentKeyStroke)) {
            _keyToKSOC.remove(_ksoc.getKeyStroke());

            KeyStrokeOptionComponent conflict = _keyToKSOC.get(_currentKeyStroke);

            if (conflict != null) {
              _keyToKSOC.remove(_currentKeyStroke);
              conflict.setValue(KeyStrokeOption.NULL_KEYSTROKE);
            }
            _keyToKSOC.put(_currentKeyStroke, _ksoc);
            _ksoc.setValue(_currentKeyStroke);
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
      _inputAndClearPanel.add(_clearButton, BorderLayout.EAST);

      //_labelsPanel = new JPanel();
      //_labelsPanel.add(_currentLabel);
      //_labelsPanel.add(_actionLabel);

      _cancelAndOKPanel = new JPanel(new GridLayout(1,0));
      _cancelAndOKPanel.add(_okButton);
      _cancelAndOKPanel.add(_cancelButton);

      JPanel panel = (JPanel)this.getContentPane();

      panel.setLayout(new GridLayout(0, 1));
      panel.add(_instructionLabel);
      panel.add(_inputAndClearPanel);
      //panel.add(_labelsPanel);
      panel.add(_currentLabel);
      panel.add(_actionLabel);
      panel.add(_cancelAndOKPanel);
      this.setSize((int)_instructionLabel.getPreferredSize().getWidth() + 30, DIALOG_HEIGHT);
      centerOnScreen();
      this.pack();
    }

    public void promptKey(KeyStrokeOptionComponent k) {
      _ksoc = k;
      _instructionLabel.setText("Type in the keystroke you want to use for \"" +
                                k.getLabelText() +
                                "\" and click \"OK\"");
      _currentKeyStroke = k.getKeyStroke();
      _actionLabel.setText(k.getLabelText());
      _inputField.setText(_option.format(_currentKeyStroke));
      //this.setLocation(frame.getLocation());
      this.setSize((int)_instructionLabel.getPreferredSize().getWidth() + 30, DIALOG_HEIGHT);
      centerOnScreen();
      super.setVisible(true);
    }

    private void centerOnScreen() {

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension dialogSize = this.getSize();
      // Set to the new correct size and location
      this.setLocation((screenSize.width - dialogSize.width) / 2,
                       (screenSize.height - dialogSize.height) / 2);
    }

    /**
     * A textfield that takes in one keystroke at a time and displays
     * its formatted String version. It updates the label that displays
     * what action the currently displayed keystroke is bound to.
     */
    private class InputField extends JTextField {
      /*public boolean getFocusTraversalKeysEnabled() {
        return false;
      }*/

      public void processKeyEvent(KeyEvent e) {
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
        if (e.getID() == KeyEvent.KEY_PRESSED) {
          this.setText(_option.format(ks));
          KeyStrokeOptionComponent configKs = _keyToKSOC.get(ks);
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
