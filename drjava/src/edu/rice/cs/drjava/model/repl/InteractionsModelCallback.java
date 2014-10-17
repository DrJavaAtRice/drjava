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

package edu.rice.cs.drjava.model.repl;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Callback interface which allows an InteractionsModel to respond to events in a remote Java interpreter.  These
  * methods may run outside the event thread!
  * @version $Id: InteractionsModelCallback.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public interface InteractionsModelCallback {
  
  /** Returns an available port number to use for debugging a remote interpreter.
    * @throws IOException if unable to get a valid port number.
    */
  public int getDebugPort() throws IOException;
  
  /** Called when the repl prints to System.out.
    * @param s String to print
    */
  public void replSystemOutPrint(String s);
  
    
  /** Called when the repl prints to System.err.
    * @param s String to print
    */
  public void replSystemErrPrint(String s);
  
  /** Called when input is request from System.in.
    * @return the input given to System.in
    */
  public String getConsoleInput();
  
  /** Sets the listener for any type of single-source input event. The listener can only be changed with 
    * the changeInputListener method.
    * @param listener a listener that reacts to input requests
    * @throws IllegalStateException if the input listener is locked
    */
  public void setInputListener(InputListener listener);
  
  /** Changes the input listener. Takes in the old listener to ensure that the owner of the original 
    * listener is aware that it is being changed.
    * @param oldListener the previous listener
    * @param newListener the listener to install
    * @throws IllegalArgumentException if oldListener is not the currently installed listener
    */
  public void changeInputListener(InputListener oldListener, InputListener newListener);
  
  /** Signifies that the most recent interpretation completed successfully,
   * returning no value.
   */
  public void replReturnedVoid();
  
  /** Signifies that the most recent interpretation completed successfully, returning a value.
    *
    * @param result The .toString-ed version of the value that was returned
    *               by the interpretation. We must return the String form
    *               because returning the Object directly would require the
    *               data type to be serializable.
    */
  public void replReturnedResult(String result, String style);
  
  /** Signifies that the most recent interpretation was ended due to an exception being thrown.
    * @param message The exception's message
    */
  public void replThrewException(String message);
  
  /** Signifies that the most recent interpretation was ended due to an exception being thrown.
    * @param message The exception's message
    */
  public void replThrewException(String message, StackTraceElement[] stackTrace);
  
  /** Signifies that the most recent interpretation was preempted by a syntax error.
    * @param errorMessage The syntax error message
    * @param startRow The starting row of the error
    * @param startCol The starting column of the error
    * @param endRow The end row of the error
    * @param endCol The end column of the error
    */
  public void replReturnedSyntaxError(String errorMessage, String interaction, int startRow, int startCol,
                                      int endRow, int endCol);
  
  /** Signifies that the most recent interpretation contained a call to System.exit.
    * @param status The exit status that will be returned.
    */
  public void replCalledSystemExit(int status);
  
  /** This method is called by the Main JVM if the Interpreter JVM cannot be exited (likely because of its 
    * having a security manager)
    * @param th The Throwable thrown by System.exit
    */
  public void interpreterResetFailed(Throwable th);
  
  /** Called when the slave JVM fails to startup */
  public void interpreterWontStart(Exception e);

  /** Called when the interpreter starts to reset. */
  public void interpreterResetting();
  
  /**
   * Called to assert that a fresh Java interpreter is ready for use either after a start or a restart.
   * Is sometimes preceded by a call to {@code interpreterResetting()}, but not when the interpreter is
   * first starting or is already fresh.
   */
  public void interpreterReady(File wd);
  
  /** A compiler can instruct DrJava to include additional elements for the boot
    * class path of the Interactions JVM. */
  public List<File> getCompilerBootClassPath();
  
  /** Transform the command line to be interpreted into something the Interactions JVM can use.
    * This replaces "java MyClass a b c" with Java code to call MyClass.main(new String[]{"a","b","c"}).
    * "import MyClass" is not handled here.
    * @param interactionsString unprocessed command line
    * @return command line with commands transformed */
  public String transformCommands(String interactionsString);
}