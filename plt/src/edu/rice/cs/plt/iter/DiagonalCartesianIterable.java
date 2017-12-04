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

import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * <p>Enumerates the elements of a cartesian (or cross) product in diagonal order.  Where the
 * "index" of the ith item in an iterator is i, this class produces all pairs of values with
 * indices that sum to n before proceding to those with indices that sum to n+1.  This allows
 * the cartesian product of two infinite iterables to be methodically traversed.  Within the
 * set of pairs with indices summing to n, the order is lexographical in terms of the respective
 * indices.  For example, {@code [0, 1, 2]} crossed with itself will produce (under string concatenation)
 * {@code [00, 01, 10, 02, 11, 20, 12, 21, 22]}.  The {@code combiner} function is used, rather than simply 
 * producing {@code Pair}s, in order to provide a greater degree of flexibility.</p>
 * <p>In order to support this traversal, the set of previously-seen values must be cached in each iterator.
 * The amount of space required by an iterator after n invocations of {@code next()} is in O(sqrt(n)).</p>
 */
public class DiagonalCartesianIterable<T1, T2, R> extends AbstractIterable<R>
                                                  implements SizedIterable<R>, OptimizedLastIterable<R>,
                                                             Composite, Serializable {
  
  private final Iterable<? extends T1> _left;
  private final Iterable<? extends T2> _right;
  private final Lambda2<? super T1, ? super T2, ? extends R> _combiner;
  
  public DiagonalCartesianIterable(Iterable<? extends T1> left, Iterable<? extends T2> right,
                                   Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    _left = left;
    _right = right;
    _combiner = combiner;
  }
  
  public DiagonalCartesianIterator<T1, T2, R> iterator() {
    return new DiagonalCartesianIterator<T1, T2, R>(_left.iterator(), _right.iterator(), _combiner);
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_left, _right) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_left, _right) + 1; }
  
  public boolean isEmpty() { return IterUtil.isEmpty(_left) || IterUtil.isEmpty(_right); }

  public int size() { return size(Integer.MAX_VALUE); }
  
  public int size(int bound) {
    int size1 = IterUtil.sizeOf(_left, bound);
    if (size1 == 0) { return 0; }
    else {
      int bound2 = bound / size1;
      if (bound2 < Integer.MAX_VALUE) { bound2++; } // division must round up, not down
      int size2 = IterUtil.sizeOf(_right, bound2);
      // if this overflows, it must be negative:
      // size1*size2 <= size1 * ((bound/size1)+1) = bound + size1
      int result = size1*size2;
      return (result > bound || result < 0) ? bound : result;
    }
  }
  
  public boolean isInfinite() { return IterUtil.isInfinite(_left) || IterUtil.isInfinite(_right); }
  
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_left) && IterUtil.hasFixedSize(_right); }
  
  /** Always false: results of a lambda may be arbitrary. */
  public boolean isStatic() { return false; }
  
  public R last() { return _combiner.value(IterUtil.last(_left), IterUtil.last(_right)); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, R>
    DiagonalCartesianIterable<T1, T2, R> make(Iterable<? extends T1> left, Iterable<? extends T2> right,
                                              Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    return new DiagonalCartesianIterable<T1, T2, R>(left, right, combiner);
  }
  
  /**
   * Create a {@code DiagonalCartesianIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the permutations.
   */
  public static <T1, T2, R>
    SnapshotIterable<R> makeSnapshot(Iterable<? extends T1> left, Iterable<? extends T2> right,
                                     Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    return new SnapshotIterable<R>(new DiagonalCartesianIterable<T1, T2, R>(left, right, combiner));
  }
  
}
