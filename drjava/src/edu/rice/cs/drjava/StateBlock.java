package edu.rice.cs.drjava;

import java.awt.*;

public class StateBlock
{
	public static final Color BLOCK_COMMENT_COLOR = Color.red;
	public static final Color LINE_COMMENT_COLOR = Color.blue;
	public static final Color QUOTE_COLOR = Color.green;
	public static final Color DEFAULT_COLOR = Color.black;
	
	public int location; //location relative to current cursor.
	public int size;
	public Color state;
	
	public StateBlock (int location, int size, Color state)
		{
			this.location = location;
			this.size = size;
			this.state = state;
		}

	
	public boolean equals(Object other)
		{
			return ((this.getClass() == other.getClass()) &&
							(this.location == ((StateBlock)(other)).location) &&
							(this.size == ((StateBlock)(other)).size) &&
							(this.state.equals(((StateBlock)(other)).state)));
		}

	public String toString()
		{
			String hilite = "";
		 
			if (state.equals(BLOCK_COMMENT_COLOR))
				hilite = "BLOCK";
			if (state.equals(LINE_COMMENT_COLOR))
				hilite = "LINE";
			if (state.equals(QUOTE_COLOR))
				hilite = "QUOTE";
			else //state.equals(DEFAULT_COLOR)
				hilite = "DEFAULT";
												
			return ("loc=" + this.location + ", " +
							"size=" + this.size + "," +
							"state=" + hilite);
		}
}









