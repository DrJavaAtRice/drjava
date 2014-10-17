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

import static edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates.*;

/** Shadowing state that indicates normal, unshadowed text.
  * @version $Id: Free.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class Free extends ReducedModelState {
  public static final Free ONLY = new Free();
  
  private Free() { }
  
  /** Walk function for when we're not inside a string or comment. Mutually recursive with other walk functions.
    * <ol>
    *  <li> atEnd: return
    *  <li> If we find / *, * /, or / /, combine them into a single Brace, and keep the cursor on that Brace.
    *  <li> If current brace = //, go to next then call updateLineComment.<BR>
    *       If current brace = /*, go to next then call updateBlockComment.<BR>
    *       If current brace = ", go to next then call updateInsideDoubleQuote.<BR>
    *       Else, mark current brace as FREE, go to the next brace, and recur.
    * </ol>
    */
  ReducedModelState update(TokenList.Iterator copyCursor) {
    if (copyCursor.atEnd()) return STUTTER;
    
    _combineCurrentAndNextIfFind("/", "*", copyCursor);
    _combineCurrentAndNextIfFind("/", "/", copyCursor);
    _combineCurrentAndNextIfFind("","", copyCursor);
    //if a / preceeds a /* or a // combine them.
    _combineCurrentAndNextIfFind("/","/*",copyCursor);
    _combineCurrentAndNextIfFind("/","//",copyCursor);
    _combineCurrentAndNextIfEscape(copyCursor);
    
    String type = copyCursor.current().getType();
    if (type.equals("*/")) {
      copyCursor._splitCurrentIfCommentBlock(true,false);
      copyCursor.prev();
      return STUTTER;
    }
    else if (type.equals("//")) {
      // open comment blocks are not set commented, they're set free
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_LINE_COMMENT;
    }
    else if (type.equals("/*")) {
      // open comment blocks are not set commented, they're set free
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_BLOCK_COMMENT;
    }
    else if (type.equals("\'")) {
      // make sure this is a OPEN single quote
      if (copyCursor.current().isClosed()) {
        copyCursor.current().flip();
      }
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_SINGLE_QUOTE;
    }
    else if (type.equals("\"")) {
      // make sure this is a OPEN quote
      if (copyCursor.current().isClosed()) {
        copyCursor.current().flip();
      }
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_DOUBLE_QUOTE;
    }
    else {
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return FREE;
    }
  }
}
