/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2017, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

/** * Tests whether start of line is within a multiline comment.
 *
 * @version $Id$
 */
public final class QuestionInsideCommentTest extends IndentRulesTestCase {

  static IndentRuleQuestion _rule = new QuestionInsideComment(null, null);
  
  public void setUp() throws Exception {
    super.setUp();
    try {
      _setDocText("\n/*\nfoo\n*/\nbar\nfoo /* bar\n// /*\nfoo */ bar\n// /*\nblah");
      //           .  .    .   .    .   .       .      .   .       .         .
      // sample code:          expected result:
      //                       F
      //  /*                   F
      //  foo                  T
      //  */                   T
      //  bar                  F
      //  foo /* bar           F,F
      //  // /*                T
      //  foo */ bar           T,T
      //  // /*
      //  blah                 F
    } catch (javax.swing.text.BadLocationException ex) {
      throw new RuntimeException("Bad Location Exception");
    }
  }
      
  
  public void testDocStart() throws javax.swing.text.BadLocationException {      
    assertEquals(false, _rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
  }
  public void testLineBeginsComment() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.testApplyRule(_doc, 3, Indenter.IndentReason.OTHER));
  }
  public void testFooLine() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.testApplyRule(_doc, 6, Indenter.IndentReason.OTHER));
  }
  public void testLineEndsComment() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.testApplyRule(_doc, 9, Indenter.IndentReason.OTHER));
  }
  public void testBarLine() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.testApplyRule(_doc, 13, Indenter.IndentReason.OTHER));
  }
  public void testSlashStarMidLineBefore() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.testApplyRule(_doc, 16, Indenter.IndentReason.OTHER));
  }
  public void testSlashStarMidLineAfter() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.testApplyRule(_doc, 24, Indenter.IndentReason.OTHER));
  }
  public void testCommentedOutSlashStar() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.testApplyRule(_doc, 30, Indenter.IndentReason.OTHER));
  }
  public void testStarSlashMidLineBefore() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.testApplyRule(_doc, 33, Indenter.IndentReason.OTHER));
  }
  public void testStarSlashMidLineAfter() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.testApplyRule(_doc, 41, Indenter.IndentReason.OTHER));
  }
  public void testAfterCommentedOutSlashStar() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.testApplyRule(_doc, 49, Indenter.IndentReason.OTHER));
  }
  
}
