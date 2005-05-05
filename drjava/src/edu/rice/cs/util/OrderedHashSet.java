/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;
import java.util.*;

/** A set class patterned after HashSet except that the construction order for elements is scrupulously maintained
 *  for the sake of supporting obvious list operations based on construction order (addition to the set). */

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
    if ((o == null) || o.getClass() != getClass()) return false;
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
