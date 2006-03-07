/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.swing;

import javax.swing.SwingUtilities;

/**
 * SwingWorker, adapted from Sun's Java Tutorial.
 *
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on using this class, see:
 *
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 *
 * @version $Id$
 */
public abstract class SwingWorker {
  private Object _value;  // see getValue(), setValue()
//  private Thread _thread;

  /**
   * Class to maintain reference to current worker thread
   * under separate synchronization control.
   */
  private static class ThreadVar {
    private Thread _thread;
    ThreadVar(Thread t) { _thread = t; }
    synchronized Thread get() { return _thread; }
    synchronized void clear() { _thread = null; }
  }

  private ThreadVar _threadVar;

  /** Gets the value produced by the worker thread, or null if it
   *  hasn't been constructed yet.
   */
  protected synchronized Object getValue() { return _value; }

  /** Sets the value produced by worker thread. */
  private synchronized void setValue(Object x) { _value = x; }

  /** Compute the value to be returned by the <code>get</code> method. */
  public abstract Object construct();

  /** Called on the event dispatching thread (not on the worker thread)
   *  after the <code>construct</code> method has returned.
   */
  public void finished() { }

  /** A new method that interrupts the worker thread.  Call this method to force the worker to stop what it's doing. */
  public void interrupt() {
    Thread t = _threadVar.get();
    if (t != null) t.interrupt();
    _threadVar.clear();
  }

  /** Return the value created by the <code>construct</code> method.  Returns null if either the constructing thread 
   *  or the current thread was interrupted before a value was produced.
   *
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

  /**
   * Start a thread that will call the <code>construct</code> method
   * and then exit.
   */
  public SwingWorker() {
    final Runnable doFinished = new Runnable() {
      public void run() { finished(); }
    };

    Runnable doConstruct = new Runnable() {
      public void run() {
        try { setValue(construct()); }
        catch (final RuntimeException e) {
          // Throw the exception in the event dispatching thread.
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { throw e; }
          });
          throw e;
        }
        catch (final Error e) {
          // Throw the error in the event dispatching thread.
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { throw e; }
          });
          throw e;
        }
        finally { _threadVar.clear(); }

        SwingUtilities.invokeLater(doFinished);
      }
    };

    Thread t = new Thread(doConstruct);
    _threadVar = new ThreadVar(t);
  }

  /**
   * Start the worker thread.
   */
  public void start() {
    Thread t = _threadVar.get();
    if (t != null) {
      t.start();
    }
  }
}
