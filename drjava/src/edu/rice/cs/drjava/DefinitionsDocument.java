/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.StyleContext.SmallAttributeSet;
import javax.swing.text.*;

import gj.util.Vector;

import java.util.HashSet;


/** 
	 hasHighlightChanged()
	 getHighLightInformation()
	 getMatchingBrace  - given absolute offset
	     match backward then foreward, return -1 if no match
*/

/** The model for the definitions window. */
public class DefinitionsDocument extends DefaultStyledDocument
{
  private boolean _modifiedSinceSave = false;
	private boolean _modifiedHighlights = false;
  BraceReduction _reduced = new ReducedModelControl();
	//keeps track of all lit blocks
	Vector<StateBlock> litBlocks = new Vector<StateBlock>(); 
	StyleUpdateMessage changes;

	private static HashSet _normEndings = _makeNormEndings();
	
  int _currentLocation = 0;

	StyleUpdateThread _styleUpdater;

	Semaphore _taskCounter;

	public DefinitionsDocument()
		{
			super();
			this._taskCounter = new Semaphore();
			this._styleUpdater = new StyleUpdateThread(this);
			_styleUpdater.start();
		}

	private static HashSet _makeNormEndings() {
		HashSet normEndings = new HashSet();
		normEndings.add(";");
		normEndings.add("{");
		normEndings.add("}");
		normEndings.add("(");
		return normEndings;
	}


	/**
	 *1)mark the item previous to the current first insert
	 *2)insert string
	 *3)update from marked point.
	 *****This means that all positions in the returned vector have to be
	 *****altered by - (getSize() + strSize + _innitialcursor.offset)
	 *  Also in this update make sure that generate gets passed an absolute
	 *  location so that the update returns absolute locations.
	 */
  public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException
  {
    int locationChange = offset - _currentLocation;
		int strLength = str.length();
		int prevSize;		//stores the size of the item prev when insert begins.
		int reducedOffset;
		ModelList<ReducedToken>.Iterator mark;
		StyleUpdateMessage message;
		_modifiedHighlights = false;

		_taskCounter.increment();
		
		//1)adjust location
    _reduced.move(locationChange);

		//3)loop through string inserting characters
    for (int i = 0; i < str.length(); i++)
			{
				char curChar = str.charAt(i);
				_addCharToReducedView(curChar);
				_modifiedHighlights = _modifiedHighlights ||
					_reduced.hasHighlightChanged();
			}

		_currentLocation = offset + strLength;
		
    super.insertString(offset, str, a);
		_modifiedSinceSave = true;
		
		//get highlight information from mark onward
		//numbers are off by prevSize + strLength + reducedOffset
		//the adjustment is the absolute position that newStates started at
			message = _reduced.generateHighlights(offset,strLength,
																						!_modifiedHighlights);
			updateCurrentHighlights(message);
			updateStyles(message);
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
        //System.err.println("calling insert {");
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
		_taskCounter.increment();
    int locationChange = offset - _currentLocation;
		ModelList<ReducedToken>.Iterator mark;
		StyleUpdateMessage message;

		_reduced.move(locationChange);

		_currentLocation = offset;		
		_reduced.delete(len);
		
    super.remove(offset, len);
    _modifiedSinceSave = true;
		_modifiedHighlights = _reduced.hasHighlightChanged();
		
		message = _reduced.generateHighlights(offset,0, !_modifiedHighlights);
		updateCurrentHighlights(message);
		updateStyles(message);
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

	/**
	 *@return has the information on highlighted sections of doc changed.
	 */
	public boolean hasHighlightChanged()
		{
			return  _modifiedHighlights;
		}

	private void updateStyles(StyleUpdateMessage message) {
					_styleUpdater.sendMessage(message);
		}

	public void updateCurrentHighlights(StyleUpdateMessage message)
		{
			this.changes = message;
		}
	
	public StyleUpdateMessage getHighlightInformation()
		{
			return this.changes;
		}

	public int getCurrentLocation()
		{
			return _currentLocation;
		}

	public void setCurrentLocation(int loc)
  {
    _reduced.move(loc - _currentLocation);
    _currentLocation = loc;
	}


	public void move(int dist)
		{
			_currentLocation += dist;
			_reduced.move(dist);
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
			
			
			try{
				if ((distToNewline == -1) || (distToBrace == -1))
					tab = 0;
				else if (braceType.equals("("))
					tab = distToNewline - distToBrace + 1;
				else if (braceType.equals("{"))
					tab = getWhiteSpaceBetween(distToNewline, distToBrace) + 2;
				else if (braceType.equals("["))
					tab = distToNewline - distToBrace + 1;

				tab = _indentSpecialCases(tab,distToPrevNewline);
			 
				tab(tab, distToPrevNewline);
			}
			catch (BadLocationException e){
				//e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			}
		}

	/**
	 *Deals with the special cases.
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
			int i = length - distToPrevNewline;
			while (i < length && text.charAt(i) == ' ')
				i++;
			if(i < length && text.charAt(i) == '}')
				tab -= 2;
			// if no matching { then let offset be 0.
			if(tab < 0)
				tab = 0;

			//non-normal endings
			i = length - distToPrevNewline - 2;
			while (i >= 0 && text.charAt(i) == ' ') {
				i--;
			}
			if (i >= 0 && !(_normEndings.contains(text.substring(i, i+1)))) {
				int j = 0;
				while (j < length && text.charAt(j) == ' ')
					j++;
				
				tab = j + 2;
			}
				
      //return tab
			return tab;
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
	

}









