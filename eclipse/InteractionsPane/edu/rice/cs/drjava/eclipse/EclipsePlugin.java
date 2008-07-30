/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2008 JavaPLT group at Rice University (drjava@rice.edu)
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
 * (drjava@rice.edu) gives permission to link the code of DrJava with
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

package edu.rice.cs.drjava.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
//import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
//import org.osgi.framework.Constants;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.FileOps;

/**
 * The main plugin class to be used in the desktop.
 * 
 * This was partially generated using Eclipse's plugin wizard.  It appears to
 * partly follow the singleton pattern, but the constructor is still public.
 * Perhaps this is one of their own conventions?
 * 
 * @version $Id: EclipsePlugin.java 4314 2008-01-30 00:08:33Z mgricken $
 */
public class EclipsePlugin extends AbstractUIPlugin {
  public static final String PLUGIN_ID = "InteractionsPane";
  public static final String DRJAVA_JAR = "drjava-15.jar";
	
  /**
   * The shared instance of the plugin.
   */
  private static EclipsePlugin _plugin;
  /**
   * The Resource bundle to be used by the plugin.
   */
  private ResourceBundle _resourceBundle;
  
  private BundleContext _context;
  
  private File _tempDir;
  
  /**
   * Constructs a new EclipsePlugin.
   * @param descriptor PluginDescriptor used by Eclipse
   */
  public EclipsePlugin() {
    super();
    try {
      _resourceBundle = ResourceBundle.getBundle("edu.rice.cs.drjava.plugins.eclipse.EclipsePluginResources");
    }
    catch (MissingResourceException x) {
      _resourceBundle = null;
    }
  }
  
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
	  super.start(context);
	  _context = context;
	  _plugin = this;
	  _tempDir = FileOps.createTempDirectory(PLUGIN_ID);
	  IOUtil.deleteOnExitRecursively(_tempDir);
	  Bundle bundle = context.getBundle();
	  Path path = new Path(DRJAVA_JAR);
	  URL fileURL = FileLocator.find(bundle, path, null);
	  InputStream in = fileURL.openStream();
	  File outfile = new File(_tempDir, DRJAVA_JAR);
	  FileOutputStream out = new FileOutputStream(outfile);
	  IOUtil.copyInputStream(in, out);
	  log("Plugin started.");
  }
  
  public void log(String msg) {
	  log(msg, null);
  }

  public void log(String msg, Exception e) {
	  getLog().log(new Status(Status.INFO, PLUGIN_ID, Status.OK, msg, e));
  }
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
	  _plugin = null;
	  _context = null;
	  log("Plugin stopped.");
	  super.stop(context);
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
   * Returns an image descriptor for the image file at the given
   * plug-in relative path
   *
   * @param path the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
	  return imageDescriptorFromPlugin(PLUGIN_ID, path);
  }
  
  /**
   * Returns the string from the plugin's resource bundle,
   * or 'key' if not found.
   */
  public static String getResourceString(String key) {
    ResourceBundle bundle = getDefault().getResourceBundle();
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
 * @throws BundleException If there is an error resolving the {@link Bundle}'s classpath.
   */
  public String getPluginClasspath() throws IOException, BundleException {
    // Note: This method tries to find a local path from a URL.
    //  It's possible that if a remote URL is returned, the
    //  interpreter JVM won't be able to start, which could
    //  cause the interpreter to lock up.  We should make sure
    //  this can't happen.
	
	//  This method is used ONLY for this reason: to pass the right
	//  classpath to the interpreter JVM.
	final String trimPrefix = "file:";
	final String trimSuffix = "!";
	Bundle bundle = _context.getBundle();
    String pathSep = System.getProperty("path.separator");
    String fileSep = System.getProperty("file.separator");
    URL installURL = bundle.getEntry("/");
	String installDir = FileLocator.resolve(installURL).getPath();
    if(installDir.startsWith(trimPrefix))
    	installDir = installDir.substring(
    			installDir.indexOf(trimPrefix) + trimPrefix.length(),
    			installDir.length());
    if(installDir.endsWith(fileSep)) {
    	installDir = installDir.substring(0, installDir.lastIndexOf(fileSep));
    }
    if(installDir.endsWith(trimSuffix))
    	installDir = installDir.substring(0, installDir.lastIndexOf(trimSuffix));
    StringBuffer buf = new StringBuffer();
    buf.append(installDir);
    buf.append(pathSep);
    buf.append(_tempDir);
    buf.append(fileSep);
    buf.append(DRJAVA_JAR);
//	String requires = (String)bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
//	ManifestElement[] elements = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, requires);
//    for (int i=0; i < elements.length; i++) {
//      buf.append(installDir);
//      buf.append(fileSep);
//      buf.append(elements[i].getValue());
//      buf.append(pathSep);
//    }
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
