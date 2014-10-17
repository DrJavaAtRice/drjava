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

import edu.rice.cs.plt.lambda.Runnable1;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.swing.DocumentIterator;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.Log;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.drjava.config.OptionConstants;

import java.awt.EventQueue;

import javax.swing.text.BadLocationException;
import javax.swing.JOptionPane;
import java.awt.Component;

/** Implementation of logic of find/replace over a document.
  * @version $Id: FindReplaceMachine.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class FindReplaceMachine {
  
  static private Log _log = new Log("FindReplace.txt", false);
  
  /* Visible machine state; manipulated directly or indirectly by FindReplacePanel. */
  private OpenDefinitionsDocument _doc;      // Current search document 
  private OpenDefinitionsDocument _firstDoc; // First document where searching started (when searching all documents)
//  private Position _current;                 // Position of the cursor in _doc when machine is stopped
  private int _current;                 // Position of the cursor in _doc when machine is stopped
  private MovingDocumentRegion _selectionRegion; // selected text region
//  private Position _start;                   // Position in _doc from which searching started or will start.
  private String _findWord;                  // Word to find. */
  private String _replaceWord;               // Word to replace _findword.
  private boolean _matchCase;
  private boolean _matchWholeWord;
  private boolean _searchAllDocuments;       // Whether to search all documents (or just the current document)
  private boolean _searchSelectionOnly;      // Whether to search only the selection
  private boolean _isForward;                // Whether search direction is forward (false means backward)
  private boolean _ignoreCommentsAndStrings; // Whether to ignore matches in comments and strings
  private boolean _ignoreTestCases;          // Whether to ignore documents that end in *Test.java
  private String _lastFindWord;              // Last word found; set to null by FindReplacePanel if caret is updated
  private boolean _skipText;                 // Whether to skip over the current match if direction is reversed
  private DocumentIterator _docIterator;     // An iterator of open documents; _doc is current
  private SingleDisplayModel _model;
  private Component _frame;  
  
  /** Standard Constructor.
    * Creates new machine to perform find/replace operations on a particular document starting from a given position.
    * @param docIterator an object that allows navigation through open Swing documents (it is DefaultGlobalModel)
    */
  public FindReplaceMachine(SingleDisplayModel model, DocumentIterator docIterator, Component frame) {    
    _skipText = false;
//    _checkAllDocsWrapped = false;
//    _allDocsWrapped = false;
    _model = model;
    _frame = frame;
    _docIterator = docIterator;
    _current = -1;
    setFindAnyOccurrence();
    setFindWord("");
    setReplaceWord("");
    setSearchBackwards(false);
    setMatchCase(true);
    setSearchAllDocuments(false);
    setSearchSelectionOnly(false);
    setIgnoreCommentsAndStrings(false);
    setIgnoreTestCases(false);
  }
  
  public void cleanUp() {
    _docIterator = null;
    setFindWord("");
    _doc = null;
  }
  
  /** Called when the current position is updated in the document implying _skipText should not be set
    * if the user toggles _searchBackwards
    */
  public void positionChanged() {
    _lastFindWord = null;
    _skipText = false;
  }
  
  public void setLastFindWord() { _lastFindWord = _findWord; }
  
  public boolean isSearchBackwards() { return ! _isForward; }
  
  public void setSearchBackwards(boolean searchBackwards) {
    if (_isForward == searchBackwards) {
      // If we switch from searching forward to searching backwards or vice versa, isOnMatch is true, and _findword is
      // the same as the _lastFindWord, we know the user just found _findWord, so skip over this match.
      if (onMatch() && _findWord.equals(_lastFindWord)) _skipText = true;
      else _skipText = false;
    }
    _isForward = ! searchBackwards;
  }
  
  public void setMatchCase(boolean matchCase) { _matchCase = matchCase; }
  public boolean getMatchCase() { return _matchCase; }
  
  public void setMatchWholeWord() { _matchWholeWord = true; }
  
  public boolean getMatchWholeWord() { return _matchWholeWord; }
  
  public void setFindAnyOccurrence() { _matchWholeWord = false; }  
  
  public void setSearchAllDocuments(boolean searchAllDocuments) { _searchAllDocuments = searchAllDocuments; }
  
  public void setSearchSelectionOnly(boolean searchSelectionOnly) { _searchSelectionOnly = searchSelectionOnly; }
  
  public void setIgnoreCommentsAndStrings(boolean ignoreCommentsAndStrings) {
    _ignoreCommentsAndStrings = ignoreCommentsAndStrings;
  }
  public boolean getIgnoreCommentsAndStrings() { return _ignoreCommentsAndStrings; }
  
  public void setIgnoreTestCases(boolean ignoreTestCases) {
    _ignoreTestCases = ignoreTestCases;
  }
  public boolean getIgnoreTestCases() { return _ignoreTestCases; }

  public void setDocument(OpenDefinitionsDocument doc) { _doc = doc; }
  
  public void setFirstDoc(OpenDefinitionsDocument firstDoc) { _firstDoc = firstDoc; }
  
  public void setPosition(int pos) { _current = pos; }
  
  /** Gets the character offset to which this machine is currently pointing. */
  public int getCurrentOffset() { //return _current.getOffset(); 
    return _current;
  }
  
  public String getFindWord() { return _findWord; }
  
  public String getReplaceWord() { return _replaceWord; }
  
  public boolean getSearchAllDocuments() { return _searchAllDocuments; }
  
  public boolean getSearchSelectionOnly() { return _searchSelectionOnly; }
  
  public OpenDefinitionsDocument getDocument() { return _doc; }
  
  public OpenDefinitionsDocument getFirstDoc() { return _firstDoc; }
  
  /** Change the word being sought.
    * @param word the new word to seek
    */
  public void setFindWord(String word) {  
    _findWord = StringOps.replace(word, StringOps.EOL, "\n"); 
  }
  
  /** Change the replacing word.
    * @param word the new replacing word
    */
  public void setReplaceWord(String word) { 
    _replaceWord = StringOps.replace(word, StringOps.EOL,"\n"); 
  }
  
  /** Determine if the machine is on an instance of the find word.  Only executes in event thread except for
    * initialization.
    * @return true if the current position is right after an instance of the find word.
    */
  public boolean onMatch() {
    
    // Should be fixed now because of invokeAndWait in MainFrame constructor
    // (was: this invariant doesn't hold.  See DrJava bug #2321815)
    assert Utilities.TEST_MODE || EventQueue.isDispatchThread();
    
    String findWord = _findWord;
    int wordLen, off;
    
    if(_current == -1) return false;
    
    wordLen = findWord.length();
    if (_isForward) off = getCurrentOffset() - wordLen;
    else off = getCurrentOffset();
    
    if (off < 0) return false;
    
    String matchSpace;
    try {
      if (off + wordLen > _doc.getLength()) return false;
      matchSpace = _doc.getText(off, wordLen);
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    
    if (!_matchCase) {
      matchSpace = matchSpace.toLowerCase();
      findWord = findWord.toLowerCase();
    }
    return matchSpace.equals(findWord);
  }
  
  /** If we're on a match for the find word, replace it with the replace word.  Only executes in event thread. */
  public boolean replaceCurrent() {
    
    assert EventQueue.isDispatchThread();
    
    if (! onMatch()) return false;
    try {
//      boolean atStart = false;
      int offset = getCurrentOffset();
      if (_isForward) offset -= _findWord.length();  // position is now on left edge of match
//      assert _findWord.equals(_doc.getText(offset, _findWord.length()));
      
//      Utilities.show("ReplaceCurrent called. _doc = " + _doc.getText() + " offset = " + offset + " _findWord = " +
//        _findWord);
      
      _doc.remove(offset, _findWord.length());
      
//      if (position == 0) atStart = true;
      _doc.insertString(offset, _replaceWord, null);  // could use _insertString if we had the DefinitionsDocument
      
      // update _current Position
      if (_isForward) setPosition(offset + _replaceWord.length());
      else setPosition(offset);
      
      return true;
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }

  /** Set the selected text region.
    * @param s selected region
    */
  public void setSelection(MovingDocumentRegion s) { 
    _selectionRegion = s;
  }

  /** Replaces all occurrences of the find word with the replace word in the current document of in all documents
    * depending the value of the machine register _searchAllDocuments.
    * @return the number of replacements
    */
  public int replaceAll() { 
    return replaceAll(_searchAllDocuments, _searchSelectionOnly); 
  }
  
  /** Replaces all occurences of the find word with the replace word in the current document of in all documents or 
    * in the current selection of the current document depending the value of the flag searchAll
    * @return the number of replacements
    */
  private int replaceAll(boolean searchAll, boolean searchSelectionOnly) {
    if (searchAll) {
      int count = 0;           // the number of replacements done so far
      int n = _docIterator.getDocumentCount();
      for (int i = 0; i < n; i++) {
        // replace all in the rest of the documents
        count += _replaceAllInCurrentDoc(false);
        _doc = _docIterator.getNextDocument(_doc, _frame);
        
        if(_doc==null) break;
      }
      
      // update display (adding "*") in navigatgorPane
      _model.getDocumentNavigator().repaint();
      
      return count;
    }
    else if(searchSelectionOnly) {
      int count = 0;
      count += _replaceAllInCurrentDoc(searchSelectionOnly);
      return count;
    }
    else 
      return _replaceAllInCurrentDoc(false);
  }
  
  /** Replaces all occurences of _findWord with _replaceWord in _doc. Never searches in other documents.  Starts at
    * the beginning or the end of the document (depending on find direction).  This convention ensures that matches 
    * created by string replacement will not be replaced as in the following example:<p>
    *   findString:    "hello"<br>
    *   replaceString: "e"<br>
    *   document text: "hhellollo"<p>
    * Depending on the cursor position, clicking replace all could either make the document text read "hello" 
    * (which is correct) or "e".  This is because of the behavior of findNext(), and it would be incorrect
    * to change that behavior.  Only executes in event thread.
    * @return the number of replacements
    */
  private int _replaceAllInCurrentDoc(boolean searchSelectionOnly) {
    
    assert EventQueue.isDispatchThread();
    
    if(!searchSelectionOnly) {
      _selectionRegion = new MovingDocumentRegion(_doc, 0, _doc.getLength(),
                                                  _doc._getLineStartPos(0),
                                                  _doc._getLineEndPos(_doc.getLength()));
    }
    if (_isForward) setPosition(_selectionRegion.getStartOffset());
    else setPosition(_selectionRegion.getEndOffset());
    
    int count = 0;
    FindResult fr = findNext(false);  // find next match in current doc   
    //  Utilities.show(fr + " returned by call on findNext()");
    
    while (!fr.getWrapped() && fr.getFoundOffset() <= _selectionRegion.getEndOffset()) {
      replaceCurrent();
      count++;
      //  Utilities.show("Found " + count + " occurrences. Calling findNext() inside loop");
      fr = findNext(false);           // find next match in current doc
      //  Utilities.show("Call on findNext() returned " + fr.toString() + "in doc '" + 
      //    _doc.getText().substring(0,fr.getFoundOffset()) + "[|]" + _doc.getText().substring(fr.getFoundOffset()) + "'");
    }
    return count;
  }
  
  /** Processes all occurences of the find word with the replace word in the current document or in all documents
    * depending the value of the machine register _searchAllDocuments.
    * @param findAction action to perform on the occurrences; input is the FindResult, output is ignored
    * @return the number of processed occurrences
    */
  public int processAll(Runnable1<FindResult> findAction, MovingDocumentRegion region) { 
    _selectionRegion = region;
    return processAll(findAction, _searchAllDocuments, _searchSelectionOnly); 
  }
  
  /** Processes all occurences of the find word with the replace word in the current document or in all documents
    * depending the value of the flag searchAll.  Assumes that findAction does not modify the document it processes.
    * Only executes in event thread.
    * @param findAction action to perform on the occurrences; input is the FindResult, output is ignored
    * @return the number of replacements
    */
  private int processAll(Runnable1<FindResult> findAction, boolean searchAll, boolean searchSelectionOnly) {
    
    assert EventQueue.isDispatchThread();
    
    if (searchAll) {
      int count = 0;           // the number of replacements done so far
      int n = _docIterator.getDocumentCount();
      for (int i = 0; i < n; i++) {
        // process all in the rest of the documents
        count += _processAllInCurrentDoc(findAction, false);
        _doc = _docIterator.getNextDocument(_doc, _frame);
        
        if(_doc==null) break;
      }
      
      // update display (perhaps adding "*") in navigatgorPane
      _model.getDocumentNavigator().repaint();
      
      return count;
    }
    else if(searchSelectionOnly) {
      int count = 0;
      count += _processAllInCurrentDoc(findAction, searchSelectionOnly);
      return count;
    }
    else return _processAllInCurrentDoc(findAction, false);
  }
  
  /** Processes all occurences of _findWord in _doc. Never processes other documents.  Starts at the beginning or the
    * end of the document (depending on find direction).  This convention ensures that matches created by string 
    * replacement will not be replaced as in the following example:<p>
    *  findString:    "hello"<br>
    *  replaceString: "e"<br>
    *  document text: "hhellollo"<p>
    * Assumes this has mutually exclusive access to _doc (e.g., by hourglassOn) and findAction does not modify _doc.
    * Only executes in event thread.
    * @param findAction action to perform on the occurrences; input is the FindResult, output is ignored
    * @return the number of replacements
    */
  private int _processAllInCurrentDoc(Runnable1<FindResult> findAction, boolean searchSelectionOnly) {
    if(!searchSelectionOnly) {
      _selectionRegion = new MovingDocumentRegion(_doc, 0, _doc.getLength(),
                                                  _doc._getLineStartPos(0),
                                                  _doc._getLineEndPos(_doc.getLength()));
    }
    if (_isForward) setPosition(_selectionRegion.getStartOffset());
    else setPosition(_selectionRegion.getEndOffset());
    
    int count = 0;
    FindResult fr = findNext(false);  // find next match in current doc   
    
    while (! fr.getWrapped() && fr.getFoundOffset() <= _selectionRegion.getEndOffset()) {
      findAction.run(fr);
      count++;
      fr = findNext(false);           // find next match in current doc
    }
    return count;
  }
  
  public FindResult findNext() { return findNext(_searchAllDocuments); }
  
  /** Finds the next occurrence of the find word and returns an offset at the end of that occurrence or -1 if the word
    * was not found.  In a forward search, the match offset is the RIGHT edge of the word.  In subsequent searches, the
    * same instance won't be found again.  In a backward search, the position returned is the LEFT edge of the word.  
    * Also returns a flag indicating whether the end of the document was reached and wrapped around. This is done
    * using the FindResult class which contains the matching document, an integer offset and two flag indicated whether
    * the search wrapped (within _doc and across all documents).  Only executes in the event thread.
    * @param searchAll whether to search all documents (or just _doc)
    * @return a FindResult object containing foundOffset and a flag indicating wrapping to the beginning during a search
    */
  private FindResult findNext(boolean searchAll) {
    
    assert EventQueue.isDispatchThread();
    
    // Find next match, if any, in _doc. 
    FindResult fr;
    int start;
    int len;
    
    // If the user just found a match and toggled the "Search Backwards" option, we should skip the matched text.
    if (_skipText) {  // adjust position (offset)
//      System.err.println("Skip text is true!  Last find word = " + _lastFindWord);
      int wordLen = _lastFindWord.length();
      if (_isForward) setPosition(getCurrentOffset() + wordLen);
      else setPosition(getCurrentOffset() - wordLen);
      positionChanged();
    }
    
//    System.err.println("findNext(" + searchAll + ") called with _doc = [" + _doc.getText() + "] and offset = " +
//      _current.getOffset());
    
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
    if (fr.getFoundOffset() >= 0 || ! searchAll) return fr;  // match found in _doc or search is local
    
    // find match in other docs
    return _findNextInOtherDocs(_doc, start, len);
  }
  
  
  /** Finds next match in specified doc only.  If searching forward, len must be doc.getLength().  If searching backward,
    * start must be 0.  If searchAll, suppress executing in-document wrapped search, because it must be deferred.  Only
    * runs in the event thread.  Note than this method does a wrapped search if specified search fails.
    */
  private FindResult _findNextInDoc(OpenDefinitionsDocument doc, int start, int len, boolean searchAll) {
    // search from current position to "end" of document ("end" is start if searching backward)
//    Utilities.show("_findNextInDoc([" + doc.getText() + "], " + start + ", " + len + ", " + searchAll + ")");
//    _log.log("_findNextInDoc([" + doc.getText() + "], " + start + ", " + len + ", " + searchAll + ")");
    FindResult fr = _findNextInDocSegment(doc, start, len);
    if (fr.getFoundOffset() >= 0 || searchAll) return fr;
    
    return _findWrapped(doc, start, len, false);  // last arg is false because search has not wrapped through all docs
  }
  
  /** Helper method for findNext that looks for a match after searching has wrapped off the "end" (start if searching
    * backward) of the document.  Only runs in event thread.  
    * INVARIANT (! _isForward => start = 0) && (_isForward => start + len = doc.getLength()).
    * @param doc  the document in which search wrapped
    * @param start the location of preceding text segment where search FAILED.  
    * @param len  the length of text segment previously searched
    * @param allWrapped  whether this wrapped search is being performed after an all document search has wrapped
    * @return the offset where the instance was found. Returns -1 if no instance was found between start and end
    */  
  private FindResult _findWrapped(OpenDefinitionsDocument doc, int start, int len, boolean allWrapped) {
    
    final int docLen = doc.getLength();
    if (docLen == 0) return new FindResult(doc, -1, true, allWrapped); // failure result
    
    final int wordLen =  _findWord.length();
    
    assert (start >= 0 && start <= docLen) && (len >= 0 && len <= docLen) && wordLen > 0;
    assert (_isForward && start + len == docLen) || (! _isForward && start == 0);
//    Utilities.show("_findWrapped(" + doc + ", " + start + ", " + len + ", " + allWrapped + ")  docLength = " +
//                       doc.getLength() + ", _isForward = " + _isForward);
//    _log.log("_findWrapped(" + doc + ", " + start + ", " + len + ", " + allWrapped + ")  docLength = " +
//             doc.getLength() + ", _isForward = " + _isForward);
    
    int newLen;
    int newStart;
    
    final int adjustment = wordLen - 1; // non-negative max size of the findWord suffix (prefix) within preceding text
    
    if (_isForward) {
      newStart = 0;
      newLen = start + adjustment;  // formerly start, which was an annoying bug
      if (newLen > docLen) newLen = docLen;
    }
    else {
      newStart = len - adjustment;
      if (newStart < 0) newStart = 0;
      newLen = docLen - newStart;
    }
    
//    _log.log("Calling _findNextInDocSegment(" + doc.getText() + ", newStart = " + newStart + ", newLen = " + 
//             newLen + ", allWrapped = " + allWrapped + ") and _isForward = " + _isForward);
    return _findNextInDocSegment(doc, newStart, newLen, true, allWrapped);
  } 
  
  /** Find first valid match withing specified segment of doc. */  
  private FindResult _findNextInDocSegment(OpenDefinitionsDocument doc, int start, int len) {
    return _findNextInDocSegment(doc, start, len, false, false);
  }
  
  /** Main helper method for findNext... that searches for _findWord inside the specified document segment.  Only runs
    * in the event thread.
    * @param doc document to be searched
    * @param start the location (offset/left edge) of the text segment to be searched 
    * @param len the requested length of the text segment to be searched
    * @param wrapped whether this search is after wrapping around the document
    * @param allWrapped whether this seach is after wrapping around all documents
    * @return a FindResult object with foundOffset and a flag indicating wrapping to the beginning during a search. The
    * foundOffset returned insided the FindResult is -1 if no instance was found.
    */
  private FindResult _findNextInDocSegment(final OpenDefinitionsDocument doc, final int start, int len, 
                                           final boolean wrapped, final boolean allWrapped) {  
//    Utilities.show("called _findNextInDocSegment(" + doc.getText() + ",\n" + start + ", " + len + ", " + wrapped +
//      " ...)");
    boolean inTestCase = false;
    for(String ext: OptionConstants.LANGUAGE_LEVEL_EXTENSIONS) {
      inTestCase |= doc.getFileName().endsWith("Test" + ext);
    }
    
    if (!_ignoreTestCases || ! inTestCase) {
      final int docLen = doc.getLength();;     // The length of the segment to be searched
      final int wordLen = _findWord.length();   // length of search key (word being searched for)
      
      assert (start >= 0 && start <= docLen) && (len >= 0 && len <= docLen);
      
      if (len == 0 || docLen == 0) return new FindResult(doc, -1, wrapped, allWrapped);
      
      if (start + len > docLen) len = docLen - start;
      
//    if (start + len > docLen) len = docLen - start;
      
      String text;             // The text segment to be searched
      final String findWord;   // copy of word being searched (so it can converted to lower case if necessary
      
      try { 
        
//      if (wrapped && allWrapped) Utilities.show(start  + ", " + len + ", " + docLen + ", doc = '" + doc.getText() + "'");
        text = doc.getText(start, len);
        
        if (! _matchCase) {
          text = text.toLowerCase();
          findWord = _findWord.toLowerCase();  // does not affect wordLen
        }
        else findWord = _findWord;
//       if (wrapped && allWrapped) Utilities.show("Executing loop with findWord = " + findWord + "; text = " + text +
//          "; len = " + len);     
        
        // loop to find first valid (not ignored) occurrence of findWord
        // loop carried variables are rem, foundOffset; 
        // loop invariant variables are _doc, docLen, _isForward, findWord, wordLen, start, len.
        // Invariant:  on forwardsearch, foundOffset + rem == len; on backward search foundOffset == rem.
        // loop exits by returning match (as FindResult) or by falling through with no match.
        // if match is returned, _current has been updated to match location
        int foundOffset = _isForward? 0 : len;
        int rem = len;
//      _log.log("Starting search loop; text = '" + text + "' findWord = '" + findWord + "' forward? = " + _isForward +
//         " rem = " + rem + " foundOffset = " + foundOffset);
        while (rem >= wordLen) {
          
          // Find next match in text
          foundOffset = _isForward ? text.indexOf(findWord, foundOffset) : text.lastIndexOf(findWord, foundOffset);
//        _log.log("foundOffset = " + foundOffset);
          if (foundOffset < 0) break;  // no valid match in this document
          int foundLocation = start + foundOffset;
          int matchLocation;
          
          if (_isForward) {
            foundOffset += wordLen;                          // skip over matched word
//          text = text.substring(adjustedOffset, len);    // len is length of text before update
            rem = len - foundOffset;                         // len is updated to length of remaining text to search
            matchLocation = foundLocation + wordLen;         // matchLocation is index in _doc of right edge of match
//            _current = docToSearch.createPosition(start);          // put caret at beginning of found word
          }
          else { 
            
            foundOffset -= wordLen;                        // skip over matched word        
            rem = foundOffset;                             // rem is adjusted to match foundOffset
            matchLocation = foundLocation;                 // matchLocation is index in _doc of left edge of match
//          text = text.substring(0, len);               // len is length of text after update
//            _current = docToSearch.createPosition(foundLocation);  // put caret at end of found word
          }
//        _log.log("rem = " + rem);
          
//        _log.log("Finished iteration with text = " + text + "; len = " + len + "; foundLocation = " + foundLocation);
          assert foundLocation > -1;
          if (_shouldIgnore(foundLocation, doc)) continue;
          
          //_current = doc.createPosition(matchLocation);   // formerly doc.createPosition(...)
          setPosition(matchLocation);
          
//        System.err.println("Returning result = " + new FindResult(doc, matchLocation, wrapped, allWrapped));
          
          return new FindResult(doc, matchLocation, wrapped, allWrapped);  // return valid match
        }
      }
      catch (BadLocationException e) { throw new UnexpectedException(e); }
    }      
    // loop fell through; search failed in doc segment
    return new FindResult(doc, -1, wrapped, allWrapped);
  }
  
  /** Searches all documents following startDoc for _findWord, cycling through the documents in the direction specified
    * by _isForward. If the search cycles back to doc without finding a match, performs a wrapped search on doc.
    * @param startDoc  document where searching started and just failed
    * @param start  location in startDoc of the document segment where search failed.
    * @param len  length of the text segment where search failed.
    * @return the FindResult containing the information for where we found _findWord or a dummy FindResult.
    */
  private FindResult _findNextInOtherDocs(final OpenDefinitionsDocument startDoc, int start, int len) {
    
//    System.err.println("_findNextInOtherDocs(" + startDoc.getText() + ", " + start + ", " + len + ")");
    
    boolean allWrapped = false;
    // _doc may be null if the next document isn't found and the user didn't want to continue!
    _doc = _isForward ? _docIterator.getNextDocument(startDoc) : _docIterator.getPrevDocument(startDoc);
    if (_doc == null) return new FindResult(startDoc, -1, true, true);
    
    while (_doc != startDoc) {
      if (_doc == _firstDoc) allWrapped = true;
      boolean inTestCase = (_doc.getFileName().endsWith("Test.scala"));
      
      if (! _ignoreTestCases || ! inTestCase) {
//      System.err.println("_doc = [" + _doc.getText() + "]");
        
//      if (_isForward) setPosition(0);
//      else setPosition(_doc.getLength());
        
        
        // find next match in _doc
        FindResult fr;
        fr = _findNextInDocSegment(_doc, 0, _doc.getLength(), false, allWrapped); 
        
        if (fr.getFoundOffset() >= 0) return fr;
      }
//      System.err.println("Advancing from '" + _doc.getText() + "' to next doc");        
      // _doc may be null if the next document isn't found and the user didn't want to continue!
      _doc = _isForward ? _docIterator.getNextDocument(_doc) : _docIterator.getPrevDocument(_doc);     
      if (_doc == null) return new FindResult(startDoc, -1, true, true);
//      System.err.println("Next doc is: '" + _doc.getText() + "'");
    }
    
    // No valid match found; perform wrapped search.  Only runs in event thread.
    return _findWrapped(startDoc, start, len, true);  // last arg is true because searching all docs has wrapped
  } 
  
  /** Determines whether the whole find word is found at the input position.  Only called in event thread or when 
    * hourglass is held.
    * @param doc - the document where an instance of the find word was found
    * @param foundOffset - the position where that instance was found
    * @return true if the whole word is found at foundOffset, false otherwise
    */
  private boolean wholeWordFoundAtCurrent(OpenDefinitionsDocument doc, int foundOffset) {    
    
    char leftOfMatch = 0;   //  forced initialization
    char rightOfMatch = 0;  //  forced initialization
    int leftLoc = foundOffset - 1;
    int rightLoc = foundOffset + _findWord.length();
    boolean leftOutOfBounds = false;
    boolean rightOutOfBounds = false;
    
    try { leftOfMatch = doc.getText(leftLoc, 1).charAt(0); }
    catch (BadLocationException e) { leftOutOfBounds = true; } 
    catch (IndexOutOfBoundsException e) { leftOutOfBounds = true; }
    try { rightOfMatch = doc.getText(rightLoc, 1).charAt(0); }
    catch (BadLocationException e) { rightOutOfBounds = true; } 
    catch (IndexOutOfBoundsException e) { rightOutOfBounds = true; }    
    
    if (! leftOutOfBounds && ! rightOutOfBounds) return isDelimiter(rightOfMatch) && isDelimiter(leftOfMatch);
    if (! leftOutOfBounds) return isDelimiter(leftOfMatch);
    if (! rightOutOfBounds) return isDelimiter(rightOfMatch);
    return true;
  }
  
  /** Determines whether a character is a delimiter (not a letter or digit) as a helper to wholeWordFoundAtCurrent
    * 
    * @param ch - a character
    * @return true if ch is a delimiter, false otherwise
    */
  private boolean isDelimiter(char ch) { return ! Character.isLetterOrDigit(ch)  &&  ch != '_'; }
  
  /** Returns true if the currently found instance should be ignored (either because it is inside a string or comment or
    * because it does not match the whole word when either or both of those conditions are set to true).  Only executes 
    * in event thread.
    * @param foundOffset the location of the instance found
    * @param odd the current document where the instance was found
    * @return true if the location should be ignored, false otherwise
    */
  private boolean _shouldIgnore(int foundOffset, OpenDefinitionsDocument odd) {
    
    assert EventQueue.isDispatchThread();

    return (_matchWholeWord && ! wholeWordFoundAtCurrent(odd, foundOffset)) || 
      (_ignoreCommentsAndStrings && odd.isShadowed(foundOffset));
  }
}
