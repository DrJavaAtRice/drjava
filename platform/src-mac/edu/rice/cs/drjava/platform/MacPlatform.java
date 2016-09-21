/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.platform;

import java.net.URL;
import com.apple.eawt.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;


/**
 * Platform-specific code shared by all Mac OS X platforms.
 */
class MacPlatform extends DefaultPlatform {
  /**
   * Singleton instance.
   */
  public static MacPlatform ONLY = new MacPlatform();
  
  /**
   * Private constructor for singleton pattern.
   */
  protected MacPlatform() {};
 
  public boolean openURL(URL address) {
    // First, try to delegate up.
    if (super.openURL(address)) {
      return true;
    }
    else {
      try {
        // OS X doesn't like how Java formats file URLs:
        //  "file:/Users/dir/file.html" isn't legal.
        // Instead, we need to put another slash in the protocol:
        //  "file:///Users/dir/file.html"
        String addressString = address.toString();
        if (addressString.startsWith("file:/")) {
          String suffix = addressString.substring("file:/".length(), addressString.length());
          addressString = "file:///" + suffix;
        }

        // If there is no command specified, or it won't work, try using "open".
        //Process proc = 
        Runtime.getRuntime().exec(new String[] { "open", addressString });
      }
      catch (Throwable t) {
        // If there was any kind of problem, ignore it and report failure.
        return false;
      }
    }
    
    // Otherwise, trust that it worked.
    return true;
  }
  
  /**
   * Hook for performing general UI setup.  Called before all other UI setup is done.
   * The Mac JDK implementation sets a system property to use the screen menu bar.
   */
  public void beforeUISetup() {
    System.setProperty("apple.laf.useScreenMenuBar","true");
    
    // needs to be done here, otherwise the event gets lost
    ApplicationListener appListener = new ApplicationAdapter() {
      public void handleOpenFile(ApplicationEvent event) {
        if (event.getFilename()!=null) {
          edu.rice.cs.drjava.DrJavaRoot.handleRemoteOpenFile(new java.io.File(event.getFilename()), -1);
          event.setHandled(true);
        }
      }
    };
    
    // Register the ApplicationListener.
    Application appl = new Application();
    appl.addApplicationListener(appListener);
  }
   
  /**
   * Hook for performing general UI setup.  Called after all other UI setup is done.
   * The Mac JDK implementation adds handlers for the application menu items.
   * @param about the Action associated with openning the About dialog
   * @param prefs the Action associated with openning the Preferences dialog
   * @param quit the Action associated with quitting the DrJava application
   */
  public void afterUISetup(final Action about, final Action prefs, final Action quit) {    
    ApplicationListener appListener = new ApplicationAdapter() {
      public void handleAbout(ApplicationEvent e) {
        about.actionPerformed(new ActionEvent(this, 0, "About DrJava"));
        e.setHandled(true);
      }

      public void handlePreferences(ApplicationEvent e) {
        prefs.actionPerformed(new ActionEvent(this, 0, "Preferences..."));
        e.setHandled(true);
      }

      public void handleQuit(ApplicationEvent e) {
        // Workaround for 2868805:  show modal dialogs in a separate thread.
        // This encapsulation is not necessary in 10.2, but will not break either.
        final ApplicationEvent ae = e;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            quit.actionPerformed(new ActionEvent(this, 0, "Quit DrJava"));
            ae.setHandled(true);
          }
        });
      }
    };
    
    // Register the ApplicationListener.
    Application appl = new Application();
    appl.setEnabledPreferencesMenu(true);
    appl.addApplicationListener(appListener);
  }
  
  /**
   * Returns whether this is a Mac platform.
   */
  public boolean isMacPlatform() {
    return true;
  }
  
  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform. On Mac OS, the Alt-? mnemonics are broken, so we do not
    * set them.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.AbstractButton#setMnemonic(int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.AbstractButton obj, int mnemonic) {
    // on Mac OS, the Alt-? mnemonics are broken, so we do not set them
  }

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform. On Mac OS, the Alt-? mnemonics are broken, so we do not
    * set them.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic a char specifying the mnemonic value
    * @see javax.swing.AbstractButton#setMnemonic(char) */
  public void setMnemonic(javax.swing.AbstractButton obj, char mnemonic) {
    // on Mac OS, the Alt-? mnemonics are broken, so we do not set them
  }

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform. On Mac OS, the Alt-? mnemonics are broken, so we do not
    * set them.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.ButtonModel#setMnemonic(int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.ButtonModel obj, int mnemonic) {
    // on Mac OS, the Alt-? mnemonics are broken, so we do not set them
  }

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform. On Mac OS, the Alt-? mnemonics are broken, so we do not
    * set them.
    * @param obj the component whose mnemonic should be set
    * @param tabIndex the index of the tab that the mnemonic refers to
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.JTabbedPane#setMnemonic(int,int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.JTabbedPane obj, int tabIndex, int mnemonic) {
    // on Mac OS, the Alt-? mnemonics are broken, so we do not set them
  }

  /** Set the keyboard mnemonic for the component in a way that is consistent with
    * the current platform. On Mac OS, the Alt-? mnemonics are broken, so we do not
    * set them.
    * @param obj the component whose mnemonic should be set
    * @param mnemonic the key code which represents the mnemonic
    * @see javax.swing.JTabbedPane#setMnemonic(int)
    * @see java.awt.event.KeyEvent */
  public void setMnemonic(javax.swing.JTabbedPane obj, int mnemonic) {
    // on Mac OS, the Alt-? mnemonics are broken, so we do not set them
  }
}
