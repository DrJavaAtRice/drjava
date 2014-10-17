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

import java.util.LinkedList;

/** This class implements synchronization primitives to solve the classic readers/writers problem without deadlock or
  * starvation. <p>
  * Problem: Suppose multiple threads want to read and write a resource. Multiple threads can safely read at the same
  * time, but only one thread can safely write at a time, and no threads can read while a thread is writing. </p> <p>
  * We must be careful to avoid starvation in our solution, so that a steady flow of reader threads cannot block 
  * waiting writer threads indefinitely. This property can be achieved by imposing an ordering on the incoming readers
  * and writers. </p> <p>
  * To use this class, instantiate a ReaderWriterLock in the class holding the shared resource.  Any methods which read
  * from the resource must call startRead before reading and endRead after reading, and must not be synchronized 
  * themselves.  Similarly, any methods which write to the resource must not be synchronized and must call startWrite 
  * before writing and endWrite after writing. </p> <p>
  * This class enforces that any readers and writers that are forced to wait are given access to the shared resource in 
  * the order in which they arrive. Groups of readers are allowed to proceed simultaneously, where each group contains 
  * all waiting readers that arrived before the next waiting writer.
  * </p> <p>
  * This class is loosely adapted from the "Starvation-Free Readers and Writers Monitor" available from Stephen J. 
  * Hartley (Drexel University) at: http://www.mcs.drexel.edu/~shartley/ConcProgJava/monitors.html
  * We have imposed an ordering on the pending waiting readers and writers using an ordered queue.
  * </p>
  * TODO: revise this formulation of readers/writers to allow writers to recursively readLock and writeLock!
  * @version $Id: ReaderWriterLock.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class ReaderWriterLock {
  /** The number of readers currently reading. */
  private volatile int _numActiveReaders = 0;
  /** The number of writers currently writing (ie. 0 or 1). */
  private volatile int _numActiveWriters = 0;
  /** The number of readers waiting to read. */
  private volatile int _numWaitingReaders = 0;
  /** The number of writers waiting to write. */
  private volatile int _numWaitingWriters = 0;
  
  /** Queue of all waiting reader and writer threads.  The front of the queue (first element of the list) represents 
    * the thread which arrived first; new waiting threads are added at the end. "Groups" on the queue refer to several
    * sequential Readers or a single Writer.  (We can wake up the front group on the queue each time a writer finishes 
    * and each time the last reader finishes.)
    */
  private final LinkedList<ReaderWriterThread> _waitQueue;
  
  /** A list of the Threads that are currently reading or writing.
   * We maintain this list to prevent the deadlock which would occur if
   * a Thread which is reading or writing attempted to read or write again.
   * (If that happens, we throw an IllegalStateException.)
   */
  private final LinkedList<Thread> _runningThreads;
  
  /** Creates a new ReaderWriterLock. */
  public ReaderWriterLock() {
    _waitQueue = new LinkedList<ReaderWriterThread>();
    _runningThreads = new LinkedList<Thread>();
  }
  
  /** Must be called by each reader thread before starting to read.  The calling method must <i>not</i> be 
    * synchronized.  This method blocks the reader if there are current active or waiting writers, until those 
    * writers have finished.
    * @throws IllegalStateException if the thread is already a reader or writer
    */
  public synchronized void startRead() {
    // If we're already reading, we can perform another read without waiting
    if (!_alreadyReading()) {
      
      // Make sure this thread isn't already writing.
      _ensureNotAlreadyRunning();
      
      // Check if any writers are active or waiting
      if (_numWaitingWriters > 0 || _numActiveWriters > 0) {
        // If so, we wait until it's our turn (on the waitQueue)
        _numWaitingReaders++;
        Reader r = new Reader();
        r.startWaiting();
        
        // Ok, we're no longer on the waitQueue
        _numWaitingReaders--;
      }
    }
    
    // Ok, start the read
    _numActiveReaders++;
    _runningThreads.add(Thread.currentThread());
  }
  
  /** Must be called by each reader thread after it is finished reading.  The calling method must <i>not</i> be 
    * synchronized. This method wakes up a waiting writer if there are no remaining reader threads actively reading. 
    * @throws IllegalStateException if the thread is already a reader or writer
    */
  public synchronized void endRead() {
    if (_numActiveReaders == 0) {
      throw new IllegalStateException("Trying to end a read with no active readers!");
    }
    
    _numActiveReaders--;
    _ensureAlreadyRunning();
    _runningThreads.remove(Thread.currentThread());
    
    // There shouldn't be any active writers at this point
    if (_numActiveWriters > 0) {
      String msg = "A writer was active during a read!";
      throw new UnexpectedException(new Exception(msg));
    }
    
    // Only safe to write if there are no more active readers
    if (_numActiveReaders == 0) {
      // Wake up any waiting writers
      //  (We expect there to be a writer at the front, if anything.)
      _wakeFrontGroupOfWaitQueue();
    }
  }
  
  /** Must be called by each writer thread before starting to write.  The calling method must <i>not</i> be 
    * synchronized. This method blocks the writer if there are any active readers or writers, and prevents any new 
    * readers from starting to read until this writer gets a chance to write.
    * @throws IllegalStateException if the thread is already a reader or writer
    */
  public synchronized void startWrite() {
    // Make sure this thread isn't already reading or writing.
    _ensureNotAlreadyRunning();
    
    // Can only write if no other readers *or* writers
    // Note: normally, there will be no waiting readers/writers if there
    //  are no active reader/writers.  However, a new thread could call
    //  startWrite at just the wrong time, allowing it to sneak in after
    //  the last reader/writer finished, while others are waiting.  Thus,
    //  we also check to see if anyone is waiting.
    if ((_numActiveReaders > 0 || _numActiveWriters > 0) ||
        (_numWaitingReaders > 0 || _numWaitingWriters > 0)) {
      // Must wait
      _numWaitingWriters++;
      
      // If _okToWrite is true, it means there are no active writers (and thus
      //  there are active readers).  We set it to false so that we wait until
      //  the last reader finishes, setting it back to true in endRead().
      //_okToWrite = false;
      
      Writer w = new Writer();
      w.startWaiting();
      
      _numWaitingWriters--;
    }
    
    // We're writing now, so it's not ok for others to write
    _numActiveWriters++;
    _runningThreads.add(Thread.currentThread());
  }
  
  /** Must be called by each writer thread after it is finished writing.  The calling method must <i>not</i> be 
    * synchronized. This method wakes up any waiting readers and writers.  If there are waiting readers, they read 
    * before the next writer, but any new readers (after this call) wait until the next waiting writer writes.
    * @throws IllegalStateException if the thread is already a reader or writer
    */
  public synchronized void endWrite() {
    if (_numActiveWriters != 1) {
      throw new IllegalStateException("Trying to end a write with " +
                                      _numActiveWriters + " active writers!");
    }
    
    _numActiveWriters--;
    _ensureAlreadyRunning();
    _runningThreads.remove(Thread.currentThread());
    
    // There shouldn't be any active threads at this point
    if ((_numActiveWriters > 0) || (_numActiveReaders > 0)) {
      String msg = "Multiple readers/writers were active during a write!";
      throw new UnexpectedException(new Exception(msg));
    }
    
    // Wake up any waiting readers and writers
    _wakeFrontGroupOfWaitQueue();
  }
  
  /** Checks if the current thread is already a reader. */
  private boolean _alreadyReading() {
    // If the current thread is active, and there are active readers, then
    //  the current thread must be a reader and not a writer.
    return _numActiveReaders > 0 &&
      _runningThreads.contains(Thread.currentThread());  // How can the executing thread not be among those running?
    
  }
  
  /** Ensures that the current thread is not already considered a reader or writer.  This prevents deadlock if a reader 
    * thread is trying to write (or vice versa).
    * @throws IllegalStateException if the thread is already a reader or writer
    */
  private void _ensureNotAlreadyRunning() {
    if (_runningThreads.contains(Thread.currentThread())) {
      throw new DeadlockException("Same thread cannot read or write multiple " +
                                  "times!  (Would cause deadlock.)");
    }
  }
  
  /** Ensures that the current thread is already considered a reader or writer.  This prevents deadlock if a reader 
    * thread is trying to write (or vice versa).
    * @throws IllegalStateException if the thread is already a reader or writer
    */
  private void _ensureAlreadyRunning() {
    if (! _runningThreads.contains(Thread.currentThread())) {
      throw new IllegalStateException("Current thread did not initiate a read or write!");
    }
  }
  
  /** Wakes up either the writer or all sequential readers before a writer at the front of the waitQueue. */
  private synchronized void _wakeFrontGroupOfWaitQueue() {
    if (!_waitQueue.isEmpty()) {
      // Wake, whether it's a reader or writer
      ReaderWriterThread front = _waitQueue.getFirst();
      front.stopWaiting();  // removes front from queue
      
      // If it's a reader, wake up more until we find a writer
      if (front.isReader()) {
        while (!_waitQueue.isEmpty()) {
          front = _waitQueue.getFirst();
          if (front.isReader()) {
            front.stopWaiting();  // removes front from queue
          }
          else {
            // Found a writer, so we're done
            break;
          }
        }
      }
    }
  }
  
  
  /** Represents a thread waiting to either read or write.  Instances of this class are placed in a queue to enforce
    * the correct order when allowing new threads to read or write.  The waiting thread must call wait() on this object,
    * allowing it to be notified when it reaches the front of the queue.  This object will remain on the queue until the
    * thread completes its read or write, allowing us to check for and prevent deadlock if the same thread tries to both 
    * read and write at the same time.
    */
  public abstract class ReaderWriterThread {
    private volatile boolean _isWaiting = true;
    /** Returns whether this ReaderWriter is a writer. */
    public abstract boolean isWriter();
    /** Returns whether this ReaderWriter is a reader. */
    public abstract boolean isReader();
    
    /** Causes this ReaderWriterThread to wait until stopWaiting is called. While it's waiting, it is on the waitQueue.
      */
    public void startWaiting() {
      synchronized(ReaderWriterLock.this) {
        _isWaiting = true;
        _waitQueue.addLast(this);
        while (_isWaiting) {
          try { ReaderWriterLock.this.wait(); }
          catch (InterruptedException e) {
            // loop checks if we still need to wait...
          }
        }
      }
    }
    
    /** Wakes up this ReaderWriterThread, removing it from the waitQueue. */
    public void stopWaiting() {
      synchronized(ReaderWriterLock.this) {
        _isWaiting = false;
        _waitQueue.remove(this);  // note: we must be in the front group!
        ReaderWriterLock.this.notifyAll();
      }
    }
  }
  
  /** Object representing a reader thread which is waiting for read access on the queue of waiting threads. */
  public class Reader extends ReaderWriterThread {
    public boolean isReader() { return true; }
    public boolean isWriter() { return false; }
  }
  
  /** Object representing a writer thread which is waiting for write access on the queue of waiting threads. */
  public class Writer extends ReaderWriterThread {
    public boolean isReader() { return false; }
    public boolean isWriter() { return true; }
  }
  
  /** Class representing a deadlock that would have occurred if the acquire operation had been executed. */
  public static class DeadlockException extends IllegalStateException {
    public DeadlockException() { }
    public DeadlockException(String s) { super(s); }
    public DeadlockException(String s, Throwable t) { super(s,t); }
    public DeadlockException(Throwable t) { super(t); }
  }
}
