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
import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * Collapses a list of lists into a single list.  Subsequent changes to the list or its sublists will be
 * reflected.
 */
public class CollapsedIterable<T> extends AbstractIterable<T> 
  implements SizedIterable<T>, OptimizedLastIterable<T>, Composite, Serializable {
  
  private final Iterable<? extends Iterable<? extends T>> _iters;
  
  public CollapsedIterable(Iterable<? extends Iterable<? extends T>> iters) { _iters = iters; }
    
  public int compositeHeight() { return ObjectUtil.compositeHeight((Object) _iters) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize((Object) _iters) + 1; }
  
  public CollapsedIterator<T> iterator() {
    Iterator<? extends Iterator<? extends T>> i =
      new MappedIterable<Iterable<? extends T>, Iterator<? extends T>>(_iters, GetIterator.<T>make()).iterator();
    return new CollapsedIterator<T>(i);
  }
  
  public boolean isEmpty() { return size(1) == 0; }
  
  public int size() {
    int result = 0;
    for (Iterable<?> iter : _iters) {
      result += IterUtil.sizeOf(iter);
      if (result < 0) { result = Integer.MAX_VALUE; break; } // overflow
    }
    return result;
  }
  
  public int size(int bound) {
    int result = 0;
    for (Iterable<?> iter : _iters) {
      result += IterUtil.sizeOf(iter);
      if (result >= bound) { break; }
      else if (result < 0) { result = Integer.MAX_VALUE; break; } // overflow
    }
    return result <= bound ? result : bound;
  }
  
  public boolean isInfinite() {
    if (IterUtil.isInfinite(_iters)) { return true; }
    for (Iterable<?> iter : _iters) {
      if (IterUtil.isInfinite(iter)) { return true; }
    }
    return false;
  }
  
  public boolean hasFixedSize() {
    if (!IterUtil.hasFixedSize(_iters)) { return false; }
    for (Iterable<?> iter : _iters) {
      if (!IterUtil.hasFixedSize(iter)) { return false; }
    }
    return true;
  }
  
  public boolean isStatic() {
    if (!IterUtil.isStatic(_iters)) { return false; }
    for (Iterable<?> iter : _iters) {
      if (!IterUtil.isStatic(iter)) { return false; }
    }
    return true;
  }
  
  /**
   * Determine the last value in the iterable.  This implementation will usually be faster than
   * the general approach of iterating through the entire list, because it only iterates through
   * the top-level list to find the last non-empty nested list.
   */
  public T last() {
    Iterable<? extends T> lastNonEmpty = null;
    for (Iterable<? extends T> iter : _iters) {
      if (lastNonEmpty == null || !IterUtil.isEmpty(iter)) { lastNonEmpty = iter; }
    }
    return IterUtil.last(lastNonEmpty);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> CollapsedIterable<T> make(Iterable<? extends Iterable<? extends T>> iters) {
    return new CollapsedIterable<T>(iters);
  }
  
  private static final class GetIterator<T>
    implements Lambda<Iterable<? extends T>, Iterator<? extends T>>, Serializable {
    public static final GetIterator<Object> INSTANCE = new GetIterator<Object>();
    @SuppressWarnings("unchecked") public static <T> GetIterator<T> make() { return (GetIterator<T>) INSTANCE; }
    private GetIterator() {}
    public Iterator<? extends T> value(Iterable<? extends T> iter) { return iter.iterator(); }
  }
  
}
