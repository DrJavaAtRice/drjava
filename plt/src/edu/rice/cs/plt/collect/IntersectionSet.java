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
import edu.rice.cs.plt.iter.IterUtil;

/**
 * The intersection of two sets, lazily constructed and updated dynamically.  This improves on
 * a general {@code FilteredSet} by taking advantage of the fact that the predicate used for 
 * filtering is also a set.
 */
public class IntersectionSet<E> extends FilteredSet<E> {
  
  /**
   * To guarantee that the intersection is a set of {@code E}, only <em>one</em> of the arguments must
   * be a set of {@code E}s.  We arbitrarily choose {@code set2}.  For best performance of {@link #iterator},
   * {@link #size}, and {@link #isEmpty}, {@code set2} should be the smaller of the two sets (this is not 
   * handled automatically because calculating sizes may be expensive).
   */
  public IntersectionSet(Set<?> set1, Set<? extends E> set2) {
    super(set2, CollectUtil.asPredicateSet(set1));
  }
  
  public boolean isInfinite() {
    return ((PredicateSet<?>) _pred).isInfinite() && IterUtil.isInfinite(_set);
  }
  
  public boolean hasFixedSize() {
    return ((PredicateSet<?>) _pred).hasFixedSize() && IterUtil.hasFixedSize(_set);
  }
  
  public boolean isStatic() {
    return ((PredicateSet<?>) _pred).isStatic() && IterUtil.isStatic(_set);
  }
  
  @Override public boolean isEmpty() {
    return ((Set<?>) _pred).isEmpty() || (_set != _pred && super.isEmpty());
  }
  
}
