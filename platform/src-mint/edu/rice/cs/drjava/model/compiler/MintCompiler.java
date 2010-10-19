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

import edu.rice.cs.mint.comp.com.sun.tools.javac.util.Options;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.Context;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.List;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.ListBuffer;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.Log;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.JavacMessages;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.PropagatedException;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.FatalError;
import edu.rice.cs.mint.comp.com.sun.tools.javac.util.ClientCodeException;
import edu.rice.cs.mint.comp.com.sun.tools.javac.processing.AnnotationProcessingError;

import edu.rice.cs.mint.comp.com.sun.tools.javac.main.JavaCompiler;
  
import edu.rice.cs.mint.comp.com.sun.tools.javac.file.JavacFileManager;
import edu.rice.cs.mint.comp.com.sun.tools.javac.file.CacheFSInfo;

import edu.rice.cs.mint.comp.javax.tools.JavaFileObject;
import edu.rice.cs.mint.comp.javax.tools.JavaFileManager;
import edu.rice.cs.mint.comp.javax.tools.Diagnostic;
import edu.rice.cs.mint.comp.javax.tools.DiagnosticListener;
import edu.rice.cs.mint.comp.javax.annotation.processing.Processor;

import java.io.*;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

import java.lang.reflect.*;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

// DJError class is not in the same package as this
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.plt.reflect.JavaVersion;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.util.ArgumentTokenizer;

import static edu.rice.cs.plt.debug.DebugUtil.debug;
import static edu.rice.cs.plt.debug.DebugUtil.error;

/** An implementation of JavacCompiler that supports compiling with the Java 1.6.0/Mint compiler.
  * Must be compiled using javac 1.6.0 and with Mint on the boot classpath.
  *
  *  @version $Id$
  */
public class MintCompiler extends Javac160FilteringCompiler {
  public MintCompiler(JavaVersion.FullVersion version,
                      String location,
                      java.util.List<? extends File> defaultBootClassPath) {
    super(version, location, defaultBootClassPath);
  }

  public String getName() {
      try {
          // use reflection to be compatible with older versions of Mint
          Class<?> c = Class.forName("edu.rice.cs.mint.Version");
          Method m = c.getMethod("getRevisionNumber");
          return "Mint r" + m.invoke(null);
      }
      catch(Exception e) {
          return "Mint " + _version.versionString();
      }
  }
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. This is necessary for the Mint compiler,
    * since the Mint compiler needs to be invoked at runtime. */
  public java.util.List<File> additionalBootClassPathForInteractions() {
//    System.out.println("MintCompiler default boot classpath: "+((_defaultBootClassPath==null)?"null":IOUtil.pathToString(_defaultBootClassPath)));
//    System.out.println("MintCompiler.additionalBootClassPathForInteractions: "+new File(_location));
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
      throw new RuntimeException("Applets not supported by Mint.");
    }
    if (interactionsString.startsWith("run ") ||
        interactionsString.startsWith("applet ") ||
        interactionsString.startsWith("mint ") ||
        interactionsString.startsWith("run ") ||
        interactionsString.startsWith("java ")) interactionsString = _transformMintCommand(interactionsString);
    return interactionsString;    
  }
  
  protected static String _transformMintCommand(String s) {
    final String command = "edu.rice.cs.mint.runtime.Mint.execute(\"edu.rice.cs.drjava.interactions.class.path\", \"{0}\"{1});";
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
      Class<?> diagnostic = Class.forName("edu.rice.cs.mint.comp.javax.tools.Diagnostic");
      diagnostic.getMethod("getKind");
      // edu.rice.cs.mint.comp.javax.tools.Diagnostic is also found in rt.jar; to test if tools.jar
      // is availble, we need to test for a class only found in tools.jar
      Class.forName("edu.rice.cs.mint.comp.com.sun.tools.javac.main.JavaCompiler");
      // check for Mint classes
      Class.forName("edu.rice.cs.mint.comp.TransStaging");
      Class.forName("edu.rice.cs.mint.comp.com.sun.source.tree.BracketExprTree");
      Class.forName("edu.rice.cs.mint.comp.com.sun.source.tree.BracketStatTree");
      Class.forName("edu.rice.cs.mint.comp.com.sun.source.tree.EscapeExprTree");
      Class.forName("edu.rice.cs.mint.comp.com.sun.source.tree.EscapeStatTree");
      return true;
    }
    catch (Exception e) { return false; }
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
    return new FileNameExtensionFilter
      ("Mint source files (*"+edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION+")",
       edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION.substring(1)); // skip '.'
  }

  /** Return the extension of the files that should be opened with the "Open Folder..." command.
    * @return file extension for the "Open Folder..." command for this compiler. */
  public String getOpenAllFilesInFolderExtension() {
    return edu.rice.cs.drjava.config.OptionConstants.JAVA_FILE_EXTENSION;
  }

  /** Return true if this compiler can be used in conjunction with the language level facility.
    * @return true if language levels can be used. */
  public boolean supportsLanguageLevels() { return false; }
  
  /** Return the set of keywords that should be highlighted in the specified file.
    * @param f file for which to return the keywords
    * @return the set of keywords that should be highlighted in the specified file. */
  public Set<String> getKeywordsForFile(File f) { return new HashSet<String>(MINT_KEYWORDS); }
  
  /** Set of Mint keywords for special coloring. */
  public static final HashSet<String> MINT_KEYWORDS = new HashSet<String>();
  static {
    MINT_KEYWORDS.addAll(JAVA_KEYWORDS);
    MINT_KEYWORDS.add("separable");
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
    debug.logStart("compile()");
    debug.logValues(new String[]{ "this", "files", "classPath", "sourcePath", "destination", "bootClassPath", 
                                  "sourceVersion", "showWarnings" },
                              this, files, classPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    java.util.List<File> filteredClassPath = getFilteredClassPath(classPath);

    LinkedList<DJError> errors = new LinkedList<DJError>();
    Context context = _createContext(filteredClassPath, sourcePath, destination, bootClassPath, sourceVersion, showWarnings);
    new CompilerErrorListener(context, errors);

    int result = compile(new String[] {},
                         ListBuffer.<File>lb().appendArray(files.toArray(new File[0])).toList(),
                         context);
    
    debug.logEnd("compile()");
    return errors;
  }
    
  private Context _createContext(java.util.List<? extends File> classPath,
                                 java.util.List<? extends File> sourcePath,
                                 File destination, 
                                 java.util.List<? extends File> bootClassPath,
                                 String sourceVersion,
                                 boolean showWarnings) {

    if (bootClassPath == null) { bootClassPath = _defaultBootClassPath; }
    
    Context context = new Context();
    Options options = Options.instance(context);
    
    for (Map.Entry<String, String> e : CompilerOptions.getOptions(showWarnings).entrySet()) {
      options.put(e.getKey(), e.getValue());
    }
    
    //Should be setable some day?
    options.put("-g", "");
    
    if (classPath != null) { options.put("-classpath", IOUtil.pathToString(classPath)); }
    if (sourcePath != null) { options.put("-sourcepath", IOUtil.pathToString(sourcePath)); }
    if (destination != null) { options.put("-d", destination.getPath()); }
    if (bootClassPath != null) {
      // System.out.println("bootClassPath: "+IOUtil.pathToString(bootClassPath));
      options.put("-bootclasspath", IOUtil.pathToString(bootClassPath));
    }
    if (sourceVersion != null) { options.put("-source", sourceVersion); }
    if (!showWarnings) { options.put("-nowarn", ""); }
    
    return context;
  }
  
  /** We need to embed a DiagnosticListener in our own Context.  This listener will build a CompilerError list. */
  private static class CompilerErrorListener implements DiagnosticListener<JavaFileObject> {
    
    private java.util.List<? super DJError> _errors;
    
    public CompilerErrorListener(Context context, java.util.List<? super DJError> errors) {
      _errors = errors;
      context.put(DiagnosticListener.class, this);
    }
    
    public void report(Diagnostic<? extends JavaFileObject> d) {
      
      Diagnostic.Kind dt = d.getKind();
      boolean isWarning = false;  // init required by javac
      
      switch (dt) {
        case OTHER:             return;
        case NOTE:              return;
        case MANDATORY_WARNING: isWarning = true; break;
        case WARNING:           isWarning = true; break;
        case ERROR:             isWarning = false; break;
      }
      
      /* The new Java 6.0 Diagnostic interface appears to be broken.  The expression d.getSource().getName() returns a 
        * non-existent path--the name of the test file (allocated as a TEMP file) appended to the source root for 
        * DrJava--in GlobalModelCompileErrorsTest.testCompileFailsCorrectLineNumbers().  The expression 
        * d.getSource().toUri().getPath() returns the correct result as does ((JCDiagnostic) d).getSourceName(). */
      
      if (d.getSource()!=null) {
        _errors.add(new DJError(new File(d.getSource().toUri().getPath()), // d.getSource().getName() fails! 
                                ((int) d.getLineNumber()) - 1,  // javac starts counting at 1
                                ((int) d.getColumnNumber()) - 1, 
                                d.getMessage(null),    // null is the locale
                                isWarning));
      }
      else {
        _errors.add(new DJError(d.getMessage(null), isWarning));
      }
    }
  }
  
  //////////////////////////////////////////////////////////////
  
    /** The name of the compiler, for use in diagnostics.
     */
    String ownName = "mint";

    /** The writer to use for diagnostic output.
     */
    PrintWriter out = new PrintWriter(System.err,true);

    /**
     * If true, any command line arg errors will cause an exception.
     */
    boolean fatalErrors;

    /** Result codes.
     */
    static final int
        EXIT_OK = 0,        // Compilation completed with no errors.
        EXIT_ERROR = 1,     // Completed but reported errors.
        EXIT_CMDERR = 2,    // Bad command-line arguments
        EXIT_SYSERR = 3,    // System error or resource exhaustion.
        EXIT_ABNORMAL = 4;  // Compiler terminated abnormally

    /** A table of all options that's passed to the JavaCompiler constructor.  */
    private Options options = null;

    /** The list of source files to process
     */
    public ListBuffer<File> filenames = null; // XXX sb protected

    /** List of class files names passed on the command line
     */
    public ListBuffer<String> classnames = null; // XXX sb protected

    /** Report a usage error.
     */
    void error(String key, Object... args) {
        if (fatalErrors) {
            String msg = getLocalizedString(key, args);
            throw new PropagatedException(new IllegalStateException(msg));
        }
        warning(key, args);
        Log.printLines(out, getLocalizedString("msg.usage", ownName));
    }

    /** Report a warning.
     */
    void warning(String key, Object... args) {
        Log.printLines(out, ownName + ": "
                       + getLocalizedString(key, args));
    }

    public void setFatalErrors(boolean fatalErrors) {
        this.fatalErrors = fatalErrors;
    }

    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args, List<File> files, Context context) {
        JavacFileManager.preRegister(context); // can't create it until Log has been set up
        int result = compile(args, files, context, List.<JavaFileObject>nil(), null);
        if (fileManager instanceof JavacFileManager) {
            // A fresh context was created above, so jfm must be a JavacFileManager
            ((JavacFileManager)fileManager).close();
        }
        return result;
    }
    
    /** Programmatic interface for main function.
     * @param args    The command line parameters.
     */
    public int compile(String[] args,
                       List<File> files,
                       Context context,
                       List<JavaFileObject> fileObjects,
                       Iterable<? extends Processor> processors) {
        if (options == null)
            options = Options.instance(context); // creates a new one

        filenames = new ListBuffer<File>();
        classnames = new ListBuffer<String>();
        JavaCompiler comp = null;
        /*
         * TODO: Logic below about what is an acceptable command line
         * should be updated to take annotation processing semantics
         * into account.
         */
        try {
            if (args.length == 0 && files.isEmpty() && fileObjects.isEmpty()) {
                return EXIT_CMDERR;
            }

//            try {
//                files = processArgs(CommandLine.parse(args));
                if (files == null) {
                    // null signals an error in options, abort
                    return EXIT_CMDERR;
                } else if (files.isEmpty() && fileObjects.isEmpty() && classnames.isEmpty()) {
                    // it is allowed to compile nothing if just asking for help or version info
                    error("err.no.source.files");
                    return EXIT_CMDERR;
                }
//            } catch (java.io.FileNotFoundException e) {
//                Log.printLines(out, ownName + ": " +
//                               getLocalizedString("err.file.not.found",
//                                                  e.getMessage()));
//                return EXIT_SYSERR;
//            }
            boolean forceStdOut = options.get("stdout") != null;
            if (forceStdOut) {
                out.flush();
                out = new PrintWriter(System.out, true);
            }
            context.put(Log.outKey, out);

            // allow System property in following line as a Mustang legacy
            boolean batchMode = (options.get("nonBatchMode") == null
                        && System.getProperty("nonBatchMode") == null);
            if (batchMode)
                CacheFSInfo.preRegister(context);
            fileManager = context.get(JavaFileManager.class);
            comp = JavaCompiler.instance(context);
            if (comp == null) return EXIT_SYSERR;
            Log log = Log.instance(context);
            if (!files.isEmpty()) {
                // add filenames to fileObjects
                comp = JavaCompiler.instance(context);
                List<JavaFileObject> otherFiles = List.nil();
                JavacFileManager dfm = (JavacFileManager)fileManager;
                for (JavaFileObject fo : dfm.getJavaFileObjectsFromFiles(files))
                    otherFiles = otherFiles.prepend(fo);
                for (JavaFileObject fo : otherFiles)
                    fileObjects = fileObjects.prepend(fo);
            }
            comp.compile(fileObjects,
                         classnames.toList(),
                         processors);
            // TODO: Is this necessary?
//            if (log.expectDiagKeys != null) {
//                if (log.expectDiagKeys.size() == 0) {
//                    Log.printLines(log.noticeWriter, "all expected diagnostics found");
//                    return EXIT_OK;
//                } else {
//                    Log.printLines(log.noticeWriter, "expected diagnostic keys not found: " + log.expectDiagKeys);
//                    return EXIT_ERROR;
//                }
//            }
            if (comp.errorCount() != 0 ||
                options.get("-Werror") != null && comp.warningCount() != 0)
                return EXIT_ERROR;
        } catch (IOException ex) {
            ioMessage(ex);
            return EXIT_SYSERR;
        } catch (OutOfMemoryError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (StackOverflowError ex) {
            resourceMessage(ex);
            return EXIT_SYSERR;
        } catch (FatalError ex) {
            feMessage(ex);
            return EXIT_SYSERR;
        } catch(AnnotationProcessingError ex) {
            apMessage(ex);
            return EXIT_SYSERR;
        } catch (ClientCodeException ex) {
            // as specified by edu.rice.cs.mint.comp.javax.tools.JavaCompiler#getTask
            // and edu.rice.cs.mint.comp.javax.tools.JavaCompiler.CompilationTask#call
            throw new RuntimeException(ex.getCause());
        } catch (PropagatedException ex) {
            throw ex.getCause();
        } catch (Throwable ex) {
            // Nasty.  If we've already reported an error, compensate
            // for buggy compiler error recovery by swallowing thrown
            // exceptions.
            if (comp == null || comp.errorCount() == 0 ||
                options == null || options.get("dev") != null)
                bugMessage(ex);
            return EXIT_ABNORMAL;
        } finally {
            if (comp != null) comp.close();
            filenames = null;
            options = null;
        }
        return EXIT_OK;
    }

    /** Print a message reporting an internal error.
     */
    void bugMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.bug",
                                               JavaCompiler.version()));
        ex.printStackTrace(out);
    }

    /** Print a message reporting an fatal error.
     */
    void feMessage(Throwable ex) {
        Log.printLines(out, ex.getMessage());
    }

    /** Print a message reporting an input/output error.
     */
    void ioMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.io"));
        ex.printStackTrace(out);
    }

    /** Print a message reporting an out-of-resources error.
     */
    void resourceMessage(Throwable ex) {
        Log.printLines(out, getLocalizedString("msg.resource"));
//      System.out.println("(name buffer len = " + Name.names.length + " " + Name.nc);//DEBUG
        ex.printStackTrace(out);
    }

    /** Print a message reporting an uncaught exception from an
     * annotation processor.
     */
    void apMessage(AnnotationProcessingError ex) {
        Log.printLines(out,
                       getLocalizedString("msg.proc.annotation.uncaught.exception"));
        ex.getCause().printStackTrace();
    }

    private JavaFileManager fileManager;

    /* ************************************************************************
     * Internationalization
     *************************************************************************/

    /** Find a localized string in the resource bundle.
     *  @param key     The key for the localized string.
     */
    public static String getLocalizedString(String key, Object... args) { // FIXME sb private
        try {
            if (messages == null)
                messages = new JavacMessages(javacBundleName);
            return messages.getLocalizedString("javac." + key, args);
        }
        catch (MissingResourceException e) {
            throw new Error("Fatal Error: Resource for javac is missing", e);
        }
    }

    public static void useRawMessages(boolean enable) {
        if (enable) {
            messages = new JavacMessages(javacBundleName) {
                    public String getLocalizedString(String key, Object... args) {
                        return key;
                    }
                };
        } else {
            messages = new JavacMessages(javacBundleName);
        }
    }

    private static final String javacBundleName =
        "edu.rice.cs.mint.comp.com.sun.tools.javac.resources.javac";

    private static JavacMessages messages;
}
