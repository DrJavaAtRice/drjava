package edu.rice.cs.util.classloader;

import java.net.*;
import java.io.File;

/**
 * A class loader that tries to load classes from tools.jar.
 * It will never delegate to the system loader.
 *
 * NOTE: I am not sure if this loader will work perfectly correctly
 * if you use loadClass. Currently its purpose is to be used from
 * {@link StickyClassLoader}, which just needs getResource.
 *
 * @version $Id$
 */
public class ToolsJarClassLoader extends URLClassLoader {
  public ToolsJarClassLoader() {
    super(_getURLs());
  }

  private static URL[] _getURLs() {
    File home = new File(System.getProperty("java.home"));
    File libDir = new File(home, "lib");
    File libDir2 = new File(home.getParentFile(), "lib");

    try {
      return new URL[] {
        new File(libDir, "tools.jar").toURL(),
        new File(libDir2, "tools.jar").toURL()
      };
    }
    catch (MalformedURLException e) {
      return new URL[0];
    }
  }

  /**
   * Gets the requested resource, bypassing the parent classloader.
   */
  public URL getResource(String name) {
    return findResource(name);
  }
}
