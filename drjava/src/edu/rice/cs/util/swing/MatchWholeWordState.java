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

import javax.swing.text.BadLocationException;

/**
 * MatchWholeWordState.java
 * <p/>
 * <p/>
 * Created: Fri Feb  6 15:18:51 2004
 *
 * @version $Id$
 */
public class MatchWholeWordState extends AFindReplaceMachineState {
  /**
   * Constructs a new MatchWholeWordState
   * @param docIterator the document iterator to use
   */
  public MatchWholeWordState(DocumentIterator docIterator) {
    super(docIterator);
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
    try {
      //String searchDocument =
      //    _doc.getText(searchOriginLocation, _doc.getLength() - searchOriginLocation) +
      //    _doc.getText(0, searchOriginLocation);

      // If the user just found and toggled the "Search Backwards"
      // option, we should skip the first find.
      if (_skipOneFind) {

        int wordLength = _lastFindWord.length();
        if (!_searchBackwards) {
          setPosition(getCurrentOffset() + wordLength);
        }
        else {
          setPosition(getCurrentOffset() - wordLength);
        }
        positionChanged();
      }


      int searchOriginLocation = getCurrentOffset();
      _wrapped = false;
      String searchDocument = _doc.getText(0, _doc.getLength());

      if (indexOf(searchDocument, _findWord) != -1) {
        return findNextHelp(searchDocument, searchOriginLocation);
      }
      else {
        return new FindResult(_doc, -1, true);
      }
    }
    catch (BadLocationException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private FindResult findNextHelp(String searchDocument, int
      searchOriginLocation) {

    if (_searchBackwards) {
      setPosition(getCurrentOffset() - _findWord.length());
    }

    int findOffset = _searchBackwards ? -1 : 1;
    while (!hasLappedOriginalLocation(searchOriginLocation)) {
      // actually, may not even need this first clause... think about it...

      int current = getCurrentOffset();
      int nextPotentialMatchLocation = indexOf(searchDocument, _findWord, current);
      if (nextPotentialMatchLocation == -1) {
        if (_wrapped) {
          break;
        }
        wrapAroundDocument();
      }
      else {
        setPosition(nextPotentialMatchLocation);

        if (wholeWordFoundAtCurrent(searchDocument)) {
         
          if (!_searchBackwards) {
            setPosition(getCurrentOffset() + _findWord.length());
          }

          return new FindResult(_doc, getCurrentOffset(), _wrapped);
        }
        else {
          if (nextPotentialMatchLocation + findOffset < 0 ||
              nextPotentialMatchLocation + findOffset >= _doc.getLength()) {
            wrapAroundDocument();
          }
          setPosition(nextPotentialMatchLocation + findOffset);
        }
      }
    }
    setPosition(searchOriginLocation);
    return new FindResult(_doc, -1, _wrapped);
  }

  private int indexOf(String str, String findword) {
    if (_matchCase) {
      return str.indexOf(findword);
    }
    else {
      String lowerstr = str.toLowerCase();
      String lowerfindword = findword.toLowerCase();
      return lowerstr.indexOf(lowerfindword);
    }

  }

  private int indexOf(String str, String findword, int fromIndex) {
    if (!_searchBackwards) {
      if (_matchCase) {
        return str.indexOf(findword, fromIndex);
      }
      else {
        String lowerstr = str.toLowerCase();
        String lowerfindword = findword.toLowerCase();
        return lowerstr.indexOf(lowerfindword, fromIndex);
      }
    }
    else {
      if (_matchCase) {
        return str.lastIndexOf(findword, fromIndex);
      }
      else {
        String lowerstr = str.toLowerCase();
        String lowerfindword = findword.toLowerCase();
        return lowerstr.lastIndexOf(lowerfindword, fromIndex);
      }
    }
  }


  private boolean hasLappedOriginalLocation(int searchOriginLocation) {
    if (!_searchBackwards) {
      if (_wrapped && searchOriginLocation <= getCurrentOffset()) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      if (_wrapped && searchOriginLocation >= getCurrentOffset()) {
        return true;
      }
      else {
        return false;
      }
    }
  }

  private void wrapAroundDocument() {
    _wrapped = true;
    if (!_searchBackwards) {
      setPosition(0);
    }
    else {
      setPosition(_doc.getLength() - 1);
    }
  }

  private boolean wholeWordFoundAtCurrent(String searchDocument) {

    int current = getCurrentOffset();
    Character leftOfMatch = null;
    Character rightOfMatch = null;
    int leftLocation = current - 1;
    int rightLocation = current + _findWord.length();
    boolean leftOutOfBounds = false;
    boolean rightOutOfBounds = false;

    try {
      leftOfMatch = new Character(searchDocument.charAt(leftLocation));
    }
    catch (IndexOutOfBoundsException e) {
      leftOutOfBounds = true;
    }

    try {
      rightOfMatch = new Character(searchDocument.charAt(rightLocation));
    }
    catch (IndexOutOfBoundsException e) {
      rightOutOfBounds = true;
    }

    if (!leftOutOfBounds && !rightOutOfBounds) {
      if (isDelimter(rightOfMatch) && isDelimter(leftOfMatch)) {
        return true;
      }
      else {
        return false;
      }
    }
    else if (!leftOutOfBounds) {
      if (isDelimter(leftOfMatch)) {
        return true;
      }
      else {
        return false;
      }

    }
    else if (!rightOutOfBounds) {
      if (isDelimter(rightOfMatch)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      //return false;
      return true;
    }
  }

  private boolean isDelimter(Character ch) {
    return !Character.isLetterOrDigit(ch.charValue());
  }
  /*
  private void setCurrentSafely(int newcurrent)
  {
  try
  {
  _current = _doc.createPosition(newcurrent);
  }
  catch(BadLocationException e)
  {
  throw new RuntimeException(e.getMessage());
  }
  }
  */


} // MatchWholeWordState
