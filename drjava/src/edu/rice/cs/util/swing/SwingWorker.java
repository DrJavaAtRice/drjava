/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

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
  private Thread _thread;
  
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
  
  /** 
   * Get the value produced by the worker thread, or null if it 
   * hasn't been constructed yet.
   */
  protected synchronized Object getValue() { 
    return _value; 
  }
  
  /** 
   * Set the value produced by worker thread 
   */
  private synchronized void setValue(Object x) { 
    _value = x; 
  }
  
  /** 
   * Compute the value to be returned by the <code>get</code> method. 
   */
  public abstract Object construct();
  
  /**
   * Called on the event dispatching thread (not on the worker thread)
   * after the <code>construct</code> method has returned.
   */
  public void finished() {
  }
  
  /**
   * A new method that interrupts the worker thread.  Call this method
   * to force the worker to stop what it's doing.
   */
  public void interrupt() {
    Thread t = _threadVar.get();
    if (t != null) {
      t.interrupt();
    }
    _threadVar.clear();
  }
  
  /**
   * Return the value created by the <code>construct</code> method.  
   * Returns null if either the constructing thread or the current
   * thread was interrupted before a value was produced.
   * 
   * @return the value created by the <code>construct</code> method
   */
  public Object get() {
    while (true) {  
      Thread t = _threadVar.get();
      if (t == null) {
        return getValue();
      }
      try {
        t.join();
      }
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
        try {
          setValue(construct());
        }
        catch (final RuntimeException e) {
          // Throw the exception in the event dispatching thread.
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              throw e;
            }
          });
          throw e;
        }
        catch (final Error e) {
          // Throw the error in the event dispatching thread.
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              throw e;
            }
          });
          throw e;
        }
        finally {
          _threadVar.clear();
        }
        
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
