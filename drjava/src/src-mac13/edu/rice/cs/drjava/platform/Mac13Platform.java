/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.platform;

import com.apple.mrj.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

/**
 * Platform-specific code unique to the 1.3.* version of the JDK on Mac OS X.
 */
class Mac13Platform extends MacPlatform {
  /**
   * Singleton instance.
   */
  public static Mac13Platform ONLY = new Mac13Platform();
  
  /**
   * Private constructor for singleton pattern.
   */
  protected Mac13Platform() {};
  
  /**
   * Hook for performing general UI setup.  Called before all other UI setup is done.
   * The Mac JDK 1.3 implementation sets a system property to use the screen menu bar.
   */
  public void beforeUISetup() {
    System.setProperty("com.apple.macos.useScreenMenuBar","true");
  }
  
  /**
   * Hook for performing general UI setup.  Called after all other UI setup is done.
   * The Mac JDK 1.3 implementation adds handlers for the application menu items.
   * @param about the Action associated with openning the About dialog
   * @param prefs the Action associated with openning the Preferences dialog
   * @param quit the Action associated with quitting the DrJava application
   */
  public void afterUISetup(Action about, Action prefs, Action quit) {
    
    final Action aboutAction = about;
    MRJAboutHandler aboutHandler = new MRJAboutHandler() {
      public void handleAbout() {
        aboutAction.actionPerformed(new ActionEvent(this, 0, "About DrJava"));
      }
    };
    MRJApplicationUtils.registerAboutHandler(aboutHandler);
    
    final Action prefsAction = prefs;
    MRJPrefsHandler prefsHandler = new MRJPrefsHandler() {
      public void handlePrefs() {
        prefsAction.actionPerformed(new ActionEvent(this, 0, "Preferences..."));
      }
    };
    MRJApplicationUtils.registerPrefsHandler(prefsHandler);
    
    final Action quitAction = quit;
    MRJQuitHandler quitHandler = new MRJQuitHandler() {
      public void handleQuit() {
        // Workaround for 2868805:  show modal dialogs in a separate thread.
        // This encapsulation is not necessary in 10.2, but will not break either.
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            quitAction.actionPerformed(new ActionEvent(this, 0, "Quit DrJava"));
          }
        });

        // Throw IllegalStateException so new thread can execute.  If showing
        // dialog on this thread in 10.2, we would throw upon JOptionPane.NO_OPTION
        throw new IllegalStateException("Quit Pending User Confirmation"); 
      }
    };
    MRJApplicationUtils.registerQuitHandler(quitHandler);
  }
}