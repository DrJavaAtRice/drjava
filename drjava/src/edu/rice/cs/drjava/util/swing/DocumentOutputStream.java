package  edu.rice.cs.drjava;

import  java.io.OutputStream;
import  javax.swing.text.Document;
import  javax.swing.text.BadLocationException;
import  javax.swing.text.AttributeSet;
import  javax.swing.text.StyleConstants.ColorConstants;


/**
 * @version $Id$
 */
public class DocumentOutputStream extends OutputStream {
  private Document _doc;
  private AttributeSet _attributes;

  /**
   * put your documentation comment here
   * @param     Document doc
   */
  public DocumentOutputStream(Document doc) {
    this(doc, null);
  }

  /**
   * put your documentation comment here
   * @param     Document doc
   * @param     AttributeSet attributes
   */
  public DocumentOutputStream(Document doc, AttributeSet attributes) {
    _doc = doc;
    _attributes = attributes;
  }

  /**
   * put your documentation comment here
   * @param c
   */
  public void write(int c) {
    try {
      _doc.insertString(_doc.getLength(), String.valueOf((char)c), _attributes);
    } catch (BadLocationException canNeverHappen) {
      throw  new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }

  /**
   * put your documentation comment here
   * @param b
   * @param off
   * @param len
   */
  public void write(byte[] b, int off, int len) {
    try {
      _doc.insertString(_doc.getLength(), new String(b, off, len), _attributes);
    } catch (BadLocationException canNevenHappen) {
      throw  new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }
}



