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
import edu.rice.cs.plt.lambda.Predicate2;

/**
 * A set of pairs representing a binary relation.  Relations can be viewed as generalizations
 * of maps in which keys map to sets of values, and the mapping occurs in both directions.
 */
public interface Relation<T1, T2> extends PredicateSet<Pair<T1, T2>>, Predicate2<T1, T2> {

  /** Whether the given object appears in the set. */
  public boolean contains(Object o);
  /** Whether {@code Pair.make(first, second)} appears in the set. */
  public boolean contains(T1 first, T2 second);
  /**
   * Add {@code Pair.make(p.first(), p.second())} to the set.  (That is, the pair that is
   * added is not an instance of some subclass of Pair.)
   */
  public boolean add(Pair<T1, T2> pair);
  /** Add {@code Pair.make(first, second)} to the set. */
  public boolean add(T1 first, T2 second);
  /**
   * If {@code o} is a pair, remove {@code Pair.make(o.first(), o.second())} from the set.
   * (That is, equality is always defined according to the Pair class's equals method, not 
   * that of some subclass.)
   */
  public boolean remove(Object o);
  /** Remove {@code Pair.make(first, second)} from the set. */
  public boolean remove(T1 first, T2 second);
  
  /**
   * Produce the inverse of the relation, derived by swapping the elements of each pair.  Need not
   * allow mutation, but must reflect subsequent changes.
   */
  public Relation<T2, T1> inverse();

  /** The set of firsts.  Need not allow mutation, but must reflect subsequent changes. */
  public PredicateSet<T1> firstSet();
  /** Whether a pair with the given first value appears in the set. */
  public boolean containsFirst(T1 first);
  /**
   * The set of seconds corresponding to a specific first.  Need not allow mutation, but must
   * reflect subsequent changes.
   */
  public PredicateSet<T2> matchFirst(T1 first);
  /**
   * The set of seconds for which there exists a (first, second) pair in the
   * relation.  Equivalent to {@link #secondSet}, but defined redundantly for consistency
   * with higher-arity relations.  Need not allow mutation, but must reflect subsequent changes.
   */
  public PredicateSet<T2> excludeFirsts();

  /** The set of seconds.  Need not allow mutation, but must reflect subsequent changes. */
  public PredicateSet<T2> secondSet();
  /** Whether a pair with the given second value appears in the set. */
  public boolean containsSecond(T2 second);
  /**
   * The set of firsts corresponding to a specific second.  Need not allow mutation, but must
   * reflect subsequent changes.
   */
  public PredicateSet<T1> matchSecond(T2 second);
  /**
   * The set of firsts for which there exists a (first, second) pair in the
   * relation.  Equivalent to {@link #firstSet}, but defined redundantly for consistency
   * with higher-arity relations.  Need not allow mutation, but must reflect subsequent changes.
   */
  public PredicateSet<T1> excludeSeconds();
  
}
