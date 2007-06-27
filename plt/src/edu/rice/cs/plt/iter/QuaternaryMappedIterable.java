/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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
import edu.rice.cs.plt.lambda.Lambda4;

/**
 * An Iterable containing the results of some quaternary operation on four input lists 
 * (assumed to always have the same size)
 * 
 * @param T1  The element type of the first input list
 * @param T2  The element type of the second input list
 * @param T3  The element type of the third input list
 * @param T4  The element type of the fourth input list
 * @param R  The element type of the result list
 */
public class QuaternaryMappedIterable<T1, T2, T3, T4, R>  extends AbstractIterable<R>
                                                          implements SizedIterable<R>, Serializable {
  
  private final Iterable<? extends T1> _source1;
  private final Iterable<? extends T2> _source2;
  private final Iterable<? extends T3> _source3;
  private final Iterable<? extends T4> _source4;
  private final Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> _map;
  
  public QuaternaryMappedIterable(Iterable<? extends T1> source1, Iterable<? extends T2> source2,
                                  Iterable<? extends T3> source3, Iterable<? extends T4> source4,
                                  Lambda4<? super T1, ? super T2, 
                                          ? super T3, ? super T4, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _source3 = source3;
    _source4 = source4;
    _map = map;
  }
  
  public QuaternaryMappedIterator<T1, T2, T3, T4, R> iterator() { 
    return new QuaternaryMappedIterator<T1, T2, T3, T4, R>(_source1.iterator(), _source2.iterator(), 
                                                           _source3.iterator(), _source4.iterator(),
                                                           _map);
  }
  
  public int size() { return IterUtil.sizeOf(_source1); }
  public int size(int bound) { return IterUtil.sizeOf(_source1, bound); }
  public boolean isFixed() { return IterUtil.isFixed(_source1); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, R> QuaternaryMappedIterable<T1, T2, T3, T4, R> 
    make(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
         Iterable<? extends T3> source3, Iterable<? extends T4> source4,
         Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> map) {
    return new QuaternaryMappedIterable<T1, T2, T3, T4, R>(source1, source2, source3, source4, map);
  }
  
  /**
   * Create a {@code QuaternaryMappedIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the mapping.
   */
  public static <T1, T2, T3, T4, R> SnapshotIterable<R> 
    makeSnapshot(Iterable<? extends T1> source1, Iterable<? extends T2> source2, 
                 Iterable<? extends T3> source3, Iterable<? extends T4> source4,
                 Lambda4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> map) {
    return new SnapshotIterable<R>(new QuaternaryMappedIterable<T1, T2, T3, T4, R>(source1, source2, 
                                                                                   source3, source4, map));
  }
  
}
