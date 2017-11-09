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

/**
 * A sextet that defines {@link #equals} and {@link #hashCode} in terms of its elements' 
 * identity ({@code ==}) instead of equality (@code equals})
 */
public class IdentitySextet<T1, T2, T3, T4, T5, T6> extends Sextet<T1, T2, T3, T4, T5, T6> {
  
  public IdentitySextet(T1 first, T2 second, T3 third, T4 fourth, T5 fifth, T6 sixth) {
    super(first, second, third, fourth, fifth, sixth);
  }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and each
   *          corresponding element is identical (according to {@code ==})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (o == null || !getClass().equals(o.getClass())) { return false; }
    else {
      Sextet<?, ?, ?, ?, ?, ?> cast = (Sextet<?, ?, ?, ?, ?, ?>) o;
      return 
        _first == cast._first &&
        _second == cast._second &&
        _third == cast._third &&
        _fourth == cast._fourth &&
        _fifth == cast._fifth &&
        _sixth == cast._sixth;
    }
  }
  
  protected int generateHashCode() {
    return 
      System.identityHashCode(_first) ^ 
      (System.identityHashCode(_second) << 1) ^ 
      (System.identityHashCode(_third) << 2) ^ 
      (System.identityHashCode(_fourth) << 3) ^ 
      (System.identityHashCode(_fifth) << 4) ^ 
      (System.identityHashCode(_sixth) << 5) ^ 
      getClass().hashCode();
  }
    
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, T4, T5, T6> 
    IdentitySextet<T1, T2, T3, T4, T5, T6> make(T1 first, T2 second, T3 third, T4 fourth, 
                                                T5 fifth, T6 sixth) {
    return new IdentitySextet<T1, T2, T3, T4, T5, T6>(first, second, third, fourth, fifth, sixth);
  }
  
}
