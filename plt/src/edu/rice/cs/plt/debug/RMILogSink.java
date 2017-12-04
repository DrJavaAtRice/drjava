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

package edu.rice.cs.plt.debug;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.rice.cs.plt.concurrent.ConcurrentUtil;
import edu.rice.cs.plt.concurrent.JVMBuilder;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.WrappedException;

/**
 * A log sink that passes messages to a separate JVM for recording or display. Clients provide a serializable
 * LogSink factory; when the first message is logged, a separate process is started and the factory object is
 * passed to it, allowing for the creation of an RMI-based LogSink server on the remote JVM.  If any exception
 * occurs when the remote LogSink is started, it is thrown by the invoked {@code log} method.
 */
public class RMILogSink implements LogSink, Closeable {
  
  private final Thunk<RemoteLogSink> _delegate;
  private volatile boolean _active;
  
  /**
   * Create a LogSink server in a new process using {@link JVMBuilder#DEFAULT}.  {@code closeOnExit}
   * is {@code true}.
   * @param factory  A serializable LogSink factory, to be invoked in the new process
   */
  public RMILogSink(final Thunk<? extends LogSink> factory) { this(factory, JVMBuilder.DEFAULT, true); }

  /**
   * Create a LogSink server in a new process using {@link JVMBuilder#DEFAULT}.
   * @param factory  A serializable LogSink factory, to be invoked in the new process
   * @param closeOnExit  Whether this sink should be registered to be closed on system exit.
   */
  public RMILogSink(final Thunk<? extends LogSink> factory, boolean closeOnExit) {
    this(factory, JVMBuilder.DEFAULT, closeOnExit);
  }
    
  
  /**
   * Create a LogSink server in a new process using {@link JVMBuilder#DEFAULT}.  {@code closeOnExit} is
   * {@code true}.
   * @param factory  A serializable LogSink factory, to be invoked in the new process
   * @param jvm  A factory for producing the remote JVM.  The class path must include the factory's class,
   *             RMILogSink, ConcurrentUtil, and their dependencies.
   */
  public RMILogSink(final Thunk<? extends LogSink> factory, JVMBuilder jvm) {
    this(factory, jvm, true);
  }
  
  /**
   * Create a LogSink server in a new process using {@link JVMBuilder#DEFAULT}.
   * @param factory  A serializable LogSink factory, to be invoked in the new process
   * @param jvm  A factory for producing the remote JVM.  The class path must include the factory's class,
   *             RMILogSink, ConcurrentUtil, and their dependencies.
   * @param closeOnExit  Whether this sink should be registered to be closed on system exit.
   */
  public RMILogSink(final Thunk<? extends LogSink> factory, final JVMBuilder jvm, boolean closeOnExit) {
    _delegate = LazyThunk.make(new Thunk<RemoteLogSink>() {
      public RemoteLogSink value() {
        try { 
          RemoteLogSink result = (RemoteLogSink) ConcurrentUtil.exportInProcess(new ServerFactory(factory), jvm);
          _active = true;
          return result;
        }
        catch (Exception e) { throw new WrappedException(e); }
      }
    });
    _active = false;
    if (closeOnExit) { IOUtil.closeOnExit(this); }
  }

  /** Quit the remote process. */
  public void close() throws IOException {
    if (_active) { _delegate.value().close(); _active = false; }
  }
  
  public void log(StandardMessage m) {
    try { _delegate.value().log(m.serializable()); }
    catch (RemoteException e) { throw new WrappedException(e); }
  }
  
  public void logStart(StartMessage m) {
    try { _delegate.value().logStart(m.serializable()); }
    catch (RemoteException e) { throw new WrappedException(e); }
  }
  
  public void logEnd(EndMessage m) {
    try { _delegate.value().logEnd(m.serializable()); }
    catch (RemoteException e) { throw new WrappedException(e); }
  }
  
  public void logError(ErrorMessage m) {
    try { _delegate.value().logError(m.serializable()); }
    catch (RemoteException e) { throw new WrappedException(e); }
  }
  
  public void logStack(StackMessage m) {
    try { _delegate.value().logStack(m.serializable()); }
    catch (RemoteException e) { throw new WrappedException(e); }
  }
  
  private static interface RemoteLogSink extends Remote {
    public void log(StandardMessage m) throws RemoteException;
    public void logStart(StartMessage m) throws RemoteException;
    public void logEnd(EndMessage m) throws RemoteException;
    public void logError(ErrorMessage m) throws RemoteException;
    public void logStack(StackMessage m) throws RemoteException;
    public void close() throws IOException;
  }
  
  private static class ServerFactory implements Thunk<RemoteLogSink>, Serializable {
    private final Thunk<? extends LogSink> _sinkFactory;
    public ServerFactory(Thunk<? extends LogSink> sinkFactory) { _sinkFactory = sinkFactory; }
    public RemoteLogSink value() {
      // Code to execute in the remote JVM
      final LogSink sink = _sinkFactory.value();
      return new RemoteLogSink() {
        public void close() throws IOException { System.exit(0); }
        public void log(StandardMessage m) { sink.log(m); }
        public void logStart(StartMessage m) { sink.logStart(m); }
        public void logEnd(EndMessage m) { sink.logEnd(m); }
        public void logError(ErrorMessage m) { sink.logError(m); }
        public void logStack(StackMessage m) { sink.logStack(m); }
      };
    }
  }

}
