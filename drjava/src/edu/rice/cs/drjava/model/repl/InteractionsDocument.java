package edu.rice.cs.drjava.model.repl;

import  javax.swing.text.PlainDocument;
import  javax.swing.text.AttributeSet;
import  javax.swing.text.BadLocationException;
import  java.awt.Toolkit;
import  java.util.LinkedList;
import  java.util.ListIterator;
import  java.util.StringTokenizer;

import edu.rice.cs.drjava.util.UnexpectedException;

/**
 * The document that handles input to the repl and the interpretation
 * of said input.
 * @version $Id$
 */
public class InteractionsDocument extends PlainDocument {

  /** Index in the document of the first place that is editable. */
  int frozenPos = 0;
  private final String banner = "Welcome to DrJava.\n";

  /**
   * Command-line history. It's not reset when the interpreter is reset.
   */
  private History _history = new History();

  /**
   * put your documentation comment here
   */
  public InteractionsDocument() {
    reset();
  }

  /**
   * Override superclass insertion to prevent insertion past frozen point. 
   * @exception BadLocationException
   */
  public void insertString(int offs, String str, AttributeSet a)
    throws BadLocationException
  {
    if (offs < frozenPos) {
      Toolkit.getDefaultToolkit().beep();
    } 
    else {
      super.insertString(offs, str, a);
    }
  }

  /**
   * Override superclass deletion to prevent deletion past frozen point. 
   * @exception BadLocationException
   */
  public void remove(int offs, int len) throws BadLocationException {
    if (offs < frozenPos) {
      Toolkit.getDefaultToolkit().beep();
    } 
    else {
      super.remove(offs, len);
    }
  }

  /** Clear the UI, and restart the interpreter. */
  public void reset() {
    try {
      super.remove(0, getLength());
      super.insertString(0, banner, null);
      prompt();
      _history.moveEnd();
    } catch (BadLocationException e) {
      throw  new InternalError("repl reset failed");
    }
  }

  /**
   * put your documentation comment here
   */
  public void prompt() {
    try {
      super.insertString(getLength(), "> ", null);
      frozenPos = getLength();
    } catch (BadLocationException e) {
      throw  new InternalError("printing prompt failed");
    }
  }

  /**
   * put your documentation comment here
   */
  public void moveHistoryPrevious() {
    _history.movePrevious();
    _replaceCurrentLineFromHistory();
  }

  /**
   * put your documentation comment here
   */
  public void moveHistoryNext() {
    _history.moveNext();
    _replaceCurrentLineFromHistory();
  }

  public String getCurrentInteraction() {
    try {
      return getText(frozenPos, getLength() - frozenPos);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Replaces any text entered past the prompt with the current
   * item in the history.
   */
  private void _replaceCurrentLineFromHistory() {
    try {
      // Delete old value of current line
      remove(frozenPos, getLength() - frozenPos);
      // Add current.
      insertString(getLength(), _history.getCurrent(), null);
    } catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean hasHistoryPrevious() {
    return  _history.hasPrevious();
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public boolean hasHistoryNext() {
    return  _history.hasNext();
  }

  public void addToHistory(String text) {
    _history.add(text);
  }
}



