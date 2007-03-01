/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import edu.rice.cs.plt.collect.WeakHashSet;
import java.util.Set;

/** A doubly-linked list class with header and trailer nodes. Allows multiple iterators to make modifications to the 
  * same list without failing unlike the iterators for java.util.*List.
  * @version $Id$
  */
class ModelList<T> {
  private Node<T> _head;
  private Node<T> _tail;
  /** keep track of length for constant time length lookup */
  private int _length;
  /** a set of objects that can trigger and listen for updates to the list */
  private Set<Iterator> _listeners;

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
    _listeners = new WeakHashSet<Iterator>();
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

  private void addListener(Iterator that) { _listeners.add(that); }

  private void removeListener(Iterator that) { _listeners.remove(that); }

  public int listenerCount() { return _listeners.size(); }
  
  /** Returns true if the list is empty. */
  public boolean isEmpty() { return _head._next == _tail; }

  public int length() { return _length; }

  /** Create a new iterator for this list and register it as one of the listeners which are notified when the list is
    * updated.
    */
  public Iterator getIterator() { return new Iterator(); }

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

  /** The uterator class for ModelList.  Package private instead of private so that it can be extended.  The methods of
    * this class constitute the only public interface for traversing and modifying ModelList objects (other than 
    * insertFront).  These iterators support concurrent modification from within the same thread.  They are NOT thread 
    * safe.
    */
  class Iterator {
    private Node<T> _point;  // the current node
    private int _pos;        // the offset of _point within the list; _head has index 0

    /** Standard constructor that creates an iterator pointing to the list head (_head) and adds it the listeners. */
    public Iterator() {
      _point = _head;
      _pos = 0;
      addListener(this);
    }

    /** Copy constructor that creates a copy of an existing iterator and adds it to the listeners. */
    public Iterator(Iterator iter) {
      _point = iter._point;
      _pos = iter._pos;
      addListener(this);
    }

    public Iterator copy() { return new Iterator(this); }

    /** Tests "that" for equality with "this". */
    public boolean eq(Iterator that) { return _point == that._point; }

    /** Force "this" iterator to take the values of "that". */
    public void setTo(Iterator that) {
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
      assert ! atStart() && ! atEnd();
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
    public void collapse(Iterator iter) {
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
      for (Iterator listener : _listeners) {
        int lisPos = listener._pos;
        if (lisPos >= pos) listener._pos = lisPos + 1;
      } 
    }

    /** Notifies the iterators in _listeners that a node has been removed. */
    private void notifyOfRemove(int pos, Node<T> point) {
      for (Iterator listener : _listeners) {
        int lisPos = listener._pos;
        if (lisPos == pos) listener._point = point;
        else if (lisPos > pos) listener._pos = lisPos - 1;
      }
    }

    /** Notifies the iterators in _listeners that a range of nodes has been collapsed. */
    private void notifyOfCollapse(int leftPos, int rightPos, Node<T> rightPoint) {
      for (Iterator listener : _listeners) {
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
