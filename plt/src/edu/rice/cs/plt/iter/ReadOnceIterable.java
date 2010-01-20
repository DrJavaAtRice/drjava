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

/**
 * An iterable that simply wraps an {@link Iterator}.  Unlike most iterables,
 * state changes on the wrapped iterator (such as advancing) are reflected across 
 * invocations of {@link #iterator}; once the iterator has been traversed,
 * this iterable is effectively empty.  While these semantics are inconvenient at
 * times, they also allow a very lightweight implementation.  Clients that need
 * to traverse an iterator multiple times (or that need a {@link SizedIterable})
 * can use a {@link SnapshotIterable} instead, at the expense of copying and
 * storing the entire contents of the iterator.
 */
public class ReadOnceIterable<T> implements Iterable<T> {
  
  private final Iterator<T> _iter;
  public ReadOnceIterable(Iterator<T> iter) { _iter = iter; }
  public Iterator<T> iterator() { return _iter; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ReadOnceIterable<T> make(Iterator<T> iter) { 
    return new ReadOnceIterable<T>(iter);
  }
  
}
