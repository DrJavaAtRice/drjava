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
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.io.IOException;
import java.util.jar.JarFile;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.iter.IterUtil;

/** The description of the Mint compound JDK. */
public class MintDescriptor implements JDKDescriptor {
  public String getName() {
    return "Mint";
  }
  
  /** Packages to shadow when loading a new tools.jar.  If we don't shadow these classes, we won't
    * be able to load distinct versions for each tools.jar library.  These should be verified whenever
    * a new Java version is released.  (We can't just shadow *everything* because some classes, at 
    * least in OS X's classes.jar, can only be loaded by the JVM.)
    */
  public Set<String> getToolsPackages() {
    HashSet<String> set = new HashSet<String>();
    Collections.addAll(set, new String[] {
      // Additional from 6 tools.jar:
      "com.sun.codemodel",
        "com.sun.istack.internal.tools", // other istack packages are in rt.jar
        "com.sun.istack.internal.ws",
        "com.sun.source",
        "com.sun.xml.internal.dtdparser", // other xml.internal packages are in rt.jar
        "com.sun.xml.internal.rngom",
        "com.sun.xml.internal.xsom",
        "org.relaxng",
        
        // Mint:
        "com.sun.tools.javac",
        "com.sun.tools.javac.tree",
        "com.sun.tools.javac.comp",
        "com.sun.tools.javac.main",
        "edu.rice.cs.mint",
        "edu.rice.cs.mint.comp",
        "edu.rice.cs.mint.runtime",
        "edu.rice.cs.mint.runtime.mspTree"
    });
    return set;
  }

  public Iterable<File> getSearchDirectories() { return IterUtil.empty(); }
  public Iterable<File> getSearchFiles() {
    Iterable<File> files = IterUtil.asIterable(new File[] {
      new File("/C:/Program Files/JavaMint/langtools/dist/lib/classes.jar"),
        new File("/C:/Program Files/JavaMint/langtools/dist/lib/tools.jar"),
        new File("/usr/local/soylatte/lib/classes.jar"),
        new File("/usr/local/soylatte/lib/tools.jar"),
        new File("/usr/local/JavaMint/langtools/dist/lib/classes.jar"),
        new File("/usr/local/JavaMint/langtools/dist/lib/tools.jar")
    });
    try {
      String mint_home = System.getenv("MINT_HOME");
      if (mint_home!=null) {
        // JDKToolsLibrary.msg("MINT_HOME environment variable set to: "+mint_home);
        files = IterUtil.compose(files, new File(new File(mint_home), "langtools/dist/lib/classes.jar"));
        files = IterUtil.compose(files, new File(new File(mint_home), "langtools/dist/lib/tools.jar"));
      }
      else {
        // JDKToolsLibrary.msg("MINT_HOME not set");
      }
    }
    catch(Exception e) { /* ignore MINT_HOME variable */ }
    
    // drjava.jar file itself; check if it's a combined Mint/DrJava jar
    files = IterUtil.compose(files, edu.rice.cs.util.FileOps.getDrJavaFile()); 
    return files;
  }
  
  public boolean containsCompiler(File f) {
    if (f.isFile()) {
      try {
        JarFile jf = new JarFile(f);
        return (jf.getJarEntry("edu/rice/cs/mint/comp/TransStaging.class")!=null &&
                jf.getJarEntry("com/sun/source/tree/BracketExprTree.class")!=null &&
                jf.getJarEntry("com/sun/source/tree/BracketStatTree.class")!=null &&
                jf.getJarEntry("com/sun/source/tree/EscapeExprTree.class")!=null &&
                jf.getJarEntry("com/sun/source/tree/EscapeStatTree.class")!=null &&
                jf.getJarEntry("com/sun/tools/javac/util/DefaultFileManager.class")==null);
      }
      catch(IOException ioe) { return false; }
    }
    else if (f.isDirectory()) {
      return (new File(f,"edu/rice/cs/mint/comp/TransStaging.class").exists() &&
              new File(f,"com/sun/source/tree/BracketExprTree.class").exists() &&
              new File(f,"com/sun/source/tree/BracketStatTree.class").exists() &&
              new File(f,"com/sun/source/tree/EscapeExprTree.class").exists() &&
              new File(f,"com/sun/source/tree/EscapeStatTree.class").exists() &&
              new File(f,"com/sun/tools/javac/util/DefaultFileManager.class").exists());
    }
    return false;
  }
  
  public String getAdapterForCompiler() { return "edu.rice.cs.drjava.model.compiler.MintCompiler"; }
  public String getAdapterForDebugger() { return null; }
  
  public JavaVersion getMinimumMajorVersion() { return JavaVersion.JAVA_6; }
  
  public String toString() { return getClass().getSimpleName()+" --> "+getAdapterForCompiler(); }
}
