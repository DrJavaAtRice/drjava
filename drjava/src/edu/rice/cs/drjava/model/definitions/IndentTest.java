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

package  edu.rice.cs.drjava.model.definitions;

import  junit.framework.*;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Test the tab/enter/squiggly indenting functionality.
 * @version $Id$
 */
public class IndentTest extends TestCase {
  protected DefinitionsDocument doc;
  static String noBrace = IndentInfo.noBrace;
  static String openSquiggly = IndentInfo.openSquiggly;
  static String openParen = IndentInfo.openParen;
  static String openBracket = IndentInfo.openBracket;

  /**
   * put your documentation comment here
   * @param     String name
   */
  public IndentTest(String name) {
    super(name);
  }

  /**
   * put your documentation comment here
   */
  public void setUp() {
    doc = new DefinitionsDocument();
  }

  /**
   * put your documentation comment here
   * @return 
   */
  public static Test suite() {
    return  new TestSuite(IndentTest.class);
  }

 
  
  
  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoSquiggly() throws BadLocationException {
    //empty document
    BraceReduction rm = doc._reduced;
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, noBrace, -1, -1, -1);
    //single newline
    doc.insertString(0, "\n", null);
    _assertContents("\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, noBrace, -1, -1, 0);
    //single layer brace
    doc.insertString(0, "{\n\n", null);
    // {\n\n#\n
    _assertContents("{\n\n\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, -1, 3, 0);
    //another squiggly
    doc.insertString(3, "{\n\n", null);
    // {\n\n{\n\n#\n
    _assertContents("{\n\n{\n\n\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, 3, 3, 0);
    //brace with whitespace
    doc.insertString(6, "  {\n\n", null);
    // {\n\n{\n\n  {\n\n#\n
    _assertContents("{\n\n{\n\n  {\n\n\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, 5, 3, 0);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoParen() throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n(\n", null);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openParen, 2, 2, 0);
    // paren with stuff in front
    doc.insertString(1, "  helo ", null);
    doc.move(2);
    // \n  helo (\n#
    _assertContents("\n  helo (\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openParen, 9, 2, 0);
    //single layer brace
    doc.move(-1);
    doc.insertString(9, " (", null);
    doc.move(1);
    // \n  helo ( (\n#
    _assertContents("\n  helo ( (\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openParen, 11, 2, 0);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoBracket() throws BadLocationException {
    // just bracket
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n[\n", null);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openBracket, 2, 2, 0);
    // bracket with stuff in front
    doc.insertString(1, "  helo ", null);
    doc.move(2);
    // \n  helo (\n#
    _assertContents("\n  helo [\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openBracket, 9, 2, 0);
    //single layer brace
    doc.move(-1);
    doc.insertString(9, " [", null);
    doc.move(1);
    // \n  helo ( (\n#
    _assertContents("\n  helo [ [\n", doc);
    ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openBracket, 11, 2, 0);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoPrevNewline () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  {\nhello", null);
    // {\n  {\nhello#
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, 9, 7, 5);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEndOfBlockComment () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n /*\n hello\n */", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n /*\n hello\n */", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  */\nhello", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n  /*\n  hello\n  */\n  hello", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment3 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  grr*/\nhello", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n  /*\n  hello\n  grr*/\n  hello", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment4 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n /*\n  hello\n */ hello", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n /*\n  hello\n  */ hello", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment2 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  */ (\nhello", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n{\n  hello;\n  /*\n  hello\n  */ (\n      hello", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoBlockComments () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "(\n /*\n*\n", null);
    // (\n/*\n*#\n
    rm.move(-1);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openParen, -1, 7, 1);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoBlockComments2 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n(\n /*\n*\n", null);
    // \n(\n/*\n*#\n
    rm.move(-1);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openParen, 7, 7, 1);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoBlockComments3 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  /*\n*\n", null);
    // (\n/*\n*#\n
    rm.move(-1);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, -1, 8, 1);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInfoBlockComments4 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  /*\n*\n", null);
    // \n(\n/*\n*#\n
    rm.move(-1);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, 8, 8, 1);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSkippingBraces () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n   { ()}\n}", null);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, 12, 12, 1);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSkippingComments () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n   //{ ()\n}", null);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, 13, 13, 1);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSkippingCommentsBraceAtBeginning () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n   //{ ()}{", null);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, openSquiggly, -1, 13, 11);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testNothingToIndentOn () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "   //{ ()}{", null);
    IndentInfo ii = rm.getIndentInformation();
    _assertIndentInfo(ii, noBrace, -1, -1, -1);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testStartSimple () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "abcde", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("abcde", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testStartSpaceIndent () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "  abcde", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("abcde", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testStartBrace () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n {", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("public class temp \n{", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEndBrace () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{ \n  }", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("public class temp \n{ \n}", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testInsideClass () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{ \ntext here", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("public class temp \n{ \n  text here", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testInsideClassWithBraceSets () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{  ()\ntext here", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("public class temp \n{  ()\n  text here", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIgnoreBraceOnSameLine () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{  ()\n{text here", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("public class temp \n{  ()\n  {text here", doc);
  }

  /**
   * Not supported any more.
   *
  public void testLargerIndent () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n  {  ()\n { text here", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("public class temp \n  {  ()\n    { text here", doc);
  }*/

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testWeird () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("hello\n  ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testWierd2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("hello", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testMotion () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hes{\n{abcde", null);
    doc.insertString(11, "\n{", null);
    // hes{\n{abcde\n{#
    doc.move(-8);
    // hes{\n#{abcde\n{
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    // hes{\n  #{abcde\n{
    _assertContents("hes{\n  {abcde\n{", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testNextCharIsNewline () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hes{\n{abcde", null);
    doc.insertString(11, "\n{", null);
    // hes{\n{abcde\n{#
    doc.move(-2);
    // hes{\n{abcde#\n{
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    // hes{\n  {abcde#\n{
    _assertContents("hes{\n  {abcde\n{", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testFor () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "for(;;)\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("for(;;)\n  ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testFor2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  for(;;)\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("{\n  for(;;)\n    ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testOpenParen () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello(\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("hello(\n      ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testPrintString () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "Sys.out(\"hello\"\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("Sys.out(\"hello\"\n          ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testOpenBracket () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello[\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("hello[\n      ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSquigglyAlignment () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  }", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("{\n}", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSpaceBrace () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "   {\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("   {\n     ", doc);
  }

  /**
   * Cascading indent is not used anymore.
   *
  public void testOpenSquigglyCascade () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "if\n  if\n    if\n{", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("if\n  if\n    if\n    {", doc);
  }*/

  /**
   * Cascading indent is not used anymore.
   *
  public void testOpenSquigglyCascade2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  if\n    if\n      if\n{", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("{\n  if\n    if\n      if\n      {", doc);
  }*/

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEnter () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n\n", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEnter2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testNotRecognizeComments () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\nhello //bal;\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\nhello //bal;\n  ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testNotRecognizeComments2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\nhello; /*bal*/\n ", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\nhello; /*bal*/\n", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testBlockIndent () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello\n{\n{\n  {", null);
    doc.indentLines(8, 13);
    _assertContents("hello\n{\n  {\n    {", doc);
  }

  /**
   * Regression test for bug in drjava-20010802-1020:
   * Indent block on a file containing just "  x;\n  y;\n" would throw an
   * exception.
   * @exception BadLocationException
   */
  public void testBlockIndent2 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "  x;\n  y;\n", null);
    doc.indentLines(0, doc.getLength());
    _assertContents("x;\ny;\n", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInsideCommentBlock () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello\n{\n/*{\n{\n*/\nhehe", null);
    doc.indentLines(0, 21);
    _assertContents("hello\n{\n  /*{\n   {\n   */\n  hehe", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSecondLineProblem () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSecondLineProblem2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "a\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("a\n  ", doc);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSmallFileProblem () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n\n", null);
    doc.indentLines(doc.getCurrentLocation(), doc.getCurrentLocation());
    _assertContents("\n\n", doc);
  }
  
  private void _assertContents(String expected, Document document) 
    throws BadLocationException
  {
    assertEquals("document contents", expected, 
                 document.getText(0, document.getLength()));
  }

  private void _assertIndentInfo(IndentInfo ii, 
                                 String braceType,
                                 int distToNewline,
                                 int distToBrace,
                                 int distToPrevNewline) 
  {
    assertEquals("indent info: brace type",
                 braceType, ii.braceType);
    assertEquals("indent info: dist to new line", 
                 distToNewline, ii.distToNewline);
    assertEquals("indent info: dist to brace",
                 distToBrace, ii.distToBrace);
    assertEquals("indent info: dist to prev new line", 
                 distToPrevNewline, ii.distToPrevNewline);
  }
}



