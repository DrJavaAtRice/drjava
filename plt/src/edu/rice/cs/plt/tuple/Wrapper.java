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

import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.Lambda;

/** An arbitrary 1-tuple; overrides {@link #toString()}, {@link #equals(Object)}, and {@link #hashCode()}. */
public class Wrapper<T> extends Option<T> implements Thunk<T> {
  
  private final T _value;
  
  public Wrapper(T value) { _value = value; }
  
  public T value() { return _value; }
  
//  public void setValue(T newValue) { _value = newValue; }
  
  public T unwrap() { return _value; }
  
  public T unwrap(T forNone) { return _value; }
  
  public <Ret> Ret apply(OptionVisitor<? super T, ? extends Ret> visitor) {
    return visitor.forSome(_value);
  }
  
  public boolean isSome() { return true; }
  
  public String toString() { return "(" + _value + ")"; }
  
  /**
   * @return  {@code true} iff {@code this} is of the same class as {@code o}, and the
   *          wrapped values are equal (according to {@code equals})
   */
  public boolean equals(Object o) {
    if (this == o) { return true; }
    else if (o == null || !getClass().equals(o.getClass())) { return false; }
    else {
      Wrapper<?> cast = (Wrapper<?>) o;
      return _value == null ? cast._value == null : _value.equals(cast._value);
    }
  }
  
  protected int generateHashCode() {
    return (_value == null ? 0 : _value.hashCode()) ^ getClass().hashCode();
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> Wrapper<T> make(T value) { return new Wrapper<T>(value); }
  
  /** Produce a lambda that invokes the constructor */
  @SuppressWarnings("unchecked") public static <T> Lambda<T, Wrapper<T>> factory() {
    return (Factory<T>) Factory.INSTANCE;
  }
  
  private static final class Factory<T> implements Lambda<T, Wrapper<T>>, Serializable {
    public static final Factory<Object> INSTANCE = new Factory<Object>();
    private Factory() {}
    public Wrapper<T> value(T val) { return new Wrapper<T>(val); }
  }
  
}
