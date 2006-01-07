/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import junit.framework.*;
import java.rmi.*;

/**
 * Test cases for the master/slave jvm control framework.
 *
 * @version $Id$
 */
public class IntegratedMasterSlaveTest extends TestCase {
  private MasterImpl _testMaster = new MasterImpl();

  public void testItAll() throws Exception {
    // run a couple of times. each one forks its own jvm so not
    // too many! we run multiple times to prove that the master
    // can invoke multiple slaves (only one active at a time though)
    for (int i = 0; i < 2; i++) {
      _testMaster.runTestSequence();
    }
  }

  public void testImmediateQuit() throws Exception {
    for (int i = 0; i < 5; i++) {
      _testMaster.runImmediateQuitTest();
    }
  }

  private class MasterImpl extends AbstractMasterJVM implements MasterI {
    private char _letter;
    private boolean _justQuit;
    private String _currentTest = "";

    public MasterImpl() {
      super(IntegratedMasterSlaveTest.class.getName() + "$CounterSlave");
    }

    /**
     * In util-20020414-0647, if quitSlave were called between the time the
     * slave was invoked and the time it registered, an IllegalStateException
     * was thrown. The correct behavior, which we test for here, is for the
     * slave to quit as soon as it is started up.
     */
    public synchronized void runImmediateQuitTest() throws Exception {
      _currentTest = "runImmediateQuitTest";
      _justQuit = false;

      // this needs to be reset because the slave is going to check it!
      _letter = 'a';

      invokeSlave(new String[]{"-Djava.system.class.loader=edu.rice.cs.util.newjvm.CustomSystemClassLoader"});
//      invokeSlave();
      
      // we don't wait for it to start before calling quit.
      // This should not throw an exception! It should quickly return,
      // queueing up a quit to be processes ASAP.
      quitSlave();

      // now we just wait for the quit to process
      while (! _justQuit) {
        wait();
      }

      // If we get here, it worked as expected.
      // (All of the post-quit invariants are checked in handleSlaveQuit.
      _currentTest = "";
    }

    public synchronized void runTestSequence() throws Exception {
      _currentTest = "runTestSequence";
      _justQuit = false;
      _letter = 'a';

//      long start, end;
//      start = System.currentTimeMillis();
      invokeSlave(new String[]{"-Djava.system.class.loader=edu.rice.cs.util.newjvm.CustomSystemClassLoader"});
//      invokeSlave();
      wait();  // for handleConnected
//      end = System.currentTimeMillis();
//      System.err.println((end-start) + "ms waiting for invocation");

      ((SlaveI)getSlave()).startLetterTest();

      // now, wait until five getletter calls passed
      // (after fifth call letter is 'f' due to the ++
//      start = System.currentTimeMillis();
      while (_letter != 'f') {
        wait();  // for getLetter()
      }

//      end = System.currentTimeMillis();
//      System.err.println((end-start) + "ms waiting for 'f'");

      // now make some slave calls
//      start = System.currentTimeMillis();
      for (int i = 0; i < 7; i++) {
        int value = ((SlaveI) getSlave()).getNumber();
        assertEquals("value returned by slave", i, value);
      }
//      end = System.currentTimeMillis();
      //System.err.println((end-start) + "ms calling getNumber");

      // OK, time to kill the slave
//      start = System.currentTimeMillis();
      quitSlave();
      wait(); // for quit to finish
//      end = System.currentTimeMillis();
//      System.err.println((end-start) + "ms waiting to quit");
      _currentTest = "";
    }

    public synchronized char getLetter() {
      char ret = _letter;
      _letter++;

      notify();

      return ret;
    }

    protected synchronized void handleSlaveConnected() {
      SlaveI slave = (SlaveI) getSlave();
      assertTrue("slave is set", slave != null);
      assertTrue("startup not in progress", !isStartupInProgress());
      // getLetter should have never been called.
      assertEquals("letter value", 'a', _letter);
      notify();
    }

    protected synchronized void handleSlaveQuit(int status) {
      assertEquals("slave result code", 0, status);
      if (_currentTest.equals("runTestSequence")) {
        // 5 letter calls must have occurred, so 'f' should be next
        assertEquals("last letter returned", 'f', _letter);
      }
      assertTrue("slave is not set", getSlave() == null);
      assertTrue("startup not in progress", !isStartupInProgress());

      // alert test method that quit occurred.
      notify();
      _justQuit = true;
    }

    /**
     * Called if the slave JVM dies before it is able to register.
     * @param cause The Throwable which caused the slave to die.
     */
    public void errorStartingSlave(Throwable cause) throws RemoteException {
      fail("There was an error starting the slave JVM: " + cause);
    }
  }


  /**
   * The slave will exit with error codes in the case of problems,
   * since there is no other thing it can do!
   * <DL>
   * <DT>1</DT><DD>MasterRemote class cast exception.</DD>
   * <DT>2</DT><DD>Incorect value from getLetter</DD>
   * <DT>3</DT><DD>RemoteException caught</DD>
   * <DT>4</DT><DD>Timeout waiting for master JVM to call</DD>
   * <DT>4</DT><DD>Interrupted while waiting for master JVM to call</DD>
   * </DL>
   */
  public static class CounterSlave extends AbstractSlaveJVM implements SlaveI {
    private int _counter = 0;
    private MasterI _master = null;

    public int getNumber() {
      return _counter++;
    }

    protected void handleStart(MasterRemote m) {
      _master = (MasterI) m;
    }

    public void startLetterTest() throws RemoteException {
      // Run this part of the test in a new thread, so this call will
      //  immediately return
      Thread thread = new Thread() {
        public void run() {
          try {
            for (char c = 'a'; c <= 'e'; c++) {
              char got = _master.getLetter();
              if (c != got) {
                System.exit(2);
              }
            }

            // OK, now wait up till 15 seconds for master jvm to call
            Thread.sleep(15000);
            System.exit(4);
          }
          catch (InterruptedException e) {
            System.exit(5);
          }
          catch (RemoteException re) {
            javax.swing.JOptionPane.showMessageDialog(null, re.toString());
            System.exit(3);
          }
          catch (ClassCastException cce) {
            System.exit(1);
          }
        }
      };
      thread.start();
    }
  }

  public interface SlaveI extends SlaveRemote {
    public int getNumber() throws RemoteException;
    public void startLetterTest() throws RemoteException;
  }

  public interface MasterI/*<SlaveType extends SlaveRemote>*/ extends MasterRemote/*<SlaveType>*/ {
    public char getLetter() throws RemoteException;
  }
}
