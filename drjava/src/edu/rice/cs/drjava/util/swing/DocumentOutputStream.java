/* $Id$ */

package edu.rice.cs.drjava;

import java.io.OutputStream;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants.ColorConstants;

public class DocumentOutputStream extends OutputStream {
  private Document _doc;
  private AttributeSet _attributes;
  
  public DocumentOutputStream(Document doc) {
    this(doc, null);
  }

  public DocumentOutputStream(Document doc, AttributeSet attributes) {
    _doc = doc;
    _attributes = attributes;
  }
  
  public void write(int c) {
    try {
      _doc.insertString(_doc.getLength(), String.valueOf((char)c), _attributes);
    }
    catch(BadLocationException canNeverHappen) {
      throw new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }

  public void write(byte[] b, int off, int len) {
    try {
      _doc.insertString(_doc.getLength(), new String(b, off, len), _attributes);
    }
    catch(BadLocationException canNevenHappen) {
      throw new RuntimeException("Internal error: bad location in OutputWindowStream");
    }
  }
}

