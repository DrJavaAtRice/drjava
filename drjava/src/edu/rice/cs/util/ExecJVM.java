package edu.rice.cs.util;

import java.io.*;
import java.util.*;

/**
 * A utility class to allow executing another JVM.
 *
 * @version $Id$
 */
public final class ExecJVM {
  private static final String PATH_SEPARATOR = System.getProperty("path.separator");
  private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);

  private ExecJVM() {}
  
  /**
   * Runs a new JVM.
   * 
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param classPath Array of items to put in classpath of new JVM
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass,
                               String[] classParams,
                               String[] classPath,
                               String[] jvmParams) throws IOException
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < classPath.length; i++) {
      if (i != 0) {
        buf.append(PATH_SEPARATOR);
      }
      
      buf.append(classPath[i]);
    }

    return runJVM(mainClass, classParams, buf.toString(), jvmParams);
  }

  /**
   * Runs a new JVM.
   * 
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param classPath Pre-formatted classpath parameter
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass,
                               String[] classParams,
                               String classPath,
                               String[] jvmParams) throws IOException
  {
    LinkedList args = new LinkedList();
    args.add("-classpath");
    args.add(classPath);
    _addArray(args, jvmParams);    
    String[] jvmWithCP = (String[]) args.toArray(new String[0]);
    
    return runJVM(mainClass, classParams, jvmWithCP);
  }

  /**
   * Runs a new JVM, propogating the present classpath.
   * 
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVMPropogateClassPath(String mainClass,
                                                 String[] classParams,
                                                 String[] jvmParams)
    throws IOException
  {
    String cp = System.getProperty("java.class.path");
    return runJVM(mainClass, classParams, cp, jvmParams);
  }
  
  /**
   * Runs a new JVM, propogating the present classpath.
   * 
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass, String[] classParams)
    throws IOException
  {
    return runJVM(mainClass, classParams, new String[0]);
  }
  
  /**
   * Runs a new JVM.
   * 
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass,
                               String[] classParams,
                               String[] jvmParams) throws IOException
  {
    LinkedList args = new LinkedList();
    args.add(_getExecutable());
    _addArray(args, jvmParams);
    args.add(mainClass);
    _addArray(args, classParams);

    String[] argArray = (String[]) args.toArray(new String[0]);
    return Runtime.getRuntime().exec(argArray);
  }
    
  private static void _addArray(LinkedList list, Object[] array) {
    if (array != null) {
      for (int i = 0; i < array.length; i++) {  
        list.add(array[i]);
      }
    }
  }
  
  private static String _getExecExtension() {
    if (_isDOS()) {
      return ".exe";
    }
    else {
      return "";
    }
  }

  /** DOS/Windows family OS's use ; to separate paths. */
  private static boolean _isDOS() {
    return PATH_SEPARATOR.equals(";");
  }
  
  private static boolean _isNetware() {
    return OS_NAME.indexOf("netware") != -1;
  }

  /**
   * Find the java executable.
   * This logic comes from Ant.
   */
  private static String _getExecutable() {
    File executable = new File(System.getProperty("java.home") +
                               "/../bin/java" + _getExecExtension() );
    
    // this netware thing is based on comments from ant's code
    if (executable.exists() && ! _isNetware()) {
      return executable.getAbsolutePath();
    }
    else {
      // hope for the best using the system's path!
      return "java";
    }
  }
}
  
