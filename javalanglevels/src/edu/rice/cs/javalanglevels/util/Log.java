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

package edu.rice.cs.javalanglevels.util;

import java.io.*;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/** Logging class to record errors or unexpected behavior to a file.  The file is created in the current directory,
  * and is only used if the log is enabled.  All logs can be enabled at once with the ENABLE_ALL field.
  * @version $Id$
  */
public class Log {
  public static final boolean ENABLE_ALL = false;
  
  /** Whether this particular log is enabled in development mode. */
  protected volatile boolean _isEnabled;
  
  /** The filename of this log. */
  protected volatile String _name;
  
  /** The file object for this log. */
  protected volatile File _file;
  
  /** PrintWriter to print messages to a file. */
  protected volatile PrintWriter _writer;
  
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM yyyy H:mm:ss z");
  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    DATE_FORMAT.setLenient(false);
  }
  
  /** Creates a new Log with the given name.  If enabled is true, a file is created in the current directory with the
    * given name.
    * @param name  File name for the log
    * @param isEnabled  Whether to actively use this log
    */
  public Log(String name, boolean isEnabled) { this(new File(name), isEnabled); }
  
  public Log(File f, boolean isEnabled) { _file = f;
    _name = f.getName();
    _isEnabled = isEnabled;
    _init();
  }
  
  /** Creates the log file, if enabled. */
  protected void _init() { if (_writer == null) {   if (_isEnabled || ENABLE_ALL) {     try {
          FileWriter w = new FileWriter(_file.getAbsolutePath(), true);
          _writer = new PrintWriter(w);
          log("Log '" + _name + "' opened: " + DATE_FORMAT.format(new Date()));
        }
        catch (IOException ioe) {
//          Utilities.show("Could not create log: " + ioe);
          throw new RuntimeException("Could not create log: " + ioe);
        }
      }
    }
  }
  
  /** Sets whether this log is enabled.  Only has an effect if the code is in development mode.
    * @param isEnabled  Whether to print messages to the log file
    */
  public void setEnabled(boolean isEnabled) { _isEnabled = isEnabled; }
  
  /** Returns whether this log is currently enabled. */
  public boolean isEnabled() { return (_isEnabled || ENABLE_ALL); }
  
  /** Prints a message to the log, if enabled.
    * @param message Message to print.
    */
  public synchronized void log(String message) { 
    if (isEnabled()) {   
      if (_writer == null) _init();
      _writer.println(DATE_FORMAT.format(new Date()) + ": " + message);
      _writer.flush();
    }
  }
  
  /** Converts a stack trace (StackTraceElement[]) to string form */
  public static String traceToString(StackTraceElement[] trace) { 
    final StringBuilder traceImage = new StringBuilder();
    for (StackTraceElement e: trace) traceImage.append("\n\tat " + e.toString());
    return traceImage.toString();
  }
  
  /** Prints a message and exception stack trace to the log, if enabled.
    * @param s  Message to print
    * @param trace  Stack track to log
    */
  public synchronized void log(String s, StackTraceElement[] trace) { 
    if (isEnabled()) log(s + traceToString(trace));
  }
  
  /** Prints a message and exception stack trace to the log, if enabled.
    * @param s Message to print
    * @param t Throwable to log
    */
  public synchronized void log(String s, Throwable t) { 
    if (isEnabled()) {   
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      log(s + "\n" + sw.toString());
    }
  }
  
  /** Closes a log file. */
  public void close() { 
    _writer.close();
    _writer = null;
  }
}