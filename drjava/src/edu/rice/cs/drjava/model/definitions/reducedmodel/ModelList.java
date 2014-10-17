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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import edu.rice.cs.plt.collect.WeakHashSet;
import java.util.Set;

/** A doubly-linked list class with header and trailer nodes. Allows multiple iterators to make modifications to the 
  * same list without failing unlike the iterators for java.util.*List.
  * @version $Id: ModelList.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
class ModelList<T> {
  private Node<T> _head;
  private Node<T> _tail;
  /** length of this list; supports constant time length lookup */
  private int _length;
  /** a set of objects that can trigger and listen for updates to the list */
  private Set<ModelIterator> _listeners;

  /** Constructor.  Initializes the head and tail nodes, as well as the listener table and the length variable. */
  ModelList() {
    // This node is the only node that exists in an empty list.
    // If an Iterator points to this node, the iterator is considered to be in "initial position."
    _head = new Node<T>();
    _tail = new Node<T>();
    _head._prev = null;
    _head._next = _tail;
    _tail._prev = _head;
    _tail._next = null;
    _length = 0;
    
    /* We use a WeakHashSet so that listeners do not leak. That is, even if the dispose method is not called, when they
     * are no longer strongly referenced, they will be automatically removed from the listener set. */
    _listeners = new WeakHashSet<ModelIterator>();
  }

  public void insertFront(T item) { insert(_head._next, item); }
  
  /** Insert a node immediately before the specified point. Returns the inserted node. Assumes point is not head. */
  private Node<T> insert(Node<T> point, T item) {
    assert point != _head;
    Node<T> newNode = point.insert(item);
    _length++;
    return newNode;
  }

  /** Remove a node from the list.  Assumes point is not head or tail. */
  private void remove(Node<T> point) {
    assert point != _head && point != _tail;
    point.remove();
    _length--;
  } 

  private void addListener(ModelIterator that) { _listeners.add(that); }

  private void removeListener(ModelIterator that) { _listeners.remove(that); }

  public int listenerCount() { return _listeners.size(); }
  
  /** Returns true if the list is empty. */
  public boolean isEmpty() { return _head._next == _tail; }

  public int length() { return _length; }

  /** Create a new iterator for this list and register it as one of the listeners which are notified when the list is
    * updated.
    */
  public ModelIterator getIterator() { return new ModelIterator(); }

  /** The Node class for ModelLists.  The _prev and _next pointers are mutable.  The _item field is null in _head and _tail. */
  private static class Node<T> {
    Node<T> _prev;
    Node<T> _next;
    T _item;

    /** Constructor for _head and _tail nodes. */
    Node() { }

    /** Constructor for nodes containing data. */
    Node(T item, Node<T> pred, Node<T> succ) {
      _item = item;
      _prev = pred;
      _next = succ;
    }
    
    /** Insert a new node before "this".  Returns the new node. Assumes that "this" is not the head node. */
    Node<T> insert(T item) {
      assert _prev != null;
      Node<T> newNode = new Node<T>(item, _prev, this);
      _prev._next = newNode;
      _prev = newNode;
      return newNode;
    }
    
    /** Remove the current node. Assumes that this is neither _head not _tail. */
    void remove() {
      assert _prev != null && _next != null;
      _prev._next = _next;
      _next._prev = _prev;
    }
  }

  /** The iterator class for ModelList.  Package private instead of private so that it can be extended.  The methods of
    * this class constitute the only public interface for traversing and modifying ModelList objects (other than 
    * insertFront).  These iterators support concurrent modification from within the same thread.  They are NOT thread 
    * safe.
    */
  class ModelIterator {
    private Node<T> _point;  // the current node
    private int _pos;        // the offset of _point within the list; _head has index 0

    /** Standard constructor that creates an iterator pointing to the list head (_head) and adds it the listeners. */
    public ModelIterator() {
      _point = _head;
      _pos = 0;
      addListener(this);
    }

    /** Copy constructor that creates a copy of an existing iterator and adds it to the listeners. */
    public ModelIterator(ModelIterator iter) {
      _point = iter._point;
      _pos = iter._pos;
      addListener(this);
    }

    public ModelIterator copy() { return new ModelIterator(this); }

    /** Tests "that" for equality with "this". */
    public boolean eq(ModelIterator that) { return _point == that._point; }

    /** Force "this" iterator to take the values of "that". */
    public void setTo(ModelIterator that) {
      _point = that._point;
      _pos = that._pos;
    }

    /** Disposes of an iterator by removing it from the listeners.  If an iterator becomes unreachable, it is 
      * automatically reclaimed as part of system garbage collection.  The manual use of dispose() reduces the
      * cost of notifying the listeners because it reduces the size of the listener set.
      */
    public void dispose() { removeListener(this); }

    /** Return true if we're pointing at the head.*/
    public boolean atStart() { return _point == _head; }

    /** Return true if we're pointing at the tail. */
    public boolean atEnd() { return _point == _tail; }

    /** Return true if we're pointing at the node after the head. */
    public boolean atFirstItem() { return _point._prev == _head; }

    /** Return true if we're pointing at the node before the tail. */
    public boolean atLastItem() { return _point._next == _tail; }

    /** Return the item associated with the current node. */
    public T current() {
//      assert ! atStart() && ! atEnd();
      return _point._item;
    }

    /** Returns the item associated with the node before the current node. */
    public T prevItem() {
      assert ! atStart() && ! isEmpty() && ! atFirstItem();
      return _point._prev._item;
    }

    /** Returns the item associated with the node after the current node. */
    public T nextItem() {
      assert ! atStart() && ! isEmpty() && ! atLastItem();
      return _point._next._item;
    }
    
    public int pos() { return _pos; }

    /** Inserts an item before the current item.  If current is head, we need to move to the next node
      * to perform the insert properly.  Otherwise, we'll get a null pointer exception because the function will try 
      * to insert the new item before the head.  Ends pointing to inserted item.
      */
    public void insert(T item) {
      //so as not to insert at head
      if (atStart()) next();
      _point = ModelList.this.insert(_point, item);
      int savPos = _pos;
      notifyOfInsert(_pos);

      _pos = savPos;  // this._pos is incremented by notify; reverse this change
    }

    /** Removes the current item from the list.  Ends pointing to the node following the removed node.
      * Throws exception if performed atStart() or atEnd().
      */
    public void remove() {
      Node<T> succ = _point._next;
      ModelList.this.remove(_point);
      _point = succ;
      notifyOfRemove(_pos, succ);
    }

    /** Moves to the previous node. Throws exception atStart(). */
    public void prev() {
      assert ! atStart();
      _point = _point._prev;
      _pos--;
    }

    /** Moves to the next node. Throws exception atEnd(). */
    public void next() {
      assert ! atEnd();
      _point = _point._next;
      _pos++;
    }

    /** Delete all nodes between the current position of this and the current position of the given iterator.
      * 1) Two iterators pointing to same node: do nothing
      * 2) Iterator 2 is before iterator 1: remove between iterator 2 and iterator 1
      * 3) Iterator 1 is before iterator 2: remove between iterator 1 and iterator 2
      * Does not remove points iterators point to.
      */
    public void collapse(ModelIterator iter) {
      int itPos = iter._pos;
      int diff = Math.abs(_pos - itPos);
      if (diff <= 1) return; // _pos and iter.pos are either equal or adjacent
      
      int leftPos, rightPos;
      Node<T> leftPoint, rightPoint;
      
      if (_pos > itPos) {
        leftPos = itPos;
        leftPoint = iter._point;
        rightPos = _pos;
        rightPoint = _point;
      }
      else /* _pos < iter._pos */ {
        leftPos = _pos;
        leftPoint = _point;
        rightPos = itPos;
        rightPoint = iter._point;
      }
      
      rightPoint._prev = leftPoint;
      leftPoint._next = rightPoint;
      _length -= rightPos - leftPos - 1;  //determine new length
      notifyOfCollapse(leftPos, rightPos, rightPoint);
    }

    /** Notifies the iterators in _listeners that a node has been inserted. */
    private void notifyOfInsert(int pos) {
      for (ModelIterator listener : _listeners) {
        int lisPos = listener._pos;
        if (lisPos >= pos) listener._pos = lisPos + 1;
      } 
    }

    /** Notifies the iterators in _listeners that a node has been removed. */
    private void notifyOfRemove(int pos, Node<T> point) {
      for (ModelIterator listener : _listeners) {
        int lisPos = listener._pos;
        if (lisPos == pos) listener._point = point;
        else if (lisPos > pos) listener._pos = lisPos - 1;
      }
    }

    /** Notifies the iterators in _listeners that a range of nodes has been collapsed. */
    private void notifyOfCollapse(int leftPos, int rightPos, Node<T> rightPoint) {
      for (ModelIterator listener : _listeners) {
        int lisPos = listener._pos;
        if (lisPos <= leftPos) continue;
        if (lisPos < rightPos) {
          listener._pos = leftPos + 1;
          listener._point = rightPoint;
        }
        else { // lisPos >+ rightPos
          listener._pos = lisPos - (rightPos - leftPos - 1);
        }
      }
    }
  }
}
