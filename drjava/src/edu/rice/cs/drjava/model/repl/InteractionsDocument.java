package edu.rice.cs.drjava.model.repl;

import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

import edu.rice.cs.util.UnexpectedException;

/**
 * The document that handles input to the repl and the interpretation
 * of said input.
 * @version $Id$
 */
public class InteractionsDocument extends DefaultStyledDocument {
  public static final String BANNER = "Welcome to DrJava.\n";
  public static final String PROMPT = "> ";

  private boolean _inProgress = false;

  /** Index in the document of the first place that is editable. */
  int frozenPos = 0;

  /**
   * Command-line history. It's not reset when the interpreter is reset.
   */
  private History _history = new History();

  /**
   * put your documentation comment here
   */
  public InteractionsDocument() {
    reset();

    /*
    ActionListener l = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        JOptionPane.showMessageDialog(null, "clicked");
      }
    };

    JButton b = new JButton("clicky");
    b.setAlignmentY(.8f);
    b.addActionListener(l);

    SimpleAttributeSet buttonSet = new SimpleAttributeSet();
    StyleConstants.setComponent(buttonSet, b);
    try {
      insertString(getLength(), " ", buttonSet);
      insertString(getLength(), " ", null);
    }
    catch (BadLocationException ble) {}

    frozenPos = getLength();
    */
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

  public void setInProgress(boolean b) {
    _inProgress = b;
  }

  public void insertBeforeLastPrompt(String s, AttributeSet a) {
    try {
      int pos;
      if (_inProgress) {
        pos = getLength();
      }
      else {
        pos = frozenPos - PROMPT.length();
      }

      super.insertString(pos, s, a);
      frozenPos += s.length();
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
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

  /** Clear the UI. */
  public void reset() {
    try {
      super.remove(0, getLength());
      super.insertString(0, BANNER, null);
      prompt();
      _history.moveEnd();
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  public void prompt() {
    try {
      super.insertString(getLength(), PROMPT, null);
      frozenPos = getLength();
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
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

  public void appendExceptionResult(String exceptionClass,
                                    String message,
                                    String stackTrace,
                                    AttributeSet set)
  {
    //writeLock();
    try {

      if (null != message || "null".equals(message)) {
        message = "";
      }

      insertString(getLength(), exceptionClass + ": " + message + "\n", set);

      // An example stack trace:
      //
      // java.lang.IllegalMonitorStateException: 
      // at java.lang.Object.wait(Native Method)
      // at java.lang.Object.wait(Object.java:425)
      if (! stackTrace.trim().equals("")) {
        BufferedReader reader=new BufferedReader(new StringReader(stackTrace));
        
        String line;
        // a line is parsable if it has ( then : then ), with some
        // text between each of those
        while ((line = reader.readLine()) != null) {
          String fileName = null;
          int lineNumber = -1;

          int openLoc = line.indexOf('(');

          if (openLoc != -1) {
            int closeLoc = line.indexOf(')', openLoc + 1);

            if (closeLoc != -1) {
              int colonLoc = line.indexOf(':', openLoc + 1);
              if ((colonLoc > openLoc) && (colonLoc < closeLoc)) {
                // ok this line is parsable!
                String lineNumStr = line.substring(colonLoc + 1, closeLoc);
                try {
                  lineNumber = Integer.parseInt(lineNumStr);
                  fileName = line.substring(openLoc + 1, colonLoc);
                }
                catch (NumberFormatException nfe) {
                  // do nothing; we failed at parsing
                }
              }
            }
          }

          insertString(getLength(), line, set);

          // OK, now if fileName != null we did parse out fileName
          // and lineNumber.
          // Here's where we'd add the button, etc.
          if (fileName != null) {
            /*
            JButton button = new JButton("go");
            button.addActionListener(new ExceptionButtonListener(fileName,
                                                                 lineNumber));

            SimpleAttributeSet buttonSet = new SimpleAttributeSet(set);
            StyleConstants.setComponent(buttonSet, button);
            insertString(getLength(), "  ", null);
            insertString(getLength() - 1, " ", buttonSet);
            */
            //JOptionPane.showMessageDialog(null, "button in");
            //insertString(getLength(), " ", null);
            //JOptionPane.showMessageDialog(null, "extra space");
          }

          //JOptionPane.showMessageDialog(null, "\\n");
          insertString(getLength(), "\n", set);

        } // end the while
      }
    }
    catch (IOException ioe) {
      // won't happen; we're readLine'ing from a String!
      throw new UnexpectedException(ioe);
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    finally {
      //writeUnlock();
    }
  }

  private class ExceptionButtonListener implements ActionListener {
    private final String _fileName;
    private final int _lineNumber;

    public ExceptionButtonListener(final String fileName, final int lineNumber)
    {
      _fileName = fileName;
      _lineNumber = lineNumber;
    }

    public void actionPerformed(ActionEvent e) {
      javax.swing.JOptionPane.showMessageDialog(null, "exception at line " + 
                                                      _lineNumber +
                                                      " in file " +
                                                      _fileName);
    }
  }
}

/*
class LinkInfo implements Comparable {
  private final String _text;
  private final Position _start;
  private final Position _end;

  public LinkInfo(final Position start, final Position end, final String text) {
    _start = start;
    _end = end;
    _text = text;
  }

  public int compareTo(Object o) {
    LinkInfo other = (LinkInfo) o;

    if (_start.getOffset() == other._start.getOffset()) {
      return _end.getOffset() < other._end.getOffset();
    }
    else {
      if (_end.getOffset() == other._end.getOffset()) {
        return 0;
      }
      else {
        return _start.getOffset() < o._start.getOffset();
      }
    }
  }

  public boolean containsOffset(int offset) {
    return (offset >= _start.getOffset()) &&
           (offset < _end.getOffset());
  }

  public String getText() {
    return _text;
  }
}
*/
