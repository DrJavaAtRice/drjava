/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2005 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
 *
 * Developed by:   Java Programming Languages Team, Rice University, http://www.cs.rice.edu/~javaplt/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal with the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 *     - Redistributions of source code must retain the above copyright notice, this list of conditions and the 
 *       following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the 
 *       following disclaimers in the documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the names of its contributors may be used to 
 *       endorse or promote products derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor use the term "DrJava" as part of their 
 *       names without prior written permission from the JavaPLT group.  For permission, write to javaplt@rice.edu.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * WITH THE SOFTWARE.
 * 
 *END_COPYRIGHT_BLOCK*/

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