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

import edu.rice.cs.plt.tuple.Pair;

/**
 * A relation that delegates all operations to a wrapped relation.  Subclasses can be defined that
 * override a few of the methods, while maintaining the default delegation behavior in most cases.
 * Subclasses can also invoke the overridden methods in {@link java.util.AbstractCollection} to use the
 * default implementations there by invoking, for example, {@link #abstractCollectionAddAll}
 * (see {@link java.util.AbstractCollection} for details on the default implementations).
 */
public class DelegatingRelation<T1, T2> extends DelegatingSet<Pair<T1, T2>> implements Relation<T1, T2> {
  protected final Relation<T1, T2> _delegate; // field is redundant, but prevents a lot of useless casts 
  public DelegatingRelation(Relation<T1, T2> delegate) { super(delegate); _delegate = delegate; }
  
  public int size(int bound) { return _delegate.size(bound); }
  public boolean isInfinite() { return _delegate.isInfinite(); }
  public boolean hasFixedSize() { return _delegate.hasFixedSize(); }
  public boolean isStatic() { return _delegate.isStatic(); }

  public boolean contains(T1 first, T2 second) { return _delegate.contains(first, second); }
  public boolean add(T1 first, T2 second) { return _delegate.add(first, second); }
  public boolean remove(T1 first, T2 second) { return _delegate.remove(first, second); }
  public Relation<T2, T1> inverse() { return _delegate.inverse(); }
  public PredicateSet<T1> firstSet() { return _delegate.firstSet(); }
  public boolean containsFirst(T1 first) { return _delegate.containsFirst(first); }
  public PredicateSet<T2> matchFirst(T1 first) { return _delegate.matchFirst(first); }
  public PredicateSet<T2> excludeFirsts() { return _delegate.excludeFirsts(); }
  public PredicateSet<T2> secondSet() { return _delegate.secondSet(); }
  public boolean containsSecond(T2 second) { return _delegate.containsSecond(second); }
  public PredicateSet<T1> matchSecond(T2 second) { return _delegate.matchSecond(second); }
  public PredicateSet<T1> excludeSeconds() { return _delegate.excludeSeconds(); }
  
}
