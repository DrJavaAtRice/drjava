package edu.rice.cs.drjava;

import gj.util.Vector;
import java.awt.Color;

class NoUpdateMessage extends StyleUpdateMessage
{
	public NoUpdateMessage()
		{
			super();
		}
	public void dispatch(DefinitionsDocument doc,
											 Vector<StyleUpdateMessage> queue)
		{
		}
}
