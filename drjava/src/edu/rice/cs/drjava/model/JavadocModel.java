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

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.IOException;

import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.definitions.InvalidPackageException;
import java.util.List;

/**
 * Model interface for Javadoc integration features.
 * Note: Implementors should have a constructor taking an IGetDocuments.
 * @version $Id$
 */
public interface JavadocModel {
  /**
   * Name for the suggested destination directory to be placed in the
   * source root of one of the open documents.  (Value is "doc".)
   */
  public static final String SUGGESTED_DIR_NAME = "doc";
  
  /**
   * Add a JavadocListener to the model.
   * @param listener a listener that reacts to Javadoc events
   */
  public void addListener(JavadocListener listener);

  /**
   * Remove a JavadocListener from the model.  If the listener is not currently
   * listening to this model, this method has no effect.
   * @param listener a listener that reacts to Javadoc events
   */
  public void removeListener(JavadocListener listener);

  /**
   * Removes all JavadocListeners from this model.
   */
  public void removeAllListeners();
  
  /**
   * Accessor for the Javadoc error model.
   */
  public CompilerErrorModel getJavadocErrorModel();
  
  /**
   * Clears all current Javadoc errors.
   */
  public void resetJavadocErrors();
  
  /**
   * Suggests a default location for generating Javadoc, based on the given
   * document's source root.  (Appends JavadocModel.SUGGESTED_DIR_NAME to
   * the sourceroot.)
   * @param doc Document with the source root to use as the default.
   * @return Suggested destination directory, or null if none could be
   * determined.
   */
  public File suggestJavadocDestination(OpenDefinitionsDocument doc);
  
  /**
   * Javadocs all open documents, after ensuring that all are saved.
   * The user provides a destination, and the gm provides the package info.
   * 
   * @param select a command object for selecting a directory and warning a user
   *        about bad input
   * @param saver a command object for saving a document (if it moved/changed)
   * @param classpath a collection of classpath elements to be used by Javadoc
   * 
   * @throws IOException if there is a problem manipulating files
   */
  public void javadocAll(DirectorySelector select, FileSaveSelector saver,
                         String classpath)
    throws IOException;
  
  /**
   * Generates Javadoc for the given document only, after ensuring it is saved.
   * Saves the output to a temporary directory, which is provided in the
   * javadocEnded event on the provided listener.
   * 
   * @param doc Document to generate Javadoc for
   * @param saver a command object for saving the document (if it moved/changed)
   * @param classpath a collection of classpath elements to be used by Javadoc
   * 
   * @throws IOException if there is a problem manipulating files
   */
  public void javadocDocument(final OpenDefinitionsDocument doc,
                              final FileSaveSelector saver,
                              final String classpath)
    throws IOException;
}
