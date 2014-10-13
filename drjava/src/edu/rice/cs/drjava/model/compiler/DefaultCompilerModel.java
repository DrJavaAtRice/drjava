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
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import java.util.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.Option;
import edu.rice.cs.drjava.model.AbstractGlobalModel;
import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.DrJavaFileUtils;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;
import edu.rice.cs.util.swing.DirectoryChooser;
import edu.rice.cs.util.swing.ScrollableListDialog;

import static edu.rice.cs.plt.debug.DebugUtil.debug;

/** Default implementation of the CompilerModel interface. This implementation is used for normal DrJava execution
  * (as opposed to testing DrJava).  TO DO: convert edu.rice.cs.util.Pair to edu.rice.cs.plt.tuple.Pair; requires 
  * making the same conversion in javalanglevels.
  * @version $Id: DefaultCompilerModel.java 5703 2012-08-23 23:16:15Z wdforson $
  */
public class DefaultCompilerModel implements CompilerModel {
  
  /** for logging debug info */
  private static edu.rice.cs.util.Log _log = new edu.rice.cs.util.Log("GlobalModel.txt", false);
  
  /** The available compilers */
  private final List<CompilerInterface> _compilers;
  
  /** Current compiler -- one of _compilers, or a NoCompilerAvailable */
  private volatile CompilerInterface _activeCompiler;
  
  /** Manages listeners to this model. */
  private final CompilerEventNotifier _notifier = new CompilerEventNotifier();
  
  /** The global model to which this compiler model belongs. */
  private final GlobalModel _model;
  
  /** The error model containing all current compiler errors. */
  private volatile CompilerErrorModel _compilerErrorModel;
  
  /** The current build directory. */
  private volatile File _buildDir;
  
  /** The lock providing mutual exclustion between compilation and unit testing */
  private Object _compilerLock = new Object();
  
//  /** The LanguageLevelStackTraceMapper that helps translate .java line 
//    * numbers to .dj* line numbers when an error is thrown */
//  public LanguageLevelStackTraceMapper _LLSTM;
  
  /** Main constructor.  
    * @param m the GlobalModel that is the source of documents for this CompilerModel
    * @param compilers  The compilers to use.  The first will be made active; all are assumed
    *                   to be available.  An empty list is acceptable.
    */
  public DefaultCompilerModel(GlobalModel m, Iterable<? extends CompilerInterface> compilers) {
    
    _compilers = new ArrayList<CompilerInterface>();
    ArrayList<String> compilerNames = new ArrayList<String>();
    
    for (CompilerInterface i : compilers) { _compilers.add(i); compilerNames.add(i.getName());}
    
    OptionConstants.COMPILER_PREFERENCE_CONTROL.setList(compilerNames); // populates the compiler list for preference panel
    
    String dCompName = DrJava.getConfig().getSetting(OptionConstants.DEFAULT_COMPILER_PREFERENCE);
    
    if (_compilers.size() > 0) {
      if (! dCompName.equals(OptionConstants.COMPILER_PREFERENCE_CONTROL.NO_PREFERENCE) &&
           compilerNames.contains(dCompName)) 
        _activeCompiler = _compilers.get(compilerNames.indexOf(dCompName));
      else {
        _activeCompiler = _compilers.get(0);
      }
    }
    else
      _activeCompiler = NoCompilerAvailable.ONLY;
    
//    System.err.println("Setting _activeCompiler to " + _activeCompiler);
    
    _model = m;
    _compilerErrorModel = new CompilerErrorModel(new DJError[0], _model);
//    _LLSTM = new LanguageLevelStackTraceMapper(_model);
  }
   
  //--------------------------------- Locking -------------------------------//
  
  /** Returns the lock used to prevent simultaneous compilation and JUnit testing */
  public Object getCompilerLock() { return _compilerLock; }
  
  //-------------------------- Listener Management --------------------------//
  
  /** Adds a CompilerListener to the model.  This operation is synchronized by the readers/writers protocol in 
    * EventNotifier<T>.
    * @param listener  A listener that reacts to compiler events.
    */
  public void addListener(CompilerListener listener) { _notifier.addListener(listener); }
  
  /** Removes a CompilerListener from the model.  If the listener is not installed, this method has no effect.
    * @param listener a listener that reacts to compiler events
    * This operation is synchronized by the readers/writers protocol in EventNotifier<T>.
    */
  public void removeListener(CompilerListener listener) { _notifier.removeListener(listener); }
  
  /** Removes all CompilerListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }
  
  //-------------------------------- Triggers --------------------------------//
  
  
  /** Compile all open documents.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compileAll() throws IOException {
    if (_prepareForCompile()) { _doCompile(_model.getOpenDefinitionsDocuments()); }
    else _notifier.compileAborted(new UnexpectedException("Some modified open files are unsaved"));
  }
  
  /** Compiles all documents in the project source tree.  Assumes DrJava currently contains an active project.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compileProject() throws IOException {
    if (! _model.isProjectActive()) 
      throw new UnexpectedException("compileProject invoked when DrJava is not in project mode");
    
    if (_prepareForCompile()) { _doCompile(_model.getProjectDocuments()); }
    else _notifier.compileAborted(new UnexpectedException("Project contains unsaved modified files"));
  }
  
  /** Compiles all of the given files.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compile(List<OpenDefinitionsDocument> defDocs) throws IOException {
    if (_prepareForCompile()) { _doCompile(defDocs); }
    else _notifier.compileAborted(new UnexpectedException("The files to be compiled include unsaved modified files"));
  }
  
  /** Compiles the given file.
    * <p>Before compiling, all unsaved and untitled documents are saved, and compilation ends if the user cancels this 
    * step.  The compilation classpath and sourcepath includes the build directory (if it exists), the source roots, 
    * the project "extra classpath" (if it exists), the global "extra classpath", and the current JVM's classpath
    * (which includes drjava.jar, containing JUnit classes).</p>
    * This method formerly only compiled documents which were out of sync with their class file, as a performance 
    * optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
    * case this command would not recompile a file in some situations when it should.  Since we value correctness over
    * performance, we now always compile all open documents.</p>
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compile(OpenDefinitionsDocument doc) throws IOException {
    if (_prepareForCompile()) { _doCompile(Arrays.asList(doc)); }
    else _notifier.compileAborted(new UnexpectedException(doc + "is modified but unsaved"));
  }
  
  /** Check that there are no unsaved or untitled files currently open.
    * @return  @code{true} iff compilation should continue
    */
  private boolean _prepareForCompile() {
    if (_model.hasModifiedDocuments()) _notifier.saveBeforeCompile();
    // If user cancelled save, abort compilation
    return ! _model.hasModifiedDocuments();
  }
  
  /** Compile the given documents. All compile commands invoke this private method! */
  private void _doCompile(List<OpenDefinitionsDocument> docs) throws IOException {
//    _LLSTM.clearCache();
//    System.err.println("_doCompile(" + docs + ") called");
    final ArrayList<File> filesToCompile = new ArrayList<File>();
//    final ArrayList<OpenDefinitionsDocument> validDocs = new ArrayList<OpenDefinitionsDocument>();
    final ArrayList<File> excludedFiles = new ArrayList<File>();
    final ArrayList<DJError> prelimErrors = new ArrayList<DJError>();
    File sourceRoot = FileOps.NULL_FILE;
    
    /* Filter docs to construct filesToCompile */
    for (OpenDefinitionsDocument doc : docs) {
      if (doc.isSourceFile()) {
        File f = doc.getFile();
        // Check for null in case the file is untitled (not sure this is the correct check)
        if (f != null && f != FileOps.NULL_FILE) {
          // process titled source file
          String name = f.getName();
          assert name.endsWith(".scala") || name.endsWith(".java");
          filesToCompile.add(f);
          doc.setCachedClassFile(FileOps.NULL_FILE); // clear cached class file
          
          // in flat file mode, ensure that all files have a common source root
          if (! _model.isProjectActive()) {
            try {
              if (sourceRoot == FileOps.NULL_FILE) 
                sourceRoot = doc.getSourceRoot();
              else {
                File newRoot = doc.getSourceRoot();
                if (! newRoot.equals(sourceRoot)) {
//                  System.err.println("***** File " + f + " has a different source root.");
                  throw new IllegalArgumentException("File " + f + " has a different source root than the first file: "
                                                       + filesToCompile.get(0).getName());
                }
              } 
            }
            catch (Exception e) {
              prelimErrors.add(new DJError(f, e.getMessage(), false));
            }
          }
        }
        else { // add non-source file to excluded files list
          excludedFiles.add(f);
          _log.log("Adding " + f + " to excluded files");
        }
      }
    }
    
    if (filesToCompile.size() == 0) 
      prelimErrors.add(new DJError("None of the documents in " + docs + " is a valid source file!", false));
    
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.compileStarted(); } });
    
    try {
      if (! prelimErrors.isEmpty()) { 
//        System.err.println("Preliminary errors found in compilation");
        _distributeErrors(prelimErrors); 
      }
      else {
        assert filesToCompile.size() > 0;
        // Determine output directory (buildDir)
        
        File buildDir = _model.isProjectActive() ? _model.getBuildDirectory() : sourceRoot;
        if (buildDir == null || buildDir == FileOps.NULL_FILE)   // build directory may not be set in project 
          buildDir = sourceRoot;
        
        if (! buildDir.exists() && ! buildDir.mkdirs()) {   // project file build directory may not exist yet (?)
          _log.log("Could not create build directory: " + buildDir);
          throw new IOException("Could not create build directory: " + buildDir);
        }
        
//        System.err.println("Calling _compileFiles(" + filesToCompile + ", " + buildDir + ")");
        _compileFiles(filesToCompile, buildDir);
      }
    }
    catch (Throwable t) {
      DJError err = new DJError(t.toString(), false);
      _distributeErrors(Arrays.asList(err));
      throw new UnexpectedException(t);
    }
    finally {
      Utilities.invokeLater(new Runnable() {
        public void run() { _notifier.compileEnded(_model.getWorkingDirectory(), excludedFiles); }
      });
    }
  }
  
  /** Converts errors thrown by the language level visitors to CompilerErrors. */
  private LinkedList<DJError> _visitorErrors2CompilerErrors(LinkedList<Pair<String, JExpressionIF>> visitorErrors) {
    final LinkedList<DJError> errors = new LinkedList<DJError>();
    Iterator<Pair<String, JExpressionIF>> iter = visitorErrors.iterator();
    while (iter.hasNext()) {
      Pair<String, JExpressionIF> pair = iter.next();
      String message = pair.getFirst();      
//      System.out.println("Got error message: " + message);
      JExpressionIF jexpr = pair.getSecond();
      
      SourceInfo si;
      if (jexpr == null) si = SourceInfo.NO_INFO;
      else si = pair.getSecond().getSourceInfo();
      
      errors.addLast(new DJError(si.getFile(), si.getStartLine()-1, si.getStartColumn()-1, message, false));
    }
    return errors;
  }
  
  /** Compile the given files and update the model with any errors that result.  Does not notify listeners.  
    * All public compile methods delegate to this one so this method is the only one that uses synchronization to 
    * prevent compiling and unit testing at the same time.
    * @param files The files to be compiled
    * @param buildDir The output directory for all the .class files; assumed to be a valid directory
    */
  private void _compileFiles(List<File> files, File buildDir) throws IOException {
    _log.log("DefaultCompilerModel._compileFiles called with files = " + files + " and buildDir = " + buildDir);
    assert ! files.isEmpty();
    
    /* Canonicalize buildDir */
    buildDir = IOUtil.attemptCanonicalFile(buildDir);
    
    _buildDir = buildDir;  // Cache build directory in this compiler model so it can displayed in CompilerErrorPanel
    
    CompilerInterface compiler = getActiveCompiler();
    
    List<File> classPath = CollectUtil.makeList(_model.getClassPath());
//    System.err.println("Compilation class path is: " + classPath);
    
    // Temporary hack to allow a boot class path to be specified
    List<File> bootClassPath = null;
    String bootProp = System.getProperty("drjava.bootclasspath");
    if (bootProp != null) { bootClassPath = CollectUtil.makeList(IOUtil.parsePath(bootProp)); }
    
    final LinkedList<DJError> errors = new LinkedList<DJError>();
    
    // Mutual exclusion with JUnit code that finds all test classes (in DefaultJUnitModel)
    synchronized(_compilerLock) {
      _log.log("Calling the compiler adapter on: \n  files = " + files + "\n classPath = " + classPath +
               "\n buildDir = " + buildDir + "\n  bootClassPath = " + bootClassPath);
      errors.addAll(compiler.compile(files, classPath, null, buildDir, bootClassPath, null, true));
      _distributeErrors(errors);
    }
  }
  
//  private File _askForOutputDir(File defaultOutputDir) {
//    DirectoryChooser outputDirChooser = new DirectoryChooser(null /* No parent component */, defaultOutputDir);
//    outputDirChooser.setDialogTitle("Choose an output directory for compiled Scala code");
//    outputDirChooser.showDialog();
//    return outputDirChooser.getSelectedDirectory();
//  }
  
  
  /** Reorders files so that all file names containing "Test" are at the end.  */
  private static List<File> _testFileSort(List<File> files) {
    LinkedList<File> testFiles = new LinkedList<File>();
    LinkedList<File> otherFiles = new LinkedList<File>();
    for (File f: files) {
      if (f.getName().contains("Test")) testFiles.add(f);
      else otherFiles.add(f);
    }
    otherFiles.addAll(testFiles);
    return otherFiles;
  }
  
  /** Sorts the given array of CompilerErrors and divides it into groups based on the file, giving each group to the
    * appropriate OpenDefinitionsDocument, opening files if necessary.  Called immediately after compilations finishes.
    */
  private void _distributeErrors(List<? extends DJError> errors) throws IOException {
//    resetCompilerErrors();  // Why is this done?
    _log.log("Preparing to construct CompilerErrorModel for errors: " + errors);
//    System.err.println("Preparing to construct CompilerErrorModel for errors: " + errors);
//    System.err.println("Creating error model with " + errors.size() + " errors");
    _compilerErrorModel = new CompilerErrorModel(errors.toArray(new DJError[0]), _model);
    _model.setNumCompilerErrors(_compilerErrorModel.getNumCompilerErrors());  // cache number of compiler errors in global model
  }
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel getCompilerErrorModel() { return _compilerErrorModel; }
  
  /** Gets the total number of errors in this compiler model. */
  public int getNumErrors() { return getCompilerErrorModel().getNumErrors(); }
  
  /** Gets the total number of current compiler errors. */
  public int getNumCompilerErrors() { return getCompilerErrorModel().getNumCompilerErrors(); }
  
  /** Gets the total number of current warnings. */  
  public int getNumWarnings() { return getCompilerErrorModel().getNumWarnings(); }
  
  /** Resets the compiler error state to have no errors. */
  public void resetCompilerErrors() {
    // TODO: see if we can get by without this function
    _compilerErrorModel = new CompilerErrorModel(new DJError[0], _model);
  }
  
  //-------------------------- Compiler Management --------------------------//
  
  /** Returns all registered compilers that are actually available.  If there are none,
    * the result is {@link NoCompilerAvailable#ONLY}.
    */
  public Iterable<CompilerInterface> getAvailableCompilers() {
    if (_compilers.isEmpty()) { return IterUtil.singleton(NoCompilerAvailable.ONLY); }
    else { return IterUtil.snapshot(_compilers); }
  }
  
  /** Gets the compiler that is the "active" compiler.
    *
    * @see #setActiveCompiler
    */
  public CompilerInterface getActiveCompiler() { return _activeCompiler; }
  
  /** Sets which compiler is the "active" compiler.
    *
    * @param compiler Compiler to set active.
    * @throws IllegalArgumentException  If the compiler is not in the list of available compilers
    *
    * @see #getActiveCompiler
    */
  public void setActiveCompiler(CompilerInterface compiler) {
    if (_compilers.isEmpty() && compiler.equals(NoCompilerAvailable.ONLY)) {
      // _activeCompiler should be set correctly already
    }
    else if (_compilers.contains(compiler)) {
      _activeCompiler = compiler;
      _notifier.activeCompilerChanged();
    }
    else {
      throw new IllegalArgumentException("Compiler is not in the list of available compilers: " + compiler);
    }
  }
  
  /** Returns the current build directory. */
  public File getBuildDir() { return _buildDir; }
  
//  /** Add a compiler to the active list */
//  public void addCompiler(CompilerInterface compiler) {
//    if (_compilers.isEmpty()) {
//      _activeCompiler = compiler;
//    }
//    _compilers.add(compiler);
//  }
  
  /** Delete the .class files that match the following pattern:
    * XXX.dj? --> XXX.class
    *             XXX$*.class
    * @param sourceToTopLevelClassMap a map from directories to classes in them
    */
  public void smartDeleteClassFiles(Map<File,Set<String>> sourceToTopLevelClassMap) {
    final File buildDir = _model.getBuildDirectory();
    final File sourceDir = _model.getProjectRoot();
    // Accessing the disk is the most costly part; therefore, we want to scan each directory only once.
    // We create a map from parent directory to class names in that directory.
    // Then we scan the files in each directory and delete files that match the class names listed for it.
    // dirToClassNameMap: key=parent directory, value=set of classes in this directory
    Map<File,Set<String>> dirToClassNameMap = new HashMap<File,Set<String>>();
    for(Map.Entry<File,Set<String>> e: sourceToTopLevelClassMap.entrySet()) {
      try {
        File dir = e.getKey().getParentFile();
        if (buildDir != null && buildDir != FileOps.NULL_FILE &&
            sourceDir != null && sourceDir != FileOps.NULL_FILE) {
          // build directory set
          String rel = edu.rice.cs.util.FileOps.stringMakeRelativeTo(dir,sourceDir);
          dir = new File(buildDir,rel);
        }
        Set<String> classNames = dirToClassNameMap.get(dir);
        if (classNames == null) classNames = new HashSet<String>();
        classNames.addAll(e.getValue());
        dirToClassNameMap.put(dir,classNames);
//          System.out.println(e.getKey() + " --> " + dir);
//          for(String name: e.getValue()) {
//            System.out.println("\t" + name);
//          }
      }
      catch(IOException ioe) { /* we'll fail to delete this, but that's better than deleting something we shouldn't */ }
    }
    // Now that we have a map from parent directories to the class names that should be deleted
    // in them, we scan the files in each directory, then check if the names match the class names.      
    for(final Map.Entry<File,Set<String>> e: dirToClassNameMap.entrySet()) {
//        System.out.println("Processing dir: " + e.getKey());
//        System.out.println("\t" + java.util.Arrays.toString(e.getValue().toArray(new String[0])));
      e.getKey().listFiles(new java.io.FilenameFilter() {
        public boolean accept(File dir, String name) {
//            System.out.println("\t" + name);
          int endPos = name.lastIndexOf(".class");
          if (endPos < 0) return false; // can't be a class file
          int dollarPos = name.indexOf('$');
          if ((dollarPos >= 0) && (dollarPos < endPos)) endPos = dollarPos;
          // class name goes to the .class or the first $, whichever comes first
          Set<String> classNames = e.getValue();
          if (classNames.contains(name.substring(0,endPos))) { 
            // this is a class file that is generated from a .dj? file
            new File(dir, name).delete();
            // don't need to return true, we're deleting the file here already
//              System.out.println("\t\tDeleted");
          }
          return false;
        }
      });
    }
  }
}
