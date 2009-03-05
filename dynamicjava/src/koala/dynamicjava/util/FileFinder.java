/*
 * DynamicJava - Copyright (C) 1999-2001
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DYADE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dyade shall not be
 * used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization from
 * Dyade.
 *
 */

package koala.dynamicjava.util;

import java.io.*;
import java.util.*;

/**
 * This class represents an object that manages a set of path where to
 * find files.
 *
 * @author  Stephane Hillion
 * @version 1.0 - 1999/05/18
 */

public class FileFinder {
  /**
   * The paths
   */
  private List<String> paths;
  
  /**
   * Creates a new file finder
   */
  public FileFinder() {
    paths = new LinkedList<String>();
  }
  
  /**
   * Adds a search path. This path becomes the one with the highest priority.
   * @param path the path to add
   */
  public void addPath(String path) {
    String s = (path.endsWith("/")) ? path : path + "/";
    paths.remove(s);
    paths.add(0, s);
  }
  
  /**
   * Searches and returns a file.
   * @param name the base name of the file to find
   * @throws IOException if no file was found
   */
  public File findFile(String name) throws IOException {
    for (String s : paths) {
      File f = new File(s + name);
      if (f.exists()) {
        return f;
      }
    }
    throw new IOException("File Not Found: " + name);
  }
}
