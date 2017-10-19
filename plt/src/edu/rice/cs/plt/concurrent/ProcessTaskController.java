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

import static edu.rice.cs.plt.debug.DebugUtil.error;

import java.io.*;
import java.util.concurrent.Executor;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.WrappedException;

/**
 * A TaskController that executes a simple task in another Java process.  The task and its result must 
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
public class ProcessTaskController<R> extends TaskController<R> {
  // this could be implemented as a PollingController (preventing the need for another thread if
  // onExit is null), but that would be messy -- the update method would have to carefully buffer the contents
  // of the process's stdout stream without blocking

  // fields will be changed to null by discard(), but no need for volatile because it's only for garbage collection
  private JVMBuilder _jvmBuilder;
  private Executor _executor;
  private Thunk<? extends R> _task;
  // may be null, indicating nothing should be done on exit
  private Runnable1<? super Process> _onExit;
  // must be volatile because it starts uninitialized
  private volatile Thread _t;

  /**
   * Create, but do not start, a ProcessTaskController.
   * @param jvmBuilder  A JVMBuilder for the remote process; must have this class, {@code task}'s class,
   *                    and their dependencies on its class path.
   * @param executor  An executor for scheduling a local task that manages interaction with the remote
   *                  process.  The local task completes once a value has been returned.
   * @param task  A computation to perform; the task and its return value must be serializable.
   */
  public ProcessTaskController(JVMBuilder jvmBuilder, Executor executor, Thunk<? extends R> task) {
    _jvmBuilder = jvmBuilder;
    _executor = executor;
    _task = task;
    _onExit = null;
    _t = null;
  }
  
  /**
   * Create, but do not start, a ProcessTaskController.  This constructor allows code to be executed
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
   * @param onExit  An action to perform when the process has quit, or {@code null} for no action.
   */
  public ProcessTaskController(JVMBuilder jvmBuilder, Executor executor, Thunk<? extends R> task,
                               Runnable1<? super Process> onExit) {
    _jvmBuilder = jvmBuilder;
    _executor = executor;
    _task = task;
    _onExit = onExit;
    _t = null;
  }
  
  protected void doStart() {
    _executor.execute(new Runnable() {
      public void run() {
        _t = Thread.currentThread();
        started();
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
            ObjectOutputStream objOut = new ObjectOutputStream(p.getOutputStream());
            try { objOut.writeObject(_task); }
            finally { objOut.close(); }
            ObjectInputStream objIn = new ObjectInputStream(in);
            try {
              @SuppressWarnings("unchecked") R result = (R) objIn.readObject();
              Exception taskE = (Exception) objIn.readObject();
              RuntimeException implementationE = (RuntimeException) objIn.readObject();
              if (implementationE != null) { p.destroy(); finishedWithImplementationException(implementationE); }
              else if (taskE != null) { p.destroy(); finishedWithTaskException(taskE); }
              else {
                Runnable1<? super Process> onExit = _onExit; // keep local copy so it can be discarded
                finishedCleanly(result);
                if (onExit != null) {
                  p.waitFor();
                  onExit.run(p);
                }
              }
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
        catch (InterruptedException e) { stopped(); }
        catch (InterruptedIOException e) { stopped(); }
        catch (RuntimeException e) { finishedWithImplementationException(e); }
        catch (Throwable t) { finishedWithImplementationException(new WrappedException(t)); }
      }
    });
  }
  
  protected void doStop() { _t.interrupt(); }
  
  protected void discard() {
    _jvmBuilder = null;
    _executor = null;
    _task = null;
    _onExit = null;
    _t = null;
  }

  /**
   * Reads a serialized thunk from the input stream.  Writes to {@code System.out}: 1) The byte array
   * {@link #PREFIX}; 2) the result of running the task, or null if running failed; 3) any Exception thrown by the 
   * task (or null); 4) any RuntimeException due to serialization errors or other implementation-related problems
   * (or null).  Once running begins, no other output is written to {@code System.out} or {@code System.err}.
   */
  private static class Runner {
    /**
     * A byte sequence marking the beginning of the return data.  Allows java commands to output
     * text before {@code main()} is invoked without corrupting the data stream.  (This occurs, for example,
     * with flag "-Xrunjdwp".)  In order to avoid false positives, this prefix uses non-printing ASCII values.
     * To simplify the matching algorithm, each digit is guaranteed to be unique -- if a particular byte
     * fails to match, the DFA can only jump to either the initial state or the state after a single match. 
     */
    public static final byte[] PREFIX = { 0x00, 0x03, 0x7f, -0x80 };
    
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
          Object result = null;
          Exception taskException = null;
          RuntimeException internalException = null;
          try {
            ObjectInputStream objIn = new ObjectInputStream(System.in);
            try {
              Thunk<?> task = (Thunk<?>) objIn.readObject();
              try { result = task.value(); }
              catch (Exception e) { taskException = e; }
            }
            finally { objIn.close(); }
          }
          catch (RuntimeException e) { internalException = e; }
          catch (Throwable t) { internalException = new WrappedException(t); }
          
          objOut.writeObject(result);
          objOut.writeObject(taskException);
          objOut.writeObject(internalException);
        }
        finally { objOut.close(); }
      }
      catch (IOException e) { error.log("Error writing to System.out", e); }
    }
  }

}
