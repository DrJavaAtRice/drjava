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

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import java.util.Stack;

/** Keeps track of the true braces (i.e., "() {}[]"). This reduced sub-model is used to balance braces for both 
  * indenting and highlighting purposes.  For example, when the user's caret is immediately after a closing brace, 
  * this allows the DefinitionsPane to produced a highlight extending from the closing brace to its match.
  * @version $Id: ReducedModelBrace.java 5675 2012-08-16 21:25:57Z rcartwright $
  * @author JavaPLT
  */
public class ReducedModelBrace extends AbstractReducedModel {
  
  private volatile ReducedModelControl _parent;  // contains the walker which is moved by moveWalkerGetState
  
  public ReducedModelBrace(ReducedModelControl parent) {
    super();
    _parent = parent;
  }
  
  public void insertChar(char ch) {
    switch(ch) {
      case '{':
      case '}':
      case '[':
      case ']':
      case '(':
      case ')':
        _insertBrace(String.valueOf(ch));
        break;
      default:
        _insertGap(1);
        break;
    }
  }
  
  /** Helper function for top level brace insert functions.
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
    if (_cursor.atStart() || _cursor.atEnd()) _cursor.insertNewBrace(text); // inserts brace and goes to next
    else if (current().isGap()) _cursor.insertBraceToGap(text);
    else _cursor.insertNewBrace(text);
  }
  
  /** Inserts a gap between the characters in a multiple character brace. However, since ReducedModelBrace doesn't keep
    * track of comment braces and escape sequences, we just throw an exception since the condition in insertGap 
    * that spawns this method doesn't arise.
    */
  protected void insertGapBetweenMultiCharBrace(int length) {
    throw new RuntimeException("ReducedModelBrace does not keep track of multi-character braces.");
  }
  
  /** Updates ReducedModelBrace to reflect cursor movement. Negative values move left from the cursor, positive 
    * values move right.  All functionality has been refactored into TokenList.  ASSUMES that count is within range.
    * @param count indicates the direction and magnitude of cursor movement
    */
  public void move(int count) { _cursor.move(count); }
  
  /** Updates ReducedModelBrace to reflect text deletion. Negative values mean text left of the cursor, positive 
    * values mean text to the right.  All functionality has been refactored into TokenList.  Assumes that count is 
    * with range.
    */
  public void delete(int count) {
    if (count == 0) return;
    _cursor.delete(count);
    return;
  }
  
  /** If the current brace is a /, a *, a // or a \n, it's not matchable. This means it is ignored on balancing and
    * on next/prev brace finding.  All other braces are matchable.
    */
  private boolean _isCurrentBraceMatchable() { return _cursor.current().isMatchable(); }
  
  public boolean isShadowed() { return _parent.isShadowed(); }
  
  /** Returns distance from current location of cursor to the location of the previous significant brace including 
    * opening comment braces (but not closing ones since they are not "FREE").  For example,
    * given "(...|)" where | signifies the cursor, previousBrace returns 4 because it goes to the position preceding the (.
    * Given "* /|*", it returns 1 (the distance to the position of this brace) since you're in the middle of it and going 
    * backward finds it.
    */
  public int previousBrace() {
    int relDistance;
    int dist = 0;
    resetWalkerLocationToCursor(); //reset the interface to the comment model
    
    TokenList.Iterator copyCursor = _cursor.copy();
    if (! copyCursor.atStart()) copyCursor.prev();
    
    if (copyCursor.atStart()) {
      copyCursor.dispose();
      return -1;
    }
    
    //initialize the size.
    dist += _cursor.getBlockOffset();
    relDistance = dist;
    
    // if we're in the middle of the first brace element, we're not going to find any previous braces
    
    while (! copyCursor.atStart()) {
      if (! copyCursor.current().isGap()) {
        if (moveWalkerGetState(-relDistance) == FREE) {
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
  
  /** Determines the distance to the location before the next open brace. For example, |...( where | is the cursor,
    * returns 3 since it is 3 moves to the position preceding the (.  NOTE: /|* returns the next brace. It does not 
    * return this brace because you are past it.
    */
  public int nextBrace() {
    int relDistance = 0;
    int dist = 0;
    TokenList.Iterator copyCursor = _cursor.copy();
    
    resetWalkerLocationToCursor();
    
    if (copyCursor.atStart()) copyCursor.next();
    int offset = getBlockOffset();
    if (offset > 0) {
      dist = copyCursor.current().getSize() - offset;
      relDistance = dist;
      copyCursor.next();
    }
    // there are no braces on the last brace element - it's empty
    while (! copyCursor.atEnd()) {
      if (! copyCursor.current().isGap()) {
        if (moveWalkerGetState(relDistance) == FREE) {
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
  
  /** If the current ReducedToken is an open significant brace and the offset is 0 (i.e., if we're immediately left of 
    * said brace), push the current Brace onto a Stack and iterate forwards, keeping track of the distance covered.
    * - For every closed significant Brace, if it matches the top of the Stack, pop the Stack.  Increase the distance 
    *   by the size of the Brace. If the Stack is Empty, we have a balance.  Return distance.  If the closed Brace does 
    *   not match the top of the Stack, return -1; We have an unmatched open Brace at the top of the Stack.
    * - For every open significant Brace, push onto the Stack.  Increase distance by size of the Brace, continue.
    * - Anything else, increase distance by size of the ReducedToken, continue.
    */
  public int balanceForward() {
//    System.err.println("-------------------------------------------");
    resetWalkerLocationToCursor();
//    System.err.println("Balancing forward with iterator cursor at " + absOffset() + " and walker at " + _parent.walkerOffset());
    
    Stack<Brace> braceStack = new Stack<Brace>();
    TokenList.Iterator iter = _cursor.copy();
    
    if (! openBraceImmediatelyLeft() || isShadowed()) {
//      System.err.println("openBraceImmediatelyLeft(): " + openBraceImmediatelyLeft());
      iter.dispose();
//      System.err.println("! atStart, atFirstItem, or no closed brace");
//      System.err.println("balanceForward immediately aborted; returning -1");
      return -1;
    }
    
    iter.prev();
    ReducedToken curToken = iter.current();
    
    assert curToken instanceof Brace; // In fact, it is a significant matchable open brace.
    
//    int openBraceDistance = - curToken.getSize();
    
//    moveWalkerGetState(openBraceDistance);
    braceStack.push((Brace) curToken);
    iter.next();  // undoes iter.prev() above
//    moveWalkerGetState(-openBraceDistance);
    
    int relDistance = 0;  // distance from iter position to walker position
    int distance = 0;     // distance from _cursor position to iter position
    
    /* Loop until either:
     * (i)   we get a match and the stack is empty (success);
     * (ii)  we reach the end of a file and haven't found a match and abort; or
     * (iii) we reach a close brace that doesn't have a match and abort.
     */
    while (! iter.atEnd() && ! braceStack.isEmpty()) {
      curToken = iter.current(); // a ReducedToken is either a Gap or a Brace
      if (! curToken.isGap()) {  // curToken is a Brace
        Brace curBrace = (Brace) curToken;
        ReducedModelState curBraceState = moveWalkerGetState(relDistance); // walker moves right
        relDistance = 0;
//        System.err.println("curBrace = " + curBrace + "curBraceState = " + curBraceState);
//        System.err.println("Walker is now at offset " + _parent.walkerOffset() + " distance = " + distance);
        if (curBraceState == FREE && ! curToken.isCommentStart()) {  // not shadowed
//          System.err.println(curBrace + " is not shadowed");
          // check for closed brace
          if (curBrace.isClosedBrace()) {
//            System.err.println(curBrace + " is CLOSED at distance " + distance);
            Brace popped = braceStack.pop();
            if (! curBrace.isMatch(popped)) {
              iter.dispose();
//              System.err.println("! encountered closed brace that didn't match");
//              System.err.println("! encountered closed brace " + curBrace + " that didn't match; returning -1");
              return -1;
            }
          }
          // otherwise, this must be an open brace
          else {
            braceStack.push(curBrace);
//            System.err.println(curBrace + " pushed on stack");
          }
        }
//        else System.err.println(curToken + " at distance " + distance + " is shadowed");
      }
      // increment distances of size of current token
      int size = curToken.getSize();
      distance += size;     
      relDistance += size;
//      System.err.println("distance " + distance + " was incremented by " + size);
      
      iter.next();
    }
    
    // check if we exited because of failure
    if (! braceStack.isEmpty()) {
      iter.dispose();
//      System.err.println("Unmatched braces in stack: balancebackward returning -1");
//      System.err.println("! ran to end of file. distance: " + distance);
      return -1;
    }
    // success
    else {
      iter.dispose();
//      System.err.println("balancebackward returning " + distance);
      return distance;
    }
  }
  
//  /** This is no longer used internally -- highlight is always started on left. */
//  public boolean openBraceImmediatelyRight() {
//    if (_cursor.atEnd()) {
//      return false;
//    }
//    else {
//      return ((_cursor.getBlockOffset() == 0) && _cursor.current().isOpen() &&
//              _isCurrentBraceMatchable());
//    }
//  }
  
  public boolean openBraceImmediatelyLeft() {
    if (_cursor.atStart() || _cursor.atFirstItem()) return false;
    else {
      int offset = getBlockOffset();
      prev();  // does not affect offset
      assert offset == getBlockOffset();
//      System.err.println(" +  openBraceImmediatelyLeft() {");
//      System.err.println("  getBlockOffset(): " + getBlockOffset());
//      System.err.println("  current().isOpen(): " + current().isOpen());
//      System.err.println("  _isCurrentBraceMatchable(): " + _isCurrentBraceMatchable());
//      System.err.println("  }");

      boolean isLeft = (getBlockOffset() == 0 && current().isOpen() && _isCurrentBraceMatchable());
//      System.err.println("In openBraceLeft, token to left: " + _cursor);
      next();
      return isLeft;
    }
  }
  
  public boolean closedBraceImmediatelyLeft() {
    if (_cursor.atStart() || _cursor.atFirstItem()) return false;
    else {
      int offset = getBlockOffset();
      prev();  // does not affect block offset
      assert offset == getBlockOffset();
//      System.err.println(" +  closedBraceImmediatelyLeft() {");
//      System.err.println("  getBlockOffset(): " + getBlockOffset());
//      System.err.println("  current().isClosed(): " + _current().isClosed());
//      System.err.println("  _isCurrentBraceMatchable(): " + _isCurrentBraceMatchable());
//      System.err.println("  }");

      boolean isLeft = (offset == 0 && current().isClosed() && _isCurrentBraceMatchable());
//      System.err.println("In closedBraceLeft, token to left: " + _cursor);
      next();  // restore current token
      return isLeft;
    }
  }
  
  /* If the previous ReducedToken is a closed significant brace, offset is 0 (i.e., if we're immediately right of said
   * brace), push the previous Brace onto a Stack and iterate backwards, keeping track of the distance covered.
   * - For every open significant Brace, if it matches the top of the Stack, pop the Stack.  Increase the distance by
   *   the size of the Brace. If the Stack is Empty, we have a balance.  Return distance. If the open Brace does not 
   *   match the top of the Stack, return -1; We have an unmatched closed Brace at the top of the Stack.
   * - For every closed significant Brace, push onto the Stack. Increase distance by size of the Brace, continue.
   * - Anything else, increase distance by size of the ReducedToken, continue.
   */
  public int balanceBackward() {
//    System.err.println("-------------------------------------------");
    
    Stack<Brace> braceStack = new Stack<Brace>();
    TokenList.Iterator iter = _cursor.copy();
    resetWalkerLocationToCursor();  // _walker aligned to _cursor
//    System.err.println("Balancing backward with iterator cursor at " + absOffset() + " and walker at " + _parent.walkerOffset());
    
    if (! closedBraceImmediatelyLeft() || isShadowed()) {
//      System.err.println("closedBraceImmediatelyLeft(): " + closedBraceImmediatelyLeft());
      iter.dispose();
//      System.err.println("balanceBackward immediately aborted; returning -1");
      return -1;
    }
    
    int relDistance = 0; // distance from iter position to walker position
    int distance = 0;    // distance from iter position to _cursor position
    
    // move one token left, changing iter but not walker
    iter.prev();   
    assert iter.current() instanceof Brace;  // In fact, it is a significant closed brace.
    
    /* We loop until:
     * (i)   we get a match and the stack is empty and report success
     * (ii)  we reach the start of a file and haven't found a match and aborrt
     * (iii) we reach an open brace that doesn't have a match and abort
     */
    do {
      ReducedToken curToken = iter.current();
      int size = curToken.getSize();
      distance += size;
      relDistance += size;
//      System.err.println("distance " + distance + " was incremented by " + size);
      
      if (! curToken.isGap()) {  // curToken is a Brace
        Brace curBrace = (Brace) curToken;
        ReducedModelState curBraceState = moveWalkerGetState(- relDistance);  // walker moves left
        relDistance = 0;
//        System.err.println("curBrace = " + curBrace + "curBraceState = " + curBraceState);
//        System.err.println("Walker is now at offset " + _parent.walkerOffset() + " distance = " + distance);
        if (curBraceState == FREE && ! curToken.isCommentStart()) { // curBrace NOT shadowed
//          System.err.println(curBrace + " is not shadowed");
          if (curBrace.isOpenBrace()) {
//            System.err.println(curBrace + " is OPEN at distance " + distance);
            Brace popped = braceStack.pop();
            if (! curBrace.isMatch(popped)) {
              iter.dispose();
//              System.err.println("! encountered open brace " + curBrace + " that didn't match; returning -1");
              return -1;
            }
          }
          else { // curBrace is closed
//            System.err.println(curBrace + " pushed on stack");
            braceStack.push(curBrace);
          }
        }
      }
//      else System.err.println(curToken + " at distance " + distance + " is shadowed");
      iter.prev();
    }
    while (! iter.atStart() && ! braceStack.isEmpty());
    
    
    // test to see if we exited without a match
    if (! braceStack.isEmpty()) {
      iter.dispose();
//      System.err.println("! ran to end of brace stack");
//      System.err.println("Unmatched braces in stack: balancebackward returning -1");
      return -1;
    }
    // success
    else {
      iter.dispose();
//      System.err.println("balancebackward returning " + distance);
      return distance;
    }
  }
  
  protected ReducedModelState moveWalkerGetState(int relDistance) { return _parent.moveWalkerGetState(relDistance); }
  
  protected void resetWalkerLocationToCursor() { _parent.resetLocation(); }
  
  /** Determines the brace (type and distance) enclosing the beginning of the current line (except the first line). The
    * matching brace obviously must appear on the preceding line or before.  To find the enclosing brace one must first 
    * move past this newline. If there is not a line enclosing brace, returns BraceInfo.NULL.  Usually called when 
    * distToStart() = 0.
    * Why not move the cursor to the beginning of the line and use getEnclosingBrace?
    */
  public BraceInfo _getLineEnclosingBrace() {
    Stack<Brace> braceStack = new Stack<Brace>();
    TokenList.Iterator iter = _cursor.copy();
    resetWalkerLocationToCursor();
    // this is the distance to front edge of the preceding newline.
    final int distToStart = _parent.getDistToStart();
//    System.err.println("_getLineEnclosingBrace.distToStart = " + distToStart);
    
    if (distToStart == -1) {
      iter.dispose();
      return BraceInfo.NULL;
    }
    
    int relDistance = distToStart + 1;  // distance to end of preceding line
    int distance = 1;                   // distance from right edge of newline
    
    // move to the proper location, then add the rest of the block and go to the previous.
    iter.move(-relDistance);
//    System.err.println("After moving to start, reduced model = " + _parent.simpleString());
    final int offset = iter.getBlockOffset();
    relDistance += offset;
    distance += offset;
    
    if (iter.atStart() || iter.atFirstItem()) { // no preceding brace exists
      iter.dispose();
      return BraceInfo.NULL;
    }
    
    iter.prev(); // move to reduced token preceding the newline.
    
    
    String braceType;
    
    // either we get a match and the stack is empty
    // or we reach the start of a file and haven't found a match
    // or we have a open brace that doesn't have a match,
    // so we abort
    while (! iter.atStart()) {
      
      ReducedToken curToken = iter.current();
      int size = curToken.getSize();
      distance += size;
//      System.err.println("Adding " + size + " to dist for token " + curToken);
      relDistance += size;
      
      if (! curToken.isGap()) {
        
        Brace curBrace = (Brace) curToken;
        
        if (moveWalkerGetState(-relDistance) == FREE && ! curToken.isCommentStart()) {
          // open
          if (curBrace.isOpenBrace()) {
            if (braceStack.isEmpty()) {
              braceType = curBrace.getType();
              // distance to brace == distance;
              iter.dispose();
              return new BraceInfo(braceType, distance);
            }
            Brace popped = braceStack.pop();
            if (! curBrace.isMatch(popped)) {
              iter.dispose();
              return BraceInfo.NULL;
            }
          }
          // closed
          else braceStack.push(curBrace);
        }
        relDistance = 0;
      }
      // no matter what, we always want to increase the distance
      // by the size of the token we have just gone over
      iter.prev();
    }
    
    // Enclosing brace not found
    iter.dispose();
    return BraceInfo.NULL;
  }
  
  /** Determines the brace enclosing the current location. */
  protected BraceInfo _getEnclosingBrace() {
    Stack<Brace> braceStack = new Stack<Brace>();
    TokenList.Iterator iter = _cursor.copy();
    resetWalkerLocationToCursor();
    int relDistance = 0;
    int distance = relDistance;
    
    // Move to the proper location, then add the rest of the block and go to the previous.
    
    int offset = iter.getBlockOffset();
    relDistance += offset;
    distance += offset;
    
    if (iter.atStart() || iter.atFirstItem()) {
      iter.dispose();
      return BraceInfo.NULL;
    }
    
    iter.prev();
    
    String braceType;
    
    // either we get a match and the stack is empty or we reach the start of a file and haven't found a match
    // or we have a open brace that doesn't have a match, so we abort
    while (! iter.atStart()) {
      
      ReducedToken curToken = iter.current();
      int size = curToken.getSize();
      distance += size;
      relDistance += size;
      
      if (! curToken.isGap()) {
        Brace curBrace = (Brace) curToken;
        if (moveWalkerGetState(-relDistance) == FREE && ! curToken.isCommentStart()) {
          // open
          if (curBrace.isOpenBrace()) {
            if (braceStack.isEmpty()) {
              braceType = curBrace.getType();
              iter.dispose();
              return new BraceInfo(braceType, distance);
            }
            Brace popped = braceStack.pop();
            if (! curBrace.isMatch(popped)) {
              iter.dispose();
              return BraceInfo.NULL;
            }
          }
          // closed
          else braceStack.push(curBrace);
        }
        relDistance = 0;
      }
      // no matter what, we always want to increase the distance
      // by the size of the token we have just gone over
      iter.prev();
    }
    
    iter.dispose();
    return BraceInfo.NULL;
  }  
}
