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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/** TODO: convert ModelList representation to a doubly linked circular list;
 *  separate head and tail nodes are ugly and unnecessary. */

/**
 * A list class with some extra features.
 * Allows multiple iterators to make modifications to the same list
 * without failing like the iterators for java.util.*List.
 * @version $Id$
 */
class ModelList<T> {
  private Node<T> _head;
  private Node<T> _tail;
  /** keep track of length for constant time length lookup */
  private int _length;
  /** a set of objects that can trigger and listen for updates to the list */
  private Set<ModelList<T>.Iterator> _listeners;
  
  /**
   * Constructor.
   * Initializes the head and tail nodes, as well as the listener table
   * and the length variable.
   */
  ModelList() {
    // This node is the only node that exists in an empty list.
    // If an Iterator points to this node, the iterator is considered
    // to be in "initial position."
    _head = new Node<T>();
    _tail = new Node<T>();
    _head.pred = null;
    _head.succ = _tail;
    _tail.pred = _head;
    _tail.succ = null;
    _length = 0;
    _listeners = new HashSet<ModelList<T>.Iterator>();
  }
  
  /**
   * Insert an item before a certain node in the list.
   * Can never be called on head node.
   */
  private void insert(Node<T> point, T item) {
    Node<T> ins = new Node<T>(item, point.pred, point);
    point.pred.succ = ins;
    point.pred = ins;
    _length++;
  }
   
  public void insertFront(T item) {
    ModelList<T>.Iterator it = new ModelList.Iterator();  // Should be ModelList<T>.Iterator() but current Generic Java compiler rejects it.
    it.insert(item);
    it.dispose();
  }
  
  /**
   * Remove a node from the list.
   * Can't remove head or tail node - exception thrown.
   */
  private void remove(Node<T> point) {
    if ((point == _head) || (point == _tail))
      throw new RuntimeException("Can't remove head.");
    else
    {
      point.succ.pred = point.pred;
      point.pred.succ = point.succ;
      _length--;
    }
  }
  
  private void addListener(ModelList<T>.Iterator thing) {
    this._listeners.add(thing);
  }
  
  private void removeListener(ModelList<T>.Iterator thing) {
    this._listeners.remove(thing);
  }

  public int listenerCount() {
    return _listeners.size();
  }
  /**
   * Returns true if the list is empty.
   */
  public boolean isEmpty() {
    return (_head.succ == _tail);
  }
  
  public int length() {
    return _length;
  }
  
  /**
   * Create a new iterator for this list.  The constructor for the
   * iterator adds itself to the list's listeners.  The iterator
   * must be notified of changes so it does not become out-of-date.
   */
  public ModelList<T>.Iterator getIterator() {
    return new ModelList.Iterator();   // Should be ModelList<T>.Iterator() but not accepted by current Generic Java compiler;
  }
  
  
  /**
   * A node class for the list.
   * Each node has a successor and predecessor, which is also a node
   * as well as an item of type T.
   * We keep it private inside this class so Node is never shown to
   * the outside world to wreak havoc upon the list.
   */
  private static class Node<T> {
    Node<T> pred;
    Node<T> succ;
    private T _item;
    
    Node() {
      _item = null;
      pred = this;
      succ = this;
    }
    
    Node(T item, Node<T> previous, Node<T> successor) {
      _item = item;
      pred = previous;
      succ = successor;
    }
    
    T getItem() {
      return _item;
    }
  }
  
  /**
   * Iterators for model list.
   * The iterators are intimately coupled with the ModelList to which they
   * belong.  They are the only public interface for manipulating
   * ModelList.  The iterators are also fail-safe with regards to
   * manipulation of the same list, although probably not thread-safe.
   */
  class Iterator {
    private Node<T> _point;
    private int _pos;
    
    /**
     * Constructor.
     * Initializes an iterator to point to its list's head.
     */
    public Iterator() {
      _point = ModelList.this._head;
      _pos = 0;
      ModelList.this.addListener(this);
    }
    
    /**
     * Copy constructor.
     * Creates a new iterator with the same values as the progenitor.
     * Adds it to the list's set of listeners.
     */
    public Iterator(ModelList<T>.Iterator iter) {
      _point = iter._point;
      _pos = iter._pos;
      ModelList.this.addListener(this);
    }
    
    public Iterator copy() {
      return new Iterator(this);
    }
    
    /**
     * an equals test
     */
    public boolean eq(Object thing) {
      return this._point == ((ModelList.Iterator)(thing))._point;
    }
    
    /**
     * Force this iterator to take the values of the given iterator.
     */
    public void setTo(ModelList<T>.Iterator it) {
      this._point = it._point;
      this._pos = it._pos;
    }
    
    /**
     * Disposes of an iterator by removing it from the list's set of
     * listeners.  When an iterator is no longer necessary, it
     * should be disposed of.  Otherwise, there will be memory leaks
     * because the listener set of the list provides a root reference
     * for the duration of the list's existence.  What this means is that
     * unless an iterator is disposed of, it will continue to exist even
     * after garbage collection as long as the list itself is not
     * garbage collected.
     */
    public void dispose() {
      ModelList.this.removeListener(this);
    }
    
    /**
     * Return true if we're pointing at the head.
     */
    public boolean atStart() {
      return (_point == ModelList.this._head);
    }
    
    /**
     * Return true if we're pointing at the tail.
     */
    public boolean atEnd() {
      return (_point == ModelList.this._tail);
    }
    
    /**
     * Return true if we're pointing at the node after the head.
     */
    public boolean atFirstItem() {
      return (_point.pred == ModelList.this._head);
    }
    
    /**
     * Return true if we're pointing at the node before the tail.
     */
    public boolean atLastItem() {
      return (_point.succ == ModelList.this._tail);
    }
    
    /**
     * Return the item associated with the current node.
     */
    public T current() {
      if (atStart()) {
        throw new RuntimeException("Attempt to call current on an " +
                                   "iterator in the initial position");
      }
      else if (atEnd()) {
        throw new RuntimeException("Attempt to call current on an " +
                                   "iterator in the final position");
      }
      else {
        return _point.getItem();
      }
    }
    
    /**
     * Return the item associated with the node before the current node.
     */
    public T prevItem() {
      if (atFirstItem() || atStart() || ModelList.this.isEmpty()) {
        throw new RuntimeException("No more previous items.");
      }
      else {
        return _point.pred.getItem();
      }
    }
    
    /**
     * Return the item associated with the node after the current node.
     */
    public T nextItem() {
      if (atLastItem() || atEnd() || ModelList.this.isEmpty()) {
        throw new RuntimeException("No more following items.");
      }
      else {
        return _point.succ.getItem();
      }
    }
    
    /**
     * Insert an item before the current item.
     * If at the containing list's head, we need to move to the next node
     * to perform the insert properly.  Otherwise, we'll get a null pointer
     * exception because the function will try to insert the new item
     * before the head.
     *
     * Ends pointing to inserted item.
     */
    public void insert(T item) {
      //so as not to insert at head
      if (this.atStart()) {
        next();
      }
      ModelList.this.insert(_point, item);
      _point = _point.pred; //puts pointer on inserted item
      notifyOfInsert(_pos);
      
      //because notifyOfInsert will change the position of this iterator
      //we must change it back.
      _pos -= 1;
    }
    
    /**
     * Remove the current item from the list.
     * Ends pointing to the node following the removed node.
     * Throws exception if performed atStart() or atEnd().
     */
    public void remove() {
      Node<T> tempNode = _point.succ;
      ModelList.this.remove(_point);
      _point = tempNode;
      notifyOfRemove(_pos, _point);
    }
    
    /**
     * Move to the previous node.
     * Throws exception atStart().
     */
    public void prev() {
      if (atStart()) {
        throw new RuntimeException("Can't cross list boundary.");
      }
      _point = _point.pred;
      _pos--;
    }
    
    /**
     * Move to the next node.
     * Throws exception atEnd().
     */
    public void next() {
      if (atEnd()) {
        throw new RuntimeException("Can't cross list boundary.");
      }
      _point = _point.succ;
      _pos++;
    }
    
    /**
     * Delete all nodes between the current position of this and the
     * current position of the given iterator.
     *
     * 1)Two iterators pointing to same node: do nothing
     * 2)Iterator 2 is before iterator 1    : remove between iterator 2 and
     *                                       iterator 1
     * 3)Iterator 1 is before iterator 2    : remove between iterator 1 and
     *                                       iterator 2
     *
     *D oes not remove points iterators point to.
     */
    public void collapse(ModelList<T>.Iterator iter) {
      int leftPos;
      int rightPos;
      Node<T> rightPoint;
      
      if (this._pos > iter._pos) {
        leftPos = iter._pos;
        rightPos = this._pos;
        rightPoint = this._point;
        
        this._point.pred = iter._point;
        iter._point.succ = this._point;
        //determine new length
        ModelList.this._length -= this._pos - iter._pos - 1;
        notifyOfCollapse(leftPos, rightPos, rightPoint);
      }
      else if (this._pos < iter._pos) {
        leftPos = this._pos;
        rightPos = iter._pos;
        rightPoint = iter._point;
        
        iter._point.pred = this._point;
        this._point.succ = iter._point;
        
        ModelList.this._length -= iter._pos - this._pos - 1;
        notifyOfCollapse(leftPos, rightPos, rightPoint);
      }
      else { // this._pos == iter._pos
      }
    }
    
    /**
     * When an iterator inserts an item, it notifies other iterators
     * in the set of listeners so they can stay updated.
     */
    private void notifyOfInsert(int pos) {
      java.util.Iterator<ModelList<T>.Iterator> iter =
        ModelList.this._listeners.iterator();
      while (iter.hasNext()) {
        ModelList<T>.Iterator next = iter.next();
        if ( next._pos < pos ) {
          // do nothing
        }
        else { // ( next._pos == pos ) || next._pos > pos
          next._pos += 1;
        }
      }
    }
    
    /**
     * When an iterator removes an item, it notifies other iterators
     * in the set of listeners so they can stay updated.
     */
    private void notifyOfRemove(int pos, Node<T> point) {
      java.util.Iterator<ModelList<T>.Iterator> iter =
        ModelList.this._listeners.iterator();
      while (iter.hasNext()) {
        ModelList<T>.Iterator next = iter.next();
        if ( next._pos < pos ) {
          // do nothing
        }
        else if ( next._pos == pos ) {
          next._point = point;
        }
        else { // next._pos > pos
          next._pos -= 1;
        }
      }
    }
    
    /**
     * When an iterator collapses part of the list, it notifies other iterators
     * in the set of listeners so they can stay updated.
     */
    private void notifyOfCollapse(int leftPos, int rightPos, Node<T> rightPoint) {
      java.util.Iterator<ModelList<T>.Iterator> iter =
        ModelList.this._listeners.iterator();
      while (iter.hasNext()) {
        ModelList<T>.Iterator next = iter.next();
        if ( next._pos <= leftPos ) {
          // do nothing
        }
        else if (( next._pos > leftPos ) && ( next._pos <= rightPos )) {
          next._pos = leftPos + 1;
          next._point = rightPoint;
        }
        else { // next._pos > rightPos
          next._pos -= (rightPos - leftPos - 1);
        }
      }
    }
  }
}
