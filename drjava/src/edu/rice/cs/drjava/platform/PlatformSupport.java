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

import javax.swing.Action;
import java.net.URL;

/**
 * Central interface for all platform-specific code in DrJava.
 * A default platform-neutral implementation is provided in DefaultPlatform.
 * @version $Id$
 * @see edu.rice.cs.drjava.platform.DefaultPlatform
 */
public interface PlatformSupport {
  
  /**
   * Returns whether this is a Mac platform (any JDK version).
   */
  public boolean isMacPlatform();
  
  /**
   * Returns whether this is a Mac platform with JDK 1.3.1.
   */
  public boolean isMac13Platform();
  
  /**
   * Returns whether this is a Mac platform with JDK 1.4.1.
   */
  public boolean isMac14Platform();
  
  /**
   * Returns whether this is a Windows platform.
   */
  public boolean isWindowsPlatform();
  
  /**
   * Utility method to determine if the current Swing look and feel is the
   * platform-specific look and feel for the client platform.
   * @return true if current Swing look and feel is the system look and feel
   */
  public boolean isUsingSystemLAF();
  
  /**
   * Hook for performing general UI setup.  Called before all other UI setup is done.
   */
  public void beforeUISetup();
  
  /**
   * Hook for performing general UI setup.  Called after all other UI setup is done.
   * @param about the Action associated with openning the About dialog
   * @param prefs the Action associated with openning the Preferences dialog
   * @param quit the Action associated with quitting the DrJava application
   */
  public void afterUISetup(Action about, Action prefs, Action quit);
  
  /**
   * Utility method for opening a URL in a browser in a platform-specific way.
   * The default implementation uses Runtime.exec to execute a command specified
   * in Preferences.  Platform implementations should attempt the default method
   * first, then try to use a "default browser", if such a thing exists on the
   * specific platform.
   * @param address the URL to open
   * @return true if the URL was successfully handled, false otherwise
   */
  public boolean openURL(URL address);
}
