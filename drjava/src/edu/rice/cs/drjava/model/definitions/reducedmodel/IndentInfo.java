/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/** Indent information block.
 *  @version $Id: IndentInfo.java 5668 2012-08-15 04:58:30Z rcartwright $
 */
public class IndentInfo {
  private String _lineEnclosingBraceType;      //the type of brace at the beginning of our line

  //the distance to the start of the line containing the brace that encloses the start of our line.
  //____\n|_____
  private int _distToLineEnclosingBraceStart;   /* formerly distToNewline */

  //distance to the brace enclosing the start of our line  ____|{_____
  private int _distToLineEnclosingBrace;  /* formerly distToBrace */

  private String _enclosingBraceType; /* formely braceTypeCurrent */ // type of brace enclosing current location
  
  // distance to the start of the line containing the brace enclosing the current location
  private int _distToEnclosingBraceStart;  /* formerly distToNewlineCurrent */ 

  // distance to the brace enclosing the current location
  private int _distToEnclosingBrace; /* formerly distToBraceCurrent */

  //the distance to the start of the current line
  private int _distToStart; /* formerly distToStart */

  static public final String NONE = "";
  static public final String OPEN_CURLY = "{";
  static public final String OPEN_PAREN = "(";    /* identical constant defeind in BraceInfo class */
  static public final String OPEN_BRACKET = "[";
  
  /** Creates an IndentInfo with default values. */
  public IndentInfo() {
    _lineEnclosingBraceType = NONE;
    _distToLineEnclosingBraceStart = -1;
    _distToLineEnclosingBrace = -1;
    _enclosingBraceType = NONE;
    _distToEnclosingBraceStart = -1;
    _distToEnclosingBrace = -1;
    _distToStart = -1;
  }

  /** Creates an indent info with the specified parameters
    * @param lineEnclosingBraceType the enclosingBraceType
    * @param distToLineEnclosingBraceStart the distance to the next newline
    * @param distToLineEnclosingBrace the distance to a brace
    * @param distToStart the distance to the previous newline
    */
  public IndentInfo(String lineEnclosingBraceType, int distToLineEnclosingBraceStart, int distToLineEnclosingBrace, 
                    int distToStart) {
    _lineEnclosingBraceType = lineEnclosingBraceType;
    _distToLineEnclosingBraceStart = distToLineEnclosingBraceStart;
    _distToLineEnclosingBrace = distToLineEnclosingBrace;
    _distToStart = distToStart;
  }
  
  public String lineEnclosingBraceType() { return _lineEnclosingBraceType; }
  public int distToLineEnclosingBraceStart() { return _distToLineEnclosingBraceStart; }
  public int distToLineEnclosingBrace() { return _distToLineEnclosingBrace; }
  public String enclosingBraceType() { return _enclosingBraceType; }
  public int distToEnclosingBraceStart() { return _distToEnclosingBraceStart; }
  public int distToEnclosingBrace() { return _distToEnclosingBrace; }
  public int distToStart() { return _distToStart; }
  
  public void setLineEnclosingBraceType(String t) { _lineEnclosingBraceType = t; }
  public void setDistToLineEnclosingBraceStart(int d) { _distToLineEnclosingBraceStart = d; }
  public void setDistToLineEnclosingBrace(int d) { _distToLineEnclosingBrace = d; }
  public void setEnclosingBraceType(String t) { _enclosingBraceType = t; }
  public void setDistToEnclosingBraceStart(int d) { _distToEnclosingBraceStart = d; }
  public void setDistToEnclosingBrace(int d) { _distToEnclosingBrace = d; }
  public void setDistToStart(int d) { _distToStart = d; }
  
  public String toString() {
    return "IdentInfo[" + _distToStart + ", " + _lineEnclosingBraceType + ", " + _distToLineEnclosingBrace + ", " +
      _distToLineEnclosingBraceStart + ", " + _enclosingBraceType + ", " + _distToEnclosingBrace + ", " + 
      _distToEnclosingBraceStart + "]";
  }
}

