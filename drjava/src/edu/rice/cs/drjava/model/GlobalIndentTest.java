package edu.rice.cs.drjava.model;

import  junit.framework.*;
import  junit.extensions.*;
import  javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;

/**
 * Tests the indenting functionality on the level of the GlobalModel.
 * Not only are we testing that the document turns out right, but also
 * that the cursor position in the document is consistent with a standard.
 * @version $Id$
 */
public class GlobalIndentTest extends TestCase {
  private GlobalModel _model;
  private static final String FOO_EX_1 = "public class Foo {\n";
  private static final String FOO_EX_2 = "int foo;\n";
  private static final String BAR_CALL_1 = "bar(monkey,\n";
  private static final String BAR_CALL_2 = "banana)\n";
  private static final String BEAT_1 = "void beat(Horse dead,\n";
  private static final String BEAT_2 = "          Stick pipe)\n";
  /**
   * put your documentation comment here
   * @param     String name
   */
  public GlobalIndentTest(String name) {
    super(name);
  }

  /**
   * put your documentation comment here
   */
  public void setUp() {
    _model = new GlobalModel();
    _model.setDefinitionsIndent(2);
  }

  /**
   * put your documentation comment here
   * @return
   */
  public static Test suite() {
    return  new TestSuite(GlobalIndentTest.class);
  }

  /**
   * Tests indent that increases the size of the tab when the
   * cursor is at the start of the line.  When the cursor is in the
   * whitespace before the first word on a line, indent always
   * moves the cursor up to the beginning of the first non-whitespace
   * character.
   * @throws BadLocationException
   */
  public void testIndentGrowTabAtStart() throws BadLocationException{
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_1, null);
    doc.insertString(FOO_EX_1.length(), " " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(FOO_EX_1.length());
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, doc);
    _assertLocation(FOO_EX_1.length() + 2, openDoc);
  }

  /**
   * Tests indent that increases the size of the tab when the
   * cursor is in the middle of the line.  The cursor stays in the
   * same place.
   * @throws BadLocationException
   */
  public void testIndentGrowTabAtMiddle() throws BadLocationException{
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_1, null);
    doc.insertString(FOO_EX_1.length(), " " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(FOO_EX_1.length() + 5);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, doc);
    _assertLocation(FOO_EX_1.length() + 6, openDoc);
  }

  /**
   * Tests indent that increases the size of the tab when the
   * cursor is at the end of the line.  The cursor stays in the
   * same place.
   * @throws BadLocationException
   */
  public void testIndentGrowTabAtEnd() throws BadLocationException{
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_1, null);
    doc.insertString(FOO_EX_1.length(), " " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(doc.getLength() - 1);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, doc);
    _assertLocation(doc.getLength() - 1, openDoc);
  }

  /**
   * Tests indent that increases the size of the tab when the
   * cursor is at the start of the line.  When the cursor is in the
   * whitespace before the first word on a line, indent always
   * moves the cursor up to the beginning of the first non-whitespace
   * character.
   * @throws BadLocationException
   */
  public void testIndentShrinkTabAtStart() throws BadLocationException{
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_1, null);
    doc.insertString(FOO_EX_1.length(), "   " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(FOO_EX_1.length());
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, doc);
    _assertLocation(FOO_EX_1.length() + 2, openDoc);
  }

  /**
   * Tests indent that increases the size of the tab when the
   * cursor is in the middle of the line.  The cursor stays in the
   * same place.
   * @throws BadLocationException
   */
  public void testIndentShrinkTabAtMiddle() throws BadLocationException{
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_1, null);
    doc.insertString(FOO_EX_1.length(), "   " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(FOO_EX_1.length() + 5);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, doc);
    _assertLocation(FOO_EX_1.length() + 4, openDoc);
  }

  /**
   * Tests indent that increases the size of the tab when the
   * cursor is at the end of the line.  The cursor stays in the
   * same place.
   * @throws BadLocationException
   */
  public void testIndentShrinkTabAtEnd() throws BadLocationException{
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_1, null);
    doc.insertString(FOO_EX_1.length(), "   " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(doc.getLength() - 1);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_1 + "  " + FOO_EX_2, doc);
    _assertLocation(doc.getLength() - 1, openDoc);
  }

  /**
   * Do an indent that should match up with the indent on the line above.
   * The cursor is at the start of the line.
   * @exception BadLocationException
   */
  public void testIndentSameAsLineAboveAtStart() throws BadLocationException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_2, null);
    doc.insertString(FOO_EX_2.length(), "   " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(FOO_EX_2.length());
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_2 + FOO_EX_2, doc);
    _assertLocation(FOO_EX_2.length(), openDoc);
  }

  /**
   * Do an indent that should match up with the indent on the line above.
   * The cursor is at the end of the line.
   * @exception BadLocationException
   */
  public void testIndentSameAsLineAboveAtEnd() throws BadLocationException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_2, null);
    doc.insertString(FOO_EX_2.length(), "   " + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(doc.getLength() - 1);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_2 + FOO_EX_2, doc);
    _assertLocation(doc.getLength() - 1, openDoc);
  }

  /**
   * Do an indent that follows the behavior in line with parentheses.
   * The cursor is at the start of the line.
   * @exception BadLocationException
   */
  public void testIndentInsideParenAtStart() throws BadLocationException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, BAR_CALL_1, null);
    doc.insertString(BAR_CALL_1.length(), BAR_CALL_2, null);
    openDoc.syncCurrentLocationWithDefinitions(BAR_CALL_1.length());
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(BAR_CALL_1 + "    " + BAR_CALL_2, doc);
    _assertLocation(BAR_CALL_1.length() + 4, openDoc);
  }

  /**
   * Do an indent that follows the behavior in line with parentheses.
   * The cursor is at the end of the line.
   * @exception BadLocationException
   */
  public void testIndentInsideParenAtEnd() throws BadLocationException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, BAR_CALL_1, null);
    doc.insertString(BAR_CALL_1.length(), BAR_CALL_2, null);
    openDoc.syncCurrentLocationWithDefinitions(doc.getLength() - 1);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(BAR_CALL_1 + "    " + BAR_CALL_2, doc);
    _assertLocation(doc.getLength() - 1, openDoc);
  }

  /**
   * Indent does nothing to change the document when everything is in place.
   */
  public void testIndentDoesNothing() throws BadLocationException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, FOO_EX_2 + FOO_EX_2, null);
    openDoc.syncCurrentLocationWithDefinitions(doc.getLength() - 1);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(FOO_EX_2 + FOO_EX_2, doc);
    _assertLocation(doc.getLength() - 1, openDoc);
  }


  /**
   * The quintessential "make the squiggly go to the start, even though
   * method arguments extend over two lines" test.  This behavior is not
   * correctly followed yet, so until it is, leave this method commented.
   * @exception BadLocationException
   *
  public void testIndentSquigglyAfterTwoLines() throws BadLocationException {
    OpenDefinitionsDocument openDoc = _getOpenDoc();
    Document doc = openDoc.getDocument();

    doc.insertString(0, BEAT_1, null);
    doc.insertString(BEAT_1.length(), BEAT_2, null);
    doc.insertString(doc.getLength(), "{", null);
    int loc = openDoc.getCurrentDefinitionsLocation();
    openDoc.indentLinesInDefinitions(loc, loc);
    _assertContents(BEAT_1 + BEAT_2 + "{", doc);
    _assertLocation(doc.getLength(), openDoc);
  }
*/

  /**
   * Indents block comments with stars as they should.
   * Uncomment this method when the correct functionality is implemented.
   */
//  public void testIndentBlockCommentStar() throws BadLocationException {
//    OpenDefinitionsDocument openDoc = _getOpenDoc();
//    Document doc = openDoc.getDocument();
//    doc.insertString(0, "/*\n*\n*/\n " + FOO_EX_2, null);
//    int loc = openDoc.getCurrentDefinitionsLocation();
//    openDoc.indentLinesInDefinitions(0, doc.getLength());
//    _assertContents("/*\n *\n */\n" + FOO_EX_2, doc);
//    _assertLocation(doc.getLength(), openDoc);
//  }

  /**
   * Get the only open definitions document.
   */
  private OpenDefinitionsDocument _getOpenDoc() {
    _assertNumOpenDocs(0);
    _model.newFile();
    OpenDefinitionsDocument[] docs = _model.getDefinitionsDocuments();
    _assertNumOpenDocs(1);
    return docs[0];
  }

  private void _assertNumOpenDocs(int num) {
    assertEquals("number of open documents",
                 num,
                 _model.getDefinitionsDocuments().length);
  }

  private void _assertContents(String expected, Document document)
    throws BadLocationException
  {
    assertEquals("document contents", expected,
                 document.getText(0, document.getLength()));
  }

  private void _assertLocation(int loc, OpenDefinitionsDocument openDoc) {
    assertEquals("current def'n loc", loc,
                 openDoc.getCurrentDefinitionsLocation());
  }
}
