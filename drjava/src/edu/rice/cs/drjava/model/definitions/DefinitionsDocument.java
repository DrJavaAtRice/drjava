/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.StyleContext.SmallAttributeSet;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;

import gj.util.Vector;

import java.util.HashSet;


/** The model for the definitions window. */
public class DefinitionsDocument extends PlainDocument
{
  private boolean _modifiedSinceSave = false;
  BraceReduction _reduced = new ReducedModelControl();

  private static HashSet _normEndings = _makeNormEndings();
  
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

  private static int _indent = 2;

  /**
   *1)mark the item previous to the current first insert
   *2)insert string
   *3)update from marked point.
   */
  public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException
  {
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

  /** Whenever this document has been saved, this method should be called
   *  so that it knows it's no longer in a modified state. */
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
  
  /**
   *Starts at start and gets whitespace starting at relStart and either
   *stopping at relEnd or at the first non-white space char.
   */
  private int getWhiteSpaceBetween(int relStart, int relEnd)
    throws BadLocationException
  {

    String text = this.getText(_currentLocation - relStart,
                               relStart - relEnd);
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

    // Return to previous location
    setCurrentLocation(oldLocation);
    return v;
  }
}
