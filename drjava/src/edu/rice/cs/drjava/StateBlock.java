package edu.rice.cs.drjava;

public class StateBlock
{
	public int location; //location relative to current cursor.
	public int size;
	public int state;

	public StateBlock (int location, int size, int state)
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
							(this.state == ((StateBlock)(other)).state));
		}

	public String toString()
		{
			String hilite = "";
			switch(this.state){
			case ReducedToken.FREE: hilite = "FREE";break;
			case ReducedToken.INSIDE_LINE_COMMENT: hilite = "LINE";break;
			case ReducedToken.INSIDE_BLOCK_COMMENT: hilite = "BLOCK";break;
			case ReducedToken.INSIDE_QUOTE: hilite = "QUOTE";break;
			}
			return ("loc=" + this.location + ", " +
							"size=" + this.size + "," +
							"state=" + hilite);
		}
}

