/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

package edu.rice.cs.plt.collect;

import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.lambda.Predicate;

/**
 * A visitor for {@link ConsList}s.  Implementations handle the two list variants: 
 * {@link ConsList.Empty} and {@link ConsList.Nonempty}.  For convenience, visitors may
 * also be treated as {@code Lambda}s -- the {@code value} method is implemented to
 * apply the visitor.  A number of standard list visitors are also provided, either as static
 * fields or static methods.
 */
public abstract class ConsVisitor<T, Ret> implements Lambda<ConsList<? extends T>, Ret> {
  
  /** Handle an empty list */
  public abstract Ret forEmpty(ConsList.Empty<? extends T> list);
  
  /** Handle a nonempty list */
  public abstract Ret forNonempty(ConsList.Nonempty<? extends T> list);
  
  /** Invoke {@code list.apply(this)} */
  public Ret value(ConsList<? extends T> list) { return list.apply(this); }
  
  
  /** Determines if a list is empty */
  public static final ConsVisitor<Object, Boolean> IS_EMPTY = new Empty();
  
  private static class Empty extends ConsVisitor<Object, Boolean> implements Serializable {
    private Empty() {}
    public Boolean forEmpty(ConsList.Empty<?> list) { return true; }
    public Boolean forNonempty(ConsList.Nonempty<?> list) { return false; }
  }
  
  /** Attempt to access the first of the given list (throws an exception in the empty case). */
  @SuppressWarnings("unchecked")
  public static final <T> ConsVisitor<T, T> first() { return (First<T>) First.INSTANCE; }
  
  private static class First<T> extends ConsVisitor<T, T> implements Serializable {
    private static final First<Object> INSTANCE = new First<Object>();
    private First() {}
    public T forEmpty(ConsList.Empty<? extends T> list) {
      throw new IllegalArgumentException("Empty ConsList has no first");
    }
    public T forNonempty(ConsList.Nonempty<? extends T> list) {
      return list.first();
    }
  }
  
  
  /** Attempt to access the rest of the given list (throws an exception in the empty case). */
  @SuppressWarnings("unchecked")
    public static final <T> ConsVisitor<T, ConsList<? extends T>> rest() {
      return (Rest<T>) Rest.INSTANCE;
    }
  
  private static class Rest<T> extends ConsVisitor<T, ConsList<? extends T>> implements Serializable {
    private static final Rest<Object> INSTANCE = new Rest<Object>();
    private Rest() {}
    public ConsList<? extends T> forEmpty(ConsList.Empty<? extends T> list) {
      throw new IllegalArgumentException("Empty ConsList has no rest");
    }
    public ConsList<? extends T> forNonempty(ConsList.Nonempty<? extends T> list) {
      return list.rest();
    }
  }
  
  
  /** Reverses the order of the elements in a list */
  public static <T> ConsVisitor<T, ConsList<? extends T>> reverse() {
    return new ReverseHelper<T>(ConsList.<T>empty());
  }
  
  /** Reverses the list and appends {@code toAppend} to the end of it */
  private static class ReverseHelper<T> extends ConsVisitor<T, ConsList<? extends T>> implements Serializable {
    private ConsList<? extends T> _toAppend;
    
    public ReverseHelper(ConsList<? extends T> toAppend) { _toAppend = toAppend; }
    
    public ConsList<? extends T> forEmpty(ConsList.Empty<? extends T> list) { return _toAppend; }
    
    public ConsList<? extends T> forNonempty(ConsList.Nonempty<? extends T> list) {
      return list.rest().apply(new ReverseHelper<T>(ConsList.cons(list.first(), _toAppend)));
    }
  }
  
  
  /** Appends the given list to the end of another list */
  @SuppressWarnings("unchecked")
  public static <T> ConsVisitor<T, ConsList<? extends T>> append(final ConsList<? extends T> rest) {
    return new Append<T>(rest);
  }
  
  private static class Append<T> extends ConsVisitor<T, ConsList<? extends T>> implements Serializable {
    private final ConsList<? extends T> _rest;
    public Append(ConsList<? extends T> rest) { _rest = rest; }
    public ConsList<? extends T> forEmpty(ConsList.Empty<? extends T> list) { return _rest; }
    public ConsList<? extends T> forNonempty(ConsList.Nonempty<? extends T> list) {
      return ConsList.cons(list.first(), list.rest().apply(this));
    }
  }

  
  /** Filters a list to contain only those elements accepted by the given predicate */
  public static <T> ConsVisitor<T, ConsList<? extends T>> filter(Predicate<? super T> pred) {
    return new Filter<T>(pred);
  }
  
  private static class Filter<T> extends ConsVisitor<T, ConsList<? extends T>> implements Serializable {
    private final Predicate<? super T> _pred;
    public Filter(Predicate<? super T> pred) { _pred = pred; }
    public ConsList<? extends T> forEmpty(ConsList.Empty<? extends T> list) { return list; }
    public ConsList<? extends T> forNonempty(ConsList.Nonempty<? extends T> list) {
      if (_pred.value(list.first())) { return ConsList.cons(list.first(), list.rest().apply(this)); }
      else { return list.rest().apply(this); }
    }
  }
  
  
  /** Produces a new list by applying the given lambda to each of a list's elements */
  public static <S, T> ConsVisitor<S, ConsList<? extends T>> map(Lambda<? super S, ? extends T> lambda) {
    return new Map<S, T>(lambda);
  }

  private static class Map<S, T> extends ConsVisitor<S, ConsList<? extends T>> implements Serializable {
    private final Lambda<? super S, ? extends T> _lambda;
    public Map(Lambda<? super S, ? extends T> lambda) { _lambda = lambda; }
    public ConsList<? extends T> forEmpty(ConsList.Empty<? extends S> list) { return ConsList.empty(); }
    public ConsList<? extends T> forNonempty(ConsList.Nonempty<? extends S> list) {
      return ConsList.cons(_lambda.value(list.first()), list.rest().apply(this));
    }
  }
  
}
