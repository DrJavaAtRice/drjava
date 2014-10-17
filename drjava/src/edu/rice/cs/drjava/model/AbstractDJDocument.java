/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.OptionEvent;
import edu.rice.cs.drjava.config.OptionListener;

import edu.rice.cs.drjava.model.compiler.ScalaCompiler;

import edu.rice.cs.drjava.model.definitions.indent.Indenter;
import edu.rice.cs.drjava.model.definitions.reducedmodel.BraceInfo;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelControl;
import edu.rice.cs.drjava.model.definitions.reducedmodel.HighlightStatus;
import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelState;

import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.SwingDocument;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.swing.ProgressMonitor;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

import static edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates.*;

/** This class contains code supporting the concept of a "DJDocument"; it is shared between DefinitionsDocument and 
  * InteractionsDJDocument. This partial implementation of <code>Document</code> contains a "reduced model". The reduced
  * model is automatically kept in sync when this document is updated. Also, that synchronization is maintained even 
  * across undo/redo -- this is done by making the undo/redo commands know how to restore the reduced model state.
  *
  * The reduced model is not thread-safe, so it is essential that its methods are only called from the event thread.  In
  * addition, any information from the reduced model should be obtained through helper methods in this class/subclasses.
  *
  * @see edu.rice.cs.drjava.model.definitions.reducedmodel.BraceReduction
  * @see edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelControl
  * @see edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelComment
  * @see edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelBrace
  * TODO: create specialized methods where opening and closing are passed as args and then assembled into a char[] array,
  * which is used in method call where caching takes place; the cache will not recognize duplicate calls because the char[]
  * argument is rebuilt every time
  */
public abstract class AbstractDJDocument extends SwingDocument implements DJDocument, OptionConstants {
  
  /*-------- CONSTANTS ----------*/
  
  /* All of the following char[] arrays appear in ascending order so that they support Arrays.binarySearch. */
  public static final char[] DEFAULT_DELIMS                = {';', '{', '}'};
  public static final char[] SCALA_DELIMS                  = {'(', ')', ';', '=', '>', '{', '}'};
  public static final char[] OPENING_DELIMS_AND_NEWLINE    = {'\n', '(', '=', '>', '{'};
  public static final char[] DEFAULT_WHITESPACE            = {'\t', '\n', '\r', ' '};  // Sorted!
  public static final char[] DEFAULT_WHITESPACE_WITH_COMMA = {'\t', '\n', '\r', ' ', ','};
  public static final char[] NEWLINE                       = {'\n'};
  public static final char[] EQUALS                        = {'='};
  public static final char[] STRICT_OPENING_BRACES         = {'(', '[', '{'};
  public static final char[] OPENING_BRACES                = {'(', '>', '[', '{'};  // includes pseudo-brace "=>"
  public static final char[] OPENING_BRACES_WITH_EQUALS    = {'(', '=', '>', '[', '{'};  // includes pseudo-braces "=" & "=>"
  public static final char[] CLOSING_BRACES                = {')', ']', '}'};  
  public static final char[] SEMICOLON_MARKERS             = {'\n', '(', ')', '>', '[', ']', '{', '}'};
  
  public static final char[] NOT_TERMINATING_CHARS         =   
    {'!', '%', '&', '(', '*', '+', '-', '.', '/', '<', '=', '>', '[', '^', '{', '|'};
  public static final char[] ALPHANUMERIC                  = 
    {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R',
     'S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t',
     'u','v','w','x','y','z'};
  
  private static final char[] DOUBLE_DELIMS = {'\t','\n','\r',' ','%',')','*','+','-','/',':','<','=','>'};
  private static final char[] ILLEGAL_PREFIX_CHARS = {'!', '%', '&', '*', '+', '-', '/', '<', '=', '>', '^', '|'};
  

//  private static final String[] PREFIXES = 
//    new String[] {"if", "else", "val", "var", "def", "class", "case", "trait", "import", "implicit", "override", "type",
//    "sealed", "}", "/*", "//" };
  
  private static final String DELIMITERS = " \t\n\r{}()[].+-/*;:=!@#$%^&*~<>?,\"`'<>|";  // not in sorted order !

  protected static final char newline = '\n';
  
  // error index for indexing String or Document; program logic assumes that it is negative
  public static final int ERROR_INDEX = -1;
  
  // index of '.' in NOT_TERMINATING_CHARS */
  private static final int DOT_INDEX = Arrays.binarySearch(NOT_TERMINATING_CHARS, '.');
  
  // TODO: set up documents so that keywords, primwords are different for .scala and .java files
  
  /** A set of Scala keywords. */
  protected volatile Set<String> _keywords = ScalaCompiler.SCALA_KEYWORDS;
  /** A set of Java primitive types. */  // TODO: change "prim" to "value"
  protected static final Set<String> _primTypes = _makePrimTypes();
  /** The default indent setting. */
  protected volatile int _indent = 2;
  /** Initial number of elements in _queryCache (see below). */
  private static final int INIT_CACHE_SIZE = 0x10000;  // 16**4 = 16384 
  /** Constant specifying how large pos must be before incremental analysis is applied in posInBlockComment */
  public static final int POS_THRESHOLD = 10000; 
//  /** Constant specifying how large pos must be before incremental analysis is applied in posInParenPhrase */
//  public static final int POS_THRESHOLD = 10000; 

  
//  /** Whether a block indent operation is in progress on this document. */
//  private volatile boolean _indentInProgress = false;
  
  /** The reduced model of the document (stored in field _reduced) handles most of the document logic and keeps 
    * track of state.  This field together with _currentLocation function as a virtual object for purposes of 
    * synchronization.  All operations that access or modify this virtual object should be performed in the event
    * thread [Archaic: synchronized on _reduced].
    */
  protected final ReducedModelControl _reduced = new ReducedModelControl();
  
  /** The absolute character offset in the document. Treated as part of the _reduced (model) for locking 
    * purposes. */
  protected volatile int _currentLocation = 0;
  
  /* The fields _queryCache, _offsetToQueries, and _cacheModified function as an extension of the reduced model. 
   * When enabled in blockIndent, this data structure caches calls to the reduced model to speed up indent performance.
   * Must be cleared every time the document is changed.  Use by calling _checkCache, _storeInCache, and _clearCache.
   * When _queryCache = null, the cache is disabled.
   */
  private volatile HashMap<Query, Object> _queryCache;
  
  /** Records the set of queries (as a list) for each offset. */
  private volatile SortedMap<Integer, List<Query>> _offsetToQueries;
  
  /** The instance of the indent decision tree used by Definitions documents. */
  private volatile Indenter _indenter;
  
  /* Saved here to allow the listener to be removed easily. This is needed to allow for garbage collection. */
  private volatile OptionListener<Integer> _listener1;
  private volatile OptionListener<Boolean> _listener2;
  
  /*-------- CONSTRUCTORS --------*/
  
  /** Constructor used in super calls from DefinitionsDocument and InteractionsDJDocument. */
  protected AbstractDJDocument() { 
    this(new Indenter(DrJava.getConfig().getSetting(INDENT_LEVEL).intValue()));
  }
  
  /** Constructor used from anonymous test classes. */
  protected AbstractDJDocument(int indentLevel) { 
    this(new Indenter(indentLevel));
  }
  
  /** Constructor used to build a new document with an existing indenter.  Used in tests and super calls from 
    * DefinitionsDocument and interactions documents. */
  protected AbstractDJDocument(Indenter indenter) { 
    _indenter = indenter;
    _queryCache = null;
    _offsetToQueries = null;
    _initNewIndenter();
//     System.err.println("AbstractDJDocument constructor with indent level " + indenter.getIndentLevel() 
//    + " invoked on " + this);
    // ensure that char arrays are sorted
    Arrays.sort(DEFAULT_DELIMS);
    Arrays.sort(SCALA_DELIMS);
    Arrays.sort(OPENING_DELIMS_AND_NEWLINE);
    Arrays.sort(DEFAULT_WHITESPACE);
    Arrays.sort(DEFAULT_WHITESPACE_WITH_COMMA);
    Arrays.sort(NEWLINE);
    Arrays.sort(EQUALS);
    Arrays.sort(STRICT_OPENING_BRACES);
    Arrays.sort(OPENING_BRACES);
    Arrays.sort(OPENING_BRACES_WITH_EQUALS);
    Arrays.sort(CLOSING_BRACES);
    Arrays.sort(SEMICOLON_MARKERS);
    Arrays.sort(NOT_TERMINATING_CHARS); 
    Arrays.sort(ALPHANUMERIC);
    Arrays.sort(DOUBLE_DELIMS);
    Arrays.sort(ILLEGAL_PREFIX_CHARS);             
  }
  
  //-------- METHODS ---------//
  
  /** NOTE: methods with names beginning with underscore do not throw checked exceptions; methods with names not beginning
    * with underscore generally do. */
  
  /** Get the indenter.
    * @return the indenter
    */
  private Indenter getIndenter() { return _indenter; }
  
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
//    System.err.println("REMOVE INDENTER called");
    DrJava.getConfig().removeOptionListener(INDENT_LEVEL, _listener1);
    DrJava.getConfig().removeOptionListener(AUTO_CLOSE_COMMENTS, _listener2);
  }
  
  /** Only called from within getIndenter(). */
  private void _initNewIndenter() {
    // Create the indenter from the config values
    
    final Indenter indenter = _indenter;
//    System.err.println("Installing Indent Option Listener for " + this);
    _listener1 = new OptionListener<Integer>() {
      public void optionChanged(OptionEvent<Integer> oce) {
//        System.err.println("Changing INDENT_LEVEL for " + this + " to " + oce.value);
        indenter.buildTree(oce.value);
      }
    };
    
    _listener2 = new OptionListener<Boolean>() {
      public void optionChanged(OptionEvent<Boolean> oce) {
//        System.err.println("Reconfiguring indenter to use AUTO_CLOSE_COMMENTS = " + oce.value);
        indenter.buildTree(DrJava.getConfig().getSetting(INDENT_LEVEL));
      }
    };
    
    DrJava.getConfig().addOptionListener(INDENT_LEVEL, _listener1);
    DrJava.getConfig().addOptionListener(AUTO_CLOSE_COMMENTS, _listener2);
  }

  /** Set the specified keywords as keywords for syntax highlighting.
    * @param keywords keywords to highlight */
  public void setKeywords(Set<String> keywords) { _keywords = keywords; }

  /** Create a set of Java/GJ primitive types for special coloring.
    * @return the set of primitive types
    * TODO; move this code to CompilerInterface so Scala and Java can have different prim/value type names
    */
  protected static HashSet<String> _makePrimTypes() {
    final String[] words =  {
      "Boolean", "Char", "Byte", "Short", "Int", "Long", "Float", "Double", "Unit",
    };
    HashSet<String> prims = new HashSet<String>();
    for (String w: words) { prims.add(w); }
    return prims;
  }
  
  /** Return all highlight status info for text between start and end. This should collapse adjoining blocks with the
    * same status into one.  ONLY runs in the event thread.  Perturbs _currentLocation to improve performance.
    */
  public ArrayList<HighlightStatus> getHighlightStatus(int start, int end) {
    
    assert EventQueue.isDispatchThread();
    
    if (start == end) return new ArrayList<HighlightStatus>(0);
    ArrayList<HighlightStatus> v;
    
    setCurrentLocation(start);
    /* Now ask reduced model for highlight status for chars till end */
    v = _reduced.getHighlightStatus(start, end - start);
    
    /* Go through and find any NORMAL blocks. Within them check for keywords. */
    for (int i = 0; i < v.size(); i++) {
      HighlightStatus stat = v.get(i);
      if (stat.getState() == HighlightStatus.NORMAL) i = _highlightKeywords(v, i);
    }
    
    /* bstoler: Previously we moved back to the old location. This implementation choice severely slowed down 
     * rendering when scrolling because parts are rendered in order. Thus, if old location is 0, but now we've
     * scrolled to display 100000-100100, if we keep jumping back to 0 after getting every bit of highlight, it 
     * slows stuff down incredibly. */
    //setCurrentLocation(oldLocation);
    return v;
  }

  /** Distinguishes keywords from normal text in the given HighlightStatus element. Specifically, it looks to see
    * if the given text contains a keyword. If it does, it splits the HighlightStatus block into separate blocks
    * so that each keyword has its own block. This process identifies all keywords in the given block.
    * Note that the given block must have state NORMAL.  Only runs in the event thread.  Perturbs _currentLocation.
    * @param v Vector with highlight info
    * @param i Index of the single HighlightStatus to check for keywords in
    * @return the index into the vector of the last processed element
    */
  private int _highlightKeywords(ArrayList<HighlightStatus> v, int i) {
    // Basically all non-alphanumeric chars are delimiters
    final HighlightStatus original = v.get(i);
    final String text;
    
    try { text = getText(original.getLocation(), original.getLength()); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    // Because this text is not quoted or commented, we can use the simpler tokenizer StringTokenizer. We have 
    // to return delimiters as tokens so we can keep track of positions in the original string.
    StringTokenizer tokenizer = new StringTokenizer(text, DELIMITERS, true);
    
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
    * @return true if x is a parseable number, i.e. either parsable as a double or as a long after chopping off a possible trailing 'L' or 'l'
    */
  static boolean _isNum(String x) {
    try {
      Double.parseDouble(x);
      return true;
    } 
    catch (NumberFormatException e) {
      int radix = 10;
      int begin = 0;
      int end = x.length();
      int bits = 32;
      if (end-begin>1) {
        // string is not empty
        char ch = x.charAt(end - 1);
        if ((ch=='l')||(ch=='L')) { // skip trailing 'l' or 'L'
          --end;
          bits = 64;  
        }
        if (end-begin>1) {
          // string is not empty
          if (x.charAt(0) == '0') { // skip leading '0' of octal or hexadecimal literal
            ++begin;
            radix = 8;
            if (end-begin>1) {
              // string is not empty
              ch = x.charAt(1);
              if ((ch=='x')||(ch=='X')) { // skip 'x' or 'X' from hexadecimal literal
                ++begin;
                radix = 16;
              }
            }
          }
        }
      }
      try {
        // BigInteger can parse hex numbers representing negative longs; Long can't
        java.math.BigInteger val = new java.math.BigInteger(x.substring(begin, end), radix);
        return (val.bitLength() <= bits);
      }
      catch (NumberFormatException e2) {
        return false;
      }
    }
  }
  
  /** Checks to see if the current string is a type. A type is assumed to be a primitive type OR
    * anything else that begins with a capitalized character
    */
  private boolean _isType(String x) {
    if (_primTypes.contains(x)) return true;
    
    try { return Character.isUpperCase(x.charAt(0)); } 
    catch (IndexOutOfBoundsException e) { return false; }
  }
  
  /** Returns whether the given text only has spaces. */
  public static boolean hasOnlySpaces(String text) { return (text.trim().length() == 0); }
  
  /** Fire event that styles changed from current location to the end.
    * Right now we do this every time there is an insertion or removal.
    * Two possible future optimizations:
    * <ol>
    * <li>Only fire changed event if text other than that which was inserted
    *    or removed *actually* changed status. If we didn't changed the status
    *    of other text (by inserting or deleting unmatched pair of quote or
    *    comment chars), no change need be fired.
    * <li>If a change must be fired, we could figure out the exact end
    *    of what has been changed. Right now we fire the event saying that
    *    everything changed to the end of the document.
    * </ol>
    *
    * I don't think we'll need to do either one since it's still fast now.
    * I think this is because the UI only actually paints the things on the screen anyway.
    */
  protected abstract void _styleChanged(); 
  
  /** Add a character to the underlying reduced model. ASSUMEs _reduced lock is already held! [Archaic]
    * @param curChar the character to be added. */
  private void _addCharToReducedModel(char curChar) {
//    _clearCache(_currentLocation);  // redundant; already done in insertUpdate
    _reduced.insertChar(curChar);
  }
  
  /** Get the current location of the cursor in the document.  Unlike the usual swing document model, which is 
    * stateless, we maintain a cursor position within our implementation of the reduced model.  Can only be modified 
    * in the event thread!.  The returned value may be stale if accessed outside of the event thread.
    * @return where the cursor is as the number of characters into the document
    */
  public int getCurrentLocation() { return _currentLocation; }
  
  /** Returns the current line as a String; if lineStart or lineEnd cannot be found, returns "". */
  public String _getCurrentLine() {
    return _getCurrentLine(_currentLocation);
  }

  /** Returns the current line as a String; if lineStart or lineEnd cannot be found, returns "". */
  public String _getCurrentLine(int pos) {
    int start = _getLineStartPos(pos);
    int end = _getLineEndPos(pos);
    if (start < 0 || end < 0) return "";
    int diff = end - start;
    return _getText(start, diff);
  }
  
  /** Returns the previous line as a String; if no previous line exists, returns "". */
  public String _getPrevLine() {
    int endPlus = _getLineStartPos();
    if (endPlus <= 0) return ""; // There is no preceding line
    int end = endPlus - 1;
    int start = _getLineStartPos(end);
    int diff = end - start;
    return _getText(start, diff);
  }
  
  /** Change the current location of the document.  Only runs in the event thread.
    * @param loc the new absolute location 
    */
  public void setCurrentLocation(int loc) {
    if (loc < 0) {
      throw new UnexpectedException("Illegal location " + loc);  // was loc = 0
    }
    if (loc > getLength()) {
      throw new UnexpectedException("Illegal location " + loc + "; maximum location is " + getLength()); // was loc = getLength();
    }
    int dist = loc - _currentLocation;  // _currentLocation and _reduced can be updated asynchronously
    _currentLocation = loc;
    _reduced.move(dist);   // must call _reduced.move here; this._move changes _currentLocation
//    System.err.println("_setCurrentLocation(" + loc + ") executed");
  }
  
  /** Moves _currentLocation the specified distance.
    * Identical to _setCurrentLocation, except that input arg is relative rather than absolute and the new location
    * is bounds checked.  Only runs in the event thread.
    * @param dist the distance from the current location to the new location.
    */
  public void move(int dist) {
    int newLocation = _currentLocation + dist;
    if (0 <= newLocation && newLocation <= getLength()) {
      _reduced.move(dist);
      _currentLocation = newLocation;
    }
    else throw new IllegalArgumentException("AbstractDJDocument.move(" + dist + ") places the cursor at " + 
                                            newLocation + " which is out of range");
  } 
  
  /** Finds the distance to the match for the closing brace immediately to the left of _currentocation, assuming 
    * there is such a brace.  On failure, returns ERROR_INDEX.  Only runs in the event thread.
    * @return the relative distance backwards to the offset before the matching brace.
    */
  public int balanceBackward() { 
    int origPos = _currentLocation;
    try {
      if (_currentLocation < 2) return ERROR_INDEX;
      char prevChar = _getText(_currentLocation - 1, 1).charAt(0);
//      Utilities.show("_currentLocation = " + _currentLocation + "; prevChar = '" + prevChar + "'");
      if (prevChar != '}' && prevChar != ')' && prevChar != ']') return ERROR_INDEX;
      return _reduced.balanceBackward();
    }
    finally { setCurrentLocation(origPos); }
  }
  
  /** FindS the match for the open brace immediately to the right, assuming there is such a brace.  On failure, 
    * returns ERROR_INDEX (-1).  Only runs in event thread.
    * @return the relative distance forwards to the offset after the matching brace.
    */
  public int balanceForward() {
    int origPos = _currentLocation;
    try {
      if (_currentLocation == 0) return ERROR_INDEX;
      char prevChar = _getText(_currentLocation - 1, 1).charAt(0);
//      System.err.println("_currentLocation = " + _currentLocation + "; prevChar = '" + prevChar + "'");
      if (prevChar != '{' && prevChar != '(' && prevChar != '[') return ERROR_INDEX;
//      System.err.println("Calling _reduced.balanceForward()");
      return _reduced.balanceForward() ; 
    }
    finally { setCurrentLocation(origPos); }
  }
  
  /** This method is used ONLY called from event thread.  This method is UNSAFE in any other context!
    * @return The reduced model of this document.
    */
  public ReducedModelControl getReduced() { return _reduced; } 
  
  /** Assumes that reduced lock is already held. */
  public ReducedModelState stateAtRelLocation(int dist) { return _reduced.moveWalkerGetState(dist); }
  
  /** Assumes that reduced lock is already held. */
  public ReducedModelState getStateAtCurrent() { 
    /* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    return _reduced.getStateAtCurrent(); 
  }
  
  /** Assumes that reduced lock is already held. */
  public void resetReducedModelLocation() { _reduced.resetLocation(); }
  
  /** Searching backwards, finds the position of the enclosing brace of specified type.  Ignores comments.  Only runs in
    * event thread.
    * @param pos Position to start from
    * @param opening opening brace character
    * @param closing closing brace character
    * @return position of enclosing brace, or ERROR_INDEX (-1) if beginning
    * of document is reached.
    */
  public int findPrevEnclosingBrace(final int pos, final char opening, final char closing) throws BadLocationException {
    
//    System.err.println("***** FindPrevEnclosingBrace(" + pos + ", opening = '" + opening + "', closing = '" + closing + "'");
//    System.err.println("  Corresponding line = '" + _getCurrentLine(pos) + "'");
    // assert EventQueue.isDispatchThread();
    // Check cache
    final Query key = new Query.PrevEnclosingBrace(pos, opening);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    if (pos >= getLength() || pos <= 0) { return ERROR_INDEX; }
    
    final char[] delims = {opening, closing};
    int origPos = _currentLocation;
    int reducedPos = pos;
    int i = pos -1;  // index of for loop below; compiler requires initialization
    int braceBalance = 0;
    
    String text = getText(0, pos);
    
    // Move reduced model to location pos
    setCurrentLocation(pos);  // reduced model points to pos == reducedPos
    
    // Walk backwards from specificed position
    try {
      for (i = pos - 1; i >= 0; i--) {
        /* Invariant: reduced model points to reducedPos, text[i+1:pos] contains no valid delims, 
         * 0 <= i < reducedPos <= pos */
        
        if (match(text.charAt(i), delims)) {
          // Move reduced model to walker's location
          setCurrentLocation(i);  // reduced model points to i
          reducedPos = i;          // reduced model points to reducedPos
          
          // Check if matching char should be ignored because it is within a comment, 
          // quotes, or ignored paren phrase
          if (isShadowed()) continue;  // ignore matching char 
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
    }
    finally { setCurrentLocation(origPos); }    // Restore the state of the reduced model;
    
    /* Invariant: same as for loop except that -1 <= i <= reducedPos <= pos */
    
    if (i == ERROR_INDEX) reducedPos = ERROR_INDEX; // No matching char was found
    
    _storeInCache(key, reducedPos, pos - 1);
    
    // Return position of matching char or ERROR_INDEX (-1) 
    return reducedPos;  
  }
  
  /** @return true iff _currentLocation is inside comment or string excluding opening char of a comment */
  public boolean isShadowed() { return _reduced.isShadowed(); }
  
  /** @return true iff _currentLocation is inside a block or line comment or string */
  public boolean isWeaklyShadowed(boolean shadowStrings) { return _reduced.isWeaklyShadowed(shadowStrings); }
  
  /** @return true iff specified pos is inside comment or string. */
  public boolean isShadowed(int pos) {
    int origPos = _currentLocation;
    try {
      setCurrentLocation(pos);
      return isShadowed();
    }
    finally { setCurrentLocation(origPos); }
  }
  
  public boolean isWeaklyShadowed(int pos, boolean shadowStrings) {
    int origPos = _currentLocation;
    try {
      setCurrentLocation(pos);
      return isWeaklyShadowed(shadowStrings);
    }
    finally { setCurrentLocation(origPos); }
  }
  
  public boolean isWeaklyShadowed(int pos) { return isWeaklyShadowed(pos, false); }
  
  /** Searching forward, finds the position of the next enclosing brace, which may be a pointy bracket, ignoring 
    * comments.  Only runs in event thread.
    * @param pos Position to start from
    * @param opening opening brace character
    * @param closing closing brace character
    * @return position of enclosing brace, or ERROR_INDEX (-1) if beginning of document is reached.
    */
  public int findNextEnclosingBrace(final int pos, final char opening, final char closing) throws BadLocationException {
    assert EventQueue.isDispatchThread();
    
//    System.err.println("***** FindNextEnclosingBrace(" + pos + ", opening = '" + opening + "', closing = '" + closing + "'");
//    System.err.println("  Corresponding line = '" + _getCurrentLine(pos) + "'");
    
    // Check cache
    final Query key = new Query.NextEnclosingBrace(pos, opening, closing);
    final Integer cached = (Integer) _checkCache(key);
    
    if (cached != null) return cached.intValue();
    if (pos >= getLength() - 1) { return ERROR_INDEX; }
    
    final int origPos = _currentLocation;
    
    final char[] delims = {opening, closing};
    int reducedPos = pos;
    int i;  // index of for loop below
    int braceBalance = 0;
    
    String text = getText();
    
    // Move reduced model to location pos
    setCurrentLocation(pos);  // reduced model points to pos == reducedPos
    
    // Walk forward from specificed position
    for (i = pos + 1; i < text.length(); i++) {
      /* Invariant: reduced model points to reducedPos, text[pos:i-1] contains no valid delims, 
       * pos <= reducedPos < i <= text.length() */
      
      if (match(text.charAt(i),delims)) {
        // Move reduced model to walker's location
        setCurrentLocation(i);  // reduced model points to i
        reducedPos = i;          // reduced model points to reducedPos
        
        // Check if matching char should be ignored because it is within a comment, quotes, or ignored paren phrase
        if (isShadowed()) continue;  // ignore matching char 
        else {
          // found valid matching char
          if (text.charAt(i) == opening) ++braceBalance;
          else {
            if (braceBalance == 0) break; // found our closing brace
            --braceBalance;
          }
        }
      }
    }
    
    /* Invariant: same as for loop except that pos <= reducedPos <= i <= text.length() */
    
    setCurrentLocation(origPos);    // Restore the state of the reduced model;
    
    if (i == text.length()) reducedPos = ERROR_INDEX; // No matching char was found

    _storeInCache(key, reducedPos, reducedPos);
    // Return position of matching char or ERROR_INDEX (-1)     
    return reducedPos;  
  }
  
  /** Searching backwards, finds the position of the first character that is one of the given delimiters, ignoring
    * phrases bracketed by parens, curly braces, or comments.  The search also ignores the contents of strings and
    * hence cannot find '"" as a delimiter.  The array of delimiters MUST APPEAR IN ASCENDING ORDER 
    * to support using Array.binarySearch.  See below for description of special case treatment of ">" and "=" as
    * delimters.
    * @param pos Position to start from
    * @param delims array of characters to search IN ASCENDING ORDER
    * @return position of first matching delimiter, or ERROR_INDEX (-1) if beginning of document is reached.
    */
  public int findPrevDelimiter(int pos, char[] delims) throws BadLocationException {
    return findPrevDelimiter(pos, delims, true);
  }
  
  /** Searching backwards, finds position of first character preceding pos that is a given delimiter, skipping over 
    * comments and phrases bracketed in parens or curly braces, if so instructed.  The search also ignores the contents 
    * of strings and hence cannot find '"" as a delimiter.  This method should only be called from pos inside 
    * explict/implicit enclosing brace (??)  If '>' appears in the delims array, this method looks for "=>" instead of 
    * ">" so that the implicit brace opening a case body can be found.  If '=' appears in the delims array, this method 
    * looks for '=' preceded by a character other than '!', '=', or a binary operator and an empty rest of line.  
    * The individual char '>' is not supported as a possible delimiter. In addition, if skipOverBraces is enabled, 
    * the delims must exclude ')' and '}'.  Otherwise no skipping will happen! 
    * The array of delimiters MUST APPEAR IN ASCENDING ORDER to support using Array.binarySearch.
    * Only runs in event thread.
    * Question (??): does this method work when there is no enclosing brace; it superficially appears that it does.
    * TODO: make this method significantly faster; may require re-factoring the reduced model
    * @param pos Position to start from
    * @param delims array of characters to search for; must exclude '}' and ')' if skipBracePhrases is true
    * @param skipBracePhrases whether to look for delimiters inside brace phrases
    * @return position of first matching delimiter, or ERROR_INDEX (-1) if beginning of document is reached.
    */
  public int findPrevDelimiter(final int pos, final char[] delims, final boolean skipBracePhrases)
    throws BadLocationException {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
//    System.err.println("***** findPrevDelimiter(" + pos + ", " + Arrays.toString(delims) + ", " + 
//                       skipBracePhrases + ")");
    
    // Check cache
    final Query key = new Query.PrevDelimiter(pos, delims, skipBracePhrases);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
//      System.err.println(cached.intValue() + " found in cache");
      return cached.intValue();
    }
    
    if (pos == 0) return ERROR_INDEX;
    
//    System.err.println("Executing delim search loop");
    
    int i = pos - 1;  // index for for loop below
    String text = getText();
    final int origPos = getCurrentLocation();
    
    // Walk backwards from specificed position, scanning current line for a delimiter
    while (i >= 0) {
//      System.err.println("findPrevDelimiter loop iteration: " + i);
      /* Invariant: 0 <= i < pos; text[i+1:pos] contains no valid delims */

      setCurrentLocation(i);  // reduced model points to i
      if (isWeaklyShadowed(true)) {  // includes chars in string literals!
//            System.err.println(text.charAt(i) + " at pos " + i + " is shadowed");
        i--; continue;  // ignore current char (text[i]) because it is shadowed
      }
      char ch = text.charAt(i);
//      System.err.println("Testing delimiter match for character " + ch + " against " + Arrays.toString(delims));
      
      if (match(ch, delims)) {
//        System.err.println("Matching char '" + ch + "' found at pos " + i);
        /* The following is an UGLY hack to enable treating "=>" as a delimiter.  If '>' is a delimiter char, then
         * it signifies the two character delimiter "=>". */ 
        if (ch != '>' && ch != '=') break;  // matched delim other than '>', '='; i points to the delim;

        // must examine preceding character
        if (i - 1 < 0) {
          i = ERROR_INDEX; // position i - 1 precedes start of document;
          break;
        }
        char prev = text.charAt(i - 1);
        int lineEnd = _getLineEndPos(i);
        int firstNonWS = _getFirstNonWSCharPos(i + 1);  // Start at right edge of ch
//          System.err.println("*****Found '" + ch + "' at pos " + i);         
//          System.err.println("    lineEnd = " + lineEnd + " firstNonWSPos = " + firstNonWS + " firstNonWSChar = '" + 
//                             text.charAt(firstNonWS) + "'  prev = '" + prev + "'");
        if (ch == '>') {
          if (prev == '=' && firstNonWS != ERROR_INDEX && lineEnd < firstNonWS)
              break;  // matched line-ending "=>";  i points to '>' within "=>"
          i--;  // decrement i and continue;
        }
        else { /* ch == '=' */
          // Confirm that '=' appears as symbol at end of line

          if ((prev == ' ' || Arrays.binarySearch(ILLEGAL_PREFIX_CHARS, prev) == ERROR_INDEX) && (lineEnd < firstNonWS)) 
            break;  // matched line-ending '=' 
          i--;  // decrement i and continue;
        }
      }
        
      // ch is not a matching delim; examine ch as possible end of brace phrase to skip
      else if (skipBracePhrases && match(ch, CLOSING_BRACES)) {  // note that delims have already been matched
//        System.err.println("In findPrevDelimiter, found skipPhrase ending in'" + ch + "' at pos " + i);
        setCurrentLocation(i + 1); // move cursor immediately to right of ch (a brace); i = _currentLocation - 1
//            Utilities.show("_currentLocation = " + _currentLocation);
        int dist = balanceBackward();
        if (dist < 0) { 
          i = ERROR_INDEX;
          break;            // braces do not balance; return error
        }
        assert dist > 1;  // minimum length for dist is 2 corresponding to "()"
//        Utilities.show("text = '" + text.substring(i + 1 - dist, dist) + "' dist = " + dist + " matching bracket is '" 
//                         + text.charAt(i) + "' at pos " + i);
        i = i - dist;  // decrement i to skip over balanced brace text and continue with PRECEDING char
//        System.err.println("Skipped phrase = '" + _getText(i+1,dist) + "'");
      }
      else i--;  // decrement i and continue
    }  // end of while loop
    
    /* Invariant: (i >= 0) => text[i] is a delimiter (including case where it is '>' with preceding '=') & 
     *                        text[i+1:pos-1] does not contain a delimiter
     *            (i == ERROR_INDEX) => text[0:pos-1] contains no valid (unshadowed) delimiters or 
     *                        (exists 0 <=i < j < pos such that text[i:j] is unbalanced brace text &&
     *                         text[j+1:pos-1] does not contain a valid delimiter.                         */
    
    setCurrentLocation(origPos);    // Restore the state of the reduced model; 
  
    _storeInCache(key, i, pos - 1);  // update cache; only changes in text[0:pos-1] affect the result
//    System.err.println("***** findPrevDelimiter returning offset " + i + "; ch = " + ((i >= 0) ? getText(i,1) : "ERROR_INDEX"));
    // Return position of matching delim or ERROR_INDEX (-1)
    return i;  
  }
  
  // Assumes delims in ascending order
  private static boolean match(char c, char[] delims) { return Arrays.binarySearch(delims, c) >= 0; }
  
  /** This function finds nearest occurrence of char c in the same statement (?) as pos that precedes pos assuming that there is
    * no intervening brace or semicolon. (No longer used?)
    */
  public boolean findCharInStmtBeforePos(char findChar, int pos) {
    
    System.err.println("*** findCharInStmtBeforePos(" + findChar + "," + pos + ")");
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    if (pos == ERROR_INDEX) {
      String msg = 
        "Internal error: argument endChar to QuestionExistsCharInStmt must be a char that exists on the current line.";
      throw new UnexpectedException(new IllegalArgumentException(msg));
    }
    
    final int origPos = _currentLocation;
    
    char[] findCharDelims = {findChar, ';', '{', '}'};

    // Find the position of the preceding occurrence of findChar (looking in paren phrases as well)  
    try {
      int prevFindCharPos = findPrevDelimiter(pos, findCharDelims, false);
      int prevImplicitSemicolonPos = (findChar == ';') ? _findPrevImplicitSemicolonPos(pos) : ERROR_INDEX;
      
      int prevCharPos = Math.max(prevFindCharPos, prevImplicitSemicolonPos);
      
      System.err.println("  prevFindCharPos = " + prevFindCharPos + " prevCharPos = " + prevCharPos);
      
      if (prevCharPos == ERROR_INDEX) return false; // no such char
      
      // Determine if prevFindCharPos is findChar or the end of statement delimiter
      String foundString = (prevCharPos == prevImplicitSemicolonPos) ? ";" : getText(prevCharPos, 1);
      char foundChar = foundString.charAt(0);
      
      System.err.println("   foundChar = " + foundChar);
      return (foundChar == findChar);
    }
    catch (Throwable t) { throw new UnexpectedException(t); }
    finally { setCurrentLocation(origPos); }
  }
  
  /** Finds the position of the first non-whitespace, non-comment character before pos.  Skips comments and all 
    * whitespace, including newlines.
    * @param pos Position to start from
    */
  public int getPrevNonWSCharPos(int pos) throws BadLocationException {
    return getPrevNonWSCharPos(pos, DEFAULT_WHITESPACE);
  }
  
  /** Finds the position of the first non-whitespace, non-comment character before pos.  Skips comments and all 
    * whitespace, including newlines.  This search does NOT skip over balanced parenthetical expressions!
    * @param pos Position to start from
    * @param whitespace chars considered as white space
    * @return position of first non-whitespace character before pos OR ERROR_INDEX (-1) if no such char, e.g., when
    *   pos < 0
    */
  public int getPrevNonWSCharPos(final int pos, final char[] whitespace) throws BadLocationException {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    if (pos < 0) return ERROR_INDEX;  // 
    
//    System.err.println("getPrevNonWSCharPos(" + pos + ") called");
    // Check cache
    final Query key = new Query.PrevCharPos(pos, whitespace);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    int i = pos - 1;
    String text;
    text = getText(0, pos); 
    
    final int origPos = _currentLocation;
    try {
      
      // Walk backward from specified position
      
      while (i >= 0) { 
        /* Invariant: 0 <= i <  pos, text[i+1:pos-1] contains invalid chars */
        
        if (match(text.charAt(i), whitespace)) {
          // ith char is whitespace
          i--;
          continue;
        }
        
        // Found a non-whitespace char;  move reduced model to location i and check for shadowing
        setCurrentLocation(i);
        if ((_reduced.getStateAtCurrent().equals(INSIDE_LINE_COMMENT)) ||
            (_reduced.getStateAtCurrent().equals(INSIDE_BLOCK_COMMENT))) {
          i--;
          continue;
        }
        if (i > 0 && _isStartOfComment(text, i - 1)) { /* char i is second character in opening comment marker */  
          // Move i past the first comment character and continue searching
          i = i - 2;
          continue;
        }
        
        // Found valid previous character
        break;
      }
      /* Exit invariant same as for loop except that i == ERROR_INDEX | i == pos of last valid char */
    }  
    finally { setCurrentLocation(origPos); }

    if (i < 0) i = ERROR_INDEX;
    _storeInCache(key, i, pos - 1);
    return i;
  }
    
  /** Checks the query cache for a stored value.  Returns the value if it has been cached, or null 
    * otherwise. Calling convention for keys: methodName:arg1:arg2.
    * @param key Name of the method and arguments
    */
  protected Object _checkCache(final Query key) {
    if (_queryCache == null) return null;
    return _queryCache.get(key); 
  }
  
  /** Stores the given result in the helper method cache. Query classes define equality structurally.
    * @param query  A canonical description of the query
    * @param answer  The answer returned for the query
    * @param offset  The offset bounding the right edge of the text on which the query depends; if (0:offset) in
    *                the document is unchanged, the query should return the same answer.
    */
  protected void _storeInCache(final Query query, final Object answer, final int offset) {
    if (_queryCache == null) return;
    _queryCache.put(query, answer);
    _addToOffsetsToQueries(query, offset);
  }
  
  /** Clears the memozing cache of queries with offset >= than specified value.  Should be called every time the 
    * document is modified. 
    */
  protected void _clearCache(int offset) {
    if (_queryCache == null) return;
    
    if (offset <= 0) {
      _queryCache.clear();
      _offsetToQueries.clear();
      return;
    }
    // The Integer[] copy of the key set is required to avoid ConcurrentModifiationExceptions.  Ugh!
    Integer[] deadOffsets = _offsetToQueries.tailMap(offset).keySet().toArray(new Integer[0]);
    for (int i: deadOffsets) {
      for (Query query: _offsetToQueries.get(i)) _queryCache.remove(query);  // remove query entry from cache
      _offsetToQueries.remove(i);   // remove query bucket for i from offsetToQueries table
    }
  }
  
  /** Add <query,offset> pair to _offsetToQueries map. Assumes lock on _queryCache is already held. */
  private void _addToOffsetsToQueries(final Query query, final int offset) {
    List<Query> selectedQueries = _offsetToQueries.get(offset);
    if (selectedQueries == null) {
      selectedQueries = new LinkedList<Query>();
      _offsetToQueries.put(offset, selectedQueries);
    }
    selectedQueries.add(query);
  }
  
  /** Default indentation - uses OTHER flag and no progress indicator.  NOTE: documents are only updated in event
    * thread.  This method only runs in event thread.
    * @param selStart the offset of the initial character of the region to indent
    * @param selEnd the offset of the last character of the region to indent
    */
  public void indentLines(int selStart, int selEnd) {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    try { indentLines(selStart, selEnd, Indenter.IndentReason.OTHER, null); }
    catch (OperationCanceledException oce) {
      // Indenting without a ProgressMonitor should never be cancelled!
      throw new UnexpectedException(oce);
    }
  }
  
  /** Parameterized indentation for special-case handling.  If selStart == selEnd, then the line containing the
    * currentLocation is indented.  The values of selStart and selEnd are ignored!  Perturbs _currentLocation.
    * 
    * @param selStart the offset of the initial character of the region to indent
    * @param selEnd the offset of the last character of the region to indent
    * @param reason a flag from {@link Indenter} to indicate the reason for the indent
    *        (indent logic may vary slightly based on the trigger action)
    * @param pm used to display progress, null if no reporting is desired
    */
  public void indentLines(int selStart, int selEnd, Indenter.IndentReason reason, ProgressMonitor pm)
    throws OperationCanceledException {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
//    Utilities.show("AbstractDJDocument.indentLines(" + selStart + ", " + selEnd + ", " + reason + ")");
    // Begins a compound edit.
    // int key = startCompoundEdit(); // commented out in connection with the FrenchKeyBoard Fix
    
    try {
      if (selStart == selEnd) {  // single line to indent
//          Utilities.showDebug("selStart = " + selStart + " currentLocation = " + _currentLocation);
        Position oldPosition = createUnwrappedPosition(_currentLocation);
        int lineStart = _getLineStartPos(selStart);
        if (lineStart <  0) lineStart = 0;  // selStart on first line
        setCurrentLocation(lineStart);
        // Indent, updating current location if necessary.
//          Utilities.showDebug("Indenting line at offset " + selStart);
        if (_indentLine(reason)) {
          setCurrentLocation(oldPosition.getOffset()); // moves currentLocation back to original offset on line
          if (onlySpacesBeforeCurrent()) move(getWhiteSpace());  // passes any additional spaces before firstNonWS
        }
      }
      else indentBlock(selStart, selEnd, reason, pm);
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    // Ends the compound edit.
    //endCompoundEdit(key);   //Changed to endLastCompoundEdit in connection with the FrenchKeyBoard Fix
    endLastCompoundEdit();
  }
  
  /** Indents the lines between and including the lines containing points start and end.  Only runs in event thread.
    * Perturbs _currentLocation.
    * @param start Position in document to start indenting from
    * @param end Position in document to end indenting at
    * @param reason a flag from {@link Indenter} to indicate the reason for the indent
    *        (indent logic may vary slightly based on the trigger action)
    * @param pm used to display progress, null if no reporting is desired
    */
  private void indentBlock(final int start, final int end, Indenter.IndentReason reason, ProgressMonitor pm)
    throws OperationCanceledException, BadLocationException {
    
//    Utilities.show("indentBlock called");
    // Set up the query cache;
    _queryCache = new HashMap<Query, Object>(INIT_CACHE_SIZE);
    _offsetToQueries = new TreeMap<Integer, List<Query>>();
    
    // Keep marker at the end. This Position will be the correct endpoint no matter how we change 
    // the doc doing the indentLine calls.
    final Position endPos = this.createUnwrappedPosition(end);
    // Iterate, line by line, until we get to/past the end
    int walker = start;
//    _indentInProgress = true;
    while (walker < endPos.getOffset()) {
      setCurrentLocation(walker);
      // Keep pointer to walker position that will stay current regardless of how indentLine changes things
      Position walkerPos = this.createUnwrappedPosition(walker);
      // Indent current line
      // We ignore current location info from each line, because it probably doesn't make sense in a block context.
      _indentLine(reason);  // this operation is atomic
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
//      _indentInProgress = false;
    }
    
    // disable the query cache
    _queryCache = null;
    _offsetToQueries = null;
  }
  
  /** Indents a line using the Indenter.  Public ONLY for testing purposes. */
  public boolean _indentLine(Indenter.IndentReason reason) { return getIndenter().indent(this, reason); }
  
  /** Returns the "intelligent" beginning of line.  If currPos is to the right of the first 
    * non-whitespace character, the position of the first non-whitespace character is returned.  
    * If currPos is at or to the left of the first non-whitespace character, the beginning of
    * the line is returned.
    * @param currPos A position on the current line
    */
  public int getIntelligentBeginLinePos(int currPos) throws BadLocationException {
    /* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    String prefix;
    int firstChar;
    firstChar = _getLineStartPos(currPos);
    prefix = getText(firstChar, currPos-firstChar);
    
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
  
  /** Returns the length of the whitespace prefix of the line containing pos.  Hence, it assumes that it is already
    * properly indented.  If no nonWS char appears on the specified line, returns 0. */
  public int _getIndentOfLine(int pos) {
//    System.err.println("_getIndentOfLine(" + pos + ") called");
//    System.err.println("char at pos == newline? " + (_getText(pos, 1).equals("\n")));
//    System.err.println("line for _getIndentOfLine = '" + _getCurrentLine(pos) + "'");
    int lineStart = _getLineStartPos(pos);
//    System.err.println("lineStart = " + lineStart);
    if (lineStart == ERROR_INDEX) return 0;
    int lineEnd = _getLineEndPos(pos);
//    System.err.println("lineEnd = " + lineEnd);
    try {
      int firstNonWSCharPos = getFirstNonWSCharPos(lineStart, true);  // Comment text is recognized
      if (firstNonWSCharPos > lineEnd || firstNonWSCharPos == ERROR_INDEX) return 0;
//      System.err.println("first Non WS char is '" + _getText(firstNonWSCharPos, 1));
      return firstNonWSCharPos - lineStart;
    }
    catch(BadLocationException ble) { return ERROR_INDEX; }
  }
   
  /* Searching backward from the end of the line containg pos, find the beginning of the statement containing pos. 
   * This seach skips over any brace phrase that ends on the line of pos that spans lines.  It assumes that:
   * (i)   pos >= 0
   * (ii)  the document text up through the end of this line is non-empty, and 
   * (iii) this stmt begins on the first unspanned line.
   * It does not include any prelude text beyond what appears on the first unspanned line.
   */
  
  public int _findBeginningOfStmt(final int pos) {
    
    assert pos >= 0;
    
    int org = getCurrentLocation();
    int lineEnd = _getLineEndPos(pos);
    
//    System.err.println("***** _findBeginningOfStmt(" + pos + ") called; Line end is + " + lineEnd + "; Current line is '" + 
//                       _getCurrentLine(pos) + "'");
    /* Find the beginning of the stmt (without prelude) on this line.  Must skip over paren phrases and comments that
     * end on this line, including both embedded and spanning phrases.  The innermost char beginning a line and 
     * preceding all paren phrases ending on this line (or all paren phrases forming a line spanning chain that ends
     * on this line) is the beginning of our stmt. */
    try {
      // Find last valid char preceding lineEnd
      int revPos = getPrevNonWSCharPos(lineEnd);     // revised pos created by skipping over paren phrases that span stmt lines
      if (revPos == ERROR_INDEX) return 0;
      
      int revLineEnd = _getLineEndPos(revPos);       // end of the line containing revPos
      int revLineStart = _getLineStartPos(revPos);      // start of the line containing revPos
//      System.err.println("[FBOS] revPos = " + revPos + "; revLineEnd = " + revLineEnd + "; revLineStart = " + revLineStart);
      
      assert revLineEnd >=0 && revLineStart >= 0;
      
      /* Find pos of last closing brace on this line; all paren phrases must be examined. */
      int lastBracePos = findPrevDelimiter(revLineEnd, CLOSING_BRACES, /* skip paren phrases */ false);
      // lastBracePos may be ERROR_INDEX
//      System.err.println("[FBOS] lastBracePos = " + lastBracePos);
      
      // Skip all line spanning paren phrases chained from this line
      while (lastBracePos >= revLineStart) {   // closing brace found on line; lastBracePos >= 0
        // find matching open brace
        setCurrentLocation(lastBracePos + 1);  // always well-defined since there is brace at pos lastBracePos
        int dist = balanceBackward();
        if (dist < 0) // Unmatched closing brace on this line; text is mangled
          break;
        revPos = lastBracePos - dist; // pos of matching open brace
//        System.err.println("{FBOS} Skipping over paren phrase ending at pos " + lastBracePos + " to pos " + revPos);
        if (revPos < revLineStart) {
          revLineStart = _getLineStartPos(revPos);
          revLineEnd = _getLineEndPos(revLineStart);
        }
        lastBracePos = findPrevDelimiter(revPos, CLOSING_BRACES, /* skip paren phrases */ false);
//        System.err.println("[FBOS] new lastBracePos = " + lastBracePos);
      }
      int result = _getFirstNonWSCharPos(revLineStart);
//      System.err.println("***** FBOS returning " + result);
      return result;
    } 
    catch(BadLocationException e) { /* either getPrevNonWSCharPos(pos) or findPrevDelimter(...) failed; fall through */ }
    finally { setCurrentLocation(org); }
    // punt by returning the indent of line containing pos
    int result = _getFirstNonWSCharPos(lineEnd);
//    System.err.println("***** FBOS returning " + result);
    return result;
  }
  
  /** Returns the number of blanks in the indent prefix for the start of the statement identified by pos.  Uses a 
    * default set of delimiters. (';', '{', '}') and a default set of whitespace characters (' ', '\t', n', ',')
    * Assumes beginning of statement is already propertly indented!
    * @param pos Cursor position
    */
  public int _getIndentOfStmt(int pos) {
    return _getIndentOfStmt(pos, DEFAULT_DELIMS, DEFAULT_WHITESPACE_WITH_COMMA, true);
  }
  
  /** Returns the number of blanks in the indent prefix for the start of the RESTRICTED statement ('=' preludes are excluded)
    * identified by pos.  Uses a default set of delimiters, {';', '{', '}'} and a default set of whitespace characters 
    * {' ', '\t', n', ','}.  Assumes beginning of statement is already propertly indented!
    * @param pos Cursor position
    */
  public int _getIndentOfRestrictedStmt(int pos) {
    return _getIndentOfStmt(pos, DEFAULT_DELIMS, DEFAULT_WHITESPACE_WITH_COMMA, false);
  }
  /** Returns the number of blanks in the indent prefix of the start of the statement identified by pos.  Uses a 
    * default set of whitespace characters: {' ', '\t', '\n', ','}.  Assumes beginning of statement is already propertly
    * indented!  '{' is a statement break but '=' is not.
    * @param pos Cursor position
    * TODO: why is whitespace passed as an argument.  Isn't the default OK?
    */
  public int _getIndentOfStmt(int pos, char[] delims) {
    return _getIndentOfStmt(pos, delims, DEFAULT_WHITESPACE_WITH_COMMA, true);
  }
 
  /** Returns the number of spaces in the indent prefix of the start of the statement identified by pos) assuming
    * that the leading line of this statement is already properly indented.  Assumes pos >= 0.  In Scala, the hard 
    * problem is finding the beginning of a statement since semicolons are generally omitted.  The current statement 
    * includes all statement prefixes connected by '=' (like 'def foo = ').  If current line is empty, use end of 
    * preceding line as pos.
    * @param pos  the current reference position which must identify a non WS char.
    * @param delims  delimiter characters denoting possible ends of preceding statement.
    * @param whitespace  characters to skip when looking for beginning of next statement; must include ' ' and '\n'
    */
  public int _getIndentOfStmt(final int pos, final char[] delims, final char[] whitespace)  {
    return _getIndentOfStmt(pos, delims, whitespace, true);
  }
  
  /** Same method as above except inlcudeEqualsPrelude flag has been added so searching back for such preludes can be
    * suppressed.  The default is for this flag to be true. */
  public int _getIndentOfStmt(final int pos, final char[] delims, final char[] whitespace, boolean includeEqualsPrelude)  {
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    assert pos >= 0;
    
//    System.err.println("***** _getIndentOfStmt(" + pos + ") called. Char at this pos = " + _getText(pos, 1).charAt(0));
//    System.err.println("  Corresponding line = '" + _getCurrentLine(pos) + "'");
    
    //  Check cache
    int lineEnd = _getLineEndPos(pos);  // returns 0 for empty document
    if (lineEnd <= 1) return 0;         // lineEnd = 1 iff first line is empty followed newline; otherwise lineEnd > 1
    
    final Query key = new Query.IndentOfStmt(lineEnd, delims, whitespace);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) {
//      System.err.println("  [GIOS] Returning cached value " + cached);
      return cached;
    }
    
    /* Searching backwards from lineEnd, skip over any brace phrase ending on this line that spans multiple lines, errs if
     * this line is a continuation and no braces span this line! */
    int stmtStart = _findBeginningOfStmt(lineEnd);
//    System.err.println("[GIOS] lineEnd = " + lineEnd + " stmtStart = " + stmtStart + " from calling FBOS again");
    
    int indentOfStmt; // indent of plausible statement start (excluding possible prelude)
    
    if (stmtStart == ERROR_INDEX) {  // No beginning found; perhaps no valid text precedes pos
      try {
        int newPos = getPrevNonWSCharPos(lineEnd);
        if (newPos == ERROR_INDEX) { // Document preceding lineEnd contains no NonWS chars
//          System.err.println("***** [GIOS] returning 0 because no NonWS chars precede end of specified line");
          return 0;
        }
        stmtStart = _findBeginningOfStmt(newPos);
//        System.err.println("[GIOS] called FBOS yet again because stmtStart == ERROR_INDEX; newPos = " + newPos +
//                           "; final stmtStart = " + stmtStart);
        indentOfStmt = stmtStart - _getLineStartPos(stmtStart);
        
      }
      catch(BadLocationException e) { return 0; } // document preceding pos is empty
    }
    else indentOfStmt = stmtStart - _getLineStartPos(stmtStart); 
    
//    System.err.println("[GIOS] plausible first char pos in stmt at pos " + pos + " is " + stmtStart);
    
    /* Find the EnclosingBraceOrPrevSemicolon of this stmt.  The braces setincludes '=' and "=>". */
    try {
      int boundaryPos = findEnclosingBraceOrPrevSemicolon(stmtStart);
      if (boundaryPos == ERROR_INDEX) { // find indent of firstNonWS char in doc
//        System.err.println("***** GIOS returning " + indentOfStmt);
        return indentOfStmt;
      }
      
      assert boundaryPos >= 0;
        
      char boundaryChar = _getText(boundaryPos, 1). charAt(0);   // Note: if brace is "=>", returned pos points to '>'.
      
      /* Perform some analysis depending on the boundary char preceding the previous line:
       * (i)   if char == newline (semicolon or implicit semicolon immediately precedes it), return indent of next Non WS Char
       * (ii)  if char == '=' then
       *       (a) if includeEqualsPrelude == true, find the prev semicolon (returning pos of a NEWLINE) and then the pos
       *           the firstNonWS char following the newline   (The find prev semicolon can be done with the findEBOPS 
       *           method because the prelude of '=' cannot contain an enclosing brace of any form in a legal program.)
       *        (b) otherwise return indent of next Non WS char
       * (iii) if char = '{' or '(' or '>', find the indent of the firstNonWS char following the brace.
       *     
       */
//      System.err.println("[GIOS] boundaryPos = " + boundaryPos + ", boundaryChar = '" + boundaryChar + "'");
      if (boundaryChar == '=' && includeEqualsPrelude) return _getIndentOfStmt(boundaryPos - 1);  // find indent of stmt prelude
      else { 
        assert boundaryChar == newline || boundaryChar == '(' || boundaryChar == '{' || boundaryChar == '>' || 
          (boundaryChar == '=' && ! includeEqualsPrelude);                                                                                                        ;
        int endBoundaryLine = _getLineEndPos(boundaryPos);
        int nextNonWSCharPos = _getFirstNonWSCharPos(endBoundaryLine + 1);
        int diff = nextNonWSCharPos - _getLineStartPos(nextNonWSCharPos);
//        System.err.println("[GIOS] first Non WS char pos of this stmt is " + nextNonWSCharPos + " indent = " + diff);
//        System.err.println("***** _getIndentOfStmt returning " + diff);
        return nextNonWSCharPos - _getLineStartPos(nextNonWSCharPos);
      }
    }
    catch(BadLocationException e) { return indentOfStmt; }
  }
  
  /** Finds the position of the first explicit/implicit semicolon preceding the statement enclosing pos skipping over 
    * balanced braces/parens and comment text.  Returns ERROR_INDEX if no such feature is found.
    * @param pos Position to start from
    * @return position of first implicit semicolon (end of a line) OR ERROR_INDEX (-1) if no such char
    */
  public int _findPrevImplicitSemicolonPos(int pos) {
    
//    System.err.println("***** _findPrevImplicitSemicolon(" + pos + ")");
//    System.err.println("      Corresponding line = '" + _getCurrentLine(pos) + "'");
    
    /* Searching backward, look at each line end that is not enclosed in a balanced phrase, and find the  
     * first line that does NOT satisfy one of the following:
     * (i)   ends in a closing paren that is not the end of a test clause for an if/for/while; or
     * (ii)  ends in '.' that is a separate token (not the end of a Double token), or
     * (iii) ends in a character other than a designated terminating char (like '+', '*', ...), or
     * (iv)  is empty, or
     * (v)   starts with "if" and the next ??
     * Then classify this boundary as ending in a semicolon and return the index of the newline character marking 
     * this boundary.  If no such boundary is found, return ERROR_INDEX. */
    
    assert pos >= 0;
    
    // Check cache
    final Query key = new Query.PrevImplicitSemicolon(pos);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int origPos = _currentLocation;
    
    int lineStart;
    int lastNonWSCharPos;
    int result = ERROR_INDEX;  // cache cell for method result; copied to Query cache on exit
    
    try {  // finally clause copies result cache cell to Query cache
      
      // Find closest preceding newline that is not inside a brace phrase and does not terminate an empty line
      do {
        pos = findPrevDelimiter(pos, NEWLINE);
//        System.err.println("[FPIS] In loop, candidate line for prev semicolon = '" + _getCurrentLine(pos) + "'");
        if (pos < 0) return result;  // return ERROR_INDEX
        
        assert getText(pos, 1).charAt(0) == newline;
        
        lineStart = _getLineStartPos(pos);
        
        // Find last unshadowed (by a comment) nonWS char in text preceding endPrevLinePos
        lastNonWSCharPos = getPrevNonWSCharPos(pos);
//        System.err.println("[FPIS] candidate lineStart = " + lineStart + " lastNonWSCharPos = " + lastNonWSCharPos);
        if (lastNonWSCharPos == ERROR_INDEX) {
          return result;  //  return ERROR_INDEX
        }
      } while (lastNonWSCharPos < lineStart);
      
//      System.err.println("[FPIS]  lineStart = " + lineStart + " line = '" + _getCurrentLine(pos) + "'");
      
      // pos is end of previous non-empty line; lastNonWSCharPos is pos of nearest preceding unshadowed Non WS char 
      char lastNonWSChar = getText().charAt(lastNonWSCharPos);
//      System.err.println("[FPIS] last NonWS char is '" + lastNonWSChar + "' on line '" + 
//                         _getCurrentLine(lastNonWSCharPos) + "'");
      
      // Is lastNonWSChar a ')'?
      
      if (lastNonWSChar == ')') {
        if (! isTestIfForWhile(lastNonWSCharPos)) {
          result = pos;
          return result;
        }
        else {
          setCurrentLocation(lastNonWSCharPos + 1);  // immediately to right of paren
          int dist = balanceBackward();
          int newPos = _getLineStartPos(lastNonWSCharPos + 1 - dist);  // resume search from left of matching paren
//          System.err.println("[FPIS] Skipping back past ForIfWhile prefix in searching for implicit semicolon");
          result = _findPrevImplicitSemicolonPos(newPos);
          return result;
        }
      } else {
        // Is lastNonWSChar a possible terminating char (preceding an implicit semicolon) or a semicolon
        int chIndex = Arrays.binarySearch(NOT_TERMINATING_CHARS, lastNonWSChar);
        if (chIndex < 0) { // ch can end a statement/expression (may even be an explicit semicolon)
          result = pos;
          return result;
        }
//        System.err.println("[FPIS] lastNonWSChar is a potential non-terminating char"); 
        if (chIndex == DOT_INDEX) { // lastNonWSChar is '.'; must exclude numeric constants
          // The ordered char array DOUBLE_DELIMS was defined heuristically; it may need perturbing
//          System.err.println("[FPIS] lastNonWSChar at pos " + lastNonWSCharPos + " is '.'");
          int numericDelimPos = findPrevDelimiter(lastNonWSCharPos, DOUBLE_DELIMS);
          // numericDelimPos may be -1
          try {
            String numText = getText(numericDelimPos + 1, lastNonWSCharPos + 1 - numericDelimPos);
//            System.err.println("[FPIS] numerical text = '" + numText + "'");
            double numVal = Double.parseDouble(numText);
          }
          catch(NumberFormatException nfe) { 
            result = _findPrevImplicitSemicolonPos(lastNonWSCharPos);
//            System.err.println("[FPIS] '.' was not part of a number");
            return result;
          }
//          System.err.println("[FPIS] '.' was part of a number");
          result = pos;  // lastNonWSChar is plausible terminating char because it is final char in a Double
          return result;  
        }
        
        // find semicolon pos starting at the preceding line boundary
        result = _findPrevImplicitSemicolonPos(lastNonWSCharPos);
        return result;
      }
    }
    catch(BadLocationException bpe) { /* fall through */ }  // should never happen?
    finally { // On all returns except for a cache hit, clean up by executing
      _storeInCache(key, result, Math.max(0, pos - 1));  // Only changes to text[0: pos-1] affect the result
      setCurrentLocation(origPos);
//      System.err.println("[FPIS] returning " + result);
    }   
    return ERROR_INDEX;  // compiler flow analysis is weak; unreachable!
  }
  
  /* Old version */
//  /** Find the enclosing line Scala brace, '{', '(', or "=>" with no intervening "case". */
//  public int findEnclosingScalaBracePos(int pos) throws BadLocationException {
//    
////    System.err.println("findEnclosingScalaBracePos(" + pos + ") called");
//    
//    int origPos = _currentLocation;
//    int lineStart = _getLineStartPos(pos);
//    setCurrentLocation(lineStart);
//    try {
//      BraceInfo info = _getLineEnclosingBrace(); // Uses reduced model; more efficient that using findPrevDelim
//      int dist1 = info.distance();
//      
//      // Check preconditions
//      if (info.braceType().equals("") || dist1 < 0) {  // Should use interned Strings here
//        // Can't find brace, return ERROR_INDEX
////        System.err.println("Finding line enclosing brace failed");
//        return ERROR_INDEX;
//      }
//      
//      // Enclosing brace exists
//      int bracePos = lineStart - dist1;
//      
////      System.err.println("Tentative scala enclosing brace position is " + bracePos + "; brace is " + 
////                         _getText(bracePos, 1).charAt(0));
//      
//      /* Must look back for Scala pseudo-brace skipping over any intervening brace phrases! */
//      
//      int chPos = getPrevNonWSCharPos(lineStart - 1);
//      char ch = getText(chPos, 1).charAt(0);
//      
////      System.err.println("Looking back for brace phrase to skip. chPos = " + chPos + " ch = " + ch);
//      
//      // Skip intervening brace phrases 
//      
//     while (match(ch, CLOSING_BRACES)) {  // note that delims have already been matched
////            Utilities.show("closing bracket is '" + ch + "' at pos " + i);
////       System.err.println("Setting current loc to " + (chPos + 1));
//       setCurrentLocation(chPos + 1); // move cursor immediately to right of ch (a brace); chPos = _currentLocation - 1
////            Utilities.show("_currentLocation = " + _currentLocation);
//        int dist2 = balanceBackward();
////        System.err.println("Balance back dist = " + dist2);
//        if (dist2 < 0) { 
//          bracePos = ERROR_INDEX;
//          break;            // braces do not balance; return error
//        }
//        assert dist2 > 1;  // minimum length for dist is 2 corresponding to "()"
////        Utilities.show("text = '" + text.substring(i + 1 - dist, dist) + "' dist = " + dist + " matching bracket is '" 
////                         + text.charAt(i) + "' at pos " + i);
//        chPos -= (dist2 - 1);  // decrement chPos to skip over balanced brace text and continue
////        System.err.println("chPos moved to " + chPos + " ch = " + getText(chPos, 1).charAt(0));
//        chPos = getPrevNonWSCharPos(chPos + 1);  // include current char as possible result
//        ch = getText(chPos, 1).charAt(0);
////        System.err.println("For next loop iteration, chPos = " + chPos + " and ch = " + ch);
//      }
//      
////     System.err.println("After brace phrase skipping, chPos = " + chPos + " and ch = " + ch);
//     
//      /* In Scala, a pseudo-brace "=>" may shadow the enclosing brace, check for this case but a
//       * "case" keyword at line opening closes such a preceding pseudo-brace. */
//      if (! _getCurrentLine(lineStart).trim().startsWith("case")) {
//        try {
//          int pseudoBracePos = findPrevDelimiter(chPos, OPENING_BRACES);
//          if (pseudoBracePos > bracePos) bracePos = pseudoBracePos;
//        }
//        catch(BadLocationException ble) { throw new UnexpectedException(ble); } // should never happen
//      }
////      System.err.println("Scala brace is '" + _getText(bracePos, 1).charAt(0) + "' pos is " + bracePos);
//      return bracePos;
//    }
//    finally { setCurrentLocation(origPos); }
//  }
  
  /** Find the position of the enclosing Scala brace { '{', '(', or "=>" } enclosing pos. */
  public int findEnclosingScalaBracePos(int pos) throws BadLocationException {  
    int res = findPrevDelimiter(pos, OPENING_BRACES);
    if (res == ERROR_INDEX) return res;
//    System.err.println("***** EnclosingScalaBrace(" + pos + ") = " + res + " brace is '" + _getText(res,1).charAt(0) + "'");
//    System.err.println("  input line = '" + _getCurrentLine(pos) + "'");
//    System.err.println("  output line = '" + _getCurrentLine(res) + "'");
    return res;
  }
  
  /** Find the position of the Scala brace { '{', '(', or "=>" } enclosing the start of the line containing pos  */
  public int findLineEnclosingScalaBracePos(int pos) throws BadLocationException {  
    return findEnclosingScalaBracePos(_getLineStartPos(pos));
  }
  
  /** Find the position of the enclosing Scala brace { '{', '(', '[', "=", or "=>" } enclosing pos. */
  public int findEnclosingScalaBracePosWithEquals(int pos) throws BadLocationException {  
    int res = findPrevDelimiter(pos, OPENING_BRACES_WITH_EQUALS);
    if (res == ERROR_INDEX) return ERROR_INDEX;
//    System.err.println("***** EnclosingScalaBraceWithEquals(" + pos + ") = " + res + " brace is '" + _getText(res,1).charAt(0) + "'");
//    System.err.println("  input line = '" + _getCurrentLine(pos) + "'");
//    System.err.println("  output line = '" + _getCurrentLine(res) + "'");
    return res;
  }
  
  
  /** Find the position of the Scala brace { '{', '(', "=", or "=>" } enclosing the start of the line containing pos  */
  public int findLineEnclosingScalaBracePosWithEquals(int pos) throws BadLocationException {  
    return findEnclosingScalaBracePosWithEquals(_getLineStartPos(pos));
  }
  
  /** Finds position of the first explicit/implicit line-terminatating semicolon [comma] or enclosing Scala brace 
    * (including '=') preceding pos skipping over balanced braces/parens and comment text.  Returns ERROR_INDEX if 
    * no such feature is found.
    * @param pos Position to start from
    * @return position of first implicit semicolon (end of a line) OR ERROR_INDEX (-1) if no such char
    */
  public int findEnclosingBraceOrPrevSemicolon(final int pos) throws BadLocationException {  
    
    /* Searching backward, look at each line end that is not enclosed in a balanced phrase or block comment, and find 
     * the first line satisfying one of the following property:
     * (i)  contains an enclosing brace '{', '(', '=', or "=>" where the latter two are required to appear at line end (ignoring
     *      comments and whitespace), or 
     * (ii) does not qualify under (i) above and does not have any of the following properties (implying no semicolon)
     *      (a) ends in a closing paren that is the end of a test clause for an if/for/while;
     *      (b) ends in '.' that is a separate token (not the end of a Double token), or
     *      (c) ends in a character that cannot terminate a line (see NON_TERMINATING_CHARS)
     *      (d) ends with "extends" or precedes a line beginning with "extends"
     *      (d) is empty
     * Then classify this boundary as the brace found in (i) or the newline at the end of the line qualifying under (ii) [an
     * implicit semeicolon].
     * If no such boundary is found, return ERROR_INDEX. */
    
    assert pos >= 0;
    
//    System.err.println("***** findEnclosingBraceOrPrevSemicolon(" + pos + ") called");
    
    // Check cache
    final Query key = new Query.EnclosingBraceOrPrevSemicolon(pos);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int origPos = _currentLocation;
    
    int lineStart;
    int newPos = pos;          // current pos searching backward
    int lastNonWSCharPos;
    int result = ERROR_INDEX;  // cache cell for method result; copied to Query cache on exit
    
    try {  // finally clause copies result cache cell to Query cache
      
      // Find closest unmatched open brace or newline preceding newPos that does not terminate an empty line
      do {
        newPos = findPrevDelimiter(newPos, OPENING_DELIMS_AND_NEWLINE);
//        System.err.println("[FEBOPS] In loop, candidate line for prev brace/semicolon = '" + _getCurrentLine(pos) + "'");
        if (newPos < 0) {
//          System.err.println("[FEBOPS] returning " + result);
          return result;  // return ERROR_INDEX
        }
        lineStart = _getLineStartPos(newPos);
        
        // Find last unshadowed (by comments) nonWS char in text up to (and including) newPos
        lastNonWSCharPos = getPrevNonWSCharPos(newPos + 1);
//        System.err.println("{FEBOPS] For this line, lineStart = " + lineStart + " lastNonWSCharPos = " + lastNonWSCharPos);
        
        if (lastNonWSCharPos == ERROR_INDEX) {
          return result;  //  return ERROR_INDEX
        }
      } while (lastNonWSCharPos < lineStart);
      
//      System.err.println("[FEBOPS] final lineStart = " + lineStart + " line = '" + _getCurrentLine(pos) + "'");
      
      // newPos points to matching delim; lastNonWSCharPos points to nearest preceding (including ==) unshadowed NonWS char 
      char lastNonWSChar = getText(lastNonWSCharPos, 1).charAt(0);
//      System.err.println("[FEBOPS] last NonWS char is '" + lastNonWSChar + "' on line '" + 
//                         _getCurrentLine(lastNonWSCharPos) + "'");
      
      if (Arrays.binarySearch(OPENING_BRACES_WITH_EQUALS, lastNonWSChar) >= 0)  { // found a match among delims
        result = lastNonWSCharPos;
//        System.err.println("[FEBOPS] returning " + result);
        return result;
      }
      
      // prev delim is newline; putative result = newPos
      result = newPos;
      
      if (lastNonWSChar == ')') {
        if (! isTestIfForWhile(lastNonWSCharPos)) {
//          System.err.println("[FEBOPS] returning pos 0f return = " + result);
          return result;
        }
        else {
          setCurrentLocation(lastNonWSCharPos + 1);  // immediately to right of paren
          int dist = balanceBackward();
          int revPos = lastNonWSCharPos + 1 - dist;  // resume search from left of matching paren
//          System.err.println("[FEBOPS] Skipping back past ForIfWhile prefix in searching for implicit semicolon");
          result = findEnclosingBraceOrPrevSemicolon(revPos);
//          System.err.println("[FEBOPS] returning " + result);
          return result;
        }
      } else if (lastNonWSChar == '}' || lastNonWSChar == ';') {  // found an implicit/explicit semicolon at pos newPos
//        System.err.println("[FEBOPS] returning " + result);
        return result;
      } else { // lastNonWSChar is not a closing brace or a semicloon
        
        // Is lastNonWSChar a possible terminating char (preceding an implicit semicolon) or a semicolon
        int chIndex = Arrays.binarySearch(NOT_TERMINATING_CHARS, lastNonWSChar);
        if (chIndex < 0) {
          result = newPos;
          if (lastNonWSChar == 's') {
            int offset = lastNonWSCharPos - 7;
            if (offset >= 0 && getText(offset, 8).equals(" extends"))  { // not a line end
//              Utilities.show("Found ' extends' at position " + offset);
              result = findEnclosingBraceOrPrevSemicolon(offset); 
            }
          }
          //            System.err.println("[FEBOPS] returning " + result);
          return result;
        }
          
        if (chIndex == DOT_INDEX) { // prevNonWSChar is '.'; must exclude numeric constants
          // The ordered char array DOUBLE_DELIMS was defined heuristically; it may need perturbing
          int numericDelimPos = findPrevDelimiter(lastNonWSCharPos, DOUBLE_DELIMS);
          try {
            double numVal = Double.parseDouble(getText(numericDelimPos + 1, lastNonWSCharPos + 1));
          }
          catch(NumberFormatException nfe) { 
            result = findEnclosingBraceOrPrevSemicolon(lastNonWSCharPos);
//            System.err.println("[FEBOPS] returning " + result);
            return result;
          }
//          System.err.println("***** _findPrevImplicitSemicolonPos returning " + newPos);
          // lastNonWSChar is plausible terminating char because it is final char in a Double
//          System.err.println("[FEBOPS] returning " + result);
          return result;  
        }
        
        // reject lastNonWSChar as enclosing brace or implicit semicolon. Recur starting at the preceding line boundary
        result = _findPrevImplicitSemicolonPos(lastNonWSCharPos);
//        System.err.println("[FEBOPS] returning " + result);
        return result;
      }
    }
    catch(BadLocationException bpe) { }  // should never happen?
    finally {
      _storeInCache(key, result, Math.max(0, pos - 1));  // Only changes to text[0: pos-1] affect the result
      setCurrentLocation(origPos); 
    }
//    System.err.println("[FEBOPS] returning " + ERROR_INDEX);
    return ERROR_INDEX;
  }
  
  /** Assumes that getText().charAt(pos) == ')'.  Returns true iff the balanced paren text ending at pos is the test
    * expression for an if/for/while,  Embedded comment text is ignored. 
    * TO DO: add this query to the cache! */
  public boolean isTestIfForWhile(int pos) {
    final int origPos = _currentLocation;
    
//    System.err.println("isTestIfForWhile(" + pos + ") called");
//    System.err.println("Current line is '" + _getCurrentLine(pos) + "'");
    
    boolean result = false;
    int rightEdge = pos + 1;
    setCurrentLocation(rightEdge);
//    System.err.println("rightEdge + " + rightEdge + " preceding char is '" + _getText(rightEdge - 1, 1).charAt(0) + "'");
    int dist = balanceBackward();
    
    if (dist != ERROR_INDEX) {
    
      int newPos = rightEdge - dist;  // skip over matching opening paren
      
      assert newPos >= 0;
      
      try {
        int lastKeywordCharPos = getPrevNonWSCharPos(newPos);
        // TODO: ensure character preceding keyword text is a delimiter
        if (lastKeywordCharPos >= 0) 
          if (getText(lastKeywordCharPos - 4, 5).equals("while")) { 
          // found "while", but it may be part of "do { ... } while (...)" loop; check for preceding "do { ... }"
          int prevKeywordCharPos = findPrevDelimiter(lastKeywordCharPos - 4, ALPHANUMERIC);
//          System.err.println("[ITIFW] preceding keyword = '" + getText(prevKeywordCharPos - 2, 3) + "'");
          result = ! getText(prevKeywordCharPos - 2, 3).equals(" do");
          // Note: "do" must be preceded by a space if embedded in properly indented program text
          // TODO?  fix the corner case when "do" appears unembedded in some interpreted statement sequence
        }                                                        
        else
          result = getText(lastKeywordCharPos - 1, 2).equals("if") || getText(lastKeywordCharPos - 2, 3).equals("for");
        
//        System.err.println("[ITIFW] lastKeywordCharPos = " + lastKeywordCharPos + " keywordText = '" + 
//                           getText(lastKeywordCharPos - 5, 6) + "'"  + " result = " + result);
      }
      catch(BadLocationException ble) { /* fall through */ }
    }
    setCurrentLocation(origPos);
    return result;
  }
  
//  /** Gets the white space prefix preceding the first non-blank/tab character on the line identified by pos. 
//    * Assumes that line has nonWS character.
//    */
//  public String getWSPrefix(int pos) {
//  assert EventQueue.isDispatchThread();
//    try {
//        
//        // Get the start of this line
//        int lineStart = _getLineStartPos(pos);
//        // Get the position of the first non-ws character on this line
//        int firstNonWSPos = _getLineFirstCharPos(pos);
//        return StringOps.getBlankString(firstNonWSPos - lineStart);
//    }
//    catch(BadLocationException e) { throw new UnexpectedException(e); }
//  }
  
  /** Determines if the given character exists on the line (including newline char at end of line if it exists)
    * containing pos.  Does not search inside quotes or comments. Only runs in event thread.
    * <b>Does not work if character being searched for is a '/' or a '*'</b>.  
    * @param pos Cursor position
    * @param findChar Character to search for
    * @return true if this node's rule holds.
    */
  public int findCharOnLine(final int pos, final char findChar) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();  // violated in some unit tests
    
    // Check cache
    final Query key = new Query.CharOnLine(pos, findChar);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    final int origPos = _currentLocation;
    
    try {
      int lineStart = _getLineStartPos(pos);
      int lineEnd = _getLineEndPos(pos);
      /* increment lineEnd if newline pos points to newLine char */
      if (getLength() > lineEnd) ++lineEnd;
      String lineText = getText(lineStart, lineEnd - lineStart);
      int i = lineText.indexOf(findChar, 0);
      int matchIndex = i + lineStart;
      
      while (i != ERROR_INDEX) { // match found
        /* Invariant: reduced model points to original location (here), lineText[0:i-1] does not contain valid 
         *            findChar, lineText[i] == findChar which may or may not be valid. */
        
        // Move reduced model to location of ith char
        setCurrentLocation(matchIndex);  // move reduced model to location matchIndex
        
        // Check if matching char is in comment or quotes
        if (_reduced.getStateAtCurrent().equals(FREE)) break; // found matching char
        
        // matching character is not valid, try again
        i = lineText.indexOf(findChar, i+1);
      }
      setCurrentLocation(origPos);  // restore old position
      
      if (i == ERROR_INDEX) matchIndex = ERROR_INDEX;
      _storeInCache(key, matchIndex, Math.max(pos - 1, matchIndex));
      return matchIndex;
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    finally { setCurrentLocation(origPos); }
  }
  
  /** Returns the absolut position of the beginning of the current line using _currentLocation as pos. */
  public int _getLineStartPos() { return _getLineStartPos(_currentLocation); }
  
  /** Returns the absolute position of the beginning of the current line.  If pos  input is bad, returns ERROR_INDEX
    * Doesn't ignore comments.
    * @param pos Any position on the current line
    * @return position of the beginning of this line
    */
  public int _getLineStartPos(final int pos) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    if (pos < 0 || pos > getLength()) return ERROR_INDEX;
    // Check cache
    final Query key = new Query.LineStartPos(pos);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int dist;
    
    final int origPos = _currentLocation;
    setCurrentLocation(pos);
    dist = _reduced.getDistToStart(0);  // distance from pos to preceding newline character
    
    setCurrentLocation(origPos);
    
    int newPos = 0;
    if (dist >= 0)  newPos = pos - dist;  // offset immediately following preceding newline
    _storeInCache(key, newPos, pos - 1);
    return newPos;  // may equal 0
  }
  
  /** Returns the absolute position of the current line using _currentLocation as pos. */
  public int _getLineEndPos() { return _getLineEndPos(_currentLocation); }
    
  /** Returns the absolute position of the end of the current line.  (At beginning of next newline, or the end of the 
    * document.)
    * @param pos Any position on the current line
    * @return position of the end of this line
    */
  public int _getLineEndPos(final int pos) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    if (pos < 0 || pos > getLength()) return ERROR_INDEX;
    
    // Check cache
    final Query key = new Query.LineEndPos(pos);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null) return cached.intValue();
    
    int dist, newPos;
    
    final int origPos = _currentLocation;
    setCurrentLocation(pos);
    dist = _reduced.getDistToNextNewline();
    setCurrentLocation(origPos);
    
    newPos = pos + dist;
    assert newPos == getLength() || _getText(newPos, 1).charAt(0) == newline;
    _storeInCache(key, newPos, newPos);
    return newPos;
  }
  
  /** Returns the absolute position of the first non-blank/tab character on the current line including comment text or
    * the end of the line if no non-blank/tab character is found.  Does not skip comments.
    * TODO: 
    * (i)  get rid of tab character references in AbstractDJDocument and related files and prevent insertion of tabs
    * (ii) implement this method using getFirstNonWSCharPos.
    * @param pos position on the line
    * @return position of first non-blank/tab character on this line, or the end of the line if no non-blank/tab 
    *         character is found.
    */
  public int _getLineFirstCharPos(final int pos) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    // Check cache
    final Query key = new Query.LineFirstCharPos(pos);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    final int startLinePos = _getLineStartPos(pos);
    final int endLinePos = _getLineEndPos(pos);
    int nonWSPos = endLinePos;
    
    // Get all text on this line and search for first nonWS char
    String text = _getText(startLinePos, endLinePos - startLinePos);
    int walker = 0;
    while (walker < text.length()) {
      if (text.charAt(walker) == ' ' || text.charAt(walker) == '\t') walker++;
      else {
        nonWSPos = startLinePos + walker;
        break;
      }
    }
    _storeInCache(key, nonWSPos, Math.max(pos - 1, nonWSPos));
    return nonWSPos;  // may equal lineEnd
  }
  
  /** Determines if line containing pos only contains whitespace; an empty line contains no white space. 
    * Only called in event thread. */
  public boolean _isLineEmpty(int pos) {
    
    int lineStart = _getLineStartPos(pos);
    int lineEnd = _getLineEndPos(pos);
    return _getLineFirstCharPos(pos) == lineEnd;
  }
  /** Finds the position of the first non-whitespace character after pos, skipping comments and whil
    * @param pos Position to start from
    * @return position of first non-whitespace character after pos, or ERROR_INDEX (-1) if end of document is reached
    */
  public int _getFirstNonWSCharPos(int pos) {
    try { return getFirstNonWSCharPos(pos); }
    catch(BadLocationException ble) { return ERROR_INDEX; }
  }
  
  /** Finds the position of the first non-whitespace character after pos. NB: Skips comments and all whitespace, 
    * including newlines.
    * @param pos Position to start from
    * @return position of first non-whitespace character after pos, or ERROR_INDEX (-1) if end of document is reached
    */
  public int getFirstNonWSCharPos(int pos) throws BadLocationException {
    return getFirstNonWSCharPos(pos, DEFAULT_WHITESPACE, false);
  }
  
  /** Similar to the single-argument version, but allows including comments.
    * @param pos Position to start from
    * @param acceptComments if true, find non-whitespace chars in comments
    * @return position of first non-whitespace character after pos,
    * or ERROR_INDEX (-1) if end of document is reached
    */
  public int getFirstNonWSCharPos(int pos, boolean acceptComments) throws BadLocationException {
    return getFirstNonWSCharPos(pos, DEFAULT_WHITESPACE, acceptComments);
  }
  
  /** Finds the position of the first non-whitespace character after pos ON THE CURRENT LINE.  If acceptComments 
    * is false, skips chars in comments.  Cannot actually throw a BadLocationException.  Returns ERROR_INDEX if 
    * no such char exists.
    * TODO: change to _getFirstNonWSCharPos by swallowing impossible BadLocationException
    * @param pos Position to start from
    * @param whitespace array of whitespace chars to ignore
    * @param acceptComments if true, find non-whitespace chars in comments
    * @return position of first non-whitespace character after pos, or ERROR_INDEX (-1) if end of document is reached
    */
  public int getFirstNonWSCharPos(final int pos, final char[] whitespace, final boolean acceptComments) throws 
      BadLocationException {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
//    System.err.println("***** getFirstNonWSChaPos called at pos " + pos + "; acceptComments = " + acceptComments);
    
    // Check cache
    final Query key = new Query.FirstNonWSCharPos(pos, whitespace, acceptComments);
    final Integer cached = (Integer) _checkCache(key);
    if (cached != null)  return cached.intValue();
    
    final int docLen = getLength();
    final int origPos = _currentLocation;
    
    // cursor is set to a specific position (i in the loop below) before reduced model is accesed
//    if (origPos != pos) setCurrentLocation(pos);  // Move reduced model to location pos
    try {
      int i = pos;
      int result = ERROR_INDEX; // default value if no valid char is found
      // Walk forward from specificed position until Non WS char is found or end of document is reached
      // Invariant: i <= endPos && i >= pos &&  no char in document(pos:i) 
      while (i < docLen) {
        
//        System.err.println("Checking character '" + _getText(i, 1).charAt(0) + "'");
        // Check if character in position i is whitespace
        if (! acceptComments && inComment(i)) { 
          i++; 
          continue; 
        }
        else if (match(_getText(i, 1).charAt(0), whitespace)) {
//          System.err.println("'" + _getText(i,1).charAt(0) + "' is whitespace");
          i++;
          continue;
        }
        // Found a non-whitespace character that meets comment filter criterion (flagged by acceptComments)
        // Move reduced model to this location for subsequent processing
        setCurrentLocation(i);  // reduced model points to location i
//        System.err.println("In [GFNWSCP], found Non WS char at pos " + i + "; char is '" + _getText(i, 1).charAt(0) + "'");
        
        // TODO: is this version faster than actual code?
//        // Check if non-ws char is within comment and if we want to ignore them.
//        if (! acceptComments &&
//            ((_reduced.getStateAtCurrent().equals(INSIDE_LINE_COMMENT)) ||
//             (_reduced.getStateAtCurrent().equals(INSIDE_BLOCK_COMMENT)))) {
//          i++;  // TODO: increment i to skip over entire comment
//          continue;
//        }
//        
//        // Check if non-ws char is part of comment opening bracket and if we want to ignore them
//        if (! acceptComments && _isStartOfComment(line, i-pos)) {
//          // ith char is first char in comment open market; skip past this marker and continue searching
//          i = i + 2;  // TODO: increment i to skip over entire comment
//          continue;
//        }
        
        if (acceptComments || ! isWeaklyShadowed(false)) { // found a valid char, cache this result and return it
          result = i;
          break;
        }
     
        // Since char is invalid, update i and continue
//        System.err.println("In [GFNWSCP], char at pos " + i + " is shadowed.");
        i++;  // TODO: increment i to skip over entire comment
      }
      
      assert i < docLen || result == ERROR_INDEX;
      
      // cache and return result
      _storeInCache(key, result, result + 1);  // Cached answer depends only on text(0:result]
//      System.err.println("***** getFirstNonWSCharPos returning " + result + "; char is '" + _getText(result, 1).charAt(0) + "'");
      return result;
    }
    catch(Exception e) {  // Must restore origPos if any form of exception occurs
      // No matching char found in document
      _storeInCache(key, ERROR_INDEX, Integer.MAX_VALUE);  // Any change to the document invalidates this result!
//          _setCurrentLocation(origPos);
      return ERROR_INDEX;
    }
    finally { setCurrentLocation(origPos); }  // restore _currentLocatio which is perturbed in loop
  }
  
//  public int getFirstNonWSCharPos(final int pos, final char[] whitespace, final boolean acceptComments) throws 
//    BadLocationException {
//    
//    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    
//    // Check cache
//    final Query key = new Query.FirstNonWSCharPos(pos, whitespace, acceptComments);
//    final Integer cached = (Integer) _checkCache(key);
//    if (cached != null)  return cached.intValue();
//    
//    final int docLen = getLength();
//    final int origPos = _currentLocation;
//    final int endPos = _getLineEndPos(pos);
//    
//    String line = _getText(pos, endPos - pos);   // Get text from pos to end of line; never returns ERROR_INDEX
//    setCurrentLocation(pos);  // Move reduced model to location pos
//    try {
//      int i = pos;
//      int reducedPos = pos;
//      // Walk forward from specificed position
//      // Invariant: cursor == i == reducedPos && text(pos: i) && text(pos:i) does not
//      //            contain any unshadowed delims
//      while (i < endPos) {
//        
//        // If character is whitespace, ignore it
//        if (match(line.charAt(i-pos), whitespace)) {
//          i++;
//          continue;
//        }
//        // Found a non whitespace character
//        // Move reduced model to walker's location for subsequent processing
//        setCurrentLocation(i);  // reduced model points to location i
//        reducedPos = i;
//        
//        // Check if non-ws char is within comment, ignore it
//        if (! acceptComments && isWeaklyShadowed(false)) {
//          i++;  // TODO: increment i to skip over entire comment
//          continue;
//        }
//        // Found unshadowed matching characgter; return its position
//        _storeInCache(key, reducedPos, reducedPos);  // Cached answer depends only on text(0:reducedPos]
////          _setCurrentLocation(origPos);
//        return reducedPos;
//      }
//      
//      // No matching char found on this line
//      if (endPos + 1 >= docLen) { // No matching char found in doc
//        _storeInCache(key, ERROR_INDEX, Integer.MAX_VALUE);  // Any change to the document invalidates this result!
////          _setCurrentLocation(origPos);
//        return ERROR_INDEX;
//      }
//    }
//    finally { setCurrentLocation(origPos); }  // restore _currentLocation
//    
//    // Search through remaining lines of document; recursion depth is bounded by number of blank lines following pos
//    return getFirstNonWSCharPos(endPos + 1, whitespace, acceptComments);
//  }

  /** Helper method for getFirstNonWSCharPos Determines whether the current character is the start of a comment: 
    * "/*" or "//"
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
  
  // Never used
//  /** Determines if _currentLocation is the start of a comment. */
//  private boolean _isStartOfComment(int pos) { return _isStartOfComment(getText(), pos); }
  
//  /** Helper method for getPrevNonWSCharPos. Determines whether the current character is the start of a comment
//    * encountered from the end: '/' or '*' preceded by a '/'.
//    * @return true if (pos-1,pos) == '/*' or '//'
//    */
//  protected static boolean _isOneCharPastStartOfComment(String text, int pos) {
//    char currChar = text.charAt(pos);
//    if (currChar == '/' || currChar == '*') {
//      try {
//        char beforeCurrChar = text.charAt(pos - 1);
//        if (beforeCurrChar == '/')  return true;
//      } catch (StringIndexOutOfBoundsException e) { /* do nothing */ }
//    }
//    return false;
//  }
  
  
  /** Returns true if the given position is inside a paren phrase.
    * @param pos the position we're looking at
    * @return true if pos is immediately inside parentheses
    */
  public boolean _inParenPhrase(final int pos) {
    
    /* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    // Check cache
    final Query key = new Query.PosInParenPhrase(pos);
    Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) return cached.booleanValue();
    
    boolean _inParenPhrase;
    
    final int origPos = _currentLocation;
    // assert pos == here if reduced already held before call
    setCurrentLocation(pos);
    _inParenPhrase = _inParenPhrase();
    setCurrentLocation(origPos);
    _storeInCache(key, _inParenPhrase, pos - 1);
    
    return _inParenPhrase;
  }
  
  /** Returns BraceInfo for innermost brace enclosing start of current line.  If there is not a line enclosing brace,
    * returns BraceInfo.NULL.
    * Only runs in event thread.  */
  public BraceInfo _getLineEnclosingBrace() {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    // Check cache
    final int lineStart = _getLineStartPos(_currentLocation);
//    System.err.println("_currentLocation = " + origPos + " lineStart = " + lineStart);
    if (lineStart < 0) return BraceInfo.NULL;
    final int keyPos = lineStart;
    final Query key = new Query.LineEnclosingBrace(keyPos);
    final BraceInfo cached = (BraceInfo) _checkCache(key);
    if (cached != null) return cached;
    
//    BraceInfo b = _reduced.getLineEnclosingBrace(lineStart);  // optimized version to be developed
    BraceInfo b = _reduced._getLineEnclosingBrace();
    
    // TODO: check for intervening pseudoBrace "=>"
    
    _storeInCache(key, b, keyPos - 1);
    return b;
  }
  
  /** Cached version of _reduced.getEnclosingBrace().  Only runs in event thread. */
  public BraceInfo _getEnclosingBrace() {
//    System.err.println("***** _getEnclosingBrace() called at pos " + _currentLocation);
//    System.err.println("  Corresponding line = '" + _getCurrentLine() + "'");
    int pos = _currentLocation;
    // Check cache
    final Query key = new Query.EnclosingBrace(pos);
    final BraceInfo cached = (BraceInfo) _checkCache(key);
    if (cached != null) return cached;
    BraceInfo b = _reduced._getEnclosingBrace();
    
    // TODO: check for intervening pseudoBrace "=>"
    _storeInCache(key, b, pos - 1);
    return b;
  }
  
  /** returns position of enclosing brace.  If no such brace exists, returns ERROR_INDEX. */
  public int _getEnclosingBracePos() {
    
    int pos = _currentLocation;
    BraceInfo b = _getEnclosingBrace();
    int dist = b.distance();
    if (dist < 0) return ERROR_INDEX;
    return pos - dist;
  }
  
  /** returns position of enclosing brace for the given pos.  If no such brace exists, returns ERROR_INDEX. */
  public int _getEnclosingBracePos(int pos) {
    int orig = _currentLocation;
    try {
      setCurrentLocation(pos);
      BraceInfo b = _getEnclosingBrace();
      int dist = b.distance();
      if (dist < 0) return ERROR_INDEX;
      return pos - dist;
    }
    finally { setCurrentLocation(orig); }
  }
    
  /** Returns true if the reduced model's current position is inside a paren phrase.  Only runs in the event thread.
    * @return true if pos is immediately inside parentheses
    */
  private boolean _inParenPhrase() {
    
    BraceInfo info = _reduced._getEnclosingBrace(); 
    return info.braceType().equals(BraceInfo.OPEN_PAREN);
//    return _getLineEnclosingBrace(_currentLocation).braceType().equals(IndentInfo.openParen);
  }
  
//  /** @return true if the start of the current line is inside a block comment. Assumes that reduced lock is already 
//    * held. Only called from event thread. */
//  public boolean posInBlockComment() {
//    int pos = _currentLocation;
//    final int lineStart = getLineStartPos(pos);
//    if (lineStart < POS_THRESHOLD) return posInBlockComment(lineStart);
//    return cachedPosInBlockComment(lineStart);
//  }
  
//  /** Returns true if given position is inside a block comment using cached information.  Only runs in event thread/
//    * @param pos a position at the beginning of a line.
//    * @return true if pos is immediately inside a block comment.
//    */
//  private boolean cachedPosInBlockComment(final int pos) {
//    
//    // Check cache
//    final Query key = new Query.PosInBlockComment(pos);
//    final Boolean cached = (Boolean) _checkCache(key);
//    if (cached != null) return cached.booleanValue();
//    
//    boolean result;
//    
//    final int startPrevLine = getLineStartPos(pos - 1);
//    final Query prevLineKey = new Query.PosInBlockComment(startPrevLine);
//    final Boolean cachedPrevLine = (Boolean) _checkCache(prevLineKey);
//    
//    if (cachedPrevLine != null) result = posInBlockComment(cachedPrevLine, startPrevLine, pos - startPrevLine); 
//    else result = posInBlockComment(pos);
//    
//    _storeInCache(key, result, pos - 1);
//    return result;
//  }    
  
  /** Determines if pos lies within a block comment using the reduced model (ignoring the cache). Only called from 
    * event thread.  The position of the first character in the opening bracket of a comment block is NOT inside
    * the block.  This code is complicated by the fact that only positions of chars following the opening block 
    * comment bracket in the block comment are shadowed (have state IN_BLOCK_COMMENT) and that the entire closing
    * bracket is shadowed (including the position following the bracket).   */
  public boolean _insideBlockComment(final int pos) {
    final int origPos = _currentLocation;  // save state of reduced model
    try {
      return isShadowed(pos);
    }
    catch(UnexpectedException e) { return false; }
  }
////    System.err.println("In _insideBlockComment, pos = " + pos + ", origPos = " + origPos);
//    try {
//      ReducedModelState state = getStateAtCurrent();
////      System.err.println("state for position " + pos + " is: " + state);
//      if (state.equals(INSIDE_BLOCK_COMMENT)) {
//          /* Check for position trailing end of block which has state INSIDE_BLOCK_COMMENT. */
//        setCurrentLocation(pos + 1);
//        return getStateAtCurrent().equals(INSIDE_BLOCK_COMMENT);
//      }
//      else return false;

//      /* Check to see if pos precedes either char in opening comment bracket */
//      if (pos >= getLength()) return false;  // pos cannot point to second char in opening comment bracket
//      char charAtPos = getText(pos,1).charAt(0);
//      if (charAtPos == '*') {
//        setCurrentLocation(pos + 1);   // pos must exist because of preceding getLength() test
//        return getStateAtCurrent().equals(INSIDE_BLOCK_COMMENT);
//      }
//      else return false;
//    }
//    catch(BadLocationException e) { throw new UnexpectedException(e); } // getText(pos,1) cannot fail; pos < getLength()
//    finally { setCurrentLocation(origPos); } // Restore state of reduced model
//  }
  
  /** Determines if pos lies within a line comment using the reduced model (ignoring the cache). Only called from 
    * event thread.  The position of the lead character in '//' opening of a line comment is NOT inside
    * the block.  This code is complicated by the fact that only positions of chars following the opening line 
    * comment bracket in the block comment have state IN_LINE_COMMENT.  Hence, the position of second char in the opening
    * bracket requires special case treatment. */
  public boolean _inLineComment(final int pos) {
    final int origPos = _currentLocation;  // save state of reduced model
    setCurrentLocation(pos);
//    System.err.println("In _inLineComment, pos = " + pos + ", origPos = " + origPos);
    try {
      ReducedModelState state = getStateAtCurrent();
//      System.err.println("state for position " + pos + " is: " + state);
      if (state.equals(INSIDE_LINE_COMMENT)) return true;
      /* Check to see if pos precedes either char in opening comment bracket */
      if (pos >= getLength()) return false;  // pos cannot point to second char in opening line comment marker
      char charAtPos = getText(pos,1).charAt(0);
      if (charAtPos == '/') {
        setCurrentLocation(pos + 1);   // pos must exist because of preceding getLength() test
        return getStateAtCurrent().equals(INSIDE_LINE_COMMENT);
      }
      else return false;
    }
    catch(BadLocationException e) { throw new UnexpectedException(e); } // getText(pos,1) cannot fail; pos < getLength()
    finally { setCurrentLocation(origPos); } // Restore state of reduced model
  } 
  
  /** Determines if pos lies within either a line comment or block comment using the reduced model (ignoring the cache)
    * INCLUDING the opening char in the bracket or marker opening the comment.  Assumes pos in not within a String.
    * Only called from event thread. */
  public boolean inComment(int pos) { return isWeaklyShadowed(pos); }
//    if (_insideBlockComment(pos) || _inLineComment(pos)) return true;
//    try {
//      String prefix = getText(pos, 2); // will fail if pos == getLength()
//      return (prefix.equals("//") || prefix.equals("/*"));
//    }
//    catch(BadLocationException e) { return false; }  // pos == getLength()
//  }
      
  /** Determines if _currentLocation lies within a block comment.  Only called from event thread. */
  public boolean _insideBlockComment() { return _insideBlockComment(_currentLocation); }
    
  /** Determines if the current line is a wing comment.  Only called from event thread. */
  public boolean isCurrLineWingComment() {
    int lineStart = _getLineStartPos(_currentLocation);
    return isCommentEscape(lineStart); 
  }
    
  /** Determines if the getText(pos, 2) is "//". Returns false if getText(pos,2) runs off end of this. */
  public boolean isCommentEscape(int pos) { 
    if (pos + 2 > getLength()) return false;
    return _getText(pos, 2).equals("//"); 
  }
  
  /** Assumes previous line in not inside a block comment.  Determines if the previous line
    * begins with a block comment open bracket or a line comment market '//'. */
  public boolean isPrevLineNewComment(int pos) {
   try {
     
      // Find the first non-whitespace character on the previous line.
      int startPos = _getLineStartPos(pos);
      if (startPos == 0) return false;
      int prevLineStartPos = _getLineStartPos(startPos - 1);
      
      String opening = _getText(getFirstNonWSCharPos(prevLineStartPos, true), 2);   // Comment text is recognized
      return opening.equals("//") || opening.equals("/*");
    }                                      
    catch (BadLocationException e) { return false; }  // getFirstNonWSCharPos(prevLineStartPos) may not exist
  }
  
  /** Determines if the previous line is within a block comment.  Returns false if prev line opens a block comment.
    * Only called from event thread. */
  public boolean isPrevLineInBlockComment(int pos) {
    
    // Find the first non-whitespace character on the current line.
    int startPos   = _getLineStartPos(pos);
    if (startPos == 0) return false;
    return isShadowed(_getLineStartPos(startPos - 1));
  }
  
  /** Determines if the previous line is empty (all whitespace). */
  public boolean isPrevLineEmpty(int pos) { 
    int startPos   = _getLineStartPos(pos);
    if (startPos == 0) return false;
    int prevLineStartPos = _getLineStartPos(startPos - 1);
    return _isLineEmpty(prevLineStartPos); 
  }
    
  /** Determines if the previous line is a comment or blank line.  Only called from event thread. */
  public boolean isPrevLineIgnorable(int pos) {
    return isPrevLineInBlockComment(pos) || isPrevLineNewComment(pos) ||  isPrevLineEmpty(pos);
  }
  
//  /** Determines if pos lies within a block comment using the cached result for previous line.  Assumes that
//    * _reduced lock is already held. Only called from event thread.
//    * @param resultPrevLine whether the start of the previous line is inside a block comment
//    * @param startPrevLine the document offset of the start of the previous line
//    * @param the len length of the previous line
//    */  
//  private boolean posInBlockComment(final boolean resultPrevLine, final int startPrevLine, final int len) {
//    try {
//      final String text = getText(startPrevLine, len);    // text of previous line
//      if (resultPrevLine) return text.indexOf("*/") < 0;  // inside a block comment unless "*/" found 
//      int startLineComment = text.indexOf("//");
//      int startBlockComment = text.indexOf("/*"); 
//      /* inside a block comment if "/*" found and it precedes "//" (if present) */
//      return startBlockComment >= 0 && (startLineComment == ERROR_INDEX || startLineComment > startBlockComment);
//    }
//    catch(BadLocationException e) { throw new UnexpectedException(e); }
//  }
  
  /** Returns true if the given position is not inside a paren/brace/etc phrase.  Assumes that reduced
    * lock is already held.  Only called from event thread.
    * @param pos the position we're looking at
    * @return true if pos is immediately inside a paren/brace/etc
    */
  protected boolean notInBlock(final int pos) {
    // Check cache
    final Query key = new Query.PosNotInBlock(pos);
    final Boolean cached = (Boolean) _checkCache(key);
    if (cached != null) return cached.booleanValue();
    
    final int origPos = _currentLocation;
    setCurrentLocation(pos);
    final BraceInfo info = _reduced._getEnclosingBrace();
    final boolean notInParenPhrase = info.braceType().equals(BraceInfo.NONE);
    setCurrentLocation(origPos);
    _storeInCache(key, notInParenPhrase, pos - 1);
    return notInParenPhrase;
  }
  
  /** Returns true if the current line has only spaces before the current location. Serves as a check so that
    * indentation will only move the caret when it is at or before the "smart" beginning of a line (i.e. the first
    * non-blank character). Only runs in the event thread.
    * @return true if there are only blank characters before the current location on the current line.
    */
  private boolean onlySpacesBeforeCurrent() throws BadLocationException{
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    int lineStart = _getLineStartPos(_currentLocation);
    if (lineStart < 0) lineStart = 0;    // _currentLocation on first line
    int prefixSize = _currentLocation - lineStart;
    
    // get prefix of _currentLocation (the text after the previous new line, but before the current location)
    String prefix = getText(lineStart, prefixSize);
    
    //check all positions in the prefix to determine if there are any blank chars
    int pos = prefixSize - 1;
    while (pos >= 0 && prefix.charAt(pos) == ' ') pos--;
    return (pos < 0);
  }
  
  /** Gets the number of blank characters between the current location and the first non-blank character or the end of
    * the document, whichever comes first.  TODO: cache it.
    * (The method is misnamed.)
    * @return the number of whitespace characters
    */
  private int getWhiteSpace() throws BadLocationException {
    
    /* */ assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    int lineEnd = _getLineEndPos(_currentLocation);  // index of next '\n' char or end of document
    int lineLen = lineEnd - _currentLocation;
    String line = getText(_currentLocation, lineLen);
    int i;
    for (i = 0; i < lineLen && line.charAt(i) == ' '; i++) ;
    return i;
  }
  
//  /** Returns the size of the white space prefix before the current location. If the prefix contains any
//    * non white space chars, returns 0.  Use definition of white space in String.trim()
//    * Only called from event thread.
//    * @return true if there are only blank characters before the current location on the current line.
//    */
//  private int getWhiteSpacePrefix() throws BadLocationException {
//    
////    System.err.println("lockState = " + _lockState);
//    
//    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    
//    int lineStart = _getLineStartPos(_currentLocation);
//    if (lineStart < 0) lineStart = 0;    // _currentLocation on first line
//    int prefixSize = _currentLocation - lineStart;
//    
//    // get prefix of _currentLocation (the text after the previous new line, but before the current location)
//    String prefix = getText(lineStart, prefixSize);
//    
//    //check all positions in the prefix to determine if there are any blank chars
//    int pos = prefixSize - 1;
//    while (pos >= 0 && prefix.charAt(pos) == ' ') pos--;
//    return (pos < 0) ? prefixSize : 0;
//  }
  
  /** Inserts the number of blanks specified as the whitespace prefix for the line identified by pos.  The prefix 
    * replaces the prefix already there.  Assumes that the prefix consists of spaces.  Only called from event thread.
    * @param tab  The string to be placed between previous newline and first non-whitespace character
    */
  public void setTab(int tab, int pos) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    System.err.println("Inserting prefix consisting of " + tab + " spaces in line '" + _getCurrentLine() + ";");
    
    try {
      int startPos = _getLineStartPos(pos);
      int firstNonWSPos = _getLineFirstCharPos(pos);
      int len = firstNonWSPos - startPos;
      
      // Adjust prefix
      if (len != tab) {
        // Only add or remove the difference
        int diff = tab - len;
        if (diff > 0) insertString(firstNonWSPos, StringOps.getBlankString(diff), null);
        else remove(firstNonWSPos + diff, -diff);
      }
      /* else do nothing */ 
    }
    catch (BadLocationException e) {
      // Should never see a bad location
      throw new UnexpectedException(e);
    }
  }
  
  /** Inserts the string specified by tab at the beginning of the line identified by pos. Only runs in event thread.
    * @param tab  The string to be placed between previous newline and first non-whitespace character
    */
  public void setTab(String tab, int pos) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
//    System.err.println("Inserting prefix '" + tab + "' in line '" + _getCurrentLine() + ";");
    try {
      int startPos = _getLineStartPos(pos);
      int firstNonWSPos = _getLineFirstCharPos(pos);
      int len = firstNonWSPos - startPos;
      
      // Remove the whole prefix, then add the new one
      remove(startPos, len);
      insertString(startPos, tab, null);
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
    * NOTE: only called in event thread.
    * @see edu.rice.cs.drjava.model.AbstractDJDocument.InsertCommand
    * @see javax.swing.text.AbstractDocument.DefaultDocumentEvent
    * @see edu.rice.cs.drjava.model.definitions.DefinitionsDocument.CommandUndoableEdit
    */
  protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng, AttributeSet attr) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    super.insertUpdate(chng, attr);
    
    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      final String str = getText(offset, length);
      
      if (length > 0) _clearCache(offset);    // Selectively clear the query cache
      
      Runnable doCommand = 
        (length == 1) ? new CharInsertCommand(offset, str.charAt(0)) : new InsertCommand(offset, str);
      RemoveCommand undoCommand = new UninsertCommand(offset, length, str);
      
      // add the undo/redo
      addUndoRedo(chng, undoCommand, doCommand);
      //chng.addEdit(new CommandUndoableEdit(undoCommand, doCommand));
      // actually do the insert
      doCommand.run();  // This method runs in the updating thread with exclusive access to the updated document
    }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }
  
  /** Updates document structure as a result of text removal. This happens within the swing remove operation before
    * the text has actually been removed. Updates the reduced model (using a {@link AbstractDJDocument.RemoveCommand
    * RemoveCommand}) and store information for how to undo/redo the reduced model changes inside the 
    * {@link javax.swing.text.AbstractDocument.DefaultDocumentEvent DefaultDocumentEvent}.
    * NOTE: only called in event thread.
    * @see AbstractDJDocument.RemoveCommand
    * @see javax.swing.text.AbstractDocument.DefaultDocumentEvent
    */
  protected void removeUpdate(AbstractDocument.DefaultDocumentEvent chng) {
    
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    try {
      final int offset = chng.getOffset();
      final int length = chng.getLength();
      
      final String removedText = getText(offset, length);
      super.removeUpdate(chng);
      
      if (length > 0) _clearCache(offset);  // Selectively clear the query cache
      
      Runnable doCommand = new RemoveCommand(offset, length, removedText);
      Runnable undoCommand = new UnremoveCommand(offset, removedText);
      
      // add the undo/redo info
      addUndoRedo(chng, undoCommand, doCommand);
      // actually do the removal from the reduced model
      doCommand.run();
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Returns the byte image (as written to a file) of this document. */
  public byte[] getBytes() { return getText().getBytes(); }
  
  public void clear() {
    try { remove(0, getLength()); }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** @return true if pos is the position of one of the chars in an occurrence of "//" or "/*" in text. */
  private static boolean isCommentOpen(String text, int pos) {
    int len = text.length();
    if (len < 2) return false;
    if (pos == len - 1) return isCommentStart(text, pos - 1);
    if (pos == 0) return isCommentStart(text, 0);
    return isCommentStart(text, pos - 1) || isCommentStart(text, pos);
  }
  
  /** @return true if pos is index of string "//" or "/*" in text.  Assumes pos < text.length() - 1 */
  private static boolean isCommentStart(String text, int pos) {
    char ch1 = text.charAt(pos);
    char ch2 = text.charAt(pos + 1);
    return ch1 == '/' && (ch2 == '/' || ch2 == '*');
  }
  
  //Two abstract methods to delegate to the undo manager, if one exists.
  protected abstract int startCompoundEdit();
  protected abstract void endCompoundEdit(int i);
  protected abstract void endLastCompoundEdit();
  protected abstract void addUndoRedo(AbstractDocument.DefaultDocumentEvent chng, Runnable undoCommand, 
                                      Runnable doCommand);
  
  //Checks if the document is closed, and then throws an error if it is.
  
  //-------- INNER CLASSES ------------
  
  //--- Fields set only by InsertCommand, CharInsertCommand, and RemoveCommand
  
  /** Offset marking where line number changes begin due to an insertion or deletion. */
  private volatile int _numLinesChangedAfter = ERROR_INDEX;
  
  //--- Private methods that only support these inner classes
  
  /** Updates _numLinesChanged given that a newline was inserted or removed at the specified offset. */
  private void _numLinesChanged(int offset) {
    if (_numLinesChangedAfter < 0) {
      _numLinesChangedAfter =  offset;
      return;
    }
    _numLinesChangedAfter = Math.min(_numLinesChangedAfter, offset);
  }
  
  /** Gets the value of _numLinesChangedAfter field and reset it to ERROR_INDEX (-1). */
  public int getAndResetNumLinesChangedAfter() {
    int result = _numLinesChangedAfter;
    _numLinesChangedAfter = ERROR_INDEX;
    return result;
  }
  
  protected class InsertCommand implements Runnable {
    protected final int _offset;
    protected final String _text;
    
    public InsertCommand(final int offset, final String text) {
      _offset = offset;
      _text = text;
    }
    
    /** Inserts chars in reduced model and moves location to end of insert; cache has already been cleared. */
    public void run() {
      
      _reduced.move(_offset - _currentLocation);  
      int len = _text.length();
      // Record any change to line numbering
      int newLineOffset = _text.indexOf(newline);
      if (newLineOffset >= 0) _numLinesChanged(_offset + newLineOffset);
      // loop over string, inserting characters into reduced model and recording any change to line numbering
      for (int i = 0; i < len; i++) { _addCharToReducedModel(_text.charAt(i)); }
      
      _currentLocation = _offset + len;  // update _currentLocation to match effects on the reduced model
      _styleChanged();  // update the color highlighting of the remainder of the document
      
//      if (getClass() ==  InsertCommand.class) 
//        System.err.println("Inserted '" + _text + "' loc is now " + _currentLocation);
    }
  }
  
  // command that undoes a RemoveCommand; same as InsertCommand except the cursor is placed before the inserted text
  protected class UnremoveCommand extends InsertCommand {
    public UnremoveCommand(final int offset, final String text) { super(offset, text); }
    public void run() {
      assert EventQueue.isDispatchThread();
      super.run();
//      System.err.println("Before restoration, currentLocation in unremove operation = " + getCurrentLocation());
      // The following command effectively modifies a document in a document listener; the invokeLater
      // call moves it out of the listener; pending events reset _currentLocation
      /* EventQueue.invokeLater(new Runnable() { public void run() { */ setCurrentLocation(_offset); /* } }); */
    }
  }
  
  protected class CharInsertCommand implements Runnable {
    protected final int _offset;
    protected final char _ch;
    
    public CharInsertCommand(final int offset, final char ch) {
      _offset = offset;
      _ch = ch;
    }
    
    /** Inserts chars in reduced model and moves location to end of insert; cache has already been cleared. */
    public void run() {
      
      _reduced.move(_offset - _currentLocation);  
      if (_ch == newline) _numLinesChanged(_offset);  // record change to line numbering
      _addCharToReducedModel(_ch);
      _currentLocation = _offset + 1;  // update _currentLocation to match effects on the reduced model
      _styleChanged();
    }
  }
  
  protected class RemoveCommand implements Runnable {
    protected final int _offset;
    protected final int _length;
    protected final String _removedText;
    
    public RemoveCommand(final int offset, final int length, final String removedText) {
      _offset = offset;
      _length = length;
      _removedText = removedText;
    }
    
    /** Removes chars from reduced model; cache has already been selectively cleared. */
    public void run() {
      setCurrentLocation(_offset);
      if (_removedText.indexOf(newline) >= 0) _numLinesChanged(_offset);  // record change to line numbering
      _reduced.delete(_length);    
      _styleChanged(); 
    }
  }
  
  // command that undoes an InsertCommand; identical to RemoveCommand; separate for debugging purposes
  protected class UninsertCommand extends RemoveCommand {
    public UninsertCommand(final int offset, final int length, String text) { super(offset, length, text); }
    public void run() { super.run(); }
  }
}
