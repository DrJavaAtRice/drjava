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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.undo.*;
import java.util.LinkedList;

import edu.rice.cs.drjava.model.GlobalEventNotifier;

/**
 * Extended UndoManager with increased functionality.  Can handle aggregating
 * multiple edits into one for the purposes of undoing and redoing.
 * Is used to be able to call editToBeUndone and editToBeRedone since they
 * are protected methods in UndoManager.
 * @version $Id$
 */
public class CompoundUndoManager extends UndoManager {
  
  private static int counter = 0;
  
  private int id;
  
  
  /**
   * The compound edits we are storing.
   */
  private LinkedList<CompoundEdit> _compoundEdits;
  
  /**
   * The keys for the CompoundEdits we are storing.
   */
  private LinkedList<Integer> _keys;
  
  /**
   * The next key to use for nested CompoundEdits.
   */
  private int _nextKey;
  
  /**
   * The last edit that was performed before the last save
   */
  private UndoableEdit _savePoint;
  
  /**
   * keeps track of the listeners to this undo manager
   */
  private final GlobalEventNotifier _notifier;
  
  /**
   * Default constructor.
   */
  public CompoundUndoManager(GlobalEventNotifier notifier) {
    super();
    counter++;
    id = counter;
    _compoundEdits = new LinkedList<CompoundEdit>();
    _keys = new LinkedList<Integer>();
    _nextKey = 0;
    _savePoint = null;
    _notifier = notifier;
  }
  
  /**
   * Starts a compound edit.
   * @return the key for the compound edit
   */
  public int startCompoundEdit() {
    _compoundEdits.add(0, new CompoundEdit());
    _keys.add(0, new Integer(_nextKey));
    if(_nextKey < Integer.MAX_VALUE) {
      _nextKey++;
    }
    else {
      _nextKey = Integer.MIN_VALUE;
    }
    return _keys.get(0).intValue();
  }
  
  
  
  /**
   * Ends the last compound edit that was created.
   * Used when a compound edit is created by the _undoListener in DefinitionsPane and the key is not known in DefinitionsDocument.
   */
  public void endLastCompoundEdit() {
    if (_keys.size()==0) {
      return;//NOTE: This can happen if for example uncomment lines does not modify any text: throw new IllegalStateException("There should currently be an open compound edit.");
    }
    endCompoundEdit(_keys.get(0).intValue());
  }
  
  
  
  /**
   * Ends a compound edit.
   * @param key the key that was returned by startCompoundEdit()
   */
  public void endCompoundEdit(int key) {
    if(_keys.size() > 0) {
      if(_keys.get(0).intValue() == key) {
        CompoundEdit compoundEdit = _compoundEdits.remove(0);
        compoundEdit.end();
        
        if (compoundEdit.canUndo()) {
          if (!_compoundEditInProgress()) {
            super.addEdit(compoundEdit);
            //          if(!canUndo()){
            //            throw new RuntimeException("could not add the edit to the undomanager");
            //          }
            _notifyUndoHappened();
          }
          else {
            _compoundEdits.get(0).addEdit(compoundEdit);
          }
        }
        _keys.remove(0);
        
        // signal view to update undo state
      }
      else {
        throw new IllegalStateException("Improperly nested compound edits.");
      }
    }
  }
  
  /**
   * We are getting the last Compound Edit entered into the list.
   * This is for making a Compound edit for granular undo.
   */
  public CompoundEdit getLastCompoundEdit(){
    return _compoundEdits.get(0);
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
    if (_compoundEditInProgress()) {
      //      _notifyUndoHappened(); // added this for granular undo
      return _compoundEdits.get(0).addEdit(e);
    }
    else {
      boolean result = super.addEdit(e);
      _notifyUndoHappened();
      return result;
    }
  }
  
  /**
   * Returns whether or not a compound edit is in progress.
   * @return true iff in progress
   */
  public boolean _compoundEditInProgress() {
    return !_compoundEdits.isEmpty();
  }
  
  /**
   * returns true when a compound edit is in progress,
   * or when there are valid stored undoable edits
   * @return true iff undoing is possible
   */
  public boolean canUndo() {
    return _compoundEditInProgress() || super.canUndo();
  }
  
  /**
   * returns the presentation name for this undo,
   * or delegates to super if none is available
   * @return the undo's presentation name
   */
  public String getUndoPresentationName() {
    if (_compoundEditInProgress()) {
      return "Undo Previous Command";
    }
    else {
      return super.getUndoPresentationName();
    }
  }
  
  /**
   * Undoes the last undoable edit, or compound edit created by the user.
   * Since addition of granular undo, undoing is allowed while in the compound
   * edit state.  In this case, the edit is ended cleanly and then begun.
   */
  public void undo() {
    if (_compoundEditInProgress()) {
      while (_keys.size() > 0) {
        endCompoundEdit(_keys.get(0).intValue());
      }
      //      throw new CannotUndoException();
    }
    super.undo();
  }
  
  /**
   * Overload for undo which allows the initiator of a CompoundEdit to abondon it.
   * XXX: This has not been properly tested and very possibly may not work.
   * @param key the key returned by the last call to startCompoundEdit
   * @throws IllegalArgumentException if the key is incorrect
   */
  public void undo(int key) {
    if(_keys.get(0).intValue() == key) {
      CompoundEdit compoundEdit = _compoundEdits.get(0);
      _compoundEdits.remove(0);
      _keys.remove(0);
      
      compoundEdit.end();
      compoundEdit.undo();
      compoundEdit.die();
    }
    else {
      throw new IllegalArgumentException("Bad undo key " + key + "!");
    }
  }
  
  /**
   * overrides the inherited redo method so that an exception will
   * be thrown if redo is attempted while in the compound undo state
   */
  public void redo() {
    if(_compoundEditInProgress()) {
      while (_keys.size() > 0) {
        endCompoundEdit(_keys.get(0).intValue());
      }
      super.redo();
      //      throw new CannotRedoException();
    }
    else {
      super.redo();
    }
  }
  
  /**
   * helper method to notify the view that an undoable edit has occured
   */
  private void _notifyUndoHappened() {
    _notifier.undoableEditHappened();
  }
  
  /**
   * Informs this undo manager that the document has been saved.
   */
  public void documentSaved() {
    _savePoint = editToBeUndone();
  }
  
  /**
   * Determines if the document is in the same undo state as it was when it
   * was last saved.
   * @return true iff all changes have been undone since the last save
   */
  public boolean isModified() {
    return editToBeUndone() != _savePoint;
  }
  
  public String toString(){
    return "(CompoundUndoManager: " + id + ")";
  }
  /**
   * Used to help track down memory leaks
   */
  //  protected void finalize() throws Throwable{
  //    super.finalize();
  //  }
}
