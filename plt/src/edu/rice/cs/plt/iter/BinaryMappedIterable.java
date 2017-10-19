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
import edu.rice.cs.plt.lambda.Lambda2;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;


/**
 * An Iterable containing the results of some binary operation on two input lists 
 * (assumed to always have the same size).
 * 
 * @param <T1>  The element type of the first input list
 * @param <T2>  The element type of the second input list
 * @param <R>  The element type of the result list
 */
public class BinaryMappedIterable<T1, T2, R> extends AbstractIterable<R> 
                                             implements SizedIterable<R>, OptimizedLastIterable<R>,
                                                        Composite, Serializable {
  
  private final Iterable<? extends T1> _source1;
  private final Iterable<? extends T2> _source2;
  private final Lambda2<? super T1, ? super T2, ? extends R> _map;
  
  public BinaryMappedIterable(Iterable<? extends T1> source1, Iterable<? extends T2> source2,
                              Lambda2<? super T1, ? super T2, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _map = map;
  }
  
  public BinaryMappedIterator<T1, T2, R> iterator() { 
    return new BinaryMappedIterator<T1, T2, R>(_source1.iterator(), _source2.iterator(), _map);
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_source1, _source2) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_source1, _source2) + 1; }
  
  public boolean isEmpty() { return IterUtil.isEmpty(_source1); }
  public int size() { return IterUtil.sizeOf(_source1); }
  public int size(int bound) { return IterUtil.sizeOf(_source1, bound); }
  public boolean isInfinite() { return IterUtil.isInfinite(_source1); }
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_source1); }
  /** Always false: results of a lambda may be arbitrary. */
  public boolean isStatic() { return false; }
  
  public R last() { return _map.value(IterUtil.last(_source1), IterUtil.last(_source2)); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, R> BinaryMappedIterable<T1, T2, R> 
    make(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
         Lambda2<? super T1, ? super T2, ? extends R> map) {
    return new BinaryMappedIterable<T1, T2, R>(source1, source2, map);
  }
  
  /**
   * Create a {@code BinaryMappedIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the mapping.
   */
  public static <T1, T2, R> SnapshotIterable<R> 
    makeSnapshot(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
                 Lambda2<? super T1, ? super T2, ? extends R> map) {
    return new SnapshotIterable<R>(new BinaryMappedIterable<T1, T2, R>(source1, source2, map));
  }
  
}
