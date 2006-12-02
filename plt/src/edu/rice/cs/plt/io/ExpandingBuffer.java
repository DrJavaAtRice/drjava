package edu.rice.cs.plt.io;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.LinkedList;

/**
 * Abstraction of {@link ExpandingByteBuffer} and {@link ExpandingCharBuffer} to manage
 * indices and bookeeping for these buffers from a single control point.  In general, this
 * class represents an expandable and thread safe buffer of elements of some type.  {@code T} is the
 * type of a sequence of these elements of fixed length {@link #BUFFER_SIZE}.  Subclasses are responsible
 * for managing reading and writing, but need not interact directly with the expanding queue
 * of {@code T}s, nor with the indices used to manage this queue.  Instead, the methods in
 * this class provide the necessary tools.  Synchronization should occur on the ExpandingBuffer
 * object to prevent conflicts between threads before invoking any of this class's helper methods.
 */
public abstract class ExpandingBuffer<T> {
  
  protected static final int BUFFER_SIZE = 1024;
  
  private final LinkedList<T> _buffers;
  
  /*
   * The indices below are assumed to be positive longs.  This assertion is not checked, and they
   * will eventually wrap around, but this assumption holds for over 8 exabytes (8 million terabytes) 
   * of data (assuming 1 byte per element).
   */

  /** 
   * The virtual index of {@code _buffers.getFirst()[0]} (or {@code _nextBuffer} if {@code _buffers} 
   * is empty)
   */
  private long _base;
  
  /**
   * The virtual index of the beginning of the next buffer to be allocated -- {@code BUFFER_SIZE} after 
   * {@code _buffers.getLast()} ({@code _nextBuffer >= _base})
   */
  private long _nextBuffer;

  /**
   * The virtual index of the first character in the virtual buffer ({@code _first >= _base},
   * {@code _first <= _last})
   */
  private long _first;

  /**
   * The virtual index *after* the last character in the virtual buffer ({@code _last >= _first},
   * {@code _last <= _nextBuffer})
   */
  private long _last;
  
  public ExpandingBuffer() {
    _buffers = new LinkedList<T>();
    _base = 0l;
    _nextBuffer = 0l;
    _first = 0l;
    _last = 0l;
  }
  
  /** Create a fixed-size sub-buffer */
  protected abstract T allocateBuffer(int size);
  
  /**
   * @return  the size of the buffer
   */
  public synchronized long size() {
    return _last - _first;
  }
  
  public synchronized boolean isEmpty() {
    return _first == _last;
  }
  
  
  /**
   * Allocate space in the buffer if none is available.  Insures that there is room for at least
   * one new element (and that {@code _buffers} is nonempty).  Should be called <em>before</em>
   * a write is attempted.
   * @return  The amount of space now available at the end of the buffer ({@code > 0})
   */
  protected int allocate() {
    if (_last == _nextBuffer) {
      _buffers.addLast(allocateBuffer(BUFFER_SIZE));
      _nextBuffer += BUFFER_SIZE;
      return BUFFER_SIZE;
    }
    else { return (int) (_nextBuffer - _last); }
  }
  
  /** Determine the number of buffered elements located in {@code _buffers.getFirst()} */
  protected int elementsInFirstBuffer() {
    long secondBuffer = _base + BUFFER_SIZE;
    return (int) (((secondBuffer > _last) ? _last : secondBuffer) - _base);
  }
  
  /**
   * Deallocate the first buffer if it is no longer needed. Return true if deallocation took place.
   * Should be called <em>after</em> a read occurs.
   */
  protected boolean deallocate() {
    long secondBuffer = _base + BUFFER_SIZE;
    if (_first >= secondBuffer) {
      _buffers.removeFirst();
      _base = secondBuffer;
      return true;
    }
    else { return false; }
  }
  
  /** Access the first buffer (assuming it exists) */
  protected T firstBuffer() { return _buffers.getFirst(); }
  
  /** Calculate the first valid index in {@code firstBuffer()} (assuming it exists) */
  protected int firstIndex() { return (int) (_first - _base); }
  
  /** Access the last buffer (assuming it exists) */
  protected T lastBuffer() { return _buffers.getLast(); }
  
  /** Calculate the index after the last valid element in {@code lastBuffer()} (assuming it exists) */
  protected int lastIndex() { return (int) (_last - (_nextBuffer - BUFFER_SIZE)); }
  
  /** Adjust the indices after writing the given number of elements. */
  protected void recordWrite(long written) { _last += written; }
  
  /** Adjust the indices after reading the given number of elements. */
  protected void recordRead(long read) { _first += read; }
}
