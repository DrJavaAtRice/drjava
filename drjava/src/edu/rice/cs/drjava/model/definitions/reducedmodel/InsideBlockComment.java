/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * Shadowing state that corresponds to being inside a block comment.
 * @version $Id$
 */
public class InsideBlockComment extends ReducedModelState {
  public static final InsideBlockComment ONLY = new InsideBlockComment();
  
  private InsideBlockComment() {
  }
  
    /**
   * Walk function for inside block comment.
   *  Self-recursive and mutually recursive with other walk functions.
   *  <ol>
   *   <li> If we've reached the end of the list, return.
   *   <li> If we find * /, combine it into a single Brace, and
   *        keep the cursor on that Brace.
   *   <li> If we find // or /*, split that into two Braces and keep the cursor
   *        on the first one.
   *   <li> If current brace = * /, mark the current brace as FREE,
   *        go to the next brace, and call updateFree.<BR>
   *        Else, mark current brace as INSIDE_BLOCK_COMMENT
   *        and go to next brace and recur.
   *  </ol>
   */
  ReducedModelState update(TokenList.Iterator copyCursor) {
    if (copyCursor.atEnd()) {
      return STUTTER;
    }
    _combineCurrentAndNextIfFind("*", "/", copyCursor);
    _combineCurrentAndNextIfFind("*","//", copyCursor);
    _combineCurrentAndNextIfFind("*","/*", copyCursor);
    _combineCurrentAndNextIfFind("","", copyCursor);    
    _combineCurrentAndNextIfEscape(copyCursor);                                              
        
    copyCursor._splitCurrentIfCommentBlock(false, false);
    
    String type = copyCursor.current().getType();
    if (type.equals("*/")) {
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return FREE;
    }
    
    else {
      copyCursor.current().setState(INSIDE_BLOCK_COMMENT);
      copyCursor.next();
      return INSIDE_BLOCK_COMMENT;
    }
  }
}
