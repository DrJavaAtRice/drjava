/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.File;
import java.util.LinkedList;
import java.lang.ClassLoader;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.reflect.PathClassLoader;

import static edu.rice.cs.plt.debug.DebugUtil.error;
import static edu.rice.cs.plt.debug.DebugUtil.debug;

/**
 * Maintains a dynamic class path, allowing entries to be incrementally added in the appropriate
 * place in the list.  This class is used in the interpreter JVM, and may be accessed concurrently.
 */
public class ClassPathManager implements Lambda<ClassLoader, ClassLoader> {
  
  // For thread safety, all accesses to these lists are synchronized on this, and when they are made available
  // to others (via getters or in the class loader), a snapshot is used.
  
  private final LinkedList<File> _projectCP;       /* The custom project class path. */
  private final LinkedList<File> _buildCP;         /* The build directory. */
  private final LinkedList<File> _projectFilesCP;  /* The open project files. */
  private final LinkedList<File> _externalFilesCP; /* The open external files. */
  private final LinkedList<File> _extraCP;         /* The extra preferences class path. */
  // these can be accessed concurrently:
  
  private final Iterable<File> _fullPath;
  
  public ClassPathManager(Iterable<File> builtInCP) {
    _projectCP = new LinkedList<File>();
    _buildCP = new LinkedList<File>();
    _projectFilesCP = new LinkedList<File>();
    _externalFilesCP = new LinkedList<File>();
    _extraCP = new LinkedList<File>();
    // conversions to SizedIterables are necessary to support 1.4 compatibility
    Iterable<Iterable<File>> allPaths =
      IterUtil.<Iterable<File>>make(IterUtil.asSizedIterable(_projectCP),
                                    IterUtil.asSizedIterable(_buildCP),
                                    IterUtil.asSizedIterable(_projectFilesCP),
                                    IterUtil.asSizedIterable(_externalFilesCP),
                                    IterUtil.asSizedIterable(_extraCP),
                                    IterUtil.snapshot(builtInCP));
    // lazily map the lists to their snapshots -- the snapshot code executes every time
    // _fullPath is traversed
    _fullPath = IterUtil.collapse(IterUtil.map(allPaths, _makeSafeSnapshot));
    updateProperty();
  }
  
  public static final String INTERACTIONS_CLASS_PATH_PROPERTY = "edu.rice.cs.drjava.interactions.class.path";
  
  protected void updateProperty() {
    System.setProperty(INTERACTIONS_CLASS_PATH_PROPERTY,IOUtil.pathToString(_fullPath));
  }
  
  private final Lambda<Iterable<File>, Iterable<File>> _makeSafeSnapshot =
    new Lambda<Iterable<File>, Iterable<File>>() {
    public Iterable<File> value(Iterable<File> arg) {
      synchronized(ClassPathManager.this) { return IterUtil.snapshot(arg); }
    }
  };
  
  /** Adds the entry to the front of the project classpath
    * (this is the classpath specified in project properties)
    */
  public synchronized void addProjectCP(File f) { _projectCP.addFirst(f); updateProperty(); }
  
  public synchronized Iterable<File> getProjectCP() { return IterUtil.snapshot(_projectCP); }
  
  /** Adds the entry to the front of the build classpath. */
  public synchronized void addBuildDirectoryCP(File f) {
    _buildCP.remove(f); // eliminate duplicates
    _buildCP.addFirst(f);
    updateProperty();
  }
  
  public synchronized Iterable<File> getBuildDirectoryCP() { return IterUtil.snapshot(_buildCP); }
  
  /** Adds the entry to the front of the project files classpath (this is the classpath for all open project files). */
  public synchronized void addProjectFilesCP(File f) {
    _projectFilesCP.remove(f); // eliminate duplicates
    _projectFilesCP.addFirst(f);
    updateProperty();
  }
  
  public synchronized Iterable<File> getProjectFilesCP() { return IterUtil.snapshot(_projectFilesCP); }
  
  /** Adds new entry containing f to the front of the external classpath. */
  public synchronized void addExternalFilesCP(File f) {
    _externalFilesCP.remove(f); // eliminate duplicates
    _externalFilesCP.addFirst(f);
    updateProperty();
  }
  
  public synchronized Iterable<File> getExternalFilesCP() { return IterUtil.snapshot(_externalFilesCP); }
  
  /** Adds the entry to the front of the extra classpath. */
  public synchronized void addExtraCP(File f) {
    _extraCP.remove(f); // eliminate duplicates
    _extraCP.addFirst(f);
    updateProperty();
  }
  
  public Iterable<File> getExtraCP() { return IterUtil.snapshot(_extraCP); }
  
  /** Create a new class loader based on the given path.  The loader's path is dynamically updated
    * as changes are made in the ClassPathManager.  Each loader returned by this method will
    * have its own set of loaded classes, and will only share those classes that are loaded
    * by a common parent.
    * @param parent  The parent class loader.  May be {@code null}, signifying the bootstrap
    *                class loader.
    */
  public synchronized ClassLoader makeClassLoader(ClassLoader parent) {
    updateProperty();
    return new PathClassLoader(parent, _fullPath);
  }
  
  /** Lambda value method */
  public ClassLoader value(ClassLoader parent) { return makeClassLoader(parent); }
  
  /** Get a dynamic view of the full class path. */
  public synchronized Iterable<File> getClassPath() { updateProperty(); return _fullPath; }
}
