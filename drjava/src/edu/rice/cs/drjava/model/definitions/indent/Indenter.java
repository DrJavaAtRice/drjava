/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 *     - Redistributions of source code must retain the above copyright 
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR 
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
 * OTHER DEALINGS WITH THE SOFTWARE.
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

    boolean autoCloseComments = DrJava.getConfig().getSetting
      (OptionConstants.AUTO_CLOSE_COMMENTS).booleanValue();
    
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
        
      rule41 = new ActionStartPrevLinePlusMultilinePreserve
        (new String[] { " * \n", " */" }, 0, 3, 0, 3),
      rule42 = new QuestionFollowedByStar(rule04, rule41),
      rule03 = new QuestionCurrLineEmptyOrEnterPress
          ((autoCloseComments? rule42 : rule04), rule05),
      rule02 = new QuestionPrevLineStartsComment(rule03, rule06),
        
      rule01 = new QuestionInsideComment(rule02, rule13);

    _topRule = rule01;
  }
  
  /**
   * Indents the current line based on a decision tree which determines
   * the indent based on context.
   * @param doc document containing line to be indented
   * @return true if the caller should update the current location itself,
   * false if the indenter has already handled this
   */
  public boolean indent(DefinitionsDocument doc, int reason)
  {
    return _topRule.indentLine(doc, reason);
  }

}



