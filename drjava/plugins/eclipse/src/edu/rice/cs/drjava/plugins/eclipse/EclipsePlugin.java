package edu.rice.cs.drjava.plugins.eclipse;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class EclipsePlugin extends AbstractUIPlugin {
  // The shared instance.
  private static EclipsePlugin plugin;
  // Resource bundle.
  private ResourceBundle resourceBundle;
  
  /**
   * The constructor.
   */
  public EclipsePlugin(IPluginDescriptor descriptor) {
    super(descriptor);
    plugin = this;
    try {
      resourceBundle = ResourceBundle.getBundle("edu.rice.cs.drjava.plugins.eclipse.EclipsePluginResources");
    }
    catch (MissingResourceException x) {
      resourceBundle = null;
    }
  }
  
  /**
   * Returns the shared instance.
   */
  public static EclipsePlugin getDefault() {
    return plugin;
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
    ResourceBundle bundle= EclipsePlugin.getDefault().getResourceBundle();
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
    return resourceBundle;
  }
}
