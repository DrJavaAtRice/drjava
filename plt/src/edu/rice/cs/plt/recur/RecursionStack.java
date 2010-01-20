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
import edu.rice.cs.plt.tuple.Wrapper;
import edu.rice.cs.plt.tuple.IdentityWrapper;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda;

/**
 * <p>A stack used to store the arguments of a recursive invocation in order to prevent
 * infinite recursion.  By checking that a given argument has not been used previously before
 * recurring, a client can prevent infinite recursion in some circumstances (such as
 * when traversing an infinite, immutable data structure).</p>
 * 
 * <p>The client may either choose to explicity check for containment, {@link #push} the 
 * argument, recur, and then {@link #pop}, or invoke one of a variety of lambda-based 
 * methods that perform these bookkeeping tasks automatically.  In the latter case, when an 
 * exception occurs between a {@code push} and a matching {@code pop}, the {@code pop} is 
 * guaranteed to execute before the exception propagates upward.  Thus, clients who do not 
 * directly invoke {@link #push} and {@link #pop} may assume that the stack is always in a 
 * consistent state.</p>
 * 
 * @see PrecomputedRecursionStack
 * @see RecursionStack2
 * @see RecursionStack3
 * @see RecursionStack4
 */
public class RecursionStack<T> {
  
  private final Lambda<? super T, ? extends Wrapper<T>> _wrapperFactory;
  private final Multiset<Wrapper<T>> _previous;
  private final LinkedList<Wrapper<T>> _stack;
  
  /** Create an empty recursion stack with an {@link IdentityWrapper} factory */
  public RecursionStack() { this(IdentityWrapper.<T>factory()); }
  
  /**
   * Create an empty recursion stack with the given {@code Wrapper} factory
   * @param wrapperFactory  A lambda used to produce a wrapper for values placed on the
   *                        stack.  This provides clients with control over the method used
   *                        to determine if a value has been seen previously.
   */
  public RecursionStack(Lambda<? super T, ? extends Wrapper<T>> wrapperFactory) {
    _wrapperFactory = wrapperFactory;
    _previous = new HashMultiset<Wrapper<T>>();
    _stack = new LinkedList<Wrapper<T>>();
  }
  
  /** 
   * @return  {@code true} iff a value identical (according to {@code ==}) to {@code arg}
   *          is currently on the stack
   */
  public boolean contains(T arg) { return _previous.contains(_wrapperFactory.value(arg)); }
  
  /** 
   * @return  {@code true} iff at least {@code threshold} values identical (according to 
   *          {@code ==}) to {@code arg} are currently on the stack
   */
  public boolean contains(T arg, int threshold) {
    return _previous.count(_wrapperFactory.value(arg)) >= threshold;
  }
  
  /** Add {@code arg} to the top of the stack */
  public void push(T arg) {
    Wrapper<T> wrapped = _wrapperFactory.value(arg);
    _stack.addLast(wrapped);
    _previous.add(wrapped);
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
   * Run the given runnable, unless {@code arg} is already on the stack; push {@code arg}
   * onto the stack during runnable execution
   */
  public void run(Runnable r, T arg) {
    if (!contains(arg)) { 
      push(arg);
      try { r.run(); }
      finally { pop(arg); }
    }
  }
  
  /**
   * Run the given runnable, unless {@code threshold} instances of {@code arg} are already 
   * on the stack; push {@code arg} onto the stack during runnable execution
   */
  public void run(Runnable r, T arg, int threshold) {
    if (!contains(arg, threshold)) { 
      push(arg);
      try { r.run(); }
      finally { pop(arg); }
    }
  }
  
  /**
   * If {@code arg} is not on the stack, run {@code r}; otherwise, run {@code infiniteCase}.  
   * In either case, push {@code arg} onto the stack during runnable execution.
   */
  public void run(Runnable r, Runnable infiniteCase, T arg) {
    Runnable toRun = (contains(arg) ? infiniteCase : r);
    push(arg);
    try { toRun.run(); }
    finally { pop(arg); }
  }
  
  /**
   * If less than {@code threshold} instances of {@code arg} are on the stack, run {@code r}; 
   * otherwise, run {@code infiniteCase}.  In either case, push {@code arg} onto the stack 
   * during runnable execution.
   */
  public void run(Runnable r, Runnable infiniteCase, T arg, int threshold) {
    Runnable toRun = (contains(arg, threshold) ? infiniteCase : r);
    push(arg);
    try { toRun.run(); }
    finally { pop(arg); }
  }
  
  /**
   * Run the given runnable with argument {@code arg}, unless {@code arg} is already on the 
   * stack; push {@code arg} onto the stack during runnable execution
   */
  public <V extends T> void run(Runnable1<? super V> r, V arg) {
    if (!contains(arg)) { 
      push(arg);
      try { r.run(arg); }
      finally { pop(arg); }
    }
  }
  
  /**
   * Run the given runnable with argument {@code arg}, unless {@code threshold} instances 
   * of {@code arg} are already on the stack; push {@code arg} onto the stack during 
   * runnable execution
   */
  public <V extends T> void run(Runnable1<? super V> r, V arg, int threshold) {
    if (!contains(arg, threshold)) { 
      push(arg);
      try { r.run(arg); }
      finally { pop(arg); }
    }
  }
  
  /**
   * If {@code arg} is not on the stack, run {@code r} with argument {@code arg}; otherwise, 
   * run {@code infiniteCase}.  In either case, push {@code arg} onto the stack during 
   * runnable execution.
   */
  public <V extends T> void run(Runnable1<? super V> r, Runnable1<? super V> infiniteCase, V arg) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Runnable1<? super V> toRun = 
      (Runnable1<? super V>) (contains(arg) ? infiniteCase : r);
    push(arg);
    try { toRun.run(arg); }
    finally { pop(arg); }
  }
  
  /**
   * If less than {@code threshold} instances of {@code arg} are on the stack, run {@code r}
   * with argument {@code arg}; otherwise, run {@code infiniteCase}.  In either case, 
   * push {@code arg} onto the stack during runnable execution.
   */
  public <V extends T> void run(Runnable1<? super V> r, Runnable1<? super V> infiniteCase, V arg, 
                                int threshold) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Runnable1<? super V> toRun = 
      (Runnable1<? super V>) (contains(arg, threshold) ? infiniteCase : r);
    push(arg);
    try { toRun.run(arg); }
    finally { pop(arg); }
  }
  
  /**
   * Evaluate the given thunk, unless {@code arg} is already on the stack; push {@code arg}
   * onto the stack during thunk evaluation
   * 
   * @return  The value of {@code thunk}, or {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, R infiniteCase, T arg) {
    if (!contains(arg)) { 
      push(arg);
      try { return thunk.value(); }
      finally { pop(arg); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * Evaluate the given thunk, unless {@code threshold} instances of {@code arg} are already 
   * on the stack; push {@code arg} onto the stack during thunk evaluation
   * 
   * @return  The value of {@code thunk}, or {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, R infiniteCase, T arg, int threshold) {
    if (!contains(arg, threshold)) { 
      push(arg);
      try { return thunk.value(); }
      finally { pop(arg); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * If {@code arg} is not on the stack, evaluate {@code thunk}; otherwise, evaluate 
   * {@code infiniteCase}.  In either case, push {@code arg} onto the stack during 
   * thunk evaluation.
   * 
   * @return  The value of {@code thunk}, or the value of {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, Thunk<? extends R> infiniteCase, T arg) {
    Thunk<? extends R> toApply = (contains(arg) ? infiniteCase : thunk);
    push(arg);
    try { return toApply.value(); }
    finally { pop(arg); }
  }
  
  /**
   * If less than {@code threshold} instances of {@code arg} are on the stack, evaluate 
   * {@code thunk}; otherwise, evaluate {@code infiniteCase}.  In either case, push 
   * {@code arg} onto the stack during thunk evaluation.
   * 
   * @return  The value of {@code thunk}, or the value of {@code infiniteCase}
   */
  public <R> R apply(Thunk<? extends R> thunk, Thunk<? extends R> infiniteCase, T arg, 
                     int threshold) {
    Thunk<? extends R> toApply = (contains(arg, threshold) ? infiniteCase : thunk);
    push(arg);
    try { return toApply.value(); }
    finally { pop(arg); }
  }
  
  /**
   * Evaluate the given lambda with argument {@code arg}, unless {@code arg} is already on 
   * the stack; push {@code arg} onto the stack during lambda evaluation
   * 
   * @return  The value of {@code lambda}, or {@code infiniteCase}
   */
  public <V extends T, R> R apply(Lambda<? super V, ? extends R> lambda, R infiniteCase, V arg) {
    if (!contains(arg)) { 
      push(arg);
      try { return lambda.value(arg); }
      finally { pop(arg); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * Evaluate the given lambda with argument {@code arg}, unless {@code threshold} instances 
   * of {@code arg} are already on the stack; push {@code arg} onto the stack during 
   * lambda evaluation
   * 
   * @return  The value of {@code lambda}, or {@code infiniteCase}
   */
  public <V extends T, R> R apply(Lambda<? super V, ? extends R> lambda, R infiniteCase, V arg, 
                                  int threshold) {
    if (!contains(arg, threshold)) { 
      push(arg);
      try { return lambda.value(arg); }
      finally { pop(arg); }
    }
    else { return infiniteCase; }
  }
  
  /**
   * If {@code arg} is not on the stack, evaluate {@code lambda} with argument {@code arg}; 
   * otherwise, evaluate {@code infiniteCase}.  In either case, push {@code arg} onto the
   * stack during lambda evaluation.
   * 
   * @return  The value of {@code lambda}, or the value of {@code infiniteCase}
   */
  public <V extends T, R> R apply(Lambda<? super V, ? extends R> lambda, 
                                  Lambda<? super V, ? extends R> infiniteCase, V arg) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Lambda<? super V, ? extends R> toApply = 
      (Lambda<? super V, ? extends R>) (contains(arg) ? infiniteCase : lambda);
    push(arg);
    try { return toApply.value(arg); }
    finally { pop(arg); }
  }
  
  /**
   * If less than {@code threshold} instances of {@code arg} are on the stack, evaluate
   * {@code lambda} with argument {@code arg}; otherwise, evaluate {@code infiniteCase}.  
   * In either case, push {@code arg} onto the stack during lambda evaluation.
   * 
   * @return  The value of {@code lambda}, or the value of {@code infiniteCase}
   */
  public <V extends T, R> R apply(Lambda<? super V, ? extends R> lambda, 
                                  Lambda<? super V, ? extends R> infiniteCase, V arg, 
                                  int threshold) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Lambda<? super V, ? extends R> toApply = 
      (Lambda<? super V, ? extends R>) (contains(arg, threshold) ? infiniteCase : lambda);
    push(arg);
    try { return toApply.value(arg); }
    finally { pop(arg); }
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> RecursionStack<T> make() { return new RecursionStack<T>(); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> RecursionStack<T> make(Lambda<? super T, ? extends Wrapper<T>> wrapperFactory) {
    return new RecursionStack<T>(wrapperFactory);
  }
  
}
