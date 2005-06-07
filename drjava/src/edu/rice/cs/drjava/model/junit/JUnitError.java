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

package edu.rice.cs.drjava.model.junit;

import edu.rice.cs.drjava.model.compiler.CompilerError;


import java.io.IOException;
import edu.rice.cs.util.FileOps;

import java.io.File;
import java.io.Serializable;

/**
 * A class to represent JUnit errors.  Having this class allows DrJava
 * to make the errors as legible as possible.
 * @version $Id$
 */
public class JUnitError extends CompilerError implements Comparable, Serializable {
  private String _test;
  private String _className;
  private String _stackTrace;
  
  /**
   * Constructor.
   * @param file the file where the error occurred
   * @param lineNumber the line number of the error
   * @param startColumn the starting column of the error
   * @param message  the error message
   * @param isWarning true if the error is a warning
   * @param test the name of the test that failed
   */
  public JUnitError(File file, int lineNumber, int startColumn, String message,
                    boolean isWarning, String test, String className, String stackTrace) {
    super(file, lineNumber, startColumn, message, isWarning);
    _test = test;
    _className = className;
    _stackTrace = stackTrace;
  }

  /**
   * Constructor for an error with no associated location.  This constructor also
   * provides a default stackTrace.
   * @param message  the error message
   * @param isWarning true if the error is a warning
   * @param test the name of the test that failed
   */
  public JUnitError(String message, boolean isWarning, String test) {
    this(null, -1, -1, message, isWarning, test, "", "No associated stack trace");
  }

  /**
   * Gets the test name
   * @return the test name
   */
  public String testName() {
    return _test;
  }

  /**
   * Gets the class name
   * @return the class name
   */
  public String className() {
    return _className;
  }

  /**
   * All JUnit errors are Throwables that have been thrown, so all have
   * a stack trace
   * @return the stack trace associated with the error
   */
  public String stackTrace() {
    return _stackTrace;
  }
  
}