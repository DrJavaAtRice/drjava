/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.FileSaveSelector;

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
  private int _frozenPos = 0;

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

    _frozenPos = getLength();
    */
  }

  /**
   * Returns the first location in the document where editing is allowed.
   */
  public int getFrozenPos() {
    return _frozenPos;
  }

  /**
   * Override superclass insertion to prevent insertion past frozen point. 
   * @exception BadLocationException
   */
  public void insertString(int offs, String str, AttributeSet a)
    throws BadLocationException
  {
    if (offs < _frozenPos) {
      Toolkit.getDefaultToolkit().beep();
    } 
    else {
      super.insertString(offs, str, a);
    }
  }

  public void setInProgress(boolean b) {
    _inProgress = b;
  }

  public boolean inProgress() {
    return _inProgress;
  }

  public void insertBeforeLastPrompt(String s, AttributeSet a) {
    try {
      int pos;
      if (_inProgress) {
        pos = getLength();
      }
      else {
        pos = _frozenPos - PROMPT.length();
      }

      super.insertString(pos, s, a);
      _frozenPos += s.length();
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
    if (offs < _frozenPos) {
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
      setInProgress(false);
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  public void prompt() {
    try {
      super.insertString(getLength(), PROMPT, null);
      _frozenPos = getLength();
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Clears the history
   */
  public void clearHistory() {
    _history.clear();
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
      return getText(_frozenPos, getLength() - _frozenPos);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Clears the current interaction text and then moves
   * to the end of the command history.
   */
  public void clearCurrentInteraction() {
    _clearCurrentInteractionText();
    _history.moveEnd();
  }

  private void _clearCurrentInteractionText() {
    try {
      // Delete old value of current line
      remove(_frozenPos, getLength() - _frozenPos);
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }
  
  /**
   * Replaces any text entered past the prompt with the current
   * item in the history.
   */
  private void _replaceCurrentLineFromHistory() {
    try {
      _clearCurrentInteractionText();
      insertString(getLength(), _history.getCurrent(), null);
    }
    catch (BadLocationException ble) {
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

      if (null == message || "null".equals(message)) {
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
  
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _history.writeToFile(selector);
  }
  
  public String getHistoryAsString() {
    return _history.getHistoryAsString();
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
