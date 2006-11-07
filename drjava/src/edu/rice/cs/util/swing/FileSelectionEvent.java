/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project from http://www.drjava.org/
 * or http://sourceforge.net/projects/drjava/
 *
 * DrJava Open Source License
 * 
 * Copyright (C) 2001-2006 JavaPLT group at Rice University (javaplt@rice.edu).  All rights reserved.
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

package edu.rice.cs.util.swing;

import java.util.EventObject;
import java.io.File;

public class FileSelectionEvent extends EventObject {
  
  protected File[] _changed;
  protected boolean[] _areNew;
  protected File _newLead;
  protected File _oldLead;
  
  public FileSelectionEvent(Object source, File changed, boolean isNew, File newLead, File oldLead) {
    this(source, new File[]{changed}, new boolean[]{isNew}, newLead, oldLead);
  }
  
  public FileSelectionEvent(Object source, File[] changed, boolean[] areNew, File newLead, File oldLead) {
    super(source);
    _changed = changed;
    _areNew = areNew;
    _newLead = newLead;
    _oldLead = oldLead;
  }
  
  public File getOldLeadSelectionFile() { return _oldLead; }
  
  public File getNewLeadSelectionFile() { return _newLead; }
  
  public File getFile() { return _changed[0]; }
  
  public File[] getFiles() { return _changed; }
  
  public boolean isAddedFile() { return _areNew[0]; }
  
  public boolean isAddedFile(int i) { return _areNew[i]; }
  
  public boolean isAddedFile(File f) {
    for (int i = 0; i < _changed.length; i++) {
      if (f.equals(_changed[i])) return _areNew[i];
    }
    throw new IllegalArgumentException("File, " + f + ", not found in changed files");
  }  
}