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
 * Shadowing state that indicates normal, unshadowed text.
 * @version $Id$
 */
public class Free extends ReducedModelState {
  public static final Free ONLY = new Free();
  
  private Free() {
  }
  
  /**
   *  Walk function for when we're not inside a string or comment.
   *  Self-recursive and mutually recursive with other walk functions.
   *  <ol>
   *   <li> atEnd: return
   *   <li> If we find / *, * /, or / /, combine them into a single Brace,
   *        and keep the cursor on that Brace.
   *   <li> If current brace = //, go to next then call updateLineComment.<BR>
   *        If current brace = /*, go to next then call updateBlockComment.<BR>
   *        If current brace = ", go to next then call updateInsideDoubleQuote.<BR>
   *        Else, mark current brace as FREE, go to the next brace, and recur.
   * </ol>
   */
  ReducedModelState update(TokenList.Iterator copyCursor) {
    if (copyCursor.atEnd()) {
      return STUTTER;
    }
    
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
