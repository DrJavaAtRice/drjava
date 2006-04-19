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

import javax.swing.ProgressMonitor;

/**
 * This is the base class to the component that launches the AsyncTask. It
 * manages the multi-threading and ensures that the correct methods of the task
 * are performed on the correct thread.
 * 
 * @author jlugo
 */
public abstract class AsyncTaskLauncher {
  
  /**
   * Returns whether the launcher should call
   * <code>setParentContainerEnabled</code> both to dissable and to re-enable
   * the parent. This gives the concrete implementation of the launcher more
   * control in case dissabling and re-enabling the view could cause
   * inconsistent behavior.
   * <p>
   * In some cases this should always be true, for instance, when for each call
   * to lock the UI, you must call the unlock method an equal number of times to
   * actually unlock the frame. If this were dissabling a normal swing
   * component, where there is only on and off, you wouldn't want to re-enable
   * the component if it was dissabled to begin with.
   * 
   * @return whether the launcher should call
   *         <code>setParentContainerEnabled</code>
   */
  protected abstract boolean shouldSetEnabled();
  
  /**
   * Sets the enabled state of the parent component. If the parent component is
   * set to dissabled, this means that the user is unable to invoke any
   * operations via mouse clicks or key strokes. <i><b>Note:</b> this method
   * is called strictly on the event-handling thread.</i>
   * 
   * @param enabled
   *          Whether the parent container should be enabled
   */
  protected abstract void setParentContainerEnabled(boolean enabled);
  
  /**
   * Creates a progress monitor that can be used to provide feedback to the user
   * during the asynchronous task. This progress monitor will also be used to
   * allow the user to request the task to be canceled.
   * <p>
   * <i><b>Note:</b> this method is called strictly on the event-handling
   * thread.</i>
   * 
   * @return The progress monitor used to provide feedback.
   */
  protected abstract IAsyncProgress createProgressMonitor(String description, int min, int max);
  
  /**
   * Executes the AsyncTask in its own thread after performing any needed steps
   * to prepare the UI for its execution.
   * 
   * @param <R>
   *          The type of result to pass from <code>runAsync</code> to
   *          <code>complete</code>
   * @param task
   *          The task to execute on its own worker thread
   * @param showProgress
   *          Whether the progress monitor should be displayed to the user. If
   *          it's value is false, the user will not be able to make any
   *          cancelation requests to the task.
   * @param lockUI
   *          Whether the user should be able to interact with the rest of the
   *          UI while the task is in progress
   */
  public <P, R> void executeTask(final AsyncTask<P, R> task, final P param, final boolean showProgress,
                                 final boolean lockUI) {
    Runnable uiInit = new Runnable() {
      public void run() {
        final boolean shouldUnlockUI = shouldSetEnabled() && lockUI;
        final IAsyncProgress monitor = createProgressMonitor(task.getDiscriptionMessage(), 
                                                             task.getMinProgress(), 
                                                             task.getMaxProgress());
        if (shouldSetEnabled() && lockUI) {
          setParentContainerEnabled(false);
        }
        
        Thread taskThread = new Thread(new Runnable() {
          public void run() {
            R result = null;
            Exception caughtException = null;
            try {
              result = task.runAsync(param, monitor);
            } catch (Exception e) {
              caughtException = e;
            }
            
            final AsyncCompletionArgs<R> args = new AsyncCompletionArgs<R>(result, caughtException, monitor
                                                                             .isCanceled());
            
            Runnable cleanup = new Runnable() {
              public void run() {
                task.complete(args);
                
                if (shouldUnlockUI) {
                  setParentContainerEnabled(true);
                }
              }
            };
            
            Utilities.invokeLater(cleanup);
          }
        }, "Task Thread - " + task.getName());
        
        taskThread.start();
      }
    };
    
    Utilities.invokeLater(uiInit);
  }
}
