/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleContext.SmallAttributeSet;
import gj.util.Vector;

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
  ReducedModel _reduced = new ReducedModel();
	//keeps track of all lit blocks
	Vector<StateBlock> litBlocks = new Vector<StateBlock>(); 
	Vector<StateBlock> changes = new Vector<StateBlock>();
  int _currentLocation = 0;

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
    super.insertString(offset, str, a);
		//variables
    int locationChange = offset - _currentLocation;
		int strLength = str.length();
		int prevSize;		//stores the size of the item prev when insert begins.
		int reducedOffset;
		ModelList<ReducedToken>.Iterator mark;
		Vector<StateBlock> newStates = new Vector<StateBlock>();

		//1)adjust location
    _reduced.move(locationChange);
		reducedOffset = _reduced._offset;
		//2)set mark to the previous item.
		mark = _reduced.makeCopyCursor();
		if (!mark.atStart()) //if not at start then get the previous item.
			mark.prev();
		if (mark.atStart()) //if now at start then size of current == 0
			prevSize = 0;
		else
			prevSize = mark.current().getSize(); //else size == size of current
		
		
		//3)loop through string inserting characters
    for (int i = 0; i < str.length(); i++)
			{
				char curChar = str.charAt(i);
				_addCharToReducedView(curChar);
			}
		
		//get highlight information from mark onward
		//numbers are off by prevSize + strLength + reducedOffset
		//the adjustment is the absolute position that newStates started at

		int adjustment = offset - prevSize - reducedOffset;
		newStates = SBVectorFactory.generate(mark,0,adjustment);
		
		updateCurrentHighlights(newStates,adjustment);
		
		_currentLocation = offset + strLength;
		_modifiedSinceSave = true;
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
        //System.err.println("calling insert }");
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

  public void remove(int offset, int len) throws BadLocationException
  {
    super.remove(offset, len);

    int locationChange = offset - _currentLocation;
		ModelList<ReducedToken>.Iterator mark;
		Vector<StateBlock> newStates;
		int reducedOffset;
		int prevSize;
		_reduced.move(locationChange);
		
		reducedOffset = _reduced._offset;
		
		mark = _reduced.makeCopyCursor();
		if (!mark.atStart()) //if not at start then get the previous item.
			mark.prev();
		if (mark.atStart()) //if now at start then size of current == 0
			prevSize = 0;
		else
			prevSize = mark.current().getSize(); //else size == size of current
		
//System.err.println("doc.remove: locChange=" + locationChange);
		_currentLocation = offset;		
		
		_reduced.delete(len);
		
		int adjustment = offset - prevSize - reducedOffset;
		newStates = SBVectorFactory.generate(mark,0,adjustment);
		
		updateCurrentHighlights(newStates,adjustment);
		//else the absolute location stays the same.
		//adjust the current location if delete works
    _modifiedSinceSave = true;
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
			return true;
			//return  _modifiedHighlights;
		}

	public Vector<StateBlock> getHighLightInformation()
		{
			return changes;
		}

	/**
	 *@param newStates the vector of blocks that are now lit up.
	 *@param adjustment the point where the walk was begun.	 
	 */
	private void updateCurrentHighlights(Vector<StateBlock> newBlocks,
																			 int adjustment)
		{
			//litBlocks
			/**
				 StateBlock sbLit;
				 int i = 0;
				 for (i = 0; i < litBlocks.size(); i++) {
				 sbLit = newBlocks.elementAt(i);
				 if (sbLit.location + sbLit.size >= adjustment)
				 break;
				 }
			*/
			changes = newBlocks;
		}
}







