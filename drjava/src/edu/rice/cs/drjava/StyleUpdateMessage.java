package edu.rice.cs.drjava;

import gj.util.Vector;
import javax.swing.text.SimpleAttributeSet;

abstract class StyleUpdateMessage
{
	protected DefinitionsDocument target;
	protected SimpleAttributeSet attributes;
	public StyleUpdateMessage()
		{
				attributes = new SimpleAttributeSet();
		}
	public abstract void dispatch(DefinitionsDocument doc,
																Vector<StyleUpdateMessage> queue);
}
