package edu.rice.cs.drjava.model.compiler;

import java.io.File;

/**
 * A class to represent compiler errors.  Having this class allows DrJava
 * to make the errors as legible as possible.
 * @version $Id$
 */
public class CompilerError implements Comparable {
  private File _file;

  /** zero-based line number. */
  private int _lineNumber;

  /** zero-based column number. */
  private int _startColumn;
  private String _message;
  private boolean _isWarning;

  /**
   * Constructor.
   * @param     File file the file where the error occurred
   * @param     int lineNumber the line number of the error
   * @param     int startColumn the starting column of the error
   * @param     String message  the error message
   * @param     boolean isWarning true if the error is a warning
   */
  public CompilerError(File file, int lineNumber, int startColumn, String message,
      boolean isWarning) {
    _file = file;
    _lineNumber = lineNumber;
    _startColumn = startColumn;
    _message = message;
    _isWarning = isWarning;
  }

  /**
   * Constructor for CompilerErrors without files.
   * @param     String message  the error message
   * @param     boolean isWarning true if the error is a warning
   */
  public CompilerError(String message, boolean isWarning) {
    _file = null;
    _lineNumber = -1;
    _startColumn = -1;
    _message = message;
    _isWarning = isWarning;
  }

  /**
   * Gets a String representation of the error.
   * @return the error as a String
   */
  public String toString() {
    return  "CompilerError(file=" + _file.getAbsolutePath() + ", line=" +
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