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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.lang.ClassLoader;
import edu.rice.cs.drjava.model.ClassPathEntry;
import edu.rice.cs.drjava.model.DeadClassLoader;
import edu.rice.cs.drjava.model.BrainClassLoader;

import edu.rice.cs.util.ClassPathVector;

/* This class runs in the Main JVM, but it accessed from the Slave JVM via RMI.  All public methods are synchronzed. */
public class ClassPathManager{
  
  private LinkedList<ClassPathEntry> projectCP;              /* The custom project classpath. */
  private LinkedList<ClassPathEntry> buildCP;                /* The build directory. */
  private LinkedList<ClassPathEntry> projectFilesCP;         /* The open project files. */
  private LinkedList<ClassPathEntry> externalFilesCP;        /* The open external files. */
  private LinkedList<ClassPathEntry> extraCP;                /* The extra preferences classpath. */ 
  
//  private volatile LinkedList<ClassPathEntry> systemCP;               /* The system classpath. */
//  private List<ClasspathEntry> openFilesCP;                           /* Open files classpath (for nonproject mode) */
  
  public ClassPathManager() {
    projectCP = new LinkedList<ClassPathEntry>();
    buildCP = new LinkedList<ClassPathEntry>();
    projectFilesCP = new LinkedList<ClassPathEntry>();
    externalFilesCP = new LinkedList<ClassPathEntry>();
    extraCP = new LinkedList<ClassPathEntry>();
//    systemCP = new LinkedList<ClassPathEntry>();
//    openFilesCP = new LinkedList<ClasspathEntry>();
  }
  
  /** Adds the entry to the front of the project classpath
   *  (this is the classpath specified in project properties)
   */
  public synchronized void addProjectCP(URL f) { projectCP.add(0, new ClassPathEntry(f)); }
  
  public synchronized ClassPathEntry[] getProjectCP() { 
    return projectCP.toArray(new ClassPathEntry[projectCP.size()]); 
  }
  
  /** Adds the entry to the front of the build classpath. */
  public synchronized void addBuildDirectoryCP(URL f) {
    buildCP.addFirst(new ClassPathEntry(f));
  }

  public synchronized ClassPathEntry[] getBuildDirectoryCP() { 
    return buildCP.toArray(new ClassPathEntry[buildCP.size()]); 
  }
  
  /** Adds the entry to the front of the project files classpath (this is the classpath for all open project files). */
  public synchronized void addProjectFilesCP(URL f) { projectFilesCP.addFirst(new ClassPathEntry(f)); }
  
  public synchronized ClassPathEntry[] getProjectFilesCP() { 
    return projectFilesCP.toArray(new ClassPathEntry[projectFilesCP.size()]); 
  }
  
  /** Adds new entry containing f to the front of the external classpath. */
  public void addExternalFilesCP(URL f) { externalFilesCP.add(0, new ClassPathEntry(f)); }
  
  public ClassPathEntry[] getExternalFilesCP() { 
    return externalFilesCP.toArray(new ClassPathEntry[externalFilesCP.size()]); 
  }
  
  /** Adds the entry to the front of the extra classpath. */
  public synchronized void addExtraCP(URL f) { extraCP.addFirst(new ClassPathEntry(f)); }
  
  public ClassPathEntry[] getExtraCP() { return extraCP.toArray(new ClassPathEntry[extraCP.size()]); }
  
  /** Returns a new classloader that represents the custom classpath. */
  public synchronized ClassLoader getClassLoader() {
    return new BrainClassLoader(buildClassLoader(projectCP), 
                                buildClassLoader(buildCP), 
                                buildClassLoader(projectFilesCP), 
                                buildClassLoader(externalFilesCP), 
                                buildClassLoader(extraCP));
  }
  
  /** Builds a new classloader for the list of classpath entries. */
  private ClassLoader buildClassLoader(List<ClassPathEntry>locpe) {
    ClassLoader c = new DeadClassLoader();
    for(ClassPathEntry cpe: locpe) { c = cpe.getClassLoader(c); }
    return c;
  }

  /** Returns a copy of the list of unique entries on the classpath. */
  public synchronized ClassPathVector getAugmentedClassPath() {
    ClassPathVector ret = new ClassPathVector();
  
    for (ClassPathEntry e: getProjectCP()) { ret.add(e.getEntry()); }

    for (ClassPathEntry e: getBuildDirectoryCP()) { ret.add(e.getEntry()); }

    for (ClassPathEntry e: getProjectFilesCP()) { ret.add(e.getEntry()); }

    for (ClassPathEntry e: getExternalFilesCP()) { ret.add(e.getEntry()); }

    for (ClassPathEntry e: getExtraCP()) { ret.add(e.getEntry()); }
    return ret;
  }

}

