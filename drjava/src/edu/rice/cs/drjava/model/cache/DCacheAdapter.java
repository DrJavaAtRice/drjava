/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.cache;

import java.io.IOException;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;

/** A lightweight wrapper type for DefinitionsDocuments that may or may not be resident in memory.  An instance of this
  * The wrapper includes a copy of the String text of the document if it has been kicked out of the cache.
  */
public interface DCacheAdapter {
  
  /** Retrieves the document for its corresponding ODD
    * @return the definitions document for the corresponding ODD
    */
  public DefinitionsDocument getDocument() throws IOException, FileMovedException;
  
  public int getLength();
  
  /* Gets the entire text of this document. */
  public String getText();
  
  /* Gets the specified substring of this document.
   * @throws an IndexOutOfBounds exception if the specification is ill-formed. 
   */
  public String getText(int offset, int length) throws BadLocationException;
  
  /** Checks whether the document is ready to be returned.  If false, then the document would have to be
    * loaded from disk when getDocument() is called.  
    * @return if the document is already loaded
    */
  public boolean isReady();
  
  /** Closes the corresponding document for this adapter. */
  public void close();
  
  /** Adds a DocumentListener to the reconstructor. */
  public void addDocumentListener(DocumentListener l);
  
  /* Method for notifying the DCacheAdapter that this document has been saved to a file. */
  public void documentSaved();
  
  /* Method for notifying the DCacheAdapter that this document has been modified. */
  public void documentModified();
  
  /* Method for notifying the DCacheAdapter that this document has been reset via undo commands. */
  public void documentReset();
}