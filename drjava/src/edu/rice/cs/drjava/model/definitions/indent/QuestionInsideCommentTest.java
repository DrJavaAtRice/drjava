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

/**
 * Tests whether start of line is within a multiline comment.
 *
 * @version $Id$
 */
public final class QuestionInsideCommentTest extends IndentRulesTestCase {

  static IndentRuleQuestion _rule = new QuestionInsideComment(null, null);
  
  public void setUp() {
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
    assertEquals(false, _rule.applyRule(_doc, 0, Indenter.OTHER));
  }
  public void testLineBeginsComment() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.applyRule(_doc, 3, Indenter.OTHER));
  }
  public void testFooLine() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.applyRule(_doc, 6, Indenter.OTHER));
  }
  public void testLineEndsComment() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.applyRule(_doc, 9, Indenter.OTHER));
  }
  public void testBarLine() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.applyRule(_doc, 13, Indenter.OTHER));
  }
  public void testSlashStarMidLineBefore() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.applyRule(_doc, 16, Indenter.OTHER));
  }
  public void testSlashStarMidLineAfter() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.applyRule(_doc, 24, Indenter.OTHER));
  }
  public void testCommentedOutSlashStar() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.applyRule(_doc, 30, Indenter.OTHER));
  }
  public void testStarSlashMidLineBefore() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.applyRule(_doc, 33, Indenter.OTHER));
  }
  public void testStarSlashMidLineAfter() throws javax.swing.text.BadLocationException {
    assertEquals(true, _rule.applyRule(_doc, 41, Indenter.OTHER));
  }
  public void testAfterCommentedOutSlashStar() throws javax.swing.text.BadLocationException {
    assertEquals(false, _rule.applyRule(_doc, 49, Indenter.OTHER));
  }
  
}
