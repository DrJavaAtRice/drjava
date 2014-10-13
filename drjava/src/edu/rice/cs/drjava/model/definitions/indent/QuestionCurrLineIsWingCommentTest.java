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

/** Tests visitor class that determines whether the current line is a wing comment.
 * 
 * @version $Id: QuestionCurrLineIsWingCommentTest.java 5594 2012-06-21 11:23:40Z rcartwright $
 */
public final class QuestionCurrLineIsWingCommentTest extends IndentRulesTestCase {

  static IndentRuleQuestion _rule = new QuestionCurrLineIsWingComment(null, null);

  public void testWingComment() throws javax.swing.text.BadLocationException {
    _setDocText("// This is a wing comment");
    assertTrue("A valid wing comment 1", _rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertTrue("A valid wing comment 2", _rule.testApplyRule(_doc, 4, Indenter.IndentReason.OTHER));
    assertTrue("A valid wing comment 3", _rule.testApplyRule(_doc, 10, Indenter.IndentReason.OTHER));
  }
  public void testSpaces() throws javax.swing.text.BadLocationException {
    _setDocText("/*\n \n*/");
    assertFalse("A block comment 1", _rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertFalse("A block comment 2", _rule.testApplyRule(_doc, 3, Indenter.IndentReason.OTHER));
    assertFalse("A block comment 3", _rule.testApplyRule(_doc, 5, Indenter.IndentReason.OTHER));
  }
  
  static String cornerCase = " //\n";
  
  public void testCornerCase() throws javax.swing.text.BadLocationException {
    _setDocText(cornerCase);
    assertFalse("Corner Case 1", _rule.testApplyRule(_doc, 0, Indenter.IndentReason.OTHER));
    assertFalse("Corner Case 2", _rule.testApplyRule(_doc, 1, Indenter.IndentReason.OTHER));
    assertFalse("Corner Case 3", _rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
    assertFalse("Corner Case 4", _rule.testApplyRule(_doc, 3, Indenter.IndentReason.OTHER));
  }
   
  public void testWingInsideBlock() throws javax.swing.text.BadLocationException {
    _setDocText("/*//\n \n */");
    assertFalse("Wing Inside BlockComment 1", _rule.testApplyRule(_doc, Indenter.IndentReason.OTHER));
    assertFalse("Wing Inside BlockComment 2", _rule.testApplyRule(_doc, 2, Indenter.IndentReason.OTHER));
  }
}
