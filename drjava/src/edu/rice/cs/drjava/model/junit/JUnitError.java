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

package edu.rice.cs.drjava.model.junit;

import java.io.*;
import junit.framework.*;
import java.util.Enumeration;

/**
 * A class to represent JUnit errors.  Having this class allows DrJava
 * to make the errors as legible as possible.
 * @version $Id$
 */
public class JUnitError extends TestResult implements Comparable {
  private File _file;

  /** zero-based line number. */
  private int _lineNumber;

  /** zero-based column number. */
  private int _startColumn;
  private String _message;
  private String _test;
  private boolean _isError;
  private String _stackTrace;
  
  /**
   * Constructor.
   * @param     File file the file where the error occurred
   * @param     int lineNumber the line number of the error
   * @param     int startColumn the starting column of the error
   * @param     String message  the error message
   * @param     boolean isError true if the error is a warning
   */
  public JUnitError(File file, int lineNumber, int startColumn, String message,
      boolean isError, String test, String stackTrace) {
    _file = file;
    _lineNumber = lineNumber;
    _startColumn = startColumn;
    _message = message;
    _isError = isError;
    _test = test;
    _stackTrace = stackTrace;
  }

  /**
   * Gets a String representation of the error.
   * @return the error as a String
   */
  public String toString() {
    return "";
  }

  /**
   * Gets the file.
   * @return the file with errors.
   */
  public File file() {
    return _file;
  }

  /**
   * Gets the full name of the file.
   * @return the file name.
   */
  public String fileName() {
    if (_file == null) return "";
    else return _file.getAbsolutePath();
  }

  /**
   * Gets the line number of the error.
   * @return the line number
   */
  public int lineNumber() {
    return  _lineNumber;
  }

  /**
   * Gets the column where the error begins.
   * @return the starting column
   */
  public int startColumn() {
    return  _startColumn;
  }

  /**
   * Gets the error message.
   * @return the error message.
   */
  public String message() {
    return  _message;
  }

  /**
   * Gets the test name
   * @return the test name
   */
  public String testName() {
    return  _test;
  }

  public String stackTrace() {
    return _stackTrace;
  }

  /**
   * Determines if the error is a warning.
   * @return true if the error is a warning.
   */
  public boolean isWarning() {
    return  _isError;
  }

  /**
   * Compares by file, then by line, then by column.
   * Errors without files are considered equal, but less
   * than any errors with files.
   */
  public int compareTo(Object o) {
    JUnitError other = (JUnitError)o;

    // Determine if I have a file
    if (_file != null) {
      if (other.file() == null) {
        // Errors with files are bigger
        return 1;
      }
      else {
        // Compare by file
        int fileComp = _file.compareTo(other.file());
        if (fileComp != 0) {
          return fileComp;
        }
        else {
          // Compare by position
          return compareByPosition(other);
        }
      }
    }
    else {
      // My file is null
      if (other.file() == null) {
        return 0;
      }
      else {
        // Errors without files are smaller
        return -1;
      }
    }
  }

  /**
   * Compares this error with the given one, based first
   * on line number, and then by column.
   */
  private int compareByPosition(JUnitError other) {
    // Compare by line unless lines are equal
    if (_lineNumber == other._lineNumber) {
      return  _startColumn - other._startColumn;
    }
    else {
      return  _lineNumber - other._lineNumber;
    }
  }

}