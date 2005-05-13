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

package edu.rice.cs.util.swing;

import edu.rice.cs.util.text.AbstractDocumentInterface;
import edu.rice.cs.util.UnexpectedException;

import javax.swing.text.BadLocationException;

/** State for finding any occurrences for the FindReplaceMachine.
 *  @version $Id$
 */
class FindAnyOccurrenceState extends AFindReplaceMachineState {

  public FindAnyOccurrenceState(DocumentIterator docIterator) {
    super(docIterator);
  }

  /** Finds the next occurrence of the find word and returns an offset at the end of that occurrence or -1
   *  if the word was not found.  Selectors should select backwards the length of the find word from the 
   *  find offset.  This position is stored in the current offset of the machine, and that is why it is
   *  after: in subsequent searches, the same instance won't be found twice.  In a backward search, the 
   *  position returned is at the beginning of the word.  Also returns a flag indicating whether the end of 
   *  the document was reached and wrapped around. This is done using  the FindResult class which just 
   *  contains an integer and a flag.
   *  @return a FindResult object with foundOffset and a flag indicating wrapping to the beginning during a search
   */
  public FindResult findNext() {
   
    try {
      FindResult tempFr = new FindResult(_doc, -1, false);
      // If the user just found and toggled the "Search Backwards"
      // option, we should skip the first find.
      if (_skipOneFind) {
        int wordLength = _lastFindWord.length();
        if (!_searchBackwards) setPosition(getCurrentOffset() + wordLength);
        else setPosition(getCurrentOffset() - wordLength);
        positionChanged();
      }
      int start, len, docLen;
      String findWord = _findWord;
      // get the search space in the document
      String findSpace;
      _doc.acquireReadLock();  
      try {
        docLen = _doc.getLength();
        if (!_searchBackwards) {
          start = _current.getOffset();
          len = docLen - start;
        }
        else {
          start = 0;
          len = _current.getOffset();
        }
        findSpace = _doc.getText(start, len);
      }
      finally { _doc.releaseReadLock(); }
      
      if (!_matchCase) {
        findSpace = findSpace.toLowerCase();
        findWord = findWord.toLowerCase();
      }

      // find the first occurrence of findWord
      int foundOffset;
      foundOffset = !_searchBackwards ? findSpace.indexOf(findWord)
                    : findSpace.lastIndexOf(findWord);

      if (foundOffset >= 0) { // we have found it
        _found = true;
        foundOffset += start;
        if (!_searchBackwards) foundOffset += findWord.length();
        _current = _doc.createPosition(foundOffset);  // thread-safe operation on _doc
      }
      else { // we haven't found it yet
        if (_searchAllDocuments) {
          tempFr = _findNextInAllDocs(!_searchBackwards ? _docIterator.getNextDocument(_doc) :
                                      _docIterator.getPrevDocument(_doc));
          foundOffset = tempFr.getFoundOffset();
        }
        if (foundOffset == -1) {  // we still haven't found it
          _wrapped = true;
          //When we wrap, we need to include some text that was already searched before wrapping.
          //Otherwise, we won't find an only match that has the caret in it already.
          
          _doc.acquireReadLock();  
          try { 
            docLen = _doc.getLength(); 
            if (!_searchBackwards) {
              start = 0;
              len = _current.getOffset() + (_findWord.length() - 1);
              if (len > docLen) len = docLen;
            }
            else {  // found it
              start = _current.getOffset() - (_findWord.length() - 1);
              if (start < 0) start = 0;
              len = docLen - start;
            }
            findSpace = _doc.getText(start, len);
          }
          finally { _doc.releaseReadLock(); }

          if (!_matchCase) findSpace = findSpace.toLowerCase();
          foundOffset = !_searchBackwards ? findSpace.indexOf(findWord)
                        : findSpace.lastIndexOf(findWord);

          if (foundOffset >= 0) {
            foundOffset += start;
            if (!_searchBackwards) foundOffset += findWord.length();
            _current = _doc.createPosition(foundOffset);  // thread-safe operation on _doc
          }
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

  /** Searches docToSearch for _findWord, and continues cycling through the documents in the direction 
   *  specified by _searchBackwards. If the original _document is reached, we stop searching.
   *  @param docToSearch the document to search
   *  @return the FindResult containing the information for where we found _findWord or a dummy FindResult.
   */
  private FindResult _findNextInAllDocs(AbstractDocumentInterface docToSearch) throws BadLocationException {
    if (docToSearch == _doc) return new FindResult(_doc, -1, false);
    
    String text;
    docToSearch.acquireReadLock();
    try { text = docToSearch.getText(0, docToSearch.getLength()); }
    finally { docToSearch.releaseReadLock(); }
    String findWord = _findWord;
    if (!_matchCase) {
      text = text.toLowerCase();
      findWord = findWord.toLowerCase();
    }
    int index = !_searchBackwards ? text.indexOf(findWord) : text.lastIndexOf(findWord);
    
    if (index != -1) {
      // We found it in a different document, put the caret at the end of the
      // found word (if we're going forward).
      if (!_searchBackwards) index += findWord.length();
      return new FindResult(docToSearch, index, false);
    }
    return _findNextInAllDocs(!_searchBackwards ? _docIterator.getNextDocument(docToSearch) :
                                _docIterator.getPrevDocument(docToSearch));
  }
}

