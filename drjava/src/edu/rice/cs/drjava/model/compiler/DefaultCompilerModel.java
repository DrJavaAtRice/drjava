/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.IGetDocuments;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import javax.swing.*;
import edu.rice.cs.util.swing.*;
import java.lang.reflect.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.ClasspathVector;
import edu.rice.cs.javalanglevels.*;
import edu.rice.cs.javalanglevels.parser.*;
import edu.rice.cs.javalanglevels.tree.*;


/**
 * Default implementation all compiler functionality in the model, as specified
 * in the CompilerModel interface.  This is the implementation that is used in
 * most circumstances during normal use (as opposed to test-specific purposes).
 *
 * @version $Id$
 */
public class DefaultCompilerModel implements CompilerModel {

  /** Returns file extensions of files types that we can compile. */
  private String[] getCompilableExtensions() { return new String[]{".java", ".dj0", ".dj1", ".dj2"}; }
  
  /** Manages listeners to this model. */
  private final CompilerEventNotifier _notifier = new CompilerEventNotifier();

  /** Used by CompilerErrorModel to open documents that have errors. 
   *  A reference to the global model. */
  private final IGetDocuments _getter;

  /** The error model containing all current compiler errors. */
  private CompilerErrorModel<? extends CompilerError> _compilerErrorModel;

  /** Main constructor.  
   *  @param getter Source of documents for this CompilerModel
   */
  public DefaultCompilerModel(IGetDocuments getter) {
    _getter = getter;
    _compilerErrorModel = new CompilerErrorModel<CompilerError>(new CompilerError[0], getter);
  }

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


  /** Compiles all open documents, after ensuring that all are saved.
   *  If drjava is in project mode when this method is called, only 
   *  the project files are saved.  Also, at this point, we do not require
   *  external files (files not belonging to the project) to be saved
   *  before we compile the files.
   *  
   *  <p>In project mode, since only project files are compiled, we
   *  perform the compilation with the specified build directory if 
   *  defined in the project state.</p>
   *
   *  <p>This method used to only compile documents which were out of sync
   *  with their class file, as a performance optimization.  However,
   *  bug #634386 pointed out that unmodified files could depend on
   *  modified files, in which case this would not recompile a file in
   *  some situations when it should.  Since we value correctness over
   *  performance, we now always compile all open documents.</p>
   *  @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compileAll() throws IOException {
    
    boolean isProjActive = _getter.getFileGroupingState().isProjectActive();
    
    //System.out.println("Running compile all");
    List<OpenDefinitionsDocument> defDocs = _getter.getOpenDefinitionsDocuments();
    
    if (isProjActive) {
      // If we're in project mode, filter out only the documents that are in the project and leave 
      // out the external files.
      List<OpenDefinitionsDocument> projectDocs = new LinkedList<OpenDefinitionsDocument>();
      
      for (OpenDefinitionsDocument doc : defDocs) {
        if (doc.isInProjectPath() || doc.isAuxiliaryFile()) projectDocs.add(doc);
      }
      //System.out.println("Project is active");
      defDocs = projectDocs;
    }
    
    compile(defDocs);
  }
  
  //TODO: compileAll(roots,files), compile(docs), and compile(doc) contain very similar code;
  //  they should be refactored into one core routine and three adaptations (instantiations?)
  
  /**
   * Compiles all files with the specified source root set.  
   * @param sourceRootSet a list of source roots
   * @param filesToCompile a list of files to compile
   */
  public void compileAll(List<File> sourceRootSet, List<File> filesToCompile) throws IOException {
    
    File buildDir = null;
   
    //ScrollableDialog sd1 = new ScrollableDialog(null, "DefaultCompilerModel.compileAll called", "", "");
    //sd1.show();
    
    if (_getter.getFileGroupingState().isProjectActive()) 
      buildDir = _getter.getFileGroupingState().getBuildDirectory();
    List<OpenDefinitionsDocument> defDocs;
    
    defDocs = _getter.getOpenDefinitionsDocuments(); 
    
    // Only compile if all are saved
    if (_hasModifiedFiles(defDocs)) {
      //System.out.println("Has modified files");
      //ScrollableDialog sd2 = new ScrollableDialog(null, "_hasModifiedFiles(...) returned true!", "", "");
      //sd2.show();
      _notifier.saveBeforeCompile();
    }
    
    // Check for modified project files, in case they didn't save when prompted.
    // If any files haven't been saved after we told our listeners to do so, 
    // don't proceed with the rest of the compile.
    if (_hasModifiedFiles(defDocs)) return;
    
//    ScrollableDialog sd3 = new ScrollableDialog(null, "DefaultCompilerModel.compileAll(...,...) has finished file saving", "","");
//    sd3.show();
    
    // Get sourceroots and all files
    File[] sourceRoots = sourceRootSet.toArray(new File[0]);;
    File[] files = filesToCompile.toArray(new File[0]);
    
//    ScrollableDialog sd4 = new ScrollableDialog(null, "Ready to invoke compileStarted() event on _notifier [" + _notifier + "]", "", "");
//    sd4.show();
    
    _notifier.compileStarted();
    
//    ScrollableDialog sd5 = new ScrollableDialog(null, "compileStarted() event successfully invoked ", "", "");
//    sd5.show();
    
    try { _compileFiles(sourceRoots, files, buildDir); }
    catch (Throwable t) {
      CompilerError err = new CompilerError(t.toString(), false);
      CompilerError[] errors = new CompilerError[] { err };
      _distributeErrors(errors);
    }
    finally { _notifier.compileEnded(); }
  }
  
  /** Compiles all documents in the list of opendefinitionsdocuments sent as input. */
  public void compile(List<OpenDefinitionsDocument> defDocs) throws IOException {
    
    File buildDir = null;
    
    if (_getter.getFileGroupingState().isProjectActive()) {
      buildDir = _getter.getFileGroupingState().getBuildDirectory();
    }
    
    // Only compile if all are saved
    if (_hasModifiedFiles(defDocs)) {
      //System.out.println("Has modified files");
      _notifier.saveBeforeCompile();
    }
    
    
    // check for modified project files, in case they didn't save when prompted
    if (_hasModifiedFiles(defDocs)) return;
    // if any files haven't been saved after we told our
    // listeners to do so, don't proceed with the rest
    // of the compile.
    
    // Get sourceroots and all files
    File[] sourceRoots = getSourceRootSet();
    ArrayList<File> filesToCompile = new ArrayList<File>();
    
    File f;
    String[] exts = getCompilableExtensions();
    for (OpenDefinitionsDocument doc : defDocs) {
      try {
        f = doc.getFile();
        if (endsWithExt(f, exts)) filesToCompile.add(f);
      }
      catch (IllegalStateException ise) {
        // No file for this document; skip it
      }
    }
    File[] files = filesToCompile.toArray(new File[filesToCompile.size()]);
    
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
    finally { _notifier.compileEnded(); }
  }
  
  /** Starts compiling the specified source document.  Demands that the definitions be saved before proceeding
   *  with the compile. If the compile can proceed, a compileStarted event is fired which guarantees that a 
   *  compileEnded event will be fired when the compile finishes or fails.  If the compilation succeeds, then 
   *  a call is made to resetInteractions(), which fires an event of its own, contingent on the conditions.  
   *  If the current package as determined by getSourceRoot(String) and getPackageName() is invalid, 
   *  compileStarted and compileEnded will fire, and an error will be put in compileErrors.
   *
   *  (Interactions are not reset if the _resetAfterCompile field is set to false, which allows some test cases 
   *  to run faster.)
   *
   *  @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compile(OpenDefinitionsDocument doc) throws IOException {
    File buildDir = null;
    
    if (doc.isInProjectPath() || doc.isAuxiliaryFile()) {
      buildDir = _getter.getFileGroupingState().getBuildDirectory();
    }
    
    List<OpenDefinitionsDocument> defDocs;
    defDocs = _getter.getOpenDefinitionsDocuments(); 
    
    // Only compile if all are saved
    if (_hasModifiedFiles(defDocs)) _notifier.saveBeforeCompile();
    
    if (_hasModifiedFiles(defDocs)) return;  /* Abort compilation */
    
    try {
      File file = doc.getFile();
      File[] files = new File[] { file };
      
      try {
        _notifier.compileStarted();
        File[] sourceRoots = new File[] { doc.getSourceRoot() };
        _compileFiles(sourceRoots, files, buildDir);
      }
      catch (Throwable e) {
        CompilerError err = new CompilerError(file, -1, -1, e.getMessage(), false);
        CompilerError[] errors = new CompilerError[] { err };
        _distributeErrors(errors);
      }
      finally {
        // Fire a compileEnded event
        _notifier.compileEnded();
      }
    }
    catch (IllegalStateException ise) {
      // No file exists, don't try to compile
    }
  }

  //-------------------------------- Helpers --------------------------------//

  /**
   * Converts ParseExceptions thrown by the JExprParser in language levels to
   * CompilerErrors.
   */
  private LinkedList<CompilerError> _parseExceptions2CompilerErrors(LinkedList<ParseException> pes) {
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    Iterator<ParseException> iter = pes.iterator();
    while (iter.hasNext()) {
      ParseException pe = iter.next();
      errors.addLast(new CompilerError(pe.file, pe.currentToken.beginLine-1, pe.currentToken.beginColumn-1, pe.getMessage(), false));
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
  /**
   * Compile the given files (with the given sourceroots), and update
   * the model with any errors that result.  Does not notify listeners;
   * use compileAll or compile instead.  All public compile methods delegate
   * to this one so this method is the only one that is synchronized to prevent
   * compiling and unit testing at the same time.
   * 
   * @param sourceRoots An array of all sourceroots for the files to be compiled
   * @param files An array of all files to be compiled
   * @param buildDir the output directory for all the .class files.
   *        null means output to the same directory as the source file
   * 
   */
  private synchronized void _compileFiles(File[] sourceRoots, File[] files, File buildDir) throws IOException {

//    CompilerError[] errors = new CompilerError[0];
    
//    ScrollableDialog sd1 = new ScrollableDialog(null, "DefaultCompilerModel._compileFiles called with args " + sourceRoots + " " + files + " " + buildDir, "", "");
//    sd1.show();
    
    Pair<LinkedList<ParseException>, LinkedList<Pair<String, JExpressionIF>>> errors;
    LinkedList<ParseException> parseExceptions;
    LinkedList<Pair<String, JExpressionIF>> visitorErrors;
    LinkedList<CompilerError> compilerErrors = new LinkedList<CompilerError>();
    CompilerInterface compiler = CompilerRegistry.ONLY.getActiveCompiler();

    compiler.setBuildDirectory(buildDir);
    ClasspathVector extraClasspath = new ClasspathVector();
    if (_getter.getFileGroupingState().isProjectActive()) 
      extraClasspath.addAll(_getter.getFileGroupingState().getExtraClasspath());
    for (File f : DrJava.getConfig().getSetting(OptionConstants.EXTRA_CLASSPATH)) {
      extraClasspath.add(f);
    }
//    System.out.println("Extra classpath passed to compiler: " + extraClasspath.toString());
    compiler.setExtraClassPath(extraClasspath);
    if (files.length > 0) {
//      if (DrJava.getConfig().getSetting(OptionConstants.LANGUAGE_LEVEL) == DrJava.ELEMENTARY_LEVEL) {
      LanguageLevelConverter llc = new LanguageLevelConverter(getActiveCompiler().getName());
      // Language level files are moved to another file, copied back
      // in augmented form to be compiled.  This compiled version
      // is also copied to another file with the same path with the 
      // ".augmented" suffix on the end.
      // We have to copy the original back to its original spot so the
      // user doesn't have to do anything funny.
      //      LinkedList<File> filesToRestore = new LinkedList<File>();
      //      System.out.println("Calling convert!");
      
//      ScrollableDialog sd2 = new ScrollableDialog(null, "Ready to call file converter " + llc + " in DefaultCompilerModel", "", "");
//      sd2.show();
      errors = llc.convert(files);//, filesToRestore);
      
//      ScrollableDialog sd3 = new ScrollableDialog(null, "Files successfully converted in DefaultCompilerModel", "", "");
//      sd3.show();
      
      compiler.setWarningsEnabled(true);
      
      /**Rename any .dj0 files in files to be .java files, so the correct thing is compiled.*/
      // The hashset is used to make sure we never send in duplicate files. This can happen if
      // the java file was sent in allong with the corresponding .dj* file. The dj* file
      // is renamed to a .java file and thus we have two of the same file in the list.  By
      // adding the renamed file to the hashset, the hashset efficiently removes duplicates.
      HashSet<File> javaFileSet = new HashSet<File>();
      for (File f : files) {
        File canonicalFile;
        try {
          canonicalFile = f.getCanonicalFile();
        } catch(IOException e) {
          canonicalFile = f.getAbsoluteFile();
        }
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
//      }
//      System.out.println("Got back " + errors.length + " errors");
      CompilerError[] compilerErrorsArray = (CompilerError[]) compilerErrors.toArray(new CompilerError[compilerErrors.size()]);
      
      /** Compile the files in specified sourceRoots and files */
      if (compilerErrorsArray.length == 0) compilerErrorsArray = compiler.compile(sourceRoots, files);

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
  private void _distributeErrors(CompilerError[] errors)
      throws IOException {
    resetCompilerErrors();

    _compilerErrorModel = new CompilerErrorModel<CompilerError>(errors, _getter);
  }

  /**
   * Gets an array of all sourceRoots for the open definitions
   * documents, without duplicates. Note that if any of the open
   * documents has an invalid package statement, it won't be added
   * to the source root set.
   */
  public File[] getSourceRootSet() {
    List<OpenDefinitionsDocument> defDocs = _getter.getOpenDefinitionsDocuments();
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

        // Don't add duplicate Files, based on path
        if (!roots.contains(root)) { roots.add(root); }
      }
      catch (InvalidPackageException e) {
        // oh well, invalid package statement for this one
        // can't add it to roots
      }/*catch(RuntimeException) {
       * Adam look here
       * }*/
    }

    return roots.toArray(new File[roots.size()]);
  }

  /** This method would normally be called from the getter; however, with the introduction of projects, the 
   *  list of files that may not be modified is not known.  We pull the relevant documents from the getter 
   *  and instead of calling it from there, we check it from here.
   *  @param defDocs the list of documents to check
   *  @return whether any of the given documents are modified
   */
  protected static boolean _hasModifiedFiles(List<OpenDefinitionsDocument> defDocs) {
    for (OpenDefinitionsDocument doc : defDocs) {
      if (doc.isModifiedSinceSave()) return true;  // Not all documents must be inspected
    }
    return false;
  }
  
  //----------------------------- Error Results -----------------------------//

  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel<? extends CompilerError> getCompilerErrorModel() { return _compilerErrorModel; }

  /** Gets the total number of current errors. */
  public int getNumErrors() { return getCompilerErrorModel().getNumErrors(); }

  /** Resets the compiler error state to have no errors. */
  public void resetCompilerErrors() {
    // TODO: see if we can get by without this function
    _compilerErrorModel = new CompilerErrorModel<CompilerError>(new CompilerError[0], _getter);
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
