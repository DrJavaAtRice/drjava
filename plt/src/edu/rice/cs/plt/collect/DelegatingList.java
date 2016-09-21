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

package edu.rice.cs.plt.collect;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * An implementation of {@link List} that delegates all operations to a wrapped
 * list.  Subclasses can be defined that override a few of the methods, while maintaining
 * the default delegation behavior in most cases.  Subclasses can also invoke the overridden
 * methods in {@link java.util.AbstractCollection} to use the default implementations there by 
 * invoking, for example, {@link #abstractCollectionAddAll} (see 
 * {@link java.util.AbstractCollection} for details on the default implementations).
 */
public class DelegatingList<E> extends DelegatingCollection<E> implements List<E> {
  protected final List<E> _delegate; // field is redundant, but prevents a lot of useless casts 
  public DelegatingList(List<E> delegate) { super(delegate); _delegate = delegate; }
  
  public boolean equals(Object o) { return _delegate.equals(o); }
  public int hashCode() { return _delegate.hashCode(); }
  public E get(int index) { return _delegate.get(index); }
  public E set(int index, E element) { return _delegate.set(index, element); }
  public void add(int index, E element) { _delegate.add(index, element); }
  public E remove(int index) { return _delegate.remove(index); }
  public boolean addAll(int index, Collection<? extends E> c) { return _delegate.addAll(index, c); }
  public int indexOf(Object o) { return _delegate.indexOf(o); }
  public int lastIndexOf(Object o) { return _delegate.lastIndexOf(o); }
  public ListIterator<E> listIterator() { return _delegate.listIterator(); }
  public ListIterator<E> listIterator(int index) { return _delegate.listIterator(index); }
  public List<E> subList(int fromIndex, int toIndex) { return _delegate.subList(fromIndex, toIndex); }
}
