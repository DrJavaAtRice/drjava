/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.awt.Color;
import java.awt.Rectangle;

import java.io.PrintStream;

public class OutputView extends JTextPane
{
  private PrintStream _out;
  private PrintStream _err;
	private PrintStream _realOut;
	private PrintStream _realErr;

  private class ScrollToEndDocumentListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
      try {
        Rectangle endPos = modelToView(getDocument().getLength());
        scrollRectToVisible(endPos);
      }
      catch (BadLocationException willNeverHappenISwear) {}
    }

    public void removeUpdate(DocumentEvent e) {
    }

    public void changedUpdate(DocumentEvent e) {
    }
  }

  public OutputView() {
    // user can't edit this thing!
    setEditable(false);

    // when we make the view uneditable, it no longer scrolls when text is
    // added. To get around this we wrote this listener to scroll on output.
    // Unfortunately it slows the output window down. Maybe there's a better
    // way?
    getDocument().addDocumentListener(new ScrollToEndDocumentListener());

    StyleContext defaultStyle = StyleContext.getDefaultStyleContext();

		_realOut = System.out;
		_realErr = System.err;

    _out = new PrintStream(new DocumentOutputStream(getDocument()));
                                                    

    AttributeSet red= defaultStyle.addAttribute(defaultStyle.getEmptySet(),
                                                StyleConstants.Foreground,
                                                Color.red);

    _err = new PrintStream(new DocumentOutputStream(getDocument(),
                                                    red));
  }

  public void clear() {
    try {
      getDocument().remove(0, getDocument().getLength());
    }
    catch (BadLocationException willNeverHappen) {}
  }

  /**
   * Makes this output view the active one, so it will get stdout
   * and stderr.
   */
  public void makeActive() {
    System.setOut(_out);
    System.setErr(_err);
  }

	public void deactivate() {
		System.setOut(_realOut);
		System.setErr(_realErr);
	}
}

