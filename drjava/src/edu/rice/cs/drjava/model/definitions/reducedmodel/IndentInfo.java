/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * Indent information block.
 * @version $Id$
 */
public class IndentInfo {
  public String braceType;      //the type of brace at the beginning of our line

  //the distance to the start of the line containing
  //the brace that encloses the start of our line.
  //____\n|_____
  public int distToNewline;

  //distance to the brace enclosing the start of our line  ____|{_____
  public int distToBrace;

  // type of brace at current position
  public String braceTypeCurrent;

  // distance to the start of the line containing the brace enclosing the current location
  public int distToNewlineCurrent;

  // distance to the brace enclosing the current location
  public int distToBraceCurrent;

  //the distance to the start of the current line
  public int distToPrevNewline;

  static public final String noBrace = "";
  static public final String openSquiggly = "{";
  static public final String openParen = "(";
  static public final String openBracket = "[";

  /**
   * Creates an IndentInfo with default values.
   */
  public IndentInfo() {
    braceType = noBrace;
    distToNewline = -1;
    distToBrace = -1;
    braceTypeCurrent = noBrace;
    distToNewlineCurrent = -1;
    distToBraceCurrent = -1;
  }

  /**
   * Creates an indent info with the specified parameters
   * @param _braceType the braceType
   * @param _distToNewline the distance to the next newline
   * @param _distToBrace the distance to a brace
   * @param _distToPrevNewline the distance to the previous newline
   */
  public IndentInfo(String _braceType, int _distToNewline, int _distToBrace, int _distToPrevNewline) {
    braceType = _braceType;
    distToNewline = _distToNewline;
    distToBrace = _distToBrace;
    distToPrevNewline = _distToPrevNewline;
  }
}



