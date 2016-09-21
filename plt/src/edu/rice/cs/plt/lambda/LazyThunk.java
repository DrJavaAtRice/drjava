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
 * <p>A thunk providing call-by-need evaluation of the nested thunk, {@code value}. The first invocation of
 * {@link #value()} evaluates and discards the thunk; subsequent invocations return the same value as returned
 * previously.  (If an exception occurs during evaluation, no result is cached and the nested thunk will be
 * evaluated again on a subsequent invocation.)</p>
 * 
 * <p>Evaluation is thread-safe: locking guarantees that the nested thunk will never be evaluated (and
 * terminate normally) twice. Thus, if two threads invoke {@code value()} simultaneously (and a result has not
 * yet been cached), one will block until the other resolves the nested thunk.</p>
 * 
 * @see CachedThunk
 * @see DelayedThunk
 * @see LazyRunnable
 */
public class LazyThunk<R> implements ResolvingThunk<R> {

  private volatile R _val;
  private volatile Thunk<? extends R> _thunk;
  
  public LazyThunk(Thunk<? extends R> value) {
    _thunk = value;
    // the value of _val doesn't matter
  }
  
  public R value() {
    if (_thunk != null) { resolve(); }
    return _val;
  }
  
  public boolean isResolved() { return _thunk == null; }
  
  private synchronized void resolve() {
    if (_thunk != null) { // verify that the result is still unresolved now that we have a lock
      _val = _thunk.value();
      _thunk = null;
    }
  }
  
  public static <R> LazyThunk<R> make(Thunk<? extends R> value) { return new LazyThunk<R>(value); }
  
}
