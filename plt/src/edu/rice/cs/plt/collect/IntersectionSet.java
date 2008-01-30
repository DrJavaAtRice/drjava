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
import java.util.AbstractSet;
import java.util.Iterator;
import edu.rice.cs.plt.iter.FilteredIterator;
import edu.rice.cs.plt.lambda.Predicate;

/**
 * The intersection of two sets, lazily constructed and updated dynamically.
 */
public class IntersectionSet<E> extends AbstractSet<E> {
  private final Set<?> _set1;
  private final Set<? extends E> _set2;
  
  /**
   * To guarantee that the intersection is a set of {@code E}, only <em>one</em> of the arguments must
   * be a set of {@code E}s.  We arbitrarily choose {@code set2}.  For best performance of {@link #iterator},
   * {@link #size}, and {@link #isEmpty}, {@code set2} should be the smaller of the two sets (this is not 
   * handled automatically because calculating sizes may be expensive).
   */
  public IntersectionSet(Set<?> set1, Set<? extends E> set2) {
    _set1 = set1;
    _set2 = set2;
  }
  
  /** Traversing is linear in the size of {@code set2}. */
  public Iterator<E> iterator() {
    Predicate<Object> filter = CollectUtil.containsPredicate(_set1);
    return new FilteredIterator<E>(_set2.iterator(), filter);
  }
  
  /** Linear in the size of {@code set2}. */
  public int size() {
    int result = 0;
    for (E elt : this) { result++; }
    return result;
  }
  
  /** Linear in the size of {@code set2}. */
  public boolean isEmpty() {
    if (_set1.isEmpty() || _set2.isEmpty()) { return true; }
    else if (_set1 == _set2) { return false; }
    else {
      for (Object obj : _set2) {
        if (_set1.contains(obj)) { return false; }
      }
      return true;
    }
  }
  
  public boolean contains(Object o) {
    return _set1.contains(o) || _set2.contains(o);
  }
  
  // inherit default implementation of containsAll
  
}
