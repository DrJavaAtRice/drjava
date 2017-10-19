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

import junit.framework.TestCase;

public class CompletionMonitorTest extends TestCase {
  
  volatile boolean _flag; // Set and reset by tests to check for proper sequencing
  
  public void testDegenerateSignal() throws InterruptedException {
    CompletionMonitor as = new CompletionMonitor(true);
    assertTrue("Flag should start out as true", as.isSignaled());
    
    DelayedInterrupter interrupter = new DelayedInterrupter(50);
    as.ensureSignaled();
    interrupter.abort();
  }
  
  public void testRealSignal() throws InterruptedException {
    final CompletionMonitor as = new CompletionMonitor(false);
    
    DelayedInterrupter interrupter1 = new DelayedInterrupter(500); // got a failure when this was 300
    _flag = false;
    assertFalse(as.isSignaled());
    new Thread() { public void run() { ConcurrentUtil.sleep(100); _flag = true; as.signal(); } }.start();
    as.ensureSignaled();
    assertTrue(_flag);
    interrupter1.abort();
    assertTrue(as.isSignaled());
    
    DelayedInterrupter interrupter2 = new DelayedInterrupter(10);
    assertTrue(as.isSignaled());
    as.ensureSignaled(); // should not block
    interrupter2.abort();
    
    as.reset();
    assertFalse(as.isSignaled());
    @SuppressWarnings("unused") DelayedInterrupter interrupter3 = new DelayedInterrupter(50);
    try { as.ensureSignaled(); fail("Monitor should not be signalled"); }
    catch (InterruptedException e) { /* expected behavior */ }
  }
  
    
}
