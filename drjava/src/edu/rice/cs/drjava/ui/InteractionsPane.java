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

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Toolkit;


/**
 * The view component for repl interaction.
 *
 * @version $Id$
 */
public class InteractionsPane extends JTextPane {
  
  /** The custom keymap for the interactions pane. */
  protected Keymap _keymap;
  
  
  /** A runnable object that causes the editor to beep. */
  protected Runnable _beep = new Runnable() {
    public void run() {
        Toolkit.getDefaultToolkit().beep();      
    }
  };
  
  /**
   * Returns a runnable object that beeps to the user.
   */
  public Runnable getBeep() {
    return _beep;
  }
  
  /**
   * Creates an InteractionsPane with the given document.
   * @param doc StyledDocument containing the interactions history.
   */
  public InteractionsPane(StyledDocument doc) {
    super(doc);
    
    //add actions for enter key, etc.
    _keymap = addKeymap("INTERACTIONS_KEYMAP", getKeymap());

    setCaretPosition(doc.getLength());
  }
  
  /**
   * Assigns the given keystroke to the given action in this pane.
   * @param stroke keystroke that triggers the action
   * @param action Action to perform
   */
  public void addActionForKeyStroke(KeyStroke stroke, Action action) {
    _keymap.addActionForKeyStroke(stroke, action);
    setKeymap(_keymap);
  }
  
  /**
   * Sets this pane's beep to be a different runnable object.
   * (Defaults to Toolkit.getDefaultToolkit().beep().)
   * @param beep Runnable command to notify the user
   */
  public void setBeep(Runnable beep) {
    _beep = beep;
  }
}



