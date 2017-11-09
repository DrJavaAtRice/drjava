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
 * A continuation for results that depend on a two recursive invocations, followed by some
 * additional computation.  Instances must implement {@link #arg1}, which wraps the first recursive
 * invocation, {@link #arg2}, which wraps the second, and {@link #apply}, which performs the remaining
 * computation.
 */
public abstract class BinaryArgContinuation<T1, T2, R> extends PendingContinuation<R> {
  
  /** Produce a continuation computing the first result of a recursive invocation. */
  protected abstract Continuation<? extends T1> arg1();
  
  /** Produce a continuation computing the second result of a recursive invocation. */
  protected abstract Continuation<? extends T2> arg2();
  
  /**
   * Given the results of evaluating {@code arg1()} and {@code arg2()}, produce a continuation for 
   * the ultimate result.
   */
  protected abstract Continuation<? extends R> apply(T1 arg1, T2 arg2);
  
  /**
   * Create a {@link ComposedContinuation} in terms of the result of {@code arg1()} and a lambda
   * that computes {@code arg2()} and ultimately invokes {@code apply()} with the results.
   */
  public Continuation<R> step() {
    return new ComposedContinuation<T1, R>(arg1(), new Lambda<T1, Continuation<? extends R>>() {
      public Continuation<? extends R> value(final T1 arg1) {
        return new ComposedContinuation<T2, R>(arg2(), new Lambda<T2, Continuation<? extends R>>() {
          public Continuation<? extends R> value(T2 arg2) { return apply(arg1, arg2); }
        });
      }
    });
  }
  
}
