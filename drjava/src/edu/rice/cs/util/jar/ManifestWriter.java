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

import edu.rice.cs.util.UnexpectedException;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.io.*;

/**
 * Writes manifest objects. Useful for creating Manifest files without writing them to files
 */
public class ManifestWriter {
  private List<String> _classPaths;
  private String _mainClass;
  public static final Manifest DEFAULT = new ManifestWriter().getManifest();

  /**
   * Create a new manifest file
   */
  public ManifestWriter() {
    _classPaths = new LinkedList<String>();
    _mainClass = null;
  }

  /**
   * Add a class path to the Manifest
   * @param path the path to be added
   */
  public void addClassPath(String path) {
    _classPaths.add(_classPaths.size(), path);
  }

  /**
   * Set the main class of the Manifest
   * @param mainClass
   */
  public void setMainClass(String mainClass) {
    _mainClass = mainClass;
  }

  /**
   * Get an input stream to the contents of the manifest file
   * @return an InputStream whose contents are the contents of the Manifest file
   */
  protected InputStream getInputStream() {
    // NOTE: All significant lines in the manifest MUST end in the end of line character

    StringBuffer sbuf = new StringBuffer();
    sbuf.append(Attributes.Name.MANIFEST_VERSION.toString());
    sbuf.append(": 1.0\n");
    if( !_classPaths.isEmpty() ) {
      Iterator<String> iter = _classPaths.iterator();
      sbuf.append(Attributes.Name.CLASS_PATH.toString());
      sbuf.append(":");
      while (iter.hasNext()) {
        sbuf.append(" ");
        sbuf.append(iter.next());
      }
      sbuf.append("\n");
    }
    if( _mainClass != null ) {
      sbuf.append(Attributes.Name.MAIN_CLASS.toString());
      sbuf.append(": ");
      sbuf.append(_mainClass);
      sbuf.append("\n");
    }
    try {
      return new ByteArrayInputStream(sbuf.toString().getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
      throw new UnexpectedException(e);
    }
  }

  /**
   * Get the Manifest object that this object created.
   * @return the Manifest that this builder created
   */
  public Manifest getManifest() {
    try {
      Manifest m = new Manifest();
      m.read(getInputStream());
      return m;
    }
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}