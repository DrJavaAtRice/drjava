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
 * An interactions script model.
 */
public class InteractionsScriptModel {
  /** The interactions model associated with the script. */
  private InteractionsModel _model;
  /** The interactions document. */
  private InteractionsDocument _doc;
  /** The interactions to perform. */
  private List<String> _interactions;
  /** The index into the list of the next interaction. */
  private int _nextInteraction;

  /**
   * Constructs a new interactions script using the given model and interactions.
   * @param model the interactions model
   * @param interactions the interactions that make up the script.
   */
  public InteractionsScriptModel(InteractionsModel model, List<String> interactions) {
    _model = model;
    _doc = model.getDocument();
    _interactions = interactions;
    _nextInteraction = 0;
  }

  /**
   * Enters the next interaction into the interactions pane.
   */
  public void nextInteraction() {
    if (_nextInteraction >= _interactions.size()) {
      throw new IllegalStateException("There is no next interaction!");
    }
    try {
      _doc.clearCurrentInteraction();
      _doc.insertText(_doc.getDocLength(), _interactions.get(_nextInteraction++), _doc.DEFAULT_STYLE);
    }
    catch (DocumentAdapterException dae) {
      throw new UnexpectedException(dae);
    }
  }

  /**
   * Enters the previous interaction into the interactions pane.
   */
  public void prevInteraction() {
    if (_nextInteraction <= 0) {
      throw new IllegalStateException("There is no previous interaction!");
    }
    try {
      _doc.clearCurrentInteraction();
      _doc.insertText(_doc.getDocLength(), _interactions.get(--_nextInteraction), _doc.DEFAULT_STYLE);
    }
    catch (DocumentAdapterException dae) {
      throw new UnexpectedException(dae);
    }
  }

  /**
   * Executes the current interaction.
   */
  public void executeInteraction() {
    _model.interpretCurrentInteraction();
  }

  /**
   * Ends the script.
   */
  public void closeScript() {
    _interactions = null;
    _nextInteraction = -1;
  }

  /**
   * @return true iff this script has another interaction to perform.
   */
  public boolean hasNextInteraction() {
    return _nextInteraction < _interactions.size();
  }

  /**
   * @return true iff this script has a previous interaction to perform.
   */
  public boolean hasPrevInteraction() {
    return _nextInteraction > 0;
  }
}