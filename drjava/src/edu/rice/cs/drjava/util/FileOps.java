package edu.rice.cs.drjava.util;

import java.io.*;

/**
 * A class to provide some convenient file operations as static methods.
 * It's abstract to prevent (useless) instantiation, though it can be subclassed
 * to provide convenient namespace importation of its methods.
 *
 * @version $Id$
 */
public abstract class FileOps {
  /**
   * Read the entire contents of a file and return them.
   */
  public static String readFile(final File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      char c = (char) reader.read();
      buf.append(c);
    }

    return buf.toString();
  }

  /**
   * Creates a new temporary file and writes the given text to it.
   * 
   * @param prefix Beginning part of file name, before unique number
   * @param suffix Ending part of file name, after unique number
   * @param text Text to write to file
   *
   * @return name of the temporary file that was created
   */
  public static File writeToNewTempFile(final String prefix,
                                        final String suffix,
                                        final String text)
    throws IOException
  {
    File file = File.createTempFile(prefix, suffix);
    writeToFile(file, text);
    return file;
  }

  /**
   * Writes text to the file.
   *
   * @param file File to write to
   * @param text Text to write
   */
  public static void writeToFile(File file, String text) throws IOException {
    FileWriter writer = new FileWriter(file);
    writer.write(text);
    writer.close();
  }

  /**
   * Create a new temporary directory.
   *
   * @param name Non-unique portion of the name of the directory to create.
   * @return File representing the directory that was created.
   */
  public static File createTempDirectory(final String name) throws IOException {
    return createTempDirectory(name, null);
  }

  /**
   * Create a new temporary directory.
   *
   * @param name Non-unique portion of the name of the directory to create.
   * @param parent Parent directory to contain the new directory
   * @return File representing the directory that was created.
   */
  public static File createTempDirectory(final String name, final File parent)
    throws IOException
  {
    File file =  File.createTempFile(name, "", parent);
    file.delete();
    file.mkdir();

    return file;
  }

  /**
   * Delete the given directory including any files and directories it contains.
   *
   * @param dir File object representing directory to delete. If, for some
   *            reason, this file object is not a directory, it will still be
   *            deleted.
   *
   * @return true if there were no problems in deleting. If it returns
   *         false, something failed and the directory contents likely at least
   *         partially still exist.         
   */
  public static boolean deleteDirectory(final File dir) {
    if (! dir.isDirectory()) {
      return dir.delete();
    }

    boolean ret = true;
    File[] childFiles = dir.listFiles();
    for (int i = 0; i < childFiles.length; i++) {
      ret = ret && deleteDirectory(childFiles[i]);
    }
    
    // Now we should have an empty directory
    ret = ret && dir.delete();
    return ret;
  }
}
