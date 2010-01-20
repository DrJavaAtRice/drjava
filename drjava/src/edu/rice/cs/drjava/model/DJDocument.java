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

package edu.rice.cs.drjava.model;

import edu.rice.cs.drjava.model.definitions.reducedmodel.*;
import edu.rice.cs.util.text.SwingDocumentInterface;
import edu.rice.cs.util.OperationCanceledException;
import edu.rice.cs.drjava.model.definitions.indent.Indenter;

import java.util.ArrayList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.ProgressMonitor;

/** Interface shared by the Definitions Document, Open Definitions Document, and Interactions Document. Characteristic
  * of native DrJava Documents (as opposed to DrJava plugin documents).
  */
public interface DJDocument extends SwingDocumentInterface {
  
  /** Gets the indent level.
    * @return the indent level
    */
  public int getIndent();
  
  /** Sets the indent to a particular number of spaces.
    * @param indent the size of indent that you want for the document
    */
  public void setIndent(int indent);
  
  /** Returns highlight status info for text between start and end, coalescing adjoining blocks with the same status. */
  public ArrayList<HighlightStatus> getHighlightStatus(int start, int end);
  
  /** Gets the current location of the cursor the document. Unlike the Swing document model, which is stateless, we must
    * maintain a current location as part of the document because the implementation of the reduced model relies on this
    * state information.
    * @return where the cursor is as the number of characters into the document
    */
  public int getCurrentLocation();
  
  /** Change the current location of the document
    * @param loc the new absolute location
    */
  public void setCurrentLocation(int loc);
  
  /** Moves the current location the specified number of chars (positive is right; negative is left).  It is used as a
    * helper for setCurrentLocation(int).
    * @param dist the distance from the current location to the new location.
    */
  public void move(int dist);
  
//  /* Returns whether a block indent operation is in progress on this document. */
//  public boolean indentInProgress();
  
  /** Finds the match for the closing brace immediately to the left, assuming there is such a brace.
    * @return the relative distance backwards to the offset before the matching brace.
    */
  public int balanceBackward();
  
  /** Finds the match for the open brace immediately to the right, assuming there is such a brace.
    * @return the relative distance forwards to the offset after the matching brace.
    */
  public int balanceForward();
  
//  /** Returns the indent information for the current location. */
//  public IndentInfo getIndentInformation();
  
//  public ReducedModelState stateAtRelLocation(int dist);
  
  public ReducedModelState getStateAtCurrent();
  
//  public void resetReducedModelLocation();
  
  /** Searching backwards, finds the position of the enclosing brace. NB: ignores comments.
    * @param pos Position to start from
    * @param opening opening brace character
    * @param closing closing brace character
    * @return position of enclosing curly brace, or ERROR_INDEX (-1) if beginning
    * of document is reached.
    */
  public int findPrevEnclosingBrace(int pos, char opening, char closing) throws BadLocationException;
  
  /** Searching forwards, finds the position of the enclosing brace.
    * NB: ignores comments.
    * @param pos Position to start from
    * @param opening opening brace character
    * @param closing closing brace character
    * @return position of enclosing curly brace, or ERROR_INDEX (-1) if beginning
    * of document is reached.
    */
  public int findNextEnclosingBrace(int pos, char opening, char closing) throws BadLocationException;
  
  /** Searching backwards, finds the position of the first character that is one
    * of the given delimiters.  Does not look for delimiters inside paren phrases.
    * (eg. skips semicolons used inside for statements.)
    * NB: ignores comments.
    * @param pos Position to start from
    * @param delims array of characters to search for
    * @return position of first matching delimiter, or ERROR_INDEX (-1) if beginning
    * of document is reached.
    */
  public int findPrevDelimiter(int pos, char[] delims) throws BadLocationException;
  
  /** Searching backwards, finds the position of the first character that is one
   * of the given delimiters.  Will not look for delimiters inside a paren
   * phrase if skipParenPhrases is true.
   * NB: ignores comments.
   * @param pos Position to start from
   * @param delims array of characters to search for
   * @param skipParenPhrases whether to look for delimiters inside paren phrases
   * (eg. semicolons in a for statement)
   * @return position of first matching delimiter, or ERROR_INDEX (-1) if beginning
   * of document is reached.
   */
  public int findPrevDelimiter(int pos, char[] delims, boolean skipParenPhrases) throws BadLocationException;
  
//  /** This function finds the given character in the same statement as the given
//   * position, and before the given position.  It is used by QuestionExistsCharInStmt and
//   * QuestionExistsCharInPrevStmt
//   */
//  public boolean findCharInStmtBeforePos(char findChar, int position);
  
//  /** Finds the position of the first non-whitespace character before pos.
//   * NB: Skips comments and all whitespace, including newlines
//   * @param pos Position to start from
//   * @param whitespace chars considered as white space
//   * @return position of first non-whitespace character before pos,
//   * or ERROR_INDEX (-1) if begining of document is reached
//   */
//  public int findPrevCharPos(int pos, char[] whitespace) throws BadLocationException;
  
  /** Default indentation - uses OTHER flag and no progress indicator.
    * @param selStart the offset of the initial character of the region to indent
    * @param selEnd the offset of the last character of the region to indent
    */
  public void indentLines(int selStart, int selEnd);
  
  /** Parameterized indentation for special-case handling.
    * @param selStart the offset of the initial character of the region to indent
    * @param selEnd the offset of the last character of the region to indent
    * @param reason a flag from {@link edu.rice.cs.drjava.model.definitions.indent.Indenter Indenter}
    *        to indicate the reason for the indent (indent logic may vary slightly based on the trigger action)
    * @param pm used to display progress, null if no reporting is desired
    */
  public void indentLines(int selStart, int selEnd, Indenter.IndentReason reason, ProgressMonitor pm)
    throws OperationCanceledException;
  
  /** Returns the "intelligent" beginning of line.  If currPos is to the right of the first non-whitespace character,
    * the position of the first non-whitespace character is returned.  If currPos is at or to the left of the first 
    * non-whitespace character, the beginning of the line is returned.
    * @param currPos A position on the current line
    */
  public int getIntelligentBeginLinePos(int currPos) throws BadLocationException;;
  
  /** Returns the indent level of the start of the statement that the cursor is on.  Uses a default set of delimiters.
    * (';', '{', '}') and a default set of whitespace characters (' ', '\t', n', ',').
    * @param pos Cursor position
    */
  public int _getIndentOfCurrStmt(int pos) throws BadLocationException;
  
  /** Returns the indent level of the start of the statement that the cursor is on.  Uses a default set of whitespace
    * characters (' ', '\t', '\n', ',').
    * @param pos Cursor position
    */
  public int _getIndentOfCurrStmt(int pos, char[] delims) throws BadLocationException;
  
  /** Returns the indent level of the start of the statement
   * that the cursor is on.
   * @param pos Cursor position
   * @param delims Delimiter characters denoting end of statement
   * @param whitespace characters to skip when looking for beginning of next statement
   */
  public int _getIndentOfCurrStmt(int pos, char[] delims, char[] whitespace)
    throws BadLocationException;
  
  /** Determines if the given character exists on the line where
   * the given cursor position is. Does not search in quotes or comments.
   * <p>
   * <b>Does not work if character being searched for is a '/' or a '*'</b>
   * @param pos Cursor position
   * @param findChar Character to search for
   * @return true if this node's rule holds.
   */
  public int findCharOnLine(int pos, char findChar);
  
  /** Returns the absolute position of the beginning of the
   * current line.  (Just after most recent newline, or 0)
   * Doesn't ignore comments.
   * @param pos Any position on the current line
   * @return position of the beginning of this line
   */
  public int _getLineStartPos(int pos);
  
  /** Returns the absolute position of the end of the current
   * line.  (At the next newline, or the end of the document.)
   * @param pos Any position on the current line
   * @return position of the end of this line
   */
  public int _getLineEndPos(int pos);
  
  /** Returns the absolute position of the first non-whitespace character
   * on the current line.
   * NB: Doesn't ignore comments.
   * @param pos position on the line
   * @return position of first non-whitespace character on this line, or the end
   * of the line if no non-whitespace character is found.
   */
  public int _getLineFirstCharPos(int pos) throws BadLocationException;
  
  /** Finds the position of the first non-whitespace character after pos.
   * NB: Skips comments and all whitespace, including newlines
   * @param pos Position to start from
   * @return position of first non-whitespace character after pos,
   * or ERROR_INDEX (-1) if end of document is reached
   */
  public int getFirstNonWSCharPos(int pos) throws BadLocationException;
  
  /** Similar to the single-argument version, but allows including comments.
   * @param pos Position to start from
   * @param acceptComments if true, find non-whitespace chars in comments
   * @return position of first non-whitespace character after pos,
   * or ERROR_INDEX (-1) if end of document is reached
   */
  public int getFirstNonWSCharPos(int pos, boolean acceptComments) 
    throws BadLocationException;
  
  /** Finds the position of the first non-whitespace character after pos.
   * NB: Skips comments and all whitespace, including newlines
   * @param pos Position to start from
   * @param whitespace array of whitespace chars to ignore
   * @param acceptComments if true, find non-whitespace chars in comments
   * @return position of first non-whitespace character after pos,
   * or ERROR_INDEX (-1) if end of document is reached
   */
  public int getFirstNonWSCharPos (int pos, char[] whitespace, boolean acceptComments)
    throws BadLocationException;
  
//  public int findPrevNonWSCharPos(int pos) throws BadLocationException;
  
//  /** Returns true if the given position is inside a paren phrase.
//   * @param pos the position we're looking at
//   * @return true if pos is immediately inside parentheses
//   */
//  public boolean inParenPhrase(int pos);
  
//  /** Returns true if the reduced model's current position is inside a paren phrase.
//    * @return true if pos is immediately inside parentheses
//    */
//  public boolean posInParenPhrase();
  
//  /** Gets the number of whitespace characters between the current location and the rest of the document or the 
//    * first non-whitespace character, whichever comes first.
//    * @return the number of whitespace characters
//    */
//  public int getWhiteSpace();
  
//  /** Sets text between previous newline and first non-whitespace character of the line containing pos to tab.
//    * @param tab String to be placed between previous newline and first non-whitespace character
//    */
//  public void setTab(String tab, int pos);
//  
//  /** Sets text between previous newline and first non-whitespace character of the line containing pos to a prefix
//    * consisting of tab spaces.
//    * @param tab String to be placed between previous newline and first non-whitespace character
//    */
//  public void setTab(int tab, int pos);
  
  /** Inserts a string of text into the document. It turns out that this is not where we should do custom processing
    * of the insert; that is done in {@link AbstractDJDocument#insertUpdate}.
    */
  public void insertString(int offset, String str, AttributeSet a) throws BadLocationException;
  
  /** Removes a block of text from the specified location.  We don't update the reduced model here; that happens
    * in {@link AbstractDJDocument#removeUpdate}.
    */
  public void remove(int offset, int len) throws BadLocationException;
  
  /** Gets the entire text of the document.  Without this operation, a client must use locking to perform this
    * task safely.
    */
  public String getText();
  
  /** Clears the entire text of the document.  Without this operation, a client must use locking to perform this
    * task safely.
    */
  public void clear();
  
  /* Locking operations */
  
  /* Gets the reduced model so it can be locked. */
  public ReducedModelControl getReduced();
}