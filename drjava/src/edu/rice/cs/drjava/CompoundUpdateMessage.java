package edu.rice.cs.drjava;

import gj.util.Vector;
import javax.swing.text.*;
import javax.swing.SwingUtilities;

class CompoundUpdateMessage extends StyleUpdateMessage
{
	private Vector<StateBlock> _states;
	StateBlock currentSB;

	CompoundUpdateMessage()
		{
			super();
			this._states = new Vector<StateBlock>();
		}
	public CompoundUpdateMessage(Vector<StateBlock> states)
		{
			super();
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

	public void dispatch(DefinitionsDocument doc,
											 Vector<StyleUpdateMessage> queue)		
		{
			this.target = doc;
			for (int i = 0; i < this.size(); i++) {
				currentSB = this.elementAt(i);
				if ((queue.size() != 0) &&
						(queue.elementAt(0) instanceof CompoundUpdateMessage) &&
						(currentSB.location >
						 ((CompoundUpdateMessage)queue.elementAt(0))
						 .elementAt(0).location))
					{
						// Abort the current update because we got a new one.
						// The abort happens once we've updated using the old info
						// at least up to where the new one starts.
						break;
					}
				
				StyleConstants.setForeground(attributes, currentSB.state);
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							try {
								if ((currentSB.location + currentSB.size) <=
										target.getLength())
									target.setCharacterAttributes(currentSB.location,
																								currentSB.size,
																								attributes,
																								false);
							} catch (Exception e) {}
							
						}
					});
				} catch (Exception e) {}
				
			}
		}
}
