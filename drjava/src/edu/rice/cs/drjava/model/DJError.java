/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.Serializable;

import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;


/** A class to represent source errors and warnings generated by the compiler, JUnit, etc.  This class enables DrJava
  * to highlight the error text.
  * @version $Id$
  */
public class DJError implements Comparable<DJError>, Serializable {
	
  /** Debugging log. */
  public static Log _log = new Log("DJError.txt", false);

  private volatile File _file;
  
  /** zero-based line number. */
  private volatile int _lineNumber;
  
  /** zero-based column number. */
  private final int _startColumn;
  private final String _message;
  private final boolean _isWarning;
  
  /** This boolean is true when the DJError does not have a location (lineNumber is -1). */
  private volatile boolean _noLocation;
  
  /** Constructor.
   * @param     file the file where the error occurred
   * @param     lineNumber the line number of the error
   * @param     startColumn the starting column of the error
   * @param     message  the error message
   * @param     isWarning true if this is a warning; false if this is an error
   */
  public DJError(File file, int lineNumber, int startColumn, String message, boolean isWarning) {
//    System.err.println("instance of DJError (or subclass) constructed; file = " + file + "; message = " + message);
    // need to precisely match the CompilerError message, otherwise a file name containing
    // "CompilerError" may trigger an UnexpectedException (see bug 2872797)
    if (message != null && message.startsWith("Compile exception: ")) throw new UnexpectedException(message);
    _file = file;
    _lineNumber = lineNumber;
    _startColumn = startColumn;
    _message = message;
    _isWarning = isWarning;
    //TODO
    _log.log("_lineNumber= "+_lineNumber);
    _log.log("_file= "+_file);
    if (lineNumber < 0) _noLocation = true;
  }
  
  /** Constructor for an DJError with an associated file but no location in the source 
   * @param     file the file where the error occurred
   * @param     message  the error message
   * @param     isWarning true if this is a warning; false if this is an error
   */
  public DJError(File file, String message, boolean isWarning) { this(file, -1, -1, message, isWarning); }
  
  /** Constructor for CompilerErrors without files.
   * @param message the error message
   * @param isWarning true if this is a warning; false if this is an error
   */
  public DJError(String message, boolean isWarning) { this(null, message, isWarning); }
  
  /** @return true if and only if the given error has no location */
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
  
  /** Sets the line number.
    * @param ln line number
    */
  public void setLineNumber(int ln) {
	//TODO
	    _log.log("in setLineNumber  _lineNumber= "+_lineNumber);
	  _lineNumber = ln;
	  }
  
  /** Gets the column where the error begins.
    * @return the starting column
    */
  public int startColumn() { return  _startColumn; }
  
  /** Gets the error message.
    * @return the error message.
    */
  public String message() { return  _message; }
  
  /** @return a message telling the file this error is from appropriate to 
   *         display to a user, indicating if there is no file associated 
   *         with this error.
   */
  public String getFileMessage() {
    if (_file == null || _file == FileOps.NULL_FILE) return "(no associated file)";
    return fileName();
  }
  
  /** This function returns a message telling the line this error is from 
   * appropriate to display to a user, indicating if there is no file 
   * associated with this error.  This is adjusted to show one-based numbers,
   * since internally we store a zero-based index.
   * @return the message
   */
  public String getLineMessage() {
    if (_file == null || _file == FileOps.NULL_FILE || this._lineNumber < 0) return "(no source location)";
    return "" + (_lineNumber + 1);
  }
  
  /** Determines if the error is a warning.
    * @return true if the error is a warning.
    */
  public boolean isWarning() { return  _isWarning; }
  
  /** Compares by file, then by line, then by column.  Errors without files are considered equal, but less than any 
    * errors with files.  Warnings are considered greater than errors when they are otherwise equal.
    */
  public int compareTo(DJError other) {
    
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
//    boolean otherWarning = other.isWarning();
    return compareErrorWarning(other);
  }
  
  /** Compares this error's postion with other error's, based first on line 
   * number, then by column. 
   * @param other the error to compare this to
   * @return the difference in lines (or columns, if lines are equal)
   */
  private int compareByPosition(DJError other) {
    // Compare by line unless lines are equal
    int byLine = _lineNumber - other.lineNumber();
    if (byLine != 0) return byLine;
    
    int byCol = _startColumn - other.startColumn();
    if (byCol != 0) return byCol;
    return compareErrorWarning(other);
  }
  
  /** Compare otherwise equal errors. 
   * @param other the error to compare this to
   * @return 0 if both are warnings or both are errors; 1 if this is a 
   *         warning other is an error; -1 if this is an error and other is a 
   *         warning
   */
  private int compareErrorWarning(DJError other) {
    return (isWarning()? (other.isWarning()? 0 : 1) : (other.isWarning()? -1 : 0));
  }
}
