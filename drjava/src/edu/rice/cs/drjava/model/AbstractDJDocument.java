/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
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

package edu.rice.cs.drjava.model;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;

import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceReduction;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelControl;
import edu.rice.cs.drjava.model.definitions.reducedmodel.HighlightStatus;
import edu.rice.cs.drjava.model.definitions.reducedmodel.IndentInfo;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelState;
import edu.rice.cs.drjava.model.definitions.ClassNameNotFoundException;


import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.SwingDocument;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.ProgressMonitor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/** This class contains code supporting the concept of a "DJDocument"; it is shared between DefinitionsDocument and 
  * InteractionsDJDocument. This partial implementation of <code>Document</code> contains a "reduced model". The reduced
  * model is automatically kept in sync when this document is updated. Also, that synchronization is maintained even 
  * across undo/redo -- this is done by making the undo/redo commands know how to restore the reduced model state.
  *
  * The reduced model is not thread-safe, so it is essential that ONLY this class/subclasses call methods on it.  
  * Any information from the reduced model should be obtained through helper methods in this class/subclasses, and ALL 
  * methods in this class/subclasses which reference the reduced model (via the _reduced field) sync on _reducedModel.
  * Of course, a readLock or writeLock on this must be acquired BEFOFE locking _reducedModel.  This protocol
  * prevents any thread from seeing an inconsistent state in the middle of another thread's changes.
  *
  * @see BraceReduction
  * @see ReducedModelControl
  * @see ReducedModelComment
  * @see ReducedModelBrace
  */
public abstract class AbstractDJDocument extends SwingDocument implements DJDocument, OptionConstants {
  
  /*-------- FIELDS ----------*/
  
  /** A set of normal endings for lines. */
  protected static final HashSet<String> _normEndings = _makeNormEndings();
  /** A set of Java keywords. */
  protected static final HashSet<String> _keywords = _makeKeywords();
  /** A set of Java keywords. */
  protected static final HashSet<String> _primTypes = _makePrimTypes();
  /** The default indent setting. */
  protected volatile int _indent = 2;
  
  /** The reduced model of the document (stored in field _reduced) handles most of the document logic and keeps 
    * track of state.  This field together with _currentLocation function as a virtual object for purposes of 
    * synchronization.  All operations that access or modify this virtual object should be synchronized on _reduced.
    */
  public final BraceReduction _reduced = new ReducedModelControl();  // public only for locking purposes
  
  /** The absolute character offset in the document. Treated as part of the _reduced (model) for locking 
    * purposes. */
  protected volatile int _currentLocation = 0;
  
  /* The fields _queryCache and _offsetToQueries function as an extension of the reduced model.
   * Hence _reduced should be the lock object for operations on these two structures. 
   * This data structure caches calls to the reduced model to speed up indent performance. Must be cleared every time 
   * the document is changed.  Use by calling _checkCache, _storeInCache, and _clearCache.
   */
  private final HashMap<String, Object> _queryCache = new HashMap<String, Object>(INIT_CACHE_SIZE);
  
  /** Records the set of queries (as a list) for each offset. */
  private final SortedMap<Integer, List<String>> _offsetToQueries = new TreeMap<Integer, List<String>>();
  
  /** Initial number of elements in _queryCache. */
  private static final int INIT_CACHE_SIZE = 0x10000;  // 16**4 = 16384 
  
  /** Constant for starting position of document. */
  public static final int DOCSTART = 0;
  
  /** Constant used by helper methods to indicate an error. */
  public static final int ERROR_INDEX = -1;
  
  /** The instance of the indent decision tree used by Definitions documents. */
  private volatile Indenter _indenter;
  
  /* Saved here to allow the listener to be removed easily. This is needed to allow for garbage collection. */
  private volatile OptionListener<Integer> _listener1;
  private volatile OptionListener<Boolean> _listener2;
  
  /*-------- CONSTRUCTORS --------*/
  
  /** Standard default constructor; required because a unary constructor is defined. */
  protected AbstractDJDocument() {
//    int ind = DrJava.getConfig().getSetting(INDENT_LEVEL).intValue();
//    _indenter = makeNewIndenter(ind); //new Indenter(ind);
//    _initNewIndenter();
  }
  
  /** Constructor used to build a new document with an existing indenter. Only used in tests. */
  protected AbstractDJDocument(Indenter indent) { _indenter = indent; }
  
  //-------- METHODS ---------//
  
  /* acquireReadLock, releaseReadLock, acquireWriteLock, releaseWriteLock are inherited from SwingDocument. */
  
  /** Returns a new indenter.  Assumes writeLock is held. */
  protected abstract Indenter makeNewIndenter(int indentLevel);
  
  /** Get the indenter.  Assumes writeLock is already held.
    * @return the indenter
    */
  private Indenter getIndenter() { 
    if (_indenter == null) {
      int ind = DrJava.getConfig().getSetting(INDENT_LEVEL).intValue();
      _indenter = makeNewIndenter(ind); //new Indenter(ind);
      _initNewIndenter();
    }
    return _indenter; 
  }
  
  /** Get the indent level.
    * @return the indent level
    */
  public int getIndent() { return _indent; }
  
  /** Set the indent to a particular number of spaces.
    * @param indent the size of indent that you want for the document
    */
  public void setIndent(final int indent) {
    DrJava.getConfig().setSetting(INDENT_LEVEL, indent);
    this._indent = indent;
  }
  
  protected void _removeIndenter() {
    DrJava.getConfig().removeOptionListener(INDENT_LEVEL, _listener1);
    DrJava.getConfig().removeOptionListener(AUTO_CLOSE_COMMENTS, _listener2);
  }
  
  /** Only called from within getIndenter(). */
  private void _initNewIndenter() {
    // Create the indenter from the config values
    
    final Indenter indenter = _indenter;
    
    _listener1 = new OptionListener<Integer>() {
      public void optionChanged(OptionEvent<Integer> oce) {
        indenter.buildTree(oce.value.intValue());
      }
    };
    
    _listener2 = new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
        indenter.buildTree(DrJava.getConfig().getSetting(INDENT_LEVEL).intValue());
      }
    };
    
    DrJava.getConfig().addOptionListener(INDENT_LEVEL, _listener1);
    DrJava.getConfig().addOptionListener(AUTO_CLOSE_COMMENTS, _listener2);
  }
  
  
  /** Create a set of normal endings, i.e., semi-colons and braces for the purposes of indenting.
    *  @return the set of normal endings
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
    * @return the set of keywords
    */
  protected static HashSet<String> _makeKeywords() {
    final String[] words =  {
      "import", "native", "package", "goto", "const", "if", "else", "switch", "while", "for", "do", "true", "false",
      "null", "this", "super", "new", "instanceof", "return", "static", "synchronized", "transient", "volatile", 
      "final", "strictfp", "throw", "try", "catch", "finally", "throws", "extends", "implements", "interface", "class",
      "break", "continue", "public", "protected", "private", "abstract", "case", "default", "assert", "enum"
    };
    HashSet<String> keywords = new HashSet<String>();
    for (int i = 0; i < words.length; i++) { keywords.add(words[i]); }
    return  keywords;
  }
  
  /** Create a set of Java/GJ primitive types for special coloring.
    * @return the set of primitive types
    */
  protected static HashSet<String> _makePrimTypes() {
    final String[] words =  {
      "boolean", "char", "byte", "short", "int", "long", "float", "double", "void",
    };
    HashSet<String> prims = new HashSet<String>();
    for (String w: words) { prims.add(w); }
    return prims;
  }
  
  /** Computes the maximum of x and y. */ 
  private int max(int x, int y) { return x <= y? y : x; }
  
  /** Return all highlight status info for text between start and end. This should collapse adjoining blocks 
    * with the same status into one.
    */
  public Vector<HighlightStatus> getHighlightStatus(int start, int end) {
    
    if (start == end) return new Vector<HighlightStatus>(0);
    Vector<HighlightStatus> v;
    
    acquireReadLock();
    try {
      synchronized(_reduced) {
        _setCurrentLocation(start);
        /* Now ask reduced model for highlight status for chars till end */
        v = _reduced.getHighlightStatus(start, end - start);
        
        /* Go through and find any NORMAL blocks. Within them check for keywords. */
        for (int i = 0; i < v.size(); i++) {
          HighlightStatus stat = v.get(i);
          if (stat.getState() == HighlightStatus.NORMAL) i = _highlightKeywords(v, i);
        }
      }
    }
    finally { releaseReadLock(); }
    
    // bstoler: Previously we moved back to the old location. This was
    // very bad and severly slowed down rendering when scrolling.
    // This is because parts are rendered in order. Thus, if old location is
    // 0, but now we've scrolled to display 100000-100100, if we keep
    // jumping back to 0 after getting every bit of highlight, it slows
    // stuff down incredibly.
    //setCurrentLocation(oldLocation);
    return v;
  }
  
  /** Distinguishes keywords from normal text in the given HighlightStatus element. Specifically, it looks to see
    * if the given text contains a keyword. If it does, it splits the HighlightStatus into separate blocks
    * so that each keyword has its own block. This process identifies all keywords in the given block.
    * Note that the given block must have state NORMAL.  Assumes that readLock is ALREADY HELD.
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
  
  /** Fire event that styles changed from current location to the end.
    *  Right now we do this every time there is an insertion or removal.
    *  Two possible future optimizations:
    *  <ol>
    *  <li>Only fire changed event if text other than that which was inserted
    *     or removed *actually* changed status. If we didn't changed the status
    *     of other text (by inserting or deleting unmatched pair of quote or
    *     comment chars), no change need be fired.
    *  <li>If a change must be fired, we could figure out the exact end
    *     of what has been changed. Right now we fire the event saying that
    *     everything changed to the end of the document.
    *  </ol>
    *
    *  I don't think we'll need to do either one since it's still fast now.
    *  I think this is because the UI only actually paints the things on the screen anyway.
    */
  protected abstract void _styleChanged(); 
  
  /** Clears the memozing cache of queries with offset greater than specified value.  Should be called every time the 
    * document is modified. */
  protected void _clearCache(int offset) {
    synchronized(_reduced) {
      if (offset < 0) {
        _queryCache.clear();
        _offsetToQueries.clear();
        return;
      }
      
      Integer[] deadOffsets = _offsetToQueries.tailMap(offset).keySet().toArray(new Integer[0]);
      for (int i: deadOffsets) {
        for (String query: _offsetToQueries.get(i)) {
          _queryCache.remove(query);  // remove query entry from cache
        }
        _offsetToQueries.remove(i);   // remove query bucket for i from offsetToQueries table
      }
    }
  }
  
  /** Add a character to the underlying reduced model. ASSUMEs _reduced lock is already held!
    *  @param curChar the character to be added. */
  private void _addCharToReducedModel(char curChar) {
    _clearCache(_currentLocation);
    _reduced.insertChar(curChar);
  }
  
  /** Get the current location of the cursor in the document.  Unlike the usual swing document model, 
    * which is stateless, because of our implementation of the underlying reduced model, we need to 
    * keep track of the current location.  Can be modified by any thread locking _reducedModel.
    * @return where the cursor is as the number of characters into the document 
    */
  public int getCurrentLocation() { return  _currentLocation; }
  
  /** Change the current location of the document
    * @param loc the new absolute location 
    */
  public void setCurrentLocation(int loc)  { 
    acquireReadLock();
    try { _setCurrentLocation(loc); }
    finally { releaseReadLock(); }
  }  
  
  /** Change the current location of the document assuming that ReadLock is already held
    * @param loc the new absolute location 
    */
  private void _setCurrentLocation(int loc) {
    synchronized(_reduced) {
      int dist = loc - _currentLocation;  // _currentLocation and _reduced can be updated asynchronously
      _currentLocation = loc;
      _reduced.move(dist);
    }
  }
  
  /** The actual cursor movement logic.  Helper for setCurrentLocation(int).
    * @param dist the distance from the current location to the new location.
    */
  public void move(int dist) {
    acquireReadLock();
    try {
      synchronized(_reduced) {
        int newLoc = _currentLocation + dist;
//        // location is set asynchronously when caret is moved so the following adjustment is necessary
//        // should no longer be true
//        if (newLoc < 0) {
//          assert false; // should never get here
//          dist -= newLoc; // increase dist by error in newLoc
//          newLoc = 0;
//        }
//        else {
//          int len = getLength();
//          if (newLoc > len) {
//            assert false; // should never get here
//            dist -= (newLoc - len); // decrease dist by error in newLoc
//            newLoc = len;
//          }
//        }
        _currentLocation = newLoc;
        _reduced.move(dist);
      }
    }
    finally { releaseReadLock(); }   
  }
  
  /** Forwarding method to find the match for the closing brace immediately to the left, assuming 
    *  there is such a brace.
    *  @return the relative distance backwards to the offset before the matching brace.
    */
  public int balanceBackward() {
    acquireReadLock();
    try { 
      synchronized(_reduced) { return _reduced.balanceBackward(); }
    }
    finally { releaseReadLock(); }  
  }
  
  /** Forwarding method to find the match for the open brace immediately to the right, assuming there 
    * is such a brace.
    * @return the relative distance forwards to the offset after the matching brace.
    */
  public int balanceForward() {
    acquireReadLock();
    try { synchronized(_reduced) { return _reduced.balanceForward(); } }
    finally { releaseReadLock(); }  
  }
  
  /** This method is used ONLY for testing.  This method is UNSAFE in any other context!
    * @return The reduced model of this document.
    */
  public BraceReduction getReduced() { return _reduced; }
  
  /** Returns the indent information for the current location. */
  public IndentInfo getIndentInformation() {
    // Check cache
    
    IndentInfo info;
    acquireReadLock();
    try {
      int loc = _currentLocation;
      String key = new StringBuilder("getIndentInformation:").append(loc).toString();
      
      IndentInfo cached = (IndentInfo) _checkCache(key);
      if (cached != null) return cached; 
      synchronized(_reduced) { info = _reduced.getIndentInformation(); } 
      _storeInCache(key, info, loc);
    }
    finally { releaseReadLock(); }  
    
    return info;
  }
  
  public ReducedModelState stateAtRelLocation(int dist) {
    acquireReadLock();
    try { synchronized(_reduced) { return _reduced.moveWalkerGetState(dist); } }
    finally { releaseReadLock(); }  
  }
  
  public ReducedModelState getStateAtCurrent() {
    acquireReadLock();
    try { synchronized(_reduced) { return _reduced.getStateAtCurrent(); } }
    finally { releaseReadLock(); } 
  }
  
  public void resetReducedModelLocation() {
    acquireReadLock();
    try { synchronized(_reduced) { _reduced.resetLocation(); } }
    finally { releaseReadLock(); } 
  }
  
  /** Searching backwards, finds the position of the enclosing brace.  Ignores comments.  Assumes readLock is already 
    * held!
    * @param pos Position to start from
    * @param opening opening brace character
    * @param closing closing brace character
    * @return position of enclosing squiggly brace, or ERROR_INDEX if beginning
    * of document is reached.
    */
  public int findPrevEnclosingBrace(int pos, char opening, char closing) throws BadLocationException {
    // Check cache
    final StringBuilder keyBuf = 
      new StringBuilder("findPrevEnclosingBrace:").append(opening).append(':').append(closing).append(':').append(pos);
    final String key = keyBuf.toString();
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    if (pos >= getLength() || pos == DOCSTART) { return ERROR_INDEX; }
    
    final char[] delims = {opening, closing};
    int reducedPos = pos;
    int i;  // index of for loop below
    int braceBalance = 0;
    
    String text = getText(DOCSTART, pos);
    
    synchronized(_reduced) {
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
          if (! state.equals(ReducedModelState.FREE) || _isStartOfComment(text, i) || 
              (i > 0 && _isStartOfComment(text, i-1)))
            continue;  // ignore matching char 
          else {
            // found valid matching char
            if (text.charAt(i) == closing) ++braceBalance;
            else {
              if (braceBalance == 0) break; // found our opening brace
              --braceBalance;
            }
          }
        }
      }
      
      /* Invariant: same as for loop except that DOCSTART-1 <= i <= reducedPos <= pos */
      
      _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
    }  // end synchronized
    
    if (i == DOCSTART-1) reducedPos = ERROR_INDEX; // No matching char was found
    _storeInCache(key, reducedPos, pos);
    
    // Return position of matching char or ERROR_INDEX 
    return reducedPos;  
  }
  
  /** Searching forward, finds the position of the enclosing squiggly brace. NB: ignores comments.
    *  @param pos Position to start from
    *  @param opening opening brace character
    *  @param closing closing brace character
    *  @return position of enclosing squiggly brace, or ERROR_INDEX if beginning of document is reached.
    */
  public int findNextEnclosingBrace(int pos, char opening, char closing) throws BadLocationException {
    // Check cache
    final StringBuilder keyBuf = 
      new StringBuilder("findNextEnclosingBrace:").append(opening).append(':').append(closing).append(':').append(pos);
    final String key = keyBuf.toString();
    final Integer cached = (Integer) _checkCache(key);
    
    if (cached != null) return cached.intValue();
    if (pos >= getLength()-1) { return ERROR_INDEX; }
    
    final char[] delims = {opening, closing};
    int reducedPos = pos;
    int i;  // index of for loop below
    int braceBalance = 0;
    
    acquireReadLock();
    String text = getText();
    try {      
      synchronized(_reduced) {
        final int origLocation = _currentLocation;
        // Move reduced model to location pos
        _reduced.move(pos - origLocation);  // reduced model points to pos == reducedPos
        
        // Walk forward from specificed position
        for (i = pos+1; i < text.length(); i++) {
          /* Invariant: reduced model points to reducedPos, text[pos:i-1] contains no valid delims, 
           * pos <= reducedPos < i <= text.length() */
          
          if (match(text.charAt(i),delims)) {
            // Move reduced model to walker's location
            _reduced.move(i - reducedPos);  // reduced model points to i
            reducedPos = i;                 // reduced model points to reducedPos
            
            // Check if matching char should be ignored because it is within a comment, quotes, or ignored paren phrase
            ReducedModelState state = _reduced.getStateAtCurrent();
            if (!state.equals(ReducedModelState.FREE) || _isStartOfComment(text, i)
                  || ((i > 0) && _isStartOfComment(text, i - 1)))
              continue;  // ignore matching char 
            else {
              // found valid matching char
              if (text.charAt(i)==opening) {
                ++braceBalance;
              }
              else {
                if (braceBalance==0) break; // found our closing brace
                --braceBalance;
              }
            }
          }
        }
        
        /* Invariant: same as for loop except that pos <= reducedPos <= i <= text.length() */
        
        _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
      }  // end synchronized
      
      if (i == text.length()) reducedPos = ERROR_INDEX; // No matching char was found
      _storeInCache(key, reducedPos, reducedPos);
      // Return position of matching char or ERROR_INDEX     
      return reducedPos;  
    }
    finally { releaseReadLock(); }
    
    
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
    final StringBuilder keyBuf = new StringBuilder("findPrevDelimiter:").append(pos);
    for (char ch: delims) { keyBuf.append(':').append(ch); }
    keyBuf.append(':').append(skipParenPhrases);
    final String key = keyBuf.toString();
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int reducedPos = pos;
    int i;  // index of for loop below
    acquireReadLock();
    try {
      String text = getText(DOCSTART, pos);
      
      synchronized(_reduced) {
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
            
            // Check if matching char should be ignored because it is within a comment, quotes, or ignored paren phrase
            ReducedModelState state = _reduced.getStateAtCurrent();
            if (! state.equals(ReducedModelState.FREE) || _isStartOfComment(text, i)
                  || ((i > 0) && _isStartOfComment(text, i - 1)) || (skipParenPhrases && posInParenPhrase()))
              continue;  // ignore matching char 
            else break;  // found valid matching char
          }
        }
        
        /* Invariant: same as for loop except that DOCSTART-1 <= i <= reducedPos <= pos */
        
        _reduced.move(origLocation - reducedPos);    // Restore the state of the reduced model;
      }  // end synchronized
      
      if (i == DOCSTART-1) reducedPos = ERROR_INDEX; // No matching char was found
      _storeInCache(key, reducedPos, pos);
      
      // Return position of matching char or ERROR_INDEX 
      return reducedPos;  
    }
    finally { releaseReadLock(); }
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
    
    acquireReadLock();
    try {
      prevFindChar = this.findPrevDelimiter(position, findCharDelims, false);
      
      if ((prevFindChar == DefinitionsDocument.ERROR_INDEX) || (prevFindChar < 0)) return false; // no such char
      
      // Determine if prevFindChar is findChar or the end of statement delimiter
      String foundString = this.getText(prevFindChar, 1);
      char foundChar = foundString.charAt(0);
      found = (foundChar == findChar);
    }
    catch (Throwable t) { throw new UnexpectedException(t); }
    finally { releaseReadLock(); }
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
    final StringBuilder keyBuf = new StringBuilder("findPrevCharPos:").append(pos).append(':').append(whitespace);
    final String key = keyBuf.toString();
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    int reducedPos = pos;
    int i = pos - 1;
    String text;
    acquireReadLock();
    try { 
      text = getText(0, pos); 
      
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
          
          if (_isReversteStartOfComment(text, i)) { /* char is second character in opening comment marker */  
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
      
      int result = reducedPos;
      if (i < 0) result = ERROR_INDEX;
      _storeInCache(key, result, pos);
      return result;
    }
    finally { releaseReadLock(); } 
  }
  
  /** Checks the query cache for a stored value.  Returns the value if it has been cached, or null 
    *  otherwise. Calling convention for keys: methodName:arg1:arg2
    *  @param key Name of the method and arguments
    */
  protected Object _checkCache(String key) {
    synchronized (_reduced) { return _queryCache.get(key); }
  }
  
  /** Stores the given result in the helper method cache. Calling convention for keys: methodName:arg1:arg2
    *  @param key Name of method and arguments
    *  @param result Result of the method call
    */
  protected void _storeInCache(String query, Object answer, int offset) {
    synchronized(_reduced) {
      _queryCache.put(query, answer);
      _addToOffsetsToQueries(query, offset);
    }
  }
  
  /** Add <query,offset> pair to _offsetToQueries map. Assumes lock on _queryCache is already held. */
  private void _addToOffsetsToQueries(String query, int offset) {
    List<String> selectedQueries = _offsetToQueries.get(offset);
    if (selectedQueries == null) {
      selectedQueries = new LinkedList<String>();
      _offsetToQueries.put(offset, selectedQueries);
    }
    selectedQueries.add(query);
  }
  
  /** Default indentation - uses OTHER flag and no progress indicator.
    *  @param selStart the offset of the initial character of the region to indent
    *  @param selEnd the offset of the last character of the region to indent
    */
  public void indentLines(int selStart, int selEnd) {
    try { indentLines(selStart, selEnd, Indenter.IndentReason.OTHER, null); }
    catch (OperationCanceledException oce) {
      // Indenting without a ProgressMonitor should never be cancelled!
      throw new UnexpectedException(oce);
    }
  }
  
  
  /** Parameterized indentation for special-case handling.  If selStart == selEnd, then the line containing the
    * currentLocation is indented.  The values of selStart and selEnd are ignored!
    * 
    * @param selStart the offset of the initial character of the region to indent
    * @param selEnd the offset of the last character of the region to indent
    * @param reason a flag from {@link Indenter} to indicate the reason for the indent
    *        (indent logic may vary slightly based on the trigger action)
    * @param pm used to display progress, null if no reporting is desired
    */
  public void indentLines(int selStart, int selEnd, Indenter.IndentReason reason, ProgressMonitor pm)
    throws OperationCanceledException {
    
    // Begins a compound edit.
    // int key = startCompoundEdit(); // commented out in connection with the FrenchKeyBoard Fix
    
    acquireWriteLock();
    try {
//      synchronized(_reduced) {   // Unnecessary. Write access is exclusive.
      if (selStart == selEnd) {  // single line to indent
//          Utilities.showDebug("selStart = " + selStart + " currentLocation = " + _currentLocation);
        Position oldCurrentPosition = createUnwrappedPosition(_currentLocation);
        
        // Indent, updating current location if necessary.
//          Utilities.showDebug("Indenting line at offset " + selStart);
        if (_indentLine(reason)) {
          _setCurrentLocation(oldCurrentPosition.getOffset());
          if (onlyWhiteSpaceBeforeCurrent()) {
            int space = getWhiteSpace();
            move(space);
          }
        }
      }
      else _indentBlock(selStart, selEnd, reason, pm);
//      }
    }
    catch (Throwable t) { throw new UnexpectedException(t); }
    finally { releaseWriteLock(); } 
    
    // Ends the compound edit.
    //endCompoundEdit(key);   //Changed to endLastCompoundEdit in connection with the FrenchKeyBoard Fix
    endLastCompoundEdit();
  }
  
  /** Indents the lines between and including the lines containing points start and end.  Assumes that writeLock
    *  and _reduced locks are already held.
    *  @param start Position in document to start indenting from
    *  @param end Position in document to end indenting at
    *  @param reason a flag from {@link Indenter} to indicate the reason for the indent
    *        (indent logic may vary slightly based on the trigger action)
    *  @param pm used to display progress, null if no reporting is desired
    */
  private void _indentBlock(final int start, final int end, Indenter.IndentReason reason, ProgressMonitor pm)
    throws OperationCanceledException, BadLocationException {
    
    // Keep marker at the end. This Position will be the correct endpoint no matter how we change 
    // the doc doing the indentLine calls.
    final Position endPos = this.createUnwrappedPosition(end);
    // Iterate, line by line, until we get to/past the end
    int walker = start;
    while (walker < endPos.getOffset()) {
      _setCurrentLocation(walker);
      // Keep pointer to walker position that will stay current
      // regardless of how indentLine changes things
      Position walkerPos = this.createUnwrappedPosition(walker);
      // Indent current line
      // We ignore current location info from each line, because it probably doesn't make sense in a block context.
      _indentLine(reason);  // this operation is atomic
      // Move back to walker spot
      _setCurrentLocation(walkerPos.getOffset());
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
  
  /** Indents a line using the Indenter.  Public ONLY for testing purposes. Assumes writeLock is already held.*/
  public boolean _indentLine(Indenter.IndentReason reason) { return getIndenter().indent(this, reason); }
  
  /** Returns the "intelligent" beginning of line.  If currPos is to the right of the first 
    *  non-whitespace character, the position of the first non-whitespace character is returned.  
    *  If currPos is at or to the left of the first non-whitespace character, the beginning of
    *  the line is returned.
    *  @param currPos A position on the current line
    */
  public int getIntelligentBeginLinePos(int currPos) throws BadLocationException {
    String prefix;
    int firstChar;
    acquireReadLock();
    try {
      firstChar = getLineStartPos(currPos);
      prefix = getText(firstChar, currPos-firstChar);
    }
    finally { releaseReadLock(); }
    
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
    char[] delims = {';', '{', '}'};
    char[] whitespace = {' ', '\t', '\n', ','};
    return getIndentOfCurrStmt(pos, delims, whitespace);
  }
  
  /** Returns the indent level of the start of the statement that the cursor is on.  Uses a default
    *  set of whitespace characters: {' ', '\t', '\n', ','}
    *  @param pos Cursor position
    */
  public String getIndentOfCurrStmt(int pos, char[] delims) throws BadLocationException {
    char[] whitespace = {' ', '\t', '\n',','};
    return getIndentOfCurrStmt(pos, delims, whitespace);
  }
  
  /** Returns the indent level of the start of the statement that the cursor is on.
    *  @param pos Cursor position
    *  @param delims Delimiter characters denoting end of statement
    *  @param whitespace characters to skip when looking for beginning of next statement
    */
  public String getIndentOfCurrStmt(int pos, char[] delims, char[] whitespace) throws BadLocationException {
    // Check cache
    final StringBuilder keyBuf = new StringBuilder("getIndentOfCurrStmt:").append(pos).append(':').append(whitespace);
    final String key = keyBuf.toString();
    final String cached = (String) _checkCache(key);
    if (cached != null) return cached;
    
    String lineText;
    
    acquireReadLock();
    try {
      synchronized(_reduced) {
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
        _storeInCache(key, lineText, pos);
      }
    }
    catch(Exception e) { throw new UnexpectedException(e); }
    finally { releaseReadLock(); }
    
    return lineText;
  }
  
  /** Determines if the given character exists on the line where the given cursor position is.  Does not 
    *  search in quotes or comments. <b>Does not work if character being searched for is a '/' or a '*'</b>.
    *  @param pos Cursor position
    *  @param findChar Character to search for
    *  @return true if this node's rule holds.
    */
  public int findCharOnLine(int pos, char findChar) {
    // Check cache
    String key = new StringBuilder("findCharOnLine:").append(pos).append(':').append(findChar).toString();
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int i;
    int matchIndex; // absolute index of matching character 
    
    acquireReadLock();
    try {
      synchronized(_reduced) {
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
      
      if (i == -1) matchIndex = ERROR_INDEX;
      _storeInCache(key, matchIndex, max(pos, matchIndex));
    }
    catch (Throwable t) { throw new UnexpectedException(t); }
    finally { releaseReadLock(); }
    
    return matchIndex;
  }
  
  /** Returns the absolute position of the beginning of the current line.  (Just after most recent newline, or DOCSTART)
    *  Doesn't ignore comments.
    *  @param pos Any position on the current line
    *  @return position of the beginning of this line
    */
  public int getLineStartPos(final int pos) {
    if (pos < 0 || pos > getLength()) return -1;
    // Check cache
    String key = "getLineStartPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int dist;
    acquireReadLock();
    try {
      synchronized(_reduced) {
        int location = _currentLocation;
        _reduced.move(pos - location);
        dist = _reduced.getDistToPreviousNewline(0);
        _reduced.move(location - pos);
      }
      
      int newPos = DOCSTART;
      if (dist >= 0)  newPos = pos - dist;
      _storeInCache(key, newPos, pos);
      return newPos;  // may equal DOCSTART
    }
    finally { releaseReadLock(); }
  }
  
  /** Returns the absolute position of the end of the current line.  (At the next newline, or the end of the document.)
    *  @param pos Any position on the current line
    *  @return position of the end of this line
    */
  public int getLineEndPos(final int pos) {
    if (pos < 0 || pos > getLength()) return -1;
    
    // Check cache
    String key = "getLineEndPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int dist, newPos;
    acquireReadLock();
    try {
      synchronized(_reduced) {
        int location = _currentLocation;
        _reduced.move(pos - location);
        dist = _reduced.getDistToNextNewline();
        _reduced.move(location - pos);
      }
      newPos = pos + dist;
      _storeInCache(key, newPos, newPos);
      return newPos;
    }
    finally { releaseReadLock(); }
  }
  
  /** Returns the absolute position of the first non-whitespace character on the current line.
    *  NB: Doesn't ignore comments.
    *  @param pos position on the line
    *  @return position of first non-whitespace character on this line, or the end
    *  of the line if no non-whitespace character is found.
    */
  public int getLineFirstCharPos(int pos) throws BadLocationException {
    // Check cache
    String key = "getLineFirstCharPos:" + pos;
    Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    acquireReadLock();
    try {
      final int startLinePos = getLineStartPos(pos);
      final int endLinePos = getLineEndPos(pos);
      int nonWSPos = endLinePos;
      
      // Get all text on this line
      String text = this.getText(startLinePos, endLinePos - startLinePos);
      int walker = 0;
      while (walker < text.length()) {
        if (text.charAt(walker) == ' ' || text.charAt(walker) == '\t') walker++;
        else {
          nonWSPos = startLinePos + walker;
          break;
        }
      }
      _storeInCache(key, nonWSPos, nonWSPos);
      return nonWSPos;  // may equal lineEndPos
    }
    finally { releaseReadLock(); }
  }
  
  /** Finds the position of the first non-whitespace character after pos. NB: Skips comments and all whitespace, 
    * including newlines
    * @param pos Position to start from
    * @return position of first non-whitespace character after pos, or ERROR_INDEX if end of document is reached
    */
  public int getFirstNonWSCharPos(int pos) throws BadLocationException {
    char[] whitespace = {' ', '\t', '\n'};
    return getFirstNonWSCharPos(pos, whitespace, false);
  }
  
  /** Similar to the single-argument version, but allows including comments.
    * @param pos Position to start from
    * @param acceptComments if true, find non-whitespace chars in comments
    * @return position of first non-whitespace character after pos,
    * or ERROR_INDEX if end of document is reached
    */
  public int getFirstNonWSCharPos(int pos, boolean acceptComments) throws BadLocationException {
    char[] whitespace = {' ', '\t', '\n'};
    return getFirstNonWSCharPos(pos, whitespace, acceptComments);
  }
  
  /** Finds the position of the first non-whitespace character after pos. NB: Skips comments and all whitespace, 
    *  including newlines.
    *  @param pos Position to start from
    *  @param whitespace array of whitespace chars to ignore
    *  @param acceptComments if true, find non-whitespace chars in comments
    *  @return position of first non-whitespace character after pos, or ERROR_INDEX if end of document is reached
    */
  public int getFirstNonWSCharPos(int pos, char[] whitespace, boolean acceptComments) throws BadLocationException {
    // Check cache
    final StringBuilder keyBuf = new StringBuilder("getFirstNonWSCharPos:").append(pos).append(':').append(whitespace);
    final String key = keyBuf.toString();
    
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    int result = ERROR_INDEX;  // correct result if no non-whitespace chars are found
    
    acquireReadLock();
    try {
      final int endPos = getLength();
      final int origLocation = _currentLocation;
      
      // Get text from pos to end of document
      String text = getText(pos, endPos - pos);
      int reducedPos = pos;
      
      
      // Move reduced model to location pos
      synchronized(_reduced) {
        _reduced.move(pos - origLocation);
        
        int i = pos;
        
        // Walk forward from specificed position
        while (i < endPos) {
          
          // Check if character is whitespace
          if (match(text.charAt(i-pos), whitespace)) {
            i++;
            continue;
          }
          // Found a non whitespace character
          // Move reduced model to walker's location for subsequent processing
          _reduced.move(i - reducedPos);  // reduced model points to location i
          reducedPos = i;
          
          // Check if non-ws char is within comment and if we want to ignore them.
          if (! acceptComments &&
              ((_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_LINE_COMMENT)) ||
               (_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_BLOCK_COMMENT)))) {
            i++;
            continue;
          }
          
          // Check if non-ws char is part of comment opening bracket and if we want to ignore them
          if (! acceptComments && _isStartOfComment(text, i - pos)) {
            // ith char is first char in comment open market; skip past this marker and continue searching
            i = i + 2;
            continue;
          }
          
          // Return position of matching char (reducedPos)
          break;
        }
        _reduced.move(origLocation - reducedPos);
        
        result = (i == endPos) ? ERROR_INDEX : reducedPos;
      }
      _storeInCache(key, result, reducedPos);
      return result;
    }
    finally { releaseReadLock(); }
  }
  
  public int findPrevNonWSCharPos(int pos) throws BadLocationException {
    char[] whitespace = {' ', '\t', '\n'};
    return findPrevCharPos(pos, whitespace);
  }
  
  /** Helper method for getFirstNonWSCharPos Determines whether the current character is the start of a comment: 
    *  "/*" or "//"
    */
  protected static boolean _isStartOfComment(String text, int pos) {
    char currChar = text.charAt(pos);
    if (currChar == '/') {
      try {
        char afterCurrChar = text.charAt(pos + 1);
        if ((afterCurrChar == '/') || (afterCurrChar == '*'))  return true;
      } catch (StringIndexOutOfBoundsException e) { }
    }
    return false;
  }
  
  /** Helper method for findPrevNonWSCharPos. Determines whether the current character is the start of a comment
    *  encountered from the end: '/' or '*' preceded by a '/'.
    *  @return true if (pos-1,pos) == '/*' or '//'
    */
  protected static boolean _isReversteStartOfComment(String text, int pos) {
    char currChar = text.charAt(pos);
    if ((currChar == '/')||(currChar == '*')) {
      try {
        char beforeCurrChar = text.charAt(pos - 1);
        if (beforeCurrChar == '/')  return true;
      } catch (StringIndexOutOfBoundsException e) { /* do nothing */ }
    }
    return false;
  }
  
  
  /** Returns true if the given position is inside a paren phrase.
    * @param pos the position we're looking at
    * @return true if pos is immediately inside parentheses
    */
  public boolean posInParenPhrase(int pos) {
    // Check cache
    String key = "posInParenPhrase:" + pos;
    
    boolean inParenPhrase;
    
    acquireReadLock();
    try {
      synchronized(_reduced) {
        Boolean cached = (Boolean) _checkCache(key);
        if (cached != null) return cached.booleanValue();
        int here = _currentLocation;
        _reduced.move(pos - here);
        inParenPhrase = posInParenPhrase();
        _reduced.move(here - pos);
      }
      _storeInCache(key, inParenPhrase, pos);
    }
    finally { releaseReadLock(); }
    
    return inParenPhrase;
  }
  
  /** Returns true if the reduced model's current position is inside a paren phrase.
    * @return true if pos is immediately inside parentheses
    */
  public boolean posInParenPhrase() {
    IndentInfo info;
    acquireReadLock();
    try { 
      synchronized(_reduced) { 
        info = _reduced.getIndentInformation(); 
        return info.braceTypeCurrent.equals(IndentInfo.openParen);
      } 
    }
    finally { releaseReadLock(); }
  }
  
  /** Returns true if the given position is not inside a paren/brace/etc phrase.  Assumes that read lock is ALREADY HELD.
    *  @param pos the position we're looking at
    *  @return true if pos is immediately inside a paren/brace/etc
    */
  protected boolean posNotInBlock(int pos) {
    // Check cache
    String key = "posNotInBlock:" + pos;
    
    boolean notInParenPhrase;
    
    synchronized(_reduced) {
      Boolean cached = (Boolean) _checkCache(key);
      if (cached != null) return cached.booleanValue();
      
      int here = _currentLocation;
      _reduced.move(pos - here);
      IndentInfo info = _reduced.getIndentInformation();
      notInParenPhrase = info.braceTypeCurrent.equals(IndentInfo.noBrace);
      _reduced.move(here - pos);
      _storeInCache(key, notInParenPhrase, pos);
      return notInParenPhrase;
    }
  }
  
  
  /** Gets the number of whitespace characters between the current location and the end of
    *  the document or the first non-whitespace character, whichever comes first.
    *  @return the number of whitespace characters
    */
  public int getWhiteSpace() {
    try { return  getWhiteSpaceBetween(0, getLength() - _currentLocation); } 
    catch (BadLocationException e) { e.printStackTrace(); }
    return  -1;
  }
  
  /** Starts at start and gets whitespace starting at relStart and either stopping at relEnd or at the first 
    *  non-white space char.
    *  NOTE: relStart and relEnd are relative to where we are in the document relStart must be <= _currentLocation
    *  @exception BadLocationException
    */
  private int getWhiteSpaceBetween(int relStart, int relEnd) throws BadLocationException {
    String text = this.getText(_currentLocation - relStart, Math.abs(relStart - relEnd));
    int i = 0;
    int length = text.length();
    while ((i < length) && (text.charAt(i) == ' '))
      i++;
    return  i;
  }
  
  /** Returns true if the current line has only white space before the current location. Serves as a check so that
    * indentation will only move the caret when it is at or before the "smart" beginning of a line (i.e. the first
    * non-whitespace character
    * @return true if there are only whitespace characters before the current location on the current line.
    */
  private boolean onlyWhiteSpaceBeforeCurrent() throws BadLocationException{
    String text = this.getText(0, _currentLocation);
    //get the text after the previous new line, but before the current location
    text = text.substring(text.lastIndexOf("\n")+1);
    
    //check all positions in the new text to determine if there are any non-whitespace chars
    int index = text.length()-1;
    char lastChar = ' ';
    while(lastChar == ' ' && index >= 0){
      lastChar = text.charAt(index);
      index--;
    }
    
    if (index < 0) return true;
    return false;
  }
  
  
  
  /** Sets text between previous newline and first non-whitespace character of line containing pos to tab.
    *  @param tab String to be placed between previous newline and first
    *  non-whitespace character
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
  
  /** Updates document structure as a result of text insertion. This happens after the text has actually been inserted.
    * Here we update the reduced model (using an {@link AbstractDJDocument.InsertCommand InsertCommand}) and store 
    * information for how to undo/redo the reduced model changes inside the {@link 
    * javax.swing.text.AbstractDocument.DefaultDocumentEvent DefaultDocumentEvent}.
    * NOTE: an exclusive read lock on the document is already held when this code runs.
    *
    * @see edu.rice.cs.drjava.model.AbstractDJDocument.InsertCommand
    * @see javax.swing.text.AbstractDocument.DefaultDocumentEvent
    * @see edu.rice.cs.drjava.model.definitions.DefinitionsDocument.CommandUndoableEdit
    */
  protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
    
    super.insertUpdate(chng, attr);
    
    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      final String str = getText(offset, length);
      
      _clearCache(offset);    // Selectively clear the query cache
      
      InsertCommand doCommand = new InsertCommand(offset, str);
      RemoveCommand undoCommand = new RemoveCommand(offset, length);
      
      // add the undo/redo
      addUndoRedo(chng, undoCommand, doCommand);
      //chng.addEdit(new CommandUndoableEdit(undoCommand, doCommand));
      // actually do the insert
      doCommand.run();  // Why isn't this run in the event thread?
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }
  
  /** Updates document structure as a result of text removal. This happens within the swing remove operation before
    * the text has actually been removed. Here we update the reduced model (using a {@link AbstractDJDocument.RemoveCommand
    * RemoveCommand}) and store information for how to undo/redo the reduced model changes inside the 
    * {@link javax.swing.text.AbstractDocument.DefaultDocumentEvent DefaultDocumentEvent}.
    * @see AbstractDJDocument.RemoveCommand
    * @see javax.swing.text.AbstractDocument.DefaultDocumentEvent
    */
  protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
    
    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      
      final String removedText = getText(offset, length);
      super.removeUpdate(chng);
      
      _clearCache(offset);  // Selectively clear the query cache
      
      Runnable doCommand = new RemoveCommand(offset, length);
      Runnable undoCommand = new InsertCommand(offset, removedText);
      
      // add the undo/redo info
      addUndoRedo(chng, undoCommand, doCommand);
      // actually do the removal from the reduced model
      doCommand.run();
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  
  /** Inserts a string of text into the document.  Custom processing of the insert (e.g., updating the reduced model)
    * is not done here;  it is done in {@link #insertUpdate}.
    */
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
    
    acquireWriteLock();
    try {
//      synchronized(_reduced) {    // Unnecessary.  The write lock on the document is exclusive.
//      clearCache(offset);         // Selectively clear the query cache; unnecessary: done in insertUpdate
      super.insertString(offset, str, a);
//      }
    }
    finally { releaseWriteLock(); }
  }
  
  /** Removes a block of text from the specified location.  We don't update the reduced model here; that happens
    *  in {@link #removeUpdate}.
    */
  public void remove(final int offset, final int len) throws BadLocationException {
    
    acquireWriteLock();
    try {
//      synchronized(_reduced) {   // Unnecessary.  The write lock on the document is exclusive.
//        clearCache();            // Selectively clear the query cache; unnecessary: done in removeUpdate.
        super.remove(offset, len);
//      }
    }
    finally { releaseWriteLock(); }  
  }
  
  public String getText() {
    acquireReadLock();
    try { return getText(0, getLength()); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
    finally { releaseReadLock(); }
  }
  
  /** Returns the byte image (as written to a file) of this document. */
  public byte[] getBytes() { return getText().getBytes(); }
  
  public void clear() {
    acquireWriteLock();
    try { remove(0, getLength()); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
    finally { releaseWriteLock(); }
  }
  
  //Two abstract methods to delegate to the undo manager, if one exists.
  protected abstract int startCompoundEdit();
  protected abstract void endCompoundEdit(int i);
  protected abstract void endLastCompoundEdit();
  protected abstract void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, Runnable doCommand);
  
  //Checks if the document is closed, and then throws an error if it is.
  
  //-------- INNER CLASSES ------------
  
  protected class InsertCommand implements Runnable {
    private final int _offset;
    private final String _text;
    
    public InsertCommand(final int offset, final String text) {
      _offset = offset;
      _text = text;
    }
    
    /** Inserts chars in reduced model and move location to end of insert; cache has already been selectively cleared. */
    public void run() {
      
//      acquireReadLock();  // Unnecessary! exclusive readLock should already be held!
//      try {
//        synchronized(_reduced) {  // Unnecessary?  no other thread should hold a readLock
      _reduced.move(_offset - _currentLocation);  
      int len = _text.length();
      // loop over string, inserting characters into reduced model
      for (int i = 0; i < len; i++) {
        char curChar = _text.charAt(i);
        _addCharToReducedModel(curChar);
      }
      _currentLocation = _offset + len;  // current location is at end of inserted string
      _styleChanged();
//        }
//      }
//      finally { releaseReadLock(); }
    }
  }
  
  protected class RemoveCommand implements Runnable {
    private final int _offset;
    private final int _length;
    
    public RemoveCommand(final int offset, final int length) {
      _offset = offset;
      _length = length;
    }
    
    /** Removes chars from reduced model; cache has already been selectively cleared. */
    public void run() {
//      acquireReadLock();  // unnecessary! exclusive readLock should already be held!
//      try {
//        synchronized(_reduced) {  // unnecessary?  no other thread should hold a readLock
      _setCurrentLocation(_offset);
      _reduced.delete(_length);    
      _styleChanged();
//        }
//      }
//      finally { releaseReadLock(); } 
    }
  }
}
