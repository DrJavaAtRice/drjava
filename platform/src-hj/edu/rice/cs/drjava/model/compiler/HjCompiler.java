
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

import javax.swing.filechooser.FileFilter;

import hj.lang.Runtime;
import soot.Main;

import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.JarJDKToolsLibrary;
import edu.rice.cs.plt.reflect.PathClassLoader;
import edu.rice.cs.plt.reflect.ShadowingClassLoader;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.FileOps;

import static edu.rice.cs.drjava.model.JDKDescriptor.Util;

/** An implementation of JavacCompiler that supports compiling with the Java 1.6.0/HJ compiler.
  * Must be compiled using javac 1.6.0 and with HJ  on the boot classpath.
  *
  *  @version $Id: HjCompiler.java 5206 2010-04-06 06:52:54Z mgricken $
  */
public class HjCompiler extends Javac160FilteringCompiler { 
  public HjCompiler(JavaVersion.FullVersion version, String location, java.util.List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }
  
  public String getName() { return "HJ " + polyglot.ext.hj.Version.getVersion(); }
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. This is necessary for the Mint compiler,
    * since the Mint compiler needs to be invoked at runtime. */
  public java.util.List<File> additionalBootClassPathForInteractions() {
    if (_location.equals(FileOps.getDrJavaFile())) {
      // all in one, don't need anything else
      return Arrays.asList(new File(_location));
    }
    else {
      File parentDir = new File(_location).getParentFile();
      try {
        File[] jarFiles = new File[] {
          Util.oneOf(parentDir, "sootclasses-2.3.0.jar"),
            Util.oneOf(parentDir, "polyglot.jar"),
            Util.oneOf(parentDir, "lpg.jar"),
            Util.oneOf(parentDir, "jasminclasses-2.3.0.jar"),
            Util.oneOf(parentDir, "java_cup.jar"),
            Util.oneOf(parentDir, "hj.jar")
        };
        return Arrays.asList(jarFiles);    
      }
      catch(FileNotFoundException fnfe) { return Collections.emptyList(); }
    }
  }
  
  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
    // System.out.println(interactionsString);
    if (interactionsString.startsWith("hj ")){
      interactionsString = interactionsString.replace("hj ", "hj hj.lang.Runtime ");
      interactionsString = transformHJCommand(interactionsString);
      // System.out.println(interactionsString);
    }
    if (interactionsString.startsWith("java "))  {
      interactionsString = interactionsString.replace("java ", "java hj.lang.Runtime ");
      interactionsString = transformHJCommand(interactionsString);
    }
    
    if (interactionsString.startsWith("run "))  {
      interactionsString = interactionsString.replace("run ", "java hj.lang.Runtime ");
      interactionsString = transformHJCommand(interactionsString);
      // System.out.println(interactionsString);
    }
    
    
    return interactionsString;
    
  }
    
  public static String transformHJCommand(String s) {
    // System.out.println(s);
    String HJcommand = "hj.lang.Runtime.mainEntry(new String[]{\"-rt=wsh\"";
      
    if (s.endsWith(";"))  s = _deleteSemiColon(s);
    java.util.List<String> args = ArgumentTokenizer.tokenize(s, true);
    final String classNameWithQuotes = args.get(1); // this is "MyClass"
    final StringBuilder argsString = new StringBuilder(HJcommand);
    for (int i = 2; i < args.size(); i++) {
      argsString.append(",");
      argsString.append(args.get(i));
    }
    argsString.append("});");
    
    return argsString.toString();   
  } 
  
  public boolean isAvailable() {
    try {
      Class.forName("hj.parser.HjLexer");
      Class.forName("polyglot.ext.hj.Version");
      Class.forName("polyglot.ext.hj.visit.HjTranslator");
      return true;
    }
    catch (Exception e) { return false; }
    catch (LinkageError e) { return false; }
  }

  /** The extension for a HJ source file */
  public static final String HJ_FILE_EXTENSION = ".hj";
  
  /** .hj   --> true
    * otherwise false 
    * @return true if the specified file is a source file for this compiler. */
  public boolean isSourceFileForThisCompiler(File f) {
    // by default, use DrJavaFileUtils.isSourceFile
    return f.getName().endsWith(HJ_FILE_EXTENSION);
  }
  
  /** Return the set of source file extensions that this compiler supports.
    * @return the set of source file extensions that this compiler supports. */
  public Set<String> getSourceFileExtensions() {
    HashSet<String> extensions = new HashSet<String>();
    extensions.add(getSuggestedFileExtension());
    return extensions;
  }
  
  /** Return the suggested file extension that will be appended to a file without extension.
    * @return the suggested file extension */
  public String getSuggestedFileExtension() { return HJ_FILE_EXTENSION; }
  
  /** Return a file filter that can be used to open files this compiler supports.
    * @return file filter for appropriate source files for this compiler */
  public FileFilter getFileFilter() {
    // javax.swing.filechooser.FileNameExtensionFilter not available, since HJ compiles with Java 5
    return new FileFilter() {
      /** Returns true if the file's extension matches ".hj". */
      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        }
        return f.getName().endsWith(HJ_FILE_EXTENSION);
      }
      
      /** @return A description of this filter to display. */
      public String getDescription() {
        return "Habanero Java source files (*"+HJ_FILE_EXTENSION+")";
      }
    };
  }

  /** Return the extension of the files that should be opened with the "Open Folder..." command.
    * @return file extension for the "Open Folder..." command for this compiler. */
  public String getOpenAllFilesInFolderExtension() {
    return HJ_FILE_EXTENSION;
  }

  /** Return true if this compiler can be used in conjunction with the language level facility.
    * @return true if language levels can be used. */
  public boolean supportsLanguageLevels() { return false; }
  
  /** Return the set of keywords that should be highlighted in the specified file.
    * @param f file for which to return the keywords
    * @return the set of keywords that should be highlighted in the specified file. */
  public Set<String> getKeywordsForFile(File f) {
    return isSourceFileForThisCompiler(f)?new HashSet<String>(HJ_KEYWORDS):new HashSet<String>();
  }
  
  /** Set of Mint keywords for special coloring. */
  public static final HashSet<String> HJ_KEYWORDS = new HashSet<String>();
  static {
    HJ_KEYWORDS.addAll(JAVA_KEYWORDS);
    final String[] words =  {
      "at","activitylocal","async","ateach","atomic","arrayView","await",
      "boxed","compilertest","complex64","complex32","current","extern",
      "finish","forall","foreach","fun","future","here","imag","isolated",
      "local","method","mutable","next","nonblocking","now","nullable",
      "or","phased","placelocal","real","reference","safe","self","seq",
      "sequential","signal","single","unsafe","value","wait","when"
    };
    for(String s: words) { HJ_KEYWORDS.add(s); }
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
    java.util.List<File> filteredClassPath = getFilteredClassPath(classPath);
    
    String s ="";
    if (bootClassPath == null) { bootClassPath = _defaultBootClassPath; }
    
    for(File f: bootClassPath) {
      s += ":" + f.getAbsolutePath();
    }
    if (s.length()>0) { s = s.substring(1); }
    
//    System.out.println("-------------------------------------");
//    System.out.println("-------------------------------------");
//    System.out.println("files = "+files);
//    System.out.println("sourcePath = "+sourcePath);
//    System.out.println("-------------------------------------");
    ArrayList<String> testCommand = new ArrayList<String>();
    testCommand.add("-hj");
    testCommand.add("-info");
    testCommand.add("-sp");
    int spIndex = testCommand.size();
    testCommand.add("<sp filled in here>");
    testCommand.add("-cp");
    testCommand.add(s);
    testCommand.add("-d");
    int destIndex = testCommand.size();
    if (destination != null) {
      testCommand.add(destination.getAbsolutePath());
    }
    else {
      testCommand.add("<dest dir filled in here>");
    }
    testCommand.add("-w");
    testCommand.add("-pp");
    int sourceFileIndex = testCommand.size();
    testCommand.add("<source file filled in here>");
    
    for(File next: files) {
      testCommand.set(spIndex, next.getParentFile().getAbsolutePath());
      if (destination == null) {
        testCommand.set(destIndex, next.getParentFile().getAbsolutePath());
      }
      testCommand.set(sourceFileIndex, next.getName());
      
//      for(String cmd: testCommand) System.out.print(" "+cmd);
//      System.out.println();
      
      try {
        soot.Main.mainEntry(testCommand.toArray(new String[testCommand.size()])); 
      }
      catch(Exception e) {
        e.printStackTrace();
        throw new edu.rice.cs.util.UnexpectedException(e);
      }
    }
    
    return Collections.emptyList();
  }
  
  
  //////////////////////////////////////////////////////////////
}
