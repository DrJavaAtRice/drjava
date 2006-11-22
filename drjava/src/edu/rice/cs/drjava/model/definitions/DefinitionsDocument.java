/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.DocumentEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import java.io.File;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.*;


/** The document model for the definitions pane.
 *
 *  This implementation of <code>Document</code> contains a "reduced model". The reduced model is automatically kept
 *  in sync when this document is updated. Also, that synchronization is maintained even across undo/redo -- this is 
 *  done by making the undo/redo commands know how to restore the reduced model state.
 *
 *  The reduced model is not thread-safe, so it is essential that ONLY this DefinitionsDocument call methods on it.  
 *  Any information from the reduced model should be obtained through helper methods on DefinitionsDocument, and ALL 
 *  methods in DefinitionsDocument which reference the reduced model (via the _reduced field) MUST be synchronized.  
 *  This prevents any thread from seeing an inconsistent state in the middle of another thread's changes.
 *
 *  @see BraceReduction
 *  @see ReducedModelControl
 *  @see ReducedModelComment
 *  @see ReducedModelBrace
 */
public class DefinitionsDocument extends AbstractDJDocument implements Finalizable<DefinitionsDocument> {
  
  public static final Log _log = new Log("GlobalModel.txt", false);
  private static final int NO_COMMENT_OFFSET = 0;
  private static final int WING_COMMENT_OFFSET = 2;
  
  private volatile List<DocumentClosedListener> _closedListeners = new LinkedList<DocumentClosedListener>();
  
  public void addDocumentClosedListener(DocumentClosedListener l) { 
    synchronized(_closedListeners) { _closedListeners.add(l); }
  }
  
  public void removeDocumentClosedListener(DocumentClosedListener l) { 
    synchronized(_closedListeners) { _closedListeners.remove(l); }
  }
  
  // begin debug code
  
//  private boolean _closed = false;
//  
//  protected void throwErrorHuh() {
//    if (_closed) throw new RuntimeException("Definitions Document is closed, yet is being used");
//  }
  
  /** Called when this is kicked out of the document cache so that the references made to it may 
   *  be released so that this can be GC'd. */
  public void close() {
    _removeIndenter();
    synchronized(_closedListeners) {
      for (DocumentClosedListener l: _closedListeners)  { l.close(); }
      _closedListeners = new LinkedList<DocumentClosedListener>();
    }
  }
  
  // end debug code
  
  /** The maximum number of undos the model can remember */
  private static final int UNDO_LIMIT = 1000;
  /** Specifies if tabs are removed on open and converted to spaces. */
  private static boolean _tabsRemoved = true;
  /** Specifies if the document has been modified since the last save. */
  private volatile boolean _isModifiedSinceSave = false;
  /** Specifies if classFile is in sync with current state of the document */
  private volatile boolean _classFileInSync = false;
  /** Cached location, aides in determining line number. */
  private volatile int _cachedLocation;
  /** Cached current line number. */
  private volatile int _cachedLineNum;
  /** Cached location of previous line. */
  private volatile int _cachedPrevLineLoc;
  /** Cached location of next line. */
  private volatile int _cachedNextLineLoc;

  /** The package name last extracted from this document. */
  private volatile String _packageName;
  
  private volatile File _classFile;

  /** This reference to the OpenDefinitionsDocument is needed so that the document iterator 
   * (the DefaultGlobalModel) can find the next ODD given a DD. */
  private volatile OpenDefinitionsDocument _odd;
  
  private volatile CompoundUndoManager _undoManager;
  
  /** Keeps track of the listeners to this model. */
  private final GlobalEventNotifier _notifier;
  
  /* Relying on the following definition in AbstractDJDocument.  It must be placed there to be initialized before use!
  protected static final Object _wrappedPosListLock = new Object();
  */
  
  /** List with weak references to positions. */
  private volatile LinkedList<WeakReference<WrappedPosition>> _wrappedPosList;
  
  /** Convenience constructor for using a custom indenter.
   *  @param indenter custom indenter class
   *  @param notifier used by CompoundUndoManager to announce undoable edits
   */
  public DefinitionsDocument(Indenter indenter, GlobalEventNotifier notifier) {
    super(indenter);
    _notifier = notifier;
    _init();
    resetUndoManager();
  }

  /** Main constructor.  This has an obnoxious dependency on GlobalEventNotifier, which is passed through here only 
   *  for a single usage in CompoundUndoManager.  TODO: find a better way.
   *  @param notifier used by CompoundUndoManager to announce undoable edits
   */
  public DefinitionsDocument(GlobalEventNotifier notifier) {
    super();
    _notifier = notifier;
    _init();
    resetUndoManager();
  }

  /** Main constructor.  This has an obnoxious dependency on GlobalEventNotifier, which is passed through here only 
   *  for a single usage in CompoundUndoManager.  TODO: find a better way.
   *  @param notifier used by CompoundUndoManager to announce undoable edits
   */
  public DefinitionsDocument(GlobalEventNotifier notifier, CompoundUndoManager undoManager) {
    super();
    _notifier = notifier;
    _init();
    _undoManager = undoManager;
  }
  
  
//  public void setUndoManager(CompoundUndoManager undoManager) {
//    if (undoManager != null)
//      _undoManager = undoManager;
//  }

  /** Returns a new indenter. */
  protected Indenter makeNewIndenter(int indentLevel) { return new Indenter(indentLevel); }
  
  /** Private common helper for constructors. */
  private void _init() {
    _odd = null;
    _cachedLocation = 0;
    _cachedLineNum = 1;
    _cachedPrevLineLoc = -1;
    _cachedNextLineLoc = -1;
    _classFile = null;
    _cacheInUse = false;
  }
  
  /* acquireReadLock, releaseReadLock, acquireWriteLock, releaseWriteLock are inherited from AbstractDJDocument. */
   
  /** Sets the OpenDefinitionsDocument that holds this DefinitionsDocument (the odd can only be set once).
   *  @param odd the OpenDefinitionsDocument to set as this DD's holder
   */
  public void setOpenDefDoc(OpenDefinitionsDocument odd) { if (_odd == null) _odd = odd; }
  
  /** @return the OpenDefinitonsDocument that is associated with this DefinitionsDocument. */
  public OpenDefinitionsDocument getOpenDefDoc() {
    if (_odd == null)
      throw new IllegalStateException("The OpenDefinitionsDocument for this DefinitionsDocument has never been set");
    else return _odd;
  }
  
  protected void _styleChanged() {    
    acquireWriteLock();
    try {
      int length = getLength() - _currentLocation;
      
      //DrJava.consoleErr().println("Changed: " + _currentLocation + ", " + length);
      DocumentEvent evt = new DefaultDocumentEvent(_currentLocation, length, DocumentEvent.EventType.CHANGE);
      fireChangedUpdate(evt);
    }
    finally { releaseWriteLock(); }
  } 
  
  
  /**
   * Returns whether this document is currently untitled
   * (indicating whether it has a file yet or not).
   * @return true if the document is untitled and has no file
   */
//  public boolean isUntitled() {
//    return (_file == null);
//  }

  /**
   * Returns the file for this document.  If the document
   * is untitled and has no file, it throws an IllegalStateException.
   * @return the file for this document
   * @throws IllegalStateException if file has not been set
   * @throws FileMovedException if file has been moved or deleted from its previous location
   */
//  public File getFilex()
//    throws IllegalStateException , FileMovedException {
//    if (_file == null) {
//      throw new IllegalStateException(
//        "This document does not yet have a file.");
//    }
//    //does the file actually exist?
//    if (_file.exists()) {
//      return _file;
//    }
//    else {
//      throw new FileMovedException(_file,
//        "This document's file has been moved or deleted.");
//    }
//  }
//
//  /**
//   * Returns the name of this file, or "(untitled)" if no file.
//   */
//  public String getFilenamex() {
//    String filename = "(Untitled)";
//    try {
//      File file = getFilex();
//      filename = file.getName();
//    }
//    catch (IllegalStateException ise) {
//      // No file, leave as "untitled"
//    }
//    catch (FileMovedException fme) {
//      // Recover, even though file has been deleted
//      File file = fme.getFile();
//      filename = file.getName();
//    }
//    return filename;
//  }


//  public void setFile(File file) {
//    _file = file;
//
//    //jim: maybe need lock
//    if (_file != null) {
//      _timestamp = _file.lastModified();
//    }
//  }
//
//  public long getTimestamp() {
//    return _timestamp;
//  }


  /** Gets the package and main class/interface name of this OpenDefinitionsDocument
   *  @return the qualified main class/interface name
   */
  public String getQualifiedClassName() throws ClassNameNotFoundException {
    return _getPackageQualifier() + getMainClassName();
  }

  /** Gets fully qualified class name of the top level class enclosing the given position. */
  public String getQualifiedClassName(int pos) throws ClassNameNotFoundException {
    return _getPackageQualifier() + getEnclosingTopLevelClassName(pos);
  }

  /** Gets an appropriate prefix to fully qualify a class name. Returns this class's package followed by a dot, or the
   *  empty string if no package name is found.
   */
  protected String _getPackageQualifier() {
    String packageName = "";
    try { packageName = getPackageName(); }
    catch (InvalidPackageException e) { 
      /* Couldn't find package, pretend there's none; findbugs requires multi-line formatting of this clause */ 
    }
    if ((packageName != null) && (! packageName.equals(""))) { packageName = packageName + "."; }
    return packageName;
  }

  public void setClassFileInSync(boolean inSync) { _classFileInSync = inSync; }

  public boolean getClassFileInSync() { return _classFileInSync; }

  public void setCachedClassFile(File classFile) { _classFile = classFile; }

  public File getCachedClassFile() { return _classFile; }

  /** Inserts a string of text into the document.  This is not where we do custom processing of the insert; that is
   *  done in {@link #insertUpdate}.
   */
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
    
    // If _removeTabs is set to true, remove all tabs from str.
    // It is a current invariant of the tabification functionality that
    // the document contains no tabs, but we want to allow the user
    // to override this functionality.
    
    acquireWriteLock();
    try {
      if (_tabsRemoved) str = _removeTabs(str);
      setModifiedSinceSave();
      super.insertString(offset, str, a);
    }
    finally { releaseWriteLock(); }
  }
  
  
  /** Removes a block of text from the specified location. We don't update the reduced model here; that happens
   *  in {@link #removeUpdate}.
   */
  public void remove(int offset, int len) throws BadLocationException {
    
    if (len == 0) return;
    
    acquireWriteLock();
    try {
      setModifiedSinceSave();
      super.remove(offset, len);
    }
    finally { releaseWriteLock(); }
  }

  /** Given a String, return a new String will all tabs converted to spaces.  Each tab is converted 
   *  to one space, since changing the number of characters within insertString screws things up.
   *  @param source the String to be converted.
   *  @return a String will all the tabs converted to spaces
   */
  static String _removeTabs(final String source) {
//    clearCache(); // Clear the helper method cache  // Goofy code! Eliminated when method was converted to static.
    return source.replace('\t', ' ');
  }

  /** Resets the modification state of this document to be consistent with state of _undoManager.  Called whenever
   *  an undo or redo is performed. */
  public void updateModifiedSinceSave() {
    
    acquireWriteLock();
    try {
    _isModifiedSinceSave = _undoManager.isModified();
//    System.out.println("DefinitionsDocument: set modified? " + _modifiedSinceSave);
    }
    finally { 
      if (! _isModifiedSinceSave && _odd != null) _odd.documentReset();
      releaseWriteLock();
//    Utilities.showDebug("DefintionsDocument: _modifiedSinceSave = " + _modifiedSinceSave);
    }
  }
  
   /** Sets the modification state of this document to true and updates the state of the associated _odd. 
    *  Assumes that write lock is already held. */
  private void setModifiedSinceSave() {
    if (! _isModifiedSinceSave) {
      _isModifiedSinceSave = true;
      _classFileInSync = false;
      if (_odd != null) _odd.documentModified();
    }    
  }
  
  /** Resets the modification state of this document.  Used after a document has been saved or reverted. */
  public void resetModification() {
    acquireWriteLock();
    try {
      _isModifiedSinceSave = false;
      _undoManager.documentSaved();
    }
    finally { 
      if (_odd != null) _odd.documentReset();  // null test required for some unit tests
      releaseWriteLock(); 

    }
  }
  
  /** Determines if the document has been modified since the last save.
   *  @return true if the document has been modified
   */
  public boolean isModifiedSinceSave() {
    acquireReadLock();
    try { return  _isModifiedSinceSave; }
    finally { releaseReadLock(); }
  }
  
  /** Return the current column of the cursor position. Uses a 0 based index. */
  public int getCurrentCol() {
    // throwErrorHuh();`
    int here = _currentLocation;
    int startOfLine = getLineStartPos(here);
    return here - startOfLine;
  }

  /** Return the current line of the cursor position.  Uses a 1-based index. */
  public int getCurrentLine() {
    acquireReadLock();
    try {
      synchronized(_reduced) {
        int here = _currentLocation;
        if (_cachedLocation > getLength()) {
          // we can't know the last line number after a delete; starting over.
          _cachedLocation = 0;
          _cachedLineNum = 1;
        }
        if (_cachedNextLineLoc > getLength()) _cachedNextLineLoc = -1;
        // let's see if we get off easy
        if ( ! (_cachedPrevLineLoc < here && here < _cachedNextLineLoc )) {
          
          // this if improves performance when moving from the end of the document to the beginnning.
          // in essence, it calculates the line number from scratch
          if (_cachedLocation - here > here) {
            _cachedLocation = 0;
            _cachedLineNum = 1;
          }
          int lineOffset = _getRelativeLine();
          _cachedLineNum = _cachedLineNum + lineOffset;
          
        }
        _cachedLocation = here;
        _cachedPrevLineLoc = getLineStartPos(here);
        _cachedNextLineLoc = getLineEndPos(here);
        return _cachedLineNum;
      }
    }
    finally { releaseReadLock(); }
  }

  /** This method returns the relative offset of line number from the previous location in the document. 
    * Assumes the readLock is already held. 
    */
  private int _getRelativeLine() {
    
    int count = 0;
    int currLoc = _currentLocation;
    setCurrentLocation(_cachedLocation);

    if (_cachedLocation > currLoc) {
      // we moved backward
      int prevLineLoc = getLineStartPos( _cachedLocation );
      while (prevLineLoc > currLoc) {
        count--;
        prevLineLoc = getLineStartPos( prevLineLoc - 1 );
      }
    }

    else {
      // we moved forward
      int nextLineLoc = getLineEndPos( _cachedLocation );
      while (nextLineLoc < currLoc) {
        count++;
        nextLineLoc = getLineEndPos( nextLineLoc + 1 );
      }
    }
    setCurrentLocation(currLoc);
    return count;
  }

  /** Returns the offset corresponding to the first character of the given line number,
   *  or -1 if the lineNum is not found.  Avoid locking the document by copying its text.
   *  @param lineNum the line number for which to calculate the offset.
   *  @return the offset of the first character in the given line number
   */
  public int getOffset(int lineNum) {
    if (lineNum < 0) return -1;
    String defsText = getText();
    int curLine = 1;
    int offset = 0; // offset is number of chars from beginning of file
    
    // offset is always pointing to the first character in a line
    // at the top of the loop
    while (offset < defsText.length()) {
      
      if (curLine == lineNum) return offset;
      
      int nextNewline = defsText.indexOf('\n', offset);
      if (nextNewline == -1) return -1; // end of the document, and couldn't find the supplied lineNum
      
      curLine++;
      offset = nextNewline + 1;
    }
    return -1;
  }

  /** Returns true iff tabs are to removed on text insertion. */
  public boolean tabsRemoved() { return _tabsRemoved; }
 
  /** Comments out all lines between selStart and selEnd, inclusive. The current cursor position is maintained 
   *  after the operation.
   *  @param selStart the document offset for the start of the selection
   *  @param selEnd the document offset for the end of the selection
   */
  public int commentLines(int selStart, int selEnd) {
    
    //int key = _undoManager.startCompoundEdit();  //Uncommented in regards to the FrenchKeyBoardFix
    int toReturn = selEnd;
    if (selStart == selEnd) {
      acquireWriteLock();
      try {     
        synchronized(_reduced) {
          setCurrentLocation(selStart);
          Position oldCurrentPosition = createUnwrappedPosition(_currentLocation);
          _commentLine();   
          toReturn += WING_COMMENT_OFFSET;
          //int caretPos = getCaretPosition();
          //_doc().setCurrentLocation(caretPos);
        }
      }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
      finally { releaseWriteLock(); }
    }
    else toReturn = _commentBlock(selStart, selEnd);   
    _undoManager.endLastCompoundEdit();  //Changed from endCompoundEdit(key) for FrenchKeyBoardFix
    return toReturn;
  }
 

  /** Comments out the lines between and including the lines containing points start and end, using wing 
   *  comments -- "// ".
   * 
   *  @param start Position in document to start commenting from
   *  @param end Position in document to end commenting at
   */
  private int _commentBlock(final int start, final int end) {
    int afterCommentEnd = end;
    acquireWriteLock();
    try {
      // Keep marker at the end. This Position will be the correct endpoint no matter how we change the doc doing the
      // indentLine calls.
      final Position endPos = this.createUnwrappedPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = start;
      synchronized(_reduced) {
        while (walker < endPos.getOffset()) {
          setCurrentLocation(walker);
          // Keep pointer to walker position that will stay current
          // regardless of how commentLine changes things
          Position walkerPos = this.createUnwrappedPosition(walker);
          // Comment out current line
          _commentLine();  // must be atomic
          afterCommentEnd += WING_COMMENT_OFFSET;
          // Move back to walker spot
          setCurrentLocation(walkerPos.getOffset());
          walker = walkerPos.getOffset();
          // Adding 1 makes us point to the first character AFTER the next newline.
          // We don't actually move yet. That happens at the top of the loop,
          // after we check if we're past the end.
          walker += _reduced.getDistToNextNewline() + 1;
          //DrJava.consoleOut().println("progress: " + (100*(walker-start)/(end-start)));
        }
      }
    } 
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    finally { releaseWriteLock(); }
    return afterCommentEnd;
  }

  /** Comments out a single line with wing comments -- "// ". 
   *  @pre this.writeLock() and _reduced lock are already held! */
  private void _commentLine() {
    // Insert "// " at the beginning of the line.
    // Using null for AttributeSet follows convention in this class.
    try { insertString(_currentLocation - getCurrentCol(), "//", null); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }

  /** Uncomments all lines between selStart and selEnd, inclusive.
   *  The current cursor position is maintained after the operation.
   *  @param selStart the document offset for the start of the selection
   *  @param selEnd the document offset for the end of the selection
   */
  public int uncommentLines(int selStart, int selEnd) {
 
    //int key = _undoManager.startCompoundEdit(); //commented out for FrenchKeyBoardFix
    int toReturn = selEnd;
    if (selStart == selEnd) {
      acquireWriteLock();
      try {
        synchronized(_reduced) {
          setCurrentLocation(selStart);
          Position oldCurrentPosition = createUnwrappedPosition(_currentLocation);
          _uncommentLine();  // accesses _reduced
          toReturn -= WING_COMMENT_OFFSET;
          //int caretPos = getCaretPosition();
          //_doc().setCurrentLocation(caretPos);
          //setCurrentLocation(oldCurrentPosition.getOffset());
        }
      }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
      finally { releaseWriteLock(); }
    }
    else  toReturn = _uncommentBlock(selStart, selEnd);
    //_undoManager.endCompoundEdit(key); //Commented out for FrenchKeyBoardFix, Replaced with endLastCompoundEdit();
    _undoManager.endLastCompoundEdit();
    return toReturn;
  }

  /** Uncomments all lines between and including the lines containing
   *  points start and end.  
   * @param start Position in document to start commenting from
   * @param end Position in document to end commenting at
   */
  private int _uncommentBlock(final int start, final int end) {
    int afterUncommentEnd = end;
    acquireWriteLock();
    try {
      // Keep marker at the end. This Position will be the correct endpoint no matter how we change the doc
      // doing the indentLine calls.
      final Position endPos = this.createUnwrappedPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = start;
      synchronized(_reduced) {
        while (walker < endPos.getOffset()) {
          setCurrentLocation(walker);
          // Keep pointer to walker position that will stay current
          // regardless of how commentLine changes things
          Position walkerPos = this.createUnwrappedPosition(walker);
          // uncomment current line
          afterUncommentEnd-= _uncommentLine();  // accesses _reduced
          // Move back to walker spot
          setCurrentLocation(walkerPos.getOffset());
          walker = walkerPos.getOffset();
          // Adding 1 makes us point to the first character AFTER the next newline.
          // We don't actually move yet. That happens at the top of the loop,
          // after we check if we're past the end.
          walker += _reduced.getDistToNextNewline() + 1;
          //DrJava.consoleOut().println("progress: " + (100*(walker-start)/(end-start)));
        }
      }
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    finally { releaseWriteLock(); }
    return afterUncommentEnd;
  }

  /** Uncomments a single line.  This simply looks for a leading "//".  Assumes that _reduced lock is already held and
   *  that acquireWriteLock is already held.
   *  @pre theads hold this.writeLock() and _reduced lock
   */
  private int _uncommentLine() throws BadLocationException {
    // Look for "//" at the beginning of the line, and remove it.
    int curCol = getCurrentCol();
    int lineStart = _currentLocation - curCol;
    String text = getText(lineStart, curCol + _reduced.getDistToNextNewline());
    int pos = text.indexOf("//");
    
    //      System.out.println("" + _currentLocation + " " + curCol + " "
    //                           + text + " " + pos + " " + _reduced.getDistToNextNewline());
    
    // Look for any non-whitespace chars before the "//" on the line.
    boolean goodWing = true;
    for (int i = pos-1; i >= 0; i--) {
      char c = text.charAt(i);
      // If a previous char is not whitespace, we're not looking at a wing comment.
      if (c != ' ') {
        goodWing = false;
        return NO_COMMENT_OFFSET;
      }
    }
    
    // If a wing comment wasn't found, or if the wings aren't the first
    // non-whitespace characters on the line, do nothing.
    if (pos >= 0 && goodWing) {
      // Otherwise, remove the wings.
      remove(lineStart + pos, 2);
      //_indentLine(Indenter.OTHER);
      return WING_COMMENT_OFFSET;
    }
    return NO_COMMENT_OFFSET;
  }

  /** Goes to a particular line in the document. */
  public void gotoLine(int line) {

    int dist;
    if (line < 0) return;
    int actualLine =1;
    
    acquireReadLock();
    int len = getLength();
    try {
      synchronized(_reduced) {
        setCurrentLocation(0);
        for (int i = 1; (i < line) && (_currentLocation < len); i++) {
          dist = _reduced.getDistToNextNewline();
          if (_currentLocation + dist < len) dist++;
          actualLine++;
          move(dist);
        }
        _cachedLineNum = actualLine;
        _cachedLocation = _currentLocation;
        _cachedPrevLineLoc = getLineStartPos(_currentLocation);
        _cachedNextLineLoc = getLineEndPos(_currentLocation);
      }
    }
    finally { releaseReadLock(); }
  }  
  
  private int _findNextOpenSquiggly(String text, int pos) throws BadLocationException {
    // acquireReadLock assumed to be held,
    int i;
    int reducedPos = pos;
    
    synchronized(_reduced) {
      final int origLocation = _currentLocation;
      // Move reduced model to location pos
      _reduced.move(pos - origLocation);  // reduced model points to pos == reducedPos
      
      // Walk forward from specificed position
      i = text.indexOf('{', reducedPos);
      while(i>-1) {
        // Move reduced model to walker's location
        _reduced.move(i - reducedPos);  // reduced model points to i
        reducedPos = i;                 // reduced model points to reducedPos
        
        // Check if matching keyword should be ignored because it is within a comment, or quotes
        ReducedModelState state = _reduced.getStateAtCurrent();
        if (!state.equals(ReducedModelState.FREE) || _isStartOfComment(text, i)
              || ((i > 0) && _isStartOfComment(text, i - 1))) {
          i = text.indexOf('{', reducedPos+1);
          continue;  // ignore matching brace
        }
        else {
          break; // found our brace
        }        
      }  // end synchronized
      
      _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
    }
    
    if (i == -1) reducedPos = ERROR_INDEX; // No matching brace was found
    return reducedPos;  
  }
  
  private int _findPrevKeyword(String text, String kw, int pos) throws BadLocationException {
    // acquireReadLock assumed to be held,
    int i;
    int reducedPos = pos;
    
    synchronized(_reduced) {
      final int origLocation = _currentLocation;
      // Move reduced model to location pos
      _reduced.move(pos - origLocation);  // reduced model points to pos == reducedPos
      
      // Walk backwards from specificed position
      i = text.lastIndexOf(kw, reducedPos);
      while(i >- 1) {
        // Check that this is the beginning of a word
        if (i > 0) {
          if (Character.isJavaIdentifierPart(text.charAt(i-1))) {
            // not begining
            i = text.lastIndexOf(kw, i-1);
            continue;  // ignore matching keyword 
          }
        }
        // Check that this not just the beginning of a longer word
        if (i+kw.length()<text.length()) {
          if (Character.isJavaIdentifierPart(text.charAt(i+kw.length()))) {
            // not begining
            i = text.lastIndexOf(kw, i-1);
            continue;  // ignore matching keyword 
          }
        }
        
        // Move reduced model to walker's location
        _reduced.move(i - reducedPos);  // reduced model points to i
        reducedPos = i;                 // reduced model points to reducedPos
        
        // Check if matching keyword should be ignored because it is within a comment, or quotes
        ReducedModelState state = _reduced.getStateAtCurrent();
        if (!state.equals(ReducedModelState.FREE) || _isStartOfComment(text, i)
              || ((i > 0) && _isStartOfComment(text, i - 1))) {
          i = text.lastIndexOf(kw, reducedPos-1);
          continue;  // ignore matching keyword 
        }
        else {
          break; // found our keyword
        }        
      }  // end synchronized/
      
      _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
    }
    
    if (i == -1) reducedPos = ERROR_INDEX; // No matching keyword was found
    return reducedPos;  
  }
 
//  public static boolean log = true;
  
  /** Searching backwards finds the name of the enclosing named class or interface. NB: ignores comments.
   *  WARNING: In long source files and when contained in anonymous inner classes, this function might take a LONG time.
   * @param pos Position to start from
   * @param qual true to find the fully qualified class name
   * @return name of the enclosing named class or interface
   */
  public String getEnclosingClassName(int pos, boolean qual) throws BadLocationException, ClassNameNotFoundException {    
//    boolean oldLog = log; log = false;
    // Check cache
    final StringBuilder keyBuf = new StringBuilder("getEnclosingClassName:").append(pos);
    keyBuf.append(":").append(qual);
    String key = keyBuf.toString();
    String cached = (String) _checkCache(key);
    if (cached != null) return cached;

    char[] delims = {'{','}','(',')','[',']','+','-','/','*',';',':','=',
      '!','@','#','$','%','^','~','\\','"','`','|'};
    String name = "";

    acquireReadLock();
    try {
      String text = getText(DOCSTART, pos+1);
      
      int curPos = pos;
      
      do {
        if ((text.charAt(curPos)!='{') || (text.charAt(curPos)!='}')) { ++curPos; }
        
//        if (oldLog) System.out.println("curPos=" + curPos + " `" +
//                                       text.substring(Math.max(0,curPos-10), Math.min(text.length(), curPos+1)) + "`");
        
        curPos = findPrevEnclosingBrace(curPos, '{', '}');
        if (curPos==ERROR_INDEX) { break; }
        int classPos = _findPrevKeyword(text, "class", curPos);
        int interPos = _findPrevKeyword(text, "interface", curPos);
        int otherPos = findPrevDelimiter(curPos, delims);
        int newPos = ERROR_INDEX;
        // see if there's a ) closer by
        int closeParenPos = findPrevNonWSCharPos(curPos);
        if ((closeParenPos!=ERROR_INDEX) && (text.charAt(closeParenPos)==')')) {
          // yes, find the matching (
          int openParenPos = findPrevEnclosingBrace(closeParenPos, '(', ')');
          if ((openParenPos!=ERROR_INDEX) && (text.charAt(openParenPos)=='(')) {
            // this might be an inner class
            newPos = _findPrevKeyword(text, "new", openParenPos);
//            if (oldLog) System.out.println("\tnew found at "+newPos+", openSquigglyPos="+curPos);
            if (! _isAnonymousInnerClass(newPos, curPos)) {
              // not an anonymous inner class
              newPos = ERROR_INDEX;
            }
          }
        }
//        if (oldLog) System.out.println("curPos="+curPos+" `"+text.substring(Math.max(0,curPos-10),curPos+1)+"`");
//        if (oldLog) System.out.println("\tclass="+classPos+", inter="+interPos+", other="+otherPos+", new="+newPos+" `" +
//          text.substring(Math.max(0,otherPos-10),otherPos+1)+"`");
        while((classPos!=ERROR_INDEX) || (interPos!=ERROR_INDEX) || (newPos!=ERROR_INDEX)) {
          if (newPos!=ERROR_INDEX) {
//            if (oldLog) System.out.println("\tanonymous inner class! newPos = "+newPos);
            classPos = ERROR_INDEX;
            interPos = ERROR_INDEX;
            break;
          }
          else if ((otherPos>classPos) && (otherPos>interPos)) {
            if ((text.charAt(otherPos)!='{') || (text.charAt(otherPos)!='}')) { ++otherPos; }
            curPos = findPrevEnclosingBrace(otherPos, '{', '}');
            classPos = _findPrevKeyword(text, "class", curPos);
            interPos = _findPrevKeyword(text, "interface", curPos);
            otherPos = findPrevDelimiter(curPos, delims);
            newPos = ERROR_INDEX;
            // see if there's a ) closer by
            closeParenPos = findPrevNonWSCharPos(curPos);
//            if (closeParenPos!=ERROR_INDEX) if (oldLog) System.out.println("nonWS before curPos = " + closeParenPos + 
//              " `"+text.charAt(closeParenPos)+"`");
            if ((closeParenPos!=ERROR_INDEX) && (text.charAt(closeParenPos)==')')) {
              // yes, find the matching (
              int openParenPos = findPrevEnclosingBrace(closeParenPos, '(', ')');
              if ((openParenPos!=ERROR_INDEX) && (text.charAt(openParenPos)=='(')) {
                // this might be an inner class
                newPos = _findPrevKeyword(text, "new", openParenPos);
//                if (oldLog) System.out.println("\tnew found at " + newPos + ", openSquigglyPos=" + curPos);
                if (_isAnonymousInnerClass(newPos, curPos)) {
                  // yes, anonymous inner class
                }
                else {
                  newPos = ERROR_INDEX;
                }
              }
            }
//            if (oldLog) System.out.println("curPos=" +curPos+" `"+text.substring(Math.max(0,curPos-10),curPos+1)+"`");
//            if (oldLog) System.out.println("\tclass="+classPos+", inter="+interPos+", other="+otherPos+" `"+
//              text.substring(Math.max(0,otherPos-10),otherPos+1)+"`");
          }
          else {
            // either class or interface found first            
            curPos = Math.max(classPos, Math.max(interPos, newPos));
            break;
          }
        }
        
        if ((classPos!=ERROR_INDEX) || (interPos!=ERROR_INDEX)) {
          if (classPos>interPos) {
            // class found first
            curPos += "class".length();
          }
          else {
            // interface found first
            curPos += "interface".length();
          }
          int nameStart = getFirstNonWSCharPos(curPos);
          if (nameStart==ERROR_INDEX) { throw new ClassNameNotFoundException("Cannot determine enclosing class name"); }
          int nameEnd = nameStart+1;
          while(nameEnd<text.length()) {
            if ((!Character.isJavaIdentifierPart(text.charAt(nameEnd))) && (text.charAt(nameEnd)!='.')) {
              // delimiter found
              break;
            }
            ++nameEnd;
          }
          name = text.substring(nameStart,nameEnd) + '$' + name;
        }
        else if (newPos!=ERROR_INDEX) {
          name = String.valueOf(_getAnonymousInnerClassIndex(curPos)) + "$" + name;
          curPos = newPos;
        }
        else {
          // neither class nor interface found
          break;
        }
      } while(qual);
    }
    finally { releaseReadLock(); }
    
    // chop off '$' at the end.
    if (name.length()>0) name = name.substring(0, name.length()-1);
    
    if (qual) {
      String pn = getPackageName();
      if ((pn.length()>0) && (name.length()>0)) {
        name = getPackageName() + "." + name;
      }
    }
//    log = oldLog;
    return name;
  }
  
  /** Returns true if this position is the instantiation of an anonymous inner class.
   *  @param newPos position of "new"
   *  @param openSquigglyPos position of the next '{'
   *  @return true if anonymous inner class instantiation
   */
  private boolean _isAnonymousInnerClass(int newPos, int openSquigglyPos) throws BadLocationException {
//    String t = getText(DOCSTART, openSquigglyPos+1);
//    System.out.print ("_isAnonymousInnerClass("+newPos+", "+openSquigglyPos+")");
//    System.out.println("_isAnonymousInnerClass("+newPos+", "+openSquigglyPos+"): `"+
//                       t.substring(newPos, openSquigglyPos+1)+"`");
    
    // Check cache
    final StringBuilder keyBuf = 
      new StringBuilder("_getAnonymousInnerClassIndex:").append(newPos).append(':').append(openSquigglyPos);
    String key = keyBuf.toString();
    Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) {
//      System.out.println(" ==> "+cached);
      return cached;
    }

    // acquireReadLock assumed to be held
    cached = false;
    String text = getText(DOCSTART, openSquigglyPos+1);
    int origNewPos = newPos;
    newPos += "new".length();
    int classStart = getFirstNonWSCharPos(newPos);
    if (classStart!=ERROR_INDEX) { 
      int classEnd = classStart+1;
      while(classEnd<text.length()) {
        if ((!Character.isJavaIdentifierPart(text.charAt(classEnd))) && (text.charAt(classEnd)!='.')) {
          // delimiter found
          break;
        }
        ++classEnd;
      }
      // System.out.println("\tclass = `"+text.substring(classStart,classEnd)+"`");
      int parenStart = getFirstNonWSCharPos(classEnd);
      if (parenStart!=ERROR_INDEX) {
        int origParenStart = parenStart;

        // System.out.println("\tfirst non-whitespace after class = "+parenStart+" `"+text.charAt(parenStart)+"`");
        if (text.charAt(origParenStart)=='<') {
          parenStart = ERROR_INDEX;
          // might be a generic class
          int closePointyBracket = findNextEnclosingBrace(origParenStart, '<', '>');
          if (closePointyBracket!=ERROR_INDEX) {
            if (text.charAt(closePointyBracket)=='>') {
              parenStart = getFirstNonWSCharPos(closePointyBracket+1);
            }
          }
        }
      }
      if (parenStart!=ERROR_INDEX) {
        if (text.charAt(parenStart)=='(') {
          synchronized(_reduced) {
            final int origLocation = _currentLocation;
            _reduced.move(parenStart+1 - origLocation);  // reduced model points to pos == parenStart+1
            int parenEnd = balanceForward();
            _reduced.move(origLocation - (parenStart+1));    // Restore the state of the reduced model;
            if (parenEnd > -1) {
              parenEnd = parenEnd + parenStart+1;
              // System.out.println("\tafter closing paren = "+parenEnd);
              int afterParen = getFirstNonWSCharPos(parenEnd);
              // System.out.println("\tfirst non-whitespace after paren = "+parenStart+" `"+text.charAt(afterParen)+"`");
              cached = (afterParen==openSquigglyPos);          
            }
          }
        }
      }
    }
    
    _storeInCache(key, cached);
    
//    System.out.println(" ==> "+cached);
    return cached;
  }
  
  /** Gets the package name embedded in the text of this document by minimally parsing the document to find the
   *  package statement.  If package statement is not found or is ill-formed, returns "" as the package name.
   *  @return The name of package embedded in this document.  If there is no well-formed package statement, 
   *          returns "" as the package name.
   */
  public String getPackageName() {
    try { return getStrictPackageName(); }
    catch(InvalidPackageException e) { return ""; }
  }
 
  /**
   * Return the index of the anonymous inner class being instantiated at the specified position.
   * @param position of the opening curly brace of the anonymous inner class
   * @return anonymous class index
   */
  int _getAnonymousInnerClassIndex(int pos) throws BadLocationException, ClassNameNotFoundException {   
//    boolean oldLog = log; log = false;
    
    // Check cache
    final StringBuilder keyBuf = new StringBuilder("_getAnonymousInnerClassIndex:").append(pos);
    final String key = keyBuf.toString();
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
//      log = oldLog;
      return cached.intValue();
    }

    // acquireReadLock assumed to be held
    --pos; // move outside the curly brace
    char[] delims = {'{','}','(',')','[',']','+','-','/','*',';',':','=',
      '!','@','#','$','%','^','~','\\','"','`','|'};
    String className = getEnclosingClassName(pos, true);
    String text = getText(DOCSTART, pos);
    int index = 1;
    int newPos = pos;
//    if (oldLog) System.out.println("anon before "+pos+" enclosed by "+className);
    while((newPos = _findPrevKeyword(text, "new", newPos-1)) != ERROR_INDEX) {
//      if (oldLog) System.out.println("new found at "+newPos);
      int afterNewPos = newPos + "new".length();
      int classStart = getFirstNonWSCharPos(afterNewPos);
      if (classStart==ERROR_INDEX) { continue; }
      int classEnd = classStart+1;
      while(classEnd<text.length()) {
        if ((!Character.isJavaIdentifierPart(text.charAt(classEnd))) && (text.charAt(classEnd)!='.')) {
          // delimiter found
          break;
        }
        ++classEnd;
      }
//      if (oldLog) System.out.println("\tclass = `"+text.substring(classStart,classEnd)+"`");
      int parenStart = getFirstNonWSCharPos(classEnd);
      if (parenStart==ERROR_INDEX) { continue; }
      int origParenStart = parenStart;
      
//      if (oldLog) System.out.println("\tfirst non-whitespace after class = "+parenStart+" `"+text.charAt(parenStart)+"`");
      if (text.charAt(origParenStart)=='<') {
        parenStart = ERROR_INDEX;
        // might be a generic class
        int closePointyBracket = findNextEnclosingBrace(origParenStart, '<', '>');
        if (closePointyBracket!=ERROR_INDEX) {
          if (text.charAt(closePointyBracket)=='>') {
            parenStart = getFirstNonWSCharPos(closePointyBracket+1);
          }
        }
      }
      if (parenStart==ERROR_INDEX) { continue; }      
      if (text.charAt(parenStart)!='(') { continue; }
      int parenEnd = findNextEnclosingBrace(parenStart, '(', ')');
    
      int nextOpenSquiggly = _findNextOpenSquiggly(text, parenEnd);
      if (nextOpenSquiggly==ERROR_INDEX) { continue; }
//      if (oldLog) System.out.println("{ found at "+nextOpenSquiggly+": `"+text.substring(newPos, nextOpenSquiggly+1)+"`");
//      if (oldLog) System.out.println("_isAnonymousInnerClass("+newPos+", "+nextOpenSquiggly+")");
      if (_isAnonymousInnerClass(newPos, nextOpenSquiggly)) {
//        if (oldLog) System.out.println("is anonymous inner class");
        String cn = getEnclosingClassName(newPos, true);
//        if (oldLog) System.out.println("enclosing class = "+cn);
        if (!cn.startsWith(className)) { break; }
        else if (!cn.equals(className)) {
          newPos = findPrevEnclosingBrace(newPos, '{', '}');
          continue;
        }
        else {
          ++index;
        }
      }
    }
    _storeInCache(key, new Integer(index));
//    oldLog = log;
    return index;
  }

  /** Gets the name of the package this source file claims it's in (with the package keyword). It does this by 
   *  minimally parsing the source file to find the package statement.
   *  @return The name of package declared for this source file OR the empty string if there is no package 
   *          statement (and thus the source file is in the empty package).
   *  @exception InvalidPackageException if there is some sort of a <TT>package</TT> statement but the defined 
   *             package does not match or exist.
   */
  protected String getStrictPackageName() throws InvalidPackageException {
    
//    Utilities.show("getPackageName() called on " + this);
    /* Buffer for constructing the package name. */
    final StringBuilder buf = new StringBuilder();
    int oldLocation = 0;  // javac requires this bogus initialization
    
    acquireReadLock();
    try {
      final String text = getText();
      final int docLength = text.length();
      if (docLength == 0) return "";
      
      // perturbing reduced model, which is reset in finally clause
      synchronized(_reduced) {
        oldLocation = _currentLocation;
        try {
          setCurrentLocation(0);
          
          /* The location of the first non-whitespace character that is not inside a string or comment. */
          int firstNormalLocation = 0;
          while (firstNormalLocation < docLength) {
            setCurrentLocation(firstNormalLocation);
            
            if (_reduced.currentToken().getHighlightState() == HighlightStatus.NORMAL) {
              // OK, it's normal -- so if it's not whitespace, we found the spot
              char curChar = text.charAt(firstNormalLocation);
              if (! Character.isWhitespace(curChar)) break;
            }
            firstNormalLocation++;
          }
          
          // Now there are two possibilities: firstNormalLocation is at the first spot of a non-whitespace character 
          // that's NORMAL, or it's at the end of the document.
          
          if (firstNormalLocation == docLength) return "";
          
          final int strlen = "package".length();
          
          final int endLocation = firstNormalLocation + strlen;
          
          if ((firstNormalLocation + strlen > docLength) ||
              ! text.substring(firstNormalLocation, endLocation).equals("package")) {
            // The first normal text is not "package" or there is not enough text for there to be a package statement.
            // Thus, there is no valid package statement.
            return "";
          }
          
          // OK, we must have found a package statement.
          // Now let's find the semicolon. Again, the semicolon must be free.
          int afterPackage = firstNormalLocation + strlen;
          
          int semicolonLocation = afterPackage;
          do {
            semicolonLocation = text.indexOf(";", semicolonLocation + 1);
            if (semicolonLocation == -1)
              throw new InvalidPackageException(firstNormalLocation,
                                                "No semicolon found to terminate package statement!");
            setCurrentLocation(semicolonLocation);
          }
          while (_reduced.currentToken().getHighlightState() != HighlightStatus.NORMAL);
          
          // Now we have semicolon location. We'll gather text in between one character at a time for simplicity. 
          for (int walk = afterPackage + 1; walk < semicolonLocation; walk++) {
            setCurrentLocation(walk);
            if (_reduced.currentToken().getHighlightState() == HighlightStatus.NORMAL) {
              char curChar = text.charAt(walk);
              if (! Character.isWhitespace(curChar)) buf.append(curChar);
            }
          }
          
          String toReturn = buf.toString();
          if (toReturn.equals(""))
            throw new InvalidPackageException(firstNormalLocation,
                                              "Package name was not specified after the package keyword!");
          return toReturn;
        }
        finally { // reset oldLocation
          setCurrentLocation(0);  // Why?
          setCurrentLocation(oldLocation);
        }
      }
    }
    finally { releaseReadLock(); }
  }

  /** Returns the name of the class or interface enclosing the caret position at the top level.
   *  @return Name of enclosing class or interface
   *  @throws ClassNameNotFoundException if no enclosing class found
   */
  public String getEnclosingTopLevelClassName(int pos) throws ClassNameNotFoundException {
    acquireReadLock();
    synchronized(_reduced) {
      int oldLocation = _currentLocation;
      try {
        setCurrentLocation(pos);
        IndentInfo info = getIndentInformation();
        
        // Find top level open brace
        int topLevelBracePos = -1;
        String braceType = info.braceTypeCurrent;
        while (!braceType.equals(IndentInfo.noBrace)) {
          if (braceType.equals(IndentInfo.openSquiggly)) {
            topLevelBracePos = _currentLocation - info.distToBraceCurrent;
          }
          move(-info.distToBraceCurrent);
          info = getIndentInformation();
          braceType = info.braceTypeCurrent;
        }
        if (topLevelBracePos == -1) {
          // No top level brace was found, so we can't find a top level class name
          setCurrentLocation(oldLocation);
          throw new ClassNameNotFoundException("no top level brace found");
        }
        
        char[] delims = {'{', '}', ';'};
        int prevDelimPos = findPrevDelimiter(topLevelBracePos, delims);
        if (prevDelimPos == ERROR_INDEX) {
          // Search from start of doc
          prevDelimPos = DOCSTART;
        }
        else prevDelimPos++;
        setCurrentLocation(oldLocation);
        
        // Parse out the class name
        return getNextTopLevelClassName(prevDelimPos, topLevelBracePos);
      }
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      finally { 
        setCurrentLocation(oldLocation);
        releaseReadLock();
      }
    }
  }
  
  /** Gets the name of first class/interface decclared in file among the definitions anchored at:
   * @param indexOfClass  index in this of a top-level occurrence of class 
   * @param indexOfInterface  index in this of a top-level occurrence of interface
   */
  private String getFirstClassName(int indexOfClass, int indexOfInterface) throws ClassNameNotFoundException {
    
    if ((indexOfClass == -1) && (indexOfInterface == -1)) throw ClassNameNotFoundException.DEFAULT;
    if ((indexOfInterface == -1) || (indexOfClass != -1 && indexOfClass < indexOfInterface)) 
          return getNextIdentifier(indexOfClass + "class".length());
    return getNextIdentifier(indexOfInterface + "interface".length());
  }
  
  /** Gets the name of the document's main class: the document's only public class/interface or 
    * first top level class if document contains no public classes or interfaces. */
  public String getMainClassName() throws ClassNameNotFoundException {
    acquireReadLock();
    synchronized(_reduced) {
      final int oldLocation = _currentLocation;
      
      try {
        setCurrentLocation(0);
        final String text = getText();
        
        final int indexOfClass = _findKeywordAtToplevel("class", text, 0);
        final int indexOfInterface = _findKeywordAtToplevel("interface", text, 0);
        final int indexOfPublic = _findKeywordAtToplevel("public", text, 0);
        
        if (indexOfPublic == -1)  return getFirstClassName(indexOfClass, indexOfInterface);
        
//        _log.log("text =\n" + text);
//        _log.log("indexOfClass = " + indexOfClass + "; indexOfPublic = " + indexOfPublic);
        
        // There is an explicit public declaration
        final int afterPublic = indexOfPublic + "public".length();
        final String subText = text.substring(afterPublic);
        setCurrentLocation(afterPublic);
//        _log.log("After public text = '" + subText + "'");
        int indexOfPublicClass  = _findKeywordAtToplevel("class", subText, afterPublic);  // relative offset
        if (indexOfPublicClass != -1) indexOfPublicClass += afterPublic;
        int indexOfPublicInterface = _findKeywordAtToplevel("interface", subText, afterPublic); // relative offset
        if (indexOfPublicInterface != -1) indexOfPublicInterface += afterPublic;
//        _log.log("indexOfPublicClass = " + indexOfPublicClass + " indexOfPublicInterface = " + indexOfPublicInterface);
        
        return getFirstClassName(indexOfPublicClass, indexOfPublicInterface);
        
      }
      finally { 
        setCurrentLocation(oldLocation);
        releaseReadLock();
      }
    }
  }

  /** Gets the name of the top level class in this source file. This attempts to find the first declaration
   *  of a class or interface.
   *   @return The name of first class in the file
   * @throws ClassNameNotFoundException if no top level class found
   */
  public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
    return getNextTopLevelClassName(0, getLength());
  }

  // note: need to update this to work with pos
  public String getNextTopLevelClassName(int startPos, int endPos) throws ClassNameNotFoundException {

    acquireReadLock();
    synchronized(_reduced) {
      int oldLocation = _currentLocation;
      
      try {
        setCurrentLocation(startPos);
        final int textLength = endPos - startPos;
        final String text = getText(startPos, textLength);
        
        int index;
        
        int indexOfClass = _findKeywordAtToplevel("class", text, startPos);
        int indexOfInterface = _findKeywordAtToplevel("interface", text, startPos);
        int indexOfEnum = _findKeywordAtToplevel("enum",text,startPos);
        
        //If class exists at top level AND either there is no interface at top level or the index of class precedes the index of the top
        //level interface, AND the same for top level enum, then the class is the first top level declaration
        if (indexOfClass > -1 && (indexOfInterface <= -1 || indexOfClass < indexOfInterface) 
              && (indexOfEnum <= -1 || indexOfClass < indexOfEnum)) {
          index = indexOfClass + "class".length();
        }
        else if (indexOfInterface > -1 && (indexOfClass <= -1 || indexOfInterface < indexOfClass) 
                  && (indexOfEnum <= -1 || indexOfInterface < indexOfEnum)) {
          index = indexOfInterface + "interface".length();
        }
        else if (indexOfEnum > -1 && (indexOfClass <= -1 || indexOfEnum < indexOfClass)   
                   && (indexOfInterface <= -1 || indexOfEnum < indexOfInterface)) {
          index = indexOfEnum + "enum".length();
        }
        else {
          // no index was valid
          throw ClassNameNotFoundException.DEFAULT;
        }
        
        // we have a valid index
        return getNextIdentifier(startPos + index);
      }
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      catch (IllegalStateException e) { throw new ClassNameNotFoundException("No top level class name found"); }
      finally { 
        setCurrentLocation(oldLocation);
        releaseReadLock();
      }
    }
  }
  
  /** Finds the next identifier (following a non-whitespace character) in the document starting at start. Assumes that
    * read lock and _reduced lock are already held. */
  private String getNextIdentifier(final int startPos) throws ClassNameNotFoundException {
    
//    _log.log("getNextIdentifer(" + startPos + ") called");
    
//    int index = 0;
//    int length = 0;
//    int endIndex = 0;
//    String text = "";
//    int i;
    try {
      // first find index of first non whitespace (from the index in document)
      int index = getFirstNonWSCharPos(startPos);
      if (index == -1) throw new IllegalStateException("No identifier found");
      
      String text = getText();
      int length = text.length(); 
      int endIndex = length; //just in case no whitespace at end of file
      
//      _log.log("In getNextIdentifer text = \n" + text);
//      _log.log("index = " + index + "; length = " + length);
      
      //find index of next delimiter or whitespace
      char c;
      for (int i = index; i < length; i++) {
        c = text.charAt(i);
        if (! Character.isJavaIdentifierPart(c)) {
          endIndex = i;
          break;
        }
      }
//      _log.log("endIndex = " + endIndex);
      return text.substring(index, endIndex);
    }
    catch(BadLocationException e) { 
//      System.err.println("text =\n" + text);
//      System.err.println("The document =\n" + getText());
//      System.err.println("startPos = " + startPos + "; length = " + length + "; index = " + index + "; endIndex = " + endIndex);
      throw new UnexpectedException(e); 
    }
  }

  /** Finds the first occurrence of the keyword within the text (located at textOffset in this documennt) that is not 
    * enclosed within a brace or comment and is followed by whitespace.
    * @param keyword the keyword for which to search
    * @param text in which to search
    * @param textOffset Offset at which the text occurs in the document
    * @return index of the keyword in text, or -1 if the keyword is not found or not followed by whitespace
    */
  private int _findKeywordAtToplevel(String keyword, String text, int textOffset) {
    
    acquireReadLock();
    synchronized(_reduced) {
      int oldLocation = _currentLocation;
      int index = 0;
      try {
        while (true) {
          index = text.indexOf(keyword, index);
          if (index == -1) break; // not found
          else {
            // found a match, check quality
            setCurrentLocation(textOffset + index);
            
            // check that the keyword is not in a comment and is followed by whitespace
            ReducedToken rt = _reduced.currentToken();
            int indexPastKeyword = index + keyword.length();
            if (indexPastKeyword < text.length()) {
              if (rt.getState() == ReducedModelStates.FREE &&
                  Character.isWhitespace(text.charAt(indexPastKeyword))) {
                // found a match but may not be at top level
                if (! posNotInBlock(index)) index = -1; //in a paren phrase, gone too far
                break;
              }
              else index++;  //move past so we can search again
            }
            else { // No space found past the keyword
              index = -1;
              break;
            }
          }
        }
        setCurrentLocation(oldLocation);
//        _log.log("findKeyWord(" + keyword + ", ..., " + textOffset + ")");
        return index;
      }
      finally { releaseReadLock(); }
    }
  }
  
  /** Wrapper for Position objects to allow relinking to a new Document. */
  //TODO: move this class to OpenDefinitionsDocument interface
  public static class WrappedPosition implements Position {
    private Position _wrapped;
    public WrappedPosition(Position w) { setWrapped(w); }
    public void setWrapped(Position w) { _wrapped = w; }
    public int getOffset() { return _wrapped.getOffset(); }
  }
  
  /** Factory method for created WrappedPositions. Stores the created Position instance
   *  so it can be linked to a different DefinitionsDocument later. */
  public Position createPosition(int offs) throws BadLocationException {
    WrappedPosition wp = new WrappedPosition(createUnwrappedPosition(offs));
    synchronized(_wrappedPosListLock) {
      if (_wrappedPosList == null) _wrappedPosList = new LinkedList<WeakReference<WrappedPosition>>(); 
      _wrappedPosList.add(new WeakReference<WrappedPosition>(wp));
    }
    return wp;
  }
  
  /** Remove all positions that have been garbage-collected from the list of positions, then return a weakly-linked
    * hashmap with positions and their current offsets.
    * @return list of weak references to all positions that have been created and that have not been garbage-collected yet.
    */
  public WeakHashMap<WrappedPosition, Integer> getWrappedPositionOffsets() {
    LinkedList<WeakReference<WrappedPosition>> newList = new LinkedList<WeakReference<WrappedPosition>>();
    synchronized(_wrappedPosListLock) {
      if (_wrappedPosList == null) { _wrappedPosList = new LinkedList<WeakReference<WrappedPosition>>(); }
      WeakHashMap<WrappedPosition, Integer> ret = new WeakHashMap<WrappedPosition, Integer>(_wrappedPosList.size());
      
      for (WeakReference<WrappedPosition> wr: _wrappedPosList) {
        if (wr.get() != null)  {
          // hasn't been garbage-collected yet
          newList.add(wr);
          ret.put(wr.get(), wr.get().getOffset());
        }
      }
      _wrappedPosList.clear();
      _wrappedPosList = newList;  
    return ret;
    }
  }
 
  /** Re-create the wrapped positions in the hashmap, update the wrapped position, and add them to the list.
    * @param whm weakly-linked hashmap of wrapped positions and their offsets
    */
  public void setWrappedPositionOffsets(WeakHashMap<WrappedPosition, Integer> whm) throws BadLocationException {
    synchronized(_wrappedPosListLock) {
      if (_wrappedPosList == null) { _wrappedPosList = new LinkedList<WeakReference<WrappedPosition>>(); }
      _wrappedPosList.clear();
      
      for(Map.Entry<WrappedPosition, Integer> entry: whm.entrySet()) {
        if (entry.getKey() != null) {
          // hasn't been garbage-collected yet
          WrappedPosition wp = entry.getKey();
          wp.setWrapped(createUnwrappedPosition(entry.getValue()));
          _wrappedPosList.add(new WeakReference<WrappedPosition>(wp));
        }
      }
    }
  }
  
  /** Appending any information for the reduced model from each undo command */
  private static class CommandUndoableEdit extends AbstractUndoableEdit {
    private final Runnable _undoCommand;
    private final Runnable _redoCommand;

    public CommandUndoableEdit(final Runnable undoCommand, final Runnable redoCommand) {
      _undoCommand = undoCommand;
      _redoCommand = redoCommand;
    }

    public void undo() throws CannotUndoException {
      super.undo();
      _undoCommand.run();
    }

    public void redo() throws CannotRedoException {
      super.redo();
      _redoCommand.run();
    }

    public boolean isSignificant() { return false; }
  }

  /**
   * Getter method for CompoundUndoManager
   * @return _undoManager
   */
  public CompoundUndoManager getUndoManager() { return _undoManager; }

  /** Resets the undo manager. */
  public void resetUndoManager() {
    _undoManager = new CompoundUndoManager(_notifier);
    _undoManager.setLimit(UNDO_LIMIT);
  }

  /** Public accessor for the next undo action. */
  public UndoableEdit getNextUndo() { return _undoManager.getNextUndo(); }

  /** Public accessor for the next undo action. */
  public UndoableEdit getNextRedo() { return _undoManager.getNextRedo(); }

  /** Informs this document's undo manager that the document has been saved. */
  public void documentSaved() { _undoManager.documentSaved(); }
  
  protected int startCompoundEdit() { return _undoManager.startCompoundEdit(); }
  
  protected void endCompoundEdit(int key) {
    _undoManager.endCompoundEdit(key);
  }
  
  //This method added for FrenchKeyBoardFix
  protected void endLastCompoundEdit() { _undoManager.endLastCompoundEdit(); }
   
  protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) {
    chng.addEdit(new CommandUndoableEdit(undoCommand, doCommand));    
  }
  
  
  /**
   * Is used to be able to call editToBeUndone and editToBeRedone since they
   * are protected methods in UndoManager
   */
  /*
  private class OurUndoManager extends UndoManager {
    private boolean _compoundEditState = false;
    private OurCompoundEdit _compoundEdit;

    public void startCompoundEdit() {
      if (_compoundEditState) {
        throw new IllegalStateException("Cannot start a compound edit while making a compound edit");
      }
      _compoundEditState = true;
      _compoundEdit = new OurCompoundEdit();
    }

    public void endCompoundEdit() {
      if (!_compoundEditState) {
        throw new IllegalStateException("Cannot end a compound edit while not making a compound edit");
      }
      _compoundEditState = false;
      _compoundEdit.end();
      super.addEdit(_compoundEdit);
    }

    public UndoableEdit getNextUndo() {
      return editToBeUndone();
    }

    public UndoableEdit getNextRedo() {
      return editToBeRedone();
    }

    public boolean addEdit(UndoableEdit e) {
      if (_compoundEditState) {
        return _compoundEdit.addEdit(e);
      }
      else {
        return super.addEdit(e);
      }
    }
  }


  public java.util.Vector getEdits() {
     return _undoManager._compoundEdit.getEdits();
  }

  private class OurCompoundEdit extends CompoundEdit {
     public java.util.Vector getEdits() {
        return edits;
     }
  }
  */
  
  /**
   * used to help track down memory leaks
   */
//  protected void finalize() throws Throwable{
//    System.out.println("destroying DefDocument for " + _odd);
//    super.finalize();
//  }
//  
//  private List<Pair<Option, OptionListener>> _optionListeners = new LinkedList<Option, OptionListener>>();
//
//  public void clearOptionListeners() {
//    for (Pair<Option, OptionListener> l: _optionListeners) {
//      DrJava.getConfig().removeOptionListener( l.getFirst(), l.getSecond());
//    }
//    _optionListeners.clear();
//  }
//  
//  public void addOptionListener(Option op, OptionListener l) {
//    DrJava.getConfig().addOptionListener(op, l);
//    _optionListeners.add(new Pair<Option, OptionListener>(op, l));
//  }
  
  /** This list of listeners to notify when we are finalized. */
  private List<FinalizationListener<DefinitionsDocument>> _finalizationListeners = 
    new LinkedList<FinalizationListener<DefinitionsDocument>>();
  
  /**
   * Registers a finalization listener with the specific instance of the ddoc
   * <p><b>NOTE:</b><i>This should only be used by test cases.  This is to ensure that
   * we don't spring memory leaks by allowing our unit tests to keep track of 
   * whether objects are being finalized (garbage collected)</i></p>
   * @param fl the listener to register
   */
  public void addFinalizationListener(FinalizationListener<DefinitionsDocument> fl) {
    synchronized(_finalizationListeners) { _finalizationListeners.add(fl); }
  }
  
  public List<FinalizationListener<DefinitionsDocument>> getFinalizationListeners() {
    return _finalizationListeners;
  }

  /** This is called when this method is GC'd.  Since this class implements
   *  edu.rice.cs.drjava.model.Finalizable, it must notify its listeners
   */
  protected void finalize() {
    FinalizationEvent<DefinitionsDocument> fe = new FinalizationEvent<DefinitionsDocument>(this);
    synchronized(_finalizationListeners) {
      for (FinalizationListener<DefinitionsDocument> fl: _finalizationListeners) {
        fl.finalized(fe);
      }
    }
  }
  
  public String toString() { return "ddoc for " + _odd; }
}
