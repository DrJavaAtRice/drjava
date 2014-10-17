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

package edu.rice.cs.util.swing;

import  java.io.OutputStream;
import  javax.swing.text.Document;
import  javax.swing.text.BadLocationException;
import  javax.swing.text.AttributeSet;

/** An extension of {@link OutputStream} that writes its output to
  * an implementation of {@link Document}.
  * @version $Id: DocumentOutputStream.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class DocumentOutputStream extends OutputStream {
  private Document _doc;
  private AttributeSet _attributes;
  
  /** Constructs an {@link OutputStream} that writes its output to a {@link Document}.
    * When this constructor is used, all insertions to the Document will
    * be done with the attributes set to <code>null</code>.
    * @param doc Document to write output to.
    */
  public DocumentOutputStream(Document doc) {
    this(doc, null);
  }
  
  /** Constructs an {@link OutputStream} that writes its output to a {@link Document}.
    * @param doc Document to write output to.
    * @param attributes Attributes to use for inserting text into the document that is sent to this stream.
    */
  public DocumentOutputStream(Document doc, AttributeSet attributes) {
    _doc = doc;
    _attributes = attributes;
  }
  
  /** Writes a character to the stream.
    * @param c the ASCII value of the character to write.
    */
  public void write(int c) {
    try { _doc.insertString(_doc.getLength(), String.valueOf((char)c), _attributes); } 
    catch (BadLocationException canNeverHappen) {
      throw  new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }
  
  /** Writes an array of characters (bytes) to the stream at a particular offset.
    * @param b characters to write to stream
    * @param off start of writing
    * @param len number of characters to write from b
    */
  public void write(byte[] b, int off, int len) {
    try { _doc.insertString(_doc.getLength(), new String(b, off, len), _attributes); } 
    catch (BadLocationException canNevenHappen) {
      throw  new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }
}
