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

package edu.rice.cs.util.swing;

import  java.io.OutputStream;
import  javax.swing.text.Document;
import  javax.swing.text.BadLocationException;
import  javax.swing.text.AttributeSet;

/**
 * An extension of {@link OutputStream} that writes its output to
 * an implementation of {@link Document}.
 *
 * @version $Id$
 */
public class DocumentOutputStream extends OutputStream {
  private Document _doc;
  private AttributeSet _attributes;

  /**
   * Constructs an {@link OutputStream} that writes its output to a
   * {@link Document}.
   *
   * When this constructor is used, all insertions to the Document will
   * be done with the attributes set to <code>null</code>.
   *
   * @param doc Document to write output to.
   */
  public DocumentOutputStream(Document doc) {
    this(doc, null);
  }

  /**
   * Constructs an {@link OutputStream} that writes its output to a
   * {@link Document}.
   *
   * @param doc Document to write output to.
   * @param attributes Attributes to use for inserting text into the document
   *                   that is sent to this stream.
   */
  public DocumentOutputStream(Document doc, AttributeSet attributes) {
    _doc = doc;
    _attributes = attributes;
  }

  /**
   * Write a character to the stream.
   * @param c the ASCII value of the character to write.
   */
  public void write(int c) {
    try {
      _doc.insertString(_doc.getLength(), String.valueOf((char)c), _attributes);
    } catch (BadLocationException canNeverHappen) {
      throw  new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }

  /**
   * Write an array of characters (bytes) to the stream at a particular offset.
   * @param b characters to write to stream
   * @param off start of writing
   * @param len number of characters to write from b
   */
  public void write(byte[] b, int off, int len) {
    try {
      _doc.insertString(_doc.getLength(), new String(b, off, len), _attributes);
    } catch (BadLocationException canNevenHappen) {
      throw  new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }
}
