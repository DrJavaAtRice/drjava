/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.io.File;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.plt.reflect.JavaVersion;

import javax.swing.filechooser.FileFilter;
import edu.rice.cs.drjava.ui.SmartSourceFilter;

import java.lang.reflect.Constructor;

/** An abstract parent for all javac-based compiler interfaces.  Manages the auxiliary naming methods.
  * To support loading via reflection, all subclasses are assumed to have a public constructor with
  * a matching signature.
  *  @version $Id: JavacCompiler.java 5668 2012-08-15 04:58:30Z rcartwright $
  */
public abstract class JavacCompiler implements CompilerInterface {
  
  protected final JavaVersion.FullVersion _version;
  protected final String _location;
  protected List<? extends File> _defaultBootClassPath;
  
  /** The set of class names that are run as ACM Java Task Force library programs. */
  protected static final Set<String> ACM_PROGRAM_CLASSES = new HashSet<String>();
  static {
    Collections.addAll(ACM_PROGRAM_CLASSES, new String[] {
      "acm.program.Program",
        "acm.graphics.GTurtle"
    });
  }
  
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
  public List<File> additionalBootClassPathForInteractions() { return Arrays.<File>asList(); }

  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * transformCommands should support at least "run", "java" and "applet".
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
    if (interactionsString.startsWith("java ")) {
      interactionsString = transformJavaCommand(interactionsString);
    }
    else if (interactionsString.startsWith("applet ")) {
      interactionsString = transformAppletCommand(interactionsString);
    }
    else if (interactionsString.startsWith("run ")) {
      interactionsString = transformRunCommand(interactionsString);
    }
    return interactionsString;
  }
  
  public static String transformJavaCommand(String s) {
    // check the return type and public access before executing, per bug #1585210
    String command = 
      "try '{'\n" +
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
  
  /** This method performs the "smart run". Unfortunately, we don't get the right static error messages.
    * @param s full command line, i.e. "run MyClass 1 2 3"
    * @param c class to be run, i.e. MyClass.class
    */
  @SuppressWarnings("unchecked")
  public static void runCommand(String s, Class<?> c) throws Throwable {
    if (s.endsWith(";"))  s = _deleteSemiColon(s);
    List<String> tokens = ArgumentTokenizer.tokenize(s, true);
    final String classNameWithQuotes = tokens.get(1); // this is "MyClass"
    final String className =
      classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
    String[] args = new String[tokens.size() - 2];
    for (int i = 2; i < tokens.size(); i++) {
      String t = tokens.get(i);
      args[i - 2] = t.substring(1, t.length() - 1);
    }
    
    boolean isProgram = false;
    boolean isApplet = false;
    Class<?> oldC = c;
    while(c != null) {
      if (ACM_PROGRAM_CLASSES.contains(c.getName())) { isProgram = true; break; }
      c = c.getSuperclass();
    }
    c = oldC;
    if (! isProgram) {
      try {
        // if this doesn't throw, c is a subclass of Applet
        c.asSubclass(java.applet.Applet.class);
        isApplet = true;
      } catch(ClassCastException cce) { }
    }

    java.lang.reflect.Method m = null;
    if (isApplet) {
      try {
        m = c.getMethod("main", java.lang.String[].class);
        if (!m.getReturnType().equals(void.class)) { m = null; }
      }
      catch (java.lang.NoSuchMethodException e) { m = null; }
      if (m == null) {
        java.applet.Applet instance = null;
        if (args.length == 0) {
          try {
            // try default (nullary) constructor first
            Constructor<?> ctor = c.getConstructor();
            instance = java.applet.Applet.class.cast(ctor.newInstance());
          }
          catch(NoSuchMethodException nsme) { instance = null; }
          catch(InstantiationException ie) { instance = null; }
          catch(IllegalAccessException iae) { instance = null; }
          catch(java.lang.reflect.InvocationTargetException ite) {
            if (ite.getCause() != null) {
              throw ite.getCause();
            }
            else {
//              System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
            }
          }
          if (instance == null) {
            try {
              // try String[] constructor next
              Constructor<?> ctor = c.getConstructor(String[].class);
              instance = java.applet.Applet.class.cast(ctor.newInstance(new Object[] { new String[0] }));
            }
            catch(NoSuchMethodException nsme) { instance = null; }
            catch(InstantiationException ie) { instance = null; }
            catch(IllegalAccessException iae) { instance = null; }
            catch(java.lang.reflect.InvocationTargetException ite) {
              if (ite.getCause()!=null) {
                throw ite.getCause();
              }
              else {
//                System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
                return;
              }
            }
          }
          if (instance==null) {
//            System.err.println("Static Error: This applet does not have a default constructor or a constructor "+
//                               "accepting String[].");
            return;
          }
        }
        else {
          try {
            // try String[] constructor
            Constructor<?> ctor = c.getConstructor(String[].class);
            instance = java.applet.Applet.class.cast(ctor.newInstance(new Object[] { args }));
          }
          catch(NoSuchMethodException nsme) { instance = null; }
          catch(InstantiationException ie) { instance = null; }
          catch(IllegalAccessException iae) { instance = null; }
          catch(java.lang.reflect.InvocationTargetException ite) {
            if (ite.getCause()!=null) {
              throw ite.getCause();
            }
            else {
//              System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
              return;
            }
          }
          if (instance==null) {
//            System.err.println("Static Error: This applet does not have a constructor accepting String[].");
            return;
          }
        }
        edu.rice.cs.plt.swing.SwingUtil.showApplet(instance, 400, 300);
      }
    }
    else {
      try {
        m = c.getMethod("main", java.lang.String[].class);
        if (!m.getReturnType().equals(void.class)) {
//          System.err.println("Static Error: This class does not have a static void main method accepting String[].");
          m = null;
        }
      }
      catch (java.lang.NoSuchMethodException e) {
//        System.err.println("Static Error: This class does not have a static void main method accepting String[].");
        m = null;
      }
    }
    if (m != null) {
      if (isProgram) {
        String[] newArgs = new String[args.length+1];
        newArgs[0] = "code="+c.getName();
        System.arraycopy(args, 0, newArgs, 1, args.length);
        args = newArgs;
      }
      try {
        m.setAccessible(true);
        m.invoke(null, new Object[] { args });
      }
      catch(SecurityException se) {
//        System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
      }
      catch(IllegalAccessException iae) {
//        System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
      }
      catch(java.lang.reflect.InvocationTargetException ite) {
        if (ite.getCause()!=null) {
          throw ite.getCause();
        }
        else {
//          System.err.println("Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.");
        }
      }
    }
  }
  
  // This is a command that automatically detects if
  // a) the class is an ACM Java Task Force program (subclass of acm.program.Program)
  // b) an applet
  // c) a class with a static main method
  //
  // If a), then DrJava inserts "code=MyClass" as argument 0.
  // If b), then DrJava performs the same as "applet MyClass" (see above).
  // If c), then DrJava executes MyClass.main (traditional java behavior).
  public String transformRunCommand(String s) {    
    if (s.endsWith(";"))  s = _deleteSemiColon(s);
    List<String> args = ArgumentTokenizer.tokenize(s, true);
    final String classNameWithQuotes = args.get(1); // this is "MyClass"
    final String className =
      classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
    
    // we pass MyClass.class just to get a "Static Error: Undefined class 'MyClass'"
    String ret = JavacCompiler.class.getName() + ".runCommand(\"" + s.toString() + "\", " + className + ".class)";
    // System.out.println(ret);
    return ret;
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

  /** .java --> true
    * otherwise false 
    * @return true if the specified file is a source file for this compiler. */
  public boolean isSourceFileForThisCompiler(File f) {
    String fileName = f.getName();
    return fileName.endsWith(OptionConstants.JAVA_FILE_EXTENSION);
  }
  
  /** Return the set of source file extensions that this compiler supports.
    * @return the set of source file extensions that this compiler supports. */
  public Set<String> getSourceFileExtensions() { return DrJavaFileUtils.getSourceFileExtensions(); }
  
  /** Return the suggested file extension that will be appended to a file without extension.
    * @return the suggested file extension */
  public String getSuggestedFileExtension() {
    return OptionConstants.JAVA_FILE_EXTENSION;
  }
  
  /** Return a file filter that can be used to open files this compiler supports.
    * @return file filter for appropriate source files for this compiler */
  public FileFilter getFileFilter() { return new SmartSourceFilter(); }

  /** Return the extension of the files that should be opened with the "Open Folder..." command.
    * @return file extension for the "Open Folder..." command for this compiler. */
  public String getOpenAllFilesInFolderExtension() {
    return OptionConstants.JAVA_FILE_EXTENSION;
  }

  /** Return true if this compiler can be used in conjunction with the language level facility.
    * @return true if language levels can be used. */
  public boolean supportsLanguageLevels() { return true; }  // Allowed in Java code in DrScala
  
  /** Return the set of keywords that should be highlighted in the specified file.
    * @param f file for which to return the keywords
    * @return the set of keywords that should be highlighted in the specified file. */
  public Set<String> getKeywordsForFile(File f) { return JAVA_KEYWORDS; }
  
  /** Set of Java/GJ keywords for special coloring. */
  public static final HashSet<String> JAVA_KEYWORDS = new HashSet<String>();
  static {
    final String[] words =  {
      "import", "native", "package", "goto", "const", "if", "else", "switch", "while", "for", "do", "true", "false",
      "null", "this", "super", "new", "instanceof", "return", "static", "synchronized", "transient", "volatile", 
      "final", "strictfp", "throw", "try", "catch", "finally", "throws", "extends", "implements", "interface", "class",
      "break", "continue", "public", "protected", "private", "abstract", "case", "default", "assert", "enum"
    };
    for(String s: words) { JAVA_KEYWORDS.add(s); }
  }
  
//  // This is a command that automatically detects if
//  // a) the class is an ACM Java Task Force program (subclass of acm.program.Program or acm.graphics.GTurtle)
//  // b) an applet
//  // c) a class with a static main method
//  //
//  // If a), then DrJava inserts "code=MyClass" as argument 0.
//  // If b), then DrJava performs the same as "applet MyClass" (see above).
//  // If c), then DrJava executes MyClass.main (traditional java behavior).
//  public static String transformRunCommand(String s) {
//    if (s.endsWith(";"))  s = _deleteSemiColon(s);
//    List<String> tokens = ArgumentTokenizer.tokenize(s, true);
//    final String classNameWithQuotes = tokens.get(1); // this is "MyClass"
//    final String className =
//      classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
//    String[] args = new String[tokens.size() - 2];
//    final StringBuilder argsString = new StringBuilder();
//    boolean seenArg = false;
//    for (int i = 2; i < tokens.size(); i++) {
//      String t = tokens.get(i); // with quotes
//      args[i - 2] = t.substring(1, t.length() - 1);
//      
//      if (seenArg) argsString.append(",");
//      else seenArg = true;
//      argsString.append(t);
//    }
//    
//    StringBuilder command = new StringBuilder();
//    
//    command.append("{\n"+
//       "boolean isProgram = false;\n" +
//       "boolean isApplet = false;\n" +
//       "Class c = ").append(className).append(".class;\n" +
//       "while(c != null) {\n" +
//       "  if (\"acm.program.Program\".equals(c.getName()) ||\n" +
//       "      \"acm.graphics.GTurtle\".equals(c.getName())) { isProgram = true; break; }\n" +
//       "  c = c.getSuperclass();\n" +
//       "}\n" +
//       "c = ").append(className).append(".class;\n" +
//       "if (!isProgram) {\n" +
//       "  try {\n" +
//       "    // if this doesn't throw, c is a subclass of Applet\n" +
//       "    c.asSubclass(java.applet.Applet.class);\n" +
//       "    isApplet = true;\n" +
//       "  } catch(ClassCastException cce) { }\n" +
//       "}\n" +
//       "java.lang.reflect.Method m = null;\n" +
//       "String[] args = new String[] {").append(argsString.toString()).append("};\n" +
//       "if (isApplet) {\n" +
//       "  try {\n" +
//       "    m = c.getMethod(\"main\", java.lang.String[].class);\n" +
//       "    if (!m.getReturnType().equals(void.class)) { m = null; }\n" +
//       "  }\n" +
//       "  catch (java.lang.NoSuchMethodException e) { m = null; }\n" +
//       "  if (m==null) {\n" +
//       "    java.applet.Applet instance = null;\n" +
//       "    boolean fail = false;\n");
//    if (args.length==0) {
//      command.append(
//       "    try {\n" +
//       "      // try default (nullary) constructor first\n" +
//       "      java.lang.reflect.Constructor ctor = c.getConstructor();\n" +
//       "      instance = java.applet.Applet.class.cast(ctor.newInstance());\n" +
//       "    }\n" +
//       "    catch(NoSuchMethodException nsme) { instance = null; }\n" +
//       "    catch(InstantiationException ie) { instance = null; }\n" +
//       "    catch(IllegalAccessException iae) { instance = null; }\n" +
//       "    catch(java.lang.reflect.InvocationTargetException ite) {\n" +
//       "      if (ite.getCause()!=null) {\n" +
//       "        throw ite.getCause();\n" +
//       "      }\n" +
//       "      else {\n" +
//       "        System.err.println(\"Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.\");\n" +
//       "      }\n" +
//       "    }\n" +
//       "    if (instance==null) {\n" +
//       "      try {\n" +
//       "        // try String[] constructor next\n" +
//       "        java.lang.reflect.Constructor ctor = c.getConstructor(String[].class);\n" +
//       "        instance = java.applet.Applet.class.cast(ctor.newInstance(new Object[] { new String[0] }));\n" +
//       "      }\n" +
//       "      catch(NoSuchMethodException nsme) { instance = null; }\n" +
//       "      catch(InstantiationException ie) { instance = null; }\n" +
//       "      catch(IllegalAccessException iae) { instance = null; }\n" +
//       "      catch(java.lang.reflect.InvocationTargetException ite) {\n" +
//       "        if (ite.getCause()!=null) {\n" +
//       "          throw ite.getCause();\n" +
//       "        }\n" +
//       "        else {\n" +
//       "          System.err.println(\"Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.\");\n" +
//       "          fail = true;\n" +
//       "        }\n" +
//       "      }\n" +
//       "    }\n" +
//       "    if (!fail && (instance==null)) {\n" +
//       "      System.err.println(\"Error: This applet does not have a default constructor or a constructor \"+\n" +
//       "                         \"accepting String[].\");\n" +
//       "      fail = true;\n" +
//       "    }\n");
//    }
//    else {
//      command.append(
//       "    try {\n" +
//       "      // try String[] constructor\n" +
//       "      java.lang.reflect.Constructor ctor = c.getConstructor(String[].class);\n" +
//       "      instance = java.applet.Applet.class.cast(ctor.newInstance(new Object[] { args }));\n" +
//       "    }\n" +
//       "    catch(NoSuchMethodException nsme) { instance = null; }\n" +
//       "    catch(InstantiationException ie) { instance = null; }\n" +
//       "    catch(IllegalAccessException iae) { instance = null; }\n" +
//       "    catch(java.lang.reflect.InvocationTargetException ite) {\n" +
//       "      if (ite.getCause()!=null) {\n" +
//       "        throw ite.getCause();\n" +
//       "      }\n" +
//       "      else {\n" +
//       "        System.err.println(\"Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.\");\n" +
//       "        fail = true;\n" +
//       "      }\n" +
//       "    }\n" +
//       "    if (!fail && (instance==null)) {\n" +
//       "      System.err.println(\"Error: This applet does not have a constructor accepting String[].\");\n" +
//       "      fail = true;\n" +
//       "    }\n");
//    }
//    command.append(
//       "    if (!fail) { edu.rice.cs.plt.swing.SwingUtil.showApplet(instance, 400, 300); }\n" +
//       "  } // if (m==null)\n" +
//       "} // if (isApplet)\n" +
//       "else {\n" +
//       "  try {\n" +
//       "    m = c.getMethod(\"main\", java.lang.String[].class);\n" +
//       "    if (!m.getReturnType().equals(void.class)) {\n" +
//       "      System.err.println(\"Error: This class does not have a static void main method accepting String[].\");\n" +
//       "      m = null;\n" +
//       "    }\n" +
//       "  }\n" +
//       "  catch (java.lang.NoSuchMethodException e) {\n" +
//       "    System.err.println(\"Error: This class does not have a static void main method accepting String[].\");\n" +
//       "    m = null;\n" +
//       "  }\n" +
//       "} // else\n" +
//       "if (m != null) {\n" +
//       "  if (isProgram) {\n" +
//       "    String[] newArgs = new String[args.length+1];\n" +
//       "    newArgs[0] = \"code=\"+c.getName();\n" +
//       "    System.arraycopy(args, 0, newArgs, 1, args.length);\n" +
//       "    args = newArgs;\n" +
//       "  }\n" +
//       "  try {\n" +
//       "    m.setAccessible(true);\n" +
//       "    m.invoke(null, new Object[] { args });\n" +
//       "  }\n" +
//       "  catch(SecurityException se) {\n" +
//       "    System.err.println(\"Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.\");\n" +
//       "  }\n" +
//       "  catch(IllegalAccessException iae) {\n" +
//       "    System.err.println(\"Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.\");\n" +
//       "  }\n" +
//       "  catch(java.lang.reflect.InvocationTargetException ite) {\n" +
//       "    if (ite.getCause()!=null) {\n" +
//       "      throw ite.getCause();\n" +
//       "    }\n" +
//       "    else {\n" +
//       "      System.err.println(\"Error: Please turn off 'Smart Run' or use 'java' command instead of 'run'.\");\n" +
//       "    }\n" +
//       "  }\n" +
//       "}\n" +
//    "}");
//    
//    // System.out.println(command);
//
//    return command.toString();
//  }
}
