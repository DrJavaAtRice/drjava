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

package koala.dynamicjava.tree;

import java.io.*;

import edu.rice.cs.plt.object.ObjectUtil;
import edu.rice.cs.plt.tuple.Option;

/** A simple tuple class to represent source location. */
public final class SourceInfo implements Comparable<SourceInfo> {
  
  public interface Wrapper {
    SourceInfo getSourceInfo();
  }
  
  public static final SourceInfo NONE = new SourceInfo(Option.<File>none(), 0, 0, 0, 0);
  
  public static SourceInfo point(File f, int line, int column) {
    return new SourceInfo(Option.wrap(f), line, column, line, column);
  }
  
  public static SourceInfo range(File f, int startLine, int startColumn, int endLine, int endColumn) {
    return new SourceInfo(Option.wrap(f), startLine, startColumn, endLine, endColumn);
  }
  
  public static SourceInfo extend(SourceInfo si, int endLine, int endColumn) {
    return new SourceInfo(si._file, si._startLine, si._startColumn, endLine, endColumn);
  }
  
  public static SourceInfo extend(Wrapper wrapper, int endLine, int endColumn) {
    return extend(wrapper.getSourceInfo(), endLine, endColumn);
  }
  
  public static SourceInfo prepend(int startLine, int startColumn, SourceInfo si) {
    return new SourceInfo(si._file, startLine, startColumn, si._endLine, si._endColumn);
  }
  
  public static SourceInfo prepend(int startLine, int startColumn, Wrapper wrapper) {
    return prepend(startLine, startColumn, wrapper.getSourceInfo());
  }
  
  public static SourceInfo span(SourceInfo first, SourceInfo second) {
    assert ObjectUtil.equal(first._file, second._file);
    return new SourceInfo(first._file, first._startLine, first._startColumn, second._endLine, second._endColumn);
  }
  
  public static SourceInfo span(SourceInfo first, Wrapper second) {
    return span(first, second.getSourceInfo());
  }
  
  public static SourceInfo span(Wrapper first, SourceInfo second) {
    return span(first.getSourceInfo(), second);
  }
  
  public static SourceInfo span(Wrapper first, Wrapper second) {
    return span(first.getSourceInfo(), second.getSourceInfo());
  }
  
  /**
   * The source file.
   */
  private final Option<File> _file;
  
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

  private SourceInfo(Option<File> file, int startLine, int startColumn, int endLine, int endColumn) {
    _file = file;
    _startLine = startLine;
    _startColumn = startColumn;
    _endLine = endLine;
    _endColumn = endColumn;
  }
  
  /** May be null, if the source file is unknown. TODO: Change this interface to Option<File>.  */
  public File getFile() { return _file.unwrap(null); }
  
  /** Get the file's name, or {@code "(no file)"}. */
  public String getFilename() { return _file.isNone() ? "(no file)" : _file.unwrap().getPath(); }
  
  public int getStartLine() { return _startLine; }
  public int getStartColumn() { return _startColumn; }
  public int getEndLine() { return _endLine; }
  public int getEndColumn() { return _endColumn; }
  
  /**
   * Returns a string representation of the source information.  The format is as following:
   * [fileName: (startLine,startColumn)-(endLine,endColumn)]
   * If there is no file then fileName is "(no file)"
   * @return The string format of the source info
   */
  public String toString() {
    return "[" + getFilename() + ": " +
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
  @Override public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    else {
      SourceInfo casted = (SourceInfo) obj;
      return
        this._file.equals(casted._file) &&
        this._startLine == casted._startLine &&
        this._startColumn == casted._startColumn &&
        this._endLine == casted._endLine &&
        this._endColumn == casted._endColumn;
    }
  }

  @Override public int hashCode() {
    return ObjectUtil.hash(getClass(), _file, _startLine, _startColumn, _endLine, _endColumn);
  }
  
  public int compareTo(SourceInfo that) {
    int result = Option.<File>comparator().compare(this._file, that._file);
    if (result == 0) {
      result = ObjectUtil.compare(this._startLine, that._startLine,
                                  this._startColumn, that._startColumn,
                                  this._endLine, that._endLine,
                                  this._endColumn, that._endColumn);
    }
    return result;
  }
  
}
