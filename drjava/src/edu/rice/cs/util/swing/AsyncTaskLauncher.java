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

package edu.rice.cs.util.swing;

/** The base class of the component that launches an AsyncTask. It manages the multi-threading and ensures that the
  * correct methods of the task are performed on the correct thread. 
  * @author jlugo
  */
public abstract class AsyncTaskLauncher {
  
  /** Returns whether the launcher should call <code>setParentContainerEnabled</code> both to disable and to re-enable
    * the parent. This facility gives launchers more control over the view in case dissabling or re-enabling the view 
    * produces inconsistent behavior. <p>
    * In some cases, this method should always return true, e.g., when for each call to lock the UI, you must call the 
    * unlock method an equal number of times to actually unlock the frame. If this were dissabling a normal swing
    * component, where can only be on/off, you wouldn't want to re-enable the component if it was already disabled. 
    * @return whether the launcher should call <code>setParentContainerEnabled</code>
    */
  protected abstract boolean shouldSetEnabled();
  
  /** Sets the enabled state of the parent component. When the parent component is dissabled, the user cannot invoke any
    * operations via mouse clicks or key strokes. <i><b>Note:</b> this method only runs in the event thread.</i>
    * @param enabled  Whether the parent container should be enabled
    */
  protected abstract void setParentContainerEnabled(boolean enabled);
  
  /** Creates a progress monitor that can be used to provide feedback to the user during the asynchronous task. This
    * progress monitor can also be used to allow the user to request the task to be canceled. <p>
    * <i>This method only executes in the event-handling thread.</i> 
    * @return The progress monitor used to provide feedback.
    */
  protected abstract IAsyncProgress createProgressMonitor(String description, int min, int max);
  
  /** Executes the AsyncTask in its own thread after performing any needed steps to prepare the UI for its execution.
   * 
   * @param <R>  The type of result to pass from <code>runAsync</code> to <code>complete</code>
   * @param task  The task to execute on its own worker thread
   * @param showProgress  Whether the progress monitor should be displayed to the user. If it is false, the user will 
   *                      not be able to make any cancelation requests to the task.
   * @param lockUI  Whether the user should be able to interact with the rest of the UI while the task is in progress.
   */
  public <P, R> void executeTask(final AsyncTask<P, R> task, final P param, final boolean showProgress,
                                 final boolean lockUI) {
    Runnable uiInit = new Runnable() {
      public void run() {
        final boolean shouldUnlockUI = shouldSetEnabled() && lockUI;
        final IAsyncProgress monitor = 
          createProgressMonitor(task.getDiscriptionMessage(), task.getMinProgress(), task.getMaxProgress());
        if (shouldSetEnabled() && lockUI) setParentContainerEnabled(false);
        
        Thread taskThread = new Thread(new Runnable() {
          public void run() {
            R result = null;
            Exception caughtException = null;
            try {
              result = task.runAsync(param, monitor);
            } catch (Exception e) {
              caughtException = e;
            }
            
            final AsyncCompletionArgs<R> args = 
              new AsyncCompletionArgs<R>(result, caughtException, monitor.isCanceled());
            
            Runnable cleanup = new Runnable() {
              public void run() {
                task.complete(args);
                
                if (shouldUnlockUI) setParentContainerEnabled(true);
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
