/*BEGIN_COPYRIGHT_BLOCK
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
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.cache;

import java.io.IOException;

import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/** A lightweight wrapper type for DefinitionsDocuments that may or may not be resident in memory.
 *  An object of within OpenDefinitionsDocument holds the DefinitionsDocument associated with the
 *  OpenDefinitionsDocument.
 */
public interface DCacheAdapter {
  
  /** Retrieves the document for its corresponding ODD
   *  @return the definitions document for the corresponding ODD
   */
  public DefinitionsDocument getDocument() throws IOException, FileMovedException;
  
  /** Checks whether the document is ready to be returned.  If false, then the document would have to be
   *  loaded from disk when getDocument() is called.  
   *  @return if the document is already loaded
   */
  public boolean isReady();
  
  /** Closes the corresponding document for this adapter. */
  public void close();
  
  public DDReconstructor getReconstructor();
  
  /* Method for notifying the DCacheAdapter that this document has been saved to a file. */
  public void documentSaved(String filename);
  
  /* Method for notifying the DCacheAdapter that this document has been modified. */
  public void documentModified();
  
  /* Method for notifying the DCacheAdapter that this document has been reset via undo commands. */
  public void documentReset();
  
}