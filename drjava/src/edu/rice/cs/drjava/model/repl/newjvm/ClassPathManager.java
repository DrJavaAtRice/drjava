/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2015, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl.newjvm;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.lang.ClassLoader;

import edu.rice.cs.drjava.DrScala;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;

//import static edu.rice.cs.plt.debug.DebugUtil.error;
//import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Maintains a dynamic class path, allowing entries to be incrementally added in the appropriate
  * place in the list.  In normal DrScala sessions, this class is used in the interpreter JVM, and 
  * may be accessed concurrently.
  */
public class ClassPathManager implements Lambda<ClassLoader, ClassLoader> {
  
  public static final Log _log = DrScala._log;
  
  public static final String INTERACTIONS_CLASS_PATH = "edu.rice.cs.drjava.interactions.class.path";
  
  // For thread safety, all accesses to these lists are synchronized on this, and when they are made available
  // to others (via getters or in the class loader), a new copy is made.
  
  private final List<File> _interactionsClassPath;       /* The class path maintained for the slave JVM. */
  
  /* NOTE: this method should be synchronised to robustly support for multi-threading, but Java does not allow it. */
  public ClassPathManager(Iterable<File> initialClassPath) {
    _interactionsClassPath = CollectUtil.makeArrayList(initialClassPath);
  }
  
  // Saves the current value of _interactionsClassPath in a System property
  protected synchronized void updateProperty() {
    System.setProperty(INTERACTIONS_CLASS_PATH, _interactionsClassPath.toString());
  }
  
  /** Adds the entry to the front of the interactions classpath, unless already present.
    * @return true iff f is already present in the interactions class path. 
    * Note: a better data structure might be used to avoid O(N^2) cost for adding N new files.
    */
  public synchronized boolean addInteractionsClassPath(File f) {
    _log.log("In ClassPathManager, addInteractionsClassPath(" + f + ") called");
    boolean isPresent = _interactionsClassPath.contains(f);
    if (! isPresent) { 
      _interactionsClassPath.add(0,f); // Terrible notation for cons(f, _interactionsClassPath)
      Utilities.show("In ClassPathManger, " + f + " added to interactions class path");
    }
    return isPresent;
  }

  /** Returns a copy of _interactionsClassPath. */
  public synchronized List<File> getInteractionsClassPath() { return _interactionsClassPath; }
  
  /** Create a new class loader based on the given path.  The loader's path is dynamically updated as changes are made 
    * in the ClassPathManager.  Each loader returned by this method will have its own set of loaded classes, and will 
    * only share those classes that are loaded by a common parent.
    * @param parent  The parent class loader.  May be {@code null}, signifying the bootstrap class loader.
    */
  public synchronized ClassLoader makeClassLoader(ClassLoader parent) {
    _log.log("Creating new PathClassLoader with parent " + parent + " and path " + _interactionsClassPath);
    return new PathClassLoader(parent, _interactionsClassPath);
  }
  
  /** value method supporting Lambda<ClassLoader, ClassLoader> interface. */
  public ClassLoader value(ClassLoader parent) { return makeClassLoader(parent); }
}
