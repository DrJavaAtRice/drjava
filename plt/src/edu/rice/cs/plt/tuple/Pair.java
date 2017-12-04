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

package edu.rice.cs.plt.tuple;

import java.io.Serializable;
import java.util.Comparator;

import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.collect.TotalOrder;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * An arbitrary pair of objects; overrides {@link #toString()}, {@link #equals(Object)}, 
 * and {@link #hashCode()}.
 */
public class Pair<T1, T2> extends Tuple {
  
  protected final T1 _first;
  protected final T2 _second;
  
  public Pair(T1 first, T2 second) {
    _first = first;
    _second = second;
  }
  
  public T1 first() { return _first; }
  public T2 second() { return _second; }
  
  /** Invert a pair.  Subclasses should override this method to create a new pair of the same type. */
  public Pair<T2, T1> inverse() { return new Pair<T2, T1>(_second, _first); }
  
  public String toString() {
    return "(" + _first + ", " + _second + ")";
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is equal (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (o == null || !getClass().equals(o.getClass())) { return false; }
    else {
      Pair<?, ?> cast = (Pair<?, ?>) o;
      return 
        (_first == null ? cast._first == null : _first.equals(cast._first)) &&
        (_second == null ? cast._second == null : _second.equals(cast._second));
    }
  }
  
  protected int generateHashCode() {
    return 
      (_first == null ? 0 : _first.hashCode()) ^ 
      (_second == null ? 0 : _second.hashCode() << 1) ^ 
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2> Pair<T1, T2> make(T1 first, T2 second) {
    return new Pair<T1, T2>(first, second);
  }
  
  /** Produce a lambda that invokes the constructor. */
  @SuppressWarnings("unchecked") public static <T1, T2> Lambda2<T1, T2, Pair<T1, T2>> factory() {
    return (Factory<T1, T2>) Factory.INSTANCE;
  }
  
  private static final class Factory<T1, T2> implements Lambda2<T1, T2, Pair<T1, T2>>, Serializable {
    public static final Factory<Object, Object> INSTANCE = new Factory<Object, Object>();
    private Factory() {}
    public Pair<T1, T2> value(T1 first, T2 second) { return new Pair<T1, T2>(first, second); }
  }
  
  /** Produce a lambda that invokes {@link #inverse} on a provided pair. */
  @SuppressWarnings("unchecked") public static <T1, T2> Lambda<Pair<T1, T2>, Pair<T2, T1>> inverter() {
    return (Inverter<T1, T2>) Inverter.INSTANCE;
  }
  
  private static final class Inverter<T1, T2> implements Lambda<Pair<T1, T2>, Pair<T2, T1>>, Serializable {
    public static final Inverter<Void, Void> INSTANCE = new Inverter<Void, Void>();
    private Inverter() {}
    public Pair<T2, T1> value(Pair<T1, T2> arg) { return arg.inverse(); }
  }
  
  /** Produce a lambda that invokes {@link #first} on a provided pair. */
  @SuppressWarnings("unchecked") public static <T> Lambda<Pair<? extends T, ?>, T> firstGetter() {
    return (GetFirst<T>) GetFirst.INSTANCE;
  }
  
  private static final class GetFirst<T> implements Lambda<Pair<? extends T, ?>, T>, Serializable {
    public static final GetFirst<Void> INSTANCE = new GetFirst<Void>();
    private GetFirst() {}
    public T value(Pair<? extends T, ?> arg) { return arg.first(); }
  }
      
  /** Produce a lambda that invokes {@link #second} on a provided pair. */
  @SuppressWarnings("unchecked") public static <T> Lambda<Pair<?, ? extends T>, T> secondGetter() {
    return (GetSecond<T>) GetSecond.INSTANCE;
  }
  
  private static final class GetSecond<T> implements Lambda<Pair<?, ? extends T>, T>, Serializable {
    public static final GetSecond<Void> INSTANCE = new GetSecond<Void>();
    private GetSecond() {}
    public T value(Pair<?, ? extends T> arg) { return arg.second(); }
  }
  
  /**
   * Produce a comparator for pairs, ordered by the natural order of the elements (the leftmost
   * elements have the highest sort priority).
   */
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
      TotalOrder<Pair<? extends T1, ? extends T2>> comparator() {
    return new PairComparator<T1, T2>(CollectUtil.<T1>naturalOrder(), CollectUtil.<T2>naturalOrder());
  }
  
  /**
   * Produce a comparator for pairs, ordered by the given comparators (the leftmost
   * elements have the highest sort priority).
   */
  public static <T1, T2> TotalOrder<Pair<? extends T1, ? extends T2>>
      comparator(Comparator<? super T1> comp1, Comparator<? super T2> comp2) {
    return new PairComparator<T1, T2>(comp1, comp2);
  }
  
  private static final class PairComparator<T1, T2> extends TotalOrder<Pair<? extends T1, ? extends T2>> {
    private final Comparator<? super T1> _comp1;
    private final Comparator<? super T2> _comp2;
    public PairComparator(Comparator<? super T1> comp1, Comparator<? super T2> comp2) {
      _comp1 = comp1;
      _comp2 = comp2;
    }
    public int compare(Pair<? extends T1, ? extends T2> p1, Pair<? extends T1, ? extends T2> p2) {
      return ObjectUtil.compare(_comp1, p1.first(), p2.first(), _comp2, p1.second(), p2.second());
    }
    public boolean equals(Object o) {
      if (this == o) { return true; }
      else if (!(o instanceof PairComparator<?,?>)) { return false; }
      else {
        PairComparator<?,?> cast = (PairComparator<?,?>) o;
        return _comp1.equals(cast._comp1) && _comp2.equals(cast._comp2);
      }
    }
    public int hashCode() { return ObjectUtil.hash(PairComparator.class, _comp1, _comp2); }
  }

}
