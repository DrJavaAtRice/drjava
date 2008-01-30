/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LambdaUtil;

/**
 * A wrapper for optional values.  This provides a strictly-typed alternative to using
 * {@code null} to represent the absence of a value.  Options have two variants: "some"
 * and "none."  The "some" case is represented by {@link Wrapper}s; the "none" case is
 * represented by the {@link Null} singleton.  {@code Option} values may be decomposed
 * using an {@link OptionVisitor}.
 */
public abstract class Option<T> extends Tuple {
  
  /** Calls the appropriate case in the visitor. */
  public abstract <Ret> Ret apply(OptionVisitor<? super T, ? extends Ret> visitor);
  
  public abstract boolean isSome();
  
  public boolean isNone() { return !isSome(); }
  

  /** Create a "some" case wrapper for the given value. */
  public static <T> Option<T> some(T val) { return new Wrapper<T>(val); }
  
  /** 
   * Return the "none" case singleton, cast (unsafe formally, but safe in practice) to the 
   * appropriate type.
   */
  @SuppressWarnings("unchecked") public static <T> Option<T> none() {
    return (Option<T>) Null.INSTANCE;
  }
  
  /**
   * Treat a possibly-null value as an {@code Option}: if the value is {@code null}, produce
   * a "none"; otherwise, produce a "some" wrapping the value.
   */
  @SuppressWarnings("unchecked") public static <T> Option<T> wrap(T val) {
    if (val == null) { return (Option<T>) Null.INSTANCE; }
    else { return new Wrapper<T>(val); }
  }
  
  /** 
   * Access the value in the given {@code Option}, or throw an exception in the "none" case.
   * @return  The value of {@code opt} if it is a "some"
   * @throws RuntimeException  If {@code opt} is a "none"
   */
  public static <T> T unwrap(Option<T> opt) {
    if (opt instanceof Wrapper<?>) { return ((Wrapper<T>)opt).value(); }
    else { throw new IllegalArgumentException("Cannot unwrap a none option"); }
  }
  
  /**
   * Access the value in the given {@code Option}, or return the given default value in the
   * "none" case.
   * @return  The value of {@code opt} if it is a "some", and {@code forNone} otherwise
   */
  public static <T> T unwrap(Option<T> opt, T forNone) {
    if (opt instanceof Wrapper<?>) { return ((Wrapper<T>)opt).value(); }
    else { return forNone; }
  }
  
  /** 
   * Access the value in the given {@code Option}, or throw the given exception in the "none" case.
   * @return  The value of {@code opt} if it is a "some"
   * @throws RuntimeException  If {@code opt} is a "none"; fills in the stack trace
   */
  public static <T> T unwrap(Option<T> opt, RuntimeException forNone) {
    if (opt instanceof Wrapper<?>) { return ((Wrapper<T>)opt).value(); }
    else { forNone.fillInStackTrace(); throw forNone; }
  }
  
  /**
   * Access the value in the given {@code Option}, or throw the exception produced by {@code forNone}
   * in the "none" case.
   * @return  The value of {@code opt} if it is a "some"
   * @throws RuntimeException  If {@code opt} is a "none"; fills in the stack trace
   */
  public static <T> T unwrap(Option<T> opt, final Thunk<? extends RuntimeException> forNone) {
    if (opt instanceof Wrapper<?>) { return ((Wrapper<T>)opt).value(); }
    else {
      RuntimeException e = forNone.value();
      e.fillInStackTrace();
      throw e;
    }
  }
  
}
