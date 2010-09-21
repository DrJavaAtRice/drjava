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
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import javax.swing.filechooser.FileFilter;

// NextGen classes
import edu.rice.cs.nextgen2.compiler.main.JavaCompiler;
import edu.rice.cs.nextgen2.compiler.util.Context;
import edu.rice.cs.nextgen2.compiler.util.Name;
import edu.rice.cs.nextgen2.compiler.util.Options;
import edu.rice.cs.nextgen2.compiler.util.Position;
//import edu.rice.cs.nextgen2.compiler.util.List; Clashes with java.util.List
import edu.rice.cs.nextgen2.compiler.util.Log;


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
  
  /** A writer that discards its input. */
  private static final PrintWriter NULL_WRITER = new PrintWriter(new Writer() {
    public void write(char cbuf[], int off, int len) throws IOException {}
    public void flush() throws IOException {}
    public void close() throws IOException {}
  });

  public String getName() { return "Nextgen " + _version.versionString(); }
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. This is necessary for the Nextgen compiler,
    * since the Nextgen compiler needs to be invoked at runtime. */
  public java.util.List<File> additionalBootClassPathForInteractions() {
    System.out.println("NextGenCompiler default boot classpath: "+((_defaultBootClassPath==null)?"null":IOUtil.pathToString(_defaultBootClassPath)));
    System.out.println("NextGenCompiler.additionalBootClassPathForInteractions: "+new File(_location));
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
        interactionsString.startsWith("nextgen ") ||
        interactionsString.startsWith("java ")) interactionsString = _transformNextgenCommand(interactionsString);
    return interactionsString;    
  }
  
  protected static String _transformNextgenCommand(String s) {
    final String command = "edu.rice.cs.nextgen2.classloader.Runner.main(new String[]'{'\"{0}\"{1}'}');";
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
      // At least Java 5
      Class.forName("java.lang.Enum");
      
      // check for NextGen classes
      Class.forName("edu.rice.cs.nextgen2.classloader.Runner");
      Class.forName("edu.rice.cs.nextgen2.compiler.Main");
      Class.forName("edu.rice.cs.nextgen2.compiler.main.JavaCompiler");
      return true;
    }
    catch (Exception e) { System.out.println(e); return false; }
    catch (LinkageError e) { return false; }
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
  public String getSuggestedFileExtension() { return edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION; }
  
  /** Return a file filter that can be used to open files this compiler supports.
    * @return file filter for appropriate source files for this compiler */
  public FileFilter getFileFilter() {
    // javax.swing.filechooser.FileNameExtensionFilter not available, since NextGen compiles with Java 5
    return new FileFilter() {
      /** Returns true if the file's extension matches ".java". */
      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        }
        return f.getName().endsWith(edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION);
      }
      
      /** @return A description of this filter to display. */
      public String getDescription() {
        return "Nextgen source files (*"+edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION+")";
      }
    };
  }

  /** Return the extension of the files that should be opened with the "Open Folder..." command.
    * @return file extension for the "Open Folder..." command for this compiler. */
  public String getOpenAllFilesInFolderExtension() {
    return edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION;
  }
  
  /** Return true if this compiler can be used in conjunction with the language level facility.
    * @return true if language levels can be used. */
  public boolean supportsLanguageLevels() { return false; }

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
    debug.logStart("compile()");
    debug.logValues(new String[]{ "this", "files", "classPath", "sourcePath", "destination", "bootClassPath", 
                                          "sourceVersion", "showWarnings" },
                    this, files, classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    
    Context context = _createContext(classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    OurLog log = new OurLog(context);
    JavaCompiler compiler = _makeCompiler(context);
    
    edu.rice.cs.nextgen2.compiler.util.List<String> filesToCompile = _emptyStringList();
    for (File f : files) {
      // TODO: Can we assume the files are canonical/absolute?  If not, we should use the util methods here.
      filesToCompile = filesToCompile.prepend(f.getAbsolutePath());
    }
    
    try { compiler.compile(filesToCompile); }
    catch (Throwable t) {
      // GJ defines the compile method to throw Throwable?!
      //Added to account for error in javac whereby a variable that was not declared will
      //cause an out of memory error. This change allows us to output both errors and not
      //just the out of memory error
      
      LinkedList<DJError> errors = log.getErrors();
      errors.addFirst(new DJError("Compile exception: " + t, false));
      error.log(t);
      debug.logEnd("compile() (caught an exception)");
      return errors;
    }
    
    debug.logEnd("compile()");
    return log.getErrors();
  }
  
  private Context _createContext(List<? extends File> classPath, List<? extends File> sourcePath, File destination, 
                                 List<? extends File> bootClassPath, String sourceVersion, boolean showWarnings) {

    if (bootClassPath == null) { bootClassPath = _defaultBootClassPath; }
    
    Context context = new Context();
    Options options = Options.instance(context);
    options.putAll(CompilerOptions.getOptions(showWarnings));
    
    //Should be setable some day?
    options.put("-g", "");

    if (classPath != null) { options.put("-classpath", IOUtil.pathToString(classPath)); }
    if (sourcePath != null) { options.put("-sourcepath", IOUtil.pathToString(sourcePath)); }
    if (destination != null) { options.put("-d", destination.getPath()); }
    if (bootClassPath != null) { System.out.println("bootClassPath: "+IOUtil.pathToString(bootClassPath)); options.put("-bootclasspath", IOUtil.pathToString(bootClassPath)); }
    if (sourceVersion != null) { options.put("-source", sourceVersion); }
    if (!showWarnings) { options.put("-nowarn", ""); }
    
    // Bug fix: if "-target" is not present, Iterables in for-each loops cause compiler errors
    if (sourceVersion != null) { options.put("-target", sourceVersion); }
    else { options.put("-target", "1.5"); }

    return context;
  }

  protected JavaCompiler _makeCompiler(Context context) {
    return JavaCompiler.instance(context);
  }
  
  /** Get an empty List using reflection, since the method to do so changed  with version 1.5.0_04. */
  private edu.rice.cs.nextgen2.compiler.util.List<String> _emptyStringList() {
    return edu.rice.cs.nextgen2.compiler.util.List.<String>make();
  }

  /**
   * Replaces the standard compiler "log" so we can track the error
   * messages ourselves. This version will work for JDK 1.4.1+
   * or JSR14 v1.2+.
   */
  private static class OurLog extends Log {
    // List of CompilerError
    private LinkedList<DJError> _errors = new LinkedList<DJError>();
    private String _sourceName = "";

    public OurLog(Context context) { super(context, NULL_WRITER, NULL_WRITER, NULL_WRITER); }

    /** JSR14 uses this crazy signature on warning method because it localizes the warning message. */
    public void warning(int pos, String key, Object ... args) {
      super.warning(pos, key, args);
      //System.out.println("warning: pos = " + pos);

      String msg = getText("compiler.warn." + key, args);
      
      if (currentSource()!=null) {
        _errors.addLast(new DJError(new File(currentSource().toString()),
                                    Position.line(pos) - 1, // gj is 1 based
                                    Position.column(pos) - 1,
                                    msg,
                                    true));
      }
      else {
        _errors.addLast(new DJError(msg, true));
      }
    }

    /** "Mandatory warnings" were added at some point in the development of JDK 5 */
    public void mandatoryWarning(int pos, String key, Object ... args) {
      // super.mandatoryWarning(pos, key, args);
      super.warning(pos, key, args);
      //System.out.println("warning: pos = " + pos);
      
      String msg = getText("compiler.warn." + key, args);
      
      if (currentSource()!=null) {
        _errors.addLast(new DJError(new File(currentSource().toString()),
                                    Position.line(pos) - 1, // gj is 1 based
                                    Position.column(pos) - 1,
                                    msg,
                                    true));
      }
      else {
        _errors.addLast(new DJError(msg, true));
      }
    }
    
    /** JSR14 uses this crazy signature on error method because it localizes the error message. */
    public void error(int pos, String key, Object ... args) {
      super.error(pos, key, args);
      //System.out.println("error: pos = " + pos);

      String msg = getText("compiler.err." + key, args);

      if (currentSource()!=null) {
        _errors.addLast(new DJError(new File(currentSource().toString()),
                                    Position.line(pos) - 1, // gj is 1 based
                                    Position.column(pos) - 1,
                                    msg,
                                    false));
      }
      else {
        _errors.addLast(new DJError(msg, false));
      }
    }

    public void note(String key, Object ... args) {
      super.note(key, args);
      // For now, we just ignore notes
      
      //String msg = getText("compiler.note." + key, args);
    }
    
    public void mandatoryNote(String key, Object ... args) {
      // super.mandatoryNote(key, args);
      super.note(key, args);
      // For now, we just ignore notes
      
      //String msg = getText("compiler.note." + key, args);
    }
    
    public LinkedList<DJError> getErrors() { return _errors; }
  }
}
