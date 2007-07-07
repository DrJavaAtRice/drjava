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
import edu.rice.cs.drjava.model.javadoc.JavadocModel;
import edu.rice.cs.drjava.model.javadoc.NoJavadocAvailable;
import edu.rice.cs.drjava.model.javadoc.DefaultJavadocModel;

/** The class that provides methods for interfacing to facilities in tools.jar that do not have published interfaces
  * (or did not have published interfaces at the time DrJava was originally written). 
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
  
  protected static String compilerMainClass(JavaVersion version) {
    switch (version) {
      case JAVA_6: return "com.sun.tools.javac.main.JavaCompiler";
      case JAVA_5: return "com.sun.tools.javac.main.JavaCompiler";
      case JAVA_1_4: return "com.sun.tools.javac.v7.JavaCompiler";
      default: return null;
    }
  } 
  public static JDKToolsLibrary makeFromRuntime(GlobalModel model) {
    FullVersion version = JavaVersion.CURRENT_FULL;
    CompilerInterface compiler = NoCompilerAvailable.ONLY;
    String compilerAdapter = null;
    
    // force DrJava to try to load the javac main class;
    // if tools.jar isn't on the class path, this will fail
    // having tools.jar on the class path is not sufficient
    try {
      ReflectUtil.class.getClassLoader().loadClass(compilerMainClass(JavaVersion.CURRENT));
      compilerAdapter = adapterForCompiler(version.majorVersion());
    }
    catch(ClassNotFoundException e) { /* keep compilerAdapter == null */ }
    catch(UnsupportedClassVersionError e) { /* keep compilerAdapter == null */ }
    catch(RuntimeException e) { /* keep compilerAdapter == null */ }  

    if (compilerAdapter != null) {
      List<File> bootClassPath = null;
      String bootProp = System.getProperty("sun.boot.class.path");
      if (bootProp != null) { bootClassPath = IterUtil.asList(IOUtil.parsePath(bootProp)); }
      try {
        Class[] sig = new Class[]{ FullVersion.class, String.class, List.class };
        Object[] args = new Object[]{ version, "the runtime class path", bootClassPath };
        CompilerInterface attempt = (CompilerInterface) ReflectUtil.loadObject(compilerAdapter, sig, args);
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
    
    JavadocModel javadoc = new NoJavadocAvailable(model);
    try {
      Class.forName("com.sun.tools.javadoc.Main");
      javadoc = new DefaultJavadocModel(model, null, GlobalModel.RUNTIME_CLASS_PATH);
    }
    catch (ClassNotFoundException e) { /* can't load */ }
    catch (LinkageError e) { /* can't load (probably not necessary, but might as well catch it) */ }
    
    return new JDKToolsLibrary(version, compiler, debugger, javadoc);
  }
  
}
