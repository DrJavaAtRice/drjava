/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;

/** Singleton class to construct and use the indentation decision tree.
  * @version $Id$
  */
public class Indenter {
  
  public Indenter(int indentLevel) { 
    _indentLevel = indentLevel;
    buildTree(indentLevel); 
  }
  
  private final int _indentLevel;
  
  /** Enumeration of reasons why indentation may be preformed. */
  public enum IndentReason {
    /** Indicates that an enter key press caused the indentation.  This is important for some rules dealing with stars
      * at the line start in multiline comments
      */
    ENTER_KEY_PRESS,
      /** Indicates that indentation was started for some other reason.  This is important for some rules dealing with stars
        * at the line start in multiline comments
        */
      OTHER
  }
  
  /** Root of decision tree. */
  protected IndentRule _topRule;
  
  public int getIndentLevel() { return _indentLevel; }
  
  /** Builds the decision tree for indentation.
    * For now, this method needs to be called every time the size of one indent level is being changed!
    */
  public void buildTree(int indentLevel) {
    char[] indent = new char[indentLevel];
    java.util.Arrays.fill(indent,' ');
    
    boolean autoCloseComments = false;
    try { autoCloseComments = DrJava.getConfig().getSetting(OptionConstants.AUTO_CLOSE_COMMENTS).booleanValue(); }
    catch(Exception e) { /* ignore */ }  // some unit tests produce NullPointer exceptions in preceding line
    
    IndentRule
      // Main tree
      rule40 = new ActionStartPrevLinePlus(""),
      rule39 = new ActionStartPrevStmtPlus(0, true),  // Indent line that starts new statement
      rule37 = new ActionStartCurrStmtPlus(2),  // continuation of current statement
      rule36 = new ActionStartStmtOfBracePlus(indentLevel),
      rule35 = new ActionStartLineOf("if"),
      rule34 = new QuestionExistsCharInStmt('?', ':', rule37, rule36),
      rule33 = new QuestionLineContains(':', rule34, rule37),
      rule32 = new ActionStartCurrStmtPlus(0),
      rule31 = new QuestionCurrLineStartsWithSkipComments(new char[] {'{'}, rule32, rule37),
      rule30 = new QuestionCurrLineStartsWith("case", /* accept comments */ false, rule36, rule39),
      // Does this new statement begin with "else"?  If so, must match corresponding "if" (the prev stmt)
      rule29 = new QuestionCurrLineStartsWith("else", /* accept comments */ false, rule35, rule30),
      rule25 = new QuestionStartingNewStmt(rule29, rule31),  // Is this line the start of a new statement?
      rule24 = new QuestionPrevLineStartsWith("@", rule40, rule25),  // Does this line follow an annotation?  ??
      // In Scala, array initializers do not have Java syntax
//      // Is this line an element of an array initializer?
//      rule22 = new QuestionHasCharPrecedingOpenBrace(new char[] {'=',',','{'}, rule36, rule24),
      // Does the preceding line contain the enclosing brace as last NonWS char?
      rule20 = new QuestionStartAfterOpenBrace(rule36, /* rule22 */ rule24),  
      rule19 = new ActionStartStmtOfBracePlus(0),  // indents the line to match whitespace preceding the line enclosing brace
      
      // root of non-comment indent tree for Scala: is brace enclosing start of this line a square bracket?  
      // Does current line begin with '}' or ')' ignoring comment text, WS  TODO: check for balanced braces
      rule18 = new QuestionCurrLineStartsWithSkipComments(new char[] {')', '}'}, rule19, rule20),
     
      // Part of a revised Java decision tree follows commented out
      // Is brace enclosing this line '{' or '(' (as opposed to quotes, etc.); '[' already excluded  
//      rule17 = new QuestionBraceIsCurly(rule18, rule24),
      

//      rule16 = new ActionBracePlus(1 + indentLevel),
//      rule15 = new ActionBracePlus(1),
      
//      rule38 = new ActionBracePlus(0),
//      rule14 = new QuestionNewParenPhrase(rule15, rule16),  // is current non ) line a new phrase after open paren?
//      rule23 = new QuestionNewParenPhrase(rule30, rule38),  // is current ) line a new phrase after open paren?
//      rule21 = new QuestionCurrLineStartsWith(")", rule23, rule14), // does current line start with ')'?
      
//      rule13 = new QuestionBraceIsBracket(rule16, rule18),   
      // rule 8, 11, 13, 14, 15, 16, 17, 21, 22, 23, 26, 27, 28, 29, 30, 35, 38, 40, 42 avail
      
      // Comment tree
      rule12 = new ActionStartPrevLinePlus(""),
      rule10 = new ActionStartPrevLinePlus("* "),
      rule09 = new QuestionCurrLineEmptyOrEnterPress(rule10, rule12),
      rule07 = new QuestionCurrLineStartsWith("*", rule12, rule09),
      rule06 = new QuestionPrevLineStartsWith("*", rule07, rule12),
      rule05 = new ActionStartPrevLinePlus(" "),    // padding prefix for interior of ordinary block comment
      rule04 = new ActionStartPrevLinePlus(" * "),  // padding prefix for new line within ordinary block comment
      rule46 = new ActionStartPrevLinePlus("  * "), // padding prefix for new line within special javadoc block comment
      rule47 = new ActionStartPrevLinePlus("  "),   // padding prefix for interior of special javadoc block comment
      rule45 = new QuestionPrevLineStartsJavaDocWithText(rule46, rule04),  // Prev line begins special javadoc comment?
      rule48 = new QuestionPrevLineStartsJavaDocWithText(rule47, rule05),  // Prev line begins special javadoc comment? 
      rule41 = new ActionStartPrevLinePlusMultilinePreserve(new String[] { " * \n", " */" }, 0, 3, 0, 3),
      rule49 = new ActionStartPrevLinePlusMultilinePreserve(new String[] { "  * \n", "  */"}, 0, 4, 0, 4),
      rule50 = new QuestionPrevLineStartsJavaDocWithText(rule49, rule41),
      
      rule03 = new QuestionCurrLineEmptyOrEnterPress(rule45, rule48),
      rule51 = new QuestionCurrLineEmpty(rule50, rule03), // autoClose: rule03 unnecessarily retests CurrentLineEmpty
      rule02 = new QuestionPrevLineStartsComment(autoCloseComments ? rule51 : rule03, rule06),
      rule43 = new ActionDoNothing(),
      rule44 = new QuestionCurrLineIsWingComment(rule43, rule18),
      rule01 = new QuestionInsideComment(rule02, rule44);
    
    _topRule = rule01;
  }
  
  /** Indents the current line based on a decision tree which determines the indent based on context.
    * @param doc document containing line to be indented  Assumes that reduced lock is already held.
    * @return true if the condition tested by the top rule holds, false otherwise
    */
  public boolean indent(AbstractDJDocument doc, Indenter.IndentReason reason) {
//    Utilities.showDebug("Indenter.indent called on doc "  + doc);
    return _topRule.indentLine(doc, reason);
  }
}




