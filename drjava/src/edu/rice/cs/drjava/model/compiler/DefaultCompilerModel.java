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

// TODO: should this be in the compiler package?
package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.IGetDocuments;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import javax.swing.*;
import java.lang.reflect.*;
import edu.rice.cs.util.UnexpectedException;
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

  /**
   * returns file extensions of files types that we can compile
   */
  private String[] getCompilableExtensions(){
    return new String[]{".java", ".dj0", ".dj1", ".dj2"};
  }
  
  /**
   * Manages listeners to this model.
   */
  private final CompilerEventNotifier _notifier = new CompilerEventNotifier();

  /**
   * Used by CompilerErrorModel to open documents that have errors.
   */
  private final IGetDocuments _getter;

  /**
   * The error model containing all current compiler errors.
   */
  private CompilerErrorModel<? extends CompilerError> _compilerErrorModel;

  /**
   * Lock to prevent multiple threads from accessing the compiler at the
   * same time.
   */
//  private Object _compilerLock = this;

  /**
   * Main constructor.
   * @param getter Source of documents for this CompilerModel
   */
  public DefaultCompilerModel(IGetDocuments getter) {
    _getter = getter;
    _compilerErrorModel =
      new CompilerErrorModel<CompilerError>(new CompilerError[0], getter);
  }

  //-------------------------- Listener Management --------------------------//

  /**
   * Add a CompilerListener to the model.
   * @param listener a listener that reacts to compiler events
   */
  public void addListener(CompilerListener listener) {
    _notifier.addListener(listener);
  }

  /**
   * Remove a CompilerListener from the model.  If the listener is not currently
   * listening to this model, this method has no effect.
   * @param listener a listener that reacts to compiler events
   */
  public void removeListener(CompilerListener listener) {
    _notifier.removeListener(listener);
  }

  /**
   * Removes all CompilerListeners from this model.
   */
  public void removeAllListeners() {
    _notifier.removeAllListeners();
  }

  //-------------------------------- Triggers --------------------------------//


  /**
   * Compiles all open documents, after ensuring that all are saved.
   * If drjava is in project mode when this method is called, only 
   * the project files are saved.  Also, at this point, we do not require
   * external files (files not belonging to the project) to be saved
   * before we compile the files.
   * 
   * <p>In project mode, since only project files are compiled, we
   * perform the compilation with the specified build directory if 
   * defined in the project state.</p>
   *
   * <p>This method used to only compile documents which were out of sync
   * with their class file, as a performance optimization.  However,
   * bug #634386 pointed out that unmodified files could depend on
   * modified files, in which case this would not recompile a file in
   * some situations when it should.  Since we value correctness over
   * performance, we now always compile all open documents.</p>
   * @throws IOException if a filesystem-related problem prevents compilation
   */
  synchronized public void compileAll() throws IOException {
    //System.out.println("Running compile all");
    List<OpenDefinitionsDocument> defDocs =
      _getter.getDefinitionsDocuments();
    
    File buildDir = null;
    if (_getter.getFileGroupingState().isProjectActive()) {

      // If we're in project mode, filter out only the 
      // documents that are in the project and leave out
      // the external files.
      List<OpenDefinitionsDocument> projectDocs =
        new LinkedList<OpenDefinitionsDocument>();
    
      for(OpenDefinitionsDocument odd : defDocs){
        if(odd.isInProjectPath() || odd.isAuxiliaryFile()){
            projectDocs.add(odd);
        }
      }
      //System.out.println("Project is active");
      defDocs = projectDocs;
    }

    compile(defDocs);
  }
  
  /**
   * compiles all files with the specified source root set
   * @param sourceRootSet, a list of source roots
   * @param filesToCompile a list of files to compile
   */
  synchronized public void compileAll(List<File> sourceRootSet, List<File> filesToCompile) throws IOException {
    File buildDir = null;
    if (_getter.getFileGroupingState().isProjectActive()) {
      buildDir = _getter.getFileGroupingState().getBuildDirectory();
    }
    List<OpenDefinitionsDocument> defDocs = _getter.getDefinitionsDocuments();
    // Only compile if all are saved
    if (_hasModifiedFiles(defDocs)) {
      //System.out.println("Has modified files");
      _notifier.saveBeforeCompile();
    }

    // check for modified project files, in case they didn't save when prompted
    if (_hasModifiedFiles(defDocs)) {
      // if any files haven't been saved after we told our
      // listeners to do so, don't proceed with the rest
      // of the compile.
    }
    else {

      // Get sourceroots and all files
      File[] sourceRoots = sourceRootSet.toArray(new File[0]);;
      File[] files = filesToCompile.toArray(new File[0]);

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
      finally {
        // Fire a compileEnded event
        _notifier.compileEnded();
      }
    }
  }

  
  /**
   * compiles all documents in the list of opendefinitionsdocuments sent as input
   */
  synchronized public void compile(List<OpenDefinitionsDocument> defDocs) throws IOException {
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
    if (_hasModifiedFiles(defDocs)) {
      // if any files haven't been saved after we told our
      // listeners to do so, don't proceed with the rest
      // of the compile.
    }
    else {

      // Get sourceroots and all files
      File[] sourceRoots = getSourceRootSet();
      ArrayList<File> filesToCompile = new ArrayList<File>();

      File f;
      String[] exts = getCompilableExtensions();
      boolean okToAdd;
      for (int i = 0; i < defDocs.size(); i++) {
        OpenDefinitionsDocument doc = defDocs.get(i);
        try {
          f = doc.getFile();
          okToAdd = false;
          for(String ext: exts){
            if(f.getName().endsWith(ext)){
              okToAdd = true;
            }
          }
          if(okToAdd){
            filesToCompile.add(f);
          }
        }
        catch (IllegalStateException ise) {
          // No file for this document; skip it
        }
      }
      File[] files = filesToCompile.toArray(new File[0]);

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
      finally {
        // Fire a compileEnded event
        _notifier.compileEnded();
      }
    }
  }  
  
  /**
   * Starts compiling the source.  Demands that the definitions be
   * saved before proceeding with the compile. If the compile can
   * proceed, a compileStarted event is fired which guarantees that
   * a compileEnded event will be fired when the compile finishes or
   * fails.  If the compilation succeeds, then a call is
   * made to resetInteractions(), which fires an
   * event of its own, contingent on the conditions.  If the current
   * package as determined by getSourceRoot(String) and getPackageName()
   * is invalid, compileStarted and compileEnded will fire, and
   * an error will be put in compileErrors.
   *
   * (Interactions are not reset if the _resetAfterCompile field is
   * set to false, which allows some test cases to run faster.)
   *
   * @throws IOException if a filesystem-related problem prevents compilation
   */
  synchronized public void compile(OpenDefinitionsDocument doc)
      throws IOException {
    File buildDir = null;
    if (doc.isInProjectPath() || doc.isAuxiliaryFile()) {
      buildDir = _getter.getFileGroupingState().getBuildDirectory();
    }
    
    // Only compile if all are saved
    if (_getter.hasModifiedDocuments()) {
      _notifier.saveBeforeCompile();
    }

    if (_getter.hasModifiedDocuments()) {
      // if any files haven't been saved after we told our
      // listeners to do so, don't proceed with the rest
      // of the compile.
    }
    else {
      try {
        File file = doc.getFile();
        File[] files = new File[] { file };

        try {
          _notifier.compileStarted();

          File[] sourceRoots = new File[] { doc.getSourceRoot() };

          _compileFiles(sourceRoots, files, buildDir);
        }
        catch (Throwable e) {
          CompilerError err =
            new CompilerError(file, -1, -1, e.getMessage(), false);
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
  }

  //-------------------------------- Helpers --------------------------------//

  /**
   * Converts ParseExceptions thrown by the JExprParser in language levels to
   * CompilerErrors.
   */
  private LinkedList<CompilerError> _parseExceptions2CompilerErrors(LinkedList<ParseException> pes) {
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    Iterator<ParseException> iter = pes.iterator();
    while(iter.hasNext()) {
      ParseException pe = iter.next();
      errors.addLast(new CompilerError(pe.file, pe.currentToken.beginLine-1, pe.currentToken.beginColumn-1, pe.getMessage(), false));
    }
    return errors;
  }
  
  /**
   * Converts errors thrown by the language level visitors to CompilerErrors.
   */
  private LinkedList<CompilerError> _visitorErrors2CompilerErrors(LinkedList<Pair<String, JExpression>> visitorErrors) {
    LinkedList<CompilerError> errors = new LinkedList<CompilerError>();
    Iterator<Pair<String, JExpression>> iter = visitorErrors.iterator();
    while (iter.hasNext()) {
      Pair<String, JExpression> pair = iter.next();
      String message = pair.getFirst();      
//      System.out.println("Got error message: " + message);
      JExpression jexpr = pair.getSecond();
      SourceInfo si;
      if (jexpr == null) {
        si = JExprParser.NO_SOURCE_INFO;
      }
      else {
        si = pair.getSecond().getSourceInfo();
      }
      errors.addLast(new CompilerError(si.getFile(), si.getStartLine()-1, si.getStartColumn()-1, message, false));
    }
    return errors;
  }
  /**
   * Compile the given files (with the given sourceroots), and update
   * the model with any errors that result.  Does not notify listeners;
   * use compileAll or doc.startCompile instead.
   * @param sourceRoots An array of all sourceroots for the files to be compiled
   * @param files An array of all files to be compiled
   * @param buildDir the output directory for all the .class files.
   *        null means output to the same directory as the source file
   */
  protected void _compileFiles(File[] sourceRoots, File[] files, File buildDir) throws IOException {

//    CompilerError[] errors = new CompilerError[0];
    Pair<LinkedList<ParseException>, LinkedList<Pair<String, JExpression>>> errors;
    LinkedList<ParseException> parseExceptions;
    LinkedList<Pair<String, JExpression>> visitorErrors;
      LinkedList<CompilerError> compilerErrors = new LinkedList<CompilerError>();
    CompilerInterface compiler
      = CompilerRegistry.ONLY.getActiveCompiler();

    compiler.setBuildDirectory(buildDir);
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
      errors = llc.convert(files);//, filesToRestore);
      
      compiler.setWarningsEnabled(true);
      
      /**Rename any .dj0 files in files to be .java files, so the correct thing is compiled.*/
      for (int i = 0; i<files.length; i++) {
        String fileName = files[i].getAbsolutePath();
        int lastIndex = fileName.lastIndexOf(".dj");
        if (lastIndex != -1) {
          /** If compiling a language level file, do not show warnings, as these are not caught by the language level parser */
          compiler.setWarningsEnabled(false);
          files[i]=new File(fileName.substring(0, lastIndex) + ".java");
        }
      }
      parseExceptions = errors.getFirst();
      compilerErrors.addAll(_parseExceptions2CompilerErrors(parseExceptions));
      visitorErrors = errors.getSecond();
      compilerErrors.addAll(_visitorErrors2CompilerErrors(visitorErrors));
//      }
//      System.out.println("Got back " + errors.length + " errors");
      CompilerError[] compilerErrorsArray = (CompilerError[]) compilerErrors.toArray(new CompilerError[0]);
      if (compilerErrorsArray.length == 0) {
        compilerErrorsArray = compiler.compile(sourceRoots, files);
      }
//      Iterator<File> iter = filesToRestore.iterator();
//      while (iter.hasNext()) {
//        _getter.getDocumentForFile(iter.next()).revertFile();
//      }
      _distributeErrors(compilerErrorsArray);
      // Restore the files that were moved.
//      Iterator<File> iter = filesToRestore.iterator();
//      while (iter.hasNext()) {
//        File f = iter.next();
//        File sourceFile = new File(f.getAbsolutePath() + ".beginner");
//        // Windows needs this since otherwise rename won't work.
//        if (f.exists()) {
//          f.delete();
//        }
//        sourceFile.renameTo(f);
//      }
    }
    else {
      _distributeErrors(new CompilerError[0]);
    }
  }

  /**
   * Sorts the given array of CompilerErrors and divides it into groups
   * based on the file, giving each group to the appropriate
   * OpenDefinitionsDocument, opening files if necessary.
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
    List<OpenDefinitionsDocument> defDocs =
      _getter.getDefinitionsDocuments();
    return getSourceRootSet(defDocs);
  }
  
  /**
   * gets an array of all sourceRoots for the list of open definitions
   * documents, without duplicates. Note that if any of hte open
   * documents has an invalid package statement, it won't be  added
   * to the source root set.
   * @param defDocs the list of OpenDefinitionsDocuments to find
   * the source roots of
   */
  public File[] getSourceRootSet(List<OpenDefinitionsDocument> defDocs){
    LinkedList<File> roots = new LinkedList<File>();

    for (int i = 0; i < defDocs.size(); i++) {
      OpenDefinitionsDocument doc = defDocs.get(i);

      try {
        File root = doc.getSourceRoot();

        // Don't add duplicate Files, based on path
        if (!roots.contains(root)) {
          roots.add(root);
        }
      }
      catch (InvalidPackageException e) {
        // oh well, invalid package statement for this one
        // can't add it to roots
      }/*catch(RuntimeException){
       * Adam look here
       * }*/
    }

    return roots.toArray(new File[0]);
  }

  /**
   * This method would normally be called from the getter; however, 
   * with the introduction of projects, the list of files that may not
   * be modified is not known.  We pull the relevant documents from
   * the getter and instead of calling it from there, we check it from
   * here.
   * @param defDocs the list of documents to check
   * @return whether any of the given documents are modified
   */
  protected boolean _hasModifiedFiles(List<OpenDefinitionsDocument> defDocs) {
    boolean hasModifiedFiles = false;
    for(OpenDefinitionsDocument odd : defDocs){
      hasModifiedFiles |= odd.isModifiedSinceSave();
    }
    return hasModifiedFiles;
  }
  
  //----------------------------- Error Results -----------------------------//

  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel<? extends CompilerError> getCompilerErrorModel() { return _compilerErrorModel; }

  /** Gets the total number of current errors. */
  public int getNumErrors() { return getCompilerErrorModel().getNumErrors(); }

  /** Resets the compiler error state to have no errors. */
  public void resetCompilerErrors() {
    // TODO: see if we can get by without this function
    _compilerErrorModel =
      new CompilerErrorModel<CompilerError>(new CompilerError[0], _getter);
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
