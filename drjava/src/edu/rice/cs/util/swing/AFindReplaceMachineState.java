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

import edu.rice.cs.util.UnexpectedException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;


/**
 * Abstract State of the FindReplaceMachine.
 *
 * @version $Id$
 */
abstract class AFindReplaceMachineState {
  /**
   * The document on which FindReplaceMachine is operating.
   */
  static Document _doc;
  /**
   * The position in _doc from which the searches started.
   */
  static Position _start;
  /**
   * The position in _doc which the machine is currently at.
   */
  static Position _current;
  /**
   * The word being sought.
   */
  static String _findWord;
  /**
   * The word to replace the word being sought.
   */
  static String _replaceWord;
  static boolean _found;
  static boolean _wrapped;
  static boolean _matchCase;
  static boolean _searchBackwards;
  static boolean _searchAllDocuments;
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

  public AFindReplaceMachineState(DocumentIterator docIterator) {
    _skipOneFind = false;
    _docIterator = docIterator;
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

  public void setLastFindWord() {
    _lastFindWord = _findWord;
  }

  public boolean getSearchBackwards() {
    return _searchBackwards;
  }

  public void setSearchBackwards(boolean searchBackwards) {
    if (_searchBackwards != searchBackwards) {
      // If we switch from searching forward to searching backwards,
      // isOnMatch is true, and _findword is the same as the _lastFindWord,
      // we know the user just found _findWord, so skip one find.
      if (isOnMatch() && _findWord.equals(_lastFindWord)) {
        _skipOneFind = true;
      }else{
        _skipOneFind = false;
      }
    }
    _searchBackwards = searchBackwards;
  }

  public void setMatchCase(boolean matchCase) {
    _matchCase = matchCase;
  }

  public void setSearchAllDocuments(boolean searchAllDocuments) {
    _searchAllDocuments = searchAllDocuments;
  }

  public void setDocument(Document doc) {
    _doc = doc;
  }

  public void setPosition(int pos) {
    try {
      _current = _doc.createPosition(pos);
    }
    catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
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

  /**
   * Gets the character offset at which this machine started
   * operations.
   */
  public int getStartOffset() {
    return _start.getOffset();
  }

  /**
   * Gets the character offset to which this machine is currently pointing.
   */
  public int getCurrentOffset() {
    return _current.getOffset();
  }

  public void makeCurrentOffsetStart() {
    try {
      _start = _doc.createPosition(getCurrentOffset());
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  public String getFindWord() {
    return _findWord;
  }

  public String getReplaceWord() {
    return _replaceWord;
  }

  public boolean getSearchAllDocuments() {
    return _searchAllDocuments;
  }

  public Document getDocument() {
    return _doc;
  }

  /**
   * Change the word being sought.
   *
   * @param word the new word to seek
   */
  public void setFindWord(String word) {
    _findWord = word;
  }

  /**
   * Change the replacing word.
   *
   * @param word the new replacing word
   */
  public void setReplaceWord(String word) {
    _replaceWord = word;
  }

  /**
   * Determine if the machine is on an instance of the find word.
   *
   * @return true if the current position is right after an instance
   *         of the find word.
   */
  public boolean isOnMatch() {
    String findWord = _findWord;
    int len, off;
    
    if(_current == null) return false;
    
    len = findWord.length();
    if (!_searchBackwards) {
      off = _current.getOffset() - len;
    }
    else {
      off = _current.getOffset();
    }

    if (off < 0) {
      return false;
    }
    else if (off + len > _doc.getLength()) {
      return false;
    }

    try {
      String matchSpace = _doc.getText(off, len);
      if (!_matchCase) {
        matchSpace = matchSpace.toLowerCase();
        findWord = findWord.toLowerCase();
      }
      return matchSpace.equals(findWord);
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
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
  abstract public FindResult findNext();


  /**
   * If we're on a match for the find word, replace
   * it with the replace word.
   */
  public boolean replaceCurrent() {
    try {
      if (isOnMatch()) {
        boolean atStart = false;
        int position = getCurrentOffset();
        if (!_searchBackwards) {
          position -= _findWord.length();
        }
        _doc.remove(position, _findWord.length());
        if (position == 0) {
          atStart = true;
        }
        _doc.insertString(getCurrentOffset(), _replaceWord, null);

        // the current offset will be the end of the inserted word
        //since we keep track of current as a Position.
        //The exception is if we are at the beginning of the document,
        //in which case the text is inserted AFTER the current position
        //So, current offset is correct for forwards searching unless
        //we were at the start of the document, in which case it is
        //correct for backwards searching.
        if (atStart && !_searchBackwards) {
          setPosition(_replaceWord.length());
        }
        if (!atStart && _searchBackwards) {
          setPosition(getCurrentOffset() - _replaceWord.length());
        }

        return true;
      }
      else {
        return false;
      }
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Replaces all occurences of the find word with the replace
   * word. Checks to see if the entire document is searched in case
   * the find word is equivalent to the replace word in which case
   * an infinite loop would otherwise occur.  Starts at the beginning
   * or the end of the document (depending on find direction).  This
   * is so that matches created by string replacement will not be
   * replaced as in the following example:
   * findString:    "hello"
   * replaceString: "e"
   * document text: "hhellollo"
   * Depending on the cursor position, clicking replace all could either
   * make the document text read "hello" (which is correct) or "e".  This
   * is because of the behavior of findNext(), and it would be incorrect
   * to change that behavior.
   *
   * @return the number of replacements
   */
  public int replaceAll() {
    if (_searchAllDocuments) {
      Document startDoc = _doc;
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
    else {
      return _replaceAllInCurrentDoc();
    }
  }

  /**
   * Replaces all occurences of _findWord with _replaceWord in _doc.
   * Never searches in other documents.
   *
   * @return the number of replacements
   */
  private int _replaceAllInCurrentDoc() {
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
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
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


}
