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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.ProgressMonitor;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.DocumentEvent;
import java.util.Vector;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.io.*;

import java.io.File;

import edu.rice.cs.util.Pair;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionListener;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.Option;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.*;


/**
 * The model for the definitions pane.
 *
 * This implementation of <code>Document</code> contains a
 * "reduced model". The reduced model is automatically kept in sync
 * when this document is updated. Also, that synchronization is maintained
 * even across undo/redo -- this is done by making the undo/redo commands know
 * how to restore the reduced model state.
 *
 * The reduced model is not thread-safe, so it is essential that ONLY this
 * DefinitionsDocument call methods on it.  Any information from the reduced
 * model should be obtained through helper methods on DefinitionsDocument,
 * and ALL methods in DefinitionsDocument which reference the reduced model
 * (via the _reduced field) MUST be synchronized.  This prevents any thread
 * from seeing an inconsistent state in the middle of another thread's changes.
 *
 * @see BraceReduction
 * @see ReducedModelControl
 * @see ReducedModelComment
 * @see ReducedModelBrace
 *
 */
public class DefinitionsDocument extends AbstractDJDocument implements Finalizable<DefinitionsDocument> {
  
  List<DocumentClosedListener> _closedListeners = new LinkedList<DocumentClosedListener>();
  
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
      for (DocumentClosedListener l: _closedListeners) { l.close(); }
      _closedListeners = new LinkedList<DocumentClosedListener>();
    }
    
//    _closed = false;
  }
  
  // end debug code
  
  
  /** The maximum number of undos the model can remember */
  private static final int UNDO_LIMIT = 1000;
  /** Determines if tabs are removed on open and converted to spaces. */
  private static boolean _tabsRemoved = true;
  /** Determines if the document has been modified since the last save. */
  private boolean _modifiedSinceSave = false;
  /** Cached location, aides in determining line number. */
  private int _cachedLocation;
  /** Cached current line number. */
  private int _cachedLineNum;
  /** Cached location of previous line. */
  private int _cachedPrevLineLoc;
  /** Cached location of next line. */
  private int _cachedNextLineLoc;
  private boolean _classFileInSync;
  private File _classFile;

  /** This reference to the OpenDefinitionsDocument is needed so that the document iterator 
   * (the DefaultGlobalModel) can find the next ODD given a DD. */
  private OpenDefinitionsDocument _odd;
  
  private CompoundUndoManager _undoManager;
  
  /** Keeps track of the listeners to this model. */
  private final GlobalEventNotifier _notifier;
  
  /**
   * Convenience constructor for using a custom indenter.
   * @param indenter custom indenter class
   * @param notifier used by CompoundUndoManager to announce undoable edits
   */
  public DefinitionsDocument(Indenter indenter, GlobalEventNotifier notifier) {
    super(indenter);
    _notifier = notifier;
    _init();
    resetUndoManager();
  }

  /**
   * Main constructor.  This has an obnoxious dependency on
   * GlobalEventNotifier, which is passed through here only for a single
   * usage in CompoundUndoManager.  TODO: find a better way.
   * @param notifier used by CompoundUndoManager to announce undoable edits
   */
  public DefinitionsDocument(GlobalEventNotifier notifier) {
    super();
    _notifier = notifier;
    _init();
    resetUndoManager();
  }

  /**
   * Main constructor.  This has an obnoxious dependency on
   * GlobalEventNotifier, which is passed through here only for a single
   * usage in CompoundUndoManager.  TODO: find a better way.
   * @param notifier used by CompoundUndoManager to announce undoable edits
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
    _classFileInSync = false;
    _classFile = null;
    _cacheInUse = false;
  }
  

  /** This function is for use by the OpenDefinitionsDocument. This will lock the Document. */
  public void aquireWriteLock() {
    // throwErrorHuh();
    writeLock();
  }
  
  /**
   * this function is for use by the OpenDefinitionsDocument. This will release the lock to the Document.
   */
  public void releaseWriteLock() { writeUnlock(); }
  
  /** This function is for use by the OpenDefinitionsDocument. This will lock the Document.  */
  public void aquireReadLock() { readLock(); }
  
  /** This function is for use by the OpenDefinitionsDocument. This will release the lock to the Document. */
  public void releaseReadLock() { readUnlock(); }
   
  
  /**
   * sets the OpenDefinitionsDocument that holds this DefinitionsDocument
   * (the odd can only be set once)
   * @param odd the OpenDefinitionsDocument to set as this DD's holder
   */
  public void setOpenDefDoc(OpenDefinitionsDocument odd) { if (_odd == null) _odd = odd; }
  
  /**
   * @return the OpenDefinitonsDocument that is associated with this DefinitionsDocument
   */
  public OpenDefinitionsDocument getOpenDefDoc() {
    if (_odd == null)
      throw new IllegalStateException("The OpenDefinitionsDocument for this DefinitionsDocument has never been set");
    else return _odd;
  }
  
  protected void _styleChanged() {
    // throwErrorHuh();
    int length = getLength() - _currentLocation;
    //DrJava.consoleErr().println("Changed: " + _currentLocation + ", " + length);
    DocumentEvent evt = new DefaultDocumentEvent(_currentLocation,
                                                 length,
                                                 DocumentEvent.EventType.CHANGE);
    fireChangedUpdate(evt);
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
  /**
   * Returns the name of this file, or "(untitled)" if no file.
   */
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


  /**
   * Gets the package and class name of this OpenDefinitionsDocument
   * @return the qualified class name
   */
  public String getQualifiedClassName() throws ClassNameNotFoundException {
    return _getPackageQualifier() + getFirstTopLevelClassName();
  }

  /**
   * Gets fully qualified class name of the top level class enclosing
   * the given position.
   */
  public String getQualifiedClassName(int pos) throws ClassNameNotFoundException {
    return _getPackageQualifier() + getEnclosingTopLevelClassName(pos);
  }

  /**
   * Gets an appropriate prefix to fully qualify a class name.
   * Returns this class's package followed by a dot, or the empty
   * string if no package name is found.
   */
  protected String _getPackageQualifier() {
    String packageName = "";
    try { packageName = this.getPackageName(); }
    catch (InvalidPackageException e) { 
      /* Couldn't find package, pretend there's none; findbugs requires multi-line formatting of this clause */ 
    }
    if ((packageName != null) && (!packageName.equals(""))) { packageName = packageName + "."; }
    return packageName;
  }

  public void setClassFileInSync(boolean inSync) { _classFileInSync = inSync; }

  public boolean getClassFileInSync() { return _classFileInSync; }

  public void setCachedClassFile(File classFile) { _classFile = classFile; }

  public File getCachedClassFile() { return _classFile; }

  /**
   * Inserts a string of text into the document.
   * It turns out that this is not where we should do custom processing
   * of the insert; that is done in {@link #insertUpdate}.
   */
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
    
    // If _removeTabs is set to true, remove all tabs from str.
    // It is a current invariant of the tabification functionality that
    // the document contains no tabs, but we want to allow the user
    // to override this functionality.
    
    writeLock();
    try {
      if (_tabsRemoved) str = _removeTabs(str);
      
      if (!_modifiedSinceSave) {
        _modifiedSinceSave = true;
        _classFileInSync = false;
      }
      super.insertString(offset, str, a);
    }
    finally { writeUnlock(); }
  }
  
  
  /** Removes a block of text from the specified location. We don't update the reduced model here; that happens
   *  in {@link #removeUpdate}.
   */
  public void remove(int offset, int len) throws BadLocationException {
    
    writeLock();
    try {
      
      if (!_modifiedSinceSave) {
        _modifiedSinceSave = true;
        _classFileInSync = false;
      }
      super.remove(offset, len);
    }
    finally { writeUnlock(); }
  }

  /** Given a String, return a new String will all tabs converted to spaces.  Each tab is converted 
   *  to one space, since changing the number of characters within insertString screws things up.
   *  @param source the String to be converted.
   *  @return a String will all the tabs converted to spaces
   */
  String _removeTabs(final String source) {
    clearCache(); // Clear the helper method cache
    return source.replace('\t', ' ');
  }

//  String _removeTabs(String source) {
//    StringBuffer target = new StringBuffer();
//    for (int i = 0; i < source.length(); i++) {
//      char next = source.charAt(i);
//
//      if (next != '\t') {
//        target.append(source.charAt(i));
//      }
//      else {
//        // Replace tab with a number of
//        // spaces according to the value of _indent.
//        for (int j = 0; j < _indent; j++) {
//          target.append(' ');
//        }
//      }
//    }
//    return target.toString();
//  }

  /** Originally designed to allow undoManager to set the current document to be modified whenever an undo or 
   *  redo is performed. Now it actually does this. */
  public void setModifiedSinceSave() {
    writeLock();
    try {
    _modifiedSinceSave = _undoManager.isModified();
//    System.out.println("DefinitionsDocument: set modified? " + _modifiedSinceSave);
    }
    finally { writeUnlock(); }
  }
  
  /** Whenever this document has been saved, this method should be called so that it knows it's no longer in 
   *  a modified state.
   */
  public void resetModification() {
    writeLock();
    try {
      _modifiedSinceSave = false;
      _undoManager.documentSaved();
    }
    finally { writeUnlock(); }
  }
  
  /** Determines if the document has been modified since the last save.
   *  @return true if the document has been modified
   */
  public boolean isModifiedSinceSave() {
    readLock();
    try { return  _modifiedSinceSave; }
    finally { readUnlock(); }
  }
  
  /** Return the current column of the cursor position. Uses a 0 based index. */
  public int getCurrentCol() {
    // throwErrorHuh();
    int here = _currentLocation;
    int startOfLine = getLineStartPos(here);
    return here - startOfLine;
  }

  /** Return the current line of the cursor position.  Uses a 1-based index. */
  public int getCurrentLine() {
    // throwErrorHuh();
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


  /** This method returns the relative offset of line number from the previous location in the 
   *  document. */
  private int _getRelativeLine() {
    int count=0;
    int currLoc = _currentLocation;

    setCurrentLocation(_cachedLocation);

    if (_cachedLocation > currLoc) {
      // we moved backward
      int prevLineLoc = getLineStartPos( _cachedLocation );
      while (prevLineLoc > currLoc) {
        count--;
        prevLineLoc = getLineStartPos( prevLineLoc - 1 );
        // temporary performance optimization
        setCurrentLocation(prevLineLoc);
      }
    }

    else {
      // we moved forward
      int nextLineLoc = getLineEndPos( _cachedLocation );
      while (nextLineLoc < currLoc) {
        count++;
        nextLineLoc = getLineEndPos( nextLineLoc + 1 );
        // temporary performance optimization
        setCurrentLocation(nextLineLoc);
      }
    }
    setCurrentLocation(currLoc);
    return count;
  }

  /**
   * Returns the offset corresponding to the first character of the given line number,
   *  or -1 if the lineNum is not found.
   * @param lineNum the line number for which to calculate the offset.
   * @return the offset of the first character in the given line number
   */
  public int getOffset(int lineNum) {
    
    try {
      if (lineNum < 0) return -1;
      
      String defsText = getText(0, getLength());
      int curLine = 1;
      int offset = 0; // offset is number of chars from beginning of file
      
      // offset is always pointing to the first character in a line
      // at the top of the loop
      while (offset < defsText.length()) {
        
        if (curLine==lineNum) return offset;
        
        int nextNewline = defsText.indexOf('\n', offset);
        if (nextNewline == -1) return -1; // end of the document, and couldn't find the supplied lineNum
          
        curLine++;
        offset = nextNewline + 1;
      }
      return -1;
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }

  

  /** Returns true iff tabs are to removed on text insertion. */
  public boolean tabsRemoved() { return _tabsRemoved; }

 
  /** Comments out all lines between selStart and selEnd, inclusive. The current cursor position is maintained 
   *  after the operation.
   *  @param selStart the document offset for the start of the selection
   *  @param selEnd the document offset for the end of the selection
   */
  public void commentLines(int selStart, int selEnd) {
    
      //int key = _undoManager.startCompoundEdit();  //Uncommented in regards to the FrenchKeyBoardFix
      if (selStart == selEnd) {
        writeLock();
        try {
          
          synchronized(_reduced) {
            Position oldCurrentPosition = createPosition(_currentLocation);
            _commentLine();
            //int caretPos = getCaretPosition();
            //_doc().setCurrentLocation(caretPos);
          }
        }
        catch (BadLocationException e) { throw new UnexpectedException(e); }
        finally { writeUnlock(); }
      }
      else _commentBlock(selStart, selEnd);
        
      _undoManager.endLastCompoundEdit();  //Changed from endCompoundEdit(key) for FrenchKeyBoardFix
  }
 

  /** Comments out the lines between and including the lines containing points start and end, using wing 
   *  comments -- "// ".
   * 
   *  @param start Position in document to start commenting from
   *  @param end Position in document to end commenting at
   */
  private void _commentBlock(final int start, final int end) {
    
    writeLock();
    try {
      // Keep marker at the end. This Position will be the
      // correct endpoint no matter how we change the doc
      // doing the indentLine calls.
      final Position endPos = this.createPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = start;
      synchronized(_reduced) {
        while (walker < endPos.getOffset()) {
          setCurrentLocation(walker);
          // Keep pointer to walker position that will stay current
          // regardless of how commentLine changes things
          Position walkerPos = this.createPosition(walker);
          // Comment out current line
          _commentLine();  // must be atomic
          
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
    finally { writeUnlock(); }
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
  public void uncommentLines(int selStart, int selEnd) {
 
    //int key = _undoManager.startCompoundEdit(); //commented out for FrenchKeyBoardFix
    if (selStart == selEnd) {
      writeLock();
      try {
        synchronized(_reduced) {
          Position oldCurrentPosition = createPosition(_currentLocation);
          _uncommentLine();
          //int caretPos = getCaretPosition();
          //_doc().setCurrentLocation(caretPos);
          setCurrentLocation(oldCurrentPosition.getOffset());
        }
      }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
      finally { writeUnlock(); }
    }
    else _uncommentBlock(selStart, selEnd);
    //_undoManager.endCompoundEdit(key); //Commented out for FrenchKeyBoardFix, Replaced with endLastCompoundEdit();
    _undoManager.endLastCompoundEdit();
  }

  /** Uncomments all lines between and including the lines containing
   *  points start and end.  
   * @param start Position in document to start commenting from
   * @param end Position in document to end commenting at
   */
  private void _uncommentBlock(final int start, final int end) {
    writeLock();
    try {
      // Keep marker at the end. This Position will be the correct endpoint no matter how we change the doc
      // doing the indentLine calls.
      final Position endPos = this.createPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = start;
      synchronized(_reduced) {
        while (walker < endPos.getOffset()) {
          setCurrentLocation(walker);
          // Keep pointer to walker position that will stay current
          // regardless of how commentLine changes things
          Position walkerPos = this.createPosition(walker);
          // uncomment current line
          _uncommentLine();
          
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
    finally { writeUnlock(); }
  }

  /** Uncomments a single line.  This simply looks for a leading "//".
   *  Also indents the line, once the comments have been removed.
   *  @pre theads hold this.writeLock() and _reduced lock
   */
  private void _uncommentLine() throws BadLocationException {
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
        break;
      }
    }
    
    // If a wing comment wasn't found, or if the wings aren't the first
    // non-whitespace characters on the line, do nothing.
    if (pos >= 0 && goodWing) {
      // Otherwise, remove the wings and indent.
      remove(lineStart + pos, 2);
      _indentLine(Indenter.OTHER);
    }
  }

//  /** Indents a line in accordance with the rules that DrJava has set up. This is the old version, 
//   *  which has been replaced by the indent rule decision tree.
//   */
//  private void _indentLine() {
//    try {
//      // moves us to the end of the line
//      move(_reduced.getDistToNextNewline());
//      IndentInfo ii = _reduced.getIndentInformation();
//      String braceType = ii.braceType;
//      int distToNewline = ii.distToNewline;
//      int distToBrace = ii.distToBrace;
//      int distToPrevNewline = ii.distToPrevNewline;
//      int tab = 0;
//      boolean isSecondLine = false;
//      if (distToNewline == -1) {
//        distToNewline = _currentLocation;
//        isSecondLine = true;
//      }
//      if (distToPrevNewline == -1)              //only on the first line
//        tab = 0;
//      //takes care of the second line
//      else if (this._currentLocation - distToPrevNewline < 2)
//        tab = 0;
//      else if (distToBrace == -1)
//        tab = _indentSpecialCases(0, distToPrevNewline);
//      else if (braceType.equals("("))
//        tab = distToNewline - distToBrace + 1;
//      else if (braceType.equals("{")) {
//        tab = getWhiteSpaceBetween(distToNewline, distToBrace) + _indent;
//        tab = _indentSpecialCases(tab, distToPrevNewline);
//      }
//      else if (braceType.equals("["))
//        tab = distToNewline - distToBrace + 1;
//      tab(tab, distToPrevNewline);
//    } catch (BadLocationException e) {
//      throw  new UnexpectedException(e);
//    }
//  }

//  /** Deals with the special cases. If the first character after the previous \n is a } then -2
//   *  Replaced by indent rule decision tree.
//   *  @exception BadLocationException
//   */
//  private int _indentSpecialCases(int tab, int distToPrevNewline) throws BadLocationException {
//    //not a special case.
//    if (distToPrevNewline == -1)
//      return  tab;
//    //setup
//    int start = _reduced.getDistToPreviousNewline(distToPrevNewline + 1);
//    if (start == -1)
//      start = 0;
//    else
//      start = _currentLocation - start;
//    String text = this.getText(start, _currentLocation - start);
//    //case of  }
//    int length = text.length();
//    int k = length - distToPrevNewline;
//    while (k < length && text.charAt(k) == ' ')
//      k++;
//    if (k < length && text.charAt(k) == '}')
//      tab -= _indent;
//    // if no matching { then let offset be 0.
//    if (tab < 0)
//      tab = 0;
//    //non-normal endings
//    int i = length - distToPrevNewline - 2;
//    int distanceMoved = distToPrevNewline + 2;
//    move(-distToPrevNewline - 2);               //assumed: we are at end of a line.
//    while (i >= 0 && _isCommentedOrSpace(i, text)) {
//      i--;
//      if (i > 0) {              //gaurentees you don't move into document Start.
//        distanceMoved++;
//        move(-1);
//      }
//    }
//    move(distanceMoved);        //move the document bac.
//    if (i >= 0 && !(_normEndings.contains(text.substring(i, i + 1)))) {
//      int j = 0;
//      while ((j < length) && (text.charAt(j) == ' '))
//        j++;
//      if ((k < length) && (text.charAt(k) == '{')) {
//        if ((j < length) && (text.charAt(j) == '{'))
//          tab = j + _indent;
//        else
//          tab = j;
//      }
//      else
//        tab = j + _indent;
//    }
//    //return tab
//    return  tab;
//  }

//  /** Determines if current token is part of a comment OR text.charAt(i) in the given text argument is a space.
//   *  @param i the index to look at for the space in text
//   *  @param text a block of text
//   *  @return true if the conditions are met
//   * doesn't seem to be used*/
//  private synchronized boolean _isCommentedOrSpace(int i, String text) {
//    ReducedToken rt = _reduced.currentToken();
//    String type = rt.getType();
//    return  (rt.isCommented() || type.equals("//") || type.equals("")
//        || (text.charAt(i) == ' '));
//  }


//  /** The function that handles what happens when a tab key is pressed. It is given the size of the leading 
//   *  whitespace and based on the current indent information, either shrinks or expands that whitespace.
//   *  @param tab number of indents, i.e., level of nesting
//   *  @param distToPrevNewline distance to end of previous line
//   *  @exception BadLocationException
//   */
//  void tab(int tab, int distToPrevNewline) throws BadLocationException {
//    if (distToPrevNewline == -1) distToPrevNewline = _currentLocation;
//    int currentTab = getWhiteSpaceBetween(distToPrevNewline, 0);
//    int dist = tab - currentTab;
//    
//    if (dist == 0) return;
//    if (dist > 0) {
//      String spaces = "";
//      for (int i = 0; i < dist; i++) { spaces = spaces + " "; }
//      insertString(_currentLocation - distToPrevNewline, spaces, null);
//    }
//    else  remove(_currentLocation - distToPrevNewline, currentTab - tab);
//  }

  /** Goes to a particular line in the document. */
  public void gotoLine(int line) {

    int dist;
    if (line < 0) return;
    int actualLine =1;
    
    readLock();
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
    finally { readUnlock(); }
  }

  /**
   * Gets the name of the package this source file claims it's in (with the package keyword). It does this by 
   * minimally parsing the source file to find the package statement.
   * @return The name of package declared for this source file OR the empty string if there is no package 
   *         statement (and thus the source file is in the empty package).
   * @exception InvalidPackageException if there is some sort of a <TT>package</TT> statement but the defined 
   *            package does not match or exist.
   */
  public String getPackageName() throws InvalidPackageException {
    // throwErrorHuh();
    // Where we'll build up the package name we find
    StringBuffer buf = new StringBuffer();
    int oldLocation;
    
    readLock();
    synchronized(_reduced) {
      oldLocation = _currentLocation;
      try {
        int firstNormalLocation;
        
        setCurrentLocation(0);
        final int docLength = getLength();
        final String text = getText(0, docLength);
        // The location of the first non-whitespace character that is not inside a string or comment.
        firstNormalLocation = 0;
        while (firstNormalLocation < docLength) {
          setCurrentLocation(firstNormalLocation);
          
          if (_reduced.currentToken().getHighlightState() == HighlightStatus.NORMAL) {
            // OK, it's normal -- so if it's not whitespace, we found the spot
            char curChar = text.charAt(firstNormalLocation);
            if (! Character.isWhitespace(curChar)) break;
          }
          
          firstNormalLocation++;
        }

        // Now there are two possibilities: firstNormalLocation is at
        // the first spot of a non-whitespace character that's NORMAL,
        // or it's at the end of the document.
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
        
        // Now we have semicolon location. We'll gather text in between one
        // character at a time for simplicity. It's inefficient (I think?)
        // but it's easy, and there shouldn't be much text between
        // "package" and ";" anyhow.
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
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      finally {
        setCurrentLocation(0);  // Why?
        setCurrentLocation(oldLocation);
        readUnlock();  // _reduced lock will be released in the next instruction
      }
    }
  }

  /** Returns the name of the class or interface enclosing the caret position at the top level.
   * @return Name of enclosing class or interface
   * @throws ClassNameNotFoundException if no enclosing class found
   */
  public String getEnclosingTopLevelClassName(int pos) throws ClassNameNotFoundException {
    readLock();
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
        readUnlock();
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
    // Where we'll build up the package name we find
    int oldLocation = _currentLocation;
    
    readLock();
    synchronized(_reduced) {
      try {
        setCurrentLocation(startPos);
        final int textLength = endPos - startPos;
        final String text = getText(startPos, textLength);
        
        boolean done;
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
          throw new ClassNameNotFoundException("No top level class name found");
        }
        //if we make it here we have a valid index
        
        //first find index of first non whitespace (from the index in document)
        index = getFirstNonWSCharPos(startPos + index) - startPos;
        if (index == -1) throw new ClassNameNotFoundException("No top level class name found");
        
        int endIndex = textLength; //just in case no whitespace at end of file
        
        //find index of next delimiter or whitespace
        char c;
        for (int i = index; i < textLength; i++) {
          c = text.charAt(i);
          if (!Character.isJavaIdentifierPart(c)) {
            endIndex = i;
            break;
          }
        }
        
        setCurrentLocation(oldLocation);
        return text.substring(index,endIndex);
      }
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      finally { 
        setCurrentLocation(oldLocation);
        readUnlock();
      }
    }
  }

  /** Finds the first occurrence of the keyword within the text that is not enclosed within a brace or comment 
   *  and is followed by whitespace.
   *  @param keyword the keyword for which to search
   *  @param text in which to search
   *  @param textOffset Offset at which the text occurs in the document
   *  @return index of the keyword, or -1 if the keyword is not found or not followed by whitespace
   */
  private int _findKeywordAtToplevel(String keyword, String text, int textOffset) {
    
    readLock();
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
                if (!posNotInBlock(index)) index = -1; //in a paren phrase, gone too far
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
        return index;
      }
      finally { readUnlock(); }
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
    // throwErrorHuh();
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
  
  /**
   * This list of listeners to notify when we are finalized
   */
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
