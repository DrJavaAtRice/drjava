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
import java.util.ArrayList;

import edu.rice.cs.drjava.ui.DrJavaErrorHandler;

/**
 * This class represents a piping chain of processes, in which the output of the first
 * process is piped into the input of the second process, and so on. The class allows
 * the entire chain to be treated as if it were just one process.
 * The constructor also sets up the input and output streams of the individual processes
 * so they can function as a chain.
 */

public class ProcessChain extends Process {
  /** Separator used between different process chains. */
  public static final String PROCESS_SEPARATOR = (File.pathSeparatorChar==':')?";":":";

  /** Separator used between processes inside the same process chain. */
  public static final String PIPE_SEPARATOR = "|";
  
  /** The process creators that create the processes in this process chain. */
  protected ProcessCreator[] _creators;

  /** The processes inside this piping chain. */
  protected Process[] _processes;
  
  /** True when execution of this chain has been aborted. */
  protected boolean _aborted = false;
  
  /** The redirector threads that move stdout from one process
    * to the input of the next process. */
  protected ArrayList<StreamRedirectThread> _stdOutRedirectors = new ArrayList<StreamRedirectThread>();

  /** The redirector threads that move stderr from one process
    * to the input of the next process. */
  protected ArrayList<StreamRedirectThread> _stdErrRedirectors = new ArrayList<StreamRedirectThread>();
  
  /** Threads that wait for the subprocesses to terminate. */
  // protected Thread[] _deathThreads;
  
  /** Constructor for a process chain consisting of the individual processes provided.
    * @param pcs array of ProcessCreators */
  public ProcessChain(ProcessCreator[] pcs) {
    _creators = pcs;
    _processes = new Process[_creators.length];
    // _deathThreads = new Thread[_creators.length];
    for(int i=0; i<_processes.length; ++i) {
      final int index = i;
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
//            GeneralProcessCreator.LOG.log("Process "+index+" has terminated");
//          }
//        });
//        _deathThreads[i].start();
      }
      catch(IOException e) {
        _aborted = true;
        destroy();
        return;
      }
    }
    
    for(int i=0; i<_processes.length-1; ++i) {
      // _processes.length-1 because we're processing the gaps between the processes:
      // (P0 P1 P2) has two gaps: P0-P1 and P1-P2. There's always one less gap than processes.
      StreamRedirectThread r = new StreamRedirectThread("stdout Redirector "+i,
                                                        _processes[i].getInputStream(),
                                                        _processes[i+1].getOutputStream(),
                                                        PROCESS_CHAIN_THREAD_GROUP);
      _stdOutRedirectors.add(r);
      r.start();
      r = new StreamRedirectThread("stderr Redirector "+i,
                                   _processes[i].getErrorStream(),
                                   _processes[i+1].getOutputStream(),
                                   PROCESS_CHAIN_THREAD_GROUP);
      _stdErrRedirectors.add(r);
      r.start();
    }
  }
  
  /**
   * Gets the output stream of the process chain, i.e. the output
   * stream of the first process in the chain.
   *
   * @return  the output stream of the process chain.
   */
  public OutputStream getOutputStream() {
    if (_aborted) {
      return new OutputStream() {
        public void write(int b) throws IOException { }
      };
    }
    return _processes[0].getOutputStream();
  }
  
  /**
   * Gets the error stream of the process chain, i.e. the error
   * stream of the last process in the chain.
   *
   * @return  the error stream of the process chain.
   */
  public InputStream getErrorStream() {
    if (_aborted) {
      return new InputStream() {
        public int read() throws IOException { return -1; }
      };
    }
    return _processes[_processes.length-1].getErrorStream();
  }
  
  /**
   * Gets the input stream of the process chain, i.e. the input
   * stream of the first process in the chain.
   *
   * @return  the input stream of the process chain
   */
  public InputStream getInputStream() {
    if (_aborted) {
      return new InputStream() {
        public int read() throws IOException { return -1; }
      };
    }
    return _processes[_processes.length-1].getInputStream();
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
    for(int i=0; i<_processes.length; ++i) {
      exitCode = _processes[i].waitFor();
//      if (i<_processes.length-1) {
//        _stdOutRedirectors.get(i).setStopFlag();
//        _stdErrRedirectors.get(i).setStopFlag();
//      }
//      try {
//        _processes[i].getInputStream().close();
//        _processes[i].getErrorStream().close();
//        if (i<_processes.length-1) {
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
   *          <code>0</code> indicates normal termination.
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
    for(int i=0; i<_processes.length; ++i) {
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
    for(int i=0; i<_processes.length; ++i) {
      _processes[i].destroy();
    }
    stopAllRedirectors();
  }
  
  /** Set the stop flags for all redirector threads. */
  protected void stopAllRedirectors() {
    for(StreamRedirectThread r: _stdOutRedirectors) {
      // r.setStopFlag();
    }
    _stdOutRedirectors.clear();
    for(StreamRedirectThread r: _stdErrRedirectors) {
      // r.setStopFlag();
    }
    _stdErrRedirectors.clear();
  }
  
  /** Thread group for all threads that deal with this process chain. */
  protected static final ThreadGroup PROCESS_CHAIN_THREAD_GROUP = new ThreadGroup("Process Chain Thread Group") {
    public void uncaughtException(Thread t, Throwable e) {
      DrJavaErrorHandler.record(e);
    }
  };
}
