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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.DocumentEvent;
import gj.util.Vector;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.StringTokenizer;

import java.io.File;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.FileMovedException;

/**
 * Extended UndoManager with increased functionality.  Can handle aggregating 
 * multiple edits into one for the purposes of undoing and redoing.
 * Is used to be able to call editToBeUndone and editToBeRedone since they
 * are protected methods in UndoManager.
 */
public class CompoundUndoManager extends UndoManager {
  /**
   * The compound edits we are storing.
   */
  private Vector<CompoundEdit> _compoundEdits;
  
  /**
   * The keys for the CompoundEdits we are storing.
   */
  private Vector<Integer> _keys;
    
  /**
   * The next key to use for nested CompoundEdits.
   */
  private int _nextKey;
  
  /**
   * Default constructor.
   */
  public CompoundUndoManager() {
    super();
    _compoundEdits = new Vector<CompoundEdit>();
    _keys = new Vector<Integer>();
    _nextKey = 0;
  }
  
  /**
   * Starts a compound edit.
   * @return the key for the compound edit
   */
  public int startCompoundEdit() {
    _compoundEdits.insertElementAt(new CompoundEdit(), 0);
    _keys.insertElementAt(new Integer(_nextKey++), 0);
    return _keys.firstElement().intValue();
  }
  
  /**
   * Ends a compound edit.
   * @param key the key that was returned by startCompoundEdit()
   */
  public void endCompoundEdit(int key) {
    if(_keys.elementAt(0).intValue() == key) {
      CompoundEdit compoundEdit = _compoundEdits.elementAt(0);
      _compoundEdits.removeElementAt(0);
      compoundEdit.end();

      if (_compoundEdits.isEmpty()) {
        super.addEdit(compoundEdit);
      }
      else {
        _compoundEdits.firstElement().addEdit(compoundEdit);
      }
    } 
    else {
      throw new IllegalStateException("Improperly nested compound edits.");
    }
  }
  
  /**
   * Gets the next undo.
   * @return the next undo
   */
  public UndoableEdit getNextUndo() {
    return editToBeUndone();
  }
  
  /**
   * Gets the next redo.
   * @return the next redo
   */
  public UndoableEdit getNextRedo() {
    return editToBeRedone();
  }
  
  /**
   * Adds an edit.  Checks whether or not the current edit is a compound edit.
   * @param e the edit to be added
   * @return true if the add is successful, false otherwise
   */
  public boolean addEdit(UndoableEdit e) {
    if (!_compoundEdits.isEmpty()) {
      return _compoundEdits.firstElement().addEdit(e);
    }
    else {
      return super.addEdit(e);
    }
  }
}