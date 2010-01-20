/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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
import edu.rice.cs.util.JoinInputStream;

/**
 * This class represents a piping chain of processes, in which the output of the first
 * process is piped into the input of the second process, and so on. The class allows
 * the entire chain to be treated as if it were just one process.
 * The constructor also sets up the input and output streams of the individual processes
 * so they can function as a chain.
 */

public class ProcessChain extends Process {
  /** Separator used between different process chains. */
  public static final char PROCESS_SEPARATOR_CHAR = '#';

  /** Separator used between different process chains. */
  public static final String PROCESS_SEPARATOR = String.valueOf(PROCESS_SEPARATOR_CHAR);

  /** Separator used between processes inside the same process chain. */
  public static final char PIPE_SEPARATOR_CHAR = '|';
  
  /** Separator used between processes inside the same process chain. */
  public static final String PIPE_SEPARATOR = String.valueOf(PIPE_SEPARATOR_CHAR);
  
  /** The process creators that create the processes in this process chain. */
  protected ProcessCreator[] _creators;

  /** The processes inside this piping chain. */
  protected Process[] _processes;
  
  /** True when execution of this chain has been aborted. */
  protected boolean _aborted = false;
  
  /** The redirector threads that move output (both stdout and stderr) from one process
    * to the input of the next process. */
  protected Set<StreamRedirectThread> _redirectors = new HashSet<StreamRedirectThread>();

  /** The combined input stream of all processes. */
  protected PipedInputStream _combinedInputStream;
  
  /** The stream into which all outputs to stdout are written. */
  protected PipedOutputStream _combinedStdOutStream;
  
  /** The combined input stream of all the processes, plus a debug stream. */
  protected JoinInputStream _combinedInputJoinedWithDebugStream;

  /** Debug output that gets joined with the streams from the processes. */
  protected PrintWriter _debugOutput;

  /** Debug input and output stream. */
  protected PipedInputStream _debugInputStream;
  protected PipedOutputStream _debugOutputStream;

  /** The combined error stream of all processes. */
  protected PipedInputStream _combinedErrorStream;
  
  /** The stream into which all outputs to stderr are written. */
  protected PipedOutputStream _combinedStdErrStream;

  /** Threads that wait for the subprocesses to terminate. */
  // protected Thread[] _deathThreads;
  
  /** Constructor for a process chain consisting of the individual processes provided.
    * @param pcs array of ProcessCreators */
  public ProcessChain(ProcessCreator[] pcs) {
    _creators = pcs;
    _processes = new Process[_creators.length];

    _combinedInputStream = new PipedInputStream();
    try {
      _combinedStdOutStream = new PipedOutputStream(_combinedInputStream);
      _combinedInputStream.connect(_combinedStdOutStream);
    }
    catch(IOException e) { /* ignore, no output if this goes wrong */ }
    
    _debugInputStream = new PipedInputStream();
    try {
      _debugOutputStream = new PipedOutputStream(_debugInputStream);
      _debugInputStream.connect(_debugOutputStream);
    }
    catch(IOException e) { /* ignore, no output if this goes wrong */ }
     _combinedInputJoinedWithDebugStream = new JoinInputStream(_combinedInputStream, _debugInputStream);
    _debugOutput = new PrintWriter(new OutputStreamWriter(_debugOutputStream));

    _combinedErrorStream = new PipedInputStream();
    try {
      _combinedStdErrStream = new PipedOutputStream(_combinedErrorStream);
      _combinedErrorStream.connect(_combinedStdErrStream);
    }
    catch(IOException e) { /* ignore, no output if this goes wrong */ }

    // _deathThreads = new Thread[_creators.length];
    for(int i = 0; i < _processes.length; ++i) {
//      final int index = i;
      try {
        _processes[i] = _creators[i].start();
//        _deathThreads[i] = new Thread(new Runnable() {
//          public void run() {
//            boolean interrupted = false;
//            do {
//              interrupted = false;
//              try {
//                _processes[index].waitFor();
//              }
//              catch(InterruptedException e) { interrupted = true; }
//            } while(interrupted);
//            GeneralProcessCreator.LOG.log("Process " + index + " has terminated");
//          }
//        });
//        _deathThreads[i].start();
      }
      catch(IOException e) {
        GeneralProcessCreator.LOG.log("\nIOException in external process: " + e.getMessage() + "\nCheck your command line.\n");
        // could not start the process, record error and abort
        _debugOutput.println("\nIOException in external process: " + e.getMessage() + "\nCheck your command line.\n");
        _debugOutput.flush();
        _aborted = true;
        destroy();
        return;
      }
    }
    
    for(int i = 0; i < _processes.length-1; ++i) {
      // _processes.length-1 because we're processing the gaps between the processes:
      // (P0 P1 P2) has two gaps: P0-P1 and P1-P2. There's always one less gap than processes.
      StreamRedirectThread r = new StreamRedirectThread("stdout Redirector " + i,
                                                        _processes[i].getInputStream(),
                                                        _processes[i+1].getOutputStream(),
                                                        new ProcessChainThreadGroup(this));
      _redirectors.add(r);
      r.start();
      r = new StreamRedirectThread("stderr Redirector " + i,
                                   _processes[i].getErrorStream(),
                                   _processes[i+1].getOutputStream(),
                                   new ProcessChainThreadGroup(this));
      _redirectors.add(r);
      r.start();
    }
    // now pipe output from the last process into our output streams
    StreamRedirectThread r = new StreamRedirectThread("stdout Redirector " + (_processes.length-1),
                                                      _processes[_processes.length-1].getInputStream(),
                                                      _combinedStdOutStream,
                                                      new ProcessChainThreadGroup(this));
    _redirectors.add(r);
    r.start();
    r = new StreamRedirectThread("stderr Redirector " + (_processes.length-1),
                                 _processes[_processes.length-1].getErrorStream(),
                                 _combinedStdErrStream,
                                 new ProcessChainThreadGroup(this));
    _redirectors.add(r);
    r.start();
//    _debugOutput.println("\n\nProcessChain started\n\n");
//    _debugOutput.flush();
  }
  
//  /**
//   * Gets the output stream of the process chain, i.e. the output
//   * stream of the first process in the chain.
//   *
//   * @return  the output stream of the process chain.
//   */
//  public OutputStream getOutputStream() {
//    if (_aborted) {
//      return new OutputStream() {
//        public void write(int b) throws IOException { }
//      };
//    }
//    return _processes[0].getOutputStream();
//  }
//  
//  /**
//   * Gets the error stream of the process chain, i.e. the error
//   * stream of the last process in the chain.
//   *
//   * @return  the error stream of the process chain.
//   */
//  public InputStream getErrorStream() {
//    if (_aborted) {
//      return new InputStream() {
//        public int read() throws IOException { return -1; }
//      };
//    }
//    return _processes[_processes.length-1].getErrorStream();
//  }
//  
//  /**
//   * Gets the input stream of the process chain, i.e. the input
//   * stream of the first process in the chain.
//   *
//   * @return  the input stream of the process chain
//   */
//  public InputStream getInputStream() {
//    if (_aborted) {
//      return new InputStream() {
//        public int read() throws IOException { return -1; }
//      };
//    }
//    return _processes[_processes.length-1].getInputStream();
//  }
  
  /**
   * Gets the output stream of the process sequence, i.e. the combined
   * output stream of all the processes in the sequence.
   *
   * @return  the output stream of the process sequence.
   */
  public OutputStream getOutputStream() {
    if (_aborted) {
      return new OutputStream() {
        public void write(int b) throws IOException { }
      };
    }
    else {
      return new BufferedOutputStream(_processes[0].getOutputStream());
    }
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
    return _combinedInputJoinedWithDebugStream;
  }
  
  /**
   * Causes the current thread to wait, if necessary, until the 
   * process chain has terminated, i.e. until all processes in the
   * chain have terminated. This method returns immediately if
   * all subprocesses have already terminated. If any of the
   * subprocess has not yet terminated, the calling thread will be
   * blocked until all subprocesses exit.
   *
   * @return     the exit value of the process chain, i.e. the
   *             exit code of the last process in the chain.
   * @exception  InterruptedException  if the current thread is 
   *             {@link Thread#interrupt() interrupted} by another thread 
   *             while it is waiting, then the wait is ended and an 
   *             {@link InterruptedException} is thrown.
   */
  public int waitFor() throws InterruptedException {
    if (_aborted) { return -1; }
    int exitCode = 0;
    for(int i = 0; i < _processes.length; ++i) {
      exitCode = _processes[i].waitFor();
//      if (i < _processes.length-1) {
//        _stdOutRedirectors.get(i).setStopFlag();
//        _stdErrRedirectors.get(i).setStopFlag();
//      }
//      try {
//        _processes[i].getInputStream().close();
//        _processes[i].getErrorStream().close();
//        if (i < _processes.length-1) {
//          _processes[i+1].getOutputStream().close();
//        }
//      }
//      catch(IOException e) { /* ignore, just don't close streams */ }
    }
//    stopAllRedirectors();
    return exitCode;
  }
  
  /**
   * Returns the exit value for the subprocess.
   *
   * @return  the exit value of the subprocess represented by this 
   *          <code>Process</code> object. by convention, the value 
   *          <code > 0</code> indicates normal termination.
   * @exception  IllegalThreadStateException  if the subprocess represented 
   *             by this <code>Process</code> object has not yet terminated.
   */
  public int exitValue() {
    if (_aborted) { return -1; }
    int exitCode = 0;
    // executing this loop guarantees that the IllegalThreadStateException
    // is thrown when at least one thread has not terminated yet.
    // just returning the exit value of the last process is not sufficient:
    // the last process could have terminated while other processes have not.
    for(int i = 0; i < _processes.length; ++i) {
      exitCode = _processes[i].exitValue();
    }
    return exitCode;
  }
  
  /**
   * Kills all subprocesses. The subprocesses represented by this 
   * <code>ProcessChain</code> object is forcibly terminated.
   */
  public void destroy() {
    _aborted = true;
    for(int i = 0; i < _processes.length; ++i) {
      _processes[i].destroy();
    }
    stopAllRedirectors();
  }
  
  /** Set the stop flags for all redirector threads. */
  protected void stopAllRedirectors() {
    for(StreamRedirectThread r: _redirectors) { r.setStopFlag(); }
    _redirectors.clear();
  }
  
  /** Thread group for all threads that deal with this process sequence. */
  protected static class ProcessChainThreadGroup extends ThreadGroup {
    private ProcessChain _chain;
    private PrintWriter _debugOut;
    public ProcessChainThreadGroup(ProcessChain chain) {
      super("Process Chain Thread Group");
      _chain = chain;
      _debugOut = _chain._debugOutput;
    }
    public void uncaughtException(Thread t, Throwable e) {
      destroy();
      if ((e instanceof StreamRedirectException) &&
          (e.getCause() instanceof java.io.IOException)) {
        _debugOut.println("\n\n\nAn exception occurred during the execution of the command line:\n" + 
                          e.toString() + "\n\n");
      }
      else {
        DrJavaErrorHandler.record(e);
      }
    }
  }
}
