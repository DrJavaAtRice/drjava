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

package edu.rice.cs.drjava.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.print.*;
import javax.swing.text.*;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import java.io.*;
import java.util.*;
import java.net.ServerSocket;

import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.awt.font.TextLayout;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.*;

import edu.rice.cs.util.swing.FindReplaceMachine;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import edu.rice.cs.util.*;
import edu.rice.cs.util.newjvm.*;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.text.DocumentAdapterException;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.config.Configuration;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.FileOption;
import edu.rice.cs.drjava.config.BooleanOption;
import edu.rice.cs.drjava.model.print.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.debug.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.drjava.model.junit.*;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;


/**
 * Handles the bulk of DrJava's program logic.
 * The UI components interface with the GlobalModel through its public methods,
 * and GlobalModel responds via the GlobalModelListener interface.
 * This removes the dependency on the UI for the logical flow of the program's
 * features.  With the current implementation, we can finally test the compile
 * functionality of DrJava, along with many other things.
 *
 * @version $Id$
 */
public class DefaultGlobalModel implements GlobalModel, OptionConstants,
  JUnitModelCallback {
  
  // ----- FIELDS -----

  /**
   * Keeps track of all listeners to the model, and has the ability
   * to notify them of some event.
   */
  protected final EventNotifier _notifier = new EventNotifier();
  
  // ---- Definitions fields ----
  
  /**
   * Factory for new definitions documents and views.
   */
  private final DefinitionsEditorKit _editorKit = new DefinitionsEditorKit(_notifier);
  
  /**
   * ListModel for storing all OpenDefinitionsDocuments.
   */
  private final DefaultListModel _definitionsDocs = new DefaultListModel();
  
  /**
   * The instance of the indent decision tree used by Definitions documents.
   */
  public static final Indenter INDENTER;
  static {
    // Statically create the indenter from the config values
    int ind = DrJava.getConfig().getSetting(OptionConstants.INDENT_LEVEL).intValue();
    INDENTER = new Indenter(ind);
    DrJava.getConfig().addOptionListener(OptionConstants.INDENT_LEVEL,
                                    new OptionListener<Integer>() {
      public void optionChanged(OptionEvent<Integer> oce) {
        INDENTER.buildTree(DrJava.getConfig().getSetting(OptionConstants.INDENT_LEVEL).intValue());
      }
    });
  }
  
  
  // ---- Interpreter fields ----
  
  /**
   * RMI interface to the Interactions JVM.
   * Final, but set differently in the two constructors.
   * Package private so we can access it from test cases.
   */
  final MainJVM _interpreterControl;
  
  /**
   * Interface between the InteractionsDocument and the JavaInterpreter,
   * which runs in a separate JVM.
   */
  protected DefaultInteractionsModel _interactionsModel;
  
  
  // ---- Compiler Fields ----
  
  /**
   * Lock to prevent multiple threads from accessing the compiler at the
   * same time.
   */
  private Object _compilerLock = new Object();
  
  private CompilerErrorModel _compilerErrorModel = new CompilerErrorModel<CompilerError>(new CompilerError[0], this);

  /**
   * The total number of current compiler errors, including both errors
   * with and without files.
   */
  private int _numErrors = 0;
  
  /**
   * Whether or not to reset the interactions JVM after compiling.
   * Should only be false in test cases.
   */
  //private boolean _resetAfterCompile = true;
  
  
  // ---- JUnit Fields ----

  private JUnitErrorModel _junitErrorModel = new JUnitErrorModel(new JUnitError[0], this, false);
  
  /**
   * If a JUnit test is currently running, this is the OpenDefinitionsDocument
   * being tested.  Otherwise this is null.
   */
  private OpenDefinitionsDocument _docBeingTested = null;
  
  
  // ---- Javadoc Fields ----
  
  private JavadocModel _javadocModel = new DefaultJavadocModel(this);
  
  // ---- Debugger Fields ----
  
  /**
   * Interface to the integrated debugger.  If the JPDA classes are not
   * available, this is set NoDebuggerAvailable.ONLY.
   */
  private Debugger _debugger = NoDebuggerAvailable.ONLY;
  
  /**
   * Port used by the debugger to connect to the Interactions JVM.
   * Uniquely created in getDebugPort().
   */
//  private int _debugPort = -1;

  
  // ---- Input/Output Document Fields ----
  
  /**
   * The document adapter used in the Interactions model.
   */
  private final SwingDocumentAdapter _interactionsDocAdapter;
  
  /**
   * The document used to display System.out and System.err,
   * and to read from System.in.
   */
  private final ConsoleDocument _consoleDoc;

  /**
   * The document adapter used in the console document.
   */
  private final SwingDocumentAdapter _consoleDocAdapter;

  /**
   * The document used to display JUnit test results.
   */
  private final StyledDocument _junitDoc = new DefaultStyledDocument();
  
  /**
   * A lock object to prevent print calls to System.out or System.err
   * from flooding the JVM, ensuring the UI remains responsive.
   */
  private final Object _systemWriterLock = new Object();
  
  /**
   * Number of milliseconds to wait after each println, to prevent
   * the JVM from being flooded with print calls.
   */
  public static final int WRITE_DELAY = 50;

  /**
   * A PageFormat object for printing to paper.
   */
  private PageFormat _pageFormat = new PageFormat();

  /**
   * Listens for requests from System.in.
   */
  private InputListener _inputListener;

  
  // ----- CONSTRUCTORS -----
  
  /**
   * Constructs a new GlobalModel.
   * Creates a new MainJVM and starts its Interpreter JVM.
   */
  public DefaultGlobalModel() {
    //this(new MainJVM());
    
    _interpreterControl = new MainJVM();
    _interactionsDocAdapter = new SwingDocumentAdapter();
    _interactionsModel =
      new DefaultInteractionsModel(this, _interpreterControl,
                                   _interactionsDocAdapter);
    _interpreterControl.setInteractionsModel(_interactionsModel);
    _interpreterControl.setJUnitModel(this);  // to be replaced by JUnitModel

    _consoleDocAdapter = new SwingDocumentAdapter();
    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);

    _inputListener = NoInputListener.ONLY;

    _createDebugger();
    
    _registerOptionListeners();
    
    
    // Perhaps do this in another thread to allow startup to continue...
    _interpreterControl.startInterpreterJVM();
    resetInteractionsClasspath();
  }
  
  /**
   * Constructor.  Initializes all the documents, but take the interpreter
   * from the given previous model. This is used only for test cases,
   * since there is substantial overhead to initializing the interpreter.
   *
   * Reset the interpreter for good measure since it's an old one.
   * (NOTE: I'm not sure this is still correct or effective any more,
   *   now that we're always restarting the JVM.  Needs to be looked at...)
   *
  public DefaultGlobalModel(DefaultGlobalModel other) {
    this(other._interpreterControl);
    
    _interpreterControl.reset();
    try {
      _interactionsModel.setDebugPort(other.getDebugPort());
      _interactionsModel.setWaitingForFirstInterpreter(false);
    }
    catch (IOException ioe) {
      // Other model should already have a port, or it should be -1.
      //  We shouldn't ever get an IOException here.
      throw new UnexpectedException(ioe);
    }
  }*/
  
  /**
   * Constructs a new GlobalModel with the given MainJVM to act as an
   * RMI interface to the Interpreter JVM.  Does not attempt to start
   * the InterpreterJVM.
   * @param control RMI interface to the Interpreter JVM
   *
  public DefaultGlobalModel(MainJVM control) {
    _interpreterControl = control;
    _interactionsDocAdapter = new SwingDocumentAdapter();
    _interactionsModel =
      new DefaultInteractionsModel(this, control, _interactionsDocAdapter);
    _interpreterControl.setInteractionsModel(_interactionsModel);
    _interpreterControl.setJUnitModel(this);  // to be replaced by JUnitModel

    _consoleDocAdapter = new SwingDocumentAdapter();
    _consoleDoc = new ConsoleDocument(_consoleDocAdapter);

    _inputListener = NoInputListener.ONLY;

    _createDebugger();
    
    _registerOptionListeners();
  }*/


  
  // ----- METHODS -----
  
  /**
   * Add a listener to this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void addListener(GlobalModelListener listener) {
    _notifier.addListener(listener);
  }

  /**
   * Remove a listener from this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void removeListener(GlobalModelListener listener) {
    _notifier.removeListener(listener);
  }

  // getter methods for the private fields
  
  public EventNotifier getNotifier() {
    return _notifier;
  }

  public DefinitionsEditorKit getEditorKit() {
    return _editorKit;
  }
  
  /**
   * @return the interactions model.
   */
  public DefaultInteractionsModel getInteractionsModel() {
    return _interactionsModel;
  }

  /**
   * @return SwingDocumentAdapter in use by the InteractionsDocument.
   */
  public SwingDocumentAdapter getSwingInteractionsDocument() {
    return _interactionsDocAdapter;
  }

  public InteractionsDocument getInteractionsDocument() {
    return _interactionsModel.getDocument();
  }

  public JUnitErrorModel getJUnitErrorModel() {
    return _junitErrorModel;
  }

  public CompilerErrorModel getJavadocErrorModel() {
    return _javadocModel.getJavadocErrorModel();
  }
  
  public ConsoleDocument getConsoleDocument() {
    return _consoleDoc;
  }

  public SwingDocumentAdapter getSwingConsoleDocument() {
    return _consoleDocAdapter;
  }

  public CompilerErrorModel getCompilerErrorModel() {
    return _compilerErrorModel;
  }

  public StyledDocument getJUnitDocument() {
    return _junitDoc;
  }

  public PageFormat getPageFormat() {
    return _pageFormat;
  }

  public void setPageFormat(PageFormat format) {
    _pageFormat = format;
  }

  /**
   * @return the current total number of errors, both with and without files.
   */
  public int getNumErrors() {
    return _numErrors;
  }

  /**
   * Static inner class for newFile method.
   */
  private static class NewFileNotifier extends EventNotifier.Notifier {
    private OpenDefinitionsDocument _doc;
    public NewFileNotifier(OpenDefinitionsDocument doc) {
      super();
      _doc = doc;
    }
    public void notifyListener(GlobalModelListener l) {
      l.newFileCreated(_doc);
    }
  }

  /**
   * Creates a new definitions document and adds it to the list.
   * @return The new open document
   */
  public OpenDefinitionsDocument newFile() {
    final OpenDefinitionsDocument doc = _createOpenDefinitionsDocument();
    doc.getDocument().setFile(null);
    _definitionsDocs.addElement(doc);
    _notifier.notifyListeners(new NewFileNotifier(doc));
    return doc;
  }

  /**
   * Creates a new junit test case.
   * @param name the name of the new test case
   * @param makeSetUp true iff an empty setUp() method should be included
   * @param makeTearDown true iff an empty tearDown() method should be included
   * @return the new open test case
   */
  public OpenDefinitionsDocument newTestCase(String name, boolean makeSetUp, boolean makeTearDown) {
    StringBuffer buf = new StringBuffer();
    buf.append("import junit.framework.TestCase;\n\n");
    buf.append("/**\n");
    buf.append("* A JUnit test case class.\n");
    buf.append("* Every method starting with the word \"test\" will be called when running\n");
    buf.append("* the test with JUnit.\n");
    buf.append("*/\n");
    buf.append("public class ");
    buf.append(name);
    buf.append(" extends TestCase {\n\n");
    if (makeSetUp) {
      buf.append("/**\n");
      buf.append("* This method is called before each test method, to perform any common\n");
      buf.append("* setup if necessary.\n");
      buf.append("*/\n");
      buf.append("public void setUp() {\n}\n\n");
    }
    if (makeTearDown) {
      buf.append("/**\n");
      buf.append("* This method is called after each test method, to perform any common\n");
      buf.append("* clean-up if necessary.\n");
      buf.append("*/\n");
      buf.append("public void tearDown() {\n}\n\n");
    }
    buf.append("/**\n");
    buf.append("* A test method.\n");
    buf.append("* (Replace \"X\" with a few words describing the test.  You may write\n");
    buf.append("* as many \"testSomething\" methods in this class as you wish,\n");
    buf.append("* and each one will be executed when running JUnit over this class.)\n");
    buf.append("*/\n");
    buf.append("public void testX() {\n}\n");
    buf.append("}\n");
    String test = buf.toString();

    OpenDefinitionsDocument openDoc = newFile();
    DefinitionsDocument doc = openDoc.getDocument();
    try {
      doc.insertString(0, test, null);
      doc.indentLines(0, test.length());
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    return openDoc;
  }

  //---------------------- Specified by ILoadDocuments ----------------------//

  /**
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return an absolute path, as this method makes
   * it absolute.
   * @see ILoadDocuments
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {
    final File file = (com.getFiles())[0].getAbsoluteFile();
    OpenDefinitionsDocument odd = _openFile(file);
    
    // Make sure this is on the classpath
    try {
      File classpath = odd.getSourceRoot();
      _interactionsModel.addToClassPath(classpath.getAbsolutePath());
    }
    catch (InvalidPackageException e) {
      // Invalid package-- don't add it to classpath
    }
    
    return odd;
  }

  /**
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return an absolute path, as this method makes
   * it absolute.
   * @see ILoadDocuments
   */
  public OpenDefinitionsDocument openFiles(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {

    final File[] files = com.getFiles();
    OpenDefinitionsDocument retDoc = null;
    
    if (files == null)
      throw new IOException("No Files returned from FileSelector");

    AlreadyOpenException storedAOE = null;
    
    for (int i=0; i < files.length; i++) {
      if (files[i] == null) {
        throw new IOException("File name returned from FileSelector is null");
      }
        
      try {
      //always return last opened Doc
      retDoc = _openFile(files[i].getAbsoluteFile());
      }
      catch (AlreadyOpenException aoe) {
        retDoc = aoe.getOpenDocument();
        //Remember the first AOE
        if (storedAOE == null) {
          storedAOE = aoe;
        }
      }
      
      // Make sure this is on the classpath
      try {
        File classpath = retDoc.getSourceRoot();
        _interactionsModel.addToClassPath(classpath.getAbsolutePath());
      }
      catch (InvalidPackageException e) {
        // Invalid package-- don't add it to classpath
      }
      
    }
    
    if (storedAOE != null) throw storedAOE;
    
    if (retDoc != null) {
      return retDoc;
    }
    else {
      //if no OperationCanceledException, then getFiles should
      //have at least one file.
      throw new IOException("No Files returned from FileChooser");
    }
  }
  
  //----------------------- End ILoadDocuments Methods -----------------------//
 
  /**
   * Saves all open files, prompting for names if necessary.
   * When prompting (ie, untitled document), set that document as active.
   * @param com a selector that picks the file name, used for each
   * @exception IOException
   */
  public void saveAllFiles(FileSaveSelector com) throws IOException {
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.getElementAt(i);
      aboutToSaveFromSaveAll(doc);
      doc.saveFile(com);
    }
  }

  /**
   * Does nothing in default model.
   * @param doc the document which is about to be saved by a save all
   *            command
   */
  public void aboutToSaveFromSaveAll(OpenDefinitionsDocument doc) {}

  /**
   * Closes an open definitions document, prompting to save if
   * the document has been changed.  Returns whether the file
   * was successfully closed.
   * @return true if the document was closed
   */
  public boolean closeFile(OpenDefinitionsDocument doc) {
    boolean canClose = doc.canAbandonFile();
    final OpenDefinitionsDocument closedDoc = doc;
    if (canClose) {
      doc.removeFromDebugger();
      // Only fire event if doc exists and was removed from list
      if (_definitionsDocs.removeElement(doc)) {
        _notifier.notifyListeners(new EventNotifier.Notifier() {
          public void notifyListener(GlobalModelListener l) {
          l.fileClosed(closedDoc);
        }
        });
        return true;
      }
    }
    return false;
  }

  /**
   * Attempts to close all open documents.
   * @return true if all documents were closed
   */
  public boolean closeAllFiles() {
    boolean keepClosing = true;
    while (!_definitionsDocs.isEmpty() && keepClosing) {
      OpenDefinitionsDocument openDoc = (OpenDefinitionsDocument)
        _definitionsDocs.get(0);
      keepClosing = closeFile(openDoc);
    }
    return keepClosing;
  }

  /**
   * Reverts all open files.
   * Not working yet: causes an exception in the reduced model if a
   * non-active document is reverted...?
   *
  public void revertAllFiles() throws IOException {
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.getElementAt(i);
      if (!doc.isUntitled()) {
        doc.revertFile();
      }
    }
  }*/

  /**
   * Exits the program.
   * Only quits if all documents are successfully closed.
   */
  public void quit() {
    if (closeAllFiles()) {
      dispose();  // kills the interpreter

      if (DrJava.getSecurityManager() != null) {
        DrJava.getSecurityManager().exitVM(0);
      }
      else {
        // If we are being debugged by another copy of DrJava,
        //  then we have no security manager.  Just exit cleanly.
        System.exit(0);
      }
        
    }
  }
  
  /**
   * Prepares this model to be thrown away.  Never called in
   * practice outside of quit(); only used in tests.
   */
  public void dispose() {
    // Kill the interpreter
    _interpreterControl.killInterpreter(false);

    _notifier.removeAllListeners();
    _definitionsDocs.clear();
  }

  //----------------------- Specified by IGetDocuments -----------------------//
  
  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException
  {
    // Check if this file is already open
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null) {
      // If not, open and return it
      final File f = file;
      
      // TODO: Is this class construction overhead really necessary?
      FileOpenSelector selector = new FileOpenSelector() {
        public File getFile() throws OperationCanceledException {
          return f;
        }

        public File[] getFiles() throws OperationCanceledException {
          return new File[] {f};
        }
      };
      try {
        doc = openFile(selector);
      }
      catch (AlreadyOpenException aoe) {
        doc = aoe.getOpenDocument();
      }
      catch (OperationCanceledException oce) {
        // Cannot happen, since we don't throw it in our selector
        throw new UnexpectedException(oce);
      }
    }
    return doc;
  }
  
  /**
   * Iterates over OpenDefinitionsDocuments, looking for this file.
   * TODO: This is not very efficient!
   */
  public boolean isAlreadyOpen(File file) {
    return (_getOpenDocument(file) != null);
  }
  
  /**
   * Simply returns a reference to our internal ListModel.
   * TODO: Protect this object from untrusted code!
   * @deprecated Use getDefinitionsDocuments().
   */
  public ListModel getDefinitionsDocs() {
    return _definitionsDocs;
  }
  
  /**
   * Returns a collection of all documents currently open for editing.
   * This is equivalent to the results of getDocumentForFile for the set
   * of all files for which isAlreadyOpen returns true.
   * @return a random-access List of the open definitions documents.
   */
  public List<OpenDefinitionsDocument> getDefinitionsDocuments() {
    ArrayList<OpenDefinitionsDocument> docs =
      new ArrayList<OpenDefinitionsDocument>(_definitionsDocs.size());
    java.util.Enumeration en = _definitionsDocs.elements();
  
    while (en.hasMoreElements()) {
      docs.add((OpenDefinitionsDocument) en.nextElement());
    }
  
    return docs;
  }
  
  //----------------------- End IGetDocuments Methods -----------------------//

  /**
   * Set the indent tab size for all definitions documents.
   * @param indent the number of spaces to make per level of indent
   */
  void setDefinitionsIndent(int indent) {
    for (int i = 0; i < _definitionsDocs.size(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.get(i);
      doc.setDefinitionsIndent(indent);
    }
  }

  /**
   * Clears and resets the interactions pane.
   * Bug #576179 pointed out that this needs to end any threads that were
   * running in the interactions JVM, so we completely restart the JVM now.
   * Ideally, we'd like a way to end any running threads and cleanly reset
   * the interpreter (to speed up this method), but that might be too complex...
   * <p>
   * (Old approach:
   * First it makes sure it's in the right package given the
   * package specified by the definitions.  If it can't,
   * the package for the interactions becomes the defualt
   * top level. In either case, this method calls a helper
   * which fires the interactionsReset() event.)
   */
  public void resetInteractions() {
    if ((_debugger.isAvailable()) && (_debugger.isReady())) {
      _debugger.shutdown();
    }
    
    _interactionsModel.resetInterpreter();
    //_restoreInteractionsState();
    
    /* Old approach.  (Didn't kill leftover interactions threads)
    _interpreterControl.reset();
    _restoreInteractionsState();
    */
  }


  /**
   * Resets the console.
   * Fires consoleReset() event.
   */
  public void resetConsole() {
    _consoleDoc.reset();

    _notifier.notifyListeners(new EventNotifier.Notifier() {
      public void notifyListener(GlobalModelListener l) {
        l.consoleReset();
      }
    });
  }

  /**
   * Interprets the current given text at the prompt in the interactions
   * pane.
   */
  public void interpretCurrentInteraction() {
    _interactionsModel.interpretCurrentInteraction();
  }

  /**
   * Interprets the file selected in the FileOpenSelector. Assumes all strings
   * have no trailing whitespace. Interprets the array all at once so if there are
   * any errors, none of the statements after the first erroneous one are processed.
   */
  public void loadHistory(FileOpenSelector selector) throws IOException {
    _interactionsModel.loadHistory(selector);
  }
  
  /**
   * Clears the interactions history
   */
  public void clearHistory() {
    _interactionsModel.getDocument().clearHistory();
  }
  
  /**
   * Saves the unedited version of the current history to a file
   * @param selector File to save to
   */
  public void saveHistory(FileSaveSelector selector) throws IOException {
    _interactionsModel.getDocument().saveHistory(selector);
  }

  /**
   * Saves the edited version of the current history to a file
   * @param selector File to save to
   * @param editedVersion Edited verison of the history which will be
   * saved to file instead of the lines saved in the history. The saved
   * file will still include any tags needed to recognize it as a saved
   * interactions file.
   */
  public void saveHistory(FileSaveSelector selector, String editedVersion)
    throws IOException
  {
    _interactionsModel.getDocument().saveHistory(selector, editedVersion);
  }
 
  /**
   * Returns the entire history as a String with semicolons as needed
   */
  public String getHistoryAsStringWithSemicolons() {
    return _interactionsModel.getDocument().getHistoryAsStringWithSemicolons();
  }
  
  /**
   * Returns the entire history as a String
   */
  public String getHistoryAsString() {
    return _interactionsModel.getDocument().getHistoryAsString();
  }
  
  /**
   * Registers OptionListeners.  Factored out code from the two constructors
   */
  private void _registerOptionListeners(){
    // Listen to any relevant config options
    DrJava.getConfig().addOptionListener(EXTRA_CLASSPATH,
                                         new ExtraClasspathOptionListener());
    DrJava.getConfig().addOptionListener(BACKUP_FILES,
                                         new BackUpFileOptionListener());
    Boolean makeBackups = DrJava.getConfig().getSetting(BACKUP_FILES);
    FileOps.DefaultFileSaver.setBackupsEnabled(makeBackups.booleanValue());
  }
  
  /**
   * Appends a string to the given document using a particular attribute set.
   * Also waits for a small amount of time (WRITE_DELAY) to prevent any one
   * writer from flooding the model with print calls to the point that the
   * user interface could become unresponsive.
   * @param doc Document to append to
   * @param s String to append to the end of the document
   * @param style the style to print with
   */
  private void _docAppend(ConsoleDocument doc, String s, String style) {
    synchronized(_systemWriterLock) {
      try {
        doc.insertBeforeLastPrompt(s, style);
        
        // Wait to prevent being flooded with println's
        _systemWriterLock.wait(WRITE_DELAY);
      }
      catch (InterruptedException e) {
        // It's ok, we'll go ahead and resume
      }
    }
  }
  
 
  /**
   * Prints System.out to the DrJava console.
   */
  public void systemOutPrint(String s) {
    _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_OUT_STYLE);
  }

  /**
   * Prints System.err to the DrJava console.
   */
  public void systemErrPrint(String s) {
    _docAppend(_consoleDoc, s, ConsoleDocument.SYSTEM_ERR_STYLE);
  }

  /** Called when the repl prints to System.out.
  public void replSystemOutPrint(String s) {
    systemOutPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_OUT_STYLE);
  } */

  /** Called when the repl prints to System.err.
  public void replSystemErrPrint(String s) {
    systemErrPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, InteractionsDocument.SYSTEM_ERR_STYLE);
  } */

  /** Called when the debugger wants to print a message.  Inserts a newline. */
  public void printDebugMessage(String s) {
    _interactionsModel.getDocument().
      insertBeforeLastPrompt(s + "\n", InteractionsDocument.DEBUGGER_STYLE);
  }

  
  /**
   * Blocks until the interpreter has registered.
   */
  public void waitForInterpreter() {
    _interpreterControl.ensureInterpreterConnected();
  }

  /**
   * Returns all registered compilers that are actually available.
   * That is, for all elements in the returned array, .isAvailable()
   * is true.
   * This method will never return null or a zero-length array.
   *
   * @see CompilerRegistry#getAvailableCompilers
   */
  public CompilerInterface[] getAvailableCompilers() {
    return CompilerRegistry.ONLY.getAvailableCompilers();
  }

  /**
   * Sets which compiler is the "active" compiler.
   *
   * @param compiler Compiler to set active.
   *
   * @see #getActiveCompiler
   * @see CompilerRegistry#setActiveCompiler
   */
  public void setActiveCompiler(CompilerInterface compiler) {
    CompilerRegistry.ONLY.setActiveCompiler(compiler);
  }

  /**
   * Gets the compiler is the "active" compiler.
   *
   * @see #setActiveCompiler
   * @see CompilerRegistry#getActiveCompiler
   */
  public CompilerInterface getActiveCompiler() {
    return CompilerRegistry.ONLY.getActiveCompiler();
  }

  /**
   * Returns the current classpath in use by the Interpreter JVM.
   */
  public Vector<String> getClasspath() {
    return _interpreterControl.getClasspath();
    /*
    String separator= System.getProperty("path.separator");
    String classpath= "";
    File[] sourceFiles = getSourceRootSet();

    for(int i=0; i < sourceFiles.length; i++) {
      classpath += sourceFiles[i].getAbsolutePath() + separator;
    }

    // Adds extra.classpath to the classpath.
    Vector<File> extraClasspath = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if(extraClasspath != null) {
        Enumeration<File> en = extraClasspath.elements();
        while(en.hasMoreElements()) {
            classpath += en.nextElement().getAbsolutePath() + separator;
        }
    }
    return classpath;
    */
  }
  
  /**
   * Gets an array of all sourceRoots for the open definitions
   * documents, without duplicates. Note that if any of the open
   * documents has an invalid package statement, it won't be added
   * to the source root set. On 8.7.02 changed the sourceRootSet such that
   * the directory DrJava was executed from is now after the sourceRoots
   * of the currently open documents in order that whatever version the user
   * is looking at corresponds to the class file the interactions window
   * uses.
   */
  public File[] getSourceRootSet() {
    LinkedList<File> roots = new LinkedList<File>();

    for (int i = 0; i < _definitionsDocs.size(); i++) {
      OpenDefinitionsDocument doc
        = (OpenDefinitionsDocument) _definitionsDocs.get(i);

      try {
        File root = doc.getSourceRoot();

        // Don't add duplicate Files, based on path
        if (!roots.contains(root)) {
          roots.add(root);
        }
      }
      catch (InvalidPackageException e) {
        // oh well, invalid package statement for this one
        // can't add it to roots
      }
    }
    /*
    File workDir = DrJava.getConfig().getSetting(WORKING_DIRECTORY);
        
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File( System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    roots.add(workDir);*/

    return (File[]) roots.toArray(new File[0]);
  }
  
  /**
   * Sets whether or not the Interactions JVM will be reset after
   * a compilation succeeds.  This should ONLY be used in tests!
   * @param shouldReset Whether to reset after compiling
   *
  void setResetAfterCompile(boolean shouldReset) {
    _resetAfterCompile = shouldReset;
  }*/
  
  /**
   * Compiles all open documents, after ensuring that all are saved.
   *
   * This method used to only compile documents which were out of sync
   * with their class file, as a performance optimization.  However,
   * bug #634386 pointed out that unmodified files could depend on
   * modified files, in which case this would not recompile a file in
   * some situations when it should.  Since we value correctness over
   * performance, we now always compile all open documents.
   */
  public void compileAll() throws IOException {
    synchronized(_compilerLock) {
      // Only compile if all are saved
      if (hasModifiedDocuments()) {
        _notifier.saveBeforeCompile();
      }
      
      if (hasModifiedDocuments()) {
        // if any files haven't been saved after we told our
        // listeners to do so, don't proceed with the rest
        // of the compile.
      }
      else {
        // Get sourceroots and all files
        File[] sourceRoots = getSourceRootSet();
        ArrayList<File> filesToCompile = new ArrayList<File>();
        for (int i = 0; i < _definitionsDocs.getSize(); i++) {
          OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
            _definitionsDocs.getElementAt(i);
          try {
            filesToCompile.add(doc.getFile());
          }
          catch (IllegalStateException ise) {
            // No file for this document; skip it
          }
        }
        File[] files = (File[]) filesToCompile.toArray(new File[0]);
        
        _notifier.notifyListeners(new EventNotifier.Notifier() {
          public void notifyListener(GlobalModelListener l) {
            l.compileStarted();
          }
        });
        
        try {
          // Compile the files
          _compileFiles(sourceRoots, files);
        }
        catch (Throwable t) {
          CompilerError err = new CompilerError(t.toString(), false);
          CompilerError[] errors = new CompilerError[] { err };
          _distributeErrors(errors);
        }
        finally {
          // Fire a compileEnded event
          _notifier.notifyListeners(new EventNotifier.Notifier() {
            public void notifyListener(GlobalModelListener l) {
              l.compileEnded();
            }
          });
          
          // Only clear interactions if there were no errors
          if (_numErrors == 0) {
            //resetConsole();
            if (/*_resetAfterCompile && */
                _interactionsModel.interpreterUsed()) {
              resetInteractions();
            }
          }
        }
      }
    }
  }

  /**
   * Compile the given files (with the given sourceroots), and update
   * the model with any errors that result.  Does not notify listeners;
   * use compileAll or doc.startCompile instead.
   * @param sourceRoots An array of all sourceroots for the files to be compiled
   * @param files An array of all files to be compiled
   */
  protected void _compileFiles(File[] sourceRoots, File[] files) throws IOException {
    
    CompilerError[] errors = new CompilerError[0];
    
    CompilerInterface compiler
      = CompilerRegistry.ONLY.getActiveCompiler();
      
    if (files.length > 0) {
      errors = compiler.compile(sourceRoots, files);
    }
    _distributeErrors(errors);
  }

  /**
   * Gets the Debugger used by DrJava.
   */
  public Debugger getDebugger() {
    return _debugger;
  }
  
  /**
   * Returns an available port number to use for debugging the interactions JVM.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException {
    return _interactionsModel.getDebugPort();
  }
  
  /**
   * Checks if any open definitions documents have been modified
   * since last being saved.
   * @return whether any documents have been modified
   */
  public boolean hasModifiedDocuments() {
    boolean modified = false;
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc =
        (OpenDefinitionsDocument)_definitionsDocs.getElementAt(i);
      if (doc.isModifiedSinceSave()) {
        modified = true;
        break;
      }
    }
    return modified;
  }
  
  /**
   * Searches for a file with the given name on the provided paths.
   * Returns null if the file is not found.
   * @param filename Name of the source file to look for
   * @param paths An array of directories to search
   */
  public File getSourceFileFromPaths(String filename, Vector<File> paths) {
    File f = null;
    for (int i = 0; i < paths.size(); i++) {
      String currRoot = paths.elementAt(i).getAbsolutePath();
      f = new File(currRoot + System.getProperty("file.separator") + filename);
      if (f.exists()) {
        return f;
      }
    }
    return null;
  }
  
  /**
   * Called from the JUnitTestManager if its given className is not a test case.
   */
  public void nonTestCase() {
    synchronized(_compilerLock) {
      _docBeingTested = null;
      _notifier.notifyListeners(new EventNotifier.Notifier() {
        public void notifyListener(GlobalModelListener l) {
          l.nonTestCase();
          l.junitEnded();
        }
      });
    }
  }
  
  /**
   * Called to indicate that a suite of tests has started running.
   * @param numTests The number of tests in the suite to be run.
   */
  public void testSuiteStarted(final int numTests) {
    synchronized(_compilerLock) {
      _notifier.notifyListeners(new EventNotifier.Notifier() {
        public void notifyListener(GlobalModelListener l) {
          l.junitSuiteStarted(numTests);
        }
      });
    }
  }
  
  /**
   * Called when a particular test is started.
   * @param testName The name of the test being started.
   */
  public void testStarted(final String testName) {
    synchronized(_compilerLock) {
      _notifier.notifyListeners(new EventNotifier.Notifier() {
        public void notifyListener(GlobalModelListener l) {
          l.junitTestStarted(testName);
        }
      });
    }
  }
  
  /**
   * Called when a particular test has ended.
   * @param testName The name of the test that has ended.
   * @param wasSuccessful Whether the test passed or not.
   * @param causedError If not successful, whether the test caused an error
   *  or simply failed.
   */
  public void testEnded(final String testName, final boolean wasSuccessful,
                        final boolean causedError)
  {
    synchronized(_compilerLock) {
      _notifier.notifyListeners(new EventNotifier.Notifier() {
        public void notifyListener(GlobalModelListener l) {
          l.junitTestEnded(_docBeingTested, testName, wasSuccessful, causedError);
        }
      });
    }
  }
  
  /**
   * Called when a full suite of tests has finished running.
   * @param errors The array of errors from all failed tests in the suite.
   */
  public void testSuiteEnded(JUnitError[] errors) {
    synchronized(_compilerLock) {
      if (_docBeingTested == null) {
        return;
      }
      _junitErrorModel = new JUnitErrorModel(errors, this, true);

      _docBeingTested = null;
      _notifier.notifyListeners(new EventNotifier.Notifier() {
        public void notifyListener(GlobalModelListener l) {
          l.junitEnded();
        }
      });
    }
  }
  
  /**
   * Returns the document currently being tested (with JUnit) if there is
   * one, otherwise null.
   */
  public OpenDefinitionsDocument getDocBeingTested() {
    return _docBeingTested;
  }
    
  // ---------- DefinitionsDocumentHandler inner class ----------
  
  /**
   * Inner class to handle operations on each of the open
   * DefinitionsDocuments by the GlobalModel.
   */
  private class DefinitionsDocumentHandler implements OpenDefinitionsDocument {
    private final DefinitionsDocument _doc;
    // TODO: Should these be document-specific?  They aren't used as such now.
//    private CompilerErrorModel _errorModel;
//    private JUnitErrorModel _junitErrorModel;
    private DrJavaBook _book;
    private Vector<Breakpoint> _breakpoints;

//    boolean _shouldRun;
//    private GlobalModelListener _notifyListener = new DummySingleDisplayModelListener() {
//      public synchronized void interpreterReady() {
//        notify();
//      }
//      public synchronized void interperterResetting() {
//        notify();
//        _shouldRun = false;
//      }
//      public synchronized void interactionEnded() {
//        notify();
//      }
//    };

    /**
     * Constructor.  Initializes this handler's document.
     * @param doc DefinitionsDocument to manage
     */
    DefinitionsDocumentHandler(DefinitionsDocument doc) {
      _doc = doc;
//      _errorModel = new CompilerErrorModel<CompilerError> (new CompilerError[0], null);
//      _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);
      _breakpoints = new Vector<Breakpoint>();
    }

    /**
     * Gets the definitions document being handled.
     * @return document being handled
     */
    public DefinitionsDocument getDocument() {
      return _doc;
    }

    /**
     * Returns the name of the top level class, if any.
     * @throws ClassNameNotFoundException if no top level class name found.
     */
    public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
      return _doc.getFirstTopLevelClassName();
    }
    
    /**
     * Returns whether this document is currently untitled
     * (indicating whether it has a file yet or not).
     * @return true if the document is untitled and has no file
     */
    public boolean isUntitled() {
      return _doc.isUntitled();
    }

    /**
     * Returns the file for this document.  If the document
     * is untitled and has no file, it throws an IllegalStateException.
     * @return the file for this document
     * @exception IllegalStateException if no file exists
     */
    public File getFile() throws IllegalStateException, FileMovedException {
      return _doc.getFile();
    }
    
    /**
     * Returns the name of this file, or "(untitled)" if no file.
     */
    public String getFilename() {
      return _doc.getFilename();
    }
    
    // TODO: Move this to where it can be static.
    private class TrivialFSS implements FileSaveSelector {
      private File _file;
      private TrivialFSS(File file) {
        _file = file;
      }
      public File getFile() throws OperationCanceledException {
        return _file;
      }
      public void warnFileOpen() {}
      public boolean verifyOverwrite() {
        return true;
      }
      public boolean shouldSaveAfterFileMoved(OpenDefinitionsDocument doc,
                                              File oldFile) {
        return true;
      }
    }

    /**
     * Saves the document with a FileWriter.  If the file name is already
     * set, the method will use that name instead of whatever selector
     * is passed in.
     * @param com a selector that picks the file name if the doc is untitled
     * @exception IOException
     * @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFile(FileSaveSelector com) throws IOException {
      FileSaveSelector realCommand;
      final File file;
      
      if (!isModifiedSinceSave()) {
        // Don't need to save.
        //  Return true, since the save wasn't "canceled"
        return true;
      }

      try {
        if (_doc.isUntitled()) {
          realCommand = com;
        }
        else {
          try {
            file = _doc.getFile();
            realCommand = new TrivialFSS(file);
          }
          catch (FileMovedException fme) {
            // getFile() failed, prompt the user if a new one should be selected
            if (com.shouldSaveAfterFileMoved(this, fme.getFile())) {
              realCommand = com;
            }
            else {
              // User declines to save as a new file, so don't save
              return false;
            }
          }
        }

        return saveFileAs(realCommand);
      }
      catch (IllegalStateException ise) {
        // No file--  this should have been caught by isUntitled()
        throw new UnexpectedException(ise);
      }
    }

    /**
     * Saves the document with a FileWriter.  The FileSaveSelector will
     * either provide a file name or prompt the user for one.  It is
     * up to the caller to decide what needs to be done to choose
     * a file to save to.  Once the file has been saved succssfully,
     * this method fires fileSave(File).  If the save fails for any
     * reason, the event is not fired.
     * @param com a selector that picks the file name.
     * @throws IOException if the save fails due to an IO error
     * @return true if the file was saved, false if the operation was canceled
     */
    public boolean saveFileAs(FileSaveSelector com) throws IOException {
      try {
        final OpenDefinitionsDocument openDoc = this;
        final File file = com.getFile();
        final OpenDefinitionsDocument otherDoc = _getOpenDocument(file);
        
        // Check if file is already open in another document
        if ( otherDoc != null && openDoc != otherDoc ) {
          // Can't save over an open document
          com.warnFileOpen();
        }
        
        // If the file exists, make sure it's ok to overwrite it
        else if (!file.exists() || com.verifyOverwrite()) {
          
          // Correct the case of the filename (in Windows)
          if (! file.getCanonicalFile().getName().equals(file.getName())) {
            file.renameTo(file);
          }

          // have FileOps save the file
          FileOps.saveFile(new FileOps.DefaultFileSaver(file){
            public void saveTo(OutputStream os) throws IOException {
              try {
                _editorKit.write(os, _doc, 0, _doc.getLength());
              } catch (BadLocationException docFailed){
                // We don't expect this to happen
                throw new UnexpectedException(docFailed);
              }
            }
          });
          
          _doc.resetModification();
          _doc.setFile(file);
          _doc.setCachedClassFile(null);
          checkIfClassFileInSync();
          _notifier.notifyListeners(new EventNotifier.Notifier() {
            public void notifyListener(GlobalModelListener l) {
              l.fileSaved(openDoc);
            }
          });

          // Make sure this file is on the classpath
          try {
            File classpath = getSourceRoot();
            _interactionsModel.addToClassPath(classpath.getAbsolutePath());
          }
          catch (InvalidPackageException e) {
            // Invalid package-- don't add to classpath
          }
        }
        
        return true;
        
      }
      catch (OperationCanceledException oce) {
        // Thrown by com.getFile() if the user cancels.
        //   We don't save if this happens.
        return false;
      }
    }

    /**
     * This method tells the document to prepare all the DrJavaBook
     * and PagePrinter objects.
     */
    public void preparePrintJob() throws BadLocationException,
      FileMovedException {
      
      String filename = "(untitled)";
      try {
        filename = _doc.getFile().getAbsolutePath();
      }
      catch (IllegalStateException e) {
      }

      _book = new DrJavaBook(_doc.getText(0, _doc.getLength()), filename, _pageFormat);
    }

    /**
     * Prints the given document by bringing up a
     * "Print" window.
     */
    public void print() throws PrinterException, BadLocationException,
      FileMovedException
    {
      preparePrintJob();
      PrinterJob printJob = PrinterJob.getPrinterJob();
      printJob.setPageable(_book);
      if (printJob.printDialog()) {
        printJob.print();
      }
      cleanUpPrintJob();
    }

    /**
     * Returns the Pageable object for printing.
     * @return A Pageable representing this document.
     */
    public Pageable getPageable() throws IllegalStateException {
      return _book;
    }

    public void cleanUpPrintJob() {
      _book = null;
    }

    /**
     * Starts compiling the source.  Demands that the definitions be
     * saved before proceeding with the compile. If the compile can
     * proceed, a compileStarted event is fired which guarantees that
     * a compileEnded event will be fired when the compile finishes or
     * fails.  If the compilation succeeds, then a call is
     * made to resetInteractions(), which fires an
     * event of its own, contingent on the conditions.  If the current
     * package as determined by getSourceRoot(String) and getPackageName()
     * is invalid, compileStarted and compileEnded will fire, and
     * an error will be put in compileErrors.
     *
     * (Interactions are not reset if the _resetAfterCompile field is
     * set to false, which allows some test cases to run faster.)
     */
    public void startCompile() throws IOException {
      synchronized(_compilerLock) {
        // Only compile if all are saved
        if (hasModifiedDocuments()) {
          _notifier.saveBeforeCompile();
        }
        
        if (hasModifiedDocuments()) {
          // if any files haven't been saved after we told our
          // listeners to do so, don't proceed with the rest
          // of the compile.
        }
        else {
          try {
            File file = _doc.getFile();
            File[] files = new File[] { file };
            
            try {
              _notifier.notifyListeners(new EventNotifier.Notifier() {
                public void notifyListener(GlobalModelListener l) {
                  l.compileStarted();
                }
              });
              
              File[] sourceRoots = new File[] { getSourceRoot() };
              
              _compileFiles(sourceRoots, files);
            }
            catch (Throwable e) {
              CompilerError err = new CompilerError(file,
                                                    -1,
                                                    -1,
                                                    e.getMessage(),
                                                    false);
              CompilerError[] errors = new CompilerError[] { err };
              _distributeErrors(errors);
            }
            finally {
              // Fire a compileEnded event
              _notifier.notifyListeners(new EventNotifier.Notifier() {
                public void notifyListener(GlobalModelListener l) {
                  l.compileEnded();
                }
              });
              
              // Only clear interactions if there were no errors
              if (_numErrors == 0) {
                //resetConsole();
                if (/*_resetAfterCompile && */
                    _interactionsModel.interpreterUsed()) {
                  resetInteractions();
                }
              }
            }
          }
          catch (IllegalStateException ise) {
            // No file exists, don't try to compile
          }
        }
      }
    }
  
    /**
     * Runs the main method in this document in the interactions pane.
     * Demands that the definitions be saved and compiled before proceeding.
     * Fires an event to signal when execution is about to begin.
     * @exception ClassNameNotFoundException propagated from getFirstTopLevelClass()
     * @exception IOException propagated from GlobalModel.compileAll()
     */
    public void runMain() throws ClassNameNotFoundException, IOException {
      try {
        // First, get the class name to use.  This relies on Java's convention of
        // one top-level class per file.
        DefinitionsDocument doc = getDocument();
        String className = doc.getQualifiedClassName();
        /*  Do not compile in any case.
        // Prompt to save and compile if any document is modified.
        if (hasModifiedDocuments()) {
          _notifier.saveBeforeRun();
          
          // If the user chose to cancel, abort the run.
          if (hasModifiedDocuments()) {
            return;
          }
        }
        // If no document is modified, still compile the current doc.
        // compile only if class file out of sync
        if (!checkIfClassFileInSync()) {
          startCompile();
        }
        */
        // Make sure that the compiler is done before continuing.
        synchronized(_compilerLock) {
          // If the compile had errors, abort the run.
          if (!_compilerErrorModel.hasOnlyWarnings()) {
            return;
          }
        }
        // Then clear the current interaction and replace it with a "java X" line.
        InteractionsDocument iDoc = _interactionsModel.getDocument();
//        if (iDoc.inProgress()) {
//          addListener(_notifyListener);
//          _shouldRun = true;
//          synchronized(_notifyListener) {
//            try {
//              _notifyListener.wait();
//            }
//            catch(InterruptedException ie) {
//            }
//          }
//          removeListener(_notifyListener);
//          if (!_shouldRun) {
//            // The interactions pane was reset during another interaction.
//            //  Don't run the main method.
//            return;
//          }
//        }
        synchronized (_interpreterControl) {
          iDoc.clearCurrentInput();
        
          iDoc.insertText(iDoc.getDocLength(), "java " + className, null);
        
          // Notify listeners that the file is about to be run.
          _notifier.runStarted(this);
        
          // Finally, execute the new interaction.
          _interactionsModel.interpretCurrentInteraction();
        }
      }
      catch (DocumentAdapterException e) {
        // This was thrown by insertText - and shouldn't have happened.
        throw new UnexpectedException(e);
      }
    }

    /**
     * Runs JUnit on the current document. Used to compile all open documents
     * before testing but have removed that requirement in order to allow the
     * debugging of test cases. If the classes being tested are out of
     * sync, a message is displayed.
     *
     * @return The results of running the tests specified in the
     * given definitions document.
     *
     */
    public void startJUnit() throws ClassNotFoundException, IOException{
      synchronized(_compilerLock) {
        //JUnit started, so throw out all JUnitErrorModels now, regardless of whether
        //  the tests succeed, etc.
        
        // if a test is running, don't start another one
        if (_docBeingTested != null) {
          return;
        }

        //reset the JUnitErrorModel
        // TODO: does this need to be done here?
        _junitErrorModel = new JUnitErrorModel(new JUnitError[0], null, false);

        // Compile and save before proceeding.
        /*saveAllBeforeProceeding(GlobalModelListener.JUNIT_REASON);
        if (areAnyModifiedSinceSave()) {
          return;
        }*/
        try {
          File testFile = getFile();
          /*
          compileAll();
          if(getNumErrors() > 0) {
            _notifier.notifyListeners(new EventNotifier.Notifier() {
              public void notifyListener(GlobalModelListener l) {
                l.compileErrorDuringJUnit();
              }
            });
            return;
          }
          */
          _notifier.notifyListeners(new EventNotifier.Notifier() {
            public void notifyListener(GlobalModelListener l) {
              l.junitStarted(DefinitionsDocumentHandler.this);
            }
          });
          
          try {
            getJUnitDocument().remove(0, getJUnitDocument().getLength() - 1);
          }
          catch (BadLocationException e) {
            nonTestCase();
            return;
          }
          
          String testFilename = testFile.getName();
          if (testFilename.toLowerCase().endsWith(".java")) {
            testFilename = testFilename.substring(0, testFilename.length() - 5);
          }
          else {
            nonTestCase();
            return;
          }
          String packageName;
          try {
            packageName = _doc.getPackageName();
          }
          catch (InvalidPackageException e) {
            nonTestCase();
            return;
          }
          if(!packageName.equals("")) {
            testFilename = packageName + "." + testFilename;
          }
          _interpreterControl.runTestSuite(testFilename,
                                           testFile.getAbsolutePath());
          // Assign _docBeingTested after calling runTest because we know at
          // this point that the interpreterJVM has registered itself. We also
          // know that the testFinished cannot be entered before this because
          // it has to acquire the same lock as this method.
          _docBeingTested = this;
          
        }
        catch (IllegalStateException e) {
          // No file exists, don't try to compile and test
          nonTestCase();
          return;
        }
        catch (NoClassDefFoundError e) {
          // Method getTest in junit.framework.BaseTestRunner can throw a
          // NoClassDefFoundError (via reflection).
          _docBeingTested = null;
          _notifier.notifyListeners(new EventNotifier.Notifier() {
            public void notifyListener(GlobalModelListener l) {
              l.junitEnded();
            }
          });
          throw e;
        }
        catch (ExitingNotAllowedException enae) {
          _docBeingTested = null;
          _notifier.notifyListeners(new EventNotifier.Notifier() {
            public void notifyListener(GlobalModelListener l) {
              l.junitEnded();
            }
          });
          throw enae;
        }
      }
    }
    
    /**
     * Generates Javadoc for this document, saving the output to a temporary
     * directory.  The location is provided to the javadocEnded event on
     * the given listener.
     * @param saver FileSaveSelector for saving the file if it needs to be saved
     */
    public void generateJavadoc(FileSaveSelector saver) throws IOException {
      // Use the model's classpath, and use the EventNotifier as the listener
      javadocDocument(this, saver, getClasspath(), getNotifier());
    }

    /**
     * Determines if the definitions document has changed since the
     * last save.
     * @return true if the document has been modified
     */
    public boolean isModifiedSinceSave() {
      return _doc.isModifiedSinceSave();
    }

    /**
     * Determines if the definitions document has changed on disk
     * since the last time the document was read.
     * @return true if the document has been modified on disk
     */
    public boolean isModifiedOnDisk() {
      return _doc.isModifiedOnDisk();
    }
  
    /**
     * Checks if the document is modified. If not, searches for the class file
     * corresponding to this document and compares the timestamps of the
     * class file to that of the source file.
     */
    public boolean checkIfClassFileInSync() {
      // If modified, then definitely out of sync
      if(isModifiedSinceSave()) {
        _doc.setClassFileInSync(false);
        return false;
      }
      
      // Look for cached class file
      File classFile = _doc.getCachedClassFile();
      if (classFile == null) {
        // Not cached, so locate the file
        classFile = _locateClassFile();
        _doc.setCachedClassFile(classFile);
        
        if (classFile == null) {
          // couldn't find the class file
          _doc.setClassFileInSync(false);
          return false;
        }
      }
      
      // compare timestamps
      File sourceFile = null;
      try {
        sourceFile = getFile();
      }
      catch (IllegalStateException ise) {
        throw new UnexpectedException(ise);
      }
      catch (FileMovedException fme) {
        _doc.setClassFileInSync(false);
        return false;
      }
      if (sourceFile.lastModified() > classFile.lastModified()) {
        _doc.setClassFileInSync(false);
        return false;
      }
      else {
        _doc.setClassFileInSync(true);
        return true;
      }
    }
    
    /**
     * Returns the class file for this source document, if one could be found.
     * Looks in the source root directories of the open documents, the
     * system classpath, and the "extra.classpath".  Returns null if the
     * class file could not be found.
     */
    private File _locateClassFile() {
      try {
        String className = _doc.getQualifiedClassName();
        String ps = System.getProperty("file.separator");
        // replace periods with the System's file separator
        className = StringOps.replace(className, ".", ps);
        String filename = className + ".class";
        
        // Check source root set (open files)
        File[] sourceRoots = getSourceRootSet();
        Vector<File> roots = new Vector<File>();
        // Add the current document to the beginning of the roots Vector
        try {
          roots.addElement(getSourceRoot());
        }
        catch (InvalidPackageException ipe) {
          try {
            File f = getFile().getParentFile();
            if (f != null) {
              roots.addElement(f);
            }
          }
          catch (IllegalStateException ise) {
            // No file, don't add to source root set
          }
          catch (FileMovedException fme) {
            // Moved, but we'll add the old file to the set anyway
            File root = fme.getFile().getParentFile();
            if (root != null) {
              roots.addElement(root);
            }
          }
        }
        
        for (int i=0; i < sourceRoots.length; i++) {
          roots.addElement(sourceRoots[i]);
        }
        File classFile = getSourceFileFromPaths(filename, roots);
        
        if (classFile == null) {
          // Class not on source root set, check system classpath
          String cp = System.getProperty("java.class.path");
          String pathSeparator = System.getProperty("path.separator");
          Vector<File> cpVector = new Vector<File>();
          for (int i = 0; i < cp.length();) {
            int nextSeparator = cp.indexOf(pathSeparator, i);
            if (nextSeparator == -1) {
              cpVector.addElement(new File(cp.substring(i, cp.length())));
              break;
            }
            cpVector.addElement(new File(cp.substring(i, nextSeparator)));
            i = nextSeparator + 1;
          }
          classFile = getSourceFileFromPaths(filename, cpVector);
        }
        
        if (classFile == null) {
          // not on system classpath, check interactions classpath
          classFile = getSourceFileFromPaths(filename, DrJava.getConfig().getSetting(EXTRA_CLASSPATH));
        }
        
        return classFile;
      }
      catch (ClassNameNotFoundException cnnfe) {
        // No class name found, so we can't find a class file
        return null;
      }
    }
    
      
    /**
     * Determines if the defintions document has been changed
     * by an outside program. If the document has changed,
     * then asks the listeners if the GlobalModel should
     * revert the document to the most recent version saved.
     * @return true if document has been reverted
     */
    public boolean revertIfModifiedOnDisk() throws IOException{
      final OpenDefinitionsDocument doc = this;
      if (isModifiedOnDisk()) {
        
        boolean shouldRevert = _notifier.pollListeners(new EventNotifier.Poller() {
          public boolean poll(GlobalModelListener l) {
            return l.shouldRevertFile(doc);
          }
        });
        if (shouldRevert) {
          doc.revertFile();
        }
        return shouldRevert;
      }
      else {
        return false;
      }
    }
    
    public void revertFile() throws IOException {

      //need to remove old, possibly invalid breakpoints
      removeFromDebugger();
      
      final OpenDefinitionsDocument doc = this;
      
      try {
        File file = doc.getFile();
        //this line precedes the .remove() so that a document with an invalid
        // file is not cleared before this fact is discovered.

        FileReader reader = new FileReader(file);
        DefinitionsDocument tempDoc = doc.getDocument();
        
        tempDoc.remove(0,tempDoc.getLength());
        

        _editorKit.read(reader, tempDoc, 0);
        reader.close(); // win32 needs readers closed explicitly!
        
        tempDoc.resetModification();
        doc.checkIfClassFileInSync();
        
        syncCurrentLocationWithDefinitions(0);
        
        _notifier.notifyListeners(new EventNotifier.Notifier() {
          public void notifyListener(GlobalModelListener l) {
            l.fileReverted(doc);
          }
        });
      }
      catch (IllegalStateException docFailed) {
        //cant revert file if doc has no file
        throw new UnexpectedException(docFailed);
      }
      catch (BadLocationException docFailed) {
        throw new UnexpectedException(docFailed);
      }
    }

    /**
     * Asks the listeners if the GlobalModel can abandon the current document.
     * Fires the canAbandonFile(File) event if isModifiedSinceSave() is true.
     * @return true if the current document may be abandoned, false if the
     * current action should be halted in its tracks (e.g., file open when
     * the document has been modified since the last save).
     */
    public boolean canAbandonFile() {
      final OpenDefinitionsDocument doc = this;
      if (isModifiedSinceSave()) {
        return _notifier.pollListeners(new EventNotifier.Poller() {
          public boolean poll(GlobalModelListener l) {
            return l.canAbandonFile(doc);
          }
        });
      }
      else {
        return true;
      }
    }

    /**
     * Moves the definitions document to the given line, and returns
     * the character position in the document it's gotten to.
     * @param line Number of the line to go to. If line exceeds the number
     *             of lines in the document, it is interpreted as the last line.
     * @return Index into document of where it moved
     */
    public int gotoLine(int line) {
      _doc.gotoLine(line);
      return _doc.getCurrentLocation();
    }

    /**
     * Forwarding method to sync the definitions with whatever view
     * component is representing them.
     */
    public void syncCurrentLocationWithDefinitions(int location) {
      _doc.setCurrentLocation(location);
    }

    /**
     * Get the location of the cursor in the definitions according
     * to the definitions document.
     */
    public int getCurrentDefinitionsLocation() {
      return _doc.getCurrentLocation();
    }

    /**
     * Forwarding method to find the match for the closing brace
     * immediately to the left, assuming there is such a brace.
     * @return the relative distance backwards to the offset before
     *         the matching brace.
     */
    public int balanceBackward() {
      return _doc.balanceBackward();
    }
    
    /**
     * Forwarding method to find the match for the open brace
     * immediately to the right, assuming there is such a brace.
     * @return the relative distance forwards to the offset after
     *         the matching brace.
     */
    public int balanceForward() {
      return _doc.balanceForward();
    }

    /**
     * Set the indent tab size for this document.
     * @param indent the number of spaces to make per level of indent
     */
    public void setDefinitionsIndent(int indent) {
      _doc.setIndent(indent);
    }

    /**
     * A forwarding method to indent the current line or selection
     * in the definitions.
     */
    public void indentLinesInDefinitions(int selStart, int selEnd, int reason) {
      _doc.indentLines(selStart, selEnd, reason);
    }

    /**
     * A forwarding method to comment out the current line or selection
     * in the definitions.
     */
    public void commentLinesInDefinitions(int selStart, int selEnd) {
      _doc.commentLines(selStart, selEnd);
    }

    /**
     * A forwarding method to un-comment the current line or selection
     * in the definitions.
     */
    public void uncommentLinesInDefinitions(int selStart, int selEnd) {
      _doc.uncommentLines(selStart, selEnd);
    }

    /**
     * Create a find and replace mechanism starting at the current
     * character offset in the definitions.
     */
    public FindReplaceMachine createFindReplaceMachine() {
      //try {
      //return new FindReplaceMachine(_doc, _doc.getCurrentLocation());
      return new FindReplaceMachine();
      //}
      //catch (BadLocationException e) {
      //throw new UnexpectedException(e);
      //}
    }

    /**
     * Returns the first Breakpoint in this OpenDefinitionsDocument whose region
     * includes the given offset, or null if one does not exist.
     * @param offset an offset at which to search for a breakpoint
     * @return the Breakpoint at the given lineNumber, or null if it does not exist.
     */
    public Breakpoint getBreakpointAt( int offset) {
      //return _breakpoints.get(new Integer(lineNumber));
      
      for (int i =0; i<_breakpoints.size(); i++) {
        Breakpoint bp = _breakpoints.elementAt(i);
        if (offset >= bp.getStartOffset() && offset <= bp.getEndOffset()) {
          return bp;
        }
      }
      return null;
    }
    
    /**
     * Inserts the given Breakpoint into the list, sorted by region
     * @param breakpoint the Breakpoint to be inserted
     */
    public void addBreakpoint( Breakpoint breakpoint) {
      //_breakpoints.put( new Integer(breakpoint.getLineNumber()), breakpoint);
      
      for (int i=0; i<_breakpoints.size();i++) {
        Breakpoint bp = _breakpoints.elementAt(i);
        int oldStart = bp.getStartOffset();
        int newStart = breakpoint.getStartOffset();
        
        if ( newStart < oldStart) {
          // Starts before, add here
          _breakpoints.insertElementAt(breakpoint, i);
          return;
        }
        if ( newStart == oldStart) {
          // Starts at the same place
          int oldEnd = bp.getEndOffset();
          int newEnd = breakpoint.getEndOffset();
          
          if ( newEnd < oldEnd) {
            // Ends before, add here
            _breakpoints.insertElementAt(breakpoint, i);
            return;
          }
        }
      }
      _breakpoints.addElement(breakpoint);
    }
    
    /**
     * Remove the given Breakpoint from our list (but not the debug manager)
     * @param breakpoint the Breakpoint to be removed.
     */
    public void removeBreakpoint( Breakpoint breakpoint) {
      _breakpoints.removeElement( breakpoint);
    }
    
    /**
     * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects
     * in this document.
     */
    public Vector<Breakpoint> getBreakpoints() {
      return _breakpoints;
    }
    
    /**
     * Tells the document to remove all breakpoints (without removing them
     * from the debug manager).
     */
    public void clearBreakpoints() {
      _breakpoints.removeAllElements();
    }
    
    /**
     * Called to indicate the document is being closed, so to remove
     * all related state from the debug manager.
     */
    public void removeFromDebugger() {
      if (_debugger.isAvailable() && (_debugger.isReady())) {
        try {
          while (_breakpoints.size() > 0) {
            _debugger.removeBreakpoint(_breakpoints.elementAt(0));
          }
        }
        catch (DebugException de) {
          // Shouldn't happen if debugger is active
          throw new UnexpectedException(de);
        }
      }
      else {
        clearBreakpoints();
      }
    }
  
    /**
     * Finds the root directory of the source files.
     * @return The root directory of the source files,
     *         based on the package statement.
     * @throws InvalidPackageException If the package statement is invalid,
     *                                 or if it does not match up with the
     *                                 location of the source file.
     */
    public File getSourceRoot() throws InvalidPackageException
    {
      return _getSourceRoot(_doc.getPackageName());
    }
    
    /**
     * Gets the name of the package this source file claims it's in (with the
     * package keyword). It does this by minimally parsing the source file
     * to find the package statement.
     *
     * @return The name of package this source file declares itself to be in,
     *         or the empty string if there is no package statement (and thus
     *         the source file is in the empty package).
     *
     * @exception InvalidPackageException if there is some sort of a
     *                                    <TT>package</TT> statement but it
     *                                    is invalid.
     */
    public String getPackageName() throws InvalidPackageException {
      return _doc.getPackageName();
    }

    /**
     * Finds the root directory of the source files.
     * @param packageName Package name, already fetched from the document
     * @return The root directory of the source files,
     *         based on the package statement.
     * @throws InvalidPackageException If the package statement is invalid,
     *                                 or if it does not match up with the
     *                                 location of the source file.
     */
    private File _getSourceRoot(String packageName)
      throws InvalidPackageException
    {
      File sourceFile;
      try {
        sourceFile = _doc.getFile();
      }
      catch (IllegalStateException ise) {
        throw new InvalidPackageException(-1, "Can not get source root for " +
                                          "unsaved file. Please save.");
      }
      catch (FileMovedException fme) {
        throw new InvalidPackageException(-1, "File has been moved or deleted " +
                                          "from its previous location. Please save.");
      }
      
      if (packageName.equals("")) {
        return sourceFile.getParentFile();
      }

      Stack<String> packageStack = new Stack<String>();
      int dotIndex = packageName.indexOf('.');
      int curPartBegins = 0;

      while (dotIndex != -1)
      {
        packageStack.push(packageName.substring(curPartBegins, dotIndex));
        curPartBegins = dotIndex + 1;
        dotIndex = packageName.indexOf('.', dotIndex + 1);
      }

      // Now add the last package component
      packageStack.push(packageName.substring(curPartBegins));

      File parentDir = sourceFile;
      while (!packageStack.empty()) {
        String part = (String) packageStack.pop();
        parentDir = parentDir.getParentFile();

        if (parentDir == null) {
          throw new RuntimeException("parent dir is null?!");
        }

        // Make sure the package piece matches the directory name
        if (! part.equals(parentDir.getName())) {
          String msg = "The source file " + sourceFile.getAbsolutePath() +
            " is in the wrong directory or in the wrong package. " +
            "The directory name " + parentDir.getName() +
            " does not match the package component " + part + ".";

          throw new InvalidPackageException(-1, msg);
        }
      }

      // OK, now parentDir points to the directory of the first component of the
      // package name. The parent of that is the root.
      parentDir = parentDir.getParentFile();
      if (parentDir == null) {
        throw new RuntimeException("parent dir of first component is null?!");
      }

      return parentDir;
    }
  }

  /**
   * Resets the compiler error state to have no errors.
   * Also resets the JUnit error state.  Since we went to
   * a single CompilerErrorModel and a single JUnitErrorModel,
   * this <b>should</b> no longer be necessary
   */
  public void resetCompilerErrors() {
    // Reset CompilerErrorModel (and JUnitErrorModel)
    // TODO: see if we can get by without this function
    _compilerErrorModel = new CompilerErrorModel<CompilerError>(new CompilerError[0], this);
    _numErrors = 0;
  }

  /**
   * Resets the junit error state to have no errors.
   */
  public void resetJUnitErrors() {
    _junitErrorModel = new JUnitErrorModel(new JUnitError[0], this, false);
  }

  /**
   * Resets the javadoc error state to have no errors.
   */
  public void resetJavadocErrors() {
    _javadocModel.resetJavadocErrors();
  }
  
  /**
   * Suggests a default location for generating Javadoc, based on the given
   * document's source root.  (Appends JavadocModel.SUGGESTED_DIR_NAME to
   * the sourceroot.)
   * @param doc Document with the source root to use as the default.
   * @return Suggested destination directory, or null if none could be
   * determined.
   */
  public File suggestJavadocDestination(OpenDefinitionsDocument doc) {
    return _javadocModel.suggestJavadocDestination(doc);
  }
  
  /**
   * Javadocs all open documents, after ensuring that all are saved.
   */
  public void javadocAll(DirectorySelector select, FileSaveSelector saver,
                         List<String> classpath,
                         JavadocListener listener)
    throws IOException
  {
    _javadocModel.javadocAll(select, saver, classpath, listener);
  }
  
  /**
   * Generates Javadoc for the given document only, after ensuring it is saved.
   */
  public void javadocDocument(final OpenDefinitionsDocument doc,
                              final FileSaveSelector saver,
                              final List<String> classpath,
                              final JavadocListener listener)
    throws IOException
  {
    _javadocModel.javadocDocument(doc, saver, classpath, listener);
  }
  
  /**
   * Sorts the given array of CompilerErrors and divides it into groups
   * based on the file, giving each group to the appropriate
   * OpenDefinitionsDocument, opening files if necessary.
   */
  private void _distributeErrors(CompilerError[] errors)
    throws IOException {
    resetCompilerErrors();
    
    // Store number of errors
    _numErrors = errors.length;

    _compilerErrorModel = new CompilerErrorModel(errors, this);
  }

  /**
   * Creates a DefinitionsDocumentHandler for a new DefinitionsDocument,
   * using the DefinitionsEditorKit.
   * @return OpenDefinitionsDocument object for a new document
   */
  private OpenDefinitionsDocument _createOpenDefinitionsDocument() {
    DefinitionsDocument doc = (DefinitionsDocument)
      _editorKit.createNewDocument();
    return new DefinitionsDocumentHandler(doc);
  }


  /**
   * Returns the OpenDefinitionsDocument corresponding to the given
   * File, or null if that file is not open.
   * @param file File object to search for
   * @return Corresponding OpenDefinitionsDocument, or null
   */
  private OpenDefinitionsDocument _getOpenDocument(File file) {
    OpenDefinitionsDocument doc = null;

    for (int i=0; ((i < _definitionsDocs.size()) && (doc == null)); i++) {
      OpenDefinitionsDocument thisDoc =
        (OpenDefinitionsDocument) _definitionsDocs.get(i);
      try {
        File thisFile = null;
        try {
          thisFile = thisDoc.getFile();
        }
        catch (FileMovedException fme) {
          // Ok, file is invalid, but compare anyway
          thisFile = fme.getFile();
        }
        finally {
          // Always do the comparison
          if (thisFile != null) {
            try {
              // Compare canonical paths if possible
              if (thisFile.getCanonicalFile().equals(file.getCanonicalFile())) {
                doc = thisDoc;
              }
            }
            catch (IOException ioe) {
              // Can be thrown from getCanonicalFile.
              //  If so, compare the files themselves
              if (thisFile.equals(file)) {
                doc = thisDoc;
              }
            }
          }
        }
      }
      catch (IllegalStateException ise) {
        // No file in thisDoc
      }
    }

    return doc;
  }

  /**
   * Returns true if a document corresponding to the given
   * file is open, or false if that file is not open.
   * @param file File object to search for
   * @return boolean whether file is open
   */
  private boolean _docIsOpen(File file) {
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null)
      return false;
    else
      return true;
  }

  /**
   * Creates a document from a file.
   * @param file File to read document from
   * @return openened document
   */
  private OpenDefinitionsDocument _openFile(File file)
    throws IOException, AlreadyOpenException {

      DefinitionsDocument tempDoc = (DefinitionsDocument)
        _editorKit.createNewDocument();

    try {
      OpenDefinitionsDocument openDoc = _getOpenDocument(file);
      if (openDoc != null) {
        throw new AlreadyOpenException(openDoc);
      }
      
      FileReader reader = new FileReader(file);
      _editorKit.read(reader, tempDoc, 0);
      reader.close(); // win32 needs readers closed explicitly!
      
      tempDoc.setFile(file);
      tempDoc.resetModification();
      
      tempDoc.setCurrentLocation(0);
      
      final OpenDefinitionsDocument doc =
        new DefinitionsDocumentHandler(tempDoc);
      _definitionsDocs.addElement(doc);
      //doc.checkIfClassFileInSync();
      
      _notifier.notifyListeners(new EventNotifier.Notifier() {
          public void notifyListener(GlobalModelListener l) {
            l.fileOpened(doc);
          }
        });
      
      return doc;
    }
    catch (BadLocationException docFailed) {
      throw new UnexpectedException(docFailed);
    }
  }

  /**
   * Instantiates the integrated debugger if the "debugger.enabled"
   * config option is set to true.  Leaves it at null if not.
   */
  private void _createDebugger() {
    try {
      _debugger = new JPDADebugger(this);
      _interpreterControl.setDebugModel((JPDADebugger) _debugger);
    }
    catch( NoClassDefFoundError ncdfe ){
      // JPDA not available, so we won't use it.
      _debugger = NoDebuggerAvailable.ONLY;
    }
    catch( UnsupportedClassVersionError ucve ) {
      // Wrong version of JPDA, so we won't use it.
      _debugger = NoDebuggerAvailable.ONLY;
    }
    catch( Throwable t ) {
      // Something went wrong in initialization, don't use debugger
      _debugger = NoDebuggerAvailable.ONLY;
    }
  }


  /**
   * Adds the source roots for all open documents and the paths on the
   * "extra classpath" config option to the interpreter's classpath.
   */
  public void resetInteractionsClasspath() {
    // Ideally, we'd like to put the open docs before the config option,
    //  but this is inconsistent with how the classpath was defined
    //  as it was built up.  (The config option is inserted on startup,
    //  and docs are added as they are opened.  It shouldn't switch after
    //  a reset.)

    Vector<File> cp = DrJava.getConfig().getSetting(EXTRA_CLASSPATH);
    if(cp!=null) {
      Enumeration<File> en = cp.elements();
      while(en.hasMoreElements()) {
        _interactionsModel.addToClassPath(en.nextElement().getAbsolutePath());
      }
    }
    
    File[] sourceRoots = getSourceRootSet();
    for (int i = 0; i < sourceRoots.length; i++) {
      _interactionsModel.addToClassPath(sourceRoots[i].getAbsolutePath());
    }
  }

  /**
   * Called when the JVM used for unit tests has registered.
   */
  public void junitJVMReady() {
    
    if (_docBeingTested != null) {
      JUnitError[] errors = new JUnitError[1];
      String fileName = null;
      try {
        fileName = _docBeingTested.getDocument().getFile().getAbsolutePath();
      }
      catch (IllegalStateException ise) {
      }
      catch (FileMovedException fme) {
        fileName = fme.getFile().getAbsolutePath();
      }
      errors[0] = new JUnitError(new File(fileName), -1, -1, "Previous test was interrupted", true,
                                 "", "No associated stack trace");
      // TODO: Should this happen here?  The modified field is on the outer class.
      _junitErrorModel = new JUnitErrorModel(errors, this, true);
      _docBeingTested = null;
      _notifier.notifyListeners(new EventNotifier.Notifier() {
        public void notifyListener(GlobalModelListener l) {
          l.junitEnded();
        }
      });
    }
  }

  /**
   * Sets the listener for any type of single-source input event.
   * The listener can only be changed with the changeInputListener method.
   * @param listener a listener that reacts to input requests
   * @throws IllegalStateException if the input listener is locked
   */
  public void setInputListener(InputListener listener) {
    if (_inputListener == NoInputListener.ONLY) {
      _inputListener = listener;
    }
    else {
      throw new IllegalStateException("Cannot change the input listener until it is released.");
    }
  }

  /**
   * Changes the input listener. Takes in the old listener to ensure that the owner
   * of the original listener is aware that it is being changed. It is therefore
   * important NOT to include a public accessor to the input listener on the model.
   * @param oldListener the listener that was installed
   * @param newListener the listener to be installed
   */
  public void changeInputListener(InputListener oldListener, InputListener newListener) {
    // syncrhonize to prevent concurrent modifications to the listener
    synchronized(NoInputListener.ONLY) {
      if (_inputListener == oldListener) {
        _inputListener = newListener;
      }
      else {
        throw new IllegalArgumentException("The given old listener is not installed!");
      }
    }
  }

  /**
   * Gets input from the console through the currently installed input listener.
   * @return the console input
   */
  public String getConsoleInput() {
    _consoleDoc.insertPrompt();
    return _inputListener.getConsoleInput();
  }

  /**
   * Singleton InputListener which should never be asked for input.
   */
  private static class NoInputListener implements InputListener {
    public static final NoInputListener ONLY = new NoInputListener();
    private NoInputListener() {
    }

    public String getConsoleInput() {
      throw new IllegalStateException("No input listener installed!");
    }
  }

  private class ExtraClasspathOptionListener implements OptionListener<Vector<File>> {
    
    public void optionChanged (OptionEvent<Vector<File>> oce) {
      Vector<File> cp = oce.value;
      if(cp!=null) {
        Enumeration<File> en = cp.elements();
        while(en.hasMoreElements()) {
          _interactionsModel.addToClassPath(en.nextElement().getAbsolutePath());
        }
      }
    }
  }

  private static class BackUpFileOptionListener implements OptionListener<Boolean> {

    public void optionChanged (OptionEvent<Boolean> oe){
      Boolean value = oe.value;
      FileOps.DefaultFileSaver.setBackupsEnabled(value.booleanValue());
    }
  }
  
}
