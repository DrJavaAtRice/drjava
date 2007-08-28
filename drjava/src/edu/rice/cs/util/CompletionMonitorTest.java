/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;
import junit.framework.*;

/**
 * Test case for CompletionMonitorTest
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
