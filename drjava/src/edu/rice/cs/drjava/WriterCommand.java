package edu.rice.cs.drjava;

import java.io.Writer;

/**
 * An interface to give GlobalModel a writer and a file name so
 * it can do file operations without having to muck around with
 * FileWriter creation.
 */
public interface WriterCommand {

  /**
   * Get the file name to which this writer corresponds.
   * The name is irrelevant if the Writer is a StringWriter
   * used for testing.  However, the programmer may wish to
   * endow the implementation of WriterCommand with a method
   * that can access the Writer's buffer to facilitate easy
   * testing of whether something was written.
   */
  public String getName();
  
  /**
   * Get the Writer which will carry the document's contents
   * to a file.
   */
  public Writer getWriter();

}