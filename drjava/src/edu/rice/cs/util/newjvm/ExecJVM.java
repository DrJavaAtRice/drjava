/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import edu.rice.cs.drjava.config.FileOption;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/** A utility class to allow executing another JVM.
 *  @version $Id$
 */
public final class ExecJVM {
  private static final String PATH_SEPARATOR = System.getProperty("path.separator");
  private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US);

  private ExecJVM() { }

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
  public static Process runJVM(String mainClass, String[] classParams, String[] classPath, String[] jvmParams, File workDir)
    throws IOException {

    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < classPath.length; i++) {
      if (i != 0) buf.append(PATH_SEPARATOR);

      buf.append(classPath[i]);
    }

    return runJVM(mainClass, classParams, buf.toString(), jvmParams, workDir);
  }

  /* REPAIRING working directory behavior for File I/O.
   * 1.  We need to restart the JVM if path for working directory does not equal system property "user.dir" (which version of getPath ?)
   * 2.  We need to add a File argument to some or all our calls to runJVM and to invokeSlave.  The corresponding actual parameter is
   *     DrJava.getConfig().getSetting(OptionConstants.WORKING_DIRECTORY).
   * 3.  Questions:
   *     a.  Is running javadoc effected?  What directory should we run this command within?
   *     b.  What working directory should we use with projects?  (I think it should be a project property that overrides the corresponding
   *         IDE property.  If so what should the default value be?  The project root?  I think this means that closing a project resets
   *         the working directory.  Hence, we need to reset interactions when we close a project.)  We can punt on what to do for projects
   *         in our first cut.
   */

  /** Runs a new JVM.
   *
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param classPath Pre-formatted classpath parameter
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVM(String mainClass, String[] classParams, String classPath, String[] jvmParams, File workDir)
    throws IOException {

    LinkedList<String> args = new LinkedList<String>();
    args.add("-classpath");
    args.add(edu.rice.cs.util.FileOps.convertToAbsolutePathEntries(classPath));
    _addArray(args, jvmParams);
    String[] jvmWithCP = args.toArray(new String[args.size()]);

    return _runJVM(mainClass, classParams, jvmWithCP, workDir);
  }

  /**
   * Runs a new JVM, propogating the present classpath.
   * It does change the entries of the current class path to global paths, though.
   *
   * @param mainClass Class to run
   * @param classParams Parameters to pass to the main class
   * @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   * @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVMPropagateClassPath(String mainClass, String[] classParams, String[] jvmParams, File workDir)
    throws IOException {
    String cp = System.getProperty("java.class.path");
    return runJVM(mainClass, classParams, cp, jvmParams, workDir);
  }

  /** Runs a new JVM, propogating the present classpath.
   *
   *  @param mainClass Class to run
   *  @param classParams Parameters to pass to the main class
   *  @return {@link Process} object corresponding to the executed JVM
   */
  public static Process runJVMPropagateClassPath(String mainClass, String[] classParams, File workDir)
    throws IOException {
    return runJVMPropagateClassPath(mainClass, classParams, new String[0], workDir);
  }

  /** Creates and runs a new JVM.  This method is private now because it cannot change the classpath entries to 
   *  absolute paths, so it should not be used.
   *
   *  @param mainClass Class to run
   *  @param classParams Parameters to pass to the main class
   *  @param jvmParams Array of additional command-line parameters to pass to JVM
   *
   *  @return {@link Process} object corresponding to the executed JVM
   */
  private static Process _runJVM(String mainClass, String[] classParams, String[] jvmParams, File workDir) throws IOException {
    LinkedList<String> args = new LinkedList<String>();
    args.add(_getExecutable());
    _addArray(args, jvmParams);
    args.add(mainClass);
    _addArray(args, classParams);

    String[] argArray = args.toArray(new String[args.size()]);

    // exec our "java" command in the specified working directory setting
    Process p;
    if ((workDir != null) && (workDir != FileOption.NULL_FILE)) {
      // execute in the working directory
      if (workDir.exists()) p = Runtime.getRuntime().exec(argArray, null, workDir);
      else {
        edu.rice.cs.util.swing.Utilities.showMessageBox("Working directory does not exist:\n" + workDir +
                                                        "\nThe setting will be ignored. Press OK to continue.",
                                                        "Configuration Error");
        p = Runtime.getRuntime().exec(argArray);
      }
    }
    else {
      // execute without caring about working directory
      p = Runtime.getRuntime().exec(argArray);
    }
    return p;
  }

  /** Empties BufferedReaders by copying lines into LinkedLists.
   *  This is intended for use with the output streams from an ExecJVM process.
   *  Source and destination objects are specified for stdout and for stderr.
   *  @param theProc a Process object whose output will be handled
   *  @param outLines the LinkedList of Strings to be filled with the lines read from outBuf
   *  @param errLines the LinkedList of Strings to be filled with the lines read from errBuf
   */
  public static void ventBuffers(Process theProc, LinkedList<String> outLines,
                                 LinkedList<String> errLines) throws IOException {
    // getInputStream actually gives us the stdout from the Process.
    BufferedReader outBuf = new BufferedReader(new InputStreamReader(theProc.getInputStream()));
    BufferedReader errBuf = new BufferedReader(new InputStreamReader(theProc.getErrorStream()));
    String output;

    if (outBuf.ready()) {
      output = outBuf.readLine();

      while (output != null) {
        //        System.out.println("[stdout]: " + output);
        outLines.add(output);
        if (outBuf.ready()) output = outBuf.readLine();
        else output = null;
      }
    }
    outBuf.close();

    if (errBuf.ready()) {
      output = errBuf.readLine();
      while (output != null) {
        //        System.out.println("[stderr] " + output);
        errLines.add(output);
        if (errBuf.ready()) {
          output = errBuf.readLine();
        }
        else {
          output = null;
        }
      }
    }
    errBuf.close();
  }

  /**
   * Prints the stdout and stderr of the given process, line by line.  Adds a
   * message and tag to identify the source of the output.  Note that this code
   * will print all available stdout before all stderr, since it is impossible
   * to determine in which order lines were added to the respective buffers.
   * @param theProc a Process object whose output will be handled
   * @param msg an initial message to print before output
   * @param sourceName a short string to identify the process
   * @throws IOException if there is a problem with the streams
   */
  public static void printOutput(Process theProc, String msg, String sourceName)
    throws IOException {
    // First, write out our opening message.
    System.out.println(msg);

    LinkedList<String> outLines = new LinkedList<String>();
    LinkedList<String> errLines = new LinkedList<String>();

    ventBuffers(theProc, outLines, errLines);

    Iterator<String> it = outLines.iterator();
    String output;
    while (it.hasNext()) {
      output = it.next();
      System.out.println("    [" +sourceName + " stdout]: " + output);
    }

    it = errLines.iterator();
    while (it.hasNext()) {
      output = it.next();
      System.out.println("    [" +sourceName + " stderr]: " + output);
    }
  }

  private static void _addArray(LinkedList<String> list, String[] array) {
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
    if (_isNetware()) return "java";

    File executable;

    String java_home = System.getProperty("java.home") + "/";

    String[] candidates = { java_home + "../bin/java", java_home + "bin/java", java_home + "java", };

    // search all the candidates to find java
    for (int i = 0; i < candidates.length; i++) {
      String current = candidates[i];

      // try javaw.exe first for dos, otherwise try java.exe for dos
      if (_isDOS()) {
        executable = new File(current + "w.exe");
        if (! executable.exists())  executable = new File(current + ".exe");
      }
      else executable = new File(current);

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

