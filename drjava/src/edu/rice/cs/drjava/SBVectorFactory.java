package edu.rice.cs.drjava;

import gj.util.Vector;

public class SBVectorFactory
{
	public SBVectorFactory()
		{			
		}

	public static Vector<StateBlock> generate
	(ModelList<ReducedToken>.Iterator it, int offset)
		{
			//can never be at head at start.
			Vector<StateBlock> blocks = new Vector<StateBlock>();
			int prevState = -1;
			int currState;
			int length = 0;
			int start = -1 * offset;
			
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
				if (prevState == currState){
					length = length + it.current().getSize();
					it.next();
				}
				else {
					if (prevState != ReducedToken.FREE)
						blocks.addElement(new StateBlock(start,length,prevState));
					prevState = currState;
					start = start + length; 
					length = 0;
				}	
			}
			if ((length > 0) && (prevState != ReducedToken.FREE))
				blocks.addElement(new StateBlock(start,length,prevState));
			return blocks;
		}

}

