/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.indent;

import junit.framework.*;
import junit.extensions.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Test the action rules for code in the indentation decision tree.
 * Assumes cursor is within a brace.
 * @version $Id$
 */
public class ActionStartStmtOfBracePlusTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   * @param     String name
   */
  public ActionStartStmtOfBracePlusTest(String name) {
    super(name);
  }


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
    rule1.indentLine(_doc, 20);
    assertEquals("single line contract, no indent, no suffix", 
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 20);
    assertEquals("single line contract, no indent, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
    rule1.indentLine(_doc, 20);
    assertEquals("single line contract, with indent, no suffix", 
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 20);
    assertEquals("single line contract, with indent, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
    rule1.indentLine(_doc, 56);
    assertEquals("multi line contract, with indent, no suffix", 
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 56);
    assertEquals("multi line contract, with indent, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
    rule1.indentLine(_doc, 28);
    assertEquals("for statement, with indent, no suffix", 
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 28);
    assertEquals("for statement, with indent, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
    rule1.indentLine(_doc, 44);
    assertEquals("multi-line for statement, with indent, no suffix", 
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 44);
    assertEquals("multi-line for statement, with indent, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 28);
    assertEquals("nested braces, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
    rule1.indentLine(_doc, 30);
    assertEquals("commented brace, no suffix", 
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 30);
    assertEquals("commented brace, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 30);
    assertEquals("start in different brace, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
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
    rule1.indentLine(_doc, 13);
    assertEquals("no brace, no suffix", 
                 aligned1, _doc.getText(0, _doc.getLength()));
    
    _setDocText(text);
    rule2.indentLine(_doc, 13);
    assertEquals("no brace, with suffix", 
                 aligned2, _doc.getText(0, _doc.getLength()));
  }
}



