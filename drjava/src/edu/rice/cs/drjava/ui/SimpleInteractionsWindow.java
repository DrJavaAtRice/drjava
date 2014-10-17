/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;

import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.swing.SwingFrame;


/** A standalone Interactions Window that provides the functionality of DrJava's Interactions Pane in a single JVM.
  * Useful for quickly testing small pieces of code if DrJava is not running.
  * @version $Id: SimpleInteractionsWindow.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class SimpleInteractionsWindow extends SwingFrame {
  //private final SimpleRMIInteractionsModel _rmiModel;
  private final SimpleInteractionsModel _model;
  private final InteractionsDJDocument _adapter;
  private final InteractionsPane _pane;
  private final InteractionsController _controller;
  
  public SimpleInteractionsWindow() { this("Interactions Window"); }
  
  public SimpleInteractionsWindow(String title) {
    super(title);
    setSize(600, 400);
    
    _adapter = new InteractionsDJDocument();
    //_rmiModel = new SimpleRMIInteractionsModel(_adapter);
    _model = new SimpleInteractionsModel(_adapter);
    _pane = new InteractionsPane(_adapter) {
      public int getPromptPos() { return _model.getDocument().getPromptPos(); }
    };
    _controller = new InteractionsController(_model, _adapter, _pane, new Runnable() { public void run() { } });
    
    _pane.setFont(Font.decode("monospaced"));
    
    _model.addListener(new InteractionsListener() {
      public void interactionStarted() { _pane.setEditable(false); }
      public void interactionEnded() {
        _controller.moveToPrompt();
        _pane.setEditable(true);
      }
      public void interpreterResetting() { _pane.setEditable(false); }
      public void interactionErrorOccurred(int offset, int length) { _pane.highlightError(offset, length); }
      public void interpreterReady(File wd) {
        _controller.moveToPrompt();
        _pane.setEditable(true);
      }
      public void interpreterExited(int status) { }
      public void interpreterChanged(boolean inProgress) { _pane.setEditable(inProgress); }
      public void interpreterResetFailed(Throwable t) { interpreterReady(FileOps.NULL_FILE); }
      public void interactionIncomplete() {
        int caretPos = _pane.getCaretPosition();
        _controller.getConsoleDoc().insertNewline(caretPos);
      }
    });
    
    JScrollPane scroll = new JScrollPane(_pane);
    getContentPane().add(scroll);
    
    // Add listener to quit if window is closed
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) { close(); }
    });
    
    initDone(); // call mandated by SwingFrame contract
  }
  
  /** Terminates this process. This is overridden in DrJava so that is disposes of itself instead of calling 
    * System.exit(0).
    */
  protected void close() { System.exit(0); }
  
  /** Accessor for the controller. */
  public InteractionsController getController() { return _controller; }
  
  /** Main method to create a SimpleInteractionsWindow from the console. Doesn't take any command line arguments. */
  public static void main(String[] args) {
    SimpleInteractionsWindow w = new SimpleInteractionsWindow();
    if (args.length > 0 && args[0].equals("-debug")) {
      // TODO: define apropriate context
//      w.defineVariable("FRAME", w);
//      w.defineVariable("CONTROLLER", w.getController());
    }
    w.setVisible(true);
  }
}
