/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2009, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import java.util.List;
import java.io.File;

import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.JavaVersion.FullVersion;
import edu.rice.cs.plt.reflect.ReflectException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.collect.CollectUtil;

import edu.rice.cs.drjava.model.compiler.CompilerInterface;
import edu.rice.cs.drjava.model.compiler.NoCompilerAvailable;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.NoDebuggerAvailable;
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.model.javadoc.NoJavadocAvailable;
import edu.rice.cs.drjava.model.javadoc.DefaultJavadocModel;

/** 
 * Provides dynamic access to the interface of a JDK's tools.jar classes.  This level of indirection
 * eliminates the need to have specific tools.jar classes available statically (and the resulting need
 * to reset the JVM if they are not), and makes it possible to interface with multiple tools.jar
 * libraries simultaneously.
 */
public class JDKToolsLibrary {
  
  private final FullVersion _version;
  private final CompilerInterface _compiler;
  private final Debugger _debugger;
  private final JavadocModel _javadoc;
  
  protected JDKToolsLibrary(FullVersion version, CompilerInterface compiler, Debugger debugger,
                            JavadocModel javadoc) {
    _version = version;
    _compiler = compiler;
    _debugger = debugger;
    _javadoc = javadoc;
  }
  
  public FullVersion version() { return _version; }
  
  public CompilerInterface compiler() { return _compiler; }
  
  public Debugger debugger() { return _debugger; }
  
  public JavadocModel javadoc() { return _javadoc; }
  
  public boolean isValid() {
    return _compiler.isAvailable() || _debugger.isAvailable() || _javadoc.isAvailable();
  }
  
  public String toString() { return "JDK library " + _version.versionString(); }
  
  protected static String adapterForCompiler(JavaVersion.FullVersion version) {
    switch (version.majorVersion()) {
      case JAVA_6: {
        switch (version.vendor()) {
          case OPENJDK: return "edu.rice.cs.drjava.model.compiler.Javac160OpenJDKCompiler";
          case MINT: return "edu.rice.cs.drjava.model.compiler.MintCompiler";
          default: return "edu.rice.cs.drjava.model.compiler.Javac160Compiler";
        }
      }
      case JAVA_5: return "edu.rice.cs.drjava.model.compiler.Javac150Compiler";
      case JAVA_1_4: return "edu.rice.cs.drjava.model.compiler.Javac141Compiler";
      default: return null;
    }
  }
  
  protected static String adapterForDebugger(JavaVersion.FullVersion version) {
    switch (version.majorVersion()) {
      case JAVA_6: return "edu.rice.cs.drjava.model.debug.jpda.JPDADebugger";
      case JAVA_5: return "edu.rice.cs.drjava.model.debug.jpda.JPDADebugger";
      case JAVA_1_4: return "edu.rice.cs.drjava.model.debug.jpda.JPDADebugger";
      default: return null;
    }
  }
  
  /** Create a JDKToolsLibrary from the runtime class path (or, more accurately, from the class
   * loader that loaded this class.
   */
  public static JDKToolsLibrary makeFromRuntime(GlobalModel model) {
    FullVersion version = JavaVersion.CURRENT_FULL;

    CompilerInterface compiler = NoCompilerAvailable.ONLY;
    String compilerAdapter = adapterForCompiler(version);
    if (compilerAdapter != null) {
      List<File> bootClassPath = null;
      String bootProp = System.getProperty("sun.boot.class.path");
      if (bootProp != null) { bootClassPath = CollectUtil.makeList(IOUtil.parsePath(bootProp)); }
      try {
        Class<?>[] sig = { FullVersion.class, String.class, List.class };
        Object[] args = { version, "the runtime class path", bootClassPath };
        CompilerInterface attempt = (CompilerInterface) ReflectUtil.loadObject(compilerAdapter, sig, args);
        if (attempt.isAvailable()) { compiler = attempt; }
      }
      catch (ReflectException e) { /* can't load */ }
      catch (LinkageError e) { /* can't load */ }
    }
    
    Debugger debugger = NoDebuggerAvailable.ONLY;
    String debuggerAdapter = adapterForDebugger(version);
    if (debuggerAdapter != null) {
      try {
        Debugger attempt = (Debugger) ReflectUtil.loadObject(debuggerAdapter, new Class<?>[]{GlobalModel.class}, model);
        if (attempt.isAvailable()) { debugger = attempt; }
      }
      catch (ReflectException e) { /* can't load */ }
      catch (LinkageError e) { /* can't load */ }
    }
    
    JavadocModel javadoc = new NoJavadocAvailable(model);
    try {
      Class.forName("com.sun.tools.javadoc.Main");
      javadoc = new DefaultJavadocModel(model, null, ReflectUtil.SYSTEM_CLASS_PATH);
    }
    catch (ClassNotFoundException e) { /* can't load */ }
    catch (LinkageError e) { /* can't load (probably not necessary, but might as well catch it) */ }
    
    return new JDKToolsLibrary(version, compiler, debugger, javadoc);
  }
  
}
