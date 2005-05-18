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

import junit.framework.*;
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.AbstractDJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Test the action rules for code in the indentation decision tree.
 * Assumes cursor is within a brace.
 * @version $Id$
 */
public final class ActionStartStmtOfBracePlusTest extends IndentRulesTestCase {


  /**
   * Tests indenting with a single line contract
   */
  public void testSingleLineContract() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("   "); // 3 spaces
    
    String text = "public void foo() {\nbar();";
    String aligned1 = text;
    String aligned2 = "public void foo() {\n   bar();";
    
    _setDocText(text);
    rule1.indentLine(_doc, 20, Indenter.OTHER);
    assertEquals("single line contract, no indent, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 20, Indenter.OTHER);
    assertEquals("single line contract, no indent, with suffix", 
                 aligned2, _doc.getText());
  }
  
  /**
   * Tests indenting with an indented single line contract
   */
  public void testIndentedSingleLineContract() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("   "); // 3 spaces
    
    String text = "  y = new Foo() {\nbar();";
    String aligned1 = "  y = new Foo() {\n  bar();";
    String aligned2 = "  y = new Foo() {\n     bar();";
    
    _setDocText(text);
    rule1.indentLine(_doc, 20, Indenter.OTHER);
    assertEquals("single line contract, with indent, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 20, Indenter.OTHER);
    assertEquals("single line contract, with indent, with suffix", 
                 aligned2, _doc.getText());
  }
  
  /**
   * Tests indenting with a multiple line contract
   */
  public void testMultiLineContract() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
    
    String text = "    foobar();\n" +
                  "    int foo(int x,\n" +
                  "            int y) {\n" + 
                  "bar();";
    String aligned1 = "    foobar();\n" +
                      "    int foo(int x,\n" +
                      "            int y) {\n" + 
                      "    bar();";
    String aligned2 = "    foobar();\n" +
                      "    int foo(int x,\n" +
                      "            int y) {\n" + 
                      "      bar();";
    
    _setDocText(text);
    rule1.indentLine(_doc, 56, Indenter.OTHER);
    assertEquals("multi line contract, with indent, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 56, Indenter.OTHER);
    assertEquals("multi line contract, with indent, with suffix", 
                 aligned2, _doc.getText());
  }
  
  /**
   * Tests indenting a for statement (odd semicolons)
   */
  public void testForStatement() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("   "); // 3 spaces
    
    String text = "  for (int i=0; i<j; i++) {\nbar();";
    String aligned1 = "  for (int i=0; i<j; i++) {\n  bar();";
    String aligned2 = "  for (int i=0; i<j; i++) {\n     bar();";
    
    _setDocText(text);
    rule1.indentLine(_doc, 28, Indenter.OTHER);
    assertEquals("for statement, with indent, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 28, Indenter.OTHER);
    assertEquals("for statement, with indent, with suffix", 
                 aligned2, _doc.getText());
  }
  
  /**
   * Tests indenting a multiple line for statement (odd semicolons)
   */
  public void testMultiLineForStatement() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
    
    String text = "  for (int i=0;\n" +
                  "       i<j;\n" +
                  "       i++)\n" +
                  "  {\n" +
                  "bar();";
    String aligned1 = "  for (int i=0;\n" +
                      "       i<j;\n" +
                      "       i++)\n" +
                      "  {\n" +
                      "  bar();";
    String aligned2 = "  for (int i=0;\n" +
                      "       i<j;\n" +
                      "       i++)\n" +
                      "  {\n" +
                      "    bar();";
    
    _setDocText(text);
    rule1.indentLine(_doc, 44, Indenter.OTHER);
    assertEquals("multi-line for statement, with indent, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 44, Indenter.OTHER);
    assertEquals("multi-line for statement, with indent, with suffix", 
                 aligned2, _doc.getText());
  }
  
  /**
   * Tests indenting with nested braces
   * Note: multiple braces on a line are not yet supported.  This test
   * will be useful in a later version.
   *
  public void testNestedBraces() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
    
    String text = "  {  {  }\n" +
                  "    {\n" +
                  "       }  {\n" +
                  "foo();\n";
    String aligned1 = "  {  {  }\n" +
                      "    {\n" +
                      "       }  {\n" +
                      "    foo();\n";
    String aligned2 = "  {  {  }\n" +
                      "    {\n" +
                      "       }  {\n" +
                      "      foo();\n";
    
    _setDocText(text);
    rule1.indentLine(_doc, 28);
    assertEquals("nested braces, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 28);
    assertEquals("nested braces, with suffix", 
                 aligned2, _doc.getText());
   }*/
  
  /**
   * Tests indenting with commented delimiters
   */
  public void testCommentedBrace() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
    
    String text = "  void foo()\n" +
                  "  {\n" +
                  "      // {\n" +
                  "foo();\n";
    String aligned1 = "  void foo()\n" +
                      "  {\n" +
                      "      // {\n" +
                      "  foo();\n";
    String aligned2 = "  void foo()\n" +
                      "  {\n" +
                      "      // {\n" +
                      "    foo();\n";
    
    _setDocText(text);
    rule1.indentLine(_doc, 30, Indenter.OTHER);
    assertEquals("commented brace, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 30, Indenter.OTHER);
    assertEquals("commented brace, with suffix", 
                 aligned2, _doc.getText());
  }
  
  /**
   * Tests having start of line belong to a different brace
   * Note: multiple braces on a line are not yet supported.  This test
   * will be useful in a later version.
   *
  public void testStartOfLineInDifferentBrace() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
    
    String text = "  class Foo {\n" +
                  "    void bar() {\n" +
                  "      i=0; } void foo() {\n" +
                  "bar();\n";
    String aligned1 = "  class Foo {\n" +
                      "    void bar() {\n" +
                      "      i=0; } void foo() {\n" +
                      "    bar();\n";
    String aligned2 = "  class Foo {\n" +
                      "    void bar() {\n" +
                      "      i=0; } void foo() {\n" +
                      "      bar();\n";
    
    _setDocText(text);
    rule1.indentLine(_doc, 30);
    assertEquals("start in different brace, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 30);
    assertEquals("start in different brace, with suffix", 
                 aligned2, _doc.getText());
   }*/
  
  /**
   * Tests indenting without an enclosing brace
   */
  public void testNoBrace() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartStmtOfBracePlus("");
    IndentRuleAction rule2 = new ActionStartStmtOfBracePlus("  "); // 2 spaces
    
    String text = "package foo;\n" +
                  "import bar.*;\n";
    String aligned1 = "package foo;\n" +
                      "import bar.*;\n";
    String aligned2 = "package foo;\n" +
                      "  import bar.*;\n";
    
    _setDocText(text);
    rule1.indentLine(_doc, 13, Indenter.OTHER);
    assertEquals("no brace, no suffix", 
                 aligned1, _doc.getText());
    
    _setDocText(text);
    rule2.indentLine(_doc, 13, Indenter.OTHER);
    assertEquals("no brace, with suffix", 
                 aligned2, _doc.getText());
  }
}



