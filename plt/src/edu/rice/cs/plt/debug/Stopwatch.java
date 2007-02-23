package edu.rice.cs.plt.debug;

import java.util.List;
import java.util.LinkedList;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A simple timer based on {@link System#currentTimeMillis}.  To support better performance, this class 
 * is not thread-safe.
 */
public class Stopwatch {
  private final List<Long> _splits;
  private boolean _running;
  private long _start;
  
  /** Create a new stopwatch with no split times and in a stopped state. */
  public Stopwatch() {
    _running = false;
    _splits = new LinkedList<Long>();
  }
  
  /**
   * Create a new stopwatch with no split times; if {@code startImmediately} is {@code true},
   * start the timer before returning.
   */
  public Stopwatch(boolean startImmediately) {
    _running = false;
    _splits = new LinkedList<Long>();
    if (startImmediately) start();
  }
  
  /**
   * Start the timer.
   * @throws IllegalStateException  If the stopwatch is currently running.
    */
  public void start() {
    if (_running) { throw new IllegalStateException("Already running"); }
    _start = System.currentTimeMillis();
    _running = true;
  }
  
  /**
   * Record and return the number of milliseconds since {@code start()} was invoked.
   * @throws IllegalStateException  If the stopwatch is not currently running.
   */
  public long split() {
    if (!_running) { throw new IllegalStateException("Not running"); }
    long result = System.currentTimeMillis() - _start;
    _splits.add(result);
    return result;
  }
  
  /**
   * Stop the timer; record and return the number of milliseconds since {@code start()} was invoked.
   * @throws IllegalStateException  If the stopwatch is not currently running.
   */
  public long stop() {
    long result = split();
    _running = false;
    return result;
  }

  /**
   * Get a dynamically-updating view of all splits recorded by the stopwatch.
   * (Create a {@link IterUtil#snapshot} if the stopwatch is to be used while iterating over
   * this list.)
   */
  public Iterable<Long> splits() { return IterUtil.immutable(_splits); }
}
