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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Given the start of the current line is inside a C-style comment, asks
 * whether the comment begins on the "previous line," ignoring white space.
 * 
 * @version $Id$
 */
class QuestionPrevLineStartsComment extends IndentRuleQuestion {
  
  QuestionPrevLineStartsComment(IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
  }
  
  /**
   * Determines if the previous line in the document starts a block comment.
   * We know that the current line is in a block comment. Therefore, if the
   * start of the previous line is not inside of a block comment, then the
   * previous line must have started the comment. 
   * <p>
   * There is an exception to this; however, it is handled adequately. Consider
   * the case when the previous line contains the following code:
   * <code>*&#47; bar(); &#47;*</code>
   * <p>
   * Our approach will say that since the beginning of the previous line is
   * inside of a comment, the previous line did not start the comment. This
   * is acceptable because we think of the previous line as a continuation
   * of a larger commented out region.
   *
   * @param  doc DefinitionsDocument containing the line to be indented.
   * @return true if this node's rule holds.
   */
  boolean applyRule(DefinitionsDocument doc) {

      int cursor;

    // Move back to start of current line
    cursor = doc.getLineStartPos(doc.getCurrentLocation());
    
    // If the start of the current line is the start of the
    // document, there was no previous line and so this
    // line must have started the comment
    if(cursor == DefinitionsDocument.DOCSTART) {
      return false;
    } else {
      // Move the cursor to the previous line
      cursor = cursor - 1;
      
      // Move it to the start of the previous line
      cursor = doc.getLineStartPos(cursor);
      
      // Return if the start of the previous line is
      // in a comment.
      //BraceReduction reduced = doc.getReduced();
      doc.resetReducedModelLocation();
      ReducedModelState state = doc.stateAtRelLocation(cursor -
          doc.getCurrentLocation());
      return !state.equals(ReducedModelState.INSIDE_BLOCK_COMMENT);
    }
  }
}

