/* $Id$ */

package edu.rice.cs.drjava;

class Gap extends ReducedToken
{
	private int _size;
	
	Gap(int size, int state)
		{
			_size = size;
			_state = state;
		}

	public int getSize()
		{
			return _size;			
		}

	public String getType()
		{
			return "";
		}

	public void setType(String type)
		{
			throw new RuntimeException("Can't set type on Gap!");
		}

	public void flip()
		{
			throw new RuntimeException("Can't flip a Gap!");
		}

	/**
   * Increases the size of the gap.
   * @param delta the amount by which the gap is augmented.
   */
  public void grow( int delta )
    {
      if ( delta >= 0 )
				_size += delta;
    }
  
  /**
   * Decreases the size of the gap.
   * @param delta the amount by which the gap is diminished.
   */
  public void shrink( int delta )
    {	
      if (( delta <= _size ) && ( delta >= 0 ))
				_size -= delta;
    }
  /**
   * Converts a Brace to a String.
   * Used for debugging.
   * @return the string representation of the Brace.
   */
  public String toString()
    {
      String val = "";
      int i;
      for( i = 0; i < _size; i++ )
				{
					val += " _";
				}
      return val;
    }

	public boolean isMultipleCharBrace()
		{
			return false;
		}

	public boolean isGap()
		{
			return true;
		}

	public boolean isLineComment()
		{
			return false;
		}

	public boolean isBlockCommentStart()
		{
			return false;
		}

	public boolean isBlockCommentEnd()
		{
			return false;
		}

	public boolean isNewline()
		{
			return false;
		}

	public boolean isSlash()
		{
			return false;
		}

	public boolean isStar()
		{
			return false;
		}

	public boolean isQuote()
		{
			return false;
		}
	public boolean isDoubleEscapeSequence()
		{
			return false;
		}

	public boolean isDoubleEscape()
		{
			return false;
		}

	public boolean isEscapedQuote()
		{
			return false;
		}
	
	public boolean isOpen()
		{
			return false;
		}

	public boolean isClosed()
		{
			return false;
		}

	public boolean isMatch(ReducedToken other)
		{
			return false;
		}

	public boolean isOpenBrace()
		{
			return false;
		}

	public boolean isClosedBrace()
		{
			return false;
		}
}
