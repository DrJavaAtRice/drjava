package edu.rice.cs.drjava.model.compiler;

/**
 * A class to represent compiler errors.  Having this class allows DrJava
 * to make the errors as legible as possible.
 * @version $Id$
 */
public class CompilerError implements Comparable {
  private String _fileName;

  /** zero-based line number. */
  private int _lineNumber;

  /** zero-based column number. */
  private int _startColumn;
  private String _message;
  private boolean _isWarning;

  /**
   * Constructor.
   * @param     String fileName the name of the file where the error occurred
   * @param     int lineNumber the line number of the error
   * @param     int startColumn the starting column of the error
   * @param     String message  the error message
   * @param     boolean isWarning true if the error is a warning
   */
  public CompilerError(String fileName, int lineNumber, int startColumn, String message, 
      boolean isWarning) {
    _fileName = fileName;
    _lineNumber = lineNumber;
    _startColumn = startColumn;
    _message = message;
    _isWarning = isWarning;
  }

  /**
   * Gets a String representation of the error.
   * @return the error as a String
   */
  public String toString() {
    return  "CompilerError(file=" + _fileName + ", line=" + _lineNumber + ", col="
      + _startColumn + ", msg=" + _message + ")";
  }

  /**
   * Gets the name of the file.
   * @return the file name.
   */
  public String fileName() {
    return  _fileName;
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

  /** Compare by line, then by column. */
  public int compareTo(Object o) {
    CompilerError other = (CompilerError)o;
    if (_lineNumber == other._lineNumber) {
      return  _startColumn - other._startColumn;
    } 
    else {
      return  _lineNumber - other._lineNumber;
    }
  }
}