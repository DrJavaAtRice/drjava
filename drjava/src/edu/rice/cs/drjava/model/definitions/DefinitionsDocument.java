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

package edu.rice.cs.drjava.model.definitions;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.DocumentEvent;
import gj.util.Vector;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.StringTokenizer;

import java.io.File;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.DefaultGlobalModel;
import edu.rice.cs.drjava.model.FileMovedException;
import edu.rice.cs.drjava.model.EventNotifier;


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
 * @see BraceReductions
 * @see ReducedModelControl
 * @see ReducedModelComment
 * @see ReducedModelBrace
 *
 * @version $Id$
 */
public class DefinitionsDocument extends PlainDocument implements OptionConstants {
  /** The maximum number of undos the model can remember */
  private static final int UNDO_LIMIT = 1000;
  /** A set of normal endings for lines. */
  protected static HashSet _normEndings = _makeNormEndings();
  /** A set of Java keywords. */
  protected static HashSet _keywords = _makeKeywords();
  /** A set of Java keywords. */
  protected static HashSet _primTypes = _makePrimTypes();
  /** The default indent setting. */
  private int _indent = 2;
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
  
  /**
   * The reduced model of the document that handles most of the
   * document logic and keeps track of state.  Should ONLY be referenced from
   * this class, and all references to it MUST be synchronized.
   */
  private BraceReduction _reduced = new ReducedModelControl();
  /** The absolute character offset in the document. */
  private int _currentLocation = 0;
  
  private File _file;
  private long _timestamp;

  private final Indenter _indenter;
  private CompoundUndoManager _undoManager;
  
  /**
   * Caches calls to the reduced model to speed up indent performance.
   * Must be cleared every time document is changed.  Use by calling
   * _checkCache and _storeInCache.
   */
  private final Hashtable _helperCache;
  
  /**
   * Keeps track of the order of elements added to the helper method cache,
   * so that the oldest elements can be removed when the maximum size is
   * reached.  A true LRU cache might be more effective, though I'm not sure
   * what the exact pattern of helper method reuse is-- this should be
   * sufficient without significantly decreasing the effectiveness of the cache.
   */
  private final Vector<String> _helperCacheHistory;
  
  /**
   * Whether anything is stored in the cache.  (Prevents clearing the table
   * unnecessarily on every change to the document.)
   */
  private boolean _cacheInUse;
  
  /**
   * Maximum number of elements to allow in the helper method cache.
   * Only encountered when indenting very large blocks, since the cache
   * is cleared after each change to the document.
   */
  private static final int MAX_CACHE_SIZE = 10000;
  
  /**
   * Constant for starting position of document.
   */
  public static final int DOCSTART = 0;

  /**
   * Constant used by helper methods to indicate an error
   */
  public static final int ERROR_INDEX = -1;

  /**
   * keeps track of the listeners to this model
   */
  private final EventNotifier _notifier;
  
  /**
   * Constructor.
   */
  public DefinitionsDocument(EventNotifier notifier) {
    this(DefaultGlobalModel.INDENTER, notifier);
  }

  public DefinitionsDocument(Indenter i, EventNotifier notifier) {
    super();
    _indenter = i;
    _file = null;
    _cachedLocation = 0;
    _cachedLineNum = 1;
    _cachedPrevLineLoc = -1;
    _cachedNextLineLoc = -1;
    _classFileInSync = false;
    _classFile = null;
    _helperCache = new Hashtable();
    _helperCacheHistory = new Vector();
    _cacheInUse = false;
    _notifier = notifier;
    resetUndoManager();
  }

  /**
   * This method should never be called outside of this class. Doing so can create
   * all sorts of synchronization issues. It is package private for test purposes.
   * @return The reduced model of this document.
   */
  BraceReduction getReduced() {
    return _reduced;
  }

  /**
   * Create a set of normal endings, i.e., semi-colons and braces for the purposes
   * of indenting.
   * @return the set of normal endings
   */
  protected static HashSet _makeNormEndings() {
    HashSet normEndings = new HashSet();
    normEndings.add(";");
    normEndings.add("{");
    normEndings.add("}");
    normEndings.add("(");
    return  normEndings;
  }

  /**
   * Create a set of Java/GJ keywords for special coloring.
   * @return the set of keywords
   */
  protected static HashSet _makeKeywords() {
    final String[] words =  {
      "import", "native", "package", "goto", "const", "if", "else",
      "switch", "while", "for", "do", "true", "false", "null", "this",
      "super", "new", "instanceof",    "return",
      "static", "synchronized", "transient", "volatile", "final",
      "strictfp", "throw", "try", "catch", "finally",
      "throws", "extends", "implements", "interface", "class",
      "break", "continue", "public", "protected", "private", "abstract",
      "case", "default", "assert"
    };
    HashSet keywords = new HashSet();
    for (int i = 0; i < words.length; i++) {
      keywords.add(words[i]);
    }
    return  keywords;
  }
  /**
   * Create a set of Java/GJ primitive types for special coloring.
   * @return the set of primitive types
   */
  protected static HashSet _makePrimTypes() {
    final String[] words =  {
      "boolean", "char", "byte", "short", "int", 
      "long", "float", "double", "void", 
    };
    HashSet prims = new HashSet();
    for (int i = 0; i < words.length; i++) {
      prims.add(words[i]);
    }
    return  prims;
  }

  /**
   * Returns whether this document is currently untitled
   * (indicating whether it has a file yet or not).
   * @return true if the document is untitled and has no file
   */
  public boolean isUntitled() {
    return (_file == null);
  }

  /**
   * Returns the file for this document.  If the document
   * is untitled and has no file, it throws an IllegalStateException.
   * @return the file for this document
   * @throws IllegalStateException if file has not been set
   * @throws FileMovedException if file has been moved or deleted from its previous location
   */
  public File getFile() 
    throws IllegalStateException , FileMovedException {
    if (_file == null) {
      throw new IllegalStateException(
        "This document does not yet have a file.");
    }
    //does the file actually exist?
    if (_file.exists()) {
      return _file;
    }
    else {
      throw new FileMovedException(_file,
        "This document's file has been moved or deleted.");
    }
  }
  
  /**
   * Returns the name of this file, or "(untitled)" if no file.
   */
  public String getFilename() {
    String filename = "(Untitled)";
    try {
      File file = getFile();
      filename = file.getName();
    }
    catch (IllegalStateException ise) {
      // No file, leave as "untitled"
    }
    catch (FileMovedException fme) {
      // Recover, even though file has been deleted
      File file = fme.getFile();
      filename = file.getName();
    }
    return filename;
  }
      

  public void setFile(File file) {
    _file = file;

    //jim: maybe need lock
    if (_file != null) {
      _timestamp = _file.lastModified();
    }
  }
  
  public long getTimestamp() {
    return _timestamp;
  }
    /**
   * This function finds the given character in the same statement as the given
   * position, and before the given position.  It is used by QuestionExistsCharInStmt and
   * QuestionExistsCharInPrevStmt
   */
  public boolean findCharInStmtBeforePos(char findChar, int position){
    if(position == DefinitionsDocument.ERROR_INDEX) {
      // Should not happen
      throw new UnexpectedException(new
        IllegalArgumentException("Argument endChar to " + 
                                 "QuestionExistsCharInStmt must be a char " +
                                 "that exists on the current line."));
    }
    
    char[] findCharDelims = {findChar, ';', '{', '}'};
    int prevFindChar;
    
    // Find the position of the previous occurence findChar from the 
    // position of endChar (looking in paren phrases as well)
    try {
      prevFindChar = this.findPrevDelimiter(position, findCharDelims, false);
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
    
    if ((prevFindChar == DefinitionsDocument.ERROR_INDEX) ||
        (prevFindChar < 0)) {
      // Couldn't find a previous occurence findChar
      return false;
    }
    
    // Determine if prevFindChar was findChar, rather than end
    //  of statement delimiter
    boolean found = false;
    try {
      String foundString = this.getText(prevFindChar, 1);
      char foundChar = foundString.charAt(0);
      found = (foundChar == findChar);
    }
    catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
    
    return found;
  }
  
  /**
   * Checks the helper method cache for a stored value.  Returns the
   * value if it has been cached, or null otherwise.
   * Calling convention for keys:
   *   methodName:arg1:arg2
   * @param key Name of the method and arguments
   */
  protected Object _checkCache(String key) {
    //_helperCache.put(key+"|time", new Long(System.currentTimeMillis()));
    Object result = _helperCache.get(key);
    //if (result != null) DrJava.consoleOut().println("Using cache for " + key);
    return result;
  }
  
  /**
   * Stores the given result in the helper method cache.
   * Calling convention for keys:
   *   methodName:arg1:arg2
   * @param key Name of method and arguments
   * @param result Result of the method call
   */
  protected void _storeInCache(String key, Object result) {
    _cacheInUse = true;
    
    // Prevent going over max size
    if (_helperCache.size() >= MAX_CACHE_SIZE) {
      if (_helperCacheHistory.size() > 0) {
        _helperCache.remove( _helperCacheHistory.elementAt(0) );
        _helperCacheHistory.removeElementAt(0);
      }
      else {
        // Shouldn't happen
        throw new RuntimeException("Cache larger than cache history!");
      }
    }
    Object prev = _helperCache.put(key, result);
    // Add to history if the insert increased the size of the table
    if (prev == null) {
      _helperCacheHistory.addElement(key);
    }
    
    /*
    long end = System.currentTimeMillis();
    Long start = (Long)_helperCache.get(key+"|time");
    if (start != null) {
      _helperCache.remove(key+"|time");
      long delay = end - start.longValue();
      if (delay > maxHelpDelay) {
        maxHelpDelay = delay;
        maxKey = key;
        //DrJava.consoleOut().println("   Longest: " + maxHelpDelay + "ms from " + maxKey +
        //                            ", line " + getCurrentLine());
      }
    }
    else {
      DrJava.consoleOut().println("  CACHE MISS: " + key);
    }
    */
  }
  
  // Fields for monitoring performance
  
  //long maxHelpDelay = 0;
  //String maxKey = "none";
  
  
  /**
   * Clears the helper method cache.
   * Should be called every time the document is modified.
   */
  protected void _clearCache() {
    _helperCache.clear();
    _helperCacheHistory.removeAllElements();
    _cacheInUse = false;
  }
  
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
  public String getQualifiedClassName(int pos) 
    throws ClassNameNotFoundException 
  {
    return _getPackageQualifier() + getEnclosingTopLevelClassName(pos);
  }
  
  /**
   * Gets an appropriate prefix to fully qualify a class name.
   * Returns this class's package followed by a dot, or the empty
   * string if no package name is found.
   */
  protected String _getPackageQualifier() {
    String packageName = "";
    try {
      packageName = this.getPackageName();
    }
    catch (InvalidPackageException e) {
      // Couldn't find package, pretend there's none
    }
    if ((packageName != null) && (!packageName.equals(""))) {
      packageName = packageName + ".";
    }
    return packageName;
  }
  
  public void setClassFileInSync(boolean inSync) {
    _classFileInSync = inSync;
  }
  
  public boolean getClassFileInSync() {
    return _classFileInSync;
  }
  
  public void setCachedClassFile(File classFile) {
    _classFile = classFile;
  }
  
  public File getCachedClassFile() {
    return _classFile;
  }
  
  /**
   * Inserts a string of text into the document.
   * It turns out that this is not where we should do custom processing
   * of the insert; that is done in {@link #insertUpdate}.
   */
  public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException
  {
    // Clear the helper method cache
    if (_cacheInUse) _clearCache();
    
    // If _removeTabs is set to true, remove all tabs from str.
    // It is a current invariant of the tabification functionality that
    // the document contains no tabs, but we want to allow the user
    // to override this functionality.
    if (_tabsRemoved) {
      str = _removeTabs(str);
    }

    if (!_modifiedSinceSave) {
      _modifiedSinceSave = true;
      _classFileInSync = false;
    }
    
    super.insertString(offset, str, a);
  }

  /**
   * Updates document structure as a result of text insertion.
   * This happens after the text has actually been inserted.
   * Here we update the reduced model (via an {@link InsertCommand})
   * and store information for how to undo/redo the reduced model changes
   * inside the {@link DefaultDocumentEvent}.
   *
   * @see InsertCommand
   * @see DefaultDocumentEvent
   * @see CommandUndoableEdit
   */
  protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng,
                              AttributeSet attr)
  {
    // Clear the helper method cache
    if (_cacheInUse) _clearCache();
    
    super.insertUpdate(chng, attr);

    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      final String str = getText(offset, length);

      InsertCommand doCommand = new InsertCommand(offset, str);
      RemoveCommand undoCommand = new RemoveCommand(offset, length);

      // add the undo/redo
      chng.addEdit(new CommandUndoableEdit(undoCommand, doCommand));
      // actually do the insert
      doCommand.run();
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }

  /**
   * Removes a block of text from the specified location.
   * We don't update the reduced model here; that happens
   * in {@link #removeUpdate}.
   */
  public void remove(int offset, int len) throws BadLocationException {
    // Clear the helper method cache
    if (_cacheInUse) _clearCache();
    
    if (!_modifiedSinceSave) {
      _modifiedSinceSave = true;
      _classFileInSync = false;
    }
    super.remove(offset, len);
  }

  /**
   * Updates document structure as a result of text removal.
   * This happens before the text has actually been removed.
   * Here we update the reduced model (via an {@link RemoveCommand})
   * and store information for how to undo/redo the reduced model changes
   * inside the {@link DefaultDocumentEvent}.
   *
   * @see RemoveCommand
   * @see DefaultDocumentEvent
   * @see CommandUndoableEdit
   */
  protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
    // Clear the helper method cache
    if (_cacheInUse) _clearCache();
    
    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      final String removedText = getText(offset, length);
            super.removeUpdate(chng);

      Runnable doCommand = new RemoveCommand(offset, length);
      Runnable undoCommand = new InsertCommand(offset, removedText);

      // add the undo/redo info
      chng.addEdit(new CommandUndoableEdit(undoCommand, doCommand));

      // actually do the removal from the reduced model
      doCommand.run();
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    
  }

  /**
   * Given a String, return a new String will all tabs converted to spaces.
   * Each tab is converted to one space, since changing the number of 
   * characters within insertString screws things up.
   * @param source the String to be converted.
   * @return a String will all the tabs converted to spaces
   */
  String _removeTabs(final String source) {
    // Clear the helper method cache
    if (_cacheInUse) _clearCache();
    
    return source.replace('\t', ' ');
  }

  /*
  String _removeTabs(String source) {
    StringBuffer target = new StringBuffer();
    for (int i = 0; i < source.length(); i++) {
      char next = source.charAt(i);

      if (next != '\t') {
        target.append(source.charAt(i));
      }
      else {
        // Replace tab with a number of
        // spaces according to the value of _indent.
        for (int j = 0; j < _indent; j++) {
          target.append(' ');
        }
      }
    }
    return target.toString();
  }
  */

  /**
   * Add a character to the underlying reduced model.
   * @param curChar the character to be added.
   */
  private synchronized void _addCharToReducedModel(char curChar) {
    // Clear the helper method cache
    if (_cacheInUse) _clearCache();
    
    _reduced.insertChar(curChar);
  }

  /**
   * Fire event that styles changed from current location to the end.
   * Right now we do this every time there is an insertion or removal.
   * Two possible future optimizations:
   * <ol>
   * <li>Only fire changed event if text other than that which was inserted
   *     or removed *actually* changed status. If we didn't changed the status
   *     of other text (by inserting or deleting unmatched pair of quote or
   *     comment chars), no change need be fired.
   * <li>If a change must be fired, we could figure out the exact end
   *     of what has been changed. Right now we fire the event saying that
   *     everything changed to the end of the document.
   * </ol>
   *
   * I don't think we'll need to do either one since it's still fast now.
   * I think this is because the UI only actually paints the things on the
   * screen anyway.
   */
  private void _styleChanged() {
    int length = getLength() - _currentLocation;
    //DrJava.consoleErr().println("Changed: " + _currentLocation + ", " + length);
    DocumentEvent evt = new DefaultDocumentEvent(_currentLocation,
                                                 length,
                                                 DocumentEvent.EventType.CHANGE);
    fireChangedUpdate(evt);
  }
  
  /**
   * Originally designed to allow undoManager to set the current document to
   * be modified whenever an undo or redo is performed.
   */
  public void setModifiedSinceSave() {
    _modifiedSinceSave = true;
  }

  /**
   * Whenever this document has been saved, this method should be called
   * so that it knows it's no longer in a modified state.
   */
  public void resetModification() {
    try {
      writeLock();
      _modifiedSinceSave = false;      
   if (_file != null)
     _timestamp = _file.lastModified();
    }
    finally {
      writeUnlock();
    }
  }

  /**
   * Determines if the document has been modified since the last save.
   * @return true if the document has been modified
   */
  public boolean isModifiedSinceSave() {
    try {
      readLock();
      return  _modifiedSinceSave;
    }
    finally {
      readUnlock();
    }
  }

  /**
   * Determines if the document has been modified since the last save.
   * @return true if the document has been modified
   */
  public boolean isModifiedOnDisk() {
    boolean ret = false;
    try {
      readLock();
      if (_file == null) {
      } else {
        ret = (_file.lastModified() > _timestamp);
      }
    }
    finally {
      readUnlock();
    }
    return ret;
  }
 
  /**
   * Get the current location of the cursor in the document.
   * Unlike the usual swing document model, which is stateless, because of our implementation
   * of the underlying reduced model, we need to keep track of the current location.
   * @return where the cursor is as the number of characters into the document
   */
  public int getCurrentLocation() {
    return  _currentLocation;
  }

  /**
   * Change the current location of the document
   * @param loc the new absolute location
   */
  public void setCurrentLocation(int loc) {
    move(loc - _currentLocation);
  }

  /**
   * The actual cursor movement logic.  Helper for setCurrentLocation(int).
   * @param dist the distance from the current location to the new location.
   */
  public synchronized void move(int dist) {
    //if (_currentLocation != _reduced.absOffset()) {
    //  DrJava.consoleOut().println("DefDoc.currentLocation: " + _currentLocation);
    //  DrJava.consoleOut().println("Reduced location: " + _reduced.absOffset());
    //}
    
    int newLoc = _currentLocation + dist;
    if (newLoc < 0) {
      throw  new RuntimeException("location < 0?! oldLoc=" + _currentLocation + " dist=" +
                                  dist);
    }
    _currentLocation = newLoc;
    _reduced.move(dist);
  }
  
  /** 
   * Return the current column of the cursor position.
   * Uses a 0 based index.
   */
  public int getCurrentCol() {
    int here = getCurrentLocation();
    int startOfLine = getLineStartPos(here);
    return here - startOfLine;
  }
  
  /**
   * Return the current line of the cursor position.
   * Uses a 1 based index.
   */
  public int getCurrentLine() {
    int here = getCurrentLocation();
    if ( _cachedLocation > getLength() ){ 
      // we can't know the last line number after a delete.
      // starting over.
      _cachedLocation = 0;
      _cachedLineNum = 1;
    }
    if ( _cachedNextLineLoc > getLength() ){
      _cachedNextLineLoc = -1;
    }
    // let's see if we get off easy
    if( ! ( _cachedPrevLineLoc < here && here < _cachedNextLineLoc ) ){ 
    
      // this if improves performance when moving from the
      // end of the document to the beginnning.
      // in essence, it calculates the line number from
      // scratch
      if( _cachedLocation - here > here ){
        _cachedLocation = 0;
        _cachedLineNum = 1;
      }        
      int lineOffset = _getRelativeLine();
      _cachedLineNum = _cachedLineNum+lineOffset;      
      
    }
    _cachedLocation = here;
    _cachedPrevLineLoc = getLineStartPos(here);
    _cachedNextLineLoc = getLineEndPos(here);
    return _cachedLineNum;
  }
  

  /**
   * This method returns the relative offset of line number
   * from the previous location in the document.
   **/
  private int _getRelativeLine(){
    int count=0;
    int currLoc = getCurrentLocation();
    
    setCurrentLocation( _cachedLocation );
    
    if( _cachedLocation > currLoc ){
      // we moved backward
      int prevLineLoc = getLineStartPos( _cachedLocation );
      while( prevLineLoc > currLoc ){
        count--;
        prevLineLoc = getLineStartPos( prevLineLoc - 1 );
        // temporary performance optimization
        setCurrentLocation(prevLineLoc);
      }
    }
    
    else{
      // we moved forward
      int nextLineLoc = getLineEndPos( _cachedLocation );
      while( nextLineLoc < currLoc ){
        count++;
        nextLineLoc = getLineEndPos( nextLineLoc + 1 );
        // temporary performance optimization
        setCurrentLocation(nextLineLoc);
      }
    }
    setCurrentLocation( currLoc );
    return count;
  }
  
  
  /**
   * Get the indent level.
   * @return the indent level
   */
  public int getIndent() {
    return _indent;
  }

  /**
   * Set the indent to a particular number of spaces.
   * @param indent the size of indent that you want for the document
   */
  public void setIndent(final int indent) {
    DrJava.getConfig().setSetting(INDENT_LEVEL,new Integer(indent));
    this._indent = indent;
  }

  /**
   * Searching backwards, finds the position of the first character that is one
   * of the given delimiters.  Does not look for delimiters inside paren phrases.
   * (eg. skips semicolons used inside for statements.)
   * NB: ignores comments.
   * @param pos Position to start from
   * @param delims array of characters to search for
   * @return position of first matching delimiter, or ERROR_INDEX if beginning
   * of document is reached.
   */
  public int findPrevDelimiter(int pos, char[] delims) throws BadLocationException {
    return findPrevDelimiter(pos, delims, true);
  }
    
  /**
   * Searching backwards, finds the position of the first character that is one
   * of the given delimiters.  Will not look for delimiters inside a paren
   * phrase if skipParenPhrases is true.
   * NB: ignores comments.
   * @param pos Position to start from
   * @param delims array of characters to search for
   * @param skipParenPhrases whether to look for delimiters inside paren phrases
   *  (eg. semicolons in a for statement)
   * @return position of first matching delimiter, or ERROR_INDEX if beginning
   * of document is reached.
   */
  public synchronized int findPrevDelimiter(int pos, char[] delims, boolean skipParenPhrases)
    throws BadLocationException
  {
    // Check cache
    String key = "findPrevDelimiter:" + pos;
    for (int i=0; i < delims.length; i++) {
      key += ":" + delims[i];
    }
    key += ":" + skipParenPhrases;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
      return cached.intValue();
    }
    
    int j, i;
    char c;
    String text = getText(DOCSTART, pos);
    
    final int origLocation = _currentLocation;
    // Move reduced model to location pos
    _reduced.move(pos - origLocation);
    int reducedPos = pos;

    // Walk backwards from specificed position
    for (i = pos-1; i >= DOCSTART; i--) {
      c = text.charAt(i);
      // Check if character is one of the delimiters
      for (j = 0; j < delims.length; j++) {
        if (c == delims[j]) {
          // Move reduced model to walker's location
          _reduced.move(i - reducedPos);
          reducedPos = i;
          
          // Check if matching char is in comment or quotes
          ReducedModelState state = _reduced.getStateAtCurrent();
          if (!state.equals(ReducedModelState.FREE)
                || _isStartOfComment(text, i)
                || ((i > 0) && _isStartOfComment(text, i - 1))) {
            // Ignore matching char
          } else {
            // Found a matching char, check if we should ignore it
            if (skipParenPhrases && posInParenPhrase()) {
              // In a paren phrase, so ignore
            }
            else {
              // Return position of matching char
              _reduced.move(origLocation - i);
              _storeInCache(key, new Integer(i));
              return i;
            }
          }
          //_reduced.move(pos - i);
        }
      }
    }
    _reduced.move(origLocation - reducedPos);
    
    _storeInCache(key, new Integer(ERROR_INDEX));
    return ERROR_INDEX;
  }

  /**
   * Returns true if the given position is inside a paren phrase.
   * @param pos the position we're looking at
   * @return true if pos is immediately inside parentheses
   */
  public synchronized boolean posInParenPhrase(int pos) {
    // Check cache
    String key = "posInParenPhrase:" + pos;
    Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) {
      return cached.booleanValue();
    }
    
    int here = _currentLocation;
    _reduced.move(pos - here);
    boolean inParenPhrase = posInParenPhrase();
    _reduced.move(here - pos);
    
    _storeInCache(key, new Boolean(inParenPhrase));
    return inParenPhrase;
  }
  
  /**
   * Returns true if the reduced model's current position is inside a paren phrase.
   * @return true if pos is immediately inside parentheses
   */
  public synchronized boolean posInParenPhrase() {
    IndentInfo info = _reduced.getIndentInformation();
    return info.braceTypeCurrent.equals(IndentInfo.openParen);
  }

  /**
   * Returns true if the given position is not inside a paren/brace/etc phrase.
   * @param pos the position we're looking at
   * @return true if pos is immediately inside a paren/brace/etc
   */
  protected synchronized boolean posNotInBlock(int pos) {
    // Check cache
    String key = "posNotInBlock:" + pos;
    Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) {
      return cached.booleanValue();
    }

    int here = _currentLocation;
    _reduced.move(pos - here);
    IndentInfo info = _reduced.getIndentInformation();
    boolean notInParenPhrase = info.braceTypeCurrent.equals(IndentInfo.noBrace);
    _reduced.move(here - pos);
    _storeInCache(key, new Boolean(notInParenPhrase));
    return notInParenPhrase;
  }

  /**
   * Returns the indent level of the start of the statement
   * that the cursor is on.  Uses a default set of delimiters.
   * (';', '{', '}') and a default set of whitespace characters 
   * (' ', '\t', n', ',')
   * @param pos Cursor position
   */
  public String getIndentOfCurrStmt(int pos) throws BadLocationException {
    char[] delims = {';', '{', '}'};
    char[] whitespace = {' ', '\t', '\n',','};
    return getIndentOfCurrStmt(pos, delims, whitespace);
  }
  
  /**
   * Returns the indent level of the start of the statement
   * that the cursor is on.  Uses a default set of whitespace characters.
  * (' ', '\t', '\n', ',')
   * @param pos Cursor position
   */
  public String getIndentOfCurrStmt(int pos, char[] delims) throws BadLocationException {
    char[] whitespace = {' ', '\t', '\n',','};
    return getIndentOfCurrStmt(pos, delims, whitespace);
  }

  /**
   * Returns the indent level of the start of the statement
   * that the cursor is on.
   * @param pos Cursor position
   * @param delims Delimiter characters denoting end of statement
   * @param whitespace characters to skip when looking for beginning of next statement
   */
  public String getIndentOfCurrStmt(int pos, char[] delims, char[] whitespace) throws BadLocationException {
    // Check cache
    String key = "getIndentOfCurrStmt:" + pos;
    for (int i=0; i < delims.length; i++) {
      key += ":" + delims[i];
    }
    //long start = System.currentTimeMillis();
    String cached = (String) _checkCache(key);
    if (cached != null) {
      return cached;
    }
    
    // Get the start of the current line
    int lineStart = getLineStartPos(pos);

    // Find the previous delimiter that closes a statement
    boolean reachedStart = false;
    boolean ignoreParens;
    int prevDelimiter = lineStart;
    
    //long mid = System.currentTimeMillis();  // START STAGE 2
    do {
      ignoreParens = posInParenPhrase(prevDelimiter);
      prevDelimiter = findPrevDelimiter(prevDelimiter, delims, ignoreParens);
      try {
        if ((prevDelimiter > 0) && (prevDelimiter < getLength()) &&
            (getText(prevDelimiter,1).charAt(0) == '{')) {
          break;
        }
      }
      catch (BadLocationException e) {
        // Shouldn't happen
        throw new UnexpectedException(e);
      }
      // Check delimiter found was start of document
      if(prevDelimiter == ERROR_INDEX) {
        reachedStart = true;
        break;
      }
    } while(posInParenPhrase(prevDelimiter));  // this is being calculated twice...
    //long mid2 = System.currentTimeMillis();  // START STAGE 3

    // From the previous delimiter, find the next non-whitespace character
    int nextNonWSChar;
    if(reachedStart) {
      nextNonWSChar = getFirstNonWSCharPos(DOCSTART);
    }
    else {
      nextNonWSChar = getFirstNonWSCharPos(prevDelimiter+1,whitespace);
    }
    //long mid3 = System.currentTimeMillis();  // START STAGE 4
    
    // If the end of the document was reached
    if(nextNonWSChar == ERROR_INDEX) {
      nextNonWSChar = getLength();
    }

    // Get the start of the line of the non-ws char
    int lineStartStmt = getLineStartPos(nextNonWSChar);

    // Get the position of the first non-ws character on this line
    int lineFirstNonWS = getLineFirstCharPos(lineStartStmt);
    String lineText = "";
    try {
      lineText = getText(lineStartStmt, lineFirstNonWS - lineStartStmt);
    }
    catch(BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    } 
    
    _storeInCache(key, lineText);
    /*
    long end = System.currentTimeMillis();
    if (maxKey.equals(key)) {
      DrJava.consoleOut().print("     getIndent: [" + (mid-start));
      DrJava.consoleOut().print("] (" + (mid2-start));
      DrJava.consoleOut().print(") [" + (mid3-start));
      DrJava.consoleOut().println("] total: " + (end-start) + "ms");
    }
    */
    return lineText;
  }

  /**
   * Determines if the given character exists on the line where
   * the given cursor position is. Does not search in quotes or comments.
   * <p>
   * <b>Does not work if character being searched for is a '/' or a '*'</b>
   * @param pos Cursor position 
   * @param findChar Character to search for
   * @return true if this node's rule holds.
   */
  public int findCharOnLine(int pos, char findChar) {
    // Check cache
    String key = "findCharOnLine:" + pos + ":" + findChar;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
      return cached.intValue();
    }

    int here = _currentLocation;
    int lineStart = this.getLineStartPos(pos);
    int lineEnd = this.getLineEndPos(pos);
    String lineText;
    
    try {
      lineText = this.getText(lineStart, lineEnd - lineStart);
    } catch(BadLocationException e) {
      // Should not be here
      throw new UnexpectedException(e);
    }
    
    int i = lineText.indexOf(findChar, 0);
    
    // Move to start of line
    /*
    _reduced.move(lineStart - here);
    int reducedPos = lineStart;
    int prevI = 0;
    */
    
    while(i != -1) {
      // Move reduced model to walker's location
      int matchIndex = i + lineStart;
      _reduced.move(matchIndex - here);
      //int dist = i - prevI;
      //_reduced.move(dist);
      //reducedPos = reducedPos + dist;
      
      // Check if matching char is in comment or quotes
      if (!_reduced.getStateAtCurrent().equals(ReducedModelState.FREE)) {
        // Ignore matching char
      } else {
        // Return position of matching char
        //_reduced.move(here - reducedPos);
        _reduced.move(here - matchIndex);
        _storeInCache(key, new Integer(matchIndex));
        return matchIndex;
      }
      _reduced.move(here - matchIndex);
      
      //prevI = i;
      i = lineText.indexOf(findChar, i+1);
    }
    
    //_reduced.move(here - reducedPos);
    _storeInCache(key, new Integer(ERROR_INDEX));
    return ERROR_INDEX;
  }
  
  /**
   * Returns the absolute position of the beginning of the
   * current line.  (Just after most recent newline, or DOCSTART)
   * Doesn't ignore comments.
   * @param pos Any position on the current line
   * @return position of the beginning of this line
   */
  public synchronized int getLineStartPos(int pos) {
    if (pos < 0 || pos > getLength()) {
      return -1;
    }
    // Check cache
    String key = "getLineStartPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
      return cached.intValue();
    }
    
    int location = _currentLocation;
    _reduced.move(pos - location);
    int dist = _reduced.getDistToPreviousNewline(0);
    _reduced.move(location - pos);
    if(dist == -1) {
      // If no previous newline was found
      // return DOCSTART
      _storeInCache(key, new Integer(DOCSTART));
      return DOCSTART;
    }
    else {
      _storeInCache(key, new Integer(pos - dist));
      return pos - dist;
    }
  }
  
  /**
   * Returns the absolute position of the end of the current
   * line.  (At the next newline, or the end of the document.)
   * @param pos Any position on the current line
   * @return position of the end of this line
   */
  public synchronized int getLineEndPos(int pos) {
    if (pos < 0 || pos > getLength()) {
      return -1;
    }
    
    // Check cache
    String key = "getLineEndPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
      return cached.intValue();
    }
    
    int location = _currentLocation;
    _reduced.move(pos - location);
    int dist = _reduced.getDistToNextNewline();
    _reduced.move(location - pos);
    _storeInCache(key, new Integer(pos + dist));
    return pos + dist;
  }

  /**
   * Returns the absolute position of the first non-whitespace character
   * on the current line.
   * NB: Doesn't ignore comments.
   * @param pos position on the line
   * @return position of first non-whitespace character on this line, or the end
   * of the line if no non-whitespace character is found.
   */
  public int getLineFirstCharPos(int pos) throws BadLocationException {
    // Check cache
    String key = "getLineFirstCharPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
      return cached.intValue();
    }
    
    int startLinePos = getLineStartPos(pos);
    int endLinePos = getLineEndPos(pos);
    
    // Get all text on this line
    String text = this.getText(startLinePos, endLinePos - startLinePos);
    int walker = 0;
    while (walker < text.length()) {
      if (text.charAt(walker) == ' ' || 
          text.charAt(walker) == '\t') {
            walker++;
      }
      else {
        _storeInCache(key, new Integer(startLinePos + walker));
        return startLinePos + walker;
      }
    }
    // No non-WS char found, so return last position on line
    _storeInCache(key, new Integer(endLinePos));
    return endLinePos;
  }

  /**
   * Finds the position of the first non-whitespace character after pos.
   * NB: Skips comments and all whitespace, including newlines
   * @param pos Position to start from
   * @return position of first non-whitespace character after pos,
   * or ERROR_INDEX if end of document is reached
   */
  public int getFirstNonWSCharPos(int pos) throws BadLocationException {
    char[] whitespace = {' ', '\t', '\n'};
    return getFirstNonWSCharPos(pos,whitespace);
  }
  /**
   * Finds the position of the first non-whitespace character after pos.
   * NB: Skips comments and all whitespace, including newlines
   * @param pos Position to start from
   * @param whitespace array of whitespace chars to ignore
   * @return position of first non-whitespace character after pos,
   * or ERROR_INDEX if end of document is reached
   */
  public synchronized int getFirstNonWSCharPos(int pos,char[] whitespace)
    throws BadLocationException
  {
    // Check cache
    String key = "getFirstNonWSCharPos:" + pos;
    for (int i=0; i < whitespace.length; i++) {
      key += ":" + whitespace[i];
    }
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
      return cached.intValue();
    }
    
    int j, i;
    char c;
    int endPos = getLength();
    
    // Get text from pos to end of document
    String text = getText(pos, endPos - pos);
    
    final int origLocation = _currentLocation;
    // Move reduced model to location pos
    _reduced.move(pos - origLocation);
    int reducedPos = pos;
    
    //int iter = 0;
    
    // Walk forward from specificed position
    for (i = pos; i != endPos; i++) {
      //iter++;
      boolean isWhitespace = false;
      c = text.charAt(i - pos);
      // Check if character is whitespace
      for (j = 0; j < whitespace.length && !isWhitespace; j++) {
        if (c == whitespace[j]) {
          isWhitespace = true;
        }
      }
      if (!isWhitespace) {
        // Move reduced model to walker's location
        _reduced.move(i - reducedPos);
        reducedPos = i;
        
        // Check if non-ws char is in comment
        if((_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_LINE_COMMENT)) ||
           (_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_BLOCK_COMMENT))) {
          // Ignore non-ws char
          
          // Move to next token?  (requires making getBlockOffset public)
          //  doesn't work yet
          /*
          int tokenSize = _reduced.currentToken().getSize();
          int offset = _reduced.getBlockOffset();
          //DrJava.consoleOut().println("     token len: " + tokenSize +
          //                            ", offset: " + offset);
          //DrJava.consoleOut().println("     token before: " + _reduced.currentToken().getState());
          _reduced.move(tokenSize - offset);
          i += tokenSize - offset;
          //DrJava.consoleOut().println("     token after: " + _reduced.currentToken().getState());
          */
        }
        else { 
          if(_isStartOfComment(text, i - pos)) {
            // Move i past the start of comment characters
            // and continue searching
            i = i + 1;
            _reduced.move(1);
            reducedPos = i;
          }
          else {
            // Return position of matching char
            _reduced.move(origLocation - i);
            _storeInCache(key, new Integer(i));
            return i;
          }
        }
      }
    }
    //DrJava.consoleOut().println("getFirstNonWS iterations: " + iter);
    
    _reduced.move(origLocation - reducedPos);
    _storeInCache(key, new Integer(ERROR_INDEX));
    return ERROR_INDEX;
  }
  public int findPrevNonWSCharPos(int pos) throws BadLocationException {
    char[] whitespace = {' ', '\t', '\n'};
    return findPrevCharPos(pos, whitespace);
  }
  
  /**
   * Returns the offset corresponding to the first character of the given line number, 
   *  or -1 if the lineNum is not found.
   * @param lineNum the line number for which to calculate the offset.
   * @return the offset of the first character in the given line number
   */
  public int getOffset(int lineNum) {
    
    try {
      if (lineNum < 0) {
        return -1;
      }
      String defsText = getText(0, getLength());
      
      int curLine = 1;
      int offset = 0; // offset is number of chars from beginning of file
      
      // offset is always pointing to the first character in a line
      // at the top of the loop
      while (offset < defsText.length()) {
        
        if (curLine==lineNum) {
      
          return offset;
        }
        
        int nextNewline = defsText.indexOf('\n', offset);
        if (nextNewline == -1) {
         
          // end of the document, and couldn't find the supplied lineNum
          return -1;
        }
        else {
          curLine++;
          offset = nextNewline + 1;
        } 
      }
    
      return -1;
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }
  
  /**
   * Finds the position of the first non-whitespace character before pos.
   * NB: Skips comments and all whitespace, including newlines
   * @param pos Position to start from
   * @param whitespace chars considered as white space
   * @return position of first non-whitespace character before pos,
   * or ERROR_INDEX if begining of document is reached
   */
  public synchronized int findPrevCharPos(int pos, char[] whitespace)
    throws BadLocationException
  {
    // Check cache
    String key = "findPrevCharPos:" + pos;
    for (int i=0; i < whitespace.length; i++) {
      key += ":" + whitespace[i];
    }
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
      return cached.intValue();
    }
    
    int j, i;
    char c;
    String text = getText(0, pos);
    
    final int origLocation = _currentLocation;
    // Move reduced model to location pos
    _reduced.move(pos - origLocation);
    int reducedPos = pos;
    
    // Walk backward from specified position
    for (i = pos-1; i >= 0; i--) {
      boolean isWhitespace = false;
      c = text.charAt(i);
      // Check if character is whitespace
      for (j = 0; j < whitespace.length; j++) {
        if (c == whitespace[j]) {
          isWhitespace = true;
        }
      }
      if (!isWhitespace) {
        // Move reduced model to walker's location
        _reduced.move(i - reducedPos);
        reducedPos = i;
        
        // Check if matching char is in comment
        if((_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_LINE_COMMENT)) ||
           (_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_BLOCK_COMMENT))) {
          // Ignore matching char
        }
        else { 
          if(_isEndOfComment(text, i)) {
            // Move i past the start of comment characters
            // and continue searching
            i = i - 1;
            _reduced.move(-1);
            reducedPos = i;
          }
          else {
            // Return position of matching char
            _reduced.move(origLocation - i);
            _storeInCache(key, new Integer(i));
            return i;
          }
        }
      }
    }
    _reduced.move(origLocation - reducedPos);
    _storeInCache(key, new Integer(ERROR_INDEX));
    return ERROR_INDEX;
  }

  /**
   * Helper method for getFirstNonWSCharPos
   * Determines whether the current character is the start
   * of a comment: "/*" or "//"
   */
  protected boolean _isStartOfComment(String text, int pos) {
    char currChar = text.charAt(pos);
    if(currChar == '/') {
      try {
        char afterCurrChar = text.charAt(pos + 1);
        if((afterCurrChar == '/') || (afterCurrChar == '*')) {
          return true;
        }
      } catch (StringIndexOutOfBoundsException e) {
      }
    }
    return false;
  }

  /**
   * Helper method for findPrevNonWSCharPos
   * Determines whether the current character is the end
   * of a comment: "*\/" or a hanging "//"
   * @return true if (pos-1,pos) == '*\/' or '//'
   */
  protected boolean _isEndOfComment(String text, int pos) {
    char currChar = text.charAt(pos);
    if(currChar == '/') {
      try {
        char beforeCurrChar = text.charAt(pos - 1);
        if((beforeCurrChar == '/') || (beforeCurrChar == '*')) {
          return true;
        }
      } catch (StringIndexOutOfBoundsException e) {
      }
    }
    return false;
  }
  
  /**
   * Returns true iff tabs are to removed on text insertion.
   */
  public boolean tabsRemoved() {
    return _tabsRemoved;
  }

  /**
   * Forwarding method to find the match for the closing brace
   * immediately to the left, assuming there is such a brace.
   * @return the relative distance backwards to the offset before
   *         the matching brace.
   */
  public synchronized int balanceBackward() {
    return _reduced.balanceBackward();
  }
  
  /**
   * Forwarding method to find the match for the open brace
   * immediately to the right, assuming there is such a brace.
   * @return the relative distance forwards to the offset after
   *         the matching brace.
   */
  public synchronized int balanceForward() {
    return _reduced.balanceForward();
  }

  public void indentLines(int selStart, int selEnd){
    indentLines(selStart, selEnd, Indenter.OTHER);
  }
  public void indentLines(int selStart, int selEnd, int reason) {
    //long start = System.currentTimeMillis();
    try {
      // Begins a compound edit. 
      int key = _undoManager.startCompoundEdit();
      
      if (selStart == selEnd) {
        Position oldCurrentPosition = createPosition(_currentLocation);
        _indentLine(reason);
        //int caretPos = getCaretPosition();
        //_doc().setCurrentLocation(caretPos);
        setCurrentLocation(oldCurrentPosition.getOffset());
        int space = getWhiteSpace();
        move(space);
        //setCaretPosition(caretPos + space);
      }
      else {
        _indentBlock(selStart, selEnd, reason);
      }
      // Ends the compound edit.
      _undoManager.endCompoundEdit(key);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
    
    //long end = System.currentTimeMillis();
    //DrJava.consoleOut().println("Elapsed Time (sec): " + ((end-start)/1000));
    //DrJava.consoleOut().println("   Cache size: " + _helperCache.size());
    //DrJava.consoleOut().println("   Cache History size: " + _helperCacheHistory.size());
    
    //DrJava.consoleOut().println("   Longest: " + maxHelpDelay + "ms from " + maxKey);
    //maxHelpDelay = 0;  maxKey = "none";
    
  }

  /**
   * Indents the lines between and including the lines containing
   * points start and end.
   * @param start Position in document to start indenting from
   * @param end Position in document to end indenting at
   * @param reason the user action spawning this indent
   */
  private synchronized void _indentBlock(final int start, final int end, int reason) {
    //DrJava.consoleOut().println("indenting block of " + (end-start));
    try {
      // Keep marker at the end. This Position will be the
      // correct endpoint no matter how we change the doc
      // doing the indentLine calls.
      final Position endPos = this.createPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = start;
      while (walker < endPos.getOffset()) {
        setCurrentLocation(walker);
        // Keep pointer to walker position that will stay current
        // regardless of how indentLine changes things
        Position walkerPos = this.createPosition(walker);
        // Indent current line
        _indentLine(reason);
        // Move back to walker spot
        setCurrentLocation(walkerPos.getOffset());
        walker = walkerPos.getOffset();
        // Adding 1 makes us point to the first character AFTER the next newline.
        // We don't actually move yet. That happens at the top of the loop,
        // after we check if we're past the end.
        walker += _reduced.getDistToNextNewline() + 1;
        //DrJava.consoleOut().println("progress: " + (100*(walker-start)/(end-start)));
      }
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
  }

  /**
   * Indents a line using the Indenter decision tree.  Package private for testing purposes
   */
void _indentLine(int reason) {
    _indenter.indent(this, reason);
  }
  
  /**
   * Comments out all lines between selStart and selEnd, inclusive.
   * The current cursor position is maintained after the operation.
   * @param selStart the document offset for the start of the selection
   * @param selEnd the document offset for the end of the selection
   */
  public void commentLines(int selStart, int selEnd) {
    try {
      int key = _undoManager.startCompoundEdit();
      if (selStart == selEnd) {
        Position oldCurrentPosition = createPosition(_currentLocation);
        _commentLine();
        //int caretPos = getCaretPosition();
        //_doc().setCurrentLocation(caretPos);
        setCurrentLocation(oldCurrentPosition.getOffset());
      }
      else {
        _commentBlock(selStart, selEnd);
      }
      _undoManager.endCompoundEdit(key);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Comments out the lines between and including the lines containing
   * points start and end, using wing comments -- "// ".
   * @param start Position in document to start commenting from
   * @param end Position in document to end commenting at
   */
  private synchronized void _commentBlock(final int start, final int end) {
    //DrJava.consoleOut().println("commenting out block of " + (end-start));
    try {
      // Keep marker at the end. This Position will be the
      // correct endpoint no matter how we change the doc
      // doing the indentLine calls.
      final Position endPos = this.createPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = start;
      while (walker < endPos.getOffset()) {
        setCurrentLocation(walker);
        // Keep pointer to walker position that will stay current
        // regardless of how commentLine changes things
        Position walkerPos = this.createPosition(walker);
        // Comment out current line
        _commentLine();
        
        // Move back to walker spot
        setCurrentLocation(walkerPos.getOffset());
        walker = walkerPos.getOffset();
        // Adding 1 makes us point to the first character AFTER the next newline.
        // We don't actually move yet. That happens at the top of the loop,
        // after we check if we're past the end.
        walker += _reduced.getDistToNextNewline() + 1;
        //DrJava.consoleOut().println("progress: " + (100*(walker-start)/(end-start)));
      }
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Comments out a single line with wing comments -- "// ".
   */
  private void _commentLine() {
    // Insert "// " at the beginning of the line.
    // Using null for AttributeSet follows convention in this class.
    try {
      insertString(getCurrentLocation() - getCurrentCol(), "// ", null);
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Uncomments all lines between selStart and selEnd, inclusive.
   * The current cursor position is maintained after the operation.
   * @param selStart the document offset for the start of the selection
   * @param selEnd the document offset for the end of the selection
   */
  public void uncommentLines(int selStart, int selEnd) {
    try {
      int key = _undoManager.startCompoundEdit();
      if (selStart == selEnd) {
        Position oldCurrentPosition = createPosition(_currentLocation);
        _uncommentLine();
        //int caretPos = getCaretPosition();
        //_doc().setCurrentLocation(caretPos);
        setCurrentLocation(oldCurrentPosition.getOffset());
      }
      else {
        _uncommentBlock(selStart, selEnd);
      }
      _undoManager.endCompoundEdit(key);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Uncomments all lines between and including the lines containing
   * points start and end.
   * @param start Position in document to start commenting from
   * @param end Position in document to end commenting at
   */
  private synchronized void _uncommentBlock(final int start, final int end) {
    //DrJava.consoleOut().println("uncommenting block of " + (end-start));
    try {
      // Keep marker at the end. This Position will be the
      // correct endpoint no matter how we change the doc
      // doing the indentLine calls.
      final Position endPos = this.createPosition(end);
      // Iterate, line by line, until we get to/past the end
      int walker = start;
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
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Uncomments a single line.  This simply looks for a leading "//".
   * Also indents the line, once the comments have been removed.
   */
  private void _uncommentLine() {
    try {
      // Look for "//" at the beginning of the line, and remove it.
      int curCol = getCurrentCol();
      int lineStart = getCurrentLocation() - curCol;
      String text = getText(lineStart, curCol + _reduced.getDistToNextNewline());
      int pos = text.indexOf("//");
      
//      System.out.println("" + getCurrentLocation() + " " + curCol + " " 
//                           + text + " " + pos + " " + _reduced.getDistToNextNewline());
      
      // Look for any non-whitespace chars before the "//" on the line.
      boolean goodWing = true;
      for (int i = pos-1; ((i >= 0) && goodWing); i--) {
        char c = text.charAt(i);
        // If a previous char is not whitespace, we're not looking at a wing comment.
        if (!((c == ' ') || (c == ' ') || (c == ' '))) {
          goodWing = false;
        }
      }
      
      // If a wing comment wasn't found, or if the wings aren't the first
      // non-whitespace characters on the line, do nothing.
      if ((pos >= 0) && goodWing) {
        // Otherwise, remove the wings and indent.
        remove(lineStart + pos, 2);
        _indentLine(Indenter.OTHER);
      }
    } catch (BadLocationException e) {
      // Should not happen
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Indents a line in accordance with the rules that DrJava has set up.
   *
   * This is the old version, which has been replaced by the indent
   * rule decision tree.
   *
  private void _indentLine() {
    try {
      // moves us to the end of the line
      move(_reduced.getDistToNextNewline());
      IndentInfo ii = _reduced.getIndentInformation();
      String braceType = ii.braceType;
      int distToNewline = ii.distToNewline;
      int distToBrace = ii.distToBrace;
      int distToPrevNewline = ii.distToPrevNewline;
      int tab = 0;
      boolean isSecondLine = false;
      if (distToNewline == -1) {
        distToNewline = _currentLocation;
        isSecondLine = true;
      }
      if (distToPrevNewline == -1)              //only on the first line
        tab = 0;
      //takes care of the second line
      else if (this._currentLocation - distToPrevNewline < 2)
        tab = 0;
      else if (distToBrace == -1)
        tab = _indentSpecialCases(0, distToPrevNewline);
      else if (braceType.equals("("))
        tab = distToNewline - distToBrace + 1;
      else if (braceType.equals("{")) {
        tab = getWhiteSpaceBetween(distToNewline, distToBrace) + _indent;
        tab = _indentSpecialCases(tab, distToPrevNewline);
      }
      else if (braceType.equals("["))
        tab = distToNewline - distToBrace + 1;
      tab(tab, distToPrevNewline);
    } catch (BadLocationException e) {
      throw  new UnexpectedException(e);
    }
  }*/

  /**
   * Deals with the special cases.
   * If the first character after the previous \n is a } then -2
   *
   * Replaced by indent rule decision tree.
   *
   * @exception BadLocationException
   *
  private int _indentSpecialCases(int tab, int distToPrevNewline) throws BadLocationException {
    //not a special case.
    if (distToPrevNewline == -1)
      return  tab;
    //setup
    int start = _reduced.getDistToPreviousNewline(distToPrevNewline + 1);
    if (start == -1)
      start = 0;
    else
      start = _currentLocation - start;
    String text = this.getText(start, _currentLocation - start);
    //case of  }
    int length = text.length();
    int k = length - distToPrevNewline;
    while (k < length && text.charAt(k) == ' ')
      k++;
    if (k < length && text.charAt(k) == '}')
      tab -= _indent;
    // if no matching { then let offset be 0.
    if (tab < 0)
      tab = 0;
    //non-normal endings
    int i = length - distToPrevNewline - 2;
    int distanceMoved = distToPrevNewline + 2;
    move(-distToPrevNewline - 2);               //assumed: we are at end of a line.
    while (i >= 0 && _isCommentedOrSpace(i, text)) {
      i--;
      if (i > 0) {              //gaurentees you don't move into document Start.
        distanceMoved++;
        move(-1);
      }
    }
    move(distanceMoved);        //move the document bac.
    if (i >= 0 && !(_normEndings.contains(text.substring(i, i + 1)))) {
      int j = 0;
      while ((j < length) && (text.charAt(j) == ' '))
        j++;
      if ((k < length) && (text.charAt(k) == '{')) {
        if ((j < length) && (text.charAt(j) == '{'))
          tab = j + _indent;
        else
          tab = j;
      }
      else
        tab = j + _indent;
    }
    //return tab
    return  tab;
  }*/

  /**
   * Determines if the current token is part of a comment or if the i'th character
   * in the given text argument is a space.
   * @param i the index to look at for the space in text
   * @param text a block of text
   * @return true if the conditions are met
   */
  private synchronized boolean _isCommentedOrSpace(int i, String text) {
    ReducedToken rt = _reduced.currentToken();
    String type = rt.getType();
    return  (rt.isCommented() || type.equals("//") || type.equals("/*") || type.equals("*/")
        || (text.charAt(i) == ' '));
  }

  /**
   * Gets the number of whitespace characters between the current location and the rest of
   * the document or the first non-whitespace character, whichever comes first.
   * @return the number of whitespace characters
   */
  public int getWhiteSpace() {
    try {
      return  getWhiteSpaceBetween(0, getLength() - _currentLocation);
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return  -1;
  }

  /**
   *Starts at start and gets whitespace starting at relStart and either
   *stopping at relEnd or at the first non-white space char.
   *NOTE: relStart and relEnd are relative to where we are in the document
   *relStart must be <= _currentLocation
   * @exception BadLocationException
   */
  private int getWhiteSpaceBetween(int relStart, int relEnd) throws BadLocationException {
    String text = this.getText(_currentLocation - relStart, Math.abs(relStart -
        relEnd));
    int i = 0;
    int length = text.length();
    while ((i < length) && (text.charAt(i) == ' '))
      i++;
    return  i;
  }

  /**
   * The function that handles what happens when a tab key is pressed.
   * It is given the size of the leading whitespace and based on the
   * current indent information, either shrinks or expands that whitespace.
   * @param tab number of indents, i.e., level of nesting
   * @param distToPrevNewline distance to end of previous line
   * @exception BadLocationException
   */
  void tab(int tab, int distToPrevNewline) throws BadLocationException {
    if (distToPrevNewline == -1) {
      distToPrevNewline = _currentLocation;
    }
    int currentTab = getWhiteSpaceBetween(distToPrevNewline, 0);
    if (tab == currentTab) {
      return;
    }
    if (tab > currentTab) {
      String spaces = "";

      for (int i = 0; i < tab - currentTab; i++) {
        spaces = spaces + " ";
      }

      insertString(_currentLocation - distToPrevNewline, spaces, null);
    }
    else {
      remove(_currentLocation - distToPrevNewline, currentTab - tab);
    }
  }
  
  /**
   * Sets the text between the previous newline and the first non-whitespace
   * character of the line containing pos to tab.
   * @param tab String to be placed between previous newline and first
   * non-whitespace character
   */
  public void setTab(String tab, int pos) {
    try {
      int startPos = getLineStartPos(pos);
      int firstNonWSPos = getLineFirstCharPos(pos);
      int len = firstNonWSPos - startPos;
      
      // Adjust prefix
      boolean onlySpaces = _hasOnlySpaces(tab);
      if (!onlySpaces || (len != tab.length())) {
        
        if (onlySpaces) {
          // Only add or remove the difference
          int diff = tab.length() - len;
          if (diff > 0) {
            insertString(firstNonWSPos, tab.substring(0, diff), null);
          }
          else {
            remove(firstNonWSPos + diff, -diff);
          }
        }
        else {
          // Remove the whole prefix, then add the new one
          remove(startPos, len);
          insertString(startPos, tab, null);
        }
      }
    }
    catch (BadLocationException e) {
      // Should never see a bad location
      throw new UnexpectedException(e);
    }
  }
  
  /**
   * Returns whether the given text only has spaces.
   */
  private boolean _hasOnlySpaces(String text) {
    return (text.trim().length() == 0);
  }

  /**
   * Return all highlight status info for text between start and end.
   * This should collapse adjoining blocks with the same status into one.
   */
  public synchronized Vector<HighlightStatus> getHighlightStatus(int start, int end) {
    // First move the reduced model to the start
    int oldLocation = _currentLocation;
    setCurrentLocation(start);

    // Now ask reduced model for highlight status for chars till end
    Vector<HighlightStatus> v =
      _reduced.getHighlightStatus(start, end - start);

    // Go through and find any NORMAL blocks
    // Within them check for keywords
    for (int i = 0; i < v.size(); i++) {
      HighlightStatus stat = v.elementAt(i);

      if (stat.getState() == HighlightStatus.NORMAL) {
        i = _highlightKeywords(v, i);
      }
    }

    // bstoler: Previously we moved back to the old location. This was
    // very bad and severly slowed down rendering when scrolling.
    // This is because parts are rendered in order. Thus, if old location is
    // 0, but now we've scrolled to display 100000-100100, if we keep
    // jumping back to 0 after getting every bit of highlight, it slows
    // stuff down incredibly.
    //setCurrentLocation(oldLocation);
    return v;
  }

  /**
   * Separates out keywords from normal text for the given
   * HighlightStatus element.
   *
   * What this does is it looks to see if the given part of the text
   * contains a keyword. If it does, it splits the HighlightStatus into
   * separate blocks so that each keyword is in its own block.
   * This will find all keywords in a given block.
   *
   * Note that the given block must have state NORMAL.
   *
   * @param v Vector with highlight info
   * @param i Index of the single HighlightStatus to check for keywords in
   * @return the index into the vector of the last processed element
   */
  private int _highlightKeywords(Vector<HighlightStatus> v, int i) {
    // Basically all non-alphanumeric chars are delimiters
    final String delimiters = " \t\n\r{}()[].+-/*;:=!@#$%^&*~<>?,\"`'<>|";
    final HighlightStatus original = v.elementAt(i);
    final String text;

    try {
      text = getText(original.getLocation(), original.getLength());
    }
    catch (BadLocationException e) {
      e.printStackTrace();
      throw new RuntimeException(e.toString());
    }

    // Because this text is not quoted or commented, we can use the simpler
    // tokenizer StringTokenizer.
    // We have to return delimiters as tokens so we can keep track of positions
    // in the original string.
    StringTokenizer tokenizer = new StringTokenizer(text, delimiters, true);

    // start and length of the text that has not yet been put back into the
    // vector.
    int start = original.getLocation();
    int length = 0;

    // Remove the old element from the vector.
    v.removeElementAt(i);

    // Index where we are in the vector. It's the location we would insert
    // new things into.
    int index = i;

    boolean process = false;
    int state = 0;
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();

      //first check to see if we need highlighting
      process = false;
      if (_isType(token)) {
        //right now keywords incl prim types, so must put this first
        state = HighlightStatus.TYPE;
        process = true;
      } else if (_keywords.contains(token)) {
        state = HighlightStatus.KEYWORD;
        process = true;
      } else if (_isNum(token)) {
        state = HighlightStatus.NUMBER;
        process = true;
      }

      if (process) {
        // first check if we had any text before the token
        if (length != 0) {
          HighlightStatus newStat =
            new HighlightStatus(start,
                                length,
                                original.getState());
          v.insertElementAt(newStat, index);
          index++;
          start += length;
          length = 0;
        }

        // Now pull off the keyword
        int keywordLength = token.length();
        v.insertElementAt(new HighlightStatus(start,
                                              keywordLength,
                                              state),
                          index);
        index++;
        // Move start to the end of the keyword
        start += keywordLength;
      }
      else {
        // This is not a keyword, so just keep accumulating length
        length += token.length();
      }
    }
    // Now check if there was any text left after the keywords.
    if (length != 0) {
      HighlightStatus newStat =
        new HighlightStatus(start,
                            length,
                            original.getState());
      v.insertElementAt(newStat, index);
      index++;
      length = 0;
    }
    // return one before because we need to point to the last one we inserted
    return index - 1;
    }

  /**
   * Checks to see if the current string is a number
   * @return true if x is a parseable number
   */
  private boolean _isNum(String x) {
    try {
      Double.parseDouble(x);
      return true;
      
    } catch (NumberFormatException e) {
      return false;
    }
  }
  
  /**
   * Checks to see if the current string is a type
   * A type is assumed to be a primitive type OR
   * anything else that begins with a capitalized character
   */
  private boolean _isType(String x) {
    if (_primTypes.contains(x)) return true;
    
    try {
      return Character.isUpperCase(x.charAt(0));
    } catch (IndexOutOfBoundsException e) {
      return false;
    }
  }

  /**
   * Goes to a particular line in the document.
   */
  public synchronized void gotoLine(int line) {
    int dist;
    if (line < 0) {
     return;
    }
    int actualLine =1;
    setCurrentLocation(0);
    for (int i = 1; (i < line) && (_currentLocation < getLength()); i++) {
      dist = _reduced.getDistToNextNewline();
      if (_currentLocation + dist < getLength()) {
        dist++;
      }
      actualLine++;
      move(dist);
    }
    _cachedLineNum = actualLine;
    _cachedLocation = _currentLocation;
    _cachedPrevLineLoc = getLineStartPos(_currentLocation);
    _cachedNextLineLoc = getLineEndPos(_currentLocation);
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
  public synchronized String getPackageName() throws InvalidPackageException {
    // Where we'll build up the package name we find
    StringBuffer buf = new StringBuffer();

    int oldLocation = getCurrentLocation();

    try {
      setCurrentLocation(0);
      final int docLength = getLength();
      final String text = getText(0, docLength);

      // The location of the first non-whitespace character that
      // is not inside quote or comment.
      int firstNormalLocation = 0;
      while ((firstNormalLocation < docLength)) {
        setCurrentLocation(firstNormalLocation);

        if (_reduced.currentToken().getHighlightState() ==
            HighlightStatus.NORMAL)
        {
          // OK, it's normal -- so if it's not whitespace, we found the spot
          char curChar = text.charAt(firstNormalLocation);
          if (!Character.isWhitespace(curChar)) {
            break;
          }
        }

        firstNormalLocation++;
      }

      // Now there are two possibilities: firstNormalLocation is at
      // the first spot of a non-whitespace character that's NORMAL,
      // or it's at the end of the document.
      if (firstNormalLocation == docLength) {
        return "";
      }

      final int strlen = "package".length();

      final int endLocation = firstNormalLocation + strlen;

      if ((firstNormalLocation + strlen > docLength) ||
          ! text.substring(firstNormalLocation, endLocation).equals("package"))
      {
        // the first normal text is not "package" or there is not enough
        // text for there to be a package statement.
        // thus, there is no valid package statement.
        return "";
      }

      // OK, we must have found a package statement.
      // Now let's find the semicolon. Again, the semicolon must be free.
      int afterPackage = firstNormalLocation + "package".length();

      int semicolonLocation = afterPackage;
      do {
        semicolonLocation = text.indexOf(";", semicolonLocation + 1);

        if (semicolonLocation == -1) {
          throw new InvalidPackageException(firstNormalLocation,
                                            "No semicolon found to terminate " +
                                            "package statement!");
        }

        setCurrentLocation(semicolonLocation);
      }
      while (_reduced.currentToken().getHighlightState() !=
             HighlightStatus.NORMAL);

      // Now we have semicolon location. We'll gather text in between one
      // character at a time for simplicity. It's inefficient (I think?)
      // but it's easy, and there shouldn't be much text between
      // "package" and ";" anyhow.
      for (int walk = afterPackage + 1; walk < semicolonLocation; walk++) {
        setCurrentLocation(walk);

        if (_reduced.currentToken().getHighlightState() ==
            HighlightStatus.NORMAL)
        {
          char curChar = text.charAt(walk);

          if (! Character.isWhitespace(curChar)) {
            buf.append(curChar);
          }
        }
      }

      String toReturn = buf.toString();
      if (toReturn.equals("")) {
        throw new InvalidPackageException(firstNormalLocation,
                                          "Package name was not specified " +
                                          "after the package keyword!");
      }

      return toReturn;
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    finally {
      setCurrentLocation(0);
      setCurrentLocation(oldLocation);
    }
  }

  /**
   * Returns the indent information for the current location.
   */
  public synchronized IndentInfo getIndentInformation() {
    // Check cache
    String key = "getIndentInformation:" + _currentLocation;
    IndentInfo cached = (IndentInfo) _checkCache(key);
    if (cached != null) {
      return cached;
    }
    IndentInfo info = getReduced().getIndentInformation();
    _storeInCache(key, info);
    return info;
  }
  
  public synchronized ReducedModelState stateAtRelLocation(int dist){
    return getReduced().stateAtRelLocation(dist);
  }
  
  public synchronized ReducedModelState getStateAtCurrent(){
    return getReduced().getStateAtCurrent();
  }
  
  public synchronized void resetReducedModelLocation() {
    getReduced().resetLocation();
  }
  
  /**
   * Returns the name of the class or interface enclosing the caret position
   * at the top level.
   * @return Name of enclosing class or interface
   * @throws ClassNameNotFoundException if no enclosing class found
   */
  public synchronized String getEnclosingTopLevelClassName(int pos) throws
    ClassNameNotFoundException
  {
    int oldLocation = getCurrentLocation();
    
    try {
      setCurrentLocation(pos);
      
      IndentInfo info = getIndentInformation();
      
      // Find top level open brace
      int topLevelBracePos = -1;
      String braceType = info.braceTypeCurrent;
      while (!braceType.equals(IndentInfo.noBrace)) {
        if (braceType.equals(IndentInfo.openSquiggly)) {
          topLevelBracePos = getCurrentLocation() - info.distToBraceCurrent;
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
      else {
        prevDelimPos++;
      }
      setCurrentLocation(oldLocation);
      
      // Parse out the class name
      return getNextTopLevelClassName(prevDelimPos, topLevelBracePos);      
    }
    catch (BadLocationException ble) {
      // All positions here should be legal
      throw new UnexpectedException(ble);
    }
    finally {
      setCurrentLocation(oldLocation);
    }
  }
  
  /**
   * Gets the name of the top level class in this source file.
   * This attempts to find the first declaration of a class or interface.
   *
   * @return The name of first class in the file
   * @throws ClassNameNotFoundException if no top level class found
   */
  public String getFirstTopLevelClassName() throws ClassNameNotFoundException {
    return getNextTopLevelClassName(0, getLength());
  }
  
  // note: need to update this to work with pos
  public String getNextTopLevelClassName(int startPos, int endPos)
    throws ClassNameNotFoundException 
  {
    // Where we'll build up the package name we find
    int oldLocation = getCurrentLocation();

    try {
      setCurrentLocation(startPos);
      final int textLength = endPos - startPos;
      final String text = getText(startPos, textLength);
      
      boolean done = false;
      int index = 0;

      int indexOfClass = _findKeywordAtToplevel("class", text, startPos);
      int indexOfInterface = _findKeywordAtToplevel("interface", text, startPos);
      
      if ( indexOfClass > -1 ) {
        
        if (indexOfInterface > -1) {
          // compare indices to find the lesser
          index = (indexOfClass < indexOfInterface) ?
            indexOfClass + "class".length() :
            indexOfInterface + "interface".length();     
        }
        else {
          //top level class declaration found
          index = indexOfClass + "class".length();
        }
      }
      else {
        
        if (indexOfInterface > -1) {
          index = indexOfInterface + "interface".length();
        }
        else { 
          // neither index was valid
          throw new ClassNameNotFoundException("No top level class name found");
        }
      }
      
      //if we make it here we have a valid index
      
      //first find index of first non whitespace (from the index in document)
      index = getFirstNonWSCharPos(startPos + index) - startPos;
      if (index == -1) {
        throw new ClassNameNotFoundException("No top level class name found");
      }
      
      int endIndex = textLength; //just in case no whitespace at end of file

      //find index of next delimiter or whitespace
      char c;
      done = false;
      for (int i=index; i < textLength && !done; i++) {
        c = text.charAt(i);
        if (!Character.isJavaIdentifierPart(c)) {
          endIndex = i;
          done = true;
        }
      }

      setCurrentLocation(oldLocation);
      return text.substring(index,endIndex);
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
    finally {
      setCurrentLocation(oldLocation);
    }
  }

  /**
   * Finds the first occurrence of the keyword within the text that is not
   *  enclosed within a brace or comment and is followed by whitespace.
   * @param keyword the keyword for which to search
   * @param text in which to search
   * @param textOffset Offset at which the text occurs in the document
   * @return index of the keyword, or -1 if the keyword is not found or
   * not followed by whitespace
   */
  private synchronized int _findKeywordAtToplevel(String keyword,
                                                  String text,
                                                  int textOffset) {
    int oldLocation = getCurrentLocation();
    
    int index = 0;
    boolean done = false;
    
    while (!done) {
      index = text.indexOf(keyword, index);
      if (index == -1) {  //not found
        done = true;
        break;
      }
      else {
        //found a match, check quality
        setCurrentLocation(textOffset + index);
          
        // check that the keyword is not in a comment and is followed by whitespace
        ReducedToken rt = _reduced.currentToken();
        int indexPastKeyword = index + keyword.length();
        if (indexPastKeyword < text.length()) {
          if (rt.getState() == ReducedModelStates.FREE &&
              Character.isWhitespace(text.charAt(indexPastKeyword))) {
            //if (!_isCommentedOrSpace(index,text)) {
            done = true;
            if (!posNotInBlock(index)) { //in a paren phrase, gone too far
              index = -1;
            }
          }
          else {
            index++;  //move past so we can search again
          }
        }
        else {
          // No space found past the keyword
          index = -1;
          done = true;
        }
      }
    }
    setCurrentLocation(oldLocation);
    return index;
  }
  /**
   * Appending any information for the reduced model from each undo command
   */
  private class CommandUndoableEdit extends AbstractUndoableEdit {
    private final Runnable _undoCommand;
    private final Runnable _redoCommand;

    public CommandUndoableEdit(final Runnable undoCommand,
                               final Runnable redoCommand)
    {
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

  private class InsertCommand implements Runnable {
    private final int _offset;
    private final String _text;

    public InsertCommand(final int offset, final String text) {
      _offset = offset;
      _text = text;
    }

    public void run() {
      // adjust location to the start of the text to input
      synchronized(DefinitionsDocument.this){
        _reduced.move(_offset - _currentLocation);
      }

      // loop over string, inserting characters into reduced model
      for (int i = 0; i < _text.length(); i++) {
        char curChar = _text.charAt(i);
        _addCharToReducedModel(curChar);
      }

      _currentLocation = _offset + _text.length();
      _styleChanged();
    }
  }

  private class RemoveCommand implements Runnable {
    private final int _offset;
    private final int _length;

    public RemoveCommand(final int offset, final int length) {
      _offset = offset;
      _length = length;
    }

    public void run() {
      setCurrentLocation(_offset); 
      
      // (don't move the cursor... I hope this doesn't break too much)
      synchronized(DefinitionsDocument.this){
        _reduced.delete(_length);
      }
      _styleChanged();
    }
  }
  
  /**
   * Getter method for CompoundUndoManager
   * @return _undoManager
   */
  public CompoundUndoManager getUndoManager() {
    return _undoManager;
  }

  /**
   * resets the undo manager
   */
  public void resetUndoManager() {
    _undoManager = new CompoundUndoManager(_notifier);
    _undoManager.setLimit(UNDO_LIMIT);
  }

  /**
   * public accessor for the next undo action
   */
  public UndoableEdit getNextUndo() {
    return _undoManager.getNextUndo();
  }
  
  /**
   * public accessor for the next undo action
   */
  public UndoableEdit getNextRedo() {
    return _undoManager.getNextRedo();
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
      if(_compoundEditState) {
        throw new IllegalStateException("Cannot start a compound edit while making a compound edit");
      }
      _compoundEditState = true;
      _compoundEdit = new OurCompoundEdit();
    }

    public void endCompoundEdit() {
      if(!_compoundEditState) {
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
      if(_compoundEditState) {
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
}
