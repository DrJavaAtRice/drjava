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

package edu.rice.cs.plt.iter;

import java.util.*;
import java.lang.reflect.Array;
import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;

import edu.rice.cs.plt.lambda.*;
import edu.rice.cs.plt.tuple.*;
import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.collect.ConsList;
import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.object.ObjectUtil;

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
    else if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).isEmpty(); }
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
      // javac 1.5.0_16 crashes with this annotation
      //for (@SuppressWarnings("unused") Object o : iter) { result++; if (result == Integer.MAX_VALUE) break; }
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
      // javac 1.5.0_16 crashes with this annotation
      //for (@SuppressWarnings("unused") Object o : iter) { result++; if (result == bound) break; }
      for (Object o : iter) { result++; if (result == bound) break; }
      return result;
    }
  }
  
  /**
   * Return {@code true} iff the given iterable is known to have an infinite size.
   * @see SizedIterable#isInfinite
   */
  public static boolean isInfinite(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).isInfinite(); }
    else { return false; }
  }
  
  /**
   * Return {@code true} iff the given iterable is known to have a fixed size.  Infinite iterables are considered
   * fixed if they will never become finite.
   * @see SizedIterable#hasFixedSize
   */
  public static boolean hasFixedSize(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).hasFixedSize(); }
    else if (iter instanceof Collection<?>) { return isFixedSizeCollection((Collection<?>) iter); }
    else { return false; }
  }
  
  /** Return {@code true} iff the given collection is known to have a fixed size. */
  private static boolean isFixedSizeCollection(Collection<?> iter) {
    return (iter == Collections.EMPTY_SET) || (iter == Collections.EMPTY_LIST);
  }
  
  /**
   * Return {@code true} iff the given iterable is known to be immutable.
   * @see SizedIterable#isStatic
   */
  public static boolean isStatic(Iterable<?> iter) {
    if (iter instanceof SizedIterable<?>) { return ((SizedIterable<?>) iter).isStatic(); }
    else if (iter instanceof Collection<?>) { return isStaticCollection((Collection<?>) iter); }
    else { return false; }
  }
  
  /** Return {@code true} iff the given collection is known to be immutable. */
  private static boolean isStaticCollection(Collection<?> iter) {
    return (iter == Collections.EMPTY_SET) || (iter == Collections.EMPTY_LIST);
  }
  
  /**
   * Test whether the given object appears in an iteration of {@code iter}.  Uses the
   * {@link Collection#contains} method where possible; otherwise, may take linear time.
   */
  public static boolean contains(Iterable<?> iter, Object o) {
    if (iter instanceof Collection<?>) { return ((Collection<?>) iter).contains(o); }
    else { return iteratedContains(iter, o); }
  }
  
  /**
   * Test whether the given objects all appear in when iterating over {@code iter}.  Uses the
   * {@link Collection#containsAll} and {@link Collection#contains} methods where possible;
   * otherwise, may take quadratic time.
   * @see CollectUtil#containsAll
   * @see #and
   */
  public static boolean containsAll(Iterable<?> iter, Iterable<?> subset) {
    if (iter instanceof Collection<?>) { return CollectUtil.containsAll((Collection<?>) iter, subset); }
    else {
      for (Object o : subset) {
        if (!iteratedContains(iter, o)) { return false; }
      }
      return true;
    }
  }
  
  /**
   * Test whether one of the given objects appears in an iteration of {@code iter}.  Uses the
   * {@link Collection#contains} method where possible; otherwise, may take quadratic time.
   * @see #or
   */
  public static boolean containsAny(Iterable<?> iter, Iterable<?> candidates) {
    if (iter instanceof Collection<?>) { return CollectUtil.containsAny((Collection<?>) iter, candidates); }
    else {
      for (Object o : candidates) {
        if (iteratedContains(iter, o)) { return true; }
      }
      return false;
    }
  }
  
  private static boolean iteratedContains(Iterable<?> iter, Object o) {
    if (o == null) {
      for (Object elt : iter) {
        if (elt == null) { return true; }
      }
      return false;
    }
    else {
      for (Object elt : iter) {
        if (o.equals(elt)) { return true; }
      }
      return false;
    }
  }
  
  /** 
   * Generate a string representation of the given iterable, matching the {@link Collection}
   * conventions (results like {@code "[foo, bar, baz]"}).  Invokes 
   * {@link TextUtil#toString(Object)} on each element.  If the iterable is known to be
   * infinite ({@link #isInfinite}), the string contains a few elements followed by {@code "..."}.
   */
  public static String toString(Iterable<?> iter) {
    return toString(iter, "[", ", ", "]");
  }
  
  /** 
   * Generate a string representation of the given iterable where each element is listed on a
   * separate line.  Invokes {@link TextUtil#toString(Object)} on each element.  If the iterable 
   * is known to be infinite ({@link #isInfinite}), the string contains a few elements followed by 
   * {@code "..."}.
   */
  public static String multilineToString(Iterable<?> iter) {
    return toString(iter, "", TextUtil.NEWLINE, "");
  }
  
  /** 
   * Generate a string representation of the given iterable beginning with {@code prefix}, ending with
   * {@code suffix}, and delimited by {@code delimiter}.  Invokes {@link TextUtil#toString(Object)} 
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
      result.append(TextUtil.toString(obj));
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
   * Return a hash code computed based on the hashes of each element.  The result is consistent
   * with {@link #isEqual}, but may not be consistent with the input's {@code equals} and
   * {@code hashCode} methods. Assumes the iterable is finite.  Implemented with
   * {@link ObjectUtil#hash(Iterable)}.
   */
  public static int hashCode(Iterable<?> iter) {
    return ObjectUtil.hash(iter);
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
    return (Lambda2<T, Iterable<? extends T>, Iterable<T>>) (Lambda2<?, ?, ?>) ComposeLeftLambda.INSTANCE;
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
    return (Lambda2<Iterable<? extends T>, T, Iterable<T>>) (Lambda2<?, ?, ?>) ComposeRightLambda.INSTANCE;
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
    return (Lambda2<Iterable<? extends T>, Iterable<? extends T>, Iterable<T>>) (Lambda2<?, ?, ?>)
           ComposeLambda.INSTANCE;
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
  
  /**
   * Produce a snapshot of {@code iter} if its composite size is greater than the given threshold.
   * @see ObjectUtil#compositeSize
   */
  public static <T> Iterable<T> conditionalSnapshot(Iterable<T> iter, int threshold) {
    if (ObjectUtil.compositeSize(iter) > threshold) { return new SnapshotIterable<T>(iter); }
    else { return iter; }
  }
  
  /** Produce an {@link ImmutableIterable} with the given iterable. */
  public static <T> ImmutableIterable<T> immutable(Iterable<? extends T> iter) {
    return new ImmutableIterable<T>(iter);
  }
  
  /**
   * Allow covariance in situations where wildcards can't be used by wrapping the iterable with a less-
   * precise type parameter.
   */
  public static <T> SizedIterable<T> relax(Iterable<? extends T> iter) {
    return new ImmutableIterable<T>(iter);
  }
    
  /**
   * Allow covariance in situations where wildcards can't be used by wraping the iterator with a less-
   * precise type parameter.
   */
  public static <T> Iterator<T> relax(Iterator<? extends T> iter) {
    return new ImmutableIterator<T>(iter);
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
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7, T v8) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7, v8 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7, T v8, T v9) {
    @SuppressWarnings("unchecked") T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7, v8, v9 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values. */
  public static <T> SizedIterable<T> make(T v1, T v2, T v3, T v4, T v5, T v6, T v7, T v8, T v9, T v10) {
    @SuppressWarnings("unchecked") 
    T[] values = (T[]) new Object[]{ v1, v2, v3, v4, v5, v6 , v7, v8, v9, v10 };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Create an immutable SizedIterable containing the given values.  Requires linear time to make a copy
    * (necessary because {@code vals} can be mutated by the caller).  Note that restrictions on array
    * creation may lead to errors or warnings at the invocation site where {@code T} is a non-reifiable type.
    */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> SizedIterable<T> make(T... vals) {
    return snapshot(new ObjectArrayWrapper<T>(vals));
  }

  /** Create an immutable SizedIterable containing the given values, from index {@code start} through the end
    * of the array. Requires linear time to make a copy (necessary because {@code vals} can be mutated by the
    * caller).
    */
  public static <T> SizedIterable<T> make(T[] vals, int start) {
    return snapshot(new ObjectArrayWrapper<T>(vals, start));
  }

  /**
   * Create an immutable SizedIterable containing the given values, from array index {@code start} through
   * {@code end-1}, inclusive (the size of the result is {@code end-start}). Requires linear time to make a
   * copy (necessary because {@code vals} can be mutated by the caller).
   */
  public static <T> SizedIterable<T> make(T[] vals, int start, int end) {
    return snapshot(new ObjectArrayWrapper<T>(vals, start, end));
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
  
  /** Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
    * result.  (If that is not the desired behavior, make a copy instead with {@link #make(Object[])}.)
    */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> SizedIterable<T> asIterable(T... array) {
    return new ObjectArrayWrapper<T>(array);
  }
  
  /** Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
    * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
    * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
    * and potential memory leaks, make a copy instead with {@link #make(Object[], int)}.)
    * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
    */
  public static <T> SizedIterable<T> arraySegment(T[] array, int start) {
    return new ObjectArrayWrapper<T>(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, make a copy instead with 
   * {@link #make(Object[], int, int)}.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static <T> SizedIterable<T> arraySegment(T[] array, int start, int end) {
    return new ObjectArrayWrapper<T>(array, start, end);
  }
  
  private static final class ObjectArrayWrapper<T> extends AbstractIterable<T> 
      implements SizedIterable<T>, OptimizedLastIterable<T>, Serializable {
    private final T[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    private final boolean _refs; // whether there may be other references to _array (allowing mutation)

    public ObjectArrayWrapper(T[] array) { this(array, 0, array.length, true); }
    
    public ObjectArrayWrapper(T[] array, int start) {
      this(array, start, array.length, true);
      if (_start < 0 || _start > _end) { throw new IndexOutOfBoundsException(); }
    }
    
    public ObjectArrayWrapper(T[] array, int start, int end) {
      this(array, start, end, true);
      if (_start < 0 || _start > _end || _end > _array.length) { throw new IndexOutOfBoundsException(); }
    }
    
    public ObjectArrayWrapper(T[] array, boolean refs) { this(array, 0, array.length, refs); }
    
    public ObjectArrayWrapper(T[] array, int start, int end, boolean refs) {
      _array = array;
      _start = start;
      _end = end;
      _refs = refs;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return !_refs; }
    public T last() { return _array[_end-1]; }
    
    public Iterator<T> iterator() {
      return new IndexedIterator<T>() {
        protected int size() { return _end-_start; }
        protected T get(int i) { return _array[_start+i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Boolean> asIterable(boolean[] array) {
    return new BooleanArrayWrapper(array);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Boolean> arraySegment(boolean[] array, int start) {
    return new BooleanArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Boolean> arraySegment(boolean[] array, int start, int end) {
    return new BooleanArrayWrapper(array, start, end);
  }
  
  private static final class BooleanArrayWrapper extends AbstractIterable<Boolean> 
      implements SizedIterable<Boolean>, OptimizedLastIterable<Boolean>, Serializable {
    private final boolean[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public BooleanArrayWrapper(boolean[] array) { _array = array; _start = 0; _end = _array.length; }
    public BooleanArrayWrapper(boolean[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public BooleanArrayWrapper(boolean[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Boolean last() { return _array[_end-1]; }
    public Iterator<Boolean> iterator() {
      return new IndexedIterator<Boolean>() {
        protected int size() { return _end-_start; }
        protected Boolean get(int i) { return _array[_start+i]; }
      };
    }
  }
  
  /**
   * Create a SizedIterable wrapping the given array.  Subsequent changes to the array will be reflected in the
   * result.  (If that is not the desired behavior, {@link #snapshot(Iterable)} may be invoked on the result.)
   */
  public static SizedIterable<Character> asIterable(char[] array) {
    return new CharArrayWrapper(array);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Character> arraySegment(char[] array, int start) {
    return new CharArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Character> arraySegment(char[] array, int start, int end) {
    return new CharArrayWrapper(array, start, end);
  }
  
  private static final class CharArrayWrapper extends AbstractIterable<Character> 
      implements SizedIterable<Character>, OptimizedLastIterable<Character>, Serializable {
    private final char[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public CharArrayWrapper(char[] array) { _array = array; _start = 0; _end = _array.length; }
    public CharArrayWrapper(char[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public CharArrayWrapper(char[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Character last() { return _array[_end-1]; }
    public Iterator<Character> iterator() {
      return new IndexedIterator<Character>() {
        protected int size() { return _end-_start; }
        protected Character get(int i) { return _array[_start+i]; }
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
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Byte> arraySegment(byte[] array, int start) {
    return new ByteArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Byte> arraySegment(byte[] array, int start, int end) {
    return new ByteArrayWrapper(array, start, end);
  }
  
  private static final class ByteArrayWrapper extends AbstractIterable<Byte> 
      implements SizedIterable<Byte>, OptimizedLastIterable<Byte>, Serializable {
    private final byte[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public ByteArrayWrapper(byte[] array) { _array = array; _start = 0; _end = _array.length; }
    public ByteArrayWrapper(byte[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public ByteArrayWrapper(byte[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Byte last() { return _array[_end-1]; }
    public Iterator<Byte> iterator() {
      return new IndexedIterator<Byte>() {
        protected int size() { return _end-_start; }
        protected Byte get(int i) { return _array[_start+i]; }
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
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Short> arraySegment(short[] array, int start) {
    return new ShortArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Short> arraySegment(short[] array, int start, int end) {
    return new ShortArrayWrapper(array, start, end);
  }
  
  private static final class ShortArrayWrapper extends AbstractIterable<Short> 
      implements SizedIterable<Short>, OptimizedLastIterable<Short>, Serializable {
    private final short[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public ShortArrayWrapper(short[] array) { _array = array; _start = 0; _end = _array.length; }
    public ShortArrayWrapper(short[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public ShortArrayWrapper(short[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Short last() { return _array[_end-1]; }
    public Iterator<Short> iterator() {
      return new IndexedIterator<Short>() {
        protected int size() { return _end-_start; }
        protected Short get(int i) { return _array[_start+i]; }
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
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Integer> arraySegment(int[] array, int start) {
    return new IntArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Integer> arraySegment(int[] array, int start, int end) {
    return new IntArrayWrapper(array, start, end);
  }
  
  private static final class IntArrayWrapper extends AbstractIterable<Integer> 
      implements SizedIterable<Integer>, OptimizedLastIterable<Integer>, Serializable {
    private final int[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public IntArrayWrapper(int[] array) { _array = array; _start = 0; _end = _array.length; }
    public IntArrayWrapper(int[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public IntArrayWrapper(int[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Integer last() { return _array[_end-1]; }
    public Iterator<Integer> iterator() {
      return new IndexedIterator<Integer>() {
        protected int size() { return _end-_start; }
        protected Integer get(int i) { return _array[_start+i]; }
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
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Long> arraySegment(long[] array, int start) {
    return new LongArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Long> arraySegment(long[] array, int start, int end) {
    return new LongArrayWrapper(array, start, end);
  }
  
  private static final class LongArrayWrapper extends AbstractIterable<Long> 
      implements SizedIterable<Long>, OptimizedLastIterable<Long>, Serializable {
    private final long[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public LongArrayWrapper(long[] array) { _array = array; _start = 0; _end = _array.length; }
    public LongArrayWrapper(long[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public LongArrayWrapper(long[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Long last() { return _array[_end-1]; }
    public Iterator<Long> iterator() {
      return new IndexedIterator<Long>() {
        protected int size() { return _end-_start; }
        protected Long get(int i) { return _array[_start+i]; }
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
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Float> arraySegment(float[] array, int start) {
    return new FloatArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Float> arraySegment(float[] array, int start, int end) {
    return new FloatArrayWrapper(array, start, end);
  }
  
  private static final class FloatArrayWrapper extends AbstractIterable<Float> 
      implements SizedIterable<Float>, OptimizedLastIterable<Float>, Serializable {
    private final float[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public FloatArrayWrapper(float[] array) { _array = array; _start = 0; _end = _array.length; }
    public FloatArrayWrapper(float[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public FloatArrayWrapper(float[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Float last() { return _array[_end-1]; }
    public Iterator<Float> iterator() {
      return new IndexedIterator<Float>() {
        protected int size() { return _end-_start; }
        protected Float get(int i) { return _array[_start+i]; }
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
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through the
   * end of the array are included.  Subsequent changes to the array will be reflected in the result; also note
   * that entire array will remain in memory until references to this segment are discarded.  (To prevent mutation
   * and potential memory leaks, {@link #snapshot(Iterable)} may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} is an invalid index.
   */
  public static SizedIterable<Double> arraySegment(double[] array, int start) {
    return new DoubleArrayWrapper(array, start);
  }
  
  /**
   * Create a SizedIterable wrapping a segment of the given array.  Elements from index {@code start} through 
   * {@code end-1} are included (and the size is thus {@code end-start}).  Subsequent changes to the array
   * will be reflected in the result; also note that entire array will remain in memory until references to 
   * this segment are discarded.  (To prevent mutation and potential memory leaks, {@link #snapshot(Iterable)}
   * may be invoked on the result.)
   * @throws IndexOutOfBoundsException  If {@code start} and {@code end} are inconsistent with each other or
   *                                    with the length of the array.
   */
  public static SizedIterable<Double> arraySegment(double[] array, int start, int end) {
    return new DoubleArrayWrapper(array, start, end);
  }
  
  private static final class DoubleArrayWrapper extends AbstractIterable<Double> 
      implements SizedIterable<Double>, OptimizedLastIterable<Double>, Serializable {
    private final double[] _array;
    private final int _start; // start index
    private final int _end; // 1 + the last index
    public DoubleArrayWrapper(double[] array) { _array = array; _start = 0; _end = _array.length; }
    public DoubleArrayWrapper(double[] array, int start) {
      if (start < 0 || start > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = array.length;
    }
    public DoubleArrayWrapper(double[] array, int start, int end) {
      if (start < 0 || start > end || end > array.length) { throw new IndexOutOfBoundsException(); }
      _array = array; _start = start; _end = end;
    }
    
    public boolean isEmpty() { return _start == _end; }
    public int size() { return _end-_start; }
    public int size(int bound) { int result = _end-_start; return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return true; }
    public boolean isStatic() { return false; }
    public Double last() { return _array[_end-1]; }
    public Iterator<Double> iterator() {
      return new IndexedIterator<Double>() {
        protected int size() { return _end-_start; }
        protected Double get(int i) { return _array[_start+i]; }
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
   * Convert the given {@code Collection} to a {@code SizedIterable}.  If it already <em>is</em>
   * a {@code SizedIterable}, cast it as such.  Otherwise, wrap it.  In either case, subsequent
   * changes made to the collection will be reflected in the result (if this is not the desired
   * behavior, {@link #snapshot(Iterable)} can be used instead).
   */
  public static <T> SizedIterable<T> asSizedIterable(Collection<T> coll) {
    if (coll instanceof SizedIterable<?>) { return (SizedIterable<T>) coll; }
    else { return new CollectionWrapper<T>(coll); }
  }
  
  private static final class CollectionWrapper<T> extends AbstractIterable<T> 
                                                  implements SizedIterable<T>, OptimizedLastIterable<T>,
                                                             Serializable {
    private final Collection<T> _c;
    public CollectionWrapper(Collection<T> c) { _c = c; }
    public Iterator<T> iterator() { return _c.iterator(); }
    public boolean isEmpty() { return _c.isEmpty(); }
    public int size() { return _c.size(); }
    public int size(int bound) { int result = _c.size(); return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return isFixedSizeCollection(_c); }
    public boolean isStatic() { return isStaticCollection(_c); }
    public T last() { return IterUtil.last(_c); }
  }
  

  /** Create an iterable that wraps the given {@code CharSequence}. */
  public static SizedIterable<Character> asIterable(CharSequence sequence) {
    return new CharSequenceWrapper(sequence, true);
  }
  
  /** 
   * Create an iterable that wraps the given string.  This is similar to {@link #asIterable(CharSequence)}, 
   * but takes advantage of the fact that {@code String}s are immutable.
   */
  public static SizedIterable<Character> asIterable(final String sequence) {
    return new CharSequenceWrapper(sequence, false);
  }
  
  private static final class CharSequenceWrapper extends AbstractIterable<Character> 
      implements SizedIterable<Character>, OptimizedLastIterable<Character>, Serializable {
    private final CharSequence _s;
    private final boolean _mutable; // whether this sequence is possibly mutable
    public CharSequenceWrapper(CharSequence s, boolean mutable) { _s = s; _mutable = mutable; }
    public boolean isEmpty() { return _s.length() == 0; }
    public int size() { return _s.length(); }
    public int size(int bound) { int result = _s.length(); return result <= bound ? result : bound; }
    public boolean isInfinite() { return false; }
    public boolean hasFixedSize() { return !_mutable; }
    public boolean isStatic() { return !_mutable; }
    public Character last() { return _s.charAt(_s.length()-1); }
    public Iterator<Character> iterator() {
      return new IndexedIterator<Character>() {
        protected int size() { return _s.length(); }
        protected Character get(int i) { return _s.charAt(i); }
      };
    }
  }
  
  /** Produce an iterable of size 0 or 1 from an {@code Option}. */
  public static <T> SizedIterable<T> toIterable(Option<? extends T> option) {
    return option.apply(new OptionVisitor<T, SizedIterable<T>>() {
      public SizedIterable<T> forSome(T val) { return new SingletonIterable<T>(val); }
      @SuppressWarnings("unchecked")
      public SizedIterable<T> forNone() { return (EmptyIterable<T>) EmptyIterable.INSTANCE; }
    });
  }

  /** Produce an iterable of size 1 from a {@code Wrapper}. */
  public static <T> SizedIterable<T> toIterable(Wrapper<? extends T> tuple) {
    return new SingletonIterable<T>(tuple.value());
  }
  
  /** Produce an iterable of size 2 from a {@code Pair}. */
  public static <T> SizedIterable<T> toIterable(Pair<? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second() };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Produce an iterable of size 3 from a {@code Triple}. */
  public static <T> SizedIterable<T> toIterable(Triple<? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third() };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Produce an iterable of size 4 from a {@code Quad}. */
  public static <T> SizedIterable<T> toIterable(Quad<? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth() };
    return new ObjectArrayWrapper<T>(values, false);
  }

  /** Produce an iterable of size 5 from a {@code Quint}. */
  public static <T> SizedIterable<T>
    toIterable(Quint<? extends T, ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(), 
                                     tuple.fifth() };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Produce an iterable of size 6 from a {@code Sextet}. */
  public static <T> SizedIterable<T>
  toIterable(Sextet<? extends T, ? extends T, ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(),
                                     tuple.fifth(), tuple.sixth() };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Produce an iterable of size 7 from a {@code Septet}. */
  public static <T> SizedIterable<T>
  toIterable(Septet<? extends T, ? extends T, ? extends T, ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(),
                                     tuple.fifth(), tuple.sixth(), tuple.seventh() };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /** Produce an iterable of size 8 from an {@code Octet}. */
  public static <T> SizedIterable<T> toIterable(Octet<? extends T, ? extends T, ? extends T, ? extends T,
                                                      ? extends T, ? extends T, ? extends T, ? extends T> tuple) {
    @SuppressWarnings("unchecked")
    T[] values = (T[]) new Object[]{ tuple.first(), tuple.second(), tuple.third(), tuple.fourth(),
                                     tuple.fifth(), tuple.sixth(), tuple.seventh(), tuple.eighth() };
    return new ObjectArrayWrapper<T>(values, false);
  }
  
  /**
   * Make an array with the given elements.  Takes advantage of the (potentially optimized)
   * {@link Collection#toArray} method where possible; otherwise, just iterates through
   * {@code iter} to fill the array.  If the size of the iterable is larger than {@code Integer.MAX_VALUE}
   * or is infinite, it will be truncated to fit in an array.
   */
  public static <T> T[] toArray(Iterable<? extends T> iter, Class<T> type) {
    // Cast is safe because the result has the type of the variable "type"
    @SuppressWarnings("unchecked") T[] result = (T[]) Array.newInstance(type, sizeOf(iter));
    if (iter instanceof Collection<?>) {
      // javac 6 doesn't like this -- Collection<? extends T> </: Iterable<capture extends T>
      @SuppressWarnings("unchecked") T[] newResult = ((Collection<? extends T>) iter).toArray(result);
      result = newResult; // redeclared for the sake of @SuppressWarnings
    }
    else {
      int i = 0;
      for (T t : iter) { result[i++] = t; if (i < 0) break; }
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
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Option<T> makeOption(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size == 0) { return Option.none(); }
    else if (size == 1) { return Option.some(first(iter)); }
    else {
      throw new IllegalArgumentException("Iterable has more than 1 element: size == " + size);
    }
  }
  
  /**
   * Convert an iterable of 1 element to a {@code Wrapper}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Wrapper<T> makeWrapper(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 1) {
      throw new IllegalArgumentException("Iterable does not have 1 element: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Wrapper<T>(i.next());
  }
  
  /**
   * Convert an iterable of 2 elements to a {@code Pair}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Pair<T, T> makePair(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 2) {
      throw new IllegalArgumentException("Iterable does not have 2 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Pair<T, T>(i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 3 elements to a {@code Triple}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Triple<T, T, T> makeTriple(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 3) {
      throw new IllegalArgumentException("Iterable does not have 3 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Triple<T, T, T>(i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 4 elements to a {@code Quad}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Quad<T, T, T, T> makeQuad(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 4) {
      throw new IllegalArgumentException("Iterable does not have 4 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Quad<T, T, T, T>(i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 5 elements to a {@code Quint}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Quint<T, T, T, T, T> makeQuint(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 5) {
      throw new IllegalArgumentException("Iterable does not have 5 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Quint<T, T, T, T, T>(i.next(), i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 6 elements to a {@code Sextet}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Sextet<T, T, T, T, T, T> makeSextet(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 6) {
      throw new IllegalArgumentException("Iterable does not have 6 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Sextet<T, T, T, T, T, T>(i.next(), i.next(), i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 7 elements to a {@code Septet}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Septet<T, T, T, T, T, T, T> makeSeptet(Iterable<? extends T> iter) {
    int size = sizeOf(iter);
    if (size != 7) {
      throw new IllegalArgumentException("Iterable does not have 7 elements: size == " + size);
    }
    Iterator<? extends T> i = iter.iterator();
    return new Septet<T, T, T, T, T, T, T>(i.next(), i.next(), i.next(), i.next(), i.next(), i.next(), i.next());
  }
  
  /**
   * Convert an iterable of 8 elements to an {@code Octet}.
   * @throws IllegalArgumentException  If the iterable is not of the appropriate size.
   */
  public static <T> Octet<T, T, T, T, T, T, T, T> makeOctet(Iterable<? extends T> iter) {
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
    ArrayList<T> result = CollectUtil.makeArrayList(iter);
    Collections.shuffle(result);
    return asSizedIterable(result);
  }
  
  /**
   * Produce a shuffled iterable over the given elements, using the specified random number generator.  
   * Subsequent changes to {@code iter} will not be reflected in the result.  Runs in linear time.
   */
  public static <T> SizedIterable<T> shuffle(Iterable<T> iter, Random random) {
    ArrayList<T> result = CollectUtil.makeArrayList(iter);
    Collections.shuffle(result, random);
    return asSizedIterable(result);
  }
  
  /**
   * Produce a sorted iterable over the given elements.  Subsequent changes to {@code iter} will not
   * be reflected in the result.  Runs in n log n time.
   */
  public static <T extends Comparable<? super T>> SizedIterable<T> sort(Iterable<T> iter) {
    ArrayList<T> result = CollectUtil.makeArrayList(iter);
    Collections.sort(result);
    return asSizedIterable(result);
  }
  
  /**
   * Produce a sorted iterable over the given elements, using the specified comparator.  
   * Subsequent changes to {@code iter} will not be reflected in the result.  Runs in n log n time.
   */
  public static <T> SizedIterable<T> sort(Iterable<T> iter, Comparator<? super T> comp) {
    ArrayList<T> result = CollectUtil.makeArrayList(iter);
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
  
  /** Cast all instances of the given type appropriately; filter out any non-instances. */
  public static <T> FilteredIterable<T> filterInstances(Iterable<? super T> iter, final Class<? extends T> c) {
    Iterable<T> cast = IterUtil.map(iter, new Lambda<Object, T>() {
      public T value(Object obj) {
        if (c.isInstance(obj)) { return c.cast(obj); }
        else { return null; }
      }
    });
    return new FilteredIterable<T>(cast, LambdaUtil.NOT_NULL);
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
    for (T elt : iter) { if (!pred.contains(elt)) { return false; } }
    return true;
  }
  
  /** 
   * Check whether the given predicate holds for some value in {@code iter}.  Computation halts immediately where the 
   * predicate succeeds.  May never halt if the interable is infinite.
   */
  public static <T> boolean or(Iterable<? extends T> iter, Predicate<? super T> pred) {
    for (T elt : iter) { if (pred.contains(elt)) { return true; } }
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
    while (i1.hasNext()) { if (!pred.contains(i1.next(), i2.next())) { return false; } }
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
    while (i1.hasNext()) { if (pred.contains(i1.next(), i2.next())) { return true; } }
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
    while (i1.hasNext()) { if (!pred.contains(i1.next(), i2.next(), i3.next())) { return false; } }
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
    while (i1.hasNext()) { if (pred.contains(i1.next(), i2.next(), i3.next())) { return true; } }
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
      if (!pred.contains(i1.next(), i2.next(), i3.next(), i4.next())) { return false; }
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
      if (pred.contains(i1.next(), i2.next(), i3.next(), i4.next())) { return true; }
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
  
  /** Apply the given runnable to every element in an iterable. */
  public static <T> void run(Iterable<? extends T> iter, Runnable1<? super T> runnable) {
    for (T elt : iter) { runnable.run(elt); }
  }
  
  /**
   * Apply the given runnable to every pair of corresponding elements in the given iterables.
   * The iterables are assumed to have the same length. 
   */
  public static <T1, T2> void run(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                  Runnable2<? super T1, ? super T2> runnable) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    while (i1.hasNext()) { runnable.run(i1.next(), i2.next()); }
  }
  
  /**
   * Apply the given runnable to every triple of corresponding elements in the given iterables.
   * The iterables are assumed to have the same length. 
   */
  public static <T1, T2, T3> void run(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                      Iterable<? extends T3> iter3, 
                                      Runnable3<? super T1, ? super T2, ? super T3> runnable) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    Iterator<? extends T3> i3 = iter3.iterator();
    while (i1.hasNext()) { runnable.run(i1.next(), i2.next(), i3.next()); }
  }
  
  /**
   * Apply the given runnable to every quadruple of corresponding elements in the given iterables.
   * The iterables are assumed to have the same length. 
   */
  public static <T1, T2, T3, T4> void run(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2,
                                          Iterable<? extends T3> iter3, Iterable<? extends T4> iter4,
                                          Runnable4<? super T1, ? super T2, ? super T3, ? super T4> runnable) {
    Iterator<? extends T1> i1 = iter1.iterator();
    Iterator<? extends T2> i2 = iter2.iterator();
    Iterator<? extends T3> i3 = iter3.iterator();
    Iterator<? extends T4> i4 = iter4.iterator();
    while (i1.hasNext()) { runnable.run(i1.next(), i2.next(), i3.next(), i4.next()); }
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
  
  /**
   * Use the {@link #cross(Iterable)} function to lazily apply a distribution rule to the given composite
   * object list.  Given a list of conjunctions, for example, this method transforms the list into an
   * equivalent list of disjunctions.
   * @param <T1> The original object's components, each composed of {@code A}s.
   * @param <A> The type of atomic components of a {@code T1} or {@code S2}.
   * @param <S2> The type of the result's components, again composed of {@code A}s.
   * @param original  A list of original {@code T1}s.
   * @param breakT Decomposes a {@code T1} into its constituent elements.
   * @param makeS Construct an {@code S2} from the given elements.
   */
  public static <T1, A, S2> Iterable<S2> distribute(Iterable<? extends T1> original,
                                                    Lambda<? super T1, ? extends Iterable<? extends A>> breakT,
                                                    Lambda<? super Iterable<A>, ? extends S2> makeS) {
    // to make things concrete, assume original is a sum of products, and we want a product of sums
    Iterable<Iterable<? extends A>> sumOfProducts = map(original, breakT);
    Iterable<Iterable<A>> productOfSums = cross(sumOfProducts);
    return map(productOfSums, makeS);
  }

  /**
   * Use the {@link #cross(Iterable)} function to apply a distribution rule to the given composite object.
   * Given constructors {@code $} and {@code %}, for example, this method transforms an object of the form
   * {@code (a$b) % (c$d$e)} to {@code (a%c) $ (a%d) $ (a%e) $ (b%c) $ (b%d) $ (b%e)}.  For maximum flexibility,
   * the types produced by the {@code $} and {@code %} constructors may be different from each other and from
   * the type of atomic elements.  Additionally, the type of {@code $} applied to some {@code %}-constructed
   * elements may be different than the type of {@code $} applied to atomic elements (and the same for {@code %}).
   * @param <S1> The original object type, composed of {@code T1}s.
   * @param <T1> The type of {@code S1}'s components, composed of {@code A}s.
   * @param <A> The type of atomic components of a {@code T1} or {@code S2}.
   * @param <S2> The type of a {@code T2}'s components in the result, composed of {@code A}s.
   * @param <T2> The result type, composed of {@code S2}s.
   * @param original  The original object
   * @param breakS  Decomposes an {@code S1} into its constituent elements.
   * @param breakT Decomposes a {@code T1} into its constituent elements.
   * @param makeS Construct an {@code S2} from the given elements.
   * @param makeT Construct a {@code T2} from the given elements.
   */
  public static <S1, T1, A, S2, T2> T2 distribute(S1 original,
                                                  Lambda<? super S1, ? extends Iterable<? extends T1>> breakS,
                                                  Lambda<? super T1, ? extends Iterable<? extends A>> breakT,
                                                  Lambda<? super Iterable<A>, ? extends S2> makeS,
                                                  Lambda<? super Iterable<S2>, ? extends T2> makeT) {
    return makeT.value(distribute(breakS.value(original), breakT, makeS));
  }
  
  
  /** Lazily create an iterable containing the values of the given thunks. */
  public static <R> SizedIterable<R> valuesOf(Iterable<? extends Thunk<? extends R>> iter) {
    return new MappedIterable<Thunk<? extends R>, R>(iter, LambdaUtil.<R>thunkValueLambda());
  }
  
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T, R> Iterable<R> valuesOf(Iterable<? extends Lambda<? super T, ? extends R>> iter, T arg) {
    Lambda<Lambda<? super T, ? extends R>, R> l = LambdaUtil.bindSecond(LambdaUtil.<T, R>applicationLambda(), arg);
    return new MappedIterable<Lambda<? super T, ? extends R>, R>(iter, l);
  }
    
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T1, T2, R> SizedIterable<R> 
    valuesOf(Iterable<? extends Lambda2<? super T1, ? super T2, ? extends R>> iter, T1 arg1, T2 arg2) {
    Lambda<Lambda2<? super T1, ? super T2, ? extends R>, R> l = 
      LambdaUtil.bindSecond(LambdaUtil.bindThird(LambdaUtil.<T1, T2, R>binaryApplicationLambda(), arg2), arg1);
    return new MappedIterable<Lambda2<? super T1, ? super T2, ? extends R>, R>(iter, l);
  }
  
  /** Lazily create an iterable containing the values of the application of the given lambdas. */
  public static <T1, T2, T3, R> SizedIterable<R>
    valuesOf(Iterable<? extends Lambda3<? super T1, ? super T2, ? super T3, ? extends R>> iter, 
             T1 arg1, T2 arg2, T3 arg3) {
    Lambda<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R> l = 
      LambdaUtil.bindSecond(LambdaUtil.bindThird(LambdaUtil.bindFourth(
                            LambdaUtil.<T1, T2, T3, R>ternaryApplicationLambda(), arg3), arg2), arg1);
    return new MappedIterable<Lambda3<? super T1, ? super T2, ? super T3, ? extends R>, R>(iter, l);
  }
  

  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> SizedIterable<T> pairFirsts(Iterable<? extends Pair<? extends T, ?>> iter) {
    return new MappedIterable<Pair<? extends T, ?>, T>(iter, Pair.<T>firstGetter());
  }
  
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> SizedIterable<T> pairSeconds(Iterable<? extends Pair<?, ? extends T>> iter) {
    return new MappedIterable<Pair<?, ? extends T>, T>(iter, Pair.<T>secondGetter());
  }
  
  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> SizedIterable<T> tripleFirsts(Iterable<? extends Triple<? extends T, ?, ?>> iter) {
    return new MappedIterable<Triple<? extends T, ?, ?>, T>(iter, Triple.<T>firstGetter());
  }
  
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> SizedIterable<T> tripleSeconds(Iterable<? extends Triple<?, ? extends T, ?>> iter) {
    return new MappedIterable<Triple<?, ? extends T, ?>, T>(iter, Triple.<T>secondGetter());
  }
  
  /** Lazily create an iterable containing the third values of the given tuples. */
  public static <T> SizedIterable<T> tripleThirds(Iterable<? extends Triple<?, ?, ? extends T>> iter) {
    return new MappedIterable<Triple<?, ?, ? extends T>, T>(iter, Triple.<T>thirdGetter());
  }
  
  /** Lazily create an iterable containing the first values of the given tuples. */
  public static <T> SizedIterable<T> quadFirsts(Iterable<? extends Quad<? extends T, ?, ?, ?>> iter) {
    return new MappedIterable<Quad<? extends T, ?, ?, ?>, T>(iter, Quad.<T>firstGetter());
  }
  
  /** Lazily create an iterable containing the second values of the given tuples. */
  public static <T> SizedIterable<T> quadSeconds(Iterable<? extends Quad<?, ? extends T, ?, ?>> iter) {
    return new MappedIterable<Quad<?, ? extends T, ?, ?>, T>(iter, Quad.<T>secondGetter());
  }
  
  /** Lazily create an iterable containing the third values of the given tuples. */
  public static <T> SizedIterable<T> quadThirds(Iterable<? extends Quad<?, ?, ? extends T, ?>> iter) {
    return new MappedIterable<Quad<?, ?, ? extends T, ?>, T>(iter, Quad.<T>thirdGetter());
  }
  
  /** Lazily create an iterable containing the fourth values of the given tuples. */
  public static <T> SizedIterable<T> quadFourths(Iterable<? extends Quad<?, ?, ?, ? extends T>> iter) {
    return new MappedIterable<Quad<?, ?, ?, ? extends T>, T>(iter, Quad.<T>fourthGetter());
  }
  
  
  /** 
   * Lazily create an iterable of {@code Pair}s of corresponding values from the given iterables (assumed to always 
   * have the same length).  Useful for simultaneous iteration of multiple lists in a {@code for} loop.
   */
  public static <T1, T2> SizedIterable<Pair<T1, T2>> zip(Iterable<? extends T1> iter1, Iterable<? extends T2> iter2) {
    return new BinaryMappedIterable<T1, T2, Pair<T1, T2>>(iter1, iter2, Pair.<T1, T2>factory());
  }
    
  /** 
   * Lazily create an iterable of {@code Triple}s of corresponding values from the given iterables (assumed to  
   * always have the same length).  Useful for simultaneous iteration of multiple lists in a {@code for} loop.
   */
  public static <T1, T2, T3> SizedIterable<Triple<T1, T2, T3>> zip(Iterable<? extends T1> iter1, 
                                                                   Iterable<? extends T2> iter2,
                                                                   Iterable<? extends T3> iter3) {
    return map(iter1, iter2, iter3, Triple.<T1, T2, T3>factory());
  }
    
  /** 
   * Lazily create an iterable of {@code Quad}s of corresponding values from the given iterables (assumed to  
   * always have the same length).  Useful for simultaneous iteration of multiple lists in a {@code for} loop.
   */
  public static <T1, T2, T3, T4> SizedIterable<Quad<T1, T2, T3, T4>> zip(Iterable<? extends T1> iter1, 
                                                                         Iterable<? extends T2> iter2,
                                                                         Iterable<? extends T3> iter3,
                                                                         Iterable<? extends T4> iter4) {
    return map(iter1, iter2, iter3, iter4, Quad.<T1, T2, T3, T4>factory());
  }
  
}
