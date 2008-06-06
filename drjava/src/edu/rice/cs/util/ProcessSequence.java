/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import java.io.*;
import java.util.Set;
import java.util.HashSet;

import edu.rice.cs.drjava.ui.DrJavaErrorHandler;

/**
 * This class represents a sequence of processes. The inputs and outputs of the processes
 * are not connected. One process wait until the preceding process has terminated.
 * The class allows the entire sequence to be treated as if it were just one process.
 * The constructor starts the first subprocess.
 */

public class ProcessSequence extends Process {  
  /** The process creators that create the processes in this process sequence. */
  protected ProcessCreator[] _creators;

  /** The processes inside this process sequence. */
  protected Process[] _processes;

  /** Index of the currently running process. */
  protected volatile int _index = 0;
  
  /** True if the execution was aborted. */
  protected volatile boolean _aborted = false;
  
  /** The redirector threads that move output (both stdout and stderr) from one process
    * to the input of the next process. */
  protected Set<StreamRedirectThread> _redirectors = new HashSet<StreamRedirectThread>();

  /** The combined input stream of all processes. */
  protected PipedInputStream _combinedInputStream;
  
  /** The stream into which all outputs to stdout are written. */
  protected PipedOutputStream _combinedStdOutStream;

  /** The combined error stream of all processes. */
  protected PipedInputStream _combinedErrorStream;
  
  /** The stream into which all outputs to stderr are written. */
  protected PipedOutputStream _combinedStdErrStream;
  
  /** The output stream of the currently executing process. */
  protected volatile OutputStream _combinedOutputStream;
  
  /** Thread that monitors the subprocesses and starts the next process when the previous
    * one has terminated. */
  protected Thread _deathThread;
  
  /** Constructor for a process sequence consisting of the individual processes provided.
    * @param pcs array of ProcessCreators */
  public ProcessSequence(ProcessCreator[] pcs) {
    _creators = pcs;
    _processes = new Process[_creators.length];
    for(int i=0; i<_processes.length; ++i) { _processes[i] = null; }
    _combinedInputStream = new PipedInputStream();
    try {
      _combinedStdOutStream = new PipedOutputStream(_combinedInputStream);
      _combinedInputStream.connect(_combinedStdOutStream);
    }
    catch(IOException e) { /* ignore, no output if this goes wrong */ }
    _combinedErrorStream = new PipedInputStream();
    try {
      _combinedStdErrStream = new PipedOutputStream(_combinedErrorStream);
      _combinedErrorStream.connect(_combinedStdErrStream);
    }
    catch(IOException e) { /* ignore, no output if this goes wrong */ }
    
    _deathThread = new Thread(new Runnable() {
      public void run() {
        GeneralProcessCreator.LOG.log("ProcessSequence._deathThread running");
        boolean interrupted = false;
        // wait for the completion of each of the subprocesses
        while(_index<_processes.length) {
          GeneralProcessCreator.LOG.log("Waiting for process "+_index);
          do {
            interrupted = false;
            try {
              _processes[_index].waitFor();
            }
            catch(InterruptedException e) { interrupted = true; }
          } while(interrupted);
          GeneralProcessCreator.LOG.log("Process "+_index+" terminated");
          // a process has just terminated
          if (_index<_processes.length-1) {
            // increase index;
            ++_index;
            try {
              _processes[_index] = _creators[_index].start();
              GeneralProcessCreator.LOG.log("Process "+_index+" started");
              connectProcess(_processes[_index]);
            }
            catch(IOException e) {
              // could not start the process, record error but continue
              DrJavaErrorHandler.record(e);
            }
          }
          else {
            ++_index;
            try {
              _combinedStdOutStream.flush();
              _combinedStdOutStream.close();
              _combinedStdErrStream.flush();
              _combinedStdErrStream.close();
            }
            catch(IOException e) { /* ignore, just don't close the streams */ }
          }
        }
      }
    },"Process Sequence Death Thread");
    _index = 0;
    try {
      _processes[_index] = _creators[_index].start();
    }
    catch(IOException e) {
      // could not start the process, abort
      _aborted = true;
      destroy();
      return;
    }
    connectProcess(_processes[_index]);
    _deathThread.start();
  }
  
  /**
   * Gets the output stream of the process sequence, i.e. the combined
   * output stream of all the processes in the sequence.
   *
   * @return  the output stream of the process sequence.
   */
  public OutputStream getOutputStream() {
    return new BufferedOutputStream(new OutputStream() {
      public void write(int b) throws IOException {
        _combinedOutputStream.write(b);
      }
      public void flush() throws IOException {
        _combinedOutputStream.flush();
      }
      public void close() throws IOException {
        _combinedOutputStream.close();
      }
    });
  }
  
  /**
   * Gets the error stream of the process sequence, i.e. the combined
   * error stream of all the processes in the sequence.
   *
   * @return  the error stream of the process sequence.
   */
  public InputStream getErrorStream() {
    return _combinedErrorStream;
  }
  
  /**
   * Gets the input stream of the process sequence,  i.e. the combined
   * input stream of all the processes in the sequence.
   *
   * @return  the input stream of the process chain
   */
  public InputStream getInputStream() {
    return _combinedInputStream;
  }
  
  /**
   * Causes the current thread to wait, if necessary, until the 
   * process sequence has terminated, i.e. until all processes in
   * the sequence have terminated. This method returns immediately if
   * all subprocesses have already terminated. If any of the
   * subprocess has not yet terminated, the calling thread will be
   * blocked until all subprocesses exit.
   *
   * @return     the exit value of the process chain, i.e. the
   *             exit code of the last process in the sequence.
   * @exception  InterruptedException  if the current thread is 
   *             {@link Thread#interrupt() interrupted} by another thread 
   *             while it is waiting, then the wait is ended and an 
   *             {@link InterruptedException} is thrown.
   */
  public int waitFor() throws InterruptedException {
    if (_aborted) { return -1; }
    int exitCode = 0;
    for(int i=0; i<_processes.length; ++i) {
      while((!_aborted) && (_processes[i]==null)) {
        try {
          // next process has not been assigned and started yet, sleep a bit
          Thread.sleep(100);
        }
        catch(InterruptedException e) { /* interruptions are ok, just ignore */ }
      }
      if (_aborted) { return -1; }
      exitCode = _processes[i].waitFor();
    }
    stopAllRedirectors();
    return exitCode;
  }
  
  /**
   * Returns the exit value for the subprocess.
   *
   * @return  the exit value of the subprocess represented by this 
   *          <code>Process</code> object. by convention, the value 
   *          <code>0</code> indicates normal termination.
   * @exception  IllegalThreadStateException  if the subprocess represented 
   *             by this <code>Process</code> object has not yet terminated.
   */
  public int exitValue() {
    if (_aborted) { return -1; }
    if ((_index<_processes.length-1) || (_processes[_processes.length-1]==null)) {
      throw new IllegalThreadStateException("Process sequence has not terminated yet, exit value not available.");
    }
    // just returning the exit value of the last process is sufficient:
    // the last process gets started when the previous one has already terminated
    return _processes[_processes.length-1].exitValue();
  }
  
  /**
   * Kills all subprocesses. The subprocesses represented by this 
   * <code>ProcessChain</code> object is forcibly terminated.
   */
  public void destroy() {
    _aborted = true;
    for(int i=0; i<_processes.length; ++i) {
      if (_processes[i]!=null) { _processes[i].destroy(); }
    }
    stopAllRedirectors();
  }
  
  /** Set the stop flags for all redirector threads. */
  protected void stopAllRedirectors() {
    while(!_redirectors.isEmpty()) {
      StreamRedirectThread r = _redirectors.iterator().next();
      r.setStopFlag();
      _redirectors.remove(r);
    }
  }
  
  /** Connect the streams of the specified process. */
  protected void connectProcess(Process p) {
    // redirect all stdout from all the processes into a combined output stream
    // that pipes all the data into a combined input stream that serves as this
    // process sequence's input stream
    StreamRedirectThread r = new StreamRedirectThread("stdout Redirector "+_index,
                                                      p.getInputStream(),
                                                      _combinedStdOutStream,
                                                      false,
                                                      PROCESS_SEQUENCE_THREAD_GROUP);
    _redirectors.add(r);
    r.start();
    r = new StreamRedirectThread("stderr Redirector "+_index,
                                 p.getErrorStream(),
                                 _combinedStdErrStream,
                                 false,
                                 PROCESS_SEQUENCE_THREAD_GROUP);
    _redirectors.add(r);
    r.start();
    _combinedOutputStream = p.getOutputStream();
  }

  /** Thread group for all threads that deal with this process sequence. */
  protected static final ThreadGroup PROCESS_SEQUENCE_THREAD_GROUP = new ThreadGroup("Process Sequence Thread Group") {
    public void uncaughtException(Thread t, Throwable e) {
      DrJavaErrorHandler.record(e);
    }
  };
}
