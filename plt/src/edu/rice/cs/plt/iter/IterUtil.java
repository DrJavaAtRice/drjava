package edu.rice.cs.plt.iter;

import java.util.*;
import java.lang.reflect.Array;
import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;

import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.tuple.*;
import edu.rice.cs.plt.recur.RecurUtil;
import edu.rice.cs.plt.collect.ConsList;
import edu.rice.cs.plt.text.TextUtil;

/**
 * <p>A collection of static methods operating on iterables and iterators.</p>
 * 
 * <p>Most classes instantiated by these methods are serializable.  However, since the classes generally 
 * wrap other objects, those objects must be serializable in order for serialization to succeed.</p>
 */

public final class IterUtil {
  
  /** Prevents instance creation */
  private IterUtil() {}
  
  /** @return  {@code true} iff the given iterable contains no elements */
  public static boolean isEmpty(Iterable<?> iter) { 
    if (iter instanceof Collection<?>) { return ((Collection<?>) iter).isEmpty(); }
    else { return ! iter.iterator().hasNext(); }
  }
  
  /**
   * Compute the size of the given iterable.  Where possible (when {@code iter} is a {@code SizedIterable} or a 
   * {@code Collection}), this is a potentially constant-time operation; otherwise, it is linear in the size of 
   * {@code iter} (if {@code iter} is infinite in this case, this method will loop {@code Integer.MAX_VALUE} times
   * before returning).
   */
  public static int sizeOf(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).size(); }
    else if (iter instanceof Collection<?>) { return ((Collection<?>) iter).size(); }
    else {
      int result = 0;
      for (Object o : iter) { result++; if (result == Integer.MAX_VALUE) break; }
      return result;
    }
  }
  
  /**
   * Compute the size of the given iterable, or {@code bound} -- whichever is less.  This allows a size to be 
   * computed where the iterable may be infinite.  Where possible (when {@code iter} is a {@code SizedIterable} 
   * or a {@code Collection}), this is a potentially constant-time operation; otherwise, it is linear in the size 
   * of {@code iter} or {@code bound} (whichever is smaller).
   */
  public static int sizeOf(Iterable<?> iter, int bound) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).size(bound); }
    else if (iter instanceof Collection<?>) {
      int result = ((Collection<?>) iter).size();
      return result <= bound ? result : bound;
    }
    else {
      int result = 0;
      for (Object o : iter) { result++; if (result == bound) break; }
      return result;
    }
  }
  
  /** @return  {@code true} iff the given iterable is known to have a fixed size */
  public static boolean isFixed(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).isFixed(); }
    else if (iter instanceof Collection<?>) { return isFixedCollection((Collection<?>) iter); }
    else { return false; }
  }
  
  /** @return  {@code true} iff the given collection is known to have a fixed size */
  private static boolean isFixedCollection(Collection<?> iter) {
    return (iter == Collections.EMPTY_SET) || (iter == Collections.EMPTY_LIST);
  }
  
  /** 
   * Generate a string representation of the given iterable, matching the {@link Collection}
   * conventions (results like {@code "[foo, bar, baz]"}); invokes 
   * {@link RecurUtil#safeToString(Object)} on each element
   */
  public static String toString(Iterable<?> iter) {
    return toString(iter, "[", ", ", "]");
  }
  
  /** 
   * Generate a string representation of the given iterable where each element is listed on a
   * separate line; invokes {@link RecurUtil#safeToString(Object)} on each element
   */
  public static String multilineToString(Iterable<?> iter) {
    return toString(iter, "", TextUtil.NEWLINE, "");
  }
  
  /** 
   * Generate a string representation of the given iterable beginning with {@code prefix}, ending with
   * {@code suffix}, and delimited by {@code delimiter}; invokes {@link RecurUtil#safeToString(Object)} 
   * on each element
   */
  public static String toString(Iterable<?> iter, String prefix, String delimiter, String suffix) {
    StringBuilder result = new StringBuilder();
    result.append(prefix);
    boolean first = true;
    for (Object obj : iter) {
      if (first) { first = false; }
      else { result.append(delimiter); }
      result.append(RecurUtil.safeToString(obj));
    }
    result.append(suffix);
    return result.toString();
  }
  
  /**
   * @return  {@code true} iff the lists are identical (according to {@code ==}), or they
   *          have the same size (according to {@link #sizeOf}) and each corresponding 
   *          element is equal (according to {@link LambdaUtil#EQUAL})
   */
  public static boolean isEqual(Iterable<?> iter1, Iterable<?> iter2) {
    if (iter1 == iter2) { return true; }
    else if (sizeOf(iter1) == sizeOf(iter2)) { return and(iter1, iter2, LambdaUtil.EQUAL); }
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
   * Make an iterator based on a (legacy-style) {@link Enumeration}.  If an {@code Iterable} is
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
   * Make an iterator based on a {@link Reader}.  If an {@link IOException}
   * occurs while reading, an {@link IllegalStateException} is thrown.  If an 
   * {@code Iterable} is needed (rather than an {@code Iterator}), the result 
   * can be wrapped in a {@link ReadOnceIterable}.
   */
  public static ReadOnlyIterator<Character> asIterator(final Reader in) {
    return new ReadOnlyIterator<Character>() {
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
      
    };
  }
  
  /**
   * Make an iterator based on an {@link InputStream}.  If an {@link IOException}
   * occurs while reading, an {@link IllegalStateException} is thrown.  If an 
   * {@code Iterable} is needed (rather than an {@code Iterator}), the result 
   * can be wrapped in a {@link ReadOnceIterable}.
   */
  public static ReadOnlyIterator<Byte> asIterator(final InputStream in) {
    return new ReadOnlyIterator<Byte>() {
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
      
    };
  }
  
  /** Make an {@code Enumeration} based on the given {@code Iterator}.  For compatibility with legacy APIs. */
  public static <T> Enumeration<T> asEnumeration(final Iterator<? extends T> iter) {
    return new Enumeration<T>() {
      public boolean hasMoreElements() { return iter.hasNext(); }
      public T nextElement() { return iter.next(); }
    };
  }
  
  
  /** Create an {@link EmptyIterable}; equivalent to {@link #make()} */
  @SuppressWarnings("unchecked") public static <T> EmptyIterable<T> empty() {
    return (EmptyIterable<T>) EmptyIterable.INSTANCE;
  }
  
  /** Create a {@link SingletonIterable}; equivalent to {@link #make(Object)} */
  public static <T> SingletonIterable<T> singleton(T value) {
    return new SingletonIterable<T>(value);
  }
  
  /** Create a {@link ComposedIterable} with the given arguments */
  public static <T> ComposedIterable<T> compose(T first, Iterable<? extends T> rest) {
    return new ComposedIterable<T>(first, rest);
  }
    
  /** Create a {@link ComposedIterable} with the given arguments */
  public static <T> ComposedIterable<T> compose(Iterable<? extends T> rest, T last) {
    return new ComposedIterable<T>(rest, last);
  }
    
  /** Create a {@link ComposedIterable} with the given arguments */
  public static <T> ComposedIterable<T> compose(Iterable<? extends T> i1, Iterable<? extends T> i2) {
    return new ComposedIterable<T>(i1, i2);
  }
  
  /** Create a {@link SnapshotIterable} with the given iterable */
  public static <T> SnapshotIterable<T> snapshot(Iterable<? extends T> iter) {
    return new SnapshotIterable<T>(iter);
  }
  
  /** Create a {@link SnapshotIterable} with the given iterator */
  public static <T> SnapshotIterable<T> snapshot(Iterator<? extends T> iter) {
    return new SnapshotIterable<T>(iter);
  }
  
  /** Create an {@link ImmutableIterable} with the given iterable */
  public static <T> ImmutableIterable<T> immutable(Iterable<? extends T> iter) {
    return new ImmutableIterable<T>(iter);
  }
    
  /**
   * <p>Create a SizedIterable based on the given values or array.  Equivalent to {@link #asIterable(Object[])}.</p>
   * 
   * <p>When used as a varargs method, an unchecked warning will occur at the call site when {@code T} is a 
   * non-reifiable (generic or variable) type.  As a workaround, the function is overloaded to take a range
   * of fixed numbers of arguments, up to a practical limit.  Above that limit, this varargs version is matched.</p>
   */
  public static <T> SizedIterable<T> make(T... values) {
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make() {
    @SuppressWarnings("unchecked") EmptyIterable<T> result = (EmptyIterable<T>) EmptyIterable.INSTANCE;
    return result;
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1) {
    return new SingletonIterable<T>(v1);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7, T v8) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7, v8 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7, T v8, T v9) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7, v8, v9 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7, T v8, T v9, T v10) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7, v8, v9, v10 };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static <T> SizedIterable<T> asIterable(T[] array) {
    return new ObjectArrayWrapper<T>(array);
  }
  
  private static final class ObjectArrayWrapper<T> extends AbstractIterable<T> 
    implements SizedIterable<T>, Serializable {
    private final T[] _array;
    public ObjectArrayWrapper(T[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<T> iterator() {
      return new IndexedIterator<T>() {
        protected int size() { return _array.length; }
        protected T get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Boolean> asIterable(boolean[] values) {
    return new BooleanArrayWrapper(values);
  }
  
  private static final class BooleanArrayWrapper extends AbstractIterable<Boolean> 
    implements SizedIterable<Boolean>, Serializable {
    private final boolean[] _array;
    public BooleanArrayWrapper(boolean[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Boolean> iterator() {
      return new IndexedIterator<Boolean>() {
        protected int size() { return _array.length; }
        protected Boolean get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Character> asIterable(char[] values) {
    return new CharArrayWrapper(values);
  }
  
  private static final class CharArrayWrapper extends AbstractIterable<Character> 
    implements SizedIterable<Character>, Serializable {
    private final char[] _array;
    public CharArrayWrapper(char[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Character> iterator() {
      return new IndexedIterator<Character>() {
        protected int size() { return _array.length; }
        protected Character get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Byte> asIterable(byte[] values) {
    return new ByteArrayWrapper(values);
  }
  
  private static final class ByteArrayWrapper extends AbstractIterable<Byte> 
    implements SizedIterable<Byte>, Serializable {
    private final byte[] _array;
    public ByteArrayWrapper(byte[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Byte> iterator() {
      return new IndexedIterator<Byte>() {
        protected int size() { return _array.length; }
        protected Byte get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Short> asIterable(short[] values) {
    return new ShortArrayWrapper(values);
  }
  
  private static final class ShortArrayWrapper extends AbstractIterable<Short> 
    implements SizedIterable<Short>, Serializable {
    private final short[] _array;
    public ShortArrayWrapper(short[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Short> iterator() {
      return new IndexedIterator<Short>() {
        protected int size() { return _array.length; }
        protected Short get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Integer> asIterable(int[] values) {
    return new IntArrayWrapper(values);
  }
  
  private static final class IntArrayWrapper extends AbstractIterable<Integer> 
    implements SizedIterable<Integer>, Serializable {
    private final int[] _array;
    public IntArrayWrapper(int[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Integer> iterator() {
      return new IndexedIterator<Integer>() {
        protected int size() { return _array.length; }
        protected Integer get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Long> asIterable(long[] values) {
    return new LongArrayWrapper(values);
  }
  
  private static final class LongArrayWrapper extends AbstractIterable<Long> 
    implements SizedIterable<Long>, Serializable {
    private final long[] _array;
    public LongArrayWrapper(long[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Long> iterator() {
      return new IndexedIterator<Long>() {
        protected int size() { return _array.length; }
        protected Long get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Float> asIterable(float[] values) {
    return new FloatArrayWrapper(values);
  }
  
  private static final class FloatArrayWrapper extends AbstractIterable<Float> 
    implements SizedIterable<Float>, Serializable {
    private final float[] _array;
    public FloatArrayWrapper(float[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Float> iterator() {
      return new IndexedIterator<Float>() {
        protected int size() { return _array.length; }
        protected Float get(int i) { return _array[i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Double> asIterable(double[] values) {
    return new DoubleArrayWrapper(values);
  }
  
  private static final class DoubleArrayWrapper extends AbstractIterable<Double> 
    implements SizedIterable<Double>, Serializable {
    private final double[] _array;
    public DoubleArrayWrapper(double[] array) { _array = array; }
    public int size() { return _array.length; }
    public int size(int bound) { return _array.length <= bound ? _array.length : bound; }
    public boolean isFixed() { return true; }
    public Iterator<Double> iterator() {
      return new IndexedIterator<Double>() {
        protected int size() { return _array.length; }
        protected Double get(int i) { return _array[i]; }
      };
    }
  }
  
  /** 
   * @return  An iterable that traverses the given array
   * @throws IllegalArgumentException  If {@code array} is not an array
   */
  public static SizedIterable<?> arrayAsIterable(Object array) {
    if (array instanceof Object[]) { return new ObjectArrayWrapper<Object>((Object[]) array); }
    else if (array instanceof int[]) { return new IntArrayWrapper((int[]) array); }
    else if (array instanceof char[]) { return new CharArrayWrapper((char[]) array); }
    else if (array instanceof byte[]) { return new ByteArrayWrapper((byte[]) array); }
    else if (array instanceof double[]) { return new DoubleArrayWrapper((double[]) array); }
    else if (array instanceof boolean[]) { return new BooleanArrayWrapper((boolean[]) array); }
    else if (array instanceof short[]) { return new ShortArrayWrapper((short[]) array); }
    else if (array instanceof long[]) { return new LongArrayWrapper((long[]) array); }
    else if (array instanceof float[]) { return new FloatArrayWrapper((float[]) array); }
    else { throw new IllegalArgumentException("Non-array argument"); }
  }
  
  
  /** 
   * Wrap the given {@code Collection} in a {@code SizedIterable}.  Subsequent changes made 
   * to the collection will be reflected in the result (if this is not the desired behavior,
   * {@link #snapshot(Iterable)} can be used instead).
   */
  public static <T> SizedIterable<T> asIterable(Collection<T> coll) {
    return new CollectionWrapper<T>(coll);
  }
  
  private static final class CollectionWrapper<T> extends AbstractIterable<T> 
    implements SizedIterable<T>, Serializable {
    private final Collection<T> _c;
    public CollectionWrapper(Collection<T> c) { _c = c; }
    public Iterator<T> iterator() { return _c.iterator(); }
    public int size() { return _c.size(); }
    public int size(int bound) { int result = _c.size(); return result <= bound ? result : bound; }
    public boolean isFixed() { return isFixedCollection(_c); }
  }
  

  /** Create an iterable that wraps the given {@code CharSequence} */
  public static SizedIterable<Character> asIterable(CharSequence sequence) {
    return new CharSequenceWrapper(sequence, false);
  }
  
  /** 
   * Create an iterable that wraps the given string.  This is similar to {@link #asIterable(CharSequence)}, 
   * but takes advantage of the fact that {@code String}s are immutable.
   */
  public static SizedIterable<Character> asIterable(final String sequence) {
    return new CharSequenceWrapper(sequence, true);
  }
  
  private static final class CharSequenceWrapper extends AbstractIterable<Character> 
    implements SizedIterable<Character>, Serializable {
    private final CharSequence _s;
    private final boolean _fixed;
    public CharSequenceWrapper(CharSequence s, boolean fixed) { _s = s; _fixed = fixed; }
    public int size() { return _s.length(); }
    public int size(int bound) { int result = _s.length(); return result <= bound ? result : bound; }
    public boolean isFixed() { return _fixed; }
    public Iterator<Character> iterator() {
      return new IndexedIterator<Character>() {
        protected int size() { return _s.length(); }
        protected Character get(int i) { return _s.charAt(i); }
      };
    }
  }
  
  /**
   * Make a {@code List} with the given elements.  If the input <em>is</em> a {@code List},
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
   * Make an {@code ArrayList} with the given elements.  If the input <em>is</em> an {@code ArrayList},
   * casts it as such; otherwise, creates a new list.  In the second case, changes made to
   * one will necessarily not be reflected in the other.
   */
  public static <T> ArrayList<T> asArrayList(Iterable<T> iter) {
    if (iter instanceof ArrayList<?>) { return (ArrayList<T>) iter; }
    else if (iter instanceof Collection<?>) { return new ArrayList<T>((Collection<T>) iter); }
    else {
      ArrayList<T> result = new ArrayList<T>();
      for (T e : iter) { result.add(e); }
      return result;
    }
  }
  
  /**
   * Make a {@link LinkedList} with the given elements.  If the input <em>is</em> a {@code List},
   * casts it as such; otherwise, creates a new list.  In the second case, changes made to
   * one will necessarily not be reflected in the other.
   */
  public static <T> LinkedList<T> asLinkedList(Iterable<T> iter) {
    if (iter instanceof LinkedList<?>) { return (LinkedList<T>) iter; }
    else if (iter instanceof Collection<?>) { return new LinkedList<T>((Collection<T>) iter); }
    else {
      LinkedList<T> result = new LinkedList<T>();
      for (T e : iter) { result.add(e); }
      return result;
    }
  }
  
  /**
   * Make a {@link ConsList} with the given elements.  If the input <em>is</em> a {@code ConsList},
   * casts it as such; otherwise, creates a new list.  Of course, since ConsLists are immutable,
   * subsequent changes made to {@code iter} will not be reflected in the result.
   */
  public static <T> ConsList<T> asConsList(Iterable<T> iter) {
    if (iter instanceof ConsList<?>) { return (ConsList<T>) iter; }
    else {
      ConsList<T> result = ConsList.empty();
      for (T elt : reverse(iter)) { result = ConsList.cons(elt, result); }
      return result;
    }
  }
  
  /**
   * Make an array with the given elements.  Takes advantage of the (potentially optimized)
   * {@link Collection#toArray} method where possible; otherwise, just iterates through
   * {@code iter} to fill the array.
   */
  public static <T> T[] asArray(Iterable<? extends T> iter, Class<T> type) {
    // Cast is safe because the result has the type of the variable "type"
    @SuppressWarnings("unchecked") T[] result = (T[]) Array.newInstance(type, sizeOf(iter));
    if (iter instanceof Collection<?>) {
      result = ((Collection<? extends T>) iter).toArray(result);
    }
    else {
      int i = 0;
      for (T t : iter) { result[i] = t; i++; }
    }
    return result;
  }
  
  
  /**
   * Access the first value in the given iterable.
   * @throws NoSuchElementException  If the iterable is empty
   */
  public static <T> T first(Iterable<? extends T> iter) {
    return iter.iterator().next();
  }
  
  /** Produce an iterable that skips the first element of {@code iter} (if it exists) */
  public static <T> SkipFirstIterable<T> skipFirst(Iterable<T> iter) {
    return new SkipFirstIterable<T>(iter);
  }
  
  /**
   * Access the last value in the given iterable.  With the exception of some special cases
   * ({@link OptimizedLastIterable}s, {@link SortedSet}s, or {@link List}s), this operation takes time on 
   * the order of the length of the list.
   * @throws NoSuchElementException  If the iterable is empty
   */
  public static <T> T last(Iterable<? extends T> iter) {
    if (iter instanceof OptimizedLastIterable<?>) {
      return ((OptimizedLastIterable<? extends T>) iter).last();
    }
    else if (iter instanceof List<?>) {
      List<? extends T> l = (List<? extends T>) iter;
      int size = l.size();
      if (size == 0) { throw new NoSuchElementException(); }
      return l.get(size - 1);
    }
    else if (iter instanceof SortedSet<?>) {
      SortedSet<? extends T> s = (SortedSet<? extends T>) iter;
      return s.last();
    }
    else {
      Iterator<? extends T> i = iter.iterator();
      T result = i.next();
      while (i.hasNext()) { result = i.next(); }
      return result;
    }
  }
  
  /** Produce an iterable that skips the last element of {@code iter} (if it exists) */
  public static <T> SkipLastIterable<T> skipLast(Iterable<? extends T> iter) {
    return new SkipLastIterable<T>(iter);
  }
  
  /**
   * Produce a reverse-order iterable over the given elements.  Subsequent changes to {@code iter} will not
   * be reflected in the result.  Runs in linear time.
   */
  public static <T> SizedIterable<T> reverse(Iterable<? extends T> iter) {
    ConsList<T> result = ConsList.empty();
    for (T elt : iter) { result = ConsList.cons(elt, result); }
    return result;
  }
  
  /**
   * Produce a shuffled iterable over the given elements.  Subsequent changes to {@code iter} will not
   * be reflected in the result.  Runs in linear time.
   */
  public static <T> SizedIterable<T> shuffle(Iterable<T> iter) {
    ArrayList<T> result = asArrayList(iter);
    Collections.shuffle(result);
    return asIterable(result);
  }
  
  /**
   * Produce a shuffled iterable over the given elements, using the specified random number generator.  
   * Subsequent changes to {@code iter} will not be reflected in the result.  Runs in linear time.
   */
  public static <T> SizedIterable<T> shuffle(Iterable<T> iter, Random random) {
    ArrayList<T> result = asArrayList(iter);
    Collections.shuffle(result, random);
    return asIterable(result);
  }
  
  /**
   * Produce a sorted iterable over the given elements.  Subsequent changes to {@code iter} will not
   * be reflected in the result.  Runs in n log n time.
   */
  public static <T extends Comparable<? super T>> SizedIterable<T> sort(Iterable<T> iter) {
    ArrayList<T> result = asArrayList(iter);
    Collections.sort(result);
    return asIterable(result);
  }
  
  /**
   * Produce a sorted iterable over the given elements, using the specified comparator.  
   * Subsequent changes to {@code iter} will not be reflected in the result.  Runs in n log n time.
   */
  public static <T> SizedIterable<T> sort(Iterable<T> iter, Comparator<? super T> comp) {
    ArrayList<T> result = asArrayList(iter);
    Collections.sort(result, comp);
    return asIterable(result);
  }
  
  
  /**
   * Split the given iterable into two at the given index.  The first {@code index} values in
   * {@code iter} will belong to the first half; the rest will belong to the second half.
   * Where there are less than {@code index} values in {@code iter}, the first half will contain
   * them all and the second half will be empty.  Note that the result is a snapshot &mdash; later
   * modifications to {@code iter} will not be reflected.
   */
  public static <T> Pair<SizedIterable<T>, SizedIterable<T>> split(Iterable<? extends T> iter, int index) {
    Iterator<? extends T> iterator = iter.iterator();
    @SuppressWarnings("unchecked") SizedIterable<T> left = (EmptyIterable<T>) EmptyIterable.INSTANCE;
    for (int i = 0; i < index && iterator.hasNext(); i++) {
      left = new ComposedIterable<T>(left, iterator.next());
    }
    return new Pair<SizedIterable<T>, SizedIterable<T>>(left, new SnapshotIterable<T>(iterator));
  }
  
  /**
   * Collapse a list of lists into a single list.  Subsequent changes to {@code iter} or its sublists
   * will be reflected in the result.
   */
  public static <T> SizedIterable<T> collapse(Iterable<? extends Iterable<? extends T>> iters) {
    return new CollapsedIterable<T>(iters);
  }
  
  /** Produce an iterable that only contains values from the given iterable that satisfy a predicate. */
  public static <T> FilteredIterable<T> filter(Iterable<? extends T> iter, Predicate<? super T> pred) {
    return new FilteredIterable<T>(iter, pred);
  }
  
  /** Produce an iterable that only contains values from the given iterable that satisfy a predicate. */
  public static <T> SnapshotIterable<T> filterSnapshot(Iterable<? extends T> iter, Predicate<? super T> pred) {
    return new SnapshotIterable<T>(new FilteredIterable<T>(iter, pred));
  }
  
  /** 
   * Check whether the given predicate holds for all values in {@code iter}.  Computation halts immediately where the 
   * predicate fails.
   */
  public static <T> boolean and(Iterable<? extends T> iter, Predicate<? super T> pred) {
    for (T elt : iter) { if (!pred.value(elt)) { return false; } }
    return true;
  }
  
  /** 
   * Check whether the given predicate holds for some value in {@code iter}.  Computation halts immediately where the 
   * predicate succeeds.
   */
  public static <T> boolean or(Iterable<? extends T> iter, Predicate<? super T> pred) {
    for (T elt : iter) { if (pred.value(elt)) { return true; } }
    return false;
  }
  
  /** 
   * Check whether the given predicate holds for all corresponding values in {@code iter1} and {@code iter2}.  The
   * iterables are assumed to have the same length; computation halts immediately where the predicate fails.
   */
  public static <T1, T2> boolean and(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                     Predicate2<? super T1, ? super T2> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    while (i1.hasNext()) { if (!pred.value(i1.next(), i2.next())) { return false; } }
    return true;
  }
  
  /** 
   * Check whether the given predicate holds for some corresponding values in {@code iter1} and {@code iter2}.  The
   * iterables are assumed to have the same length; computation halts immediately where the predicate succeeds.
   */
  public static <T1, T2> boolean or(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                    Predicate2<? super T1, ? super T2> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    while (i1.hasNext()) { if (pred.value(i1.next(), i2.next())) { return true; } }
    return false;
  }
  
  /** 
   * Check whether the given predicate holds for all corresponding values in the given iterables.  The iterables
   * are assumed to all have the same length; computation halts immediately where the predicate fails.
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
   * Check whether the given predicate holds for some corresponding values in the given iterables.  The iterables
   * are assumed to all have the same length; computation halts immediately where the predicate fails.
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
   * Check whether the given predicate holds for all corresponding values in the given iterables.  The iterables
   * are assumed to all have the same length; computation halts immediately where the predicate fails.
   */
  public static <T1, T2, T3, T4> boolean and(Iterable<? extends T1> iter1, 
                                             Iterable<? extends T2> iter2,
                                             Iterable<? extends T3> iter3,
                                             Iterable<? extends T4> iter4,
                                             Predicate4<? super T1, ? super T2, ? super T3, ? super T4> pred) {
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
   * Check whether the given predicate holds for some corresponding values in the given iterables.  The iterables
   * are assumed to all have the same length; computation halts immediately where the predicate fails.
   */
  public static <T1, T2, T3, T4> boolean or(Iterable<? extends T1> iter1, 
                                            Iterable<? extends T2> iter2,
                                            Iterable<? extends T3> iter3,
                                            Iterable<? extends T4> iter4,
                                            Predicate4<? super T1, ? super T2, ? super T3, ? super T4> pred) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    Iterator<? extends T3> i3 = iter3.iterator();
    Iterator<? extends T4> i4 = iter4.iterator();
    while (i1.hasNext()) { 
      if (pred.value(i1.next(), i2.next(), i3.next(), i4.next())) { return true; }
    }
    return false;
  }
  
  /** Create a {@code MappedIterable} with the given arguments */
  public static <S, T> MappedIterable<S, T> map(Iterable<? extends S> source, Lambda<? super S, ? extends T> map) {
    return new MappedIterable<S, T>(source, map);
  }
  
  /**
   * Create a {@link MappedIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the mapping.
   */
  public static <S, T> SnapshotIterable<T> mapSnapshot(Iterable<? extends S> source, Lambda<? super S, ? extends T> map) {
    return new SnapshotIterable<T>(new MappedIterable<S, T>(source, map));
  }
  
  /** Lazily create an iterable containing the values of the given thunks. */
  public static <R> Iterable<R> valuesOf(Iterable<? extends Thunk<? extends R>> iter) {
    @SuppressWarnings("unchecked") ThunkValue<R> l = (ThunkValue<R>) ThunkValue.INSTANCE;
    return new MappedIterable<Thunk<? extends R>, R>(iter, l);
  }
  
  private static final class ThunkValue<R> implements Lambda<Thunk<? extends R>, R>, Serializable {
    public static final ThunkValue<Void> INSTANCE = new ThunkValue<Void>();
    private ThunkValue() {}
    public R value(Thunk<? extends R> t) { return t.value(); }
  }
    
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T, R> Iterable<R> valuesOf(Iterable<? extends Lambda<? super T, ? extends R>> iter, T arg) {
    return new MappedIterable<Lambda<? super T, ? extends R>, R>(iter, new LambdaValue<T, R>(arg));
  }
  
  private static final class LambdaValue<T, R> implements Lambda<Lambda<? super T, ? extends R>, R>, Serializable {
    private final T _arg;
    public LambdaValue(T arg) { _arg = arg; }
    public R value(Lambda<? super T, ? extends R> l) { return l.value(_arg); }
  }
    
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T1, T2, R> Iterable<R> 
    valuesOf(Iterable<? extends Lambda2<? super T1, ? super T2, ? extends R>> iter, T1 arg1, T2 arg2) {
    return new MappedIterable<Lambda2<? super T1, ? super T2, ? extends R>, R>
                 (iter, new Lambda2Value<T1, T2, R>(arg1, arg2));
  }
  
  private static final class Lambda2Value<T1, T2, R> 
    implements Lambda<Lambda2<? super T1, ? super T2, ? extends R>, R>, Serializable {
    private final T1 _arg1;
    private final T2 _arg2;
    public Lambda2Value(T1 arg1, T2 arg2) { _arg1 = arg1; _arg2 = arg2; }
    public R value(Lambda2<? super T1, ? super T2, ? extends R> l) { return l.value(_arg1, _arg2); }
  }
    
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T1, T2, T3, R> Iterable<R>
    valuesOf(Iterable<? extends Lambda3<? super T1, ? super T2, ? super T3, ? extends R>> iter, 
             T1 arg1, T2 arg2, T3 arg3) {
    return new MappedIterable<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R>
                 (iter, new Lambda3Value<T1, T2, T3, R>(arg1, arg2, arg3));
  }
  
  private static final class Lambda3Value<T1, T2, T3, R> 
    implements Lambda<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R>, Serializable {
    private final T1 _arg1;
    private final T2 _arg2;
    private final T3 _arg3;
    public Lambda3Value(T1 arg1, T2 arg2, T3 arg3) { _arg1 = arg1; _arg2 = arg2; _arg3 = arg3; }
    public R value(Lambda3<? super T1, ? super T2, ? super T3, ? extends R> l) {
      return l.value(_arg1, _arg2, _arg3);
    }
  }
    
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T1, T2, T3, T4, R> Iterable<R>
    valuesOf(Iterable<? extends Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>> iter, 
             T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
    return new MappedIterable<Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>, R>
                 (iter, new Lambda4Value<T1, T2, T3, T4, R>(arg1, arg2, arg3, arg4));
  }
  
  private static final class Lambda4Value<T1, T2, T3, T4, R> 
    implements Lambda<Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R>, R>, Serializable {
    private final T1 _arg1;
    private final T2 _arg2;
    private final T3 _arg3;
    private final T4 _arg4;
    public Lambda4Value(T1 arg1, T2 arg2, T3 arg3, T4 arg4) {
      _arg1 = arg1; _arg2 = arg2; _arg3 = arg3; _arg4 = arg4;
    }
    public R value(Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> l) {
      return l.value(_arg1, _arg2, _arg3, _arg4);
    }
  }

  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> Iterable<T> pairFirsts(Iterable<? extends Pair<? extends T, ?>> iter) {
    @SuppressWarnings("unchecked") PairFirst<T> getter = (PairFirst<T>) PairFirst.INSTANCE;
    return new MappedIterable<Pair<? extends T, ?>, T>(iter, getter);
  }
  
  private static final class PairFirst<T> implements Lambda<Pair<? extends T, ?>, T>, Serializable {
    public static final PairFirst<Void> INSTANCE = new PairFirst<Void>();
    private PairFirst() {}
    public T value(Pair<? extends T, ?> arg) { return arg.first(); }
  }
      
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> Iterable<T> pairSeconds(Iterable<? extends Pair<?, ? extends T>> iter) {
    @SuppressWarnings("unchecked") PairSecond<T> getter = (PairSecond<T>) PairSecond.INSTANCE;
    return new MappedIterable<Pair<?, ? extends T>, T>(iter, getter);
  }
  
  private static final class PairSecond<T> implements Lambda<Pair<?, ? extends T>, T>, Serializable {
    public static final PairSecond<Object> INSTANCE = new PairSecond<Object>();
    private PairSecond() {}
    public T value(Pair<?, ? extends T> arg) { return arg.second(); }
  }
      
  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> Iterable<T> tripleFirsts(Iterable<? extends Triple<? extends T, ?, ?>> iter) {
    @SuppressWarnings("unchecked") TripleFirst<T> getter = (TripleFirst<T>) TripleFirst.INSTANCE;
    return new MappedIterable<Triple<? extends T, ?, ?>, T>(iter, getter);
  }
  
  private static final class TripleFirst<T> implements Lambda<Triple<? extends T, ?, ?>, T>, Serializable {
    public static final TripleFirst<Object> INSTANCE = new TripleFirst<Object>();
    private TripleFirst() {}
    public T value(Triple<? extends T, ?, ?> arg) { return arg.first(); }
  }
      
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> Iterable<T> tripleSeconds(Iterable<? extends Triple<?, ? extends T, ?>> iter) {
    @SuppressWarnings("unchecked") TripleSecond<T> getter = (TripleSecond<T>) TripleSecond.INSTANCE;
    return new MappedIterable<Triple<?, ? extends T, ?>, T>(iter, getter);
  }
  
  private static final class TripleSecond<T> implements Lambda<Triple<?, ? extends T, ?>, T>, Serializable {
    public static final TripleSecond<Object> INSTANCE = new TripleSecond<Object>();
    private TripleSecond() {}
    public T value(Triple<?, ? extends T, ?> arg) { return arg.second(); }
  }
      
  /** Lazily create an iterable containing the third values of the given tuples. */
  public static <T> Iterable<T> tripleThirds(Iterable<? extends Triple<?, ?, ? extends T>> iter) {
    @SuppressWarnings("unchecked") TripleThird<T> getter = (TripleThird<T>) TripleThird.INSTANCE;
    return new MappedIterable<Triple<?, ?, ? extends T>, T>(iter, getter);
  }
  
  private static final class TripleThird<T> implements Lambda<Triple<?, ?, ? extends T>, T>, Serializable {
    public static final TripleThird<Object> INSTANCE = new TripleThird<Object>();
    private TripleThird() {}
    public T value(Triple<?, ?, ? extends T> arg) { return arg.third(); }
  }
      
  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> Iterable<T> quadFirsts(Iterable<? extends Quad<? extends T, ?, ?, ?>> iter) {
    @SuppressWarnings("unchecked") QuadFirst<T> getter = (QuadFirst<T>) QuadFirst.INSTANCE;
    return new MappedIterable<Quad<? extends T, ?, ?, ?>, T>(iter, getter);
  }
  
  private static final class QuadFirst<T> implements Lambda<Quad<? extends T, ?, ?, ?>, T>, Serializable {
    public static final QuadFirst<Object> INSTANCE = new QuadFirst<Object>();
    private QuadFirst() {}
    public T value(Quad<? extends T, ?, ?, ?> arg) { return arg.first(); }
  }
      
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> Iterable<T> quadSeconds(Iterable<? extends Quad<?, ? extends T, ?, ?>> iter) {
    @SuppressWarnings("unchecked") QuadSecond<T> getter = (QuadSecond<T>) QuadSecond.INSTANCE;
    return new MappedIterable<Quad<?, ? extends T, ?, ?>, T>(iter, getter);
  }
  
  private static final class QuadSecond<T> implements Lambda<Quad<?, ? extends T, ?, ?>, T>, Serializable {
    public static final QuadSecond<Object> INSTANCE = new QuadSecond<Object>();
    private QuadSecond() {}
    public T value(Quad<?, ? extends T, ?, ?> arg) { return arg.second(); }
  }
      
  /** Lazily create an iterable containing the third values of the given tuples. */
  public static <T> Iterable<T> quadThirds(Iterable<? extends Quad<?, ?, ? extends T, ?>> iter) {
    @SuppressWarnings("unchecked") QuadThird<T> getter = (QuadThird<T>) QuadThird.INSTANCE;
    return new MappedIterable<Quad<?, ?, ? extends T, ?>, T>(iter, getter);
  }
  
  private static final class QuadThird<T> implements Lambda<Quad<?, ?, ? extends T, ?>, T>, Serializable {
    public static final QuadThird<Object> INSTANCE = new QuadThird<Object>();
    private QuadThird() {}
    public T value(Quad<?, ?, ? extends T, ?> arg) { return arg.third(); }
  }
      
  /** Lazily create an iterable containing the fourth values of the given tuples. */
  public static <T> Iterable<T> quadFourths(Iterable<? extends Quad<?, ?, ?, ? extends T>> iter) {
    @SuppressWarnings("unchecked") QuadFourth<T> getter = (QuadFourth<T>) QuadFourth.INSTANCE;
    return new MappedIterable<Quad<?, ?, ?, ? extends T>, T>(iter, getter);
  }
  
  private static final class QuadFourth<T> implements Lambda<Quad<?, ?, ?, ? extends T>, T>, Serializable {
    public static final QuadFourth<Object> INSTANCE = new QuadFourth<Object>();
    private QuadFourth() {}
    public T value(Quad<?, ?, ?, ? extends T> arg) { return arg.fourth(); }
  }
      
  
  /** 
   * Lazily create an iterable of {@code Pair}s of corresponding values from the given iterables (assumed to always 
   * have the same length).  Useful for simultaneous iteration of multiple lists in a {@code for} loop.
   */
  public static <T1, T2> Iterable<Pair<T1, T2>> zip(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2) {
    return new BinaryMappedIterable<T1, T2, Pair<T1, T2>>(iter1, iter2, Pair.<T1, T2>factory());
  }
    
  /** 
   * Lazily create an iterable of {@code Triple}s of corresponding values from the given iterables (assumed to  
   * always have the same length).  Useful for simultaneous iteration of multiple lists in a {@code for} loop.
   */
  public static <T1, T2, T3> Iterable<Triple<T1, T2, T3>> zip(Iterable<? extends T1> iter1, 
                                                              Iterable<? extends T2> iter2,
                                                              Iterable<? extends T3> iter3) {
    return new TernaryMappedIterable<T1, T2, T3, Triple<T1, T2, T3>>(iter1, iter2, iter3, Triple.<T1, T2, T3>factory());
  }
    
  /** 
   * Lazily create an iterable of {@code Quad}s of corresponding values from the given iterables (assumed to  
   * always have the same length).  Useful for simultaneous iteration of multiple lists in a {@code for} loop.
   */
  public static <T1, T2, T3, T4> Iterable<Quad<T1, T2, T3, T4>> zip(Iterable<? extends T1> iter1, 
                                                                    Iterable<? extends T2> iter2,
                                                                    Iterable<? extends T3> iter3,
                                                                    Iterable<? extends T4> iter4) {
    return new QuaternaryMappedIterable<T1, T2, T3, T4, Quad<T1, T2, T3, T4>>(iter1, iter2, iter3, iter4, 
                                                                              Quad.<T1, T2, T3, T4>factory());
  }
  
}
