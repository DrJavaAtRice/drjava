/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

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
  }
   
  /**
   * Hook for performing general UI setup.  Called after all other UI setup is done.
   * The Mac JDK implementation adds handlers for the application menu items.
   * @param about the Action associated with openning the About dialog
   * @param prefs the Action associated with openning the Preferences dialog
   * @param quit the Action associated with quitting the DrJava application
   */
  public void afterUISetup(Action about, Action prefs, Action quit) {
    
    final Action aboutAction = about;
    final Action prefsAction = prefs;
    final Action quitAction = quit;
    
    ApplicationListener appListener = new ApplicationAdapter() {
      public void handleAbout(ApplicationEvent e) {
        aboutAction.actionPerformed(new ActionEvent(this, 0, "About DrJava"));
        e.setHandled(true);
      }

      public void handlePreferences(ApplicationEvent e) {
        prefsAction.actionPerformed(new ActionEvent(this, 0, "Preferences..."));
        e.setHandled(true);
      }

      public void handleQuit(ApplicationEvent e) {
        // Workaround for 2868805:  show modal dialogs in a separate thread.
        // This encapsulation is not necessary in 10.2, but will not break either.
        final ApplicationEvent ae = e;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            quitAction.actionPerformed(new ActionEvent(this, 0, "Quit DrJava"));
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
  
}
