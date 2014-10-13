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

import javax.swing.text.BadLocationException;

/**
 * Tests ActionStartPrevLinePlusMultilinePreserve(String,int,int,int,int).
 * It specifically tests the behavior of the auto-closing comments feature.
 * This means it tests cases where the user has just hit ENTER somewhere
 * on the opening line of a block comment.
 *
 * @version $Id: ActionStartPrevLinePlusMultilinePreserveTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public class ActionStartPrevLinePlusMultilinePreserveTest extends IndentRulesTestCase {
  
  /** A factory method that constructs the specified instance of IndentRuleAction.  @see 
    * ActionStartPrevLinePlusMultilinePreserve#ActionStartPrevLinePlusMultilinePreserve(String[], int, int, int, int)
    */
  private IndentRuleAction makeAction(String[] suffices, int cursorLine, int cursorPos, int psrvLine, int psrvPos) {
    return new ActionStartPrevLinePlusMultilinePreserve(suffices, cursorLine, cursorPos, psrvLine, psrvPos);
  }
  
  /** This method abstracts the common behavior in subsequent tests.
    * @param start The text that should be in the document at time rule is called
    * @param loc the location of the cursor when rule is called
    * @param endLoc the expected final size of the document
    * @param finish the text string that remaining after the rule is called
    */
  public void helperCommentTest(String start, int loc, int endLoc, String finish) throws BadLocationException {
      _setDocText(start);
      _doc.setCurrentLocation(loc);
      makeAction(new String[]{" * \n", " */"},0,3,0,3).testIndentLine(_doc, Indenter.IndentReason.ENTER_KEY_PRESS);
      assertEquals(endLoc, _doc.getCurrentLocation());
      //assertEquals(finish.length(), _doc.getLength());
      assertEquals(finish, _doc.getText());
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
  
  public void test8() throws BadLocationException {
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

  public void test11() throws BadLocationException {
///** ABC **/ | /** ABC **/
//
//---------------------------------
///** ABC **/
//|/** ABC **/

    helperCommentTest("/** ABC **/ \n /** ABC **/",
                      13, 16,
                      "/** ABC **/ \n *  /** ABC **/\n */");
  }
  
}