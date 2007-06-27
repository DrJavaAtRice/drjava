/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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

import edu.rice.cs.plt.lambda.Thunk;

/**
 * <p>A thunk enabling iterative evaluation of a recursive function.  If a function with
 * a continuation return type can immediately produce the result value, it wraps the value
 * in a simple continuation; otherwise, it wraps a recursive computation in a continuation
 * that, when resolved, will produce the result.</p>
 * 
 * <p>For example, this recursive function:<code>
 * boolean isEven(int x) {
 *   if (x == 0) { return true; }
 *   if (x == 1) { return false; }
 *   else { return isEven(x - 2); }
 * }
 * </code>
 * Could be written using continuations, as follows:<code>
 * Continuation<Boolean> isEven(int x) {
 *   if (x == 0) { return ValueContinuation.make(true); }
 *   if (x == 1) { return ValueContinuation.make(false); }
 *   else {
 *     return new TailContinuation<Boolean>() {
 *       public Continuation<? extends Boolean> step() { return isEven(x - 2); }
 *     };
 *   }
 * }
 * </code>
 * While evaluation of the original {@code isEven} function might lead to a stack overflow
 * on modestly large inputs, evaluation of the second will not.</p>
 * 
 * <p>To produce a value from a continuation, clients may either invoke {@link #value}
 * or iteratively invoke {@link #step} until {@link #isResolved} on the result is {@code true}.</p>
 * 
 * TODO: Implement continuations for non-tail recursion.
 */
public interface Continuation<T> extends Thunk<T> {
  
  /** Resolve the continuation to a value */
  public T value();
  
  /** @return  {@code true} iff the continuation has been resolved to a value */
  public boolean isResolved();
  
  /**
   * @return  A continuation representing the next step of compuation
   * @throws IllegalStateException  If {@code isResolved()} is {@code true}
   */
  public Continuation<? extends T> step();
}
