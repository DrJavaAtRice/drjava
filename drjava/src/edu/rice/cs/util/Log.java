/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

import java.io.*;

import edu.rice.cs.drjava.CodeStatus;
import java.util.Date;

/**
 * Logging class to record errors or unexpected behavior to a file.
 * The file is created in the current directory, and is only used if
 * the log is enabled and the code is in development mode.  All
 * logs can be enabled at once with the ENABLE_ALL field.
 * 
 * @version $Id$
 */
public class Log {
  public static final boolean ENABLE_ALL = false;
  
  /**
   * Whether this particular log is enabled in development mode.
   */
  protected boolean _enabled;
  
  /**
   * The filename of this log.
   */
  protected String _name;
  
  /**
   * PrintWriter to print messages to a file.
   */
  protected PrintWriter _writer;
  
  /**
   * Creates a new Log with the given name.  If enabled is true,
   * a file is created in the current directory with the given name.
   * @param name File name for the log
   * @param enabled Whether to actively use this log
   */
  public Log(String name, boolean enabled) {
    _name = name;
    _enabled = enabled;
    _init();
  }

  /**
   * Creates the log file, if enabled.
   */
  protected void _init() {
    if (_writer == null) {
      if (CodeStatus.DEVELOPMENT && (_enabled || ENABLE_ALL)) {
        try {
          File f = new File(_name);
          FileWriter w = new FileWriter(f.getAbsolutePath(), true);
          _writer = new PrintWriter(w);
          
          logTime("Log '" + _name + "' opened: " + (new Date()));
        }
        catch (IOException ioe) {
          throw new RuntimeException("Could not create log: " + ioe);
        }
      }
    }
  }
  
  /**
   * Sets whether this log is enabled.  Only has an effect if
   * the code is in development mode.
   * @param enabled Whether to print messages to the log file
   */
  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }
  
  /**
   * Returns whether this log is currently enabled.
   */
  public boolean isEnabled() {
    return CodeStatus.DEVELOPMENT && (_enabled || ENABLE_ALL);
  }
  
  /**
   * Prints a message to the log, if enabled.
   * @param s Message to print.
   */
  public synchronized void log(String message) {
    if (isEnabled()) {
      if (_writer == null) {
        _init();
      }
      _writer.println(message);
      _writer.flush();
    }
  }
  
  /**
   * Prints a time stamped message to the log, if enabled.
   * @param message Message to print
   */
  public synchronized void logTime(String message) {
    if (isEnabled()) {
      long t = System.currentTimeMillis();
      log(t + ": " + message);
    }
  }
  
  /**
   * Prints a time stamped message and exception stack trace
   * to the log, if enabled.
   * @param message Message to print
   * @param t Exception or Error to log
   */
  public synchronized void logTime(String s, Throwable t) {
    if (isEnabled()) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      logTime(s + "\n" + sw.toString());
    }
  }
}