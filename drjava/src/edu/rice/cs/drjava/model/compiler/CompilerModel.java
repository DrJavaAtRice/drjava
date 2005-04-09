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

import java.io.IOException;
import java.io.File;
import java.util.List;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/**
 * Interface for all compiler functionality in the model.  The compilation
 * process itself can be monitored through the CompilerListener interface.
 * The four primary uses of this interface will be to manage listeners, to
 * trigger compilation of (a) document(s), to handle the results, and to manage
 * available compilers.
 * 
 * @version $Id$
 */
public interface CompilerModel {
  
  //-------------------------- Listener Management --------------------------//
  
  /**
   * Add a CompilerListener to the model.
   * @param listener a listener that reacts to compiler events
   */
  public void addListener(CompilerListener listener);

  /**
   * Remove a CompilerListener from the model.  If the listener is not currently
   * listening to this model, this method has no effect.
   * @param listener a listener that reacts to compiler events
   */
  public void removeListener(CompilerListener listener);

  /**
   * Removes all CompilerListeners from this model.
   */
  public void removeAllListeners();
  
  //-------------------------------- Triggers --------------------------------//
  
  /**
   * Compiles all documents with the active compiler.
   * This normally requires that the documents be saved first.
   * @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compileAll() throws IOException;
  
  /**
   * compiles all files with the specified source root set
   * @param sourceroots a list of source roots
   * @param files a list of files to compile
   */
  public void compileAll(List<File> sourceroots, List<File> files) throws IOException ;
  
  /**
   * Compiles a single document with the active compiler.
   * This normally requires that the document be saved first.
   * @param doc the document to be compiled
   * @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compile(List<OpenDefinitionsDocument> doc) throws IOException;
  
  /**
   * Compiles a single document with the active compiler.
   * This normally requires that the document be saved first.
   * @param doc the document to be compiled
   * @throws IOException if a filesystem-related problem prevents compilation
   */
  public void compile(OpenDefinitionsDocument doc) throws IOException;
  
  //----------------------------- Error Results -----------------------------//
  
  /**
   * Gets the CompilerErrorModel representing the last compile.
   */
  public CompilerErrorModel<? extends CompilerError> getCompilerErrorModel();  // Return type should be CompilerErrorModel<? extends CompilerError> /**/

  /**
   * Gets the total number of current errors.
   */
  public int getNumErrors();
  
  /**
   * Resets the compiler error state to have no errors.
   */
  public void resetCompilerErrors();
  
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
  public CompilerInterface[] getAvailableCompilers();

  /**
   * Gets the compiler is the "active" compiler.
   *
   * @see #setActiveCompiler
   * @see CompilerRegistry#getActiveCompiler
   */
  public CompilerInterface getActiveCompiler(); 

  /**
   * Sets which compiler is the "active" compiler.
   *
   * @param compiler Compiler to set active.
   *
   * @see #getActiveCompiler
   * @see CompilerRegistry#setActiveCompiler
   */
  public void setActiveCompiler(CompilerInterface compiler);
}