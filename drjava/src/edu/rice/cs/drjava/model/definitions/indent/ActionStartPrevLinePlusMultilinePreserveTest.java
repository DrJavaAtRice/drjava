/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2004 JavaPLT group at Rice University (javaplt@rice.edu)
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

import junit.framework.*;
import edu.rice.cs.drjava.model.AbstractDJDocument;
import javax.swing.text.BadLocationException;

/**
 * Tests ActionStartPrevLinePlusMultilinePreserve(String,int,int,int,int).
 * It specifically tests the behavior of the auto-closing comments feature.
 * This means it tests cases where the user has just hit ENTER somewhere
 * on the opening line of a block comment.
 *
 * @version $Id$
 */
public class ActionStartPrevLinePlusMultilinePreserveTest extends IndentRulesTestCase {
  
  /**
   * This is a clever (IMHO) factory trick to reuse these methods in TestCases
   * for logically similar IndentActions.
   * @see ActionStartPrevLinePlusMultilinePreserve#ActionStartPrevLinePlusMultilinePreserve(String[], int, int, int, int)
   */
  private IndentRuleAction makeAction(String[] suffices,
                                      int cursorLine, int cursorPos,
                                      int psrvLine, int psrvPos) {
    return new ActionStartPrevLinePlusMultilinePreserve(suffices,
                                                        cursorLine, cursorPos,
                                                        psrvLine, psrvPos);
  }
  
  /**
   * This method abstracts the common processes of the tests so that the tests
   * themselves may only contain information about original conditions and
   * expected results.
   * @param start The text that should be in the document at time rule is called
   * @param loc the location of the cursor when rule is called
   * @param endLoc the expected final size of the document
   * @param finish the text string that remaining after the rule is called
   */
  public void helperCommentTest(String start, int loc, int endLoc, String finish) throws 
    BadLocationException {
      _setDocText(start);
      _doc.setCurrentLocation(loc);
      makeAction(new String[]{" * \n", " */"},0,3,0,3).indentLine(_doc, Indenter.ENTER_KEY_PRESS);
      assertEquals(endLoc, _doc.getCurrentLocation());
      //assertEquals(finish.length(), _doc.getLength());
      assertEquals(finish, _doc.getText(0, finish.length()));
  }

  public void test1() throws BadLocationException {
    helperCommentTest("/**\n",
                      4, 7,
                      "/**\n * \n */");
  }

  public void test2() throws BadLocationException {
    helperCommentTest("   /**\n",
                      7, 13,
                      "   /**\n    * \n    */");
  }

  public void test3() throws BadLocationException {
    helperCommentTest("/* abc\ndefg\n   hijklmnop",
                      7, 10,
                      "/* abc\n * defg\n */\n   hijklmnop");
  }

  public void test4() throws BadLocationException {
    helperCommentTest("/* \nThis is a comment */",
                      4, 7,
                      "/* \n * This is a comment */\n */");
  }

  public void test5() throws BadLocationException {
    helperCommentTest("/* This is code\n     and more */",
                      16, 19,
                      "/* This is code\n *      and more */\n */");
  }

  public void test6() throws BadLocationException {
///* This |is a comment block
//   That is already closed */
//---------------------------------
///* This 
// * |is a comment block
// */
//   That is already closed */
//---------------------------------
//// (After undo command)
//
///* This
// * |is a comment block
//   That is already closed */
    helperCommentTest("/* This \nis a comment block\n   That is already closed */",
                      9, 12,
                      "/* This \n * is a comment block\n */\n   That is already closed */");
  }
  
  public void test7() throws BadLocationException {
///* This |is a comment block
// * That is already closed 
// */
//
//---------------------------------
///* This 
// * |is a comment block
// */
// * That is already closed 
// */
//---------------------------------
//(after undo command)
///* This 
// * |is a comment block
// * That is already closed 
// */
  helperCommentTest("/* This \nis a comment block\n * That is already closed \n */",
                      9, 12,
                      "/* This \n * is a comment block\n */\n * That is already closed \n */");
  }
  
  public void xtest8() throws BadLocationException {
///* ABC | */
//
//---------------------------------
///* ABC
// * |
// */
    helperCommentTest("/* ABC \n */",
                      8, 11,
                      "/* ABC \n *  */\n */");
  }
  
  public void xtest9() throws BadLocationException {
///**|
// * Text
// */
//---------------------------------
///**
// * |
// * Text
// */
    helperCommentTest("/**\n * Text\n */",
                      4, 7,
                      "/**\n * \n * Text\n */");
  }
  
  public void test10() throws BadLocationException {
///** This is |bad */ **/
//
//---------------------------------
///** This is 
// * bad */ **/
// */
    
    helperCommentTest("/** This is \nbad */ **/",
                      13, 16,
                      "/** This is \n * bad */ **/\n */");
  }

  public void xtest11() throws BadLocationException {
///** ABC **/ | /** ABC **/
//
//---------------------------------
///** ABC **/
//|/** ABC **/

    helperCommentTest("/** ABC **/ \n /** ABC **/",
                      13, 13,
                      "/** ABC **/ \n/** ABC **/");
  }
  
}