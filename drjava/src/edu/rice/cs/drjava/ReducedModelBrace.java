package edu.rice.cs.drjava;

import gj.util.Stack;
import gj.util.Vector;

/**
 * This class provides an implementation of the BraceReduction
 * interface for brace matching.  In order to correctly match, this class
 * keeps track of what is commented (line and block) and what is inside
 * double quotes and keeps this in mind when matching.
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
 * <li> There is no nesting of comment open characters. If // is encountered
 *      in the middle of a comment, it is treated as two separate slashes.
 *      Similar for /*.
 * </ol>
 * @version $Id$
 * @author JavaPLT
 */
public class ReducedModelBrace implements ReducedModelStates {
  
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
  ReducedModelControl parent;
  
  /**
   * Constructor.  Creates a new reduced model with the cursor
   * at the start of a blank "page."
   */
  public ReducedModelBrace(ReducedModelControl parent) {
    _braces = new TokenList();
    _cursor = _braces.getIterator();
    // we should be pointing to the head of the list
    _cursor.setBlockOffset(0);
    this.parent = parent;
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
    if (!it.atStart())
      it.prev();
    
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
      
      if (!_cursor.atStart() && !_cursor.atEnd() && (tmp == _cursor.current())) {
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
      case '{':
      case '}':
      case '[':
      case ']':
      case '(':
      case ')':
        _insertBrace("" + ch);
      break;
      default:
        _insertGap(1);
      break;
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
  private void _insertGap( int length ) {
    //0 - a
    if (_cursor.atStart()) {
      if (_gapToRight()) {
        _cursor.next();
        _augmentCurrentGap(length); //increases gap and moves offset
      }
      else {
        _insertNewGap(length);//inserts gap and goes to next item
      }
    }
    //0 - b
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
            !_cursor.atFirstItem() &&_cursor.prevItem().isGap());
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
    _cursor.insert(new Gap(length,FREE));
    _cursor.next();
    _cursor.setBlockOffset(0);
  }
 
  /**
   * Helper function for top level brace insert functions.
   *
   * <OL>
   *  <li> at Head: not special case
   *  <li> at Tail: not special case
   *  <li> between two things (offset is 0):
   *      <ol>
   *       <li> insert brace
   *       <li> move next
   *       <li> offset = 0
   *      </ol>
   *  <li> inside gap:
   *      <ol>
   *       <li> shrink gap to size of gap - offset.
   *       <li> insert brace
   *       <li> insert gap the size of offset.
   *       <li> move next twice
   *       <li> offset = 0
   *      </ol>
   * <li> inside multiple char brace:
   *      <ol>
   *       <li> break
   *       <li> insert brace
   *      </ol>
   * </OL>
   * @param text the String type of the brace to insert
   */
  private void _insertBrace(String text) {
    if (_cursor.atStart() || _cursor.atEnd()) {
      _cursor.insertNewBrace(text); // inserts brace and goes to next
    }
    else if (_cursor.current().isGap()) {
      _cursor.insertBraceToGap(text);
    }
    
    else {
      _cursor.insertNewBrace(text);
    }
  }
  
  
  
  
  /**
   * Updates ReducedModelBrace to reflect cursor movement.
   * Negative values move left from the cursor, positive values move
   * right.  All functionality has been refactored into TokenList.
   * @param count indicates the direction and magnitude of cursor movement
   */
  public void move(int count) {
    _cursor.move(count);
  }
  
  /**
   * Updates ReducedModelBrace to reflect text deletion.
   * Negative values mean text left of the cursor, positive values mean
   * text to the right.  All functionality has been refactored into TokenList.
   */
  public void delete( int count ) {
    if (count == 0) {
      return;
    }
    _cursor.delete(count);
    return;
  }
  

  /** If the current brace is a /, a *, a // or a \n, it's not matchable.
  *  This means it is ignored on balancing and on next/prev brace finding.
  *  All other braces are matchable.
  */
  private boolean _isCurrentBraceMatchable
    (TokenList.Iterator copyCursor)
  {
    return _isBraceMatchable(copyCursor.current());
  }
  
  private boolean _isBraceMatchable(ReducedToken token) {
    String type = token.getType();
    
    return (!token.isGap() &&
            !(type.equals("/")    ||
              type.equals("*")    ||
              type.equals("\n")   ||
              type.equals("//")   ||
              type.equals("\\")   ||
              type.equals("\\\\") ||
              type.equals("\\\"")) &&
            !token.isShadowed());
  }
  
  /**
   *Returns distance from current location of cursor to the location of the
   *previous significant brace.
   *ex. (...|)  where | signifies the cursor. previousBrace returns 4 because
   *it goes to the spot behind the (.
   * /|* returns this brace since you're in the middle of it and going
   *backward can find it.
   */
  public int previousBrace() {
    int relDistance = 0;
    int dist = 0;
    resetLocation();//reset the interface to the comment model
    
    TokenList.Iterator copyCursor = _cursor.copy();
    if (!copyCursor.atStart())
      copyCursor.prev();
    if (copyCursor.atStart()) {
      copyCursor.dispose();
      return -1;
    }
    //initialize the size.
    dist += _cursor.getBlockOffset();
    relDistance = dist;
    
    // if we're in the middle of the first brace element, we're
    // not going to find any previous braces
    
    while (!copyCursor.atStart()) {
      if (!copyCursor.current().isGap()) {
        if (stateAtRelLocation(-relDistance) == FREE) {
          copyCursor.dispose();
          return dist + copyCursor.current().getSize();
        }
        relDistance = 0;
      }
      
      dist += copyCursor.current().getSize();
      relDistance += copyCursor.current().getSize();
      copyCursor.prev();
    }
    copyCursor.dispose();
    return -1;
  }
  
  
  /**
   *Goes to the location before the brace. |...( where | is the cursor,
   *returns three since it is three moves to the location of the (
   *NOTE: /|* returns the next brace. It does not return this brace because
   *you are past it.
   */
  public int nextBrace() {
    int relDistance = 0;
    int dist = 0;
    TokenList.Iterator copyCursor = _cursor.copy();
    
    resetLocation();
    
    if ( copyCursor.atStart())
      copyCursor.next();
    if (_cursor.getBlockOffset() > 0){
      dist = copyCursor.current().getSize() - _cursor.getBlockOffset();
      relDistance = dist;
      copyCursor.next();
    }
    // there are no braces on the last brace element - it's empty
    while (!copyCursor.atEnd() ){
      if (!copyCursor.current().isGap()) {
        if (stateAtRelLocation(relDistance) ==
            FREE){
              copyCursor.dispose();
              return dist;
            }
        relDistance = 0;
      }
      relDistance += copyCursor.current().getSize();
      dist += copyCursor.current().getSize();
      copyCursor.next();
    }
    copyCursor.dispose();
    return -1;
  }
  
  /**
   * If the current ReducedToken is an open significant brace and the
   * offset is 0 (i.e., if we're immediately left of said brace),
   * push the current Brace onto a Stack and iterate forwards,
   * keeping track of the distance covered.
   * - For every closed significant Brace, if it matches the top of the Stack,
   *   pop the Stack.  Increase the distance by the size of the Brace.
   *   If the Stack is Empty, we have a balance.  Return distance.
   *   If the closed Brace does not match the top of the Stack, return -1;
   *   We have an unmatched open Brace at the top of the Stack.
   * - For every open significant Brace, push onto the Stack.
   *   Increase distance by size of the Brace, continue.
   * - Anything else, increase distance by size of the ReducedToken, continue.
   */
  public int balanceForward() {
    Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
    TokenList.Iterator iter = _cursor.copy();
    int relDistance = 0;
    int distance = 0;
    
    resetLocation(); //resets location of ReducedModelComment
    
    if (iter.atStart())
      iter.next();
    
    // here we check to make sure there is an open significant brace
    // immediately to the right of the cursor
    if (!iter.atEnd() && openBraceImmediatelyRight()) {
      if (stateAtRelLocation(relDistance) == FREE) {
        relDistance = 0;
        // initialize the distance and the stack with the first brace,
        // the one we are balancing
        
        braceStack.push(iter.current());
        distance += iter.current().getSize();
        iter.next();
        
        // either we get a match and the stack is empty
        // or we reach the end of a file and haven't found a match
        // or we have a closed brace that doesn't have a match,
        //    so we abort
        while (!iter.atEnd() && !braceStack.isEmpty()) {
          if (!iter.current().isGap()) {
            if (stateAtRelLocation(relDistance) == FREE) {
              if (iter.current().isClosedBrace()) {
                ReducedToken popped = braceStack.pop();
                if (!iter.current().isMatch(popped)) {
                  iter.dispose();
                  return -1;
                }
              }
              //open
              else{
                braceStack.push(iter.current());
              }
            }
            relDistance = 0;
          }
          // no matter what, we always want to increase the distance
          // by the size of the token we have just gone over
          distance += iter.current().getSize();
          relDistance += iter.current().getSize();
          iter.next();
        }
        
        // we couldn't find a match
        if (!braceStack.isEmpty()) {
          iter.dispose();
          return -1;
        }
        // success
        else {
          iter.dispose();
          return distance;
        }
      }
      // not the right initial conditions 
    }
    iter.dispose();
    return -1;
  }
  
  public boolean openBraceImmediatelyRight() {
    return ((_cursor.getBlockOffset() == 0) && _cursor.current().isOpen() &&
            _isBraceMatchable(_cursor.current()));
  }
  
  public boolean closedBraceImmediatelyLeft() {
    return ((_cursor.getBlockOffset() == 0) && _cursor.prevItem().isClosed() &&
            _isBraceMatchable(_cursor.prevItem()));
  }
  
  /* 
   * If the previous ReducedToken is a closed significant brace,
   * offset is 0 (i.e., if we're immediately right of said brace),
   * push the previous Brace onto a Stack and iterate backwards,
   * keeping track of the distance covered.
   * - For every open significant Brace, if it matches the top of the Stack,
   *   pop the Stack.  Increase the distance by the size of the Brace.
   *   If the Stack is Empty, we have a balance.  Return distance.
   *   If the open Brace does not match the top of the Stack, return -1;
   *   We have an unmatched closed Brace at the top of the Stack.
   * - For every closed significant Brace, push onto the Stack.
   *   Increase distance by size of the Brace, continue.
   * - Anything else, increase distance by size of the ReducedToken, continue.
   */
  public int balanceBackward() { 
    Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
    TokenList.Iterator iter = _cursor.copy();
    resetLocation();
    int relDistance = 0;
    int distance = 0;
    if (iter.atStart() || 
        iter.atFirstItem() ||
        !closedBraceImmediatelyLeft()) 
    {
      iter.dispose();
      return -1;
    }
    
    iter.prev();
    relDistance = iter.current().getSize();
    // here we check to make sure there is an open significant brace
    // immediately to the right of the cursor
    if (iter.current().isClosedBrace()) {
      if(stateAtRelLocation(-relDistance) == FREE) {
        // initialize the distance and the stack with the first brace,
        // the one we are balancing
        
        braceStack.push(iter.current());
        distance += iter.current().getSize();
        iter.prev();
        if (!iter.atStart()) {
          distance += iter.current().getSize();
          relDistance = iter.current().getSize();
        }
      }
      else {
        iter.dispose();
        return -1;
      }
    }
    else {
      iter.dispose();
      return -1;
    }
    // either we get a match and the stack is empty
    // or we reach the start of a file and haven't found a match
    // or we have a open brace that doesn't have a match,
    // so we abort
    while (!iter.atStart() && !braceStack.isEmpty()) {
      if (!iter.current().isGap()) {
        if (stateAtRelLocation(-relDistance) ==
            FREE) {
              // open
              if (iter.current().isOpenBrace()) {
                ReducedToken popped = braceStack.pop();
                if (!iter.current().isMatch(popped)){
                  iter.dispose();
                  return -1;
                }
              }
              // closed
              else {
                braceStack.push(iter.current());
              }
            }
        relDistance = 0;
      }
      // no matter what, we always want to increase the distance
      // by the size of the token we have just gone over
      iter.prev();
      if (!iter.atStart() && !braceStack.isEmpty()) {
        distance += iter.current().getSize();
        relDistance += iter.current().getSize();
      }
    }
    
    // we couldn't find a match
    if (!braceStack.isEmpty()) {
      iter.dispose();
      return -1;
    }
    // success
    else {
      iter.dispose();
      return distance;
    }
  }
  
  ReducedModelState stateAtRelLocation(int relDistance) {
    return parent.stateAtRelLocation(relDistance);
  }
  
  void resetLocation() {
    parent.resetLocation();
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
  
  /*
  *The braceInfo.distToNewline holds the distance to the previous newline.
  *To find the enclosing brace one must first move past this newline.
  *The distance held in this variable is only to the space in front of the
  *newline hence you must move back that distance + 1.
  */
  protected void getDistToEnclosingBrace(IndentInfo braceInfo) {
    Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
    TokenList.Iterator iter = _cursor.copy();
    resetLocation();
    //this is the distance to in front of the previous newline.
    int relDistance = braceInfo.distToNewline + 1;
    int distance = relDistance;
    
    if (braceInfo.distToNewline == -1) {
      iter.dispose();
      return;
    }
    //move to the proper location, then add the rest of the block
    // and go to the previous.
    iter.move(-braceInfo.distToNewline - 1);
    relDistance += iter.getBlockOffset();
    distance += iter.getBlockOffset();
    
    //reset the value of braceInfo signiling the necessary newline has
    //not been found.
    braceInfo.distToNewline = -1;
    
    if (iter.atStart() || iter.atFirstItem()) {
      iter.dispose();
      return;
    }
    
    iter.prev();
    
    // either we get a match and the stack is empty
    // or we reach the start of a file and haven't found a match
    // or we have a open brace that doesn't have a match,
    // so we abort
    while (!iter.atStart()) {
      
      distance += iter.current().getSize();
      relDistance += iter.current().getSize();
      
      if (!iter.current().isGap()) {
        
        if (stateAtRelLocation(-relDistance) == FREE) {
              // open
              if (iter.current().isOpenBrace()) {
                if (braceStack.isEmpty()) {
                  braceInfo.braceType = iter.current().getType();
                  braceInfo.distToBrace = distance;
                  iter.dispose();
                  return;
                }
                ReducedToken popped = braceStack.pop();
                if (!iter.current().isMatch(popped)){
                  iter.dispose();
                  return;
                }
              }
              // closed
              else {
                braceStack.push(iter.current());
              }
            }
        relDistance = 0;
      }
      // no matter what, we always want to increase the distance
      // by the size of the token we have just gone over
      iter.prev();
    }
    
    iter.dispose();
    return;
  }
}
