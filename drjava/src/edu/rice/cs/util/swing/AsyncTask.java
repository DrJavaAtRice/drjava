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

/**
 * The AsyncTask base class is a framework that facilitates execution of
 * operations asynchronously in order to free up the event-handling thread. This
 * task is passed to an implementation of the AsyncTaskLauncher to be run.
 * <p>
 * Any code that should be performed in asynchronously with the UI should be put
 * in the <code>runAsync</code> method. Any code that is run in this method
 * should NOT modify any Swing components at all. All modifications to Swing
 * components must either be enqueued on the event-handling thread or performed
 * in the <code>complete</code> method. If there is any data that must be
 * passed from the asynchronous step to the completion step, it should be
 * returned by the <code>runAsync</code> method. The same data returned by the
 * <code>runAsync</code> method will be given to the <code>complete</code>
 * method. In short, implementations of an AsyncTask need not manage the
 * information passing between the task thread and the UI thread.
 * <p>
 * The <code>runAsync</code> method is given a progress monitor in order for
 * the task to provide feedback to the user as to the progress of the task. The
 * min and max values for the progress are specified by the
 * <code>getMinProgress</code> and <code>getMaxProgress</code> methods in
 * the task.
 * 
 * @author jlugo
 */
public abstract class AsyncTask<ParamType, ResType> {

 private String _name;

 /** Default Constructor */
 public AsyncTask() { this("Untitled"); }

 /** Creates a task that has the given name
   * @param name The name of the task.
   */
 public AsyncTask(String name) { _name = name; }

 /** This is the method of the task that is run on the separate thread. Any
   * implementation of this method should not make any changes to GUI components
   * unless those calls are made explicitly thread safe by the developer. Any
   * code that modifies swing GUI components in any way should be located in the
   * <code>complete</code> method.
   *
   * @param param  Any parameter that should be passed to the task when it is executed
   * @param monitor  An object that controls the flow of information about task progress both to and from the runAsync 
   *                 method. This also offers a means of passing a result from the async step to the completion step.
   * @throws  RuntimeException
   */
 public abstract ResType runAsync(ParamType param, IAsyncProgress monitor) throws Exception;

 /** Performs te completion step where modifications to swing components are made. This method runs in the event thread
   * so changes made to swing components are safe.
   */
 public abstract void complete(AsyncCompletionArgs<ResType> args);

 /** Sets the description of the task that should be displayed in the progress
   * monitor that the user sees. While the task is in progress, a separate note
   * can be set in order to display specific information about the progress of
   * the task. This can be set by calling <code>ProgressMonitor.setNote</code>
   * 
   * @return A brief description of the task being performed
   */
 public abstract String getDiscriptionMessage();

 /** Returns the name of this specific type of task. If this is not overridden
   * @return the name of the task
   */
 public String getName() { return _name; }

 /** Returns the minimum value of the progress monitor
   * @return The minimum value (0.0%) of the progress monitor
   */
 public int getMinProgress() { return 0; }

 /** Reutrns the minimum value of the progress monitor
   * @return The minimum value (100.0%) of the progress monitor
   */
 public int getMaxProgress() { return 100; }

 public String toString() {
  return getClass().getName() + ": " + getName() + " (@" + System.identityHashCode(this) + ")";
 }
}