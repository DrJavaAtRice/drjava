/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

/** Tests whether the previous line starts the comment containing the cursor.
  * @version $Id$
  */
public final class QuestionPrevLineStartsCommentTest extends IndentRulesTestCase {

  static IndentRuleQuestion rule2 = new QuestionPrevLineStartsComment(null,
              null);
  private static String example1 = "/*\nfoo\nbar\n*/";
  //                                    .    .
  // /* 
  // foo
  // bar
  // */
  private static String example2 = "foo /* bar\nblah\nmoo\n*/";
  //                                            .     .
  // foo /* bar
  // blah
  // moo
  // */
  private static String example3 = "/*\nfoo\n// /*\nbar\n*/";
  //                                    .    .      .
  // /*
  // foo
  // // /*
  // bar
  // */

  public void testSimpleFirstLine() throws javax.swing.text.BadLocationException {
    _setDocText(example1);
    assertEquals(true, rule2.applyRule(_doc, 3, Indenter.IndentReason.OTHER));
  }
  
  public void testSimpleSecondLine() throws javax.swing.text.BadLocationException {
    _setDocText(example1);
    assertEquals(false, rule2.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
  }
  
  public void testSlashStarMidLineFirstLine() throws javax.swing.text.BadLocationException {
    _setDocText(example2);
    assertEquals(true, rule2.applyRule(_doc, 11, Indenter.IndentReason.OTHER));
  }
  public void testSlashStarMidLineSecondLine() throws javax.swing.text.BadLocationException {
    _setDocText(example2);
    assertEquals(false, rule2.applyRule(_doc, 16, Indenter.IndentReason.OTHER));
  }
  public void testCommentedOutSlashStarBefore() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    assertEquals(true, rule2.applyRule(_doc, 3, Indenter.IndentReason.OTHER));
  }
  public void testCommentedOutSlashStarAt() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    assertEquals(false, rule2.applyRule(_doc, 7, Indenter.IndentReason.OTHER));
  }
  public void testCommentedOutSlashStarAfter() throws javax.swing.text.BadLocationException {
    _setDocText(example3);
    assertEquals(false, rule2.applyRule(_doc, 13, Indenter.IndentReason.OTHER));
  }
}


