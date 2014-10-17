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

import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.*;
import edu.rice.cs.util.swing.SwingFrame;
import edu.rice.cs.util.swing.Utilities;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import javax.swing.*;

/**
 * Graphical form of a KeyStrokeOption.
 * @version $Id: KeyStrokeOptionComponent.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class KeyStrokeOptionComponent extends OptionComponent<KeyStroke,JPanel>
                                      implements Comparable<KeyStrokeOptionComponent> {
  private static final int DIALOG_HEIGHT = 185;

  public static final HashMap<KeyStroke, KeyStrokeOptionComponent> _keyToKSOC =
    new HashMap<KeyStroke, KeyStrokeOptionComponent>();
  private JButton _button;
  private JPanel _panel;
  private static GetKeyDialog _getKeyDialog =  null;

  private KeyStroke _key;

  public KeyStrokeOptionComponent(KeyStrokeOption opt, String text, final SwingFrame parent) {
    super(opt, text, parent);

    _key = DrJava.getConfig().getSetting(opt);

    _button = new JButton();
    _button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {

        if (_getKeyDialog == null) { _getKeyDialog = new GetKeyDialog(parent, "Specify Shortcut", true); }

        String oldText = _button.getText();
        _getKeyDialog.promptKey(KeyStrokeOptionComponent.this);
        if (!_button.getText().equals(oldText)) { notifyChangeListeners(); }
      }
    });
    _button.setText(_option.format(_key));

    _panel = new JPanel(new BorderLayout());
    _panel.add(_button, BorderLayout.CENTER);

    GridLayout gl = new GridLayout(1,0);
    gl.setHgap(15);
    _keyToKSOC.put(_key, this);
    setComponent(_panel);
  }

  /** Constructor that allows for a tooltip description. */
  public KeyStrokeOptionComponent(KeyStrokeOption opt, String text,
                                  SwingFrame parent, String description) {
    this(opt, text, parent);
    setDescription(description);
  }

  /** Sets the tooltip description text for this option.
    * @param description the tooltip text
    */
  public void setDescription(String description) {
    _panel.setToolTipText(description);
    _button.setToolTipText(description);
    _label.setToolTipText(description);
  }

  /** Returns a custom string representation of this option component. */
  public String toString() {
    return "<KSOC>label:" + getLabelText() + "ks: " +
      getKeyStroke() + "jb: " + _button.getText() + "</KSOC>\n";
  }

  /** Updates the config object with the new setting.  Should run in event thread.
   * @return true if the new value is set successfully
   */
  public boolean updateConfig() {
    if (!_key.equals(getConfigKeyStroke())) {
      DrJava.getConfig().setSetting(_option, _key);
      setValue(_key);
    }
    return true;
  }

  /** Displays the given value.
   */
  public void setValue(KeyStroke value) {
    _key = value;
    _button.setText(_option.format(value));
  }

  /** Compares two KeyStrokeOptionComponents based on the text of their labels.
   * @return Comparison based on labels, or 1 if o is not a KeyStrokeOptionComponent
   */
  public int compareTo(KeyStrokeOptionComponent other) {
    return this.getLabelText().compareTo(other.getLabelText());
  }

  /** Returns the currently selected KeyStroke.
   */
  public KeyStroke getKeyStroke() {
    return _key;
  }

  /** Returns the KeyStroke current set in the Config settings.
   */
  public KeyStroke getConfigKeyStroke() {
    return DrJava.getConfig().getSetting(_option);
  }

  /** A dialog that allows the user to type in a keystroke to be bound
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

    public GetKeyDialog(SwingFrame f, String title, boolean modal) {
      super(f, title, modal);
//      frame = f;
      // Should all of the following code be run in event thread?
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
      setSize((int)_instructionLabel.getPreferredSize().getWidth() + 30, DIALOG_HEIGHT);
      //centerOnScreen();
      EventQueue.invokeLater(new Runnable() { public void run() { GetKeyDialog.this.pack(); } });
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
      Utilities.setPopupLoc(this, getOwner());
      this.setVisible(true);
    }

    /** A textfield that takes in one keystroke at a time and displays its formatted String version. It updates the
      * label that displays what action the currently displayed keystroke is bound to.
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
