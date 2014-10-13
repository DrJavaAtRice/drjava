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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.model.GlobalEventNotifier;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceReduction;
import edu.rice.cs.drjava.model.definitions.reducedmodel.HighlightStatus;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedToken;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;

import junit.framework.Test;
import junit.framework.TestSuite;

import javax.swing.text.BadLocationException;
import java.util.List;

/** Tests the functionality of the definitions document.
  * @version $Id: DefinitionsDocumentTest.java 5707 2012-08-26 05:57:03Z rcartwright $
  */
public final class DefinitionsDocumentTest extends DrJavaTestCase implements ReducedModelStates {
  private DefinitionsDocument _doc;
  private GlobalEventNotifier _notifier;
  
  /** Standard constructor.
    * @param name of the test
    */
  public DefinitionsDocumentTest(String name) { super(name); }
  
  /** Create a definitions document to work with. */
  protected void setUp() throws Exception {
    super.setUp();
    _notifier = new GlobalEventNotifier();
    _doc = new DefinitionsDocument(_notifier);
    DrJava.getConfig().resetToDefaults();
  }
  
  /** Create a test suite for JUnit to run.
   * @return a test suite based on this class
   */
  public static Test suite() { return  new TestSuite(DefinitionsDocumentTest.class); }
  
  /** Convenience method that wraps _doc.indentLines calls in a write lock. [Archaic] */
  private void indentLines(int selStart, int selEnd) {
    _doc.indentLines(selStart, selEnd); 
  }
  
  /** Test insertion. */
  public void textInsertToDoc() throws BadLocationException {
    _doc.insertString(0, "a/*bc */\"\\{}()", null);
    assertEquals("#0.0", _doc.getText(0, 8), "a/*bc */");
    assertEquals("#0.1", 14, _doc.getCurrentLocation());
    _doc.insertString(0, "Start:", null);
    assertEquals("#1.0", _doc.getText(0, 14), "Start:a/*bc */");
    assertEquals("#1.1", 6, _doc.getCurrentLocation());
    // document is:
    // Start:=>a/*bc */"\\{}()
    BraceReduction _reduced = _doc.getReduced();
    assertEquals("2.1", FREE, _reduced.getStateAtCurrent());
    _reduced.move(2);
    // document is:
    // Start:a/=>*bc */"\\{}()
    assertEquals("2.3", "/*", _reduced.currentToken().getType());
    _reduced.move(2);
    // document is:
    // Start:a/*b=>c */"\\{}()
    assertEquals("2.4", true, _reduced.currentToken().isGap());
    assertEquals("2.5", ReducedToken.INSIDE_BLOCK_COMMENT, _reduced.currentToken().getState());
    _reduced.move(2);
    // document is:
    // Start:a/*bc =>*/"\{}()
    assertEquals("2.6", "*/", _reduced.currentToken().getType());
    _reduced.move(2);
    // document is:
    // Start:a/*bc */=>"\{}()
    assertEquals("2.7", "\"", _reduced.currentToken().getType());
    _reduced.move(1);
    // document is:
    // Start:a/*bc */"=>\{}()
    assertEquals("2.8", "\\", _reduced.currentToken().getType());
    _reduced.move(1);
    // document is:
    // Start:a/*bc */"\=>{}()
    assertEquals("2.9", "{", _reduced.currentToken().getType());
    _reduced.move(1);
    // document is:
    // Start:a/*bc */"\{=>}()
    assertEquals("2.91", "}", _reduced.currentToken().getType());
    _reduced.move(1);
    // document is:
    // Start:a/*bc */"\{}=>()
    assertEquals("2.92", "(", _reduced.currentToken().getType());
    _reduced.move(1);
    // document is:
    // Start:a/*bc */"\\{}(=>)
    assertEquals("2.93", ")", _reduced.currentToken().getType());
  }
  
  /** Test inserting a star between a star-slash combo.
   * @exception BadLocationException
   */
  public void textInsertStarIntoStarSlash() throws BadLocationException {
    BraceReduction _reduced = _doc.getReduced();
    _doc.insertString(0, "/**/", null);
    // Put new star between second star and second slash
    _doc.insertString(3, "*", null);
    _doc.move(-4);
    assertEquals("1", "/*", _reduced.currentToken().getType());
    assertEquals("2", ReducedToken.FREE, _reduced.currentToken().getState());
    _reduced.move(2);
    assertEquals("3", "*", _reduced.currentToken().getType());
    assertEquals("4", ReducedToken.INSIDE_BLOCK_COMMENT, _reduced.currentToken().getState());
    _reduced.move(1);
    assertEquals("5", "*/", _reduced.currentToken().getType());
    assertEquals("6", ReducedToken.FREE, _reduced.currentToken().getState());
  }
  
  /** Test inserting a slash between a star-slash combo.
   * @exception BadLocationException
   */
  public void textInsertSlashIntoStarSlash() throws BadLocationException {
    BraceReduction _reduced = _doc.getReduced();
    _doc.insertString(0, "/**/", null);
    // Put new slash between second star and second slash
    _doc.insertString(3, "/", null);
    _doc.move(-4);
    assertEquals("1", "/*", _reduced.currentToken().getType());
    assertEquals("2", ReducedToken.FREE, _reduced.currentToken().getState());
    _reduced.move(2);
    assertEquals("3", "*/", _reduced.currentToken().getType());
    assertEquals("4", ReducedToken.FREE, _reduced.currentToken().getState());
    _reduced.move(2);
    assertEquals("5", "/", _reduced.currentToken().getType());
    assertEquals("6", ReducedToken.FREE, _reduced.currentToken().getState());
  }
  
  /** Test inserting a star between a slash-star combo.
    * @exception BadLocationException
    */
  public void textInsertStarIntoSlashStar() throws BadLocationException {
    BraceReduction _reduced = _doc.getReduced();
    _doc.insertString(0, "/**/", null);
    // Put new star between second star and second slash
    _doc.insertString(1, "*", null);
    _doc.move(-2);
    assertEquals("1", "/*", _reduced.currentToken().getType());
    assertEquals("2", ReducedToken.FREE, _reduced.currentToken().getState());
    _reduced.move(2);
    assertEquals("3", "*", _reduced.currentToken().getType());
    assertEquals("4", ReducedToken.INSIDE_BLOCK_COMMENT, _reduced.currentToken().getState());
    _reduced.move(1);
    assertEquals("5", "*/", _reduced.currentToken().getType());
    assertEquals("6", ReducedToken.FREE, _reduced.currentToken().getState());
  }
  
  /** Test removal of text. */
  public void textDeleteDoc() throws BadLocationException {
    _doc.insertString(0, "a/*bc */", null);
    _doc.remove(3, 3);
    assertEquals("#0.0", "a/**/", _doc.getText(0, 5));
    assertEquals("#0.1", 3, _doc.getCurrentLocation());
    BraceReduction _reduced = _doc.getReduced();
    assertEquals("1.0", "*/", _reduced.currentToken().getType());
    // no longer support getBlockOffset
    //        assertEquals("1.1",0,rm.getBlockOffset());
    _reduced.move(-2);
    assertEquals("1.2", "/*", _reduced.currentToken().getType());
    _reduced.move(2);
    assertEquals("1.3", ReducedToken.INSIDE_BLOCK_COMMENT, _reduced.getStateAtCurrent());
  }
  
  /** Make sure the vector is consistent: all elements immediately adjoin one another (no overlap), and make sure all
    * indices between start and end are in the vector. Vector is guaranteed to not have size zero.
    */
  private void _checkHighlightStatusConsistent(List<HighlightStatus> v, int start, int end) {
    // location we're at so far
    int walk = start;
    for (int i = 0; i < v.size(); i++) {
      assertEquals("Item #" + i + "in highlight vector starts at right place", walk, v.get(i).getLocation());
      // Sanity check: length > 0?
      assertTrue("Item #" + i + " in highlight vector has positive length", v.get(i).getLength() > 0);
      
      walk += v.get(i).getLength();
    }
    assertEquals("Location after walking highlight vector", end, walk);
  }
  
  /** Test that keywords are highlighted properly.
    * @exception BadLocationException
    */
  public void textHighlightKeywords1() throws BadLocationException {
    List<HighlightStatus> v;
    final String s = "public class Foo {\n" +
      "  private int _x = 0;\n" +
      "}";
    _doc.insertString(_doc.getLength(), s, null);
    v = _doc.getHighlightStatus(0, _doc.getLength());
    _checkHighlightStatusConsistent(v, 0, _doc.getLength());
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
  
  /** This test case simulates what happens when some text is selected and there is a keyword around too. In 
    * drjava-20010720-1712 there is a bug that if you enter "int Y" and then try to select "t Y", it throws an
    * exception. This is a test for that bug.  The important thing about the selection process is that it asks
    * for only the first two chars in the call to getHighlightStatus even though it wants to render the last 
    * three chars selected.
    * @throws BadLocationException
    */
  public void textHighlightKeywords2() throws BadLocationException {
    List<HighlightStatus> v;
    final String s = "int y";
    _doc.insertString(_doc.getLength(), s, null);
    // First sanity check the whole string's status
    v = _doc.getHighlightStatus(0, _doc.getLength());
    _checkHighlightStatusConsistent(v, 0, _doc.getLength());
    // Make sure the keyword is highlighted
    
    assertEquals("vector length", 2, v.size());
    assertEquals(HighlightStatus.TYPE, v.get(0).getState());
    assertEquals(HighlightStatus.NORMAL, v.get(1).getState());
    // Now only ask for highlights for "in"
    v = _doc.getHighlightStatus(0, 2);
    _checkHighlightStatusConsistent(v, 0, 2);
    assertEquals("vector length", 1, v.size());
    assertEquals(0, v.get(0).getLocation());
    assertEquals(2, v.get(0).getLength());
  }
  
  /** Test going to the second line in a two-line document.
    * @throws BadLocationException
    */
  public void textGotoLine1() throws BadLocationException {
    final String s = "a\n";
    _doc.insertString(0, s, null);
    _doc.gotoLine(2);
    assertEquals("#0.0", 2, _doc.getCurrentLocation());
  }
  
  /** Test going to a specific line.
   * @exception BadLocationException
   */
  public void textGotoLine2() throws BadLocationException {
    final String s = "abcd\n";
    _doc.insertString(0, s, null);
    _doc.gotoLine(2);
    assertEquals("#0.0", 5, _doc.getCurrentLocation());
  }
  
  /** Test going to the fourth line in a four line document.
   * @exception BadLocationException
   */
  public void textGotoLine3() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _doc.insertString(0, s, null);
    _doc.gotoLine(4);
    assertEquals("#0.0", 6, _doc.getCurrentLocation());
  }
  
  /** Test going to a line beyond the number of lines in a document
   * just goes to the end of the file.
   * @exception BadLocationException
   */
  public void textGotoLine4() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _doc.insertString(0, s, null);
    _doc.gotoLine(8);
    assertEquals("#0.0", 6, _doc.getCurrentLocation());
  }
  
  /** Test going to the first line of an empty document
   * doesn't do anything funny.  It should stay in the same
   * location.
   */
  public void textGotoLine5() {
    _doc.gotoLine(1);
    assertEquals("#0.0", 0, _doc.getCurrentLocation());
  }
  
  /** Test going to a line that is greater than the line count
   * of an empty document just keeps you in your current location.
   */
  public void textGotoLine6() {
    _doc.gotoLine(4);
    assertEquals("#0.0", 0, _doc.getCurrentLocation());
  }
  
  /** Test that going to a line within the document's line count
   * sets the current position to the first character of the line.
   * @exception BadLocationException
   */
  public void textGotoLine7() throws BadLocationException {
    final String s = "11111\n2222\n33333\n44444";
    _doc.insertString(0, s, null);
    _doc.gotoLine(3);
    assertEquals("#0.0", 11, _doc.getCurrentLocation());
  }
  
  /** Tests returning the current column in the document.
   */
  public void textGetColumn1() throws BadLocationException {
    final String s = "1234567890";
    assertEquals("#0.0", 0, _doc.getCurrentCol());
    _doc.insertString(0, s, null);
    assertEquals("#0.1", 10, _doc.getCurrentCol());
    _doc.gotoLine(0);
    assertEquals("#0.2", 0, _doc.getCurrentCol());
  }
  
  
  /** Tests returning the current column in the document.
   */
  public void textGetColumn2() throws BadLocationException {
    final String s = "1234567890\n1234\n12345";
    _doc.insertString(0, s, null);
    assertEquals("#0.0", 5, _doc.getCurrentCol() );
  }
  
  /** Test returning second line in a two-line document.
   * @exception BadLocationException
   */
  public void textGetLine1() throws BadLocationException {
    final String s = "a\n";
    _doc.insertString(0, s, null);
    _doc.setCurrentLocation(2);
    assertEquals("#0.0", 2, _doc.getCurrentLine());
  }
  
  /** Test going to a specific line.
   * @exception BadLocationException
   */
  public void textGetLine2() throws BadLocationException {
    final String s = "abcd\n";
    _doc.insertString(0, s, null);
    _doc.setCurrentLocation(2);
    assertEquals("#0.0", 1, _doc.getCurrentLine());
    _doc.gotoLine(2);
    assertEquals("#0.1", 2, _doc.getCurrentLine());
  }
  
  /** Test going to the fourth line in a four line document.
   * @exception BadLocationException
   */
  public void textGetLine3() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _doc.insertString(0, s, null);
    _doc.setCurrentLocation(6);
    assertEquals("#0.0", 4, _doc.getCurrentLine());
  }
  
  /** Test going to a line beyond the number of lines in a document
   * just goes to the end of the file.
   * @exception BadLocationException
   */
  public void textGetLine4() throws BadLocationException {
    final String s = "a\nb\nc\n";
    _doc.insertString(0, s, null);
    _doc.gotoLine(8);
    assertEquals("#0.0", 4, _doc.getCurrentLine());
  }
  
  /** Test going to the first line of an empty document
   * doesn't do anything funny.  It should stay in the same
   * location.
   */
  public void textGetLine5() {
    _doc.setCurrentLocation(0);
    assertEquals("#0.0", 1, _doc.getCurrentLine());
  }
  
  /** Test going to a line that is greater than the line count
   * of an empty document just keeps you in your current location.
   */
  public void textGetLine6() {
    _doc.gotoLine(4);
    assertEquals("#0.0", 1, _doc.getCurrentLine());
  }
  
  /** Test that going to a line within the document's line count
   * sets the current position to the first character of the line.
   * @exception BadLocationException
   */
  public void textGetLine7() throws BadLocationException {
    final String s = "12345\n7890\n2345\n789";
    _doc.insertString(0, s, null);
    _doc.setCurrentLocation(12);
    assertEquals("#0.0", 3, _doc.getCurrentLine());
    _doc.move(-5);
    assertEquals("#0.1", 2, _doc.getCurrentLine());
    _doc.setCurrentLocation(19);
    assertEquals("#0.2", 4, _doc.getCurrentLine());
  }
  
  /** Tests line numbering output after deletion of a block
   */
  public void textGetLineDeleteText() throws BadLocationException{
    final String s = "123456789\n123456789\n123456789\n123456789\n";
    _doc.insertString(0,s,null);
    _doc.setCurrentLocation(35);
    assertEquals("Before delete", 4, _doc.getCurrentLine() );
    _doc.remove(0,30);
    _doc.setCurrentLocation(5);
    assertEquals("After delete", 1, _doc.getCurrentLine() );
  }
  
  /** Tests line numbering output after deletion of a block
   */
  public void textGetLineDeleteText2() throws BadLocationException {
    final String s = "123456789\n123456789\n123456789\n123456789\n";
    _doc.insertString(0,s,null);
    _doc.setCurrentLocation(35);
    assertEquals("Before delete", 4, _doc.getCurrentLine());
    _doc.remove(18,7);
    assertEquals("After delete", 2, _doc.getCurrentLine());
  }
  
  /** Test whether removeTabs actually removes all tabs.
   */
  public void textRemoveTabs1() {
    _doc.setIndent(1);
    String test = "\t this \t\tis a \t\t\t\t\ttest\t\t";
    String result = DefinitionsDocument._removeTabs(test);
    assertEquals( "  this   is a      test  ", result);
  }
  
  /** As of drjava-20020122-1534, files with tabs ended up garbled, with
   * some of the text jumbled all around (bug #506630).
   * This test aims to replicate the problem.
   */
  public void textRemoveTabs2() {
    String input =
      "\ttoken = nextToken(); // read trailing parenthesis\n" +
      "\tif (token != ')')\n" +
      "\t  throw new ParseException(\"wrong number of arguments to |\");\n";
    
    String expected =
      " token = nextToken(); // read trailing parenthesis\n" +
      " if (token != ')')\n" +
      "   throw new ParseException(\"wrong number of arguments to |\");\n";
    
    int count = 5000;
    final StringBuilder bigIn = new StringBuilder(input.length() * count);
    final StringBuilder bigExp = new StringBuilder(expected.length() * count);
    for (int i = 0; i < count; i++) {
      bigIn.append(input);
      bigExp.append(expected);
    }
    
    String result = DefinitionsDocument._removeTabs(bigIn.toString());
    assertEquals(bigExp.toString(), result);
  }
  
  /** Test whether tabs are removed as appropriate on call to insertString.
   */
  public void textTabRemovalOnInsertString2() throws BadLocationException {
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
      _doc.insertString(_doc.getLength(), inputs[i], null);
    }
    
    assertEquals(expected, _getAllText());
  }
  
  /** Test whether tabs are removed as appropriate on call to insertString. */
  public void textTabRemovalOnInsertString() throws BadLocationException {
    _doc.setIndent(1);
    _doc.insertString(0, " \t yet \t\tanother\ttest\t", null);
    String result = _doc.getText();
    
    if (_doc.tabsRemoved()) {
      assertEquals("   yet   another test ", result);
    }
    else { // Tabs should have been inserted.
      assertEquals(" \t yet \t\tanother\ttest\t", result);
    }
  }
  
  /** Test package-finding on empty document. */
  public void textPackageNameEmpty() throws InvalidPackageException {
    assertEquals("Package name for empty document", "", _doc.getPackageName());
  }
  
  /** Test package-finding on simple document, with no funny comments. */
  public void textPackageNameSimple()
    throws Exception
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
        _doc.insertString(0, curComment + "\n\n" + "package " + curPack + ";\nclass Foo { int x; }\n", null);
        
        assertEquals("Package name for document with comment " + curComment, curPack, _doc.getPackageName());
      }
    }
  }
  
  /** Test package-finding on document with a block comment between parts of package. */
  public void textPackageNameWeird1() throws BadLocationException, InvalidPackageException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava;";
    String normal = "edu.rice.cs.drjava";
    _doc.insertString(0, weird, null);
    
    assertEquals("Package name for weird: '" + weird + "'", normal, _doc.getPackageName());
  }
  
  /** Test package-finding on document with a line comment between parts of package. */
  public void textPackageNameWeird2() throws BadLocationException, InvalidPackageException {
    String weird = "package edu . rice //comment!\n.cs.drjava;";
    String normal = "edu.rice.cs.drjava";
    _doc.insertString(0, weird, null);
    
    assertEquals("Package name for weird: '" + weird + "'", normal, _doc.getPackageName());
  }
  
  /** Puts an otherwise valid package statement after a valid import declaration. This should result in seeing no 
    * package statement (for the purposes of getSourceRoot), so the resulting package name should be "".
    */
  public void textGetPackageNameWithPackageStatementAfterImport() throws BadLocationException, InvalidPackageException {
    String text = "import java.util.*;\npackage junk;\nclass Foo {}";
    _doc.insertString(0, text, null);
    assertEquals("Package name for text with package statement after import", "", _doc.getPackageName());
  }
  
  private String _getAllText() throws BadLocationException { return _doc.getText(); }
  
  /** Tests class name-finding on document. */
  public void textTopLevelClassName() throws BadLocationException, ClassNameNotFoundException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; class MyClass<T> implements O{";
    String result = "MyClass";
    _doc.insertString(0, weird, null);
    
    assertEquals("class name for weird: '" + weird + "'", result, _doc.getFirstTopLevelClassName());
  }
  
  /** Test interface name-finding on document */
  public void textTopLevelInterfaceName() throws BadLocationException, ClassNameNotFoundException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" + " interface thisInterface { \n" +
      " class MyClass {";
    String result = "thisInterface";
    _doc.insertString(0, weird, null);
    
    assertEquals("class name for interface: '" + weird + "'", result, _doc.getFirstTopLevelClassName());
  }
  
  /** Test class name-finding on document */
  public void textTopLevelClassNameWComments() throws BadLocationException, ClassNameNotFoundException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      "/* class Y */ \n" +
      " /* class Foo \n" +
      " * class Bar \n" +
      " interface Baz \n" +
      " */ \n" +
      "//class Blah\n" +
      "class MyClass {";
    
    String result = "MyClass";
    _doc.insertString(0, weird, null);
    
    assertEquals("class name for class: '" + weird + "'", result, _doc.getFirstTopLevelClassName());
  }
  
  /** Tests that a keyword with no space following it does not cause a StringOutOfBoundsException (bug 742226). */
  public void textTopLevelClassNameNoSpace() throws BadLocationException {
    String c = "class";
    _doc.insertString(0, c, null);
    try {
      _doc.getFirstTopLevelClassName();
      fail("Should not have found a class name");
    }
    catch (ClassNameNotFoundException e) {
      // Good, we expect this
    }
  }
  
  /** Tests that the word class is not recognized if it is not followed
   * by whitespace.
   */
  public void textTopLevelClassNameWithClassloaderImport()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "import classloader.class; class MyClass {";
    String result = "MyClass";
    _doc.insertString(0, weird, null);
    
    assertEquals("class name for weird: '" + weird + "'", result, _doc.getFirstTopLevelClassName());
  }
  
  /** Tests class name-finding on document. */
  public void textTopLevelClassNameMisleading() throws BadLocationException, ClassNameNotFoundException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " {class X} \n" +
      " interface thisInterface { \n" +
      " class MyInnerClass {";
    String result = "thisInterface";
    _doc.insertString(0, weird, null);
    
    assertEquals("class name for interface: '" + weird + "'",
                 result,
                 _doc.getFirstTopLevelClassName());
  }
  
  /** Tests class name-finding on document */
  public void textTopLevelInterfaceNameMisleading() throws BadLocationException, ClassNameNotFoundException {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" + " {interface X} " + " \"class Foo\"" +
      " class MyClass {";
    String result = "MyClass";
    _doc.insertString(0, weird, null);
    
    assertEquals("class name for user interface: '" + weird + "'", result, _doc.getFirstTopLevelClassName());
  }
  
  /** Tests class name-finding on document */
  public void textTopLevelInterfaceNameMisleading2() throws BadLocationException, ClassNameNotFoundException {
    String weird = "package edu . rice\n./*interface comment!*/cs.drjava; \n" + " {interface X<T>} " +
      " \"class interface Foo\"" + " class MyClass extends Foo<T> {";
    String result = "MyClass";
    _doc.insertString(0, weird, null);
    
    assertEquals("class name for user interface: '" + weird + "'", result, _doc.getFirstTopLevelClassName());
  }
  
  /** Tests class name-finding on document. */
  public void textTopLevelInterfaceNameBeforeClassName()
    throws BadLocationException, ClassNameNotFoundException
  {
    String weird = "package edu . rice\n./*comment!*/cs.drjava; \n" +
      " interface thisInterface { \n" +
      "  } \n" +
      " class thatClass {\n" +
      "  }";
    String result = "thisInterface";
    _doc.insertString(0, weird, null);
    
    assertEquals("interface should have been chosen, rather than the class: '" + weird + "'",
                 result,
                 _doc.getFirstTopLevelClassName());
  }
  
  /** Tests class name-finding on document. */
  public void textTopLevelClassNameWithDelimiters() throws BadLocationException, ClassNameNotFoundException {
    String weird1 = "package edu . rice\n./*comment!*/cs.drjava; \n" + " class MyClass<T> {";
    String result1 = "MyClass";
    _doc.insertString(0, weird1, null);
    
    assertEquals("generics should be removed: '" + weird1 + "'", result1, _doc.getFirstTopLevelClassName());
    
    String weird2 = "package edu . rice\n./*comment!*/cs.drjava; \n" + " class My_Class {";
    String result2 = "My_Class";
    _doc.insertString(0, weird2, null);
    
    assertEquals("underscores should remain: '" + weird1 + "'", result2, _doc.getFirstTopLevelClassName());
  }
  
  /** Tests that the name of a top level enclosing class can be found. */
  public void textTopLevelEnclosingClassName() throws BadLocationException, ClassNameNotFoundException {
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
    
    _doc.insertString(0, classes, null);
    
    // No enclosing class at start
    try {
      @SuppressWarnings("unused") String result = _doc.getEnclosingTopLevelClassName(3);
      fail("no enclosing class should be found at start");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
    
    // No enclosing class before open brace
    try {
      _doc.getEnclosingTopLevelClassName(15);
      fail("no enclosing class should be found before open brace");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
    
    try {
      @SuppressWarnings("unused") String result = _doc.getEnclosingTopLevelClassName(186);
      fail("no enclosing class should be found at end of file");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
    
    assertEquals("top level class name after first open brace", "C1", _doc.getEnclosingTopLevelClassName(22));
    assertEquals("top level class name inside C1", "C1", _doc.getEnclosingTopLevelClassName(26));
    assertEquals("top level class name inside method of C1", "C1", _doc.getEnclosingTopLevelClassName(42));
    assertEquals("top level class name on C2's brace", "C1", _doc.getEnclosingTopLevelClassName(58));
    assertEquals("top level class name after C2's brace", "C1", _doc.getEnclosingTopLevelClassName(59));
    assertEquals("top level class name inside C2", "C1", _doc.getEnclosingTopLevelClassName(68));
    assertEquals("top level class name inside C3", "C1", _doc.getEnclosingTopLevelClassName(92));
    assertEquals("top level class name after C3's close brace", "C1", _doc.getEnclosingTopLevelClassName(93));
    assertEquals("top level class name after C2's close brace", "C1", _doc.getEnclosingTopLevelClassName(100));
    
    // No enclosing class between classes
    try {
      _doc.getEnclosingTopLevelClassName(107);
      fail("no enclosing class should be found between classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
    
    assertEquals("class name inside C4", "C4", _doc.getEnclosingTopLevelClassName(122));
    assertEquals("class name inside C5", "C4", _doc.getEnclosingTopLevelClassName(135));
    assertEquals("class name inside C6", "C4", _doc.getEnclosingTopLevelClassName(167));
    assertEquals("class name inside C7", "C7", _doc.getEnclosingTopLevelClassName(185));
    
    // No enclosing class at end
    try {
      @SuppressWarnings("unused") String result = _doc.getEnclosingTopLevelClassName(186);
      fail("no enclosing class should be found at end");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
  }
  
  /** Tests that the correct qualified class name is returned with a package. */
  public void textQualifiedClassNameWithPackage() throws BadLocationException, ClassNameNotFoundException {
    String classes =
      "package foo;\n" +  // 13
      "class C1 {}\n" +  // 25
      "class C2 {}";  // 36
    _doc.insertString(0, classes, null);
    
    assertEquals("qualified class name without pos", "foo.C1", _doc.getQualifiedClassName());
    assertEquals("enclosing class name in C1", "C1", _doc.getEnclosingTopLevelClassName(23));
    assertEquals("qualified class name with pos in C1", "foo.C1", _doc.getQualifiedClassName(23));
    assertEquals("qualified class name with pos in C2", "foo.C2", _doc.getQualifiedClassName(35));
    
    // No class name outside classes
    try {
      _doc.getQualifiedClassName(15);
      fail("no qualified class name should be found outside classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
  }
  
  /** Tests that the correct qualified class name is returned without a package. */
  public void textQualifiedClassNameWithoutPackage() throws BadLocationException, ClassNameNotFoundException {
    String classes =
      "class C1 {}\n" +  // 12
      "class C2 {}";  // 36
    _doc.insertString(0, classes, null);
    
    assertEquals("qualified class name without pos", "C1", _doc.getQualifiedClassName());
    assertEquals("qualified class name with pos in C1", "C1", _doc.getQualifiedClassName(10));
    assertEquals("qualified class name with pos in C2", "C2", _doc.getQualifiedClassName(22));
    
    // No class name outside classes
    try {
      _doc.getQualifiedClassName(15);
      fail("no qualified class name should be found outside classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
      // Correct: no class name found
    }
  }
  
  /** Tests that the name of an enclosing class can be found.
    *
    * Note: I started to write this assuming that we would need to find
    * inner class names, but I'm not sure that's the case.  I'm writing
    * the method for the debugger, which only needs the *top level*
    * enclosing class.  Rather than delete this test, though, I'll leave
    * it in case we ever need to write getEnclosingClassName().
    *
    public void testEnclosingClassName() throws BadLocationException, ClassNameNotFoundException {
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
    _doc.insertString(0, classes, null);
    
    // No enclosing class at start
    try {
    String result = _doc.getEnclosingClassName(3);
    fail("no enclosing class should be found at start");
    }
    catch (ClassNameNotFoundException cnnfe) {
    // Correct: no class name found
    }
    
    // No enclosing class before open brace
    try {
    String result = _doc.getEnclosingClassName(15);
    fail("no enclosing class should be found before open brace");
    }
    catch (ClassNameNotFoundException cnnfe) {
    // Correct: no class name found
    }
    
    assertEquals("class name after first open brace", "C1",
    _doc.getEnclosingClassName(22));
    assertEquals("class name inside C1", "C1",
    _doc.getEnclosingClassName(26));
    assertEquals("class name inside method of C1", "C1",
    _doc.getEnclosingClassName(42));
    assertEquals("class name on C2's brace", "C1",
    _doc.getEnclosingClassName(58));
    assertEquals("class name after C2's brace", "C2",
    _doc.getEnclosingClassName(59));
    assertEquals("class name inside C2", "C2",
    _doc.getEnclosingClassName(68));
    assertEquals("class name inside C3", "C3",
    _doc.getEnclosingClassName(92));
    assertEquals("class name after C3's close brace", "C2",
    _doc.getEnclosingClassName(93));
    assertEquals("class name after C2's close brace", "C1",
    _doc.getEnclosingClassName(100));
    
    // No enclosing class between classes
    try {
    String result = _doc.getEnclosingClassName(107);
    fail("no enclosing class should be found between classes");
    }
    catch (ClassNameNotFoundException cnnfe) {
    // Correct: no class name found
    }
    
    assertEquals("class name inside C4", "C4",
    _doc.getEnclosingClassName(118));
    assertEquals("class name inside C5", "C5",
    _doc.getEnclosingClassName(130));
    
    // No enclosing class at end
    try {
    String result = _doc.getEnclosingClassName(131);
    fail("no enclosing class should be found at end");
    }
    catch (ClassNameNotFoundException cnnfe) {
    // Correct: no class name found
    }
    }*/
  
  /** Verify that undoing a multiple-line indent will be a single undo action
    * @throws BadLocationException
    */
  public void textUndoAndRedoAfterMultipleLineIndent() throws BadLocationException {  //this fails
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
    
    _doc.addUndoableEditListener(_doc.getUndoManager());
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,Integer.valueOf(2));
//    Utilities.clearEventQueue();
    _doc.insertString(0, text, null);
    assertEquals("insertion",text, _doc.getText()); 
    /* This is necessary here and other places where indenting or commenting takes place because the undoListener in DefinitionsPane 
     * currently starts compound edits, but here, there's no DefinitionsPane.
     * Perhaps there's some way to factor the undoListener in CompoundUndoManager to be the one that starts compound edits 
     * so that it will work with or without the view.
     */
    _doc.getUndoManager().startCompoundEdit();
    indentLines(0,_doc.getLength());
    assertEquals("indenting",indented, _doc.getText());
    _doc.getUndoManager().undo();
    assertEquals("undo",text, _doc.getText());
    _doc.getUndoManager().redo();
    assertEquals("redo",indented, _doc.getText());
  }
  
  /** Verify that undoing a multiple-line indent will be a single undo action
    * @throws BadLocationException
    */
  public void textUndoAndRedoAfterMultipleLineCommentAndUncomment()
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
    
    _doc.addUndoableEditListener(_doc.getUndoManager());
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,Integer.valueOf(2));
//    Utilities.clearEventQueue();
    _doc.insertString(0,text,null);
    assertEquals("insertion",text, _doc.getText());
    
    _doc.getUndoManager().startCompoundEdit();
    _doc.commentLines(0,_doc.getLength());
    assertEquals("commenting",commented, _doc.getText());
    _doc.getUndoManager().undo();
    assertEquals("undo commenting",text, _doc.getText());
    _doc.getUndoManager().redo();
    assertEquals("redo commenting",commented, _doc.getText());
    
    _doc.getUndoManager().startCompoundEdit();
    _doc.uncommentLines(0,_doc.getLength());
    assertEquals("uncommenting",text, _doc.getText());
    _doc.getUndoManager().undo();
    assertEquals("undo uncommenting",commented, _doc.getText());
    _doc.getUndoManager().redo();
    assertEquals("redo uncommenting",text, _doc.getText());
  }
  
  /** Verify that uncommenting an empty document does not crash
    * @throws BadLocationException
    */
  public void testUncommentEmpty()
    throws BadLocationException {
    String text = "";
    
    _doc.uncommentLines(0,_doc.getLength());
    assertEquals("uncommenting",text, _doc.getText());
  }
  
  /** Test method for CompoundUndoManager.  Tests that the nested compound edit functionality works correctly.
    * @throws BadLocationException
    */
  public void textCompoundUndoManager() throws BadLocationException {
    String text =
      "public class foo {\n" +
      "int bar;\n" +
      "}";
    
    String indented =
      "public class foo {\n" +
      "  int bar;\n" +
      "}";
    CompoundUndoManager undoManager = _doc.getUndoManager();
    
    _doc.addUndoableEditListener(undoManager);
    DrJava.getConfig().setSetting(OptionConstants.INDENT_LEVEL,Integer.valueOf(2));
//    Utilities.clearEventQueue();
    // 1
    
    // Start a compound edit and verify the returned key
    int key = undoManager.startCompoundEdit();
    assertEquals("Should have returned the correct key.", 0, key);
    
    // Insert a test string into the document
    _doc.insertString(0, text, null);
    assertEquals("Should have inserted the text properly.", text, _doc.getText());
    
    // Indent the lines, so as to trigger a nested compound edit
    undoManager.startCompoundEdit();
    
    indentLines(0, _doc.getLength());
    assertEquals("Should have indented correctly.", indented,  _doc.getText());
    
    undoManager.undo();
    assertEquals("Should have undone correctly.", "",  _doc.getText());
    
    // 2
    
    String commented =
      "//public class foo {\n" +
      "//  int bar;\n" +
      "//}";
    
    // Start a compound edit and verify the returned key
    key = _doc.getUndoManager().startCompoundEdit();
    assertEquals("Should have returned the correct key.", 2, key);
    
    // Insert a test string into the document
    _doc.insertString(0, text, null);
    assertEquals("Should have inserted the text properly.", text,
                 _doc.getText());
    
    // Indent the lines, so as to trigger a nested compond edit
    indentLines(0, _doc.getLength());
    assertEquals("Should have indented correctly.", indented,
                 _doc.getText());
    
    undoManager.startCompoundEdit();
    _doc.commentLines(0, _doc.getLength());
    assertEquals("Should have commented correctly.", commented,
                 _doc.getText());
    
    // Undo the second compound edit
    _doc.getUndoManager().undo();
    assertEquals("Should have undone the commenting.", indented,
                 _doc.getText());
    
    // Undo the first compound edit
    _doc.getUndoManager().undo();
    assertEquals("Should have undone the indenting and inserting.", "",
                 _doc.getText());
    
    // 3
    
    // Start a compound edit and verify the returned key
    key = _doc.getUndoManager().startCompoundEdit();
    assertEquals("Should have returned the correct key.", 4, key);
    
    // Insert a test string into the document
    _doc.insertString(0, text, null);
    assertEquals("Should have inserted the text properly.", text,
                 _doc.getText());
    
    // Indent the lines, so as to trigger a nested compond edit
    indentLines(0, _doc.getLength());
    assertEquals("Should have indented correctly.", indented,
                 _doc.getText());
    
//    // Try to undo the nested edit
//    try {
//      _doc.getUndoManager().undo();
//      fail("Should not have allowed undoing a nested edit.");
//    }
//    catch (CannotUndoException e) {
//      // Correct: cannot undo a nested edit
//    }
//
//    try {
//      _doc.getUndoManager().redo();
//      fail("Should not have allowed redoing a nested edit.");
//    }
//    catch (CannotRedoException cre) {
//      // Correct: cannot redo a nested edit
//    }
//
    // Try end the compound edit with a wrong key
    try {
      _doc.getUndoManager().endCompoundEdit(key + 2);
//      fail("Should not have allowed ending a compound edit with a wrong key.");
    }
    catch (IllegalStateException e) {
      assertEquals("Should have printed the correct error message.",
                   "Improperly nested compound edits.", e.getMessage());
    }
    
    // Indent the lines, so as to trigger a nested compound edit
    undoManager.startCompoundEdit();
    indentLines(0, _doc.getLength());
    assertEquals("Should have indented correctly.", indented, _doc.getText());
    
    // We've taken out this part of the test because of our change to
    // undo where we close the nearest open compound edit upon undo-ing,
    // pasting, commenting, un-commenting, indenting, and backspacing.
    // We should never have a nested edit anymore.
    
    // Try to undo the nested edit
//    try {
//      _doc.getUndoManager().undo();
//      fail("Should not have allowed undoing a nested edit.");
//    }
//    catch (CannotUndoException e) {
//      // Correct: cannot undo a nested edit
//    }
    
    // End the compound edit and undo
//    _doc.getUndoManager().endCompoundEdit(key);
    _doc.getUndoManager().undo();
    assertEquals("Should have undone the indenting and inserting.", "", _doc.getText());
  }
  
  /** Verifies that the undo manager correctly determines if the document has
   * been modified since the last save.
   */
  public void textUndoOrRedoSetsUnmodifiedState() throws BadLocationException {
    _doc.addUndoableEditListener(_doc.getUndoManager());
    _doc.insertString(0, "This is text", null);
    assertTrue("Document should be modified.", _doc.isModifiedSinceSave());
    _doc.getUndoManager().undo();
    _doc.updateModifiedSinceSave();
    assertFalse("Document should no longer be modified after undo.", _doc.isModifiedSinceSave());
    _doc.insertString(0, "This is text", null);
    _doc.resetModification();
    assertFalse("Document should not be modified after \"save\".", _doc.isModifiedSinceSave());
    _doc.getUndoManager().undo();
    _doc.updateModifiedSinceSave();
    assertTrue("Document should be modified after undo.", _doc.isModifiedSinceSave());
    _doc.getUndoManager().redo();
    _doc.updateModifiedSinceSave();
    assertFalse("Document should no longer be modified after redo.", _doc.isModifiedSinceSave());
  }
  
  protected static final String NEWLINE = "\n"; // Was StringOps.EOL;but swing usees '\n' for newLine
  
  protected static final String NESTED_CLASSES_TEXT =
    "/*bof*/package Temp" + NEWLINE +
    "" + NEWLINE +
    "class Test(var i: Int) {" + NEWLINE +
    "  " + NEWLINE +
    "  def foo(other:Test) {" + NEWLINE +
    "    i = other.i" + NEWLINE +
    "    " + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  def bar() {" + NEWLINE +
    "    println(i)" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  trait Interf {" + NEWLINE +
    "    static long C = System.currentTimeMillis();" + NEWLINE +
    "    def act();" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  class Implementor extends Interf {" + NEWLINE +
    "    override def act() { /*Implementor.act*/" + NEWLINE +
    "      println(C);" + NEWLINE +
    "      val inter = new Interf() { /*Implementor$1*/" + NEWLINE +
    "        override def act() {" + NEWLINE +
    "          println(\"Test$Implementor$1\")" + NEWLINE +
    "          val inter = new Interf() { /*Implementor$1$1*/" + NEWLINE +
    "            override def act() {" + NEWLINE +
    "              println(\"Test$Implementor$1$1\");" + NEWLINE +
    "            }" + NEWLINE +
    "          };" + NEWLINE +
    "          val inn = new Inner[Integer]() { /*Implementor$1$2*/" + NEWLINE +
    "            override def set(t: Int) { _t = t }" + NEWLINE +
    "          }" + NEWLINE +
    "        } /*b-Implementor$1*/" + NEWLINE +
    "      } /*b-Implementor*/" + NEWLINE +
    "    } /*c-Implementor*/" + NEWLINE +
    "    " + NEWLINE +
    "    abstract class Inner[T] { /*Implementor$Inner*/" + NEWLINE +
    "      var _t : T; /*b-Implementor$Inner*/" + NEWLINE +
    "      def set(t:T)" + NEWLINE +
    "    }" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  public void anon() { /*anon()*/" + NEWLINE +
    "    Interf inter = new Interf() { /*Test$1*/" + NEWLINE +
    "      class NamedInAnonymous implements Interf { /*Test$1$NamedInAnonymous*/" + NEWLINE +
    "        public void act() {" + NEWLINE +
    "          System.out.println(\"Test$1$NamedInAnonymous\");" + NEWLINE +
    "        }" + NEWLINE +
    "      }" + NEWLINE +
    "      public void act() { /*b-Test$1*/" + NEWLINE +
    "        System.out.println(\"Test$1\");" + NEWLINE +
    "        NamedInAnonymous nia = new NamedInAnonymous();" + NEWLINE +
    "        nia.act();" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "    inter.act(); /*b-anon()*/" + NEWLINE +
    "    Interf inter2 = new Interf() { /*Test$2*/" + NEWLINE +
    "      public void act() {" + NEWLINE +
    "        System.out.println(\"Test$2\");" + NEWLINE +
    "        Interf inter = new Interf() { /*Test$2$1*/" + NEWLINE +
    "          public void act() {" + NEWLINE +
    "            System.out.println(\"Test$2$1\");" + NEWLINE +
    "          }" + NEWLINE +
    "        };" + NEWLINE +
    "        inter.act();" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "    inter2.act();" + NEWLINE +
    "    Interf inter3 = new Implementor() { /*Test$3*/" + NEWLINE +
    "      public void act() {" + NEWLINE +
    "        System.out.println(\"Test$3\");" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "    inter3.act();" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  public Test(int j) { if (true) { i = j; } }" + NEWLINE +
    "  " + NEWLINE +  
    "  protected abstract class Param<T> {" + NEWLINE +  
    "    T _t;" + NEWLINE +  
    "    public Param(T t, T t2) { _t = t; }" + NEWLINE +  
    "    public abstract void paramDo();" + NEWLINE +  
    "  }" + NEWLINE +  
    "  " + NEWLINE +
    "  public void anon2() {" + NEWLINE +
    "    Param<Interf> p = new Param<Interf>(/*anon2()*/new Interf() { /*Test$4*/" + NEWLINE +
    "      public void act() {" + NEWLINE +
    "        System.out.println(\"parameter 1 = Test$4\");" + NEWLINE +
    "        Interf i = new Interf() { /*Test$4$1*/" + NEWLINE +
    "          public void act() {" + NEWLINE +
    "            System.out.println(\"Test$4$1\");" + NEWLINE +
    "          }" + NEWLINE +
    "        };" + NEWLINE +
    "      }" + NEWLINE +
    "    }, /*b-anon2()*/ new Interf() { /*Test$5*/" + NEWLINE +
    "      public void act() {" + NEWLINE +
    "        System.out.println(\"parameter 2 = Test$5\");" + NEWLINE +
    "      }" + NEWLINE +
    "    }) /*c-anon2()*/ { /*Test$6*/" + NEWLINE +
    "      public void paramDo() {" + NEWLINE +
    "        System.out.println(\"Test$6\");" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "  }" + NEWLINE +
    "" + NEWLINE +
    "  public void anon3() {" + NEWLINE +
    "    Param<Interf> p = new Param<Interf>(/*anon3()*/new Interf() { /*Test$7*/" + NEWLINE +
    "      class NamedClassAgain {" + NEWLINE +
    "        void doSomething() { System.out.println(\"doSomething\"); }" + NEWLINE +
    "      }" + NEWLINE +
    "      public void act() {" + NEWLINE +
    "        System.out.println(\"parameter 3 = Test$7\");" + NEWLINE +
    "        Interf i = new Interf() { /*Test$7$1*/" + NEWLINE +
    "          public void act() {" + NEWLINE +
    "            System.out.println(\"Test$7$1\");" + NEWLINE +
    "          }" + NEWLINE +
    "        };" + NEWLINE +
    "      }" + NEWLINE +
    "    }) /*c-anon2()*/ { /*Test$8*/" + NEWLINE +
    "      public void paramDo() {" + NEWLINE +
    "        System.out.println(\"Test$8\");" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  public static void main(String[] args) {" + NEWLINE +
    "    Test t1 = new Test(1);" + NEWLINE +
    "    t1.bar();" + NEWLINE +
    "    Test t2 = new Test(123);" + NEWLINE +
    "    t2.bar();" + NEWLINE +
    "    t1.foo(t2);" + NEWLINE +
    "    t1.bar();" + NEWLINE +
    "    Implementor imp = new Implementor();" + NEWLINE +
    "    imp.act();" + NEWLINE +
    "    t1.anon();" + NEWLINE +
    "  }" + NEWLINE +
    "  public static class Outer {" + NEWLINE +
    "    public static interface Inner {" + NEWLINE +
    "      public void innerDo();" + NEWLINE +
    "    }" + NEWLINE +
    "    public static interface InnerParam<T> {" + NEWLINE +
    "      public void innerParam(T t);" + NEWLINE +
    "    }" + NEWLINE +
    "    public class Middle<T> {" + NEWLINE +
    "      T t;" + NEWLINE +
    "      public abstract class Innerst {" + NEWLINE +
    "        public abstract void innerstDo();" + NEWLINE +
    "      }" + NEWLINE +
    "      " + NEWLINE +
    "      Innerst i = new Outer.Middle.Innerst() { /*Test$Outer$Middle$1*/" + NEWLINE +
    "        public void innerstDo() {" + NEWLINE +
    "          System.out.println(\"Test$Outer$Middle$1\");" + NEWLINE +
    "        }" + NEWLINE +
    "      };" + NEWLINE +
    "    }" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  public void anonDotTest() {" + NEWLINE +
    "    Outer.Inner test = new Outer.Inner() { /*Test$9*/" + NEWLINE +
    "      public void innerDo() {" + NEWLINE +
    "        System.out.println(\"Test$9\");" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "    Outer.InnerParam<String> test2 = new Outer.InnerParam<String>() { /*Test$10*/" + NEWLINE +
    "      public void innerParam(String t) {" + NEWLINE +
    "        System.out.println(\"Test$10\");" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "  }" + NEWLINE +
    "}" + NEWLINE +
    "" + NEWLINE +
    "class Foo {" + NEWLINE +
    "  public void foo() {" + NEWLINE +
    "    System.out.println(\"foo\");" + NEWLINE +
    "    FooImplementor fimp = new FooImplementor();" + NEWLINE +
    "    fimp.act();" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  static interface FooInterf {" + NEWLINE +
    "    static long C = System.currentTimeMillis(); /*Foo$FooInterf*/" + NEWLINE +
    "    public void act();" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  public static class FooImplementor implements FooInterf { /*Foo$FooImplementor*/" + NEWLINE +
    "    public void act() {" + NEWLINE +
    "      System.out.println(C); /*b-Foo$FooImplementor*/" + NEWLINE +
    "      FooInterf inter = new FooInterf() { /*Foo$FooImplementor$1*/" + NEWLINE +
    "        public void act() {" + NEWLINE +
    "          System.out.println(\"Foo$FooImplementor$1\");" + NEWLINE +
    "          FooInterf inter = new FooInterf() { /*Foo$FooImplementor$1$1*/" + NEWLINE +
    "            public void act() {" + NEWLINE +
    "              System.out.println(\"Foo$FooImplementor$1$1\");" + NEWLINE +
    "            }" + NEWLINE +
    "          };" + NEWLINE +
    "        }" + NEWLINE +
    "      };" + NEWLINE +
    "    }" + NEWLINE +
    "    public class Inner<T> { /*Foo$FooImplementor$Inner*/" + NEWLINE +
    "      T t;" + NEWLINE +
    "    }" + NEWLINE +
    "  }" + NEWLINE +
    "}" + NEWLINE +
    "abstract class Fee {" + NEWLINE +
    "  public abstract void feeDo();" + NEWLINE +
    "  public abstract void feeAct();" + NEWLINE +
    "  protected String s, t, u;" + NEWLINE +
    "  " + NEWLINE +
    "  public static class FeeConc extends Fee {/*Fee$FeeConc*/" + NEWLINE +
    "    {" + NEWLINE +
    "      s = \"FeeConc/s\";" + NEWLINE +
    "    }" + NEWLINE +
    "    public void feeDo() { System.out.println(\"FeeConc/feeDo\"); }" + NEWLINE +
    "    {" + NEWLINE +
    "      t = \"FeeConc/t\";" + NEWLINE +
    "    }" + NEWLINE +
    "    public abstract void feeAct() { System.out.println(\"FeeConc/feeAct\"); }" + NEWLINE +
    "    {" + NEWLINE +
    "      u = \"FeeConc/u\";" + NEWLINE +
    "    }" + NEWLINE +
    "  }" + NEWLINE +
    "  " + NEWLINE +
    "  public static void main(String[] args) {" + NEWLINE +
    "    Fee f = new Fee() {/*Fee$1*/" + NEWLINE +
    "      {" + NEWLINE +
    "        s = \"Fee$1/s\";" + NEWLINE +
    "      }" + NEWLINE +
    "      public void feeDo() { System.out.println(\"Fee$1/feeDo\"); }" + NEWLINE +
    "      {" + NEWLINE +
    "        t = \"Fee$1/t\";" + NEWLINE +
    "      }" + NEWLINE +
    "      public abstract void feeAct() { System.out.println(\"Fee$1/feeAct\"); }" + NEWLINE +
    "      {" + NEWLINE +
    "        u = \"Fee$1/u\";" + NEWLINE +
    "      }" + NEWLINE +
    "    };" + NEWLINE +
    "  }" + NEWLINE +
    "}/*eof*/" + NEWLINE;
  
  /** Test finding anonymous class index on document.  TODO: path this for Scala. */
  public void xtestAnonymousClassIndex() throws BadLocationException, ClassNameNotFoundException {
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          _doc.insertString(0, NESTED_CLASSES_TEXT, null);
          
          String substr;
          int exp, act;
          substr = "{ /*Test$4*/";
          exp = 4;
          act = _doc._getAnonymousInnerClassIndex(NESTED_CLASSES_TEXT.indexOf(substr)); 
          assertEquals("index at " + substr + " exp=`" + exp + "`, act=`" + act + "`", exp, act);
          
//    SySystem.err.println(NESTED_CLASSES_TEXT);
          substr = "{ /*Test$5*/";
          exp = 5;
          act = _doc._getAnonymousInnerClassIndex(NESTED_CLASSES_TEXT.indexOf(substr)); 
          assertEquals("index at " + substr + " exp=`" + exp + "`, act=`" + act + "`", exp, act);
          
          substr = "{ /*Test$6*/";
          exp = 6;
          act = _doc._getAnonymousInnerClassIndex(NESTED_CLASSES_TEXT.indexOf(substr)); 
          assertEquals("index at " + substr + " exp=`" + exp + "`, act=`" + act + "`", exp, act);
          
          substr = "{ /*Test$7*/";
          exp = 7;
          act = _doc._getAnonymousInnerClassIndex(NESTED_CLASSES_TEXT.indexOf(substr)); 
          assertEquals("index at " + substr + " exp=`" + exp + "`, act=`" + act + "`", exp, act);
          
          substr = "{ /*Test$8*/";
          exp = 8;
          act = _doc._getAnonymousInnerClassIndex(NESTED_CLASSES_TEXT.indexOf(substr)); 
          assertEquals("index at " + substr + " exp=`" + exp + "`, act=`" + act + "`", exp, act);
        }
        catch(BadLocationException e) { throw new UnexpectedException(e); }
        catch(ClassNameNotFoundException e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  /** Test exact class name-finding on document. */
  public void xtestExactClassName() throws BadLocationException, ClassNameNotFoundException {
    
    Utilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          _doc.insertString(0, NESTED_CLASSES_TEXT, null);
          String substr, exp1, exp2, act1, act2;
          substr = "private int i";
          exp1   = "Temp.Test";    
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "= other.i";
          exp1   = "Temp.Test";    
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "System.out.println(i)";
          exp1   = "Temp.Test";    
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "System.currentTimeMillis";
          exp1   = "Temp.Test$Interf";
          exp2   = "Interf";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "Implementor implements Interf";
          exp1   = "Temp.Test";    
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Implementor.act*/";
          exp1   = "Temp.Test$Implementor";
          exp2   = "Implementor";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Implementor$1*/";
          exp1   = "Temp.Test$Implementor$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$Implementor$1\"";
          exp1   = "Temp.Test$Implementor$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Implementor$1$1*/";
          exp1   = "Temp.Test$Implementor$1$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$Implementor$1$1\"";
          exp1   = "Temp.Test$Implementor$1$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Implementor$1$2*/";
          exp1   = "Temp.Test$Implementor$1$2";
          exp2   = "2";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*b-Implementor$1*/";
          exp1   = "Temp.Test$Implementor$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*b-Implementor*/";
          exp1   = "Temp.Test$Implementor";
          exp2   = "Implementor";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*c-Implementor*/";
          exp1   = "Temp.Test$Implementor";
          exp2   = "Implementor";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Implementor$Inner*/";
          exp1   = "Temp.Test$Implementor$Inner";
          exp2   = "Inner";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*b-Implementor$Inner*/";
          exp1   = "Temp.Test$Implementor$Inner";
          exp2   = "Inner";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*anon()*/";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$1*/";
          exp1   = "Temp.Test$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$1$NamedInAnonymous*/";
          exp1   = "Temp.Test$1$NamedInAnonymous";
          exp2   = "NamedInAnonymous";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$1$NamedInAnonymous\"";
          exp1   = "Temp.Test$1$NamedInAnonymous";
          exp2   = "NamedInAnonymous";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*b-Test$1*/";
          exp1   = "Temp.Test$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$1\"";
          exp1   = "Temp.Test$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*b-anon()*/";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$2*/";
          exp1   = "Temp.Test$2";
          exp2   = "2";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$2\"";
          exp1   = "Temp.Test$2";
          exp2   = "2";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$2$1*/";
          exp1   = "Temp.Test$2$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$2$1\"";
          exp1   = "Temp.Test$2$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$3*/";
          exp1   = "Temp.Test$3";
          exp2   = "3";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$3\"";
          exp1   = "Temp.Test$3";
          exp2   = "3";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "(true) { i = j; }";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "new Test(1)";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "class Foo";
          exp1   = "";
          exp2   = "";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "FooImplementor fimp";
          exp1   = "Temp.Foo";
          exp2   = "Foo";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Foo$FooInterf*/";
          exp1   = "Temp.Foo$FooInterf";
          exp2   = "FooInterf";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Foo$FooImplementor*/";
          exp1   = "Temp.Foo$FooImplementor";
          exp2   = "FooImplementor";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*b-Foo$FooImplementor*/";
          exp1   = "Temp.Foo$FooImplementor";
          exp2   = "FooImplementor";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Foo$FooImplementor$1*/";
          exp1   = "Temp.Foo$FooImplementor$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Foo$FooImplementor$1\"";
          exp1   = "Temp.Foo$FooImplementor$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Foo$FooImplementor$1$1*/";
          exp1   = "Temp.Foo$FooImplementor$1$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Foo$FooImplementor$1$1\"";
          exp1   = "Temp.Foo$FooImplementor$1$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Foo$FooImplementor$Inner*/";
          exp1   = "Temp.Foo$FooImplementor$Inner";
          exp2   = "Inner";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*eof*/";
          exp1   = "";
          exp2   = "";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*bof*/";
          exp1   = "";
          exp2   = "";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "public class Test";
          exp1   = "";
          exp2   = "";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*anon2()*/";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$4*/";
          exp1   = "Temp.Test$4";
          exp2   = "4";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"parameter 1 = Test$4\"";
          exp1   = "Temp.Test$4";
          exp2   = "4";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$4$1*/";
          exp1   = "Temp.Test$4$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$4$1\"";
          exp1   = "Temp.Test$4$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*b-anon2()*/";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$5*/";
          exp1   = "Temp.Test$5";
          exp2   = "5";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"parameter 2 = Test$5\"";
          exp1   = "Temp.Test$5";
          exp2   = "5";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*c-anon2()*/";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$6*/";
          exp1   = "Temp.Test$6";
          exp2   = "6";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$6\"";
          exp1   = "Temp.Test$6";
          exp2   = "6";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*anon3()*/";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$7*/";
          exp1   = "Temp.Test$7";
          exp2   = "7";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"doSomething\"";
          exp1   = "Temp.Test$7$NamedClassAgain";
          exp2   = "NamedClassAgain";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"parameter 3 = Test$7\"";
          exp1   = "Temp.Test$7";
          exp2   = "7";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$7$1*/";
          exp1   = "Temp.Test$7$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$7$1\"";
          exp1   = "Temp.Test$7$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*c-anon2()*/";
          exp1   = "Temp.Test";
          exp2   = "Test";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$8*/";
          exp1   = "Temp.Test$8";
          exp2   = "8";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$8\"";
          exp1   = "Temp.Test$8";
          exp2   = "8";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "abstract void feeDo()";
          exp1   = "Temp.Fee";
          exp2   = "Fee";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "class FeeConc extends Fee";
          exp1   = "Temp.Fee";
          exp2   = "Fee";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Fee$FeeConc*/";
          exp1   = "Temp.Fee$FeeConc";
          exp2   = "FeeConc";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"FeeConc/feeDo\"";
          exp1   = "Temp.Fee$FeeConc";
          exp2   = "FeeConc";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"FeeConc/feeAct\"";
          exp1   = "Temp.Fee$FeeConc";
          exp2   = "FeeConc";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"FeeConc/s\"";
          exp1   = "Temp.Fee$FeeConc";
          exp2   = "FeeConc";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"FeeConc/t\"";
          exp1   = "Temp.Fee$FeeConc";
          exp2   = "FeeConc";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"FeeConc/u\"";
          exp1   = "Temp.Fee$FeeConc";
          exp2   = "FeeConc";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Fee$1*/";
          exp1   = "Temp.Fee$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Fee$1/feeDo\"";
          exp1   = "Temp.Fee$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Fee$1/feeAct\"";
          exp1   = "Temp.Fee$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Fee$1/s\"";
          exp1   = "Temp.Fee$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Fee$1/t\"";
          exp1   = "Temp.Fee$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Fee$1/u\"";
          exp1   = "Temp.Fee$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$Outer$Middle$1*/";
          exp1   = "Temp.Test$Outer$Middle$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$Outer$Middle$1\"";
          exp1   = "Temp.Test$Outer$Middle$1";
          exp2   = "1";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$9*/";
          exp1   = "Temp.Test$9";
          exp2   = "9";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$9\"";
          exp1   = "Temp.Test$9";
          exp2   = "9";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "/*Test$10*/";
          exp1   = "Temp.Test$10";
          exp2   = "10";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
          
          substr = "\"Test$10\"";
          exp1   = "Temp.Test$10";
          exp2   = "10";
          act1   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), true);
          act2   = _doc.getEnclosingClassName(NESTED_CLASSES_TEXT.indexOf(substr), false);
          assertEquals("class name at " + substr + " exp=`" + exp1 + "`, act=`" + act1 + "`", exp1, act1);
          assertEquals("class name at " + substr + " exp=`" + exp2 + "`, act=`" + act2 + "`", exp2, act2);
        }
        catch(BadLocationException e) { throw new UnexpectedException(e); }
        catch(ClassNameNotFoundException e) { throw new UnexpectedException(e); }
      }
    });
  }
  
  protected final String PUBLIC_CIE_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "public class C {" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "interface I { }" + NEWLINE +
    "enum E { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String PUBLIC_CEI_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "public class C {" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "enum E { }" + NEWLINE +
    "interface I { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String PUBLIC_ICE_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "public interface I {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "class C { }" + NEWLINE +
    "enum E { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String PUBLIC_IEC_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "public interface I {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "enum E { }" + NEWLINE +
    "class C { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String PUBLIC_ECI_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "public enum E {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "}" + NEWLINE +
    "class C { }" + NEWLINE +
    "interface I { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String PUBLIC_EIC_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "public enum E {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "}" + NEWLINE +
    "interface I { }" + NEWLINE +
    "class C { }" + NEWLINE +
    "}" + NEWLINE;
  
  // public after non-public
  protected final String I_PUBLIC_CE_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "interface I { }" + NEWLINE +
    "public class C {" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "enum E { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String E_PUBLIC_CI_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "enum E { }" + NEWLINE +
    "public class C {" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "interface I { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String C_PUBLIC_IE_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "class C { }" + NEWLINE +
    "public interface I {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "enum E { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String E_PUBLIC_IC_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "enum E { }" + NEWLINE +
    "public interface I {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "class C { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String C_PUBLIC_EI_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "class C { }" + NEWLINE +
    "public enum E {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "}" + NEWLINE +
    "interface I { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String I_PUBLIC_EC_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "interface I { }" + NEWLINE +
    "public enum E {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "}" + NEWLINE +
    "class C { }" + NEWLINE +
    "}" + NEWLINE;
  
  // no public class
  protected final String CIE_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "class C {" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "interface I { }" + NEWLINE +
    "enum E { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String CEI_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "class C {" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "enum E { }" + NEWLINE +
    "interface I { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String ICE_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "interface I {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "class C { }" + NEWLINE +
    "enum E { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String IEC_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "public interface I {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static enum NestedE { }" + NEWLINE +
    "}" + NEWLINE +
    "enum E { }" + NEWLINE +
    "class C { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String ECI_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "enum E {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "}" + NEWLINE +
    "class C { }" + NEWLINE +
    "interface I { }" + NEWLINE +
    "}" + NEWLINE;
  
  protected final String EIC_TEXT =
    "/*bof*/package temp;" + NEWLINE +
    "" + NEWLINE +
    "enum E {" + NEWLINE +
    "  public static class NestedC { }" + NEWLINE +
    "  public static interface NestedI { }" + NEWLINE +
    "}" + NEWLINE +
    "interface I { }" + NEWLINE +
    "class C { }" + NEWLINE +
    "}" + NEWLINE;

  /** Test getMainClassName. */
  public void testgetMainClassName() throws BadLocationException, ClassNameNotFoundException {
    _doc.insertString(0, PUBLIC_CIE_TEXT, null);
    assertEquals("C", _doc.getMainClassName());
    _doc.remove(0, PUBLIC_CIE_TEXT.length());

    _doc.insertString(0, PUBLIC_CEI_TEXT, null);
    assertEquals("C", _doc.getMainClassName());
    _doc.remove(0, PUBLIC_CEI_TEXT.length());

    _doc.insertString(0, PUBLIC_ICE_TEXT, null);
    assertEquals("I", _doc.getMainClassName());
    _doc.remove(0, PUBLIC_ICE_TEXT.length());

    _doc.insertString(0, PUBLIC_IEC_TEXT, null);
    assertEquals("I", _doc.getMainClassName());
    _doc.remove(0, PUBLIC_IEC_TEXT.length());

    _doc.insertString(0, PUBLIC_ECI_TEXT, null);
    assertEquals("E", _doc.getMainClassName());
    _doc.remove(0, PUBLIC_ECI_TEXT.length());

    _doc.insertString(0, PUBLIC_EIC_TEXT, null);
    assertEquals("E", _doc.getMainClassName());
    _doc.remove(0, PUBLIC_EIC_TEXT.length());

    // Ignore: testing visibility issues
//    // public after non-public
//    _doc.insertString(0, I_PUBLIC_CE_TEXT, null);
//    assertEquals("C", _doc.getMainClassName());
//    _doc.remove(0, I_PUBLIC_CE_TEXT.length());
//
//    _doc.insertString(0, E_PUBLIC_CI_TEXT, null);
//    assertEquals("C", _doc.getMainClassName());
//    _doc.remove(0, E_PUBLIC_CI_TEXT.length());
//
//    _doc.insertString(0, C_PUBLIC_IE_TEXT, null);
//    assertEquals("I", _doc.getMainClassName());
//    _doc.remove(0, C_PUBLIC_IE_TEXT.length());
//
//    _doc.insertString(0, E_PUBLIC_IC_TEXT, null);
//    assertEquals("I", _doc.getMainClassName());
//    _doc.remove(0, E_PUBLIC_IC_TEXT.length());
//
//    _doc.insertString(0, C_PUBLIC_EI_TEXT, null);
//    assertEquals("E", _doc.getMainClassName());
//    _doc.remove(0, C_PUBLIC_EI_TEXT.length());
//
//    _doc.insertString(0, I_PUBLIC_EC_TEXT, null);
//    assertEquals("E", _doc.getMainClassName());
//    _doc.remove(0, I_PUBLIC_EC_TEXT.length());
    
    // no public class
    _doc.insertString(0, CIE_TEXT, null);
    assertEquals("C", _doc.getMainClassName());
    _doc.remove(0, CIE_TEXT.length());

    _doc.insertString(0, CEI_TEXT, null);
    assertEquals("C", _doc.getMainClassName());
    _doc.remove(0, CEI_TEXT.length());

    _doc.insertString(0, ICE_TEXT, null);
    assertEquals("I", _doc.getMainClassName());
    _doc.remove(0, ICE_TEXT.length());

    _doc.insertString(0, IEC_TEXT, null);
    assertEquals("I", _doc.getMainClassName());
    _doc.remove(0, IEC_TEXT.length());

    _doc.insertString(0, ECI_TEXT, null);
    assertEquals("E", _doc.getMainClassName());
    _doc.remove(0, ECI_TEXT.length());

    _doc.insertString(0, EIC_TEXT, null);
    assertEquals("E", _doc.getMainClassName());
    _doc.remove(0, EIC_TEXT.length());
  }
  
    /** Test containsSource. */
  public void testContainsClassOrInterfaceOrEnum() throws BadLocationException {
    _doc.insertString(0, "class", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "interface", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "enum", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "foo", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "//class", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "//interface", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "//enum", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "//foo", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "/*class*/", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "/*interface*/", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "/*enum*/", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "/*foo*/", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());
    
    _doc.insertString(0, "\"class\"", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "\"interface\"", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "\"enum\"", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "\"foo\"", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());
    
    _doc.insertString(0, "/*class*/class", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "/*interface*/interface", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "/*enum*/enum", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "/*foo*/foo", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());
    
    _doc.insertString(0, "\"class\"class", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "\"interface\"interface", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "\"enum\"enum", null);
    assertTrue(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());

    _doc.insertString(0, "\"foo\"foo", null);
    assertFalse(_doc.containsSource());
    _doc.remove(0, _doc.getText().length());
  }
}
