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

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Serializable;

/**
 * A class to represent compiler errors.  Having this class allows DrJava
 * to make the errors as legible as possible.
 * @version $Id$
 */
public class CompilerError implements Comparable, Serializable {
  private File _file;

  /** zero-based line number. */
  private int _lineNumber;

  /** zero-based column number. */
  private int _startColumn;
  private String _message;
  private boolean _isWarning;

  /**
   * This boolean is true when the CompilerError does not
   * have a location (lineNumber is -1)
   */
  private boolean _noLocation;

  /**
   * Constructor.
   * @param     file the file where the error occurred
   * @param     lineNumber the line number of the error
   * @param     startColumn the starting column of the error
   * @param     message  the error message
   * @param     isWarning true if the error is a warning
   */
  public CompilerError(File file, int lineNumber, int startColumn, String message,
      boolean isWarning) {
    _file = file;
    _lineNumber = lineNumber;
    _startColumn = startColumn;
    _message = message;
    _isWarning = isWarning;
    if (lineNumber < 0){
      _noLocation = true;
    }
  }

  /**
   * Constructor for a CompilerError with an associated file but no location in the source
   */
  public CompilerError(File file, String message, boolean isWarning){
    this(file, -1, -1, message, isWarning);
  }

  /**
   * Constructor for CompilerErrors without files.
   * @param     message  the error message
   * @param     isWarning true if the error is a warning
   */
  public CompilerError(String message, boolean isWarning) {
    this(null, message, isWarning);
  }

  /**
   * This function returns true if and only if the given error has no location
   */
  public boolean hasNoLocation(){
    return _noLocation;
  }

  /**
   * Gets a String representation of the error.
   * @return the error as a String
   */
  public String toString() {
    return  this.getClass().toString() + "(file=" + fileName() + ", line=" +
      _lineNumber + ", col=" + _startColumn + ", msg=" + _message + ")";
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
   * This function returns a message telling the file this error is from
   * appropriate to display to a user, indicating if there is no file
   * associated with this error
   */
  public String getFileMessage(){
    if (_file == null){
      return "(no associated file)";
    } else {
      return fileName();
    }
  }

  /**
   * This function returns a message telling the line this error is from
   * appropriate to display to a user, inidicating if there is no file
   * associated with this error
   */
  public String getLineMessage(){
    if (_file == null || this._lineNumber < 0){
      return "(no source location)";
    } else {
      return "" + (_lineNumber + 1);
    }
  }

  /**
   * Determines if the error is a warning.
   * @return true if the error is a warning.
   */
  public boolean isWarning() {
    return  _isWarning;
  }

  /**
   * Compares by file, then by line, then by column.
   * Errors without files are considered equal, but less
   * than any errors with files.
   */
  public int compareTo(Object o) {
    CompilerError other = (CompilerError)o;

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
  private int compareByPosition(CompilerError other) {
    // Compare by line unless lines are equal
    if (_lineNumber == other._lineNumber) {
      return  _startColumn - other._startColumn;
    }
    else {
      return  _lineNumber - other._lineNumber;
    }
  }

}