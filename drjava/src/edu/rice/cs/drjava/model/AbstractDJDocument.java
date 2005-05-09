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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.text.SwingDocumentAdapter;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.model.repl.*;
import edu.rice.cs.drjava.model.definitions.*;
import edu.rice.cs.drjava.model.definitions.indent.*;
import edu.rice.cs.drjava.model.definitions.reducedmodel.*;

import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

/** Abstract DrJava Document which contains shared code between the DefinitionsDocument and the InteractionsDocumentAdapter
 */
public abstract class AbstractDJDocument extends SwingDocumentAdapter implements DJDocument, OptionConstants {
  
  //-------- FIELDS ----------
  
  /** A set of normal endings for lines. */
  protected static final HashSet<String> _normEndings = _makeNormEndings();
  /** A set of Java keywords. */
  protected static final HashSet<String> _keywords = _makeKeywords();
  /** A set of Java keywords. */
  protected static final HashSet<String> _primTypes = _makePrimTypes();
  /** The default indent setting. */
  protected int _indent = 2;
  
  /**
   * The reduced model of the document (stored in field _reduced) handles most of the
   * document logic and keeps track of state.  This field together with _currentLocation
   * function as a virtual object for purposes of synchronization.  All operations that
   * access or modify this virtual object should be synchronized on _reduced.
   */
  protected BraceReduction _reduced = new ReducedModelControl();
  
  /** The absolute character offset in the document. */
  protected int _currentLocation = 0;
  
  /* The fields _helperCache, _helperCacheHistory, and _cacheInUse function as
   * a virtual object that is synchronized on operations that access or modify
   * any of these fields.  The _helperCache object serves as the lock. */
  
  /** Caches calls to the reduced model to speed up indent performance. Must be cleared every time 
   *  the document is changed.  Use by calling _checkCache and _storeInCache.
   */
  private final Hashtable<String, Object> _helperCache = new Hashtable<String, Object>();
  
  /**
   * Keeps track of the order of elements added to the helper method cache,
   * so that the oldest elements can be removed when the maximum size is
   * reached.  A true LRU cache might be more effective, though I'm not sure
   * what the exact pattern of helper method reuse is-- this should be
   * sufficient without significantly decreasing the effectiveness of the cache.
   */
  private final Vector<String> _helperCacheHistory = new Vector<String>();
  
  /** Whether anything is stored in the cache.  It is used to avoid clearing the table
   *  unnecessarily on every change to the document.
   */
  protected boolean _cacheInUse;
  
  /** Maximum number of elements to allow in the helper method cache.  Only encountered when indenting 
   *  very large blocks, since the cache is cleared after each change to the document.
   */
  private static final int MAX_CACHE_SIZE = 10000;
  
  /** Constant for starting position of document. */
  public static final int DOCSTART = 0;
  
  /** Constant used by helper methods to indicate an error. */
  public static final int ERROR_INDEX = -1;
  
  /** The instance of the indent decision tree used by Definitions documents. */
  private final Indenter _indenter;
  
  // Saved here to allow the listener to be removed easily
  // This is needed to allow for garbage collection
  private OptionListener<Integer> _listener1;
  private OptionListener<Boolean> _listener2;
  

  //-------- CONSTRUCTORS --------
  
  protected AbstractDJDocument() {
    int ind = DrJava.getConfig().getSetting(INDENT_LEVEL).intValue();
    _indenter = makeNewIndenter(ind); //new Indenter(ind);
    _initNewIndenter();
  }
  
  protected AbstractDJDocument(Indenter indent) { _indenter = indent; }
  
  //-------- METHODS ---------
  
  /** Returns a new indenter. */
  protected abstract Indenter makeNewIndenter(int indentLevel);
  
  /** Get the indent level.
   *  @return the indent level
   */
  public int getIndent() { return _indent; }
  
  /** Set the indent to a particular number of spaces.
   *  @param indent the size of indent that you want for the document
   */
  public void setIndent(final int indent) {
    // throwErrorHuh();
    DrJava.getConfig().setSetting(INDENT_LEVEL,new Integer(indent));
    this._indent = indent;
  }
  
  protected void _removeIndenter() {
    DrJava.getConfig().removeOptionListener(INDENT_LEVEL, _listener1);
    DrJava.getConfig().removeOptionListener(AUTO_CLOSE_COMMENTS, _listener2);
  }
  
  private void _initNewIndenter() {
    // Create the indenter from the config values
    
    _listener1 = new OptionListener<Integer>() {
      public void optionChanged(OptionEvent<Integer> oce) {
        _indenter.buildTree(oce.value.intValue());
      }
    };
    
    _listener2 = new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        _indenter.buildTree(DrJava.getConfig().getSetting(INDENT_LEVEL).intValue());
      }
    };
    
    DrJava.getConfig().addOptionListener(INDENT_LEVEL, _listener1);
    DrJava.getConfig().addOptionListener(AUTO_CLOSE_COMMENTS, _listener2);
  }
  
  
  /**
   * Create a set of normal endings, i.e., semi-colons and braces for the purposes
   * of indenting.
   * @return the set of normal endings
   */
  protected static HashSet<String> _makeNormEndings() {
    HashSet<String> normEndings = new HashSet<String>();
    normEndings.add(";");
    normEndings.add("{");
    normEndings.add("}");
    normEndings.add("(");
    return  normEndings;
  }
  
  /** Create a set of Java/GJ keywords for special coloring.
   *  @return the set of keywords
   */
  protected static HashSet<String> _makeKeywords() {
    final String[] words =  {
      "import", "native", "package", "goto", "const", "if", "else",
      "switch", "while", "for", "do", "true", "false", "null", "this",
      "super", "new", "instanceof",    "return",
      "static", "synchronized", "transient", "volatile", "final",
      "strictfp", "throw", "try", "catch", "finally",
      "throws", "extends", "implements", "interface", "class",
      "break", "continue", "public", "protected", "private", "abstract",
      "case", "default", "assert", "enum"
    };
    HashSet<String> keywords = new HashSet<String>();
    for (int i = 0; i < words.length; i++) { keywords.add(words[i]); }
    return  keywords;
  }
  
  /** Create a set of Java/GJ primitive types for special coloring.
   *  @return the set of primitive types
   */
  protected static HashSet<String> _makePrimTypes() {
    final String[] words =  {
      "boolean", "char", "byte", "short", "int", "long", "float", "double", "void",
    };
    HashSet<String> prims = new HashSet<String>();
    for (String w: words) { prims.add(w); }
    return prims;
  }
  
  
  /**
   * Return all highlight status info for text between start and end.
   * This should collapse adjoining blocks with the same status into one.
   */
  public Vector<HighlightStatus> getHighlightStatus(int start, int end) {
    // First move the reduced model to the start
    //    int oldLocation = _currentLocation;
    Vector<HighlightStatus> v;
    readLock();
    try {
      synchronized (_reduced) {
        setCurrentLocation(start);
        // Now ask reduced model for highlight status for chars till end
        v = _reduced.getHighlightStatus(start, end - start);
        
        // Go through and find any NORMAL blocks
        // Within them check for keywords
        for (int i = 0; i < v.size(); i++) {
          HighlightStatus stat = v.get(i);
          if (stat.getState() == HighlightStatus.NORMAL) i = _highlightKeywords(v, i);
        }
      }
    }
    finally { readUnlock(); }
    
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
   * Separates out keywords from normal text for the given HighlightStatus element.
   *
   * What this does is it looks to see if the given part of the text contains a keyword. If it does, it splits
   * the HighlightStatus into separate blocks so that each keyword is in its own block. This will find all 
   * keywords in a given block.
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
    final HighlightStatus original = v.get(i);
    final String text;
    
    try { text = getText(original.getLocation(), original.getLength()); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    // Because this text is not quoted or commented, we can use the simpler tokenizer StringTokenizer. We have 
    // to return delimiters as tokens so we can keep track of positions in the original string.
    StringTokenizer tokenizer = new StringTokenizer(text, delimiters, true);
    
    // start and length of the text that has not yet been put back into the vector.
    int start = original.getLocation();
    int length = 0;
    
    // Remove the old element from the vector.
    v.remove(i);
    
    // Index where we are in the vector. It's the location we would insert new things into.
    int index = i;
    
    boolean process;
    int state = 0;
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      
      //first check to see if we need highlighting
      process = false;
      if (_isType(token)) {
        //right now keywords incl prim types, so must put this first
        state = HighlightStatus.TYPE;
        process = true;
      } 
      else if (_keywords.contains(token)) {
        state = HighlightStatus.KEYWORD;
        process = true;
      } 
      else if (_isNum(token)) {
        state = HighlightStatus.NUMBER;
        process = true;
      }
      
      if (process) {
        // first check if we had any text before the token
        if (length != 0) {
          HighlightStatus newStat = new HighlightStatus(start, length, original.getState());
          v.add(index, newStat);
          index++;
          start += length;
          length = 0;
        }
        
        // Now pull off the keyword
        int keywordLength = token.length();
        v.add(index, new HighlightStatus(start, keywordLength, state));
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
      HighlightStatus newStat = new HighlightStatus(start, length, original.getState());
      v.add(index, newStat);
      index++;
      length = 0;
    }
    // return one before because we need to point to the last one we inserted
    return index - 1;
  }
  
  
  /** Checks to see if the current string is a number
   *  @return true if x is a parseable number
   */
  private boolean _isNum(String x) {
    try {
      Double.parseDouble(x);
      return true;
    } 
    catch (NumberFormatException e) {  return false; }
  }
  
  /** Checks to see if the current string is a type. A type is assumed to be a primitive type OR
   *  anything else that begins with a capitalized character
   */
  private boolean _isType(String x) {
    if (_primTypes.contains(x)) return true;
    
    try { return Character.isUpperCase(x.charAt(0)); } 
    catch (IndexOutOfBoundsException e) { return false; }
  }
  
  /** Returns whether the given text only has spaces. */
  private boolean _hasOnlySpaces(String text) { return (text.trim().length() == 0); }
  
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
  protected abstract void _styleChanged(); 
  
  /** Clears the memoizing cache for read operations on the document.  This 
   * operation must be done before the document is modified since the contents 
   * of this cache are invalidated by any modification to the document.
   */
  protected void clearCache() {
    synchronized (_helperCache) { if (_cacheInUse) _clearCache(); }
  }
  
  /** Clears the helper method cache.  Should be called every time the document is modified. */
  private void _clearCache() {
    _helperCache.clear();
    _helperCacheHistory.clear();
    _cacheInUse = false;
  }
    
  
  /** Add a character to the underlying reduced model. ONLY called from already synchronized code!
   *  @param curChar the character to be added. */
  private void _addCharToReducedModel(char curChar) {
    clearCache();
    synchronized (_reduced) { _reduced.insertChar(curChar); }
  }
  
  /** Get the current location of the cursor in the document.  Unlike the usual swing document model, 
   *  which is stateless, because of our implementation of the underlying reduced model, we need to 
   *  keep track of the current location.
   * @return where the cursor is as the number of characters into the document 
   */
  public int getCurrentLocation() { return  _currentLocation; }
  
  /** Change the current location of the document
   *  @param loc the new absolute location 
   *  NOTE: synchronization on _reduced seems pointless here because loc in principle can be stale.  Without
   *  synchronization on _reduce, _currentLocation can be stale as well.  But why use synchronization to 
   *  prevent _currentLocation from being stale when loc may be stale? */
  public void setCurrentLocation(int loc)  { move(loc - _currentLocation); }
  
  /** The actual cursor movement logic.  Helper for setCurrentLocation(int).
   *  @param dist the distance from the current location to the new location.
   */
  public void move(int dist) {
    
    synchronized (_reduced) {
      int newLoc = _currentLocation + dist;
      if (newLoc < 0)
        throw new IllegalStateException("Tried to move cursor to a negative location");
      
      _currentLocation = newLoc;
      _reduced.move(dist);
    }
  }
  
  /** Forwarding method to find the match for the closing brace immediately to the left, assuming 
   *  there is such a brace.
   *  @return the relative distance backwards to the offset before the matching brace.
   */
  public int balanceBackward() {
    synchronized (_reduced) { return _reduced.balanceBackward(); }
  }
  
  /** Forwarding method to find the match for the open brace immediately to the right, assuming there 
   *  is such a brace.
   * @return the relative distance forwards to the offset after the matching brace.
   */
  public int balanceForward() {
    synchronized (_reduced) { return _reduced.balanceForward(); }
  }
  
  /** This method is used ONLY for testing.
   *  @return The reduced model of this document.
   */
  public BraceReduction getReduced() { return _reduced; }
  
  /** Returns the indent information for the current location. */
  public IndentInfo getIndentInformation() {
    // Check cache
    String key = "getIndentInformation:" + _currentLocation;

    IndentInfo cached = (IndentInfo) _checkCache(key);
    if (cached != null) return cached;
    
    IndentInfo info;
    synchronized (_reduced) { info = _reduced.getIndentInformation(); }
    _storeInCache(key, info);
    return info;
  }
  
  public ReducedModelState stateAtRelLocation(int dist) {
    synchronized (_reduced) { return _reduced.moveWalkerGetState(dist); }
  }
  
  public ReducedModelState getStateAtCurrent() {
    synchronized (_reduced) { return _reduced.getStateAtCurrent(); }
  }
  
  public void resetReducedModelLocation() {
    synchronized (_reduced) { _reduced.resetLocation(); }
  }
  
  /** Searching backwards, finds the position of the first character that is one of the given delimiters.  Does
   *  not look for delimiters inside paren phrases (e.g., skips semicolons used inside for statements.)  
   *  NB: ignores comments.
   *  @param pos Position to start from
   *  @param delims array of characters to search for
   *  @return position of first matching delimiter, or ERROR_INDEX if beginning of document is reached.
   */
  public int findPrevDelimiter(int pos, char[] delims) throws BadLocationException {
    return findPrevDelimiter(pos, delims, true);
  }
  
  /** Searching backwards, finds the position of the first character that is one of the given delimiters.  
   *  Will not look for delimiters inside a paren phrase if skipParenPhrases is true. NB: ignores comments.
   *  @param pos Position to start from
   *  @param delims array of characters to search for
   *  @param skipParenPhrases whether to look for delimiters inside paren phrases
   *  @return position of first matching delimiter, or ERROR_INDEX if beginning of document is reached.
   */
  public int findPrevDelimiter(final int pos, final char[] delims, final boolean skipParenPhrases)
    throws BadLocationException {
    // Check cache
    StringBuffer keyBuf = new StringBuffer("findPrevDelimiter:").append(pos);
    for (char ch: delims) { keyBuf.append(':').append(ch); }
    keyBuf.append(':').append(skipParenPhrases);
    String key = keyBuf.toString();
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int reducedPos = pos;
    int i;  // index of for loop below
    readLock();
    try {
      String text = getText(DOCSTART, pos);
      
      synchronized (_reduced) {
        final int origLocation = _currentLocation;
        // Move reduced model to location pos
        _reduced.move(pos - origLocation);  // reduced model points to pos == reducedPos
        
        // Walk backwards from specificed position
        for (i = pos-1; i >= DOCSTART; i--) {
          /* Invariant: reduced model points to reducedPos, text[i+1:pos] contains no valid delims, 
           * DOCSTART <= i < reducedPos <= pos */
          
          if (match(text.charAt(i),delims)) {
            // Move reduced model to walker's location
            _reduced.move(i - reducedPos);  // reduced model points to i
            reducedPos = i;                 // reduced model points to reducedPos
            
            // Check if matching char should be ignored because it is within a comment, 
            // quotes, or ignored paren phrase
            ReducedModelState state = _reduced.getStateAtCurrent();
            if (!state.equals(ReducedModelState.FREE) || _isStartOfComment(text, i)
                  || ((i > 0) && _isStartOfComment(text, i - 1)) || (skipParenPhrases && posInParenPhrase()))
              continue;  // ignore matching char 
            else break;  // found valid matching char
          }
        }
        
        /* Invariant: same as for loop except that DOCSTART-1 <= i <= reducedPos <= pos */
        
        _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
      }  // end synchronized
    }
    finally { readUnlock(); }
    
    // Return position of matching char or ERROR_INDEX 
    
    if (i == DOCSTART-1) reducedPos = ERROR_INDEX; // No matching char was found
    _storeInCache(key, new Integer(reducedPos));
    return reducedPos;  
  }
  
  private static boolean match(char c, char[] delims) {
    for (char d : delims) { if (c == d) return true; } // Found matching delimiter
    return false;
  }
        
  /** This function finds the given character in the same statement as the given position, and before the given
   *  position.  It is used by QuestionExistsCharInStmt and QuestionExistsCharInPrevStmt
   */
  public boolean findCharInStmtBeforePos(char findChar, int position) {
    if (position == DefinitionsDocument.ERROR_INDEX) {
      String mesg = 
        "Argument endChar to QuestionExistsCharInStmt must be a char that exists on the current line.";
      throw new UnexpectedException(new IllegalArgumentException(mesg));
    }
    
    char[] findCharDelims = {findChar, ';', '{', '}'};
    int prevFindChar;
    
    // Find the position of the preceding occurrence findChar position (looking in paren phrases as well)
    boolean found;
    
    readLock();
    try {
      prevFindChar = this.findPrevDelimiter(position, findCharDelims, false);
      
      if ((prevFindChar == DefinitionsDocument.ERROR_INDEX) || (prevFindChar < 0)) return false; // no such char
      
      // Determine if prevFindChar is findChar or the end of statement delimiter
      String foundString = this.getText(prevFindChar, 1);
      char foundChar = foundString.charAt(0);
      found = (foundChar == findChar);
    }
    catch (Throwable t) { throw new UnexpectedException(t); }
    finally { readUnlock(); }
    return found;
  }
  
  /** Finds the position of the first non-whitespace, non-comment character before pos.
   *  Skips comments and all whitespace, including newlines.
   *  @param pos Position to start from
   *  @param whitespace chars considered as white space
   *  @return position of first non-whitespace character before pos OR ERROR_INDEX if no such char
   */
  public int findPrevCharPos(int pos, char[] whitespace) throws BadLocationException {
    // Check cache
    StringBuffer keyBuf = new StringBuffer("findPrevCharPos:").append(pos);
    for (char ch: whitespace) { keyBuf.append( ':').append(ch); }
    String key = keyBuf.toString();
    Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    int reducedPos = pos;
    int i = pos - 1;
    readLock();
    try {
      String text = getText(0, pos);
      synchronized(_reduced) {
        
        final int origLocation = _currentLocation;
        // Move reduced model to location pos
        _reduced.move(pos - origLocation);  // reduced model points to pos == reducedPos
        
        // Walk backward from specified position
        
        while (i >= 0) { 
          /* Invariant: reduced model points to reducedPos, i < reducedPos <= pos, 
           * text[i+1:pos-1] contains invalid chars */
          
          if (match(text.charAt(i), whitespace)) {
            // ith char is whitespace
            i--;
            continue;
          }
          
          // Found a non-whitespace char;  move reduced model to location i
          _reduced.move(i - reducedPos);
          reducedPos = i;                  // reduced model points to i == reducedPos
          
          // Check if matching char is within a comment (not including opening two characters)
          if ((_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_LINE_COMMENT)) ||
              (_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_BLOCK_COMMENT))) {
            i--;
            continue;
          }
          
          if (_isEndOfComment(text, i)) { /* char is second character is opening comment market */  
            // Move i past the first comment character and continue searching
            i = i - 2;
            continue;
          }
          
          // Found valid previous character
          break;
        }
        
        /* Exit invariant same as for loop except that i <= reducedPos because at break i = reducedPos */
        _reduced.move(origLocation - reducedPos);
      }
    }
    finally { readUnlock(); }
    
    int result = reducedPos;
    if (i < 0) result = ERROR_INDEX;
    _storeInCache(key, new Integer(result));
    return result;
  }
  
  /** Checks the helper method cache for a stored value.  Returns the value if it has been cached, or null 
   *  otherwise. Calling convention for keys: methodName:arg1:arg2
   *  @param key Name of the method and arguments
   */
  protected Object _checkCache(String key) {
    // throwErrorHuh();
    //_helperCache.put(key+"|time", new Long(System.currentTimeMillis()));
    Object result = _helperCache.get(key);  /* already synchronized by Hashtable */
    //if (result != null) DrJava.consoleOut().println("Using cache for " + key);
    return result;
  }
  
  /** Stores the given result in the helper method cache. Calling convention for keys: methodName:arg1:arg2
   *  @param key Name of method and arguments
   *  @param result Result of the method call
   */
  protected void _storeInCache(String key, Object result) {
    // throwErrorHuh();
    synchronized (_helperCache) {
      _cacheInUse = true;
      
      // Prevent going over max size
      if (_helperCache.size() >= MAX_CACHE_SIZE) {
        if (_helperCacheHistory.size() > 0) {
          _helperCache.remove( _helperCacheHistory.get(0) );
          _helperCacheHistory.remove(0);
        }
        else { // Should not happen
          throw new RuntimeException("Cache larger than cache history!");
        }
      }
      Object prev = _helperCache.put(key, result);
      // Add to history if the insert increased the size of the table
      if (prev == null) _helperCacheHistory.add(key);
    }
  }
  
  /** Default indentation - uses OTHER flag and no progress indicator.
   *  @param selStart the offset of the initial character of the region to indent
   *  @param selEnd the offset of the last character of the region to indent
   */
  public void indentLines(int selStart, int selEnd) {
    try { indentLines(selStart, selEnd, Indenter.OTHER, null); }
    catch (OperationCanceledException oce) {
      // Indenting without a ProgressMonitor should never be cancelled!
      throw new UnexpectedException(oce);
    }
  }
  
  /** Parameterized indentation for special-case handling.
   *  @param selStart the offset of the initial character of the region to indent
   *  @param selEnd the offset of the last character of the region to indent
   *  @param reason a flag from {@link Indenter} to indicate the reason for the indent
   *        (indent logic may vary slightly based on the trigger action)
   *  @param pm used to display progress, null if no reporting is desired
   */
  public void indentLines(int selStart, int selEnd, int reason, ProgressMonitor pm)
    throws OperationCanceledException {
 
    // Begins a compound edit.
    // int key = startCompoundEdit(); // commented out in connection with the FrenchKeyBoard Fix
    
    writeLock();
    try {
      synchronized (_reduced) {
        if (selStart == selEnd) {  // single line to indent
          Position oldCurrentPosition = createPosition(_currentLocation);
          // Indent, updating current location if necessary.
          if (_indentLine(reason)) {
            setCurrentLocation(oldCurrentPosition.getOffset());
            int space = getWhiteSpace();
            move(space);
          }
        }
        else _indentBlock(selStart, selEnd, reason, pm);
      }
    }
    catch (Throwable t) { throw new UnexpectedException(t); }
    finally { writeUnlock(); } 
    
    // Ends the compound edit.
    //endCompoundEdit(key);   //Changed to endLastCompoundEdit in connection with the FrenchKeyBoard Fix
    endLastCompoundEdit();
  }
  
  /** Indents the lines between and including the lines containing points start and end.
   *  @param start Position in document to start indenting from
   *  @param end Position in document to end indenting at
   *  @param reason a flag from {@link Indenter} to indicate the reason for the indent
   *        (indent logic may vary slightly based on the trigger action)
   *  @param pm used to display progress, null if no reporting is desired
   */
  private void _indentBlock(final int start, final int end, int reason, ProgressMonitor pm)
    throws OperationCanceledException, BadLocationException {
    
    // Keep marker at the end. This Position will be the correct endpoint no matter how we change 
    // the doc doing the indentLine calls.
    final Position endPos = this.createPosition(end);
    // Iterate, line by line, until we get to/past the end
    int walker = start;
    while (walker < endPos.getOffset()) {
      setCurrentLocation(walker);
      // Keep pointer to walker position that will stay current
      // regardless of how indentLine changes things
      Position walkerPos = this.createPosition(walker);
      // Indent current line
      // We currently ignore current location info from each line, because
      // it probably doesn't make sense in a block context.
      _indentLine(reason);
      // Move back to walker spot
      setCurrentLocation(walkerPos.getOffset());
      walker = walkerPos.getOffset();
      
      if (pm != null) {
        pm.setProgress(walker); // Update ProgressMonitor.
        if (pm.isCanceled()) throw new OperationCanceledException(); // Check for cancel button-press.
      }
      
      // Adding 1 makes us point to the first character AFTER the next newline. We don't actually move the
      // location yet. That happens at the top of the loop, after we check if we're past the end.
      walker += _reduced.getDistToNextNewline() + 1;
    }
  }
  
  /** Indents a line using the Indenter decision tree.  Public ONLY for testing purposes */
  public boolean _indentLine(int reason) { return _indenter.indent(this, reason); }
  
  /** Returns the "intelligent" beginning of line.  If currPos is to the right of the first 
   *  non-whitespace character, the position of the first non-whitespace character is returned.  
   *  If currPos is at or to the left of the first non-whitespace character, the beginning of
   *  the line is returned.
   *  @param currPos A position on the current line
   */
  public int getIntelligentBeginLinePos(int currPos) throws BadLocationException {
    // throwErrorHuh();

    String prefix;
    int firstChar;
    readLock();
    try {
      firstChar = getLineStartPos(currPos);
      prefix = getText(firstChar, currPos-firstChar);
    }
    finally { readUnlock(); }
    
    // Walk through string until we find a non-whitespace character
    int i;
    int len = prefix.length();
   
    for (i = 0; i < len; i++ ) { if (! Character.isWhitespace(prefix.charAt(i))) break; }

    // If we found a non-WS char left of curr pos, return it
    if (i < len) {
      int firstRealChar = firstChar + i;
      if (firstRealChar < currPos) return firstRealChar;
    }
    // Otherwise, return the beginning of the line
    return firstChar;
  }
  
  /** Returns the indent level of the start of the statement that the cursor is on.  Uses a default 
   *  set of delimiters. (';', '{', '}') and a default set of whitespace characters (' ', '\t', n', ',')
   *  @param pos Cursor position
   */
  public String getIndentOfCurrStmt(int pos) throws BadLocationException {
    // throwErrorHuh();
    char[] delims = {';', '{', '}'};
    char[] whitespace = {' ', '\t', '\n',','};
    return getIndentOfCurrStmt(pos, delims, whitespace);
  }
  
  /** Returns the indent level of the start of the statement that the cursor is on.  Uses a default
   *  set of whitespace characters: {' ', '\t', '\n', ','}
   *  @param pos Cursor position
   */
  public String getIndentOfCurrStmt(int pos, char[] delims) throws BadLocationException {
    // throwErrorHuh();
    char[] whitespace = {' ', '\t', '\n',','};
    return getIndentOfCurrStmt(pos, delims, whitespace);
  }
  
  /** Returns the indent level of the start of the statement that the cursor is on.
   *  @param pos Cursor position
   *  @param delims Delimiter characters denoting end of statement
   *  @param whitespace characters to skip when looking for beginning of next statement
   */
  public String getIndentOfCurrStmt(int pos, char[] delims, char[] whitespace) throws BadLocationException {
    // throwErrorHuh();
    // Check cache
    StringBuffer keyBuf = new StringBuffer("getIndentOfCurrStmt:").append(pos);
    for (char ch: delims) { keyBuf.append(':').append(ch); }
    String key = keyBuf.toString();
    String cached = (String) _checkCache(key);
    if (cached != null) return cached;
    
    String lineText;
    
    readLock();
    try {
      synchronized (_reduced) {
        // Get the start of the current line
        int lineStart = getLineStartPos(pos);
        
        // Find the previous delimiter that closes a statement
        boolean reachedStart = false;
        int prevDelim = lineStart;
        boolean ignoreParens = posInParenPhrase(prevDelim);
        
        do {
          prevDelim = findPrevDelimiter(prevDelim, delims, ignoreParens);
          if (prevDelim > 0 && prevDelim < getLength() && getText(prevDelim,1).charAt(0) == '{') break;
          if (prevDelim == ERROR_INDEX) { // no delimiter found
            reachedStart = true;
            break;
          }
          ignoreParens = posInParenPhrase(prevDelim);
        } while (ignoreParens);  
    
        // From the previous delimiter, find the next non-whitespace character
        int nextNonWSChar;
        if (reachedStart) nextNonWSChar = getFirstNonWSCharPos(DOCSTART);
        else nextNonWSChar = getFirstNonWSCharPos(prevDelim+1, whitespace, false);
        
        // If the end of the document was reached
        if (nextNonWSChar == ERROR_INDEX) nextNonWSChar = getLength();
        
        // Get the start of the line of the non-ws char
        int lineStartStmt = getLineStartPos(nextNonWSChar);
        
        // Get the position of the first non-ws character on this line
        int lineFirstNonWS = getLineFirstCharPos(lineStartStmt);
        lineText = getText(lineStartStmt, lineFirstNonWS - lineStartStmt); 
      }
    }
    catch(Throwable t) { throw new UnexpectedException(t); }
    finally { readUnlock(); }
    
    _storeInCache(key, lineText);
    return lineText;
  }
  
  /** Determines if the given character exists on the line where the given cursor position is.  Does not 
   *  search in quotes or comments. <b>Does not work if character being searched for is a '/' or a '*'</b>.
   *  @param pos Cursor position
   *  @param findChar Character to search for
   *  @return true if this node's rule holds.
   */
  public int findCharOnLine(int pos, char findChar) {
    // throwErrorHuh();
    // Check cache
    String key = "findCharOnLine:" + pos + ":" + findChar;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int i;
    int matchIndex; // absolute index of matching character 
    
    readLock();
    try {
      synchronized (_reduced) {
        int here = _currentLocation;
        int lineStart = getLineStartPos(pos);
        int lineEnd = getLineEndPos(pos);
        String lineText = getText(lineStart, lineEnd - lineStart);
        i = lineText.indexOf(findChar, 0);
        matchIndex = i + lineStart;
        
        while (i != -1) { // match found
          /* Invariant: reduced model points to original location (here), lineText[0:i-1] does not contain valid 
           *            findChar, lineText[i] == findChar which may or may not be valid. */
          
          // Move reduced model to location of ith char
          _reduced.move(matchIndex - here);  // move reduced model to location matchIndex
          
          // Check if matching char is in comment or quotes
          if (_reduced.getStateAtCurrent().equals(ReducedModelState.FREE)) {
            // Found matching char
            _reduced.move(here - matchIndex);  // Restore reduced model
            break;
          } 
          
          // matching character is not valid, try again
          _reduced.move(here - matchIndex);  // Restore reduced model
          i = lineText.indexOf(findChar, i+1);
        }
      }
    }
    catch (Throwable t) { throw new UnexpectedException(t); }
    finally { readUnlock(); }
    
    if (i == -1) matchIndex = ERROR_INDEX;
    _storeInCache(key, new Integer(matchIndex));
    return matchIndex;
  }
  
  /**
   * Returns the absolute position of the beginning of the
   * current line.  (Just after most recent newline, or DOCSTART)
   * Doesn't ignore comments.
   * @param pos Any position on the current line
   * @return position of the beginning of this line
   */
  public int getLineStartPos(final int pos) {
    // throwErrorHuh();
    if (pos < 0 || pos > getLength()) return -1;
    // Check cache
    String key = "getLineStartPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int dist;
    synchronized (_reduced) {
      int location = _currentLocation;
      _reduced.move(pos - location);
      dist = _reduced.getDistToPreviousNewline(0);
      _reduced.move(location - pos);
    }
    
    if (dist == -1) {
      // No previous newline was found; return DOCSTART
      _storeInCache(key, new Integer(DOCSTART));
      return DOCSTART;
    }
    
    _storeInCache(key, new Integer(pos - dist));
    return pos - dist;
  }
  
  /**
   * Returns the absolute position of the end of the current
   * line.  (At the next newline, or the end of the document.)
   * @param pos Any position on the current line
   * @return position of the end of this line
   */
  public int getLineEndPos(final int pos) {
    // throwErrorHuh();
    if (pos < 0 || pos > getLength()) return -1;
    
    // Check cache
    String key = "getLineEndPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int dist;
    synchronized (_reduced) {
      int location = _currentLocation;
      _reduced.move(pos - location);
      dist = _reduced.getDistToNextNewline();
      _reduced.move(location - pos);
    }
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
    // throwErrorHuh();
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
    // throwErrorHuh();
    char[] whitespace = {' ', '\t', '\n'};
    return getFirstNonWSCharPos(pos, whitespace, false);
  }
  
  /**
   * Similar to the single-argument version, but allows including comments.
   * @param pos Position to start from
   * @param acceptComments if true, find non-whitespace chars in comments
   * @return position of first non-whitespace character after pos,
   * or ERROR_INDEX if end of document is reached
   */
  public int getFirstNonWSCharPos(int pos, boolean acceptComments)
    throws BadLocationException {
    // throwErrorHuh();
    char[] whitespace = {' ', '\t', '\n'};
    return getFirstNonWSCharPos(pos, whitespace, acceptComments);
  }
  
  /**
   * Finds the position of the first non-whitespace character after pos.
   * NB: Skips comments and all whitespace, including newlines
   * @param pos Position to start from
   * @param whitespace array of whitespace chars to ignore
   * @param acceptComments if true, find non-whitespace chars in comments
   * @return position of first non-whitespace character after pos,
   * or ERROR_INDEX if end of document is reached
   */
  public int getFirstNonWSCharPos(int pos, char[] whitespace, boolean acceptComments) 
    throws BadLocationException {
    // throwErrorHuh();
    // Check cache
    StringBuffer keyBuf = new StringBuffer("getFirstNonWSCharPos:").append(pos);
    for (char ch: whitespace) { keyBuf.append(':').append(ch); }
    String key = keyBuf.toString();
    
    Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    int i = pos;
    int endPos = getLength();
    
    // Get text from pos to end of document
    String text = getText(pos, endPos - pos);
    
    final int origLocation = _currentLocation;
    // Move reduced model to location pos
    _reduced.move(pos - origLocation);
    int reducedPos = pos;
    
    //int iter = 0;
    
    // Walk forward from specificed position
    while (i < endPos) {
      
      // Check if character is whitespace
      if (match(text.charAt(i-pos), whitespace)) {
        i++;
        continue;
      }
      // Found a non whitespace character
      // Move reduced model to walker's location
      _reduced.move(i - reducedPos);  // reduced model points to location i
      reducedPos = i;                 // reduced mdoel points to location reducedPos
      
      // Check if non-ws char is within comment and if we want to ignore them.
      if (!acceptComments &&
          ((_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_LINE_COMMENT)) ||
           (_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_BLOCK_COMMENT)))) {
        i++;
        continue;
      }
      
      // Check if non-ws char is part of comment opening market and if we want to ignore them
      if (!acceptComments && _isStartOfComment(text, i - pos)) {
        // ith char is first char in comment open market; skip past this marker
        // and continue searching
        i = i + 2;
        continue;
      }
      
      // Return position of matching char
      break;
    }
    _reduced.move(origLocation - reducedPos);
    
    int result = reducedPos;
    if (i == endPos) result = ERROR_INDEX;
    
    _storeInCache(key, new Integer(result));
    return result;
  }
  
  
  public int findPrevNonWSCharPos(int pos) throws BadLocationException {
    // throwErrorHuh();
    char[] whitespace = {' ', '\t', '\n'};
    return findPrevCharPos(pos, whitespace);
  }
  
  /**
   * Helper method for getFirstNonWSCharPos
   * Determines whether the current character is the start
   * of a comment: "/*" or "//"
   */
  protected static boolean _isStartOfComment(String text, int pos) {
    // throwErrorHuh();
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
  protected static boolean _isEndOfComment(String text, int pos) {
    // throwErrorHuh();
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
   * Returns true if the given position is inside a paren phrase.
   * @param pos the position we're looking at
   * @return true if pos is immediately inside parentheses
   */
  public boolean posInParenPhrase(int pos) {
    // throwErrorHuh();
    // Check cache
    String key = "posInParenPhrase:" + pos;
    Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) return cached.booleanValue();

    boolean inParenPhrase;
    
    synchronized (_reduced) {
      int here = _currentLocation;
      _reduced.move(pos - here);
      inParenPhrase = posInParenPhrase();
      _reduced.move(here - pos);
    }

    _storeInCache(key, Boolean.valueOf(inParenPhrase));
    return inParenPhrase;
  }

  /**
   * Returns true if the reduced model's current position is inside a paren phrase.
   * @return true if pos is immediately inside parentheses
   */
  public boolean posInParenPhrase() {
    // throwErrorHuh();
    IndentInfo info;
    synchronized (_reduced) {
      info = _reduced.getIndentInformation();
    }
    return info.braceTypeCurrent.equals(IndentInfo.openParen);
  }

  /**
   * Returns true if the given position is not inside a paren/brace/etc phrase.
   * @param pos the position we're looking at
   * @return true if pos is immediately inside a paren/brace/etc
   */
  protected boolean posNotInBlock(int pos) {
    // throwErrorHuh();
    // Check cache
    String key = "posNotInBlock:" + pos;
    Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) return cached.booleanValue();

    boolean notInParenPhrase;
    
    synchronized (_reduced) {
      int here = _currentLocation;
      _reduced.move(pos - here);
      IndentInfo info = _reduced.getIndentInformation();
      notInParenPhrase = info.braceTypeCurrent.equals(IndentInfo.noBrace);
      _reduced.move(here - pos);
    }
    _storeInCache(key, Boolean.valueOf(notInParenPhrase));
    return notInParenPhrase;
  }
  
  
  /**
   * Gets the number of whitespace characters between the current location and the rest of
   * the document or the first non-whitespace character, whichever comes first.
   * @return the number of whitespace characters
   */
  public int getWhiteSpace() {
    // throwErrorHuh();
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
    // throwErrorHuh();
    String text = this.getText(_currentLocation - relStart, Math.abs(relStart -
        relEnd));
    int i = 0;
    int length = text.length();
    while ((i < length) && (text.charAt(i) == ' '))
      i++;
    return  i;
  }
  
  /**
   * Sets the text between the previous newline and the first non-whitespace
   * character of the line containing pos to tab.
   * @param tab String to be placed between previous newline and first
   * non-whitespace character
   */
  public void setTab(String tab, int pos) {
    // throwErrorHuh();
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
   * Updates document structure as a result of text insertion.
   * This happens after the text has actually been inserted.
   * Here we update the reduced model (using an {@link AbstractDJDocument.InsertCommand
   * InsertCommand}) and store information for how to undo/redo the reduced model
   * changes inside the {@link javax.swing.text.AbstractDocument.DefaultDocumentEvent 
   * DefaultDocumentEvent}.
   *
   * @see edu.rice.cs.drjava.model.AbstractDJDocument.InsertCommand
   * @see javax.swing.text.AbstractDocument.DefaultDocumentEvent
   * @see edu.rice.cs.drjava.model.definitions.DefinitionsDocument.CommandUndoableEdit
   */
  protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng,
                              AttributeSet attr) {
    // Clear the helper method cache
    clearCache();

    super.insertUpdate(chng, attr);

    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      final String str = getText(offset, length);

      InsertCommand doCommand = new InsertCommand(offset, str);
      RemoveCommand undoCommand = new RemoveCommand(offset, length);

      // add the undo/redo
      addUndoRedo(chng,undoCommand,doCommand);
      //chng.addEdit(new CommandUndoableEdit(undoCommand, doCommand));
      // actually do the insert
      doCommand.run();
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }
  
  
  
  /**
   * Updates document structure as a result of text removal.
   * This happens before the text has actually been removed.
   * Here we update the reduced model (using a {@link AbstractDJDocument.RemoveCommand
   * RemoveCommand}) and store information for how to undo/redo the reduced
   * model changes inside the {@link javax.swing.text.AbstractDocument.DefaultDocumentEvent 
   * DefaultDocumentEvent}.
   *
   * @see AbstractDJDocument.RemoveCommand
   * @see javax.swing.text.AbstractDocument.DefaultDocumentEvent
   */
  protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
    clearCache();

    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      final String removedText = getText(offset, length);
      super.removeUpdate(chng);

      Runnable doCommand = new RemoveCommand(offset, length);
      Runnable undoCommand = new InsertCommand(offset, removedText);

      // add the undo/redo info
      addUndoRedo(chng,undoCommand,doCommand);

      // actually do the removal from the reduced model
      doCommand.run();
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  
  /**
   * Inserts a string of text into the document.
   * It turns out that this is not where we should do custom processing
   * of the insert; that is done in {@link #insertUpdate}.
   */
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
    // throwErrorHuh();
    // Clear the helper method cache
    clearCache();

    super.insertString(offset,str,a);
  }
  
  /**
   * Removes a block of text from the specified location.
   * We don't update the reduced model here; that happens
   * in {@link #removeUpdate}.
   */
  public void remove(int offset, int len) throws BadLocationException {
    // throwErrorHuh();
    // Clear the helper method cache
    clearCache();
    
    super.remove(offset, len);
  }
  
  
  
  //Two abstract methods to delegate to the undo manager, if one exists.
  protected abstract int startCompoundEdit();
  protected abstract void endCompoundEdit(int i);
  protected abstract void endLastCompoundEdit();
  protected abstract void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand);
  
  //Checks if the document is closed, and then throws an error if it is.
//  protected abstract void throwErrorHuh();
  
  //-------- INNER CLASSES ------------
  
  protected class InsertCommand implements Runnable {
    private final int _offset;
    private final String _text;
    
    public InsertCommand(final int offset, final String text) {
      _offset = offset;
      _text = text;
    }
    
    public void run() {
      // adjust location to the start of the text to input
      synchronized(_reduced) {
        _reduced.move(_offset - _currentLocation);  
        // loop over string, inserting characters into reduced model
        for (int i = 0; i < _text.length(); i++) {
          char curChar = _text.charAt(i);
          _addCharToReducedModel(curChar);
        }
      }
      _currentLocation = _offset + _text.length();
      _styleChanged();
    }
  }
  
  protected class RemoveCommand implements Runnable {
    private final int _offset;
    private final int _length;
    
    public RemoveCommand(final int offset, final int length) {
      _offset = offset;
      _length = length;
    }
    
    public void run() {
      synchronized (_reduced) { 
         setCurrentLocation(_offset);
         _reduced.delete(_length); }
      _styleChanged();
    }
  }
}
