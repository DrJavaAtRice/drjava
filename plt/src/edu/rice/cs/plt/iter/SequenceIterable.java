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
import edu.rice.cs.plt.lambda.Lambda;

/**
 * <p>An iterable representing an infinite sequence.  The sequence is defined by an initial
 * value and a successor function (described by a {@link Lambda}).</p>
 * 
 * <p>Note that the infinite nature of this list makes it impossible to use the standard {@code equals} and 
 * {@code hashCode} implementations (in {@link AbstractIterable}).  Care must
 * also be taken in invoking many iterable-handling methods that assume finite length, 
 * such as those in {@code IterUtil}.</p>
 */
public class SequenceIterable<T> implements SizedIterable<T>, Serializable {
  
  private final T _initial;
  private final Lambda<? super T, ? extends T> _successor;
  
  /**
   * @param initial  The first value in the sequence
   * @param successor  A function that, given the nth sequence value, produces the n+1st value
   */
  public SequenceIterable(T initial, Lambda<? super T, ? extends T> successor) {
    _initial = initial;
    _successor = successor;
  }
  
  /** Create a new {@link SequenceIterator} based on this iterable's parameters */
  public SequenceIterator<T> iterator() { return new SequenceIterator<T>(_initial, _successor); }
  
  public boolean isEmpty() { return false; }
  public int size() { return Integer.MAX_VALUE; }
  public int size(int bound) { return bound; }
  public boolean isInfinite() { return true; }
  public boolean hasFixedSize() { return true; }
  /** Always false: results of a lambda may be arbitrary. */
  public boolean isStatic() { return false; }

  /** Defers to {@link IterUtil#toString} */
  public String toString() { return IterUtil.toString(this); }
  
  /**
   * Returns {@code true} iff {@code o} is a SequenceIterable with the same initial value
   * and successor function (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (o == null || !getClass().equals(o.getClass())) { return false; }
    else {
      SequenceIterable<?> cast = (SequenceIterable<?>) o;
      return _initial.equals(cast._initial) && _successor.equals(cast._successor);
    }
  }
  
  public int hashCode() {
    return getClass().hashCode() ^ (_initial.hashCode() << 1) ^ (_successor.hashCode() << 2);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SequenceIterable<T> make(T initial, Lambda<? super T, ? extends T> successor) {
    return new SequenceIterable<T>(initial, successor);
  }

}
