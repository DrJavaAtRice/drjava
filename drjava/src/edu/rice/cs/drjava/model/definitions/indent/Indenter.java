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
  private Indenter() { 
    // Listen to configuration.
    buildTree(); 
  }
  
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
      rule35 = new ActionStartCurrStmtPlus(oneLevel),
      rule34 = new ActionStartStmtOfBracePlus(oneLevel),
      rule33 = rule35,
      rule32 = new QuestionExistsCharInStmt('?', ':', rule33, rule34),
      rule31 = new QuestionLineContains(':', rule32, rule35),
      rule30 = new ActionStartCurrStmtPlus(""),
      rule29 = new QuestionCurrLineStartsWithSkipComments("{", rule30, rule31),
      rule28 = new ActionStartPrevStmtPlus("", true),
      rule27 = rule34,
      rule26 = new ActionStartPrevStmtPlus("", false),
      rule25 = new QuestionExistsCharInStmt('?', ':', rule26, rule27),
      rule24 = new QuestionLineContains(':', rule25, rule28),
      rule23 = new QuestionStartingNewStmt(rule24, rule29),
      rule22 = rule23,
      rule21 = rule34,
      rule20 = new QuestionStartAfterOpenBrace(rule21, rule22),
      rule19 = new ActionStartStmtOfBracePlus(""),
      rule18 = new QuestionCurrLineStartsWithSkipComments("}", rule19, rule20),
      rule17 = new QuestionBraceIsCurly(rule18, rule23),
      rule16 = new ActionBracePlus(" " + oneLevel),
      rule15 = new ActionBracePlus(" "),
      rule14 = new QuestionNewParenPhrase(rule15, rule16),
      rule13 = new QuestionBraceIsParenOrBracket(rule14, rule17),
      rule12 = new ActionStartPrevLinePlus(""),
      rule11 = rule12,
      rule10 = new ActionStartPrevLinePlus("* "),
      rule09 = new QuestionCurrLineEmpty(rule10, rule11),
      rule08 = rule12,          
      rule07 = new QuestionCurrLineStartsWith("*", rule08, rule09),
      rule06 = new QuestionPrevLineStartsWith("*", rule07, rule12),
      rule05 = new ActionStartPrevLinePlus(" "),
      rule04 = new ActionStartPrevLinePlus(" * "), 
      rule03 = new QuestionCurrLineEmpty(rule04, rule05),
      rule02 = new QuestionPrevLineStartsComment(rule03, rule06),
      rule01 = new QuestionInsideComment(rule02, rule13);
    
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





