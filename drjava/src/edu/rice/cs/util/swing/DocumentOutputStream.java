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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import  java.io.OutputStream;
import  javax.swing.text.Document;
import  javax.swing.text.BadLocationException;
import  javax.swing.text.AttributeSet;
import  javax.swing.text.StyleConstants.ColorConstants;


/**
 * Output stream for documents.  Used for file I/O.
 * @version $Id$
 */
public class DocumentOutputStream extends OutputStream {
  private Document _doc;
  private AttributeSet _attributes;

  /**
   * Constructor.
   * @param     Document doc
   */
  public DocumentOutputStream(Document doc) {
    this(doc, null);
  }

  /**
   * Constructor.
   * @param     Document doc
   * @param     AttributeSet attributes
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
