/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
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
    StringBuffer classpath = new StringBuffer();
    String pathSep = System.getProperty("path.separator");

    for (int i=0; i < files.length; i++) {
      if (i > 0) classpath.append(pathSep);
      classpath.append(files[i].getAbsolutePath());
    }
    return classpath.toString();
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
    if (javaHome.indexOf("Program Files") == -1) {
      return "";
    }

    String prefix = "C:\\j2sdk";
    String suffix = "\\lib\\tools.jar";
    int versionIndex;

    if (javaHome.indexOf("JavaSoft") != -1) {
      prefix = "C:\\jdk";
      versionIndex = javaHome.indexOf("JRE\\") + 4;
    }
    else {
      versionIndex = javaHome.indexOf("j2re") + 4;
    }
    String version = javaHome.substring(versionIndex);

    return prefix + version + suffix;
  }

  /**
   * Gets the requested resource, bypassing the parent classloader.
   */
  public URL getResource(String name) {
    return findResource(name);
  }
}
