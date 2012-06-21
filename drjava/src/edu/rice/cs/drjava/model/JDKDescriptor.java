/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.Collections;

import edu.rice.cs.drjava.model.JDKToolsLibrary;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.JavaVersion.FullVersion;
import edu.rice.cs.plt.iter.IterUtil;

/** A description of a JDK.
  * Put subclasses of JDKDescriptor in the edu.rice.cs.drjava.model.compiler.descriptors package for DrJava
  * to find. */
public abstract class JDKDescriptor {
  /** Return the name of this JDK.
    * @return name */
  public abstract String getName();
  
  /** Packages to shadow when loading a new tools.jar.  If we don't shadow these classes, we won't
    * be able to load distinct versions for each tools.jar library.  These should be verified whenever
    * a new Java version is released.  (We can't just shadow *everything* because some classes, at 
    * least in OS X's classes.jar, can only be loaded by the JVM.)
    * 
    * @return set of packages that need to be shadowed
    */
  public abstract Set<String> getToolsPackages();

  /** Returns a list of directories that should be searched for tools.jar and classes.jar files.
    * @return list of directories to search */
  public abstract Iterable<File> getSearchDirectories();
  
  /** Returns a list of files that should be searched if they contain a compiler.
    * @return list of files to search */
  public abstract Iterable<File> getSearchFiles();
  
  /** True if this is a compound JDK and needs a fully featured JDK to operate.
    * @return true if compound JDK (e.g. NextGen, Mint, Habanero). */
  public abstract boolean isCompound();
  
  /** True if this is a JDK that can serve as base for a compound JDK.
    * @return true if base for a compound JDK (e.g. Sun JDK, OpenJDK, AppleJDK). */
  public boolean isBaseForCompound() { return false; }
  
  /** Return the class name of the compiler adapter.
    * @return class name of compiler, or null if no compiler */
  public abstract String getAdapterForCompiler();

  /** Return the class name of the compiler adapter.
    * @param guessedVersion the guessed version of the compiler
    * @return class name of compiler, or null if no compiler */
  public String getAdapterForCompiler(JavaVersion.FullVersion guessedVersion) {
    return getAdapterForCompiler(); // ignore the version
  }
  
  /** Return the class name of the debugger adapter.
    * @return class name of debugger, or null if no debugger */
  public abstract String getAdapterForDebugger();

  /** Return the class name of the debugger adapter.
    * @param guessedVersion the guessed version of the compiler
    * @return class name of debugger, or null if no debugger */
  public String getAdapterForDebugger(JavaVersion.FullVersion guessedVersion) {
    return getAdapterForDebugger(); // ignore version
  }
  
  /** Return true if the file (jar file or directory) contains the compiler.
    * @return true if the file contains the compiler */
  public abstract boolean containsCompiler(File f);
  
  /** Return the guessed version for the compiler in the specified file (jar file or directory).
    * Note that this is the Java version that this compiler is compatible to, not the internal compiler version.
    * For full (non-compound) JDKs, this is equal to the version, i.e. JDK6 should guess Java 6.0.
    * For compound JDKs, this is equal to the version of the full JDK that the compound JDK needs, i.e.
    * if a version of the HJ compiler requires JDK6, it should guess JDK6.
    * @return guessed version */
  public JavaVersion.FullVersion guessVersion(File f) { return JarJDKToolsLibrary.guessVersion(f, this); }

  /** Return the minimum Java version required to use this JDK.
    * @return minimum version */
  public abstract JavaVersion getMinimumMajorVersion();
  
  /** Return the list of additional files required to use the compiler.
    * The compiler was found in the specified file. This method may have to search the user's hard drive, e.g.
    * by looking relative to compiler.getParentFile(), by checking environment variables, or by looking in
    * certain OS-specific directories.
    * 
    * // for example:
    * public Iterable<File> getAdditionalCompilerFiles(File compiler) throws FileNotFoundException {
    *   File parent = compiler.getParentFile();
    *   File nextgen2orgjar = new File(parent, "nextgen2org.jar");
    *   if (!Util.exists(nextgen2orgjar,
    *                    "org/apache/bcel/classfile/Node.class")) {
    *     throw new FileNotFoundException("org/apache/bcel/classfile/Node.class");
    *   }
    *   return IterUtil.singleton(nextgen2orgjar);
    * }
    * 
    * @param compiler location where the compiler was fund
    * @return list of additional files that need to be available */
  public abstract Iterable<File> getAdditionalCompilerFiles(File compiler) throws FileNotFoundException;

  /** Return a description of this JDK.
    * @param version the specific version of the compiler
    * @return description */
  public String getDescription(JavaVersion.FullVersion version) {
    return getName() + " library " + version.versionString();
  }
  
  public String toString() {
    return this.getClass().getName()+": "+getName();
  }
  
  /** Singleton representing a JDK that doesn't have a descriptor. */
  public static final JDKDescriptor NONE = new None();
    
  /** Class for the singleton representing a JDK that doesn't have a descriptor. */
  private static final class None extends JDKDescriptor {
    public String getName() { return "none"; }
    public String getDescription(JavaVersion.FullVersion version) {
      switch (version.vendor()) {
        case ORACLE:
          return "Oracle JDK library " + version.toString();
        case OPENJDK:
          return "OpenJDK library " + version.toString();
        case APPLE:
          return "Apple JDK library " + version.toString();
        default:
          return "JDK library " + version.toString();
      }
    }
    public Set<String> getToolsPackages() { return Collections.emptySet(); }
    public Iterable<File> getSearchDirectories() { return IterUtil.empty(); }  
    public Iterable<File> getSearchFiles() { return IterUtil.empty(); }
    public boolean isCompound() { return false; }
    public boolean isBaseForCompound() { return true; }
    public String getAdapterForCompiler() { return ""; }
    public String getAdapterForCompiler(JavaVersion.FullVersion guessedVersion) {
      return JDKToolsLibrary.adapterForCompiler(guessedVersion);
    }
    public String getAdapterForDebugger() { return ""; }
    public String getAdapterForDebugger(JavaVersion.FullVersion guessedVersion) {
      return JDKToolsLibrary.adapterForDebugger(guessedVersion);
    }
    public boolean containsCompiler(File f) { return true; }
    public JavaVersion getMinimumMajorVersion() { return JavaVersion.JAVA_1_1; }
    public Iterable<File> getAdditionalCompilerFiles(File compiler) throws FileNotFoundException {
      return IterUtil.empty();
    }
  }
  
  /** Utilities for JDK descriptors. */
  public static class Util {
    /** Return true if the file names exist in the specified file, which can either be a directory or jar file.
      * @param jarOrDir jar file or directory
      * @param fileNames file names that need to exist
      * @return true if all file names are found */
    public static boolean exists(File jarOrDir, String... fileNames) {
      if (jarOrDir.isFile()) {
        try {
          JarFile jf = new JarFile(jarOrDir);
          for(String fn: fileNames) {
            if (jf.getJarEntry(fn)==null) return false;
          }
          return true;
        }
        catch(IOException ioe) { return false; }
      }
      else if (jarOrDir.isDirectory()) {
        for(String fn: fileNames) {
          if (! (new File(jarOrDir,fn).exists())) return false;
        }
        return true;
      }
      return false;
    }
   
    /** Return the first of the file names that exists in the specified directory.
      * Throws FileNotFoundException if none of them exists.
      * @param jarOrDir jar file or directory
      * @param fileNames file names that need to exist
      * @return file name if found, or null
      * @throws FileNotFoundException if none of them exists.*/
    public static File oneOf(File dir, String... fileNames) throws FileNotFoundException {
      if (dir.isDirectory()) {
        for(String fn: fileNames) {
          File f = new File(dir,fn);
          if (f.exists()) return f;
        }
        throw new FileNotFoundException("None of "+IterUtil.toString(IterUtil.make(fileNames), "", ", ", "")+
                                        " found in "+dir);
      }
      throw new FileNotFoundException(dir+" is not a directory");
    }
  }
}
