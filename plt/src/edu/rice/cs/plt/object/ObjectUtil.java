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

package edu.rice.cs.plt.object;

import java.util.Comparator;

import edu.rice.cs.plt.iter.IterUtil;

/** Utility methods that may be useful in implementing an object of any type. */
public final class ObjectUtil {
  
  private ObjectUtil() {}
  
  
  public static boolean equal(Object o1, Object o2) {
    return (o1 == null) ? (o2 == null) : o1.equals(o2);
  }
  
  
  /** Prime number used for hashing (approximately 2^32 divided by the golden ratio). */
  private static final int KNUTH_CONST = -1640531535; // 2654435761 (unsigned)
  
  /** Optimized implementation of {@link #hash(int[])} with 0 args. */
  public static int hash() { return 1; }
  
  /** Optimized implementation of {@link #hash(int[])} with 1 arg. */
  public static int hash(int a) { return KNUTH_CONST ^ a; }
  
  /** Optimized implementation of {@link #hash(int[])}) with 2 args. */
  public static int hash(int a, int b) { return ((KNUTH_CONST ^ a) * KNUTH_CONST) ^ b; }
  
  /** Optimized implementation of {@link #hash(int[])} with 3 args. */
  public static int hash(int a, int b, int c) {
    return ((((KNUTH_CONST ^ a) * KNUTH_CONST) ^ b) * KNUTH_CONST) ^ c;
  }
  
  /** Optimized implementation of {@link #hash(int[])} with 4 args. */
  public static int hash(int a, int b, int c, int d) {
    return ((((((KNUTH_CONST ^ a) * KNUTH_CONST) ^ b) * KNUTH_CONST) ^ c) * KNUTH_CONST) ^ d;
  }
  
  /**
   * Produce a hash value based on the given keys according to an algorithm attributed to
   * Knuth (The Art of Programming, Vol. 3, Sorting and Searching) (TODO: verify this source).
   * The key idea is to combine 32-bit hash keys (where a typical class has multiple hash keys)
   * using exclusive OR after multiplying the existing accumulated result by a large prime number.
   */
  public static int hash(int... keys) {
    int len = keys.length;
    int result = 1;
    for (int i = 0; i < len; i++) { result = (result * KNUTH_CONST) ^ keys[i]; }
    return result;
  }

  /** Optimized implementation of {@link #hash(Object[])} with 1 arg. */
  public static int hash(Object a) { return hash((a == null) ? 0 : a.hashCode()); }
  
  /** Optimized implementation of {@link #hash(Object[])}) with 2 args. */
  public static int hash(Object a, Object b) {
    return hash((a == null) ? 0 : a.hashCode(), (b == null) ? 0 : b.hashCode());
  }
  
  /** Optimized implementation of {@link #hash(Object[])} with 3 args. */
  public static int hash(Object a, Object b, Object c) {
    return hash((a == null) ? 0 : a.hashCode(), (b == null) ? 0 : b.hashCode(),
                (c == null) ? 0 : c.hashCode());
  }
  
  /** Optimized implementation of {@link #hash(Object[])} with 4 args. */
  public static int hash(Object a, Object b, Object c, Object d) {
    return hash((a == null) ? 0 : a.hashCode(), (b == null) ? 0 : b.hashCode(),
                (c == null) ? 0 : c.hashCode(), (d == null) ? 0 : d.hashCode());
  }
  
  /** Produce a hash code for an object in which {@code #equals()} depends on the given values. */
  public static int hash(Object... objs) {
    int len = objs.length;
    int[] keys = new int[len];
    for (int i = 0; i < len; i++) {
      Object obj = objs[i];
      keys[i] = (obj == null) ? 0 : obj.hashCode();
    }
    return hash(keys);
  }
  
  /** Produce a hash code for an object in which {@code equals()} depends on the given values. */
  public static int hash(Iterable<?> iter) {
    int result = 1;
    for (Object obj : iter) {
      int key = (obj == null) ? 0 : obj.hashCode();
      result = (result * KNUTH_CONST) ^ key;
    }
    return result;
  }
  
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
      int compare(T1 x1, T1 y1, T2 x2, T2 y2) {
    int result = x1.compareTo(y1);
    if (result == 0) { result = x2.compareTo(y2); }
    return result;
  }
  
  public static <T1, T2> int compare(Comparator<? super T1> comp1, T1 x1, T1 y1,
                                       Comparator<? super T2> comp2, T2 x2, T2 y2) {
    int result = comp1.compare(x1, y1);
    if (result == 0) { result = comp2.compare(x2, y2); }
    return result;
  }
  
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>,
                   T3 extends Comparable<? super T3>>
      int compare(T1 x1, T1 y1, T2 x2, T2 y2, T3 x3, T3 y3) {
    int result = x1.compareTo(y1);
    if (result == 0) { result = x2.compareTo(y2); }
    if (result == 0) { result = x3.compareTo(y3); }
    return result;
  }
  
  public static <T1, T2, T3> int compare(Comparator<? super T1> comp1, T1 x1, T1 y1,
                                           Comparator<? super T2> comp2, T2 x2, T2 y2,
                                           Comparator<? super T3> comp3, T3 x3, T3 y3) {
    int result = comp1.compare(x1, y1);
    if (result == 0) { result = comp2.compare(x2, y2); }
    if (result == 0) { result = comp3.compare(x3, y3); }
    return result;
  }

  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>,
                   T3 extends Comparable<? super T3>, T4 extends Comparable<? super T4>>
      int compare(T1 x1, T1 y1, T2 x2, T2 y2, T3 x3, T3 y3, T4 x4, T4 y4) {
    int result = x1.compareTo(y1);
    if (result == 0) { result = x2.compareTo(y2); }
    if (result == 0) { result = x3.compareTo(y3); }
    if (result == 0) { result = x4.compareTo(y4); }
    return result;
  }
  
  public static <T1, T2, T3, T4> int compare(Comparator<? super T1> comp1, T1 x1, T1 y1,
                                               Comparator<? super T2> comp2, T2 x2, T2 y2,
                                               Comparator<? super T3> comp3, T3 x3, T3 y3,
                                               Comparator<? super T4> comp4, T4 x4, T4 y4) {
    int result = comp1.compare(x1, y1);
    if (result == 0) { result = comp2.compare(x2, y2); }
    if (result == 0) { result = comp3.compare(x3, y3); }
    if (result == 0) { result = comp4.compare(x4, y4); }
    return result;
  }


  /**
   * Get the height of the object when treated as a composite.  If {@code obj} implements
   * {@code Composite}, invokes {@link Composite#compositeHeight}; otherwise, returns {@code 0}.
   */
  public static int compositeHeight(Object obj) {
    if (obj instanceof Composite) { return ((Composite) obj).compositeHeight(); }
    else { return 0; }
  }
  
  /** Get the maximum composite height of a set of objects. */
  public static int compositeHeight(Object... objs) { return compositeHeight(IterUtil.asIterable(objs)); }
  
  /** Get the maximum composite height of a set of objects. */
  public static int compositeHeight(Iterable<?> objs) {
    int result = 0;
    for (Object obj : objs) {
      int height = compositeHeight(obj);
      if (result < height) { result = height; }
    }
    return result;
  }
  
  /**
   * Get the composite size of the object.  If {@code obj} implements {@code Composite},
   * invokes {@link Composite#compositeSize}; otherwise, returns {@code 1}.
   */
  public static int compositeSize(Object obj) {
    if (obj instanceof Composite) { return ((Composite) obj).compositeSize(); }
    else { return 1; }
  }
  
  /** Get the total composite size of a set of objects. */
  public static int compositeSize(Object... objs) { return compositeSize(IterUtil.asIterable(objs)); }
  
  /** Get the total composite size of a set of objects. */
  public static int compositeSize(Iterable<?> objs) {
    int result = 0;
    for (Object obj : objs) { result += compositeSize(obj); }
    return result;
  }
  
}
