/*BEGIN_COPYRIGHT_BLOCK*
 * 
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

import java.util.Iterator;
import edu.rice.cs.plt.tuple.Pair;
import edu.rice.cs.plt.iter.SizedIterable;

/**
 * Maintains an index mapping values of one type to sets of another.  Useful in implementations
 * of Relations.
 */
public interface RelationIndex<K, V> extends SizedIterable<Pair<K, V>> {
  /** Whether the given key-value mapping occurs. */
  public boolean contains(Object key, Object value);
  /**
   * A dynamically-updating view of all keys mapping to at least one value.  Mutation (removal),
   * if supported, will be automatically reflected in the relation being indexed.
   */
  public PredicateSet<K> keys();
  /**
   * A dynamically-updating view of all values matching {@code key}.  May be empty. Mutation,
   * if supported, will be automatically reflected in the relation being indexed.
   */
  public PredicateSet<V> match(K key);
  /** Iterates through all key-value pairs in the index. */
  public Iterator<Pair<K, V>> iterator();
  
  /**
   * Requests that the index be updated to reflect the addition of the given key/value pair.
   * Assumes the pair is not already present.
   */
  public void added(K key, V value);
  /**
   * Requests that the index be updated to reflect the removal of the given key/value pair.
   * Assumes the pair is present.
   */
  public void removed(K key, V value);
  /**
   * Requests that the index be cleared to reflect the current state of the relation.
   * Assumes the index is non-empty.
   */
  public void cleared();
}
