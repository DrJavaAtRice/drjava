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
import edu.rice.cs.plt.lambda.Predicate;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * An Iterable containing all the values in the provided Iterable for which the provided
 * Predicate holds.  Because the size cannot be determined without traversing the list,
 * does not implement {@code SizedIterable}.
 */
public class FilteredIterable<T> extends AbstractIterable<T>
                                 implements Iterable<T>, Composite, Serializable {
  
  private Iterable<? extends T> _iterable;
  private Predicate<? super T> _predicate;
  
  public FilteredIterable(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
    _iterable = iterable;
    _predicate = predicate;
  }
  
  public FilteredIterator<T> iterator() { 
    return new FilteredIterator<T>(_iterable.iterator(), _predicate);
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight((Object) _iterable) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize((Object) _iterable) + 1; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> FilteredIterable<T> make(Iterable<? extends T> iterable, 
                                             Predicate<? super T> predicate) {
    return new FilteredIterable<T>(iterable, predicate);
  }
  
  /**
   * Create a {@code FilteredIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the filter.
   */
  public static <T> SnapshotIterable<T> makeSnapshot(Iterable<? extends T> iterable,
                                                     Predicate<? super T> predicate) {
    return new SnapshotIterable<T>(new FilteredIterable<T>(iterable, predicate));
  }
}
