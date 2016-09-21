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

import java.util.LinkedList;
import java.util.List;

import edu.rice.cs.plt.lambda.Condition;

import junit.framework.TestCase;

public class ConditionMonitorTest extends TestCase {
  
  public void test() throws InterruptedException {
    final List<String> l = new LinkedList<String>();
    final ConditionMonitor m = new ConditionMonitor(new Condition() {
      public boolean isTrue() { return l.isEmpty(); }
    });
    assertTrue(m.isTrue());
    
    DelayedInterrupter interrupter1 = new DelayedInterrupter(50);
    m.ensureTrue();
    assertEquals(0, l.size());
    interrupter1.abort();
    
    l.add("x");
    assertEquals(1, l.size());
    assertFalse(m.isTrue());
    DelayedInterrupter interrupter2 = new DelayedInterrupter(300);
    new Thread() { public void run() { ConcurrentUtil.sleep(100); l.remove("x"); m.check(); } }.start();
    m.ensureTrue();
    assertTrue(m.isTrue());
    assertEquals(0, l.size());
    interrupter2.abort();

    l.add("y");
    @SuppressWarnings("unused") DelayedInterrupter interrupter4 = new DelayedInterrupter(50);
    try { m.ensureTrue(); fail("Monitor should block until interrupted"); }
    catch (InterruptedException e) { /* expected behavior */ }
  }
      
}
