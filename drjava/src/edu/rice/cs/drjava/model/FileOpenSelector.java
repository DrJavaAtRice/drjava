package edu.rice.cs.drjava.model;

import java.io.File;

/**
 * An interface to give GlobalModel a file to open from.
 */
public interface FileOpenSelector {

  public File getFile() throws OperationCanceledException;
  
}