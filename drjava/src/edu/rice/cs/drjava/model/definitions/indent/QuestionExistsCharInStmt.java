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

import javax.swing.text.BadLocationException;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Determines if the given search character is found between
 * the start of the current statement and the end character.
 * Accomplishes this by searching backwards from the end
 * character, for the search character until one of the
 * following characters is found: '}', '{', ';', DOCSTART.
 * <b>The given end character must exist on the current line and
 * not be part of a quote or comment.</b> If there is more than 
 * end character on the given line, then the first end character is
 * used.
 * <p>
 * This question is useful for determining if, when a colon is found
 * on a line, it is part of a ternary operator or not (construct
 * this question with '?' for search character and ':' for end
 * character).
 * <p>
 * It can also be used to determine if a statement contains a 
 * particular character by constructing it with the desired
 * character as a search character and the end character as ';'.
 * <p>
 * Note that characters in comments and quotes are disregarded. 
 *
 * @version $Id$
 */
public class QuestionExistsCharInStmt extends IndentRuleQuestion {
  /**
   * The character to search for
   */
  private char _findChar;
  
  /**
   * The character which marks the end of the search
   * space. i.e. search from the start of the statment
   * to the end char.
   */
  private char _endChar;

  /**
   * Constructs a rule to determine if findChar exists
   * between the start of the current statement and endChar.
   *
   * @param findChar Character to search for from the start of the
   * statement to endChar
   * @param endChar Character that marks the end of the search space. Must
   * exist on the current line and not be in quotes or comments.
   * @param yesRule Rule to use if this rule holds
   * @param noRule Rule to use if this rule does not hold
   */
  public QuestionExistsCharInStmt(char findChar, char endChar, IndentRule yesRule, IndentRule noRule) {
    super(yesRule, noRule);
    _findChar = findChar;
    _endChar = endChar;
  }
 
  /**
   * Searches backwards from endChar to the start of the statement
   * looking for findChar. Ignores characters in comments and quotes.
   * Start of the statement is the point right after when one of the
   * following characters is found: ';', '{', '}', DOCSTART.
   *
   * @param doc DefinitionsDocument containing the line to be indented.
   * @return true if this node's rule holds.
   */
  boolean applyRule(DefinitionsDocument doc) {

   // Find the position of endChar on the current line
    int endCharPos = doc.findCharOnLine(doc.getCurrentLocation(), _endChar);
    return doc.findCharInStmtBeforePos(_findChar, endCharPos);
  }
}
