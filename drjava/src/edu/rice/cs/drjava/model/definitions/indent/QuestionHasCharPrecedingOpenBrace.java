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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;
import javax.swing.text.BadLocationException;


/**
 * Determines whether or not the last '{' was immediately preceded 
 * by _prefix So when _prefix='=', effectivily, we are looking for "={"
 * This questions corresponds to rule 22 in our decision tree.
 * @version $Id$
 */
public class QuestionHasCharPrecedingOpenBrace extends IndentRuleQuestion 
{
  private char[] _prefix;

  /**
   * @param yesRule The decision subtree for the case that this rule applies
   * in the current context.
   * @param noRule The decision subtree for the case that this rule does not
   * apply in the current context.
   */
  public QuestionHasCharPrecedingOpenBrace(char[] prefix, IndentRule yesRule, IndentRule noRule)
  {
    super(yesRule, noRule);
    _prefix = prefix;
  }
  
  /**
   * @param doc The DefinitionsDocument containing the current line.
   * @return True iff the last block or expression list opened previous 
   * to the start of the current line was opened by the character '{'. 
   */
  boolean applyRule(DefinitionsDocument doc)
  {
    // PRE: We are inside a {.
    
    int origin = doc.getCurrentLocation();
    int lineStart = doc.getLineStartPos(origin);
    
    // Get brace for start of line
    synchronized(doc){
      doc.move(lineStart - origin);
      IndentInfo info = doc.getIndentInformation();
      doc.move(origin - lineStart);
      
      
      if ((!info.braceType.equals(IndentInfo.openSquiggly)) ||
          (info.distToBrace < 0)) {
        // Precondition not met: we should have a brace
        return false;
      }
      int bracePos = lineStart - info.distToBrace;
      
      // Get position of previous non-WS char (not in comments)
      int prevNonWS = -1;
      try {
        prevNonWS = doc.findPrevNonWSCharPos(bracePos);
        char c = doc.getText(prevNonWS,1).charAt(0);
        for (int i=0; i<_prefix.length; i++) {
          char prefix = _prefix[i];
          if (c == prefix) {
            return true;
          } 
        }
      }
      catch (BadLocationException e) {
      }    
    }
    return false;
  }
}
