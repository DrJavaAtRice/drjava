package edu.rice.cs.drjava.model.repl;

/**
 * Exception to signify that something went wrong during an interaction.
 *
 * @version $Id$
 */
public class InteractionsException extends RuntimeException {
  public InteractionsException(String s) {
    super(s);
  }
}
