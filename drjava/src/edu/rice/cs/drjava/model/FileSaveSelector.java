package edu.rice.cs.drjava.model;

import java.io.File;
/**
 * An interface to give GlobalModel a file to save a
 * document to.
 */
public interface FileSaveSelector {

  public File getFile() throws OperationCanceledException;

}