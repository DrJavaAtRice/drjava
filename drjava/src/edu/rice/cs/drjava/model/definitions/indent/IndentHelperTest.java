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

/**
 * Tests for the helper methods in DefinitionsDocument
 * @version $Id$
 */
public class IndentHelperTest extends IndentRulesTestCase {

  public IndentHelperTest(String name) {
    super(name);
  }

  /**
   * @return The position of the first matching character in the given
   * array of characters found when scanning backwards from the current
   * position. Returns -1 if no match is found.
   */
  public void testFindPrevDelimiter() throws BadLocationException {
    char[] delimiters1 = {';', ':', '?'};

    // Used to test for delimiters not in test string
    char[] delimiters2 = {'%'};

    // Used to test if delimiter right after DOCSTART is found
    char[] delimiters3 = {'f'};

    _setDocText("/*bar;\nfoo();\nx;*/\nreturn foo;\n");
    assertEquals("Check that delimiters in multi-line " +
                 "comments are ignored",
                 DefinitionsDocument.ERROR_INDEX,
                 _doc.findPrevDelimiter(23, delimiters1));
    
    _setDocText("foo();\n//bar();\nbiz();\n");
    assertEquals("Check that delimiters in single-line " +
                 "comments are ignored",
                 5,
                 _doc.findPrevDelimiter(16, delimiters1));
    
    _setDocText("x=';'\n");
    assertEquals("Check that delimiters in single-quotes " +
                 "are ignored",
                 DefinitionsDocument.ERROR_INDEX,
                 _doc.findPrevDelimiter(5, delimiters1));
    
    _setDocText("x=\";\"\n");
    assertEquals("Check that delimiters in double-quotes " +
                 "are ignored",
                 DefinitionsDocument.ERROR_INDEX,
                 _doc.findPrevDelimiter(5, delimiters1));
    
    _setDocText("foo();\nfor(;;)\n");
    assertEquals("Check that delimiters in paren phrases " +
                 "are usually ignored",
                 5,
                 _doc.findPrevDelimiter(14, delimiters1));
    
    _setDocText("foo();\nfor(;;)\n");
    assertEquals("Check that delimiters in paren phrases " +
                 "can be detected",
                 12,
                 _doc.findPrevDelimiter(14, delimiters1, false));
    
    _setDocText("foo();\n test ? x : y;\n\t    return blah();\n");
    assertEquals("Check that ERROR_INDEX is returned if no matching character is found", 
                 DefinitionsDocument.ERROR_INDEX, 
                 _doc.findPrevDelimiter(20, delimiters2)); 
    assertEquals("Check that delimiter is found if it is right after DOCSTART",
                 0,
                 _doc.findPrevDelimiter(20, delimiters3));
    assertEquals("Check that delimiter is not found if " + 
                 "it is at cursor's position",
                 DefinitionsDocument.ERROR_INDEX,
                 _doc.findPrevDelimiter(5, delimiters1));
    assertEquals("Check that the first delimiter in the list is found",
                 17,
                 _doc.findPrevDelimiter(19, delimiters1));
    assertEquals("Check that the second delimiter in the list is found",
                 13,
                 _doc.findPrevDelimiter(17, delimiters1));
    assertEquals("Check that the last delimiter in the list is found",
                 5,
                 _doc.findPrevDelimiter(13, delimiters1));
    
    _setDocText("abcdefghijk");
    _doc.setCurrentLocation(3);
    int reducedModelPos = _doc.getReduced().absOffset();
    _doc.findPrevDelimiter(8, delimiters2);
    assertEquals("Check that position in reduced model is unaffected " +
                 "after call to findPrevDelimiter",
                 reducedModelPos,
                 _doc.getReduced().absOffset());
  }


  public void testPosInParenPhrase() 
    throws BadLocationException {

      _setDocText("(;)");
      assertEquals("';' in parent phrase",
                   true,
                   _doc.posInParenPhrase(1));
      
      _setDocText("abcdefghijk");
      _doc.setCurrentLocation(3);
      int reducedModelPos = _doc.getReduced().absOffset();
      _doc.posInParenPhrase(8);
      assertEquals("Check that position in reduced model is unaffected " +
                   "after call to posInParenPhrase",
                   reducedModelPos,
                   _doc.getReduced().absOffset());   
    }


  public void testGetIndentOfCurrStmtDelimiters() throws BadLocationException {

    _setDocText("foo();\n");
    assertEquals("prev delimiter DOCSTART, no indent",
                 "",
                 _doc.getIndentOfCurrStmt(3));
    _setDocText("  foo();\n");
    assertEquals("prev delimiter DOCSTART, indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(7));
    
    _setDocText("bar();\nfoo();\n");
    assertEquals("prev delimiter ';', no indent",
                 "",
                 _doc.getIndentOfCurrStmt(7));
    _setDocText("  bar();\n  foo();\n");
    assertEquals("prev delimiter ';', indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(9));
    
    _setDocText("void bar()\n{\nfoo();\n");
    assertEquals("prev delimiter '{', no indent",
                 "",
                 _doc.getIndentOfCurrStmt(13));
    _setDocText("void bar()\n{\n  foo();\n");
    assertEquals("prev delimiter '{', indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(13));
    
    _setDocText("}\nfoo();\n");
    assertEquals("prev delimiter '}', no indent",
                 "",
                 _doc.getIndentOfCurrStmt(2));
    _setDocText("}\n  foo();\n");
    assertEquals("prev delimiter '}', indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(2));
  }

  public void testGetIndentOfCurrStmtDelimiterSameLine() 
    throws BadLocationException {
    
      _setDocText("bar(); foo();\n");
      assertEquals("prev delimiter on same line, no indent",
                   "",
                   _doc.getIndentOfCurrStmt(6));
      
      _setDocText("  bar(); foo();\n");
      assertEquals("prev delimiter on same line, indent two spaces",
                   "  ",
                   _doc.getIndentOfCurrStmt(8));
  }

  public void testGetIndentOfCurrStmtMultipleLines()
    throws BadLocationException {

    String text = 
      "  oogabooga();\n" +
      "  bar().\n" +  
      "    bump().\n" +  
      "    //comment\n" +  
      "    /*commment\n" +
      "     *again;{}\n" +
      "     */\n" +
      "     foo();\n";

    _setDocText(text);
    assertEquals("start stmt on previous line, indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(24));
    assertEquals("start stmt before previous line, " +
                 "cursor inside single-line comment " +
                 "indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(42));
    assertEquals("start stmt before single-line comment, " +
                 "cursor inside multi-line comment " +
                 "indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(56));
    assertEquals("start stmt before multi-line comment, " +
                 "indent two spaces",
                 "  ",
                 _doc.getIndentOfCurrStmt(88));
  }

  public void testGetIndentOfCurrStmtIgnoreDelimsInParenPhrase()
    throws BadLocationException
  {
    
    String text =
      "  bar.\n (;)\nfoo();";
    
    _setDocText(text);
    assertEquals("ignores delimiter in paren phrase",
                 "  ",
                 _doc.getIndentOfCurrStmt(12));
  }

  public void testGetIndentOfCurrStmtEndOfDoc() 
    throws BadLocationException
  {
    
    _setDocText("foo.\n");
    assertEquals("cursor at end of document, no indent",
                 "",
                 _doc.getIndentOfCurrStmt(5));
   
  }

  public void testGetLineStartPos() throws BadLocationException {
    _setDocText("foo\nbar\nblah");
    assertEquals("Returns position after the previous newline",
                 4,
                 _doc.getLineStartPos(6));
    assertEquals("Returns position after previous newline when cursor " +
                 "is at the position after the previous newline",
                 4,
                 _doc.getLineStartPos(4));
    assertEquals("Returns DOCSTART when there's no previous newline",
                 0,
                 _doc.getLineStartPos(2));
    assertEquals("Returns DOCSTART when the cursor is at DOCSTART",
                 0,
                 _doc.getLineStartPos(0));
    
    _setDocText("abcdefghijk");
    _doc.setCurrentLocation(3);
    int reducedModelPos = _doc.getReduced().absOffset();
    _doc.getLineStartPos(5);
    assertEquals("Check that position in reduced model is unaffected " +
                 "after call to getLineStartPos",
                 reducedModelPos,
                 _doc.getReduced().absOffset());
  }

  public void testGetLineEndPos() throws BadLocationException {
    _setDocText("foo\nbar\nblah");
    assertEquals("Returns position before the next newline",
                 7,
                 _doc.getLineEndPos(5));
    assertEquals("Returns position before the next newline when cursor " +
                 "is at the position before the next newline",
                 7,
                 _doc.getLineEndPos(7));
    assertEquals("Returns the end of the document when there's no next newline",
                 12,
                 _doc.getLineEndPos(9));
    
    _setDocText("abcdefghijk");
    _doc.setCurrentLocation(3);
    int reducedModelPos = _doc.getReduced().absOffset();
    _doc.getLineEndPos(5);
    assertEquals("Check that position in reduced model is unaffected " +
                 "after call to getLineEndPos",
                 reducedModelPos,
                 _doc.getReduced().absOffset());
  }

  public void testGetLineFirstCharPos() throws BadLocationException {
    _setDocText("   ");
    assertEquals("Returns the end of the document if the line " +
                 "is the last line in the document and has no " +
                 "non-whitespace characters",
                 3,
                 _doc.getLineFirstCharPos(1));
    
    _setDocText("   \nfoo();\n");
    assertEquals("Returns the next newline if there are " +
                 "no non-whitespace characters on the line",
                 3,
                 _doc.getLineFirstCharPos(1));
    
    _setDocText("foo();\n   \t  bar();\nbiz()\n");
    assertEquals("Returns first non-whitespace character on the line " +
                 "when position is at the start of the line",
                 13,
                 _doc.getLineFirstCharPos(7));
    assertEquals("Returns first non-whitespace character on the line " +
                 "when the position is after the first non-ws character " +
                 "on the line",
                 13,
                 _doc.getLineFirstCharPos(16));
    assertEquals("Returns first non-whitespace character on the line " +
                 "when the position is at the newline",
                 13,
                 _doc.getLineFirstCharPos(19));
    
    
    _setDocText("abcdefghijk");
    _doc.setCurrentLocation(3);
    int reducedModelPos = _doc.getReduced().absOffset();
    _doc.getLineFirstCharPos(5);
    assertEquals("Check that position in reduced model is unaffected " +
                 "after call to getLineFirstCharPos",
                 reducedModelPos,
                 _doc.getReduced().absOffset());
  }


  public void testGetFirstNonWSCharPos() throws BadLocationException {

    _setDocText("foo();\nbar()\tx();     y();\n  \t  \n\nz();\n ");
    assertEquals("Current position is non-whitespace", 0, _doc.getFirstNonWSCharPos(0));
    assertEquals("Current position is non-whitespace, end of line", 5, _doc.getFirstNonWSCharPos(5));
    assertEquals("Next non-whitespace is 1 '\\n' ahead.", 7, _doc.getFirstNonWSCharPos(6));
    assertEquals("Next non-whitespace is 2 '\\t' ahead.", 13, _doc.getFirstNonWSCharPos(12));
    assertEquals("Next non-whitespace is 3 spaces ahead.", 22, _doc.getFirstNonWSCharPos(20));
    assertEquals("Next non-whitespace is multiple whitespaces ('\\n', '\\t', ' ') ahead.", 
                 34,
                 _doc.getFirstNonWSCharPos(27));
    assertEquals("Next non-whitespace is end of document", 
                 -1,
                 _doc.getFirstNonWSCharPos(39));
    
    _setDocText("foo();\n// comment\nbar();\n");
    assertEquals("Ignore single-line comments",
                 18,
                 _doc.getFirstNonWSCharPos(6));
    
    _setDocText("foo();\n /* bar\nblah */ boo\n");
    assertEquals("Ignore multiline comments",
                 23,
                 _doc.getFirstNonWSCharPos(6));  
    _setDocText("foo   /");
    assertEquals("Slash at end of document",
                 6,
                 _doc.getFirstNonWSCharPos(4));
    _setDocText("foo   //");
    assertEquals("// at end",
                 -1,
                 _doc.getFirstNonWSCharPos(4));
    _setDocText("foo   /*");
    assertEquals("/* at end",
                 -1,
                 _doc.getFirstNonWSCharPos(4));
    
    _setDocText("abcdefghijk");
    _doc.setCurrentLocation(3);
    int reducedModelPos = _doc.getReduced().absOffset();
    _doc.getLineFirstCharPos(5);
    assertEquals("Check that position in reduced model is unaffected " +
                 "after call to getLineFirstCharPos",
                 reducedModelPos,
                 _doc.getReduced().absOffset());
  }  
}


