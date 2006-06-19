package edu.rice.cs.javalanglevels;

import edu.rice.cs.javalanglevels.parser.*;
import java.io.File;

/**
 * Used to represent custom parse exceptions in the JExpression parser.
 */
public class JExprParseException extends ParseException {

  private File _file;
  private String _message;

  public JExprParseException(File file, 
                        String message,
                        Token currentTokenVal,
                        int[][] expectedTokenSequencesVal,
                        String[] tokenImageVal
                       )
  {
    super(currentTokenVal, expectedTokenSequencesVal, tokenImageVal);
    _file = file;
    _message = message;
  }
  
  /** Wrap a ParseException (assumed not to be a JExprParseException.
   * The file will be null. */
  public JExprParseException(ParseException e) {
    super(e.currentToken, e.expectedTokenSequences, e.tokenImage);
    _file = null;
    _message = e.getMessage();
  }
  
  /** May be null */
  public File getFile() { return _file; }
  
  public String getMessage() { return _message; }
  
}
