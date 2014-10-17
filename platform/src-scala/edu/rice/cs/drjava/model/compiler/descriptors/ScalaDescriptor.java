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
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
  
  /** Packages to shadow when loading a new scala-compiler.jar.  If we don't shadow these classes, we won't
    * be able to load distinct versions for each scala-compiler.jar library.  These should be verified whenever
    * a new Scala version is released.  (We can't just shadow *everything* because some classes, at 
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
      // TBA
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
    /* Code that looks for compiler outside of drjava.jar */
    Iterable<File> files = IterUtil.asIterable(new File[] {
      new File("/C:/Scala/scala-2.9.1.final/lib/scala-compiler.jar")  //TODO make this more robust so it does not depend on the particular version
    });
    files = IterUtil.compose(files, new File("/opt/scala/lib/scala-compiler.jar"));
    files = IterUtil.compose(files, new File("/usr/share/scala/lib/scala-compiler.jar"));
    files = IterUtil.compose(files, new File("/usr/share/java/scala-compiler-2.9.2.jar"));
    // If executable is class file tree, looks for compiler in associated lib directory
    File f = FileOps.getDrJavaFile();
    if (! f.isFile()) { // f is a directory
      File grandParentFile = f.getParentFile().getParentFile();
      files = IterUtil.compose(files, new File(grandParentFile, "lib/scala-compiler.jar"));
    }
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
    
    // add drjava.jar file itself
    files = IterUtil.compose(edu.rice.cs.util.FileOps.getDrJavaFile(), files); 
    JDKToolsLibrary.msg("ScalaDescriptor.getSearchFiles is returning " + files);
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
    * Note that this is the Java version on which this Scala compiler is based, not the internal compiler version.
    * For a Scala enhanced JDK, this is equal to the version of the full JDK that the compound JDK needs, For Scala 2.9.*,
    * this corresponding version of Java is Java 7.
    * @return guessed version */
  public JavaVersion.FullVersion guessVersion(File f) {
      return JavaVersion.parseFullVersion(JavaVersion.JAVA_6.fullVersion().versionString(), /* TODO: add SCALA to ENUM definition */
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
  public JavaVersion getMinimumMajorVersion() { return JavaVersion.JAVA_5; }
  
  /** Return the list of additional files required to use the compiler.
    * The compiler was found in the specified file. This method may have to search the user's hard drive, e.g.
    * by looking relative to compiler.getParentFile(), by checking environment variables, or by looking in
    * certain OS-specific directories.
    * @param compiler location where the compiler was foUund
    * @return list of additional files that need to be available */
  public Iterable<File> getAdditionalCompilerFiles(File compiler) throws FileNotFoundException {
    if (compiler.equals(FileOps.getDrJavaFile())) {
      // all in one, don't need anything else
      return IterUtil.empty();
    }
    else {
      File parentDir = compiler.getParentFile();
      File[] jars = parentDir.listFiles(new FilenameFilter() {
        public boolean accept(File parent, String name) { return name.endsWith(".jar"); }
      });
      return Arrays.asList(jars);                                      
                                        
//      return IterUtil.make(Util.oneOf(parentDir, "scala-library.jar"),
//                           Util.oneOf(parentDir, "scala-dbc.jar"),
//                           Util.oneOf(parentDir, "scala-swing.jar"),
//                           Util.oneOf(parentDir, "scalap.jar"),
//                           Util.oneOf(parentDir, "jline.jar"));
    }
  }
  
  public String toString() { return getClass().getSimpleName() + " --> " + getAdapterForCompiler(); }
}
