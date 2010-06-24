/*BEGIN_COPYRIGHT_BLOCK
*
* This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
* or http://sourceforge.net/projects/drjava/
*
* DrJava Open Source License
* 
* Copyright (C) 2001-2010 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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
*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.util.MissingResourceException;

import java.io.*;

import java.util.Arrays;
import java.util.Map;
import java.util.LinkedList;
import java.util.Iterator;

// DJError class is not in the same package as this
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.ArgumentTokenizer;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** An implementation of JavacCompiler that supports compiling with the Java 1.6.0/Nextgen compiler.
  * Must be compiled using javac 1.6.0 and with Nextgen on the boot classpath.
  *
  * @version $Id$
  */
public class NextGenCompiler extends Javac160FilteringCompiler {
  public NextGenCompiler(JavaVersion.FullVersion version,
                         String location,
                         java.util.List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }

  public String getName() { return "Nextgen " + _version.versionString(); }
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. This is necessary for the Nextgen compiler,
    * since the Nextgen compiler needs to be invoked at runtime. */
  public java.util.List<File> additionalBootClassPathForInteractions() {
    return Arrays.asList(new File(_location));
  }

  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * transformCommands should support at least "run", "java" and "applet".
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
    if (interactionsString.startsWith("applet ")) {
      throw new RuntimeException("Applets not supported by Nextgen.");
    }
    if (interactionsString.startsWith("run ") ||
        interactionsString.startsWith("applet ") ||
        interactionsString.startsWith("nextgen ") ||
        interactionsString.startsWith("java ")) interactionsString = _transformNextgenCommand(interactionsString);
    return interactionsString;    
  }
  
  protected static String _transformNextgenCommand(String s) {
    final String command = "edu.rice.cs.nextgen2.classloader.Runner.main(\"{0}\"{1});";
    if (s.endsWith(";"))  s = _deleteSemiColon(s);
    java.util.List<String> args = ArgumentTokenizer.tokenize(s, true);
    final String classNameWithQuotes = args.get(1); // this is "MyClass"
    final String className = classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
    final StringBuilder argsString = new StringBuilder();
    for (int i = 2; i < args.size(); i++) {
      argsString.append(",");
      argsString.append(args.get(i));
    }
    return java.text.MessageFormat.format(command, className, argsString.toString());
  }

  public boolean isAvailable() {
    try {
      // Diagnostic was introduced in the Java 1.6 compiler
      Class<?> diagnostic = Class.forName("javax.tools.Diagnostic");
      diagnostic.getMethod("getKind");
      // check for Mint classes
      Class.forName("edu.rice.cs.nextgen2.classloader.Runner");
      return true;
    }
    catch (Exception e) { System.out.println(e); return false; }
    catch (LinkageError e) { return false; }
  }
  

  /** Compile the given files.
    *  @param files  Source files to compile.
    *  @param classPath  Support jars or directories that should be on the classpath.  If @code{null}, the default is used.
    *  @param sourcePath  Location of additional sources to be compiled on-demand.  If @code{null}, the default is used.
    *  @param destination  Location (directory) for compiled classes.  If @code{null}, the default in-place location is used.
    *  @param bootClassPath  The bootclasspath (contains Java API jars or directories); should be consistent with @code{sourceVersion} 
    *                        If @code{null}, the default is used.
    *  @param sourceVersion  The language version of the sources.  Should be consistent with @code{bootClassPath}.  If @code{null},
    *                        the default is used.
    *  @param showWarnings  Whether compiler warnings should be shown or ignored.
    *  @return Errors that occurred. If no errors, should be zero length (not null).
    */
  public java.util.List<? extends DJError> compile(java.util.List<? extends File> files,
                                                   java.util.List<? extends File> classPath, 
                                                   java.util.List<? extends File> sourcePath,
                                                   File destination, 
                                                   java.util.List<? extends File> bootClassPath,
                                                   String sourceVersion,
                                                   boolean showWarnings) {
    LinkedList<DJError> errors = new LinkedList<DJError>();
    return errors;
  }
}
