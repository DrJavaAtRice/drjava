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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Field;

import java.util.LinkedList;

// Importing the Java 1.3 / JSR-14 v1.0 Compiler classes
import com.sun.tools.javac.v8.JavaCompiler;
import com.sun.tools.javac.v8.util.Name;
import com.sun.tools.javac.v8.util.Position;
import com.sun.tools.javac.v8.util.Hashtable;
import com.sun.tools.javac.v8.util.List;
import com.sun.tools.javac.v8.util.Log;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.FileOps;

/**
 * An implementation of the CompilerInterface that supports compiling with
 * the JSR-14 prototype compiler.
 * It adds the collections classes signature to the bootclasspath
 * as requested by {@link Configuration}.
 * 
 * This class can only be compiled using the JSR-14 v1.0 compiler,
 * since the JSR-14 v1.2 compiler has incompatible classes.
 *
 * @version $Id$
 */
public class JSR14v10Compiler extends JavacGJCompiler {
  
  private File _collectionsPath;
  
  /** Singleton instance. */
  public static final CompilerInterface ONLY = new JSR14v10Compiler();

  protected JSR14v10Compiler() {
    super();
  }
  
  public boolean isAvailable() {
    try {
      // Main$10 exists in JSR14 v1.0 but not JDK 1.3
      Class.forName("com.sun.tools.javac.v8.Main$10");
      return super.isAvailable();
    }
    catch (Throwable t) {
      return false;
    }
  }
      
  
  protected void updateBootClassPath() {

    // add collections path to the bootclasspath
    // Yes, we are mutating some other class's public variable.
    // But the docs for ClassReader say it's OK for others to mutate it!
    // And this way, we don't need to specify the entire bootclasspath,
    // just what we want to add on to it.
    
    if (_collectionsPath != null) {
      String ccp = _collectionsPath.getAbsolutePath();
    
      if (ccp != null && ccp.length() > 0) {
        compiler.syms.reader.bootClassPath = ccp +
          System.getProperty("path.separator")+
          compiler.syms.reader.bootClassPath;
  
      }
    }
  }
    
  protected void initCompiler(File[] sourceRoots) {
    super.initCompiler(sourceRoots);
    updateBootClassPath();
  }

  /**
   * This method allows us to set the JSR14 collections path across a class loader.
   * (cannot cast a loaded class to a subclass, so all compiler interfaces must have this method)
   */ 
  public void addToBootClassPath( File cp) {
    _collectionsPath = cp;
  }
  
  public String getName() { return "JSR-14 v1.0"; }


}
