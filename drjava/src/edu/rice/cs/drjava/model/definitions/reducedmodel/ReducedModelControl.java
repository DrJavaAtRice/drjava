package edu.rice.cs.drjava.model.definitions.reducedmodel;

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
 * @author JavaPLT
 * @version $Id$
 */
public class ReducedModelControl implements BraceReduction {
  ReducedModelBrace rmb;
  ReducedModelComment rmc;
  int _offset;
  
  public ReducedModelControl() {
    rmb = new ReducedModelBrace(this);
    rmc = new ReducedModelComment();
  }
  
  public void insertChar(char ch) {
    rmb.insertChar(ch);
    rmc.insertChar(ch);
  }
  
  /**
  * <P>Updates the BraceReduction to reflect cursor movement.
  * Negative values move left from the cursor, positive values move
  * right. </P>
  * @param count indicates the direction and magnitude of cursor movement
  */
  public void move(int count) {
    rmb.move(count);
    rmc.move(count);
  }
  
  /**
  * <P>Update the BraceReduction to reflect text deletion.</P>
  * @param count indicates the size and direction of text deletion.
  * Negative values delete text to the left of the cursor, positive
  * values delete text to the right.
  */
  public void delete(int count) {
    rmb.delete(count);
    rmc.delete(count);
  }
  
  
  /**
  * <P>Finds the closing brace that matches the next significant
  * brace iff that brace is an open brace.</P>
  * @return the distance until the matching closing brace.  On
  * failure, returns -1.
  * @see #nextBrace()
  */
  public int balanceForward() {
    return rmb.balanceForward();
  }
  /**
  * <P>Finds the open brace that matches the previous significant
  * brace iff that brace is an closing brace.</P>
  * @return the distance until the matching open brace.  On
  * failure, returns -1.
  * @see #previousBrace()
  */
  public int balanceBackward() {
    return rmb.balanceBackward();
  }
  
  /**
  *This function returns the state at the relDistance, where relDistance
  *is relative to the last time it was called. You can reset the last
  *call to the current offset using resetLocation.
  */
  public ReducedModelState stateAtRelLocation(int relDistance) {
    return rmc.stateAtRelLocation(relDistance);
  }
  
  /**
  *This function resets the location of the walker in the comment list to
  *where the current cursor is. This allows the walker to keep walking and
  *using relative distance instead of having to rewalk the same distance
  *every call to stateAtRelLocation. It is an optimization.
  */
  void resetLocation() {
    rmc.resetLocation();
  }

  /**
   * Get the token currently pointed at by the cursor.
   * Because the reduced model is split into two reduced sub-models,
   * we have to check each sub-model first as each one has unique
   * information.  If we find a non-gap token in either sub-model
   * we want to return that.  Otherwise, we want to return a sort
   * of hybrid Gap of the two, i.e., a Gap where there are neither
   * special comment/quote tokens nor parens/squigglies/brackets.
   * @return a ReducedToken representative of the unified reduced
   * model
   */
  public ReducedToken currentToken() {
    // check the reduced comment model for specials
    ReducedToken rmcToken = rmc.current();    
    if (!rmcToken.isGap()) {
        return rmcToken;
    }
    // check the reduced brace model for braces
    ReducedToken rmbToken = rmb.current();    
    if (!rmbToken.isGap()) {
      rmbToken.setState(rmc.getStateAtCurrent());
      return rmbToken;
    }
    // otherwise, we have a gap.
    int size = getSize(rmbToken,rmcToken);
    return new Gap(size, rmc.getStateAtCurrent());
  }

  /**
   * Get the shadowing state at the current caret position.
   * @return FREE|INSIDE_LINE_COMMENT|INSIDE_BLOCK_COMMENT|
   * INSIDE_SINGLE_QUOTE|INSIDE_DOUBLE_QUOTE
   */
  public ReducedModelState getStateAtCurrent() {
      return rmc.getStateAtCurrent();
  }

  /**
   * Get a string representation of the current token's type.
   * @return "" if current is a Gap, otherwise, use ReducedToken.getType()
   */
  String getType() {
    ReducedToken rmcToken = rmc.current();
    if (!rmcToken.isGap())
      return rmcToken.getType();
    
    ReducedToken rmbToken = rmb.current();
    if (!rmbToken.isGap()) {
      return rmbToken.getType();
    }
    return ""; //a gap
  }
    
  /**
   * Gets the size of the current token.
   * It checks both the brace and comment sub-models to find the size
   * of the current token.  If the current token is a Gap, we have to reconcile
   * the information of both sub-models in order to get the correct size
   * of the current token as seen by the outside world.
   * @return the number of characters represented by the current token
   */
  int getSize() {
    return getSize(rmb.current(),rmc.current());
  }
  
  int getSize(ReducedToken rmbToken, ReducedToken rmcToken) {
    int rmb_offset = rmb.getBlockOffset();
    int rmc_offset = rmc.getBlockOffset();
    int rmb_size = rmbToken.getSize();
    int rmc_size = rmcToken.getSize();
    int size = 0;
    if (rmb_offset < rmc_offset) {
      size = rmb_offset;
      _offset = size;
    }
    else {
      size = rmc_offset;
      _offset = size;
    }
    
    if (rmb_size - rmb_offset < rmc_size - rmc_offset) {
      size += (rmb_size - rmb_offset);      
    }
    else {
      size += (rmc_size - rmc_offset);
    }
    return size;
  }

  /**
   * Move the reduced model to the next token and update the cursor information.
   */
  void next() {
    if (rmc._cursor.atStart()){
      rmc.next();
      rmb.next();
      return;
    }
    int size = getSize(rmb.current(),rmc.current());
    rmc.move(size - _offset);
    rmb.move(size - _offset);
  }

  /**
   * Move the reduced model to the previous token and update the cursor information.
   */
  void prev() {
    int size = 0;
    if (rmc._cursor.atEnd()){
      rmc.prev();
      rmb.prev();
      if (rmc._cursor.atStart()) //because in place now.
        return;
      
      if (rmc.current().getSize() < rmb.current().getSize())
        size = -rmc.current().getSize();
      else
        size = -rmb.current().getSize();
      rmc.next();
      rmb.next();
      move (size);
      return;
    }
    
    if (rmb.getBlockOffset() < rmc.getBlockOffset()) {
      rmb.prev();
      size = rmb.current().getSize() + rmb.getBlockOffset();
      rmb.next();
      if (size < rmc.getBlockOffset()) 
        move(-size);
      else
        move(-rmc.getBlockOffset());
    }
    else if (rmb.getBlockOffset() == rmc.getBlockOffset()) {
      rmb.prev();
      rmc.prev();
      rmb.setBlockOffset(0);
      rmc.setBlockOffset(0);
    }
    else {
      rmc.prev();
      size = rmc.current().getSize() + rmc.getBlockOffset();
      rmc.next();
      if (size < rmb.getBlockOffset()) 
        move(-size);
      else
        move(-rmb.getBlockOffset());
    }
    
  }

  /**
   * Get the previous token.
   */
  public ReducedToken prevItem() {
    int rmbOffset = rmb.getBlockOffset();
    int rmcOffset = rmc.getBlockOffset();
    
    prev();
    ReducedToken temp = currentToken();
    next();
    
    rmb.setBlockOffset(rmbOffset);
    rmc.setBlockOffset(rmcOffset);
    return temp;
  }

  /**
   * Get the next token.
   */
  public ReducedToken nextItem() {
    int rmbOffset = rmb.getBlockOffset();
    int rmcOffset = rmc.getBlockOffset();
    next();
    ReducedToken temp = currentToken();
    prev();
    rmb.setBlockOffset(rmbOffset);
    rmc.setBlockOffset(rmcOffset);
    return temp;
  }

  /**
   * Determines if the cursor is at the end of the reduced model.
   */
  boolean atEnd() {
    return (rmb._cursor.atEnd() || rmc._cursor.atEnd());      
  }

  /**
   * Determines if the cursor is at the start of the reduced model.
   */
  boolean atStart() {
    return (rmb._cursor.atStart() || rmc._cursor.atStart());
  }

  /**
   * Gets the offset within the current token.
   */
  int getBlockOffset() {
    if (rmb.getBlockOffset() < rmc.getBlockOffset())
      return rmb.getBlockOffset();
    return rmc.getBlockOffset();
  }

  /**
   * Gets the absolute character offset into the document
   * represented by the reduced model.
   */
  public int absOffset() {
    return rmc.absOffset();
  }


  /**
   * A toString() substitute.
   */
  public String simpleString() {
    return "\n********\n" + rmb.simpleString() + "\n________\n" +
      rmc.simpleString();
  }

  /**
   * Gets the distance to the enclosing brace.
   */
  public IndentInfo getIndentInformation() {
    IndentInfo braceInfo = new IndentInfo();
    //get distance to the previous newline (in braceInfo.distToNewline)
    rmc.getDistToPreviousNewline(braceInfo);
    //get distance to the closing brace before that new line.
    rmb.getDistToEnclosingBrace(braceInfo);
    //get distance to newline before the previous, just mentioned, brace.
    rmc.getDistToIndentNewline(braceInfo);
    return braceInfo;
  }

  /**
   * Gets distance to end of line on the line previous.
   */
  public int getDistToPreviousNewline(int relLoc) {
    return rmc.getDistToPreviousNewline(relLoc);
  }

  public int getDistToNextNewline() {
    return rmc.getDistToNextNewline();
  }

  /**
   * Return all highlight status info for text between the current
   * location and current location + end.
   * This should collapse adjoining blocks with the same status into one.
   * @param start The starting location of the area we want to get status of.
   *              The reduced model is already at this position, but the 
   *              parameter is needed to determine the absolute positions
   *              needed in the HighlightStatus objects we return.
   * @param length How far should we generate info for?             
   */
  public Vector<HighlightStatus> getHighlightStatus(final int start,
                                                    final int length)
  {
    Vector<HighlightStatus> vec = new Vector<HighlightStatus>();

    int curState;
    int curLocation;
    int curLength;

    TokenList.Iterator cursor = rmc._cursor._copy();
    int ct = rmc._tokens.listenerCount();
    curLocation = start;
    curLength = cursor.current().getSize() - rmc.getBlockOffset();
    curState = cursor.current().getHighlightState();

    while ((curLocation + curLength) < (start + length)) {
      cursor.next();
      int nextState = cursor.current().getHighlightState();

      if (nextState == curState) {
        // add on and keep building
        curLength += cursor.current().getSize();
      }
      else {
        // add old one to the vector and start new one
        vec.addElement(new HighlightStatus(curLocation, curLength, curState));
        curLocation += curLength; // new block starts after previous one
        curLength = cursor.current().getSize();
        curState = nextState;
      }
    }

    // Make sure this token length doesn't extend past start+length.
    // This is because we guarantee that the returned vector only refers
    // to chars on [start, start+length).
    int requestEnd = start + length;
    if ((curLocation + curLength) > requestEnd) {
      curLength = requestEnd - curLocation;
    }

    // Add the last one, which has not been added yet
    vec.addElement(new HighlightStatus(curLocation, curLength, curState));

    cursor.dispose();
    
    return vec;
  }
}
