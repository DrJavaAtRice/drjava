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

	/**
	 * Returns the result of the asynchronous computation performed by the
	 * <code>AsyncTask</code>. If the task threw an exception, this value will
	 * be null. The exception can be obtained by calling
	 * <code>getCaughtException</code>
	 * 
	 * @return The resulting data produced by <code>AsyncTask.runAsync</code>
	 */
	public R getResult() {
		return _result;
	}

	/**
	 * Returns the exception thrown from within the asynchronous task if an
	 * exception was thrown. If no exception was thrown and the task completed
	 * successfully, this value will be null.
	 * 
	 * @return The exception that was caught when running
	 *         <code>AsyncTask.runAsync</code> or <code>null</code> if no
	 *         exception was thrown.
	 */
	public Exception getCaughtException() {
		return _caughtException;
	}

	/**
	 * If an exception was thrown during the execution of the AsyncTask, calling
	 * this method will cause the exception to be thrown again in the thread that
	 * calls this method. If no exception was thrown, this method does nothing.
	 * 
	 * @throws Exception
	 *           If an exception was thrown from within the asynchronous task.
	 */
	public void throwCaughtException() throws Exception {
		if (_caughtException != null) {
			throw _caughtException;
		}
	}

	/**
	 * Returns whether the user requested that the operation be canceled before
	 * completing. The task itself is responsible for terminating its own
	 * execution and thus may have successfully completed.
	 * 
	 * @return Whether the user requested for the task to be canceled was 
	 */
	public boolean cancelRequested() {
		return _cancelRequested;
	}
}
