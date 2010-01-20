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
import java.io.Serializable;
import edu.rice.cs.plt.tuple.Pair;

/** An immutable, empty, one-to-one relation. */
public final class EmptyRelation<T1, T2> extends EmptyCollection<Pair<T1, T2>>
                                         implements OneToOneRelation<T1, T2>, Serializable {

  public static final EmptyRelation<Object, Object> INSTANCE = new EmptyRelation<Object, Object>();

  private EmptyRelation() {}
  
  public boolean contains(T1 first, T2 second) { return false; }

  public boolean add(T1 first, T2 second) { throw new UnsupportedOperationException(); }
  public boolean remove(T1 first, T2 second) { throw new UnsupportedOperationException(); }
 
  @SuppressWarnings("unchecked")
  public OneToOneRelation<T2, T1> inverse() { return (EmptyRelation<T2, T1>) INSTANCE; }

  public PredicateSet<T1> firstSet() { return EmptySet.make(); }
  public boolean containsFirst(T1 first) { return false; }
  public PredicateSet<T2> matchFirst(T1 first) { return EmptySet.make(); }
  public PredicateSet<T2> excludeFirsts() { return EmptySet.make(); }

  public PredicateSet<T2> secondSet() { return EmptySet.make(); }
  public boolean containsSecond(T2 second) { return false; }
  public PredicateSet<T1> matchSecond(T2 second) { return EmptySet.make(); }
  public PredicateSet<T1> excludeSeconds() { return EmptySet.make(); }

  public T2 value(T1 first) { return null; }
  public T1 antecedent(T2 second) { return null; }
  public LambdaMap<T1, T2> functionMap() { return EmptyMap.make(); }
  public LambdaMap<T2, T1> injectionMap() { return EmptyMap.make(); }

  public boolean equals(Object o) {
    if (o instanceof Set<?>) { return ((Set<?>) o).isEmpty(); }
    else { return false; }
  }

  public int hashCode() { return 0; }

  /** Return a singleton, cast to the appropriate type. */
  @SuppressWarnings("unchecked") public static <T1, T2> EmptyRelation<T1, T2> make() {
    return (EmptyRelation<T1, T2>) INSTANCE;
  }

}
