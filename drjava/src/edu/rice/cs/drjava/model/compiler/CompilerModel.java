/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

// TODO: should this be in the compiler package?
package edu.rice.cs.drjava.model.compiler;

import java.io.IOException;
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
  public CompilerErrorModel getCompilerErrorModel();

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