package edu.rice.cs.drjava;

public class Semaphore {
	Flag _f;
	int _value = 0;

	public Semaphore() {
		this._f = new DummyFlag();
	}

	public Semaphore(Flag f) {
		this._f = f;
	}

	synchronized void setFlag(Flag f) {
		this._f = f;
	}
	
	public synchronized void increment() {
		_value++;
		_f.raise();
	}
	
	public synchronized void decrement() {
		if (_value == 0) {
			throw new RuntimeException("attempt to decrement a Semaphore at zero");
		}
		_value--;
		if (_value == 0) {
			_f.lower();
		}
		else {
			_f.wave();
		}
	}

	public void signalProgress() {
		_f.wave();
	}
	
	public synchronized boolean atZero() {
		return (_value == 0);
	}

	public String toString()
		{
			return (new Integer(_value)).toString();
		}
}
