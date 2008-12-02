/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

import edu.rice.cs.drjava.model.DJError;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
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

/** Default implementation of the CompilerModel interface. This implementation is used for normal DrJava execution
  * (as opposed to testing DrJava).  TO DO: convert edu.rice.cs.util.Pair to edu.rice.cs.plt.tuple.Pair; requires 
  * making the same conversion in javalanglevels.
  * @version $Id$
  */
public class DefaultCompilerModel implements CompilerModel {
  
  /** The available compilers */
  private final List<CompilerInterface> _compilers;
  
  /** Current compiler -- one of _compilers, or a NoCompilerAvailable */
  private CompilerInterface _active;
  
  /** Manages listeners to this model. */
  private final CompilerEventNotifier _notifier = new CompilerEventNotifier();
  
  /** The global model to which this compiler model belongs. */
  private final GlobalModel _model;
  
  /** The error model containing all current compiler errors. */
  private CompilerErrorModel _compilerErrorModel;
  
  /** The lock providing mutual exclustion between compilation and unit testing */
  private Object _compilerLock = new Object();
  
  /** Main constructor.  
    * @param m the GlobalModel that is the source of documents for this CompilerModel
    * @param compilers  The compilers to use.  The first will be made active; all are assumed
    *                   to be available.  An empty list is acceptable.
    */
  public DefaultCompilerModel(GlobalModel m, Iterable<? extends CompilerInterface> compilers) {
    _compilers = new ArrayList<CompilerInterface>();
    for (CompilerInterface i : compilers) { _compilers.add(i); }
    if (_compilers.size() > 0) { _active = _compilers.get(0); }
    else { _active = NoCompilerAvailable.ONLY; }
    
    _model = m;
    _compilerErrorModel = new CompilerErrorModel(new DJError[0], _model);
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
    * performance, we now always compile all open documents.</p>                                                                                                                              @throws IOException if a filesystem-related problem prevents compilation
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
  
  /** Compile the given documents. */
  private void _doCompile(List<OpenDefinitionsDocument> docs) throws IOException {
    final ArrayList<File> filesToCompile = new ArrayList<File>();
    final ArrayList<File> excludedFiles = new ArrayList<File>();
    final ArrayList<DJError> packageErrors = new ArrayList<DJError>();
    for (OpenDefinitionsDocument doc : docs) {
      if (doc.isSourceFile()) {
        File f = doc.getFile();
        // Check for null in case the file is untitled (not sure this is the correct check)
        if (f != null && f != FileOps.NULL_FILE) { filesToCompile.add(f); }
        doc.setCachedClassFile(FileOps.NULL_FILE); // clear cached class file
        
        try { doc.getSourceRoot(); }
        catch (InvalidPackageException e) {
          packageErrors.add(new DJError(f, e.getMessage(), false));
        }
      }
      else excludedFiles.add(doc.getFile());
    }
    
    Utilities.invokeLater(new Runnable() { public void run() { _notifier.compileStarted(); } });
    try {
      if (! packageErrors.isEmpty()) { _distributeErrors(packageErrors); }
      else {
        try {
          File buildDir = _model.getBuildDirectory();
          if (buildDir != null && buildDir != FileOps.NULL_FILE && ! buildDir.exists() && ! buildDir.mkdirs()) {
            throw new IOException("Could not create build directory: " + buildDir);
          }
          
//          File workDir = _model.getWorkingDirectory();
//          if (workDir == FileOps.NULL_FILE) workDir = null;
//          if (workDir != null && ! workDir.exists() && ! workDir.mkdirs()) {
//            throw new IOException("Could not create working directory: "+workDir);
//          }
          
          _compileFiles(filesToCompile, buildDir);
        }
        catch (Throwable t) {
//          t.printStackTrace();
//          throw new UnexpectedException(t);
          DJError err = new DJError(t.toString(), false);
          _distributeErrors(Arrays.asList(err));
        }
      }
    }
    finally { 
      Utilities.invokeLater(new Runnable() { 
        public void run() { _notifier.compileEnded(_model.getWorkingDirectory(), excludedFiles); } });
    }
  }
  
  
  //-------------------------------- Helpers --------------------------------//
  
  /** Converts JExprParseExceptions thrown by the JExprParser in language levels to CompilerErrors. */
  private LinkedList<DJError> _parseExceptions2CompilerErrors(LinkedList<JExprParseException> pes) {
    final LinkedList<DJError> errors = new LinkedList<DJError>();
    Iterator<JExprParseException> iter = pes.iterator();
    while (iter.hasNext()) {
      JExprParseException pe = iter.next();
      errors.addLast(new DJError(pe.getFile(), pe.currentToken.beginLine-1, pe.currentToken.beginColumn-1, 
                                       pe.getMessage(), false));
    }
    return errors;
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
      if (jexpr == null) si = JExprParser.NO_SOURCE_INFO;
      else si = pair.getSecond().getSourceInfo();
      
      errors.addLast(new DJError(si.getFile(), si.getStartLine()-1, si.getStartColumn()-1, message, false));
    }
    return errors;
  }
  
  /** Compile the given files and update the model with any errors that result.  Does not notify listeners.  
    * All public compile methods delegate to this one so this method is the only one that uses synchronization to 
    * prevent compiling and unit testing at the same time.
    * @param files The files to be compiled
    * @param buildDir The output directory for all the .class files; @code{null} means output to the same 
    *                 directory as the source file
    */
  private void _compileFiles(List<? extends File> files, File buildDir) throws IOException {
    if (! files.isEmpty()) {
      /* Canonicalize buildDir */
      if (buildDir == FileOps.NULL_FILE) buildDir = null; // compiler interface wants null pointer if no build directory
      if (buildDir != null) buildDir = IOUtil.attemptCanonicalFile(buildDir);
      
      List<File> classPath = CollectUtil.makeList(_model.getClassPath());
      
      // Temporary hack to allow a boot class path to be specified
      List<File> bootClassPath = null;
      String bootProp = System.getProperty("drjava.bootclasspath");
      if (bootProp != null) { bootClassPath = CollectUtil.makeList(IOUtil.parsePath(bootProp)); }
      
      final LinkedList<DJError> errors = new LinkedList<DJError>();
      
      List<? extends File> preprocessedFiles = _compileLanguageLevelsFiles(files, errors, classPath, bootClassPath);
      
      if (errors.isEmpty()) {
        CompilerInterface compiler = getActiveCompiler();
        
        synchronized(_compilerLock) {
          if (preprocessedFiles == null) {
            errors.addAll(compiler.compile(files, classPath, null, buildDir, bootClassPath, null, true));
          }
          else {
            /** If compiling a language level file, do not show warnings, as these are not caught by the language level parser */
            errors.addAll(compiler.compile(preprocessedFiles, classPath, null, buildDir, bootClassPath, null, false));
          }
        }
      }
      _distributeErrors(errors);
    }
    else { 
      // TODO: Is this necessary?
      _distributeErrors(Collections.<DJError>emptyList());
    }
  }
  
  
  /** Compiles the language levels files in the list.  Adds any errors to the given error list.
    * @return  An updated list for compilation containing no Language Levels files, or @code{null}
    *          if there were no Language Levels files to process.
    */
  private List<? extends File> _compileLanguageLevelsFiles(List<? extends File> files, List<DJError> errors,
                                                           Iterable<File> classPath, Iterable<File> bootClassPath) {
    LanguageLevelConverter llc = new LanguageLevelConverter();
    Options llOpts;
    if (bootClassPath == null) { llOpts = new Options(getActiveCompiler().version(), classPath); }
    else { llOpts = new Options(getActiveCompiler().version(), classPath, bootClassPath); }
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> llErrors = 
      llc.convert(files.toArray(new File[0]), llOpts);
    
    /* Rename any .dj0 files in files to be .java files, so the correct thing is compiled.  The hashset is used to 
     * make sure we never send in duplicate files. This can happen if the java file was sent in along with the 
     * corresponding .dj* file. The dj* file is renamed to a .java file and thus we have two of the same file in 
     * the list.  By adding the renamed file to the hashset, the hashset efficiently removes duplicates.
     */
    HashSet<File> javaFileSet = new HashSet<File>();
    boolean containsLanguageLevels = false;
    for (File f : files) {
      File canonicalFile = IOUtil.attemptCanonicalFile(f);
      String fileName = canonicalFile.getPath();
      int lastIndex = fileName.lastIndexOf(".dj");
      if (lastIndex != -1) {
        containsLanguageLevels = true;
        javaFileSet.add(new File(fileName.substring(0, lastIndex) + ".java"));
      }
      else { javaFileSet.add(canonicalFile); }
    }
    files = new LinkedList<File>(javaFileSet);
    
    errors.addAll(_parseExceptions2CompilerErrors(llErrors.getFirst()));
    errors.addAll(_visitorErrors2CompilerErrors(llErrors.getSecond()));
    if (containsLanguageLevels) { return files; }
    else { return null; }
  }
  
  /** Sorts the given array of CompilerErrors and divides it into groups based on the file, giving each group to the
    * appropriate OpenDefinitionsDocument, opening files if necessary.  Called immediately after compilations finishes.
    */
  private void _distributeErrors(List<? extends DJError> errors) throws IOException {
//    resetCompilerErrors();  // Why is this done?
//    System.err.println("Preparing to construct CompilerErrorModel for errors: " + errors);
    _compilerErrorModel = new CompilerErrorModel(errors.toArray(new DJError[0]), _model);
    _model.setNumCompErrors(_compilerErrorModel.getNumCompErrors());  // cache number of compiler errors in global model
  }
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel getCompilerErrorModel() { return _compilerErrorModel; }
  
  /** Gets the total number of errors in this compiler model. */
  public int getNumErrors() { return getCompilerErrorModel().getNumErrors(); }
  
  /** Gets the total number of current compiler errors. */
  public int getNumCompErrors() { return getCompilerErrorModel().getNumCompErrors(); }
  
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
  public CompilerInterface getActiveCompiler() { return _active; }
  
  /** Sets which compiler is the "active" compiler.
   *
   * @param compiler Compiler to set active.
   * @throws IllegalArgumentException  If the compiler is not in the list of available compilers
   *
   * @see #getActiveCompiler
   */
  public void setActiveCompiler(CompilerInterface compiler) {
    if (_compilers.isEmpty() && compiler.equals(NoCompilerAvailable.ONLY)) {
      // _active should be set correctly already
    }
    else if (_compilers.contains(compiler)) {
      _active = compiler;
      _notifier.activeCompilerChanged();
    }
    else {
      throw new IllegalArgumentException("Compiler is not in the list of available compilers: " + compiler);
    }
  }
  
  /** Add a compiler to the active list */
  public void addCompiler(CompilerInterface compiler) {
    if (_compilers.isEmpty()) {
      _active = compiler;
    }
    _compilers.add(compiler);
  }
  
}
