/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.util.jar;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarBuilder {
  private JarOutputStream _output;

  /**
   * Creates a file file without a manifest
   *
   * @param file the file to write the jar to
   * @throws IOException thrown if the file cannot be opened for writing
   */
  public JarBuilder(File file) throws IOException {
    _output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(file)), ManifestWriter.DEFAULT);
  }

  /**
   * Creates an empty jar file with the given manifest
   *
   * @param jar      the file to write the jar to
   * @param manifest the file that is the manifest for the archive
   * @throws IOException thrown if either file cannot be opened for reading
   */
  public JarBuilder(File jar, File manifest) throws IOException {
    _output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jar)), new Manifest(new FileInputStream(manifest)));
  }

  /**
   * Creates an empty jar file with the given manifest
   *
   * @param jar      the file to write the jar to
   * @param manifest the manifest file for the jar
   * @see ManifestWriter
   */
  public JarBuilder(File jar, Manifest manifest) {
    try {
      _output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jar)), manifest);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Takes a parent name and a field name and returns the concatenation of them correctly
   *
   * @param parent The parent directory
   * @param name   The name of the file or directory
   * @return the string concatenation of the parent and the name
   */
  private String makeName(String parent, String name) {
    String sep = "/"; // NOTE: This can be a '/' since it is a path in the jar file itself
    if( parent.equals("") )
      return name;
    if (parent.endsWith(sep))
      return parent + name;
    return parent + sep + name;
  }

  /**
   * Adds the file to the given path and name
   *
   * @param file     the file to be added
   * @param parent   the directory to the path in which the file is to be added
   * @param fileName the name of the file in the archive
   */
  public void addFile(File file, String parent, String fileName) throws IOException {
    byte data[] = new byte[2048];

    FileInputStream fi = new FileInputStream(file.getAbsolutePath());
    BufferedInputStream origin = new BufferedInputStream(fi, 2048);

    JarEntry entry = new JarEntry(makeName(parent, fileName));
    _output.putNextEntry(entry);

    int count = origin.read(data, 0, 2048);
    while (count != -1) {
      _output.write(data, 0, count);
      count = origin.read(data, 0, 2048);
    }

    origin.close();
  }

  /**
   * Add the directory into the directory specified by parent
   * @param dir the directory to add
   * @param parent the path inside the jar that the directory should be added to
   */
  public void addDirectoryRecursive(File dir, String parent) {
    addDirectoryRecursiveHelper(dir, parent, new byte[2048], new FileFilter() {
      public boolean accept(File pathname) {
        return true;
      }
    });
  }

  /**
   * Add the directory into the directory specified by parent
   * @param dir the directory to add
   * @param parent the path inside the jar that the directory should be added to
   * @param filter the filter used to filter the files
   */
  public void addDirectoryRecursive(File dir, String parent, FileFilter filter) {
    addDirectoryRecursiveHelper(dir, parent, new byte[2048], filter);
  }

  /**
   * Add the contents of a directory that match a filter to the archive
   * @param dir the directory to add
   * @param parent the directory to add into
   * @param buffer a buffer that is 2048 bytes
   * @param filter the FileFilter to filter the files by
   * @return true on success, false on failure
   */
  private boolean addDirectoryRecursiveHelper(File dir, String parent, byte[] buffer, FileFilter filter) {
    try {
      File[] files = dir.listFiles(filter);
      BufferedInputStream origin = null;

      if( files == null )
        return true;
      for (int i = 0; i < files.length; i++) {
        if( files[i].isFile() ) {
          origin = new BufferedInputStream(new FileInputStream(files[i]), 2048);

          JarEntry entry = new JarEntry(makeName(parent, files[i].getName()));
          _output.putNextEntry(entry);

          int count;
          while((count = origin.read(buffer, 0, 2048)) != -1) {
            _output.write(buffer, 0, count);
          }
          origin.close();
        }
        else if( files[i].isDirectory() ) {
          addDirectoryRecursiveHelper(files[i], makeName(parent, files[i].getName()),buffer,filter);
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   * Makes a directory in the jar file
   *
   * @param parent  The name of the parent that the directory is to be created in
   * @param dirName The name of the directory to be created
   * @return Returns true on success, false on failure
   */
  public boolean makeDirectory(String parent, String dirName) {
    JarEntry entry = new JarEntry(makeName(parent, dirName));
    try {
      _output.putNextEntry(entry);
    }
    catch (IOException e) {
      return false;
    }
    return true;
  }

  /**
   * Close writing on the jar file
   */
  public void close() throws IOException {
    _output.flush();
    _output.close();
  }
}