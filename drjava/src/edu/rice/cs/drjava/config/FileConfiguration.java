/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2012, JavaPLT group at Rice University (drjava@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, DrScala, the JavaPLT group, Rice University, nor the
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
 * This file is part of DrScala.  Download the current version of this project
 * from http://www.drscala.org/.
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.config;
import java.io.*;
import edu.rice.cs.util.FileOps;

/** A Configuration object that is backed by a file.
  * @version $Id: FileConfiguration.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class FileConfiguration extends SavableConfiguration {  
  
  protected final File file;
  
  /** Creates a new Configuration object using the values stored in file f.  Any values not specified by f will be 
    * set to defaults from OptionConstants.
    * @param f Properties file containing customized values
    */
  public FileConfiguration(File f) {
    super(new DefaultOptionMap());
    file = f.getAbsoluteFile();
  }
  
  /** Returns the file that this configuration is backed by. */
  public File getFile() { return file; }
  
  /** Calls SavableConfiguration.loadConfiguration, which loads all values from the file, based on the defaults in
    * OptionConstants.
    */
  public void loadConfiguration() throws IOException {
    loadConfiguration(new BufferedInputStream(new FileInputStream(file)));
  }
  
  /** Saves the current settings to the stored properties file. */
  public void saveConfiguration() throws IOException {
    saveConfiguration("DrScala configuration file");
  }
  
  /** Saves the current settings to the stored properties file.
    * @param header Description of the properties list
    */
  public void saveConfiguration(final String header) throws IOException {
    FileOps.saveFile(new FileOps.DefaultFileSaver(file) {
      public void saveTo(OutputStream os) throws IOException { saveConfiguration(os,header); }
      public boolean shouldBackup() { return false; }
    });
  }
}
