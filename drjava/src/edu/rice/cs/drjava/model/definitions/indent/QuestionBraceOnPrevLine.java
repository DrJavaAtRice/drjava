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
 * Determines whether or not the last opened block or expression 
 * list containing the start of the current line (opened by one of 
 * the characters '{', '(', or '[') was opened on the previous line 
 * (relative to the current position in the document). 
 * This question corresponds to rule 16 in our decision tree.
 * @version $Id$
 */
public class QuestionBraceOnPrevLine extends IndentRuleQuestion 
{
  /**
  * @param yesRule The decision subtree for the case that this rule applies 
  * in the current context.
  * @param noRule The decision subtree for the case that this rule does not
  * apply in the current context.
  */
  public QuestionBraceOnPrevLine(IndentRule yesRule, IndentRule noRule)
  {
    super(yesRule, noRule);
  }
  
  /**
   * @param doc The DefinitionsDocument containing the current line.
   * @param reducedModel reduced model used by the document.
   * @param pos The position in the document to set the caret to.
   * @return True iff the last opened block or expression list containing 
   * the start of the current line (opened by one of the characters '{', 
   * '(', or '[') was opened on the previous line.
   */
  boolean applyRule(DefinitionsDocument doc, BraceReduction reducedModel, int pos)
  {
    doc.setCurrentLocation(pos);
    return applyRule(doc, reducedModel);
  }
  
  /**
   * @param doc The DefinitionsDocument containing the current line.
   * @param reducedModel reduced model used by the document.
   * @return True iff the last opened block or expression list containing 
   * the start of the current line (opened by one of the characters '{', 
   * '(', or '[') was opened on the previous line.
   */
  boolean applyRule(DefinitionsDocument doc, BraceReduction reducedModel)
  {
    // PRE: We are not inside a multiline comment.
    // PRE: The most recently opened expression list or block
    //      was opened by a '{'.
    
    throw new RuntimeException("Not yet implemented!");
    
    /*
    * pos := START
    * counter := 0
    *
    * while (pos > DOCSTART)   
    *    if char[pos] = '{' 
    *       return (counter = 1)     [if pos is not in // comment!!]
    *  
    *    else if char[pos] = '\n'
    *       counter := counter + 1
    *       pos := pos - 1
    *
    * return false
    *
    */
  }
}
