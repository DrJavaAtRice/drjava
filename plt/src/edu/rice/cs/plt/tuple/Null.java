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
 * An empty tuple.  There is only one accessible instance, the {@code INSTANCE} singleton, which has
 * arbitrarily-chosen type argument {@code Void} ({@code Option<null>} would make more sense, but is 
 * not expressible).  Clients needing a specific kind of {@code Null} can perform an unsafe cast on 
 * the singleton to produce the desired type (this is done in {@link Null#make}).
 */
public final class Null<T> extends Option<T> {
  
  /** Forces access through the singleton */
  private Null() {}
  
  /** A singleton null tuple */
  public static final Null<Void> INSTANCE = new Null<Void>();
  
  /** Invokes {@code visitor.forNone()} */
  public <Ret> Ret apply(OptionVisitor<? super T, ? extends Ret> visitor) {
    return visitor.forNone();
  }
  
  public boolean isSome() { return false; }
  
  public T unwrap() { throw new OptionUnwrapException(); }
  
  public T unwrap(T forNone) { return forNone; }
  
  /** Produces {@code "()"} */
  public String toString() { return "()"; }
  
  /** Defined in terms of identity (since the singleton is the only accessible instance) */
  public boolean equals(Object o) { return this == o; }
  
  /** Defined in terms of identity (since the singleton is the only accessible instance) */
  protected int generateHashCode() { return System.identityHashCode(this); }
  
  /** Return a singleton, cast to the appropriate type. */
  @SuppressWarnings("unchecked")
  public static <T> Null<T> make() { return (Null<T>) INSTANCE; }
}  
