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
import edu.rice.cs.drjava.model.IGetDocuments;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
/**
 * Default implementation all compiler functionality in the model, as specified
 * in the CompilerModel interface.  This is the implementation that is used in
 * most circumstances during normal use (as opposed to test-specific purposes).
 *
 * @version $Id$
 */
public class DefaultCompilerModel implements CompilerModel {

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
  private CompilerErrorModel<CompilerError> _compilerErrorModel;

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
   *
   * This method used to only compile documents which were out of sync
   * with their class file, as a performance optimization.  However,
   * bug #634386 pointed out that unmodified files could depend on
   * modified files, in which case this would not recompile a file in
   * some situations when it should.  Since we value correctness over
   * performance, we now always compile all open documents.
   * @throws IOException if a filesystem-related problem prevents compilation
   */
  synchronized public void compileAll() throws IOException {
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
      List<OpenDefinitionsDocument> defDocs =
        _getter.getDefinitionsDocuments();

      // Get sourceroots and all files
      File[] sourceRoots = getSourceRootSet();
      ArrayList<File> filesToCompile = new ArrayList<File>();

      for (int i = 0; i < defDocs.size(); i++) {
        OpenDefinitionsDocument doc = defDocs.get(i);
        try {
          filesToCompile.add(doc.getFile());
        }
        catch (IllegalStateException ise) {
          // No file for this document; skip it
        }
      }
      File[] files = filesToCompile.toArray(new File[0]);

      _notifier.compileStarted();

      try {
        // Compile the files
        _compileFiles(sourceRoots, files);
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
        File file = doc.getDocument().getFile();
        File[] files = new File[] { file };

        try {
          _notifier.compileStarted();

          File[] sourceRoots = new File[] { doc.getSourceRoot() };

          _compileFiles(sourceRoots, files);
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
   * Compile the given files (with the given sourceroots), and update
   * the model with any errors that result.  Does not notify listeners;
   * use compileAll or doc.startCompile instead.
   * @param sourceRoots An array of all sourceroots for the files to be compiled
   * @param files An array of all files to be compiled
   */
  protected void _compileFiles(File[] sourceRoots, File[] files) throws IOException {

    CompilerError[] errors = new CompilerError[0];

    CompilerInterface compiler
      = CompilerRegistry.ONLY.getActiveCompiler();

    if (files.length > 0) {
      errors = compiler.compile(sourceRoots, files);
    }
    _distributeErrors(errors);
  }

  /**
   * Sorts the given array of CompilerErrors and divides it into groups
   * based on the file, giving each group to the appropriate
   * OpenDefinitionsDocument, opening files if necessary.
   */
  private void _distributeErrors(CompilerError[] errors) {
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
      }
    }

    return roots.toArray(new File[0]);
  }

  //----------------------------- Error Results -----------------------------//

  /**
   * Gets the CompilerErrorModel representing the last compile.
   */
  public CompilerErrorModel getCompilerErrorModel() {
    return _compilerErrorModel;
  }

  /**
   * Gets the total number of current errors.
   */
  public int getNumErrors() {
    return getCompilerErrorModel().getNumErrors();
  }

  /**
   * Resets the compiler error state to have no errors.
   */
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
