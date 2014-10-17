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

/** Tests whether the previous line starts the block comment containing the cursor.
  * @version $Id: QuestionPrevLineStartsCommentTest.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public final class QuestionPrevLineStartsCommentTest extends IndentRulesTestCase {

  static IndentRuleQuestion rule2 = new QuestionPrevLineStartsComment(null,
              null);
  private static String example1 = "/*\nfoo\nbar\n*/";
  
  // /* 
  // foo
  // bar
  // */
  private static String example2 = "foo /* bar\nblah\nmoo\n*/";
  
  // foo /* bar
  // blah
  // moo
  // */
  private static String example3 = "/*\nfoo\n// /*\nbar\n*/";
  
  // /*
  // foo
  // // /*
  // bar
  // */

  public void testSimpleFirstLine() throws javax.swing.text.BadLocationException {
    _setDocText(example1);
    assertTrue("prev line starts block comment", rule2.testApplyRule(_doc, 3, Indenter.IndentReason.OTHER));
  }
  
  public void testSimpleSecondLine() throws javax.swing.text.BadLocationException {
    _setDocText(example1);
    assertFalse("prev line is inside block comment", rule2.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
  }
  
  public void testSlashStarMidLineFirstLine() throws javax.swing.text.BadLocationException {
    _setDocText(example2);
    assertEquals(true, rule2.testApplyRule(_doc, 11, Indenter.IndentReason.OTHER));
  }
  public void testSlashStarMidLineSecondLine() throws javax.swing.text.BadLocationException {
    _setDocText(example2);
    assertEquals(false, rule2.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
  }
  public void testCommentedOutSlashStarBefore() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    assertEquals(true, rule2.testApplyRule(_doc, 3, Indenter.IndentReason.OTHER));
  }
  public void testCommentedOutSlashStarAt() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    assertEquals(false, rule2.testApplyRule(_doc, 7, Indenter.IndentReason.OTHER));
  }
  public void testCommentedOutSlashStarAfter() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    assertEquals(false, rule2.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
  }
}


