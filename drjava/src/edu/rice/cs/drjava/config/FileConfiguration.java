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
import edu.rice.cs.util.FileOps;

/** A Configuration object that is backed by a file.
 *  @version $Id$
 */
public class FileConfiguration extends SavableConfiguration {  
  
  public final File file;
  
  /** Creates a new Configuration object using the values stored in file f.  Any values not specified by f will be 
   *  set to defaults from OptionConstants.
   *  @param f Properties file containing customized values
   */
  public FileConfiguration(File f) {
    super(new DefaultOptionMap());
    file = f.getAbsoluteFile();
  }
  
  /** Calls SavableConfiguration.loadConfiguration, which loads all values from the file, based on the defaults in
   *  OptionConstants.
   */
  public void loadConfiguration() throws IOException {
    loadConfiguration(new BufferedInputStream(new FileInputStream(file)));
  }
  
  /** Saves the current settings to the stored properties file. */
  public void saveConfiguration() throws IOException {
    saveConfiguration("DrJava configuration file");
  }
  
  /** Saves the current settings to the stored properties file.
   *  @param header Description of the properties list
   */
  public void saveConfiguration(final String header) throws IOException {
    FileOps.saveFile(new FileOps.DefaultFileSaver(file) {
      public void saveTo(OutputStream os) throws IOException {
        saveConfiguration(os,header);
      }
      public boolean shouldBackup() {
        return false;
      }
    });
  }
}
