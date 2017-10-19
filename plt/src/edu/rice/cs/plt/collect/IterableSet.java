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

import java.io.Serializable;
import java.util.Iterator;
import edu.rice.cs.plt.iter.NoDuplicatesIterator;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * A Set wrapping an Iterable.  Allows iterables to be viewed as sets without creating a
 * copy, and reflects subsequent changes made to the iterable.  Does not support mutation.
 * Since duplicate elements must be lazily skipped, most operations (including {@code size()}
 * and {@code contains}) require a traversal of the iterable, and thus have relatively poor
 * performance.
 */
public class IterableSet<E> extends AbstractPredicateSet<E> implements Composite, Serializable {
  
  private final Iterable<? extends E> _iter;
  
  public IterableSet(Iterable<? extends E> iter) { _iter = iter; }
  
  public boolean contains(Object o) {
    return IterUtil.contains(_iter, o);
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_iter) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_iter) + 1; }
  
  /**
   * Must store all previously-seen elements in order to skip duplicates.  As a result, requires
   * space proportional to the number of {@code next()} invocations.
   */
  public Iterator<E> iterator() { return new NoDuplicatesIterator<E>(_iter.iterator()); }
  
  public boolean isInfinite() { return IterUtil.isInfinite(_iter); }
  
  public boolean hasFixedSize() {
    // _iter may have fixed size but be changed to have more/fewer duplicates
    return IterUtil.isStatic(_iter) ||
           IterUtil.isInfinite(_iter) && IterUtil.hasFixedSize(_iter);
  }
  
  public boolean isStatic() { return IterUtil.isStatic(_iter); }
  
  @Override public boolean isEmpty() { return IterUtil.isEmpty(_iter); }

}
