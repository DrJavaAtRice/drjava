package koala.dynamicjava.parser.wrapper;

import koala.dynamicjava.parser.impl.ParseException;
import koala.dynamicjava.parser.impl.Token;

public class CustomParseException extends ParseException {
  
  private final String _message;
  private final boolean _customMessage;
  
  public CustomParseException(String message, Token currentTokenVal,
      int[][] expectedTokenSequencesVal, String[] tokenImageVal) {
    super(currentTokenVal, expectedTokenSequencesVal, tokenImageVal);
    _message = message;
    _customMessage = true;
  }
  
  public CustomParseException(ParseException e) {
    super(e.currentToken, e.expectedTokenSequences, e.tokenImage);
    _message = e.getMessage();
    _customMessage = (e instanceof CustomParseException && ((CustomParseException)e)._customMessage) ||
                     e.currentToken == null;
  }
  
  public String getMessage() { return _message; }
  
  public String getShortMessage() {
    if (_customMessage) {
      return getMessage();
    }
    else {
      String expected = "";
      int maxSize = 0;
      for (int i = 0; i < expectedTokenSequences.length; i++) {
        if (maxSize < expectedTokenSequences[i].length) {
          maxSize = expectedTokenSequences[i].length;
        }
      }
      String retval = "Syntax Error: \"";
      Token tok = currentToken.next;
      
      for (int i = 0; i < maxSize; i++) {
        if (i != 0) retval += " ";
        if (tok.kind == 0) {
          retval += tokenImage[0];
          break;
        }
        retval += add_escapes(tok.image);
        tok = tok.next; 
      }
      // retval += "\" at line " + currentToken.next.beginLine + ", column " + currentToken.next.beginColumn + "." + eol;
      retval += "\"";
      return retval;
    }
  }
  
  /**
   * Returns starting line of syntax error.
   */
  public int getBeginLine() {
    if(currentToken.next!=null)
      return currentToken.next.beginLine;
    return currentToken.beginLine;
  }
  
  /**
   * Returns starting column of syntax error.
   */
  public int getBeginColumn() {
    if(currentToken.next!=null)
      return currentToken.next.beginColumn;
    return currentToken.beginColumn;
  }
  
  /**
   * Returns ending line of syntax error.
   */
  public int getEndLine() {
    if(currentToken.next!=null)
      return currentToken.next.endLine;
    return currentToken.endLine;
  }
  
  /**
   * Returns ending column of syntax error.
   */
  public int getEndColumn() {
    if(currentToken.next!=null)
      return currentToken.next.endColumn;
    return currentToken.endColumn;
  }
  
  public static CustomParseException makeCustom(ParseException e) {
    if (e instanceof CustomParseException) { return (CustomParseException) e; }
    else { return new CustomParseException(e); }
  }
  
}
