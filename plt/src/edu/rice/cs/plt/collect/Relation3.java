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

import edu.rice.cs.plt.tuple.Triple;
import edu.rice.cs.plt.lambda.Predicate3;

/**
 * A set of triples representing a ternary relation.  Relations can be viewed as generalizations
 * of maps in which keys map to sets of values, and the mapping occurs in all directions.
 */
public interface Relation3<T1, T2, T3> extends PredicateSet<Triple<T1, T2, T3>>, Predicate3<T1, T2, T3> {

  /** Whether {@code Triple.make(first, second, third)} appears in the set. */
  public boolean contains(T1 first, T2 second, T3 third);
  /** Add {@code Triple.make(first, second, third)} to the set. */
  public boolean add(T1 first, T2 second, T3 third);
  /** Remove {@code Triple.make(first, second, third)} from the set. */
  public boolean remove(T1 first, T2 second, T3 third);

  /** The set of firsts.  Need not allow mutation, but must reflect subsequent changes. */
  public PredicateSet<T1> firstSet();
  /**
   * A map view of the relation, mapping firsts to sets of (second, third) pairs.  Only keys
   * appearing in the relation are contained by the map -- the value sets are always non-empty.
   * Need not allow mutation, but must reflect subsequent changes.
   */
  public LambdaMap<T1, Relation<T2, T3>> firstMap();
  /** Whether a triple with the given first value appears in the set. */
  public boolean containsFirst(T1 first);
  /**
   * The set of (second, third) pairs corresponding to a specific first.  Need not allow mutation,
   * but must reflect subsequent changes.
   */
  public Relation<T2, T3> matchFirst(T1 first);
  /**
   * The set of (second, third) pairs for which there exists a (first, second, third) triple in the
   * relation.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation<T2, T3> excludeFirsts();

  /** The set of seconds.  Need not allow mutation, but must reflect subsequent changes. */
  public PredicateSet<T2> secondSet();
  /**
   * A map view of the relation, mapping seconds to sets of (first, third) pairs.  Only keys
   * appearing in the relation are contained by the map -- the value sets are always non-empty.
   * Need not allow mutation, but must reflect subsequent changes.
   */
  public LambdaMap<T2, Relation<T1, T3>> secondMap();
  /** Whether a triple with the given second value appears in the set. */
  public boolean containsSecond(T2 second);
  /**
   * The set of (first, third) pairs corresponding to a specific second.  Need not allow mutation,
   * but must reflect subsequent changes.
   */
  public Relation<T1, T3> matchSecond(T2 second);
  /**
   * The set of (first, third) pairs for which there exists a (first, second, third) triple in the
   * relation.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation<T1, T3> excludeSeconds();

  /** The set of thirds.  Need not allow mutation, but must reflect subsequent changes. */
  public PredicateSet<T3> thirdSet();
  /**
   * A map view of the relation, mapping thirds to sets of (first, second) pairs.  Only keys
   * appearing in the relation are contained by the map -- the value sets are always non-empty.
   * Need not allow mutation, but must reflect subsequent changes.
   */
  public LambdaMap<T3, Relation<T1, T2>> thirdMap();
  /** Whether a triple with the given third value appears in the set. */
  public boolean containsThird(T3 third);
  /**
   * The set of (first, second) pairs corresponding to a specific third.  Need not allow mutation,
   * but must reflect subsequent changes.
   */
  public Relation<T1, T2> matchThird(T3 third);
  /**
   * The set of (first, second) pairs for which there exists a (first, second, third) triple in the
   * relation.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation<T1, T2> excludeThirds();

}
