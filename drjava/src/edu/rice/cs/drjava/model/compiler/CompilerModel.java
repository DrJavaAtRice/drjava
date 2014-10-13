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
import java.util.List;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;

/** Interface for all compiler functionality in the model.  The compilation process itself can be monitored through
  * the CompilerListener interface.  The four primary uses of this interface will be to manage listeners, to trigger
  * compilation of (a) document(s), to handle the results, and to manage available compilers.
  * 
  * @version $Id: CompilerModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public interface CompilerModel {
  //----------------------------Locking--------------------------------------//
  
  /** Returns the lock used to prevent simultaneous compilation and JUnit testing */
  public Object getCompilerLock();
  
  //-------------------------- Listener Management --------------------------//
  
  /** Add a CompilerListener to the model.
    * @param listener a listener that reacts to compiler events
    */
  public void addListener(CompilerListener listener);
  
  /** Remove a CompilerListener from the model.  If the listener is not currently listening to this model, this method
    * has no effect.
    * @param listener a listener that reacts to compiler events
    */
  public void removeListener(CompilerListener listener);
  
  /** Removes all CompilerListeners from this model. */
  public void removeAllListeners();
  
  //-------------------------------- Triggers --------------------------------//
  
  /** Compiles all documents, which requires that the documents be saved first.
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compileAll() throws IOException;
  
  /** Compiles all documents in the project source tree, which requires that the documents be saved first.
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compileProject() throws IOException;
  
  /** Compiles the specified documents which must be saved first.
    * @param docs the documents to be compiled
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compile(List<OpenDefinitionsDocument> docs) throws IOException;
  
  /** Compiles a single document which must be saved first.
    * @param doc the document to be compiled
    * @throws IOException if a filesystem-related problem prevents compilation
    */
  public void compile(OpenDefinitionsDocument doc) throws IOException;
  
  //----------------------------- Error Results -----------------------------//
  
  /** Gets the CompilerErrorModel representing the last compile. */
  public CompilerErrorModel getCompilerErrorModel();
  
  /** Gets the total number of current errors. */
  public int getNumErrors();
  
  /** Resets the compiler error state to have no errors. */
  public void resetCompilerErrors();
  
  //-------------------------- Compiler Management --------------------------//
  
  /** Returns all registered compilers that are actually available.  If there are none,
   * the result is {@link NoCompilerAvailable#ONLY}.
   */
  public Iterable<CompilerInterface> getAvailableCompilers();
  
  /** Gets the compiler that is the "active" compiler.
   *
   * @see #setActiveCompiler
   */
  public CompilerInterface getActiveCompiler(); 
  
  /** Sets which compiler is the "active" compiler.
    *
    * @param compiler Compiler to set active.
    * @throws IllegalArgumentException  If the compiler is not in the list of available compilers
    *
    * @see #getActiveCompiler
    */
  public void setActiveCompiler(CompilerInterface compiler);
  
  /** Gets the current build directory. */
  public File getBuildDir();

  /* The following method is no longer used. */
//  /** Add a compiler to the active list */
//  public void addCompiler(CompilerInterface compiler);
  
//  /** Gets the LanguageLevelStackTraceMapper from the model */
//  public LanguageLevelStackTraceMapper getLLSTM();
}
