package edu.rice.cs.plt.concurrent;

import junit.framework.TestCase;

/**
 * Test for CompletionMonitor
 */
public class CompletionMonitorTest extends TestCase {
  
  private boolean _shouldInterrupt;
  
  public void testDegenerateSignal() {
    
    CompletionMonitor as = new CompletionMonitor(true);
    assertTrue("Flag should start out as true", as.isFlag());
    //This thread will interrupt the main thread if it hangs
    
    ThreadInterrupter interrupter = new ThreadInterrupter();
    interrupter.start();
    
    assertTrue("WaitOne hung, and was interrupted by the failsafe.", as.waitOne());
    interrupter.targetCompleted();
  }
  
  public void testRealSignal() {
    final CompletionMonitor as = new CompletionMonitor(false);
    Thread worker = new Thread() {
      public void run() {
        try {
          Thread.sleep(50);
          as.set();
        } catch (InterruptedException e) {
        }
      }
    };
    worker.start();
    assertTrue("WaitOne hung", as.waitOne());
    assertTrue(as.waitOne());
    as.reset();
    assertFalse("Reset failed to do its job", as.isFlag());
  }
  
  private static class ThreadInterrupter {
    
    private Object _lock = new Object();
    
    private int _timeout;
    private boolean _targetComplete;
    
    private Thread _target;
    
    private Thread _interrupter = new Thread() {
      public void run() {
        synchronized(_lock) {
          try {
            // This is an 'if' rather than a 'while' because we do not want to wait beyond
            // _timeout milliseconds if the target fails to complete
            
            if (!_targetComplete) _lock.wait(_timeout);
          }
          catch(InterruptedException e) {
            // Do nothing if interrupted. simply go to the finally block
          }
          finally {
            // This code may be called due to the timeout on the wait
            // or by an interruption by the target thread.
            if(!_targetComplete)  _target.interrupt();
          }
        }
      }
    };
    
    public ThreadInterrupter() {
      this(Thread.currentThread(), 50);
    }
    
    public ThreadInterrupter(int timeout) {
      this(Thread.currentThread(), timeout);
    }
    
    public ThreadInterrupter(Thread target, int timeout) {
      _target = target;
      _timeout = timeout;
      _targetComplete = false;
    }
    
    public void start() {
      _interrupter.start();
    }
    
    public void targetCompleted() {
      synchronized(_lock) {
        _targetComplete = true;
        _lock.notify();
      }
    }
    
  }
}
