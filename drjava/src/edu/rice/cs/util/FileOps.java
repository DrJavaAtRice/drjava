/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * any Java compiler, even if it is provided in binary-only form, and distribute
 * linked combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than Java compilers.
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so.  If you do not wish to
 * do so, delete this exception statement from your version.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util;

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
   * Reads the stream until it reaches EOF, and then returns the read
   * contents as a byte array. This call may block, since it will not
   * return until EOF has been reached.
   *
   * @param stream Input stream to read.
   * @return Byte array consisting of all data read from stream.
   */
  public static byte[] readStreamAsBytes(final InputStream stream)
    throws IOException
  {
    BufferedInputStream buffered;

    if (stream instanceof BufferedInputStream) {
      buffered = (BufferedInputStream) stream;
    }
    else {
      buffered = new BufferedInputStream(stream);
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int readVal = buffered.read();
    while (readVal != -1) {
      out.write(readVal);
      readVal = buffered.read();
    }

    stream.close();
    return out.toByteArray();
  }

  /**
   * Read the entire contents of a file and return them.
   */
  public static String readFileAsString(final File file) throws IOException {
    FileReader reader = new FileReader(file);
    StringBuffer buf = new StringBuffer();

    while (reader.ready()) {
      char c = (char) reader.read();
      buf.append(c);
    }

    reader.close();
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
  public static File writeStringToNewTempFile(final String prefix,
                                              final String suffix,
                                              final String text)
    throws IOException
  {
    File file = File.createTempFile(prefix, suffix);
    writeStringToFile(file, text);
    return file;
  }

  /**
   * Writes text to the file.
   *
   * @param file File to write to
   * @param text Text to write
   */
  public static void writeStringToFile(File file, String text)
    throws IOException
  {
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
