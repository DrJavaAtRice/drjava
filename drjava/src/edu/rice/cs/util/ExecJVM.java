/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

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
  public static Process runJVMPropogateClassPath(String mainClass,
                                                 String[] classParams)
    throws IOException
  {
    return runJVMPropogateClassPath(mainClass, classParams, new String[0]);
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

    //for (int i = 0; i < argArray.length; i++) {
      //System.err.println("arg #" + i + ": " + argArray[i]);
    //}

    return Runtime.getRuntime().exec(argArray);
  }
    
  private static void _addArray(LinkedList list, Object[] array) {
    if (array != null) {
      for (int i = 0; i < array.length; i++) {  
        list.add(array[i]);
      }
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
    // this netware thing is based on comments from ant's code
    if (_isNetware()) {
      return "java";
    }
    
    File executable;

    String java_home = System.getProperty("java.home") + "/";
    
    String[] candidates = {
      java_home + "../bin/java",
      java_home + "bin/java",
      java_home + "java",
    };

    // search all the candidates to find java
    for (int i = 0; i < candidates.length; i++) {
      String current = candidates[i];

      // try javaw.exe first for dos, otherwise try java.exe for dos
      if (_isDOS()) {
        executable = new File(current + "w.exe");
        if (! executable.exists()) {
          executable = new File(current + ".exe");
        }
      }
      else {
        executable = new File(current);
      }

      //System.err.println("checking: " + executable);

      if (executable.exists()) {
        //System.err.println("JVM executable found: " + executable.getAbsolutePath());
        return executable.getAbsolutePath();
      }
    }

    // hope for the best using the system's path!
    //System.err.println("Could not find java executable, using 'java'!");
    return "java";
  }
}
  
