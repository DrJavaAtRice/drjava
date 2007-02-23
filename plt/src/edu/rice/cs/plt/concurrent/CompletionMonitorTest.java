package edu.rice.cs.plt.concurrent;

import junit.framework.TestCase;

public class CompletionMonitorTest extends TestCase {
  
  volatile boolean _flag; // Set and reset by tests to check for proper sequencing
  
  public void testDegenerateSignal() throws InterruptedException {
    CompletionMonitor as = new CompletionMonitor(true);
    assertTrue("Flag should start out as true", as.isSignalled());
    
    DelayedInterrupter interrupter = new DelayedInterrupter(50);
    as.insureSignalled();
    interrupter.abort();
  }
  
  public void testRealSignal() throws InterruptedException {
    final CompletionMonitor as = new CompletionMonitor(false);
    
    DelayedInterrupter interrupter1 = new DelayedInterrupter(200);
    _flag = false;
    new Thread() { public void run() { ConcurrentUtil.sleep(100); _flag = true; as.signal(); } }.start();
    assertFalse(_flag);
    assertFalse(as.isSignalled());
    as.insureSignalled();
    assertTrue(_flag);
    interrupter1.abort();
    assertTrue(as.isSignalled());
    
    DelayedInterrupter interrupter2 = new DelayedInterrupter(10);
    assertTrue(as.isSignalled());
    as.insureSignalled(); // should not block
    interrupter2.abort();
    
    as.reset();
    assertFalse(as.isSignalled());
    DelayedInterrupter interrupter3 = new DelayedInterrupter(50);
    try { as.insureSignalled(); fail("Monitor should not be signalled"); }
    catch (InterruptedException e) { /* expected behavior */ }
  }
  
    
}
