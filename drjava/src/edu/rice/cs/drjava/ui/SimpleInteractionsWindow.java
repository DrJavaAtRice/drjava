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
import java.awt.Font;
import java.awt.event.*;

import edu.rice.cs.drjava.model.repl.SimpleInteractionsModel;
//import edu.rice.cs.drjava.model.repl.SimpleRMIInteractionsModel;
import edu.rice.cs.drjava.model.repl.InteractionsListener;
import edu.rice.cs.util.text.SwingDocumentAdapter;

/**
 * A standalone Interactions Window that provides the functionality of
 * DrJava's Interactions Pane in a single JVM.  Useful for quickly
 * testing small pieces of code if DrJava is not running.
 * @version $Id$
 */
public class SimpleInteractionsWindow extends JFrame {
  //private final SimpleRMIInteractionsModel _rmiModel;
  private final SimpleInteractionsModel _model;
  private final SwingDocumentAdapter _adapter;
  private final InteractionsPane _pane;
  private final InteractionsController _controller;
  
  public SimpleInteractionsWindow() {
    this("Interactions Window");
  }
  
  public SimpleInteractionsWindow(String title) {
    super(title);
    setSize(600, 400);
    
    _adapter = new SwingDocumentAdapter();
    //_rmiModel = new SimpleRMIInteractionsModel(_adapter);
    _model = new SimpleInteractionsModel(_adapter);
    _pane = new InteractionsPane(_adapter);
    _controller = new InteractionsController(_model, _adapter, _pane);
    
    _pane.setFont(Font.decode("monospaced"));

    _model.addInteractionsListener(new InteractionsListener() {
      public void interactionStarted() {
        _pane.setEditable(false);
      }
      public void interactionEnded() {
        _controller.moveToPrompt();
        _pane.setEditable(true);
      }
      public void interpreterResetting() {
        _pane.setEditable(false);
      }
      public void interactionErrorOccurred(int offset, int length){
        _pane.highlightError(offset, length);
      }
      public void interpreterReady() {
        _controller.moveToPrompt();
        _pane.setEditable(true);
      }
      public void interpreterExited(int status) {}
      public void interpreterChanged(boolean inProgress) {
        _pane.setEditable(inProgress);
      }
      public void interpreterResetFailed(Throwable t) {
        interpreterReady();
      }
    });

    JScrollPane scroll = new JScrollPane(_pane);
    getContentPane().add(scroll);
    
    // Add listener to quit if window is closed
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        close();
      }
    });
    
  }
  
  /**
   * Terminates this process. This is overridden in DrJava
   * so that is disposes of itself instead of calling System.exit(0).
   */
  protected void close() {
    System.exit(0);
  }
  
  /**
   * Accessor for the controller.
   */
  public InteractionsController getController() {
    return _controller;
  }
  
  /**
   * Defines a variable in this window to the given value.
   */
  public void defineVariable(String name, Object value) {
    _model.defineVariable(name, value);
  }
  
  /**
   * Defines a final variable in this window to the given value.
   */
  public void defineConstant(String name, Object value) {
    _model.defineConstant(name, value);
  }

  /**
   * Sets whether protected and private variables and methods can be accessed 
   * from within the interpreter.
   */
  public void setInterpreterPrivateAccessible(boolean accessible) {
    _model.setInterpreterPrivateAccessible(accessible);
  }
  
  /**
   * Main method to create a SimpleInteractionsWindow from the console.
   * Doesn't take any command line arguments.
   */
  public static void main(String[] args) {
    SimpleInteractionsWindow w = new SimpleInteractionsWindow();
    if (args.length > 0 && args[0].equals("-debug")) {
      w.defineVariable("FRAME", w);
      w.defineVariable("CONTROLLER", w.getController());
      w.setInterpreterPrivateAccessible(true);
    }
    w.show();
  }
}
