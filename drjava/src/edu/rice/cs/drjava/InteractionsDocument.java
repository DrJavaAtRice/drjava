/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import java.awt.Toolkit;

// model
class InteractionsDocument extends PlainDocument {
  /** Index in the document of the first place that is editable. */
  private int frozenPos = 0;
  private final String banner = "Welcome to DrJava.\n";

  private JavaInterpreter _interpreter;

  public InteractionsDocument()
  {
    reset();
  }

  /** Override superclass insertion to prevent insertion past frozen point. */
  public void insertString(int offs, String str, AttributeSet a)
  throws BadLocationException {
    if (offs < frozenPos) {
      Toolkit.getDefaultToolkit().beep();
    }
    else {
      super.insertString(offs, str, a);
    }
  }

  /** Override superclass deletion to prevent deletion past frozen point. */
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

      _interpreter = new DynamicJavaAdapter();
    } catch (BadLocationException e) {
      throw new InternalError("repl reset failed");
    }
  }

  public void addClassPath(String path) {
    _interpreter.addClassPath(path);
  }

  public void prompt() {
    try {
      super.insertString(getLength(), "> ", null);
      frozenPos = getLength();
    } catch (BadLocationException e) {
      throw new InternalError("printing prompt failed");
    }
  }

  public void eval() {
    try {
      String toEval = getText(frozenPos, getLength()-frozenPos).trim();

      Object result = _interpreter.interpret(toEval);
			if(result != JavaInterpreter.NO_RESULT)
				 super.insertString(getLength(), String.valueOf(result) + "\n", null);

      prompt();
    }
    catch (BadLocationException e) {
      throw new InternalError("getting repl text failed");
    }
    catch (Exception e) {
      try {
				if(e.toString().startsWith("java.lang.RuntimeException: Encountered"))
					{
						int end = e.toString().indexOf('\n');
						super.insertString(getLength(), "Error in evaluation: " +
															 "Invalid Expression (Bad Syntax)\n", null);
					} else
						super.insertString(getLength(), "Error in evaluation: " + e + "\n", null);
        prompt();
      }
      catch (BadLocationException willNeverHappen) {}
    }
  }
}

