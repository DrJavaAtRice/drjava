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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.JDKDescriptor;
import edu.rice.cs.drjava.model.JDKDescriptor.Util;
import edu.rice.cs.drjava.model.JDKToolsLibrary;
import edu.rice.cs.drjava.model.compiler.Javac160FilteringCompiler;
import edu.rice.cs.drjava.model.compiler.ScalaCompilerInterface;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.util.ArgumentTokenizer;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.swing.Utilities;

import scala.tools.nsc.Global;
import scala.tools.nsc.Settings;
import scala.tools.nsc.io.PlainFile;
import scala.tools.nsc.io.Path;

/** An implementation of JavacCompiler that supports compiling with the Scala 2.9.x compiler based on Java 1.7.0.
  * Must be compiled using javac 1.7.0 and with Scala compiler jar on the boot classpath.  The class 
  * Javac160FilteringCompiler filters .exe files out of the class path because the JVM does not recognize such files
  * on its classpath after early builds of Java 6.
  *
  *  @version $Id$
  */
public class ScalaCompiler extends Javac160FilteringCompiler implements ScalaCompilerInterface {
  
  public static final Log _log = new Log("GlobalModel.txt", true);
  
  private File _outputDir = null;
    
  public ScalaCompiler(JavaVersion.FullVersion version, String location, java.util.List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }
  
  public String getName() { return "Scala " + scala.tools.nsc.Properties.versionString(); }  // Properties$.MODULE$.versionString()
  
  /** returns the output directory for the Scala compiler.  This method is not an override! */
  public File getOutputDir() { return _outputDir; }
  /** sets the output directory for the Scala compiler. */
  public void setOutputDir(File f) { _outputDir = f; }
   
  /** A compiler can instruct DrJava to include additional elements for the boot class path of the Interactions JVM. 
    * This feature is necessary for the Scala compiler, since the Scala interpreter needs to be invoked at runtime. */
  public java.util.List<File> additionalBootClassPathForInteractions() {
    if (_location.equals(FileOps.getDrJavaFile())) {
      // all in one, don't need anything else
      return Arrays.asList(new File(_location));
    }
    else {
      File parentDir = new File(_location).getParentFile();
      try {
        File[] jarFiles = new File[] {
          Util.oneOf(parentDir, "jline.jar"),
          Util.oneOf(parentDir, "scala-compiler.jar"),
          Util.oneOf(parentDir, "scala-dbc.jar"),
          Util.oneOf(parentDir, "scala-library.jar"),
          Util.oneOf(parentDir, "scala-swing.jar"),
          Util.oneOf(parentDir, "scalap.jar")
        };
        return Arrays.asList(jarFiles);    
      }
      catch(FileNotFoundException fnfe) { return Collections.emptyList(); }
    }
  }
  
  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "scala MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString) {
    // System.out.println(interactionsString);
    if (interactionsString.startsWith("scala ")) {
      interactionsString = interactionsString.replace("scala ", "java ");
    }
   return super.transformCommands(interactionsString);
  }
  
  public boolean isAvailable() {
    JDKToolsLibrary.msg("Testing scala-compiler.jar to determine if it contains scala.tools.nsc.Main");
    try {
      // Confirm that Scala compiler is available
      Class.forName("scala.tools.nsc.Main");
      JDKToolsLibrary.msg("Returning true");
      return true;
    }
    catch (Exception e) { JDKToolsLibrary.msg("Returning false"); return false; }
    catch (LinkageError e) { JDKToolsLibrary.msg("Returning false"); return false; }
  }

  /** The extension for a Scala source file */
  public static final String SCALA_FILE_EXTENSION = ".scala";
  
  /** .scala --> true
    * .java --> true       scalac processes .java files to generate symbol tables but does not generate their bytecode
    * otherwise false 
    * @return true if the specified file is a source file for this compiler. */
  public boolean isSourceFileForThisCompiler(File f) {
    // by default, use DrJavaFileUtils.isSourceFile
    _log.log("ScalaCompiler.isSourceFile(" + f + ") called");
    String fileName = f.getName();
    return fileName.endsWith(SCALA_FILE_EXTENSION) || fileName.endsWith(OptionConstants.JAVA_FILE_EXTENSION);
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
  public String getSuggestedFileExtension() { return SCALA_FILE_EXTENSION; }
  
  /** Return a file filter that can be used to open files this compiler supports.
    * @return file filter for appropriate source files for this compiler */
  public FileFilter getFileFilter() {
    // javax.swing.filechooser.FileNameExtensionFilter not available, since HJ compiles with Java 5
    return new FileFilter() {
      /** Returns true if the file's extension matches ".scala" or ".java". */
      public boolean accept(File f) {
        if (f.isDirectory()) return true;
        return f.getName().endsWith(SCALA_FILE_EXTENSION) || f.getName().endsWith(OptionConstants.JAVA_FILE_EXTENSION);
      }
      
      /** @return A description of this filter to display. */
      public String getDescription() {
        return "Scala/Java source files (*" + SCALA_FILE_EXTENSION + ", *" + OptionConstants.JAVA_FILE_EXTENSION + ")";
      }
    };
  }

  /** Return the extension of the files that should be opened with the "Open Folder..." command.
    * @return file extension for the "Open Folder..." command for this compiler. */
  public String getOpenAllFilesInFolderExtension() {
    return SCALA_FILE_EXTENSION;
  }

  /** Return true if this compiler can be used in conjunction with the language level facility.
    * @return true if language levels can be used. */
  public boolean supportsLanguageLevels() { return false; }
  
  /** Return the set of keywords that should be highlighted in the specified file.
    * @param f file for which to return the keywords
    * @return the set of keywords that should be highlighted in the specified file. */
  public Set<String> getKeywordsForFile(File f) {
    return isSourceFileForThisCompiler(f) ? new HashSet<String>(SCALA_KEYWORDS) : new HashSet<String>();
  }
  
  /** Set of Scala keywords for special coloring. */
  public static final HashSet<String> SCALA_KEYWORDS = new HashSet<String>();
  static {
    SCALA_KEYWORDS.addAll(JAVA_KEYWORDS);
    final String[] words =  {
      "val", "var", "def", "implicit", "override"
    };
    for(String s: words) { SCALA_KEYWORDS.add(s); }
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
  public List<? extends DJError> compile(List<? extends File> files, List<? extends File> classPath, 
                                         List<? extends File> sourcePath, File destination, 
                                         List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {
//    debug.logStart("compile()");
//    debug.logValues(new String[]{ "this", "files", "classPath", "sourcePath", "destination", "bootClassPath", 
//      "sourceVersion", "showWarnings" },
//                    this, files, classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
//    java.util.List<File> filteredClassPath = getFilteredClassPath(classPath);
    
    /* Must create a Reporter object for the Scalac compiler.  In Scala Setting appears to be the rough equivalent of
     * compilatoin context.  We are using the default Setting.  Scalac does not take a list of JavaFileObjects. */
    
    /** DrJava error table, which is passed to our compilation reporter and embedded in it. */
    LinkedList<DJError> errors = new LinkedList<DJError>();
    
//    Utilities.show("compile command for Scala called");
    

    // Create a Settings object that captures the Java class path as the Scala class path!
    Settings settings = new Settings();
    settings.processArgumentString("-usejavacp");
    settings.processArgumentString("-d " + '"' + destination.getPath() + '"');
    settings.processArgumentString("-cp " + '"' + dJPathToPath(classPath) + '"');
    scala.tools.nsc.reporters.Reporter reporter = new DrJavaReporter(errors);
    Global compiler = new Global(settings, reporter);
 
//    Utilities.show("Scala compiler object constructed");
    /* Create a run of the Scala compiler. */
    Global.Run run = compiler.new Run();
    
    /* Build a Scala List[PlainFile] corresponding to files.  fileObjects is a Scala List of PlainFile but
     * the Java compiler does not recognize Scala generic covariant types.  */
    scala.collection.immutable.List fileObjects =   
      scala.collection.immutable.Nil$.MODULE$;  // the empty list in Scala
    for (File f : files) fileObjects = fileObjects.$colon$colon(new PlainFile(Path.jfile2path(f)));
    try { run.compileFiles(fileObjects); }  // fileObjects has raw type List
    catch(Throwable t) {  // compiler threw an exception/error (typically out of memory error)
      errors.addFirst(new DJError("Compile exception: " + t, false));
//      error.log(t);
    }
    
//    debug.logEnd("compile()");
    return errors;
  }
  
  /** Converts the DJ path (of type Iterable<File>) to the corresponding platform-dependent String. */
  public static String dJPathToPath(Iterable<? extends File> dJPath) {
    Lambda2<StringBuilder, File, StringBuilder> pathAppend = new Lambda2<StringBuilder, File, StringBuilder>() {
      public StringBuilder value(StringBuilder sb, File f) { return sb.append(File.separator + f.getPath()); }
    }; 
        
    return IterUtil.fold(dJPath, new StringBuilder("."), pathAppend).toString();
  }
}
