package edu.rice.cs.drjava;

public class UnexpectedException extends RuntimeException {

    private Exception _value;

    /**
     * Constructs an unexpected exception with
     *<code>value.toString()</code> as it's message.
     */
    public UnexpectedException(Exception value) {
	super(value.toString());
	_value = value;
    }

    /**
     * Returns the contained exception.
     */
    public Exception getValue() {
	return _value;
    }
}
