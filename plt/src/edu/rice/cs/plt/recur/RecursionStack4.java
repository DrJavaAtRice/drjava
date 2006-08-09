package edu.rice.cs.plt.recur;

import java.util.LinkedList;
import edu.rice.cs.plt.collect.Multiset;
import edu.rice.cs.plt.collect.HashMultiset;
import edu.rice.cs.plt.tuple.IdentityQuad;
import edu.rice.cs.plt.lambda.Command;
import edu.rice.cs.plt.lambda.Command4;
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
  
  private Multiset<IdentityQuad<T1, T2, T3, T4>> _previous;
  private LinkedList<IdentityQuad<T1, T2, T3, T4>> _stack;
  
  /** Create an empty recursion stack */
  public RecursionStack4() {
    _previous = new HashMultiset<IdentityQuad<T1, T2, T3, T4>>();
    _stack = new LinkedList<IdentityQuad<T1, T2, T3, T4>>();
  }
  
  /** 
   * @return  {@code true} iff a set of values identical (according to {@code ==}) to the 
   *          given arguments is currently on the stack
   */
  public boolean contains(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    return _previous.contains(new IdentityQuad<T1, T2, T3, T4>(arg1, arg2, arg3, arg4));
  }
  
  /** 
   * @return  {@code true} iff at least {@code threshold} sets of values identical 
   *          (according to {@code ==}) to the given arguments are currently on the stack
   */
  public boolean contains(T1 arg1, T2 arg2, T3 arg3, T4 arg4, int threshold) {
    return _previous.count(new IdentityQuad<T1, T2, T3, T4>(arg1, arg2, arg3, arg4)) >= threshold;
  }
  
  /** Add the given arguments to the top of the stack */
  public void push(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    IdentityQuad<T1, T2, T3, T4> wrapped = new IdentityQuad<T1, T2, T3, T4>(arg1, arg2, arg3, arg4);
    _stack.addLast(wrapped);
    _previous.add(wrapped);
  }
  
  /** 
   * Remove the given arguments from the top of the stack
   * @throws IllegalArgumentException  If the given arguments are not at the top of the stack
   */
  public void pop(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    IdentityQuad<T1, T2, T3, T4> wrapped = new IdentityQuad<T1, T2, T3, T4>(arg1, arg2, arg3, arg4);
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
   * Run the given command, unless the given arguments are already on the stack; push the 
   * arguments onto the stack during command execution
   */
  public void run(Command c, T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4);
      try { c.run(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * Run the given command, unless {@code threshold} instances of the given arguments are 
   * already on the stack; push the arguments onto the stack during command execution
   */
  public void run(Command c, T1 arg1, T2 arg2, T3 arg3, T4 arg4, int threshold) {
    if (!contains(arg1, arg2, arg3, arg4, threshold)) { 
      push(arg1, arg2, arg3, arg4);
      try { c.run(); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * If the given arguments are not on the stack, run {@code c}; otherwise, run 
   * {@code infiniteCase}.  In either case, push the arguments onto the stack during 
   * command execution.
   */
  public void run(Command c, Command infiniteCase, T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    Command toRun = (contains(arg1, arg2, arg3, arg4) ? infiniteCase : c);
    push(arg1, arg2, arg3, arg4);
    try { toRun.run(); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * If less than {@code threshold} instances of the given arguments are on the stack, run 
   * {@code c}; otherwise, run {@code infiniteCase}.  In either case, push the 
   * arguments onto the stack during command execution.
   */
  public void run(Command c, Command infiniteCase, T1 arg1, T2 arg2, T3 arg3, T4 arg4, int threshold) {
    Command toRun = (contains(arg1, arg2, arg3, arg4, threshold) ? infiniteCase : c);
    push(arg1, arg2, arg3, arg4);
    try { toRun.run(); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * Run the given command with the given arguments, unless the arguments are already on the 
   * stack; push the arguments onto the stack during command execution
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Command4<? super V1, ? super V2, ? super V3, ? super V4> c, V1 arg1, V2 arg2, 
             V3 arg3, V4 arg4) {
    if (!contains(arg1, arg2, arg3, arg4)) { 
      push(arg1, arg2, arg3, arg4);
      try { c.run(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * Run the given command with the given arguments, unless {@code threshold} instances 
   * of the arguments are already on the stack; push the arguments onto the stack during 
   * command execution
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Command4<? super V1, ? super V2, ? super V3, ? super V4> c, V1 arg1, V2 arg2, 
             V3 arg3, V4 arg4, int threshold) {
    if (!contains(arg1, arg2, arg3, arg4, threshold)) { 
      push(arg1, arg2, arg3, arg4);
      try { c.run(arg1, arg2, arg3, arg4); }
      finally { pop(arg1, arg2, arg3, arg4); }
    }
  }
  
  /**
   * If the given arguments are not on the stack, run {@code c} with argument the arguments; 
   * otherwise, run {@code infiniteCase}.  In either case, push the arguments onto the 
   * stack during command execution.
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Command4<? super V1, ? super V2, ? super V3, ? super V4> c, 
             Command4<? super V1, ? super V2, ? super V3, ? super V4> infiniteCase, 
             V1 arg1, V2 arg2, V3 arg3, V4 arg4) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Command4<? super V1, ? super V2, ? super V3, ? super V4> toRun = 
      (Command4<? super V1, ? super V2, ? super V3, ? super V4>)
        (contains(arg1, arg2, arg3, arg4) ? infiniteCase : c);
    push(arg1, arg2, arg3, arg4);
    try { toRun.run(arg1, arg2, arg3, arg4); }
    finally { pop(arg1, arg2, arg3, arg4); }
  }
  
  /**
   * If less than {@code threshold} instances of the given arguments are on the stack, 
   * run {@code c} with the arguments; otherwise, run {@code infiniteCase}.  In either case, 
   * push the arguments onto the stack during command execution.
   */
  public <V1 extends T1, V2 extends T2, V3 extends T3, V4 extends T4>
    void run(Command4<? super V1, ? super V2, ? super V3, ? super V4> c, 
             Command4<? super V1, ? super V2, ? super V3, ? super V4> infiniteCase, 
             V1 arg1, V2 arg2, V3 arg3, V4 arg4, int threshold) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Command4<? super V1, ? super V2, ? super V3, ? super V4> toRun = 
      (Command4<? super V1, ? super V2, ? super V3, ? super V4>)
        (contains(arg1, arg2, arg3, arg4, threshold) ? infiniteCase : c);
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

}
