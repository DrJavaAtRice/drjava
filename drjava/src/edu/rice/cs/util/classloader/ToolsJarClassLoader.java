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

package edu.rice.cs.util.classloader;

import java.net.*;
import java.io.File;

import java.util.ArrayList;

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
    super(getToolsJarURLs());
  }
  
  /**
   * Returns an array of possible Files for the tools.jar file.
   */
  public static File[] getToolsJarFiles() {
    String javaHome = System.getProperty("java.home");
    File home = new File(javaHome);    
    ArrayList<File> files = new ArrayList<File>();
    
    // Check $JAVA_HOME/lib/tools.jar
    File libDir = new File(home, "lib");
    File jar = new File(libDir, "tools.jar");
    if (jar.exists()) {
      files.add(jar);
    }
    
    // Check $JAVA_HOME/../lib/tools.jar
    File libDir2 = new File(home.getParentFile(), "lib");
    File jar2 = new File(libDir2, "tools.jar");
    if (jar2.exists()) {
      files.add(jar2);
    }
    
    if (javaHome.toLowerCase().indexOf("program files") != -1) {
      // Windows: JavaHome is JRE; guess where SDK is
      File jar3 = new File(getWindowsToolsJar(javaHome));
      if (jar3.exists()) {
        files.add(jar3);
      }
    }

    File[] fileArray = new File[files.size()];
    files.toArray(fileArray);
    return fileArray;
  }
  
  /**
   * Returns an array of possible URLs for the tools.jar file.
   */
  public static URL[] getToolsJarURLs() {
    File[] files = getToolsJarFiles();
    try {
      URL[] urls = new URL[files.length];
      for (int i=0; i < files.length; i++) {
        urls[i] = files[i].toURL();
      }
      return urls;
    }
    catch (MalformedURLException e) {
      return new URL[0];
    }
  }
  
  /**
   * Returns a string containing all possible tools.jar locations,
   * separated by the system's path separator.
   */
  public static String getToolsJarClasspath() {
    File[] files = getToolsJarFiles();
    String classpath = "";
    String pathSep = System.getProperty("path.separator");
    
    for (int i=0; i < files.length; i++) {
      if (i > 0) classpath += pathSep;
      classpath += files[i].getAbsolutePath();
    }
    return classpath;
  }
  
  /**
   * Returns a guess for the location of tools.jar based on the default
   * installation directory for the Windows Java SDK.  In Windows,
   * JAVA_HOME is set to the JRE directory in "Program Files", but tools.jar
   * is located in the SDK directory.  Guess is simplistic: only looks on C:.
   *
   * PRECONDITION: javaHome contains "Program Files"
   *
   * @param javaHome The current JAVA_HOME System property
   */
  public static String getWindowsToolsJar(String javaHome) {
    if (javaHome.indexOf("Program Files") == -1) return "";
    
    String prefix = "C:\\j2sdk";
    String suffix = "\\lib\\tools.jar";
    String version = "";
    
    if (javaHome.indexOf("JavaSoft") != -1) {
      prefix = "C:\\jdk";
      int versionIndex = javaHome.indexOf("JRE\\") + 4;
      version = javaHome.substring(versionIndex);
    }
    else {
      int versionIndex = javaHome.indexOf("j2re") + 4;
      version = javaHome.substring(versionIndex);
    }
    
    return prefix + version + suffix;
  }

  /**
   * Gets the requested resource, bypassing the parent classloader.
   */
  public URL getResource(String name) {
    return findResource(name);
  }
}
