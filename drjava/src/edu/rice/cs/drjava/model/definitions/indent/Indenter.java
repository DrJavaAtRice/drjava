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
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceReduction;

/**
 * Singleton class to construct and use the indentation decision tree.
 *
 * @version $Id$
 */
public class Indenter
{
  /**
   * Singleton instance.
   */
  public static final Indenter ONLY = new Indenter();

  /**
   * Private constructor for singleton instance.
   */
  private Indenter()  { buildTree(); }
  
  /**
   * Hardcoded indent size, for now.
   */
  private String _indentLevel = "  ";  // 2 spaces

  /**
   * Root of decision tree.
   */
  private IndentRule _topRule;
  
  /**
   * Builds the decision tree for indentation.
   * 
   * For now, this method needs to be called every time the
   * size of one indent level is being changed!
   */
  public void buildTree()
  {
    String oneLevel = _indentLevel;
    
    IndentRule 
      rule05 = new ActionStartPrevLinePlus(" "),
      rule04 = new ActionStartPrevLinePlus(" * "), 
      rule03 = new QuestionCurrLineEmpty(rule04, rule05);
    
    IndentRule 
      rule33 = new ActionStartCurrStmtPlus(oneLevel),
      rule32 = new ActionStartStmtOfBracePlus(oneLevel),
      rule31 = rule33,
      rule30 = new QuestionExistsCharInStmt('?', ':', rule31, rule32),
      rule29 = new QuestionLineContains(':', rule30, rule33),
      rule28 = new ActionStartCurrStmtPlus(""),
      rule27 = new QuestionCurrLineStartsWithSkipComments("{", rule28, rule29),
      rule26 = new ActionStartPrevStmtPlus(""),
      rule25 = rule32,
      rule24 = rule26,
      rule23 = new QuestionExistsCharInStmt('?', ':', rule24, rule25),
      rule22 = new QuestionLineContains(':', rule23, rule26),
      rule21 = new ActionStartStmtOfBracePlus(""),
      rule19 = new QuestionStartingNewStmt(rule22, rule27),
      rule18 = rule19,
      rule17 = rule32,
      rule16 = new QuestionStartAfterOpenBrace(rule17, rule18),
      rule20 = new QuestionCurrLineStartsWithSkipComments("}", rule21, rule16),
      rule15 = new QuestionBraceIsCurly(rule20, rule19),
      rule14 = new ActionBracePlus(" " + oneLevel),
      rule13 = new ActionBracePlus(" "),
      rule12 = new QuestionNewParenPhrase(rule13, rule14),
      rule11 = new QuestionBraceIsParenOrBracket(rule12, rule15),
      rule10 = new ActionStartPrevLinePlus(""),
      rule9b = rule10,
      rule9a = new ActionStartPrevLinePlus("* "),
      rule09 = new QuestionCurrLineEmpty(rule9a, rule9b),
      rule08 = rule10,          
      rule07 = new QuestionCurrLineStartsWith("*", rule08, rule09),
      rule06 = new QuestionPrevLineStartsWith("*", rule07, rule10),
      rule02 = new QuestionPrevLineStartsComment(rule03, rule06),
      rule01 = new QuestionInsideComment(rule02, rule11);
    
    _topRule = rule01;
  }

  /**
   * Indents the current line based on a decision tree which determines
   * the indent based on context.
   * @param doc document containing line to be indented
   */
  public void indent(DefinitionsDocument doc)
  {
    _topRule.indentLine(doc);
  }
}





