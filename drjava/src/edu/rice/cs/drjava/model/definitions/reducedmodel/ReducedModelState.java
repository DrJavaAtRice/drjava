package edu.rice.cs.drjava;

/**
 * @version$Id$
 */
public abstract class ReducedModelState 
  implements /*imports*/ ReducedModelStates 
{
  abstract ReducedModelState update(TokenList.Iterator copyCursor);
  
  /**
   * Combines the current and next braces if they match the given types.
   * If we have braces of first and second in immediate succession, and if
   * second's gap is 0, combine them into first+second.
   * The cursor remains on the same block after this method is called.
   * @param first the first half of a multiple char brace
   * @param second the second half of a multiple char brace
   * @return true if we combined two braces or false if not
   */
  boolean  _combineCurrentAndNextIfFind
    (String first, 
     String second, 
     TokenList.Iterator copyCursor)
  {
    if (copyCursor.atStart() ||
        copyCursor.atEnd() ||
        copyCursor.atLastItem() ||
        !copyCursor.current().getType().equals(first))
    {
      return false;
    }
    copyCursor.next(); // move to second one to check if we can combine
    
    // The second one is eligible to combine if it exists (atLast is false),
    // if it has the right brace type, and if it has no gap.
    if (copyCursor.current().getType().equals(second)) {
      if ((copyCursor.current().getType().equals("")) &&
          (copyCursor.prevItem().getType().equals(""))) {
        // delete first Gap and augment the second
        copyCursor.prev();
        int growth = copyCursor.current().getSize();
        copyCursor.remove();
        copyCursor.current().grow(growth);
      }
      else if (copyCursor.current().getType().length() == 2) {
        String tail = copyCursor.current().getType().substring(1,2);
        String head = copyCursor.prevItem().getType() + 
          copyCursor.current().getType().substring(0,1);        
        copyCursor.current().setType(tail);
        copyCursor.prev();
        copyCursor.current().setType(head);
        copyCursor.current().setState(FREE);
      }
      else {
        // delete the first Brace and augment the second
        copyCursor.prev();
        copyCursor.remove();
        copyCursor.current().setType(first + second);
      }
      return true;
    }
    else {
      // we couldn't combine, so move back and return
      copyCursor.prev();
      return false;
    }
  }
  
  
  boolean 
    _combineCurrentAndNextIfEscape(TokenList.Iterator copyCursor)
  { 
    boolean combined = false;
    combined = combined || _combineCurrentAndNextIfFind("\\","\\",copyCursor);  // \-\
    combined = combined || _combineCurrentAndNextIfFind("\\","\'",copyCursor);  // \-'
    combined = combined || _combineCurrentAndNextIfFind("\\","\\'",copyCursor);// \-\'
    combined = combined || _combineCurrentAndNextIfFind("\\","\"",copyCursor);  // \-"
    combined = combined || _combineCurrentAndNextIfFind("\\","\\\"",copyCursor);// \-\"
    combined = combined || _combineCurrentAndNextIfFind("\\","\\\\",copyCursor);// \-\\
    return combined;
  }
}