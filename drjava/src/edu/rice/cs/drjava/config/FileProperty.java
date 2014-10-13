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

import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.util.StringOps;
import edu.rice.cs.util.FileOps;
import java.util.HashSet;
import java.io.*;

/** Property that evaluates to a file and that can be inserted as variables in external processes.
  * @version $Id: FileProperty.java 5594 2012-06-21 11:23:40Z rcartwright $
  */
public class FileProperty extends DrJavaProperty {
  protected Thunk<File> _getFile;
  /** Create an eager file property. */
  public FileProperty(String name, Thunk<File> getFile, String help) {
    super(name,help);
    _getFile = getFile;
    resetAttributes();
  }
  
  /** Return the value of the property. If it is not current, update first.
    * @param pm PropertyMaps used for substitution when replacing variables */
  public String getCurrent(PropertyMaps pm) {
    update(pm);
    if (_value == null) { throw new IllegalArgumentException("DrScalaProperty value is null"); }
    _isCurrent = true;
    return _value;
  }

  /** Return the value. */
  public String toString() { return _value; }
  
  /** Return true if the value is current. */
  public boolean isCurrent() { return true; }
  
  /** Mark the value as stale. */
  public void invalidate() {
    // nothing to do, but tell those who are listening
    invalidateOthers(new HashSet<DrJavaProperty>());
  }
  
  /** Update the value of the property.
    * @param pm PropertyMaps used for substitution */
  public void update(PropertyMaps pm) {
    String quot = "";
    String q = _attributes.get("squote");
    if (q != null) {
      if (q.toLowerCase().equals("true")) { quot = "'"; }
    }
    q = _attributes.get("dquote");
    if (q != null) {
      if (q.toLowerCase().equals("true")) { quot = "\"" + quot; }
    }
    try {
      File f;
      if (_getFile == null || (f = _getFile.value()) == null) {
        _value = "";
        return;
      }
      if (_attributes.get("rel").equals("/")) {
        f = f.getAbsoluteFile();
        try { f = f.getCanonicalFile(); }
        catch(IOException ioe) { }
        _value = edu.rice.cs.util.StringOps.escapeFileName(f.toString());
      }
      else {
        File rf = new File(StringOps.unescapeFileName(StringOps.replaceVariables(_attributes.get("rel"), 
                                                                                        pm,
                                                                                        PropertyMaps.GET_CURRENT)));
        String s = FileOps.stringMakeRelativeTo(f,rf);
        _value = quot+edu.rice.cs.util.StringOps.escapeFileName(s)+quot;
      }
    }
    catch(IOException e) { _value = "(Error...)"; }
    catch(SecurityException e) { _value = "(Error...)"; }
  }    

  public void resetAttributes() {
    _attributes.clear();
    _attributes.put("rel", "/");
    _attributes.put("squote", null);
    _attributes.put("dquote", null);
  }
} 
