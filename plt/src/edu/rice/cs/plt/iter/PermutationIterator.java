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

/**
 * Enumerates all permutations of the given list.  Behavior is undefined if
 * the given list changes during iteration.  The size of the enumeration, where 
 * the original list has size n, is n! (and is thus guaranteed to be >= 1).  The order of the 
 * results is lexicographical, assuming each element of the original list is taken to 
 * lexicographically precede all of its successors.  (Thus, the original list is
 * the first to be returned, and a reversed list is the last.)    Of course, due to the 
 * factorial complexity of enumerating all permutations, this class is probably not suitable
 * for applications in which n is unbounded (or just intractably large).
 * 
 * @param <T>  The element type of the permuted lists; note that {@code next()} returns
 *             {@code Iterable<T>}s, not {@code T}s.
 */
public class PermutationIterator<T> extends ReadOnlyIterator<Iterable<T>> {
  
  private final Iterable<? extends T> _original;
  private final Iterator<? extends T> _elements;
  private T _element;
  private int _elementIndex;
  private Iterator<Iterable<T>> _restPermutations;
  
  public PermutationIterator(Iterable<? extends T> original) { 
    _original = original;
    _elements = _original.iterator();
    // _element is initialized later
    _elementIndex = -1;
    
    // to deal with the empty case, we set _restPermutations appropriately
    if (IterUtil.isEmpty(_original)) { 
      _restPermutations = SingletonIterator.<Iterable<T>>make(EmptyIterable.<T>make());
    }
    else { _restPermutations = EmptyIterator.make(); }
  }
  
  public boolean hasNext() { return _restPermutations.hasNext() || _elements.hasNext(); }
  
  public Iterable<T> next() {
    // in the empty case, _restPermutations contians a single empty list
    if (IterUtil.isEmpty(_original)) { return _restPermutations.next(); }
    else {
      if (!_restPermutations.hasNext()) {
        _element = _elements.next(); // exception occurs if !elements.hasNext()
        _elementIndex++;
        _restPermutations = new PermutationIterator<T>(makeRest(_elementIndex));
        // restPermutations must now haveNext()
      }
      return new ComposedIterable<T>(_element, _restPermutations.next());
    }
  }
  
  private Iterable<T> makeRest(int skipIndex) {
    Iterable<T> result = EmptyIterable.make();
    int i = 0;
    for (T e : _original) {
      if (i != skipIndex) { result = new ComposedIterable<T>(result, e); }
      i++;
    }
    return result;
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> PermutationIterator<T> make(Iterable<? extends T> original) {
    return new PermutationIterator<T>(original);
  }
}
