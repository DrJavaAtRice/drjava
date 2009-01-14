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

package edu.rice.cs.plt.debug;

import junit.framework.TestCase;

import static edu.rice.cs.plt.debug.DebugUtil.*;

public class DebugUtilTest extends TestCase {
  
  public void testMakeLogSink() {
    // main purpose of this test is to make sure descriptors are parsed -- testing that they're
    // interpreted *correctly* is difficult (since the structure of the returned object is private)
    // and not as productive, since it's more likely that a descriptor won't parse at all than that
    // it will be accepted but parsed incorrectly
    
    assertClass(SystemOutLogSink.class, makeLogSink("System.out", "Test"));
    assertClass(SystemOutLogSink.class, makeLogSink("stdout", "Test"));
    assertClass(SystemErrLogSink.class, makeLogSink("System.err", "Test"));
    assertClass(SystemErrLogSink.class, makeLogSink("stderr", "Test"));
    assertClass(FileLogSink.class, makeLogSink("file", "Test"));
    assertClass(AssertEmptyLogSink.class, makeLogSink("assert", "Test"));
    assertClass(PopupLogSink.class, makeLogSink("popup", "Test"));
    assertClass(RMILogSink.class, makeLogSink("tree", "Test"));
    
    assertClass(AsynchronousLogSink.class, makeLogSink("~popup", "Test"));
    
    assertClass(FilteredLogSink.class, makeLogSink("popup+foo", "Test"));
    assertClass(FilteredLogSink.class, makeLogSink("popup+'foo'", "Test"));
    assertClass(FilteredLogSink.class, makeLogSink("assert+com-com.pkg1-com.pkg2", "Test"));
    assertClass(FilteredLogSink.class, makeLogSink("assert +com -com.pkg1 -com.pkg2", "Test"));
    assertClass(FilteredLogSink.class, makeLogSink("(stdout:'UTF-8', stderr:'UTF-8') -'Foo'", "Test"));
    
    assertClass(SplitLogSink.class, makeLogSink("stdout,stderr", "Test"));
    assertClass(SplitLogSink.class, makeLogSink("stdout, stderr", "Test"));
    assertClass(SplitLogSink.class, makeLogSink("tree, ~file", "Test"));
    assertClass(SplitLogSink.class, makeLogSink("file:pkg1.txt +pkg1, file:pkg2.txt +pkg2", "Test"));
    
    assertNull(makeLogSink("fish", "Test"));
  }
  
  private void assertClass(Class<?> expected, Object val) {
    assertNotNull(val);
    assertEquals(expected, val.getClass());
  }

}
