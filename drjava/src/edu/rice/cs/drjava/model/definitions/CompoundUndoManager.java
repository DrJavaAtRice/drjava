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

package edu.rice.cs.drjava.model.definitions;

import java.awt.EventQueue;
import java.util.LinkedList;
import javax.swing.undo.*;

import edu.rice.cs.drjava.model.GlobalEventNotifier;

/** Extended UndoManager with increased functionality.  Can handle aggregating multiple edits into one for the purposes
  * of undoing and redoing.  It exposes editToBeUndone and editToBeRedone (under new names); they are protected methods
  * in UndoManager.  The public methods that involve composite state are synchronized, so this manager can be accessed
  * outside of the event thread.  The internal data structures _compoundEdits and _keys are not thread safe but they
  * only accessed only by synchronized methods.  The synchronization scheme (locking on this) follows UndoManager.
  * @version $Id: CompoundUndoManager.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class CompoundUndoManager extends UndoManager {
  
  static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("CompoundUndoManager.txt", false);
  
  private static volatile int counter = 0;
  
  private final int id;
  
  /** The compound edits we are storing. Not thread safe! */
  private final LinkedList<CompoundEdit> _compoundEdits;
  
  /** The keys for the CompoundEdits we are storing. */
  private final LinkedList<Integer> _keys;
  
  /** The next key to use for nested CompoundEdits. */
  private volatile int _nextKey;
  
  /** The last edit that was performed before the last save. */
  private volatile UndoableEdit _savePoint;
  
  /** Keeps track of the listeners to this undo manager. */
  private final GlobalEventNotifier _notifier;
  
  /** Standard constructor. */
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
  
  /** Starts a compound edit.
    * @return the key for the compound edit
    */
  public /* synchronized */ int startCompoundEdit() {
    _compoundEdits.add(0, new CompoundEdit());
    _keys.add(0, Integer.valueOf(_nextKey));
    if (_nextKey < Integer.MAX_VALUE) _nextKey++;
    else _nextKey = Integer.MIN_VALUE;
    return _keys.get(0).intValue();
  }
  
  /** Ends the last compound edit that was created.  Used when a compound edit is created by the _undoListener in
    * DefinitionsPane and the key is not known in DefinitionsDocument.
    */
  public /* synchronized */ void endLastCompoundEdit() {
    if (_keys.size() == 0) return;
    // NOTE: The preceding can happen if for example uncomment lines does not modify any text.
    endCompoundEdit(_keys.get(0).intValue());
  }
  
  /** Ends a compound edit.
    * @param key the key that was returned by startCompoundEdit()
    */
  public /* synchronized */ void endCompoundEdit(int key) {
    if (_keys.size() == 0) return;
    
    if (_keys.get(0) == key) {
      _keys.remove(0);
      final CompoundEdit ce = _compoundEdits.remove(0);
      
      ce.end();
      if (ce.canUndo()) {
        if (! _compoundEditInProgress()) {
          super.addEdit(ce);
          _notifyUndoHappened();
        }
        else {
          _compoundEdits.get(0).addEdit(ce);
        }
      } 
    }
    else throw new IllegalStateException("Improperly nested compound edits.");
  }
  
  /** Gets the last Compound Edit entered into the list. Used in making a Compound edit for granular undo. */
  public /* synchronized */ CompoundEdit getLastCompoundEdit() { return _compoundEdits.get(0); }
  
  /** Gets the next undo.
    * @return the next undo
    */
  public UndoableEdit getNextUndo() { return editToBeUndone(); }
  
  /** Gets the next redo.
    * @return the next redo
    */
  public UndoableEdit getNextRedo() { return editToBeRedone(); }
  
  /** Adds an edit.  Checks whether or not the current edit is a compound edit.
    * @param e the edit to be added
    * @return true if the add is successful, false otherwise
    */
  public /* synchronized */ boolean addEdit(UndoableEdit e) {
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
  
  /** Returns whether or not a compound edit is in progress.
    * @return true iff in progress
    */
  public /* synchronized */ boolean _compoundEditInProgress() { return ! _compoundEdits.isEmpty(); }
  
  /** Returns true when a compound edit is in progress,  or when there are valid stored undoable edits
    * @return true iff undoing is possible
    */
  public /* synchronized */ boolean canUndo() {
    LOG.log("canUndo: _compoundEditInProgress() = "+_compoundEditInProgress()+", super.canUndo() = "+super.canUndo());
    LOG.log("    "+_compoundEdits);
    return _compoundEditInProgress() || super.canUndo(); }
  
  /** Returns the presentation name for this undo, or delegates to super if none is available
    * @return the undo's presentation name
    */
  public /* synchronized */ String getUndoPresentationName() {
    if (_compoundEditInProgress()) return "Undo Previous Command";
    return super.getUndoPresentationName();
  }
  
  /** Undoes the last undoable edit, or compound edit created by the user. */
  public /* synchronized */ void undo() {
    endCompoundEdit();
    super.undo();
  }
  
  // Not currently used.
//  /** Overload for undo which allows the initiator of a CompoundEdit to abandon it.
//    * WARNING: this has been used to date and has not been properly tested and very possibly may not work.
//    * @param key the key returned by the last call to startCompoundEdit
//    * @throws IllegalArgumentException if the key is incorrect
//    */
//  public synchronized void undo(int key) {
//    if (_keys.get(0) == key) {
//      final CompoundEdit ce = _compoundEdits.get(0);
//      _compoundEdits.remove(0);
//      _keys.remove(0);
//
//      EventQueue.invokeLater(new Runnable() { 
//        public void run() { 
//          ce.end();
//          ce.undo();
//          ce.die(); 
//        } 
//      });  // unsafe methods inherited from CompoundEdit
//    }
//    else throw new IllegalArgumentException("Bad undo key " + key + "!");
//  }
  
  /** Overrides redo so that any compound edit in progress is ended before the redo is performed. */
  public /* synchronized */ void redo() {
    endCompoundEdit();
    super.redo();
  }
  
  /** Helper method to notify the view that an undoable edit has occured. Note that lock on this is not held by
    * the event thread (even if called from event thread) when notification happens. 
    */
  private void _notifyUndoHappened() { 
    // Use SwingUtilities.invokeLater so that notification is deferred when running in the event thread.
    EventQueue.invokeLater(new Runnable() { public void run() { _notifier.undoableEditHappened(); } });
  }
  
  /** Ends the compoundEdit in progress if any.  Used by undo(), redo(), documentSaved(). */
  private /* synchronized */ void endCompoundEdit() {
    Integer[] keys = _keys.toArray(new Integer[_keys.size()]);  // unit testing ran into a concurrent modification exception without this copying operation
    if (_compoundEditInProgress()) {
      for (int key: keys) endCompoundEdit(key);
    }
  }
  
  /** Informs this undo manager that the document has been saved. */
  public /* synchronized */ void documentSaved() {
    endCompoundEdit();
    _savePoint = editToBeUndone(); 
//    Utilities.showDebug("_savePoint := " + _savePoint);
  }
  
  /** Determines if the document is in the same undo state as it was when it was last saved.
    * @return true iff all changes have been undone since the last save
    */
  public /* synchronized */ boolean isModified() { 
//    Utilities.showDebug("_savePoint = " + _savePoint + " editToBeUndone() = " + editToBeUndone());
    return editToBeUndone() != _savePoint; 
  }
  
  public String toString() { return "(CompoundUndoManager: " + id + ")"; }
  
  /** Used to help track down memory leaks. */
  //  protected void finalize() throws Throwable{
  //    super.finalize();
  //  }
}
