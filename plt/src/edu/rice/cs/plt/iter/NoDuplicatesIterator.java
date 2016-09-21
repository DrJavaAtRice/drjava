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

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.tuple.OptionUnwrapException;

/**
 * An Iterator that returns each value only once.  Values are compared via {@code equals()} and
 * hashed in order to detect duplicates.  A set of previously-seen values is maintained, which
 * requires space linear in the number of times {@code next()} has been invoked.
 */
public class NoDuplicatesIterator<T> extends ReadOnlyIterator<T> implements Composite {
  
  private final Iterator<? extends T> _i;
  private final Set<T> _seen;
  private Option<T> _lookahead;
  
  public NoDuplicatesIterator(Iterator<? extends T> i) {
    _i = i;
    _seen = new HashSet<T>();
    advanceLookahead();
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_i) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_i) + 1; }
  
  public boolean hasNext() { return _lookahead.isSome(); }
  
  public T next() {
    try {
      T result = _lookahead.unwrap();
      advanceLookahead();
      return result;
    }
    catch (OptionUnwrapException e) { throw new NoSuchElementException(); }
  }
  
  /**
   * Finds the next unique value in {@code _i}.  Ignores the previous value of
   * {@code _lookahead}.  If a value is found, sets {@code _lookahead} to that
   * value; otherwise, sets it to Option.none.
   */
  private void advanceLookahead() {
    // This class is similar to FilteredIterator, but can't be implemented
    // as an instance because we need to manipulate the "seen" set here, before 
    // a "next" value is returned.  Otherwise, the lookahead test fails to
    // consider the most recently-seen value.
    _lookahead = Option.none();
    while (_i.hasNext() && _lookahead.isNone()) {
      T next = _i.next();
      if (!_seen.contains(next)) {
        _lookahead = Option.some(next);
        _seen.add(next);
      }
    }
  }
  
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> NoDuplicatesIterator<T> make(Iterator<? extends T> i) {
    return new NoDuplicatesIterator<T>(i);
  }
  
}
