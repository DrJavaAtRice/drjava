/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by: Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.awt.Container;

import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.docnavigation.IDocumentNavigator;
import edu.rice.cs.util.docnavigation.INavigatorItem;
import edu.rice.cs.util.docnavigation.INavigationListener;
import edu.rice.cs.util.docnavigation.NodeData;
import edu.rice.cs.util.docnavigation.NodeDataVisitor;
import edu.rice.cs.util.swing.Utilities;

/** A GlobalModel that enforces invariants associated with having one active document at a time.
 *  Invariants:
 *  <OL>
 *  <LI>{@link #getOpenDefinitionsDocuments} will always return an array of at least size 1.
 *  </LI>
 *  <LI>(follows from previous) If there is ever no document in the model, a new one will be created.
 *  </LI>
 *  <LI>There is always exactly one active document, which can be get/set
 *      via {@link #getActiveDocument} and {@link #setActiveDocument}.
 *  </LI>
 *  </OL>
 * Other functions added by this class:
 *  <OL>
 *  <LI>When calling {@link #openFile}, if there is currently only one open document, and it is untitled and 
 *      unchanged, it will be closed after the new document is opened. This means that, in one atomic transaction, 
 *      the model goes from having one totally empty document open to having one document (the requested one) open.
 *  </LI>
 *  </OL>
 *  @version $Id$
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
      public Boolean itemCase(INavigatorItem docu) {
        _setActiveDoc(docu);  // sets _activeDocument, the shadow copy of the active document
//        Utilities.showDebug("Setting the active doc done");
        File dir = _activeDocument.getParentDirectory();
        
        if (dir != null) {  
        /* If the file is in External or Auxiliary Files then then we do not want to change our project directory
         * to something outside the project. */
          _activeDirectory = dir;
          _notifier.currentDirectoryChanged(_activeDirectory);
        }
        return Boolean.valueOf(true); 
      }
      public Boolean fileCase(File f) {
        if (! f.isAbsolute()) {
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

//  /** Add a listener to this global model. Synchronized using EventNotifier readers/writers protocol.
//   *  TODO: is this ever actually used?
//   *  @param listener a listener that reacts on events generated by the GlobalModel.
//   */
//  public void addListener(GlobalModelListener listener) {
//    if (! (listener instanceof GlobalModelListener))
//      throw new IllegalArgumentException("Must use GlobalModelListener");
//
//    addListenerHelper(listener);
//  }

  //----------------------- New SingleDisplay Methods -----------------------//

  /** Returns the currently active document. */
  public OpenDefinitionsDocument getActiveDocument() {return  _activeDocument; }
  
  
  /** Sets the currently active document by updating the selection model.
   *  @param doc Document to set as active
   */
  public void setActiveDocument(final OpenDefinitionsDocument doc) {
    /* The following code fixes a potential race because this method modifies the documentNavigator which is a swing
     * component. Hence it must run in the event thread.  Note that setting the active document triggers the execution
     * of listeners some of which also need to run in the event thread. */

//    if (_activeDocument == doc) return; // this optimization appears to cause some subtle bugs 
//    Utilities.showDebug("DEBUG: Called setActiveDocument()");

    Runnable command = new Runnable() {  
      public void run() {_documentNavigator.setActiveDoc(doc);} 
    };
    try {Utilities.invokeAndWait(command); }  // might be relaxed to invokeLater
    catch(Exception e) { throw new UnexpectedException(e); } 
//    try { _documentNavigator.setActiveDoc(doc); } 
//    catch(DocumentClosedException dce) { 
//      /* do nothing; findbugs signals a bug unless this catch clause spans more than two lines */
//  }
  }
  
  public Container getDocCollectionWidget() { return _documentNavigator.asContainer(); }
  
  /** Sets the active document to be the next one in the collection. */
  public void setActiveNextDocument() {
    INavigatorItem key = _activeDocument;
    OpenDefinitionsDocument nextKey = (OpenDefinitionsDocument) _documentNavigator.getNext(key);
    if (key != nextKey) setActiveDocument(nextKey);
    else setActiveDocument((OpenDefinitionsDocument)_documentNavigator.getFirst());
    /* selects the active document in the navigator, which signals a listener to call _setActiveDoc(...) */
  }

  /** Sets the active document to be the previous one in the collection. */
  public void setActivePreviousDocument() {
    INavigatorItem key = _activeDocument;
    OpenDefinitionsDocument prevKey = (OpenDefinitionsDocument) _documentNavigator.getPrevious(key);
    if (key != prevKey) setActiveDocument(prevKey);
    else setActiveDocument((OpenDefinitionsDocument)_documentNavigator.getLast());
      /* selects the active document in the navigator, which signals a listener to call _setActiveDoc(...) */
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

  /** Creates a new document, adds it to the list of open documents, and sets it to be active.
   *  @return The new open document
   */
  public OpenDefinitionsDocument newFile() {
    OpenDefinitionsDocument doc = newFile(_activeDirectory);
    setActiveDocument(doc);
    return doc;
  }

  //---------------------- Specified by ILoadDocuments ----------------------//

  /** Open a file and add it to the pool of definitions documents. The provided file selector chooses a file, 
   *  and on a successful open, the fileOpened() event is fired. This method also checks if there was previously 
   *  a single unchanged, untitled document open, and if so, closes it after a successful opening.
   *  @param com a command pattern command that selects what file to open
   *  @return The open document, or null if unsuccessful
   *  @exception IOException
   *  @exception OperationCanceledException if the open was canceled
   *  @exception AlreadyOpenException if the file is already open
   */
  public OpenDefinitionsDocument openFile(FileOpenSelector com) throws 
    IOException, OperationCanceledException, AlreadyOpenException {
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneEmptyDocument();
    OpenDefinitionsDocument oldDoc = _activeDocument; 
    OpenDefinitionsDocument openedDoc = openFileHelper(com);
    if (closeUntitled) closeFileHelper(oldDoc);
//    Utilities.showDebug("DrJava has opened" + openedDoc + " and is setting it active");
    setActiveDocument(openedDoc);
//    Utilities.showDebug("active doc set; openFile returning"); 
    return openedDoc;
  }

 /** Open multiple files and add them to the pool of definitions documents.  The provided file selector chooses 
  *  a collection of files, and on successfully opening each file, the fileOpened() event is fired.  This method
  *  also checks if there was previously a single unchanged, untitled document open, and if so, closes it after 
  *  a successful opening.
  *  @param com a command pattern command that selects what file
  *            to open
  *  @return The open document, or null if unsuccessful
  *  @exception IOException
  *  @exception OperationCanceledException if the open was canceled
  *  @exception AlreadyOpenException if the file is already open
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
   *  When prompting (i.e., untitled document), set that document as active.
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

  /** Closes an open definitions document, prompting to save if the document has been changed.  Returns whether
   *  the file was successfully closed.  Also ensures the invariant that there is always at least
   *  one open document holds by creating a new file if necessary.
   *  @return true if the document was closed
   */
   public boolean closeFile(OpenDefinitionsDocument doc) {
     List<OpenDefinitionsDocument> list = new LinkedList<OpenDefinitionsDocument>();
     list.add(doc);
     return closeFiles(list);
   }
   
   /** Attempts to close all open documents. Also ensures the invariant that there is always at least
    *  one open document holds by creating a new file if necessary.
    //Bug when the first document, in list view, is selected:
    //When "close all" documents is selected, each document in turn is set active
    //Fix: close the currently active document last
    * @return true if all documents were closed
    */
   public boolean closeAllFiles() {
     List<OpenDefinitionsDocument> docs = getOpenDefinitionsDocuments();
     return closeFiles(docs);
   }
  
  /**
   * This function closes a group of files assuming that the files are contiguous in the enumeration
   * provided by the document navigator. This assumption is used in selecting which remaining document
   * (if any) to activate.
   * <p>
   * The corner cases in which the file that is being closed had been externally
   * deleted have been addressed in a few places, namely DefaultGlobalModel.canAbandonFile()
   * and MainFrame.ModelListener.canAbandonFile().  If the DefinitionsDocument for the 
   * OpenDefinitionsDocument being closed is not in the cache (see model.cache.DocumentCache)
   * then it is closed without prompting the user to save it.  If it is in the cache, then
   * we can successfully notify the user that the file is selected for closing and ask whether to
   * saveAs, close, or cancel.
   * @param docList the list od OpenDefinitionsDocuments to close
   * @param together if true then no files will be closed if not all can be abandoned
   * @return whether all files were closed
   * 
   * Question: what is the together flag for?  How does it affect observable behavior?
   */
  public boolean closeFiles(List<OpenDefinitionsDocument> docList) {
    if (docList.size() == 0) return true;
    
    /* Force the user to save or discard all modified files in docList */
    for (OpenDefinitionsDocument doc : docList) { if (!doc.canAbandonFile()) return false; }
    
    // If all files are being closed, create a new file before starTing in order to have 
    // an active file that is not in the list of closing files.
    OpenDefinitionsDocument newDoc = null;
    if (docList.size() == getOpenDefinitionsDocumentsSize()) newDoc = newFile();
    
    // Set the active document to the document just after the last document or the document just before the 
    // first document in docList.
    _ensureNotActive(docList);
        
    // Close the files in docList. 
    for (OpenDefinitionsDocument doc : docList) { closeFileWithoutPrompt(doc); }  
    return true;
  }
  
  /**
   * Returns whether there is currently only one open document
   * which is untitled and unchanged.
   */
  private boolean _hasOneEmptyDocument() {
    return getOpenDefinitionsDocumentsSize() == 1 && _activeDocument.isUntitled() &&
            ! _activeDocument.isModifiedSinceSave();
  }

  /** Creates a new document if there are currently no documents open. */
  private void _ensureNotEmpty() {
    if ((!_isClosingAllDocs) && (getOpenDefinitionsDocumentsSize() == 0)) newFile(null);
  }
  
  /** Makes sure that none of the documents in the list are active. */
  private void _ensureNotActive(List<OpenDefinitionsDocument> docs) {
    if (docs.contains(getActiveDocument())) {
      // Find the one that should be the new active document
      IDocumentNavigator nav = getDocumentNavigator();
      
      INavigatorItem item = docs.get(docs.size()-1);
      OpenDefinitionsDocument nextActive = (OpenDefinitionsDocument) nav.getNext(item);
      if (!nextActive.equals(item)) {
        setActiveDocument(nextActive); 
        return;
      }
      
      item = docs.get(0);
      nextActive = (OpenDefinitionsDocument) nav.getPrevious(item);
      if (!nextActive.equals(item)) { 
        setActiveDocument(nextActive);
        return;
      }
      
      throw new RuntimeException("No document to set active before closing");
    }
  }
  
  /** Sets the first document in the navigator as active. */
  public void setActiveFirstDocument() {
    List<OpenDefinitionsDocument> docs = getOpenDefinitionsDocuments();
    /* The follwoing will select the active document in the navigator, which will signal a listener to call _setActiveDoc(...)
     */
    setActiveDocument(docs.get(0));
  }
  
  private synchronized void _setActiveDoc(INavigatorItem idoc) {
      _activeDocument = (OpenDefinitionsDocument) idoc;  // FIX THIS!
      refreshActiveDocument();
  }
  
  /** Invokes the activeDocumentChanged method in the global listener on the argument _activeDocument.  This process sets up
   *  _activeDocument as the document in the definitions pane.  It is also necessary after an "All Documents" search that wraps
   *  around. */
  public void refreshActiveDocument() {
    try {
      _activeDocument.checkIfClassFileInSync();
      // notify single display model listeners   // notify single display model listeners
      _notifier.activeDocumentChanged(_activeDocument);
    } catch(DocumentClosedException dce) { /* do nothing */ }
  }
}
