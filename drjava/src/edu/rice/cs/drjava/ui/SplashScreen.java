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
import java.awt.*;
import java.awt.event.*;

/**
 * A splash screen window to be displayed as DrJava is first starting up.
 * @version $Id: SplashScreen.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class SplashScreen extends JWindow {
  private static final String SPLASH_ICON = "splash.png";
  private static final int PAUSE_TIME = 4000; // in milliseconds
  
  private ImageIcon _icon;

  /** Creates a new splash screen, but does not display it.  Display the splash screen using show() and close it 
    * with dispose().
    */
  public SplashScreen() {
    _icon = MainFrame.getIcon(SPLASH_ICON);
    getContentPane().add(new JLabel(_icon, SwingConstants.CENTER));
    setSize(_icon.getIconWidth(), _icon.getIconHeight());
    //for multi-monitor support
    //Question: do we want it to popup on the first monitor always?
    GraphicsDevice[] dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    Rectangle rec = dev[0].getDefaultConfiguration().getBounds();
    Point ownerLoc = rec.getLocation();
    Dimension ownerSize = rec.getSize();
    Dimension frameSize = getSize();
    setLocation(ownerLoc.x + (ownerSize.width - frameSize.width) / 2,
                ownerLoc.y + (ownerSize.height - frameSize.height) / 2);
  }
  
  /** Display the splash screen, and schedule it to be removed after a delay.  This does not
    * need to run on the event thread.
    */
  public void flash() {
    setVisible(true);
    repaint();
    Timer cleanup = new Timer(PAUSE_TIME, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    cleanup.setRepeats(false);
    cleanup.start();
  }
  
}
