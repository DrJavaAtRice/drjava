/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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
  
  /** Return {@code true} iff the given iterable is known to have an infinite size. */
  public static boolean isInfinite(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).isInfinite(); }
    else if (iter instanceof FilteredIterable<?>) { return ((FilteredIterable<?>) iter).isInfinite(); }
    else { return false; }
  }
  
  /**
   * Return {@code true} iff the given iterable is known to have a fixed size.  Infinite iterables are considered
   * fixed if they will never become finite.
   */
  public static boolean isFixed(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).isFixed(); }
    else if (iter instanceof Collection<?>) { return isFixedCollection((Collection<?>) iter); }
    else { return false; }
  }
  
  /** Return {@code true} iff the given collection is known to have a fixed size. */
  private static boolean isFixedCollection(Collection<?> iter) {
    return (iter == Collections.EMPTY_SET) || (iter == Collections.EMPTY_LIST);
  }
  
  /** 
   * Generate a string representation of the given iterable, matching the {@link Collection}
   * conventions (results like {@code "[foo, bar, baz]"}).  Invokes 
   * {@link RecurUtil#safeToString(Object)} on each element.  If the iterable is known to be
   * infinite ({@link #isInfinite}), the string contains a few elements followed by {@code "..."}.
   */
  public static String toString(Iterable<?> iter) {
    return toString(iter, "[", ", ", "]");
  }
  
  /** 
   * Generate a string representation of the given iterable where each element is listed on a
   * separate line.  Invokes {@link RecurUtil#safeToString(Object)} on each element.  If the iterable 
   * is known to be infinite ({@link #isInfinite}), the string contains a few elements followed by 
   * {@code "..."}.
   */
  public static String multilineToString(Iterable<?> iter) {
    return toString(iter, "", TextUtil.NEWLINE, "");
  }
  
  /** 
   * Generate a string representation of the given iterable beginning with {@code prefix}, ending with
   * {@code suffix}, and delimited by {@code delimiter}.  Invokes {@link RecurUtil#safeToString(Object)} 
   * on each element.  If the iterable is known to be infinite ({@link #isInfinite}), the string contains 
   * a few elements followed by {@code "..."}.
   */
  public static String toString(Iterable<?> iter, String prefix, String delimiter, String suffix) {
    if (isInfinite(iter)) { iter = compose(new TruncatedIterable<Object>(iter, 8), "..."); }
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
   * Return {@code true} iff the lists are identical (according to {@code ==}), or they
   * have the same size (according to {@link #sizeOf}) and each corresponding element is equal 
   * (according to {@link LambdaUtil#EQUAL}).  Assumes that at least one of the iterables is finite.
   */
  public static boolean isEqual(Iterable<?> iter1, Iterable<?> iter2) {
    if (iter1 == iter2) { return true; }
    else if (sizeOf(iter1) == sizeOf(iter2)) { return and(iter1, iter2, LambdaUtil.EQUAL); }
    else { return false; }
  }
  
  /**
   * Return a hash code computed by xoring shifted copies of each element's hash code;
   * the result is consistent with {@link #isEqual}, but may not be consistent with
   * the input's {@code equals} and {@code hashCode} methods; invokes 
   * {@code RecurUtil#safeHashCode(Object)} on each element. Assumes the iterable is finite.
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
  
  
  /** Create an {@link EmptyIterable}; equivalent to {@link #make()}. */
  @SuppressWarnings("unchecked") public static <T> EmptyIterable<T> empty() {
    return (EmptyIterable<T>) EmptyIterable.INSTANCE;
  }
  
  /** Create a {@link SingletonIterable}; equivalent to {@link #make(Object)}. */
  public static <T> SingletonIterable<T> singleton(T value) {
    return new SingletonIterable<T>(value);
  }
  
  /** Create a {@link ComposedIterable} with the given arguments. */
  public static <T> ComposedIterable<T> compose(T first, Iterable<? extends T> rest) {
    return new ComposedIterable<T>(first, rest);
  }
    
  /** Produce a lambda that invokes {@link #compose(Object, Iterable)}. */
  @SuppressWarnings("unchecked") public static <T> Lambda2<T, Iterable<? extends T>, Iterable<T>> composeLeftLambda() {
    return (Lambda2<T, Iterable<? extends T>, Iterable<T>>) ComposeLeftLambda.INSTANCE;
  }
  
  private static class ComposeLeftLambda<T> implements Lambda2<T, Iterable<? extends T>, Iterable<T>>, Serializable {
    private static ComposeLeftLambda<Object> INSTANCE = new ComposeLeftLambda<Object>();
    private ComposeLeftLambda() {}
    public Iterable<T> value(T first, Iterable<? extends T> rest) {
      return new ComposedIterable<T>(first, rest);
    }
  }
  
  /** Create a {@link ComposedIterable} with the given arguments. */
  public static <T> ComposedIterable<T> compose(Iterable<? extends T> rest, T last) {
    return new ComposedIterable<T>(rest, last);
  }
    
  /** Produce a lambda that invokes {@link #compose(Iterable, Object)}. */
  @SuppressWarnings("unchecked") public static <T> Lambda2<Iterable<? extends T>, T, Iterable<T>> composeRightLambda() {
    return (Lambda2<Iterable<? extends T>, T, Iterable<T>>) ComposeRightLambda.INSTANCE;
  }
  
  private static class ComposeRightLambda<T> implements Lambda2<Iterable<? extends T>, T, Iterable<T>>, Serializable {
    private static ComposeRightLambda<Object> INSTANCE = new ComposeRightLambda<Object>();
    private ComposeRightLambda() {}
    public Iterable<T> value(Iterable<? extends T> rest, T last) {
      return new ComposedIterable<T>(rest, last);
    }
  }
  
  /** Create a {@link ComposedIterable} with the given arguments. */
  public static <T> ComposedIterable<T> compose(Iterable<? extends T> i1, Iterable<? extends T> i2) {
    return new ComposedIterable<T>(i1, i2);
  }
  
  /** Produce a lambda that invokes {@link #compose(Object, Iterable)}. */
  @SuppressWarnings("unchecked") 
  public static <T> Lambda2<Iterable<? extends T>, Iterable<? extends T>, Iterable<T>> composeLambda() {
    return (Lambda2<Iterable<? extends T>, Iterable<? extends T>, Iterable<T>>) ComposeLambda.INSTANCE;
  }
  
  private static class ComposeLambda<T> 
    implements Lambda2<Iterable<? extends T>, Iterable<? extends T>, Iterable<T>>, Serializable {
    private static ComposeLambda<Object> INSTANCE = new ComposeLambda<Object>();
    private ComposeLambda() {}
    public Iterable<T> value(Iterable<? extends T> i1, Iterable<? extends T> i2) {
      return new ComposedIterable<T>(i1, i2);
    }
  }
  
  
  /** Create a {@link SnapshotIterable} with the given iterable. */
  public static <T> SnapshotIterable<T> snapshot(Iterable<? extends T> iter) {
    return new SnapshotIterable<T>(iter);
  }
  
  /** Create a {@link SnapshotIterable} with the given iterator. */
  public static <T> SnapshotIterable<T> snapshot(Iterator<? extends T> iter) {
    return new SnapshotIterable<T>(iter);
  }
  
  /** Create an {@link ImmutableIterable} with the given iterable. */
  public static <T> ImmutableIterable<T> immutable(Iterable<? extends T> iter) {
    return new ImmutableIterable<T>(iter);
  }
    
  /**
   * <p>Create a SizedIterable based on the given values or array; equivalent to {@link #asIterable(Object[])}.</p>
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
  
  /** Create an infinite sequence defined by an initial value and a successor function. */
  public static <T> SequenceIterable<T> infiniteSequence(T initial, Lambda<? super T, ? extends T> successor) {
    return new SequenceIterable<T>(initial, successor);
  }
  
  /** Create a finite sequence of the given size defined by an initial value and a successor function. */
  public static <T> FiniteSequenceIterable<T> finiteSequence(T initial, Lambda<? super T, ? extends T> successor, 
                                                             int size) {
    return new FiniteSequenceIterable<T>(initial, successor, size);
  }
  
  /** 
   * Create a simple sequence containing the numbers between {@code start} and {@code end}
   * (inclusive).  {@code start} may be less than <em>or</em> greater than {@code end} (or even
   * equal to it); the resulting iterator will increment or decrement as necessary.
   */
  public static FiniteSequenceIterable<Integer> integerSequence(int start, int end) {
    return FiniteSequenceIterable.makeIntegerSequence(start, end);
  }
  
  /** Create a sequence containing {@code copies} instances of the given value. */
  public static <T> FiniteSequenceIterable<T> copy(T value, int copies) {
    return FiniteSequenceIterable.makeCopies(value, copies);
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
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
    public boolean isInfinite() { return false; }
    public boolean isFixed() { return true; }
    public Iterator<Double> iterator() {
      return new IndexedIterator<Double>() {
        protected int size() { return _array.length; }
        protected Double get(int i) { return _array[i]; }
      };
    }
  }
  
  /** 
   * Returns an iterable that traverses the given array, which may contain primitives or references.
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
  public static <T> SizedIterable<T> asSizedIterable(Collection<T> coll) {
    return new CollectionWrapper<T>(coll);
  }
  
  private static final class CollectionWrapper<T> extends AbstractIterable<T> 
                                                  implements SizedIterable<T>, Serializable {
    private final Collection<T> _c;
    public CollectionWrapper(Collection<T> c) { _c = c; }
    public Iterator<T> iterator() { return _c.iterator(); }
    public int size() { return _c.size(); }
    public int size(int bound) { int result = _c.size(); return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean isFixed() { return isFixedCollection(_c); }
  }
  

  /** Create an iterable that wraps the given {@code CharSequence}. */
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
    public boolean isInfinite() { return false; }
    public boolean isFixed() { return _fixed; }
    public Iterator<Character> iterator() {
      return new IndexedIterator<Character>() {
        protected int size() { return _s.length(); }
        protected Character get(int i) { return _s.charAt(i); }
      };
    }
  }
  
  /** Produce an iterable of size 0 or 1 from an {@code Option}. */
  public static <T> SizedIterable<T> asIterable(Option<? extends T> option) {
    return option.apply(new OptionVisitor<T, SizedIterable<T>>() {
      public SizedIterable<T> forSome(T val) { return new SingletonIterable<T>(val); }
      @SuppressWarnings("unchecked")
      public SizedIterable<T> forNone() { return (EmptyIterable<T>) EmptyIterable.INSTANCE; }
    });
  }

  /** Produce an iterable of size 1 from a {@code Wrapper}. */
  public static <T> SizedIterable<T> asIterable(Wrapper<? extends T> tuple) {
    return new SingletonIterable<T>(tuple.value());
  }
  
  /** Produce an iterable of size 2 from a {@code Pair}. */
  public static <T> SizedIterable<T> asIterable(Pair<? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second() };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Produce an iterable of size 3 from a {@code Triple}. */
  public static <T> SizedIterable<T> asIterable(Triple<? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third() };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Produce an iterable of size 4 from a {@code Quad}. */
  public static <T> SizedIterable<T> asIterable(Quad<? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth() };
    return new ObjectArrayWrapper<T>(values);
  }

  /** Produce an iterable of size 5 from a {@code Quint}. */
  public static <T> SizedIterable<T>
    asIterable(Quint<? extends T, ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(), 
                                     tuple.fifth() };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Produce an iterable of size 6 from a {@code Sextet}. */
  public static <T> SizedIterable<T>
  asIterable(Sextet<? extends T, ? extends T, ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(),
                                     tuple.fifth(), tuple.sixth() };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Produce an iterable of size 7 from a {@code Septet}. */
  public static <T> SizedIterable<T>
  asIterable(Septet<? extends T, ? extends T, ? extends T, ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(),
                                     tuple.fifth(), tuple.sixth(), tuple.seventh() };
    return new ObjectArrayWrapper<T>(values);
  }
  
  /** Produce an iterable of size 8 from an {@code Octet}. */
  public static <T> SizedIterable<T> asIterable(Octet<? extends T, ? extends T, ? extends T, ? extends T,
                                                      ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(),
                                     tuple.fifth(), tuple.sixth(), tuple.seventh(), tuple.eighth() };
    return new ObjectArrayWrapper<T>(values);
  }
  
  
  /**
   * Make a {@code List} with the given elements.  If the input <em>is</em> a {@code List},
   * casts it as such; otherwise, creates a new list.  In the second case, changes made to
   * one will necessarily not be reflected in the other.  Assumes the iterable is finite.
   */
  public static <T> List<T> asList(Iterable<T> iter) {
    if (iter instanceof List<?>) { return (List<T>) iter; }
    else if (iter instanceof Collection<?>) { return new ArrayList<T>((Collection<T>) iter); }
    else {
      ArrayList<T> result = new ArrayList<T>(0); // minimize footprint of empty
      for (T e : iter) { result.add(e); }
      return result;
    }
  }
  
  /**
   * Make an {@code ArrayList} with the given elements.  If the input <em>is</em> an {@code ArrayList},
   * casts it as such; otherwise, creates a new list.  In the second case, changes made to
   * one will necessarily not be reflected in the other.  Assumes the iterable is finite.
   */
  public static <T> ArrayList<T> asArrayList(Iterable<T> iter) {
    if (iter instanceof ArrayList<?>) { return (ArrayList<T>) iter; }
    else if (iter instanceof Collection<?>) { return new ArrayList<T>((Collection<T>) iter); }
    else {
      ArrayList<T> result = new ArrayList<T>(0); // minimize footprint of empty
      for (T e : iter) { result.add(e); }
      return result;
    }
  }
  
  /**
   * Make a {@link LinkedList} with the given elements.  If the input <em>is</em> a {@code List},
   * casts it as such; otherwise, creates a new list.  In the second case, changes made to
   * one will necessarily not be reflected in the other.  Assumes the iterable is finite.
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
   * subsequent changes made to {@code iter} will not be reflected in the result.  Assumes the iterable
   * is finite.
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
   * {@code iter} to fill the array.  If the size of the iterable is larger than {@code Integer.MAX_VALUE}
   * or is infinite, it will be truncated to fit in an array.
   */
  public static <T> T[] asArray(Iterable<? extends T> iter, Class<T> type) {
    // Cast is safe because the result has the type of the variable "type"
    @SuppressWarnings("unchecked") T[] result = (T[]) Array.newInstance(type, sizeOf(iter));
    if (iter instanceof Collection<?>) {
      // javac 6 doesn't like this -- Collection<? extends T> </: Iterable<capture extends T>
      @SuppressWarnings("unchecked") T[] newResult = ((Collection<? extends T>) iter).toArray(result);
      result = newResult; // redeclared for the sake of @SuppressWarnings
    }
    else {
      int i = 0;
      for (T t : iter) { result[i] = t; i++; if (i < 0) break; }
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
   * the order of the length of the list.  Assumes the iterable is finite.
   * @throws NoSuchElementException  If the iterable is empty
   */
  public static <T> T last(Iterable<? extends T> iter) {
    if (iter instanceof OptimizedLastIterable<?>) {
      // javac 6 doesn't like this -- OptimizedLastIterable<? extends T> </: Iterable<capture extends T>
      @SuppressWarnings("unchecked") OptimizedLastIterable<? extends T> o = (OptimizedLastIterable<? extends T>) iter;
      return o.last();
    }
    else if (iter instanceof List<?>) {
      // javac 6 doesn't like this -- List<? extends T> </: Iterable<capture extends T>
      @SuppressWarnings("unchecked") List<? extends T> l = (List<? extends T>) iter;
      int size = l.size();
      if (size == 0) { throw new NoSuchElementException(); }
      return l.get(size - 1);
    }
    else if (iter instanceof SortedSet<?>) {
      // javac 6 doesn't like this -- SortedSet<? extends T> </: Iterable<capture extends T>
      @SuppressWarnings("unchecked") SortedSet<? extends T> s = (SortedSet<? extends T>) iter;
      return s.last();
    }
    else {
      Iterator<? extends T> i = iter.iterator();
      T result = i.next();
      while (i.hasNext()) { result = i.next(); }
      return result;
    }
  }
  
  /**
   * Produce an iterable that skips the last element of {@code iter} (if it exists).  Assumes the iterable
   * is finite.
   */
  public static <T> SkipLastIterable<T> skipLast(Iterable<? extends T> iter) {
    return new SkipLastIterable<T>(iter);
  }
  
  /**
   * Convert an iterable of 0 or 1 elements to an {@code Option}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Option<T> asOption(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size == 0) { return Option.none(); }
    else if (size == 1) { return Option.some(first(iter)); }
    else {
      throw new IllegalArgumentException("Iterable has more than 1 element: size == " + size);
    }
  }
  
  /**
   * Convert an iterable of 1 element to a {@code Wrapper}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Wrapper<T> asWrapper(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 1) {
      throw new IllegalArgumentException("Iterable does not have 1 element: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Wrapper<T>(i.next());
  }
  
  /**
   * Convert an iterable of 2 elements to a {@code Pair}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Pair<T, T> asPair(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 2) {
      throw new IllegalArgumentException("Iterable does not have 2 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Pair<T, T>(i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 3 elements to a {@code Triple}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Triple<T, T, T> asTriple(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 3) {
      throw new IllegalArgumentException("Iterable does not have 3 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Triple<T, T, T>(i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 4 elements to a {@code Quad}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Quad<T, T, T, T> asQuad(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 4) {
      throw new IllegalArgumentException("Iterable does not have 4 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Quad<T, T, T, T>(i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 5 elements to a {@code Quint}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Quint<T, T, T, T, T> asQuint(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 5) {
      throw new IllegalArgumentException("Iterable does not have 5 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Quint<T, T, T, T, T>(i.next(), i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 6 elements to a {@code Sextet}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Sextet<T, T, T, T, T, T> asSextet(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 6) {
      throw new IllegalArgumentException("Iterable does not have 6 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Sextet<T, T, T, T, T, T>(i.next(), i.next(), i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 7 elements to a {@code Septet}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Septet<T, T, T, T, T, T, T> asSeptet(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 7) {
      throw new IllegalArgumentException("Iterable does not have 7 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Septet<T, T, T, T, T, T, T>(i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 8 elements to an {@code Octet}.
   * @throws IllegalArgumentException  If the iterator is not of the appropriate size.
   */
  public static <T> Octet<T, T, T, T, T, T, T, T> asOctet(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 8) {
      throw new IllegalArgumentException("Iterable does not have 8 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Octet<T, T, T, T, T, T, T, T>(i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next(),
                                             i.next());
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
    return asSizedIterable(result);
  }
  
  /**
   * Produce a shuffled iterable over the given elements, using the specified random number generator.  
   * Subsequent changes to {@code iter} will not be reflected in the result.  Runs in linear time.
   */
  public static <T> SizedIterable<T> shuffle(Iterable<T> iter, Random random) {
    ArrayList<T> result = asArrayList(iter);
    Collections.shuffle(result, random);
    return asSizedIterable(result);
  }
  
  /**
   * Produce a sorted iterable over the given elements.  Subsequent changes to {@code iter} will not
   * be reflected in the result.  Runs in n log n time.
   */
  public static <T extends Comparable<? super T>> SizedIterable<T> sort(Iterable<T> iter) {
    ArrayList<T> result = asArrayList(iter);
    Collections.sort(result);
    return asSizedIterable(result);
  }
  
  /**
   * Produce a sorted iterable over the given elements, using the specified comparator.  
   * Subsequent changes to {@code iter} will not be reflected in the result.  Runs in n log n time.
   */
  public static <T> SizedIterable<T> sort(Iterable<T> iter, Comparator<? super T> comp) {
    ArrayList<T> result = asArrayList(iter);
    Collections.sort(result, comp);
    return asSizedIterable(result);
  }
  
  
  /**
   * Split the given iterable into two at the given index.  The first {@code index} values in
   * {@code iter} will belong to the first half; the rest will belong to the second half.
   * Where there are less than {@code index} values in {@code iter}, the first half will contain
   * them all and the second half will be empty.  Note that the result is a snapshot &mdash; later
   * modifications to {@code iter} will not be reflected.  Assumes the iterable is finite.
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
   * Truncate the given iterable.  The result will have size less than or equal to {@code size}.  Subsequent
   * changes to {@code iter} <em>will</em> be reflected in the result.
   */
  public static <T> TruncatedIterable<T> truncate(Iterable<? extends T> iter, int size) {
    return new TruncatedIterable<T>(iter, size);
  }
  
  /**
   * Collapse a list of lists into a single list.  Subsequent changes to {@code iter} or its sublists
   * will be reflected in the result.
   */
  public static <T> CollapsedIterable<T> collapse(Iterable<? extends Iterable<? extends T>> iters) {
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
   * Compute the left fold of the given list.  That is, for some combination function {@code #} (written here
   * with infix notation), compute {@code base # iter.next() # iter.next() # ...}.  Assumes the iterable is finite.
   */
  public static <T, R> R fold(Iterable<? extends T> iter, R base,
                              Lambda2<? super R, ? super T, ? extends R> combiner) {
    R result = base;
    for (T elt : iter) { result = combiner.value(result, elt); }
    return result;
  }
  
  /** 
   * Check whether the given predicate holds for all values in {@code iter}.  Computation halts immediately where the 
   * predicate fails.  May never halt if the iterable is infinite.
   */
  public static <T> boolean and(Iterable<? extends T> iter, Predicate<? super T> pred) {
    for (T elt : iter) { if (!pred.value(elt)) { return false; } }
    return true;
  }
  
  /** 
   * Check whether the given predicate holds for some value in {@code iter}.  Computation halts immediately where the 
   * predicate succeeds.  May never halt if the interable is infinite.
   */
  public static <T> boolean or(Iterable<? extends T> iter, Predicate<? super T> pred) {
    for (T elt : iter) { if (pred.value(elt)) { return true; } }
    return false;
  }
  
  /** 
   * Check whether the given predicate holds for all corresponding values in {@code iter1} and {@code iter2}.  The
   * iterables are assumed to have the same length; computation halts immediately where the predicate fails.
   * May never halt if the iterables are infinite.
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
   * May never halt if the iterables are infinite.
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
   * May never halt if the iterables are infinite.
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
   * May never halt if the iterables are infinite.
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
   * May never halt if the iterables are infinite.
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
   * May never halt if the iterables are infinite.
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
  
  
  /** Lazily apply a map function to each element in an iterable. */
  public static <T, R> SizedIterable<R> map(Iterable<? extends T> source, Lambda<? super T, ? extends R> map) {
    return new MappedIterable<T, R>(source, map);
  }
  
  /** Immediately apply a map function to each element in an iterable. */
  public static <T, R> SnapshotIterable<R> mapSnapshot(Iterable<? extends T> source,
                                                       Lambda<? super T, ? extends R> map) {
    return new SnapshotIterable<R>(new MappedIterable<T, R>(source, map));
  }
  
  /**
   * Lazily apply a map function to each corresponding pair of elements in the given iterables. The input 
   * iterables are assumed to have the same size.
   */
  public static <T1, T2, R> SizedIterable<R> map(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, 
                                                 Lambda2<? super T1, ? super T2, ? extends R> map) {
    return new BinaryMappedIterable<T1, T2, R>(iter1, iter2, map);
  }
  
  /**
   * Immediately apply a map function to each corresponding pair of elements in the given iterables. The input 
   * iterables are assumed to have the same size.
   */
  public static <T1, T2, R> SnapshotIterable<R> mapSnapshot(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                                            Lambda2<? super T1, ? super T2, ? extends R> map) {
    return new SnapshotIterable<R>(new BinaryMappedIterable<T1, T2, R>(iter1, iter2, map));
  }
  
  /**
   * Lazily apply a map function to each corresponding triple of elements in the given iterables. The input 
   * iterables are assumed to have the same size.
   */
  public static <T1, T2, T3, R> SizedIterable<R> map(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, 
                                                     Iterable<? extends T3> iter3,
                                                     Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    Iterable<Lambda<T1, Lambda<T2, Lambda<T3, R>>>> r0 = singleton(LambdaUtil.<T1, T2, T3, R>curry(map));
    Iterable<Lambda<T2, Lambda<T3, R>>> r1 =
      cross(r0, iter1, LambdaUtil.<T1, Lambda<T2, Lambda<T3, R>>>applicationLambda());
    Iterable<Lambda<T3, R>> r2 =
      BinaryMappedIterable.make(r1, iter2, LambdaUtil.<T2, Lambda<T3, R>>applicationLambda());
    return BinaryMappedIterable.make(r2, iter3, LambdaUtil.<T3, R>applicationLambda());
  }
  
  /**
   * Immediately apply a map function to each corresponding triple of elements in the given iterables. The input 
   * iterables are assumed to have the same size.
   */
  public static <T1, T2, T3, R> SnapshotIterable<R>
    mapSnapshot(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, Iterable<? extends T3> iter3,
                Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    return new SnapshotIterable<R>(map(iter1, iter2, iter3, map));
  }
  
  /**
   * Lazily apply a map function to each corresponding quadruple of elements in the given iterables. The input 
   * iterables are assumed to have the same size.
   */
  public static <T1, T2, T3, T4, R> SizedIterable<R>
    map(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, Iterable<? extends T3> iter3, 
        Iterable<? extends T4> iter4, Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> map) {
    Iterable<Lambda<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>>> r0 = 
      singleton(LambdaUtil.<T1, T2, T3, T4, R>curry(map));
    Iterable<Lambda<T2, Lambda<T3, Lambda<T4, R>>>> r1 =
      cross(r0, iter1, LambdaUtil.<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>>applicationLambda());
    Iterable<Lambda<T3, Lambda<T4, R>>> r2 =
      BinaryMappedIterable.make(r1, iter2, LambdaUtil.<T2, Lambda<T3, Lambda<T4, R>>>applicationLambda());
    Iterable<Lambda<T4, R>> r3 =
      BinaryMappedIterable.make(r2, iter3, LambdaUtil.<T3, Lambda<T4, R>>applicationLambda());
    return BinaryMappedIterable.make(r3, iter4, LambdaUtil.<T4, R>applicationLambda());
  }
  
  /**
   * Immediately apply a map function to each corresponding quadruple of elements in the given iterables. The input 
   * iterables are assumed to have the same size.
   */
  public static <T1, T2, T3, T4, R> SnapshotIterable<R>
    mapSnapshot(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, Iterable<? extends T3> iter3,
                Iterable<? extends T4> iter4,
                Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> map) {
    return new SnapshotIterable<R>(map(iter1, iter2, iter3, iter4, map));
  }
  
  
  /**
   * Lazily produce the cartesian (cross) product of two iterables.  Each pair of elements is combined by the 
   * given function.  The order of results is defined by {@link CartesianIterable}.
   */
  public static <T1, T2, R> SizedIterable<R> cross(Iterable<? extends T1> left, Iterable<? extends T2> right,
                                              Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    return new CartesianIterable<T1, T2, R>(left, right, combiner);
  }
  
  /**
   * Lazily produce the cartesian (cross) product of two iterables, wrapping each combination of elements in a 
   * {@code Pair}.  The order of results is defined by {@link CartesianIterable}.
   */
  public static <T1, T2> SizedIterable<Pair<T1, T2>> cross(Iterable<? extends T1> left, Iterable<? extends T2> right) {
    return cross(left, right, Pair.<T1, T2>factory());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of two iterables.  Each pair of elements is combined by the 
   * given function.  The order of results is defined by {@link DiagonalCartesianIterable}.
   */
  public static <T1, T2, R> SizedIterable<R> diagonalCross(Iterable<? extends T1> left, Iterable<? extends T2> right,
                                                           Lambda2<? super T1, ? super T2, ? extends R> combiner) {
    return new DiagonalCartesianIterable<T1, T2, R>(left, right, combiner);
  }
  
  /**
   * Lazily produce the cartesian (cross) product of two iterables, wrapping each combination of elements in a 
   * {@code Pair}.  The order of results is defined by {@link DiagonalCartesianIterable}.
   */
  public static <T1, T2> SizedIterable<Pair<T1, T2>> diagonalCross(Iterable<? extends T1> left, 
                                                                   Iterable<? extends T2> right) {
    return diagonalCross(left, right, Pair.<T1, T2>factory());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of three iterables.  Each triple of elements is combined by the 
   * given function.  The order of results is defined by {@link CartesianIterable}.
   */
  public static <T1, T2, T3, R>
    SizedIterable<R> cross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, Iterable<? extends T3> iter3, 
                           Lambda3<? super T1, ? super T2, ? super T3, ? extends R> combiner) {
    Iterable<Lambda<T1, Lambda<T2, Lambda<T3, R>>>> r0 = singleton(LambdaUtil.<T1, T2, T3, R>curry(combiner));
    Iterable<Lambda<T2, Lambda<T3, R>>> r1 =
      CartesianIterable.make(r0, iter1, LambdaUtil.<T1, Lambda<T2, Lambda<T3, R>>>applicationLambda());
    Iterable<Lambda<T3, R>> r2 =
      CartesianIterable.make(r1, iter2,LambdaUtil.<T2, Lambda<T3, R>>applicationLambda());
    return CartesianIterable.make(r2, iter3, LambdaUtil.<T3, R>applicationLambda());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of three iterables, wrapping each combination of elements in a 
   * {@code Triple}.  The order of results is defined by {@link CartesianIterable}.
   */
  public static <T1, T2, T3> SizedIterable<Triple<T1, T2, T3>>
    cross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, Iterable<? extends T3> iter3) {
    return cross(iter1, iter2, iter3, Triple.<T1, T2, T3>factory());
  }

  /**
   * Lazily produce the cartesian (cross) product of three iterables.  Each triple of elements is combined by the 
   * given function.  The order of results is defined by {@link DiagonalCartesianIterable}.
   */
  public static <T1, T2, T3, R> SizedIterable<R>
    diagonalCross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, Iterable<? extends T3> iter3, 
                  Lambda3<? super T1, ? super T2, ? super T3, ? extends R> combiner) {
    Iterable<Lambda<T1, Lambda<T2, Lambda<T3, R>>>> r0 = singleton(LambdaUtil.<T1, T2, T3, R>curry(combiner));
    Iterable<Lambda<T2, Lambda<T3, R>>> r1 =
      DiagonalCartesianIterable.make(r0, iter1, LambdaUtil.<T1, Lambda<T2, Lambda<T3, R>>>applicationLambda());
    Iterable<Lambda<T3, R>> r2 =
      DiagonalCartesianIterable.make(r1, iter2,LambdaUtil.<T2, Lambda<T3, R>>applicationLambda());
    return DiagonalCartesianIterable.make(r2, iter3, LambdaUtil.<T3, R>applicationLambda());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of three iterables, wrapping each combination of elements in a 
   * {@code Triple}.  The order of results is defined by {@link DiagonalCartesianIterable}.
   */
  public static <T1, T2, T3> SizedIterable<Triple<T1, T2, T3>>
    diagonalCross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2, Iterable<? extends T3> iter3) {
    return diagonalCross(iter1, iter2, iter3, Triple.<T1, T2, T3>factory());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of four iterables.  Each quadruple of elements is combined by the 
   * given function.  The order of results is defined by {@link CartesianIterable}.
   */
  public static <T1, T2, T3, T4, R>
    SizedIterable<R> cross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                           Iterable<? extends T3> iter3, Iterable<? extends T4> iter4,
                           Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> combiner) {
    Iterable<Lambda<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>>> r0 =
      singleton(LambdaUtil.<T1, T2, T3, T4, R>curry(combiner));
    Iterable<Lambda<T2, Lambda<T3, Lambda<T4, R>>>> r1 =
      CartesianIterable.make(r0, iter1, LambdaUtil.<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>>applicationLambda());
    Iterable<Lambda<T3, Lambda<T4, R>>> r2 =
      CartesianIterable.make(r1, iter2, LambdaUtil.<T2, Lambda<T3, Lambda<T4, R>>>applicationLambda());
    Iterable<Lambda<T4, R>> r3 =
      CartesianIterable.make(r2, iter3,LambdaUtil.<T3, Lambda<T4, R>>applicationLambda());
    return CartesianIterable.make(r3, iter4, LambdaUtil.<T4, R>applicationLambda());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of four iterables, wrapping each combination of elements in a 
   * {@code Quad}.  The order of results is defined by {@link CartesianIterable}.
   */
  public static <T1, T2, T3, T4>
    SizedIterable<Quad<T1, T2, T3, T4>> cross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                         Iterable<? extends T3> iter3, Iterable<? extends T4> iter4) {
    return cross(iter1, iter2, iter3, iter4, Quad.<T1, T2, T3, T4>factory());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of four iterables.  Each quadruple of elements is combined by the 
   * given function.  The order of results is defined by {@link DiagonalCartesianIterable}.
   */
  public static <T1, T2, T3, T4, R>
    SizedIterable<R> diagonalCross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                   Iterable<? extends T3> iter3, Iterable<? extends T4> iter4,
                                   Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> combiner) {
    Iterable<Lambda<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>>> r0 =
      singleton(LambdaUtil.<T1, T2, T3, T4, R>curry(combiner));
    Iterable<Lambda<T2, Lambda<T3, Lambda<T4, R>>>> r1 =
      DiagonalCartesianIterable.make(r0, iter1, 
                                     LambdaUtil.<T1, Lambda<T2, Lambda<T3, Lambda<T4, R>>>>applicationLambda());
    Iterable<Lambda<T3, Lambda<T4, R>>> r2 =
      DiagonalCartesianIterable.make(r1, iter2, LambdaUtil.<T2, Lambda<T3, Lambda<T4, R>>>applicationLambda());
    Iterable<Lambda<T4, R>> r3 =
      DiagonalCartesianIterable.make(r2, iter3,LambdaUtil.<T3, Lambda<T4, R>>applicationLambda());
    return DiagonalCartesianIterable.make(r3, iter4, LambdaUtil.<T4, R>applicationLambda());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of four iterables, wrapping each combination of elements in a 
   * {@code Quad}.  The order of results is defined by {@link DiagonalCartesianIterable}.
   */
  public static <T1, T2, T3, T4>
    SizedIterable<Quad<T1, T2, T3, T4>> diagonalCross(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                                      Iterable<? extends T3> iter3, Iterable<? extends T4> iter4) {
    return diagonalCross(iter1, iter2, iter3, iter4, Quad.<T1, T2, T3, T4>factory());
  }
  
  /**
   * Lazily produce the cartesian (cross) product of an arbitrary number of iterables.  Each tuple in the result 
   * is represented by an iterable.  If {@code iters} is empty, the result is a single empty iterable.
   * The order of results is defined by {@link CartesianIterable}.  The input iterable is assumed to be finite;
   * the elements of this list, on the other hand, need not be.
   */
  public static <T> SizedIterable<Iterable<T>> cross(Iterable<? extends Iterable<? extends T>> iters) {
    return crossFold(iters, IterUtil.<T>empty(), IterUtil.<T>composeRightLambda());
  }  
  
  /**
   * Lazily produce the cartesian (cross) product of an arbitrary number of iterables.  Each tuple in the result 
   * is represented by an iterable.  If {@code iters} is empty, the result is a single empty iterable.
   * The order of results is defined by {@link DiagonalCartesianIterable}.  The input iterable is assumed 
   * to be finite; the elements of this list, on the other hand, need not be.
   */
  public static <T> SizedIterable<Iterable<T>> diagonalCross(Iterable<? extends Iterable<? extends T>> iters) {
    return diagonalCrossFold(iters, IterUtil.<T>empty(), IterUtil.<T>composeRightLambda());
  }  
  
  /**
   * Lazily apply the given folding function to each tuple in the cartesian (cross) product of the given iterables.
   * The order of results is defined by {@link CartesianIterable}.  The input iterable is assumed 
   * to be finite; the elements of this list, on the other hand, need not be.
   */
  public static <T, R> SizedIterable<R> crossFold(Iterable<? extends Iterable<? extends T>> iters, R base,
                                                  Lambda2<? super R, ? super T, ? extends R> combiner) {
    SizedIterable<R> result = singleton(base);
    for (Iterable<? extends T> iter : iters) {
      result = new CartesianIterable<R, T, R>(result, iter, combiner);
    }
    return result;
  }
  
  /**
   * Lazily apply the given folding function to each tuple in the cartesian (cross) product of the given iterables.
   * The order of results is defined by {@link DiagonalCartesianIterable}.  The input iterable is assumed 
   * to be finite; the elements of this list, on the other hand, need not be.
   */
  public static <T, R> SizedIterable<R> diagonalCrossFold(Iterable<? extends Iterable<? extends T>> iters, R base,
                                                          Lambda2<? super R, ? super T, ? extends R> combiner) {
    SizedIterable<R> result = singleton(base);
    for (Iterable<? extends T> iter : iters) {
      result = new DiagonalCartesianIterable<R, T, R>(result, iter, combiner);
    }
    return result;
  }
  
  
  /** Lazily create an iterable containing the values of the given thunks. */
  public static <R> Iterable<R> valuesOf(Iterable<? extends Thunk<? extends R>> iter) {
    return new MappedIterable<Thunk<? extends R>, R>(iter, LambdaUtil.<R>thunkValueLambda());
  }
  
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T, R> Iterable<R> valuesOf(Iterable<? extends Lambda<? super T, ? extends R>> iter, T arg) {
    Lambda<Lambda<? super T, ? extends R>, R> l = LambdaUtil.bindSecond(LambdaUtil.<T, R>applicationLambda(), arg);
    return new MappedIterable<Lambda<? super T, ? extends R>, R>(iter, l);
  }
    
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T1, T2, R> Iterable<R> 
    valuesOf(Iterable<? extends Lambda2<? super T1, ? super T2, ? extends R>> iter, T1 arg1, T2 arg2) {
    Lambda<Lambda2<? super T1, ? super T2, ? extends R>, R> l = 
      LambdaUtil.bindSecond(LambdaUtil.bindThird(LambdaUtil.<T1, T2, R>binaryApplicationLambda(), arg2), arg1);
    return new MappedIterable<Lambda2<? super T1, ? super T2, ? extends R>, R>(iter, l);
  }
  
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T1, T2, T3, R> Iterable<R>
    valuesOf(Iterable<? extends Lambda3<? super T1, ? super T2, ? super T3, ? extends R>> iter, 
             T1 arg1, T2 arg2, T3 arg3) {
    Lambda<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R> l = 
      LambdaUtil.bindSecond(LambdaUtil.bindThird(LambdaUtil.bindFourth(
                            LambdaUtil.<T1, T2, T3, R>ternaryApplicationLambda(), arg3), arg2), arg1);
    return new MappedIterable<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R>(iter, l);
  }
  

  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> Iterable<T> pairFirsts(Iterable<? extends Pair<? extends T, ?>> iter) {
    return new MappedIterable<Pair<? extends T, ?>, T>(iter, Pair.<T>firstGetter());
  }
  
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> Iterable<T> pairSeconds(Iterable<? extends Pair<?, ? extends T>> iter) {
    return new MappedIterable<Pair<?, ? extends T>, T>(iter, Pair.<T>secondGetter());
  }
  
  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> Iterable<T> tripleFirsts(Iterable<? extends Triple<? extends T, ?, ?>> iter) {
    return new MappedIterable<Triple<? extends T, ?, ?>, T>(iter, Triple.<T>firstGetter());
  }
  
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> Iterable<T> tripleSeconds(Iterable<? extends Triple<?, ? extends T, ?>> iter) {
    return new MappedIterable<Triple<?, ? extends T, ?>, T>(iter, Triple.<T>secondGetter());
  }
  
  /** Lazily create an iterable containing the third values of the given tuples. */
  public static <T> Iterable<T> tripleThirds(Iterable<? extends Triple<?, ?, ? extends T>> iter) {
    return new MappedIterable<Triple<?, ?, ? extends T>, T>(iter, Triple.<T>thirdGetter());
  }
  
  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> Iterable<T> quadFirsts(Iterable<? extends Quad<? extends T, ?, ?, ?>> iter) {
    return new MappedIterable<Quad<? extends T, ?, ?, ?>, T>(iter, Quad.<T>firstGetter());
  }
  
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> Iterable<T> quadSeconds(Iterable<? extends Quad<?, ? extends T, ?, ?>> iter) {
    return new MappedIterable<Quad<?, ? extends T, ?, ?>, T>(iter, Quad.<T>secondGetter());
  }
  
  /** Lazily create an iterable containing the third values of the given tuples. */
  public static <T> Iterable<T> quadThirds(Iterable<? extends Quad<?, ?, ? extends T, ?>> iter) {
    return new MappedIterable<Quad<?, ?, ? extends T, ?>, T>(iter, Quad.<T>thirdGetter());
  }
  
  /** Lazily create an iterable containing the fourth values of the given tuples. */
  public static <T> Iterable<T> quadFourths(Iterable<? extends Quad<?, ?, ?, ? extends T>> iter) {
    return new MappedIterable<Quad<?, ?, ?, ? extends T>, T>(iter, Quad.<T>fourthGetter());
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
    return map(iter1, iter2, iter3, Triple.<T1, T2, T3>factory());
  }
    
  /** 
   * Lazily create an iterable of {@code Quad}s of corresponding values from the given iterables (assumed to  
   * always have the same length).  Useful for simultaneous iteration of multiple lists in a {@code for} loop.
   */
  public static <T1, T2, T3, T4> Iterable<Quad<T1, T2, T3, T4>> zip(Iterable<? extends T1> iter1, 
                                                                    Iterable<? extends T2> iter2,
                                                                    Iterable<? extends T3> iter3,
                                                                    Iterable<? extends T4> iter4) {
    return map(iter1, iter2, iter3, iter4, Quad.<T1, T2, T3, T4>factory());
  }
  
}
