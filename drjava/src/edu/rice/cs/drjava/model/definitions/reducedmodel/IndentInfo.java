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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/** Indent information block.
 *  @version $Id$
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

  /** Creates an IndentInfo with default values. */
  public IndentInfo() {
    braceType = noBrace;
    distToNewline = -1;
    distToBrace = -1;
    braceTypeCurrent = noBrace;
    distToNewlineCurrent = -1;
    distToBraceCurrent = -1;
  }

  /** Creates an indent info with the specified parameters
   *  @param _braceType the braceType
   *  @param _distToNewline the distance to the next newline
   *  @param _distToBrace the distance to a brace
   *  @param _distToPrevNewline the distance to the previous newline
   */
  public IndentInfo(String _braceType, int _distToNewline, int _distToBrace, int _distToPrevNewline) {
    braceType = _braceType;
    distToNewline = _distToNewline;
    distToBrace = _distToBrace;
    distToPrevNewline = _distToPrevNewline;
  }
}



