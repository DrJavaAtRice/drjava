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

package edu.rice.cs.plt.lambda;

/**
 * <p>A block of code that executes at most once. The first invocation of {@code run()} runs the code;
 * subsequent invocations are no-ops. (If an exception occurs during evaluation, the nested runnable will be
 * evaluated again on a subsequent invocation.)</p>
 * 
 * <p>Evaluation is thread-safe: locking guarantees that the nested code will never be run (and terminate
 * normally) twice. Thus, if two threads invoke {@code run()} for the first time simultaneously, one will
 * block until the other completes, and then act as a no-op.</p>
 * 
 * @see LazyThunk
 */
public class LazyRunnable implements Runnable {

  private Runnable _block;
  
  public LazyRunnable(Runnable block) { _block = block; }
  
  public void run() {
    // double-checked locking is generally incorrect without "volatile"; in this case, though,
    // it works because we're setting a field to "null", not allocating a new object -- in the
    // worst case, threads don't see the null update and we call resolve() more than necessary
    if (_block != null) { resolve(); }
  }
  
  private synchronized void resolve() {
    if (_block != null) { // verify that the block is still unresolved now that we have a lock
      _block.run();
      _block = null;
    }
  }
  
}
