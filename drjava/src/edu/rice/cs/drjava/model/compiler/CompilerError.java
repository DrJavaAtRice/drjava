/* $Id$ */

package edu.rice.cs.drjava;

public class CompilerError implements Comparable {
  private String _fileName;
  /** zero-based line number. */
  private int _lineNumber;
  /** zero-based column number. */
  private int _startColumn;
  private String _message;
  private boolean _isWarning;

  public CompilerError(String fileName,
                       int lineNumber,
                       int startColumn,
                       String message,
                       boolean isWarning) {
    _fileName = fileName;
    _lineNumber = lineNumber;
    _startColumn = startColumn;
    _message = message;
    _isWarning = isWarning;
  }

  public String toString() {
    return "CompilerError(file=" + _fileName + ", line=" + _lineNumber +
           ", col=" + _startColumn + ", msg=" + _message + ")";
  }

  public String fileName() {
    return _fileName;
  }

  public int lineNumber() {
    return _lineNumber;
  }

  public int startColumn() {
    return _startColumn;
  }

  public String message() {
    return _message;
  }

  public boolean isWarning() {
    return _isWarning;
  }

  /** Compare by line, then by column. */
  public int compareTo(Object o) {
    CompilerError other = (CompilerError) o;
    if (_lineNumber == other._lineNumber) {
      return _startColumn - other._startColumn;
    }
    else {
      return _lineNumber - other._lineNumber;
    }
  }
}
