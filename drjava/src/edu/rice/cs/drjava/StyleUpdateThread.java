package edu.rice.cs.drjava;

//import java.util.Thread;
import javax.swing.text.*;
import gj.util.Vector;
import javax.swing.SwingUtilities;

public class StyleUpdateThread extends Thread {
	private DefinitionsDocument _doc;
	private Vector<StyleUpdateMessage> _messages;
	private Semaphore _taskCounter;
	SimpleAttributeSet attributes;
	StateBlock currentSB;
	
	public StyleUpdateThread(DefinitionsDocument doc) {													 
		this._doc = doc;
		this._taskCounter = doc._taskCounter;
		this._messages = new Vector<StyleUpdateMessage>();
		this.attributes = new SimpleAttributeSet();
	}

	public void run() {
		while (true) {
			while (_messages.size() == 0) {
				try {
					sleep(100);
				}
				catch (InterruptedException e) {}
			}

			StyleUpdateMessage nextMessage = _messages.elementAt(0);
			_messages.removeElementAt(0);
			
			if (nextMessage instanceof CompoundUpdateMessage) {
				CompoundUpdateMessage compoundMessage =
					(CompoundUpdateMessage)nextMessage;
				for (int i = 0; i < compoundMessage.size(); i++) {
					currentSB = compoundMessage.elementAt(i);
					if ((_messages.size() != 0) &&
							(_messages.elementAt(0) instanceof CompoundUpdateMessage) &&
							(currentSB.location >
							 ((CompoundUpdateMessage)_messages.elementAt(0))
							 .elementAt(0).location)) {
						// Abort the current update because we got a new one.
						// The abort happens once we've updated using the old info
						// at least up to where the new one starts.
						break;
					}
				
					StyleConstants.setForeground(attributes, currentSB.state);
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								_doc.setCharacterAttributes(currentSB.location,
																						currentSB.size,
																						attributes,
																						false);
							}
						});
					} catch (Exception e) {}
						
						
//					} catch (ArrayIndexOutOfBoundsException e) {
//						System.err.println("len=" + _doc.getLength() +
//																				", cur=" + _doc.getCurrentLocation() +
//																				", loc=" + currentSB.location +
//																				", +=" + currentSB.size);
//					}
//					_taskCounter.signalProgress();
//					DrJava.consoleOut().println("Compound: " + currentSB.location + ", " +
//														 currentSB.size + ": " +
//														 currentSB.state);
				}
			}
			else if (nextMessage instanceof SimpleUpdateMessage) {
					currentSB = ((SimpleUpdateMessage)nextMessage)
						.getStateBlock();
					StyleConstants.setForeground(attributes, currentSB.state);
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								_doc.setCharacterAttributes(currentSB.location,
																						currentSB.size,
																						attributes,
																						false);
							}
						});
					} catch (Exception e) {}
//					DrJava.consoleOut().println("Simple: " + currentSB.location + ", " +
//														 currentSB.size + ": " +
//														 currentSB.state);

			}
			else { // nextMessage instanceof NoUpdateMessage
//					DrJava.consoleOut().println("No update");
			}
			_taskCounter.decrement();
		}
	}

	public void sendMessage(StyleUpdateMessage message)
		{
			this._messages.addElement(message);
			//if (this._messages.size() == 1) {
			//	this.interrupt();
			//}
		}
	

}



