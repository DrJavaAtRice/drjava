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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import javax.swing.event.*;
import java.util.*;
import gj.util.Vector;

import junit.framework.*;
import junit.extensions.*;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

/**
 * Tests the functionality of the definitions document.
 * @version $Id$
 */
public class DefinitionsDocumentTest extends TestCase
  implements ReducedModelStates 
{
  protected DefinitionsDocument defModel;

  /**
   * Constructor.
   * @param   String name
   */
  public DefinitionsDocumentTest(String name) {
    super(name);
  }

  /**
   * Create a definitions document to work with.
   */
  protected void setUp() {
    defModel = new DefinitionsDocument();
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
    defModel.insertString(0, "a/*bc */\"\\{}()", null);
    assertEquals("#0.0", defModel.getText(0, 8), "a/*bc */");
    assertEquals("#0.1", 14, defModel.getCurrentLocation());
    defModel.insertString(0, "Start:", null);
    assertEquals("#1.0", defModel.getText(0, 14), "Start:a/*bc */");
    assertEquals("#1.1", 6, defModel.getCurrentLocation());
    // document is:
    // Start:=>a/*bc */"\\{}()
    BraceReduction rm = defModel.getReduced();
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
    BraceReduction rm = defModel.getReduced();
    defModel.insertString(0, "/**/", null);
    // Put new star between second star and second slash
    defModel.insertString(3, "*", null);
    defModel.move(-4);
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
    BraceReduction rm = defModel.getReduced();
    defModel.insertString(0, "/**/", null);
    // Put new slash between second star and second slash
    defModel.insertString(3, "/", null);
    defModel.move(-4);
    assertEquals("1", "/*", rm.currentToken().getType());
    assertEquals("2", ReducedToken.FREE, rm.currentToken().getState());
    rm.move(2);
    assertEquals("3", "*/", rm.currentToken().getType());
    assertEquals("4", ReducedToken.FREE, rm.currentToken().getState());
    rm.move(2);
    assertEquals("5", "/", rm.currentToken().getType());
    assertEquals("6", ReducedToken.FREE, rm.currentToken().getState());
  }

  /**
   * Test inserting a star between a slash-star combo.
   * @exception BadLocationException
   */
  public void testInsertStarIntoSlashStar() throws BadLocationException {
    BraceReduction rm = defModel.getReduced();
    defModel.insertString(0, "/**/", null);
    // Put new star between second star and second slash
    defModel.insertString(1, "*", null);
    defModel.move(-2);
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
   * Test removal of text.
   */
  public void testDeleteDoc() throws BadLocationException {
    defModel.insertString(0, "a/*bc */", null);
    defModel.remove(3, 3);
    assertEquals("#0.0", "a/**/", defModel.getText(0, 5));
    assertEquals("#0.1", 3, defModel.getCurrentLocation());
    BraceReduction rm = defModel.getReduced();
    assertEquals("1.0", "*/", rm.currentToken().getType());
    // no longer support getBlockOffset
    //        assertEquals("1.1",0,rm.getBlockOffset());
    rm.move(-2);
    assertEquals("1.2", "/*", rm.currentToken().getType());
    rm.move(2);
    assertEquals("1.3", ReducedToken.INSIDE_BLOCK_COMMENT, rm.getStateAtCurrent());
  }

  /**
   * Make sure the vector is consistent: all elements immediately adjoin
   * one another (no overlap), and make sure all indices between start and end
   * are in the vector. Vector is guaranteed to not have size zero.
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
                   v.elementAt(i).getLocation());
      // Sanity check: length > 0?
      assertTrue("Item #" + i + " in highlight vector has positive length",
                 v.elementAt(i).getLength() > 0);
      
      walk += v.elementAt(i).getLength();
    }
    assertEquals("Location after walking highlight vector",
                 end,
                 walk);
  }
  
  /** 
   * Test that keywords are highlighted properly.
   * @exception BadLocationException
   */
  public void testHighlightKeywords1() throws BadLocationException {
    Vector<HighlightStatus> v;
    final String s = "public class Foo {\n" +
      "  private int _x = 0;\n" +
        "}";
    defModel.insertString(defModel.getLength(), s, null);
    v = defModel.getHighlightStatus(0, defModel.getLength());
    _checkHighlightStatusConsistent(v, 0, defModel.getLength());
    // Make sure the keywords are highlighted
    assertEquals("vector length", 12, v.size());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(0).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(1).getState());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(2).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(3).getState());
    assertEquals(HighlightStatus.TYPE, v.elementAt(4).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(5).getState());

    assertEquals(HighlightStatus.KEYWORD, v.elementAt(6).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(7).getState());
    assertEquals(HighlightStatus.TYPE, v.elementAt(8).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(9).getState());
    assertEquals(HighlightStatus.NUMBER, v.elementAt(10).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(11).getState());
  }
  
  /**
   * This test case simulates what happens when some text is selected
   * and there is a keyword around too.
   * In drjava-20010720-1712 there is a bug that if you enter "int Y" and
   * then try to select "t Y", it exceptions. This is a test for that case.
   * The important thing about the selecting thing is that because it wants
   * to render the last three chars selected, it asks for the first two only
   * in the call to getHighlightStatus.
   * @exception BadLocationException
   */
  public void testHighlightKeywords2() throws BadLocationException {
    Vector<HighlightStatus> v;
    final String s = "int y";
    defModel.insertString(defModel.getLength(), s, null);
    // First sanity check the whole string's status
    v = defModel.getHighlightStatus(0, defModel.getLength());
    _checkHighlightStatusConsistent(v, 0, defModel.getLength());
    // Make sure the keyword is highlighted

    assertEquals("vector length", 2, v.size());
    assertEquals(HighlightStatus.TYPE, v.elementAt(0).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(1).getState());
    // Now only ask for highlights for "in"
    v = defModel.getHighlightStatus(0, 2);
    _checkHighlightStatusConsistent(v, 0, 2);
    assertEquals("vector length", 1, v.size());
    assertEquals(0, v.elementAt(0).getLocation());
    assertEquals(2, v.elementAt(0).getLength());
  }
  
  /**
   * Test going to the second line in a two-line document.
   * @exception BadLocationException
   */
  public void testGotoLine1() throws BadLocationException {
    final String s = "a\n";
    defModel.insertString(0, s, null);
    defModel.gotoLine(2);
    assertEquals("#0.0", 2, defModel.getCurrentLocation());
  }

  /**
   * Test going to a specific line.
   * @exception BadLocationException
   */
  public void testGotoLine2() throws BadLocationException {
    final String s = "abcd\n";
    defModel.insertString(0, s, null);
    defModel.gotoLine(2);
    assertEquals("#0.0", 5, defModel.getCurrentLocation());
  }

  /**
   * Test going to the fourth line in a four line document.
   * @exception BadLocationException
   */
  public void testGotoLine3() throws BadLocationException {
    final String s = "a\nb\nc\n";
    defModel.insertString(0, s, null);
    defModel.gotoLine(4);
    assertEquals("#0.0", 6, defModel.getCurrentLocation());
  }

  /**
   * Test going to a line beyond the number of lines in a document
   * just goes to the end of the file.
   * @exception BadLocationException
   */
  public void testGotoLine4() throws BadLocationException {
    final String s = "a\nb\nc\n";
    defModel.insertString(0, s, null);
    defModel.gotoLine(8);
    assertEquals("#0.0", 6, defModel.getCurrentLocation());
  }

  /**
   * Test going to the first line of an empty document
   * doesn't do anything funny.  It should stay in the same
   * location.
   */
  public void testGotoLine5() {
    defModel.gotoLine(1);
    assertEquals("#0.0", 0, defModel.getCurrentLocation());
  }

  /**
   * Test going to a line that is greater than the line count
   * of an empty document just keeps you in your current location.
   */
  public void testGotoLine6() {
    defModel.gotoLine(4);
    assertEquals("#0.0", 0, defModel.getCurrentLocation());
  }

  /**
   * Test that going to a line within the document's line count
   * sets the current position to the first character of the line.
   * @exception BadLocationException
   */
  public void testGotoLine7() throws BadLocationException {
    final String s = "11111\n2222\n33333\n44444";
    defModel.insertString(0, s, null);
    defModel.gotoLine(3);
    assertEquals("#0.0", 11, defModel.getCurrentLocation());
  }
  
  /**
   * Tests returning the current column in the document.
   */
  public void testGetColumn1() throws BadLocationException {
    final String s = "1234567890";
    assertEquals("#0.0", 0, defModel.getCurrentCol());
    defModel.insertString(0, s, null);
    assertEquals("#0.1", 10, defModel.getCurrentCol());
    defModel.gotoLine(0);
    assertEquals("#0.2", 0, defModel.getCurrentCol());
  }
  
  
  /**
   * Tests returning the current column in the document.
   */
  public void testGetColumn2() throws BadLocationException {
    final String s = "1234567890\n1234\n12345";
    defModel.insertString(0, s, null);
    assertEquals("#0.0", 5, defModel.getCurrentCol() );
  }
  
  /**
   * Test returning second line in a two-line document.
   * @exception BadLocationException
   */
  public void testGetLine1() throws BadLocationException {
    final String s = "a\n";
    defModel.insertString(0, s, null);
    defModel.setCurrentLocation(2);
    assertEquals("#0.0", 2, defModel.getCurrentLine());
  }

  /**
   * Test going to a specific line.
   * @exception BadLocationException
   */
  public void testGetLine2() throws BadLocationException {
    final String s = "abcd\n";
    defModel.insertString(0, s, null);
    defModel.setCurrentLocation(2);
    assertEquals("#0.0", 1, defModel.getCurrentLine());
    defModel.gotoLine(2);
    assertEquals("#0.1", 2, defModel.getCurrentLine());
  }

  /**
   * Test going to the fourth line in a four line document.
   * @exception BadLocationException
   */
  public void testGetLine3() throws BadLocationException {
    final String s = "a\nb\nc\n";
    defModel.insertString(0, s, null);
    defModel.setCurrentLocation(6);
    assertEquals("#0.0", 4, defModel.getCurrentLine());
  }

  /**
   * Test going to a line beyond the number of lines in a document
   * just goes to the end of the file.
   * @exception BadLocationException
   */
  public void testGetLine4() throws BadLocationException {
    final String s = "a\nb\nc\n";
    defModel.insertString(0, s, null);
    defModel.gotoLine(8);
    assertEquals("#0.0", 4, defModel.getCurrentLine());
  }

  /**
   * Test going to the first line of an empty document
   * doesn't do anything funny.  It should stay in the same
   * location.
   */
  public void testGetLine5() {
    defModel.setCurrentLocation(0);
    assertEquals("#0.0", 1, defModel.getCurrentLine());
  }

  /**
   * Test going to a line that is greater than the line count
   * of an empty document just keeps you in your current location.
   */
  public void testGetLine6() {
    defModel.gotoLine(4);
    assertEquals("#0.0", 1, defModel.getCurrentLine());
  }

  /**
   * Test that going to a line within the document's line count
   * sets the current position to the first character of the line.
   * @exception BadLocationException
   */
  public void testGetLine7() throws BadLocationException {
    final String s = "12345\n7890\n2345\n789";
    defModel.insertString(0, s, null);
    defModel.setCurrentLocation(12);
    assertEquals("#0.0", 3, defModel.getCurrentLine());
    defModel.move(-5);
    assertEquals("#0.1", 2, defModel.getCurrentLine());
    defModel.setCurrentLocation(19);
    assertEquals("#0.2", 4, defModel.getCurrentLine());
  }
  
  public void testGetLineDeleteText() throws BadLocationException{
    final String s = "123456789\n123456789\n123456789\n123456789\n";
    defModel.insertString(0,s,null);
    defModel.setCurrentLocation(35);
    assertEquals("Before delete", 4, defModel.getCurrentLine() );
    defModel.remove(0,30);
    defModel.setCurrentLocation(5);
    assertEquals("After delete", 1, defModel.getCurrentLine() );
  }
  
  /**
   * Test whether removeTabs actually removes all tabs.
   */
  public void testRemoveTabs1() {
    defModel.setIndent(1);
    String test = "\t this \t\tis a \t\t\t\t\ttest\t\t";
    String result = defModel._removeTabs(test);
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

    String result = defModel._removeTabs(bigIn.toString());
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
      defModel.insertString(defModel.getLength(), inputs[i], null);
    }

    assertEquals(expected, _getAllText());
  }

  /**
   * Test whether tabs are removed as appropriate on call to insertString.
   */
  public void testTabRemovalOnInsertString() throws BadLocationException {
    defModel.setIndent(1);
    defModel.insertString(0, " \t yet \t\tanother\ttest\t", null);
    String result = defModel.getText(0, defModel.getLength());
    
    if (defModel.tabsRemoved()) {
      assertEquals("   yet   another test ", result);
    }
    else { // Tabs should have been inserted.
      assertEquals(" \t yet \t\tanother\ttest\t", result);
    }
  }

  /** Test package-finding on empty document. */
  public void testPackageNameEmpty()
    throws BadLocationException, InvalidPackageException
  {
    assertEquals("Package name for empty document",
                 "",
                 defModel.getPackageName());
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
        defModel.insertString(0,
                              curComment + "\n\n" + 
                                "package " + curPack +
                                ";\nclass Foo { int x; }\n",
                              null);

        assertEquals("Package name for document with comment " + curComment,
                     curPack,
                     defModel.getPackageName());
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
    defModel.insertString(0, weird, null);

    assertEquals("Package name for weird: '" + weird + "'",
                 normal,
                 defModel.getPackageName());
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
    defModel.insertString(0, weird, null);

    assertEquals("Package name for weird: '" + weird + "'",
                 normal,
                 defModel.getPackageName());
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
    defModel.insertString(0, text, null);
    assertEquals("Package name for text with package statement after import",
                 "",
                 defModel.getPackageName());
  }

  private String _getAllText() throws BadLocationException {
    return defModel.getText(0, defModel.getLength());
  }
  /**
   * Test class name-finding on document 
   */
  public void testClassName()
    throws BadLocationException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; class MyClass<T> implements O{";
    String result = "MyClass";
    defModel.insertString(0, weird, null);

    assertEquals("class name for weird: '" + weird + "'",
                 result,
                 defModel.getClassName());
  }

 /**
   * Test class name-finding on document 
   */
  public void testInterfaceName() throws BadLocationException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " interface thisInterface { \n" +
      " class MyClass {";
    String result = "thisInterface";
    defModel.insertString(0, weird, null);

    assertEquals("class name for interface: '" + weird + "'",
                 result,
                 defModel.getClassName());
  }

 /**
   * Test class name-finding on document 
   */
  public void testClassNameWComments() throws BadLocationException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" + 
      "/* class Y */ \n" + 
      " /* class Foo \n" + 
      " * class Bar \n" + 
      " interface Baz \n" + 
      " */ \n" + 
      "//class Blah\n" +
      "class MyClass {";

    String result = "MyClass";
    defModel.insertString(0, weird, null);

    assertEquals("class name for class: '" + weird + "'",
                 result,
                 defModel.getClassName());
  }
  
  /**
   * Test class name-finding on document 
   */
  public void testClassNameMisleading() throws BadLocationException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " {class X} " + 
      " interface thisInterface { \n" +
      " class MyInnerClass {";
    String result = "thisInterface";
    defModel.insertString(0, weird, null);

    assertEquals("class name for interface: '" + weird + "'",
                 result,
                 defModel.getClassName());
  }

  /**
   * Test class name-finding on document 
   */
  public void testInterfaceNameMisleading() throws BadLocationException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " {interface X} " + 
      " \"class Foo\"" +
      " class MyClass {";
    String result = "MyClass";
    defModel.insertString(0, weird, null);

    assertEquals("class name for interface: '" + weird + "'",
                 result,
                 defModel.getClassName());
  }
  
    /**
   * Test class name-finding on document 
   */
  public void testInterfaceNameBeforeClassName() throws BadLocationException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " interface thisInterface { \n" + 
      "  } \n" +
      " class thatClass {\n" +
      "  }";
    String result = "thisInterface";
    defModel.insertString(0, weird, null);

    assertEquals("interface should have been chosen, rather than the class: '" + weird + "'",
                 result,
                 defModel.getClassName());
  }
  
  /**
   * Test class name-finding on document 
   */
  public void testClassNameWithDelimiters() throws BadLocationException {
    String weird1 = "package edu . rice\n./*comment!*/cs.drjava; \n" +
       " class MyClass<T> {";
    String result1 = "MyClass";
    defModel.insertString(0, weird1, null);

    assertEquals("generics should be removed: '" + weird1 + "'",
                 result1,
                 defModel.getClassName());
    
    String weird2 = "package edu . rice\n./*comment!*/cs.drjava; \n" +
       " class My_Class {";
    String result2 = "My_Class";
    defModel.insertString(0, weird2, null);

    assertEquals("underscores should remain: '" + weird1 + "'",
                 result2,
                 defModel.getClassName());
  }
  
}
