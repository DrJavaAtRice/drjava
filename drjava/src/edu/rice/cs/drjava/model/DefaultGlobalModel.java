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

import gj.util.Vector;
import gj.util.Enumeration;
import gj.util.Hashtable;
import edu.rice.cs.util.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
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

import junit.framework.Test;
import junit.framework.TestResult;
import junit.textui.TestRunner;
import junit.runner.TestSuiteLoader;
import junit.runner.ReloadingTestSuiteLoader;


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
public class DefaultGlobalModel implements GlobalModel, OptionConstants {
  private final DefinitionsEditorKit _editorKit = new DefinitionsEditorKit();
  private final DefaultListModel _definitionsDocs = new DefaultListModel();
  private final InteractionsDocument _interactionsDoc
    = new InteractionsDocument();
  private final StyledDocument _consoleDoc = new DefaultStyledDocument();
  private final StyledDocument _junitDoc = new DefaultStyledDocument();
  private final LinkedList _listeners = new LinkedList();
  private PageFormat _pageFormat = new PageFormat();
  private final JUnitTestRunner _junitTestRunner = new JUnitTestRunner(this);

  // blank final, set differently in the two constructors
  // package private to allow access from test cases
  final MainJVM _interpreterControl;

  private CompilerError[] _compilerErrorsWithoutFiles;
  private int _numErrors;

  // Debug manager (null if not available)
  private DebugManager _debugManager = null;
  private int _debugPort = -1;

  public static final Indenter INDENTER;

  static {
    int ind = DrJava.CONFIG.getSetting(OptionConstants.INDENT_LEVEL).intValue();
    INDENTER = new Indenter(ind);
    if (CodeStatus.DEVELOPMENT) {
      DrJava.CONFIG.addOptionListener( OptionConstants.INDENT_LEVEL, 
                                      new OptionListener<Integer>() {
          public void optionChanged(OptionEvent<Integer> oce) {
            INDENTER.buildTree(DrJava.CONFIG.getSetting(OptionConstants.INDENT_LEVEL).intValue());
          }  
        });
    }
  }

  public static final String EXIT_CALLED_MESSAGE
    = "The interaction was aborted by a call to System.exit.";

  public static final AttributeSet SYSTEM_OUT_STYLE
    = SimpleAttributeSet.EMPTY;

  public static final AttributeSet SYSTEM_ERR_STYLE = _getErrStyle();
  private static AttributeSet _getErrStyle() {
    SimpleAttributeSet s = new SimpleAttributeSet(SYSTEM_OUT_STYLE);
    s.addAttribute(StyleConstants.Foreground, Color.red);
    return s;
  }

  public static final AttributeSet SYSTEM_OUT_INTERACTIONS_STYLE
    = _getOutInsideInteractionsStyle();
  
  public static final AttributeSet SYSTEM_ERR_INTERACTIONS_STYLE
    = _getErrStyle();

  public static final AttributeSet DEBUG_STYLE
    = _getDebugStyle();

  private static AttributeSet _getOutInsideInteractionsStyle() {
    SimpleAttributeSet s = new SimpleAttributeSet(SYSTEM_OUT_STYLE);
    s.addAttribute(StyleConstants.Foreground, Color.green.darker().darker());
    return s;
  }
  
  private static AttributeSet _getDebugStyle() {
    SimpleAttributeSet s = new SimpleAttributeSet(SYSTEM_OUT_STYLE);
    s.addAttribute(StyleConstants.Foreground, Color.blue.darker());
    s.addAttribute(StyleConstants.Bold, new Boolean(true));
    return s;
  }
  
  public static final AttributeSet INTERACTIONS_ERR_STYLE
    = _getInteractionsErrStyle();
  
  private static AttributeSet _getInteractionsErrStyle() {
    SimpleAttributeSet s = new SimpleAttributeSet(SYSTEM_OUT_STYLE);
    s.addAttribute(StyleConstants.Foreground, Color.red.darker());
    s.addAttribute(StyleConstants.Bold, new Boolean(true));
    return s;
  }
  
  /**
   * Constructor.  Initializes all the documents and the interpreter.
   */
  public DefaultGlobalModel()
  {
    _compilerErrorsWithoutFiles = new CompilerError[0];
    _numErrors = 0;

    try {
      _interpreterControl = new MainJVM(this);
      _resetInteractionsClasspath();
    }
    catch (java.rmi.RemoteException re) {
      throw new UnexpectedException(re);
    }

    _createDebugger();
    
    if (CodeStatus.DEVELOPMENT) {
      DrJava.CONFIG.addOptionListener(EXTRA_CLASSPATH, new ExtraClasspathOptionListener());
    }
  }

  /**
   * Constructor.  Initializes all the documents, but take the interpreter
   * from the given previous model. This is used only for test cases,
   * since there is substantial overhead to initializing the interpreter.
   *
   * Reset the interpreter for good measure since it's an old one.
   */
  public DefaultGlobalModel(DefaultGlobalModel other)
  {
    _compilerErrorsWithoutFiles = new CompilerError[0];
    _numErrors = 0;

    _interpreterControl = other._interpreterControl;
    _interpreterControl.setModel(this);
    _interpreterControl.reset();
    _createDebugger();
    try {
      _debugPort = other.getDebugPort();
    }
    catch (IOException ioe) {
      // Other model should already have a port, or it should be -1.
      //  We shouldn't ever get an IOException here.
      throw new UnexpectedException(ioe);
    }
    
    if (CodeStatus.DEVELOPMENT) {    
      DrJava.CONFIG.addOptionListener(EXTRA_CLASSPATH, new ExtraClasspathOptionListener());
    }
  }

  /**
   * Add a listener to this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void addListener(GlobalModelListener listener) {
    synchronized(_listeners) {
      _listeners.addLast(listener);
    }
  }

  /**
   * Remove a listener from this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void removeListener(GlobalModelListener listener) {
    synchronized(_listeners) {
      _listeners.remove(listener);
    }
  }

  // getter methods for the private fields

  public DefinitionsEditorKit getEditorKit() {
    return _editorKit;
  }

  public ListModel getDefinitionsDocuments() {
    return _definitionsDocs;
  }

  public StyledDocument getInteractionsDocument() {
    return _interactionsDoc;
  }

  public StyledDocument getConsoleDocument() {
    return _consoleDoc;
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

  public JUnitTestRunner getTestRunner() {
    return _junitTestRunner;
  }

  /** Errors without associated files */
  public CompilerError[] getCompilerErrorsWithoutFiles() {
    return _compilerErrorsWithoutFiles;
  }
  /** Total number of current errors */
  public int getNumErrors() {
    return _numErrors;
  }

  /**
   * Creates a new document in the definitions pane and
   * adds it to the list of open documents.
   * @return The new open document
   */
  public OpenDefinitionsDocument newFile() {
    final OpenDefinitionsDocument doc = _createOpenDefinitionsDocument();
    doc.getDocument().setFile(null);
    _definitionsDocs.addElement(doc);
    notifyListeners(new EventNotifier() {
      public void notifyListener(GlobalModelListener l) {
      l.newFileCreated(doc);
    }
    });
    return doc;
  }

  /**
   * Open a file and read it into the definitions.
   * The provided file selector chooses a file, and on a successful
   * open, the fileOpened() event is fired.
   * @param com a command pattern command that selects what file
   *            to open
   *
   * @return The open document, or null if unsuccessful.
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return an absolute path, as this method makes
   * it absolute.
   *
   * @exception IOException if an underlying I/O operation fails
   * @exception OperationCanceledException if the open was canceled
   * @exception AlreadyOpenException if the file is already open
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {
    DefinitionsDocument tempDoc = (DefinitionsDocument)
      _editorKit.createDefaultDocument();

    final File file = (com.getFiles())[0].getAbsoluteFile();
    OpenDefinitionsDocument odd = _openFile(file);
    
    // Make sure this is on the classpath
    try {
        File classpath = odd.getSourceRoot();
        _interpreterControl.addClassPath(classpath.getAbsolutePath());
    }
    catch (InvalidPackageException e) {
        // Invalid package-- don't add it to classpath
    }
    
    return odd;
  }

  /**
   * Opens multiple files and reads them into the definitions.
   * The provided file selector chooses multiple files, and for each
   * successful open, the fileOpened() event is fired.
   * @param com a command pattern command that selects which files
   *            to open
   *
   * @return The last opened document, or null if unsuccessful.
   * Note that .getFile called on the returned OpenDefinitionsDocument
   * is guaranteed to return an absolute path, as this method makes
   * it absolute.
   *
   * @exception IOException if an underlying I/O operation fails
   * @exception OperationCanceledException if the open was canceled
   * @exception AlreadyOpenException if the file is already open
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
        _interpreterControl.addClassPath(classpath.getAbsolutePath());
      }
      catch (InvalidPackageException e) {
        // Invalid package-- don't add it to classpath
      }
      
    }
    
    if (storedAOE != null) throw storedAOE;
    
    if (retDoc != null) {
      return retDoc;
    } else {
      //if no OperationCanceledException, then getFiles should
      //have atleast one file. 
      throw new IOException("No Files returned from FileChooser");
    }
    
    
  }
 
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
   * Saves all open files, prompting for names if necessary.
   * When prompting (ie, untitled document), set that document as active.
   * @param com[] selectors to pick file name; size = size of _definitionsDocs
   * @exception IOException
   */
  public void saveAllFiles(FileSaveSelector com[]) throws IOException {
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.getElementAt(i);
      aboutToSaveFromSaveAll(doc);
      doc.saveFile(com[i]);
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
      // Only fire event if doc exists and was removed from list
      if (_definitionsDocs.removeElement(doc)) {
        notifyListeners(new EventNotifier() {
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
   * Exits the program.
   * Only quits if all documents are successfully closed.
   */
  public void quit() {
    if (closeAllFiles()) {
      // Kill the interpreter
      _interpreterControl.killInterpreter();

      // Clean up debugger if necessary
      //if ((_debugManager != null) && (_debugManager.isReady())) {
      //  _debugManager.shutdown();
      //}
      
      if (DrJava.getSecurityManager() != null) {
        DrJava.getSecurityManager().exitVM(0);
      }
      else {
        //might be being debugged by another DrJava
        System.exit(0);
      }
        
    }
  }

  /**
   * Returns the OpenDefinitionsDocument for the specified
   * File, opening a new copy if one is not already open.
   * @param file File contained by the document to be returned
   * @return OpenDefinitionsDocument containing file
   * @exception IOException if there are problems opening the file
   */
  public OpenDefinitionsDocument getDocumentForFile(File file)
    throws IOException
  {
    // Check if this file is already open
    OpenDefinitionsDocument doc = _getOpenDocument(file);
    if (doc == null) {
      // If not, open and return it
      final File f = file;
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
    // Keep a note that we're resetting so that the exit message is not displayed
    _interpreterControl.setIsResetting(true);
    if ((_debugManager != null) && (_debugManager.isReady())){
      _debugManager.shutdown();
    }
    _interpreterControl.restartInterpreterJVM();
    _restoreInteractionsState();
    _interpreterControl.setIsResetting(false);
    
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
    try {
      _consoleDoc.remove(0, _consoleDoc.getLength());
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }

    notifyListeners(new EventNotifier() {
      public void notifyListener(GlobalModelListener l) {
      l.consoleReset();
    }
    });
  }


  /**
   * Forwarding method to remove logical dependency of InteractionsPane on
   * the InteractionsDocument.  Gets the previous interaction in the
   * InteractionsDocument's history and replaces whatever is on the current
   * interactions input line with this interaction.
   */
  public void recallPreviousInteractionInHistory(Runnable failed) {
    if (_interactionsDoc.hasHistoryPrevious()) {
      _interactionsDoc.moveHistoryPrevious();
    }
    else {
      failed.run();
    }
  }

  /**
   * Forwarding method to remove logical dependency of InteractionsPane on
   * the InteractionsDocument.  Gets the next interaction in the
   * InteractionsDocument's history and replaces whatever is on the current
   * interactions input line with this interaction.
   */
  public void recallNextInteractionInHistory(Runnable failed) {
    if (_interactionsDoc.hasHistoryNext()) {
      _interactionsDoc.moveHistoryNext();
    }
    else {
      failed.run();
    }
  }

  /**
   * Aborts any threads running in the Interactions JVM by completely
   * restarting that JVM.
   */
  public void abortCurrentInteraction() {
    _interpreterControl.restartInterpreterJVM();
  }

  /**
   * Returns the first location in the document where editing is allowed.
   */
  public int getInteractionsFrozenPos() {
    return _interactionsDoc.getFrozenPos();
  }

  /**
   * Clears the current interaction text and then moves
   * to the end of the command history.
   */
  public void clearCurrentInteraction() {
    _interactionsDoc.clearCurrentInteraction();
  }

  /**
   * Interprets the current given text at the prompt in the interactions
   * pane.
   */
  public void interpretCurrentInteraction() {
    notifyListeners(new EventNotifier() {
      public void notifyListener(GlobalModelListener l) {
      l.interactionStarted();
    }
    });

    String text = _interactionsDoc.getCurrentInteraction();
    _interactionsDoc.setInProgress(true);
    _interactionsDoc.addToHistory(text);

    // there is no return at the end of the last line
    // better to put it on now and not later.
    _docAppend(_interactionsDoc, "\n", null);

    String toEval = text.trim();
    if (toEval.startsWith("java ")) {
      toEval = _testClassCall(toEval);
    }

    _interpreterControl.interpret(toEval);
  }

  /**
   * Interprets the file selected in the FileOpenSelector. Assumes all strings 
   * have no trailing whitespace. Interprets the array all at once so if there are 
   * any errors, none of the statements after the first erroneous one are processed.
   */
  public void loadHistory(FileOpenSelector selector) 
    throws IOException {//Vector<String> interactions) {
    
    File[] files = null;
    try {
      files = selector.getFiles();
    }
    catch (OperationCanceledException oce) {
      return;
      // don't need to do anything
    }
    Vector<String> strings = new Vector<String>();
    if (files == null) 
      throw new IOException("No Files returned from FileSelector");
    
    for (int i=0; i < files.length; i++) {
      if (files[i] == null) {
        throw new IOException("File name returned from FileSelector is null");
      }
      File c = files[i];
      if (c != null) {
        try {
          FileInputStream fis = new FileInputStream(c);
          InputStreamReader isr = new InputStreamReader(fis);
          BufferedReader br = new BufferedReader(isr);
          String currLine;
          while ((currLine = br.readLine()) != null) {
            strings.addElement(currLine);
          }
        }
        catch (IOException ioe) {
          throw new IOException("File name returned from FileSelector is null");
          //_showIOError(ioe);
        }
        
      }
      notifyListeners(new EventNotifier() {
        public void notifyListener(GlobalModelListener l) {
          l.interactionStarted();
        }
      });
      String text = "";
      String currString;
      for (int j = 0; j < strings.size(); j++) {
        currString = strings.elementAt(j);
        if (currString.length() > 0) {
          if (currString.charAt(currString.length() - 1) == ';')
            //currString += "\n";
            text += currString + "\n";
          else
            //currString += ";\n";
            text += currString + ";\n";
        }
      }
      _docAppend(_interactionsDoc, text, null);
      //  _docAppend(_interactionsDoc, currString, null);
      _interactionsDoc.setInProgress(true);
      _interactionsDoc.addToHistory(text);
      
      // there is no return at the end of the last line
      // better to put it on now and not later.
      //_docAppend(_interactionsDoc, "\n", null);
      
      String toEval = text.trim();
      //String toEval = currString.trim();
      if (toEval.startsWith("java ")) {
        toEval = _testClassCall(toEval);
      }
      
      //System.out.println("Interpreting "+toEval);
      _interpreterControl.interpret(toEval);
      
      // Might need this if trying to implement line-by-line interpretation
      /*
       notifyListeners(new EventNotifier() {
       public void notifyListener(GlobalModelListener l) {
       l.interactionCaretPositionChanged(getInteractionsFrozenPos());
       }
       });
       */
    }
  }
  
  /**
   * Clears the interactions history
   */
  public void clearHistory() {
    _interactionsDoc.clearHistory();
  }
  
  /**
   * Saves the current history to a file
   */
  public void saveHistory(FileSaveSelector selector) throws IOException{
    _interactionsDoc.saveHistory(selector);
  }
  
  /**
   * Returns the entire history as a Vector<String>
   */
  public String getHistoryAsString() {
    return _interactionsDoc.getHistoryAsString();
  }
  
  private void _docAppend(Document doc, String s, AttributeSet set) {
    try {
      doc.insertString(doc.getLength(), s, set);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }
  
  /** Prints System.out to the DrJava console. */
  public void systemOutPrint(String s) {
    _docAppend(_consoleDoc, s, SYSTEM_OUT_STYLE);
  }

  /** Prints System.err to the DrJava console. */
  public void systemErrPrint(String s) {
    _docAppend(_consoleDoc, s, SYSTEM_ERR_STYLE);
  }

  /** Called when the repl prints to System.out. */
  public void replSystemOutPrint(String s) {
    systemOutPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, SYSTEM_OUT_INTERACTIONS_STYLE);
  }

  /** Called when the repl prints to System.err. */
  public void replSystemErrPrint(String s) {
    systemErrPrint(s);
    _interactionsDoc.insertBeforeLastPrompt(s, SYSTEM_ERR_INTERACTIONS_STYLE);
  }

  /** Called when the debugger wants to print a message. */
  public void printDebugMessage(String s) {
    _interactionsDoc.insertBeforeLastPrompt(s + "\n", DEBUG_STYLE);
  }


  private void _interactionIsOver() {
    _interactionsDoc.setInProgress(false);
    _interactionsDoc.prompt();

    notifyListeners(new EventNotifier() {
      public void notifyListener(GlobalModelListener l) {
      l.interactionEnded();
    }
    });
  }

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning no value.
   */
  public void replReturnedVoid() {
    _interactionIsOver();
  }

  /**
   * Signifies that the most recent interpretation completed successfully,
   * returning a value.
   *
   * @param result The .toString-ed version of the value that was returned
   *               by the interpretation. We must return the String form
   *               because returning the Object directly would require the
   *               data type to be serializable.
   */
  public void replReturnedResult(String result) {
    _docAppend(_interactionsDoc, result + "\n", null);
    _interactionIsOver();
  }

  /**
   * Signifies that the most recent interpretation was ended
   * due to an exception being thrown.
   *
   * @param exceptionClass The name of the class of the thrown exception
   * @param message The exception's message
   * @param stackTrace The stack trace of the exception
   */
  public void replThrewException(String exceptionClass,
                                 String message,
                                 String stackTrace)
  {
    _interactionsDoc.appendExceptionResult(exceptionClass,
                                           message,
                                           stackTrace,
                                           INTERACTIONS_ERR_STYLE);
    /*
     if (null == message || "null".equals(message)) {
     message = "";
     }

     String txt = exceptionClass + ": " + message;
     if (! stackTrace.trim().equals("")) {
     txt += "\n" + stackTrace;
     }

     _docAppend(_interactionsDoc, txt + "\n", SYSTEM_ERR_STYLE);
     */

    _interactionIsOver();
  }

  /**
   * Signifies that the most recent interpretation contained a call to
   * System.exit.
   *
   * @param status The exit status that will be returned.
   */
  public void replCalledSystemExit(final int status) {
    notifyListeners(new EventNotifier() {
      public void notifyListener(GlobalModelListener l) {
      l.interactionsExited(status);
    }
    });

    // all old interactions are irrelevant, so reset them
    _restoreInteractionsState();
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
  public String getClasspath() {
    String separator= System.getProperty("path.separator");
    String classpath= "";
    File[] sourceFiles = getSourceRootSet();

    // Adds extra.classpath to the classpath.
    Vector<File> extraClasspath = DrJava.CONFIG.getSetting(EXTRA_CLASSPATH);
    if(extraClasspath != null) {
        Enumeration<File> enum = extraClasspath.elements();
        while(enum.hasMoreElements()) {
            classpath += enum.nextElement().getAbsolutePath() + separator;
        }
    }

    for(int i=0; i < sourceFiles.length; i++) {
      classpath += sourceFiles[i].getAbsolutePath() + separator;
    }
    return classpath;
  }

  /**
   * Gets an array of all sourceRoots for the open definitions
   * documents, without duplicates. Note that if any of the open
   * documents has an invalid package statement, it won't be added
   * to the source root set.
   * This set includes the user's current directory, as well.
   */
  public File[] getSourceRootSet() {
    LinkedList roots = new LinkedList();
    File workDir = DrJava.CONFIG.getSetting(WORKING_DIRECTORY);
        
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File( System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    roots.add(workDir);

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

    return (File[]) roots.toArray(new File[0]);
  }
  
  /**
   * Compiles all open documents, after ensuring that all are saved.
   */
  public void compileAll() throws IOException {
    // Only compile if all are saved
    saveAllBeforeProceeding(GlobalModelListener.COMPILE_REASON);
    
    if (areAnyModifiedSinceSave()) {
      // if any files haven't been saved after we told our
      // listeners to do so, don't proceed with the rest
      // of the compile.
    }
    else {
      try {
        // Get sourceroots and all files
        File[] sourceRoots = getSourceRootSet();
        File[] files = new File[_definitionsDocs.getSize()];
        for (int i = 0; i < _definitionsDocs.getSize(); i++) {
          OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
            _definitionsDocs.getElementAt(i);
          files[i] = doc.getFile();
        }
      
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
            l.compileStarted();
          }
        });
        
        // Compile everything
        _compileFiles(sourceRoots, files);
        
        // Fire a compileEnded event
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
            l.compileEnded();
          }
        });
        
        // Only clear console/interactions if there were no errors
        if (_numErrors == 0) {
          resetConsole();
          resetInteractions();
        }
        
      }
      catch (IllegalStateException ise) {
        // One of the docs didn't have a file.  Don't compile.
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
  private void _compileFiles(File[] sourceRoots, File[] files) throws IOException {
    
    CompilerError[] errors = new CompilerError[0];
    
    CompilerInterface compiler
      = CompilerRegistry.ONLY.getActiveCompiler();
      
    errors = compiler.compile(sourceRoots, files);
    _distributeErrors(errors);
  }

  /**
   * Gets the DebugManager, which interfaces with the integrated debugger.
   */
  public DebugManager getDebugManager() {
    return _debugManager;
  }
  
  /**
   * Returns an available port number to use for debugging the interactions JVM.
   * @throws IOException if unable to get a valid port number.
   */
  public int getDebugPort() throws IOException {
    if (_debugPort == -1) {
      ServerSocket socket = new ServerSocket(0);
      _debugPort = socket.getLocalPort();
      socket.close();
    }
    return _debugPort;
  }

  /**
   * Called to demand that one or more listeners saves all the
   * definitions documents before proceeding.  It is up to the caller
   * of this method to check if the documents have been saved.
   * Fires saveAllBeforeProceeding(SaveReason) if areAnyModifiedSinceSave() is true.
   * @param reason the reason behind the demand to save the file
   */
    public void saveAllBeforeProceeding(final GlobalModelListener.SaveReason reason)
    {
      if (areAnyModifiedSinceSave()) {
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
          l.saveAllBeforeProceeding(reason);
        }
        });
      }
    }

    /**
     * Checks if any open definitions documents have been modified 
     * since last being saved. 
     * @return whether any documents have been modified
     */
    public boolean areAnyModifiedSinceSave() {
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
    
    
    // ---------- DefinitionsDocumentHandler inner class ----------
    
    /**
   * Inner class to handle operations on each of the open
   * DefinitionsDocuments by the GlobalModel.
   */
  private class DefinitionsDocumentHandler implements OpenDefinitionsDocument {
    private final DefinitionsDocument _doc;
    private CompilerErrorModel _errorModel;
    private JUnitErrorModel _junitErrorModel;
    private DrJavaBook _book;
    private Vector<Breakpoint> _breakpoints;
    
    /**
     * Constructor.  Initializes this handler's document.
     * @param doc DefinitionsDocument to manage
     */
    DefinitionsDocumentHandler(DefinitionsDocument doc) {
      _doc = doc;
      _errorModel = new CompilerErrorModel();
      _junitErrorModel = new JUnitErrorModel();
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
     * Retrieves the class name for the associated document
     * reads the name of the first class declaration
     */
    public String getClassName() { 
      return _doc.getClassName();
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
    public File getFile() throws IllegalStateException {
      return _doc.getFile();
    }


    /**
     * Saves the document with a FileWriter.  If the file name is already
     * set, the method will use that name instead of whatever selector
     * is passed in.
     * @param com a selector that picks the file name
     * @exception IOException
     */
    public void saveFile(FileSaveSelector com) throws IOException {
      FileSaveSelector realCommand;
      final File file;

      try {
        if (_doc.isUntitled()) {
          realCommand = com;
        } else {
          file = _doc.getFile();
          realCommand = new FileSaveSelector() {
            public File getFile() throws OperationCanceledException {
              return file;
            }
            public void warnFileOpen() {}
            public boolean verifyOverwrite() {
              return true;
            }
          };
        }

        saveFileAs(realCommand);
      }
      catch (IllegalStateException ise) {
        // No file; this should be caught by isUntitled()
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
     * @exception IOException
     */
    public void saveFileAs(FileSaveSelector com) throws IOException {
      try {
        final OpenDefinitionsDocument openDoc = this;
        final File file = com.getFile();
        final OpenDefinitionsDocument otherDoc = _getOpenDocument(file);
        if ( otherDoc != null && openDoc != otherDoc ) {
          com.warnFileOpen();
          throw new OperationCanceledException();
        }
        else if (file.exists()) {
          if (com.verifyOverwrite()) {
            if (! file.getCanonicalFile().getName().equals(file.getName())) {
              //need filename case switching (on windows)
              file.renameTo(file);
            }
          } else {
            throw new OperationCanceledException();
          }
        }
        FileWriter writer = new FileWriter(file);
        _editorKit.write(writer, _doc, 0, _doc.getLength());
        writer.close();
        _doc.resetModification();
        _doc.setFile(file);
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
          l.fileSaved(openDoc);
        }
        });
        
        // Make sure this is on the classpath
        try {
          File classpath = getSourceRoot();
          _interpreterControl.addClassPath(classpath.getAbsolutePath());
        }
        catch (InvalidPackageException e) {
          // Invalid package-- don't add to classpath
        }
      }
      catch (OperationCanceledException oce) {
        // OK, do nothing as the user wishes.
      }
      catch (BadLocationException docFailed) {
        throw new UnexpectedException(docFailed);
      }
    }

    /**
     * This method tells the document to prepare all the DrJavaBook
     * and PagePrinter objects.
     */
    public void preparePrintJob() throws BadLocationException {
      String filename = "(untitled)";
      try {
        filename = _doc.getFile().getAbsolutePath();
      } catch (IllegalStateException e) {
      }

      _book = new DrJavaBook(_doc.getText(0, _doc.getLength()), filename, _pageFormat);
    }

    /**
     * Prints the given document by bringing up a
     * "Print" window.
     */
    public void print() throws PrinterException, BadLocationException {
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
     * fails.  If the compilation succeeds, then calls are
     * made to resetConsole() and resetInteractions(), which fire
     * events of their own, contingent on the conditions.  If the current
     * package as determined by getSourceRoot(String) and getPackageName()
     * is invalid, compileStarted and compileEnded will fire, and
     * an error will be put in compileErrors.
     */
    public void startCompile() throws IOException {
      // Only compile if all are saved
      saveAllBeforeProceeding(GlobalModelListener.COMPILE_REASON);
    
      if (areAnyModifiedSinceSave()) {
        // if any files haven't been saved after we told our
        // listeners to do so, don't proceed with the rest
        // of the compile.
      }
      else {
        try {
          File file = _doc.getFile();
          File[] files = new File[] { file };
          
          try {
            notifyListeners(new EventNotifier() {
              public void notifyListener(GlobalModelListener l) {
                l.compileStarted();
              }
            });
          
            File[] sourceRoots = new File[] { getSourceRoot() };
        
            _compileFiles(sourceRoots, files);
          }
          catch (InvalidPackageException e) {
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
            notifyListeners(new EventNotifier() {
              public void notifyListener(GlobalModelListener l) {
                l.compileEnded();
              }
            });
            
            // Only clear console/interactions if there were no errors
            if (_numErrors == 0) {
              resetConsole();
              resetInteractions();
            }
          }
        }
        catch (IllegalStateException ise) {
          // No file exists, don't try to compile
        }
      }
    }

    /**
     * Runs JUnit on the current document.
     *
     * @return The results of running the tests specified in the
     * given definitions document.
     *
     */
    public TestResult startJUnit() throws ClassNotFoundException, IOException{

      //JUnit started, so throw out all JUnitErrorModels now, egardless of whether
      //  the tests succeed, etc.
      
      ListModel docs = getDefinitionsDocuments();
      // walk thru all open documents, resetting the JUnitErrorModel
      for (int i = 0; i < docs.getSize(); i++) {
        OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
          docs.getElementAt(i);
        doc.setJUnitErrorModel( new JUnitErrorModel() );
      }
      
      // Compile and save before proceeding.
      saveAllBeforeProceeding(GlobalModelListener.JUNIT_REASON);
      if (areAnyModifiedSinceSave()) {
        return null;
      }
      try {
        File testFile = getFile();

        startCompile();
        if(getNumErrors() != 0) {
          notifyListeners(new EventNotifier() {
            public void notifyListener(GlobalModelListener l) {
            l.compileErrorDuringJUnit();
          }
          });        
          return null;
        }

        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
          l.junitStarted();
        }
        });

        try {
          getJUnitDocument().remove(0, getJUnitDocument().getLength() - 1);
        }
        catch (BadLocationException e) {
          notifyListeners(new EventNotifier() {
            public void notifyListener(GlobalModelListener l) {
            l.junitEnded();
            l.nonTestCase();
          }
          });
          return null;
        }

        JUnitTestRunner testRunner = getTestRunner();

        String testFilename = testFile.getName();
        if (testFilename.toLowerCase().endsWith(".java")) {
          testFilename = testFilename.substring(0, testFilename.length() - 5);
        }
        else {
          notifyListeners(new EventNotifier() {
            public void notifyListener(GlobalModelListener l) {
            l.junitEnded();
            l.nonTestCase();
          }
          });
          return null;
        }
        String packageName;
        try {
          packageName = _doc.getPackageName();
        }
        catch (InvalidPackageException e) {
          notifyListeners(new EventNotifier() {
            public void notifyListener(GlobalModelListener l) {
            l.junitEnded();
            l.nonTestCase();
          }
          });
          return null;
        }
        if(!packageName.equals("")) {
          testFilename = packageName + "." + testFilename;
        }
        try {
          if (! testRunner.isTestCase(testFilename)) {
            notifyListeners(new EventNotifier() {
              public void notifyListener(GlobalModelListener l) {
              l.nonTestCase();
              l.junitEnded();
            }
            });
            return null;
          }
        }
        catch (ClassNotFoundException e) {
          notifyListeners(new EventNotifier() {
            public void notifyListener(GlobalModelListener l) {
            l.junitEnded();
          }
          });
          throw e;
        }

        Test suite = testRunner.getTest(testFilename);
        TestResult testResult = testRunner.doRun(suite, false, this);

        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
          l.junitEnded();
        }
        });
        return testResult;
      }
      catch (IllegalStateException e) {
        // No file exists, don't try to compile and test
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
          l.junitEnded();
          l.nonTestCase();
        }
        });
        return null;
      }
      catch (NoClassDefFoundError e) {
        // Method getTest in junit.framework.BaseTestRunner can throw a
        // NoClassDefFoundError (via reflection).
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
          l.junitEnded();
        }
        });
        throw e;
      }
      catch (ExitingNotAllowedException enae) {
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
          l.junitEnded();
        }
        });
        throw enae;
      }
      //System.err.println("JUnit tried to exit -- Resolving this bug requires some refactoring");
      //System.err.println(" because such an error causes a System.exit(Exception) in JUnit, and");
      //System.err.println(" we need a way of catching its output.");
      
    }

    /**
     * Returns the model responsible for maintaining all current errors
     * within this OpenDefinitionsDocument's file.
     */
    public CompilerErrorModel getCompilerErrorModel() {
      return _errorModel;
    }

    /**
     * Sets this OpenDefinitionsDocument's notion of all current errors
     * within the corresponding file.
     * @param model CompilerErrorModel containing all errors for this file
     */
    public void setCompilerErrorModel(CompilerErrorModel model) {
      if (model == null) {
        model = new CompilerErrorModel();
      }
      _errorModel = model;
    }


    /**
     * Returns the model responsible for maintaining all JUnit
     * errors within this OpenDefinitionsDocument's file.
     */
    public JUnitErrorModel getJUnitErrorModel() {
      return _junitErrorModel;
    }

    /**
     * Sets the OpenDefinitionDocument's notion of all JUnit errors
     * within this current document.
     * @param model JUnitErrorModel containing all JUnit errors for this file.
     */
    public void setJUnitErrorModel(JUnitErrorModel model) {
      _junitErrorModel = model;
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
     * Determines if the defintions document has been changed
     * by an outside program. If the document has changed,
     * then asks the listeners if the GlobalModel should 
     * revert the document to the most recent version saved.
     * @return true if document has been reverted
     */
    public boolean revertIfModifiedOnDisk() throws IOException{
      final OpenDefinitionsDocument doc = this;
      if (isModifiedOnDisk()) {
        
        boolean shouldRevert = pollListeners(new EventPoller() {
            public boolean poll(GlobalModelListener l) {
              return l.shouldRevertFile(doc);
            }
          });
        if (shouldRevert) { 
          doc.revertFile();
        }
        return shouldRevert;
      } else {
        return false;
      }
    }
    public void revertFile() throws IOException {
      final OpenDefinitionsDocument doc = this;
      
      try {
        File file = doc.getFile();
        DefinitionsDocument tempDoc = doc.getDocument();
        
        tempDoc.remove(0,tempDoc.getLength());
        
        FileReader reader = new FileReader(file);
        _editorKit.read(reader, tempDoc, 0);
        reader.close(); // win32 needs readers closed explicitly!
        
        tempDoc.resetModification();
        syncCurrentLocationWithDefinitions(0);
        
        notifyListeners(new EventNotifier() {
            public void notifyListener(GlobalModelListener l) {
              l.fileReverted(doc);
            }
          });
      } catch (IllegalStateException docFailed) {
        //cant revert file if doc has no file
        throw new UnexpectedException(docFailed);
      } catch (BadLocationException docFailed) {
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
        return pollListeners(new EventPoller() {
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
    public void indentLinesInDefinitions(int selStart, int selEnd) {
      _doc.indentLines(selStart, selEnd);
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
     * Add the supplied Breakpoint to the hashtable, keyed by its BreakpointRequest
     * @param breakpoint the Breakpoint to be inserted into the hashtable
     */
    public void addBreakpoint( Breakpoint breakpoint) {
      //_breakpoints.put( new Integer(breakpoint.getLineNumber()), breakpoint); 
      
      for (int i=0; i<_breakpoints.size();i++) {
        Breakpoint bp = _breakpoints.elementAt(i);
        int oldStart = bp.getStartOffset();
        int newStart = breakpoint.getStartOffset();
        
        if ( newStart < oldStart) {
          _breakpoints.insertElementAt(breakpoint, i);
          return;
        }
        if ( newStart == oldStart) {
          int oldEnd = bp.getEndOffset();
          int newEnd = breakpoint.getEndOffset();
          
          if ( newEnd < oldEnd) {
            _breakpoints.insertElementAt(breakpoint, i);
            return;
          }
        }
      }
      _breakpoints.addElement(breakpoint);
    }
    
    /**
     * Remove the given Breakpoint from the hashtable.
     * @param breakpoint the Breakpoint to be removed.
     */
    public void removeBreakpoint( Breakpoint breakpoint) {
      _breakpoints.removeElement( breakpoint);
    }
    
    /**
     * Returns a Vector<Breakpoint> that contains all of the Breakpoint objects that
     * this document contains
     */
    public Vector<Breakpoint> getBreakpoints() {
      return _breakpoints;
    }
    
    /**
     * Tells the document to remove all breakpoints
     */
    public void clearBreakpoints() {
      _breakpoints.removeAllElements();
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

      if (packageName.equals("")) {
        return sourceFile.getParentFile();
      }

      Stack packageStack = new Stack();
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
   * Sorts the given array of CompilerErrors and divides it into groups
   * based on the file, giving each group to the appropriate
   * OpenDefinitionsDocument, opening files if necessary.
   */
  private void _distributeErrors(CompilerError[] errors)
    throws IOException
  {
    // Reset CompilerErrorModels
    for (int i = 0; i < _definitionsDocs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        _definitionsDocs.getElementAt(i);
      doc.setCompilerErrorModel(new CompilerErrorModel());
    }
    
    // Store number of errors
    _numErrors = errors.length;
    
    // Sort the errors by file and position
    Arrays.sort(errors);
    
    // Filter out ones without files
    int numWithoutFiles = 0;
    for (int i = 0; i < errors.length; i++) {
      if (errors[i].file() == null) {
        numWithoutFiles++;
      }
      else {
        // Since sorted, finding one with a file means we're done
        break;
      }
    }
    
    // Copy errors without files into GlobalModel's array
    _compilerErrorsWithoutFiles = new CompilerError[numWithoutFiles];
    System.arraycopy(errors, 0, _compilerErrorsWithoutFiles, 0,
                     numWithoutFiles);
    
    // Create error models and give to their respective documents
    for (int i = numWithoutFiles; i < errors.length; i++) {
      File file = errors[i].file();
      OpenDefinitionsDocument doc = getDocumentForFile(file);
      
      // Find all other errors with this file
      int numErrors = 1;
      int j = i + 1;
      while ((j < errors.length) &&
             (file.equals(errors[j].file()))) {
        j++;
        numErrors++;
      }
      
      // Create a model with all errors with this file
      CompilerError[] fileErrors = new CompilerError[numErrors];
      System.arraycopy(errors, i, fileErrors, 0, numErrors);
      CompilerErrorModel model =
        new CompilerErrorModel(fileErrors, doc.getDocument(), file);
      doc.setCompilerErrorModel(model);
      
      // Continue with errors for the next file
      i = j - 1;
    }
  }

  /**
   * Creates a DefinitionsDocumentHandler for a new DefinitionsDocument,
   * using the DefinitionsEditorKit.
   * @return OpenDefinitionsDocument object for a new document
   */
  private OpenDefinitionsDocument _createOpenDefinitionsDocument() {
    DefinitionsDocument doc = (DefinitionsDocument)
      _editorKit.createDefaultDocument();
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
        if (thisDoc.getFile().equals(file)) {
          doc = thisDoc;
        }
      }
      catch (IllegalStateException ise) {
        // No file in thisDoc
        //throw new UnexpectedException(ise, "Exception on doc #" + i);
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
        _editorKit.createDefaultDocument();

    try {
      OpenDefinitionsDocument openDoc = _getOpenDocument(file);
      if (openDoc != null) {
        //System.err.println("This file is already open!");
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
      
      
      notifyListeners(new EventNotifier() {
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
    if (CodeStatus.DEVELOPMENT) {
      //boolean useDebug = DrJava.CONFIG.getSetting(DEBUGGER_ENABLED).booleanValue();
      //if (useDebug) {
      try {
        _debugManager = new DebugManager(this);
      }
      catch( NoClassDefFoundError ncdfe ){
        // JPDA not available, so we won't use it.
        _debugManager = null;
      }
    }    
  }



  /**
   * Assumes a trimmed String. Returns a string of the main call that the
   * interpretor can use.
   */
  private String _testClassCall(String s) {
    LinkedList ll = new LinkedList();
    if (s.endsWith(";"))
      s = _deleteSemiColon(s);
    StringTokenizer st = new StringTokenizer(s);
    st.nextToken();             //don't want to get back java
    String argument = st.nextToken();           // must have a second Token
    while (st.hasMoreTokens())
      ll.add(st.nextToken());
    argument = argument + ".main(new String[]{";
    ListIterator li = ll.listIterator(0);
    while (li.hasNext()) {
      argument = argument + "\"" + (String)(li.next()) + "\"";
      if (li.hasNext())
        argument = argument + ",";
    }
    argument = argument + "});";
    return  argument;
  }

  private void _resetInteractionsClasspath() {
    File[] sourceRoots = getSourceRootSet();
    for (int i = 0; i < sourceRoots.length; i++) {
      _interpreterControl.addClassPath(sourceRoots[i].getAbsolutePath());
    }

    Vector<File> cp = DrJava.CONFIG.getSetting(EXTRA_CLASSPATH);
    if(cp!=null) {
      Enumeration<File> enum = cp.elements();
      while(enum.hasMoreElements()) {
        _interpreterControl.addClassPath(enum.nextElement().getAbsolutePath());
      }
    }
  }

  private class ExtraClasspathOptionListener implements OptionListener<Vector<File>> {
    
    public void optionChanged (OptionEvent<Vector<File>> oce) {
      Vector<File> cp = oce.value;
      if(cp!=null) {
        Enumeration<File> enum = cp.elements();
        while(enum.hasMoreElements()) {
          _interpreterControl.addClassPath(enum.nextElement().getAbsolutePath());
        }
      } 
    }    
  }

  /**
   * Sets up a new interpreter to clear out the interpreter's environment.
   * If the setup works and the package directory exists,
   * interactionsReset() is fired.
   */
  private void _restoreInteractionsState() {
    _resetInteractionsClasspath();
    _interactionsDoc.reset();

    //_interpreterControl.setPackageScope("");

    notifyListeners(new EventNotifier() {
      public void notifyListener(GlobalModelListener l) {
      l.interactionsReset();
    }
    });
  }

  /**
   * Deletes the last character of a string.  Assumes semicolon at the
   * end, but does not check.  Helper for _testClassCall(String).
   * @param s
   * @return
   */
  private String _deleteSemiColon(String s) {
    return  s.substring(0, s.length() - 1);
  }


  /**
   * Allows the GlobalModel to ask its listeners a yes/no question and
   * receive a response.
   * @param EventPoller p the question being asked of the listeners
   * @return the listeners' responses ANDed together, true if they all
   * agree, false if some disagree
   */
  protected boolean pollListeners(EventPoller p) {
    ListIterator i = _listeners.listIterator();
    boolean poll = true;

    while(i.hasNext()) {
      GlobalModelListener cur = (GlobalModelListener) i.next();
      poll = poll && p.poll(cur);
    }
    return poll;
  }

  /**
   * Lets the listeners know some event has taken place.
   * @param EventNotifier n tells the listener what happened
   */
  protected void notifyListeners(EventNotifier n) {
    synchronized(_listeners) {
      ListIterator i = _listeners.listIterator();

      while(i.hasNext()) {
        GlobalModelListener cur = (GlobalModelListener) i.next();
        n.notifyListener(cur);
      }
    }
  }

  /**
   * Class model for notifying listeners of an event.
   */
  protected abstract class EventNotifier {
    public abstract void notifyListener(GlobalModelListener l);
  }

  /**
   * Class model for asking listeners a yes/no question.
   */
  protected abstract class EventPoller {
    public abstract boolean poll(GlobalModelListener l);
  }
}
