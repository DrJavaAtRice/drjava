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

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.util.FileOps;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.regex.Pattern;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.text.TextUtil;

/** Class representing a lazy lists of files that are found recursively inside a start directory.
  * @version $Id$
  */
public class RecursiveFileListProperty extends LazyFileListProperty {
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
    public FileMaskFilter(String mask) {
      super(TextUtil.regexEscape(mask)
              .replaceAll("\\\\\\*",".*") // turn \* into .*
              .replaceAll("\\\\\\?",".")); // turn \? into .
    }
  }
  
  /** Abstract factory method specifying the list. */
  protected List<File> getList() {
    FileMaskFilter fFilter = new FileMaskFilter(_attributes.get("filter"));
    FileMaskFilter fDirFilter = new FileMaskFilter(_attributes.get("dirfilter"));
    String start = StringOps.replaceVariables(_attributes.get("dir"), PropertyMaps.ONLY, PropertyMaps.GET_CURRENT);
    start = StringOps.unescapeSpacesWith1bHex(start);
    Iterable<File> it = edu.rice.cs.plt.io.IOUtil.listFilesRecursively(new File(start), fFilter, fDirFilter);
    StringBuilder sb = new StringBuilder();
    ArrayList<File> l = new ArrayList<File>();
    for(File f: it) {
      l.add(f);
    }
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
  }

  /** @return true if the specified property is equal to this one. */
  public boolean equals(Object other) {
    if (other == null || other.getClass() != this.getClass()) return false;
    RecursiveFileListProperty o = (RecursiveFileListProperty)other;
    return _name.equals(o._name) && (_isCurrent == o._isCurrent) && _value.equals(o._value);
  }
  
  /** @return the hash code. */
  public int hashCode() {
    int result;
    result = _name.hashCode();
    result = 31 * result + (_value.hashCode());
    result = 31 * result + (_isCurrent?1:0);
    return result;
  }
} 
