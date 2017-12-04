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

import edu.rice.cs.plt.tuple.Option;

/**
 * <p>A thunk that caches the result of a nested thunk on the first invocation of {@code value()}.
 * Unlike {@link LazyThunk}, the cached result can be invalidated by invoking {@link #reset}; the
 * next invocation of {@code value()} will again delegate to the nested thunk (the trade-off for
 * this behavior is that the wrapped thunk cannot be discarded for the life of this object).
 * If an exception occurs during evaluation, no result is cached.</p>
 * 
 * <p>Evaluation is thread-safe: locking guarantees that the nested thunk will never be evaluated (and
 * terminate normally) twice without an intervening {@code reset()} invocation. Thus, if two threads 
 * invoke {@code value()} simultaneously (and a result is not cached), one will block until
 * the other resolves the nested thunk.</p>
 * 
 * @see LazyThunk
 * @see DelayedThunk
 * @see LazyRunnable
 */
public class CachedThunk<R> implements ResolvingThunk<R> {

  private final Thunk<? extends R> _thunk;
  private volatile Option<R> _val;
  
  public CachedThunk(Thunk<? extends R> value) {
    _thunk = value;
    _val = Option.none();
  }
  
  public R value() {
    Option<R> v = _val; // get a local copy in case there's a concurrent reset
    if (v.isNone()) { return resolve(); }
    else { return v.unwrap(); }
  }
  
  public synchronized void reset() {
    // *must* synchronize -- if we try to reset while resolve() is half-done, the reset may appear 
    // to have never happened
    _val = Option.none();
  }
  
  public boolean isResolved() { return _val.isSome(); }
  
  /**
   * Combines {@link #isResolved} and {@link #value} in an atomic operation (avoiding inconsistencies
   * due to a concurrent {@link #reset}): if a value is cached, return it; otherwise, return "none".
   */
  public Option<R> cachedValue() { return _val; }
  
  private synchronized R resolve() {
    if (_val.isNone()) { // verify that the result is still unresolved now that we have a lock
      R result = _thunk.value();
      _val = Option.some(result);
      return result;
    }
    else { return _val.unwrap(); }
  }
  
  public static <R> CachedThunk<R> make(Thunk<? extends R> value) { return new CachedThunk<R>(value); }
  
}
