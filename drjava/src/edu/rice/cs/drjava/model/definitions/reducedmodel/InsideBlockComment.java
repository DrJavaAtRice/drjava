package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * Shadowing state that corresponds to being inside a block comment.
 * @version $Id$
 */
public class InsideBlockComment extends ReducedModelState {
  public static final InsideBlockComment ONLY = new InsideBlockComment();
  
  private InsideBlockComment() {
  }
  
    /**
   * Walk function for inside block comment.
   *  Self-recursive and mutually recursive with other walk functions.
   *  <ol>
   *   <li> If we've reached the end of the list, return.
   *   <li> If we find * /, combine it into a single Brace, and
   *        keep the cursor on that Brace.
   *   <li> If we find // or /*, split that into two Braces and keep the cursor
   *        on the first one.
   *   <li> If current brace = * /, mark the current brace as FREE,
   *        go to the next brace, and call updateFree.<BR>
   *        Else, mark current brace as INSIDE_BLOCK_COMMENT
   *        and go to next brace and recur.
   *  </ol>
   */
  ReducedModelState update(TokenList.Iterator copyCursor) {
    if (copyCursor.atEnd()) {
      return STUTTER;
    }
    _combineCurrentAndNextIfFind("*", "/", copyCursor);
    _combineCurrentAndNextIfFind("*","//", copyCursor);
    _combineCurrentAndNextIfFind("*","/*", copyCursor);
    _combineCurrentAndNextIfFind("","", copyCursor);    
    _combineCurrentAndNextIfEscape(copyCursor);                                              
        
    copyCursor._splitCurrentIfCommentBlock(false, false);
    
    String type = copyCursor.current().getType();
    if (type.equals("*/")) {
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return FREE;
    }
    
    else {
      copyCursor.current().setState(INSIDE_BLOCK_COMMENT);
      copyCursor.next();
      return INSIDE_BLOCK_COMMENT;
    }
  }
}
