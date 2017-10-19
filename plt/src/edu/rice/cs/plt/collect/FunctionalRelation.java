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
import edu.rice.cs.plt.lambda.Lambda;

/**
 * <p>A functional relation: each first (of type {@code T1}) corresponds to at most one second
 * (of type {@code T2}).  This can be viewed as modeling both a function from firsts to seconds
 * and a one-to-many relationship between seconds and firsts.  Like a {@link java.util.Map}, each
 * "key" (first) maps to a single "value" (second); users may prefer using {@code FunctionalRelation}s,
 * however, where it will also be useful to map from "values" (seconds) to sets of "keys" (firsts).</p>
 *
 * <p>The similarity with maps suggests that it would be useful for this interface to extend {@code Map}.
 * Unfortunately, the {@code hashCode()} conventions for {@code Set}s and {@code Pair}s do not correspond
 * directly to those of {@code Map}s (and, in general, the elements of a {@code Relation} may be {@code Pair}s
 * with arbitrarily-defined {@code hashCode} methods).</p>
 */
public interface FunctionalRelation<T1, T2> extends Relation<T1, T2>, Lambda<T1, T2> {

  /** Produce the second corresponding to {@code first}, or {@code null} if there is none. */
  public T2 value(T1 first);
  /**
   * A map view of the relation, mapping firsts to seconds.    Need not allow mutation, but must
   * reflect subsequent changes.
   */
  public LambdaMap<T1, T2> functionMap();

  /**
   * Add a pair to the set.  If the pair violates the cardinality constraint, throw an exception.
   * @throws IllegalArgumentException  If {@code containsFirst(pair.first())} but not {@code contains(pair)}.
   */
  public boolean add(Pair<T1, T2> pair);
  /**
   * Add {@code Pair.make(first, second)} to the set.  If the pair violates the cardinality constraint,
   * throw an exception.
   * @throws IllegalArgumentException  If {@code containsFirst(first)} but not {@code contains(first, second)}.
   */
  public boolean add(T1 first, T2 second);

  /**
   * Produce the inverse of the relation, derived by swapping the elements of each pair.  The result <em>must</em>
   * be an {@link InjectiveRelation}; however, limitations in Java's overriding rules (possible just a javac bug)
   * prevent this assertion from being expressed in the return type, because a {@link OneToOneRelation} must be
   * allowed to extend both interfaces.  Need not allow mutation, but must reflect subsequent changes.
   */
  public Relation<T2, T1> inverse();

  /**
   * The set of seconds corresponding to a specific first.  Guaranteed to have size 0 or 1.
   * Need not allow mutation, but must reflect subsequent changes.
   */
  public PredicateSet<T2> matchFirst(T1 first);
  
}
