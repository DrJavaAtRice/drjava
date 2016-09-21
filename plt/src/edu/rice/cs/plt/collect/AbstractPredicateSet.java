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

import java.util.AbstractSet;

/**
 * An extension of AbstractSet that implements the PredicateSet interface.  Subclasses must
 * define {@link #contains}, {@link #iterator}, {@link #isInfinite}, {@link #hasFixedSize},
 * and {@link #isStatic}.  Mutable sets must also implement {@link #add} and the iterator's
 * {@link java.util.Iterator#remove} method.  Subclasses may also find it useful to override
 * {@link #size(int)}, {@link #isEmpty}, {@link #remove}, and {@link #clear} for improved
 * efficiency.
 */
public abstract class AbstractPredicateSet<T> extends AbstractSet<T> implements PredicateSet<T> {
  
  /**
   * Test whether the set contains an object.  Overridden here to force subclasses to provide an
   * implementation.  The default implementation ({@link AbstractSet#contains}) is a linear search, 
   * which is almost always unreasonable for a set.
   */
  @Override public abstract boolean contains(Object o);
  
  /** Returns {@code size(1) == 0}. */
  @Override public boolean isEmpty() { return size(1) == 0; }
  
  /** Returns {@code size(Integer.MAX_VALUE)}. */
  public int size() { return size(Integer.MAX_VALUE); }
  
  /** Computes the size by traversing the iterator (requires linear time). */
  public int size(int bound) {
    int result = 0;
    // javac 1.5.0_16 crashes with this annotation
    //for (@SuppressWarnings("unused") T elt : this) { result++; if (result == bound) break; }
    for (T elt : this) { result++; if (result == bound) break; }
    return result;
  }
  
}
