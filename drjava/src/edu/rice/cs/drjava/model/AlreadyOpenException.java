package edu.rice.cs.drjava.model;

/**
 * Indicates that the file attempting to be opened is
 * already open.
 * @version $Id$
 */
public class AlreadyOpenException extends Exception {
  private OpenDefinitionsDocument _openDoc;

  /**
   * Exception indicating that the requested file is
   * already open.
   * @param doc the currently open document
   */
  public AlreadyOpenException(OpenDefinitionsDocument doc) {
    _openDoc = doc;
  }

  /**
   * @return the currently open document for the requested file
   */
  public OpenDefinitionsDocument getOpenDocument() {
    return _openDoc;
  }

}