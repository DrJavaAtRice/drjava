package  edu.rice.cs.drjava;


/**
 * @version $Id$
 */
public class CompilerError
    implements Comparable {
  private String _fileName;

  /** zero-based line number. */
  private int _lineNumber;

  /** zero-based column number. */
  private int _startColumn;
  private String _message;
  private boolean _isWarning;

  /**
   * put your documentation comment here
   * @param     String fileName
   * @param     int lineNumber
   * @param     int startColumn
   * @param     String message
   * @param     boolean isWarning
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
   * put your documentation comment here
   * @return 
   */
  public String toString() {
    return  "CompilerError(file=" + _fileName + ", line=" + _lineNumber + ", col="
        + _startColumn + ", msg=" + _message + ")";
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public String fileName() {
    return  _fileName;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public int lineNumber() {
    return  _lineNumber;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public int startColumn() {
    return  _startColumn;
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public String message() {
    return  _message;
  }

  /**
   * put your documentation comment here
   * @return 
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



