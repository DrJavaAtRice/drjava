/*BEGIN_COPYRIGHT_BLOCK*

DrJava Eclipse Plug-in BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of DrJava, the JavaPLT group, Rice University, nor the names of software 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.plugins.eclipse;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
//import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
//import org.osgi.framework.Constants;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

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

  public static final String PLUGIN_ID = "edu.rice.cs.drjava";
 
  /**
   * The shared instance of the plugin.
   */
  private static EclipsePlugin _plugin;
  /**
   * The Resource bundle to be used by the plugin.
   */
  private ResourceBundle _resourceBundle;
  
  private BundleContext _context;
  
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
   * Make a best attempt to return the classpath containing all plugin libraries.  May fail
   * if the bundle URL can't be interpreted as a local file, or if some IO error occurs.  In
   * that case, returns an empty string.
   */
  public String getPluginClasspath() {
    Bundle bundle = _context.getBundle();
    URL installURL = bundle.getEntry("/");
    try { return FileLocator.toFileURL(installURL).getPath(); }
    catch (IOException e) { return ""; }
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
