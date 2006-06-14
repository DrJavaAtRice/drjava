package edu.rice.cs.plt.lambda;

import edu.rice.cs.plt.iter.IterUtil;

/**
 * A collection of static methods that create or operate on lambdas, commands, and predicates.
 * Since most of these operations apply to lambdas of arbitrary arity, they are defined here 
 * in groups with similar or identical names, but slightly different types.  These groups 
 * include:<ul>
 * <li>{@code nullThunk}, {@code nullLambda}, etc.: create a lambda that returns {@code null}</li>
 * <li>{@code valueThunk}, {@code valueLambda}, etc.: create a lambda that returns a value
 *     determined at creation time</li>
 * <li>{@code promote}: add an additional, ignored argument to a command, lambda, or predicate</li>
 * <li>{@code compose}: define the lambda that takes the result of one lambda and applies it
 *     to another</li>
 * <li>{@code curry}, {@code curryFirst}, etc.: convert an n-ary lambda to a unary lambda 
 *     whose result is another lambda</li>
 * <li>{@code negate}: define a predicate whose result is the opposite of the given predicate</li>
 * <li>{@code and}: define a conjunction of predicates</li>
 * <li>{@code or}: define a disjunction of predicates</li>
 * <li>{@code asCommand}: define a command equivalent to the given lambda, ignoring the result</li>
 * <li>{@code asLambda}: define a lambda equivalent to the given command, returning the given
 *     result after execution</li>
 * <li>{@code asPredicate}: treat a lambda with a {@code Boolean} return type as a predicate</li>
 */
public final class LambdaUtil {
  
  /** Prevents instance creation */
  private LambdaUtil() {}
  
  /** @return  The identity lambda for the type {@code T} */
  public static <T> Lambda<T, T> identity() {
    return new Lambda<T, T>() { public T value(T arg) { return arg; } };
  }
  
  /** @return  A {@link Thunk} whose result is always {@code null} */
  public static <T> Thunk<T> nullThunk() {
    return new Thunk<T>() { public T value() { return null; } };
  }
  
  /** @return  A {@link Lambda} whose result is always {@code null} */
  public static <T> Lambda<Object, T> nullLambda() {
    return new Lambda<Object, T>() { public T value(Object arg) { return null; } };
  }
  
  /** @return  A {@link Lambda2} whose result is always {@code null} */
  public static <T> Lambda2<Object, Object, T> nullLambda2() {
    return new Lambda2<Object, Object, T>() { 
      public T value(Object arg1, Object arg2) { return null; }
    };
  }
  
  /** @return  A {@link Lambda3} whose result is always {@code null} */
  public static <T> Lambda3<Object, Object, Object, T> nullLambda3() {
    return new Lambda3<Object, Object, Object, T>() { 
      public T value(Object arg1, Object arg2, Object arg3) { return null; }
    };
  }
  
  /** @return  A {@link Lambda4} whose result is always {@code null} */
  public static <T> Lambda4<Object, Object, Object, Object, T> nullLambda4() {
    return new Lambda4<Object, Object, Object, Object, T>() { 
      public T value(Object arg1, Object arg2, Object arg3, Object arg4) { return null; }
    };
  }
  
  /** @return  A {@link Thunk} whose result is always {@code val} */
  public static <T> Thunk<T> valueThunk(final T val) {
    return new Thunk<T>() { public T value() { return val; } };
  }
  
  /** @return  A {@link Lambda} whose result is always {@code val} */
  public static <T> Lambda<Object, T> valueLambda(final T val) {
    return new Lambda<Object, T>() { public T value(Object arg) { return val; } };
  }
  
  /** @return  A {@link Lambda2} whose result is always {@code val} */
  public static <T> Lambda2<Object, Object, T> valueLambda2(final T val) {
    return new Lambda2<Object, Object, T>() { 
      public T value(Object arg1, Object arg2) { return val; }
    };
  }
  
  /** @return  A {@link Lambda3} whose result is always {@code val} */
  public static <T> Lambda3<Object, Object, Object, T> valueLambda3(final T val) {
    return new Lambda3<Object, Object, Object, T>() { 
      public T value(Object arg1, Object arg2, Object arg3) { return val; }
    };
  }
  
  /** @return  A {@link Lambda4} whose result is always {@code val} */
  public static <T> Lambda4<Object, Object, Object, Object, T> valueLambda4(final T val) {
    return new Lambda4<Object, Object, Object, Object, T>() { 
      public T value(Object arg1, Object arg2, Object arg3, Object arg4) { return val; }
    };
  }
  
  /** @return A {@code Command1} equivalent to {@code c} with an ignored argument */
  public static Command1<Object> promote(final Command c) {
    return new Command1<Object>() {
      public void run(Object arg) { c.run(); }
    };
  }
  
  /** @return A {@code Command2} equivalent to {@code c} with an additional, ignored argument */
  public static <T> Command2<T, Object> promote(final Command1<? super T> c) {
    return new Command2<T, Object>() {
      public void run(T arg1, Object arg2) { c.run(arg1); }
    };
  }
  
  /** @return A {@code Command3} equivalent to {@code c} with an additional, ignored argument */
  public static <T1, T2> Command3<T1, T2, Object> promote(final Command2<? super T1, ? super T2> c) {
    return new Command3<T1, T2, Object>() {
      public void run(T1 arg1, T2 arg2, Object arg3) { c.run(arg1, arg2); }
    };
  }
  
  /** @return A {@code Command4} equivalent to {@code c} with an additional, ignored argument */
  public static <T1, T2, T3> 
    Command4<T1, T2, T3, Object> promote(final Command3<? super T1, ? super T2, ? super T3> c) {
    return new Command4<T1, T2, T3, Object>() {
      public void run(T1 arg1, T2 arg2, T3 arg3, Object arg4) { c.run(arg1, arg2, arg3); }
    };
  }
  
  /** @return A {@code Lambda} equivalent to {@code thunk} with an ignored argument */
  public static <R> Lambda<Object, R> promote(final Thunk<? extends R> thunk) {
    return new Lambda<Object, R>() {
      public R value(Object arg) { return thunk.value(); }
    };
  }
  
  /** @return A {@code Lambda2} equivalent to {@code lambda} with an additional, ignored argument */
  public static <T, R> Lambda2<T, Object, R> promote(final Lambda<? super T, ? extends R> lambda) {
    return new Lambda2<T, Object, R>() {
      public R value(T arg1, Object arg2) { return lambda.value(arg1); }
    };
  }
  
  /** @return A {@code Lambda3} equivalent to {@code lambda} with an additional, ignored argument */
  public static <T1, T2, R> 
    Lambda3<T1, T2, Object, R> promote(final Lambda2<? super T1, ? super T2, ? extends R> lambda) {
    return new Lambda3<T1, T2, Object, R>() {
      public R value(T1 arg1, T2 arg2, Object arg3) { return lambda.value(arg1, arg2); }
    };
  }
  
  /** @return A {@code Lambda4} equivalent to {@code lambda} with an additional, ignored argument */
  public static <T1, T2, T3, R> 
    Lambda4<T1, T2, T3, Object, R> promote(final Lambda3<? super T1, ? super T2, 
                                                         ? super T3, ? extends R> lambda) {
    return new Lambda4<T1, T2, T3, Object, R>() {
      public R value(T1 arg1, T2 arg2, T3 arg3, Object arg4) { 
        return lambda.value(arg1, arg2, arg3);
      }
    };
  }
  
  /** @return A {@code Predicate2} equivalent to {@code pred} with an additional, ignored argument */
  public static <T> Predicate2<T, Object> promote(final Predicate<? super T> pred) {
    return new Predicate2<T, Object>() {
      public Boolean value(T arg1, Object arg2) { return pred.value(arg1); }
    };
  }
  
  /** @return A {@code Predicate3} equivalent to {@code pred} with an additional, ignored argument */
  public static <T1, T2> 
    Predicate3<T1, T2, Object> promote(final Predicate2<? super T1, ? super T2> pred) {
    return new Predicate3<T1, T2, Object>() {
      public Boolean value(T1 arg1, T2 arg2, Object arg3) { return pred.value(arg1, arg2); }
    };
  }
  
  /** @return A {@code Predicate4} equivalent to {@code pred} with an additional, ignored argument */
  public static <T1, T2, T3> 
    Predicate4<T1, T2, T3, Object> promote(final Predicate3<? super T1, ? super T2, 
                                                            ? super T3> pred) {
    return new Predicate4<T1, T2, T3, Object>() {
      public Boolean value(T1 arg1, T2 arg2, T3 arg3, Object arg4) { 
        return pred.value(arg1, arg2, arg3);
      }
    };
  }
  
  /** @return  A thunk that executes {@code lambda} with the result of {@code thunk} */
  public static <T, U> Thunk<U> compose(final Thunk<? extends T> thunk, 
                                        final Lambda<? super T, ? extends U> lambda) {
    return new Thunk<U>() {
      public U value() { return lambda.value(thunk.value()); }
    };
  }
  
  /** @return  A lambda that executes {@code l2} with the result of {@code l1} */
  public static <S, T, U> Lambda<S, U> compose(final Lambda<? super S, ? extends T> l1, 
                                               final Lambda<? super T, ? extends U> l2) {
    return new Lambda<S, U>() {
      public U value(S arg) { return l2.value(l1.value(arg)); }
    };
  }
  
  /** @return  A lambda that executes {@code l2} with the result of {@code l1} */
  public static <S1, S2, T, U> Lambda2<S1, S2, U> 
    compose(final Lambda2<? super S1, ? super S2, ? extends T> l1, 
            final Lambda<? super T, ? extends U> l2) {
    return new Lambda2<S1, S2, U>() {
      public U value(S1 arg1, S2 arg2) { return l2.value(l1.value(arg1, arg2)); }
    };
  }
  
  /** @return  A lambda that executes {@code l2} with the result of {@code l1} */
  public static <S1, S2, S3, T, U> Lambda3<S1, S2, S3, U> 
    compose(final Lambda3<? super S1, ? super S2, ? super S3, ? extends T> l1, 
            final Lambda<? super T, ? extends U> l2) {
    return new Lambda3<S1, S2, S3, U>() {
      public U value(S1 arg1, S2 arg2, S3 arg3) { return l2.value(l1.value(arg1, arg2, arg3)); }
    };
  }
  
  /** @return  A lambda that executes {@code l2} with the result of {@code l1} */
  public static <S1, S2, S3, S4, T, U> Lambda4<S1, S2, S3, S4, U> 
    compose(final Lambda4<? super S1, ? super S2, ? super S3, ? super S4, ? extends T> l1, 
            final Lambda<? super T, ? extends U> l2) {
    return new Lambda4<S1, S2, S3, S4, U>() {
      public U value(S1 a1, S2 a2, S3 a3, S4 a4) { return l2.value(l1.value(a1, a2, a3, a4)); }
    };
  }
  
  /** @return  A command that executes {@code c1} followed by {@code c2} */
  public static Command compose(final Command c1, final Command c2) { 
    return new Command() {
      public void run() { c1.run(); c2.run(); }
    };
  }
  
  /** @return  A command that executes {@code c1} followed by {@code c2} with the same input */
  public static <T> Command1<T> compose(final Command1<? super T> c1, 
                                        final Command1<? super T> c2) { 
    return new Command1<T>() {
      public void run(T arg) { c1.run(arg); c2.run(arg); }
    };
  }
  
  /** @return  A command that executes {@code c1} followed by {@code c2} with the same input */
  public static <T1, T2> Command2<T1, T2> compose(final Command2<? super T1, ? super T2> c1, 
                                                  final Command2<? super T1, ? super T2> c2) { 
    return new Command2<T1, T2>() {
      public void run(T1 arg1, T2 arg2) { c1.run(arg1, arg2); c2.run(arg1, arg2); }
    };
  }
  
  /** @return  A command that executes {@code c1} followed by {@code c2} with the same input */
  public static <T1, T2, T3> Command3<T1, T2, T3> 
    compose(final Command3<? super T1, ? super T2, ? super T3> c1, 
            final Command3<? super T1, ? super T2, ? super T3> c2) {
    return new Command3<T1, T2, T3>() {
      public void run(T1 a1, T2 a2, T3 a3) { c1.run(a1, a2, a3); c2.run(a1, a2, a3); }
    };
  }
  
  /** @return  A command that executes {@code c1} followed by {@code c2} with the same input */
  public static <T1, T2, T3, T4> Command4<T1, T2, T3, T4>
    compose(final Command4<? super T1, ? super T2, ? super T3, ? super T4> c1, 
            final Command4<? super T1, ? super T2, ? super T3, ? super T4> c2) {
    return new Command4<T1, T2, T3, T4>() {
      public void run(T1 a1, T2 a2, T3 a3, T4 a4) { 
        c1.run(a1, a2, a3, a4); c2.run(a1, a2, a3, a4);
      }
    };
  }
  
  /**
   * @return  A curried version of the input, which accepts a single argument and returns a
   *           lambda on the second argument
   */
  public static <T1, T2, R> Lambda<T1, Lambda<T2, R>> 
    curry(final Lambda2<? super T1, ? super T2, ? extends R> lambda) {
    return new Lambda<T1, Lambda<T2, R>>() {
      public Lambda<T2, R> value(final T1 arg1) {
        return new Lambda<T2, R>() {
          public R value(T2 arg2) { return lambda.value(arg1, arg2); }
        };
      }
    };
  }
  
  /**
   * @return  A curried version of the input, which accepts a single argument and returns a
   *          curried lambda on the other arguments
   */
  public static <T1, T2, T3, R> Lambda<T1, Lambda<T2, Lambda<T3, R>>> 
    curry(final Lambda3<? super T1, ? super T2, ? super T3, ? extends R> lambda) {
    final Lambda<T1, Lambda2<T2, T3, R>> partial = curryFirst(lambda);
    return new Lambda<T1, Lambda<T2, Lambda<T3, R>>>() {
      public Lambda<T2, Lambda<T3, R>> value(T1 arg1) {
        return curry(partial.value(arg1));
      }
    };
  }
        
  /**
   * @return  A curried version of the input, which accepts a single argument and returns a
   *          curried lambda on the other arguments
   */
  public static <T1, T2, T3, T4, R> Lambda<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>> 
    curry(final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda) {
    final Lambda<T1, Lambda3<T2, T3, T4, R>> partial = curryFirst(lambda);
    return new Lambda<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>>() {
      public Lambda<T2, Lambda<T3, Lambda<T4, R>>> value(T1 arg1) {
        return curry(partial.value(arg1));
      }
    };
  }
        
  /**
   * @return  A partially-curried version of the input, accepting the first argument and
   *          returning a lambda on the other arguments
   */
  public static <T1, T2, T3, R> Lambda<T1, Lambda2<T2, T3, R>> 
    curryFirst(final Lambda3<? super T1, ? super T2, ? super T3, ? extends R> lambda) {
    return new Lambda<T1, Lambda2<T2, T3, R>>() {
      public Lambda2<T2, T3, R> value(final T1 arg1) {
        return new Lambda2<T2, T3, R>() {
          public R value(T2 arg2, T3 arg3) { return lambda.value(arg1, arg2, arg3); }
        };
      }
    };
  }
  
  /**
   * @return  A partially-curried version of the input, accepting the first argument and
   *          returning a lambda on the other arguments
   */
  public static <T1, T2, T3, T4, R> Lambda<T1, Lambda3<T2, T3, T4, R>> 
    curryFirst(final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda) {
    return new Lambda<T1, Lambda3<T2, T3, T4, R>>() {
      public Lambda3<T2, T3, T4, R> value(final T1 a1) {
        return new Lambda3<T2, T3, T4, R>() {
          public R value(T2 a2, T3 a3, T4 a4) { return lambda.value(a1, a2, a3, a4); }
        };
      }
    };
  }
  
  /**
   * @return  A curried version of the input, where the <em>second</em> argument is expected
   *          first
   */
  public static <T1, T2, R> Lambda<T2, Lambda<T1, R>> 
    currySecond(final Lambda2<? super T1, ? super T2, ? extends R> lambda) {
    return new Lambda<T2, Lambda<T1, R>>() {
      public Lambda<T1, R> value(final T2 arg2) {
        return new Lambda<T1, R>() {
          public R value(T1 arg1) { return lambda.value(arg1, arg2); }
        };
      }
    };
  }
        
  /**
   * @return  A partially-curried version of the input, accepting the second argument and
   *          returning a lambda on the other arguments
   */
  public static <T1, T2, T3, R> Lambda<T2, Lambda2<T1, T3, R>> 
    currySecond(final Lambda3<? super T1, ? super T2, ? super T3, ? extends R> lambda) {
    return new Lambda<T2, Lambda2<T1, T3, R>>() {
      public Lambda2<T1, T3, R> value(final T2 arg2) {
        return new Lambda2<T1, T3, R>() {
          public R value(T1 arg1, T3 arg3) { return lambda.value(arg1, arg2, arg3); }
        };
      }
    };
  }
  
  /**
   * @return  A partially-curried version of the input, accepting the second argument and
   *          returning a lambda on the other arguments
   */
  public static <T1, T2, T3, T4, R> Lambda<T2, Lambda3<T1, T3, T4, R>> 
    currySecond(final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda) {
    return new Lambda<T2, Lambda3<T1, T3, T4, R>>() {
      public Lambda3<T1, T3, T4, R> value(final T2 a2) {
        return new Lambda3<T1, T3, T4, R>() {
          public R value(T1 a1, T3 a3, T4 a4) { return lambda.value(a1, a2, a3, a4); }
        };
      }
    };
  }
  
  /**
   * @return  A partially-curried version of the input, accepting the second argument and
   *          returning a lambda on the other arguments
   */
  public static <T1, T2, T3, R> Lambda<T3, Lambda2<T1, T2, R>> 
    curryThird(final Lambda3<? super T1, ? super T2, ? super T3, ? extends R> lambda) {
    return new Lambda<T3, Lambda2<T1, T2, R>>() {
      public Lambda2<T1, T2, R> value(final T3 arg3) {
        return new Lambda2<T1, T2, R>() {
          public R value(T1 arg1, T2 arg2) { return lambda.value(arg1, arg2, arg3); }
        };
      }
    };
  }
  
  /**
   * @return  A partially-curried version of the input, accepting the second argument and
   *          returning a lambda on the other arguments
   */
  public static <T1, T2, T3, T4, R> Lambda<T3, Lambda3<T1, T2, T4, R>> 
    curryThird(final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda) {
    return new Lambda<T3, Lambda3<T1, T2, T4, R>>() {
      public Lambda3<T1, T2, T4, R> value(final T3 a3) {
        return new Lambda3<T1, T2, T4, R>() {
          public R value(T1 a1, T2 a2, T4 a4) { return lambda.value(a1, a2, a3, a4); }
        };
      }
    };
  }
  
  /**
   * @return  A partially-curried version of the input, accepting the second argument and
   *          returning a lambda on the other arguments
   */
  public static <T1, T2, T3, T4, R> Lambda<T4, Lambda3<T1, T2, T3, R>> 
    curryFourth(final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lambda) {
    return new Lambda<T4, Lambda3<T1, T2, T3, R>>() {
      public Lambda3<T1, T2, T3, R> value(final T4 a4) {
        return new Lambda3<T1, T2, T3, R>() {
          public R value(T1 a1, T2 a2, T3 a3) { return lambda.value(a1, a2, a3, a4); }
        };
      }
    };
  }
  
  /** @return  The negation ({@code !}) of {@code pred} */
  public static <T> Predicate<T> negate(final Predicate<? super T> pred) {
    return new Predicate<T>() { public Boolean value(T arg) { return !pred.value(arg); } };
  }
  
  /** @return  The conjunction ({@code &&}) of {@code p1} and {@code p2} */
  public static <T> Predicate<T> and(Predicate<? super T> p1, Predicate<? super T> p2) {
    return and(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The conjunction ({@code &&}) of the given predicates */
  public static <T> Predicate<T> and(final Iterable<? extends Predicate<? super T>> preds) {
    return new Predicate<T>() {
      public Boolean value(T arg) {
        for (Predicate<? super T> p : preds) { if (!p.value(arg)) { return false; } }
        return true;
      }
    };
  }
  
  /** @return  The disjunction ({@code ||}) of {@code p1} and {@code p2} */
  public static <T> Predicate<T> or(Predicate<? super T> p1, Predicate<? super T> p2) {
    return or(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The disjunction ({@code ||}) of the given predicates */
  public static <T> Predicate<T> or(final Iterable<? extends Predicate<? super T>> preds) {
    return new Predicate<T>() {
      public Boolean value(T arg) {
        for (Predicate<? super T> p : preds) { if (p.value(arg)) { return true; } }
        return false;
      }
    };
  }
  
  /** @return  The negation ({@code !}) of {@code pred} */
  public static <T1, T2> Predicate2<T1, T2> negate2(final Predicate2<? super T1, ? super T2> pred) {
    return new Predicate2<T1, T2>() { 
      public Boolean value(T1 arg1, T2 arg2) { return !pred.value(arg1, arg2); }
    };
  }
  
  /** @return  The conjunction ({@code &&}) of {@code p1} and {@code p2} */
  public static <T1, T2> Predicate2<T1, T2> and2(Predicate2<? super T1, ? super T2> p1, 
                                                 Predicate2<? super T1, ? super T2> p2) {
    return and2(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The conjunction ({@code &&}) of the given predicates */
  public static <T1, T2> 
    Predicate2<T1, T2> and2(final Iterable<? extends Predicate2<? super T1, ? super T2>> preds) {
    return new Predicate2<T1, T2>() {
      public Boolean value(T1 arg1, T2 arg2) {
        for (Predicate2<? super T1, ? super T2> p : preds) { 
          if (!p.value(arg1, arg2)) { return false; }
        }
        return true;
      }
    };
  }
  
  /** @return  The disjunction ({@code ||}) of {@code p1} and {@code p2} */
  public static <T1, T2> Predicate2<T1, T2> or2(Predicate2<? super T1, ? super T2> p1, 
                                                Predicate2<? super T1, ? super T2> p2) {
    return and2(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The disjunction ({@code ||}) of the given predicates */
  public static <T1, T2> 
    Predicate2<T1, T2> or2(final Iterable<? extends Predicate2<? super T1, ? super T2>> preds) {
    return new Predicate2<T1, T2>() {
      public Boolean value(T1 arg1, T2 arg2) {
        for (Predicate2<? super T1, ? super T2> p : preds) { 
          if (p.value(arg1, arg2)) { return true; }
        }
        return false;
      }
    };
  }
  
  /** @return  The negation ({@code !}) of {@code pred} */
  public static <T1, T2, T3> Predicate3<T1, T2, T3> 
    negate3(final Predicate3<? super T1, ? super T2, ? super T3> pred) {
    return new Predicate3<T1, T2, T3>() { 
      public Boolean value(T1 arg1, T2 arg2, T3 arg3) { return !pred.value(arg1, arg2, arg3); }
    };
  }
  
  /** @return  The conjunction ({@code &&}) of {@code p1} and {@code p2} */
  public static <T1, T2, T3> 
    Predicate3<T1, T2, T3> and3(Predicate3<? super T1, ? super T2, ? super T3> p1, 
                                Predicate3<? super T1, ? super T2, ? super T3> p2) {
    return and3(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The conjunction ({@code &&}) of the given predicates */
  public static <T1, T2, T3> 
    Predicate3<T1, T2, T3> and3(final Iterable<? extends Predicate3<? super T1, ? super T2, 
                                                                   ? super T3>> preds) {
    return new Predicate3<T1, T2, T3>() {
      public Boolean value(T1 arg1, T2 arg2, T3 arg3) {
        for (Predicate3<? super T1, ? super T2, ? super T3> p : preds) { 
          if (!p.value(arg1, arg2, arg3)) { return false; }
        }
        return true;
      }
    };
  }
  
  /** @return  The disjunction ({@code ||}) of {@code p1} and {@code p2} */
  public static <T1, T2, T3> 
    Predicate3<T1, T2, T3> or3(Predicate3<? super T1, ? super T2, ? super T3> p1, 
                               Predicate3<? super T1, ? super T2, ? super T3> p2) {
    return and3(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The disjunction ({@code ||}) of the given predicates */
  public static <T1, T2, T3> 
    Predicate3<T1, T2, T3> or3(final Iterable<? extends Predicate3<? super T1, ? super T2, 
                                                                   ? super T3>> preds) {
    return new Predicate3<T1, T2, T3>() {
      public Boolean value(T1 arg1, T2 arg2, T3 arg3) {
        for (Predicate3<? super T1, ? super T2, ? super T3> p : preds) { 
          if (p.value(arg1, arg2, arg3)) { return true; }
        }
        return false;
      }
    };
  }
  
  /** @return  The negation ({@code !}) of {@code pred} */
  public static <T1, T2, T3, T4> Predicate4<T1, T2, T3, T4> 
    negate4(final Predicate4<? super T1, ? super T2, ? super T3, ? super T4> pred) {
    return new Predicate4<T1, T2, T3, T4>() { 
      public Boolean value(T1 a1, T2 a2, T3 a3, T4 a4) { return !pred.value(a1, a2, a3, a4); }
    };
  }
  
  /** @return  The conjunction ({@code &&}) of {@code p1} and {@code p2} */
  public static <T1, T2, T3, T4> 
    Predicate4<T1, T2, T3, T4> and4(Predicate4<? super T1, ? super T2, ? super T3, ? super T4> p1,
                                    Predicate4<? super T1, ? super T2, ? super T3, ? super T4> p2) {
    return and4(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The conjunction ({@code &&}) of the given predicates */
  public static <T1, T2, T3, T4> 
    Predicate4<T1, T2, T3, T4> and4(final Iterable<? extends Predicate4<? super T1, ? super T2, 
                                                                        ? super T3, ? super T4>> preds) {
    return new Predicate4<T1, T2, T3, T4>() {
      public Boolean value(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
        for (Predicate4<? super T1, ? super T2, ? super T3, ? super T4> p : preds) { 
          if (!p.value(arg1, arg2, arg3, arg4)) { return false; }
        }
        return true;
      }
    };
  }
  
  /** @return  The disjunction ({@code ||}) of {@code p1} and {@code p2} */
  public static <T1, T2, T3, T4> 
    Predicate4<T1, T2, T3, T4> or4(Predicate4<? super T1, ? super T2, ? super T3, ? super T4> p1,
                                   Predicate4<? super T1, ? super T2, ? super T3, ? super T4> p2) {
    return and4(IterUtil.makeIterable(p1, p2));
  }
  
  /** @return  The disjunction ({@code ||}) of the given predicates */
  public static <T1, T2, T3, T4> 
    Predicate4<T1, T2, T3, T4> or4(final Iterable<? extends Predicate4<? super T1, ? super T2, 
                                                                       ? super T3, ? super T4>> preds) {
    return new Predicate4<T1, T2, T3, T4>() {
      public Boolean value(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
        for (Predicate4<? super T1, ? super T2, ? super T3, ? super T4> p : preds) { 
          if (p.value(arg1, arg2, arg3, arg4)) { return true; }
        }
        return false;
      }
    };
  }
  
  /** @return  A command that executes the given thunk (ignoring the result) */
  public static Command asCommand(final Thunk<?> thunk) {
    return new Command() { public void run() { thunk.value(); } };
  }
  
  /** @return  A command that executes the given lambda (ignoring the result) */
  public static <T> Command1<T> asCommand(final Lambda<? super T, ?> lambda) {
    return new Command1<T>() { public void run(T arg) { lambda.value(arg); } };
  }
  
  /** @return  A command that executes the given lambda (ignoring the result) */
  public static <T1, T2> Command2<T1, T2> asCommand(final Lambda2<? super T1, ? super T2, ?> lambda) {
    return new Command2<T1, T2>() { 
      public void run(T1 arg1, T2 arg2) { lambda.value(arg1, arg2); }
    };
  }
  
  /** @return  A command that executes the given lambda (ignoring the result) */
  public static <T1, T2, T3> Command3<T1, T2, T3> 
    asCommand(final Lambda3<? super T1, ? super T2, ? super T3, ?> lambda) {
    return new Command3<T1, T2, T3>() { 
      public void run(T1 arg1, T2 arg2, T3 arg3) { lambda.value(arg1, arg2, arg3); }
    };
  }
  
  /** @return  A command that executes the given lambda (ignoring the result) */
  public static <T1, T2, T3, T4> Command4<T1, T2, T3, T4> 
    asCommand(final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ?> lambda) {
    return new Command4<T1, T2, T3, T4>() { 
      public void run(T1 a1, T2 a2, T3 a3, T4 a4) { lambda.value(a1, a2, a3, a4); }
    };
  }
  
  /** @return  A thunk that executes the given command, then returns {@code result} */
  public static <R> Thunk<R> asThunk(final Command c, final R result) {
    return new Thunk<R>() { public R value() { c.run(); return result; } };
  }
  
  /** @return  A lambda that executes the given command, then returns {@code result} */
  public static <T, R> Lambda<T, R> asLambda(final Command1<? super T> c, final R result) {
    return new Lambda<T, R>() { public R value(T arg) { c.run(arg); return result; } };
  }
  
  /** @return  A lambda that executes the given command, then returns {@code result} */
  public static <T1, T2, R> Lambda2<T1, T2, R> asLambda(final Command2<? super T1, ? super T2> c, 
                                                 final R result) {
    return new Lambda2<T1, T2, R>() { 
      public R value(T1 arg1, T2 arg2) { c.run(arg1, arg2); return result; }
    };
  }
  
  /** @return  A lambda that executes the given command, then returns {@code result} */
  public static <T1, T2, T3, R> Lambda3<T1, T2, T3, R> 
    asLambda(final Command3<? super T1, ? super T2, ? super T3> c, final R result) {
    return new Lambda3<T1, T2, T3, R>() { 
      public R value(T1 arg1, T2 arg2, T3 arg3) { c.run(arg1, arg2, arg3); return result; }
    };
  }
  
  /** @return  A lambda that executes the given command, then returns {@code result} */
  public static <T1, T2, T3, T4, R> Lambda4<T1, T2, T3, T4, R> 
    asLambda(final Command4<? super T1, ? super T2, ? super T3, ? super T4> c, final R result) {
    return new Lambda4<T1, T2, T3, T4, R>() { 
      public R value(T1 a1, T2 a2, T3 a3, T4 a4) { c.run(a1, a2, a3, a4); return result; }
    };
  }
  
  /** @return  A predicate based on an input that acts as a predicate but is not types as one */
  public static <T> Predicate<T> asPredicate(final Lambda<? super T, ? extends Boolean> lambda) {
    return new Predicate<T>() { public Boolean value(T arg) { return lambda.value(arg); } };
  }
  
  /** @return  A predicate based on an input that acts as a predicate but is not types as one */
  public static <T1, T2> Predicate2<T1, T2> 
    asPredicate(final Lambda2<? super T1, ? super T2, ? extends Boolean> lambda) {
    return new Predicate2<T1, T2>() { 
      public Boolean value(T1 arg1, T2 arg2) { return lambda.value(arg1, arg2); }
    };
  }
  
  /** @return  A predicate based on an input that acts as a predicate but is not types as one */
  public static <T1, T2, T3> Predicate3<T1, T2, T3> 
    asPredicate(final Lambda3<? super T1, ? super T2, ? super T3, ? extends Boolean> lambda) {
    return new Predicate3<T1, T2, T3>() { 
      public Boolean value(T1 arg1, T2 arg2, T3 arg3) { return lambda.value(arg1, arg2, arg3); }
    };
  }
  
  /** @return  A predicate based on an input that acts as a predicate but is not types as one */
  public static <T1, T2, T3, T4> Predicate4<T1, T2, T3, T4> 
    asPredicate(final Lambda4<? super T1, ? super T2, 
                              ? super T3, ? super T4, ? extends Boolean> lambda) {
    return new Predicate4<T1, T2, T3, T4>() { 
      public Boolean value(T1 a1, T2 a2, T3 a3, T4 a4) { return lambda.value(a1, a2, a3, a4); }
    };
  }
 
}
