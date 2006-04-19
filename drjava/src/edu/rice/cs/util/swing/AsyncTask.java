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

 /**
  * Default Constructor
  */
 public AsyncTask() {
  this("Untitled");
 }

 /**
  * Creates a task that has the given name
  * 
  * @param name
  *          The name of the task.
  */
 public AsyncTask(String name) {
  _name = name;
 }

 /**
  * This is the method of the task that is run on the separate thread. Any
  * implementation of this method should not make any changes to GUI components
  * unless those calls are made explicitly thread safe by the developer. Any
  * code that modifies swing GUI components in any way should be located in the
  * <code>complete</code> method.
  * 
  * @param param
  *          Any parameter that should be passed to the task when it is
  *          executed
  * @param monitor
  *          An object that handles the flow of information about the progress
  *          of the task both to and from the runAsync method. This also offers
  *          a means of passing a result from the async step to the completion
  *          step.
  * @throws Exception
  */
 public abstract ResType runAsync(ParamType param, IAsyncProgress monitor) throws Exception;

 /**
  * This is the completion step where any modifications to swing components
  * should be made. This method is called on the AWT event thread and so any
  * changes made to swing components are safe.
  * 
  * @param result
  *          The result set from within the runAsync method that specifies any
  *          changes that need to be made on swing components in the UI thread
  * @param isCanceled
  *          Whether the user requested that the task be aborted. The task is
  *          not obligated to have actually aborted. This parameter merely
  *          states whether the cancel request was made.
  */
 public abstract void complete(AsyncCompletionArgs<ResType> args);

 /**
  * Sets the description of the task that should be displayed in the progress
  * monitor that the user sees. While the task is in progress, a separate note
  * can be set in order to display specific information about the progress of
  * the task. This can be set by calling <code>ProgressMonitor.setNote</code>
  * 
  * @return A brief description of the task being performed
  */
 public abstract String getDiscriptionMessage();

 /**
  * Returns the name of this specific type of task. If this is not overridden
  * 
  * @return the name of the task
  */
 public String getName() {
  return _name;
 }

 /**
  * Reutrns the minimum value of the progress monitor
  * 
  * @return The minimum value (0.0%) of the progress monitor
  */
 public int getMinProgress() {
  return 0;
 }

 /**
  * Reutrns the minimum value of the progress monitor
  * 
  * @return The minimum value (100.0%) of the progress monitor
  */
 public int getMaxProgress() {
  return 100;
 }

 public String toString() {
  return getClass().getName() + ": " + getName() + " (@" + System.identityHashCode(this) + ")";
 }
}