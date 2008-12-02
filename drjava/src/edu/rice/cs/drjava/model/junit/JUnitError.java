/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model.junit;

import edu.rice.cs.drjava.model.DJError;

import java.io.File;
import java.io.Serializable;

/** A class to represent JUnit errors.  This class enables DrJava to highlight the exact error text.
  * @version $Id$
  */
public class JUnitError extends DJError implements Serializable {
  private String _test;
  private String _className;
  private String _stackTrace;
  
  /** Constructor.
   * @param file the file where the error occurred
   * @param lineNumber the line number of the error
   * @param startColumn the starting column of the error
   * @param message  the error message
   * @param isWarning true if the error is a warning
   * @param test the name of the test that failed
   */
  public JUnitError(File file, int lineNumber, int startColumn, String message, boolean isWarning, String test, 
                    String className, String stackTrace) {
    super(file, lineNumber, startColumn, message, isWarning);
    _test = test;
    _className = className;
    _stackTrace = stackTrace;
  }

  /** Constructor for an error with no associated location.  This constructor also
    * provides a default stackTrace.
    * @param message  the error message
    * @param isWarning true if the error is a warning
    * @param test the name of the test that failed
    */
  public JUnitError(String message, boolean isWarning, String test) {
    this(null, -1, -1, message, isWarning, test, "", "No associated stack trace");
  }

  /** Gets the test name
    * @return the test name
    */
  public String testName() { return _test; }

  /** Gets the class name
    * @return the class name
    */
  public String className() { return _className; }

  /** All JUnit errors are Throwables that have been thrown, so all have
    * a stack trace
    * @return the stack trace associated with the error
    */
  public String stackTrace() { return _stackTrace; }
}