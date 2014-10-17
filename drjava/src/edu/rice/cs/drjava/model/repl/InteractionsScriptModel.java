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

package edu.rice.cs.drjava.model.repl;

import java.awt.EventQueue;

import java.util.List;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.EditDocumentException;

/** Manages the execution of a Interactions History as a script of individual commands.  Useful for presentations.
  * @version $Id: InteractionsScriptModel.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class InteractionsScriptModel /* implements Serializable */ {
  /** The interactions model associated with the script. */
  private volatile InteractionsModel _model;
  /** The interactions document. */
  private volatile InteractionsDocument _doc;
  /** The interactions to perform. */
  private volatile List<String> _interactions;
  /** The index into the list of the current interaction. */
  private volatile int _currentInteraction;
  /** Indicates whether the iterator has "passed" the current interaction, which is the case after an execution. In
    * this state, "next" will show the interaction after our index, and "prev" will show the interaction at our index
    * (which was mostrecently executed).
    */
  private volatile boolean _passedCurrent;

  /** Constructs a new interactions script using the given model and interactions.
    * @param model the interactions model
    * @param interactions the interactions that make up the script.
    */
  public InteractionsScriptModel(InteractionsModel model, List<String> interactions) {
    _model = model;
    _doc = model.getDocument();
    _interactions = interactions;
    _currentInteraction = -1;
    _passedCurrent = false;
  }

  /** Enters the next interaction into the interactions pane. Should only run in the event thread. */
  public void nextInteraction() {
    if (! hasNextInteraction()) { throw new IllegalStateException("There is no next interaction!"); }
    _currentInteraction++;
    _showCurrentInteraction();
    _passedCurrent = false;
  }

//  /** Enters the current interaction into the interactions pane. */
//  public void currentInteraction() {
//    if (!hasCurrentInteraction()) {
//      throw new IllegalStateException("There is no current interaction!");
//    }
//    try {
//      _doc.clearCurrentInteraction();
//      String text = _interactions.get(_currentInteraction);
//      _doc.insertText(_doc.getLength(), text, _doc.DEFAULT_STYLE);
//    }
//    catch (EditDocumentException dae) {
//      throw new UnexpectedException(dae);
//    }
//  }

  /** Enters the previous interaction into the interactions pane. Should only run in the event thread. */
  public void prevInteraction() {
    if (! hasPrevInteraction()) throw new IllegalStateException("There is no previous interaction!");

    // Only move back if we haven't passed the current interaction
    if (! _passedCurrent)  _currentInteraction--;
    _showCurrentInteraction();
    _passedCurrent = false;
  }

  /** Clears the current text at the prompt and shows the current interaction from the script.  Should only run in the
    * event thread.  Assumes that write lock is already held.
    */
  private void _showCurrentInteraction() {
    try {
      _doc.clearCurrentInteraction();
      String text = _interactions.get(_currentInteraction);
      _doc.insertText(_doc.getLength(), text, InteractionsDocument.DEFAULT_STYLE);
    }
    catch (EditDocumentException dae) {
      throw new UnexpectedException(dae);
    }
  }

  /** Executes the current interaction.  Should only run in the event thread. After this call, we have passed the 
    * current interaction.
    */
  public void executeInteraction() {
    _passedCurrent = true;
    /* The following must use EventQueue rather than Utilities because this task must be placed at the end of the
     * event queue, running the interpretCurrentInteraction call apart from this write locked section. In 
     * SimpleInteractionModel, the interpret method is called SYNCHRONOUSLY.  There is a faint chance of a race with
     * regard to the sequenceing of operations in the event queue.  There could already be operations that affect
     * the determination of the current interaction on the event queue. If we forced the interpret method to run 
     * asynchronously in SimpleInteractionsModel, then we could determine the current interaction within this write
     * locked section avoiding the race. */
    EventQueue.invokeLater(new Runnable() { public void run() { _model.interpretCurrentInteraction(); } });
  }

//  /** Ends the script.  Not currently used. */
//  public synchronized void closeScript() {
//    _currentInteraction = -1;
//    _passedCurrent = false;
//  }

  /** @return true iff this script has another interaction to perform. */
  public boolean hasNextInteraction() {
    return _currentInteraction < _interactions.size() - 1; 
  }

//  /** @return true iff this script has a current interaction to perform. Not currently used.  No sync required because
//    * it only reads a single volatile field. 
//    */
//  public boolean hasCurrentInteraction() { return _currentInteraction >= 0; }

  /** @return true iff this script has a previous interaction to perform. */
  public boolean hasPrevInteraction() {
    int index = _currentInteraction;
    if (_passedCurrent) index++; // We're passed the current, so the previous interaction is the current.
    return index > 0;
  }
}