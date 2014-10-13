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

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.plt.text.TextUtil;

/** Class representing a lazy lists of files that are found recursively inside a start directory.
  * @version $Id: RecursiveFileListProperty.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class RecursiveFileListProperty extends FileListProperty {
  /** Start directory. */
  protected String _start;
  /** Create an recursive file list property. */
  public RecursiveFileListProperty(String name, String sep, String dir, String start, String help) {
    super(name, sep, dir, help);
    _start = start;
    resetAttributes();
  }
  
  public static class RegexFilter implements FileFilter {
    protected String _regex;
    public RegexFilter(String regex) {
      _regex = regex;
    }
    public boolean accept(File pathname) {
      return pathname.getName().matches(_regex);
    }
  }
  public static class FileMaskFilter extends RegexFilter {
    private HashSet<File> _include = new HashSet<File>();
    private HashSet<File> _exclude = new HashSet<File>();
    public FileMaskFilter(String mask) {
      super(TextUtil.regexEscape(mask)
              .replaceAll("\\\\\\*",".*") // turn \* into .*
              .replaceAll("\\\\\\?",".")); // turn \? into .
    }
    public boolean accept(File pathname) {
      if (_include.contains(pathname)) { return true; }
      if (_exclude.contains(pathname)) { return false; }
      return super.accept(pathname);
    }
    public void addIncludedFile(File f) { _include.add(f); }
    public void removeIncludedFile(File f) { _include.remove(f); }
    public void clearIncludedFile() { _include.clear(); }
    public void addExcludedFile(File f) { _exclude.add(f); }
    public void removeExcludedFile(File f) { _exclude.remove(f); }
    public void clearExcludedFile() { _exclude.clear(); }
  }
  
  /** Abstract factory method specifying the list.
    * @param pm PropertyMaps used for substitution when replacing variables */
  protected List<File> getList(PropertyMaps pm) {
    FileMaskFilter fFilter = new FileMaskFilter(_attributes.get("filter"));
    FileMaskFilter fDirFilter = new FileMaskFilter(_attributes.get("dirfilter"));
    String start = StringOps.replaceVariables(_attributes.get("dir"), pm, PropertyMaps.GET_CURRENT);
    start = StringOps.unescapeFileName(start);
    File fStart = new File(start);
    // if the specified starting point is a directory, allow that directory
    if (fStart.isDirectory()) { fDirFilter.addIncludedFile(fStart); }
    Iterable<File> it = edu.rice.cs.plt.io.IOUtil.listFilesRecursively(fStart, fFilter, fDirFilter);
//    StringBuilder sb = new StringBuilder();  // not used
    ArrayList<File> l = new ArrayList<File>();
    for(File f: it) { l.add(f); }
    return l;
  }
  
  /** Reset the attributes. */
  public void resetAttributes() {
    _attributes.clear();
    _attributes.put("sep", _sep);
    _attributes.put("rel", _dir);
    _attributes.put("dir", _start);
    _attributes.put("filter", "*");
    _attributes.put("dirfilter", "*");
    _attributes.put("squote", null);
    _attributes.put("dquote", null);
  }
} 
