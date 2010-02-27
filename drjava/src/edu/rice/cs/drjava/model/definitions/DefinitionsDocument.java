/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions;

import java.awt.EventQueue;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.DocumentEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import koala.dynamicjava.parser.impl.Parser;
import koala.dynamicjava.parser.impl.ParseException;
import koala.dynamicjava.parser.impl.TokenMgrError;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.OpenDefinitionsDocument;
import edu.rice.cs.drjava.model.*;

import static edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates.*;

/** The document model for the definitions pane; it contains a reduced model since it extends AbstractDJDocument. 
  * @see AbstractDJDocument
  */
public class DefinitionsDocument extends AbstractDJDocument implements Finalizable<DefinitionsDocument> {
  
  public static final Log _log = new Log("GlobalModel.txt", false);
  private static final int NO_COMMENT_OFFSET = 0;
  private static final int WING_COMMENT_OFFSET = 2;
  
  private final List<DocumentClosedListener> _closedListeners = new LinkedList<DocumentClosedListener>();
  
  public void addDocumentClosedListener(DocumentClosedListener l) { 
    synchronized(_closedListeners) { _closedListeners.add(l); }
  }
  
  public void removeDocumentClosedListener(DocumentClosedListener l) { 
    synchronized(_closedListeners) { _closedListeners.remove(l); }
  }
  
  // begin debug code
  
  /** Closes this DefinitionsDocument (but not the enclosing OpenDefinitionsDocument).  Called when this is kicked out
    * of the document cache so that this can be GC'd. */
  public void close() {
    _removeIndenter();
    synchronized(_closedListeners) {
      for (DocumentClosedListener l: _closedListeners)  { l.close(); }
      _closedListeners.clear();
    }
  }
  // end debug code
  
  /** The maximum number of undos the model can remember */
  private static final int UNDO_LIMIT = 1000;
  /** Specifies if tabs are removed on open and converted to spaces. */
  private static boolean _tabsRemoved = true;
  
  /** Specifies if the document has been modified since the last save.  Modified under write lock. */
  private volatile boolean _isModifiedSinceSave = false;
  
  /** This reference to the OpenDefinitionsDocument is needed so that the document iterator 
    * (the DefaultGlobalModel) can find the next ODD given a DD. */
  private volatile OpenDefinitionsDocument _odd;
  
  private volatile CompoundUndoManager _undoManager;
  
  /** Keeps track of the listeners to this model. */
  private final GlobalEventNotifier _notifier;
  
  /** Uses an updated version of the DefaultEditorKit */
  private final DefinitionsEditorKit _editor;
  
  /* Relying on the following definition in AbstractDJDocument.  It must be placed there to be initialized before use!
   protected static final Object _wrappedPosListLock = new Object();
   */
  
  /** List with weak references to positions. */
  private volatile LinkedList<WeakReference<WrappedPosition>> _wrappedPosList;
  
  /** Convenience constructor for using a custom indenter.
    * @param indenter custom indenter class
    * @param notifier used by CompoundUndoManager to announce undoable edits
    */
  public DefinitionsDocument(Indenter indenter, GlobalEventNotifier notifier) {
    super(indenter);
    _notifier = notifier;
    _editor = new DefinitionsEditorKit(notifier);
    resetUndoManager();
  }
  
  /** Main constructor.  This has an obnoxious dependency on GlobalEventNotifier, which is passed through here only 
    * for a single usage in CompoundUndoManager.  TODO: find a better way.
    * @param notifier used by CompoundUndoManager to announce undoable edits
    */
  public DefinitionsDocument(GlobalEventNotifier notifier) {
    super();
    _notifier = notifier;
    _editor = new DefinitionsEditorKit(notifier);
    resetUndoManager();
  }
  
  /** Main constructor.  This has an obnoxious dependency on GlobalEventNotifier, which is passed through here only 
    * for a single usage in CompoundUndoManager.  TODO: find a better way.
    * @param notifier used by CompoundUndoManager to announce undoable edits
    */
  public DefinitionsDocument(GlobalEventNotifier notifier, CompoundUndoManager undoManager) {
    super();
    _notifier = notifier;
    _editor = new DefinitionsEditorKit(notifier);
    _undoManager = undoManager;
  }
  
  /** Returns the document's editor */
  public DefinitionsEditorKit getEditor(){
    return _editor;
  }
  
  /** Returns a new indenter. */
  protected Indenter makeNewIndenter(int indentLevel) { return new Indenter(indentLevel); }
  
  /** Sets the OpenDefinitionsDocument that holds this DefinitionsDocument (the odd can only be set once).
    * @param odd the OpenDefinitionsDocument to set as this DD's holder
    */
  public void setOpenDefDoc(OpenDefinitionsDocument odd) { if (_odd == null) _odd = odd; }
  
  /** @return the OpenDefinitonsDocument that is associated with this DefinitionsDocument. */
  public OpenDefinitionsDocument getOpenDefDoc() {
    if (_odd == null)
      throw new IllegalStateException("The OpenDefinitionsDocument for this DefinitionsDocument has never been set");
    else return _odd;
  }
  
  /** Recolors the rest of the document based on the change that triggered this call. */
  protected void _styleChanged() {    
    
      int length = getLength() - _currentLocation;
      
      //DrJava.consoleErr().println("Changed: " + _currentLocation + ", " + length);
      DocumentEvent evt = new DefaultDocumentEvent(_currentLocation, length, DocumentEvent.EventType.CHANGE);
      fireChangedUpdate(evt);
  } 
   
//  /** Returns whether this document is currently untitled
//    * (indicating whether it has a file yet or not).
//    * @return true if the document is untitled and has no file
//    */
//  public boolean isUntitled() { return (_file == null); }
  
//  /** Returns the file for this document.  If the document
//   * is untitled and has no file, it throws an IllegalStateException.
//   * @return the file for this document
//   * @throws IllegalStateException if file has not been set
//   * @throws FileMovedException if file has been moved or deleted from its previous location
//   */
//  public File getFilex() throws IllegalStateException , FileMovedException {
//    if (_file == null) {
//      throw new IllegalStateException("This document does not yet have a file.");
//    }
//    //does the file actually exist?
//    if (_file.exists()) return _file;
//    else throw new FileMovedException(_file, "This document's file has been moved or deleted.");
//  }
//
//  /** Returns the name of this file, or "(untitled)" if no file. */
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
    * @return the qualified main class/interface name
    */
  public String getQualifiedClassName() throws ClassNameNotFoundException {
    return getPackageQualifier() + getMainClassName();
  }
  
  /** Gets fully qualified class name of the top level class enclosing the given position. */
  public String getQualifiedClassName(int pos) throws ClassNameNotFoundException {
    return getPackageQualifier() + getEnclosingTopLevelClassName(pos);
  }
  
  /** Gets an appropriate prefix to fully qualify a class name. Returns this class's package followed by a dot, or the
    * empty string if no package name is found.
    */
  private String getPackageQualifier() {
    String packageName = getPackageName();
    if ((packageName != null) && (! packageName.equals(""))) { packageName = packageName + "."; }
    return packageName;
  }
  
  /** Inserts a string of text into the document.  This is not where we do custom processing of the insert; that is
    * done in {@link #insertUpdate}.  If _removeTabs is set to true, remove all tabs from str. It is a current invariant
    * of the tabification functionality that the document contains no tabs, but we want to allow the user to override 
    * this functionality.
    */
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
    if (_tabsRemoved) str = _removeTabs(str);
    _setModifiedSinceSave();
    super.insertString(offset, str, a);
  }
    
  /** Removes a block of text from the specified location. We don't update the reduced model here; that happens
    * in {@link #removeUpdate}.
    */
  public void remove(int offset, int len) throws BadLocationException {
    
    if (len == 0) return;
    _setModifiedSinceSave();
    super.remove(offset, len);
  }
  
  /** Given a String, return a new String will all tabs converted to spaces.  Each tab is converted 
    * to one space, since changing the number of characters within insertString screws things up.
    * @param source the String to be converted.
    * @return a String will all the tabs converted to spaces
    */
  static String _removeTabs(final String source) { return source.replace('\t', ' '); }
  
  /** Resets the modification state of this document to be consistent with state of _undoManager.  Called whenever
    * an undo or redo is performed. */
  public void updateModifiedSinceSave() {
    _isModifiedSinceSave = _undoManager.isModified();
    if (_odd != null) _odd.documentReset();
  }
  
  /** Sets the modification state of this document to true and updates the state of the associated _odd. 
    * Assumes that write lock is already held. 
    */
  private void _setModifiedSinceSave() {
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    if (! _isModifiedSinceSave) {
      _isModifiedSinceSave = true;
      if (_odd != null) _odd.documentModified();  // null test required for some unit tests
    }    
  }
  
  /** Resets the modification state of this document.  Used after a document has been saved or reverted. */
  public void resetModification() {
    _isModifiedSinceSave = false;
    _undoManager.documentSaved();
    if (_odd != null) _odd.documentReset();  // null test required for some unit tests
  }
  
  /** Determines if the document has been modified since the last save.
    * @return true if the document has been modified
    */
  public boolean isModifiedSinceSave() { return  _isModifiedSinceSave; }
  
  /** Return the current column of the cursor position. Uses a 0 based index. */
  public int getCurrentCol() {
      Element root = getDefaultRootElement();
      int line = root.getElementIndex(_currentLocation);
      return _currentLocation - root.getElement(line).getStartOffset();
  }
  
  /** Return the current line of the cursor position.  Uses a 1-based index. */
  public int getCurrentLine() { return getLineOfOffset(_currentLocation); }
  
  /** Return the line number corresponding to offset.  Uses a 1-based index. */
  public int getLineOfOffset(int offset) { return getDefaultRootElement().getElementIndex(offset) + 1; }
  
  /** Returns the offset corresponding to the first character of the given line number, or -1 if the lineNum is not
    * found.  Line number counting begins with 1 not 0.  Assumes read lock is already held.
    * @param lineNum the line number for which to calculate the offset.
    * @return the offset of the first character in the given line number
    */
  public int _getOffset(int lineNum) {
    if (lineNum <= 0) return -1;
    if (lineNum == 1) return 0;
    
//    synchronized(_reduced) {
      final int origPos = getCurrentLocation();
      try {
        final int docLen = getLength();
        
        setCurrentLocation(0); // _currentLocation is candidate offset to return
        int i;
        for (i = 1; (i < lineNum) && (_currentLocation < docLen); i++) {
          int dist = _reduced.getDistToNextNewline();     // or end of doc
          if (_currentLocation + dist < docLen) dist++;  // skip newline
          move(dist);  // updates _currentLocation to beginning of line # (i + 1)
        }
        if (i == lineNum) return _currentLocation;
        else return -1;
      }
      finally { setCurrentLocation(origPos); }
//    }
  }

  
  /** Returns true iff tabs are to removed on text insertion. */
  public boolean tabsRemoved() { return _tabsRemoved; }
  
  /** Comments out all lines between selStart and selEnd, inclusive. The cursor position is unchanged by the operation.
    * @param selStart the document offset for the start of the selection
    * @param selEnd the document offset for the end of the selection
    */
  public int commentLines(int selStart, int selEnd) {
    
    //int key = _undoManager.startCompoundEdit();  //Uncommented in regards to the FrenchKeyBoardFix
    int toReturn = selEnd;
    if (selStart == selEnd) {
      setCurrentLocation(_getLineStartPos(selStart));
//          Position oldCurrentPosition = createUnwrappedPosition(_currentLocation);
      _commentLine();
      toReturn += WING_COMMENT_OFFSET;
    }
    else toReturn = commentBlock(selStart, selEnd);   
    _undoManager.endLastCompoundEdit();  //Changed from endCompoundEdit(key) for FrenchKeyBoardFix
    return toReturn;
  }
  
  
  /** Comments out the lines between start and end inclusive, using wing comments -- "// ".
    * @param start Position in document to start commenting from
    * @param end Position in document to end commenting at
    */
  private int commentBlock(final int start, final int end) {
    int afterCommentEnd = end;
    try {
      // Keep marker at the end. This Position will be the correct endpoint no matter how we change the doc doing the
      // indentLine calls.
      final Position endPos = this.createUnwrappedPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = _getLineStartPos(start);
      while (walker < endPos.getOffset()) {
        setCurrentLocation(walker);  // Update cursor
        
        _commentLine();              // Comment out current line; must be atomic
        afterCommentEnd += WING_COMMENT_OFFSET;
        
        walker = walker + 2;         // Skip over inserted slashes; getDistToNewline(walker) = 0 if not advanced
        setCurrentLocation(walker);  // reset currentLocation to position past newline
        
        // Adding 1 makes us point to the first character AFTER the next newline.
        walker += _reduced.getDistToNextNewline() + 1;
      }
    } 
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    return afterCommentEnd;
  }
  
  /** Comments out a single line with wing comments -- "// ".  Assumes that _currentLocation is the beginning of the
    * line to be commented out.  Only runs in event thread. */
  private void _commentLine() {
    // Insert "// " at the beginning of the line.
    // Using null for AttributeSet follows convention in this class.
    try { insertString(_currentLocation, "//", null); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Uncomments all lines between selStart and selEnd, inclusive.  The cursor position is unchanged by the operation.
    * @param selStart the document offset for the start of the selection
    * @param selEnd the document offset for the end of the selection
    */
  public int uncommentLines(int selStart, int selEnd) {
    
    //int key = _undoManager.startCompoundEdit(); //commented out for FrenchKeyBoardFix
    int toReturn = selEnd;
    if (selStart == selEnd) {
      try {
        setCurrentLocation(_getLineStartPos(selStart));
        _uncommentLine();  // accesses _reduced
        toReturn -= WING_COMMENT_OFFSET;
      }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
    }
    else  toReturn = uncommentBlock(selStart, selEnd);
    //_undoManager.endCompoundEdit(key); //Commented out for FrenchKeyBoardFix, Replaced with endLastCompoundEdit();
    _undoManager.endLastCompoundEdit();
    return toReturn;
  }
  
  /** Uncomments all lines between start and end inclusive. 
    * @param start Position in document to start commenting from
    * @param end Position in document to end commenting at
    */
  private int uncommentBlock(final int start, final int end) {
    int afterUncommentEnd = end;
    try {
      // Keep marker at the end. This Position will be the correct endpoint no matter how we change the doc
      // doing the indentLine calls.
      final Position endPos = this.createUnwrappedPosition(end);
      // Iterate, line by line, until we get to/past the end
      
      int walker = _getLineStartPos(start);
//      Utilities.show("Initial walker pos = " + walker);
      while (walker < endPos.getOffset()) {
        setCurrentLocation(walker);           // Move cursor to walker position
        int diff = _uncommentLine();          // Uncomment current line, accessing the reduced model
        afterUncommentEnd -= diff;            // Update afterUncommentEnd
        walker = _getLineEndPos(walker) + 1;   // Update walker pos to point to beginning of next line
//        Utilities.show("Updated value of walker = " + walker);
      }           
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    return afterUncommentEnd;
  }
  
  /** Uncomments a single line.  This simply looks for a leading "//".  Assumes that cursor is already located at the
    * beginning of line.  Also assumes that write lock and _reduced lock are already held.
    */
  private int _uncommentLine() throws BadLocationException {
    // Look for "//" at the beginning of the line, and remove it.
//    Utilities.show("Uncomment line at location " + _currentLocation);
//    Utilities.show("Preceding char = '" + getText().charAt(_currentLocation - 1) + "'");
//    Utilities.show("Line = \n" + getText(_currentLocation, getLineEndPos(_currentLocation) - _currentLocation + 1));
    int pos1 = getText().indexOf("//", _currentLocation);  // TODO: get text of current line instead of whole document
    if (pos1 < 0) return NO_COMMENT_OFFSET;
    int pos2 = getFirstNonWSCharPos(_currentLocation, true);
//    Utilities.show("Pos1 = " + pos1 + " Pos2 = " + pos2);
    if (pos1 != pos2) return NO_COMMENT_OFFSET;
    
    remove(pos1, 2);
    return WING_COMMENT_OFFSET;
  }

  /** Goes to a particular line in the document. */
  public void gotoLine(int line) {
    
    int dist;
    if (line < 0) return;
    int actualLine = 1;
    
    int len = getLength();
      setCurrentLocation(0);
      for (int i = 1; (i < line) && (_currentLocation < len); i++) {
        dist = _reduced.getDistToNextNewline();
        if (_currentLocation + dist < len) dist++;
        actualLine++;
        move(dist);  // updates _currentLocation
      }
  }  
  
  /** Assumes that read lock is already held. */
  private int _findNextOpenCurly(String text, int pos) throws BadLocationException {
    
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    int i;
    int reducedPos = pos;
    
//    synchronized(_reduced) {
      final int origLocation = _currentLocation;
      // Move reduced model to location pos
      _reduced.move(pos - origLocation);  // reduced model points to pos == reducedPos
      
      // Walk forward from specificed position
      i = text.indexOf('{', reducedPos);
      while (i >- 1) {
        // Move reduced model to walker's location
        _reduced.move(i - reducedPos);  // reduced model points to i
        reducedPos = i;                 // reduced model points to reducedPos
        
        // Check if matching keyword should be ignored because it is within a comment, or quotes
        ReducedModelState state = _reduced.getStateAtCurrent();
        if (!state.equals(FREE) || _isStartOfComment(text, i)
              || ((i > 0) && _isStartOfComment(text, i - 1))) {
          i = text.indexOf('{', reducedPos+1);
          continue;  // ignore matching brace
        }
        else {
          break; // found our brace
        }        
      }  
      _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
//    } // end synchronized
    
    if (i == -1) reducedPos = -1; // No matching brace was found
    return reducedPos;  
  }
  
  /** Assuming that text is a document prefix including offset pos, finds the index of the keyword kw
    * searching back from pos.
    */
  public int _findPrevKeyword(String text, String kw, int pos) throws BadLocationException {
    
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    int i;
    int reducedPos = pos;
    
//    synchronized(_reduced) {
      final int origLocation = _currentLocation;
      // Move reduced model to location pos
      _reduced.move(pos - origLocation);  // reduced model points to pos == reducedPos
      
      // Walk backwards from specificed position
      i = text.lastIndexOf(kw, reducedPos);
      while (i >- 1) {
        // Check that this is the beginning of a word
        if (i > 0) {
          if (Character.isJavaIdentifierPart(text.charAt(i-1))) {
            // not begining
            i = text.lastIndexOf(kw, i - 1);
            continue;  // ignore matching keyword 
          }
        }
        // Check that this not just the beginning of a longer word
        if (i + kw.length() < text.length()) {
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
        if (!state.equals(FREE) || _isStartOfComment(text, i) || ((i > 0) && _isStartOfComment(text, i - 1))) {
          i = text.lastIndexOf(kw, reducedPos-1);
          continue;  // ignore matching keyword 
        }
        else break; // found our keyword   
      }  // end synchronized/
      
      _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
//    }
    
    if (i == -1) reducedPos = -1; // No matching keyword was found
    return reducedPos;  
  }
  
//  public static boolean log = true;
  
  /** Searches backwards to find the name of the enclosing named class or interface. NB: ignores comments.
    * WARNING: In long source files and when contained in anonymous inner classes, this function might take a LONG time.
    * @param pos Position to start from
    * @param qual true to find the fully qualified class name
    * @return name of the enclosing named class or interface
    */
  public String getEnclosingClassName(int pos, boolean qual) throws BadLocationException, ClassNameNotFoundException {
      return _getEnclosingClassName(pos, qual);
  }
  
  /** Searches backwards to find the name of the enclosing named class or interface. NB: ignores comments. Only runs in
    * event thread.
    * WARNING: In long source files and when contained in anonymous inner classes, this function might take a LONG time.
    * @param pos Position to start from
    * @param qual true to find the fully qualified class name
    * @return name of the enclosing named class or interface
    */
  public String _getEnclosingClassName(final int pos, final boolean qual) throws BadLocationException, 
    ClassNameNotFoundException {    
    
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    // Check cache
    final Query key = new Query.EnclosingClassName(pos, qual);
    final String cached = (String) _checkCache(key);
    if (cached != null) return cached;
    
    final char[] delims = {'{','}','(',')','[',']','+','-','/','*',';',':','=','!','@','#','$','%','^','~','\\','"','`','|'};
    String name = "";
    
    final String text = getText(0, pos);  
    
    int curPos = pos;
    
    do {
//        if (text.charAt(curPos) != '{' || text.charAt(curPos) != '}') ++curPos;
      
//        if (oldLog) System.err.println("curPos=" + curPos + " `" +
//                                       text.substring(Math.max(0,curPos-10), Math.min(text.length(), curPos+1)) + "`");
      
      curPos = findPrevEnclosingBrace(curPos, '{', '}');
      if (curPos == -1) { break; }
      int classPos = _findPrevKeyword(text, "class", curPos);
      int interPos = _findPrevKeyword(text, "interface", curPos);
      int otherPos = findPrevDelimiter(curPos, delims);
      int newPos = -1;
      // see if there's a ) closer by
      int closeParenPos = _findPrevNonWSCharPos(curPos);
      if (closeParenPos != -1 && text.charAt(closeParenPos) == ')') {
        // yes, find the matching (
        int openParenPos = findPrevEnclosingBrace(closeParenPos, '(', ')');
        if (openParenPos != -1 && text.charAt(openParenPos) == '(') {
          // this might be an inner class
          newPos = _findPrevKeyword(text, "new", openParenPos);
//            if (oldLog) System.err.println("\tnew found at " + newPos + ", openCurlyPos=" + curPos);
          if (! _isAnonymousInnerClass(newPos, curPos)) {
            // not an anonymous inner class
            newPos = -1;
          }
        }
      }

      while (classPos != -1 || interPos != -1 || newPos != -1) {
        if (newPos != -1) {
          classPos = -1;
          interPos = -1;
          break;
        }
        else if (otherPos > classPos && otherPos > interPos) {
          if (text.charAt(otherPos) != '{' || text.charAt(otherPos) != '}') ++otherPos;
          curPos = findPrevEnclosingBrace(otherPos, '{', '}');
          classPos = _findPrevKeyword(text, "class", curPos);
          interPos = _findPrevKeyword(text, "interface", curPos);
          otherPos = findPrevDelimiter(curPos, delims);
          newPos = -1;
          // see if there's a ) closer by
          closeParenPos = _findPrevNonWSCharPos(curPos);
          if (closeParenPos != -1 && text.charAt(closeParenPos) == ')') {
            // yes, find the matching (
            int openParenPos = findPrevEnclosingBrace(closeParenPos, '(', ')');
            if (openParenPos != -1 && text.charAt(openParenPos) == '(') {
              // this might be an inner class
              newPos = _findPrevKeyword(text, "new", openParenPos);
//                if (oldLog) System.err.println("\tnew found at " + newPos + ", openCurlyPos=" + curPos);
              if (! _isAnonymousInnerClass(newPos, curPos)) newPos = -1;
            }
          }
        }
        else {
          // either class or interface found first            
          curPos = Math.max(classPos, Math.max(interPos, newPos));
          break;
        }
      }
      
      if (classPos != -1 || interPos != -1) {
        if (classPos > interPos) curPos += "class".length();  // class found first
        else curPos += "interface".length();                  // interface found first
        
        int nameStart = getFirstNonWSCharPos(curPos);
        if (nameStart==-1) { throw new ClassNameNotFoundException("Cannot determine enclosing class name"); }
        int nameEnd = nameStart + 1;
        while (nameEnd < text.length()) {
          if (! Character.isJavaIdentifierPart(text.charAt(nameEnd)) && text.charAt(nameEnd) != '.') break;
          ++nameEnd;
        }
        name = text.substring(nameStart,nameEnd) + '$' + name;
      }
      else if (newPos != -1) {
        name = String.valueOf(_getAnonymousInnerClassIndex(curPos)) + "$" + name;
        curPos = newPos;
      }
      else break; // neither class nor interface found (exiting loop if qual == true)
    } while(qual);
    
    // chop off '$' at the end.
    if (name.length() > 0) name = name.substring(0, name.length() - 1);
    
    if (qual) {
      String pn = getPackageName();
      if ((pn.length() > 0) && (name.length() > 0)) {
        name = getPackageName() + "." + name;
      }
    }
//    log = oldLog;
    _storeInCache(key, name, pos);
    return name;
  }
  
  /** Returns true if this position is the instantiation of an anonymous inner class.  Only runs in the event thread.
    * @param pos position of "new"
    * @param openCurlyPos position of the next '{'
    * @return true if anonymous inner class instantiation
    */

  public boolean _isAnonymousInnerClass(final int pos, final int openCurlyPos) throws BadLocationException {
//    String t = getText(0, openCurlyPos+1);
//    System.err.print("_isAnonymousInnerClass(" + pos + ", " + openCurlyPos + ")");
//    System.err.println("_isAnonymousInnerClass(" + pos + ", " + openCurlyPos + "): `" + 
//                       t.substring(pos, openCurlyPos+1) + "`");
    
    // Check cache
    final Query key = new Query.AnonymousInnerClass(pos, openCurlyPos);
    Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) {
//      System.err.println(" ==> " + cached);
      return cached;
    }
    int newPos = pos;
//    synchronized(_reduced) {
      cached = false;

      String text = getText(0, openCurlyPos + 1);  // includes open Curly brace
      newPos += "new".length();
      int classStart = getFirstNonWSCharPos(newPos);
      if (classStart != -1) { 
        int classEnd = classStart + 1;
        while (classEnd < text.length()) {
          if (! Character.isJavaIdentifierPart(text.charAt(classEnd)) && text.charAt(classEnd) != '.') {
            // delimiter found
            break;
          }
          ++classEnd;
        }
        
        /* Determine parenStart, the postion immediately before the open parenthesis following the superclass name. */
//         System.err.println("\tclass = `" + text.substring(classStart,classEnd) + "`");
        int parenStart = getFirstNonWSCharPos(classEnd);
        if (parenStart != -1) {
          int origParenStart = parenStart;
          
//           System.err.println("\tfirst non-whitespace after class = " + parenStart + " `" + text.charAt(parenStart) + "`");
          if (text.charAt(origParenStart) == '<') {
            parenStart = -1;
            // might be a generic class
            int closePointyBracket = findNextEnclosingBrace(origParenStart, '<', '>');
            if (closePointyBracket != -1) {
              if (text.charAt(closePointyBracket) == '>') {
                parenStart = getFirstNonWSCharPos(closePointyBracket+1);
              }
            }
          }
        }
        
        if (parenStart != -1) {
          if (text.charAt(parenStart) == '(') {
            setCurrentLocation(parenStart + 1);   // reduced model points to pos == parenStart + 1
            int parenEnd = balanceForward();
            if (parenEnd > -1) {
              parenEnd = parenEnd + parenStart + 1;
//               System.err.println("\tafter closing paren = " + parenEnd);
              int afterParen = getFirstNonWSCharPos(parenEnd);
//               System.err.println("\tfirst non-whitespace after paren = " + parenStart + " `" + text.charAt(afterParen) + "`");
              cached = (afterParen == openCurlyPos); 
            }
          }
        }
      }
      _storeInCache(key, cached, openCurlyPos);
//      System.err.println(" ==> " + cached);
      return cached;
//    }
  }
  
  /** Gets the package name embedded in the text of this document by minimally parsing the document to find the
    * package statement.  If package statement is not found or is ill-formed, returns "" as the package name.
    * @return the name of package embedded in this document.  If there is no well-formed package statement, 
    * returns "" as the package name.
    */
  public String getPackageName() {
    // assert EventQueue.isDispatchThread();
    Reader r;
    r = new StringReader(getText()); // getText() is cheap if document is not resident
    try { return new Parser(r).packageDeclaration(Parser.DeclType.TOP).getName(); }
    catch (ParseException e) { return ""; }
    // addresses bug [ 1815387 ] Editor should discard parse errors for now
    // we should upgrade our parser to handle @
    catch (TokenMgrError e) { return ""; }
    catch (Error e) {
      // JavaCharStream does not use a useful exception type for escape character errors
      String msg = e.getMessage();
      if (msg != null && msg.startsWith("Invalid escape character")) {
        return "";
      }
      else { throw e; }
    }
    finally {
      try { r.close(); }
      catch (IOException e) { /* ignore */ }
    }
  }
  
  /** Returns the index of the anonymous inner class being instantiated at the specified position (where openining brace
    * for anonymous inner class is pos).  Only runs in event thread.
    * @param pos is position of the opening curly brace of the anonymous inner class
    * @return anonymous class index
    */
  int _getAnonymousInnerClassIndex(final int pos) throws BadLocationException, ClassNameNotFoundException {   
//    boolean oldLog = true; // log; log = false;
    
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    // Check cache
    final Query key = new Query.AnonymousInnerClassIndex(pos);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
//      log = oldLog;
      return cached.intValue();
    }

    int newPos = pos; // formerly pos -1 // move outside the curly brace?  Corrected to do nothing since already outside

//    final char[] delims = {'{','}','(',')','[',']','+','-','/','*',';',':','=','!','@','#','$','%','^','~','\\','"','`','|'};

    final String className = _getEnclosingClassName(newPos - 2 , true);  // class name must be followed by at least "()"
    final String text = getText(0, newPos - 2);  // excludes miminal (empty) argument list after class name
    int index = 1;
    
//    if (oldLog) System.err.println("anon before " + pos + " enclosed by " + className);
    while ((newPos = _findPrevKeyword(text, "new", newPos - 4)) != -1) { // excludes space + minimal class name + args
//      if (oldLog) System.err.println("new found at " + newPos);
      int afterNewPos = newPos + "new".length();
      int classStart = getFirstNonWSCharPos(afterNewPos);
      if (classStart == -1) { continue; }
      int classEnd = classStart + 1;
      while (classEnd < text.length()) {
        if (! Character.isJavaIdentifierPart(text.charAt(classEnd)) && text.charAt(classEnd) != '.') {
          // delimiter found
          break;
        }
        ++classEnd;
      }
//      if (oldLog) System.err.println("\tclass = `" + text.substring(classStart,classEnd) + "`");
      int parenStart = getFirstNonWSCharPos(classEnd);
      if (parenStart == -1) { continue; }
      int origParenStart = parenStart;
      
//      if (oldLog) System.err.println("\tfirst non-whitespace after class = " + parenStart + " `" + text.charAt(parenStart) + "`");
      if (text.charAt(origParenStart) == '<') {
        parenStart = -1;
        // might be a generic class
        int closePointyBracket = findNextEnclosingBrace(origParenStart, '<', '>');
        if (closePointyBracket != -1) {
          if (text.charAt(closePointyBracket) == '>') {
            parenStart = getFirstNonWSCharPos(closePointyBracket + 1);
          }
        }
      }
      if (parenStart == -1) { continue; }      
      if (text.charAt(parenStart) != '(') { continue; }
      int parenEnd = findNextEnclosingBrace(parenStart, '(', ')');
      
      int nextOpenCurly = _findNextOpenCurly(text, parenEnd);
      if (nextOpenCurly == -1) { continue; }
//      if (oldLog) System.err.println("{ found at " + nextOpenCurly + ": `" + 
//                                     text.substring(newPos, nextOpenCurly + 1) + "`");
//      if (oldLog) System.err.println("_isAnonymousInnerClass(" + newPos + ", " + nextOpenCurly + ")");
      if (_isAnonymousInnerClass(newPos, nextOpenCurly)) {
//        if (oldLog) System.err.println("is anonymous inner class");
        String cn = _getEnclosingClassName(newPos, true);
//        if (oldLog) System.err.println("enclosing class = " + cn);
        if (! cn.startsWith(className)) { break; }
        else if (! cn.equals(className)) {
          newPos = findPrevEnclosingBrace(newPos, '{', '}');
          continue;
        }
        else ++index;
      }
    }
    _storeInCache(key, index, pos);
//    oldLog = log;
    return index;
  }
  
  /** Returns the name of the class or interface enclosing the caret position at the top level.
    * @return Name of enclosing class or interface
    * @throws ClassNameNotFoundException if no enclosing class found
    */
  public String getEnclosingTopLevelClassName(int pos) throws ClassNameNotFoundException {
      int oldPos = _currentLocation;
      try {
        setCurrentLocation(pos);
        BraceInfo info = _getEnclosingBrace();
        
        // Find top level open brace
        int topLevelBracePos = -1;
        String braceType = info.braceType();
        while (! braceType.equals(BraceInfo.NONE)) {
          if (braceType.equals(BraceInfo.OPEN_CURLY)) {
            topLevelBracePos = _currentLocation - info.distance();
          }
          move(-info.distance());
          info = _getEnclosingBrace();
          braceType = info.braceType();
        }
        if (topLevelBracePos == -1) {
          // No top level brace was found, so we can't find a top level class name
          setCurrentLocation(oldPos);
          throw new ClassNameNotFoundException("no top level brace found");
        }
        
        char[] delims = {'{', '}', ';'};
        int prevDelimPos = findPrevDelimiter(topLevelBracePos, delims);
        if (prevDelimPos == -1) {
          // Search from start of doc
          prevDelimPos = 0;
        }
        else prevDelimPos++;
        setCurrentLocation(oldPos);
        
        // Parse out the class name
        return getNextTopLevelClassName(prevDelimPos, topLevelBracePos);
      }
      catch (BadLocationException ble) { throw new UnexpectedException(ble); }
      finally { setCurrentLocation(oldPos); }
  }
  
  /** Gets the name of first class/interface/enum declared in file among the definitions anchored at:
    * @param indexOfClass  index in this of a top-level occurrence of class 
    * @param indexOfInterface  index in this of a top-level occurrence of interface
    * @param indexOfEnum index in this of a top-level occurrence of enum
    */
  private String getFirstClassName(int indexOfClass, int indexOfInterface,
                                   int indexOfEnum) throws ClassNameNotFoundException {
    try {
      if ((indexOfClass == -1) && (indexOfInterface == -1) && (indexOfEnum == -1)) throw ClassNameNotFoundException.DEFAULT;
      
      // should we convert this to a sorted queue or something like that?
      // should we have to extend this past three keywords, it will get rather hard to maintain
      if ((indexOfEnum == -1) || 
          ((indexOfClass != -1) && (indexOfClass < indexOfEnum)) ||
          ((indexOfInterface != -1) && (indexOfInterface < indexOfEnum))) {
        // either "enum" not found, or "enum" found after "class" or "interface"
        // "enum" is irrelevant
        // we know that at least one of indexOfClass and indexOfInterface is != -1
        if ((indexOfInterface == -1) ||
            ((indexOfClass != -1) && (indexOfClass < indexOfInterface))) {
          // either "interface" not found, or "interface" found after "class"
          return getNextIdentifier(indexOfClass + "class".length());
        }
        else {
          // "interface" found, and found before "class"
          return getNextIdentifier(indexOfInterface + "interface".length());
        }
      }
      else {
        // "enum" found, and found before "class" and "interface"
        return getNextIdentifier(indexOfEnum + "enum".length());
      }    
    }
    catch(IllegalStateException ise) { throw ClassNameNotFoundException.DEFAULT; }
  }
  
  /** Gets the name of the document's main class: the document's only public class/interface or 
    * first top level class if document contains no public classes or interfaces. */
  public String getMainClassName() throws ClassNameNotFoundException {
      final int oldPos = _currentLocation;
      
      try {
        setCurrentLocation(0);
        final String text = getText();  // getText() is cheap if document is not resident
        
        final int indexOfClass = _findKeywordAtToplevel("class", text, 0);
        final int indexOfInterface = _findKeywordAtToplevel("interface", text, 0);
        final int indexOfEnum = _findKeywordAtToplevel("enum", text, 0);
        final int indexOfPublic = _findKeywordAtToplevel("public", text, 0);
        
        if (indexOfPublic == -1)  return getFirstClassName(indexOfClass, indexOfInterface, indexOfEnum);
        
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
        int indexOfPublicEnum = _findKeywordAtToplevel("enum", subText, afterPublic); // relative offset
        if (indexOfPublicEnum != -1) indexOfPublicEnum += afterPublic;
//        _log.log("indexOfPublicClass = " + indexOfPublicClass + " indexOfPublicInterface = " + indexOfPublicInterface);
        
        return getFirstClassName(indexOfPublicClass, indexOfPublicInterface, indexOfPublicEnum);
        
      }
      finally { setCurrentLocation(oldPos); }
  }
  
  /** Gets the name of the top level class in this source file by finding the first declaration of a class or interface.
    * @return The name of first class in the file
    * @throws ClassNameNotFoundException if no top level class found
    */
  public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
    return getNextTopLevelClassName(0, getLength());
  }
  
  // note: need to update this to work with pos
  public String getNextTopLevelClassName(int startPos, int endPos) throws ClassNameNotFoundException {
      int oldPos = _currentLocation;
      
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
      finally { setCurrentLocation(oldPos); }
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
    
    int oldPos = _currentLocation;
    int index = 0;
    while (true) {
      index = text.indexOf(keyword, index);
      if (index == -1) break; // not found
      else {
        // found a match, check quality
        setCurrentLocation(textOffset + index);
        
        // check that the keyword is not in a comment and is followed by whitespace
        int indexPastKeyword = index + keyword.length();
        if (indexPastKeyword < text.length()) {
          if (! isShadowed() && Character.isWhitespace(text.charAt(indexPastKeyword))) {
            // found a match but may not be at top level
            if (! notInBlock(index)) index = -1; //in a paren phrase, gone too far
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
    setCurrentLocation(oldPos);
//        _log.log("findKeyWord(" + keyword + ", ..., " + textOffset + ")");
    return index;
  }
  
  /** Wrapper for Position objects to allow relinking to a new Document. */
  public static class WrappedPosition implements Position {
    private Position _wrapped;
    /** Constructor is only called from createPosition below. */
    WrappedPosition(Position w) { setWrapped(w); }
    public void setWrapped(Position w) { _wrapped = w; }
    public int getOffset() { return _wrapped.getOffset(); }
  }
  
  /** Factory method for created WrappedPositions. Stores the created Position instance so it can be linked to a 
    * different DefinitionsDocument later. 
    */
  public Position createPosition(final int offset) throws BadLocationException {
    /* The following attempt to defer document loading did not work because offset became stale.  Postions must be
     * created eagerly. */
//    WrappedPosition wp = new WrappedPosition(new LazyPosition(new Suspension<Position>() {
//      public Position eval() {
//        try { return createUnwrappedPosition(offset); }
//        catch(BadLocationException e) { throw new UnexpectedException(e); }
//      }
//    }));
    WrappedPosition wp = new WrappedPosition(createUnwrappedPosition(offset));
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
  
  /** Getter method for CompoundUndoManager
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
  
  protected void endCompoundEdit(int key) { _undoManager.endCompoundEdit(key); }
  
  //This method added for FrenchKeyBoardFix
  protected void endLastCompoundEdit() { _undoManager.endLastCompoundEdit(); }
  
  protected void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand) {
    chng.addEdit(new CommandUndoableEdit(undoCommand, doCommand));    
  }
  
  
  /** Formerly used to call editToBeUndone and editToBeRedone since they are protected methods in UndoManager. */
//  private class OurUndoManager extends UndoManager {
//    private boolean _compoundEditState = false;
//    private OurCompoundEdit _compoundEdit;
//
//    public void startCompoundEdit() {
//      if (_compoundEditState) {
//        throw new IllegalStateException("Cannot start a compound edit while making a compound edit");
//      }
//      _compoundEditState = true;
//      _compoundEdit = new OurCompoundEdit();
//    }
//
//    public void endCompoundEdit() {
//      if (!_compoundEditState) {
//        throw new IllegalStateException("Cannot end a compound edit while not making a compound edit");
//      }
//      _compoundEditState = false;
//      _compoundEdit.end();
//      super.addEdit(_compoundEdit);
//    }
//
//    public UndoableEdit getNextUndo() {
//      return editToBeUndone();
//    }
//
//    public UndoableEdit getNextRedo() {
//      return editToBeRedone();
//    }
//
//    public boolean addEdit(UndoableEdit e) {
//      if (_compoundEditState) {
//        return _compoundEdit.addEdit(e);
//      }
//      else {
//        return super.addEdit(e);
//      }
//    }
//  }
//
//
//  public java.util.Vector getEdits() {
//     return _undoManager._compoundEdit.getEdits();
//  }
//
//  private class OurCompoundEdit extends CompoundEdit {
//     public java.util.Vector getEdits() {
//        return edits;
//     }
//  }
  
  /** Formerly used to help track down memory leaks */
//  protected void finalize() throws Throwable{
//    System.err.println("destroying DefDocument for " + _odd);
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
  
  /** Registers a finalization listener with the specific instance of the ddoc
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
  
  /** This is called when this method is GC'd.  Since this class implements edu.rice.cs.drjava.model.Finalizable, it
    * must notify its listeners
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
  
  /** Returns true if one of the words 'class', 'interface' or 'enum' is found
    * in non-comment text. */
  public boolean containsClassOrInterfaceOrEnum() throws BadLocationException {
    
/* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    int i, j;
    int reducedPos = 0;
    
    final String text = getText();
    final int origLocation = _currentLocation;
    try {
      // Move reduced model to beginning of file
      _reduced.move(-origLocation);
      
      // Walk forward from specificed position
      i = text.indexOf("class", reducedPos);
      j = text.indexOf("interface", reducedPos);
      if (i==-1) i = j; else if (j >= 0) i = Math.min(i,j);
      j = text.indexOf("enum", reducedPos);
      if (i==-1) i = j; else if (j >= 0) i = Math.min(i,j);
      while (i > - 1) {
        // Move reduced model to walker's location
        _reduced.move(i - reducedPos);  // reduced model points to i
        reducedPos = i;                 // reduced model points to reducedPos
        
        // Check if matching keyword should be ignored because it is within a comment, or quotes
        ReducedModelState state = _reduced.getStateAtCurrent();
        if (!state.equals(FREE) || _isStartOfComment(text, i) || ((i > 0) && _isStartOfComment(text, i - 1))) {
          i = text.indexOf("class", reducedPos+1);
          j = text.indexOf("interface", reducedPos+1);
          if (i==-1) i = j; else if (j >= 0) i = Math.min(i,j);
          j = text.indexOf("enum", reducedPos+1);
          if (i==-1) i = j; else if (j >= 0) i = Math.min(i,j);
          continue;  // ignore match
        }
        else {
          return true; // found match
        }        
      }  
      
      return false;
    }
    finally {
      _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
    }
  }
}
