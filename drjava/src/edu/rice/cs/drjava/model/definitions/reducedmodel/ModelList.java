/* $Id$ */

package edu.rice.cs.drjava;
import java.util.Set;
import java.util.HashSet;

class ModelList<T>
{
  private Node<T> _head;
	private Node<T> _tail;
	private int _length;
	private Set _listeners;
	
	ModelList()
		{
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
			_listeners = new HashSet();
		}
/**
 *Ends pointing to inserted item
 *
 *If at head: insert after head.
 *If at node: insert before node being pointed at.
 *If at tail: insert before tail.
 */
	private void insert(Node<T> point, T item)
    {
			Node<T> ins = new Node<T>(item, point.pred, point);
			point.pred.succ = ins;
			point.pred = ins;
			_length++;
    }

	/**
	 *If at head: can't remove, throw exception
	 *If at node: remove node point to following node
	 *If at tail: can't remove, throw exception
	 */
	private void remove(Node<T> point)
		{
			if ((point == _head) || (point == _tail))
				throw new RuntimeException("Can't remove head.");
			else
				{
					point.succ.pred = point.pred;
					point.pred.succ = point.succ;
					_length--;
				}
		}

	public boolean isEmpty()
		{
			return (_head.succ == _tail);
		}

	public int length()
		{
			return _length;
		}
	
	public Iterator getIterator()
		{
			Iterator result = new Iterator();
			_listeners.add(result);
			return result;
		}

	private static class Node<T>
	{
		Node<T> pred;
		Node<T> succ;
		private T _item;

		Node()
			{
				_item = null;
				pred = this;
				succ = this;
			}
		
		Node(T item, Node<T> previous, Node<T> successor)
			{
				_item = item;
				pred = previous;
				succ = successor;
			}

		T getItem()
			{
				return _item;
			}
	}

	class Iterator
	{
		private Node<T> _point;
		private int _pos;
		private int _moved;

		private final int MOVED_PREV = 0;
		private final int MOVED_NEXT = 1;
		
		public Iterator()
			{
				_point = ModelList.this._head;
				_pos = 0;
				_moved = MOVED_PREV;
				ModelList.this._listeners.add(this);
			}

		private Iterator(Iterator iter)
			{
				_point = iter._point;
				_pos = iter._pos;
				_moved = iter._moved;
				ModelList.this._listeners.add(this);
			}

		public Iterator copy()
			{
				return new Iterator(this);
			}


		public boolean eq(Object thing)
			{
				return this._point == ((Iterator)(thing))._point;
			}
		
		public void setTo(Iterator it)
			{
				this._point = it._point;
				this._pos = it._pos;
				this._moved = it._moved;
			}

		public boolean atStart()
			{
				return (_point == ModelList.this._head);
			}
		
		public boolean atEnd()
			{
				return (_point == ModelList.this._tail);
			}

		public boolean atFirstItem()
			{
				return (_point.pred == ModelList.this._head);
			}
		
		public boolean atLastItem()
			{
				return (_point.succ == ModelList.this._tail);
			}
		
		public T current()
			{
				if (atStart())
					{						
						throw new RuntimeException("Attempt to call current on an " +
																			 "iterator in the initial position");
					}
				else if (atEnd())
					{
						throw new RuntimeException("Attempt to call current on an " +
																			 "iterator in the final position");
					}
				else						
					return _point.getItem();
			}

		public T prevItem()
			{
				if (atFirstItem() || atStart() || ModelList.this.isEmpty())
					{
						throw new RuntimeException("No more previous items.");
					}
				else
					return _point.pred.getItem();
			}

		public T nextItem()
			{
				if (atLastItem() || atEnd() || ModelList.this.isEmpty())
					{
						throw new RuntimeException("No more following items.");
					}
				else
					return _point.succ.getItem();
			}
		
		/**
		 *if at the containing list's head, we need to move to the next node
		 * to perform the insert properly.  Otherwise, we'll get a null pointer
		 * exception because the function will try to insert the new item
		 * before the head.
		 *
		 * Ends pointing to inserted item.
		 */
		public void insert(T item)
			{
				//so as not to insert at head
				if ( this.atStart() )
					next();
				ModelList.this.insert(_point, item);
				_point = _point.pred; //puts pointer on inserted item				
				notifyOfInsert(_pos);
				//because notifyOfInsert will change the position of this iterator
				//we must change it back.
				_pos -= 1;
				
			}

		/**
		 *1)Ends pointing to the node following the removed node.
		 *2)If head or tail removed throws exception
		 */
		public void remove()
			{
				Node<T> tempNode = _point.succ;
				ModelList.this.remove(_point);
				_point = tempNode;
				notifyOfRemove(_pos, _point);				
			}

		public void prev()
			{
				if (atStart())
					throw new RuntimeException("Can't cross list boundary.");
				_point = _point.pred;
				_moved = MOVED_PREV;
				_pos--;
			}

		public void next()
			{
				if (atEnd())
					throw new RuntimeException("Can't cross list boundary.");
				_point = _point.succ;
				_moved = MOVED_NEXT;
				_pos++;
			}

		/**
		 *1)Two iterators pointing to same node: do nothing
		 *2)Iterator 2 is before iterator 1    : remove between iterator 2 and
		 *                                       iterator 1
		 *3)Iterator 1 is before iterator 2    : remove between iterator 1 and
		 *                                       iterator 2
		 *
		 *Does not remove points iterators point to.
		 */
		public void collapse(Iterator iter)
			{
				int leftPos;
				int rightPos;
				Node<T> rightPoint;

				if (this._pos > iter._pos)
					{
						leftPos = iter._pos;
						rightPos = this._pos;
						rightPoint = this._point;
						
						this._point.pred = iter._point;
						iter._point.succ = this._point;
						//determine new length
						ModelList.this._length -= this._pos - iter._pos - 1;
						notifyOfCollapse(leftPos, rightPos, rightPoint);
					}
				else if (this._pos < iter._pos)
					{
						leftPos = this._pos;
						rightPos = iter._pos;
						rightPoint = iter._point;
						
						iter._point.pred = this._point;
						this._point.succ = iter._point;
															 
						ModelList.this._length -= iter._pos - this._pos - 1;
						notifyOfCollapse(leftPos, rightPos, rightPoint);
					}
				else // this._pos == iter._pos
					{
					}
			}

		private void notifyOfInsert(int pos)
			{
				java.util.Iterator iter = ModelList.this._listeners.iterator();
				while (iter.hasNext())
					{
						Iterator next = (Iterator)iter.next();
						if ( next._pos < pos )
							{
								// do nothing								
							}
						else // ( next._pos == pos ) || next._pos > pos
							{
								next._pos += 1;
							}
					}
			}
		
		private void notifyOfRemove(int pos, Node<T> point)
			{
				java.util.Iterator iter = ModelList.this._listeners.iterator();
				while (iter.hasNext())
					{
						Iterator next = (Iterator)iter.next();
						if ( next._pos < pos )
							{
								// do nothing								
							}
						else if ( next._pos == pos )
							{
								next._point = point;
							}
						else // next._pos > pos
							{
								next._pos -= 1;
							}
					}
			}

		private void notifyOfCollapse(int leftPos,
																	int rightPos,
																	Node<T> rightPoint)
			{
				java.util.Iterator iter = ModelList.this._listeners.iterator();
				while (iter.hasNext())
					{
						Iterator next = (Iterator)iter.next();
						if ( next._pos <= leftPos )
							{
								// do nothing								
							}
						else if (( next._pos > leftPos ) && ( next._pos <= rightPos ))
							{
								next._pos = leftPos + 1;
								next._point = rightPoint;
							}
						else // next._pos > rightPos
							{
								next._pos -= (rightPos - leftPos - 1);
							}
					}
								
			}
	}
}




