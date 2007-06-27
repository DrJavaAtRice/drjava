/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
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

package edu.rice.cs.plt.recur;

import junit.framework.TestCase;

public class ContinuationTest extends TestCase {
  
  /** A simple recursive function.  Should cause a stack overflow on large inputs. */
  public static boolean isEven(int x) {
    if (x == 0) { return true; }
    if (x == 1) { return false; }
    else { return isEven(x - 2); }
  }
  
  /** Continuation-based version of {@code isEven}.  Should be able to handle large inputs. */
  public static Continuation<Boolean> safeIsEven(final int x) {
    if (x == 0) { return ValueContinuation.make(true); }
    if (x == 1) { return ValueContinuation.make(false); }
    else {
      return new TailContinuation<Boolean>() {
        public Continuation<? extends Boolean> step() { return safeIsEven(x - 2); }
      };
    }
  }
  
  public void testIsEven() {
    // make sure isEven is defined correctly
    assertTrue(isEven(0));
    assertFalse(isEven(1));
    assertTrue(isEven(6));
    assertFalse(isEven(7));
    
    // make sure safeIsEven is defined correctly
    assertTrue(safeIsEven(0).value());
    assertFalse(safeIsEven(1).value());
    assertTrue(safeIsEven(6).value());
    assertFalse(safeIsEven(7).value());

    // this probably causes a stack overflow
    //assertTrue(isEven(300000));
    //assertFalse(isEven(300001));
    
    // this should execute without an overflow
    assertTrue(safeIsEven(300000).value());
    assertFalse(safeIsEven(300001).value());
  }
}
