package  edu.rice.cs.drjava;

import  javax.swing.text.BadLocationException;
import  junit.framework.*;
import  gj.util.Vector;
import  junit.extensions.*;


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
  public void testInsertToDoc() {
    try {
      defModel.insertString(0, "a/*bc */\"\\{}()", null);
      assertEquals("#0.0", defModel.getText(0, 8), "a/*bc */");
      assertEquals("#0.1", 14, defModel._currentLocation);
      defModel.insertString(0, "Start:", null);
      assertEquals("#1.0", defModel.getText(0, 14), "Start:a/*bc */");
      assertEquals("#1.1", 6, defModel._currentLocation);
      // document is:
      // Start:=>a/*bc */"\\{}()
      BraceReduction rm = defModel._reduced;
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
    } catch (javax.swing.text.BadLocationException e) {
      System.out.println("EXCEPTION");
    }
  }

  /**
   * Test inserting a star between a star-slash combo.
   * @exception BadLocationException
   */
  public void testInsertStarIntoStarSlash() throws BadLocationException {
    BraceReduction rm = defModel._reduced;
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
    BraceReduction rm = defModel._reduced;
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
    BraceReduction rm = defModel._reduced;
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
  public void testDeleteDoc() {
    try {
      defModel.insertString(0, "a/*bc */", null);
      defModel.remove(3, 3);
      assertEquals("#0.0", "a/**/", defModel.getText(0, 5));
      assertEquals("#0.1", 3, defModel._currentLocation);
      BraceReduction rm = defModel._reduced;
      assertEquals("1.0", "*/", rm.currentToken().getType());
      // no longer support getBlockOffset
      //        assertEquals("1.1",0,rm.getBlockOffset());
      rm.move(-2);
      assertEquals("1.2", "/*", rm.currentToken().getType());
      rm.move(2);
      assertEquals("1.3", ReducedToken.INSIDE_BLOCK_COMMENT, rm.getStateAtCurrent());
    } catch (javax.swing.text.BadLocationException e) {
      System.out.println(e.toString());
    }
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
    assertEquals("vector length", 8, v.size());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(0).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(1).getState());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(2).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(3).getState());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(4).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(5).getState());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(6).getState());
    assertEquals(HighlightStatus.NORMAL, v.elementAt(7).getState());
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
    final String s = "int Y";
    defModel.insertString(defModel.getLength(), s, null);
    // First sanity check the whole string's status
    v = defModel.getHighlightStatus(0, defModel.getLength());
    _checkHighlightStatusConsistent(v, 0, defModel.getLength());
    // Make sure the keyword is highlighted
    assertEquals("vector length", 2, v.size());
    assertEquals(HighlightStatus.KEYWORD, v.elementAt(0).getState());
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
   * Test whether removeTabs actually removes all tabs.
   */
  public void testRemoveTabs() {
    defModel.setIndent(2);
    String test = "\t this \t\tis a \t\t\t\t\ttest\t\t";
    String result = defModel._removeTabs(test);
    assertEquals( "   this     is a           test    ", result);
  }
  
  /**
   * Test whether tabs are removed as appropriate on call to insertString.
   * @exception BadLocationException
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
  public void testPackageNameEmpty() throws BadLocationException {
    assertEquals("Package name for empty document",
                 "",
                 defModel.getPackageName());
  }

  /** Test package-finding on simple document, with no funny comments. */
  public void testPackageNameSimple() throws BadLocationException {
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
  public void testPackageNameWeird1() throws BadLocationException {
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
  public void testPackageNameWeird2() throws BadLocationException {
    String weird = "package edu . rice //comment!\n.cs.drjava;";
    String normal = "edu.rice.cs.drjava";
    defModel.insertString(0, weird, null);

    assertEquals("Package name for weird: '" + weird + "'",
                 normal,
                 defModel.getPackageName());
  }
}
