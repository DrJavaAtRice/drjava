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

import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.awt.font.TextLayout;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.*;

import edu.rice.cs.util.swing.FindReplaceMachine;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.*;
import edu.rice.cs.drjava.model.print.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.repl.newjvm.*;
import edu.rice.cs.drjava.model.compiler.*;

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
public class DefaultGlobalModel implements GlobalModel {
  private final DefinitionsEditorKit _editorKit = new DefinitionsEditorKit();
  private final DefaultListModel _definitionsDocs = new DefaultListModel();
  private final InteractionsDocument _interactionsDoc
    = new InteractionsDocument();
  private final StyledDocument _consoleDoc = new DefaultStyledDocument();
  private final LinkedList _listeners = new LinkedList();
  private PageFormat _pageFormat = new PageFormat();

  // blank final, set differently in the two constructors
  private final MainJVM _interpreterControl;

  private CompilerError[] _compilerErrorsWithoutFiles;
  private int _numErrors;

  public static final String EXIT_CALLED_MESSAGE
    = "The interaction was aborted by a call to System.exit.";

  public static final AttributeSet SYSTEM_OUT_STYLE
    = SimpleAttributeSet.EMPTY;

  public static final AttributeSet SYSTEM_ERR_STYLE = _getErrStyle();
  private static AttributeSet _getErrStyle() {
    SimpleAttributeSet s = new SimpleAttributeSet(SYSTEM_OUT_STYLE);
    s.addAttribute(StyleConstants.Foreground, Color.red.darker());
    return s;
  }

  public static final AttributeSet SYSTEM_OUT_INTERACTIONS_STYLE
    = _getOutInsideInteractionsStyle();

  private static AttributeSet _getOutInsideInteractionsStyle() {
    SimpleAttributeSet s = new SimpleAttributeSet(SYSTEM_OUT_STYLE);
    s.addAttribute(StyleConstants.Foreground, Color.green.darker().darker());
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

  public InteractionsDocument getInteractionsDocument() {
    return _interactionsDoc;
  }

  public StyledDocument getConsoleDocument() {
    return _consoleDoc;
  }

  public PageFormat getPageFormat() {
    return _pageFormat;
  }

  public void setPageFormat(PageFormat format) {
    _pageFormat = format;
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
   * @return The open document, or null if unsuccessful
   * @exception IOException
   * @exception OperationCanceledException if the open was canceled
   * @exception AlreadyOpenException if the file is already open
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException
  {
    DefinitionsDocument tempDoc = (DefinitionsDocument)
      _editorKit.createDefaultDocument();
    try {
      final File file = com.getFile();

      OpenDefinitionsDocument openDoc = _getOpenDocument(file);
      if (openDoc != null) {
        throw new AlreadyOpenException(openDoc);
      }

      FileReader reader = new FileReader(file);
      _editorKit.read(reader, tempDoc, 0);
      reader.close(); // win32 needs readers closed explicitly!

      tempDoc.setFile(file);
      tempDoc.resetModification();

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
      // Don't kill the interpreter. It'll die in a minute on its own,
      // and if we kill it using killInterpreter, we'll just start
      // another one!
      //_interpreterControl.killInterpreter();
      DrJava.getSecurityManager().exitVM(0);
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
   * First it makes sure it's in the right package given the
   * package specified by the definitions.  If it can't,
   * the package for the interactions becomes the defualt
   * top level. In either case, this method calls a helper
   * which fires the interactionsReset() event.
   */
  public void resetInteractions() {
    _interpreterControl.reset();
    _restoreInteractionsState();
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

  private void _docAppend(Document doc, String s, AttributeSet set) {
    try {
      doc.insertString(doc.getLength(), s, set);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  /** Called when the repl prints to System.out. */
  public void replSystemOutPrint(String s) {
    _docAppend(_consoleDoc, s, SYSTEM_OUT_STYLE);
    _interactionsDoc.insertBeforeLastPrompt(s, SYSTEM_OUT_INTERACTIONS_STYLE);
  }

  /** Called when the repl prints to System.err. */
  public void replSystemErrPrint(String s) {
    _docAppend(_consoleDoc, s, SYSTEM_ERR_STYLE);
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
                                           SYSTEM_ERR_STYLE);
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
   * Gets an array of all sourceRoots for the open definitions
   * documents, without duplicates. Note that if any of the open
   * documents has an invalid package statement, it won't be added
   * to the source root set.
   */
  public File[] getSourceRootSet() {
    LinkedList roots = new LinkedList();

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



  // ---------- DefinitionsDocumentHandler inner class ----------

  /**
   * Inner class to handle operations on each of the open
   * DefinitionsDocuments by the GlobalModel.
   */
  private class DefinitionsDocumentHandler implements OpenDefinitionsDocument {
    private final DefinitionsDocument _doc;
    private CompilerErrorModel _errorModel;
    private DrJavaBook _book;

    /**
     * Constructor.  Initializes this handler's document.
     * @param doc DefinitionsDocument to manage
     */
    DefinitionsDocumentHandler(DefinitionsDocument doc) {
      _doc = doc;
      _errorModel = new CompilerErrorModel();
    }

    /**
     * Gets the definitions document being handled.
     * @return document being handled
     */
    public DefinitionsDocument getDocument() {
      return _doc;
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
        else if (file.exists())
          if (!com.verifyOverwrite())
            throw new OperationCanceledException();  
        
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
      }
      catch (OperationCanceledException oce) {
        // OK, do nothing as the user wishes.
      }
      catch (BadLocationException docFailed) {
        throw new UnexpectedException(docFailed);
      }
    }

    /**
     * Called to demand that one or more listeners saves the
     * definitions document before proceeding.  It is up to the caller
     * of this method to check if the document has been saved.
     * Fires saveBeforeProceeding(SaveReason) if isModifiedSinceSave() is true.
     * @param reason the reason behind the demand to save the file
     */
    public void saveBeforeProceeding(final GlobalModelListener.SaveReason reason)
    {
      if (isModifiedSinceSave()) {
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
            l.saveBeforeProceeding(reason);
          }
        });
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
      saveBeforeProceeding(GlobalModelListener.COMPILE_REASON);
      CompilerError[] errors = new CompilerError[0];

      if (isModifiedSinceSave()) {
        // if the file hasn't been saved after we told our
        // listeners to do so, don't proceed with the rest
        // of the compile.
      }
      else {
        try {
          File file = _doc.getFile();

          try {
            notifyListeners(new EventNotifier() {
              public void notifyListener(GlobalModelListener l) {
                l.compileStarted();
              }
            });

            File[] files = new File[] { file };

            String packageName = _doc.getPackageName();
            File sourceRoot = _getSourceRoot(packageName);

            CompilerInterface compiler
              = CompilerRegistry.ONLY.getActiveCompiler();

            errors = compiler.compile(sourceRoot, files);
          }
          catch (InvalidPackageException e) {
            CompilerError err = new CompilerError(file,
                                                  -1,
                                                  -1,
                                                  e.getMessage(),
                                                  false);
            errors = new CompilerError[] { err };
          }
          finally {
            _distributeErrors(errors);

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
     * Determines if the definitions document has changed since the
     * last save.
     * @return true if the document has been modified
     */
    public boolean isModifiedSinceSave() {
      return _doc.isModifiedSinceSave();
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
      try {
        return new FindReplaceMachine(_doc, _doc.getCurrentLocation());
      }
      catch (BadLocationException e) {
        throw new UnexpectedException(e);
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
   *Assumes a trimmed String. Returns a string of the main call that the
   *interpretor can use.
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

    String[] cp = Configuration.ONLY.getExtraClasspath();
    for (int i = 0; i < cp.length; i++) {
      _interpreterControl.addClassPath(cp[i]);
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
