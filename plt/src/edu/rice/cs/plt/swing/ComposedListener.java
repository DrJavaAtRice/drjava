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

package edu.rice.cs.plt.swing;

import java.util.LinkedList;

/**
 * Abstract parent of composed listeners, which allow a list to be treated as a single
 * listener object.  Subclasses should implement some listener interface and, for each method
 * in that interface, traverse the result of {@code #listeners} and delegate.  For
 * example:
 * <pre>
 * public class ComposedFooListener extends ComposedListener<FooListener> implements FooListener {
 *   public void handleFooEvent1(FooEvent1 e) {
 *     for (FooListener l : listeners()) { l.handleFooEvent1(e); }
 *   }
 *   public void handleFooEvent2(FooEvent2 e) {
 *     for (FooListener l : listeners()) { l.handleFooEvent2(e); }
 *   }
 * }
 * </pre>
 * Following the convention of {@code javax.swing.event.EventListenerList}, the encapsulated list is
 * treated like a stack: traversal always returns the last-added element first; removal will match the
 * most recently-added listener equal to the provided listener.
 */
public abstract class ComposedListener<T> {
  private final LinkedList<T> _listeners;
  public ComposedListener() { _listeners = new LinkedList<T>(); }
  public void add(T listener) { _listeners.addFirst(listener); }
  public void remove(T listener) { _listeners.remove(listener); }
  protected Iterable<T> listeners() { return _listeners; }
}
