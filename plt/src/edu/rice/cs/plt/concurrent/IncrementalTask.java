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

import edu.rice.cs.plt.lambda.ResolvingThunk;

/**
 * A thunk that performs incremental computation.  Clients will repeatedly invoke {@link #step}
 * until {@link #isResolved} is {@code true}, signifying that the result has been computed.
 * {@link #value} need only be supported <em>after</em> the task has completed.
 * @param <I>  The type of the incremental result (may be {@link Void} if there is no useful
 *             intermediate result)
 * @param <R>  The type of the final result (may be {@link Void} if the task has no useful final result)
 */
public interface IncrementalTask<I, R> extends ResolvingThunk<R> {
  /**
   * Whether the final result is ready. As long as this returns {@code false}, {@link #step} will
   * be invoked; after returning {@code true}, only {@link #value} will be invoked.
   */
  public boolean isResolved();
  
  /** Perform a step in the computation.  Undefined when {@link #isResolved} is {@code true}. */
  public I step();
  
  /** Produce the final result of the task.  Undefined when {@link #isResolved} is {@code false}. */
  public R value();
}
