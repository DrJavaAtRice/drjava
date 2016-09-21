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
import edu.rice.cs.plt.concurrent.CompletionMonitor;

/** StreamRedirectThread is a thread which copies its input to its output and terminates when it completes. */
public class StreamRedirectThread extends Thread {
  /// Input reader
  private Reader in;
  
  /// Output writer
  private final Writer out;
  
  /// Data buffer size
  private static final int BUFFER_SIZE = 2048;
  
  /// When stop flag is set to true, this thread should stop copying.
  private volatile boolean stop = false;
  
  /// When close flag is set to true, the streams are closed when copying has stopped.
  private volatile boolean close = true;
  
  /** When keepRunning flag is set to true, the thread will not terminate when the
    * stream ends. Instead, it will wait until another input stream is provided using
    * setInputStream(), and begin reading from that stream. To terminate the thread,
    * use setStopFlag(). */
  private volatile boolean keepRunning = false;
  
  /** This completion monitor is used when a new input stream is set. The copying loop
    * waits for a signal, which is set in setInputStream(). */
  private volatile CompletionMonitor cm = new CompletionMonitor();
  
  /**
   * Constructor
   *
   * @param name thread name
   * @param in   stream to copy from
   * @param out  stream to copy to
   * @param close true if the streams should be closed after copying has ended
   */
  public StreamRedirectThread(String name, InputStream in, OutputStream out, boolean close) {
    this(name,in,out,close,false/*keepRunning*/);
  }
  
  /**
   * Constructor
   *
   * @param name  thread name
   * @param in    stream to copy from
   * @param out   stream to copy to
   * @param tg    thread group for this thread
   */
  public StreamRedirectThread(String name, InputStream in, OutputStream out, ThreadGroup tg) {
    this(name,in,out,true/*close*/,tg,false/*keepRunning*/);
  }
  /**
   * Constructor
   *
   * @param name  thread name
   * @param in    stream to copy from
   * @param out   stream to copy to
   * @param close true if the streams should be closed after copying has ended
   * @param tg    thread group for this thread
   */
  public StreamRedirectThread(String name, InputStream in, OutputStream out, boolean close, ThreadGroup tg) {
    this(name,in,out,close,tg,false/*keepRunning*/);
  }

  /**
   * Constructor
   *
   * @param name  thread name
   * @param in    stream to copy from
   * @param out   stream to copy to
   */
  public StreamRedirectThread(String name, InputStream in, OutputStream out) {
    this(name,in,out,true/*close*/,false/*keepRunning*/);
  }
  
  /**
   * Constructor
   *
   * @param name thread name
   * @param in   stream to copy from
   * @param out  stream to copy to
   * @param close true if the streams should be closed after copying has ended
   * @param keepRunning  true if the thread should keep running and not terminate when a stream ends
   */
  public StreamRedirectThread(String name, InputStream in, OutputStream out, boolean close, boolean keepRunning) {
    super(name);
    this.keepRunning = keepRunning;
    this.close = close;
    this.in = new BufferedReader(new InputStreamReader(in));
    this.out = new BufferedWriter(new OutputStreamWriter(out));
    setPriority(Thread.MAX_PRIORITY - 1);
  }
  
  /**
   * Constructor
   * @param name         thread name
   * @param in           stream to copy from
   * @param out          stream to copy to
   * @param tg           thread group for this thread
   * @param keepRunning  true if the thread should keep running and not terminate when a stream ends
   */
  public StreamRedirectThread(String name, InputStream in, OutputStream out, ThreadGroup tg, boolean keepRunning) {
    this(name,in,out,true,tg,keepRunning);
  }
  
  /**
   * Constructor
   *
   * @param name         thread name
   * @param in           stream to copy from
   * @param out          stream to copy to
   * @param close        true if the streams should be closed after copying has ended
   * @param tg           thread group for this thread
   * @param keepRunning  true if the thread should keep running and not terminate when a stream ends
   */
  public StreamRedirectThread(String name, InputStream in, OutputStream out, boolean close, ThreadGroup tg, boolean keepRunning) {
    super(tg,name);
    this.keepRunning = keepRunning;
    this.close = close;
    this.in = new BufferedReader(new InputStreamReader(in));
    this.out = new BufferedWriter(new OutputStreamWriter(out));
    setPriority(Thread.MAX_PRIORITY - 1);
  }
  
  /**
   * Set a new input stream.
   * @param in new input stream to read from
   */
  public void setInputStream(InputStream in) {
    this.in = new BufferedReader(new InputStreamReader(in));
    cm.signal();
  }
  
  /**
   * Copy.
   */
  public void run() {
    do {
      try {
        char[] cbuf = new char[BUFFER_SIZE];
        int count;
        while ((!stop) && ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0)) {
          try {
            out.write(cbuf, 0, count);
            out.flush();
          }
          catch (IOException exc) {
            GeneralProcessCreator.LOG.log("StreamRedirectThread " + getName() + " had IOException while writing: " + exc);
            throw new StreamRedirectException("An error occurred during stream redirection, while piping data into a process.",
                                              exc);
          }
        }
        GeneralProcessCreator.LOG.log("StreamRedirectThread " + getName() + " finished copying");
        out.flush();
        if (close) {
          in.close();
        }
      }
      catch (IOException exc) {
        GeneralProcessCreator.LOG.log("StreamRedirectThread " + getName() + " had IOException: " + exc);
        throw new StreamRedirectException("An error occurred during stream redirection, while piping data out of a process.",
                                          exc);
      }
      if (keepRunning) {
        // wait for a new input stream
        while(!cm.attemptEnsureSignaled());
        cm.reset();
      }
    } while(keepRunning && !close);
    if (close) {
      try {
        out.close();
      }
      catch (IOException exc) {
        GeneralProcessCreator.LOG.log("StreamRedirectThread " + getName() + " had IOException: " + exc);
        throw new StreamRedirectException("An error occurred during stream redirection, while piping data out of a process.",
                                          exc);
      }
    }
  }
  
  /**
   * Tell the thread to stop copying.
   */
  public void setStopFlag() {
    stop = true;
  }
}
