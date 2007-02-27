/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.javadoc;

import java.io.File;
import edu.rice.cs.drjava.model.GlobalModel;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.compiler.CompilerErrorModel;
import edu.rice.cs.drjava.model.compiler.CompilerError;
import edu.rice.cs.util.DirectorySelector;

/**
 * Javadoc model to use when javadoc is unavailable.
 * @version $Id: JavadocModel.java 3901 2006-06-30 05:28:11Z rcartwright $
 */
public class NoJavadocAvailable implements JavadocModel {
  
  private final JavadocEventNotifier _notifier = new JavadocEventNotifier();
  private final CompilerErrorModel _javadocErrorModel;
  
  public NoJavadocAvailable(GlobalModel model) {
    CompilerError e = new CompilerError("The javadoc feature is not available.", false);
    _javadocErrorModel = new CompilerErrorModel(new CompilerError[]{e}, model);
  }
  
  public boolean isAvailable() { return false; }


  /** Add a JavadocListener to the model.
   *  @param listener a listener that reacts to Javadoc events
   */
  public void addListener(JavadocListener listener) { _notifier.addListener(listener); }

  /** Remove a JavadocListener from the model.  If the listener is not currently
   *  listening to this model, this method has no effect.
   *  @param listener a listener that reacts to Javadoc events
   */
  public void removeListener(JavadocListener listener) { _notifier.removeListener(listener); }

  /** Removes all JavadocListeners from this model. */
  public void removeAllListeners() { _notifier.removeAllListeners(); }
  

  /** Accessor for the Javadoc error model. */
  public CompilerErrorModel getJavadocErrorModel() { return _javadocErrorModel; }
  
  /** Clears all current Javadoc errors. */
  public void resetJavadocErrors() { /* ignore */ }
  
  /**
   * Suggests a default location for generating Javadoc, based on the given
   * document's source root.  (Appends JavadocModel.SUGGESTED_DIR_NAME to
   * the sourceroot.)
   * @param doc Document with the source root to use as the default.
   * @return Suggested destination directory, or null if none could be
   * determined.
   */
  public File suggestJavadocDestination(OpenDefinitionsDocument doc) { return null; }
  
  /**
   * Javadocs all open documents, after ensuring that all are saved.
   * The user provides a destination, and the gm provides the package info.
   * 
   * @param select a command object for selecting a directory and warning a user
   *        about bad input
   * @param saver a command object for saving a document (if it moved/changed)
   * 
   * @throws IOException if there is a problem manipulating files
   */
  public void javadocAll(DirectorySelector select, FileSaveSelector saver) {
    _notifier.javadocStarted();
    _notifier.javadocEnded(false, null, true);
  }
  
  /**
   * Generates Javadoc for the given document only, after ensuring it is saved.
   * Saves the output to a temporary directory, which is provided in the
   * javadocEnded event on the provided listener.
   * 
   * @param doc Document to generate Javadoc for
   * @param saver a command object for saving the document (if it moved/changed)
   * 
   * @throws IOException if there is a problem manipulating files
   */
  public void javadocDocument(OpenDefinitionsDocument doc, FileSaveSelector saver) {
    _notifier.javadocStarted();
    _notifier.javadocEnded(false, null, true);
  }
  
}
