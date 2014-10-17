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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.util.swing.Utilities;

/** Displays a popup window for the first uncaught exception or logged conditions.
 *  @version $Id: DrJavaErrorPopup.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class DrJavaErrorPopup extends JDialog {
  /** information about the error */
  private JComponent _errorInfo;
  /** contains the stack trace */
  private JCheckBox _keepDisplaying;
  /** compresses the buttonPanel into the east */
  private JPanel _bottomPanel;
  /** contains the buttons */
  private JPanel _buttonPanel;
  /** the button that closes this window */
  private JButton _okButton;
  /** the button that shows the error window */
  private JButton _moreButton;
  /** the error */
  private Throwable _error;
//  /** the parent frame */
//  private JFrame _parentFrame = new JFrame();
  
  /** Creates a window to graphically display the current error that has occurred in the code of DrJava. */
  public DrJavaErrorPopup(JFrame parent, Throwable error) {
    super(parent, "DrScala Error");
    
//    _parentFrame = parent;
    _error = error;

    this.setSize(500,150);

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
    _okButton = new JButton(_okAction);

    _bottomPanel = new JPanel(new BorderLayout());
    _buttonPanel = new JPanel();
    _buttonPanel.add(_moreButton);
    _buttonPanel.add(_okButton);
    _bottomPanel.add(_keepDisplaying, BorderLayout.WEST);
    _bottomPanel.add(_buttonPanel, BorderLayout.EAST);

    if (_error instanceof DrJavaErrorHandler.LoggedCondition) { msg[1] = "Logged condition: " + _error.getMessage(); }
    else { msg[1] = _error.toString(); }
    _errorInfo = new JOptionPane(msg,JOptionPane.ERROR_MESSAGE,
                                 JOptionPane.DEFAULT_OPTION,null,
                                 new Object[0]);      

    JPanel cp = new JPanel(new BorderLayout(5,5));
    cp.setBorder(new EmptyBorder(5,5,5,5));
    setContentPane(cp);
    cp.add(_errorInfo, BorderLayout.CENTER);
    cp.add(_bottomPanel, BorderLayout.SOUTH);    
    getRootPane().setDefaultButton(_okButton);
  }
  
  /* Close the window. */
  private Action _okAction = new AbstractAction("OK") {
    public void actionPerformed(ActionEvent e) {
      DrJavaErrorPopup.this.dispose();
      if (DrJavaErrorHandler.getButton() == null) { System.exit(1); }
    }
  };

  /** Close this window, but display the full DrJava Errors window. */
  private Action _moreAction = new AbstractAction("More Information") {
    public void actionPerformed(ActionEvent e) {
      if (! Utilities.TEST_MODE) {
        DrJavaErrorPopup.this.dispose();
        Utilities.setPopupLoc(DrJavaErrorWindow.singleton(), DrJavaErrorWindow.getFrame());
        DrJavaErrorWindow.singleton().setVisible(true);
      }
    }
  };

  /** Contains the canned message for the user
   */
  private final String[] msg = {
    "An error occurred in DrJava:",
    "",
    "You may wish to save all your work and restart DrJava."};
}