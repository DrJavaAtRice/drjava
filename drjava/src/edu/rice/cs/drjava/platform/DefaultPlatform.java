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

import javax.swing.*;
import java.net.URL;
import java.io.File;
import java.util.List;

import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileOption;

/**
 * Default platform-neutral implementation of PlatformSupport.  Most implementations
 * will extend this class to inherit default behaviors.
 * @version $Id$
 */
class DefaultPlatform implements PlatformSupport {
  /**
   * Singleton instance.
   */
  public static DefaultPlatform ONLY = new DefaultPlatform();
  
  /**
   * Private constructor for singleton pattern.
   */
  protected DefaultPlatform() {};
  
  /**
   * Utility method to determine if the current Swing look and feel is the
   * platform-specific look and feel for the client platform.
   * @return true if current Swing look and feel is the system look and feel
   */
  public boolean isUsingSystemLAF() {
    String sysLAF = UIManager.getSystemLookAndFeelClassName();
    String curLAF = UIManager.getLookAndFeel().getClass().getName();
    return (sysLAF.equals(curLAF));
  }
  
  /**
   * Hook for performing general UI setup.  Called before all other UI setup is done.
   * The default implementation does nothing.
   */
  public void beforeUISetup() {}
  
  /**
   * Hook for performing general UI setup.  Called after all other UI setup is done.
   * The default implementation does nothing.
   * @param about the Action associated with openning the About dialog
   * @param prefs the Action associated with openning the Preferences dialog
   * @param quit the Action associated with quitting the DrJava application
   */
  public void afterUISetup(Action about, Action prefs, Action quit) {}
  
  /**
   * Returns whether this is a Mac platform (any JDK version).
   */
  public boolean isMacPlatform() {
    return false;
  }
  
  /**
   * Returns whether this is a Mac platform with JDK 1.3.1.
   */
  public boolean isMac13Platform() {
    return false;
  }
  
  /**
   * Returns whether this is a Mac platform with JDK 1.4.1.
   */
  public boolean isMac14Platform() {
    return false;
  }
  
  /**
   * Returns whether this is a Windows platform.
   */
  public boolean isWindowsPlatform() {
    return false;
  }
  
  /**
   * Utility method for opening a URL in a browser in a platform-specific way.
   * The default implementation uses Runtime.exec to execute a command specified
   * in Preferences.  Platform implementations should attempt the default method
   * first, then try to use a "default browser", if such a thing exists on the
   * specific platform.
   * @param address the URL to open
   * @return true if the URL was successfully handled, false otherwise
   */
  public boolean openURL(URL address) {
    // Get the two config options.
    Configuration config = DrJava.getConfig();
    File exe = config.getSetting(OptionConstants.BROWSER_FILE);
    String args = config.getSetting(OptionConstants.BROWSER_STRING);
    
    // Check for empty settings.
    if ((exe == FileOption.NULL_FILE) && (args.equals(""))) {
      // If the user hasn't specified anything, don't try to run it.
      return false;
    }
    else {
      // Build a string array of command and arguments.
      List<String> command = ArgumentTokenizer.tokenize(args);
      command.add(0, exe.getAbsolutePath());
      command.add(address.toString());
      
      // Call the command.
      try {
        // Process proc = 
        Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
        
        // TODO: This may cause a memory leak on Windows, if we don't check the exit code.
      }
      catch (Throwable t) {
        // If there was any kind of problem, ignore it and report failure.
        return false;
      }
    }
    
    // Otherwise, trust that it worked.
    return true;
  }
}