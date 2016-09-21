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

package edu.rice.cs.plt.iter;

import java.io.Serializable;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * Wraps an iterable in an immutable interface, thus allowing internal data structures to be treated 
 * by clients as iterables without allowing access (via casting) to their mutating methods.  
 * Also ensures that the {@link java.util.Iterator#remove()} method of the provided Iterator is not 
 * supported.  Note that only only <em>this</em> interface with the data is immutable --
 * if the original data structure is mutable, a client with direct access to that structure can
 * still mutate it.  To guarantee that no mutation will take place, use a {@link SnapshotIterable} instead,
 * which makes an immutable copy.
 */
public class ImmutableIterable<T> extends AbstractIterable<T>
                                  implements SizedIterable<T>, OptimizedLastIterable<T>, Composite, Serializable {
  
  private final Iterable<? extends T> _iterable;
  
  public ImmutableIterable(Iterable<? extends T> iterable) { _iterable = iterable; }
  public ImmutableIterator<T> iterator() { return new ImmutableIterator<T>(_iterable.iterator()); }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight((Object) _iterable) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize((Object) _iterable) + 1; }
  
  public boolean isEmpty() { return IterUtil.isEmpty(_iterable); }
  public int size() { return IterUtil.sizeOf(_iterable); }
  public int size(int bound) { return IterUtil.sizeOf(_iterable, bound); }
  public boolean isInfinite() { return IterUtil.isInfinite(_iterable); }
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_iterable); }
  public boolean isStatic() { return IterUtil.isStatic(_iterable); }
  
  public T last() { return IterUtil.last(_iterable); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ImmutableIterable<T> make(Iterable<? extends T> iterable) {
    return new ImmutableIterable<T>(iterable);
  }
  
}
