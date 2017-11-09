/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.util.LinkedList;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * <p>Enumerates the elements of a cartesian (or cross) product in diagonal order.  Where the
 * "index" of the ith item in an iterator is i, this class produces all pairs of values with
 * indices that sum to n before proceding to those with indices that sum to n+1.  This allows
 * the cartesian product of two infinite iterators to be methodically traversed.  Within the
 * set of pairs with indices summing to n, the order is lexographical in terms of the respective
 * indices.  For example, {@code [0, 1, 2]} crossed with itself will produce (under string concatenation)
 * {@code [00, 01, 10, 02, 11, 20, 12, 21, 22]}.  The {@code combiner} function is used, rather than simply 
 * producing {@code Pair}s, in order to provide a greater degree of flexibility.  {@link Iterator#remove} 
 * is not supported.</p>
 * <p>In order to support this traversal, the set of previously-seen values must be cached.  The amount
 * of space required by this iterator after n invocations of {@code next()} is in O(sqrt(n)).</p>
 */
public class DiagonalCartesianIterator<T1, T2, R> extends ReadOnlyIterator<R> implements Composite {
  
  private final Lambda2<? super T1, ? super T2, ? extends R> _combiner;
  private final Iterator<? extends T1> _left;
  private final Iterator<? extends T2> _right;
  private LinkedList<T1> _leftCache;
  private Iterator<T1> _leftCacheIter;
  private LinkedList<T2> _rightCache;
  private Iterator<T2> _rightCacheIter;
  
  public DiagonalCartesianIterator(Iterator<? extends T1> left, Iterator<? extends T2> right,
                                   Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    _combiner = combiner;
    _left = left;
    _right = right;
    _leftCache = new LinkedList<T1>();
    _rightCache = new LinkedList<T2>();
    _leftCacheIter = _leftCache.iterator();
    _rightCacheIter = _rightCache.iterator();
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_left, _right) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_left, _right) + 1; }
  
  public boolean hasNext() {
    if (_left.hasNext()) { return _right.hasNext() || !_rightCache.isEmpty(); }
    else if (_right.hasNext()) { return !_leftCache.isEmpty(); }
    else {
      return _leftCacheIter.hasNext() && _rightCacheIter.hasNext() || // there's more in the current traversal
        _leftCache.size() > 1 && _rightCache.size() > 1; // there are leftovers to create a new traversal
    }
//    return (_left.hasNext() || !_leftCache.isEmpty()) && (_right.hasNext() || !_rightCache.isEmpty()) ||
//      ;
//    return _leftCacheIter.hasNext() && _rightCacheIter.hasNext() || 
//      _left.hasNext() && (_right.hasNext() || !_rightCache.isEmpty()) ||
//      _right.hasNext() && !_leftCache.isEmpty();
  }
  
  public R next() {
    if (!_leftCacheIter.hasNext() || !_rightCacheIter.hasNext()) {
      if (_left.hasNext()) { _leftCache.addLast(_left.next()); }
      else if (!_rightCache.isEmpty()) { _rightCache.removeLast(); }
      if (_right.hasNext()) { _rightCache.addFirst(_right.next()); }
      else if (!_leftCache.isEmpty()) { _leftCache.removeFirst(); }
      _leftCacheIter = _leftCache.iterator();
      _rightCacheIter = _rightCache.iterator();
    }
    // This may cause an exception, which is OK.
    return _combiner.value(_leftCacheIter.next(), _rightCacheIter.next());
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, R>
    DiagonalCartesianIterator<T1, T2, R> make(Iterator<? extends T1> left, Iterator<? extends T2> right,
                                              Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    return new DiagonalCartesianIterator<T1, T2, R>(left, right, combiner);
  }
  
}
