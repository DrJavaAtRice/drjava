/* $Id$ */

package edu.rice.cs.drjava;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleContext.SmallAttributeSet;

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
  int _currentLocation = 0;

  public void insertString(int offset, String str, AttributeSet a)
    throws BadLocationException
  {
    super.insertString(offset, str, a);

    int locationChange = offset - _currentLocation;

    //System.err.println("rv at start=" + _reduced.simpleString());

    _reduced.move(locationChange);

    //System.err.println("location changed: " + locationChange);
    //System.err.println("old location: " + _currentLocation + 
                       //" rv=" + _reduced.simpleString());

    for (int i = 0; i < str.length(); i++)
    {
      char curChar = str.charAt(i);
      _addCharToReducedView(curChar);
    }

    _currentLocation = offset + str.length();
    //System.err.println("new location: " + _currentLocation + 
                       //" rv=" + _reduced.simpleString());
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
    //System.err.println("doc.remove: locChange=" + locationChange);
		_currentLocation = offset;
    _reduced.move(locationChange);
    _reduced.delete(len);
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
}







