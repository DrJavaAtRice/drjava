package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

import edu.rice.cs.util.classloader.ToolsJarClassLoader;

/**
 * A compiler interface to search a given 
 * @version $Id$
 */
public class JavacFromToolsJar extends CompilerProxy {
  public static final CompilerInterface ONLY = new JavacFromToolsJar();

  /** Private constructor due to singleton. */
  private JavacFromToolsJar() {
    super("edu.rice.cs.drjava.model.compiler.JavacGJCompiler",
          new ToolsJarClassLoader());
  }

  /**
   * Returns the name of this compiler, appropriate to show to the user.
   */
  public String getName() {
    return super.getName() + " (tools.jar)";
  }
}



