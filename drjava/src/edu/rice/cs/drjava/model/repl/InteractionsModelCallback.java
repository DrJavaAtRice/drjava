/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.repl;

import java.io.IOException;

/**
 * Callback interface which allows an InteractionsModel to respond to
 * events in a remote Java interpreter.
 * @version $Id$
 */
public interface InteractionsModelCallback {

  /**
   * Returns an available port number to use for debugging a remote interpreter.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException;

  /**
   * Called when the repl prints to System.out.
   * @param s String to print
   */
  public void replSystemOutPrint(String s);

  /**
   * Called when input is request from System.in.
   * @return the input given to System.in
   */
  public String getConsoleInput();

  /**
   * Sets the listener for any type of single-source input event.
   * The listener can only be changed with the changeInputListener method.
   * @param listener a listener that reacts to input requests
   * @throws IllegalStateException if the input listener is locked
   */
  public void setInputListener(InputListener listener);

  /**
   * Changes the input listener. Takes in the old listener to ensure that
   * the owner of the original listener is aware that it is being changed.
   * @param oldListener the previous listener
   * @param newListener the listener to install
   * @throws IllegalArgumentException if oldListener is not the currently installed listener
   */
  public void changeInputListener(InputListener oldListener, InputListener newListener);

  /** 
   * Called when the repl prints to System.err.
   * @param s String to print
   */
  public void replSystemErrPrint(String s);

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning no value.
   */
  public void replReturnedVoid();

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning a value.
   *
   * @param result The .toString-ed version of the value that was returned
   *               by the interpretation. We must return the String form
   *               because returning the Object directly would require the
   *               data type to be serializable.
   */
  public void replReturnedResult(String result);

  /**
   * Signifies that the most recent interpretation was ended
   * due to an exception being thrown.
   *
   * @param exceptionClass The name of the class of the thrown exception
   * @param message The exception's message
   * @param stackTrace The stack trace of the exception
   */
  public void replThrewException(String exceptionClass,
                                 String message,
                                 String stackTrace,
                                 String specialMessage);

  /**
   * Signifies that the most recent interpretation was preempted
   * by a syntax error.
   *
   * @param errorMessage The syntax error message
   * @param startRow The starting row of the error
   * @param startCol The starting column of the error
   * @param startRow The end row of the error
   * @param startCol The end column of the error
   */
  public void replReturnedSyntaxError(String errorMessage,
                                      String interaction,
                                      int startRow,
                                      int startCol,
                                      int endRow,
                                      int endCol );

  /**
   * Signifies that the most recent interpretation contained a call to
   * System.exit.
   *
   * @param status The exit status that will be returned.
   */
  public void replCalledSystemExit(int status);

  /**
   * This method is called by the Main JVM if the Interpreter JVM cannot
   * be exited (likely because of its having a security manager)
   * @param th The Throwable thrown by System.exit
   */
  public void interpreterResetFailed(Throwable th);

  /**
   * Called when the interpreter starts to reset.
   */
  public void interpreterResetting();

  /**
   * Called when a new Java interpreter has registered and is ready for use.
   */
  public void interpreterReady();

}