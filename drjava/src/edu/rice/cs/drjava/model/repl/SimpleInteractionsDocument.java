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

import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.*;

import gj.util.Vector;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.FileSaveSelector;
import edu.rice.cs.drjava.model.repl.newjvm.InterpreterJVM;

/**
 * A simple implementation of the InteractionsDocument, which uses a
 * DynamicJavaAdatper directly (in the same JVM) to interpret code.
 * This can be used in a standalone interface, such as
 * edu.rice.cs.drjava.ui.SimpleInteractionsWindow.
 * @version $Id$
 */
public class SimpleInteractionsDocument extends AbstractInteractionsDocument {

  /**
   * An interpreter to evaluate interactions.
   */
  protected final JavaInterpreter _interpreter;

  /**
   * List of listeners to this document.
   */
  protected final Vector<SimpleInteractionsListener> _listeners;

  /**
   * Creates a new InteractionsDocument.
   */
  public SimpleInteractionsDocument() {
    super();
    _interpreter = new DynamicJavaAdapter();
    _listeners = new Vector<SimpleInteractionsListener>();
  }
  
  /**
   * Interprets the current command at the prompt.
   */
  public void interpretCurrentInteraction() {
    synchronized(_interpreter) {
      // Don't start a new interaction while one is in progress
      if (inProgress()) {
        return;
      }
      
      notifyInteractionStarted();
      
      String text = getCurrentInteraction();
      setInProgress(true);
      addToHistory(text);
      
      // there is no return at the end of the last line
      // better to put it on now and not later.
      insertString("\n");
      
      String toEval = text.trim();
      if (toEval.startsWith("java ")) {
        //toEval = _testClassCall(toEval);  (no support for "java" hack yet)
      }
      
      try {
        Object result = _interpreter.interpret(toEval);
        if (result != JavaInterpreter.NO_RESULT) {
          insertString(String.valueOf(result) + "\n");
        }
      }
      catch (ExceptionReturnedException e) {
        Throwable t = e.getContainedException();
        // getStackTrace should be a utility method somewhere...
        appendExceptionResult(t.getClass().getName(),
                              t.getMessage(),
                              InterpreterJVM.getStackTrace(t),
                              null);
      }
      finally {
        setInProgress(false);
        insertPrompt();
        
        // notify listeners
        notifyInteractionEnded();
      }
    }
  }

  /**
   * Adds a listener to this document.
   */
  public void addInteractionListener(SimpleInteractionsListener l) {
    _listeners.addElement(l);
  }

  /**
   * Removes the given listener from this document.
   */
  public void removeInteractionListener(SimpleInteractionsListener l) {
    _listeners.removeElement(l);
  }

  /**
   * Removes all listeners from this document.
   */
  public void removeAllInteractionListeners() {
    _listeners.removeAllElements();
  }

  /**
   * Notifies all listeners that an interaction has started.
   */
  protected void notifyInteractionStarted() {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.elementAt(i).interactionStarted();
    }
  }

  /**
   * Notifies all listeners that an interaction has ended.
   */
  protected void notifyInteractionEnded() {
    for (int i=0; i < _listeners.size(); i++) {
      _listeners.elementAt(i).interactionEnded();
    }
  }
  
  /**
   * Helper method to insert a string into the InteractionsDocument.
   */
  protected void insertString(String s) {
    try {
      insertString(getLength(), s, null);
    }
    catch (BadLocationException ble) {
      System.err.println("Error printing text: " + ble);
    }
  }
}
