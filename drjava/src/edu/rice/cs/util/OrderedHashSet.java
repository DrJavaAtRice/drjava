/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;
import java.util.*;

/** A set class patterned after HashSet except that the construction order for elements is scrupulously maintained
  * for the sake of supporting obvious list operations based on construction order (addition to the set). 
  */
public class OrderedHashSet<Type> implements Collection<Type> {
  private HashSet<Type> elements = new HashSet<Type>();
  private ArrayList<Type> order = new ArrayList<Type>();
  
  /** Relying on standard 0-ary constructor */
  
  public boolean add(Type elt) {
    boolean validAdd = elements.add(elt);
    if (validAdd) order.add(elt);
    return validAdd;
  }
  
  public boolean addAll(Collection<? extends Type> c) { 
    throw new UnsupportedOperationException("OrderedHashSet does not support this operation");
  }
  
  public void clear() {
    elements.clear();
    order.clear();
  }
  
  public boolean contains(Object elt) { return elements.contains(elt); }
   
  public boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException("OrderedHashSet does not support this operation");
  }
  
  public boolean equals(Object o) { 
    if (o == null || o.getClass() != getClass()) return false;
    return order.equals(elements());
  }
  
  public int hashCode() { return order.hashCode(); }
  
  public boolean isEmpty() { return order.isEmpty(); }
  
  public Type get(int i) { return order.get(i); }
  
  public Iterator<Type> iterator() { return new OHMIterator(); }
  
  /** @throws {@link IndexOutOfBoundsException */
  public Type remove(int i) {
    Type elt = order.remove(i); // O(n) operation
    elements.remove(elt);
    return elt;
  }
  
  public boolean remove(Object elt) {
    elements.remove(elt);
    return order.remove(elt);  // O(n) operation
  }
  
  public boolean removeAll(Collection<?> elts) {
    throw new UnsupportedOperationException("OrderedHashSet does not support this operation");
  }
  
  public boolean retainAll(Collection<?> elts) {
    throw new UnsupportedOperationException("OrderedHashSet does not support this operation");
  }
  
  public int size() { return order.size(); }
  
  public Object[] toArray() { return order.toArray(); }
  
  public <T> T[] toArray(T[] a) { return order.toArray(a); }
 
  public Collection<Type> elements() { return order; }
  
  public String toString() { return order.toString(); }
  
    /** Iterator class for OrderedHashSet */
  class OHMIterator implements Iterator<Type> {
    
    Iterator<Type> it = order.iterator();
    
    /** Cached values of last elt visited */
    Type lastElt = null;

    public boolean hasNext() { return it.hasNext(); }
    
    public Type next() {
      lastElt = it.next();
      return lastElt;
    }
    
    /** Removes last element returned by next(); throws IllegalStateException if no such element */
    public void remove() {
      it.remove();                 /* throws exception if lastElt is null */
      elements.remove(lastElt);
    }
  }
}
