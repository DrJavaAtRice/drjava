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
 * Test the action rules for code in the indentation decision tree.
 * Assumes cursor is within a brace.
 * @version $Id: ActionStartStmtOfBracePlusTest.java 5675 2012-08-16 21:25:57Z rcartwright $
 */
public final class ActionStartStmtOfBracePlusTest extends IndentRulesTestCase {


//  /** Tests indenting with a single line contract. */
//  public void testSingleLineContract() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus(0);
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus(3); // 3 spaces
//    
//    String text = "public void foo() {\nbar();";
//    String aligned1 = text;
//    String aligned2 = "public void foo() {\n   bar();";
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 20, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("single line contract, no indent, no suffix", aligned1, _doc.getText());
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 20, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("single line contract, no indent, with suffix", aligned2, _doc.getText());
//  }
//  
//  /** Tests indenting with an indented single line contract. */
//  public void testIndentedSingleLineContract() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus(0);
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus(3); // 3 spaces
//    
//    String text = "  y = new Foo() {\nbar();";
//    String aligned1 = "  y = new Foo() {\n  bar();";
//    String aligned2 = "  y = new Foo() {\n     bar();";
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 20, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("single line contract, with indent, no suffix", 
//                 aligned1, _doc.getText());
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 20, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("single line contract, with indent, with suffix", 
//                 aligned2, _doc.getText());
//  }
//  
//  /** Tests indenting with a multiple line contract. */
//  public void testMultiLineContract() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus(0);
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus(2); // 2 spaces
//    
//    String text = "    foobar();\n" +
//                  "    int foo(int x,\n" +
//                  "            int y) {\n" + 
//                  "bar();";
//    String aligned1 = "    foobar();\n" +
//                      "    int foo(int x,\n" +
//                      "            int y) {\n" + 
//                      "    bar();";
//    String aligned2 = "    foobar();\n" +
//                      "    int foo(int x,\n" +
//                      "            int y) {\n" + 
//                      "      bar();";
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 56, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("multi line contract, with indent, no suffix", aligned1, _doc.getText());
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 56, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("multi line contract, with indent, with suffix", aligned2, _doc.getText());
//  }
//  
//  /** Tests indenting a for statement (odd semicolons) */
//  public void testForStatement() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus(0);
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus(3); // 3 spaces
//    
//    String text = "  for (int i=0; i<j; i++) {\nbar();";
//    String aligned1 = "  for (int i=0; i<j; i++) {\n  bar();";
//    String aligned2 = "  for (int i=0; i<j; i++) {\n     bar();";
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 28, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("for statement, with indent, no suffix", 
//                 aligned1, _doc.getText());
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 28, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("for statement, with indent, with suffix", 
//                 aligned2, _doc.getText());
//  }
//  
//  /** Tests indenting a multiple line for statement (odd semicolons) */
//  public void testMultiLineForStatement() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus(0);
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus(2); // 2 spaces
//    
//    String text = "  for (int i=0;\n" +
//                  "       i<j;\n" +
//                  "       i++)\n" +
//                  "  {\n" +
//                  "bar();";
//    String aligned1 = "  for (int i=0;\n" +
//                      "       i<j;\n" +
//                      "       i++)\n" +
//                      "  {\n" +
//                      "  bar();";
//    String aligned2 = "  for (int i=0;\n" +
//                      "       i<j;\n" +
//                      "       i++)\n" +
//                      "  {\n" +
//                      "    bar();";
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 44, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("multi-line for statement, with indent, no suffix", aligned1, _doc.getText());
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 44, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("multi-line for statement, with indent, with suffix", aligned2, _doc.getText());
//  }
//  
//  /** Tests indenting with nested braces
//   * Note: multiple braces on a line are not yet supported.  This test
//   * will be useful in a later version.
//   *
//  public void testNestedBraces() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
//    
//    String text = "  {  {  }\n" +
//                  "    {\n" +
//                  "       }  {\n" +
//                  "foo();\n";
//    String aligned1 = "  {  {  }\n" +
//                      "    {\n" +
//                      "       }  {\n" +
//                      "    foo();\n";
//    String aligned2 = "  {  {  }\n" +
//                      "    {\n" +
//                      "       }  {\n" +
//                      "      foo();\n";
//    
//    _setDocText(text);
//    rule1.indentLine(_doc, 28);
//    assertEquals("nested braces, no suffix", 
//                 aligned1, _doc.getText());
//    
//    _setDocText(text);
//    rule2.indentLine(_doc, 28);
//    assertEquals("nested braces, with suffix", 
//                 aligned2, _doc.getText());
//   }*/
//  
//  /** Tests indenting with commented delimiters. */
//  public void testCommentedBrace() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus(0);
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus(2); // 2 spaces
//    
//    String text = "  void foo()\n" +
//                  "  {\n" +
//                  "      // {\n" +
//                  "foo();\n";
//    String aligned1 = "  void foo()\n" +
//                      "  {\n" +
//                      "      // {\n" +
//                      "  foo();\n";
//    String aligned2 = "  void foo()\n" +
//                      "  {\n" +
//                      "      // {\n" +
//                      "    foo();\n";
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 30, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("commented brace, no suffix", aligned1, _doc.getText());
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 30, Indenter.IndentReason.OTHER);
////    System.err.println("Intended Document text is: " + _doc.getText());
//    assertEquals("commented brace, with suffix", aligned2, _doc.getText());
//  }
//  
//  /** Tests having start of line belong to a different brace
//   * Note: multiple braces on a line are not yet supported.  This test
//   * will be useful in a later version.
//   *
//  public void testStartOfLineInDifferentBrace() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
//    
//    String text = "  class Foo {\n" +
//                  "    void bar() {\n" +
//                  "      i=0; } void foo() {\n" +
//                  "bar();\n";
//    String aligned1 = "  class Foo {\n" +
//                      "    void bar() {\n" +
//                      "      i=0; } void foo() {\n" +
//                      "    bar();\n";
//    String aligned2 = "  class Foo {\n" +
//                      "    void bar() {\n" +
//                      "      i=0; } void foo() {\n" +
//                      "      bar();\n";
//    
//    _setDocText(text);
//    rule1.indentLine(_doc, 30);
//    assertEquals("start in different brace, no suffix", 
//                 aligned1, _doc.getText());
//    
//    _setDocText(text);
//    rule2.indentLine(_doc, 30);
//    assertEquals("start in different brace, with suffix", 
//                 aligned2, _doc.getText());
//   }*/
//  
//  /** Tests indenting without an enclosing brace. */
//  public void testNoBrace() throws BadLocationException {
//    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus(0);
//    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus(2); // 2 spaces
//    
//    String text = "package foo;\n" +
//                  "import bar.*;\n";
//    String aligned1 = "package foo;\n" +
//                      "import bar.*;\n";
//    String aligned2 = "package foo;\n" +
//                      "  import bar.*;\n";
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule1.testIndentLine(_doc, 13, Indenter.IndentReason.OTHER);
////    System.err.println("Intended Document text is: " + _doc.getText());
//    assertEquals("no brace, no suffix", aligned1, _doc.getText());
//    
//    _setDocText(text);
////    System.err.println("Raw Document text is: " + _doc.getText());
//    rule2.testIndentLine(_doc, 13, Indenter.IndentReason.OTHER);
////    System.err.println("Indented Document text is: " + _doc.getText());
//    assertEquals("no brace, with suffix", aligned2, _doc.getText());
//  }
}



