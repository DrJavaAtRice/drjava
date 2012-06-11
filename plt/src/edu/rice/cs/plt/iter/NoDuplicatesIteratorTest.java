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

package edu.rice.cs.plt.iter;

import junit.framework.TestCase;

import static edu.rice.cs.plt.iter.IterUtilTest.assertIterator;
import static edu.rice.cs.plt.iter.IterUtilTest.assertIteratorUnchecked;

public class NoDuplicatesIteratorTest extends TestCase {
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> NoDuplicatesIterator<T> make(T... elts) {
    return NoDuplicatesIterator.make(IterUtil.asIterable(elts).iterator());
  }
  
  public void test() {
    assertIterator(make(new Integer[0]));
    assertIteratorUnchecked(make(new Integer[0]));
    assertIterator(make(1), 1);
    assertIteratorUnchecked(make(1), 1);
    assertIterator(make(1, 1), 1);
    assertIteratorUnchecked(make(1, 1), 1);
    assertIterator(make(1, 1, 1), 1);
    assertIteratorUnchecked(make(1, 1, 1), 1);
    assertIterator(make(1, 2), 1, 2);
    assertIteratorUnchecked(make(1, 2), 1, 2);
    assertIterator(make(1, 2, 1), 1, 2);
    assertIteratorUnchecked(make(1, 2, 1), 1, 2);
    assertIterator(make(1, 2, 1, 3, 2), 1, 2, 3);
    assertIteratorUnchecked(make(1, 2, 1, 3, 2), 1, 2, 3);
    assertIterator(make(1, 2, null, 4, null, 2, 5), 1, 2, null, 4, 5);
  }
  
}
