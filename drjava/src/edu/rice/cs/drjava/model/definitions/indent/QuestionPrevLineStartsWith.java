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
 * Question rule in the indentation decision tree.  Determines if the 
 * line previous to the current position starts with the specified character.
 * @version $Id$
 */
public class QuestionPrevLineStartsWith extends IndentRuleQuestion {
  private String _prefix;
  private boolean _searchComments;
  
  /**
   * Constructs a new rule for the given prefix string.
   * Does not look inside comments.
   * @param prefix String to search for
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public QuestionPrevLineStartsWith(String prefix, IndentRule yesRule, IndentRule noRule) {
    this(prefix, false, yesRule, noRule);
  }
  
  /**
   * Constructs a new rule for the given prefix string, allowing
   * user to specify whether to look inside comments.
   * @param prefix String to search for
   * @param searchComments Whether to search for prefix in a comment
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public QuestionPrevLineStartsWith(String prefix, boolean searchComments,
                                    IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _prefix = prefix;
    _searchComments = searchComments;
  }
  
  /**
   * Determines if the previous line in the document stars with the
   * specified character.
   * @param doc DefinitionsDocument containing the line to be indented.
   * @param reducedModel reduced model used by the document.
   * @param pos Position within line to be indented.
   * @return true if this node's rule holds.
   */
  boolean applyRule(DefinitionsDocument doc, BraceReduction reducedModel, int pos) {
    throw new RuntimeException("Not yet implemented.");
    
    /** FIXME: don't look in comments if _searchComments == false
    int start = startOfPrevLine(doc, pos);
    int end = endOfLine(doc, start);
    String text = doc.getText(start, end);
    int prefixPos = text.indexOf(_prefix);
    return (prefixPos == 0);
    */
  }
}