package edu.rice.cs.drjava.ui;

import javax.swing.text.*;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.*;
import java.util.*;

import edu.rice.cs.util.swing.FindReplaceMachine;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.Version;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;

/**
 * A GlobalModel that enforces invariants associated with having
 * one active document at a time.
 *
 * Invariants:
 * <OL>
 * <LI>{@link #getDefinitionsDocuments} will always return an array of
 *     at least size 1.
 * </LI>
 * <LI>(follows from previous) If there is ever no document in the model,
 *     a new one will be created.
 * </LI>
 * <LI>There is always exactly one active document, which can be get/set
 *     via {@link #getActiveDocument} and {@link #setActiveDocument}.
 * </LI>
 * <OL>
 *
 * Other functions added by this class:
 * <OL>
 * <LI>When calling {@link #openFile}, if there is currently only one open
 *     document, and it is untitled and unchanged, it will be closed after the
 *     new document is opened. This means that, in one atomic transaction, the
 *     model goes from having one totally empty document open to having one
 *     document (the requested one) open.
 * </LI>
 * </OL>
 *
 * @version $Id$
 */
public class SingleDisplayModel extends DefaultGlobalModel {
  /**
   * The active document pointer, which will never be null once
   * the constructor is done.
   */
  private OpenDefinitionsDocument _activeDocument;

  /**
   * Keeps track of the currently selected document in the list model.
   */
  private ListSelectionModel _selectionModel;

  /**
   * Denotes whether the model is currently trying to close all
   * documents, and thus that a new one should not be created.
   */
  private boolean _isClosingAllDocs;

  /**
   * Creates a SingleDisplayModel.
   *
   * <OL>
   * <LI>If the given model has no open documents, a new document is created.
   * </LI>
   * <LI>The first document in the list is set as the active document.
   * </LI>
   * </OL>
   *
   */
  public SingleDisplayModel() {
    super();
    _isClosingAllDocs = false;
    _ensureNotEmpty();

    _selectionModel = new DefaultListSelectionModel();
    _selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _selectionModel.addListSelectionListener(new SelectionModelListener());
    setActiveDocument(0);
  }

  // new methods introduced here

  /**
   * Returns the currently active document.
   */
  public OpenDefinitionsDocument getActiveDocument() {
    return _activeDocument;
  }

  /**
   * Sets the currently active document by updating the selection model.
   * @param doc Document to set as active
   */
  public void setActiveDocument(OpenDefinitionsDocument doc) {
    setActiveDocument(_getDocumentIndex(doc));
  }

  /**
   * Sets the currently active document by updating the selection model.
   * @param index Index of active document in the list of documents.
   */
  public void setActiveDocument(int index) {
    if ((index < 0) || (index >= getDefinitionsDocuments().getSize())) {
      throw new IllegalArgumentException(
        "No such document in model to be set to active.");
    }
    _selectionModel.setSelectionInterval(index, index);
  }

  /**
   * Returns the selection model for the list of documents.
   */
  public ListSelectionModel getDocumentSelectionModel() {
    return _selectionModel;
  }

  /**
   * Sets the active document to be the next one in the list.
   */
  public void setNextActiveDocument() {
    int index = _getDocumentIndex(_activeDocument);

    if (index < getDefinitionsDocuments().getSize() - 1) {
      index++;
      setActiveDocument(index);
    }
  }

  /**
   * Sets the active document to be the previous one in the list.
   */
  public void setPreviousActiveDocument() {
    int index = _getDocumentIndex(_activeDocument);

    if (index > 0) {
      index--;
      setActiveDocument(index);
    }
  }

  /**
   * Returns the text to be displayed in the About box.
   */
  public String getAboutText() {
    String message = "DrJava, brought to you by the Java PLT "
                   + "research group at Rice University.\n"
                   + "http://www.cs.rice.edu/~javaplt/drjava\n\n"
                   + "Version: "
                   + Version.BUILD_TIME;
    return message;
  }


  /**
   * Return the name of the file, or "(untitled)" if no file exists.
   * Does not include the ".java" if it is present.
   */
  public String getDisplayFilename(OpenDefinitionsDocument doc) {

    String filename = "(untitled)";
    try {
      File file = doc.getFile();
      filename = file.getName();
    }
    catch (IllegalStateException ise) {
      // No file, filename stays "Untitled"
    }
    // Remove ".java"
    int extIndex = filename.lastIndexOf(".java");
    if (extIndex > 0) {
      filename = filename.substring(0, extIndex);
    }

    // Mark if modified
    if (doc.isModifiedSinceSave()) {
      filename = filename + " *";
    }
    return filename;
  }

  /**
   * Return the absolute path of the file, or "(untitled)" if no file exists.
   */
  public String getDisplayFullPath(OpenDefinitionsDocument doc) {

    String path = "(untitled)";
    try {
      File file = doc.getFile();
      path = file.getAbsolutePath();
    }
    catch (IllegalStateException ise) {
      // No file, filename stays "Untitled"
    }
    // Mark if modified
    if (doc.isModifiedSinceSave()) {
      path = path + " *";
    }
    return path;
  }

  /**
   * Return the absolute path of the file with the given index,
   * or "(untitled)" if no file exists.
   */
  public String getDisplayFullPath(int index) {
    OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
      getDefinitionsDocuments().getElementAt(index);
    if (doc == null) {
      throw new RuntimeException(
        "Document not found with index " + index);
    }
    return getDisplayFullPath(doc);
  }



  /**
   * Add a listener to this global model.
   * @param listener a listener that reacts on events generated by the GlobalModel
   */
  public void addListener(GlobalModelListener listener) {
    if (! (listener instanceof SingleDisplayModelListener)) {
      throw new IllegalArgumentException("Must use SingleDisplayModelListener");
    }

    super.addListener(listener);
  }

  /**
   * Open a file and read it into the definitions.
   * The provided file selector chooses a file, and on a successful
   * open, the fileOpened() event is fired.
   * This also checks if there was previously a single, unchanged,
   * untitled document open, and if so, closes it after a successful
   * opening.
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
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    OpenDefinitionsDocument oldDoc = _activeDocument;

    OpenDefinitionsDocument openedDoc = super.openFile(com);
    if (closeUntitled) {
      super.closeFile(oldDoc);
    }

    setActiveDocument(openedDoc);
    return openedDoc;
  }

  /**
   * Closes an open definitions document, prompting to save if
   * the document has been changed.  Returns whether the file
   * was successfully closed.
   * Also ensures the invariant that there is always at least
   * one open document holds by creating a new file if necessary.
   * @return true if the document was closed
   */
  public boolean closeFile(OpenDefinitionsDocument doc) {
    int index = _getDocumentIndex(doc);
    if (super.closeFile(doc)) {
      if (!_isClosingAllDocs) {
        _ensureNotEmpty();

        // Select next document
        int size = getDefinitionsDocuments().getSize();
        if (index < 0) {
          index = 0;
        }
        if (index >= size) {
          index = size - 1;
        }
        setActiveDocument(index);
      }
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Attempts to close all open documents.
   * Also ensures the invariant that there is always at least
   * one open document holds by creating a new file if necessary.
   * @return true if all documents were closed
   */
  public boolean closeAllFiles() {
    _isClosingAllDocs = true;
    boolean success = super.closeAllFiles();
    _isClosingAllDocs = false;

    _ensureNotEmpty();
    setActiveDocument(0);
    return success;
  }


  /**
   * Returns the index of the first occurrence of the specified document
   * in the list of open documents, or -1 if it is not found.
   */
  private int _getDocumentIndex(OpenDefinitionsDocument doc) {
    ListModel docs = getDefinitionsDocuments();
    int index = -1;
    for (int i=0; (i < docs.getSize()) && (index < 0); i++) {
      if (docs.getElementAt(i).equals(doc)) {
        index = i;
      }
    }
    return index;
  }


  /**
   * Returns whether there is currently only one open document
   * which is untitled and unchanged.
   */
  private boolean _hasOneEmptyDocument() {
    return ((getDefinitionsDocuments().getSize() == 1) &&
            (_activeDocument.isUntitled()) &&
            (!_activeDocument.isModifiedSinceSave()));
  }

  /**
   * Creates a new document if there are currently no documents open.
   */
  private void _ensureNotEmpty() {
    if ((!_isClosingAllDocs) &&
        (getDefinitionsDocuments().getSize() == 0)) {
      newFile();
    }
  }


  /**
   * Listens to the selection model for the open documents.  On a change,
   * this updates the activeDocument and notifies all listeners to the model.
   */
  private class SelectionModelListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (!e.getValueIsAdjusting()) {
        int index = _selectionModel.getMinSelectionIndex();
        ListModel docs = getDefinitionsDocuments();
        if ((index < 0) || (index > docs.getSize())) {
          throw new RuntimeException("Document index out of bounds: " + index);
        }
        _activeDocument = (OpenDefinitionsDocument) docs.getElementAt(index);

        // notify listeners
        notifyListeners(new EventNotifier() {
          public void notifyListener(GlobalModelListener l) {
            SingleDisplayModelListener sl = (SingleDisplayModelListener) l;
            sl.activeDocumentChanged(_activeDocument);
          }
        });
      }
    }
  }

}
