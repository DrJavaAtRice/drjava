/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2008, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;

import java.io.*;
import java.util.Iterator;
import java.util.Date;

/** A Configuration object that can be read and saved from a Stream.
 *  @version $Id$
 */
public class SavableConfiguration extends Configuration {
  /** Creates a new Configuration based on the given OptionMap.
   * @param map an empty OptionMap
   */
  public SavableConfiguration(OptionMap map) { super(map); }

  /** Creates an OptionMapLoader with the values loaded from the InputStream
   * (and defaults where values weren't specified) and loads them into
   * this Configuration's OptionMap.
   * @param is InputStream containing properties-style keys and values
   */
  public void loadConfiguration(InputStream is) throws IOException {
    new OptionMapLoader(is).loadInto(map);
  }

  /** Used to save the values from this Configuration into the given OutputStream
   * as a Properties file. The elements weren't ordered, so now the properties
   * are written in the same way as the about dialog.
   * Values equal to their defaults are not written to disk.
   */
  public void saveConfiguration(OutputStream os, String header) throws IOException {
    Writer w = new BufferedWriter(new OutputStreamWriter(os));
    Iterator<OptionParser<?>> keys = map.keys();
    //Properties p = new Properties();
//    String tmpString;
//    StringBuffer buff;
//    OptionParser<?> key;
    
    // Write the header
    Date date = new Date();
    w.write((int)'#');
    w.write(header, 0, header.length());
    w.write((int)'\n');
    w.write((int)'#');
    w.write(date.toString(), 0, date.toString().length());
    w.write((int)'\n');

    // Write each option
    while (keys.hasNext()) {
      
      OptionParser<?> key = keys.next();

      if (!key.getDefault().equals(map.getOption(key))) {

        // Write name
        String tmpString = key.getName();
        w.write(tmpString, 0, tmpString.length());

        // Write equals sign
        tmpString = " = ";
        w.write(tmpString, 0, 3);

        // Write value
        tmpString = map.getString(key);
        // This replaces all backslashes with two backslashes for windows
        int index = 0;
        int pos;
        while (index < tmpString.length() &&
               ((pos = tmpString.indexOf('\\', index)) >= 0)) {
          final StringBuilder buff = new StringBuilder(tmpString);  // should use StringBuilder, but not 1.4 compatible
          buff.insert(pos, '\\');
          index = pos + 2;
          tmpString = buff.toString();
        }
        w.write(tmpString, 0, tmpString.length());
        w.write((int)'\n');

        // p.setProperty(key.getName(),map.getString(key));
      }
    }
    w.close();
    //p.store(os,header)
  }
}
