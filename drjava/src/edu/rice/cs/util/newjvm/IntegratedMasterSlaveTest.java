/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.newjvm;

import junit.framework.*;
import java.io.*;
import java.rmi.*;

/**
 * Test cases for the master/slave jvm control framework.
 *
 * @version $Id$
 */
public class IntegratedMasterSlaveTest extends TestCase {
  private MasterImpl _testMaster = new MasterImpl();

  /**
   * Constructor.
   * @param  String name
   */
  public IntegratedMasterSlaveTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return new TestSuite(IntegratedMasterSlaveTest.class);
  }
  
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
  
  private class MasterImpl extends AbstractMasterJVM implements MasterI
  {
    private char _letter;
    private boolean _justQuit;
   
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
      _justQuit = false;

      // this needs to be reset because the slave is going to check it!
      _letter = 'a';

      invokeSlave();
      
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
    }
    
    public synchronized void runTestSequence() throws Exception {
      _justQuit = false;
      _letter = 'a';
      
      long start, end;
      start = System.currentTimeMillis();
      invokeSlave();
      wait(); // for connection
      end = System.currentTimeMillis();
      //System.err.println((end-start) + "ms waiting for invocation");
      
      // now, wait until five getletter calls passed
      // (after fifth call letter is 'f' due to the ++
      start = System.currentTimeMillis();
      while (_letter != 'f') {
        wait();
      }

      end = System.currentTimeMillis();
      //System.err.println((end-start) + "ms waiting for 'f'");
      
      // now make some slave calls
      start = System.currentTimeMillis();
      for (int i = 0; i < 7; i++) {
        int value = ((SlaveI) getSlave()).getNumber();
        assertEquals("value returned by slave", i, value);
      }
      end = System.currentTimeMillis();
      //System.err.println((end-start) + "ms calling getNumber");
      
      // OK, time to kill the slave
      start = System.currentTimeMillis();
      quitSlave();
      wait(); // for quit to finish
      end = System.currentTimeMillis();
      //System.err.println((end-start) + "ms waiting to quit");      
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
      // 5 letter calls must have occurred, so 'f' should be next
      assertEquals("last letter returned", 'f', _letter);
      assertTrue("slave is not set", getSlave() == null);
      assertTrue("startup not in progress", !isStartupInProgress());

      // alert test method that quit occurred.
      notify();
      _justQuit = true;
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

    public int getNumber() {
      return _counter++;
    }
    
    protected void handleStart(MasterRemote m) {
      try {
        MasterI master = (MasterI) m;
        for (char c = 'a'; c <= 'e'; c++) {
          char got = master.getLetter();
          if (c != got) {
            System.exit(2);
          }
        }
        
        // OK, now wait up till 15 seconds for master jvm to call
        Thread.currentThread().sleep(15000);
        System.exit(4);
      }
      catch (InterruptedException e) {
        System.exit(5);
      }
      catch (RemoteException re) {
        System.exit(3);
      }
      catch (ClassCastException cce) {
        System.exit(1);
      }
    }
  }
  
  public interface SlaveI extends SlaveRemote {
    public int getNumber() throws RemoteException;
  }
  
  public interface MasterI/*<SlaveType extends SlaveRemote>*/ extends MasterRemote/*<SlaveType>*/ {
    public char getLetter() throws RemoteException;
  }
}
