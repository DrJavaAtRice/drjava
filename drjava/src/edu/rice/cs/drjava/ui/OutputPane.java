package edu.rice.cs.drjava.ui;

import  javax.swing.JTextPane;
import  javax.swing.text.AttributeSet;
import  javax.swing.text.BadLocationException;
import  javax.swing.text.StyleContext;
import  javax.swing.text.StyleConstants;
import  javax.swing.event.DocumentListener;
import  javax.swing.event.DocumentEvent;
import  java.awt.Color;
import  java.awt.Rectangle;
import  java.io.PrintStream;

import edu.rice.cs.drjava.model.GlobalModel;

/**
 * The view component to which System.out and System.err is redirected
 * when DrJava is run.
 *
 * @version $Id$
 */
public class OutputPane extends JTextPane {
  private class ScrollToEndDocumentListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
      try {
        Rectangle endPos = modelToView(getDocument().getLength());
        // If the window is not on the screen, this will be null
        // In that case, don't try to scroll!
        if (endPos != null) {
          scrollRectToVisible(endPos);
        }
      } catch (BadLocationException willNeverHappenISwear) {}
    }

    public void removeUpdate(DocumentEvent e) {}
    public void changedUpdate(DocumentEvent e) {}
  }

  public OutputPane(final GlobalModel model) {
    super(model.getConsoleDocument());
    
    // user can't edit this thing!
    setEditable(false);
    // when we make the view uneditable, it no longer scrolls when text is
    // added. To get around this we wrote this listener to scroll on output.
    // Unfortunately it slows the output window down. Maybe there's a better
    // way?
    getDocument().addDocumentListener(new ScrollToEndDocumentListener());
  }
}
