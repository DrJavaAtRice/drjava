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

/**
 * <p>Allows size calculations on {@code Iterable}s.  Implementing classes must be able to calculate their size; 
 * ideally, this calculation should be done in constant time.  {@link IterUtil#sizeOf} uses this interface to 
 * optimize size calculations.</p>
 * 
 * <p>In an ideal design, {@code java.util.Collection} would implement a {@code SizedIterable}
 * API class, and applications that required this behavior could be defined in terms of 
 * {@code SizedIterable}s instead of {@code Iterable}s.  However, since the Java APIs can't be 
 * modified and are too valuable to abandon, the {@code sizeOf} method provides a workaround that, 
 * through casting, calculates the size appropriately.  An alternative design would allow 
 * collections as components of {@code SizedIterable}s such as {@link ComposedIterable} only by manually
 * wrapping them in a bridge class (see {@link IterUtil#asSizedIterable}).</p>
 */
public interface SizedIterable<T> extends Iterable<T> {
  /** Whether the iterable does not contain any elements. */
  public boolean isEmpty();
  /**
   * Compute the number of elements in the iterable.  If the size is infinite or too large to be represented as an 
   * {@code int}, {@code Integer.MAX_VALUE} should be returned.  Otherwise, {@code next()} may be safely invoked 
   * on the iterator exactly this number of times.  
   */
  public int size();
  
  /**
   * Compute the number of elements in the iterable, up to the given bound.  If the size is infinite or greater
   * than {@code bound}, {@code bound} is returned.
   * @param bound  Maximum result.  Assumed to be nonnegative.
   */
  public int size(int bound);
  
  /**
   * {@code true} if the iterable is known to have infinite size.  If true, an iterator over the iterable in its 
   * current state will never return {@code false} from {@code hasNext()}.
   */
  public boolean isInfinite();
  
  /**
   * {@code true} if this iterable is known to have a fixed size.  This is the case if the iterable is immutable, 
   * or if changes can only replace values, not remove or add them.  An infinite iterable may be fixed if it
   * is guaranteed to never become finite.
   */
  public boolean hasFixedSize();
  
  /**
   * {@code true} if this iterable is unchanging.  This implies that {@code hasFixedSize()} is true, and that
   * {@code iterator()} will always return the same (either {@code ==} or {@code equal()} and immutable) elements
   * in the same order.  ("Immutable" here means that {@code equals()} invocations are consistent over time -- if 
   * two objects are equal, they will never become inequal, and vice versa.)
   */
  public boolean isStatic();
  
}
