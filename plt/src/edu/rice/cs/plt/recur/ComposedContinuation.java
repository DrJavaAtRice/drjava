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
 * A continuation defined in terms of two other continuations, where the second is parameterized
 * by the result of the first.  This class is essential to correctly handling general recursion.
 * Its {@link #step} and {@link #compose} methods are defined so that iterative computation
 * does not require "drilling down" to an unbounded depth in order to locate and perform the
 * next evaluation step.
 */
public class ComposedContinuation<T, R> extends PendingContinuation<R> {
  
  private final Continuation<? extends T> _first;
  private final Lambda<? super T, ? extends Continuation<? extends R>> _rest;
  
  public ComposedContinuation(Continuation<? extends T> first,
                              Lambda<? super T, ? extends Continuation<? extends R>> rest) {
    _first = first;
    _rest = rest;
  }
  
  /**
   * If {@code first} is resolved, apply {@code rest} to compute the second.  Otherwise,
   * invoke {@code first.step()}, and then "push" {@code rest} into the result by invoking
   * {@code compose()}.
   */
  public Continuation<? extends R> step() {
    if (_first.isResolved()) { return _rest.value(_first.value()); }
    else { return _first.step().compose(_rest); }
  }
  
  /**
   * Create a new {@code ComposedContinuation} with the same {@code first}, but with a {@code rest}
   * that will compose {@code c} onto the result of this object's {@code rest} function.
   * (Note that the default behavior -- nesting this object in another {@code ComposedContinuation}
   * with {@code c} as its {@code rest} -- results in continuations whose {@code step()} methods
   * are invoked recursively to an unbounded depth.)
   */
  public <S> Continuation<S> compose(final Lambda<? super R, ? extends Continuation<? extends S>> c) {
    return new ComposedContinuation<T, S>(_first, new Lambda<T, Continuation<? extends S>>() {
      public Continuation<? extends S> value(T arg) {
        return _rest.value(arg).compose(c);
      }
    });
  }
  
}
