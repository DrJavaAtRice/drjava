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
import edu.rice.cs.plt.tuple.Wrapper;
import edu.rice.cs.plt.tuple.IdentityWrapper;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * <p>A stack used to store the arguments of a recursive invocation in order to prevent
 * infinite recursion.  By checking that a given argument has not been used previously before
 * recurring, a client can prevent infinite recursion in some circumstances (such as
 * when traversing an infinite, immutable data structure).</p>
 * 
 * <p>While {@link RecursionStack} allows arbitrary application result values to be provided 
 * for the infinite case, this class follows a stricter discipline: the infinite case result
 * must be provided at the time of the <em>first</em> invocation of an argument; that value
 * will be stored, and a second invocation will return it.  In this way, the result of
 * a recursive computation is always precomputed -- that is, it must be determined before 
 * the computation takes place.  Classes like {@link edu.rice.cs.plt.lambda.DelayedThunk} can be 
 * used to create precomputed values, providing an initial "empty box" that can be "filled" when 
 * computation is complete.  This allows the definition, for example, of data structures that 
 * contain themselves.  Due to the restricted applicability of this class (in comparison to
 * {@code RecursionStack}), methods that involve invoking {@code Runnable}s or recurring multiple
 * times based on a threshold value are not defined here.</p>
 * 
 * <p>The client may either choose to explicity check for containment, {@link #push} the argument, 
 * recur, and then {@link #pop}, or invoke one of a variety of lambda-based methods that perform 
 * these bookkeeping tasks automatically.  In the latter case, when an exception occurs between
 * a {@code push} and a matching {@code pop}, the {@code pop} is guaranteed to execute before 
 * the exception propagates upward.  Thus, clients who do not directly invoke {@link #push}
 * and {@link #pop} may assume that the stack is always in a consistent state.</p>
 * 
 * @see RecursionStack
 * @see PrecomputedRecursionStack2
 * @see PrecomputedRecursionStack3
 * @see PrecomputedRecursionStack4
 */
public class PrecomputedRecursionStack<T, R> {
  
  private final Lambda<? super T, ? extends Wrapper<T>> _wrapperFactory;
  private final Map<Wrapper<T>, Lambda<? super T, ? extends R>> _previous;
  private final LinkedList<Wrapper<T>> _stack;
  
  /** Create an empty recursion stack with an {@link IdentityWrapper} factory */
  public PrecomputedRecursionStack() { this(IdentityWrapper.<T>factory()); }
  
  /**
   * Create an empty recursion stack with the given {@code Wrapper} factory
   * @param wrapperFactory  A lambda used to produce a wrapper for values placed on the
   *                        stack.  This provides clients with control over the method used
   *                        to determine if a value has been seen previously.
   */
  public PrecomputedRecursionStack(Lambda<? super T, ? extends Wrapper<T>> wrapperFactory) {
    _wrapperFactory = wrapperFactory;
    _previous = new HashMap<Wrapper<T>, Lambda<? super T, ? extends R>>();
    _stack = new LinkedList<Wrapper<T>>();
  }
  
  /** 
   * @return  {@code true} iff a value identical (according to {@code ==}) to {@code arg}
   *          is currently on the stack
   */
  public boolean contains(T arg) { return _previous.containsKey(_wrapperFactory.value(arg)); }
  
  /** 
   * @return  The infinite-case result provided for {@code arg}
   * @throws  IllegalStateException  If {@code arg} is not on the stack
   */
  public R get(T arg) {
    Lambda<? super T, ? extends R> result = _previous.get(_wrapperFactory.value(arg));
    if (result == null) { throw new IllegalArgumentException("Value is not on the stack"); }
    return result.value(arg);
  }
  
  /**
   * Add {@code arg} to the top of the stack with the given infinite-case result.
   * @throws IllegalArgumentException  If {@code arg} is already on the stack
   */
  public void push(T arg, R value) { push(arg, (Lambda<Object, R>) LambdaUtil.valueLambda(value)); }
  
  /**
   * Add {@code arg} to the top of the stack with the given thunk producing its infinite-case result.
   * @throws IllegalArgumentException  If {@code arg} is already on the stack
   */
  public void push(T arg, Thunk<? extends R> value) {
    push(arg, (Lambda<Object, ? extends R>) LambdaUtil.promote(value));
  }
  
  /**
   * Add {@code arg} to the top of the stack with the given lambda producing its infinite-case result.
   * @throws IllegalArgumentException  If {@code arg} is already on the stack
   */
  public void push(T arg, Lambda<? super T, ? extends R> value) {
    Wrapper<T> wrapped = _wrapperFactory.value(arg);
    if (_previous.containsKey(wrapped)) {
      throw new IllegalArgumentException("arg is already on the stack");
    }
    _stack.addLast(wrapped);
    _previous.put(wrapped, value);
  }
  
  /** 
   * Remove {@code arg} from the top of the stack
   * @throws IllegalArgumentException  If {@code arg} is not at the top of the stack
   */
  public void pop(T arg) {
    Wrapper<T> wrapped = _wrapperFactory.value(arg);
    if (_stack.isEmpty() || !_stack.getLast().equals(wrapped)) {
      throw new IllegalArgumentException("arg is not on top of the stack");
    }
    _stack.removeLast();
    _previous.remove(wrapped);
  }
  
  /** @return  The current size (depth) of the stack */
  public int size() { return _stack.size(); }
  
  /** @return  {@code true} iff the stack is currently empty */
  public boolean isEmpty() { return _stack.isEmpty(); }
  
  /**
   * Evaluate the given thunk, unless {@code arg} is already on the stack; push {@code arg}
   * onto the stack with the given precomputed result during {@code thunk}'s evaluation
   * 
   * @return  The value of {@code thunk}, or a previously-provided precomputed value
   */
  public R apply(Thunk<? extends R> thunk, R precomputed, T arg) {
    if (!contains(arg)) { 
      push(arg, precomputed);
      try { return thunk.value(); }
      finally { pop(arg); }
    }
    else { return get(arg); }
  }
  
  /**
   * Evaluate the given thunk, unless {@code arg} is already on the stack; push {@code arg}
   * onto the stack with the given precomputed result during {@code thunk}'s evaluation
   * 
   * @return  The value of {@code thunk}, or a previously-provided precomputed value
   */
  public R apply(Thunk<? extends R> thunk, Thunk<? extends R> precomputed, T arg) {
    if (!contains(arg)) { 
      push(arg, precomputed);
      try { return thunk.value(); }
      finally { pop(arg); }
    }
    else { return get(arg); }
  }
  
  /**
   * Evaluate the given thunk, unless {@code arg} is already on the stack; push {@code arg}
   * onto the stack with the given precomputed result during {@code thunk}'s evaluation
   * 
   * @return  The value of {@code thunk}, or a previously-provided precomputed value
   */
  public R apply(Thunk<? extends R> thunk, Lambda<? super T, ? extends R> precomputed, T arg) {
    if (!contains(arg)) { 
      push(arg, precomputed);
      try { return thunk.value(); }
      finally { pop(arg); }
    }
    else { return get(arg); }
  }
  
  /**
   * Evaluate the given lambda with argument {@code arg}, unless {@code arg} is already on the 
   * stack; push {@code arg} onto the stack with the given precomputed result during 
   * {@code lambda}'s evaluation
   * 
   * @return  The value of {@code lambda}, or a previously-provided precomputed value
   */
  public R apply(Lambda<? super T, ? extends R> lambda, R precomputed, T arg) {
    if (!contains(arg)) { 
      push(arg, precomputed);
      try { return lambda.value(arg); }
      finally { pop(arg); }
    }
    else { return get(arg); }
  }
  
  /**
   * Evaluate the given lambda with argument {@code arg}, unless {@code arg} is already on the 
   * stack; push {@code arg} onto the stack with the given precomputed result during 
   * {@code lambda}'s evaluation
   * 
   * @return  The value of {@code lambda}, or a previously-provided precomputed value
   */
  public R apply(Lambda<? super T, ? extends R> lambda, Thunk<? extends R> precomputed, T arg) {
    if (!contains(arg)) { 
      push(arg, precomputed);
      try { return lambda.value(arg); }
      finally { pop(arg); }
    }
    else { return get(arg); }
  }
  
  /**
   * Evaluate the given lambda with argument {@code arg}, unless {@code arg} is already on the 
   * stack; push {@code arg} onto the stack with the given precomputed result during 
   * {@code lambda}'s evaluation
   * 
   * @return  The value of {@code lambda}, or a previously-provided precomputed value
   */
  public R apply(Lambda<? super T, ? extends R> lambda, Lambda<? super T, ? extends R> precomputed, 
                     T arg) {
    if (!contains(arg)) { 
      push(arg, precomputed);
      try { return lambda.value(arg); }
      finally { pop(arg); }
    }
    else { return get(arg); }
  }
    
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T, R> PrecomputedRecursionStack<T, R> make() {
    return new PrecomputedRecursionStack<T, R>();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T, R> PrecomputedRecursionStack<T, R> 
    make(Lambda<? super T, ? extends Wrapper<T>> wrapperFactory) {
    return new PrecomputedRecursionStack<T, R>(wrapperFactory);
  }
  
}
