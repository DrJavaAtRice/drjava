package edu.rice.cs.drjava;

//import java.util.Thread;
import javax.swing.text.*;
import gj.util.Vector;


public class StyleUpdateThread extends Thread {
	private DefinitionsDocument _doc;
	public Vector<StateBlock> _changedStates;
	public int _breakLocation;

	public StyleUpdateThread(DefinitionsDocument doc,
													 Vector<StateBlock> changedStates) {
		this._doc = doc;
		this._changedStates = changedStates;
		this._breakLocation = _doc.getLength();
	}

	public void run() {
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		//int startOfInterimText = _changedStates.elementAt(0).location;
		// Paint all affected text yellow as interim color.
//		StyleConstants.setForeground(attributes, StateBlock.INTERIM_COLOR);
//		_doc.setCharacterAttributes(startOfInterimText,
//													 _doc.getLength() - startOfInterimText,
//													 attributes,
//													 false);
		//synchronized(this) {
//		try {sleep(150);}
//		catch (InterruptedException ex) {}
//			}
		for (int i = 0; i < _changedStates.size(); i++) {
			StateBlock currentSB = _changedStates.elementAt(i);
			if (currentSB.location >= _breakLocation) {
				break;
			}
			StyleConstants.setForeground(attributes, currentSB.state);
			_doc.setCharacterAttributes(currentSB.location,
																	currentSB.size,
																	attributes,
																	false);			
		}
	}
}



