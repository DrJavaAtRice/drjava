/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.StyleContext.SmallAttributeSet;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;

import gj.util.Vector;

import java.util.HashSet;
import java.util.StringTokenizer;


/** The model for the definitions window. */
public class DefinitionsDocument extends PlainDocument
{
  private boolean _modifiedSinceSave = false;
  BraceReduction _reduced = new ReducedModelControl();

  private static HashSet _normEndings = _makeNormEndings();
  private static HashSet _keywords = _makeKeywords();
  
  int _currentLocation = 0;


  public DefinitionsDocument()
  {
    super();
  }

  private static HashSet _makeNormEndings() {
    HashSet normEndings = new HashSet();
    normEndings.add(";");
    normEndings.add("{");
    normEndings.add("}");
    normEndings.add("(");
    return normEndings;
  }

  private static HashSet _makeKeywords() {
    final String[] words = {
      "import", "native", "package", "goto", "const", "if", "else", "switch",
      "while", "for", "do", "true", "false", "null", "this", "super", "new",
      "instanceof", "boolean", "char", "byte", "short", "int", "long", "float",
      "double", "void", "return", "static", "synchronized", "transient",
      "volatile", "final", "strictfp", "throw", "try", "catch", "finally",
      "synchronized", "throws", "extends", "implements", "interface", "class",
      "break", "continue", "public", "protected", "private", "abstract"
    };

    HashSet keywords = new HashSet();
    for (int i = 0; i < words.length; i++) {
      keywords.add(words[i]);
    }

    return keywords;
  }

  private static int _indent = 2;

  /**
   *1)mark the item previous to the current first insert
   *2)insert string
   *3)update from marked point.
   */
  public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException
  {
    //System.err.println("insert@" + offset + ": |" + str + "|");
    int locationChange = offset - _currentLocation;
    int strLength = str.length();
    int prevSize;   //stores the size of the item prev when insert begins.
    int reducedOffset;
    
    //1)adjust location
    _reduced.move(locationChange);

    //3)loop through string inserting characters
    for (int i = 0; i < str.length(); i++)
    {
      char curChar = str.charAt(i);
      _addCharToReducedView(curChar);
    }

    //DrJava.consoleErr().print("Insert: loc before=" + _currentLocation);
    _currentLocation = offset + strLength;
    //DrJava.consoleErr().println(" loc after=" + _currentLocation);
    
    super.insertString(offset, str, a);
    _modifiedSinceSave = true;
    _styleChanged();
  }
  
  private void _addCharToReducedView(char curChar)
  {
    switch (curChar)
      {
      case '(':
        _reduced.insertOpenParen();
        break;
      case ')':
        _reduced.insertClosedParen();
        break;
      case '[':
        _reduced.insertOpenBracket();
        break;
      case ']':
        _reduced.insertClosedBracket();
        break;
      case '{':
        _reduced.insertOpenSquiggly();
        break;
      case '}':
        _reduced.insertClosedSquiggly();
        break;
      case '/':
        _reduced.insertSlash();
        break;
      case '*':
        _reduced.insertStar();
        break;
      case '"':
        _reduced.insertQuote();
        break;
      case '\\':
        _reduced.insertBackSlash();
        break;
      case '\n':
      case '\r':
        _reduced.insertNewline();
        break;
      default:
        _reduced.insertGap(1);
        break;
    }
  }

  /**
   *len must be positive.
   */
  
  public void remove(int offset, int len) throws BadLocationException
  {
    //System.err.println("remove: " + offset + ", " + len);
    int locationChange = offset - _currentLocation;

    _reduced.move(locationChange);

    _currentLocation = offset;    
    _reduced.delete(len);
    
    super.remove(offset, len);
    _modifiedSinceSave = true;
    
    _styleChanged();
  }

  /**
   * Fire event that styles changed from current location to the end.
   * Right now we do this every time there is an insertion or removal. 
   * Two possible future optimizations:
   * 1. Only fire changed event if text other than that which was inserted
   *    or removed *actually* changed status. If we didn't changed the status
   *    of other text (by inserting or deleting unmatched pair of quote or
   *    comment chars), no change need be fired.
   * 2. If a change must be fired, we could figure out the exact end
   *    of what has been changed. Right now we fire the event saying that
   *    everything changed to the end of the document.
   *
   * I don't think we'll need to do either one since it's still fast now.
   * I think this is because the UI only actually paints the things on the
   * screen anyway.
   */
  private void _styleChanged() {
    int length = getLength() - _currentLocation;
    //DrJava.consoleErr().println("Changed: " + _currentLocation + ", " + length);
    DocumentEvent evt = new DefaultDocumentEvent(_currentLocation,
                                                 length,
                                                 DocumentEvent.EventType.CHANGE);
    fireChangedUpdate(evt);
  }

  /*
  protected void insertUpdate(AbstractDocument.DefaultDocumentEvent chng,
                              AttributeSet attr)
  {
    // OK we need to add an UndoableEdit to chng to make it undo
    // reduced model changes
  }
  */

  /** Whenever this document has been saved, this method should be called
   *  so that it knows it's no longer in a modified state.
   */
  public void resetModification()
  {
    _modifiedSinceSave = false;
  }

  public boolean modifiedSinceSave()
  {
    return _modifiedSinceSave;
  }

  public int getCurrentLocation()
  {
    return _currentLocation;
  }

  public void setCurrentLocation(int loc)
  {
    move(loc - _currentLocation);
  }

  public void move(int dist)
  {
    _currentLocation += dist;
    if (_currentLocation < 0) {
      throw new RuntimeException("location < 0?!");
    }
    _reduced.move(dist);
  }

  public void setIndent(int indent)
  {
    this._indent = indent;
  }

  public void indentBlock(int start, int end)
  {
    try {
      int moved = 0;
      Position endPos = this.createPosition(end);

      while(start < endPos.getOffset()){
        moved = _reduced.getDistToNextNewline();
        start += moved;
        setCurrentLocation(start);
        indentLine();
        //keeps track of the start and end position
        start += endPos.getOffset() - end;
        end = endPos.getOffset();

        if(start < end)
          start++;
        setCurrentLocation(start);
      }
    }
    catch (Exception e) {e.printStackTrace();}
  }

  public void indentLine() 
  {
    // moves us to the end of the line
    move(_reduced.getDistToNextNewline());

    IndentInfo ii = _reduced.getIndentInformation();
    String braceType = ii.braceType;
    int distToNewline = ii.distToNewline;
    int distToBrace = ii.distToBrace;
    int distToPrevNewline = ii.distToPrevNewline;
    int tab = 0;
    boolean isSecondLine = false;

    if (distToNewline == -1) {
      distToNewline = _currentLocation;
      isSecondLine = true;
    }

    try {
      if (distToPrevNewline == -1) //only on the first line
        tab = 0;
      //takes care of the second line
      else if(this._currentLocation - distToPrevNewline < 2)
        tab = 0;
      else if (distToBrace == -1)
        tab = _indentSpecialCases(0, distToPrevNewline);
      else if (braceType.equals("("))
        tab = distToNewline - distToBrace + 1;
      else if (braceType.equals("{")) {
        tab = getWhiteSpaceBetween(distToNewline, distToBrace) + _indent;
        tab = _indentSpecialCases(tab, distToPrevNewline);
      }
      else if (braceType.equals("["))
        tab = distToNewline - distToBrace + 1;

      tab(tab, distToPrevNewline);
    }
    catch (BadLocationException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Deals with the special cases.
   * If the first character after the previous \n is a } then -2
   */
  private int _indentSpecialCases(int tab, int distToPrevNewline)
    throws BadLocationException
  {
    //not a special case.
    if (distToPrevNewline == -1)
      return tab;
    
    //setup
    int start = _reduced.getDistToPreviousNewline(distToPrevNewline + 1);

    if (start == -1)
      start = 0;
    else
      start = _currentLocation - start;
    String text = this.getText(start,_currentLocation - start);

    //case of  }
    int length = text.length();
    int k = length - distToPrevNewline;
    while (k < length && text.charAt(k) == ' ')
      k++;
    if(k < length && text.charAt(k) == '}')
      tab -= _indent;
    // if no matching { then let offset be 0.
    if(tab < 0)
      tab = 0;

    //non-normal endings
    int i = length - distToPrevNewline - 2;
    int distanceMoved = distToPrevNewline + 2;
    move(-distToPrevNewline - 2); //assumed: we are at end of a line.
    
    while (i >= 0 && _isCommentedOrSpace(i, text)){
      i--;
      if (i > 0){ //gaurentees you don't move into document Start.
        distanceMoved++;
        move(-1);
      }
    }
    move (distanceMoved); //move the document bac.

    if (i >= 0 && !(_normEndings.contains(text.substring(i, i+1)))) {
      int j = 0;
      while ((j < length) && (text.charAt(j) == ' '))
        j++;
      if ((k < length) && (text.charAt(k) == '{')){
        if ((j < length) && (text.charAt(j) == '{'))
          tab = j + _indent;
        else
          tab = j;
      }
      else
        tab = j + _indent;
    }
      
    //return tab
    return tab;
  }

  private boolean _isCommentedOrSpace (int i, String text)
  {
    ReducedToken rt = _reduced.currentToken();
    String type = rt.getType();

    return (rt.isCommented() || type.equals("//") || type.equals("/*") ||
            type.equals("*/") || (text.charAt(i) == ' '));
  }

	public int getWhiteSpace()
		{
			try{
				return getWhiteSpaceBetween(0,getLength()-_currentLocation);
			}
			catch (BadLocationException e){e.printStackTrace();}
			return -1;
		}
  /**
   *Starts at start and gets whitespace starting at relStart and either
   *stopping at relEnd or at the first non-white space char.
	 *NOTE: relStart and relEnd are relative to where we are in the document
	 *relStart must be <= _currentLocation
   */
  private int getWhiteSpaceBetween(int relStart, int relEnd)
    throws BadLocationException
  {

    String text = this.getText(_currentLocation - relStart,
                               Math.abs(relStart - relEnd));
    int i = 0;
    int length = text.length();
    while ((i < length) && (text.charAt(i) == ' '))
      i++;

    return i;
  }

  
  void tab(int tab, int distToPrevNewline)
    throws BadLocationException
  {
    if (distToPrevNewline == -1)
      distToPrevNewline = _currentLocation;
    
    int currentTab = getWhiteSpaceBetween(distToPrevNewline, 0);
    
    if (tab == currentTab)
      return;
    if (tab > currentTab){
      String spaces = "";
      for (int i = 0; i < tab - currentTab; i++)
        spaces = spaces + " ";

      insertString(_currentLocation - distToPrevNewline, spaces, null);
    }
    else
      remove(_currentLocation - distToPrevNewline, currentTab - tab);
  }
  

  /**
   * Return all highlight status info for text between start and end.
   * This should collapse adjoining blocks with the same status into one.
   */
  public Vector<HighlightStatus> getHighlightStatus(int start, int end) {
    //DrJava.consoleErr().println("getHi: start=" + start + " end=" + end +
                                //" currentLoc=" + _currentLocation);
    
    // First move the reduced model to the start
    int oldLocation = _currentLocation;
    setCurrentLocation(start);

    // Now ask reduced model for highlight status for chars till end
    Vector<HighlightStatus> v = _reduced.getHighlightStatus(start, end - start);

    // Go through and find any NORMAL blocks
    // Within them check for keywords
    for (int i = 0; i < v.size(); i++) {
      HighlightStatus stat = v.elementAt(i);

      if (stat.getState() == HighlightStatus.NORMAL) {
        i = _highlightKeywords(v, i);
      }
    }

    // Return to previous location
    setCurrentLocation(oldLocation);
    return v;
  }

  /**
   * Separates out keywords from normal text for the given
   * HighlightStatus element.
   *
   * What this does is it looks to see if the given part of the text
   * contains a keyword. If it does, it splits the HighlightStatus into
   * separate blocks so that each keyword is in its own block.
   * This will find all keywords in a given block.
   *
   * Note that the given block must have state NORMAL.
   *
   * @param v Vector with highlight info
   * @param i Index of the single HighlightStatus to check for keywords in
   * @return the index into the vector of the last processed element
   */
  private int _highlightKeywords(Vector<HighlightStatus> v, int i) {
    // Basically all non-alphanumeric chars are delimiters
    final String delimiters = " \t\n\r{}()[].+-/*;:=!~<>?";
    final HighlightStatus original = v.elementAt(i);

    final String text;
    try {
      text = getText(original.getLocation(), original.getLength());
    }
    catch (BadLocationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    // Because this text is not quoted or commented, we can use the simpler
    // tokenizer StringTokenizer.
    // We have to return delimiters as tokens so we can keep track of positions
    // in the original string.
    StringTokenizer tokenizer = new StringTokenizer(text, delimiters, true);

    // start and length of the text that has not yet been put back into the
    // vector.
    int start = original.getLocation();
    int length = 0;

    // Remove the old element from the vector.
    v.removeElementAt(i);

    // Index where we are in the vector. It's the location we would insert
    // new things into.
    int index = i;

    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();

      if (_keywords.contains(token)) {
        // first check if we had any text before the token
        if (length != 0) {
          HighlightStatus newStat =
            new HighlightStatus(start,
                                length,
                                original.getState());

          v.insertElementAt(newStat, index);
          index++;
          start += length;
          length = 0;
        }

        // Now pull off the keyword
        int keywordLength = token.length();
        v.insertElementAt(new HighlightStatus(start,
                                              keywordLength,
                                              HighlightStatus.KEYWORD),
                          index);
        index++;

        // Move start to the end of the keyword
        start += keywordLength;
      }
      else {
        // This is not a keyword, so just keep accumulating length
        length += token.length();
      }
    }

    // Now check if there was any text left after the keywords.
    if (length != 0) {
      HighlightStatus newStat =
        new HighlightStatus(start,
                            length,
                            original.getState());

      v.insertElementAt(newStat, index);
      index++;
      length = 0;
    }

    // return one before because we need to point to the last one we inserted
    return index - 1;
  }

	public void gotoLine(int line)
		{
			int dist;
			if (line < 0)
				return;
			setCurrentLocation(0);

			for (int i = 1; (i < line) && (_currentLocation < getLength()); i++){
				dist = _reduced.getDistToNextNewline();
				if (_currentLocation + dist < getLength()){
					dist++;
				}
				move(dist);
			}
		}
}











