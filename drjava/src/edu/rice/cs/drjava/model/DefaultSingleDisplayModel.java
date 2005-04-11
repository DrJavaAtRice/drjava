/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import javax.swing.text.*;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.*;
import java.util.*;
import java.awt.Container;


import edu.rice.cs.util.swing.FindReplaceMachine;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.Version;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.compiler.*;
import edu.rice.cs.util.docnavigation.*;

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
 * </OL>
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
public class DefaultSingleDisplayModel extends DefaultGlobalModel implements SingleDisplayModel {

  /** The active document pointer, which will never be null once the constructor is done.
   *  Maintained by the _gainVisitor with a navigation listener.
   */
  private OpenDefinitionsDocument _activeDocument;
  
  /** A pointer to the active directory, which is not necessarily the parent of the active document
   *  The user may click on a folder component in the navigation pane and that will set this field without
   *  setting the active document.  It is used by the newFile method to place new files into the active directory.
   */
  private File _activeDirectory;

  /** Creates a SingleDisplayModel.
   *
   *  <ol>
   *    <li>A new document is created to satisfy the invariant.
   *    <li>The first document in the list is set as the active document.
   *  </ol>
   */
  public DefaultSingleDisplayModel() {
    super();
    _init();
  }

  /** Initiates this SingleDisplayModel.  Should only be called from the constructor. */
  private void _init() {
    
    final NodeDataVisitor<Boolean> _gainVisitor = new NodeDataVisitor<Boolean>() {
      public Boolean itemCase(INavigatorItem docu){
        _setActiveDoc(docu);  // sets _activeDocument
        File dir = _activeDocument.getParentDirectory();
        
        if (dir != null) {  //If the file is in External or Auxiliary Files then then we do not want to change our project directory to something outside the project
          _activeDirectory = dir;
          _notifier.currentDirectoryChanged(_activeDirectory);
        }
        return Boolean.valueOf(true); 
      }
      public Boolean fileCase(File f) {
        if (!f.isAbsolute()) {
          File root = _state.getProjectFile().getParentFile().getAbsoluteFile();
          f = new File(root, f.getPath());
        }
        _activeDirectory = f;
        _notifier.currentDirectoryChanged(f);
        return Boolean.valueOf(true);
      }
      public Boolean stringCase(String s) { return Boolean.valueOf(false); }
    };
    
    _documentNavigator.addNavigationListener(new INavigationListener() {
      public void gainedSelection(NodeData dat) { dat.execute(_gainVisitor); }
      public void lostSelection(NodeData dat) {
        // not important, only one document selected at a time
      }
    });
    
    _isClosingAllDocs = false;
    _ensureNotEmpty();
    setActiveFirstDocument();
  }

  /**
   * Add a listener to this global model.
   * TODO: is this ever actually used?
   * @param listener a listener that reacts on events generated by the GlobalModel
   * synchronized using EventNotifier readers/writers protocol
   */
  public void addListener(GlobalModelListener listener) {
    if (! (listener instanceof SingleDisplayModelListener))
      throw new IllegalArgumentException("Must use SingleDisplayModelListener");

    addListenerHelper(listener);
  }

  //----------------------- New SingleDisplay Methods -----------------------//

  /**
   * Returns the currently active document.
   */
  public OpenDefinitionsDocument getActiveDocument() { return _activeDocument; }
  
  /**
   * Sets the currently active document by updating the selection model.
   * @param doc Document to set as active
   */
  public void setActiveDocument(OpenDefinitionsDocument doc) {
    try { _documentNavigator.setActiveDoc(doc); } 
    catch(DocumentClosedException dce) { 
      /* do nothing; findbugs signals a bug unless this catch clause spans more than two lines */
    }
  }
  
  public Container getDocCollectionWidget() { return _documentNavigator.asContainer(); }
  
  /** Sets the active document to be the next one in the collection. */
  public void setActiveNextDocument() {
    INavigatorItem key = _activeDocument;
    INavigatorItem nextKey =_documentNavigator.getNext(key);
      if (key != nextKey) _documentNavigator.setActiveDoc(nextKey);
        /* this will select the active document in the navigator, which
         * will signal a listener to call _setActiveDoc(...) */
  }

  /**
   * Sets the active document to be the previous one in the collection
   */
  public void setActivePreviousDocument() {
    INavigatorItem key = _activeDocument;
    INavigatorItem prevKey =_documentNavigator.getPrevious(key);
    if (key != prevKey) {
      /* this will select the active document in the navigator, which
       * will signal a listener to call _setActiveDoc(...)
       */
      _documentNavigator.setActiveDoc(prevKey);
      //   _setActiveDoc(prevKey);
    }
  }
//
//  /**
//   * Returns whether we are in the process of closing all documents.
//   * (Don't want to prompt the user to revert files that have become
//   * modified on disk if we're just closing everything.)
//   * TODO: Move to DGM?  Make private?
//   */
//  public boolean isClosingAllFiles() {
//    return _isClosingAllDocs;
//  }

  //----------------------- End SingleDisplay Methods -----------------------//

  /**
   * Creates a new document, adds it to the list of open documents,
   * and sets it to be active.
   * @return The new open document
   */
  public OpenDefinitionsDocument newFile() {
    OpenDefinitionsDocument doc = newFile(_activeDirectory);
    
    setActiveDocument(doc);
    return doc;
  }

  //---------------------- Specified by ILoadDocuments ----------------------//

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
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    OpenDefinitionsDocument oldDoc = _activeDocument;

    OpenDefinitionsDocument openedDoc = openFileHelper(com);
    if (closeUntitled) closeFileHelper(oldDoc);

    setActiveDocument(openedDoc);
    return openedDoc;
  }

 /**
   * Open multiple files and read it into the definitions.
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
  public OpenDefinitionsDocument openFiles(FileOpenSelector com)
    throws IOException, OperationCanceledException, AlreadyOpenException {
    
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    OpenDefinitionsDocument oldDoc = _activeDocument;

    OpenDefinitionsDocument openedDoc = openFilesHelper(com);
    if (closeUntitled) closeFileHelper(oldDoc);
    setActiveDocument(openedDoc);
    return openedDoc;
  }

  //----------------------- End ILoadDocuments Methods -----------------------//

  /** Saves all open files, prompting for names if necessary.
   *  When prompting (ie, untitled document), set that document as active.
   *  @param com a FileSaveSelector
   *  @exception IOException
   */
   public void saveAllFiles(FileSaveSelector com) throws IOException {
     OpenDefinitionsDocument curdoc = getActiveDocument();
     saveAllFilesHelper(com);
     setActiveDocument(curdoc); // Return focus to previously active doc
   }

  /** If the document is untitled, brings it to the top so that the
   *  user will know which is being saved.
   */
   public void aboutToSaveFromSaveAll(OpenDefinitionsDocument doc) {
     if (doc.isUntitled()) setActiveDocument(doc);
   }

  /** Closes an open definitions document, prompting to save if
   *  the document has been changed.  Returns whether the file
   *  was successfully closed.
   *  Also ensures the invariant that there is always at least
   *  one open document holds by creating a new file if necessary.
   *  @return true if the document was closed
   */
   public boolean closeFile(OpenDefinitionsDocument doc) {
     List<OpenDefinitionsDocument> list = new LinkedList<OpenDefinitionsDocument>();
     list.add(doc);
     return closeFiles(list, false);
   }
   
   /**
    * Attempts to close all open documents.
    * Also ensures the invariant that there is always at least
    * one open document holds by creating a new file if necessary.
    //Bug when the first document, in list view, is selected:
    //When "close all" documents is selected, each document in turn is set active
    //Fix: close the currently active document last
    * @return true if all documents were closed
    */
   public boolean closeAllFiles() {
     List<OpenDefinitionsDocument> docs = getDefinitionsDocuments();
     return closeFiles(docs, false);
   }
  
  /**
   * This function closes a group of files assuming that there is some sort of 
   * continuity with the list of documents that are being closed.  This assumption
   * is mostly made in order to decide which document to activate.
   * <p>
   * The corner cases in which the file that is being closed had been externally
   * deleted have been addressed in a few places, namely DefaultGlobalModel.canAbandonFile()
   * and MainFrame.ModelListener.canAbandonFile().  If the DefinitionsDocument for the 
   * OpenDefinitionsDocument being closed is not in the cache (see model.cache.DocumentCache)
   * then it is closed without prompting the user to save it.  If it is in the cache, then
   * we can successfully notify the user that the file had been deleted and ask whether to
   * saveAs, close, or cancel.
   * @param docList the list od OpenDefinitionsDocuments to close
   * @param together if true then no files will be closed if not all can be abandoned
   * @return whether all files were closed
   */
  public boolean closeFiles(List<OpenDefinitionsDocument> docList, boolean together) {
    if (docList.size() == 0) return true;

    if (together) { // if together then do all prompting at once
      for (OpenDefinitionsDocument doc : docList) { if (!doc.canAbandonFile()) return false; }
    }
    
    // create new file before you start closing in order to have 
    // an active file that's not in the list of closing files.
    // If the current active document is closed before the MainFrame
    // can switch to a new file, drjava throws some unexpected exceptions
    // relating to the document not being found.
    OpenDefinitionsDocument newDoc = null;
    if (docList.size() == getDefinitionsDocumentsSize()) newDoc = newFile();
    _ensureNotActive(docList);
        
    // Close all files. If together, then don't let it prompt a 2nd time;
    // but, if not together, then call closeFile which may prompt the user.
    for (OpenDefinitionsDocument doc : docList) {
      if (together) closeFileWithoutPrompt(doc);
      else if (! closeFileHelper(doc)) {
        setActiveDocument(doc);
        if (newDoc != null) closeFileHelper(newDoc); // undo previous newFile() 
        return false;
      }
    }  
    return true;
  }
  
  /**
   * Returns whether there is currently only one open document
   * which is untitled and unchanged.
   */
  private boolean _hasOneEmptyDocument() {
    return ((getDefinitionsDocumentsSize() == 1) && (_activeDocument.isUntitled()) &&
            (!_activeDocument.isModifiedSinceSave()));
  }

  /**
   * Creates a new document if there are currently no documents open.
   */
  private void _ensureNotEmpty() {
    if ((!_isClosingAllDocs) && (getDefinitionsDocumentsSize() == 0)) newFile(null);
  }
  
  /**
   * Makes sure that none of the documents in the list are active.
   */
  private void _ensureNotActive(List<OpenDefinitionsDocument> docs) {
    if (docs.contains(getActiveDocument())) {
      // Find the one that should be the new active document
      IDocumentNavigator nav = getDocumentNavigator();
      
      INavigatorItem item = docs.get(docs.size()-1);
      INavigatorItem nextActive = nav.getNext(item);
      if (!nextActive.equals(item)) {
        nav.setActiveDoc(nextActive); 
        return;
      }
      
      item = docs.get(0);
      nextActive = nav.getPrevious(item);
      if (!nextActive.equals(item)) { 
        nav.setActiveDoc(nextActive);
        return;
      }
      
      throw new RuntimeException("No document to set active before closing");
    }
  }
  
  /**
   * some duplicated work, but avoids duplicated code, which is our nemesis
   */
  public void setActiveFirstDocument() {
    List<OpenDefinitionsDocument> docs = getDefinitionsDocuments();
    /* this will select the active document in the navigator, which
     * will signal a listener to call _setActiveDoc(...)
     */
    setActiveDocument(docs.get(0));
    //      _documentNavigator.setActiveDoc(getIDocGivenODD(docs.get(0)));
    //      _setActiveDoc(getIDocGivenODD(docs.get(0)));
  }
  
  private void _setActiveDoc(INavigatorItem idoc) {
    //Hashtable<INavigatorItem, OpenDefinitionsDocument> docs = getDefinitionsDocumentsTable();
    
    _activeDocument = (OpenDefinitionsDocument) idoc;  // FIX THIS!
    try {
      _activeDocument.checkIfClassFileInSync();
     
      // notify single display model listeners   // notify single display model listeners
      _notifier.notifyListeners(new GlobalEventNotifier.Notifier() {
        public void notifyListener(GlobalModelListener l) {
          // If it is a SingleDisplayModelListener, let it know that the
          //  active doc changed
          if (l instanceof SingleDisplayModelListener) {
            SingleDisplayModelListener sl = (SingleDisplayModelListener) l;
            sl.activeDocumentChanged(_activeDocument);
          }
        }
      });
    } catch(DocumentClosedException dce) { /* do nothing */ }
  }
}
