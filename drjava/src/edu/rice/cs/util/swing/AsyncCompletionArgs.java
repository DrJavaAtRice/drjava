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
 * @author jlugo
 * 
 */
public class AsyncCompletionArgs<R> {
  
  private R _result;
  
  private Exception _caughtException;
  
  private boolean _cancelRequested;
  
  public AsyncCompletionArgs(R result, boolean cancelRequested) {
    this(result, null, cancelRequested);
  }
  
  public AsyncCompletionArgs(R result, Exception caughtException, boolean wasCanceled) {
    _result = result;
    _caughtException = caughtException;
    _cancelRequested = wasCanceled;
  }
  
  /** Returns the result of the asynchronous computation performed by the
   * <code>AsyncTask</code>. If the task threw an exception, this value will
   * be null. The exception can be obtained by calling
   * <code>getCaughtException</code>
   * 
   * @return The resulting data produced by <code>AsyncTask.runAsync</code>
   */
  public R getResult() {
    return _result;
  }
  
  /** Returns the exception thrown from within the asynchronous task if an exception was thrown. If no exception was
    * thrown and the task completed successfully, this value will be null. 
    * @return The exception that was caught when running <code>AsyncTask.runAsync</code> or <code>null</code> if no
    *         exception was thrown.
    */
  public Exception getCaughtException() {
    return _caughtException;
  }
  
  /** If an exception was thrown during the execution of the AsyncTask, calling
    * this method will cause the exception to be thrown again in the thread that
    * calls this method. If no exception was thrown, this method does nothing.
    * @throws Exception if an exception was thrown from within the asynchronous task.
    */
  public void throwCaughtException() throws Exception {
    if (_caughtException != null) {
      throw _caughtException;
    }
  }
  
  /** Returns whether the user requested cancellation of the operation before completion. The task itself is responsible
    * for terminating its own execution and thus may have successfully completed.
    * @return Whether the user requested for the task to be canceled was 
    */
  public boolean cancelRequested() {
    return _cancelRequested;
  }
}
