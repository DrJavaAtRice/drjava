package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.net.URL;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.classloader.StickyClassLoader;

/**
 * A compiler interface to search a given 
 * @version $Id$
 */
public class CompilerProxy implements CompilerInterface {
  /**
   * The actual compiler interface. If it's null, we couldn't load it.
   */
  private CompilerInterface _realCompiler = null;

  /**
   * These classes will always be loaded using the previous classloader.
   * This is important to make sure there is only one instance of them, so
   * their values can be freely passed about the program.
   */
  private static final String[] _useOldLoader = {
    "edu.rice.cs.drjava.model.compiler.CompilerInterface",
    "edu.rice.cs.drjava.model.compiler.CompilerError"
  };

  /**
   * A proxy compiler interface that tries to load the given class
   * from one of the given locations. It uses its own classloader, which will
   * even allow loading a second instance of the class!
   *
   * @param className Implementation of {@link CompilerInterface} to proxy for.
   * @param loader Classloader to use
   */

  public CompilerProxy(String className,
                       ClassLoader newLoader)
  {
    StickyClassLoader loader =
      new StickyClassLoader(newLoader,
                            getClass().getClassLoader(),
                            _useOldLoader);

    try {
      Class c = loader.loadClass(className);
      _realCompiler = CompilerRegistry.createCompiler(c);
      //DrJava.consoleErr().println("real compiler: " + _realCompiler + " this: " + this);
    }
    catch (Throwable t) {
      // don't do anything. realCompiler stays null.
      //DrJava.consoleErr().println("loadClass fails: " + t);
      //t.printStackTrace(DrJava.consoleErr());
    }
  }


  /**
   * Compile the given files.
   * @param files Source files to compile.
   * @param sourceRoot Source root directory, the base of the package structure.
   *
   * @return Array of errors that occurred. If no errors, should be zero
   * length array (not null).
   */
  public CompilerError[] compile(File sourceRoot, File[] files) {
    //DrJava.consoleErr().println("proxy to compile: " + files[0]);

    return _realCompiler.compile(sourceRoot, files);
  }

  /**
   * Indicates whether this compiler is actually available.
   * As in: Is it installed and located?
   * This method should load the compiler class, which should
   * hopefully prove whether the class can load.
   * If this method returns true, the {@link #compile} method
   * should not fail due to class not being found.
   */
  public boolean isAvailable() {
    if (_realCompiler == null) {
      return false;
    }
    else {
      return _realCompiler.isAvailable();
    }
  }

  /**
   * Returns the name of this compiler, appropriate to show to the user.
   */
  public String getName() {
    if (!isAvailable()) {
      return "(unavailable)";
    }

    return _realCompiler.getName();
  }

  /** Should return info about compiler, at least including name. */
  public String toString() {
    return getName();
  }
}



