package  edu.rice.cs.drjava;

import  junit.framework.*;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;


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
    assertEquals("1.0", noBrace, ii.braceType);
    assertEquals("1.1", -1, ii.distToNewline);
    assertEquals("1.2", -1, ii.distToBrace);
    assertEquals("1.3", -1, ii.distToPrevNewline);
    //single newline
    doc.insertString(0, "\n", null);
    assertEquals("0.1", "\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("2.0", noBrace, ii.braceType);
    assertEquals("2.1", -1, ii.distToNewline);
    assertEquals("2.2", -1, ii.distToBrace);
    assertEquals("2.3", 0, ii.distToPrevNewline);
    //single layer brace
    doc.insertString(0, "{\n\n", null);
    // {\n\n#\n
    assertEquals("0.2", "{\n\n\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("3.0", openSquiggly, ii.braceType);
    assertEquals("3.2", 3, ii.distToBrace);
    assertEquals("3.1", -1, ii.distToNewline);
    assertEquals("3.3", 0, ii.distToPrevNewline);
    //another squiggly
    doc.insertString(3, "{\n\n", null);
    // {\n\n{\n\n#\n
    assertEquals("0.3", "{\n\n{\n\n\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("4.0", openSquiggly, ii.braceType);
    assertEquals("4.1", 3, ii.distToNewline);
    assertEquals("4.2", 3, ii.distToBrace);
    assertEquals("4.3", 0, ii.distToPrevNewline);
    //brace with whitespace
    doc.insertString(6, "  {\n\n", null);
    // {\n\n{\n\n  {\n\n#\n
    assertEquals("0.4", "{\n\n{\n\n  {\n\n\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("5.0", openSquiggly, ii.braceType);
    assertEquals("5.1", 5, ii.distToNewline);
    assertEquals("5.2", 3, ii.distToBrace);
    assertEquals("5.3", 0, ii.distToPrevNewline);
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
    assertEquals("1.0", openParen, ii.braceType);
    assertEquals("1.2", 2, ii.distToBrace);
    assertEquals("1.1", 2, ii.distToNewline);
    assertEquals("1.3", 0, ii.distToPrevNewline);
    // paren with stuff in front
    doc.insertString(1, "  helo ", null);
    doc.move(2);
    // \n  helo (\n#
    assertEquals("0.1", "\n  helo (\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("2.0", openParen, ii.braceType);
    assertEquals("2.1", 9, ii.distToNewline);
    assertEquals("2.2", 2, ii.distToBrace);
    //single layer brace
    doc.move(-1);
    doc.insertString(9, " (", null);
    doc.move(1);
    // \n  helo ( (\n#
    assertEquals("0.2", "\n  helo ( (\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("3.0", openParen, ii.braceType);
    assertEquals("3.1", 11, ii.distToNewline);
    assertEquals("3.2", 2, ii.distToBrace);
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
    assertEquals("1.0", openBracket, ii.braceType);
    assertEquals("1.1", 2, ii.distToNewline);
    assertEquals("1.2", 2, ii.distToBrace);
    // bracket with stuff in front
    doc.insertString(1, "  helo ", null);
    doc.move(2);
    // \n  helo (\n#
    assertEquals("0.1", "\n  helo [\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("2.0", openBracket, ii.braceType);
    assertEquals("2.1", 9, ii.distToNewline);
    assertEquals("2.2", 2, ii.distToBrace);
    //single layer brace
    doc.move(-1);
    doc.insertString(9, " [", null);
    doc.move(1);
    // \n  helo ( (\n#
    assertEquals("0.2", "\n  helo [ [\n", doc.getText(0, doc.getLength()));
    ii = rm.getIndentInformation();
    assertEquals("3.0", openBracket, ii.braceType);
    assertEquals("3.1", 11, ii.distToNewline);
    assertEquals("3.2", 2, ii.distToBrace);
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
    assertEquals("1.0", openSquiggly, ii.braceType);
    assertEquals("1.1", 9, ii.distToNewline);
    assertEquals("1.2", 7, ii.distToBrace);
    assertEquals("1.2", 5, ii.distToPrevNewline);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEndOfBlockComment () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n /*\n hello\n */", null);
    doc.indentLine();
    assertEquals("0.1", "\n{\n  hello;\n /*\n hello\n  */", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  */\nhello", null);
    doc.indentLine();
    assertEquals("0.1", "\n{\n  hello;\n  /*\n  hello\n  */\n  hello", doc.getText(0, 
        doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment3 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  grr*/\nhello", null);
    doc.indentLine();
    assertEquals("0.1", "\n{\n  hello;\n  /*\n  hello\n  grr*/\n  hello", doc.getText(0, 
        doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment4 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n /*\n  hello\n */ hello", null);
    doc.indentLine();
    assertEquals("0.1", "\n{\n  hello;\n /*\n  hello\n  */ hello", doc.getText(0, 
        doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testAfterBlockComment2 () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n  hello;\n  /*\n  hello\n  */ (\nhello", null);
    doc.indentLine();
    assertEquals("0.1", "\n{\n  hello;\n  /*\n  hello\n  */ (\n      hello", doc.getText(0, 
        doc.getLength()));
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
    assertEquals("1.0", openParen, ii.braceType);
    assertEquals("1.1", -1, ii.distToNewline);
    assertEquals("1.2", 7, ii.distToBrace);
    assertEquals("1.2", 1, ii.distToPrevNewline);
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
    assertEquals("1.0", openParen, ii.braceType);
    assertEquals("1.1", 7, ii.distToNewline);
    assertEquals("1.2", 7, ii.distToBrace);
    assertEquals("1.2", 1, ii.distToPrevNewline);
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
    assertEquals("1.0", openSquiggly, ii.braceType);
    assertEquals("1.1", -1, ii.distToNewline);
    assertEquals("1.2", 8, ii.distToBrace);
    assertEquals("1.2", 1, ii.distToPrevNewline);
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
    assertEquals("1.0", openSquiggly, ii.braceType);
    assertEquals("1.1", 8, ii.distToNewline);
    assertEquals("1.2", 8, ii.distToBrace);
    assertEquals("1.2", 1, ii.distToPrevNewline);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSkippingBraces () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n{\n   { ()}\n}", null);
    IndentInfo ii = rm.getIndentInformation();
    assertEquals("1.0", openSquiggly, ii.braceType);
    assertEquals("1.2", 12, ii.distToBrace);
    assertEquals("1.1", 12, ii.distToNewline);
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
    assertEquals("1.0", openSquiggly, ii.braceType);
    assertEquals("1.2", 13, ii.distToBrace);
    assertEquals("1.1", 13, ii.distToNewline);
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
    assertEquals("1.0", openSquiggly, ii.braceType);
    assertEquals("1.2", 13, ii.distToBrace);
    assertEquals("1.1", -1, ii.distToNewline);
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
    assertEquals("1.0", noBrace, ii.braceType);
    assertEquals("1.2", -1, ii.distToBrace);
    assertEquals("1.1", -1, ii.distToNewline);
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testStartSimple () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "abcde", null);
    doc.indentLine();
    assertEquals("0.1", "abcde", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testStartSpaceIndent () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "  abcde", null);
    doc.indentLine();
    assertEquals("0.1", "abcde", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testStartBrace () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n {", null);
    doc.indentLine();
    assertEquals("0.1", "public class temp \n{", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEndBrace () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{ \n  }", null);
    doc.indentLine();
    assertEquals("0.1", "public class temp \n{ \n}", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testInsideClass () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{ \ntext here", null);
    doc.indentLine();
    assertEquals("0.1", "public class temp \n{ \n  text here", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testInsideClassWithBraceSets () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{  ()\ntext here", null);
    doc.indentLine();
    assertEquals("0.1", "public class temp \n{  ()\n  text here", doc.getText(0, 
        doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIgnoreBraceOnSameLine () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n{  ()\n{text here", null);
    doc.indentLine();
    assertEquals("0.1", "public class temp \n{  ()\n  {text here", doc.getText(0, 
        doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testLargerIndent () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "public class temp \n  {  ()\n { text here", null);
    doc.indentLine();
    assertEquals("0.1", "public class temp \n  {  ()\n    { text here", doc.getText(0, 
        doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testWierd () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello\n", null);
    doc.indentLine();
    assertEquals("0.1", "hello\n  ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testWierd2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello", null);
    doc.indentLine();
    assertEquals("0.1", "hello", doc.getText(0, doc.getLength()));
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
    doc.indentLine();
    // hes{\n  #{abcde\n{
    assertEquals("0.1", "hes{\n  {abcde\n{", doc.getText(0, doc.getLength()));
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
    doc.indentLine();
    // hes{\n  {abcde#\n{
    assertEquals("0.1", "hes{\n  {abcde\n{", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testFor () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "for(;;)\n", null);
    doc.indentLine();
    assertEquals("0.1", "for(;;)\n  ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testFor2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  for(;;)\n", null);
    doc.indentLine();
    assertEquals("0.1", "{\n  for(;;)\n    ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testOpenParen () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello(\n", null);
    doc.indentLine();
    assertEquals("0.1", "hello(\n      ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testPrintString () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "Sys.out(\"hello\"\n", null);
    doc.indentLine();
    assertEquals("0.1", "Sys.out(\"hello\"\n        ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testOpenBracket () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello[\n", null);
    doc.indentLine();
    assertEquals("0.1", "hello[\n      ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSquigglyAlignment () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  }", null);
    doc.indentLine();
    assertEquals("0.1", "{\n}", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSpaceBrace () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "   {\n", null);
    doc.indentLine();
    assertEquals("0.1", "   {\n     ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testOpenSquigglyCascade () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "if\n  if\n    if\n{", null);
    doc.indentLine();
    assertEquals("0.1", "if\n  if\n    if\n    {", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testOpenSquigglyCascade2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "{\n  if\n    if\n      if\n{", null);
    doc.indentLine();
    assertEquals("0.1", "{\n  if\n    if\n      if\n      {", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEnter () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n\n", null);
    doc.indentLine();
    assertEquals("0.1", "\n\n", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testEnter2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n", null);
    doc.indentLine();
    assertEquals("0.1", "\n", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testNotRecognizeComments () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\nhello //bal;\n", null);
    doc.indentLine();
    assertEquals("0.1", "\nhello //bal;\n  ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testNotRecognizeComments2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\nhello; /*bal*/\n ", null);
    doc.indentLine();
    assertEquals("0.1", "\nhello; /*bal*/\n", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testBlockIndent () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello\n{\n{\n  {", null);
    doc.indentBlock(8, 13);
    assertEquals("0.1", "hello\n{\n  {\n    {", doc.getText(0, doc.getLength()));
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
    doc.indentBlock(0, doc.getLength());
    assertEquals("text after indent", "x;\ny;\n", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testIndentInsideCommentBlock () throws BadLocationException {
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "hello\n{\n/*{\n{\n*/\nhehe", null);
    doc.indentBlock(0, 21);
    assertEquals("0.1", "hello\n{\n  /*{\n  {\n  */\n  hehe", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSecondLineProblem () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n", null);
    doc.indentLine();
    assertEquals("0.1", "\n", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSecondLineProblem2 () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "a\n", null);
    doc.indentLine();
    assertEquals("0.1", "a\n  ", doc.getText(0, doc.getLength()));
  }

  /**
   * put your documentation comment here
   * @exception BadLocationException
   */
  public void testSmallFileProblem () throws BadLocationException {
    // just paren
    BraceReduction rm = doc._reduced;
    doc.insertString(0, "\n\n", null);
    doc.indentLine();
    assertEquals("0.1", "\n\n", doc.getText(0, doc.getLength()));
  }
}



