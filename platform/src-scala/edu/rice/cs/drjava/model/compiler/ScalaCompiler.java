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
import scala.tools.nsc.reporters.ConsoleReporter;

/** An implementation of JavacCompiler that supports compiling with the Scala 2.9.x compiler based on Java 1.7.0.
  * Must be compiled using javac 1.7.0 and with Scala compiler jar on the boot classpath.  The class 
  * Javac160FilteringCompiler filters .exe files out of the class path because the JVM does not recognize such files
  * on its classpath after early builds of Java 6.
  *
  *  @version $Id$
  */
public class ScalaCompiler extends Javac160FilteringCompiler implements /* Scala */ CompilerInterface {
  
  public static final Log _log = new Log("GlobalModel.txt", false);
  
  private File _outputDir = null;
    
  public ScalaCompiler(JavaVersion.FullVersion version, String location, java.util.List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }
  
  public String getName() { return "Scala " + scala.tools.nsc.Properties.versionString(); }  // Properties$.MODULE$.versionString()
  
//  /** returns the output directory for the Scala compiler.  This method is not an override! */
//  public File getOutputDir() { return _outputDir; }
//  /** sets the output directory for the Scala compiler. */
//  public void setOutputDir(File f) { _outputDir = f; }
   
  /** A compiler can instruct DrJava to include additional elements for the boot class path of the Interactions JVM. 
    * This feature is necessary for the Scala compiler, since the Scala interpreter needs to be invoked at runtime. */
  public java.util.List<File> additionalBootClassPathForInteractions() {
//    Utilities.show("additionalBootClassPath ... called in Scala compiler adapter; _location = " + _location);
    if (_location.equals(FileOps.getDrJavaFile())) {
      // all in one, don't need anything else
      return Arrays.asList(new File(_location));
    }
    else {
      File parentDir = new File(_location).getParentFile();
      if (parentDir == null) return Collections.emptyList();
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
  
  public String transformRunCommand(String s) {    
    if (s.endsWith(";"))  s = _deleteSemiColon(s);
    List<String> args = ArgumentTokenizer.tokenize(s, true);
    final String classNameWithQuotes = args.get(1); // this is "MyClass"
    final String className =
      classNameWithQuotes.substring(1, classNameWithQuotes.length() - 1); // removes quotes, becomes MyClass
    
    // In this override, we use Scala notation for the class object for "MyClass"
    String ret = JavacCompiler.class.getName() + ".runCommand(\"" + s.toString() + "\", classOf[" + className + "])";
    // System.out.println(ret);
    return ret;
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
//    _log.log("ScalaCompiler.isSourceFile(" + f + ") called");
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
  
  /* Java language levels is supported for Java code in .dj files */

//  /** Return true if this compiler can be used in conjunction with the language level facility.
//    * @return true if language levels can be used. */
//  public boolean supportsLanguageLevels() { return false; }
  
  /** Return the set of keywords that should be highlighted in the specified file.
    * @param f file for which to return the keywords
    * @return the set of keywords that should be highlighted in the specified file. */
  public Set<String> getKeywordsForFile(File f) { return SCALA_KEYWORDS; }
  
  /** Add Scala keywords to set of keywords for special coloring. What syntax do we expect in interactions pane? */
  public static final HashSet<String> SCALA_KEYWORDS = new HashSet<String>();
  static {
    SCALA_KEYWORDS.addAll(JAVA_KEYWORDS);
    final String[] words =  {
      "val", "var", "def", "implicit", "override", "yield", "trait", "type", "sealed", "lazy", "object", "forSome", 
      "match", "=>", "<-", "->"
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
    
    ConsoleReporter reporter = new DrJavaReporter(errors);

    // Create a Settings object that captures the Java class path as the Scala class path!
    Settings settings = reporter.settings();
    
//    settings.processArgumentString("-usejavacp");
    settings.processArgumentString("-deprecation");
    _log.log("Passing argument string '" + "-d " + '"' + destination.getPath() + '"' + "to the scala compiler (Global)");
    settings.processArgumentString("-d " + '"' + destination.getPath() + '"');
    // additionalBootClassPathForInteractions consists of the jar files required to run scalac
    String cp = additionalBootClassPathForInteractions().toString() + dJPathToPath(classPath);
    settings.processArgumentString("-cp " + '"' + cp + '"');  // cp quoted because unescaped blanks may appear in Windows file names
//    Utilities.show("Location of Scala distribution is: " + _location + "\nScala compiler class path set to '" + cp + "'");
   
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
    final String pathSep = System.getProperty("path.separator");
    Lambda2<StringBuilder, File, StringBuilder> pathAppend = new Lambda2<StringBuilder, File, StringBuilder>() {
      public StringBuilder value(StringBuilder sb, File f) { return sb.append(pathSep + f.getPath()); }
    }; 
        
    return IterUtil.fold(dJPath, new StringBuilder("."), pathAppend).toString();
  }
}
