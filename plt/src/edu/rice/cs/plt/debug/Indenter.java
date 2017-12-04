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

package edu.rice.cs.plt.debug;

import edu.rice.cs.plt.text.TextUtil;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;

/**
 * <p>Manages a string of varying size to be used for indenting.  The "indent string" is some token string
 * repeated a variable number of times.  By default, the token string is {@code "  "} (two spaces), but
 * a different string may be provided if needed.  Initially, the indentation level (the number of repeats)
 * is set to {@code 0}; {@link #push} and {@link #pop} are used to adjust this value.</p>
 * 
 * <p>If thread safety is required, alternative {@link #atomicPop} and {@link #atomicPush} methods are
 * provided as well.  These use locking to prevent concurrent changes to the indentation level.</p>
 */
public class Indenter {
  
  private final String _token;
  private int _level;
  private Thunk<String> _stringFactory;
  
  /** Create an indenter with the default token string: {@code "  "} (two spaces) */
  public Indenter() { this("  "); }
  
  /** Create an indenter with the specified number of spaces as its token string */
  public Indenter(int spaces) { this(TextUtil.repeat(' ', spaces)); }
  
  /** Create an indenter with the given string as its token string */
  public Indenter(String token) {
    _token = token;
    _level = 0;
    _stringFactory = makeThunk();
  }
  
  /** Increase the indentation level */
  public void push() { _level++; _stringFactory = makeThunk(); }
  
  /** Decrease the indentation level */
  public void pop() { _level--; _stringFactory = makeThunk(); }
  
  /**
   * Increase the indentation level atomically.  Locking prevents other {@code atomicPush()} and
   * {@code atomicPop()} invocations from adjusting the level simultaneously.
   */
  public void atomicPush() { adjust(1); _stringFactory = makeThunk(); }
  
  /**
   * Decrease the indentation level atomically.  Locking prevents other {@code atomicPush()} and
   * {@code atomicPop()} invocations from adjusting the level simultaneously.
   */
  public void atomicPop() { adjust(-1); _stringFactory = makeThunk(); }
  
  /** Atomically adjust {@code _level}. */
  private synchronized void adjust(int delta) { _level += delta; }

  /**
   * Produce a string based on the token string and current indentation level.  If the level is {@code <= 0},
   * this is the empty string.
   */
  public String indentString() { return _stringFactory.value(); }
  
  private Thunk<String> makeThunk() {
    return LazyThunk.make(new Thunk<String>() {
      public String value() {
        if (_level <= 0) { return ""; }
        else { return TextUtil.repeat(_token, _level); }
      }
    });
  }
  
}
