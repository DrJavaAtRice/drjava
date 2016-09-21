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

package edu.rice.cs.plt.tuple;

import java.io.Serializable;
import edu.rice.cs.plt.lambda.Lambda3;

/**
 * A triple that defines {@link #equals} and {@link #hashCode} in terms of its elements' 
 * identity ({@code ==}) instead of equality (@code equals})
 */
public class IdentityTriple<T1, T2, T3> extends Triple<T1, T2, T3> {
  
  public IdentityTriple(T1 first, T2 second, T3 third) { super(first, second, third); }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is identical (according to {@code ==})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (o == null || !getClass().equals(o.getClass())) { return false; }
    else {
      Triple<?, ?, ?> cast = (Triple<?, ?, ?>) o;
      return 
        _first == cast._first &&
        _second == cast._second &&
        _third == cast._third;
    }
  }
  
  protected int generateHashCode() {
    return 
      System.identityHashCode(_first) ^ 
      (System.identityHashCode(_second) << 1) ^ 
      (System.identityHashCode(_third) << 2) ^
      getClass().hashCode();
  }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3> IdentityTriple<T1, T2, T3> make(T1 first, T2 second, T3 third) {
    return new IdentityTriple<T1, T2, T3>(first, second, third);
  }
  
  /** Produce a lambda that invokes the constructor */
  @SuppressWarnings("unchecked") public static <T1, T2, T3> Lambda3<T1, T2, T3, Triple<T1, T2, T3>> factory() {
    return (Factory<T1, T2, T3>) Factory.INSTANCE;
  }
  
  private static final class Factory<T1, T2, T3> implements Lambda3<T1, T2, T3, Triple<T1, T2, T3>>, Serializable {
    public static final Factory<Object, Object, Object> INSTANCE = new Factory<Object, Object, Object>();
    private Factory() {}
    public Triple<T1, T2, T3> value(T1 first, T2 second, T3 third) {
      return new IdentityTriple<T1, T2, T3>(first, second, third);
    }
  }
  
}
