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

package  edu.rice.cs.drjava.model.definitions;

import  junit.framework.*;
import  javax.swing.text.BadLocationException;
//import java.io.File;
//import java.io.FileReader;
//import java.io.BufferedReader;
//import java.io.IOException;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.model.DJDocument;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceInfo;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.definitions.indent.*;
import edu.rice.cs.drjava.model.GlobalEventNotifier;

//import edu.rice.cs.util.FileOps;
import edu.rice.cs.util.swing.Utilities;

import static edu.rice.cs.drjava.model.definitions.reducedmodel.BraceInfo.*;

/** Class that tests the tab/enter/curly indenting functionality.
  * @version $Id: IndentTest.java 5668 2012-08-15 04:58:30Z rcartwright $
  */
public final class IndentTest extends DrJavaTestCase {
  protected DefinitionsDocument _doc;
  
  private Integer indentLevel = Integer.valueOf(2);
  private GlobalEventNotifier _notifier;
  
  /** Standard constructor for IdentTest */
  public IndentTest(String name) { super(name); }
  
  /** Sets up the member bindings common to all tests. */
  public void setUp() throws Exception {
    super.setUp();
    DrJava.getConfig().resetToDefaults();
    _notifier = new GlobalEventNotifier();
    _doc = new DefinitionsDocument(_notifier);
    setConfigSetting(OptionConstants.INDENT_LEVEL, indentLevel);
  }
  
  /** Builds the suite of tests for Indent.class.
    * @return the suite.
    */
  public static Test suite() { return  new TestSuite(IndentTest.class); }
  
  /** Convenience method that performs _doc._indentLine in the event thread. */
  private void safeIndentLine(final Indenter.IndentReason reason) {
    Utilities.invokeAndWait(new Runnable() { public void run() { _doc._indentLine(reason); } });
  }
 
  /** Convenience method that performs _doc.indentLines in the event thread. */
  private void safeIndentLines(final int startSel, final int endSel) {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _doc.indentLines(startSel, endSel); 
      } 
    });
  }

  /** test for scala case match */  
  public void testScalaCaseMatch() throws BadLocationException {
    String text=
     "val type = FRUIT match {\n"+
     "     case APPLE => 1\n"+
     "   case BANANA => 2\n"+
     "     case OTHERFRUIT match {\n"+
     " case GRAPE => 3\n"+
     " }\n"+
     "}\n";
    String indented = 
     "val type = FRUIT match {\n"+
     "  case APPLE => 1\n"+
     "  case BANANA => 2\n"+
     "  case OTHERFRUIT match {\n"+
     "    case GRAPE => 3\n"+
     "  }\n"+
     "}\n";
         _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    safeIndentLines(9, _doc.getLength());
    _assertContents(indented, _doc);

   }
   /** test for scala class def
   * code borrowed from Rice comp 402 homework boolean simplfier
   */
  public void testScalaClassAndDef() throws BadLocationException{
    String text = 
     "class Parser(r: Reader) extends StreamTokenizer(r) {\n"+
     "  import StreamTokenizer.{TT_WORD => WORD, TT_EOF => EOF, TT_EOL => EOL}  \n"+
     "       def this(text: String) = this(new StringReader(text));\n"+
     "  def read(): BoolExp = {\n"+
     " var token:Int=nextToken() \n"+
     " }\n"+
     "}\n";

    String indented=
     "class Parser(r: Reader) extends StreamTokenizer(r) {\n"+
     "  import StreamTokenizer.{TT_WORD => WORD, TT_EOF => EOF, TT_EOL => EOL}  \n"+
     "  def this(text: String) = this(new StringReader(text));\n"+
     "  def read(): BoolExp = {\n"+
     "    var token:Int=nextToken() \n"+
     "  }\n"+
     "}\n";
_doc.insertString(0, text, null);
    _assertContents(text, _doc);
    safeIndentLines(9, _doc.getLength());
    _assertContents(indented, _doc);
 }







  /** Regression test for comment portion of indent tree. */
  public void testIndentComments() throws BadLocationException {
    String text =
      "  foo();\n" +
      "   // foo\n" +
      "/**\n" +
      "\n" +
      "* Comment\n" +
      "    * More comment\n" +
      "code;\n" +
      "* More comment\n" +
      "\n" +
      "*/\n" +
      "\n";
    
    String indented =
      "  foo();\n" +     // (skip this line)
      "  // foo\n" +     // align to start of statement
      "  /**\n" +     // start of statement
      "   * \n" +     // add a star after first line
      "   * Comment\n" +     // align to star
      "   * More comment\n" +     // align to star
      "   code;\n" +     // align commented code to stars
      "   * More comment\n" +     // align star after commented code
      "   * \n" +     // add a star after line with star
      "   */\n" +     // align star
      "  \n";     // align close comment to prev statement
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    safeIndentLines(9, _doc.getLength());
    _assertContents(indented, _doc);
  }
  
  /** Test case for SourceForge bug# 681203. */
  public void testMultiLineStarInsertFirstLine() throws BadLocationException {
    String text =
      "/**\n" +
      "comments here blah blah\n" +
      " */";
    
    String noStarAdded =
      "/**\n" +
      " comments here blah blah\n" +
      " */";
    
    String starAdded =
      "/**\n" +
      " * comments here blah blah\n" +
      " */";
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    _doc.gotoLine(2);
    /* First test that indentation caused not by an enter press inserts no star */
    safeIndentLine(Indenter.IndentReason.OTHER);
    _assertContents(noStarAdded, _doc);
    /* Now test that indentation caused by an enter press does insert a star */
    safeIndentLine(Indenter.IndentReason.ENTER_KEY_PRESS);
    _assertContents(starAdded, _doc);
  }
  
  /** Test case for SourceForge bug# 681203. */
  public void testMultiLineStarInsertLaterLine() throws BadLocationException {
    
    String text =
      "/**\n" +
      " * other comments\n" +
      "comments here blah blah\n" +
      " */";
    
    String noStarAdded =
      "/**\n" +
      " * other comments\n" +
      " comments here blah blah\n" +
      " */";
    
    String starAdded =
      "/**\n" +
      " * other comments\n" +
      " * comments here blah blah\n" +
      " */";
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    _doc.gotoLine(3);
    /* First test that indentation caused not by an enter press inserts no star */
    safeIndentLine(Indenter.IndentReason.OTHER);
    _assertContents(noStarAdded, _doc);
    /* Now test that indentation caused by an enter press does insert a star */
    safeIndentLine(Indenter.IndentReason.ENTER_KEY_PRESS);
    _assertContents(starAdded, _doc);
  }
  
  /** Regression test for paren phrases. */
  public void testIndentParenPhrases() throws BadLocationException {
    String text =
      "foo(i,\n" +
      "j.\n" +
      "bar().\n" +
      "// foo();\n" +
      "baz(),\n" +
      "cond1 ||\n" +
      "cond2);\n" +
      "i = myArray[x *\n" +
      "y.\n" +
      "foo() +\n" +
      "z\n" +
      "];\n";
    
    String indented =
      "foo(i,\n" +
      "    j.\n" +     // new paren phrase
      "      bar().\n" +     // not new paren phrase
      "// foo();\n" +     // not new
      "      baz(),\n" +     // not new (after comment)
      "    cond1 ||\n" +     // new
      "    cond2);\n" +     // new (after operator)
      "i = myArray[x *\n" +     // new statement
      "            y.\n" +     // new phrase
      "              foo() +\n" +     // not new phrase
      "            z\n" +     // new phrase
      "              ];\n";     // not new phrase (debatable)
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    safeIndentLines(0, _doc.getLength());
    _assertContents(indented, _doc);
  }
  
  /** Regression test for braces. */
  public void testIndentBraces() throws BadLocationException {
    String text =
      "{\n" +
      "class Foo\n" +
      "extends F {\n" +
      "int i;   \n" +
      "void foo() {\n" +
      "if (true) {\n" +
      "bar();\n" +
      "}\n" +
      "}\n" +
      "/* comment */ }\n" +
      "class Bar {\n" +
      "/* comment\n" +
      "*/ }\n" +
      "int i;\n" +
      "}\n";
    
    String indented =
      "{\n" +
      "  class Foo\n" +     // After open brace
      "  extends F {\n" +     // Not new statement
      "    int i;   \n" +     // After open brace
      "    void foo() {\n" +     // After statement
      "      if (true) {\n" +     // Nested brace
      "        bar();\n" +     // Nested brace
      "      }\n" +     // Close nested brace
      "    }\n" +     // Close nested brace
      "  /* comment */ }\n" +     // Close brace after comment
      "  class Bar {\n" +     // After close brace
      "    /* comment\n" +     // After open brace
      "     */ }\n" +      // In comment
      "  int i;\n" +     // After close brace
      "}\n";
    
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    safeIndentLines(0, _doc.getLength());
    _assertContents(indented, _doc);
  }
  
  /** Regression test for arrays. */
  public void testIndentArray() throws BadLocationException {
    String text =
      "int[2][] a ={\n" +
      "{\n"  +
      "1,\n" +
      "2,\n" +
      "3},\n" +
      "{\n" +
      "4,\n" +
      "5}\n" +
      "};\n";
    
    String indented =
      "int[2][] a ={\n" +
      "  {\n"  +
      "    1,\n" +
      "    2,\n" +
      "    3},\n" +
      "  {\n" +
      "    4,\n" +
      "    5}\n" +
      "};\n";
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    safeIndentLines(0, _doc.getLength());
    _assertContents(indented, _doc);
  }
  
  /** Regression test for common cases. */
  public void testIndentCommonCases() throws BadLocationException {
    String text =
      "int x;\n" +
      "      int y;\n" +
      "  class Foo\n" +
      "     extends F\n" +
      " {\n" +
      "   }";
    
    String indented =
      "int x;\n" +
      "int y;\n" +
      "class Foo\n" +
      "extends F\n" +
      "{\n" +
      "}";
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
    safeIndentLines(0, _doc.getLength());
    _assertContents(indented, _doc);
  }
  
  
  /** Tests getLineEnclosingBrace, getEnclosingBrace
    * @exception BadLocationException
    */
  public void testIndentInfoCurly() throws BadLocationException {
    //empty document
    _assertLineBraceInfo(-1, NONE);
    _assertBraceInfo(-1, NONE);
    //single newline
    _doc.insertString(0, "\n", null);
    _assertContents("\n", _doc);
    _assertLineBraceInfo(-1, NONE);
    _assertBraceInfo(-1, NONE);
    //single layer brace
    _doc.insertString(0, "{\n\n", null);
    // {\n\n#\n
    _assertContents("{\n\n\n", _doc);
    _assertLineBraceInfo(3, OPEN_CURLY);
    _assertBraceInfo(3, OPEN_CURLY);
    //another curly
    _doc.insertString(3, "{\n\n", null);
    // {\n\n{\n\n#\n
    _assertContents("{\n\n{\n\n\n", _doc);
    _assertLineBraceInfo(3, OPEN_CURLY);
    _assertBraceInfo(3, OPEN_CURLY);
    //brace with whitespace
    _doc.insertString(6, "  {\n\n", null);
    // {\n\n{\n\n  {\n\n#\n
    _assertContents("{\n\n{\n\n  {\n\n\n", _doc);
    _assertLineBraceInfo(3, OPEN_CURLY);
    _assertBraceInfo(3, OPEN_CURLY);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testIndentInfoParen() throws BadLocationException {
    // just paren
    _doc.insertString(0, "\n(\n", null);
    _assertLineBraceInfo(2, OPEN_PAREN);
    _assertBraceInfo(2, OPEN_PAREN);
    // paren with stuff in front
    _doc.insertString(1, "  helo ", null);
    _doc.move(2);
    // \n  helo (\n#
    _assertContents("\n  helo (\n", _doc);
    _assertLineBraceInfo(2, OPEN_PAREN);
    _assertBraceInfo(2, OPEN_PAREN);
    //single layer brace
    _doc.move(-1);
    _doc.insertString(9, " (", null);
    _doc.move(1);
    // \n  helo ( (\n#
    _assertContents("\n  helo ( (\n", _doc);
    _assertLineBraceInfo(2, OPEN_PAREN);
    _assertBraceInfo(2, OPEN_PAREN);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testIndentInfoBracket() throws BadLocationException {
    // just bracket
    _doc.insertString(0, "\n[\n", null);
    _assertLineBraceInfo(2, OPEN_BRACKET);
    _assertBraceInfo(2, OPEN_BRACKET);
    // bracket with stuff in front
    _doc.insertString(1, "  helo ", null);
    _doc.move(2);
    // \n  helo (\n#
    _assertContents("\n  helo [\n", _doc);
    _assertLineBraceInfo(2, OPEN_BRACKET);
    _assertBraceInfo(2, OPEN_BRACKET);
    //single layer brace
    _doc.move(-1);
    _doc.insertString(9, " [", null);
    _doc.move(1);
    // \n  helo [ [\n#
    _assertContents("\n  helo [ [\n", _doc);
    _assertLineBraceInfo(2, OPEN_BRACKET);
    _assertBraceInfo(2, OPEN_BRACKET);
  }
  
  /** Put your documentation comment here
    * @exception BadLocationException
    */
  public void testIndentInfoPrevNewline () throws BadLocationException {
//    System.err.println("***** reduced before insert = " + _doc.getReduced().simpleString());
    _doc.insertString(0, "{\n  {\nhello", null);
//    System.err.println("***** reduced after insert = " + _doc.getReduced().simpleString());
    // {\n  {\nhello#
//    System.err.println("***** text = " + _doc.getText() + "loc = " + _doc.getCurrentLocation() + " length = " + 
//    _doc.getLength());
    _assertLineBraceInfo(2, OPEN_CURLY);
    _assertBraceInfo(7, OPEN_CURLY);
  }
  
  /** Tests block comment indenting.
    * @exception BadLocationException
    */
  public void testEndOfBlockComment () throws BadLocationException {
    _doc.insertString(0, "\n{\n  hello;\n /*\n hello\n */", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n /*\n hello\n */", _doc);
  }
  
  /** Tests block comment indenting.
    * @exception BadLocationException
    */
  public void testAfterBlockComment () throws BadLocationException {
    _doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  */\nhello", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n  /*\n  hello\n  */\n  hello", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testAfterBlockComment3 () throws BadLocationException {
    _doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  grr*/\nhello", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n  /*\n  hello\n  grr*/\n  hello", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testAfterBlockComment4 () throws BadLocationException {
    _doc.insertString(0, "\n{\n  hello;\n /*\n  hello\n */ hello", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n /*\n  hello\n  */ hello", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testAfterBlockComment2 () throws BadLocationException {
    _doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  */ (\nhello", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n  /*\n  hello\n  */ (\n      hello", _doc);
  }
  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testIndentInfoBlockComments () throws BadLocationException {
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "(\n /*\n*\n", null);
//    // (\n/*\n*#\n
//    _reduced.move(-1);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, OPEN_PAREN, -1, 7, 1);
//  }
  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testIndentInfoBlockComments2 () throws BadLocationException {
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "\n(\n /*\n*\n", null);
//    // \n(\n/*\n*#\n
//    _reduced.move(-1);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, OPEN_PAREN, 7, 7, 1);
//  }
  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testIndentInfoBlockComments3 () throws BadLocationException {
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "{\n  /*\n*\n", null);
//    // (\n/*\n*#\n
//    _reduced.move(-1);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, OPEN_CURLY, -1, 8, 1);
//  }
  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testIndentInfoBlockComments4 () throws BadLocationException {
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "\n{\n  /*\n*\n", null);
//    // \n(\n/*\n*#\n
//    _reduced.move(-1);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, OPEN_CURLY, 8, 8, 1);
//  }
//  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testSkippingBraces () throws BadLocationException {
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "\n{\n   { ()}\n}", null);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, OPEN_CURLY, 12, 12, 1);
//  }
//  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testSkippingComments () throws BadLocationException {
//    // just paren
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "\n{\n   //{ ()\n}", null);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, OPEN_CURLY, 13, 13, 1);
//  }
//  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testSkippingCommentsBraceAtBeginning () throws BadLocationException {
//    // just paren
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "{\n   //{ ()}{", null);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, OPEN_CURLY, -1, 13, 11);
//  }
//  
//  /** put your documentation comment here
//    * @exception BadLocationException
//    */
//  public void testNothingToIndentOn () throws BadLocationException {
//    // just paren
//    BraceReduction _reduced = _doc.getReduced();
//    _doc.insertString(0, "   //{ ()}{", null);
//    IndentInfo info = _reduced.getIndentInformation();
//    _assertIndentInfo(info, NONE, -1, -1, -1);
//  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testStartSimple () throws BadLocationException {
    // just paren
    _doc.insertString(0, "abcde", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("abcde", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testStartSpaceIndent () throws BadLocationException {
    // just paren
    _doc.insertString(0, "  abcde", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("abcde", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testStartBrace () throws BadLocationException {
    // just paren
    _doc.insertString(0, "public class temp \n {", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("public class temp \n{", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testEndBrace () throws BadLocationException {
    // just paren
    _doc.insertString(0, "public class temp \n{ \n  }", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("public class temp \n{ \n}", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testInsideClass () throws BadLocationException {
    // just paren
    _doc.insertString(0, "public class temp \n{ \ntext here", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("public class temp \n{ \n  text here", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testInsideClassWithBraceSets () throws BadLocationException {
    // just paren
    _doc.insertString(0, "public class temp \n{  ()\ntext here", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("public class temp \n{  ()\n  text here", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testIgnoreBraceOnSameLine () throws BadLocationException {
    // just paren
    _doc.insertString(0, "public class temp \n{  ()\n{text here", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("public class temp \n{  ()\n  {text here", _doc);
  }
  
//  /** Not supported any more. */
//  public void testLargerIndent () throws BadLocationException {
//    // just paren
//    BraceReduction rm = doc.getReduced();
//    doc.insertString(0, "public class temp \n  {  ()\n { text here", null);
//    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
//    _assertContents("public class temp \n  {  ()\n    { text here", doc);
//  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testWeird () throws BadLocationException {
    // just paren
    _doc.insertString(0, "hello\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("hello\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testWierd2 () throws BadLocationException {
    // just paren
    _doc.insertString(0, "hello", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("hello", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testMotion () throws BadLocationException {
    // just paren
    _doc.insertString(0, "hes{\n{abcde", null);
    _doc.insertString(11, "\n{", null);
    // hes{\n{abcde\n{#
    _doc.move(-8);
    // hes{\n#{abcde\n{
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    // hes{\n  #{abcde\n{
    _assertContents("hes{\n  {abcde\n{", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testNextCharIsNewline () throws BadLocationException {
    // just paren
    _doc.insertString(0, "hes{\n{abcde", null);
    _doc.insertString(11, "\n{", null);
    // hes{\n{abcde\n{#
    _doc.move(-2);
    // hes{\n{abcde#\n{
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    // hes{\n  {abcde#\n{
    _assertContents("hes{\n  {abcde\n{", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testFor () throws BadLocationException {
    // just paren
    _doc.insertString(0, "for(;;)\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("for(;;)\n", _doc);
  }
  
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testOpenParen () throws BadLocationException {
    // just paren
    _doc.insertString(0, "hello(\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("hello(\n      ", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testPrintString () throws BadLocationException {
    // just paren
    _doc.insertString(0, "Sys.out(\"hello\"\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("Sys.out(\"hello\"\n          ", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testOpenBracket () throws BadLocationException {
    // just paren
    _doc.insertString(0, "hello[\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("hello[\n      ", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testCurlyAlignment () throws BadLocationException {
    // just paren
    _doc.insertString(0, "{\n  }", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("{\n}", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testSpaceBrace () throws BadLocationException {
    // just paren
    _doc.insertString(0, "   {\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("   {\n     ", _doc);
  }
  
  /** Cascading indent is not used anymore.
   *
   public void testOpenCurlyCascade () throws BadLocationException {
   // just paren
   BraceReduction rm = doc.getReduced();
   doc.insertString(0, "if\n  if\n    if\n{", null);
   doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
   _assertContents("if\n  if\n    if\n    {", doc);
   }*/
  
  /** Cascading indent is not used anymore.
   *
   public void testOpenCurlyCascade2 () throws BadLocationException {
   // just paren
   BraceReduction rm = doc.getReduced();
   doc.insertString(0, "{\n  if\n    if\n      if\n{", null);
   doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
   _assertContents("{\n  if\n    if\n      if\n      {", doc);
   }*/
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testEnter () throws BadLocationException {
    // just paren
    _doc.insertString(0, "\n\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testEnter2 () throws BadLocationException {
    // just paren
    _doc.insertString(0, "\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testNotRecognizeComments () throws BadLocationException {
    // just paren
    _doc.insertString(0, "\nhello //bal;\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\nhello //bal;\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testNotRecognizeComments2 () throws BadLocationException {
    // just paren
    _doc.insertString(0, "\nhello; /*bal*/\n ", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\nhello; /*bal*/\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testBlockIndent () throws BadLocationException {
    // just paren
    _doc.insertString(0, "hello\n{\n{\n  {", null);
    safeIndentLines(8, 13);
    _assertContents("hello\n{\n  {\n    {", _doc);
  }
  
  /** Regression test for bug in drjava-20010802-1020:
   * Indent block on a file containing just "  x;\n  y;\n" would throw an
   * exception.
   * @exception BadLocationException
   */
  public void testBlockIndent2 () throws BadLocationException {
    _doc.insertString(0, "  x;\n  y;\n", null);
    safeIndentLines(0, _doc.getLength());
    _assertContents("x;\ny;\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testIndentInsideCommentBlock () throws BadLocationException {
    _doc.insertString(0, "hello\n{\n/*{\n{\n*/\nhehe", null);
    safeIndentLines(0, 21);
    _assertContents("hello\n{\n  /*{\n   {\n   */\n  hehe", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testSecondLineProblem () throws BadLocationException {
    // just paren
    _doc.insertString(0, "\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testSecondLineProblem2 () throws BadLocationException {
    // just paren
    _doc.insertString(0, "a\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("a\n", _doc);
  }
  
  /** put your documentation comment here
    * @exception BadLocationException
    */
  public void testSmallFileProblem () throws BadLocationException {
    // just paren
    _doc.insertString(0, "\n\n", null);
    safeIndentLines(_doc.getCurrentLocation(), _doc.getCurrentLocation());
    _assertContents("\n\n", _doc);
  }
  
  /** Regression test for arrays.
   */
  public void testAnonymousInnerClass() throws BadLocationException {
    String text =
      "addWindowListener(new WindowAdapter() {\n" +
      "public void windowClosing(WindowEvent e) {\n" +
      "dispose();\n" +
      "}\n" +
      "void x() {\n" +
      "\n" +
      "}\n" +
      "\n" +
      "}\n" +
      ");\n" +
      "foo.bar();\n";
    String indented =
      "addWindowListener(new WindowAdapter() {\n" +
      "  public void windowClosing(WindowEvent e) {\n" +
      "    dispose();\n" +
      "  }\n" +
      "  void x() {\n" +
      "    \n" +
      "  }\n" +
      "  \n" +
      "}\n" +
      ");\n" +
      "foo.bar();\n";
    
    
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
//    System.err.println("Original text:\n" + text);
    
    safeIndentLines(0, _doc.getLength());
//    System.err.println("Indented text:\n" + doc.getText());
//    System.err.println("Correct text:\n" + indented);
    _assertContents(indented, _doc);
//    fail("Asserted failure");
  }
  
  
  public void testParenthesizedAnonymousInnerClass() throws BadLocationException {
    String text = "addActionListener(new ActionListener() {\n" +
      "public void actionPerformed(ActionEvent e) {\n" +
        "config.setSetting(LANGUAGE_LEVEL, edu.rice.cs.drjava.config.OptionConstants.FULL_JAVA);\n" +
      "}});\n" +
      "group.add(rbMenuItem);\n";
    String indented = "addActionListener(new ActionListener() {\n" +
      "  public void actionPerformed(ActionEvent e) {\n" +
       "    config.setSetting(LANGUAGE_LEVEL, edu.rice.cs.drjava.config.OptionConstants.FULL_JAVA);\n" +
      "  }});\n" +
      "group.add(rbMenuItem);\n";
    _doc.insertString(0, text, null);
    _assertContents(text, _doc);
//    System.err.println("Original text:\n" + text);
//    System.err.println("Indented text:\n" + indented);
    safeIndentLines(0, _doc.getLength());
//    System.err.println("Computed result:\n" + doc.getText());
//    fail("Asserted failure");
    _assertContents(indented, _doc);
  }
  
//  /** Regression test for Bug #627753.  Uncomment when it is fixed.
//   */
//  public void testNestedUnbracedFor() throws BadLocationException {
//    String text =
//      "for (int a =0; a < 5; a++)\n" +
//      "for (int b = 0; b < 5; b++) {\n" +
//      "System.out.println(a + b);";
//    String indented =
//      "for (int a =0; a < 5; a++)\n" +
//      "  for (int b = 0; b < 5; b++) {\n" +
//      "    System.out.println(a + b);";
//    doc.insertString(0, text, null);
//    _assertContents(text, doc);
//    System.err.println("Original text:\n" + text);
//    System.err.println("Indented text:\n" + indented);
//    doc.indentLines(0, doc.getLength());
//    _assertContents(indented, doc);
//    doc.remove(0,doc.getLength() - 1);
//
//    text =
//      "if (true)\n" +
//      "if (true)\n" +
//      "System.out.println(\"Hello\");";
//    indented =
//      "if (true)\n" +
//      "  if (true)\n" +
//      "    System.out.println(\"Hello\");";
//    doc.insertString(0, text, null);
//    _assertContents(text, doc);
//    doc.indentLines(0, doc.getLength());
//    _assertContents(indented, doc);
//    doc.remove(0,doc.getLength() - 1);
//
//    text =
//      "{\n" +
//      "while (a < 5)\n" +
//      "while (b < 5) {\n" +
//      "System.out.println(a + b);";
//    indented =
//      "{\n" +
//      "  while (a < 5)\n" +
//      "    while (b < 5) {\n" +
//      "      System.out.println(a + b);";
//    doc.insertString(0, text, null);
//    _assertContents(text, doc);
//    doc.indentLines(0, doc.getLength());
//    _assertContents(indented, doc);
//    doc.remove(0,doc.getLength() - 1);
//
//    text =
//      "while (a < 5)\n" +
//      "while (b < 5);\n" +
//      "System.out.println(a + b);";
//    indented =
//      "while (a < 5)\n" +
//      "  while (b < 5);\n" +
//      "System.out.println(a + b);";
//    doc.insertString(0, text, null);
//    _assertContents(text, doc);
//    doc.indentLines(0, doc.getLength());
//    _assertContents(indented, doc);
//    doc.remove(0,doc.getLength() - 1);
//
//    text =
//      "do\n" +
//      "do\n" +
//      "x=5;\n" +
//      "while(false);\n" +
//      "while(false);\n";
//    indented =
//      "do\n" +
//      "  do\n" +
//      "    x=5;\n" +
//      "  while(false);\n" +
//      "while(false);\n";
//    doc.insertString(0, text, null);
//    _assertContents(text, doc);
//    doc.indentLines(0, doc.getLength());
//    _assertContents(indented, doc);
//    doc.remove(0,doc.getLength() - 1);
//  }
  
  public void testLiveUpdateOfIndentLevel() throws BadLocationException {
    
    String text =
      "int[2][] a ={\n" +
      "{\n"  +
      "1,\n" +
      "2,\n" +
      "3},\n" +
      "{\n" +
      "4,\n" +
      "5}\n" +
      "};\n";
    
    String indentedBefore =
      "int[2][] a ={\n" +
      "  {\n"  +
      "    1,\n" +
      "    2,\n" +
      "    3},\n" +
      "  {\n" +
      "    4,\n" +
      "    5}\n" +
      "};\n";
    
    String indentedAfter =
      "int[2][] a ={\n" +
      "        {\n" +
      "                1,\n" +
      "                2,\n" +
      "                3},\n" +
      "        {\n" +
      "                4,\n" +
      "                5}\n" +
      "};\n";
    
    setDocText(_doc, text);
    
    _assertContents(text, _doc);
    safeIndentLines(0, _doc.getLength());
//    Utilities.clearEventQueue();
//    Utilities.clearEventQueue();
    _assertContents(indentedBefore, _doc);
//    System.err.println("Changing INDENT_LEVEL option constant to 8");
    setConfigSetting(OptionConstants.INDENT_LEVEL, 8);
    
//    Utilities.clearEventQueue();
//    Utilities.clearEventQueue();
//    System.err.println("level is " + DrJava.getConfig().getSetting(OptionConstants.INDENT_LEVEL));
//    System.err.println("doc = " + _doc);
    safeIndentLines(0, _doc.getLength());
        
//    Utilities.clearEventQueue();
//    Utilities.clearEventQueue();
//    System.err.println("Performing failing assertion");
    _assertContents(indentedAfter, _doc);
  }
  
//  Commented out because reference files are missing!
//  /** Tests a list of files when indented match their correct indentations */
//  public void testIndentationFromFile() throws IOException {
//    File directory = new File("testFiles");
//
//    File[] unindentedFiles = {new File(directory, "IndentSuccesses.indent")
//         /*, new File(directory, "IndentProblems.indent")*/};
//    File[] correctFiles = {new File(directory, "IndentSuccessesCorrect.indent")
//      /*, new File(directory, "IndentProblemsCorrect.indent")*/};
//
//    for (int x = 0; x < correctFiles.length; x++) {
//      _indentAndCompare(unindentedFiles[x], correctFiles[x]);
//    }
//
//    //We know the following test file should (currently) fail, so we assert that it will fail to check
//    //our _indentAndCompare(...) function
//    boolean threwAFE = false;
//    try {
//      _indentAndCompare(new File(directory, "IndentProblems.indent"),
//                        new File(directory, "IndentProblemsCorrect.indent"));
//    }
//    catch(AssertionFailedError afe) {
//      threwAFE = true;
//    }
//    if (!threwAFE) {
//      fail("_indentAndCompare should have failed for IndentProblems.indent");
//    }
//  }
  
  public void testIndentingCorrectLine() throws BadLocationException {
    String test1 = 
      "class A {\n" +
      "  int a = 5;\n" +
      "     }";
    
    String test1Correct =
      "class A {\n" +
      "  int a = 5;\n" +
      "}";
    
    String test2 = 
      "     {\n" +
      "  int a = 5;\n" +
      "  }\n";
    
    String test2Correct =
      "{\n" +
      "  int a = 5;\n" +
      "  }\n";
    
    _doc.insertString(0, test1, null);
    _assertContents(test1, _doc);
    _doc.setCurrentLocation(20);
    safeIndentLines(20,20);
    _assertContents(test1, _doc);
    
    _doc = new DefinitionsDocument(_notifier);
    
    _doc.insertString(0, test1, null);
    _assertContents(test1, _doc);
    safeIndentLines(28,28);
//    System.out.println("test1 = \n" + test1 + "\n length = " + test1.length());
//    System.out.println("test1Correct = \n" + test1Correct + " \n length = " + test1Correct.length());
//    System.out.println("doc = \n" + doc.getText() + "\n length = " + doc.getLength());
    _assertContents(test1Correct, _doc);
    
    _doc = new DefinitionsDocument(_notifier);
    
    _doc.insertString(0, test2, null);
    _assertContents(test2, _doc);
    _doc.setCurrentLocation(5);
    safeIndentLines(5,5);
    _assertContents(test2Correct, _doc);
  }
  
  /** Tests that annotations do not change the indent level of the lines following.
    * @throws BadLocationException
    */
  public void testAnnotationsAfterOpenCurly() throws BadLocationException {
    String textToIndent =
      "@Annotation\n" +
      "public class TestClass {\n" +
      "public TestClass() {}\n" +
      "\n" +
      "@Annotation(WithParens)\n" +
      "private int _classField = 42;\n" +
      "\n" +
      "@Override\n" +
      "public String toString() {\n" +
      "@LocalVariableAnnotation\n" +
      "String msg = \"hello\";\n" +
      "return msg;\n" +
      "}\n" +
      "\n" +
      "public int methodAfterAnnotation() {\n" +
      "return 0;\n" +
      "}\n" +
      "}\n" +
      "\n";
    String textIndented = 
      "@Annotation\n" +
      "public class TestClass {\n" +
      "  public TestClass() {}\n" +
      "  \n" +
      "  @Annotation(WithParens)\n" +
      "  private int _classField = 42;\n" +
      "  \n" +
      "  @Override\n" +
      "  public String toString() {\n" +
      "    @LocalVariableAnnotation\n" +
      "    String msg = \"hello\";\n" +
      "    return msg;\n" +
      "  }\n" +
      "  \n" +
      "  public int methodAfterAnnotation() {\n" +
      "    return 0;\n" +
      "  }\n" +
      "}\n" +
      "\n";
    
    _doc.insertString(0, textToIndent, null);
    _assertContents(textToIndent, _doc);
    safeIndentLines(0, _doc.getLength());
    _assertContents(textIndented, _doc);
  }
  
  /** Tests that annotations do not change the indent level of the lines following.
    * @throws BadLocationException
    */
  public void testAnnotationsAfterDefinition() throws BadLocationException {
    String textToIndent =
      "@Annotation\n" +
      "public class TestClass {\n" +
      "public TestClass() {}\n" +
      "\n" +
      "private int _classField = 0;\n" +
      "\n" +
      "@Annotation(WithParens)\n" +
      "private int _classField2 = 42;\n" +
      "\n" +
      "@Override\n" +
      "public String toString() {\n" +
      "@LocalVariableAnnotation\n" +
      "String msg = \"hello\";\n" +
      "return msg;\n" +
      "}\n" +
      "\n" +
      "public int methodAfterAnnotation() {\n" +
      "return 0;\n" +
      "}\n" +
      "}\n";
    String textIndented = 
      "@Annotation\n" +
      "public class TestClass {\n" +
      "  public TestClass() {}\n" +
      "  \n" +
      "  private int _classField = 0;\n" +
      "  \n" +
      "  @Annotation(WithParens)\n" +
      "  private int _classField2 = 42;\n" +
      "  \n" +
      "  @Override\n" +
      "  public String toString() {\n" +
      "    @LocalVariableAnnotation\n" +
      "    String msg = \"hello\";\n" +
      "    return msg;\n" +
      "  }\n" +
      "  \n" +
      "  public int methodAfterAnnotation() {\n" +
      "    return 0;\n" +
      "  }\n" +
      "}\n";
    
    _doc.insertString(0, textToIndent, null);
    _assertContents(textToIndent, _doc);
    safeIndentLines(0, _doc.getLength());
    _assertContents(textIndented, _doc);
  }
  
//  /** Tests that an if statment nested in a switch will be indented properly.  This test, as opposed to the previous
//    * one, does not have any code in that case.  Fails.  Need to add test for case (other keywords?) as special case.
//    * except the if statement
//    * @throws BadLocationException
//    */
//  public void testNestedIfInSwitch2() throws BadLocationException {
//    String text =
//      "switch(c) {\n" +
//      "case 2:\n" +
//      "break;\n" +
//      "case 3:\n" +
//      "if(owner.command() == ROLL_OVER) {\n" +
//      "dog.rollOver();\n" +
//      "}\n" +
//      "break;\n" +
//      "}\n";
//
//    String indented =
//      "switch(c) {\n" +
//      "  case 2:\n" +
//      "    break;\n" +
//      "  case 3:\n" +
//      "    if(owner.command() == ROLL_OVER) {\n" +
//      "      dog.rollOver();\n" +
//      "    }\n" +
//      "    break;\n" +
//      "}\n";
//
//    doc.insertString(0, text, null);
//    _assertContents(text, doc);
//    doc.indentLines(0, doc.getLength());
//    System.err.println(doc.getText());
//    System.err.println(indented);
//    _assertContents(indented, doc);
//  }
  
  private void _assertContents(String expected, DJDocument document) throws BadLocationException {
    assertEquals("document contents", expected, document.getText());
  }
  
  private void _assertLineBraceInfo(int distance, String braceType) {
    BraceInfo info = _doc._getLineEnclosingBrace();
//      System.err.println(info);
    assertEquals("line brace info: brace distance", distance, info.distance());
    assertEquals("line brace info: brace type", braceType, info.braceType());
  }
  
  private void _assertBraceInfo(int distance, String braceType) {
    BraceInfo info = _doc._getEnclosingBrace();
    assertEquals("line brace info: brace distance", distance, info.distance());
    assertEquals("line brace info: brace type", braceType, info.braceType());
  }
//  /** Copies fromFile to toFile, assuming both files exist. */
//  private void _copyFile(File fromFile, File toFile) throws IOException {
//    String text = FileOps.readFileAsString(fromFile);
//    FileOps.writeStringToFile(toFile, text);
//    String newText = FileOps.readFileAsString(toFile);
//    assertEquals("File copy verify", text, newText);
//  }
  
//  /** Indents one file, compares it to the other, reindents and recompares to make sure indent(x) = indent(indent(x))
//    */
//  private void _indentAndCompare(File unindented, File correct)
//    throws IOException
//  {
//    File test = null;
//    try {
//      test = File.createTempFile("test", ".scala");
//      _copyFile(unindented, test);
//      test.deleteOnExit();
//      IndentFiles.main(new String[] {"-silent", test.toString()});
//      _fileCompare(test, correct);
//      IndentFiles.main(new String[] {"-silent", test.toString()});
//      _fileCompare(test, correct);
//    }
//    finally {
//      if (test != null) {
//        test.delete();
//      }
//    }
//
//  }
  
//  /** @throws AssertionFailedError if the files are not identical */
//  private void _fileCompare(File test, File correct) throws IOException {
//    FileReader fr = new FileReader(correct);
//    FileReader fr2 = new FileReader(test);
//    BufferedReader correctBufferedReader = new BufferedReader(fr);
//    BufferedReader testBufferedReader = new BufferedReader(fr2);
//
//    String correctString = correctBufferedReader.readLine();
//    String testString = testBufferedReader.readLine();
//    int lineNo = 1;
//    while (correctString != null && testString != null) {
//      assertEquals("File: " + correct + " line: " + lineNo, correctString, testString);
//      correctString = correctBufferedReader.readLine();
//      testString = testBufferedReader.readLine();
//      lineNo++;
//    }
//    assertTrue("Indented file longer than expected", correctString == null);
//    assertTrue("Indented file shorter than expected", testString == null);
//
//    testBufferedReader.close();
//    correctBufferedReader.close();
//    fr.close();
//    fr2.close();
//  }
  
  
  public void testNoParameters() throws BadLocationException {
    //IndentRuleAction _action = new ActionBracePlus(0);
    
    String _text =
      "method(\n" + 
      ")\n";
    
    String _aligned =
      "method(\n" + 
      ")\n";
    
    _doc.insertString(0, _text, null);
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    safeIndentLines(0, 7); // Does nothing.
    assertEquals("START has no brace.", _text.length(), _doc.getLength());
    
    safeIndentLines(0, _doc.getLength()); // Aligns second line, a second time.
    
//    System.err.println("Indented Text is:\n" + doc.getText());
//    System.err.println("Correct Text is:\n" + _aligned);
    _assertContents(_aligned, _doc);
    assertEquals("Line aligned to open paren.", _aligned.length(), _doc.getLength());
  }
  
  
//  public void testArrayInit() throws BadLocationException {
//    IndentRuleAction _action = new ActionBracePlus("");
//
//    String _text =
//      "int[] ar = new int[] {\n" + 
//      "1,1,1,1,1,1,1,1,1 };";
//
//    String _aligned =
//      "int[] ar = new int[] {\n" + 
//      "                      1,1,1,1,1,1,1,1,1 };";
//
//    doc.insertString(0, _text, null);
//    _action.indentLine(doc, 0); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//    doc.indentLines(0, 7); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//
//    doc.indentLines(0, doc.getLength()); // Aligns second line, a second time.
//    System.out.println(doc.getText());
//    _assertContents(_aligned, doc);
//    assertEquals("Line aligned to open paren.", _aligned.length(), doc.getLength());
//  }
  
  
//  public void testArrayInitNewline() throws BadLocationException {
//    IndentRuleAction _action = new ActionBracePlus("");
//
//    String _text =
//      "int[] ar = new int[] { 1,1,1,\n" + 
//      "1,1,1,1,1,1 };";
//
//    String _aligned =
//      "int[] ar = new int[] { 1,1,1,\n" + 
//      "                       1,1,1,1,1,1 };";
//
//    doc.insertString(0, _text, null);
//    _action.indentLine(doc, 0); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//    doc.indentLines(0, 7); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//
//    doc.indentLines(0, doc.getLength()); // Aligns second line, a second time.
//    System.out.println(doc.getText());
//    _assertContents(_aligned, doc);
//    assertEquals("Line aligned to open paren.", _aligned.length(), doc.getLength());
//  }
  
  
//  public void testArrayInitBraceNewline() throws BadLocationException {
//    IndentRuleAction _action = new ActionBracePlus("");
//
//    String _text =
//      "int[] blah = new int[] {1, 2, 3\n" + 
//      "};";
//
//    String _aligned =
//      "int[] blah = new int[] {1, 2, 3\n" + 
//      "                        };";
//
//    doc.insertString(0, _text, null);
//    _action.indentLine(doc, 0); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//    doc.indentLines(0, 7); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//
//    doc.indentLines(0, doc.getLength()); // Aligns second line, a second time.
//    System.out.println(doc.getText());
//    _assertContents(_aligned, doc);
//    assertEquals("Line aligned to open paren.", _aligned.length(), doc.getLength());
//  }
  
  
//  public void testArrayInitAllNewline() throws BadLocationException {
//    IndentRuleAction _action = new ActionBracePlus("");
//
//    String _text =
//      "int[] blah = new int[]\n" + 
//      "{4, 5, 6};";
//
//    String _aligned =
//      "int[] blah = new int[]\n" + 
//      "  {4, 5, 6};";
//
//    doc.insertString(0, _text, null);
//    _action.indentLine(doc, 0); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//    doc.indentLines(0, 7); // Does nothing.
//    assertEquals("START has no brace.", _text.length(), doc.getLength());
//
//    doc.indentLines(0, doc.getLength()); // Aligns second line, a second time.
//    System.out.println(doc.getText());
//    _assertContents(_aligned, doc);
//    assertEquals("Line aligned to open paren.", _aligned.length(), doc.getLength());
//  }

  // test exhibiting bug 2870973: UnexpectedException when indenting with superfluous )
  public void testNoBalancedParens() throws BadLocationException {
    String _text =
      "public class Foo {\n" + 
      "  public void m() {\n" + 
      "                                         _junitLocationEnabledListener = new ConfigOptionListeners.\n" + 
      "                                           RequiresDrJavaRestartListener<Boolean>(this, \"Use External JUnit\"));\n" + 
      "                                         _junitLocationListener = new ConfigOptionListeners.\n" + 
      "                                           RequiresDrJavaRestartListener<File>(_configFrame, \"JUnit Location\"));\n" + 
      "                                         _rtConcJUnitLocationEnabledListener = new ConfigOptionListeners.\n" + 
      "                                           RequiresInteractionsRestartListener<Boolean>(_configFrame, \"Use ConcJUnit Runtime\"));\n" + 
      "                                         _rtConcJUnitLocationListener = new ConfigOptionListeners.\n" + 
      "                                           RequiresInteractionsRestartListener<File>(_configFrame, \"ConcJUnit Runtime Location\"));\n" + 
      "  }\n" + 
      "}\n";
    
    String _aligned =
      "public class Foo {\n" + 
      "  public void m() {\n" + 
      "    _junitLocationEnabledListener = new ConfigOptionListeners.\n" + 
      "    RequiresDrJavaRestartListener<Boolean>(this, \"Use External JUnit\"));\n" + 
      "                                         _junitLocationListener = new ConfigOptionListeners.\n" + 
      "                                         RequiresDrJavaRestartListener<File>(_configFrame, \"JUnit Location\"));\n" + 
      "                                         _rtConcJUnitLocationEnabledListener = new ConfigOptionListeners.\n" + 
      "                                         RequiresInteractionsRestartListener<Boolean>(_configFrame, \"Use ConcJUnit Runtime\"));\n" + 
      "                                         _rtConcJUnitLocationListener = new ConfigOptionListeners.\n" + 
      "                                         RequiresInteractionsRestartListener<File>(_configFrame, \"ConcJUnit Runtime Location\"));\n" + 
      "  }\n" + 
      "}\n";
    
    _doc.insertString(0, _text, null);
    assertEquals("Document does not have the right length.", _text.length(), _doc.getLength());
    safeIndentLines(0, _doc.getLength()); // Aligns second line, a second time.    
//    System.err.println("Indented Text is:\n" + doc.getText());
//    System.err.println("Correct Text is:\n" + _aligned);
    _assertContents(_aligned, _doc);
    assertEquals("Document does not have the right length after indent.", _aligned.length(), _doc.getLength());
  }
}
