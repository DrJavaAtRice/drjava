package edu.rice.cs.drjava;

import java.io.Reader;

/**
 * An interface to give GlobalModel a Reader and a file name so
 * it can do file operations without having to muck around with
 * FileReader creation.
 */
public interface ReaderCommand {

/**
   * Get the file name to which this writer corresponds.
   * The name is irrelevant if the Reader is a StringReader
   * used for testing.  However, the programmer may wish to
   * add a String constructor argument as the source
   * from which the Reader will derive the text.
   */
  public String getName();
  
  /**
   * Get the Reader which will carry the file's contents into
   * the document.
   */
  public Reader getReader();

}