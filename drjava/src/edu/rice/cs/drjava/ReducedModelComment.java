package edu.rice.cs.drjava;

import gj.util.Stack;
import gj.util.Vector;

/**
 * This class provides an implementation of the BraceReduction
 * interface for brace matching.  In order to correctly match, this class
 * keeps track of what is commented (line and block) and what is inside
 * single and double quotes and keeps this in mind when matching.
 * To avoid unnecessary complication, this class maintains a few
 * invariants for its consistent states, i.e., between top-level
 * function calls.
 * <ol>
 * <li> The cursor offset is never at the end of a brace.  If movement
 * or insertion puts it there, the cursor is updated to point to the 0
 * offset of the next brace.
 * <li> Quoting information is invalid inside valid comments.  When part
 * of the document becomes uncommented, the reduced model must update the
 * quoting information linearly in the newly revealed code.
 * <li> Quote shadowing and comment shadowing are mutually exclusive.
 * <li> Single quotes and double quotes are mutually exclusive with regards to
 *  shadowing.
 * <li> There is no nesting of comment open characters. If // is encountered
 *      in the middle of a comment, it is treated as two separate slashes.
 *      Similar for /*.
 * </ol>
 * @version $Id$
 */

public class ReducedModelComment implements ReducedModelStates {

  /**
  * The character that represents the cursor in toString().
  * @see #toString()
  */
  public static final char PTR_CHAR = '#';
  
  /**
  * A list of ReducedTokens (braces and gaps).
  * @see ModelList
  */
  TokenList _braces;
  /**
  * keeps track of cursor position in document
  * @see ModelList.Iterator
  */
  TokenList.Iterator _cursor;
  
  /**Can be used by other classes to walk through the list of comment chars*/
  TokenList.Iterator _walker;

  /**
  * Constructor.  Creates a new reduced model with the cursor
  * at the start of a blank "page."
  */
  public ReducedModelComment() {
    _braces = new TokenList();
    _cursor = _braces.getIterator();
    _walker = _cursor.copy();
    // we should be pointing to the head of the list
    _cursor.setBlockOffset(0);
  }
  
  int getBlockOffset() {
    return _cursor.getBlockOffset();
  }
  void setBlockOffset(int offset) {
    _cursor.setBlockOffset(offset);
  }
  /**
  * Package private absolute offset for tests.
  * We don't keep track of absolute offset as it causes too much confusion
  * and trouble.
  */
  int absOffset() {
    int off = _cursor.getBlockOffset();
    TokenList.Iterator it = _cursor.copy();
    
    if (!it.atStart()) {
      it.prev();
    }
    while (!it.atStart()) {
      off += it.current().getSize();
      it.prev();
    }
    it.dispose();
    return off;
  }
  
  /**
  * A toString replacement for testing - easier to read.
  */  
  public String simpleString() {
    String val = "";
    ReducedToken tmp;
    
    TokenList.Iterator it = _braces.getIterator();
    it.next(); // since we start at the head, which has no current item
    
    
    if (_cursor.atStart()) {
      val += PTR_CHAR;
      val += _cursor.getBlockOffset();
    }
    
    while(!it.atEnd()) {
      tmp = it.current();
      
      if (!_cursor.atStart() && !_cursor.atEnd() && 
          (tmp == _cursor.current()))
      {
        val += PTR_CHAR;
        val += _cursor.getBlockOffset();
      }
      
      val += "|";
      val += tmp;
      val += "|\t";
      
      it.next();
    }
    
    if (_cursor.atEnd()) {
      val += PTR_CHAR;
      val += _cursor.getBlockOffset();
    }
    
    val += "|end|";
    it.dispose();
    return val;
  }
  
  
  public void insertChar(char ch) {
    switch(ch) {
      case '*': insertSpecial("*"); break;
      case '/': insertSpecial("/"); break;
      case '\n': insertNewline(); break;
      case '\\': insertSpecial("\\"); break;
      case '\'': insertQuote("'"); break; 
      case '\"': insertQuote("\""); break;
      default:
        _insertGap(1); break;
    }
  }

  /**
  * Inserts one of three special chars, (*),(/), or (\).
  * <OL>
  *  <li> empty list: insert slash
  *  <li> atEnd: check previous and insert slash
  *  <li> inside multiple character brace:
  *   <ol>
  *    <li> break current brace
  *    <li> move next to make second part current
  *    <li> insert brace between broken parts of former brace
  *    <li> move previous twice to get before the broken first part
  *    <li> walk
  *    <li> current = multiple char brace? move next once<BR>
  *         current = single char brace?  move next twice<BR>
  *         We moved two previous, but if the broken part combined with
  *         the insert, there's only one brace where once were two.
  *   </ol>
  *  <li> inside a gap: use helper function
  *  <li> before a multiple char brace:
  *   <ol>
  *    <li> break the current brace
  *    <li> check previous and insert
  *   </ol>
  *  <li>otherwise, check previous and insert
  * </OL>
  */
  private void insertSpecial(String special) {
    // Check if empty.
    if (_braces.isEmpty()) {
      _insertNewBrace(special, _cursor); //now pointing to tail.
      return;
    }
    // Check if at start.
    if (_cursor.atStart()) {
      _cursor.next();
    }
    // Not empty, not at start, if at end check the previous brace
    if (_cursor.atEnd()) {
      _checkPreviousInsertSpecial(special);
    }      
    // If inside a double character brace, break it.
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace()) {
      _cursor._splitCurrentIfCommentBlock(true,true);
      //leaving us at the start
      _cursor.next(); //leaving us after first char
      _insertNewBrace(special, _cursor); //leaves us after the insert
      move(-2);
      _updateBasedOnCurrentState();
      move(2);
    }
    // inside a gap
    else if ((_cursor.getBlockOffset() > 0) && (_cursor.current().isGap())) {
      _insertBraceToGap(special, _cursor);
    }   
    //if at start of double character brace, break it.
    else if ((_cursor.getBlockOffset() == 0) && _cursor.current().isMultipleCharBrace()) {
      //if we're free there won't be a block comment close so if there
      //is then we don't want to break it.  If the special character is 
      // a backslash, we want to break the following escape sequence if there
      // is one.
      _cursor._splitCurrentIfCommentBlock(false,special.equals("\\")); 
      //leaving us at start
      
      _checkPreviousInsertSpecial(special);
    }
    else {
      _checkPreviousInsertSpecial(special);
    }
    return;
  }
 
  /**
   * Checks before point of insertion to make sure we don't need to combine.
   * Delegates work to _checkPreviousInsertBackSlash and _checkPreviousInsertCommentChar,
   * depending on what's being inserted into the document.
   */  
  private void _checkPreviousInsertSpecial(String special)
     {
       if (special.equals("\\"))
         _checkPreviousInsertBackSlash();
       else
         _checkPreviousInsertCommentChar(special);
     }
         
  /**
  * Checks before point of insertion to make sure we don't need to combine
  * backslash with another backslash (yes, they too can be escaped).
  */
  
  private void _checkPreviousInsertBackSlash() {
    if (!_cursor.atStart()  && !_cursor.atFirstItem()) {
      if (_cursor.prevItem().getType().equals("\\")) {
        _cursor.prevItem().setType("\\\\");
        _updateBasedOnCurrentState();
        return;
      }
    }
    // Here we know the / unites with nothing behind it.
    _insertNewBrace("\\", _cursor); //leaving us after the brace.
    _cursor.prev();
    _updateBasedOnCurrentState();
    if (_cursor.current().getSize() == 2) {
      _cursor.setBlockOffset(1);
    }
    else {
      _cursor.next();
    }
  }
  
  /**
  * Checks before the place of insert to make sure there are no preceding
  * slashes with which the inserted slash must combine.  It then performs
  * the insert of either (/), (/ /), (/ *) or (* /).   
  */
  private void _checkPreviousInsertCommentChar(String special) {
    if (!_cursor.atStart()  && !_cursor.atFirstItem()) {
      if ((_cursor.prevItem().getType().equals("/")) &&
          (_cursor.prevItem().getState() == FREE))
          {
            _cursor.prevItem().setType("/" + special);
            _updateBasedOnCurrentState();
            return;
          }
      // if we're after a star, 
      else if ((_cursor.prevItem().getType().equals("*")) &&
               (getStateAtCurrent() == INSIDE_BLOCK_COMMENT) &&
               special.equals("/"))
        {
          _cursor.prevItem().setType("*" + special);
          _cursor.prevItem().setState(FREE);
          _updateBasedOnCurrentState();
          return;
        }
    }
    //Here we know the / unites with nothing behind it.
    _insertNewBrace(special, _cursor); //leaving us after the brace.
    _cursor.prev();
    _updateBasedOnCurrentState();
    if (_cursor.current().getSize() == 2)
      _cursor.setBlockOffset(1);
    else
      _cursor.next();
  }
  
  /**
  * Inserts an end-of-line character.
  * <OL>
  *  <li> atStart: insert
  *  <li> atEnd: insert
  *  <li> inside multiple character brace:
  *   <ol>
  *    <li> break current brace
  *    <li> move next to make second part current
  *    <li> insert brace between broken parts of former brace
  *    <li> move previous twice to get before the broken first part
  *    <li> walk
  *    <li> move next twice to be after newline insertion
  *   </ol>
  *  <li> inside a gap: use helper function
  *  <li>otherwise, just insert normally
  * </OL>
  * @return a Vector of highlighting information after the cursor
  */
  public void insertNewline() {
    if (_cursor.atStart()) {
      _insertNewEndOfLine();
    }
    else if (_cursor.atEnd()) {
      _insertNewEndOfLine();
    }
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace()) {
      _cursor._splitCurrentIfCommentBlock(true, true);
      _cursor.next();
      _cursor.insert(Brace.MakeBrace("\n", getStateAtCurrent()));
      _cursor.prev();
      _updateBasedOnCurrentState();
      _cursor.next();
      _cursor.next();
      _cursor.setBlockOffset(0);
    }
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isGap()) {
      _insertBraceToGap("\n", _cursor);
    }
    else {
      _insertNewEndOfLine();
    }
    return;
  }
  
  private void _insertNewEndOfLine() {
    _insertNewBrace("\n", _cursor);
    _cursor.prev();
    _updateBasedOnCurrentState();
    _cursor.next();
    _cursor.setBlockOffset(0);
  }

  /**
   * Inserts the specified quote character.
   * <OL>
   *  <li> atStart: insert
   *  <li> atEnd: insert
   *  <li> inside multiple character brace:
   *   <ol>
   *    <li> break current brace
   *    <li> move next to make second part current
   *    <li> insert brace between broken parts of former brace
   *    <li> walk
   *    <li> current = multiple char brace? move next once<BR>
   *         current = single char brace?  move next twice<BR>
   *         We moved two previous, but if the broken part combined with
   *         the insert, there's only one brace where once were two.
   *    <li> move next twice to be after newline insertion
   *   </ol>
   *  <li> inside a gap: use helper function
   *  <li> before a multiple char brace:
   *   <ol>
   *    <li> break the current brace
   *    <li> check previous and insert
   *   </ol>
   *  <li>otherwise, just insert normally
   * </OL>
   * @param quote the type of quote to insert
   * @return a Vector of highlighting information after the cursor
   */
  public void insertQuote(String quote) {
    if (_cursor.atStart()) {
      _insertNewQuote(quote);
    }
    else if (_cursor.atEnd()) {
      _insertNewQuote(quote);
    }
    // in the middle of a multiple character brace
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isMultipleCharBrace()) {
      _cursor._splitCurrentIfCommentBlock(true,true);
      _cursor.next();
      _cursor.insert(Brace.MakeBrace(quote, getStateAtCurrent()));
      _cursor.prev();
      _updateBasedOnCurrentState();
      if (!_cursor.current().isMultipleCharBrace())
        _cursor.next();
      _cursor.next();
      _cursor.setBlockOffset(0);
    }
    // in the middle of a gap
    else if ((_cursor.getBlockOffset() > 0) && _cursor.current().isGap()) {
      _insertBraceToGap(quote, _cursor);
    }
    else {
      _insertNewQuote(quote);
    }
    return;
  }
  
  /**
   * Helper function for insertQuote.  Creates a new quote Brace and puts it in the
   * reduced model.
   * @param quote the quote to insert
   */
  private void _insertNewQuote(String quote) {
    String insert = _getQuoteType(quote);    
    _insertNewBrace(insert, _cursor);
    _cursor.prev();
    _updateBasedOnCurrentState();
    _cursor.next();
    _cursor.setBlockOffset(0);
  }
  
  /**
   * Helper function for insertNewQuote.  In the case where a backslash
   * precedes the point of insertion, it removes the backslash and returns
   * the text for an escaped quote.  The type of quote depends on the given
   * argument.
   * @param quote the type of quote to insert
   * @return a regular or escaped quote, depending on what was previous
   */
  private String _getQuoteType(String quote) {
    if (_cursor.atStart() || _cursor.atFirstItem()) {
      return quote;
    }
    else if (_cursor.prevItem().getType().equals("\\")) {
      _cursor.prev();
      _cursor.remove();
      return "\\" + quote;
    }
    else {
      return quote;
    }
  }  

  /**
  * Inserts a block of non-brace text into the reduced model.
  * <OL>
  *  <li> atStart: if gap to right, augment first gap, else insert
  *  <li> atEnd: if gap to left, augment left gap, else insert
  *  <li> inside a gap: grow current gap, move offset by length
  *  <li> inside a multiple character brace:
  *   <ol>
  *    <li> break current brace
  *    <li> insert new gap
  *   </ol>
  *  <li> gap to left: grow that gap and set offset to zero
  *  <li> gap to right: this case handled by inside gap (offset invariant)
  *  <li> between two braces: insert new gap
  * @param length the length of the inserted text
  */
  public void _insertGap(int length) {
    if (_cursor.atStart()) {
      if (_gapToRight()) {
        _cursor.next();
        _augmentCurrentGap(length); //increases gap and moves offset
      }
      else {
        _insertNewGap(length);//inserts gap and goes to next item
      }
    }
    else if (_cursor.atEnd()) {
      if (_gapToLeft()) {
        _augmentGapToLeft(length);
        //increases the gap to the left and
        //cursor to next item in list leaving offset 0              
      }
      else {
        _insertNewGap(length); //inserts gap and moves to next item
      }
    }
    //offset should never be greater than 1 here because JAVA only has 2
    //char comments  
    else if (_cursor.current().isMultipleCharBrace() && (_cursor.getBlockOffset() > 0)) {    
      if (_cursor.getBlockOffset() > 1) {      
        throw new IllegalArgumentException("OFFSET TOO BIG:  " + _cursor.getBlockOffset());  
      }    
      _cursor._splitCurrentIfCommentBlock(true, true);      
      _cursor.next();
      _insertNewGap(length);  //inserts gap and goes to next item  
      // we have to go back two tokens; we don't want to use move because it could
      // throw us past start if there was only one character before us and we went
      // the usual 2 spaces before.  There would have to be a check and a branch
      // depending on conditions that way.
      _cursor.prev();
      _cursor.prev();
      _updateBasedOnCurrentState();   
      // restore cursor state
      _cursor.next();
      _cursor.next();
      return;
    }
    
    //1
    else if (_cursor.current().isGap()) {
      _cursor.current().grow(length);
      _cursor.setBlockOffset(_cursor.getBlockOffset() + length);
    }
    //2
    else if (!_cursor.atFirstItem() &&
             _cursor.prevItem().isGap())
    {
      //already pointing to next item
      _cursor.prevItem().grow(length);
    }
    //4
    else { //between two braces
      _insertNewGap(length); //inserts a gap and goes to the next item
    }
    return;
  }
  
  public TokenList.Iterator makeCopyCursor() {
    return _cursor.copy();
  }
 
 /**
  * Wrapper for TokenList.Iterator.getStateAtCurrent that returns the current 
  * state for some iterator.
  * Convenience method to return the current state in the cursor iterator.
  */
  ReducedModelState getStateAtCurrent() {
    return _cursor.getStateAtCurrent();
  }
  
 /**
  * Returns true if there is a gap immediately to the right.
  */
  private boolean _gapToRight() {
    // Before using, make sure not at last, or tail.
    return (!_braces.isEmpty() && !_cursor.atEnd() &&
            !_cursor.atLastItem() && _cursor.nextItem().isGap());
  }
  /**
  * Returns true if there is a gap immediately to the left.   
  */
  private boolean _gapToLeft() {
    // Before using, make sure not at first or head.
    return (!_braces.isEmpty() && !_cursor.atStart() &&
            !_cursor.atFirstItem() &&  _cursor.prevItem().isGap());
  }
  
  /**
  * Assuming there is a gap to the left, this function increases
  * the size of that gap.
  * @param length the amount of increase
  */
  private void _augmentGapToLeft(int length) {
    _cursor.prevItem().grow(length);      
  }
  
  /**
  * Assuming there is a gap to the right, this function increases
  * the size of that gap.
  * @param length the amount of increase
  */
  private void _augmentCurrentGap(int length) {
    _cursor.current().grow(length);
    _cursor.setBlockOffset(length);
  }
  /**
  * Helper function for _insertGap.
  * Performs the actual insert and marks the offset appropriately.
  * @param length size of gap to insert
  */
  private void _insertNewGap(int length) {
    _cursor.insert(new Gap(length, getStateAtCurrent()));
    _cursor.next();
    _cursor.setBlockOffset(0);
  }
  
  /**
  * Helper function to _insertBrace.
  * Handles the details of the case where a brace is inserted into a gap.
  */
  private void _insertBraceToGap(String text, TokenList.Iterator copyCursor) {
    copyCursor.current().shrink(_cursor.getBlockOffset());
    copyCursor.insert(Brace.MakeBrace(text, getStateAtCurrent()));
    copyCursor.insert(new Gap(_cursor.getBlockOffset(), getStateAtCurrent()));
    TokenList.Iterator copy2 = copyCursor.copy();
    _updateBasedOnCurrentStateHelper(copy2);
    copy2.dispose();
    copyCursor.next(); // now pointing at new brace
    copyCursor.next(); // now pointing at second half of gap
    _cursor.setBlockOffset(0);
  }
  
  /**
  * Helper function to _insertBrace.
  * Handles the details of the case where brace is inserted between two
  * reduced tokens.  No destructive action is taken.
  */
  private void _insertNewBrace(String text, TokenList.Iterator copyCursor) {
    copyCursor.insert(Brace.MakeBrace(text, getStateAtCurrent()));
    copyCursor.next();
    _cursor.setBlockOffset(0);
  }
  
  /**
  * USE RULES:
  * Inserting between braces: This should be called from between the two
  *                           characters of the broken double comment.
  * Deleting special chars: Start from previous char if it exists.
  * Begins updating at current character.  /./ would not become // because
  * current is in the middle. 
  * Double character comments inside of a quote or a comment are broken.
  */
  
  private void _updateBasedOnCurrentState() {
    TokenList.Iterator copyCursor = _cursor.copy();
    _updateBasedOnCurrentStateHelper(copyCursor);
    copyCursor.dispose();
  }
  
  /**
  * The walk function.
  * Walks along the list on which ReducedModel is based from the current
  * cursor position.  Which path it takes depends on the
  * return value of getStateAtCurrent() at the start of the walk.
  */
  private void _updateBasedOnCurrentStateHelper(TokenList.Iterator copyCursor) {
    if (copyCursor.atStart()) {
      copyCursor.next();
    }
    
    // If there's no text after here, nothing to update!
    if (copyCursor.atEnd()) {
      return;
    }
    
    ReducedModelState curState = copyCursor.getStateAtCurrent();
    // Free if at the beginning     
    while (!copyCursor.atEnd()) {
      curState = curState.update(copyCursor);
    }
  }
  
 /**
  * Updates the BraceReduction to reflect cursor movement.
  * Negative values move left from the cursor, positive values move
  * right.
  * @param count indicates the direction and magnitude of cursor movement
  */
  public void move(int count) {
    _cursor.move(count);
  }
  
  /**
  * <P>Update the BraceReduction to reflect text deletion.</P>
  * @param count indicates the size and direction of text deletion.
  * Negative values delete text to the left of the cursor, positive
  * values delete text to the right.
  * Always move count spaces to make sure we can delete.
  */
  public void delete(int count) {
    if (count == 0) {
      return;
    }
    TokenList.Iterator copyCursor = _cursor.copy();
    // from = the _cursor
    // to = _cursor.copy()
    _cursor.setBlockOffset(_delete(count, _cursor, copyCursor));
    copyCursor.dispose();
    int absOff = this.absOffset();
    int movement;
    if (absOff < 2)
      movement = absOff;
    else
      movement = 2;
    _cursor.move(-movement);
    _updateBasedOnCurrentState();
    _cursor.move(movement);
    return;
  }
  
  /**
  * Helper function for delete.
  * If deleting forward, move delTo the distance forward and call
  * deleteRight.<BR>
  * If deleting backward, move delFrom the distance back and call
  * deleteRight.
  * @param count size of deletion
  * @param offset current offset for cursor
  * @param delFrom where to delete from
  * @param delTo where to delete to
  * @return new offset after deletion
  */
  private int _delete(int count, TokenList.Iterator delFrom,
                      TokenList.Iterator delTo)
  {                      
    // Guarrantees that its possible to delete count characters.
    if (count > 0) {
      try {
        delTo.move(count);
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Trying to delete" +
                                           " past end of file.");        
      }
    }
    else { // count <= 0
      try {
        delFrom.move(count);
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Trying to delete" +
                                           " past end of file.");        
      }
    }
    return _deleteRight(delFrom, delTo);
  }
  
  
  /**
   * Deletes from offset in delFrom to endOffset in delTo.
   * Uses ModelList's collapse function to facilitate quick deletion.
   */
  private int _deleteRight(TokenList.Iterator delFrom, TokenList.Iterator delTo)
  {
    delFrom.collapse(delTo);
    
    // if both pointing to same item, and it's a gap
    if (delFrom.eq(delTo) && delFrom.current().isGap()) {
      // inside gap
      delFrom.current().shrink(delTo.getBlockOffset()-delFrom.getBlockOffset());
      return delFrom.getBlockOffset();
    }
    
    
    //if brace is multiple char it must be a comment because the above if
    //test guarrantees it can't be a gap.
    if (!delFrom.eq(delTo)) {
      _clipLeft(delFrom.getBlockOffset(), delFrom);
    }
    _clipRight(delTo.getBlockOffset(), delTo);      
    
    if (!delFrom.atStart()) {
      delFrom.prev();
    }
    int delToSizeCurr;
    String delToTypeCurr;
    if (delTo.atEnd()) {
//      _updateBasedOnCurrentState();
      delFrom.setTo(delTo);
      return 0;
    }
    else {
      delToSizeCurr = delTo.current().getSize();
      delToTypeCurr = delTo.current().getType();
    }
    
    //get info on previous item.
    delTo.prev(); //get stats on previous item
    
    int delToSizePrev;
    String delToTypePrev;
    if (delTo.atStart()) { //no previous item, can't be at end
      delTo.next();
//      _updateBasedOnCurrentStateHelper(delFrom);
      delFrom.setTo(delTo);
      return 0;
    }
    else {
      delToSizePrev = delTo.current().getSize();
      delToTypePrev = delTo.current().getType();
    }
    delTo.next(); //put delTo back on original node
    
//    _updateBasedOnCurrentState();
    
    int temp = _calculateOffset(delToSizePrev,delToTypePrev,
                                delToSizeCurr, delToTypeCurr,
                                delTo);
    delFrom.setTo(delTo);
    return temp;
  }
  
  /**
  * Gets rid of extra text.
  * Because collapse cannot get rid of all deletion text as some may be
  * only partially spanning a token, we need to make sure that
  * this partial span into the non-collapsed token on the left is removed.
  */
  private void _clipLeft(int offset, 
                         TokenList.Iterator
                         copyCursor)
  {
    if (copyCursor.atStart()) {
      return;
    }
    else if (offset == 0) {
      copyCursor.remove();
    }
    else if (copyCursor.current().isGap()) {
      int size = copyCursor.current().getSize();
      copyCursor.current().shrink(size-offset);
    }
    else if (copyCursor.current().isMultipleCharBrace()) {
      if (offset != 1) {
        throw new IllegalArgumentException("Offset incorrect");
      }
      else {
        String type = copyCursor.current().getType();
        String first = type.substring(0,1);
        copyCursor.current().setType(first);
      }
    }
    else {
      throw new IllegalArgumentException("Cannot clip left.");
    }
  }
  
  
  /**
  * Gets rid of extra text.
  * Because collapse cannot get rid of all deletion text as some may be
  * only partially spanning a token, we need to make sure that
  * this partial span into the non-collapsed token on the right is removed.
  */
  private void _clipRight(int offset, TokenList.Iterator
                          copyCursor)
  {
    if (copyCursor.atEnd()) {
      return;
    }
    else if (offset == 0) {
      return;
    }
    else if (offset == copyCursor.current().getSize()) {
      copyCursor.remove();
    }
    else if (copyCursor.current().isGap()) {
      copyCursor.current().shrink(offset);
    }
    else if (copyCursor.current().isMultipleCharBrace()) {
      if (offset != 1) {
        throw new IllegalArgumentException("Offset incorrect");
      }
      else {
        String type = copyCursor.current().getType();
        String second = type.substring(1,2);
        copyCursor.current().setType(second);
      }
    }
    else {
      throw new IllegalArgumentException("Cannot clip left.");
    }
  }
  
  /**
  *By contrasting the delTo token after the walk to what it was before the
  *walk we can see how it has changed and where the offset should go.
  *
  *Prev is the item previous to the current cursor
  *Current is what the current cursor
  *delTo is where current is pointing at this moment in time.
  */
  private int _calculateOffset(int delToSizePrev, String delToTypePrev,
                               int delToSizeCurr, String delToTypeCurr,
                               TokenList.Iterator delTo)
  {      
    int offset;
    int delToSizeChange = delTo.current().getSize();
    String delToTypeChange = delTo.current().getType();
    
    //1)if there was a gap previous to the gap at delTo delTo should be
    //augmented by its size, and that size is the offset.
    //2)if the gap was not preceeded by a gap then it would not need to
    //be shrunk
    if (delTo.atEnd()) {
      throw new IllegalArgumentException("Shouldn't happen");
    }
    if (delTo.current().isGap()) {
      return delToSizeChange - delToSizeCurr;
    }
    //this means that the item at the end formed a double brace with the
    //item that the delete left preceeding it. /dddddd*
    
    //the final item shrunk. This can only happen if the starting item
    //stole one of its braces: /ddddd*/
    //or if it was a double brace that had to get broken because it was
    //now commented or no longer has an open block
    
    //EXAMPLES: /*___*/  becoming */
    //          /*___*/  delete the first star, through the spaces to get
    //                   /*/
    //         //*__\n// becoming //*__//, the // is broken
    //         //*__\n// becoming ////   , the // is broken
    //THIS MUST HAVE THE previous items size and type passed in from
    //before the update. This way we know how it's changing too.
         
    // In this if clause, special characters are initially separated by some text
    // (represented here as ellipses), and when the text is deleted, the special
    // characters come together.  Sometimes, this breaks up the second token if
    // it is a multiple character brace.  Each in-line comment demonstrates
    // the individual case that occurs and for which we check with this if.
    // In this branch, both the cursor is off and the offset is also not correct.
    if (((delToTypePrev.equals("/")) &&
         // /.../* => //-*
         ((delToTypeCurr.equals("/*") && 
           _checkPrevEquals(delTo,"//")) ||
          // /...// => //-/
          (delToTypeCurr.equals("//") &&
           _checkPrevEquals(delTo,"//")))) ||
        
        ((delToTypePrev.equals("*")) &&
         // *.../* => */-*
         ((delToTypeCurr.equals("/*") && 
           _checkPrevEquals(delTo,"*/")) ||
          // *...// => */-/
          (delToTypeCurr.equals("//") &&
           _checkPrevEquals(delTo,"*/")))) ||
        
        ((delToTypePrev.equals("\\")) &&
         // \...\\ => \\-\
         ((delToTypeCurr.equals("\\\\") && 
           _checkPrevEquals(delTo,"\\")) ||
          // \...\' => \\-'
          (delToTypeCurr.equals("\\'") &&
           _checkPrevEquals(delTo,"'")) ||
          // \...\" => \\-"
          (delToTypeCurr.equals("\\\"") &&
           _checkPrevEquals(delTo,"\""))))) {
             delTo.prev();
             offset = 1;
           }
    // In this branch, the cursor is on the right token, but the offset is not correct. 
    else if (((delToTypePrev.equals("/")) &&
              // /-*/
              ((delToTypeCurr.equals("*/") && 
                delTo.current().getType().equals("/*")) ||
               (delToTypeCurr.equals("*") &&
                delTo.current().getType().equals("/*")) ||
               (delToTypeCurr.equals("/") &&
                delTo.current().getType().equals("//")))) ||
             
             ((delToTypePrev.equals("*")) &&
              ((delToTypeCurr.equals("/") &&
                delTo.current().getType().equals("*/")))) ||
             
             ((delToTypePrev.equals("\\")) &&
              ((delToTypeCurr.equals("\\") &&
                delTo.current().getType().equals("\\\\")) ||
               (delToTypeCurr.equals("'") &&
                delTo.current().getType().equals("\\'")) ||
               (delToTypeCurr.equals("\"") &&
                delTo.current().getType().equals("\\\""))))) {
                  offset = 1;
                }
    // otherwise, we're on the right token and our offset is correct 
    // because no recombinations occurred
    else {
      offset = 0;
    }
    return offset;
  }
  
  /**
  * Checks if the previous token is of a certain type.
  * @param delTo the cursor for calling prevItem on
  * @param match the type we want to check
  * @return true if the previous token is of type match
  */
  private boolean _checkPrevEquals(TokenList.Iterator delTo,
                                   String match)
  {
    if (delTo.atFirstItem() || delTo.atStart()) {
      return false;
    }
    return delTo.prevItem().getType().equals(match);
  }
  
  /**In order to interface with the ReducedModelComment two functions are
  provided. One resets the walker and the other will both move the cursor
  by x and return the state at that new location.
  Once the new value has returned all new calculations will be relative to
  that spot until the walker is reset to the _cursor.
  */
  
  /**
  *Returns the state at the relLocation, where relLocation is the location
  *relative to the walker
  *@param relLocation distance from walker to get state at.
  */
  ReducedModelState stateAtRelLocation(int relLocation) {
    _walker.move(relLocation);
    return _walker.getStateAtCurrent();
  }
  
  /**
  *Resets the walker to the current position in document
  */
  void resetLocation() {
    _walker.dispose();
    _walker = _cursor.copy();
  }
  
  ReducedToken current() {
    return _cursor.current();
  }
  
  void  next() {
    _cursor.next();
  }
  void prev() {
    _cursor.prev();
  }
  /**
   * Dist to Previous newline will be -1 if no newline.
   */
  void getDistToPreviousNewline(IndentInfo braceInfo) {
    braceInfo.distToPrevNewline = _getDistToPreviousNewline(_cursor.copy());
    braceInfo.distToNewline = braceInfo.distToPrevNewline;
    return;
  }
  
  /**
   *returns distance to after newline
   */
  private int _getDistToPreviousNewline(TokenList.Iterator copyCursor) {
    int walkcount = copyCursor.getBlockOffset();
    if (!copyCursor.atStart()) {
      copyCursor.prev();
    }
    while ((!copyCursor.atStart()) &&
           (!(copyCursor.current().getType().equals("\n"))))
           {
             //  copyCursor.current().getState() == FREE))){
             walkcount += copyCursor.current().getSize();
             copyCursor.prev();
           }
    
    if (copyCursor.atStart()) {
      return -1;
    }
    return walkcount;
  }
  
  void getDistToIndentNewline(IndentInfo braceInfo) {
    int walkcount = -1;
    TokenList.Iterator copyCursor = _cursor.copy();
    
    if (braceInfo.distToBrace == -1 || copyCursor.atStart()) { // no brace
      return;
    }
    
    copyCursor.move(-braceInfo.distToBrace);    
    walkcount = _getDistToPreviousNewline(copyCursor);
    
    if (walkcount == -1) {
      braceInfo.distToNewline = -1;
    }
    else {
      braceInfo.distToNewline = walkcount + braceInfo.distToBrace;
    }
    return;
  }
  
  /**
  * Gets distance to previous newline, relLoc is the distance
  * back from the cursor that we want to start searching.
  */
  public int getDistToPreviousNewline(int relLoc) {
    TokenList.Iterator copyCursor = _cursor.copy();
    copyCursor.move(-relLoc);
    int dist = _getDistToPreviousNewline(copyCursor);
    copyCursor.dispose();
    if(dist == -1) {
      return -1;
    }
    return dist + relLoc;
  }
  
  /**
  * returns the distance to the space before the next newline
  * returns the distance to the end of the document if there is no newline
  */
  public int getDistToNextNewline() {
    TokenList.Iterator copyCursor = _cursor.copy();
    if(copyCursor.atStart()) {
      copyCursor.next();
    }
    if(copyCursor.atEnd() || copyCursor.current().getType().equals("\n")) {
      return 0;
    }
    int walkcount = copyCursor.current().getSize() - _cursor.getBlockOffset();
    copyCursor.next();
    
    while ((!copyCursor.atEnd()) &&
           (!(copyCursor.current().getType().equals("\n")))) 
    {
      //copyCursor.current().getState() == FREE))){
      walkcount += copyCursor.current().getSize();
      copyCursor.next();
    }
    return walkcount;
  }
}