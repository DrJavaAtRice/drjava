/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.JTextArea;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InteractionsView extends JTextArea
{
  public InteractionsView() {
    super(new InteractionsDocument());
    reset();

    // Eval when the user hits enter.
    addKeyListener(new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                      if (e.getKeyChar() == '\n') {
                        getInteractionsDocument().eval();
                      }
                    }
                  });
  }

  private InteractionsDocument getInteractionsDocument() {
    return (InteractionsDocument) getDocument();
  }

  // The class path will be reset on reset().
  public void addClassPath(String path) {
    getInteractionsDocument().addClassPath(path);
  }

  public void reset() {
    getInteractionsDocument().reset();
    setCaretPosition(getInteractionsDocument().getLength());
  }
  
  public void prompt() {
    getInteractionsDocument().prompt();
    setCaretPosition(getInteractionsDocument().getLength());
  }
  
  // public boolean atEnd() { return getCaretPosition() == doc.getLength(); }
}
