/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.util;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import edu.rice.cs.util.swing.Utilities;

/** An exception which DrJava throws on an unexpected error.
  * Many times, we have to catch BadLocationExceptions in
  * code that accesses DefinitionDocument, even if we know for a
  * fact that a BadLocationException cannot occur.  In that case,
  * and in other similar cases where we know that an exception should not
  * occur, we throw this on the off chance that something does go wrong.
  * This aids us in debugging the code.
  * @version $Id$
  */
public class UnexpectedException extends RuntimeException {
  
  public static void throwRuntimeException(Throwable t) {
    if (t instanceof RuntimeException) throw (RuntimeException) t;
    else throw new UnexpectedException(t);
  }

  private Throwable _value;

  /** Constructs an unexpected exception with <code>value.toString()</code> as it's message. 
   * @param value Throwable to be reported
   */
  public UnexpectedException(Throwable value) {
    super(value.toString());
    StringWriter sw = new StringWriter();
    new Throwable("").printStackTrace(new PrintWriter(sw));
//    Utilities.show("UnexpectedException(" + value.toString() + ") created.  Backtrace is:\n" + sw.toString());
    _value = value;
  }

  /** Constructs an unexpected exception for value with custom message 
   * string + <code>value.toString()</code>. 
   * @param value Throwable to be reported
   * @param msg additional message to prepend to throwable
   */
  public UnexpectedException(Throwable value, String msg) {
    super(msg + ": " + value.toString());
    _value = value;
  }
  
  /** Constructs a new RuntimeException to report that unreachable point in code has been reached */
  public UnexpectedException() {
    this(new RuntimeException("Unreachable point in code has been reached!"));
  }

  /** Constructs a new RuntimeException to report specified message 
   * @param msg message to be reported
   */
  public UnexpectedException(String msg) {
    this(new RuntimeException(msg == null ? "" : msg));
  }

  /** Returns the contained exception. */
  public Throwable getCause() { return _value; }
}
