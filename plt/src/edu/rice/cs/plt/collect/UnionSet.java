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
import java.util.Iterator;
import java.io.Serializable;
import edu.rice.cs.plt.iter.ImmutableIterator;
import edu.rice.cs.plt.iter.ComposedIterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * The union of two sets, lazily constructed and updated dynamically.
 */
public class UnionSet<E> extends AbstractPredicateSet<E> implements Composite, Serializable {
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
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_set1, _set2) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_set1, _set2) + 1; }
  
  public boolean contains(Object o) {
    return _set1.contains(o) || _set2.contains(o);
  }
  
  public Iterator<E> iterator() {
    return new ImmutableIterator<E>(new ComposedIterator<E>(_set1.iterator(), _set2Extras.iterator()));
  }
  
  public boolean isInfinite() { return IterUtil.isInfinite(_set1) || IterUtil.isInfinite(_set2); }
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_set1) && IterUtil.hasFixedSize(_set2); }
  public boolean isStatic() { return IterUtil.isStatic(_set1) && IterUtil.isStatic(_set2); }
  
  
  /** Linear in the size of {@code set2}. */
  @Override public int size() {
    return _set1.size() + _set2Extras.size();
  }
  
  @Override public int size(int bound) {
    int size1 = IterUtil.sizeOf(_set1, bound);
    int bound2 = bound - size1;
    int size2 = (bound2 > 0) ? IterUtil.sizeOf(_set2Extras, bound) : 0;
    return size1 + size2;
  }
  
  @Override public boolean isEmpty() {
    return _set1.isEmpty() && _set2.isEmpty();
  }
  
}
