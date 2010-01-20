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
 * An enumeration of all permutations of the given list.  The size of the enumeration, where 
 * the original list has size n, is n! (and is thus guaranteed to be >= 1).  The order of the 
 * results is "lexicographical" where each element of the original list is taken to 
 * lexicographically precede all of its successors.  (Thus, the original list is
 * the first to be returned, and a reversed list is the last.)  Of course, due to the factorial
 * complexity of enumerating all permutations, this class is probably not suitable
 * for applications in which n is unbounded (or just intractably large).
 * 
 * @param <T>  The element type of the permuted lists; note that the iterator returns
 *             {@code Iterable<T>}s, not {@code T}s.
 */
public class PermutationIterable<T> extends AbstractIterable<Iterable<T>> 
                                    implements SizedIterable<Iterable<T>>, OptimizedLastIterable<Iterable<T>>,
                                               Composite, Serializable {
  
  private final Iterable<? extends T> _original;
  
  public PermutationIterable(Iterable<? extends T> original) { _original = original; }
  public PermutationIterator<T> iterator() { return new PermutationIterator<T>(_original); }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight((Object) _original) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize((Object) _original) + 1; }
    
  public boolean isEmpty() { return false; }

  public int size() { return size(Integer.MAX_VALUE); }
  
  public int size(int bound) {
    int n = IterUtil.sizeOf(_original, bound);
    long result = 1; // won't overflow -- worst case is 2^31 * 2^31 = 2^62 < 2^63
    for (int i = 2; i < n && result < bound; i++) { result *= i; }
    return result <= bound ? (int) result : bound;
  }
  
  public boolean isInfinite() { return IterUtil.isInfinite(_original); }
  
  public boolean hasFixedSize() { return IterUtil.hasFixedSize(_original); }
  
  public boolean isStatic() { return IterUtil.isStatic(_original); }
  
  public Iterable<T> last() { return IterUtil.reverse(_original); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> PermutationIterable<T> make(Iterable<? extends T> original) {
    return new PermutationIterable<T>(original);
  }
  
  /**
   * Create a {@code PermutationIterable} and wrap it in a {@code SnapshotIterable}, forcing
   * immediate evaluation of the permutations.
   */
  public static <T> SnapshotIterable<Iterable<T>> makeSnapshot(Iterable<? extends T> original) {
    return new SnapshotIterable<Iterable<T>>(new PermutationIterable<T>(original));
  }
  
}
