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

/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2004 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/


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
  private final File _file;
  private final int _startLine;
  private final int _startColumn;
  private final int _endLine;
  private final int _endColumn;

  /**
   * Constructs a SourceInfo.
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

  final public File getFile() { return _file; }
  final public String getFilename() { return _file.getName(); }
  final public int getStartLine() { return _startLine; }
  final public int getStartColumn() { return _startColumn; }
  final public int getEndLine() { return _endLine; }
  final public int getEndColumn() { return _endColumn; }

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
