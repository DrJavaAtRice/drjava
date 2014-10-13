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

/** Test the action rules for code in the indentation decision tree.
  * The implementation relies heavily on functions which are fully
  * tested in IndentHelpersTest.
  * @version $Id: ActionStartPrevStmtPlusTest.java 5675 2012-08-16 21:25:57Z rcartwright $
  */
public final class ActionStartPrevStmtPlusTest extends IndentRulesTestCase {
  
//  public void testNoPrevStmt() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartPrevStmtPlus(0, true);
//    IndentRuleAction rule2 = new ActionStartPrevStmtPlus(2, true);
//
//    _setDocText("foo();\n");
//    rule1.testIndentLine(_doc, 2, Indenter.IndentReason.OTHER);
//    assertEquals("no prev stmt, no suffix", "foo();\n", _doc.getText());
//    
//    _setDocText("foo();\n");
//    rule2.testIndentLine(_doc, 2, Indenter.IndentReason.OTHER);
//    assertEquals("no prev stmt, suffix two spaces", "  foo();\n", _doc.getText());
//  }
//  
//  public void testPrevStmtPrevLine() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartPrevStmtPlus(0, true);
//    IndentRuleAction rule2 = new ActionStartPrevStmtPlus(2, true);
//
//    _setDocText("  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\nbiz();\n");
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 44, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("prev stmt on prev line, no suffix",
//                 "  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\n  biz();\n",
//                 _doc.getText());
//    
//    _setDocText("  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\nbiz();\n");
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 44, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("prev stmt on prev line, suffix two spaces",
//                 "  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\n    biz();\n",
//                 _doc.getText());
//  }
//
//  public void testPrevStmtSeveralLinesBeforeCurrLocation() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartPrevStmtPlus(0, true);
//    IndentRuleAction rule2 = new ActionStartPrevStmtPlus(2, true);
//    
//    _setDocText("  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\nx;\n");
//    rule1.testIndentLine(_doc, 56, Indenter.IndentReason.OTHER);
//    assertEquals("prev stmt serveral lines before, no suffix",
//                 "  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\n  x;\n",
//                 _doc.getText());
//    
//    _setDocText("  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\nx;\n");
//    rule2.testIndentLine(_doc, 56, Indenter.IndentReason.OTHER);
//    assertEquals("prev stmt serveral lines before, suffix two spaces", 
//                 "  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\n    x;\n",
//                 _doc.getText());
//  }
//  
//  public void testColonNotDelim() throws BadLocationException {
//    IndentRuleAction rule = new ActionStartPrevStmtPlus(0, false);
//    
//    _setDocText("test2 = x ? y :\n" +     // ? and : on one line
//                "  z;\n" +     // unfinished ternary
//                "foo();\n");     // new stmt
//    rule.testIndentLine(_doc, 21, Indenter.IndentReason.OTHER);
//    assertEquals("Colon is not a delimiter",
//                 "test2 = x ? y :\n" +     // ? and : on one line
//                 "  z;\n" +     // unfinished ternary
//                 "foo();\n",
//                 _doc.getText());
//  }
//
//
//  public void testAfterArrayAssign() throws BadLocationException {
//    IndentRuleAction rule = new ActionStartPrevStmtPlus(0, false);
//    
//    _setDocText("a = {\n" +
//                "  b,c,d\n" + 
//                "};\n" +
//                "   a;");     // new stmt
//    //rule.indentLine(_doc, 8);
//    rule.testIndentLine(_doc, 17, Indenter.IndentReason.OTHER);
//    assertEquals("After array assignment",
//                 "a = {\n" + 
//                 "  b,c,d\n" +
//                 "};\n" +
//                 "a;",
//                 _doc.getText());
//  }
//  public void testAfterArrayAssignMultiSemi() throws BadLocationException {
//    IndentRuleAction rule = new ActionStartPrevStmtPlus(0, false);
//    
//    _setDocText("a = {\n" +
//                "  b,c,d\n" + 
//                "};;;\n" +
//                "   a;");     // new stmt
//    //rule.indentLine(_doc, 8);
//    rule.testIndentLine(_doc, 19, Indenter.IndentReason.OTHER);
//    assertEquals("After array assignment multi semi colons",
//                 "a = {\n" + 
//                 "  b,c,d\n" +
//                 "};;;\n" +
//                 "a;",
//                 _doc.getText());
//  }
//  
//  public void testAnonymousInnerClassAssign() throws BadLocationException {
//    IndentRuleAction rule = new ActionStartPrevStmtPlus(0, false);
//    
//    _setDocText("Runnable command = new Runnable() {\n" + 
//                "  public void run() { ... }\n" +
//                "};\n" +
//                "   command.run();");  // new stmt
//    rule.testIndentLine(_doc, 67, Indenter.IndentReason.OTHER);
//    assertEquals("After anonymous inner class assignment",
//                 "Runnable command = new Runnable() {\n" + 
//                "  public void run() { ... }\n" +
//                "};\n" +
//                "command.run();",
//                 _doc.getText());
//  }
//  
//  public void testAnonymousInnerClassArg() throws BadLocationException {
//    IndentRuleAction rule = new ActionStartPrevStmtPlus(0, false);
//    
//    _setDocText("setCommand(new Runnable() {\n" + 
//                "  public void run() { ... }\n" +
//                "});\n" +
//                "   command.run();");  // new stmt
//    rule.testIndentLine(_doc, 60, Indenter.IndentReason.OTHER);
//    assertEquals("After method call with anonymous inner class argument",
//                 "setCommand(new Runnable() {\n" + 
//                 "  public void run() { ... }\n" +
//                 "});\n" +
//                 "command.run();",
//                 _doc.getText());
//  } 

  /** 
   * not currently supported 
   * currently assuming single stmt per line
   */
  /*
  public void testAfterArrayAssignMultiSemiAndStmt() throws BadLocationException {
    IndentRuleAction rule = new ActionStartPrevStmtPlus("", false);
    
    _setDocText("a = {\n" +
                "  b,c,d\n" + 
                "};b;;\n" +
                "   a;");     // new stmt
    //rule.indentLine(_doc, 8);
    rule.indentLine(_doc, 20);
    assertEquals("After array assignment multi semi colons and embedded stmt",
                 "a = {\n" + 
                 "  b,c,d\n" +
                 "};b;;\n" +
                 "a;",
                 _doc.getText());
  }
  */
}



