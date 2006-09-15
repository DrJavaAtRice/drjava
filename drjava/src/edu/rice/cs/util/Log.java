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

package edu.rice.cs.util;

import java.io.*;

import java.util.Arrays;
import java.util.Date;

/** Logging class to record errors or unexpected behavior to a file.  The file is created in the current directory,
  * and is only used if the log is enabled.  All logs can be enabled at once with the ENABLE_ALL field.
  * @version $Id$
  */
public class Log {
  public static final boolean ENABLE_ALL = false;

  /** Whether this particular log is enabled in development mode. */
  protected boolean _isEnabled;

  /** The filename of this log. */
  protected String _name;

  /** PrintWriter to print messages to a file. */
  protected PrintWriter _writer;

  /** Creates a new Log with the given name.  If enabled is true, a file is created in the current directory with the
    * given name.
    * @param name File name for the log
    * @param enabled Whether to actively use this log
    */
  public Log(String name, boolean isEnabled) {
    _name = name;
    _isEnabled = isEnabled;
    _init();
  }

  /** Creates the log file, if enabled. */
  protected void _init() {
    if (_writer == null) {
      if (_isEnabled || ENABLE_ALL) {
        try {
          File f = new File(_name);
          FileWriter w = new FileWriter(f.getAbsolutePath(), true);
          _writer = new PrintWriter(w);

          log("Log '" + _name + "' opened: " + (new Date()));
        }
        catch (IOException ioe) {
          throw new RuntimeException("Could not create log: " + ioe);
        }
      }
    }
  }

  /** Sets whether this log is enabled.  Only has an effect if the code is in development mode.
   *  @param enabled Whether to print messages to the log file
   */
  public void setEnabled(boolean isEnabled) { _isEnabled = isEnabled; }

  /** Returns whether this log is currently enabled. */
  public boolean isEnabled() {
    return (_isEnabled || ENABLE_ALL);
  }

  /** Prints a message to the log, if enabled.
   *  @param message Message to print.
   */
  public synchronized void log(String message) {
    if (isEnabled()) {
      if (_writer == null) {
        _init();
      }
      _writer.println((new Date()) + ": " + message);
      _writer.flush();
    }
  }

  /** Converts a stack trace (StackTraceElement[]) to string form */
  public static String traceToString(StackTraceElement[] trace) {
    final StringBuilder traceImage = new StringBuilder();
    for (StackTraceElement e: trace) traceImage.append("\n" + e.toString());
    return traceImage.toString();
  }
    
  /** Prints a message and exception stack trace to the log, if enabled.
   *  @param s Message to print
   *  @param t Throwable to log
   */
  public synchronized void log(String s, StackTraceElement[] trace) {
    if (isEnabled()) log(s + traceToString(trace));
  }
  
  /** Prints a message and exception stack trace to the log, if enabled.
   *  @param s Message to print
   *  @param t Throwable to log
   */
  public synchronized void log(String s, Throwable t) {
    if (isEnabled()) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      log(s + "\n" + sw.toString());
    }
  }
}