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

import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.io.Serializable;
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.tuple.Option;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.FilteredIterator;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A set that contains only those elements contained by both the given set and the given
 * predicate.  Note that size operations will take linear time.
 */
public class FilteredSet<T> extends AbstractPredicateSet<T> implements Composite, Serializable {

  protected final Set<? extends T> _set;
  protected final Predicate<? super T> _pred;
  
  public FilteredSet(Set<? extends T> set, Predicate<? super T> predicate) {
    _set = set;
    _pred = predicate;
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_set, _pred) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_set, _pred) + 1; }
  
  public boolean contains(Object o) {
    Option<T> cast = CollectUtil.castIfContains(_set, o);
    if (cast.isSome()) { return _pred.contains(cast.unwrap()); }
    else { return false; }
  }
  
  @Override public boolean containsAll(Collection<?> objs) {
    if (_set.containsAll(objs)) {
      // all elements must be subtypes of T (strictly speaking, the type parameter of this
      // collection may not be a subtype of T, despite all the elements having that type; but
      // that doesn't matter as far as the Iterable interface is concerned
      @SuppressWarnings("unchecked") Iterable<? extends T> iter = (Iterable<? extends T>) objs;
      return IterUtil.and(iter, _pred);
    }
    else { return false; }
  }
  
  /** Traversing the iterator is linear in the size of the original set. */
  public Iterator<T> iterator() { return new FilteredIterator<T>(_set.iterator(), _pred); }
  
  @Override public boolean isEmpty() { return _set.isEmpty() || super.isEmpty(); }
  public boolean isInfinite() { return false; }
  public boolean hasFixedSize() { return false; }
  public boolean isStatic() { return false; }
  
}
