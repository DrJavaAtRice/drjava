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

package edu.rice.cs.plt.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import edu.rice.cs.plt.lambda.Box;

/** 
 * <p>A thread-safe box implementation.  Extends {@link AtomicReference} so that it can be treated as a
 * {@link Box}.</p>
 * 
 * <p>As a wrapper for arbitrary objects, instances of this class will serialize without error
 * only if the wrapped object is serializable.</p>
 */
public class ConcurrentBox<T> extends AtomicReference<T> implements Box<T> {
  
  /** Create a box initialized with {@code val} */
  public ConcurrentBox(T val) { super(val); }
  
  /** Create a box initialized with {@code null} */
  public ConcurrentBox() { super(null); }
  
  public T value() { return get(); }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ConcurrentBox<T> make(T val) { return new ConcurrentBox<T>(val); }

  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ConcurrentBox<T> make() { return new ConcurrentBox<T>(); }
  
}
