/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

// Commented code below belonged to DynamicJava that got overriden by code
// imported from the earlier DrJava's Interactions preprocessor

///**
// * To represent the source code informations
// */
//public static class SourceInformation {
//  // The fields
//  private String filename;
//  private int    line;
//  private int    column;
//  
//  /**
//   * Creates a source information
//   */
//  public SourceInformation(String filename, int line, int column) {
//    this.filename = filename;
//    this.line     = line;
//    this.column   = column;
//  }
//  
//  /**
//   * Returns the filename
//   */
//  public String getFilename() {
//    return filename;
//  }
//  
//  /**
//   * Returns the line where the error occurs
//   */
//  public int getLine() {
//    return line;
//  }
//  
//  /**
//   * Returns the column where the error occurs
//   */
//  public int getColumn() {
//    return column;
//  }
//}


// Code below imported from the earlier DrJava's Interactions preprocessor
package koala.dynamicjava;

import java.io.*;

/**
 * A simple tuple class to represent source location.
 * TODO: Add comments.
 */
public final class SourceInfo {
  /**
   * The source file
   */
  private final File _file;
  
  /**
   * The starting line of the source location
   */
  private final int _startLine;
  
  /**
   * The starting column of the source location
   */
  private final int _startColumn;
  
  /**
   * The ending line of the source location
   */
  private final int _endLine;
  
  /**
   * The ending column of the source location
   */
  private final int _endColumn;

  /**
   * Constructs a SourceInfo.
   * @param file The source file
   * @param startLine Starting line
   * @param startColumn Starting column
   * @param endLine Ending line
   * @param endColumn Ending column
   */
  public SourceInfo(File file,
                    int startLine,
                    int startColumn,
                    int endLine,
                    int endColumn)
  {
    _file = file;
    _startLine = startLine;
    _startColumn = startColumn;
    _endLine = endLine;
    _endColumn = endColumn;
  }

  /**
   * Constructs an empty SourceInfo.
   */
  public SourceInfo()
  {
    _file = new File("");
    _startLine = -1;
    _startColumn = -1;
    _endLine = -1;
    _endColumn = -1;
  }

  /**
   * Getter Method
   * @return Source file
   */
  final public File getFile() { return _file; }
  
  /**
   * Getter Method
   * @return Source filename
   */
  final public String getFilename() { return _file.getName(); }
  
  /**
   * Getter Method
   * @return Starting line
   */
  final public int getStartLine() { return _startLine; }
  
  /**
   * Getter Method
   * @return Starting column
   */
  final public int getStartColumn() { return _startColumn; }
  
  /**
   * Getter Method
   * @return Ending line
   */
  final public int getEndLine() { return _endLine; }
  
  /**
   * Getter Method
   * @return Ending column
   */
  final public int getEndColumn() { return _endColumn; }

  /**
   * Returns a string representation of the source information.  The format is as following:
   * [fileName: (startLine,startColumn)-(endLine,endColumn)]
   * If there is no file then fileName is "(no file)"
   * @return The string format of the source info
   */
  public String toString() {
    String fileName;
    if (_file == null) {
      fileName = "(no file)";
    }
    else {
      fileName = _file.getName();
    }

    return "[" + fileName + ": " +
           "(" + _startLine + "," + _startColumn + ")-" +
           "(" + _endLine + "," + _endColumn + ")]";
  }

  /**
   * Method for determining the equality of two source locations - overriden from Object
   * The method for determining if two source locations are equal is as follows:
   * 
   * The two Files must be equal using the File.equals method
   * The integers for the corresponding Start/End Line/Column must be identical
   * @return Whether or not the two SourceInfo objects are equal
   */
  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj.getClass() != this.getClass()) {
      return false;
    }
    else {
      SourceInfo casted = (SourceInfo) obj;

      File tF = getFile();
      File oF = casted.getFile();

      if ( ((tF == null) && (oF != null)) ||
           ((tF != null) && ! tF.equals(oF)))
      {
        return false;
      }

      if (! (getStartLine() == casted.getStartLine())) return false;
      if (! (getStartColumn() == casted.getStartColumn())) return false;
      if (! (getEndLine() == casted.getEndLine())) return false;
      if (! (getEndColumn() == casted.getEndColumn())) return false;
      return true;
    }
  }

  /**
   * Implementation of hashCode that is consistent with
   * equals. The value of the hashCode is formed by
   * XORing the hashcode of the class object with
   * the hashcodes of all the fields of the object.
   * @return A hashcode for a SourceInfo object
   */
  public final int hashCode() {
    int code = getClass().hashCode();

    if (getFile() != null) {
      code ^= getFile().hashCode();
    }

    code ^= getStartLine();
    code ^= getStartColumn();
    code ^= getEndLine();
    code ^= getEndColumn();
    return code;
  }
}
