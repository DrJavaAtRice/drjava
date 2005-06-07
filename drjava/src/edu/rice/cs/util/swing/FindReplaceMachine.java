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

package edu.rice.cs.util.swing;

import edu.rice.cs.util.UnexpectedException;

import edu.rice.cs.util.text.AbstractDocumentInterface;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position;

/**
 * Implementation of logic of find/replace over a document.
 *
 * @version $Id$
 */
public class FindReplaceMachine {
  
 /** The document on which FindReplaceMachine is operating. */
  static AbstractDocumentInterface _doc;
  /** The position in _doc from which the searches started. */
  static Position _start;
  /** The position in _doc which the machine is currently at. */
  static Position _current;
  /** The word to find. */
  static String _findWord;
  /** The word to replace _findword. */
  static String _replaceWord;
  static boolean _found;
  static boolean _wrapped;
  static boolean _matchCase;
  static boolean _matchWholeWord;
  static boolean _searchBackwards;
  static boolean _searchAllDocuments;
  static boolean _ignoreCommentsAndStrings;
  // The last word that was found. This is set to null by the
  // FindReplaceDialog if the caret is updated. We keep this
  // so we know to ignore finding this instance of the word
  // if the user toggles the _searchBackwards flag and has not
  // moved the caret.
  static String _lastFindWord;
  // Set to true if we should skip the first instance of the
  // word that we find (if the user toggled _searchBackwards
  // under certain circumstances).
  static boolean _skipOneFind;
  // An interface for the FindReplaceMachine to get the
  // next or previous document.
  DocumentIterator _docIterator;
  
  
  /** Standard Constructor.
   *  Creates a new machine to perform find/replace operations on a particular document starting from a 
   *  certain position.
   *  @param docIterator an object that allows navigation through open Swing documents (it is DefaultGlobalModel)
   *  @exception BadLocationException
   */
  public FindReplaceMachine(DocumentIterator docIterator) {    
    _skipOneFind = false;
    _docIterator = docIterator;
    setFindAnyOccurrence();
    setFindWord("");
    setReplaceWord("");
    setSearchBackwards(false);
    setMatchCase(true);
    setSearchAllDocuments(false);
    setIgnoreCommentsAndStrings(false);
    
  }
  
  /**
   * Called when the current position is updated in the document
   * and therefore we do not want to skip the instance of the
   * findWord we are on if the user toggles _searchBackwards
   */
  public void positionChanged() {
    _lastFindWord = null;
    _skipOneFind = false;
  }

  public void setLastFindWord() { _lastFindWord = _findWord; }

  public boolean getSearchBackwards() { return _searchBackwards; }

  public void setSearchBackwards(boolean searchBackwards) {
    if (_searchBackwards != searchBackwards) {
      // If we switch from searching forward to searching backwards or viceversa,
      // isOnMatch is true, and _findword is the same as the _lastFindWord,
      // we know the user just found _findWord, so skip one find.
      if (isOnMatch() && _findWord.equals(_lastFindWord))  _skipOneFind = true;
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

  public void setPosition(int pos) {
    try { _current = _doc.createPosition(pos); }
    catch (BadLocationException ble) { throw new UnexpectedException(ble); }
  }

  public void setStart(int pos) {
    try {
      _start = _doc.createPosition(pos);
      _found = false;
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
    if (off + len > _doc.getLength()) return false;

    try {
      String matchSpace = _doc.getText(off, len);
      if (!_matchCase) {
        matchSpace = matchSpace.toLowerCase();
        findWord = findWord.toLowerCase();
      }
      return matchSpace.equals(findWord);
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  
  /** If we're on a match for the find word, replace it with the replace word. */
  public boolean replaceCurrent() {
    _doc.acquireWriteLock();
    try {
      if (isOnMatch()) {
        boolean atStart = false;
        int position = getCurrentOffset();
        if (!_searchBackwards) position -= _findWord.length();
        _doc.remove(position, _findWord.length());
        if (position == 0) atStart = true;
        _doc.insertString(getCurrentOffset(), _replaceWord, null);

        // the current offset will be the end of the inserted word
        //since we keep track of current as a Position.
        //The exception is if we are at the beginning of the document,
        //in which case the text is inserted AFTER the current position
        //So, current offset is correct for forwards searching unless
        //we were at the start of the document, in which case it is
        //correct for backwards searching.
        if (atStart && !_searchBackwards) setPosition(_replaceWord.length());
        else if (!atStart && _searchBackwards) setPosition(getCurrentOffset() - _replaceWord.length());

        return true;
      }
      return false;
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
//    int found = fr.getFoundOffset(); // This is never used
//    int wrapped = 0;
//    if (fr.getWrapped())
//      wrapped++;
// Checks that the findNext method has found something and has not
// wrapped once and gone beyond start.
//    while (found >= 0 && (wrapped == 0 ||
//                         ((found < _start.getOffset() + _findWord.length() && !_searchBackwards)  ||
//                          (found > _start.getOffset() - _findWord.length() && _searchBackwards))) && wrapped < 2) {

//new while condition, since I started replacing from the beginning/end of the document only
    while (!fr.getWrapped()) {// wrapped == 0) {
      replaceCurrent();
      count++;
      fr = findNext();
      _doc = fr.getDocument();
//      found = fr.getFoundOffset();
      //      if (fr.getWrapped())
      //        wrapped++;
    }
    return count;
  }
  

  /**
   * Finds the next occurrence of the find word and returns an
   * offset at the end of that occurrence or -1 if the word was
   * not found.  Selectors should select backwards the length of
   * the find word from the find offset.  This position is stored
   * in the current offset of the machine, and that is why it is
   * after: in subsequent searches, the same instance won't be found
   * twice.  In a backward search, the position returned is at the
   * beginning of the word.  Also returns a flag indicating whether the
   * end of the document was reached and wrapped around. This is done
   * using  the FindResult class which just contains an integer and a
   * flag.
   *
   * @return a FindResult object containing foundOffset and aflag
   *         indicating wrapping to the beginning during a search
   */
  public FindResult findNext() {
//    Utilities.showDebug("DEBUG: findNext() in FindAnyOccurrenceState called");
    
    // If the user just found and toggled the "Search Backwards"
    // option, we should skip the first find.
    if (_skipOneFind) {
//      Utilities.showDebug("DEBUG: We should skip one");
      int wordLength = _lastFindWord.length();
      if (!_searchBackwards) setPosition(getCurrentOffset() + wordLength);
      else setPosition(getCurrentOffset() - wordLength);
      positionChanged();
    }
    if (!_searchBackwards) return _findNext(_current.getOffset(), _doc.getLength()-_current.getOffset());
    return _findNext(0, _current.getOffset());
  }        
  
  
  /** Finds the next occurrence of the find word and returns an offset at the end of that occurrence or -1
   *  if the word was not found.  Selectors should select backwards the length of the find word from the 
   *  find offset.  This position is stored in the current offset of the machine, and that is why it is
   *  after: in subsequent searches, the same instance won't be found twice.  In a backward search, the 
   *  position returned is at the beginning of the word.  Also returns a flag indicating whether the end of 
   *  the document was reached and wrapped around. This is done using  the FindResult class which just 
   *  contains an integer and a flag.
   * 
   *  @return a FindResult object with foundOffset and a flag indicating wrapping to the beginning during a search
   */
  private FindResult _findNext(int start, int end) {
//    Utilities.showDebug("DEBUG: Inside _findNext()");
    try {
      FindResult tempFr = new FindResult(_doc, -1, false);      
      int /*len,*/ docLen;
      String findWord = _findWord;
      // get the search space in the document
      String findSpace;
//      Utilities.showDebug("DEBUG: Acquiring the lock on document");
      _doc.acquireReadLock();
//      Utilities.showDebug("DEBUG: Lock acquired on document");
      try {
        docLen = _doc.getLength();
//        Utilities.showDebug("DEBUG: Length of document: " + docLen);
//        if (!_searchBackwards) {
//          end = docLen - start;
        findSpace = _doc.getText(start, end);
//        }  
//        else findSpace = _doc.getText(start, end);        
        
      }
      finally { _doc.releaseReadLock(); }
//      Utilities.showDebug("DEBUG: Lock released");
      
      if (!_matchCase) {
//        Utilities.showDebug("DEBUG: We shouldn't match the case");
        findSpace = findSpace.toLowerCase();
        findWord = findWord.toLowerCase();
      }
      
      // find the first occurrence of findWord
      int foundOffset;
      foundOffset = !_searchBackwards ? findSpace.indexOf(findWord)
        : findSpace.lastIndexOf(findWord);
      
//      Utilities.showDebug("DEBUG: foundOffset = " + foundOffset);
      
      if (foundOffset >= 0) {
//        Utilities.showDebug("DEBUG: Offset is greater than 0");
        // the found location is inside a comment or a string that the user has asked to ignore
        int locationToIgnore = foundOffset + start;
        if (_shouldIgnore(locationToIgnore, _doc)) {
//          Utilities.showDebug("DEBUG: We are at a position to be ignored");
          foundOffset += start;
          if (!_searchBackwards) {
            foundOffset += findWord.length();
            return _findNext(foundOffset, docLen-foundOffset);
          }
          return _findNext(start, foundOffset); //searching backwards
        }       
        // otherwise we have found it
//        Utilities.showDebug("DEBUG: We don't have to ignore anything, and we have found the word!");
        _found = true;
        foundOffset += start;
        if (!_searchBackwards) foundOffset += findWord.length();
        _current = _doc.createPosition(foundOffset);  // thread-safe operation on _doc
      }
      else { // we haven't found it yet
//        Utilities.showDebug("DEBUG: We didn't find it on the first pass.");
        if (_searchAllDocuments) {
//          Utilities.showDebug("DEBUG: Searching the rest of the documents");
          AbstractDocumentInterface nextDocToSearch = (!_searchBackwards ? _docIterator.getNextDocument(_doc) :
                                                         _docIterator.getPrevDocument(_doc));
          
          tempFr = _findNextInAllDocs(nextDocToSearch, 0, nextDocToSearch.getLength());
          foundOffset = tempFr.getFoundOffset();
        }
        if (foundOffset == -1) {   // we still haven't found it 
//          Utilities.showDebug("DEBUG: Search has wrapped");
          if (!_searchBackwards) foundOffset = _findWrapped(0, _current.getOffset() + (_findWord.length() -1));
          else {
            int startBackOffset = _current.getOffset() - (_findWord.length() - 1);
            foundOffset = _findWrapped(startBackOffset, docLen - startBackOffset);
          }
//          Utilities.showDebug("DEBUG: foundOffset = " + foundOffset);
        }
      }
      // flag the return value so that they can tell that we had to wrap
      // the file to determine the info.
      
      //This means we have found the word before, just not in this call
      //      if(foundOffset == -1 && _found) {
      //        _current = _start;
      //        _found = false;
      //        return findNext();
      //      }
      //      else {
      FindResult fr = new FindResult(tempFr.getDocument(), foundOffset, _wrapped);
      _wrapped = false;
      return fr;
      //      }
    }
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  /** Helper method for finding after it has wrapped*/
  private int _findWrapped(int start, int end) {
    try{
      _wrapped = true;
      //When we wrap, we need to include some text that was already searched before wrapping.
      //Otherwise, we won't find an only match that has the caret in it already.
      int docLen;
      String findSpace;
      
//      Utilities.showDebug("DEBUG: Acquiring wrapped lock");
      _doc.acquireReadLock();  
//      Utilities.showDebug("DEBUG: Lock acquired");
      try { 
        docLen = _doc.getLength(); 
//        Utilities.showDebug("DEBUG: Length of doc = " + docLen);
        if (!_searchBackwards) {
//          end = _current.getOffset() + (_findWord.length() - 1);
          if (end > docLen) end = docLen;
        }
        
        //combine all these if/elses into single, becasue took away commented stuff 
        
        else {  // searching backwards
          if (start < 0){ 
            start = 0;
            end = docLen;
          }
        }

//        Utilities.showDebug("DEBUG: start =  " + start);
//        Utilities.showDebug("DEBUG: end = " + end);
        
        findSpace = _doc.getText(start, end);
//        Utilities.showDebug("DEBUG: Text to be used is = " + findSpace);
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
        // the found location is inside a comment or a string that the user has asked to ignore
        int locationToIgnore = start + foundOffset;
        if (_shouldIgnore(locationToIgnore, _doc)) {
          foundOffset += start;
          if (!_searchBackwards) {
            foundOffset += findWord.length();
            return _findWrapped(foundOffset, docLen-foundOffset);
          }
          return _findWrapped(start, foundOffset-start);
        }       
        // otherwise we have found it
        _found = true;
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
    
    while (docToSearch != _doc) {
      String text;
      int docLen;
//      Utilities.showDebug("DEBUG: Acquiring other documents lock");
      docToSearch.acquireReadLock();
//      Utilities.showDebug("DEBUG: Lock acquired");
      try { 
//        Utilities.showDebug("DEBUG: startPos = " + start);
//        Utilities.showDebug("DEBUG: end = " + end);
        docLen = docToSearch.getLength();
        if (!_searchBackwards) text = docToSearch.getText(start, end /*docToSearch.getLength() - start*/);
        else text = docToSearch.getText(start, end);
      }
      finally { docToSearch.releaseReadLock(); }
//      Utilities.showDebug("DEBUG: text = " + text);
      String findWord = _findWord;
      if (!_matchCase) {
        text = text.toLowerCase();
        findWord = findWord.toLowerCase();
      }
      int foundOffset = !_searchBackwards ? text.indexOf(findWord) : text.lastIndexOf(findWord);
//      Utilities.showDebug("DEBUG: foundOffset = " + foundOffset);
      if (foundOffset >= 0) {
        // the found location is inside a comment or a string that the user has asked to ignore
        int locationToIgnore = start + foundOffset;
        if (_shouldIgnore(locationToIgnore, docToSearch)) {
//          Utilities.showDebug("DEBUG: We are in a position to be ignored in another document");
          foundOffset += start;
          if (!_searchBackwards) {
            foundOffset += findWord.length();
            return _findNextInAllDocs(docToSearch, foundOffset, docLen-foundOffset);
          }
          return _findNextInAllDocs(docToSearch, start, foundOffset-start);
        }       
      
        // We found it in a different document, put the caret at the end of the
        // found word (if we're going forward).
        foundOffset += start;
        if (!_searchBackwards) foundOffset += findWord.length();
        return new FindResult(docToSearch, foundOffset, false);
      }
      docToSearch = !_searchBackwards ? _docIterator.getNextDocument(docToSearch) :
                                        _docIterator.getPrevDocument(docToSearch);
      start = 0;
      end = docToSearch.getLength();
    }
    return new FindResult(docToSearch, -1, false);
  } 
  
  
  /** Determines whether the whole find word is found at the input position
   * 
   * @param doc - the document where an instance of the find word was found
   * @param foundOffset - the position where that instance was found
   * @return true if the whole word is found at foundOffset, false otherwise
   */
  private boolean wholeWordFoundAtCurrent(AbstractDocumentInterface doc, int foundOffset) {    
    try {
      String docText;
      doc.acquireReadLock();
      try {
        docText = doc.getText(0, doc.getLength());
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
    catch (BadLocationException e) { throw new UnexpectedException(e); }
  }

  /** Determines whether a character is a delimiter (not a letter or digit) as a helper to wholeWordFoundAtCurrent
   * 
   * @param ch - a character
   * @return true if ch is a delimiter, false otherwise
   */
  private boolean isDelimiter(Character ch) {
    return !Character.isLetterOrDigit(ch.charValue());
  }
  
  /** Returns true if comments and Strings should be ignored, false otherwise*/
  private boolean _shouldIgnore(int foundOffset, AbstractDocumentInterface doc) {
    try{
//      Utilities.showDebug("DEBUG: _shouldIgnore called");
      doc.acquireReadLock();
      String docText;
      try{
        docText = doc.getText(0, foundOffset);
      }
      finally{doc.releaseReadLock();}
//      Utilities.showDebug("DEBUG: docText acquired: " + docText);
    
      return ((_matchWholeWord && !wholeWordFoundAtCurrent(doc, foundOffset)) 
                || (_ignoreCommentsAndStrings && (_insideBlockComment(docText) || _insideLineComment(docText) 
                                                    || _insideString(docText) || _insideChar(docText))));
    }
    catch(BadLocationException e) { throw new UnexpectedException(e); }
  }
  
  
  
  /** Checks whether the offset input is within a block comment. Haven't implemented the case where a comment "brace" 
   * (i.e /* or  (star)/ ) is within a string */ 
  private boolean _insideBlockComment (String docText) {
//    Utilities.showDebug("DEBUG: _insideBlockComment called");
    int previousOpenCommentOffset = docText.lastIndexOf("/*");
    int previousCloseCommentOffset = docText.lastIndexOf("*/");
    
    // the following code ensures the following correct behavior:
    //   - a "/*" contained inside a String should not be registered as opening a comment
    //   - a "*/" contained inside a String should not be registered as closing a comment
    //
    //   - a "/*" contained inside character delimiters (i.e. ') should not be registered as opening a comment
    //   - a "*/" contained inside character delimiters (i.e. ') should not be registered as closing a comment
    //      --> even though the compiler counts these 2 as errors, finding does not depend on the compiler so must check
    String tempText = docText;
    
    if (previousOpenCommentOffset != -1) tempText = tempText.substring(0, previousOpenCommentOffset);
    
    while(previousOpenCommentOffset != -1 && ((_insideString(tempText)) || _insideChar(tempText))) { 
      previousOpenCommentOffset = tempText.lastIndexOf("/*");
      if (previousOpenCommentOffset != -1) tempText = tempText.substring(0, previousOpenCommentOffset);
    }
    
    tempText = docText;
    if (previousCloseCommentOffset != -1) tempText = tempText.substring(0, previousCloseCommentOffset);
    
    while(previousCloseCommentOffset != -1 && ((_insideString(tempText)) || _insideChar(tempText))) { 
      previousCloseCommentOffset = tempText.lastIndexOf("*/");
      if (previousCloseCommentOffset != -1) tempText = tempText.substring(0, previousCloseCommentOffset);
    }
    
//    Utilities.showDebug("DEBUG: value of previousOpenCommentOffset = " +  previousOpenCommentOffset);
//    Utilities.showDebug("DEBUG: value of previousCloseCommentOffset = " +  previousCloseCommentOffset);
    
    if (previousCloseCommentOffset < previousOpenCommentOffset) return true;
    return false;
  }
  
  /** Checks whether the offset input is within a line comment. Haven't implemented the case where a comment "brace" 
   * (//) is within a string */
  private boolean _insideLineComment (String docText) {
//    Utilities.showDebug("DEBUG: _insideLineComment called");
    int previousNewLineOffset = docText.lastIndexOf("\n");
    int previousOpenCommentOffset = docText.lastIndexOf("//");    

    //the following code ensures the following correct behaviors:
    //   - a "//" contained inside a String should not be registered as opening a comment
    //   - a "//" contained inside a block comment should not be registered as opening a comment
    //   - a "//" contained inside character delimiters (i.e. ') should not be redistered as opening a comment
    //      --> even though the compiler counts this as illegal, finding does not depend on the compiler so must check
    if (previousOpenCommentOffset != -1) docText = docText.substring(0, previousOpenCommentOffset);
    
    while(previousOpenCommentOffset != -1 && ((_insideString(docText)) || _insideChar(docText) || _insideBlockComment(docText))) { 
      previousOpenCommentOffset = docText.lastIndexOf("//");
      if (previousOpenCommentOffset != -1) docText = docText.substring(0, previousOpenCommentOffset);
    }
    

//    Utilities.showDebug("DEBUG: value of previousOpenCommentOffset = " +  previousOpenCommentOffset);
//    Utilities.showDebug("DEBUG: value of previousNewLineOffset = " +  previousNewLineOffset);
    
    if (previousNewLineOffset < previousOpenCommentOffset) return true;
    return false;
  }
      
  /** Checks whether the offset input is within a string. Haven't implemented the case where an unpaired double quote
   * is within a comment on the same line as the string*/
  private boolean _insideString(String docText) {
//    Utilities.showDebug("DEBUG: _insideString called");  
    int previousNewLineOffset = docText.lastIndexOf("\n");

//    Utilities.showDebug("DEBUG: value of previousNewLineOffset = " +  previousNewLineOffset);    
    
    String tempText = docText;
    
    if (previousNewLineOffset != -1) docText = docText.substring(previousNewLineOffset+1);
    int doubleQuoteCount = 0;
    int doubleQuoteIndex = docText.lastIndexOf("\"");
    
    if (doubleQuoteIndex != -1) 
      tempText = tempText.substring(0, doubleQuoteIndex + (tempText.length() - docText.length()));
    
    while (doubleQuoteIndex != -1) { 
      // the conditional below ensures the following correct behavior:
      //     - a quotation mark inside a block comment should not count as opening or closing a string
      //     - a quotation mark inside character delimeters (i.e ') should not count as opening or closing a string
      if (!_insideBlockComment(tempText) && !_insideChar(tempText))
        doubleQuoteCount++;
      docText = docText.substring(0, doubleQuoteIndex); 
      doubleQuoteIndex = docText.lastIndexOf("\"");
      if (doubleQuoteIndex != -1) 
        tempText = tempText.substring(0, doubleQuoteIndex + (tempText.length() - docText.length()));
    }
    
    if (doubleQuoteCount % 2 == 1) return true;
    return false;
  }
  
  
  /** Checks whether the offset input is within a char. */
  private boolean _insideChar(String docText) {
//    Utilities.showDebug("DEBUG: _insideChar called");
    int singleQuoteIndex = docText.lastIndexOf("\'");
    if (singleQuoteIndex != -1 && singleQuoteIndex == docText.length()-1)
      return true;    
    return false;
  } 
}