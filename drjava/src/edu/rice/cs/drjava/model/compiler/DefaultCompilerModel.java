/* BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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
 *END_COPYRIGHT_BLOCK */

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;

import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;

import edu.rice.cs.util.ClassPathVector;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.FileOps;

import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;

/** Default implementation of the CompilerModel interface. This implementation is used for normal DrJava execution
 *  (as opposed to testing DrJava).
 *  @version $Id$
 */
public class DefaultCompilerModel implements CompilerModel {
  
  /** Manages listeners to this model. */
  private final CompilerEventNotifier _notifier = new CompilerEventNotifier();

  /** The global model to which this compiler model belongs. */
  private final GlobalModel _model;

  /** The error model containing all current compiler errors. */
  private CompilerErrorModel _compilerErrorModel;
  
  /** The working directory corresponding to the last compilation */
  private File _workDir;
  
  /** The lock for using the slaveJVM to perform compilation and run unit tests */
  private Object _slaveJVMLock = new Object();

  /** Main constructor.  
   *  @param m the GlobalModel that is the source of documents for this CompilerModel
   */
  public DefaultCompilerModel(GlobalModel m) {
    _model = m;
    _compilerErrorModel = new CompilerErrorModel(new CompilerError[0], _model);
    _workDir = _model.getWorkingDirectory();
  }
  
  //--------------------------------- Locking -------------------------------//
  
  /** Returns the lock used to prevent simultaneous compilation and JUnit testing */
  public Object getSlaveJVMLock() { return _slaveJVMLock; }

  //-------------------------- Listener Management --------------------------//

  /** Add a CompilerListener to the model.
   *  @param listener a listener that reacts to compiler events
   * 
   *  This operation is synchronized by the readers/writers protocol in EventNotifier<T>.
   */
  public void addListener(CompilerListener listener) { _notifier.addListener(listener); }

  /** Remove a CompilerListener from the model.  If the listener is not currently
   *  listening to this model, this method has no effect.
   *  @param listener a listener that reacts to compiler events
   * 
   *  This operation is synchronized by the readers/writers protocol in EventNotifier<T>.
   */
  public void removeListener(CompilerListener listener) { _notifier.removeListener(listener); }

  /** Removes all CompilerListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }

  //-------------------------------- Triggers --------------------------------//


  /** Compiles all open documents, after ensuring that all are saved.  If drjava is in project mode when this method is
   *  called, only the project files are saved.  We do not require external files (files not belonging to the project) 
   *  to be saved before we compile the files.  In project mode, we perform the compilation with the specified build 
   *  directory if defined in the project state.<p>
   *  This method formerly only compiled documents which were out of sync with their class file, as a performance 
   *  optimization.  However, bug #634386 pointed out that unmodified files could depend on modified files, in which 
   *  case this command would not recompile a file in some situations when it should.  Since we value correctness over
   *  performance, we now always compile all open documents.</p>
   *  @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compileAll() throws IOException {
    
    boolean isProjActive = _model.isProjectActive();
    
    List<OpenDefinitionsDocument> defDocs = _model.getOpenDefinitionsDocuments();
    
//    System.err.println("Docs to compile: " + defDocs);
    
    if (isProjActive) {
      // If we're in project mode, filter out only the documents that are in the project and leave 
      // out the external files.
      List<OpenDefinitionsDocument> projectDocs = new LinkedList<OpenDefinitionsDocument>();
      
      for (OpenDefinitionsDocument doc : defDocs) {
        if (doc.inProjectPath() || doc.isAuxiliaryFile()) projectDocs.add(doc);
      }
      defDocs = projectDocs;
    }
    compile(defDocs);
  }
  
//  //TODO: compileAll(roots,files), compile(docs), and compile(doc) contain very similar code;
//  //  they should be refactored into one core routine and three adaptations (instantiations?)
//  
//  /** Compiles all files with the specified source root set.  
//   *  @param sourceRootSet a list of source roots
//   *  @param filesToCompile a list of files to compile
//   */
//  public void compileAll(List<File> sourceRootSet, List<File> filesToCompile) throws IOException {
// 
//    List<OpenDefinitionsDocument> defDocs;
//    
//    defDocs = _model.getOpenDefinitionsDocuments(); 
//    
////    System.err.println("Docs to compile: " + defDocs);
//    
//    // Only compile if all are saved
//    if (_hasModifiedFiles(defDocs)) _notifier.saveBeforeCompile();
//    
//    // Check for modified project files, in case they didn't save when prompted. If any files haven't been saved
//    // after we told our listeners to do so, don't proceed with the rest of the compile.
//    if (_hasModifiedFiles(defDocs)) return;
//    
//    // Get sourceroots and all files
//    _rawCompile(sourceRootSet.toArray(new File[0]), filesToCompile.toArray(new File[0]), new File[0]);
//  }
  
  /** Compiles all documents in the specified list of OpenDefinitionsDocuments. */
  public void compile(List<OpenDefinitionsDocument> defDocs) throws IOException {
    
//    System.err.println("compile(" + defDocs + ") called");
    
    // Only compile if all are saved
    if (_hasModifiedFiles(defDocs)) _notifier.saveBeforeCompile();
    // check for modified project files, in case they didn't save when prompted
    if (_hasModifiedFiles(defDocs)) return;
    // if any files haven't been saved after we told our
    // listeners to do so, don't proceed with the rest
    // of the compile.
    
    // Get sourceroots and all files
    ArrayList<File> filesToCompile = new ArrayList<File>();
    ArrayList<File> excludedFiles = new ArrayList<File>();
    
    File f;
    
    for (OpenDefinitionsDocument doc : defDocs) {
      if (doc.isSourceFile()) {
        filesToCompile.add(doc.getFile());
        doc.setCachedClassFile(null); // clear cached class file
      }
      else excludedFiles.add(doc.getFile());
    } 
    
//    System.err.println("Filtered list of docs to compile: " + filesToCompile);
    
    _rawCompile(getSourceRootSet(), filesToCompile.toArray(new File[0]), excludedFiles.toArray(new File[0]));
  }
  
  /** Starts compiling the specified source document.  Demands that the definitions be saved before proceeding
   *  with the compile. If the compile can proceed, a compileStarted event is fired which guarantees that a 
   *  compileEnded event will be fired when the compile finishes or fails.  If the compilation succeeds, then 
   *  a call is made to resetInteractions(), which fires an event of its own, contingent on the conditions.  
   *  If the current package as determined by getSourceRoot(String) and getPackageName() is invalid, 
   *  compileStarted and compileEnded will fire, and an error will be put in compileErrors.
   *
   *  (The Interactions pane is not reset if the _resetAfterCompile field is set to false; this features is used
   *  in some test cases to make them more efficient.)
   *
   *  @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compile(OpenDefinitionsDocument doc) throws IOException {
    
    List<OpenDefinitionsDocument> defDocs;
    defDocs = _model.getOpenDefinitionsDocuments(); 
    
    // Only compile if all docs are saved.  In project mode, untitled docs are ignored.
    if (_hasModifiedFiles(defDocs)) _notifier.saveBeforeCompile();
    if (_hasModifiedFiles(defDocs)) return;  /* Abort compilation */
    
    // In project mode, untitled files are ignored; check if doc is untitled. 
    if (doc.isUntitled()) {
      _notifier.saveUntitled();
      if (doc.isUntitled()) return;
    }
    
    File f = doc.getFile();
    File[] files, excludedFiles;
    
    if (endsWithExt(f, EXTENSIONS)) {
      files = new File[]{f};
      excludedFiles = new File[0];
    }
    else {
      files = new File[0];
      excludedFiles = new File[]{f};
    }
    doc.setCachedClassFile(null); // clear cached class file
    _rawCompile(new File[] { doc.getSourceRoot() }, files, excludedFiles); 
  }
  
  private void _rawCompile(File[] sourceRoots, File[] files, File[] excludedFiles) throws IOException {
    
//    Utilities.show("_rawCompile(" + Arrays.toString(sourceRoots) + ", " + 
//                   Arrays.toString(files) + ", " + Arrays.toString(excludedFiles) + ")");
    
    File buildDir = _model.getBuildDirectory();
    File workDir = _model.getWorkingDirectory();
     
//    System.err.println("sourceRoots are: " + Arrays.toString(sourceRoots));
//    System.err.println("sourceFiles are: " + Arrays.toString(files));
//    System.err.println("BuildDir is: " + buildDir);
    
    _notifier.compileStarted();
    try {
      // Compile the files
      _compileFiles(sourceRoots, files, buildDir);
    }
    catch (Throwable t) {
      CompilerError err = new CompilerError(t.toString(), false);
      CompilerError[] errors = new CompilerError[] { err };
      _distributeErrors(errors);
    }
    finally { _notifier.compileEnded(workDir, excludedFiles); }
  }
  

  //-------------------------------- Helpers --------------------------------//

  /**
   * Converts JExprParseExceptions thrown by the JExprParser in language levels to
   * CompilerErrors.
   */
  private LinkedList<CompilerError> _parseExceptions2CompilerErrors(LinkedList<JExprParseException> pes) {
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    Iterator<JExprParseException> iter = pes.iterator();
    while (iter.hasNext()) {
      JExprParseException pe = iter.next();
      errors.addLast(new CompilerError(pe.getFile(), pe.currentToken.beginLine-1, pe.currentToken.beginColumn-1, pe.getMessage(), false));
    }
    return errors;
  }
  
  /**
   * Converts errors thrown by the language level visitors to CompilerErrors.
   */
  private LinkedList<CompilerError> _visitorErrors2CompilerErrors(LinkedList<Pair<String, JExpressionIF>> visitorErrors) {
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    Iterator<Pair<String, JExpressionIF>> iter = visitorErrors.iterator();
    while (iter.hasNext()) {
      Pair<String, JExpressionIF> pair = iter.next();
      String message = pair.getFirst();      
//      System.out.println("Got error message: " + message);
      JExpressionIF jexpr = pair.getSecond();
      
      SourceInfo si;
      if (jexpr == null) si = JExprParser.NO_SOURCE_INFO;
      else si = pair.getSecond().getSourceInfo();
      
      errors.addLast(new CompilerError(si.getFile(), si.getStartLine()-1, si.getStartColumn()-1, message, false));
    }
    return errors;
  }
  /** Compile the given files (with the given sourceroots), and update the model with any errors that result.  Does 
   *  not notify listeners; use compileAll or compile instead.  All public compile methods delegate to this one so this 
   *  method is the only one that uses synchronization to prevent compiling and unit testing at the same time.
   * 
   * @param sourceRoots An array of all sourceroots for the files to be compiled
   * @param files An array of all files to be compiled
   * @param buildDir the output directory for all the .class files.
   *        null means output to the same directory as the source file
   * 
   */
  private void _compileFiles(File[] sourceRoots, File[] files, File buildDir) throws IOException {

//    CompilerError[] errors = new CompilerError[0];
    
//    System.err.println("Compiling files: " + Arrays.toString(files) + " to " + buildDir);
      
    Pair<LinkedList<JExprParseException>, LinkedList<Pair<String, JExpressionIF>>> errors;
    LinkedList<JExprParseException> parseExceptions;

    LinkedList<Pair<String, JExpressionIF>> visitorErrors;
    LinkedList<CompilerError> compilerErrors = new LinkedList<CompilerError>();
    CompilerInterface compiler = CompilerRegistry.ONLY.getActiveCompiler();
    
    /* Canonicalize buildDir */
    if (buildDir != null) buildDir = FileOps.getCanonicalFile(buildDir);

    compiler.setBuildDirectory(buildDir);
    ClassPathVector extraClassPath = new ClassPathVector();
    if (_model.isProjectActive()) 
      extraClassPath.addAll(_model.getExtraClassPath());
//    Utilities.showDebug("extra class path is: " + extraClasspath);
    for (File f : DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)) extraClassPath.add(f);
    
//    Utilities.showDebug("Extra classpath passed to compiler: " + extraClasspath.toString());
    compiler.setExtraClassPath(extraClassPath);
    if (files.length > 0) {
//      if (DrJava.getConfig().getSetting(OptionConstants.LANGUAGE_LEVEL) == DrJava.ELEMENTARY_LEVEL) {
      LanguageLevelConverter llc = new LanguageLevelConverter(getActiveCompiler().getName());
//      System.err.println(getActiveCompiler().getName());
      /* Language level files are moved to another file, copied back in augmented form to be compiled.  This
       * compiled version is also copied to another file with the same path with the ".augmented" suffix on the 
       * end.  We have to copy the original back to its original spot so the user doesn't have to do anything funny.
       */
//      Utilities.showDebug("Getting ready to call LL converter on " + Arrays.toString(files));
      errors = llc.convert(files);
//      Utilities.showDebug("Conversion complete");
      
      compiler.setWarningsEnabled(true);
      
      /* Rename any .dj0 files in files to be .java files, so the correct thing is compiled.  The hashset is used to 
       * make sure we never send in duplicate files. This can happen if the java file was sent in along with the 
       * corresponding .dj* file. The dj* file is renamed to a .java file and thus we have two of the same file in 
       * the list.  By adding the renamed file to the hashset, the hashset efficiently removes duplicates.
      */
      HashSet<File> javaFileSet = new HashSet<File>();
      for (File f : files) {
        File canonicalFile;
        try { canonicalFile = f.getCanonicalFile(); } 
        catch(IOException e) { canonicalFile = f.getAbsoluteFile(); }
        String fileName = canonicalFile.getPath();
        int lastIndex = fileName.lastIndexOf(".dj");
        if (lastIndex != -1) {
          /** If compiling a language level file, do not show warnings, as these are not caught by the language level parser */
          compiler.setWarningsEnabled(false);
          javaFileSet.add(new File(fileName.substring(0, lastIndex) + ".java"));
        }
        else javaFileSet.add(canonicalFile);
      }
      files = javaFileSet.toArray(new File[javaFileSet.size()]);
        
      parseExceptions = errors.getFirst();
      compilerErrors.addAll(_parseExceptions2CompilerErrors(parseExceptions));
      visitorErrors = errors.getSecond();
      compilerErrors.addAll(_visitorErrors2CompilerErrors(visitorErrors));
      CompilerError[] compilerErrorsArray = null;
      
      compilerErrorsArray = compilerErrors.toArray(new CompilerError[compilerErrors.size()]);

      /** Compile the files in specified sourceRoots and files */
    
      if (compilerErrorsArray.length == 0) 
        synchronized(_slaveJVMLock) { compilerErrorsArray = compiler.compile(sourceRoots, files); }

      _distributeErrors(compilerErrorsArray);
    }
    else _distributeErrors(new CompilerError[0]);
  }
  
  /** Determines if file f ends with one of the extensions in exts. */
  private static boolean endsWithExt(File f, String[] exts) {
    for (String ext: exts) { if (f.getName().endsWith(ext)) return true; }
    return false;
  }

  /** Sorts the given array of CompilerErrors and divides it into groups
   *  based on the file, giving each group to the appropriate
   *  OpenDefinitionsDocument, opening files if necessary. 
   */
  private void _distributeErrors(CompilerError[] errors) throws IOException {
    resetCompilerErrors();  // Why is this done?
    _compilerErrorModel = new CompilerErrorModel(errors, _model);
  }

  /** Gets an array of all sourceRoots for the open definitions documents, without duplicates. Note that if any of the 
   *  open documents has an invalid package statement, it won't be added to the source root set.
   */
  public File[] getSourceRootSet() {
    List<OpenDefinitionsDocument> defDocs = _model.getOpenDefinitionsDocuments();
    return getSourceRootSet(defDocs);
  }
  
  /** Constructs an array of all sourceRoots for the list of open DefinitionsDocuments,
   *  without duplicates. Note that if any of the open documents has an invalid package 
   *  statement, it won't be added to the source root set.
   *  @param defDocs the list of OpenDefinitionsDocuments to process.
   */
  public static File[] getSourceRootSet(List<OpenDefinitionsDocument> defDocs) {
    
    LinkedList<File> roots = new LinkedList<File>();

    for (int i = 0; i < defDocs.size(); i++) {
      OpenDefinitionsDocument doc = defDocs.get(i);

      try {
        File root = doc.getSourceRoot();
        if (root == null) continue;
        // Don't add duplicate Files, based on path
        if (! roots.contains(root)) { roots.add(root); }
      }
      catch (InvalidPackageException e) {
        // invalid package statement for this one; suppress adding it to roots
      }
    }

    return roots.toArray(new File[roots.size()]);
  }

  /** This method would normally be called from the getter; however, with the introduction of projects, the 
   *  list of files that may not be modified is not known.  We pull the relevant documents from the getter 
   *  and instead of calling it from there, we check it from here.
   *  Modified, untitled documents are ignored in project mode.
   *  @param defDocs the list of documents to check
   *  @return whether any of the given documents are modified
   */
  protected boolean _hasModifiedFiles(List<OpenDefinitionsDocument> defDocs) {
    boolean isProjActive = _model.isProjectActive();
    for (OpenDefinitionsDocument doc : defDocs) {
      if (doc.isModifiedSinceSave() && ( ! isProjActive || ! doc.isUntitled())) return true;
    }
    return false;
  }
  
  //----------------------------- Error Results -----------------------------//

  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel getCompilerErrorModel() { return _compilerErrorModel; }

  /** Gets the total number of current errors. */
  public int getNumErrors() { return getCompilerErrorModel().getNumErrors(); }
  
  /** Gets the total number of current compiler errors. */
  public int getNumCompErrors() { return getCompilerErrorModel().getNumCompErrors(); }
  
  /** Gets the total number of current warnings. */  
  public int getNumWarnings() { return getCompilerErrorModel().getNumWarnings(); }

  /** Resets the compiler error state to have no errors. */
  public void resetCompilerErrors() {
    // TODO: see if we can get by without this function
    _compilerErrorModel = new CompilerErrorModel(new CompilerError[0], _model);
  }

  //-------------------------- Compiler Management --------------------------//

  /**
   * Returns all registered compilers that are actually available.
   * That is, for all elements in the returned array, .isAvailable()
   * is true.
   * This method will never return null or a zero-length array.
   * Instead, if no compiler is registered and available, this will return
   * a one-element array containing an instance of
   * {@link NoCompilerAvailable}.
   *
   * @see CompilerRegistry#getAvailableCompilers
   */
  public CompilerInterface[] getAvailableCompilers() {
    return CompilerRegistry.ONLY.getAvailableCompilers();
  }

  /**
   * Gets the compiler that is the "active" compiler.
   *
   * @see #setActiveCompiler
   * @see CompilerRegistry#getActiveCompiler
   */
  public CompilerInterface getActiveCompiler() {
    return CompilerRegistry.ONLY.getActiveCompiler();
  }

  /**
   * Sets which compiler is the "active" compiler.
   *
   * @param compiler Compiler to set active.
   *
   * @see #getActiveCompiler
   * @see CompilerRegistry#setActiveCompiler
   */
  public void setActiveCompiler(CompilerInterface compiler) {
    CompilerRegistry.ONLY.setActiveCompiler(compiler);
  }
}
