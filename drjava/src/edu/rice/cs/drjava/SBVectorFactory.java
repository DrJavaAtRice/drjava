package edu.rice.cs.drjava;

import gj.util.Vector;
import java.awt.*;

public class SBVectorFactory
{
	public SBVectorFactory()
		{			
		}

	/**
	 *@param adjustment the amount to shift each start location by.
	 */
	public static Vector<StateBlock> generate
	(ModelList<ReducedToken>.Iterator it, int offset,int adjustment)
		{
			//can never be at head at start.
			Vector<StateBlock> blocks = new Vector<StateBlock>();
			Color prevState = null;
			Color currState = null;
			int length = 0;
			int start = adjustment;
			//if innitially at Start... begin on first char.
			
			//must check the previous character's state
			if (!it.atStart())
				it.prev();
				
			if (it.atStart())
					it.next();
			else
				start = start + (-1 * it.current().getSize());

			if (!it.atEnd())
				prevState = it.current().getHighlight();
			
			while (!it.atEnd()){
				currState = it.current().getHighlight();
				if (prevState.equals(currState)){
					length = length + it.current().getSize();
					it.next();
				}
				else {
					blocks.addElement(new StateBlock(start,length,prevState));
					prevState = currState;
					start = start + length; 
					length = 0;
				}	
			}
			if (length > 0)
				blocks.addElement(new StateBlock(start,length,prevState));
			return blocks;
		}
}



