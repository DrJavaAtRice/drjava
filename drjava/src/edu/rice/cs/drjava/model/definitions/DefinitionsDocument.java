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
import java.util.HashSet;
import java.util.StringTokenizer;

import java.io.File;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.drjava.model.definitions.indent.Indenter;

/**
 * The model for the definitions pane.
 *
 * This implementation of <code>Document</code> contains a 
 * "reduced model". The reduced model is automatically kept in sync
 * when this document is updated. Also, that synchronization is maintained
 * even across undo/redo -- this is done by making the undo/redo commands know
 * how to restore the reduced model state.
 *
 * @see BraceReduction
 * @see ReducedModelControl
 * @see ReducedModelComment
 * @see ReducedModelBrace
 *
 * @version $Id$
 */
public class DefinitionsDocument extends PlainDocument {
  /** A set of normal endings for lines. */
  private static HashSet _normEndings = _makeNormEndings();
  /** A set of Java keywords. */
  private static HashSet _keywords = _makeKeywords();
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
  /**
   * The reduced model of the document that handles most of the
   * document logic and keeps track of state.
   */
  BraceReduction _reduced = new ReducedModelControl();
  /** The absolute character offset in the document. */
  int _currentLocation = 0;
  
  private File _file;
  
  /**
   * Constant for starting position of document.
   */
  public static final int DOCSTART = 0;

  /**
   * Constant used by helper methods to indicate an error
   */
  public static final int ERROR_INDEX = -1;

  /**
   * Constructor.
   */
  public DefinitionsDocument() {
    super();
    _file = null;
    _cachedLocation = 0;
    _cachedLineNum = 1;
    _cachedPrevLineLoc = -1;
    _cachedNextLineLoc = -1;
  }

  /**
   * @return The reduced model of this document.
   */
  public BraceReduction getReduced() {
    return _reduced;
  }

  /**
   * Create a set of normal endings, i.e., semi-colons and braces for the purposes
   * of indenting.
   * @return the set of normal endings
   */
  private static HashSet _makeNormEndings() {
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
  private static HashSet _makeKeywords() {
    final String[] words =  {
      "import", "native", "package", "goto", "const", "if", "else",
      "switch", "while", "for", "do", "true", "false", "null", "this",
      "super", "new", "instanceof", "boolean", "char", "byte",
      "short", "int", "long", "float", "double", "void", "return",
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
   * @exception IllegalStateException if no file exists
   */
  public File getFile() throws IllegalStateException {
    if (_file == null) {
      throw new IllegalStateException(
        "This document does not yet have a file.");
    }
    return _file;
  }

  public void setFile(File file) {
    _file = file;
  }

  /**
   * Inserts a string of text into the document.
   * It turns out that this is not where we should do custom processing
   * of the insert; that is done in {@link #insertUpdate}.
   */
  public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException
  {
    // If _removeTabs is set to true, remove all tabs from str.
    // It is a current invariant of the tabification functionality that
    // the document contains no tabs, but we want to allow the user
    // to override this functionality.
    if (_tabsRemoved) {
      str = _removeTabs(str);
    }

    _modifiedSinceSave = true;

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
    _modifiedSinceSave = true;
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
  private void _addCharToReducedModel(char curChar) {
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
   * Whenever this document has been saved, this method should be called
   * so that it knows it's no longer in a modified state.
   */
  public void resetModification() {
    try {
      writeLock();
      _modifiedSinceSave = false;
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
   * Get the current location of the cursor in the document.
   * Unlike the usual swing document model, which is stateless, because of our implementation
   * of the underlying reduced model, we need to keep track fo the current location.
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
  public void move(int dist) {
    int oldLoc = _currentLocation;
    _currentLocation += dist;
    
    if (_currentLocation < 0) {
      throw  new RuntimeException("location < 0?! oldLoc=" + oldLoc + " dist=" +
          dist);
    }
    _reduced.move(dist);
  }
  
  /** 
   * Return the current column of the cursor position.
   * Uses a 1 based index.
   */
  public int getCurrentCol() {
    int here = getCurrentLocation();
    int startOfLine = getLineStartPos(here);
    return here - startOfLine + 1;
  }
  
  /**
   * Return the current line of the cursor position.
   * Uses a 1 based index.
   */
  public int getCurrentLine() {
    // don't know if this does anything.
    // if (_cachedNextLineLoc == -1 ){ initNextLineNum(); }
    int here = getCurrentLocation();
    // let's see if we get off easy
    if( _cachedPrevLineLoc < here && here < _cachedNextLineLoc ){ 
      return _cachedLineNum;
    }
    
    // test to see which is easier: starting from the top
    // or calculating relatively.
    if( _cachedLocation - here > here ){ 
      _cachedLineNum = getLineFromScratch(); 
    }
    else {
      int lineOffset = getRelativeLine(here);
      _cachedLineNum = _cachedLineNum+lineOffset;      
    }
    _cachedLocation = here;
    _cachedPrevLineLoc = getLineStartPos(here);
    _cachedNextLineLoc = here + _reduced.getDistToNextNewline();
    return _cachedLineNum;
  }
  
  public int getLineFromScratch(){
    int count=1;
    int _copyLocation = getCurrentLocation();
    int distPrevNewLine = _reduced.getDistToPreviousNewline( 0 );

    while (distPrevNewLine != -1 && getCurrentLocation()>0) {
      setCurrentLocation( getCurrentLocation()-distPrevNewLine-1 );
      count++;
      distPrevNewLine = _reduced.getDistToPreviousNewline( 0 );
    }
    setCurrentLocation( _copyLocation );
    return count;
  }
  
  

  /**
   * This method returns the relative offset of line number
   * from the previous location in the document.
   **/
  private int getRelativeLine( int currLoc ){
    // we moved backwards
    int count=0;
    setCurrentLocation( _cachedLocation );
    if( _cachedLocation > currLoc ){
      int distPrevNewLine = _reduced.getDistToPreviousNewline( 0 );
      while (distPrevNewLine != -1 && getCurrentLocation()>currLoc) {
        setCurrentLocation( getCurrentLocation()-distPrevNewLine-1 );
        count--;
        distPrevNewLine = _reduced.getDistToPreviousNewline( 0 );
      }
      // this and the similar test in the other case
      // account for the fact that our loop condition guarantees
      // one extraneous loop through the document.
      if( getCurrentLocation() != currLoc ){ count++; }
   }
   // we moved forwards
   else{
     int distNextNewLine = _reduced.getDistToNextNewline();
     while (distNextNewLine != -1 && getCurrentLocation()<currLoc) {
       setCurrentLocation( getCurrentLocation()+distNextNewLine+1 );
       count++;
       distNextNewLine = _reduced.getDistToNextNewline();
     }
     if( getCurrentLocation() != currLoc ){ count--; }
   }
     setCurrentLocation( currLoc );
     return count;
   }
  
  private void initNextLineNum(){
    _cachedNextLineLoc = _reduced.getDistToNextNewline();
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
  public int findPrevDelimiter(int pos, char[] delims, boolean skipParenPhrases)
    throws BadLocationException
  {
    int j, i;
    char c;
    String text = getText(DOCSTART, pos);
    
    final int origLocation = _reduced.absOffset();
    // Move reduced model to location pos
    _reduced.move(pos - origLocation);
    
    // Walk backwards from specificed position
    for (i = pos-1; i >= DOCSTART; i--) {
      c = text.charAt(i);
      // Check if character is one of the delimiters
      for (j = 0; j < delims.length; j++) {
        if (c == delims[j]) {
          // Move reduced model to walker's location
          _reduced.move(i - pos);
          // Check if matching char is in comment or quotes
          ReducedModelState state = _reduced.getStateAtCurrent();
          if (!state.equals(ReducedModelState.FREE)
                || _isStartOfComment(text, i)
                || ((i > 0) && _isStartOfComment(text, i - 1))) {
            // Ignore matching char
          } else {
            // Found a matching char, check if we should ignore it
            if (skipParenPhrases && posInParenPhrase(i)) {
              // In a paren phrase, so ignore
            }
            else {
              // Return position of matching char
              _reduced.move(origLocation - i);
              return i;
            }
          }
          _reduced.move(pos - i);
        }
      }
    }
    _reduced.move(origLocation - pos);
    return ERROR_INDEX;
  }

  /**
   * Returns true if the given position is inside a paren phrase.
   * @param pos the position we're looking at
   * @return true if pos is immediately inside parentheses
   */
  public boolean posInParenPhrase(int pos) {
    int here = _reduced.absOffset();
    _reduced.move(pos - here);
    IndentInfo info = _reduced.getIndentInformation();
    boolean inParenPhrase = info.braceTypeCurrent.equals(IndentInfo.openParen);
    _reduced.move(here - pos);
    return inParenPhrase;
  }

  /**
   * Returns the indent level of the start of the statement
   * that the cursor is on.  Uses a default set of delimiters.
  * (';', '{', '}')
   * @param pos Cursor position
   */
  public String getIndentOfCurrStmt(int pos) throws BadLocationException {
    char[] delims = {';', '{', '}'};
    return getIndentOfCurrStmt(pos, delims);
  }
  
  /**
   * Returns the indent level of the start of the statement
   * that the cursor is on.
   * @param pos Cursor position
   * @param delims Delimiter characters denoting end of statement
   */
  public String getIndentOfCurrStmt(int pos, char[] delims) throws BadLocationException {
    
    // Get the start of the current line
    int lineStart = getLineStartPos(pos);
    
    // Find the previous delimiter that closes a statement
    boolean reachedStart = false;
    int prevDelimiter = lineStart; 
    do {
      prevDelimiter = findPrevDelimiter(prevDelimiter, delims);
      // Check delimiter found was start of document
      if(prevDelimiter == ERROR_INDEX) {
        reachedStart = true;
        break;
      }
    } while(posInParenPhrase(prevDelimiter));
    
    // From the previous delimiter, find the next
    // non-whitespace character
    int nextNonWSChar;
    if(reachedStart) {
      nextNonWSChar = getFirstNonWSCharPos(DOCSTART);
    } else {
      nextNonWSChar = getFirstNonWSCharPos(prevDelimiter+1);
    }
    
    
    // If the end of the document was reached
    if(nextNonWSChar == ERROR_INDEX) {
      nextNonWSChar = getLength();
    }
    
    // Get the start of the line of the non-ws char
    int lineStartStmt = getLineStartPos(nextNonWSChar);
    
    // Get the position of the first non-ws character on 
    // this line
    int lineFirstNonWS = getLineFirstCharPos(lineStartStmt);
    
    String lineText = "";
    try {
      lineText = getText(lineStartStmt, lineFirstNonWS - lineStartStmt);
    } catch(BadLocationException e) {
      // Should not be here
      throw new UnexpectedException(e);
    } 
    
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
    int reducedAbsOffset = this.getReduced().absOffset();
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
    while(i != -1) {
      // Move reduced model to walker's location
      this.getReduced().move(i + lineStart - reducedAbsOffset);
      
      // Check if matching char is in comment or quotes
      if (!this.getReduced().getStateAtCurrent().equals(ReducedModelState.FREE)) {
        // Ignore matching char
      } else {
        // Return position of matching char
        this.getReduced().move(reducedAbsOffset - (i + lineStart));
        return (i + lineStart);
      }
      this.getReduced().move(reducedAbsOffset - (i + lineStart));
      i = lineText.indexOf(findChar, i+1);
    }
    return ERROR_INDEX;
  }
  
  /**
   * Returns the absolute position of the beginning of the
   * current line.  (Just after most recent newline, or DOCSTART)
   * Doesn't ignore comments.
   * @param pos Any position on the current line
   * @return position of the beginning of this line
   */
  public int getLineStartPos(int pos) {
    int location = _reduced.absOffset();
    _reduced.move(pos - location);
    int dist = _reduced.getDistToPreviousNewline(0);
    _reduced.move(location - pos);
    if(dist == -1) {
      // If no previous newline was found
      // return DOCSTART
      return DOCSTART;
    } else {
      return pos - dist;
    }
  }
  
  /**
   * Returns the absolute position of the end of the current
   * line.  (At the next newline, or the end of the document.)
   * @param pos Any position on the current line
   * @return position of the end of this line
   */
  public int getLineEndPos(int pos) {
    int location = _reduced.absOffset();
    _reduced.move(pos - location);
    int dist = _reduced.getDistToNextNewline();
    _reduced.move(location - pos);
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
        return startLinePos + walker;
      }
    }
    // No non-WS char found, so return last position on line
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
    int j, i;
    char c;
    int endPos = getLength();
    String text = getText(pos, endPos - pos);
    char[] whitespace = {' ', '\t', '\n'};
    
    final int origLocation = _reduced.absOffset();
    // Move reduced model to location pos
    _reduced.move(pos - origLocation);
    
    // Walk forward from specificed position
    for (i = pos; i != endPos; i++) {
      boolean isWhitespace = false;
      c = text.charAt(i - pos);
      // Check if character is whitespace
      for (j = 0; j < whitespace.length; j++) {
        if (c == whitespace[j]) {
          isWhitespace = true;
        }
      }
      if (!isWhitespace) {
        // Move reduced model to walker's location
        _reduced.move(i - pos);
        // Check if matching char is in comment
        if((_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_LINE_COMMENT)) ||
           (_reduced.getStateAtCurrent().equals(ReducedModelState.INSIDE_BLOCK_COMMENT))) {
             // Ignore matching char
           } else { 
             if(_isStartOfComment(text, i - pos)) {
               // Move i past the start of comment characters
               // and continue searching
               i = i + 1;
               _reduced.move(1);
             } else {
               // Return position of matching char
               _reduced.move(origLocation - i);
               return i;
             }
           }
        _reduced.move(pos - i);
      }
    }
    _reduced.move(origLocation - pos);
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
  public int balanceBackward() {
    return _reduced.balanceBackward();
  }


  public void indentLines(int selStart, int selEnd) {
    try {
      if (selStart == selEnd) {
        Position oldCurrentPosition = createPosition(_currentLocation);
        _indentLine();
        //int caretPos = getCaretPosition();
        //_doc().setCurrentLocation(caretPos);
        setCurrentLocation(oldCurrentPosition.getOffset());
        int space = getWhiteSpace();
        move(space);
        //setCaretPosition(caretPos + space);
      }
      else {
        _indentBlock(selStart, selEnd);
      }
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Indents the lines between and including the lines containing
   * points start and end.
   * @param start Position in document to start indenting from
   * @param end Position in document to end indenting at
   */
  private void _indentBlock(final int start, final int end) {
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
        _indentLine();
        // Move back to walker spot
        setCurrentLocation(walkerPos.getOffset());
        walker = walkerPos.getOffset();
        // Adding 1 makes us point to the first character AFTER the
        // next newline.
        // We don't actually move yet. That happens at the top of the loop,
        // after we check if we're past the end.
        walker += _reduced.getDistToNextNewline() + 1;
      }
    } catch (BadLocationException e) {
      throw  new RuntimeException("Impossible bad loc except: " + e);
    }
  }

  /**
   * Indents a line using the Indenter decision tree.
   */
  private void _indentLine() {
    Indenter.ONLY.indent(this);
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
  private boolean _isCommentedOrSpace(int i, String text) {
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
      
      // Removes old prefix, then adds new one
      // FIXME: If tab only contains spaces, then just adjust as necessary
      //   for efficiency, rather than replacing the whole thing
      if (!_hasOnlySpaces(tab) || (len != tab.length())) {
        remove(startPos, len);
        insertString(startPos, tab, null);
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
  public Vector<HighlightStatus> getHighlightStatus(int start, int end) {
    //DrJava.consoleErr().println("getHi: start=" + start + " end=" + end +
    //" currentLoc=" + _currentLocation);

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

    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (_keywords.contains(token)) {
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
                                              HighlightStatus.KEYWORD),
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
   * Goes to a particular line in the document.
   */
  public void gotoLine(int line) {
    int dist;
    if (line < 0) {
     return;
    }
    setCurrentLocation(0);
    for (int i = 1; (i < line) && (_currentLocation < getLength()); i++) {
      dist = _reduced.getDistToNextNewline();
      if (_currentLocation + dist < getLength()) {
        dist++;
      }
      move(dist);
    }
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
  public String getPackageName() throws InvalidPackageException {
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
      setCurrentLocation(oldLocation);
    }
  }

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
      _reduced.move(_offset - _currentLocation);

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
      _reduced.delete(_length);
      _styleChanged();
    }
  }
}
