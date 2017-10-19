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

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import edu.rice.cs.plt.tuple.Quad;
import edu.rice.cs.plt.tuple.IdentityQuad;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda4;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * <p>A stack used to store the arguments of a recursive invocation in order to prevent
 * infinite recursion.  By checking that the given arguments have not been used previously 
 * before recurring, a client can prevent infinite recursion in some circumstances (such as
 * when traversing an infinite, immutable data structure).</p>
 * 
 * <p>While {@link RecursionStack4} allows arbitrary application result values to be provided 
 * for the infinite case, this class follows a stricter discipline: the infinite case result
 * must be provided at the time of the <em>first</em> invocation of the arguments; that value
 * will be stored, and a second invocation will return it.  In this way, the result of
 * a recursive computation is always precomputed -- that is, it must be determined before 
 * the computation takes place.  Classes like {@link edu.rice.cs.plt.lambda.DelayedThunk} can be 
 * used to create precomputed values, providing an initial "empty box" that can be "filled" when 
 * computation is complete.  This allows the definition, for example, of data structures that 
 * contain themselves.  Due to the restricted applicability of this class (in comparison to
 * {@code RecursionStack4}), methods that involve invoking {@code Runnable}s or recurring multiple
 * times based on a threshold value are not defined here.</p>
 * 
 * <p>The client may either choose to explicity check for containment, {@link #push} the argument, 
 * recur, and then {@link #pop}, or invoke one of a variety of lambda-based methods that perform 
 * these bookkeeping tasks automatically.  In the latter case, when an exception occurs between
 * a {@code push} and a matching {@code pop}, the {@code pop} is guaranteed to execute before 
 * the exception propagates upward.  Thus, clients who do not directly invoke {@link #push}
 * and {@link #pop} may assume that the stack is always in a consistent state.</p>
 * 
 * @see RecursionStack4
 * @see PrecomputedRecursionStack
 * @see PrecomputedRecursionStack2
 * @see PrecomputedRecursionStack3
 */
public class PrecomputedRecursionStack4<T1, T2, T3, T4, R> {
  
  private final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, 
                        ? extends Quad<T1, T2, T3, T4>> _quadFactory;
  private final Map<Quad<T1, T2, T3, T4>, 
                    Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>> _previous;
  private final LinkedList<Quad<T1, T2, T3, T4>> _stack;
  
  /** Create an empty recursion stack with an {@link IdentityQuad} factory */
  public PrecomputedRecursionStack4() { this(IdentityQuad.<T1, T2, T3, T4>factory()); }
  
  /**
   * Create an empty recursion stack with the given {@code Quad} factory
   * @param quadFactory  A lambda used to produce a quad for values placed on the
   *                     stack.  This provides clients with control over the method used
   *                     to determine if a value has been seen previously.
   */
  public PrecomputedRecursionStack4(Lambda4<? super T1, ? super T2, ? super T3, ? super T4,
                                            ? extends Quad<T1, T2, T3, T4>> quadFactory) {
    _quadFactory = quadFactory;
    _previous = new HashMap<Quad<T1, T2, T3, T4>, 
                            Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>>();
    _stack = new LinkedList<Quad<T1, T2, T3, T4>>();
  }
  
  /** 
   * @return  {@code true} iff values identical (according to {@code ==}) to the given arguments
   *          are currently on the stack
   */
  public boolean contains(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    return _previous.containsKey(_quadFactory.value(arg1, arg2, arg3, arg4));
  }
  
  /** 
   * @return  The infinite-case result provided for the given arguments
   * @throws  IllegalStateException  If the arguments are not on the stack
   */
  public R get(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> result = 
      _previous.get(_quadFactory.value(arg1, arg2, arg3, arg4));
    if (result == null) { throw new IllegalArgumentException("Values are not on the stack"); }
    return result.value(arg1, arg2, arg3, arg4);
  }
  
  /**
   * Add the given arguments to the top of the stack with the given infinite-case result.
   * @throws IllegalArgumentException  If the arguments are already on the stack
   */
  public void push(T1 arg1, T2 arg2, T3 arg3, T4 arg4, R value) {
    push(arg1, arg2, arg3, arg4, (Lambda4<Object, Object, Object, Object, R>) LambdaUtil.valueLambda(value));
  }
  
  /**
   * Add the given arguments to the top of the stack with the given thunk producing their 
   * infinite-case result.
   * @throws IllegalArgumentException  If the arguments are already on the stack
   */
  public void push(T1 arg1, T2 arg2, T3 arg3, T4 arg4, Thunk<? extends R> value) {
    push(arg1, arg2, arg3, arg4, (Lambda4<Object, Object, Object, Object, ? extends R>) LambdaUtil.promote(value));
  }
  
  /**
   * Add the given arguments to the top of the stack with the given lambda producing their 
   * infinite-case result.
   * @throws IllegalArgumentException  If the arguments are already on the stack
   */
  public void push(T1 arg1, T2 arg2, T3 arg3, T4 arg4, 
                   Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> value) {
    Quad<T1, T2, T3, T4> wrapped = _quadFactory.value(arg1, arg2, arg3, arg4);
    if (_previous.containsKey(wrapped)) {
      throw new IllegalArgumentException("The given arguments are already on the stack");
    }
    _stack.addLast(wrapped);
    _previous.put(wrapped, value);
  }
  
  /** 
   * Remove the given arguments from the top of the stack
   * @throws IllegalArgumentException  If the arguments are not at the top of the stack
   */
  public void pop(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    Quad<T1, T2, T3, T4> wrapped = _quadFactory.value(arg1, arg2, arg3, arg4);
    if (_stack.isEmpty() || !_stack.getLast().equals(wrapped)) {
      throw new IllegalArgumentException("the given arguments are not on top of the stack");
    }
    _stack.removeLast();
    _previous.remove(wrapped);
  }
  
  /** @return  The current size (depth) of the stack */
  public int size() { return _stack.size(); }
  
  /** @return  {@code true} iff the stack is currently empty */
  public boolean isEmpty() { return _stack.isEmpty(); }
  
  /**
   * Evaluate the given thunk, unless the given arguments are already on the stack; push the
   * arguments onto the stack with the given precomputed result during {@code thunk}'s evaluation
   * 
   * @return  The value of {@code thunk}, or a previously-provided precomputed value
   */
  public R apply(Thunk<? extends R> thunk, R precomputed, T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4, precomputed);
      try { return thunk.value(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return get(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Evaluate the given thunk, unless the given arguments are already on the stack; push the
   * arguments onto the stack with the given precomputed result during {@code thunk}'s evaluation
   * 
   * @return  The value of {@code thunk}, or a previously-provided precomputed value
   */
  public R apply(Thunk<? extends R> thunk, Thunk<? extends R> precomputed, 
                 T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4, precomputed);
      try { return thunk.value(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return get(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Evaluate the given thunk, unless the given arguments are already on the stack; push the
   * arguments onto the stack with the given precomputed result during {@code thunk}'s evaluation
   * 
   * @return  The value of {@code thunk}, or a previously-provided precomputed value
   */
  public R apply(Thunk<? extends R> thunk, 
                 Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> precomputed, 
                 T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4, precomputed);
      try { return thunk.value(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return get(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Evaluate the given lambda with the given arguments, unless the arguments are already on the 
   * stack; push the arguments onto the stack with the given precomputed result during 
   * {@code lambda}'s evaluation
   * 
   * @return  The value of {@code lambda}, or a previously-provided precomputed value
   */
  public R apply(Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda, R precomputed,
                 T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4, precomputed);
      try { return lambda.value(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return get(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Evaluate the given lambda with the given arguments, unless the arguments are already on the 
   * stack; push the arguments onto the stack with the given precomputed result during 
   * {@code lambda}'s evaluation
   * 
   * @return  The value of {@code lambda}, or a previously-provided precomputed value
   */
  public R apply(Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda, 
                 Thunk<? extends R> precomputed, 
                 T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4, precomputed);
      try { return lambda.value(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return get(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Evaluate the given lambda with the given arguments, unless the arguments are already on the 
   * stack; push the arguments onto the stack with the given precomputed result during 
   * {@code lambda}'s evaluation
   * 
   * @return  The value of {@code lambda}, or a previously-provided precomputed value
   */
  public R apply(Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda, 
                 Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> precomputed, 
                 T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4, precomputed);
      try { return lambda.value(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return get(arg1, arg2, arg3, arg4); }
  }
    
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, R> PrecomputedRecursionStack4<T1, T2, T3, T4, R> make() {
    return new PrecomputedRecursionStack4<T1, T2, T3, T4, R>();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, R> PrecomputedRecursionStack4<T1, T2, T3, T4, R> 
    make(Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends Quad<T1, T2, T3, T4>> quadFactory) {
    return new PrecomputedRecursionStack4<T1, T2, T3, T4, R>(quadFactory);
  }
  
}
