package edu.rice.cs.drjava;

import gj.util.Vector;
import java.awt.Color;

class SimpleUpdateMessage extends StyleUpdateMessage
{
	private StateBlock _stateBlock;

	public SimpleUpdateMessage(int start, int length, Color highlight)
		{
			this._stateBlock = new StateBlock(start, length, highlight);
		}

	public StateBlock getStateBlock() {
		return this._stateBlock;
	}
}
