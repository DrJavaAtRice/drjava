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
 * The abstract notion of a shadowing state.  We use shadowing to mean
 * the state of text as it is interpreted during compile.  Commented text
 * is ignored, and quoted text does not factor into the ASTs generated
 * by the compiler except as a text constant. This buys us a lot in
 * terms of correctness when highlighting, indenting, and performing
 * other editor functions.
 * @version $Id$
 */
public abstract class ReducedModelState 
  implements /*imports*/ ReducedModelStates 
{
  abstract ReducedModelState update(TokenList.Iterator copyCursor);
  
  /**
   * Combines the current and next braces if they match the given types.
   * If we have braces of first and second in immediate succession, and if
   * second's gap is 0, combine them into first+second.
   * The cursor remains on the same block after this method is called.
   * @param first the first half of a multiple char brace
   * @param second the second half of a multiple char brace
   * @return true if we combined two braces or false if not
   */
  boolean  _combineCurrentAndNextIfFind
    (String first, 
     String second, 
     TokenList.Iterator copyCursor)
  {
    if (copyCursor.atStart() ||
        copyCursor.atEnd() ||
        copyCursor.atLastItem() ||
        !copyCursor.current().getType().equals(first))
    {
      return false;
    }
    copyCursor.next(); // move to second one to check if we can combine
    
    // The second one is eligible to combine if it exists (atLast is false),
    // if it has the right brace type, and if it has no gap.
    if (copyCursor.current().getType().equals(second)) {
      if ((copyCursor.current().getType().equals("")) &&
          (copyCursor.prevItem().getType().equals(""))) {
        // delete first Gap and augment the second
        copyCursor.prev();
        int growth = copyCursor.current().getSize();
        copyCursor.remove();
        copyCursor.current().grow(growth);
      }
      else if (copyCursor.current().getType().length() == 2) {
        String tail = copyCursor.current().getType().substring(1,2);
        String head = copyCursor.prevItem().getType() + 
          copyCursor.current().getType().substring(0,1);        
        copyCursor.current().setType(tail);
        copyCursor.prev();
        copyCursor.current().setType(head);
        copyCursor.current().setState(FREE);
      }
      else {
        // delete the first Brace and augment the second
        copyCursor.prev();
        copyCursor.remove();
        copyCursor.current().setType(first + second);
      }
      return true;
    }
    else {
      // we couldn't combine, so move back and return
      copyCursor.prev();
      return false;
    }
  }
  
  
  boolean 
    _combineCurrentAndNextIfEscape(TokenList.Iterator copyCursor)
  { 
    boolean combined = false;
    combined = combined || _combineCurrentAndNextIfFind("\\","\\",copyCursor);  // \-\
    combined = combined || _combineCurrentAndNextIfFind("\\","\'",copyCursor);  // \-'
    combined = combined || _combineCurrentAndNextIfFind("\\","\\'",copyCursor);// \-\'
    combined = combined || _combineCurrentAndNextIfFind("\\","\"",copyCursor);  // \-"
    combined = combined || _combineCurrentAndNextIfFind("\\","\\\"",copyCursor);// \-\"
    combined = combined || _combineCurrentAndNextIfFind("\\","\\\\",copyCursor);// \-\\
    return combined;
  }
}
