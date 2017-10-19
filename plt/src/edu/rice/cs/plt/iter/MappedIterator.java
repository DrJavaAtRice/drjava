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
import edu.rice.cs.plt.lambda.Lambda;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/**
 * An Iterator that applies a provided mapping lambda to each member of a source iterator.
 * {@link #remove()} simply delegates to the source iterator.
 */
public class MappedIterator<S, T> implements Iterator<T>, Composite {

  private final Iterator<? extends S> _source;
  private final Lambda<? super S, ? extends T> _map;
  
  public MappedIterator(Iterator<? extends S> source, Lambda<? super S, ? extends T> map) {
    _source = source;
    _map = map;
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_source) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_source) + 1; }
    
  public boolean hasNext() { return _source.hasNext(); }
  public T next() { return _map.value(_source.next()); }
  public void remove() { _source.remove(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <S, T> MappedIterator<S, T> make(Iterator<? extends S> source, 
                                                 Lambda<? super S, ? extends T> map) {
    return new MappedIterator<S, T>(source, map);
  }
  
}
