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

package edu.rice.cs.drjava.config;

import java.io.*;
import java.util.Iterator;
import java.util.Date;

/**
 * A Configuration object that can be read and saved from a Stream.
 * @version $Id$
 */
public class SavableConfiguration extends Configuration {
  /**
   * Creates a new Configuration based on the given OptionMap.
   * @param map an empty OptionMap
   */
  public SavableConfiguration(OptionMap map) {
    super(map);
  }

  /**
   * Creates an OptionMapLoader with the values loaded from the InputStream
   * (and defaults where values weren't specified) and loads them into
   * this Configuration's OptionMap.
   * @param is InputStream containing properties-style keys and values
   */
  public void loadConfiguration(InputStream is) throws IOException {

    new OptionMapLoader(is).loadInto(map);
  }

  /**
   * Used to save the values from this Configuration into the given OutputStream
   * as a Properties file. The elements weren't ordered, so now the properties
   * are written in the same way as the about dialog.
   * Values equal to their defaults are not written to disk.
   */
  public void saveConfiguration(OutputStream os, String header) throws IOException {
    OutputStreamWriter osw = new OutputStreamWriter(os);
    Iterator<OptionParser> keys = map.keys();
    //Properties p = new Properties();
    String tmpString;
    StringBuffer buff;
    OptionParser key;

    // Write the header
    Date date = new Date();
    osw.write((int)'#');
    osw.write(header, 0, header.length());
    osw.write((int)'\n');
    osw.write((int)'#');
    osw.write(date.toString(), 0, date.toString().length());
    osw.write((int)'\n');

    // Write each option
    while (keys.hasNext()) {
      key = keys.next();

      if (!key.getDefault().equals(map.getOption(key))) {

        // Write name
        tmpString = key.getName();
        osw.write(tmpString, 0, tmpString.length());

        // Write equals sign
        tmpString = " = ";
        osw.write(tmpString, 0, 3);

        // Write value
        tmpString = map.getString(key);
        // This replaces all backslashes with two backslashes for windows
        int index = 0;
        int pos;
        while (index < tmpString.length() &&
               ((pos = tmpString.indexOf('\\', index)) >= 0)) {
          buff = new StringBuffer(tmpString);
          buff.insert(pos, '\\');
          index = pos + 2;
          tmpString = buff.toString();
        }
        osw.write(tmpString, 0, tmpString.length());
        osw.write((int)'\n');

        // p.setProperty(key.getName(),map.getString(key));
      }
    }
    osw.close();
    //p.store(os,header)
  }
}
