/* $Id$ */

package edu.rice.cs.drjava;


public class IndentInfo {
	public String braceType; //the type of brace.	
	//the distance to infront of the newline directly preceding the brace.
	//____\n|_____
	public int distToNewline;
	//distance to behind the brace  ____|{_____
	public int distToBrace;
	//the distance to in front of the newline directly behind the cursor
	public int distToPrevNewline; //put us at space after newline
	static public String noBrace = "";
	static public String openSquiggly = "{";
	static public String openParen = "(";
	static public String openBracket = "[";

	public IndentInfo () {
		braceType = noBrace;
		distToNewline = -1;
		distToBrace = -1;
	}
	
	public IndentInfo (String _braceType,
										 int _distToNewline,
										 int _distToBrace,
										 int _distToPrevNewline) {
		braceType = _braceType;
		distToNewline = _distToNewline;
		distToBrace = _distToBrace;
		distToPrevNewline = _distToPrevNewline;
	}
}
