/* $Id$ */

package edu.rice.cs.drjava;


public class IndentInfo {
	public String braceType;
	public int distToNewline;
	public int distToBrace;
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
										 int _distToBrace) {
		braceType = _braceType;
		distToNewline = _distToNewline;
		distToBrace = _distToBrace;
	}
}
