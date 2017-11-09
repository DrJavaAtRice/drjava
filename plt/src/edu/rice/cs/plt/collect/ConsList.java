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

package edu.rice.cs.plt.collect;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import edu.rice.cs.plt.iter.AbstractIterable;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.iter.EmptyIterator;
import edu.rice.cs.plt.iter.ReadOnlyIterator;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.object.Composite;

/**
 * <p>A Lisp- or Scheme-style immutable list.  A {@code ConsList} is either an {@link Empty}
 * or a {@link Nonempty}; operations may be defined on the lists by implementing {@link ConsVisitor}.
 * While {@link edu.rice.cs.plt.iter.ComposedIterable} provides similar facilities for creating
 * lists, {@code ConsList}s provide a more efficient means of <em>decomposing</em> lists --
 * because there is no {@code rest} operation on {@code Iterable}s in general, the only way to
 * produce an {@code Iterable} sublist of an {@code Iterable} is to make a copy.  With a 
 * well-designed {@code ConsList}, on the other hand, producing a sublist is trivial.</p>
 * 
 * <p>This class also defines a few static convenience methods to simplify 
 * client code.  A static import ({@code import static edu.rice.cs.plt.collect.ConsList.*})
 * will eliminate the need for explicit name qualifiers when using these methods.</p>
 */
public abstract class ConsList<T> extends AbstractIterable<T> implements SizedIterable<T>, Composite, Serializable {
  
  public abstract <Ret> Ret apply(ConsVisitor<? super T, ? extends Ret> visitor);
  
  public abstract Iterator<T> iterator();
  
  /** Whether this is an empty ConsList. */
  public abstract boolean isEmpty();
  
  /** Compute the size of the list.  Note that this is a linear &mdash; not constant-time &mdash; operation. */
  public abstract int size();
  
  /**
   * Compute the size of the list, up to a given bound.  Note that this is a linear &mdash; not 
   * constant-time &mdash; operation.
   */
  public abstract int size(int bound);
  
  /** Return {@code false}: cons lists are not infinite. */
  public boolean isInfinite() { return false; }
  
  /** Return {@code true}: cons lists have a fixed size. */
  public boolean hasFixedSize() { return true; }
  
  /** Return {@code true}: cons lists are immutable. */
  public boolean isStatic() { return true; }
  
  public int compositeHeight() { return size(); }
  public int compositeSize() { return size() + 1; /* add 1 for empty */ }
  
  /** Create an empty list (via {@link Empty#make}) */
  public static <T> Empty<T> empty() { return Empty.<T>make(); }
    
  /** Create a nonempty list (via {@link ConsList#ConsList()}) */
  public static <T> Nonempty<T> cons(T first, ConsList<? extends T> rest) {
    return new Nonempty<T>(first, rest);
  }
  
  /** Create a singleton nonempty list (via {@link ConsList#ConsList()}) */
  public static <T> Nonempty<T> singleton(T value) {
    return new Nonempty<T>(value, Empty.<T>make());
  }
  
  /** Attempt to access the first of the given list (throws an exception in the empty case). */
  public static <T> T first(ConsList<? extends T> list) { return list.apply(ConsVisitor.<T>first()); }
  
  /** Attempt to access the rest of the given list (throws an exception in the empty case). */
  public static <T> ConsList<? extends T> rest(ConsList<? extends T> list) { return list.apply(ConsVisitor.<T>rest()); }
  
  /** Reverse the given list */
  public static <T> ConsList<? extends T> reverse(ConsList<? extends T> list) {
    return list.apply(ConsVisitor.<T>reverse());
  }
  
  /** Append {@code l2} to the end of {@code l1} */
  public static <T> ConsList<? extends T> append(ConsList<? extends T> l1, ConsList<? extends T> l2) {
    return l1.apply(ConsVisitor.append(l2));
  }
  
  /** Filter the given list according to a predicate */
  public static <T> ConsList<? extends T> filter(ConsList<? extends T> list, Predicate<? super T> pred) {
    return list.apply(ConsVisitor.<T>filter(pred));
  }
  
  /** Map the given list according to a lambda */
  public static <S, T> ConsList<? extends T> map(ConsList<? extends S> list, 
                                                 Lambda<? super S, ? extends T> lambda) {
    return list.apply(ConsVisitor.map(lambda));
  }
  
  
  /**
   * The empty variant of a {@code ConsList}.  Instances are made available via {@link #make}.
   */
  public static class Empty<T> extends ConsList<T> {
    
    /** Force use of {@link #make} */
    private Empty() {}
    
    private static final Empty<Void> INSTANCE = new Empty<Void>();
    
    /**
     * Creates an empty list.  The result is a singleton, cast (unsafe formally, but safe in 
     * practice) to the appropriate type
     */
    @SuppressWarnings("unchecked")
    public static <T> Empty<T> make() { return (Empty<T>) INSTANCE; }
    
    /** Invoke the {@code forEmpty} case of a visitor */
    public <Ret> Ret apply(ConsVisitor<? super T, ? extends Ret> visitor) {
      return visitor.forEmpty();
    }
    
    /** Return an empty iterator */
    public Iterator<T> iterator() { return EmptyIterator.make(); }
    
    /** Return {@code true}. */
    public boolean isEmpty() { return true; }
    
    /** Return {@code 0} */
    public int size() { return 0; }
    
    /** Return {@code 0} */
    public int size(int bound) { return 0; }
    
  }
  
  
  /**
   * The nonempty variant of a {@code ConsList}.  Contains a <em>first</em> value of the element
   * type and a <em>rest</em> which is another list.
   */
  public static class Nonempty<T> extends ConsList<T> {
    
    private final T _first;
    private final ConsList<? extends T> _rest;
    
    public Nonempty(T first, ConsList<? extends T> rest) {
      _first = first;
      _rest = rest;
    }
    
    public T first() { return _first; }
    
    public ConsList<? extends T> rest() { return _rest; }
    
    /** Invoke the {@code forNonempty} case of a visitor */
    public <Ret> Ret apply(ConsVisitor<? super T, ? extends Ret> visitor) {
      return visitor.forNonempty(_first, _rest);
    }
    
    /** Create an iterator to traverse the list */
    public Iterator<T> iterator() {
      return new ReadOnlyIterator<T>() {
        private ConsList<? extends T> _current = Nonempty.this;

        public boolean hasNext() { return !_current.isEmpty(); }

        public T next() {
          return _current.apply(new ConsVisitor<T, T>() {
            public T forEmpty() { throw new NoSuchElementException(); }
            public T forNonempty(T first, ConsList<? extends T> rest) { 
              _current = rest;
              return first;
            }
          });
        }
      };
    }
    
    /** Return {@code false}. */
    public boolean isEmpty() { return false; }
    
    /** Return {@code 1 + rest.size()}. */
    public int size() { return 1 + _rest.size(); }
    
    /** Return {@code 1 + rest.size(bound - 1)}, or {@code 0} if {@code bound == 0}. */
    public int size(int bound) {
      if (bound == 0) { return 0; }
      else { return 1 + _rest.size(bound - 1); }
    }
    
  }

}
