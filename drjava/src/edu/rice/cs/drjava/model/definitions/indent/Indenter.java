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
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.FileConfiguration;
/**
 * Singleton class to construct and use the indentation decision tree.
 *
 * @version $Id$
 */
public class Indenter {

  public Indenter(int indentLevel) {
      buildTree(indentLevel);
  }

  /**
   * This constant is used to indicate that an enter key press caused the
   * indentation.  This is important for some rules dealing with stars
   * at the line start in multiline comments
   */
  public static final int ENTER_KEY_PRESS = 1;

  /**
   * This constant is used to indicate that indentation was started for
   * some other reason.  This is important for some rules dealing with stars
   * at the line start in multiline comments
   */
  public static final int OTHER = 0;

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
  public void buildTree(int indentLevel)
  {
    char[] indent = new char[indentLevel];
    java.util.Arrays.fill(indent,' ');
    String oneLevel = new String(indent);

    IndentRule
      // Main tree
      rule37 = new ActionStartCurrStmtPlus(oneLevel),
      rule36 = new ActionStartStmtOfBracePlus(oneLevel),
      rule35 = rule37,
      rule34 = new QuestionExistsCharInStmt('?', ':', rule35, rule36),
      rule33 = new QuestionLineContains(':', rule34, rule37),
      rule32 = new ActionStartCurrStmtPlus(""),
      rule31 = new QuestionCurrLineStartsWithSkipComments("{", rule32, rule33),
      rule39 = new ActionStartPrevStmtPlus("", true),
      rule29 = rule36,
      rule28 = new ActionStartPrevStmtPlus("", false),
      rule40 = rule28,
      rule30 = new QuestionExistsCharInPrevStmt('?', rule40, rule39),
      rule27 = new QuestionExistsCharInStmt('?', ':', rule28, rule29),
      rule26 = new QuestionLineContains(':', rule27, rule30),
      rule25 = new QuestionStartingNewStmt(rule26, rule31),
      rule24 = rule25,
      rule23 = rule36,
      rule22 = new QuestionHasCharPrecedingOpenBrace(new char[] {'=',',','{'},rule23,rule24),
      rule21 = rule36,
      rule20 = new QuestionStartAfterOpenBrace(rule21, rule22),
      rule19 = new ActionStartStmtOfBracePlus(""),
      rule18 = new QuestionCurrLineStartsWithSkipComments("}", rule19, rule20),
      rule17 = new QuestionBraceIsCurly(rule18, rule25),
      rule16 = new ActionBracePlus(" " + oneLevel),
      rule15 = new ActionBracePlus(" "),
      rule38 = new QuestionCurrLineStartsWith(")",rule30,rule15),  //BROKEN
        // Why is rule 38 here?
      rule14 = new QuestionNewParenPhrase(rule38, rule16), //rule15->rule38
      rule13 = new QuestionBraceIsParenOrBracket(rule14, rule17),

      // Comment tree
      rule12 = new ActionStartPrevLinePlus(""),
      rule11 = rule12,
      rule10 = new ActionStartPrevLinePlus("* "),
      rule09 = new QuestionCurrLineEmptyOrEnterPress(rule10, rule11),
      rule08 = rule12,
      rule07 = new QuestionCurrLineStartsWith("*", rule08, rule09),
      rule06 = new QuestionPrevLineStartsWith("*", rule07, rule12),
      rule05 = new ActionStartPrevLinePlus(" "),
      rule04 = new ActionStartPrevLinePlus(" * "),
      rule03 = new QuestionCurrLineEmptyOrEnterPress(rule04, rule05),
      rule02 = new QuestionPrevLineStartsComment(rule03, rule06),

      rule01 = new QuestionInsideComment(rule02, rule13);

    _topRule = rule01;
  }

  /**
   * Indents the current line based on a decision tree which determines
   * the indent based on context.
   * @param doc document containing line to be indented
   */
  public void indent(DefinitionsDocument doc, int reason)
  {
    _topRule.indentLine(doc, /*, reason*/reason);
  }

}



