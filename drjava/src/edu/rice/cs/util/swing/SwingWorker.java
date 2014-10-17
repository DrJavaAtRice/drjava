/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import java.awt.EventQueue;

/** SwingWorker, adapted from Sun's Java Tutorial.  This is the 3rd version of SwingWorker (also known as 
  * SwingWorker 3), an abstract class that you subclass to perform GUI-related work in a dedicated thread.  For
  * instructions on using this class, see: http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
  *
  * Note that the API changed slightly in the 3rd version: you must now invoke start() on the SwingWorker after
  * creating it.
  * @version $Id: SwingWorker.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public abstract class SwingWorker {
  private volatile Object _value;  // see getValue(), setValue()
//  private Thread _thread;

  /** Class to maintain reference to current worker thread under separate synchronization control. */
  private static class ThreadVar {
    private volatile Thread _thread;
    ThreadVar(Thread t) { _thread = t; }
    Thread get() { return _thread; }
    void clear() { _thread = null; }
  }

  private volatile ThreadVar _threadVar;

  /** Gets the value produced by the worker thread, or null if it hasn't been constructed yet. */
  protected Object getValue() { return _value; }

  /** Sets the value produced by worker thread. */
  private void setValue(Object x) { _value = x; }

  /** Compute the value to be returned by the <code>get</code> method. */
  public abstract Object construct();

  /** Called from the event dispatching thread (not on the worker thread) after the <code>construct</code> method
    * has returned.
    */
  public void finished() { }

  /** A new method that interrupts the worker thread.  Call this method to force the worker to stop what it's doing. */
  public void interrupt() {
    Thread t = _threadVar.get();
    if (t != null) t.interrupt();
    _threadVar.clear();
  }

  /** Return the value created by the <code>construct</code> method.  Returns null if either the constructing thread 
    * or the current thread was interrupted before a value was produced.
    * @return the value created by the <code>construct</code> method
    */
  public Object get() {
    while (true) {
      Thread t = _threadVar.get();
      if (t == null) return getValue();
      try { t.join(); }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // propagate
        return null;
      }
    }
  }

  /** Start a thread that will call the <code>construct</code> method and then exit. */
  public SwingWorker() {
    final Runnable doFinished = new Runnable() {
      public void run() { finished(); }
    };

    Runnable doConstruct = new Runnable() {
      public void run() {
        try { setValue(construct()); }
        catch (final RuntimeException e) {
          // Throw the exception in the event dispatching thread.
          EventQueue.invokeLater(new Runnable() { public void run() { throw e; } });
          throw e;
        }
        catch (final Error e) {
          // Throw the error in the event dispatching thread.
          EventQueue.invokeLater(new Runnable() { public void run() { throw e; } });
          throw e;
        }
        finally { _threadVar.clear(); }

        EventQueue.invokeLater(doFinished);
      }
    };

    Thread t = new Thread(doConstruct);
    _threadVar = new ThreadVar(t);
  }

  /** Start the worker thread. */
  public void start() {
    Thread t = _threadVar.get();
    if (t != null) t.start();
  }
}
