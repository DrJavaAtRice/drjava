/* $Id$ */

package edu.rice.cs.drjava;
/** @author Mike Yantosca */

/**
 * This class acts as the representation of a brace in the reduced
 * view.  It also includes information about the gap of plaintext
 * preceding the actual brace before the previous brace or the start
 * of the file.
 */

class Brace extends ReducedToken {

	/**
	 * @see String
	 */
  public static final String[] braces = { "{", "}",
																					"(", ")",
																					"[", "]",
																					"/*", "*/",
																					"//", "\n",
                                          "/", "*",
																					"\"", "\"",
																					"" };


  public static final String BLK_CMT_BEG = "/*";
  public static final String BLK_CMT_END = "*/";
  public static final String EOLN = "\n";
  public static final String LINE_CMT = "//";
  public static final String QUOTE = "\"";
	public static final String STAR = "*";
	public static final String SLASH = "/";
  /** the type of the brace */
  protected int _type;
  
  /**
   * Virtual constructor.
   * This method throws an exception if the given type is not a valid
   * brace type.
   * @param type the brace text
   * @param state whether the brace is shadwowed by a comment, quote etc
   * @return a new Brace if type is valid, otherwise null
   */
  public static Brace MakeBrace( String type, int state )
    {
      int index = findBrace( type );
      if ( index == braces.length )
				{
					throw new BraceException( "Invalid brace type \"" + type + "\"" );
				}
      else
				{
					return new Brace( index, state );
				}
    }
  /**
   * Constructor.
   * @param type the brace type
   * @param width the size of the brace and its gap
   */
  private Brace( int type, int state )
    {
      _type = type;
      _state = state;
    }
  
  /**
   * Get the text of the brace.
   * @return the text of the Brace
   */
  public String getType()
    {
      return ( _type == braces.length ) ? "!" : braces[_type];
    }
  
  /**
   * @return the size of the brace and its preceding gap
   */
  public int getSize()
    {
      return getType().length();
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
      for( i = 0; i < getSize(); i++ )
				{
					val += " ";
					val += getType().charAt( i );
				}
      return val;
    }

  /**
   * Flips the orientation of the brace.
   * Useful for updating quote information.
   */
  public void flip()
    {
      if ( isOpen() )
				_type += 1;
      else if ( _type < braces.length - 1 )
				_type -= 1;	
    }
  
  /**
   * Indicates whether this is an opening brace.
   * @return true if the brace is an opening brace.
   */
  public boolean isOpen()
    {
      return (( ( _type % 2 ) == 0 ) &&
							( _type < braces.length - 1 ));
    }
  
  /**
   * Indicates whether this is a closing brace.
   * @return true if the brace is a closing brace.
   */
  public boolean isClosed()
    {
      return !isOpen();
    }

  /** Reset the type of this brace. */
  public void setType(String type)
  {
    String oldType = braces[_type];

    int index = findBrace( type );
    if ( index == braces.length )
      {
        throw new BraceException( "Invalid brace type \"" + type + "\"" );
      }
    else
      {
        _type = index;
      }
  }


  /**
   * Determine the brace type of a given String.
   * The integer value returned is only used internally.
   * Externally, the brace shows the text as its "type".
   * @param type the text of the brace
   * @return an integer indicating the type of brace
   */  
  protected static int findBrace( String type )
    {
      int i;
      
      for( i = 0; i < braces.length; i++ )
				{
					if ( type.equals( braces[i] ) )
						break;
				}
      
      return i;
    }
  
  
  /**
   * Check if two braces match.
   * @param other the brace to compare
   * @return true if this is a match for other.
   */
  public boolean isMatch( ReducedToken other )
    {
      if ( this.getType().equals("") )
				return false;
      int off = ( this.isOpen() ) ? 1 : -1;
      return ( braces[_type + off].equals( other.getType() ));
    }

  /**
   * @return true if this is a quote
   */
  public boolean isQuote()
    {
      return this.getType().equals( QUOTE );
    }

  /**
   * @return true if this is a line comment delimiter
   */
  public boolean isLineComment()
    {
      return this.getType().equals( LINE_CMT );
    }

  /**
   * @return true if this is a block comment open delimiter
   */
  public boolean isBlockCommentStart()
    {
      return this.getType().equals( BLK_CMT_BEG );
    }

  /**
   * @return true if this is a block comment close delimiter
   */
  public boolean isBlockCommentEnd()
    {
      return this.getType().equals( BLK_CMT_END );
    }

  /**
   * @return true if this is a newline delimiter
   */
  public boolean isNewline()
    {
      return this.getType().equals( EOLN );
    }

	public boolean isMultipleCharBrace()
		{
			return (isLineComment() || isBlockCommentStart() || isBlockCommentEnd());
		}

	public boolean isGap()
		{
			return false;
		}

	public boolean isSlash()
		{
			return this.getType().equals( SLASH );
		}

	public boolean isStar()
		{
			return this.getType().equals( STAR );
		}

	public void grow(int delta)
		{
			throw new RuntimeException("Braces can't grow.");			
		}

	public void shrink(int delta)
		{
			throw new RuntimeException("Braces can't shrink.");
		}
}

/**
 * An exception class used by methods in this class.
 */
class BraceException extends RuntimeException
{
  BraceException( String s )
    {
      super( s );
    }
}

