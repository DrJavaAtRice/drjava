/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import java.util.Vector;

import junit.framework.*;
import junit.extensions.*;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.GlobalEventNotifier;

/**
 * Tests the functionality of the definitions document.
 * @version $Id$
 */
public final class DefinitionsDocumentTest extends TestCase implements ReducedModelStates {
  private DefinitionsDocument _defModel;
  private GlobalEventNotifier _notifier;

  /**
   * Standard constructor.
   * @param name of the test
   */
  public DefinitionsDocumentTest(String name) {
    super(name);
  }

  /**
   * Create a definitions document to work with.
   */
  protected void setUp() {
    _notifier = new GlobalEventNotifier();
    _defModel = new DefinitionsDocument(_notifier);
    DrJava.getConfig().resetToDefaults();
  }

  /**
   * Create a test suite for JUnit to run.
   * @return a test suite based on this class
   */
  public static Test suite() {
    return  new TestSuite(DefinitionsDocumentTest.class);
  }

  /**
   * Test insertion.
   */
  public void testInsertToDoc() throws BadLocationException {
    _defModel.insertString(0, "a/*bc */\"\\{}()", null);
    assertEquals("#0.0", _defModel.getText(0, 8), "a/*bc */");
    assertEquals("#0.1", 14, _defModel.getCurrentLocation());
    _defModel.insertString(0, "Start:", null);
    assertEquals("#1.0", _defModel.getText(0, 14), "Start:a/*bc */");
    assertEquals("#1.1", 6, _defModel.getCurrentLocation());
    // document is:
    // Start:=>a/*bc */"\\{}()
    BraceReduction rm = _defModel.getReduced();
    assertEquals("2.1", FREE, rm.getStateAtCurrent());
    rm.move(2);
    // document is:
    // Start:a/=>*bc */"\\{}()
    assertEquals("2.3", "/*", rm.currentToken().getType());
    rm.move(2);
    // document is:
    // Start:a/*b=>c */"\\{}()
    assertEquals("2.4", true, rm.currentToken().isGap());
    assertEquals("2.5", ReducedToken.INSIDE_BLOCK_COMMENT, rm.currentToken().getState());
    rm.move(2);
    // document is:
    // Start:a/*bc =>*/"\{}()
    assertEquals("2.6", "*/", rm.currentToken().getType());
    rm.move(2);
    // document is:
    // Start:a/*bc */=>"\{}()
    assertEquals("2.7", "\"", rm.currentToken().getType());
    rm.move(1);
    // document is:
    // Start:a/*bc */"=>\{}()
    assertEquals("2.8", "\\", rm.currentToken().getType());
    rm.move(1);
    // document is:
    // Start:a/*bc */"\=>{}()
    assertEquals("2.9", "{", rm.currentToken().getType());
    rm.move(1);
    // document is:
    // Start:a/*bc */"\{=>}()
    assertEquals("2.91", "}", rm.currentToken().getType());
    rm.move(1);
    // document is:
    // Start:a/*bc */"\{}=>()
    assertEquals("2.92", "(", rm.currentToken().getType());
    rm.move(1);
    // document is:
    // Start:a/*bc */"\\{}(=>)
    assertEquals("2.93", ")", rm.currentToken().getType());
  }

  /**
   * Test inserting a star between a star-slash combo.
   * @exception BadLocationException
   */
  public void testInsertStarIntoStarSlash() throws BadLocationException {
    BraceReduction rm = _defModel.getReduced();
    _defModel.insertString(0, "/**/", null);
    // Put new star between second star and second slash
    _defModel.insertString(3, "*", null);
    _defModel.move(-4);
    assertEquals("1", "/*", rm.currentToken().getType());
    assertEquals("2", ReducedToken.FREE, rm.currentToken().getState());
    rm.move(2);
    assertEquals("3", "*", rm.currentToken().getType());
    assertEquals("4", ReducedToken.INSIDE_BLOCK_COMMENT, rm.currentToken().getState());
    rm.move(1);
    assertEquals("5", "*/", rm.currentToken().getType());
    assertEquals("6", ReducedToken.FREE, rm.currentToken().getState());
  }

  /**
   * Test inserting a slash between a star-slash combo.
   * @exception BadLocationException
   */
  public void testInsertSlashIntoStarSlash() throws BadLocationException {
    BraceReduction rm = _defModel.getReduced();
    _defModel.insertString(0, "/**/", null);
    // Put new slash between second star and second slash
    _defModel.insertString(3, "/", null);
    _defModel.move(-4);
    assertEquals("1", "/*", rm.currentToken().getType());
    assertEquals("2", ReducedToken.FREE, rm.currentToken().getState());
    rm.move(2);
    assertEquals("3", "*/", rm.currentToken().getType());
    assertEquals("4", ReducedToken.FREE, rm.currentToken().getState());
    rm.move(2);
    assertEquals("5", "/", rm.currentToken().getType());
    assertEquals("6", ReducedToken.FREE, rm.currentToken().getState());
  }

  /** Test inserting a star between a slash-star combo.
   *  @exception BadLocationException
   */
  public void testInsertStarIntoSlashStar() throws BadLocationException {
    BraceReduction rm = _defModel.getReduced();
    _defModel.insertString(0, "/**/", null);
    // Put new star between second star and second slash
    _defModel.insertString(1, "*", null);
    _defModel.move(-2);
    assertEquals("1", "/*", rm.currentToken().getType());
    assertEquals("2", ReducedToken.FREE, rm.currentToken().getState());
    rm.move(2);
    assertEquals("3", "*", rm.currentToken().getType());
    assertEquals("4", ReducedToken.INSIDE_BLOCK_COMMENT, rm.currentToken().getState());
    rm.move(1);
    assertEquals("5", "*/", rm.currentToken().getType());
    assertEquals("6", ReducedToken.FREE, rm.currentToken().getState());
  }

  /** Test removal of text. */
  public void testDeleteDoc() throws BadLocationException {
    _defModel.insertString(0, "a/*bc */", null);
    _defModel.remove(3, 3);
    assertEquals("#0.0", "a/**/", _defModel.getText(0, 5));
    assertEquals("#0.1", 3, _defModel.getCurrentLocation());
    BraceReduction rm = _defModel.getReduced();
    assertEquals("1.0", "*/", rm.currentToken().getType());
    // no longer support getBlockOffset
    //        assertEquals("1.1",0,rm.getBlockOffset());
    rm.move(-2);
    assertEquals("1.2", "/*", rm.currentToken().getType());
    rm.move(2);
    assertEquals("1.3", ReducedToken.INSIDE_BLOCK_COMMENT, rm.getStateAtCurrent());
  }

  /** Make sure the vector is consistent: all elements immediately adjoin
   *  one another (no overlap), and make sure all indices between start and end
   *  are in the vector. Vector is guaranteed to not have size zero.
   */
  private void _checkHighlightStatusConsistent(Vector<HighlightStatus> v,
                                               int start,
                                               int end)
  {
    // location we're at so far
    int walk = start;
    for (int i = 0; i < v.size(); i++) {
      assertEquals("Item #" + i + "in highlight vector starts at right place",
                   walk,
                   v.get(i).getLocation());
      // Sanity check: length > 0?
      assertTrue("Item #" + i + " in highlight vector has positive length",
                 v.get(i).getLength() > 0);

      walk += v.get(i).getLength();
    }
    assertEquals("Location after walking highlight vector",
                 end,
                 walk);
  }

  /** Test that keywords are highlighted properly.
   *  @exception BadLocationException
   */
  public void testHighlightKeywords1() throws BadLocationException {
    Vector<HighlightStatus> v;
    final String s = "public class Foo {\n" +
      "  private int _x = 0;\n" +
        "}";
    _defModel.insertString(_defModel.getLength(), s, null);
    v = _defModel.getHighlightStatus(0, _defModel.getLength());
    _checkHighlightStatusConsistent(v, 0, _defModel.getLength());
    // Make sure the keywords are highlighted
    assertEquals("vector length", 12, v.size());
    assertEquals(HighlightStatus.KEYWORD, v.get(0).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(1).getState());
    assertEquals(HighlightStatus.KEYWORD, v.get(2).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(3).getState());
    assertEquals(HighlightStatus.TYPE, v.get(4).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(5).getState());

    assertEquals(HighlightStatus.KEYWORD, v.get(6).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(7).getState());
    assertEquals(HighlightStatus.TYPE, v.get(8).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(9).getState());
    assertEquals(HighlightStatus.NUMBER, v.get(10).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(11).getState());
  }

  /** This test case simulates what happens when some text is selected
   *  and there is a keyword around too.
   *  In drjava-20010720-1712 there is a bug that if you enter "int Y" and
   *  then try to select "t Y", it exceptions. This is a test for that case.
   *  The important thing about the selecting thing is that because it wants
   *  to render the last three chars selected, it asks for the first two only
   *  in the call to getHighlightStatus.
   *  @exception BadLocationException
   */
  public void testHighlightKeywords2() throws BadLocationException {
    Vector<HighlightStatus> v;
    final String s = "int y";
    _defModel.insertString(_defModel.getLength(), s, null);
    // First sanity check the whole string's status
    v = _defModel.getHighlightStatus(0, _defModel.getLength());
    _checkHighlightStatusConsistent(v, 0, _defModel.getLength());
    // Make sure the keyword is highlighted

    assertEquals("vector length", 2, v.size());
    assertEquals(HighlightStatus.TYPE, v.get(0).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(1).getState());
    // Now only ask for highlights for "in"
    v = _defModel.getHighlightStatus(0, 2);
    _checkHighlightStatusConsistent(v, 0, 2);
    assertEquals("vector length", 1, v.size());
    assertEquals(0, v.get(0).getLocation());
    assertEquals(2, v.get(0).getLength());
  }

  /** Test going to the second line in a two-line document.
   *  @exception BadLocationException
   */
  public void testGotoLine1() throws BadLocationException {
    final String s = "a\n";
    _defModel.insertString(0, s, null);
    _defModel.gotoLine(2);
    assertEquals("#0.0", 2, _defModel.getCurrentLocation());
  }

  /**
   * Test going to a specific line.
   * @exception BadLocationException
   */
  public void testGotoLine2() throws BadLocationException {
    final String s = "abcd\n";
    _defModel.insertString(0, s, null);
    _defModel.gotoLine(2);
    assertEquals("#0.0", 5, _defModel.getCurrentLocation());
  }

  /**
   * Test going to the fourth line in a four line document.
   * @exception BadLocationException
   */
  public void testGotoLine3() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _defModel.insertString(0, s, null);
    _defModel.gotoLine(4);
    assertEquals("#0.0", 6, _defModel.getCurrentLocation());
  }

  /**
   * Test going to a line beyond the number of lines in a document
   * just goes to the end of the file.
   * @exception BadLocationException
   */
  public void testGotoLine4() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _defModel.insertString(0, s, null);
    _defModel.gotoLine(8);
    assertEquals("#0.0", 6, _defModel.getCurrentLocation());
  }

  /**
   * Test going to the first line of an empty document
   * doesn't do anything funny.  It should stay in the same
   * location.
   */
  public void testGotoLine5() {
    _defModel.gotoLine(1);
    assertEquals("#0.0", 0, _defModel.getCurrentLocation());
  }

  /**
   * Test going to a line that is greater than the line count
   * of an empty document just keeps you in your current location.
   */
  public void testGotoLine6() {
    _defModel.gotoLine(4);
    assertEquals("#0.0", 0, _defModel.getCurrentLocation());
  }

  /**
   * Test that going to a line within the document's line count
   * sets the current position to the first character of the line.
   * @exception BadLocationException
   */
  public void testGotoLine7() throws BadLocationException {
    final String s = "11111\n2222\n33333\n44444";
    _defModel.insertString(0, s, null);
    _defModel.gotoLine(3);
    assertEquals("#0.0", 11, _defModel.getCurrentLocation());
  }

  /**
   * Tests returning the current column in the document.
   */
  public void testGetColumn1() throws BadLocationException {
    final String s = "1234567890";
    assertEquals("#0.0", 0, _defModel.getCurrentCol());
    _defModel.insertString(0, s, null);
    assertEquals("#0.1", 10, _defModel.getCurrentCol());
    _defModel.gotoLine(0);
    assertEquals("#0.2", 0, _defModel.getCurrentCol());
  }


  /**
   * Tests returning the current column in the document.
   */
  public void testGetColumn2() throws BadLocationException {
    final String s = "1234567890\n1234\n12345";
    _defModel.insertString(0, s, null);
    assertEquals("#0.0", 5, _defModel.getCurrentCol() );
  }

  /**
   * Test returning second line in a two-line document.
   * @exception BadLocationException
   */
  public void testGetLine1() throws BadLocationException {
    final String s = "a\n";
    _defModel.insertString(0, s, null);
    _defModel.setCurrentLocation(2);
    assertEquals("#0.0", 2, _defModel.getCurrentLine());
  }

  /**
   * Test going to a specific line.
   * @exception BadLocationException
   */
  public void testGetLine2() throws BadLocationException {
    final String s = "abcd\n";
    _defModel.insertString(0, s, null);
    _defModel.setCurrentLocation(2);
    assertEquals("#0.0", 1, _defModel.getCurrentLine());
    _defModel.gotoLine(2);
    assertEquals("#0.1", 2, _defModel.getCurrentLine());
  }

  /**
   * Test going to the fourth line in a four line document.
   * @exception BadLocationException
   */
  public void testGetLine3() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _defModel.insertString(0, s, null);
    _defModel.setCurrentLocation(6);
    assertEquals("#0.0", 4, _defModel.getCurrentLine());
  }

  /**
   * Test going to a line beyond the number of lines in a document
   * just goes to the end of the file.
   * @exception BadLocationException
   */
  public void testGetLine4() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _defModel.insertString(0, s, null);
    _defModel.gotoLine(8);
    assertEquals("#0.0", 4, _defModel.getCurrentLine());
  }

  /**
   * Test going to the first line of an empty document
   * doesn't do anything funny.  It should stay in the same
   * location.
   */
  public void testGetLine5() {
    _defModel.setCurrentLocation(0);
    assertEquals("#0.0", 1, _defModel.getCurrentLine());
  }

  /**
   * Test going to a line that is greater than the line count
   * of an empty document just keeps you in your current location.
   */
  public void testGetLine6() {
    _defModel.gotoLine(4);
    assertEquals("#0.0", 1, _defModel.getCurrentLine());
  }

  /**
   * Test that going to a line within the document's line count
   * sets the current position to the first character of the line.
   * @exception BadLocationException
   */
  public void testGetLine7() throws BadLocationException {
    final String s = "12345\n7890\n2345\n789";
    _defModel.insertString(0, s, null);
    _defModel.setCurrentLocation(12);
    assertEquals("#0.0", 3, _defModel.getCurrentLine());
    _defModel.move(-5);
    assertEquals("#0.1", 2, _defModel.getCurrentLine());
    _defModel.setCurrentLocation(19);
    assertEquals("#0.2", 4, _defModel.getCurrentLine());
  }

  /**
   * Tests line numbering output after deletion of a block
   */
  public void testGetLineDeleteText() throws BadLocationException{
    final String s = "123456789\n123456789\n123456789\n123456789\n";
    _defModel.insertString(0,s,null);
    _defModel.setCurrentLocation(35);
    assertEquals("Before delete", 4, _defModel.getCurrentLine() );
    _defModel.remove(0,30);
    _defModel.setCurrentLocation(5);
    assertEquals("After delete", 1, _defModel.getCurrentLine() );
  }

  /**
   * Tests line numbering output after deletion of a block
   */
  public void testGetLineDeleteText2() throws BadLocationException {
    final String s = "123456789\n123456789\n123456789\n123456789\n";
    _defModel.insertString(0,s,null);
    _defModel.setCurrentLocation(35);
    assertEquals("Before delete", 4, _defModel.getCurrentLine());
    _defModel.remove(18,7);
    assertEquals("After delete", 2, _defModel.getCurrentLine());
  }

  /**
   * Test whether removeTabs actually removes all tabs.
   */
  public void testRemoveTabs1() {
    _defModel.setIndent(1);
    String test = "\t this \t\tis a \t\t\t\t\ttest\t\t";
    String result = _defModel._removeTabs(test);
    assertEquals( "  this   is a      test  ", result);
  }

  /**
   * As of drjava-20020122-1534, files with tabs ended up garbled, with
   * some of the text jumbled all around (bug #506630).
   * This test aims to replicate the problem.
   */
  public void testRemoveTabs2() {
   String input =
    "\ttoken = nextToken(); // read trailing parenthesis\n" +
    "\tif (token != ')')\n" +
    "\t  throw new ParseException(\"wrong number of arguments to |\");\n";

   String expected =
    " token = nextToken(); // read trailing parenthesis\n" +
    " if (token != ')')\n" +
    "   throw new ParseException(\"wrong number of arguments to |\");\n";

    int count = 5000;
    StringBuffer bigIn = new StringBuffer(input.length() * count);
    StringBuffer bigExp = new StringBuffer(expected.length() * count);
    for (int i = 0; i < count; i++) {
      bigIn.append(input);
      bigExp.append(expected);
    }

    String result = _defModel._removeTabs(bigIn.toString());
    assertEquals(bigExp.toString(), result);
  }

  /**
   * Test whether tabs are removed as appropriate on call to insertString.
   */
  public void testTabRemovalOnInsertString2() throws BadLocationException {
   String[] inputs = {
      "\ttoken = nextToken(); // read trailing parenthesis\n",
      "\tif (token != ')')\n",
      "\t  throw new ParseException(\"wrong number of arguments to |\");\n",
    };

   String expected =
    " token = nextToken(); // read trailing parenthesis\n" +
    " if (token != ')')\n" +
    "   throw new ParseException(\"wrong number of arguments to |\");\n";

    for (int i = 0; i < inputs.length; i++) {
      _defModel.insertString(_defModel.getLength(), inputs[i], null);
    }

    assertEquals(expected, _getAllText());
  }

  /** Test whether tabs are removed as appropriate on call to insertString. */
  public void testTabRemovalOnInsertString() throws BadLocationException {
    _defModel.setIndent(1);
    _defModel.insertString(0, " \t yet \t\tanother\ttest\t", null);
    String result = _defModel.getText();

    if (_defModel.tabsRemoved()) {
      assertEquals("   yet   another test ", result);
    }
    else { // Tabs should have been inserted.
      assertEquals(" \t yet \t\tanother\ttest\t", result);
    }
  }

  /** Test package-finding on empty document. */
  public void testPackageNameEmpty() throws InvalidPackageException {
    assertEquals("Package name for empty document", "", _defModel.getPackageName());
  }

  /** Test package-finding on simple document, with no funny comments. */
  public void testPackageNameSimple()
    throws BadLocationException, InvalidPackageException
  {
    final String[] comments = {
      "/* package very.bad; */",
      "// package terribly.wrong;"
    };

    final String[] packages = {"edu", "edu.rice", "edu.rice.cs.drjava" };

    for (int i = 0; i < packages.length; i++) {
      String curPack = packages[i];

      for (int j = 0; j < comments.length; j++) {
        String curComment = comments[j];

        setUp();
        _defModel.insertString(0,
                              curComment + "\n\n" +
                                "package " + curPack +
                                ";\nclass Foo { int x; }\n",
                              null);

        assertEquals("Package name for document with comment " + curComment,
                     curPack,
                     _defModel.getPackageName());
      }
    }
  }

  /**
   * Test package-finding on document with a block comment
   * between parts of package.
   */
  public void testPackageNameWeird1()
    throws BadLocationException, InvalidPackageException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava;";
    String normal = "edu.rice.cs.drjava";
    _defModel.insertString(0, weird, null);

    assertEquals("Package name for weird: '" + weird + "'",
                 normal,
                 _defModel.getPackageName());
  }

  /**
   * Test package-finding on document with a line comment between
   * parts of package.
   */
  public void testPackageNameWeird2()
    throws BadLocationException, InvalidPackageException
  {
    String weird = "package edu . rice //comment!\n.cs.drjava;";
    String normal = "edu.rice.cs.drjava";
    _defModel.insertString(0, weird, null);

    assertEquals("Package name for weird: '" + weird + "'",
                 normal,
                 _defModel.getPackageName());
  }

  /**
   * Puts an otherwise valid package statement after a valid import
   * declaration.
   * This should result in seeing no package statement (for the purposes
   * of getSourceRoot), so the resulting package name should be "".
   */
  public void testGetPackageNameWithPackageStatementAfterImport()
    throws BadLocationException, InvalidPackageException
  {
    String text = "import java.util.*;\npackage junk;\nclass Foo {}";
    _defModel.insertString(0, text, null);
    assertEquals("Package name for text with package statement after import",
                 "",
                 _defModel.getPackageName());
  }

  private String _getAllText() throws BadLocationException {
    return _defModel.getText();
  }
  /**
   * Test class name-finding on document
   */
  public void testTopLevelClassName()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; class MyClass<T> implements O{";
    String result = "MyClass";
    _defModel.insertString(0, weird, null);

    assertEquals("class name for weird: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

 /**
   * Test interface name-finding on document
   */
  public void testTopLevelInterfaceName()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " interface thisInterface { \n" +
      " class MyClass {";
    String result = "thisInterface";
    _defModel.insertString(0, weird, null);

    assertEquals("class name for interface: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

 /**
   * Test class name-finding on document
   */
  public void testTopLevelClassNameWComments()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      "/* class Y */ \n" +
      " /* class Foo \n" +
      " * class Bar \n" +
      " interface Baz \n" +
      " */ \n" +
      "//class Blah\n" +
      "class MyClass {";

    String result = "MyClass";
    _defModel.insertString(0, weird, null);

    assertEquals("class name for class: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

  /**
   * Tests that a keyword with no space following it does not cause
   * a StringOutOfBoundsException (bug 742226).
   */
  public void testTopLevelClassNameNoSpace()
    throws BadLocationException
  {
    String c = "class";
    _defModel.insertString(0, c, null);
    try {
      _defModel.getFirstTopLevelClassName();
      fail("Should not have found a class name");
    }
    catch (ClassNameNotFoundException e) {
      // Good, we expect this
    }
  }

  /**
   * Tests that the word class is not recognized if it is not followed
   * by whitespace.
   */
  public void testTopLevelClassNameWithClassloaderImport()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "import classloader.class; class MyClass {";
    String result = "MyClass";
    _defModel.insertString(0, weird, null);

    assertEquals("class name for weird: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

  /**
   * Test class name-finding on document
   */
  public void testTopLevelClassNameMisleading()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " {class X} \n" +
      " interface thisInterface { \n" +
      " class MyInnerClass {";
    String result = "thisInterface";
    _defModel.insertString(0, weird, null);

    assertEquals("class name for interface: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

  /**
   * Test class name-finding on document
   */
  public void testTopLevelInterfaceNameMisleading()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " {interface X} " +
      " \"class Foo\"" +
      " class MyClass {";
    String result = "MyClass";
    _defModel.insertString(0, weird, null);

    assertEquals("class name for user interface: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

  /**
   * Test class name-finding on document
   */
  public void testTopLevelInterfaceNameMisleading2()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*interface comment!*/cs.drjava; \n" +
      " {interface X<T>} " +
      " \"class interface Foo\"" +
      " class MyClass extends Foo<T> {";
    String result = "MyClass";
    _defModel.insertString(0, weird, null);

    assertEquals("class name for user interface: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

  /**
   * Test class name-finding on document
   */
  public void testTopLevelInterfaceNameBeforeClassName()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " interface thisInterface { \n" +
      "  } \n" +
      " class thatClass {\n" +
      "  }";
    String result = "thisInterface";
    _defModel.insertString(0, weird, null);

    assertEquals("interface should have been chosen, rather than the class: '" + weird + "'",
                 result,
                 _defModel.getFirstTopLevelClassName());
  }

  /**
   * Test class name-finding on document
   */
  public void testTopLevelClassNameWithDelimiters()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird1 = "package edu . rice\n./*comment!*/cs.drjava; \n" +
       " class MyClass<T> {";
    String result1 = "MyClass";
    _defModel.insertString(0, weird1, null);

    assertEquals("generics should be removed: '" + weird1 + "'",
                 result1,
                 _defModel.getFirstTopLevelClassName());

    String weird2 = "package edu . rice\n./*comment!*/cs.drjava; \n" +
       " class My_Class {";
    String result2 = "My_Class";
    _defModel.insertString(0, weird2, null);

    assertEquals("underscores should remain: '" + weird1 + "'",
                 result2,
                 _defModel.getFirstTopLevelClassName());
  }

  /**
   * Tests that the name of a top level enclosing class can be found.
   */
  public void testTopLevelEnclosingClassName()
    throws BadLocationException, ClassNameNotFoundException
  {
    String classes =
      "import foo;\n" +  // 12 (including newline)
      "class C1 {\n" +  // 23
      "  void foo() { int a; }\n" +  // 47
      "  class C2 { int x;\n" +  // 67
      "    int y;\n" +  // 78
      "    class C3 {}\n" +  // 94
      "  } int b;\n" +  // 105
      "}\n" +  // 107
      "class C4 {\n" +  // 118
      "  class C5 {\n" +  // 131
      "    void bar() { int c; } class C6 {}\n" +  // 169
      "  }\n" +  // 173
      "} class C7 {}";  // 186

    _defModel.insertString(0, classes, null);

    // No enclosing class at start
    try {
      String result = _defModel.getEnclosingTopLevelClassName(3);
      fail("no enclosing class should be found at start");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }

    // No enclosing class before open brace
    try {
      _defModel.getEnclosingTopLevelClassName(15);
      fail("no enclosing class should be found before open brace");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }

    try {
      String result = _defModel.getEnclosingTopLevelClassName(186);
      fail("no enclosing class should be found at end of file");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }

    assertEquals("top level class name after first open brace", "C1",
                 _defModel.getEnclosingTopLevelClassName(22));
    assertEquals("top level class name inside C1", "C1",
                 _defModel.getEnclosingTopLevelClassName(26));
    assertEquals("top level class name inside method of C1", "C1",
                 _defModel.getEnclosingTopLevelClassName(42));
    assertEquals("top level class name on C2's brace", "C1",
                 _defModel.getEnclosingTopLevelClassName(58));
    assertEquals("top level class name after C2's brace", "C1",
                 _defModel.getEnclosingTopLevelClassName(59));
    assertEquals("top level class name inside C2", "C1",
                 _defModel.getEnclosingTopLevelClassName(68));
    assertEquals("top level class name inside C3", "C1",
                 _defModel.getEnclosingTopLevelClassName(92));
    assertEquals("top level class name after C3's close brace", "C1",
                 _defModel.getEnclosingTopLevelClassName(93));
    assertEquals("top level class name after C2's close brace", "C1",
                 _defModel.getEnclosingTopLevelClassName(100));

    // No enclosing class between classes
    try {
      _defModel.getEnclosingTopLevelClassName(107);
      fail("no enclosing class should be found between classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }

    assertEquals("class name inside C4", "C4",
                 _defModel.getEnclosingTopLevelClassName(122));
    assertEquals("class name inside C5", "C4",
                 _defModel.getEnclosingTopLevelClassName(135));
    assertEquals("class name inside C6", "C4",
                 _defModel.getEnclosingTopLevelClassName(167));
    assertEquals("class name inside C7", "C7",
                 _defModel.getEnclosingTopLevelClassName(185));

    // No enclosing class at end
    try {
      String result = _defModel.getEnclosingTopLevelClassName(186);
      fail("no enclosing class should be found at end");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
  }

  /**
   * Tests that the correct qualified class name is returned with a package.
   */
  public void testQualifiedClassNameWithPackage()
    throws BadLocationException, ClassNameNotFoundException
  {
    String classes =
      "package foo;\n" +  // 13
      "class C1 {}\n" +  // 25
      "class C2 {}";  // 36
    _defModel.insertString(0, classes, null);

    assertEquals("qualified class name without pos", "foo.C1",
                 _defModel.getQualifiedClassName());
    assertEquals("enclosing class name in C1", "C1",
                 _defModel.getEnclosingTopLevelClassName(23));
    assertEquals("qualified class name with pos in C1", "foo.C1",
                 _defModel.getQualifiedClassName(23));
    assertEquals("qualified class name with pos in C2", "foo.C2",
                 _defModel.getQualifiedClassName(35));

    // No class name outside classes
    try {
      _defModel.getQualifiedClassName(15);
      fail("no qualified class name should be found outside classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
  }

  /**
   * Tests that the correct qualified class name is returned without a package.
   */
  public void testQualifiedClassNameWithoutPackage()
    throws BadLocationException, ClassNameNotFoundException
  {
    String classes =
      "class C1 {}\n" +  // 12
      "class C2 {}";  // 36
    _defModel.insertString(0, classes, null);

    assertEquals("qualified class name without pos", "C1",
                 _defModel.getQualifiedClassName());
    assertEquals("qualified class name with pos in C1", "C1",
                 _defModel.getQualifiedClassName(10));
    assertEquals("qualified class name with pos in C2", "C2",
                 _defModel.getQualifiedClassName(22));

    // No class name outside classes
    try {
      _defModel.getQualifiedClassName(15);
      fail("no qualified class name should be found outside classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
  }

  /**
   * Tests that the name of an enclosing class can be found.
   *
   * Note: I started to write this assuming that we would need to find
   * inner class names, but I'm not sure that's the case.  I'm writing
   * the method for the debugger, which only needs the *top level*
   * enclosing class.  Rather than delete this test, though, I'll leave
   * it in case we ever need to write getEnclosingClassName().
   *
  public void testEnclosingClassName()
    throws BadLocationException, ClassNameNotFoundException
  {
    String classes =
      "import foo;\n" +  // 12 (including newline)
      "class C1 {\n" +  // 23
      "  void foo() { int a; }\n" +  // 47
      "  class C2 { int x;\n" +  // 67
      "    int y;\n" +  // 78
      "    class C3 {}\n" +  // 94
      "  } int b;\n" +  // 105
      "}\n" +  // 107
      "class C4 {\n" +  // 118
      "} class C5 {}";  // 131
    _defModel.insertString(0, classes, null);

    // No enclosing class at start
    try {
      String result = _defModel.getEnclosingClassName(3);
      fail("no enclosing class should be found at start");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }

    // No enclosing class before open brace
    try {
      String result = _defModel.getEnclosingClassName(15);
      fail("no enclosing class should be found before open brace");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }

    assertEquals("class name after first open brace", "C1",
                 _defModel.getEnclosingClassName(22));
    assertEquals("class name inside C1", "C1",
                 _defModel.getEnclosingClassName(26));
    assertEquals("class name inside method of C1", "C1",
                 _defModel.getEnclosingClassName(42));
    assertEquals("class name on C2's brace", "C1",
                 _defModel.getEnclosingClassName(58));
    assertEquals("class name after C2's brace", "C2",
                 _defModel.getEnclosingClassName(59));
    assertEquals("class name inside C2", "C2",
                 _defModel.getEnclosingClassName(68));
    assertEquals("class name inside C3", "C3",
                 _defModel.getEnclosingClassName(92));
    assertEquals("class name after C3's close brace", "C2",
                 _defModel.getEnclosingClassName(93));
    assertEquals("class name after C2's close brace", "C1",
                 _defModel.getEnclosingClassName(100));

    // No enclosing class between classes
    try {
      String result = _defModel.getEnclosingClassName(107);
      fail("no enclosing class should be found between classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }

    assertEquals("class name inside C4", "C4",
                 _defModel.getEnclosingClassName(118));
    assertEquals("class name inside C5", "C5",
                 _defModel.getEnclosingClassName(130));

    // No enclosing class at end
    try {
      String result = _defModel.getEnclosingClassName(131);
      fail("no enclosing class should be found at end");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
  }*/

  /** Verify that undoing a multiple-line indent will be a single undo action
   *  @throws BadLocationException
   */
  public void testUndoAndRedoAfterMultipleLineIndent() throws BadLocationException {  //this fails
    String text =
      "public class stuff {\n" +
      "private int _int;\n" +
      "private Bar _bar;\n" +
      "public void foo() {\n" +
      "_bar.baz(_int);\n" +
      "}\n" +
      "}\n";

    String indented =
      "public class stuff {\n" +
      "  private int _int;\n" +
      "  private Bar _bar;\n" +
      "  public void foo() {\n" +
      "    _bar.baz(_int);\n" +
      "  }\n" +
      "}\n";

    _defModel.addUndoableEditListener(_defModel.getUndoManager());
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,new Integer(2));
    _defModel.insertString(0,text,null);
    assertEquals("insertion",text, _defModel.getText()); 
    /* This is necessary here and other places where indenting or commenting takes place because the undoListener in DefinitionsPane 
     * currently starts compound edits, but here, there's no DefinitionsPane.
     * Perhaps there's some way to factor the undoListener in CompoundUndoManager to be the one that starts compound edits 
     * so that it will work with or without the view.
     */
    _defModel.getUndoManager().startCompoundEdit();
    _defModel.indentLines(0,_defModel.getLength());
    assertEquals("indenting",indented, _defModel.getText());
    _defModel.getUndoManager().undo();
    assertEquals("undo",text, _defModel.getText());
    _defModel.getUndoManager().redo();
    assertEquals("redo",indented, _defModel.getText());
  }

  /** Verify that undoing a multiple-line indent will be a single undo action
   *  @throws BadLocationException
   */
  public void testUndoAndRedoAfterMultipleLineCommentAndUncomment()
    throws BadLocationException {
    String text =
      "public class stuff {\n" +
      "  private int _int;\n" +
      "  private Bar _bar;\n" +
      "  public void foo() {\n" +
      "    _bar.baz(_int);\n" +
      "  }\n" +
      "}\n";

    String commented =
      "//public class stuff {\n" +
      "//  private int _int;\n" +
      "//  private Bar _bar;\n" +
      "//  public void foo() {\n" +
      "//    _bar.baz(_int);\n" +
      "//  }\n" +
      "//}\n";

    _defModel.addUndoableEditListener(_defModel.getUndoManager());
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,new Integer(2));
    _defModel.insertString(0,text,null);
    assertEquals("insertion",text, _defModel.getText());

    _defModel.getUndoManager().startCompoundEdit();
    _defModel.commentLines(0,_defModel.getLength());
    assertEquals("commenting",commented, _defModel.getText());
    _defModel.getUndoManager().undo();
    assertEquals("undo commenting",text, _defModel.getText());
    _defModel.getUndoManager().redo();
    assertEquals("redo commenting",commented, _defModel.getText());

    _defModel.getUndoManager().startCompoundEdit();
    _defModel.uncommentLines(0,_defModel.getLength());
    assertEquals("uncommenting",text, _defModel.getText());
    _defModel.getUndoManager().undo();
    assertEquals("undo uncommenting",commented, _defModel.getText());
    _defModel.getUndoManager().redo();
    assertEquals("redo uncommenting",text, _defModel.getText());
  }

  /** Test method for CompoundUndoManager.  Tests that the nested compound edit functionality works correctly.
   *  @throws BadLocationException
   */
  public void testCompoundUndoManager() throws BadLocationException {
    String text =
      "public class foo {\n" +
      "int bar;\n" +
      "}";

    String indented =
      "public class foo {\n" +
      "  int bar;\n" +
      "}";
    CompoundUndoManager undoManager = _defModel.getUndoManager();

    _defModel.addUndoableEditListener(undoManager);
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,new Integer(2));

    // 1

    // Start a compound edit and verify the returned key
    int key = undoManager.startCompoundEdit();
    assertEquals("Should have returned the correct key.", 0, key);

    // Insert a test string into the document
    _defModel.insertString(0, text, null);
    assertEquals("Should have inserted the text properly.", text,
                 _defModel.getText());

    // Indent the lines, so as to trigger a nested compound edit
    undoManager.startCompoundEdit();
    _defModel.indentLines(0, _defModel.getLength());
    assertEquals("Should have indented correctly.", indented,
                 _defModel.getText());

    undoManager.undo();
    assertEquals("Should have undone correctly.", "",
                 _defModel.getText());

    // 2

    String commented =
      "//public class foo {\n" +
      "//  int bar;\n" +
      "//}";

    // Start a compound edit and verify the returned key
    key = _defModel.getUndoManager().startCompoundEdit();
    assertEquals("Should have returned the correct key.", 2, key);

    // Insert a test string into the document
    _defModel.insertString(0, text, null);
    assertEquals("Should have inserted the text properly.", text,
                 _defModel.getText());

    // Indent the lines, so as to trigger a nested compond edit
    _defModel.indentLines(0, _defModel.getLength());
    assertEquals("Should have indented correctly.", indented,
                 _defModel.getText());

    undoManager.startCompoundEdit();
    _defModel.commentLines(0, _defModel.getLength());
    assertEquals("Should have commented correctly.", commented,
                 _defModel.getText());

    // Undo the second compound edit
    _defModel.getUndoManager().undo();
    assertEquals("Should have undone the commenting.", indented,
                 _defModel.getText());

    // Undo the first compound edit
    _defModel.getUndoManager().undo();
    assertEquals("Should have undone the indenting and inserting.", "",
                 _defModel.getText());

    // 3

    // Start a compound edit and verify the returned key
    key = _defModel.getUndoManager().startCompoundEdit();
    assertEquals("Should have returned the correct key.", 4, key);

    // Insert a test string into the document
    _defModel.insertString(0, text, null);
    assertEquals("Should have inserted the text properly.", text,
                 _defModel.getText());

    // Indent the lines, so as to trigger a nested compond edit
    _defModel.indentLines(0, _defModel.getLength());
    assertEquals("Should have indented correctly.", indented,
                 _defModel.getText());

//    // Try to undo the nested edit
//    try {
//      _defModel.getUndoManager().undo();
//      fail("Should not have allowed undoing a nested edit.");
//    }
//    catch (CannotUndoException e) {
//      // Correct: cannot undo a nested edit
//    }
//
//    try {
//      _defModel.getUndoManager().redo();
//      fail("Should not have allowed redoing a nested edit.");
//    }
//    catch (CannotRedoException cre) {
//      // Correct: cannot redo a nested edit
//    }
//
    // Try end the compound edit with a wrong key
    try {
      _defModel.getUndoManager().endCompoundEdit(key + 2);
//      fail("Should not have allowed ending a compound edit with a wrong key.");
    }
    catch (IllegalStateException e) {
      assertEquals("Should have printed the correct error message.",
                   "Improperly nested compound edits.", e.getMessage());
    }

    // Indent the lines, so as to trigger a nested compound edit
    undoManager.startCompoundEdit();
    _defModel.indentLines(0, _defModel.getLength());
    assertEquals("Should have indented correctly.", indented,
                 _defModel.getText());

    // We've taken out this part of the test because of our change to
    // undo where we close the nearest open compound edit upon undo-ing,
    // pasting, commenting, un-commenting, indenting, and backspacing.
    // We should never have a nested edit anymore.

    // Try to undo the nested edit
//    try {
//      _defModel.getUndoManager().undo();
//      fail("Should not have allowed undoing a nested edit.");
//    }
//    catch (CannotUndoException e) {
//      // Correct: cannot undo a nested edit
//    }

    // End the compound edit and undo
//    _defModel.getUndoManager().endCompoundEdit(key);
    _defModel.getUndoManager().undo();
    assertEquals("Should have undone the indenting and inserting.", "",
                 _defModel.getText());
  }

  /**
   * Verifies that the undo manager correctly determines if the document has
   * been modified since the last save.
   */
  public void testUndoOrRedoSetsUnmodifiedState() throws BadLocationException {
    _defModel.addUndoableEditListener(_defModel.getUndoManager());
    _defModel.insertString(0, "This is text", null);
    assertTrue("Document should be modified.", _defModel.isModifiedSinceSave());
    _defModel.getUndoManager().undo();
    _defModel.setModifiedSinceSave();
    assertFalse("Document should no longer be modified after undo.", _defModel.isModifiedSinceSave());
    _defModel.insertString(0, "This is text", null);
    _defModel.resetModification();
    assertFalse("Document should not be modified after \"save\".", _defModel.isModifiedSinceSave());
    _defModel.getUndoManager().undo();
    _defModel.setModifiedSinceSave();
    assertTrue("Document should be modified after undo.", _defModel.isModifiedSinceSave());
    _defModel.getUndoManager().redo();
    _defModel.setModifiedSinceSave();
    assertFalse("Document should no longer be modified after redo.", _defModel.isModifiedSinceSave());
  }
}
