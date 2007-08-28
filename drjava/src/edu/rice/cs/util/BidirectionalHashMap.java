/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
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

public class BidirectionalHashMap<Type1, Type2> {
  HashMap<Type1, Type2> forward = new HashMap<Type1, Type2>();
  HashMap<Type2, Type1> backward = new HashMap<Type2, Type1>();
  
  public void put(Type1 key, Type2 value) {
    if (forward.containsKey(key)) {
      throw new IllegalArgumentException("Key "  + key + " exists in hash already.");
    }
    if (forward.containsValue(value)) {
      throw new IllegalArgumentException("Double hashes must be one to one. " + value + " exists already in hash.");
    }      
    forward.put(key, value);
    backward.put(value, key);
  }
  
  public Type2 getValue(Type1 key) { return forward.get(key); }

  public Type1 getKey(Type2 value) { return backward.get(value); }
  
  public boolean containsKey(Type1 key) { return forward.containsKey(key); }
  
  public boolean containsValue(Type2 value) { return backward.containsKey(value); }
  
  public Iterator<Type2> valuesIterator() { return new BHMIterator(); }
  
  public boolean isEmpty() { return forward.isEmpty(); }
  
  /** Returns a Collection<Type2> in some order. */
  public Collection<Type2> values() { return forward.values(); }  
  
  public Object[] valuesArray() { return values().toArray(); }  // Return type should be Type2[];  type erasure bites!
  public  Type2[] valuesArray(Type2[] a) { return values().toArray(a); }  // argument is hack to get the return type right
  
  public Type2 removeValue(Type1 key) {
    Type2 tmp = forward.remove(key);
    backward.remove(tmp);
    return tmp;
  }
  
  public Type1 removeKey(Type2 value) {
    Type1 tmp = backward.remove(value);
    forward.remove(tmp);
    return tmp;
  }
  
  public int size() { return forward.size(); }
 
  
  public void clear() {
    forward = new HashMap<Type1, Type2>();
    backward = new HashMap<Type2, Type1>();
  }
  
  public String toString() {
    String ret = "";
    ret = "forward = " + forward.values() + "\nbackward = " + backward.values();
    return ret;
  }
  
  /** Iterator class for BiDirectionalHashMap */
  class BHMIterator implements Iterator<Type2> {
    
    Iterator<Type2> forwardIt = forward.values().iterator();
    
    /** Cached value of last element returned by next() */
    Type2 lastValue = null;
    
    public boolean hasNext() { 
      return forwardIt.hasNext(); 
    }
    
    public Type2 next() { 
      lastValue = forwardIt.next(); 
      return lastValue;
    }
    
    /** Removes last element returned by next(); throws IllegalStateException if no such element */
    public void remove() {
      forwardIt.remove();          /* throws exception if lastValue is null */
      backward.remove(lastValue);  /* cannot fail because lastValue is not null */
      lastValue = null;
    }
  }
}
