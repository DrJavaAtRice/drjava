package edu.rice.cs.drjava;

/**
 * An exception thrown by DefinitionsDocument.getPackageName() when the
 * document contains an invalid package statement.
 * This can happen if there is nothing between "package" and ";", or if there
 * is no terminating semicolon ever.
 *
 * @version $Id$
 */
public class InvalidPackageException extends Exception {
  private final int _location;

   /**
    * Constructs a exception
    * @param location The location in the document where the invalid package
    *                 statement begins.
    * @param message  Textual explanation of the problem.
   */
  public InvalidPackageException(int location, String message) {
    super(message);
    _location = location;
  }

  /**
   * Returns the location of the problem.
   */
  public int getLocation() {
    return _location;
  }
}
