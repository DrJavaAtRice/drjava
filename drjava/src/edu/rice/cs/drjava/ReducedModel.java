/* $Id$ */

package edu.rice.cs.drjava;

import gj.util.Stack;
import gj.util.Vector;

/**
 * This class provides an implementation of the BraceReduction
 * interface that supports brace matching with support
 * for comment and quote (string) shadowing.
 * To avoid unnecessary complication, this class maintains a few
 * invariants for its consistent states, i.e., between top-level
 * function calls.
 * <ol>
 * <li> The cursor offset is never at the end of a brace.  If movement
 * or insertion puts it there, the cursor is updated to point to the 0
 * offset of the next brace.
 * <li> Quoting information is invalid inside valid comments.  When part
 * of the document becomes uncommented, the reduced model must update the
 * quoting information linearly in the newly revealed code.
 * <li> Quote shadowing and comment shadowing are mutually exclusive.
 * <li> There is no nesting of comment open characters. If // is encountered
 *      in the middle of a comment, it is treated as two separate slashes.
 *      Similar for /*.
 * </ol>
 * @author Mike Yantosca, Jonathan Bannet
 */

public class ReducedModel implements BraceReduction
{
  /**
   * The character that represents the cursor in toString().
   * @see #toString()
   */
  public static final char PTR_CHAR = '#';
  
  /**
	 * element list
	 * @see ModelList
	 */
  ModelList<ReducedToken> _braces;
  /**
	 * keeps track of cursor position in document
	 * @see ModelList.Iterator
	 */
  ModelList<ReducedToken>.Iterator _cursor;
  /** more refined cursor information */
  int _offset;
  

  /**
   * Constructor.  Creates a new reduced model and inserts a blank
   * node as a placeholder for gaps after the last brace.  The blank
   * node becomes the current node.
   */
  public ReducedModel()
    {
      _braces = new ModelList<ReducedToken>();
      _cursor = _braces.getIterator();
			// we should be pointing to the head of the list
			_offset = 0;
    }

	/**
	 * Package private absolute offset for tests.
	 */

	int absOffset()
		{
			int off = _offset;
			ModelList<ReducedToken>.Iterator it = _cursor.copy();

			if (!it.atStart())
				it.prev();
			
			while (!it.atStart())
				{
					off += it.current().getSize();
					it.prev();
				}

			return off;
		}

	public String simpleString()
		{
			String val = "";
			ReducedToken tmp;

			ModelList<ReducedToken>.Iterator it = _braces.getIterator();
			it.next(); // since we start at the head, which doesn't have a current
			// item

			if (_cursor.atStart())
				{
					val += PTR_CHAR;
					val += _offset;
				}

			while(!it.atEnd())
				{
					tmp = it.current();

					if (!_cursor.atStart() && !_cursor.atEnd() && (tmp == _cursor.current()))
						{
							val += PTR_CHAR;
							val += _offset;
						}

					val += "|";
					val += tmp;
					val += "|\t";

					it.next();
				}
		
			if (_cursor.atEnd())
				{
					val += PTR_CHAR;
					val += _offset;
				}

			val += "|end|";
			return val;
		}

  

	//************************************TOP LEVEL FUNCTIONS****************
	//**********************************************************************
	//All inserts end up with pointer after inserted item
	
  /**
   * Inserts an open brace ({) into the reduced model.	 
   */
	public Vector<StateBlock> insertOpenSquiggly()
		{
			_insertBrace("{");
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}

  /**
   * Inserts a closed brace (}) into the reduced model.
   */
  public Vector<StateBlock> insertClosedSquiggly()
		{
			_insertBrace("}");
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}
  /**
   * Inserts an open parenthesis (() into the reduced model.
   */
  public Vector<StateBlock> insertOpenParen()
		{
			_insertBrace("(");
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}

  /**
   * Inserts a closed parenthesis ()) into the reduced model.
   */
  public Vector<StateBlock> insertClosedParen()
		{
			_insertBrace(")");
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}
	
  /**
   * Inserts an open bracket ([) into the reduced model.
   */
  public Vector<StateBlock> insertOpenBracket()
		{
			_insertBrace("[");
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}

  /**
   * Inserts a closed bracket (]) into the reduced model.
   */
  public Vector<StateBlock> insertClosedBracket()
		{
			_insertBrace("]");
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}
	
  /**
   * Inserts a star.
   */
  public Vector<StateBlock> insertStar()
		{
			//check if empty
			if (_braces.isEmpty())
				{
					_insertNewBrace("*",_cursor);//now pointing to tail.
					return SBVectorFactory.generate(_cursor.copy(),_offset);
				}
			//check if at start
			if (_cursor.atStart())
				_cursor.next();
			//not empty, not at start, if at end check the previous brace
			if (_cursor.atEnd())
				{
					_checkPreviousInsertStar(_cursor);
					return SBVectorFactory.generate(_cursor.copy(),_offset);
				}
		 
			//if inside a double character brace, break it.
			else if ((_offset > 0) && _cursor.current().isMultipleCharBrace())
				{
					_splitCurrentIfCommentBlock(true,_cursor);
          //leaving us at the start
					_cursor.next(); //leaving us after first char
					_insertNewBrace("*",_cursor); //leaves us after *
					_cursor.prev();
					_cursor.prev(); //puts us back on first char in double comment
					_updateBasedOnCurrentState();
					if (!_cursor.current().isMultipleCharBrace())
						_cursor.next();
					_cursor.next();
				}
			//if a gap
			else if ((_offset > 0) && (_cursor.current().isGap()))
				{
					_insertBraceToGap("*", _cursor);
				}
			//if at start of double character brace, break it.
			else if ((_offset == 0) && _cursor.current().isMultipleCharBrace())
				{
					//if we're free there won't be a block comment close so if there
					//is then we don't want to break it.
					_splitCurrentIfCommentBlock(false,_cursor); //leaving us at start
					_checkPreviousInsertStar(_cursor);
				}
			else
				{
					_checkPreviousInsertStar(_cursor);
				}
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}
	
			 
	private void _checkPreviousInsertStar(ModelList<ReducedToken>.Iterator
																				copyCursor)
		{
			if ( !copyCursor.atStart()	&& !copyCursor.atFirstItem())
				{
					if (copyCursor.prevItem().getType().equals("/") &&
							(copyCursor.prevItem().getState() == ReducedToken.FREE))
						{
							copyCursor.prevItem().setType("/*");
							_updateBasedOnCurrentState();
							return;
						}
					// if we're after a star, 
				}
			
			_insertNewBrace("*",copyCursor); //leaving us after the brace.
			copyCursor.prev();
			_updateBasedOnCurrentState();
			if (copyCursor.current().getSize() == 2)
				_offset = 1;
			else
				copyCursor.next();			
		}

		
	
  /**
   * Inserts a slash
	 *1) If at head, or start: insert normally
	 *2) If previous is a * and inside block comment: unite.
	 *3) If previous is a / and Free: unite.
	 *4) If left of or inside of / *: a)break,
	 *                                b)insert,
	 *                                c)walk from first of three
	 *5) If left of or inside of / /: a)break
	 *                                b)insert
	 *                                c)walk from the first of three
	 *6) If inside of * /           : a)break
	 *                                b)insert
	 *                                c)walk from first of three
	 */
  public Vector<StateBlock> insertSlash()
		{
			//check if empty
			if (_braces.isEmpty())
				{
					_insertNewBrace("/",_cursor);//now pointing to tail.
					return SBVectorFactory.generate(_cursor.copy(),_offset);
				}
			//check if at start
			if (_cursor.atStart())
				_cursor.next();
			//not empty, not at start, if at end check the previous brace
			if (_cursor.atEnd())
				{
					_checkPreviousInsertSlash(_cursor);
					return SBVectorFactory.generate(_cursor.copy(),_offset);
				}
			
			//if inside a double character brace, break it.
			else if ((_offset > 0) && _cursor.current().isMultipleCharBrace())
				{
					_splitCurrentIfCommentBlock(true,_cursor);
          //leaving us at the start
					_cursor.next(); //leaving us after first char
					//System.out.println(this.simpleString());
					_insertNewBrace("/",_cursor); //leaves us after /
					_cursor.prev();
					_cursor.prev(); //puts us back on first char in double comment
					_updateBasedOnCurrentState();
					if (!_cursor.current().isMultipleCharBrace())
						_cursor.next();
					_cursor.next();
				}

			else if ((_offset > 0) && (_cursor.current().isGap()))
				{
					_insertBraceToGap("/", _cursor);
				}
			
			//if at start of double character brace, break it.
			else if ((_offset == 0) && _cursor.current().isMultipleCharBrace())
				{
					//if we're free there won't be a block comment close so if there
					//is then we don't want to break it.
					_splitCurrentIfCommentBlock(false,_cursor); //leaving us at start
					_checkPreviousInsertSlash(_cursor);
				}
			else
				{
					_checkPreviousInsertSlash(_cursor);
				}
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}

	
	private void _checkPreviousInsertSlash(ModelList<ReducedToken>.Iterator
																				 copyCursor)
		{
			if ( !copyCursor.atStart()	&& !copyCursor.atFirstItem())
				{
					if (copyCursor.prevItem().getType().equals("/") &&
							(copyCursor.prevItem().getState() == ReducedToken.FREE))
						{
							copyCursor.prevItem().setType("//");
							_updateBasedOnCurrentState();
							return;
						}
					// if we're after a star, 
					else if ((getStateAtCurrent() == ReducedToken.INSIDE_BLOCK_COMMENT)
									 && copyCursor.prevItem().getType().equals("*"))
						
						{
							copyCursor.prevItem().setType("*/");
							copyCursor.prevItem().setState(ReducedToken.FREE);
							_updateBasedOnCurrentState();
							return;
						}
				}
			//here we know the / unites with nothing behind it.
			_insertNewBrace("/",copyCursor); //leaving us after the brace.
			copyCursor.prev();
			_updateBasedOnCurrentState();
			if (copyCursor.current().getSize() == 2)
				_offset = 1;
			else
				copyCursor.next();
		}

	
  /**
   * Inserts a new line character (\n) into the reduced model.
	 * 1) atStart: nothing special
	 * 2) atEnd: nothing special
	 * 3) in between multiple char brace: 1) break
	 *                                    2) insert newline in between
	 *                                    3) move two back
	 *                                    4) updateBasedOnCurrentState
	 *                                    5) move two forward, _offset = 0
	 * 4) in between Gap: 1) shrink Gap by _offset
	 *                    2) insert newline
	 *                    3) insert Gap of size _offset
	 *                    4) walk from first Gap
	 *                    5) move two forward, _offset = 0
	 * 5) otherwise, just insert newline, walk, move next, _offset = 0
   */
  public Vector<StateBlock> insertNewline()
		{
			if (_cursor.atStart())
				{
					_insertNewEndOfLine();
				}
			else if (_cursor.atEnd())
				{
					_insertNewEndOfLine();
				}
			else if ((_offset > 0) && _cursor.current().isMultipleCharBrace())
				{
					_splitCurrentIfCommentBlock(true, _cursor);
					_cursor.next();
					_cursor.insert(Brace.MakeBrace("\n", getStateAtCurrent()));
					_cursor.prev();
					_updateBasedOnCurrentState();
					_cursor.next();
					_cursor.next();
					_offset = 0;
				}
			else if ((_offset > 0) && _cursor.current().isGap())
				{
					_insertBraceToGap("\n", _cursor);
				}
			else
				{
					_insertNewEndOfLine();
				}
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}

	private void _insertNewEndOfLine()
		{
			_insertNewBrace("\n", _cursor);
			_cursor.prev();
			_updateBasedOnCurrentState();
			_cursor.next();
			_offset = 0;
		}
	
/**
   * Inserts a double quote (") into the reduced model.
	 * 1) atStart: nothing special
	 * 2) atEnd: nothing special
	 * 3) in between multiple char brace: 1) break
	 *                                    2) insert quote in between
	 *                                    3) move two back
	 *                                    4) updateBasedOnCurrentState
	 *                                    5) move two forward, _offset = 0
	 * 4) in between Gap: 1) shrink Gap by offset
	 *                    2) insert quote
	 *                    3) insert Gap of size offset
	 *                    4) walk from first Gap
	 *                    5) move two forward, _offset = 0
	 * 5) otherwise, just insert, walk, move next, _offset = 0
   */
  public Vector<StateBlock> insertQuote()
		{
			if (_cursor.atStart())
				{
					_insertNewQuote();
				}
			else if (_cursor.atEnd())
				{
					_insertNewQuote();
				}
			else if ((_offset > 0) && _cursor.current().isMultipleCharBrace())
				{
					_splitCurrentIfCommentBlock(true, _cursor);
					_cursor.next();
					_cursor.insert(Brace.MakeBrace("\"", getStateAtCurrent()));
					_cursor.prev();
					_updateBasedOnCurrentState();
					_cursor.next();
					_cursor.next();
					_offset = 0;
				}
			else if ((_offset > 0) && _cursor.current().isGap())
				{
					_insertBraceToGap("\"", _cursor);
				}
			else
				{
					_insertNewQuote();
				}
			return SBVectorFactory.generate(_cursor.copy(),_offset);	
		}

	private void _insertNewQuote()
		{
			_insertNewBrace("\"", _cursor);
			_cursor.prev();
			_updateBasedOnCurrentState();
			_cursor.next();
			_offset = 0;
		}
	
	void insertLineComment()
		{
			insertSlash();
			insertSlash();
		}

	void insertBlockCommentStart()
		{
			insertSlash();
			insertStar();
		}

	void insertBlockCommentEnd()
		{
			insertStar();
			insertSlash();
		}
	
  /**
	 * 0) If at head: check if gap to right, else insert gap / next
	 * 0) If at tail: check if gap to left, else insert gap / next
	 * 1) If inside a gap: grow current gap, move offset by length
	 * 5) Inside a multiple character brace:  a) break brace (updates state
	 *                                                      	 of braces).
	 *                                        b) insert gap
	 *                                        c) goto next / offset = 0	
	 * 2) If gap to left : a) grow gap to left, set offset to zero
	 * 3) If gap to right: can never happen because you would be inside gap.
	 * 4) Inbetween two braces: a) insert gap
	 *                          b) point to next item / offset = 0.
	 * <P>Updates the BraceReduction to reflect the insertion of a
   * regular text string into the document.</P>
   * @param length the length of the inserted string
   */
  public Vector<StateBlock> insertGap( int length )
		{
			//0 - a
			if (_cursor.atStart())
				{
					if (_gapToRight())
						{
							_cursor.next();
							_augmentCurrentGap(length); //increases gap and moves offset
						}
					else
						{
							_insertNewGap(length);//inserts gap and goes to next item
						}
				}
			//0 - b
			else if (_cursor.atEnd())
				{
					if (_gapToLeft())
						{
							_augmentGapToLeft(length);
              //increases the gap to the left and
							//cursor to next item in list leaving offset 0							
						}
					else
						{
							_insertNewGap(length); //inserts gap and moves to next item
						}
				}

			//5
			//offset should never be greater than 1 here because JAVA only has 2
			//char comments
			else if (_cursor.current().isMultipleCharBrace() && (_offset > 0))
				{
					if (_offset > 1)
						throw new IllegalArgumentException("OFFSET TOO BIG:  "+
																							 _offset);
					
					_breakComment(_cursor); //leaves us inside comment
					_insertNewGap(length); //inserts gap and goes to next item
				}
			
			//1
			else if (_cursor.current().isGap())
				{
					_cursor.current().grow(length);
					_offset += length;
				}
			//2
			else if (!_cursor.atFirstItem() &&
							 _cursor.prevItem().isGap())
				{
					//already pointing to next item
					_cursor.prevItem().grow(length);
				}
			//4
			else //between two braces
				{
					_insertNewGap(length); //inserts a gap and goes to the next item
				}
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}

	/**
	 *Wrapper for getStateAtCurrentHelper that returns the current state for
	 *some iterator. This function passes _cursor to the _getStateAtCurrent
	 *Helper to return the current state in the cursor iterator.
	 */
	int getStateAtCurrent()
		{
			return _getStateAtCurrentHelper(_cursor);
		}
	
	private int _getStateAtCurrentHelper(ModelList<ReducedToken>.Iterator temp)
		{
			int state = ReducedToken.FREE;
			if (temp.atFirstItem() || temp.atStart() || _braces.isEmpty())
				state = ReducedToken.FREE;
			else if ( temp.prevItem().isLineComment() ||
								(temp.prevItem().getState() ==
								 ReducedToken.INSIDE_LINE_COMMENT))
				state = ReducedToken.INSIDE_LINE_COMMENT;
			else if ( temp.prevItem().isBlockCommentStart() ||
								(temp.prevItem().getState() ==
								 ReducedToken.INSIDE_BLOCK_COMMENT))
				state = ReducedToken.INSIDE_BLOCK_COMMENT;
			else if ( (temp.prevItem().isQuote() &&
								 temp.prevItem().isOpen()) ||
								(temp.prevItem().getState() ==
								 ReducedToken.INSIDE_QUOTE))
				state = ReducedToken.INSIDE_QUOTE;
			else
				state = ReducedToken.FREE;

			return state;
		}
	
	/**
	 *Before using, make sure not at last, or tail.
	 */
	private boolean _gapToRight()
		{
			return (!_braces.isEmpty() && !_cursor.atEnd() &&
							!_cursor.atLastItem() && _cursor.nextItem().isGap());
		}
	/**
	 *Before using, make sure not at first or head.
	 */
	private boolean _gapToLeft()
		{
			return (!_braces.isEmpty() && !_cursor.atStart() &&
							!_cursor.atFirstItem() &&	_cursor.prevItem().isGap());
		}
	/**
	 *Make sure there is a gap to the left. Increases gap.
	 */
	private void _augmentGapToLeft(int length)
		{
			_cursor.prevItem().grow(length);			
		}

	private void _augmentCurrentGap(int length)
		{
			_cursor.current().grow(length);
			_offset = length;
		}
	
	private void _insertNewGap(int length)
		{
			//getStateAtCurrent returns the state of the current location based
			//upon what the previous item is. If it is //, /*, ", or itself
			//shadowed then getStateAtCurrent returns some commented state, else
			//it returns FREE
			_cursor.insert(new Gap(length, getStateAtCurrent()));
			_cursor.next();
			_offset = 0;
		}

	/**
	 *1)at Head: not special case
	 *2)at Tail: not special case
	 *3)between two things (offset is 0): a) insert,
	 *                                    b) next, offset = 0;
	 *4)inside gap:  a)shrink gap to size of gap - offset.
	 *               b)insert brace
	 *               c)insert gap the size of offset.
	 *               d) 2 * next, offset = 0
	 *5)inside double char comment: a)break
	 *                              b)insert
	 */
	private void _insertBrace(String text)
		{
			if (_cursor.atStart() || _cursor.atEnd())
				{
					_insertNewBrace(text,_cursor); // inserts brace and goes to next
				}
			else if ( _cursor.current().isGap() )
				{
					_insertBraceToGap(text,_cursor);
				}
			else if (_cursor.current().isMultipleCharBrace() && (_offset > 0))
				{
					_breakComment(_cursor); // leave cursor in same place
					_insertNewBrace(text,_cursor);
				}
			else
				{
					_insertNewBrace(text,_cursor);
				}
		}
	private void _insertBraceToGap(String text,
																 ModelList<ReducedToken>.Iterator
																 copyCursor)
		{
			copyCursor.current().shrink(_offset);
			copyCursor.insert(Brace.MakeBrace(text, getStateAtCurrent()));
			copyCursor.insert(new Gap(_offset, getStateAtCurrent()));
			ModelList<ReducedToken>.Iterator copy2 = copyCursor.copy();
			_updateBasedOnCurrentStateHelper(copy2);
			copyCursor.next(); // now pointing at new brace
			copyCursor.next(); // now pointing at second half of gap
			_offset = 0;
		}
	
	private void _insertNewBrace(String text,
															 ModelList<ReducedToken>.Iterator
															 copyCursor)
		{
			copyCursor.insert(Brace.MakeBrace(text, getStateAtCurrent()));
			copyCursor.next();
			_offset = 0;
		}
	private void _insertTestChar(String text)
		{
			_insertBrace(text);
		}
	
	/**
	 * _breakComment is only called if inside a double-character comment
	 * i.e., _offset > 0
	 *1)Line comment:  a)change block comment to "/"
	 *                 b)set state of slash
	 * 								 c)make the next slash and insert it before
	 *	 							 d)move pointer to second slash (old one)
	 *	 							 e)call walk.
	 *	 							 f)offset = 0
	 */
	private void _breakComment(ModelList<ReducedToken>.Iterator copyCursor)
		{
			if (copyCursor.current().isLineComment())
				{
					copyCursor.current().setType("/");
					copyCursor.current().setState(getStateAtCurrent());
					copyCursor.insert(Brace.MakeBrace("/", getStateAtCurrent()));
					copyCursor.next(); // pointing to second slash
					_updateBasedOnCurrentState(); // slashes will not be combined
					//_offset = 0;
				}
			else if (copyCursor.current().isBlockCommentStart())
				{
					copyCursor.current().setType("*");
					copyCursor.current().setState(getStateAtCurrent());
					copyCursor.insert(Brace.MakeBrace("/", getStateAtCurrent()));
					copyCursor.next();
					_updateBasedOnCurrentState();
					//_offset = 0;
				}
			else if (copyCursor.current().isBlockCommentEnd())
				{
					copyCursor.current().setType("/");
					copyCursor.current().setState(getStateAtCurrent());
					copyCursor.insert(Brace.MakeBrace("*", getStateAtCurrent()));
					copyCursor.next();
					_updateBasedOnCurrentState();
					//_offset = 0;
				}
			else
				{
					throw new RuntimeException("_breakComment miscalled!");
				}
		}


/**
 *USE RULES:
 * In the case of inserting between brackets: This should be called from
 *                               between the two characters of the broken
 *                               double comment.
 * In case of deletion of special character start from previous char...
 *                               unless that char is the head.
 *Begins updating at current character.  /./ would not become // because
 * current is in the middle. 
 *Double character comments inside of a quote or a comment are broken.
 */
	
	private void _updateBasedOnCurrentState()
		{
			ModelList<ReducedToken>.Iterator copyCursor = _cursor.copy();
			_updateBasedOnCurrentStateHelper(copyCursor);
		}

	private void _updateBasedOnCurrentStateHelper(
		ModelList<ReducedToken>.Iterator copyCursor)
		{

			if (copyCursor.atStart())
				{
					copyCursor.next();
				}
		 
			// If there's no text after here, nothing to update!
			if (copyCursor.atEnd())
				return;

			int curState = _getStateAtCurrentHelper(copyCursor);
			// Free if at the beginning  
   
		
			switch (curState)
				{
				case Brace.FREE:
					_updateFree(copyCursor);
					break;
				case Brace.INSIDE_QUOTE:
					_updateInsideQuote(copyCursor);
					break;
				case Brace.INSIDE_BLOCK_COMMENT:
					_updateInsideBlockComment(copyCursor);
					break;
				case Brace.INSIDE_LINE_COMMENT:
					_updateInsideLineComment(copyCursor);
					break;
				}

			/*
				System.err.println("New state for current: " + current().getState()
				+ " text=" + this);
			*/
		}

  /** We're current not in a comment or quote. Things we have to update:
   *  0. If we've reached the end of the list, return.
   *  1. If we find / *, * /, or / /, combine them into a single Brace, and
   *     keep the cursor on that Brace.
   *  2. If current brace = //, go to next then call updateLineComment.
   *     If current brace = /*, go to next then call updateBlockComment.
   *     If current brace = ", go to next then call updateInsideQuote.
   *     Else, mark current brace as FREE and go to next brace and recur.
   */
  private void _updateFree(ModelList<ReducedToken>.Iterator copyCursor)
		{
			if (copyCursor.atEnd())
				return;

			_combineCurrentAndNextIfFind("/", "*", copyCursor);
			//_combineCurrentAndNextIfFind("*", "/", copyCursor);
			_combineCurrentAndNextIfFind("/", "/", copyCursor);
			_combineCurrentAndNextIfFind("","", copyCursor);
			//if a / preceeds a /* or a // combine them.
			_combineCurrentAndNextIfFind("/","/*",copyCursor);
			_combineCurrentAndNextIfFind("/","//",copyCursor);
		
			String type = copyCursor.current().getType();
			if (type.equals("*/"))
				{
					_splitCurrentIfCommentBlock(true,copyCursor);
					//_breakComment(copyCursor);
					copyCursor.prev();
					_updateBasedOnCurrentStateHelper(copyCursor);
				}
			else if (type.equals("//"))
				{
					// open comment blocks are not set commented, they're set free
					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateInsideLineComment(copyCursor);
				}
			else if (type.equals("/*"))
				{
					// open comment blocks are not set commented, they're set free
					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateInsideBlockComment(copyCursor);
				}
			else if (type.equals("\""))
				{
					// make sure this is a OPEN quote
					if (copyCursor.current().isClosed())
						copyCursor.current().flip();

					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateInsideQuote(copyCursor);
				}
			else
				{
					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateFree(copyCursor);
				}
		}

  /** We're inside a quoted string. Things we have to update:
   *  0. If we've reached the end of the list, return.
   *  1. If we find //, /* or star-slash, split them into two separate braces.
   *     The cursor will be on the first of the two new braces.
   *  2. If current brace = \n or ", mark current brace FREE, next(), and
   *     go to updateFree.
   *     Else, mark current brace as INSIDE_QUOTE and goto next brace and recur.
   */
  private void _updateInsideQuote(ModelList<ReducedToken>.Iterator copyCursor)
		{
			if (copyCursor.atEnd())
				return;

			_splitCurrentIfCommentBlock(true, copyCursor);
			_combineCurrentAndNextIfFind("","", copyCursor);

			String type = copyCursor.current().getType();

			if (type.equals("\n"))
				{
					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateFree(copyCursor);
				}
			else if (type.equals("\""))
				{
					// make sure this is a CLOSE quote
					if (copyCursor.current().isOpen())
						copyCursor.current().flip();

					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateFree(copyCursor);
				}
			else
				{
					copyCursor.current().setState(Brace.INSIDE_QUOTE);
					copyCursor.next();
					_updateInsideQuote(copyCursor);
				}
		}

  /** We're inside a line comment. Things we have to update:
   *  0. If we've reached the end of the list, return.
   *  1. If we find //, /* or star-slash, split them into two separate braces.
   *     The cursor will be on the first of the two new braces.
   *  2. If current brace = \n, mark current brace FREE, next(), and
   *     go to updateFree.
   *     Else, mark current brace as LINE_COMMENT and goto next brace and recur.
   */
  private void _updateInsideLineComment(ModelList<ReducedToken>.Iterator copyCursor)
		{
			if (copyCursor.atEnd())
				return;

			_splitCurrentIfCommentBlock(true, copyCursor);
			_combineCurrentAndNextIfFind("","", copyCursor);

			String type = copyCursor.current().getType();

			if (type.equals("\n"))
				{
					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateFree(copyCursor);
				}
			else
				{
					copyCursor.current().setState(Brace.INSIDE_LINE_COMMENT);
					copyCursor.next();
					_updateInsideLineComment(copyCursor);
				}
		}

  /** We're inside a block comment. Things we have to update:
   *  0. If we've reached the end of the list, return.
   *  1. If we find * /, combine it into a single Brace, and
   *     keep the cursor on that Brace.
   *  2. If we find // or /*, split that into two Braces and keep the cursor
   *     on the first one.
   *  3. If current brace = star-slash, mark the current brace as FREE,
   *     go to the next brace, and call updateFree.
   *     Else, mark current brace as INSIDE_BLOCK_COMMENT
   *     and go to next brace and recur.
   */
  private void _updateInsideBlockComment(ModelList<ReducedToken>.Iterator copyCursor)
		{
			if (copyCursor.atEnd())
				return;

			_combineCurrentAndNextIfFind("*", "/", copyCursor);
			_combineCurrentAndNextIfFind("*","//", copyCursor);
			_combineCurrentAndNextIfFind("*","/*", copyCursor);
			_combineCurrentAndNextIfFind("","", copyCursor);
			_splitCurrentIfCommentBlock(false, copyCursor);

			String type = copyCursor.current().getType();
			if (type.equals("*/"))
				{
					copyCursor.current().setState(Brace.FREE);
					copyCursor.next();
					_updateFree(copyCursor);
				}
		
			else
				{
					copyCursor.current().setState(Brace.INSIDE_BLOCK_COMMENT);
					copyCursor.next();
					_updateInsideBlockComment(copyCursor);
				}
		}

  /** If we have braces of first and second in immediate succession, and if
   *  second's gap is 0, combine them into first+second.
   *  The cursor remains on the same block after this method is called.
   *  @return true if we combined two braces or false if not
   */
  private boolean
	_combineCurrentAndNextIfFind(String first, String second,
															 ModelList<ReducedToken>.Iterator copyCursor)
		{

			if (copyCursor.atStart() ||
					copyCursor.atEnd() ||
					copyCursor.atLastItem() ||
					!copyCursor.current().getType().equals(first))
				return false;

			copyCursor.next(); // move to second one to check if we can combine

			// The second one is eligible to combine if it exists (atLast is false),
			// if it has the right brace type, and if it has no gap.
			if (copyCursor.current().getType().equals(second))        
				{
					if ((copyCursor.current().getType().equals("//"))&&
							(copyCursor.prevItem().getType().equals("*")))
						{//now pointing to //
							copyCursor.current().setType("/");
							copyCursor.prev();
							copyCursor.current().setType("*/");
							copyCursor.current().setState(ReducedToken.FREE);
							return true;
						}
					else if ((copyCursor.current().getType().equals("/*"))&&
									 (copyCursor.prevItem().getType().equals("*")))
						{
							copyCursor.current().setType("*");
							copyCursor.prev();
							copyCursor.current().setType("*/");
							copyCursor.current().setState(ReducedToken.FREE);
							return true;
						}
					else if ((copyCursor.current().getType().equals("/*"))&&
									 (copyCursor.prevItem().getType().equals("/")))
						{
							copyCursor.current().setType("*");
							copyCursor.prev();
							copyCursor.current().setType("//");
							copyCursor.current().setState(ReducedToken.FREE);
							return true;
						}
					else if ((copyCursor.current().getType().equals("//"))&&
									 (copyCursor.prevItem().getType().equals("/")))
						{
							copyCursor.current().setType("/");
							copyCursor.prev();
							copyCursor.current().setType("//");
							copyCursor.current().setState(ReducedToken.FREE);
							return true;
						}
					else if ((copyCursor.current().getType().equals(""))&&
									 (copyCursor.prevItem().getType().equals("")))
						{
							// delete first Gap and augment the second
							copyCursor.prev();
							int growth = copyCursor.current().getSize();
							copyCursor.remove();
							copyCursor.current().grow(growth);
							return true;
						}
					// delete the first Brace and augment the second
					copyCursor.prev();
					copyCursor.remove();
					copyCursor.current().setType(first + second);
					return true;
				}

			// we couldn't combine, so move back and return
			copyCursor.prev();
			return false;
		}


/** If the current brace is a // or /*, split it into two braces.
   *  Do the same for star-slash (end comment block) as well if
   *  the parameter splitClose is true.
   *  If a split was performed, the first of the two Braces
   *  will be the current one when we're done.
   *  The offset is not changed.
   *  The two new Braces will have the same quoted/commented status
   *  as the one they were split from.
   */
  private void
	_splitCurrentIfCommentBlock(boolean splitClose,
															ModelList<ReducedToken>.Iterator copyCursor)
		{
			String type = copyCursor.current().getType();
			if (type.equals("//") ||
					type.equals("/*") ||
					(splitClose && type.equals("*/")))
				{
					String first = type.substring(0, 1);
					String second = type.substring(1, 2);
					// change current Brace to only be first character
					copyCursor.current().setType(first);
					int oldState = copyCursor.current().getState();

					// then put a new brace after the current one
					copyCursor.next();
					copyCursor.insert( Brace.MakeBrace(second, oldState) );
					// Move back to make the first brace we inserted current
					copyCursor.prev();
				}
		}
	
  /**
   * <P>Updates the BraceReduction to reflect cursor movement.
   * Negative values move left from the cursor, positive values move
   * right. </P>
   * @param count indicates the direction and magnitude of cursor movement
   */
	public void move(int count)
		{
			_offset = _move(count, _cursor, _offset);			
		}
	
  private int _move(int count, ModelList<ReducedToken>.Iterator copyCursor,
										int currentOffset)
		{
			int retval = currentOffset;
			ModelList<ReducedToken>.Iterator copyCursor2 = copyCursor.copy();
			
			if (count == 0)
				return retval;
			//make copy of cursor and return new iterator?
			else if (count > 0)
				retval = _moveRight(count,copyCursor2,currentOffset);
			else
				retval = _moveLeft(Math.abs(count),copyCursor2,currentOffset);

			copyCursor.setTo(copyCursor2);
			return retval;
		}

	/**
	 * 1) at head && count>0:  next
	 * 2) LOOP:
	 *     at tail:
	 *        a)count ==0
	 *            stop;
	 *        b)else exception
	 * 3)  count < size
	 *        a) _offset = count;
	 *              stop;
	 * 4)  else
	 *        a) count = count - size
	 *           next()
	 *           loop
	 */
	private int _moveRight(int count,
												 ModelList<ReducedToken>.Iterator copyCursor,
												 int currentOffset)
		{
			if (copyCursor.atStart()){
				currentOffset = 0;
				copyCursor.next();
			}
			if (copyCursor.atEnd())
				throw new IllegalArgumentException("At end");
			
			while (count >= copyCursor.current().getSize() - currentOffset){
				count = count - copyCursor.current().getSize()+currentOffset;
				copyCursor.next();
				currentOffset = 0;
				if (copyCursor.atEnd()){
					if (count == 0)
						break;
					else {throw new IllegalArgumentException("Moved into tail");}
				}
			}
			return count+currentOffset; //returns the offset
		}

	private int _moveLeft(int count,
												ModelList<ReducedToken>.Iterator copyCursor,
												int currentOffset)
		{
			if (copyCursor.atEnd()){
				copyCursor.prev();
				if (!copyCursor.atStart()) //make sure list not empty
					currentOffset = copyCursor.current().getSize();
			}
			
			if (copyCursor.atStart())
				throw new IllegalArgumentException("At Start");
		
			while (count > currentOffset){
				count = count - currentOffset;
				copyCursor.prev();
				
				if (copyCursor.atStart()){
					if (count > 0)
						throw new IllegalArgumentException("At Start");
					else {
						copyCursor.next();
						currentOffset = 0;
					}
				}
				else {
					currentOffset = copyCursor.current().getSize();
				}
			}
			return currentOffset - count;
		}		 
												
											 
											 
  /**
   * <P>Update the BraceReduction to reflect text deletion.</P>
   * @param count indicates the size and direction of text deletion.
   * Negative values delete text to the left of the cursor, positive
   * values delete text to the right.
	 * Always move count spaces to make sure we can delete.
   */
  public Vector<StateBlock> delete( int count )
		{
			if (count == 0)
				return SBVectorFactory.generate(_cursor.copy(),_offset);;
			ModelList<ReducedToken>.Iterator copyCursor = _cursor.copy();
			// from = the _cursor
			// to = _cursor.copy()
			_offset = _delete(count, _offset, _cursor, copyCursor);
			return SBVectorFactory.generate(_cursor.copy(),_offset);
		}

	private int _delete(int count, int offset,
											ModelList<ReducedToken>.Iterator delFrom,
											ModelList<ReducedToken>.Iterator delTo)
		{
			
				
			//gaurentees that its possible to delete count characters
			if (count >0){
				int endOffset = -1;
				try {
					endOffset = _move(count,delTo, offset);
				}
				catch (Exception e) {
					throw new IllegalArgumentException("Trying to delete" +
																						 " past end of file.");				
				}
				return _deleteRight(offset, endOffset,delFrom, delTo);
			}
			else {//(count < 0)
				int startOffset = -1;
				try {
					startOffset = _move(count,delFrom, offset);
				}
				catch (Exception e) {
					throw new IllegalArgumentException("Trying to delete" +
																						 " past end of file.");				
				}

				return _deleteRight(startOffset,offset, delFrom, delTo);
			}
		}
	
	private int _deleteLeft(int offset,
													ModelList<ReducedToken>.Iterator delFrom,
													ModelList<ReducedToken>.Iterator delTo)
		{
			return -1;
		}
	
	/**
	 *!!!!!!!COMBINE GAPS!!!!!!!!!!!
	 */
	private int _deleteRight(int offset,int endOffset,
													 ModelList<ReducedToken>.Iterator delFrom,
													 ModelList<ReducedToken>.Iterator delTo)
		{
			
		

			delFrom.collapse(delTo);
						
			// if both pointing to same item, and it's a gap
			if (delFrom.eq(delTo) && delFrom.current().isGap()){
				// inside gap
				delFrom.current().shrink(endOffset-offset);
				return offset;
			}

			//if brace is multiple char it must be a comment because the above if
 			//test gaurentees it can't be a gap.
			if (!delFrom.eq(delTo))
				_clipLeft(offset, delFrom);

			_clipRight(endOffset, delTo);			

			if (!delFrom.atStart())
				delFrom.prev();
					
			//int delToSizePrevious = delTo.current().getSize();
			//String delToTypePrevious = delTo.current().getType();
			int delToSizeCurr;
			String delToTypeCurr;
			if (delTo.atEnd()){
				_updateBasedOnCurrentState();
				delFrom.setTo(delTo);
				return 0;
			}
			else{
				delToSizeCurr = delTo.current().getSize();
				delToTypeCurr = delTo.current().getType();
			}
					
			//get info on previous item.
			delTo.prev();//get stats on previous item
			
			int delToSizePrev;
			String delToTypePrev;
			if (delTo.atStart()){//no previous item, can't be at end
				delTo.next();
				_updateBasedOnCurrentStateHelper(delFrom);
				delFrom.setTo(delTo);
				return 0;
			}
			else{
				delToSizePrev = delTo.current().getSize();
				delToTypePrev = delTo.current().getType();
			}
			delTo.next(); //put delTo back on original node


			_updateBasedOnCurrentState();

			int temp =
				_calculateOffset(delToSizePrev,delToTypePrev,
												 delToSizeCurr, delToTypeCurr,
												 delTo);
			delFrom.setTo(delTo);
			return temp;
		}
	

	private void _clipLeft(int offset, ModelList<ReducedToken>.Iterator
												 copyCursor)
		{
			if (copyCursor.atStart()){
				return;
			}
			else if (offset == 0){
				copyCursor.remove();
			}
			else if (copyCursor.current().isGap()){
				int size = copyCursor.current().getSize();
				copyCursor.current().shrink(size-offset);
			}
			else if (copyCursor.current().isMultipleCharBrace()){
				if (offset != 1)
					throw new IllegalArgumentException("Offset incorrect");
				else{
					String type = copyCursor.current().getType();
					String first = type.substring(0,1);
					copyCursor.current().setType(first);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot clip left.");
			}
		}
	

	private void _clipRight(int offset, ModelList<ReducedToken>.Iterator
													copyCursor)
		{
			if (copyCursor.atEnd()){
				return;
			}
			else if (offset == 0) {
				return;
			}
			else if (offset == copyCursor.current().getSize()){
				copyCursor.remove();
			}
			else if (copyCursor.current().isGap()){
				copyCursor.current().shrink(offset);
			}
			else if (copyCursor.current().isMultipleCharBrace()){
				if (offset != 1)
					throw new IllegalArgumentException("Offset incorrect");
				else{
					String type = copyCursor.current().getType();
					String second = type.substring(1,2);
					copyCursor.current().setType(second);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot clip left.");
			}
		}

	/**
	 *By contrasting the delTo token after the walk to what it was before the
	 *walk we can see how it has changed and where the offset should go.
	 */
	private int _calculateOffset(int delToSizePrev, String delToTypePrev,
															 int delToSizeCurr, String delToTypeCurr,
															 ModelList<ReducedToken>.Iterator delTo)
		{			
			int delToSizeChange = delTo.current().getSize();
			String delToTypeChange = delTo.current().getType();

			//1)if there was a gap previous to the gap at delTo delTo should be
			//augmented by its size, and that size is the offset.
			//2)if the gap was not preceeded by a gap then it would not need to
			//be shrunk
			if (delTo.atEnd())
				throw new IllegalArgumentException("Shouldn't happen");
			if (delTo.current().isGap())
				return delToSizeChange - delToSizeCurr;
			//this means that the item at the end formed a double brace with the
			//item that the delete left preceeding it. /dddddd*

			//the final item shrunk. This can only happen if the starting item
			//stole one of its braces: /ddddd*/
			//or if it was a double brace that had to get broken because it was
			//now commented or no longer has an open block

			//EXAMPLES: /*___*/  becoming */
			//          /*___*/  delete the first star, through the spaces to get
			//                   /*/
			//         //*__\n// becoming //*__//, the // is broken
			//         //*__\n// becoming ////   , the // is broken
			//THIS MUST HAVE THE previous items size and type passed in from
			//before the update. This way we know how it's changing too.
				
			// case of /
			if (delToTypePrev.equals("/")){
				//  /-/* becoming //-*
				if(delToTypeCurr.equals("/*") && 
					 _checkPrevEquals(delTo,"//")){ //because pointer will be at *
					delTo.prev();
					return 1;
				}
				else if (delToTypeCurr.equals("//") &&
								 _checkPrevEquals(delTo,"//")){
					delTo.prev();
					return 1;
				}
				else if (delToTypeCurr.equals("*/") && //changed
								 delTo.current().getType().equals("/*")){										
					return 1;
				}
				else if (delToTypeCurr.equals("*") &&
								 delTo.current().getType().equals("/*"))
					return 1;
				else if (delToTypeCurr.equals("/") &&
								 delTo.current().getType().equals("//"))
					return 1;
			}
			//case of *
			else if (delToTypePrev.equals("*")){
				//  /-/* becoming //-*
				if(delToTypeCurr.equals("/*") && 
					 _checkPrevEquals(delTo,"*/")){ //because pointer will be at *
					delTo.prev();
					return 1;
				}
				else if (delToTypeCurr.equals("//") &&
								 _checkPrevEquals(delTo,"*/")){
					delTo.prev();
					return 1;
				}
				else if (delToTypeCurr.equals("/") &&
								 delTo.current().getType().equals("*/"))
					return 1;							
			}
							
			return 0;
		}

	private boolean _checkPrevEquals(ModelList<ReducedToken>.Iterator delTo,
																	 String match)
		{
			if (delTo.atFirstItem() || delTo.atStart())
				return false;

			return delTo.prevItem().getType().equals(match);
		}

  /** If the current brace is a /, a *, a // or a \n, it's not matchable.
   *  This means it is ignored on balancing and on next/prev brace finding.
   *  All other braces are matchable.
   */
  private boolean _isCurrentBraceMatchable(
		ModelList<ReducedToken>.Iterator copyCursor)
		{
			String type = copyCursor.current().getType();

			return (!copyCursor.current().isGap() &&
							!(type.equals("/")  ||
								type.equals("*")  ||
								type.equals("\n") ||
								type.equals("//")) &&
							!copyCursor.current().isShadowed());
		}
  

/**
 *returns distance from current location of cursor to the location of the
 *previous significant brace.
 *ex. (...|)  where | signifies the cursor. previousBrace returns 4 because
 *it goes to the spot behind the (.
 * /|* returns this brace since you're in the middle of it and going
 *backward can find it.
 */
	public int previousBrace()															
    {
			int dist = 0;
		
			ModelList<ReducedToken>.Iterator copyCursor = _cursor.copy();
			if (copyCursor.atEnd() ||
				(!copyCursor.atStart()&& _offset == 0))
				copyCursor.prev();
			
			if (_offset > 0){
				if (_isCurrentBraceMatchable(copyCursor))
					return _offset;
				else{
					dist = dist +_offset;
					copyCursor.prev();
				}
			}
      // if we're in the middle of the first brace element, we're
      // not going to find any previous braces
      			
			while (!copyCursor.atStart()){				
				if (_isCurrentBraceMatchable(copyCursor))
					return dist + copyCursor.current().getSize();
				else{
					dist = dist + copyCursor.current().getSize();
					copyCursor.prev();
				}
			}
			return -1;
		}

  /**
	 *Goes to the location before the brace. |...( where | is the cursor,
	 *returns three since it is three moves to the location of the (
	 *NOTE: /|* returns the next brace. It does not return this brace because
	 *you are past it.
   */
  public int nextBrace()
    {
			int dist = 0;
			ModelList<ReducedToken>.Iterator copyCursor = _cursor.copy();
			
			if ( copyCursor.atStart())
				copyCursor.next();
			if (_offset > 0){
				dist = dist + _offset;
				copyCursor.next();
			}
      // there are no braces on the last brace element - it's empty
      while (!copyCursor.atEnd() ){
				if (_isCurrentBraceMatchable(copyCursor))
					return dist;
				else{
					dist = dist + copyCursor.current().getSize();
					copyCursor.next();
				}
			}					
			return -1;
		}
 


  /**
	 * If the current ReducedToken is an open significant brace and the
	 * offset is 0 (i.e., if we're immediately left of said brace),
	 * push the current Brace onto a Stack and iterate forwards,
	 * keeping track of the distance covered.
	 * - For every closed significant Brace, if it matches the top of the Stack,
	 *   pop the Stack.  Increase the distance by the size of the Brace.
	 *   If the Stack is Empty, we have a balance.  Return distance.
	 *   If the closed Brace does not match the top of the Stack, return -1;
	 *   We have an unmatched open Brace at the top of the Stack.
	 * - For every open significant Brace, push onto the Stack.
	 *   Increase distance by size of the Brace, continue.
	 * - Anything else, increase distance by size of the ReducedToken, continue.
   */
  public int balanceForward()
    {
			Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
			ModelList<ReducedToken>.Iterator iter = _cursor.copy();

			if (iter.atStart())
				iter.next();

			// here we check to make sure there is an open significant brace
			// immediately to the right of the cursor
			if (!iter.atEnd() &&
					_isCurrentBraceMatchable(iter) &&
					iter.current().isOpen() &&
					_offset == 0)
				{
					// initialize the distance and the stack with the first brace,
					// the one we are balancing
					int distance = 0;
					braceStack.push(iter.current());
					distance += iter.current().getSize();
					iter.next();

					// either we get a match and the stack is empty
					// or we reach the end of a file and haven't found a match
					// or we have a closed brace that doesn't have a match,
					//    so we abort
					while (!iter.atEnd() && !braceStack.isEmpty())
						{
							if (_isCurrentBraceMatchable(iter)) {
								// closed
								if (iter.current().isClosed()) {
									ReducedToken popped = braceStack.pop();
									if (!iter.current().isMatch(popped))
										return -1;
								}
								// open
								else {
									braceStack.push(iter.current());
								}
							}
							// no matter what, we always want to increase the distance
							// by the size of the token we have just gone over
							distance += iter.current().getSize();
							iter.next();
						}
					
					// we couldn't find a match
					if (!braceStack.isEmpty())
						return -1;
					// success
					else
						return distance;
				}
			// not the right initial conditions 
			else
				return -1;			
    }
	/* 
	 * If the previous ReducedToken is a closed significant brace,
	 * offset is 0 (i.e., if we're immediately right of said brace),
	 * push the previous Brace onto a Stack and iterate backwards,
	 * keeping track of the distance covered.
	 * - For every open significant Brace, if it matches the top of the Stack,
	 *   pop the Stack.  Increase the distance by the size of the Brace.
	 *   If the Stack is Empty, we have a balance.  Return distance.
	 *   If the open Brace does not match the top of the Stack, return -1;
	 *   We have an unmatched closed Brace at the top of the Stack.
	 * - For every closed significant Brace, push onto the Stack.
	 *   Increase distance by size of the Brace, continue.
	 * - Anything else, increase distance by size of the ReducedToken, continue.
   */

  public int balanceBackward()
    { 
			Stack<ReducedToken> braceStack = new Stack<ReducedToken>();
			ModelList<ReducedToken>.Iterator iter = _cursor.copy();

			if (iter.atStart() || iter.atFirstItem())
				return -1;
			
			iter.prev();
			// here we check to make sure there is an open significant brace
			// immediately to the right of the cursor
			if (_isCurrentBraceMatchable(iter) &&
					iter.current().isClosed() &&
					_offset == 0)
				{
					// initialize the distance and the stack with the first brace,
					// the one we are balancing
					int distance = 0;
					braceStack.push(iter.current());
					distance += iter.current().getSize();
					iter.prev();

					// either we get a match and the stack is empty
					// or we reach the start of a file and haven't found a match
					// or we have a open brace that doesn't have a match,
					//    so we abort
					while (!iter.atStart() && !braceStack.isEmpty())
						{
							if (_isCurrentBraceMatchable(iter)) {
								// open
								if (iter.current().isOpen()) {
									ReducedToken popped = braceStack.pop();
									if (!iter.current().isMatch(popped))
										return -1;
								}
								// closed
								else {
									braceStack.push(iter.current());
								}
							}
							// no matter what, we always want to increase the distance
							// by the size of the token we have just gone over
							distance += iter.current().getSize();
							iter.prev();
						}
					
					// we couldn't find a match
					if (!braceStack.isEmpty())
						return -1;
					// success
					else
						return distance;
				}
			// not the right initial conditions 
			else
				return -1;				
    }	
}
