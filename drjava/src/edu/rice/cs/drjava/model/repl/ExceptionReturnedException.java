package edu.rice.cs.drjava.model.repl;

/**
 * Exception to be returned by the interpreter to contain the exception that
 * occurred during interpretation.
 *
 * @version $Id$
 */
public class ExceptionReturnedException extends Exception {
  private final Throwable _contained;

  public ExceptionReturnedException(Throwable t) {
    _contained = t;
  }

  public Throwable getContainedException() { return _contained; }
}
