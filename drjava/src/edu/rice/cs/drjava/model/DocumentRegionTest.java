package edu.rice.cs.drjava.model;

import junit.framework.TestCase;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.util.UnexpectedException;

import java.io.File;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/**
 * A JUnit test case class.
 * Every method starting with the word "test" will be called when running
 * the test with JUnit.
 */
public class DocumentRegionTest extends DrJavaTestCase {
  
  private static Position createPosition(OpenDefinitionsDocument doc, int i) {
    try { return doc.createPosition(i); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Tests DummyDocumentRegion class. */
  public void testDummyDocumentRegion() {
    DocumentRegion r1 = new DummyDocumentRegion(new File("dummy1"), 5, 20);
    DocumentRegion r2 = new DummyDocumentRegion(new File("dummy1"), 5, 20);
    DocumentRegion r3 = new DummyDocumentRegion(new File("dummy2"), 20, 20);
    assertEquals("equality test 1", r1, r1);
    assertFalse("equality test 2", r1.equals(r2));
    assertFalse("equality test 3", r2.equals(r3));
  }
  
  /** Tests DummyDocumentRegion class. */
  public void testSimpleDocumentRegion() {
    DummyOpenDefDoc doc1 = new DummyOpenDefDoc();
    DummyOpenDefDoc doc2 = new DummyOpenDefDoc();
    doc1.append("This is a test");
    doc2.append("This is another test");
    EnhancedDocumentRegion r1 = new SimpleDocumentRegion(doc1, 5, 10);
    EnhancedDocumentRegion r2 = new SimpleDocumentRegion(doc1, 5, 10);
    EnhancedDocumentRegion r3 = new SimpleDocumentRegion(doc2, 5, 10);
    assertEquals("equality test 1", r1, r1);
    assertEquals("equality test 2", r1, r2);
    assertFalse("equality test 3", r2.equals(r3));
  }
  
  /** Tests BrowserDocumentRegion class. */
  public void testBrowserDocumentRegion() {
    DummyOpenDefDoc doc1 = new DummyOpenDefDoc();
    DummyOpenDefDoc doc2 = new DummyOpenDefDoc();
    doc1.append("This is a test");
    doc2.append("This is another test");
    EnhancedDocumentRegion r1 = new BrowserDocumentRegion(doc1, createPosition(doc1, 5), createPosition(doc1, 10));
    EnhancedDocumentRegion r2 = new SimpleDocumentRegion(doc1, createPosition(doc1, 5), createPosition(doc1, 10));
    EnhancedDocumentRegion r3 = new SimpleDocumentRegion(doc2, createPosition(doc1, 5), createPosition(doc2, 10));
    assertEquals("equality test 1", r1, r1);
    assertFalse("equality test 2", r1.equals(r2));
    assertFalse("equality test 3", r2.equals(r3));
  }
}
