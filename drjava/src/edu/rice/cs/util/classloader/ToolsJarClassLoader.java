package edu.rice.cs.util.classloader;

import java.net.*;
import java.io.File;

/**
 * A class loader that tries to load classes from tools.jar.
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
}
