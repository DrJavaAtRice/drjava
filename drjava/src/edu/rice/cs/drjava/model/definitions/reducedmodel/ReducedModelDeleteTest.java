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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * Test the delete functionality of the reduced model.
 * @version $Id: ReducedModelDeleteTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class ReducedModelDeleteTest extends BraceReductionTestCase
  implements ReducedModelStates
{
  /** Tests that line comments after code are indeted correctly.
   */
  public void testHalfLineComment() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.move(-2);
    assertEquals("#0.0", "//", model1.currentToken().getType());
    assertEquals("#0.1", 0, model1.absOffset());
    model1.delete(1);
    assertEquals("#1.0", "/", model1.currentToken().getType());
    assertEquals("#1.1", 0, model1.absOffset());
    model1.insertChar('/');
    model1.delete(1);           //This time delete the second slash
    assertEquals("#2.0", 1, model1.absOffset());
    model1.move(-1);
    assertEquals("#2.1", 0, model1.absOffset());
    assertEquals("#2.2", "/", model1.currentToken().getType());
    model1.delete(1);
    assertEquals("#3.0", 0, model1.absOffset());
  }

  /** Tests that inner gaps are deleted correctcly.
   */
  public void testInnerGapDelete() {
    insertGap(model1, 8);
    assertEquals("#0.0", 8, model1.absOffset());
    model1.move(-6);
    assertEquals("#0.0", 2, model1.absOffset());
    model1.delete(3);
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", 5, model1.currentToken().getSize());
    model1.move(3);
    assertEquals("#2.0", 5, model1.absOffset());
  }

  /** Tests reduced model behavioir for deleting text then merging two gaps.
   */
  public void testDeleteAndMergeTwoGaps() {
    insertGap(model1, 5);
    model1.insertChar('/');
    assertEquals("#1.0", 6, model1.absOffset());
    model1.insertChar('*');
    assertEquals("#2.0", 7, model1.absOffset());
    insertGap(model1, 6);
    assertEquals("#3.0", 13, model1.absOffset());
    model1.move(-9);
    assertEquals("#4.0", 4, model1.absOffset());
    assertTrue("#4.1", model1.currentToken().isGap());
    assertEquals("#4.2", 5, model1.currentToken().getSize());
    model1.move(2);
    assertEquals("#5.0", 6, model1.absOffset());
    assertEquals("#5.2", "/*", model1.currentToken().getType());
    model1.move(-2);
    model1.delete(5);
    assertEquals("#6.0", 4, model1.absOffset());
    assertTrue("#6.1", model1.currentToken().isGap());
    assertEquals("#6.2", 8, model1.currentToken().getSize());
  }

  public void testDeleteBlockCommentMakesLineComment() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    insertGap(model1, 2);
    assertEquals("#2.0", 4, model1.absOffset());
    assertEquals("#2.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#4.0", 6, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    insertGap(model1, 1);
    assertEquals("#5.0", 7, model1.absOffset());
    assertEquals("#5.1", FREE, model1.getStateAtCurrent());
    //  /*__*/_#
    model1.move(-6);
    assertEquals("#6.0", 1, model1.absOffset());
    model1.delete(4);
    assertEquals("#7.0", 1, model1.absOffset());
    assertEquals("#7.1", "//", model1.currentToken().getType());
    assertEquals("#7.3", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#7.0", 2, model1.absOffset());
    assertTrue("#7.1", model1.currentToken().isGap());
    assertEquals("#7.2", 1, model1.currentToken().getSize());
    assertEquals("#7.3", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
  }

  public void testLineCommentStealsBlockCommentSlash() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    insertGap(model1, 2);
    assertEquals("#1.0", 3, model1.absOffset());
    model1.insertChar('/');
    assertEquals("#2.0", 4, model1.absOffset());
    model1.insertChar('*');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-4);
    model1.delete(2);
    assertEquals("#4.0", 1, model1.absOffset());
    assertEquals("#4.1", "//", model1.currentToken().getType());
    assertEquals("#4.2", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#5.0", 2, model1.absOffset());
    assertEquals("#5.1", "*", model1.currentToken().getType());
    assertEquals("#5.2", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#5.3", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }

  public void testLineCommentStealsLineCommentSlash() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    insertGap(model1, 2);
    assertEquals("#1.0", 3, model1.absOffset());
    model1.insertChar('/');
    assertEquals("#2.0", 4, model1.absOffset());
    model1.insertChar('/');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.move(-4);
    model1.delete(2);
    assertEquals("#4.0", 1, model1.absOffset());
    assertEquals("#4.1", "//", model1.currentToken().getType());
    assertEquals("#4.2", FREE, model1.getStateAtCurrent());
    model1.move(1);
    assertEquals("#5.0", 2, model1.absOffset());
    assertEquals("#5.1", "/", model1.currentToken().getType());
    assertEquals("#5.2", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#5.3", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }

  public void testDeleteNewlineAndShadowBlockCommentStart() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#2.0", 3, model1.absOffset());
    assertEquals("#2.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('\n');
    assertEquals("#3.0", 4, model1.absOffset());
    assertEquals("#3.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#4.0", 5, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#5.0", 6, model1.absOffset());
    assertEquals("#5.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-3);
    assertEquals("#6.0", 3, model1.absOffset());
    model1.delete(1);
    assertEquals("#7.0", 3, model1.absOffset());
    assertEquals("#7.1", "/", model1.currentToken().getType());
    assertEquals("#7.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(-1);
    assertEquals("#8.0", 2, model1.absOffset());
    assertEquals("#8.1", "*", model1.currentToken().getType());
    assertEquals("#8.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(2);
    assertEquals("#9.0", 4, model1.absOffset());
    assertEquals("#9.1", "*", model1.currentToken().getType());
    assertEquals("#9.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }

  public void testBlockCommentStartEatsEnd() {
    model1.insertChar('/');
    assertEquals("#0.0", 1, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#1.0", 2, model1.absOffset());
    assertEquals("#1.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    insertGap(model1, 2);
    assertEquals("#2.0", 4, model1.absOffset());
    assertEquals("#2.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('*');
    assertEquals("#3.0", 5, model1.absOffset());
    assertEquals("#3.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.insertChar('/');
    assertEquals("#4.0", 6, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    model1.move(-5);
    assertEquals("#4.0", 1, model1.absOffset());
    assertEquals("#4.1", "/*", model1.currentToken().getType());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    model1.delete(3);
    assertEquals("#5.0", 1, model1.absOffset());
    assertEquals("#5.1", "/*", model1.currentToken().getType());
    model1.move(1);
    assertEquals("#6.0", 2, model1.absOffset());
    assertEquals("#6.1", "/", model1.currentToken().getType());
    assertEquals("#6.2", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("#6.3", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#7.0", 3, model1.absOffset());
    assertEquals("#7.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
  }

  public void deleteLineCommentSlashOpensBlockComment() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.insertChar('*');
    model1.insertChar('\n');
    insertGap(model1, 2);
    model1.insertChar('(');
    model1.insertChar('*');
    model1.insertChar('/');
    assertEquals("#0.0", 9, model1.absOffset());
    assertEquals("#0.1", FREE, model1.getStateAtCurrent());
    model1.move(-1);
    assertEquals("#1.0", 8, model1.absOffset());
    assertEquals("#1.1", FREE, model1.getStateAtCurrent());
    assertEquals("#1.2", "/", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#2.0", 7, model1.absOffset());
    assertEquals("#2.1", FREE, model1.getStateAtCurrent());
    assertEquals("#2.2", "*", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#3.0", 6, model1.absOffset());
    assertEquals("#3.1", FREE, model1.getStateAtCurrent());
    assertEquals("#3.2", "(", model1.currentToken().getType());
    model1.move(-2);
    assertEquals("#4.0", 4, model1.absOffset());
    assertEquals("#4.1", FREE, model1.getStateAtCurrent());
    assertTrue("#4.2", model1.currentToken().isGap());
    assertEquals("#4.3", 2, model1.currentToken().getSize());
    model1.move(-1);
    assertEquals("#5.0", 3, model1.absOffset());
    assertEquals("#5.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#5.2", "\n", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#6.0", 2, model1.absOffset());
    assertEquals("#6.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    assertEquals("#6.2", "*", model1.currentToken().getType());
    model1.move(-1);
    assertEquals("#7.0", 1, model1.absOffset());
    assertEquals("#7.1", FREE, model1.getStateAtCurrent());
    assertEquals("#7.2", "//", model1.currentToken().getType());
    model1.delete(-1);
    assertEquals("#8.0", 0, model1.absOffset());
    assertEquals("#8.1", FREE, model1.getStateAtCurrent());
    assertEquals("#8.2", "/*", model1.currentToken().getType());
    model1.move(7);
    assertEquals("#8.0", 7, model1.absOffset());
    assertEquals("#8.1", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    assertEquals("#8.2", "*/", model1.currentToken().getType());
  }

  public void testStartDeleteGap() {
    model1.insertChar('/');
    model1.insertChar('*');
    insertGap(model1, 2);
    model1.insertChar('*');
    model1.insertChar('/');
    model1.move(-4);
    model1.delete(2);
    assertEquals("#0.0", 2, model1.absOffset());
    assertEquals("#0.1", "*/", model1.currentToken().getType());
    assertEquals("#0.2", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-2);
    assertEquals("#1.0", 0, model1.absOffset());
    assertEquals("#1.1", "/*", model1.currentToken().getType());
    assertEquals("#1.2", FREE, model1.getStateAtCurrent());
  }

  public void testDeleteFreesBlockCommentEnd() {
    model1.insertChar('/');
    model1.insertChar('*');
    assertEquals("#0.0", 2, model1.absOffset());

    insertGap(model1, 2);
    assertEquals("#0.1", 4, model1.absOffset());

    model1.insertChar('*');
    model1.insertChar('/');
    assertEquals("#0.2", 6, model1.absOffset());

    model1.move(-6);
    assertEquals("#0.3", 0, model1.absOffset());

    model1.delete(4);
    assertEquals("#0.4", 0, model1.absOffset());
    assertEquals("#0.5", "*", model1.currentToken().getType());
    assertEquals("#0.6", FREE, model1.currentToken().getState());
    assertEquals("#0.7", FREE, model1.getStateAtCurrent());

    model1.move(1);
    assertEquals("#1.0", 1, model1.absOffset());
    assertEquals("#1.1", "/", model1.currentToken().getType());
    assertEquals("#1.2", FREE, model1.currentToken().getState());
    assertEquals("#1.3", FREE, model1.getStateAtCurrent());
  }

  public void testUnmaskBlockCommentedLineComment() {
    model1.insertChar('/');
    model1.insertChar('*');
    insertGap(model1, 2);
    model1.insertChar('/');
    model1.insertChar('/');
    model1.move(-1);
    assertEquals("#0.0", 5, model1.absOffset());
    assertEquals("#0.1", "/", model1.currentToken().getType());
    assertEquals("#0.2", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    assertEquals("#0.3", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-1);
    assertEquals("#0.0", 4, model1.absOffset());
    assertEquals("#0.1", "/", model1.currentToken().getType());
    assertEquals("#0.2", INSIDE_BLOCK_COMMENT, model1.currentToken().getState());
    assertEquals("#0.3", INSIDE_BLOCK_COMMENT, model1.getStateAtCurrent());
    model1.move(-4);
    model1.delete(4);
    assertEquals("#2.0", 0, model1.absOffset());
    assertEquals("#2.1", "//", model1.currentToken().getType());
    assertEquals("#2.2", FREE, model1.currentToken().getState());
    assertEquals("#2.3", FREE, model1.getStateAtCurrent());
    model1.move(2);
    assertEquals("#3.0", 2, model1.absOffset());
    assertEquals("#3.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
  }

  public void testCrazyDelete() {
    model1.insertChar('/');
    model1.insertChar('/');
    model1.insertChar('*');
    insertGap(model1, 2);
    model1.insertChar('\n');
    model1.insertChar('/');
    model1.insertChar('/');
    assertEquals("#0.0", 8, model1.absOffset());
    assertEquals("#0.1", INSIDE_LINE_COMMENT, model1.getStateAtCurrent());
    model1.move(-2);
    assertEquals("#1.0", 6, model1.absOffset());
    assertEquals("#1.1", FREE, model1.getStateAtCurrent());
    assertEquals("#1.2", "//", model1.currentToken().getType());
    assertEquals("#1.3", FREE, model1.currentToken().getState());
    model1.move(-4);
    model1.delete(4);
    assertEquals("#2.0", 2, model1.absOffset());
    assertEquals("#2.1", "/", model1.currentToken().getType());
    assertEquals("#2.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
    model1.move(1);
    assertEquals("#3.0", 3, model1.absOffset());
    assertEquals("#3.1", "/", model1.currentToken().getType());
    assertEquals("#3.2", INSIDE_LINE_COMMENT, model1.currentToken().getState());
  }
}
