package edu.rice.cs.plt.recur;

import java.util.LinkedList;
import edu.rice.cs.plt.collect.Multiset;
import edu.rice.cs.plt.collect.HashMultiset;
import edu.rice.cs.plt.tuple.IdentityWrapper;
import edu.rice.cs.plt.lambda.Command;
import edu.rice.cs.plt.lambda.Command1;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda;

/**
 * A stack used to store the arguments of a recursive invocation in order to prevent
 * infinite recursion.  By checking that a given argument has not been used previously before
 * recurring, a client can prevent infinite recursion in some circumstances (such as
 * when traversing an infinite, immutable data structure).  The client may either choose
 * to explicity check for containment, {@link #push} the argument, recur, and then 
 * {@link #pop}, or invoke one of a variety of lambda-based methods that perform these
 * bookkeeping tasks automatically.  In the latter case, when an exception occurs between
 * a {@code push} and a matching {@code pop}, the {@code pop} is guaranteed to execute before 
 * the exception propagates upward.  Thus, clients who do not directly invoke {@link #push}
 * and {@link #pop} may assume that the stack is always in a consistent state.
 * 
 * @see RecursionStack2
 * @see RecursionStack3
 * @see RecursionStack4
 */
public class RecursionStack<T> {
  
  private Multiset<IdentityWrapper<T>> _previous;
  private LinkedList<IdentityWrapper<T>> _stack;
  
  /** Create an empty recursion stack */
  public RecursionStack() {
    _previous = new HashMultiset<IdentityWrapper<T>>();
    _stack = new LinkedList<IdentityWrapper<T>>();
  }
  
  /** 
   * @return  {@code true} iff a value identical (according to {@code ==}) to {@code arg}
   *          is currently on the stack
   */
  public boolean contains(T arg) { return _previous.contains(new IdentityWrapper<T>(arg)); }
  
  /** 
   * @return  {@code true} iff at least {@code threshold} values identical (according to 
   *          {@code ==}) to {@code arg} are currently on the stack
   */
  public boolean contains(T arg, int threshold) {
    return _previous.count(new IdentityWrapper<T>(arg)) >= threshold;
  }
  
  /** Add {@code arg} to the top of the stack */
  public void push(T arg) {
    IdentityWrapper<T> wrapped = new IdentityWrapper<T>(arg);
    _stack.addLast(wrapped);
    _previous.add(wrapped);
  }
  
  /** 
   * Remove {@code arg} from the top of the stack
   * @throws IllegalArgumentException  If {@code arg} is not at the top of the stack
   */
  public void pop(T arg) {
    IdentityWrapper<T> wrapped = new IdentityWrapper<T>(arg);
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
   * Run the given command, unless {@code arg} is already on the stack; push {@code arg}
   * onto the stack during command execution
   */
  public void run(Command c, T arg) {
    if (!contains(arg)) { 
      push(arg);
      try { c.run(); }
      finally { pop(arg); }
    }
  }
  
  /**
   * Run the given command, unless {@code threshold} instances of {@code arg} are already 
   * on the stack; push {@code arg} onto the stack during command execution
   */
  public void run(Command c, T arg, int threshold) {
    if (!contains(arg, threshold)) { 
      push(arg);
      try { c.run(); }
      finally { pop(arg); }
    }
  }
  
  /**
   * If {@code arg} is not on the stack, run {@code c}; otherwise, run {@code infiniteCase}.  
   * In either case, push {@code arg} onto the stack during command execution.
   */
  public void run(Command c, Command infiniteCase, T arg) {
    Command toRun = (contains(arg) ? infiniteCase : c);
    push(arg);
    try { toRun.run(); }
    finally { pop(arg); }
  }
  
  /**
   * If less than {@code threshold} instances of {@code arg} are on the stack, run {@code c}; 
   * otherwise, run {@code infiniteCase}.  In either case, push {@code arg} onto the stack 
   * during command execution.
   */
  public void run(Command c, Command infiniteCase, T arg, int threshold) {
    Command toRun = (contains(arg, threshold) ? infiniteCase : c);
    push(arg);
    try { toRun.run(); }
    finally { pop(arg); }
  }
  
  /**
   * Run the given command with argument {@code arg}, unless {@code arg} is already on the 
   * stack; push {@code arg} onto the stack during command execution
   */
  public <V extends T> void run(Command1<? super V> c, V arg) {
    if (!contains(arg)) { 
      push(arg);
      try { c.run(arg); }
      finally { pop(arg); }
    }
  }
  
  /**
   * Run the given command with argument {@code arg}, unless {@code threshold} instances 
   * of {@code arg} are already on the stack; push {@code arg} onto the stack during 
   * command execution
   */
  public <V extends T> void run(Command1<? super V> c, V arg, int threshold) {
    if (!contains(arg, threshold)) { 
      push(arg);
      try { c.run(arg); }
      finally { pop(arg); }
    }
  }
  
  /**
   * If {@code arg} is not on the stack, run {@code c} with argument {@code arg}; otherwise, 
   * run {@code infiniteCase}.  In either case, push {@code arg} onto the stack during 
   * command execution.
   */
  public <V extends T> void run(Command1<? super V> c, Command1<? super V> infiniteCase, V arg) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Command1<? super V> toRun = 
      (Command1<? super V>) (contains(arg) ? infiniteCase : c);
    push(arg);
    try { toRun.run(arg); }
    finally { pop(arg); }
  }
  
  /**
   * If less than {@code threshold} instances of {@code arg} are on the stack, run {@code c}
   * with argument {@code arg}; otherwise, run {@code infiniteCase}.  In either case, 
   * push {@code arg} onto the stack during command execution.
   */
  public <V extends T> void run(Command1<? super V> c, Command1<? super V> infiniteCase, V arg, 
                                int threshold) {
    // The javac type checker is broken here
    @SuppressWarnings("unchecked") Command1<? super V> toRun = 
      (Command1<? super V>) (contains(arg, threshold) ? infiniteCase : c);
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
  
}
