/* Project: Homework - 04
 * File: edu.rice.cs.util.jar.JarReader
 * Date: Feb 21, 2005 3:45:00 PM
 */

package edu.rice.cs.util.jar;

import java.util.jar.JarInputStream;
import java.io.*;

public class JarReader {
  private JarInputStream _input;

  public JarReader(InputStream in) throws IOException {
    _input = new JarInputStream(in);
  }
  public JarReader(File file) throws IOException {
    _input = new JarInputStream(new FileInputStream(file));
  }

//  public void getFile()
}
