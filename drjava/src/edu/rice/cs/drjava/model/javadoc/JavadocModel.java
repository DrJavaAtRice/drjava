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

package edu.rice.cs.drjava.model.javadoc;

import java.io.File;
import java.io.IOException;

import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.util.DirectorySelector;

/**
 * Model interface for Javadoc integration features.
 * Note: Implementors should have a constructor taking an IGetDocuments.
 * @version $Id: JavadocModel.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public interface JavadocModel {
  /** Name for the suggested destination directory to be placed in the
   * source root of one of the open documents.  (Value is "doc".)
   */
  public static final String SUGGESTED_DIR_NAME = "doc";
  
  /** {@code true} iff the classes to run javadoc are available */
  public boolean isAvailable();
  
  /** Add a JavadocListener to the model.
   * @param listener a listener that reacts to Javadoc events
   */
  public void addListener(JavadocListener listener);
  
  /** Remove a JavadocListener from the model.  If the listener is not currently
   * listening to this model, this method has no effect.
   * @param listener a listener that reacts to Javadoc events
   */
  public void removeListener(JavadocListener listener);
  
  /** Removes all JavadocListeners from this model. */
  public void removeAllListeners();
  
  /** Accessor for the Javadoc error model. */
  public CompilerErrorModel getJavadocErrorModel();
  
  /** Clears all current Javadoc errors. */
  public void resetJavadocErrors();
  
  /** Suggests a default location for generating Javadoc, based on the given
   * document's source root.  (Appends JavadocModel.SUGGESTED_DIR_NAME to
   * the sourceroot.)
   * @param doc Document with the source root to use as the default.
   * @return Suggested destination directory, or null if none could be
   * determined.
   */
  public File suggestJavadocDestination(OpenDefinitionsDocument doc);
  
  /** Javadocs all open documents, after ensuring that all are saved.
    * The user provides a destination, and the gm provides the package info.
    * 
    * @param select a command object for selecting a directory and warning a user
    *        about bad input
    * @param saver a command object for saving a document (if it moved/changed)
    * 
    * @throws IOException if there is a problem manipulating files
    */
  public void javadocAll(DirectorySelector select, FileSaveSelector saver) throws IOException;
  
  /** Generates Javadoc for the given document only, after ensuring it is saved.
    * Saves the output to a temporary directory, which is provided in the
    * javadocEnded event on the provided listener.
    * 
    * @param doc Document to generate Javadoc for
    * @param saver a command object for saving the document (if it moved/changed)
    * 
    * @throws IOException if there is a problem manipulating files
    */
  public void javadocDocument(OpenDefinitionsDocument doc, FileSaveSelector saver) throws IOException;
}
