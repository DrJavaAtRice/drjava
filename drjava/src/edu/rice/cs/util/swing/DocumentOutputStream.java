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

package edu.rice.cs.util.swing;

import  java.io.OutputStream;
import  javax.swing.text.Document;
import  javax.swing.text.BadLocationException;
import  javax.swing.text.AttributeSet;
import  javax.swing.text.StyleConstants.ColorConstants;


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
