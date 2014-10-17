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
 *  @version $Id: BraceInfo.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class BraceInfo {
  
  // TODO: convert String brace type to char brace type
  
  public static final String NONE = "";             // char '\u0000';
  public static final String OPEN_CURLY = "{";      // char '{';
  public static final String OPEN_PAREN    = "(";   // char '(';
  public static final String OPEN_BRACKET  = "[";   // char '[';

  public static final BraceInfo NULL = new BraceInfo(NONE, -1);
  
  private String _braceType;   // one of the four above options

  /** distance to open brace preceding the anchor point (_currentLocation or beginning of line). Distance includes 
    * brace so that (anchor - distance) gives offset of brace. */
  private int _distance; 

  /** Creates an IndentInfo with default values. */
  public BraceInfo(String braceType, int distance) {
    _braceType = braceType;
    _distance = distance;
  }

  /** Gets the _braceType. */
  public String braceType() { return _braceType; }
  
  /** Gets the _distance. */
  public int distance() { return _distance; }
  
  /** @return a new BraceInfo equivalent to this except that this.distance is shifted by dist. NONE is treated 
    * as a special case. */
  public BraceInfo shift(int dist) { 
    if (this == NULL) return NULL;
    return new BraceInfo(_braceType, _distance + dist); 
  }
  
  public String toString() { return "BraceInfo(" + _distance + ", '" + _braceType + "')"; }
}



