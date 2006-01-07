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
import edu.rice.cs.util.text.AbstractDocumentInterface;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/** Implementation of logic of find/replace over a document.
 *  @version $Id$
 */
public class FindReplaceMachine {
  /** The document on which FindReplaceMachine is operating. */
  private AbstractDocumentInterface _doc;
  /** The first document that the current search was executed on (used for wrapping around all docs). */
  private AbstractDocumentInterface _firstDoc;
  /** The position in _doc from which the searches started. */
  private Position _start;
  /** The position in _doc which the machine is currently at. */
  private Position _current;
  /** The word to find. */
  private String _findWord;
  /** The word to replace _findword. */
  private String _replaceWord;
//  private boolean _found;
  private boolean _wrapped;
  private boolean _allDocsWrapped;
  private boolean _checkAllDocsWrapped;
  private boolean _matchCase;
  private boolean _matchWholeWord;
  private boolean _searchBackwards;
  private boolean _searchAllDocuments;
  private boolean _ignoreCommentsAndStrings;
  // The last word that was found. This is set to null by the FindReplaceDialog if the caret is updated. We keep this
  // so we know to ignore finding this instance of the word if the user toggles the _searchBackwards flag and has not
  // moved the caret.
  private String _lastFindWord;
  // Set to true if we should skip the first instance of the word that we find (if the user toggled _searchBackwards
  // under certain circumstances).
  private boolean _skipOneFind;
  // An interface for the FindReplaceMachine to get the next or previous document.
  private DocumentIterator _docIterator;
  // The model currently being used. Used to get the ReducedModelState
  private SingleDisplayModel _model;
  
  /** Standard Constructor.
   *  Creates new machine to perform find/replace operations on a particular document starting from a given position.
   *  @param docIterator an object that allows navigation through open Swing documents (it is DefaultGlobalModel)
   *  @exception BadLocationException
   */
  public FindReplaceMachine(SingleDisplayModel model, DocumentIterator docIterator) {    
    _skipOneFind = false;
    _checkAllDocsWrapped = false;
    _allDocsWrapped = false;
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
  
  /** Called when the current position is updated in the document and therefore we do not want to skip the instance of
   *  the findWord we are on if the user toggles _searchBackwards
   */
  public void positionChanged() {
    _lastFindWord = null;
    _skipOneFind = false;
  }

  public void setLastFindWord() { _lastFindWord = _findWord; }

  public boolean getSearchBackwards() { return _searchBackwards; }

  public void setSearchBackwards(boolean searchBackwards) {
    if (_searchBackwards != searchBackwards) {
      // If we switch from searching forward to searching backwards or viceversa, isOnMatch is true, and _findword is the
      // same as the _lastFindWord, we know the user just found _findWord, so skip one find.
      if (isOnMatch() && _findWord.equals(_lastFindWord)) _skipOneFind = true;
      else _skipOneFind = false;
    }
    _searchBackwards = searchBackwards;
  }

  public void setMatchCase(boolean matchCase) { _matchCase = matchCase; }
  
  public void setMatchWholeWord() { _matchWholeWord = true; }
  
  public void setFindAnyOccurrence() { _matchWholeWord = false; }  

  public void setSearchAllDocuments(boolean searchAllDocuments) { _searchAllDocuments = searchAllDocuments; }
  
  public void setIgnoreCommentsAndStrings(boolean ignoreCommentsAndStrings) {
    _ignoreCommentsAndStrings = ignoreCommentsAndStrings;
  }

  public void setDocument(AbstractDocumentInterface doc) { _doc = doc; }
  
  public void setFirstDoc(AbstractDocumentInterface firstDoc) { _firstDoc = firstDoc; }
 
  public void setPosition(int pos) {
    try { _current = _doc.createPosition(pos); }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }

  public void setStart(int pos) {
    try {
      _start = _doc.createPosition(pos);
//      _found = false;
      _wrapped = false;
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }

  /** Gets the character offset at which this machine started operations. */
  public int getStartOffset() { return _start.getOffset(); }

  /** Gets the character offset to which this machine is currently pointing. */
  public int getCurrentOffset() { return _current.getOffset(); }

  public void makeCurrentOffsetStart() {
    try { _start = _doc.createPosition(getCurrentOffset()); }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }

  public String getFindWord() { return _findWord; }

  public String getReplaceWord() { return _replaceWord; }

  public boolean getSearchAllDocuments() { return _searchAllDocuments; }

  public AbstractDocumentInterface getDocument() { return _doc; }
  
  public AbstractDocumentInterface getFirstDoc() { return _firstDoc; }

  /** Change the word being sought.
   *  @param word the new word to seek
   */
  public void setFindWord(String word) { _findWord = word; }

  /** Change the replacing word.
   *  @param word the new replacing word
   */
  public void setReplaceWord(String word) { _replaceWord = word; }

  /** Determine if the machine is on an instance of the find word.
   *  @return true if the current position is right after an instance of the find word.
   */
  public boolean isOnMatch() {
    String findWord = _findWord;
    int len, off;
    
    if(_current == null) return false;
    
    len = findWord.length();
    if (!_searchBackwards) off = _current.getOffset() - len;
    else off = _current.getOffset();

    if (off < 0) return false;
    
     String matchSpace;
    _doc.acquireReadLock();
    try {
      if (off + len > _doc.getLength()) return false;
      matchSpace = _doc.getText(off, len);
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

    if (! isOnMatch()) return false;
    _doc.acquireWriteLock();
    try {
      boolean atStart = false;
      int position = getCurrentOffset();
      if (!_searchBackwards) position -= _findWord.length();
      _doc.remove(position, _findWord.length());
      if (position == 0) atStart = true;
      _doc.insertString(getCurrentOffset(), _replaceWord, null);
      
      // The current offset will be the end of the inserted word since we keep track of current as a Position. The 
      // exception is if we are at the beginning of the document, in which case the text is inserted AFTER the current 
      // position.  So, current offset is correct for forwards searching unless we were at the start of the document, 
      // in which case it is correct for backwards searching.
      if (atStart && !_searchBackwards) setPosition(_replaceWord.length());
      else if (! atStart && _searchBackwards) setPosition(getCurrentOffset() - _replaceWord.length());
      
      return true;
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    finally { _doc.releaseWriteLock(); }
  }

  /** Replaces all occurences of the find word with the replace word. Checks to see if the entire document is
   *  searched in case the find word is equivalent to the replace word in which case an infinite loop would 
   *  otherwise occur.  Starts at the beginning or the end of the document (depending on find direction).  
   *  This convention ensures that matches created by string replacement will not be replaced as in the 
   *  following example:<p>
   *    findString:    "hello"
   *    replaceString: "e"
   *    document text: "hhellollo"<p>
   *  Depending on the cursor position, clicking replace all could either make the document text read "hello" 
   *  (which is correct) or "e".  This is because of the behavior of findNext(), and it would be incorrect
   *  to change that behavior.
   *  @return the number of replacements
   */
  public int replaceAll() {
    if (_searchAllDocuments) {
      AbstractDocumentInterface startDoc = _doc;
      _searchAllDocuments = false;
      // replace all in the first document
      int count = _replaceAllInCurrentDoc();
      _doc = _docIterator.getNextDocument(_doc);
      int n = _docIterator.getDocumentCount();
      for (int i=1; i < n; i++) {
        // replace all in the rest of the documents
        count += _replaceAllInCurrentDoc();
        _doc = _docIterator.getNextDocument(_doc);
      }
      _searchAllDocuments = true;
      return count;
    }
    else return _replaceAllInCurrentDoc();
  }
  
  /** Replaces all occurences of _findWord with _replaceWord in _doc. Never searches in other documents.
   *  @return the number of replacements
   */
  private int _replaceAllInCurrentDoc() {
    _doc.acquireReadLock();
    try {
      if (!_searchBackwards) {
        _start = _doc.createPosition(0);
        setPosition(0);
      }
      else {
        _start = _doc.createPosition(_doc.getLength());
        setPosition(_doc.getLength());
      }
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
    finally { _doc.releaseReadLock(); }
    int count = 0;
    FindResult fr = findNext();
    _doc = fr.getDocument();

    while (!fr.getWrapped()) {
      replaceCurrent();
      count++;
      fr = findNext();
      _doc = fr.getDocument();

    }
    return count;
  }

  /** Finds the next occurrence of the find word and returns an offset at the end of that occurrence or -1 if the word
   *  was not found.  Selectors should select backwards the length of the find word from the find offset.  This 
   *  position is stored in the current offset of the machine, and that is why it is after: in subsequent searches, the
   *  same instance won't be found twice.  In a backward search, the position returned is at the beginning of the word.  
   *  Also returns a flag indicating whether the end of the document was reached and wrapped around. This is done
   *  using the FindResult class which just contains an integer and a flag.
   *
   *  @return a FindResult object containing foundOffset and a flag indicating wrapping to the beginning during a search
   */
  public FindResult findNext() {    
    // If the user just found and toggled the "Search Backwards"
    // option, we should skip the first find.
    if (_skipOneFind) {
      int wordLength = _lastFindWord.length();
      if (!_searchBackwards) setPosition(getCurrentOffset() + wordLength);
      else setPosition(getCurrentOffset() - wordLength);
      positionChanged();
    }
    if (!_searchBackwards) return _findNext(_current.getOffset(), _doc.getLength()-_current.getOffset());
    return _findNext(0, _current.getOffset());
  }        
  
  
  /** Helper method for findNext that searches for _findWord inside a document. If necessary (i.e. an instance wasn't
   *  found), it delegates to _findNextInAllDocuments (if searching through all open documents) and/or to _findWrapped.
   * 
   *  @param start the location to begin searching in the document
   *  @param end the location to end searching in the document
   *  @return a FindResult object with foundOffset and a flag indicating wrapping to the beginning during a search. The
   *  foundOffset returned insided the FindResult is -1 if no instance was found.
   */
  private FindResult _findNext(int start, int end) {
    try {
//      Utilities.showDebug(""+ _model.getODDForDocument(_doc).getStateAtCurrent());
      FindResult tempFr = new FindResult(_doc, -1, false, false);      
      int docLen;
      String findWord = _findWord;
      // get the search space in the document
      String findSpace;
      _doc.acquireReadLock();
      try {
        docLen = _doc.getLength();
        findSpace = _doc.getText(start, end);
      }
      finally { _doc.releaseReadLock(); }
      
      if (!_matchCase) {
        findSpace = findSpace.toLowerCase();
        findWord = findWord.toLowerCase();
      }
      
      // find the first occurrence of findWord
      int foundOffset;
      foundOffset = !_searchBackwards ? findSpace.indexOf(findWord) : findSpace.lastIndexOf(findWord);
          
      if (foundOffset >= 0) {
        int locationToIgnore = foundOffset + start;
        _model.getODDForDocument(_doc).setCurrentLocation(locationToIgnore);
        if (_shouldIgnore(locationToIgnore, _doc)) {
          foundOffset += start;
          if (!_searchBackwards) {
            foundOffset += findWord.length();
            return _findNext(foundOffset, docLen-foundOffset);
          }
          return _findNext(start, foundOffset); //searching backwards
        }       
        // otherwise we have found it
//        _found = true;
        foundOffset += start;
        if (!_searchBackwards) foundOffset += findWord.length();
        _current = _doc.createPosition(foundOffset);  // thread-safe operation on _doc
      }
      else { // we haven't found it yet
        if (_searchAllDocuments) {
          AbstractDocumentInterface nextDocToSearch;
          
          nextDocToSearch = (!_searchBackwards ? _docIterator.getNextDocument(_doc) :
                                                 _docIterator.getPrevDocument(_doc));
          
          tempFr = _findNextInAllDocs(nextDocToSearch, 0, nextDocToSearch.getLength());
          foundOffset = tempFr.getFoundOffset();
        }
        else { 
          _checkAllDocsWrapped = false;
          _allDocsWrapped = false;
        }
        
        if (foundOffset == -1) {   // we still haven't found it            
          if (!_searchBackwards) foundOffset = _findWrapped(0, _current.getOffset() + (_findWord.length() - 1));
          else {
            int startBackOffset = _current.getOffset() - (_findWord.length() - 1);
            foundOffset = _findWrapped(startBackOffset, docLen - startBackOffset);
          }
        }
      }
      
      if (_checkAllDocsWrapped && tempFr.getDocument() == _firstDoc) {
        _allDocsWrapped = true;
        _checkAllDocsWrapped = false;
      }
      
      FindResult fr = new FindResult(tempFr.getDocument(), foundOffset, _wrapped, _allDocsWrapped);
      _wrapped = false;
      if (_allDocsWrapped = true) _allDocsWrapped = false;
      return fr;
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Helper method for findNext that searches whenever a search has wrapped to the beginning or end of the starting
   *  document.
   * 
   *  @param start the location to begin searching
   *  @param end the location to end searching
   *  @return the offset where the instance was found. Returns -1 if no instance was found between start and end
   */
  private int _findWrapped(int start, int end) {
    try{
      _wrapped = true;
      int docLen;
      String findSpace;
      
      _doc.acquireReadLock();  
      try { 
        docLen = _doc.getLength(); 
        if (!_searchBackwards) {
          if (end > docLen) end = docLen;
        }
              
        else {  // searching backwards
          if (start < 0){ 
            start = 0;
            end = docLen;
          }
        }
        findSpace = _doc.getText(start, end);
      }
      finally { _doc.releaseReadLock(); }
      
      String findWord = _findWord;
      
      if (!_matchCase) {
        findSpace = findSpace.toLowerCase();
        findWord = findWord.toLowerCase();
      }
      
      int foundOffset;
      foundOffset = !_searchBackwards ? findSpace.indexOf(findWord)
        : findSpace.lastIndexOf(findWord);
      
      if (foundOffset >= 0) {
        int locationToIgnore = start + foundOffset;
        _model.getODDForDocument(_doc).setCurrentLocation(locationToIgnore);
        if (_shouldIgnore(locationToIgnore, _doc)) {
          foundOffset += start;
          if (!_searchBackwards) {
            foundOffset += findWord.length();
            return _findWrapped(foundOffset, docLen-foundOffset);
          }
          return _findWrapped(start, foundOffset-start);
        }       
        // otherwise we have found it
//        _found = true;
        foundOffset += start;
        if (!_searchBackwards) foundOffset += findWord.length();
        _current = _doc.createPosition(foundOffset);  // thread-safe operation on _doc
      }
      return foundOffset;
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }   
  
  /** Searches docToSearch for _findWord, and continues cycling through the documents in the direction 
   *  specified by _searchBackwards. If the original _document is reached, we stop searching.
   *  @param docToSearch the document to search
   *  @return the FindResult containing the information for where we found _findWord or a dummy FindResult.
   */
  private FindResult _findNextInAllDocs(AbstractDocumentInterface docToSearch, int start, int end) throws BadLocationException {
    _checkAllDocsWrapped = true;    
    while (docToSearch != _doc) {
      if (docToSearch == _firstDoc) {
        _allDocsWrapped = true;
        _checkAllDocsWrapped = false;
      }
      
      String text;
      int docLen;
      docToSearch.acquireReadLock();
      try { 
        docLen = docToSearch.getLength();
        text = docToSearch.getText(start, end);
      }
      finally { docToSearch.releaseReadLock(); }
      String findWord = _findWord;
      if (!_matchCase) {
        text = text.toLowerCase();
        findWord = findWord.toLowerCase();
      }
      int foundOffset = !_searchBackwards ? text.indexOf(findWord) : text.lastIndexOf(findWord);
      if (foundOffset >= 0) {
        int locationToIgnore = start + foundOffset;
        _model.getODDForDocument(docToSearch).setCurrentLocation(locationToIgnore);
        if (_shouldIgnore(locationToIgnore, docToSearch)) {
          foundOffset += start;
          if (!_searchBackwards) {
            foundOffset += findWord.length();
            return _findNextInAllDocs(docToSearch, foundOffset, docLen-foundOffset);
          }
          return _findNextInAllDocs(docToSearch, start, foundOffset-start);
        }       
            
        // We found it in a different document, put the caret at the end of the found word (if we're going forward).
        foundOffset += start;
        if (!_searchBackwards) foundOffset += findWord.length();
        return new FindResult(docToSearch, foundOffset, false, _allDocsWrapped);
      }
      docToSearch = !_searchBackwards ? _docIterator.getNextDocument(docToSearch) :
                                        _docIterator.getPrevDocument(docToSearch);
      start = 0;
      end = docToSearch.getLength();
    }
    return new FindResult(docToSearch, -1, false, _allDocsWrapped);
  } 
  
  /** Determines whether the whole find word is found at the input position
   * 
   * @param doc - the document where an instance of the find word was found
   * @param foundOffset - the position where that instance was found
   * @return true if the whole word is found at foundOffset, false otherwise
   */
  private boolean wholeWordFoundAtCurrent(AbstractDocumentInterface doc, int foundOffset) {    
    String docText;
    doc.acquireReadLock();
    try {
      docText = doc.getText();
    }
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
  private boolean _shouldIgnore(int foundOffset, AbstractDocumentInterface doc) {
    try{
      doc.acquireReadLock();
      String docText;
      try{
        docText = doc.getText(0, foundOffset);
      }
      finally{doc.releaseReadLock();}
    
      return ((_matchWholeWord && !wholeWordFoundAtCurrent(doc, foundOffset)) || 
              (_ignoreCommentsAndStrings && 
               _model.getODDForDocument(doc).getStateAtCurrent() 
                 != ReducedModelStates.FREE));
    }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }
}