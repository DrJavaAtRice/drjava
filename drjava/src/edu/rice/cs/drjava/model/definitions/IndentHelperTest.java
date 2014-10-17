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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.BadLocationException;
import edu.rice.cs.drjava.model.definitions.indent.IndentRulesTestCase;
import edu.rice.cs.util.swing.Utilities;

/** Tests for the helper methods in DefinitionsDocument
  * @version $Id: IndentHelperTest.java 5751 2013-02-06 10:32:04Z rcartwright $
  */
public final class IndentHelperTest extends IndentRulesTestCase {
  
  /** Convenience method that calls _doc.findPrevDelimiter. It formerly wrapped this call in a read lock. */
  private int findPrevDelimiter(int pos, char[] delimiters) throws BadLocationException {
    return _doc.findPrevDelimiter(pos, delimiters); 
  }
  
  /** Convenience method that calls _doc._inParenPhrase. It formerly wrapped this call in a read lock. */
  private boolean inParenPhrase(int pos) throws BadLocationException {
    return _doc._inParenPhrase(pos); 
  }
  
  /** Convenience method that calls _doc._getIndentOfStmt.  It formerly wrapped this call in a read lock. */
  private int getIndentOfCurrStmt(int pos) throws BadLocationException {
    return _doc._getIndentOfStmt(pos);
  }
  
  /** Convenience method that calls _doc._getIndentOfLine. It formerly wrapped this call in a read lock. */
  private int getIndentOfLine(int pos) throws BadLocationException {
    return _doc._getIndentOfLine(pos); 
  }
  
  /** Convenience method that calls _doc._getLineStartPos. It formerly wrapped this call in a read lock. */
  private int getLineStartPos(int pos) throws BadLocationException {
    return _doc._getLineStartPos(pos); 
  }
  
  /** Convenience method that calls _doc._getLineEndPos. It formerly wrapped this call in a read lock.*/
  private int getLineEndPos(int pos) throws BadLocationException {
    return _doc._getLineEndPos(pos); 
  }
  
  /** Convenience method that calls _doc._getLineFirstCharPos. It formerly wrapped this call in a read lock.*/
  private int getLineFirstCharPos(int pos) throws BadLocationException {
    return _doc._getLineFirstCharPos(pos); 
  }
  
    /** Convenience method that calls _doc.getFirstNonWSCharPos. It formerly wrapped this call in a read lock.*/
  private int getFirstNonWSCharPos(int pos) throws BadLocationException {
    return _doc.getFirstNonWSCharPos(pos); 
  }
  
  /** Convenience method that performs _doc.indentLines in the event thread. */
  private void safeIndentLines(final int startSel, final int endSel) {
    Utilities.invokeAndWait(new Runnable() { 
      public void run() {
        _doc.indentLines(startSel, endSel); 
      } 
    });
  }
  
  /** Convenience method that performs _doc.indentLines in the event thread. */
  private void indentDoc() { safeIndentLines(0, _doc.getLength()); }
  
  /** Tests findPrevDelimiter() */
  public void testFindPrevDelimiter() throws BadLocationException {
    char[] delimiters1 = {':', ';', '?'};

    // Used to test for delimiters not in test string
    char[] delimiters2 = {'%'};

    // Used to test if delimiter right after 0 is found
    char[] delimiters3 = {'f'};

    // Used to test finding delimiters that can be confused with comments
    char[] delimiters4 = {'*','/'};
    
    _setDocText("/*bar;\nfoo();\nx;*/\nfoo;\n");
    assertEquals("Check that delimiters in block comments are ignored",
                 -1,
                 findPrevDelimiter(22, delimiters1));
    
    _setDocText("foo();\n//bar();\nbiz();\n");
    assertEquals("Check that delimiters in wing comments are ignored",
                 5,
                 findPrevDelimiter(16, delimiters1));
    
    _setDocText("x=';'\n");
    assertEquals("Check that delimiters in single-quotes are ignored",
                 -1,
                 findPrevDelimiter(5, delimiters1));
    
    _setDocText("x=\";\"\n");
    assertEquals("Check that delimiters in double-quotes are ignored",
                 -1,
                 findPrevDelimiter(5, delimiters1));
    
    _setDocText("foo();\nfor(;;)\n");
    assertEquals("Check that delimiters in paren phrases are usually ignored",
                 5,
                 findPrevDelimiter(14, delimiters1));
    
// Commented out because this behavior is undesirable!  Preceding chars enclosed in parens should not be visible!    
//    _setDocText("foo();\nfor(;;)\n");
//    assertEquals("Check that delimiters in paren phrases " +
//                 "can be detected",
//                 12,
//                 _doc.findPrevDelimiter(14, delimiters1, false));
    
    _setDocText("foo();\n test ? x : y;\n\t    blah();\n");
    assertEquals("Check that ERROR_INDEX (-1) is returned if no matching character is found", 
                 -1, 
                 findPrevDelimiter(20, delimiters2)); 
    assertEquals("Check that delimiter is found if it is located at position 0",
                 0,
                 findPrevDelimiter(20, delimiters3));
    assertEquals("Check that delimiter is not found if it is at cursor's position",
                 -1,
                 findPrevDelimiter(5, delimiters1));
    assertEquals("Check that the first delimiter in the list is found",
                 17,
                 findPrevDelimiter(19, delimiters1));
    assertEquals("Check that the second delimiter in the list is found",
                 13,
                 findPrevDelimiter(17, delimiters1));
    assertEquals("Check that the last delimiter in the list is found",
                 5,
                 findPrevDelimiter(13, delimiters1));
    
    _setDocText("foo *\n" + "// comment\n" + "bar\n");
    assertEquals("Check that findprevDelimiter ignores comments even when delimiters include * and / (1)",
                 4,
                 findPrevDelimiter(17, delimiters4));
    _setDocText("foo /\n" + "/* comment */\n" + "bar\n");
    assertEquals("Check that findprevDelimiter ignores comments even when delimiters include * and / (2)",
                 4,
                 findPrevDelimiter(17, delimiters4));

    _setDocText("abcdefghijk");
    _doc.setCurrentLocation(3);
    int reducedModelPos = _doc.getReduced().absOffset();
    findPrevDelimiter(8, delimiters2);
    assertEquals("Check that position in reduced model is unaffected after call to findPrevDelimiter",
                 reducedModelPos,
                 _doc.getReduced().absOffset());
    
  }


  public void testPosInParenPhrase() throws BadLocationException {

      _setDocText("(;)");
      assertEquals("';' in parent phrase", true, inParenPhrase(1));
      
      _setDocText("abcdefghijk");
      _doc.setCurrentLocation(3);
      int reducedModelPos = _doc.getReduced().absOffset();
      inParenPhrase(8);
      assertEquals("Check that position in reduced model is unaffected after call to posInParenPhrase",
                   reducedModelPos,
                   _doc.getReduced().absOffset());   
    }


  public void testGetIndentOfCurrStmtDelimiters() throws BadLocationException {

    _setDocText("foo();\n");
    assertEquals("prev delimiter 0, no indent", 0, getIndentOfCurrStmt(3));
    _setDocText("  foo();\n");
    assertEquals("prev delimiter 0, indent two spaces", 2, getIndentOfCurrStmt(7));
    
    _setDocText("bar();\nfoo();\n");
    assertEquals("prev delimiter ';', no indent", 0, getIndentOfCurrStmt(7));
    _setDocText("  bar();\n  foo();\n");
    assertEquals("prev delimiter ';', indent two spaces", 2, getIndentOfCurrStmt(9));
    
    _setDocText("void bar()\n{\nfoo();\n");
    assertEquals("prev delimiter '{', no indent", 0, getIndentOfCurrStmt(13));
    _setDocText("void bar()\n{\n  foo();\n");
    assertEquals("prev delimiter '{', indent two spaces", 2, getIndentOfCurrStmt(13));
    
    _setDocText("}\nfoo();\n");
    assertEquals("prev delimiter '}', no indent", 0, getIndentOfCurrStmt(2));
    _setDocText("}\n  foo();\n");
    assertEquals("prev delimiter '}', indent two spaces", 2, getIndentOfCurrStmt(2));
  }

  public void testGetIndentOfCurrStmtDelimiterSameLine() 
    throws BadLocationException {
    
      _setDocText("bar(); foo();\n");
      assertEquals("prev delimiter on same line, no indent", 0, getIndentOfCurrStmt(6));
      
      _setDocText("  bar(); foo();\n");
      assertEquals("prev delimiter on same line, indent two spaces", 2, getIndentOfCurrStmt(8));
  }

  /** Test indenting of multiple line statement.  DrScala indents two more spaces at the beginning of each continuation
    * line.  Comment text is indents like program text but it is ignored when determined the indentation of the next 
    * line.  Is this the right convention? */
  public void testGetIndentOfCurrStmtMultipleLines()
    throws BadLocationException {

    String text = 
      "oogabooga()\n" +
      "bar().\n" +      // indent of this = 0
      "    bump().\n" +   // indent of this line = 2
      "    //comment\n" +  // indent of this line should be 4
      "    /*commment\n" +
      "     *again;{}\n" +
      "     */\n" +
      "     foo();\n";

    _setDocText(text);
    indentDoc();
    text = _getDocText();
//    System.err.println("text = \n" + text);
    assertEquals("implict semicolon on previous line, indent zero spaces", 0, getIndentOfLine(12));
//    System.err.println("text less 12 chars = \n" + text.substring(12));
//    System.err.println("text less 19 chars = \n" + text.substring(19));
    assertEquals("nested method call should add two spaces to indent", 2, getIndentOfLine(19));
//    System.err.println("text less 29 chars = \n" + text.substring(29));
    assertEquals("single line comment on third line should be indented 4 spaces", 4, getIndentOfLine(29));
//    System.err.println("text less 43 chars = \n" + text.substring(43));
    assertEquals("block comment on fourth line should be indented 4 spaces", 4, getIndentOfLine(43));
    
//    System.err.println("text less 58 chars = \n" + text.substring(58));  
    assertEquals("second line in block comment on fifth line should be indented 5 spaces ", 5, getIndentOfLine(58));
//    System.err.println("text less 73 chars = \n" + text.substring(73));
    assertEquals("last line in block comment should be indented 5 spaces", 5, getIndentOfLine(73));
//    System.err.println("text less 81 chars = \n" + text.substring(81));
    assertEquals("method call on foo should be indented 4 spaces", 4, getIndentOfLine(81));
  }

//  public void testGetIndentOfCurrStmtIgnoreDelimsInParenPhrase() throws BadLocationException {
//    
//    String text = "  bar.\n (;)\nfoo();";
//    
//    _setDocText(text);
//    indentDoc();
//    text = _getDocText();
//    System.err.println("text = \n" + text);
//    System.err.println("text less 11 chars = \n" + text.substring(11));
//    assertEquals("ignores delimiter in paren phrase", 2, getIndentOfLine(11));
//  }

//  public void testGetIndentOfCurrStmtEndOfDoc() throws BadLocationException {
//    _setDocText("foo.\n");
//    indentDoc();
//    assertEquals("cursor at end of document, no indent", 0, getIndentOfLine(5));
//  }
//
//  public void testGetLineStartPos() throws BadLocationException {
//    _setDocText("foo\nbar\nblah");
//    assertEquals("Returns position after the previous newline", 4, getLineStartPos(6));
//    assertEquals("Returns position after previous newline when cursor is at the position after the previous newline",
//                 4,
//                 getLineStartPos(4));
//    assertEquals("Returns 0 when there's no previous newline", 0, getLineStartPos(2));
//    assertEquals("Returns 0 when the cursor is at 0", 0, getLineStartPos(0));
//    
//    _setDocText("abcdefghijk");
//    _doc.setCurrentLocation(3);
//    int reducedModelPos = _doc.getReduced().absOffset();
//    getLineStartPos(5);
//    assertEquals("Check that position in reduced model is unaffected after call to getLineStartPos",
//                 reducedModelPos,
//                 _doc.getReduced().absOffset());
//  }
//
//  public void testGetLineEndPos() throws BadLocationException {
//    _setDocText("foo\nbar\nblah");
//    assertEquals("Returns position before the next newline",
//                 7,
//                 getLineEndPos(5));
//    assertEquals("Returns position before the next newline when cursor " +
//                 "is at the position before the next newline",
//                 7,
//                 getLineEndPos(7));
//    assertEquals("Returns the end of the document when there's no next newline",
//                 12,
//                 getLineEndPos(9));
//    
//    _setDocText("abcdefghijk");
//    _doc.setCurrentLocation(3);
//    int reducedModelPos = _doc.getReduced().absOffset();
//    getLineEndPos(5);
//    assertEquals("Check that position in reduced model is unaffected " +
//                 "after call to getLineEndPos",
//                 reducedModelPos,
//                 _doc.getReduced().absOffset());
//  }
//
//  public void testGetLineFirstCharPos() throws BadLocationException {
//    _setDocText("   ");
//    assertEquals("Returns the end of the document if the line " +
//                 "is the last line in the document and has no " +
//                 "non-whitespace characters",
//                 3,
//                 getLineFirstCharPos(1));
//    
//    _setDocText("   \nfoo();\n");
//    assertEquals("Returns the next newline if there are " +
//                 "no non-whitespace characters on the line",
//                 3,
//                 getLineFirstCharPos(1));
//    
//    _setDocText("foo();\n   \t  bar();\nbiz()\n");
//    assertEquals("Returns first non-whitespace character on the line " +
//                 "when position is at the start of the line",
//                 13,
//                 getLineFirstCharPos(7));
//    assertEquals("Returns first non-whitespace character on the line " +
//                 "when the position is after the first non-ws character " +
//                 "on the line",
//                 13,
//                 getLineFirstCharPos(16));
//    assertEquals("Returns first non-whitespace character on the line " +
//                 "when the position is at the newline",
//                 13,
//                 getLineFirstCharPos(19));
//    
//    
//    _setDocText("abcdefghijk");
//    _doc.setCurrentLocation(3);
//    int reducedModelPos = _doc.getReduced().absOffset();
//    getLineFirstCharPos(5);
//    assertEquals("Check that position in reduced model is unaffected " +
//                 "after call to getLineFirstCharPos",
//                 reducedModelPos,
//                 _doc.getReduced().absOffset());
//  }
//
//
//  public void testGetFirstNonWSCharPos() throws BadLocationException {
//
//    _setDocText("foo();\nbar()\tx();     y();\n  \t  \n\nz();\n ");
//    assertEquals("Current position is non-whitespace", 0, getFirstNonWSCharPos(0));
//    assertEquals("Current position is non-whitespace, end of line", 5, getFirstNonWSCharPos(5));
//    assertEquals("Next non-whitespace is 1 '\\n' ahead.", 7, getFirstNonWSCharPos(6));
//    assertEquals("Next non-whitespace is 2 '\\t' ahead.", 13, getFirstNonWSCharPos(12));
//    assertEquals("Next non-whitespace is 3 spaces ahead.", 22, getFirstNonWSCharPos(20));
//    assertEquals("Next non-whitespace is multiple whitespaces ('\\n', '\\t', ' ') ahead.", 
//                 34,
//                 getFirstNonWSCharPos(27));
//    assertEquals("Next non-whitespace is end of document", 
//                 -1,
//                 getFirstNonWSCharPos(39));
//    
//    _setDocText("foo();\n// comment\nbar();\n");
//    assertEquals("Ignore single-line comments",
//                 18,
//                 getFirstNonWSCharPos(6));
//    
//    _setDocText("foo();\n /* bar\nblah */ boo\n");
//    assertEquals("Ignore multiline comments",
//                 23,
//                 getFirstNonWSCharPos(6));  
//    _setDocText("foo   /");
//    assertEquals("Slash at end of document",
//                 6,
//                 getFirstNonWSCharPos(4));
//    _setDocText("foo   //");
//    assertEquals("// at end",
//                 -1,
//                 getFirstNonWSCharPos(4));
//    _setDocText("foo   /*");
//    assertEquals("/* at end",
//                 -1,
//                 getFirstNonWSCharPos(4));
//    
//    _setDocText("abcdefghijk");
//    _doc.setCurrentLocation(3);
//    int reducedModelPos = _doc.getReduced().absOffset();
//    getLineFirstCharPos(5);
//    assertEquals("Check that position in reduced model is unaffected " +
//                 "after call to getLineFirstCharPos",
//                 reducedModelPos,
//                 _doc.getReduced().absOffset());
//  }
//  
//  /** Tests that the "intelligent" beginning of line can be found, given
//   * a position on the line.  Very similar to getFirstNonWSCharPos, except
//   * that comments are treated as non-whitespace, and less parsing needs
//   * to be done.
//   */
//  public void testGetIntelligentBeginLinePos() throws BadLocationException {
//    _setDocText("   foo();");
//    assertEquals("simple text, in WS", 
//                 0, _doc.getIntelligentBeginLinePos(1));
//    assertEquals("simple text, end of WS", 
//                 0, _doc.getIntelligentBeginLinePos(3));
//    assertEquals("simple text, in text", 
//                 3, _doc.getIntelligentBeginLinePos(4));
//    assertEquals("simple text, at end", 
//                 3, _doc.getIntelligentBeginLinePos(9));
//    
//    _setDocText("   // foo");
//    assertEquals("comment, in WS", 
//                 0, _doc.getIntelligentBeginLinePos(0));
//    assertEquals("comment, end of WS", 
//                 0, _doc.getIntelligentBeginLinePos(3));
//    assertEquals("comment, in text", 
//                 3, _doc.getIntelligentBeginLinePos(4));
//    assertEquals("comment, at end", 
//                 3, _doc.getIntelligentBeginLinePos(9));
//    
//    _setDocText("   foo();\n bar();\n");
//    assertEquals("multiple lines, at start", 
//                 10, _doc.getIntelligentBeginLinePos(10));
//    assertEquals("multiple lines, end of WS", 
//                 10, _doc.getIntelligentBeginLinePos(11));
//    assertEquals("multiple lines, in text", 
//                 11, _doc.getIntelligentBeginLinePos(13));
//    assertEquals("multiple lines, at end", 
//                 11, _doc.getIntelligentBeginLinePos(17));
//    
//    _setDocText("abc  def");
//    assertEquals("no leading WS, in middle", 
//                 0, _doc.getIntelligentBeginLinePos(5));
//  }
}


