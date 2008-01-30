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

/** Class representing all configuration options with values of type File.
 *  @version $Id$
 */
public class FileOption extends Option<File> {
  
  /** Special sentinal file indicating that this option is not set. */
  public static final File NULL_FILE = new File("") {
    public boolean canRead() { return false; }
    public boolean canWrite() { return false; }
    public int compareTo(File f) { return (f == this) ? 0 : -1; }
    public boolean createNewFile() { return false; }
    public boolean delete() { return false; }
    public void deleteOnExit() {}
    public boolean equals(Object o) { return o == this; }
    public boolean exists() { return true; }
    public File getAbsoluteFile() { return this; }
    public String getAbsolutePath() { return ""; }
    public File getCanonicalFile() { return this; }
    public String getCanonicalPath() { return ""; }
    public String getName() { return ""; }
    public String getParent() { return null; }
    public File getParentFile() { return null; }
    public String getPath() { return ""; }
    public int hashCode() { return getClass().hashCode(); }
    public boolean isAbsolute() { return false; }
    public boolean isDirectory() { return false; }
    public boolean isFile() { return false; }
    public boolean isHidden() { return false; }
    public long lastModified() { return 0L; }
    public long length() { return 0L; }
    public String[] list() { return null; }
    public String[] list(FilenameFilter filter) { return null; }
    public File[] listFiles() { return null; }
    public File[] listFiles(FileFilter filter) { return null; }
    public File[] listFiles(FilenameFilter filter) { return null; }
    public boolean mkdir() { return false; }
    public boolean mkdirs() { return false; }
    public boolean renameTo(File dest) { return false; }
    public boolean setLastModified(long time) { return false; }
    public boolean setReadOnly() { return false; }
    public String toString() { return ""; }
    //public URI toURI() {} (Defer to super implementation.)
    //public URL toURL() {} (Defer to super implementation.)
  };
  
  /** @param key The name of this option. */
  public FileOption(String key, File def) { super(key,def); }
  
  /** @param s The String to be parsed, must represent a legal file path for the File to be created.
   *  @return The absolute File object corresponding to the input path string.
   */
  public File parse(String s) { 
    if (s.trim().equals("")) return NULL_FILE;
    
    try { return new File(s).getAbsoluteFile(); }
    catch (NullPointerException e) { throw new OptionParseException(name, s, "Must have a legal filename."); }
  }

  /** @param f The instance of class File to be formatted.
   *  @return A String representing the absolute path of "f".
   */
  public String format(File f) { return f.getAbsolutePath(); }
}