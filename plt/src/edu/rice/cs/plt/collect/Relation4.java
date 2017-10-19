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
import java.util.Map;
import edu.rice.cs.plt.tuple.Quad;
import edu.rice.cs.plt.lambda.Predicate4;

/**
 * A set of quads representing a quaternary relation.  Relations can be viewed as generalizations
 * of maps in which keys map to sets of values, and the mapping occurs in all directions.
 */
public interface Relation4<T1, T2, T3, T4> extends PredicateSet<Quad<T1, T2, T3, T4>>,
                                                   Predicate4<T1, T2, T3, T4> {

  /** Whether {@code Quad.make(first, second, third, fourth)} appears in the set. */
  public boolean contains(T1 first, T2 second, T3 third, T4 fourth);
  /** Add {@code Quad.make(first, second, third, fourth)} to the set. */
  public boolean add(T1 first, T2 second, T3 third, T4 fourth);
  /** Remove {@code Quad.make(first, second, third, fourth)} from the set. */
  public boolean remove(T1 first, T2 second, T3 third, T4 fourth);

  /** The set of firsts.  Need not allow mutation, but must reflect subsequent changes. */
  public Set<T1> firstSet();
  /**
   * A map view of the relation, mapping firsts to sets of (second, third, fourth) triples.
   * Only keys appearing in the relation are contained by the map -- the value sets are always
   * non-empty.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Map<T1, Relation3<T2, T3, T4>> firstMap();
  /** Whether a quad with the given first value appears in the set. */
  public boolean containsFirst(T1 first);
  /**
   * The set of (second, third, fourth) triples corresponding to a specific first.  Need not
   * allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T2, T3, T4> matchFirst(T1 first);
  /**
   * The set of (second, third, fourth) triples for which there exists a (first, second, third, fourth) 
   * quad in the relation.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T2, T3, T4> excludeFirsts();

  /** The set of seconds.  Need not allow mutation, but must reflect subsequent changes. */
  public Set<T2> secondSet();
  /**
   * A map view of the relation, mapping seconds to sets of (first, third, fourth) triples.
   * Only keys appearing in the relation are contained by the map -- the value sets are always
   * non-empty.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Map<T2, Relation3<T1, T3, T4>> secondMap();
  /** Whether a quad with the given second value appears in the set. */
  public boolean containsSecond(T2 second);
  /**
   * The set of (first, third, fourth) triples corresponding to a specific second.  Need not
   * allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T1, T3, T4> matchSecond(T2 second);
  /**
   * The set of (first, third, fourth) triples for which there exists a (first, second, third, fourth) 
   * quad in the relation.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T1, T3, T4> excludeSeconds();

  /** The set of thirds.  Need not allow mutation, but must reflect subsequent changes. */
  public Set<T3> thirdSet();
  /**
   * A map view of the relation, mapping thirds to sets of (first, second, fourth) triples.
   * Only keys appearing in the relation are contained by the map -- the value sets are always
   * non-empty.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Map<T3, Relation3<T1, T2, T4>> thirdMap();
  /** Whether a quad with the given third value appears in the set. */
  public boolean containsThird(T3 third);
  /**
   * The set of (first, second, fourth) triples corresponding to a specific third.  Need not
   * allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T1, T2, T4> matchThird(T3 third);
  /**
   * The set of (first, second, fourth) triples for which there exists a (first, second, third, fourth) 
   * quad in the relation.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T1, T2, T4> excludeThirds();

  /** The set of fourths.  Need not allow mutation, but must reflect subsequent changes. */
  public Set<T4> fourthSet();
  /**
   * A map view of the relation, mapping fourths to sets of (first, second, third) triples.
   * Only keys appearing in the relation are contained by the map -- the value sets are always
   * non-empty.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Map<T4, Relation3<T1, T2, T3>> fourthMap();
  /** Whether a quad with the given fourth value appears in the set. */
  public boolean containsFourth(T4 fourth);
  /**
   * The set of (first, second, third) triples corresponding to a specific fourth.  Need not
   * allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T1, T2, T3> matchFourth(T4 fourth);
  /**
   * The set of (first, second, third) triples for which there exists a (first, second, third, fourth)  
   * quad in the relation.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation3<T1, T2, T3> excludeFourths();

}
