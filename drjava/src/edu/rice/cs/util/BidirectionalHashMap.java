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

public class BidirectionalHashMap<Type1, Type2> {
  HashMap<Type1, Type2> forward = new HashMap<Type1, Type2>();
  HashMap<Type2, Type1> backward = new HashMap<Type2, Type1>();
  
  public synchronized void put(Type1 key, Type2 value) {
    if(forward.containsKey(key))
    {
      throw new IllegalArgumentException("Key "  + key + " exists in hash already.");
    }
    if(forward.containsValue(value))
    {
      throw new IllegalArgumentException("Double hashes must be one to one. " + value + " exists already in hash.");
    }      
    forward.put(key, value);
    backward.put(value,key);
  }
  
  public synchronized Type2 getValue(Type1 key) { return forward.get(key); }

  public synchronized Type1 getKey(Type2 value) { return backward.get(value); }
  
  public synchronized boolean containsKey(Type1 key) { return forward.containsKey(key); }
  
  public synchronized boolean containsValue(Type2 value) { return backward.containsKey(value); }
  
  public synchronized Iterator<Type2> valuesIterator() { return new BHMIterator(); }
  
  public synchronized Type2 removeValue(Type1 key) {
    Type2 tmp = forward.remove(key);
    backward.remove(tmp);
    return tmp;
  }
  
  public synchronized Type1 removeKey(Type2 value) {
    Type1 tmp = backward.remove(value);
    forward.remove(tmp);
    return tmp;
  }
  
  public synchronized int size() { return forward.size(); }
 
  
  public synchronized void clear() {
    forward = new HashMap<Type1, Type2>();
    backward = new HashMap<Type2, Type1>();
  }
  
  public synchronized String toString() {
    String ret = new String();
    ret = "forward = \n" + forward.values() + "\n backward = \n" + backward.values();
    return ret;
  }
  
  /** Iterator class for BiDirectionalHashMap */
  class BHMIterator implements Iterator<Type2> {
    
    Iterator<Type2> forwardIt = forward.values().iterator();
    
    /** Cached value of last element returned by next() */
    Type2 lastValue = null;
    
    public boolean hasNext() { 
      synchronized(BidirectionalHashMap.this) {
        return forwardIt.hasNext(); 
      }
    }
    
    public Type2 next() { 
      synchronized(BidirectionalHashMap.this) {
        lastValue = forwardIt.next(); 
        return lastValue;
      }
    }
    
    /** Removes last element returned by next(); throws IllegalStateException if no such element */
    public void remove() {
      synchronized(BidirectionalHashMap.this) {
        forwardIt.remove();          /* throws exception if lastValue is null */
        backward.remove(lastValue);  /* cannot fail because lastValue is not null */
        lastValue = null;
      }
    }
  }
      
      
}
