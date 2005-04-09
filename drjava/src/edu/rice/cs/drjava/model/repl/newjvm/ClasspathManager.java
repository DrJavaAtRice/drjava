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

package edu.rice.cs.drjava.model.repl.newjvm;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.lang.ClassLoader;
import edu.rice.cs.drjava.model.ClasspathEntry;
import edu.rice.cs.drjava.model.DeadClassLoader;
import edu.rice.cs.drjava.model.BrainClassLoader;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.ClasspathVector;
import java.io.File;


public class ClasspathManager{
  
  // the custom project classpath
  List<ClasspathEntry> projectCP;
  // the build directory
  List<ClasspathEntry> buildCP;
  // the open project files
  List<ClasspathEntry> projectFilesCP;
  // the open external files
  List<ClasspathEntry> externalFilesCP;
  // the extra preferences classpath
  List<ClasspathEntry> extraCP;
  // the system classpath
  List<ClasspathEntry> systemCP;
//  The open files classpath (for nonproject mode)
//  List<ClasspathEntry> openFilesCP;
  
  public ClasspathManager(){
    projectCP = new LinkedList<ClasspathEntry>();
    buildCP = new LinkedList<ClasspathEntry>();
    projectFilesCP = new LinkedList<ClasspathEntry>();
    externalFilesCP = new LinkedList<ClasspathEntry>();
    extraCP = new LinkedList<ClasspathEntry>();
    systemCP = new LinkedList<ClasspathEntry>();
//    openFilesCP = new LinkedList<ClasspathEntry>();
  }
  
  /** Adds the entry to the front of the project classpath
   *  (this is the classpath specified in project properties)
   */
  public void addProjectCP(URL f){
    // add new entry to front of classpath
    projectCP.add(0, new ClasspathEntry(f));
  }
  
  public List<ClasspathEntry> getProjectCP(){ return projectCP; }
  
  /** Adds the entry to the front of the build classpath. */
  public void addBuildDirectoryCP(URL f) {
    // add new entry to front of classpath
    buildCP.add(0, new ClasspathEntry(f));
  }

  public List<ClasspathEntry> getBuildDirectoryCP(){ return buildCP; }
  
  /** Adds the entry to the front of the project files classpath
   *  (this is the classpath for all open project files)
   */
  public void addProjectFilesCP(URL f){
    // add new entry to front of classpath
    projectFilesCP.add(0, new ClasspathEntry(f));
  }
  
  public List<ClasspathEntry> getProjectFilesCP(){
    return projectFilesCP;
  }
  
  /**
   * adds the entry to the front of the external classpath
   */
  public void addExternalFilesCP(URL f){
    // add new entry to front of classpath
    externalFilesCP.add(0, new ClasspathEntry(f));
  }
  
  public List<ClasspathEntry> getExternalFilesCP(){
    return externalFilesCP;
  }
  
  /**
   * adds the entry to the front of the extra classpath
   */
  public void addExtraCP(URL f){
    // add new entry to front of classpath
    extraCP.add(0, new ClasspathEntry(f));
  }
  
  public List<ClasspathEntry> getExtraCP(){
    return extraCP;
  }
  
  public List<ClasspathEntry> getSystemCP(){
    return systemCP;
  }
  
  
  
  /**
   * returns a new classloader that represents the
   * custom classpath
   */
  public ClassLoader getClassLoader(){
    return new BrainClassLoader(buildClassLoader(projectCP), 
                                buildClassLoader(buildCP), 
                                buildClassLoader(projectFilesCP), 
                                buildClassLoader(externalFilesCP), 
                                buildClassLoader(extraCP));
  }
  
  /**
   * builds a new classloader for the list of classpath entries
   */
  private ClassLoader buildClassLoader(List<ClasspathEntry>locpe){
    ClassLoader c = new DeadClassLoader();
    for(ClasspathEntry cpe: locpe){
        c = cpe.getClassLoader(c);
    }
    return c;
  }

  /**
   * Returns a copy of the list of unique entries on the classpath.
   */
  public ClasspathVector getAugmentedClasspath() {
    ClasspathVector ret = new ClasspathVector();
    List<ClasspathEntry> locpe = getProjectCP();
    for (ClasspathEntry e: locpe) {
      ret.add(e.getEntry());
    }

    locpe = getBuildDirectoryCP();
    for (ClasspathEntry e: locpe) {
      ret.add(e.getEntry());
    }

    locpe = getProjectFilesCP();
    for (ClasspathEntry e: locpe) {
      ret.add(e.getEntry());
    }

    locpe = getExternalFilesCP();
    for (ClasspathEntry e: locpe) {
      ret.add(e.getEntry());
    }

    locpe = getExtraCP();
    for (ClasspathEntry e: locpe) {
      ret.add(e.getEntry());
    }
    return ret;
  }

}

