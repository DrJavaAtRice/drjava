package edu.rice.cs.drjava;

import gj.util.Vector;
import java.awt.Color;
import javax.swing.text.*;
import javax.swing.SwingUtilities;

class SimpleUpdateMessage extends StyleUpdateMessage
{
	private StateBlock _stateBlock;

	public SimpleUpdateMessage(int start, int length, Color highlight)
		{
			super();
			this._stateBlock = new StateBlock(start, length, highlight);
		}

	public StateBlock getStateBlock() {
		return this._stateBlock;
	}

	public void dispatch(DefinitionsDocument doc,
											 Vector<StyleUpdateMessage> queue)
		{
			target = doc;
			StyleConstants.setForeground(attributes, _stateBlock.state);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							try {
								if ((_stateBlock.location + _stateBlock.size) <=
										target.getLength())
									target.setCharacterAttributes(_stateBlock.location,
																								_stateBlock.size,
																								attributes,
																								false);
							} catch (Exception e) {}
						}
				});
			} catch (Exception e) {}

			
		}
}
