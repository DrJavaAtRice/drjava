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

import java.util.List;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.text.DocumentAdapterException;

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
      _doc.insertText(_doc.getDocLength(), text, _doc.DEFAULT_STYLE);
    }
    catch (DocumentAdapterException dae) {
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
      _doc.insertText(_doc.getDocLength(), text, _doc.DEFAULT_STYLE);
    }
    catch (DocumentAdapterException dae) {
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