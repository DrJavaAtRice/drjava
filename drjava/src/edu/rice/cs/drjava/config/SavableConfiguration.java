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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
 END_COPYRIGHT_BLOCK*/
package edu.rice.cs.drjava.config;
import java.io.*;
import gj.util.Enumeration;
import java.util.Properties;
import java.util.Arrays;
import java.util.Vector;
import java.util.Date;

/**
 * A Configuration object that can be read and saved from a Stream.
 * @version $Id$
 */
public class SavableConfiguration extends Configuration {  
  
  private File file;
  
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
   */
  public void saveConfiguration(OutputStream os, String header) throws IOException {
    OutputStreamWriter osw = new OutputStreamWriter(os);
    Enumeration<OptionParser> keys = map.keys();
    //Properties p = new Properties();
    String tmpString;
    StringBuffer buff;
    OptionParser key;
    Date date = new Date();
    osw.write((int)'#');
    osw.write(header, 0, header.length());
    osw.write((int)'\n');
    osw.write((int)'#');
    osw.write(date.toString(), 0, date.toString().length());
    osw.write((int)'\n');
    while(keys.hasMoreElements()) {
      key = keys.nextElement();
      tmpString = key.getName();
      osw.write(tmpString, 0, tmpString.length());
      tmpString = " = ";
      osw.write(tmpString, 0, 3);
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
    osw.close();
    //p.store(os,header)
    
  }
}
