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
 * The implementation relies heavily on functions which are fully
 * tested in IndentHelpersTest.
 *
 * @version $Id$
 */
public final class ActionStartPrevStmtPlusTest extends IndentRulesTestCase {
  
  public void testNoPrevStmt() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartPrevStmtPlus("", true);
    IndentRuleAction rule2 = new ActionStartPrevStmtPlus("  ", true);

    _setDocText("foo();\n");
    rule1.indentLine(_doc, 2, Indenter.OTHER);
    assertEquals("no prev stmt, no suffix",
                 "foo();\n",
                 _doc.getText(0, _doc.getLength()));
    
    _setDocText("foo();\n");
    rule2.indentLine(_doc, 2, Indenter.OTHER);
    assertEquals("no prev stmt, suffix two spaces",
                 "  foo();\n",
                 _doc.getText(0, _doc.getLength()));
  }
  
  public void testPrevStmtPrevLine() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartPrevStmtPlus("", true);
    IndentRuleAction rule2 = new ActionStartPrevStmtPlus("  ", true);

    _setDocText("  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\nbiz();\n");
    rule1.indentLine(_doc, 44, Indenter.OTHER);
    assertEquals("prev stmt on prev line, no suffix",
                 "  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\n  biz();\n",
                 _doc.getText(0, _doc.getLength()));
    
    _setDocText("  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\nbiz();\n");
    rule2.indentLine(_doc, 44, Indenter.OTHER);
    assertEquals("prev stmt on prev line, suffix two spaces",
                 "  foo().\n//boo();\n/*y=x+1;\nfoo(){}*/\nbar();\n    biz();\n",
                 _doc.getText(0, _doc.getLength()));
  }

  public void testPrevStmtSeveralLinesBeforeCurrLocation() throws BadLocationException {
    IndentRuleAction rule1 = new ActionStartPrevStmtPlus("", true);
    IndentRuleAction rule2 = new ActionStartPrevStmtPlus("  ", true);
    
    _setDocText("  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\nx;\n");
    rule1.indentLine(_doc, 56, Indenter.OTHER);
    assertEquals("prev stmt serveral lines before, no suffix",
                 "  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\n  x;\n",
                 _doc.getText(0, _doc.getLength()));
    
    _setDocText("  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\nx;\n");
    rule2.indentLine(_doc, 56, Indenter.OTHER);
    assertEquals("prev stmt serveral lines before, suffix two spaces", 
                 "  foo();\n//y=x+1;\n/*void blah {\n}*/\n  ';' + blah.\n//foo\n    x;\n",
                 _doc.getText(0, _doc.getLength()));
  }
  
  public void testColonNotDelim() throws BadLocationException {
    IndentRuleAction rule = new ActionStartPrevStmtPlus("", false);
    
    _setDocText("test2 = x ? y :\n" +     // ? and : on one line
                "  z;\n" +     // unfinished ternary
                "foo();\n");     // new stmt
    rule.indentLine(_doc, 21, Indenter.OTHER);
    assertEquals("Colon is not a delimiter",
                 "test2 = x ? y :\n" +     // ? and : on one line
                 "  z;\n" +     // unfinished ternary
                 "foo();\n",
                 _doc.getText(0, _doc.getLength()));
  }


  public void testAfterArrayAssign() throws BadLocationException {
    IndentRuleAction rule = new ActionStartPrevStmtPlus("", false);
    
    _setDocText("a = {\n" +
                "  b,c,d\n" + 
                "};\n" +
                "   a;");     // new stmt
    //rule.indentLine(_doc, 8);
    rule.indentLine(_doc, 17, Indenter.OTHER);
    assertEquals("After array assignment",
                 "a = {\n" + 
                 "  b,c,d\n" +
                 "};\n" +
                 "a;",
                 _doc.getText(0, _doc.getLength()));
  }
  public void testAfterArrayAssignMultiSemi() throws BadLocationException {
    IndentRuleAction rule = new ActionStartPrevStmtPlus("", false);
    
    _setDocText("a = {\n" +
                "  b,c,d\n" + 
                "};;;\n" +
                "   a;");     // new stmt
    //rule.indentLine(_doc, 8);
    rule.indentLine(_doc, 19, Indenter.OTHER);
    assertEquals("After array assignment multi semi colons",
                 "a = {\n" + 
                 "  b,c,d\n" +
                 "};;;\n" +
                 "a;",
                 _doc.getText(0, _doc.getLength()));
  }

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
                 _doc.getText(0, _doc.getLength()));
  }
  */
}



