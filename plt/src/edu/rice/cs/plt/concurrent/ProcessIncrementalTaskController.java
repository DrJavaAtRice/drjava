/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.concurrent;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.WrappedException;

import static edu.rice.cs.plt.debug.DebugUtil.error;

/**
 * A TaskController that executes an IncrementalTask in another Java process.  The task and its results must 
 * be serializable.  If the task completes successfully, the remote process is allowed to run indefinitely
 * (the process terminates automatically for simple tasks, but if additional non-daemon threads are spawned,
 * users are responsible for cleaning up the process).  If the task is unsuccessful, the remote process
 * is immediately destroyed.  A local task, scheduled by an Executor, manages the startup of and exchange
 * of information with the remote process.  This local task is submitted
 * (via {@link Executor#execute}) when {@code start()} is invoked (if the executor blocks, so will 
 * {@code start()}); its status is changed to "running" when it actually begins executing; if canceled
 * in the interim, the status will still be "paused" until the task begins its scheduled execution, but
 * no process will ever by spawned.
 */
public class ProcessIncrementalTaskController<I, R> extends IncrementalTaskController<I, R> {

  // fields will be changed to null by discard(), but no need for volatile because it's only for garbage collection
  private JVMBuilder _jvmBuilder;
  private Executor _executor;
  private IncrementalTask<? extends I, ? extends R> _task;
  // may be null, indicating nothing should be done on exit
  private Runnable1<? super Process> _onExit;
  // must be volatile because it starts uninitialized
  private volatile Thread _t;
  private volatile ObjectOutputStream _commandSink;

  /**
   * Create, but do not start, aProcessIncrementalTaskController.
   * @param jvmBuilder  A JVMBuilder for the remote process; must have this class, {@code task}'s class,
   *                    and their dependencies on its class path.
   * @param executor  An executor for scheduling a local task that manages interaction with the remote
   *                  process.  The local task completes once a value has been returned.
   * @param task  A computation to perform; the task and its return value must be serializable.
   * @param ignoreIntermediate  Whether intermediate values will be discarded (but counted) rather than enqueued.
   */
  public ProcessIncrementalTaskController(JVMBuilder jvmBuilder, Executor executor,
                                          IncrementalTask<? extends I, ? extends R> task,
                                          boolean ignoreIntermediate) {
    super(ignoreIntermediate);
    _jvmBuilder = jvmBuilder;
    _executor = executor;
    _task = task;
    _onExit = null;
    _t = null;
    _commandSink = null;
  }
  
  /**
   * Create, but do not start, a ProcessIncrementalTaskController.  This constructor allows code to be executed
   * when the remote process terminates: if the computation terminates successfully, {@code onExit} will
   * be run after the process finishes (which may occur in the indefinite future if the task spawns
   * additional non-daemon threads).
   * @param jvmBuilder  A JVMBuilder for the remote process; must have this class, {@code task}'s class,
   *                    and their dependencies on its class path.
   * @param executor  An executor for scheduling a local task that manages interaction with the remote
   *                  process.  If {@code onExit} is defined, the local task completes after the process
   *                  terminates and {@code onExit} has run; otherwise, the local task completes once a
   *                  value has been returned.
   * @param task  A computation to perform; the task and its return value must be serializable.
   * @param ignoreIntermediate  Whether intermediate values will be discarded (but counted) rather than enqueued.
   * @param onExit  An action to perform when the process has quit, or {@code null} for no action.
   */
  public ProcessIncrementalTaskController(JVMBuilder jvmBuilder,
                                          Executor executor,
                                          IncrementalTask<? extends I, ? extends R> task,
                                          boolean ignoreIntermediate,
                                          Runnable1<? super Process> onExit) {
    super(ignoreIntermediate);
    _jvmBuilder = jvmBuilder;
    _executor = executor;
    _task = task;
    _onExit = onExit;
    _t = null;
    _commandSink = null;
  }
  
  
  protected void doStart() {
    _executor.execute(new Runnable() {
      public void run() {
        _t = Thread.currentThread();
        try {
          // stop if the task was canceled before starting
          if (Thread.interrupted()) { throw new InterruptedException(); }
          Process p = _jvmBuilder.start(Runner.class.getName(), IterUtil.<String>empty());
          try {
            InputStream in = p.getInputStream();
            // skip prefix
            int matching = 0;
            while (matching < Runner.PREFIX.length) {
              int read = in.read();
              if (read == -1) { throw new EOFException("Data prefix not found"); }
              else if ((byte) read == Runner.PREFIX[matching]) { matching++; } // cast handles negatives
              else if ((byte) read == Runner.PREFIX[0]) { matching = 1; } // cast handles negatives
              else { matching = 0; }
            }
            // prefix has been matched
            ObjectInputStream objIn = new ObjectInputStream(in);
            try {
              ObjectOutputStream objOut = new ObjectOutputStream(p.getOutputStream());
              try {
                objOut.writeObject(_task);
                objOut.writeObject(Command.RUN);
                objOut.flush();
                _commandSink = objOut;
                
                Result r;
                do {
                  r = (Result) objIn.readObject();
                  r.handle(ProcessIncrementalTaskController.this);
                } while (!(r instanceof FinishResult));
                if (r instanceof CleanFinishResult) {
                  // let the process run if we finished cleanly
                  Runnable1<? super Process> onExit = _onExit; // keep local copy so it can be discarded
                  if (onExit != null) { p.waitFor(); onExit.run(p); }
                }
                else { p.destroy(); }
              }
              finally { objOut.close(); }
            }
            finally { objIn.close(); }
          }
          catch (EOFException e) {
            p.destroy();
            throw new IOException("Unable to run process; class path may need to be adjusted");
          }
          // destroy the process on an exception, but let it run if we completed cleanly
          catch (Throwable e) { p.destroy(); throw e; }
        }
        catch (InterruptedException e) { /* ignore -- indicates error occurred in another thread */ }
        catch (InterruptedIOException e) { /* ignore -- indicates error occurred in another thread */ }
        catch (RuntimeException e) { finishedWithImplementationException(e); }
        catch (Throwable t) { finishedWithImplementationException(new WrappedException(t)); }
      }
    });
  }
  
  protected void doStop() { writeCommand(Command.CANCEL); }
  protected void doPause() { writeCommand(Command.PAUSE); }
  protected void doResume() { writeCommand(Command.RUN); }
  
  private void writeCommand(Command c) {
    try { _commandSink.writeObject(c); _commandSink.flush(); }
    catch (IOException e) {
      finishedWithImplementationException(new WrappedException(e));
      _t.interrupt();
    }
  }
  
  protected void discard() {
    _jvmBuilder = null;
    _executor = null;
    _task = null;
    _onExit = null;
    _t = null;
    _commandSink = null;
  }
  
  /** A serializable command to be passed from the local to the remote process. */
  private static enum Command { RUN, PAUSE, CANCEL; }
  
  /** A serializable result of computation to be passed from the remote to the local process. */
  private static abstract class Result implements Serializable {
    public abstract <I, R> void handle(ProcessIncrementalTaskController<I, R> c);
  }
  
  private static class StartedResult extends Result {
    public <I, R> void handle(ProcessIncrementalTaskController<I, R> c) { c.started(); }
  }
  
  private static class PausedResult extends Result {
    public <I, R> void handle(ProcessIncrementalTaskController<I, R> c) { c.paused(); }
  }

  private static class StepResult extends Result {
    private final Object _value;
    public StepResult(Object value) { _value = value; }
    /** Clients are responsible for ensuring the value has the appropriate type. */
    @SuppressWarnings("unchecked") public <I, R> void handle(ProcessIncrementalTaskController<I, R> c) {
      c.stepped((I) _value);
    }
  }
  
  private static abstract class FinishResult extends Result {}
  
  private static class CleanFinishResult extends FinishResult {
    private final Object _value;
    public CleanFinishResult(Object value) { _value = value; }
    /** Clients are responsible for ensuring the value has the appropriate type. */
    @SuppressWarnings("unchecked") public <I, R> void handle(ProcessIncrementalTaskController<I, R> c) {
      c.finishedCleanly((R) _value);
    }
  }

  private static class TaskExceptionResult extends FinishResult {
    private final Exception _e;
    public TaskExceptionResult(Exception e) { _e = e; }
    public <I, R> void handle(ProcessIncrementalTaskController<I, R> c) { c.finishedWithTaskException(_e); }
  }

  private static class ImplementationExceptionResult extends FinishResult {
    private final RuntimeException _e;
    public ImplementationExceptionResult(Throwable t) {
      if (t instanceof RuntimeException) { _e = (RuntimeException) t; }
      else { _e = new WrappedException(t); }
    }
    public <I, R> void handle(ProcessIncrementalTaskController<I, R> c) { c.finishedWithImplementationException(_e); }
  }
  
  private static class CanceledResult extends FinishResult {
    public <I, R> void handle(ProcessIncrementalTaskController<I, R> c) { c.stopped(); }
  }

  /**
   * Reads a serialized thunk from the input stream, followed by a sequence of Commands.  Writes to
   * {@code System.out} the byte array {@link #PREFIX}, followed by a sequence of Results, terminated
   * with FinishResult.  Once running begins, no other output is written to {@code System.out} or
   * {@code System.err}.
   */
  private static class Runner {
    /**
     * A byte sequence marking the beginning of the result data.  Allows java commands to output
     * text before {@code main()} is invoked without corrupting the data stream.  (This occurs, for example,
     * with flag "-Xrunjdwp".)  In order to avoid false positives, this prefix uses non-printing ASCII values.
     * To simplify the matching algorithm, each digit is guaranteed to be unique -- if a particular byte
     * fails to match, the DFA can only jump to either the initial state or the state after a single match. 
     */
    public static final byte[] PREFIX = { 0x00, 0x7f, 0x03, -0x80 };
    
    private final IncrementalTask<?, ?> _task;
    private final ObjectOutputStream _objOut;
    private final ObjectInputStream _objIn;
    private final CompletionMonitor _continueMonitor;
    private final BlockingQueue<Result> _results; 
    private final Thread _taskThread; // task run in a different thread to allow interrupting without corrupting i/o
    private final Thread _objInReader; // reads run in a different thread to allow asynchronous pausing
    
    public Runner(IncrementalTask<?, ?> task, ObjectOutputStream objOut, ObjectInputStream objIn) {
      _task = task;
      _objOut = objOut;
      _objIn = objIn;
      _continueMonitor = new CompletionMonitor(false);
      _results = new ArrayBlockingQueue<Result>(256);
      
      _taskThread = new Thread("task runner") {
        public void run() {
          try {
            try {
              while (!_task.isResolved()) {
                authorizeContinue();
                _results.put(new StepResult(_task.step()));
              }
              authorizeContinue();
              _results.put(new CleanFinishResult(_task.value()));
            }
            catch (InterruptedException e) { _results.put(new CanceledResult()); }
            catch (WrappedException e) {
              if (e.getCause() instanceof InterruptedException) { _results.put(new CanceledResult()); }
              else { _results.put(new TaskExceptionResult(e)); }
            }
            catch (RuntimeException e) { _results.put(new TaskExceptionResult(e)); }
            catch (Throwable t) { _results.put(new ImplementationExceptionResult(t)); }
          }
          catch (InterruptedException e) { /* interrupted while trying to put a result -- must have quit already */ }
        }
      };
      
      _objInReader = new Thread("objIn reader") {
        public void run() {
          try {
            try {
              while (!Thread.interrupted()) {
                Command c = (Command) _objIn.readObject();
                switch (c) {
                  case RUN: _continueMonitor.signal(); break;
                  case PAUSE: _continueMonitor.reset(); break;
                  case CANCEL: _taskThread.interrupt(); break;
                }
              }
            }
            catch (InterruptedIOException e) { /* task has completed cleanly; stop processing */ }
            catch (Throwable t) { _results.put(new ImplementationExceptionResult(t)); }
          }
          catch (InterruptedException e) { /* interrupted while trying to put a result -- must have quit already */ }
        }
      };
    }
    
    public void run() throws IOException, InterruptedException {
      // assumes a StartedResult has been written to objOut already; but waits until a
      // "run" command is received before actually running the task
      _objInReader.start();
      _taskThread.start();
      try {
        Result r;
        do {
          r = _results.take();
          _objOut.writeObject(r);
          _objOut.flush();
        } while (!(r instanceof FinishResult));
      }
      finally {
        // interrupt threads in case they haven't already stopped
        _objInReader.interrupt();
        _taskThread.interrupt();
      }
    }
    
    private void authorizeContinue() throws InterruptedException {
      if (Thread.interrupted()) { throw new InterruptedException(); }
      if (!_continueMonitor.isSignaled()) {
        _results.put(new PausedResult());
        _continueMonitor.ensureSignaled();
        _results.put(new StartedResult());
      }
    }
    
    public static void main(String... args) {
      OutputStream out = System.out;
      IOUtil.attemptClose(System.err); // in case other objects already have a handle on it, try to close the stream
      IOUtil.ignoreSystemOut();
      IOUtil.ignoreSystemErr();
      try {
        out.write(PREFIX);
        out.flush();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        try {
          objOut.writeObject(new StartedResult());
          objOut.flush();
          ObjectInputStream objIn = new ObjectInputStream(System.in);
          try {
            IncrementalTask<?, ?> task = (IncrementalTask<?, ?>) objIn.readObject();
            Runner runner = new Runner(task, objOut, objIn);
            runner.run();
          }
          finally { objIn.close(); }
        }
        catch (RuntimeException e) { objOut.writeObject(new ImplementationExceptionResult(e)); }
        catch (Throwable t) { objOut.writeObject(new ImplementationExceptionResult(t)); }
        finally { objOut.close(); }
      }
      catch (IOException e) { error.log("Error writing to System.out", e); }
    }

  }

}
