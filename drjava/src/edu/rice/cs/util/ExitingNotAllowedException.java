package edu.rice.cs.util;

public class ExitingNotAllowedException extends RuntimeException {
  public ExitingNotAllowedException() {
    super("The call to System.exit() was not allowed.");
  }
}
