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
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * An Iterable containing all the values in the provided Iterable after applying some 
 * specified transformation.
 * 
 * @param <S>  The element type of the original list
 * @param <T>  The element type of the transformed list
 */
public class MappedIterable<S, T> extends AbstractIterable<T>
                                  implements SizedIterable<T>, OptimizedLastIterable<T>,
                                             Composite, Serializable {
  
  private final Iterable<? extends S> _source;
  private final Lambda<? super S, ? extends T> _map;
  
  public MappedIterable(Iterable<? extends S> source, Lambda<? super S, ? extends T> map) {
    _source = source;
    _map = map;
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight((Object) _source) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize((Object) _source) + 1; }
  
  public MappedIterator<S, T> iterator() { 
    return new MappedIterator<S, T>(_source.iterator(), _map);
  }
  
  public boolean isEmpty() { return IterUtil.isEmpty(_source); }
  public int size() { return IterUtil.sizeOf(_source); }
  public int size(int bound) { return IterUtil.sizeOf(_source, bound); }
  public boolean isInfinite() { return IterUtil.isInfinite(_source); }
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_source); }
  /** Always false: results of a lambda may be arbitrary. */
  public boolean isStatic() { return false; }
  
  public T last() { return _map.value(IterUtil.last(_source)); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <S, T> MappedIterable<S, T> make(Iterable<? extends S> source, 
                                                 Lambda<? super S, ? extends T> map) {
    return new MappedIterable<S, T>(source, map);
  }
  
  /**
   * Create a {@code MappedIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the mapping.
   */
  public static <S, T> SnapshotIterable<T> 
    makeSnapshot(Iterable<? extends S> source, Lambda<? super S, ? extends T> map) {
    return new SnapshotIterable<T>(new MappedIterable<S, T>(source, map));
  }
  
}
