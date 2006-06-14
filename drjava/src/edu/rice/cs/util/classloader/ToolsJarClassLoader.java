/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.classloader;

import java.net.*;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.swing.Utilities;

/** A class loader that tries to load classes from tools.jar.  It will never delegate to the system loader.
 *  @version $Id$
 */
public class ToolsJarClassLoader extends URLClassLoader {
  
  /* Directory containing tools.jar, if known by caller */
  
  /** Standard constructors */
  public ToolsJarClassLoader(File toolsJar) { super(getToolsJarURLs(toolsJar)); }
  public ToolsJarClassLoader() { this(FileOps.NONEXISTENT_FILE); }

  /** Returns an array of possible Files for the tools.jar file. */
  public static File[] getToolsJarFiles(File toolsJar) {
    File javaHome = FileOps.getCanonicalFile(new File(System.getProperty("java.home")));
    
    /*
     * javaHomeParents is a set of (attempted) canonical paths that may not exist.
     * We must maintain insertion order, so that the first entries have priority;
     * at the same time, we want to eliminate duplicates so that the same tools.jar file
     * doesn't show up multiple times.
     */
    LinkedHashSet<File> javaHomeParents = new LinkedHashSet<File>();
    javaHomeParents.add(FileOps.getCanonicalFile(new File(javaHome, "..")));
    javaHomeParents.add(FileOps.getCanonicalFile(new File(javaHome, "../..")));
    
    String winPrograms = System.getenv("ProgramFiles");
    if (winPrograms != null) {
      javaHomeParents.add(FileOps.getCanonicalFile(new File(winPrograms, "Java")));
      javaHomeParents.add(FileOps.getCanonicalFile(new File(winPrograms)));
    }
    else {  // in case the environment variables aren't set up properly
      javaHomeParents.add(FileOps.getCanonicalFile(new File("/C:/Program Files/Java/")));
      javaHomeParents.add(FileOps.getCanonicalFile(new File("/C:/Program Files/")));
    }

    String winSystem = System.getenv("SystemDrive");
    if (winSystem != null) {
      javaHomeParents.add(FileOps.getCanonicalFile(new File(winSystem, "Java")));
      javaHomeParents.add(FileOps.getCanonicalFile(new File(winSystem)));
    }
    else { // in case the environment variables aren't set up properly
      javaHomeParents.add(FileOps.getCanonicalFile(new File("/C:/Java/")));
      javaHomeParents.add(FileOps.getCanonicalFile(new File("/C:/")));
    }
    
    javaHomeParents.add(FileOps.getCanonicalFile(new File("/usr/")));
    javaHomeParents.add(FileOps.getCanonicalFile(new File("/usr/java/")));
    javaHomeParents.add(FileOps.getCanonicalFile(new File("/usr/j2se/")));
    javaHomeParents.add(FileOps.getCanonicalFile(new File("/usr/local/")));
    javaHomeParents.add(FileOps.getCanonicalFile(new File("/usr/local/java/")));
    javaHomeParents.add(FileOps.getCanonicalFile(new File("/usr/local/j2se/")));
    
    
    /* javaHomes is a set of potential Java installations.  Each is an existing directory. */
    LinkedHashSet<File> javaHomes = new LinkedHashSet<File>();
    
    try { if (javaHome.isDirectory()) { javaHomes.add(javaHome); } }
    catch (SecurityException e) { /* ignore */ }
    
    String version = System.getProperty("java.specification.version");
    final String prefix1 = "j2sdk" + version;
    final String prefix2 = "jdk" + version;
    FileFilter matchHomes = new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory() && (f.getName().startsWith(prefix1) || f.getName().startsWith(prefix2));
      }
    };
    for (File parent : javaHomeParents) {
      try {
        File[] files = parent.listFiles(matchHomes);
        if (files != null) { for (File f : files) javaHomes.add(f); }
      }
      catch (SecurityException e) { /* ignore */ }
    }
    
    /* The result is a set of existing tools.jar files, (attempted) canonicalized */
    LinkedHashSet<File> result = new LinkedHashSet<File>();
    
    try { if (toolsJar.isFile()) result.add(FileOps.getCanonicalFile(toolsJar)); }
    catch (SecurityException e) { /* ignore */ }
    
    for (File home : javaHomes) {
      try {
        File tools = new File(home, "lib/tools.jar");
        if (tools.isFile()) { result.add(FileOps.getCanonicalFile(tools)); }
      }
      catch (SecurityException e) { /* ignore */ }
    }

    return result.toArray(new File[0]);
  }
  
  /** Returns an array of possible URLs for the tools.jar file. */
  public static URL[] getToolsJarURLs() { return getToolsJarURLs(FileOps.NONEXISTENT_FILE); }
  
  /** Returns an array of possible URLs for the tools.jar file. */
  public static URL[] getToolsJarURLs(File toolsJar) {
    File[] files = getToolsJarFiles(toolsJar);
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
  
   /** Returns a string containing all possible tools.jar locations, separated by the system's path separator. */
  public static String getToolsJarClassPath() { return getToolsJarClassPath(FileOps.NONEXISTENT_FILE); }

  /** Returns a string containing all possible tools.jar locations, separated by the system's path separator. */
  public static String getToolsJarClassPath(File toolsJar) {
    File[] files = getToolsJarFiles(toolsJar);
    StringBuffer classPath = new StringBuffer();
    String pathSep = System.getProperty("path.separator");

    for (int i=0; i < files.length; i++) {
      if (i > 0) classPath.append(pathSep);
      classPath.append(files[i].getAbsolutePath());
    }
    return classPath.toString();
  }

  /** Gets the requested resource, bypassing the parent classloader. */
  public URL getResource(String name) { return findResource(name); }
}
