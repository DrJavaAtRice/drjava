/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;

import java.awt.Color;

import java.io.PrintStream;

public class OutputView extends JTextPane
{
  private PrintStream _out;
  private PrintStream _err;

  public OutputView() {
    StyleContext defaultStyle = StyleContext.getDefaultStyleContext();

    _out = new PrintStream(new DocumentOutputStream(getDocument()));
                                                    

    AttributeSet red= defaultStyle.addAttribute(defaultStyle.getEmptySet(),
                                                StyleConstants.Foreground,
                                                Color.red);

    _err = new PrintStream(new DocumentOutputStream(getDocument(),
                                                    red));
  }

  /**
   * Makes this output view the active one, so it will get stdout
   * and stderr.
   */
  public void makeActive() {
    System.setOut(_out);
    System.setErr(_err);
  }
}
