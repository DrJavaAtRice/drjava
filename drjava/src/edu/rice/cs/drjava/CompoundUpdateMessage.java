package edu.rice.cs.drjava;

import gj.util.Vector;

class CompoundUpdateMessage extends StyleUpdateMessage
{
	private Vector<StateBlock> _states;

	CompoundUpdateMessage()
		{
			this._states = new Vector<StateBlock>();
		}
	public CompoundUpdateMessage(Vector<StateBlock> states)
		{
			this._states = states;
		}

	void addElement(StateBlock s)
		{
			_states.addElement(s);
		}

	public StateBlock elementAt(int pos)
		{
			return _states.elementAt(pos);
		}

	public int size()
		{
			return _states.size();
		}
}
