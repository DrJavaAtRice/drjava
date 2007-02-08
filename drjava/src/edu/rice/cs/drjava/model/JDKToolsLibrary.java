package edu.rice.cs.drjava.model;

import java.util.List;
import java.io.File;

import edu.rice.cs.plt.reflect.ReflectUtil;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.reflect.JavaVersion.FullVersion;
import edu.rice.cs.plt.reflect.ReflectException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;

import edu.rice.cs.drjava.model.compiler.CompilerInterface;
import edu.rice.cs.drjava.model.compiler.NoCompilerAvailable;
import edu.rice.cs.drjava.model.debug.Debugger;
import edu.rice.cs.drjava.model.debug.NoDebuggerAvailable;

public class JDKToolsLibrary {
  
  private final FullVersion _version;
  private final CompilerInterface _compiler;
  private final Debugger _debugger;
  
  protected JDKToolsLibrary(FullVersion version, CompilerInterface compiler, Debugger debugger) {
    _version = version;
    _compiler = compiler;
    _debugger = debugger;
  }
  
  public FullVersion version() { return _version; }
  
  public CompilerInterface compiler() { return _compiler; }
  
  public Debugger debugger() { return _debugger; }
  
  public boolean isValid() { return _compiler.isAvailable() || _debugger.isAvailable(); }
  
  public String toString() { return "JDK library " + version(); }
  
  protected static String adapterForCompiler(JavaVersion version) {
    switch (version) {
      case JAVA_6: return "edu.rice.cs.drjava.model.compiler.Javac160Compiler";
      case JAVA_5: return "edu.rice.cs.drjava.model.compiler.Javac150Compiler";
      case JAVA_1_4: return "edu.rice.cs.drjava.model.compiler.Javac141Compiler";
      default: return null;
    }
  }
  
  protected static String adapterForDebugger(JavaVersion version) {
    switch (version) {
      case JAVA_6: return "edu.rice.cs.drjava.model.debug.jpda.JPDADebugger";
      case JAVA_5: return "edu.rice.cs.drjava.model.debug.jpda.JPDADebugger";
      case JAVA_1_4: return "edu.rice.cs.drjava.model.debug.jpda.JPDADebugger";
      default: return null;
    }
  }
  
  public static JDKToolsLibrary makeFromRuntime(GlobalModel model) {
    FullVersion version = JavaVersion.CURRENT_FULL;

    CompilerInterface compiler = NoCompilerAvailable.ONLY;
    String compilerAdapter = adapterForCompiler(version.majorVersion());
    if (compilerAdapter != null) {
      List<File> bootClassPath = null;
      String bootProp = System.getProperty("sun.boot.class.path");
      if (bootProp != null) { bootClassPath = IterUtil.asList(IOUtil.parsePath(bootProp)); }
      try {
        CompilerInterface attempt = (CompilerInterface) 
          ReflectUtil.loadObject(compilerAdapter, new Class[]{FullVersion.class, List.class}, version, bootClassPath);
        if (attempt.isAvailable()) { compiler = attempt; }
      }
      catch (ReflectException e) { /* can't load */ }
      catch (LinkageError e) { /* can't load */ }
    }
    
    Debugger debugger = NoDebuggerAvailable.ONLY;
    String debuggerAdapter = adapterForDebugger(version.majorVersion());
    if (debuggerAdapter != null) {
      try {
        Debugger attempt = (Debugger) ReflectUtil.loadObject(debuggerAdapter, new Class[]{GlobalModel.class}, model);
        if (attempt.isAvailable()) { debugger = attempt; }
      }
      catch (ReflectException e) { /* can't load */ }
      catch (LinkageError e) { /* can't load */ }
    }
    
    return new JDKToolsLibrary(version, compiler, debugger);
  }
  
}
