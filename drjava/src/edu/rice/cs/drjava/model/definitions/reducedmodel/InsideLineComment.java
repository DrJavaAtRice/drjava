package edu.rice.cs.drjava;

/**
 * Shadowing state that corresponds to being inside a line comment.
 * @version$Id$
 */
public class InsideLineComment extends ReducedModelState {
  public static final InsideLineComment ONLY = new InsideLineComment();
  
  private InsideLineComment() {
  }
  
    /**
  * Walk function for inside line comment.
  *  <ol>
  *   <li> If we've reached the end of the list, return.
  *   <li> If we find //, /* or * /, split them into two separate braces.
  *     The cursor will be on the first of the two new braces.
  *   <li> If current brace = \n, mark current brace FREE, next(), and
  *        go to updateFree.<BR>
  *        Else, mark current brace as LINE_COMMENT, goto next, and recur.
  *  </ol>
  */
  ReducedModelState update(TokenList.Iterator copyCursor) {
    
    if (copyCursor.atEnd()) {
      return STUTTER;
    }
    copyCursor._splitCurrentIfCommentBlock(true, false);
    _combineCurrentAndNextIfFind("","", copyCursor);
    _combineCurrentAndNextIfEscape(copyCursor);
    
    String type = copyCursor.current().getType();
    
    if (type.equals("\n")) {
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return FREE;
    }
    else {
      copyCursor.current().setState(INSIDE_LINE_COMMENT);
      copyCursor.next();
      return INSIDE_LINE_COMMENT;
    }
  }
}