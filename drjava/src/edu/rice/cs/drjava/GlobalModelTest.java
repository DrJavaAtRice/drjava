package  edu.rice.cs.drjava;

import  junit.framework.*;
import  java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.Reader;

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
    DefinitionsEditorKit kit = new DefinitionsEditorKit();
    DefinitionsDocument defDoc = new DefinitionsDocument();
    InteractionsDocument iactDoc = new InteractionsDocument();
    Document conDoc = new DefaultStyledDocument();
    CompilerError[] errors = {};
    _gm = new GlobalModel(kit, defDoc, iactDoc, conDoc, errors);
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
    Document doc = _gm.getDefinitionsDocument();
    doc.insertString(0, "blah", null);
    assertTrue("#0.0", _gm.isModifiedSinceSave());
    TestWriterCommand com = new TestWriterCommand("blah.txt");
    _gm.saveFile(com);
    assertEquals("#1.0", "saveFile ", _recorder.getEventList());
    assertTrue("#1.1", !_gm.isModifiedSinceSave());
    assertEquals("#1.2", "blah", com.getBuffer());
  }
  
  public void testOpenFromFileMenu() throws BadLocationException, IOException {
    Document doc = _gm.getDefinitionsDocument();
    TestReaderCommand com = new TestReaderCommand("blah.txt", "blah");
    _gm.openFile(com);
    doc = _gm.getDefinitionsDocument();
    assertEquals("#1.0", "openFile ", _recorder.getEventList());
    assertTrue("#1.1", !_gm.isModifiedSinceSave());
    assertEquals("#1.2", "blah", doc.getText(0, doc.getLength()));
  }
  
  public void testQuit() {
    _gm.quit();
    assertEquals("#1.0", "quit ", _recorder.getEventList());
  }
  
  public void testResetInteractions() throws BadLocationException {
    Document iDoc = _gm.getInteractionsDocument();
    assertEquals("#0.0", "Welcome to DrJava.\n> ", iDoc.getText(0, iDoc.getLength()));
    iDoc.insertString(21, "monkey", null);
    assertEquals("#1.0", "Welcome to DrJava.\n> monkey", iDoc.getText(0, iDoc.getLength()));
    _gm.resetInteractions();
    assertEquals("#2.0", "resetInteractions ", _recorder.getEventList());
    assertEquals("#2.1", "Welcome to DrJava.\n> ", iDoc.getText(0, iDoc.getLength()));
  }
  
  public void testResetConsole() throws BadLocationException {
    Document cDoc = _gm.getConsoleDocument();
    assertEquals("#0.0", "", cDoc.getText(0, cDoc.getLength()));
    cDoc.insertString(0, "monkey", null);
    assertEquals("#1.0", "monkey", cDoc.getText(0, cDoc.getLength()));
    _gm.resetConsole();
    assertEquals("#2.0", "resetConsole ", _recorder.getEventList());
    assertEquals("#2.1", "", cDoc.getText(0, cDoc.getLength()));
  }

  
  private static class GmRecorder implements GlobalModelListener {
    private StringBuffer _eventsFired = new StringBuffer();
    
    public void newFileCreated() {
      _eventsFired.append("newFile ");
    }
    
    public void fileSaved(String fileName) {
      _eventsFired.append("saveFile ");      
    }
    
    public void fileOpened(String fileName) {
      _eventsFired.append("openFile ");
    }
    
    public void compileStarted() {
      _eventsFired.append("compileBegin ");
    }
    
    public void compileEnded() {
      _eventsFired.append("compileEnd ");
    }
    
    public void quit() {
      _eventsFired.append("quit ");
    }
    
    public void interactionsReset() {
      _eventsFired.append("resetInteractions ");
    }
    
    public void consoleReset() {
      _eventsFired.append("resetConsole ");
    }
    public boolean canAbandonFile() {
      return true;
    }
    public String getEventList() {
      return _eventsFired.toString();
    }
  }
  
  class TestReaderCommand implements ReaderCommand {
    private String _name;
    private String _buf;
    private StringReader _reader;
    
    TestReaderCommand(String name, String buf) {
      _name = name;
      _buf = buf;
      _reader = new StringReader(_buf);
    }
    
    public String getName() {
      return _name;
    }
    
    public String getBuffer() {
      return _buf;
    }
    public Reader getReader() {
      return _reader;
    }
  }
  
  class TestWriterCommand implements WriterCommand {
    private String _name;
    private StringWriter _writer;
    
    TestWriterCommand(String name) {
      _name = name;
      _writer = new StringWriter();
    }
    
    public String getName() {
      return _name;
    }
    
    public String getBuffer() {
      return _writer.getBuffer().toString();
    }
    
    public Writer getWriter() {
      return _writer;
    }
  }

}