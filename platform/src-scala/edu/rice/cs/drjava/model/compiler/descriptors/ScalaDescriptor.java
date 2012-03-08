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

package edu.rice.cs.drjava.model.compiler.descriptors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.jar.JarFile;

import edu.rice.cs.drjava.model.JDKDescriptor;
import edu.rice.cs.drjava.model.JDKToolsLibrary;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.swing.Utilities;

/** The description of the HJ compound JDK. */
public class ScalaDescriptor extends JDKDescriptor {
  /** Return the name of this JDK.
    * @return name */
  public String getName() { return "Scala"; }
  
  /** Packages to shadow when loading a new tools.jar.  If we don't shadow these classes, we won't
    * be able to load distinct versions for each tools.jar library.  These should be verified whenever
    * a new Java version is released.  (We can't just shadow *everything* because some classes, at 
    * least in OS X's classes.jar, can only be loaded by the JVM.)
    * 
    * @return set of packages that need to be shadowed
    */
  public Set<String> getToolsPackages() {
    HashSet<String> set = new HashSet<String>();
    Collections.addAll(set, new String[] {
//        "com.sun.tools.javac",
//        "com.sun.tools.javac.tree",
//        "com.sun.tools.javac.comp",
//        "com.sun.tools.javac.main"
    });
    return set;
  }

  /** Returns a list of directories that should be searched for tools.jar and classes.jar files.
    * @return list of directories to search */
  public Iterable<File> getSearchDirectories() {
    return IterUtil.singleton(edu.rice.cs.util.FileOps.getDrJavaFile().getParentFile());
  }

  /** Returns a list of files that should be searched if they contain a compiler.
    * @return list of files to search */
  public Iterable<File> getSearchFiles() {
    Iterable<File> files = IterUtil.asIterable(new File[] {
      new File("/C:/Scala/scala-2.9.1.final/lib/scala-compiler.jar")
    });
    try {
      String scala_home = System.getenv("SCALA_HOME");
      if (scala_home != null) {
        files = IterUtil.compose(files, new File(new File(scala_home), "lib/scala-compiler.jar"));
      }
      else {
        JDKToolsLibrary.msg("SCALA_HOME not set");
      }
    }
    catch(Exception e) { /* ignore SCALA_HOME variable */ }
    
    // drjava.jar file itself; check if it's a combined Scala/DrJava jar
    files = IterUtil.compose(edu.rice.cs.util.FileOps.getDrJavaFile(), files); 
    Utilities.show("getSearchFiles is returning " + files);
    return files;
  }
  
  /** True if this is a compound JDK and needs a fully featured JDK to operate.
    * @return true if compound JDK (e.g. NextGen, Mint, Habanero). */
  public boolean isCompound() { return true; }
  
  /** Return true if the file (jar file or directory) contains the compiler.
    * @return true if the file contains the compiler */
  public boolean containsCompiler(File f) {
    return Util.exists(f,
                       "scala/tools/nsc/Main.class",
                       "scala/tools/nsc/Global.class");
  }

  /** Return the guessed version for the compiler in the specified file (jar file or directory).
    * Note that this is the Java version that this compiler is compatible to, not the internal compiler version.
    * For full (non-compound) JDKs, this is equal to the version, i.e. JDK6 should guess Java 6.0.
    * For compound JDKs, this is equal to the version of the full JDK that the compound JDK needs, i.e.
    * if a version of the HJ compiler requires JDK6, it should guess JDK6.
    * @return guessed version */
  public JavaVersion.FullVersion guessVersion(File f) {
      return JavaVersion.parseFullVersion(JavaVersion.JAVA_7.fullVersion().versionString(),
                                          "Java PLT Research Group",
                                          "Java PLT Research Group",
                                          f);
  }
  
  /** Return the class name of the compiler adapter.
    * @return class name of compiler, or null if no compiler */
  public String getAdapterForCompiler() { return "edu.rice.cs.drjava.model.compiler.ScalaCompiler"; }

  /** Return the class name of the debugger adapter.
    * @return class name of debugger, or null if no debugger */
  public String getAdapterForDebugger() { return null; }
  
  /** Return the minimum Java version required to use this JDK.
    * @return minimum version */
  public JavaVersion getMinimumMajorVersion() { return JavaVersion.JAVA_7; }
  
  /** Return the list of additional files required to use the compiler.
    * The compiler was found in the specified file. This method may have to search the user's hard drive, e.g.
    * by looking relative to compiler.getParentFile(), by checking environment variables, or by looking in
    * certain OS-specific directories.
    * @param compiler location where the compiler was fund
    * @return list of additional files that need to be available */
  public Iterable<File> getAdditionalCompilerFiles(File compiler) throws FileNotFoundException {
    if (compiler.equals(FileOps.getDrJavaFile())) {
      // all in one, don't need anything else
      return IterUtil.empty();
    }
    else {
      File parentDir = compiler.getParentFile();
      return IterUtil.make(Util.oneOf(parentDir, "scala-library.jar"),
                           Util.oneOf(parentDir, "scala-dbc.jar"),
                           Util.oneOf(parentDir, "scala-swing.jar"),
                           Util.oneOf(parentDir, "scalap.jar"),
                           Util.oneOf(parentDir, "jline.jar"));
    }
  }
  
  public String toString() { return getClass().getSimpleName()+" --> "+getAdapterForCompiler(); }
}
