/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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
import edu.rice.cs.plt.lambda.Predicate;

/**
 * An Iterator that only returns the values in another Iterator ({@code i}) for which some
 * predicate ({@code p}) holds.  Does not support {@link #remove()}.
 */
public class FilteredIterator<T> extends ReadOnlyIterator<T> {
  
  private final Predicate<? super T> _p;
  private final Iterator<? extends T> _i;
  private T _lookahead;
  
  public FilteredIterator(Iterator<? extends T> i, Predicate<? super T> p) {
    _p = p;
    _i = i;
    advanceLookahead();
  }
  
  public boolean hasNext() { return _lookahead != null; }
  
  public T next() {
    T result = _lookahead;
    advanceLookahead();
    return result;
  }
  
  /**
   * Finds the next value in {@code _i} for which {@code _p} holds.
   * Ignores the previous value of {@code _lookahead}.  If a value is
   * found, sets {@code _lookahead} to that value; otherwise, sets it to 
   * {@code null}.
   */
  private void advanceLookahead() {
    _lookahead = null;
    while (_i.hasNext() && _lookahead == null) {
      T next = _i.next();
      if (_p.contains(next)) { _lookahead = next; }
    }
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> FilteredIterator<T> make(Iterator<? extends T> i, Predicate<? super T> p) {
    return new FilteredIterator<T>(i, p);
  }
  
}
