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
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.iter.ComposedIterator;

/**
 * The union of two sets, lazily constructed and updated dynamically.
 */
public class UnionSet<E> extends AbstractSet<E> {
  private final Set<? extends E> _set1;
  private final Set<? extends E> _set2;
  private final Set<? extends E> _set2Extras;
  
  /**
   * For best performance of {@link #size}, {@code set2} should be the smaller
   * of the two sets (this is not handled automatically because calculating sizes may be expensive).
   */
  public UnionSet(Set<? extends E> set1, Set<? extends E> set2) {
    _set1 = set1;
    _set2 = set2;
    _set2Extras = new ComplementSet<E>(set2, set1);
  }
  
  public Iterator<E> iterator() {
    return new ImmutableIterator<E>(new ComposedIterator<E>(_set1.iterator(), _set2Extras.iterator()));
  }
  
  /** Linear in the size of {@code set2}. */
  public int size() {
    return _set1.size() + _set2Extras.size();
  }
  
  public boolean isEmpty() {
    return _set1.isEmpty() && _set2.isEmpty();
  }
  
  public boolean contains(Object o) {
    return _set1.contains(o) || _set2.contains(o);
  }
  
  // inherit default implementation of containsAll
  
}
