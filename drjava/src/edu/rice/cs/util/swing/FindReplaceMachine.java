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
import edu.rice.cs.util.text.AbstractDocumentInterface;

/**
* Implementation of logic of find/replace over a document.
*
* @version $Id$
*/
public class FindReplaceMachine {

    private AFindReplaceMachineState _state;

    private AFindReplaceMachineState _findAnyOccurrence;
    private AFindReplaceMachineState _matchWholeWord;
    
    /** Standard Constructor.
     *  Creates a new machine to perform find/replace operations on a particular document starting from a 
     *  certain position.
     *  @param doc the Document being operated on
     *  @param position the character offset into the document
     *  @exception BadLocationException
     */

    /** NonModal version */
    public FindReplaceMachine(DocumentIterator docIterator) {

        _findAnyOccurrence = new FindAnyOccurrenceState(docIterator);
        _matchWholeWord = new MatchWholeWordState(docIterator);

        setFindAnyOccurrence();
        setFindWord("");
        setReplaceWord("");
        setMatchCase(true);
        setSearchAllDocuments(false);

    }

    /** Called when the current position is updated in the document and therefore we do not want to skip the
     *  instance of the findWord we are on if the user toggles _searchBackwards
     */
    public void positionChanged() { _state.positionChanged(); }

    public void setLastFindWord() { _state.setLastFindWord(); }

    public boolean getSearchBackwards() { return _state.getSearchBackwards(); }

    public void setSearchBackwards(boolean searchBackwards) { _state.setSearchBackwards(searchBackwards); }

    public void setMatchCase(boolean matchCase) { _state.setMatchCase(matchCase); }

    public void setSearchAllDocuments(boolean searchAllDocuments) {
        _state.setSearchAllDocuments(searchAllDocuments);
    }

    public void setDocument(AbstractDocumentInterface doc) { _state.setDocument(doc); }

    public void setPosition(int pos)  { _state.setPosition(pos); }

    public void setStart(int pos)  { _state.setStart(pos); }

    /** Gets the character offset at which this machine started operations. */
    public int getStartOffset() { return _state.getStartOffset(); }

    /** Gets the character offset to which this machine is currently pointing.*/
    public int getCurrentOffset() {  return _state.getCurrentOffset(); }

    public void makeCurrentOffsetStart() { _state.makeCurrentOffsetStart(); }

    public String getFindWord() { return _state.getFindWord(); }

    public String getReplaceWord() { return _state.getReplaceWord(); }

    public boolean getSearchAllDocuments() { return _state.getSearchAllDocuments(); }

    public AbstractDocumentInterface getDocument() { return _state.getDocument(); }

    /** Change the word being sought.
    *   @param word the new word to seek
    */
    public void setFindWord(String word) { _state.setFindWord(word); }

    /** Change the state to MatchWholeWordState. */
    public void setMatchWholeWord() { _state = _matchWholeWord; }

    /** Change the state to FindAnyOccurrenceState. */
    public void setFindAnyOccurrence() { _state = _findAnyOccurrence; }

    /** Change the replacing word.
    *  @param word the new replacing word
    */
    public void setReplaceWord(String word) { _state.setReplaceWord(word); }

    /** Determine if the machine is on an instance of the find word.
     *  @return true if the current position is right after an instance of the find word.
     */
    public boolean isOnMatch() { return _state.isOnMatch(); }
    
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
    public FindResult findNext() { return _state.findNext(); }

    /** If we're on a match for the find word, replace it with the replace word. */
    public boolean replaceCurrent() { return _state.replaceCurrent(); }

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
    * @return the number of replacements
    */
    public int replaceAll() { return _state.replaceAll(); }

}
