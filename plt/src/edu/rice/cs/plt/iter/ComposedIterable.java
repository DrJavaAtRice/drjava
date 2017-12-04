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
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * Defines an iterable by composing two other iterables (or a value with an iterable).
 * Subsequent changes to the input lists will be reflected.
 */
public class ComposedIterable<T> extends AbstractIterable<T> 
  implements SizedIterable<T>, OptimizedLastIterable<T>, Composite, Serializable {
  
  private final Iterable<? extends T> _i1;
  private final int _i1Size; // negative implies dynamic size
  private final Iterable<? extends T> _i2;
  private final int _i2Size; // negative implies dynamic size
  private final boolean _isStatic;
  
  /** The result contains {@code i1}'s elements followed by {@code i2}'s elements. */
  public ComposedIterable(Iterable<? extends T> i1, Iterable<? extends T> i2) {
    _i1 = i1;
    _i2 = i2;
    if (IterUtil.hasFixedSize(_i1)) { _i1Size = IterUtil.sizeOf(_i1); }
    else { _i1Size = -1; }
    if (IterUtil.hasFixedSize(_i2)) { _i2Size = IterUtil.sizeOf(_i2); }
    else { _i2Size = -1; }
    _isStatic = IterUtil.isStatic(_i1) && IterUtil.isStatic(_i2);
  }
    
  /** The result contains {@code v1} followed by {@code i2}'s elements */
  public ComposedIterable(T v1, Iterable<? extends T> i2) {
    this(new SingletonIterable<T>(v1), i2);
  }
  
  /** The result contains {@code i1}'s elements followed by {@code v2} */
  public ComposedIterable(Iterable<? extends T> i1, T v2) {
    this(i1, new SingletonIterable<T>(v2));
  }
  
  public ComposedIterator<T> iterator() { 
    return new ComposedIterator<T>(_i1.iterator(), _i2.iterator());
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_i1, _i2) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_i1, _i2) + 1; }
  
  public boolean isEmpty() {
    return (_i1Size < 0 ? IterUtil.isEmpty(_i1) : _i1Size == 0) &&
           (_i2Size < 0 ? IterUtil.isEmpty(_i2) : _i2Size == 0);
  }
  
  public int size() {
    int result = (_i1Size < 0 ? IterUtil.sizeOf(_i1) : _i1Size) +
                 (_i2Size < 0 ? IterUtil.sizeOf(_i2) : _i2Size);
    if (result < 0) { result = Integer.MAX_VALUE; } // overflow
    return result;
  }
  
  public int size(int bound) {
    int size1 = (_i1Size < 0) ? IterUtil.sizeOf(_i1, bound) :
                                (bound < _i1Size) ? bound : _i1Size;
    int bound2 = bound-size1;
    int size2 = (_i2Size < 0) ? IterUtil.sizeOf(_i2, bound2) :
                                (bound2 < _i2Size) ? bound2 : _i2Size;
    return size1+size2;
  }
  
  public boolean isInfinite() { return IterUtil.isInfinite(_i1) || IterUtil.isInfinite(_i2); }
  
  public boolean hasFixedSize() { return _i1Size >= 0 && _i2Size >= 0; }
  
  public boolean isStatic() { return _isStatic; }
  
  /**
   * Determine the last value in the iterable.  This implementation will usually be faster than
   * the general approach of iterating through the entire list -- for a balanced
   * {@code ComposedIterable} tree, it takes log(n) time; if the right subtree is a singleton, 
   * the result is computed trivially.  (Note that the approach used avoids recursion in order 
   * to prevent a stack overflow.)
   */
  public T last() {
    Iterable<? extends T> lastIterable;
    if (IterUtil.isEmpty(_i2)) { lastIterable = _i1; }
    else { lastIterable = _i2; }
    
    while (lastIterable instanceof ComposedIterable<?>) {
      // javac 6 doesn't like this -- ComposedIterable<? extends T> </: Iterable<capture extends T>
      @SuppressWarnings("unchecked")
      ComposedIterable<? extends T> cast = (ComposedIterable<? extends T>) lastIterable;
      if (IterUtil.isEmpty(cast._i2)) { lastIterable = cast._i1; }
      else { lastIterable = cast._i2; }
    }
    
    return IterUtil.last(lastIterable);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ComposedIterable<T> make(Iterable<? extends T> i1, Iterable<? extends T> i2) {
    return new ComposedIterable<T>(i1, i2);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ComposedIterable<T> make(T v1, Iterable<? extends T> i2) {
    return new ComposedIterable<T>(v1, i2);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ComposedIterable<T> make(Iterable<? extends T> i1, T v2) {
    return new ComposedIterable<T>(i1, v2);
  }
  
}
