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

package edu.rice.cs.util;

import edu.rice.cs.drjava.DrJavaTestCase;

/** Attempts to test the correctness of the ReaderWriterLock class, which allows multiple reader and writer threads to
  * safely access a shared resource.  (Multiple readers can be active at a time, but only one writer can be active, 
  * during which time no readers can be active.)  This can be difficult to test because there is little control over 
  * how the threads are actually scheduled.
  * @version $Id: ReaderWriterLockTest.java 5708 2012-08-29 23:52:35Z rcartwright $
  */
public class ReaderWriterLockTest extends DrJavaTestCase {

  protected volatile ReaderWriterLock _lock;

  /** Creates a new lock for the tests. */
  public void setUp() throws Exception {
    super.setUp();
    _lock = new ReaderWriterLock();
  }

  // TO DO: Pull the next few lines out into a Semaphore class

  /** Number of notifications expected before we actually notify. */
  private volatile int _notifyCount = 0;

  /** Object to provide semaphore-like synchronization. */
  private final Object _notifyObject = new Object();

  /** Notifies the _notifyObject (semaphore) when the _notifyCount reaches 0.  (Decrements the count on each call.) */
  private void _notify() {
    synchronized(_notifyObject) {
      _notifyCount--;
      if (_notifyCount <= 0) {
        _notifyObject.notify();
        _notifyCount = 0;
      }
    }
  }

  /** Tests that multiple readers can run without causing deadlock. We can't really impose any ordering on their output.
    */
  public void testMultipleReaders() throws InterruptedException {
    final StringBuffer buf = new StringBuffer();

    // Create three threads
    ReaderThread r1 = new PrinterReaderThread("r1 ", buf);
    ReaderThread r2 = new PrinterReaderThread("r2 ", buf);
    ReaderThread r3 = new PrinterReaderThread("r3 ", buf);

    // Init the count
    _notifyCount = 3;

    // Start the readers
    synchronized(_notifyObject) {
      r1.start();
      r2.start();
      r3.start();
      _notifyObject.wait();
    }
//    String output = buf.toString();
//    System.out.println(output);
    r1.join();
    r2.join();
    r3.join();
  }

  /** Tests that multiple writers run in mutually exclusive intervals without causing deadlock. */
  public void testMultipleWriters() throws InterruptedException {
    final StringBuffer buf = new StringBuffer();

    // Create three threads
    WriterThread w1 = new PrinterWriterThread("w1 ", buf);
    WriterThread w2 = new PrinterWriterThread("w2 ", buf);
    WriterThread w3 = new PrinterWriterThread("w3 ", buf);

    // Init the count
    _notifyCount = 3;

    // Start the readers
    synchronized(_notifyObject) {
      w1.start();
      w2.start();
      w3.start();
      _notifyObject.wait();
    }
    String output = buf.toString();
    //System.out.println(output);

    // Writer output should never be interspersed.
    assertTrue("w1 writes should happen in order", output.indexOf("w1 w1 w1 ") != -1);
    assertTrue("w2 writes should happen in order", output.indexOf("w2 w2 w2 ") != -1);
    assertTrue("w1 writes should happen in order", output.indexOf("w3 w3 w3 ") != -1);
    
    w1.join();
    w2.join();
    w3.join();
  }

  /** Ensure that a single thread can perform multiple reads. */
  public void testReaderMultipleReads() throws InterruptedException {
    // Simulate a reader that performs multiple reads in one thread
    _lock.startRead();
    _lock.startRead();
    _lock.endRead();
    _lock.endRead();

    // Test that a reading thread can perform an additional read even if
    //  a writing thread is waiting.
    _lock.startRead();
    Thread w = new Thread() {
      public void run() {
        synchronized(_lock) {
          _lock.notifyAll();
          _lock.startWrite();
          // Waits here and releases _lock...
          _lock.endWrite();
        }
      }
    };
    synchronized(_lock) {
      w.start();
      _lock.wait();
    }
    _lock.startRead();
    _lock.endRead();
    _lock.endRead();
    
    w.join();
  }

  /** Ensure that a reading thread cannot perform a write. */
  public void testCannotWriteInARead() {
    try {
      _lock.startRead();
      _lock.startWrite();
      fail("Should have caused an IllegalStateException!");
    }
    catch (IllegalStateException ise) {
      // Good, that's what we want
    }
  }

  /** Ensure that a writing thread cannot perform an additional write. */
  public void testCannotWriteInAWrite() {
    try {
      _lock.startWrite();
      _lock.startWrite();
      fail("Should have caused an IllegalStateException!");
    }
    catch (IllegalStateException ise) {
      // Good, that's what we want
    }
  }

  /** Ensure that a writing thread cannot perform a read. */
  public void testCannotReadInAWrite() {
    try {
      _lock.startWrite();
      _lock.startRead();
      fail("Should have caused an IllegalStateException!");
    }
    catch (IllegalStateException ise) {
      // Good, that's what we want
    }
  }


  /** We would like to test the following schedule.
   *
   * <pre>
   * W1 |***********|
   * W2   |..........*****|
   * R1     |..............********|
   * R2       |............****|
   * W3         |...................***|
   * R3           |.....................****|
   * R4                |................*******|
   * R5                                   |***|
   * </pre>
   *
   * Key: "." means waiting, "*" means running
   *
   * This is next to impossible to set up in Java.  What we'd really
   * like is a unit-testing framework that allows us to easily specify
   * such a schedule in a test.  (Conveniently, Corky Cartwright has
   * applied for a Texas ATP grant to develop just such a framework.)
   *
   *
   * So, instead, we'll just set up these threads, let them run, and
   * enforce that no one interferes with output from a writer.
   * 
   * NOTE: this test occasionally generated an error 
   * 
   * java.lang.StringIndexOutOfBoundsException: String index out of range: 72
   * 
   * on line 263 (fetching the contents of buf for testing) when the code used a 
   * StringBuilder instead of a STringBuffer.  Shy?  Could the _notify operation 
   * in print threads be reordered with respect to appending to buf?  If so, does
   * using StringBuffer instead of StringBuilder completely fix the bug?  Can two
   * synchronized operations within a thread be reordered?  Prior to the 
   * Builder/Buffer change, the fact that _buf is final presumably did not ensure 
   * sequential consistency in operatoins on _buf (accessing _buf is distinct from
   * performing operations on its value).
   */
  public void testMultipleReadersAndWriters() throws InterruptedException {
    final StringBuffer buf = new StringBuffer();

    // Create threads
    WriterThread w1 = new PrinterWriterThread("w1 ", buf);
    WriterThread w2 = new PrinterWriterThread("w2 ", buf);
    WriterThread w3 = new PrinterWriterThread("w3 ", buf);

    ReaderThread r1 = new PrinterReaderThread("r1 ", buf);
    ReaderThread r2 = new PrinterReaderThread("r2 ", buf);
    ReaderThread r3 = new PrinterReaderThread("r3 ", buf);
    ReaderThread r4 = new PrinterReaderThread("r4 ", buf);
    ReaderThread r5 = new PrinterReaderThread("r5 ", buf);

    // Init the count
    _notifyCount = 8;

    // Start the readers
    synchronized(_notifyObject) {
      w1.start();
      w2.start();
      r1.start();
      r2.start();
      w3.start();
      r3.start();
      r4.start();
      r5.start();
      _notifyObject.wait();
    }
    String output = buf.toString();
    //System.out.println(output);

    // Writer output should never be interspersed.
    assertTrue("w1 writes should happen in order", output.indexOf("w1 w1 w1 ") != -1);
    assertTrue("w2 writes should happen in order", output.indexOf("w2 w2 w2 ") != -1);
    assertTrue("w3 writes should happen in order", output.indexOf("w3 w3 w3 ") != -1);
    
    w1.join();
    w2.join();
    w3.join();
    r1.join();
    r2.join();
    r3.join();
    r4.join();
    r5.join();
  }


  /** A reader thread. */
  public abstract class ReaderThread extends Thread {
    public abstract void read() throws Throwable;
    public void run() {
      _lock.startRead();
      try { read(); }
      catch (Throwable t) { t.printStackTrace(); }
      _lock.endRead();
    }
  }

  /** A writer thread. */
  public abstract class WriterThread extends Thread {
    public abstract void write() throws Throwable;
    public void run() {
      _lock.startWrite();
      try { write(); }
      catch (Throwable t) { t.printStackTrace(); }
      _lock.endWrite();
    }
  }

  /** A ReaderThread which repeatedly prints to a buffer. */
  public class PrinterReaderThread extends ReaderThread {
    volatile PrintCommand _command;
    public PrinterReaderThread(String msg, final StringBuffer buf) { _command = new PrintCommand(msg, buf); }
    public void read() { _command.print(); }
  }

  /** A WriterThread which repeatedly prints to a buffer. */
  public class PrinterWriterThread extends WriterThread {
    volatile PrintCommand _command;
    public PrinterWriterThread(String msg, final StringBuffer buf) { _command = new PrintCommand(msg, buf); }
    public void write() { _command.print(); }
  }

  /** Command pattern class to print to a buffer. */
  public class PrintCommand {
    /** Number of times to print */
    volatile int _numIterations = 3;
    /** Number of milliseconds to wait between iterations */
    volatile int _waitMillis = 5;
    /** Buffer to print to */
    final StringBuffer _buf;
    /** Message to print */
    final String _msg;
    /** Creates a new command to print to a buffer during a read or write. */
    public PrintCommand(String msg, StringBuffer buf) {
      _msg = msg;
      _buf = buf;
    }
    /** Prints the message to the buffer. */
    public void print() {
      for (int i = 0; i < _numIterations; i++) {
        _buf.append(_msg);
        try { Thread.sleep(_waitMillis); }
        catch (InterruptedException e) { _buf.append(e); }
      }
      _notify();
    }
  }
}
