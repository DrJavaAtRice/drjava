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

package edu.rice.cs.util.swing;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;

import edu.rice.cs.util.UnexpectedException;

import java.util.Date;

/**
 * Implementation of logic of find/replace over a document.
 *
 * @version $Id$
 */
public class FindReplaceMachine {
  /** The document on which FindReplaceMachine is operating. */
  private Document _doc;
  /** The position in _doc from which the searches started. */
  private Position _start;
  /** The position in _doc which the machine is currently at. */
  private Position _current;
  /** The word being sought. */
  private String _findWord;
  /** The word to replace the word being sought. */
  private String _replaceWord;
  private boolean _found;
  private boolean _wrapped;
  private boolean _matchCase;
  private boolean _searchBackwards;

  /**
   * Constructor.
   * Creates a new machine to perform find/replace operations
   * on a particular document starting from a certain position.
   * @param doc the Document being operated on
   * @param position the character offset into the document
   * @exception BadLocationException
   */

  /**
   * NonModal version
   */
  public FindReplaceMachine() {
    _findWord = "";
    _replaceWord = "";
    _matchCase = true;
  }

  public boolean getSearchBackwards() {
    return _searchBackwards;
  }

  public void setSearchBackwards(boolean searchBackwards) {
    this._searchBackwards = searchBackwards;
  }

  public void setMatchCase(boolean matchCase) {
    _matchCase = matchCase;
  }

  public void setDocument(Document doc) {
    _doc = doc;
  }

  public void setPosition(int pos)
  {
    try {
      _current = _doc.createPosition(pos);
    } catch (BadLocationException ble) {
      throw new UnexpectedException(ble);
    }
  }

  public void setStart(int pos)
  {
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

  /**
   * Change the word being sought.
   * @param word the new word to seek
   */
  public void setFindWord(String word) {
    _findWord = word;
  }

  /**
   * Change the replacing word.
   * @param word the new replacing word
   */
  public void setReplaceWord(String word) {
    _replaceWord = word;
  }

  /**
   * Determine if the machine is on an instance of the find word.
   * @return true if the current position is right after an instance
   *         of the find word.
   */
  public boolean isOnMatch() {
    String findWord = this._findWord;
    int len, off;
    len = findWord.length();
    if(!_searchBackwards) {
      off = _current.getOffset() - len;
    } else {
      off = _current.getOffset();
    }

    if(off < 0){
      return false;
    } else if (off + len > _doc.getLength()){
      return false;
    }

    try {
      String matchSpace = _doc.getText(off, len);
      if (!_matchCase){
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
   * @return a FindResult object containing foundOffset and aflag
   * indicating wrapping to the beginning during a search
   */
  public FindResult findNext() {
    try {
      int start, len;
      String findWord = this._findWord;
      // get the search space in the document
      String findSpace;
      if(!_searchBackwards){
        start = _current.getOffset();
        len = _doc.getLength() - start;
      } else {
        start = 0;
        len = _current.getOffset();
      }
      findSpace = _doc.getText(start, len);
      if (!_matchCase){
        findSpace = findSpace.toLowerCase();
        findWord = findWord.toLowerCase();
      }

      // find the first occurrence of findWord
      int foundOffset;
      foundOffset = !_searchBackwards ? findSpace.indexOf(findWord)
                                      : findSpace.lastIndexOf(findWord);
      // if we've found it
      if (foundOffset >= 0) {
        _found = true;
        foundOffset += start;
        if (!_searchBackwards){
           foundOffset += findWord.length();
        }
        _current = _doc.createPosition(foundOffset);
      } else {
        // if we haven't found it
        _wrapped = true;
        //When we wrap, we need to include some text that was already searched before wrapping.
        //Otherwise, we won't find an only match that has the caret in it already.
        if(!_searchBackwards){
          start = 0;
          len = _start.getOffset() + (_findWord.length() - 1);
          if(len > _doc.getLength()){
            len = _doc.getLength();
          }
        } else {
          start = _start.getOffset() - (_findWord.length() - 1);
          if (start < 0){
            start = 0;
          }
          len = _doc.getLength() - start;
        }
        findSpace = _doc.getText(start, len);

        if (!_matchCase) {
          findSpace = findSpace.toLowerCase();
        }

        foundOffset = !_searchBackwards ? findSpace.indexOf(findWord)
                                        : findSpace.lastIndexOf(findWord);

        if (foundOffset >= 0) {
          foundOffset += start;
          if (!_searchBackwards) {
            foundOffset += findWord.length();
          }
          _current = _doc.createPosition(foundOffset);
        }
      }
      // flag the return value so that they can tell that we had to wrap
      // the file to determine the info.

      //This means we have found the word before, just not in this call
      if(foundOffset == -1 && _found) {
        _current = _start;
        _found = false;
        return findNext();
      }
      else {
        FindResult fr = new FindResult(foundOffset, _wrapped);
        _wrapped = false;
        return fr;
      }
    }
    catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * If we're on a match for the find word, replace
   * it with the replace word.
  */
  public boolean replaceCurrent() {
    try {
      if (isOnMatch()) {
        boolean atStart = false;
        int position = getCurrentOffset();
        if(!_searchBackwards) {
          position -= _findWord.length();
        }
        _doc.remove(position, _findWord.length());
        if (position == 0){
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
        if(atStart && !_searchBackwards) {
          setPosition(_replaceWord.length());
        }
        if(!atStart && _searchBackwards){
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
   * make the document text read "hello" (which is correct) or "".  This
   * is because of the behavior of findNext(), and it would be incorrect
   * to change that behavior
   */
  public int replaceAll() {
    try {
      if (!_searchBackwards){
        _start = _doc.createPosition(0);
        setPosition(0);
      } else {
        _start = _doc.createPosition(_doc.getLength());
        setPosition(_doc.getLength());
      }
    } catch (BadLocationException e) {
      throw new UnexpectedException(e);
    }
    int count = 0;
    FindResult fr = findNext();
    int found = fr.getFoundOffset();
    int wrapped = 0;
    if (fr.getWrapped())
      wrapped++;
    // Checks that the findNext method has found something and has not
    // wrapped once and gone beyond start.
//    while (found >= 0 && (wrapped == 0 ||
//                         ((found < _start.getOffset() + _findWord.length() && !_searchBackwards)  ||
//                          (found > _start.getOffset() - _findWord.length() && _searchBackwards))) && wrapped < 2) {

    //new while condition, since I started replacing from the beginning/end of the document only
    while(wrapped == 0) {
      replaceCurrent();
      count++;
      fr = findNext();
      found = fr.getFoundOffset();
      if (fr.getWrapped())
        wrapped++;
    }
    return count;
  }
}
