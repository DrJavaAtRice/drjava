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

package edu.rice.cs.plt.recur;

import edu.rice.cs.plt.lambda.Lambda;

/** 
 * A continuation representing computation left to be done (in contrast to {@link ValueContinuation},
 * which represents a completed computation).  This class provides default implementations of
 * {@link #value}, {@link #isResolved}, and {@link #compose}.  Subclasses may be defined in one of two
 * ways:<ul>
 * <li>Simple tail calls may be represented by simply defining an anonymous subclass of
 * {@code PendingContinuation}, where the {@code step()} method wraps the delayed recursive invocation.</li>
 * <li>More complex cases may be handled by defining continuation classes that extend
 * {@code PendingContinuation}.  {@link ComposedContinuation} is one example.</li>
 * </ul>
 */
public abstract class PendingContinuation<T> implements Continuation<T> {
  
  /** Iteratively invoke {@code step()} until a resolved continuation is produced. */
  public T value() {
    Continuation<? extends T> k = this;
    while (!k.isResolved()) { k = k.step(); }
    return k.value();
  }
  
  /** Return {@code false}. */
  public boolean isResolved() { return false; }
  
  /** Create a {@code ComposedContinuation} in terms of this object and the provided function. */
  public <R> Continuation<R> compose(Lambda<? super T, ? extends Continuation<? extends R>> c) {
    return new ComposedContinuation<T, R>(this, c);
  }
  
  /**
   * Defines the next step of the continuation.  For simple tail calls, this is generally a recursive 
   * invocation of the current method (or a related method).
   */
  public abstract Continuation<? extends T> step();
  
}
