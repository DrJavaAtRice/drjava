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
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.object.Composite;
import edu.rice.cs.plt.object.ObjectUtil;

/** An iterator that notifies a callback whenever an element is removed. */
public class RemoveNotificationIterator<T> implements Iterator<T>, Composite {
  
  private final Iterator<? extends T> _i;
  private final Runnable1<? super T> _listener;
  private T _last; // null before iteration starts
  
  public RemoveNotificationIterator(Iterator<? extends T> i, Runnable1<? super T> listener) {
    _i = i;
    _listener = listener;
    _last = null;
  }
  
  public int compositeHeight() { return ObjectUtil.compositeHeight(_i) + 1; }
  public int compositeSize() { return ObjectUtil.compositeSize(_i) + 1; }
    
  public boolean hasNext() { return _i.hasNext(); }
  
  public T next() {
    _last = _i.next();
    return _last;
  }
  
  public void remove() {
    _i.remove(); // if this succeeds, _last is non-null
    _listener.run(_last);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> RemoveNotificationIterator<T> make(Iterator<? extends T> i,
                                                       Runnable1<? super T> listener) {
    return new RemoveNotificationIterator<T>(i, listener);
  }
    
}
