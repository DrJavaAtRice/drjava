package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.io.File;
import  java.util.Vector;
import  javax.swing.text.BadLocationException;
import  junit.extensions.*;
import java.util.LinkedList;
import javax.swing.text.Document;
import javax.swing.text.DefaultStyledDocument;
/**
 * @version $Id$
 */
public class GlobalModelTest extends TestCase {
  GlobalModel _gm;
  GmRecorder _recorder;
  
  /**
   * Constructor.
   * @param  String name
   */
  public GlobalModelTest(String name) {
    super(name);
  }

  /**
   */
  public void setUp() {
    DefinitionsDocument defDoc = new DefinitionsDocument();
    InteractionsDocument iactDoc = new InteractionsDocument();
    Document conDoc = new DefaultStyledDocument();
    CompilerError[] errors = {};
    _gm = new GlobalModel(defDoc, iactDoc, conDoc, errors);
    _recorder = new GmRecorder();
    _gm.addListener(_recorder);
  }

  /**
   * Creates a test suite for JUnit to run.
   * @return a test suite based on the methods in this class
   */
  public static Test suite() {
    return  new TestSuite(GlobalModelTest.class);
  }
  
  public void testNewFromFileMenu() {
    _gm.newFile();
    assertEquals("#0.0", "newFile ", _recorder.getEventList());
    assertTrue("#0.1", !_gm.isModifiedSinceSave());
  }
  
  public void testSaveFromFileMenu() throws BadLocationException {
    try {
      Document doc = _gm.getDefinitionsDocument();
      doc.insertString(0, "blah", null);
      assertTrue("#0.0", _gm.isModifiedSinceSave());
      _gm.saveFileAs("blah.txt");
      assertEquals("#1.0", "saveFile ", _recorder.getEventList());
      assertTrue("#1.1", !_gm.isModifiedSinceSave());
    }
    finally {
      new File("blah.txt").delete();
    }
  }
  
  private static class GmRecorder implements GlobalModelListener {
    private StringBuffer _eventsFired = new StringBuffer();
    
    public void fireNewFileEvent() {
      _eventsFired.append("newFile ");
    }
    
    public void fireSaveFileEvent(String fileName) {
      _eventsFired.append("saveFile ");      
    }
    
    public void fireOpenFileEvent(String fileName) {
      _eventsFired.append("openFile ");
    }
    
    public void fireCompileBeginEvent() {
      _eventsFired.append("compileBegin ");
    }
    
    public void fireCompileEndEvent() {
      _eventsFired.append("compileEnd ");
    }
    
    public void fireQuitEvent() {
      _eventsFired.append("quit ");
    }
    
    public void fireResetInteractionsEvent() {
      _eventsFired.append("resetInteractions ");
    }
    
    public void fireResetConsoleEvent() {
      _eventsFired.append("resetConsole ");
    }
    
    public String getEventList() {
      return _eventsFired.toString();
    }
  }
}