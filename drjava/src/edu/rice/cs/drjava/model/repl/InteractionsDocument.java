/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import java.awt.Toolkit;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

// model
class InteractionsDocument extends PlainDocument {
  /** Index in the document of the first place that is editable. */
  int frozenPos = 0;
  private final String banner = "Welcome to DrJava.\n";
	
  JavaInterpreter _interpreter;

  /**
   * Command-line history. It's not reset when the interpreter is reset.
   */
  private History _history = new History();

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
      _history.moveEnd();

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

  public void moveHistoryPrevious() {
    _history.movePrevious();
    _replaceCurrentLineFromHistory();
  }

  public void moveHistoryNext() {
    _history.moveNext();
    _replaceCurrentLineFromHistory();
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
    }
    catch (BadLocationException ble) {
      // impossible
    }
  }

  public boolean hasHistoryPrevious() {
    return _history.hasPrevious();
  }

  public boolean hasHistoryNext() {
    return _history.hasNext();
  }

  public void eval() {
    try {
      String text = getText(frozenPos, getLength()-frozenPos);
      _history.add(text);

      String toEval = text.trim();

      // Result of interpretation, or JavaInterpreter.NO_RESULT if none.
      Object result; 
      
      // Do nothing but prompt if there's nothing to evaluate!
      if (toEval.length() == 0) {
        result = JavaInterpreter.NO_RESULT;
      }
      else {
        if (toEval.startsWith("java ")) {
          toEval = _testClassCall(toEval);
        }
                
        result = _interpreter.interpret(toEval);
        String resultStr;

        try {
          resultStr = String.valueOf(result);
        }
        catch (Throwable t) {
          // Very weird. toString() on result must have thrown this exception!
          // Let's act like DynamicJava would have if this exception were thrown
          // and rethrow as RuntimeException
          throw new RuntimeException(t.toString());
        }
      }

      if(result != JavaInterpreter.NO_RESULT) {
        super.insertString(getLength(), "\n" + String.valueOf(result)
            + "\n", null);
      }
      else {
        super.insertString(getLength(), "\n", null);
      }

      prompt();
    }		
    catch (BadLocationException e) {
      throw new InternalError("getting repl text failed");
    }
    catch (Throwable e) {
      String message = e.getMessage();
      // Don't let message be null. Java sadly makes getMessage() return
      // null if you construct an exception without a message.
      if (message == null) {
        message = e.toString();
        e.printStackTrace();
      }

      // Hack to prevent long syntax error messages
      try {
				if (message.startsWith("koala.dynamicjava.interpreter.InterpreterException: Encountered"))
        {
          super.insertString(getLength(),
                             "\nError in evaluation: " +
                             "Invalid syntax\n", null);
        }
        else {
          super.insertString(getLength(),
                             "\nError in evaluation: " + message + "\n",
                             null);
        }
          
        prompt();
      }
      catch (BadLocationException willNeverHappen) {}
    }
	}

	/**
	 *Assumes a trimmed String. Returns a string of the main call that the
	 *interpretor can use.
	 */
	private String _testClassCall(String s)
		{
			LinkedList ll = new LinkedList();
			if(s.endsWith(";"))
				s = _deleteSemiColon(s);
			
			StringTokenizer st = new StringTokenizer(s);
			st.nextToken(); //don't want to get back java
						
			String argument = st.nextToken(); // must have a second Token
			
			while(st.hasMoreTokens())
				ll.add(st.nextToken());
			
			argument = argument + ".main(new String[]{";
			ListIterator li = ll.listIterator(0);
			while (li.hasNext()){
				argument = argument + "\""+ (String)(li.next())+"\"";
				if (li.hasNext())
					argument = argument + ",";
			}
			argument = argument + "});";
			return argument;
		}

	private String _deleteSemiColon(String s)
		{
			return s.substring(0, s.length()-1);
		}
	
		
	
}










