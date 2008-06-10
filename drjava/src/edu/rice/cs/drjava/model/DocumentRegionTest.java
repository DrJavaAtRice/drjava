package edu.rice.cs.drjava.model;

import junit.framework.TestCase;

import edu.rice.cs.drjava.DrJavaTestCase;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.plt.io.IOUtil;

import java.io.File;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/** Trivial JUnit test case class for DocumentRegion. */
public class DocumentRegionTest extends DrJavaTestCase {
  private volatile OpenDefinitionsDocument _doc;  // working document accessible across threads
  private static final AbstractGlobalModel _model = new AbstractGlobalModel();
  private volatile File _tempDir;

  /** Initializes the document for the tests. */
  public void setUp() throws Exception {
    super.setUp();
    String user = System.getProperty("user.name");
    _tempDir = IOUtil.createAndMarkTempDirectory("DrJava-test-" + user, "");
    _doc = _model.newFile(_tempDir);    
  }  
  
  public void tearDown() throws Exception {
    _model.closeAllFiles();
    _tempDir = null;
    super.tearDown();
  }

  private static Position createPosition(OpenDefinitionsDocument doc, int i) {
    try { return doc.createPosition(i); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Tests DummyDocumentRegion class. */
  public void testDummyDocumentRegion() {
    Region r1 = new DummyDocumentRegion(new File("dummy1"), 5, 20);
    Region r2 = new DummyDocumentRegion(new File("dummy1"), 5, 20);
    Region r3 = new DummyDocumentRegion(new File("dummy2"), 20, 20);
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
    OrderedDocumentRegion r1 = new DocumentRegion(doc1, 5, 10);
    OrderedDocumentRegion r2 = new DocumentRegion(doc1, 5, 10);
    OrderedDocumentRegion r3 = new DocumentRegion(doc2, 5, 10);
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
    IDocumentRegion r1 = new BrowserDocumentRegion(doc1, createPosition(doc1, 5), createPosition(doc1, 10));
    IDocumentRegion r2 = new DocumentRegion(doc1, createPosition(doc1, 5), createPosition(doc1, 10));
    IDocumentRegion r3 = new DocumentRegion(doc2, createPosition(doc1, 5), createPosition(doc2, 10));
    assertEquals("equality test 1", r1, r1);
    assertFalse("equality test 2", r1.equals(r2));
    assertFalse("equality test 3", r2.equals(r3));
  }

  private static final String DOCUMENT_TEXT =
    "public class Foo\n" +
    "{\n" +
    "        /**\n" +
    "         * Barry Good!\n" +
    "         * (what I really mean is bar)\n" +
    "         */\n" +
    "        public void bar() \n" +
    "        {\n" +
    "                this.bar();\n" +
    "        }\n" +
    "}";

  /** Tests RegionManager. */
  public void testRegionManager() throws BadLocationException {
    _doc.insertString(0, DOCUMENT_TEXT, null);
    RegionManager<DocumentRegion> rm = new ConcreteRegionManager<DocumentRegion>();
    assertNull(rm.getRegionAt(_doc, 5));
    assertNull(rm.getRegionAt(_doc, 3));
    assertNull(rm.getRegionAt(_doc, 7));
    assertNull(rm.getRegionAt(_doc, 2));
    assertNull(rm.getRegionAt(_doc, 8));
    assertNull(rm.getRegionContaining(_doc, 5, 5));
    assertNull(rm.getRegionContaining(_doc, 4, 6));
    assertNull(rm.getRegionContaining(_doc, 3, 7));
    assertNull(rm.getRegionContaining(_doc, 2, 8));
    assertNull(rm.getRegionContaining(_doc, 2, 5));
    assertNull(rm.getRegionContaining(_doc, 5, 8));
    
    DocumentRegion r1 = new DocumentRegion(_doc, createPosition(_doc, 3), createPosition(_doc, 7));
    rm.addRegion(r1);
    assertTrue(r1==rm.getRegionAt(_doc, 5));
    assertTrue(r1==rm.getRegionAt(_doc, 3));
    assertTrue(r1==rm.getRegionAt(_doc, 7));
    assertNull(rm.getRegionAt(_doc, 2));
    assertNull(rm.getRegionAt(_doc, 8));
    assertTrue(r1==rm.getRegionContaining(_doc, 5, 5));
    assertTrue(r1==rm.getRegionContaining(_doc, 4, 6));
    assertTrue(r1==rm.getRegionContaining(_doc, 3, 7));
    assertNull(rm.getRegionContaining(_doc, 2, 8));
    assertNull(rm.getRegionContaining(_doc, 2, 5));
    assertNull(rm.getRegionContaining(_doc, 5, 8));
    
    assertNull(rm.getRegionAt(_doc, 15));
    assertNull(rm.getRegionAt(_doc, 13));
    assertNull(rm.getRegionAt(_doc, 17));
    assertNull(rm.getRegionAt(_doc, 12));
    assertNull(rm.getRegionAt(_doc, 18));
    assertNull(rm.getRegionContaining(_doc, 15, 15));
    assertNull(rm.getRegionContaining(_doc, 14, 16));
    assertNull(rm.getRegionContaining(_doc, 13, 17));
    assertNull(rm.getRegionContaining(_doc, 12, 18));
    assertNull(rm.getRegionContaining(_doc, 12, 15));
    assertNull(rm.getRegionContaining(_doc, 15, 18));
    
    DocumentRegion r2 = new DocumentRegion(_doc, createPosition(_doc, 13), createPosition(_doc, 17));
    rm.addRegion(r2);
    assertTrue(r2==rm.getRegionAt(_doc, 15));
    assertTrue(r2==rm.getRegionAt(_doc, 13));
    assertTrue(r2==rm.getRegionAt(_doc, 17));
    assertNull(rm.getRegionAt(_doc, 12));
    assertNull(rm.getRegionAt(_doc, 18));
    assertTrue(r2==rm.getRegionContaining(_doc, 15, 15));
    assertTrue(r2==rm.getRegionContaining(_doc, 14, 16));
    assertTrue(r2==rm.getRegionContaining(_doc, 13, 17));
    assertNull(rm.getRegionContaining(_doc, 12, 18));
    assertNull(rm.getRegionContaining(_doc, 12, 15));
    assertNull(rm.getRegionContaining(_doc, 15, 18)); 
    
    // r2 and r3 are the same region, so the region will be reused
    // and we get r2 back
    DocumentRegion r3 = new DocumentRegion(_doc, createPosition(_doc, 13), createPosition(_doc, 17));
    rm.addRegion(r3);
    assertTrue(r2==rm.getRegionAt(_doc, 15));
    assertTrue(r2==rm.getRegionAt(_doc, 13));
    assertTrue(r2==rm.getRegionAt(_doc, 17));
    assertNull(rm.getRegionAt(_doc, 12));
    assertNull(rm.getRegionAt(_doc, 18));
    assertTrue(r2==rm.getRegionContaining(_doc, 15, 15));
    assertTrue(r2==rm.getRegionContaining(_doc, 14, 16));
    assertTrue(r2==rm.getRegionContaining(_doc, 13, 17));
    assertNull(rm.getRegionContaining(_doc, 12, 18));
    assertNull(rm.getRegionContaining(_doc, 12, 15));
    assertNull(rm.getRegionContaining(_doc, 15, 18)); 
    
    // removal uses equality, not identity, so we can remove r3, and r2 will be gone
    rm.removeRegion(r3);
    assertNull(rm.getRegionAt(_doc, 15));
    assertNull(rm.getRegionAt(_doc, 13));
    assertNull(rm.getRegionAt(_doc, 17));
    assertNull(rm.getRegionAt(_doc, 12));
    assertNull(rm.getRegionAt(_doc, 18));
    assertNull(rm.getRegionContaining(_doc, 15, 15));
    assertNull(rm.getRegionContaining(_doc, 14, 16));
    assertNull(rm.getRegionContaining(_doc, 13, 17));
    assertNull(rm.getRegionContaining(_doc, 12, 18));
    assertNull(rm.getRegionContaining(_doc, 12, 15));
    assertNull(rm.getRegionContaining(_doc, 15, 18)); 

    assertNull(rm.getRegionAt(_doc, 15));
    assertNull(rm.getRegionAt(_doc, 13));
    assertNull(rm.getRegionAt(_doc, 17));
    assertNull(rm.getRegionAt(_doc, 12));
    assertNull(rm.getRegionAt(_doc, 18));
    assertNull(rm.getRegionContaining(_doc, 15, 15));
    assertNull(rm.getRegionContaining(_doc, 14, 16));
    assertNull(rm.getRegionContaining(_doc, 13, 17));
    assertNull(rm.getRegionContaining(_doc, 12, 18));
    assertNull(rm.getRegionContaining(_doc, 12, 15));
    assertNull(rm.getRegionContaining(_doc, 15, 18));
    
    rm.removeRegion(r1);
    assertNull(rm.getRegionAt(_doc, 5));
    assertNull(rm.getRegionAt(_doc, 3));
    assertNull(rm.getRegionAt(_doc, 7));
    assertNull(rm.getRegionAt(_doc, 2));
    assertNull(rm.getRegionAt(_doc, 8));
    assertNull(rm.getRegionContaining(_doc, 5, 5));
    assertNull(rm.getRegionContaining(_doc, 4, 6));
    assertNull(rm.getRegionContaining(_doc, 3, 7));
    assertNull(rm.getRegionContaining(_doc, 2, 8));
    assertNull(rm.getRegionContaining(_doc, 2, 5));
    assertNull(rm.getRegionContaining(_doc, 5, 8));
  }
}
