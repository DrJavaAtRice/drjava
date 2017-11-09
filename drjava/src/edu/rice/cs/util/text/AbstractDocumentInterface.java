/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util.text;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
//import javax.swing.text.Document;
import javax.swing.text.Position;

/** An interface designed to augment the Swing Document interface with useful operations including operations like
  * append that  cannot be safely implemented in terms of the methods provided by the Document interface.  
  * Operations like append require write locking the document which cannot be done using the Document interface.  */
public interface AbstractDocumentInterface { 
  
  /* Methods from swing Document interface used in FindReplaceMachine */
  
  /* Returns the length of the document. */
  public int getLength();
  
  /* Returns the specified substring of the document. */
  public String getText(int offset, int length) throws BadLocationException;
  
  /* Returns the entire text of this document. */
  public String getText();
  
  /* Inserts given string with specified attributes at the specified offset. */
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException;
  
  /* Removes the substring of specified length at the specified offset. */
  public void remove(int offset, int length) throws BadLocationException;
  
  /** Creates a "sticky" position within a document 
   * @param offs the offset at which to create the sticky position
   * @return the newly-created position
   * @throws BadLocationException if attempts to reference an invalid location
   */
  public Position createPosition(int offs) throws BadLocationException;
  
  /* Methods not in swing Document interface. */
    
  /** Appends given string with specified attributes to end of this document. 
   * @param str the string to be appended
   * @param set the set of attributes for str
   */
  public void append(String str, AttributeSet set);
}
