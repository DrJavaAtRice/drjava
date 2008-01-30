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

import java.util.Set;
import edu.rice.cs.plt.tuple.Pair;

/**
 * A set of pairs representing a binary relation.  Additional methods provide a convenient
 * (and potentially optimized) interface for accessing sets of firsts (values of type {@code T1})
 * and seconds (values of type {@code T2}) based on a given key.  Thus, a relation can also
 * be viewed as a generalization of a map in which keys map to sets of values, and this mapping
 * occurs in both directions.
 */
public interface Relation<T1, T2> extends Set<Pair<T1, T2>> {

  public boolean contains(T1 first, T2 second);
  public boolean add(T1 first, T2 second);
  public boolean remove(T1 first, T2 second);

  /** The set of firsts.  Does not allow mutation. */
  public Set<T1> firstSet();
  public boolean containsFirst(T1 first);
  /** The set of seconds corresponding to a specific first.  Does not allow mutation. */
  public Set<T2> getSeconds(T1 first);

  /** The set of seconds.  Does not allow mutation. */
  public Set<T2> secondSet();
  public boolean containsSecond(T2 second);
  /** The set of firsts corresponding to a specific second.  Does not allow mutation. */
  public Set<T1> getFirsts(T2 second);
  
}
