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
 * Contains, for some value {@code size}, the first {@code size} elements of a nested iterable.
 * (If the nested list has less than {@code size} elements, this iterable is identical.)
 * Changes made to the underlying list are reflected here.
 */
public class TruncatedIterable<T> extends AbstractIterable<T>
                                  implements SizedIterable<T>, Composite, Serializable {
  
  private final Iterable<? extends T> _iterable;
  protected final int _size;
  
  public TruncatedIterable(Iterable<? extends T> iterable, int size) {
    if (size < 0) { throw new IllegalArgumentException("size < 0"); }
    _iterable = iterable;
    _size = size;
  }

  public int compositeHeight() { return ObjectUtil.compositeHeight((Object) _iterable) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize((Object) _iterable) + 1; }
    
  public TruncatedIterator<T> iterator() {
    return new TruncatedIterator<T>(_iterable.iterator(), _size);
  }
  
  public boolean isEmpty() { return (_size == 0) || IterUtil.isEmpty(_iterable); }

  /**
   * Return {@code size}, unless the nested iterable is smaller than {@code size}; in that
   * case, returns the iterable's size.
   */
  public int size() { return IterUtil.sizeOf(_iterable, _size); }
  
  public int size(int bound) { return IterUtil.sizeOf(_iterable, _size <= bound ? _size : bound); }
  
  public boolean isInfinite() { return false; }
    
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_iterable); }
  
  public boolean isStatic() { return IterUtil.isStatic(_iterable); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> TruncatedIterable<T> make(Iterable<? extends T> iterable, int size) {
    return new TruncatedIterable<T>(iterable, size);
  }
  
  /**
   * Create a {@code TruncatedIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate traversal of the list.
   */
  public static <T> SnapshotIterable<T> makeSnapshot(Iterable<? extends T> iterable, int size) { 
    return new SnapshotIterable<T>(new TruncatedIterable<T>(iterable, size));
  }
}
