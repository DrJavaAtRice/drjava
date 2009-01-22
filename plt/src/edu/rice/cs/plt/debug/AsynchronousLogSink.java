package edu.rice.cs.plt.debug;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import edu.rice.cs.plt.concurrent.CompletionMonitor;
import edu.rice.cs.plt.lambda.LazyRunnable;
import edu.rice.cs.plt.lambda.WrappedException;

/**
 * <p>A LogSink that processes log messages in a separate thread.  This minimizes the impact of logging
 * bottlenecks on the performance of the program.  When the first log message is sent to this sink,
 * a daemon thread is started which records and waits for messages until the program terminates.</p>
 * 
 * <p>The impact of logging on performance cannot be entirely eliminated.  In addition to the added
 * pressure caused by an additional logging thread, the program thread still must generate a stack 
 * trace for each logging invocation, and enqueuing each logging message can take both time and space.</p>
 * 
 * <p>An advantage of synchronous logging is that users can infer from the lack of a message in a log that
 * a certain invocation never occurred.  In contrast, it is possible for asynchronous log messages to be
 * created but not recorded before program termination (or any other arbitrary deadline).  To avoid this
 * problem, users may invoke {@link #flush} at any time, which will block until all messages in a nonempty 
 * queue have been recorded; additionally, by default a shutdown hook is registered which attempts to flush
 * the queue before program shutdown completes.</p>
 */
public class AsynchronousLogSink implements LogSink {
  
  private final LogSink _delegate;
  private final Runnable _startThread; // lazily start thread on first invocation
  private final Queue<Message> _queue;
  
  private final CompletionMonitor _emptyNotifier; // signaled -> queue has become empty
  private final CompletionMonitor _nonemptyNotifier; // signaled -> queue has become nonempty
  
  /**
   * Create an asynchronous LogSink which passes messages to the given delegate sink.  Sets
   * {@code flushOnShutdown} to {@code true}.
   */
  public AsynchronousLogSink(LogSink delegate) { this(delegate, true); }
  
  /**
   * Create an asynchronous LogSink which passes messages to the given delegate sink.
   * @param flushOnShutdown  Whether a shutdown hook invoking {@link #flush} should be registered when
   *                         the logging thread is first started.
   */
  public AsynchronousLogSink(LogSink delegate, final boolean flushOnShutdown) {
    _delegate = delegate;
    _startThread = new LazyRunnable(new Runnable() {
      public void run() {
        if (flushOnShutdown) {
          Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
              try { flush(); }
              catch (InterruptedException e) { throw new WrappedException(e); }
            }
          });
        }
        new DequeueThread().start();
      }
    });
    _queue = new ConcurrentLinkedQueue<Message>();
    _emptyNotifier = new CompletionMonitor(true);
    _nonemptyNotifier = new CompletionMonitor(false);
  }
  
  public void close() throws IOException { _delegate.close(); }
  
  /** If any log messages have been enqueued but not yet recorded, block until they are recorded. */
  public void flush() throws InterruptedException {
    _emptyNotifier.ensureSignaled();
  }

  public void log(StandardMessage m) { handle(m); }
  public void logStart(StartMessage m) { handle(m); }
  public void logEnd(EndMessage m) { handle(m); }
  public void logError(ErrorMessage m) { handle(m); }
  public void logStack(StackMessage m) { handle(m); }
  
  private void handle(Message m) {
    boolean wasEmpty = _queue.isEmpty();
    _queue.offer(m);
    if (wasEmpty) {
      synchronized (this) {
        if (!_queue.isEmpty()) { // verify state after we have a lock
          _emptyNotifier.reset();
          _nonemptyNotifier.signal();
        }
      }
      _startThread.run();
    }
  }
  
  private class DequeueThread extends Thread {
    public DequeueThread() { super(AsynchronousLogSink.this.toString()); setDaemon(true); }
    
    public void run() {
      while (true) {
        _nonemptyNotifier.attemptEnsureSignaled();
        while (!_queue.isEmpty()) {
          _queue.remove().send(_delegate);
        }
        synchronized (AsynchronousLogSink.this) {
          if (_queue.isEmpty()) { // verify state after we have a lock
            _nonemptyNotifier.reset();
            _emptyNotifier.signal();
          }
        }
      }
    }
    
  }
  
}
