package  edu.rice.cs.drjava;

/**
 * @version $Id$
 */
public class IndentInfo {
  public String braceType;      //the type of brace.	
  //the distance to infront of the newline directly preceding the brace.
  //____\n|_____
  public int distToNewline;
  //distance to behind the brace  ____|{_____
  public int distToBrace;
  //the distance to in front of the newline directly behind the cursor
  public int distToPrevNewline;                 //put us at space after newline
  static public String noBrace = "";
  static public String openSquiggly = "{";
  static public String openParen = "(";
  static public String openBracket = "[";

  /**
   * put your documentation comment here
   */
  public IndentInfo() {
    braceType = noBrace;
    distToNewline = -1;
    distToBrace = -1;
  }

  /**
   * put your documentation comment here
   * @param     String _braceType
   * @param     int _distToNewline
   * @param     int _distToBrace
   * @param     int _distToPrevNewline
   */
  public IndentInfo(String _braceType, int _distToNewline, int _distToBrace, int _distToPrevNewline) {
    braceType = _braceType;
    distToNewline = _distToNewline;
    distToBrace = _distToBrace;
    distToPrevNewline = _distToPrevNewline;
  }
}



