package edu.rice.cs.plt.iter;

import java.util.*;
import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;

import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.tuple.*;
import edu.rice.cs.plt.recur.RecurUtil;

/**
 * A collection of static methods operating on iterables.
 */
public class IterUtil {
  
  /** Prevents instance creation */
  private IterUtil() {}
  
  /** @return  {@code true} iff the given iterable contains no elements */
  public static boolean isEmpty(Iterable<?> iter) { 
    if (iter instanceof Collection<?>) { return ((Collection<?>) iter).isEmpty(); }
    else { return ! iter.iterator().hasNext(); }
  }
  
  /**
   * @return  The size of the given iterable.  Where possible (when {@code iter} is a 
   *          {@code SizedIterable} or a {@code Collection}), this is a potentially constant-time
   *          operation; otherwise, it is linear in the size of {@code iter} (if {@code iter}
   *          is infinite in this case, this method will never return).
   */
  public static int sizeOf(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).size(); }
    else if (iter instanceof Collection<?>) { return ((Collection<?>) iter).size(); }
    else {
      int result = 0;
      for (Object o : iter) { result++; }
      return result;
    }
  }
  
  /** @return  {@code true} iff the given iterable is known to have a fixed size */
  public static boolean isFixed(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).isFixed(); }
    else if (iter instanceof Collection<?>) { return isFixed((Collection<?>) iter); }
    else { return false; }
  }
  
  /** @return  {@code true} iff the given collection is known to have a fixed size */
  public static boolean isFixedCollection(Collection<?> iter) {
    return (iter == Collections.EMPTY_SET) || (iter == Collections.EMPTY_LIST);
  }
  
  /** 
   * @return  A string representation of the given iterable, matching the {@link Collection}
   *          conventions (results like {@code "[foo, bar, baz]"}); invokes 
   *          {@link RecurUtil#safeToString(Object)} on each element
   */
  public static String toString(Iterable<?> iter) {
    StringBuilder result = new StringBuilder();
    result.append("[");
    boolean first = true;
    for (Object obj : iter) {
      if (first) { first = false; }
      else { result.append(", "); }
      result.append(RecurUtil.safeToString(obj));
    }
    result.append("]");
    return result.toString();
  }
  
  /**
   * @return  {@code true} iff the lists are identical (according to {@code ==}), or they
   *          have the same size (according to {@link #sizeOf}) and each corresponding 
   *          element is equal (according to {@link Predicate2#EQUAL})
   */
  public static boolean isEqual(Iterable<?> iter1, Iterable<?> iter2) {
    if (iter1 == iter2) { return true; }
    else if (sizeOf(iter1) == sizeOf(iter2)) { return and(iter1, iter2, Predicate2.EQUAL); }
    else { return false; }
  }
  
  /**
   * @return  A hash code computed by xoring shifted copies of each element's hash code;
   *          the result is consistent with {@link #isEqual}, but may not be consistent with
   *          the input's {@code equals} and {@code hashCode} methods; invokes 
   *          {@code RecurUtil#safeHashCode(Object)} on each element
   */
  public static int hashCode(Iterable<?> iter) {
    int result = Iterable.class.hashCode();
    int shift = 0;
    // So that values in long lists don't get ignored, we mask shift to be < 16
    for (Object obj : iter) { result ^= RecurUtil.safeHashCode(obj) << (shift & 0xF); shift++; }
    return result;
  }
  
  /** 
   * Wrap the given {@link Collection} in a {@link SizedIterable}.  Subsequent changes made 
   * to the collection will be reflected in the result (if this is not the desired behavior,
   * a {@link SnapshotIterable} can be used instead).
   */
  public static <T> SizedIterable<T> asSizedIterable(final Collection<T> coll) {
    return new SizedIterable<T>() {
      public Iterator<T> iterator() { return coll.iterator(); }
      public int size() { return coll.size(); }
      public boolean isFixed() { return isFixedCollection(coll); }
    };
  }
  
  /**
   * Make a {@link List} with the given elements.  If the input <em>is</em> a {@code List},
   * casts it as such; otherwise, creates a new list.  In the second case, changes made to
   * one will necessarily not be reflected in the other.
   */
  public static <T> List<T> asList(Iterable<T> iter) {
    if (iter instanceof List<?>) { return (List<T>) iter; }
    else if (iter instanceof Collection<?>) { return new ArrayList<T>((Collection<T>) iter); }
    else {
      LinkedList<T> result = new LinkedList<T>();
      for (T e : iter) { result.add(e); }
      return result;
    }
  }
  
  /**
   * Make an iterator based on an older-style {@link Enumeration}.  If an {@code Iterable} is
   * needed (rather than an {@code Iterator}), the result can be wrapped in a 
   * {@link ReadOnceIterable}.
   */
  public static <T> ReadOnlyIterator<T> asIterator(final Enumeration<? extends T> en) {
    return new ReadOnlyIterator<T>() {
      public boolean hasNext() { return en.hasMoreElements(); }
      public T next() { return en.nextElement(); }
    };
  }
  
  /**
   * Make an iterator based on a {@link StringTokenizer}.  (This is similar to 
   * {@link #asIterator(Enumeration)}, but allows the tokenizer to be treated as an enumeration
   * of {@code String}s rather than, as defined, an enumeration of {@code Object}s.)  If an 
   * {@code Iterable} is needed (rather than an {@code Iterator}), the result can be wrapped in a 
   * {@link ReadOnceIterable}.
   */
  public static ReadOnlyIterator<String> asIterator(final StringTokenizer s) {
    return new ReadOnlyIterator<String>() {
      public boolean hasNext() { return s.hasMoreTokens(); }
      public String next() { return s.nextToken(); }
    };
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for all values in {@code iter}
   *          (computation halts immediately where the predicate fails)
   */
  public static <T> boolean and(Iterable<? extends T> iter, Predicate<? super T> pred) {
    for (T elt : iter) { if (!pred.value(elt)) { return false; } }
    return true;
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for some value in {@code iter}
   *          (computation halts immediately where the predicate succeeds)
   */
  public static <T> boolean or(Iterable<? extends T> iter, Predicate<? super T> pred) {
    for (T elt : iter) { if (pred.value(elt)) { return true; } }
    return false;
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for all corresponding values in 
   *          {@code iter1} and {@code iter2}, which are assumed to have the same length
   *          (computation halts immediately where the predicate fails)
   */
  public static <T1, T2> boolean and(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                     Predicate2<? super T1, ? super T2> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    while (i1.hasNext()) { if (!pred.value(i1.next(), i2.next())) { return false; } }
    return true;
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for some corresponding values in 
   *          {@code iter1} and {@code iter2}, which are assumed to have the same length
   *          (computation halts immediately where the predicate succeeds)
   */
  public static <T1, T2> boolean or(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                    Predicate2<? super T1, ? super T2> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    while (i1.hasNext()) { if (pred.value(i1.next(), i2.next())) { return true; } }
    return false;
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for all corresponding values in 
   *          the given iterables, which are assumed to all have the same length
   *          (computation halts immediately where the predicate fails)
   */
  public static <T1, T2, T3> boolean and(Iterable<? extends T1> iter1, 
                                         Iterable<? extends T2> iter2,
                                         Iterable<? extends T3> iter3,
                                         Predicate3<? super T1, ? super T2, ? super T3> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    Iterator<? extends T3> i3 = iter3.iterator();
    while (i1.hasNext()) { if (!pred.value(i1.next(), i2.next(), i3.next())) { return false; } }
    return true;
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for some corresponding values in 
   *          the given iterables, which are assumed to all have the same length
   *          (computation halts immediately where the predicate fails)
   */
  public static <T1, T2, T3> boolean or(Iterable<? extends T1> iter1, 
                                        Iterable<? extends T2> iter2,
                                        Iterable<? extends T3> iter3,
                                        Predicate3<? super T1, ? super T2, ? super T3> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    Iterator<? extends T3> i3 = iter3.iterator();
    while (i1.hasNext()) { if (pred.value(i1.next(), i2.next(), i3.next())) { return true; } }
    return false;
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for all corresponding values in 
   *          the given iterables, which are assumed to all have the same length
   *          (computation halts immediately where the predicate fails)
   */
  public static <T1, T2, T3, T4> boolean and(Iterable<? extends T1> iter1, 
                                             Iterable<? extends T2> iter2,
                                             Iterable<? extends T3> iter3,
                                             Iterable<? extends T4> iter4,
                                             Predicate4<? super T1, ? super T2, 
                                                        ? super T3, ? super T4> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    Iterator<? extends T3> i3 = iter3.iterator();
    Iterator<? extends T4> i4 = iter4.iterator();
    while (i1.hasNext()) { 
      if (!pred.value(i1.next(), i2.next(), i3.next(), i4.next())) { return false; }
    }
    return true;
  }
  
  /** 
   * @return  {@code true} iff the given predicate holds for some corresponding values in 
   *          the given iterables, which are assumed to all have the same length
   *          (computation halts immediately where the predicate fails)
   */
  public static <T1, T2, T3, T4> boolean or(Iterable<? extends T1> iter1, 
                                            Iterable<? extends T2> iter2,
                                            Iterable<? extends T3> iter3,
                                            Iterable<? extends T4> iter4,
                                            Predicate4<? super T1, ? super T2, 
                                                       ? super T3, ? super T4> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    Iterator<? extends T3> i3 = iter3.iterator();
    Iterator<? extends T4> i4 = iter4.iterator();
    while (i1.hasNext()) { 
      if (pred.value(i1.next(), i2.next(), i3.next(), i4.next())) { return true; }
    }
    return false;
  }
  
  /** @return  An iterable containing the values of the given thunks */
  public static <T> Iterable<T> valuesOf(Iterable<? extends Thunk<? extends T>> iter) {
    Lambda<Thunk<? extends T>, T> thunkValue = new Lambda<Thunk<? extends T>, T>() {
      public T value(Thunk<? extends T> arg) { return arg.value(); }
    };
    return new MappedIterable<Thunk<? extends T>, T>(iter, thunkValue);
  }
    
  /** @return  An iterable containing the values of the application of the given lambdas */
  public static <T, R> Iterable<R> 
    valuesOf(Iterable<? extends Lambda<? super T, ? extends R>> iter, final T arg) {
    Lambda<Lambda<? super T, ? extends R>, R> lambdaValue = 
      new Lambda<Lambda<? super T, ? extends R>, R>() {
        public R value(Lambda<? super T, ? extends R> lam) { return lam.value(arg); }
      };
    return new MappedIterable<Lambda<? super T, ? extends R>, R>(iter, lambdaValue);
  }
    
  /** @return  An iterable containing the values of the application of the given lambdas */
  public static <T1, T2, R> Iterable<R> 
    valuesOf(Iterable<? extends Lambda2<? super T1, ? super T2, ? extends R>> iter, 
             final T1 arg1, final T2 arg2) {
    Lambda<Lambda2<? super T1, ? super T2, ? extends R>, R> lambdaValue = 
      new Lambda<Lambda2<? super T1, ? super T2, ? extends R>, R>() {
        public R value(Lambda2<? super T1, ? super T2, ? extends R> lam) { 
          return lam.value(arg1, arg2);
        }
      };
    return new MappedIterable<Lambda2<? super T1, ? super T2, ? extends R>, R>(iter, lambdaValue);
  }
    
  /** @return  An iterable containing the values of the application of the given lambdas */
  public static <T1, T2, T3, R> Iterable<R> 
    valuesOf(Iterable<? extends Lambda3<? super T1, ? super T2, ? super T3, ? extends R>> iter, 
             final T1 arg1, final T2 arg2, final T3 arg3) {
    Lambda<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R> lambdaValue = 
      new Lambda<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R>() {
        public R value(Lambda3<? super T1, ? super T2, ? super T3, ? extends R> lam) { 
          return lam.value(arg1, arg2, arg3);
        }
      };
    return new MappedIterable<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, 
                              R>(iter, lambdaValue);
  }
    
  /** @return  An iterable containing the values of the application of the given lambdas */
  public static <T1, T2, T3, T4, R> Iterable<R> 
    valuesOf(Iterable<? extends Lambda4<? super T1, ? super T2, 
                                        ? super T3, ? super T4, ? extends R>> iter, 
             final T1 arg1, final T2 arg2, final T3 arg3, final T4 arg4) {
    Lambda<Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>, R> lambdaValue = 
      new Lambda<Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>, R>() {
        public R value(Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> lam) { 
          return lam.value(arg1, arg2, arg3, arg4);
        }
      };
    return new MappedIterable<Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>,
                              R>(iter, lambdaValue);
  }
    
  /** @return  An iterable containing the first values of the given tuples */
  public static <T> Iterable<T> firstsOf(Iterable<? extends Pair<? extends T, ?>> iter) {
    Lambda<Pair<? extends T, ?>, T> getFirst = new Lambda<Pair<? extends T, ?>, T>() {
      public T value(Pair<? extends T, ?> arg) { return arg.first(); }
    };
    return new MappedIterable<Pair<? extends T, ?>, T>(iter, getFirst);
  }
    
  /** @return  An iterable containing the second values of the given tuples */
  public static <T> Iterable<T> secondsOf(Iterable<? extends Pair<?, ? extends T>> iter) {
    Lambda<Pair<?, ? extends T>, T> getSecond = new Lambda<Pair<?, ? extends T>, T>() {
      public T value(Pair<?, ? extends T> arg) { return arg.second(); }
    };
    return new MappedIterable<Pair<?, ? extends T>, T>(iter, getSecond);
  }
    
  /** @return  An iterable containing the third values of the given tuples */
  public static <T> Iterable<T> thirdsOf(Iterable<? extends Triple<?, ?, ? extends T>> iter) {
    Lambda<Triple<?, ?, ? extends T>, T> getThird = new Lambda<Triple<?, ?, ? extends T>, T>() {
      public T value(Triple<?, ?, ? extends T> arg) { return arg.third(); }
    };
    return new MappedIterable<Triple<?, ?, ? extends T>, T>(iter, getThird);
  }
    
  /** @return  An iterable containing the fourth values of the given tuples */
  public static <T> Iterable<T> fourthsOf(Iterable<? extends Quad<?, ?, ?, ? extends T>> iter) {
    Lambda<Quad<?, ?, ?, ? extends T>, T> getFourth = new Lambda<Quad<?, ?, ?, ? extends T>, T>() {
      public T value(Quad<?, ?, ?, ? extends T> arg) { return arg.fourth(); }
    };
    return new MappedIterable<Quad<?, ?, ?, ? extends T>, T>(iter, getFourth);
  }
    
  /** @return  An iterable containing the fifth values of the given tuples */
  public static <T> Iterable<T> fifthsOf(Iterable<? extends Quint<?, ?, ?, ?, ? extends T>> iter) {
    Lambda<Quint<?, ?, ?, ?, ? extends T>, T> getFifth = 
      new Lambda<Quint<?, ?, ?, ?, ? extends T>, T>() {
        public T value(Quint<?, ?, ?, ?, ? extends T> arg) { return arg.fifth(); }
      };
    return new MappedIterable<Quint<?, ?, ?, ?, ? extends T>, T>(iter, getFifth);
  }
    
  /** @return  An iterable containing the sixth values of the given tuples */
  public static <T> Iterable<T> 
    sixthsOf(Iterable<? extends Sextet<?, ?, ?, ?, ?, ? extends T>> iter) {
    Lambda<Sextet<?, ?, ?, ?, ?, ? extends T>, T> getSixth = 
      new Lambda<Sextet<?, ?, ?, ?, ?, ? extends T>, T>() {
        public T value(Sextet<?, ?, ?, ?, ?, ? extends T> arg) { return arg.sixth(); }
      };
    return new MappedIterable<Sextet<?, ?, ?, ?, ?, ? extends T>, T>(iter, getSixth);
  }
    
  /** @return  An iterable containing the seventh values of the given tuples */
  public static <T> Iterable<T> 
    seventhsOf(Iterable<? extends Septet<?, ?, ?, ?, ?, ?, ? extends T>> iter) {
    Lambda<Septet<?, ?, ?, ?, ?, ?, ? extends T>, T> getSeventh = 
      new Lambda<Septet<?, ?, ?, ?, ?, ?, ? extends T>, T>() {
        public T value(Septet<?, ?, ?, ?, ?, ?, ? extends T> arg) { return arg.seventh(); }
      };
    return new MappedIterable<Septet<?, ?, ?, ?, ?, ?, ? extends T>, T>(iter, getSeventh);
  }
    
  /** @return  An iterable containing the eighth values of the given tuples */
  public static <T> Iterable<T> 
    eighthsOf(Iterable<? extends Octet<?, ?, ?, ?, ?, ?, ?, ? extends T>> iter) {
    Lambda<Octet<?, ?, ?, ?, ?, ?, ?, ? extends T>, T> getEighth = 
      new Lambda<Octet<?, ?, ?, ?, ?, ?, ?, ? extends T>, T>() {
        public T value(Octet<?, ?, ?, ?, ?, ?, ?, ? extends T> arg) { return arg.eighth(); }
      };
    return new MappedIterable<Octet<?, ?, ?, ?, ?, ?, ?, ? extends T>, T>(iter, getEighth);
  }
  
  /** 
   * @return  An iterable of {@link Pair}s of corresponding values from the given iterables 
   *          (assumed to always have the same length).  Useful for simultaneous iteration
   *          of multiple lists in a {@code for} loop.
   */
  public static <T1, T2> Iterable<Pair<T1, T2>> zip(Iterable<? extends T1> iter1, 
                                                    Iterable<? extends T2> iter2) {
    Lambda2<T1, T2, Pair<T1, T2>> makePair = new Lambda2<T1, T2, Pair<T1, T2>>() {
      public Pair<T1, T2> value(T1 arg1, T2 arg2) { return new Pair<T1, T2>(arg1, arg2); }
    };
    return new BinaryMappedIterable<T1, T2, Pair<T1, T2>>(iter1, iter2, makePair);
  }
    
  /** 
   * @return  An iterable of {@link Triple}s of corresponding values from the given iterables 
   *          (assumed to always have the same length).  Useful for simultaneous iteration
   *          of multiple lists in a {@code for} loop.
   */
  public static <T1, T2, T3> Iterable<Triple<T1, T2, T3>> zip(Iterable<? extends T1> iter1, 
                                                              Iterable<? extends T2> iter2,
                                                              Iterable<? extends T3> iter3) {
    Lambda3<T1, T2, T3, Triple<T1, T2, T3>> makeTriple = 
      new Lambda3<T1, T2, T3, Triple<T1, T2, T3>>() {
        public Triple<T1, T2, T3> value(T1 arg1, T2 arg2, T3 arg3) { 
          return new Triple<T1, T2, T3>(arg1, arg2, arg3);
        }
      };
    return new TernaryMappedIterable<T1, T2, T3, Triple<T1, T2, T3>>(iter1, iter2, iter3, 
                                                                     makeTriple);
  }
    
  /** 
   * @return  An iterable of {@link Quad}s of corresponding values from the given iterables 
   *          (assumed to always have the same length).  Useful for simultaneous iteration
   *          of multiple lists in a {@code for} loop.
   */
  public static <T1, T2, T3, T4> Iterable<Quad<T1, T2, T3, T4>> zip(Iterable<? extends T1> iter1, 
                                                                    Iterable<? extends T2> iter2,
                                                                    Iterable<? extends T3> iter3,
                                                                    Iterable<? extends T4> iter4) {
    Lambda4<T1, T2, T3, T4, Quad<T1, T2, T3, T4>> makeQuad = 
      new Lambda4<T1, T2, T3, T4, Quad<T1, T2, T3, T4>>() {
        public Quad<T1, T2, T3, T4> value(T1 arg1, T2 arg2, T3 arg3, T4 arg4) { 
          return new Quad<T1, T2, T3, T4>(arg1, arg2, arg3, arg4);
        }
      };
    return new QuaternaryMappedIterable<T1, T2, T3, T4, 
                                        Quad<T1, T2, T3, T4>>(iter1, iter2, iter3, iter4, makeQuad);
  }
    
  /**
   * Create an immutable SizedIterable containing the given values.  Unfortunately, the use of 
   * varargs here causes an unchecked warning at the call site when {@code T} is a non-reifiable
   * (generic or variable) type.  As a workaround, the function is overloaded to take a range
   * of fixed numbers of arguments, up to a practical limit.  Above that limit, this 
   * varargs version is matched.
   */
  public static <T> SizedIterable<T> makeIterable(T... values) {
    SizedIterable<T> result = EmptyIterable.make();
    for (T v : values) { result = ComposedIterable.make(result, v); }
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable() { 
    return EmptyIterable.make();
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1) {
    return SingletonIterable.make(v1);
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    return result;
  }
  
  /** Create a SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3, T v4) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    result = ComposedIterable.make(result, v4);
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3, T v4, T v5) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    result = ComposedIterable.make(result, v4);
    result = ComposedIterable.make(result, v5);
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3, T v4, T v5, T v6) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    result = ComposedIterable.make(result, v4);
    result = ComposedIterable.make(result, v5);
    result = ComposedIterable.make(result, v6);
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3, T v4, T v5, T v6, T v7) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    result = ComposedIterable.make(result, v4);
    result = ComposedIterable.make(result, v5);
    result = ComposedIterable.make(result, v6);
    result = ComposedIterable.make(result, v7);
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3, T v4, T v5, T v6, T v7, 
                                                  T v8) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    result = ComposedIterable.make(result, v4);
    result = ComposedIterable.make(result, v5);
    result = ComposedIterable.make(result, v6);
    result = ComposedIterable.make(result, v7);
    result = ComposedIterable.make(result, v8);
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3, T v4, T v5, T v6, T v7, 
                                                  T v8, T v9) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    result = ComposedIterable.make(result, v4);
    result = ComposedIterable.make(result, v5);
    result = ComposedIterable.make(result, v6);
    result = ComposedIterable.make(result, v7);
    result = ComposedIterable.make(result, v8);
    result = ComposedIterable.make(result, v9);
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values */
  public static <T> SizedIterable<T> makeIterable(T v1, T v2, T v3, T v4, T v5, T v6, T v7, 
                                                  T v8, T v9, T v10) {
    SizedIterable<T> result = SingletonIterable.make(v1);
    result = ComposedIterable.make(result, v2);
    result = ComposedIterable.make(result, v3);
    result = ComposedIterable.make(result, v4);
    result = ComposedIterable.make(result, v5);
    result = ComposedIterable.make(result, v6);
    result = ComposedIterable.make(result, v7);
    result = ComposedIterable.make(result, v8);
    result = ComposedIterable.make(result, v9);
    result = ComposedIterable.make(result, v10);
    return result;
  }
  
  
  /** 
   * @return  An iterable that traverses the given array
   * @throws IllegalArgumentException  If {@code array} is not an array
   */
  public static SizedIterable<?> arrayIterable(Object array) {
    if (!array.getClass().isArray()) { throw new IllegalArgumentException("Non-array argument"); }
    if (array instanceof Object[]) { return arrayIterable((Object[]) array); }
    else if (array instanceof int[]) { return arrayIterable((int[]) array); }
    else if (array instanceof char[]) { return arrayIterable((char[]) array); }
    else if (array instanceof byte[]) { return arrayIterable((byte[]) array); }
    else if (array instanceof double[]) { return arrayIterable((double[]) array); }
    else if (array instanceof boolean[]) { return arrayIterable((boolean[]) array); }
    else if (array instanceof short[]) { return arrayIterable((short[]) array); }
    else if (array instanceof long[]) { return arrayIterable((long[]) array); }
    else if (array instanceof float[]) { return arrayIterable((float[]) array); }
    else { throw new IllegalArgumentException("Unrecognized array type"); }
  }
  
  /** @return  An iterable that traverses the given array */
  public static <T> SizedIterable<T> arrayIterable(final T[] array) {
    return new SizedIterable<T>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<T> iterator() {
        return new IndexedIterator<T>() {
          protected int size() { return array.length; }
          protected T get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Boolean> arrayIterable(final boolean[] array) {
    return new SizedIterable<Boolean>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Boolean> iterator() {
        return new IndexedIterator<Boolean>() {
          protected int size() { return array.length; }
          protected Boolean get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Character> arrayIterable(final char[] array) {
    return new SizedIterable<Character>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Character> iterator() {
        return new IndexedIterator<Character>() {
          protected int size() { return array.length; }
          protected Character get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Byte> arrayIterable(final byte[] array) {
    return new SizedIterable<Byte>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Byte> iterator() {
        return new IndexedIterator<Byte>() {
          protected int size() { return array.length; }
          protected Byte get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Short> arrayIterable(final short[] array) {
    return new SizedIterable<Short>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Short> iterator() {
        return new IndexedIterator<Short>() {
          protected int size() { return array.length; }
          protected Short get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Integer> arrayIterable(final int[] array) {
    return new SizedIterable<Integer>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Integer> iterator() {
        return new IndexedIterator<Integer>() {
          protected int size() { return array.length; }
          protected Integer get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Long> arrayIterable(final long[] array) {
    return new SizedIterable<Long>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Long> iterator() {
        return new IndexedIterator<Long>() {
          protected int size() { return array.length; }
          protected Long get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Float> arrayIterable(final float[] array) {
    return new SizedIterable<Float>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Float> iterator() {
        return new IndexedIterator<Float>() {
          protected int size() { return array.length; }
          protected Float get(int i) { return array[i]; }
        };
      }
    };
  }
    
  /** @return  An iterable that traverses the given array */
  public static SizedIterable<Double> arrayIterable(final double[] array) {
    return new SizedIterable<Double>() {
      public int size() { return array.length; }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Double> iterator() {
        return new IndexedIterator<Double>() {
          protected int size() { return array.length; }
          protected Double get(int i) { return array[i]; }
        };
      }
    };
  }
  
  /** @return  An iterable that traverses the given {@link CharSequence} */
  public static SizedIterable<Character> charSequenceIterable(final CharSequence sequence) {
    return new SizedIterable<Character>() {
      public int size() { return sequence.length(); }
      
      public boolean isFixed() { return false; }
      
      public Iterator<Character> iterator() {
        return new IndexedIterator<Character>() {
          protected int size() { return sequence.length(); }
          protected Character get(int i) { return sequence.charAt(i); }
        };
      }
    };
  }
  
  /** 
   * @return  An iterable that traverses the given {@link String}; similar to 
   *          {@link #charSequenceIterable(CharSequence)}, but takes advantage of the fact
   *          that {@code String}s are immutable
   */
  public static SizedIterable<Character> charSequenceIterable(final String sequence) {
    return new SizedIterable<Character>() {
      public int size() { return sequence.length(); }
      
      public boolean isFixed() { return true; }
      
      public Iterator<Character> iterator() {
        return new IndexedIterator<Character>() {
          protected int size() { return sequence.length(); }
          protected Character get(int i) { return sequence.charAt(i); }
        };
      }
    };
  }
  
  /**
   * @return  An iterable that traverses the given {@link Reader}.  If an {@link IOException}
   *          occurs while reading, an {@link IllegalStateException} is thrown.  Note that, as a
   *          {@link ReadOnceIterable}, the result only allows a single traversal
   *          of its contents.
   */
  public static ReadOnceIterable<Character> readerIterable(final Reader in) {
    return new ReadOnceIterable<Character>(new ReadOnlyIterator<Character>() {
      private int _lookahead = readNext();

      public boolean hasNext() { return _lookahead >= 0; }
      
      public Character next() {
        if (_lookahead < 0) { throw new NoSuchElementException(); }
        Character result = (char) _lookahead;
        _lookahead = readNext();
        return result;
      }
      
      private int readNext() {
        try { return in.read(); }
        catch (IOException e) { throw new IllegalStateException(e); }
      }
      
    });
  }
  
  /**
   * @return  An iterable that traverses the given {@link InputStream}.  If an {@link IOException}
   *          occurs while reading, an {@link IllegalStateException} is thrown.  Note that, as a
   *          {@link ReadOnceIterable}, the result only allows a single traversal
   *          of its contents.
   */
  public static ReadOnceIterable<Byte> inputStreamIterable(final InputStream in) {
    return new ReadOnceIterable<Byte>(new ReadOnlyIterator<Byte>() {
      private int _lookahead = readNext();
      
      public boolean hasNext() { return _lookahead >= 0; }
      
      public Byte next() {
        if (_lookahead < 0) { throw new NoSuchElementException(); }
        Byte result = (byte) _lookahead;
        _lookahead = readNext();
        return result;
      }
      
      private int readNext() {
        try { return in.read(); }
        catch (IOException e) { throw new IllegalStateException(e); }
      }
      
    });
  }
  
}
