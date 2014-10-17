/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (javaplt@rice.edu)
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

import java.awt.event.*;
import javax.swing.*;
//import java.io.Serializable;

import edu.rice.cs.drjava.model.repl.InteractionsScriptModel;

/** Controller for an interactions script.
  * @version $Id: InteractionsScriptController.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class InteractionsScriptController /* implements Serializable */ {
  /** Associated model. */
  private InteractionsScriptModel _model;
  /** Associated view. */
  private InteractionsScriptPane _pane;
  /** Interactions pane. */
  private InteractionsPane _interactionsPane;

  /** Builds a new interactions script pane and links it to the given model.
    * @param model the InteractionsScriptModel to use
    * @param closeAction how to close this script.
    */
  public InteractionsScriptController(InteractionsScriptModel model, Action closeAction,
                                      InteractionsPane interactionsPane) {
    _model = model;
    _closeScriptAction = closeAction;
    _interactionsPane = interactionsPane;
    _pane = new InteractionsScriptPane(4, 1);

    // Previous
    _setupAction(_prevInteractionAction, "Previous", "Insert Previous Interaction from Script");
    _pane.addButton(_prevInteractionAction);
    // Next
    _setupAction(_nextInteractionAction, "Next", "Insert Next Interaction from Script");
    _pane.addButton(_nextInteractionAction);
    // Execute
    _setupAction(_executeInteractionAction, "Execute", "Execute Current Interaction");
    _pane.addButton(_executeInteractionAction);
    // Close
    _setupAction(_closeScriptAction, "Close", "Close Interactions Script");
    _pane.addButton(_closeScriptAction);
    setActionsEnabled();
  }

  /** Sets the navigation actions to be enabled, if appropriate. */
  public void setActionsEnabled() {
    _nextInteractionAction.setEnabled(_model.hasNextInteraction());
    _prevInteractionAction.setEnabled(_model.hasPrevInteraction());
    _executeInteractionAction.setEnabled(true);
  }

  /** Disables navigation actions */
  public void setActionsDisabled() {
    _nextInteractionAction.setEnabled(false);
    _prevInteractionAction.setEnabled(false);
    _executeInteractionAction.setEnabled(false);
  }

  /** @return the interactions script pane controlled by this controller. */
  public InteractionsScriptPane getPane() { return _pane; }

  /** Action to go back in the script. */
  private Action _prevInteractionAction = new AbstractAction("Previous") {
    public void actionPerformed(ActionEvent e) {
      _model.prevInteraction();
      setActionsEnabled();
      _interactionsPane.requestFocusInWindow();
    }
  };
  /** Action to go forward in the script. */
  private Action _nextInteractionAction = new AbstractAction("Next") {
    public void actionPerformed(ActionEvent e) {
      _model.nextInteraction();
      setActionsEnabled();
      _interactionsPane.requestFocusInWindow();
    }
  };
  /** Action to execute the current interaction. */
  private Action _executeInteractionAction = new AbstractAction("Execute") {
    public void actionPerformed(ActionEvent e) {
      _model.executeInteraction();
      _interactionsPane.requestFocusInWindow();
    }
  };
  /** Action to end the script.  (Defined in constructor.) */
  private Action _closeScriptAction; /* = new AbstractAction("<=Close=>") {
    public void actionPerformed(ActionEvent e) {
      _model.closeScript();
      _pane.setMaximumSize(new Dimension(0,0));
    }
  };*/

  /** Sets up fields on the given Action, such as the name and tooltip.
   * @param a Action to modify
   * @param name Default name for the Action (for buttons)
   * @param desc Short description of the Action (for tooltips)
   */
  protected void _setupAction(Action a, String name, String desc) {
    a.putValue(Action.DEFAULT, name);
    a.putValue(Action.SHORT_DESCRIPTION, desc);
  }
}