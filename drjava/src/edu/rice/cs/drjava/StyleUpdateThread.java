package edu.rice.cs.drjava;

//import java.util.Thread;
import gj.util.Vector;

public class StyleUpdateThread extends Thread {
	private DefinitionsDocument _doc;
	private Vector<StyleUpdateMessage> _messages;
	private Semaphore _taskCounter;
	
	public StyleUpdateThread(DefinitionsDocument doc) {													 
		this._doc = doc;
		this._taskCounter = doc._taskCounter;
		this._messages = new Vector<StyleUpdateMessage>();
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
			nextMessage.dispatch(_doc, _messages);
			_taskCounter.decrement();
		}
	}

	public void sendMessage(StyleUpdateMessage message)
		{
			this._messages.addElement(message);
		}
}
