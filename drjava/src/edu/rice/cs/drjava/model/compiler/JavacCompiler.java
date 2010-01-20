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

package edu.rice.cs.drjava.model.compiler;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.plt.reflect.JavaVersion;

/** An abstract parent for all javac-based compiler interfaces.  Manages the auxiliary naming methods.
  * To support loading via reflection, all subclasses are assumed to have a public constructor with
  * a matching signature.
  *  @version $Id$
  */
public abstract class JavacCompiler implements CompilerInterface {
  
  protected final JavaVersion.FullVersion _version;
  protected final String _location;
  protected List<? extends File> _defaultBootClassPath;
  
  protected JavacCompiler(JavaVersion.FullVersion version, String location, List<? extends File> defaultBootClassPath) {
    _version = version;
    _location = location;
    _defaultBootClassPath = defaultBootClassPath;
  }
  
  public abstract boolean isAvailable();
  
  public abstract List<? extends DJError> compile(List<? extends File> files, List<? extends File> classPath, 
                                                        List<? extends File> sourcePath, File destination, 
                                                        List<? extends File> bootClassPath, String sourceVersion, 
                                                        boolean showWarnings);
  
  public JavaVersion version() { return _version.majorVersion(); } 
 
  public String getName() { return "JDK " + _version.versionString(); }
  
  public String getDescription() { return getName() + " from " + _location; }
  
  public String toString() { return getName(); }
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. This isn't necessary for the Java compilers, though. */
  public List<File> additionalBootClassPathForInteractions() { return new ArrayList<File>(); }

  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
    if (interactionsString.startsWith("java ")) interactionsString = transformJavaCommand(interactionsString);
    else if (interactionsString.startsWith("applet ")) interactionsString = transformAppletCommand(interactionsString);    
    return interactionsString;
  }
  
  public static String transformJavaCommand(String s) {
    // check the return type and public access before executing, per bug #1585210
    String command = "try '{'\n" +
                     "  java.lang.reflect.Method m = {0}.class.getMethod(\"main\", java.lang.String[].class);\n" +
                     "  if (!m.getReturnType().equals(void.class)) throw new java.lang.NoSuchMethodException();\n" +
                     "'}'\n" +
                     "catch (java.lang.NoSuchMethodException e) '{'\n" +
                     "  throw new java.lang.NoSuchMethodError(\"main\");\n" +
                     "'}'\n" +
                     "{0}.main(new String[]'{'{1}'}');";
    return _transformCommand(s, command);
  }
  
  public static String transformAppletCommand(String s) {
    return _transformCommand(s,"edu.rice.cs.plt.swing.SwingUtil.showApplet(new {0}({1}), 400, 300);");
  }
  
  /** Assumes a trimmed String. Returns a string of the call that the interpreter can use.
    * The arguments get formatted as comma-separated list of strings enclosed in quotes.
    * Example: _transformCommand("java MyClass arg1 arg2 arg3", "{0}.main(new String[]'{'{1}'}');")
    * returns "MyClass.main(new String[]{\"arg1\",\"arg2\",\"arg3\"});"
    * NOTE: the command to run is constructed using {@link java.text.MessageFormat}. That means that certain characters,
    * single quotes and curly braces, for example, are special. To write single quotes, you need to double them.
    * To write curly braces, you need to enclose them in single quotes. Example:
    * MessageFormat.format("Abc {0} ''foo'' '{'something'}'", "def") returns "Abc def 'foo' {something}".
    * @param s the command line, either "java MyApp arg1 arg2 arg3" or "applet MyApplet arg1 arg2 arg3"
    * @param command the command to execute, with {0} marking the place for the class name and {1} the place for the arguments
    */
  protected static String _transformCommand(String s, String command) {
    if (s.endsWith(";"))  s = _deleteSemiColon(s);
    List<String> args = ArgumentTokenizer.tokenize(s, true);
    final String classNameWithQuotes = args.get(1); // this is "MyClass"
    final String className = classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
    final StringBuilder argsString = new StringBuilder();
    boolean seenArg = false;
    for (int i = 2; i < args.size(); i++) {
      if (seenArg) argsString.append(",");
      else seenArg = true;
      argsString.append(args.get(i));
    }
    return java.text.MessageFormat.format(command, className, argsString.toString());
  }
  
  /** Deletes the last character of a string.  Assumes semicolon at the end, but does not check.  Helper 
    * for _transformCommand(String,String).
    * @param s the String containing the semicolon
    * @return a substring of s with one less character
    */
  protected static String _deleteSemiColon(String s) { return  s.substring(0, s.length() - 1); }
}
