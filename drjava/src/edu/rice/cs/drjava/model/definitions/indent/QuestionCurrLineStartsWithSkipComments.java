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

import javax.swing.text.*;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Determines whether or not the current line in the document starts
 * with a specific character sequence. This sequence is passed to the
 * constructor of the class as a String argument.
 *
 * @version $Id$
 */
public class QuestionCurrLineStartsWithSkipComments extends IndentRuleQuestion
{
  private String _prefix;
  
  /**
   * @param yesRule The decision subtree for the case that this rule applies
   * in the current context.
   * @param noRule The decision subtree for the case that this rule does not
   * apply in the current context.
   */
  public QuestionCurrLineStartsWithSkipComments(String prefix, IndentRule yesRule, IndentRule noRule)
  {
    super(yesRule, noRule);
    _prefix = prefix;
  }
   
  /**
   * Determines whether or not the current line in the document starts
   * with the character sequence specified by the String field _prefix.
   * @param doc The DefinitionsDocument containing the current line.
   * @return True iff the current line in the document starts with the
   * character sequence specified by the String field _prefix.
   */
  boolean applyRule(DefinitionsDocument doc)
  {
    try
    {
      // Find the first non-whitespace character on the current line.
      
      int
        current = doc.getCurrentLocation(),
        start   = doc.getLineFirstCharPos(current),
        end     = doc.getLineEndPos(current);
      
      
      // Return false if the specified prefix doesn't "fit" on the current line.
      
      if ((start + _prefix.length()) > end)
        return false;
      
      // Return false if the start of the line is inside a comment, or if
      // it is shadowed by single or double quotes.
      
      BraceReduction reduced = doc.getReduced();
      reduced.move(start - current);
      ReducedModelState state = reduced.getStateAtCurrent();
      reduced.move(current - start);
      
      if (!state.equals(ReducedModelStates.FREE))
        return false;
      
      // Compare the specified prefix with the beginning of the current line.
      
      return _prefix.equals(doc.getText(start, _prefix.length()));
    }
    catch (BadLocationException e)
    {
      // Control flow should never reach this point!
      throw new UnexpectedException(new RuntimeException("Bug in QuestionCurrLineStartsWithSkipComments"));
    }
  }
}
