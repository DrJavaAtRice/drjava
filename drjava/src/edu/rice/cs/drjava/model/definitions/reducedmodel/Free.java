package edu.rice.cs.drjava.model.definitions.reducedmodel;

/**
 * Shadowing state that indicates normal, unshadowed text.
 * @version$Id$
 */
public class Free extends ReducedModelState {
  public static final Free ONLY = new Free();
  
  private Free() {
  }
  
  /**
   *  Walk function for when we're not inside a string or comment.
   *  Self-recursive and mutually recursive with other walk functions.
   *  <ol>
   *   <li> atEnd: return
   *   <li> If we find / *, * /, or / /, combine them into a single Brace,
   *        and keep the cursor on that Brace.
   *   <li> If current brace = //, go to next then call updateLineComment.<BR>
   *        If current brace = /*, go to next then call updateBlockComment.<BR>
   *        If current brace = ", go to next then call updateInsideDoubleQuote.<BR>
   *        Else, mark current brace as FREE, go to the next brace, and recur.
   * </ol>
   */
  ReducedModelState update(TokenList.Iterator copyCursor) {
    if (copyCursor.atEnd()) {
      return STUTTER;
    }
    
    _combineCurrentAndNextIfFind("/", "*", copyCursor);
    _combineCurrentAndNextIfFind("/", "/", copyCursor);
    _combineCurrentAndNextIfFind("","", copyCursor);
    //if a / preceeds a /* or a // combine them.
    _combineCurrentAndNextIfFind("/","/*",copyCursor);
    _combineCurrentAndNextIfFind("/","//",copyCursor);
    _combineCurrentAndNextIfEscape(copyCursor);
 
    String type = copyCursor.current().getType();
    if (type.equals("*/")) {
      copyCursor._splitCurrentIfCommentBlock(true,false);
      copyCursor.prev();
      return STUTTER;
    }
    else if (type.equals("//")) {
      // open comment blocks are not set commented, they're set free
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_LINE_COMMENT;
    }
    else if (type.equals("/*")) {
      // open comment blocks are not set commented, they're set free
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_BLOCK_COMMENT;
    }
    else if (type.equals("\'")) {
      // make sure this is a OPEN single quote
      if (copyCursor.current().isClosed()) {
        copyCursor.current().flip();
      }
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_SINGLE_QUOTE;
    }
    else if (type.equals("\"")) {
      // make sure this is a OPEN quote
      if (copyCursor.current().isClosed()) {
        copyCursor.current().flip();
      }
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return INSIDE_DOUBLE_QUOTE;
    }
    else {
      copyCursor.current().setState(FREE);
      copyCursor.next();
      return FREE;
    }
  }
}