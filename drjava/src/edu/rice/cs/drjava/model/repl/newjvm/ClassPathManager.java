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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.lang.ClassLoader;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.drjava.model.DeadClassLoader;
import edu.rice.cs.drjava.model.BrainClassLoader;

import static edu.rice.cs.plt.debug.DebugUtil.error;

/* This class runs in the Main JVM, but it accessed from the Slave JVM via RMI.  All public methods are synchronzed. */
public class ClassPathManager {
  
  private LinkedList<File> projectCP;              /* The custom project classpath. */
  private LinkedList<File> buildCP;                /* The build directory. */
  private LinkedList<File> projectFilesCP;         /* The open project files. */
  private LinkedList<File> externalFilesCP;        /* The open external files. */
  private LinkedList<File> extraCP;                /* The extra preferences classpath. */ 
  
//  private volatile LinkedList<File> systemCP;               /* The system classpath. */
//  private List<File> openFilesCP;                           /* Open files classpath (for nonproject mode) */
  
  public ClassPathManager() {
    projectCP = new LinkedList<File>();
    buildCP = new LinkedList<File>();
    projectFilesCP = new LinkedList<File>();
    externalFilesCP = new LinkedList<File>();
    extraCP = new LinkedList<File>();
//    systemCP = new LinkedList<File>();
//    openFilesCP = new LinkedList<File>();
  }
  
  /** Adds the entry to the front of the project classpath
   *  (this is the classpath specified in project properties)
   */
  public synchronized void addProjectCP(File f) { projectCP.add(f); }
  
  public synchronized Iterable<File> getProjectCP() { return IterUtil.reverse(projectCP); }
  
  /** Adds the entry to the front of the build classpath. */
  public synchronized void addBuildDirectoryCP(File f) { buildCP.add(f); }

  public synchronized Iterable<File> getBuildDirectoryCP() { return IterUtil.reverse(buildCP); }
  
  /** Adds the entry to the front of the project files classpath (this is the classpath for all open project files). */
  public synchronized void addProjectFilesCP(File f) { projectFilesCP.add(f); }
  
  public synchronized Iterable<File> getProjectFilesCP() { return IterUtil.reverse(projectFilesCP); }
  
  /** Adds new entry containing f to the front of the external classpath. */
  public void addExternalFilesCP(File f) { externalFilesCP.add(f); }
  
  public Iterable<File> getExternalFilesCP() { return IterUtil.reverse(externalFilesCP); }
  
  /** Adds the entry to the front of the extra classpath. */
  public synchronized void addExtraCP(File f) { extraCP.add(f); }
  
  public Iterable<File> getExtraCP() { return IterUtil.reverse(extraCP); }
  
  /** Returns a new classloader that represents the custom classpath. */
  public synchronized ClassLoader getClassLoader() {
    return new BrainClassLoader(buildClassLoader(projectCP), 
                                buildClassLoader(buildCP), 
                                buildClassLoader(projectFilesCP), 
                                buildClassLoader(externalFilesCP), 
                                buildClassLoader(extraCP));
  }
  
  /** Builds a new classloader for the list of classpath entries. */
  private ClassLoader buildClassLoader(List<File> path) {
    List<URL> urls = new LinkedList<URL>();
    for (File f : path) {
      try {
        URL u = f.toURI().toURL();
        urls.add(u);
      }
      catch (MalformedURLException e) { error.log("Can't convert file to URL", e); }
    }
    return new URLClassLoader(urls.toArray(new URL[urls.size()]), new DeadClassLoader());
  }

}

