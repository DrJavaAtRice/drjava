package edu.rice.cs.drjava;

/**
 * An exception which DrJava throws on an unexpected error.
 * Many times, we have to catch BadLocationExceptions in
 * code that accesses DefinitionDocument, even if we know for a
 * fact that a BadLocationException cannot occur.  In that case,
 * and in other similar cases where we know that an exception should not
 * occur, we throw this on the off chance that something does go wrong.
 * This aids us in debugging the code.
 * @version $Id$
 */
public class UnexpectedException extends RuntimeException {

  private Throwable _value;

   /**
   * Constructs an unexpected exception with
   * <code>value.toString()</code> as it's message.
   */
  public UnexpectedException(Throwable value) {
    super(value.toString());
    _value = value;
  }

  /**
   * Returns the contained exception.
   */
  public Throwable getContainedThrowable() {
    return _value;
  }
}
