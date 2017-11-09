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

import java.util.Iterator;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/** An iterator over an arbitrary number of iterators.  Supports {@link #remove()}. */
public class CollapsedIterator<T> implements Iterator<T>, Composite {
  
  private Iterator<? extends T> _i;
  private Iterator<? extends T> _last; // to support remove()
  private final Iterator<? extends Iterator<? extends T>> _rest;
  
  /** The result traverses {@code i1}, then {@code i2} */
  public CollapsedIterator(Iterator<? extends Iterator<? extends T>> iters) {
    _i = EmptyIterator.make();
    _rest = iters;
    advance();
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_rest) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_rest) + 1; }
  
  public boolean hasNext() { return _i.hasNext(); }

  public T next() {
    T result = _i.next();
    _last = _i;
    advance();
    return result;
  }
  
  public void remove() { _last.remove(); }
  
  private void advance() {
    while (!_i.hasNext() && _rest.hasNext()) { _i = _rest.next(); }
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> CollapsedIterator<T> make(Iterator<? extends Iterator<? extends T>> iters) {
    return new CollapsedIterator<T>(iters);
  }
    
}
