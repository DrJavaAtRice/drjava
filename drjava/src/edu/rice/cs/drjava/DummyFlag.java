package edu.rice.cs.drjava;

public class DummyFlag implements Flag {
	public void raise() {
		MainFrame._status.setText("updating colors...");
	}
	public void lower() {
		MainFrame._status.setText("done.");
	}
	public void wave() {
		MainFrame._status.setText(MainFrame._status.getText() + ".");;
	}
}
