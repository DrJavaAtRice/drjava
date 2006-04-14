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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.swing.BorderlessScrollPane;

/** Displays a popup window for the first uncaught exception or logged conditions.
 *  @version $Id$
 */
public class DrJavaErrorPopup extends JDialog {
  /** information about the error */
  private JComponent _errorInfo;
  /** contains the stack trace */
  private JCheckBox _keepDisplaying;
  /** compresses the buttonPanel into the east */
  private JPanel _bottomPanel;
  /** contains the butons */
  private JPanel _buttonPanel;
  /** the button that closes this window */
  private JButton _closeButton;
  /** the button that shows the error window */
  private JButton _moreButton;
  /** the error */
  private Throwable _error;
  /** the parent frame */
  private static JFrame _parentFrame = new JFrame();
  
  /** Creates a window to graphically display the current error that has occurred in the code of DrJava. */
  public DrJavaErrorPopup(JFrame parent, Throwable error) {
    super(parent, "DrJava Error");
    
    _parentFrame = parent;
    _error = error;

    this.setSize(500,150);
    setLocationRelativeTo(_parentFrame);

    // If we set this pane to be of type text/rtf, it wraps based on words
    // as opposed to based on characters.
    _keepDisplaying = new JCheckBox("Keep showing this notification",
                                    DrJava.getConfig().getSetting(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED).booleanValue());
    _keepDisplaying.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        DrJava.getConfig().setSetting(OptionConstants.DIALOG_DRJAVA_ERROR_POPUP_ENABLED, _keepDisplaying.isSelected());
      }
    });

    _moreButton = new JButton(_moreAction);
    _closeButton = new JButton(_closeAction);

    _bottomPanel = new JPanel(new BorderLayout());
    _buttonPanel = new JPanel();
    _buttonPanel.add(_moreButton);
    _buttonPanel.add(_closeButton);
    _bottomPanel.add(_keepDisplaying, BorderLayout.WEST);
    _bottomPanel.add(_buttonPanel, BorderLayout.EAST);

    msg[1] = _error.toString();
    _errorInfo = new JOptionPane(msg,JOptionPane.ERROR_MESSAGE,
                                 JOptionPane.DEFAULT_OPTION,null,
                                 new Object[0]);      

    JPanel cp = new JPanel(new BorderLayout(5,5));
    cp.setBorder(new EmptyBorder(5,5,5,5));
    setContentPane(cp);
    cp.add(_errorInfo, BorderLayout.CENTER);
    cp.add(_bottomPanel, BorderLayout.SOUTH);    
    getRootPane().setDefaultButton(_closeButton);
  }
  
  /* Close the window. */
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent e) {
      DrJavaErrorPopup.this.dispose();
    }
  };

  /** Close this window, but display the full DrJava Errors window. */
  private Action _moreAction = new AbstractAction("More Information") {
    public void actionPerformed(ActionEvent e) {
      _closeAction.actionPerformed(e);
      DrJavaErrorWindow.singleton().setVisible(true);
    }
  };

  /**
   * Contains the canned message for the user
   */
  private final String[] msg = {
    "An error occurred in DrJava:",
    "",
    "You may wish to save all your work and restart DrJava."};
}