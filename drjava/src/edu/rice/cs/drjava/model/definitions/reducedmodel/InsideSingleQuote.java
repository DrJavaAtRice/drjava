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
 * Shadowing state that corresponds to being between single quotes.
 * @version $Id$
 */
public class InsideSingleQuote extends ReducedModelState {
  public static final InsideSingleQuote ONLY = new InsideSingleQuote();
  
  private InsideSingleQuote() {
  }
  
   /**
   * Walk function for when inside single quotes.
   *  <ol>
   *  <li> If we've reached the end of the list, return.
   *  <li> If we find //, /* or * /, split them into two separate braces.
   *       The cursor will be on the first of the two new braces.
   *  <li> If current brace = \n or ', mark current brace FREE, next(), and
   *       go to updateFree.
   *       Else, mark current brace as INSIDE_SINGLE_QUOTE, go to next brace, recur.
   * </ol>   
   */
  ReducedModelState update(TokenList.Iterator copyCursor) {
    if (copyCursor.atEnd()) {
      return STUTTER;
    }
    copyCursor._splitCurrentIfCommentBlock(true,false);
    _combineCurrentAndNextIfFind("","", copyCursor);
    _combineCurrentAndNextIfEscape(copyCursor);
    
    String type = copyCursor.current().getType();
    
    if (type.equals("\n")) {
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return FREE;
    }
    else if (type.equals("\'")) {
      // make sure this is a CLOSE quote
      if (copyCursor.current().isOpen())
        copyCursor.current().flip();
      
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return FREE;
    }
    else {
      copyCursor.current().setState(INSIDE_SINGLE_QUOTE);
      copyCursor.next();
      return INSIDE_SINGLE_QUOTE;
    }
  }
}
