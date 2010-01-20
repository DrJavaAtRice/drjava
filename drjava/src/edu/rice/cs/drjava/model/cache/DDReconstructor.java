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

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentListener;
import edu.rice.cs.drjava.model.FileMovedException;

/** The Reconstructor is a closure that builds a document.  This class is used by the DocumentCache to load 
  * DefinitionsDocuments lazily from files on disk.
  */
public interface DDReconstructor {
  
  /** @return a new DefinitionsDocument */
  public DefinitionsDocument make() throws IOException, BadLocationException, FileMovedException;
  
  /** Saves information (like cursor location, highlight, etc.) from the DefinitionsDocument before the cache deletes it
    * so that those pieces of info can be restored when reconstructing the DefinitionsDocument again.
    * @param doc the DefinitionsDocument whose data needs saving
    */
  public void saveDocInfo(DefinitionsDocument doc);
  
  /** Sets a document listener to be added to the definitions document when it is created
    * @param dl the listener to add to the document
    */
  public void addDocumentListener(DocumentListener dl);
  
  /** Returns the string text for document that has been kicked out; null otherwise. */
  public String getText();
}
