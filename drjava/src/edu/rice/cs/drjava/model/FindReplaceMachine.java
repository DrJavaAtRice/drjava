/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.drjava.model;   

import edu.rice.cs.drjava.model.definitions.reducedmodel.ReducedModelStates;
  
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.Lambda;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/** Implementation of logic of find/replace over a document.
 *  @version $Id$
 */
public class FindReplaceMachine {
  
  // TODO: is _start still used in any way that matters?
  
  /* Visible machine state; manipulated directly or indirectly by FindReplacePanel. */
  private OpenDefinitionsDocument _doc;      // Current search document 
  private OpenDefinitionsDocument _firstDoc; // First document where searching started (when searching all documents)
  private Position _current;                 // Position of the cursor in _doc when machine is stopped
//  private Position _start;                   // Position in _doc from which searching started or will start.
  private String _findWord;                  // Word to find. */
  private String _replaceWord;               // Word to replace _findword.
  private boolean _matchCase;
  private boolean _matchWholeWord;
  private boolean _searchAllDocuments;       // Whether to search all documents (or just the current document)
  private boolean _isForward;                // Whether search direction is forward (false means backward)
  private boolean _ignoreCommentsAndStrings; // Whether to ignore matches in comments and strings
  private String _lastFindWord;              // Last word found; set to null by FindReplacePanel if caret is updated
  private boolean _skipText;                 // Whether to skip over the current match if direction is reversed
  private DocumentIterator _docIterator;     // An iterator of open documents; _doc is current
  private SingleDisplayModel _model;

  /** Standard Constructor.
   *  Creates new machine to perform find/replace operations on a particular document starting from a given position.
   *  @param docIterator an object that allows navigation through open Swing documents (it is DefaultGlobalModel)
   *  @exception BadLocationException
   */
  public FindReplaceMachine(SingleDisplayModel model, DocumentIterator docIterator) {    
    _skipText = false;
//    _checkAllDocsWrapped = false;
//    _allDocsWrapped = false;
    _model = model;
    _docIterator = docIterator;
    setFindAnyOccurrence();
    setFindWord("");
    setReplaceWord("");
    setSearchBackwards(false);
    setMatchCase(true);
    setSearchAllDocuments(false);
    setIgnoreCommentsAndStrings(false);
  }
  
  public void cleanUp() {
    _docIterator = null;
    setFindWord("");
    _doc = null;
  }
  
  /** Called when the current position is updated in the document implying _skipText should not be set
   *  if the user toggles _searchBackwards
   */
  public void positionChanged() {
    _lastFindWord = null;
    _skipText = false;
  }

  public void setLastFindWord() { _lastFindWord = _findWord; }

  public boolean getSearchBackwards() { return ! _isForward; }

  public void setSearchBackwards(boolean searchBackwards) {
    if (_isForward == searchBackwards) {
      // If we switch from searching forward to searching backwards or viceversa, isOnMatch is true, and _findword is the
      // same as the _lastFindWord, we know the user just found _findWord, so skip over this match.
      if (onMatch() && _findWord.equals(_lastFindWord)) _skipText = true;
      else _skipText = false;
    }
    _isForward = ! searchBackwards;
  }

  public void setMatchCase(boolean matchCase) { _matchCase = matchCase; }
  
  public void setMatchWholeWord() { _matchWholeWord = true; }
  
  public void setFindAnyOccurrence() { _matchWholeWord = false; }  

  public void setSearchAllDocuments(boolean searchAllDocuments) { _searchAllDocuments = searchAllDocuments; }
  
  public void setIgnoreCommentsAndStrings(boolean ignoreCommentsAndStrings) {
    _ignoreCommentsAndStrings = ignoreCommentsAndStrings;
  }

  public void setDocument(OpenDefinitionsDocument doc) { _doc = doc; }
  
  public void setFirstDoc(OpenDefinitionsDocument firstDoc) { _firstDoc = firstDoc; }
 
  public void setPosition(int pos) {
//    System.err.println("Setting position " + pos + " in doc [" + _doc.getText() + "]");
    assert (pos >= 0) && (pos <= _doc.getLength());
    try { _current = _doc.createPosition(pos); }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }

  /** Gets the character offset to which this machine is currently pointing. */
  public int getCurrentOffset() { return _current.getOffset(); }

  public String getFindWord() { return _findWord; }

  public String getReplaceWord() { return _replaceWord; }

  public boolean getSearchAllDocuments() { return _searchAllDocuments; }

  public OpenDefinitionsDocument getDocument() { return _doc; }
  
  public OpenDefinitionsDocument getFirstDoc() { return _firstDoc; }

  /** Change the word being sought.
   *  @param word the new word to seek
   */
  public void setFindWord(String word) {
    _findWord = word.replace(System.getProperty("line.separator"),"\n");
  }

  /** Change the replacing word.
   *  @param word the new replacing word
   */
  public void setReplaceWord(String word) { _replaceWord = word.replace(System.getProperty("line.separator"),"\n"); }

  /** Determine if the machine is on an instance of the find word.
   *  @return true if the current position is right after an instance of the find word.
   */
  public boolean onMatch() {
    String findWord = _findWord;
    int wordLen, off;
    
    if(_current == null) return false;
    
    wordLen = findWord.length();
    if (_isForward) off = getCurrentOffset() - wordLen;
    else off = getCurrentOffset();

    if (off < 0) return false;
    
     String matchSpace;
    _doc.acquireReadLock();
    try {
      if (off + wordLen > _doc.getLength()) return false;
      matchSpace = _doc.getText(off, wordLen);
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    finally { _doc.releaseReadLock(); }
    
    if (!_matchCase) {
      matchSpace = matchSpace.toLowerCase();
      findWord = findWord.toLowerCase();
    }
    return matchSpace.equals(findWord);
  }
  
  
  /** If we're on a match for the find word, replace it with the replace word. */
  public boolean replaceCurrent() {

    if (! onMatch()) return false;
    _doc.acquireWriteLock();
    try {
//      boolean atStart = false;
      int offset = getCurrentOffset();
      if (_isForward) offset -= _findWord.length();  // position is now on left edge of match
//      assert _findWord.equals(_doc.getText(offset, _findWord.length()));
      
//      Utilities.show("ReplaceCurrent called. _doc = " + _doc.getText() + " offset = " + offset + " _findWord = " + _findWord);
      
      _doc.remove(offset, _findWord.length());

//      if (position == 0) atStart = true;
      _doc.insertString(getCurrentOffset(), _replaceWord, null);
      
      // update _current Position
      if (_isForward) setPosition(offset + _replaceWord.length());
      else setPosition(offset);
      
      return true;
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    finally { _doc.releaseWriteLock(); }
  }

  /** Replaces all occurences of the find word with the replace word in the current document of in all documents
   *  depending the value of the machine register _searchAllDocuments.
   *  @return the number of replacements
   */
  public int replaceAll() { return replaceAll(_searchAllDocuments); }
  
  /** Replaces all occurences of the find word with the replace word in the current document of in all documents
   *  depending the value of the flag searchAll. 
   *  @return the number of replacements
   */
  private int replaceAll(boolean searchAll) {
    if (searchAll) {
      OpenDefinitionsDocument startDoc = _doc;
      int count = 0;           // the number of replacements done so farr
      int n = _docIterator.getDocumentCount();
      for (int i = 0; i < n; i++) {
        // replace all in the rest of the documents
        count += _replaceAllInCurrentDoc();
        _doc = _docIterator.getNextDocument(_doc);
      }
      
      // update display (adding "*") in navigatgorPane
      _model.getDocumentNavigator().repaint();
      
      return count;
    }
    else return _replaceAllInCurrentDoc();
  }
  
  /** Replaces all occurences of _findWord with _replaceWord in _doc. Never searches in other documents.  Starts at
   *  the beginning or the end of the document (depending on find direction).  This convention ensures that matches 
   *  created by string replacement will not be replaced as in the following example:<p>
   *    findString:    "hello"<br>
   *    replaceString: "e"<br>
   *    document text: "hhellollo"<p>
   *  Depending on the cursor position, clicking replace all could either make the document text read "hello" 
   *  (which is correct) or "e".  This is because of the behavior of findNext(), and it would be incorrect
   *  to change that behavior.
   *  @return the number of replacements
   */
  private int _replaceAllInCurrentDoc() {
//    System.err.println("_replaceAllInCurrentDoc() called; _doc = [" + _doc.getText() + "]");
    _doc.acquireWriteLock();
    try {
      if (_isForward) setPosition(0);
      else setPosition(_doc.getLength());
      
      int count = 0;
      FindResult fr = findNext(false);  // find next match in current doc   
//      Utilities.show(fr + " returned by call on findNext()");
      
      while (! fr.getWrapped()) {
        replaceCurrent();
        count++;
//        Utilities.show("Found " + count + " occurrences. Calling findNext() inside loop");
        fr = findNext(false);           // find next match in current doc
//        Utilities.show("Call on findNext() returned " + fr.toString() + "in doc '" + _doc.getText() + "'");
      }
      return count;
    }
    finally { _doc.releaseWriteLock(); }
  }
  

  /** Processes all occurences of the find word with the replace word in the current document or in all documents
   *  depending the value of the machine register _searchAllDocuments.
   *  @param findAction action to perform on the occurrences; input is the FindResult, output is ignored
   *  @return the number of processed occurrences
   */
  public int processAll(Lambda<Void, FindResult> findAction) { return processAll(findAction, _searchAllDocuments); }
  
  /** Processes all occurences of the find word with the replace word in the current document of in all documents
   *  depending the value of the flag searchAll. 
   *  @param findAction action to perform on the occurrences; input is the FindResult, output is ignored
   *  @return the number of replacements
   */
  private int processAll(Lambda<Void, FindResult> findAction, boolean searchAll) {
    if (searchAll) {
      OpenDefinitionsDocument startDoc = _doc;
      int count = 0;           // the number of replacements done so farr
      int n = _docIterator.getDocumentCount();
      for (int i = 0; i < n; i++) {
        // process all in the rest of the documents
        count += _processAllInCurrentDoc(findAction);
        _doc = _docIterator.getNextDocument(_doc);
      }
      
      // update display (perhaps adding "*") in navigatgorPane
      _model.getDocumentNavigator().repaint();
      
      return count;
    }
    else return _processAllInCurrentDoc(findAction);
  }
  
  /** Processes all occurences of _findWord in _doc. Never processes other documents.  Starts at
   *  the beginning or the end of the document (depending on find direction).  This convention ensures that matches 
   *  created by string replacement will not be replaced as in the following example:<p>
   *    findString:    "hello"<br>
   *    replaceString: "e"<br>
   *    document text: "hhellollo"<p>
   *  @param findAction action to perform on the occurrences; input is the FindResult, output is ignored
   *  @return the number of replacements
   */
  private int _processAllInCurrentDoc(Lambda<Void, FindResult> findAction) {
    _doc.acquireWriteLock();
    try {
      if (_isForward) setPosition(0);
      else setPosition(_doc.getLength());
      
      int count = 0;
      FindResult fr = findNext(false);  // find next match in current doc   
      
      while (! fr.getWrapped()) {
        findAction.apply(fr);
        count++;
        fr = findNext(false);           // find next match in current doc
      }
      return count;
    }
    finally { _doc.releaseWriteLock(); }
  }
  
  public FindResult findNext() { return findNext(_searchAllDocuments); }

  /** Finds the next occurrence of the find word and returns an offset at the end of that occurrence or -1 if the word
   *  was not found.  Selectors should select backwards the length of the find word from the find offset.  This 
   *  position is stored in the current offset of the machine, and that is why it is after: in subsequent searches, the
   *  same instance won't be found twice.  In a backward search, the position returned is at the beginning of the word.  
   *  Also returns a flag indicating whether the end of the document was reached and wrapped around. This is done
   *  using the FindResult class which contains the matching document, an integer offset and two flag indicated whether
   *  the search wrapped (within _doc and across all documents)
   *  @param searchAll whether to search all documents (or just _doc)
   *  @return a FindResult object containing foundOffset and a flag indicating wrapping to the beginning during a search
   */
  private FindResult findNext(boolean searchAll) {
    
    // Find next match, if any, in _doc. 
    _doc.acquireReadLock();
    FindResult fr;
    int start;
    int len;
    try {
      
      // If the user just found a match and toggled the "Search Backwards" option, we should skip the matched text.
      if (_skipText) {  // adjust position (offset)
//      System.err.println("Skip text is true!  Last find word = " + _lastFindWord);
        int wordLen = _lastFindWord.length();
        if (_isForward) setPosition(getCurrentOffset() + wordLen);
        else setPosition(getCurrentOffset() - wordLen);
        positionChanged();
      }
      
//    System.err.println("findNext(" + searchAll + ") called with _doc = [" + _doc.getText() + "] and offset = " + _current.getOffset());
      
      int offset = getCurrentOffset();
//    System.err.println("findNext(" + searchAll + ") called; initial offset is " + offset);
//    System.err.println("_doc = [" + _doc.getText() + "], _doc.getLength() = " + _doc.getLength());
      if (_isForward) { 
        start = offset; 
        len = _doc.getLength() - offset; 
      }
      else { 
        start = 0; 
        len = offset; 
      }
      fr = _findNextInDoc(_doc, start, len, searchAll);
    }
    finally { _doc.releaseReadLock(); }
    if ((fr.getFoundOffset() >= 0) || ! searchAll) return fr;  // match found in _doc
    
    // find match in other docs
    return _findNextInOtherDocs(_doc, start, len);
  }

 
  /** Finds next match in specified doc only.  If searching forward, len must be doc.getLength().  If searching backward,
   *  start must be 0.  If searchAll, suppress executing in-document wrapped search, because it must be deferred.  Assumes
   *  acquireReadLock is already held.  Note than this method does a wrapped search if specified search fails.
   */
  private FindResult _findNextInDoc(OpenDefinitionsDocument doc, int start, int len, boolean searchAll) {
    // search from current position to "end" of document ("end" is start if searching backward)
//    System.err.println("_findNextInDoc([" + doc.getText() + "], " + start + ", " + len + ", " + searchAll + ")");
    FindResult fr = _findNextInDocSegment(doc, start, len);
    if (fr.getFoundOffset() >= 0 || searchAll) return fr;
    
    return _findWrapped(doc, start, len, false);  // last arg is false because search has not wrapped through all docs
  }
  
  /** Helper method for findNext that looks for a match after searching has wrapped off the "end" (start if searching
   *  backward) of the document. Assumes acquireReadLock is already held!  
   *  INVARIANT (! _isForward => start = 0) && (_isForward => start + len = doc.getLength()).
   *  @param doc  the document in which search wrapped
   *  @param start location of preceding text segment where search FAILED.  
   *  @param len  length of text segment previously searched
   *  @param allWrapped  whether this wrapped search is being performed after an all document search has wrapped
   *  @return the offset where the instance was found. Returns -1 if no instance was found between start and end
   */  
  private FindResult _findWrapped(OpenDefinitionsDocument doc, int start, int len, boolean allWrapped) {
    int newLen, newStart;
    
    assert (_isForward && start + len == doc.getLength()) || (! _isForward && start == 0);
    
//    System.err.println("_findWrapped(" + doc + ", " + start + ", " + len + ", " + allWrapped + ")  docLength = " +
//                       doc.getLength() + ", _isForward = " + _isForward);

    if (doc.getLength() == 0) return new FindResult(doc, -1, true, allWrapped);
    if (_isForward) {
      newLen = start;
      newStart = 0;
    }
    else {
      newStart = len;
      newLen = doc.getLength() - len;
    }
//      System.err.println("Calling _findNextInDocSegment(" + doc.getText() + ", newStart = " + newStart + ", newLen = " + 
//                     newLen + ", allWrapped = " + allWrapped + ") and _isForward = " + _isForward);
    return _findNextInDocSegment(doc, newStart, newLen, true, allWrapped);
  } 
      
      
  /** Find first valid match withing specified segment of doc.  Assumes acquireReadLock is already held. */  
  private FindResult _findNextInDocSegment(OpenDefinitionsDocument doc, int start, int len) {
    return _findNextInDocSegment(doc, start, len, false, false);
  }
  
  /** Main helper method for findNext... that searches for _findWord inside the specified document segment.  Assumes
   *  acquireReadLock is already held!
   *  @param doc document to be searched
   *  @param start the location (offset) of the text segment to be searched 
   *  @param len the length of the text segment to be searched
   *  @param whether this search should span all documents
   *  @param wrapped whether this search is after wrapping around the document
   *  @param allWrapped whether this seach is after wrapping around all documents
   *  @return a FindResult object with foundOffset and a flag indicating wrapping to the beginning during a search. The
   *  foundOffset returned insided the FindResult is -1 if no instance was found.
   */
  private FindResult 
    _findNextInDocSegment(OpenDefinitionsDocument doc, int start, int len, boolean wrapped, boolean allWrapped) {  
//    Utilities.show("called _findNextInDocSegment(" + doc.getText() + ",\n" + start + ", " + len + ", " + wrapped + " ...)");
    
    if (len == 0 || doc.getLength() == 0) return new FindResult(doc, -1, wrapped, allWrapped);
    
    int docLen;     // The length of the segment to be searched
    String text;    // The text segment to be searched
    
    String findWord = _findWord;       // copy of word being searched (so it can converted to lower case if necessary
    int wordLen = findWord.length();   // length of search key (word being searched fo  
    
    try { 
      docLen = doc.getLength();
//      if (wrapped && allWrapped) Utilities.show(start +", " + len + ", " + docLen + ", doc = '" + doc.getText() + "'");
      text = doc.getText(start, len);
      
      if (! _matchCase) {
        text = text.toLowerCase();
        findWord = findWord.toLowerCase();  // does not affect wordLen
      }
      
//       if (wrapped && allWrapped) Utilities.show("Executing loop with findWord = " + findWord + "; text = " + text + "; len = " + len);     
      
      // loop to find first valid (not ignored) occurrence of findWord
      // loop carried variables are text, len, start; 
      // loop invariant variables are _doc, docLen, _isForward, findWord, wordLen.
      // On forward search, (start + len) is invariant; on backward search start is invariant.
      // loop exits by returning match (as FindResult) or by falling through with no match.
      // if match is returned, _current has been updated to match location
      while (len >= wordLen) {
        
        // Find next match in text
        int foundOffset = _isForward ? text.indexOf(findWord) : text.lastIndexOf(findWord);
        if (foundOffset < 0) break;  // no valid match in this document
        int foundLocation = start + foundOffset;
        int matchLocation;
        
        if (_isForward) {
          int adjustedOffset = foundOffset + wordLen;
          start += adjustedOffset;                       // start is moved to match
          text = text.substring(adjustedOffset, len);    // len is length of text before update
          len = len - adjustedOffset;                    // len is updated to length of text after update
          matchLocation = start;                         // matchLocation is index in _doc of right edge of match
//            _current = docToSearch.createPosition(start);          // put caret at beginning of found word
        }
        else {
          len = foundOffset;                             // start is left invariant; len is moved to match
          matchLocation = start + foundOffset;           // matchLocation is index in _doc of left edge of match
          text = text.substring(0, len);                 // len is length of text after update
//            _current = docToSearch.createPosition(foundLocation);  // put caret at end of found word
        }
        
        doc.setCurrentLocation(foundLocation);           // _shouldIgnore below uses reduced model
        
//        Utilities.show("Finished iteration with text = " + text + "; len = " + len);
        if (_shouldIgnore(foundLocation, doc)) continue;
        
        _current = doc.createPosition(matchLocation);   
        
//        System.err.println("Returning result = " + new FindResult(doc, matchLocation, wrapped, allWrapped));

        return new FindResult(doc, matchLocation, wrapped, allWrapped);  // return valid match
      }
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    // loop fell through; search failed in doc segment
    return new FindResult(doc, -1, wrapped, allWrapped);
  }
  
  /** Searches all documents following startDoc for _findWord, cycling through the documents in the direction specified
   *  by _isForward. If the search cycles back to doc without finding a match, performs a wrapped search on doc.
   *  @param startDoc  document where searching started and just failed
   *  @param start  location in startDoc of the document segment where search failed.
   *  @param len  length of the text segment where search failed.
   *  @return the FindResult containing the information for where we found _findWord or a dummy FindResult.
   */
  private FindResult _findNextInOtherDocs(final OpenDefinitionsDocument startDoc, int start, int len) {
    
//    System.err.println("_findNextInOtherDocs(" + startDoc.getText() + ", " + start + ", " + len + ")");
  
    boolean allWrapped = false;
    _doc = _isForward ? _docIterator.getNextDocument(startDoc) : _docIterator.getPrevDocument(startDoc);

    while (_doc != startDoc) {
      if (_doc == _firstDoc) allWrapped = true;
      
//      System.err.println("_doc = [" + _doc.getText() + "]");
      
//      if (_isForward) setPosition(0);
//      else setPosition(_doc.getLength());
      

      // find next match in _doc
      _doc.acquireReadLock();
      FindResult fr;
      try { fr = _findNextInDocSegment(_doc, 0, _doc.getLength(), false, allWrapped); } 
      finally { _doc.releaseReadLock(); }
      
      if (fr.getFoundOffset() >= 0) return fr;
      
//      System.err.println("Advancing from '" + _doc.getText() + "' to next doc");        
      _doc = _isForward ? _docIterator.getNextDocument(_doc) : _docIterator.getPrevDocument(_doc);     
//      System.err.println("Next doc is: '" + _doc.getText() + "'");
    }
    
    // No valid match found; perform wrapped search.  _findWrapped assumes acquireReadLock is held.
    startDoc.acquireReadLock();
    try { return _findWrapped(startDoc, start, len, true); }  // last arg is true because searching all docs has wrapped
    finally { startDoc.releaseReadLock(); } 
  } 
  
  /** Determines whether the whole find word is found at the input position
   * 
   * @param doc - the document where an instance of the find word was found
   * @param foundOffset - the position where that instance was found
   * @return true if the whole word is found at foundOffset, false otherwise
   */
  private boolean wholeWordFoundAtCurrent(OpenDefinitionsDocument doc, int foundOffset) {    
    String docText;
    doc.acquireReadLock();
    try { docText = doc.getText(); }
    finally {doc.releaseReadLock();}      
    
    Character leftOfMatch = null;
    Character rightOfMatch = null;
    int leftLocation = foundOffset - 1;
    int rightLocation = foundOffset + _findWord.length();
    boolean leftOutOfBounds = false;
    boolean rightOutOfBounds = false;
    
    try { leftOfMatch = new Character(docText.charAt(leftLocation)); }
    catch (IndexOutOfBoundsException e) { leftOutOfBounds = true; }
    
    try { rightOfMatch = new Character(docText.charAt(rightLocation)); }
    catch (IndexOutOfBoundsException e) { rightOutOfBounds = true; }
    
    if (!leftOutOfBounds && !rightOutOfBounds) 
      return isDelimiter(rightOfMatch) && isDelimiter(leftOfMatch);
    if (!leftOutOfBounds) return isDelimiter(leftOfMatch);
    if (!rightOutOfBounds) return isDelimiter(rightOfMatch);
    return true;
  }

  /** Determines whether a character is a delimiter (not a letter or digit) as a helper to wholeWordFoundAtCurrent
   * 
   * @param ch - a character
   * @return true if ch is a delimiter, false otherwise
   */
  private boolean isDelimiter(Character ch) {
    return !Character.isLetterOrDigit(ch.charValue());
  }
  
  /** Returns true if the currently found instance should be ignored (either because it is inside a string or comment or
   *  because it does not match the whole word when either or both of those conditions are set to true).
   * 
   *  @param foundOffset the location of the instance found
   *  @param doc the current document where the instance was found
   *  @return true if the location should be ignored, false otherwise
   */
  private boolean _shouldIgnore(int foundOffset, OpenDefinitionsDocument odd) {
    return (_matchWholeWord && ! wholeWordFoundAtCurrent(odd, foundOffset)) || 
      (_ignoreCommentsAndStrings && odd.getStateAtCurrent() != ReducedModelStates.FREE);
  }
}