package  edu.rice.cs.drjava;

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