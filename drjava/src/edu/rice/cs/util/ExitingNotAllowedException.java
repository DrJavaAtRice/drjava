package edu.rice.cs.util;

/**
 * An exception to be thrown when System.exit is called when the
 * {@link PreventExitSecurityManager} is in force.
 *
 * @version $Id$
 */
public class ExitingNotAllowedException extends RuntimeException {
  public ExitingNotAllowedException() {
    super("The call to System.exit() was not allowed.");
  }
}
