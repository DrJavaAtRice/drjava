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

package edu.rice.cs.util.newjvm;

import edu.rice.cs.drjava.DrJavaTestCase;

import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.concurrent.JVMBuilder;

import java.rmi.RemoteException;

/** Test cases for the master/slave jvm control framework.
  * @version $Id: IntegratedMasterSlaveTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class IntegratedMasterSlaveTest extends DrJavaTestCase {
  
  volatile TestMasterJVM _testMaster;
  
  public void setUp() throws Exception {
    super.setUp();
    _testMaster = new TestMasterJVM();
  }
  
  public void tearDown() throws Exception {
    _testMaster.dispose();
    super.tearDown();
  }
  
  // this test uses thread pools and starts a THRAD_EXECUTOR-n thread that we cannot join
  public void testItAll_NOJOIN() throws Exception {
    // run a couple of times. each one forks its own jvm so not
    // too many! we run multiple times to prove that the master
    // can invoke multiple slaves (only one active at a time though)
    for (int i = 0; i < 2; i++) _testMaster.runTestSequence();
  }
  
  // this test uses thread pools and starts a THRAD_EXECUTOR-n thread that we cannot join
  public void testImmediateQuit_NOJOIN() throws Exception {
    for (int i = 0; i < 5; i++)  _testMaster.runImmediateQuitTest();
  }
  
  private static class TestMasterJVM extends AbstractMasterJVM implements TestMasterRemote {
    
    private static final int WAIT_TIMEOUT = 10000; 
    
    /** Field and lock used to signal slave quit events. */
    private final CompletionMonitor _justQuit = new CompletionMonitor(); 
    
    /** Field and lock used to signal slave connected events. */
    private volatile TestSlaveRemote _slave;                    // non-null when slave is connected
//    private final Object _slaveLock = new Object();           // nothing waits on this lock
    
    /** Field and lock used to signal letter change events. */
    private volatile char _letter;
    private final Object _letterLock = new Object();
    
    private volatile String _currentTest = "";
    
    public TestMasterJVM() { super(CounterSlave.class.getName()); }
    
    /** In util-20020414-0647, if quitSlave were called between the time the slave was invoked and the time it 
      * registered, an IllegalStateException was thrown. The correct behavior, which we test for here, is for the
      * slave to quit as soon as it is started up.
      */
    public void runImmediateQuitTest() throws Exception {
      _currentTest = "runImmediateQuitTest";
      _justQuit.reset(); 
      _slave = null;
      _letter = 'a';  // this needs to be reset because the slave is going to check it!
      
      new Thread() {
        public void run() { invokeSlave(JVMBuilder.DEFAULT); }
      }.start();
      
      // Immediately call quit, which should not throw an exception.
      quitSlave();
      assertTrue(_justQuit.attemptEnsureSignaled(WAIT_TIMEOUT));
      _currentTest = "";  // If we get here, it worked as expected.
      // (All of the post-quit invariants are checked in handleSlaveQuit.
    }
    
    public void runTestSequence() throws Exception {
      _currentTest = "runTestSequence";
      _justQuit.reset();
      _slave = null;
      _letter = 'a';
      
      invokeSlave(JVMBuilder.DEFAULT);           
      _slave.startLetterTest();
      // now, wait until five getletter calls passed; after fifth call letter is 'f' due to the ++
      synchronized(_letterLock) { while (_letter != 'f') { _letterLock.wait(); } }
      for (int i = 0; i < 7; i++) {
        int value = _slave.getNumber();
        assertEquals("value returned by slave", i, value);
      }
      
      quitSlave();
      assertTrue(_justQuit.attemptEnsureSignaled(WAIT_TIMEOUT));
      _currentTest = "";
    }
    
    public char getLetter() {
      synchronized(_letterLock) {
        char ret = _letter;
        _letter++;
        _letterLock.notify();
        return ret;
      }
    }
    
    @Override protected void handleSlaveConnected(SlaveRemote slave) {
      // getLetter should have never been called.
      assertEquals("letter value", 'a', _letter);
//      synchronized(_slaveLock) {
      _slave = (TestSlaveRemote) slave;
//        _slaveLock.notify();
//    }
    }
    
    @Override protected void handleSlaveQuit(int status) {
      assertEquals("slave result code", 0, status);
      if (_currentTest.equals("runTestSequence")) {
        // 5 letter calls must have occurred, so 'f' should be next
        assertEquals("last letter returned", 'f', _letter);
      }
      // alert test method that quit occurred.
      _justQuit.signal();
    }
    
    @Override protected void handleSlaveWontStart(Exception e) {
      fail("There was an error starting the slave JVM: " + e);
    }
  }
  
  /** The slave will exit with error codes in the case of problems, since there is no other thing it can do!
    * <DL>
    * <DT>1</DT><DD>MasterRemote class cast exception.</DD>
    * <DT>2</DT><DD>Incorect value from getLetter</DD>
    * <DT>3</DT><DD>RemoteException caught</DD>
    * <DT>4</DT><DD>Timeout waiting for master JVM to call</DD>
    * <DT>5</DT><DD>Interrupted while waiting for master JVM to call</DD>
    * </DL>
    */
  public static class CounterSlave extends AbstractSlaveJVM implements TestSlaveRemote {
    
    public static final CounterSlave ONLY = new CounterSlave();
    
    private volatile int _counter = 0;
    private volatile TestMasterRemote _master = null;
    
    private CounterSlave() { }
    
    /* Some inherited methods are synchronized */
    public synchronized int getNumber() { return _counter++; }
    
    protected void handleStart(MasterRemote m) { _master = (TestMasterRemote) m; }
    
    public void startLetterTest() {
      // Run this part of the test in a new thread, so this call will immediately return
      Thread thread = new Thread() {
        public void run() {
          try {
            for (char c = 'a'; c <= 'e'; c++) {
              char got = _master.getLetter();
              if (c != got) System.exit(2);
            }
            
            // OK, now wait up until 15 seconds for master jvm to quit the slave
            Thread.sleep(15000);
            System.exit(4);
          }
          catch (InterruptedException e) { System.exit(5); }
          catch (RemoteException re) { System.exit(3); }
          catch (ClassCastException cce) { System.exit(1); }
        }
      };
      thread.start();
    }
  }
  
  public interface TestSlaveRemote extends SlaveRemote {
    public int getNumber() throws RemoteException;
    public void startLetterTest() throws RemoteException;
  }
  
  public interface TestMasterRemote extends MasterRemote {
    public char getLetter() throws RemoteException;
  }
}
  
