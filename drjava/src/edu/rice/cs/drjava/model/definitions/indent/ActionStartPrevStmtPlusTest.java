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
 * The implementation relies heavily on functions which are fully
 * tested in IndentHelpersTest.
 *
 * @version $Id$
 */
public class ActionStartPrevStmtPlusTest extends IndentRulesTestCase {

  /**
   * Tests the indentation decision tree.
   * @param     String name
   */
  public ActionStartPrevStmtPlusTest(String name) {
    super(name);
  }
  
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



