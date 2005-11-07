/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.util.List;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.EditDocumentException;

/**
 * Manages the execution of a Interactions History as a script of
 * individual commands.  Useful for presentations.
 * @version $Id$
 */
public class InteractionsScriptModel {
  /** The interactions model associated with the script. */
  private InteractionsModel _model;
  /** The interactions document. */
  private InteractionsDocument _doc;
  /** The interactions to perform. */
  private List<String> _interactions;
  /** The index into the list of the current interaction. */
  private int _currentInteraction;
  /**
   * Indicates whether the iterator has "passed" the current interaction,
   * which is the case after an execution.
   * In this state, "next" will show the interaction after our index,
   * and "prev" will show the interaction at our index (which was most
   * recently executed).
   */
  private boolean _passedCurrent;

  /**
   * Constructs a new interactions script using the given model and interactions.
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

  /**
   * Enters the next interaction into the interactions pane.
   */
  public void nextInteraction() {
    if (!hasNextInteraction()) {
      throw new IllegalStateException("There is no next interaction!");
    }
    _currentInteraction++;
    _showCurrentInteraction();
    _passedCurrent = false;
  }

  /**
   * Enters the current interaction into the interactions pane.
   *
  public void currentInteraction() {
    if (!hasCurrentInteraction()) {
      throw new IllegalStateException("There is no current interaction!");
    }
    try {
      _doc.clearCurrentInteraction();
      String text = _interactions.get(_currentInteraction);
      _doc.insertText(_doc.getLength(), text, _doc.DEFAULT_STYLE);
    }
    catch (EditDocumentException dae) {
      throw new UnexpectedException(dae);
    }
  }*/

  /**
   * Enters the previous interaction into the interactions pane.
   */
  public void prevInteraction() {
    if (!hasPrevInteraction()) {
      throw new IllegalStateException("There is no previous interaction!");
    }
    // Only move back if we haven't passed the current interaction
    if (!_passedCurrent) {
      _currentInteraction--;
    }
    _showCurrentInteraction();
    _passedCurrent = false;
  }

  /**
   * Clears the current text at the prompt and shows the current
   * interaction from the script.
   */
  protected void _showCurrentInteraction() {
    try {
      _doc.clearCurrentInteraction();
      String text = _interactions.get(_currentInteraction);
      _doc.insertText(_doc.getLength(), text, _doc.DEFAULT_STYLE);
    }
    catch (EditDocumentException dae) {
      throw new UnexpectedException(dae);
    }
  }

  /**
   * Executes the current interaction.
   * After this call, we have passed the current interaction.
   */
  public void executeInteraction() {
    _model.interpretCurrentInteraction();
    _passedCurrent = true;
  }

  /**
   * Ends the script.
   * TODO: Is this method necessary at all?
   */
  public void closeScript() {
    //_interactions = null;  // Why do this?  It can only cause problems...
    _currentInteraction = -1;
    _passedCurrent = false;
  }

  /**
   * @return true iff this script has another interaction to perform.
   */
  public boolean hasNextInteraction() {
    return _currentInteraction < _interactions.size() - 1;
  }

  /**
   * @return true iff this script has a current interaction to perform.
   *
  public boolean hasCurrentInteraction() {
    return _currentInteraction >= 0;
  }*/

  /**
   * @return true iff this script has a previous interaction to perform.
   */
  public boolean hasPrevInteraction() {
    int index = _currentInteraction;
    if (_passedCurrent) {
      // We're passed the current, so the previous interaction is the current.
      index++;
    }
    return index > 0;
  }
}