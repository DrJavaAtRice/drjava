package edu.rice.cs.util;

import junit.framework.TestCase;

/**
 * Attempts to test the correctness of the ReaderWriterLock class,
 * which allows multiple reader and writer threads to safely access
 * a shared resource.  (Multiple readers can be active at a time, but
 * only one writer can be active, during which time no readers can
 * be active.)
 * 
 * This can be difficult to test because there is little control over
 * how the threads are actually scheduled.
 * 
 * @version $Id$
 */
public class ReaderWriterLockTest extends TestCase {
  
  protected ReaderWriterLock _lock;
  
  /**
   * Creates a new lock for the tests.
   */
  public void setUp() {
    _lock = new ReaderWriterLock();
  }
  
  // TO DO: Pull the next few lines out into a Semaphore class
  
  /**
   * Number of notifications expected before we actually notify.
   */
  private int _notifyCount = 0;
  
  /**
   * Object to provide semaphore-like synchronization.
   */
  private final Object _notifyObject = new Object();
  
  /**
   * Notifies the _notifyObject (semaphore) when the _notifyCount
   * reaches 0.  (Decrements the count on each call.)
   */
  private void _notify() {
    synchronized (_notifyObject) {
      _notifyCount--;
      if (_notifyCount <= 0) { 
        _notifyObject.notify();
        _notifyCount = 0;
      }
    }
  }
  
  /**
   * Tests that multiple readers can run without causing deadlock.
   * We can't really impose any ordering on their output.
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
    synchronized (_notifyObject) {
      r1.start();
      r2.start();
      r3.start();
      _notifyObject.wait();
    }
    String output = buf.toString();
    //System.out.println(output);
  }
  
  /**
   * Tests that multiple writers run in mutually exclusive intervals
   * without causing deadlock.
   */
  public void testMultipleWriters() throws InterruptedException {
    final StringBuffer buf = new StringBuffer();
    
    // Create three threads
    WriterThread w1 = new PrinterWriterThread("w1 ", buf);
    WriterThread w2 = new PrinterWriterThread("w2 ", buf);
    WriterThread w3 = new PrinterWriterThread("w3 ", buf);
    
    // Init the count
    _notifyCount = 3;
    
    // Start the readers
    synchronized (_notifyObject) {
      w1.start();
      w2.start();
      w3.start();
      _notifyObject.wait();
    }
    String output = buf.toString();
    //System.out.println(output);
    
    // Writer output should never be interspersed.
    assertTrue("w1 writes should happen in order",
               output.indexOf("w1 w1 w1 ") != -1);
    assertTrue("w2 writes should happen in order",
               output.indexOf("w2 w2 w2 ") != -1);
    assertTrue("w1 writes should happen in order",
               output.indexOf("w3 w3 w3 ") != -1);
  }
  
  /**
   * Ensure that a single thread can perform multiple reads.
   */
  public void testReaderMultipleReads() throws InterruptedException {
    // Simulate a reader that performs multiple reads in one thread
    _lock.startRead();
    _lock.startRead();
    _lock.endRead();
    _lock.endRead();
    
    // Test that a reading thread can perform an additional read even if
    //  a writing thread is waiting.
    final Object o = new Object();
    _lock.startRead();
    Thread w = new Thread() {
      public void run() {
        synchronized (_lock) {
          _lock.notifyAll();
          _lock.startWrite();
          // Waits here and releases _lock...
          _lock.endWrite();
        }
      }
    };
    synchronized (_lock) {
      w.start();
      _lock.wait();
    }
    _lock.startRead();
    _lock.endRead();
    _lock.endRead();
  }
  
  /**
   * Ensure that a reading thread cannot perform a write.
   */
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
  
  /**
   * Ensure that a writing thread cannot perform an additional write.
   */
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
  
  /**
   * Ensure that a writing thread cannot perform a read.
   */
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
  
  
  /**
   * We would like to test the following schedule.
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
    synchronized (_notifyObject) {
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
    assertTrue("w1 writes should happen in order",
               output.indexOf("w1 w1 w1 ") != -1);
    assertTrue("w2 writes should happen in order",
               output.indexOf("w2 w2 w2 ") != -1);
    assertTrue("w1 writes should happen in order",
               output.indexOf("w3 w3 w3 ") != -1);
  }
  
  
  /**
   * A reader thread.
   */
  public abstract class ReaderThread extends Thread {
    public abstract void read() throws Throwable;
    public void run() {
      _lock.startRead();
      try {
        read();
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
      _lock.endRead();
    }
  }
  
  /**
   * A writer thread.
   */
  public abstract class WriterThread extends Thread {
    public abstract void write() throws Throwable;
    public void run() {
      _lock.startWrite();
      try {
        write();
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
      _lock.endWrite();
    }
  }
  
  /**
   * A ReaderThread which repeatedly prints to a buffer.
   */
  public class PrinterReaderThread extends ReaderThread {
    PrintCommand _command;
    public PrinterReaderThread(String msg, StringBuffer buf) {
      _command = new PrintCommand(msg, buf);
    }
    public void read() {
      _command.print();
    }
  }
  
  /**
   * A WriterThread which repeatedly prints to a buffer.
   */
  public class PrinterWriterThread extends WriterThread {
    PrintCommand _command;
    public PrinterWriterThread(String msg, StringBuffer buf) {
      _command = new PrintCommand(msg, buf);
    }
    public void write() {
      _command.print();
    }
  }
  
  /**
   * Command pattern class to print to a buffer.
   */
  public class PrintCommand {
    /** Number of times to print */
    int _numIterations = 3;
    /** Number of milliseconds to wait between iterations */
    int _waitMillis = 5;
    /** Buffer to print to */
    StringBuffer _buf;
    /** Message to print */
    String _msg;
    /** Creates a new command to print to a buffer during a read or write. */
    public PrintCommand(String msg, StringBuffer buf) {
      _msg = msg;
      _buf = buf;
    }
    /** Prints the message to the buffer. */
    public void print() {
      for (int i=0; i < _numIterations; i++) {
        _buf.append(_msg);
        try {
          Thread.sleep(_waitMillis);
        }
        catch (InterruptedException e) {
          _buf.append(e);
        }
      }
      _notify();
    }
  }
}
