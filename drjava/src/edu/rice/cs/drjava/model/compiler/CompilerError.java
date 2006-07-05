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
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.compiler;

import java.io.File;
import java.io.Serializable;

/** A class to represent compilerr errors and warnings.  Having this class allows DrJava
  * to make the errors as legible as possible.
  * @version $Id$
  */
public class CompilerError implements Comparable, Serializable {
  private File _file;
  
  /** zero-based line number. */
  private final int _lineNumber;
  
  /** zero-based column number. */
  private final int _startColumn;
  private final String _message;
  private final boolean _isWarning;
  
  /** This boolean is true when the CompilerError does not have a location (lineNumber is -1). */
  private boolean _noLocation;
  
  /** Constructor.
    * @param     file the file where the error occurred
    * @param     lineNumber the line number of the error
    * @param     startColumn the starting column of the error
    * @param     message  the error message
    */
  public CompilerError(File file, int lineNumber, int startColumn, String message, boolean isWarning) {
    _file = file;
    _lineNumber = lineNumber;
    _startColumn = startColumn;
    _message = message;
    _isWarning = isWarning;
    if (lineNumber < 0) _noLocation = true;
  }
    
  /** Constructor for an CompilerError with an associated file but no location in the source */
  public CompilerError(File file, String message, boolean isWarning) { this(file, -1, -1, message, isWarning); }
  
  /** Constructor for CompilerErrors without files.
   *  @param message the error message
   */
  public CompilerError(String message, boolean isWarning) { this(null, message, isWarning); }
  
  /** This function returns true if and only if the given error has no location */
  public boolean hasNoLocation() { return _noLocation; }
  
  /** Gets a String representation of the error. Abstract.
    * @return the error as a String
    */
  public String toString() {
    return this.getClass().toString() + "(file=" + fileName() + ", line=" + _lineNumber + ", col=" + _startColumn + 
      ", msg=" + _message + ")";
  }
  
  /** Gets the file.
    * @return the file with errors.
    */
  public File file() { return _file; }
  
  /** Gets the full name of the file.
    * @return the file name.
    */
  public String fileName() {
    if (_file == null) return "";
    return _file.getAbsolutePath();
  }
  
  /** Gets the zero-based line number of the error.  NOTE: javac/javadoc produces zero-based line numbers internally, 
    * but prints one-based line numbers to the command line.
    * @return the zero-based line number
    */
  public int lineNumber() { return  _lineNumber; }
  
  /** Gets the column where the error begins.
    * @return the starting column
    */
  public int startColumn() { return  _startColumn; }
  
  /** Gets the error message.
    * @return the error message.
    */
  public String message() { return  _message; }
  
  /** This function returns a message telling the file this error is from appropriate to display to a user, indicating
    * if there is no file associated with this error.
    */
  public String getFileMessage() {
    if (_file == null) return "(no associated file)";
    return fileName();
  }
  
  /** This function returns a message telling the line this error is from appropriate to display to a user, indicating
    * if there is no file associated with this error.  This is adjusted to show one-based numbers,
    * since internally we store a zero-based index.
    */
  public String getLineMessage() {
    if (_file == null || this._lineNumber < 0) return "(no source location)";
    return "" + (_lineNumber + 1);
  }
  
  /** Determines if the error is a warning.
    * @return true if the error is a warning.
    */
  public boolean isWarning() { return  _isWarning; }
  
  /** Compares by file, then by line, then by column.  Errors without files are considered equal, but less than any 
    * errors with files.  Warnings are considered greater than errors when they are otherwise equal.
    */
  public int compareTo(Object o) {
    CompilerError other = (CompilerError) o;
    
    // Determine if I have a file
    if (_file != null) {
      // "this" has a file
      if (other.file() != null) {
         // "this" and other have files; compare them
        int fileComp = _file.compareTo(other.file());
        if (fileComp != 0) return fileComp;
        // This and other have equal files; compare positions
        return compareByPosition(other);
      }
      else return 1; // Other has no file so "this" is bigger   
    }
    // "this" has no file
    if (other.file() != null) return -1; // Other has a file so "this" is smaller
    // Neither error has a file
    boolean otherWarning = other.isWarning();
    return compareErrorWarning(other);
  }
  
  /** Compares this error's postion with other error's, based first on line number, then by column. */
  private int compareByPosition(CompilerError other) {
    // Compare by line unless lines are equal
    int byLine = _lineNumber - other.lineNumber();
    if (byLine != 0) return byLine;
    
    int byCol = _startColumn - other.startColumn();
    if (byCol != 0) return byCol;
    return compareErrorWarning(other);
  }
  
  /** Compare otherwise equal errors. */
  private int compareErrorWarning(CompilerError other) {
    return (isWarning()? (other.isWarning()? 0 : 1) : (other.isWarning()? -1 : 0));
  }
}