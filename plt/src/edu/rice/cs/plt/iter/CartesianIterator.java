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
import java.util.NoSuchElementException;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * Enumerates the elements of a cartesian (or cross) product.  For each element in the
 * iterator {@code left}, the result of a lambda {@code combiner}, applied to that element
 * and each of the elements of the iterable {@code right}, is produced.  Since iteration
 * of {@code right} occurs in an "inner loop," {@code right} must be finite (at least for 
 * most interesting results...), and is an {@code Iterable} rather than an {@code Iterator}.
 * The {@code combiner} function is used, rather than simply producing {@code Pair}s, in
 * order to provide a greater degree of flexibility.  {@link Iterator#remove} is not supported.
 */
public class CartesianIterator<T1, T2, R> extends ReadOnlyIterator<R> implements Composite {
  
  private final Lambda2<? super T1, ? super T2, ? extends R> _combiner;
  private final Iterator<? extends T1> _left;
  private Iterator<? extends T2> _right;
  private T1 _currentLeft;
  private boolean _done;
  private final Iterable<? extends T2> _rightIterable;
  
  public CartesianIterator(Iterator<? extends T1> left, Iterable<? extends T2> right,
                           Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    _combiner = combiner;
    _left = left;
    _right = right.iterator();
    if (_left.hasNext() && _right.hasNext()) { _done = false; _currentLeft = _left.next(); }
    else { _done = true; }
    _rightIterable = right;
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_left, _right) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_left, _right) + 1; }
  
  public boolean hasNext() { return !_done; }
  
  public R next() {
    if (_done) { throw new NoSuchElementException(); }
    else {
      R result = _combiner.value(_currentLeft, _right.next());
      if (!_right.hasNext()) {
        if (!_left.hasNext()) { _done = true; }
        else {
          _currentLeft = _left.next();
          _right = _rightIterable.iterator();
        }
      }
      return result;
    }
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, R>
    CartesianIterator<T1, T2, R> make(Iterator<? extends T1> left, Iterable<? extends T2> right,
                                      Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    return new CartesianIterator<T1, T2, R>(left, right, combiner);
  }
  
}
