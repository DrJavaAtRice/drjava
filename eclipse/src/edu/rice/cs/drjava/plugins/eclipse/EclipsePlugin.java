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

package edu.rice.cs.drjava.plugins.eclipse;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import java.util.*;
import java.net.URL;
import java.io.IOException;

/**
 * The main plugin class to be used in the desktop.
 * 
 * This was partially generated using Eclipse's plugin wizard.  It appears to
 * partly follow the singleton pattern, but the constructor is still public.
 * Perhaps this is one of their own conventions?
 * 
 * @version $Id$
 */
public class EclipsePlugin extends AbstractUIPlugin {
  /**
   * The shared instance of the plugin.
   */
  private static EclipsePlugin _plugin;
  /**
   * The Resource bundle to be used by the plugin.
   */
  private ResourceBundle _resourceBundle;
  
  /**
   * Constructs a new EclipsePlugin.
   * @param descriptor PluginDescriptor used by Eclipse
   */
  public EclipsePlugin() {
    super();
    _plugin = this;
    try {
      _resourceBundle = ResourceBundle.getBundle("edu.rice.cs.drjava.plugins.eclipse.EclipsePluginResources");
    }
    catch (MissingResourceException x) {
      _resourceBundle = null;
    }
  }
  
  /**
   * Returns the shared instance of the plugin.
   */
  public static EclipsePlugin getDefault() {
    return _plugin;
  }
  
  /**
   * Returns the workspace instance.
   */
  public static IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }
  
  /**
   * Returns the string from the plugin's resource bundle,
   * or 'key' if not found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = EclipsePlugin.getDefault().getResourceBundle();
    try {
      return bundle.getString(key);
    }
    catch (MissingResourceException e) {
      return key;
    }
  }
  
  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle() {
    return _resourceBundle;
  }

  /**
   * Returns the classpath containing all plugin libraries.
   */
  public String getPluginClasspath() throws IOException {
    // Note: This method tries to find a local path from a URL.
    //  It's possible that if a remote URL is returned, the
    //  interpreter JVM won't be able to start, which could
    //  cause the interpreter to lock up.  We should make sure
    //  this can't happen.
    URL installURL = getDescriptor().getInstallURL();
    String installDir = Platform.resolve(installURL).getPath();
    String pathSep = System.getProperty("path.separator");
    String fileSep = System.getProperty("file.separator");
    StringBuffer buf = new StringBuffer();
    ILibrary[] libs = getDescriptor().getRuntimeLibraries();
    for (int i=0; i < libs.length; i++) {
      buf.append(installDir);
      buf.append(fileSep);
      buf.append(libs[i].getPath().toOSString());
      buf.append(pathSep);
    }
    return buf.toString();
  }
  
  /** 
   * Sets default preference values. These values will be used
   * until some preferences are actually set using Preference dialog.
   */
  protected void initializeDefaultPreferences(IPreferenceStore store) {
    // These settings will show up when Preference dialog
    // opens up for the first time.
    store.setDefault(DrJavaConstants.INTERACTIONS_RESET_PROMPT, true);
    store.setDefault(DrJavaConstants.ALLOW_PRIVATE_ACCESS, false);
    store.setDefault(DrJavaConstants.INTERACTIONS_EXIT_PROMPT, true);
    store.setDefault(DrJavaConstants.HISTORY_MAX_SIZE, 500);
    store.setDefault(DrJavaConstants.JVM_ARGS, "");
  }
}
