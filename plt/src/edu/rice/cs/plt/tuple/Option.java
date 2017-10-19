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

import java.util.Comparator;

import edu.rice.cs.plt.collect.CollectUtil;
import edu.rice.cs.plt.collect.TotalOrder;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A wrapper for optional values.  This provides a strictly-typed alternative to using
 * {@code null} to represent the absence of a value.  Options have two variants: "some"
 * and "none."  The "some" case is represented by {@link Wrapper}s; the "none" case is
 * represented by the {@link Null} singleton.  {@code Option} values may be decomposed
 * by invoking {@link #unwrap()} or {@link #unwrap(Object)}, or by using an
 * {@link OptionVisitor}.
 */
public abstract class Option<T> extends Tuple {
  
  protected Option() {}
  
  /** Calls the appropriate case in the visitor. */
  public abstract <Ret> Ret apply(OptionVisitor<? super T, ? extends Ret> visitor);
  
  /** Determine whether this Option is a "some" case.  Mutually exclusive with {@link #isNone}. */
  public abstract boolean isSome();
  
  /** Determine whether this Option is a "none" case.  Mutually exclusive with {@link #isSome}. */
  public final boolean isNone() { return !isSome(); }
  
  /**
   * Get the value wrapped by this Option, or throw an {@link OptionUnwrapException} if there
   * is no wrapped value.
   */
  public abstract T unwrap() throws OptionUnwrapException;
  
  /** Get the value wrapped by this Option, or {@code forNone} if there is no wrapped value. */
  public abstract T unwrap(T forNone);
  

  /** Create a "some" case wrapper for the given value. */
  public static <T> Option<T> some(T val) { return new Wrapper<T>(val); }
  
  /** Return the "none" case singleton, cast to the appropriate type. */
  @SuppressWarnings("unchecked") public static <T> Option<T> none() {
    return (Option<T>) Null.INSTANCE;
  }
  
  /**
   * Treat a possibly-null value as an {@code Option}: if the value is {@code null}, produce
   * a "none"; otherwise, produce a "some" wrapping the value.
   */
  @SuppressWarnings("unchecked") public static <T> Option<T> wrap(T val) {
    if (val == null) { return (Option<T>) Null.INSTANCE; }
    else { return new Wrapper<T>(val); }
  }
  
  /**
   * A more general form of the instance method {@link #unwrap(Object)}, allowing {@code forNone}
   * to have a different type than {@code opt} (the result has a common supertype).
   */
  public static <T> T unwrap(Option<? extends T> opt, T forNone) {
    if (opt.isSome()) { return opt.unwrap(); }
    else { return forNone; }
  }
    
  /**
   * Produce a comparator for options, ordered by the natural order of the elements with "none" values at
   * the front.
   */
  public static <T extends Comparable<? super T>> TotalOrder<Option<? extends T>> comparator() {
    return new OptionComparator<T>(CollectUtil.<T>naturalOrder());
  }
  
  /** Produce a comparator for options, ordered by the given comparator with "none" values at the front. */
  public static <T> TotalOrder<Option<? extends T>> comparator(Comparator<? super T> comp) {
    return new OptionComparator<T>(comp);
  }
  
  private static final class OptionComparator<T> extends TotalOrder<Option<? extends T>> {
    private final Comparator<? super T> _comp;
    public OptionComparator(Comparator<? super T> comp) { _comp = comp; }
    public int compare(Option<? extends T> o1, Option<? extends T> o2) {
      if (o1.isSome()) { return o2.isSome() ? _comp.compare(o1.unwrap(), o2.unwrap()) : 1; }
      else { return o2.isSome() ? -1 : 0; }
    }
    public boolean equals(Object o) {
      if (this == o) { return true; }
      else if (!(o instanceof OptionComparator<?>)) { return false; }
      else {
        OptionComparator<?> cast = (OptionComparator<?>) o;
        return _comp.equals(cast._comp);
      }
    }
    public int hashCode() { return ObjectUtil.hash(OptionComparator.class, _comp); }
  }

}
