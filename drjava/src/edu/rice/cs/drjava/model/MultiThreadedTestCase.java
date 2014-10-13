/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.ui.DefinitionsPaneTest;

import junit.framework.AssertionFailedError;

/** TestCase which can fail if another thread causes an error or failure.
  * @version $Id: MultiThreadedTestCase.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class MultiThreadedTestCase extends DrJavaTestCase {
  public MultiThreadedTestCase() { super(); }
  public MultiThreadedTestCase(String name) { super(name); }  
  
  /** Flag to keep track of whether or not a test failed in another thread (not the testing thread). */
  protected volatile static boolean _testFailed = false;

  /** Initialize test state to not failed. 
    * @throws Exception  This convention is mandated by the JUnit TestCase class which is an ancestor of this class. 
    */
  public void setUp() throws Exception {
    super.setUp();
    _testFailed = false;
    ExceptionHandler.ONLY.reset();
    Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.ONLY);
  }

  /** If any test failed, print a message saying that some test failed in another thread (not the testing thread). */
  public void tearDown() throws Exception {
    ExceptionHandler.ONLY.rethrow();
    if (_testFailed) fail("test failed in another thread");
    DefinitionsPaneTest._log.log("MultithreadedTestCase.tearDown() calling super.tearDown()");
    super.tearDown();
  }

  /** This method prints the failure message to System.out and kills the JVM.  Just calling fail() doesn't always cause
    * the test to fail, because the listener is often called from another thread.
    */
  protected static void listenerFail(String s) {
//    StackTraceElement[] trace = Thread.getAllStackTraces().get(Thread.currentThread());
//    System.err.println("TEST FAILED in a listener thread");
//    System.err.println("Failing thread stack trace:\n " + Log.traceToString(trace));
//    new AssertionFailedError(s).printStackTrace(System.out);
    _testFailed = true;
    fail(s);
  }

  /** This method prints the failure message to System.out and kills the JVM.  Just calling fail() doesn't always cause
    * the test to fail, because the listener is often called from another thread.
    */
  protected static void listenerFail(Throwable t) {
    java.io.StringWriter sw = new java.io.StringWriter();
    t.printStackTrace(new java.io.PrintWriter(sw));
    listenerFail(sw.toString());
  }
  
  /** Join with a thread, i.e. continue only after that thread has terminated.  If the join is interrupted, an 
    * UnexpectedException is thrown.
    * @param t thread to join with
    */
  public static void join(Thread t) {
    try { t.join(); }
    catch(InterruptedException e) {
      throw new edu.rice.cs.util.UnexpectedException(e, "Thread.join was unexpectedly interrupted.");
    }
  }
  
  /**  Wait for a notify or notifyAll. If the wait is interrupted, an UnexpectedException is thrown.
   *   @param o object to wait for
   */
  public static void wait(Object o) {
    try { o.wait(); }
    catch(InterruptedException e) {
      e.printStackTrace();
      throw new edu.rice.cs.util.UnexpectedException(e, "Thread.wait was unexpectedly interrupted.");
    }
  }
  
  /** Class that stores exceptions thrown in other threads so they can be rethrown in the main thread.
    * AssertionFailedErrors thrown in other threads do not count as AssertionFailedErrors in the
    * main class, i.e. if an assertion fails in a thread that is not the main thread, the unit test will not fail!
    */
  private static class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    
    /** Stored throwable, or null if nothing stored. */
    private volatile Throwable _e = null;
    
    /** Stored thread that threw or null if none. */
    private volatile java.lang.Thread _t = null;
    
    /**  Thread that spawns the other threads. */
    private volatile java.lang.Thread _mainThread = java.lang.Thread.currentThread();
    
    /** Gets called if an uncaught exception occurs in a thread.
      * @param t the thread
      * @param e the uncaught exception
      */
    public void uncaughtException(java.lang.Thread t, Throwable e) {
      _t = t;
      _e = e;
      if (_mainThread != null) {
//        System.err.println("***Uncaught Exception in spawned thread within a MultiThreadedTestCase:");
        e.printStackTrace(System.out);
        _mainThread.interrupt();
      }
    }
    
    /** Reset the stored exception and thread. */
    public void reset() {
      _t = null;
      _e = null;
    }
    
    /** Rethrow the exception, if one was stored. */
    public void rethrow() {
      if (exceptionOccurred()) {
        if (_e instanceof Error)  throw (Error)_e;
        if (_e instanceof RuntimeException) throw (RuntimeException)_e;
        else {
          // avoid checked exceptions
          throw new AssertionFailedError("Exception in thread " + _t + ": " + _e);
        }
      }            
    }
    
    /** Returns true if an exception has occurred.
      * @return true if exception has occurred
      */
    public boolean exceptionOccurred() { return (_e != null); }
    
    public Throwable getException() { return _e; }
    
    public java.lang.Thread getThread() { return _t; }
    
    /** Set the thread that spawns the other threads. */
    public void setMainThread(java.lang.Thread mainThread) { _mainThread = mainThread; }
    
    /** Singleton constructor. */
    private ExceptionHandler() { }
    
    /** Singleton instance. */
    public static final ExceptionHandler ONLY = new ExceptionHandler();
  }
}
