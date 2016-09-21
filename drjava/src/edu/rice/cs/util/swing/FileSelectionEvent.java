/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2010, JavaPLT group at Rice University (drjava@rice.edu)
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