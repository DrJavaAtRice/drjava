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
}
