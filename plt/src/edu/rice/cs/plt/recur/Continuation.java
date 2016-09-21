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

import edu.rice.cs.plt.lambda.ResolvingThunk;
import edu.rice.cs.plt.lambda.Lambda;

/**
 * <p>A thunk enabling iterative evaluation of a recursive function.  If a function with
 * a continuation return type can immediately produce the result value, it wraps the value
 * in a simple continuation; otherwise, it wraps a recursive computation in a continuation
 * that, when resolved, will produce the result.  To produce a value from a continuation, clients 
 * may either invoke {@link #value} or iteratively invoke {@link #step} until {@link #isResolved}
 * on the result is {@code true}.</p>
 * 
 * <p>Traditionally, continuations are functions that are threaded through all recursive
 * invocations of some function.  Such an approach allows continuation-based functions to 
 * be defined exclusively in terms of tail recursion, and to immediately return a result.  
 * Here, in contrast, we use continuations to solve <em>two</em> problems: first, as
 * traditionally, they allow us to represent calling contexts without relying on the stack;
 * and second, they delay the evaluation of recursion so that even tail recursion is prevented
 * from filling up the stack (since Java does not perform tail-call optimization).
 * Continuation-based methods thus must return, where {@code T} is the original return type,
 * a {@code Continuation<T>} rather than a {@code T}.  To prevent unnecessary clutter,
 * these methods are <em>not</em> required to accept a continuation as a parameter; instead,
 * they may assume that there is nothing more to be done with the result, and where this
 * is not the case, the continuation classes' implementation of {@code step()} will handle
 * the result appropriately.</p>
 * 
 * <p>As an example, here are two recursive functions translated to use {@code Continuation}s.
 * First, a tail-recursive method:<pre>
 * boolean isEven(int x) {
 *   if (x == 0) { return true; }
 *   if (x == 1) { return false; }
 *   else { return isEven(x - 2); }
 * }
 * </pre>
 * This can be written using continuations as follows:<pre>
 * Continuation&lt;Boolean&gt; isEven(int x) {
 *   if (x == 0) { return ValueContinuation.make(true); }
 *   if (x == 1) { return ValueContinuation.make(false); }
 *   else {
 *     return new PendingContinuation&lt;Boolean&gt;() {
 *       public Continuation&lt;? extends Boolean&gt; step() {
 *         return isEven(x - 2);
 *       }
 *     };
 *   }
 * }
 * </pre>
 * Second, a recursive method that requires on calling context:<pre>
 * long sum(int n) {
 *   if (n == 0) { return 0l; }
 *   else { return sum(n-1) + n; }
 * }
 * </pre>
 * This is written with continuations thus:<pre>
 * Continuation&lt;Long&gt; sum(final int n) {
 *   if (n == 0l) { return ValueContinuation.make(0l); }
 *   else {
 *     return new ArgContinuation&lt;Long, Long&gt;() {
 *       public Continuation&lt;Long&gt; arg() { return sum(n-1); }
 *       public Continuation&lt;Long&gt; apply(Long arg) {
 *         return ValueContinuation.make(arg + n);
 *       }
 *     };
 *   }
 * }
 * </pre>
 * In both cases, evaluation of the original function with large inputs will lead to a stack overflow,
 * while the stack size in the continuation-based versions is bounded by a small constant.</p>
 * 
 * <p>As an illustration, an invocation of {@code sum(3)}, as defined above, results in the following
 * evaluation (using informal abbreviations to represent the continuations and lambdas involved):<pre>
 * sum(3)
 * = Arg(sum(2), \x.Val(x+3))
 * [STEP]
 * Comp(Arg(sum(1), \x.Val(x+2)), \x.Val(x+3))
 * [STEP]
 * Comp(Arg(sum(0), \x.Val(x+1)), \x.Val(x+2)).compose(\x.Val(x+3))
 * = Comp(Arg(sum(0), \x.Val(x+1)), \y.(\x.Val(x+2))(y).compose(\x.Val(x+3)) )
 * [STEP]
 * Comp(Val(0), \x.Val(x+1)).compose( \y.(\x.Val(x+2))(y).compose(\x.Val(x+3)) )
 * = Comp(Val(0), \z.(\x.Val(x+1))(z).compose( \y.(\x.Val(x+2))(y).compose(\x.Val(x+3)) ) )
 * [STEP]
 * (\x.Val(x+1))(0).compose( \y.(\x.Val(x+2))(y).compose(\x.Val(x+3)) )
 * = Val(1).compose( \y.(\x.Val(x+2))(y).compose(\x.Val(x+3)) )
 * = Comp(Val(1), \y.(\x.Val(x+2))(y).compose(\x.Val(x+3)) )
 * [STEP]
 * \x.Val(x+2))(1).compose(\x.Val(x+3))
 * = Val(3).compose(\x.Val(x+3))
 * = Comp(Val(3), \x.Val(x+3))
 * [STEP]
 * Val(6)
 * </pre>
 * There are 6 steps: three to expand the recursion to the base case, and three to perform the
 * necessary computation after a recursive result has been determined.</p>
 */
public interface Continuation<T> extends ResolvingThunk<T> {
  
  /** Iteratively resolve the continuation to a value. */
  public T value();
  
  /** Return {@code true} iff the continuation has been resolved to a value. */
  public boolean isResolved();
  
  /**
   * Produce a continuation representing the next step of computation.
   * @throws IllegalStateException  If {@code isResolved()} is {@code true}
   */
  public Continuation<? extends T> step();
  
  /** Produce a continuation that will invoke {@code c} with this object's result. */
  public <R> Continuation<? extends R> compose(Lambda<? super T, ? extends Continuation<? extends R>> c);
}
