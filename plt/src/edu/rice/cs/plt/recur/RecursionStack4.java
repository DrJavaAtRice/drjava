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
import edu.rice.cs.plt.collect.Multiset;
import edu.rice.cs.plt.collect.HashMultiset;
import edu.rice.cs.plt.tuple.Quad;
import edu.rice.cs.plt.tuple.IdentityQuad;
import edu.rice.cs.plt.lambda.Runnable4;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda4;

/**
 * <p>A stack used to store the arguments of a recursive invocation in order to prevent
 * infinite recursion.  By checking that the given arguments have not been used previously 
 * before recurring, a client can prevent infinite recursion in some circumstances (such as
 * when traversing an infinite, immutable data structure).</p>
 * 
 * <p>The client may either choose to explicity check for containment, {@link #push} the 
 * arguments, recur, and then {@link #pop}, or invoke one of a variety of lambda-based 
 * methods that perform these bookkeeping tasks automatically.  In the latter case, when an 
 * exception occurs between a {@code push} and a matching {@code pop}, the {@code pop} is 
 * guaranteed to execute before the exception propagates upward.  Thus, clients who do not 
 * directly invoke {@link #push} and {@link #pop} may assume that the stack is always in a 
 * consistent state.</p>
 * 
 * @see PrecomputedRecursionStack4
 * @see RecursionStack
 * @see RecursionStack2
 * @see RecursionStack3
 */
public class RecursionStack4<T1, T2, T3, T4> {
  
  private final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, 
                        ? extends Quad<T1, T2, T3, T4>> _quadFactory;
  private final Multiset<Quad<T1, T2, T3, T4>> _previous;
  private final LinkedList<Quad<T1, T2, T3, T4>> _stack;
  
  /** Create an empty recursion stack with an {@link IdentityQuad} factory */
  public RecursionStack4() { this(IdentityQuad.<T1, T2, T3, T4>factory()); }
  
  /**
   * Create an empty recursion stack with the given {@code Quad} factory
   * @param quadFactory  A lambda used to produce a quad for values placed on the
   *                     stack.  This provides clients with control over the method used
   *                     to determine if a value has been seen previously.
   */
  public RecursionStack4(Lambda4<? super T1, ? super T2, ? super T3, ? super T4,
                                 ? extends Quad<T1, T2, T3, T4>> quadFactory) {
    _quadFactory = quadFactory;
    _previous = new HashMultiset<Quad<T1, T2, T3, T4>>();
    _stack = new LinkedList<Quad<T1, T2, T3, T4>>();
  }
  
  /** 
   * @return  {@code true} iff a set of values identical (according to {@code ==}) to the 
   *          given arguments is currently on the stack
   */
  public boolean contains(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    return _previous.contains(_quadFactory.value(arg1, arg2, arg3, arg4));
  }
  
  /** 
   * @return  {@code true} iff at least {@code threshold} sets of values identical 
   *          (according to {@code ==}) to the given arguments are currently on the stack
   */
  public boolean contains(T1 arg1, T2 arg2, T3 arg3, T4 arg4, int threshold) {
    return _previous.count(_quadFactory.value(arg1, arg2, arg3, arg4)) >= threshold;
  }
  
  /** Add the given arguments to the top of the stack */
  public void push(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    Quad<T1, T2, T3, T4> wrapped = _quadFactory.value(arg1, arg2, arg3, arg4);
    _stack.addLast(wrapped);
    _previous.add(wrapped);
  }
  
  /** 
   * Remove the given arguments from the top of the stack
   * @throws IllegalArgumentException  If the given arguments are not at the top of the stack
   */
  public void pop(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    Quad<T1, T2, T3, T4> wrapped = _quadFactory.value(arg1, arg2, arg3, arg4);
    if (_stack.isEmpty() || !_stack.getLast().equals(wrapped)) {
      throw new IllegalArgumentException("given args are not on top of the stack");
    }
    _stack.removeLast();
    _previous.remove(wrapped);
  }
  
  /** @return  The current size (depth) of the stack */
  public int size() { return _stack.size(); }
  
  /** @return  {@code true} iff the stack is currently empty */
  public boolean isEmpty() { return _stack.isEmpty(); }
  
  /**
   * Run the given runnable, unless the given arguments are already on the stack; push the 
   * arguments onto the stack during runnable execution
   */
  public void run(Runnable r, T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4);
      try { r.run(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * Run the given runnable, unless {@code threshold} instances of the given arguments are 
   * already on the stack; push the arguments onto the stack during runnable execution
   */
  public void run(Runnable r, T1 arg1, T2 arg2, T3 arg3, T4 arg4, int threshold) {
    if (!contains(arg1, arg2, arg3, arg4, threshold)) { 
      push(arg1, arg2, arg3, arg4);
      try { r.run(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * If the given arguments are not on the stack, run {@code r}; otherwise, run 
   * {@code infiniteCase}.  In either case, push the arguments onto the stack during 
   * runnable execution.
   */
  public void run(Runnable r, Runnable infiniteCase, T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    Runnable toRun = (contains(arg1, arg2, arg3, arg4) ? infiniteCase : r);
    push(arg1, arg2, arg3, arg4);
    try { toRun.run(); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * If less than {@code threshold} instances of the given arguments are on the stack, run 
   * {@code r}; otherwise, run {@code infiniteCase}.  In either case, push the 
   * arguments onto the stack during runnable execution.
   */
  public void run(Runnable r, Runnable infiniteCase, T1 arg1, T2 arg2, T3 arg3, T4 arg4, int threshold) {
    Runnable toRun = (contains(arg1, arg2, arg3, arg4, threshold) ? infiniteCase : r);
    push(arg1, arg2, arg3, arg4);
    try { toRun.run(); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Run the given runnable with the given arguments, unless the arguments are already on the 
   * stack; push the arguments onto the stack during runnable execution
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Runnable4<? super V1, ? super V2, ? super V3, ? super V4> r, V1 arg1, V2 arg2, 
             V3 arg3, V4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4);
      try { r.run(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * Run the given runnable with the given arguments, unless {@code threshold} instances 
   * of the arguments are already on the stack; push the arguments onto the stack during 
   * runnable execution
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Runnable4<? super V1, ? super V2, ? super V3, ? super V4> r, V1 arg1, V2 arg2, 
             V3 arg3, V4 arg4, int threshold) {
    if (!contains(arg1, arg2, arg3, arg4, threshold)) { 
      push(arg1, arg2, arg3, arg4);
      try { r.run(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * If the given arguments are not on the stack, run {@code r} with argument the arguments; 
   * otherwise, run {@code infiniteCase}.  In either case, push the arguments onto the 
   * stack during runnable execution.
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Runnable4<? super V1, ? super V2, ? super V3, ? super V4> r, 
             Runnable4<? super V1, ? super V2, ? super V3, ? super V4> infiniteCase, 
             V1 arg1, V2 arg2, V3 arg3, V4 arg4) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Runnable4<? super V1, ? super V2, ? super V3, ? super V4> toRun = 
      (Runnable4<? super V1, ? super V2, ? super V3, ? super V4>)
        (contains(arg1, arg2, arg3, arg4) ? infiniteCase : r);
    push(arg1, arg2, arg3, arg4);
    try { toRun.run(arg1, arg2, arg3, arg4); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * If less than {@code threshold} instances of the given arguments are on the stack, 
   * run {@code r} with the arguments; otherwise, run {@code infiniteCase}.  In either case, 
   * push the arguments onto the stack during runnable execution.
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Runnable4<? super V1, ? super V2, ? super V3, ? super V4> r, 
             Runnable4<? super V1, ? super V2, ? super V3, ? super V4> infiniteCase, 
             V1 arg1, V2 arg2, V3 arg3, V4 arg4, int threshold) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Runnable4<? super V1, ? super V2, ? super V3, ? super V4> toRun = 
      (Runnable4<? super V1, ? super V2, ? super V3, ? super V4>)
        (contains(arg1, arg2, arg3, arg4, threshold) ? infiniteCase : r);
    push(arg1, arg2, arg3, arg4);
    try { toRun.run(arg1, arg2, arg3, arg4); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Evaluate the given thunk, unless the given arguments are already on the stack; push 
   * the arguments onto the stack during thunk evaluation
   * 
   * @return  The value of {@code thunk}, or {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, R infiniteCase, T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4);
      try { return thunk.value(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * Evaluate the given thunk, unless {@code threshold} instances of the given arguments are 
   * already on the stack; push the arguments onto the stack during thunk evaluation
   * 
   * @return  The value of {@code thunk}, or {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, R infiniteCase, T1 arg1, T2 arg2, T3 arg3, T4 arg4, 
                     int threshold) {
    if (!contains(arg1, arg2, arg3, arg4, threshold)) { 
      push(arg1, arg2, arg3, arg4);
      try { return thunk.value(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * If the given arguments are not on the stack, evaluate {@code thunk}; otherwise, evaluate 
   * {@code infiniteCase}.  In either case, push the arguments onto the stack during 
   * thunk evaluation.
   * 
   * @return  The value of {@code thunk}, or the value of {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, Thunk<? extends R> infiniteCase, T1 arg1, 
                     T2 arg2, T3 arg3, T4 arg4) {
    Thunk<? extends R> toApply = (contains(arg1, arg2, arg3, arg4) ? infiniteCase : thunk);
    push(arg1, arg2, arg3, arg4);
    try { return toApply.value(); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * If less than {@code threshold} instances of the given arguments are on the stack, 
   * evaluate {@code thunk}; otherwise, evaluate {@code infiniteCase}.  In either case, push 
   * the arguments onto the stack during thunk evaluation.
   * 
   * @return  The value of {@code thunk}, or the value of {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, Thunk<? extends R> infiniteCase, T1 arg1, 
                     T2 arg2, T3 arg3, T4 arg4, int threshold) {
    Thunk<? extends R> toApply = (contains(arg1, arg2, arg3, arg4, threshold) ? infiniteCase : thunk);
    push(arg1, arg2, arg3, arg4);
    try { return toApply.value(); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Evaluate the given lambda with the given arguments, unless the arguments are already on 
   * the stack; push the arguments onto the stack during lambda evaluation
   * 
   * @return  The value of {@code lambda}, or {@code infiniteCase}
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4, R> 
    R apply(Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> lambda, 
            R infiniteCase, V1 arg1, V2 arg2, V3 arg3, V4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4);
      try { return lambda.value(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * Evaluate the given lambda with the given arguments, unless {@code threshold} instances 
   * of the arguments are already on the stack; push the arguments onto the stack 
   * during lambda evaluation
   * 
   * @return  The value of {@code lambda}, or {@code infiniteCase}
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4, R> 
    R apply(Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> lambda, 
            R infiniteCase, V1 arg1, V2 arg2, V3 arg3, V4 arg4, int threshold) {
    if (!contains(arg1, arg2, arg3, arg4, threshold)) { 
      push(arg1, arg2, arg3, arg4);
      try { return lambda.value(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * If the given arguments are not on the stack, evaluate {@code lambda} with the arguments; 
   * otherwise, evaluate {@code infiniteCase}.  In either case, push the arguments onto the
   * stack during lambda evaluation.
   * 
   * @return  The value of {@code lambda}, or the value of {@code infiniteCase}
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4, R> 
    R apply(Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> lambda, 
            Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> infiniteCase, 
            V1 arg1, V2 arg2, V3 arg3, V4 arg4) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> toApply = 
      (Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R>) 
        (contains(arg1, arg2, arg3, arg4) ? infiniteCase : lambda);
    push(arg1, arg2, arg3, arg4);
    try { return toApply.value(arg1, arg2, arg3, arg4); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * If less than {@code threshold} instances of the given arguments are on the stack, 
   * evaluate {@code lambda} with the arguments; otherwise, evaluate {@code infiniteCase}.  
   * In either case, push the arguments onto the stack during lambda evaluation.
   * 
   * @return  The value of {@code lambda}, or the value of {@code infiniteCase}
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4, R> 
    R apply(Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> lambda, 
            Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> infiniteCase, 
            V1 arg1, V2 arg2, V3 arg3, V4 arg4, int threshold) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R> toApply = 
      (Lambda4<? super V1, ? super V2, ? super V3, ? super V4, ? extends R>)
        (contains(arg1, arg2, arg3, arg4, threshold) ? infiniteCase : lambda);
    push(arg1, arg2, arg3, arg4);
    try { return toApply.value(arg1, arg2, arg3, arg4); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4> RecursionStack4<T1, T2, T3, T4> make() { 
    return new RecursionStack4<T1, T2, T3, T4>();
  }

  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4> RecursionStack4<T1, T2, T3, T4> 
    make(Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends Quad<T1, T2, T3, T4>> quadFactory) {
    return new RecursionStack4<T1, T2, T3, T4>(quadFactory);
  }
  
}
